package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class ManuscriptServiceImpl extends ExtendableServiceImpl<Manuscript, ManuscriptRepository> implements ManuscriptService {

    @Autowired
    private SettingService settingService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private TagService tagService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private LorebookService lorebookService;

    @Override
    @CommonTxReadOnly
    public String getMasterTemplate(Manuscript manuscript) throws Exception {
        if (StringUtils.isNotBlank(manuscript.getTemplate())) {
            return manuscript.getTemplate();
        }
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        if (StringUtils.isNotBlank(userSetting.getMasterTemplate())) {
            return userSetting.getMasterTemplate();
        }
        return Defaults.DEFAULT_MASTER_TEMPLATE;
    }

    @Override
    @CommonTxReadOnly
    public String getPov(Manuscript manuscript) throws Exception {
        if (StringUtils.isNotBlank(manuscript.getPov())) {
            return manuscript.getPov();
        }
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        if (StringUtils.isNotBlank(userSetting.getDefaultPov())) {
            return userSetting.getDefaultPov();
        }
        return Defaults.DEFAULT_POV;
    }

    @Override
    @CommonTxReadOnly
    public String getTense(Manuscript manuscript) throws Exception {
        if (StringUtils.isNotBlank(manuscript.getTense())) {
            return manuscript.getTense();
        }
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        if (StringUtils.isNotBlank(userSetting.getDefaultTense())) {
            return userSetting.getDefaultTense();
        }
        return Defaults.DEFAULT_TENSE;
    }

    @Override
    @CommonTxReadOnly
    public String getStyle(Manuscript manuscript) throws Exception {
        if (StringUtils.isNotBlank(manuscript.getStyle())) {
            return manuscript.getStyle();
        }
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        if (StringUtils.isNotBlank(userSetting.getDefaultStyle())) {
            return userSetting.getDefaultStyle();
        }
        return Defaults.DEFAULT_STYLE;
    }

    @Override
    @CommonTxReadOnly
    public String getUserPrompt(Manuscript manuscript) throws Exception {
        if (StringUtils.isNotBlank(manuscript.getUserPrompt())) {
            return manuscript.getUserPrompt();
        }
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        if (StringUtils.isNotBlank(userSetting.getDefaultUserPrompt())) {
            return userSetting.getDefaultUserPrompt();
        }
        return Defaults.DEFAULT_USER_PROMPT;
    }

    @Override
    public String getSummaryPrompt(Manuscript manuscript) throws Exception {
        if (StringUtils.isNotBlank(manuscript.getSummaryPrompt())) {
            return manuscript.getUserPrompt();
        }
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        if (StringUtils.isNotBlank(userSetting.getDefaultSummaryPrompt())) {
            return userSetting.getDefaultSummaryPrompt();
        }
        return Defaults.DEFAULT_SUMMARY_PROMPT;
    }

    @Override
    public BackupStrategy getBackupStrategy(Manuscript manuscript) throws Exception {
        if (manuscript.getBackupStrategy() != null)
            return manuscript.getBackupStrategy();
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        if (userSetting.getBackupStrategy() != null)
            return userSetting.getBackupStrategy();
        return null;
    }

    @Override
    public String getBackupStrategyValue(Manuscript manuscript) throws Exception {
        if (manuscript.getBackupStrategy() != null)
            return manuscript.getBackupStrategyValue();
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        if (userSetting.getBackupStrategy() != null)
            return userSetting.getBackupStrategyValue();
        return null;
    }

    @Override
    @CommonTxReadOnly
    public JsonObject createBackup(Manuscript manuscript) throws Exception {
        Manuscript m = find(manuscript);
        if (m == null) {
            return null;
        }

        JsonObject backup = new JsonObject();
        backup.addProperty("uuid", m.getUuid());
        backup.addProperty("name", m.getName());
        backup.addProperty("description", m.getDescription());

        // Save extended content
        if (m.getExtendedContent() == null || m.getExtendedContent().length == 0) {
            new ExtendableEntityListener().serialize(m);
        }
        if (m.getExtendedContent() != null && m.getExtendedContent().length > 0) {
            try {
                backup.add("extendedContent", JsonParser.parseString(new String(m.getExtendedContent(), StandardCharsets.UTF_8)));
            } catch (Exception e) {
                backup.addProperty("extendedContent", new String(m.getExtendedContent(), StandardCharsets.UTF_8));
            }
        }

        if (m.getActiveLeaf() != null) {
            backup.addProperty("activeLeafUuid", m.getActiveLeaf().getUuid());
        }

        // Save linked AI (uuid and name)
        if (m.getAi() != null) {
            AI ai = aiService.find(m.getAi());
            if (ai != null) {
                JsonObject aiObj = new JsonObject();
                aiObj.addProperty("uuid", ai.getUuid());
                aiObj.addProperty("name", ai.getName());
                backup.add("ai", aiObj);
            }
        }

        // Save linked Protocol (uuid and name)
        if (m.getProtocol() != null) {
            Protocol protocol = protocolService.find(m.getProtocol());
            if (protocol != null) {
                JsonObject protoObj = new JsonObject();
                protoObj.addProperty("uuid", protocol.getUuid());
                protoObj.addProperty("name", protocol.getName());
                backup.add("protocol", protoObj);
            }
        }

        // Save linked Lorebook (uuid and name only, ignoring lorebook entries)
        if (m.getLorebook() != null) {
            Lorebook lorebook = lorebookService.find(m.getLorebook());
            if (lorebook != null) {
                JsonObject lbObj = new JsonObject();
                lbObj.addProperty("uuid", lorebook.getUuid());
                lbObj.addProperty("name", lorebook.getName());
                backup.add("lorebook", lbObj);
            }
        }

        // Save tags as simple strings
        List<Tag> tags = tagRelationService.getTagsForObject(m);
        JsonArray tagsArray = new JsonArray();
        for (Tag tag : tags) {
            if (tag.getValue() != null) {
                tagsArray.add(tag.getValue());
            }
        }
        backup.add("tags", tagsArray);

        // Save flat list of messages
        List<ChatMessage> allMessages = chatMessageService.getAllMessages(m);
        JsonArray messagesArray = new JsonArray();
        ExtendableEntityListener listener = new ExtendableEntityListener();

        for (ChatMessage msg : allMessages) {
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("uuid", msg.getUuid());
            msgObj.addProperty("tokenCount", msg.getTokenCount());
            msgObj.addProperty("wordCount", msg.getWordCount());
            msgObj.addProperty("edited", msg.isEdited());

            if (msg.getParent() != null) {
                msgObj.addProperty("parentUuid", msg.getParent().getUuid());
            }

            if (msg.getExtendedContent() == null || msg.getExtendedContent().length == 0) {
                listener.serialize(msg);
            }
            if (msg.getExtendedContent() != null && msg.getExtendedContent().length > 0) {
                try {
                    msgObj.add("extendedContent", JsonParser.parseString(new String(msg.getExtendedContent(), StandardCharsets.UTF_8)));
                } catch (Exception e) {
                    msgObj.addProperty("extendedContent", new String(msg.getExtendedContent(), StandardCharsets.UTF_8));
                }
            }

            if (msg.getSummary() != null) {
                Summary summary = summaryService.find(msg.getSummary());
                if (summary != null) {
                    JsonObject sumObj = new JsonObject();
                    sumObj.addProperty("uuid", summary.getUuid());
                    if (summary.getExtendedContent() == null || summary.getExtendedContent().length == 0) {
                        listener.serialize(summary);
                    }
                    if (summary.getExtendedContent() != null && summary.getExtendedContent().length > 0) {
                        try {
                            sumObj.add("extendedContent", JsonParser.parseString(new String(summary.getExtendedContent(), StandardCharsets.UTF_8)));
                        } catch (Exception e) {
                            sumObj.addProperty("extendedContent", new String(summary.getExtendedContent(), StandardCharsets.UTF_8));
                        }
                    }
                    msgObj.add("summary", sumObj);
                }
            }

            messagesArray.add(msgObj);
        }

        backup.add("messages", messagesArray);

        return backup;
    }

    @Override
    @CommonTx
    public Manuscript cloneFromBackup(JsonObject backup) throws Exception {
        if (backup == null) {
            return null;
        }

        Manuscript manuscript = new Manuscript();

        if (backup.has("name") && !backup.get("name").isJsonNull()) {
            manuscript.setName(backup.get("name").getAsString());
        }
        if (backup.has("description") && !backup.get("description").isJsonNull()) {
            manuscript.setDescription(backup.get("description").getAsString());
        }
        if (backup.has("extendedContent") && !backup.get("extendedContent").isJsonNull()) {
            JsonElement ext = backup.get("extendedContent");
            manuscript.setExtendedContent(ext.toString().getBytes(StandardCharsets.UTF_8));
        }

        if (backup.has("ai") && backup.get("ai").isJsonObject()) {
            try {
                JsonObject aiObj = backup.getAsJsonObject("ai");
                if (aiObj.has("uuid") && !aiObj.get("uuid").isJsonNull()) {
                    String aiUuid = aiObj.get("uuid").getAsString();
                    Long aiId = aiService.find(aiUuid);
                    if (aiId != null) {
                        manuscript.setAi(aiService.find(aiId));
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (backup.has("protocol") && backup.get("protocol").isJsonObject()) {
            try {
                JsonObject protoObj = backup.getAsJsonObject("protocol");
                if (protoObj.has("uuid") && !protoObj.get("uuid").isJsonNull()) {
                    String protoUuid = protoObj.get("uuid").getAsString();
                    Long protoId = protocolService.find(protoUuid);
                    if (protoId != null) {
                        manuscript.setProtocol(protocolService.find(protoId));
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (backup.has("lorebook") && backup.get("lorebook").isJsonObject()) {
            try {
                JsonObject lbObj = backup.getAsJsonObject("lorebook");
                if (lbObj.has("uuid") && !lbObj.get("uuid").isJsonNull()) {
                    String lbUuid = lbObj.get("uuid").getAsString();
                    Long lbId = lorebookService.find(lbUuid);
                    if (lbId != null) {
                        manuscript.setLorebook(lorebookService.find(lbId));
                    }
                }
            } catch (Exception ignored) {
            }
        }

        manuscript = saveWithoutEvent(manuscript);

        if (backup.has("tags") && backup.get("tags").isJsonArray()) {
            for (JsonElement tagElem : backup.getAsJsonArray("tags")) {
                try {
                    String tagVal = tagElem.getAsString();
                    if (StringUtils.isNotBlank(tagVal)) {
                        List<Tag> matches = tagService.searchTagsForUser(tagVal.trim(), 0, 10);
                        Tag tag = matches.stream()
                                .filter(t -> StringUtils.equals(t.getValue(), tagVal.trim()))
                                .findFirst()
                                .orElse(null);
                        if (tag != null) {
                            tagRelationService.createRelation(tag, manuscript);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        Map<String, ChatMessage> uuidToNodeMap = new HashMap<>();
        if (backup.has("messages") && backup.get("messages").isJsonArray()) {
            uuidToNodeMap = restoreMessages(backup.getAsJsonArray("messages"), manuscript);
        }

        if (backup.has("activeLeafUuid") && !backup.get("activeLeafUuid").isJsonNull()) {
            String activeLeafUuid = backup.get("activeLeafUuid").getAsString();
            ChatMessage activeLeaf = uuidToNodeMap.get(activeLeafUuid);
            if (activeLeaf != null) {
                manuscript.setActiveLeaf(activeLeaf);
                manuscript = saveWithoutEvent(manuscript);
            }
        }

        return manuscript;
    }

    @Override
    @CommonTx
    public Manuscript restoreBackup(Manuscript manuscript, boolean onlyRestoreMessages, JsonObject backup) throws Exception {
        Manuscript m = find(manuscript);
        if (m == null || backup == null) {
            return m;
        }

        // 1. Store all current messages in that manuscript
        List<ChatMessage> oldMessages = chatMessageService.getAllMessages(m);

        // Clear active leaf temporarily to avoid FK constraint issues during message cleanup
        m.setActiveLeaf(null);
        m = saveWithoutEvent(m);

        if (!onlyRestoreMessages) {
            if (backup.has("name") && !backup.get("name").isJsonNull()) {
                m.setName(backup.get("name").getAsString());
            }
            if (backup.has("description") && !backup.get("description").isJsonNull()) {
                m.setDescription(backup.get("description").getAsString());
            }
            if (backup.has("extendedContent") && !backup.get("extendedContent").isJsonNull()) {
                JsonElement ext = backup.get("extendedContent");
                m.setExtendedContent(ext.toString().getBytes(StandardCharsets.UTF_8));
            }

            // Try to find linked AI by UUID, ignore if failed
            if (backup.has("ai") && backup.get("ai").isJsonObject()) {
                try {
                    JsonObject aiObj = backup.getAsJsonObject("ai");
                    if (aiObj.has("uuid") && !aiObj.get("uuid").isJsonNull()) {
                        String aiUuid = aiObj.get("uuid").getAsString();
                        Long aiId = aiService.find(aiUuid);
                        if (aiId != null) {
                            m.setAi(aiService.find(aiId));
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            // Try to find linked Protocol by UUID, ignore if failed
            if (backup.has("protocol") && backup.get("protocol").isJsonObject()) {
                try {
                    JsonObject protoObj = backup.getAsJsonObject("protocol");
                    if (protoObj.has("uuid") && !protoObj.get("uuid").isJsonNull()) {
                        String protoUuid = protoObj.get("uuid").getAsString();
                        Long protoId = protocolService.find(protoUuid);
                        if (protoId != null) {
                            m.setProtocol(protocolService.find(protoId));
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            // Try to find linked Lorebook by UUID, ignore if failed
            if (backup.has("lorebook") && backup.get("lorebook").isJsonObject()) {
                try {
                    JsonObject lbObj = backup.getAsJsonObject("lorebook");
                    if (lbObj.has("uuid") && !lbObj.get("uuid").isJsonNull()) {
                        String lbUuid = lbObj.get("uuid").getAsString();
                        Long lbId = lorebookService.find(lbUuid);
                        if (lbId != null) {
                            m.setLorebook(lorebookService.find(lbId));
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            // Restore tags
            if (backup.has("tags") && backup.get("tags").isJsonArray()) {
                try {
                    List<Tag> existingTags = tagRelationService.getTagsForObject(m);
                    for (Tag tag : existingTags) {
                        tagRelationService.removeRelation(tag, m);
                    }
                    for (JsonElement tagElem : backup.getAsJsonArray("tags")) {
                        String tagVal = tagElem.getAsString();
                        if (StringUtils.isNotBlank(tagVal)) {
                            List<Tag> matches = tagService.searchTagsForUser(tagVal.trim(), 0, 10);
                            Tag tag = matches.stream()
                                    .filter(t -> StringUtils.equals(t.getValue(), tagVal.trim()))
                                    .findFirst()
                                    .orElse(null);
                            if (tag != null) {
                                tagRelationService.createRelation(tag, m);
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            m = saveWithoutEvent(m);
        }

        // 2. Restore all backed up messages
        Map<String, ChatMessage> uuidToNodeMap = new HashMap<>();
        if (backup.has("messages") && backup.get("messages").isJsonArray()) {
            uuidToNodeMap = restoreMessages(backup.getAsJsonArray("messages"), m);
        }

        // Link active leaf
        if (backup.has("activeLeafUuid") && !backup.get("activeLeafUuid").isJsonNull()) {
            String activeLeafUuid = backup.get("activeLeafUuid").getAsString();
            ChatMessage activeLeaf = uuidToNodeMap.get(activeLeafUuid);
            if (activeLeaf != null) {
                m.setActiveLeaf(activeLeaf);
                m = saveWithoutEvent(m);
            }
        }

        // 3. Hard delete all stored old messages in reverse order (child messages first)
        List<ChatMessage> reversedOld = new ArrayList<>(oldMessages);
        Collections.reverse(reversedOld);
        for (ChatMessage oldMsg : reversedOld) {
            if (oldMsg.getSummary() != null) {
                Summary summary = summaryService.find(oldMsg.getSummary());
                oldMsg.setSummary(null);
                chatMessageService.saveWithoutEvent(oldMsg);
                if (summary != null) {
                    summaryService.delete(summary, true);
                }
            }
            chatMessageService.delete(oldMsg, true);
        }

        return m;
    }

    private Map<String, ChatMessage> restoreMessages(JsonArray messagesArray, Manuscript manuscript) throws Exception {
        Map<String, ChatMessage> uuidToNodeMap = new HashMap<>();
        if (messagesArray == null) {
            return uuidToNodeMap;
        }

        Map<ChatMessage, String> parentUuidMap = new HashMap<>();

        for (JsonElement elem : messagesArray) {
            JsonObject msgObj = elem.getAsJsonObject();
            ChatMessage msg = new ChatMessage();
            msg.setParentScript(manuscript);

            if (msgObj.has("tokenCount") && !msgObj.get("tokenCount").isJsonNull()) {
                msg.setTokenCount(msgObj.get("tokenCount").getAsLong());
            }
            if (msgObj.has("wordCount") && !msgObj.get("wordCount").isJsonNull()) {
                msg.setWordCount(msgObj.get("wordCount").getAsInt());
            }
            if (msgObj.has("edited") && !msgObj.get("edited").isJsonNull()) {
                msg.setEdited(msgObj.get("edited").getAsBoolean());
            }

            if (msgObj.has("extendedContent") && !msgObj.get("extendedContent").isJsonNull()) {
                JsonElement ext = msgObj.get("extendedContent");
                msg.setExtendedContent(ext.toString().getBytes(StandardCharsets.UTF_8));
            }

            if (msgObj.has("summary") && msgObj.get("summary").isJsonObject()) {
                JsonObject sumObj = msgObj.getAsJsonObject("summary");
                Summary summary = new Summary();
                if (sumObj.has("extendedContent") && !sumObj.get("extendedContent").isJsonNull()) {
                    JsonElement ext = sumObj.get("extendedContent");
                    summary.setExtendedContent(ext.toString().getBytes(StandardCharsets.UTF_8));
                }
                summary = summaryService.saveWithoutEvent(summary);
                msg.setSummary(summary);
            }

            msg = chatMessageService.saveWithoutEvent(msg);

            if (msgObj.has("uuid") && !msgObj.get("uuid").isJsonNull()) {
                String uuid = msgObj.get("uuid").getAsString();
                uuidToNodeMap.put(uuid, msg);
            }

            if (msgObj.has("parentUuid") && !msgObj.get("parentUuid").isJsonNull()) {
                parentUuidMap.put(msg, msgObj.get("parentUuid").getAsString());
            }
        }

        for (Map.Entry<ChatMessage, String> entry : parentUuidMap.entrySet()) {
            ChatMessage msg = entry.getKey();
            String parentUuid = entry.getValue();
            ChatMessage parent = uuidToNodeMap.get(parentUuid);
            if (parent != null) {
                msg.setParent(parent);
                chatMessageService.saveWithoutEvent(msg);
            }
        }

        return uuidToNodeMap;
    }
}