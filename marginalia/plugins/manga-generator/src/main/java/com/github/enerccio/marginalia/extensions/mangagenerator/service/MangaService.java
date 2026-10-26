package com.github.enerccio.marginalia.extensions.mangagenerator.service;

import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LorebookEntryVerdict;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateContext;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.Manga;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.MangaData;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.MangaSection;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.MangaSettings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.enerccio.marginalia.extensions.mangagenerator.MangaGeneratorLocalization.T;

/**
 * What the manga generator does with the book: keeps its data and the defaults of the user, builds the requests (the
 * story text of the section, the lore its text activates, the previous script) and streams the answer.
 * <p>
 * Parts of the story are numbered from 1 like in the outline, the parts of the active branch.
 */
@Configurable
public class MangaService {

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
    private LorebookActivationService activationService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private InferenceServices inferenceServices;

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_]+)\\s*}}");

    private final Gson gson = new Gson();

    /**
     * A request ready to be sent, with the model it goes to and how it fits the context of the model.
     *
     * @param promptTokens approximate size of the payload
     * @param promptLimit  the room the model has for the prompt (its context minus the room for the reply), 0 when it is not known
     * @param loreEntries  how many lorebook entries went in, {@code skippedLore} how many did not fit
     */
    public record Request(List<LLMChatMessage> payload, AI ai, Protocol protocol, InferenceService service,
                          long promptTokens, int promptLimit, int loreEntries, int skippedLore) {

        public boolean exceedsContext() {
            return promptLimit > 0 && promptTokens > promptLimit;
        }

        /** The payload as one text, to show to the user. */
        public String asText() {
            StringBuilder text = new StringBuilder();
            for (LLMChatMessage message : payload) {
                text.append("--- ").append(message.getRole()).append(" ---\n").append(message.getContent()).append("\n\n");
            }
            return text.toString().strip();
        }
    }

    public enum Outcome {
        COMPLETED, CANCELLED, FAILED
    }

    /** Receives the answer, from the inference thread. */
    public interface StreamListener {

        /** Called after every chunk with everything received so far. */
        void onProgress(String text, String reasoning, long reasoningMillis, boolean reasoningFinished);

        /** Called once, when the answer is complete, stopped or failed. */
        void onDone(Outcome outcome, Throwable error);
    }

    // --- settings and data ---

    public MangaSettings getSettings() {
        try {
            JsonObject attrs = settingService.getOrCreate(UserSetting.class).getAttributes();
            if (attrs != null && attrs.has(MangaSettings.KEY)) {
                return gson.fromJson(attrs.get(MangaSettings.KEY), MangaSettings.class);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return new MangaSettings();
    }

    public void saveSettings(MangaSettings settings) throws Exception {
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        saveSettings(settings, userSetting);
        settingService.save(userSetting);
    }

    /**
     * Puts the settings into the user settings that are being edited; the caller saves them.
     */
    public void saveSettings(MangaSettings settings, UserSetting userSetting) {
        JsonObject attrs = userSetting.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            userSetting.setAttributes(attrs);
        }
        attrs.add(MangaSettings.KEY, gson.toJsonTree(settings));
    }

    /**
     * The data of the book as it is stored; the book is read again, the one a story tab holds may be older than the last save.
     */
    public MangaData loadData(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        JsonObject attrs = current == null ? null : current.getAttributes();
        if (attrs != null && attrs.has(MangaSettings.KEY)) {
            MangaData data = gson.fromJson(attrs.get(MangaSettings.KEY), MangaData.class);
            if (data != null) {
                return data;
            }
        }
        return new MangaData();
    }

    public void saveData(Manuscript manuscript, MangaData data) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        JsonObject attrs = current.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            current.setAttributes(attrs);
        }
        attrs.add(MangaSettings.KEY, gson.toJsonTree(data));
        manuscriptService.save(current);
    }

    // --- models ---

    public AI resolveAI(MangaSettings settings, Manuscript manuscript) throws Exception {
        if (settings.getSelectedAiId() != null) {
            AI configured = aiService.find(settings.getSelectedAiId());
            if (configured != null && !configured.isDeleted()) {
                return configured;
            }
        }
        return aiService.find(manuscript.getAi());
    }

    public Protocol resolveProtocol(MangaSettings settings, Manuscript manuscript) throws Exception {
        if (settings.getSelectedProtocolId() != null) {
            Protocol configured = protocolService.find(settings.getSelectedProtocolId());
            if (configured != null && !configured.isDeleted()) {
                return configured;
            }
        }
        return protocolService.find(manuscript.getProtocol());
    }

    public List<AI> getAvailableAis() throws Exception {
        return aiService.findAllForUser();
    }

    public List<Protocol> getAvailableProtocols() throws Exception {
        return protocolService.findAllForUser();
    }

    public AI findAi(Long id) throws Exception {
        return id == null ? null : aiService.find(id);
    }

    public Protocol findProtocol(Long id) throws Exception {
        return id == null ? null : protocolService.find(id);
    }

    // --- the story ---

    /**
     * The parts of the active branch, from the first.
     */
    public List<ChatMessage> branch(Manuscript manuscript) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        if (current == null || current.getActiveLeaf() == null) {
            return List.of();
        }
        ChatMessage leaf = chatMessageService.find(current.getActiveLeaf());
        return leaf == null ? List.of() : chatMessageService.getBranchFromLeaf(leaf);
    }

    /**
     * The text of parts {@code from} to {@code to} (from 1, cut to the story), each under its number; parts with no text are left out.
     */
    static String partsText(List<ChatMessage> branch, int from, int to) {
        StringBuilder text = new StringBuilder();
        for (int i = Math.max(1, from); i <= Math.min(branch.size(), to); i++) {
            String response = branch.get(i - 1).getResponse();
            if (StringUtils.isNotBlank(response)) {
                text.append("[Part ").append(i).append("]\n").append(response.strip()).append("\n\n");
            }
        }
        return text.toString().strip();
    }

    // --- requests ---

    /**
     * The request that makes the script of a section.
     *
     * @param previous the section before it, whose script the model continues; may be null
     */
    public Request buildScriptRequest(Manuscript manuscript, Manga manga, MangaSection section, MangaSection previous) throws Exception {
        MangaSettings settings = getSettings();
        List<ChatMessage> branch = branch(manuscript);
        String source = partsText(branch, section.getContextStart(), section.getContextEnd());
        if (source.isEmpty()) {
            throw new IllegalStateException(T.localized("The story has no text in the parts of this section"));
        }

        String userPrompt = compile(StringUtils.defaultIfBlank(
                        StringUtils.defaultIfBlank(section.getPromptOverride(), manga.getPromptOverride()), Defaults.DEFAULT_PROMPT),
                manga, rules(manga, section, true));
        String boundary = boundaryRules(section);
        if (!boundary.isEmpty()) {
            userPrompt += "\n\n" + boundary;
        }

        StringBuilder after = new StringBuilder();
        after.append(block(Defaults.MANUSCRIPT, source));
        if (previous != null && StringUtils.isNotBlank(previous.getScript())) {
            after.append("\n\n").append(block(Defaults.PREVIOUS_SCRIPT, previous.getScript().strip()));
        }
        return assemble(manuscript, branch, settings, source, after.toString(), userPrompt);
    }

    /**
     * The request that makes the image tags of a section from its script.
     */
    public Request buildTagsRequest(Manuscript manuscript, Manga manga, MangaSection section) throws Exception {
        MangaSettings settings = getSettings();
        if (StringUtils.isBlank(section.getScript())) {
            throw new IllegalStateException(T.localized("Generate or write the script of the section first"));
        }
        List<ChatMessage> branch = branch(manuscript);
        String source = partsText(branch, section.getContextStart(), section.getContextEnd());

        String userPrompt = compile(StringUtils.defaultIfBlank(
                        StringUtils.defaultIfBlank(section.getTagsPromptOverride(), manga.getTagsPromptOverride()), Defaults.DEFAULT_TAGS_PROMPT),
                manga, rules(manga, section, false));
        String after = block(Defaults.MANUSCRIPT, source) + "\n\n" + block(Defaults.SCRIPT, section.getScript().strip());
        return assemble(manuscript, branch, settings, source, after, userPrompt);
    }

    /**
     * System message (initial query, lore, {@code after}) and the prompt as the user message. The lore is what the story
     * text activates, as many entries as fit into the context of the model next to the rest of the request.
     */
    private Request assemble(Manuscript manuscript, List<ChatMessage> branch, MangaSettings settings, String matchedText,
                             String after, String userPrompt) throws Exception {
        AI ai = resolveAI(settings, manuscript);
        Protocol protocol = resolveProtocol(settings, manuscript);
        InferenceService service = inferenceServices.forAI(ai);
        int limit = TokenLimits.promptTokens(ai, protocol);

        String head = settings.getInitialQuery().strip();
        String fixed = (head.isEmpty() ? "" : head + "\n\n") + after;
        long budget = Long.MAX_VALUE;
        if (limit > 0) {
            budget = Math.max(0, limit - service.countTokensApprox(fixed) - service.countTokensApprox(userPrompt));
        }
        Lore lore = buildLore(manuscript, branch, matchedText, budget, service);

        StringBuilder system = new StringBuilder();
        if (!head.isEmpty()) {
            system.append(head).append("\n\n");
        }
        if (!lore.text().isEmpty()) {
            system.append(block(Defaults.LORE, lore.text())).append("\n\n");
        }
        system.append(after);

        List<LLMChatMessage> payload = new ArrayList<>();
        payload.add(LLMChatMessage.of(LLMRole.SYSTEM, system.toString()));
        payload.add(LLMChatMessage.of(LLMRole.USER, userPrompt));
        long tokens = 0;
        for (LLMChatMessage message : payload) {
            tokens += service.countTokensApprox(message.getContent());
        }
        return new Request(payload, ai, protocol, service, tokens, limit, lore.used(), lore.skipped());
    }

    private static String block(String name, String content) {
        return "===== " + name + " START =====\n\n" + content + "\n\n===== " + name + " END =====";
    }

    /**
     * Tells the model which of the parts it gets are the ones to adapt, when the section has more as the context.
     */
    static String boundaryRules(MangaSection section) {
        if (section.getContextStart() >= section.getStartPart() && section.getContextEnd() <= section.getEndPart()) {
            return "";
        }
        StringBuilder rules = new StringBuilder("[Content Boundary Rules]\n");
        rules.append("Adapt only the parts ").append(section.getStartPart()).append(" to ").append(section.getEndPart())
                .append(" of the ").append(Defaults.MANUSCRIPT).append(".");
        if (section.getContextStart() < section.getStartPart()) {
            rules.append(" The parts before them are only the context: what happened earlier.");
        }
        if (section.getContextEnd() > section.getEndPart()) {
            rules.append(" The parts after them are only the context: what comes next. Do not adapt them; the last part to adapt is part ")
                    .append(section.getEndPart()).append(".");
        }
        return rules.toString();
    }

    private static String rules(Manga manga, MangaSection section, boolean withAddendum) {
        String rules = StringUtils.defaultString(StringUtils.defaultIfBlank(section.getAdditionalRules(), manga.getAdditionalRules()));
        if (withAddendum && StringUtils.isNotBlank(section.getAdditionalRulesAddendum())) {
            rules += (rules.isEmpty() ? "" : "\n") + section.getAdditionalRulesAddendum();
        }
        return rules;
    }

    /**
     * Replaces {@code {{tokenBudget}}} and {@code {{additionalRules}}}; any other text in double braces stays as it is.
     */
    static String compile(String prompt, Manga manga, String rules) {
        Matcher matcher = PLACEHOLDER.matcher(prompt);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String value = switch (matcher.group(1)) {
                case "tokenBudget" -> String.valueOf(manga.getTokenBudget());
                case "additionalRules" -> rules;
                default -> matcher.group();
            };
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    // --- lore ---

    private record Lore(String text, int used, int skipped) {
    }

    /**
     * The lore the text activates, like the generation: the lorebook of the book and its sub lorebooks, the entries whose filters
     * are found in the text (and the ones the scan depth brings in), rendered as templates, in the order they are used. Entries are added
     * until the budget is spent; the rest are left out.
     */
    private Lore buildLore(Manuscript manuscript, List<ChatMessage> branch, String matchedText, long budget,
                           InferenceService service) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        Lorebook root = current.getLorebook() != null ? lorebookService.find(current.getLorebook()) : null;
        if (root == null) {
            return new Lore("", 0, 0);
        }
        List<Lorebook> books = activationService.collectLorebooks(root);
        List<String> tags = tagRelationService.getTagsForObject(current).stream().map(Tag::getValue).toList();
        LorebookTemplateData data = createTemplateData(current, branch);

        StringBuilder lore = new StringBuilder();
        long used = 0;
        int count = 0;
        int skipped = 0;
        for (LorebookEntryVerdict verdict : activationService.evaluateAll(root, books, tags, matchedText)) {
            if (!verdict.verdict().activated()) {
                continue;
            }
            String payload = activationService.renderPayload(verdict.entry(), data);
            if (StringUtils.isBlank(payload)) {
                continue;
            }
            String entry = "[" + verdict.entry().getName() + "]:\n" + payload.strip();
            long tokens = service.countTokensApprox(entry);
            if (skipped > 0 || used + tokens > budget) {
                // the entries come in the order of their importance: once one does not fit, the later ones stay out too
                skipped++;
                continue;
            }
            lore.append(entry).append("\n\n");
            used += tokens;
            count++;
        }
        return new Lore(lore.toString().strip(), count, skipped);
    }

    /**
     * Template data for lorebook entries, set up like a quiet generation after the last part of the active branch. The context is a
     * throw-away copy - variables set by the entries are not stored.
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

    // --- inference ---

    /**
     * Streams the answer to the request. The listener is called from the inference thread.
     */
    public CancellationToken stream(Request request, StreamListener listener) throws Exception {
        CancellationToken[] token = new CancellationToken[1];
        token[0] = request.service().stream(request.payload(), request.protocol(), new InferenceService.InferenceAsyncCallback() {
            private final StringBuilder text = new StringBuilder();
            private final StringBuilder reasoning = new StringBuilder();
            private long reasoningStart;
            private long reasoningMillis;
            private boolean reasoningFinished;

            @Override
            public void onChunk(InferenceService.InferenceAsyncController controller, InferenceService.ChunkType chunkType, String chunk) {
                if (chunkType == InferenceService.ChunkType.REASONING) {
                    if (reasoningStart == 0) {
                        reasoningStart = System.nanoTime();
                    }
                    reasoning.append(chunk);
                    reasoningMillis = (System.nanoTime() - reasoningStart) / 1_000_000;
                } else {
                    // the answer starts when the thinking is over
                    reasoningFinished = reasoningStart != 0;
                    text.append(chunk);
                }
                listener.onProgress(text.toString(), reasoning.toString(), reasoningMillis, reasoningFinished);
                controller.continueInference();
            }

            @Override
            public void onCompletion() {
                reasoningFinished = reasoningStart != 0;
                listener.onProgress(text.toString(), reasoning.toString(), reasoningMillis, reasoningFinished);
                listener.onDone(Outcome.COMPLETED, null);
            }

            @Override
            public void onCancel() {
                listener.onDone(Outcome.CANCELLED, null);
            }

            @Override
            public void onError(Throwable exception) {
                listener.onDone(Outcome.FAILED, exception);
            }

            @Override
            public boolean isDead() {
                return token[0] != null && token[0].isCancelled();
            }
        });
        return token[0];
    }
}
