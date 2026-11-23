package com.github.enerccio.marginalia.ui;

import com.google.gson.JsonObject;

/**
 * A piece of the UI whose state is remembered between sessions (the workspace autosave): the selected tab, the filters
 * of the book list, the open book window and so on. A class stores what it needs into a new object and asks the
 * children it has (parts, views) for theirs; it restores itself and passes the nested objects on. See
 * {@link RestorableState} for reading the values.
 * <p>
 * The state is stored as the user left it, so it can name things that are gone by the time it is restored. Restoring
 * resolves entities by their uuid when it runs and skips what it does not find (a book that was deleted is not opened,
 * a tag that was deleted is not filtered), it never fails because of an old state.
 */
public interface Restorable {

    /**
     * @return the state of this component and of its children, never null; empty when there is nothing to remember
     */
    JsonObject storeState();

    /**
     * @param self what {@link #storeState()} returned, possibly from an older version: unknown or missing keys are
     *             ignored
     */
    void restoreState(JsonObject self);

}
