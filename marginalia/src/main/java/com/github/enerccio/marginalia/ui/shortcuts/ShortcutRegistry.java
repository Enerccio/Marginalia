package com.github.enerccio.marginalia.ui.shortcuts;

import com.github.enerccio.marginalia.loc.L;

import java.util.*;

/**
 * The actions that can be bound to a key: the application's own (the ones of the menu items are added by the
 * {@link com.github.enerccio.marginalia.ui.menu.StoryMenuRegistry} when the application starts) and the ones extensions register (and unregister in
 * {@code onExtensionUnload}). The key bindings table of the user settings lists them in the order of registration.
 */
public final class ShortcutRegistry {

    private static final Map<String, ShortcutAction> ACTIONS = new LinkedHashMap<>();

    static {
        register(new ShortcutAction(ShortcutActions.STORY_NEW_TURN, loc -> loc.getValue(L.LABEL_OPEN_NEW_TURN_FORM), "+", null));
        register(new ShortcutAction(ShortcutActions.STORY_GENERATE, loc -> loc.getValue(L.LABEL_GENERATE_FROM_NEW_TURN_FORM), "Ctrl+Enter", null));
        register(new ShortcutAction(ShortcutActions.STORY_STOP, loc -> loc.getValue(L.LABEL_STOP), "Ctrl+.", null));
        register(new ShortcutAction(ShortcutActions.STORY_SAVE_EDIT, loc -> loc.getValue(L.LABEL_SAVE_EDITED_PART), "Ctrl+s", null));
        register(new ShortcutAction(ShortcutActions.STORY_COMMAND_BAR, loc -> loc.getValue(L.LABEL_FOCUS_COMMAND_BAR), "/", null));
        register(new ShortcutAction(ShortcutActions.STORY_SEARCH, loc -> loc.getValue(L.LABEL_SEARCH_THIS_BRANCH), "Ctrl+Shift+f", null));
        register(new ShortcutAction(ShortcutActions.DOCK_NEXT_TAB, loc -> loc.getValue(L.LABEL_NEXT_TAB), "Alt+PageDown", null));
        register(new ShortcutAction(ShortcutActions.DOCK_PREVIOUS_TAB, loc -> loc.getValue(L.LABEL_PREVIOUS_TAB), "Alt+PageUp", null));
        // no function keys (a Mac keeps them for the media keys, the browsers for themselves: F5 reloads, F7 is caret browsing) and no Alt
        // with a letter (it types a character on a Mac): plain keys, which the Organise window gets only while no input has the focus
        register(new ShortcutAction(ShortcutActions.ORGANISE_COPY, loc -> loc.getValue(L.LABEL_ORGANISE) + ": " + loc.getValue(L.LABEL_COPY), "c", null));
        register(new ShortcutAction(ShortcutActions.ORGANISE_MOVE, loc -> loc.getValue(L.LABEL_ORGANISE) + ": " + loc.getValue(L.LABEL_MOVE), "m", null));
        register(new ShortcutAction(ShortcutActions.ORGANISE_SWITCH_PANE, loc -> loc.getValue(L.LABEL_ORGANISE) + ": " + loc.getValue(L.LABEL_SWITCH_PANE), "Tab", null));
        // plain r is the regeneration of a story turn
        register(new ShortcutAction(ShortcutActions.ORGANISE_NEW_FOLDER, loc -> loc.getValue(L.LABEL_ORGANISE) + ": " + loc.getValue(L.LABEL_NEW_FOLDER), "n", null));
        register(new ShortcutAction(ShortcutActions.ORGANISE_RENAME, loc -> loc.getValue(L.LABEL_ORGANISE) + ": " + loc.getValue(L.LABEL_RENAME), "Shift+r", null));
        register(new ShortcutAction(ShortcutActions.ORGANISE_DELETE, loc -> loc.getValue(L.LABEL_ORGANISE) + ": " + loc.getValue(L.LABEL_DELETE), "Delete", null));
        register(new ShortcutAction(ShortcutActions.ORGANISE_MODEL_PROTOCOL, loc -> loc.getValue(L.LABEL_ORGANISE) + ": " + loc.getValue(L.LABEL_MASS_CHANGE_MODEL_PROTOCOL), "p", null));
        register(new ShortcutAction(ShortcutActions.ORGANISE_LANGUAGE, loc -> loc.getValue(L.LABEL_ORGANISE) + ": " + loc.getValue(L.LABEL_MASS_CHANGE_LANGUAGE), "l", null));
        register(new ShortcutAction(ShortcutActions.ORGANISE_TAG_BOOKS, loc -> loc.getValue(L.LABEL_ORGANISE) + ": " + loc.getValue(L.LABEL_TAG_BOOKS), "t", null));
        register(new ShortcutAction(ShortcutActions.ORGANISE_SEARCH, loc -> loc.getValue(L.LABEL_ORGANISE) + ": " + loc.getValue(L.LABEL_ADVANCED_SEARCH), "f", null));
        register(new ShortcutAction(ShortcutActions.ORGANISE_SHOW_IN_LIST, loc -> loc.getValue(L.LABEL_ORGANISE) + ": " + loc.getValue(L.LABEL_SHOW_FOLDER_IN_LIST), "o", null));
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
