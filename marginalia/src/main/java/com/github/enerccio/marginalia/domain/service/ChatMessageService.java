package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.components.BranchTokens;
import com.github.enerccio.marginalia.domain.components.PartMerge;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ChatMessageRepository;

import com.github.enerccio.marginalia.domain.service.SummaryService.SummaryBlock;
import com.github.enerccio.marginalia.domain.service.search.FulltextHit;
import com.github.enerccio.marginalia.domain.service.search.FulltextQuery;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ChatMessageService extends ExtendableService<ChatMessage, ChatMessageRepository> {

    ChatMessage getParent(ChatMessage node) throws Exception;

    ChatMessage createRoot(Manuscript manuscript, ChatMessage message) throws Exception;

    ChatMessage addChild(ChatMessage parent, ChatMessage child) throws Exception;

    List<ChatMessage> getBranchFromLeaf(ChatMessage leaf) throws Exception;

    List<ChatMessage> getSwipesForMessage(ChatMessage message) throws Exception;

    ChatMessage swipeTo(Manuscript manuscript, ChatMessage targetMessage) throws Exception;

    ChatMessage branch(Manuscript manuscript, ChatMessage branched) throws Exception;

    /**
     * Branches the part (a copy of it, with a copy of its summary, as its sibling) and makes the new part the active leaf of
     * the book, which is saved. What the Branch button does.
     *
     * @return the saved manuscript
     * @throws IllegalArgumentException when the part is not a part of the manuscript, or the manuscript is not the user's
     */
    Manuscript branchAndSwitch(Manuscript manuscript, ChatMessage branched) throws Exception;

    /**
     * Makes the end of the branch that goes through the part (the newest descendants) the active leaf of the book, which is
     * saved. What choosing another swipe does.
     *
     * @return the saved manuscript
     * @throws IllegalArgumentException when the part is not a part of the manuscript, or the manuscript is not the user's
     */
    Manuscript swipeAndSwitch(Manuscript manuscript, ChatMessage target) throws Exception;

    List<ChatMessage> getAllMessages(Manuscript manuscript) throws Exception;

    /**
     * Deletes for good every message of the manuscript (the soft deleted ones too) with their summaries, the children
     * before their parents. For the working copy of an agent, whose manuscript is deleted for good right after.
     */
    void hardDeleteForManuscript(Manuscript manuscript) throws Exception;

    /**
     * Full-text search over the parts of all branches of the book, oldest first.
     *
     * @see FulltextQuery
     */
    List<FulltextHit> searchFulltext(Manuscript manuscript, String query) throws Exception;

    /**
     * Which of the books have a part with every word of the query, the parts in the branch of the active part only or
     * in all branches. A book of another user is never found.
     *
     * @return the ids of the books found
     * @see FulltextQuery
     */
    Set<Long> findManuscriptsWithText(Collection<Long> manuscriptIds, String query, boolean allBranches) throws Exception;

    /**
     * The id and {@code _fulltext} of the parts of the book that are not deleted, oldest first.
     *
     * @param literal the text the {@code _fulltext} has to contain (the database does not tell the case of every
     *                letter apart, check again), null for all the parts
     */
    List<MessageFulltext> findFulltextsContaining(Manuscript manuscript, String literal) throws Exception;

    /**
     * The same for the summaries of the parts that are not deleted, with the id of the part.
     */
    List<SummaryFulltext> findSummaryFulltextsContaining(Manuscript manuscript, String literal) throws Exception;

    record MessageFulltext(Long messageId, String fulltext) {
    }

    record SummaryFulltext(Long messageId, Long summaryId, String fulltext) {
    }

    /**
     * The end of the branch to show to display the part: the active leaf when the part is on the active branch,
     * otherwise the nearest end of a branch below the part (the newer one when there are two). Null when the part
     * isn't a part of the book.
     */
    ChatMessage findLeafFor(Manuscript manuscript, ChatMessage target) throws Exception;

    /**
     * The size of the active branch of a book (what the list of books shows).
     */
    record BranchSize(long words, long tokens) {
    }

    /**
     * The size of the active branch of each of the books, with one query for all of them (the rows of a page of the list).
     * A book without parts has a size of 0, every id is a key.
     */
    Map<Long, BranchSize> getActiveBranchSizes(Collection<Long> manuscriptIds) throws Exception;

    int getBranchWordCount(ChatMessage leaf) throws Exception;

    long getBranchTokenCount(ChatMessage leaf) throws Exception;

    /**
     * The tokens the story of the branch ending in the leaf takes in a prompt without and with its summaries, see
     * {@link BranchTokens}.
     */
    BranchTokens getBranchTokens(ChatMessage leaf) throws Exception;

    int getTotalWordCount(Manuscript manuscript) throws Exception;

    int getTotalTokenCount(Manuscript manuscript) throws Exception;

    boolean hasAnyMessages(Manuscript manuscript) throws Exception;

    /**
     * Soft deletes all messages of the manuscript and their summaries, for the delete of the manuscript. One update
     * for all of them (a book has thousands of messages), the messages are not loaded.
     */
    void softDeleteForManuscript(Manuscript manuscript) throws Exception;

    Manuscript deleteNodeAndMigrateChildren(ChatMessage message, Manuscript manuscript, boolean hard) throws Exception;

    int countWords(String text);

    /**
     * Stores the text of a part the author (or an agent) edited: counts its tokens (with the tokenizer of the book's AI, a
     * generic one when it has none) and words, flags the part as edited and saves it.
     *
     * @return the saved part and the summaries the edit made outdated (the ones that described the story before and no
     * longer do, not the ones that were outdated already)
     * @throws IllegalArgumentException when the part is not a part of the manuscript, or the manuscript is not the user's
     */
    PartEdit updateResponse(Manuscript manuscript, ChatMessage message, String text) throws Exception;

    /**
     * Whether a summary describes the text of the part, so that an edit of the part can make it outdated: the part has a summary
     * itself, or a newer part of its branch has one that goes down to it.
     *
     * @throws IllegalArgumentException when the part is not a part of the manuscript, or the manuscript is not the user's
     */
    boolean isSummarized(Manuscript manuscript, ChatMessage message) throws Exception;

    /**
     * As {@link #updateResponse}, without the work of finding the summaries the edit made outdated (it reads the whole branch
     * of the part): for the code that changes many parts at once and does not tell about the summaries.
     *
     * @return the saved part
     * @throws IllegalArgumentException when the part is not a part of the manuscript, or the manuscript is not the user's
     */
    ChatMessage storeResponse(Manuscript manuscript, ChatMessage message, String text) throws Exception;

    /**
     * Adds a part the author (or an agent) wrote as a child of {@code parent}, or as the first part of an empty book when
     * the parent is null: counts its tokens and words and makes it the active leaf of the book, which is saved. The part is
     * not flagged as edited, nobody changed a text of the model.
     *
     * @param input what the turn the part stands for was made from; null for none
     * @throws IllegalArgumentException when the parent is not a part of the manuscript, the manuscript is not the user's,
     *                                  or there is no parent and the book has parts already
     */
    ChatMessage createPart(Manuscript manuscript, ChatMessage parent, String text, TurnInput input) throws Exception;

    /**
     * Stores a prepared part as a child of {@code parent}, or as one more first part of the book when the parent is null (the
     * book may have parts already): counts the tokens and words of its text, nothing else of it is changed, and the active leaf
     * of the book stays where it is. For the imports that build a whole tree of parts.
     *
     * @param part the part to store, with its text, instructions and the data kept on it; not stored yet
     * @return the stored part
     * @throws IllegalArgumentException when the parent is not a part of the manuscript, or the manuscript is not the user's
     */
    ChatMessage addPart(Manuscript manuscript, ChatMessage parent, ChatMessage part) throws Exception;

    /**
     * Inserts a part into the story, between a part and what follows it: the new part becomes the only child of {@code after},
     * and everything that followed {@code after} (all its continuations, the swipes of the next part with their own
     * continuations) follows the new part now. No part is deleted or made again, so their uuids, summaries and the data
     * kept on them stay; every part below the new one is one place deeper. As {@link #createPart}, the text is counted and
     * the part is not flagged as edited. The active leaf stays where it is, unless {@code after} was the leaf: the new
     * part is the end of the branch then, as with {@link #createPart}. The summaries of the parts below that covered
     * {@code after} are outdated, as they describe the story without the new part.
     *
     * @param after the part the new one comes after; null for a part before the whole story (the first parts of the story
     *              follow it then); the book may have no parts, then this is {@link #createPart} of the first part
     * @param input what the turn the part stands for was made from; null for none
     * @throws IllegalArgumentException when {@code after} is not a part of the manuscript or the manuscript is not the user's
     */
    ChatMessage insertPart(Manuscript manuscript, ChatMessage after, String text, TurnInput input) throws Exception;

    /**
     * What merging a part into the part before it would do, for the review of the author: the two parts, the prompt fields
     * of both combined without the start they share, the data kept on them value by value and the join of the two texts the
     * text asks for. Nothing is changed.
     *
     * @throws IllegalArgumentException when the part is not a part of the manuscript, or the manuscript is not the user's
     * @throws IllegalStateException    when the part cannot be merged, see {@link #mergeIntoPrevious}
     */
    MergePreview previewMergeIntoPrevious(Manuscript manuscript, ChatMessage part) throws Exception;

    /**
     * Appends the text of the part to the part before it and removes the part: the continuation of a text the model
     * stopped in the middle ("continue" is not reliable in chat completion). The previous part keeps its identity, summary
     * and generation record (prompt, model, times); the texts, original texts (so that the generated text is told from the
     * author's), reasoning, images and counts are put together, and what followed the removed part follows the previous
     * one now (the active leaf moves to it when the removed part was the leaf). The summaries that described the story with
     * the two parts are outdated by the change of the texts, as with an edit; the summary of the removed part is deleted.
     * The part is deleted like by <i>Delete</i>, the Trash restores it (but not the merge).
     *
     * @param input        the prompt fields of the merged turn
     * @param join         what stands between the two texts
     * @param fromPrevious the attributes ({@link PartMerge.AttributeChange#path()}) for which the value of the previous part is
     *                     taken instead of the value of the later part (which wins otherwise)
     * @param dropped      the attributes that are left out
     * @return the previous part as saved
     * @throws IllegalArgumentException when the part is not a part of the manuscript, or the manuscript is not the user's
     * @throws IllegalStateException    when the part is the first part of the book, or the part before it has other
     *                                  continuations (the swipes of the part): they would follow a text that already contains
     *                                  the part, delete them or make them a branch first
     */
    ChatMessage mergeIntoPrevious(Manuscript manuscript, ChatMessage part, TurnInput input, PartMerge.Join join,
                                  Set<List<String>> fromPrevious, Set<List<String>> dropped) throws Exception;

    /**
     * @param previous   the part the other is merged into
     * @param part       the part that is merged
     * @param input      the combined prompt fields
     * @param join       the join of the two texts
     * @param attributes the data kept on the parts, every value of both
     */
    record MergePreview(ChatMessage previous, ChatMessage part, TurnInput input, PartMerge.Join join,
                        List<PartMerge.AttributeChange> attributes) {
    }

    /**
     * @param message  the part as saved
     * @param outdated the summaries that became outdated by the edit, oldest first
     */
    record PartEdit(ChatMessage message, List<SummaryBlock> outdated) {
    }

}