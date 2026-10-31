package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.PromptField;
import com.github.enerccio.marginalia.extensions.InheritedSetting;
import com.github.enerccio.marginalia.extensions.InheritedSettingRegistry;
import com.github.enerccio.marginalia.extensions.InheritedSettingValues;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Makes the backup of a book fit to leave the installation: the folder of the book is dropped (where a book is filed is a
 * local choice), and, when asked, what the book inherits is written into it as the book's own values, so it reads the same
 * where there are no such folders and user settings. Only the values that come from the folders in the backup
 * ({@link BackupFolders}) or from the user settings are written, a built-in default stays unset: the other side has its own.
 */
final class BackupInheritance {

    private BackupInheritance() {
    }

    /**
     * Changes the inner object of a backup in place.
     *
     * @param user         the user settings that come after the folders, null to write only what the folders give
     * @param registry     the settings of the extensions, whose user level is written after the folders as well
     * @param inlineValues write the inherited values into the book
     */
    static void prepare(JsonObject backup, UserSetting user, InheritedSettingRegistry registry, boolean inlineValues) throws Exception {
        if (backup == null) {
            return;
        }
        JsonObject folder = backup.has(BackupFolders.KEY) && backup.get(BackupFolders.KEY).isJsonObject()
                ? backup.getAsJsonObject(BackupFolders.KEY) : null;
        backup.remove(BackupFolders.KEY);
        if (inlineValues) {
            inline(backup, folder, user, registry);
        }
    }

    private static void inline(JsonObject backup, JsonObject folder, UserSetting user, InheritedSettingRegistry registry) throws Exception {
        JsonObject extended = extendedOf(backup);
        if (extended == null) {
            return;
        }
        ExtendableEntityListener listener = new ExtendableEntityListener();
        Manuscript own = new Manuscript();
        own.setExtendedContent(extended.toString().getBytes(StandardCharsets.UTF_8));
        listener.deserialize(own);

        // nearest folder first
        JsonArray path = folder != null && folder.has("path") && folder.get("path").isJsonArray() ? folder.getAsJsonArray("path") : new JsonArray();
        Manuscript inherited = new Manuscript();
        boolean any = false;
        for (PromptField field : PromptField.values()) {
            if (StringUtils.isNotBlank(field.ofBook(own))) {
                continue;
            }
            String value = null;
            for (int i = path.size() - 1; i >= 0 && value == null; i--) {
                JsonObject settings = path.get(i).isJsonObject() && path.get(i).getAsJsonObject().has("settings")
                        && path.get(i).getAsJsonObject().get("settings").isJsonObject()
                        ? path.get(i).getAsJsonObject().getAsJsonObject("settings") : null;
                if (settings != null && settings.has(field.name()) && StringUtils.isNotBlank(settings.get(field.name()).getAsString())) {
                    value = settings.get(field.name()).getAsString();
                }
            }
            if (value == null && user != null && StringUtils.isNotBlank(field.ofUser(user))) {
                value = field.ofUser(user);
            }
            if (value != null) {
                field.setOfBook(inherited, value);
                any = true;
            }
        }
        any |= inlineExtensionSettings(own, inherited, path, user, registry);
        if (own.getOwnLanguage() == null) {
            for (int i = path.size() - 1; i >= 0 && inherited.getOwnLanguage() == null; i--) {
                JsonObject level = path.get(i).isJsonObject() ? path.get(i).getAsJsonObject() : null;
                if (level != null && level.has("language") && level.get("language").isJsonPrimitive()) {
                    try {
                        inherited.setLanguage(Language.valueOf(level.get("language").getAsString()));
                        any = true;
                    } catch (IllegalArgumentException ignored) {
                        // a language this version does not know
                    }
                }
            }
        }
        if (!any) {
            return;
        }
        // the keys that the values made, in the format the entity writes them
        JsonObject empty = serialized(new Manuscript(), listener);
        JsonObject written = serialized(inherited, listener);
        for (String key : written.keySet()) {
            if (!written.get(key).equals(empty.get(key))) {
                extended.add(key, written.get(key));
            }
        }
        backup.add("extendedContent", extended);
    }

    /**
     * The settings of the extensions the book does not set: from the nearest folder that does, else from the user level of the
     * extension (only for a setting that is registered, and when the user settings are written). The values the book sets itself
     * go along, because they are stored in one object that replaces the book's.
     *
     * @return whether a value was added
     */
    private static boolean inlineExtensionSettings(Manuscript own, Manuscript inherited, JsonArray path, UserSetting user,
                                                    InheritedSettingRegistry registry) {
        Map<String, String> values = new LinkedHashMap<>();
        // nearest folder first wins, so the folders are walked from the nearest and a key is taken once
        for (int i = path.size() - 1; i >= 0; i--) {
            JsonObject level = path.get(i).isJsonObject() ? path.get(i).getAsJsonObject() : null;
            if (level == null || !level.has("extensions") || !level.get("extensions").isJsonObject()) {
                continue;
            }
            for (Map.Entry<String, JsonElement> entry : level.getAsJsonObject("extensions").entrySet()) {
                if (entry.getValue().isJsonPrimitive() && StringUtils.isNotBlank(entry.getValue().getAsString())) {
                    values.putIfAbsent(entry.getKey(), entry.getValue().getAsString());
                }
            }
        }
        if (user != null && registry != null) {
            for (InheritedSettingRegistry.Group group : registry.groups()) {
                for (InheritedSetting setting : group.settings()) {
                    if (StringUtils.isNotBlank(setting.userValue())) {
                        values.putIfAbsent(setting.getKey(), setting.userValue());
                    }
                }
            }
        }
        Map<String, String> ownValues = InheritedSettingValues.all(own);
        boolean added = false;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (!ownValues.containsKey(entry.getKey())) {
                InheritedSettingValues.set(inherited, entry.getKey(), entry.getValue());
                added = true;
            }
        }
        if (added) {
            ownValues.forEach((key, value) -> InheritedSettingValues.set(inherited, key, value));
        }
        return added;
    }

    private static JsonObject serialized(Manuscript manuscript, ExtendableEntityListener listener) throws Exception {
        listener.serialize(manuscript);
        return JsonParser.parseString(new String(manuscript.getExtendedContent(), StandardCharsets.UTF_8)).getAsJsonObject();
    }

    /**
     * The extended content of the backup as an object ({@code createBackup} writes it as an object, or as text when it was no JSON).
     */
    private static JsonObject extendedOf(JsonObject backup) {
        if (!backup.has("extendedContent") || backup.get("extendedContent").isJsonNull()) {
            return new JsonObject();
        }
        JsonElement element = backup.get("extendedContent");
        if (element.isJsonObject()) {
            return element.getAsJsonObject();
        }
        try {
            JsonElement parsed = element.isJsonPrimitive() ? JsonParser.parseString(element.getAsString()) : null;
            return parsed != null && parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
