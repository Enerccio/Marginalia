package com.github.enerccio.marginalia.lorebook;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Lorebook made from a zip archive of markdown files.
 */
class LorebookArchiveImportTest extends MarginaliaTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @BeforeEach
    void setUp() throws Exception {
        login();
    }

    @Test
    void entriesFollowTheNamesOfTheFiles() throws Exception {
        Map<String, String> files = new LinkedHashMap<>();
        files.put("chunks/10.md", "ten");
        files.put("chunks/2.md", "# Second: heading\n\ntext of two");
        files.put("chunks/1.md", "﻿\n## First\nbody");
        files.put("chunks/notes.txt", "not markdown");
        files.put("chunks/.hidden.md", "hidden");
        files.put("__MACOSX/chunks/1.md", "resource fork");
        files.put("chunks/sub/", null);

        Lorebook book = lorebookService.importFromArchive(zip(files), "story.zip", null);

        assertThat(book.getName()).isEqualTo("story");
        assertThat(book.isEnabled()).isTrue();
        List<LorebookEntry> entries = lorebookEntryService.getEntriesForLorebook(book).stream()
                .sorted(java.util.Comparator.comparingInt(LorebookEntry::getOrder)).toList();
        assertThat(entries).extracting(LorebookEntry::getName).containsExactly("First", "Second: heading", "10");
        assertThat(entries).extracting(LorebookEntry::getOrder).containsExactly(1, 2, 3);
        assertThat(entries).extracting(LorebookEntry::isEnabled).containsOnly(false);
        assertThat(entries.get(2).getPayload()).isEqualTo("ten");
        assertThat(entries.get(1).getPayload()).isEqualTo("# Second: heading\n\ntext of two");
    }

    @Test
    void givenNamesWinOverNamesTxtAndHeadings() throws Exception {
        Map<String, String> files = new LinkedHashMap<>();
        files.put("1.md", "# Heading one");
        files.put("2.md", "# Heading two");
        files.put("3.md", "# Heading three");
        files.put("4.md", "body four");
        files.put("names.txt", "Alice\n\nCarol\n");

        assertThat(importedNames(zip(files), null)).containsExactly("Alice", "Heading two", "Carol", "4");
        assertThat(importedNames(zip(files), List.of("Given", " ", "", "Dave"))).containsExactly("Given", "Heading two", "Carol", "Dave");
    }

    @Test
    void archiveWithoutMarkdownIsRejected() throws Exception {
        Map<String, String> files = new LinkedHashMap<>();
        files.put("a.txt", "text");
        assertThatThrownBy(() -> lorebookService.importFromArchive(zip(files), "x.zip", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void notAnArchiveIsRejected() {
        assertThatThrownBy(() -> lorebookService.importFromArchive("plain".getBytes(StandardCharsets.UTF_8), "x.zip", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private List<String> importedNames(byte[] archive, List<String> names) throws Exception {
        Lorebook book = lorebookService.importFromArchive(archive, "names.zip", names);
        return lorebookEntryService.getEntriesForLorebook(book).stream()
                .sorted(java.util.Comparator.comparingInt(LorebookEntry::getOrder))
                .map(LorebookEntry::getName).toList();
    }

    private static byte[] zip(Map<String, String> files) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Map.Entry<String, String> file : files.entrySet()) {
                zip.putNextEntry(new ZipEntry(file.getKey()));
                if (file.getValue() != null) {
                    zip.write(file.getValue().getBytes(StandardCharsets.UTF_8));
                }
                zip.closeEntry();
            }
        }
        return out.toByteArray();
    }
}
