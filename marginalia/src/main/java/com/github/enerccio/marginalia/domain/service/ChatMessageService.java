package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ChatMessageRepository;

import com.github.enerccio.marginalia.domain.service.SummaryService.SummaryBlock;
import com.github.enerccio.marginalia.domain.service.search.FulltextHit;
import com.github.enerccio.marginalia.domain.service.search.FulltextQuery;

import java.util.List;

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

    int getBranchWordCount(ChatMessage leaf) throws Exception;

    long getBranchTokenCount(ChatMessage leaf) throws Exception;

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
     * Adds the {@code .md} files of a zip archive to the end of the book, one part for each file, in the order of the file
     * names (numbers inside names compare as numbers): the first part follows the active leaf of the book (it is the first
     * part of an empty book) and each next part follows the one before. The last part becomes the active leaf, which is
     * saved. As {@link #createPart}, the texts are counted and the parts are not flagged as edited. See
     * {@link com.github.enerccio.marginalia.utils.MarkdownArchive}.
     *
     * @return the number of parts added
     * @throws IllegalArgumentException when the data is not a zip archive or has no {@code .md} file, the manuscript is not
     *                                  the user's, or it has parts but no active leaf
     */
    int appendFromArchive(Manuscript manuscript, byte[] archive) throws Exception;

    /**
     * @param message  the part as saved
     * @param outdated the summaries that became outdated by the edit, oldest first
     */
    record PartEdit(ChatMessage message, List<SummaryBlock> outdated) {
    }

}