package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.ui.dialogs.ProgressBarDialog;

import java.util.List;

public interface ExporterService {

    void registerExporter(Exporter exporter);
    void unregisterExporter(Exporter exporter);
    List<Exporter> getExporters();


    /**
     * Exporters are shared between all users and run in parallel, they must not keep state of an export between calls.
     */
    interface Exporter {

        /**
         * @return name of the format shown to the user
         */
        String getName();

        /**
         * @return extension of the exported file without the dot
         */
        String getFileExtension();

        String getMimeType();

        /**
         * @param chatMessages messages to export in the story order
         * @param progress     dialog to report finished messages to with {@link ProgressBarDialog#updateProgress()}
         * @return content of the exported file
         */
        byte[] export(ExportOptions options, List<ChatMessage> chatMessages, ProgressBarDialog progress) throws Exception;

        /**
         * @return whether the exporter looks at {@link ExportOptions#style()} (a format that has fonts, sizes and colors: HTML, EPUB, PDF,
         * Word); the export dialog offers the choice of a style only for these
         */
        default boolean supportsStyles() {
            return false;
        }

        /**
         * @return id of the style (a style {@link com.github.enerccio.marginalia.domain.styles.PartStyle#isForExport() for exports}) that
         * is the look of the format when the author chooses none, which the export dialog preselects for the format; null for none
         */
        default String getDefaultStyleId() {
            return null;
        }

    }

    /**
     * @param bookName       title of the exported book
     * @param author         author of the exported book, may be blank
     * @param includeHeaders true to start the export with a title page
     * @param language       ISO 639-1 code of the language of the book
     * @param style          text style (see {@code PartStyle}) the author wants the exported text to have, null for the look the exporter had before
     *                       styles ({@link Exporter#getDefaultStyleId()});
     *                       only {@link Exporter#supportsStyles() exporters that support styles} use it
     */
    record ExportOptions(String bookName, String author, boolean includeHeaders, String language, PartStyle style) {

        /**
         * An export in the look of the exporter.
         */
        public ExportOptions(String bookName, String author, boolean includeHeaders, String language) {
            this(bookName, author, includeHeaders, language, null);
        }

    }

}
