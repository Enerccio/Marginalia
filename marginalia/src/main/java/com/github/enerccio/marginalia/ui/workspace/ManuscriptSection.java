package com.github.enerccio.marginalia.ui.workspace;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;

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

    /**
     * Opens the book in this page for code that is not the book list (a click in the panel of the runs), the way the list opens it (the
     * agent lock, the question when it is open in another page), and then runs {@code then} with its window. When the book is open here
     * already, {@code then} runs at once. When another book is open here, the author is told to close it first and nothing happens: the
     * book window is strict and its unsaved state is not ours to drop.
     *
     * @return false when another book is open here
     */
    boolean openBook(Manuscript book, java.util.function.Consumer<ManuscriptWindow> then);

}
