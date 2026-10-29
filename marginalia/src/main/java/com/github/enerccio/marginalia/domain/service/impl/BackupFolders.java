package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.PromptField;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The folder of a book in its backup: the {@code folder} object holds the {@code uuid} of the folder and the {@code path},
 * the folders from the top one to it, each with its name, uuid, description, language and the text settings it sets. A
 * book restored from the backup goes into the folder of that uuid if the user has it, else into the folders of that path
 * (a missing one is made with the settings it had), else to the top level.
 */
final class BackupFolders {

    private static final Logger log = LoggerFactory.getLogger(BackupFolders.class);

    static final String KEY = "folder";

    private BackupFolders() {
    }

    /**
     * @param chain the folder of the book and the folders above it, the folder of the book first (as {@code FolderService.findChain})
     */
    static void write(JsonObject backup, List<Folder> chain) {
        if (chain.isEmpty()) {
            return;
        }
        JsonArray path = new JsonArray();
        for (Folder folder : chain.reversed()) {
            JsonObject level = new JsonObject();
            level.addProperty("uuid", folder.getUuid());
            level.addProperty("name", folder.getName());
            if (StringUtils.isNotBlank(folder.getDescription())) {
                level.addProperty("description", folder.getDescription());
            }
            if (folder.getLanguage() != null) {
                level.addProperty("language", folder.getLanguage().name());
            }
            JsonObject settings = new JsonObject();
            for (PromptField field : PromptField.values()) {
                String value = field.ofFolder(folder);
                if (StringUtils.isNotBlank(value)) {
                    settings.addProperty(field.name(), value);
                }
            }
            if (!settings.isEmpty()) {
                level.add("settings", settings);
            }
            path.add(level);
        }
        JsonObject folder = new JsonObject();
        folder.addProperty("uuid", chain.getFirst().getUuid());
        folder.add("path", path);
        backup.add(KEY, folder);
    }

    /**
     * The folder a book of the backup goes into, null for the top level (no folder in the backup, or its folders can't be
     * made: the book is restored anyway).
     */
    static Folder resolve(JsonObject backup, FolderService folders) {
        if (backup == null || !backup.has(KEY) || !backup.get(KEY).isJsonObject()) {
            return null;
        }
        JsonObject described = backup.getAsJsonObject(KEY);
        try {
            if (described.has("uuid") && described.get("uuid").isJsonPrimitive()) {
                Folder same = folders.findForUser(described.get("uuid").getAsString());
                if (same != null) {
                    return same;
                }
            }
            if (!described.has("path") || !described.get("path").isJsonArray()) {
                return null;
            }
            Folder current = null;
            for (JsonElement element : described.getAsJsonArray("path")) {
                if (!element.isJsonObject() || !element.getAsJsonObject().has("name")) {
                    return current;
                }
                JsonObject level = element.getAsJsonObject();
                Folder next = folders.findChild(current, level.get("name").getAsString());
                current = next != null ? next : make(level, current, folders);
            }
            return current;
        } catch (Exception e) {
            log.warn("The folder of the backup can't be found or made, restoring the book at the top level", e);
            return null;
        }
    }

    private static Folder make(JsonObject level, Folder parent, FolderService folders) throws Exception {
        String name = level.get("name").getAsString();
        Folder made = folders.create(name, parent);
        Map<PromptField, String> prompts = new EnumMap<>(PromptField.class);
        if (level.has("settings") && level.get("settings").isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : level.getAsJsonObject("settings").entrySet()) {
                try {
                    prompts.put(PromptField.valueOf(entry.getKey()), entry.getValue().getAsString());
                } catch (IllegalArgumentException ignored) {
                    // a setting this version does not know
                }
            }
        }
        Language language = null;
        if (level.has("language") && level.get("language").isJsonPrimitive()) {
            try {
                language = Language.valueOf(level.get("language").getAsString());
            } catch (IllegalArgumentException ignored) {
                // a language this version does not know: inherited
            }
        }
        String description = level.has("description") && level.get("description").isJsonPrimitive() ? level.get("description").getAsString() : null;
        Folder updated = folders.update(made, new FolderService.Details(made.getName(), description, language, prompts));
        return updated == null ? made : updated;
    }
}
