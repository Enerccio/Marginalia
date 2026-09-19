package com.github.enerccio.marginalia.loc;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.collections.ReasoningEffort;

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
}
