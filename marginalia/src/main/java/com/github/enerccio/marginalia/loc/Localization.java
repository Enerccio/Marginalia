package com.github.enerccio.marginalia.loc;

import com.github.enerccio.marginalia.domain.collections.*;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookDecision;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookMatch;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;

import java.text.DateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Texts and formats of the UI in the language of the current session ({@code SessionPoint}), the default language
 * when there is no session (startup, scheduled tasks). The texts come from {@link LocalizationBundle}s, one per
 * {@link Language}.
 */
public interface Localization {

    /**
     * The text in the language of the current session; a text its bundle doesn't have is taken from the default
     * language.
     */
    String getValue(L l);
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
    L getLimitStrategy(LimitStrategy t);
    L getResetStrategy(ResetStrategy t);
    L getBackupStrategy(BackupStrategy t);
    L getFilteringMode(FilteringMode t);
    L getInsertionMode(InsertionMode t);
    L getCleanupPolicy(CleanupReference.Policy t);
    L getLorebookMatch(LorebookMatch t);
    L getLorebookDecision(LorebookDecision t);
    L getPermissionType(PermissionType t);

    /**
     * Localized name of the kind of object an entity class stands for (book, lorebook...), subclasses use the name of
     * their closest known superclass.
     */
    String localizeDomainObject(Class<?> clazz);

    /**
     * Language that is used when the session has none, or has one without a bundle.
     */
    Language getDefaultLanguage();

    /**
     * Language of the current session, always one that has a bundle.
     */
    Language getCurrentLanguage();

    /**
     * Languages that have a bundle, sorted by their names.
     */
    List<Language> getAvailableLanguages();

    /**
     * Name of the language in the language itself, the code when it has no bundle.
     */
    String getDisplayName(Language language);

    /**
     * The language with the code when it has a bundle, otherwise the default language.
     */
    Language resolveLanguage(String code);

    /**
     * Adds a language.
     *
     * @throws IllegalArgumentException when the language already has a bundle
     */
    void registerBundle(LocalizationBundle bundle);

    /**
     * Removes the bundle if it is the registered one of its language. The default language can't be removed.
     */
    void unregisterBundle(LocalizationBundle bundle);
}
