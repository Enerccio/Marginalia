package com.github.enerccio.marginalia.ui.workspace;

import com.github.enerccio.marginalia.ui.dialogs.ManuscriptWindow;

/**
 * The book list of the workspace: {@link com.github.enerccio.marginalia.ui.workspace.parts.ManuscriptPart} on the
 * desktop, {@link com.github.enerccio.marginalia.ui.workspace.phone.parts.PhoneManuscriptPart} on phones. It opens the
 * book windows, so it knows which one is open.
 */
public interface ManuscriptSection extends WorkspaceComponent {

    /**
     * The window of the book that is open, null when the user has none open.
     */
    ManuscriptWindow getOpenWindow();

}
