package com.github.enerccio.marginalia.domain.service.impl.inference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.concurrent.AsyncRunnableWrapper;
import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.InferenceException.Type;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.traits.SupportedAI;
import com.google.gson.JsonElement;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.core.Timeout;
import com.openai.core.http.StreamResponse;
import com.openai.errors.OpenAIIoException;
import com.openai.errors.OpenAIServiceException;
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

import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

@SupportedAI(AIType.OPEN_AI_COMPATIBLE)
@Configurable
public class OpenAICompatibleInferenceService implements InferenceService, InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(OpenAICompatibleInferenceService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final int DEFAULT_TIMEOUT_SECONDS = 300;
    public static final int DEFAULT_MAX_RETRIES = 2;

    private static final Pattern CONTEXT_OVERFLOW = Pattern.compile(
            "context[ _-]?(length|window|size)|maximum context|too many tokens|token limit|prompt is too long"
                    + "|context.{0,30}too (long|large)|too (long|large).{0,30}context|exceeds? .{0,40}(context|tokens)",
            Pattern.CASE_INSENSITIVE);

    @Autowired
    private TokenizerService tokenizerService;

    @Autowired
    private Configuration configuration;

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
        OpenAIClient client = openClient();
        CancellationToken cancellationToken = new CancellationToken();

        List<ChatCompletionMessageParam> messages = new ArrayList<>();
        for (LLMChatMessage msg : payload) {
            if (msg.getContent() == null) {
                continue;
            }
            switch (msg.getRole()) {
                case SYSTEM -> messages.add(ChatCompletionMessageParam.ofSystem(
                        ChatCompletionSystemMessageParam.builder().content(msg.getContent()).build()));
                case USER -> messages.add(ChatCompletionMessageParam.ofUser(
                        ChatCompletionUserMessageParam.builder().content(msg.getContent()).build()));
                case ASSISTANT -> messages.add(ChatCompletionMessageParam.ofAssistant(
                        ChatCompletionAssistantMessageParam.builder().content(msg.getContent()).build()));
            }
        }

        ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                .model(ai.getModel())
                .messages(messages);

        Set<String> overridden = applyAdditionalParameters(paramsBuilder);

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

        AsyncRunnableWrapper wrapper = new AsyncRunnableWrapper(configuration);
        CompletableFuture.runAsync(() -> {
            try {
                wrapper.run(() -> {
                    if (cancellationToken.isCancelled())
                        return;
                    StreamResponse<ChatCompletionChunk> streamResponse = client.chat().completions().createStreaming(paramsBuilder.build());
                    OpenAIInferenceAsyncController controller = new OpenAIInferenceAsyncController(streamResponse, callback, cancellationToken);
                    controller.continueInference();
                });
            } catch (Throwable e) {
                log.error("Failed to start streaming inference: {}", e.getMessage(), e);
                try {
                    callback.onError(translate(e));
                } catch (Exception ex) {
                    log.error("Error during callback.onError", ex);
                }
            }
        });
        return cancellationToken;
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
                .apiKey(ai.getApiKey())
                .timeout(timeouts)
                .maxRetries(retries)
                .build();
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

        public OpenAIInferenceAsyncController(StreamResponse<ChatCompletionChunk> streamResponse,
                                              InferenceAsyncCallback callback, CancellationToken cancellationToken) {
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
                            callback.onCancel();
                            return;
                        }

                        ChatCompletionChunk chunk = iterator.next();
                        for (ChatCompletionChunk.Choice choice : chunk.choices()) {
                            ChatCompletionChunk.Choice.Delta delta = choice.delta();

                            String reasoning = extractReasoningContent(delta);
                            if (reasoning != null && !reasoning.isEmpty()) {
                                pendingChunks.add(new PendingChunk(ChunkType.REASONING, reasoning));
                            }

                            delta.content().ifPresent(content -> {
                                if (!content.isEmpty()) {
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
                    try {
                        callback.onError(translate(e));
                    } catch (Exception ex) {
                        log.error("Error invoking callback.onError", ex);
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        private String extractReasoningContent(ChatCompletionChunk.Choice.Delta delta) {
            delta._additionalProperties();
            com.openai.core.JsonValue val = delta._additionalProperties().get("reasoning_content");
            if (val == null) {
                val = delta._additionalProperties().get("reasoning");
            }
            return (val != null && !val.isNull()) ? (String) val.asString().orElse((String) null) : null;
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