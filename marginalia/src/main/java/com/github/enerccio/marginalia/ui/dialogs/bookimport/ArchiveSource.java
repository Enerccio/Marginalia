package com.github.enerccio.marginalia.ui.dialogs.bookimport;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ArchiveImportRunner;
import com.github.enerccio.marginalia.utils.MarkdownArchive;

import java.io.IOException;

/**
 * A zip archive of {@code .md} files, a part for each file ({@link MarkdownArchive}). It has no options.
 */
final class ArchiveSource extends BookImportSource {

    private final boolean intoBook;
    private MarkdownArchive.Content content;
    private String name = "";

    /**
     * @param intoBook true when the files go into a book that exists (the text does not speak of a new book)
     */
    ArchiveSource(Localization loc, boolean intoBook) {
        super(loc);
        this.intoBook = intoBook;
    }

    @Override
    L label() {
        return L.LABEL_IMPORT_FROM_ARCHIVE;
    }

    @Override
    L description() {
        return intoBook ? L.MSG_IMPORT_ARCHIVE_TO_BOOK : L.MSG_IMPORT_ARCHIVE_NEW_BOOK;
    }

    @Override
    String[] fileTypes() {
        return new String[]{"application/zip", "application/x-zip-compressed", ".zip"};
    }

    @Override
    void read(byte[] data, String fileName) {
        try {
            content = MarkdownArchive.read(data);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        name = fileName.toLowerCase().endsWith(".zip") ? fileName.substring(0, fileName.length() - 4).trim() : fileName.trim();
    }

    @Override
    String invalid(String fileName) {
        return loc.getValue(L.MSG_INVALID_ARCHIVE);
    }

    @Override
    void reset() {
        content = null;
        name = "";
    }

    @Override
    String status() {
        return content == null ? "" : String.format(loc.getValue(L.MSG_ARCHIVE_FILES_READY), content.items().size());
    }

    @Override
    boolean ready() {
        return content != null;
    }

    @Override
    String suggestedName() {
        return name;
    }

    @Override
    BookImportDialog.Run prepare() {
        MarkdownArchive.Content read = content;
        return (manuscript, place, afterwards) -> new ArchiveImportRunner().insertParts(manuscript, place, read, afterwards);
    }
}
