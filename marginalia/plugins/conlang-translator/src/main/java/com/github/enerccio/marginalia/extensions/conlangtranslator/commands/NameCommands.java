package com.github.enerccio.marginalia.extensions.conlangtranslator.commands;

import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.EntryStatus;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.LexiconEntry;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.NameCategory;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.NameGeneratorBridge;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The categories of names a language gives the Name generator, as commands (what the <i>Names</i> tab of the editor does): list, add, change the
 * words of, delete, and preview the names of one. Listing and generating the names themselves is the Name generator's own
 * ({@code list-name-categories} lists them as {@code Conlang - language - category}, {@code generate-names} makes the names).
 * They change the library of the user, or the run's copy of the language inside an agent run.
 */
public class NameCommands extends ConlangCommandSupport {

    public static final String LIST = "conlang-list-name-categories";
    public static final String ADD = "conlang-add-name-category";
    public static final String UPDATE = "conlang-update-name-category";
    public static final String DELETE = "conlang-delete-name-category";
    public static final String PREVIEW = "conlang-preview-names";

    private static final String PARAM_NAME_CATEGORY = "The category of names: its name, or its number in conlang-list-name-categories (the first is 1)";
    private static final String PARAM_WORDS = "Words of the lexicon, separated by ; or new lines: each its form (or its id for homophones, see conlang-list-words)";
    private static final String PARAM_FROM_CATEGORY = "A category of the lexicon (name or number, see conlang-list-categories): all its words that are not retired";

    private final NameGeneratorBridge names;

    public NameCommands(NameGeneratorBridge names) {
        this.names = names;
    }

    @Command(commandName = LIST, linkedCommand = ADD, humanNameText = "List name categories of a conlang", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "Lists the categories of names of a language. Reads only.",
            llmDescriptionText = "Lists the categories of names a language gives the Name generator (the words of the lexicon its Markov chain is made of) as JSON: number, name, the title "
                    + "the generator lists it as, and its words as they are now (a deleted or retired word is not counted). Reads only.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void list(CommandContext context) throws Exception {
        JsonObject result = new JsonObject();
        result.add("nameCategories", rows(language(context)));
        answer(context, result);
    }

    @Command(commandName = ADD, linkedCommand = LIST, humanNameText = "Add a name category to a conlang", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Adds a category of names, with words of the lexicon.",
            llmDescriptionText = "Adds a category of names at the end and returns the categories. The Name generator trains a Markov chain on its words, so names made of it sound like the "
                    + "language: a dozen words or more give better names. Give the words and/or a category of the lexicon.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "name", type = String.class, descriptionText = "The name of the category of names, for example Places"),
                    @CommandParameter(name = "words", type = String.class, optional = true, descriptionText = PARAM_WORDS),
                    @CommandParameter(name = "fromCategory", type = String.class, optional = true, descriptionText = PARAM_FROM_CATEGORY)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void add(CommandContext context) throws Exception {
        Language language = language(context);
        String name = required(context.get("name", String.class), "the name of the category of names");
        if (language.getNameCategories().stream().anyMatch(category -> category.getName().equalsIgnoreCase(name))) {
            throw new IllegalArgumentException("A category of names named " + name + " exists already");
        }
        List<String> chosen = new ArrayList<>(ids(language, context.get("words", String.class)));
        chosen.addAll(idsOfCategory(language, context.get("fromCategory", String.class)));
        NameCategory created = new NameCategory(name);
        chosen.forEach(id -> created.set(id, true));
        Language changed = change(context, language, stored -> stored.getNameCategories().add(created));
        JsonObject result = new JsonObject();
        result.add("nameCategories", rows(changed));
        answer(context, result);
    }

    @Command(commandName = UPDATE, linkedCommand = LIST, humanNameText = "Update a name category of a conlang", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Renames a category of names, adds words to it and takes words away.",
            llmDescriptionText = "Changes a category of names; pass only what to change. add and addFromCategory put words in, remove takes words out (a word that is not in it is ignored). "
                    + "Returns the categories.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "nameCategory", type = String.class, descriptionText = PARAM_NAME_CATEGORY),
                    @CommandParameter(name = "name", type = String.class, optional = true, descriptionText = "The new name"),
                    @CommandParameter(name = "add", type = String.class, optional = true, descriptionText = PARAM_WORDS),
                    @CommandParameter(name = "addFromCategory", type = String.class, optional = true, descriptionText = PARAM_FROM_CATEGORY),
                    @CommandParameter(name = "remove", type = String.class, optional = true, descriptionText = PARAM_WORDS)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void update(CommandContext context) throws Exception {
        Language language = language(context);
        NameCategory category = nameCategory(language, context.get("nameCategory", String.class));
        String name = context.get("name", String.class);
        String add = context.get("add", String.class);
        String addFrom = context.get("addFromCategory", String.class);
        String remove = context.get("remove", String.class);
        if (name == null && StringUtils.isBlank(add) && StringUtils.isBlank(addFrom) && StringUtils.isBlank(remove)) {
            throw new IllegalArgumentException("Nothing to change: pass the name, add, addFromCategory or remove");
        }
        if (name != null && StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("The name of a category of names can't be empty");
        }
        if (name != null && language.getNameCategories().stream().anyMatch(other -> other != category && other.getName().equalsIgnoreCase(name.strip()))) {
            throw new IllegalArgumentException("A category of names named " + name.strip() + " exists already");
        }
        List<String> adding = new ArrayList<>(ids(language, add));
        adding.addAll(idsOfCategory(language, addFrom));
        List<String> removing = ids(language, remove);
        Language changed = change(context, language, stored -> {
            NameCategory target = stored.findNameCategory(category.getId());
            if (target == null) {
                throw new IllegalArgumentException("The category of names is gone");
            }
            if (name != null) {
                target.setName(name.strip());
            }
            adding.forEach(id -> target.set(id, true));
            removing.forEach(id -> target.set(id, false));
        });
        JsonObject result = new JsonObject();
        result.add("nameCategories", rows(changed));
        answer(context, result);
    }

    @Command(commandName = DELETE, linkedCommand = LIST, humanNameText = "Delete a name category of a conlang", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Deletes a category of names (the words stay in the lexicon).",
            llmDescriptionText = "Deletes a category of names; the words of the lexicon are not touched. Destructive, asks the user.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "nameCategory", type = String.class, descriptionText = PARAM_NAME_CATEGORY)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_JSON))
    public void delete(CommandContext context) throws Exception {
        Language language = language(context);
        NameCategory category = nameCategory(language, context.get("nameCategory", String.class));
        if (!context.confirm("Delete the category of names " + category.getName() + " of " + language.getName() + "?")) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was deleted");
            }
            return;
        }
        Language changed = change(context, language, stored -> stored.getNameCategories().removeIf(other -> other.getId().equals(category.getId())));
        JsonObject result = new JsonObject();
        result.add("nameCategories", rows(changed));
        answer(context, result);
    }

    @Command(commandName = PREVIEW, linkedCommand = LIST, humanNameText = "Preview the names of a conlang", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "Makes names from the words of a category of names, nothing is saved. Reads only.",
            llmDescriptionText = "Makes names (up to 100, 20 by default) with the Name generator's Markov chain from the words of a category of names as they are now, to see what it gives "
                    + "before you use it. Needs the Name generator extension. Returns a JSON array. Reads only.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "nameCategory", type = String.class, descriptionText = PARAM_NAME_CATEGORY),
                    @CommandParameter(name = "count", type = Integer.class, optional = true, defaultValue = "20", min = "1", max = "100", descriptionText = "How many names")}),
            returns = @CommandReturn(name = "names", type = String.class, descriptionText = "JSON array of names"))
    public void preview(CommandContext context) throws Exception {
        Language language = language(context);
        NameCategory category = nameCategory(language, context.get("nameCategory", String.class));
        Integer count = context.get("count", Integer.class);
        List<String> words = language.nameWords(category);
        if (words.isEmpty()) {
            throw new IllegalArgumentException("The category " + category.getName() + " has no words (check the words of the lexicon with conlang-update-name-category)");
        }
        if (!names.available()) {
            throw new IllegalStateException("The Name generator is not installed");
        }
        List<String> made = names.preview(words, count == null ? 20 : count);
        context.setReturn(array(made).toString());
    }

    // ---- finding things

    /** A category of names by its name (the case does not matter) or its number. */
    private static NameCategory nameCategory(Language language, String reference) {
        if (StringUtils.isBlank(reference)) {
            throw new IllegalArgumentException("Give the category of names");
        }
        String wanted = reference.strip();
        List<NameCategory> named = language.getNameCategories().stream().filter(category -> category.getName().equalsIgnoreCase(wanted)).toList();
        if (named.size() == 1) {
            return named.getFirst();
        }
        NameCategory byId = language.findNameCategory(wanted);
        if (byId != null) {
            return byId;
        }
        if (wanted.matches("\\d{1,6}")) {
            int number = Integer.parseInt(wanted);
            if (number >= 1 && number <= language.getNameCategories().size()) {
                return language.getNameCategories().get(number - 1);
            }
        }
        throw new IllegalArgumentException("There is no category of names " + wanted + " in " + language.getName() + ", see " + LIST);
    }

    /** The ids of the words written in a list ({@code ;} or new lines between them). */
    private static List<String> ids(Language language, String text) {
        List<String> ids = new ArrayList<>();
        if (StringUtils.isNotBlank(text)) {
            for (String piece : text.split("[\\n;]")) {
                if (!piece.isBlank()) {
                    ids.add(word(language, piece).getId());
                }
            }
        }
        return ids;
    }

    /** The ids of the words of a category of the lexicon that are not retired. */
    private static List<String> idsOfCategory(Language language, String reference) {
        if (StringUtils.isBlank(reference)) {
            return List.of();
        }
        return language.entriesOf(category(language, reference).getId()).stream().filter(entry -> entry.getStatus() != EntryStatus.RETIRED)
                .map(LexiconEntry::getId).toList();
    }

    private static JsonArray rows(Language language) {
        JsonArray rows = new JsonArray();
        int number = 1;
        for (NameCategory category : language.getNameCategories()) {
            JsonObject row = new JsonObject();
            row.addProperty("number", number++);
            row.addProperty("name", category.getName());
            row.addProperty("title", language.nameCategoryTitle(category));
            List<String> words = language.nameWords(category);
            row.addProperty("words", words.size());
            row.add("forms", array(words));
            rows.add(row);
        }
        return rows;
    }
}
