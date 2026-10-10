package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.Constants;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.InferenceErrors;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceService.ChunkType;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncCallback;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncController;
import com.github.enerccio.marginalia.domain.service.TokenLimits;
import com.github.enerccio.marginalia.domain.service.impl.generation.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationController.State;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.tools.Pointer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class InferenceStep extends GenerationStepBase {
    private static final Logger log = LoggerFactory.getLogger(InferenceStep.class);
    private static final Gson gson = new GsonBuilder().create();


    @Override
    protected void onStep(GenerationController controller) throws Exception {
        controller.emitEvent(Events.BEFORE_INFERENCE, () -> {
            InferenceService inferenceService = inferenceServices.forAI(controller.getManuscript().getAi());

            long promptTokens = inferenceService.countTokens(gson.toJson(controller.getPayload()));
            controller.getMessage().setPromptTokens(promptTokens);
            controller.setMessage(chatMessageService.save(controller.getMessage()));
            warnIfOverflowing(controller, promptTokens);

            Pointer<Long> lastChunkReceived = new Pointer<>(System.currentTimeMillis());
            inferenceService.stream(controller.getPayload(), controller.getManuscript().getProtocol(), new InferenceAsyncCallback() {

                @Override
                public void onChunk(InferenceAsyncController inferenceController, ChunkType chunkType, String text) throws Exception {
                    if (controller.getCancellationToken().isCancelled()) {
                        ChatMessage chatMessage = controller.getMessage();
                        controller.setMessage(chatMessageService.save(chatMessage));
                        controller.jumpTo(GenerationStepType.CLEANUP);
                        inferenceController.terminateInference();
                        return;
                    }
                    if (Thread.interrupted()) {
                        ChatMessage chatMessage = controller.getMessage();
                        controller.setMessage(chatMessageService.save(chatMessage));
                        controller.getUIListener().onSimpleError(loc.getValue(L.MSG_INTERRUPTED));
                        controller.jumpTo(GenerationStepType.CLEANUP);
                        inferenceController.terminateInference();
                        return;
                    }

                    if (chunkType == ChunkType.REASONING) {
                        controller.getProperties().put(GenerationProperties.REASONING_CHUNK, text);
                        controller.emitEvent(Events.REASONING_CHUNK_RECEIVED, () -> {
                            ChatMessage chatMessage = controller.getMessage();
                            if (chatMessage.getTtft() == null) {
                                chatMessage.setTtft(new Date());
                            }
                            String t = (String) controller.getProperties().get(GenerationProperties.REASONING_CHUNK);
                            long deltaTokenIncrease = inferenceService.countTokensApprox(t);
                            String existingReasoning = StringUtils.defaultString(chatMessage.getResponseReasoning());
                            chatMessage.setResponseReasoning(existingReasoning + t);
                            chatMessage.setTokenReasoningCount(chatMessage.getTokenReasoningCount() == null ? deltaTokenIncrease : chatMessage.getTokenReasoningCount() + deltaTokenIncrease);
                            chatMessage = saveIfNecessary(chatMessage, lastChunkReceived);
                            controller.setMessage(chatMessage);
                            controller.getUIListener().onReasoningChunk(t, controller.getMessage());
                            controller.getUIListener().onMetricsUpdated(controller.getMessage());
                            inferenceController.continueInference();
                        });
                    } else {
                        controller.getProperties().put(GenerationProperties.CHUNK, text);
                        controller.emitEvent(Events.CHUNK_RECEIVED, () -> {
                            ChatMessage chatMessage = controller.getMessage();
                            if (chatMessage.getTtft() == null) {
                                chatMessage.setTtft(new Date());
                            }
                            if (chatMessage.getReasoningEnd() == null) {
                                chatMessage.setReasoningEnd(new Date());
                            }
                            String t = (String) controller.getProperties().get(GenerationProperties.CHUNK);
                            long deltaTokenIncrease = inferenceService.countTokensApprox(t);
                            String existingResponse = StringUtils.defaultString(chatMessage.getResponse());
                            chatMessage.setResponse(existingResponse + t);
                            chatMessage.setTokenCount(chatMessage.getTokenCount() + deltaTokenIncrease);
                            chatMessage.setWordCount(countWords(chatMessage.getResponse()));
                            chatMessage = saveIfNecessary(chatMessage, lastChunkReceived);
                            controller.setMessage(chatMessage);
                            if (StringUtils.isNotBlank(chatMessage.getResponse())) {
                                // from now on stop or error keeps what arrived
                                controller.setState(State.PARTIAL_SUCCESS);
                            }
                            controller.getUIListener().onResponseChunk(t, controller.getMessage());
                            controller.getUIListener().onMetricsUpdated(controller.getMessage());
                            inferenceController.continueInference();
                        });
                    }
                }

                @Override
                public void onCompletion() throws Exception {
                    controller.emitEvent(Events.AFTER_INFERENCE, () -> {
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
                        controller.setState(State.SUCCESSFUL);
                        controller.next();
                    });
                }

                @Override
                public void onCancel() throws Exception {
                    Manuscript manuscript = controller.getManuscript();
                    controller.setManuscript(manuscriptService.save(manuscript));
                    ChatMessage chatMessage = controller.getMessage();
                    chatMessage = chatMessageService.save(chatMessage);
                    controller.jumpTo(GenerationStepType.CLEANUP);
                }

                @Override
                public void onError(Throwable exception) throws Exception {
                    Manuscript manuscript = controller.getManuscript();
                    controller.setManuscript(manuscriptService.save(manuscript));
                    ChatMessage chatMessage = controller.getMessage();
                    chatMessageService.save(chatMessage);
                    String description = InferenceErrors.describe(loc, exception);
                    if (description != null) {
                        // the provider's answer, not a bug: say what to fix
                        log.warn("Inference request failed: {}", exception.getMessage());
                        controller.getUIListener().onSimpleError(description);
                    } else {
                        controller.getUIListener().onError(exception);
                    }
                    controller.jumpTo(GenerationStepType.CLEANUP);
                }

                @Override
                public boolean isDead() {
                    return controller.getCancellationToken().isCancelled();
                }
            });
        });
    }

    /**
     * The prompt is trimmed to the budget when it is built, but extensions and the counting of the JSON payload can
     * still push it over: the provider would reject or cut it.
     */
    private void warnIfOverflowing(GenerationController controller, long promptTokens) {
        int context = TokenLimits.contextTokens(controller.getManuscript().getAi(), controller.getManuscript().getProtocol());
        int response = TokenLimits.responseTokens(controller.getManuscript().getAi(), controller.getManuscript().getProtocol());
        if (context > 0 && promptTokens + response > context) {
            log.warn("Prompt of {} tokens and response of {} exceed the context of {}", promptTokens, response, context);
            controller.getUIListener().onWarning(String.format(loc.getValue(L.MSG_PROMPT_MAY_OVERFLOW), promptTokens, response, context));
        }
    }

    private ChatMessage saveIfNecessary(ChatMessage chatMessage, Pointer<Long> lastChunkReceived) throws Exception {
        long wtime = lastChunkReceived.get();
        long ctime = System.currentTimeMillis();
        if (ctime - Constants.WRITE_TIMEOUT > wtime) {
            lastChunkReceived.set(ctime);
            return chatMessageService.save(chatMessage);
        }
        return chatMessage;
    }

    private int countWords(String text) {
         return chatMessageService.countWords(text);
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.INFERENCE;
    }
}