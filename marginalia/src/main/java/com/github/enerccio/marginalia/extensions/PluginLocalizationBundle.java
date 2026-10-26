package com.github.enerccio.marginalia.extensions;

import com.github.enerccio.marginalia.loc.Language;
import com.github.enerccio.marginalia.loc.Localization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.HashMap;
import java.util.Map;

/**
 * Texts of a plugin's own user interface. A plugin can't add keys to the {@code L} enum, so its texts are the English
 * sentences themselves: the code asks for {@code bundle.localized("Export instructions")} and the bundle returns the
 * translation into the language of the current session, the English key when it has none. A subclass fills the
 * translations in {@link #loadKeys()} with {@link #ja}, {@link #zh} or {@link #add}; text with values uses
 * {@code String.format} placeholders in the key ({@code localized("Failed: %s").formatted(reason)}).
 */
@Configurable(preConstruction = true)
public abstract class PluginLocalizationBundle {

    public static final Language JA = Language.of("ja");
    public static final Language ZH = Language.of("zh");

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
