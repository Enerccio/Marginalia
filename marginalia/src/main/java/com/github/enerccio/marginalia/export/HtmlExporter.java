package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

public class HtmlExporter extends ExporterBase {

    static final String STYLE = """
            body { margin: 0; background: #f4f1ea; color: #222; font-family: Georgia, 'Times New Roman', serif; line-height: 1.6; }
            main { max-width: 42em; margin: 0 auto; padding: 3em 2em; background: #fffdf8; box-shadow: 0 0 12px rgba(0, 0, 0, 0.12); }
            p { margin: 0 0 1em 0; text-align: justify; hyphens: auto; }
            h1, h2, h3, h4, h5, h6 { line-height: 1.25; margin: 2em 0 1em 0; }
            blockquote { margin: 1em 0 1em 1.5em; padding-left: 1em; border-left: 3px solid #bbb; color: #444; }
            pre, code { font-family: 'Courier New', monospace; font-size: 0.9em; }
            pre { background: #eee; padding: 0.8em; overflow-x: auto; white-space: pre-wrap; }
            hr { border: 0; text-align: center; margin: 2em 0; }
            hr::after { content: '* * *'; letter-spacing: 0.5em; }
            .title-page { min-height: 90vh; display: flex; flex-direction: column; justify-content: center; text-align: center; page-break-after: always; }
            .title-page h1 { font-size: 2.6em; margin: 0 0 0.6em 0; }
            .title-page p { text-align: center; font-size: 1.3em; }
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
    protected ExportWriter open(ExportOptions options) {
        return new HtmlWriter(options);
    }

    private static class HtmlWriter implements ExportWriter {
        private final ExportOptions options;
        private final StringBuilder body = new StringBuilder();

        private HtmlWriter(ExportOptions options) {
            this.options = options;
        }

        @Override
        public void titlePage(String markdown) {
            body.append("<section class=\"title-page\">\n").append(toHtml(markdown)).append("</section>\n");
        }

        @Override
        public void message(String markdown) {
            body.append(toHtml(markdown));
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
            html.append("<style>\n").append(STYLE).append("</style>\n</head>\n<body>\n<main>\n");
            html.append(body);
            html.append("</main>\n</body>\n</html>\n");
            return html.toString().getBytes(StandardCharsets.UTF_8);
        }
    }
}
