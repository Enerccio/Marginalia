package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.PromptField;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

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
     * @param inlineValues write the inherited values into the book
     */
    static void prepare(JsonObject backup, UserSetting user, boolean inlineValues) throws Exception {
        if (backup == null) {
            return;
        }
        JsonObject folder = backup.has(BackupFolders.KEY) && backup.get(BackupFolders.KEY).isJsonObject()
                ? backup.getAsJsonObject(BackupFolders.KEY) : null;
        backup.remove(BackupFolders.KEY);
        if (inlineValues) {
            inline(backup, folder, user);
        }
    }

    private static void inline(JsonObject backup, JsonObject folder, UserSetting user) throws Exception {
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
