package com.github.enerccio.marginalia.ui.shortcuts;

/**
 * Ids of the actions of the application, the keys of the key bindings.
 */
public final class ShortcutActions {

    /** Opens the new turn popover of the story tab. */
    public static final String STORY_NEW_TURN = "story.newTurn";
    /** Generates from the new turn popover (while it is open). */
    public static final String STORY_GENERATE = "story.generate";
    /** Stops the generation that runs. */
    public static final String STORY_STOP = "story.stop";
    /** Generates another version of the last part. */
    public static final String STORY_SWIPE = "story.swipe";
    /** Generates the last part again. */
    public static final String STORY_REGENERATE = "story.regenerate";
    /** Opens the editor of the last part. */
    public static final String STORY_EDIT = "story.edit";
    /** Saves the part that is being edited (works while the cursor is in its editor). */
    public static final String STORY_SAVE_EDIT = "story.saveEdit";

    /** Puts the cursor into the command bar of the story tab. */
    public static final String STORY_COMMAND_BAR = "story.commandBar";
    /** Opens the outline and puts the cursor into the search of the branch. */
    public static final String STORY_SEARCH = "story.search";
    /** Makes a note about the text selected in a part. */
    public static final String STORY_ANNOTATE_SELECTION = "story.annotateSelection";
    /** Opens the search and replace of the book. */
    public static final String STORY_SEARCH_REPLACE = "story.searchReplace";
    /** Shows the next tab of the story editor (all sheets in the order of the layout). */
    public static final String DOCK_NEXT_TAB = "dock.nextTab";
    /** Shows the previous tab of the story editor. */
    public static final String DOCK_PREVIOUS_TAB = "dock.previousTab";

    private ShortcutActions() {
    }
}
