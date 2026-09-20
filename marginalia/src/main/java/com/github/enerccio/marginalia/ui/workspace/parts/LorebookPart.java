package com.github.enerccio.marginalia.ui.workspace.parts;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.LorebookView;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.vaadin.flow.component.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class LorebookPart implements WorkspaceComponent {

    @Autowired
    private Localization loc;

    private final Workspace workspace;
    private LorebookView lorebookView;

    public LorebookPart() {
        this(null);
    }

    public LorebookPart(Workspace workspace) {
        this.workspace = workspace;
    }

    public String getName() {
        return loc.getValue(L.LABEL_LOREBOOKS);
    }

    @Override
    public Component create() throws Exception {
        lorebookView = new LorebookView();
        return lorebookView.create();
    }

    @Override
    public void refresh() throws Exception {
        if (lorebookView != null) {
            lorebookView.refresh();
        }
    }

    @Override
    public void onTabSwitched() throws Exception {
        refresh();
    }

    @Override
    public void onTabClosed() throws Exception {

    }

}