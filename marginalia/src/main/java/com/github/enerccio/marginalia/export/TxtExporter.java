package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class TxtExporter extends ExporterBase {

    @Override
    public String getName() {
        return "Plain text";
    }

    @Override
    public String getFileExtension() {
        return "txt";
    }

    @Override
    public String getMimeType() {
        return "text/plain; charset=UTF-8";
    }

    @Override
    protected ExportWriter open(ExportOptions options) {
        return new TxtWriter();
    }

    private static class TxtWriter implements ExportWriter, BlockSink {
        private final StringBuilder out = new StringBuilder();

        @Override
        public void titlePage(String markdown) throws Exception {
            walk(markdown, this);
            out.append("\n\n");
        }

        @Override
        public void message(String markdown, Chapter chapter) throws Exception {
            walk(markdown, this);
        }

        @Override
        public byte[] finish() {
            return out.toString().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public void heading(int level, List<Run> runs) {
            out.append(text(runs)).append("\n\n");
        }

        @Override
        public void paragraph(BlockContext context, List<Run> runs) {
            out.append(prefix(context)).append(text(runs)).append("\n\n");
        }

        @Override
        public void code(BlockContext context, String code) {
            String prefix = " ".repeat(context.listDepth() * 2) + "> ".repeat(context.quoteDepth());
            for (String line : code.stripTrailing().split("\n", -1)) {
                out.append(prefix).append("    ").append(line).append('\n');
            }
            out.append('\n');
        }

        @Override
        public void rule() {
            out.append("* * *\n\n");
        }

        private static String prefix(BlockContext context) {
            StringBuilder prefix = new StringBuilder("> ".repeat(context.quoteDepth()));
            if (context.listDepth() > 0) {
                prefix.append("  ".repeat(context.listDepth() - 1));
                prefix.append(context.marker() == null ? "   " : context.marker() + " ");
            }
            return prefix.toString();
        }

        private static String text(List<Run> runs) {
            StringBuilder text = new StringBuilder();
            runs.forEach(run -> text.append(run.text()));
            return text.toString();
        }
    }
}
