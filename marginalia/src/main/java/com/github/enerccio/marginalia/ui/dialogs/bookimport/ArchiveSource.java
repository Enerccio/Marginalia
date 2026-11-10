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

    private MarkdownArchive.Content content;
    private String name = "";

    ArchiveSource(Localization loc) {
        super(loc);
    }

    @Override
    L label() {
        return L.LABEL_IMPORT_FROM_ARCHIVE;
    }

    @Override
    L description() {
        return L.MSG_IMPORT_ARCHIVE_NEW_BOOK;
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
        return (manuscript, afterwards) -> new ArchiveImportRunner().appendParts(manuscript, read, afterwards);
    }
}
