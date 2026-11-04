package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;

/**
 * A part of the story as the helpers of this package see it. The desktop and the phone story tab each have their own
 * card component (a private inner class, so extensions can decorate them separately) implementing this.
 */
public interface StoryCard {

    ChatMessage getMessage();

    boolean isReasoningOpened();

    void setReasoningOpened(boolean opened);

    void setLastMessage(boolean isLast);

    void setFrozen(boolean frozen);

    boolean isEditing();

    void autosaveAndSwapToMarkdown();

    /**
     * Saves the edit like {@link #autosaveAndSwapToMarkdown()}, after asking the author what to do when the new text would
     * make summaries outdated. For the save the author asked for; the automatic saves never ask.
     */
    void saveEdit();

    void updateReasoning(String reasoningText);

    void updateResponse(String responseText);

    /**
     * Draws the response of the message again (the marks of a search changed).
     */
    void renderResponse();

    void updateMetrics(ChatMessage msg);

    /**
     * Clears the text of the card, a regeneration of the part streams into it again.
     */
    void restartStreaming(ChatMessage msg);

    /**
     * Whether the card is the one of the last part of the story.
     */
    boolean isLast();

    // --- what the menu items of a part do; the menu shows only what makes sense for the card ---

    /**
     * Opens the editor of the text, or saves the edit when it is open.
     */
    void toggleEdit();

    void discardEdit();

    void regenerate();

    void swipe();

    /**
     * Has the model write the part again, see the redraft dialog.
     */
    void redraft();

    /**
     * Makes a note about the text selected in the part.
     */
    void annotateSelection();

    /**
     * Makes a note about the whole part.
     */
    void annotatePart();

    /**
     * Has the model propose notes for the part.
     */
    void annotateWithAi();

    /**
     * Has the model write the passage selected in the part again.
     */
    void redraftSelection();

    /**
     * Copies the text selected in the part to the clipboard.
     */
    void copySelection();

    /**
     * Finds out what the author selected in the part and gives the action its range, {@code [start, end)} in the stored text of
     * the part ({@code getMessage().getResponse()}); the right click on the text remembers the selection for the menu item that
     * follows. When the part is being edited the edit is saved first. Tells the author when nothing is selected or the selection is
     * not in the text, and then the action is not called. The action runs later, on the page, not before this method returns.
     */
    void withSelection(java.util.function.Consumer<int[]> action);

    /**
     * Opens the version history of the part, see {@link PartHistoryDialog}. The card shows the text the author chose.
     */
    void openHistory();

    /**
     * How many versions of the part the history has now (not counting the ones in the trash); the menu shows it.
     */
    long getHistoryCount();

    void branch();

    void mergeIntoPrevious();

    void showPrompt();

    /**
     * Opens the dialog that compares the prompt of the part with an earlier one (why a prompt cache was not reused).
     */
    void comparePrompt();

    void showTokenUsage();

    /**
     * Shows the lorebook entries the prompt of the part used.
     */
    void showLoreUsage();

    void openImagesDialog();

    /**
     * The images of the part were changed from outside the card (an extension generated one): the card takes the saved part and
     * shows its images.
     */
    void imagesChanged(ChatMessage updated);

    /**
     * Shows the part with its images placed as the viewer places them (the editor lists them after the text); the text is the one in
     * the editor when the part is being edited.
     */
    void openImagePreview();

    /**
     * Opens the dialog that shows or generates the summary of the part.
     */
    void openSummaryDialog();

    void confirmDeleteSummary();

    /**
     * Asks, then deletes the part (what followed it continues from its parent).
     */
    void confirmDelete();

    /**
     * Shows or hides the check box of the mass delete and sets whether the part is ticked.
     */
    void showMassDelete(boolean shown, boolean selected);

}
