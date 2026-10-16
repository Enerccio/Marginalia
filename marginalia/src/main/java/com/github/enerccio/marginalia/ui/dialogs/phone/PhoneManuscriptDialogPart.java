package com.github.enerccio.marginalia.ui.dialogs.phone;

import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.ui.Restorable;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;

/**
 * A tab of the {@link PhoneManuscriptDialog}. Unlike the desktop parts they don't add themselves to a tab sheet, the
 * dialog owns the (scrolling) tab bar and only asks the part for its content. A tab with something to remember between
 * sessions overrides the {@link Restorable} methods, the others have no state.
 */
public interface PhoneManuscriptDialogPart extends Restorable, LLMInteract {

    Component create() throws Exception;

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
