package com.github.enerccio.marginalia.extensions.grok;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.concurrent.AsyncRunnableWrapper;
import com.github.enerccio.marginalia.domain.collections.ReasoningEffort;
import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.InferenceException.Type;
import com.github.enerccio.marginalia.domain.service.function.FunctionCallException;
import com.github.enerccio.marginalia.domain.service.function.FunctionDefinition;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.domain.service.structured.SchemaCheck;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.context.request.RequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.enerccio.marginalia.extensions.grok.GrokLocalization.*;

/**
 * The inference service of an xAI Grok AI, over the xAI REST API (chat completions). It does what the service of an OpenAI
 * compatible AI does: streams with the reasoning apart from the answer, lets the model call the functions, answers in a JSON
 * schema, counts the usage of the user and honors the limits an administrator set.
 * <p>
 * Every request is streamed, also the ones whose answer is handed over whole ({@link #infer}, {@link #round}). xAI reports the
 * reasoning tokens apart from the completion tokens and bills both, so the generated tokens of the usage are the two together.
 * Tokens of the texts are counted by xAI ({@code /tokenize-text}), through the {@link TokenizerResolver} of the {@link GrokFactory}
 * so the application caches the way of counting; each text is asked once and after a failure the service counts approximately for
 * a minute.
 */
@Configurable
public class GrokInferenceService implements InferenceService, AutoCloseable {

    @Autowired
    private TokenizerService tokenizerService;

    @Autowired
    private Configuration configuration;

    @Autowired
    private UsageService usageService;

    @Autowired
    private AILimitService limitService;

    @Autowired(required = false)
    private RequestActivity activityTracker;

    private final ExtensionAI ai;
    private final GrokSettings settings;
    private final Logger log;
    private final Supplier<String> sessionIds;

    private static final int COUNT_CACHE_SIZE = 512;
    private static final long COUNT_PAUSE_MILLIS = 60_000;

    // the tokens of the texts xAI counted, by the hash of the text (the least recently used go first)
    private final Map<String, Long> countCache = Collections.synchronizedMap(new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
            return size() > COUNT_CACHE_SIZE;
        }
    });

    // after a failed count xAI is not asked until then, the estimate is used
    private volatile long countPausedUntil;

    private GrokClient client;
    private boolean closed;

    GrokInferenceService(ExtensionAI ai, GrokSettings settings, Logger log) {
        this(ai, settings, log, () -> null);
    }

    /**
     * @param sessionIds the session id of the request that starts on the calling thread, see
     *                   {@code InferenceServices.currentSessionId()}
     */
    GrokInferenceService(ExtensionAI ai, GrokSettings settings, Logger log, Supplier<String> sessionIds) {
        this.ai = ai;
        this.settings = settings;
        this.log = log;
        this.sessionIds = sessionIds;
    }

    /** What the application injects, for a test that has no Spring context. */
    void wire(TokenizerService tokenizerService, Configuration configuration, UsageService usageService, AILimitService limitService) {
        this.tokenizerService = tokenizerService;
        this.configuration = configuration;
        this.usageService = usageService;
        this.limitService = limitService;
    }

    private RequestActivity.Registration generating(Run run) {
        return run.metered && activityTracker != null ? activityTracker.generate(run.book) : RequestActivity.NONE;
    }

    // ---- models and connection

    @Override
    public List<String> getModels() {
        try {
            List<String> names = new ArrayList<>();
            for (JsonNode model : client().get("/models").path("data")) {
                names.add(model.path("id").asText());
            }
            return names;
        } catch (RuntimeException e) {
            throw runtime(e);
        }
    }

    @Override
    public List<String> testConnection() {
        List<String> models = getModels();
        try {
            ObjectNode body = GrokClient.MAPPER.createObjectNode().put("model", model()).put("max_completion_tokens", 16);
            body.putArray("messages").addObject().put("role", "user").put("content", "Say OK.");
            client().post("/chat/completions", body);
        } catch (RuntimeException e) {
            throw runtime(e);
        }
        return models;
    }

    @Override
    public long countTokens(String text) throws Exception {
        return tokenizerService.countTokens(ai, text);
    }

    /**
     * The count of xAI's own tokenizer, for {@link GrokFactory}'s resolver: the application calls it through
     * {@link TokenizerService#countTokens}, which falls back to the estimate when this throws.
     */
    long countTokensOnline(String text) {
        if (System.currentTimeMillis() < countPausedUntil) {
            throw new IllegalStateException("Counting with xAI is paused after a failure");
        }
        if (StringUtils.isEmpty(text)) {
            return 0;
        }
        String hash = sha256(text);
        Long known = countCache.get(hash);
        if (known != null) {
            return known;
        }
        try {
            ObjectNode body = GrokClient.MAPPER.createObjectNode().put("model", model()).put("text", text);
            long tokens = client().post("/tokenize-text", body).path("token_ids").size();
            countCache.put(hash, tokens);
            return tokens;
        } catch (RuntimeException e) {
            countPausedUntil = System.currentTimeMillis() + COUNT_PAUSE_MILLIS;
            logFailure("Counting the tokens with xAI failed, counting approximately for a minute", e);
            throw runtime(e);
        }
    }

    private static String sha256(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    @Override
    public long countTokensApprox(String text) throws Exception {
        return tokenizerService.countTokensApprox(text);
    }

    // ---- one round

    /**
     * One request for an agent: the model is asked once and what it answered is handed back whole, the functions it asked for are
     * <b>not</b> called (the runner of the agent calls them and asks again).
     */
    @Override
    public CancellationToken round(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions,
                                   InferenceRoundCallback callback) throws Exception {
        return round(payload, protocol, functions, false, callback);
    }

    @Override
    public CancellationToken round(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions, boolean noFunctionCalls,
                                   InferenceRoundCallback callback) throws Exception {
        if (!UnmeteredScope.isActive()) {
            limitService.check(ai, approximatePromptTokens(payload), responseTokens(protocol));
        }
        List<ObjectNode> messages = objects(GrokMessages.messages(payload));
        boolean functionsOffered = functions != null && !functions.isEmpty();
        Run run = new Run(protocol, functionsOffered ? functions : null, null,
                new Outcome(callback::isDead, (call, result, failed) -> {}, callback::onCancel, callback::onError));
        client();
        ObjectNode request = request(run, messages, functionsOffered && noFunctionCalls);

        AsyncRunnableWrapper wrapper = new AsyncRunnableWrapper(configuration);
        CompletableFuture.runAsync(() -> {
            try {
                wrapper.run(() -> {
                    if (run.outcome.dead().getAsBoolean() || run.token.isCancelled()) {
                        callback.onCancel();
                        return;
                    }
                    long began = System.nanoTime();
                    Answer answer;
                    try (RequestActivity.Registration generating = generating(run)) {
                        answer = collect(run, request, piece -> {
                            try {
                                callback.onChunk(piece.reasoning() ? ChunkType.REASONING : ChunkType.RESPONSE, piece.text());
                            } catch (RuntimeException e) {
                                log.trace("A chunk of a round was not shown", e);
                            }
                        });
                    }
                    long millis = (System.nanoTime() - began) / 1_000_000;
                    TokenUsage counted = run.metered
                            ? recordUsage(run.attributes, run.book, messages, answer.reported(), answer.received.toString()) : null;
                    if (run.outcome.dead().getAsBoolean() || run.token.isCancelled()) {
                        callback.onCancel();
                    } else if (answer.calls().isEmpty() && answer.isEmptyAndStopped()) {
                        throw answer.stopped();
                    } else {
                        String reasoning = answer.reasoning.toString();
                        callback.onRound(answer.text.toString(), reasoning.isEmpty() ? null : reasoning, answer.calls(),
                                roundUsage(counted != null ? counted : answer.reported(), answer, millis));
                    }
                });
            } catch (Throwable e) {
                logFailure("Failed to ask for a round", e);
                run.failOrCancel(e);
            } finally {
                run.forget();
            }
        });
        return run.token;
    }

    /** What a round used and how it went; the time of the whole request is given as the time of writing. */
    static RoundUsage roundUsage(TokenUsage reported, Answer answer, long millis) {
        long prompt = reported == null ? 0 : reported.promptTokens();
        long generated = reported == null ? 0 : reported.generatedTokens();
        return new RoundUsage(prompt, generated, new RoundStats(reported == null ? -1 : reported.cachedTokens(),
                reported == null ? -1 : reported.thoughtTokens(), -1, millis, answer.finishReason, -1, -1));
    }

    // ---- streaming

    @Override
    public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, InferenceAsyncCallback callback) throws Exception {
        return stream(payload, protocol, null, null, callback);
    }

    @Override
    public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions,
                                    InferenceAsyncCallback callback) throws Exception {
        return stream(payload, protocol, null, functions, callback);
    }

    @Override
    public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, JsonNode schema,
                                    FunctionSet functions, InferenceAsyncCallback callback) throws Exception {
        // the limits an administrator set for the user, before anything is sent: the prompt is known, so it must fit
        if (!UnmeteredScope.isActive()) {
            limitService.check(ai, approximatePromptTokens(payload), responseTokens(protocol));
        }
        List<ObjectNode> messages = objects(GrokMessages.messages(payload));
        Run run = new Run(protocol, functions != null && !functions.isEmpty() ? functions : null, schema,
                new Outcome(callback::isDead, callback::onFunctionCall, callback::onCancel, callback::onError));
        startStreamRound(run, callback, messages, 0);
        return run.token;
    }

    /**
     * Sends the conversation and streams the answer. Building the request fails here, in the caller's thread, for the
     * first round; the rest of the round runs asynchronously.
     */
    private void startStreamRound(Run run, InferenceAsyncCallback callback, List<ObjectNode> messages, int round) {
        boolean functionsOffered = run.functions != null;
        boolean finalRound = round >= MAX_FUNCTION_ROUNDS;
        // a missing key or model fails here, in the caller's thread, not through the callback
        client();
        ObjectNode request = request(run, messages, functionsOffered && finalRound);

        AsyncRunnableWrapper wrapper = new AsyncRunnableWrapper(configuration);
        CompletableFuture.runAsync(() -> {
            try {
                wrapper.run(() -> {
                    if (run.token.isCancelled()) {
                        callback.onCancel();
                        return;
                    }
                    // from the moment the request is sent to the end of the answer
                    RequestActivity.Registration generating = generating(run);
                    GrokClient.Stream stream;
                    try {
                        stream = open(run, request);
                    } catch (Throwable e) {
                        generating.close();
                        throw e;
                    }
                    GrokStreamController controller = new GrokStreamController(stream, callback, run,
                            (reported, generated) -> {
                                generating.close();
                                if (run.metered) {
                                    recordUsage(run.attributes, run.book, messages, reported, generated);
                                }
                            },
                            functionsOffered && !finalRound
                                    ? (answer) -> callFunctions(run, run.outcome, messages, answer,
                                    next -> startStreamRound(run, callback, next, round + 1)) : null);
                    controller.continueInference();
                });
            } catch (Throwable e) {
                run.forget();
                logFailure("Failed to start streaming inference", e);
                run.failOrCancel(e);
            }
        });
    }

    // ---- the whole answer at once

    @Override
    public CancellationToken infer(List<LLMChatMessage> payload, Protocol protocol, JsonNode schema,
                                   FunctionSet functions, InferenceResultCallback callback) throws Exception {
        if (!UnmeteredScope.isActive()) {
            limitService.check(ai, approximatePromptTokens(payload), responseTokens(protocol));
        }
        List<ObjectNode> messages = objects(GrokMessages.messages(payload));
        Run run = new Run(protocol, functions != null && !functions.isEmpty() ? functions : null, schema,
                new Outcome(callback::isDead, callback::onFunctionCall, callback::onCancel, callback::onError));
        inferRound(run, callback, messages, 0);
        return run.token;
    }

    private void inferRound(Run run, InferenceResultCallback callback, List<ObjectNode> messages, int round) {
        boolean functionsOffered = run.functions != null;
        boolean finalRound = round >= MAX_FUNCTION_ROUNDS;
        // a missing key or model fails here, in the caller's thread, not through the callback
        client();
        ObjectNode request = request(run, messages, functionsOffered && finalRound);

        AsyncRunnableWrapper wrapper = new AsyncRunnableWrapper(configuration);
        CompletableFuture.runAsync(() -> {
            try {
                wrapper.run(() -> {
                    if (run.outcome.dead().getAsBoolean() || run.token.isCancelled()) {
                        callback.onCancel();
                        return;
                    }
                    Answer answer;
                    try (RequestActivity.Registration generating = generating(run)) {
                        answer = collect(run, request, null);
                    }
                    if (run.metered) {
                        recordUsage(run.attributes, run.book, messages, answer.reported(), answer.received.toString());
                    }

                    // the request can not be taken back once sent, but a canceled one gets no result
                    if (run.outcome.dead().getAsBoolean() || run.token.isCancelled()) {
                        callback.onCancel();
                    } else if (functionsOffered && !finalRound && !answer.calls().isEmpty()) {
                        callFunctions(run, run.outcome, messages, answer, next -> inferRound(run, callback, next, round + 1));
                    } else if (answer.isEmptyAndStopped()) {
                        throw answer.stopped();
                    } else {
                        String text = answer.text.toString();
                        callback.onResult(text, run.schema == null ? SchemaCheck.notChecked() : StructuredOutputs.validate(run.schema, text));
                    }
                });
            } catch (Throwable e) {
                logFailure("Failed to infer", e);
                run.failOrCancel(e);
            } finally {
                run.forget();
            }
        });
    }

    // ---- the request

    /** What stays the same through the rounds of a request: with functions the model is asked again after each call. */
    private final class Run {
        final Protocol protocol;
        final FunctionSet functions;
        final JsonNode schema;
        final Outcome outcome;
        final CancellationToken token = new CancellationToken();
        // the request ends on other threads, the user it counts for is the one who started it
        final RequestAttributes attributes = ThreadCopyRequestAttributes.create();
        // read in the thread that starts the request: it is not metered inside an UnmeteredScope (the help assistant)
        final boolean metered = !UnmeteredScope.isActive();
        // the book the request is made for, read in the same thread (see UsageContext)
        final Long book = UsageContext.current();
        // read in the same thread: the header that tells a server with a prompt cache what kind of work this is
        final String sessionId = sessionIds.get();

        // the call that is running: cancelling it ends the HTTP exchange, also while a read waits for the next chunk
        private volatile okhttp3.Call current;

        Run(Protocol protocol, FunctionSet functions, JsonNode schema, Outcome outcome) {
            this.protocol = protocol;
            this.functions = functions;
            this.schema = schema;
            this.outcome = outcome;
            token.onCancel(this::closeCurrent);
        }

        void track(okhttp3.Call call) {
            current = call;
            if (token.isCancelled()) {
                closeCurrent();
            }
        }

        private void closeCurrent() {
            okhttp3.Call call = current;
            if (call != null) {
                call.cancel();
            }
        }

        /** The HTTP call of the round is over. */
        void forget() {
            current = null;
        }

        /** The request failed: because the user cancelled it (the call was cut short), or for real. */
        void failOrCancel(Throwable e) {
            if (token.isCancelled()) {
                try {
                    outcome.cancel().run();
                } catch (Exception ex) {
                    log.error("Error during callback.onCancel", ex);
                }
            } else {
                fail(outcome, e);
            }
        }
    }

    /** Opens the stream of the request and has the run keep its call, so that a cancellation can cut it. */
    private GrokClient.Stream open(Run run, ObjectNode request) {
        try {
            return client().stream("/chat/completions", request, run.sessionId, run::track);
        } catch (CancellationException e) {
            if (run.token.isCancelled()) {
                throw e;
            }
            throw new InferenceException(Type.CONNECTION, null, T.localized(THE_CONNECTION_TO_XAI), e);
        }
    }

    /**
     * Streams the request to its end and returns the whole answer.
     *
     * @param pieces gets the text and the reasoning as they arrive, null for none
     */
    private Answer collect(Run run, ObjectNode request, Consumer<ChunkPiece> pieces) throws Exception {
        Answer answer = new Answer();
        try (GrokClient.Stream stream = open(run, request)) {
            JsonNode event;
            while ((event = stream.next()) != null) {
                if (run.token.isCancelled()) {
                    break;
                }
                for (ChunkPiece piece : answer.absorb(event)) {
                    if (pieces != null) {
                        pieces.accept(piece);
                    }
                }
            }
            answer.streamEnded = stream.finished();
        } catch (java.io.IOException e) {
            if (!run.token.isCancelled()) {
                throw e;
            }
        }
        if (!answer.complete() && !run.token.isCancelled()) {
            throw new InferenceException(Type.CONNECTION, null, T.localized(THE_CONNECTION_TO_XAI), null);
        }
        return answer;
    }

    private ObjectNode request(Run run, List<ObjectNode> messages, boolean noFunctionCalls) {
        ObjectNode request = GrokClient.MAPPER.createObjectNode().put("model", model()).put("stream", true);
        request.putObject("stream_options").put("include_usage", true);
        ArrayNode array = request.putArray("messages");
        messages.forEach(array::add);

        int responseTokens = responseTokens(run.protocol);
        if (responseTokens > 0) {
            request.put("max_completion_tokens", responseTokens);
        }
        Protocol protocol = run.protocol;
        if (protocol != null) {
            if (Boolean.TRUE.equals(protocol.getTemperatureEnabled()) && protocol.getTemperature() != null) {
                request.put("temperature", protocol.getTemperature());
            }
            if (Boolean.TRUE.equals(protocol.getTopPEnabled()) && protocol.getTopP() != null) {
                request.put("top_p", protocol.getTopP());
            }
            if (Boolean.TRUE.equals(protocol.getFrequencyPenaltyEnabled()) && protocol.getFrequencyPenalty() != null) {
                request.put("frequency_penalty", protocol.getFrequencyPenalty());
            }
            if (Boolean.TRUE.equals(protocol.getPresencePenaltyEnabled()) && protocol.getPresencePenalty() != null) {
                request.put("presence_penalty", protocol.getPresencePenalty());
            }
        }
        String effort = effort();
        if (effort != null) {
            request.put("reasoning_effort", effort);
        }
        if (run.schema != null) {
            request.set("response_format", GrokMessages.responseFormat(run.schema));
        }
        if (run.functions != null) {
            // the tools stay in the final round too: the conversation contains their calls
            ArrayNode tools = request.putArray("tools");
            for (FunctionDefinition function : run.functions.getDefinitions()) {
                tools.add(GrokMessages.tool(function));
            }
            if (noFunctionCalls) {
                request.put("tool_choice", "none");
            }
        }
        return request;
    }

    private int responseTokens(Protocol protocol) {
        return TokenLimits.responseTokens(ai, protocol);
    }

    /**
     * The reasoning effort to send, null for none: when the AI has reasoning on and the model takes the parameter. Only
     * {@code low} and {@code high} exist for {@code grok-3-mini}, so the medium effort is sent as {@code high} there.
     */
    String effort() {
        ReasoningEffort effort = ai.getReasoningEffort();
        if (!Boolean.TRUE.equals(ai.getEnabledReasoning()) || effort == null || effort == ReasoningEffort.NONE || !settings.sendsEffort()) {
            return null;
        }
        if (settings.effortMode() == GrokSettings.EffortMode.AUTO) {
            return effort == ReasoningEffort.LOW ? "low" : "high";
        }
        return effort.name().toLowerCase(Locale.ROOT);
    }

    private String model() {
        if (settings.model().isBlank()) {
            throw new InferenceException(Type.MODEL_NOT_FOUND, null, T.localized(NO_MODEL_IS_SET), null);
        }
        return settings.model();
    }

    /**
     * The HTTP client of the AI, created on first use. It keeps connections and threads, so it is closed with the service
     * ({@link #close()}) when the extension is unloaded.
     */
    private synchronized GrokClient client() {
        if (closed) {
            throw new InferenceException(Type.EXTENSION_UNAVAILABLE, null, null,
                    new IllegalStateException("The extension " + GrokFactory.EXTENSION_ID + " was unloaded"));
        }
        if (settings.apiKey().isBlank()) {
            throw new InferenceException(Type.AUTHENTICATION, null, T.localized(NO_API_KEY_IS_SET), null);
        }
        if (client == null) {
            client = new GrokClient(settings);
        }
        return client;
    }

    /** Ends the service: the client is closed, the requests that are running fail. The extension was unloaded. */
    @Override
    public synchronized void close() {
        closed = true;
        if (client != null) {
            client.close();
            client = null;
        }
    }

    private static List<ObjectNode> objects(ArrayNode array) {
        List<ObjectNode> list = new ArrayList<>();
        array.forEach(node -> list.add((ObjectNode) node));
        return list;
    }

    // ---- functions

    /**
     * The model ended its answer with calls: runs them as a chain, one after the other, each on a thread of its own, and asks the
     * model again with the results when the last has answered. A call that fails is not an error of the request, the model gets
     * the message and can correct itself.
     */
    private void callFunctions(Run run, Outcome outcome, List<ObjectNode> messages, Answer answer, Consumer<List<ObjectNode>> nextRound) {
        List<ObjectNode> next = new ArrayList<>(messages);
        ObjectNode said = GrokClient.MAPPER.createObjectNode().put("role", "assistant").put("content", answer.text.toString());
        ArrayNode calls = said.putArray("tool_calls");
        List<LLMToolCall> toolCalls = answer.calls();
        toolCalls.forEach(call -> calls.add(GrokMessages.toolCall(call)));
        next.add(said);

        BooleanSupplier stopped = () -> outcome.dead().getAsBoolean() || run.token.isCancelled();
        Executor executor = task -> CompletableFuture.runAsync(() -> {
            try (InRequestScope _ = new InRequestScope(run.attributes)) {
                task.run();
            }
        });

        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (LLMToolCall call : toolCalls) {
            chain = chain.thenCompose(_ -> {
                if (stopped.getAsBoolean()) {
                    throw new CancellationException();
                }
                CompletableFuture<String> reply = run.functions.call(call.getName(), call.getArguments(), executor, stopped);
                // a call that never answers must not outlive the request
                Runnable unregister = run.token.onCancel(() -> reply.completeExceptionally(new CancellationException()));
                return reply.handle((result, error) -> {
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
                    synchronized (next) {
                        next.add(GrokMessages.toolResult(call.getId(), content));
                    }
                    try {
                        outcome.observer().onFunctionCall(call, content, failed);
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
                    outcome.cancel().run();
                } else if (error != null) {
                    throw error;
                } else {
                    nextRound.accept(next);
                }
            } catch (Throwable e) {
                log.error("Error calling the functions of the model", e);
                fail(outcome, e);
            }
        });
    }

    // ---- usage

    /**
     * Counts a request for the user whose thread runs it, whoever started it: what xAI reported, otherwise our count of the
     * messages and of the text that arrived (a stopped request has no report).
     */
    private TokenUsage recordUsage(RequestAttributes attributes, Long book, List<ObjectNode> messages, TokenUsage reported, String generated) {
        // the book of the request, not of the thread that runs it now
        try (InRequestScope _ = new InRequestScope(attributes); UsageContext _ = UsageContext.open(book)) {
            long prefill;
            long generatedTokens;
            if (reported != null) {
                prefill = reported.promptTokens();
                generatedTokens = reported.generatedTokens();
            } else {
                prefill = approximateTokens(messages);
                generatedTokens = tokenizerService.countTokensApprox(generated);
            }
            usageService.record(ai, prefill, generatedTokens);
            return new TokenUsage(prefill, generatedTokens, reported == null ? -1 : reported.cachedTokens(),
                    reported == null ? -1 : reported.thoughtTokens());
        } catch (Exception e) {
            log.warn("Failed to count the usage of AI {}", ai.getId(), e);
            return null;
        }
    }

    /** Our count of the tokens of the payload, xAI's own differs a little. */
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

    private long approximateTokens(List<ObjectNode> messages) throws Exception {
        long tokens = 0;
        for (ObjectNode message : messages) {
            JsonNode content = message.path("content");
            if (content.isTextual()) {
                tokens += tokenizerService.countTokensApprox(content.asText());
            } else if (content.isArray()) {
                for (JsonNode part : content) {
                    if (part.path("type").asText().equals("text")) {
                        tokens += tokenizerService.countTokensApprox(part.path("text").asText());
                    }
                }
            }
            for (JsonNode call : message.path("tool_calls")) {
                tokens += tokenizerService.countTokensApprox(call.path("function").path("name").asText()
                        + call.path("function").path("arguments").asText());
            }
        }
        return tokens;
    }

    /**
     * Token counts xAI reported for a request. The reasoning tokens are billed as generated ones, and xAI reports them apart from
     * the completion tokens: they are added when the total says they are not in them.
     */
    record TokenUsage(long promptTokens, long generatedTokens, long cachedTokens, long thoughtTokens) {

        static TokenUsage of(JsonNode usage) {
            long prompt = usage.path("prompt_tokens").asLong(0);
            long completion = usage.path("completion_tokens").asLong(0);
            long reasoning = usage.path("completion_tokens_details").path("reasoning_tokens").asLong(-1);
            long cached = usage.path("prompt_tokens_details").path("cached_tokens").asLong(-1);
            long total = usage.path("total_tokens").asLong(-1);
            boolean separate = reasoning > 0 && (total < 0 || total >= prompt + completion + reasoning);
            return new TokenUsage(prompt, separate ? completion + reasoning : completion, cached, reasoning);
        }
    }

    // ---- answers

    private static RuntimeException runtime(RuntimeException e) {
        Throwable translated = GrokErrors.translate(e);
        return translated instanceof RuntimeException r ? r : e;
    }

    /** What the provider answered is no bug, so it is a line; anything else gets its stack trace. */
    private void logFailure(String what, Throwable e) {
        if (GrokErrors.translate(e) instanceof InferenceException) {
            log.warn("{}: {}", what, e.getMessage());
            log.debug("Stack trace of the failure", e);
        } else {
            log.error("{}: {}", what, e.getMessage(), e);
        }
    }

    private void fail(Outcome outcome, Throwable e) {
        try {
            outcome.error().accept(GrokErrors.translate(e));
        } catch (Exception ex) {
            log.error("Error during callback.onError", ex);
        }
    }

    /** What the events of one response hold: the text, the reasoning, the calls and how it ended. */
    static class Answer {
        /** The text of the answer, without the reasoning. */
        final StringBuilder text = new StringBuilder();
        /** The reasoning the model showed. */
        final StringBuilder reasoning = new StringBuilder();
        /** Everything that arrived, reasoning and the arguments of calls included. */
        final StringBuilder received = new StringBuilder();
        // the calls by their index in the answer: xAI sends a call whole, a compatible server in pieces
        private final SortedMap<Integer, PartialCall> calls = new TreeMap<>();
        String finishReason;
        private TokenUsage usage;
        boolean streamEnded;

        /** Reads the event; returns the pieces of text it brought. */
        List<ChunkPiece> absorb(JsonNode event) {
            if (event.hasNonNull("error") && !event.has("choices")) {
                throw GrokErrors.fromResponse(500, event.toString());
            }
            if (event.hasNonNull("usage")) {
                usage = TokenUsage.of(event.get("usage"));
            }
            List<ChunkPiece> pieces = new ArrayList<>(2);
            for (JsonNode choice : event.path("choices")) {
                if (choice.path("index").asInt(0) != 0) {
                    continue;
                }
                JsonNode delta = choice.path("delta");
                String thought = GrokClient.text(delta.hasNonNull("reasoning_content") ? delta.get("reasoning_content") : delta.get("reasoning"));
                if (!thought.isEmpty()) {
                    reasoning.append(thought);
                    received.append(thought);
                    pieces.add(new ChunkPiece(true, thought));
                }
                String content = GrokClient.text(delta.get("content"));
                if (!content.isEmpty()) {
                    text.append(content);
                    received.append(content);
                    pieces.add(new ChunkPiece(false, content));
                }
                for (JsonNode call : delta.path("tool_calls")) {
                    PartialCall partial = calls.computeIfAbsent(call.path("index").asInt(calls.size()), _ -> new PartialCall());
                    if (call.hasNonNull("id")) {
                        partial.id = call.get("id").asText();
                    }
                    JsonNode function = call.path("function");
                    if (function.hasNonNull("name")) {
                        partial.name.append(function.get("name").asText());
                    }
                    if (function.hasNonNull("arguments")) {
                        String arguments = function.get("arguments").asText();
                        partial.arguments.append(arguments);
                        received.append(arguments);
                    }
                }
                if (choice.hasNonNull("finish_reason")) {
                    finishReason = choice.get("finish_reason").asText();
                }
            }
            return pieces;
        }

        /** The answer reached its end: the model said why it stopped, or the stream sent its end marker. */
        boolean complete() {
            return finishReason != null || streamEnded;
        }

        /** What xAI reported for the request; null when it did not (a stopped request, a server that does not report). */
        TokenUsage reported() {
            return usage;
        }

        /** The calls the model made, in order; empty for an answer that did not end. */
        List<LLMToolCall> calls() {
            List<LLMToolCall> result = new ArrayList<>();
            for (PartialCall call : calls.values()) {
                if (call.name.length() == 0) {
                    continue;
                }
                result.add(LLMToolCall.of(GrokMessages.callId(call.id), call.name.toString(),
                        call.arguments.length() == 0 ? "{}" : call.arguments.toString()));
            }
            return result;
        }

        /** The model answered nothing, and not because it had nothing to say: it was stopped. */
        boolean isEmptyAndStopped() {
            if (!received.isEmpty()) {
                return false;
            }
            return finishReason != null && !"stop".equals(finishReason) && !"tool_calls".equals(finishReason);
        }

        InferenceException stopped() {
            return GrokErrors.noAnswer(finishReason);
        }
    }

    private static final class PartialCall {
        String id;
        final StringBuilder name = new StringBuilder();
        final StringBuilder arguments = new StringBuilder();
    }

    record ChunkPiece(boolean reasoning, String text) {}

    // ---- streamed answers

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

    /** Runs the functions the model called at the end of a stream and starts the next request. */
    private interface FunctionCaller {

        void call(Answer answer);
    }

    private interface UsageRecorder {

        /**
         * @param reported  what xAI reported, null if it didn't
         * @param generated all the text that arrived, reasoning included
         */
        void record(TokenUsage reported, String generated);
    }

    /**
     * Hands the chunks of the stream to the callback one at a time: the callback asks for the next with
     * {@link #continueInference()}, so a slow consumer slows the stream down instead of piling the text up.
     */
    private final class GrokStreamController implements InferenceAsyncController {

        private final GrokClient.Stream stream;
        private final InferenceAsyncCallback callback;
        private final Run run;
        private final Queue<ChunkPiece> pending = new LinkedList<>();
        private final UsageRecorder usageRecorder;
        private final FunctionCaller functionCaller;
        private final Answer answer = new Answer();
        private boolean completed = false;
        private boolean usageRecorded;

        GrokStreamController(GrokClient.Stream stream, InferenceAsyncCallback callback, Run run,
                             UsageRecorder usageRecorder, FunctionCaller functionCaller) {
            this.stream = stream;
            this.callback = callback;
            this.run = run;
            this.usageRecorder = usageRecorder;
            this.functionCaller = functionCaller;
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
            pending.clear();
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
                    if (!pending.isEmpty()) {
                        deliver(pending.poll());
                        return;
                    }

                    JsonNode event;
                    while ((event = stream.next()) != null) {
                        if (completed) {
                            return;
                        }
                        if (callback.isDead() || run.token.isCancelled()) {
                            cancelled();
                            return;
                        }

                        pending.addAll(answer.absorb(event));
                        if (!pending.isEmpty()) {
                            deliver(pending.poll());
                            return;
                        }
                    }

                    if (callback.isDead() || run.token.isCancelled()) {
                        // the stream ended, or was cut short, after the user stopped it
                        cancelled();
                        return;
                    }
                    answer.streamEnded = stream.finished();
                    completed = true;
                    closeStream();
                    recordUsage();
                    if (!answer.complete()) {
                        // the connection ended without the end of the answer: what arrived is not a whole answer
                        fail(run.outcome, new InferenceException(Type.CONNECTION, null, T.localized(THE_CONNECTION_TO_XAI), null));
                    } else if (answer.isEmptyAndStopped() && answer.calls().isEmpty()) {
                        // nothing came and the model was stopped: that is not an empty answer, the user has to know why
                        fail(run.outcome, answer.stopped());
                    } else if (!answer.calls().isEmpty() && functionCaller != null) {
                        try {
                            functionCaller.call(answer);
                        } catch (Throwable e) {
                            log.error("Error calling the functions of the model", e);
                            fail(run.outcome, e);
                        }
                    } else {
                        try {
                            callback.onCompletion();
                        } catch (Throwable e) {
                            // completed is already set, so the outer catch would swallow this and the caller would wait forever
                            log.error("Error invoking callback.onCompletion", e);
                            fail(run.outcome, e);
                        }
                    }
                });
            } catch (Throwable e) {
                if (!completed) {
                    completed = true;
                    closeStream();
                    if (run.token.isCancelled()) {
                        // the read failed because the call was cancelled
                        recordUsage();
                        try {
                            callback.onCancel();
                        } catch (Exception ex) {
                            log.error("Error invoking callback.onCancel", ex);
                        }
                        return;
                    }
                    if (!answer.received.isEmpty()) {
                        // it answered before it failed, so that was billed; a failure before any text is not counted
                        recordUsage();
                    }
                    fail(run.outcome, e);
                }
            }
        }

        private void deliver(ChunkPiece piece) throws Exception {
            callback.onChunk(this, piece.reasoning() ? ChunkType.REASONING : ChunkType.RESPONSE, piece.text());
        }

        private void cancelled() throws Exception {
            completed = true;
            closeStream();
            recordUsage();
            callback.onCancel();
        }

        /** Counts the request, once, whichever way it ends. */
        private void recordUsage() {
            if (usageRecorded) {
                return;
            }
            usageRecorded = true;
            usageRecorder.record(answer.reported(), answer.received.toString());
        }

        private void closeStream() {
            run.forget();
            stream.close();
        }
    }
}
