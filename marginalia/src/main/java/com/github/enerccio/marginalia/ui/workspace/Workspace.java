package com.github.enerccio.marginalia.ui.workspace;

import com.github.enerccio.marginalia.ui.dialogs.ManuscriptWindow;

/**
 * The top-level layout shown after login. It hosts the parts (manuscripts, lorebooks, settings, ...) and the
 * account actions (admin, change password, logout). Its {@link Restorable} state is the selected part and the state of
 * the parts; the autosave keeps it in the user settings, see {@link #autosave()}.
 */
public interface Workspace extends BaseWorkspace {

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
}
