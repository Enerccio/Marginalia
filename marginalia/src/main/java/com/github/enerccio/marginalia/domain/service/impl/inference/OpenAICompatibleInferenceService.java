package com.github.enerccio.marginalia.domain.service.impl.inference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.concurrent.AsyncRunnableWrapper;
import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.TokenLimits;
import com.github.enerccio.marginalia.domain.service.TokenizerService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.traits.SupportedAI;
import com.google.gson.JsonElement;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.core.http.StreamResponse;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@SupportedAI(AIType.OPEN_AI_COMPATIBLE)
@Configurable
public class OpenAICompatibleInferenceService implements InferenceService, InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(OpenAICompatibleInferenceService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

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
        OpenAIClient client = openClient();

        ModelListPage modelList = client.models().list();
        List<Model> models = modelList.data();

        if (models.isEmpty()) {
            log.info("Connection successful, but no models were returned.");
        } else {
            log.info("Connection successful!");
        }

        return models.stream().map(Model::id).toList();
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

        // additional parameters go into the request body as they are and win over the same settings below
        Set<String> overridden = new HashSet<>();
        if (ai.getAdditionalParameters() != null) {
            for (Map.Entry<String, JsonElement> parameter : ai.getAdditionalParameters().entrySet()) {
                paramsBuilder.putAdditionalBodyProperty(parameter.getKey(),
                        JsonValue.fromJsonNode(objectMapper.readTree(parameter.getValue().toString())));
                overridden.add(parameter.getKey());
            }
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
                    callback.onError(e);
                } catch (Exception ex) {
                    log.error("Error during callback.onError", ex);
                }
            }
        });
        return cancellationToken;
    }

    private OpenAIClient openClient() {
        return OpenAIOkHttpClient.builder()
                .baseUrl(ai.getUri())
                .apiKey(ai.getApiKey())
                .build();
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
                    callback.onCompletion();
                });
            } catch (Throwable e) {
                if (!completed) {
                    completed = true;
                    closeStream();
                    try {
                        callback.onError(e);
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