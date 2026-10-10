package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class DocxExporter extends ExporterBase {
    private static final String FONT = "Times New Roman";
    private static final String CODE_FONT = "Courier New";
    private static final int BODY_SIZE = 12;
    private static final int[] HEADING_SIZES = {24, 20, 17, 15, 13, 12};
    // 1/20 of a point
    private static final int INDENT = 720;

    @Override
    public String getName() {
        return "Word document";
    }

    @Override
    public String getFileExtension() {
        return "docx";
    }

    @Override
    public String getMimeType() {
        return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }

    @Override
    protected ExportWriter open(ExportOptions options) {
        return new DocxWriter(options);
    }

    private static class DocxWriter implements ExportWriter {
        private final XWPFDocument document = new XWPFDocument();
        private final BlockSink bodySink = new DocxSink(false);
        private final BlockSink titleSink = new DocxSink(true);

        private DocxWriter(ExportOptions options) {
            document.getProperties().getCoreProperties().setTitle(options.bookName());
            if (StringUtils.isNotBlank(options.author())) {
                document.getProperties().getCoreProperties().setCreator(options.author());
            }
        }

        @Override
        public void titlePage(String markdown) throws Exception {
            walk(markdown, titleSink);
            document.createParagraph().createRun().addBreak(BreakType.PAGE);
        }

        @Override
        public void message(String markdown) throws Exception {
            walk(markdown, bodySink);
        }

        @Override
        public byte[] finish() throws Exception {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (document) {
                document.write(bytes);
            }
            return bytes.toByteArray();
        }

        private static void addRuns(XWPFParagraph paragraph, List<Run> runs, int size, boolean forceBold) {
            for (Run run : runs) {
                XWPFRun r = paragraph.createRun();
                r.setFontSize(size);
                r.setFontFamily(run.code() ? CODE_FONT : FONT);
                r.setBold(run.bold() || forceBold);
                r.setItalic(run.italic());
                if (run.lineBreak()) {
                    r.addBreak();
                } else {
                    r.setText(run.text());
                }
            }
        }

        private class DocxSink implements BlockSink {
            private final boolean centered;

            private DocxSink(boolean centered) {
                this.centered = centered;
            }

            @Override
            public void heading(int level, List<Run> runs) {
                XWPFParagraph paragraph = document.createParagraph();
                paragraph.setAlignment(centered ? ParagraphAlignment.CENTER : ParagraphAlignment.LEFT);
                paragraph.setKeepNext(true);
                // outline level keeps the headings in the navigation pane of Word
                paragraph.getCTP().addNewPPr().addNewOutlineLvl().setVal(java.math.BigInteger.valueOf(Math.min(level, 9) - 1));
                paragraph.setSpacingBefore(centered ? 4000 : 480);
                paragraph.setSpacingAfter(240);
                addRuns(paragraph, runs, HEADING_SIZES[Math.min(level, HEADING_SIZES.length) - 1], true);
            }

            @Override
            public void paragraph(BlockContext context, List<Run> runs) {
                XWPFParagraph paragraph = document.createParagraph();
                paragraph.setAlignment(centered ? ParagraphAlignment.CENTER
                        : context.isIndented() ? ParagraphAlignment.LEFT : ParagraphAlignment.BOTH);
                paragraph.setSpacingAfter(160);
                indent(paragraph, context);
                if (context.marker() != null) {
                    XWPFRun marker = paragraph.createRun();
                    marker.setFontFamily(FONT);
                    marker.setFontSize(BODY_SIZE);
                    marker.setText(context.marker() + " ");
                }
                addRuns(paragraph, runs, centered ? BODY_SIZE + 3 : BODY_SIZE, false);
            }

            @Override
            public void code(BlockContext context, String code) {
                XWPFParagraph paragraph = document.createParagraph();
                paragraph.setSpacingAfter(160);
                indent(paragraph, context);
                XWPFRun run = paragraph.createRun();
                run.setFontFamily(CODE_FONT);
                run.setFontSize(BODY_SIZE - 2);
                String[] lines = code.stripTrailing().split("\n", -1);
                for (int i = 0; i < lines.length; i++) {
                    run.setText(lines[i]);
                    if (i < lines.length - 1) {
                        run.addBreak();
                    }
                }
            }

            @Override
            public void rule() {
                XWPFParagraph paragraph = document.createParagraph();
                paragraph.setAlignment(ParagraphAlignment.CENTER);
                paragraph.setSpacingBefore(160);
                paragraph.setSpacingAfter(240);
                XWPFRun run = paragraph.createRun();
                run.setFontFamily(FONT);
                run.setFontSize(BODY_SIZE);
                run.setText("*   *   *");
            }

            private void indent(XWPFParagraph paragraph, BlockContext context) {
                int depth = context.listDepth() + context.quoteDepth();
                if (depth > 0) {
                    paragraph.setIndentationLeft(depth * INDENT);
                }
                if (context.quoteDepth() > 0) {
                    paragraph.setBorderLeft(Borders.SINGLE);
                }
            }
        }
    }
}
