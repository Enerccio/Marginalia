package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.agent.CommandResults;
import com.github.enerccio.marginalia.domain.service.agent.VirtualFileSystem;
import com.github.enerccio.marginalia.domain.service.agent.WorkspaceLimits;
import com.github.enerccio.marginalia.domain.service.agent.WorkspaceUsage;
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
     * Deletes the base snapshot of a run that has let go of its working copy, nothing is compared with it any more. The
     * file goes when the transaction commits.
     */
    void deleteBase(AgentRun run);

    /**
     * Deletes the folder of a run that is deleted for good, see {@link #deleteWorkspace}.
     */
    void deleteRunFolder(AgentRun run);

}
