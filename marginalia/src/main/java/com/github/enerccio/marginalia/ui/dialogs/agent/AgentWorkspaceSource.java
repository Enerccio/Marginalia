package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.service.AgentVirtualFileSystemService;
import com.github.enerccio.marginalia.domain.service.agent.VirtualFileSystem;
import com.github.enerccio.marginalia.domain.service.agent.WorkspaceLimits;
import com.github.enerccio.marginalia.ui.widgets.WorkspaceBrowser;

/**
 * The workspace of an agent for the {@link WorkspaceBrowser} of the agent console: opened and cleared through the
 * {@link AgentVirtualFileSystemService}, with the checks of the owner, at every look.
 */
public final class AgentWorkspaceSource implements WorkspaceBrowser.Source {

    private final AgentVirtualFileSystemService fileSystemService;
    private final Agent agent;

    public AgentWorkspaceSource(AgentVirtualFileSystemService fileSystemService, Agent agent) {
        this.fileSystemService = fileSystemService;
        this.agent = agent;
    }

    @Override
    public VirtualFileSystem open() throws Exception {
        return fileSystemService.openWorkspace(agent);
    }

    @Override
    public void clear() throws Exception {
        fileSystemService.clearWorkspace(agent);
    }

    /**
     * With the quotas of the owner; the default limits when they can't be read.
     */
    @Override
    public WorkspaceLimits limits() {
        try {
            return fileSystemService.getLimits(agent);
        } catch (Exception e) {
            return fileSystemService.getLimits();
        }
    }

}
