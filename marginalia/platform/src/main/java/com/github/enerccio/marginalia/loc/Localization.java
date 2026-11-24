package com.github.enerccio.marginalia.loc;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.collections.LimitStrategy;
import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.collections.ReasoningEffort;
import com.github.enerccio.marginalia.domain.collections.ReasoningPassthrough;
import com.github.enerccio.marginalia.domain.collections.ResetStrategy;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;

import java.text.DateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Texts and formats of the UI in the language of the current session ({@code SessionPoint}), the default language
 * when there is no session (startup, scheduled tasks). The formats and the platform's texts ({@link L}) come from
 * {@link LocalizationBundle}s, one per {@link Language}; every layer above the platform has its own enum of keys
 * ({@link LocalizationKey}) and registers its {@link TextBundle}s with {@link #registerTexts}, so
 * {@link #getValue(LocalizationKey)} gives the texts of all of them.
 * <p>
 * The labels of the application's own enums and the built-in prompts are in the application's subtype,
 * {@code MarginaliaLocalization}, which the bean of the application is.
 */
public interface Localization {

    /**
     * The text in the language of the current session; a text its bundle doesn't have is taken from the default
     * language.
     */
    String getValue(LocalizationKey key);

    /**
     * The text in the language, for what is said outside of a session (a message to a webhook): the default language when the
     * language has no bundle or the bundle lacks the text.
     */
    String getValue(Language language, LocalizationKey key);

    /**
     * The key whose text in the language of the session is the text, of any enum; null when there is none.
     */
    LocalizationKey parseValue(String text);
    Locale getLocale();
    DateFormat getDateFormat();
    DateFormat getHourFormat();
    DateFormat getDateHourFormat();
    Comparator<String> createNaturalLanguageComparator();
    Comparator<String> createLocaleComparator();

    L getAIType(AIType aiType);
    L getProtocolType(ProtocolType t);
    L getReasoningEffort(ReasoningEffort t);
    L getReasoningPassthrough(ReasoningPassthrough t);
    L getLimitStrategy(LimitStrategy t);
    L getResetStrategy(ResetStrategy t);
    L getCleanupPolicy(CleanupReference.Policy t);
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
     * The language of the built-in prompts for the current session: the user's setting {@code promptLanguage} (in an
     * agent run the language of the agent), English when it is not set, has no prompts or there is no session. Which
     * languages have prompts is the application's (see {@code MarginaliaLocalization}); the platform alone has only English. Independent
     * of the language of the interface.
     */
    Language getPromptLanguage();

    /**
     * The languages that have built-in prompts, English first.
     */
    List<Language> getPromptLanguages();

    /**
     * The language with the code when it has built-in prompts, otherwise English.
     */
    Language resolvePromptLanguage(String code);

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

    /**
     * Adds the texts of an enum of keys other than {@link L} in one language. They are used while the language has a
     * {@link LocalizationBundle}.
     *
     * @throws IllegalArgumentException when the language already has texts of the enum, or the keys are {@link L}
     *                                  (those are a {@link LocalizationBundle}, see {@link #registerBundle})
     */
    void registerTexts(TextBundle<?> texts);

    /**
     * Removes the texts if they are the registered ones of their enum and language. The ones of the default language
     * can't be removed.
     */
    void unregisterTexts(TextBundle<?> texts);
}
