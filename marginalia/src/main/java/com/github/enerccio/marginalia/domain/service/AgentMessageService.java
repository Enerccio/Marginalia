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
     * The messages of the run in order, ready to be sent to the model (without the system prompt), the ones a
     * compaction replaced left out.
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
