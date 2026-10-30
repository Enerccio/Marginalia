package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.AgentRunPairKind;
import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.collections.SummaryType;
import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.OwnedEntity;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;
import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.security.service.PermissionService;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.agent.UuidPair;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptSearchCriteria;
import com.github.enerccio.marginalia.domain.service.search.Sorter;
import com.github.enerccio.marginalia.domain.templates.*;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

public class ManuscriptServiceImpl extends ExtendableServiceImpl<Manuscript, ManuscriptRepository> implements ManuscriptService {
    private static final Logger log = LoggerFactory.getLogger(ManuscriptServiceImpl.class);

    /**
     * Removes the text value of the key from the object and returns it, null when there was none.
     */
    private static String takeString(JsonObject object, String key) {
        JsonElement value = object.remove(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : null;
    }


    @Autowired
    private SettingService settingService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private AnnotationService annotationService;

    @Autowired
    @Lazy
    private MessageRevisionService messageRevisionService;

    @Autowired
    private MessagePromptService messagePromptService;

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

    @Autowired
    @Lazy
    private AgentService agentService;

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    @Autowired
    @Lazy
    private FolderService folderService;

    @Autowired
    @Lazy
    private PermissionService permissionService;

    /**
     * A book an agent works on can't be changed, see {@link AgentRunService#checkNotLocked}.
     */
    @Override
    @CommonTx
    public Manuscript save(Manuscript manuscript) throws Exception {
        agentRunService.checkNotLocked(manuscript);
        checkFolder(manuscript);
        return super.save(manuscript);
    }

    @Override
    @CommonTx
    public Manuscript update(Manuscript book, BookChange change) throws Exception {
        // by id and without the current user, like save: a generation calls this from threads that have no session
        Manuscript fresh = book == null ? null : find(book.getId());
        if (fresh == null || fresh.isDeleted()) {
            throw new IllegalStateException("The book no longer exists");
        }
        change.apply(fresh);
        return save(fresh);
    }

    /**
     * A book is only put into a folder of its user.
     */
    private void checkFolder(Manuscript manuscript) throws Exception {
        // the owner of the book, the current user only for a new book: a generation saves from threads that have no session
        Long ownerId = manuscript.getOwner() != null ? manuscript.getOwner().getId() : currentUser.getId();
        checkFolder(manuscript.getFolder(), ownerId);
    }

    private Folder checkFolder(Folder folder, Long ownerId) throws Exception {
        if (folder == null) {
            return null;
        }
        Folder stored = folderService.find(folder.getId());
        if (stored == null || stored.isDeleted() || stored.getOwner() == null || !stored.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("The folder does not exist");
        }
        return stored;
    }

    @Override
    @CommonTx
    public List<String> getFavorites() throws Exception {
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        List<String> stored = setting.getFavoriteBooks();
        if (stored.isEmpty()) {
            return List.of();
        }
        Set<String> existing = getRepository().findExistingRegularUuids(stored, currentUser);
        List<String> favorites = stored.stream().filter(existing::contains).distinct().toList();
        if (favorites.size() != stored.size()) {
            setting.setFavoriteBooks(favorites);
            settingService.save(setting);
        }
        return favorites;
    }

    @Override
    @CommonTx
    public void setFavorite(Manuscript book, boolean favorite) throws Exception {
        Manuscript stored = book == null || book.getUuid() == null ? null : findForUser(book.getUuid());
        if (stored == null || stored.isDeleted()) {
            throw new IllegalArgumentException("The book does not exist");
        }
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        List<String> favorites = new ArrayList<>(setting.getFavoriteBooks());
        if (favorite ? !favorites.contains(stored.getUuid()) : favorites.remove(stored.getUuid())) {
            if (favorite) {
                favorites.add(stored.getUuid());
            }
            setting.setFavoriteBooks(favorites);
            settingService.save(setting);
        }
    }

    @Override
    @CommonTx
    public void moveToFolder(Collection<Manuscript> books, Folder folder) throws Exception {
        Folder target = checkFolder(folder, currentUser.getId());
        List<Long> ids = new ArrayList<>();
        for (Manuscript book : books) {
            Manuscript stored = book == null ? null : findForUser(book.getUuid());
            if (stored == null) {
                throw new IllegalArgumentException("The book does not exist");
            }
            agentRunService.checkNotLocked(stored);
            ids.add(stored.getId());
        }
        getRepository().moveToFolder(ids, target, currentUser);
    }

    /**
     * A soft deleted manuscript takes its messages (with their summaries), its tags and its agent (with the runs of
     * the agent) with it.
     */
    @Override
    @CommonTx
    public Manuscript delete(Manuscript manuscript, boolean hard) throws Exception {
        agentRunService.checkNotLocked(manuscript);
        if (!hard && manuscript != null && manuscript.getId() != null) {
            chatMessageService.softDeleteForManuscript(manuscript);
            tagRelationService.deleteForObject(manuscript, false);
            agentService.deleteForManuscript(manuscript, false);
        }
        return super.delete(manuscript, hard);
    }

    @Override
    @CommonTxReadOnly
    public List<Long> searchManuscripts(Sorter... sorters) throws Exception {
        return searchManuscripts(null, sorters);
    }

    @Override
    @CommonTxReadOnly
    public List<Long> searchManuscripts(ManuscriptFilterValues filterValues, Sorter... sorters) throws Exception {
        return getRepository().searchManuscripts(folderService.resolveScope(filterValues), List.of(sorters), currentUser);
    }

    @Override
    @CommonTxReadOnly
    public List<Long> searchRegularManuscripts(ManuscriptFilterValues filterValues, Sorter... sorters) throws Exception {
        return getRepository().searchManuscripts(folderService.resolveScope(filterValues), List.of(sorters), currentUser, false);
    }

    @Override
    @CommonTxReadOnly
    public List<Long> searchAdvanced(ManuscriptSearchCriteria criteria, Sorter... sorters) throws Exception {
        List<Long> ids = getRepository().searchManuscripts(criteria, List.of(sorters), currentUser);
        if (StringUtils.isBlank(criteria.getFulltext()) || ids.isEmpty()) {
            return ids;
        }
        Set<Long> withText = chatMessageService.findManuscriptsWithText(ids, criteria.getFulltext(), criteria.isAllBranches(), criteria.isIncludeVersions());
        return ids.stream().filter(withText::contains).toList();
    }

    @Override
    @CommonTxReadOnly
    public List<Long> searchAgenticManuscripts(ManuscriptFilterValues filterValues, Sorter... sorters) throws Exception {
        return getRepository().searchManuscripts(folderService.resolveScope(filterValues), List.of(sorters), currentUser, true);
    }

    @Override
    @CommonTxReadOnly
    public List<Manuscript> findRegularInFolder(Folder folder, boolean includeSubfolders) throws Exception {
        ManuscriptFilterValues scope = folderService.resolveScope(new ManuscriptFilterValues().inFolder(folder, includeSubfolders));
        List<Manuscript> books = new ArrayList<>();
        for (Long id : getRepository().searchManuscripts(scope, List.of(), currentUser, false)) {
            books.add(getRepository().find(id));
        }
        return books;
    }

    @Override
    @CommonTxReadOnly
    public List<Manuscript> findAllRegularForUser() throws Exception {
        return getRepository().findAllByAgentic(false, currentUser);
    }

    @Override
    @CommonTxReadOnly
    public List<Manuscript> findAllAgenticForUser() throws Exception {
        return getRepository().findAllByAgentic(true, currentUser);
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
    public EffectiveSetting<String> resolve(Manuscript manuscript, PromptField field) throws Exception {
        String own = field.ofBook(manuscript);
        if (StringUtils.isNotBlank(own)) {
            return new EffectiveSetting<>(own, EffectiveSetting.Origin.BOOK, null);
        }
        return resolveInherited(manuscript.getFolder(), field);
    }

    @Override
    public EffectiveSetting<String> resolveInherited(Folder start, PromptField field) throws Exception {
        for (Folder folder : folderService.findChain(start)) {
            String inherited = field.ofFolder(folder);
            if (StringUtils.isNotBlank(inherited)) {
                return new EffectiveSetting<>(inherited, EffectiveSetting.Origin.FOLDER, folder);
            }
        }
        String fromUser = field.ofUser(settingService.getOrCreate(UserSetting.class));
        if (StringUtils.isNotBlank(fromUser)) {
            return new EffectiveSetting<>(fromUser, EffectiveSetting.Origin.USER, null);
        }
        return new EffectiveSetting<>(field.fallback(), EffectiveSetting.Origin.DEFAULT, null);
    }

    @Override
    public EffectiveSetting<Language> resolveLanguage(Manuscript manuscript) throws Exception {
        if (manuscript.getOwnLanguage() != null) {
            return new EffectiveSetting<>(manuscript.getOwnLanguage(), EffectiveSetting.Origin.BOOK, null);
        }
        return resolveInheritedLanguage(manuscript.getFolder());
    }

    @Override
    public EffectiveSetting<Language> resolveInheritedLanguage(Folder start) throws Exception {
        for (Folder folder : folderService.findChain(start)) {
            if (folder.getLanguage() != null) {
                return new EffectiveSetting<>(folder.getLanguage(), EffectiveSetting.Origin.FOLDER, folder);
            }
        }
        return new EffectiveSetting<>(Language.ENGLISH, EffectiveSetting.Origin.DEFAULT, null);
    }

    @Override
    public EffectiveHistoryLimit resolveHistoryLimit(Manuscript manuscript) throws Exception {
        EffectiveHistoryLimit inherited = resolveInheritedHistoryLimit(manuscript.getFolder(), manuscript.getOwner());
        return stricter(manuscript.getHistoryMaxVersions(), EffectiveHistoryLimit.Origin.BOOK, null, inherited);
    }

    @Override
    public EffectiveHistoryLimit resolveInheritedHistoryLimit(Folder start) throws Exception {
        return resolveInheritedHistoryLimit(start, start == null ? currentUser : start.getOwner());
    }

    private EffectiveHistoryLimit resolveInheritedHistoryLimit(Folder start, User owner) throws Exception {
        // from the permission to the book, so that the nearer level wins when several set the same
        EffectiveHistoryLimit limit = permissionHistoryLimit(owner);
        UserSetting settings = owner == null ? null : settingService.find(UserSetting.class, owner);
        limit = stricter(settings == null ? null : settings.getHistoryMaxVersions(), EffectiveHistoryLimit.Origin.USER, null, limit);
        for (Folder folder : folderService.findChain(start).reversed()) {
            limit = stricter(folder.getHistoryMaxVersions(), EffectiveHistoryLimit.Origin.FOLDER, folder, limit);
        }
        return limit;
    }

    @Override
    public EffectiveHistoryLimit resolvePermissionHistoryLimit() throws Exception {
        return permissionHistoryLimit(currentUser);
    }

    /**
     * The ceiling of the administrator: no permission at all is the default, one that is not granted keeps no history.
     */
    private EffectiveHistoryLimit permissionHistoryLimit(User owner) throws Exception {
        Permission permission = owner == null ? null : permissionService.getPermission(owner, PermissionType.VERSION_HISTORY);
        if (permission == null) {
            return new EffectiveHistoryLimit(EffectiveHistoryLimit.DEFAULT_MAX_VERSIONS, EffectiveHistoryLimit.Origin.PERMISSION, null);
        }
        if (!permission.isGranted()) {
            return new EffectiveHistoryLimit(0, EffectiveHistoryLimit.Origin.PERMISSION, null);
        }
        Integer set = permission.getHistoryMaxVersions();
        return set == null || set < 0 ? new EffectiveHistoryLimit(null, EffectiveHistoryLimit.Origin.NONE, null)
                : new EffectiveHistoryLimit(set, EffectiveHistoryLimit.Origin.PERMISSION, null);
    }

    /**
     * The nearer level replaces the limit when it is smaller or equal (equal: the nearer is the one to change to get more).
     */
    private static EffectiveHistoryLimit stricter(Integer set, EffectiveHistoryLimit.Origin origin, Folder folder, EffectiveHistoryLimit farther) {
        if (set == null || set < 0) {
            return farther;
        }
        if (farther.isUnlimited() || set <= farther.value()) {
            return new EffectiveHistoryLimit(set, origin, folder);
        }
        return farther;
    }

    @Override
    public Language getLanguage(Manuscript manuscript) throws Exception {
        return resolveLanguage(manuscript).value();
    }

    @Override
    public String getMasterTemplate(Manuscript manuscript) throws Exception {
        return resolve(manuscript, PromptField.MASTER_TEMPLATE).value();
    }

    @Override
    public String getPov(Manuscript manuscript) throws Exception {
        return resolve(manuscript, PromptField.POV).value();
    }

    @Override
    public String getTense(Manuscript manuscript) throws Exception {
        return resolve(manuscript, PromptField.TENSE).value();
    }

    @Override
    public String getStyle(Manuscript manuscript) throws Exception {
        return resolve(manuscript, PromptField.STYLE).value();
    }

    @Override
    public String getUserPrompt(Manuscript manuscript) throws Exception {
        return resolve(manuscript, PromptField.USER_PROMPT).value();
    }

    @Override
    public String getSummaryPrompt(Manuscript manuscript) throws Exception {
        return resolve(manuscript, PromptField.SUMMARY_PROMPT).value();
    }

    @Override
    public String getRedraftSystemPrompt(Manuscript manuscript) throws Exception {
        return resolve(manuscript, PromptField.REDRAFT_SYSTEM_PROMPT).value();
    }

    @Override
    public String getAnnotateSystemPrompt(Manuscript manuscript) throws Exception {
        return resolve(manuscript, PromptField.ANNOTATE_SYSTEM_PROMPT).value();
    }

    @Override
    public String getRedraftPrompt(Manuscript manuscript) throws Exception {
        return resolve(manuscript, PromptField.REDRAFT_PROMPT).value();
    }

    @Override
    public String getAnnotatePrompt(Manuscript manuscript) throws Exception {
        return resolve(manuscript, PromptField.ANNOTATE_PROMPT).value();
    }

    @Override
    public String getImpersonatePrompt(Manuscript manuscript) throws Exception {
        return resolve(manuscript, PromptField.IMPERSONATE_PROMPT).value();
    }

    @Override
    public String getMetaSummaryPrompt(Manuscript manuscript) throws Exception {
        return resolve(manuscript, PromptField.META_SUMMARY_PROMPT).value();
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
        return createBackup(manuscript, false);
    }

    @Override
    @CommonTxReadOnly
    public JsonObject createBackup(Manuscript manuscript, boolean includeHistory) throws Exception {
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
        Map<Long, JsonArray> annotations = annotationService.marshalForManuscript(m);
        Map<Long, MessagePrompt> prompts = messagePromptService.findAllFor(allMessages);
        Map<Long, JsonArray> revisions = includeHistory
                ? messageRevisionService.marshalForParts(allMessages.stream().map(ChatMessage::getId).toList()) : Map.of();

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
            // the prompt is stored apart from the part, but a backup keeps it in the extended content as it always did, so
            // backups of every version are the same format (see restoreMessages)
            MessagePrompt prompt = prompts.get(msg.getId());
            if (prompt != null) {
                JsonObject ext = msgObj.has("extendedContent") && msgObj.get("extendedContent").isJsonObject()
                        ? msgObj.getAsJsonObject("extendedContent") : new JsonObject();
                if (prompt.getBuiltPrompt() != null) {
                    ext.addProperty(MessagePrompt.PROMPT_KEY, prompt.getBuiltPrompt());
                }
                if (prompt.getBackgroundLore() != null) {
                    ext.addProperty(MessagePrompt.LORE_KEY, prompt.getBackgroundLore());
                }
                msgObj.add("extendedContent", ext);
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

            JsonArray messageAnnotations = annotations.get(msg.getId());
            if (messageAnnotations != null) {
                msgObj.add("annotations", messageAnnotations);
            }

            JsonArray messageRevisions = revisions.get(msg.getId());
            if (messageRevisions != null) {
                msgObj.add("revisions", messageRevisions);
            }

            messagesArray.add(msgObj);
        }

        backup.add("messages", messagesArray);

        return backup;
    }

    @Override
    @CommonTx
    public SettingsSaved updateSettings(Manuscript manuscript, BookSettings changes) throws Exception {
        Manuscript book = manuscript == null || manuscript.getUuid() == null ? null : (Manuscript) Hibernate.unproxy(findForUser(manuscript.getUuid()));
        if (book == null || book.isDeleted()) {
            throw new IllegalArgumentException("The manuscript does not exist");
        }
        if (changes.name() != null && StringUtils.isBlank(changes.name())) {
            throw new IllegalArgumentException("The name can't be empty");
        }
        // all the templates are checked before anything is changed
        List<String> warnings = new ArrayList<>();
        checkTemplate(changes.masterTemplate(), "masterTemplate", MasterTemplateData.class, warnings);
        checkTemplate(changes.userPrompt(), "userPrompt", UserPromptData.class, warnings);
        checkTemplate(changes.summaryPrompt(), "summaryPrompt", SummaryTemplateData.class, warnings);
        checkTemplate(changes.metaSummaryPrompt(), "metaSummaryPrompt", MetaSummaryTemplateData.class, warnings);

        if (changes.name() != null) {
            book.setName(changes.name().trim());
        }
        if (changes.description() != null) {
            book.setDescription(changes.description());
        }
        if (changes.language() != null) {
            book.setLanguage(changes.language());
        }
        if (changes.pov() != null) {
            book.setPov(changes.pov());
        }
        if (changes.tense() != null) {
            book.setTense(changes.tense());
        }
        if (changes.style() != null) {
            book.setStyle(changes.style());
        }
        if (changes.masterTemplate() != null) {
            book.setTemplate(changes.masterTemplate());
        }
        if (changes.userPrompt() != null) {
            book.setUserPrompt(changes.userPrompt());
        }
        if (changes.summaryPrompt() != null) {
            book.setSummaryPrompt(changes.summaryPrompt());
        }
        if (changes.metaSummaryPrompt() != null) {
            book.setMetaSummaryPrompt(changes.metaSummaryPrompt());
        }
        return new SettingsSaved(save(book), warnings);
    }

    private void checkTemplate(String template, String templateName, Class<? extends TemplateData> dataClass, List<String> warnings) throws Exception {
        if (StringUtils.isBlank(template)) {
            return;
        }
        TemplateService.ValidationResult result = templateService.isValidTemplate(template, templateName, dataClass);
        if (!result.isValid()) {
            throw new IllegalArgumentException("The template " + templateName + " is not valid: " + result.errorMessage());
        }
        if (result.hasWarnings()) {
            warnings.add("The template " + templateName + " uses names that do not exist: " + String.join(", ", result.unknownNames()));
        }
    }

    @Override
    @CommonTx
    public AgenticCopy createAgenticCopy(Manuscript original) throws Exception {
        // the book of an agent comes as a lazy proxy: its class is not the one the tags and relations are stored by
        Manuscript source = original == null ? null : (Manuscript) Hibernate.unproxy(find(original));
        if (source == null || source.isDeleted()) {
            throw new IllegalArgumentException("The manuscript does not exist");
        }
        if (source.isAgentic()) {
            throw new IllegalArgumentException("A working copy of an agent is not copied");
        }
        // the base is this very backup and the copy is made from it, so what the run is later compared with is what it started from
        JsonObject base = createBackup(source);
        Lorebook originalLorebook = source.getLorebook() == null ? null : lorebookService.find(source.getLorebook());
        if (originalLorebook != null) {
            // the entries have no uuid in the backup format, the base and the copy need them
            base.add("lorebooks", lorebookService.marshalLorebooks(originalLorebook, true));
        }
        return copyFromSnapshot(base, source.getFolder());
    }

    @Override
    @CommonTx
    public AgenticCopy createAgenticCopyFromSnapshot(JsonObject snapshot) throws Exception {
        if (snapshot == null || !snapshot.has("messages") && !snapshot.has("name")) {
            throw new IllegalArgumentException("The snapshot is not the backup of a book");
        }
        // the folder is the book's, which a snapshot does not name: the run puts the copy there, see setAgenticCopyFolder
        return copyFromSnapshot(snapshot, null);
    }

    @Override
    @CommonTx
    public JsonObject snapshotAgenticCopy(Manuscript copy) throws Exception {
        Manuscript source = copy == null ? null : find(copy);
        if (source == null || source.isDeleted()) {
            throw new IllegalArgumentException("The manuscript does not exist");
        }
        if (!source.isAgentic()) {
            throw new IllegalArgumentException("The manuscript " + source.getUuid() + " is not a working copy of an agent");
        }
        JsonObject snapshot = createBackup(source);
        Lorebook lorebook = source.getLorebook() == null ? null : lorebookService.find(source.getLorebook());
        if (lorebook != null) {
            snapshot.add("lorebooks", lorebookService.marshalLorebooks(lorebook, true));
        }
        return snapshot;
    }

    /**
     * The working copy of a backup with its lorebooks ({@code lorebooks}); the backup is also the base that is returned.
     */
    private AgenticCopy copyFromSnapshot(JsonObject base, Folder folder) throws Exception {
        JsonObject lorebookRef = base.has("lorebook") && base.get("lorebook").isJsonObject() ? base.getAsJsonObject("lorebook") : null;
        JsonArray lorebooks = base.has("lorebooks") && base.get("lorebooks").isJsonArray() ? base.getAsJsonArray("lorebooks") : null;

        // the clone would link the lorebook of the user, the copy gets its own; the elements are shared, not copied
        JsonObject cloneSource = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : base.entrySet()) {
            if (!entry.getKey().equals("lorebook")) {
                cloneSource.add(entry.getKey(), entry.getValue());
            }
        }
        cloneSource.addProperty("published", false);

        List<UuidPair> pairs = new ArrayList<>();
        Manuscript copy = cloneFromBackup(cloneSource, pairs);
        copy.setAgentic(true);
        // the copy resolves its settings through the folders of the book it copies
        copy.setFolder(folder);
        if (lorebookRef != null && lorebooks != null && lorebookRef.has("uuid") && !lorebookRef.get("uuid").isJsonNull()) {
            copy.setLorebook(lorebookService.copyLorebooks(lorebooks, pairs).get(lorebookRef.get("uuid").getAsString()));
        }
        return new AgenticCopy(saveWithoutEvent(copy), pairs, base);
    }

    @Override
    @CommonTx
    public void deleteAgenticCopy(Manuscript copy) throws Exception {
        Manuscript stored = copy == null ? null : find(copy);
        if (stored == null) {
            return;
        }
        if (!stored.isAgentic()) {
            throw new IllegalArgumentException("The manuscript " + stored.getUuid() + " is not a working copy of an agent");
        }
        Lorebook lorebook = stored.getLorebook() == null ? null : lorebookService.find(stored.getLorebook());
        if (lorebook != null && !lorebook.isAgentic()) {
            // not the copy's own, never deleted here
            lorebook = null;
        }
        stored.setActiveLeaf(null);
        stored.setLorebook(null);
        stored = saveWithoutEvent(stored);

        chatMessageService.hardDeleteForManuscript(stored);
        tagRelationService.deleteForObject(stored, true);
        delete(stored, true);
        if (lorebook != null) {
            lorebookService.deleteAgenticCopy(lorebook);
        }
    }

    @Override
    @CommonTx
    public void setAgenticCopyModel(Manuscript copy, AI ai, Protocol protocol) throws Exception {
        Manuscript stored = copy == null ? null : find(copy);
        if (stored == null) {
            return;
        }
        if (!stored.isAgentic()) {
            throw new IllegalArgumentException("The manuscript " + stored.getUuid() + " is not a working copy of an agent");
        }
        stored.setAi(ai);
        stored.setProtocol(protocol);
        saveWithoutEvent(stored);
    }

    @Override
    @CommonTx
    public void setAgenticCopyFolder(Manuscript copy, Folder folder) throws Exception {
        Manuscript stored = copy == null ? null : find(copy);
        if (stored == null) {
            return;
        }
        if (!stored.isAgentic()) {
            throw new IllegalArgumentException("The manuscript " + stored.getUuid() + " is not a working copy of an agent");
        }
        Folder target = folder == null ? null : folderService.find(folder.getId());
        if (Objects.equals(stored.getFolder() == null ? null : stored.getFolder().getId(), target == null ? null : target.getId())) {
            return;
        }
        stored.setFolder(target);
        saveWithoutModification(stored);
    }

    @Override
    @CommonTx
    public Manuscript cloneFromBackup(JsonObject backup) throws Exception {
        return cloneFromBackup(backup, null);
    }

    @Override
    @CommonTx
    public Manuscript cloneFromBackup(JsonObject backup, Collection<UuidPair> pairs) throws Exception {
        return cloneFromBackup(backup, pairs, MissingHistory.NONE);
    }

    @Override
    @CommonTx
    public Manuscript cloneFromBackup(JsonObject backup, Collection<UuidPair> pairs, MissingHistory missing) throws Exception {
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
            uuidToNodeMap = restoreMessages(backup.getAsJsonArray("messages"), manuscript, pairs, missing);
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
        agentRunService.checkNotLocked(m);

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
            uuidToNodeMap = restoreMessages(backup.getAsJsonArray("messages"), m, null, MissingHistory.FIRST_VERSION);
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

    /**
     * @param pairs   where the messages and summaries made are reported with the uuids they were made from (the uuids of the
     *                backup), null when nobody wants them
     * @param missing what a part gets of which the backup has no versions
     */
    private Map<String, ChatMessage> restoreMessages(JsonArray messagesArray, Manuscript manuscript, Collection<UuidPair> pairs, MissingHistory missing) throws Exception {
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

            String restoredPrompt = null;
            String restoredLore = null;
            if (msgObj.has("extendedContent") && !msgObj.get("extendedContent").isJsonNull()) {
                JsonElement ext = msgObj.get("extendedContent");
                if (ext.isJsonObject()) {
                    // the prompt of the part is a row of its own, not a part of the extended content
                    JsonObject content = ext.getAsJsonObject().deepCopy();
                    restoredPrompt = takeString(content, MessagePrompt.PROMPT_KEY);
                    restoredLore = takeString(content, MessagePrompt.LORE_KEY);
                    ext = content;
                }
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
                    if (pairs != null) {
                        pairs.add(new UuidPair(AgentRunPairKind.SUMMARY, summary.getUuid(), sumObj.get("uuid").getAsString()));
                    }
                }
            }

            msg = chatMessageService.saveWithoutEvent(msg);
            messagePromptService.store(msg, restoredPrompt, restoredLore);

            if (msgObj.has("annotations") && msgObj.get("annotations").isJsonArray()) {
                annotationService.restoreForPart(msg, msgObj.getAsJsonArray("annotations"), pairs);
            }

            // the history the backup has, else the first version of the text; the part was made of its extended content, its
            // fields are read from it for the versions
            if (missing != MissingHistory.NONE || msgObj.has("revisions")) {
                new ExtendableEntityListener().deserialize(msg);
            }
            if (msgObj.has("revisions") && msgObj.get("revisions").isJsonArray()) {
                messageRevisionService.restoreForPart(msg, msgObj.getAsJsonArray("revisions"));
            } else if (missing == MissingHistory.FIRST_VERSION) {
                messageRevisionService.recordFirst(msg, null);
            } else if (missing == MissingHistory.DUPLICATE) {
                messageRevisionService.recordFirst(msg, RevisionSource.DUPLICATE);
            }

            if (msgObj.has("uuid") && !msgObj.get("uuid").isJsonNull()) {
                String uuid = msgObj.get("uuid").getAsString();
                uuidToNodeMap.put(uuid, msg);
                if (pairs != null) {
                    pairs.add(new UuidPair(AgentRunPairKind.MESSAGE, msg.getUuid(), uuid));
                }
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