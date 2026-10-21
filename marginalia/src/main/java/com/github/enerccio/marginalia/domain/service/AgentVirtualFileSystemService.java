package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.agent.*;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.google.gson.JsonObject;

/**
 * The files of agents: the <b>workspace</b> of an agent ({@code agent-workspaces/<agent uuid>} in the application
 * folder), which belongs to the agent, persists between its runs and is the only thing the model's virtual root maps
 * to, and the <b>folder of a run</b> ({@code agent-runs/<run uuid>}) with the base snapshot and the checkpoints, which
 * the model never sees except for the list of the checkpoints.
 * <p>
 * The service hands out {@link VirtualFileSystem}s (the cage, see there) for the runner and for the user to browse,
 * and the tools that let the model use one. It also owns the life of the folders: a workspace is copied with a cloned
 * agent and deleted with an agent that is purged or deleted for good, a run folder with its run. All methods work for
 * the objects of the current user only.
 */
public interface AgentVirtualFileSystemService {

    /**
     * What a workspace may hold when the administrator does not say otherwise.
     */
    WorkspaceLimits getLimits();

    /**
     * What the workspace of the agent may hold: the default limits, except what the permission {@code AGENTS} of the
     * agent's owner sets (the largest file, the size of the workspace, the number of files).
     *
     * @throws IllegalArgumentException when the agent is not an agent of the user
     */
    WorkspaceLimits getLimits(Agent agent) throws Exception;

    /**
     * The files of a run: the workspace of its agent and {@code /checkpoints}.
     *
     * @throws IllegalArgumentException when the run is not a run of the user
     */
    VirtualFileSystem open(AgentRun run) throws Exception;

    /**
     * Where the files of a run are on the disk, for the services that keep checkpoints there. Real paths: they never
     * go to the model.
     *
     * @throws IllegalArgumentException when the run is not a run of the user
     */
    RunFolder getFolder(AgentRun run) throws Exception;

    /**
     * The workspace of an agent alone, to look at and to clear.
     *
     * @throws IllegalArgumentException when the agent is not an agent of the user
     */
    VirtualFileSystem openWorkspace(Agent agent) throws Exception;

    /**
     * The functions of the model for the files ({@link com.github.enerccio.marginalia.domain.service.agent.AgentFileTools}),
     * to put with the other tools of the run.
     */
    default FunctionSet getFileTools(VirtualFileSystem fileSystem) {
        return getFileTools(fileSystem, null);
    }

    /**
     * As {@link #getFileTools(VirtualFileSystem)}, with the results of the commands of the run for the tool that saves
     * the last one.
     *
     * @param commandResults null when there is none, the tool then says there is nothing to save
     */
    FunctionSet getFileTools(VirtualFileSystem fileSystem, CommandResults commandResults);

    /**
     * How much the workspace of the agent holds.
     */
    WorkspaceUsage getUsage(Agent agent) throws Exception;

    /**
     * Deletes everything in the workspace of the agent, for the user to get rid of stale notes.
     *
     * @throws IllegalStateException when the agent is working, the run would lose its files
     */
    void clearWorkspace(Agent agent) throws Exception;

    /**
     * Copies the workspace of an agent to another agent of the user, what cloning an agent does.
     */
    void copyWorkspace(Agent source, Agent target) throws Exception;

    /**
     * Deletes the workspace of an agent that is deleted for good. A soft deleted agent keeps it, it can be restored.
     * The files go when the transaction commits.
     */
    void deleteWorkspace(Agent agent);

    /**
     * Stores the base snapshot of the run, the original book as it was when the working copy was made (see
     * {@link com.github.enerccio.marginalia.domain.service.ManuscriptService.AgenticCopy#base()}), in its folder, in place of
     * the one it had. The file is deleted again when the transaction it is written in rolls back.
     *
     * @throws IllegalArgumentException when the run is not a run of the user
     */
    void writeBase(AgentRun run, JsonObject base) throws Exception;

    /**
     * The base snapshot of the run; null when it has none.
     *
     * @throws IllegalArgumentException when the run is not a run of the user
     */
    JsonObject readBase(AgentRun run) throws Exception;

    /**
     * Replaces the base snapshot of a run that keeps its copy, when the transaction commits (the files are not brought back by
     * a rollback, and a base that does not go with the copy is worse than the old one). The old file is replaced whole.
     *
     * @throws IllegalArgumentException when the run is not a run of the user
     */
    void updateBase(AgentRun run, JsonObject base) throws Exception;

    /**
     * Keeps the report of the sync a resume made (the changes of the user that were taken into the copy and the ones that
     * conflicted), in the folder of the base, when the transaction commits; it is what {@code get_sync_report} answers. The
     * report also waits to be told to the model ({@link #takePendingSync}). A null report removes the previous one.
     *
     * @throws IllegalArgumentException when the run is not a run of the user
     */
    void writeSyncReport(AgentRun run, String report) throws Exception;

    /**
     * The report of the last sync of the run; null when it had none.
     *
     * @throws IllegalArgumentException when the run is not a run of the user
     */
    String readSyncReport(AgentRun run) throws Exception;

    /**
     * The report of a sync the model was not told of yet, and marks it as told; null when there is none. No check of the
     * owner: the runner asks for the run it works.
     */
    String takePendingSync(AgentRun run);

    /**
     * Keeps the note the author wrote with a resume, in the folder of the base, when the transaction commits, until the model is told
     * ({@link #takeResumeNote}); a note that was not told yet is kept and the new one follows it. It is a file, so it survives a restart
     * and does not need the queue of a conversation: an autonomous run has the note too.
     *
     * @throws IllegalArgumentException when the run is not a run of the user
     */
    void writeResumeNote(AgentRun run, String note) throws Exception;

    /**
     * Keeps the id of the call of the agent that is about to change something (the working copy or the workspace), at once; null
     * says that no call began (it waits for the author's approval, or for its turn). After a restart the runner finds the call
     * without a result: when this is its id, the call may have been done in part and the agent is told so; else it was not
     * started, and it is made again. No check of the owner: the runner writes it for the run it works.
     */
    void writeCallMarker(AgentRun run, String callId);

    /**
     * The id {@link #writeCallMarker} kept; null when there is none.
     */
    String readCallMarker(AgentRun run);

    /**
     * Keeps what the author wrote to a conversation while it worked (the queue of messages and commands, as the runner writes
     * it), at once, so a restart does not lose it. Null removes it. No check of the owner.
     */
    void writeQueue(AgentRun run, String json);

    /**
     * What {@link #writeQueue} kept; null when there is none.
     */
    String readQueue(AgentRun run);

    /**
     * As {@link #writeCallMarker(AgentRun, String)}, for the call of the sub-agent with the handle.
     */
    void writeCallMarker(AgentRun run, String subAgent, String callId);

    /**
     * The id {@link #writeCallMarker(AgentRun, String, String)} kept for the sub-agent; null when there is none.
     */
    String readCallMarker(AgentRun run, String subAgent);

    /**
     * Keeps the last results of the commands of the agent (JSON, as the runner writes it), at once. Null removes it.
     * No check of the owner: the runner writes it for the run it works.
     */
    void writeResults(AgentRun run, String json);

    /**
     * What {@link #writeResults} kept; null when there is none.
     */
    String readResults(AgentRun run);

    /**
     * Keeps the messages and commands of the author that the runner took from the queue and has not delivered yet (JSON, in the
     * format of the queue), at once. Null removes it. No check of the owner.
     */
    void writeDelivery(AgentRun run, String json);

    /**
     * What {@link #writeDelivery} kept; null when there is none.
     */
    String readDelivery(AgentRun run);

    /**
     * Keeps the to-do list of the agent (JSON, as the runner writes it), at once, so a restart does not lose it. Null removes it.
     * No check of the owner: the runner writes it for the run it works.
     */
    void writePlan(AgentRun run, String json);

    /**
     * What {@link #writePlan} kept; null when there is none.
     */
    String readPlan(AgentRun run);

    /**
     * The note of the author with the last resume that the model was not told of yet, and marks it as told; null when there is none.
     * No check of the owner: the runner asks for the run it works.
     */
    String takeResumeNote(AgentRun run);

    /**
     * Deletes the base snapshot of a run that has let go of its working copy, nothing is compared with it any more. The
     * file goes when the transaction commits.
     */
    void deleteBase(AgentRun run);

    /**
     * Deletes the checkpoints of a run that has let go of its working copy, there is nothing to restore them into. The
     * files go when the transaction commits.
     */
    void deleteCheckpoints(AgentRun run);

    /**
     * Deletes the folder of a run that is deleted for good, see {@link #deleteWorkspace}.
     */
    void deleteRunFolder(AgentRun run);

}
