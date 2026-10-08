package com.github.enerccio.marginalia.loc;


import com.github.enerccio.marginalia.domain.collections.*;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookDecision;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookMatch;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class LocalizationBase implements Localization {

    private final static Logger log = LoggerFactory.getLogger(LocalizationBase.class);
    private final Map<L, String> messages = new HashMap<>();

    private final Map<AIType, L> aiTypes = new HashMap<>();
    private final Map<ProtocolType, L> protocolTypes = new HashMap<>();
    private final Map<ReasoningEffort, L> reasoningEfforts = new HashMap<>();
    private final Map<BackupStrategy, L> backupStrategies = new HashMap<>();
    private final Map<FilteringMode, L> filteringModes = new HashMap<>();
    private final Map<InsertionMode, L> insertionModes = new HashMap<>();
    private final Map<CleanupReference.Policy, L> cleanupPolicies = new HashMap<>();
    private final Map<LorebookMatch, L> lorebookMatches = new HashMap<>();
    private final Map<LorebookDecision, L> lorebookDecisions = new HashMap<>();

    protected abstract void loadMessages();

    public LocalizationBase() {
        loadMessages();
        loadMaps();

        checkLocalization();
    }

    private void loadMaps() {
        aiTypes.put(AIType.OPEN_AI_COMPATIBLE, L.ENUM_AI_TYPE_OPEN_AI_COMPATIBLE);

        protocolTypes.put(ProtocolType.CHAT_COMPLETION, L.ENUM_PROTOCOL_TYPE_OPEN_CHAT_COMPLETION);

        reasoningEfforts.put(ReasoningEffort.NONE, L.ENUM_REASONING_NONE);
        reasoningEfforts.put(ReasoningEffort.LOW, L.ENUM_REASONING_LOW);
        reasoningEfforts.put(ReasoningEffort.MEDIUM, L.ENUM_REASONING_MEDIUM);
        reasoningEfforts.put(ReasoningEffort.HIGH, L.ENUM_REASONING_HIGH);

        backupStrategies.put(BackupStrategy.DISABLED, L.ENUM_BACKUP_STRATEGY_NONE);
        backupStrategies.put(BackupStrategy.AFTER_N_MESSAGES, L.ENUM_BACKUP_STRATEGY_AFTER_N_MESSAGES);
        backupStrategies.put(BackupStrategy.AFTER_N_MINUTES, L.ENUM_BACKUP_STRATEGY_AFTER_N_MINUTES);

        filteringModes.put(FilteringMode.TEXT, L.ENUM_FILTERING_MODE_TEXT);
        filteringModes.put(FilteringMode.REGEX, L.ENUM_FILTERING_MODE_REGEX);

        insertionModes.put(InsertionMode.IN_LORE_BLOCK, L.ENUM_INSERTION_MODE_IN_LORE_BLOCK);
        insertionModes.put(InsertionMode.BEFORE_USER_PROMPT, L.ENUM_INSERTION_MODE_BEFORE_USER_PROMPT);

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
    }

    protected void checkLocalization() {
        L[] keys = L.values();
        StringBuilder missingKeys = new StringBuilder();

        for(L key : keys) {
            if(!messages.containsKey(key)) {
                missingKeys.append(key.name());
                missingKeys.append("\n");
            }
        }

        evaluateMissingLocalization(missingKeys);
    }

    protected void evaluateMissingLocalization(StringBuilder missingKeys) {
        String missing = missingKeys.toString();

        if(!missing.isEmpty()) {
            if(log.isErrorEnabled()) {
                log.error("\n*********** LOCALIZATION IS MISSING! ***********\n"
                        + missing
                        + "\n************************************************\n");
            }
            else {
                System.err.println("\n*********** LOCALIZATION IS MISSING! ***********");
                System.err.println(missing);
                System.err.println("************************************************\n");
            }

            throw new IllegalStateException();
        }
    }

    @Override
    public String getValue(L l) {
        if (messages.containsKey(l)) {
            return messages.get(l);
        } else {
            log.error("Key {} is not localized!", l.name());
            return "NOT LOCALIZED!";
        }
    }

    public void setValue(L l, String caption) {
        messages.put(l, caption);
    }

    @Override
    public L parseValue(String text) {
        for(Map.Entry<L, String> entry : messages.entrySet()) {
            if(entry.getValue().equals(text)) {
                return entry.getKey();
            }
        }

        return null;
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public DateFormat getDateFormat() {
        return new SimpleDateFormat("dd.MM.yyyy", getLocale());
    }

    @Override
    public DateFormat getHourFormat() {
        return new SimpleDateFormat("HH:mm:ss", getLocale());
    }

    @Override
    public DateFormat getDateHourFormat() {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", getLocale());
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
}
