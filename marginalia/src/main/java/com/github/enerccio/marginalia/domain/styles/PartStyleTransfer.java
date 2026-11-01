package com.github.enerccio.marginalia.domain.styles;

import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Text styles as a JSON file, to move them between installations and to share them. The file is
 * <pre>{
 *   "format": "marginalia-text-style",
 *   "version": 1,
 *   "styles": [ { "name": "...", "forExport": false, "options": { "FONT_FAMILY": "...", ... }, "customCss": "..." } ]
 * }</pre>
 * The ids of the styles are not in it: an imported style is a new style of the user with a new id. An option this version does not
 * know is left out (and counted, {@link Imported#ignoredOptions()}), so a file made by a newer version still loads.
 */
public final class PartStyleTransfer {

    public static final String FORMAT = "marginalia-text-style";
    public static final int VERSION = 1;

    /** The largest file taken; a style is a few kilobytes. */
    public static final int MAX_BYTES = 2 * 1024 * 1024;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private PartStyleTransfer() {
    }

    /**
     * Why a file could not be imported.
     */
    public enum Reason {
        /** Not JSON, not of this format, or without any style. */
        NOT_STYLES,
        /** Written by a newer version of the format. */
        NEWER_VERSION,
        /** The custom CSS of a style has braces that do not match ({@link ImportException#getStyleName()}). */
        UNBALANCED_CSS
    }

    public static class ImportException extends IllegalArgumentException {
        private final Reason reason;
        private final String styleName;

        ImportException(Reason reason, String styleName) {
            super(reason + (styleName == null ? "" : ": " + styleName));
            this.reason = reason;
            this.styleName = styleName;
        }

        public Reason getReason() {
            return reason;
        }

        /** The style the problem is in, for {@link Reason#UNBALANCED_CSS}. */
        public String getStyleName() {
            return styleName;
        }
    }

    /**
     * @param styles         new styles of the user (fresh ids, never built in)
     * @param ignoredOptions how many options of the file this version does not know
     */
    public record Imported(List<PartStyle> styles, int ignoredOptions) {
    }

    /**
     * The file for the styles (built in styles too: imported, they become the user's own).
     */
    public static String toJson(List<PartStyle> styles) {
        JsonObject root = new JsonObject();
        root.addProperty("format", FORMAT);
        root.addProperty("version", VERSION);
        JsonArray array = new JsonArray();
        for (PartStyle style : styles) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", StringUtils.defaultString(style.getName()));
            entry.addProperty("forExport", style.isForExport());
            JsonObject options = new JsonObject();
            for (PartStyleOption option : PartStyleOption.values()) {
                String value = style.get(option);
                if (value != null) {
                    options.addProperty(option.getKey(), value);
                }
            }
            entry.add("options", options);
            if (style.getCustomCss() != null) {
                entry.addProperty("customCss", style.getCustomCss());
            }
            array.add(entry);
        }
        root.add("styles", array);
        return GSON.toJson(root);
    }

    /**
     * The styles of a file. A file with one style object ({@code name}, {@code options}...) and no {@code styles} list is taken too.
     *
     * @throws ImportException when the file is not a file of styles, is of a newer version, or a style has custom CSS that is not balanced
     */
    public static Imported fromJson(String json) {
        if (json == null || json.length() > MAX_BYTES) {
            throw new ImportException(Reason.NOT_STYLES, null);
        }
        JsonObject root;
        try {
            JsonElement parsed = JsonParser.parseString(json);
            if (!parsed.isJsonObject()) {
                throw new ImportException(Reason.NOT_STYLES, null);
            }
            root = parsed.getAsJsonObject();
        } catch (com.google.gson.JsonParseException e) {
            throw new ImportException(Reason.NOT_STYLES, null);
        }
        if (root.has("format") && !FORMAT.equals(text(root, "format"))) {
            throw new ImportException(Reason.NOT_STYLES, null);
        }
        if (root.has("version")) {
            try {
                if (root.get("version").getAsInt() > VERSION) {
                    throw new ImportException(Reason.NEWER_VERSION, null);
                }
            } catch (RuntimeException e) {
                if (e instanceof ImportException) {
                    throw e;
                }
                throw new ImportException(Reason.NOT_STYLES, null);
            }
        }
        List<JsonObject> entries = new ArrayList<>();
        if (root.has("styles")) {
            if (!root.get("styles").isJsonArray()) {
                throw new ImportException(Reason.NOT_STYLES, null);
            }
            for (JsonElement element : root.getAsJsonArray("styles")) {
                if (element.isJsonObject()) {
                    entries.add(element.getAsJsonObject());
                }
            }
        } else if (root.has("options") || root.has("customCss")) {
            entries.add(root);
        }
        if (entries.isEmpty()) {
            throw new ImportException(Reason.NOT_STYLES, null);
        }

        List<PartStyle> styles = new ArrayList<>();
        int ignored = 0;
        for (JsonObject entry : entries) {
            String name = StringUtils.defaultIfBlank(text(entry, "name"), "Imported style");
            PartStyle style = new PartStyle(UUID.randomUUID().toString(), name.trim());
            style.setForExport(entry.has("forExport") && entry.get("forExport").isJsonPrimitive()
                    && entry.get("forExport").getAsJsonPrimitive().isBoolean() && entry.get("forExport").getAsBoolean());
            if (entry.has("options") && entry.get("options").isJsonObject()) {
                Map<String, String> values = new LinkedHashMap<>();
                for (Map.Entry<String, JsonElement> option : entry.getAsJsonObject("options").entrySet()) {
                    if (option.getValue().isJsonPrimitive()) {
                        values.put(option.getKey(), option.getValue().getAsString());
                    }
                }
                for (Map.Entry<String, String> value : values.entrySet()) {
                    PartStyleOption option = optionOf(value.getKey());
                    if (option == null) {
                        ignored++;
                    } else {
                        style.set(option, value.getValue());
                    }
                }
            }
            String css = text(entry, "customCss");
            if (css != null && !PartStyle.isBalanced(css)) {
                throw new ImportException(Reason.UNBALANCED_CSS, style.getName());
            }
            style.setCustomCss(css);
            styles.add(style);
        }
        return new Imported(styles, ignored);
    }

    private static PartStyleOption optionOf(String key) {
        for (PartStyleOption option : PartStyleOption.values()) {
            if (option.getKey().equals(key)) {
                return option;
            }
        }
        return null;
    }

    private static String text(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }
}
