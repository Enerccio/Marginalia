package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.ImageAttachment;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LorebookEntryVerdict;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateContext;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.GpuGate;
import com.github.enerccio.marginalia.extensions.httphooks.HookRunner;
import com.github.enerccio.marginalia.extensions.imagegenerator.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.imagegenerator.model.ImageSettings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * What the image generator does with the book: keeps the backends of the user, asks the language model for the prompt of a part,
 * makes the sessions of a backend and stores the image the user accepts.
 */
@Configurable
public class ImageGeneratorService {

    @Autowired
    private SettingService settingService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private PartTextRegistry partTexts;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookActivationService activationService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private SummaryService summaryService;

    private final Gson gson = new Gson();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "image-generator-idle");
        t.setDaemon(true);
        return t;
    });

    /** Receives the prompt from the inference thread. */
    public interface PromptListener {
        void onProgress(String text);

        /** @param error null when the prompt is complete, or when it was stopped (cancelled is then true) */
        void onDone(boolean cancelled, Throwable error);
    }

    // --- settings ---

    public ImageSettings getSettings() {
        try {
            JsonObject attrs = settingService.getOrCreate(UserSetting.class).getAttributes();
            if (attrs != null && attrs.has(ImageSettings.KEY)) {
                ImageSettings settings = gson.fromJson(attrs.get(ImageSettings.KEY), ImageSettings.class);
                if (settings != null) {
                    return settings;
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return new ImageSettings();
    }

    public void saveSettings(ImageSettings settings) throws Exception {
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        saveSettings(settings, userSetting);
        settingService.save(userSetting);
    }

    /** Puts the settings into the user settings that are being edited; the caller saves them. */
    public void saveSettings(ImageSettings settings, UserSetting userSetting) {
        JsonObject attrs = userSetting.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            userSetting.setAttributes(attrs);
        }
        attrs.add(ImageSettings.KEY, gson.toJsonTree(settings));
    }

    public String protect(String apiKey) {
        return StringUtils.isBlank(apiKey) ? null : inferenceServices.protectSecret(apiKey);
    }

    /** Opens a text made by {@link #protect}; null when it was protected by another installation. */
    public String reveal(String protectedText) {
        return StringUtils.isBlank(protectedText) ? null : inferenceServices.revealSecret(protectedText);
    }

    /** The api key of the backend, null when there is none or it was protected by another installation. */
    public String reveal(BackendConfig config) {
        return config.getProtectedApiKey() == null ? null : inferenceServices.revealSecret(config.getProtectedApiKey());
    }

    // --- models ---

    public AI resolveAI(ImageSettings settings, Manuscript manuscript) throws Exception {
        if (settings.getSelectedAiId() != null) {
            AI configured = aiService.find(settings.getSelectedAiId());
            if (configured != null && !configured.isDeleted()) {
                return configured;
            }
        }
        return aiService.find(manuscript.getAi());
    }

    public Protocol resolveProtocol(ImageSettings settings, Manuscript manuscript) throws Exception {
        if (settings.getSelectedProtocolId() != null) {
            Protocol configured = protocolService.find(settings.getSelectedProtocolId());
            if (configured != null && !configured.isDeleted()) {
                return configured;
            }
        }
        return protocolService.find(manuscript.getProtocol());
    }

    // --- the prompt ---

    /**
     * The request for the prompt of a part.
     *
     * @param loreEntries how many lorebook entries went in, {@code skippedLore} how many did not fit the context of the model
     */
    public record PromptRequest(List<LLMChatMessage> payload, int loreEntries, int skippedLore) {
    }

    /**
     * The payload that asks the language model for the image prompt of a part.
     *
     * @param withLore whether the lorebook entries the part activates go along
     * @param details  what the user wants in the picture, may be empty
     */
    public PromptRequest buildPromptRequest(ImageSettings settings, Manuscript manuscript, ChatMessage message, AI ai, Protocol protocol,
                                            boolean withLore, String details) throws Exception {
        String scene = sceneText(message, partTexts.forModel(message));
        Lore lore = new Lore("", 0, 0);
        if (withLore) {
            InferenceService service = inferenceServices.forAIOrThrow(ai);
            int limit = TokenLimits.promptTokens(ai, protocol);
            long budget = Long.MAX_VALUE;
            if (limit > 0) {
                List<LLMChatMessage> bare = compose(settings.getPromptInstruction(), "", scene, details);
                long fixed = 0;
                for (LLMChatMessage m : bare) {
                    fixed += service.countTokensApprox(m.getContent());
                }
                budget = Math.max(0, limit - fixed);
            }
            lore = buildLore(manuscript, message, matchedText(message, details, partTexts.forModel(message)), budget, service);
        }
        return new PromptRequest(compose(settings.getPromptInstruction(), lore.text(), scene, details), lore.used(), lore.skipped());
    }

    /** The scene of the part as the prompt writer reads it. */
    static String sceneText(ChatMessage message) {
        return sceneText(message, message.getResponse());
    }

    /** As {@link #sceneText(ChatMessage)}, with the text of the part as a model reads it. */
    static String sceneText(ChatMessage message, String text) {
        StringBuilder scene = new StringBuilder();
        append(scene, "Scene setting", message.getSceneSetting());
        append(scene, "Point of view", message.getPovCharacter());
        append(scene, "Characters present", message.getPresentCharacters());
        append(scene, "Text", text);
        return scene.toString().strip();
    }

    /**
     * What the filters of the lorebook entries are matched against: the text of the part, its instructions, scene setting, point of
     * view and characters present, and the details of the user.
     */
    static String matchedText(ChatMessage message, String details) {
        return matchedText(message, details, message.getResponse());
    }

    /** As {@link #matchedText(ChatMessage, String)}, with the text of the part as a model reads it. */
    static String matchedText(ChatMessage message, String details, String partText) {
        StringBuilder text = new StringBuilder();
        for (String piece : new String[]{partText, message.getInstructions(), message.getSceneSetting(),
                message.getPovCharacter(), message.getPresentCharacters(), details}) {
            if (StringUtils.isNotBlank(piece)) {
                text.append(piece.strip()).append('\n');
            }
        }
        return text.toString();
    }

    static List<LLMChatMessage> compose(String instruction, String lore, String scene, String details) {
        StringBuilder system = new StringBuilder(instruction);
        if (StringUtils.isNotBlank(lore)) {
            system.append("\n\n===== LOREBOOK START =====\n\n").append(lore.strip()).append("\n\n===== LOREBOOK END =====");
        }
        StringBuilder user = new StringBuilder(scene);
        if (StringUtils.isNotBlank(details)) {
            user.append("\n\nDetails the picture must follow:\n").append(details.strip());
        }
        List<LLMChatMessage> payload = new ArrayList<>();
        payload.add(LLMChatMessage.of(LLMRole.SYSTEM, system.toString()));
        payload.add(LLMChatMessage.of(LLMRole.USER, user.toString().strip()));
        return payload;
    }

    private static void append(StringBuilder to, String label, String value) {
        if (StringUtils.isNotBlank(value)) {
            to.append(label).append(":\n").append(value.strip()).append("\n\n");
        }
    }

    // --- lore ---

    private record Lore(String text, int used, int skipped) {
    }

    /**
     * The lore the part activates, like a generation: the lorebook of the book and its sub lorebooks, the entries whose filters are
     * found in the text, rendered as templates, in the order they are used. Entries are added until the budget is spent.
     */
    private Lore buildLore(Manuscript manuscript, ChatMessage part, String matchedText, long budget, InferenceService service) throws Exception {
        Manuscript current = manuscriptService.find(manuscript);
        Lorebook root = current != null && current.getLorebook() != null ? lorebookService.find(current.getLorebook()) : null;
        if (root == null) {
            return new Lore("", 0, 0);
        }
        List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(part);
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
     * Template data for the entries, set up like a quiet generation after the part (the last one of the branch). The context is a
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
            String text = partTexts.forModel(message);
            if (StringUtils.isNotBlank(text)) {
                story.add(text);
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

    /** Streams the prompt; the listener is called from the inference thread. */
    public CancellationToken streamPrompt(AI ai, Protocol protocol, List<LLMChatMessage> payload, PromptListener listener) throws Exception {
        InferenceService service = inferenceServices.forAIOrThrow(ai);
        CancellationToken[] token = new CancellationToken[1];
        InferenceService.InferenceAsyncCallback callback = new InferenceService.InferenceAsyncCallback() {
            private final StringBuilder text = new StringBuilder();

            @Override
            public void onChunk(InferenceService.InferenceAsyncController controller, InferenceService.ChunkType chunkType, String chunk) {
                if (chunkType != InferenceService.ChunkType.REASONING) {
                    text.append(chunk);
                    listener.onProgress(text.toString());
                }
                controller.continueInference();
            }

            @Override
            public void onCompletion() {
                listener.onDone(false, null);
            }

            @Override
            public void onCancel() {
                listener.onDone(true, null);
            }

            @Override
            public void onError(Throwable exception) {
                listener.onDone(false, exception);
            }

            @Override
            public boolean isDead() {
                return token[0] != null && token[0].isCancelled();
            }
        };
        try (InferenceSession _ = InferenceSession.open("image-generator")) {
            token[0] = service.stream(payload, protocol, callback);
        }
        return token[0];
    }

    /** The prompt as it goes to the backend: the style of the user in front. */
    public String withStyle(ImageSettings settings, String prompt) {
        String prefix = settings.getStylePrefix().strip();
        String text = StringUtils.defaultString(prompt).strip();
        return prefix.isEmpty() ? text : prefix + (prefix.endsWith(",") ? " " : ", ") + text;
    }

    // --- the backend ---

    public ImageBackend createBackend(BackendConfig config) {
        String key = reveal(config);
        return switch (config.getType()) {
            case COMFYUI -> new ComfyUiBackend(config, key);
            case OPENAI -> new OpenAiImagesBackend(config, key);
        };
    }

    /**
     * A session of the backend; {@code onIdleEnd} is told when the idle time ended it. The sessions of everyone who calls the same
     * server (or GPU name, whatever extension) go through one {@link GpuGate}: the hooks run for the first and the last of them, and the images are made one at a time.
     */
    public ImageSession createSession(BackendConfig config, Consumer<String> onIdleEnd) {
        UnaryOperator<String> reveal = this::reveal;
        return new ImageSession(createBackend(config),
                // before: in order, the first failure stops the image (the next request must not load a model on a full GPU)
                () -> HookRunner.runAll(config.getBeforeHooks(), config.isLocal(), reveal),
                // after: every request is made, also when one failed (every container must be stopped); the first failure is reported
                () -> {
                    String error = HookRunner.runAllRegardless(config.getAfterHooks(), config.isLocal(), reveal);
                    if (error != null) {
                        throw new BackendException(error);
                    }
                },
                config.getIdleSeconds(), scheduler, onIdleEnd, config.gate(), config.gateMode());
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }

    // --- the result ---

    /**
     * Stores the image as a resource of the part and attaches it.
     *
     * @return the part as saved
     * @throws IllegalArgumentException when the bytes are not a supported image or are too big
     */
    public ChatMessage attach(ChatMessage message, byte[] image, String filename) throws Exception {
        Resource resource = resourceService.uploadImage(filename, image, ChatMessage.class, message.getId());
        return chatMessageService.update(message, current -> {
            List<ImageAttachment> images = current.getImages();
            images.add(new ImageAttachment(resource.getUuid(), ""));
            current.setImages(images);
        });
    }

    /** The new seed of a try. */
    public static long newSeed() {
        return java.util.concurrent.ThreadLocalRandom.current().nextLong(0, 1L << 53);
    }
}
