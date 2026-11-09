package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.AgentRunPairKind;
import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.PassageRepository;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.agent.UuidPair;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.utils.QuoteFinder;
import com.github.enerccio.marginalia.utils.TextAlignment;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PassageServiceImpl extends ExtendableServiceImpl<AnchoredPassage, PassageRepository> implements PassageService {

    private static final Logger log = LoggerFactory.getLogger(PassageServiceImpl.class);

    // a quote longer than this is cut, it only has to be enough to find the passage again
    static final int MAX_QUOTE = 4000;
    // an orphaned passage is looked for in the text again only by a quote this long, a shorter one is found by chance
    private static final int MIN_RESCUE_QUOTE = 12;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    @Lazy
    private ChatMessageService chatMessageService;

    @Autowired
    private PassageKinds passageKinds;

    @Override
    @CommonTx
    public AnchoredPassage create(Manuscript manuscript, ChatMessage part, String kind, Integer start, Integer end) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage stored = ownPart(book, part);
        AnchoredPassage passage = newPassage(kind, stored);
        setRange(passage, textOf(stored), start, end);
        return save(passage);
    }

    @Override
    @CommonTx
    public AnchoredPassage createFromQuote(Manuscript manuscript, ChatMessage part, String kind, String quote, String before, String after)
            throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage stored = ownPart(book, part);
        AnchoredPassage passage = newPassage(kind, stored);
        anchorByQuote(passage, textOf(stored), quote, before, after);
        return save(passage);
    }

    private static AnchoredPassage newPassage(String kind, ChatMessage part) {
        if (StringUtils.isBlank(kind)) {
            throw new IllegalArgumentException("The kind of the passage is missing");
        }
        AnchoredPassage passage = new AnchoredPassage();
        passage.setPassageKind(kind.trim());
        passage.setPart(part);
        return passage;
    }

    /**
     * Anchors the passage to the quote in the text, or marks it as about the part as a whole when the quote is not found.
     */
    static void anchorByQuote(AnchoredPassage passage, String body, String quote, String before, String after) {
        QuoteFinder.Match match = QuoteFinder.find(body, quote, before, after, -1);
        if (match != null && match.end() > match.start()) {
            anchor(passage, body, match.start(), match.end());
        } else {
            passage.setQuote(StringUtils.abbreviate(quote, MAX_QUOTE));
            passage.setNotLocated(true);
        }
    }

    @Override
    @CommonTxReadOnly
    public List<AnchoredPassage> listForPart(Manuscript manuscript, ChatMessage part, String kind) throws Exception {
        Manuscript book = ownBook(manuscript);
        return getRepository().findKindByPartIds(kind, List.of(ownPart(book, part).getId()));
    }

    @Override
    @CommonTxReadOnly
    public List<AnchoredPassage> listForManuscript(Manuscript manuscript, String kind) throws Exception {
        return getRepository().findKindByManuscript(kind, ownBook(manuscript));
    }

    @Override
    @CommonTxReadOnly
    public List<AnchoredPassage> listForParts(Collection<Long> partIds, String kind) throws Exception {
        return partIds == null || partIds.isEmpty() ? List.of() : getRepository().findKindByPartIds(kind, partIds);
    }

    @Override
    @CommonTx
    public AnchoredPassage reattach(Manuscript manuscript, AnchoredPassage passage, Integer start, Integer end) throws Exception {
        AnchoredPassage stored = ownPassage(ownBook(manuscript), passage);
        ChatMessage part = (ChatMessage) Hibernate.unproxy(chatMessageService.find(stored.getPart()));
        setRange(stored, textOf(part), start, end);
        stored.unorphan();
        return save(stored);
    }

    @Override
    @CommonTx
    public void remove(Manuscript manuscript, AnchoredPassage passage) throws Exception {
        delete(ownPassage(ownBook(manuscript), passage), false);
    }

    // ---- keeping the ranges in line with the text

    @Override
    @CommonTx
    public void shift(Manuscript manuscript, ChatMessage part, String oldText, String newText) throws Exception {
        String before = oldText == null ? "" : oldText;
        String after = newText == null ? "" : newText;
        if (before.equals(after) || part == null || part.getId() == null) {
            return;
        }
        List<AnchoredPassage> passages = getRepository().findAllByPart(part);
        if (passages.isEmpty()) {
            return;
        }
        TextAlignment alignment = TextAlignment.of(before, after);
        for (AnchoredPassage passage : passages) {
            if (passage.hasRange()) {
                int start = passage.getStart();
                int end = passage.getEnd();
                if (end > before.length()) {
                    // the range was not in the text it is said to be in, nothing to follow
                    relocateOne(passage, after);
                } else if (alignment.isApproximate() && !alignment.isRetained(start, end)) {
                    relocateOne(passage, after);
                } else if (!alignment.isPartlyRetained(before, start, end)) {
                    passage.orphan();
                } else {
                    int newStart = alignment.mapStart(start);
                    int newEnd = alignment.mapEnd(end);
                    if (newEnd <= newStart) {
                        passage.orphan();
                    } else {
                        anchor(passage, after, newStart, newEnd);
                    }
                }
                save(passage);
            } else if (rescuable(passage)) {
                // the passage may be back (the author undid the deletion)
                QuoteFinder.Match match = QuoteFinder.findExact(after, passage.getQuote(), passage.getContextBefore(),
                        passage.getContextAfter(), -1);
                if (match != null) {
                    anchor(passage, after, match.start(), match.end());
                    passage.unorphan();
                    save(passage);
                }
            }
        }
    }

    @Override
    @CommonTx
    public void shiftReplaced(Manuscript manuscript, ChatMessage part, String oldText, String newText) throws Exception {
        String before = oldText == null ? "" : oldText;
        String after = newText == null ? "" : newText;
        if (before.equals(after) || part == null || part.getId() == null) {
            return;
        }
        Map<Long, String> was = new HashMap<>();
        for (AnchoredPassage passage : getRepository().findAllByPart(part)) {
            if (passage.hasRange() && !passage.isOrphaned() && kindOf(passage) != null && passage.getEnd() <= before.length()) {
                was.put(passage.getId(), before.substring(passage.getStart(), passage.getEnd()));
            }
        }
        shift(manuscript, part, before, after);
        if (was.isEmpty()) {
            return;
        }
        for (AnchoredPassage passage : getRepository().findAllByPart(part)) {
            String old = was.get(passage.getId());
            if (old == null || !passage.hasRange() || passage.isOrphaned() || passage.getEnd() > after.length()) {
                continue;
            }
            String now = after.substring(passage.getStart(), passage.getEnd());
            PassageKind kind = kindOf(passage);
            if (!now.equals(old) && kind != null && kind.textReplaced(passage, old, now)) {
                save(passage);
            }
        }
    }

    /**
     * @return the registered kind of an extension's passage, null for an editorial note or a kind whose extension is not loaded
     */
    private PassageKind kindOf(AnchoredPassage passage) {
        String id = passage.getPassageKind();
        return id == null ? null : passageKinds.get(id);
    }

    @Override
    @CommonTx
    public void moveToPrevious(Manuscript manuscript, ChatMessage later, ChatMessage previous, int offset) throws Exception {
        if (later == null || later.getId() == null || previous == null || previous.getId() == null) {
            return;
        }
        for (AnchoredPassage passage : getRepository().findAllByPart(later)) {
            passage.setPart(previous);
            if (passage.hasRange()) {
                passage.setRange(passage.getStart() + offset, passage.getEnd() + offset);
            }
            save(passage);
        }
    }

    @Override
    @CommonTx
    public void relocate(Manuscript manuscript, ChatMessage part) throws Exception {
        if (part == null || part.getId() == null) {
            return;
        }
        ChatMessage stored = (ChatMessage) Hibernate.unproxy(chatMessageService.find(part));
        String text = textOf(stored);
        for (AnchoredPassage passage : getRepository().findAllByPart(stored)) {
            if (passage.hasRange() || passage.isOrphaned() && StringUtils.isNotBlank(passage.getQuote())) {
                relocateOne(passage, text);
                save(passage);
            }
        }
    }

    @Override
    @CommonTx
    public void copyForBranch(ChatMessage from, ChatMessage to) throws Exception {
        if (from == null || from.getId() == null || to == null || to.getId() == null) {
            return;
        }
        for (AnchoredPassage passage : getRepository().findAllByPart(from)) {
            AnchoredPassage copy = passage.copy();
            copy.setPart(to);
            save(copy);
        }
    }

    /**
     * The passage in the new text of the part, by its quote; the passage is orphaned when it is not there.
     */
    private static void relocateOne(AnchoredPassage passage, String text) {
        QuoteFinder.Match match = StringUtils.isBlank(passage.getQuote()) ? null
                : QuoteFinder.find(text, passage.getQuote(), passage.getContextBefore(), passage.getContextAfter(),
                passage.hasRange() ? passage.getStart() : -1);
        if (match == null || match.end() <= match.start()) {
            passage.orphan();
            return;
        }
        anchor(passage, text, match.start(), match.end());
        passage.unorphan();
    }

    private static boolean rescuable(AnchoredPassage passage) {
        return passage.isOrphaned() && StringUtils.length(passage.getQuote()) >= MIN_RESCUE_QUOTE;
    }

    // ---- deletes

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

    // ---- backups

    @Override
    @CommonTxReadOnly
    public Map<Long, JsonArray> marshalForManuscript(Manuscript manuscript) throws Exception {
        Map<Long, JsonArray> byPart = new HashMap<>();
        ExtendableEntityListener listener = new ExtendableEntityListener();
        for (AnchoredPassage passage : getRepository().findAllByManuscript(manuscript)) {
            JsonObject object = new JsonObject();
            object.addProperty("uuid", passage.getUuid());
            if (passage instanceof Annotation annotation) {
                object.addProperty("status", annotation.getStatus().name());
                object.addProperty("source", annotation.getSource().name());
            } else {
                // a backup of an older version has no kind and means an editorial note
                object.addProperty("kind", passage.discriminator());
                object.addProperty("passageKind", passage.getPassageKind());
            }
            if (passage.hasRange()) {
                object.addProperty("start", passage.getStart());
                object.addProperty("end", passage.getEnd());
            }
            if (passage.getExtendedContent() == null || passage.getExtendedContent().length == 0) {
                listener.serialize(passage);
            }
            if (passage.getExtendedContent() != null && passage.getExtendedContent().length > 0) {
                try {
                    object.add("extendedContent", JsonParser.parseString(new String(passage.getExtendedContent(), StandardCharsets.UTF_8)));
                } catch (Exception e) {
                    object.addProperty("extendedContent", new String(passage.getExtendedContent(), StandardCharsets.UTF_8));
                }
            }
            byPart.computeIfAbsent(passage.getPart().getId(), id -> new JsonArray()).add(object);
        }
        return byPart;
    }

    @Override
    @CommonTx
    public void restoreForPart(ChatMessage part, JsonArray passages, Collection<UuidPair> pairs) throws Exception {
        if (part == null || passages == null) {
            return;
        }
        for (JsonElement element : passages) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject object = element.getAsJsonObject();
            try {
                AnchoredPassage passage;
                if (AnchoredPassage.PASSAGE.equals(stringOf(object, "kind"))) {
                    passage = new AnchoredPassage();
                    passage.setPassageKind(stringOf(object, "passageKind"));
                } else {
                    Annotation annotation = new Annotation();
                    annotation.setStatus(enumOf(AnnotationStatus.class, object, "status", AnnotationStatus.OPEN));
                    annotation.setSource(enumOf(AnnotationSource.class, object, "source", AnnotationSource.AUTHOR));
                    passage = annotation;
                }
                passage.setPart(part);
                if (object.has("start") && object.has("end") && !object.get("start").isJsonNull() && !object.get("end").isJsonNull()) {
                    passage.setRange(object.get("start").getAsInt(), object.get("end").getAsInt());
                }
                if (object.has("extendedContent") && !object.get("extendedContent").isJsonNull()) {
                    passage.setExtendedContent(object.get("extendedContent").toString().getBytes(StandardCharsets.UTF_8));
                }
                passage = saveWithoutEvent(passage);
                if (pairs != null && object.has("uuid") && !object.get("uuid").isJsonNull()) {
                    pairs.add(new UuidPair(AgentRunPairKind.ANNOTATION, passage.getUuid(), object.get("uuid").getAsString()));
                }
            } catch (Exception e) {
                log.warn("Cannot restore an annotation of a backup: {}", e.getMessage());
            }
        }
    }

    private static String stringOf(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : null;
    }

    private static <E extends Enum<E>> E enumOf(Class<E> type, JsonObject object, String key, E fallback) {
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, object.get(key).getAsString());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    // ---- helpers

    static String textOf(ChatMessage part) {
        return part.getResponse() == null ? "" : part.getResponse();
    }

    /**
     * Sets the passage [start, end) of the text, white space at the edges left out; both null is the part as a whole.
     */
    static void setRange(AnchoredPassage passage, String text, Integer start, Integer end) {
        if (start == null && end == null) {
            passage.setRange(null, null);
            passage.setQuote(null);
            passage.setContextBefore(null);
            passage.setContextAfter(null);
            passage.setNotLocated(false);
            return;
        }
        if (start == null || end == null || start < 0 || end > text.length() || start >= end) {
            throw new IllegalArgumentException("The passage is not in the text of the part");
        }
        int from = start;
        int to = end;
        while (from < to && Character.isWhitespace(text.charAt(from))) {
            from++;
        }
        while (to > from && Character.isWhitespace(text.charAt(to - 1))) {
            to--;
        }
        if (from >= to) {
            throw new IllegalArgumentException("The passage is empty");
        }
        anchor(passage, text, from, to);
    }

    /**
     * Sets the range and remembers the passage and its surroundings, to find it again if the offsets stop meaning anything.
     */
    static void anchor(AnchoredPassage passage, String text, int start, int end) {
        passage.setRange(start, end);
        passage.setQuote(StringUtils.left(text.substring(start, end), MAX_QUOTE));
        passage.setContextBefore(text.substring(Math.max(0, start - com.github.enerccio.marginalia.domain.service.AnnotationService.CONTEXT_LENGTH), start));
        passage.setContextAfter(text.substring(end, Math.min(text.length(), end + com.github.enerccio.marginalia.domain.service.AnnotationService.CONTEXT_LENGTH)));
        passage.setNotLocated(false);
    }

    Manuscript ownBook(Manuscript manuscript) throws Exception {
        Manuscript book = manuscript == null ? null : (Manuscript) Hibernate.unproxy(manuscriptService.findForUser(manuscript.getUuid()));
        if (book == null) {
            throw new IllegalArgumentException("The manuscript does not exist");
        }
        return book;
    }

    ChatMessage ownPart(Manuscript book, ChatMessage part) throws Exception {
        // the part may be the lazy proxy of an association loaded earlier, its extended attributes are not read from the proxy
        ChatMessage stored = part == null ? null : (ChatMessage) Hibernate.unproxy(chatMessageService.find(part));
        if (stored == null || stored.isDeleted() || stored.getParentScript() == null || !stored.getParentScript().getId().equals(book.getId())) {
            throw new IllegalArgumentException("The part does not exist");
        }
        return stored;
    }

    private AnchoredPassage ownPassage(Manuscript book, AnchoredPassage passage) throws Exception {
        AnchoredPassage stored = passage == null ? null : findForUser(passage.getUuid());
        if (stored == null || stored.getPart() == null || stored.getPart().isDeleted() || stored.getPart().getParentScript() == null
                || !stored.getPart().getParentScript().getId().equals(book.getId())) {
            throw new IllegalArgumentException("The passage does not exist");
        }
        return stored;
    }

}
