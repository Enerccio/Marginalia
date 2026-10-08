package com.github.enerccio.marginalia.loc;

import com.github.enerccio.marginalia.domain.collections.*;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookDecision;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookMatch;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;

import java.text.DateFormat;
import java.util.Comparator;
import java.util.Locale;

public interface Localization {

    String getValue(L l);
    void setValue(L l, String caption);
    L parseValue(String text);
    Locale getLocale();
    DateFormat getDateFormat();
    DateFormat getHourFormat();
    DateFormat getDateHourFormat();
    Comparator<String> createNaturalLanguageComparator();
    Comparator<String> createLocaleComparator();

    L getAIType(AIType aiType);
    L getProtocolType(ProtocolType t);
    L getReasoningEffort(ReasoningEffort t);
    L getBackupStrategy(BackupStrategy t);
    L getFilteringMode(FilteringMode t);
    L getInsertionMode(InsertionMode t);
    L getCleanupPolicy(CleanupReference.Policy t);
    L getLorebookMatch(LorebookMatch t);
    L getLorebookDecision(LorebookDecision t);
}
