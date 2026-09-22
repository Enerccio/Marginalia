package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.templates.MasterTemplateData;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.util.ExecutorServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class StoryGenerationServiceImpl implements StoryGenerationService, InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(StoryGenerationServiceImpl.class);

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private AIService aiService;

    @Autowired
    private Localization loc;

    @Autowired
    private ProtocolService protocolService;

    private ExecutorService taskExecutor;

    @Override
    @NoTx
    public CancellationToken generateNextTurn(Manuscript manuscript, TurnInput input, GenerationListener listener) {
        CancellationToken token = new CancellationToken();

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        ThreadCopyRequestAttributes copyAttributes = (attrs != null)
                ? new ThreadCopyRequestAttributes(attrs.getRequest(), attrs.getResponse())
                : null;

        taskExecutor.submit(() -> {
            if (copyAttributes != null) {
                RequestContextHolder.setRequestAttributes(copyAttributes);
            }

            ChatMessage node = null;
            try {
                AI aiModel = aiService.find(manuscript.getAi());
                Protocol protocol = protocolService.find(manuscript.getProtocol());
                if (aiModel == null) {
                    listener.onError(new IllegalStateException(loc.getValue(L.ERROR_AI_NOT_SET)));
                    return;
                }
                if (protocol == null) {
                    listener.onError(new IllegalStateException(loc.getValue(L.ERROR_PROTOCOL_NOT_SET)));
                    return;
                }

                // 1. Initial Node Creation & Database Attachment
                ChatMessage parentLeaf = chatMessageService.find(manuscript.getActiveLeaf());
                
                ChatMessage initialNode = new ChatMessage();
                initialNode.setSceneSetting(input.sceneSetting());
                initialNode.setPovCharacter(input.povCharacter());
                initialNode.setPresentCharacters(input.presentCharacters());
                initialNode.setInstructions(input.instructions());
                initialNode.setModelUsed(aiModel.getName());
                initialNode.setProtocolUsed(protocol.getName());
                initialNode.setRequest(new Date());

                if (parentLeaf == null) {
                    node = chatMessageService.createRoot(manuscript, initialNode);
                } else {
                    initialNode.setParentScript(manuscript);
                    initialNode.setParent(parentLeaf);
                    node = chatMessageService.addChild(parentLeaf, initialNode);
                }

                // Update manuscript active leaf immediately
                manuscript.setActiveLeaf(node);
                manuscriptService.save(manuscript);

                listener.onNodeCreated(node);

                if (token.isCancelled()) {
                    listener.onCancelled(node);
                    return;
                }

                inferenceServices.forAI(aiModel);

                // 2. Prompt Building
                List<ChatMessage> historyBranch = chatMessageService.getBranchFromLeaf(parentLeaf);
                String proseHistory = historyBranch.stream()
                        .map(ChatMessage::getResponse)
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.joining("\n\n"));

                MasterTemplateData masterData = new MasterTemplateData();
                masterData.setManuscript(proseHistory);
                masterData.setNarrativePov(manuscriptService.getPov(manuscript));
                masterData.setNarrativeTense(manuscriptService.getTense(manuscript));
                masterData.setStyle(manuscriptService.getStyle(manuscript));

                String masterTemplateStr = manuscriptService.getMasterTemplate(manuscript);
                String compiledSystemPrompt = templateService.processTemplate(masterTemplateStr, "master", masterData);

                node.setBuiltPrompt(compiledSystemPrompt);
                chatMessageService.save(node);

                // 3. Streaming Inference
                StringBuilder reasoningBuffer = new StringBuilder();
                StringBuilder responseBuffer = new StringBuilder();

                ChatMessage currentNode = node;
                
                /* 
                 * Mock Stream Loop Integration:
                 * Replace this with your actual streaming client call from InferenceServices.
                 */
                boolean firstTokenReceived = false;

                for (int i = 0; i < 50; i++) {
                    if (token.isCancelled()) {
                        break;
                    }

                    Thread.sleep(100); // Simulate network chunk latency
                    if (Thread.interrupted()) {
                        return;
                    }

                    if (!firstTokenReceived) {
                        firstTokenReceived = true;
                        currentNode.setTtft(new Date());
                        listener.onMetricsUpdated(currentNode);
                    }

                    // Simulate reasoning phase vs response phase
                    if (i < 10) {
                        String reasoningChunk = "Thinking step " + i + "... ";
                        reasoningBuffer.append(reasoningChunk);
                        currentNode.setResponseReasoning(reasoningBuffer.toString());
                        currentNode.setTokenReasoningCount(reasoningBuffer.length() / 4);
                        listener.onReasoningChunk(reasoningChunk, currentNode);
                    } else {
                        if (currentNode.getReasoningEnd() == null) {
                            currentNode.setReasoningEnd(new Date());
                        }
                        String responseChunk = "Story token " + i + " ";
                        responseBuffer.append(responseChunk);
                        currentNode.setResponse(responseBuffer.toString());
                        currentNode.setWordCount(responseBuffer.toString().split("\\s+").length);
                        currentNode.setTokenCount(responseBuffer.length() / 4);
                        listener.onResponseChunk(responseChunk, currentNode);
                    }

                    // Periodic metrics update
                    if (i % 5 == 0) {
                        currentNode = chatMessageService.save(currentNode);
                        listener.onMetricsUpdated(currentNode);
                    }
                }

                // 4. Finalization
                currentNode = chatMessageService.save(currentNode);

                if (token.isCancelled()) {
                    listener.onCancelled(currentNode);
                } else {
                    listener.onComplete(currentNode);
                }

            } catch (Exception e) {
                log.error("Error during story generation", e);
                if (node != null) {
                    try {
                        chatMessageService.save(node);
                    } catch (Exception ignored) {}
                }
                listener.onError(e);
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        });

        return token;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        AtomicLong tc = new AtomicLong();
        taskExecutor = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("Generation thread " + tc.getAndAdd(1));
            return thread;
        });
    }

    @Override
    public void destroy() throws Exception {
        taskExecutor.shutdownNow();
    }
}