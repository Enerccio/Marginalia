package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceService.ChunkType;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncCallback;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncController;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationController;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepBase;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepType;
import com.github.enerccio.marginalia.loc.L;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class InferenceStep extends GenerationStepBase {
    private static final Gson gson = new GsonBuilder().create();

    public static final String REASONING_CHUNK = "REASONING_CHUNK";
    public static final String CHUNK = "CHUNK";

    @Override
    protected void onStep(GenerationController controller) throws Exception {
        controller.emitEvent(Events.BEFORE_INFERENCE, () -> {
            InferenceService inferenceService = inferenceServices.forAI(controller.getManuscript().getAi());

            controller.getMessage().setPromptTokens(inferenceService.countTokens(gson.toJson(controller.getPayload())));

            inferenceService.stream(controller.getPayload(), new InferenceAsyncCallback() {

                @Override
                public void onChunk(InferenceAsyncController inferenceController, ChunkType chunkType, String text) throws Exception {
                    if (controller.getCancellationToken().isCancelled()) {
                        controller.getUIListener().onCancelled(controller.getMessage());
                        controller.jumpTo(GenerationStepType.CLEANUP);
                        return;
                    }
                    if (Thread.interrupted()) {
                        controller.getUIListener().onSimpleError(loc.getValue(L.MSG_INTERRUPTED));
                        controller.jumpTo(GenerationStepType.CLEANUP);
                        return;
                    }

                    if (chunkType == ChunkType.REASONING) {
                        controller.getProperties().put(REASONING_CHUNK, text);
                        controller.emitEvent(Events.REASONING_CHUNK_RECEIVED, () -> {
                            Manuscript manuscript = controller.getManuscript();
                            ChatMessage chatMessage = controller.getMessage();
                            if (chatMessage.getTtft() == null) {
                                chatMessage.setTtft(new Date());
                            }
                            String t = (String) controller.getProperties().get(REASONING_CHUNK);
                            long deltaTokenIncrease = inferenceService.countTokensApprox(t);
                            String existingReasoning = StringUtils.defaultString(chatMessage.getResponseReasoning());
                            chatMessage.setResponseReasoning(existingReasoning + t);
                            chatMessage.setTokenReasoningCount(chatMessage.getTokenReasoningCount() == null ? deltaTokenIncrease : chatMessage.getTokenReasoningCount() + deltaTokenIncrease);
                            controller.setMessage(chatMessageService.save(chatMessage));
                            controller.setManuscript(manuscriptService.save(manuscript));
                            controller.getUIListener().onReasoningChunk(t, controller.getMessage());
                            controller.getUIListener().onMetricsUpdated(controller.getMessage());
                            inferenceController.continueInference();
                        });
                    } else {
                        controller.getProperties().put(CHUNK, text);
                        controller.emitEvent(Events.CHUNK_RECEIVED, () -> {
                            Manuscript manuscript = controller.getManuscript();
                            ChatMessage chatMessage = controller.getMessage();
                            if (chatMessage.getTtft() == null) {
                                chatMessage.setTtft(new Date());
                            }
                            if (chatMessage.getReasoningEnd() == null) {
                                chatMessage.setReasoningEnd(new Date());
                            }
                            String t = (String) controller.getProperties().get(CHUNK);
                            long deltaTokenIncrease = inferenceService.countTokensApprox(t);
                            String existingResponse = StringUtils.defaultString(chatMessage.getResponse());
                            chatMessage.setResponse(existingResponse + t);
                            chatMessage.setTokenCount(chatMessage.getTokenCount() + deltaTokenIncrease);
                            chatMessage.setWordCount(countWords(chatMessage.getResponse()));
                            controller.setMessage(chatMessageService.save(chatMessage));
                            controller.setManuscript(manuscriptService.save(manuscript));
                            controller.getUIListener().onResponseChunk(t, controller.getMessage());
                            controller.getUIListener().onMetricsUpdated(controller.getMessage());
                            inferenceController.continueInference();
                        });
                    }
                }

                @Override
                public void onCompletion() throws Exception {
                    Manuscript manuscript = controller.getManuscript();
                    ChatMessage chatMessage = controller.getMessage();
                    if (StringUtils.isNotBlank(chatMessage.getResponse())) {
                        chatMessage.setTokenCount(inferenceService.countTokens(chatMessage.getResponse()));
                    }
                    if (StringUtils.isNotBlank(chatMessage.getResponseReasoning())) {
                        chatMessage.setTokenReasoningCount(inferenceService.countTokens(chatMessage.getResponseReasoning()));
                    }
                    controller.setMessage(chatMessageService.save(chatMessage));
                    controller.setManuscript(manuscriptService.save(manuscript));
                    controller.getUIListener().onMetricsUpdated(controller.getMessage());

                    controller.getUIListener().onComplete(controller.getMessage());
                    controller.next();
                }

                @Override
                public void onCancel() throws Exception {
                    controller.jumpTo(GenerationStepType.CLEANUP);
                }

                @Override
                public void onError(Exception exception) throws Exception {
                    controller.getUIListener().onError(exception);
                    controller.jumpTo(GenerationStepType.CLEANUP);
                }

                @Override
                public boolean isDead() {
                    return controller.getCancellationToken().isCancelled();
                }
            });

            controller.emitEvent(Events.AFTER_INFERENCE, controller::next);
        });
    }

    private int countWords(String text) {
         return chatMessageService.countWords(text);
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.INFERENCE;
    }
}