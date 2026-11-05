package com.github.enerccio.marginalia.extensions.conlangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.BookSettings;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Origin;
import com.github.enerccio.marginalia.extensions.conlangtranslator.store.ConlangStore;
import com.github.enerccio.marginalia.extensions.conlangtranslator.text.ConlangKind;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.*;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.ModelClient;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.ModelSelection;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.TranslationWriter;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Reverse;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.SelectionRange;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.TranslationPlan;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

/**
 * Translates the direct speech of a part into the language of the book: asks the model (one request for a part), checks the answer, writes the
 * translations into the text of the part (one edit, with a version in the history, not flagged as the author's), makes a passage of every
 * speech with its English source, and adds the words the model invented to the lexicon.
 * <p>
 * The part is read as the model reads it (so a part that is translated already is translated again from its source). The writes of a book are
 * one at a time. A run that asks the model and is stopped or fails writes nothing.
 */
@Configurable
public class TranslationService {

    /** What came of a translation. */
    public enum Status {
        /** The speeches were written into the text. */
        TRANSLATED,
        /** The model found no speech in the language: nothing was changed. */
        NOTHING_TO_TRANSLATE,
        /** The book has no language, or the one it names is not in the library. */
        NO_LANGUAGE,
        /** There is no model for the request. */
        NO_MODEL,
        /** The part has no text. */
        EMPTY,
        /** The model did not answer in the expected form; nothing was changed. */
        BAD_ANSWER,
        /** The selection holds a translated speech already (or part of one); nothing was changed. */
        OVERLAPS,
        /** The part was changed since the selection was made; nothing was changed. */
        CHANGED
    }

    /**
     * @param status      what came of it
     * @param translated  how many speeches were written into the text
     * @param newWords    how many words were added to the lexicon
     * @param dropped     how many speeches of the answer could not be used (quote not found, another language, overlap)
     * @param needsRepair how many written speeches have words that the model did not report and the lexicon does not have
     * @param note        what the user should know, may be empty
     */
    public record Outcome(Status status, int translated, int newWords, int dropped, int needsRepair, String note) {

        static Outcome of(Status status, String note) {
            return new Outcome(status, 0, 0, 0, 0, note);
        }
    }

    @Autowired
    private ManuscriptService manuscriptService;
    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired
    private PartTextRegistry partTexts;
    @Autowired
    private SettingService settingService;

    /** How much of the story before and after a selected sentence the model reads (characters). */
    private static final int AROUND = 400;

    private final TranslationWriter writer = new TranslationWriter(ConlangKind.ID);
    private final LanguageAccess languages = new LanguageAccess();
    private final ModelClient modelClient = new ModelClient("conlang-translator");

    /**
     * The part as it is stored now.
     */
    public ChatMessage find(ChatMessage part) throws Exception {
        return chatMessageService.find(part);
    }

    /**
     * The settings of the book as it is stored now.
     */
    public BookSettings settings(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        return resolve(current != null ? current : manuscript);
    }

    /**
     * What applies to the book: the language, the automatic run, the model and the protocol are the inherited settings (the book's own, else a
     * folder's, else the user's), the number of earlier parts and the size limit of the lexicon are the book's own.
     */
    BookSettings resolve(Manuscript book) throws Exception {
        BookSettings settings = ConlangStore.readBook(book.getAttributes());
        settings.setLanguageId(StringUtils.trimToNull(manuscriptService.resolveExtension(book, ConlangInherited.LANGUAGE).value()));
        settings.setAuto(ConlangInherited.YES.equals(manuscriptService.resolveExtension(book, ConlangInherited.AUTO).value()));
        settings.setAiUuid(StringUtils.trimToNull(manuscriptService.resolveExtension(book, ConlangInherited.AI).value()));
        settings.setProtocolUuid(StringUtils.trimToNull(manuscriptService.resolveExtension(book, ConlangInherited.PROTOCOL).value()));
        return settings;
    }

    /**
     * The language of the library the book names, null when it names none or the language is gone.
     */
    public Language languageOf(BookSettings settings) throws Exception {
        UserSetting user = settingService.getOrCreate(UserSetting.class);
        return ConlangStore.find(user.getAttributes(), settings.getLanguageId());
    }

    /**
     * The language of the library the book names, as the book sees it: the working copy of an agent run reads its own version of a language
     * it changed ({@link LanguageAccess}). Null when it names none or the language is gone.
     */
    public Language languageOf(Manuscript book, BookSettings settings) throws Exception {
        return languages.find(book, settings.getLanguageId());
    }

    /**
     * Translates the speech of the part.
     */
    public Outcome translate(Manuscript manuscript, ChatMessage part) throws Exception {
        Manuscript book = manuscriptService.find(manuscript);
        ChatMessage current = chatMessageService.find(part);
        if (book == null || current == null) {
            throw new IllegalArgumentException("The part does not exist");
        }
        BookSettings settings = resolve(book);
        Language language = languageOf(book, settings);
        if (language == null) {
            return Outcome.of(Status.NO_LANGUAGE, settings.getLanguageId() == null ? "The book has no language." : "The language of the book is not in your library.");
        }
        // the text the model reads: a part that is translated already is translated again from its English
        Reverse.Result read = readOf(current);
        String source = read.text();
        if (StringUtils.isBlank(source)) {
            return Outcome.of(Status.EMPTY, "The part has no text.");
        }
        ModelSelection model = resolveModel(book, settings);
        if (model == null) {
            return Outcome.of(Status.NO_MODEL, "There is no model for the translation: the book has none and neither have your defaults.");
        }

        TranslationPrompt.Request request = TranslationPrompt.build(language, settings.getSpeakers(), source, scene(book, current, settings), settings.getMaxLexiconChars());
        String raw = ask(model, book, request);
        TranslationAnswer answer = TranslationAnswer.parse(raw);
        if (answer == null) {
            return Outcome.of(Status.BAD_ANSWER, "The model did not answer in the expected form.");
        }
        AnswerChecker.Checked checked = AnswerChecker.check(source, answer, language);
        TranslationPlan plan = TranslationPlan.make(source, checked.accepted().stream().map(item -> new TranslationPlan.Accepted(item.speech(), item.start(), item.end())).toList());
        int dropped = (int) checked.speeches().stream().filter(item -> !item.accepted()).count();
        if (plan.isEmpty()) {
            return new Outcome(Status.NOTHING_TO_TRANSLATE, 0, 0, dropped, 0, "");
        }
        write(book, current, language, model, plan, checked, read);
        int repair = (int) checked.accepted().stream().filter(item -> !item.unknownWords().isEmpty()).count();
        return new Outcome(Status.TRANSLATED, plan.passages().size(), checked.words().size(), dropped, repair,
                repair > 0 ? repair + " translation(s) use words that are not in the lexicon and were not reported." : "");
    }

    /**
     * @param reverted speeches that are English again
     * @param kept     speeches the author edited, which stay as they are
     */
    public record Reverted(int reverted, int kept) {
    }

    /**
     * Puts the English back into the part: every translated speech the author did not edit is its source again and its passage is gone. The
     * history keeps the conlang version. A part with no translated speech is not touched.
     */
    public Reverted revert(Manuscript manuscript, ChatMessage part) throws Exception {
        Manuscript book = manuscriptService.find(manuscript);
        ChatMessage current = chatMessageService.find(part);
        if (book == null || current == null) {
            throw new IllegalArgumentException("The part does not exist");
        }
        TranslationWriter.Reverted done = writer.revert(book, current, writer.readOf(current));
        return new Reverted(done.reverted(), done.kept());
    }

    /**
     * The part as a model reads it, with the passages that were edited by the author (they are left as they are).
     */
    private Reverse.Result readOf(ChatMessage part) throws Exception {
        return writer.readOf(part);
    }

    private TranslationPrompt.Scene scene(Manuscript book, ChatMessage part, BookSettings settings) throws Exception {
        List<String> earlier = new ArrayList<>();
        if (settings.getContextParts() > 0) {
            List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(part);
            int index = branch.size() - 1;
            for (int i = 0; i < branch.size(); i++) {
                if (branch.get(i).getId().equals(part.getId())) {
                    index = i;
                    break;
                }
            }
            List<ChatMessage> before = branch.subList(Math.max(0, index - settings.getContextParts()), index);
            earlier.addAll(partTexts.forModel(before));
        }
        return new TranslationPrompt.Scene(StringUtils.defaultString(part.getPovCharacter()), StringUtils.defaultString(part.getPresentCharacters()), earlier);
    }

    /** What is written: the text, the passages, the new words. The part is read again under the lock, so an edit made meanwhile is not lost. */
    private void write(Manuscript book, ChatMessage part, Language language, ModelSelection model, TranslationPlan plan,
                       AnswerChecker.Checked checked, Reverse.Result read) throws Exception {
        String modelName = model.ai().getName();
        long now = System.currentTimeMillis();
        writer.write(book, part, plan, read, language.getId(), modelName, null);
        addNewWords(book, language, checked.words(), part.getUuid(), modelName, now);
    }

    /** Adds the words the model invented to the lexicon of the library (of the run's copy of the language in an agent run), new. */
    private void addNewWords(Manuscript book, Language language, List<AnswerChecker.Word> words, String partUuid, String modelName, long now) throws Exception {
        if (words.isEmpty()) {
            return;
        }
        try {
            languages.update(book, language.getId(), stored -> NewWords.addTo(stored, words, Origin.model(partUuid, modelName, now)));
        } catch (IllegalArgumentException e) {
            // the language was deleted while the model worked: there is no lexicon to add to
        }
    }

    /**
     * Translates one sentence the author selected (typically one he wrote himself) into the language: the model translates it, the
     * sentence is replaced by the translation in the text of the part, and a passage keeps the English it was, like the speeches of a translated
     * part. The words the model had to invent are added to the lexicon as new. A selection that holds a translated speech is refused.
     *
     * @param expected the text of the part the range is in; the part must still have it
     * @param start    first character of the selection in {@code expected}
     * @param end      end of the selection (exclusive)
     */
    public Outcome translateSelection(Manuscript manuscript, ChatMessage part, String expected, int start, int end) throws Exception {
        Manuscript book = manuscriptService.find(manuscript);
        ChatMessage current = chatMessageService.find(part);
        if (book == null || current == null) {
            throw new IllegalArgumentException("The part does not exist");
        }
        String text = StringUtils.defaultString(expected);
        if (!text.equals(StringUtils.defaultString(current.getResponse()))) {
            return Outcome.of(Status.CHANGED, "The part was changed after the selection was made.");
        }
        int[] range = SelectionRange.trim(text, start, end);
        if (range[1] <= range[0]) {
            return Outcome.of(Status.EMPTY, "Nothing is selected.");
        }
        BookSettings settings = resolve(book);
        Language language = languageOf(book, settings);
        if (language == null) {
            return Outcome.of(Status.NO_LANGUAGE, "The book has no language.");
        }
        ModelSelection model = resolveModel(book, settings);
        if (model == null) {
            return Outcome.of(Status.NO_MODEL, "There is no model for the translation.");
        }
        Reverse.Result read = readOf(current);
        if (writer.overlaps(current, range[0], range[1])) {
            return Outcome.of(Status.OVERLAPS, "The selection holds a translated speech.");
        }
        String english = text.substring(range[0], range[1]);
        StringBuilder who = new StringBuilder();
        if (StringUtils.isNotBlank(current.getPovCharacter())) {
            who.append("Point of view: ").append(current.getPovCharacter().strip()).append(". ");
        }
        if (StringUtils.isNotBlank(current.getPresentCharacters())) {
            who.append("Characters present: ").append(current.getPresentCharacters().strip()).append('.');
        }
        String around = text.substring(Math.max(0, range[0] - AROUND), range[0]) + "[[" + english + "]]"
                + text.substring(range[1], Math.min(text.length(), range[1] + AROUND));
        TranslationPrompt.Request request = QuickTranslation.build(language, settings.getSpeakers(), english, who.toString().strip(), around,
                settings.getMaxLexiconChars());
        QuickTranslation.Answer answer = QuickTranslation.parse(ask(model, book, request));
        if (answer == null) {
            return Outcome.of(Status.BAD_ANSWER, "The model did not answer in the expected form.");
        }
        AnswerChecker.Checked checked = AnswerChecker.checkNewWords(answer.newWords(), language);
        String conlang = answer.text();
        List<String> reported = checked.words().stream().map(word -> word.word().word()).toList();
        int unknown = new LexiconCheck(language, reported, text).unknown(conlang).isEmpty() ? 0 : 1;
        ChatMessage saved = writer.writeSelection(book, current, text, range[0], range[1], conlang, language.getId(), model.ai().getName(), read, null);
        if (saved == null) {
            return Outcome.of(Status.CHANGED, "The part was changed while it was being translated.");
        }
        addNewWords(book, language, checked.words(), saved.getUuid(), model.ai().getName(), System.currentTimeMillis());
        return new Outcome(Status.TRANSLATED, 1, checked.words().size(), 0, unknown,
                unknown > 0 ? "The translation uses words that are not in the lexicon and were not reported." : "");
    }

    // --- the model ---

    /**
     * The model and the protocol of the book's settings of this plugin, else the book's, else the defaults of the user; null when there is none.
     */
    ModelSelection resolveModel(Manuscript book, BookSettings settings) throws Exception {
        return modelClient.choose(settings.getAiUuid(), settings.getProtocolUuid(), book);
    }

    /**
     * Asks the model and waits for the whole answer.
     */
    private String ask(ModelSelection model, Manuscript book, TranslationPrompt.Request request) throws Exception {
        return modelClient.ask(model, book, request.system(), request.user());
    }
}
