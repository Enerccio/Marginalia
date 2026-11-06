package com.github.enerccio.marginalia.workset;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.BackupService;
import com.github.enerccio.marginalia.domain.service.LorebookService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * The book of the dataset as a fixture of the tests ({@code src/test/resources/workset}, see the README of this package): restored the way a user restores a backup,
 * the lorebook first (a backup only links its lorebook, by a fixed uuid).
 */
final class WorksetFixture {

    static final String BOOK = "/workset/book.zip";
    static final String LOREBOOK = "/workset/lorebook.json";

    private WorksetFixture() {
    }

    /** The archive of the book as a file ({@code deleteOnExit}), for tests that read it themselves. */
    static Path archive() throws Exception {
        Path file = Files.createTempFile("workset-book", ".zip");
        file.toFile().deleteOnExit();
        try (InputStream in = WorksetFixture.class.getResourceAsStream(BOOK)) {
            if (in == null) {
                throw new IllegalStateException("the book of the dataset is not in the test resources: " + BOOK);
            }
            Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
        }
        return file;
    }

    /** Imports the lorebook of the book into the account of the logged in user. */
    static void importLorebook(LorebookService lorebooks) throws Exception {
        try (InputStream in = WorksetFixture.class.getResourceAsStream(LOREBOOK)) {
            if (in == null) {
                throw new IllegalStateException("the lorebook of the dataset is not in the test resources: " + LOREBOOK);
            }
            lorebooks.importLorebook(new String(in.readAllBytes(), StandardCharsets.UTF_8), "Unregistered");
        }
    }

    /** Restores the book as a new one of the logged in user (the lorebook is imported first when {@code withLorebook}). */
    static Manuscript restore(BackupService backups, LorebookService lorebooks, String name, boolean withLorebook) throws Exception {
        if (withLorebook) {
            importLorebook(lorebooks);
        }
        return backups.restoreAsNewManuscript(archive().toFile(), name, Map.of());
    }
}
