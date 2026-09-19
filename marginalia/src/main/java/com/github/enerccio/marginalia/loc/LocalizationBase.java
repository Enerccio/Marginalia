package com.github.enerccio.marginalia.loc;


import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.collections.ReasoningEffort;
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
    public L getAIType(AIType aiType) {
        return aiTypes.get(aiType);
    }

    @Override
    public L getProtocolType(ProtocolType type) {
        return protocolTypes.get(type);
    }

    @Override
    public L getReasoningEffort(ReasoningEffort type) {
        return reasoningEfforts.get(type);
    }
}
