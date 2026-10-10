package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import org.apache.commons.lang3.StringUtils;
import org.librepdf.openpdf.fonts.Liberation;
import org.openpdf.text.*;
import org.openpdf.text.pdf.*;
import org.openpdf.text.pdf.draw.DottedLineSeparator;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    protected ExportWriter open(ExportOptions options) {
        return new PdfWriterAdapter(options);
    }

    /**
     * The table of contents shows the pages of the chapters, which are known only when the text is laid out, so the
     * export is kept and rendered until the pages stay the same.
     */
    private static class PdfWriterAdapter implements ExportWriter {
        private static final int MAX_PASSES = 4;

        private final ExportOptions options;
        private final List<String> messages = new ArrayList<>();
        private final List<Chapter> chapterOf = new ArrayList<>();
        private final List<List<ExportImage>> imagesOf = new ArrayList<>();
        private List<Chapter> chapters = List.of();
        private String titleMarkdown;

        private PdfWriterAdapter(ExportOptions options) {
            this.options = options;
        }

        @Override
        public void titlePage(String markdown) {
            titleMarkdown = markdown;
        }

        @Override
        public void contents(List<Chapter> chapters) {
            this.chapters = chapters;
        }

        @Override
        public void message(String markdown, Chapter chapter) {
            messages.add(markdown);
            chapterOf.add(chapter);
            imagesOf.add(List.of());
        }

        @Override
        public void images(List<ExportImage> images) {
            imagesOf.set(imagesOf.size() - 1, images);
        }

        @Override
        public byte[] finish() throws Exception {
            PdfRendering rendering = new PdfRendering(options, titleMarkdown, chapters, messages, chapterOf, imagesOf, Map.of());
            byte[] result = rendering.render();
            for (int pass = 1; pass < MAX_PASSES && !chapters.isEmpty() && !rendering.chapterPages.equals(rendering.shownPages); pass++) {
                rendering = new PdfRendering(options, titleMarkdown, chapters, messages, chapterOf, imagesOf, rendering.chapterPages);
                result = rendering.render();
            }
            return result;
        }
    }

    private static class PdfRendering {
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private final Document document = new Document(PageSize.A5, 54, 54, 60, 60);
        private final Font regular, bold, italic, boldItalic, code, small;
        private final BlockSink bodySink = new PdfSink(false);
        private final BlockSink titleSink = new PdfSink(true);
        private final PdfWriter writer;
        private final String titleMarkdown;
        private final List<Chapter> chapters;
        private final List<String> messages;
        private final List<Chapter> chapterOf;
        private final List<List<ExportImage>> imagesOf;
        // pages of the chapters (by anchor) as laid out in this rendering, and as the contents of it show them
        private final Map<String, Integer> chapterPages = new HashMap<>();
        private final Map<String, Integer> shownPages;
        // destination of the chapter that is going to be written, given to its first paragraph
        private Chapter pendingChapter;

        private PdfRendering(ExportOptions options, String titleMarkdown, List<Chapter> chapters, List<String> messages,
                             List<Chapter> chapterOf, List<List<ExportImage>> imagesOf, Map<String, Integer> shownPages) throws Exception {
            this.titleMarkdown = titleMarkdown;
            this.chapters = chapters;
            this.messages = messages;
            this.chapterOf = chapterOf;
            this.imagesOf = imagesOf;
            this.shownPages = shownPages;

            regular = Liberation.SERIF.create((int) BODY_SIZE);
            bold = Liberation.SERIF_BOLD.create((int) BODY_SIZE);
            italic = Liberation.SERIF_ITALIC.create((int) BODY_SIZE);
            boldItalic = Liberation.SERIF_BOLDITALIC.create((int) BODY_SIZE);
            code = Liberation.MONO.create(10);
            small = Liberation.SERIF.create(10);

            writer = PdfWriter.getInstance(document, bytes);
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    // the title page is the cover, the numbers are the pages of the file as the readers count them
                    if (titleMarkdown != null && writer.getPageNumber() == 1) {
                        return;
                    }
                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                            new Phrase(String.valueOf(writer.getPageNumber()), small),
                            (document.left() + document.right()) / 2, document.bottom() - 28, 0);
                }

                @Override
                public void onGenericTag(PdfWriter writer, Document document, Rectangle rect, String text) {
                    chapterPages.put(text, writer.getPageNumber());
                }
            });
            document.addTitle(options.bookName());
            if (StringUtils.isNotBlank(options.author())) {
                document.addAuthor(options.author());
            }
            document.addCreator("Marginalia");
            document.open();
        }

        private byte[] render() throws Exception {
            if (titleMarkdown != null) {
                walk(titleMarkdown, titleSink);
                document.newPage();
            }
            if (!chapters.isEmpty()) {
                writeContents();
            }
            for (int i = 0; i < messages.size(); i++) {
                pendingChapter = chapterOf.get(i);
                if (pendingChapter != null) {
                    // a chapter starts on a new page, nothing happens when the page is still empty
                    document.newPage();
                }
                walk(messages.get(i), bodySink);
                pendingChapter = null;
                for (ExportImage image : imagesOf.get(i)) {
                    writeImage(image);
                }
            }
            if (document.getPageNumber() == 0) {
                // nothing was exported, the document needs at least one page
                document.add(new Paragraph(" "));
            }
            document.close();
            return bytes.toByteArray();
        }

        private void writeImage(ExportImage export) throws Exception {
            Image image = Image.getInstance(export.data());
            image.scaleToFit(document.right() - document.left(), (document.top() - document.bottom()) * 0.8f);
            image.setAlignment(Element.ALIGN_CENTER);
            image.setSpacingBefore(8);
            image.setSpacingAfter(export.caption().isEmpty() ? 12 : 4);
            document.add(image);
            if (!export.caption().isEmpty()) {
                Paragraph caption = new Paragraph(export.caption(), small);
                caption.setAlignment(Element.ALIGN_CENTER);
                caption.setSpacingAfter(12);
                document.add(caption);
            }
        }

        private void writeContents() {
            Paragraph title = new Paragraph(CONTENTS_TITLE, new Font(bold.getBaseFont(), HEADING_SIZES[1]));
            title.setSpacingAfter(HEADING_SIZES[1]);
            document.add(title);

            PdfOutline root = writer.getRootOutline();
            for (Chapter chapter : chapters) {
                Chunk link = new Chunk(chapter.title() + " ", regular);
                link.setLocalGoto(chapter.anchor());
                Chunk page = new Chunk(" " + String.valueOf(shownPages.getOrDefault(chapter.anchor(), 0)), regular);
                page.setLocalGoto(chapter.anchor());
                Paragraph entry = new Paragraph(BODY_SIZE * 1.4f);
                entry.add(link);
                entry.add(new Chunk(new DottedLineSeparator()));
                entry.add(page);
                entry.setSpacingAfter(4);
                document.add(entry);
                // the same list is offered by the readers next to the pages
                new PdfOutline(root, PdfAction.gotoLocalPage(chapter.anchor(), false), chapter.title());
            }
            document.newPage();
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
            return anchored(paragraph);
        }

        /**
         * The first paragraph of a chapter is the target of its link in the table of contents.
         */
        private <T extends Paragraph> T anchored(T paragraph) {
            if (pendingChapter != null && !paragraph.getChunks().isEmpty()) {
                Chunk first = (Chunk) paragraph.getChunks().getFirst();
                first.setLocalDestination(pendingChapter.anchor());
                first.setGenericTag(pendingChapter.anchor());
                pendingChapter = null;
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
                Paragraph paragraph = anchored(new Paragraph(text.stripTrailing(), code));
                paragraph.setIndentationLeft((context.listDepth() + context.quoteDepth()) * INDENT);
                paragraph.setSpacingAfter(8);
                document.add(paragraph);
            }

            @Override
            public void rule() {
                Paragraph paragraph = anchored(new Paragraph("*   *   *", regular));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.setSpacingBefore(8);
                paragraph.setSpacingAfter(12);
                document.add(paragraph);
            }
        }
    }
}
