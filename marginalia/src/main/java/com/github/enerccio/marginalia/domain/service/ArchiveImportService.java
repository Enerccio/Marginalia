package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.utils.MarkdownArchive;

import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Imports of the {@code .md} files of a zip archive ({@link MarkdownArchive}). The import is not one transaction: each part
 * or entry is stored by itself, so it can be stopped at any time and what was imported until then stays (an incomplete
 * import), and so a long one does not hold the database. A failure does the same, the exception is thrown with the part
 * or entry before it stored.
 */
public interface ArchiveImportService {

    /**
     * What an import did.
     *
     * @param target   the book or lorebook the files were imported to
     * @param imported how many files were stored
     * @param total    how many files there are
     */
    record ImportResult<T>(T target, int imported, int total) {

        public boolean isComplete() {
            return imported == total;
        }
    }

    /**
     * Adds a part for each file to the end of the book, in the order of the files: the first part follows the active leaf of
     * the book (it is the first part of an empty book) and each next part follows the one before, so the active leaf is the
     * last part stored. As {@link ChatMessageService#createPart}, the texts are counted and the parts are not flagged as
     * edited.
     *
     * @param stopped asked before each file, true stops the import
     * @param onStored called after each file was stored
     * @throws IllegalArgumentException when the manuscript is not the user's or it has parts but no active leaf
     */
    ImportResult<Manuscript> appendParts(Manuscript manuscript, MarkdownArchive.Content content, BooleanSupplier stopped,
                                         Runnable onStored) throws Exception;

    /**
     * Makes a new, enabled lorebook with an entry for each file, in the order of the files (the entries have the order 1, 2,
     * 3...), and stores the lorebook first, so a stopped import leaves it with the entries imported until then. The entries
     * are disabled, as an entry without a filter would be sent with every prompt.
     *
     * @param name    the name of the lorebook, usually the name of the uploaded file (a {@code .zip} ending is cut off)
     * @param names   the names of the entries in the order of the files, null for none; a missing or blank one is taken from
     *                {@link MarkdownArchive.Content#itemNames()}
     * @param stopped asked before each file, true stops the import
     * @param onStored called after each entry was stored
     */
    ImportResult<Lorebook> importLorebook(MarkdownArchive.Content content, String name, List<String> names,
                                          BooleanSupplier stopped, Runnable onStored) throws Exception;
}
