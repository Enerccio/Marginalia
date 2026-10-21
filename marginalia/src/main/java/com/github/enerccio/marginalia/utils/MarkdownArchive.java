package com.github.enerccio.marginalia.utils;

import com.github.enerccio.marginalia.loc.NaturalOrderComparator;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The {@code .md} files of an uploaded zip archive, for the imports that make a lorebook or a story of them. The files are
 * in natural order of their paths (numbers inside the names compare as numbers). Folders, hidden files, the
 * {@code __MACOSX} folder and files of other types are left out. An optional {@code names.txt} in the root of the archive
 * gives the names of the files, one on a line, in the same order.
 */
public final class MarkdownArchive {

    public static final String NAMES_FILE = "names.txt";

    private static final int MAX_FILE_BYTES = 16 * 1024 * 1024;
    private static final long MAX_TOTAL_BYTES = 256L * 1024 * 1024;

    /**
     * @param path the path inside the archive
     * @param text the content
     */
    public record Item(String path, String text) {

        /**
         * The first heading of the file when it starts with one, else the file name without the extension.
         */
        public String defaultName() {
            for (String line : text.split("\\R", 20)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                if (trimmed.startsWith("#")) {
                    String heading = StringUtils.stripStart(trimmed, "#").trim();
                    if (!heading.isEmpty()) {
                        return heading;
                    }
                }
                break;
            }
            String fileName = path.substring(path.lastIndexOf('/') + 1);
            return fileName.substring(0, fileName.length() - 3);
        }
    }

    /**
     * The content of the archive.
     *
     * @param items the {@code .md} files in order
     * @param names the lines of {@code names.txt} (empty when the archive has none), blank lines included
     */
    public record Content(List<Item> items, List<String> names) {

        /**
         * The name of each file: the line of {@code names.txt} at its place when there is one that is not blank, else
         * {@link Item#defaultName()}.
         */
        public List<String> itemNames() {
            List<String> result = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                String named = i < names.size() ? names.get(i).trim() : "";
                result.add(named.isEmpty() ? items.get(i).defaultName() : named);
            }
            return result;
        }
    }

    private MarkdownArchive() {
    }

    /**
     * @throws IllegalArgumentException when the data is not a zip archive, has no {@code .md} file or is too big
     */
    public static Content read(byte[] archive) throws IOException {
        if (archive == null || archive.length == 0) {
            throw new IllegalArgumentException("Archive cannot be empty");
        }
        Map<String, String> files = new TreeMap<>(new NaturalOrderComparator(Collator.getInstance(Locale.ROOT)));
        List<String> names = new ArrayList<>();
        long total = 0;
        boolean any = false;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive), StandardCharsets.UTF_8)) {
            ZipEntry zipEntry;
            while ((zipEntry = zip.getNextEntry()) != null) {
                any = true;
                String path = zipEntry.getName();
                String fileName = path.substring(path.lastIndexOf('/') + 1);
                boolean namesFile = path.equalsIgnoreCase(NAMES_FILE);
                if (zipEntry.isDirectory() || path.startsWith("__MACOSX/") || fileName.startsWith(".")
                        || !namesFile && !fileName.toLowerCase().endsWith(".md")) {
                    continue;
                }
                byte[] data = zip.readNBytes(MAX_FILE_BYTES + 1);
                total += data.length;
                if (data.length > MAX_FILE_BYTES || total > MAX_TOTAL_BYTES) {
                    throw new IllegalArgumentException("Archive is too big");
                }
                String text = new String(data, StandardCharsets.UTF_8);
                if (text.startsWith("﻿")) {
                    text = text.substring(1);
                }
                if (namesFile) {
                    names.addAll(List.of(text.split("\\R", -1)));
                } else {
                    files.put(path, text);
                }
            }
        }
        if (!any) {
            throw new IllegalArgumentException("Not a zip archive");
        }
        if (files.isEmpty()) {
            throw new IllegalArgumentException("Archive has no .md file");
        }
        List<Item> items = new ArrayList<>();
        files.forEach((path, text) -> items.add(new Item(path, text)));
        // a final line break of the file is not an empty name
        if (!names.isEmpty() && names.getLast().isEmpty()) {
            names.removeLast();
        }
        return new Content(items, names);
    }
}
