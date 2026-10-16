package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.Restorable;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryHost;
import com.vaadin.flow.component.dialog.Dialog;

/**
 * The window of an open book: the {@link ManuscriptDialog} on the desktop, the
 * {@link com.github.enerccio.marginalia.ui.dialogs.phone.PhoneManuscriptDialog} on phones. Code that works with "the
 * open book" (commands, extensions) uses this interface and does not care which of them the user has; the workspace
 * hands it out, see {@link com.github.enerccio.marginalia.ui.workspace.ManuscriptSection#getOpenWindow()}.
 * <p>
 * It is a {@link StoryHost}: {@link #refreshManuscript()} and {@link #save()} replace the book the window holds with the
 * one from the database.
 */
public interface ManuscriptWindow extends StoryHost, Restorable, LLMInteract {

    void create() throws Exception;

    void open();

    void close();

    boolean isOpened();

    /**
     * The book as the window holds it now. It is the window's own copy: it is replaced by {@link #refreshManuscript()}
     * and {@link #save()}, so fetch the book from the {@code ManuscriptService} when the freshest data matters.
     */
    Manuscript getManuscript();

    /**
     * The model of the book, null when it has none.
     */
    AI getAi();

    /**
     * Switches to the story tab.
     */
    void selectStoryPart();

    /**
     * Switches to the story tab and scrolls to the part. The part has to be on the active branch.
     */
    void showMessage(Long messageId);

    Runnable getOnClose();

    void setOnClose(Runnable onClose);

    /**
     * Sets what is called after the user navigated or changed what {@link #storeState()} stores.
     */
    void setOnStateChange(Runnable onStateChange);

    void stateChanged();

    /**
     * The window as the Vaadin dialog it is, to attach to or to reach the UI it lives in.
     */
    default Dialog asDialog() {
        return (Dialog) this;
    }

}
