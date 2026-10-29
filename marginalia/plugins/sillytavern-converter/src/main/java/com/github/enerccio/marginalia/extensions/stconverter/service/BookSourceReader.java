package com.github.enerccio.marginalia.extensions.stconverter.service;

import com.github.enerccio.marginalia.extensions.stconverter.model.BookSource;
import com.github.enerccio.marginalia.extensions.stconverter.model.BookSource.Part;
import com.github.enerccio.marginalia.extensions.stconverter.model.ChatLine;
import com.github.enerccio.marginalia.extensions.stconverter.model.TurnItem;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.github.enerccio.marginalia.extensions.stconverter.STConverterLocalization.T;

/**
 * Strips a book to what the converter redrafts from ({@link BookSource}): reads the JSON of a backup (the same shape for a
 * file and for a book that is backed up on the fly), follows the parents from the active leaf to the first part and keeps the
 * instructions and the text of each part.
 */
public final class BookSourceReader {

    /** The first bytes of a ZIP, the backup with the images is one. */
    private static final int ZIP_MAGIC_1 = 0x50;
    private static final int ZIP_MAGIC_2 = 0x4B;
    private static final String ARCHIVE_ENTRY = "backup.json";

    private static final Gson GSON = new Gson();

    private BookSourceReader() {
    }

    // --- stripping a backup ---

    /**
     * The parts of a backup file: the JSON of a backup, the file stored in the backups of a book (the backup is in its
     * {@code backup} property), or the archive with the images ({@code backup.json} in it).
     *
     * @throws IllegalArgumentException when the file is not a backup, or the chain of parts is broken
     */
    public static BookSource fromBackupFile(byte[] file) {
        JsonObject root = parse(isArchive(file) ? unzip(file) : file);
        JsonObject inner = root.has("backup") && root.get("backup").isJsonObject() ? root.getAsJsonObject("backup") : root;
        String name = StringUtils.defaultIfBlank(string(root, "manuscriptName"), string(inner, "name"));
        return fromBackup(inner, BookSource.ORIGIN_BACKUP, name);
    }

    /**
     * @param backup the JSON of a backup of the book (the {@code messages} are flat, each with its {@code uuid} and
     *               {@code parentUuid}; {@code activeLeafUuid} is the end of the branch the book is on)
     * @return the parts from the first one to the active leaf, none when the book has no active leaf (an empty book)
     * @throws IllegalArgumentException when it is not a backup, or the active leaf or a parent of the chain is not there
     */
    public static BookSource fromBackup(JsonObject backup, String origin, String fallbackName) {
        if (backup == null || !backup.has("messages") || !backup.get("messages").isJsonArray()) {
            throw new IllegalArgumentException(T.localized("Not a backup of a book"));
        }
        BookSource source = new BookSource();
        source.setName(StringUtils.defaultIfBlank(string(backup, "name"), fallbackName));
        source.setOrigin(origin);
        source.setCreated(Instant.now().toString());

        String leaf = string(backup, "activeLeafUuid");
        if (leaf == null) {
            return source;
        }
        Map<String, JsonObject> byUuid = new HashMap<>();
        for (JsonElement element : backup.getAsJsonArray("messages")) {
            if (element.isJsonObject() && string(element.getAsJsonObject(), "uuid") != null) {
                byUuid.put(string(element.getAsJsonObject(), "uuid"), element.getAsJsonObject());
            }
        }

        // from the leaf up to the first part; a missing part or a loop means a damaged backup
        Deque<JsonObject> path = new ArrayDeque<>();
        Set<String> seen = new HashSet<>();
        for (String uuid = leaf; uuid != null; ) {
            JsonObject message = byUuid.get(uuid);
            if (message == null || !seen.add(uuid)) {
                throw new IllegalArgumentException(T.localized("The backup is damaged: the parts do not lead to the first part"));
            }
            path.addFirst(message);
            uuid = string(message, "parentUuid");
        }

        List<Part> parts = new ArrayList<>();
        for (JsonObject message : path) {
            JsonObject content = message.has("extendedContent") && message.get("extendedContent").isJsonObject()
                    ? message.getAsJsonObject("extendedContent") : new JsonObject();
            parts.add(new Part(string(content, "instructions"), string(content, "sceneSetting"),
                    string(content, "povCharacter"), string(content, "presentCharacters"), string(content, "response")));
        }
        source.setParts(parts);
        return source;
    }

    /**
     * The source made of the parts of a book that is read directly (the branch it is on, from the first part).
     */
    public static BookSource of(String name, String origin, List<Part> parts) {
        BookSource source = new BookSource();
        source.setName(name);
        source.setOrigin(origin);
        source.setCreated(Instant.now().toString());
        source.setParts(new ArrayList<>(parts));
        return source;
    }

    // --- the stored file ---

    public static byte[] toBytes(BookSource source) {
        return GSON.toJson(source).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * @throws IllegalArgumentException when the bytes are not a redraft source of a version this plugin knows
     */
    public static BookSource read(byte[] file) {
        JsonObject stored;
        try {
            JsonElement root = JsonParser.parseString(new String(file, StandardCharsets.UTF_8));
            stored = root.isJsonObject() ? root.getAsJsonObject() : null;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(T.localized("The stored source of the book can't be read"), e);
        }
        // the property itself, the class has the default format and would pass any JSON object
        if (stored == null || !BookSource.FORMAT.equals(string(stored, "format"))) {
            throw new IllegalArgumentException(T.localized("The stored source of the book can't be read"));
        }
        JsonElement version = stored.get("version");
        if (version != null && version.isJsonPrimitive() && version.getAsJsonPrimitive().isNumber()
                && version.getAsInt() > BookSource.VERSION) {
            throw new IllegalArgumentException(T.localized("The stored source of the book is from a newer version of the plugin"));
        }
        try {
            return GSON.fromJson(stored, BookSource.class);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(T.localized("The stored source of the book can't be read"), e);
        }
    }

    // --- what the tab shows ---

    /** The text of each part as a message of the list on the left, from 1. */
    public static List<ChatLine> toLines(BookSource source) {
        List<ChatLine> lines = new ArrayList<>();
        for (Part part : source.getParts()) {
            lines.add(new ChatLine(StringUtils.defaultString(part.getPovCharacter()), false, StringUtils.defaultString(part.getText())));
        }
        return lines;
    }

    /** The instructions of each part as the turn details, the part {@code n} is the message {@code n}. */
    public static List<TurnItem> toTurns(BookSource source) {
        List<TurnItem> turns = new ArrayList<>();
        int number = 1;
        for (Part part : source.getParts()) {
            turns.add(new TurnItem(number, number, part.getSceneSetting(), part.getPovCharacter(), part.getPresentCharacters(),
                    part.getInstructions()));
            number++;
        }
        return turns;
    }

    // --- the file ---

    private static boolean isArchive(byte[] file) {
        return file.length > 1 && (file[0] & 0xFF) == ZIP_MAGIC_1 && (file[1] & 0xFF) == ZIP_MAGIC_2;
    }

    private static byte[] unzip(byte[] file) {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(file))) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (ARCHIVE_ENTRY.equals(entry.getName())) {
                    return zip.readAllBytes();
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(T.localized("Not a backup of a book"), e);
        }
        throw new IllegalArgumentException(T.localized("Not a backup of a book"));
    }

    private static JsonObject parse(byte[] file) {
        String text = new String(file, StandardCharsets.UTF_8);
        if (text.startsWith("﻿")) {
            text = text.substring(1);
        }
        try {
            JsonElement root = JsonParser.parseString(text);
            if (root.isJsonObject()) {
                return root.getAsJsonObject();
            }
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(T.localized("Not a backup of a book"), e);
        }
        throw new IllegalArgumentException(T.localized("Not a backup of a book"));
    }

    /** The text of a property, null when it is missing, null or not a string. */
    private static String string(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }
}
