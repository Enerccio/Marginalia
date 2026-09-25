package com.github.enerccio.marginalia.domain.service.impl.inference;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.concurrent.AsyncRunnableWrapper;
import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.TokenizerService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.traits.SupportedAI;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.StreamResponse;
import com.openai.models.chat.completions.*;
import com.openai.models.models.Model;
import com.openai.models.models.ModelListPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@SupportedAI(AIType.OPEN_AI_COMPATIBLE)
@Configurable
public class OpenAICompatibleInferenceService implements InferenceService {
    private static final Logger log = LoggerFactory.getLogger(OpenAICompatibleInferenceService.class);

    @Autowired
    private TokenizerService tokenizerService;

    @Autowired
    private Configuration configuration;

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
    public void stream(List<LLMChatMessage> payload, InferenceAsyncCallback callback) throws Exception {
        OpenAIClient client = openClient();

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

        if (ai.getMaxCompletionTokens() != null && ai.getMaxCompletionTokens() > 0) {
            paramsBuilder.maxCompletionTokens(ai.getMaxCompletionTokens());
        }

        AsyncRunnableWrapper wrapper = new AsyncRunnableWrapper(configuration);
        CompletableFuture.runAsync(() -> {
            try {
                wrapper.run(() -> {
                    StreamResponse<ChatCompletionChunk> streamResponse = client.chat().completions().createStreaming(paramsBuilder.build());
                    OpenAIInferenceAsyncController controller = new OpenAIInferenceAsyncController(streamResponse, callback);
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
    }

    private OpenAIClient openClient() {
        return OpenAIOkHttpClient.builder()
                .baseUrl(ai.getUri())
                .apiKey(ai.getApiKey())
                .build();
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
        private boolean completed = false;

        public OpenAIInferenceAsyncController(StreamResponse<ChatCompletionChunk> streamResponse,
                                              InferenceAsyncCallback callback) {
            this.streamResponse = streamResponse;
            this.iterator = streamResponse.stream().iterator();
            this.callback = callback;
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
                    // 1. Deliver buffered chunks remaining from previous network packets
                    if (!pendingChunks.isEmpty()) {
                        PendingChunk chunk = pendingChunks.poll();
                        callback.onChunk(this, chunk.type(), chunk.text());
                        return;
                    }

                    // 2. Fetch network stream until next reasoning or content chunk is found
                    while (iterator.hasNext()) {
                        if (completed) {
                            return;
                        }
                        if (callback.isDead()) {
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
