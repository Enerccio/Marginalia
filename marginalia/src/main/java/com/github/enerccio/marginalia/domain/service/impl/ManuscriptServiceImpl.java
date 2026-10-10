package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.SummaryType;
import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.Sorter;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

public class ManuscriptServiceImpl extends ExtendableServiceImpl<Manuscript, ManuscriptRepository> implements ManuscriptService {
    private static final Logger log = LoggerFactory.getLogger(ManuscriptServiceImpl.class);


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
    public List<Long> searchManuscripts(Sorter... sorters) throws Exception {
        return searchManuscripts(null, sorters);
    }

    @Override
    @CommonTxReadOnly
    public List<Long> searchManuscripts(ManuscriptFilterValues filterValues, Sorter... sorters) throws Exception {
        return getRepository().searchManuscripts(filterValues, List.of(sorters), currentUser);
    }

    @Override
    @CommonTxReadOnly
    public Manuscript findViewable(String uuid) throws Exception {
        if (StringUtils.isBlank(uuid)) {
            return null;
        }
        return getRepository().findViewable(uuid, currentUser);
    }

    @Override
    @CommonTx
    public void markOpened(Manuscript manuscript) throws Exception {
        if (manuscript != null && manuscript.getId() != null) {
            getRepository().markOpened(manuscript.getId(), currentUser);
        }
    }

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
            return manuscript.getSummaryPrompt();
        }
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        if (StringUtils.isNotBlank(userSetting.getDefaultSummaryPrompt())) {
            return userSetting.getDefaultSummaryPrompt();
        }
        return Defaults.DEFAULT_SUMMARY_PROMPT;
    }

    @Override
    public String getMetaSummaryPrompt(Manuscript manuscript) throws Exception {
        if (StringUtils.isNotBlank(manuscript.getMetaSummaryPrompt())) {
            return manuscript.getMetaSummaryPrompt();
        }
        UserSetting userSetting = settingService.getOrCreate(UserSetting.class);
        if (StringUtils.isNotBlank(userSetting.getDefaultMetaSummaryPrompt())) {
            return userSetting.getDefaultMetaSummaryPrompt();
        }
        return Defaults.DEFAULT_META_SUMMARY;
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
        backup.addProperty("published", m.isPublished());

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

        // Save linked Lorebook reference and flat list of the lorebook with all its subbooks
        if (m.getLorebook() != null) {
            Lorebook lorebook = lorebookService.find(m.getLorebook());
            if (lorebook != null) {
                JsonObject lbObj = new JsonObject();
                lbObj.addProperty("uuid", lorebook.getUuid());
                lbObj.addProperty("name", lorebook.getName());
                backup.add("lorebook", lbObj);
                backup.add("lorebooks", lorebookService.marshalLorebooks(lorebook));
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
        if (backup.has("published") && !backup.get("published").isJsonNull()) {
            manuscript.setPublished(backup.get("published").getAsBoolean());
        }
        if (backup.has("extendedContent") && !backup.get("extendedContent").isJsonNull()) {
            JsonElement ext = backup.get("extendedContent");
            manuscript.setExtendedContent(ext.toString().getBytes(StandardCharsets.UTF_8));
        }

        AI ai = resolveLinked(backup, "ai", aiService, AI::getName);
        if (ai != null) {
            manuscript.setAi(ai);
        }
        Protocol protocol = resolveLinked(backup, "protocol", protocolService, Protocol::getName);
        if (protocol != null) {
            manuscript.setProtocol(protocol);
        }

        if (backup.has("lorebook") && backup.get("lorebook").isJsonObject()) {
            try {
                JsonObject lbObj = backup.getAsJsonObject("lorebook");
                if (lbObj.has("uuid") && !lbObj.get("uuid").isJsonNull()) {
                    String lbUuid = lbObj.get("uuid").getAsString();
                    // only own entities can be linked, missing ones are left as they are
                    Lorebook lorebook = lorebookService.findForUser(lbUuid);
                    if (lorebook != null) {
                        manuscript.setLorebook(lorebook);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        manuscript = saveWithoutEvent(manuscript);

        restoreTags(backup, manuscript);

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
            // older backups don't carry the flag, keep the current state then
            if (backup.has("published") && !backup.get("published").isJsonNull()) {
                m.setPublished(backup.get("published").getAsBoolean());
            }
            if (backup.has("extendedContent") && !backup.get("extendedContent").isJsonNull()) {
                JsonElement ext = backup.get("extendedContent");
                m.setExtendedContent(ext.toString().getBytes(StandardCharsets.UTF_8));
            }

            // missing AI/protocol keeps the current one
            AI ai = resolveLinked(backup, "ai", aiService, AI::getName);
            if (ai != null) {
                m.setAi(ai);
            }
            Protocol protocol = resolveLinked(backup, "protocol", protocolService, Protocol::getName);
            if (protocol != null) {
                m.setProtocol(protocol);
            }

            // Try to find linked Lorebook by UUID, ignore if failed
            if (backup.has("lorebook") && backup.get("lorebook").isJsonObject()) {
                try {
                    JsonObject lbObj = backup.getAsJsonObject("lorebook");
                    if (lbObj.has("uuid") && !lbObj.get("uuid").isJsonNull()) {
                        String lbUuid = lbObj.get("uuid").getAsString();
                        // only own entities can be linked, missing ones are left as they are
                        Lorebook lorebook = lorebookService.findForUser(lbUuid);
                        if (lorebook != null) {
                            m.setLorebook(lorebook);
                        }
                    }
                } catch (Exception ignored) {
                }
            }

            // Restore tags
            if (backup.has("tags") && backup.get("tags").isJsonArray()) {
                for (Tag tag : tagRelationService.getTagsForObject(m)) {
                    tagRelationService.removeRelation(tag, m);
                }
                restoreTags(backup, m);
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

    /**
     * Links the backup's tags to the manuscript, tags the user doesn't have (anymore) are created.
     */
    private void restoreTags(JsonObject backup, Manuscript manuscript) throws Exception {
        if (!backup.has("tags") || !backup.get("tags").isJsonArray()) {
            return;
        }
        for (JsonElement tagElem : backup.getAsJsonArray("tags")) {
            if (tagElem == null || !tagElem.isJsonPrimitive()) {
                continue;
            }
            Tag tag = tagService.getOrCreateForUser(tagElem.getAsString());
            if (tag != null) {
                tagRelationService.createRelation(tag, manuscript);
            }
        }
    }

    /**
     * Entity referenced by backup ({"uuid": ..., "name": ...}) among the current user's own entities: first by uuid,
     * then by name (case-insensitive, like lorebook import), {@code null} when there is no match.
     */
    private <T extends OwnedEntity> T resolveLinked(JsonObject backup, String key, OwnedService<T, ?> service,
                                                    Function<T, String> nameOf) {
        if (!backup.has(key) || !backup.get(key).isJsonObject()) {
            return null;
        }
        try {
            JsonObject ref = backup.getAsJsonObject(key);
            if (ref.has("uuid") && !ref.get("uuid").isJsonNull()) {
                T byUuid = service.findForUser(ref.get("uuid").getAsString());
                if (byUuid != null) {
                    return byUuid;
                }
            }
            if (ref.has("name") && !ref.get("name").isJsonNull()) {
                String name = StringUtils.trim(ref.get("name").getAsString());
                return service.findAllForUser().stream()
                        .filter(e -> StringUtils.equalsIgnoreCase(StringUtils.trim(nameOf.apply(e)), name))
                        .min(Comparator.comparing(BaseEntity::getId))
                        .orElse(null);
            }
        } catch (Exception e) {
            log.warn("Cannot resolve {} of backup: {}", key, e.getMessage());
        }
        return null;
    }

    private Map<String, ChatMessage> restoreMessages(JsonArray messagesArray, Manuscript manuscript) throws Exception {
        Map<String, ChatMessage> uuidToNodeMap = new HashMap<>();
        if (messagesArray == null) {
            return uuidToNodeMap;
        }

        Map<ChatMessage, String> parentUuidMap = new HashMap<>();
        // restored summaries get new uuids, meta summaries point to summaries by uuid
        Map<String, String> summaryUuids = new HashMap<>();
        List<Summary> restoredSummaries = new ArrayList<>();

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
                restoredSummaries.add(summary);
                if (sumObj.has("uuid") && !sumObj.get("uuid").isJsonNull()) {
                    summaryUuids.put(sumObj.get("uuid").getAsString(), summary.getUuid());
                }
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

        for (Summary summary : restoredSummaries) {
            if (summary.getSummaryType() != SummaryType.META_SUMMARY || summary.getExtendedContent() == null) {
                continue;
            }
            JsonObject extended = JsonParser.parseString(new String(summary.getExtendedContent(), StandardCharsets.UTF_8)).getAsJsonObject();
            if (remapNextSummaryUuid(extended, summaryUuids)) {
                summary.setExtendedContent(extended.toString().getBytes(StandardCharsets.UTF_8));
                summaryService.saveWithoutEvent(summary);
            }
        }

        return uuidToNodeMap;
    }

    /**
     * Points the meta summary (and the summaries it replaced, those are serialized inside of it) to the restored
     * summaries.
     *
     * @param extended extended content of the summary
     * @return true if something was changed
     */
    private boolean remapNextSummaryUuid(JsonObject extended, Map<String, String> summaryUuids) {
        boolean changed = false;
        if (extended.has("nextSummaryUuid") && extended.get("nextSummaryUuid").isJsonPrimitive()) {
            String restored = summaryUuids.get(extended.get("nextSummaryUuid").getAsString());
            if (restored != null) {
                extended.addProperty("nextSummaryUuid", restored);
                changed = true;
            }
        }
        if (extended.has("replacedSummary") && extended.get("replacedSummary").isJsonPrimitive()) {
            try {
                JsonObject replaced = JsonParser.parseString(extended.get("replacedSummary").getAsString()).getAsJsonObject();
                if (remapNextSummaryUuid(replaced, summaryUuids)) {
                    extended.addProperty("replacedSummary", replaced.toString());
                    changed = true;
                }
            } catch (Exception e) {
                log.warn("Cannot read replaced summary of a restored meta summary: {}", e.getMessage());
            }
        }
        return changed;
    }
}