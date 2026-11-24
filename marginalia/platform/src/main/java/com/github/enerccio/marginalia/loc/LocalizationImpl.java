package com.github.enerccio.marginalia.loc;


import com.github.enerccio.marginalia.bound.SessionPoint;
import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.collections.LimitStrategy;
import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.collections.ReasoningEffort;
import com.github.enerccio.marginalia.domain.collections.ReasoningPassthrough;
import com.github.enerccio.marginalia.domain.collections.ResetStrategy;
import com.github.enerccio.marginalia.domain.model.Setting;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.model.impl.TagRelation;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
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
    /**
     * The texts of every enum of keys by language, the {@link LocalizationBundle}s under {@link L}.
     */
    private final Map<Class<?>, Map<Language, TextBundle<?>>> texts = new ConcurrentHashMap<>();
    private Language defaultLanguage = Language.EN;
    private final List<LocalizationOverride> overrides = new java.util.concurrent.CopyOnWriteArrayList<>();

    @Autowired
    private SessionPoint sessionPoint;

    private final Map<AIType, L> aiTypes = new HashMap<>();
    private final Map<ProtocolType, L> protocolTypes = new HashMap<>();
    private final Map<ReasoningEffort, L> reasoningEfforts = new HashMap<>();
    private final Map<ReasoningPassthrough, L> reasoningPassthroughs = new HashMap<>();
    private final Map<LimitStrategy, L> limitStrategies = new HashMap<>();
    private final Map<ResetStrategy, L> resetStrategies = new HashMap<>();
    private final Map<CleanupReference.Policy, L> cleanupPolicies = new HashMap<>();
    private final Map<PermissionType, L> permissionTypes = new HashMap<>();
    private final Map<Class<?>, LocalizationKey> domainObjects = new HashMap<>();

    public LocalizationImpl() {
        loadMaps();
    }

    public void setBundles(List<LocalizationBundle> bundles) {
        for (LocalizationBundle bundle : bundles) {
            registerBundle(bundle);
        }
    }

    /**
     * The texts of the enums of keys above the platform, see {@link #registerTexts}.
     */
    public void setTexts(List<TextBundle<?>> texts) {
        for (TextBundle<?> bundle : texts) {
            registerTexts(bundle);
        }
    }

    /**
     * The {@link LocalizationOverride} beans of the context, the texts of the application on the platform that win over
     * the bundles' ones.
     */
    @Autowired(required = false)
    public void setOverrides(List<LocalizationOverride> overrides) {
        this.overrides.clear();
        if (overrides != null) {
            this.overrides.addAll(overrides);
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
        for (Class<? extends LocalizationKey> keys : keyEnums()) {
            TextBundle<?> defaults = texts.getOrDefault(keys, Map.of()).get(defaultLanguage);
            String missing = defaults == null ? "all of " + keys.getSimpleName() : missingKeys(defaults);
            if (!missing.isEmpty()) {
                log.error("\n*********** LOCALIZATION IS MISSING! ***********\n"
                        + missing
                        + "\n************************************************\n");
                throw new IllegalStateException("The " + keys.getSimpleName() + " texts of the default language "
                        + defaultLanguage + " are incomplete");
            }
        }
    }

    /**
     * The enums of keys whose texts the default language must have, every one of them. The platform's is {@link L};
     * a subclass adds the enum of its layer.
     */
    protected List<Class<? extends LocalizationKey>> keyEnums() {
        return List.of(L.class);
    }

    private void loadMaps() {
        aiTypes.put(AIType.OPEN_AI_COMPATIBLE, L.ENUM_AI_TYPE_OPEN_AI_COMPATIBLE);
        aiTypes.put(AIType.EXTENSION, L.ENUM_AI_TYPE_EXTENSION);

        protocolTypes.put(ProtocolType.CHAT_COMPLETION, L.ENUM_PROTOCOL_TYPE_OPEN_CHAT_COMPLETION);

        reasoningEfforts.put(ReasoningEffort.NONE, L.ENUM_REASONING_NONE);
        reasoningEfforts.put(ReasoningEffort.LOW, L.ENUM_REASONING_LOW);
        reasoningEfforts.put(ReasoningEffort.MEDIUM, L.ENUM_REASONING_MEDIUM);
        reasoningEfforts.put(ReasoningEffort.HIGH, L.ENUM_REASONING_HIGH);
        reasoningPassthroughs.put(ReasoningPassthrough.NONE, L.ENUM_REASONING_PASSTHROUGH_NONE);
        reasoningPassthroughs.put(ReasoningPassthrough.TOOL_CALLS, L.ENUM_REASONING_PASSTHROUGH_TOOL_CALLS);
        reasoningPassthroughs.put(ReasoningPassthrough.ALWAYS, L.ENUM_REASONING_PASSTHROUGH_ALWAYS);

        limitStrategies.put(LimitStrategy.UNLIMITED, L.ENUM_LIMIT_UNLIMITED);
        limitStrategies.put(LimitStrategy.TOKEN_LIMIT, L.ENUM_LIMIT_TOKEN_LIMIT);
        limitStrategies.put(LimitStrategy.NOT_AVAILABLE, L.ENUM_LIMIT_NOT_AVAILABLE);

        permissionTypes.put(PermissionType.HELPER, L.ENUM_PERMISSION_HELPER);
        permissionTypes.put(PermissionType.CREATE_AI, L.ENUM_PERMISSION_CREATE_AI);
        permissionTypes.put(PermissionType.AGENTS, L.ENUM_PERMISSION_AGENTS);
        permissionTypes.put(PermissionType.NOTIFICATIONS, L.ENUM_PERMISSION_NOTIFICATIONS);
        permissionTypes.put(PermissionType.VERSION_HISTORY, L.ENUM_PERMISSION_VERSION_HISTORY);
        permissionTypes.put(PermissionType.API, L.ENUM_PERMISSION_API);

        resetStrategies.put(ResetStrategy.DAILY, L.ENUM_RESET_DAILY);
        resetStrategies.put(ResetStrategy.WEEKLY, L.ENUM_RESET_WEEKLY);
        resetStrategies.put(ResetStrategy.MONTHLY, L.ENUM_RESET_MONTHLY);

        cleanupPolicies.put(CleanupReference.Policy.STRONG, L.ENUM_CLEANUP_POLICY_STRONG);
        cleanupPolicies.put(CleanupReference.Policy.OWNED_BY, L.ENUM_CLEANUP_POLICY_OWNED_BY);
        cleanupPolicies.put(CleanupReference.Policy.OWNS, L.ENUM_CLEANUP_POLICY_OWNS);
        cleanupPolicies.put(CleanupReference.Policy.WEAK, L.ENUM_CLEANUP_POLICY_WEAK);

        domainObjects.put(AI.class, L.LABEL_MODEL);
        domainObjects.put(Protocol.class, L.ENUM_DOMAIN_PROTOCOL);
        domainObjects.put(Tag.class, L.ENUM_DOMAIN_TAG);
        domainObjects.put(TagRelation.class, L.ENUM_DOMAIN_TAG_RELATION);
        domainObjects.put(Resource.class, L.ENUM_DOMAIN_RESOURCE);
        domainObjects.put(Setting.class, L.ENUM_DOMAIN_SETTING);
        domainObjects.put(User.class, L.LABEL_USER);
    }

    private static <K extends Enum<K> & LocalizationKey> String missingKeys(TextBundle<K> bundle) {
        StringBuilder missing = new StringBuilder();
        for (K key : bundle.getKeys().getEnumConstants()) {
            if (bundle.getValue(key) == null) {
                missing.append(key.name()).append('\n');
            }
        }
        return missing.toString();
    }

    /**
     * The text of the key in the language, from the default language when the language lacks it; null when neither
     * has it.
     */
    private String text(Language language, LocalizationKey key) {
        Map<Language, TextBundle<?>> byLanguage = texts.getOrDefault(key.getDeclaringClass(), Map.of());
        String text = null;
        if (language != null) {
            text = overrideText(language, key);
            if (text == null) {
                text = lookup(byLanguage.get(language), key);
            }
        }
        if (text == null) {
            text = overrideText(defaultLanguage, key);
        }
        if (text == null) {
            text = lookup(byLanguage.get(defaultLanguage), key);
        }
        if (text == null || overrides.isEmpty()) {
            return text;
        }
        Language shown = language == null ? defaultLanguage : language;
        for (LocalizationOverride override : overrides) {
            text = override.transform(shown, key, text);
            if (text == null) {
                return null;
            }
        }
        return text;
    }

    private String overrideText(Language language, LocalizationKey key) {
        for (LocalizationOverride override : overrides) {
            String text = override.text(language, key);
            if (text != null) {
                return text;
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String lookup(TextBundle bundle, LocalizationKey key) {
        return bundle == null ? null : bundle.getValue((Enum) key);
    }

    @Override
    public String getValue(Language language, LocalizationKey key) {
        String text = text(language, key);
        return text == null ? "NOT LOCALIZED!" : text;
    }

    @Override
    public String getValue(LocalizationKey key) {
        String text = text(getCurrentLanguage(), key);
        if (text == null) {
            log.error("Key {}.{} is not localized!", key.getDeclaringClass().getSimpleName(), key.name());
            return "NOT LOCALIZED!";
        }
        return text;
    }

    @Override
    public LocalizationKey parseValue(String text) {
        Language language = getCurrentLanguage();
        for (Map<Language, TextBundle<?>> byLanguage : texts.values()) {
            TextBundle<?> bundle = byLanguage.get(language);
            if (bundle == null) {
                continue;
            }
            for (LocalizationKey key : bundle.getMessages().keySet()) {
                // the text as getValue gives it, with the overrides
                if (text != null && text.equals(text(language, key))) {
                    return key;
                }
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
        putTexts(bundle);
    }

    @Override
    public void unregisterBundle(LocalizationBundle bundle) {
        Language language = bundle.getLanguage();
        if (!language.equals(defaultLanguage) && bundles.remove(language, bundle)) {
            removeTexts(bundle);
        }
    }

    @Override
    public void registerTexts(TextBundle<?> bundle) {
        if (bundle.getKeys() == L.class) {
            throw new IllegalArgumentException("The texts of L are a LocalizationBundle, register them with registerBundle");
        }
        putTexts(bundle);
    }

    @Override
    public void unregisterTexts(TextBundle<?> bundle) {
        if (bundle.getKeys() != L.class && !bundle.getLanguage().equals(defaultLanguage)) {
            removeTexts(bundle);
        }
    }

    private void putTexts(TextBundle<?> bundle) {
        Language language = bundle.getLanguage();
        Map<Language, TextBundle<?>> byLanguage = texts.computeIfAbsent(bundle.getKeys(), _ -> new ConcurrentHashMap<>());
        if (byLanguage.putIfAbsent(language, bundle) != null) {
            throw new IllegalArgumentException("Language " + language + " already has the texts of "
                    + bundle.getKeys().getSimpleName());
        }
        if (!language.equals(defaultLanguage)) {
            String missing = missingKeys(bundle);
            if (!missing.isEmpty()) {
                log.warn("The {} texts of {} lack some, the {} ones are used instead:\n{}",
                        bundle.getKeys().getSimpleName(), language, defaultLanguage, missing);
            }
        }
    }

    private void removeTexts(TextBundle<?> bundle) {
        Map<Language, TextBundle<?>> byLanguage = texts.get(bundle.getKeys());
        if (byLanguage != null) {
            byLanguage.remove(bundle.getLanguage(), bundle);
        }
    }

    @Override
    public Language getPromptLanguage() {
        Language language = sessionPromptLanguage();
        return hasPrompts(language) ? language : Language.EN;
    }

    @Override
    public List<Language> getPromptLanguages() {
        return promptLanguages();
    }

    /**
     * The languages that have built-in prompts, English first. The platform has none of its own, so only English; the
     * application's subclass gives the ones of its prompts.
     */
    protected List<Language> promptLanguages() {
        return List.of(Language.EN);
    }

    @Override
    public Language resolvePromptLanguage(String code) {
        if (code == null || code.isBlank()) {
            return Language.EN;
        }
        Language language = Language.of(code);
        return hasPrompts(language) ? language : Language.EN;
    }

    private boolean hasPrompts(Language language) {
        return language != null && promptLanguages().contains(language);
    }

    private Language sessionPromptLanguage() {
        if (sessionPoint == null || RequestContextHolder.getRequestAttributes() == null) {
            return null;
        }
        try {
            return sessionPoint.getPromptLanguage();
        } catch (RuntimeException e) {
            // the session is gone
            log.trace("No session to read the prompt language from: {}", e.getMessage());
            return null;
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
    public L getReasoningPassthrough(ReasoningPassthrough type) {
        return reasoningPassthroughs.get(type);
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
    public L getResetStrategy(ResetStrategy type) {
        return resetStrategies.get(type);
    }

    @Override
    public L getCleanupPolicy(CleanupReference.Policy type) {
        return cleanupPolicies.get(type);
    }

    /**
     * Names the kind of object of an entity class for {@link #localizeDomainObject}, for the classes of the application.
     */
    protected void putDomainObject(Class<?> type, LocalizationKey label) {
        domainObjects.put(type, label);
    }

    @Override
    public String localizeDomainObject(Class<?> clazz) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            LocalizationKey key = domainObjects.get(c);
            if (key != null) {
                return getValue(key);
            }
        }
        return clazz == null ? "" : clazz.getSimpleName();
    }
}
