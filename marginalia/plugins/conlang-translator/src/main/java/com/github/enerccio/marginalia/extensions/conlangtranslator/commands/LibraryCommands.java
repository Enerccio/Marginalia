package com.github.enerccio.marginalia.extensions.conlangtranslator.commands;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.extensions.InheritedSettingValues;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.ConlangInherited;
import com.github.enerccio.marginalia.extensions.conlangtranslator.store.ConlangStore;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Locale;

/**
 * The library of languages as commands: the languages, the sections of the rules, the categories and the registers (the lexicon is in
 * {@link LexiconCommands}). They change the library of the user, or, inside an agent run, the run's own copy of the language (see
 * {@link com.github.enerccio.marginalia.extensions.conlangtranslator.service.LanguageAccess}); the user takes the run's changes over when the run
 * is applied. A proposal of the model is not here but in {@link DesignerCommands}.
 */
public class LibraryCommands extends ConlangCommandSupport {

    public static final String LIST = "conlang-list";
    public static final String GET = "conlang-get";
    public static final String CREATE = "conlang-create";
    public static final String DUPLICATE = "conlang-duplicate";
    public static final String UPDATE = "conlang-update";
    public static final String DELETE = "conlang-delete";
    public static final String EXPORT = "conlang-export";
    public static final String IMPORT = "conlang-import";
    public static final String SET_REFERENCES = "conlang-set-references";
    public static final String SET_BOOK_LANGUAGE = "conlang-set-book-language";
    public static final String LIST_RULES = "conlang-list-rules";
    public static final String GET_RULES = "conlang-get-rules";
    public static final String ADD_RULES = "conlang-add-rules";
    public static final String UPDATE_RULES = "conlang-update-rules";
    public static final String DELETE_RULES = "conlang-delete-rules";
    public static final String MOVE_RULES = "conlang-move-rules";
    public static final String LIST_CATEGORIES = "conlang-list-categories";
    public static final String ADD_CATEGORY = "conlang-add-category";
    public static final String UPDATE_CATEGORY = "conlang-update-category";
    public static final String MOVE_CATEGORY = "conlang-move-category";
    public static final String DELETE_CATEGORY = "conlang-delete-category";
    public static final String LIST_REGISTERS = "conlang-list-registers";
    public static final String ADD_REGISTER = "conlang-add-register";
    public static final String UPDATE_REGISTER = "conlang-update-register";
    public static final String DELETE_REGISTER = "conlang-delete-register";

    private static final String PARAM_NAME = "The name";
    private static final String PARAM_TEXT = "The text";

    @Autowired
    private ManuscriptService books;

    // ---- the languages

    @Command(commandName = LIST, linkedCommand = GET, humanNameText = "List conlangs", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "Lists the constructed languages of the library with the sizes of their parts. Reads only.",
            llmDescriptionText = "Lists the constructed languages of the user's library as JSON: id, name, how many rules sections, categories and words, and how many words are new "
                    + "(not looked at by the author). Inside an agent run a language the run changed is listed as the run has it. Reads only.",
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the languages"))
    public void list(CommandContext context) throws Exception {
        JsonArray list = new JsonArray();
        for (Language language : languages.list(scopeBook(context))) {
            list.add(languageRow(language));
        }
        JsonObject result = new JsonObject();
        result.add("languages", list);
        if (list.isEmpty()) {
            result.addProperty("hint", "There is no language yet, conlang-create makes one");
        }
        answer(context, result);
    }

    @Command(commandName = GET, linkedCommand = LIST, humanNameText = "Get a conlang", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "Shows a language: its sections of rules, categories, registers and sizes. Reads only.",
            llmDescriptionText = "Returns the language as JSON: the sizes, the titles of the rules sections (conlang-get-rules reads one), the categories with their word counts and the registers. "
                    + "Not the words, conlang-list-words lists them. Reads only.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void get(CommandContext context) throws Exception {
        Language language = language(context);
        JsonObject row = languageRow(language);
        row.add("rules", rulesRows(language));
        row.add("categories", categoryRows(language));
        row.add("registers", registerRows(language));
        answer(context, row);
    }

    @Command(commandName = CREATE, linkedCommand = LIST, humanNameText = "Create a conlang", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Makes a new, empty language.",
            llmDescriptionText = "Makes a new empty language with the name and optional description and returns it. Build it with conlang-add-rules, conlang-add-category and conlang-add-word, "
                    + "or let the designer propose (conlang-design-stage, conlang-design-words). Inside an agent run the language is kept with the run until the user takes it over.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, descriptionText = PARAM_NAME),
                    @CommandParameter(name = "description", type = String.class, optional = true, descriptionText = "What the language is for, one or two sentences")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void create(CommandContext context) throws Exception {
        String name = required(context.get("name", String.class), "the name of the language");
        Manuscript book = scopeBook(context);
        if (languages.list(book).stream().anyMatch(language -> language.getName().strip().equalsIgnoreCase(name))) {
            throw new IllegalArgumentException("A language named " + name + " exists already");
        }
        Language language = new Language(name);
        language.setDescription(StringUtils.defaultString(context.get("description", String.class)).strip());
        languages.add(book, language);
        answer(context, languageRow(language));
    }

    @Command(commandName = DUPLICATE, linkedCommand = LIST, humanNameText = "Duplicate a conlang", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Makes a copy of a language under another name.",
            llmDescriptionText = "Copies a language (rules, categories, registers, lexicon) under a new name and returns the copy. A copy is a safe place to try a large change.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "name", type = String.class, optional = true, descriptionText = "The name of the copy; 'copy of ...' when left out")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void duplicate(CommandContext context) throws Exception {
        Language language = language(context);
        String name = StringUtils.defaultIfBlank(context.get("name", String.class), "Copy of " + language.getName()).strip();
        Manuscript book = scopeBook(context);
        if (languages.list(book).stream().anyMatch(other -> other.getName().strip().equalsIgnoreCase(name))) {
            throw new IllegalArgumentException("A language named " + name + " exists already");
        }
        Language copy = language.duplicate(name);
        languages.add(book, copy);
        answer(context, languageRow(copy));
    }

    @Command(commandName = UPDATE, linkedCommand = GET, humanNameText = "Update a conlang", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Changes the name, the description or the hyphen rule of a language.",
            llmDescriptionText = "Changes a language. Pass only what to change. hyphenStructural true means a hyphen is part of a word of the language (a morpheme boundary) and words are compared with their hyphens; "
                    + "false (a language whose compounds are written with or without it) compares without. Returns the language.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "name", type = String.class, optional = true, descriptionText = PARAM_NAME),
                    @CommandParameter(name = "description", type = String.class, optional = true, descriptionText = "What the language is for"),
                    @CommandParameter(name = "hyphenStructural", type = Boolean.class, optional = true, descriptionText = "Whether a hyphen is part of a word")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void update(CommandContext context) throws Exception {
        Language language = language(context);
        String name = context.get("name", String.class);
        String description = context.get("description", String.class);
        Boolean hyphen = context.get("hyphenStructural", Boolean.class);
        if (name == null && description == null && hyphen == null) {
            throw new IllegalArgumentException("Nothing to change: pass the name, the description or hyphenStructural");
        }
        if (name != null && StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("The name of a language can't be empty");
        }
        Language changed = change(context, language, stored -> {
            if (name != null) {
                stored.setName(name.strip());
            }
            if (description != null) {
                stored.setDescription(description.strip());
            }
            if (hyphen != null) {
                stored.setHyphenStructural(hyphen);
            }
        });
        answer(context, languageRow(changed));
    }

    @Command(commandName = DELETE, linkedCommand = LIST, humanNameText = "Delete a conlang", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Deletes a language from the library for good.",
            llmDescriptionText = "Deletes a language with all its words. Destructive, asks the user. Not available inside an agent run (the library is the user's): "
                    + "a run can only leave its own copy of a language unchanged.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "What was deleted"))
    public void delete(CommandContext context) throws Exception {
        Language language = language(context);
        Manuscript book = scopeBook(context);
        if (languages.inRun(book)) {
            throw new IllegalStateException("A language of the library is not deleted from an agent run; ask the user to delete it");
        }
        if (!context.confirm("Delete the language " + language.getName() + " with its " + language.getLexicon().size() + " words?")) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was deleted");
            }
            return;
        }
        languages.delete(book, language.getId());
        context.setReturn("Deleted the language " + language.getName());
    }

    @Command(commandName = EXPORT, linkedCommand = LIST, humanNameText = "Export a conlang", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "The language as a JSON document that conlang-import reads. Reads only.",
            llmDescriptionText = "Returns the whole language as a JSON document (rules, categories, registers, lexicon; without the links to the example book and lorebook). "
                    + "It can be long. Reads only.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "The JSON document"))
    public void export(CommandContext context) throws Exception {
        context.setReturn(ConlangStore.export(language(context)));
    }

    @Command(commandName = IMPORT, linkedCommand = LIST, humanNameText = "Import a conlang", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Adds a language from a JSON document, under a new id.",
            llmDescriptionText = "Adds the language of a document made by conlang-export (pass it as a file with @file:/path for a long one) as a new language of its own id; it never replaces a "
                    + "language. A name that is taken gets a number. Returns the new language.",
            signature = @CommandParameterSignature({@CommandParameter(name = "document", type = String.class, descriptionText = "The JSON document")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void importLanguage(CommandContext context) throws Exception {
        Language imported = ConlangStore.importDocument(required(context.get("document", String.class), "the document"));
        Manuscript book = scopeBook(context);
        String base = StringUtils.defaultIfBlank(imported.getName(), "Imported language");
        String name = base;
        List<Language> existing = languages.list(book);
        for (int n = 2; taken(existing, name); n++) {
            name = base + " " + n;
        }
        imported.setName(name);
        languages.add(book, imported);
        answer(context, languageRow(imported));
    }

    private static boolean taken(List<Language> existing, String name) {
        return existing.stream().anyMatch(language -> language.getName().strip().equalsIgnoreCase(name));
    }

    @Command(commandName = SET_REFERENCES, linkedCommand = GET, humanNameText = "Set the examples of a conlang", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Names the example book and lorebook the designer reads for the world of the language.",
            llmDescriptionText = "Sets the example book and the example lorebook of a language (by their uuid; an empty text removes the link). The designer reads the lorebook "
                    + "(the people and places the language is for) when it proposes words and rules. Pass only what to change.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "book", type = String.class, optional = true, descriptionText = "The uuid of the example book; empty removes it"),
                    @CommandParameter(name = "lorebook", type = String.class, optional = true, descriptionText = "The uuid of the example lorebook; empty removes it")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void setReferences(CommandContext context) throws Exception {
        Language language = language(context);
        String book = context.get("book", String.class);
        String lorebook = context.get("lorebook", String.class);
        if (book == null && lorebook == null) {
            throw new IllegalArgumentException("Nothing to change: pass the book or the lorebook");
        }
        Language changed = change(context, language, stored -> {
            if (book != null) {
                stored.setExampleBook(StringUtils.trimToNull(book));
            }
            if (lorebook != null) {
                stored.setExampleLorebook(StringUtils.trimToNull(lorebook));
            }
        });
        JsonObject row = languageRow(changed);
        row.addProperty("exampleBook", StringUtils.defaultString(changed.getExampleBook()));
        row.addProperty("exampleLorebook", StringUtils.defaultString(changed.getExampleLorebook()));
        answer(context, row);
    }

    @Command(commandName = SET_BOOK_LANGUAGE, linkedCommand = LIST, humanNameText = "Set the language of the book", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Chooses the language the open book is translated into.",
            llmDescriptionText = "Sets the language of the book (what the Language field of the book's Extension Settings holds): the speech of the characters who speak a conlang is translated into it. "
                    + "An empty language removes the choice, the book then inherits the folder's or the user's. Returns what applies to the book.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE + "; empty = inherit")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void setBookLanguage(CommandContext context) throws Exception {
        Manuscript book = scopeBook(context);
        if (book == null) {
            throw new IllegalStateException("No book is open");
        }
        String reference = context.get("language", String.class);
        String id = StringUtils.isBlank(reference) ? null : languages.resolve(book, reference).getId();
        books.update(book, current -> InheritedSettingValues.set(current, ConlangInherited.LANGUAGE, id));
        String applies = books.resolveExtension(book, ConlangInherited.LANGUAGE).value();
        JsonObject row = new JsonObject();
        row.addProperty("bookChoice", StringUtils.defaultString(id));
        Language effective = languages.find(book, StringUtils.trimToNull(applies));
        row.addProperty("appliesToTheBook", effective == null ? "none" : effective.getName());
        answer(context, row);
    }

    // ---- the rules

    @Command(commandName = LIST_RULES, linkedCommand = GET_RULES, humanNameText = "List rules sections", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "Lists the sections of the rules of a language. Reads only.",
            llmDescriptionText = "Lists the sections of the rules in order as JSON: number, title, audience (translator = sent to the translating model, designer = read only by the designer, nobody = the author's notes) and length. Reads only.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void listRules(CommandContext context) throws Exception {
        JsonObject result = new JsonObject();
        result.add("rules", rulesRows(language(context)));
        answer(context, result);
    }

    @Command(commandName = GET_RULES, linkedCommand = LIST_RULES, humanNameText = "Get a rules section", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "Reads a section of the rules. Reads only.",
            llmDescriptionText = "Returns the text of one rules section, in slices of 20000 characters (offset says where the slice starts) with the total length. Reads only.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "section", type = String.class, descriptionText = PARAM_SECTION),
                    @CommandParameter(name = "offset", type = Integer.class, optional = true, defaultValue = "0", min = "0", descriptionText = "Where the slice starts (characters)")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void getRules(CommandContext context) throws Exception {
        RuleSection section = section(language(context), context.get("section", String.class));
        Integer offset = context.get("offset", Integer.class);
        int from = Math.min(offset == null ? 0 : Math.max(0, offset), section.getText().length());
        int to = Math.min(section.getText().length(), from + MAX_TEXT);
        JsonObject row = rowOf(section, 0);
        row.addProperty("text", section.getText().substring(from, to));
        row.addProperty("offset", from);
        row.addProperty("length", section.getText().length());
        if (to < section.getText().length()) {
            row.addProperty("hint", "Cut off, call again with offset " + to);
        }
        answer(context, row);
    }

    @Command(commandName = ADD_RULES, linkedCommand = LIST_RULES, humanNameText = "Add a rules section", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Adds a section of rules at the end.",
            llmDescriptionText = "Adds a rules section (title and text) at the end of the rules and returns the sections. audience is translator (default: the translating model reads it in every request, "
                    + "so write the forms and examples, not a description), designer or nobody. Do not repeat a section that exists: conlang-update-rules changes one.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "title", type = String.class, descriptionText = "The title of the section"),
                    @CommandParameter(name = "text", type = String.class, descriptionText = PARAM_TEXT),
                    @CommandParameter(name = "audience", type = String.class, optional = true, allowed = {"translator", "designer", "nobody"}, descriptionText = "Who reads it, translator when left out")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void addRules(CommandContext context) throws Exception {
        Language language = language(context);
        String title = required(context.get("title", String.class), "the title of the section");
        if (language.getRules().stream().anyMatch(section -> section.getTitle().strip().equalsIgnoreCase(title))) {
            throw new IllegalArgumentException("A section titled " + title + " exists already, conlang-update-rules changes it");
        }
        Audience audience = audience(context.get("audience", String.class));
        RuleSection added = new RuleSection(title, StringUtils.defaultString(context.get("text", String.class)).strip(), audience == null ? Audience.MODEL : audience);
        Language changed = change(context, language, stored -> stored.getRules().add(added));
        JsonObject result = new JsonObject();
        result.add("rules", rulesRows(changed));
        answer(context, result);
    }

    @Command(commandName = UPDATE_RULES, linkedCommand = LIST_RULES, humanNameText = "Update a rules section", dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Changes the title, the text or the audience of a rules section.",
            llmDescriptionText = "Replaces fields of a rules section; pass only what to change. The text is replaced whole, read it first with conlang-get-rules and keep what is right. "
                    + "Words that follow the old text may not follow the new one: conlang-check-lexicon and conlang-review find such cases. Returns the section.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "section", type = String.class, descriptionText = PARAM_SECTION),
                    @CommandParameter(name = "title", type = String.class, optional = true, descriptionText = "The new title"),
                    @CommandParameter(name = "text", type = String.class, optional = true, descriptionText = "The new text (replaces the old one)"),
                    @CommandParameter(name = "audience", type = String.class, optional = true, allowed = {"translator", "designer", "nobody"}, descriptionText = "Who reads it")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void updateRules(CommandContext context) throws Exception {
        Language language = language(context);
        RuleSection section = section(language, context.get("section", String.class));
        String title = context.get("title", String.class);
        String text = context.get("text", String.class);
        Audience audience = audience(context.get("audience", String.class));
        if (title == null && text == null && audience == null) {
            throw new IllegalArgumentException("Nothing to change: pass the title, the text or the audience");
        }
        if (title != null && StringUtils.isBlank(title)) {
            throw new IllegalArgumentException("The title of a section can't be empty");
        }
        Language changed = change(context, language, stored -> {
            RuleSection target = stored.findRule(section.getId());
            if (target == null) {
                throw new IllegalArgumentException("The section is gone");
            }
            if (title != null) {
                target.setTitle(title.strip());
            }
            if (text != null) {
                target.setText(text.strip());
            }
            if (audience != null) {
                target.setAudience(audience);
            }
        });
        answer(context, rowOf(changed.findRule(section.getId()), changed.getRules().indexOf(changed.findRule(section.getId())) + 1));
    }

    @Command(commandName = DELETE_RULES, linkedCommand = LIST_RULES, humanNameText = "Delete a rules section", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Deletes a section of the rules.",
            llmDescriptionText = "Deletes a rules section for good. Destructive, asks the user. Returns the sections left.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "section", type = String.class, descriptionText = PARAM_SECTION)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void deleteRules(CommandContext context) throws Exception {
        Language language = language(context);
        RuleSection section = section(language, context.get("section", String.class));
        if (!context.confirm("Delete the rules section " + section.getTitle() + " of " + language.getName() + "?")) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was deleted");
            }
            return;
        }
        Language changed = change(context, language, stored -> stored.getRules().removeIf(rule -> rule.getId().equals(section.getId())));
        JsonObject result = new JsonObject();
        result.add("rules", rulesRows(changed));
        answer(context, result);
    }

    @Command(commandName = MOVE_RULES, linkedCommand = LIST_RULES, humanNameText = "Move a rules section", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Moves a rules section to a place in the order.",
            llmDescriptionText = "Moves a rules section to the position (the first is 1); the sections are sent to the translating model in this order. Returns the sections.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "section", type = String.class, descriptionText = PARAM_SECTION),
                    @CommandParameter(name = "position", type = Integer.class, min = "1", descriptionText = "The new place, the first is 1")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void moveRules(CommandContext context) throws Exception {
        Language language = language(context);
        RuleSection section = section(language, context.get("section", String.class));
        int position = place(context, language.getRules().size());
        Language changed = change(context, language, stored -> move(stored.getRules(), section.getId(), position, RuleSection::getId));
        JsonObject result = new JsonObject();
        result.add("rules", rulesRows(changed));
        answer(context, result);
    }

    // ---- the categories

    @Command(commandName = LIST_CATEGORIES, linkedCommand = LIST_RULES, humanNameText = "List categories", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "Lists the categories of the words of a language. Reads only.",
            llmDescriptionText = "Lists the categories (semantic groups of the lexicon: Kinship, Weapons...) as JSON: number, name, note, whether it is always sent to the model, and how many words it has. Reads only.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void listCategories(CommandContext context) throws Exception {
        JsonObject result = new JsonObject();
        result.add("categories", categoryRows(language(context)));
        answer(context, result);
    }

    @Command(commandName = ADD_CATEGORY, linkedCommand = LIST_CATEGORIES, humanNameText = "Add a category", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Adds a category for words.",
            llmDescriptionText = "Adds a category at the end and returns the categories. alwaysSend true means all its words go into every translation request (the basic words); "
                    + "the other categories are sent when the part mentions their words.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "name", type = String.class, descriptionText = PARAM_NAME),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = "What belongs in the category"),
                    @CommandParameter(name = "alwaysSend", type = Boolean.class, optional = true, defaultValue = "false", descriptionText = "Send all its words in every request")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void addCategory(CommandContext context) throws Exception {
        Language language = language(context);
        String name = required(context.get("name", String.class), "the name of the category");
        if (language.getCategories().stream().anyMatch(category -> category.getName().strip().equalsIgnoreCase(name))) {
            throw new IllegalArgumentException("A category named " + name + " exists already");
        }
        Category category = new Category(name);
        category.setNote(StringUtils.defaultString(context.get("note", String.class)).strip());
        category.setAlwaysSend(Boolean.TRUE.equals(context.get("alwaysSend", Boolean.class)));
        Language changed = change(context, language, stored -> stored.getCategories().add(category));
        JsonObject result = new JsonObject();
        result.add("categories", categoryRows(changed));
        answer(context, result);
    }

    @Command(commandName = UPDATE_CATEGORY, linkedCommand = LIST_CATEGORIES, humanNameText = "Update a category", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Changes the name, the note or the always-send flag of a category.",
            llmDescriptionText = "Changes a category; pass only what to change. Returns the categories.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "category", type = String.class, descriptionText = PARAM_CATEGORY),
                    @CommandParameter(name = "name", type = String.class, optional = true, descriptionText = PARAM_NAME),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = "What belongs in the category"),
                    @CommandParameter(name = "alwaysSend", type = Boolean.class, optional = true, descriptionText = "Send all its words in every request")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void updateCategory(CommandContext context) throws Exception {
        Language language = language(context);
        Category category = category(language, context.get("category", String.class));
        String name = context.get("name", String.class);
        String note = context.get("note", String.class);
        Boolean always = context.get("alwaysSend", Boolean.class);
        if (name == null && note == null && always == null) {
            throw new IllegalArgumentException("Nothing to change: pass the name, the note or alwaysSend");
        }
        if (name != null && StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("The name of a category can't be empty");
        }
        Language changed = change(context, language, stored -> {
            Category target = stored.findCategory(category.getId());
            if (target == null) {
                throw new IllegalArgumentException("The category is gone");
            }
            if (name != null) {
                target.setName(name.strip());
            }
            if (note != null) {
                target.setNote(note.strip());
            }
            if (always != null) {
                target.setAlwaysSend(always);
            }
        });
        JsonObject result = new JsonObject();
        result.add("categories", categoryRows(changed));
        answer(context, result);
    }

    @Command(commandName = MOVE_CATEGORY, linkedCommand = LIST_CATEGORIES, humanNameText = "Move a category", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Moves a category to a place in the order.",
            llmDescriptionText = "Moves a category to the position (the first is 1); the lexicon is written to the translating model category by category in this order. Returns the categories.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "category", type = String.class, descriptionText = PARAM_CATEGORY),
                    @CommandParameter(name = "position", type = Integer.class, min = "1", descriptionText = "The new place, the first is 1")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void moveCategory(CommandContext context) throws Exception {
        Language language = language(context);
        Category category = category(language, context.get("category", String.class));
        int position = place(context, language.getCategories().size());
        Language changed = change(context, language, stored -> move(stored.getCategories(), category.getId(), position, Category::getId));
        JsonObject result = new JsonObject();
        result.add("categories", categoryRows(changed));
        answer(context, result);
    }

    @Command(commandName = DELETE_CATEGORY, linkedCommand = LIST_CATEGORIES, humanNameText = "Delete a category", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Deletes a category; its words stay, without a category.",
            llmDescriptionText = "Deletes a category. Its words are kept and have no category after it (move them first with conlang-update-word if they belong elsewhere). Destructive, asks the user.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "category", type = String.class, descriptionText = PARAM_CATEGORY)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void deleteCategory(CommandContext context) throws Exception {
        Language language = language(context);
        Category category = category(language, context.get("category", String.class));
        int words = language.entriesOf(category.getId()).size();
        if (!context.confirm("Delete the category " + category.getName() + " of " + language.getName() + "? Its " + words + " words stay, without a category.")) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was deleted");
            }
            return;
        }
        Language changed = change(context, language, stored -> stored.deleteCategory(category.getId()));
        JsonObject result = new JsonObject();
        result.add("categories", categoryRows(changed));
        answer(context, result);
    }

    // ---- the registers

    @Command(commandName = LIST_REGISTERS, linkedCommand = LIST_RULES, humanNameText = "List registers", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "Lists the registers (formal, soft, vulgar...) of a language. Reads only.",
            llmDescriptionText = "Lists the registers of the language: ways of speaking (formal, intimate, vulgar) that a word may belong to, so that the translator picks the form the speaker and the situation ask for. Reads only.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void listRegisters(CommandContext context) throws Exception {
        JsonObject result = new JsonObject();
        result.add("registers", registerRows(language(context)));
        answer(context, result);
    }

    @Command(commandName = ADD_REGISTER, linkedCommand = LIST_REGISTERS, humanNameText = "Add a register", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Adds a register.",
            llmDescriptionText = "Adds a register with a note on when it is used and returns the registers.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "name", type = String.class, descriptionText = PARAM_NAME),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = "When the register is used")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void addRegister(CommandContext context) throws Exception {
        Language language = language(context);
        String name = required(context.get("name", String.class), "the name of the register");
        if (language.getRegisters().stream().anyMatch(register -> register.getName().strip().equalsIgnoreCase(name))) {
            throw new IllegalArgumentException("A register named " + name + " exists already");
        }
        Register register = new Register(name, StringUtils.defaultString(context.get("note", String.class)).strip());
        Language changed = change(context, language, stored -> stored.getRegisters().add(register));
        JsonObject result = new JsonObject();
        result.add("registers", registerRows(changed));
        answer(context, result);
    }

    @Command(commandName = UPDATE_REGISTER, linkedCommand = LIST_REGISTERS, humanNameText = "Update a register", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Changes the name or the note of a register.",
            llmDescriptionText = "Changes a register; pass only what to change. Returns the registers.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "register", type = String.class, descriptionText = "The register: its name"),
                    @CommandParameter(name = "name", type = String.class, optional = true, descriptionText = PARAM_NAME),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = "When the register is used")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void updateRegister(CommandContext context) throws Exception {
        Language language = language(context);
        Register register = register(language, context.get("register", String.class));
        String name = context.get("name", String.class);
        String note = context.get("note", String.class);
        if (name == null && note == null) {
            throw new IllegalArgumentException("Nothing to change: pass the name or the note");
        }
        if (name != null && StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("The name of a register can't be empty");
        }
        Language changed = change(context, language, stored -> {
            Register target = stored.findRegister(register.getId());
            if (target == null) {
                throw new IllegalArgumentException("The register is gone");
            }
            if (name != null) {
                target.setName(name.strip());
            }
            if (note != null) {
                target.setNote(note.strip());
            }
        });
        JsonObject result = new JsonObject();
        result.add("registers", registerRows(changed));
        answer(context, result);
    }

    @Command(commandName = DELETE_REGISTER, linkedCommand = LIST_REGISTERS, humanNameText = "Delete a register", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Deletes a register; its words become neutral.",
            llmDescriptionText = "Deletes a register; the words in it become neutral. Destructive, asks the user.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "register", type = String.class, descriptionText = "The register: its name")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void deleteRegister(CommandContext context) throws Exception {
        Language language = language(context);
        Register register = register(language, context.get("register", String.class));
        if (!context.confirm("Delete the register " + register.getName() + " of " + language.getName() + "? Its words become neutral.")) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was deleted");
            }
            return;
        }
        Language changed = change(context, language, stored -> stored.deleteRegister(register.getId()));
        JsonObject result = new JsonObject();
        result.add("registers", registerRows(changed));
        answer(context, result);
    }

    // ---- helpers

    private static int place(CommandContext context, int size) {
        Integer position = context.get("position", Integer.class);
        if (position == null || position < 1 || position > size) {
            throw new IllegalArgumentException("The position is a number from 1 to " + size);
        }
        return position;
    }

    private static <T> void move(List<T> list, String id, int position, java.util.function.Function<T, String> idOf) {
        T item = list.stream().filter(candidate -> idOf.apply(candidate).equals(id)).findFirst().orElseThrow(() -> new IllegalArgumentException("It is gone"));
        list.remove(item);
        list.add(Math.min(position - 1, list.size()), item);
    }

    private static JsonObject rowOf(RuleSection section, int number) {
        JsonObject row = new JsonObject();
        if (number > 0) {
            row.addProperty("number", number);
        }
        row.addProperty("title", section.getTitle());
        row.addProperty("audience", audienceName(section.getAudience()));
        row.addProperty("length", section.getText().length());
        return row;
    }

    private static JsonArray rulesRows(Language language) {
        JsonArray list = new JsonArray();
        for (int i = 0; i < language.getRules().size(); i++) {
            list.add(rowOf(language.getRules().get(i), i + 1));
        }
        return list;
    }

    private static JsonArray categoryRows(Language language) {
        JsonArray list = new JsonArray();
        for (int i = 0; i < language.getCategories().size(); i++) {
            Category category = language.getCategories().get(i);
            JsonObject row = new JsonObject();
            row.addProperty("number", i + 1);
            row.addProperty("name", category.getName());
            if (!category.getNote().isBlank()) {
                row.addProperty("note", category.getNote());
            }
            row.addProperty("alwaysSend", category.isAlwaysSend());
            row.addProperty("words", language.entriesOf(category.getId()).size());
            list.add(row);
        }
        int loose = language.entriesOf(null).size();
        if (loose > 0) {
            JsonObject row = new JsonObject();
            row.addProperty("name", "(no category)");
            row.addProperty("words", loose);
            list.add(row);
        }
        return list;
    }

    private static JsonArray registerRows(Language language) {
        JsonArray list = new JsonArray();
        for (Register register : language.getRegisters()) {
            JsonObject row = new JsonObject();
            row.addProperty("name", register.getName());
            if (!register.getNote().isBlank()) {
                row.addProperty("note", register.getNote());
            }
            row.addProperty("words", (int) language.getLexicon().stream().filter(entry -> register.getId().equals(entry.getRegister())).count());
            list.add(row);
        }
        return list;
    }

    static String lower(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
