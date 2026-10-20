package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.AgentMessage;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.repository.AgentMessageRepository;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;

import java.util.List;

/**
 * The transcript of a run (see {@link AgentMessage}): the runner appends the messages of every round and reads them
 * back when it goes on, after a pause or a restart of the server.
 */
public interface AgentMessageService extends ExtendableService<AgentMessage, AgentMessageRepository> {

    /**
     * The conversation of one sub-agent of a run, as it was stored.
     *
     * @param handle   the handle the agent knows it by ({@code sub1})
     * @param task     what it was asked to do
     * @param callId   the id of the {@code spawn_agent} call of the agent that started it
     * @param end      how it ended ({@code DONE}, {@code FAILED}, {@code CANCELLED}), null while it only waits to be continued
     * @param result   its answer, or the reason it did not give one; null when it did not end
     * @param rewound  whether the agent went back over the call that started it
     * @param rows     every row of the conversation, the last one is the end of a conversation that ended
     */
    record SubThread(String handle, String task, String callId, String end, String result, boolean rewound, List<AgentMessage> rows) {

        public boolean isOver() {
            return end != null;
        }

        /**
         * The messages the sub-agent is sent: what it said and was told, not how it ended.
         */
        public List<LLMChatMessage> conversation() {
            return rows.stream().filter(row -> row.getThreadEnd() == null && !row.isRewound()).map(AgentMessage::toChatMessage).toList();
        }

        /**
         * How many times the model answered.
         */
        public int rounds() {
            return (int) rows.stream().filter(row -> row.getContextTokens() != null).count();
        }

        /**
         * What the requests of the sub-agent cost in all, as they were reported.
         */
        public long tokens() {
            return rows.stream().filter(row -> row.getContextTokens() != null).mapToLong(AgentMessage::getContextTokens).sum();
        }
    }

    /**
     * Appends the messages to the end of the transcript of the run, in one transaction.
     *
     * @throws IllegalArgumentException when the run is not a run of the current user
     */
    List<AgentMessage> append(AgentRun run, List<LLMChatMessage> messages) throws Exception;

    /**
     * Appends the answer of the model to the transcript, with how big the context was when it was given.
     *
     * @param contextTokens the tokens of the request and of the answer
     * @param reasoning     what the model thought when the provider sent it, null otherwise; kept for the user to read, it is
     *                      not a part of the transcript the model is sent
     * @throws IllegalArgumentException when the run is not a run of the current user
     */
    AgentMessage appendAnswer(AgentRun run, LLMChatMessage answer, long contextTokens, String reasoning) throws Exception;

    /**
     * Starts the conversation of a sub-agent with the message it is told first.
     *
     * @throws IllegalArgumentException when the run is not a run of the current user
     */
    AgentMessage startThread(AgentRun run, String handle, String task, String callId, LLMChatMessage first) throws Exception;

    /**
     * Appends messages to the conversation of a sub-agent.
     *
     * @throws IllegalArgumentException when the run is not a run of the current user
     */
    List<AgentMessage> appendToThread(AgentRun run, String handle, List<LLMChatMessage> messages) throws Exception;

    /**
     * Appends the answer of the model to the conversation of a sub-agent, as {@link #appendAnswer} does for the agent.
     *
     * @throws IllegalArgumentException when the run is not a run of the current user
     */
    AgentMessage appendAnswerToThread(AgentRun run, String handle, LLMChatMessage answer, long contextTokens, String reasoning) throws Exception;

    /**
     * Ends the conversation of a sub-agent: nothing is appended to it and it is not continued with the run.
     *
     * @param end  {@code DONE}, {@code FAILED} or {@code CANCELLED}
     * @param text its answer, or the reason it did not give one
     * @throws IllegalArgumentException when the run is not a run of the current user
     */
    AgentMessage endThread(AgentRun run, String handle, String end, String text) throws Exception;

    /**
     * Ends every conversation of a sub-agent that did not end (the book it worked in is another one now).
     *
     * @return how many were ended
     * @throws IllegalArgumentException when the run is not a run of the current user
     */
    int endOpenThreads(AgentRun run, String end, String reason) throws Exception;

    /**
     * The conversations of the sub-agents of the run in the order they were started, the ones a rewind went back over too.
     *
     * @throws IllegalArgumentException when the run is not a run of the current user
     */
    List<SubThread> threads(AgentRun run) throws Exception;

    /**
     * The rows of the transcript of the run in order: the messages with what is kept beside them (the reasoning of an
     * answer, its context).
     *
     * @throws IllegalArgumentException when the run is not a run of the current user
     */
    List<AgentMessage> messages(AgentRun run) throws Exception;

    /**
     * How big the context of the run was at its last answer of the model, 0 when it has none yet.
     *
     * @throws IllegalArgumentException when the run is not a run of the current user
     */
    long getContextTokens(AgentRun run) throws Exception;

    /**
     * Compacts the transcript: every message there is now is marked compacted - it stays in {@link #messages} for the user
     * and the export and is no longer sent to the model - and the summary is appended as the message the model reads
     * first from now on.
     *
     * @param summary       the message of the user that holds the summary
     * @param contextTokens about how big the context of the run is now, for the console
     * @return the row of the summary
     * @throws IllegalArgumentException when the run is not a run of the current user
     */
    AgentMessage compact(AgentRun run, LLMChatMessage summary, long contextTokens) throws Exception;

    /**
     * Goes back in the conversation: every message from the place {@code keep} on is marked rewound - it stays in
     * {@link #messages} for the user and the export and is no longer sent to the model, and neither are the sub-agents the
     * calls of those messages started. What a compaction after that place
     * replaced is read by the model again (the messages before the last summary that is kept stay replaced).
     *
     * @param keep how many rows of the transcript stay, the place of the first one that goes
     * @return how many messages the model no longer reads
     * @throws IllegalArgumentException when the run is not a run of the current user
     */
    int rewind(AgentRun run, int keep) throws Exception;

    /**
     * The row of the transcript that is the {@code skip}-th from the end (0 is the last one), as the place a checkpoint is
     * made at.
     *
     * @return its uuid, null when the transcript has no such row
     * @throws IllegalArgumentException when the run is not a run of the current user
     */
    String lastRow(AgentRun run, int skip) throws Exception;

    /**
     * The messages of the run in order, ready to be sent to the model (without the system prompt), the ones a
     * compaction replaced or a rewind went back over left out.
     *
     * @throws IllegalArgumentException when the run is not a run of the current user
     */
    List<LLMChatMessage> transcript(AgentRun run) throws Exception;

    /**
     * Number of the messages of the run.
     */
    int count(AgentRun run) throws Exception;

    /**
     * Deletes the transcript of the run, see {@code delete}.
     */
    void deleteForRun(AgentRun run, boolean hard) throws Exception;

}
