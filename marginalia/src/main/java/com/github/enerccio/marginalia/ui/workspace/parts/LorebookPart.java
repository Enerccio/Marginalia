package com.github.enerccio.marginalia.ui.workspace.parts;

import com.github.enerccio.marginalia.domain.traits.APIReturn;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.domain.traits.PublicAPI;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.LorebookView;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.vaadin.flow.component.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
@Extendable
public class LorebookPart implements WorkspaceComponent {

    @Autowired
    private Localization loc;

    private final Workspace workspace;
    private LorebookView lorebookView;

    public LorebookPart(Workspace workspace) {
        this.workspace = workspace;
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = String.class))
    public String getName() {
        return loc.getValue(L.LABEL_LOREBOOKS);
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = Component.class))
    @Override
    public Component create() throws Exception {
        lorebookView = new LorebookView();
        return lorebookView.create();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void refresh() throws Exception {
        if (lorebookView != null) {
            lorebookView.refresh();
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void onTabSwitched() throws Exception {
        refresh();
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void onTabClosed() throws Exception {

    }

}