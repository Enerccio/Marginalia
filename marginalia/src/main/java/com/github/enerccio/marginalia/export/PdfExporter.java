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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * PDF with the embedded Liberation fonts, so the text is not limited to Latin-1, and a bundled Japanese font (a subset of Noto Sans JP, see
 * {@code src/main/resources-raw/fonts/README.md}) for the characters they lack: a character is drawn in the first font that has it. Footnotes are set
 * at the bottom of the page of their mark, in a smaller type under a short rule: the pages are laid out again until the space kept for the notes
 * of each page is the space they need (the same loop settles the page numbers of the table of contents).
 */
public class PdfExporter extends ExporterBase {
    private static final Logger log = LoggerFactory.getLogger(PdfExporter.class);

    /** The margins of the page; the bottom one is what the notes of a page add to. */
    private static final float MARGIN_SIDE = 54, MARGIN_TOP = 60, MARGIN_BOTTOM = 60;
    /** Between the text and the rule above the notes, and the rule and the notes. */
    private static final float NOTE_GAP = 8;
    private static final String NOTE_TAG = "fn:";

    private static final String CJK_FONT = "/fonts/NotoSansJP-Regular-subset.ttf";
    private static BaseFont cjkBase;
    private static boolean cjkTried;

    /**
     * @return the font of the characters Liberation lacks, null when it can't be loaded (they are then not drawn)
     */
    private static synchronized BaseFont cjk() {
        if (!cjkTried) {
            cjkTried = true;
            try (InputStream in = PdfExporter.class.getResourceAsStream(CJK_FONT)) {
                if (in == null) {
                    log.warn("The font of the Japanese text, {}, is not in the application: such text is not drawn in the PDF", CJK_FONT);
                } else {
                    cjkBase = BaseFont.createFont("NotoSansJP-Regular-subset.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, in.readAllBytes(), null);
                }
            } catch (Exception e) {
                log.warn("The font of the Japanese text can't be loaded, such text is not drawn in the PDF", e);
            }
        }
        return cjkBase;
    }

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
     * The table of contents shows the pages of the chapters, and the notes take room at the bottom of the pages that have them: both are known only when
     * the text is laid out, so the export is kept and rendered until the pages and the room kept for the notes stay the same.
     */
    private static class PdfWriterAdapter implements ExportWriter {
        private static final int MAX_PASSES = 8;

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
            Map<Integer, Float> reserve = new HashMap<>();
            // the largest room each page was ever asked for: when the layout does not settle, the last passes keep at least that much and so it does
            Map<Integer, Float> largest = new HashMap<>();
            PdfRendering rendering = new PdfRendering(options, titleMarkdown, chapters, messages, chapterOf, Map.of(), reserve);
            byte[] result = rendering.render();
            for (int pass = 1; pass < MAX_PASSES && !rendering.isSettled(chapters); pass++) {
                rendering.needed.forEach((page, height) -> largest.merge(page, height, Math::max));
                reserve = pass >= MAX_PASSES - 2 ? new HashMap<>(largest) : new HashMap<>(rendering.needed);
                rendering = new PdfRendering(options, titleMarkdown, chapters, messages, chapterOf, rendering.chapterPages, reserve);
                result = rendering.render();
            }
            if (!rendering.uncovered.isEmpty()) {
                StringBuilder missing = new StringBuilder();
                rendering.uncovered.stream().limit(20).forEach(cp -> missing.append(String.format("U+%04X ", cp)));
                log.warn("{} different characters of the book are in none of the fonts of the PDF and are not drawn: {}", rendering.uncovered.size(), missing.toString().strip());
            }
            return result;
        }
    }

    private static class PdfRendering {
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private final Document document = new Document(PageSize.A5, MARGIN_SIDE, MARGIN_SIDE, MARGIN_TOP, MARGIN_BOTTOM);
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

        /** A footnote mark of the text: the note is looked up by the label in the notes of its part when the page is closed (the text of a part may come in pieces). */
        private record Note(String label, Map<String, List<List<Run>>> source) {
        }

        // the room kept at the bottom of each page for its notes (from the pass before), and what the notes of each page need in this one
        private final Map<Integer, Float> reserve;
        final Map<Integer, Float> needed = new HashMap<>();
        private final Map<String, Note> marks = new HashMap<>();
        // the height of the notes set on each page so far
        private final Map<Integer, Float> usedOnPage = new HashMap<>();
        // the notes of the part that is being written, and whether the marks of the text are footnote marks (not on the cover)
        private Map<String, List<List<Run>>> partNotes = new HashMap<>();
        private boolean placingNotes;
        private boolean inNote;
        private int markCount;
        // the characters that are in none of the fonts
        final Set<Integer> uncovered = new TreeSet<>();

        private PdfRendering(ExportOptions options, String titleMarkdown, List<Chapter> chapters, List<List<Part>> messages,
                             List<Chapter> chapterOf, Map<String, Integer> shownPages, Map<Integer, Float> reserve) throws Exception {
            this.reserve = reserve;
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
            // the first page is laid out with the room its notes need, the later ones are set when the page before them ends
            document.setMargins(MARGIN_SIDE, MARGIN_SIDE, MARGIN_TOP, MARGIN_BOTTOM + reserve.getOrDefault(1, 0f));
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
                    int page = writer.getPageNumber();
                    // the next page keeps the room its notes had in the pass before
                    document.setMargins(MARGIN_SIDE, MARGIN_SIDE, MARGIN_TOP, MARGIN_BOTTOM + reserve.getOrDefault(page + 1, 0f));
                    // the title page is the cover, the numbers are the pages of the file as the readers count them
                    if (titleMarkdown != null && page == 1) {
                        return;
                    }
                    ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                            new Phrase(String.valueOf(page), small),
                            (document.left() + document.right()) / 2, MARGIN_BOTTOM - 28, 0);
                }

                @Override
                public void onGenericTag(PdfWriter writer, Document document, Rectangle rect, String text) {
                    if (text.startsWith(NOTE_TAG)) {
                        // the page is not finished when its last lines are written (its end event comes before them), so each note is set when its mark is
                        Note note = marks.get(text);
                        if (note != null) {
                            placeNote(writer, writer.getPageNumber(), note);
                        }
                    } else {
                        chapterPages.put(text, writer.getPageNumber());
                    }
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

        /**
         * @return whether another pass would lay the book out the same: the chapters are on the pages the table of contents shows, and every page has
         * the room its notes need
         */
        boolean isSettled(List<Chapter> chapters) {
            if (!chapters.isEmpty() && !chapterPages.equals(shownPages)) {
                return false;
            }
            Set<Integer> pages = new HashSet<>(needed.keySet());
            pages.addAll(reserve.keySet());
            for (int page : pages) {
                if (Math.abs(needed.getOrDefault(page, 0f) - reserve.getOrDefault(page, 0f)) > 0.5f) {
                    return false;
                }
            }
            return true;
        }

        private float noteSize() {
            return bodySize * 0.8f;
        }

        /** The paragraphs of the notes of a page, a number in front of the first one of each. */
        private List<Paragraph> noteParagraphs(List<Note> notes) {
            List<Paragraph> result = new ArrayList<>();
            inNote = true;
            try {
                float size = noteSize();
                for (Note note : notes) {
                    List<List<Run>> blocks = note.source().get(note.label());
                    if (blocks == null) {
                        continue;
                    }
                    boolean first = true;
                    for (List<Run> runs : blocks) {
                        Paragraph paragraph = buildParagraph(runs, size, false, null);
                        if (first) {
                            paragraph.add(0, new Chunk(note.label() + " ", textFont(regular, size, style.getTextColor())));
                            first = false;
                        }
                        paragraph.setAlignment(Element.ALIGN_LEFT);
                        paragraph.setSpacingAfter(2);
                        result.add(paragraph);
                    }
                }
            } finally {
                inNote = false;
            }
            return result;
        }

        /** The height of the notes laid out in the width of the text, without the rule and the gaps. */
        private float heightOf(List<Paragraph> paragraphs) {
            float top = 5000;
            ColumnText column = new ColumnText(writer.getDirectContent());
            column.setSimpleColumn(document.left(), 0, document.right(), top);
            paragraphs.forEach(column::addElement);
            try {
                column.go(true);
            } catch (DocumentException e) {
                throw new IllegalStateException(e);
            }
            return top - column.getYLine();
        }

        /**
         * Sets the note of a mark at the bottom of its page, above the number of the page: the notes of a page are one under another, from the top of the
         * room the pass before kept for them, the first one under a short rule. What the notes of the page need is kept for the next pass. A note that does
         * not fit in the room is left out while the layout has not settled, the book is laid out again with room for it.
         */
        private void placeNote(PdfWriter writer, int page, Note note) {
            List<Paragraph> paragraphs = noteParagraphs(List.of(note));
            if (paragraphs.isEmpty()) {
                return;
            }
            float height = heightOf(paragraphs);
            float used = usedOnPage.getOrDefault(page, 0f);
            usedOnPage.put(page, used + height);
            needed.put(page, used + height + NOTE_GAP);
            if (reserve.isEmpty()) {
                // the first pass only measures
                return;
            }
            float roomTop = MARGIN_BOTTOM + Math.max(0, reserve.getOrDefault(page, 0f) - NOTE_GAP);
            float top = roomTop - used;
            if (top - height < MARGIN_BOTTOM - 4) {
                log.debug("The note {} does not fit on page {} yet", note.label(), page);
                return;
            }
            PdfContentByte canvas = writer.getDirectContent();
            ColumnText column = new ColumnText(canvas);
            column.setSimpleColumn(document.left(), top - height - 3, document.right(), top);
            paragraphs.forEach(column::addElement);
            try {
                column.go();
            } catch (DocumentException e) {
                throw new IllegalStateException(e);
            }
            if (used == 0) {
                canvas.saveState();
                canvas.setLineWidth(0.4f);
                canvas.setColorStroke(color(style.getTextColor() != null ? style.getTextColor() : 0x000000));
                canvas.moveTo(document.left(), roomTop + NOTE_GAP / 2);
                canvas.lineTo(document.left() + (document.right() - document.left()) * 0.3f, roomTop + NOTE_GAP / 2);
                canvas.stroke();
                canvas.restoreState();
            }
        }

        private static boolean covers(Font font, int codePoint) {
            return font.getBaseFont().charExists(codePoint);
        }

        /**
         * The text as chunks, each character in the first font that has it: the font given (Liberation), then the Japanese one in the same size, style
         * and color. A character that no font has stays in the font it is next to, where it is not drawn, and is counted for the warning.
         */
        private List<Chunk> textChunks(String text, Font font, boolean bold, boolean italic) {
            BaseFont fallback = cjk();
            if (fallback == null || text.isEmpty()) {
                return List.of(new Chunk(text, font));
            }
            int style = (bold ? Font.BOLD : 0) | (italic ? Font.ITALIC : 0);
            Font japanese = new Font(fallback, font.getSize(), style == 0 ? Font.NORMAL : style, font.getColor());
            List<Chunk> chunks = new ArrayList<>();
            StringBuilder run = new StringBuilder();
            Font current = null;
            for (int i = 0; i < text.length(); ) {
                int cp = text.codePointAt(i);
                i += Character.charCount(cp);
                Font choice;
                if (cp < 0x20 || Character.isWhitespace(cp)) {
                    choice = current != null ? current : font;
                } else if (current != null && covers(current, cp)) {
                    choice = current;
                } else if (covers(font, cp)) {
                    choice = font;
                } else if (covers(japanese, cp)) {
                    choice = japanese;
                } else {
                    uncovered.add(cp);
                    choice = current != null ? current : font;
                }
                if (choice != current && run.length() > 0) {
                    chunks.add(new Chunk(run.toString(), current));
                    run.setLength(0);
                }
                current = choice;
                run.appendCodePoint(cp);
            }
            if (run.length() > 0) {
                chunks.add(new Chunk(run.toString(), current != null ? current : font));
            }
            return chunks;
        }

        private byte[] render() throws Exception {
            if (titleMarkdown != null) {
                walk(titleMarkdown, titleSink);
                document.newPage();
            }
            if (!chapters.isEmpty()) {
                writeContents();
            }
            placingNotes = true;
            for (int i = 0; i < messages.size(); i++) {
                partNotes = new HashMap<>();
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
            placingNotes = false;
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
                Chunk page = new Chunk(" " + String.valueOf(shownPages.getOrDefault(chapter.anchor(), 0)), textFont(regular, bodySize, style.getTextColor()));
                page.setLocalGoto(chapter.anchor());
                Paragraph entry = new Paragraph(bodySize * 1.4f);
                for (Chunk link : textChunks(chapter.title() + " ", textFont(regular, bodySize, style.getTextColor()), false, false)) {
                    link.setLocalGoto(chapter.anchor());
                    entry.add(link);
                }
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
            return anchored(buildParagraph(runs, size, heading, quoteRgb));
        }

        private Paragraph buildParagraph(List<Run> runs, float size, boolean heading, Integer quoteRgb) {
            Paragraph paragraph = new Paragraph();
            paragraph.setLeading(size * style.getLineHeight());
            for (Run run : runs) {
                if (run.lineBreak()) {
                    paragraph.add(Chunk.NEWLINE);
                    continue;
                }
                Font font = font(run, size, heading, quoteRgb);
                if (run.note() != null && placingNotes && !inNote) {
                    // a footnote mark: a small raised number; the note goes to the bottom of the page the mark is on
                    Font raised = new Font(font.getBaseFont(), size * 0.62f, font.getStyle(), font.getColor());
                    Chunk mark = new Chunk(run.note(), raised);
                    mark.setTextRise(size * 0.38f);
                    String id = NOTE_TAG + (++markCount);
                    mark.setGenericTag(id);
                    marks.put(id, new Note(run.note(), partNotes));
                    paragraph.add(mark);
                } else {
                    textChunks(run.text(), font, run.bold() || heading, run.italic()).forEach(paragraph::add);
                }
            }
            return paragraph;
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
            public boolean notes(Map<String, List<List<Run>>> notes) {
                if (centered) {
                    return false;
                }
                partNotes.putAll(notes);
                return true;
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
