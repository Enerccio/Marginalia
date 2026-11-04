package com.github.enerccio.marginalia.extensions.knowledgetracker.commands;

import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read.ManuscriptReadSupport;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.extensions.knowledgetracker.block.KnowledgeBlockBuilder;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.Change;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ChangeApplier;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.*;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.BranchProcessor.Report;
import com.github.enerccio.marginalia.extensions.knowledgetracker.scene.PresentCharacters;
import com.github.enerccio.marginalia.extensions.knowledgetracker.service.*;
import com.github.enerccio.marginalia.extensions.knowledgetracker.service.ExtractionService.Proposal;
import com.github.enerccio.marginalia.extensions.knowledgetracker.service.KnowledgeProcessService.Status;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;

import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.*;

/**
 * The Knowledge Tracker as commands, for the command bar, for agents and for templates: {@code knowledge-status},
 * {@code list-tracked-characters}, {@code add-tracked-character}, {@code update-tracked-character},
 * {@code delete-tracked-character}, {@code list-knowledge}, {@code add-knowledge}, {@code retract-knowledge},
 * {@code restore-knowledge}, {@code delete-knowledge} and {@code knowledge-tracker-block}, the text of the prompt, for the
 * {@code {{command::knowledge-tracker-block::<characters>}}} macro.
 * <p>
 * A tracked character is addressed by its <b>name</b> or an alias (the case does not matter), or by its <b>number</b> in
 * {@code list-tracked-characters}. An item is addressed by its number in {@code list-knowledge} of the character. No uuid
 * is given out or accepted. A command that changes a field takes only the fields it is given.
 */
public class KnowledgeTrackerCommands extends ManuscriptReadSupport {

    public static final String STATUS = "knowledge-status";
    public static final String LIST_CHARACTERS = "list-tracked-characters";
    public static final String ADD_CHARACTER = "add-tracked-character";
    public static final String UPDATE_CHARACTER = "update-tracked-character";
    public static final String DELETE_CHARACTER = "delete-tracked-character";
    public static final String LIST_KNOWLEDGE = "list-knowledge";
    public static final String ADD_KNOWLEDGE = "add-knowledge";
    public static final String RETRACT_KNOWLEDGE = "retract-knowledge";
    public static final String RESTORE_KNOWLEDGE = "restore-knowledge";
    public static final String DELETE_KNOWLEDGE = "delete-knowledge";
    public static final String BLOCK = "knowledge-tracker-block";
    public static final String PROCESS = "knowledge-process";
    public static final String REPROCESS = "knowledge-reprocess";
    public static final String SET_START = "knowledge-set-start";
    public static final String APPLY_CHANGES = "apply-knowledge-changes";
    public static final String TIDY = "knowledge-tidy";
    public static final String LOCK_KNOWLEDGE = "lock-knowledge";
    public static final String UNLOCK_KNOWLEDGE = "unlock-knowledge";

    private static final String STATUS_TEXT = "The settings of the Knowledge Tracker, the tracked characters and where the processing stands. Reads only.";
    private static final String LIST_CHARACTERS_TEXT = "Lists the tracked characters with the number of things each knows. Reads only.";
    private static final String LIST_CHARACTERS_LLM = "Lists the characters whose knowledge the book tracks, in order, as JSON: number, name, aliases and how "
            + "many active and old items each has. Characters are addressed by name or alias in the other commands. Reads only.";
    private static final String ADD_CHARACTER_TEXT = "Starts tracking a character.";
    private static final String ADD_CHARACTER_LLM = "Starts tracking what a character knows and does not know and returns it. Only tracked characters are "
            + "described to the model. Aliases are other names the character is written as in the present characters field, separated by commas. "
            + "Add what it knows with add-knowledge.";
    private static final String UPDATE_CHARACTER_TEXT = "Changes the name, the aliases or the people met of a tracked character.";
    private static final String UPDATE_CHARACTER_LLM = "Changes a tracked character. Pass only the fields to change; empty aliases remove them all; met replaces the list "
            + "of the people the character has met (empty = none). Returns the character.";
    private static final String DELETE_CHARACTER_TEXT = "Stops tracking a character and forgets everything it knows.";
    private static final String DELETE_CHARACTER_LLM = "Removes a tracked character with all its items. Destructive.";
    private static final String LIST_KNOWLEDGE_TEXT = "Lists what a tracked character knows and does not know. Reads only.";
    private static final String LIST_KNOWLEDGE_LLM = "Lists the items of a tracked character as JSON: number, whether it knows or does not know, the text, "
            + "whether it is active (sent to the model) or old (kept, not sent), where it came from, and the people the character has met. "
            + "Items are addressed by their number in the other commands. Reads only.";
    private static final String ADD_KNOWLEDGE_TEXT = "Adds something a tracked character knows or does not know.";
    private static final String ADD_KNOWLEDGE_LLM = "Adds an item to a tracked character, active at once, and returns the character's items. "
            + "The text is about anything the character knows (a person, a place, a secret, an event); kind is knows (default) or does-not-know. "
            + "Write what the character does not know explicitly, that is what the model respects (\"does not know that Thomas is her father\").";
    private static final String RETRACT_KNOWLEDGE_TEXT = "Makes an item old: kept and shown, not sent to the model.";
    private static final String RETRACT_KNOWLEDGE_LLM = "Retracts an item because it is not true any more (\"has not met Thomas\" after they meet). The item is kept as "
            + "old knowledge and can be restored. Returns the character's items.";
    private static final String RESTORE_KNOWLEDGE_TEXT = "Makes an old item active again.";
    private static final String RESTORE_KNOWLEDGE_LLM = "Makes an old item active again, from now on. Returns the character's items.";
    private static final String DELETE_KNOWLEDGE_TEXT = "Deletes an item for good.";
    private static final String DELETE_KNOWLEDGE_LLM = "Deletes an item for good, the only way to really remove one (retract-knowledge keeps it). Destructive.";
    private static final String BLOCK_TEXT = "What the tracked characters of a scene know, as plain text, the way the prompt gets it. Made for the "
            + "{{command::knowledge-tracker-block::<characters>}} macro of a template: the argument is the list of the characters of the scene, "
            + "for example {{command::knowledge-tracker-block::{{presentCharacters}}}}. Only tracked characters on the list are described. "
            + "Reads only.";

    // A command that asks the user (context.confirm) is async = true: it waits on a worker thread while the UI shows the question.

    private static final String PROCESS_TEXT = "Reads the parts of the story that are not processed yet: notes who has met whom and, when the book is set to read "
            + "with the model, asks the model what the characters learned.";
    private static final String PROCESS_LLM = "Reads the parts after the cursor of the active branch, from the start point on, and records which people each tracked "
            + "character has met (the names in the present characters field of the parts it is in). When the book is set to read what the characters learn with "
            + "the model, it also asks the model, for the next parts (the Parts per run of the settings, or parts), and applies what it finds. Pass ask true "
            + "to get what the model found as a numbered list of changes instead, apply-knowledge-changes then applies the ones you choose. The last part counts "
            + "as settled, so call it when the part is final. Returns how many parts were read and how many are left.";
    private static final String APPLY_TEXT = "Applies the changes that knowledge-process returned for review.";
    private static final String APPLY_LLM = "Applies the changes that knowledge-process (ask true) held, all of them or the numbers you give separated by commas "
            + "(a retraction makes an item old, an addition is added; the others are left out, an addition left out is kept as old knowledge), and moves the "
            + "cursor over the parts they came from. Fails when the story changed since, then call knowledge-process again.";
    private static final String PARAM_ASK = "true: return what the model found as numbered changes without applying them";
    private static final String PARAM_CHANGE_NUMBERS = "The numbers of the changes to apply, separated by commas; all of them when left out";
    private static final String REPROCESS_TEXT = "Takes back what was learned from a part and the later parts and reads them again.";
    private static final String REPROCESS_LLM = "Reprocess from a part of the active branch (its handle, or its position, the first part is 1): what was learned from it and "
            + "from the later parts is dropped (items and retractions written by hand stay), the cursor goes back to just before it and the parts are read "
            + "again. Use it after the author edited or rewrote an earlier part. Asks the user first.";
    private static final String SET_START_TEXT = "Sets the part where processing begins.";
    private static final String SET_START_LLM = "Sets the start point of the processing: the part (its handle, or its position, the first part is 1) the plugin begins at; "
            + "the parts before it are never processed. Without a part it is the start of the book. Moving it back makes the parts from the new start unread again.";

    private static final String TIDY_TEXT = "Asks the model which of the learned items to drop or merge; locked items and what you wrote are not touched.";
    private static final String TIDY_LLM = "The tidy pass: the model reads the items it learned for each tracked character (not the locked ones, not the ones written by "
            + "hand), with the last processed parts for context, and drops what is not worth keeping (trivia, events everybody saw, overtaken, duplicates) or merges "
            + "items about the same thing. The dropped items become old knowledge and can be restored. Pass ask true to get the changes as a numbered list instead; "
            + "apply-knowledge-changes then applies the ones you choose. Needs the model reading setting on.";
    private static final String LOCK_TEXT = "Locks an item so that the tidy pass leaves it alone.";
    private static final String LOCK_LLM = "Locks an item (a betrayal, a secret): the tidy pass never sees it. The story can still contradict it, then it is retracted as usual. Returns the character's items.";
    private static final String UNLOCK_TEXT = "Lets the tidy pass see the item again.";
    private static final String UNLOCK_LLM = "Unlocks an item, so that the tidy pass may drop or merge it. Returns the character's items.";

    private static final String PARAM_PART = "The part: its handle, or its position on the active branch (the first part is 1)";
    private static final String PARAM_PARTS = "How many parts to read at most; all of them when it is left out";
    private static final String PARAM_CHARACTER = "Name or alias of the tracked character (the case does not matter), or its number in list-tracked-characters";
    private static final String PARAM_NAME = "The name";
    private static final String PARAM_MET = "The people the character has met, separated by commas; replaces the whole list, empty = nobody";
    private static final String PARAM_ALIASES = "Other names of the character, separated by commas; empty = none";
    private static final String PARAM_ITEM = "Number of the item in list-knowledge of the character, the first is 1";
    private static final String PARAM_TEXT = "What the character knows or does not know, one short sentence";
    private static final String PARAM_KIND = "knows (default) or does-not-know";
    private static final String PARAM_CHARACTERS = "The characters of the scene as free text, for example the present characters field";
    private static final String RESULT_CHARACTER = "JSON: the tracked character as it is now";
    private static final String RESULT_ITEMS = "JSON: the items of the character as they are now";

    private final KnowledgeStateService service;
    private final KnowledgeProcessService processService;
    private final ExtractionService extraction;
    private final ExtractionRuns runs;

    public KnowledgeTrackerCommands(KnowledgeStateService service, KnowledgeProcessService processService,
                                    ExtractionService extraction, ExtractionRuns runs) {
        this.service = service;
        this.processService = processService;
        this.extraction = extraction;
        this.runs = runs;
    }

    // --- the block, for templates ---

    @Command(commandName = BLOCK, humanNameText = "Knowledge of the characters", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            availableInMacros = true, descriptionText = BLOCK_TEXT,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "characters", type = String.class, descriptionText = PARAM_CHARACTERS)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "Text: what the tracked characters of the list know; empty when none of them is tracked"))
    public void block(CommandContext context) throws Exception {
        Book book = new Book(context);
        KnowledgeState state = service.load(book.manuscript);
        List<String> separators = KnowledgeBlockBuilder.effectiveSeparators(state, service.getUserSeparators());
        context.setReturn(KnowledgeBlockBuilder.build(state, context.get("characters", String.class), separators));
    }

    // --- status ---

    @Command(commandName = STATUS, humanNameText = "Knowledge status", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = STATUS_TEXT, signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the settings and the state"))
    public void status(CommandContext context) throws Exception {
        Book book = new Book(context);
        KnowledgeState state = service.load(book.manuscript);
        JsonObject result = new JsonObject();
        result.addProperty("trackedCharacters", state.getCharacters().size());
        result.addProperty("inPrompt", state.getSettings().isInPrompt());
        result.addProperty("depth", state.getSettings().getDepth());
        result.addProperty("role", state.getSettings().getRole().name().toLowerCase(Locale.ROOT));
        result.addProperty("closedWorld", state.getSettings().isClosedWorld());
        result.add("separators", array(state.getSettings().getSeparators()));
        result.addProperty("autoProcess", state.getSettings().isAutoProcess());
        result.addProperty("hasStartPoint", state.getStartMessage() != null);
        Status processing = processService.status(book.manuscript);
        result.addProperty("parts", processing.parts());
        result.addProperty("processedUpToPart", processing.cursor());
        result.addProperty("unreadParts", processing.unread());
        result.addProperty("partsChangedSinceProcessed", processing.changed().size());
        answer(context, result);
    }

    // --- tracked characters ---

    @Command(commandName = LIST_CHARACTERS, linkedCommand = LIST_KNOWLEDGE, humanNameText = "List tracked characters",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = LIST_CHARACTERS_TEXT, llmDescriptionText = LIST_CHARACTERS_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the tracked characters"))
    public void listCharacters(CommandContext context) throws Exception {
        Book book = new Book(context);
        KnowledgeState state = service.load(book.manuscript);
        JsonArray list = new JsonArray();
        for (int i = 0; i < state.getCharacters().size(); i++) {
            list.add(characterRow(state.getCharacters().get(i), i + 1));
        }
        JsonObject result = new JsonObject();
        result.add("characters", list);
        if (list.isEmpty()) {
            result.addProperty("hint", "No character is tracked yet, add-tracked-character adds one");
        }
        answer(context, result);
    }

    @Command(commandName = ADD_CHARACTER, linkedCommand = LIST_CHARACTERS, humanNameText = "Track a character",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = ADD_CHARACTER_TEXT, llmDescriptionText = ADD_CHARACTER_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, descriptionText = PARAM_NAME)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_CHARACTER))
    public void addCharacter(CommandContext context) throws Exception {
        runAddCharacter(context);
    }

    @Command(commandName = ADD_CHARACTER, linkedCommand = LIST_CHARACTERS, humanNameText = "Track a character",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = ADD_CHARACTER_TEXT, llmDescriptionText = ADD_CHARACTER_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, descriptionText = PARAM_NAME),
                    @CommandParameter(name = "aliases", type = String.class, optional = true, descriptionText = PARAM_ALIASES)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_CHARACTER))
    public void addCharacterAliases(CommandContext context) throws Exception {
        runAddCharacter(context);
    }

    private void runAddCharacter(CommandContext context) throws Exception {
        Book book = new Book(context);
        KnowledgeState state = service.load(book.manuscript);
        String name = requiredName(context.get("name", String.class));
        List<String> aliases = aliases(context.get("aliases", String.class));
        TrackedCharacter used = state.findClash(null, name, aliases);
        if (used != null) {
            throw new IllegalArgumentException("The name or an alias is already used by " + used.getName());
        }
        TrackedCharacter character = new TrackedCharacter(name);
        character.setAliases(aliases);
        state.getCharacters().add(character);
        store(book, state);
        answer(context, characterRow(character, state.getCharacters().size()));
    }

    @Command(commandName = UPDATE_CHARACTER, linkedCommand = LIST_CHARACTERS, humanNameText = "Update tracked character",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = UPDATE_CHARACTER_TEXT, llmDescriptionText = UPDATE_CHARACTER_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER),
                    @CommandParameter(name = "name", type = String.class, optional = true, descriptionText = PARAM_NAME)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_CHARACTER))
    public void updateCharacter(CommandContext context) throws Exception {
        runUpdateCharacter(context);
    }

    @Command(commandName = UPDATE_CHARACTER, linkedCommand = LIST_CHARACTERS, humanNameText = "Update tracked character",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = UPDATE_CHARACTER_TEXT, llmDescriptionText = UPDATE_CHARACTER_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER),
                    @CommandParameter(name = "name", type = String.class, optional = true, descriptionText = PARAM_NAME),
                    @CommandParameter(name = "aliases", type = String.class, optional = true, descriptionText = PARAM_ALIASES)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_CHARACTER))
    public void updateCharacterAliases(CommandContext context) throws Exception {
        runUpdateCharacter(context);
    }

    @Command(commandName = UPDATE_CHARACTER, linkedCommand = LIST_CHARACTERS, humanNameText = "Update tracked character",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = UPDATE_CHARACTER_TEXT, llmDescriptionText = UPDATE_CHARACTER_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER),
                    @CommandParameter(name = "name", type = String.class, optional = true, descriptionText = PARAM_NAME),
                    @CommandParameter(name = "aliases", type = String.class, optional = true, descriptionText = PARAM_ALIASES),
                    @CommandParameter(name = "met", type = String.class, optional = true, descriptionText = PARAM_MET)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_CHARACTER))
    public void updateCharacterMet(CommandContext context) throws Exception {
        runUpdateCharacter(context);
    }

    private void runUpdateCharacter(CommandContext context) throws Exception {
        Book book = new Book(context);
        KnowledgeState state = service.load(book.manuscript);
        TrackedCharacter character = character(state, context.get("character", String.class));
        String name = context.get("name", String.class);
        String aliasText = context.get("aliases", String.class);
        String metText = context.get("met", String.class);
        if (name == null && aliasText == null && metText == null) {
            throw new IllegalArgumentException("Nothing to change: pass the name, the aliases or the people met");
        }
        String newName = name != null ? requiredName(name) : character.getName();
        List<String> newAliases = aliasText != null ? aliases(aliasText) : character.getAliases();
        TrackedCharacter other = state.findClash(character, newName, newAliases);
        if (other != null) {
            throw new IllegalArgumentException("The name or an alias is already used by " + other.getName());
        }
        character.setName(newName);
        character.setAliases(newAliases);
        if (metText != null) {
            character.setMetNames(aliases(metText));
        }
        store(book, state);
        answer(context, characterRow(character, state.getCharacters().indexOf(character) + 1));
    }

    @Command(commandName = DELETE_CHARACTER, linkedCommand = LIST_CHARACTERS, humanNameText = "Stop tracking a character", async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE, descriptionText = DELETE_CHARACTER_TEXT, llmDescriptionText = DELETE_CHARACTER_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "What was deleted"))
    public void deleteCharacter(CommandContext context) throws Exception {
        Book book = new Book(context);
        KnowledgeState state = service.load(book.manuscript);
        TrackedCharacter character = character(state, context.get("character", String.class));
        if (!context.confirm(T.localized(DELETE_THE_TRACKED_CHARACTER_AND_ALL).formatted(character.getName()))) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was deleted");
            }
            return;
        }
        state.deleteCharacter(character.getId());
        store(book, state);
        context.setReturn("Stopped tracking " + character.getName() + " (" + character.getItems().size() + " items deleted)");
    }

    // --- items ---

    @Command(commandName = LIST_KNOWLEDGE, linkedCommand = LIST_CHARACTERS, humanNameText = "List knowledge",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = LIST_KNOWLEDGE_TEXT, llmDescriptionText = LIST_KNOWLEDGE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_ITEMS))
    public void listKnowledge(CommandContext context) throws Exception {
        Book book = new Book(context);
        KnowledgeState state = service.load(book.manuscript);
        answer(context, itemsRow(character(state, context.get("character", String.class))));
    }

    @Command(commandName = ADD_KNOWLEDGE, linkedCommand = LIST_KNOWLEDGE, humanNameText = "Add knowledge",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = ADD_KNOWLEDGE_TEXT, llmDescriptionText = ADD_KNOWLEDGE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER),
                    @CommandParameter(name = "text", type = String.class, descriptionText = PARAM_TEXT)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_ITEMS))
    public void addKnowledge(CommandContext context) throws Exception {
        runAddKnowledge(context);
    }

    @Command(commandName = ADD_KNOWLEDGE, linkedCommand = LIST_KNOWLEDGE, humanNameText = "Add knowledge",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = ADD_KNOWLEDGE_TEXT, llmDescriptionText = ADD_KNOWLEDGE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER),
                    @CommandParameter(name = "text", type = String.class, descriptionText = PARAM_TEXT),
                    @CommandParameter(name = "kind", type = String.class, optional = true, descriptionText = PARAM_KIND)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_ITEMS))
    public void addKnowledgeKind(CommandContext context) throws Exception {
        runAddKnowledge(context);
    }

    private void runAddKnowledge(CommandContext context) throws Exception {
        Book book = new Book(context);
        KnowledgeState state = service.load(book.manuscript);
        TrackedCharacter character = character(state, context.get("character", String.class));
        String text = StringUtils.strip(context.get("text", String.class));
        if (StringUtils.isEmpty(text)) {
            throw new IllegalArgumentException("Give the text of the item");
        }
        character.getItems().add(KnowledgeItem.manual(text, kind(context.get("kind", String.class))));
        store(book, state);
        answer(context, itemsRow(character));
    }

    @Command(commandName = RETRACT_KNOWLEDGE, linkedCommand = LIST_KNOWLEDGE, humanNameText = "Retract knowledge",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = RETRACT_KNOWLEDGE_TEXT, llmDescriptionText = RETRACT_KNOWLEDGE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER),
                    @CommandParameter(name = "item", type = Integer.class, descriptionText = PARAM_ITEM)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_ITEMS))
    public void retractKnowledge(CommandContext context) throws Exception {
        Book book = new Book(context);
        KnowledgeState state = service.load(book.manuscript);
        TrackedCharacter character = character(state, context.get("character", String.class));
        item(character, context.get("item", Integer.class)).retractByHand();
        store(book, state);
        answer(context, itemsRow(character));
    }

    @Command(commandName = RESTORE_KNOWLEDGE, linkedCommand = LIST_KNOWLEDGE, humanNameText = "Restore knowledge",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = RESTORE_KNOWLEDGE_TEXT, llmDescriptionText = RESTORE_KNOWLEDGE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER),
                    @CommandParameter(name = "item", type = Integer.class, descriptionText = PARAM_ITEM)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_ITEMS))
    public void restoreKnowledge(CommandContext context) throws Exception {
        Book book = new Book(context);
        KnowledgeState state = service.load(book.manuscript);
        TrackedCharacter character = character(state, context.get("character", String.class));
        item(character, context.get("item", Integer.class)).restore();
        store(book, state);
        answer(context, itemsRow(character));
    }

    @Command(commandName = DELETE_KNOWLEDGE, linkedCommand = LIST_KNOWLEDGE, humanNameText = "Delete knowledge",
            dataSeverity = CommandDataSeverity.DESTRUCTIVE, descriptionText = DELETE_KNOWLEDGE_TEXT, llmDescriptionText = DELETE_KNOWLEDGE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER),
                    @CommandParameter(name = "item", type = Integer.class, descriptionText = PARAM_ITEM)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_ITEMS))
    public void deleteKnowledge(CommandContext context) throws Exception {
        Book book = new Book(context);
        KnowledgeState state = service.load(book.manuscript);
        TrackedCharacter character = character(state, context.get("character", String.class));
        KnowledgeItem item = item(character, context.get("item", Integer.class));
        character.deleteItem(item.getId());
        store(book, state);
        answer(context, itemsRow(character));
    }

    // --- processing ---

    @Command(commandName = PROCESS, linkedCommand = STATUS, humanNameText = "Process the story", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = PROCESS_TEXT, llmDescriptionText = PROCESS_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: how many parts were read, or the changes found"))
    public void process(CommandContext context) throws Exception {
        runProcess(context);
    }

    @Command(commandName = PROCESS, linkedCommand = STATUS, humanNameText = "Process the story", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = PROCESS_TEXT, llmDescriptionText = PROCESS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "parts", type = Integer.class, optional = true, descriptionText = PARAM_PARTS)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: how many parts were read, or the changes found"))
    public void processParts(CommandContext context) throws Exception {
        runProcess(context);
    }

    @Command(commandName = PROCESS, linkedCommand = STATUS, humanNameText = "Process the story", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = PROCESS_TEXT, llmDescriptionText = PROCESS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "parts", type = Integer.class, optional = true, descriptionText = PARAM_PARTS),
                    @CommandParameter(name = "ask", type = Boolean.class, optional = true, descriptionText = PARAM_ASK)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: how many parts were read, or the changes found"))
    public void processAsk(CommandContext context) throws Exception {
        runProcess(context);
    }

    private void runProcess(CommandContext context) throws Exception {
        Book book = new Book(context);
        Integer parts = context.get("parts", Integer.class);
        Boolean ask = context.get("ask", Boolean.class);
        if (parts != null && parts < 1) {
            throw new IllegalArgumentException("parts is at least 1");
        }
        KnowledgeState state = service.load(book.manuscript);
        if (!state.getSettings().isExtractWithModel()) {
            // no model: only who has met whom
            Report report = processService.process(book.manuscript, true, null, parts == null ? KnowledgeProcessService.NO_LIMIT : parts);
            answer(context, reportRow(report));
            return;
        }
        Proposal proposal = extraction.propose(book.manuscript, parts != null ? parts : state.getSettings().getPartsPerRun(), true, null);
        if (proposal.isEmpty()) {
            answer(context, reportRow(new Report(0, 0, 0)));
            return;
        }
        if (Boolean.TRUE.equals(ask) && proposal.asked() && !proposal.changes().isEmpty()) {
            runs.hold(book.manuscript, proposal);
            // the open tab shows the same changes with a button to review them
            service.changed(book.manuscript);
            answer(context, proposalRow(proposal));
            return;
        }
        answer(context, commitRow(proposal, extraction.commit(book.manuscript, proposal)));
    }

    @Command(commandName = TIDY, linkedCommand = STATUS, humanNameText = "Tidy the knowledge", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = TIDY_TEXT, llmDescriptionText = TIDY_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what was dropped and merged"))
    public void tidy(CommandContext context) throws Exception {
        runTidy(context);
    }

    @Command(commandName = TIDY, linkedCommand = STATUS, humanNameText = "Tidy the knowledge", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = TIDY_TEXT, llmDescriptionText = TIDY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "ask", type = Boolean.class, optional = true, descriptionText = PARAM_ASK)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what was dropped and merged, or the changes found"))
    public void tidyAsk(CommandContext context) throws Exception {
        runTidy(context);
    }

    private void runTidy(CommandContext context) throws Exception {
        Book book = new Book(context);
        KnowledgeState state = service.load(book.manuscript);
        if (!state.getSettings().isExtractWithModel()) {
            throw new IllegalArgumentException("The tidy pass needs the model: turn on reading what the characters learn with the model");
        }
        Proposal proposal = extraction.proposeTidy(book.manuscript);
        if (proposal.changes().isEmpty()) {
            JsonObject row = new JsonObject();
            row.addProperty("tidied", 0);
            row.addProperty("note", proposal.asked() ? "nothing to drop or merge" : "nothing to tidy: no character has enough items that the model learned");
            answer(context, row);
            return;
        }
        if (Boolean.TRUE.equals(context.get("ask", Boolean.class))) {
            runs.hold(book.manuscript, proposal);
            service.changed(book.manuscript);
            answer(context, proposalRow(proposal));
            return;
        }
        ChangeApplier.Result result = extraction.commitTidy(book.manuscript, proposal, true);
        JsonObject row = new JsonObject();
        row.addProperty("becameOld", result.retracted());
        row.addProperty("merged", result.added());
        answer(context, row);
    }

    @Command(commandName = LOCK_KNOWLEDGE, linkedCommand = LIST_KNOWLEDGE, humanNameText = "Lock knowledge",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = LOCK_TEXT, llmDescriptionText = LOCK_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER),
                    @CommandParameter(name = "item", type = Integer.class, descriptionText = PARAM_ITEM)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_ITEMS))
    public void lockKnowledge(CommandContext context) throws Exception {
        setLocked(context, true);
    }

    @Command(commandName = UNLOCK_KNOWLEDGE, linkedCommand = LIST_KNOWLEDGE, humanNameText = "Unlock knowledge",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = UNLOCK_TEXT, llmDescriptionText = UNLOCK_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER),
                    @CommandParameter(name = "item", type = Integer.class, descriptionText = PARAM_ITEM)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_ITEMS))
    public void unlockKnowledge(CommandContext context) throws Exception {
        setLocked(context, false);
    }

    private void setLocked(CommandContext context, boolean locked) throws Exception {
        Book book = new Book(context);
        KnowledgeState state = service.load(book.manuscript);
        TrackedCharacter character = character(state, context.get("character", String.class));
        item(character, context.get("item", Integer.class)).setLocked(locked);
        store(book, state);
        answer(context, itemsRow(character));
    }

    @Command(commandName = APPLY_CHANGES, linkedCommand = PROCESS, humanNameText = "Apply the changes", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = APPLY_TEXT, llmDescriptionText = APPLY_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what was applied"))
    public void applyAll(CommandContext context) throws Exception {
        runApply(context);
    }

    @Command(commandName = APPLY_CHANGES, linkedCommand = PROCESS, humanNameText = "Apply the changes", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = APPLY_TEXT, llmDescriptionText = APPLY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "changes", type = String.class, optional = true, descriptionText = PARAM_CHANGE_NUMBERS)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what was applied"))
    public void applyChosen(CommandContext context) throws Exception {
        runApply(context);
    }

    private void runApply(CommandContext context) throws Exception {
        Book book = new Book(context);
        Proposal proposal = runs.takeHeld(book.manuscript);
        if (proposal == null) {
            throw new IllegalArgumentException("No changes are waiting: call knowledge-process with ask true first");
        }
        String chosen = context.get("changes", String.class);
        if (StringUtils.isNotBlank(chosen)) {
            java.util.Set<Integer> numbers = new java.util.HashSet<>();
            for (String piece : chosen.split("[,;\\s]+")) {
                if (!piece.isBlank()) {
                    numbers.add(Integer.parseInt(piece.strip()));
                }
            }
            List<Change> changes = proposal.changes().changes();
            for (int i = 0; i < changes.size(); i++) {
                changes.get(i).setApply(numbers.contains(i + 1));
            }
        }
        try {
            answer(context, proposal.tidy() ? tidyRow(extraction.commitTidy(book.manuscript, proposal, false))
                    : commitRow(proposal, extraction.commit(book.manuscript, proposal)));
        } catch (StaleProposalException e) {
            throw new IllegalArgumentException(e.getMessage() + "; call knowledge-process again");
        }
    }

    @Command(commandName = REPROCESS, linkedCommand = STATUS, humanNameText = "Reprocess from a part", async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = REPROCESS_TEXT, llmDescriptionText = REPROCESS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, descriptionText = PARAM_PART)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what was taken back and how many parts were read"))
    public void reprocess(CommandContext context) throws Exception {
        Book book = new Book(context);
        Book.Located located = book.locate(context.get("part", String.class), null);
        if (!context.confirm(T.localized(REPROCESS_FROM_PART).formatted(located.position()))) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was reprocessed");
            }
            return;
        }
        if (service.load(book.manuscript).getSettings().isExtractWithModel()) {
            // the model reads them again: take back what came from them and let knowledge-process do it
            int dropped = processService.dropFrom(book.manuscript, located.message().getUuid());
            if (dropped < 0) {
                throw new IllegalArgumentException("That part is not on the active branch");
            }
            JsonObject row = new JsonObject();
            row.addProperty("takenBack", Math.max(0, dropped));
            row.addProperty("hint", "knowledge-process reads the parts again with the model");
            answer(context, row);
            return;
        }
        Report report = processService.reprocessFrom(book.manuscript, located.message().getUuid(), true);
        if (report == null) {
            throw new IllegalArgumentException("That part is not on the active branch");
        }
        answer(context, reportRow(report));
    }

    @Command(commandName = SET_START, linkedCommand = STATUS, humanNameText = "Set the start point", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = SET_START_TEXT, llmDescriptionText = SET_START_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "What the start point is now"))
    public void setStartOfTheBook(CommandContext context) throws Exception {
        Book book = new Book(context);
        processService.setStart(book.manuscript, null);
        context.setReturn("Processing starts at the start of the book");
    }

    @Command(commandName = SET_START, linkedCommand = STATUS, humanNameText = "Set the start point", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = SET_START_TEXT, llmDescriptionText = SET_START_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, descriptionText = PARAM_PART)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "What the start point is now"))
    public void setStart(CommandContext context) throws Exception {
        Book book = new Book(context);
        Book.Located located = book.locate(context.get("part", String.class), null);
        processService.setStart(book.manuscript, located.message().getUuid());
        context.setReturn("Processing starts at part " + located.position());
    }

    // --- helpers ---

    private void store(Book book, KnowledgeState state) throws Exception {
        service.save(book.manuscript, state);
        service.changed(book.manuscript);
    }

    private static JsonObject proposalRow(Proposal proposal) {
        JsonObject row = new JsonObject();
        row.addProperty("partsRead", 0);
        row.addProperty("partsInThisRun", proposal.parts().size());
        row.addProperty("stillUnread", proposal.pending());
        if (!proposal.note().isBlank()) {
            row.addProperty("note", proposal.note());
        }
        JsonArray list = new JsonArray();
        int number = 1;
        for (Change change : proposal.changes().changes()) {
            JsonObject c = new JsonObject();
            c.addProperty("number", number++);
            c.addProperty("character", change.getCharacterName());
            c.addProperty("change", change.getType() == Change.Type.RETRACT ? "retract" : "add");
            if (change.getType() == Change.Type.RETRACT) {
                c.addProperty("item", change.getItemText());
            } else {
                c.addProperty("kind", change.getKind() == ItemKind.KNOWS ? "knows" : "does-not-know");
                c.addProperty("text", change.getText());
            }
            c.addProperty("evidence", change.getEvidence());
            c.addProperty("part", change.getMessageNumber());
            c.addProperty("evidenceFound", change.isEvidenceFound());
            list.add(c);
        }
        row.add("changes", list);
        row.addProperty("hint", "apply-knowledge-changes applies them (pass the numbers to apply only some)");
        return row;
    }

    private static JsonObject tidyRow(ChangeApplier.Result result) {
        JsonObject row = new JsonObject();
        row.addProperty("becameOld", result.retracted());
        row.addProperty("merged", result.added());
        return row;
    }

    private static JsonObject commitRow(Proposal proposal, ChangeApplier.Result result) {
        JsonObject row = new JsonObject();
        row.addProperty("partsRead", proposal.parts().size());
        row.addProperty("added", result.added());
        row.addProperty("retracted", result.retracted());
        row.addProperty("rejected", result.rejected());
        row.addProperty("stillUnread", proposal.pending());
        if (!proposal.note().isBlank()) {
            row.addProperty("note", proposal.note());
        }
        return row;
    }

    private static JsonObject reportRow(Report report) {
        JsonObject row = new JsonObject();
        row.addProperty("partsRead", report.processed());
        row.addProperty("takenBack", report.rewound());
        row.addProperty("stillUnread", report.pending());
        return row;
    }

    private static JsonArray array(List<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    private static JsonObject characterRow(TrackedCharacter character, int number) {
        JsonObject row = new JsonObject();
        row.addProperty("number", number);
        row.addProperty("name", character.getName());
        row.add("aliases", array(character.getAliases()));
        row.addProperty("activeItems", character.activeItems().size());
        row.addProperty("oldItems", character.oldItems().size());
        row.addProperty("met", character.getMet().size());
        return row;
    }

    private static JsonObject itemsRow(TrackedCharacter character) {
        JsonObject result = new JsonObject();
        result.addProperty("character", character.getName());
        JsonArray items = new JsonArray();
        int number = 1;
        for (KnowledgeItem item : character.getItems()) {
            JsonObject row = new JsonObject();
            row.addProperty("number", number++);
            row.addProperty("kind", item.getKind() == ItemKind.KNOWS ? "knows" : "does-not-know");
            row.addProperty("text", item.getText());
            row.addProperty("state", item.isActive() ? "active" : "old");
            if (!item.isActive()) {
                row.addProperty("oldBecause", item.getOldReason().name().toLowerCase(Locale.ROOT));
            }
            row.addProperty("origin", item.getOrigin().name().toLowerCase(Locale.ROOT));
            row.addProperty("locked", item.isLocked());
            items.add(row);
        }
        result.add("items", items);
        result.add("met", array(character.getMet().stream().map(MetPerson::getName).toList()));
        return result;
    }

    private static TrackedCharacter character(KnowledgeState state, String reference) {
        if (StringUtils.isBlank(reference)) {
            throw new IllegalArgumentException("Give the name of the tracked character");
        }
        TrackedCharacter named = state.findByName(reference);
        if (named != null) {
            return named;
        }
        String wanted = reference.strip();
        if (wanted.matches("\\d{1,6}")) {
            int number = Integer.parseInt(wanted);
            if (number >= 1 && number <= state.getCharacters().size()) {
                return state.getCharacters().get(number - 1);
            }
        }
        throw new IllegalArgumentException(state.getCharacters().isEmpty() ? "No character is tracked yet"
                : "There is no tracked character " + wanted + ". Tracked: "
                + String.join(", ", state.getCharacters().stream().map(TrackedCharacter::getName).toList()));
    }

    private static KnowledgeItem item(TrackedCharacter character, Integer number) {
        if (number == null || number < 1 || number > character.getItems().size()) {
            throw new IllegalArgumentException("There is no item " + number + " of " + character.getName() + ", it has "
                    + character.getItems().size() + " (see list-knowledge)");
        }
        return character.getItems().get(number - 1);
    }

    private static String requiredName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Give the name of the character");
        }
        return name.strip();
    }

    /** Aliases are separated like the present characters field is. */
    private static List<String> aliases(String text) {
        return PresentCharacters.parse(text, null);
    }

    private static ItemKind kind(String text) {
        if (StringUtils.isBlank(text)) {
            return ItemKind.KNOWS;
        }
        String normalized = text.strip().toLowerCase(Locale.ROOT).replace('_', '-').replace(' ', '-');
        return switch (normalized) {
            case "knows", "know" -> ItemKind.KNOWS;
            case "does-not-know", "not-know", "unknown" -> ItemKind.DOES_NOT_KNOW;
            default -> throw new IllegalArgumentException("The kind is knows or does-not-know, not " + text);
        };
    }
}
