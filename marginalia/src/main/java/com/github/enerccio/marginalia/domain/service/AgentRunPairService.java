package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.AgentRunPairKind;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.agent.HandleTranslator;
import com.github.enerccio.marginalia.domain.service.agent.UuidPair;
import com.github.enerccio.marginalia.domain.service.command.CommandScope;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Which item of the working copy of a run is which item of the original (see
 * {@link com.github.enerccio.marginalia.domain.model.impl.AgentRunPair}): written when the copy is made, read when the
 * copy is matched to the original again (the merge, the restore of a checkpoint, the handles of the agent).
 * <p>
 * The reading and writing methods work for the runs of the current user only.
 */
public interface AgentRunPairService {

    /**
     * Stores the pairs of the copy the run has just been given, in place of whatever the run had.
     *
     * @throws IllegalArgumentException when the run is not a run of the user, or the pairs name one item of the copy twice
     */
    void replace(AgentRun run, Collection<UuidPair> pairs) throws Exception;

    /**
     * All the pairs of the run, in the order they were stored.
     */
    List<UuidPair> getAll(AgentRun run) throws Exception;

    /**
     * The pair of the item of the copy.
     */
    Optional<UuidPair> findByCopy(AgentRun run, String copyUuid) throws Exception;

    /**
     * The pair of the item that was copied from the original item.
     */
    Optional<UuidPair> findByOriginal(AgentRun run, String originalUuid) throws Exception;

    long count(AgentRun run) throws Exception;

    /**
     * The handle the agent knows the item of the copy by, given the first time it is asked for: the next number of the
     * kind in the run, {@code h:m17}. The pair of the item is made when it has none (an item the agent made itself has no
     * original). Asking again gives the same handle.
     *
     * @throws IllegalArgumentException when the run is not a run of the user
     */
    String handleFor(AgentRun run, AgentRunPairKind kind, String copyUuid) throws Exception;

    /**
     * The handle of the item of the copy, if it was given one.
     */
    Optional<String> findHandle(AgentRun run, String copyUuid) throws Exception;

    /**
     * The uuid in the copy of the item with the handle.
     */
    Optional<String> findCopyUuid(AgentRun run, String handle) throws Exception;

    /**
     * The translator between the uuids of the copy and the handles of the run, for the commands an agent calls.
     *
     * @param scope the cage of the run: a uuid that is not an item of the book of the scope has no handle
     * @throws IllegalArgumentException when the run is not a run of the user
     */
    HandleTranslator translator(AgentRun run, CommandScope scope) throws Exception;

    /**
     * Deletes the pairs of the run, they have no meaning without its copy. No check of the owner: it is what the run
     * does when it lets go of the copy, whoever caused it.
     */
    void deleteForRun(AgentRun run) throws Exception;

}
