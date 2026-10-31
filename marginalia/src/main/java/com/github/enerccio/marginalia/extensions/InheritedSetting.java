package com.github.enerccio.marginalia.extensions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * The definition of a setting that an extension adds to the books and the folders: like the text settings of a book
 * ({@code PromptField}) it is set on a book, else on the nearest folder above it, else by the user, else it has the default
 * of the extension. An extension registers it in the {@link InheritedSettingRegistry}; the settings dialogs of a book and of a
 * folder then show it in their <i>Extension Settings</i> section, and {@code ManuscriptService.resolveExtension} works out the
 * value.
 * <p>
 * The levels of the user are the business of the extension (it has its own settings panel), so it only says what they hold
 * ({@link #userValue}); the book and the folders keep their values in their attributes, see {@link InheritedSettingValues}.
 * The texts are suppliers because a language is chosen per session: they are asked each time a dialog is built.
 */
public final class InheritedSetting {

    private static final Logger log = LoggerFactory.getLogger(InheritedSetting.class);

    /**
     * How the setting is edited. The value is always a text; a choice keeps the id of the picked one.
     */
    public enum Kind {
        /** A line of text. */
        TEXT,
        /** Several lines of text, like a prompt. */
        TEXT_AREA,
        /** One of the {@link Choice}s of {@link #choices}. */
        CHOICE
    }

    /**
     * One of the values of a {@link Kind#CHOICE} setting.
     *
     * @param id    what is stored
     * @param label what the user sees
     */
    public record Choice(String id, String label) {
    }

    private final String key;
    private final Kind kind;
    private final Supplier<String> label;

    private Supplier<String> group;
    private Supplier<String> help;
    private Supplier<String> userValue = () -> null;
    private Supplier<String> fallback = () -> "";
    private Supplier<List<Choice>> choices = List::of;

    private InheritedSetting(String key, Kind kind, Supplier<String> label) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("The key of an inherited setting can't be blank");
        }
        this.key = key;
        this.kind = kind;
        this.label = Objects.requireNonNull(label, "label");
    }

    /**
     * A line of text.
     *
     * @param key   what the value is stored under in the attributes of the book and the folders, and what
     *              {@code resolveExtension} is asked for; prefix it with the name of the extension, the keys of all of them share the
     *              attributes
     * @param label the name of the setting
     */
    public static InheritedSetting text(String key, Supplier<String> label) {
        return new InheritedSetting(key, Kind.TEXT, label);
    }

    /**
     * Several lines of text.
     */
    public static InheritedSetting textArea(String key, Supplier<String> label) {
        return new InheritedSetting(key, Kind.TEXT_AREA, label);
    }

    /**
     * One of a list of values.
     *
     * @param choices the values to pick from, asked each time a dialog is built
     */
    public static InheritedSetting choice(String key, Supplier<String> label, Supplier<List<Choice>> choices) {
        InheritedSetting setting = new InheritedSetting(key, Kind.CHOICE, label);
        setting.choices = Objects.requireNonNull(choices, "choices");
        return setting;
    }

    /**
     * The title of the group of the settings of the extension in the dialogs (the first setting of an extension that has one
     * names the group; the name of the extension is used otherwise).
     */
    public InheritedSetting group(Supplier<String> group) {
        this.group = group;
        return this;
    }

    /**
     * A line under the field that says what the setting is for.
     */
    public InheritedSetting help(Supplier<String> help) {
        this.help = help;
        return this;
    }

    /**
     * What the user level of the extension holds, null or blank for nothing: the value a book and a folder inherit when none
     * of the folders above sets one. It is asked from any thread that resolves a setting, including a generation.
     */
    public InheritedSetting userValue(Supplier<String> userValue) {
        this.userValue = Objects.requireNonNull(userValue, "userValue");
        return this;
    }

    /**
     * The value when nothing sets one (empty when this is not called), the last level of the chain.
     */
    public InheritedSetting fallback(Supplier<String> fallback) {
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        return this;
    }

    public String getKey() {
        return key;
    }

    public Kind getKind() {
        return kind;
    }

    public String label() {
        return safe(label, key);
    }

    /**
     * The title of the group, null when the setting names none.
     */
    public String group() {
        return group == null ? null : safe(group, null);
    }

    /**
     * The line under the field, null for none.
     */
    public String help() {
        return help == null ? null : safe(help, null);
    }

    public String userValue() {
        return safe(userValue, null);
    }

    public String fallback() {
        String value = safe(fallback, "");
        return value == null ? "" : value;
    }

    public List<Choice> choices() {
        List<Choice> picked = safe(choices, List.of());
        return picked == null ? List.of() : picked;
    }

    // the supplier belongs to an extension, which must not break a dialog or a generation with its failure
    private <T> T safe(Supplier<T> supplier, T onFailure) {
        try {
            return supplier.get();
        } catch (RuntimeException e) {
            log.warn("The inherited setting {} failed to provide a value: {}", key, e.getMessage(), e);
            return onFailure;
        }
    }
}
