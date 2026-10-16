package com.github.enerccio.marginalia.ui.workspace;

import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.Restorable;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptWindow;
import com.vaadin.flow.component.Component;

/**
 * The top-level layout shown after login. It hosts the parts (manuscripts, lorebooks, settings, ...) and the
 * account actions (admin, change password, logout). Its {@link Restorable} state is the selected part and the state of
 * the parts; the autosave keeps it in the user settings, see {@link #autosave()}.
 */
public interface Workspace extends Restorable, LLMInteract {

    Component create() throws Exception;

    /**
     * Refreshes all parts of the workspace.
     */
    void refresh() throws Exception;

    /**
     * Locks the navigation (other tabs, admin and logout) while the active part has unsaved changes.
     */
    void setNavigationLocked(boolean locked);

    boolean isNavigationLocked();

    /**
     * Stores the state of the workspace into the user settings. The parts call it after the user did something worth
     * coming back to (navigated to a part, opened a book, changed a filter); it does nothing while the old state is
     * being restored and when the state did not change.
     */
    void autosave();

    /**
     * Restores the state stored by {@link #autosave()} (after {@link #create()}) and turns the autosave on.
     */
    void restoreAutosaved();

    /**
     * The book list, which knows the book that is open.
     */
    ManuscriptSection getManuscriptSection();

    /**
     * The window of the book that is open, null when the user has none open; the same as
     * {@code getManuscriptSection().getOpenWindow()}.
     */
    default ManuscriptWindow getOpenManuscriptWindow() {
        ManuscriptSection section = getManuscriptSection();
        return section == null ? null : section.getOpenWindow();
    }

    Runnable getOnLogout();

    /**
     * Sets the logout action; the logout button is only shown when it is set before {@link #create()}.
     */
    void setOnLogout(Runnable onLogout);

}
