package com.github.enerccio.marginalia.extensions.reallangtranslator.commands;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read.ManuscriptReadSupport;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.extensions.InheritedSettingValues;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.reallangtranslator.ReallangKind;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.*;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.*;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.TranslationService.Outcome;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.*;
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
    public static final String ADD_BOOK_LANGUAGE = "reallang-add-book-language";
    public static final String REMOVE_BOOK_LANGUAGE = "reallang-remove-book-language";
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
    public static final String GLOSSARY = "reallang-glossary";
    public static final String GLOSSARY_SET = "reallang-glossary-set";
    public static final String GLOSSARY_REMOVE = "reallang-glossary-remove";
    public static final String GLOSSARY_ACCEPT = "reallang-glossary-accept";
    public static final String GLOSSARY_SWITCH = "reallang-glossary-switch";
    public static final String GLOSSARY_MOVE = "reallang-glossary-move";
    public static final String GET_SOURCE = "reallang-get-source";
    public static final String LIST_PASSAGES = "reallang-list-passages";

    private static final String PARAM_PART = "The part: its handle, or its position on the active branch (the first part is 1)";
    private static final String PARAM_LANGUAGE = "The language: its name (the case does not matter) or its id, see reallang-list";
    private static final String PARAM_BOOK_LANGUAGE = "One of the languages of the book: its name or its id (reallang-status lists them); all of them when left out";
    private static final String PARAM_BOOK_LANGUAGE_FIRST = "One of the languages of the book: its name or its id (reallang-status lists them); the first language of the book when left out";
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

    @Command(commandName = ADD_BOOK_LANGUAGE, linkedCommand = SET_BOOK_LANGUAGE, humanNameText = "Add a real language to the book", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Adds another language the open book is translated into, besides the one it names.",
            llmDescriptionText = "Adds a language of the library to the book besides the language it names (reallang-set-book-language), for a story where some characters speak another real language. "
                    + "Each language has its own speakers (reallang-speakers with 'language'); a part is translated with one request for each language. Returns the languages of the book.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void addBookLanguage(CommandContext context) throws Exception {
        Book book = new Book(context);
        RealLanguage language = library.resolve(context.get("language", String.class));
        if (language.getId().equals(bookReal.primaryLanguageId(book.manuscript))) {
            throw new IllegalArgumentException(language.getName() + " is the language the book names already");
        }
        List<BookLanguage> others = bookReal.otherLanguages(book.manuscript);
        if (others.stream().noneMatch(item -> language.getId().equals(item.getLanguageId()))) {
            others.add(new BookLanguage(language.getId()));
            bookReal.saveOtherLanguages(book.manuscript, others);
        }
        answer(context, languagesRow(book.manuscript));
    }

    @Command(commandName = REMOVE_BOOK_LANGUAGE, linkedCommand = ADD_BOOK_LANGUAGE, humanNameText = "Remove a real language from the book", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Takes one of the other languages out of the book.",
            llmDescriptionText = "Removes one of the other languages of the book (not the one the book names: that is reallang-set-book-language) and its speakers. The translated speeches already in the story stay, "
                    + "put them back with reallang-revert-to-source first. Destructive, asks the user. Returns the languages of the book.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void removeBookLanguage(CommandContext context) throws Exception {
        Book book = new Book(context);
        RealLanguage language = library.resolve(context.get("language", String.class));
        List<BookLanguage> others = bookReal.otherLanguages(book.manuscript);
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
        bookReal.saveOtherLanguages(book.manuscript, others);
        context.setReturn(GSON.toJson(languagesRow(book.manuscript)));
    }

    /** The languages of the book: the first and the others. */
    private JsonObject languagesRow(Manuscript book) throws Exception {
        JsonObject row = new JsonObject();
        JsonArray languages = new JsonArray();
        for (LanguageRef item : bookReal.languages(book)) {
            languages.add(item.name());
        }
        row.add("languages", languages);
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
        RealLanguage language = library.resolve(reference);
        if (bookReal.languages(book.manuscript).stream().noneMatch(item -> item.id().equals(language.getId()))) {
            throw new IllegalArgumentException("The book does not have " + language.getName() + " (reallang-add-book-language adds it)");
        }
        return language.getId();
    }

    /** The language of a command about the speakers: the one given, else the first language of the book. */
    private String speakersLanguage(Book book, CommandContext context) throws Exception {
        String id = bookLanguage(book, context);
        if (id == null) {
            id = bookReal.primaryLanguageId(book.manuscript);
        }
        if (id == null) {
            throw new IllegalStateException("The book has no language (reallang-set-book-language)");
        }
        return id;
    }

    // ---- the glossary of names and special words

    private static final String PARAM_LEVEL = "Where the entry lives: language (every book in this language), folder (the books of a folder, see 'folder') or book (this book)";
    private static final String PARAM_FOLDER = "For the folder level: the name (or uuid) of one of the folders the book is in";

    @Command(commandName = GLOSSARY, linkedCommand = GLOSSARY_SET, humanNameText = "Glossary of the real language", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "The glossary of names and special words as the model gets it for this book. Reads only.",
            llmDescriptionText = "Returns as JSON the glossary of the book in a language: the entries of the language, of the folders of the book and of the book merged (the nearest level wins for the same word), "
                    + "each with its word, how it is written (or kept as it is), the reading, the note, the level it lives at, whether it is in use (active) and whether the model proposed it and the author has not looked at it yet. Reads only.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE_FIRST)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the glossary"))
    public void glossary(CommandContext context) throws Exception {
        Book book = new Book(context);
        String languageId = speakersLanguage(book, context);
        JsonArray list = new JsonArray();
        for (Glossary.Effective item : bookReal.glossary(book.manuscript, languageId)) {
            GlossaryEntry entry = item.entry();
            JsonObject row = new JsonObject();
            row.addProperty("source", entry.getSource());
            row.addProperty("kind", entry.getKind() == GlossaryKind.KEEP ? "keep" : "written");
            row.addProperty("written", entry.getWritten());
            row.addProperty("reading", entry.getReading());
            row.addProperty("note", entry.getNote());
            row.addProperty("level", item.level().name().toLowerCase(Locale.ROOT));
            row.addProperty("levelName", item.label());
            row.addProperty("active", item.active());
            row.addProperty("proposed", entry.isProposed());
            list.add(row);
        }
        JsonObject result = new JsonObject();
        result.add("entries", list);
        answer(context, result);
    }

    @Command(commandName = GLOSSARY_SET, linkedCommand = GLOSSARY, humanNameText = "Set a glossary entry", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Adds a name or special word to the glossary, or changes the entry of the word at that level.",
            llmDescriptionText = "Adds an entry to the glossary of a level (book by default), or replaces the entry of the same word there. 'source' is the word as the English story writes it; 'written' how the language writes it "
                    + "(not needed with kind keep, which leaves the word as it is); 'reading' how it is read, 'note' a few words for the translator. An entry the author sets is never a proposal. Returns the glossary.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "source", type = String.class, descriptionText = "The word as the story writes it (\"Saellen\", \"witch\")"),
                    @CommandParameter(name = "written", type = String.class, optional = true, descriptionText = "How the language writes it; empty for kind keep"),
                    @CommandParameter(name = "kind", type = String.class, optional = true, defaultValue = "written", allowed = {"written", "keep"}, descriptionText = "written, or keep to leave the word as it is"),
                    @CommandParameter(name = "reading", type = String.class, optional = true, descriptionText = "How it is read, may be empty"),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = "A few words for the translator"),
                    @CommandParameter(name = "level", type = String.class, optional = true, defaultValue = "book", allowed = {"language", "folder", "book"}, descriptionText = PARAM_LEVEL),
                    @CommandParameter(name = "folder", type = String.class, optional = true, descriptionText = PARAM_FOLDER),
                    @CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE_FIRST)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the glossary"))
    public void glossarySet(CommandContext context) throws Exception {
        Book book = new Book(context);
        String languageId = speakersLanguage(book, context);
        GlossaryEntry entry = new GlossaryEntry();
        entry.setSource(required(context.get("source", String.class), "the word"));
        String kind = StringUtils.defaultIfBlank(context.get("kind", String.class), "written").toLowerCase(Locale.ROOT);
        entry.setKind(switch (kind) {
            case "written" -> GlossaryKind.WRITTEN;
            case "keep" -> GlossaryKind.KEEP;
            default -> throw new IllegalArgumentException("The kind is written or keep, not " + kind);
        });
        entry.setWritten(StringUtils.defaultString(context.get("written", String.class)));
        entry.setReading(StringUtils.defaultString(context.get("reading", String.class)));
        entry.setNote(StringUtils.defaultString(context.get("note", String.class)));
        bookReal.putGlossaryEntry(book.manuscript, languageId, glossaryTarget(book, context), entry);
        glossary(context);
    }

    @Command(commandName = GLOSSARY_REMOVE, linkedCommand = GLOSSARY, humanNameText = "Remove a glossary entry", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Removes the entry of a word from a level of the glossary.",
            llmDescriptionText = "Removes the entry of the word from the glossary of the level (book by default). Destructive, asks the user. To only stop using a word of a higher level in this book use reallang-glossary-switch. Returns the glossary.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "source", type = String.class, descriptionText = "The word of the entry"),
                    @CommandParameter(name = "level", type = String.class, optional = true, defaultValue = "book", allowed = {"language", "folder", "book"}, descriptionText = PARAM_LEVEL),
                    @CommandParameter(name = "folder", type = String.class, optional = true, descriptionText = PARAM_FOLDER),
                    @CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE_FIRST)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the glossary"))
    public void glossaryRemove(CommandContext context) throws Exception {
        Book book = new Book(context);
        String languageId = speakersLanguage(book, context);
        String source = required(context.get("source", String.class), "the word");
        GlossaryService.Target target = glossaryTarget(book, context);
        GlossaryEntry entry = glossaryEntryAt(book, languageId, target, source);
        if (!context.confirm("Remove " + entry.getSource() + " from the glossary?")) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was removed");
            }
            return;
        }
        bookReal.removeGlossaryEntry(book.manuscript, languageId, target, entry.getId());
        glossary(context);
    }

    @Command(commandName = GLOSSARY_ACCEPT, linkedCommand = GLOSSARY, humanNameText = "Accept a proposed glossary entry", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Marks an entry the model proposed as looked at.",
            llmDescriptionText = "Accepts the proposed entries of the book: the word given, or all the proposals when 'source' is left out. The model uses a proposal from the moment it is made; accepting only says the author looked at it. Returns the glossary.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "source", type = String.class, optional = true, descriptionText = "The word; all the proposals of the book when left out"),
                    @CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE_FIRST)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the glossary"))
    public void glossaryAccept(CommandContext context) throws Exception {
        Book book = new Book(context);
        String languageId = speakersLanguage(book, context);
        String source = context.get("source", String.class);
        for (Glossary.Effective item : bookReal.glossary(book.manuscript, languageId)) {
            if (item.level() == Glossary.Level.BOOK && item.entry().isProposed()
                    && (StringUtils.isBlank(source) || item.entry().key().equals(Glossary.key(source)))) {
                bookReal.acceptGlossaryEntry(book.manuscript, languageId, item.entry().getId());
            }
        }
        glossary(context);
    }

    @Command(commandName = GLOSSARY_SWITCH, linkedCommand = GLOSSARY, humanNameText = "Switch a glossary word off or on for the book", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Stops using (or uses again) a word of the language's or a folder's glossary in this book.",
            llmDescriptionText = "Switches a word that comes from the language or a folder off for this book (enabled false), or on again (enabled true) by writing an entry of the book for the same word. A word of the book itself is changed with reallang-glossary-set or removed. Returns the glossary.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "source", type = String.class, descriptionText = "The word"),
                    @CommandParameter(name = "enabled", type = Boolean.class, descriptionText = "false: do not use it in this book; true: use it"),
                    @CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE_FIRST)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the glossary"))
    public void glossarySwitch(CommandContext context) throws Exception {
        Book book = new Book(context);
        String languageId = speakersLanguage(book, context);
        String source = required(context.get("source", String.class), "the word");
        Boolean enabled = context.get("enabled", Boolean.class);
        if (enabled == null) {
            throw new IllegalArgumentException("Give enabled: true or false");
        }
        Glossary.Effective found = bookReal.glossary(book.manuscript, languageId).stream().filter(item -> item.entry().key().equals(Glossary.key(source))).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The glossary has no word " + source));
        if (found.level() == Glossary.Level.BOOK) {
            GlossaryEntry own = found.entry().copy();
            own.setEnabled(enabled);
            bookReal.putGlossaryEntry(book.manuscript, languageId, GlossaryService.Target.book(), own);
        } else {
            bookReal.switchGlossaryEntry(book.manuscript, languageId, found.entry(), enabled);
        }
        glossary(context);
    }

    @Command(commandName = GLOSSARY_MOVE, linkedCommand = GLOSSARY, humanNameText = "Move a glossary entry of the book up", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Moves an entry of the book to a folder or to the language, so every book there gets it.",
            llmDescriptionText = "Moves the entry of the word from the book to the glossary of a folder the book is in or of the language (the entry of the same word there is replaced). The book does not hold it afterwards. Returns the glossary.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "source", type = String.class, descriptionText = "The word of an entry of the book"),
                    @CommandParameter(name = "level", type = String.class, allowed = {"language", "folder"}, descriptionText = "Where to: language or folder"),
                    @CommandParameter(name = "folder", type = String.class, optional = true, descriptionText = PARAM_FOLDER),
                    @CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE_FIRST)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the glossary"))
    public void glossaryMove(CommandContext context) throws Exception {
        Book book = new Book(context);
        String languageId = speakersLanguage(book, context);
        GlossaryEntry entry = glossaryEntryAt(book, languageId, GlossaryService.Target.book(), required(context.get("source", String.class), "the word"));
        if (!bookReal.moveGlossaryEntry(book.manuscript, languageId, entry.getId(), glossaryTarget(book, context))) {
            throw new IllegalArgumentException("The book does not have the entry");
        }
        glossary(context);
    }

    /** The level an entry command is about: language, folder (named by 'folder') or book. */
    private GlossaryService.Target glossaryTarget(Book book, CommandContext context) throws Exception {
        String level = StringUtils.defaultIfBlank(context.get("level", String.class), "book").strip().toLowerCase(Locale.ROOT);
        return switch (level) {
            case "language" -> GlossaryService.Target.language();
            case "book" -> GlossaryService.Target.book();
            case "folder" -> {
                String reference = required(context.get("folder", String.class), "the folder (its name or uuid)");
                List<Folder> folders = bookReal.glossaryFolders(book.manuscript);
                yield folders.stream().filter(folder -> folder.getUuid().equals(reference) || folder.getName().strip().equalsIgnoreCase(reference)).findFirst()
                        .map(folder -> GlossaryService.Target.folder(folder.getUuid()))
                        .orElseThrow(() -> new IllegalArgumentException("The book is not in a folder " + reference + ". Folders: "
                                + (folders.isEmpty() ? "none" : String.join(", ", folders.stream().map(Folder::getName).toList()))));
            }
            default -> throw new IllegalArgumentException("The level is language, folder or book, not " + level);
        };
    }

    private GlossaryEntry glossaryEntryAt(Book book, String languageId, GlossaryService.Target target, String source) throws Exception {
        return bookReal.glossary(book.manuscript, languageId).stream()
                .filter(item -> item.level() == target.level() && java.util.Objects.equals(item.id(), target.folderUuid()) && item.entry().key().equals(Glossary.key(source)))
                .map(Glossary.Effective::entry).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("That level has no word " + source + " (reallang-glossary lists the entries and their levels)"));
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
        JsonArray bookLanguages = new JsonArray();
        for (LanguageRef item : bookReal.languages(book.manuscript)) {
            bookLanguages.add(item.name());
        }
        result.add("languages", bookLanguages);
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
                    + "(reallang-set-book-language) and a model. A book with more than one language (reallang-add-book-language) takes one request for each, one after the other; 'language' limits it to one.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, descriptionText = PARAM_PART),
                    @CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what came of it"))
    public void translatePart(CommandContext context) throws Exception {
        Book book = new Book(context);
        Book.Located located = book.locate(context.get("part", String.class), null);
        Outcome outcome = translation.translate(book.manuscript, located.message(), bookLanguage(book, context));
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
                    + "the history keeps the translated version. Speeches the author edited stay as they are. Asks the user. Returns the summary.",
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
            TranslationWriter.Reverted reverted = translation.revert(book, part, languageId);
            return reverted.reverted() == 0 ? PartResult.nothing() : PartResult.done(reverted.reverted(), 0, reverted.kept());
        }
        Outcome outcome = translation.translate(book, part, languageId);
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
                    @CommandParameter(name = "context", type = String.class, optional = true, descriptionText = "Who says it, to whom, in what situation"),
                    @CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE_FIRST)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the translation"))
    public void translateText(CommandContext context) throws Exception {
        Book book = new Book(context);
        TranslationService.Quick quick = bookReal.quickTranslate(book.manuscript, required(context.get("text", String.class), "the text"),
                StringUtils.defaultString(context.get("context", String.class)), bookLanguage(book, context));
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
            throw new IllegalArgumentException("The part does not have that sentence; quote it exactly (reallang-get-source shows the text)");
        }
        if (text.indexOf(sentence, start + 1) >= 0) {
            throw new IllegalArgumentException("The sentence occurs more than once in the part; quote more of it");
        }
        Outcome outcome = translation.translateSelection(book.manuscript, part, text, start, start + sentence.length(), bookLanguage(book, context));
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
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE_FIRST)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the speakers"))
    public void speakers(CommandContext context) throws Exception {
        Book book = new Book(context);
        answer(context, speakersRow(bookReal.speakers(book.manuscript, speakersLanguage(book, context))));
    }

    @Command(commandName = SET_SPEAKERS_MODE, linkedCommand = SPEAKERS, humanNameText = "Set who speaks the real language", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
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
        Speakers speakers = bookReal.speakers(book.manuscript, languageId);
        speakers.setMode(switch (mode) {
            case "everyone-except", "everyone_except" -> SpeakersMode.EVERYONE_EXCEPT;
            case "only" -> SpeakersMode.ONLY;
            default -> throw new IllegalArgumentException("The mode is everyone-except or only, not " + mode);
        });
        bookReal.saveSpeakers(book.manuscript, languageId, speakers);
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
                    @CommandParameter(name = "voice", type = String.class, optional = true, descriptionText = "How the character speaks"),
                    @CommandParameter(name = "language", type = String.class, optional = true, descriptionText = PARAM_BOOK_LANGUAGE_FIRST)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the speakers"))
    public void setSpeaker(CommandContext context) throws Exception {
        Book book = new Book(context);
        String name = required(context.get("name", String.class), "the name of the character");
        String languageId = speakersLanguage(book, context);
        Speakers speakers = bookReal.speakers(book.manuscript, languageId);
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
        bookReal.saveSpeakers(book.manuscript, languageId, speakers);
        answer(context, speakersRow(speakers));
    }

    @Command(commandName = REMOVE_SPEAKER, linkedCommand = SPEAKERS, humanNameText = "Remove a character of the real language speakers", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
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
        Speakers speakers = bookReal.speakers(book.manuscript, languageId);
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
        bookReal.saveSpeakers(book.manuscript, languageId, speakers);
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
            llmDescriptionText = "Lists the translated speeches of the part as JSON (all the languages of the book): the translated text, how it is read (when there is a reading), its English source, the language, who said it, whether the listener understands it, "
                    + "the register, the model that translated it and whether the author edited it since (an edited speech keeps the author's words). Reads only.",
            signature = @CommandParameterSignature({@CommandParameter(name = "part", type = String.class, descriptionText = PARAM_PART)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the speeches"))
    public void listPassages(CommandContext context) throws Exception {
        Book book = new Book(context);
        Book.Located located = book.locate(context.get("part", String.class), null);
        ChatMessage part = translation.find(located.message());
        String stored = StringUtils.defaultString(part.getResponse());
        JsonArray rows = new JsonArray();
        List<LanguageRef> bookLanguages = bookReal.languages(book.manuscript);
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
            row.addProperty("language", bookLanguages.stream().filter(l -> l.id().equals(span.getLanguage())).map(LanguageRef::name).findFirst().orElse(StringUtils.defaultString(span.getLanguage())));
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
            Set<String> languages = new HashSet<>();
            translated.getOrDefault(part.getId(), List.of()).forEach(located -> languages.add(located.span().getLanguage()));
            all.add(new RangePart(path.indexOf(part) + 1, part, translated.containsKey(part.getId()), languages));
        }
        return RangeLoop.select(mode, all, first, last, languageId);
    }

    private static String slice(String text, int start, int end) {
        return start < 0 || end > text.length() || start >= end ? "" : text.substring(start, end);
    }
}
