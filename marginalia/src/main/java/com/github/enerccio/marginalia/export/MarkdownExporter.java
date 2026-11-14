package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
    protected ExportWriter open(ExportOptions options, OutputStream out) throws Exception {
        return new MarkdownWriter(out, null);
    }

    /**
     * Writes the blocks into the file as they come, an empty line between two of them.
     */
    protected static class MarkdownWriter implements ExportWriter {
        private final Writer out;
        private boolean written;

        /**
         * @param start text the file starts with as it is (the front matter), null for none
         */
        protected MarkdownWriter(OutputStream out, String start) throws IOException {
            this.out = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            if (start != null) {
                this.out.write(start);
                written = true;
            }
        }

        @Override
        public void titlePage(String markdown) throws IOException {
            // the template leaves an empty line more where the author starts
            block(markdown.replaceAll("\\n{3,}", "\n\n"));
        }

        @Override
        public void message(String markdown, Chapter chapter) throws IOException {
            block(markdown);
        }

        @Override
        public void images(List<ExportImage> images) throws IOException {
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
        public void finish() throws IOException {
            if (written) {
                out.write('\n');
            }
            // the stream is closed by the exporter
            out.flush();
        }

        private void block(String markdown) throws IOException {
            String text = markdown.strip();
            if (!text.isEmpty()) {
                if (written) {
                    out.write("\n\n");
                }
                out.write(text);
                written = true;
            }
        }

        private static String escape(String text) {
            return SPECIAL.matcher(text).replaceAll("\\\\$0");
        }
    }
}
