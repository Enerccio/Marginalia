package com.github.enerccio.marginalia.domain.service.impl.inference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.concurrent.AsyncRunnableWrapper;
import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.InferenceException.Type;
import com.github.enerccio.marginalia.domain.service.function.FunctionCallException;
import com.github.enerccio.marginalia.domain.service.function.FunctionDefinition;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.domain.service.structured.SchemaCheck;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs;
import com.github.enerccio.marginalia.domain.traits.SupportedAI;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.google.gson.JsonElement;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.core.Timeout;
import com.openai.core.http.StreamResponse;
import com.openai.errors.OpenAIIoException;
import com.openai.errors.OpenAIServiceException;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.*;
import com.openai.models.models.Model;
import com.openai.models.models.ModelListPage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.context.request.RequestAttributes;

import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

@SupportedAI(AIType.OPEN_AI_COMPATIBLE)
@Configurable
public class OpenAICompatibleInferenceService implements InferenceService, InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(OpenAICompatibleInferenceService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final int DEFAULT_TIMEOUT_SECONDS = 300;
    public static final int DEFAULT_MAX_RETRIES = 2;
    private static final String NO_API_KEY_PLACEHOLDER = "none";

    private static final Pattern CONTEXT_OVERFLOW = Pattern.compile(
            "context[ _-]?(length|window|size)|maximum context|too many tokens|token limit|prompt is too long"
                    + "|context.{0,30}too (long|large)|too (long|large).{0,30}context|exceeds? .{0,40}(context|tokens)",
            Pattern.CASE_INSENSITIVE);

    @Autowired
    private TokenizerService tokenizerService;

    @Autowired
    private Configuration configuration;

    @Autowired
    private UsageService usageService;

    @Autowired
    private AILimitService limitService;

    @Autowired
    private InferenceServices inferenceServices;

    private ExecutorService inferenceService;

    private final OpenAICompatible ai;

    public OpenAICompatibleInferenceService(AI ai) {
        this.ai = (OpenAICompatible) ai;
    }

    @Override
    public List<String> getModels() {
        try {
            OpenAIClient client = openClient();

            ModelListPage modelList = client.models().list();
            List<Model> models = modelList.data();

            if (models.isEmpty()) {
                log.info("Connection successful, but no models were returned.");
            } else {
                log.info("Connection successful!");
            }

            return models.stream().map(Model::id).toList();
        } catch (RuntimeException e) {
            throw translateException(e);
        }
    }

    @Override
    public List<String> testConnection() {
        List<String> models;
        try {
            models = getModels();
        } catch (InferenceException e) {
            // not every compatible server has the endpoint, the completion decides
            if (e.getType() != Type.NOT_FOUND) {
                throw e;
            }
            models = List.of();
        }

        try {
            ChatCompletionCreateParams.Builder params = ChatCompletionCreateParams.builder()
                    .model(ai.getModel())
                    .addUserMessage("Say OK.");
            Set<String> overridden = applyAdditionalParameters(params);
            if (!overridden.contains("max_completion_tokens")) {
                params.maxCompletionTokens(1);
            }
            openClient().chat().completions().create(params.build());
        } catch (RuntimeException e) {
            throw translateException(e);
        } catch (Exception e) {
            throw new InferenceException(Type.UNKNOWN, null, e.getMessage(), e);
        }
        return models;
    }

    @Override
    public long countTokens(String text) throws Exception {
        return tokenizerService.countTokens(ai, text);
    }

    @Override
    public long countTokensApprox(String text) throws Exception {
        return tokenizerService.countTokensApprox(text);
    }

    @Override
    public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, InferenceAsyncCallback callback) throws Exception {
        return stream(payload, protocol, null, callback);
    }

    @Override
    public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions,
                                    InferenceAsyncCallback callback) throws Exception {
        return stream(payload, protocol, null, functions, callback);
    }

    @Override
    public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, JsonNode schema,
                                    FunctionSet functions, InferenceAsyncCallback callback) throws Exception {
        // read here, the request goes on in other threads
        boolean metered = !UnmeteredScope.isActive();
        // the limits an administrator set for the user, before anything is sent: the prompt is known, so it must fit
        if (metered) {
            limitService.check(ai, approximatePromptTokens(payload), TokenLimits.responseTokens(ai, protocol));
        }
        Request request = new Request(openClient(), protocol, functions != null && !functions.isEmpty() ? functions : null,
                callback, new CancellationToken(),
                // the request ends on other threads, the user it counts for is the one who started it
                ThreadCopyRequestAttributes.create(), schema, metered);
        startRound(request, new ArrayList<>(payload), 0);
        return request.cancellationToken();
    }

    @Override
    public CancellationToken infer(List<LLMChatMessage> payload, Protocol protocol, JsonNode schema,
                                   FunctionSet functions, InferenceResultCallback callback) throws Exception {
        boolean metered = !UnmeteredScope.isActive();
        if (metered) {
            limitService.check(ai, approximatePromptTokens(payload), TokenLimits.responseTokens(ai, protocol));
        }
        Request request = new Request(openClient(), protocol, functions != null && !functions.isEmpty() ? functions : null,
                null, new CancellationToken(), ThreadCopyRequestAttributes.create(), schema, metered);
        Outcome outcome = new Outcome(callback::isDead, callback::onFunctionCall, callback::onCancel, callback::onError);
        inferRound(request, outcome, callback, schema, new ArrayList<>(payload), 0);
        return request.cancellationToken();
    }

    @Override
    public CancellationToken round(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions,
                                   InferenceRoundCallback callback) throws Exception {
        return round(payload, protocol, functions, false, callback);
    }

    @Override
    public CancellationToken round(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions, boolean noFunctionCalls,
                                   InferenceRoundCallback callback) throws Exception {
        boolean metered = !UnmeteredScope.isActive();
        if (metered) {
            limitService.check(ai, approximatePromptTokens(payload), TokenLimits.responseTokens(ai, protocol));
        }
        boolean functionsOffered = functions != null && !functions.isEmpty();
        OpenAIClient client = openClient();
        CancellationToken token = new CancellationToken();
        RequestAttributes attributes = ThreadCopyRequestAttributes.create();
        List<LLMChatMessage> conversation = new ArrayList<>(payload);
        // streamed and put together here: the read timeout is the wait for the next chunk, so a model that thinks for
        // long (its reasoning arrives chunk by chunk) is not cut as one that waits for the whole answer would be
        ChatCompletionCreateParams params = buildParams(protocol, conversation, functionsOffered ? functions : null,
                noFunctionCalls, true, null).build();

        AsyncRunnableWrapper wrapper = new AsyncRunnableWrapper(configuration);
        CompletableFuture.runAsync(() -> {
            try {
                wrapper.run(() -> {
                    if (callback.isDead() || token.isCancelled()) {
                        callback.onCancel();
                        return;
                    }
                    StringBuilder text = new StringBuilder();
                    StringBuilder reasoning = new StringBuilder();
                    // everything that arrived, for our count when the provider reports none
                    StringBuilder received = new StringBuilder();
                    SortedMap<Long, PendingToolCall> pending = new TreeMap<>();
                    TokenUsage reported = null;
                    boolean stopped = false;
                    RoundStatsCollector stats = new RoundStatsCollector();
                    try (StreamResponse<ChatCompletionChunk> stream = client.chat().completions().createStreaming(params)) {
                        Iterator<ChatCompletionChunk> chunks = stream.stream().iterator();
                        while (chunks.hasNext()) {
                            if (callback.isDead() || token.isCancelled()) {
                                stopped = true;
                                break;
                            }
                            ChatCompletionChunk chunk = chunks.next();
                            stats.chunk(chunk);
                            if (chunk.usage().isPresent()) {
                                reported = new TokenUsage(chunk.usage().get().promptTokens(), chunk.usage().get().completionTokens());
                            }
                            for (ChatCompletionChunk.Choice choice : chunk.choices()) {
                                ChatCompletionChunk.Choice.Delta delta = choice.delta();
                                String thought = reasoningOf(delta);
                                if (thought != null) {
                                    reasoning.append(thought);
                                    received.append(thought);
                                    showChunk(callback, ChunkType.REASONING, thought);
                                }
                                if (functionsOffered && delta.toolCalls().isPresent()) {
                                    collectToolCalls(pending, delta.toolCalls().get());
                                    delta.toolCalls().get().forEach(call -> call.function()
                                            .flatMap(function -> function.arguments()).ifPresent(received::append));
                                }
                                delta.content().ifPresent(content -> {
                                    text.append(content);
                                    received.append(content);
                                    showChunk(callback, ChunkType.RESPONSE, content);
                                });
                            }
                        }
                    } catch (RuntimeException e) {
                        if (!received.isEmpty()) {
                            // it answered before it failed, so that was billed; a failure before any text is not counted
                            recordUsage(attributes, conversation, null, received.toString(), metered);
                        }
                        throw e;
                    }
                    TokenUsage counted = recordUsage(attributes, conversation, stopped ? null : reported,
                            received.toString(), metered);
                    List<LLMToolCall> calls = functionsOffered ? toToolCalls(pending) : List.of();
                    if (stopped || callback.isDead() || token.isCancelled()) {
                        callback.onCancel();
                    } else {
                        callback.onRound(text.toString(), StringUtils.isBlank(reasoning) ? null : reasoning.toString(), calls,
                                new RoundUsage(counted.promptTokens(), counted.completionTokens(), stats.stats()));
                    }
                });
            } catch (Throwable e) {
                log.error("Failed to ask for a round: {}", e.getMessage(), e);
                try {
                    callback.onError(translate(e));
                } catch (Exception ex) {
                    log.error("Error during callback.onError", ex);
                }
            }
        });
        return token;
    }

    /**
     * One request of {@link #infer}. Building it fails here, in the caller's thread, for the first round; the rest runs
     * asynchronously.
     */
    private void inferRound(Request request, Outcome outcome, InferenceResultCallback callback, JsonNode schema,
                            List<LLMChatMessage> conversation, int round) {
        boolean functionsOffered = request.functions() != null;
        boolean finalRound = round >= MAX_FUNCTION_ROUNDS;
        ChatCompletionCreateParams params = buildParams(request.protocol(), conversation,
                functionsOffered ? request.functions() : null, finalRound, false, schema).build();

        AsyncRunnableWrapper wrapper = new AsyncRunnableWrapper(configuration);
        CompletableFuture.runAsync(() -> {
            try {
                wrapper.run(() -> {
                    if (outcome.dead().getAsBoolean() || request.cancellationToken().isCancelled()) {
                        callback.onCancel();
                        return;
                    }
                    ChatCompletion completion = request.client().chat().completions().create(params);
                    ChatCompletionMessage message = completion.choices().stream().findFirst()
                            .map(ChatCompletion.Choice::message).orElse(null);
                    String text = message == null ? "" : message.content().orElse("");
                    TokenUsage reported = completion.usage()
                            .map(usage -> new TokenUsage(usage.promptTokens(), usage.completionTokens())).orElse(null);
                    if (request.metered()) {
                        recordUsage(request.attributes(), conversation, reported, text);
                    }

                    List<LLMToolCall> calls = new ArrayList<>();
                    if (functionsOffered && !finalRound && message != null) {
                        message.toolCalls().orElse(List.of()).forEach(toolCall -> toolCall.function().ifPresent(
                                function -> calls.add(LLMToolCall.of(
                                        StringUtils.defaultIfEmpty(function.id(), "call_" + UUID.randomUUID().toString().replace("-", "")),
                                        function.function().name(),
                                        StringUtils.defaultIfEmpty(function.function().arguments(), "{}")))));
                    }
                    // the request can not be taken back once sent, but a canceled one gets no result
                    if (outcome.dead().getAsBoolean() || request.cancellationToken().isCancelled()) {
                        callback.onCancel();
                    } else if (!calls.isEmpty()) {
                        callFunctions(request, outcome, conversation, text, calls,
                                next -> inferRound(request, outcome, callback, schema, next, round + 1));
                    } else {
                        callback.onResult(text, schema == null ? SchemaCheck.notChecked() : StructuredOutputs.validate(schema, text));
                    }
                });
            } catch (Throwable e) {
                log.error("Failed to infer: {}", e.getMessage(), e);
                try {
                    callback.onError(translate(e));
                } catch (Exception ex) {
                    log.error("Error during callback.onError", ex);
                }
            }
        });
    }

    /**
     * What stays the same through the rounds of a request: with functions the model is asked again after each call.
     * {@code metered}: whether the limits apply and the usage is counted, see {@link UnmeteredScope}.
     */
    private record Request(OpenAIClient client, Protocol protocol, FunctionSet functions, InferenceAsyncCallback callback,
                           CancellationToken cancellationToken, RequestAttributes attributes, JsonNode schema,
                           boolean metered) {

        /** The part of the callback the function calls need, the same for a stream and for infer. */
        Outcome outcome() {
            return new Outcome(callback::isDead, callback::onFunctionCall, callback::onCancel, callback::onError);
        }
    }

    private interface FunctionObserver {
        void onFunctionCall(LLMToolCall call, String result, boolean failed) throws Exception;
    }

    private interface Failure<T> {
        void accept(T value) throws Exception;
    }

    private interface Signal {
        void run() throws Exception;
    }

    private record Outcome(BooleanSupplier dead, FunctionObserver observer, Signal cancel, Failure<Throwable> error) {}

    /**
     * Sends the conversation and streams the answer. Building the request fails here, in the caller's thread, for the
     * first round; the rest of the round runs asynchronously.
     */
    private void startRound(Request request, List<LLMChatMessage> conversation, int round) {
        // the last round may not call anything, the model has to answer with what it has
        boolean functionsOffered = request.functions() != null;
        boolean finalRound = round >= MAX_FUNCTION_ROUNDS;

        ChatCompletionCreateParams.Builder paramsBuilder = buildParams(request.protocol(), conversation,
                functionsOffered ? request.functions() : null, finalRound, true, request.schema());

        AsyncRunnableWrapper wrapper = new AsyncRunnableWrapper(configuration);
        CompletableFuture.runAsync(() -> {
            try {
                wrapper.run(() -> {
                    if (request.cancellationToken().isCancelled())
                        return;
                    StreamResponse<ChatCompletionChunk> streamResponse = request.client().chat().completions().createStreaming(paramsBuilder.build());
                    OpenAIInferenceAsyncController controller = new OpenAIInferenceAsyncController(streamResponse,
                            request.callback(), request.cancellationToken(),
                            (reported, generated) -> {
                                if (request.metered()) {
                                    recordUsage(request.attributes(), conversation, reported, generated);
                                }
                            },
                            functionsOffered && !finalRound
                                    ? (text, calls) -> callFunctions(request, request.outcome(), conversation, text, calls,
                                    next -> startRound(request, next, round + 1)) : null);
                    controller.continueInference();
                });
            } catch (Throwable e) {
                log.error("Failed to start streaming inference: {}", e.getMessage(), e);
                try {
                    request.callback().onError(translate(e));
                } catch (Exception ex) {
                    log.error("Error during callback.onError", ex);
                }
            }
        });
    }

    private ChatCompletionCreateParams.Builder buildParams(Protocol protocol, List<LLMChatMessage> conversation,
                                                           FunctionSet functions, boolean noFunctionCalls,
                                                           boolean streaming, JsonNode schema) {
        List<ChatCompletionMessageParam> messages = new ArrayList<>();
        String toolRole = inferenceServices.resolveToolRole(ai);
        for (LLMChatMessage msg : conversation) {
            if (msg.getContent() == null && !(msg.getRole() == LLMRole.USER && msg.hasAttachments()) && !msg.hasToolCalls()) {
                continue;
            }
            switch (msg.getRole()) {
                case SYSTEM -> messages.add(ChatCompletionMessageParam.ofSystem(
                        ChatCompletionSystemMessageParam.builder().content(msg.getContent()).build()));
                case USER -> messages.add(ChatCompletionMessageParam.ofUser(toUserMessage(msg)));
                case ASSISTANT -> messages.add(ChatCompletionMessageParam.ofAssistant(toAssistantMessage(msg)));
                case TOOL -> {
                    if (msg.getToolCallId() == null) {
                        throw new IllegalArgumentException("Tool message has no toolCallId");
                    }
                    ChatCompletionToolMessageParam.Builder tool = ChatCompletionToolMessageParam.builder()
                            .toolCallId(msg.getToolCallId()).content(msg.getContent());
                    if (!InferenceServices.DEFAULT_TOOL_ROLE.equals(toolRole)) {
                        // an extension knows the model wants another name for it
                        tool.role(JsonValue.from(toolRole));
                    }
                    messages.add(ChatCompletionMessageParam.ofTool(tool.build()));
                }
            }
        }

        ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                .model(ai.getModel())
                .messages(messages);

        Set<String> overridden = applyAdditionalParameters(paramsBuilder);

        // the last chunk then carries the token counts of the request (an additional parameter stream_options replaces it)
        if (streaming && !overridden.contains("stream_options")) {
            paramsBuilder.streamOptions(ChatCompletionStreamOptions.builder().includeUsage(true).build());
        }

        int responseTokens = TokenLimits.responseTokens(ai, protocol);
        if (responseTokens > 0 && !overridden.contains("max_completion_tokens")) {
            paramsBuilder.maxCompletionTokens(responseTokens);
        }

        if (protocol != null) {
            if (protocol.getTemperatureEnabled() && protocol.getTemperature() != null && !overridden.contains("temperature")) {
                paramsBuilder.temperature(protocol.getTemperature());
            }
            if (protocol.getTopPEnabled() && protocol.getTopP() != null && !overridden.contains("top_p")) {
                paramsBuilder.topP(protocol.getTopP());
            }
            if (protocol.getFrequencyPenaltyEnabled() && protocol.getFrequencyPenalty() != null && !overridden.contains("frequency_penalty")) {
                paramsBuilder.frequencyPenalty(protocol.getFrequencyPenalty());
            }
            if (protocol.getPresencePenaltyEnabled() && protocol.getPresencePenalty() != null && !overridden.contains("presence_penalty")) {
                paramsBuilder.presencePenalty(protocol.getPresencePenalty());
            }
        }

        if (Boolean.TRUE.equals(ai.getEnabledReasoning()) && ai.getReasoningEffort() != null && !overridden.contains("reasoning_effort")) {
            String effortValue = ai.getReasoningEffort().toString().toLowerCase();
            paramsBuilder.putAdditionalBodyProperty("reasoning_effort", JsonValue.from(effortValue));
            paramsBuilder.putAdditionalBodyProperty("allowed_openai_params", JsonValue.from(List.of("reasoning_effort")));
        }

        if (schema != null && !overridden.contains("response_format")) {
            com.openai.models.ResponseFormatJsonSchema.JsonSchema.Schema.Builder body =
                    com.openai.models.ResponseFormatJsonSchema.JsonSchema.Schema.builder();
            schema.fields().forEachRemaining(field -> body.putAdditionalProperty(field.getKey(), JsonValue.fromJsonNode(field.getValue())));
            // not strict: strict mode only takes a part of JSON schema, the answer is checked here anyway
            paramsBuilder.responseFormat(com.openai.models.ResponseFormatJsonSchema.builder()
                    .jsonSchema(com.openai.models.ResponseFormatJsonSchema.JsonSchema.builder()
                            .name("response").schema(body.build()).strict(false).build())
                    .build());
        }

        if (functions != null) {
            // the tools stay in the final round too: the conversation contains their calls
            for (FunctionDefinition function : functions.getDefinitions()) {
                paramsBuilder.addTool(toTool(function));
            }
            if (noFunctionCalls && !overridden.contains("tool_choice")) {
                paramsBuilder.toolChoice(ChatCompletionToolChoiceOption.Auto.NONE);
            }
        }
        return paramsBuilder;
    }

    /** What a view that shows the answer as it is written gets; its failure is not the failure of the round. */
    private static void showChunk(InferenceRoundCallback callback, ChunkType type, String text) {
        try {
            callback.onChunk(type, text);
        } catch (RuntimeException e) {
            log.debug("A view of the answer failed: {}", e.getMessage());
        }
    }

    private static ChatCompletionFunctionTool toTool(FunctionDefinition function) {
        FunctionParameters.Builder parameters = FunctionParameters.builder();
        function.getParametersSchema().fields().forEachRemaining(
                field -> parameters.putAdditionalProperty(field.getKey(), JsonValue.fromJsonNode(field.getValue())));
        com.openai.models.FunctionDefinition.Builder definition = com.openai.models.FunctionDefinition.builder()
                .name(function.getName())
                .parameters(parameters.build());
        if (StringUtils.isNotEmpty(function.getDescription())) {
            definition.description(function.getDescription());
        }
        return ChatCompletionFunctionTool.builder().function(definition.build()).build();
    }

    private static ChatCompletionAssistantMessageParam toAssistantMessage(LLMChatMessage msg) {
        ChatCompletionAssistantMessageParam.Builder builder = ChatCompletionAssistantMessageParam.builder();
        if (msg.getContent() != null) {
            builder.content(msg.getContent());
        }
        for (LLMToolCall call : msg.getToolCalls()) {
            builder.addToolCall(ChatCompletionMessageFunctionToolCall.builder()
                    .id(call.getId())
                    .function(ChatCompletionMessageFunctionToolCall.Function.builder()
                            .name(call.getName()).arguments(call.getArguments()).build())
                    .build());
        }
        return builder.build();
    }

    /**
     * The model ended its answer with calls: runs them as a chain, one after the other, each on a thread of its own,
     * and asks the model again with the results when the last has answered. A call finishes whenever and from
     * whichever thread its function says, nothing here waits for it. A call that fails is not an error of the request,
     * the model gets the message and can correct itself.
     */
    private void callFunctions(Request request, Outcome callback, List<LLMChatMessage> conversation, String text,
                               List<LLMToolCall> calls, java.util.function.Consumer<List<LLMChatMessage>> nextRound) {
        List<LLMChatMessage> next = new ArrayList<>(conversation);
        next.add(LLMChatMessage.ofToolCalls(StringUtils.isEmpty(text) ? null : text, calls));

        BooleanSupplier stopped = () -> callback.dead().getAsBoolean() || request.cancellationToken().isCancelled();
        Executor executor = task -> CompletableFuture.runAsync(() -> {
            try (InRequestScope _ = new InRequestScope(request.attributes())) {
                task.run();
            }
        });

        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (LLMToolCall call : calls) {
            chain = chain.thenCompose(_ -> {
                if (stopped.getAsBoolean()) {
                    throw new CancellationException();
                }
                CompletableFuture<String> answer = request.functions().call(call.getName(), call.getArguments(), executor, stopped);
                // a call that never answers must not outlive the request
                Runnable unregister = request.cancellationToken().onCancel(() -> answer.completeExceptionally(new CancellationException()));
                return answer.handle((result, error) -> {
                    unregister.run();
                    if (error instanceof CompletionException wrapped && wrapped.getCause() != null) {
                        error = wrapped.getCause();
                    }
                    if (error instanceof CancellationException) {
                        throw (CancellationException) error;
                    }
                    boolean failed = error != null;
                    String content = result;
                    if (failed) {
                        content = "Error: " + error.getMessage();
                        if (error instanceof FunctionCallException e && e.getKind() != FunctionCallException.Kind.FAILED) {
                            log.info("Call of function {} refused: {}", call.getName(), e.getMessage());
                        } else {
                            log.warn("Function {} called by the model failed", call.getName(), error);
                        }
                    }
                    next.add(LLMChatMessage.ofToolResult(call.getId(), content));
                    try {
                        callback.observer().onFunctionCall(call, content, failed);
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                    return null;
                });
            });
        }
        chain.whenComplete((_, error) -> {
            try {
                if (error instanceof CompletionException wrapped && wrapped.getCause() != null) {
                    error = wrapped.getCause();
                }
                if (error instanceof CancellationException || (error == null && stopped.getAsBoolean())) {
                    callback.cancel().run();
                } else if (error != null) {
                    throw error;
                } else {
                    nextRound.accept(next);
                }
            } catch (Throwable e) {
                log.error("Error calling the functions of the model", e);
                try {
                    callback.error().accept(translate(e));
                } catch (Exception ex) {
                    log.error("Error invoking callback.onError", ex);
                }
            }
        });
    }

    /**
     * A plain string content when the message has no attachments, otherwise the array of content parts: the text
     * first, then the attachments in their order.
     */
    private static ChatCompletionUserMessageParam toUserMessage(LLMChatMessage msg) {
        ChatCompletionUserMessageParam.Builder builder = ChatCompletionUserMessageParam.builder();
        if (!msg.hasAttachments()) {
            return builder.content(msg.getContent()).build();
        }

        List<ChatCompletionContentPart> parts = new ArrayList<>();
        if (StringUtils.isNotEmpty(msg.getContent())) {
            parts.add(ChatCompletionContentPart.ofText(
                    ChatCompletionContentPartText.builder().text(msg.getContent()).build()));
        }
        for (LLMAttachment attachment : msg.getAttachments()) {
            parts.add(toContentPart(attachment));
        }
        return builder.contentOfArrayOfContentParts(parts).build();
    }

    private static ChatCompletionContentPart toContentPart(LLMAttachment attachment) {
        LLMAttachment.Kind kind = attachment.getKind() != null
                ? attachment.getKind() : LLMAttachment.Kind.fromMimeType(attachment.getMimeType());
        switch (kind) {
            case IMAGE -> {
                String url = attachment.toDataUrl();
                if (url == null) {
                    throw new IllegalArgumentException("Image attachment has neither data nor url");
                }
                return ChatCompletionContentPart.ofImageUrl(ChatCompletionContentPartImage.builder()
                        .imageUrl(ChatCompletionContentPartImage.ImageUrl.builder().url(url).build())
                        .build());
            }
            case AUDIO -> {
                // the API takes the audio as base64 only and knows just wav and mp3
                if (attachment.getData() == null) {
                    throw new IllegalArgumentException("Audio attachment must carry its data, url is not supported");
                }
                String mime = StringUtils.defaultString(attachment.getMimeType()).toLowerCase();
                ChatCompletionContentPartInputAudio.InputAudio.Format format;
                if (mime.contains("wav")) {
                    format = ChatCompletionContentPartInputAudio.InputAudio.Format.WAV;
                } else if (mime.contains("mp3") || mime.contains("mpeg")) {
                    format = ChatCompletionContentPartInputAudio.InputAudio.Format.MP3;
                } else {
                    throw new IllegalArgumentException("Unsupported audio type " + attachment.getMimeType() + ", only wav and mp3 are supported");
                }
                return ChatCompletionContentPart.ofInputAudio(ChatCompletionContentPartInputAudio.builder()
                        .inputAudio(ChatCompletionContentPartInputAudio.InputAudio.builder()
                                .data(attachment.toBase64()).format(format).build())
                        .build());
            }
            case FILE -> {
                if (attachment.getData() == null) {
                    throw new IllegalArgumentException("File attachment must carry its data, url is not supported");
                }
                ChatCompletionContentPart.File.FileObject.Builder file = ChatCompletionContentPart.File.FileObject.builder()
                        .fileData(attachment.toDataUrl());
                if (attachment.getName() != null) {
                    file.filename(attachment.getName());
                }
                return ChatCompletionContentPart.ofFile(ChatCompletionContentPart.File.builder().file(file.build()).build());
            }
            default -> throw new IllegalArgumentException("Unknown attachment kind " + kind);
        }
    }

    /**
     * Additional parameters go into the request body as they are and win over the same settings of the AI and the
     * protocol.
     *
     * @return names of the parameters that were set
     */
    private Set<String> applyAdditionalParameters(ChatCompletionCreateParams.Builder paramsBuilder) {
        Set<String> overridden = new HashSet<>();
        if (ai.getAdditionalParameters() != null) {
            for (Map.Entry<String, JsonElement> parameter : ai.getAdditionalParameters().entrySet()) {
                try {
                    paramsBuilder.putAdditionalBodyProperty(parameter.getKey(),
                            JsonValue.fromJsonNode(objectMapper.readTree(parameter.getValue().toString())));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid additional parameter " + parameter.getKey(), e);
                }
                overridden.add(parameter.getKey());
            }
        }
        return overridden;
    }

    /**
     * The OpenAI client refuses to build a request without a key, but local servers (llama.cpp, Ollama...) don't
     * check it, so an empty key is replaced by a placeholder.
     */
    public static String apiKeyOrPlaceholder(OpenAICompatible ai) {
        String key = ai.getApiKey();
        return key == null || key.isBlank() ? NO_API_KEY_PLACEHOLDER : key;
    }

    private OpenAIClient openClient() {
        int timeoutSeconds = ai.getRequestTimeoutSeconds() != null && ai.getRequestTimeoutSeconds() > 0
                ? ai.getRequestTimeoutSeconds() : DEFAULT_TIMEOUT_SECONDS;
        int retries = ai.getMaxRetries() != null && ai.getMaxRetries() >= 0 ? ai.getMaxRetries() : DEFAULT_MAX_RETRIES;
        Duration timeout = Duration.ofSeconds(timeoutSeconds);

        // read is the wait for the first text and between the chunks; the whole request limit must not cut a long
        // story in the middle, so it is much longer
        Timeout timeouts = Timeout.builder()
                .connect(timeout.compareTo(Duration.ofSeconds(30)) < 0 ? timeout : Duration.ofSeconds(30))
                .read(timeout)
                .write(timeout)
                .request(timeout.compareTo(Duration.ofHours(1)) > 0 ? timeout : Duration.ofHours(1))
                .build();

        // the client retries 408, 409, 429, 5xx and connection errors with growing waits (and honors Retry-After)
        return OpenAIOkHttpClient.builder()
                .baseUrl(ai.getUri())
                .apiKey(apiKeyOrPlaceholder(ai))
                .timeout(timeouts)
                .maxRetries(retries)
                .build();
    }

    /**
     * Counts a request for the user whose thread runs it, whoever started it (generation, summary, an extension): what
     * the provider reported, otherwise our count of the payload and of the text that arrived (a stopped request has no
     * report).
     */
    private void recordUsage(RequestAttributes attributes, List<LLMChatMessage> payload, TokenUsage reported, String generated) {
        recordUsage(attributes, payload, reported, generated, true);
    }

    /**
     * @param metered whether the usage is recorded for the user, the counts are returned either way (zeros when they
     *                could not be made)
     */
    /** The calls arrive in pieces: the first has the id and the name, then the JSON arguments follow. */
    private static void collectToolCalls(SortedMap<Long, PendingToolCall> calls,
                                         List<ChatCompletionChunk.Choice.Delta.ToolCall> deltas) {
        for (ChatCompletionChunk.Choice.Delta.ToolCall delta : deltas) {
            // some servers send no index, a call that has an id is then a new one
            long index = delta._index().asKnown().orElseGet(() -> delta.id().isPresent() || calls.isEmpty()
                    ? calls.size() : calls.lastKey());
            PendingToolCall call = calls.computeIfAbsent(index, i -> new PendingToolCall());
            delta.id().filter(id -> !id.isEmpty()).ifPresent(id -> call.id = id);
            delta.function().ifPresent(function -> {
                // a name repeated in the later chunks is not a longer name
                function.name().filter(name -> !name.isEmpty() && call.name == null).ifPresent(name -> call.name = name);
                function.arguments().ifPresent(call.arguments::append);
            });
        }
    }

    private static List<LLMToolCall> toToolCalls(SortedMap<Long, PendingToolCall> pending) {
        List<LLMToolCall> calls = new ArrayList<>();
        for (PendingToolCall call : pending.values()) {
            String id = call.id != null ? call.id : "call_" + UUID.randomUUID().toString().replace("-", "");
            calls.add(LLMToolCall.of(id, call.name, call.arguments.length() == 0 ? "{}" : call.arguments.toString()));
        }
        return calls;
    }

    /**
     * What a streamed chunk carries as the reasoning of the model: the field {@code reasoning_content} or
     * {@code reasoning} of the delta, null when there is none.
     */
    @SuppressWarnings("unchecked")
    private static String reasoningOf(ChatCompletionChunk.Choice.Delta delta) {
        com.openai.core.JsonValue val = delta._additionalProperties().get("reasoning_content");
        if (val == null) {
            val = delta._additionalProperties().get("reasoning");
        }
        String reasoning = (val != null && !val.isNull()) ? (String) val.asString().orElse((String) null) : null;
        return reasoning == null || reasoning.isEmpty() ? null : reasoning;
    }

    private TokenUsage recordUsage(RequestAttributes attributes, List<LLMChatMessage> payload, TokenUsage reported,
                                   String generated, boolean metered) {
        try (InRequestScope _ = new InRequestScope(attributes)) {
            long prefill;
            long generatedTokens;
            if (reported != null) {
                prefill = reported.promptTokens();
                generatedTokens = reported.completionTokens();
            } else {
                prefill = approximatePromptTokens(payload);
                generatedTokens = tokenizerService.countTokensApprox(generated);
            }
            if (metered) {
                usageService.record(ai, prefill, generatedTokens);
            }
            return new TokenUsage(prefill, generatedTokens);
        } catch (Exception e) {
            log.warn("Failed to count the usage of AI {}", ai.getId(), e);
            return reported != null ? reported : new TokenUsage(0, 0);
        }
    }

    /**
     * Our count of the tokens of the payload, the provider's own differs a little.
     */
    private long approximatePromptTokens(List<LLMChatMessage> payload) throws Exception {
        long tokens = 0;
        for (LLMChatMessage message : payload) {
            tokens += tokenizerService.countTokensApprox(StringUtils.defaultString(message.getContent()));
            for (LLMToolCall call : message.getToolCalls()) {
                tokens += tokenizerService.countTokensApprox(call.getName() + call.getArguments());
            }
        }
        return tokens;
    }

    private static Throwable translate(Throwable e) {
        return e instanceof RuntimeException r ? translateException(r) : e;
    }

    /**
     * Maps what the client threw to an {@link InferenceException} the user can act on; other exceptions are returned
     * as they are.
     */
    static RuntimeException translateException(RuntimeException e) {
        if (e instanceof InferenceException) {
            return e;
        }
        if (e instanceof OpenAIServiceException service) {
            int status = service.statusCode();
            String message = providerMessage(service);
            String code = service.code().orElse("");
            String lower = (code + " " + StringUtils.defaultString(message)).toLowerCase();

            Type type;
            if (status == 401) {
                type = Type.AUTHENTICATION;
            } else if (status == 403) {
                type = Type.PERMISSION;
            } else if (status == 404) {
                type = lower.contains("model") ? Type.MODEL_NOT_FOUND : Type.NOT_FOUND;
            } else if (status == 408 || status == 504) {
                type = Type.TIMEOUT;
            } else if (status == 429) {
                type = Type.RATE_LIMIT;
            } else if (status >= 500) {
                type = Type.SERVER;
            } else if (code.equals("context_length_exceeded") || CONTEXT_OVERFLOW.matcher(lower).find()) {
                type = Type.CONTEXT_OVERFLOW;
            } else if (lower.contains("model_not_found") || (lower.contains("model") && lower.contains("not exist"))) {
                type = Type.MODEL_NOT_FOUND;
            } else {
                type = Type.BAD_REQUEST;
            }
            return new InferenceException(type, status, message, e);
        }
        if (e instanceof OpenAIIoException io) {
            boolean timeout = false;
            for (Throwable t = io; t != null; t = t.getCause()) {
                if (t instanceof SocketTimeoutException
                        || (t instanceof InterruptedIOException && StringUtils.containsIgnoreCase(t.getMessage(), "timeout"))) {
                    timeout = true;
                    break;
                }
                if (t.getCause() == t) {
                    break;
                }
            }
            Throwable root = io.getCause() != null ? io.getCause() : io;
            return new InferenceException(timeout ? Type.TIMEOUT : Type.CONNECTION, null,
                    root.getClass().getSimpleName() + (root.getMessage() != null ? ": " + root.getMessage() : ""), e);
        }
        return e;
    }

    /** The {@code error.message} of the provider's answer, otherwise its whole body. */
    private static String providerMessage(OpenAIServiceException e) {
        try {
            JsonValue body = e.body();
            Map<String, JsonValue> object = asObject(body);
            if (object != null) {
                JsonValue error = object.get("error");
                Map<String, JsonValue> errorObject = asObject(error);
                if (errorObject != null && asString(errorObject.get("message")) != null) {
                    return asString(errorObject.get("message"));
                }
                if (asString(error) != null) {
                    return asString(error);
                }
                if (asString(object.get("message")) != null) {
                    return asString(object.get("message"));
                }
            }
            if (asString(body) != null) {
                return asString(body);
            }
            return body.isNull() || body.isMissing() ? null : body.toString();
        } catch (Exception ex) {
            return null;
        }
    }

    // the client's JsonValue is a raw type for Java, its accessors need the casts
    @SuppressWarnings("unchecked")
    private static Map<String, JsonValue> asObject(JsonValue value) {
        return value == null ? null : (Map<String, JsonValue>) value.asObject().orElse(null);
    }

    private static String asString(JsonValue value) {
        return value == null ? null : (String) value.asString().orElse(null);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        AtomicLong tc = new AtomicLong();
        inferenceService = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("Inference (OAICompat) thread " + StringUtils.leftPad("" + tc.getAndAdd(1), 3, '0'));
            return thread;
        });
    }

    @Override
    public void destroy() throws Exception {
        inferenceService.shutdownNow();
    }

    private record PendingChunk(ChunkType type, String text) {}

    private static class PendingToolCall {
        String id;
        String name;
        final StringBuilder arguments = new StringBuilder();
    }

    /**
     * Token counts the provider reported for a request.
     */
    private record TokenUsage(long promptTokens, long completionTokens) {}

    /**
     * What a streamed answer says about how it went, besides its text: when its first and last chunks came, why it ended and what
     * the provider reports of its cache and reasoning (the usage of OpenAI, the {@code timings} of llama.cpp).
     */
    static final class RoundStatsCollector {

        private final long began = System.nanoTime();
        private long first = -1;
        private long last = -1;
        private String finishReason;
        private long cached = -1;
        private long reasoning = -1;
        private long serverPrompt = -1;
        private long serverPredicted = -1;

        void chunk(ChatCompletionChunk chunk) {
            long now = System.nanoTime();
            if (first < 0) {
                first = now;
            }
            last = now;
            try {
                chunk.usage().ifPresent(usage -> {
                    usage.promptTokensDetails().flatMap(details -> details.cachedTokens()).ifPresent(tokens -> cached = tokens);
                    usage.completionTokensDetails().flatMap(details -> details.reasoningTokens()).ifPresent(tokens -> reasoning = tokens);
                });
                for (ChatCompletionChunk.Choice choice : chunk.choices()) {
                    choice.finishReason().ifPresent(reason -> finishReason = reason.asString());
                }
                // llama.cpp: {"timings": {"cache_n": 1234, "prompt_ms": 56.7, "predicted_ms": 8901.2, ...}}
                JsonValue timings = chunk._additionalProperties().get("timings");
                Optional<?> object = timings == null ? Optional.empty() : timings.asObject();
                if (object.isPresent() && object.get() instanceof Map<?, ?> values) {
                    number(values, "cache_n").ifPresent(value -> cached = Math.max(cached, value.longValue()));
                    number(values, "prompt_ms").ifPresent(value -> serverPrompt = value.longValue());
                    number(values, "predicted_ms").ifPresent(value -> serverPredicted = value.longValue());
                }
            } catch (RuntimeException e) {
                // what a provider adds to a chunk is a bonus, not a reason to fail the round
            }
        }

        private static Optional<Number> number(Map<?, ?> values, String name) {
            Object value = values.get(name);
            if (value instanceof JsonValue json) {
                return json.asNumber();
            }
            return value instanceof Number number ? Optional.of(number) : Optional.empty();
        }

        InferenceService.RoundStats stats() {
            return new InferenceService.RoundStats(cached, reasoning,
                    first < 0 ? -1 : (first - began) / 1_000_000, first < 0 ? -1 : (Math.max(last, first) - first) / 1_000_000,
                    finishReason, serverPrompt, serverPredicted);
        }
    }

    /**
     * Runs the functions the model called at the end of a stream and starts the next request.
     */
    private interface FunctionCaller {

        /**
         * @param text  what the model wrote before the calls
         * @param calls the calls, in the order the model made them
         */
        void call(String text, List<LLMToolCall> calls);
    }

    private interface UsageRecorder {

        /**
         * @param reported  what the provider reported, null if it didn't
         * @param generated all the text that arrived, reasoning included
         */
        void record(TokenUsage reported, String generated);
    }

    @Configurable(preConstruction = true)
    private static class OpenAIInferenceAsyncController implements InferenceAsyncController {

        @Autowired
        private Configuration configuration;

        private final StreamResponse<ChatCompletionChunk> streamResponse;
        private final Iterator<ChatCompletionChunk> iterator;
        private final InferenceAsyncCallback callback;
        private final Queue<PendingChunk> pendingChunks = new LinkedList<>();
        private final CancellationToken cancellationToken;
        private boolean completed = false;
        private final UsageRecorder usageRecorder;
        private final FunctionCaller functionCaller;
        private final StringBuilder received = new StringBuilder();
        private final StringBuilder responseText = new StringBuilder();
        private final SortedMap<Long, PendingToolCall> toolCalls = new TreeMap<>();
        private TokenUsage reportedUsage;
        private boolean usageRecorded;

        public OpenAIInferenceAsyncController(StreamResponse<ChatCompletionChunk> streamResponse,
                                              InferenceAsyncCallback callback, CancellationToken cancellationToken,
                                              UsageRecorder usageRecorder, FunctionCaller functionCaller) {
            this.usageRecorder = usageRecorder;
            this.functionCaller = functionCaller;
            this.streamResponse = streamResponse;
            this.iterator = streamResponse.stream().iterator();
            this.callback = callback;
            this.cancellationToken = cancellationToken;
        }

        @Override
        public void continueInference() {
            AsyncRunnableWrapper wrapper = new AsyncRunnableWrapper(configuration);
            CompletableFuture.runAsync(() -> this.processNext(wrapper));
        }

        @Override
        public synchronized void terminateInference() {
            if (completed) {
                return;
            }
            completed = true;
            pendingChunks.clear();
            closeStream();
            recordUsage();
            log.debug("Inference execution terminated by controller request.");
        }

        private synchronized void processNext(AsyncRunnableWrapper wrapper) {
            if (completed) {
                return;
            }

            try {
                wrapper.run(() -> {
                    if (!pendingChunks.isEmpty()) {
                        PendingChunk chunk = pendingChunks.poll();
                        callback.onChunk(this, chunk.type(), chunk.text());
                        return;
                    }

                    while (iterator.hasNext()) {
                        if (completed) {
                            return;
                        }
                        if (callback.isDead() || cancellationToken.isCancelled()) {
                            closeStream();
                            recordUsage();
                            callback.onCancel();
                            return;
                        }

                        ChatCompletionChunk chunk = iterator.next();
                        chunk.usage().ifPresent(usage -> reportedUsage = new TokenUsage(usage.promptTokens(), usage.completionTokens()));
                        for (ChatCompletionChunk.Choice choice : chunk.choices()) {
                            ChatCompletionChunk.Choice.Delta delta = choice.delta();

                            String reasoning = reasoningOf(delta);
                            if (reasoning != null && !reasoning.isEmpty()) {
                                received.append(reasoning);
                                pendingChunks.add(new PendingChunk(ChunkType.REASONING, reasoning));
                            }

                            if (functionCaller != null) {
                                delta.toolCalls().ifPresent(this::collectToolCalls);
                            }

                            delta.content().ifPresent(content -> {
                                if (!content.isEmpty()) {
                                    received.append(content);
                                    responseText.append(content);
                                    pendingChunks.add(new PendingChunk(ChunkType.RESPONSE, content));
                                }
                            });
                        }

                        if (!pendingChunks.isEmpty()) {
                            PendingChunk nextChunk = pendingChunks.poll();
                            callback.onChunk(this, nextChunk.type(), nextChunk.text());
                            return;
                        }
                    }

                    completed = true;
                    closeStream();
                    recordUsage();
                    if (!toolCalls.isEmpty()) {
                        try {
                            functionCaller.call(responseText.toString(), pendingCalls());
                        } catch (Throwable e) {
                            log.error("Error calling the functions of the model", e);
                            try {
                                callback.onError(translate(e));
                            } catch (Exception ex) {
                                log.error("Error invoking callback.onError", ex);
                            }
                        }
                        return;
                    }
                    try {
                        callback.onCompletion();
                    } catch (Throwable e) {
                        // completed is already set, so the outer catch would swallow this and the caller would wait forever
                        log.error("Error invoking callback.onCompletion", e);
                        try {
                            callback.onError(translate(e));
                        } catch (Exception ex) {
                            log.error("Error invoking callback.onError", ex);
                        }
                    }
                });
            } catch (Throwable e) {
                if (!completed) {
                    completed = true;
                    closeStream();
                    if (!received.isEmpty()) {
                        // it answered before it failed, so that was billed; a failure before any text is not counted
                        recordUsage();
                    }
                    try {
                        callback.onError(translate(e));
                    } catch (Exception ex) {
                        log.error("Error invoking callback.onError", ex);
                    }
                }
            }
        }

        private void collectToolCalls(List<ChatCompletionChunk.Choice.Delta.ToolCall> deltas) {
            OpenAICompatibleInferenceService.collectToolCalls(toolCalls, deltas);
            for (ChatCompletionChunk.Choice.Delta.ToolCall delta : deltas) {
                received.append(delta.function().flatMap(function -> function.arguments()).orElse(""));
            }
        }

        private List<LLMToolCall> pendingCalls() {
            return toToolCalls(toolCalls);
        }

        /**
         * Counts the request, once, whichever way it ends.
         */
        private void recordUsage() {
            if (usageRecorded) {
                return;
            }
            usageRecorded = true;
            usageRecorder.record(reportedUsage, received.toString());
        }

        private void closeStream() {
            try {
                streamResponse.close();
            } catch (Exception e) {
                log.trace("Error closing OpenAI stream response", e);
            }
        }
    }
}