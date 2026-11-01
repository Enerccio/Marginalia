package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.model.ImageAnchor;
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
        private final List<List<Part>> messages = new ArrayList<>();
        private final List<Chapter> chapterOf = new ArrayList<>();
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
            message(List.of(new TextPart(markdown)), chapter);
        }

        @Override
        public void message(List<Part> parts, Chapter chapter) {
            messages.add(parts);
            chapterOf.add(chapter);
        }

        @Override
        public byte[] finish() throws Exception {
            PdfRendering rendering = new PdfRendering(options, titleMarkdown, chapters, messages, chapterOf, Map.of());
            byte[] result = rendering.render();
            for (int pass = 1; pass < MAX_PASSES && !chapters.isEmpty() && !rendering.chapterPages.equals(rendering.shownPages); pass++) {
                rendering = new PdfRendering(options, titleMarkdown, chapters, messages, chapterOf, rendering.chapterPages);
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
        private final List<List<Part>> messages;
        private final List<Chapter> chapterOf;
        // pages of the chapters (by anchor) as laid out in this rendering, and as the contents of it show them
        private final Map<String, Integer> chapterPages = new HashMap<>();
        private final Map<String, Integer> shownPages;
        // destination of the chapter that is going to be written, given to its first paragraph
        private Chapter pendingChapter;

        private PdfRendering(ExportOptions options, String titleMarkdown, List<Chapter> chapters, List<List<Part>> messages,
                             List<Chapter> chapterOf, Map<String, Integer> shownPages) throws Exception {
            this.titleMarkdown = titleMarkdown;
            this.chapters = chapters;
            this.messages = messages;
            this.chapterOf = chapterOf;
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

        // while a band is written, its text is gathered for the cells of its table instead of being added to the document
        private List<Element> collected;

        private void emit(Element element) {
            if (collected != null) {
                collected.add(element);
            } else {
                document.add(element);
            }
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
                for (Part part : messages.get(i)) {
                    switch (part) {
                        case TextPart text -> walk(text.markdown(), bodySink);
                        case PicturePart pictures -> writePictures(pictures);
                        case BandPart band -> writeBand(band);
                    }
                }
                pendingChapter = null;
            }
            if (document.getPageNumber() == 0) {
                // nothing was exported, the document needs at least one page
                document.add(new Paragraph(" "));
            }
            document.close();
            return bytes.toByteArray();
        }

        /**
         * @return the size in points of the image: the width asked for as a part of the text (else the size it has, 96 dpi, and no more than
         * the text), and no higher than most of a page, in the proportions of the image
         */
        private float[] sizeOf(Image image, ExportImage export) {
            float textWidth = document.right() - document.left();
            float maxHeight = (document.top() - document.bottom()) * 0.8f;
            float naturalWidth = image.getWidth() * 0.75f;
            float naturalHeight = image.getHeight() * 0.75f;
            float scale = export.width() == null ? 1 : textWidth * export.width() / 100f / naturalWidth;
            scale = Math.min(scale, Math.min(textWidth / naturalWidth, maxHeight / naturalHeight));
            return new float[]{naturalWidth * scale, naturalHeight * scale};
        }

        private static int horizontal(ImageAnchor anchor) {
            return switch (anchor.horizontal()) {
                case LEFT -> Element.ALIGN_LEFT;
                case CENTER -> Element.ALIGN_CENTER;
                case RIGHT -> Element.ALIGN_RIGHT;
            };
        }

        private Paragraph captionOf(ExportImage export, int alignment) {
            Paragraph caption = new Paragraph(export.caption(), small);
            caption.setAlignment(alignment);
            caption.setLeading(small.getSize() * 1.25f);
            return caption;
        }

        private void writePictures(PicturePart pictures) throws Exception {
            if (pictures.wrap()) {
                for (ExportImage export : pictures.images()) {
                    writeWrapped(export, pictures.anchor());
                }
            } else if (isRow(pictures)) {
                writeRow(pictures);
            } else {
                for (ExportImage export : pictures.images()) {
                    writeBlock(export, pictures.anchor());
                }
            }
        }

        /**
         * A table of three columns without borders: the text that is read first, the images, the text that follows. The text flows down
         * the left column and goes on in the right one.
         */
        private void writeBand(BandPart band) throws Exception {
            int widest = band.images().stream().mapToInt(image -> image.width() == null ? 30 : image.width()).max().orElse(30);
            float side = (100 - widest) / 2f;
            PdfPTable table = new PdfPTable(new float[]{side, widest, side});
            table.setWidthPercentage(100);
            table.setSpacingBefore(6);
            table.setSpacingAfter(10);

            table.addCell(textCell(band.left()));
            PdfPCell middle = new PdfPCell();
            middle.setBorder(Rectangle.NO_BORDER);
            middle.setPaddingLeft(6);
            middle.setPaddingRight(6);
            middle.setVerticalAlignment(Element.ALIGN_MIDDLE);
            float textWidth = document.right() - document.left();
            for (ExportImage export : band.images()) {
                Image image = Image.getInstance(export.data());
                float[] size = sizeOf(image, export);
                // a part of the column of the widest one, less the padding of the cell
                float column = textWidth * widest / 100f - 12;
                float width = Math.min(size[0], column) * (export.width() == null ? 30 : export.width()) / widest;
                width = Math.min(column, Math.max(width, 1));
                image.scaleAbsolute(width, size[1] * width / size[0]);
                image.setAlignment(Element.ALIGN_CENTER);
                middle.addElement(image);
                if (!export.caption().isEmpty()) {
                    Paragraph caption = captionOf(export, Element.ALIGN_CENTER);
                    caption.setSpacingAfter(4);
                    middle.addElement(caption);
                }
            }
            table.addCell(middle);
            table.addCell(textCell(band.right()));
            document.add(table);
        }

        private PdfPCell textCell(String markdown) throws Exception {
            PdfPCell cell = new PdfPCell();
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPaddingLeft(0);
            cell.setPaddingRight(8);
            List<Element> elements = new ArrayList<>();
            collected = elements;
            try {
                walk(markdown, bodySink);
            } finally {
                collected = null;
            }
            for (Element element : elements) {
                // the columns are narrow, justified lines would have wide gaps
                if (element instanceof Paragraph paragraph && paragraph.getAlignment() == Element.ALIGN_JUSTIFIED) {
                    paragraph.setAlignment(Element.ALIGN_LEFT);
                }
                cell.addElement(element);
            }
            return cell;
        }

        private void writeBlock(ExportImage export, ImageAnchor anchor) throws Exception {
            Image image = Image.getInstance(export.data());
            float[] size = sizeOf(image, export);
            image.scaleAbsolute(size[0], size[1]);
            image.setAlignment(horizontal(anchor));
            image.setSpacingBefore(8);
            image.setSpacingAfter(export.caption().isEmpty() ? 12 : 4);
            document.add(image);
            if (!export.caption().isEmpty()) {
                Paragraph caption = captionOf(export, horizontal(anchor));
                caption.setSpacingAfter(12);
                document.add(caption);
            }
        }

        /**
         * Images that were given a width and fit next to each other are a row.
         */
        private static boolean isRow(PicturePart pictures) {
            int sum = 0;
            for (ExportImage export : pictures.images()) {
                if (export.width() == null) {
                    return false;
                }
                sum += export.width();
            }
            return pictures.images().size() > 1 && sum <= 100;
        }

        private void writeRow(PicturePart pictures) throws Exception {
            float[] widths = new float[pictures.images().size()];
            int sum = 0;
            for (int i = 0; i < widths.length; i++) {
                widths[i] = pictures.images().get(i).width();
                sum += pictures.images().get(i).width();
            }
            PdfPTable table = new PdfPTable(widths);
            table.setWidthPercentage(sum);
            table.setHorizontalAlignment(horizontal(pictures.anchor()));
            table.setSpacingBefore(8);
            table.setSpacingAfter(12);
            for (ExportImage export : pictures.images()) {
                PdfPCell cell = new PdfPCell();
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPadding(3);
                Image image = Image.getInstance(export.data());
                float[] size = sizeOf(image, export);
                image.scaleAbsolute(size[0] - 6, size[1] - 6 * size[1] / size[0]);
                image.setAlignment(Element.ALIGN_CENTER);
                cell.addElement(image);
                if (!export.caption().isEmpty()) {
                    cell.addElement(captionOf(export, Element.ALIGN_CENTER));
                }
                table.addCell(cell);
            }
            document.add(table);
        }

        /**
         * The text that follows flows around the image: it is an image of OpenPDF that wraps text, and the caption is drawn under it in the
         * same template, as the wrapping is only for images.
         */
        private void writeWrapped(ExportImage export, ImageAnchor anchor) throws Exception {
            Image image = Image.getInstance(export.data());
            float[] size = sizeOf(image, export);
            float gap = 10;
            Image placed;
            if (export.caption().isEmpty()) {
                image.scaleAbsolute(size[0], size[1]);
                placed = image;
            } else {
                Paragraph caption = captionOf(export, Element.ALIGN_CENTER);
                ColumnText measure = new ColumnText(null);
                measure.setSimpleColumn(0, -10000, size[0], 0);
                measure.addElement(caption);
                measure.go(true);
                float captionHeight = -measure.getYLine() + 2;

                PdfTemplate template = writer.getDirectContent().createTemplate(size[0], size[1] + 4 + captionHeight);
                image.scaleAbsolute(size[0], size[1]);
                image.setAbsolutePosition(0, captionHeight + 4);
                template.addImage(image);
                ColumnText text = new ColumnText(template);
                text.setSimpleColumn(0, 0, size[0], captionHeight);
                text.addElement(captionOf(export, Element.ALIGN_CENTER));
                text.go();
                placed = Image.getInstance(template);
            }
            boolean left = anchor.horizontal() == ImageAnchor.Horizontal.LEFT;
            placed.setAlignment((left ? Image.LEFT : Image.RIGHT) | Image.TEXTWRAP);
            if (left) {
                placed.setIndentationRight(gap);
            } else {
                placed.setIndentationLeft(gap);
            }
            placed.setSpacingAfter(gap);
            document.add(placed);
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
                emit(paragraph);
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
                emit(paragraph);
            }

            @Override
            public void code(BlockContext context, String text) {
                Paragraph paragraph = anchored(new Paragraph(text.stripTrailing(), code));
                paragraph.setIndentationLeft((context.listDepth() + context.quoteDepth()) * INDENT);
                paragraph.setSpacingAfter(8);
                emit(paragraph);
            }

            @Override
            public void rule() {
                Paragraph paragraph = anchored(new Paragraph(style.getSceneBreak(),
                        textFont(regular, bodySize, style.getSceneBreakColor() != null ? style.getSceneBreakColor() : style.getTextColor())));
                paragraph.setAlignment(Element.ALIGN_CENTER);
                paragraph.setSpacingBefore(8);
                paragraph.setSpacingAfter(12);
                emit(paragraph);
                noIndentNext = true;
            }
        }
    }
}
