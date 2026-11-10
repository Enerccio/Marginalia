package com.github.enerccio.marginalia.ui.dialogs.bookimport;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.Component;

/**
 * One thing a new book can be imported from, as the {@link BookImportDialog} shows it: what to upload, the options that
 * belong to it (none for an archive of {@code .md} files), what was read of the uploads and how it is stored in the book.
 * The state is the one of a dialog, a source is made for each dialog and used on the UI thread.
 */
abstract class BookImportSource {

    protected final Localization loc;
    private Runnable onChange = () -> {
    };

    protected BookImportSource(Localization loc) {
        this.loc = loc;
    }

    /**
     * @return the label of the source in the selector
     */
    abstract L label();

    /**
     * @return what the user is asked to upload
     */
    abstract L description();

    abstract String[] fileTypes();

    /**
     * @return true when any number of files can be uploaded and all of them are used, false when a new upload replaces the
     * one before it
     */
    boolean multiple() {
        return false;
    }

    /**
     * @return true when the parts can be put into a book that has a story already, at a place of it; false for what is a tree of
     * branches (chats), which is made for a new book
     */
    boolean insertable() {
        return true;
    }

    /**
     * @return the controls of the options of the source, null when it has none
     */
    Component options() {
        return null;
    }

    /**
     * Reads an uploaded file.
     *
     * @throws IllegalArgumentException when the file is not of the kind, what was read before stays
     */
    abstract void read(byte[] data, String fileName);

    /**
     * @return the warning for a file that could not be read
     */
    abstract String invalid(String fileName);

    /**
     * Forgets what was uploaded (the options stay).
     */
    abstract void reset();

    /**
     * @return what was read, in words for the user; empty when nothing was
     */
    abstract String status();

    /**
     * @return true when there is something to import
     */
    abstract boolean ready();

    /**
     * @return the name the book gets, empty when the uploads give none
     */
    abstract String suggestedName();

    /**
     * Takes what was read and the options as they are now.
     *
     * @throws IllegalArgumentException with the text for the user when there is nothing to import
     */
    abstract BookImportDialog.Run prepare();

    /**
     * What the dialog does when the options, and with them the status, change.
     */
    final void onChange(Runnable onChange) {
        this.onChange = onChange;
    }

    protected final void changed() {
        onChange.run();
    }
}
