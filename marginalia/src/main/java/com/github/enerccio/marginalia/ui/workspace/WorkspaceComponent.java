package com.github.enerccio.marginalia.ui.workspace;

import com.github.enerccio.marginalia.ui.Restorable;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;

/**
 * A part of the workspace (a tab). A part with something to remember between sessions overrides the {@link Restorable}
 * methods, the others have no state.
 */
public interface WorkspaceComponent extends Restorable {

    Component create() throws Exception;
    void refresh() throws Exception;
    void onTabSwitched() throws Exception;
    void onTabClosed() throws Exception;

    @Override
    default JsonObject storeState() {
        return new JsonObject();
    }

    @Override
    default void restoreState(JsonObject self) {
    }

}
