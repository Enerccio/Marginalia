package com.github.enerccio.marginalia.domain.templates;

import com.github.enerccio.marginalia.domain.traits.LocalizedTemplateDescription;
import com.github.enerccio.marginalia.loc.L;

/**
 * The values of the title page template of an export.
 */
public class ExportHeaderTemplateData extends TemplateData {

    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_MANUSCRIPT_NAME)
    private final String title;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_EXPORT_AUTHOR)
    private final String author;

    /**
     * A sample without values, for the check of the names a template uses.
     */
    public ExportHeaderTemplateData() {
        this(null, null);
    }

    public ExportHeaderTemplateData(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }
}
