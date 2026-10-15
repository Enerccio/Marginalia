package com.github.enerccio.marginalia.ui.workspace.phone.parts;

import com.github.enerccio.marginalia.domain.traits.APIParameter;
import com.github.enerccio.marginalia.domain.traits.APIReturn;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.domain.traits.PublicAPI;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.PhoneLorebookView;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The lorebook tab for phones, hosting the reduced {@link PhoneLorebookView} instead of the full desktop editor.
 */
@Configurable
@Extendable
public class PhoneLorebookPart implements WorkspaceComponent {

    @Autowired
    private Localization loc;

    private final Workspace workspace;
    private PhoneLorebookView lorebookView;

    public PhoneLorebookPart(Workspace workspace) {
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
        lorebookView = new PhoneLorebookView();
        lorebookView.setOnStateChange(() -> {
            if (workspace != null) {
                workspace.autosave();
            }
        });
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

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = JsonObject.class))
    @Override
    public JsonObject storeState() {
        return lorebookView == null ? new JsonObject() : lorebookView.storeState();
    }

    @PublicAPI(from = "1.3.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "self", type = JsonObject.class)})
    @Override
    public void restoreState(JsonObject self) {
        if (lorebookView != null) {
            lorebookView.restoreState(self);
        }
    }

}
