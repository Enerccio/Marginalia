package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import com.github.enerccio.marginalia.domain.styles.PartStyleRegistry;
import com.github.enerccio.marginalia.domain.styles.PrintStyle;
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
    // the sizes of the headings as multiples of the body text, 24 pt for the first level of 12 pt text
    private static final float[] HEADING_RATIOS = {2f, 20f / 12, 17f / 12, 15f / 12, 13f / 12, 1f};
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
    public boolean supportsStyles() {
        return true;
    }

    @Override
    public String getDefaultStyleId() {
        return PartStyleRegistry.EXPORT_PDF;
    }

    @Override
    protected ExportWriter open(ExportOptions options) {
        return new PdfWriterAdapter(options);
    }

    /**
     * The four faces of a family.
     */
    private record FontSet(Font regular, Font bold, Font italic, Font boldItalic) {

        static FontSet of(PrintStyle.Family family, int size) throws java.io.IOException {
            return switch (family) {
                case SANS -> new FontSet(Liberation.SANS.create(size), Liberation.SANS_BOLD.create(size),
                        Liberation.SANS_ITALIC.create(size), Liberation.SANS_BOLDITALIC.create(size));
                case MONO -> new FontSet(Liberation.MONO.create(size), Liberation.MONO_BOLD.create(size),
                        Liberation.MONO_ITALIC.create(size), Liberation.MONO_BOLDITALIC.create(size));
                default -> new FontSet(Liberation.SERIF.create(size), Liberation.SERIF_BOLD.create(size),
                        Liberation.SERIF_ITALIC.create(size), Liberation.SERIF_BOLDITALIC.create(size));
            };
        }
    }

    private static int alignment(PrintStyle.Align align) {
        return switch (align) {
            case CENTER -> Element.ALIGN_CENTER;
            case RIGHT -> Element.ALIGN_RIGHT;
            case JUSTIFY -> Element.ALIGN_JUSTIFIED;
            default -> Element.ALIGN_LEFT;
        };
    }

    private static java.awt.Color color(Integer rgb) {
        return rgb == null ? null : new java.awt.Color(rgb);
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
        private final PrintStyle style;
        private final float bodySize;
        private final float[] headingSizes = new float[HEADING_RATIOS.length];
        private final FontSet body, headings;
        private final Font regular, bold, code, small;
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

            style = PrintStyle.of(options.style() != null ? options.style() : PartStyleRegistry.pdfExportStyle());
            bodySize = style.getBodySize();
            for (int i = 0; i < headingSizes.length; i++) {
                // the ratio of a lower level to the first is kept when the style makes the first one larger or smaller
                headingSizes[i] = HEADING_RATIOS[i] * bodySize * (style.getHeadingScale() / HEADING_RATIOS[0]);
            }
            body = FontSet.of(style.getFamily(), Math.round(bodySize));
            headings = FontSet.of(style.getHeadingFamily(), Math.round(bodySize));
            regular = body.regular();
            bold = headings.bold();
            code = Liberation.MONO.create(Math.round(bodySize * 10 / 12));
            small = Liberation.SERIF.create(Math.round(bodySize * 10 / 12));

            writer = PdfWriter.getInstance(document, bytes);
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    if (style.getBackgroundColor() != null) {
                        // under the text, the whole page including the margins, the cover as well
                        PdfContentByte under = writer.getDirectContentUnder();
                        under.saveState();
                        under.setColorFill(new java.awt.Color(style.getBackgroundColor()));
                        Rectangle page = document.getPageSize();
                        under.rectangle(0, 0, page.getWidth(), page.getHeight());
                        under.fill();
                        under.restoreState();
                    }
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
            Paragraph title = new Paragraph(CONTENTS_TITLE, new Font(bold.getBaseFont(), headingSizes[1], bold.getStyle(),
                    color(style.getHeadingColor())));
            title.setSpacingAfter(headingSizes[1]);
            document.add(title);

            PdfOutline root = writer.getRootOutline();
            for (Chapter chapter : chapters) {
                Chunk link = new Chunk(chapter.title() + " ", textFont(regular, bodySize, style.getTextColor()));
                link.setLocalGoto(chapter.anchor());
                Chunk page = new Chunk(" " + String.valueOf(shownPages.getOrDefault(chapter.anchor(), 0)), textFont(regular, bodySize, style.getTextColor()));
                page.setLocalGoto(chapter.anchor());
                Paragraph entry = new Paragraph(bodySize * 1.4f);
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

        private Font textFont(Font base, float size, Integer rgb) {
            return new Font(base.getBaseFont(), size, base.getStyle(), color(rgb));
        }

        /**
         * @param heading   the run is part of a heading: the heading font and color
         * @param quoteRgb  color of the text of a quote, null outside of one
         */
        private Font font(Run run, float size, boolean heading, Integer quoteRgb) {
            if (run.code()) {
                return code;
            }
            FontSet set = heading ? headings : body;
            boolean isBold = run.bold() || heading;
            Font base = isBold ? (run.italic() ? set.boldItalic() : set.bold()) : (run.italic() ? set.italic() : set.regular());
            Integer rgb;
            if (heading) {
                rgb = style.getHeadingColor() != null ? style.getHeadingColor() : style.getTextColor();
            } else if (run.bold() && style.getStrongColor() != null) {
                rgb = style.getStrongColor();
            } else if (run.italic() && style.getEmphasisColor() != null) {
                rgb = style.getEmphasisColor();
            } else {
                rgb = quoteRgb != null ? quoteRgb : style.getTextColor();
            }
            return textFont(base, size, rgb);
        }

        private Paragraph paragraphOf(List<Run> runs, float size, boolean heading, Integer quoteRgb) {
            Paragraph paragraph = new Paragraph();
            paragraph.setLeading(size * style.getLineHeight());
            for (Run run : runs) {
                paragraph.add(run.lineBreak() ? Chunk.NEWLINE : new Chunk(run.text(), font(run, size, heading, quoteRgb)));
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
            // the first paragraph of the text, and the first one after a heading or a scene break, has no indent
            private boolean noIndentNext = true;

            private PdfSink(boolean centered) {
                this.centered = centered;
            }

            @Override
            public void heading(int level, List<Run> runs) {
                float size = headingSizes[Math.min(level, headingSizes.length) - 1];
                Paragraph paragraph = paragraphOf(runs, size, true, null);
                paragraph.setSpacingBefore(centered ? 140 : size);
                paragraph.setSpacingAfter(size / 2);
                paragraph.setAlignment(centered ? Element.ALIGN_CENTER : alignment(style.getHeadingAlign()));
                paragraph.setKeepTogether(true);
                document.add(paragraph);
                noIndentNext = true;
            }

            @Override
            public void paragraph(BlockContext context, List<Run> runs) {
                float size = centered ? bodySize + 3 : bodySize;
                Paragraph paragraph = paragraphOf(runs, size, false, context.quoteDepth() > 0 ? style.getQuoteColor() : null);
                if (context.marker() != null) {
                    paragraph.add(0, new Chunk(context.marker() + " ", textFont(regular, size, style.getTextColor())));
                }
                paragraph.setIndentationLeft((context.listDepth() + context.quoteDepth()) * INDENT);
                paragraph.setSpacingAfter(style.getParagraphSpacing());
                paragraph.setAlignment(centered ? Element.ALIGN_CENTER : context.isIndented() ? Element.ALIGN_LEFT : alignment(style.getAlign()));
                if (!centered && !context.isIndented() && !noIndentNext && style.getFirstLineIndent() > 0) {
                    paragraph.setFirstLineIndent(style.getFirstLineIndent());
                }
                if (!centered) {
                    noIndentNext = false;
                }
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
                Paragraph paragraph = anchored(new Paragraph(style.getSceneBreak(),
                        textFont(regular, bodySize, style.getSceneBreakColor() != null ? style.getSceneBreakColor() : style.getTextColor())));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.setSpacingBefore(8);
                paragraph.setSpacingAfter(12);
                document.add(paragraph);
                noIndentNext = true;
            }
        }
    }
}
