package com.github.enerccio.marginalia.loc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Texts an application on the platform puts in place of the ones of the bundles, without a subclass of each
 * language's bundle. A bean of this class in the Spring XML is picked up by {@link LocalizationImpl} (any number of
 * them, in the order of the context); a language the platform adds later is covered by the texts and replacements
 * given for every language ({@value #ALL_LANGUAGES}).
 * <ul>
 *     <li>{@link #setTexts texts}: per language code (or {@value #ALL_LANGUAGES}) a map of key to text. A key is
 *     {@code <enum>.<constant>} ({@code L.LABEL_CLOSE}, {@code M_L.LABEL_BOOK}) or the constant alone for {@link L}.
 *     For a language the text of the language wins, then the one for every language, then the language's bundle, then
 *     the override of the default language and last the default language's bundle.</li>
 *     <li>{@link #setReplacements replacements}: per language code (or {@value #ALL_LANGUAGES}) a map of text to
 *     replace to its replacement, applied to every text of that language ({@code Marginalia} &rarr; {@code Joi}).
 *     The ones of the language run first.</li>
 *     <li>{@link #setTransformers transformers}: code that changes any text last.</li>
 * </ul>
 */
public class LocalizationOverride {

    /**
     * The language code that stands for every language.
     */
    public static final String ALL_LANGUAGES = "*";

    /**
     * Changes a text of a language after the replacements.
     */
    @FunctionalInterface
    public interface TextTransformer {
        String transform(Language language, LocalizationKey key, String text);
    }

    private final Map<String, Map<String, String>> texts = new HashMap<>();
    private final Map<String, Map<String, String>> replacements = new HashMap<>();
    private final List<TextTransformer> transformers = new ArrayList<>();

    /**
     * Language code (or {@value #ALL_LANGUAGES}) to key ({@code L.LABEL_CLOSE}, {@code LABEL_CLOSE}) to text.
     */
    public void setTexts(Map<String, Map<String, String>> texts) {
        this.texts.clear();
        if (texts != null) {
            texts.forEach((language, byKey) -> {
                Map<String, String> normalized = new HashMap<>();
                byKey.forEach((key, text) -> normalized.put(normalizeKey(key), text));
                this.texts.put(languageKey(language), normalized);
            });
        }
    }

    /**
     * Language code (or {@value #ALL_LANGUAGES}) to text to replace to its replacement, applied in the map's order.
     */
    public void setReplacements(Map<String, Map<String, String>> replacements) {
        this.replacements.clear();
        if (replacements != null) {
            replacements.forEach((language, pairs) -> this.replacements.put(languageKey(language), new LinkedHashMap<>(pairs)));
        }
    }

    public void setTransformers(List<TextTransformer> transformers) {
        this.transformers.clear();
        if (transformers != null) {
            this.transformers.addAll(transformers);
        }
    }

    /**
     * Sets the text of the key in the language (or {@value #ALL_LANGUAGES}).
     */
    public LocalizationOverride text(String language, LocalizationKey key, String text) {
        texts.computeIfAbsent(languageKey(language), _ -> new HashMap<>()).put(keyName(key), text);
        return this;
    }

    /**
     * Replaces {@code from} by {@code to} in every text of the language (or {@value #ALL_LANGUAGES}).
     */
    public LocalizationOverride replace(String language, String from, String to) {
        replacements.computeIfAbsent(languageKey(language), _ -> new LinkedHashMap<>()).put(from, to);
        return this;
    }

    public LocalizationOverride transformer(TextTransformer transformer) {
        transformers.add(transformer);
        return this;
    }

    /**
     * The text this override gives the key in the language, {@code null} when it gives none.
     */
    public String text(Language language, LocalizationKey key) {
        String name = keyName(key);
        String text = language == null ? null : lookup(texts.get(languageKey(language.code())), name);
        return text != null ? text : lookup(texts.get(ALL_LANGUAGES), name);
    }

    /**
     * The text after the replacements and the transformers of the language.
     */
    public String transform(Language language, LocalizationKey key, String text) {
        if (text == null) {
            return null;
        }
        if (language != null) {
            text = replace(replacements.get(languageKey(language.code())), text);
        }
        text = replace(replacements.get(ALL_LANGUAGES), text);
        for (TextTransformer transformer : transformers) {
            text = transformer.transform(language, key, text);
            if (text == null) {
                return null;
            }
        }
        return text;
    }

    private static String replace(Map<String, String> pairs, String text) {
        if (pairs == null) {
            return text;
        }
        for (Map.Entry<String, String> pair : pairs.entrySet()) {
            if (pair.getKey() != null && !pair.getKey().isEmpty()) {
                text = text.replace(pair.getKey(), pair.getValue() == null ? "" : pair.getValue());
            }
        }
        return text;
    }

    private static String lookup(Map<String, String> byKey, String name) {
        return byKey == null ? null : byKey.get(name);
    }

    private static String languageKey(String language) {
        if (language == null || language.isBlank() || ALL_LANGUAGES.equals(language.trim())) {
            return ALL_LANGUAGES;
        }
        return Language.of(language.trim()).code();
    }

    static String keyName(LocalizationKey key) {
        return key.getDeclaringClass().getSimpleName() + "." + key.name();
    }

    private static String normalizeKey(String key) {
        String trimmed = key.trim();
        return trimmed.contains(".") ? trimmed : L.class.getSimpleName() + "." + trimmed;
    }
}
