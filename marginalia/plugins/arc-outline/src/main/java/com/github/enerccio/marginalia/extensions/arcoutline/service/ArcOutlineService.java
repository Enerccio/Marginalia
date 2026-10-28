package com.github.enerccio.marginalia.extensions.arcoutline.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.structured.SchemaCheck;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs;
import com.github.enerccio.marginalia.domain.service.tool.ToolRequest;
import com.github.enerccio.marginalia.domain.service.tool.ToolSelection;
import com.github.enerccio.marginalia.domain.service.tool.ToolSet;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.extensions.arcoutline.model.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.concurrent.*;

@Configurable
public class ArcOutlineService {

    /** The model and the protocol the generation runs with, taken from the user settings. */
    public record ModelSelection(AI ai, Protocol protocol) {
    }

    /**
     * A message of the active branch that has text, for the import of an arc.
     *
     * @param number the place among the messages with text, from 1
     * @param text   the text of the message
     * @param tokens the approximate number of tokens of the text
     */
    public record StoryMessage(int number, String text, long tokens) {
    }

    /**
     * What a blocking generation returned.
     *
     * @param value  the answer, read into the class asked for
     * @param answer the answer of the model as it came (JSON)
     */
    public record Generated<T>(T value, String answer) {
    }

    /** The logger of the extension (Admin → Extensions → Log), set when the extension loads. */
    private Logger log;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private TokenizerService tokenizerService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private ToolService toolService;

    /** Set when the extension loads, to tell the tool providers who asks. */
    private Bundle bundle;

    private final Gson gson = new Gson();

    /** Who wants to know that the outline of a book was changed from outside of its view, by the uuid of the book. */
    private final Map<String, Set<Runnable>> changeListeners = new ConcurrentHashMap<>();

    public void setLogger(Logger log) {
        this.log = log;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    // --- the outline of a book ---

    /**
     * The outline of the book as it is stored now - the story view keeps the book it was opened with, the outline
     * may have been saved since.
     */
    public ArcOutline load(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        return read(current != null ? current : manuscript);
    }

    public ArcOutline read(Manuscript manuscript) {
        JsonObject attrs = manuscript.getAttributes();
        ArcOutlineData data = null;
        if (attrs != null && attrs.has(ArcOutlineData.KEY)) {
            data = gson.fromJson(attrs.get(ArcOutlineData.KEY), ArcOutlineData.class);
        }
        return new ArcOutline(data);
    }

    /**
     * The outline of the book as text, for other extensions (see {@code IPCService}): the arcs with their states and
     * descriptions and their chapters.
     *
     * @param withNotes also the notes of the arcs and chapters (the full version, the description is the terse one)
     * @return null when the book has no arcs
     */
    public String outlineText(Manuscript manuscript, boolean withNotes) throws Exception {
        ArcOutline outline = load(manuscript);
        return outline.arcs().isEmpty() ? null : describe(outline, null, ChapterContext.EVERYTHING, withNotes);
    }

    /**
     * The data of the chapter the story is at (see {@link ArcOutline#current()}), for the model that drafts the instructions of the next
     * turn: its arc and the chapter with their descriptions and notes, and the title and description of the chapter after it so that
     * the turn does not run ahead.
     *
     * @return null when the book has no current chapter
     */
    public String currentChapterText(Manuscript manuscript) throws Exception {
        return currentChapterText(load(manuscript));
    }

    public static String currentChapterText(ArcOutline outline) {
        ArcOutline.Current current = outline.current();
        if (current == null) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        text.append("Arc: ").append(heading(current.arc())).append('\n');
        appendDescription(text, current.arc(), "   ", true);
        if (current.chapter() != null) {
            text.append("\nCurrent chapter: ").append(heading(current.chapter())).append('\n');
            appendDescription(text, current.chapter(), "   ", true);
        }
        if (current.next() != null) {
            text.append("\nNext chapter (the story must not get there yet): ").append(heading(current.next())).append('\n');
            appendDescription(text, current.next(), "   ", false);
        }
        return text.toString().stripTrailing();
    }

    public void save(Manuscript manuscript, ArcOutline outline) throws Exception {
        manuscript = manuscriptService.find(manuscript);
        JsonObject attrs = manuscript.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            manuscript.setAttributes(attrs);
        }
        attrs.add(ArcOutlineData.KEY, gson.toJsonTree(outline.toData()));
        manuscriptService.save(manuscript);
    }

    /**
     * Tells {@code listener} whenever {@link #changed} is called for the book, from the thread of the caller. For a view
     * that keeps the outline in memory and has to read it again when a command changed it.
     *
     * @return what removes the listener
     */
    public Runnable onChange(Manuscript manuscript, Runnable listener) {
        String key = manuscript.getUuid();
        changeListeners.computeIfAbsent(key, _ -> new CopyOnWriteArraySet<>()).add(listener);
        return () -> changeListeners.computeIfPresent(key, (_, set) -> {
            set.remove(listener);
            return set.isEmpty() ? null : set;
        });
    }

    /**
     * Tells the views of the book that its outline was stored by someone else than the view (a command).
     */
    public void changed(Manuscript manuscript) {
        Set<Runnable> listeners = changeListeners.get(manuscript.getUuid());
        if (listeners != null) {
            listeners.forEach(listener -> {
                try {
                    listener.run();
                } catch (RuntimeException e) {
                    if (log != null) {
                        log.warn("A view of the outline failed to refresh: {}", e.getMessage());
                    }
                }
            });
        }
    }

    // --- settings of the user ---

    /**
     * Settings of the current user.
     */
    public ArcOutlineSettings getSettings() throws Exception {
        return getSettings(settingService.getOrCreate(UserSetting.class));
    }

    public ArcOutlineSettings getSettings(UserSetting userSetting) {
        JsonObject attrs = userSetting.getAttributes();
        ArcOutlineSettings settings = null;
        if (attrs != null && attrs.has(ArcOutlineSettings.KEY)) {
            settings = gson.fromJson(attrs.get(ArcOutlineSettings.KEY), ArcOutlineSettings.class);
        }
        if (settings == null) {
            settings = new ArcOutlineSettings();
        }
        settings.migrateLegacyNameTools();
        if (settings.getTools() == null) {
            settings.setTools(new ToolSelection());
        }
        return settings;
    }

    public void saveSettings(ArcOutlineSettings settings) throws Exception {
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        saveSettings(settings, userSetting);
        settingService.save(userSetting);
    }

    /**
     * Puts the settings into {@code userSetting}, saving the setting is up to the caller.
     */
    public void saveSettings(ArcOutlineSettings settings, UserSetting userSetting) {
        JsonObject attrs = userSetting.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            userSetting.setAttributes(attrs);
        }
        attrs.add(ArcOutlineSettings.KEY, gson.toJsonTree(settings));
    }

    /**
     * The prompt template the dialog starts with: the user's or {@link Defaults#GENERATION_PROMPT}.
     */
    public String generationPrompt(ArcOutlineSettings settings) {
        return StringUtils.defaultIfBlank(settings.getGenerationPrompt(), Defaults.GENERATION_PROMPT);
    }

    /**
     * The prompt template the chapter dialog starts with: the user's or {@link Defaults#CHAPTER_GENERATION_PROMPT}.
     */
    public String chapterGenerationPrompt(ArcOutlineSettings settings) {
        return StringUtils.defaultIfBlank(settings.getChapterGenerationPrompt(), Defaults.CHAPTER_GENERATION_PROMPT);
    }

    /**
     * The prompt template the import dialog starts with: the user's or {@link Defaults#IMPORT_PROMPT}.
     */
    public String importPrompt(ArcOutlineSettings settings) {
        return StringUtils.defaultIfBlank(settings.getImportPrompt(), Defaults.IMPORT_PROMPT);
    }

    // --- tools ---

    /**
     * The functions and the hints of the tools the selection turns on (the suites that are installed, the items that are
     * still offered); {@link ToolSet#NONE} when there are none.
     *
     * @throws IllegalStateException when two suites have a function of the same name
     */
    public ToolSet tools(ToolSelection selection) {
        return toolService.resolve(selection, new ToolRequest(bundle, ArcOutlineService.class.getName()));
    }

    // --- generation ---

    /**
     * The model and protocol the generation starts with: those of the book, and when the book has no model the
     * defaults of the user. The protocol is optional.
     *
     * @return null when neither the book nor the user has a model
     */
    public ModelSelection resolveModel(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        AI ai = (current != null ? current : manuscript).getAi();
        if (ai != null && !ai.isDeleted()) {
            Protocol protocol = (current != null ? current : manuscript).getProtocol();
            return new ModelSelection(ai, protocol != null && !protocol.isDeleted() ? protocol : null);
        }
        return resolveModel();
    }

    /**
     * The default model and protocol of the user. The protocol is optional.
     *
     * @return null when the user has no default model
     */
    public ModelSelection resolveModel() throws Exception {
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        AI ai = userSetting.getDefaultModel() != null ? aiService.find(userSetting.getDefaultModel()) : null;
        if (ai == null || ai.isDeleted()) {
            return null;
        }
        Protocol protocol = userSetting.getDefaultProtocol() != null ? protocolService.find(userSetting.getDefaultProtocol()) : null;
        if (protocol != null && protocol.isDeleted()) {
            protocol = null;
        }
        return new ModelSelection(ai, protocol);
    }

    /**
     * Asks the model for an answer in the schema and waits for it (the dialogs stream it; a command has no one to show
     * it to).
     *
     * @param type the class the answer is read into, the one the schema was made of
     * @throws CancellationException when the request was cancelled
     * @throws IllegalStateException when the answer does not match the schema
     */
    public <T> Generated<T> generate(ModelSelection model, String prompt, JsonNode schema, Class<T> type) throws Exception {
        record Answer(String text, SchemaCheck check) {
        }
        CompletableFuture<Answer> answer = new CompletableFuture<>();
        CancellationToken token;
        try (InferenceSession _ = InferenceSession.open("arc-outline")) {
            token = inferenceServices.forAIOrThrow(model.ai()).infer(toPayload(prompt), model.protocol(), schema,
                new InferenceService.InferenceResultCallback() {
                    @Override
                    public void onResult(String text, SchemaCheck check) {
                        answer.complete(new Answer(text, check));
                    }

                    @Override
                    public void onCancel() {
                        answer.cancel(false);
                    }

                    @Override
                    public void onError(Throwable exception) {
                        answer.completeExceptionally(exception);
                    }

                    @Override
                    public boolean isDead() {
                        return answer.isDone();
                    }
                });
        }
        Answer result;
        try {
            result = answer.get();
        } catch (InterruptedException e) {
            token.cancel();
            Thread.currentThread().interrupt();
            throw new CancellationException("The request was stopped");
        } catch (ExecutionException e) {
            throw e.getCause() instanceof Exception cause ? cause : e;
        }
        SchemaCheck check = StructuredOutputs.validate(schema, result.text());
        if (!check.valid()) {
            throw new IllegalStateException("The model's answer does not match what was asked for: " + String.join("; ", check.errors()));
        }
        return new Generated<>(StructuredOutputs.parse(result.text(), type), result.text());
    }

    /**
     * The JSON schema the model is asked to answer in.
     */
    public JsonNode schema() {
        return StructuredOutputs.schemaOf(GeneratedArc.class);
    }

    /**
     * The prompt for generating an arc: the template with the lore of the book, its arcs and the specification put in,
     * followed by the hints of the tools the model has (see {@link #withToolHints}).
     *
     * @param template      the prompt template, with {{backgroundLore}}, {{existingArcs}} and {{specification}}
     * @param specification what the author wants from the arc
     * @param tools         what the model gets, null for none
     * @throws Exception when the template is not valid
     */
    public String buildPrompt(Manuscript manuscript, ArcOutline outline, String template, String specification,
                              ToolSet tools) throws Exception {
        ArcGenerationTemplateData data = new ArcGenerationTemplateData();
        data.setBackgroundLore(buildBackgroundLore(manuscript));
        data.setExistingArcs(describeArcs(outline));
        data.setSpecification(StringUtils.defaultIfBlank(specification, "(nothing in particular, decide yourself)").strip());

        return withToolHints(templateService.processTemplate(template, "arcGeneration", data), tools);
    }

    /**
     * The prompt followed by the hints of the tools the model has (the user's or the default one of each suite).
     *
     * @param tools what the model gets, null for none
     */
    private String withToolHints(String prompt, ToolSet tools) {
        return tools != null && !tools.isEmpty() && StringUtils.isNotBlank(tools.hint()) ? prompt + "\n\n" + tools.hint() : prompt;
    }

    /**
     * The JSON schema the model is asked to answer in when it writes chapters.
     *
     * @param exactCount the number of chapters the author wants, null when the model decides (at least one)
     */
    public JsonNode chaptersSchema(Integer exactCount) {
        ObjectNode schema = StructuredOutputs.schemaOf(GeneratedChapters.class);
        limitChapters(schema, exactCount);
        return schema;
    }

    /**
     * The JSON schema the model is asked to answer in when it turns a part of the story into an arc with chapters.
     *
     * @param exactCount the number of chapters the author wants, null when the model decides (at least one)
     */
    public JsonNode importSchema(Integer exactCount) {
        ObjectNode schema = StructuredOutputs.schemaOf(GeneratedStoryArc.class);
        limitChapters(schema, exactCount);
        return schema;
    }

    /** Fixes the list of chapters of the schema and limits its length to {@code exactCount} when there is one. */
    private static void limitChapters(ObjectNode schema, Integer exactCount) {
        JsonNode chapters = schema.path("properties").path("chapters");
        if (chapters instanceof ObjectNode list) {
            // the constraints of the list field are also put on its items as an allOf, which some providers can't
            // take (response formats are turned into grammars): keep the schema of the item only
            JsonNode allOf = list.path("items").path("allOf");
            if (allOf.isArray() && !allOf.isEmpty()) {
                list.set("items", allOf.get(0));
            }
            if (exactCount != null) {
                list.put("minItems", exactCount);
                list.put("maxItems", exactCount);
            }
        }
    }

    /**
     * The prompt for generating chapters of {@code arc}, built like {@link #buildPrompt}.
     *
     * @param context              how much of the outline is sent
     * @param numberOfChapters     how many chapters the author wants, in words
     * @param generalSpecification what the author wants from the chapters in general
     * @param chapterSpecification the numbered list of what the author wants from each chapter
     * @throws Exception when the template is not valid
     */
    public String buildChapterPrompt(Manuscript manuscript, ArcOutline outline, Arc arc, ChapterContext context,
                                     String template, String numberOfChapters, String generalSpecification,
                                     String chapterSpecification, ToolSet tools) throws Exception {
        ChapterGenerationTemplateData data = new ChapterGenerationTemplateData();
        data.setBackgroundLore(buildBackgroundLore(manuscript));
        data.setOutline(describeOutline(outline, arc, context));
        data.setNumberOfChapters(numberOfChapters);
        data.setGeneralSpecification(StringUtils.defaultIfBlank(generalSpecification, "(nothing in particular, decide yourself)").strip());
        data.setChapterSpecification(chapterSpecification);

        return withToolHints(templateService.processTemplate(template, "chapterGeneration", data), tools);
    }

    /**
     * The prompt for importing an arc from the story, built like {@link #buildPrompt} (the name tools make no sense
     * here, the names are in the story).
     *
     * @param template         the prompt template, with {{backgroundLore}}, {{existingArcs}}, {{story}} and
     *                         {{numberOfChapters}}
     * @param messages         the messages to summarize, in the order they are read
     * @param numberOfChapters how many chapters the author wants, null when the model decides
     * @throws Exception when the template is not valid
     */
    public String buildImportPrompt(Manuscript manuscript, ArcOutline outline, String template,
                                    List<StoryMessage> messages, Integer numberOfChapters) throws Exception {
        ImportGenerationTemplateData data = new ImportGenerationTemplateData();
        data.setBackgroundLore(buildBackgroundLore(manuscript));
        data.setExistingArcs(describeArcs(outline));
        data.setStory(storyText(messages));
        data.setNumberOfChapters(numberOfChapters != null ? "exactly " + numberOfChapters
                : "as many as the story needs, divide it where it naturally divides");
        return templateService.processTemplate(template, "arcImport", data);
    }

    /** The messages as the prompt shows them, each under a heading with its number. */
    public static String storyText(List<StoryMessage> messages) {
        StringBuilder text = new StringBuilder();
        for (StoryMessage message : messages) {
            text.append("--- Message ").append(message.number()).append(" ---\n")
                    .append(message.text().strip()).append("\n\n");
        }
        return text.toString().stripTrailing();
    }

    /**
     * The messages with text of the active branch of the book as it is stored now (the story view keeps the book it
     * was opened with, the branch may have changed), from the first to the last.
     *
     * @return empty when the book has no active branch
     */
    public List<StoryMessage> activeBranch(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        ChatMessage leaf = (current != null ? current : manuscript).getActiveLeaf();
        if (leaf == null) {
            return List.of();
        }
        List<StoryMessage> messages = new ArrayList<>();
        for (ChatMessage message : chatMessageService.getBranchFromLeaf(leaf)) {
            String text = message.getResponse();
            if (StringUtils.isNotBlank(text)) {
                long tokens = message.getTokenCount() > 0 ? message.getTokenCount() : countTokens(text);
                messages.add(new StoryMessage(messages.size() + 1, text, tokens));
            }
        }
        return messages;
    }

    /** The approximate number of tokens of the text. */
    public long countTokens(String text) {
        if (StringUtils.isEmpty(text)) {
            return 0;
        }
        try {
            return tokenizerService.countTokensApprox(text);
        } catch (Exception e) {
            // a rough estimate does for a counter
            return text.length() / 4;
        }
    }

    /** The prompt as the system message, with a short user turn some providers require. */
    public List<LLMChatMessage> toPayload(String systemPrompt) {
        List<LLMChatMessage> payload = new ArrayList<>();
        payload.add(LLMChatMessage.of(LLMRole.SYSTEM, systemPrompt));
        payload.add(LLMChatMessage.of(LLMRole.USER, "[ Start ]"));
        return payload;
    }

    /**
     * The arcs of the outline for the prompt: a numbered list with the state and description of each arc and its
     * chapters. The private notes are left out.
     */
    public String describeArcs(ArcOutline outline) {
        return describe(outline, null, ChapterContext.EVERYTHING, false);
    }

    /** Marks the arc the chapters are written for in the outline sent to the model. */
    public static final String TARGET_MARKER = "<<< WRITE THE CHAPTERS FOR THIS ARC";

    /**
     * The outline for writing the chapters of {@code target}: the arcs up to and including it (later ones are not
     * sent), the target marked with {@link #TARGET_MARKER}, with the chapters {@code context} says.
     */
    public String describeOutline(ArcOutline outline, Arc target, ChapterContext context) {
        return describe(outline, target, context, false);
    }

    private String describe(ArcOutline outline, Arc target, ChapterContext context, boolean withNotes) {
        if (outline.arcs().isEmpty()) {
            return "(none yet)";
        }
        StringBuilder text = new StringBuilder();
        int number = 1;
        for (Arc arc : outline.arcs()) {
            text.append(number++).append(". ").append(heading(arc));
            if (arc == target) {
                text.append("  ").append(TARGET_MARKER);
            }
            text.append('\n');
            appendDescription(text, arc, "   ", withNotes);
            int chapterNumber = 0;
            for (Chapter chapter : arc.getChapters()) {
                // a chapter keeps its number when the ones before it are left out
                chapterNumber++;
                if (context == ChapterContext.ARCS_ONLY
                        || context == ChapterContext.ARCS_AND_FINISHED && chapter.getState() != OutlineState.FINISHED) {
                    continue;
                }
                text.append("   ").append(chapterNumber).append(") ").append(heading(chapter)).append('\n');
                appendDescription(text, chapter, "      ", withNotes);
            }
            if (arc == target) {
                // later arcs are not part of what the chapters are written against
                break;
            }
        }
        return text.toString().stripTrailing();
    }

    private static String heading(OutlineNode node) {
        return StringUtils.defaultIfBlank(node.getTitle(), "(untitled)") + " [" + node.getState().getLabel() + "]";
    }

    private static void appendDescription(StringBuilder text, OutlineNode node, String indent, boolean withNotes) {
        if (StringUtils.isNotBlank(node.getDescription())) {
            node.getDescription().strip().lines().forEach(line -> text.append(indent).append(line).append('\n'));
        }
        if (withNotes && StringUtils.isNotBlank(node.getNote())) {
            text.append(indent).append("Notes:\n");
            node.getNote().strip().lines().forEach(line -> text.append(indent).append("  ").append(line).append('\n'));
        }
    }

    /**
     * The entries of the book's lorebook and of its sub lorebooks (deleted ones are skipped) as the story would see
     * them, sorted by their order. Empty when the book has no lorebook. Which entries are used does not depend on
     * their enabled flags, tags or filtering.
     */
    public String buildBackgroundLore(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        if (current == null) {
            current = manuscript;
        }
        Lorebook root = current.getLorebook() != null ? lorebookService.find(current.getLorebook()) : null;
        if (root == null) {
            return "(no lorebook)";
        }

        List<LorebookEntry> entries = new ArrayList<>();
        Set<Long> processed = new HashSet<>();
        Queue<Lorebook> toProcess = new ArrayDeque<>();
        toProcess.add(root);
        while (!toProcess.isEmpty()) {
            Lorebook lorebook = toProcess.remove();
            if (lorebook.isDeleted() || !processed.add(lorebook.getId())) {
                continue;
            }
            lorebookEntryService.getEntriesForLorebook(lorebook).stream()
                    .filter(entry -> !entry.isDeleted()).forEach(entries::add);
            toProcess.addAll(lorebookService.getSubbooks(lorebook));
        }
        // a stable sort
        entries.sort(Comparator.comparingInt(LorebookEntry::getOrder));

        // one template data for all entries, like the generation does: variables set in an entry are visible in the next ones
        LorebookTemplateData templateData = new LorebookTemplateData();
        StringBuilder lore = new StringBuilder();
        for (LorebookEntry entry : entries) {
            String payload = render(entry, templateData);
            if (StringUtils.isNotBlank(payload)) {
                lore.append(payload.trim()).append("\n\n");
            }
        }
        return lore.length() == 0 ? "(the lorebook is empty)" : lore.toString().stripTrailing();
    }

    private String render(LorebookEntry entry, LorebookTemplateData templateData) {
        String payload = entry.getPayload();
        if (StringUtils.isBlank(payload)) {
            return payload;
        }
        try {
            return templateService.processTemplate(payload, "lorebookEntry", templateData);
        } catch (Exception e) {
            // a broken entry must not break the generation, use it as it is (the same as the story generation does)
            if (log != null) {
                log.warn("Failed to process template of lorebook entry '{}': {}", entry.getName(), e.getMessage());
            }
            return payload;
        }
    }
}
