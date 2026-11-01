package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.styles.PartStyleRegistry;
import org.apache.commons.lang3.StringUtils;

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
            """;

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
    protected ExportWriter open(ExportOptions options) {
        return new HtmlWriter(options);
    }

    private static class HtmlWriter implements ExportWriter {
        private final ExportOptions options;
        private final StringBuilder body = new StringBuilder();
        private final StringBuilder front = new StringBuilder();

        private HtmlWriter(ExportOptions options) {
            this.options = options;
        }

        @Override
        public void titlePage(String markdown) {
            front.append("<section class=\"title-page\">\n").append(toHtml(markdown)).append("</section>\n");
        }

        @Override
        public void contents(List<Chapter> chapters) {
            front.append("<nav class=\"contents\">\n<h2>").append(CONTENTS_TITLE).append("</h2>\n")
                    .append(contentsList(chapters, chapter -> "#" + chapter.anchor())).append("</nav>\n");
        }

        @Override
        public void message(String markdown, Chapter chapter) {
            if (chapter == null) {
                body.append(toHtml(markdown));
            } else {
                body.append("<section id=\"").append(chapter.anchor()).append("\">\n").append(toHtml(markdown)).append("</section>\n");
            }
        }

        @Override
        public void images(List<ExportImage> images) {
            for (ExportImage image : images) {
                body.append(figure("data:" + image.mimeType() + ";base64," + Base64.getEncoder().encodeToString(image.data()), image.caption()));
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
            PartStyle style = options.style() != null ? options.style() : PartStyleRegistry.htmlExportStyle();
            boolean styled = style != null && style.hasContent();
            html.append("<style>\n").append(STYLE);
            if (styled) {
                // the text of the book (not the title page and the contents) is inside the scope of the style
                html.append(style.toPrintCss(true));
            }
            html.append("</style>\n</head>\n<body>\n<main>\n");
            html.append(front);
            if (styled) {
                html.append("<div class=\"").append(style.getCssClass()).append("\"><div class=\"chat-message-markdown\">\n")
                        .append(body).append("</div></div>\n");
            } else {
                html.append(body);
            }
            html.append("</main>\n</body>\n</html>\n");
            return html.toString().getBytes(StandardCharsets.UTF_8);
        }
    }
}
