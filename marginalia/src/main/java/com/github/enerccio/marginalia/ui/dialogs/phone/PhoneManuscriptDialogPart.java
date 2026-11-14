package com.github.enerccio.marginalia.ui.dialogs.phone;

import com.github.enerccio.marginalia.ui.dialogs.manuscript.GenerationHold;
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

    /**
     * A generation of the book holds the window, null when none runs (or it ended). While it holds the whole book ({@link
     * GenerationHold#isWhole()}) a tab waits, as with {@link #setFrozen setFrozen(true)}; from the inference on only the part it writes
     * and the branch above it are held, and a tab that has nothing to do with them is free. By default a tab waits for the whole
     * generation.
     */
    default void setHold(GenerationHold hold) {
        setFrozen(hold != null);
    }

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
