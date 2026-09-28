package com.github.enerccio.marginalia.extensions.reviewer.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.reviewer.model.AdvancedOptions;
import com.github.enerccio.marginalia.extensions.reviewer.model.ReviewData;
import com.github.enerccio.marginalia.extensions.reviewer.model.ReviewerSettings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ReviewerService {

    private final Gson gson = new Gson();

    public ReviewerSettings getSettings(SettingService settingService) throws Exception {
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        JsonObject attrs = userSetting.getAttributes();
        if (attrs != null && attrs.has(ReviewerSettings.KEY)) {
            return gson.fromJson(attrs.get(ReviewerSettings.KEY), ReviewerSettings.class);
        }
        return new ReviewerSettings();
    }

    public void saveSettings(SettingService settingService, ReviewerSettings settings) throws Exception {
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        JsonObject attrs = userSetting.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            userSetting.setAttributes(attrs);
        }
        attrs.add(ReviewerSettings.KEY, gson.toJsonTree(settings));
        settingService.save(userSetting);
    }

    public AI resolveAI(ReviewerSettings settings, Manuscript manuscript, AIService aiService) {
        if (settings != null && settings.getSelectedAiId() != null) {
            try {
                AI configuredAi = aiService.find(settings.getSelectedAiId());
                if (configuredAi != null && !configuredAi.isDeleted()) {
                    return configuredAi;
                }
            } catch (Exception ignored) {}
        }
        return manuscript != null ? manuscript.getAi() : null;
    }

    public Protocol resolveProtocol(ReviewerSettings settings, Manuscript manuscript, ProtocolService protocolService) {
        if (settings != null && settings.getSelectedProtocolId() != null) {
            try {
                Protocol configuredProtocol = protocolService.find(settings.getSelectedProtocolId());
                if (configuredProtocol != null && !configuredProtocol.isDeleted()) {
                    return configuredProtocol;
                }
            } catch (Exception ignored) {}
        }
        return manuscript != null ? manuscript.getProtocol() : null;
    }

    public ReviewData getReviewData(ChatMessage message) {
        JsonObject attrs = message.getAttributes();
        if (attrs != null && attrs.has(ReviewerSettings.KEY)) {
            return gson.fromJson(attrs.get(ReviewerSettings.KEY), ReviewData.class);
        }
        return null;
    }

    public void saveReviewData(ChatMessageService chatMessageService, ChatMessage message, ReviewData data) throws Exception {
        JsonObject attrs = message.getAttributes();
        if (attrs == null) {
            attrs = new JsonObject();
            message.setAttributes(attrs);
        }
        attrs.add(ReviewerSettings.KEY, gson.toJsonTree(data));
        chatMessageService.save(message);
    }

    public void deleteReviewData(ChatMessageService chatMessageService, ChatMessage message) throws Exception {
        JsonObject attrs = message.getAttributes();
        if (attrs != null && attrs.has(ReviewerSettings.KEY)) {
            attrs.remove(ReviewerSettings.KEY);
            chatMessageService.save(message);
        }
    }

    public List<LLMChatMessage> buildChatCompletePrompts(
            ChatMessage targetMessage,
            ChatMessageService chatMessageService,
            InferenceService inferenceService,
            ReviewerSettings settings,
            AdvancedOptions advancedOptions) throws Exception {

        List<LLMChatMessage> payload = new ArrayList<>();

        String prePrompt = settings.getReviewPromptPre();
        String reviewPrompt = (advancedOptions != null && StringUtils.isNotBlank(advancedOptions.getPrompt()))
                ? advancedOptions.getPrompt()
                : settings.getReviewPrompt();

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
                selectedMessages.add(0, msg);
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

    public Integer getTokenLimit(ReviewerSettings settings, Manuscript manuscript, AIService aiService, ProtocolService protocolService) {
        AI ai = resolveAI(settings, manuscript, aiService);
        Protocol protocol = resolveProtocol(settings, manuscript, protocolService);
        if (protocol.getMaxTokens() != null) {
            return protocol.getMaxTokens();
        }
        return ai.getMaxContext();
    }
}