package com.github.enerccio.marginalia.extensions.loregenerator.commands;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.command.CommandScope;
import com.github.enerccio.marginalia.domain.service.impl.commands.lorebook.read.LorebookReadSupport;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.extensions.loregenerator.model.LoreGeneratorData;
import com.github.enerccio.marginalia.extensions.loregenerator.service.LoreGeneratorService;
import com.github.enerccio.marginalia.extensions.loregenerator.service.LoreGeneratorService.EntryLimit;
import com.github.enerccio.marginalia.extensions.loregenerator.service.LoreGeneratorService.LoreScope;
import com.github.enerccio.marginalia.extensions.loregenerator.service.LoreGeneratorService.ModelSelection;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CancellationException;

/**
 * The commands of the Lore Generator for the command bar and for agents: {@code list-lore-templates},
 * {@code generate-lore-entry} and {@code compact-lore-entry}. They do what the generator dialog does, in the same two
 * steps, and like the dialog they store nothing: the generated and the compacted text are only returned, putting them
 * into an entry (the dialog's <i>Copy to Lore Entry</i>) is up to the caller. All three only read.
 * <p>
 * The lorebook and the entry are addressed by number like in the commands of the application ({@code list-lorebooks},
 * {@code list-lore-entries}), and the model is the default model of the user. The entry is optional: it is the entry
 * the text is meant for, whose place decides which lore is shown and which is left out, without it a new entry after
 * all the others is meant. The model gets no name tools: an agent that wants names calls the commands of the Name
 * Generator and the Name Provider and writes them into its prompt. As in the dialog, images can be sent with the
 * generation (not with the compaction, which works on the generated text only): the {@code images} parameter takes
 * image files, for an agent the paths of images in its workspace.
 */
public class LoreGeneratorCommands extends LorebookReadSupport {

    public static final String LIST = "list-lore-templates";
    public static final String GENERATE = "generate-lore-entry";
    public static final String COMPACT = "compact-lore-entry";

    private static final String LIST_TEXT = "Lists the saved generation prompts (templates) of the Lore Generator with their length and the start "
            + "of their text; with a template name it returns the whole text of that template. Reads only.";
    private static final String LIST_LLM = "Lists the saved generation prompts (templates) of the Lore Generator: name, length and the start of "
            + "the text, and the one used last. Pass a template name to get the whole text of it, for example to copy it, fill in its "
            + "placeholders and pass the result as the prompt of generate-lore-entry. Reads only.";
    private static final String GENERATE_TEXT = "Generates the text of a lore entry with the default model of the user, written against the lore "
            + "of the lorebook, and returns it. Stores nothing. Compact it afterwards.";
    private static final String GENERATE_LLM = "Generates a long, atmospheric text for a lore entry, written against the existing lore of the "
            + "lorebook, and returns it as plain text. Stores nothing: put the result into an entry yourself when you are happy with it. Run "
            + "compact-lore-entry on the result next to make the dense text the stories use. The quality comes from the user's generation "
            + "prompt (template), which is added in front of your prompt by default (the one used last, or name another from "
            + "list-lore-templates, which also returns the text of one). The template asks for a subject, the core details to weave "
            + "in, the target length and the structure, so your prompt must give exactly that: say who or what the entry is about, list every "
            + "fact it must contain, how long it should be and which sections it needs. A one-line prompt gives a poor entry. To change the "
            + "template itself, get its text with list-lore-templates, edit it and pass the whole text as the prompt with the template "
            + "\"none\". The model has no name tools: get names with the name commands first and put them into the prompt. Pass the "
            + "number of the entry the text is for to have the lore after it left out and the entry itself not shown. If the entry does not "
            + "exist yet, pass the order it will get instead (the order that list-lore-entries shows, lower comes first): the model then sees "
            + "the entries before that order; without either, the text is written for a new entry after all the others. Pass images (paths of "
            + "image files of your workspace, a reference picture, a map, a portrait) to have the model look at them while it writes: it is the "
            + "model of the user that sees them, and it must take images, or the command fails. Takes a while. Reads only.";
    private static final String COMPACT_TEXT = "Compacts a generated text into a dense lore entry text with the default model of the user and "
            + "returns it. Stores nothing.";
    private static final String COMPACT_LLM = "Turns a generated text (the result of generate-lore-entry) into a dense, factual lore entry text "
            + "and returns it as plain text, without the code block the model wraps it in. Stores nothing: put the result into an entry yourself. "
            + "Pass the number of the entry the text is for, or the order of an entry that does not exist yet, as in generate-lore-entry. Takes a while. Reads only.";
    private static final String RESULT_LIST = "JSON: the templates (name, length, preview) and the one used last, or the name and text of one";
    private static final String RESULT_GENERATE = "The text";
    private static final String PARAM_TEMPLATE_LIST = "Name of a template, to get its whole text";
    /** Between the template and the prompt of the caller, so that the model reads it as the values for the template. */
    private static final String TASK_HEADER = "\n\n### Task for this entry\n";
    private static final String NO_TEMPLATE = "none";

    private static final String PARAM_ENTRY = "The entry the text is for: its handle, or its number in its lorebook, the first entry is 1. Omit it for a new entry";
    private static final String PARAM_PROMPT = "What this entry is about: the subject, every fact it must contain, the target length and the "
            + "sections wanted. Put in after the template as the task for this entry";
    private static final String PARAM_TEMPLATE = "Name of a saved generation prompt from list-lore-templates to put in front of the prompt; "
            + "the one used last when omitted, \"none\" for no template (then the prompt is sent as it is)";
    private static final String PARAM_ORDER = "For an entry that does not exist yet: the order it will have (the order list-lore-entries shows). "
            + "With limit UP_TO_POSITION the model sees only the entries before that order. Omit it to put the new entry after all the others. "
            + "Ignored when entry is given";
    private static final String PARAM_LOREBOOK = "The lorebook: its handle or its number from list-lorebooks; the root lorebook (1) when omitted";
    private static final String PARAM_SCOPE = "Which lore the model sees: CURRENT_LOREBOOK (default) or ALL_LORE (with the subbooks)";
    private static final String PARAM_LIMIT = "Which entries of that lore the model sees: UP_TO_POSITION (default, those before the entry) or ALL_ENTRIES";
    private static final String PARAM_IMAGES = "Images the model looks at while it writes, for example a reference picture or a map; the prompt "
            + "should say what to take from them";
    private static final String PARAM_TEXT = "The text to compact";

    private final LoreGeneratorService service;

    public LoreGeneratorCommands(LoreGeneratorService service) {
        this.service = service;
    }

    // --- list-lore-templates ---

    @Command(commandName = LIST, humanNameText = "List lore templates", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = LIST_TEXT, llmDescriptionText = LIST_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_LIST))
    public void listTemplates(CommandContext context) throws Exception {
        list(context, null);
    }

    @Command(commandName = LIST, humanNameText = "List lore templates", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true,
            descriptionText = LIST_TEXT, llmDescriptionText = LIST_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "template", type = String.class, descriptionText = PARAM_TEMPLATE_LIST)}),
            returns = @CommandReturn(name = "result", type = String.class, descriptionText = RESULT_LIST))
    public void getTemplate(CommandContext context) throws Exception {
        list(context, context.get("template", String.class));
    }

    private void list(CommandContext context, String template) throws Exception {
        LoreGeneratorData data = service.getData();
        JsonObject result = new JsonObject();
        if (StringUtils.isNotBlank(template)) {
            String name = templateName(data, template);
            result.addProperty("name", name);
            result.addProperty("text", data.getGenerationPrompts().get(name));
            answer(context, result);
            return;
        }
        JsonArray templates = new JsonArray();
        for (Map.Entry<String, String> prompt : data.getGenerationPrompts().entrySet()) {
            JsonObject row = new JsonObject();
            row.addProperty("name", prompt.getKey());
            row.addProperty("length", prompt.getValue().length());
            row.addProperty("preview", preview(prompt.getValue(), PREVIEW_LENGTH));
            templates.add(row);
        }
        result.add("templates", templates);
        String last = data.getLastGenerationPrompt();
        result.addProperty("lastUsed", last != null && data.getGenerationPrompts().containsKey(last) ? last : null);
        answer(context, result);
    }

    // --- generate-lore-entry ---

    @Command(commandName = GENERATE, humanNameText = "Generate lore entry", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true, descriptionText = GENERATE_TEXT, llmDescriptionText = GENERATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "prompt", type = String.class, optional = true, descriptionText = PARAM_PROMPT)}),
            returns = @CommandReturn(name = "text", type = String.class, descriptionText = RESULT_GENERATE))
    public void generate(CommandContext context) throws Exception {
        runGenerate(context);
    }

    @Command(commandName = GENERATE, humanNameText = "Generate lore entry", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true, descriptionText = GENERATE_TEXT, llmDescriptionText = GENERATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "prompt", type = String.class, optional = true, descriptionText = PARAM_PROMPT),
                    @CommandParameter(name = "template", type = String.class, optional = true, descriptionText = PARAM_TEMPLATE)}),
            returns = @CommandReturn(name = "text", type = String.class, descriptionText = RESULT_GENERATE))
    public void generateTemplate(CommandContext context) throws Exception {
        runGenerate(context);
    }

    @Command(commandName = GENERATE, humanNameText = "Generate lore entry", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true, descriptionText = GENERATE_TEXT, llmDescriptionText = GENERATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "prompt", type = String.class, optional = true, descriptionText = PARAM_PROMPT),
                    @CommandParameter(name = "template", type = String.class, optional = true, descriptionText = PARAM_TEMPLATE),
                    @CommandParameter(name = "lorebook", type = String.class, optional = true, descriptionText = PARAM_LOREBOOK)}),
            returns = @CommandReturn(name = "text", type = String.class, descriptionText = RESULT_GENERATE))
    public void generateLorebook(CommandContext context) throws Exception {
        runGenerate(context);
    }

    @Command(commandName = GENERATE, humanNameText = "Generate lore entry", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true, descriptionText = GENERATE_TEXT, llmDescriptionText = GENERATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "prompt", type = String.class, optional = true, descriptionText = PARAM_PROMPT),
                    @CommandParameter(name = "template", type = String.class, optional = true, descriptionText = PARAM_TEMPLATE),
                    @CommandParameter(name = "lorebook", type = String.class, optional = true, descriptionText = PARAM_LOREBOOK),
                    @CommandParameter(name = "entry", type = String.class, optional = true, descriptionText = PARAM_ENTRY)}),
            returns = @CommandReturn(name = "text", type = String.class, descriptionText = RESULT_GENERATE))
    public void generateEntry(CommandContext context) throws Exception {
        runGenerate(context);
    }

    @Command(commandName = GENERATE, humanNameText = "Generate lore entry", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true, descriptionText = GENERATE_TEXT, llmDescriptionText = GENERATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "prompt", type = String.class, optional = true, descriptionText = PARAM_PROMPT),
                    @CommandParameter(name = "template", type = String.class, optional = true, descriptionText = PARAM_TEMPLATE),
                    @CommandParameter(name = "lorebook", type = String.class, optional = true, descriptionText = PARAM_LOREBOOK),
                    @CommandParameter(name = "entry", type = String.class, optional = true, descriptionText = PARAM_ENTRY),
                    @CommandParameter(name = "order", type = Integer.class, optional = true, descriptionText = PARAM_ORDER)}),
            returns = @CommandReturn(name = "text", type = String.class, descriptionText = RESULT_GENERATE))
    public void generateOrder(CommandContext context) throws Exception {
        runGenerate(context);
    }

    @Command(commandName = GENERATE, humanNameText = "Generate lore entry", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true, descriptionText = GENERATE_TEXT, llmDescriptionText = GENERATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "prompt", type = String.class, optional = true, descriptionText = PARAM_PROMPT),
                    @CommandParameter(name = "template", type = String.class, optional = true, descriptionText = PARAM_TEMPLATE),
                    @CommandParameter(name = "lorebook", type = String.class, optional = true, descriptionText = PARAM_LOREBOOK),
                    @CommandParameter(name = "entry", type = String.class, optional = true, descriptionText = PARAM_ENTRY),
                    @CommandParameter(name = "order", type = Integer.class, optional = true, descriptionText = PARAM_ORDER),
                    @CommandParameter(name = "scope", type = String.class, optional = true, defaultValue = "CURRENT_LOREBOOK",
                            allowed = {"CURRENT_LOREBOOK", "ALL_LORE"}, descriptionText = PARAM_SCOPE)}),
            returns = @CommandReturn(name = "text", type = String.class, descriptionText = RESULT_GENERATE))
    public void generateScope(CommandContext context) throws Exception {
        runGenerate(context);
    }

    @Command(commandName = GENERATE, humanNameText = "Generate lore entry", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true, descriptionText = GENERATE_TEXT, llmDescriptionText = GENERATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "prompt", type = String.class, optional = true, descriptionText = PARAM_PROMPT),
                    @CommandParameter(name = "template", type = String.class, optional = true, descriptionText = PARAM_TEMPLATE),
                    @CommandParameter(name = "lorebook", type = String.class, optional = true, descriptionText = PARAM_LOREBOOK),
                    @CommandParameter(name = "entry", type = String.class, optional = true, descriptionText = PARAM_ENTRY),
                    @CommandParameter(name = "order", type = Integer.class, optional = true, descriptionText = PARAM_ORDER),
                    @CommandParameter(name = "scope", type = String.class, optional = true, defaultValue = "CURRENT_LOREBOOK",
                            allowed = {"CURRENT_LOREBOOK", "ALL_LORE"}, descriptionText = PARAM_SCOPE),
                    @CommandParameter(name = "limit", type = String.class, optional = true, defaultValue = "UP_TO_POSITION",
                            allowed = {"UP_TO_POSITION", "ALL_ENTRIES"}, descriptionText = PARAM_LIMIT)}),
            returns = @CommandReturn(name = "text", type = String.class, descriptionText = RESULT_GENERATE))
    public void generateLimit(CommandContext context) throws Exception {
        runGenerate(context);
    }

    @Command(commandName = GENERATE, humanNameText = "Generate lore entry", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true, descriptionText = GENERATE_TEXT, llmDescriptionText = GENERATE_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "prompt", type = String.class, optional = true, descriptionText = PARAM_PROMPT),
                    @CommandParameter(name = "template", type = String.class, optional = true, descriptionText = PARAM_TEMPLATE),
                    @CommandParameter(name = "lorebook", type = String.class, optional = true, descriptionText = PARAM_LOREBOOK),
                    @CommandParameter(name = "entry", type = String.class, optional = true, descriptionText = PARAM_ENTRY),
                    @CommandParameter(name = "order", type = Integer.class, optional = true, descriptionText = PARAM_ORDER),
                    @CommandParameter(name = "scope", type = String.class, optional = true, defaultValue = "CURRENT_LOREBOOK",
                            allowed = {"CURRENT_LOREBOOK", "ALL_LORE"}, descriptionText = PARAM_SCOPE),
                    @CommandParameter(name = "limit", type = String.class, optional = true, defaultValue = "UP_TO_POSITION",
                            allowed = {"UP_TO_POSITION", "ALL_ENTRIES"}, descriptionText = PARAM_LIMIT),
                    @CommandParameter(name = "images", type = LLMAttachment[].class, optional = true, descriptionText = PARAM_IMAGES)}),
            returns = @CommandReturn(name = "text", type = String.class, descriptionText = RESULT_GENERATE))
    public void generateImages(CommandContext context) throws Exception {
        runGenerate(context);
    }

    private void runGenerate(CommandContext context) throws Exception {
        Lore lore = new Lore(new Book(context));
        Target target = target(lore, ref(context, "lorebook"), ref(context, "entry"), context.get("order", Integer.class));
        Lorebook book = target.book();
        LorebookEntry entry = target.entry();
        LoreScope scope = option(LoreScope.class, context.get("scope", String.class), LoreScope.CURRENT_LOREBOOK, "scope");
        EntryLimit limit = option(EntryLimit.class, context.get("limit", String.class), EntryLimit.UP_TO_POSITION, "limit");

        LoreGeneratorData data = service.getData();
        String templateName = context.get("template", String.class);
        String prompt = context.get("prompt", String.class);
        String name = chosenTemplate(data, templateName, prompt);
        String generationPrompt = name == null ? prompt.strip()
                : StringUtils.isBlank(prompt) ? data.getGenerationPrompts().get(name)
                : data.getGenerationPrompts().get(name).strip() + TASK_HEADER + prompt.strip();
        LLMAttachment[] images = context.get("images", LLMAttachment[].class);
        ModelSelection model = requireModel(context);

        context.setStatus("Generating the text of a lore entry");
        String generated;
        try {
            generated = service.complete(model, service.toPayload(
                    service.buildGenerationPrompt(book, entry, scope, limit, generationPrompt, null),
                    images == null ? List.of() : List.of(images)));
        } catch (CancellationException e) {
            return;
        }
        if (context.isTerminated()) {
            return;
        }
        if (StringUtils.isBlank(generated)) {
            throw new IllegalStateException("The model returned no text");
        }
        context.setReturn(generated);
    }

    // --- compact-lore-entry ---

    @Command(commandName = COMPACT, humanNameText = "Compact lore entry", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true, descriptionText = COMPACT_TEXT, llmDescriptionText = COMPACT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class, descriptionText = PARAM_TEXT)}),
            returns = @CommandReturn(name = "text", type = String.class, descriptionText = RESULT_GENERATE))
    public void compact(CommandContext context) throws Exception {
        runCompact(context);
    }

    @Command(commandName = COMPACT, humanNameText = "Compact lore entry", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true, descriptionText = COMPACT_TEXT, llmDescriptionText = COMPACT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class, descriptionText = PARAM_TEXT),
                    @CommandParameter(name = "lorebook", type = String.class, optional = true, descriptionText = PARAM_LOREBOOK)}),
            returns = @CommandReturn(name = "text", type = String.class, descriptionText = RESULT_GENERATE))
    public void compactLorebook(CommandContext context) throws Exception {
        runCompact(context);
    }

    @Command(commandName = COMPACT, humanNameText = "Compact lore entry", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true, descriptionText = COMPACT_TEXT, llmDescriptionText = COMPACT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class, descriptionText = PARAM_TEXT),
                    @CommandParameter(name = "lorebook", type = String.class, optional = true, descriptionText = PARAM_LOREBOOK),
                    @CommandParameter(name = "entry", type = String.class, optional = true, descriptionText = PARAM_ENTRY)}),
            returns = @CommandReturn(name = "text", type = String.class, descriptionText = RESULT_GENERATE))
    public void compactEntry(CommandContext context) throws Exception {
        runCompact(context);
    }

    @Command(commandName = COMPACT, humanNameText = "Compact lore entry", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true, descriptionText = COMPACT_TEXT, llmDescriptionText = COMPACT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class, descriptionText = PARAM_TEXT),
                    @CommandParameter(name = "lorebook", type = String.class, optional = true, descriptionText = PARAM_LOREBOOK),
                    @CommandParameter(name = "entry", type = String.class, optional = true, descriptionText = PARAM_ENTRY),
                    @CommandParameter(name = "order", type = Integer.class, optional = true, descriptionText = PARAM_ORDER)}),
            returns = @CommandReturn(name = "text", type = String.class, descriptionText = RESULT_GENERATE))
    public void compactOrder(CommandContext context) throws Exception {
        runCompact(context);
    }

    @Command(commandName = COMPACT, humanNameText = "Compact lore entry", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true, descriptionText = COMPACT_TEXT, llmDescriptionText = COMPACT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class, descriptionText = PARAM_TEXT),
                    @CommandParameter(name = "lorebook", type = String.class, optional = true, descriptionText = PARAM_LOREBOOK),
                    @CommandParameter(name = "entry", type = String.class, optional = true, descriptionText = PARAM_ENTRY),
                    @CommandParameter(name = "order", type = Integer.class, optional = true, descriptionText = PARAM_ORDER),
                    @CommandParameter(name = "scope", type = String.class, optional = true, defaultValue = "CURRENT_LOREBOOK",
                            allowed = {"CURRENT_LOREBOOK", "ALL_LORE"}, descriptionText = PARAM_SCOPE)}),
            returns = @CommandReturn(name = "text", type = String.class, descriptionText = RESULT_GENERATE))
    public void compactScope(CommandContext context) throws Exception {
        runCompact(context);
    }

    @Command(commandName = COMPACT, humanNameText = "Compact lore entry", async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, readOnly = true, descriptionText = COMPACT_TEXT, llmDescriptionText = COMPACT_LLM,
            signature = @CommandParameterSignature({
                    @CommandParameter(name = "text", type = String.class, descriptionText = PARAM_TEXT),
                    @CommandParameter(name = "lorebook", type = String.class, optional = true, descriptionText = PARAM_LOREBOOK),
                    @CommandParameter(name = "entry", type = String.class, optional = true, descriptionText = PARAM_ENTRY),
                    @CommandParameter(name = "order", type = Integer.class, optional = true, descriptionText = PARAM_ORDER),
                    @CommandParameter(name = "scope", type = String.class, optional = true, defaultValue = "CURRENT_LOREBOOK",
                            allowed = {"CURRENT_LOREBOOK", "ALL_LORE"}, descriptionText = PARAM_SCOPE),
                    @CommandParameter(name = "limit", type = String.class, optional = true, defaultValue = "UP_TO_POSITION",
                            allowed = {"UP_TO_POSITION", "ALL_ENTRIES"}, descriptionText = PARAM_LIMIT)}),
            returns = @CommandReturn(name = "text", type = String.class, descriptionText = RESULT_GENERATE))
    public void compactLimit(CommandContext context) throws Exception {
        runCompact(context);
    }

    private void runCompact(CommandContext context) throws Exception {
        Lore lore = new Lore(new Book(context));
        Target target = target(lore, ref(context, "lorebook"), ref(context, "entry"), context.get("order", Integer.class));
        Lorebook book = target.book();
        LorebookEntry entry = target.entry();
        LoreScope scope = option(LoreScope.class, context.get("scope", String.class), LoreScope.CURRENT_LOREBOOK, "scope");
        EntryLimit limit = option(EntryLimit.class, context.get("limit", String.class), EntryLimit.UP_TO_POSITION, "limit");

        String source = context.get("text", String.class);
        if (StringUtils.isBlank(source)) {
            throw new IllegalArgumentException("There is nothing to compact: pass the text, for example the result of " + GENERATE);
        }
        ModelSelection model = requireModel(context);

        context.setStatus("Compacting the text of a lore entry");
        String compacted;
        try {
            compacted = service.complete(model, service.toPayload(
                    service.buildCompactionPrompt(book, entry, scope, limit, source)));
        } catch (CancellationException e) {
            return;
        }
        if (context.isTerminated()) {
            return;
        }
        if (StringUtils.isBlank(compacted)) {
            throw new IllegalStateException("The model returned no text");
        }
        context.setReturn(LoreGeneratorService.unfence(compacted));
    }

    // --- helpers ---

    /**
     * The entry the text is for; without a number a new entry with the given order (after all the others when there is
     * none). It is not stored anywhere, it only stands for the place: the model sees the entries before it. Entries with
     * the same order come before it, as the newest one is last.
     */
    private Target target(Lore lore, String lorebook, String entry, Integer order) throws Exception {
        if (entry != null) {
            Lore.Found found = lore.find(lorebook, entry);
            return new Target(found.book(), found.entry());
        }
        LorebookEntry fresh = new LorebookEntry();
        fresh.setOrder(order == null ? Integer.MAX_VALUE : order);
        return new Target(lore.book(lorebook), fresh);
    }

    private record Target(Lorebook book, LorebookEntry entry) {
    }

    /**
     * The model of the scope of the command (the model an agent works with, see {@code CommandScope.getModel}), else the default
     * model of the user.
     */
    private ModelSelection requireModel(CommandContext context) throws Exception {
        CommandScope.ModelChoice scoped = context.getScope().getModel();
        if (scoped != null && scoped.ai() != null) {
            return new ModelSelection(scoped.ai(), scoped.protocol());
        }
        ModelSelection model = service.resolveModel();
        if (model == null) {
            throw new IllegalStateException("The user has no default model, choose one in the user settings");
        }
        return model;
    }

    /**
     * The template the generation starts from: the one asked for, {@code none} (when no template has that name) for
     * none, the one used last when nothing is asked for and there is a prompt. Null means the prompt alone.
     */
    private static String chosenTemplate(LoreGeneratorData data, String requested, String prompt) {
        if (StringUtils.isBlank(requested)) {
            if (StringUtils.isBlank(prompt)) {
                throw new IllegalArgumentException("Give the prompt: what the entry is about, the facts it must contain, its length and "
                        + "its sections (the template of the user is added in front of it)");
            }
            String last = data.getLastGenerationPrompt();
            return last != null && data.getGenerationPrompts().containsKey(last) ? last
                    : data.getGenerationPrompts().keySet().iterator().next();
        }
        if (requested.strip().equalsIgnoreCase(NO_TEMPLATE) && data.getGenerationPrompts().keySet().stream()
                .noneMatch(name -> name.equalsIgnoreCase(NO_TEMPLATE))) {
            if (StringUtils.isBlank(prompt)) {
                throw new IllegalArgumentException("With no template the prompt is all the model gets, give it");
            }
            return null;
        }
        return templateName(data, requested);
    }

    /** The saved prompt called so (the case of the letters does not matter). */
    private static String templateName(LoreGeneratorData data, String requested) {
        for (String name : data.getGenerationPrompts().keySet()) {
            if (name.equalsIgnoreCase(requested.strip())) {
                return name;
            }
        }
        throw new IllegalArgumentException("There is no template \"" + requested.strip() + "\". Templates: "
                + String.join(", ", data.getGenerationPrompts().keySet()));
    }

    private static <E extends Enum<E>> E option(Class<E> type, String text, E fallback, String what) {
        if (StringUtils.isBlank(text)) {
            return fallback;
        }
        String wanted = text.strip().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        for (E constant : type.getEnumConstants()) {
            if (constant.name().equals(wanted)) {
                return constant;
            }
        }
        StringBuilder values = new StringBuilder();
        for (E constant : type.getEnumConstants()) {
            values.append(values.isEmpty() ? "" : ", ").append(constant.name());
        }
        throw new IllegalArgumentException("Unknown " + what + " \"" + text.strip() + "\". Use one of: " + values);
    }

}
