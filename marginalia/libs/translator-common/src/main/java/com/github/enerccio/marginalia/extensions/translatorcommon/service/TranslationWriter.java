package com.github.enerccio.marginalia.extensions.translatorcommon.service;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.PassageService;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Span;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Reverse;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.TranslationPlan;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Writes a translation into the story and takes it out again: the one place that changes the text of a part and the passages that belong to it.
 * A plugin asks the model, checks the answer and makes a {@link TranslationPlan}; this class applies the plan.
 * <p>
 * What a write does: the text of the part is replaced in one edit (a version in the history, not flagged as the author's, and the text a model reads
 * stays the English), the passages of the earlier translation go (the ones the author edited stay: they hold their words), and a passage of the
 * plugin's kind is made for every translated speech with its {@link Span}. The writes of a book are one at a time (a lock per book, per instance of
 * this class), and a part that was changed since the plan was made is refused, so an edit made meanwhile is never lost.
 */
@Configurable
public class TranslationWriter {

    /**
     * The part was changed between reading it and writing the translation. Nothing was written.
     */
    public static final class ChangedMeanwhile extends IllegalStateException {
        public ChangedMeanwhile(String message) {
            super(message);
        }
    }

    /**
     * What a revert did.
     *
     * @param reverted speeches that are the source again
     * @param kept     speeches the author edited, which stay as they are
     */
    public record Reverted(int reverted, int kept) {
    }

    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired
    private PassageService passageService;

    private final String kindId;
    private final PassageAccess passages;
    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    /**
     * @param kindId the id of the passage kind of the plugin
     */
    public TranslationWriter(String kindId) {
        this.kindId = kindId;
        this.passages = new PassageAccess(kindId);
    }

    /**
     * Writes the plan.
     *
     * @param read       the part as the model read it (which passages the author edited)
     * @param languageId the language the speeches were translated into, as the plugin names it
     * @param modelName  the model that translated them
     * @param extra      sets what only the plugin knows on the data of each passage (a reading, ...); may be null
     * @throws ChangedMeanwhile when the part is not what {@code part} says it is
     */
    public void write(Manuscript book, ChatMessage part, TranslationPlan plan, Reverse.Result read, String languageId, String modelName,
                      BiConsumer<Span, TranslationPlan.Planned> extra) throws Exception {
        synchronized (lock(book)) {
            ChatMessage fresh = chatMessageService.find(part);
            if (!StringUtils.defaultString(fresh.getResponse()).equals(StringUtils.defaultString(part.getResponse()))) {
                throw new ChangedMeanwhile("The part was changed while it was being translated");
            }
            // the old passages go with the text they stood in, but the ones the author edited stay: they hold his words
            for (AnchoredPassage old : passages.passagesOf(fresh)) {
                var located = PassageAccess.locate(old);
                boolean editedByAuthor = located != null && read.edited().stream().anyMatch(e -> e.start() == located.start() && e.end() == located.end());
                if (located != null && !editedByAuthor) {
                    passageService.remove(book, old);
                }
            }
            boolean untouched = fresh.getOriginalResponse() != null && fresh.getOriginalResponse().equals(fresh.getResponse());
            chatMessageService.replaceText(book, fresh, plan.stored(), plan.source(), untouched ? plan.stored() : null,
                    RevisionOrigin.automatic(RevisionDetail.TRANSLATION, false));
            long now = System.currentTimeMillis();
            for (TranslationPlan.Planned planned : plan.passages()) {
                AnchoredPassage passage = passageService.create(book, fresh, kindId, planned.start(), planned.end());
                Span span = new Span(languageId, planned.english(), planned.translated());
                span.setSpeaker(StringUtils.defaultString(planned.speech().speaker()));
                span.setUnderstood(planned.speech().isUnderstood());
                span.setRegister(StringUtils.defaultString(planned.speech().register()));
                span.setModel(modelName);
                span.setTime(now);
                if (extra != null) {
                    extra.accept(span, planned);
                }
                span.write(passage.getAttributes());
                passageService.save(passage);
            }
        }
    }

    /**
     * Replaces one sentence the author selected by its translation and makes a passage of it that keeps the sentence as its source. The text a
     * model reads is the same as before.
     *
     * @param expected   the text of the part the range is in
     * @param start      first character of the sentence in {@code expected}
     * @param end        end of the sentence (exclusive)
     * @param translated what the sentence becomes
     * @param read       the part as the model reads it now; it stays what the model reads
     * @param extra      sets what only the plugin knows on the data of the passage; may be null
     * @return the saved part; null when the part is not what {@code expected} says (nothing was written)
     */
    public ChatMessage writeSelection(Manuscript book, ChatMessage part, String expected, int start, int end, String translated, String languageId,
                                      String modelName, Reverse.Result read, java.util.function.Consumer<Span> extra) throws Exception {
        synchronized (lock(book)) {
            ChatMessage fresh = chatMessageService.find(part);
            if (!expected.equals(StringUtils.defaultString(fresh.getResponse()))) {
                return null;
            }
            String stored = expected.substring(0, start) + translated + expected.substring(end);
            boolean untouched = fresh.getOriginalResponse() != null && fresh.getOriginalResponse().equals(fresh.getResponse());
            ChatMessage saved = chatMessageService.replaceText(book, fresh, stored, read.text(), untouched ? stored : null,
                    RevisionOrigin.automatic(RevisionDetail.TRANSLATION, false));
            AnchoredPassage passage = passageService.create(book, saved, kindId, start, start + translated.length());
            Span span = new Span(languageId, expected.substring(start, end), translated);
            span.setSpeaker("");
            span.setUnderstood(true);
            span.setRegister("");
            span.setModel(modelName);
            span.setTime(System.currentTimeMillis());
            if (extra != null) {
                extra.accept(span);
            }
            span.write(passage.getAttributes());
            passageService.save(passage);
            return saved;
        }
    }

    /**
     * Puts the source back into the part: every translated speech the author did not edit is its source again and its passage is gone. The history
     * keeps the translated version. A part with no translated speech is not touched.
     */
    public Reverted revert(Manuscript book, ChatMessage part, Reverse.Result read) throws Exception {
        if (read.substitutions().isEmpty()) {
            return new Reverted(0, read.edited().size());
        }
        synchronized (lock(book)) {
            ChatMessage fresh = chatMessageService.find(part);
            if (!StringUtils.defaultString(fresh.getResponse()).equals(StringUtils.defaultString(part.getResponse()))) {
                throw new ChangedMeanwhile("The part was changed while it was being reverted");
            }
            String english = read.text();
            for (AnchoredPassage old : passages.passagesOf(fresh)) {
                var located = PassageAccess.locate(old);
                boolean editedByAuthor = located != null && read.edited().stream().anyMatch(e -> e.start() == located.start() && e.end() == located.end());
                if (located != null && !editedByAuthor) {
                    passageService.remove(book, old);
                }
            }
            boolean untouched = fresh.getOriginalResponse() != null && fresh.getOriginalResponse().equals(fresh.getResponse());
            chatMessageService.replaceText(book, fresh, english, english, untouched ? english : null,
                    RevisionOrigin.automatic(RevisionDetail.TRANSLATION, false));
        }
        return new Reverted(read.substitutions().size(), read.edited().size());
    }

    /**
     * The part as a model reads it, with the passages the author edited (they are left as they are).
     */
    public Reverse.Result readOf(ChatMessage part) throws Exception {
        List<Reverse.Located> located = passages.of(List.of(part)).getOrDefault(part.getId(), List.of());
        return Reverse.toModel(part.getResponse(), located);
    }

    /**
     * Whether a range of the part touches a translated speech.
     */
    public boolean overlaps(ChatMessage part, int start, int end) throws Exception {
        return passages.of(List.of(part)).getOrDefault(part.getId(), List.of()).stream().anyMatch(located -> start < located.end() && end > located.start());
    }

    private Object lock(Manuscript book) {
        return locks.computeIfAbsent(book.getUuid(), uuid -> new Object());
    }
}
