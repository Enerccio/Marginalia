package com.github.enerccio.marginalia.loc;


import com.github.enerccio.marginalia.bound.SessionPoint;
import com.github.enerccio.marginalia.domain.collections.*;
import com.github.enerccio.marginalia.domain.model.Setting;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookDecision;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookMatch;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;

import java.text.Collator;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LocalizationImpl implements Localization, InitializingBean {

    private final static Logger log = LoggerFactory.getLogger(LocalizationImpl.class);
    private final Map<Language, LocalizationBundle> bundles = new ConcurrentHashMap<>();
    private Language defaultLanguage = Language.EN;

    @Autowired
    private SessionPoint sessionPoint;

    private final Map<AIType, L> aiTypes = new HashMap<>();
    private final Map<ProtocolType, L> protocolTypes = new HashMap<>();
    private final Map<ReasoningEffort, L> reasoningEfforts = new HashMap<>();
    private final Map<LimitStrategy, L> limitStrategies = new HashMap<>();
    private final Map<ResetStrategy, L> resetStrategies = new HashMap<>();
    private final Map<BackupStrategy, L> backupStrategies = new HashMap<>();
    private final Map<FilteringMode, L> filteringModes = new HashMap<>();
    private final Map<InsertionMode, L> insertionModes = new HashMap<>();
    private final Map<ActivationContext, L> activationContexts = new HashMap<>();
    private final Map<LorebookActivationService.Reason, L> activationReasons = new HashMap<>();
    private final Map<CleanupReference.Policy, L> cleanupPolicies = new HashMap<>();
    private final Map<LorebookMatch, L> lorebookMatches = new HashMap<>();
    private final Map<LorebookDecision, L> lorebookDecisions = new HashMap<>();
    private final Map<PermissionType, L> permissionTypes = new HashMap<>();
    private final Map<RevisionSource, L> revisionSources = new EnumMap<>(RevisionSource.class);
    private final Map<RevisionDetail, L> revisionDetails = new EnumMap<>(RevisionDetail.class);
    private final Map<AgentRunStatus, L> agentRunStatuses = new HashMap<>();
    private final Map<DestructiveCommandMode, L> destructiveModes = new HashMap<>();
    private final Map<Class<?>, L> domainObjects = new HashMap<>();

    public LocalizationImpl() {
        loadMaps();
    }

    public void setBundles(List<LocalizationBundle> bundles) {
        for (LocalizationBundle bundle : bundles) {
            registerBundle(bundle);
        }
    }

    public void setDefaultLanguage(Language defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    /**
     * The default language by its code, from the setting {@code marginalia.default-language}
     * ({@code MARGINALIA_DEFAULT_LANGUAGE}); empty keeps English. It has to be one of the bundles of the application,
     * the languages of extensions are not loaded yet at the start.
     */
    public void setDefaultLanguageCode(String code) {
        if (code != null && !code.isBlank()) {
            this.defaultLanguage = Language.of(code);
        }
    }

    @Override
    public void afterPropertiesSet() {
        LocalizationBundle bundle = bundles.get(defaultLanguage);
        if (bundle == null) {
            throw new IllegalStateException("No localization bundle for the default language " + defaultLanguage
                    + ", the application has " + bundles.keySet() + " (the default language can't be one of an extension)");
        }
        // the other bundles fall back to this one, so it has to be complete
        String missing = missingKeys(bundle);
        if (!missing.isEmpty()) {
            log.error("\n*********** LOCALIZATION IS MISSING! ***********\n"
                    + missing
                    + "\n************************************************\n");
            throw new IllegalStateException("The bundle of the default language " + defaultLanguage + " is incomplete");
        }
    }

    private void loadMaps() {
        aiTypes.put(AIType.OPEN_AI_COMPATIBLE, L.ENUM_AI_TYPE_OPEN_AI_COMPATIBLE);
        aiTypes.put(AIType.EXTENSION, L.ENUM_AI_TYPE_EXTENSION);

        protocolTypes.put(ProtocolType.CHAT_COMPLETION, L.ENUM_PROTOCOL_TYPE_OPEN_CHAT_COMPLETION);

        reasoningEfforts.put(ReasoningEffort.NONE, L.ENUM_REASONING_NONE);
        reasoningEfforts.put(ReasoningEffort.LOW, L.ENUM_REASONING_LOW);
        reasoningEfforts.put(ReasoningEffort.MEDIUM, L.ENUM_REASONING_MEDIUM);
        reasoningEfforts.put(ReasoningEffort.HIGH, L.ENUM_REASONING_HIGH);

        limitStrategies.put(LimitStrategy.UNLIMITED, L.ENUM_LIMIT_UNLIMITED);
        limitStrategies.put(LimitStrategy.TOKEN_LIMIT, L.ENUM_LIMIT_TOKEN_LIMIT);
        limitStrategies.put(LimitStrategy.NOT_AVAILABLE, L.ENUM_LIMIT_NOT_AVAILABLE);

        permissionTypes.put(PermissionType.HELPER, L.ENUM_PERMISSION_HELPER);
        permissionTypes.put(PermissionType.CREATE_AI, L.ENUM_PERMISSION_CREATE_AI);
        permissionTypes.put(PermissionType.AGENTS, L.ENUM_PERMISSION_AGENTS);
        permissionTypes.put(PermissionType.NOTIFICATIONS, L.ENUM_PERMISSION_NOTIFICATIONS);

        revisionSources.put(RevisionSource.GENERATE, L.ENUM_REVISION_SOURCE_GENERATE);
        revisionSources.put(RevisionSource.REGENERATE, L.ENUM_REVISION_SOURCE_REGENERATE);
        revisionSources.put(RevisionSource.REDRAFT, L.ENUM_REVISION_SOURCE_REDRAFT);
        revisionSources.put(RevisionSource.HUMAN, L.ENUM_REVISION_SOURCE_HUMAN);
        revisionSources.put(RevisionSource.PLUGIN, L.ENUM_REVISION_SOURCE_PLUGIN);
        revisionSources.put(RevisionSource.AUTOMATIC, L.LABEL_AUTOMATIC);
        revisionSources.put(RevisionSource.MERGE, L.ENUM_REVISION_SOURCE_MERGE);
        revisionSources.put(RevisionSource.SPLIT, L.ENUM_REVISION_SOURCE_SPLIT);
        revisionSources.put(RevisionSource.DUPLICATE, L.LABEL_COPY);
        revisionSources.put(RevisionSource.RESTORE, L.ENUM_REVISION_SOURCE_RESTORE);
        revisionSources.put(RevisionSource.SNAPSHOT, L.ENUM_REVISION_SOURCE_SNAPSHOT);

        revisionDetails.put(RevisionDetail.COMMAND, L.LABEL_COMMAND);
        revisionDetails.put(RevisionDetail.AGENT_MERGE, L.LABEL_AGENT_MERGE);
        revisionDetails.put(RevisionDetail.REPLACE, L.LABEL_REPLACE);
        revisionDetails.put(RevisionDetail.BRANCH, L.LABEL_BRANCH);
        revisionDetails.put(RevisionDetail.ARCHIVE_IMPORT, L.LABEL_IMPORT_FROM_ARCHIVE);
        revisionDetails.put(RevisionDetail.SILLYTAVERN_IMPORT, L.LABEL_IMPORT_FROM_SILLYTAVERN);

        agentRunStatuses.put(AgentRunStatus.RUNNING, L.ENUM_AGENT_RUN_RUNNING);
        agentRunStatuses.put(AgentRunStatus.IDLE, L.ENUM_AGENT_RUN_IDLE);
        agentRunStatuses.put(AgentRunStatus.WAITING_APPROVAL, L.ENUM_AGENT_RUN_WAITING_APPROVAL);
        agentRunStatuses.put(AgentRunStatus.PAUSING, L.ENUM_AGENT_RUN_PAUSING);
        agentRunStatuses.put(AgentRunStatus.PAUSED, L.ENUM_AGENT_RUN_PAUSED);
        agentRunStatuses.put(AgentRunStatus.STOPPING, L.ENUM_AGENT_RUN_STOPPING);
        agentRunStatuses.put(AgentRunStatus.STOPPED_KEPT, L.ENUM_AGENT_RUN_STOPPED_KEPT);
        agentRunStatuses.put(AgentRunStatus.STOPPED_DISCARDED, L.ENUM_AGENT_RUN_STOPPED_DISCARDED);
        agentRunStatuses.put(AgentRunStatus.DONE, L.ENUM_AGENT_RUN_DONE);
        agentRunStatuses.put(AgentRunStatus.FAILED, L.ENUM_AGENT_RUN_FAILED);
        agentRunStatuses.put(AgentRunStatus.APPLIED, L.ENUM_AGENT_RUN_APPLIED);

        destructiveModes.put(DestructiveCommandMode.AUTO, L.ENUM_DESTRUCTIVE_MODE_AUTO);
        destructiveModes.put(DestructiveCommandMode.ASK, L.ENUM_DESTRUCTIVE_MODE_ASK);
        destructiveModes.put(DestructiveCommandMode.DENY, L.ENUM_DESTRUCTIVE_MODE_DENY);

        resetStrategies.put(ResetStrategy.DAILY, L.ENUM_RESET_DAILY);
        resetStrategies.put(ResetStrategy.WEEKLY, L.ENUM_RESET_WEEKLY);
        resetStrategies.put(ResetStrategy.MONTHLY, L.ENUM_RESET_MONTHLY);

        backupStrategies.put(BackupStrategy.DISABLED, L.ENUM_BACKUP_STRATEGY_NONE);
        backupStrategies.put(BackupStrategy.AFTER_N_MESSAGES, L.ENUM_BACKUP_STRATEGY_AFTER_N_MESSAGES);
        backupStrategies.put(BackupStrategy.AFTER_N_MINUTES, L.ENUM_BACKUP_STRATEGY_AFTER_N_MINUTES);

        filteringModes.put(FilteringMode.TEXT, L.ENUM_FILTERING_MODE_TEXT);
        filteringModes.put(FilteringMode.REGEX, L.ENUM_FILTERING_MODE_REGEX);
        filteringModes.put(FilteringMode.KEYWORDS_OR, L.ENUM_FILTERING_MODE_KEYWORDS_OR);
        filteringModes.put(FilteringMode.KEYWORDS_AND, L.ENUM_FILTERING_MODE_KEYWORDS_AND);

        insertionModes.put(InsertionMode.IN_LORE_BLOCK, L.ENUM_INSERTION_MODE_IN_LORE_BLOCK);
        insertionModes.put(InsertionMode.BEFORE_USER_PROMPT, L.ENUM_INSERTION_MODE_BEFORE_USER_PROMPT);

        activationContexts.put(ActivationContext.INSTRUCTIONS, L.LABEL_INSTRUCTIONS);
        activationContexts.put(ActivationContext.INCLUDE_PARTS, L.ENUM_ACTIVATION_CONTEXT_INCLUDE_PARTS);

        for (LorebookActivationService.Reason reason : LorebookActivationService.Reason.values()) {
            activationReasons.put(reason, L.valueOf("ENUM_ACTIVATION_REASON_" + reason.name()));
        }

        cleanupPolicies.put(CleanupReference.Policy.STRONG, L.ENUM_CLEANUP_POLICY_STRONG);
        cleanupPolicies.put(CleanupReference.Policy.OWNED_BY, L.ENUM_CLEANUP_POLICY_OWNED_BY);
        cleanupPolicies.put(CleanupReference.Policy.OWNS, L.ENUM_CLEANUP_POLICY_OWNS);
        cleanupPolicies.put(CleanupReference.Policy.WEAK, L.ENUM_CLEANUP_POLICY_WEAK);

        lorebookMatches.put(LorebookMatch.EXISTING, L.ENUM_LOREBOOK_MATCH_EXISTING);
        lorebookMatches.put(LorebookMatch.SAME_NAME, L.ENUM_LOREBOOK_MATCH_SAME_NAME);
        lorebookMatches.put(LorebookMatch.NOT_FOUND, L.ENUM_LOREBOOK_MATCH_NOT_FOUND);

        lorebookDecisions.put(LorebookDecision.LINK, L.ENUM_LOREBOOK_DECISION_LINK);
        lorebookDecisions.put(LorebookDecision.CREATE, L.ENUM_LOREBOOK_DECISION_CREATE);
        lorebookDecisions.put(LorebookDecision.SKIP, L.ENUM_LOREBOOK_DECISION_SKIP);

        domainObjects.put(Manuscript.class, L.LABEL_BOOK);
        domainObjects.put(Lorebook.class, L.LABEL_LOREBOOK);
        domainObjects.put(LorebookEntry.class, L.ENUM_DOMAIN_LOREBOOK_ENTRY);
        domainObjects.put(ChatMessage.class, L.ENUM_DOMAIN_MESSAGE);
        domainObjects.put(Summary.class, L.LABEL_SUMMARY);
        domainObjects.put(Annotation.class, L.ENUM_DOMAIN_ANNOTATION);
        domainObjects.put(AI.class, L.LABEL_MODEL);
        domainObjects.put(Protocol.class, L.ENUM_DOMAIN_PROTOCOL);
        domainObjects.put(Agent.class, L.ENUM_DOMAIN_AGENT);
        domainObjects.put(AgentRun.class, L.ENUM_DOMAIN_AGENT_RUN);
        domainObjects.put(Tag.class, L.ENUM_DOMAIN_TAG);
        domainObjects.put(TagRelation.class, L.ENUM_DOMAIN_TAG_RELATION);
        domainObjects.put(Resource.class, L.ENUM_DOMAIN_RESOURCE);
        domainObjects.put(Setting.class, L.ENUM_DOMAIN_SETTING);
        domainObjects.put(User.class, L.LABEL_USER);
    }

    private static String missingKeys(LocalizationBundle bundle) {
        StringBuilder missing = new StringBuilder();
        for (L key : L.values()) {
            if (bundle.getValue(key) == null) {
                missing.append(key.name()).append('\n');
            }
        }
        return missing.toString();
    }

    @Override
    public String getValue(Language language, L l) {
        LocalizationBundle bundle = language == null ? null : bundles.get(language);
        String text = bundle == null ? null : bundle.getValue(l);
        if (text == null) {
            LocalizationBundle fallback = bundles.get(defaultLanguage);
            text = fallback == null ? null : fallback.getValue(l);
        }
        return text == null ? "NOT LOCALIZED!" : text;
    }

    @Override
    public String getValue(L l) {
        String text = currentBundle().getValue(l);
        if (text == null) {
            LocalizationBundle fallback = bundles.get(defaultLanguage);
            text = fallback == null ? null : fallback.getValue(l);
        }
        if (text == null) {
            log.error("Key {} is not localized!", l.name());
            return "NOT LOCALIZED!";
        }
        return text;
    }

    @Override
    public L parseValue(String text) {
        for(Map.Entry<L, String> entry : currentBundle().getMessages().entrySet()) {
            if(entry.getValue().equals(text)) {
                return entry.getKey();
            }
        }

        return null;
    }

    @Override
    public Locale getLocale() {
        return currentBundle().getLocale();
    }

    @Override
    public DateFormat getDateFormat() {
        return currentBundle().getDateFormat();
    }

    @Override
    public DateFormat getHourFormat() {
        return currentBundle().getHourFormat();
    }

    @Override
    public DateFormat getDateHourFormat() {
        return currentBundle().getDateHourFormat();
    }

    @Override
    public Language getDefaultLanguage() {
        return defaultLanguage;
    }

    @Override
    public Language getCurrentLanguage() {
        Language language = sessionLanguage();
        return language != null && bundles.containsKey(language) ? language : defaultLanguage;
    }

    @Override
    public List<Language> getAvailableLanguages() {
        return bundles.values().stream()
                .sorted(Comparator.comparing(LocalizationBundle::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .map(LocalizationBundle::getLanguage)
                .toList();
    }

    @Override
    public String getDisplayName(Language language) {
        LocalizationBundle bundle = bundles.get(language);
        return bundle == null ? language.code() : bundle.getDisplayName();
    }

    @Override
    public Language resolveLanguage(String code) {
        if (code == null || code.isBlank()) {
            return defaultLanguage;
        }
        Language language = Language.of(code);
        return bundles.containsKey(language) ? language : defaultLanguage;
    }

    @Override
    public void registerBundle(LocalizationBundle bundle) {
        Language language = bundle.getLanguage();
        if (bundles.putIfAbsent(language, bundle) != null) {
            throw new IllegalArgumentException("Language " + language + " already has a localization bundle");
        }
        if (!language.equals(defaultLanguage)) {
            String missing = missingKeys(bundle);
            if (!missing.isEmpty()) {
                log.warn("Localization bundle {} lacks texts, the {} ones are used instead:\n{}", language,
                        defaultLanguage, missing);
            }
        }
    }

    @Override
    public void unregisterBundle(LocalizationBundle bundle) {
        Language language = bundle.getLanguage();
        if (!language.equals(defaultLanguage)) {
            bundles.remove(language, bundle);
        }
    }

    private LocalizationBundle currentBundle() {
        return bundles.get(getCurrentLanguage());
    }

    /**
     * Language stored in the session, {@code null} when there is no session to ask: the session scoped bean can be
     * used only in a request (or in a thread that got its attributes, see {@code ThreadCopyRequestAttributes}).
     */
    private Language sessionLanguage() {
        if (sessionPoint == null || RequestContextHolder.getRequestAttributes() == null) {
            return null;
        }
        try {
            return sessionPoint.getLanguage();
        } catch (RuntimeException e) {
            // the session is gone
            log.trace("No session to read the language from: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Comparator<String> createNaturalLanguageComparator() {
        return new NaturalOrderComparator(Collator.getInstance(getLocale()));
    }

    @Override
    public Comparator<String> createLocaleComparator() {
        Collator collator = Collator.getInstance(getLocale());
        return collator::compare;
    }

    @Override
    public L getAIType(AIType type) {
        return aiTypes.get(type);
    }

    @Override
    public L getProtocolType(ProtocolType type) {
        return protocolTypes.get(type);
    }

    @Override
    public L getReasoningEffort(ReasoningEffort type) {
        return reasoningEfforts.get(type);
    }
    @Override
    public L getLimitStrategy(LimitStrategy type) {
        return limitStrategies.get(type);
    }

    @Override
    public L getPermissionType(PermissionType type) {
        return permissionTypes.get(type);
    }

    @Override
    public L getRevisionSource(RevisionSource source) {
        return revisionSources.get(source);
    }

    @Override
    public L getRevisionDetail(RevisionDetail detail) {
        return revisionDetails.get(detail);
    }

    @Override
    public L getAgentRunStatus(AgentRunStatus type) {
        return agentRunStatuses.get(type);
    }

    @Override
    public L getDestructiveCommandMode(DestructiveCommandMode type) {
        return destructiveModes.get(type);
    }

    @Override
    public L getResetStrategy(ResetStrategy type) {
        return resetStrategies.get(type);
    }

    @Override
    public L getBackupStrategy(BackupStrategy type) {
        return backupStrategies.get(type);
    }

    @Override
    public L getFilteringMode(FilteringMode type) {
        return filteringModes.get(type);
    }

    @Override
    public L getInsertionMode(InsertionMode type) {
        return insertionModes.get(type);
    }

    @Override
    public L getActivationContext(ActivationContext type) {
        return activationContexts.get(type);
    }

    @Override
    public L getActivationReason(LorebookActivationService.Reason type) {
        return activationReasons.get(type);
    }

    @Override
    public L getCleanupPolicy(CleanupReference.Policy type) {
        return cleanupPolicies.get(type);
    }

    @Override
    public L getLorebookMatch(LorebookMatch type) {
        return lorebookMatches.get(type);
    }

    @Override
    public L getLorebookDecision(LorebookDecision type) {
        return lorebookDecisions.get(type);
    }

    @Override
    public String localizeDomainObject(Class<?> clazz) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            L key = domainObjects.get(c);
            if (key != null) {
                return getValue(key);
            }
        }
        return clazz == null ? "" : clazz.getSimpleName();
    }
}
