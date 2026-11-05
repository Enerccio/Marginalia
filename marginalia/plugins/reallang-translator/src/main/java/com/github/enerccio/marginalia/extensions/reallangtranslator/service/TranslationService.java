package com.github.enerccio.marginalia.extensions.reallangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.reallangtranslator.ReallangKind;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.BookSettings;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.RealLanguage;
import com.github.enerccio.marginalia.extensions.reallangtranslator.store.RealStore;
import com.github.enerccio.marginalia.extensions.reallangtranslator.translate.AnswerChecker;
import com.github.enerccio.marginalia.extensions.reallangtranslator.translate.QuickTranslation;
import com.github.enerccio.marginalia.extensions.reallangtranslator.translate.TranslationAnswer;
import com.github.enerccio.marginalia.extensions.reallangtranslator.translate.TranslationPrompt;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.ModelClient;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.ModelSelection;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.TranslationWriter;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Reverse;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.SelectionRange;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Speech;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.TranslationPlan;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Translates the direct speech of a part into the language of the book: asks the model (one request for a part), checks the answer, writes the
 * translations into the text of the part (one edit, with a version in the history, not flagged as the author's) and makes a passage of every speech
 * with its English source ({@link TranslationWriter}).
 * <p>
 * The part is read as the model reads it (so a part that is translated already is translated again from its source). A run that asks the model and
 * is stopped or fails writes nothing.
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
     * @param dropped     how many speeches of the answer could not be used (quote not found, another language, overlap)
     * @param wrongScript how many written speeches are not in the script the language asks for
     * @param note        what the user should know, may be empty
     */
    public record Outcome(Status status, int translated, int dropped, int wrongScript, String note) {

        static Outcome of(Status status, String note) {
            return new Outcome(status, 0, 0, 0, note);
        }
    }

    /**
     * What a quick translation came to. {@code status} is TRANSLATED when there is a text; the other fields are empty otherwise.
     */
    public record Quick(Status status, String text, String reading, String choice, String model, boolean wrongScript) {

        static Quick failed(Status status) {
            return new Quick(status, "", "", "", "", false);
        }
    }

    /** How much of the story before and after a selected sentence the model reads (characters). */
    private static final int AROUND = 400;

    @Autowired
    private ManuscriptService manuscriptService;
    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired
    private PartTextRegistry partTexts;
    @Autowired
    private SettingService settingService;

    private final TranslationWriter writer = new TranslationWriter(ReallangKind.ID);
    private final ModelClient modelClient = new ModelClient("reallang-translator");

    /**
     * The part as it is stored now.
     */
    public ChatMessage find(ChatMessage part) throws Exception {
        return chatMessageService.find(part);
    }

    /**
     * The settings of the book as they apply now.
     */
    public BookSettings settings(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        return resolve(current != null ? current : manuscript);
    }

    /**
     * What applies to the book: the language, the automatic run, the model and the protocol are the inherited settings (the book's own, else a
     * folder's, else the user's), the number of earlier parts and the speakers are the book's own.
     */
    BookSettings resolve(Manuscript book) throws Exception {
        BookSettings settings = RealStore.readBook(book.getAttributes());
        settings.setLanguageId(StringUtils.trimToNull(manuscriptService.resolveExtension(book, RealInherited.LANGUAGE).value()));
        settings.setAuto(RealInherited.YES.equals(manuscriptService.resolveExtension(book, RealInherited.AUTO).value()));
        settings.setAiUuid(StringUtils.trimToNull(manuscriptService.resolveExtension(book, RealInherited.AI).value()));
        settings.setProtocolUuid(StringUtils.trimToNull(manuscriptService.resolveExtension(book, RealInherited.PROTOCOL).value()));
        return settings;
    }

    /**
     * The language of the library the book names, null when it names none or the language is gone.
     */
    public RealLanguage languageOf(BookSettings settings) throws Exception {
        UserSetting user = settingService.getOrCreate(UserSetting.class);
        return RealStore.find(user.getAttributes(), settings.getLanguageId());
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
        RealLanguage language = languageOf(settings);
        if (language == null) {
            return Outcome.of(Status.NO_LANGUAGE, settings.getLanguageId() == null ? "The book has no language." : "The language of the book is not in your library.");
        }
        // the text the model reads: a part that is translated already is translated again from its English
        Reverse.Result read = writer.readOf(current);
        String source = read.text();
        if (StringUtils.isBlank(source)) {
            return Outcome.of(Status.EMPTY, "The part has no text.");
        }
        ModelSelection model = resolveModel(book, settings);
        if (model == null) {
            return Outcome.of(Status.NO_MODEL, "There is no model for the translation: the book has none and neither have your defaults.");
        }

        TranslationPrompt.Request request = TranslationPrompt.build(language, settings.getSpeakers(), source, scene(book, current, settings));
        TranslationAnswer answer = TranslationAnswer.parse(modelClient.ask(model, book, request.system(), request.user()));
        if (answer == null) {
            return Outcome.of(Status.BAD_ANSWER, "The model did not answer in the expected form.");
        }
        AnswerChecker.Checked checked = AnswerChecker.check(source, answer, language);
        TranslationPlan plan = TranslationPlan.make(source, checked.accepted().stream().map(item -> new TranslationPlan.Accepted(item.speech(), item.start(), item.end())).toList());
        int dropped = (int) checked.speeches().stream().filter(item -> !item.accepted()).count();
        if (plan.isEmpty()) {
            return new Outcome(Status.NOTHING_TO_TRANSLATE, 0, dropped, 0, "");
        }
        Map<Speech, String> readings = new IdentityHashMap<>();
        checked.accepted().forEach(item -> readings.put(item.speech(), StringUtils.defaultString(item.entry().reading()).strip()));
        writer.write(book, current, plan, read, language.getId(), model.ai().getName(),
                (span, planned) -> span.setReading(readings.getOrDefault(planned.speech(), "")));
        int wrong = checked.wrongScript();
        return new Outcome(Status.TRANSLATED, plan.passages().size(), dropped, wrong,
                wrong > 0 ? wrong + " translation(s) are not in the script the language asks for." : "");
    }

    /**
     * Puts the English back into the part: every translated speech the author did not edit is its source again and its passage is gone. The history
     * keeps the translated version. A part with no translated speech is not touched.
     */
    public TranslationWriter.Reverted revert(Manuscript manuscript, ChatMessage part) throws Exception {
        Manuscript book = manuscriptService.find(manuscript);
        ChatMessage current = chatMessageService.find(part);
        if (book == null || current == null) {
            throw new IllegalArgumentException("The part does not exist");
        }
        return writer.revert(book, current, writer.readOf(current));
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

    /**
     * Translates one sentence the author selected (typically one written by hand) into the language: the model translates it, the sentence is
     * replaced by the translation in the text of the part, and a passage keeps the English it was, like the speeches of a translated part. A selection
     * that holds a translated speech is refused.
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
        RealLanguage language = languageOf(settings);
        if (language == null) {
            return Outcome.of(Status.NO_LANGUAGE, "The book has no language.");
        }
        ModelSelection model = resolveModel(book, settings);
        if (model == null) {
            return Outcome.of(Status.NO_MODEL, "There is no model for the translation.");
        }
        Reverse.Result read = writer.readOf(current);
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
        TranslationPrompt.Request request = QuickTranslation.build(language, settings.getSpeakers(), english, who.toString().strip(), around);
        QuickTranslation.Answer answer = QuickTranslation.parse(modelClient.ask(model, book, request.system(), request.user()));
        if (answer == null) {
            return Outcome.of(Status.BAD_ANSWER, "The model did not answer in the expected form.");
        }
        ChatMessage saved = writer.writeSelection(book, current, text, range[0], range[1], answer.text(), language.getId(), model.ai().getName(), read,
                span -> span.setReading(answer.reading()));
        if (saved == null) {
            return Outcome.of(Status.CHANGED, "The part was changed while it was being translated.");
        }
        int wrong = com.github.enerccio.marginalia.extensions.reallangtranslator.translate.ScriptCheck.problem(language.getScript(), answer.text()) == null ? 0 : 1;
        return new Outcome(Status.TRANSLATED, 1, 0, wrong, wrong > 0 ? "The translation is not in the script the language asks for." : "");
    }

    /**
     * Translates a text for the author; nothing is written.
     *
     * @param context who says it, to whom, in what situation; may be empty
     */
    public Quick quickTranslate(Manuscript manuscript, String text, String context) throws Exception {
        Manuscript book = manuscriptService.find(manuscript);
        if (book == null) {
            throw new IllegalArgumentException("The book does not exist");
        }
        if (StringUtils.isBlank(text)) {
            return Quick.failed(Status.EMPTY);
        }
        BookSettings settings = resolve(book);
        RealLanguage language = languageOf(settings);
        if (language == null) {
            return Quick.failed(Status.NO_LANGUAGE);
        }
        ModelSelection model = resolveModel(book, settings);
        if (model == null) {
            return Quick.failed(Status.NO_MODEL);
        }
        TranslationPrompt.Request request = QuickTranslation.build(language, settings.getSpeakers(), text, context);
        QuickTranslation.Answer answer = QuickTranslation.parse(modelClient.ask(model, book, request.system(), request.user()));
        if (answer == null) {
            return Quick.failed(Status.BAD_ANSWER);
        }
        boolean wrong = com.github.enerccio.marginalia.extensions.reallangtranslator.translate.ScriptCheck.problem(language.getScript(), answer.text()) != null;
        return new Quick(Status.TRANSLATED, answer.text(), answer.reading(), answer.choice(), model.ai().getName(), wrong);
    }

    /**
     * The model and the protocol of the book's settings of this plugin, else the book's, else the defaults of the user; null when there is none.
     */
    ModelSelection resolveModel(Manuscript book, BookSettings settings) throws Exception {
        return modelClient.choose(settings.getAiUuid(), settings.getProtocolUuid(), book);
    }
}
