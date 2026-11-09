package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.model.ImageAnchor;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import com.github.enerccio.marginalia.domain.styles.PartStyleRegistry;
import com.github.enerccio.marginalia.domain.styles.PrintStyle;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STXAlign;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocxExporter extends ExporterBase {
    private static final String CODE_FONT = "Courier New";
    // the sizes of the headings as multiples of the body text, 24 pt for the first level of 12 pt text
    private static final float[] HEADING_RATIOS = {2f, 20f / 12, 17f / 12, 15f / 12, 13f / 12, 1f};
    // 1/20 of a point
    private static final int INDENT = 720;
    // position of the page numbers in the table of contents, it is within the text width of A4 and Letter
    private static final int CONTENTS_TAB = 9000;
    private static final int EMU_PER_PIXEL = 9525;
    // 5.5 inch, within the text width of A4 and Letter, and 8 inch
    private static final long MAX_IMAGE_WIDTH = 5_029_200L;
    private static final long MAX_IMAGE_HEIGHT = 7_315_200L;
    // the same width in 1/20 of a point (twips): the 100 % of the width of an image
    private static final int TEXT_WIDTH_DXA = (int) (MAX_IMAGE_WIDTH / 635);

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
    public boolean supportsStyles() {
        return true;
    }

    @Override
    public String getDefaultStyleId() {
        return PartStyleRegistry.EXPORT_DOCX;
    }

    @Override
    protected ExportWriter open(ExportOptions options) {
        return new DocxWriter(options);
    }

    /**
     * The name of a font Word has for the family, when the style names none.
     */
    private static String fontFor(PrintStyle.Family family, String named) {
        if (named != null) {
            // Word takes any name, and shows its own font when the computer has none of that name
            return named;
        }
        return switch (family) {
            case SANS -> "Arial";
            case MONO -> CODE_FONT;
            default -> "Times New Roman";
        };
    }

    private static ParagraphAlignment alignment(PrintStyle.Align align) {
        return switch (align) {
            case CENTER -> ParagraphAlignment.CENTER;
            case RIGHT -> ParagraphAlignment.RIGHT;
            case JUSTIFY -> ParagraphAlignment.BOTH;
            default -> ParagraphAlignment.LEFT;
        };
    }

    private static class DocxWriter implements ExportWriter {
        private final PrintStyle print;
        private final int bodySize;
        private final int[] headingSizes = new int[HEADING_RATIOS.length];
        private final String font;
        private final String headingFont;
        private final XWPFDocument document = new XWPFDocument();
        private final BlockSink bodySink = new DocxSink(false);
        private final BlockSink titleSink = new DocxSink(true);
        // bookmark of the chapter that is going to be written, put on its first paragraph
        private Chapter pendingChapter;
        // the next paragraph starts a page, the title page and the contents are pages of their own
        private boolean pageBreakPending;
        private int bookmarks;
        // the notes of the text that is being written (label, paragraphs), and whether a note is being written (its own marks are plain text)
        private final Map<String, List<List<Run>>> partNotes = new HashMap<>();
        private boolean inNote;

        private DocxWriter(ExportOptions options) {
            print = PrintStyle.of(options.style() != null ? options.style() : PartStyleRegistry.docxExportStyle());
            bodySize = Math.round(print.getBodySize());
            for (int i = 0; i < headingSizes.length; i++) {
                headingSizes[i] = Math.round(HEADING_RATIOS[i] * print.getBodySize() * (print.getHeadingScale() / HEADING_RATIOS[0]));
            }
            font = fontFor(print.getFamily(), print.getFontName());
            headingFont = fontFor(print.getHeadingFamily(), print.getHeadingFontName());
            document.getProperties().getCoreProperties().setTitle(options.bookName());
            if (StringUtils.isNotBlank(options.author())) {
                document.getProperties().getCoreProperties().setCreator(options.author());
            }
            addPageNumbers();
            if (print.getBackgroundColor() != null) {
                // the color of the page; Word shows it on the screen (print layout) only when the document says so
                document.getDocument().addNewBackground().setColor(PrintStyle.hex(print.getBackgroundColor()));
                document.getSettings().getCTSettings().addNewDisplayBackgroundShape();
            }
        }

        private void addPageNumbers() {
            XWPFFooter footer = new XWPFHeaderFooterPolicy(document).createFooter(STHdrFtr.DEFAULT);
            XWPFParagraph paragraph = footer.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            CTSimpleField field = paragraph.getCTP().addNewFldSimple();
            field.setInstr(" PAGE ");
            CTR run = field.addNewR();
            format(run, bodySize - 2);
            run.addNewT().setStringValue("1");
        }

        private void format(CTR run, int size) {
            CTRPr properties = run.addNewRPr();
            CTFonts fonts = properties.addNewRFonts();
            fonts.setAscii(font);
            fonts.setHAnsi(font);
            properties.addNewSz().setVal(BigInteger.valueOf(size * 2L));
        }

        @Override
        public void titlePage(String markdown) throws Exception {
            walk(markdown, titleSink);
            pageBreakPending = true;
            // the title page is the cover, it has no footer
            document.getDocument().getBody().getSectPr().addNewTitlePg();
        }

        /**
         * The table of contents is a list of links to the chapter bookmarks with the page numbers as PAGEREF fields. It
         * is not the Word TOC field, which is empty until Word updates it and is never filled by LibreOffice.
         * The fields have no result until they are updated, which Word does after asking when the file is opened.
         */
        @Override
        public void contents(List<Chapter> chapters) {
            XWPFParagraph title = newParagraph();
            title.setSpacingAfter(240);
            addRuns(title, List.of(new Run(CONTENTS_TITLE, true, false, false, false)), headingSizes[1], true, null);

            for (Chapter chapter : chapters) {
                XWPFParagraph entry = document.createParagraph();
                entry.setSpacingAfter(80);
                CTTabStop tab = entry.getCTPPr().addNewTabs().addNewTab();
                tab.setVal(STTabJc.RIGHT);
                tab.setLeader(STTabTlc.DOT);
                tab.setPos(BigInteger.valueOf(CONTENTS_TAB));

                CTHyperlink link = entry.getCTP().addNewHyperlink();
                link.setAnchor(chapter.anchor());
                link.setHistory(true);
                CTR run = link.addNewR();
                format(run, bodySize);
                run.addNewT().setStringValue(chapter.title());

                CTR separator = entry.getCTP().addNewR();
                format(separator, bodySize);
                separator.addNewTab();
                field(entry, STFldCharType.BEGIN, null);
                field(entry, null, " PAGEREF " + chapter.anchor() + " \\h ");
                field(entry, STFldCharType.SEPARATE, null);
                field(entry, STFldCharType.END, null);
            }
            pageBreakPending = true;
            document.getSettings().setUpdateFields();
        }

        private void field(XWPFParagraph paragraph, STFldCharType.Enum type, String instruction) {
            CTR run = paragraph.getCTP().addNewR();
            format(run, bodySize);
            if (type != null) {
                run.addNewFldChar().setFldCharType(type);
            } else {
                run.addNewInstrText().setStringValue(instruction);
            }
        }

        @Override
        public void message(String markdown, Chapter chapter) throws Exception {
            message(List.of(new TextPart(markdown)), chapter);
        }

        @Override
        public void message(List<Part> parts, Chapter chapter) throws Exception {
            pendingChapter = chapter;
            // a chapter starts on a new page
            pageBreakPending |= chapter != null;
            for (Part part : parts) {
                switch (part) {
                    case TextPart text -> walk(text.markdown(), bodySink);
                    case PicturePart pictures -> writePictures(pictures);
                    case BandPart band -> {
                        // Word flows the text around the table on both sides of it, line by line
                        writeFloating(band.images(), ImageAnchor.CENTER_CENTER);
                        walk(band.left(), bodySink);
                        walk(band.right(), bodySink);
                    }
                }
            }
            pendingChapter = null;
            if (!parts.isEmpty() && (parts.getLast() instanceof PicturePart last && last.wrap() || parts.getLast() instanceof BandPart)) {
                // a table the text flows around needs a paragraph after it, also when there is no text
                document.createParagraph().setSpacingAfter(0);
            }
        }

        /**
         * @return the size in EMU of the image: the width asked for as a part of the text (else the size it has, 96 dpi, and no more than
         * the text), and no higher than most of a page, in the proportions of the image
         */
        private long[] sizeOf(BufferedImage decoded, ExportImage export) {
            double naturalWidth = decoded.getWidth() * (double) EMU_PER_PIXEL;
            double naturalHeight = decoded.getHeight() * (double) EMU_PER_PIXEL;
            double scale = export.width() == null ? 1 : MAX_IMAGE_WIDTH * export.width() / 100.0 / naturalWidth;
            scale = Math.min(scale, Math.min(MAX_IMAGE_WIDTH / naturalWidth, MAX_IMAGE_HEIGHT / naturalHeight));
            return new long[]{(long) (naturalWidth * scale), (long) (naturalHeight * scale)};
        }

        private static ParagraphAlignment horizontal(ImageAnchor anchor) {
            return switch (anchor.horizontal()) {
                case LEFT -> ParagraphAlignment.LEFT;
                case CENTER -> ParagraphAlignment.CENTER;
                case RIGHT -> ParagraphAlignment.RIGHT;
            };
        }

        private void picture(XWPFParagraph paragraph, ExportImage image, long[] size) throws Exception {
            paragraph.createRun().addPicture(new ByteArrayInputStream(image.data()),
                    image.mimeType().equals("image/jpeg") ? XWPFDocument.PICTURE_TYPE_JPEG
                            : image.mimeType().equals("image/gif") ? XWPFDocument.PICTURE_TYPE_GIF : XWPFDocument.PICTURE_TYPE_PNG,
                    "image." + image.extension(), (int) size[0], (int) size[1]);
        }

        private void writePictures(PicturePart pictures) throws Exception {
            if (pictures.wrap()) {
                writeFloating(pictures.images(), pictures.anchor());
            } else if (isRow(pictures)) {
                writeRow(pictures);
            } else {
                for (ExportImage image : pictures.images()) {
                    BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(image.data()));
                    XWPFParagraph paragraph = newParagraph();
                    paragraph.setAlignment(horizontal(pictures.anchor()));
                    paragraph.setKeepNext(!image.caption().isEmpty());
                    picture(paragraph, image, sizeOf(decoded, image));
                    if (!image.caption().isEmpty()) {
                        XWPFParagraph caption = newParagraph();
                        caption.setAlignment(horizontal(pictures.anchor()));
                        addRuns(caption, List.of(new Run(image.caption(), false, true, false, false)), bodySize - 2, false, null);
                    }
                }
            }
        }

        /**
         * Images that were given a width and fit next to each other are a row.
         */
        private static boolean isRow(PicturePart pictures) {
            int sum = 0;
            for (ExportImage image : pictures.images()) {
                if (image.width() == null) {
                    return false;
                }
                sum += image.width();
            }
            return pictures.images().size() > 1 && sum <= 100;
        }

        private static void noBorders(XWPFTable table) {
            table.setTopBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "auto");
            table.setBottomBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "auto");
            table.setLeftBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "auto");
            table.setRightBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "auto");
            table.setInsideHBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "auto");
            table.setInsideVBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "auto");
        }

        /**
         * A table of one row without borders, the images next to each other with their captions under them.
         */
        private void writeRow(PicturePart pictures) throws Exception {
            // the paragraph that carries the page break and the bookmark of a chapter that starts with the images
            if (pageBreakPending || pendingChapter != null) {
                newParagraph().setSpacingAfter(0);
            }
            List<ExportImage> images = pictures.images();
            XWPFTable table = document.createTable(1, images.size());
            noBorders(table);
            table.setTableAlignment(switch (pictures.anchor().horizontal()) {
                case LEFT -> TableRowAlign.LEFT;
                case CENTER -> TableRowAlign.CENTER;
                case RIGHT -> TableRowAlign.RIGHT;
            });
            int sum = images.stream().mapToInt(ExportImage::width).sum();
            table.setWidth(TEXT_WIDTH_DXA * sum / 100);
            for (int i = 0; i < images.size(); i++) {
                ExportImage image = images.get(i);
                XWPFTableCell cell = table.getRow(0).getCell(i);
                cell.setWidth(String.valueOf(TEXT_WIDTH_DXA * image.width() / 100));
                BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(image.data()));
                long[] size = sizeOf(decoded, image);
                // a little less than the cell, which has margins
                size[1] = size[1] * 95 / 100;
                size[0] = size[0] * 95 / 100;
                XWPFParagraph paragraph = cell.getParagraphs().getFirst();
                paragraph.setAlignment(ParagraphAlignment.CENTER);
                paragraph.setKeepNext(!image.caption().isEmpty());
                picture(paragraph, image, size);
                if (!image.caption().isEmpty()) {
                    XWPFParagraph caption = cell.addParagraph();
                    caption.setAlignment(ParagraphAlignment.CENTER);
                    addRuns(caption, List.of(new Run(image.caption(), false, true, false, false)), bodySize - 2, false, null);
                }
            }
            document.createParagraph().setSpacingAfter(0);
        }

        /**
         * A table of one cell that floats at the left, at the right or in the middle, so that the text after it flows around it (Word
         * does that for a table with a position, and it can hold the captions under the images). The images are one above the other.
         */
        private void writeFloating(List<ExportImage> images, ImageAnchor anchor) throws Exception {
            if (pageBreakPending || pendingChapter != null) {
                newParagraph().setSpacingAfter(0);
            }
            List<long[]> sizes = new ArrayList<>();
            long widest = 0;
            for (ExportImage image : images) {
                long[] size = sizeOf(ImageIO.read(new ByteArrayInputStream(image.data())), image);
                sizes.add(size);
                widest = Math.max(widest, size[0]);
            }
            XWPFTable table = document.createTable(1, 1);
            noBorders(table);
            int widthDxa = (int) (widest / 635) + 120;
            table.setWidth(widthDxa);
            CTTblPPr position = table.getCTTbl().getTblPr().addNewTblpPr();
            position.setHorzAnchor(STHAnchor.MARGIN);
            position.setVertAnchor(STVAnchor.TEXT);
            position.setTblpXSpec(switch (anchor.horizontal()) {
                case LEFT -> STXAlign.LEFT;
                case CENTER -> STXAlign.CENTER;
                case RIGHT -> STXAlign.RIGHT;
            });
            position.setTblpY(BigInteger.ZERO);
            position.setLeftFromText(BigInteger.valueOf(anchor.horizontal() == ImageAnchor.Horizontal.LEFT ? 0 : 200));
            position.setRightFromText(BigInteger.valueOf(anchor.horizontal() == ImageAnchor.Horizontal.RIGHT ? 0 : 200));
            position.setBottomFromText(BigInteger.valueOf(120));
            XWPFTableCell cell = table.getRow(0).getCell(0);
            cell.setWidth(String.valueOf(widthDxa));
            boolean first = true;
            for (int i = 0; i < images.size(); i++) {
                ExportImage image = images.get(i);
                XWPFParagraph paragraph = first ? cell.getParagraphs().getFirst() : cell.addParagraph();
                first = false;
                paragraph.setAlignment(ParagraphAlignment.CENTER);
                paragraph.setKeepNext(!image.caption().isEmpty());
                picture(paragraph, image, sizes.get(i));
                if (!image.caption().isEmpty()) {
                    XWPFParagraph caption = cell.addParagraph();
                    caption.setAlignment(ParagraphAlignment.CENTER);
                    addRuns(caption, List.of(new Run(image.caption(), false, true, false, false)), bodySize - 2, false, null);
                }
            }
        }

        /**
         * The first paragraph of a chapter is the target of its link in the table of contents.
         */
        private XWPFParagraph newParagraph() {
            XWPFParagraph paragraph = document.createParagraph();
            if (pageBreakPending) {
                paragraph.setPageBreak(true);
                pageBreakPending = false;
            }
            if (pendingChapter != null) {
                BigInteger id = BigInteger.valueOf(bookmarks++);
                CTBookmark start = paragraph.getCTP().addNewBookmarkStart();
                start.setId(id);
                start.setName(pendingChapter.anchor());
                paragraph.getCTP().addNewBookmarkEnd().setId(id);
                pendingChapter = null;
            }
            return paragraph;
        }

        @Override
        public byte[] finish() throws Exception {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (document) {
                document.write(bytes);
            }
            return bytes.toByteArray();
        }

        /**
         * @param heading  the text is a heading: bold, in the font and color of the headings
         * @param quoteRgb color of the text of a quote, null outside of one
         */
        private void addRuns(XWPFParagraph paragraph, List<Run> runs, int size, boolean heading, Integer quoteRgb) {
            for (Run run : runs) {
                if (run.note() != null && !heading && !inNote && partNotes.containsKey(run.note())) {
                    footnote(paragraph, partNotes.get(run.note()), size);
                    continue;
                }
                XWPFRun r = paragraph.createRun();
                r.setFontSize(size);
                r.setFontFamily(run.code() ? CODE_FONT : heading ? headingFont : font);
                r.setBold(run.bold() || heading);
                r.setItalic(run.italic());
                Integer rgb;
                if (heading) {
                    rgb = print.getHeadingColor() != null ? print.getHeadingColor() : print.getTextColor();
                } else if (run.bold() && print.getStrongColor() != null) {
                    rgb = print.getStrongColor();
                } else if (run.italic() && print.getEmphasisColor() != null) {
                    rgb = print.getEmphasisColor();
                } else {
                    rgb = quoteRgb != null ? quoteRgb : print.getTextColor();
                }
                if (rgb != null) {
                    r.setColor(PrintStyle.hex(rgb));
                }
                if (run.lineBreak()) {
                    r.addBreak();
                } else {
                    r.setText(run.text());
                }
            }
        }

        /**
         * A real Word footnote: the mark in the paragraph (Word numbers the notes itself) and the note at the foot of the page of the mark.
         */
        private void footnote(XWPFParagraph paragraph, List<List<Run>> blocks, int size) {
            XWPFFootnote footnote = document.createFootnote();
            inNote = true;
            try {
                boolean first = true;
                for (List<Run> block : blocks) {
                    XWPFParagraph note = footnote.createParagraph();
                    note.setSpacingAfter(0);
                    if (first) {
                        footnote.ensureFootnoteRef(note);
                        for (XWPFRun mark : note.getRuns()) {
                            mark.setSubscript(VerticalAlign.SUPERSCRIPT);
                            mark.setFontSize(size - 2);
                        }
                        XWPFRun space = note.createRun();
                        space.setFontSize(size - 2);
                        space.setText(" ");
                        first = false;
                    }
                    addRuns(note, block, size - 2, false, null);
                }
            } finally {
                inNote = false;
            }
            paragraph.addFootnoteReference(footnote);
            new XWPFRun(paragraph.getCTP().getRList().getLast(), paragraph).setSubscript(VerticalAlign.SUPERSCRIPT);
        }

        private class DocxSink implements BlockSink {
            private final boolean centered;
            // the first paragraph of the text, and the first one after a heading or a scene break, has no indent
            private boolean noIndentNext = true;

            private DocxSink(boolean centered) {
                this.centered = centered;
            }

            @Override
            public boolean notes(Map<String, List<List<Run>>> notes) {
                // the title page has no notes of its own: they stay endnotes there
                if (centered) {
                    return false;
                }
                partNotes.clear();
                partNotes.putAll(notes);
                return true;
            }

            @Override
            public void heading(int level, List<Run> runs) {
                XWPFParagraph paragraph = newParagraph();
                paragraph.setAlignment(centered ? ParagraphAlignment.CENTER : alignment(print.getHeadingAlign()));
                paragraph.setKeepNext(true);
                // outline level keeps the headings in the navigation pane of Word
                paragraph.getCTPPr().addNewOutlineLvl().setVal(java.math.BigInteger.valueOf(Math.min(level, 9) - 1));
                paragraph.setSpacingBefore(centered ? 4000 : 480);
                paragraph.setSpacingAfter(240);
                addRuns(paragraph, runs, headingSizes[Math.min(level, headingSizes.length) - 1], true, null);
                noIndentNext = true;
            }

            @Override
            public void paragraph(BlockContext context, List<Run> runs) {
                XWPFParagraph paragraph = newParagraph();
                paragraph.setAlignment(centered ? ParagraphAlignment.CENTER
                        : context.isIndented() ? ParagraphAlignment.LEFT : alignment(print.getAlign()));
                paragraph.setSpacingAfter(Math.round(print.getParagraphSpacing() * 20));
                if (print.isLineHeightSet()) {
                    paragraph.setSpacingBetween(print.getLineHeight(), LineSpacingRule.AUTO);
                }
                indent(paragraph, context);
                if (!centered && !context.isIndented() && !noIndentNext && print.getFirstLineIndent() > 0) {
                    paragraph.setFirstLineIndent(Math.round(print.getFirstLineIndent() * 20));
                }
                if (!centered) {
                    noIndentNext = false;
                }
                if (context.marker() != null) {
                    XWPFRun marker = paragraph.createRun();
                    marker.setFontFamily(font);
                    marker.setFontSize(bodySize);
                    marker.setText(context.marker() + " ");
                }
                addRuns(paragraph, runs, centered ? bodySize + 3 : bodySize, false, context.quoteDepth() > 0 ? print.getQuoteColor() : null);
            }

            @Override
            public void code(BlockContext context, String code) {
                XWPFParagraph paragraph = newParagraph();
                paragraph.setSpacingAfter(160);
                indent(paragraph, context);
                XWPFRun run = paragraph.createRun();
                run.setFontFamily(CODE_FONT);
                run.setFontSize(Math.max(6, bodySize - 2));
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
                XWPFParagraph paragraph = newParagraph();
                paragraph.setAlignment(ParagraphAlignment.CENTER);
                paragraph.setSpacingBefore(160);
                paragraph.setSpacingAfter(240);
                XWPFRun run = paragraph.createRun();
                run.setFontFamily(font);
                run.setFontSize(bodySize);
                Integer rgb = print.getSceneBreakColor() != null ? print.getSceneBreakColor() : print.getTextColor();
                if (rgb != null) {
                    run.setColor(PrintStyle.hex(rgb));
                }
                run.setText(print.getSceneBreak());
                noIndentNext = true;
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
