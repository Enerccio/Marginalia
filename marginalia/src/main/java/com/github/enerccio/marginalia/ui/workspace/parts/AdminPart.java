package com.github.enerccio.marginalia.ui.workspace.parts;

import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;

public class AdminPart implements WorkspaceComponent {

    private final Workspace workspace;

    public AdminPart(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public Component create() throws Exception {
        return new Div();
    }

    @Override
    public void refresh() throws Exception {

    }

    @Override
    public void onTabSwitched() throws Exception {

    }

    @Override
    public void onTabClosed() throws Exception {

    }
}
