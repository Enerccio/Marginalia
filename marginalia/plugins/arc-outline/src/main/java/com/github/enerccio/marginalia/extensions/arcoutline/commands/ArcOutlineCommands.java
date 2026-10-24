package com.github.enerccio.marginalia.extensions.arcoutline.commands;

import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.extensions.arcoutline.model.*;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

/**
 * The outline of the book as commands, for the command bar and for agents: {@code get-arc-outline}, {@code add-arc},
 * {@code update-arc}, {@code move-arc}, {@code delete-arc} and the same for chapters ({@code add-outline-chapter},
 * {@code update-outline-chapter}, {@code move-outline-chapter}, {@code delete-outline-chapter}). They do what the Arcs
 * tab does, on the book of the scope of the command (the open book, or the working copy of an agent), and the open tab
 * shows the change at once. Arcs and chapters are addressed by their numbers, see {@link ArcOutlineCommandSupport}.
 * <p>
 * A command that changes a node takes only the fields it is given ({@code null} leaves a field as it is, an empty text
 * clears a description or a note). Reading is {@code NON_DESTRUCTIVE}, so is anything that adds or changes a node;
 * deleting is {@code DESTRUCTIVE} and asks the user first.
 */
public class ArcOutlineCommands extends ArcOutlineCommandSupport {

    public static final String GET = "get-arc-outline";
    public static final String ADD_ARC = "add-arc";
    public static final String UPDATE_ARC = "update-arc";
    public static final String MOVE_ARC = "move-arc";
    public static final String DELETE_ARC = "delete-arc";
    public static final String ADD_CHAPTER = "add-outline-chapter";
    public static final String UPDATE_CHAPTER = "update-outline-chapter";
    public static final String MOVE_CHAPTER = "move-outline-chapter";
    public static final String DELETE_CHAPTER = "delete-outline-chapter";

    private static final String GET_TEXT = "Returns the arc outline of the book: the arcs with their chapters, states and descriptions. Reads only.";
    private static final String GET_LLM = "Returns the arc outline of the book (the plan of the story: arcs, each split into chapters, with the state "
            + "of every one of them - PLANNED, CURRENT or FINISHED - and a terse description) as JSON. Arcs and chapters are addressed by their "
            + "numbers in the other commands, so call this first. The private notes are left out unless asked for (hasNote says that there is one), "
            + "they can be long: pass notes true for one arc at a time. Reads only.";
    private static final String GET_RETURN = "JSON: the arcs (number, title, state, description, chapters)";
    private static final String ADD_ARC_TEXT = "Adds an arc to the outline, at the end or at the given position.";
    private static final String ADD_ARC_LLM = "Adds an arc to the arc outline of the book and returns it with its number. The description is what the "
            + "outline shows and what the story model is told: terse, one to three sentences. Put everything longer into the note, which is the "
            + "author's private version. It goes at the end of the outline unless a position is given (the arcs from that position on move down). "
            + "Add its chapters with add-outline-chapter.";
    private static final String UPDATE_ARC_TEXT = "Changes the title, description, note or state of an arc.";
    private static final String UPDATE_ARC_LLM = "Changes an arc of the arc outline. Pass only the fields to change, the others stay as they are; "
            + "an empty description or note clears it. Marking an arc FINISHED marks all its chapters FINISHED too. Returns the arc.";
    private static final String MOVE_ARC_TEXT = "Moves an arc to another position in the outline.";
    private static final String MOVE_ARC_LLM = "Moves an arc to the position given (1 is the first), the other arcs make room. A position past the end "
            + "puts it last. Returns the new place.";
    private static final String DELETE_ARC_TEXT = "Deletes an arc with all its chapters from the outline.";
    private static final String DELETE_ARC_LLM = "Deletes an arc with all its chapters from the arc outline. Destructive, the arcs after it get "
            + "lower numbers.";
    private static final String ADD_CHAPTER_TEXT = "Adds a chapter to an arc of the outline, at the end of the arc or at the given position.";
    private static final String ADD_CHAPTER_LLM = "Adds a chapter to an arc of the arc outline and returns it with its number. The description is "
            + "terse, one or two sentences, the full version goes to the note. It goes at the end of the arc unless a position is given.";
    private static final String UPDATE_CHAPTER_TEXT = "Changes the title, description, note or state of a chapter of the outline.";
    private static final String UPDATE_CHAPTER_LLM = "Changes a chapter of the arc outline. Pass only the fields to change, the others stay as they "
            + "are; an empty description or note clears it. Returns the chapter.";
    private static final String MOVE_CHAPTER_TEXT = "Moves a chapter to another position in its arc, or to another arc.";
    private static final String MOVE_CHAPTER_LLM = "Moves a chapter of the arc outline to the position given (1 is the first) in its own arc, or in "
            + "another arc when to_arc is given. A position past the end puts it last. Returns the new place.";
    private static final String DELETE_CHAPTER_TEXT = "Deletes a chapter from the outline.";
    private static final String DELETE_CHAPTER_LLM = "Deletes a chapter from the arc outline. Destructive, the chapters after it get lower numbers.";
    private static final String RESULT_NODE = "JSON: the arc or the chapter as it is now, with its number";

    private static final String PARAM_NOTES = "true to include the private notes (default false)";
    private static final String PARAM_TITLE = "A short title";
    private static final String PARAM_DESCRIPTION = "What happens in it, terse. This is what the outline shows";
    private static final String PARAM_NOTE = "The full version for the author's notes";
    private static final String PARAM_POSITION = "The place it gets among the others, 1 for the first; the end when omitted";
    private static final String PARAM_TO_ARC = "Number of the arc the chapter moves to; its own arc when omitted";

    public ArcOutlineCommands(ArcOutlineService service) {
        super(service);
    }

    // --- get-arc-outline ---

    @Command(commandName = GET, humanNameText = "Get arc outline", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = GET_TEXT, llmDescriptionText = GET_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = GET_RETURN))
    public void get(CommandContext context) throws Exception {
        runGet(context);
    }

    @Command(commandName = GET, humanNameText = "Get arc outline", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = GET_TEXT, llmDescriptionText = GET_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, optional = true,
                            descriptionText = "Number of the arc to return alone; all the arcs when omitted")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = GET_RETURN))
    public void getArc(CommandContext context) throws Exception {
        runGet(context);
    }

    @Command(commandName = GET, humanNameText = "Get arc outline", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = GET_TEXT, llmDescriptionText = GET_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, optional = true,
                            descriptionText = "Number of the arc to return alone; all the arcs when omitted"),
                    @CommandParameter(name = "notes", type = Boolean.class, optional = true, defaultValue = "false",
                            descriptionText = PARAM_NOTES)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = GET_RETURN))
    public void getNotes(CommandContext context) throws Exception {
        runGet(context);
    }

    private void runGet(CommandContext context) throws Exception {
        Book book = new Book(context);
        ArcOutline outline = outline(book);
        boolean notes = Boolean.TRUE.equals(context.get("notes", Boolean.class));
        Integer only = context.get("arc", Integer.class);
        JsonObject result = new JsonObject();
        JsonArray arcs = new JsonArray();
        if (only != null) {
            arcs.add(arcRow(only, arc(outline, only), notes, true));
        } else {
            int number = 1;
            for (Arc arc : outline.arcs()) {
                arcs.add(arcRow(number++, arc, notes, true));
            }
        }
        result.add("arcs", arcs);
        if (outline.arcs().isEmpty()) {
            result.addProperty("hint", "The outline has no arcs yet, add-arc adds one");
        }
        answer(context, result);
    }

    // --- add-arc ---

    @Command(commandName = ADD_ARC, linkedCommand = GET, humanNameText = "Add arc", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = ADD_ARC_TEXT, llmDescriptionText = ADD_ARC_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "title", type = String.class, descriptionText = PARAM_TITLE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void addArc(CommandContext context) throws Exception {
        runAddArc(context);
    }

    @Command(commandName = ADD_ARC, linkedCommand = GET, humanNameText = "Add arc", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = ADD_ARC_TEXT, llmDescriptionText = ADD_ARC_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "title", type = String.class, descriptionText = PARAM_TITLE),
                    @CommandParameter(name = "description", type = String.class, optional = true, descriptionText = PARAM_DESCRIPTION)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void addArcDescription(CommandContext context) throws Exception {
        runAddArc(context);
    }

    @Command(commandName = ADD_ARC, linkedCommand = GET, humanNameText = "Add arc", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = ADD_ARC_TEXT, llmDescriptionText = ADD_ARC_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "title", type = String.class, descriptionText = PARAM_TITLE),
                    @CommandParameter(name = "description", type = String.class, optional = true, descriptionText = PARAM_DESCRIPTION),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = PARAM_NOTE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void addArcNote(CommandContext context) throws Exception {
        runAddArc(context);
    }

    @Command(commandName = ADD_ARC, linkedCommand = GET, humanNameText = "Add arc", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = ADD_ARC_TEXT, llmDescriptionText = ADD_ARC_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "title", type = String.class, descriptionText = PARAM_TITLE),
                    @CommandParameter(name = "description", type = String.class, optional = true, descriptionText = PARAM_DESCRIPTION),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = PARAM_NOTE),
                    @CommandParameter(name = "state", type = String.class, optional = true, defaultValue = "PLANNED",
                            allowed = {"PLANNED", "CURRENT", "FINISHED"}, descriptionText = PARAM_STATE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void addArcState(CommandContext context) throws Exception {
        runAddArc(context);
    }

    @Command(commandName = ADD_ARC, linkedCommand = GET, humanNameText = "Add arc", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = ADD_ARC_TEXT, llmDescriptionText = ADD_ARC_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "title", type = String.class, descriptionText = PARAM_TITLE),
                    @CommandParameter(name = "description", type = String.class, optional = true, descriptionText = PARAM_DESCRIPTION),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = PARAM_NOTE),
                    @CommandParameter(name = "state", type = String.class, optional = true, defaultValue = "PLANNED",
                            allowed = {"PLANNED", "CURRENT", "FINISHED"}, descriptionText = PARAM_STATE),
                    @CommandParameter(name = "position", type = Integer.class, optional = true, descriptionText = PARAM_POSITION)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void addArcPosition(CommandContext context) throws Exception {
        runAddArc(context);
    }

    private void runAddArc(CommandContext context) throws Exception {
        Book book = new Book(context);
        ArcOutline outline = outline(book);
        Arc arc = new Arc();
        arc.setTitle(requiredTitle(context.get("title", String.class)));
        arc.setDescription(StringUtils.defaultString(context.get("description", String.class)).strip());
        arc.setNote(StringUtils.defaultString(context.get("note", String.class)).strip());
        arc.setState(state(context.get("state", String.class), OutlineState.PLANNED));
        outline.add(index(context.get("position", Integer.class), outline.arcs().size()), arc);
        store(book, outline);
        answer(context, arcRow(numberOf(outline, arc), arc, true, true));
    }

    // --- update-arc ---

    @Command(commandName = UPDATE_ARC, linkedCommand = GET, humanNameText = "Update arc", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = UPDATE_ARC_TEXT, llmDescriptionText = UPDATE_ARC_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "title", type = String.class, optional = true, descriptionText = PARAM_TITLE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void updateArc(CommandContext context) throws Exception {
        runUpdateArc(context);
    }

    @Command(commandName = UPDATE_ARC, linkedCommand = GET, humanNameText = "Update arc", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = UPDATE_ARC_TEXT, llmDescriptionText = UPDATE_ARC_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "title", type = String.class, optional = true, descriptionText = PARAM_TITLE),
                    @CommandParameter(name = "description", type = String.class, optional = true, descriptionText = PARAM_DESCRIPTION)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void updateArcDescription(CommandContext context) throws Exception {
        runUpdateArc(context);
    }

    @Command(commandName = UPDATE_ARC, linkedCommand = GET, humanNameText = "Update arc", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = UPDATE_ARC_TEXT, llmDescriptionText = UPDATE_ARC_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "title", type = String.class, optional = true, descriptionText = PARAM_TITLE),
                    @CommandParameter(name = "description", type = String.class, optional = true, descriptionText = PARAM_DESCRIPTION),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = PARAM_NOTE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void updateArcNote(CommandContext context) throws Exception {
        runUpdateArc(context);
    }

    @Command(commandName = UPDATE_ARC, linkedCommand = GET, humanNameText = "Update arc", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = UPDATE_ARC_TEXT, llmDescriptionText = UPDATE_ARC_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "title", type = String.class, optional = true, descriptionText = PARAM_TITLE),
                    @CommandParameter(name = "description", type = String.class, optional = true, descriptionText = PARAM_DESCRIPTION),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = PARAM_NOTE),
                    @CommandParameter(name = "state", type = String.class, optional = true,
                            allowed = {"PLANNED", "CURRENT", "FINISHED"}, descriptionText = PARAM_STATE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void updateArcState(CommandContext context) throws Exception {
        runUpdateArc(context);
    }

    private void runUpdateArc(CommandContext context) throws Exception {
        Book book = new Book(context);
        ArcOutline outline = outline(book);
        Arc arc = arc(outline, context.get("arc", Integer.class));
        change(context, arc);
        store(book, outline);
        answer(context, arcRow(numberOf(outline, arc), arc, true, true));
    }

    /** Puts the fields the caller passed into the node; at least one has to be there. */
    private static void change(CommandContext context, OutlineNode node) {
        String title = context.get("title", String.class);
        String description = context.get("description", String.class);
        String note = context.get("note", String.class);
        String state = context.get("state", String.class);
        if (title == null && description == null && note == null && StringUtils.isBlank(state)) {
            throw new IllegalArgumentException("Nothing to change: pass the title, the description, the note or the state");
        }
        OutlineState newState = state(state, null);
        if (title != null) {
            node.setTitle(requiredTitle(title));
        }
        if (description != null) {
            node.setDescription(description.strip());
        }
        if (note != null) {
            node.setNote(note.strip());
        }
        // an unchanged state is not set again: finishing an arc would finish chapters that were reopened
        if (newState != null && newState != node.getState()) {
            node.setState(newState);
        }
    }

    // --- move-arc ---

    @Command(commandName = MOVE_ARC, linkedCommand = GET, humanNameText = "Move arc", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = MOVE_ARC_TEXT, llmDescriptionText = MOVE_ARC_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "position", type = Integer.class, descriptionText = "The place the arc gets, 1 for the first")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void moveArc(CommandContext context) throws Exception {
        Book book = new Book(context);
        ArcOutline outline = outline(book);
        Arc arc = arc(outline, context.get("arc", Integer.class));
        outline.moveTo(arc, index(context.get("position", Integer.class), outline.arcs().size() - 1));
        store(book, outline);
        answer(context, arcRow(numberOf(outline, arc), arc, false, false));
    }

    // --- delete-arc ---

    @Command(commandName = DELETE_ARC, linkedCommand = GET, humanNameText = "Delete arc", dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = DELETE_ARC_TEXT, llmDescriptionText = DELETE_ARC_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "What was deleted"))
    public void deleteArc(CommandContext context) throws Exception {
        Book book = new Book(context);
        ArcOutline outline = outline(book);
        Arc arc = arc(outline, context.get("arc", Integer.class));
        int chapters = arc.getChapters().size();
        String question = "Delete the arc " + arc.getTitle()
                + (chapters > 0 ? " with its " + chapters + (chapters == 1 ? " chapter?" : " chapters?") : "?");
        if (!context.confirm(question)) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was deleted");
            }
            return;
        }
        outline.delete(arc);
        store(book, outline);
        context.setReturn("Deleted the arc " + arc.getTitle() + (chapters > 0 ? " with " + chapters + " chapter(s)" : ""));
    }

    // --- add-outline-chapter ---

    @Command(commandName = ADD_CHAPTER, linkedCommand = GET, humanNameText = "Add outline chapter", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = ADD_CHAPTER_TEXT, llmDescriptionText = ADD_CHAPTER_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "title", type = String.class, descriptionText = PARAM_TITLE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void addChapter(CommandContext context) throws Exception {
        runAddChapter(context);
    }

    @Command(commandName = ADD_CHAPTER, linkedCommand = GET, humanNameText = "Add outline chapter", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = ADD_CHAPTER_TEXT, llmDescriptionText = ADD_CHAPTER_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "title", type = String.class, descriptionText = PARAM_TITLE),
                    @CommandParameter(name = "description", type = String.class, optional = true, descriptionText = PARAM_DESCRIPTION)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void addChapterDescription(CommandContext context) throws Exception {
        runAddChapter(context);
    }

    @Command(commandName = ADD_CHAPTER, linkedCommand = GET, humanNameText = "Add outline chapter", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = ADD_CHAPTER_TEXT, llmDescriptionText = ADD_CHAPTER_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "title", type = String.class, descriptionText = PARAM_TITLE),
                    @CommandParameter(name = "description", type = String.class, optional = true, descriptionText = PARAM_DESCRIPTION),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = PARAM_NOTE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void addChapterNote(CommandContext context) throws Exception {
        runAddChapter(context);
    }

    @Command(commandName = ADD_CHAPTER, linkedCommand = GET, humanNameText = "Add outline chapter", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = ADD_CHAPTER_TEXT, llmDescriptionText = ADD_CHAPTER_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "title", type = String.class, descriptionText = PARAM_TITLE),
                    @CommandParameter(name = "description", type = String.class, optional = true, descriptionText = PARAM_DESCRIPTION),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = PARAM_NOTE),
                    @CommandParameter(name = "state", type = String.class, optional = true, defaultValue = "PLANNED",
                            allowed = {"PLANNED", "CURRENT", "FINISHED"}, descriptionText = PARAM_STATE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void addChapterState(CommandContext context) throws Exception {
        runAddChapter(context);
    }

    @Command(commandName = ADD_CHAPTER, linkedCommand = GET, humanNameText = "Add outline chapter", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = ADD_CHAPTER_TEXT, llmDescriptionText = ADD_CHAPTER_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "title", type = String.class, descriptionText = PARAM_TITLE),
                    @CommandParameter(name = "description", type = String.class, optional = true, descriptionText = PARAM_DESCRIPTION),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = PARAM_NOTE),
                    @CommandParameter(name = "state", type = String.class, optional = true, defaultValue = "PLANNED",
                            allowed = {"PLANNED", "CURRENT", "FINISHED"}, descriptionText = PARAM_STATE),
                    @CommandParameter(name = "position", type = Integer.class, optional = true, descriptionText = PARAM_POSITION)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void addChapterPosition(CommandContext context) throws Exception {
        runAddChapter(context);
    }

    private void runAddChapter(CommandContext context) throws Exception {
        Book book = new Book(context);
        ArcOutline outline = outline(book);
        Integer arcNumber = context.get("arc", Integer.class);
        Arc arc = arc(outline, arcNumber);
        Chapter chapter = new Chapter();
        chapter.setTitle(requiredTitle(context.get("title", String.class)));
        chapter.setDescription(StringUtils.defaultString(context.get("description", String.class)).strip());
        chapter.setNote(StringUtils.defaultString(context.get("note", String.class)).strip());
        chapter.setState(state(context.get("state", String.class), OutlineState.PLANNED));
        outline.add(arc, index(context.get("position", Integer.class), arc.getChapters().size()), chapter);
        store(book, outline);
        answer(context, chapterAnswer(arcNumber, arc, chapter));
    }

    private static JsonObject chapterAnswer(int arcNumber, Arc arc, Chapter chapter) {
        JsonObject row = node(arc.getChapters().indexOf(chapter) + 1, chapter, true);
        row.addProperty("arc", arcNumber);
        return row;
    }

    // --- update-outline-chapter ---

    @Command(commandName = UPDATE_CHAPTER, linkedCommand = GET, humanNameText = "Update outline chapter", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = UPDATE_CHAPTER_TEXT, llmDescriptionText = UPDATE_CHAPTER_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "chapter", type = Integer.class, descriptionText = PARAM_CHAPTER),
                    @CommandParameter(name = "title", type = String.class, optional = true, descriptionText = PARAM_TITLE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void updateChapter(CommandContext context) throws Exception {
        runUpdateChapter(context);
    }

    @Command(commandName = UPDATE_CHAPTER, linkedCommand = GET, humanNameText = "Update outline chapter", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = UPDATE_CHAPTER_TEXT, llmDescriptionText = UPDATE_CHAPTER_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "chapter", type = Integer.class, descriptionText = PARAM_CHAPTER),
                    @CommandParameter(name = "title", type = String.class, optional = true, descriptionText = PARAM_TITLE),
                    @CommandParameter(name = "description", type = String.class, optional = true, descriptionText = PARAM_DESCRIPTION)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void updateChapterDescription(CommandContext context) throws Exception {
        runUpdateChapter(context);
    }

    @Command(commandName = UPDATE_CHAPTER, linkedCommand = GET, humanNameText = "Update outline chapter", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = UPDATE_CHAPTER_TEXT, llmDescriptionText = UPDATE_CHAPTER_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "chapter", type = Integer.class, descriptionText = PARAM_CHAPTER),
                    @CommandParameter(name = "title", type = String.class, optional = true, descriptionText = PARAM_TITLE),
                    @CommandParameter(name = "description", type = String.class, optional = true, descriptionText = PARAM_DESCRIPTION),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = PARAM_NOTE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void updateChapterNote(CommandContext context) throws Exception {
        runUpdateChapter(context);
    }

    @Command(commandName = UPDATE_CHAPTER, linkedCommand = GET, humanNameText = "Update outline chapter", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = UPDATE_CHAPTER_TEXT, llmDescriptionText = UPDATE_CHAPTER_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "chapter", type = Integer.class, descriptionText = PARAM_CHAPTER),
                    @CommandParameter(name = "title", type = String.class, optional = true, descriptionText = PARAM_TITLE),
                    @CommandParameter(name = "description", type = String.class, optional = true, descriptionText = PARAM_DESCRIPTION),
                    @CommandParameter(name = "note", type = String.class, optional = true, descriptionText = PARAM_NOTE),
                    @CommandParameter(name = "state", type = String.class, optional = true,
                            allowed = {"PLANNED", "CURRENT", "FINISHED"}, descriptionText = PARAM_STATE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void updateChapterState(CommandContext context) throws Exception {
        runUpdateChapter(context);
    }

    private void runUpdateChapter(CommandContext context) throws Exception {
        Book book = new Book(context);
        ArcOutline outline = outline(book);
        Integer arcNumber = context.get("arc", Integer.class);
        Arc arc = arc(outline, arcNumber);
        Chapter chapter = chapter(arc, arcNumber, context.get("chapter", Integer.class));
        change(context, chapter);
        store(book, outline);
        answer(context, chapterAnswer(arcNumber, arc, chapter));
    }

    // --- move-outline-chapter ---

    @Command(commandName = MOVE_CHAPTER, linkedCommand = GET, humanNameText = "Move outline chapter", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = MOVE_CHAPTER_TEXT, llmDescriptionText = MOVE_CHAPTER_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "chapter", type = Integer.class, descriptionText = PARAM_CHAPTER),
                    @CommandParameter(name = "position", type = Integer.class, descriptionText = "The place the chapter gets in the arc, 1 for the first")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void moveChapter(CommandContext context) throws Exception {
        runMoveChapter(context);
    }

    @Command(commandName = MOVE_CHAPTER, linkedCommand = GET, humanNameText = "Move outline chapter", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            descriptionText = MOVE_CHAPTER_TEXT, llmDescriptionText = MOVE_CHAPTER_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "chapter", type = Integer.class, descriptionText = PARAM_CHAPTER),
                    @CommandParameter(name = "position", type = Integer.class, descriptionText = "The place the chapter gets in the arc, 1 for the first"),
                    @CommandParameter(name = "to_arc", type = Integer.class, optional = true, descriptionText = PARAM_TO_ARC)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_NODE))
    public void moveChapterToArc(CommandContext context) throws Exception {
        runMoveChapter(context);
    }

    private void runMoveChapter(CommandContext context) throws Exception {
        Book book = new Book(context);
        ArcOutline outline = outline(book);
        Integer arcNumber = context.get("arc", Integer.class);
        Arc arc = arc(outline, arcNumber);
        Chapter chapter = chapter(arc, arcNumber, context.get("chapter", Integer.class));
        Integer toNumber = context.get("to_arc", Integer.class);
        Arc target = toNumber == null ? arc : arc(outline, toNumber);
        // the chapter leaves its arc first, so a move inside the arc has one place less to choose from
        int size = target.getChapters().size() - (target == arc ? 1 : 0);
        outline.moveTo(chapter, target, index(context.get("position", Integer.class), size));
        store(book, outline);
        answer(context, chapterAnswer(numberOf(outline, target), target, chapter));
    }

    // --- delete-outline-chapter ---

    @Command(commandName = DELETE_CHAPTER, linkedCommand = GET, humanNameText = "Delete outline chapter", dataSeverity = CommandDataSeverity.DESTRUCTIVE,
            descriptionText = DELETE_CHAPTER_TEXT, llmDescriptionText = DELETE_CHAPTER_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "chapter", type = Integer.class, descriptionText = PARAM_CHAPTER)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = "What was deleted"))
    public void deleteChapter(CommandContext context) throws Exception {
        Book book = new Book(context);
        ArcOutline outline = outline(book);
        Integer arcNumber = context.get("arc", Integer.class);
        Arc arc = arc(outline, arcNumber);
        Chapter chapter = chapter(arc, arcNumber, context.get("chapter", Integer.class));
        if (!context.confirm("Delete the chapter " + chapter.getTitle() + "?")) {
            if (!context.isTerminated()) {
                context.setReturn("Nothing was deleted");
            }
            return;
        }
        outline.delete(chapter);
        store(book, outline);
        context.setReturn("Deleted the chapter " + chapter.getTitle());
    }

}
