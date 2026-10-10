package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.List;

public class DocxExporter extends ExporterBase {
    private static final String FONT = "Times New Roman";
    private static final String CODE_FONT = "Courier New";
    private static final int BODY_SIZE = 12;
    private static final int[] HEADING_SIZES = {24, 20, 17, 15, 13, 12};
    // 1/20 of a point
    private static final int INDENT = 720;
    // position of the page numbers in the table of contents, it is within the text width of A4 and Letter
    private static final int CONTENTS_TAB = 9000;

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
        // bookmark of the chapter that is going to be written, put on its first paragraph
        private Chapter pendingChapter;
        // the next paragraph starts a page, the title page and the contents are pages of their own
        private boolean pageBreakPending;
        private int bookmarks;

        private DocxWriter(ExportOptions options) {
            document.getProperties().getCoreProperties().setTitle(options.bookName());
            if (StringUtils.isNotBlank(options.author())) {
                document.getProperties().getCoreProperties().setCreator(options.author());
            }
            addPageNumbers();
        }

        private void addPageNumbers() {
            XWPFFooter footer = new XWPFHeaderFooterPolicy(document).createFooter(STHdrFtr.DEFAULT);
            XWPFParagraph paragraph = footer.createParagraph();
            paragraph.setAlignment(ParagraphAlignment.CENTER);
            CTSimpleField field = paragraph.getCTP().addNewFldSimple();
            field.setInstr(" PAGE ");
            CTR run = field.addNewR();
            style(run, BODY_SIZE - 2);
            run.addNewT().setStringValue("1");
        }

        private static void style(CTR run, int size) {
            CTRPr properties = run.addNewRPr();
            CTFonts fonts = properties.addNewRFonts();
            fonts.setAscii(FONT);
            fonts.setHAnsi(FONT);
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
            addRuns(title, List.of(new Run(CONTENTS_TITLE, true, false, false, false)), HEADING_SIZES[1], true);

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
                style(run, BODY_SIZE);
                run.addNewT().setStringValue(chapter.title());

                CTR separator = entry.getCTP().addNewR();
                style(separator, BODY_SIZE);
                separator.addNewTab();
                field(entry, STFldCharType.BEGIN, null);
                field(entry, null, " PAGEREF " + chapter.anchor() + " \\h ");
                field(entry, STFldCharType.SEPARATE, null);
                field(entry, STFldCharType.END, null);
            }
            pageBreakPending = true;
            document.getSettings().setUpdateFields();
        }

        private static void field(XWPFParagraph paragraph, STFldCharType.Enum type, String instruction) {
            CTR run = paragraph.getCTP().addNewR();
            style(run, BODY_SIZE);
            if (type != null) {
                run.addNewFldChar().setFldCharType(type);
            } else {
                run.addNewInstrText().setStringValue(instruction);
            }
        }

        @Override
        public void message(String markdown, Chapter chapter) throws Exception {
            pendingChapter = chapter;
            // a chapter starts on a new page
            pageBreakPending |= chapter != null;
            walk(markdown, bodySink);
            pendingChapter = null;
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
                XWPFParagraph paragraph = newParagraph();
                paragraph.setAlignment(centered ? ParagraphAlignment.CENTER : ParagraphAlignment.LEFT);
                paragraph.setKeepNext(true);
                // outline level keeps the headings in the navigation pane of Word
                paragraph.getCTPPr().addNewOutlineLvl().setVal(java.math.BigInteger.valueOf(Math.min(level, 9) - 1));
                paragraph.setSpacingBefore(centered ? 4000 : 480);
                paragraph.setSpacingAfter(240);
                addRuns(paragraph, runs, HEADING_SIZES[Math.min(level, HEADING_SIZES.length) - 1], true);
            }

            @Override
            public void paragraph(BlockContext context, List<Run> runs) {
                XWPFParagraph paragraph = newParagraph();
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
                XWPFParagraph paragraph = newParagraph();
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
                XWPFParagraph paragraph = newParagraph();
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
