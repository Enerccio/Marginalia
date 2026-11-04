package com.github.enerccio.marginalia.extensions.conlangtranslator.commands;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read.ManuscriptReadSupport;
import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.Change;
import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.ChangeSet;
import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.FieldChange;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.LanguageAccess;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Consumer;

/**
 * What the commands of the plugin have in common: the language a command names (the library's, or the run's own copy of it inside an agent run, see
 * {@link LanguageAccess}), the things inside a language (a rules section, a category, a word) found by what an agent can type, and the small JSON
 * rows of the answers. No logic of the plugin is here: the commands call the same services as the dialogs.
 * <p>
 * Nothing is addressed by a uuid except a language (by name or id) and a word (by its form; the id only to tell homophones apart). A rules section is
 * addressed by its title or its number in {@code conlang-list-rules}, a category by its name or number.
 */
public abstract class ConlangCommandSupport extends ManuscriptReadSupport {

    protected static final String PARAM_LANGUAGE = "The language: its name (the case does not matter) or its id, see conlang-list";
    protected static final String PARAM_SECTION = "The rules section: its title, or its number in conlang-list-rules (the first is 1)";
    protected static final String PARAM_CATEGORY = "The category: its name, or its number in conlang-list-categories (the first is 1)";
    protected static final String PARAM_WORD = "The word: its form as the story writes it; when several words have the form (homophones) give the id from conlang-list-words";
    protected static final String PARAM_ASK = "true: return the proposal as a numbered list of changes without applying it; conlang-apply-changes then applies the ones you choose";
    protected static final String PARAM_NOTE = "What you want, in your own words; may be empty";
    protected static final String RESULT_JSON = "JSON: the result";

    /** The longest text a command returns whole (characters). */
    protected static final int MAX_TEXT = 20000;

    protected final LanguageAccess languages = new LanguageAccess();

    /**
     * The book of the scope (the working copy inside an agent run), null when there is none: the library commands do not need a book.
     */
    protected Manuscript scopeBook(CommandContext context) {
        try {
            return context.getScope().getManuscript();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Whether the command was given by an agent (what it adds is the model's, for the author to look at) and not by the author at the command bar.
     */
    protected static boolean byAgent(CommandContext context) {
        return context.getScope().isAgent();
    }

    protected Language language(CommandContext context) throws Exception {
        return languages.resolve(scopeBook(context), context.get("language", String.class));
    }

    /**
     * Changes the named language, then answers with it as it is.
     */
    protected Language change(CommandContext context, Language language, Consumer<Language> change) throws Exception {
        Manuscript book = scopeBook(context);
        languages.update(book, language.getId(), change);
        return languages.find(book, language.getId());
    }

    // ---- finding things inside a language

    protected static RuleSection section(Language language, String reference) {
        List<RuleSection> rules = language.getRules();
        if (StringUtils.isBlank(reference)) {
            throw new IllegalArgumentException("Give the section: its title or its number in conlang-list-rules");
        }
        String wanted = reference.strip();
        for (RuleSection section : rules) {
            if (section.getId().equals(wanted)) {
                return section;
            }
        }
        List<RuleSection> titled = rules.stream().filter(section -> section.getTitle().strip().equalsIgnoreCase(wanted)).toList();
        if (titled.size() == 1) {
            return titled.getFirst();
        }
        if (wanted.matches("\\d{1,6}")) {
            int number = Integer.parseInt(wanted);
            if (number >= 1 && number <= rules.size()) {
                return rules.get(number - 1);
            }
        }
        throw new IllegalArgumentException("There is no rules section " + wanted + ". Sections: " + (rules.isEmpty() ? "none"
                : String.join(", ", rules.stream().map(RuleSection::getTitle).toList())));
    }

    protected static Category category(Language language, String reference) {
        if (StringUtils.isBlank(reference)) {
            throw new IllegalArgumentException("Give the category: its name or its number in conlang-list-categories");
        }
        String wanted = reference.strip();
        Category byId = language.findCategory(wanted);
        if (byId != null) {
            return byId;
        }
        List<Category> named = language.getCategories().stream().filter(category -> category.getName().strip().equalsIgnoreCase(wanted)).toList();
        if (named.size() == 1) {
            return named.getFirst();
        }
        if (wanted.matches("\\d{1,6}")) {
            int number = Integer.parseInt(wanted);
            if (number >= 1 && number <= language.getCategories().size()) {
                return language.getCategories().get(number - 1);
            }
        }
        throw new IllegalArgumentException("There is no category " + wanted + ". Categories: " + (language.getCategories().isEmpty() ? "none"
                : String.join(", ", language.getCategories().stream().map(Category::getName).toList())));
    }

    protected static Register register(Language language, String reference) {
        if (StringUtils.isBlank(reference)) {
            throw new IllegalArgumentException("Give the register: its name");
        }
        String wanted = reference.strip();
        Register byId = language.findRegister(wanted);
        if (byId != null) {
            return byId;
        }
        return language.getRegisters().stream().filter(register -> register.getName().strip().equalsIgnoreCase(wanted)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("There is no register " + wanted + ". Registers: " + (language.getRegisters().isEmpty() ? "none"
                        : String.join(", ", language.getRegisters().stream().map(Register::getName).toList()))));
    }

    /**
     * The word a command names: its id, else its form (when only one word has it), else a unique start of an id (six characters or more).
     */
    protected static LexiconEntry word(Language language, String reference) {
        if (StringUtils.isBlank(reference)) {
            throw new IllegalArgumentException("Give the word");
        }
        String wanted = reference.strip();
        LexiconEntry byId = language.findEntry(wanted);
        if (byId != null) {
            return byId;
        }
        List<LexiconEntry> forms = language.findByForm(wanted);
        if (forms.size() == 1) {
            return forms.getFirst();
        }
        if (forms.size() > 1) {
            throw new IllegalArgumentException("More than one word is written " + wanted + " (homophones), give the id: "
                    + String.join("; ", forms.stream().map(entry -> entry.getId() + " = " + glosses(entry)).toList()));
        }
        if (wanted.length() >= 6) {
            List<LexiconEntry> byPrefix = language.getLexicon().stream().filter(entry -> entry.getId().startsWith(wanted)).toList();
            if (byPrefix.size() == 1) {
                return byPrefix.getFirst();
            }
        }
        throw new IllegalArgumentException("There is no word " + wanted + " in " + language.getName());
    }

    // ---- reading what a caller typed

    protected static EntryKind kind(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return EntryKind.valueOf(text.strip().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_'));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("The kind is root, compound, affix, particle, name, number, phrase or other, not " + text);
        }
    }

    protected static Audience audience(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        return switch (text.strip().toLowerCase(Locale.ROOT)) {
            case "translator", "model" -> Audience.MODEL;
            case "designer" -> Audience.DESIGNER;
            case "nobody", "none", "author" -> Audience.NONE;
            default -> throw new IllegalArgumentException("The audience is translator, designer or nobody, not " + text);
        };
    }

    protected static String audienceName(Audience audience) {
        return switch (audience) {
            case MODEL -> "translator";
            case DESIGNER -> "designer";
            case NONE -> "nobody";
        };
    }

    /** The senses of a text: one per line or separated by semicolons. */
    protected static List<Sense> senses(String text, String partOfSpeech) {
        List<Sense> senses = new ArrayList<>();
        for (String piece : text.split("[\\n;]")) {
            if (!piece.isBlank()) {
                senses.add(new Sense(piece.strip()));
            }
        }
        if (senses.isEmpty()) {
            throw new IllegalArgumentException("Give the meaning of the word");
        }
        if (StringUtils.isNotBlank(partOfSpeech)) {
            senses.getFirst().setPartOfSpeech(partOfSpeech.strip());
        }
        return senses;
    }

    protected static String required(String value, String what) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("Give " + what);
        }
        return value.strip();
    }

    /** The numbers of a text such as {@code 1,3,5} or {@code 2-4}; null for blank (all). */
    protected static Set<Integer> numbers(String text, int max) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        Set<Integer> numbers = new TreeSet<>();
        for (String piece : text.split("[,\\s]+")) {
            if (piece.isBlank()) {
                continue;
            }
            try {
                if (piece.contains("-")) {
                    String[] range = piece.split("-", 2);
                    for (int i = Integer.parseInt(range[0].strip()); i <= Integer.parseInt(range[1].strip()); i++) {
                        numbers.add(i);
                    }
                } else {
                    numbers.add(Integer.parseInt(piece.strip()));
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("The numbers are separated by commas (ranges as 2-4), not " + text);
            }
        }
        for (int number : numbers) {
            if (number < 1 || number > max) {
                throw new IllegalArgumentException("There is no change " + number + ", there are " + max);
            }
        }
        return numbers;
    }

    // ---- rows

    protected static JsonArray array(List<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    protected static String glosses(LexiconEntry entry) {
        return String.join("; ", entry.getSenses().stream().map(Sense::getGloss).toList());
    }

    protected static JsonObject languageRow(Language language) {
        JsonObject row = new JsonObject();
        row.addProperty("id", language.getId());
        row.addProperty("name", language.getName());
        if (!language.getDescription().isBlank()) {
            row.addProperty("description", language.getDescription());
        }
        row.addProperty("rulesSections", language.getRules().size());
        row.addProperty("categories", language.getCategories().size());
        row.addProperty("registers", language.getRegisters().size());
        row.addProperty("words", language.getLexicon().size());
        row.addProperty("newWords", language.newEntries().size());
        row.addProperty("hyphenStructural", language.isHyphenStructural());
        return row;
    }

    protected static JsonObject wordRow(Language language, LexiconEntry entry) {
        JsonObject row = new JsonObject();
        row.addProperty("id", entry.getId());
        row.addProperty("word", entry.getWord());
        if (!entry.getScript().isBlank()) {
            row.addProperty("script", entry.getScript());
        }
        JsonArray senses = new JsonArray();
        for (Sense sense : entry.getSenses()) {
            JsonObject s = new JsonObject();
            s.addProperty("gloss", sense.getGloss());
            if (!sense.getPartOfSpeech().isBlank()) {
                s.addProperty("partOfSpeech", sense.getPartOfSpeech());
            }
            if (!sense.getNote().isBlank()) {
                s.addProperty("note", sense.getNote());
            }
            senses.add(s);
        }
        row.add("senses", senses);
        Category category = language.findCategory(entry.getCategory());
        if (category != null) {
            row.addProperty("category", category.getName());
        }
        row.addProperty("kind", entry.getKind().name().toLowerCase(Locale.ROOT));
        row.addProperty("status", entry.getStatus().name().toLowerCase(Locale.ROOT));
        row.addProperty("origin", entry.getOrigin().getKind().name().toLowerCase(Locale.ROOT));
        if (!entry.getRationale().isBlank()) {
            row.addProperty("rationale", entry.getRationale());
        }
        if (!entry.getRule().isBlank()) {
            row.addProperty("rule", entry.getRule());
        }
        if (!entry.getNotes().isBlank()) {
            row.addProperty("notes", entry.getNotes());
        }
        Register register = language.findRegister(entry.getRegister());
        if (register != null) {
            row.addProperty("register", register.getName());
        }
        if (!entry.getVariants().isEmpty()) {
            row.add("variants", array(entry.getVariants().stream().map(id -> {
                LexiconEntry other = language.findEntry(id);
                return other == null ? id : other.getWord();
            }).toList()));
        }
        return row;
    }

    /** The changes of a set as a numbered list, for a proposal an agent chooses from. */
    protected static JsonArray changeRows(ChangeSet set) {
        JsonArray list = new JsonArray();
        int number = 1;
        for (Change change : set.changes()) {
            JsonObject row = new JsonObject();
            row.addProperty("number", number++);
            row.addProperty("change", change.kind().name().toLowerCase(Locale.ROOT));
            row.addProperty("about", change.target().name().toLowerCase(Locale.ROOT));
            row.addProperty("what", change.label());
            JsonArray fields = new JsonArray();
            for (FieldChange field : change.fields()) {
                if (!field.changes()) {
                    continue;
                }
                JsonObject f = new JsonObject();
                f.addProperty("field", field.field());
                f.addProperty("before", set.display(field.field(), field.before()));
                f.addProperty("after", set.display(field.field(), field.after()));
                if (field.isConflict()) {
                    f.addProperty("conflict", true);
                }
                fields.add(f);
            }
            row.add("fields", fields);
            if (change.hasConflict()) {
                row.addProperty("conflict", true);
            }
            if (!change.dependsOn().isEmpty()) {
                row.addProperty("needsAlso", String.join(", ", change.dependsOn()));
            }
            list.add(row);
        }
        return list;
    }
}
