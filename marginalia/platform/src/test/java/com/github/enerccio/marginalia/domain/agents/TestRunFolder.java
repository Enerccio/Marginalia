package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.service.agent.ListOnlyMount;
import com.github.enerccio.marginalia.domain.service.agent.VirtualFileSystem;
import com.github.enerccio.marginalia.domain.service.agent.VirtualPath;
import com.github.enerccio.marginalia.domain.service.agent.WorkspaceLimits;

import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

/**
 * The folders of an agent run for the tests of the virtual file system, laid out the way an application keeps them
 * (Marginalia's {@code RunFolder}): a workspace outside the folder of the run, and in the run folder a {@code base}
 * folder the model must never reach and the {@code checkpoints}, which the file system mounts list-only at
 * {@code /checkpoints}.
 */
public record TestRunFolder(Path run, Path workspace) {

    public static final String CHECKPOINTS = "checkpoints";

    public static TestRunFolder of(Path home, String runUuid, String agentUuid) {
        return new TestRunFolder(home.resolve("runs").resolve(UUID.fromString(runUuid).toString()), workspaceOf(home, agentUuid));
    }

    public static Path workspaceOf(Path home, String agentUuid) {
        return home.resolve("workspaces").resolve(UUID.fromString(agentUuid).toString());
    }

    public Path base() {
        return run.resolve("base");
    }

    public Path checkpoints() {
        return run.resolve(CHECKPOINTS);
    }

    /**
     * The workspace with {@code /checkpoints} next to it, its name reserved at the root.
     */
    public VirtualFileSystem fileSystem(WorkspaceLimits limits, Lock lock) {
        return VirtualFileSystem.ofWorkspace(workspace, limits, lock, Set.of(CHECKPOINTS))
                .mount(CHECKPOINTS, new ListOnlyMount(VirtualPath.ROOT.resolve(CHECKPOINTS), checkpoints(),
                        "Checkpoints can only be listed, they are restored with the checkpoint tools"));
    }

}
