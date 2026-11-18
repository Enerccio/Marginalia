package com.github.enerccio.marginalia.loc;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.*;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookDecision;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookMatch;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The localization of the application: the platform's {@link LocalizationImpl} with the texts of {@link M_L} (the
 * {@code texts} of the bean), the labels of the story enums and
 * entities and the built-in prompts.
 */
public class MarginaliaLocalizationImpl extends LocalizationImpl implements MarginaliaLocalization {

    private final Map<BackupStrategy, LocalizationKey> backupStrategies = new HashMap<>();
    private final Map<FilteringMode, LocalizationKey> filteringModes = new HashMap<>();
    private final Map<InsertionMode, LocalizationKey> insertionModes = new HashMap<>();
    private final Map<ActivationContext, LocalizationKey> activationContexts = new HashMap<>();
    private final Map<LorebookActivationService.Reason, LocalizationKey> activationReasons = new HashMap<>();
    private final Map<LorebookMatch, LocalizationKey> lorebookMatches = new HashMap<>();
    private final Map<LorebookDecision, LocalizationKey> lorebookDecisions = new HashMap<>();
    private final Map<RevisionSource, LocalizationKey> revisionSources = new EnumMap<>(RevisionSource.class);
    private final Map<RevisionDetail, LocalizationKey> revisionDetails = new EnumMap<>(RevisionDetail.class);
    private final Map<AgentRunStatus, LocalizationKey> agentRunStatuses = new HashMap<>();
    private final Map<DestructiveCommandMode, LocalizationKey> destructiveModes = new HashMap<>();

    public MarginaliaLocalizationImpl() {
        revisionSources.put(RevisionSource.GENERATE, M_L.ENUM_REVISION_SOURCE_GENERATE);
        revisionSources.put(RevisionSource.REGENERATE, M_L.ENUM_REVISION_SOURCE_REGENERATE);
        revisionSources.put(RevisionSource.REDRAFT, M_L.ENUM_REVISION_SOURCE_REDRAFT);
        revisionSources.put(RevisionSource.HUMAN, M_L.ENUM_REVISION_SOURCE_HUMAN);
        revisionSources.put(RevisionSource.PLUGIN, M_L.ENUM_REVISION_SOURCE_PLUGIN);
        revisionSources.put(RevisionSource.AUTOMATIC, L.LABEL_AUTOMATIC);
        revisionSources.put(RevisionSource.MERGE, M_L.ENUM_REVISION_SOURCE_MERGE);
        revisionSources.put(RevisionSource.SPLIT, M_L.ENUM_REVISION_SOURCE_SPLIT);
        revisionSources.put(RevisionSource.DUPLICATE, L.LABEL_COPY);
        revisionSources.put(RevisionSource.RESTORE, M_L.ENUM_REVISION_SOURCE_RESTORE);
        revisionSources.put(RevisionSource.SNAPSHOT, M_L.ENUM_REVISION_SOURCE_SNAPSHOT);

        revisionDetails.put(RevisionDetail.COMMAND, M_L.LABEL_COMMAND);
        revisionDetails.put(RevisionDetail.AGENT_MERGE, M_L.LABEL_AGENT_MERGE);
        revisionDetails.put(RevisionDetail.REPLACE, M_L.LABEL_REPLACE);
        revisionDetails.put(RevisionDetail.BRANCH, M_L.LABEL_BRANCH);
        revisionDetails.put(RevisionDetail.ARCHIVE_IMPORT, M_L.LABEL_IMPORT_FROM_ARCHIVE);
        revisionDetails.put(RevisionDetail.SILLYTAVERN_IMPORT, M_L.LABEL_IMPORT_FROM_SILLYTAVERN);
        revisionDetails.put(RevisionDetail.LMSTUDIO_IMPORT, M_L.LABEL_IMPORT_FROM_LMSTUDIO);
        revisionDetails.put(RevisionDetail.DOCUMENT_IMPORT, M_L.LABEL_IMPORT_FROM_DOCUMENT);
        revisionDetails.put(RevisionDetail.TRANSLATION, M_L.LABEL_TRANSLATION);

        agentRunStatuses.put(AgentRunStatus.RUNNING, M_L.ENUM_AGENT_RUN_RUNNING);
        agentRunStatuses.put(AgentRunStatus.IDLE, M_L.ENUM_AGENT_RUN_IDLE);
        agentRunStatuses.put(AgentRunStatus.WAITING_APPROVAL, M_L.ENUM_AGENT_RUN_WAITING_APPROVAL);
        agentRunStatuses.put(AgentRunStatus.PAUSING, M_L.ENUM_AGENT_RUN_PAUSING);
        agentRunStatuses.put(AgentRunStatus.PAUSED, M_L.ENUM_AGENT_RUN_PAUSED);
        agentRunStatuses.put(AgentRunStatus.STOPPING, M_L.ENUM_AGENT_RUN_STOPPING);
        agentRunStatuses.put(AgentRunStatus.STOPPED_KEPT, M_L.ENUM_AGENT_RUN_STOPPED_KEPT);
        agentRunStatuses.put(AgentRunStatus.STOPPED_DISCARDED, M_L.ENUM_AGENT_RUN_STOPPED_DISCARDED);
        agentRunStatuses.put(AgentRunStatus.DONE, M_L.ENUM_AGENT_RUN_DONE);
        agentRunStatuses.put(AgentRunStatus.FAILED, M_L.ENUM_AGENT_RUN_FAILED);
        agentRunStatuses.put(AgentRunStatus.APPLIED, M_L.ENUM_AGENT_RUN_APPLIED);

        destructiveModes.put(DestructiveCommandMode.AUTO, M_L.ENUM_DESTRUCTIVE_MODE_AUTO);
        destructiveModes.put(DestructiveCommandMode.ASK, M_L.ENUM_DESTRUCTIVE_MODE_ASK);
        destructiveModes.put(DestructiveCommandMode.DENY, M_L.ENUM_DESTRUCTIVE_MODE_DENY);

        backupStrategies.put(BackupStrategy.DISABLED, M_L.ENUM_BACKUP_STRATEGY_NONE);
        backupStrategies.put(BackupStrategy.AFTER_N_MESSAGES, M_L.ENUM_BACKUP_STRATEGY_AFTER_N_MESSAGES);
        backupStrategies.put(BackupStrategy.AFTER_N_MINUTES, M_L.ENUM_BACKUP_STRATEGY_AFTER_N_MINUTES);

        filteringModes.put(FilteringMode.TEXT, M_L.ENUM_FILTERING_MODE_TEXT);
        filteringModes.put(FilteringMode.REGEX, M_L.ENUM_FILTERING_MODE_REGEX);
        filteringModes.put(FilteringMode.KEYWORDS_OR, M_L.ENUM_FILTERING_MODE_KEYWORDS_OR);
        filteringModes.put(FilteringMode.KEYWORDS_AND, M_L.ENUM_FILTERING_MODE_KEYWORDS_AND);

        insertionModes.put(InsertionMode.IN_LORE_BLOCK, M_L.ENUM_INSERTION_MODE_IN_LORE_BLOCK);
        insertionModes.put(InsertionMode.BEFORE_USER_PROMPT, M_L.ENUM_INSERTION_MODE_BEFORE_USER_PROMPT);

        activationContexts.put(ActivationContext.INSTRUCTIONS, M_L.LABEL_INSTRUCTIONS);
        activationContexts.put(ActivationContext.INCLUDE_PARTS, M_L.ENUM_ACTIVATION_CONTEXT_INCLUDE_PARTS);

        for (LorebookActivationService.Reason reason : LorebookActivationService.Reason.values()) {
            activationReasons.put(reason, M_L.valueOf("ENUM_ACTIVATION_REASON_" + reason.name()));
        }

        lorebookMatches.put(LorebookMatch.EXISTING, M_L.ENUM_LOREBOOK_MATCH_EXISTING);
        lorebookMatches.put(LorebookMatch.SAME_NAME, M_L.ENUM_LOREBOOK_MATCH_SAME_NAME);
        lorebookMatches.put(LorebookMatch.NOT_FOUND, M_L.ENUM_LOREBOOK_MATCH_NOT_FOUND);

        lorebookDecisions.put(LorebookDecision.LINK, M_L.ENUM_LOREBOOK_DECISION_LINK);
        lorebookDecisions.put(LorebookDecision.CREATE, M_L.ENUM_LOREBOOK_DECISION_CREATE);
        lorebookDecisions.put(LorebookDecision.SKIP, M_L.ENUM_LOREBOOK_DECISION_SKIP);

        putDomainObject(Manuscript.class, L.LABEL_BOOK);
        putDomainObject(Lorebook.class, M_L.LABEL_LOREBOOK);
        putDomainObject(LorebookEntry.class, M_L.ENUM_DOMAIN_LOREBOOK_ENTRY);
        putDomainObject(ChatMessage.class, M_L.ENUM_DOMAIN_MESSAGE);
        putDomainObject(Summary.class, L.LABEL_SUMMARY);
        putDomainObject(Annotation.class, M_L.ENUM_DOMAIN_ANNOTATION);
        putDomainObject(AnchoredPassage.class, M_L.ENUM_DOMAIN_ANNOTATION);
        putDomainObject(MessageRevision.class, M_L.ENUM_DOMAIN_MESSAGE_REVISION);
        putDomainObject(Agent.class, M_L.ENUM_DOMAIN_AGENT);
        putDomainObject(AgentRun.class, M_L.ENUM_DOMAIN_AGENT_RUN);
    }

    @Override
    protected List<Class<? extends LocalizationKey>> keyEnums() {
        return List.of(L.class, M_L.class);
    }

    @Override
    protected List<Language> promptLanguages() {
        return Defaults.all().stream().map(Defaults::getLanguage).toList();
    }

    @Override
    public Defaults getDefaults() {
        return getDefaults(getPromptLanguage());
    }

    @Override
    public Defaults getDefaults(Language language) {
        Defaults english = null;
        for (Defaults texts : Defaults.all()) {
            if (texts.getLanguage().equals(language)) {
                return texts;
            }
            if (texts.getLanguage().equals(Language.EN)) {
                english = texts;
            }
        }
        return english;
    }

    @Override
    public List<Defaults> getAllDefaults() {
        return Defaults.all();
    }

    @Override
    public void registerDefaults(Defaults texts) {
        Defaults.register(texts);
    }

    @Override
    public void unregisterDefaults(Defaults texts) {
        Defaults.unregister(texts);
    }

    @Override
    public LocalizationKey getRevisionSource(RevisionSource source) {
        return revisionSources.get(source);
    }

    @Override
    public LocalizationKey getRevisionDetail(RevisionDetail detail) {
        return revisionDetails.get(detail);
    }

    @Override
    public LocalizationKey getAgentRunStatus(AgentRunStatus type) {
        return agentRunStatuses.get(type);
    }

    @Override
    public LocalizationKey getDestructiveCommandMode(DestructiveCommandMode type) {
        return destructiveModes.get(type);
    }

    @Override
    public LocalizationKey getBackupStrategy(BackupStrategy type) {
        return backupStrategies.get(type);
    }

    @Override
    public LocalizationKey getFilteringMode(FilteringMode type) {
        return filteringModes.get(type);
    }

    @Override
    public LocalizationKey getInsertionMode(InsertionMode type) {
        return insertionModes.get(type);
    }

    @Override
    public LocalizationKey getActivationContext(ActivationContext type) {
        return activationContexts.get(type);
    }

    @Override
    public LocalizationKey getActivationReason(LorebookActivationService.Reason type) {
        return activationReasons.get(type);
    }

    @Override
    public LocalizationKey getLorebookMatch(LorebookMatch type) {
        return lorebookMatches.get(type);
    }

    @Override
    public LocalizationKey getLorebookDecision(LorebookDecision type) {
        return lorebookDecisions.get(type);
    }
}
