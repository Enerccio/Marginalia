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

    /** Copies what is marked in the active pane of the Organise window into the other pane. */
    public static final String ORGANISE_COPY = "organise.copy";
    /** Moves what is marked in the active pane of the Organise window into the other pane. */
    public static final String ORGANISE_MOVE = "organise.move";

    /** Changes the active pane of the Organise window, like Left / Right do. */
    public static final String ORGANISE_SWITCH_PANE = "organise.switchPane";

    /** Makes a folder in the folder of the active pane of the Organise window. */
    public static final String ORGANISE_NEW_FOLDER = "organise.newFolder";
    /** Renames the folder or book under the cursor of the Organise window. */
    public static final String ORGANISE_RENAME = "organise.rename";
    /** Deletes the folder or book under the cursor of the Organise window. */
    public static final String ORGANISE_DELETE = "organise.delete";
    /** Opens the settings of the folder under the cursor of the Organise window. */
    public static final String ORGANISE_FOLDER_SETTINGS = "organise.folderSettings";

    /** Opens the mass change of the model and protocol for the marked books of the Organise window (or the folder of the active pane). */
    public static final String ORGANISE_MODEL_PROTOCOL = "organise.modelProtocol";
    /** Opens the mass change of the language for the marked books of the Organise window (or the folder of the active pane). */
    public static final String ORGANISE_LANGUAGE = "organise.language";
    /** Opens the tagging of the marked books of the Organise window (or the books of the folder of the active pane). */
    public static final String ORGANISE_TAG_BOOKS = "organise.tagBooks";
    /** Opens the advanced search in the folder of the active pane of the Organise window. */
    public static final String ORGANISE_SEARCH = "organise.search";
    /** Closes the Organise window and shows the folder of its active pane in the list of books. */
    public static final String ORGANISE_SHOW_IN_LIST = "organise.showInList";

    private ShortcutActions() {
    }
}
