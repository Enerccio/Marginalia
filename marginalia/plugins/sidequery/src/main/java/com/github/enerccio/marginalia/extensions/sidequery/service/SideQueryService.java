package com.github.enerccio.marginalia.extensions.sidequery.service;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.sidequery.model.*;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

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

        if (opts.isIncludeLorebook()) {
            Lorebook book = manuscript.getLorebook();
            if (book != null) {
                StringBuilder loreBuilder = new StringBuilder();
                List<LorebookEntry> entries = lorebookEntryService.getEntriesForLorebook(book);
                for (LorebookEntry entry : entries) {
                    if (entry.isEnabled() && StringUtils.isNotBlank(entry.getPayload())) {
                        loreBuilder.append("[").append(entry.getName()).append("]:\n")
                                .append(entry.getPayload()).append("\n\n");
                    }
                }
                if (!loreBuilder.isEmpty()) {
                    systemBuilder.append("Lorebook Context:\n").append(loreBuilder.toString().trim()).append("\n\n");
                }
            }
        }

        if (opts.isIncludeMessages() && manuscript.getActiveLeaf() != null) {
            List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(manuscript.getActiveLeaf());
            int from = Math.max(0, opts.getMessagesFrom());
            int to = Math.min(branch.size() - 1, opts.getMessagesTo());

            if (from <= to && from < branch.size()) {
                StringBuilder logBuilder = new StringBuilder("Chat History Context:\n");
                for (int i = from; i <= to; i++) {
                    ChatMessage msg = branch.get(i);
                    if (StringUtils.isNotBlank(msg.getResponse())) {
                        logBuilder.append("Message #").append(i + 1).append(":\n")
                                .append(msg.getResponse()).append("\n\n");
                    }
                }
                systemBuilder.append(logBuilder.toString().trim()).append("\n\n");
            }
        }

        if (StringUtils.isNotBlank(setting.getInstructionsBeforeUser())) {
            systemBuilder.append(setting.getInstructionsBeforeUser()).append("\n\n");
        }

        String fullSystemPrompt = systemBuilder.toString().trim();
        if (StringUtils.isNotBlank(fullSystemPrompt)) {
            payload.add(LLMChatMessage.of(LLMRole.SYSTEM, fullSystemPrompt));
        }

        for (SideQueryMessage msg : session.getMessages()) {
            if (msg.isIncluded() && StringUtils.isNotBlank(msg.getContents())) {
                LLMRole role = msg.isFromUser() ? LLMRole.USER : LLMRole.ASSISTANT;
                payload.add(LLMChatMessage.of(role, msg.getContents()));
            }
        }

        return payload;
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