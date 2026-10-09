package com.github.enerccio.marginalia.extensions.sidequery.service;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateContext;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables;
import com.github.enerccio.marginalia.extensions.sidequery.model.*;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.function.Consumer;

@Configurable
public class SideQueryService {

    @Autowired
    private Localization loc;

    @Autowired
    private SettingService settingService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private InferenceServices inferenceServices;

    private static final Logger log = LoggerFactory.getLogger(SideQueryService.class);

    private static final String TAB_NAME_PROMPT = "Give the following conversation a short title of at most five "
            + "words. Reply with the title only, without quotes or punctuation at the end.";

    private final Gson gson = new Gson();

    public SideQuerySettings getSettings() throws Exception {
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        JsonObject attrs = userSetting.getAttributes();
        SideQuerySettings settings = createDefault();
        if (attrs != null && attrs.has(SideQuerySettings.KEY)) {
            settings = gson.fromJson(attrs.get(SideQuerySettings.KEY), SideQuerySettings.class);
        }
        return settings;
    }

    public SideQuerySettings createDefault() {
        SideQuerySettings settings = new SideQuerySettings();
        settings.getSettings().put("_Default", new SideQuerySetting());
        settings.setDefaultSetting("_Default");
        return settings;
    }

    public void saveSettings(SideQuerySettings settings) throws Exception {
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        saveSettings(settings, userSetting);
        settingService.save(userSetting);
    }

    public void saveSettings(SideQuerySettings settings, UserSetting userSetting) {
        JsonObject attrs = userSetting.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            userSetting.setAttributes(attrs);
        }
        if (attrs.has(SideQuerySettings.KEY)) {
            attrs.remove(SideQuerySettings.KEY);
        }
        attrs.add(SideQuerySettings.KEY, gson.toJsonTree(settings));
    }

    public TreeMap<String, String> getSavedQueries() {
        try {
            return getSettings().getSavedQueries();
        } catch (Exception e) {
            return new TreeMap<>();
        }
    }

    public void saveQueryTemplate(String name, String content) throws Exception {
        SideQuerySettings settings = getSettings();
        settings.getSavedQueries().put(name, content);
        saveSettings(settings);
    }

    public void deleteQueryTemplate(String name) throws Exception {
        SideQuerySettings settings = getSettings();
        settings.getSavedQueries().remove(name);
        saveSettings(settings);
    }

    public AI resolveAI(SideQuerySettings settings, Manuscript manuscript) throws Exception {
        return resolveAI(settings.getSettings().get(settings.getDefaultSetting()), manuscript);
    }

    public AI resolveAI(SideQuerySetting setting, Manuscript manuscript) throws Exception {
        if (setting != null && setting.getSelectedAiId() != null) {
            try {
                AI configuredAi = aiService.find(setting.getSelectedAiId());
                if (configuredAi != null && !configuredAi.isDeleted()) {
                    return configuredAi;
                }
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }
        return aiService.find(manuscript.getAi());
    }

    public Protocol resolveProtocol(SideQuerySettings settings, Manuscript manuscript) throws Exception {
        return resolveProtocol(settings.getSettings().get(settings.getDefaultSetting()), manuscript);
    }

    public Protocol resolveProtocol(SideQuerySetting setting, Manuscript manuscript) throws Exception {
        if (setting != null && setting.getSelectedProtocolId() != null) {
            try {
                Protocol configuredProtocol = protocolService.find(setting.getSelectedProtocolId());
                if (configuredProtocol != null && !configuredProtocol.isDeleted()) {
                    return configuredProtocol;
                }
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }
        return protocolService.find(manuscript.getProtocol());
    }

    public SideQueryData getSideQueryData(Manuscript manuscript) {
        JsonObject attrs = manuscript.getAttributes();
        if (attrs != null && attrs.has(SideQuerySettings.KEY)) {
            return gson.fromJson(attrs.get(SideQuerySettings.KEY), SideQueryData.class);
        }
        return new SideQueryData();
    }

    public void saveSideQueryData(Manuscript manuscript, SideQueryData data) throws Exception {
        manuscript = manuscriptService.find(manuscript);
        JsonObject attrs = manuscript.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            manuscript.setAttributes(attrs);
        }
        attrs.add(SideQuerySettings.KEY, gson.toJsonTree(data));
        manuscriptService.save(manuscript);
    }

    public List<LLMChatMessage> buildPromptPayload(
            Manuscript manuscript,
            SideQuerySetting setting,
            SideQuerySession session) throws Exception {

        List<LLMChatMessage> payload = new ArrayList<>();
        StringBuilder systemBuilder = new StringBuilder();

        if (StringUtils.isNotBlank(setting.getInitialQuery())) {
            systemBuilder.append(setting.getInitialQuery()).append("\n\n");
        }

        SideQueryOptions opts = session.getOptions();
        Manuscript current = manuscriptService.find(manuscript);
        if (current == null) {
            current = manuscript;
        }
        ChatMessage leaf = current.getActiveLeaf() != null ? chatMessageService.find(current.getActiveLeaf()) : null;
        List<ChatMessage> branch = leaf != null ? chatMessageService.getBranchFromLeaf(leaf) : List.of();

        if (opts.isIncludeLorebook()) {
            String lore = buildLore(current, branch);
            if (StringUtils.isNotBlank(lore)) {
                systemBuilder.append("Lorebook Context:\n").append(lore).append("\n\n");
            }
        }

        if (opts.isIncludeMessages() && !branch.isEmpty()) {
            // parts are numbered from 1, like in the outline
            int from = Math.max(1, opts.getPartsFrom());
            int to = Math.min(branch.size(), opts.getPartsTo());

            if (from <= to) {
                StringBuilder logBuilder = new StringBuilder("Chat History Context:\n");
                for (int i = from; i <= to; i++) {
                    ChatMessage msg = branch.get(i - 1);
                    if (StringUtils.isNotBlank(msg.getResponse())) {
                        logBuilder.append("Message #").append(i).append(":\n")
                                .append(msg.getResponse()).append("\n\n");
                    }
                }
                systemBuilder.append(logBuilder.toString().trim()).append("\n\n");
            }
        }

        String fullSystemPrompt = systemBuilder.toString().trim();
        if (StringUtils.isNotBlank(fullSystemPrompt)) {
            payload.add(LLMChatMessage.of(LLMRole.SYSTEM, fullSystemPrompt));
        }

        int lastUserIndex = -1;
        for (SideQueryMessage msg : session.getMessages()) {
            if (msg.isIncluded() && StringUtils.isNotBlank(msg.getContents())) {
                LLMRole role = msg.isFromUser() ? LLMRole.USER : LLMRole.ASSISTANT;
                if (msg.isFromUser()) {
                    lastUserIndex = payload.size();
                }
                payload.add(LLMChatMessage.of(role, msg.getContents()));
            }
        }

        if (StringUtils.isNotBlank(setting.getInstructionsBeforeUser())) {
            if (lastUserIndex >= 0) {
                // instructions go right before the user's (last) question
                String question = payload.get(lastUserIndex).getContent();
                payload.set(lastUserIndex, LLMChatMessage.of(LLMRole.USER,
                        setting.getInstructionsBeforeUser().trim() + "\n\n" + question));
            } else {
                payload.add(LLMChatMessage.of(LLMRole.USER, setting.getInstructionsBeforeUser().trim()));
            }
        }

        return payload;
    }

    /**
     * Lore of the book like the generation collects it: the book's lorebook and its sub lorebooks (disabled ones are
     * skipped), enabled entries whose tags match the book, entry payloads rendered as templates. Keyword filtering is
     * not applied - there is no story prompt to match, the side query gets all applicable lore.
     */
    private String buildLore(Manuscript manuscript, List<ChatMessage> branch) throws Exception {
        Lorebook root = manuscript.getLorebook() != null ? lorebookService.find(manuscript.getLorebook()) : null;
        if (root == null) {
            return "";
        }

        List<Lorebook> lorebooks = new ArrayList<>();
        Set<Long> processed = new HashSet<>();
        Queue<Lorebook> toProcess = new LinkedList<>();
        toProcess.add(root);
        while (!toProcess.isEmpty()) {
            Lorebook l = toProcess.remove();
            if (l.isDeleted() || !l.isEnabled() || !processed.add(l.getId())) {
                continue;
            }
            lorebooks.add(l);
            toProcess.addAll(lorebookService.getSubbooks(l));
        }

        Map<Integer, List<LorebookEntry>> byOrder = new TreeMap<>();
        for (Lorebook l : lorebooks) {
            l = lorebookService.fillEntries(l);
            // cached tags are set on the entities, don't leave them in the persistence context
            lorebookService.evict(l);
            for (LorebookEntry entry : l.getCachedEntries()) {
                lorebookEntryService.evict(entry);
                byOrder.computeIfAbsent(entry.getOrder(), _ -> new ArrayList<>()).add(entry);
            }
        }

        Set<String> bookTags = new HashSet<>();
        for (Tag tag : tagRelationService.getTagsForObject(manuscript)) {
            bookTags.add(tag.getValue());
        }

        LorebookTemplateData templateData = createTemplateData(manuscript, branch);
        StringBuilder loreBuilder = new StringBuilder();
        for (List<LorebookEntry> entries : byOrder.values()) {
            entries.sort(Comparator.comparing(LorebookEntry::getCreation, Comparator.nullsLast(Comparator.naturalOrder())));
            for (LorebookEntry entry : entries) {
                if (!entry.isEnabled() || StringUtils.isBlank(entry.getPayload())) {
                    continue;
                }
                if (!Collections.disjoint(entry.getCachedNegativeTags(), bookTags)) {
                    continue;
                }
                if (!entry.getCachedTags().isEmpty() && Collections.disjoint(entry.getCachedTags(), bookTags)) {
                    continue;
                }
                String content = renderEntry(entry, templateData);
                if (StringUtils.isNotBlank(content)) {
                    loreBuilder.append("[").append(entry.getName()).append("]:\n")
                            .append(content.trim()).append("\n\n");
                }
            }
        }
        return loreBuilder.toString().trim();
    }

    private String renderEntry(LorebookEntry entry, LorebookTemplateData templateData) {
        try {
            return templateService.processTemplate(entry.getPayload(), "lorebookEntry", templateData);
        } catch (Exception e) {
            // a broken entry must not break the side query, use it as it is
            log.warn("Failed to process template of lorebook entry '{}': {}", entry.getName(), e.getMessage());
            return entry.getPayload();
        }
    }

    /**
     * Template data for lorebook entries, set up like a quiet generation after the last part of the active branch.
     * The context is a throw-away copy - variables set by the entries are not stored.
     */
    private LorebookTemplateData createTemplateData(Manuscript manuscript, List<ChatMessage> branch) throws Exception {
        TemplateContext context = new TemplateContext();
        ChatMessage last = branch.isEmpty() ? null : branch.getLast();
        if (last != null) {
            context.setPovCharacter(last.getPovCharacter());
            context.setPresentCharacters(last.getPresentCharacters());
            context.setSceneSetting(last.getSceneSetting());
            context.setInstructions(last.getInstructions());
            context.setLastInstructions(last.getInstructions());
            context.setLastMessageTime(last.getRequest());
        }
        String pov = manuscriptService.getPov(manuscript);
        String tense = manuscriptService.getTense(manuscript);
        String style = manuscriptService.getStyle(manuscript);
        context.setNarrativePov(pov);
        context.setNarrativeTense(tense);
        context.setStyle(style);
        context.setManuscriptName(manuscript.getName());
        context.setManuscriptDescription(manuscript.getDescription());
        context.setPickSeed(String.valueOf(manuscript.getId()));
        context.setGenerationType("quiet");

        List<String> story = new ArrayList<>();
        for (ChatMessage message : branch) {
            if (StringUtils.isNotBlank(message.getResponse())) {
                story.add(message.getResponse());
            }
        }
        context.setStoryMessages(story);
        for (ChatMessage message : branch.reversed()) {
            if (message.getSummary() != null) {
                Summary summary = summaryService.find(message.getSummary());
                if (summary != null) {
                    context.setLatestSummary(summary.getSummary());
                }
                break;
            }
        }
        if (last != null) {
            context.getVariables().loadFrom(TemplateVariables.Scope.LOCAL, last.getAttributes());
        }
        context.getVariables().loadFrom(TemplateVariables.Scope.GLOBAL, manuscript.getAttributes());

        LorebookTemplateData templateData = new LorebookTemplateData();
        if (last != null) {
            templateData.setPovCharacter(last.getPovCharacter());
            templateData.setSceneSetting(last.getSceneSetting());
            templateData.setPresentCharacters(last.getPresentCharacters());
            templateData.setInstructions(last.getInstructions());
        }
        templateData.setNarrativePov(pov);
        templateData.setNarrativeTense(tense);
        templateData.setStyle(style);
        templateData.setTemplateContext(context);
        return templateData;
    }

    /**
     * Whether the tab should get a name from the model: AI tab naming is enabled in the profile, the user didn't
     * rename the tab and it has not been named yet.
     */
    public boolean shouldNameTab(SideQuerySetting setting, SideQuerySession session) {
        return setting != null && setting.isEnableAiTabNames()
                && !session.isManuallyRenamed() && !session.isAutoNamed()
                && session.getMessages().stream().anyMatch(m -> m.isFromUser() && StringUtils.isNotBlank(m.getContents()))
                && session.getMessages().stream().anyMatch(m -> !m.isFromUser() && StringUtils.isNotBlank(m.getContents()));
    }

    /**
     * Asks the model for a short name of the conversation (first question and answer). {@code onName} is called from
     * the inference thread with the cleaned up name, not called when the model fails or returns nothing.
     */
    public CancellationToken generateTabName(Manuscript manuscript, SideQuerySetting setting, SideQuerySession session,
                                             Consumer<String> onName) throws Exception {
        SideQueryMessage question = session.getMessages().stream()
                .filter(m -> m.isFromUser() && StringUtils.isNotBlank(m.getContents())).findFirst().orElse(null);
        SideQueryMessage answer = session.getMessages().stream()
                .filter(m -> !m.isFromUser() && StringUtils.isNotBlank(m.getContents())).findFirst().orElse(null);
        if (question == null || answer == null) {
            return null;
        }

        List<LLMChatMessage> payload = new ArrayList<>();
        payload.add(LLMChatMessage.of(LLMRole.SYSTEM, TAB_NAME_PROMPT));
        payload.add(LLMChatMessage.of(LLMRole.USER, "Question:\n" + StringUtils.abbreviate(question.getContents(), 2000)
                + "\n\nAnswer:\n" + StringUtils.abbreviate(answer.getContents(), 2000)));

        AI ai = resolveAI(setting, manuscript);
        Protocol protocol = resolveProtocol(setting, manuscript);
        InferenceService service = inferenceServices.forAI(ai);
        CancellationToken[] token = new CancellationToken[1];
        token[0] = service.stream(payload, protocol, new InferenceService.InferenceAsyncCallback() {
            private final StringBuilder name = new StringBuilder();

            @Override
            public void onChunk(InferenceService.InferenceAsyncController controller, InferenceService.ChunkType chunkType, String text) {
                if (chunkType == InferenceService.ChunkType.RESPONSE) {
                    name.append(text);
                }
                if (name.length() > 500) {
                    controller.terminateInference();
                } else {
                    controller.continueInference();
                }
            }

            @Override
            public void onCompletion() {
                String cleaned = cleanTabName(name.toString());
                if (StringUtils.isNotBlank(cleaned)) {
                    onName.accept(cleaned);
                }
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(Throwable exception) {
                log.warn("Failed to name side query tab: {}", exception.getMessage());
            }

            @Override
            public boolean isDead() {
                return token[0] != null && token[0].isCancelled();
            }
        });
        return token[0];
    }

    static String cleanTabName(String text) {
        if (text == null) {
            return "";
        }
        String line = text.strip().lines().map(String::strip).filter(StringUtils::isNotBlank).findFirst().orElse("");
        line = StringUtils.strip(line, "\"'*#`.: ");
        return StringUtils.abbreviate(line, 40);
    }

    public List<AI> getAvailableAiModels() throws Exception {
        return aiService.findAllForUser();
    }

    public AI findAi(Long id) throws Exception {
        return id != null ? aiService.find(id) : null;
    }

    public List<Protocol> getAvailableProtocols() throws Exception {
        return protocolService.findAllForUser();
    }

    public Protocol findProtocol(Long id) throws Exception {
        return id != null ? protocolService.find(id) : null;
    }
}