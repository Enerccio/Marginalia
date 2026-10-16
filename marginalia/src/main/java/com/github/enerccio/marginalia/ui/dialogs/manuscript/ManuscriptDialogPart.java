package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.ui.Restorable;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import org.vaadin.firitin.layouts.VTabSheet;

/**
 * A tab of the {@link com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog}. A tab with something to remember
 * between sessions overrides the {@link Restorable} methods, the others have no state.
 */
public interface ManuscriptDialogPart extends Restorable, LLMInteract {

    Component create(VTabSheet container) throws Exception;

    void setFrozen(boolean frozen);

    void load(Manuscript manuscript);

    void onTabLeave() throws Exception;

    void onTabEnter() throws Exception;

    @Override
    default JsonObject storeState() {
        return new JsonObject();
    }

    @Override
    default void restoreState(JsonObject self) {
    }

}
