package com.github.enerccio.marginalia.domain.templates;

import com.github.enerccio.marginalia.domain.traits.LocalizedTemplateDescription;
import com.github.enerccio.marginalia.loc.L;

/**
 * The values of the front matter template of an export (the YAML block that static site generators and Obsidian read). The title and the
 * author are escaped for use inside double quotes.
 */
public class ExportFrontMatterTemplateData extends TemplateData {

    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_EXPORT_QUOTED_TITLE)
    private final String title;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_EXPORT_QUOTED_AUTHOR)
    private final String author;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_EXPORT_LANGUAGE)
    private final String language;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_EXPORT_DATE)
    private final String exportDate;

    /**
     * A sample without values, for the check of the names a template uses.
     */
    public ExportFrontMatterTemplateData() {
        this(null, null, null, null);
    }

    public ExportFrontMatterTemplateData(String title, String author, String language, String date) {
        this.title = title;
        this.author = author;
        this.language = language;
        this.exportDate = date;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getLanguage() {
        return language;
    }

    public String getExportDate() {
        return exportDate;
    }
}
