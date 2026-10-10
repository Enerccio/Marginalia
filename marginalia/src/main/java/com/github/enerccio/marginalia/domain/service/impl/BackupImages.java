package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.ImageAttachment;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * The images of the parts as they are in the JSON of a book backup: a part has them in the {@code imageAttachments}
 * string of its {@code extendedContent} (see {@code ChatMessage.getImages()}). The files are not in the JSON.
 */
final class BackupImages {
    private static final Gson GSON = new Gson();
    private static final Type IMAGES_TYPE = new TypeToken<List<ImageAttachment>>() {
    }.getType();
    private static final String KEY = "imageAttachments";

    private BackupImages() {
    }

    /**
     * @param backup the book backup (the object with the {@code messages})
     * @return uuids of the resources the parts use, in the order of their first use
     */
    static Set<String> collect(JsonObject backup) {
        Set<String> uuids = new LinkedHashSet<>();
        forEachMessage(backup, message -> {
            for (ImageAttachment image : images(message)) {
                if (image.resource() != null) {
                    uuids.add(image.resource());
                }
            }
        });
        return uuids;
    }

    /**
     * Changes the resource of every image of the parts.
     *
     * @param resolver gives the resource that is to replace the given one, null drops the image from the part
     */
    static void remap(JsonObject backup, UnaryOperator<String> resolver) {
        forEachMessage(backup, message -> {
            List<ImageAttachment> images = images(message);
            if (images.isEmpty()) {
                return;
            }
            List<ImageAttachment> mapped = new ArrayList<>();
            for (ImageAttachment image : images) {
                String resource = image.resource() == null ? null : resolver.apply(image.resource());
                if (resource != null) {
                    mapped.add(new ImageAttachment(resource, image.caption()));
                }
            }
            JsonObject extended = message.getAsJsonObject("extendedContent");
            if (mapped.isEmpty()) {
                extended.remove(KEY);
            } else {
                extended.addProperty(KEY, GSON.toJson(mapped, IMAGES_TYPE));
            }
        });
    }

    private static List<ImageAttachment> images(JsonObject message) {
        if (!message.has("extendedContent") || !message.get("extendedContent").isJsonObject()) {
            return List.of();
        }
        JsonElement value = message.getAsJsonObject("extendedContent").get(KEY);
        if (value == null || !value.isJsonPrimitive()) {
            return List.of();
        }
        try {
            List<ImageAttachment> images = GSON.fromJson(value.getAsString(), IMAGES_TYPE);
            return images == null ? List.of() : images;
        } catch (RuntimeException e) {
            // not ours or damaged, the part is restored as it is
            return List.of();
        }
    }

    private static void forEachMessage(JsonObject backup, java.util.function.Consumer<JsonObject> action) {
        if (backup != null && backup.has("messages") && backup.get("messages").isJsonArray()) {
            forEachMessage(backup.getAsJsonArray("messages"), action);
        }
    }

    private static void forEachMessage(JsonArray messages, java.util.function.Consumer<JsonObject> action) {
        for (JsonElement element : messages) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject message = element.getAsJsonObject();
            action.accept(message);
            // backups of older versions have the replies of a part inside of it
            if (message.has("children") && message.get("children").isJsonArray()) {
                forEachMessage(message.getAsJsonArray("children"), action);
            }
        }
    }
}
