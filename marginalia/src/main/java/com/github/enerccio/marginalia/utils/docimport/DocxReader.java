package com.github.enerccio.marginalia.utils.docimport;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPrBase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a Word document ({@code .docx}). A heading is a paragraph of the style {@code Heading 1} to {@code Heading 6} (the
 * built in name, whatever language the program shows) or one that has an outline level; {@code Title} is the title of the
 * document. Tables are read cell by cell, in order; headers, footers, footnotes and text boxes are left out.
 */
final class DocxReader {

    private static final Pattern HEADING_STYLE = Pattern.compile("^heading\\s*(\\d)$");

    private DocxReader() {
    }

    static TextDocument read(byte[] data) {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(data))) {
            Collected collected = new Collected();
            collect(document, document.getBodyElements(), collected);
            return new TextDocument(collected.title, collected.blocks);
        } catch (IOException | RuntimeException e) {
            // POI reports a file that is not a docx (a .doc, a renamed file, a damaged zip) with several exception types
            throw new IllegalArgumentException("Not a Word document: " + e.getMessage(), e);
        }
    }

    private static final class Collected {
        String title;
        final List<TextBlock> blocks = new ArrayList<>();
    }

    private static void collect(XWPFDocument document, List<IBodyElement> elements, Collected collected) {
        for (IBodyElement element : elements) {
            if (element instanceof XWPFParagraph paragraph) {
                paragraph(document, paragraph, collected);
            } else if (element instanceof XWPFTable table) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        collect(document, cell.getBodyElements(), collected);
                    }
                }
            }
        }
    }

    private static void paragraph(XWPFDocument document, XWPFParagraph paragraph, Collected collected) {
        RichText text = new RichText();
        for (IRunElement element : paragraph.getIRuns()) {
            if (element instanceof XWPFRun run) {
                text.add(run.text(), run.isBold(), run.isItalic());
            }
        }
        if (text.isBlank()) {
            return;
        }
        int level = headingLevel(document, paragraph);
        if (level < 0) {
            if (collected.title == null) {
                collected.title = text.plain();
            }
        } else if (level == 0) {
            collected.blocks.add(new TextBlock(0, text.markdown()));
        } else {
            collected.blocks.add(new TextBlock(level, text.plain()));
        }
    }

    /**
     * @return -1 for the title, 0 for a paragraph, 1 to 6 for a heading
     */
    private static int headingLevel(XWPFDocument document, XWPFParagraph paragraph) {
        XWPFStyle style = paragraph.getStyleID() == null || document.getStyles() == null ? null
                : document.getStyles().getStyle(paragraph.getStyleID());
        if (style != null && style.getName() != null) {
            String name = style.getName().trim().toLowerCase();
            if (name.equals("title")) {
                return -1;
            }
            Matcher matcher = HEADING_STYLE.matcher(name);
            if (matcher.matches()) {
                return clamp(Integer.parseInt(matcher.group(1)));
            }
        }
        int level = outline(paragraph.getCTP().getPPr());
        if (level == 0 && style != null && style.getCTStyle() != null) {
            level = outline(style.getCTStyle().getPPr());
        }
        return level;
    }

    // the outline level of Word is counted from 0, 9 is the body text
    private static int outline(CTPPrBase properties) {
        if (properties == null || !properties.isSetOutlineLvl()) {
            return 0;
        }
        int level = properties.getOutlineLvl().getVal().intValue();
        return level >= 0 && level < TextBlock.MAX_LEVEL ? level + 1 : 0;
    }

    private static int clamp(int level) {
        return Math.max(1, Math.min(TextBlock.MAX_LEVEL, level));
    }
}
