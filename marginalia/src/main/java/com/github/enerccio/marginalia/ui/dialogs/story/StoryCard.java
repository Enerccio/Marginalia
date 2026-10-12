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

    void updateReasoning(String reasoningText);

    void updateResponse(String responseText);

    void updateMetrics(ChatMessage msg);

    /**
     * Clears the text of the card, a regeneration of the part streams into it again.
     */
    void restartStreaming(ChatMessage msg);

}
