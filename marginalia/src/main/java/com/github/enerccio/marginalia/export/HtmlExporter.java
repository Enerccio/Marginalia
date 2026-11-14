package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.styles.PartStyleRegistry;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class HtmlExporter extends ExporterBase {

    /**
     * The page, the title page, the contents and the figures. The look of the text of the book (its font, paragraphs, headings, quotes, scene
     * breaks) is the style of the export ({@code PartStyleRegistry.htmlExportStyle()} when none is chosen), added after this.
     */
    static final String STYLE = """
            body { margin: 0; background: #f4f1ea; }
            main { max-width: 42em; margin: 0 auto; padding: 3em 2em; background: #fffdf8; box-shadow: 0 0 12px rgba(0, 0, 0, 0.12); color: #222; font-family: Georgia, 'Times New Roman', serif; line-height: 1.6; }
            .title-page { min-height: 90vh; display: flex; flex-direction: column; justify-content: center; text-align: center; page-break-after: always; }
            .title-page h1 { font-size: 2.6em; margin: 0 0 0.6em 0; }
            .title-page p { text-align: center; font-size: 1.3em; }
            figure { margin: 1.5em 0; text-align: center; page-break-inside: avoid; }
            figure img { max-width: 100%; height: auto; }
            figcaption { font-size: 0.9em; color: #555; margin-top: 0.4em; }
            .contents { page-break-after: always; }
            .contents h2 { margin-top: 0; }
            .contents ol { list-style: none; padding: 0; }
            .contents li { margin: 0.4em 0; }
            .contents a { color: inherit; text-decoration: none; }
            .contents a:hover { text-decoration: underline; }
            @media print { body { background: none; } main { box-shadow: none; max-width: none; padding: 0; } }
            """ + PICTURES_CSS;

    @Override
    public String getName() {
        return "HTML";
    }

    @Override
    public String getFileExtension() {
        return "html";
    }

    @Override
    public String getMimeType() {
        return "text/html; charset=UTF-8";
    }

    @Override
    public boolean supportsStyles() {
        return true;
    }

    @Override
    public String getDefaultStyleId() {
        return PartStyleRegistry.EXPORT_HTML;
    }

    @Override
    protected ExportWriter open(ExportOptions options, OutputStream out) throws IOException {
        return new HtmlWriter(options, out);
    }

    /**
     * Writes the page as the content comes: the head, the title page and the contents, then the text (in the scope of the style).
     */
    private static class HtmlWriter implements ExportWriter {
        private final Writer out;
        private final PartStyle style;
        private final boolean styled;
        private boolean bodyStarted;

        private HtmlWriter(ExportOptions options, OutputStream out) throws IOException {
            this.out = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            style = options.style() != null ? options.style() : PartStyleRegistry.htmlExportStyle();
            styled = style != null && style.hasContent();
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n<html lang=\"").append(escapeXml(StringUtils.defaultIfBlank(options.language(), "en"))).append("\">\n");
            html.append("<head>\n<meta charset=\"utf-8\">\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
            html.append("<title>").append(escapeXml(options.bookName())).append("</title>\n");
            if (StringUtils.isNotBlank(options.author())) {
                html.append("<meta name=\"author\" content=\"").append(escapeXml(options.author())).append("\">\n");
            }
            html.append("<style>\n").append(STYLE);
            if (styled) {
                // the text of the book (not the title page and the contents) is inside the scope of the style
                html.append(style.toPrintCss(true));
            }
            html.append("</style>\n</head>\n<body>\n<main>\n");
            this.out.write(html.toString());
        }

        @Override
        public void titlePage(String markdown) throws IOException {
            out.write("<section class=\"title-page\">\n" + toHtml(markdown) + "</section>\n");
        }

        @Override
        public void contents(List<Chapter> chapters) throws IOException {
            out.write("<nav class=\"contents\">\n<h2>" + CONTENTS_TITLE + "</h2>\n" + contentsList(chapters, chapter -> "#" + chapter.anchor()) + "</nav>\n");
        }

        @Override
        public void message(String markdown, Chapter chapter) throws IOException {
            message(List.of(new TextPart(markdown)), chapter);
        }

        @Override
        public void message(List<Part> parts, Chapter chapter) throws IOException {
            startBody();
            String html = toHtml(parts, image -> "data:" + image.mimeType() + ";base64," + Base64.getEncoder().encodeToString(image.data()));
            if (chapter == null) {
                out.write(html);
            } else {
                out.write("<section id=\"" + chapter.anchor() + "\">\n" + html + "</section>\n");
            }
        }

        private void startBody() throws IOException {
            if (!bodyStarted) {
                bodyStarted = true;
                if (styled) {
                    out.write("<div class=\"" + style.getCssClass() + "\"><div class=\"chat-message-markdown\">\n");
                }
            }
        }

        @Override
        public void finish() throws IOException {
            startBody();
            if (styled) {
                out.write("</div></div>\n");
            }
            out.write("</main>\n</body>\n</html>\n");
            // the stream is closed by the exporter
            out.flush();
        }
    }
}
