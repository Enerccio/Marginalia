package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.RevisionData;
import com.github.enerccio.marginalia.domain.components.RevisionRow;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;
import com.github.enerccio.marginalia.domain.repository.MessageRevisionRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The versions of the text of a part ({@link MessageRevision}), kept apart from the part: the parts of a branch are loaded
 * together and a history grows without a bound, so whoever needs the text of a version asks here. Every version is a whole
 * text with who wrote it, the newest one that is not in the trash mirrors the part.
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

    void softDeleteForManuscript(Manuscript manuscript) throws Exception;

    void softDeleteForParts(Collection<Long> partIds) throws Exception;

    void hardDeleteForParts(Collection<Long> partIds) throws Exception;

}
