package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.components.SummaryNode;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.repository.SummaryRepository;

import java.util.Collection;
import java.util.List;

public interface SummaryService extends ExtendableService<Summary, SummaryRepository> {

    CancellationToken createSummary(Manuscript manuscript, ChatMessage from, AsyncCallback callback) throws Exception;

    /**
     * Merges the summaries from {@code from} (newest) to {@code to} (oldest) of the branch into one meta summary
     * stored on {@code from}, where it replaces the summary {@code from} had. Summaries the generation does not use
     * (already merged into another meta summary) are not merged again. {@code to} is rounded up to the end of the
     * summary block it is in.
     *
     * @return null when the range is not valid or there is nothing to merge
     */
    CancellationToken createMetaSummary(Manuscript manuscript, ChatMessage from, ChatMessage to, AsyncCallback callback) throws Exception;

    /**
     * Puts the finished meta summary on the message, the summary the message had is kept inside the meta summary.
     * The meta summary takes over uuid of the replaced summary, so meta summaries pointing to it still find it.
     */
    Summary attachMetaSummary(Summary metaSummary, ChatMessage message) throws Exception;

    /**
     * Soft deletes the summaries of all messages of the manuscript, for the delete of the manuscript.
     */
    void softDeleteForManuscript(Manuscript manuscript) throws Exception;

    /**
     * Soft deletes the summaries of the messages with the ids, for the delete of the messages.
     */
    void softDeleteForMessages(Collection<Long> messageIds) throws Exception;

    /**
     * Removes the summary of the message.
     *
     * @param unwind for a meta summary restores the summary it replaced instead of leaving the message without summary
     * @return the updated message
     */
    ChatMessage removeSummary(ChatMessage message, boolean unwind) throws Exception;

    Summary copySummary(Summary summary) throws Exception;

    /**
     * Says that the summaries of the branch still describe the story: works out their hash again from the story as it is
     * now, the way a search and replace does for the texts it changed. For the author (or an agent) who edited a part
     * and knows that what the summary says is still true.
     *
     * @param newestFirst the branch, from the newest part to the root
     * @param only        the part whose summary is revalidated, all the summaries of the branch when null
     * @return the summaries that were outdated and are valid now, oldest first
     * @throws ManuscriptLockedException when an agent works on the book
     */
    List<SummaryBlock> revalidate(Manuscript manuscript, List<ChatMessage> newestFirst, ChatMessage only) throws Exception;

    /**
     * Changes the text of the summary, its token count is counted again.
     * <p>
     * Only for summaries that no meta summary stands in for: the text of those is a part of the hash of the meta summary.
     */
    Summary updateSummaryText(Manuscript manuscript, Summary summary, String text) throws Exception;

    /**
     * The summaries of the branch as {@link #collectBlocks} finds them, with the summaries a meta summary stands in for
     * (the one it replaced on its part and the ones further down the branch) as children, oldest first.
     *
     * @param newestFirst branch from the newest message to the root
     * @return top level nodes, oldest first
     */
    List<SummaryNode> collectTree(List<ChatMessage> newestFirst) throws Exception;

    /**
     * {@link #collectTree(List)} of the active branch of the book, empty when the book has no story.
     */
    List<SummaryNode> collectTree(Manuscript manuscript) throws Exception;

    /**
     * Splits the branch into the summary blocks generation uses, a block starts at message with summary and lasts to
     * the next block. Summaries a meta summary stands in for are not blocks, the messages and summaries between are part
     * of the meta summary block (and its hash).
     *
     * @param newestFirst branch from the newest message to the root
     * @return blocks newest first, messages newer than the first summary are not in any block
     */
    List<SummaryBlock> collectBlocks(List<ChatMessage> newestFirst) throws Exception;

    /**
     * The first block of a branch that is given from the message with the summary down to the root, with the hash the
     * block would have if the story had the texts of {@code texts}. Gives the hash a summary needs after a change of
     * the story (a search and replace) without walking the whole branch.
     *
     * @param newestFirst the message with the summary and its ancestors, newest first
     * @return null when the first message has no summary
     */
    SummaryBlock collectFirstBlock(java.util.Iterator<ChatMessage> newestFirst, BlockTexts texts) throws Exception;

    /**
     * The texts the hash of a summary block is made of.
     */
    interface BlockTexts {

        /**
         * The story as it is saved.
         */
        BlockTexts CURRENT = new BlockTexts() {
            @Override
            public String response(ChatMessage message) {
                return message.getResponse();
            }

            @Override
            public String summary(Summary summary) {
                return summary.getSummary();
            }
        };

        String response(ChatMessage message);

        String summary(Summary summary);

    }

    /**
     * @param hash hash of the block as it is now, compared to {@link Summary#getSummaryMessageHash()} it tells whether
     *             the summary still describes the story
     */
    record SummaryBlock(ChatMessage head, Summary summary, String hash) {

        public boolean isValid() {
            return hash.equals(summary.getSummaryMessageHash());
        }

    }

    interface AsyncCallback {

        void onSummaryProgress(String reasoning, String summary) throws Exception;
        void onSummaryFinished(Summary summary) throws Exception;
        void onSummaryTerminated() throws Exception;
        void onError(Throwable throwable) throws Exception;

    }

}
