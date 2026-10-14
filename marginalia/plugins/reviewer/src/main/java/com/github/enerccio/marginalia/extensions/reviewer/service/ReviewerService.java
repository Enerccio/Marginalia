package com.github.enerccio.marginalia.extensions.reviewer.service;

import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.reviewer.model.*;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configurable
public class ReviewerService {

    @Autowired
    private Localization loc;

    @Autowired
    private SettingService settingService;

    @Autowired
    private AIService aiService;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    private static final String MANUSCRIPT_PROFILE_KEY = ReviewerSettings.KEY + ".profile";

    private final Gson gson = new Gson();

    public ReviewerSettings getSettings() throws Exception {
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        JsonObject attrs = userSetting.getAttributes();
        if (attrs == null || !attrs.has(ReviewerSettings.KEY)) {
            // books refer to the profiles by uuid, so even the default one has to be stored
            ReviewerSettings settings = createDefault();
            saveSettings(settings, userSetting);
            settingService.save(userSetting);
            return settings;
        }
        JsonElement stored = attrs.get(ReviewerSettings.KEY);
        ReviewerSettings settings = gson.fromJson(stored, ReviewerSettings.class);
        if (hasProfileWithoutUuid(stored)) {
            // saved before the profiles had a uuid: the ones generated on load are kept
            saveSettings(settings, userSetting);
            settingService.save(userSetting);
        }
        return settings;
    }

    private static boolean hasProfileWithoutUuid(JsonElement stored) {
        if (!stored.isJsonObject() || !stored.getAsJsonObject().has("settings")) {
            return false;
        }
        for (Map.Entry<String, JsonElement> profile : stored.getAsJsonObject().getAsJsonObject("settings").entrySet()) {
            if (!profile.getValue().isJsonObject() || !profile.getValue().getAsJsonObject().has("uuid")) {
                return true;
            }
        }
        return false;
    }

    public ReviewerSettings createDefault() {
        ReviewerSettings settings = new ReviewerSettings();
        settings.getSettings().put("_Default", new ReviewerSetting());
        settings.setDefaultSetting("_Default");
        return settings;
    }

    public void saveSettings(ReviewerSettings settings) throws Exception {
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        saveSettings(settings, userSetting);
        settingService.save(userSetting);
    }

    public void saveSettings(ReviewerSettings settings, UserSetting userSetting) {
        JsonObject attrs = userSetting.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            userSetting.setAttributes(attrs);
        }
        if (attrs.has(ReviewerSettings.KEY)) {
            attrs.remove(ReviewerSettings.KEY);
        }
        attrs.add(ReviewerSettings.KEY, gson.toJsonTree(settings));
    }

    /**
     * The profile for the book: the one chosen on the book if it still exists, the globally selected one otherwise.
     */
    public ReviewerSetting resolveSetting(ReviewerSettings settings, Manuscript manuscript) {
        String uuid = getManuscriptProfile(manuscript);
        if (uuid != null) {
            for (ReviewerSetting setting : settings.getSettings().values()) {
                if (uuid.equals(setting.getUuid())) {
                    return setting;
                }
            }
        }
        return settings.getSettings().get(settings.getDefaultSetting());
    }

    public String getManuscriptProfile(Manuscript manuscript) {
        JsonObject attrs = manuscript.getAttributes();
        if (attrs != null && attrs.has(MANUSCRIPT_PROFILE_KEY) && !attrs.get(MANUSCRIPT_PROFILE_KEY).isJsonNull()) {
            return attrs.get(MANUSCRIPT_PROFILE_KEY).getAsString();
        }
        return null;
    }

    /**
     * @param profileUuid uuid of the profile, {@code null} for the globally selected one
     */
    public Manuscript saveManuscriptProfile(Manuscript manuscript, String profileUuid) throws Exception {
        manuscript = manuscriptService.find(manuscript);
        JsonObject attrs = manuscript.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            manuscript.setAttributes(attrs);
        }
        if (profileUuid == null) {
            attrs.remove(MANUSCRIPT_PROFILE_KEY);
        } else {
            attrs.addProperty(MANUSCRIPT_PROFILE_KEY, profileUuid);
        }
        return manuscriptService.save(manuscript);
    }

    public List<ReviewerProfile> getProfiles(ReviewerSettings settings) {
        List<ReviewerProfile> profiles = new ArrayList<>();
        settings.getSettings().forEach((name, setting) -> profiles.add(new ReviewerProfile(setting.getUuid(), name)));
        return profiles;
    }

    public AI resolveAI(ReviewerSettings settings, Manuscript manuscript) throws Exception {
        return resolveAI(resolveSetting(settings, manuscript), manuscript);
    }

    public AI resolveAI(ReviewerSetting setting, Manuscript manuscript) throws Exception {
        if (setting.getSelectedAiId() != null) {
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

    public Protocol resolveProtocol(ReviewerSettings settings, Manuscript manuscript) throws Exception {
        return resolveProtocol(resolveSetting(settings, manuscript), manuscript);
    }

    public Protocol resolveProtocol(ReviewerSetting setting, Manuscript manuscript) throws Exception {
        if (setting.getSelectedProtocolId() != null) {
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

    public ReviewData getReviewData(ChatMessage message) {
        JsonObject attrs = message.getAttributes();
        if (attrs != null && attrs.has(ReviewerSettings.KEY)) {
            return gson.fromJson(attrs.get(ReviewerSettings.KEY), ReviewData.class);
        }
        return null;
    }

    public ChatMessage saveReviewData(ChatMessage message, ReviewData data) throws Exception {
        message = chatMessageService.find(message);
        JsonObject attrs = message.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            message.setAttributes(attrs);
        }
        attrs.add(ReviewerSettings.KEY, gson.toJsonTree(data));
        return chatMessageService.save(message);
    }

    public ChatMessage deleteReviewData(ChatMessage message) throws Exception {
        message = chatMessageService.find(message);
        JsonObject attrs = message.getAttributes();
        if (attrs != null && attrs.has(ReviewerSettings.KEY)) {
            attrs.remove(ReviewerSettings.KEY);
            message = chatMessageService.save(message);
        }
        return message;
    }

    public List<LLMChatMessage> buildChatCompletePrompts(
            InferenceService inferenceService,
            ChatMessage targetMessage,
            ReviewerSetting setting,
            AdvancedOptions advancedOptions) throws Exception {
        List<LLMChatMessage> payload = new ArrayList<>();

        String prePrompt = setting.getReviewPromptPre();
        String reviewPrompt = (advancedOptions != null && StringUtils.isNotBlank(advancedOptions.getPrompt()))
                ? advancedOptions.getPrompt()
                : setting.getReviewPrompt();

        List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(targetMessage);

        if (advancedOptions != null && !advancedOptions.isUsePromptInfo()) {
            int tokenLimit = advancedOptions.getTokenLimit();
            long currentTokens = 0;

            if (StringUtils.isNotBlank(prePrompt)) {
                currentTokens += countTokens(inferenceService, prePrompt);
            }
            if (StringUtils.isNotBlank(reviewPrompt)) {
                currentTokens += countTokens(inferenceService, reviewPrompt);
            }

            // Lorebook background lore attached to target message
            String lore = targetMessage.getBackgroundLore();
            boolean includeLore = advancedOptions.isLorebook() && StringUtils.isNotBlank(lore);
            if (includeLore) {
                currentTokens += countTokens(inferenceService, lore);
            }

            // Iterate backwards through chat branch to fit within tokenLimit
            List<ChatMessage> selectedMessages = new ArrayList<>();
            for (int i = branch.size() - 1; i >= 0; i--) {
                ChatMessage msg = branch.get(i);
                String response = msg.getResponse();
                if (StringUtils.isBlank(response)) continue;

                long msgTokens = msg.getTokenCount() > 0
                        ? msg.getTokenCount()
                        : countTokens(inferenceService, response);

                if (currentTokens + msgTokens > tokenLimit) {
                    break;
                }

                currentTokens += msgTokens;
                selectedMessages.addFirst(msg);
            }

            // Assemble LLMChatMessage list
            if (StringUtils.isNotBlank(prePrompt)) {
                payload.add(LLMChatMessage.of(LLMRole.SYSTEM, prePrompt));
            }
            if (includeLore) {
                payload.add(LLMChatMessage.of(LLMRole.SYSTEM, "Lorebook:\n" + lore));
            }
            for (ChatMessage msg : selectedMessages) {
                payload.add(LLMChatMessage.of(LLMRole.ASSISTANT, msg.getResponse()));
            }
            payload.add(LLMChatMessage.of(LLMRole.USER, reviewPrompt));

        } else {
            // Standard prompt info assembly
            if (StringUtils.isNotBlank(prePrompt)) {
                payload.add(LLMChatMessage.of(LLMRole.SYSTEM, prePrompt));
            }

            String buildPrompt = targetMessage.getBuiltPrompt();
            if (buildPrompt != null) {
                JsonArray originalChain = gson.fromJson(buildPrompt,JsonArray.class);
                for (int i=0; i<originalChain.size() - 1; i++) {
                    JsonElement je = originalChain.get(i);
                    JsonObject jsonObject = je.getAsJsonObject();
                    payload.add(LLMChatMessage.of(LLMRole.valueOf(jsonObject.get("role").getAsString()),
                            jsonObject.get("content").getAsString()));
                }
            }

            payload.add(LLMChatMessage.of(LLMRole.USER, "[ Generate more story ]"));
            payload.add(LLMChatMessage.of(LLMRole.ASSISTANT, targetMessage.getResponse()));
            payload.add(LLMChatMessage.of(LLMRole.USER, reviewPrompt));
        }

        return payload;
    }

    private long countTokens(InferenceService inferenceService, String text) {
        if (StringUtils.isBlank(text)) return 0;
        try {
            if (inferenceService != null) {
                return inferenceService.countTokensApprox(text);
            }
        } catch (Exception ignored) {}
        return text.length() / 4L; // Rough fallback estimate
    }

    public Integer getTokenLimit(ReviewerSettings settings, Manuscript manuscript) throws Exception {
        return getTokenLimit(resolveSetting(settings, manuscript), manuscript);
    }

    public Integer getTokenLimit(ReviewerSetting setting, Manuscript manuscript) throws Exception {
        AI ai = resolveAI(setting, manuscript);
        Protocol protocol = resolveProtocol(setting, manuscript);
        // room for the response is reserved, the prompt gets the rest of the context
        return TokenLimits.promptTokens(ai, protocol);
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

    public ChatMessage refreshMessage(ChatMessage message) throws Exception {
        return chatMessageService.find(message);
    }
}