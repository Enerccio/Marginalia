package com.github.enerccio.marginalia.extensions.statetracker.commands;

import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read.ManuscriptReadSupport;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.extensions.statetracker.block.StateBlockBuilder;
import com.github.enerccio.marginalia.extensions.statetracker.extract.ClockChange;
import com.github.enerccio.marginalia.extensions.statetracker.extract.StateApplier;
import com.github.enerccio.marginalia.extensions.statetracker.extract.StateChange;
import com.github.enerccio.marginalia.extensions.statetracker.model.*;
import com.github.enerccio.marginalia.extensions.statetracker.service.StaleProposalException;
import com.github.enerccio.marginalia.extensions.statetracker.service.StateExtractionService;
import com.github.enerccio.marginalia.extensions.statetracker.service.StateExtractionService.Proposal;
import com.github.enerccio.marginalia.extensions.statetracker.service.StateProcessService;
import com.github.enerccio.marginalia.extensions.statetracker.service.StateService;
import com.github.enerccio.marginalia.extensions.storytracker.run.TrackerRuns;
import com.github.enerccio.marginalia.extensions.storytracker.scene.PresentCharacters;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.github.enerccio.marginalia.extensions.statetracker.StateTrackerLocalization.*;

/**
 * The State Tracker as commands, for the command bar, for agents and for templates: {@code state-status},
 * {@code list-state-characters}, {@code track-state-character}, {@code stop-tracking-state-character},
 * {@code list-character-state}, {@code set-character-state}, {@code remove-character-state}, {@code state-process},
 * {@code apply-state-changes}, {@code state-tidy}, {@code set-story-time}, {@code state-reprocess}, {@code state-set-start} and {@code state-tracker-block}, the text of the
 * prompt, for the {@code {{command::state-tracker-block::<characters>}}} macro.
 * <p>
 * A tracked character is addressed by its <b>name</b> or an alias (the case does not matter) or its <b>number</b> in
 * {@code list-state-characters}; a category by its name; a value by its number in {@code list-character-state}. No uuid is given
 * out or accepted.
 */
public class StateTrackerCommands extends ManuscriptReadSupport {

    public static final String STATUS = "state-status";
    public static final String LIST_CHARACTERS = "list-state-characters";
    public static final String TRACK_CHARACTER = "track-state-character";
    public static final String STOP_TRACKING = "stop-tracking-state-character";
    public static final String LIST_STATE = "list-character-state";
    public static final String SET_STATE = "set-character-state";
    public static final String REMOVE_STATE = "remove-character-state";
    public static final String BLOCK = "state-tracker-block";
    public static final String PROCESS = "state-process";
    public static final String APPLY_CHANGES = "apply-state-changes";
    public static final String REPROCESS = "state-reprocess";
    public static final String SET_START = "state-set-start";
    public static final String TIDY = "state-tidy";
    public static final String SET_TIME = "set-story-time";

    private static final String STATUS_TEXT = "The settings of the State Tracker, the categories of the book and the tracked characters. Reads only.";
    private static final String LIST_CHARACTERS_TEXT = "Lists the characters whose state the book tracks. Reads only.";
    private static final String LIST_CHARACTERS_LLM = "Lists the characters whose state (clothes, inventory, weapon, injuries...) the book tracks, in order, as JSON: "
            + "number, name, aliases and the categories tracked for each. Characters are addressed by name or alias in the other commands. Reads only.";
    private static final String TRACK_TEXT = "Starts tracking the state of a character.";
    private static final String TRACK_LLM = "Starts tracking the state of a character and returns it. Aliases are other names the character is written as in "
            + "the present characters field, separated by commas. Categories are the names of the categories to track, separated by commas: the book's "
            + "own, or the user's (copied into the book); see state-status for the names. Set the values with set-character-state.";
    private static final String STOP_TEXT = "Stops tracking a character and deletes its whole state.";
    private static final String STOP_LLM = "Removes a tracked character with all its values, current and past. Destructive; asks the user.";
    private static final String LIST_STATE_TEXT = "Lists the state of a tracked character: the current values of each category and their history. Reads only.";
    private static final String LIST_STATE_LLM = "Lists the values of a tracked character as JSON: for each tracked category its kind and its values, numbered, "
            + "each current (sent to the model) or past (replaced or removed, kept). Values are addressed by their number in remove-character-state. Reads only.";
    private static final String SET_TEXT = "Sets a value of a category of a tracked character.";
    private static final String SET_LLM = "Sets the state of a tracked character in a category: for a single value or a description category the text replaces "
            + "the current value (which is kept in the history); for a list category it is one more entry. The category is tracked for the character from then "
            + "on if it was not. Use it when the story changes the state (she draws the sword, he loses the map). Returns the character's state.";
    private static final String REMOVE_TEXT = "Removes a value: kept in the history, not sent to the model.";
    private static final String REMOVE_LLM = "Removes a current value (the entry of a list that was given away, the weapon that was dropped). It is kept in the "
            + "history. Returns the character's state.";
    private static final String BLOCK_TEXT = "The current state of the tracked characters of a scene, as plain text, the way the prompt gets it. Made for the "
            + "{{command::state-tracker-block::<characters>}} macro of a template: the argument is the list of the characters of the scene, for example "
            + "{{command::state-tracker-block::{{presentCharacters}}}}. Only tracked characters on the list are described. Reads only.";

    private static final String PROCESS_TEXT = "Asks the model what changed in the state of the tracked characters in the parts that are not processed yet, and applies it.";
    private static final String PROCESS_LLM = "Reads the parts after the cursor of the active branch, from the start point on, with the model (the Parts per run of the "
            + "settings, or parts) and applies the changes it finds to the state of the tracked characters. Pass ask true to get the changes as a numbered "
            + "list instead; apply-state-changes then applies the ones you choose. The last part counts as settled, so call it when the part is final. "
            + "Returns how many parts were read and how many are left.";
    private static final String APPLY_TEXT = "Applies the changes that state-process returned for review.";
    private static final String APPLY_LLM = "Applies the changes that state-process (ask true) held, all of them or the numbers you give separated by commas, and "
            + "moves the cursor over the parts they came from (a held state-tidy moves no cursor). Fails when the story changed since, then call "
            + "state-process again.";
    private static final String TIDY_TEXT = "Asks the model which current values faded, moved on or are listed twice; locked values stay.";
    private static final String TIDY_LLM = "The tidy: the model reads the current values of the tracked characters that are present in the last processed parts "
            + "(the Tidy after this many parts of the settings) with those parts, and drops what is no longer true (a mood, wet clothes, a cup long after the "
            + "meal), updates what moved on and merges list entries that are the same thing. Locked values are never touched. What it ends goes to the "
            + "history as tidied. Pass ask true to get the changes as a numbered list instead; apply-state-changes then applies the ones you choose.";
    private static final String SET_TIME_TEXT = "Sets the story time after a processed part by hand (a fixed point).";
    private static final String SET_TIME_LLM = "Sets the story clock after a processed part (its handle, or its position, the first part is 1) to a fixed point: the "
            + "story day, the time of day (dawn, morning, noon, afternoon, evening, night) and the date (YYYY-MM-DD), each optional; what is left out "
            + "is the time the clock has there. A reading of the part does not replace a fixed point and the clock counts on from it. With none of "
            + "day, timeOfDay and date the fixed point is removed. Needs the story clock on (state-status shows it). Returns the story time.";
    private static final String REPROCESS_TEXT = "Takes back what was read from a part and the later parts, so that state-process reads them again.";
    private static final String REPROCESS_LLM = "Reprocess from a part of the active branch (its handle, or its position, the first part is 1): the values read from it "
            + "and from the later parts are taken back (values written or corrected by hand stay), and the cursor goes back to just before it; "
            + "state-process reads them again. Use it after the author edited or rewrote an earlier part. Asks the user first.";
    private static final String SET_START_TEXT = "Sets the part where processing begins.";
    private static final String SET_START_LLM = "Sets the start point of the processing: the part (its handle, or its position, the first part is 1) the plugin begins at; "
            + "the parts before it are never processed. Without a part it is the start of the book.";
    private static final String PARAM_PARTS = "How many parts to read at most; the Parts per run of the settings when it is left out";
    private static final String PARAM_ASK = "true: return the changes as a numbered list without applying them";
    private static final String PARAM_CHANGE_NUMBERS = "The numbers of the changes to apply, separated by commas; all of them when left out";
    private static final String PARAM_PART = "The part: its handle, or its position on the active branch (the first part is 1)";
    private static final String PARAM_CHARACTER = "Name or alias of the tracked character (the case does not matter), or its number in list-state-characters";
    private static final String PARAM_NAME = "The name";
    private static final String PARAM_ALIASES = "Other names of the character, separated by commas";
    private static final String PARAM_CATEGORIES = "Names of the categories to track, separated by commas";
    private static final String PARAM_CATEGORY = "The name of the category (see state-status)";
    private static final String PARAM_TEXT = "The value, short, in the words of the story";
    private static final String PARAM_ITEM = "Number of the value in list-character-state of the character, the first is 1";
    private static final String PARAM_CHARACTERS = "The characters of the scene as free text, for example the present characters field";
    private static final String RESULT_CHARACTER = "JSON: the tracked character as it is now";
    private static final String RESULT_STATE = "JSON: the state of the character as it is now";

    private final StateService service;
    private final StateProcessService processService;
    private final StateExtractionService extraction;
    private final TrackerRuns<Proposal> runs;

    public StateTrackerCommands(StateService service, StateProcessService processService, StateExtractionService extraction,
                                TrackerRuns<Proposal> runs) {
        this.service = service;
        this.processService = processService;
        this.extraction = extraction;
        this.runs = runs;
    }

    // --- following the story with the model ---

    @Command(commandName = PROCESS, linkedCommand = STATUS, humanNameText = "Read the story for the state", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = PROCESS_TEXT, llmDescriptionText = PROCESS_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: how many parts were read, or the changes found"))
    public void process(CommandContext context) throws Exception {
        runProcess(context);
    }

    @Command(commandName = PROCESS, linkedCommand = STATUS, humanNameText = "Read the story for the state", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = PROCESS_TEXT, llmDescriptionText = PROCESS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "parts", type = Integer.class, optional = true, min = "1", descriptionText = PARAM_PARTS)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: how many parts were read, or the changes found"))
    public void processParts(CommandContext context) throws Exception {
        runProcess(context);
    }

    @Command(commandName = PROCESS, linkedCommand = STATUS, humanNameText = "Read the story for the state", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = PROCESS_TEXT, llmDescriptionText = PROCESS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "parts", type = Integer.class, optional = true, min = "1", descriptionText = PARAM_PARTS),
                    @CommandParameter(name = "ask", type = Boolean.class, optional = true, descriptionText = PARAM_ASK)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: how many parts were read, or the changes found"))
    public void processAsk(CommandContext context) throws Exception {
        runProcess(context);
    }

    private void runProcess(CommandContext context) throws Exception {
        Book book = new Book(context);
        Integer parts = context.get("parts", Integer.class);
        if (parts != null && parts < 1) {
            throw new IllegalArgumentException("parts is at least 1");
        }
        StateBook state = service.load(book.manuscript);
        Proposal proposal = extraction.propose(book.manuscript, parts != null ? parts : state.getSettings().getPartsPerRun(), true, null, null);
        if (proposal.isEmpty()) {
            JsonObject row = new JsonObject();
            row.addProperty("partsRead", 0);
            row.addProperty("stillUnread", 0);
            answer(context, row);
            return;
        }
        if (Boolean.TRUE.equals(context.get("ask", Boolean.class)) && proposal.asked() && !proposal.changes().isEmpty()) {
            runs.hold(book.manuscript, proposal);
            // the open tab shows the same changes with a button to review them
            service.changed(book.manuscript);
            answer(context, proposalRow(proposal));
            return;
        }
        answer(context, commitRow(proposal, extraction.commit(book.manuscript, proposal, true)));
    }

    @Command(commandName = APPLY_CHANGES, linkedCommand = PROCESS, humanNameText = "Apply the state changes", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = APPLY_TEXT, llmDescriptionText = APPLY_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what was applied"))
    public void applyAll(CommandContext context) throws Exception {
        runApply(context);
    }

    @Command(commandName = APPLY_CHANGES, linkedCommand = PROCESS, humanNameText = "Apply the state changes", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = APPLY_TEXT, llmDescriptionText = APPLY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "changes", type = String.class, optional = true, descriptionText = PARAM_CHANGE_NUMBERS)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what was applied"))
    public void applyChosen(CommandContext context) throws Exception {
        runApply(context);
    }

    @Command(commandName = TIDY, linkedCommand = STATUS, humanNameText = "Tidy the state", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = TIDY_TEXT, llmDescriptionText = TIDY_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what the tidy changed"))
    public void tidy(CommandContext context) throws Exception {
        runTidy(context);
    }

    @Command(commandName = TIDY, linkedCommand = STATUS, humanNameText = "Tidy the state", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = TIDY_TEXT, llmDescriptionText = TIDY_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "ask", type = Boolean.class, optional = true, descriptionText = PARAM_ASK)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what the tidy changed, or the changes it found"))
    public void tidyAsk(CommandContext context) throws Exception {
        runTidy(context);
    }

    private void runTidy(CommandContext context) throws Exception {
        Book book = new Book(context);
        Proposal proposal = extraction.proposeTidy(book.manuscript, null);
        if (proposal.changes().isEmpty()) {
            extraction.markTidied(book.manuscript);
            JsonObject row = new JsonObject();
            row.addProperty("changed", 0);
            row.addProperty("note", proposal.asked() ? "nothing to drop, update or merge"
                    : "nothing to tidy: no tracked character with a value that is not locked is in the last processed parts");
            answer(context, row);
            return;
        }
        if (Boolean.TRUE.equals(context.get("ask", Boolean.class))) {
            runs.hold(book.manuscript, proposal);
            service.changed(book.manuscript);
            answer(context, proposalRow(proposal));
            return;
        }
        answer(context, tidyRow(extraction.commitTidy(book.manuscript, proposal, true)));
    }

    @Command(commandName = SET_TIME, linkedCommand = STATUS, humanNameText = "Set the story time",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = SET_TIME_TEXT, llmDescriptionText = SET_TIME_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, descriptionText = PARAM_PART),
                    @CommandParameter(name = "day", type = Integer.class, optional = true, min = "1", descriptionText = "The story day"),
                    @CommandParameter(name = "timeOfDay", type = String.class, optional = true, descriptionText = "dawn, morning, noon, afternoon, evening or night"),
                    @CommandParameter(name = "date", type = String.class, optional = true, descriptionText = "The date, YYYY-MM-DD")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the story time after the part and now"))
    public void setStoryTime(CommandContext context) throws Exception {
        Book book = new Book(context);
        StateBook state = service.load(book.manuscript);
        StoryClock clock = state.getClock();
        if (!clock.isEnabled()) {
            throw new IllegalArgumentException("The story clock is off for this book");
        }
        Book.Located located = book.locate(context.get("part", String.class), null);
        String part = located.message().getUuid();
        if (state.getProcessed().stream().noneMatch(p -> p.getUuid().equals(part))) {
            throw new IllegalArgumentException("Part " + located.position() + " is not processed yet: the clock has no time there");
        }
        Integer day = context.get("day", Integer.class);
        String timeOfDay = context.get("timeOfDay", String.class);
        String date = context.get("date", String.class);
        if (day == null && StringUtils.isBlank(timeOfDay) && StringUtils.isBlank(date)) {
            ClockMark mark = clock.getMarks().get(part);
            if (mark != null && mark.isManual()) {
                clock.fix(part, null);
            }
        } else {
            StoryTime current = clock.upTo(state, part);
            TimeOfDay to = StringUtils.isBlank(timeOfDay) ? current.timeOfDay() : TimeOfDay.parse(timeOfDay);
            if (StringUtils.isNotBlank(timeOfDay) && to == null) {
                throw new IllegalArgumentException("Unknown time of day: " + timeOfDay);
            }
            if (StringUtils.isNotBlank(date) && StoryClock.parse(date) == null) {
                throw new IllegalArgumentException("Not a date (YYYY-MM-DD): " + date);
            }
            clock.fix(part, new ClockMark(part, ClockMark.Kind.SET).day(day == null ? current.day() : day).timeOfDay(to)
                    .date(StringUtils.isNotBlank(date) ? StoryClock.parse(date).toString() : current.date() == null ? null : current.date().toString()));
        }
        store(book, state);
        JsonObject row = new JsonObject();
        row.addProperty("afterPart", clock.upTo(state, part).words(clock.getAnchor()));
        row.addProperty("now", clock.now(state).words(clock.getAnchor()));
        answer(context, row);
    }

    private static JsonObject tidyRow(StateApplier.Result result) {
        JsonObject row = new JsonObject();
        row.addProperty("changed", result.applied());
        row.addProperty("rejected", result.rejected());
        row.addProperty("skipped", result.skipped());
        return row;
    }

    private void runApply(CommandContext context) throws Exception {
        Book book = new Book(context);
        Proposal proposal = runs.takeHeld(book.manuscript);
        if (proposal == null) {
            throw new IllegalArgumentException("No changes are waiting: call " + PROCESS + " with ask true first");
        }
        String chosen = context.get("changes", String.class);
        if (StringUtils.isNotBlank(chosen)) {
            Set<Integer> numbers = new HashSet<>();
            for (String piece : chosen.split("[,;\\s]+")) {
                if (!piece.isBlank()) {
                    numbers.add(Integer.parseInt(piece.strip()));
                }
            }
            List<StateChange> changes = proposal.changes();
            for (int i = 0; i < changes.size(); i++) {
                changes.get(i).setApply(numbers.contains(i + 1));
            }
            // the clock entries continue the numbers of the changes
            List<ClockChange> clock = proposal.clock();
            for (int i = 0; i < clock.size(); i++) {
                clock.get(i).setApply(numbers.contains(changes.size() + i + 1));
            }
        }
        try {
            answer(context, proposal.tidy() ? tidyRow(extraction.commitTidy(book.manuscript, proposal, false))
                    : commitRow(proposal, extraction.commit(book.manuscript, proposal, false)));
        } catch (StaleProposalException e) {
            throw new IllegalArgumentException(e.getMessage() + "; call " + PROCESS + " again");
        }
    }

    @Command(commandName = REPROCESS, linkedCommand = STATUS, humanNameText = "Reprocess the state from a part", async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE, descriptionText = REPROCESS_TEXT, llmDescriptionText = REPROCESS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "part", type = String.class, descriptionText = PARAM_PART)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: what was taken back"))
    public void reprocess(CommandContext context) throws Exception {
        Book book = new Book(context);
        Book.Located located = book.locate(context.get("part", String.class), null);
        if (!context.confirm(T.localized(REPROCESS_FROM_PART_N).formatted(located.position()))) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was reprocessed");
            }
            return;
        }
        int dropped = processService.dropFrom(book.manuscript, located.message().getUuid());
        if (dropped < 0) {
            throw new IllegalArgumentException("That part is not on the active branch");
        }
        JsonObject row = new JsonObject();
        row.addProperty("takenBack", dropped);
        row.addProperty("hint", PROCESS + " reads the parts again with the model");
        answer(context, row);
    }

    @Command(commandName = SET_START, linkedCommand = STATUS, humanNameText = "Set the start point of the state", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = SET_START_TEXT, llmDescriptionText = SET_START_LLM, signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "What the start point is now"))
    public void setStartOfTheBook(CommandContext context) throws Exception {
        Book book = new Book(context);
        processService.setStart(book.manuscript, null);
        context.setReturn("Processing starts at the start of the book");
    }

    @Command(commandName = SET_START, linkedCommand = STATUS, humanNameText = "Set the start point of the state", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
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

    private static JsonObject proposalRow(Proposal proposal) {
        JsonObject row = new JsonObject();
        row.addProperty("partsInThisRun", proposal.parts().size());
        row.addProperty("stillUnread", proposal.pending());
        if (!proposal.note().isBlank()) {
            row.addProperty("note", proposal.note());
        }
        JsonArray list = new JsonArray();
        int number = 1;
        for (StateChange change : proposal.changes()) {
            JsonObject c = new JsonObject();
            c.addProperty("number", number++);
            c.addProperty("character", change.getCharacterName());
            c.addProperty("category", change.getCategoryName());
            c.addProperty("action", change.getAction().name().toLowerCase(Locale.ROOT));
            if (change.getItemText() != null) {
                c.addProperty("ends", change.getItemText());
            }
            if (change.getValue() != null) {
                c.addProperty("value", change.getValue());
            }
            if (change.isTidy()) {
                c.addProperty("reason", change.getEvidence());
            } else {
                c.addProperty("evidence", change.getEvidence());
                c.addProperty("part", change.getPartNumber());
                c.addProperty("evidenceFound", change.isEvidenceFound());
            }
            list.add(c);
        }
        row.add("changes", list);
        if (!proposal.clock().isEmpty()) {
            JsonArray clock = new JsonArray();
            for (ClockChange change : proposal.clock()) {
                JsonObject c = new JsonObject();
                c.addProperty("number", number++);
                ClockMark mark = change.getMark();
                c.addProperty("part", change.getPartNumber());
                c.addProperty("kind", mark.getKind().name().toLowerCase(Locale.ROOT));
                if (mark.getDays() != null) {
                    c.addProperty("days", mark.getDays());
                }
                if (mark.getDay() != null) {
                    c.addProperty("day", mark.getDay());
                }
                if (mark.getDate() != null) {
                    c.addProperty("date", mark.getDate());
                }
                if (mark.getTimeOfDay() != null) {
                    c.addProperty("timeOfDay", mark.getTimeOfDay().word());
                }
                if (mark.isApproximate()) {
                    c.addProperty("approximate", true);
                }
                c.addProperty("evidence", mark.getEvidence());
                clock.add(c);
            }
            row.add("clock", clock);
        }
        row.addProperty("hint", APPLY_CHANGES + " applies them (pass the numbers to apply only some)");
        return row;
    }

    private static JsonObject commitRow(Proposal proposal, StateApplier.Result result) {
        JsonObject row = new JsonObject();
        row.addProperty("partsRead", proposal.parts().size());
        row.addProperty("applied", result.applied());
        row.addProperty("rejected", result.rejected());
        row.addProperty("skipped", result.skipped());
        row.addProperty("stillUnread", proposal.pending());
        if (!proposal.note().isBlank()) {
            row.addProperty("note", proposal.note());
        }
        return row;
    }

    // --- the block, for templates ---

    @Command(commandName = BLOCK, humanNameText = "State of the characters", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            availableInMacros = true, descriptionText = BLOCK_TEXT,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "characters", type = String.class, descriptionText = PARAM_CHARACTERS)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "Text: the state of the tracked characters of the list; empty when none of them has one"))
    public void block(CommandContext context) throws Exception {
        Book book = new Book(context);
        context.setReturn(StateBlockBuilder.build(service.load(book.manuscript), context.get("characters", String.class)));
    }

    // --- status ---

    @Command(commandName = STATUS, humanNameText = "State status", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = STATUS_TEXT, signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the settings, the categories and the characters"))
    public void status(CommandContext context) throws Exception {
        Book book = new Book(context);
        StateBook state = service.load(book.manuscript);
        JsonObject result = new JsonObject();
        result.addProperty("inPrompt", state.getSettings().isInPrompt());
        result.addProperty("depth", state.getSettings().getDepth());
        result.addProperty("role", state.getSettings().getRole().name().toLowerCase(Locale.ROOT));
        result.add("separators", array(state.getSettings().getSeparators()));
        result.addProperty("autoTidy", state.getSettings().isAutoTidy());
        result.addProperty("tidyEveryParts", state.getSettings().getTidyEveryParts());
        result.addProperty("partsSinceTidy", state.getPartsSinceTidy());
        StoryClock clock = state.getClock();
        result.addProperty("storyClock", clock.isEnabled());
        if (clock.isEnabled()) {
            result.addProperty("storyTime", clock.now(state).words(clock.getAnchor()));
            if (clock.now(state).date() != null) {
                result.addProperty("storyDate", clock.now(state).date().toString());
            }
        }
        result.addProperty("trackedCharacters", state.getCharacters().size());
        JsonArray categories = new JsonArray();
        for (Category category : state.getCategories()) {
            JsonObject row = categoryRow(category);
            row.addProperty("onlyInThisBook", category.getSourceId() == null);
            row.addProperty("trackedBy", state.trackedBy(category.getId()));
            categories.add(row);
        }
        result.add("bookCategories", categories);
        JsonArray mine = new JsonArray();
        for (Category category : service.getUserCategories()) {
            if (state.copyOf(category.getId()) == null) {
                mine.add(categoryRow(category));
            }
        }
        result.add("userCategoriesNotInTheBook", mine);
        answer(context, result);
    }

    // --- tracked characters ---

    @Command(commandName = LIST_CHARACTERS, linkedCommand = LIST_STATE, humanNameText = "List characters with a state",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = LIST_CHARACTERS_TEXT, llmDescriptionText = LIST_CHARACTERS_LLM, signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "JSON: the tracked characters"))
    public void listCharacters(CommandContext context) throws Exception {
        Book book = new Book(context);
        StateBook state = service.load(book.manuscript);
        JsonArray list = new JsonArray();
        for (int i = 0; i < state.getCharacters().size(); i++) {
            list.add(characterRow(state, state.getCharacters().get(i), i + 1));
        }
        JsonObject result = new JsonObject();
        result.add("characters", list);
        if (list.isEmpty()) {
            result.addProperty("hint", "No character is tracked yet, " + TRACK_CHARACTER + " adds one");
        }
        answer(context, result);
    }

    @Command(commandName = TRACK_CHARACTER, linkedCommand = LIST_CHARACTERS, humanNameText = "Track the state of a character",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = TRACK_TEXT, llmDescriptionText = TRACK_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, descriptionText = PARAM_NAME)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_CHARACTER))
    public void trackCharacter(CommandContext context) throws Exception {
        runTrack(context);
    }

    @Command(commandName = TRACK_CHARACTER, linkedCommand = LIST_CHARACTERS, humanNameText = "Track the state of a character",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = TRACK_TEXT, llmDescriptionText = TRACK_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, descriptionText = PARAM_NAME),
                    @CommandParameter(name = "aliases", type = String.class, optional = true, descriptionText = PARAM_ALIASES)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_CHARACTER))
    public void trackCharacterAliases(CommandContext context) throws Exception {
        runTrack(context);
    }

    @Command(commandName = TRACK_CHARACTER, linkedCommand = LIST_CHARACTERS, humanNameText = "Track the state of a character",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = TRACK_TEXT, llmDescriptionText = TRACK_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "name", type = String.class, descriptionText = PARAM_NAME),
                    @CommandParameter(name = "aliases", type = String.class, optional = true, descriptionText = PARAM_ALIASES),
                    @CommandParameter(name = "categories", type = String.class, optional = true, descriptionText = PARAM_CATEGORIES)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_CHARACTER))
    public void trackCharacterCategories(CommandContext context) throws Exception {
        runTrack(context);
    }

    private void runTrack(CommandContext context) throws Exception {
        Book book = new Book(context);
        StateBook state = service.load(book.manuscript);
        String name = context.get("name", String.class);
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Give the name of the character");
        }
        List<String> aliases = names(context.get("aliases", String.class));
        TrackedCharacter used = state.findClash(null, name.strip(), aliases);
        if (used != null) {
            throw new IllegalArgumentException("The name or an alias is already used by " + used.getName());
        }
        List<Category> user = service.getUserCategories();
        List<Category> categories = new ArrayList<>();
        for (String categoryName : names(context.get("categories", String.class))) {
            categories.add(category(state, user, categoryName));
        }
        TrackedCharacter character = new TrackedCharacter(name.strip());
        character.setAliases(aliases);
        categories.forEach(c -> character.track(c.getId()));
        state.getCharacters().add(character);
        store(book, state);
        answer(context, characterRow(state, character, state.getCharacters().size()));
    }

    @Command(commandName = STOP_TRACKING, linkedCommand = LIST_CHARACTERS, humanNameText = "Stop tracking the state of a character", async = true,
            dataSeverity = CommandDataSeverity.DESTRUCTIVE, descriptionText = STOP_TEXT, llmDescriptionText = STOP_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "What was deleted"))
    public void stopTracking(CommandContext context) throws Exception {
        Book book = new Book(context);
        StateBook state = service.load(book.manuscript);
        TrackedCharacter character = character(state, context.get("character", String.class));
        if (!context.confirm(T.localized(STOP_TRACKING_AND_DELETE_ITS_STATE).formatted(character.getName()))) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was deleted");
            }
            return;
        }
        state.deleteCharacter(character.getId());
        store(book, state);
        context.setReturn("Stopped tracking " + character.getName() + " (" + character.getItems().size() + " values deleted)");
    }

    // --- values ---

    @Command(commandName = LIST_STATE, linkedCommand = LIST_CHARACTERS, humanNameText = "List the state of a character",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = LIST_STATE_TEXT, llmDescriptionText = LIST_STATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_STATE))
    public void listState(CommandContext context) throws Exception {
        Book book = new Book(context);
        StateBook state = service.load(book.manuscript);
        answer(context, stateRow(state, character(state, context.get("character", String.class))));
    }

    @Command(commandName = SET_STATE, linkedCommand = LIST_STATE, humanNameText = "Set the state of a character",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = SET_TEXT, llmDescriptionText = SET_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER),
                    @CommandParameter(name = "category", type = String.class, descriptionText = PARAM_CATEGORY),
                    @CommandParameter(name = "text", type = String.class, descriptionText = PARAM_TEXT)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_STATE))
    public void setState(CommandContext context) throws Exception {
        Book book = new Book(context);
        StateBook state = service.load(book.manuscript);
        TrackedCharacter character = character(state, context.get("character", String.class));
        Category category = category(state, service.getUserCategories(), context.get("category", String.class));
        String text = context.get("text", String.class);
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("Give the value");
        }
        character.set(category, text.strip(), System.currentTimeMillis()).setStoryDay(state.today());
        store(book, state);
        answer(context, stateRow(state, character));
    }

    @Command(commandName = REMOVE_STATE, linkedCommand = LIST_STATE, humanNameText = "Remove a value of a character's state",
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = REMOVE_TEXT, llmDescriptionText = REMOVE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "character", type = String.class, descriptionText = PARAM_CHARACTER),
                    @CommandParameter(name = "item", type = Integer.class, descriptionText = PARAM_ITEM)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_STATE))
    public void removeState(CommandContext context) throws Exception {
        Book book = new Book(context);
        StateBook state = service.load(book.manuscript);
        TrackedCharacter character = character(state, context.get("character", String.class));
        StateItem item = numbered(state, character).get(itemIndex(state, character, context.get("item", Integer.class)));
        if (!item.isActive()) {
            throw new IllegalArgumentException("Value " + context.get("item", Integer.class) + " is in the history already");
        }
        character.remove(item, System.currentTimeMillis());
        store(book, state);
        answer(context, stateRow(state, character));
    }

    // --- helpers ---

    private void store(Book book, StateBook state) throws Exception {
        service.save(book.manuscript, state);
        service.changed(book.manuscript);
    }

    /**
     * The category of the book with that name, else the user's (copied into the book).
     */
    static Category category(StateBook state, List<Category> user, String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Give the name of the category");
        }
        Category own = state.findCategoryByName(name);
        if (own != null) {
            return own;
        }
        for (Category category : user) {
            if (category.getName().strip().equalsIgnoreCase(name.strip())) {
                return state.adopt(category);
            }
        }
        List<String> known = new ArrayList<>();
        state.getCategories().forEach(c -> known.add(c.getName()));
        user.stream().filter(c -> state.copyOf(c.getId()) == null).forEach(c -> known.add(c.getName()));
        throw new IllegalArgumentException("There is no category " + name.strip() + ". Categories: " + String.join(", ", known));
    }

    /** The values of the character in the order list-character-state numbers them: by tracked category, current before past. */
    static List<StateItem> numbered(StateBook state, TrackedCharacter character) {
        List<StateItem> items = new ArrayList<>();
        for (Category category : state.categoriesOf(character)) {
            items.addAll(character.active(category.getId()));
            items.addAll(character.history(category.getId()));
        }
        return items;
    }

    private static int itemIndex(StateBook state, TrackedCharacter character, Integer number) {
        int size = numbered(state, character).size();
        if (number == null || number < 1 || number > size) {
            throw new IllegalArgumentException("There is no value " + number + " of " + character.getName() + ", it has " + size
                    + " (see " + LIST_STATE + ")");
        }
        return number - 1;
    }

    private static JsonArray array(List<String> values) {
        JsonArray array = new JsonArray();
        values.forEach(array::add);
        return array;
    }

    private static JsonObject categoryRow(Category category) {
        JsonObject row = new JsonObject();
        row.addProperty("name", category.getName());
        row.addProperty("kind", category.getKind().name().toLowerCase(Locale.ROOT));
        row.addProperty("instruction", category.getInstruction());
        return row;
    }

    private static JsonObject characterRow(StateBook state, TrackedCharacter character, int number) {
        JsonObject row = new JsonObject();
        row.addProperty("number", number);
        row.addProperty("name", character.getName());
        row.add("aliases", array(character.getAliases()));
        row.add("categories", array(state.categoriesOf(character).stream().map(Category::getName).toList()));
        return row;
    }

    private static JsonObject stateRow(StateBook state, TrackedCharacter character) {
        JsonObject result = new JsonObject();
        result.addProperty("character", character.getName());
        JsonArray categories = new JsonArray();
        int number = 1;
        for (Category category : state.categoriesOf(character)) {
            JsonObject row = categoryRow(category);
            row.remove("instruction");
            JsonArray values = new JsonArray();
            List<StateItem> items = new ArrayList<>(character.active(category.getId()));
            items.addAll(character.history(category.getId()));
            for (StateItem item : items) {
                JsonObject value = new JsonObject();
                value.addProperty("number", number++);
                value.addProperty("text", item.getText());
                value.addProperty("state", item.isActive() ? "current" : "past");
                if (!item.isActive() && item.getEndReason() != null) {
                    value.addProperty("ended", item.getEndReason().name().toLowerCase(Locale.ROOT));
                }
                value.addProperty("locked", item.isLocked());
                values.add(value);
            }
            row.add("values", values);
            categories.add(row);
        }
        result.add("categories", categories);
        return result;
    }

    private static TrackedCharacter character(StateBook state, String reference) {
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

    /** Names and aliases are separated like the present characters field is. */
    private static List<String> names(String text) {
        return StringUtils.isBlank(text) ? List.of() : PresentCharacters.parse(text, null);
    }
}
