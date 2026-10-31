package com.github.enerccio.marginalia.extensions.arcoutline.commands;

import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.command.CommandScope;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.extensions.arcoutline.model.*;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService.Generated;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService.ModelSelection;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService.StoryMessage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.concurrent.CancellationException;

/**
 * What the generator dialogs of the Arc Outline do, as commands for the command bar and for agents:
 * {@code generate-arc}, {@code generate-outline-chapters} and {@code import-arc-from-story}. They build the same prompt
 * from the same templates (the user's, or the defaults, see the extension settings) and ask the same JSON schema, with
 * the model of the book (the default model of the user when the book has none) and without name tools: a caller that
 * wants names gets them with the name commands and puts them into the specification.
 * <p>
 * Like the dialogs they save only when asked: with {@code save} false (the default) the generated arc or chapters are
 * only returned, with {@code save} true they are added to the outline the way the dialogs' <i>Save</i> does and the
 * answer says where. Either way the commands only add, so they are {@code NON_DESTRUCTIVE}.
 */
public class ArcGenerationCommands extends ArcOutlineCommandSupport {

    public static final String GENERATE_ARC = "generate-arc";
    public static final String GENERATE_CHAPTERS = "generate-outline-chapters";
    public static final String IMPORT_ARC = "import-arc-from-story";

    private static final int MAX_CHAPTERS = 40;

    private static final String GENERATE_ARC_TEXT = "Asks the model to write a new arc for the outline, against the lore and the arcs of the book. "
            + "Saves it only when asked.";
    private static final String GENERATE_ARC_LLM = "Asks the model of the book to write a new arc (title, terse description, full note) against the "
            + "lore of the book and the arcs the outline has already, and returns it. The quality depends on the specification: say what the arc is "
            + "about, the characters and places, where it should lead. Pass save true to add it at the end of the outline at once (the answer then "
            + "has its number), otherwise nothing is stored and you can add it with add-arc, edited. Takes a while.";
    private static final String GENERATE_CHAPTERS_TEXT = "Asks the model to write chapters for an arc of the outline. Saves them only when asked.";
    private static final String GENERATE_CHAPTERS_LLM = "Asks the model of the book to write the chapters of an arc of the outline (title, terse "
            + "description, full note each), against the lore and the arcs up to this one, and returns them. Say in the specification what the "
            + "chapters should do, their pace and tone; pass count for an exact number, else the model decides. context says how much of the outline "
            + "the model sees: EVERYTHING (all the chapters of the arcs up to this one, default), ARCS_ONLY, or ARCS_AND_FINISHED (the chapters "
            + "that are finished). Pass save true to add the chapters to the end of the arc at once, otherwise nothing is stored. Takes a while.";
    private static final String IMPORT_TEXT = "Asks the model to summarize the written story (or a range of it) as an arc with chapters. Saves it only when asked.";
    private static final String IMPORT_LLM = "Asks the model of the book to turn the written story - the messages with text of the active branch, "
            + "numbered from 1; from and to choose a range - into an arc with chapters, as the outline plans them, and returns it. Use it to "
            + "record what has been written. count is an exact number of chapters, else the model divides the story where it divides naturally. "
            + "Pass save true to add the arc at the end of the outline at once, with every node in the state given (FINISHED by default, the story "
            + "is written already), otherwise nothing is stored. The range has to fit the model: a story that is too long is refused, choose "
            + "fewer messages. Takes a while.";
    private static final String RESULT_GENERATED = "JSON: the generated arc or chapters, saved (true when they were added to the outline)";

    private static final String PARAM_SPECIFICATION = "What you want: the theme, the characters, where it should lead...; the model decides when omitted";
    private static final String PARAM_SAVE = "true to add the result to the outline (default false: only return it)";
    private static final String PARAM_COUNT = "An exact number of chapters; the model decides when omitted";
    private static final String PARAM_CONTEXT = "How much of the outline the model sees: EVERYTHING (default), ARCS_ONLY or ARCS_AND_FINISHED";

    public ArcGenerationCommands(ArcOutlineService service) {
        super(service);
    }

    // --- generate-arc ---

    @Command(commandName = GENERATE_ARC, linkedCommand = ArcOutlineCommands.GET, batch = BatchPolicy.SINGLE, humanNameText = "Generate arc", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = GENERATE_ARC_TEXT, llmDescriptionText = GENERATE_ARC_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_GENERATED))
    public void generateArcAny(CommandContext context) throws Exception {
        runGenerateArc(context);
    }

    @Command(commandName = GENERATE_ARC, linkedCommand = ArcOutlineCommands.GET, batch = BatchPolicy.SINGLE, humanNameText = "Generate arc", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = GENERATE_ARC_TEXT, llmDescriptionText = GENERATE_ARC_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "specification", type = String.class, optional = true, descriptionText = PARAM_SPECIFICATION)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_GENERATED))
    public void generateArc(CommandContext context) throws Exception {
        runGenerateArc(context);
    }

    @Command(commandName = GENERATE_ARC, linkedCommand = ArcOutlineCommands.GET, batch = BatchPolicy.SINGLE, humanNameText = "Generate arc", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = GENERATE_ARC_TEXT, llmDescriptionText = GENERATE_ARC_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "specification", type = String.class, optional = true, descriptionText = PARAM_SPECIFICATION),
                    @CommandParameter(name = "save", type = Boolean.class, optional = true, defaultValue = "false", descriptionText = PARAM_SAVE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_GENERATED))
    public void generateArcSave(CommandContext context) throws Exception {
        runGenerateArc(context);
    }

    private void runGenerateArc(CommandContext context) throws Exception {
        Book book = new Book(context);
        ArcOutline outline = outline(book);
        ModelSelection model = requireModel(context, book);
        String prompt = service.buildPrompt(book.manuscript, outline,
                service.generationPrompt(service.getSettings()), context.get("specification", String.class), null);

        context.setStatus("Generating an arc");
        Generated<GeneratedArc> generated;
        try {
            generated = service.generate(model, book.manuscript, prompt, service.schema(), GeneratedArc.class);
        } catch (CancellationException e) {
            return;
        }
        if (context.isTerminated()) {
            return;
        }
        GeneratedArc result = generated.value();
        Arc arc = new Arc();
        arc.setTitle(result.name().trim());
        arc.setDescription(result.description().trim());
        arc.setNote(result.note().trim());
        arc.setGenerationResult(generated.answer());

        boolean save = Boolean.TRUE.equals(context.get("save", Boolean.class));
        if (save) {
            // the outline may have been changed while the model wrote
            outline = outline(book);
            outline.add(arc);
            store(book, outline);
        }
        JsonObject answer = arcRow(save ? numberOf(outline, arc) : 0, arc, true, false);
        if (!save) {
            answer.remove("number");
            answer.remove("chapterCount");
        }
        JsonObject root = new JsonObject();
        root.addProperty("saved", save);
        root.add("arc", answer);
        answer(context, root);
    }

    // --- generate-outline-chapters ---

    @Command(commandName = GENERATE_CHAPTERS, linkedCommand = ArcOutlineCommands.GET, batch = BatchPolicy.SINGLE, humanNameText = "Generate outline chapters", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = GENERATE_CHAPTERS_TEXT, llmDescriptionText = GENERATE_CHAPTERS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_GENERATED))
    public void generateChapters(CommandContext context) throws Exception {
        runGenerateChapters(context);
    }

    @Command(commandName = GENERATE_CHAPTERS, linkedCommand = ArcOutlineCommands.GET, batch = BatchPolicy.SINGLE, humanNameText = "Generate outline chapters", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = GENERATE_CHAPTERS_TEXT, llmDescriptionText = GENERATE_CHAPTERS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "specification", type = String.class, optional = true, descriptionText = PARAM_SPECIFICATION)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_GENERATED))
    public void generateChaptersSpecification(CommandContext context) throws Exception {
        runGenerateChapters(context);
    }

    @Command(commandName = GENERATE_CHAPTERS, linkedCommand = ArcOutlineCommands.GET, batch = BatchPolicy.SINGLE, humanNameText = "Generate outline chapters", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = GENERATE_CHAPTERS_TEXT, llmDescriptionText = GENERATE_CHAPTERS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "specification", type = String.class, optional = true, descriptionText = PARAM_SPECIFICATION),
                    @CommandParameter(name = "count", type = Integer.class, optional = true, descriptionText = PARAM_COUNT)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_GENERATED))
    public void generateChaptersCount(CommandContext context) throws Exception {
        runGenerateChapters(context);
    }

    @Command(commandName = GENERATE_CHAPTERS, linkedCommand = ArcOutlineCommands.GET, batch = BatchPolicy.SINGLE, humanNameText = "Generate outline chapters", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = GENERATE_CHAPTERS_TEXT, llmDescriptionText = GENERATE_CHAPTERS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "specification", type = String.class, optional = true, descriptionText = PARAM_SPECIFICATION),
                    @CommandParameter(name = "count", type = Integer.class, optional = true, descriptionText = PARAM_COUNT),
                    @CommandParameter(name = "context", type = String.class, optional = true, defaultValue = "EVERYTHING",
                            allowed = {"EVERYTHING", "ARCS_ONLY", "ARCS_AND_FINISHED"}, descriptionText = PARAM_CONTEXT)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_GENERATED))
    public void generateChaptersContext(CommandContext context) throws Exception {
        runGenerateChapters(context);
    }

    @Command(commandName = GENERATE_CHAPTERS, linkedCommand = ArcOutlineCommands.GET, batch = BatchPolicy.SINGLE, humanNameText = "Generate outline chapters", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = GENERATE_CHAPTERS_TEXT, llmDescriptionText = GENERATE_CHAPTERS_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "arc", type = Integer.class, descriptionText = PARAM_ARC),
                    @CommandParameter(name = "specification", type = String.class, optional = true, descriptionText = PARAM_SPECIFICATION),
                    @CommandParameter(name = "count", type = Integer.class, optional = true, descriptionText = PARAM_COUNT),
                    @CommandParameter(name = "context", type = String.class, optional = true, defaultValue = "EVERYTHING",
                            allowed = {"EVERYTHING", "ARCS_ONLY", "ARCS_AND_FINISHED"}, descriptionText = PARAM_CONTEXT),
                    @CommandParameter(name = "save", type = Boolean.class, optional = true, defaultValue = "false", descriptionText = PARAM_SAVE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_GENERATED))
    public void generateChaptersSave(CommandContext context) throws Exception {
        runGenerateChapters(context);
    }

    private void runGenerateChapters(CommandContext context) throws Exception {
        Book book = new Book(context);
        ArcOutline outline = outline(book);
        Integer arcNumber = context.get("arc", Integer.class);
        Arc arc = arc(outline, arcNumber);
        Integer count = context.get("count", Integer.class);
        if (count != null && (count < 1 || count > MAX_CHAPTERS)) {
            throw new IllegalArgumentException("The count is 1 to " + MAX_CHAPTERS);
        }
        ChapterContext chapterContext = option(ChapterContext.class, context.get("context", String.class), ChapterContext.EVERYTHING, "context");
        ModelSelection model = requireModel(context, book);
        String prompt = service.buildChapterPrompt(book.manuscript, outline, arc, chapterContext,
                service.chapterGenerationPrompt(service.getSettings()),
                count != null ? "exactly " + count : "as many as the arc needs",
                context.get("specification", String.class), "(nothing in particular, decide yourself)", null);

        context.setStatus("Generating the chapters of the arc " + arcNumber);
        Generated<GeneratedChapters> generated;
        try {
            generated = service.generate(model, book.manuscript, prompt, service.chaptersSchema(count), GeneratedChapters.class);
        } catch (CancellationException e) {
            return;
        }
        if (context.isTerminated()) {
            return;
        }
        List<Chapter> chapters = generated.value().chapters().stream().map(c -> chapter(c, generated.answer())).toList();

        boolean save = Boolean.TRUE.equals(context.get("save", Boolean.class));
        int first = 0;
        if (save) {
            // the outline may have been changed while the model wrote: the arc is found again by its place
            outline = outline(book);
            arc = arc(outline, arcNumber);
            first = arc.getChapters().size();
            for (Chapter chapter : chapters) {
                outline.add(arc, chapter);
            }
            store(book, outline);
        }
        JsonObject root = new JsonObject();
        root.addProperty("saved", save);
        root.addProperty("arc", arcNumber);
        JsonArray list = new JsonArray();
        int number = first;
        for (Chapter chapter : chapters) {
            JsonObject row = node(++number, chapter, true);
            if (!save) {
                row.remove("number");
            }
            list.add(row);
        }
        root.add("chapters", list);
        answer(context, root);
    }

    private static Chapter chapter(GeneratedChapter generated, String answer) {
        Chapter chapter = new Chapter();
        chapter.setTitle(generated.name().trim());
        chapter.setDescription(generated.description().trim());
        chapter.setNote(generated.note().trim());
        // every chapter keeps what the model wrote for all of them, as it came
        chapter.setGenerationResult(answer);
        return chapter;
    }

    // --- import-arc-from-story ---

    @Command(commandName = IMPORT_ARC, linkedCommand = ArcOutlineCommands.GET, humanNameText = "Import arc from story", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = IMPORT_TEXT, llmDescriptionText = IMPORT_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_GENERATED))
    public void importStory(CommandContext context) throws Exception {
        runImport(context);
    }

    @Command(commandName = IMPORT_ARC, linkedCommand = ArcOutlineCommands.GET, humanNameText = "Import arc from story", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = IMPORT_TEXT, llmDescriptionText = IMPORT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "from", type = Integer.class, optional = true,
                            descriptionText = "Number of the first message to send, counted among the messages with text of the active branch; the first when omitted")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_GENERATED))
    public void importStoryFrom(CommandContext context) throws Exception {
        runImport(context);
    }

    @Command(commandName = IMPORT_ARC, linkedCommand = ArcOutlineCommands.GET, humanNameText = "Import arc from story", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = IMPORT_TEXT, llmDescriptionText = IMPORT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "from", type = Integer.class, optional = true,
                            descriptionText = "Number of the first message to send, counted among the messages with text of the active branch; the first when omitted"),
                    @CommandParameter(name = "to", type = Integer.class, optional = true,
                            descriptionText = "Number of the last message to send; the last one when omitted")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_GENERATED))
    public void importStoryTo(CommandContext context) throws Exception {
        runImport(context);
    }

    @Command(commandName = IMPORT_ARC, linkedCommand = ArcOutlineCommands.GET, humanNameText = "Import arc from story", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = IMPORT_TEXT, llmDescriptionText = IMPORT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "from", type = Integer.class, optional = true,
                            descriptionText = "Number of the first message to send, counted among the messages with text of the active branch; the first when omitted"),
                    @CommandParameter(name = "to", type = Integer.class, optional = true,
                            descriptionText = "Number of the last message to send; the last one when omitted"),
                    @CommandParameter(name = "count", type = Integer.class, optional = true, descriptionText = PARAM_COUNT)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_GENERATED))
    public void importStoryCount(CommandContext context) throws Exception {
        runImport(context);
    }

    @Command(commandName = IMPORT_ARC, linkedCommand = ArcOutlineCommands.GET, humanNameText = "Import arc from story", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = IMPORT_TEXT, llmDescriptionText = IMPORT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "from", type = Integer.class, optional = true,
                            descriptionText = "Number of the first message to send, counted among the messages with text of the active branch; the first when omitted"),
                    @CommandParameter(name = "to", type = Integer.class, optional = true,
                            descriptionText = "Number of the last message to send; the last one when omitted"),
                    @CommandParameter(name = "count", type = Integer.class, optional = true, descriptionText = PARAM_COUNT),
                    @CommandParameter(name = "state", type = String.class, optional = true, defaultValue = "FINISHED",
                            allowed = {"PLANNED", "CURRENT", "FINISHED"}, descriptionText = "State of the saved arc and its chapters")}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_GENERATED))
    public void importStoryState(CommandContext context) throws Exception {
        runImport(context);
    }

    @Command(commandName = IMPORT_ARC, linkedCommand = ArcOutlineCommands.GET, humanNameText = "Import arc from story", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, descriptionText = IMPORT_TEXT, llmDescriptionText = IMPORT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "from", type = Integer.class, optional = true,
                            descriptionText = "Number of the first message to send, counted among the messages with text of the active branch; the first when omitted"),
                    @CommandParameter(name = "to", type = Integer.class, optional = true,
                            descriptionText = "Number of the last message to send; the last one when omitted"),
                    @CommandParameter(name = "count", type = Integer.class, optional = true, descriptionText = PARAM_COUNT),
                    @CommandParameter(name = "state", type = String.class, optional = true, defaultValue = "FINISHED",
                            allowed = {"PLANNED", "CURRENT", "FINISHED"}, descriptionText = "State of the saved arc and its chapters"),
                    @CommandParameter(name = "save", type = Boolean.class, optional = true, defaultValue = "false", descriptionText = PARAM_SAVE)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_GENERATED))
    public void importStorySave(CommandContext context) throws Exception {
        runImport(context);
    }

    private void runImport(CommandContext context) throws Exception {
        Book book = new Book(context);
        ArcOutline outline = outline(book);
        List<StoryMessage> messages = service.activeBranch(book.manuscript);
        if (messages.isEmpty()) {
            throw new IllegalStateException("The active branch has no text to import");
        }
        Integer from = context.get("from", Integer.class);
        Integer to = context.get("to", Integer.class);
        int first = from != null ? from : 1;
        int last = to != null ? to : messages.size();
        if (first < 1 || last > messages.size()) {
            throw new IllegalArgumentException("The messages are numbered 1 to " + messages.size());
        }
        if (first > last) {
            throw new IllegalArgumentException("The first message is after the last one");
        }
        Integer count = context.get("count", Integer.class);
        if (count != null && (count < 1 || count > MAX_CHAPTERS)) {
            throw new IllegalArgumentException("The count is 1 to " + MAX_CHAPTERS);
        }
        OutlineState state = state(context.get("state", String.class), OutlineState.FINISHED);
        ModelSelection model = requireModel(context, book);
        String prompt = service.buildImportPrompt(book.manuscript, outline, service.importPrompt(service.getSettings()),
                messages.subList(first - 1, last), count);
        int available = TokenLimits.promptTokens(model.ai(), model.protocol());
        long tokens = service.countTokens(prompt);
        if (available > 0 && tokens > available) {
            throw new IllegalArgumentException("The prompt is about " + tokens + " tokens and the model takes " + available
                    + ", choose fewer messages (from and to)");
        }

        context.setStatus("Summarizing messages " + first + " to " + last + " as an arc");
        Generated<GeneratedStoryArc> generated;
        try {
            generated = service.generate(model, book.manuscript, prompt, service.importSchema(count), GeneratedStoryArc.class);
        } catch (CancellationException e) {
            return;
        }
        if (context.isTerminated()) {
            return;
        }
        GeneratedStoryArc result = generated.value();
        Arc arc = new Arc();
        arc.setTitle(result.name().trim());
        arc.setDescription(result.description().trim());
        arc.setNote(result.note().trim());
        arc.setGenerationResult(generated.answer());
        for (GeneratedChapter item : result.chapters()) {
            Chapter chapter = chapter(item, generated.answer());
            chapter.setState(state);
            arc.getChapters().add(chapter);
        }
        // after the chapters, a finished arc finishes them
        arc.setState(state);

        boolean save = Boolean.TRUE.equals(context.get("save", Boolean.class));
        if (save) {
            outline = outline(book);
            outline.add(arc);
            store(book, outline);
        }
        JsonObject root = new JsonObject();
        root.addProperty("saved", save);
        JsonObject row = arcRow(save ? numberOf(outline, arc) : 0, arc, true, true);
        if (!save) {
            row.remove("number");
        }
        root.add("arc", row);
        answer(context, root);
    }

    // --- helpers ---

    private ModelSelection requireModel(CommandContext context, Book book) throws Exception {
        // an agent works with the model its scope names, see CommandScope.getModel
        CommandScope.ModelChoice scoped = context.getScope().getModel();
        if (scoped != null && scoped.ai() != null) {
            return new ModelSelection(scoped.ai(), scoped.protocol());
        }
        ModelSelection model = service.resolveModel(book.manuscript);
        if (model == null) {
            throw new IllegalStateException("Neither the book nor the user settings have a model, choose one");
        }
        return model;
    }

}
