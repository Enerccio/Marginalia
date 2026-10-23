package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.AnnotationRepository;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
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

public class AnnotationServiceImpl extends ExtendableServiceImpl<Annotation, AnnotationRepository> implements AnnotationService {

    private static final Logger log = LoggerFactory.getLogger(AnnotationServiceImpl.class);

    // a quote longer than this is cut, it only has to be enough to find the passage again
    private static final int MAX_QUOTE = 4000;
    // an orphaned annotation is looked for in the text again only by a quote this long, a shorter one is found by chance
    private static final int MIN_RESCUE_QUOTE = 12;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    @Lazy
    private ChatMessageService chatMessageService;

    @Override
    @CommonTx
    public Annotation create(Manuscript manuscript, ChatMessage part, Integer start, Integer end, String text, AnnotationSource source,
                             String type, String label) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage stored = ownPart(book, part);
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("The note is empty");
        }
        Annotation annotation = new Annotation();
        annotation.setPart(stored);
        annotation.setSource(source == null ? AnnotationSource.AUTHOR : source);
        annotation.setText(text);
        annotation.setType(StringUtils.trimToNull(type));
        annotation.setLabel(StringUtils.trimToNull(label));
        setRange(annotation, textOf(stored), start, end);
        return save(annotation);
    }

    @Override
    @CommonTx
    public Annotation createFromQuote(Manuscript manuscript, ChatMessage part, String quote, String before, String after, String text,
                                      AnnotationSource source, String type, String label) throws Exception {
        Manuscript book = ownBook(manuscript);
        ChatMessage stored = ownPart(book, part);
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("The note is empty");
        }
        Annotation annotation = new Annotation();
        annotation.setPart(stored);
        annotation.setSource(source == null ? AnnotationSource.AUTHOR : source);
        annotation.setText(text);
        annotation.setType(StringUtils.trimToNull(type));
        annotation.setLabel(StringUtils.trimToNull(label));
        String body = textOf(stored);
        QuoteFinder.Match match = QuoteFinder.find(body, quote, before, after, -1);
        if (match != null && match.end() > match.start()) {
            anchor(annotation, body, match.start(), match.end());
        } else {
            annotation.setQuote(StringUtils.abbreviate(quote, MAX_QUOTE));
            annotation.setNotLocated(true);
        }
        return save(annotation);
    }

    @Override
    @CommonTxReadOnly
    public List<Annotation> listForPart(Manuscript manuscript, ChatMessage part) throws Exception {
        Manuscript book = ownBook(manuscript);
        return getRepository().findAllByPart(ownPart(book, part));
    }

    @Override
    @CommonTxReadOnly
    public List<Annotation> listForManuscript(Manuscript manuscript) throws Exception {
        return getRepository().findAllByManuscript(ownBook(manuscript));
    }

    @Override
    @CommonTx
    public Annotation update(Manuscript manuscript, Annotation annotation, String text, String type, Boolean sendToPrompt) throws Exception {
        Annotation stored = ownAnnotation(ownBook(manuscript), annotation);
        if (text != null) {
            if (text.isBlank()) {
                throw new IllegalArgumentException("The note is empty");
            }
            stored.setText(text);
        }
        if (type != null) {
            stored.setType(StringUtils.trimToNull(type));
        }
        if (sendToPrompt != null) {
            stored.setSendToPrompt(sendToPrompt);
        }
        return save(stored);
    }

    @Override
    @CommonTx
    public Annotation setStatus(Manuscript manuscript, Annotation annotation, AnnotationStatus status) throws Exception {
        Annotation stored = ownAnnotation(ownBook(manuscript), annotation);
        if (status == null) {
            throw new IllegalArgumentException("The status is missing");
        }
        if (status == AnnotationStatus.ORPHANED) {
            // only a change of the text orphans an annotation
            throw new IllegalArgumentException("An annotation is orphaned by the text, not by the author");
        }
        stored.setStatus(status);
        return save(stored);
    }

    @Override
    @CommonTx
    public Annotation reattach(Manuscript manuscript, Annotation annotation, Integer start, Integer end) throws Exception {
        Annotation stored = ownAnnotation(ownBook(manuscript), annotation);
        ChatMessage part = (ChatMessage) Hibernate.unproxy(chatMessageService.find(stored.getPart()));
        setRange(stored, textOf(part), start, end);
        if (stored.getStatus() == AnnotationStatus.ORPHANED) {
            stored.setStatus(AnnotationStatus.OPEN);
        }
        return save(stored);
    }

    @Override
    @CommonTx
    public void remove(Manuscript manuscript, Annotation annotation) throws Exception {
        delete(ownAnnotation(ownBook(manuscript), annotation), false);
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
        List<Annotation> annotations = getRepository().findAllByPart(part);
        if (annotations.isEmpty()) {
            return;
        }
        TextAlignment alignment = TextAlignment.of(before, after);
        for (Annotation annotation : annotations) {
            if (annotation.hasRange()) {
                int start = annotation.getStart();
                int end = annotation.getEnd();
                if (end > before.length()) {
                    // the range was not in the text it is said to be in, nothing to follow
                    relocateOne(annotation, after);
                } else if (alignment.isApproximate() && !alignment.isRetained(start, end)) {
                    relocateOne(annotation, after);
                } else if (!alignment.isPartlyRetained(before, start, end)) {
                    orphan(annotation);
                } else {
                    int newStart = alignment.mapStart(start);
                    int newEnd = alignment.mapEnd(end);
                    if (newEnd <= newStart) {
                        orphan(annotation);
                    } else {
                        anchor(annotation, after, newStart, newEnd);
                    }
                }
                save(annotation);
            } else if (rescuable(annotation)) {
                // the passage may be back (the author undid the deletion)
                QuoteFinder.Match match = QuoteFinder.findExact(after, annotation.getQuote(), annotation.getContextBefore(),
                        annotation.getContextAfter(), -1);
                if (match != null) {
                    anchor(annotation, after, match.start(), match.end());
                    annotation.setStatus(AnnotationStatus.OPEN);
                    save(annotation);
                }
            }
        }
    }

    @Override
    @CommonTx
    public void moveToPrevious(Manuscript manuscript, ChatMessage later, ChatMessage previous, int offset) throws Exception {
        if (later == null || later.getId() == null || previous == null || previous.getId() == null) {
            return;
        }
        for (Annotation annotation : getRepository().findAllByPart(later)) {
            annotation.setPart(previous);
            if (annotation.hasRange()) {
                annotation.setRange(annotation.getStart() + offset, annotation.getEnd() + offset);
            }
            save(annotation);
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
        for (Annotation annotation : getRepository().findAllByPart(stored)) {
            if (annotation.hasRange() || annotation.getStatus() == AnnotationStatus.ORPHANED && StringUtils.isNotBlank(annotation.getQuote())) {
                relocateOne(annotation, text);
                save(annotation);
            }
        }
    }

    @Override
    @CommonTx
    public void copyForBranch(ChatMessage from, ChatMessage to) throws Exception {
        if (from == null || from.getId() == null || to == null || to.getId() == null) {
            return;
        }
        for (Annotation annotation : getRepository().findAllByPart(from)) {
            Annotation copy = new Annotation();
            copy.setOwner(annotation.getOwner());
            copy.setPart(to);
            copy.setStatus(annotation.getStatus());
            copy.setSource(annotation.getSource());
            copy.setRange(annotation.getStart(), annotation.getEnd());
            copy.setText(annotation.getText());
            copy.setQuote(annotation.getQuote());
            copy.setContextBefore(annotation.getContextBefore());
            copy.setContextAfter(annotation.getContextAfter());
            copy.setType(annotation.getType());
            copy.setLabel(annotation.getLabel());
            copy.setSendToPrompt(annotation.isSendToPrompt());
            copy.setNotLocated(annotation.isNotLocated());
            save(copy);
        }
    }

    /**
     * The passage of the annotation in the new text of the part, by its quote; the annotation is orphaned when it is not there.
     */
    private void relocateOne(Annotation annotation, String text) {
        QuoteFinder.Match match = StringUtils.isBlank(annotation.getQuote()) ? null
                : QuoteFinder.find(text, annotation.getQuote(), annotation.getContextBefore(), annotation.getContextAfter(),
                annotation.hasRange() ? annotation.getStart() : -1);
        if (match == null || match.end() <= match.start()) {
            orphan(annotation);
            return;
        }
        anchor(annotation, text, match.start(), match.end());
        if (annotation.getStatus() == AnnotationStatus.ORPHANED) {
            annotation.setStatus(AnnotationStatus.OPEN);
        }
    }

    private static boolean rescuable(Annotation annotation) {
        return annotation.getStatus() == AnnotationStatus.ORPHANED
                && StringUtils.length(annotation.getQuote()) >= MIN_RESCUE_QUOTE;
    }

    /**
     * The passage is gone: an open annotation is orphaned, one the author closed stays closed, none keeps the range.
     */
    private static void orphan(Annotation annotation) {
        annotation.setRange(null, null);
        if (annotation.getStatus() == AnnotationStatus.OPEN) {
            annotation.setStatus(AnnotationStatus.ORPHANED);
        }
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
        for (Annotation annotation : getRepository().findAllByManuscript(manuscript)) {
            JsonObject object = new JsonObject();
            object.addProperty("uuid", annotation.getUuid());
            object.addProperty("status", annotation.getStatus().name());
            object.addProperty("source", annotation.getSource().name());
            if (annotation.hasRange()) {
                object.addProperty("start", annotation.getStart());
                object.addProperty("end", annotation.getEnd());
            }
            if (annotation.getExtendedContent() == null || annotation.getExtendedContent().length == 0) {
                listener.serialize(annotation);
            }
            if (annotation.getExtendedContent() != null && annotation.getExtendedContent().length > 0) {
                try {
                    object.add("extendedContent", JsonParser.parseString(new String(annotation.getExtendedContent(), StandardCharsets.UTF_8)));
                } catch (Exception e) {
                    object.addProperty("extendedContent", new String(annotation.getExtendedContent(), StandardCharsets.UTF_8));
                }
            }
            byPart.computeIfAbsent(annotation.getPart().getId(), id -> new JsonArray()).add(object);
        }
        return byPart;
    }

    @Override
    @CommonTx
    public void restoreForPart(ChatMessage part, JsonArray annotations) throws Exception {
        if (part == null || annotations == null) {
            return;
        }
        for (JsonElement element : annotations) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject object = element.getAsJsonObject();
            try {
                Annotation annotation = new Annotation();
                annotation.setPart(part);
                annotation.setStatus(enumOf(AnnotationStatus.class, object, "status", AnnotationStatus.OPEN));
                annotation.setSource(enumOf(AnnotationSource.class, object, "source", AnnotationSource.AUTHOR));
                if (object.has("start") && object.has("end") && !object.get("start").isJsonNull() && !object.get("end").isJsonNull()) {
                    annotation.setRange(object.get("start").getAsInt(), object.get("end").getAsInt());
                }
                if (object.has("extendedContent") && !object.get("extendedContent").isJsonNull()) {
                    annotation.setExtendedContent(object.get("extendedContent").toString().getBytes(StandardCharsets.UTF_8));
                }
                saveWithoutEvent(annotation);
            } catch (Exception e) {
                log.warn("Cannot restore an annotation of a backup: {}", e.getMessage());
            }
        }
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

    private static String textOf(ChatMessage part) {
        return part.getResponse() == null ? "" : part.getResponse();
    }

    /**
     * Sets the passage [start, end) of the text, white space at the edges left out; both null is the part as a whole.
     */
    private static void setRange(Annotation annotation, String text, Integer start, Integer end) {
        if (start == null && end == null) {
            annotation.setRange(null, null);
            annotation.setQuote(null);
            annotation.setContextBefore(null);
            annotation.setContextAfter(null);
            annotation.setNotLocated(false);
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
        anchor(annotation, text, from, to);
    }

    /**
     * Sets the range and remembers the passage and its surroundings, to find it again if the offsets stop meaning anything.
     */
    private static void anchor(Annotation annotation, String text, int start, int end) {
        annotation.setRange(start, end);
        annotation.setQuote(StringUtils.left(text.substring(start, end), MAX_QUOTE));
        annotation.setContextBefore(text.substring(Math.max(0, start - CONTEXT_LENGTH), start));
        annotation.setContextAfter(text.substring(end, Math.min(text.length(), end + CONTEXT_LENGTH)));
        annotation.setNotLocated(false);
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

    private Annotation ownAnnotation(Manuscript book, Annotation annotation) throws Exception {
        Annotation stored = annotation == null ? null : findForUser(annotation.getUuid());
        if (stored == null || stored.getPart() == null || stored.getPart().isDeleted() || stored.getPart().getParentScript() == null
                || !stored.getPart().getParentScript().getId().equals(book.getId())) {
            throw new IllegalArgumentException("The annotation does not exist");
        }
        return stored;
    }

}
