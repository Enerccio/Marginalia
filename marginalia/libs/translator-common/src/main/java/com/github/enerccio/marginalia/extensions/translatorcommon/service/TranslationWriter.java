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
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Speech;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.TranslationPlan;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
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
                Span span;
                if (planned.carried() != null) {
                    // a speech of another language of the book, translated by an earlier run: it stays as it was
                    span = planned.carried();
                } else {
                    span = new Span(languageId, planned.english(), planned.translated());
                    span.setSpeaker(StringUtils.defaultString(planned.speech().speaker()));
                    span.setUnderstood(planned.speech().isUnderstood());
                    span.setRegister(StringUtils.defaultString(planned.speech().register()));
                    span.setModel(modelName);
                    span.setTime(now);
                    if (extra != null) {
                        extra.accept(span, planned);
                    }
                }
                span.write(passage.getAttributes());
                passageService.save(passage);
            }
        }
    }

    /**
     * The speeches of the part that a run for {@code languageId} leaves as they are: the translated speeches of the other languages of the book that
     * the author did not edit, as the entries of a {@link TranslationPlan} (their place in the text the model reads, the text they hold now, their
     * data). A plan made from these and the new speeches keeps the other languages when the part is written again.
     *
     * @param read       the part as the model read it (all languages turned back into the English)
     * @param languageId the language the run is for; the speeches of this language (and the ones without a language) are not carried
     */
    public List<TranslationPlan.Accepted> carried(ChatMessage part, Reverse.Result read, String languageId) throws Exception {
        return carried(StringUtils.defaultString(part.getResponse()), passages.of(List.of(part)).getOrDefault(part.getId(), List.of()), read, languageId);
    }

    /**
     * {@link #carried(ChatMessage, Reverse.Result, String)} for a stored text and its passages, nothing read from the store.
     */
    public static List<TranslationPlan.Accepted> carried(String stored, List<Reverse.Located> located, Reverse.Result read, String languageId) {
        List<TranslationPlan.Accepted> carried = new ArrayList<>();
        for (Reverse.Located passage : located) {
            boolean substituted = read.substitutions().stream().anyMatch(s -> s.start() == passage.start() && s.end() == passage.end());
            if (!substituted || StringUtils.equals(passage.span().getLanguage(), languageId)) {
                continue;
            }
            int[] model = read.model().modelRange(passage.start(), passage.end());
            if (model[1] <= model[0]) {
                continue;
            }
            Span span = passage.span();
            Speech speech = new Speech(span.getSource(), span.getSpeaker(), span.getLanguage(), stored.substring(passage.start(), passage.end()), "",
                    span.isUnderstood(), span.getRegister());
            carried.add(new TranslationPlan.Accepted(speech, model[0], model[1], span));
        }
        return carried;
    }

    /**
     * Whether a range of the text the model reads touches a speech that stays (see {@link #carried}); an answer for such a range is dropped.
     */
    public static boolean overlapsAny(List<TranslationPlan.Accepted> carried, int start, int end) {
        return carried.stream().anyMatch(item -> start < item.end() && end > item.start());
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
     * Puts the source back for the speeches of one language of the book; the speeches of the other languages stay translated. The history keeps the
     * translated version. A part with no translated speech in the language is not touched.
     *
     * @param read       the part as the model reads it now ({@link #readOf})
     * @param languageId the language to put back
     */
    public Reverted revert(Manuscript book, ChatMessage part, Reverse.Result read, String languageId) throws Exception {
        List<TranslationPlan.Accepted> carried = carried(part, read, languageId);
        int mine = read.substitutions().size() - carried.size();
        if (mine <= 0) {
            return new Reverted(0, read.edited().size());
        }
        TranslationPlan plan = TranslationPlan.make(read.text(), carried);
        write(book, part, plan, read, languageId, null, null);
        return new Reverted(mine, read.edited().size());
    }

    /**
     * Sets the English source of one translated speech by hand, for the author who edited the translation (a native speaker's fix) and now says what
     * it means, or who corrects the English the model wrote. The translated text the speech has now is taken as the one the plugin made, so a speech
     * that was marked as edited is not any more: the models read the new source in its place, and a retranslation of the part replaces it again.
     * The text of the part is not changed.
     *
     * @param passageUuid the uuid of the passage (it must be of this plugin's kind and the current user's)
     * @param source      the English the model reads in place of the speech
     * @return the data of the passage as it is saved
     * @throws IllegalArgumentException when the source is empty or the passage is not a translated speech of the text
     */
    public Span editSource(String passageUuid, String source) throws Exception {
        String english = StringUtils.strip(source);
        if (StringUtils.isEmpty(english)) {
            throw new IllegalArgumentException("The source is empty");
        }
        AnchoredPassage stored = ownPassage(passageUuid);
        Reverse.Located located = PassageAccess.locate(stored);
        if (located == null) {
            throw new IllegalArgumentException("The speech is not in the text any more");
        }
        String text = StringUtils.defaultString(chatMessageService.find(stored.getPart()).getResponse());
        if (located.end() > text.length() || located.start() >= located.end()) {
            throw new IllegalArgumentException("The speech is not in the text any more");
        }
        Span span = located.span();
        span.setSource(english);
        span.textIs(text.substring(located.start(), located.end()));
        span.write(stored.getAttributes());
        passageService.save(stored);
        return span;
    }

    /**
     * Writes the source a search and replace made, if the passage still has the source the search saw. Only the source changes: the translated
     * text and whether the author edited it stay as they are.
     *
     * @param expected the source the new one was made from
     * @return false when the source is not {@code expected} any more or the passage is gone (nothing was written)
     */
    public boolean replaceSource(String passageUuid, String expected, String source) throws Exception {
        if (StringUtils.isEmpty(source)) {
            throw new IllegalArgumentException("The source is empty");
        }
        AnchoredPassage stored;
        try {
            stored = ownPassage(passageUuid);
        } catch (IllegalArgumentException e) {
            return false;
        }
        Reverse.Located located = PassageAccess.locate(stored);
        if (located == null || !StringUtils.equals(located.span().getSource(), expected)) {
            return false;
        }
        Span span = located.span();
        span.setSource(source);
        span.write(stored.getAttributes());
        passageService.save(stored);
        return true;
    }

    private AnchoredPassage ownPassage(String uuid) throws Exception {
        AnchoredPassage stored = uuid == null ? null : passageService.findForUser(uuid);
        if (stored == null || stored.isDeleted() || !kindId.equals(stored.getPassageKind())) {
            throw new IllegalArgumentException("The speech does not exist");
        }
        return stored;
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
