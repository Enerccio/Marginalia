package com.github.enerccio.marginalia.extensions.reallangtranslator.commands;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read.ManuscriptReadSupport;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.extensions.InheritedSettingValues;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.reallangtranslator.ReallangKind;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.BookSettings;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Presets;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.RealLanguage;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Script;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.BookReal;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.LanguageLibrary;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.RealInherited;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.TranslationService;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.TranslationService.Outcome;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.CharacterNote;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Span;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Speakers;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.SpeakersMode;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.PassageAccess;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.Mode;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.PartResult;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.RangePart;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.Summary;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.TranslationWriter;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Reverse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Everything the screens of the plugin do, as commands for the command bar, for agents and for templates: the library of languages, the language of
 * the book, who speaks it, translating a part, a range, a sentence and a text, putting the English back, and the two reads an agent needs (the text of
 * a part as a model reads it and the translated passages of a part). They call the services the dialogs call.
 * <p>
 * A part is addressed as in the other commands of the application: its handle, or its position on the active branch (the first part is 1). A language
 * is addressed by its name or its id.
 * <p>
 * The library is the user's, and a language is a few lines of rules, so an agent run changes it directly (there is no working copy of it as there is
 * for a constructed language with its lexicon); the book's settings and the passages are the book's, which the working copy of a run carries.
 */
public class ReallangCommands extends ManuscriptReadSupport {

    public static final String LIST = "reallang-list";
    public static final String CREATE = "reallang-create";
    public static final String UPDATE = "reallang-update";
    public static final String DELETE = "reallang-delete";
    public static final String SET_BOOK_LANGUAGE = "reallang-set-book-language";
    public static final String STATUS = "reallang-status";
    public static final String TRANSLATE_PART = "reallang-translate-part";
    public static final String TRANSLATE_RANGE = "reallang-translate-range";
    public static final String REVERT = "reallang-revert-to-source";
    public static final String TRANSLATE_TEXT = "reallang-translate-text";
    public static final String TRANSLATE_SENTENCE = "reallang-translate-sentence";
    public static final String SPEAKERS = "reallang-speakers";
    public static final String SET_SPEAKERS_MODE = "reallang-set-speakers-mode";
    public static final String SET_SPEAKER = "reallang-set-speaker";
    public static final String REMOVE_SPEAKER = "reallang-remove-speaker";
    public static final String GET_SOURCE = "reallang-get-source";
    public static final String LIST_PASSAGES = "reallang-list-passages";

    private static final String PARAM_PART = "The part: its handle, or its position on the active branch (the first part is 1)";
    private static final String PARAM_LANGUAGE = "The language: its name (the case does not matter) or its id, see reallang-list";
    private static final String RESULT_JSON = "JSON: the result";

    /** The longest text a command returns whole (characters). */
    private static final int MAX_TEXT = 20000;

    private final TranslationService translation;
    private final BookReal bookReal;
    private final LanguageLibrary library;
    private final PassageAccess passages = new PassageAccess(ReallangKind.ID);

    @Autowired
    private PartTextRegistry partTexts;
    @Autowired
    private ManuscriptService books;

    public ReallangCommands(TranslationService translation, BookReal bookReal, LanguageLibrary library) {
        this.translation = translation;
        this.bookReal = bookReal;
        this.library = library;
    }

    private Manuscript scopeBook(CommandContext context) {
        try {
            return context.getScope().getManuscript();
        } catch (Exception e) {
            return null;
        }
    }

    // ---- the library

    @Command(commandName = LIST, linkedCommand = CREATE, humanNameText = "List real languages", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "Lists the real languages of the library and the presets a new one can start from. Reads only.",
            llmDescriptionText = "Lists the real languages (Japanese and the like) of the user's library as JSON: id, name, the language as the model names it, the script, whether the honorifics of names are "
                    + "written and whether a reading is added, and the three rule texts (politeness, names, other). Also lists the ids of the presets reallang-create can start from. Reads only.",
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void list(CommandContext context) throws Exception {
        JsonArray list = new JsonArray();
        for (RealLanguage language : library.languages()) {
            list.add(row(language));
        }
        JsonArray presets = new JsonArray();
        Presets.all().forEach(preset -> presets.add(preset.id()));
        JsonObject result = new JsonObject();
        result.add("languages", list);
        result.add("presets", presets);
        if (list.isEmpty()) {
            result.addProperty("hint", "There is no language yet, reallang-create makes one");
        }
        answer(context, result);
    }

    @Command(commandName = CREATE, linkedCommand = LIST, humanNameText = "Create a real language", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Adds a real language to the library, optionally from a preset.",
            llmDescriptionText = "Adds a real language to the library. 'preset' starts it as ja-kana-kanji, ja-kana, ja-romaji (Japanese with their politeness and name rules) or other (blank); the fields you give override the "
                    + "preset's. The name must not be taken. Returns the language.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, optional = true, descriptionText = "What the language is called; the preset's name when left out"),
                    @CommandParameter(name = "preset", type = String.class, optional = true, defaultValue = "other", descriptionText = "ja-kana-kanji, ja-kana, ja-romaji or other"),
                    @CommandParameter(name = "tongue", type = String.class, optional = true, descriptionText = "The language as the model names it (\"Japanese\")"),
                    @CommandParameter(name = "script", type = String.class, optional = true, allowed = {"kana-kanji", "kana", "romaji", "native"}, descriptionText = "The script of the translation"),
                    @CommandParameter(name = "honorifics", type = Boolean.class, optional = true, descriptionText = "Write the honorific suffixes of names"),
                    @CommandParameter(name = "reading", type = Boolean.class, optional = true, descriptionText = "Also write how the translation is read (romaji)"),
                    @CommandParameter(name = "politeness", type = String.class, optional = true, descriptionText = "How the level of speech is chosen"),
                    @CommandParameter(name = "names", type = String.class, optional = true, descriptionText = "How names are written"),
                    @CommandParameter(name = "rules", type = String.class, optional = true, descriptionText = "Anything else the translator holds to")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void create(CommandContext context) throws Exception {
        String preset = StringUtils.defaultIfBlank(context.get("preset", String.class), "other").strip();
        if (Presets.all().stream().noneMatch(known -> known.id().equals(preset))) {
            throw new IllegalArgumentException("The preset is ja-kana-kanji, ja-kana, ja-romaji or other, not " + preset);
        }
        RealLanguage language = Presets.make(preset);
        fill(context, language);
        if (language.getName().isBlank()) {
            throw new IllegalArgumentException("Give the name of the language");
        }
        ensureFreeName(language, null);
        library.store(language);
        answer(context, row(language));
    }

    @Command(commandName = UPDATE, linkedCommand = LIST, humanNameText = "Change a real language", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Changes the fields of a real language.",
            llmDescriptionText = "Changes the fields you give of a language of the library (name, tongue, script, honorifics, reading, politeness, names, rules); the rest is left. Parts already translated stay as they are. "
                    + "Returns the language.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "name", type = String.class, optional = true, descriptionText = "The new name"),
                    @CommandParameter(name = "tongue", type = String.class, optional = true, descriptionText = "The language as the model names it"),
                    @CommandParameter(name = "script", type = String.class, optional = true, allowed = {"kana-kanji", "kana", "romaji", "native"}, descriptionText = "The script of the translation"),
                    @CommandParameter(name = "honorifics", type = Boolean.class, optional = true, descriptionText = "Write the honorific suffixes of names"),
                    @CommandParameter(name = "reading", type = Boolean.class, optional = true, descriptionText = "Also write how the translation is read"),
                    @CommandParameter(name = "politeness", type = String.class, optional = true, descriptionText = "How the level of speech is chosen"),
                    @CommandParameter(name = "names", type = String.class, optional = true, descriptionText = "How names are written"),
                    @CommandParameter(name = "rules", type = String.class, optional = true, descriptionText = "Anything else the translator holds to")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void update(CommandContext context) throws Exception {
        RealLanguage language = library.resolve(context.get("language", String.class));
        fill(context, language);
        if (language.getName().isBlank()) {
            throw new IllegalArgumentException("The name of the language can not be empty");
        }
        ensureFreeName(language, language.getId());
        library.store(language);
        answer(context, row(language));
    }

    @Command(commandName = DELETE, linkedCommand = LIST, humanNameText = "Delete a real language", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Deletes a real language from the library.",
            llmDescriptionText = "Deletes the language from the library. Books that name it have no language then; the translated speeches stay as they are. Destructive, asks the user. Returns what was done.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void delete(CommandContext context) throws Exception {
        RealLanguage language = library.resolve(context.get("language", String.class));
        if (!context.confirm("Delete the language " + language.getName() + " from your library?")) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was deleted");
            }
            return;
        }
        library.remove(language.getId());
        context.setReturn("{\"deleted\":\"" + language.getName().replace("\"", "'") + "\"}");
    }

    @Command(commandName = SET_BOOK_LANGUAGE, linkedCommand = LIST, humanNameText = "Set the real language of the book", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Chooses the real language the open book is translated into.",
            llmDescriptionText = "Sets the language of the book (what the Language field of the book's Extension Settings holds). An empty language removes the choice, the book then inherits the folder's or the user's. "
                    + "Returns what applies to the book.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE + "; empty = inherit")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void setBookLanguage(CommandContext context) throws Exception {
        Manuscript book = scopeBook(context);
        if (book == null) {
            throw new IllegalStateException("No book is open");
        }
        String reference = context.get("language", String.class);
        String id = StringUtils.isBlank(reference) ? null : library.resolve(reference).getId();
        books.update(book, current -> InheritedSettingValues.set(current, RealInherited.LANGUAGE, id));
        String applies = books.resolveExtension(book, RealInherited.LANGUAGE).value();
        RealLanguage effective = StringUtils.isBlank(applies) ? null : library.languages().stream().filter(l -> l.getId().equals(applies)).findFirst().orElse(null);
        JsonObject row = new JsonObject();
        row.addProperty("bookChoice", StringUtils.defaultString(id));
        row.addProperty("appliesToTheBook", effective == null ? "none" : effective.getName());
        answer(context, row);
    }

    // ---- the state

    @Command(commandName = STATUS, linkedCommand = TRANSLATE_PART, humanNameText = "Real language status", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "The real language of the book and which parts are translated. Reads only.",
            llmDescriptionText = "Returns as JSON the language the book is translated into, whether parts are translated by themselves, how many parts of the active branch have text, how many have translated speech "
                    + "(and how many of those the author edited), and the positions of the parts that have none. A book with no language translates nothing. Reads only.",
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the state"))
    public void status(CommandContext context) throws Exception {
        Book book = new Book(context);
        BookSettings settings = translation.settings(book.manuscript);
        RealLanguage language = translation.languageOf(settings);
        JsonObject result = new JsonObject();
        result.addProperty("language", language == null ? "none" : language.getName());
        result.addProperty("translateByItself", settings.isAuto());
        List<ChatMessage> branch = textParts(book);
        Map<Long, List<Reverse.Located>> located = passages.of(branch);
        List<ChatMessage> path = book.path((Integer) null);
        int translated = 0;
        int edited = 0;
        JsonArray untranslated = new JsonArray();
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
        answer(context, result);
    }

    // ---- translating

    @Command(commandName = TRANSLATE_PART, linkedCommand = STATUS, humanNameText = "Translate a part into the real language", async = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Translates the speech of a part into the real language of the book.",
            llmDescriptionText = "Asks the translating model to translate the direct speech of the part into the language of the book and writes the translations into the part (the quotation marks and the narration stay). "
                    + "Each translated speech keeps its English as its source, which the model reads back everywhere; the history of the part gets a version. A part that is translated already is translated again from its English. "
                    + "Returns what came of it: speeches written, speeches that could not be used, and translations that are not in the script the language asks for (wrongScript). Needs a language on the book "
                    + "(reallang-set-book-language) and a model.",
            signature = @CommandParameterSignature({@CommandParameter(name = "part", type = String.class, descriptionText = PARAM_PART)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what came of it"))
    public void translatePart(CommandContext context) throws Exception {
        Book book = new Book(context);
        Book.Located located = book.locate(context.get("part", String.class), null);
        Outcome outcome = translation.translate(book.manuscript, located.message());
        JsonObject result = new JsonObject();
        result.addProperty("part", located.position());
        result.addProperty("status", outcome.status().name().toLowerCase(Locale.ROOT).replace('_', '-'));
        result.addProperty("speechesWritten", outcome.translated());
        result.addProperty("speechesDropped", outcome.dropped());
        result.addProperty("wrongScript", outcome.wrongScript());
        if (!outcome.note().isBlank()) {
            result.addProperty("note", outcome.note());
        }
        context.setReturn(GSON.toJson(result));
    }

    @Command(commandName = TRANSLATE_RANGE, linkedCommand = STATUS, humanNameText = "Translate parts into the real language", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Translates the parts of a range, or translates the translated ones again.",
            llmDescriptionText = "Translates the speech in the parts from..to of the active branch, one part after the other (a part is minutes of the model's time). mode is 'new' (default: only the parts that have no "
                    + "translated speech) or 'again' (the parts that have translated speech are translated again from their English; what the author edited stays). Without 'to' only the part 'from' is done, "
                    + "without 'from' the whole branch. A part that fails is counted and the run goes on; no language or no model ends it. Asks the user for 'again'. Returns the summary.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "from", type = String.class, optional = true, descriptionText = "The first part: its handle or position; the first part of the story when left out"),
                    @CommandParameter(name = "to", type = String.class, optional = true, descriptionText = "The last part: its handle or position; the last part of the branch when left out"),
                    @CommandParameter(name = "mode", type = String.class, optional = true, allowed = {"new", "again"}, defaultValue = "new", descriptionText = "new = the parts not translated yet, again = translate the translated parts again")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the summary"))
    public void translateRange(CommandContext context) throws Exception {
        Book book = new Book(context);
        String modeText = StringUtils.defaultIfBlank(context.get("mode", String.class), "new").strip().toLowerCase(Locale.ROOT);
        Mode mode = switch (modeText) {
            case "new" -> Mode.TRANSLATE_NEW;
            case "again", "retranslate" -> Mode.RETRANSLATE;
            default -> throw new IllegalArgumentException("The mode is new or again, not " + modeText);
        };
        List<RangePart> parts = selectRange(book, context.get("from", String.class), context.get("to", String.class), mode);
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
        run(context, book, mode, parts);
    }

    @Command(commandName = REVERT, linkedCommand = STATUS, humanNameText = "Put the English back", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Puts the English back into translated parts.",
            llmDescriptionText = "Puts the English source back into the translated speeches of the parts from..to (a single part when 'to' is left out, the whole branch when both are): the passages are removed, "
                    + "the history keeps the translated version. Speeches the author edited stay as they are. Asks the user. Returns the summary.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "from", type = String.class, optional = true, descriptionText = "The first part: its handle or position; the first part of the story when left out"),
                    @CommandParameter(name = "to", type = String.class, optional = true, descriptionText = "The last part: its handle or position; only 'from' when left out")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the summary"))
    public void revert(CommandContext context) throws Exception {
        Book book = new Book(context);
        List<RangePart> parts = selectRange(book, context.get("from", String.class), context.get("to", String.class), Mode.REVERT);
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
        run(context, book, Mode.REVERT, parts);
    }

    private void run(CommandContext context, Book book, Mode mode, List<RangePart> parts) {
        AtomicBoolean stop = new AtomicBoolean();
        int[] done = {0};
        Summary summary = RangeLoop.run(parts, part -> {
            if (context.isTerminated()) {
                stop.set(true);
                return PartResult.nothing();
            }
            context.setStatus("Part " + (done[0] + 1) + " of " + parts.size());
            PartResult result = apply(book.manuscript, mode, part);
            done[0]++;
            return result;
        }, stop, null);
        JsonObject row = new JsonObject();
        row.addProperty("planned", summary.planned());
        row.addProperty("processed", summary.processed());
        row.addProperty("partsChanged", summary.changed());
        row.addProperty("speeches", summary.speeches());
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

    private PartResult apply(Manuscript book, Mode mode, ChatMessage part) throws Exception {
        if (mode == Mode.REVERT) {
            TranslationWriter.Reverted reverted = translation.revert(book, part);
            return reverted.reverted() == 0 ? PartResult.nothing() : PartResult.done(reverted.reverted(), 0, reverted.kept());
        }
        Outcome outcome = translation.translate(book, part);
        return switch (outcome.status()) {
            case TRANSLATED -> PartResult.done(outcome.translated(), 0, 0);
            case NOTHING_TO_TRANSLATE, EMPTY -> PartResult.nothing();
            case NO_LANGUAGE, NO_MODEL -> PartResult.abort(outcome.note());
            case BAD_ANSWER, OVERLAPS, CHANGED -> PartResult.failed(outcome.note());
        };
    }

    @Command(commandName = TRANSLATE_TEXT, linkedCommand = STATUS, humanNameText = "Translate a text into the real language", async = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Translates a text into the language of the book without writing it into the story.",
            llmDescriptionText = "Translates a phrase, a name or a short text into the language of the book (the quick translation) and returns the translation, how it is read (when the language asks for a reading) and "
                    + "a note on the model's choices. Nothing is written into the story. context says who speaks to whom in what situation.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class, descriptionText = "The English text"),
                    @CommandParameter(name = "context", type = String.class, optional = true, descriptionText = "Who says it, to whom, in what situation")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the translation"))
    public void translateText(CommandContext context) throws Exception {
        Book book = new Book(context);
        TranslationService.Quick quick = bookReal.quickTranslate(book.manuscript, required(context.get("text", String.class), "the text"),
                StringUtils.defaultString(context.get("context", String.class)));
        JsonObject result = new JsonObject();
        result.addProperty("status", quick.status().name().toLowerCase(Locale.ROOT).replace('_', '-'));
        if (quick.status() == TranslationService.Status.TRANSLATED) {
            result.addProperty("translation", quick.text());
            if (!quick.reading().isBlank()) {
                result.addProperty("reading", quick.reading());
            }
            result.addProperty("choice", quick.choice());
            if (quick.wrongScript()) {
                result.addProperty("note", "The translation is not in the script the language asks for");
            }
        }
        context.setReturn(GSON.toJson(result));
    }

    @Command(commandName = TRANSLATE_SENTENCE, linkedCommand = STATUS, humanNameText = "Translate a sentence of a part into the real language", async = true, dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Translates one sentence of a part into the language, in the text of the part.",
            llmDescriptionText = "Translates one sentence you quote from the part (typically one the author wrote, outside a speech the translator found) into the language of the book and replaces it in the text of the part; "
                    + "its English is kept as the source. The sentence must occur exactly once in the part and must not hold an already translated speech. Returns what came of it.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, descriptionText = PARAM_PART),
                    @CommandParameter(name = "sentence", type = String.class, descriptionText = "The sentence, exactly as the part has it")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what came of it"))
    public void translateSentence(CommandContext context) throws Exception {
        Book book = new Book(context);
        Book.Located located = book.locate(context.get("part", String.class), null);
        String sentence = required(context.get("sentence", String.class), "the sentence");
        ChatMessage part = translation.find(located.message());
        String text = StringUtils.defaultString(part.getResponse());
        int start = text.indexOf(sentence);
        if (start < 0) {
            throw new IllegalArgumentException("The part does not have that sentence; quote it exactly (reallang-get-source shows the text)");
        }
        if (text.indexOf(sentence, start + 1) >= 0) {
            throw new IllegalArgumentException("The sentence occurs more than once in the part; quote more of it");
        }
        Outcome outcome = translation.translateSelection(book.manuscript, part, text, start, start + sentence.length());
        JsonObject result = new JsonObject();
        result.addProperty("part", located.position());
        result.addProperty("status", outcome.status().name().toLowerCase(Locale.ROOT).replace('_', '-'));
        result.addProperty("wrongScript", outcome.wrongScript());
        if (!outcome.note().isBlank()) {
            result.addProperty("note", outcome.note());
        }
        context.setReturn(GSON.toJson(result));
    }

    // ---- who speaks the language in this book

    @Command(commandName = SPEAKERS, linkedCommand = SET_SPEAKER, humanNameText = "Speakers of the real language", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "Who speaks the language in this book. Reads only.",
            llmDescriptionText = "Returns who speaks the language of the book: the mode (everyone-except = everybody speaks it but the listed characters, only = only the listed ones speak it) and the characters "
                    + "with their aliases, gender, voice and whether they are listed. This is the default; the text of a part overrides it. Reads only.",
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the speakers"))
    public void speakers(CommandContext context) throws Exception {
        answer(context, speakersRow(bookReal.speakers(new Book(context).manuscript)));
    }

    @Command(commandName = SET_SPEAKERS_MODE, linkedCommand = SPEAKERS, humanNameText = "Set who speaks the real language", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Chooses whether everybody speaks the language except the listed characters, or only the listed ones.",
            llmDescriptionText = "Sets the mode of the speakers of the book: everyone-except (everybody speaks the language but the listed characters) or only (only the listed characters speak it). Returns the speakers.",
            signature = @CommandParameterSignature({@CommandParameter(name = "mode", type = String.class, allowed = {"everyone-except", "only"}, descriptionText = "everyone-except or only")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the speakers"))
    public void setSpeakersMode(CommandContext context) throws Exception {
        Book book = new Book(context);
        String mode = required(context.get("mode", String.class), "the mode").toLowerCase(Locale.ROOT);
        Speakers speakers = bookReal.speakers(book.manuscript);
        speakers.setMode(switch (mode) {
            case "everyone-except", "everyone_except" -> SpeakersMode.EVERYONE_EXCEPT;
            case "only" -> SpeakersMode.ONLY;
            default -> throw new IllegalArgumentException("The mode is everyone-except or only, not " + mode);
        });
        bookReal.saveSpeakers(book.manuscript, speakers);
        answer(context, speakersRow(speakers));
    }

    @Command(commandName = SET_SPEAKER, linkedCommand = SPEAKERS, humanNameText = "Set a character of the real language speakers", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Adds a character to the speakers or changes the note on one.",
            llmDescriptionText = "Adds a character, or changes the one with this name or alias: aliases (separated by commas), gender, voice (how the character sounds: age, rank, manner, how they speak to others; the level of "
                    + "speech follows it) and listed (in everyone-except mode a listed character does not speak the language; in only mode a listed character does). Pass only what to change. Returns the speakers.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, descriptionText = "The name of the character (or an alias of one that is there)"),
                    @CommandParameter(name = "listed", type = Boolean.class, optional = true, descriptionText = "Whether the character is on the list of the mode"),
                    @CommandParameter(name = "aliases", type = String.class, optional = true, descriptionText = "Other names, separated by commas; empty removes them"),
                    @CommandParameter(name = "gender", type = String.class, optional = true, descriptionText = "The gender, free text"),
                    @CommandParameter(name = "voice", type = String.class, optional = true, descriptionText = "How the character speaks")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the speakers"))
    public void setSpeaker(CommandContext context) throws Exception {
        Book book = new Book(context);
        String name = required(context.get("name", String.class), "the name of the character");
        Speakers speakers = bookReal.speakers(book.manuscript);
        CharacterNote note = speakers.find(name);
        if (note == null) {
            note = new CharacterNote(name);
            speakers.getCharacters().add(note);
        }
        Boolean listed = context.get("listed", Boolean.class);
        String aliases = context.get("aliases", String.class);
        String gender = context.get("gender", String.class);
        String voice = context.get("voice", String.class);
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
        bookReal.saveSpeakers(book.manuscript, speakers);
        answer(context, speakersRow(speakers));
    }

    @Command(commandName = REMOVE_SPEAKER, linkedCommand = SPEAKERS, humanNameText = "Remove a character of the real language speakers", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Removes a character from the notes of the speakers.",
            llmDescriptionText = "Removes the character (and its note) from the speakers. Destructive, asks the user. Returns the speakers.",
            signature = @CommandParameterSignature({@CommandParameter(name = "name", type = String.class, descriptionText = "The name or an alias of the character")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the speakers"))
    public void removeSpeaker(CommandContext context) throws Exception {
        Book book = new Book(context);
        String name = required(context.get("name", String.class), "the name of the character");
        Speakers speakers = bookReal.speakers(book.manuscript);
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
        bookReal.saveSpeakers(book.manuscript, speakers);
        context.setReturn(GSON.toJson(speakersRow(speakers)));
    }

    // ---- reading

    @Command(commandName = GET_SOURCE, linkedCommand = LIST_PASSAGES, humanNameText = "Get a part as the model reads it (real language)", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "The text of a part as the model reads it (the English). Reads only.",
            llmDescriptionText = "Returns the text of the part as every model reads it: the translated speeches are their English again. In slices of 20000 characters. Reads only.",
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

    @Command(commandName = LIST_PASSAGES, linkedCommand = GET_SOURCE, humanNameText = "List translated speeches (real language)", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "The translated speeches of a part with their English. Reads only.",
            llmDescriptionText = "Lists the translated speeches of the part as JSON: the translated text, how it is read (when there is a reading), its English source, who said it, whether the listener understands it, "
                    + "the register, the model that translated it and whether the author edited it since (an edited speech keeps the author's words). Reads only.",
            signature = @CommandParameterSignature({@CommandParameter(name = "part", type = String.class, descriptionText = PARAM_PART)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the speeches"))
    public void listPassages(CommandContext context) throws Exception {
        Book book = new Book(context);
        Book.Located located = book.locate(context.get("part", String.class), null);
        ChatMessage part = translation.find(located.message());
        String stored = StringUtils.defaultString(part.getResponse());
        JsonArray rows = new JsonArray();
        for (AnchoredPassage passage : passages.passagesOf(part)) {
            Reverse.Located item = PassageAccess.locate(passage);
            if (item == null) {
                continue;
            }
            Span span = item.span();
            String translated = slice(stored, item.start(), item.end());
            JsonObject row = new JsonObject();
            row.addProperty("translated", translated);
            if (!span.getReading().isBlank()) {
                row.addProperty("reading", span.getReading());
            }
            row.addProperty("english", span.getSource());
            row.addProperty("speaker", span.getSpeaker());
            row.addProperty("understood", span.isUnderstood());
            if (!span.getRegister().isBlank()) {
                row.addProperty("register", span.getRegister());
            }
            row.addProperty("model", span.getModel());
            row.addProperty("editedByTheAuthor", span.isEdited(translated));
            rows.add(row);
        }
        JsonObject result = new JsonObject();
        result.addProperty("part", located.position());
        result.add("speeches", rows);
        answer(context, result);
    }

    // ---- helpers

    private static void fill(CommandContext context, RealLanguage language) {
        String name = context.get("name", String.class);
        if (name != null) {
            language.setName(name.strip());
        }
        String tongue = context.get("tongue", String.class);
        if (tongue != null) {
            language.setTongue(tongue.strip());
        }
        String script = context.get("script", String.class);
        if (StringUtils.isNotBlank(script)) {
            language.setScript(switch (script.strip().toLowerCase(Locale.ROOT)) {
                case "kana-kanji", "kana_kanji" -> Script.KANA_KANJI;
                case "kana" -> Script.KANA;
                case "romaji" -> Script.ROMAJI;
                case "native" -> Script.NATIVE;
                default -> throw new IllegalArgumentException("The script is kana-kanji, kana, romaji or native, not " + script);
            });
        }
        Boolean honorifics = context.get("honorifics", Boolean.class);
        if (honorifics != null) {
            language.setHonorifics(honorifics);
        }
        Boolean reading = context.get("reading", Boolean.class);
        if (reading != null) {
            language.setReading(reading);
        }
        String politeness = context.get("politeness", String.class);
        if (politeness != null) {
            language.setPoliteness(politeness.strip());
        }
        String names = context.get("names", String.class);
        if (names != null) {
            language.setNames(names.strip());
        }
        String rules = context.get("rules", String.class);
        if (rules != null) {
            language.setRules(rules.strip());
        }
    }

    private void ensureFreeName(RealLanguage language, String ownId) throws Exception {
        boolean taken = library.languages().stream().anyMatch(other -> !other.getId().equals(ownId) && other.getName().strip().equalsIgnoreCase(language.getName()));
        if (taken) {
            throw new IllegalArgumentException("There is a language called " + language.getName() + " already");
        }
    }

    private static JsonObject row(RealLanguage language) {
        JsonObject row = new JsonObject();
        row.addProperty("id", language.getId());
        row.addProperty("name", language.getName());
        row.addProperty("tongue", language.getTongue());
        row.addProperty("script", language.getScript().name().toLowerCase(Locale.ROOT).replace('_', '-'));
        row.addProperty("honorifics", language.isHonorifics());
        row.addProperty("reading", language.isReading());
        row.addProperty("politeness", language.getPoliteness());
        row.addProperty("names", language.getNames());
        row.addProperty("rules", language.getRules());
        return row;
    }

    private static JsonObject speakersRow(Speakers speakers) {
        JsonObject row = new JsonObject();
        row.addProperty("mode", speakers.getMode() == SpeakersMode.ONLY ? "only" : "everyone-except");
        JsonArray list = new JsonArray();
        for (CharacterNote note : speakers.getCharacters()) {
            JsonObject c = new JsonObject();
            c.addProperty("name", note.getName());
            JsonArray aliases = new JsonArray();
            note.getAliases().forEach(aliases::add);
            c.add("aliases", aliases);
            c.addProperty("gender", note.getGender());
            c.addProperty("voice", note.getVoice());
            c.addProperty("listed", note.isListed());
            c.addProperty("speaksTheLanguage", speakers.speaks(note.getName()));
            list.add(c);
        }
        row.add("characters", list);
        return row;
    }

    private static String required(String value, String what) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("Give " + what);
        }
        return value.strip();
    }

    /** The parts of the active branch that have text. */
    private List<ChatMessage> textParts(Book book) throws Exception {
        return book.path((Integer) null).stream().filter(part -> part.getResponse() != null && !part.getResponse().isBlank()).toList();
    }

    /**
     * The parts from..to of the active branch (their positions are the ones of the other commands), those the mode works on. A single part when only
     * 'from' is given.
     */
    private List<RangePart> selectRange(Book book, String from, String to, Mode mode) throws Exception {
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
            all.add(new RangePart(path.indexOf(part) + 1, part, translated.containsKey(part.getId())));
        }
        return RangeLoop.select(mode, all, first, last);
    }

    private static String slice(String text, int start, int end) {
        return start < 0 || end > text.length() || start >= end ? "" : text.substring(start, end);
    }
}
