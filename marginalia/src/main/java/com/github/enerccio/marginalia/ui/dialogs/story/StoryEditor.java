package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;

/**
 * What the items of the settings menu of the story (and the shortcuts of the menus) can do with the story tab. The desktop and the
 * phone story tab each implement it, the menu items of the {@link com.github.enerccio.marginalia.ui.menu.StoryMenuRegistry} call it.
 * The methods that only the desktop tab has are the ones of the items that are not shown on the phone: they do nothing there.
 */
public interface StoryEditor {

    /**
     * The book the story tab shows now, null while it shows none. The tab loads other books into the same instance: read it when an action
     * runs, do not keep it.
     */
    Manuscript getCurrentManuscript();

    /**
     * Turns the styles of the book (its fonts and colours) on or off for the story.
     */
    void toggleBookStyles();

    /**
     * Opens the search and replace of the book.
     */
    void openSearchReplaceDialog();

    /**
     * Asks for a zip archive of Markdown files to add as parts to the end of the story.
     */
    void openArchiveImportDialog();

    /**
     * Shows the tokens the story takes in the next prompt.
     */
    void openBranchTokensDialog();

    /**
     * Shows what was done to the book day by day: words, tokens, time and size.
     */
    void openStatisticsDialog();

    /**
     * Opens the dialog that lets a model annotate a range of the parts of the story, the story is shown again when annotations were added.
     */
    void openAnnotateRangeDialog();

    /**
     * Opens the settings of the automatic annotation of the book: the model, what to look for, and what happens after a part is generated.
     */
    void openAutoAnnotateDialog();

    /**
     * Whether the book annotates the parts it generates by itself.
     */
    boolean isAutoAnnotate();

    /**
     * Opens the export of the story.
     */
    void openExportDialog();

    /**
     * Turns the mass delete on or off: while it is on the parts have a check box and the selected ones are deleted together.
     */
    void toggleMassDelete();

    /**
     * Whether the mass delete is on.
     */
    boolean isMassDelete();

    /**
     * Whether the text of the parts that is still the model's is highlighted (desktop only).
     */
    default boolean isShowGeneratedDiff() {
        return false;
    }

    /**
     * Highlights the text of the parts that is still the model's, or not (desktop only).
     */
    default void setShowGeneratedDiff(boolean show) {
    }

    /**
     * Whether the notes of the annotations are in the margin of the text (desktop only).
     */
    default boolean isAnnotationsInMargin() {
        return false;
    }

    /**
     * Puts the notes of the annotations into the margin of the text or into a popover (desktop only).
     */
    default void setAnnotationsInMargin(boolean inMargin) {
    }

    /**
     * Asks, then puts the tabs of the story editor back the way a new book has them (desktop only).
     */
    default void confirmRestoreDefaultLayout() {
    }

    /**
     * Saves the arrangement of the tabs as the default of the user: the books that have none of their own open with it
     * (desktop only).
     */
    default void saveLayoutAsDefault() {
    }
}
