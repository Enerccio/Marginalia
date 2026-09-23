package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.GenerationListener;
import com.github.enerccio.marginalia.domain.service.StoryGenerationService;
import com.github.enerccio.marginalia.domain.service.TurnInput;
import com.github.enerccio.marginalia.domain.service.impl.generation.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.EventChain;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.Registration;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.PrePromptData;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class StoryGenerationServiceImpl implements StoryGenerationService, InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(StoryGenerationServiceImpl.class);

    @Autowired
    private Localization loc;

    private List<GenerationStep> installedSteps;
    private final Map<GenerationStepType, GenerationStep> steps = new LinkedHashMap<>();
    private final Map<Events, List<GenerationEvent>> listeners = new ConcurrentHashMap<>();
    private ExecutorService taskExecutor;

    @Override
    public Registration addEventListener(Events event, GenerationEvent listener) {
        List<GenerationEvent> list = listeners.computeIfAbsent(event, e -> new CopyOnWriteArrayList<>());
        list.add(listener);

        // Return callback for clean, mid-run safe removal
        return () -> list.remove(listener);
    }

    // Accessible to GenerationEngine
    protected List<GenerationEvent> getListeners(Events event) {
        return listeners.get(event);
    }

    @Override
    @NoTx
    public CancellationToken generateNextTurn(Manuscript manuscript, TurnInput input, GenerationListener listener) {
        CancellationToken token = new CancellationToken();

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new IllegalStateException("Called out of ui!");
        }
        ThreadCopyRequestAttributes attributes = new ThreadCopyRequestAttributes(attrs.getRequest(), attrs.getResponse());
        GenerationEngine generationEngine = new GenerationEngine();
        generationEngine.setManuscript(manuscript);
        generationEngine.turnInput = input;
        generationEngine.cancellationToken = token;
        generationEngine.uiListener = listener;
        generationEngine.requestAttributes = attributes;
        generationEngine.executeNextStep();

//        taskExecutor.submit(() -> {
//            if (copyAttributes != null) {
//                RequestContextHolder.setRequestAttributes(copyAttributes);
//            }
//
//            ChatMessage node = null;
//            try {
//                AI aiModel = aiService.find(manuscript.getAi());
//                Protocol protocol = protocolService.find(manuscript.getProtocol());
//                if (aiModel == null) {
//                    listener.onError(new IllegalStateException(loc.getValue(L.ERROR_AI_NOT_SET)));
//                    return;
//                }
//                if (protocol == null) {
//                    listener.onError(new IllegalStateException(loc.getValue(L.ERROR_PROTOCOL_NOT_SET)));
//                    return;
//                }
//
//                // 1. Initial Node Creation & Database Attachment
//                ChatMessage parentLeaf = chatMessageService.find(manuscript.getActiveLeaf());
//
//                ChatMessage initialNode = new ChatMessage();
//                initialNode.setSceneSetting(input.sceneSetting());
//                initialNode.setPovCharacter(input.povCharacter());
//                initialNode.setPresentCharacters(input.presentCharacters());
//                initialNode.setInstructions(input.instructions());
//                initialNode.setModelUsed(aiModel.getName());
//                initialNode.setProtocolUsed(protocol.getName());
//                initialNode.setRequest(new Date());
//
//                if (parentLeaf == null) {
//                    node = chatMessageService.createRoot(manuscript, initialNode);
//                } else {
//                    initialNode.setParentScript(manuscript);
//                    initialNode.setParent(parentLeaf);
//                    node = chatMessageService.addChild(parentLeaf, initialNode);
//                }
//
//                // Update manuscript active leaf immediately
//                manuscript.setActiveLeaf(node);
//                manuscriptService.save(manuscript);
//
//                listener.onNodeCreated(node);
//
//                if (token.isCancelled()) {
//                    listener.onCancelled(node);
//                    return;
//                }
//
//                inferenceServices.forAI(aiModel);
//
//                // 2. Prompt Building
//                List<ChatMessage> historyBranch = chatMessageService.getBranchFromLeaf(parentLeaf);
//                String proseHistory = historyBranch.stream()
//                        .map(ChatMessage::getResponse)
//                        .filter(StringUtils::isNotBlank)
//                        .collect(Collectors.joining("\n\n"));
//
//                MasterTemplateData masterData = new MasterTemplateData();
//                masterData.setManuscript(proseHistory);
//                masterData.setNarrativePov(manuscriptService.getPov(manuscript));
//                masterData.setNarrativeTense(manuscriptService.getTense(manuscript));
//                masterData.setStyle(manuscriptService.getStyle(manuscript));
//
//                String masterTemplateStr = manuscriptService.getMasterTemplate(manuscript);
//                String compiledSystemPrompt = templateService.processTemplate(masterTemplateStr, "master", masterData);
//
//                node.setBuiltPrompt(compiledSystemPrompt);
//                chatMessageService.save(node);
//
//                // 3. Streaming Inference
//                StringBuilder reasoningBuffer = new StringBuilder();
//                StringBuilder responseBuffer = new StringBuilder();
//
//                ChatMessage currentNode = node;
//
//                /*
//                 * Mock Stream Loop Integration:
//                 * Replace this with your actual streaming client call from InferenceServices.
//                 */
//                boolean firstTokenReceived = false;
//
//                for (int i = 0; i < 50; i++) {
//                    if (token.isCancelled()) {
//                        break;
//                    }
//
//                    Thread.sleep(100); // Simulate network chunk latency
//                    if (Thread.interrupted()) {
//                        return;
//                    }
//
//                    if (!firstTokenReceived) {
//                        firstTokenReceived = true;
//                        currentNode.setTtft(new Date());
//                        listener.onMetricsUpdated(currentNode);
//                    }
//
//                    // Simulate reasoning phase vs response phase
//                    if (i < 10) {
//                        String reasoningChunk = "Thinking step " + i + "... ";
//                        reasoningBuffer.append(reasoningChunk);
//                        currentNode.setResponseReasoning(reasoningBuffer.toString());
//                        currentNode.setTokenReasoningCount(reasoningBuffer.length() / 4);
//                        listener.onReasoningChunk(reasoningChunk, currentNode);
//                    } else {
//                        if (currentNode.getReasoningEnd() == null) {
//                            currentNode.setReasoningEnd(new Date());
//                        }
//                        String responseChunk = "Story token " + i + " ";
//                        responseBuffer.append(responseChunk);
//                        currentNode.setResponse(responseBuffer.toString());
//                        currentNode.setWordCount(responseBuffer.toString().split("\\s+").length);
//                        currentNode.setTokenCount(responseBuffer.length() / 4);
//                        listener.onResponseChunk(responseChunk, currentNode);
//                    }
//
//                    // Periodic metrics update
//                    if (i % 5 == 0) {
//                        currentNode = chatMessageService.save(currentNode);
//                        listener.onMetricsUpdated(currentNode);
//                    }
//                }
//
//                // 4. Finalization
//                currentNode = chatMessageService.save(currentNode);
//
//                if (token.isCancelled()) {
//                    listener.onCancelled(currentNode);
//                } else {
//                    listener.onComplete(currentNode);
//                }
//
//            } catch (Exception e) {
//                log.error("Error during story generation", e);
//                if (node != null) {
//                    try {
//                        chatMessageService.save(node);
//                    } catch (Exception ignored) {}
//                }
//                listener.onError(e);
//            } finally {
//                RequestContextHolder.resetRequestAttributes();
//            }
//        });

        return token;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        AtomicLong tc = new AtomicLong();
        taskExecutor = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("Generation thread " + StringUtils.leftPad("" + tc.getAndAdd(1), 3, '0'));
            return thread;
        });
        for (GenerationStep step : installedSteps) {
            if (steps.containsKey(step.getType()))
                throw new IllegalStateException("Steps are unique");
            steps.put(step.getType(), step);
        }
    }

    @Override
    public void destroy() throws Exception {
        taskExecutor.shutdownNow();
    }

    public List<GenerationStep> getInstalledSteps() {
        return installedSteps;
    }

    public void setInstalledSteps(List<GenerationStep> installedSteps) {
        this.installedSteps = installedSteps;
    }

    protected class GenerationEngine implements GenerationController {

        private final Map<String, Object> properties = new HashMap<>();
        private GenerationStepType currentStep = GenerationStepType.PREPARE_GENERATION;
        private TurnInput turnInput;
        private Manuscript manuscript;
        private ChatMessage chatMessage;
        private CancellationToken cancellationToken;
        private GenerationListener uiListener;
        private ThreadCopyRequestAttributes requestAttributes;
        private PrePromptData prePromptData;
        private List<LLMChatMessage> payload;

        public GenerationEngine() {

        }

        @Override
        public Map<String, Object> getProperties() {
            return properties;
        }

        @Override
        public Manuscript getManuscript() {
            return manuscript;
        }

        @Override
        public void setManuscript(Manuscript manuscript) {
            this.manuscript = manuscript;
        }

        @Override
        public ChatMessage getMessage() {
            return chatMessage;
        }

        @Override
        public void setMessage(ChatMessage message) {
            this.chatMessage = message;
        }

        @Override
        public TurnInput getInput() {
            return turnInput;
        }

        @Override
        public CancellationToken getCancellationToken() {
            return cancellationToken;
        }

        @Override
        public GenerationListener getUIListener() {
            return uiListener;
        }

        @Override
        public ThreadCopyRequestAttributes getRequestAttributes() {
            return requestAttributes;
        }

        @Override
        public PrePromptData getPrePromptData() {
            return prePromptData;
        }

        @Override
        public void setPrePromptData(PrePromptData prePromptData) {
            this.prePromptData = prePromptData;
        }

        @Override
        public List<LLMChatMessage> getPayload() {
            return payload;
        }

        @Override
        public void setPayload(List<LLMChatMessage> payload) {
            this.payload = payload;
        }

        @Override
        public void next() throws Exception {
            currentStep = currentStep.next();
            executeNextStep();
        }

        @Override
        public void jumpTo(GenerationStepType stepType) throws Exception {
            currentStep = stepType;
            executeNextStep();
        }

        private void executeNextStep() {
            taskExecutor.submit(() -> {
                if (currentStep == null) {
                    // we are done, exit
                    return;
                }

                if (Thread.interrupted()) {
                    getUIListener().onSimpleError(loc.getValue(L.MSG_INTERRUPTED));
                    try {
                        jumpTo(GenerationStepType.CLEANUP);
                    } catch (Exception e) {
                        getUIListener().onError(e);
                    }
                    return;
                }

                if (cancellationToken.isCancelled() && currentStep != GenerationStepType.CLEANUP) {
                    log.info("Generation cancelled by user, short-circuiting to CLEANUP");
                    try {
                        jumpTo(GenerationStepType.CLEANUP);
                    } catch (Exception e) {
                        getUIListener().onError(e);
                    }
                    return;
                }

                GenerationStep step = steps.get(currentStep);
                try {
                    if (step == null) {
                        // not installed? during development normal
                        uiListener.onSimpleError("NOT IMPLEMENTED");
                        if (currentStep != GenerationStepType.CLEANUP)
                            jumpTo(GenerationStepType.CLEANUP);
                        return;
                    }
                    step.step(this);
                } catch (Exception e) {
                    log.error(e.getMessage());
                    log.debug(e.getMessage(), e);
                    getUIListener().onError(e);
                    if (currentStep != GenerationStepType.CLEANUP) {
                        currentStep = GenerationStepType.CLEANUP;
                        executeNextStep();
                    }
                }
            });
        }

        @Override
        public void emitEvent(Events event, FromEventCallback continueAfterEventHandlingArg) {
            log.debug("Emitting event {}.", event);
            FromEventCallback continueAfterEventHandling = () -> {
                log.debug("Returning from event {}.", event);
                continueAfterEventHandlingArg.returnFromEvent();
            };
            List<GenerationEvent> eventListeners = getListeners(event);

            if (eventListeners == null || eventListeners.isEmpty()) {
                invokeContinuation(continueAfterEventHandling);
                return;
            }

            Iterator<GenerationEvent> iterator = eventListeners.iterator();
            GenerationControllerEventImpl eventImpl = new GenerationControllerEventImpl(this);

            EventChain chain = new EventChain() {
                @Override
                public void next() {
                    taskExecutor.submit(() -> {
                        ThreadCopyRequestAttributes attributes = getRequestAttributes();
                        RequestContextHolder.setRequestAttributes(attributes);
                        try {
                            if (cancellationToken.isCancelled()) {
                                invokeContinuation(continueAfterEventHandling);
                                return;
                            }

                            if (Thread.interrupted()) {
                                getUIListener().onSimpleError(loc.getValue(L.MSG_INTERRUPTED));
                                try {
                                    jumpTo(GenerationStepType.CLEANUP);
                                } catch (Exception e) {
                                    getUIListener().onError(e);
                                }
                                return;
                            }

                            if (iterator.hasNext() && !eventImpl.terminated) {
                                try {
                                    GenerationEvent listener = iterator.next();
                                    listener.onEvent(eventImpl, this);
                                } catch (Exception e) {
                                    log.error("Error executing listener for event " + event, e);
                                    next();
                                }
                            } else {
                                invokeContinuation(continueAfterEventHandling);
                            }
                        } finally {
                            RequestContextHolder.resetRequestAttributes();
                        }
                    });
                }

                @Override
                public void terminate() {
                    invokeContinuation(continueAfterEventHandling);
                }
            };

            chain.next();
        }

        private void invokeContinuation(FromEventCallback continuation) {
            if (continuation != null) {
                taskExecutor.submit(() -> {
                    RequestContextHolder.setRequestAttributes(requestAttributes);
                    try {
                        continuation.returnFromEvent();
                    } catch (Exception e) {
                        log.error("Failed continuation after event emission", e);
                        getUIListener().onError(e);
                    } finally {
                        RequestContextHolder.resetRequestAttributes();
                    }
                });
            }
        }
    }

    public static class GenerationControllerEventImpl implements GenerationControllerEvent {

        private final GenerationController controller;
        private boolean terminated = false;

        public GenerationControllerEventImpl(GenerationController controller) {
            this.controller = controller;
        }

        @Override
        public Manuscript getManuscript() {
            return controller.getManuscript();
        }

        @Override
        public void setManuscript(Manuscript manuscript) {
            controller.setManuscript(manuscript);
        }

        @Override
        public ChatMessage getMessage() {
            return controller.getMessage();
        }

        @Override
        public void setMessage(ChatMessage message) {
            controller.setMessage(message);
        }

        @Override
        public CancellationToken getCancellationToken() {
            return controller.getCancellationToken();
        }

        @Override
        public void terminateEvents() {
            this.terminated = true;
        }

        public boolean isTerminated() {
            return terminated;
        }
    }
}