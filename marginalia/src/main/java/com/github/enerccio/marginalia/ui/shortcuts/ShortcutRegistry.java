package com.github.enerccio.marginalia.ui.shortcuts;

import com.github.enerccio.marginalia.loc.L;

import java.util.*;

/**
 * The actions that can be bound to a key: the application's own and the ones extensions register (and unregister in
 * {@code onExtensionUnload}). The key bindings table of the user settings lists them in the order of registration.
 */
public final class ShortcutRegistry {

    private static final Map<String, ShortcutAction> ACTIONS = new LinkedHashMap<>();

    static {
        register(new ShortcutAction(ShortcutActions.STORY_NEW_TURN, loc -> loc.getValue(L.LABEL_OPEN_NEW_TURN_FORM), "+", null));
        register(new ShortcutAction(ShortcutActions.STORY_GENERATE, loc -> loc.getValue(L.LABEL_GENERATE_FROM_NEW_TURN_FORM), "Ctrl+Enter", null));
        register(new ShortcutAction(ShortcutActions.STORY_STOP, loc -> loc.getValue(L.LABEL_STOP), "Ctrl+.", null));
        register(new ShortcutAction(ShortcutActions.STORY_SWIPE, loc -> loc.getValue(L.LABEL_SWIPE), "s", null));
        register(new ShortcutAction(ShortcutActions.STORY_REGENERATE, loc -> loc.getValue(L.LABEL_REGENERATE), "r", null));
        register(new ShortcutAction(ShortcutActions.STORY_EDIT, loc -> loc.getValue(L.LABEL_EDIT_LAST_PART), "e", null));
        register(new ShortcutAction(ShortcutActions.STORY_SAVE_EDIT, loc -> loc.getValue(L.LABEL_SAVE_EDITED_PART), "Ctrl+s", null));
    }

    private ShortcutRegistry() {
    }

    /**
     * Adds the action; one with the same id is replaced.
     */
    public static synchronized void register(ShortcutAction action) {
        ACTIONS.put(action.getId(), action);
    }

    public static synchronized void unregister(String id) {
        ACTIONS.remove(id);
    }

    public static synchronized ShortcutAction get(String id) {
        return ACTIONS.get(id);
    }

    /**
     * A copy of the registered actions in the order of registration.
     */
    public static synchronized List<ShortcutAction> getActions() {
        return new ArrayList<>(ACTIONS.values());
    }

    public static synchronized Collection<String> getIds() {
        return new ArrayList<>(ACTIONS.keySet());
    }
}
