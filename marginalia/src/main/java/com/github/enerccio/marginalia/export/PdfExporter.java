package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import org.apache.commons.lang3.StringUtils;
import org.librepdf.openpdf.fonts.Liberation;
import org.openpdf.text.*;
import org.openpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * PDF with the embedded Liberation fonts, so the text is not limited to Latin-1.
 */
public class PdfExporter extends ExporterBase {
    private static final float BODY_SIZE = 12;
    private static final float[] HEADING_SIZES = {24, 20, 17, 15, 13, 12};
    private static final float INDENT = 20;

    @Override
    public String getName() {
        return "PDF";
    }

    @Override
    public String getFileExtension() {
        return "pdf";
    }

    @Override
    public String getMimeType() {
        return "application/pdf";
    }

    @Override
    protected ExportWriter open(ExportOptions options) throws Exception {
        return new PdfWriterAdapter(options);
    }

    private static class PdfWriterAdapter implements ExportWriter {
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private final Document document = new Document(PageSize.A5, 54, 54, 60, 60);
        private final Font regular, bold, italic, boldItalic, code;
        private final BlockSink bodySink = new PdfSink(false);
        private final BlockSink titleSink = new PdfSink(true);

        private PdfWriterAdapter(ExportOptions options) throws Exception {
            regular = Liberation.SERIF.create((int) BODY_SIZE);
            bold = Liberation.SERIF_BOLD.create((int) BODY_SIZE);
            italic = Liberation.SERIF_ITALIC.create((int) BODY_SIZE);
            boldItalic = Liberation.SERIF_BOLDITALIC.create((int) BODY_SIZE);
            code = Liberation.MONO.create(10);

            PdfWriter.getInstance(document, bytes);
            document.addTitle(options.bookName());
            if (StringUtils.isNotBlank(options.author())) {
                document.addAuthor(options.author());
            }
            document.addCreator("Marginalia");
            document.open();
        }

        @Override
        public void titlePage(String markdown) throws Exception {
            walk(markdown, titleSink);
            document.newPage();
        }

        @Override
        public void message(String markdown) throws Exception {
            walk(markdown, bodySink);
        }

        @Override
        public byte[] finish() {
            if (document.getPageNumber() == 0) {
                // nothing was exported, the document needs at least one page
                document.add(new Paragraph(" "));
            }
            document.close();
            return bytes.toByteArray();
        }

        private Font font(Run run, float size, boolean forceBold) {
            if (run.code()) {
                return code;
            }
            boolean isBold = run.bold() || forceBold;
            Font base = isBold ? (run.italic() ? boldItalic : bold) : (run.italic() ? italic : regular);
            return size == BODY_SIZE ? base : new Font(base.getBaseFont(), size, base.getStyle(), base.getColor());
        }

        private Paragraph paragraphOf(List<Run> runs, float size, boolean forceBold) {
            Paragraph paragraph = new Paragraph();
            paragraph.setLeading(size * 1.4f);
            for (Run run : runs) {
                paragraph.add(run.lineBreak() ? Chunk.NEWLINE : new Chunk(run.text(), font(run, size, forceBold)));
            }
            return paragraph;
        }

        private class PdfSink implements BlockSink {
            private final boolean centered;

            private PdfSink(boolean centered) {
                this.centered = centered;
            }

            @Override
            public void heading(int level, List<Run> runs) {
                float size = HEADING_SIZES[Math.min(level, HEADING_SIZES.length) - 1];
                Paragraph paragraph = paragraphOf(runs, size, true);
                paragraph.setSpacingBefore(centered ? 140 : size);
                paragraph.setSpacingAfter(size / 2);
                paragraph.setAlignment(centered ? Element.ALIGN_CENTER : Element.ALIGN_LEFT);
                paragraph.setKeepTogether(true);
                document.add(paragraph);
            }

            @Override
            public void paragraph(BlockContext context, List<Run> runs) {
                float size = centered ? BODY_SIZE + 3 : BODY_SIZE;
                Paragraph paragraph = paragraphOf(runs, size, false);
                if (context.marker() != null) {
                    paragraph.add(0, new Chunk(context.marker() + " ", regular));
                }
                paragraph.setIndentationLeft((context.listDepth() + context.quoteDepth()) * INDENT);
                paragraph.setSpacingAfter(8);
                paragraph.setAlignment(centered ? Element.ALIGN_CENTER : context.isIndented() ? Element.ALIGN_LEFT : Element.ALIGN_JUSTIFIED);
                document.add(paragraph);
            }

            @Override
            public void code(BlockContext context, String text) {
                Paragraph paragraph = new Paragraph(text.stripTrailing(), code);
                paragraph.setIndentationLeft((context.listDepth() + context.quoteDepth()) * INDENT);
                paragraph.setSpacingAfter(8);
                document.add(paragraph);
            }

            @Override
            public void rule() {
                Paragraph paragraph = new Paragraph("*   *   *", regular);
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.setSpacingBefore(8);
                paragraph.setSpacingAfter(12);
                document.add(paragraph);
            }
        }
    }
}
