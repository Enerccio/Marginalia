package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.utils.MarkdownArchive;

import java.util.List;
import java.util.Objects;
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
    default ImportResult<Manuscript> appendParts(Manuscript manuscript, MarkdownArchive.Content content, BooleanSupplier stopped,
                                                 Runnable onStored) throws Exception {
        return appendParts(manuscript, content.items().stream().map(MarkdownArchive.Item::text).toList(), RevisionDetail.ARCHIVE_IMPORT,
                stopped, onStored);
    }

    /**
     * Like {@link #appendParts(Manuscript, MarkdownArchive.Content, BooleanSupplier, Runnable)} for texts that are not files of an
     * archive (the parts cut from a document), the revisions of the parts say where the text came from.
     *
     * @param texts  the text of each part, in order
     * @param source {@link RevisionDetail#ARCHIVE_IMPORT} or {@link RevisionDetail#DOCUMENT_IMPORT}
     */
    default ImportResult<Manuscript> appendParts(Manuscript manuscript, List<String> texts, RevisionDetail source, BooleanSupplier stopped,
                                                 Runnable onStored) throws Exception {
        return insertParts(manuscript, Insertion.END, texts, source, stopped, onStored);
    }

    /**
     * Where the parts of an import go in the story of a book: after its last part, or between two parts. The parts of the story that
     * follow the place (all their branches) follow the new parts then, see {@link ChatMessageService#insertPart}.
     *
     * @param after the part the new parts come after; null for the place before the whole story; not used for the end
     * @param end   true for the end of the active branch of the story (a book without parts has only this place)
     */
    record Insertion(ChatMessage after, boolean end) {

        /**
         * After the last part of the story.
         */
        public static final Insertion END = new Insertion(null, true);

        /**
         * Before the first part of the story.
         */
        public static final Insertion START = new Insertion(null, false);

        /**
         * Right after the part.
         */
        public static Insertion after(ChatMessage part) {
            return new Insertion(Objects.requireNonNull(part, "part"), false);
        }
    }

    /**
     * Adds a part for each text at the place, in the order of the texts, as {@link #appendParts} does at the end: each part follows
     * the one before it, the texts are counted and the parts are not flagged as edited. At the end the first part follows the active
     * leaf, so the active leaf is the last part stored; elsewhere the active leaf stays where it is (it is the last part stored only
     * when the place was the end of the branch) and the story is that many parts longer. The summaries that covered the place are
     * outdated, as they describe the story without the new parts.
     *
     * @param texts   the text of each part, in order
     * @param source  {@link RevisionDetail#ARCHIVE_IMPORT} or {@link RevisionDetail#DOCUMENT_IMPORT}
     * @param stopped asked before each part, true stops the import
     * @param onStored called after each part was stored
     * @throws IllegalArgumentException when the manuscript is not the user's, the part of the place is not a part of it, or it has
     *                                  parts but no active leaf (the end)
     */
    ImportResult<Manuscript> insertParts(Manuscript manuscript, Insertion place, List<String> texts, RevisionDetail source,
                                         BooleanSupplier stopped, Runnable onStored) throws Exception;

    /**
     * As {@link #insertParts(Manuscript, Insertion, List, RevisionDetail, BooleanSupplier, Runnable)} for the files of an archive.
     */
    default ImportResult<Manuscript> insertParts(Manuscript manuscript, Insertion place, MarkdownArchive.Content content,
                                                 BooleanSupplier stopped, Runnable onStored) throws Exception {
        return insertParts(manuscript, place, content.items().stream().map(MarkdownArchive.Item::text).toList(), RevisionDetail.ARCHIVE_IMPORT,
                stopped, onStored);
    }

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
