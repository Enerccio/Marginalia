package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.RevisionClearing;
import com.github.enerccio.marginalia.domain.components.RevisionData;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.components.RevisionRow;
import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;
import com.github.enerccio.marginalia.domain.repository.MessageRevisionRepository;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService.PartEdit;
import com.github.enerccio.marginalia.domain.service.EffectiveHistoryLimit;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.MessageRevisionService;
import com.github.enerccio.marginalia.domain.service.search.FulltextHit;
import com.github.enerccio.marginalia.domain.service.search.FulltextQuery;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class MessageRevisionServiceImpl extends ExtendableServiceImpl<MessageRevision, MessageRevisionRepository> implements MessageRevisionService {

    private static final int SNIPPET_RADIUS = 60;

    // the part service deletes and restores the versions with the parts
    @Autowired
    @Lazy
    private ChatMessageService chatMessageService;

    @Autowired
    @Lazy
    private ManuscriptService manuscriptService;

    @Override
    @CommonTx
    public MessageRevision record(ChatMessage part, RevisionSource source, String detail, boolean agent) throws Exception {
        if (part == null || part.getId() == null) {
            throw new IllegalArgumentException("The part is not saved");
        }
        return record(part, RevisionData.of(part, source, detail, agent));
    }

    @Override
    @CommonTx
    public MessageRevision record(ChatMessage part, RevisionData data) throws Exception {
        return append(part, data, null, false);
    }

    /**
     * Adds the version. Unless {@code force}, nothing is added when the text is the one of the newest version.
     */
    private MessageRevision append(ChatMessage part, RevisionData data, String restoredFrom, boolean force) throws Exception {
        if (part == null || part.getId() == null) {
            throw new IllegalArgumentException("The part is not saved");
        }
        String text = data.text();
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        MessageRevision latest = getRepository().findLatest(part);
        if (!force && latest != null && text.equals(latest.getResponse())) {
            return null;
        }
        EffectiveHistoryLimit limit = limitOf(part);
        if (limit.isOff()) {
            return null;
        }
        RevisionSource kind = data.source() == null ? RevisionSource.HUMAN : data.source();
        MessageRevision revision = new MessageRevision();
        revision.setMessage(part);
        // the owner of the part, whoever records it (a worker, an agent merge)
        revision.setOwner(part.getOwner());
        revision.setSource(kind);
        revision.setAgent(data.agent());
        revision.setSourceDetail(StringUtils.abbreviate(StringUtils.trimToNull(data.detail()), 255));
        revision.setLabel(StringUtils.abbreviate(StringUtils.trimToNull(data.label()), 255));
        revision.setWordCount(data.wordCount() != null ? data.wordCount() : chatMessageService.countWords(text));
        revision.setTokenCount(data.tokenCount() != null ? data.tokenCount() : text.equals(part.getResponse()) ? part.getTokenCount() : 0L);
        revision.setResponse(text);
        revision.setResponseReasoning(StringUtils.defaultIfEmpty(data.reasoning(), null));
        revision.setRestoredFrom(restoredFrom);
        MessageRevision saved = save(revision);
        prune(part, limit);
        return saved;
    }

    /**
     * The limit of the history of the book the part is in, unlimited for a part that is in no book.
     */
    private EffectiveHistoryLimit limitOf(ChatMessage part) throws Exception {
        // the part may come with a lazy book without a session: only its id is read from it
        Manuscript book = part.getParentScript() == null ? null : manuscriptService.find(part.getParentScript().getId());
        if (book == null) {
            return new EffectiveHistoryLimit(null, EffectiveHistoryLimit.Origin.NONE, null);
        }
        return manuscriptService.resolveHistoryLimit(book);
    }

    /**
     * Brings the part down to the limit: the versions in the trash go first, the oldest first, then the oldest ones that are not in the
     * trash. The newest version is the text of the part and the exempt ones (see {@link #exempt}) are not counted, so they stay.
     */
    private void prune(ChatMessage part, EffectiveHistoryLimit limit) throws Exception {
        if (limit.isUnlimited()) {
            return;
        }
        List<RevisionRow> rows = oldestFirst(part.getId());
        Long current = current(rows);
        List<RevisionRow> counted = rows.stream().filter(row -> !exempt(rows, row)).toList();
        int excess = counted.size() - limit.value();
        if (excess <= 0) {
            return;
        }
        List<Long> victims = new ArrayList<>();
        counted.stream().filter(RevisionRow::deleted).limit(excess).forEach(row -> victims.add(row.id()));
        counted.stream().filter(row -> !row.deleted() && !row.id().equals(current)).limit(excess - victims.size()).forEach(row -> victims.add(row.id()));
        getRepository().hardDeleteByIds(victims);
    }

    private List<RevisionRow> oldestFirst(Long partId) throws Exception {
        List<RevisionRow> rows = new ArrayList<>(getRepository().findRows(partId, true));
        Collections.reverse(rows);
        return rows;
    }

    /**
     * The newest version that is not in the trash: the text of the part.
     */
    private static Long current(List<RevisionRow> oldestFirst) {
        for (int i = oldestFirst.size() - 1; i >= 0; i--) {
            if (!oldestFirst.get(i).deleted()) {
                return oldestFirst.get(i).id();
            }
        }
        return null;
    }

    /**
     * Whether pruning (and <i>Clear history</i> that keeps snapshots) leaves the version alone: it has a label, or it is the first
     * generated version of the part.
     */
    private static boolean exempt(List<RevisionRow> oldestFirst, RevisionRow row) {
        if (row.label() != null) {
            return true;
        }
        return row.source() == RevisionSource.GENERATE
                && oldestFirst.stream().filter(other -> other.source() == RevisionSource.GENERATE).findFirst().orElse(null) == row;
    }

    @Override
    @CommonTx
    public void ensureCurrent(ChatMessage part) throws Exception {
        if (part == null || part.getId() == null || StringUtils.isEmpty(part.getResponse())) {
            return;
        }
        record(part, RevisionData.of(part, RevisionSource.PLUGIN, null, false));
    }

    @Override
    @CommonTxReadOnly
    public MessageRevision findLatest(ChatMessage part) throws Exception {
        MessageRevision latest = getRepository().findLatest(part);
        return latest != null && ownedByCurrentUser(latest) ? latest : null;
    }

    @Override
    @CommonTxReadOnly
    public List<RevisionRow> list(ChatMessage part, boolean includeDeleted) throws Exception {
        if (part == null || !ownedByCurrentUser(part)) {
            return List.of();
        }
        return getRepository().findRows(part, includeDeleted);
    }

    @Override
    @CommonTxReadOnly
    public Map<Long, Long> countFor(Collection<ChatMessage> parts) throws Exception {
        List<Long> ids = parts.stream().filter(part -> part != null && ownedByCurrentUser(part)).map(ChatMessage::getId)
                .filter(Objects::nonNull).toList();
        return getRepository().countByPartIds(ids);
    }

    @Override
    @CommonTx
    public MessageRevision snapshot(Manuscript manuscript, ChatMessage part, String label) throws Exception {
        String name = StringUtils.trimToNull(label);
        if (name == null) {
            throw new IllegalArgumentException("A saved version needs a label");
        }
        ChatMessage stored = ownPart(ownBook(manuscript), part);
        if (limitOf(stored).isOff()) {
            throw new IllegalStateException("The history of the book is switched off");
        }
        ensureCurrent(stored);
        MessageRevision latest = getRepository().findLatest(stored);
        String reasoning = latest != null && stored.getResponse().equals(latest.getResponse()) ? latest.getResponseReasoning() : null;
        MessageRevision snapshot = append(stored, new RevisionData(RevisionSource.SNAPSHOT, null, false, stored.getResponse(), reasoning,
                name, stored.getWordCount(), stored.getTokenCount()), null, true);
        if (snapshot == null) {
            throw new IllegalArgumentException("The part has no text");
        }
        return snapshot;
    }

    @Override
    @CommonTx
    public MessageRevision label(MessageRevision revision, String label) throws Exception {
        MessageRevision stored = ownRevision(revision);
        stored.setLabel(StringUtils.abbreviate(StringUtils.trimToNull(label), 255));
        return save(stored);
    }

    @Override
    @CommonTx
    public PartEdit restoreOnTop(Manuscript manuscript, ChatMessage part, MessageRevision revision) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage stored = ownPart(book, part);
        MessageRevision source = ownRevisionOf(stored, revision);
        ensureCurrent(stored);
        String text = source.getResponse();
        if (text.equals(stored.getResponse())) {
            return new PartEdit(stored, List.of());
        }
        PartEdit edit = chatMessageService.updateResponse(book, stored, text, RevisionOrigin.UNRECORDED);
        ChatMessage saved = takeReasoning(edit.message(), source);
        append(saved, new RevisionData(RevisionSource.RESTORE, null, false, text, source.getResponseReasoning(), null,
                saved.getWordCount(), saved.getTokenCount()), source.getUuid(), false);
        return new PartEdit(saved, edit.outdated());
    }

    @Override
    @CommonTx
    public PartEdit rollBackTo(Manuscript manuscript, ChatMessage part, MessageRevision revision) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage stored = ownPart(book, part);
        MessageRevision target = ownRevisionOf(stored, revision);
        // a text that was not recorded is newer than the version too, it goes to the trash with the rest
        ensureCurrent(stored);
        getRepository().softDeleteNewerThan(target);
        if (target.isDeleted()) {
            target.setDeleted(false);
            target = save(target);
        }
        if (target.getResponse().equals(stored.getResponse())) {
            return new PartEdit(stored, List.of());
        }
        PartEdit edit = chatMessageService.updateResponse(book, stored, target.getResponse(), RevisionOrigin.UNRECORDED);
        return new PartEdit(takeReasoning(edit.message(), target), edit.outdated());
    }

    /**
     * The part has the reasoning the version had, when it had one: a text a person wrote does not come with one, the part keeps the
     * one it has then, as it does when the author edits it.
     */
    private ChatMessage takeReasoning(ChatMessage part, MessageRevision revision) throws Exception {
        if (StringUtils.isEmpty(revision.getResponseReasoning())) {
            return part;
        }
        part.setResponseReasoning(revision.getResponseReasoning());
        return chatMessageService.save(part);
    }

    @Override
    @CommonTx
    public void trash(MessageRevision revision) throws Exception {
        MessageRevision stored = ownRevision(revision);
        if (stored.isDeleted()) {
            return;
        }
        MessageRevision latest = getRepository().findLatest(stored.getMessage());
        if (latest != null && latest.getId().equals(stored.getId())) {
            throw new IllegalStateException("The newest version is the text of the part");
        }
        stored.setDeleted(true);
        save(stored);
    }

    @Override
    @CommonTx
    public void undelete(MessageRevision revision) throws Exception {
        MessageRevision stored = ownRevision(revision);
        if (stored.isDeleted()) {
            stored.setDeleted(false);
            save(stored);
        }
    }

    @Override
    @CommonTx
    public void purge(MessageRevision revision) throws Exception {
        MessageRevision stored = ownRevision(revision);
        if (!stored.isDeleted()) {
            throw new IllegalStateException("The version is not in the trash");
        }
        delete(stored, true);
    }

    @Override
    @CommonTx
    public int emptyTrash(ChatMessage part) throws Exception {
        if (part == null || !ownedByCurrentUser(part)) {
            return 0;
        }
        return getRepository().hardDeleteTrashByPart(part);
    }

    @Override
    @CommonTx
    public int emptyTrash(Manuscript manuscript) throws Exception {
        return getRepository().hardDeleteTrashByManuscript(ownBook(manuscript));
    }

    @Override
    @CommonTxReadOnly
    public RevisionClearing previewClear(ChatMessage part, boolean keepSnapshots) throws Exception {
        return measure(clearable(part, keepSnapshots));
    }

    @Override
    @CommonTxReadOnly
    public RevisionClearing previewClear(Manuscript manuscript, boolean keepSnapshots) throws Exception {
        return measure(clearable(ownBook(manuscript), keepSnapshots));
    }

    @Override
    @CommonTx
    public RevisionClearing clear(ChatMessage part, boolean keepSnapshots) throws Exception {
        return remove(clearable(part, keepSnapshots));
    }

    @Override
    @CommonTx
    public RevisionClearing clear(Manuscript manuscript, boolean keepSnapshots) throws Exception {
        return remove(clearable(ownBook(manuscript), keepSnapshots));
    }

    private RevisionClearing measure(List<Long> ids) throws Exception {
        return ids.isEmpty() ? RevisionClearing.NOTHING : new RevisionClearing(ids.size(), getRepository().sizeOf(ids));
    }

    private RevisionClearing remove(List<Long> ids) throws Exception {
        RevisionClearing cleared = measure(ids);
        getRepository().hardDeleteByIds(ids);
        return cleared;
    }

    private List<Long> clearable(Manuscript book, boolean keepSnapshots) throws Exception {
        List<Long> ids = new ArrayList<>();
        for (Long partId : getRepository().findPartIdsWithVersions(book)) {
            ids.addAll(clearable(partId, keepSnapshots));
        }
        return ids;
    }

    private List<Long> clearable(ChatMessage part, boolean keepSnapshots) throws Exception {
        return part == null || part.getId() == null || !ownedByCurrentUser(part) ? List.of() : clearable(part.getId(), keepSnapshots);
    }

    /**
     * Every version of the part but the current one, and the exempt ones when they are kept.
     */
    private List<Long> clearable(Long partId, boolean keepSnapshots) throws Exception {
        List<RevisionRow> rows = oldestFirst(partId);
        Long current = current(rows);
        return rows.stream().filter(row -> !row.id().equals(current) && !(keepSnapshots && exempt(rows, row))).map(RevisionRow::id).toList();
    }

    private Manuscript ownBook(Manuscript manuscript) throws Exception {
        Manuscript book = manuscript == null ? null : (Manuscript) Hibernate.unproxy(manuscriptService.findForUser(manuscript.getUuid()));
        if (book == null) {
            throw new IllegalArgumentException("The manuscript does not exist");
        }
        return book;
    }

    private ChatMessage ownPart(Manuscript book, ChatMessage part) throws Exception {
        // the part may be the lazy proxy of an association loaded earlier, its extended attributes are not read from the proxy
        ChatMessage stored = part == null ? null : (ChatMessage) Hibernate.unproxy(chatMessageService.find(part));
        if (stored == null || stored.isDeleted() || stored.getParentScript() == null || !stored.getParentScript().getId().equals(book.getId())) {
            throw new IllegalArgumentException("The part does not exist");
        }
        return stored;
    }

    /**
     * The version as stored, when it is the user's.
     */
    private MessageRevision ownRevision(MessageRevision revision) throws Exception {
        MessageRevision stored = revision == null || revision.getId() == null ? null : getRepository().find(revision.getId());
        if (stored == null || !ownedByCurrentUser(stored) || stored.getMessage() == null) {
            throw new IllegalArgumentException("The version does not exist");
        }
        return stored;
    }

    /**
     * The version as stored, when it is the user's and a version of the part.
     */
    private MessageRevision ownRevisionOf(ChatMessage part, MessageRevision revision) throws Exception {
        MessageRevision stored = ownRevision(revision);
        if (!part.getId().equals(stored.getMessageId())) {
            throw new IllegalArgumentException("The version is not a version of the part");
        }
        return stored;
    }

    @Override
    @CommonTx
    public void softDeleteForManuscript(Manuscript manuscript) throws Exception {
        getRepository().softDeleteByManuscript(manuscript);
    }

    @Override
    @CommonTx
    public void softDeleteForParts(Collection<Long> partIds) throws Exception {
        getRepository().softDeleteByPartIds(partIds);
    }

    @Override
    @CommonTx
    public void hardDeleteForParts(Collection<Long> partIds) throws Exception {
        getRepository().hardDeleteByPartIds(partIds);
    }

    private boolean ownedByCurrentUser(OwnedEntity entity) {
        return entity.getOwner() != null && Objects.equals(entity.getOwner().getId(), currentUser.getId());
    }

    @Override
    @CommonTxReadOnly
    public List<FulltextHit> searchFulltext(Manuscript manuscript, String query) throws Exception {
        FulltextQuery parsed = FulltextQuery.parse(query);
        if (manuscript == null || parsed.isEmpty() || manuscriptService.findForUser(manuscript.getUuid()) == null) {
            return Collections.emptyList();
        }
        // newest first: the first row of a part is the version to show
        Map<Long, FulltextHit> hits = new LinkedHashMap<>();
        for (Object[] row : getRepository().findFulltexts(manuscript, parsed.likePatterns())) {
            hits.putIfAbsent((Long) row[1], new FulltextHit((Long) row[1], parsed.snippet((String) row[4], SNIPPET_RADIUS),
                    (String) row[0], (Date) row[2], (RevisionSource) row[3]));
        }
        return new ArrayList<>(hits.values());
    }

    @Override
    @CommonTxReadOnly
    public Map<Long, Set<Long>> findPartsWithText(Collection<Long> manuscriptIds, String query) throws Exception {
        FulltextQuery parsed = FulltextQuery.parse(query);
        if (parsed.isEmpty() || manuscriptIds == null) {
            return Collections.emptyMap();
        }
        Set<Long> own = new HashSet<>();
        for (Manuscript book : manuscriptService.findAllForUser()) {
            if (manuscriptIds.contains(book.getId())) {
                own.add(book.getId());
            }
        }
        Map<Long, Set<Long>> parts = new HashMap<>();
        for (Object[] row : getRepository().findFulltextHits(own, parsed.likePatterns())) {
            parts.computeIfAbsent((Long) row[1], k -> new HashSet<>()).add((Long) row[0]);
        }
        return parts;
    }

    @Override
    @CommonTxReadOnly
    public Map<Long, JsonArray> marshalForParts(Collection<Long> partIds) throws Exception {
        Map<Long, JsonArray> marshalled = new HashMap<>();
        if (partIds == null) {
            return marshalled;
        }
        for (Long partId : partIds) {
            List<RevisionRow> rows = oldestFirst(partId);
            if (rows.isEmpty()) {
                continue;
            }
            JsonArray versions = new JsonArray();
            for (RevisionRow row : rows) {
                MessageRevision revision = getRepository().find(row.id());
                if (revision == null || !ownedByCurrentUser(revision)) {
                    continue;
                }
                JsonObject version = new JsonObject();
                version.addProperty("uuid", revision.getUuid());
                version.addProperty("source", revision.getSource().name());
                version.addProperty("agent", revision.isAgent());
                version.addProperty("detail", revision.getSourceDetail());
                version.addProperty("label", revision.getLabel());
                version.addProperty("created", revision.getCreation().getTime());
                version.addProperty("deleted", revision.isDeleted());
                version.addProperty("text", revision.getResponse());
                version.addProperty("reasoning", revision.getResponseReasoning());
                version.addProperty("restoredFrom", revision.getRestoredFrom());
                version.addProperty("wordCount", revision.getWordCount());
                version.addProperty("tokenCount", revision.getTokenCount());
                versions.add(version);
            }
            if (!versions.isEmpty()) {
                marshalled.put(partId, versions);
            }
        }
        return marshalled;
    }

    @Override
    @CommonTx
    public int restoreForPart(ChatMessage part, JsonArray versions) throws Exception {
        if (part == null || part.getId() == null || versions == null || versions.isEmpty()) {
            return 0;
        }
        EffectiveHistoryLimit limit = limitOf(part);
        if (limit.isOff()) {
            return 0;
        }
        // new uuids, a restored copy points to the version it was made of by the new one
        Map<String, String> uuids = new HashMap<>();
        int made = 0;
        for (JsonElement element : versions) {
            JsonObject version = element.getAsJsonObject();
            String text = string(version, "text");
            if (StringUtils.isEmpty(text)) {
                continue;
            }
            MessageRevision revision = new MessageRevision();
            revision.setMessage(part);
            revision.setOwner(part.getOwner());
            revision.setUuid(UUID.randomUUID().toString());
            if (version.has("created") && !version.get("created").isJsonNull()) {
                revision.setCreation(new Date(version.get("created").getAsLong()));
            }
            RevisionSource source = RevisionSource.HUMAN;
            try {
                source = RevisionSource.valueOf(string(version, "source"));
            } catch (IllegalArgumentException | NullPointerException ignored) {
                // a source this version does not know
            }
            revision.setSource(source);
            revision.setAgent(version.has("agent") && version.get("agent").getAsBoolean());
            revision.setSourceDetail(string(version, "detail"));
            revision.setLabel(string(version, "label"));
            revision.setDeleted(version.has("deleted") && version.get("deleted").getAsBoolean());
            revision.setWordCount(version.has("wordCount") && !version.get("wordCount").isJsonNull()
                    ? version.get("wordCount").getAsInt() : chatMessageService.countWords(text));
            revision.setTokenCount(version.has("tokenCount") && !version.get("tokenCount").isJsonNull() ? version.get("tokenCount").getAsLong() : 0L);
            revision.setResponse(text);
            revision.setResponseReasoning(string(version, "reasoning"));
            revision.setRestoredFrom(uuids.get(string(version, "restoredFrom")));
            uuids.put(string(version, "uuid"), save(revision).getUuid());
            made++;
        }
        prune(part, limit);
        // the newest version is the text of the part
        ensureCurrent(part);
        return made;
    }

    private static String string(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? StringUtils.defaultIfEmpty(object.get(key).getAsString(), null) : null;
    }

    @Override
    @CommonTx
    public void recordFirst(ChatMessage part, RevisionSource source) throws Exception {
        if (part == null || part.getId() == null || StringUtils.isEmpty(part.getResponse()) || !getRepository().findRows(part.getId(), true).isEmpty()) {
            return;
        }
        if (source != null) {
            append(part, RevisionData.of(part, source, null, false), null, false);
            return;
        }
        if (StringUtils.isBlank(part.getModelUsed())) {
            append(part, RevisionData.of(part, RevisionSource.HUMAN, null, false), null, false);
            return;
        }
        String response = part.getResponse();
        String generated = StringUtils.defaultIfBlank(part.getOriginalResponse(), response);
        String model = StringUtils.trimToNull(part.getModelUsed());
        String protocol = StringUtils.trimToNull(part.getProtocolUsed());
        boolean same = generated.equals(response);
        append(part, new RevisionData(RevisionSource.GENERATE, protocol == null ? model : model + " · " + protocol, false, generated,
                StringUtils.defaultIfEmpty(part.getResponseReasoning(), null), null, same ? part.getWordCount() : null, same ? part.getTokenCount() : 0L), null, false);
        if (!same) {
            append(part, RevisionData.of(part, RevisionSource.HUMAN, null, false), null, false);
        }
    }

}
