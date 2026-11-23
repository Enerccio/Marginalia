package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.service.AgentVirtualFileSystemService;
import com.github.enerccio.marginalia.domain.service.agent.VirtualFileSystem;
import com.github.enerccio.marginalia.domain.service.agent.VirtualPath;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The workspace of an agent behind the workspace browser of the agent console (the browser itself is tested in the
 * platform, {@code WorkspaceBrowserTest}): opened, cleared and limited through the service of the agents.
 */
class AgentWorkspaceSourceTest extends AgentTestBase {

    @Autowired
    private AgentVirtualFileSystemService fileSystemService;

    private Agent agent;
    private AgentWorkspaceSource source;

    @BeforeEach
    void setUp() throws Exception {
        loginAgentUser();
        agent = agent();
        source = new AgentWorkspaceSource(fileSystemService, agent);
    }

    @Test
    void opensTheWorkspaceOfTheAgentWithItsLimits() throws Exception {
        fileSystemService.openWorkspace(agent).writeText(VirtualPath.parse("/notes.md"), "my notes", false);

        VirtualFileSystem files = source.open();

        assertThat(files.readText(VirtualPath.parse("/notes.md"))).isEqualTo("my notes");
        assertThat(source.limits()).isEqualTo(fileSystemService.getLimits(agent));
        // the names of the mounts of a run are reserved in the browser too
        assertThatThrownBy(() -> files.createFolder(VirtualPath.parse("/checkpoints")))
                .isInstanceOf(com.github.enerccio.marginalia.domain.service.agent.VirtualFileException.class);
    }

    @Test
    void clearingDeletesEverything() throws Exception {
        VirtualFileSystem files = source.open();
        files.writeText(VirtualPath.parse("/a.txt"), "a", false);
        files.writeText(VirtualPath.parse("/reports/b.txt"), "b", false);

        source.clear();

        assertThat(source.open().list(VirtualPath.ROOT)).isEmpty();
    }

    @Test
    void whileTheAgentWorksTheServiceRefusesToClear() throws Exception {
        source.open().writeText(VirtualPath.parse("/a.txt"), "a", false);
        agentRunService.start(agent);

        assertThatThrownBy(source::clear).isInstanceOf(IllegalStateException.class);

        assertThat(source.open().list(VirtualPath.ROOT)).hasSize(1);
    }
}
