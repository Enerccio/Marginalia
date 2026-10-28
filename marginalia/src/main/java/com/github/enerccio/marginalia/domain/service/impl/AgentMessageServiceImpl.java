package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.AgentMessage;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.repository.AgentMessageRepository;
import com.github.enerccio.marginalia.domain.service.AgentActivityService;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

public class AgentMessageServiceImpl extends ExtendableServiceImpl<AgentMessage, AgentMessageRepository> implements AgentMessageService {

    // how far back the last answer of the model is looked for
    private static final int LATEST_ROWS = 50;

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    @Autowired
    private AgentActivityService activityService;

    @Override
    @CommonTx
    public List<AgentMessage> append(AgentRun run, List<LLMChatMessage> messages) throws Exception {
        AgentRun stored = ownRun(run);
        int ordinal = getRepository().countByRun(stored);
        List<AgentMessage> saved = new ArrayList<>();
        for (LLMChatMessage chatMessage : messages) {
            AgentMessage message = new AgentMessage();
            message.setRun(stored);
            message.setOrdinal(ordinal++);
            message.setChatMessage(chatMessage);
            saved.add(save(message));
        }
        announce(stored);
        return saved;
    }

    @Override
    @CommonTx
    public AgentMessage appendAnswer(AgentRun run, LLMChatMessage answer, long contextTokens, String reasoning) throws Exception {
        return appendAnswer(run, answer, contextTokens, reasoning, null);
    }

    @Override
    @CommonTx
    public AgentMessage appendAnswer(AgentRun run, LLMChatMessage answer, long contextTokens, String reasoning, String telemetry) throws Exception {
        AgentRun stored = ownRun(run);
        AgentMessage message = new AgentMessage();
        message.setRun(stored);
        message.setOrdinal(getRepository().countByRun(stored));
        message.setChatMessage(answer);
        message.setContextTokens(contextTokens);
        message.setReasoning(reasoning);
        message.setTelemetry(telemetry);
        AgentMessage saved = save(message);
        announce(stored);
        return saved;
    }

    @Override
    @CommonTx
    public AgentMessage compact(AgentRun run, LLMChatMessage summary, long contextTokens) throws Exception {
        return compact(run, summary, contextTokens, null);
    }

    @Override
    @CommonTx
    public AgentMessage compact(AgentRun run, LLMChatMessage summary, long contextTokens, String telemetry) throws Exception {
        AgentRun stored = ownRun(run);
        List<AgentMessage> rows = getRepository().findAllByRun(stored);
        for (AgentMessage row : rows) {
            if (!row.isCompacted()) {
                row.setCompacted(true);
                save(row);
            }
        }
        AgentMessage message = new AgentMessage();
        message.setRun(stored);
        message.setOrdinal(rows.size());
        message.setChatMessage(summary);
        message.setSummary(true);
        message.setContextTokens(contextTokens);
        message.setTelemetry(telemetry);
        AgentMessage saved = save(message);
        announce(stored);
        return saved;
    }

    @Override
    @CommonTx
    public int rewind(AgentRun run, int keep) throws Exception {
        AgentRun stored = ownRun(run);
        List<AgentMessage> rows = getRepository().findAllByRun(stored);
        // the compaction the kept messages were made after: what it replaced stays replaced
        int summary = -1;
        for (AgentMessage row : rows) {
            if (row.getOrdinal() < keep && row.isSummary() && !row.isRewound()) {
                summary = row.getOrdinal();
            }
        }
        int forgotten = 0;
        Set<String> goneCalls = new HashSet<>();
        for (AgentMessage row : rows) {
            if (row.getOrdinal() >= keep) {
                for (LLMToolCall call : row.toChatMessage().getToolCalls()) {
                    goneCalls.add(call.getId());
                }
                if (!row.isRewound()) {
                    row.setRewound(true);
                    save(row);
                    if (!row.isCompacted()) {
                        forgotten++;
                    }
                }
            } else if (!row.isRewound() && row.isCompacted() != row.getOrdinal() < summary) {
                row.setCompacted(row.getOrdinal() < summary);
                save(row);
            }
        }
        if (!goneCalls.isEmpty()) {
            // the sub-agents those calls started were never started, as far as the agent knows
            for (AgentMessage row : getRepository().findAllThreadRowsByRun(stored)) {
                if (!row.isRewound() && goneCalls.contains(threadCallOf(stored, row.getThread()))) {
                    row.setRewound(true);
                    save(row);
                }
            }
        }
        announce(stored);
        return forgotten;
    }

    private String threadCallOf(AgentRun run, String handle) throws Exception {
        for (AgentMessage row : getRepository().findAllByRunAndThread(run, handle)) {
            if (row.getThreadCall() != null) {
                return row.getThreadCall();
            }
        }
        return null;
    }

    @Override
    @CommonTx
    public AgentMessage startThread(AgentRun run, String handle, String task, String callId, LLMChatMessage first) throws Exception {
        return startThread(run, handle, task, callId, null, first);
    }

    @Override
    @CommonTx
    public AgentMessage startThread(AgentRun run, String handle, String task, String callId, String profile, LLMChatMessage first) throws Exception {
        AgentRun stored = ownRun(run);
        AgentMessage message = new AgentMessage();
        message.setRun(stored);
        message.setThread(handle);
        message.setOrdinal(getRepository().countByRunAndThread(stored, handle));
        message.setChatMessage(first);
        message.setThreadTask(task);
        message.setThreadCall(callId);
        message.setThreadProfile(profile);
        AgentMessage saved = save(message);
        announce(stored);
        return saved;
    }

    @Override
    @CommonTx
    public List<AgentMessage> appendToThread(AgentRun run, String handle, List<LLMChatMessage> messages) throws Exception {
        AgentRun stored = ownRun(run);
        int ordinal = getRepository().countByRunAndThread(stored, handle);
        List<AgentMessage> saved = new ArrayList<>();
        for (LLMChatMessage chatMessage : messages) {
            AgentMessage message = new AgentMessage();
            message.setRun(stored);
            message.setThread(handle);
            message.setOrdinal(ordinal++);
            message.setChatMessage(chatMessage);
            saved.add(save(message));
        }
        announce(stored);
        return saved;
    }

    @Override
    @CommonTx
    public AgentMessage appendAnswerToThread(AgentRun run, String handle, LLMChatMessage answer, long contextTokens, String reasoning) throws Exception {
        return appendAnswerToThread(run, handle, answer, contextTokens, reasoning, null);
    }

    @Override
    @CommonTx
    public AgentMessage appendAnswerToThread(AgentRun run, String handle, LLMChatMessage answer, long contextTokens, String reasoning,
                                             String telemetry) throws Exception {
        AgentRun stored = ownRun(run);
        AgentMessage message = new AgentMessage();
        message.setRun(stored);
        message.setThread(handle);
        message.setOrdinal(getRepository().countByRunAndThread(stored, handle));
        message.setChatMessage(answer);
        message.setContextTokens(contextTokens);
        message.setReasoning(reasoning);
        message.setTelemetry(telemetry);
        AgentMessage saved = save(message);
        announce(stored);
        return saved;
    }

    @Override
    @CommonTx
    public AgentMessage endThread(AgentRun run, String handle, String end, String text) throws Exception {
        AgentRun stored = ownRun(run);
        for (AgentMessage row : getRepository().findAllByRunAndThread(stored, handle)) {
            if (row.getThreadEnd() != null) {
                // the first end wins
                return row;
            }
        }
        AgentMessage message = new AgentMessage();
        message.setRun(stored);
        message.setThread(handle);
        message.setOrdinal(getRepository().countByRunAndThread(stored, handle));
        message.setChatMessage(LLMChatMessage.of(LLMRole.ASSISTANT, text == null ? "" : text));
        message.setThreadEnd(end);
        AgentMessage saved = save(message);
        announce(stored);
        return saved;
    }

    @Override
    @CommonTx
    public int endOpenThreads(AgentRun run, String end, String reason) throws Exception {
        int ended = 0;
        for (SubThread thread : threads(run)) {
            if (!thread.isOver() && !thread.rewound()) {
                endThread(run, thread.handle(), end, reason);
                ended++;
            }
        }
        return ended;
    }

    @Override
    @CommonTxReadOnly
    public List<SubThread> threads(AgentRun run) throws Exception {
        Map<String, List<AgentMessage>> byHandle = new LinkedHashMap<>();
        for (AgentMessage row : getRepository().findAllThreadRowsByRun(ownRun(run))) {
            byHandle.computeIfAbsent(row.getThread(), handle -> new ArrayList<>()).add(row);
        }
        List<SubThread> threads = new ArrayList<>();
        for (Map.Entry<String, List<AgentMessage>> entry : byHandle.entrySet()) {
            List<AgentMessage> rows = entry.getValue();
            rows.sort((a, b) -> Integer.compare(a.getOrdinal(), b.getOrdinal()));
            AgentMessage first = rows.getFirst();
            AgentMessage last = rows.getLast();
            threads.add(new SubThread(entry.getKey(), first.getThreadTask(), first.getThreadCall(), first.getThreadProfile(),
                    last.getThreadEnd(), last.getThreadEnd() == null ? null : last.toChatMessage().getContent(),
                    first.isRewound(), rows));
        }
        return threads;
    }

    @Override
    @CommonTxReadOnly
    public String lastRow(AgentRun run, int skip) throws Exception {
        List<AgentMessage> latest = getRepository().findLatestByRun(ownRun(run), skip + 1);
        return latest.size() > skip ? latest.get(skip).getUuid() : null;
    }

    @Override
    @CommonTxReadOnly
    public List<AgentMessage> messages(AgentRun run) throws Exception {
        return getRepository().findAllByRun(ownRun(run));
    }

    @Override
    @CommonTxReadOnly
    public long getContextTokens(AgentRun run) throws Exception {
        // the answers of the model are the latest rows but for the results of the calls of the last round
        for (AgentMessage message : getRepository().findLatestByRun(ownRun(run), LATEST_ROWS)) {
            if (message.getContextTokens() != null && !message.isRewound()) {
                return message.getContextTokens();
            }
        }
        return 0;
    }

    @Override
    @CommonTxReadOnly
    public List<LLMChatMessage> transcript(AgentRun run) throws Exception {
        List<LLMChatMessage> messages = new ArrayList<>();
        for (AgentMessage message : getRepository().findAllByRun(ownRun(run))) {
            if (!message.isCompacted() && !message.isRewound()) {
                messages.add(message.toChatMessage());
            }
        }
        return messages;
    }

    @Override
    @CommonTxReadOnly
    public int count(AgentRun run) throws Exception {
        return getRepository().countByRun(ownRun(run));
    }

    @Override
    @CommonTx
    public void deleteForRun(AgentRun run, boolean hard) throws Exception {
        for (AgentMessage message : getRepository().findEveryByRun(run)) {
            delete(message, hard);
        }
    }

    /**
     * The consoles of the agent read the transcript again when it is stored.
     */
    private void announce(AgentRun run) {
        Long agentId = run.getAgent() == null ? null : run.getAgent().getId();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    activityService.changed(agentId);
                }
            });
        } else {
            activityService.changed(agentId);
        }
    }

    private AgentRun ownRun(AgentRun run) throws Exception {
        AgentRun stored = run == null ? null : agentRunService.findForUser(run.getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        return stored;
    }
}
