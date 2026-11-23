package com.github.enerccio.marginalia.domain.service.agent;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.locks.Lock;

/**
 * The files of an agent as its model sees them, a {@link VirtualFileSystem} (platform) in the layout of Marginalia's
 * agents: the workspace of the agent is the root, and {@code /checkpoints} (the checkpoints of the run, which can only
 * be listed) and {@code /AGENTS.md} (the instructions of the user, read only) are mounts next to it when there is a run.
 * Their names are reserved at the root of the workspace even when the user only looks at the workspace.
 */
public final class AgentFileSystem {

    public static final String CHECKPOINTS = "checkpoints";

    /**
     * The read only file with the instructions of the user to the agent; the name is reserved at the root of the workspace.
     */
    public static final String INSTRUCTIONS = "AGENTS.md";

    /**
     * The folder of the workspace for the scratch files of the sub-agents, one folder for each of them.
     */
    public static final String SUBAGENTS = "tmp";

    private static final Set<String> RESERVED = Set.of(CHECKPOINTS, INSTRUCTIONS);

    private AgentFileSystem() {
    }

    /**
     * The workspace of an agent only, for the user to look at.
     *
     * @param folder the workspace, created when missing
     * @param lock   shared by everything that changes this workspace
     */
    public static VirtualFileSystem ofWorkspace(Path folder, WorkspaceLimits limits, Lock lock) {
        return VirtualFileSystem.ofWorkspace(folder, limits, lock, RESERVED);
    }

    /**
     * The files of a run: the workspace of its agent and the list of its checkpoints.
     */
    public static VirtualFileSystem ofRun(RunFolder folder, WorkspaceLimits limits, Lock lock) {
        return ofRun(folder, limits, lock, null);
    }

    /**
     * The files of a run: the workspace of its agent, the list of its checkpoints and, when there are instructions,
     * {@code /AGENTS.md} with them. The checkpoints are restored by name with the checkpoint tools of the runner, they
     * are not files to read or change; the instructions are the text the run started with, the user changes them in the
     * agent dialog, never the agent.
     *
     * @param instructions the text of the file, null when the file is not there
     */
    public static VirtualFileSystem ofRun(RunFolder folder, WorkspaceLimits limits, Lock lock, String instructions) {
        VirtualFileSystem fs = ofWorkspace(folder.workspace(), limits, lock);
        fs.mount(CHECKPOINTS, new ListOnlyMount(VirtualPath.ROOT.resolve(CHECKPOINTS), folder.checkpoints(),
                "Checkpoints can only be listed, they are restored with the checkpoint tools"));
        if (instructions != null) {
            VirtualPath file = VirtualPath.ROOT.resolve(INSTRUCTIONS);
            fs.mount(INSTRUCTIONS, new TextFileMount(file, instructions,
                    file + " is read only, the instructions are changed by the user"));
        }
        return fs;
    }

}
