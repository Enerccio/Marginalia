package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportTemplate;
import com.github.enerccio.marginalia.ui.dialogs.ProgressBarDialog;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Markdown for static site generators (Jekyll, Hugo, Eleventy and the like): the same text as {@link MarkdownExporter}, but it starts with
 * a YAML front matter block (the title, the author, the language and the date) instead of a title page, which the generator's theme shows.
 */
public class FrontMatterMarkdownExporter extends MarkdownExporter {

    @Override
    public String getName() {
        return "Markdown with front matter";
    }

    @Override
    public String getId() {
        return "markdown-front-matter";
    }

    @Override
    public List<ExportTemplate> getTemplates() {
        return List.of(FRONT_MATTER_TEMPLATE, AUTHORSHIP_TEMPLATE);
    }

    @Override
    public void exportTo(ExportOptions options, List<ChatMessage> chatMessages, ProgressBarDialog progress, Path target) throws Exception {
        // the front matter is the title page of this format
        ExportOptions withoutTitlePage = new ExportOptions(options.bookName(), options.author(), false, options.language(), options.style(),
                options.templates(), options.lore(), options.authorship());
        super.exportTo(withoutTitlePage, chatMessages, progress, target);
    }

    @Override
    protected ExportWriter open(ExportOptions options, OutputStream out) throws Exception {
        return new MarkdownWriter(out, buildFrontMatter(options).strip());
    }
}
