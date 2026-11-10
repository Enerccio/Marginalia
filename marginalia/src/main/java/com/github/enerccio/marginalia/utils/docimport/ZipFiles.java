package com.github.enerccio.marginalia.utils.docimport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The files of an uploaded zip archive that a reader wants (an EPUB, a zipped Obsidian vault or Scrivener project), in
 * memory. The rest (pictures, attachments) is skipped without being kept, and the sizes are limited: the archive comes from
 * the user's upload.
 */
final class ZipFiles {

    static final int MAX_ENTRY_BYTES = 64 * 1024 * 1024;
    static final long MAX_TOTAL_BYTES = 256L * 1024 * 1024;
    static final int MAX_ENTRIES = 100_000;

    private ZipFiles() {
    }

    /**
     * @param wanted gets the name of an entry (slashes as separators, no directories) and says whether to keep it
     * @return the kept entries by name, in the order of the archive
     * @throws IllegalArgumentException when the data is not a zip archive or is too big
     */
    static Map<String, byte[]> read(byte[] data, Predicate<String> wanted) {
        Map<String, byte[]> files = new LinkedHashMap<>();
        long total = 0;
        int entries = 0;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (++entries > MAX_ENTRIES) {
                    throw new IllegalArgumentException("The archive has too many files");
                }
                String name = entry.getName().replace('\\', '/');
                if (entry.isDirectory() || !wanted.test(name)) {
                    continue;
                }
                byte[] bytes = zip.readNBytes(MAX_ENTRY_BYTES + 1);
                total += bytes.length;
                if (bytes.length > MAX_ENTRY_BYTES || total > MAX_TOTAL_BYTES) {
                    throw new IllegalArgumentException("The archive is too big");
                }
                files.put(name, bytes);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Not a zip archive: " + e.getMessage(), e);
        }
        if (entries == 0) {
            throw new IllegalArgumentException("Not a zip archive");
        }
        return files;
    }

    /**
     * @return the last part of the path
     */
    static String fileName(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    /**
     * @return the file name without its last extension
     */
    static String baseName(String path) {
        String name = fileName(path);
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
