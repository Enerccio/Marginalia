package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The text of a message already is Markdown, so it is written as it is, separated by empty lines. Raw HTML in the
 * text is kept too, the file is source that the reader renders (or not), unlike the formats that show it as text.
 */
public class MarkdownExporter extends ExporterBase {
    private static final Pattern SPECIAL = Pattern.compile("[\\\\`*_{}\\[\\]<>#|~]");

    @Override
    public String getName() {
        return "Markdown";
    }

    @Override
    public String getFileExtension() {
        return "md";
    }

    @Override
    public String getMimeType() {
        return "text/markdown; charset=UTF-8";
    }

    @Override
    protected ExportWriter open(ExportOptions options) {
        return new MarkdownWriter();
    }

    private static class MarkdownWriter implements ExportWriter {
        private final StringBuilder out = new StringBuilder();

        @Override
        public void titlePage(String markdown) {
            // the template leaves an empty line more where the author starts
            block(markdown.replaceAll("\\n{3,}", "\n\n"));
        }

        @Override
        public void message(String markdown, Chapter chapter) {
            block(markdown);
        }

        @Override
        public void images(List<ExportImage> images) {
            for (ExportImage image : images) {
                String caption = image.caption().replaceAll("\\s+", " ");
                String source = "data:" + image.mimeType() + ";base64," + Base64.getEncoder().encodeToString(image.data());
                block("![" + escape(caption) + "](" + source + ")");
                if (!caption.isEmpty()) {
                    block("*" + escape(caption) + "*");
                }
            }
        }

        @Override
        public byte[] finish() {
            String text = out.toString().stripTrailing();
            return (text.isEmpty() ? text : text + "\n").getBytes(StandardCharsets.UTF_8);
        }

        private void block(String markdown) {
            String text = markdown.strip();
            if (!text.isEmpty()) {
                out.append(text).append("\n\n");
            }
        }

        private static String escape(String text) {
            return SPECIAL.matcher(text).replaceAll("\\\\$0");
        }
    }
}
