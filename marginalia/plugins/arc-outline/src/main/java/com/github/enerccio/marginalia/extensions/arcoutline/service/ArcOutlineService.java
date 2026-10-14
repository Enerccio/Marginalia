package com.github.enerccio.marginalia.extensions.arcoutline.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.extensions.arcoutline.model.*;
import com.github.enerccio.marginalia.extensions.arcoutline.names.NameGeneratorBridge;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

@Configurable
public class ArcOutlineService {

    /** The model and the protocol the generation runs with, taken from the user settings. */
    public record ModelSelection(AI ai, Protocol protocol) {
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
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InferenceServices inferenceServices;

    /** Set when the extension loads, to find the name generator extension. */
    private OsgiService osgiService;

    private final Gson gson = new Gson();

    public void setLogger(Logger log) {
        this.log = log;
    }

    public void setOsgiService(OsgiService osgiService) {
        this.osgiService = osgiService;
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
        return settings != null ? settings : new ArcOutlineSettings();
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

    // --- name tools ---

    /**
     * The categories of the name generator extension, null when it is not installed (it is looked up on every call,
     * it can be loaded and unloaded while the application runs).
     */
    public List<String> nameCategories() {
        return osgiService == null ? null : NameGeneratorBridge.categories(osgiService);
    }

    /**
     * The functions that give the model names of the selected categories, null when none of them is available
     * (nothing selected, or the extension is gone).
     */
    public FunctionSet nameFunctions(Collection<String> selected) {
        if (selected == null || selected.isEmpty()) {
            return null;
        }
        return osgiService == null ? null
                : NameGeneratorBridge.functions(osgiService, inferenceServices.getFunctionRegistry(), selected);
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
     * The JSON schema the model is asked to answer in.
     */
    public JsonNode schema() {
        return StructuredOutputs.schemaOf(GeneratedArc.class);
    }

    /**
     * The prompt for generating an arc: the template with the lore of the book, its arcs and the specification put in,
     * followed by the hint about the name tools (the user's or {@link Defaults#NAME_TOOLS_PROMPT}) when the model has
     * them.
     *
     * @param template      the prompt template, with {{backgroundLore}}, {{existingArcs}} and {{specification}}
     * @param specification what the author wants from the arc
     * @throws Exception when the template is not valid
     */
    public String buildPrompt(Manuscript manuscript, ArcOutline outline, String template, String specification,
                              boolean nameTools) throws Exception {
        ArcGenerationTemplateData data = new ArcGenerationTemplateData();
        data.setBackgroundLore(buildBackgroundLore(manuscript));
        data.setExistingArcs(describeArcs(outline));
        data.setSpecification(StringUtils.defaultIfBlank(specification, "(nothing in particular, decide yourself)").strip());

        String prompt = templateService.processTemplate(template, "arcGeneration", data);
        if (nameTools) {
            prompt += "\n\n" + StringUtils.defaultIfBlank(getSettings().getNameToolsPrompt(), Defaults.NAME_TOOLS_PROMPT).strip();
        }
        return prompt;
    }

    /**
     * The JSON schema the model is asked to answer in when it writes chapters.
     *
     * @param exactCount the number of chapters the author wants, null when the model decides (at least one)
     */
    public JsonNode chaptersSchema(Integer exactCount) {
        ObjectNode schema = StructuredOutputs.schemaOf(GeneratedChapters.class);
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
        return schema;
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
                                     String chapterSpecification, boolean nameTools) throws Exception {
        ChapterGenerationTemplateData data = new ChapterGenerationTemplateData();
        data.setBackgroundLore(buildBackgroundLore(manuscript));
        data.setOutline(describeOutline(outline, arc, context));
        data.setNumberOfChapters(numberOfChapters);
        data.setGeneralSpecification(StringUtils.defaultIfBlank(generalSpecification, "(nothing in particular, decide yourself)").strip());
        data.setChapterSpecification(chapterSpecification);

        String prompt = templateService.processTemplate(template, "chapterGeneration", data);
        if (nameTools) {
            prompt += "\n\n" + StringUtils.defaultIfBlank(getSettings().getNameToolsPrompt(), Defaults.NAME_TOOLS_PROMPT).strip();
        }
        return prompt;
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
