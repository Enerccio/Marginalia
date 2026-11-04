package com.github.enerccio.marginalia.extensions.conlangtranslator.commands;

import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * The lexicon as commands: list, read, add, change, delete, lock and retire words, accept the new ones, and the check of the lexicon that needs
 * no model. A word an agent adds or changes is the model's and <b>new</b> (the author looks at it); one the author adds at the command bar is the
 * author's, like in the word window.
 */
public class LexiconCommands extends ConlangCommandSupport {

    public static final String LIST_WORDS = "conlang-list-words";
    public static final String GET_WORD = "conlang-get-word";
    public static final String ADD_WORD = "conlang-add-word";
    public static final String UPDATE_WORD = "conlang-update-word";
    public static final String DELETE_WORD = "conlang-delete-word";
    public static final String LOCK_WORD = "conlang-lock-word";
    public static final String UNLOCK_WORD = "conlang-unlock-word";
    public static final String RETIRE_WORD = "conlang-retire-word";
    public static final String RESTORE_WORD = "conlang-restore-word";
    public static final String ACCEPT_WORDS = "conlang-accept-words";
    public static final String CHECK_LEXICON = "conlang-check-lexicon";

    private static final String RESULT_WORD = "JSON: the word as it is now";

    @Command(commandName = LIST_WORDS, linkedCommand = GET_WORD, humanNameText = "List words", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "Lists the words of a language, filtered and paged. Reads only.",
            llmDescriptionText = "Lists the words of the lexicon as JSON rows (id, word, senses, category, kind, status, origin, rationale...), 50 a page. Filters, all optional: category (name), "
                    + "status (new = not looked at by the author, accepted, locked, retired), text (a part of the word or of a meaning, the case does not matter). offset and limit page the answer; "
                    + "the answer says how many words match. Reads only.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "category", type = String.class, optional = true, descriptionText = "Only the words of this category (its name; 'none' for the words without one)"),
                    @CommandParameter(name = "status", type = String.class, optional = true, allowed = {"new", "accepted", "locked", "retired"}, descriptionText = "Only the words in this state"),
                    @CommandParameter(name = "text", type = String.class, optional = true, descriptionText = "Only the words with this text in the word, a meaning, the rationale or the notes"),
                    @CommandParameter(name = "offset", type = Integer.class, optional = true, defaultValue = "0", min = "0", descriptionText = "How many matching words to skip"),
                    @CommandParameter(name = "limit", type = Integer.class, optional = true, defaultValue = "50", min = "1", max = "200", descriptionText = "How many words to return, at most 200")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the words"))
    public void listWords(CommandContext context) throws Exception {
        Language language = language(context);
        String categoryName = context.get("category", String.class);
        String status = context.get("status", String.class);
        String text = StringUtils.defaultString(context.get("text", String.class)).strip().toLowerCase(Locale.ROOT);
        EntryStatus wanted = StringUtils.isBlank(status) ? null : EntryStatus.valueOf(status.strip().toUpperCase(Locale.ROOT));
        Category category = null;
        boolean loose = false;
        if (StringUtils.isNotBlank(categoryName)) {
            if (categoryName.strip().equalsIgnoreCase("none")) {
                loose = true;
            } else {
                category = category(language, categoryName);
            }
        }
        Integer offsetValue = context.get("offset", Integer.class);
        Integer limitValue = context.get("limit", Integer.class);
        int offset = Math.max(0, offsetValue == null ? 0 : offsetValue);
        int limit = Math.min(MAX_LIMIT, Math.max(1, limitValue == null ? DEFAULT_LIMIT : limitValue));
        List<LexiconEntry> matching = new ArrayList<>();
        for (LexiconEntry entry : language.getLexicon()) {
            if (wanted != null && entry.getStatus() != wanted) {
                continue;
            }
            if (category != null && !category.getId().equals(entry.getCategory())) {
                continue;
            }
            if (loose && entry.getCategory() != null && language.findCategory(entry.getCategory()) != null) {
                continue;
            }
            if (!text.isEmpty() && !matches(entry, text)) {
                continue;
            }
            matching.add(entry);
        }
        JsonArray rows = new JsonArray();
        for (LexiconEntry entry : matching.subList(Math.min(offset, matching.size()), Math.min(matching.size(), offset + limit))) {
            rows.add(wordRow(language, entry));
        }
        JsonObject result = new JsonObject();
        result.addProperty("matching", matching.size());
        result.addProperty("offset", offset);
        result.add("words", rows);
        if (offset + limit < matching.size()) {
            result.addProperty("hint", "More words: call again with offset " + (offset + limit));
        }
        answer(context, result);
    }

    private static boolean matches(LexiconEntry entry, String text) {
        return entry.getWord().toLowerCase(Locale.ROOT).contains(text)
                || entry.getSenses().stream().anyMatch(sense -> sense.getGloss().toLowerCase(Locale.ROOT).contains(text))
                || entry.getRationale().toLowerCase(Locale.ROOT).contains(text)
                || entry.getNotes().toLowerCase(Locale.ROOT).contains(text);
    }

    @Command(commandName = GET_WORD, linkedCommand = LIST_WORDS, humanNameText = "Get a word", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "Shows one word of the lexicon. Reads only.",
            llmDescriptionText = "Returns one word with its senses, category, kind, status, rationale, rule, notes, register and variants (the same concept in other registers). Reads only.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "word", type = String.class, descriptionText = PARAM_WORD)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_WORD))
    public void getWord(CommandContext context) throws Exception {
        Language language = language(context);
        answer(context, wordRow(language, word(language, context.get("word", String.class))));
    }

    @Command(commandName = ADD_WORD, linkedCommand = LIST_WORDS, humanNameText = "Add a word", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Adds a word to the lexicon.",
            llmDescriptionText = "Adds a word at the end of the lexicon and returns it. meaning is its gloss (several meanings separated by semicolons or new lines). Give the rationale: the rule or the roots it is made of "
                    + "(\"drak war + gild house\", the compound rule), not \"new root\". The same form with another meaning is a homophone and allowed; the same form with the same meaning is refused (change the word that exists). "
                    + "A word you add is new: the author looks at it. category and register must exist (conlang-list-categories, conlang-list-registers; conlang-add-category makes one).",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "word", type = String.class, descriptionText = "The word as the story writes it (romanized)"),
                    @CommandParameter(name = "meaning", type = String.class, descriptionText = "What it means; several meanings separated by semicolons or new lines"),
                    @CommandParameter(name = "category", type = String.class, optional = true, descriptionText = PARAM_CATEGORY),
                    @CommandParameter(name = "rationale", type = String.class, optional = true, descriptionText = "Why the word is as it is: the rule or the roots"),
                    @CommandParameter(name = "kind", type = String.class, optional = true, allowed = {"root", "compound", "affix", "particle", "name", "number", "phrase", "other"}, descriptionText = "What the word is, root when left out"),
                    @CommandParameter(name = "partOfSpeech", type = String.class, optional = true, descriptionText = "Part of speech of the first meaning, free text; a language without word classes leaves it out"),
                    @CommandParameter(name = "register", type = String.class, optional = true, descriptionText = "The register of the word; neutral when left out"),
                    @CommandParameter(name = "notes", type = String.class, optional = true, descriptionText = "Notes for the author")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_WORD))
    public void addWord(CommandContext context) throws Exception {
        Language language = language(context);
        String form = required(context.get("word", String.class), "the word");
        List<Sense> senses = senses(StringUtils.defaultString(context.get("meaning", String.class)), context.get("partOfSpeech", String.class));
        for (LexiconEntry existing : language.findByForm(form)) {
            for (Sense sense : senses) {
                if (existing.hasGloss(sense.getGloss())) {
                    throw new IllegalArgumentException("The word " + existing.getWord() + " exists already with the meaning '" + sense.getGloss()
                            + "', conlang-update-word changes it");
                }
            }
        }
        String categoryName = context.get("category", String.class);
        Category category = StringUtils.isBlank(categoryName) ? null : category(language, categoryName);
        String registerName = context.get("register", String.class);
        Register register = StringUtils.isBlank(registerName) ? null : register(language, registerName);
        EntryKind kind = kind(context.get("kind", String.class));
        boolean agent = byAgent(context);
        LexiconEntry entry = agent
                ? LexiconEntry.ofModel(form, senses.getFirst().getGloss(), StringUtils.defaultString(context.get("rationale", String.class)).strip(),
                Origin.model(null, "agent", System.currentTimeMillis()))
                : LexiconEntry.ofAuthor(form, senses.getFirst().getGloss());
        entry.setSenses(senses);
        if (!agent) {
            entry.setRationale(StringUtils.defaultString(context.get("rationale", String.class)).strip());
        }
        entry.setCategory(category == null ? null : category.getId());
        entry.setRegister(register == null ? null : register.getId());
        entry.setKind(kind == null ? EntryKind.ROOT : kind);
        entry.setNotes(StringUtils.defaultString(context.get("notes", String.class)).strip());
        Language changed = change(context, language, stored -> stored.addEntry(entry));
        answer(context, wordRow(changed, changed.findEntry(entry.getId())));
    }

    @Command(commandName = UPDATE_WORD, linkedCommand = LIST_WORDS, humanNameText = "Update a word", dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Changes a word of the lexicon.",
            llmDescriptionText = "Changes fields of a word; pass only what to change. meaning replaces all its meanings. A word the author locked can't be changed (the lock says the model may use it but never redefine it). "
                    + "A change by you makes the word new again, for the author to look at. Words already used in translated parts keep the old form: a changed word is a reason to check those parts (conlang-check-translation). "
                    + "Returns the word.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "word", type = String.class, descriptionText = PARAM_WORD),
                    @CommandParameter(name = "newForm", type = String.class, optional = true, descriptionText = "The new form of the word"),
                    @CommandParameter(name = "meaning", type = String.class, optional = true, descriptionText = "All its meanings, replacing the old ones; several separated by semicolons or new lines"),
                    @CommandParameter(name = "category", type = String.class, optional = true, descriptionText = PARAM_CATEGORY + "; 'none' removes it"),
                    @CommandParameter(name = "rationale", type = String.class, optional = true, descriptionText = "Why the word is as it is"),
                    @CommandParameter(name = "kind", type = String.class, optional = true, allowed = {"root", "compound", "affix", "particle", "name", "number", "phrase", "other"}, descriptionText = "What the word is"),
                    @CommandParameter(name = "register", type = String.class, optional = true, descriptionText = "The register; 'none' makes it neutral"),
                    @CommandParameter(name = "notes", type = String.class, optional = true, descriptionText = "Notes for the author")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_WORD))
    public void updateWord(CommandContext context) throws Exception {
        Language language = language(context);
        LexiconEntry entry = word(language, context.get("word", String.class));
        if (entry.isLocked()) {
            throw new IllegalStateException("The word " + entry.getWord() + " is locked by the author, it can't be redefined (conlang-unlock-word lifts the lock)");
        }
        String newForm = context.get("newForm", String.class);
        String meaning = context.get("meaning", String.class);
        String categoryName = context.get("category", String.class);
        String rationale = context.get("rationale", String.class);
        EntryKind kind = kind(context.get("kind", String.class));
        String registerName = context.get("register", String.class);
        String notes = context.get("notes", String.class);
        if (newForm == null && meaning == null && categoryName == null && rationale == null && kind == null && registerName == null && notes == null) {
            throw new IllegalArgumentException("Nothing to change: pass the new form, the meaning, the category, the rationale, the kind, the register or the notes");
        }
        List<Sense> senses = meaning == null ? null : senses(meaning, null);
        String categoryId = categoryName == null ? null : (isNone(categoryName) ? "" : category(language, categoryName).getId());
        String registerId = registerName == null ? null : (isNone(registerName) ? "" : register(language, registerName).getId());
        boolean agent = byAgent(context);
        Language changed = change(context, language, stored -> {
            LexiconEntry target = stored.findEntry(entry.getId());
            if (target == null) {
                throw new IllegalArgumentException("The word is gone");
            }
            if (newForm != null) {
                target.setWord(required(newForm, "the new form").strip());
            }
            if (senses != null) {
                target.setSenses(senses);
            }
            if (categoryId != null) {
                target.setCategory(categoryId.isEmpty() ? null : categoryId);
            }
            if (rationale != null) {
                target.setRationale(rationale.strip());
            }
            if (kind != null) {
                target.setKind(kind);
            }
            if (registerId != null) {
                target.setRegister(registerId.isEmpty() ? null : registerId);
            }
            if (notes != null) {
                target.setNotes(notes.strip());
            }
            if (agent) {
                target.setOrigin(Origin.model(null, "agent", System.currentTimeMillis()));
                if (target.getStatus() == EntryStatus.ACCEPTED) {
                    target.setStatus(EntryStatus.NEW);
                }
            } else {
                target.editedByAuthor();
            }
        });
        answer(context, wordRow(changed, changed.findEntry(entry.getId())));
    }

    private static boolean isNone(String text) {
        return text.strip().equalsIgnoreCase("none");
    }

    @Command(commandName = DELETE_WORD, linkedCommand = LIST_WORDS, humanNameText = "Delete a word", async = true, dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = "Deletes a word from the lexicon for good.",
            llmDescriptionText = "Deletes a word and the links other words had to it. Destructive, asks the user. A word that is replaced should be retired (conlang-retire-word) instead: it stays in the list, "
                    + "the model is not offered it, and the parts that use it can be found. A locked word can't be deleted.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "word", type = String.class, descriptionText = PARAM_WORD)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "What was deleted"))
    public void deleteWord(CommandContext context) throws Exception {
        Language language = language(context);
        LexiconEntry entry = word(language, context.get("word", String.class));
        if (entry.isLocked()) {
            throw new IllegalStateException("The word " + entry.getWord() + " is locked by the author, it can't be deleted");
        }
        if (!context.confirm("Delete the word " + entry.getWord() + " (" + glosses(entry) + ") of " + language.getName() + "?")) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was deleted");
            }
            return;
        }
        change(context, language, stored -> stored.deleteEntry(entry.getId()));
        context.setReturn("Deleted the word " + entry.getWord());
    }

    @Command(commandName = LOCK_WORD, linkedCommand = LIST_WORDS, humanNameText = "Lock a word", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Locks a word: the model may use it but never redefine it.",
            llmDescriptionText = "Locks a word (a name, a word the story depends on): the translator uses it but is never offered a change of it. Returns the word.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "word", type = String.class, descriptionText = PARAM_WORD)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_WORD))
    public void lockWord(CommandContext context) throws Exception {
        setStatus(context, EntryStatus.LOCKED);
    }

    @Command(commandName = UNLOCK_WORD, linkedCommand = LIST_WORDS, humanNameText = "Unlock a word", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Lifts the lock of a word.",
            llmDescriptionText = "Unlocks a word, so that it can be changed again (it is accepted). The lock is the author's; unlock only a word you were asked to change. Returns the word.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "word", type = String.class, descriptionText = PARAM_WORD)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_WORD))
    public void unlockWord(CommandContext context) throws Exception {
        setStatus(context, EntryStatus.ACCEPTED);
    }

    @Command(commandName = RETIRE_WORD, linkedCommand = LIST_WORDS, humanNameText = "Retire a word", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Keeps a word in the list but does not offer it to the model any more.",
            llmDescriptionText = "Retires a word that was replaced: it stays in the lexicon (the parts that use it can still be read) but the translating model is not offered it. Returns the word.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "word", type = String.class, descriptionText = PARAM_WORD)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_WORD))
    public void retireWord(CommandContext context) throws Exception {
        setStatus(context, EntryStatus.RETIRED);
    }

    @Command(commandName = RESTORE_WORD, linkedCommand = LIST_WORDS, humanNameText = "Restore a word", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Offers a retired word to the model again.",
            llmDescriptionText = "Makes a retired word offered to the translating model again (accepted). Returns the word.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "word", type = String.class, descriptionText = PARAM_WORD)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_WORD))
    public void restoreWord(CommandContext context) throws Exception {
        setStatus(context, EntryStatus.ACCEPTED);
    }

    private void setStatus(CommandContext context, EntryStatus status) throws Exception {
        Language language = language(context);
        LexiconEntry entry = word(language, context.get("word", String.class));
        Language changed = change(context, language, stored -> {
            LexiconEntry target = stored.findEntry(entry.getId());
            if (target == null) {
                throw new IllegalArgumentException("The word is gone");
            }
            target.setStatus(status);
        });
        answer(context, wordRow(changed, changed.findEntry(entry.getId())));
    }

    @Command(commandName = ACCEPT_WORDS, linkedCommand = LIST_WORDS, humanNameText = "Accept new words", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = "Marks the new words as looked at.",
            llmDescriptionText = "Marks the words the author has not looked at yet (status new) as accepted, all of them or the ones of a category. This is the author's review: do it for words you were told to accept, "
                    + "not to make your own words look reviewed. Returns how many words were accepted.",
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE),
                    @CommandParameter(name = "category", type = String.class, optional = true, descriptionText = "Only the new words of this category")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: how many were accepted"))
    public void acceptWords(CommandContext context) throws Exception {
        Language language = language(context);
        String categoryName = context.get("category", String.class);
        Category category = StringUtils.isBlank(categoryName) ? null : category(language, categoryName);
        int[] accepted = {0};
        change(context, language, stored -> {
            for (LexiconEntry entry : stored.getLexicon()) {
                if (entry.getStatus() == EntryStatus.NEW && (category == null || category.getId().equals(entry.getCategory()))) {
                    entry.setStatus(EntryStatus.ACCEPTED);
                    accepted[0]++;
                }
            }
        });
        JsonObject result = new JsonObject();
        result.addProperty("accepted", accepted[0]);
        answer(context, result);
    }

    @Command(commandName = CHECK_LEXICON, linkedCommand = LIST_WORDS, humanNameText = "Check the lexicon", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = "Finds repeated meanings, words without a category and compounds with unknown parts. Reads only.",
            llmDescriptionText = "Checks the lexicon without a model: words with the same form and a shared meaning (repeats), words that belong to no category, compounds whose parts are not in the lexicon, "
                    + "and, as information only, homophones (the same form with different meanings is normal). Reads only.",
            signature = @CommandParameterSignature({@CommandParameter(name = "language", type = String.class, descriptionText = PARAM_LANGUAGE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the findings"))
    public void checkLexicon(CommandContext context) throws Exception {
        Language language = language(context);
        JsonObject result = new JsonObject();
        JsonArray repeats = new JsonArray();
        for (List<LexiconEntry> group : language.repeats()) {
            repeats.add(String.join(" | ", group.stream().map(entry -> entry.getWord() + " = " + glosses(entry) + " [" + entry.getId() + "]").toList()));
        }
        result.add("repeats", repeats);
        JsonArray loose = new JsonArray();
        language.entriesOf(null).stream().filter(LexiconEntry::isOffered).forEach(entry -> loose.add(entry.getWord()));
        result.add("withoutCategory", loose);
        JsonArray compounds = new JsonArray();
        for (LexiconEntry entry : language.getLexicon()) {
            if (entry.getKind() != EntryKind.COMPOUND || !entry.isOffered()) {
                continue;
            }
            List<String> unknown = new ArrayList<>();
            for (String part : entry.getWord().split("-")) {
                if (!part.isBlank() && language.findByForm(part).stream().noneMatch(LexiconEntry::isOffered)) {
                    unknown.add(part);
                }
            }
            if (!unknown.isEmpty()) {
                compounds.add(entry.getWord() + ": " + String.join(", ", unknown) + " not in the lexicon");
            }
        }
        result.add("compoundsWithUnknownParts", compounds);
        Map<String, List<LexiconEntry>> forms = new LinkedHashMap<>();
        for (LexiconEntry entry : language.getLexicon()) {
            forms.computeIfAbsent(LexiconEntry.normalize(entry.getWord(), language.isHyphenStructural()), key -> new ArrayList<>()).add(entry);
        }
        JsonArray homophones = new JsonArray();
        Set<List<LexiconEntry>> repeated = new HashSet<>(language.repeats());
        forms.forEach((form, group) -> {
            if (!form.isEmpty() && group.size() > 1 && !repeated.contains(group)) {
                homophones.add(group.getFirst().getWord() + ": " + String.join(" / ", group.stream().map(ConlangCommandSupport::glosses).toList()));
            }
        });
        result.add("homophonesForInformation", homophones);
        answer(context, result);
    }
}
