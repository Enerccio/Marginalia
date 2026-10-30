package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.RevisionClearing;
import com.github.enerccio.marginalia.domain.components.RevisionData;
import com.github.enerccio.marginalia.domain.components.RevisionRow;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;
import com.github.enerccio.marginalia.domain.repository.MessageRevisionRepository;
import com.github.enerccio.marginalia.domain.service.ChatMessageService.PartEdit;
import com.github.enerccio.marginalia.domain.service.search.FulltextHit;

import com.google.gson.JsonArray;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The versions of the text of a part ({@link MessageRevision}), kept apart from the part: the parts of a branch are loaded
 * together and a history grows without a bound, so whoever needs the text of a version asks here. Every version is a whole
 * text with who wrote it, the newest one that is not in the trash mirrors the part.
 * <p>
 * How many versions a part keeps is {@link ManuscriptService#resolveHistoryLimit}, counting the ones in the trash. Every version
 * that is made prunes the part down to it: the oldest version in the trash goes first, then the oldest one that is not. The first
 * generated version and the ones with a label are never pruned and do not count against the limit; neither is the newest.
 */
public interface MessageRevisionService extends ExtendableService<MessageRevision, MessageRevisionRepository> {

    /**
     * Adds the text the part has now as its next version, the single way a version is made. The words and the tokens are
     * the ones of the part, the reasoning of the part is kept only for a {@link RevisionSource#isModel() model} source. Does
     * nothing, and returns null, when the part has no text, or the text is the one of its newest version.
     *
     * @param detail see {@link MessageRevision#getSourceDetail()}, may be null
     * @param agent  whether an agent did it
     * @return the new version, null when none was made
     * @see #record(ChatMessage, RevisionData)
     */
    MessageRevision record(ChatMessage part, RevisionSource source, String detail, boolean agent) throws Exception;

    /**
     * Adds a version made of what the writer gives (see {@link RevisionData}), the single way a version is made: for a writer
     * whose text, reasoning or counts are not the ones the part has, a redraft with the reasoning of its own, a text that is
     * not in the part. Does nothing, and returns null, when the text is empty or is the one of the newest version of the part.
     * The version belongs to the owner of the part.
     *
     * @return the new version, null when none was made
     */
    MessageRevision record(ChatMessage part, RevisionData data) throws Exception;

    /**
     * Makes sure the text the part has now is the one of its newest version: when it is not (a writer that does not record,
     * a part from before the history, a restored backup), the text is recorded as a version of an unknown writer
     * ({@link RevisionSource#PLUGIN} without a detail). Called before a change of the text, so that the text that is replaced
     * is in the history. Does nothing for a part without text, or one that is not saved.
     */
    void ensureCurrent(ChatMessage part) throws Exception;

    /**
     * The newest version of the part that is not in the trash, null when it has none.
     */
    MessageRevision findLatest(ChatMessage part) throws Exception;

    /**
     * The versions of the part, newest first, without their text; empty for a part of another user.
     *
     * @param includeDeleted whether the versions in the trash are listed too
     */
    List<RevisionRow> list(ChatMessage part, boolean includeDeleted) throws Exception;

    /**
     * How many versions that are not in the trash each of the parts has, by the id of the part; the parts without one are
     * not in the map.
     */
    Map<Long, Long> countFor(Collection<ChatMessage> parts) throws Exception;

    /**
     * Saves the text the part has now as a version with a label (a snapshot), whether or not the newest version has the same text.
     * The reasoning is the one of the newest version when it is the same text. A version with a label is kept when the history is
     * pruned.
     *
     * @throws IllegalArgumentException when the label is blank, the part has no text, or the part is not a part of the manuscript
     *                                  or the manuscript is not the user's
     */
    MessageRevision snapshot(Manuscript manuscript, ChatMessage part, String label) throws Exception;

    /**
     * Gives the version a label, or takes it away when the label is blank.
     *
     * @return the version as saved
     * @throws IllegalArgumentException when the version is not the user's
     */
    MessageRevision label(MessageRevision revision, String label) throws Exception;

    /**
     * Puts the text of an earlier version on top of the history: the part gets the text (and the reasoning the version had, when it
     * had one), and a {@link RevisionSource#RESTORE} version, a copy of the earlier one, becomes the newest. Nothing is rewritten, the
     * versions in between stay. Does nothing when the part has this text already. The version may be in the trash.
     *
     * @return the saved part and the summaries the change made outdated
     * @throws IllegalArgumentException when the version is not a version of the part, or the part is not the manuscript's
     */
    PartEdit restoreOnTop(Manuscript manuscript, ChatMessage part, MessageRevision revision) throws Exception;

    /**
     * Goes back to an earlier version: the part gets its text (and reasoning), and every version made after it goes to the trash
     * (they can be taken out again). Nothing is added to the history. The version may be in the trash, it is taken out of it.
     *
     * @return the saved part and the summaries the change made outdated
     * @throws IllegalArgumentException when the version is not a version of the part, or the part is not the manuscript's
     */
    PartEdit rollBackTo(Manuscript manuscript, ChatMessage part, MessageRevision revision) throws Exception;

    /**
     * Puts the version into the trash of the history, where it counts toward the limit of the part until it is purged.
     *
     * @throws IllegalStateException    when the version is the newest one that is not in the trash: it is the text of the part
     * @throws IllegalArgumentException when the version is not the user's
     */
    void trash(MessageRevision revision) throws Exception;

    /**
     * Takes the version out of the trash, it is in its place by time again.
     *
     * @throws IllegalArgumentException when the version is not the user's
     */
    void undelete(MessageRevision revision) throws Exception;

    /**
     * Deletes the version for good.
     *
     * @throws IllegalStateException    when the version is not in the trash
     * @throws IllegalArgumentException when the version is not the user's
     */
    void purge(MessageRevision revision) throws Exception;

    /**
     * Deletes for good the versions of the part that are in the trash.
     *
     * @return how many
     */
    int emptyTrash(ChatMessage part) throws Exception;

    /**
     * Deletes for good the versions in the trash of all the parts of the book.
     *
     * @return how many
     */
    int emptyTrash(Manuscript manuscript) throws Exception;

    /**
     * What {@link #clear(ChatMessage, boolean)} would remove, for the dialog that asks first.
     */
    RevisionClearing previewClear(ChatMessage part, boolean keepSnapshots) throws Exception;

    /**
     * What {@link #clear(Manuscript, boolean)} would remove, for the dialog that asks first.
     */
    RevisionClearing previewClear(Manuscript manuscript, boolean keepSnapshots) throws Exception;

    /**
     * <i>Clear history</i> of a part: deletes for good every version but the current one (the newest that is not in the trash),
     * the ones in the trash too. The versions with a label and the first generated one stay when {@code keepSnapshots}.
     * Does nothing for a part of another user.
     */
    RevisionClearing clear(ChatMessage part, boolean keepSnapshots) throws Exception;

    /**
     * <i>Clear history</i> of a book: {@link #clear(ChatMessage, boolean)} for every part of it that is not deleted.
     *
     * @throws IllegalArgumentException when the manuscript is not the user's
     */
    RevisionClearing clear(Manuscript manuscript, boolean keepSnapshots) throws Exception;

    void softDeleteForManuscript(Manuscript manuscript) throws Exception;

    void softDeleteForParts(Collection<Long> partIds) throws Exception;

    void hardDeleteForParts(Collection<Long> partIds) throws Exception;

    /**
     * The parts of the book that have a version (one not in the trash) with every word of the query, one hit for a part: its newest
     * version that has them, with the text around the match. A part is found here even when its own text has the words; the
     * caller decides what to do with it. Nothing for a book of another user.
     *
     * @see com.github.enerccio.marginalia.domain.service.search.FulltextQuery
     */
    List<FulltextHit> searchFulltext(Manuscript manuscript, String query) throws Exception;

    /**
     * The parts of the books that have a version with every word of the query: the id of the book to the ids of its parts. The
     * books of another user are skipped.
     */
    Map<Long, Set<Long>> findPartsWithText(Collection<Long> manuscriptIds, String query) throws Exception;

    /**
     * The versions of the parts as a backup keeps them (the part id to the array of its versions, oldest first; the ones in the
     * trash too): {@code uuid, source, agent, detail, label, created (milliseconds), deleted, text, reasoning, restoredFrom,
     * wordCount, tokenCount}. The parts without a version are not in the map.
     */
    Map<Long, JsonArray> marshalForParts(Collection<Long> partIds) throws Exception;

    /**
     * Gives the part the versions of a backup ({@link #marshalForParts}), new uuids and the owner of the part, then prunes it to the limit
     * of its book. Nothing is written when the book keeps no history. When the newest version is not the text of the part
     * (a backup made by hand, a part that was changed) the text becomes a version of an unknown writer.
     *
     * @return number of the versions made
     */
    int restoreForPart(ChatMessage part, JsonArray versions) throws Exception;

    /**
     * The first version of a part that came from somewhere that kept no history of it (a backup without one, a copy of a book).
     * With a {@code source} the version is of that source; without one it is made as the migration of existing parts did: a part
     * a model wrote ({@code modelUsed}) gets a {@code GENERATE} version of the text as it was generated (with the reasoning and
     * "model · protocol") and a {@code HUMAN} one when the text was changed since, any other part a {@code HUMAN} one. A part that has
     * versions already, or no text, gets none.
     */
    void recordFirst(ChatMessage part, RevisionSource source) throws Exception;

}
