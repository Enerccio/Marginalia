package com.github.enerccio.marginalia.extensions.conlangtranslator.commands;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.BookSettings;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.BookConlang;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.TranslationService;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.TranslationService.Outcome;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.TranslationService.Reverted;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.AnswerChecker.Word;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.LexiconCheck;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.LexiconText;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.*;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.PassageAccess;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.Mode;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.PartResult;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.RangePart;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.Summary;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.TranslationWriter;
import com.github.enerccio.marginalia.extensions.translatorcommon.text.UnderstandingBlock;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Reverse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Translating as commands: one part, a range, a quick text, the English back, the state of the book, and the two reads an agent needs (the text of
 * a part as the model reads it and the translated passages of a part). They call {@link TranslationService} and {@link BookConlang}, which the
 * dialogs call, and so work on the working copy of an agent run as well (the words a translation invents go to the run's copy of the language).
 * <p>
 * A part is addressed as in the other commands of the application: its handle, or its position on the active branch (the first part is 1).
 */
public class TranslationCommands extends ConlangCommandSupport {

    public static final String ADD_BOOK_LANGUAGE = "conlang-add-book-language";
    public static final String REMOVE_BOOK_LANGUAGE = "conlang-remove-book-language";
    public static final String STATUS = "conlang-status";
    public static final String TRANSLATE_PART = "conlang-translate-part";
    public static final String TRANSLATE_RANGE = "conlang-translate-range";
    public static final String REVERT = "conlang-revert-to-source";
    public static final String TRANSLATE_TEXT = "conlang-translate-text";
    public static final String CHECK_TRANSLATION = "conlang-check-translation";
    public static final String GET_SOURCE = "conlang-get-source";
    public static final String LIST_PASSAGES = "conlang-list-passages";
    public static final String EDIT_SOURCE = "conlang-edit-source";
    public static final String TRANSLATE_SENTENCE = "conlang-translate-sentence";
    public static final String SPEAKERS = "conlang-speakers";
    public static final String SET_SPEAKERS_MODE = "conlang-set-speakers-mode";
    public static final String SET_SPEAKER = "conlang-set-speaker";
    public static final String REMOVE_SPEAKER = "conlang-remove-speaker";
    public static final String SET_UNDERSTANDING_PROMPT = "conlang-set-understanding-prompt";
    public static final String UNDERSTANDING = "conlang-understanding";
    public static final String LEXICON = "conlang-lexicon";
    public static final String RULES = "conlang-rules";

    private static final String PARAM_LANGUAGE = "The language: its name (the case does not matter) or its id, see conlang-list";
    private static final String PARAM_BOOK_LANGUAGE = "One of the languages of the book: its name or its id (conlang-status lists them); all of them when left out";
    private static final String PARAM_BOOK_LANGUAGE_FIRST = "One of the languages of the book: its name or its id (conlang-status lists them); the first language of the book when left out";
    private static final String PARAM_UNDERSTANDS = "How much of the language the character follows: none, few-words, follows-most or fluent; empty = not said";
    private static final String PARAM_PART = "The part: its handle, or its position on the active branch (the first part is 1)";

    private final TranslationService translation;
    private final BookConlang bookConlang;
    private final PassageAccess passages = new PassageAccess(com.github.enerccio.marginalia.extensions.conlangtranslator.text.ConlangKind.ID);

    @Autowired
    private PartTextRegistry partTexts;

    public TranslationCommands(TranslationService translation, BookConlang bookConlang) {
        this.translation = translation;
        this.bookConlang = bookConlang;
    }

    // ---- the languages of the book

    @Command(commandName = ADD_BOOK_LANGUAGE, linkedCommand = STATUS, humanNameText = "Add a language to the book", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Adds another language the open book is translated into, besides the one it names.",
            llmDescriptionText = "Adds a language of the library to the book besides the language it names (conlang-set-book-language), for a story with more than one invented tongue (the elves, the dwarves, the orcs). "
                    + "Each language has its own speakers (conlang-speakers with 'language'); a part is translated with one request for each language, with that language's rules and lexicon. Returns the languages of the book.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the languages of the book"))
    public void addBookLanguage(CommandContext context) throws Exception {
        Book book = new Book(context);
        Language language = languages.resolve(book.manuscript, context.get("language", String.class));
        if (language.getId().equals(bookConlang.primaryLanguageId(book.manuscript))) {
            throw new IllegalArgumentException(language.getName() + " is the language the book names already");
        }
        List<BookLanguage> others = bookConlang.otherLanguages(book.manuscript);
        if (others.stream().noneMatch(item -> language.getId().equals(item.getLanguageId()))) {
            others.add(new BookLanguage(language.getId()));
            bookConlang.saveOtherLanguages(book.manuscript, others);
        }
        answer(context, languagesRow(book.manuscript));
    }

    @Command(commandName = REMOVE_BOOK_LANGUAGE, linkedCommand = ADD_BOOK_LANGUAGE, humanNameText = "Remove a language from the book", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Takes one of the other languages out of the book.",
            llmDescriptionText = "Removes one of the other languages of the book (not the one the book names: that is conlang-set-book-language) and its speakers. The translated speeches already in the story stay, "
                    + "put them back with conlang-revert-to-source first. Destructive, asks the user. Returns the languages of the book.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the languages of the book"))
    public void removeBookLanguage(CommandContext context) throws Exception {
        Book book = new Book(context);
        Language language = languages.resolve(book.manuscript, context.get("language", String.class));
        List<BookLanguage> others = bookConlang.otherLanguages(book.manuscript);
        BookLanguage found = others.stream().filter(item -> language.getId().equals(item.getLanguageId())).findFirst().orElse(null);
        if (found == null) {
            throw new IllegalArgumentException(language.getName() + " is not one of the other languages of the book");
        }
        if (!context.confirm("Take " + language.getName() + " and its speakers out of the book?")) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was removed");
            }
            return;
        }
        others.remove(found);
        bookConlang.saveOtherLanguages(book.manuscript, others);
        context.setReturn(GSON.toJson(languagesRow(book.manuscript)));
    }

    /** The languages of the book: the first and the others. */
    private JsonObject languagesRow(Manuscript book) throws Exception {
        JsonObject row = new JsonObject();
        JsonArray names = new JsonArray();
        for (LanguageRef item : bookConlang.languages(book)) {
            names.add(item.name());
        }
        row.add("languages", names);
        return row;
    }

    /**
     * The language the command is for: the id of the one of the book that the parameter names, null when it is left out.
     *
     * @throws IllegalArgumentException when the book does not have the language
     */
    private String bookLanguage(Book book, CommandContext context) throws Exception {
        String reference = context.get("language", String.class);
        if (StringUtils.isBlank(reference)) {
            return null;
        }
        Language language = languages.resolve(book.manuscript, reference);
        if (bookConlang.languages(book.manuscript).stream().noneMatch(item -> item.id().equals(language.getId()))) {
            throw new IllegalArgumentException("The book does not have " + language.getName() + " (conlang-add-book-language adds it)");
        }
        return language.getId();
    }

    /** The language of a command about the speakers: the one given, else the first language of the book. */
    private String speakersLanguage(Book book, CommandContext context) throws Exception {
        String id = bookLanguage(book, context);
        if (id == null) {
            id = bookConlang.primaryLanguageId(book.manuscript);
        }
        if (id == null) {
            throw new IllegalStateException("The book has no language (conlang-set-book-language)");
        }
        return id;
    }

    // ---- the state

    @Command(commandName = STATUS, linkedCommand = TRANSLATE_PART, humanNameText = "Conlang status", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "The language of the book and which parts are translated. Reads only.",
            llmDescriptionText = "Returns as JSON the language the book is translated into, whether parts are translated by themselves, how many parts of the active branch have text, how many have translated speech "
                    + "(and how many of those the author edited), and the positions of the parts that have none. A book with no language translates nothing. Reads only.",
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the state"))
    public void status(CommandContext context) throws Exception {
        Book book = new Book(context);
        BookSettings settings = translation.settings(book.manuscript);
        Language language = translation.languageOf(book.manuscript, settings);
        JsonObject result = new JsonObject();
        result.addProperty("language", language == null ? "none" : language.getName());
        JsonArray bookLanguages = new JsonArray();
        for (LanguageRef item : bookConlang.languages(book.manuscript)) {
            bookLanguages.add(item.name());
        }
        result.add("languages", bookLanguages);
        result.addProperty("translateByItself", settings.isAuto());
        List<ChatMessage> branch = textParts(book);
        Map<Long, List<Reverse.Located>> located = passages.of(branch);
        int translated = 0;
        int edited = 0;
        JsonArray untranslated = new JsonArray();
        List<ChatMessage> path = book.path((Integer) null);
        for (ChatMessage part : branch) {
            List<Reverse.Located> own = located.getOrDefault(part.getId(), List.of());
            if (own.isEmpty()) {
                if (untranslated.size() < 200) {
                    untranslated.add(path.indexOf(part) + 1);
                }
                continue;
            }
            translated++;
            String text = StringUtils.defaultString(part.getResponse());
            if (own.stream().anyMatch(item -> item.span().isEdited(slice(text, item.start(), item.end())))) {
                edited++;
            }
        }
        result.addProperty("partsWithText", branch.size());
        result.addProperty("partsTranslated", translated);
        result.addProperty("partsWithEditedSpeech", edited);
        result.add("positionsNotTranslated", untranslated);
        result.addProperty("rangeRunning", false);
        answer(context, result);
    }

    // ---- translating

    @Command(commandName = TRANSLATE_PART, linkedCommand = STATUS, humanNameText = "Translate a part", async = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Translates the speech of a part into the language of the book.",
            llmDescriptionText = "Asks the translating model to translate the direct speech of the part into the language of the book and writes the translations into the part (the quotation marks and the narration stay). "
                    + "Each translated speech keeps its English as its source, which the model reads back everywhere; the history of the part gets a version. A part that is translated already is translated again from its English. "
                    + "Words the model had to invent are added to the lexicon as new. Returns what came of it: speeches written, new words, speeches that could not be used, and translations that use words "
                    + "the lexicon does not have (needsRepair; conlang-check-translation lists them). Needs a language on the book (conlang-set-book-language) and a model. A book with more than one language "
                    + "(conlang-add-book-language) takes one request for each, one after the other; 'language' limits it to one."
                    + " 'missingOnly' translates only the speeches the model left in English and leaves the translated ones as they are (it finishes a part a run missed speeches of); 'within' quotes a passage of the part (it must occur once) and limits that to the speeches the passage touches.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, descriptionText = PARAM_PART),
                    @CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE),
                    @CommandParameter(name = "missingOnly", type = Boolean.class, optional = true, defaultValue = "false", descriptionText = "Translate only the speeches that are still in English and leave the translated ones as they are"),
                    @CommandParameter(name = "within", type = String.class, optional = true, descriptionText = "A passage of the part, exactly as it has it (it must occur once): only the speeches it touches are translated, and only the missing ones")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what came of it"))
    public void translatePart(CommandContext context) throws Exception {
        Book book = new Book(context);
        Book.Located located = book.locate(context.get("part", String.class), null);
        String within = StringUtils.trimToNull(context.get("within", String.class));
        boolean missingOnly = within != null || Boolean.TRUE.equals(context.get("missingOnly", Boolean.class));
        Outcome outcome;
        if (within != null) {
            ChatMessage part = translation.find(located.message());
            String text = StringUtils.defaultString(part.getResponse());
            int start = text.indexOf(within);
            if (start < 0) {
                throw new IllegalArgumentException("The part does not have that passage; quote it exactly (conlang-get-source shows the text)");
            }
            if (text.indexOf(within, start + 1) >= 0) {
                throw new IllegalArgumentException("The passage occurs more than once in the part; quote more of it");
            }
            outcome = translation.translateMissingIn(book.manuscript, part, text, start, start + within.length(), bookLanguage(book, context));
        } else if (missingOnly) {
            outcome = translation.translateMissing(book.manuscript, located.message(), bookLanguage(book, context));
        } else {
            outcome = translation.translate(book.manuscript, located.message(), bookLanguage(book, context));
        }
        JsonObject result = new JsonObject();
        result.addProperty("part", located.position());
        result.addProperty("status", outcome.status().name().toLowerCase(Locale.ROOT).replace('_', '-'));
        result.addProperty("speechesWritten", outcome.translated());
        result.addProperty("newWords", outcome.newWords());
        result.addProperty("speechesDropped", outcome.dropped());
        result.addProperty("needsRepair", outcome.needsRepair());
        if (!outcome.note().isBlank()) {
            result.addProperty("note", outcome.note());
        }
        context.setReturn(GSON.toJson(result));
    }

    @Command(commandName = TRANSLATE_RANGE, linkedCommand = STATUS, humanNameText = "Translate parts", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Translates the parts of a range, or translates the translated ones again.",
            llmDescriptionText = "Translates the speech in the parts from..to of the active branch, one part after the other (a part is minutes of the model's time). mode is 'new' (default: only the parts that have no "
                    + "translated speech) or 'again' (the parts that have translated speech are translated again from their English; what the author edited stays). Without 'to' only the part 'from' is done, "
                    + "without 'from' the whole branch. A part that fails is counted and the run goes on; no language or no model ends it. Asks the user for 'again'. Returns the summary.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "from", type = String.class, optional = true, descriptionText = "The first part: its handle or position; the first part of the story when left out"),
                    @CommandParameter(name = "to", type = String.class, optional = true, descriptionText = "The last part: its handle or position; the last part of the branch when left out"),
                    @CommandParameter(name = "mode", type = String.class, optional = true, allowed = {"new", "again"}, defaultValue = "new", descriptionText = "new = the parts not translated yet, again = translate the translated parts again"),
                    @CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the summary"))
    public void translateRange(CommandContext context) throws Exception {
        Book book = new Book(context);
        String modeText = StringUtils.defaultIfBlank(context.get("mode", String.class), "new").strip().toLowerCase(Locale.ROOT);
        Mode mode = switch (modeText) {
            case "new" -> Mode.TRANSLATE_NEW;
            case "again", "retranslate" -> Mode.RETRANSLATE;
            default -> throw new IllegalArgumentException("The mode is new or again, not " + modeText);
        };
        String languageId = bookLanguage(book, context);
        List<RangePart> parts = selectRange(book, context.get("from", String.class), context.get("to", String.class), mode, languageId);
        if (parts.isEmpty()) {
            context.setReturn("{\"planned\":0,\"hint\":\"Nothing to do in the range\"}");
            return;
        }
        if (mode == Mode.RETRANSLATE && !context.confirm("Translate " + parts.size() + " translated parts again? The speeches the author edited stay as they are.")) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was translated");
            }
            return;
        }
        run(context, book, mode, languageId, parts);
    }

    @Command(commandName = REVERT, linkedCommand = STATUS, humanNameText = "Put the English back", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Puts the English back into translated parts.",
            llmDescriptionText = "Puts the English source back into the translated speeches of the parts from..to (a single part when 'to' is left out, the whole branch when both are): the passages are removed, "
                    + "the history keeps the conlang version. Speeches the author edited stay as they are. Asks the user. Returns the summary.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "from", type = String.class, optional = true, descriptionText = "The first part: its handle or position; the first part of the story when left out"),
                    @CommandParameter(name = "to", type = String.class, optional = true, descriptionText = "The last part: its handle or position; only 'from' when left out"),
                    @CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the summary"))
    public void revert(CommandContext context) throws Exception {
        Book book = new Book(context);
        String languageId = bookLanguage(book, context);
        List<RangePart> parts = selectRange(book, context.get("from", String.class), context.get("to", String.class), Mode.REVERT, languageId);
        if (parts.isEmpty()) {
            context.setReturn("{\"planned\":0,\"hint\":\"No translated part in the range\"}");
            return;
        }
        if (!context.confirm("Put the English back into " + parts.size() + " translated parts?")) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was changed");
            }
            return;
        }
        run(context, book, Mode.REVERT, languageId, parts);
    }

    private void run(CommandContext context, Book book, Mode mode, String languageId, List<RangePart> parts) {
        AtomicBoolean stop = new AtomicBoolean();
        int[] done = {0};
        Summary summary = RangeLoop.run(parts, part -> {
            if (context.isTerminated()) {
                stop.set(true);
                return PartResult.nothing();
            }
            context.setStatus("Part " + (done[0] + 1) + " of " + parts.size());
            PartResult result = apply(book.manuscript, mode, part, languageId);
            done[0]++;
            return result;
        }, stop, null);
        JsonObject row = new JsonObject();
        row.addProperty("planned", summary.planned());
        row.addProperty("processed", summary.processed());
        row.addProperty("partsChanged", summary.changed());
        row.addProperty("speeches", summary.speeches());
        row.addProperty("newWords", summary.newWords());
        row.addProperty("editedSpeechesLeft", summary.kept());
        row.addProperty("failed", summary.failed());
        if (!summary.firstFailure().isBlank()) {
            row.addProperty("firstFailure", summary.firstFailure());
        }
        row.addProperty("stopped", summary.stopped());
        row.addProperty("aborted", summary.aborted());
        if (!context.isTerminated()) {
            context.setReturn(GSON.toJson(row));
        }
    }

    private PartResult apply(Manuscript book, Mode mode, ChatMessage part, String languageId) throws Exception {
        if (mode == Mode.REVERT) {
            Reverted reverted = translation.revert(book, part, languageId);
            return reverted.reverted() == 0 ? PartResult.nothing() : PartResult.done(reverted.reverted(), 0, reverted.kept());
        }
        Outcome outcome = translation.translate(book, part, languageId);
        return switch (outcome.status()) {
            case TRANSLATED -> PartResult.done(outcome.translated(), outcome.newWords(), 0);
            case NOTHING_TO_TRANSLATE, EMPTY -> PartResult.nothing();
            case NO_LANGUAGE, NO_MODEL -> PartResult.abort(outcome.note());
            case BAD_ANSWER, OVERLAPS, CHANGED -> PartResult.failed(outcome.note());
        };
    }

    @Command(commandName = TRANSLATE_TEXT, linkedCommand = STATUS, humanNameText = "Translate a text", async = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Translates a text into the language of the book without writing it into the story.",
            llmDescriptionText = "Translates a phrase, a name or a short text into the language of the book (the quick translation) and returns the translation, a note on the model's choices and the words it had to invent. "
                    + "Nothing is written into the story. With saveWords true the invented words are added to the lexicon as new. context says who speaks to whom in what situation.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class, descriptionText = "The English text"),
                    @CommandParameter(name = "context", type = String.class, optional = true, descriptionText = "Who says it, to whom, in what situation"),
                    @CommandParameter(name = "saveWords", type = Boolean.class, optional = true, defaultValue = "false", descriptionText = "Add the invented words to the lexicon"),
                    @CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE_FIRST)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the translation"))
    public void translateText(CommandContext context) throws Exception {
        Book book = new Book(context);
        String languageId = bookLanguage(book, context);
        BookConlang.Quick quick = bookConlang.quickTranslate(book.manuscript, required(context.get("text", String.class), "the text"),
                StringUtils.defaultString(context.get("context", String.class)), languageId);
        JsonObject result = new JsonObject();
        result.addProperty("status", quick.status().name().toLowerCase(Locale.ROOT).replace('_', '-'));
        if (quick.status() == TranslationService.Status.TRANSLATED) {
            result.addProperty("translation", quick.text());
            result.addProperty("choice", quick.choice());
            JsonArray words = new JsonArray();
            for (Word word : quick.words()) {
                JsonObject row = new JsonObject();
                row.addProperty("word", word.word().word());
                row.addProperty("meaning", word.word().gloss());
                row.addProperty("rationale", word.word().rationale());
                words.add(row);
            }
            result.add("newWords", words);
            if (!quick.unknownWords().isEmpty()) {
                result.add("wordsNotInTheLexicon", array(quick.unknownWords()));
            }
            if (Boolean.TRUE.equals(context.get("saveWords", Boolean.class)) && !quick.words().isEmpty()) {
                result.addProperty("wordsSaved", bookConlang.importWords(book.manuscript, quick.words(), quick.model(), languageId));
            }
        }
        context.setReturn(GSON.toJson(result));
    }

    @Command(commandName = TRANSLATE_SENTENCE, linkedCommand = STATUS, humanNameText = "Translate a sentence of a part", async = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Translates one sentence of a part into the language, in the text of the part.",
            llmDescriptionText = "Translates one sentence you quote from the part (typically one the author wrote himself, outside a speech the translator found) into the language of the book and replaces it in the text of the part; "
                    + "its English is kept as the source. The sentence must occur exactly once in the part and must not hold an already translated speech. New words go to the lexicon as new. Returns what came of it.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, descriptionText = PARAM_PART),
                    @CommandParameter(name = "sentence", type = String.class, descriptionText = "The sentence, exactly as the part has it"),
                    @CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE_FIRST)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what came of it"))
    public void translateSentence(CommandContext context) throws Exception {
        Book book = new Book(context);
        Book.Located located = book.locate(context.get("part", String.class), null);
        String sentence = required(context.get("sentence", String.class), "the sentence");
        ChatMessage part = translation.find(located.message());
        String text = StringUtils.defaultString(part.getResponse());
        int start = text.indexOf(sentence);
        if (start < 0) {
            throw new IllegalArgumentException("The part does not have that sentence; quote it exactly (conlang-get-source shows the text)");
        }
        if (text.indexOf(sentence, start + 1) >= 0) {
            throw new IllegalArgumentException("The sentence occurs more than once in the part; quote more of it");
        }
        Outcome outcome = translation.translateSelection(book.manuscript, part, text, start, start + sentence.length(), bookLanguage(book, context));
        JsonObject result = new JsonObject();
        result.addProperty("part", located.position());
        result.addProperty("status", outcome.status().name().toLowerCase(Locale.ROOT).replace('_', '-'));
        result.addProperty("newWords", outcome.newWords());
        if (!outcome.note().isBlank()) {
            result.addProperty("note", outcome.note());
        }
        context.setReturn(GSON.toJson(result));
    }

    // ---- who speaks the language in this book

    @Command(commandName = SPEAKERS, linkedCommand = SET_SPEAKER, humanNameText = "Speakers of the language", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "Who speaks the language in this book. Reads only.",
            llmDescriptionText = "Returns who speaks the language of the book: the mode (everyone-except = everybody speaks it but the listed characters, only = only the listed ones speak it) and the characters "
                    + "with their aliases, gender, voice and whether they are listed. This is the default; the text of a part overrides it. Reads only.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE_FIRST)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the speakers"))
    public void speakers(CommandContext context) throws Exception {
        Book book = new Book(context);
        answer(context, speakersRow(bookConlang.speakers(book.manuscript, speakersLanguage(book, context))));
    }

    @Command(commandName = SET_SPEAKERS_MODE, linkedCommand = SPEAKERS, humanNameText = "Set who speaks the language", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Chooses whether everybody speaks the language except the listed characters, or only the listed ones.",
            llmDescriptionText = "Sets the mode of the speakers of the book: everyone-except (everybody speaks the language but the listed characters) or only (only the listed characters speak it). Returns the speakers.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "mode", type = String.class, allowed = {"everyone-except", "only"}, descriptionText = "everyone-except or only"),
                    @CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE_FIRST)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the speakers"))
    public void setSpeakersMode(CommandContext context) throws Exception {
        Book book = new Book(context);
        String mode = required(context.get("mode", String.class), "the mode").toLowerCase(Locale.ROOT);
        String languageId = speakersLanguage(book, context);
        Speakers speakers = bookConlang.speakers(book.manuscript, languageId);
        speakers.setMode(switch (mode) {
            case "everyone-except", "everyone_except" -> SpeakersMode.EVERYONE_EXCEPT;
            case "only" -> SpeakersMode.ONLY;
            default -> throw new IllegalArgumentException("The mode is everyone-except or only, not " + mode);
        });
        bookConlang.saveSpeakers(book.manuscript, languageId, speakers);
        answer(context, speakersRow(speakers));
    }

    @Command(commandName = SET_SPEAKER, linkedCommand = SPEAKERS, humanNameText = "Set a character of the speakers", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Adds a character to the speakers or changes the note on one.",
            llmDescriptionText = "Adds a character, or changes the one with this name or alias: aliases (separated by commas), gender, voice (how the character sounds, for the register) and listed "
                    + "(in everyone-except mode a listed character does not speak the language; in only mode a listed character does) and understands (how much of the language the character follows "
                    + "when it is spoken around them: none, few-words, follows-most, fluent; empty = not said; the story model and the reviewer of the annotation are told). Pass only what to change. Returns the speakers.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, descriptionText = "The name of the character (or an alias of one that is there)"),
                    @CommandParameter(name = "listed", type = Boolean.class, optional = true, descriptionText = "Whether the character is on the list of the mode"),
                    @CommandParameter(name = "aliases", type = String.class, optional = true, descriptionText = "Other names, separated by commas; empty removes them"),
                    @CommandParameter(name = "gender", type = String.class, optional = true, descriptionText = "The gender, free text"),
                    @CommandParameter(name = "voice", type = String.class, optional = true, descriptionText = "How the character speaks"),
                    @CommandParameter(name = "understands", type = String.class, optional = true, descriptionText = PARAM_UNDERSTANDS),
                    @CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE_FIRST)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the speakers"))
    public void setSpeaker(CommandContext context) throws Exception {
        Book book = new Book(context);
        String name = required(context.get("name", String.class), "the name of the character");
        String languageId = speakersLanguage(book, context);
        Speakers speakers = bookConlang.speakers(book.manuscript, languageId);
        CharacterNote note = speakers.find(name);
        if (note == null) {
            note = new CharacterNote(name);
            speakers.getCharacters().add(note);
        }
        Boolean listed = context.get("listed", Boolean.class);
        String aliases = context.get("aliases", String.class);
        String gender = context.get("gender", String.class);
        String voice = context.get("voice", String.class);
        String understands = context.get("understands", String.class);
        if (understands != null) {
            note.setLevel(UnderstandingLevel.parse(understands));
        }
        if (listed != null) {
            note.setListed(listed);
        }
        if (aliases != null) {
            note.setAliases(Arrays.stream(aliases.split(",")).map(String::strip).filter(alias -> !alias.isEmpty()).toList());
        }
        if (gender != null) {
            note.setGender(gender.strip());
        }
        if (voice != null) {
            note.setVoice(voice.strip());
        }
        bookConlang.saveSpeakers(book.manuscript, languageId, speakers);
        answer(context, speakersRow(speakers));
    }

    @Command(commandName = REMOVE_SPEAKER, linkedCommand = SPEAKERS, humanNameText = "Remove a character of the speakers", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Removes a character from the notes of the speakers.",
            llmDescriptionText = "Removes the character (and its note) from the speakers. Destructive, asks the user. Returns the speakers.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, descriptionText = "The name or an alias of the character"),
                    @CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE_FIRST)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the speakers"))
    public void removeSpeaker(CommandContext context) throws Exception {
        Book book = new Book(context);
        String name = required(context.get("name", String.class), "the name of the character");
        String languageId = speakersLanguage(book, context);
        Speakers speakers = bookConlang.speakers(book.manuscript, languageId);
        CharacterNote note = speakers.find(name);
        if (note == null) {
            throw new IllegalArgumentException("There is no character " + name + " in the speakers");
        }
        if (!context.confirm("Remove " + note.getName() + " from the speakers of the book?")) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was removed");
            }
            return;
        }
        speakers.getCharacters().remove(note);
        bookConlang.saveSpeakers(book.manuscript, languageId, speakers);
        context.setReturn(GSON.toJson(speakersRow(speakers)));
    }

    private static JsonObject speakersRow(Speakers speakers) {
        JsonObject row = new JsonObject();
        row.addProperty("mode", speakers.getMode() == SpeakersMode.ONLY ? "only" : "everyone-except");
        JsonArray list = new JsonArray();
        for (CharacterNote note : speakers.getCharacters()) {
            JsonObject c = new JsonObject();
            c.addProperty("name", note.getName());
            c.add("aliases", array(note.getAliases()));
            c.addProperty("gender", note.getGender());
            c.addProperty("voice", note.getVoice());
            c.addProperty("listed", note.isListed());
            c.addProperty("speaksTheLanguage", speakers.speaks(note.getName()));
            c.addProperty("understands", note.getLevel() == null ? "" : note.getLevel().key());
            list.add(c);
        }
        row.add("characters", list);
        return row;
    }

    // ---- reading

    @Command(commandName = CHECK_TRANSLATION, linkedCommand = LIST_PASSAGES, humanNameText = "Check a translation", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "Checks the translated speech of a part against the lexicon. Reads only.",
            llmDescriptionText = "Checks, without a model, every translated speech of the part: the words that are not in the lexicon, a compound of known words, a known word with the language's affixes, or a name of the part "
                    + "(a word that is none of these was invented and not reported) and whether the author edited the speech since. Reads only.",
            signature = @CommandParameterSignature({@CommandParameter(name = "part", type = String.class, descriptionText = PARAM_PART)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the findings"))
    public void checkTranslation(CommandContext context) throws Exception {
        Book book = new Book(context);
        Book.Located located = book.locate(context.get("part", String.class), null);
        BookSettings settings = translation.settings(book.manuscript);
        Language language = translation.languageOf(book.manuscript, settings);
        if (language == null) {
            throw new IllegalStateException("The book has no language");
        }
        ChatMessage part = translation.find(located.message());
        String stored = StringUtils.defaultString(part.getResponse());
        Reverse.Result read = Reverse.toModel(stored, passages.of(List.of(part)).getOrDefault(part.getId(), List.of()));
        JsonArray rows = new JsonArray();
        for (Reverse.Located item : passages.of(List.of(part)).getOrDefault(part.getId(), List.of())) {
            String conlang = slice(stored, item.start(), item.end());
            JsonObject row = new JsonObject();
            row.addProperty("conlang", conlang);
            row.addProperty("english", item.span().getSource());
            row.addProperty("editedByTheAuthor", item.span().isEdited(conlang));
            List<String> unknown = new LexiconCheck(language, List.of(), read.text()).unknown(conlang);
            row.add("wordsNotInTheLexicon", array(unknown));
            rows.add(row);
        }
        JsonObject result = new JsonObject();
        result.addProperty("part", located.position());
        result.add("speeches", rows);
        if (rows.isEmpty()) {
            result.addProperty("hint", "The part has no translated speech");
        }
        answer(context, result);
    }

    @Command(commandName = GET_SOURCE, linkedCommand = LIST_PASSAGES, humanNameText = "Get a part as the model reads it", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "The text of a part as the model reads it (the English). Reads only.",
            llmDescriptionText = "Returns the text of the part as every model reads it: the translated speeches are their English again. The other read commands of the application give this text too; "
                    + "this is the one that says so. In slices of 20000 characters. Reads only.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, descriptionText = PARAM_PART),
                    @CommandParameter(name = "offset", type = Integer.class, optional = true, defaultValue = "0", min = "0", descriptionText = "Where the slice starts (characters)")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the text"))
    public void getSource(CommandContext context) throws Exception {
        Book book = new Book(context);
        Book.Located located = book.locate(context.get("part", String.class), null);
        String text = StringUtils.defaultString(partTexts.forModel(translation.find(located.message())));
        Integer offsetValue = context.get("offset", Integer.class);
        int from = Math.min(Math.max(0, offsetValue == null ? 0 : offsetValue), text.length());
        int to = Math.min(text.length(), from + MAX_TEXT);
        JsonObject result = new JsonObject();
        result.addProperty("part", located.position());
        result.addProperty("text", text.substring(from, to));
        result.addProperty("offset", from);
        result.addProperty("length", text.length());
        answer(context, result);
    }

    @Command(commandName = LIST_PASSAGES, linkedCommand = GET_SOURCE, humanNameText = "List translated speeches", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "The translated speeches of a part with their English. Reads only.",
            llmDescriptionText = "Lists the translated speeches of the part as JSON (all the languages of the book): the conlang text, its English source, the language, who said it, whether the listener understands it, the register, the model that translated it "
                    + "and whether the author edited it since (an edited speech keeps the author's words). Reads only.",
            signature = @CommandParameterSignature({@CommandParameter(name = "part", type = String.class, descriptionText = PARAM_PART)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the speeches"))
    public void listPassages(CommandContext context) throws Exception {
        Book book = new Book(context);
        Book.Located located = book.locate(context.get("part", String.class), null);
        ChatMessage part = translation.find(located.message());
        String stored = StringUtils.defaultString(part.getResponse());
        JsonArray rows = new JsonArray();
        List<LanguageRef> bookLanguages = bookConlang.languages(book.manuscript);
        int number = 0;
        for (AnchoredPassage passage : passages.passagesOf(part)) {
            Reverse.Located item = PassageAccess.locate(passage);
            if (item == null) {
                continue;
            }
            Span span = item.span();
            String conlang = slice(stored, item.start(), item.end());
            JsonObject row = new JsonObject();
            row.addProperty("speech", ++number);
            row.addProperty("conlang", conlang);
            row.addProperty("english", span.getSource());
            row.addProperty("language", bookLanguages.stream().filter(l -> l.id().equals(span.getLanguage())).map(LanguageRef::name).findFirst().orElse(StringUtils.defaultString(span.getLanguage())));
            row.addProperty("speaker", span.getSpeaker());
            row.addProperty("understood", span.isUnderstood());
            if (!span.getRegister().isBlank()) {
                row.addProperty("register", span.getRegister());
            }
            row.addProperty("model", span.getModel());
            row.addProperty("editedByTheAuthor", span.isEdited(conlang));
            rows.add(row);
        }
        JsonObject result = new JsonObject();
        result.addProperty("part", located.position());
        result.add("speeches", rows);
        answer(context, result);
    }

    @Command(commandName = EDIT_SOURCE, linkedCommand = LIST_PASSAGES, humanNameText = "Edit the source of a speech", dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Sets the English a translated speech means, after the translation was fixed by hand.",
            llmDescriptionText = "Sets the English source of one translated speech of a part (its number is the 'speech' of conlang-list-passages): the English the models read in place of the translation. "
                    + "Use it when the translation was corrected by hand (a native speaker's edit) and the source says something else now, or when the model got the English wrong. The text of the part is not changed; "
                    + "the translation as it is now counts as the one this source belongs to, so a speech marked as edited is not any more. Replaces the old source. Returns the speech.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, descriptionText = PARAM_PART),
                    @CommandParameter(name = "speech", type = Integer.class, min = "1", descriptionText = "The number of the translated speech in the part, as conlang-list-passages lists it"),
                    @CommandParameter(name = "source", type = String.class, descriptionText = "The English the speech means, without the quotation marks")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the speech with its new source"))
    public void editSource(CommandContext context) throws Exception {
        Book book = new Book(context);
        Book.Located located = book.locate(context.get("part", String.class), null);
        ChatMessage part = translation.find(located.message());
        int wanted = context.get("speech", Integer.class);
        int at = 0;
        AnchoredPassage chosen = null;
        for (AnchoredPassage passage : passages.passagesOf(part)) {
            if (PassageAccess.locate(passage) != null && ++at == wanted) {
                chosen = passage;
                break;
            }
        }
        if (chosen == null) {
            throw new IllegalArgumentException("The part has no translated speech number " + wanted + " (" + LIST_PASSAGES + " lists them)");
        }
        Span span = new TranslationWriter(com.github.enerccio.marginalia.extensions.conlangtranslator.text.ConlangKind.ID).editSource(chosen.getUuid(), required(context.get("source", String.class), "the source"));
        JsonObject result = new JsonObject();
        result.addProperty("part", located.position());
        result.addProperty("speech", wanted);
        result.addProperty("english", span.getSource());
        answer(context, result);
    }

    // ---- the understanding of the language

    @Command(commandName = SET_UNDERSTANDING_PROMPT, linkedCommand = SPEAKERS, humanNameText = "Insert who does not follow the language", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Chooses whether the levels of understanding of the characters are inserted into the prompt of the story, and where.",
            llmDescriptionText = "Sets whether the book inserts the levels of understanding (set with conlang-set-speaker) into the prompt of the story by itself (in-prompt; turn it off to place the "
                    + "{{command::conlang-understanding}} macro in a template instead) and how many messages from the end of the payload it goes (depth, 0 = the very end, 1 = before the instructions of the turn). "
                    + "Pass only what to change. Returns the settings.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "inPrompt", type = Boolean.class, optional = true, descriptionText = "Insert the block into the prompt of the story"),
                    @CommandParameter(name = "depth", type = Integer.class, optional = true, descriptionText = "Messages from the end of the payload, 0 or more")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: inPrompt and depth"))
    public void setUnderstandingPrompt(CommandContext context) throws Exception {
        Book book = new Book(context);
        UnderstandingSettings settings = bookConlang.understanding(book.manuscript);
        Boolean inPrompt = context.get("inPrompt", Boolean.class);
        Integer depth = context.get("depth", Integer.class);
        if (inPrompt != null) {
            settings.setInPrompt(inPrompt);
        }
        if (depth != null) {
            settings.setDepth(depth);
        }
        bookConlang.saveUnderstanding(book.manuscript, settings);
        JsonObject result = new JsonObject();
        result.addProperty("inPrompt", settings.isInPrompt());
        result.addProperty("depth", settings.getDepth());
        answer(context, result);
    }

    // ---- macros

    @Command(commandName = UNDERSTANDING, linkedCommand = SPEAKERS, humanNameText = "Language understanding block", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true, availableInMacros = true,
            descriptionText = "Which characters do not follow which language, as plain text, the way the prompt gets it. Made for the {{command::conlang-understanding::<characters>}} macro of a template: the argument "
                    + "is an optional list of characters to keep, all of them when left out. Reads only.",
            llmDescriptionText = "Returns the text the prompt gets about which characters do not follow which language (the levels set with conlang-set-speaker); characters is an optional list of "
                    + "names to keep, separated by commas. Empty when no level is set. Reads only.",
            signature = @CommandParameterSignature({@CommandParameter(name = "characters", type = String.class, optional = true, descriptionText = "The characters to keep, separated by commas; all of them when left out")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "Text: the levels that say something; empty when none is set"))
    public void understanding(CommandContext context) throws Exception {
        Book book = new Book(context);
        context.setReturn(UnderstandingBlock.build(bookConlang.understandingEntries(book.manuscript), context.get("characters", String.class)));
    }

    @Command(commandName = LEXICON, humanNameText = "Lexicon block", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true, availableInMacros = true,
            descriptionText = "The lexicon of a language as plain text, the way the translation request sends it. Made for the {{command::conlang-lexicon::<language>}} macro. Reads only.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "Text: the words under their categories"))
    public void lexicon(CommandContext context) throws Exception {
        context.setReturn(LexiconText.render(language(context), null));
    }

    @Command(commandName = RULES, humanNameText = "Rules block", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true, availableInMacros = true,
            descriptionText = "The rules of a language that the translator reads, as plain text. Made for the {{command::conlang-rules::<language>}} macro. Reads only.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "Text: the sections the translating model gets"))
    public void rules(CommandContext context) throws Exception {
        Language language = language(context);
        StringBuilder text = new StringBuilder();
        language.getRules().stream()
                .filter(section -> section.getAudience() == com.github.enerccio.marginalia.extensions.conlangtranslator.model.Audience.MODEL)
                .forEach(section -> text.append("## ").append(section.getTitle()).append("\n").append(section.getText().strip()).append("\n\n"));
        context.setReturn(text.toString().strip());
    }

    // ---- helpers

    /** The parts of the active branch that have text. */
    private List<ChatMessage> textParts(Book book) throws Exception {
        return book.path((Integer) null).stream().filter(part -> part.getResponse() != null && !part.getResponse().isBlank()).toList();
    }

    /**
     * The parts from..to of the active branch (their positions are the ones of the other commands), those the mode works on. A single part when only
     * 'from' is given for a revert or a range without 'to'.
     */
    private List<RangePart> selectRange(Book book, String from, String to, Mode mode, String languageId) throws Exception {
        List<ChatMessage> path = book.path((Integer) null);
        int first = StringUtils.isBlank(from) ? 1 : book.locate(from, null).position();
        int last = StringUtils.isBlank(to) ? (StringUtils.isBlank(from) ? path.size() : first) : book.locate(to, null).position();
        if (last < first) {
            throw new IllegalArgumentException("The range ends before it starts");
        }
        List<ChatMessage> branch = path.stream().filter(part -> part.getResponse() != null && !part.getResponse().isBlank()).toList();
        Map<Long, List<Reverse.Located>> translated = passages.of(branch);
        List<RangePart> all = new ArrayList<>();
        for (ChatMessage part : branch) {
            Set<String> languagesOfPart = new HashSet<>();
            translated.getOrDefault(part.getId(), List.of()).forEach(located -> languagesOfPart.add(located.span().getLanguage()));
            all.add(new RangePart(path.indexOf(part) + 1, part, translated.containsKey(part.getId()), languagesOfPart));
        }
        return RangeLoop.select(mode, all, first, last, languageId);
    }

    private static String slice(String text, int start, int end) {
        return start < 0 || end > text.length() || start >= end ? "" : text.substring(start, end);
    }
}
