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

    private ShortcutActions() {
    }
}
