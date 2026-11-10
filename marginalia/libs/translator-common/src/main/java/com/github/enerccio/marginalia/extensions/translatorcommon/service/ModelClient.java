package com.github.enerccio.marginalia.extensions.translatorcommon.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Asks a model one question and waits for the whole answer: what the translators and the designers of the plugins do. The model is chosen by the caller.
 * A run that bound a {@link RunProgress} to its thread sees the reasoning and the answer as they come and can stop the request.
 */
@Configurable
public class ModelClient {

    private final String sessionName;

    /**
     * @param sessionName what the requests are called in the usage records and the log (the name of the plugin)
     */
    public ModelClient(String sessionName) {
        this.sessionName = sessionName;
    }

    @Autowired
    private InferenceServices inferenceServices;
    @Autowired
    private AIService aiService;
    @Autowired
    private ProtocolService protocolService;
    @Autowired
    private SettingService settingService;

    /**
     * The first model that is there: the AI and the protocol of the uuids (either may be null), else the defaults of the user.
     *
     * @return null when there is no model at all
     */
    public ModelSelection choose(String aiUuid, String protocolUuid) throws Exception {
        AI ai = aiUuid != null ? aiService.findForUser(aiUuid) : null;
        Protocol protocol = protocolUuid != null ? protocolService.findForUser(protocolUuid) : null;
        if (ai == null || ai.isDeleted()) {
            UserSetting user = settingService.getOrCreate(UserSetting.class);
            ai = user.getDefaultModel() != null ? aiService.find(user.getDefaultModel()) : null;
            if (protocol == null || protocol.isDeleted()) {
                protocol = user.getDefaultProtocol() != null ? protocolService.find(user.getDefaultProtocol()) : null;
            }
        }
        if (ai == null || ai.isDeleted()) {
            return null;
        }
        return new ModelSelection(ai, protocol != null && !protocol.isDeleted() ? protocol : null);
    }

    /**
     * The model of a book's request: the AI and the protocol of the uuids (the plugin's own choice, either may be null), else the ones of the book,
     * else the defaults of the user.
     *
     * @return null when there is no model at all
     */
    public ModelSelection choose(String aiUuid, String protocolUuid, Manuscript book) throws Exception {
        AI ai = aiUuid != null ? aiService.findForUser(aiUuid) : null;
        Protocol protocol = protocolUuid != null ? protocolService.findForUser(protocolUuid) : null;
        if (ai == null || ai.isDeleted()) {
            ai = book.getAi();
            protocol = protocol != null && !protocol.isDeleted() ? protocol : book.getProtocol();
        }
        if (ai == null || ai.isDeleted()) {
            UserSetting user = settingService.getOrCreate(UserSetting.class);
            ai = user.getDefaultModel() != null ? aiService.find(user.getDefaultModel()) : null;
            protocol = user.getDefaultProtocol() != null ? protocolService.find(user.getDefaultProtocol()) : protocol;
        }
        if (ai == null || ai.isDeleted()) {
            return null;
        }
        return new ModelSelection(ai, protocol != null && !protocol.isDeleted() ? protocol : null);
    }

    public String ask(ModelSelection model, Manuscript book, String system, String user) throws Exception {
        List<LLMChatMessage> payload = List.of(LLMChatMessage.of(LLMRole.SYSTEM, system), LLMChatMessage.of(LLMRole.USER, user));
        CompletableFuture<String> answer = new CompletableFuture<>();
        StringBuilder text = new StringBuilder();
        // the run that asks may show what comes and may be stopped (none: nothing is shown)
        RunProgress progress = RunProgress.current();
        if (progress != null) {
            progress.requestStarted();
        }
        CancellationToken token;
        try (InferenceSession _ = InferenceSession.open(sessionName); UsageContext _ = UsageContext.open(book)) {
            token = inferenceServices.forAIOrThrow(model.ai()).stream(payload, model.protocol(), new InferenceService.InferenceAsyncCallback() {
                @Override
                public void onChunk(InferenceService.InferenceAsyncController controller, InferenceService.ChunkType chunkType, String chunk) throws Exception {
                    boolean reasoning = chunkType == InferenceService.ChunkType.REASONING;
                    if (!reasoning) {
                        text.append(chunk);
                    }
                    if (progress != null) {
                        progress.chunk(reasoning, chunk);
                    }
                    controller.continueInference();
                }

                @Override
                public void onCompletion() {
                    answer.complete(text.toString());
                }

                @Override
                public void onCancel() {
                    answer.cancel(false);
                }

                @Override
                public void onError(Throwable exception) {
                    answer.completeExceptionally(exception);
                }

                @Override
                public boolean isDead() {
                    return answer.isDone();
                }
            });
        }
        if (progress != null) {
            progress.attach(token);
        }
        try {
            return answer.get();
        } catch (InterruptedException e) {
            token.cancel();
            Thread.currentThread().interrupt();
            throw new CancellationException("The request was stopped");
        } catch (ExecutionException e) {
            throw e.getCause() instanceof Exception cause ? cause : e;
        }
    }
}
