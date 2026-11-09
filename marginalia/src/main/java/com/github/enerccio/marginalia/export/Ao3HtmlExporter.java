package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportTemplate;
import org.apache.commons.lang3.StringUtils;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.renderer.html.HtmlRenderer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * HTML for the editor of Archive of Our Own. AO3 has its own fields for the title, the author and the chapter titles, and its sanitizer
 * keeps only a few tags and drops what it does not know (the images that are not links, the {@code style} blocks, the page around the text),
 * so the file has the text of every chapter between two comments, clean of anything AO3 would cut, for pasting into the chapter's HTML
 * editor; the chapter titles stand above it. Opened in a browser, the page shows the work the way the paste will look (inside the
 * {@code workskin} element AO3 wraps works in).
 */
public class Ao3HtmlExporter extends ExporterBase {
    // AO3 turns a line break inside the pasted text into a <br>, so the soft breaks of the paragraphs become spaces and no line break
    // is left between the tags
    private static final HtmlRenderer HTML = HtmlRenderer.builder().extensions(extensions()).escapeHtml(true).sanitizeUrls(true).softbreak(" ").build();
    // what AO3 would cut from the footnotes of the renderer: the ids, classes and data attributes, the section around the notes and the way back
    private static final Pattern NOTE_ATTRIBUTES = Pattern.compile("\\s(?:data-[a-z-]+|aria-label|class|id)(?:=\"[^\"]*\")?(?=[\\s>/])");
    private static final Pattern NOTE_BACKLINK = Pattern.compile("\\s*<a href=\"#fnref-[^\"]*\"[^>]*>[^<]*</a>");

    private static String plainNotes(String html) {
        if (!html.contains("footnote")) {
            return html;
        }
        String cleaned = NOTE_BACKLINK.matcher(html).replaceAll("");
        cleaned = NOTE_ATTRIBUTES.matcher(cleaned).replaceAll("");
        return cleaned.replace("<section>\n", "<hr />\n").replace("</section>\n", "");
    }

    private static final String AFTER_TAG = "(</p>|</h[1-6]>|</blockquote>|</ul>|</ol>|</li>|</pre>|<hr />|<br />|<ul>|<ol>|<blockquote>)\\n";

    private static final String PREVIEW_STYLE = """
            body { margin: 0; background: #ddd; font-family: Georgia, 'Times New Roman', serif; }
            #workskin { max-width: 46em; margin: 0 auto; padding: 1.5em 2.5em; background: #fff; color: #2a2a2a; line-height: 1.5; }
            #workskin h2 { text-align: center; margin: 1.5em 0 0.8em 0; }
            #workskin hr { margin: 1.5em 0; }
            .ao3-chapter { margin-bottom: 2em; }
            """;

    @Override
    public String getName() {
        return "HTML for Archive of Our Own";
    }

    @Override
    public String getId() {
        return "ao3-html";
    }

    @Override
    public String getFileExtension() {
        return "html";
    }

    @Override
    public String getMimeType() {
        return "text/html; charset=UTF-8";
    }

    /**
     * The title page is not written: AO3 asks for the title and the author in fields of its own. The authorship statement is.
     */
    @Override
    public List<ExportTemplate> getTemplates() {
        return List.of(AUTHORSHIP_TEMPLATE);
    }

    @Override
    protected ExportWriter open(ExportOptions options) {
        return new Ao3Writer(options);
    }

    private static class Ao3Writer implements ExportWriter {
        private final ExportOptions options;
        private final List<String> titles = new ArrayList<>();
        private final List<StringBuilder> texts = new ArrayList<>();

        private Ao3Writer(ExportOptions options) {
            this.options = options;
        }

        @Override
        public void titlePage(String markdown) {
        }

        @Override
        public void message(String markdown, Chapter chapter) {
            if (chapter != null || texts.isEmpty()) {
                titles.add(chapter == null ? null : chapter.title());
                texts.add(new StringBuilder());
            }
            Node document = parse(markdown);
            if (chapter != null) {
                // the title goes to the title field of the chapter
                for (Node node = document.getFirstChild(); node != null; node = node.getNext()) {
                    if (node instanceof Heading) {
                        node.unlink();
                        break;
                    }
                }
            }
            texts.getLast().append(plainNotes(HTML.render(document)));
        }

        @Override
        public void images(List<ExportImage> images) {
            // AO3 takes images from links only, and an image of the book is inside the file
            for (ExportImage image : images) {
                String caption = image.caption().isEmpty() ? "" : ": " + escapeXml(image.caption());
                texts.getLast().append("<p><em>[Image").append(caption).append("]</em></p>\n");
            }
        }

        @Override
        public byte[] finish() {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n<html lang=\"").append(escapeXml(StringUtils.defaultIfBlank(options.language(), "en"))).append("\">\n");
            html.append("<head>\n<meta charset=\"utf-8\">\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
            html.append("<title>").append(escapeXml(options.bookName())).append("</title>\n");
            if (StringUtils.isNotBlank(options.author())) {
                html.append("<meta name=\"author\" content=\"").append(escapeXml(options.author())).append("\">\n");
            }
            html.append("<style>\n").append(PREVIEW_STYLE).append("</style>\n</head>\n<body>\n");
            html.append("<!-- Archive of Our Own: paste what is between the BEGIN and END comments of a chapter into the HTML editor of that chapter, ")
                    .append("the title of the chapter goes to its title field. Everything else is only for looking at this file. -->\n");
            html.append("<div id=\"workskin\">\n");
            for (int i = 0; i < texts.size(); i++) {
                String title = titles.get(i);
                html.append("<section class=\"ao3-chapter\">\n");
                if (title != null) {
                    html.append("<h2>").append(escapeXml(title)).append("</h2>\n");
                }
                html.append("<!-- BEGIN chapter ").append(i + 1).append(" -->\n")
                        .append(texts.get(i).toString().replaceAll(AFTER_TAG, "$1").strip()).append('\n')
                        .append("<!-- END chapter ").append(i + 1).append(" -->\n</section>\n");
            }
            html.append("</div>\n</body>\n</html>\n");
            return html.toString().getBytes(StandardCharsets.UTF_8);
        }
    }
}
