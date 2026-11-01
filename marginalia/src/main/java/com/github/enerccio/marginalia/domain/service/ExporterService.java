package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.templates.TemplateData;
import com.github.enerccio.marginalia.ui.dialogs.ProgressBarDialog;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ExporterService {

    void registerExporter(Exporter exporter);
    void unregisterExporter(Exporter exporter);
    List<Exporter> getExporters();

    /**
     * Reads the lore of the book for the exporters that {@link Exporter#usesLore() use it}. It is read on the thread of the user, the
     * exporters run without the session.
     *
     * @return the enabled entries of the lorebook of the book and of its subbooks, however deep (the disabled lorebooks and entries
     * are left out), the lorebook first; empty when the book has no lorebook
     */
    List<ExportLoreEntry> collectLore(Manuscript manuscript) throws Exception;


    /**
     * Exporters are shared between all users and run in parallel, they must not keep state of an export between calls.
     */
    interface Exporter {

        /**
         * @return name of the format shown to the user
         */
        String getName();

        /**
         * @return stable identifier of the exporter, under which the user's export settings (the default style, the changed templates) are
         * kept; unique among the exporters, and it must not change between versions. The file extension unless the exporter says otherwise.
         */
        default String getId() {
            return getFileExtension();
        }

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

        /**
         * @return the templates of the exporter that the user can change in the settings (the title page, for example), in the order they are
         * shown; none for an exporter that writes everything itself
         */
        default List<ExportTemplate> getTemplates() {
            return List.of();
        }

        /**
         * @return whether the exporter writes the lorebook of the book ({@link ExportOptions#lore()}); the export dialog reads it only for
         * these
         */
        default boolean usesLore() {
            return false;
        }

    }

    /**
     * A text of an export that the user can change: a Handlebars template the exporter renders.
     *
     * @param key         identifier of the template among the templates of its exporter, stable like {@link Exporter#getId()}
     * @param label       name shown to the user
     * @param defaultText the template when the user changed none
     * @param dataClass   the values the template is rendered with, which the settings list as the variables it can use
     */
    record ExportTemplate(String key, String label, String defaultText, Class<? extends TemplateData> dataClass) {
    }

    /**
     * An entry of the lorebook of the book, as the exporters that write the lore get it.
     *
     * @param lorebook name of the lorebook the entry is in
     * @param name     name of the entry
     * @param content  text of the entry
     * @param tags     tags of the entry
     * @param comment  note of the author to the entry, may be blank
     */
    record ExportLoreEntry(String lorebook, String name, String content, List<String> tags, String comment) {

        public ExportLoreEntry {
            tags = tags == null ? List.of() : List.copyOf(tags);
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
     * @param templates      the templates of the exporter that the user changed, by {@link ExportTemplate#key()}; a template that is not here
     *                       is rendered as {@link ExportTemplate#defaultText()}
     * @param lore           the lore of the book, filled only for the exporters that {@link Exporter#usesLore() use it}
     */
    record ExportOptions(String bookName, String author, boolean includeHeaders, String language, PartStyle style,
                         Map<String, String> templates, List<ExportLoreEntry> lore) {

        public ExportOptions {
            Map<String, String> changed = new HashMap<>();
            if (templates != null) {
                templates.forEach((key, text) -> {
                    if (StringUtils.isNotBlank(text)) {
                        changed.put(key, text);
                    }
                });
            }
            templates = Map.copyOf(changed);
            lore = lore == null ? List.of() : List.copyOf(lore);
        }

        /**
         * An export without the lore.
         */
        public ExportOptions(String bookName, String author, boolean includeHeaders, String language, PartStyle style,
                             Map<String, String> templates) {
            this(bookName, author, includeHeaders, language, style, templates, null);
        }

        /**
         * An export with the default templates of the exporter.
         */
        public ExportOptions(String bookName, String author, boolean includeHeaders, String language, PartStyle style) {
            this(bookName, author, includeHeaders, language, style, null);
        }

        /**
         * An export in the look of the exporter.
         */
        public ExportOptions(String bookName, String author, boolean includeHeaders, String language) {
            this(bookName, author, includeHeaders, language, null, null);
        }

        /**
         * @return the text of the template for this export: the one the user changed, else its default
         */
        public String templateText(ExportTemplate template) {
            return templates.getOrDefault(template.key(), template.defaultText());
        }

    }

}
