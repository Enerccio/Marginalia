package com.github.enerccio.marginalia.domain.service.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The resources a book backup points to besides the images of its parts (see {@link BackupImages}): anything that the data of the book or of a part (its
 * {@code extendedContent}, which also holds the {@code attributes} of extensions) names by the uuid of a resource, whatever the extension wrote it for: the
 * audio of an audiobook, a font, a file of a plugin. The backup knows no more about them than the uuid, so they are found as uuids inside the text of
 * the data: a uuid that is the uuid of a resource of the user (or of the archive) is a reference, any other (the uuid of a part, of a lorebook) is left alone.
 * The files are not in the JSON.
 */
final class BackupResources {

    private static final Pattern UUID = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

    private BackupResources() {
    }

    /**
     * @param backup     the book backup (the object with the {@code messages})
     * @param isResource whether a uuid is the uuid of a resource (asked once for each uuid)
     * @return uuids of the resources the data of the book and of its parts name, in the order of their first use
     */
    static Set<String> collect(JsonObject backup, Predicate<String> isResource) {
        Set<String> found = new LinkedHashSet<>();
        Map<String, Boolean> known = new HashMap<>();
        visit(backup, text -> {
            Matcher matcher = UUID.matcher(text);
            while (matcher.find()) {
                String uuid = matcher.group();
                if (known.computeIfAbsent(uuid, isResource::test)) {
                    found.add(uuid);
                }
            }
            return text;
        });
        return found;
    }

    /**
     * Writes the uuids of other resources into the data of the book and of its parts.
     *
     * @param replacements the new uuid of a resource by the one it has in the backup
     */
    static void replace(JsonObject backup, Map<String, String> replacements) {
        if (replacements.isEmpty()) {
            return;
        }
        visit(backup, text -> {
            Matcher matcher = UUID.matcher(text);
            return matcher.replaceAll(match -> Matcher.quoteReplacement(replacements.getOrDefault(match.group(), match.group())));
        });
    }

    /** The uuids that are in the text, for the data of a book that exists. */
    static Set<String> uuidsIn(String text) {
        Set<String> found = new LinkedHashSet<>();
        if (text != null) {
            Matcher matcher = UUID.matcher(text);
            while (matcher.find()) {
                found.add(matcher.group());
            }
        }
        return found;
    }

    private static void visit(JsonObject backup, java.util.function.UnaryOperator<String> change) {
        if (backup == null) {
            return;
        }
        data(backup, change);
        if (backup.has("messages") && backup.get("messages").isJsonArray()) {
            messages(backup.getAsJsonArray("messages"), change);
        }
    }

    private static void messages(JsonArray messages, java.util.function.UnaryOperator<String> change) {
        for (JsonElement element : messages) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject message = element.getAsJsonObject();
            data(message, change);
            // backups of older versions have the replies of a part inside of it
            if (message.has("children") && message.get("children").isJsonArray()) {
                messages(message.getAsJsonArray("children"), change);
            }
        }
    }

    /** The data of the book or of a part: its {@code extendedContent}; the uuids of the book, the parts and their parents are not looked at. */
    private static void data(JsonObject owner, java.util.function.UnaryOperator<String> change) {
        JsonElement data = owner.get("extendedContent");
        if (data != null && data.isJsonObject()) {
            walk(data.getAsJsonObject(), change);
        }
    }

    private static void walk(JsonObject object, java.util.function.UnaryOperator<String> change) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonObject()) {
                walk(value.getAsJsonObject(), change);
            } else if (value.isJsonArray()) {
                walk(value.getAsJsonArray(), change);
            } else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                String text = value.getAsString();
                String changed = change.apply(text);
                if (!changed.equals(text)) {
                    entry.setValue(new JsonPrimitive(changed));
                }
            }
        }
    }

    private static void walk(JsonArray array, java.util.function.UnaryOperator<String> change) {
        for (int i = 0; i < array.size(); i++) {
            JsonElement value = array.get(i);
            if (value.isJsonObject()) {
                walk(value.getAsJsonObject(), change);
            } else if (value.isJsonArray()) {
                walk(value.getAsJsonArray(), change);
            } else if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                String text = value.getAsString();
                String changed = change.apply(text);
                if (!changed.equals(text)) {
                    array.set(i, new JsonPrimitive(changed));
                }
            }
        }
    }
}
