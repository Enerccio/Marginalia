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
 * The localization of the application: the platform's {@link LocalizationImpl} with the labels of the story enums and
 * entities and the built-in prompts.
 */
public class StoryLocalizationImpl extends LocalizationImpl implements StoryLocalization {

    private final Map<BackupStrategy, L> backupStrategies = new HashMap<>();
    private final Map<FilteringMode, L> filteringModes = new HashMap<>();
    private final Map<InsertionMode, L> insertionModes = new HashMap<>();
    private final Map<ActivationContext, L> activationContexts = new HashMap<>();
    private final Map<LorebookActivationService.Reason, L> activationReasons = new HashMap<>();
    private final Map<LorebookMatch, L> lorebookMatches = new HashMap<>();
    private final Map<LorebookDecision, L> lorebookDecisions = new HashMap<>();
    private final Map<RevisionSource, L> revisionSources = new EnumMap<>(RevisionSource.class);
    private final Map<RevisionDetail, L> revisionDetails = new EnumMap<>(RevisionDetail.class);
    private final Map<AgentRunStatus, L> agentRunStatuses = new HashMap<>();
    private final Map<DestructiveCommandMode, L> destructiveModes = new HashMap<>();

    public StoryLocalizationImpl() {
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
        revisionDetails.put(RevisionDetail.LMSTUDIO_IMPORT, L.LABEL_IMPORT_FROM_LMSTUDIO);
        revisionDetails.put(RevisionDetail.DOCUMENT_IMPORT, L.LABEL_IMPORT_FROM_DOCUMENT);
        revisionDetails.put(RevisionDetail.TRANSLATION, L.LABEL_TRANSLATION);

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

        lorebookMatches.put(LorebookMatch.EXISTING, L.ENUM_LOREBOOK_MATCH_EXISTING);
        lorebookMatches.put(LorebookMatch.SAME_NAME, L.ENUM_LOREBOOK_MATCH_SAME_NAME);
        lorebookMatches.put(LorebookMatch.NOT_FOUND, L.ENUM_LOREBOOK_MATCH_NOT_FOUND);

        lorebookDecisions.put(LorebookDecision.LINK, L.ENUM_LOREBOOK_DECISION_LINK);
        lorebookDecisions.put(LorebookDecision.CREATE, L.ENUM_LOREBOOK_DECISION_CREATE);
        lorebookDecisions.put(LorebookDecision.SKIP, L.ENUM_LOREBOOK_DECISION_SKIP);

        putDomainObject(Manuscript.class, L.LABEL_BOOK);
        putDomainObject(Lorebook.class, L.LABEL_LOREBOOK);
        putDomainObject(LorebookEntry.class, L.ENUM_DOMAIN_LOREBOOK_ENTRY);
        putDomainObject(ChatMessage.class, L.ENUM_DOMAIN_MESSAGE);
        putDomainObject(Summary.class, L.LABEL_SUMMARY);
        putDomainObject(Annotation.class, L.ENUM_DOMAIN_ANNOTATION);
        putDomainObject(AnchoredPassage.class, L.ENUM_DOMAIN_ANNOTATION);
        putDomainObject(MessageRevision.class, L.ENUM_DOMAIN_MESSAGE_REVISION);
        putDomainObject(Agent.class, L.ENUM_DOMAIN_AGENT);
        putDomainObject(AgentRun.class, L.ENUM_DOMAIN_AGENT_RUN);
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
    public L getLorebookMatch(LorebookMatch type) {
        return lorebookMatches.get(type);
    }

    @Override
    public L getLorebookDecision(LorebookDecision type) {
        return lorebookDecisions.get(type);
    }
}
