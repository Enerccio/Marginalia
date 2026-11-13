package com.github.enerccio.marginalia.extensions;

import com.github.enerccio.marginalia.loc.Language;
import com.github.enerccio.marginalia.loc.Localization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.HashMap;
import java.util.Map;

/**
 * Texts of a plugin's own user interface. A plugin can't add keys to the {@code L} enum, so its texts are the English
 * sentences themselves, constants of the subclass ({@code String EXPORT_INSTRUCTIONS = "Export instructions"}): the code
 * asks for {@code bundle.localized(EXPORT_INSTRUCTIONS)} and the bundle returns the
 * translation into the language of the current session, the English key when it has none. A subclass fills the
 * translations in {@link #loadKeys()} with {@link #ja}, {@link #zh}, {@link #fr}, {@link #es}, {@link #de} or {@link #add}; text with values uses
 * {@code String.format} placeholders in the key ({@code localized(FAILED).formatted(reason)}).
 * <p>
 * The built-in prompts of a plugin follow the prompt language of the user ({@code UserSetting.promptLanguage}, see
 * {@link #getPromptLanguage()}), not the language of the interface: the plugin keeps them in an object per language (the
 * English class and a subclass per translation, like the application's {@code Defaults}) and picks one with
 * {@link #forPromptLanguage}.
 */
@Configurable(preConstruction = true)
public abstract class PluginLocalizationBundle {

    public static final Language JA = Language.of("ja");
    public static final Language ZH = Language.of("zh");
    public static final Language FR = Language.of("fr");
    public static final Language ES = Language.of("es");
    public static final Language DE = Language.of("de");
    public static final Language IT = Language.of("it");
    public static final Language KO = Language.of("ko");

    @Autowired
    private Localization loc;

    protected Map<Language, Map<String, String>> keys = new HashMap<>();

    public PluginLocalizationBundle() {
        loadKeys();
    }

    protected abstract void loadKeys();

    protected void add(Language language, String message, String translation) {
        keys.computeIfAbsent(language, l -> new HashMap<>()).put(message, translation);
    }

    protected void ja(String message, String translation) {
        add(JA, message, translation);
    }

    protected void zh(String message, String translation) {
        add(ZH, message, translation);
    }

    protected void fr(String message, String translation) {
        add(FR, message, translation);
    }

    protected void es(String message, String translation) {
        add(ES, message, translation);
    }

    protected void de(String message, String translation) {
        add(DE, message, translation);
    }

    /**
     * The language of the built-in prompts of the current session, English when there is none (see
     * {@code Localization.getPromptLanguage()}).
     */
    public Language getPromptLanguage() {
        return loc == null ? Language.EN : loc.getPromptLanguage();
    }

    /**
     * The one of the objects that holds the prompts in the prompt language of the session: {@code english} unless
     * {@code translations} has one for that language.
     */
    protected <D> D forPromptLanguage(D english, Map<Language, D> translations) {
        D translated = translations.get(getPromptLanguage());
        return translated != null ? translated : english;
    }

    public String localized(String message) {
        Language language = loc == null ? null : loc.getCurrentLanguage();
        if (language == null || !keys.containsKey(language)) {
            return message;
        }
        Map<String, String> values = keys.get(language);
        if (!values.containsKey(message))
            return message;
        return values.get(message);
    }

}
