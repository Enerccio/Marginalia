package com.github.enerccio.marginalia.domain.templates;

import com.github.enerccio.marginalia.domain.components.BookAuthorship;
import com.github.enerccio.marginalia.domain.traits.LocalizedTemplateDescription;
import com.github.enerccio.marginalia.loc.L;
import org.apache.commons.lang3.StringUtils;

/**
 * The values of the authorship statement template of an export: who wrote the words of the exported text.
 */
public class ExportAuthorshipTemplateData extends TemplateData {

    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_MANUSCRIPT_NAME)
    private final String title;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_EXPORT_AUTHOR)
    private final String author;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_EXPORT_TOTAL_WORDS)
    private final long totalWords;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_EXPORT_WRITTEN_WORDS)
    private final long writtenWords;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_EXPORT_GENERATED_WORDS)
    private final long generatedWords;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_EXPORT_WRITTEN_PERCENT)
    private final long writtenPercent;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_EXPORT_GENERATED_PERCENT)
    private final long generatedPercent;
    @LocalizedTemplateDescription(loc = L.DESC_TEMPLATE_EXPORT_MODELS)
    private final String models;

    /**
     * A sample without values, for the check of the names a template uses.
     */
    public ExportAuthorshipTemplateData() {
        this(null, null, new BookAuthorship(0, 0, 0, 0, null));
    }

    public ExportAuthorshipTemplateData(String title, String author, BookAuthorship authorship) {
        this.title = title;
        this.author = author;
        this.totalWords = authorship.totalWords();
        this.writtenWords = authorship.writtenWords();
        this.generatedWords = authorship.generatedWords();
        // the two add up to 100, the rounding of the generated share is the rest of the written one
        this.generatedPercent = Math.round(authorship.generatedShare() * 100);
        this.writtenPercent = authorship.totalWords() == 0 ? 0 : 100 - generatedPercent;
        this.models = StringUtils.trimToNull(String.join(", ", authorship.models()));
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public long getTotalWords() {
        return totalWords;
    }

    public long getWrittenWords() {
        return writtenWords;
    }

    public long getGeneratedWords() {
        return generatedWords;
    }

    public long getWrittenPercent() {
        return writtenPercent;
    }

    public long getGeneratedPercent() {
        return generatedPercent;
    }

    public String getModels() {
        return models;
    }
}
