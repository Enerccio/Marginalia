package com.github.enerccio.marginalia.utils.docimport;

import org.apache.poi.xwpf.usermodel.*;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Reading Word and LibreOffice files: headings, the title, emphasis as Markdown and the escaping of what Markdown would take
 * for its own.
 */
class DocumentFormatTest {

    private static final String ODT_NS = "xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\" "
            + "xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\" "
            + "xmlns:style=\"urn:oasis:names:tc:opendocument:xmlns:style:1.0\" "
            + "xmlns:fo=\"urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0\"";

    @Test
    void formatIsTakenFromTheExtension() {
        assertThat(DocumentFormat.of("Book.DOCX")).isEqualTo(DocumentFormat.DOCX);
        assertThat(DocumentFormat.of("book.odt")).isEqualTo(DocumentFormat.ODT);
        assertThat(DocumentFormat.of("book.doc")).isNull();
        assertThat(DocumentFormat.of(null)).isNull();
        assertThat(DocumentFormat.DOCX.baseName("My Book.docx")).isEqualTo("My Book");
    }

    private static void style(XWPFStyles styles, String id, String name) {
        CTStyle style = CTStyle.Factory.newInstance();
        style.setStyleId(id);
        style.addNewName().setVal(name);
        styles.addStyle(new XWPFStyle(style));
    }

    @Test
    void docxHeadingsParagraphsAndEmphasis() throws Exception {
        byte[] file;
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFStyles styles = document.createStyles();
            // the id of a localized Word is not Heading1, the name of the built in style is
            style(styles, "Nadpis1", "heading 1");
            style(styles, "Nazev", "Title");
            XWPFParagraph title = document.createParagraph();
            title.setStyle("Nazev");
            title.createRun().setText("The Title");
            XWPFParagraph chapter = document.createParagraph();
            chapter.setStyle("Nadpis1");
            chapter.createRun().setText("Chapter One");
            XWPFParagraph text = document.createParagraph();
            XWPFRun plain = text.createRun();
            plain.setText("He said ");
            XWPFRun italic = text.createRun();
            italic.setItalic(true);
            italic.setText("never * again ");
            XWPFRun bold = text.createRun();
            bold.setBold(true);
            bold.setText("loudly");
            document.createParagraph().createRun().setText("   ");
            document.createParagraph().createRun().setText("- not a list");
            XWPFParagraph outlined = document.createParagraph();
            outlined.getCTP().addNewPPr().addNewOutlineLvl().setVal(BigInteger.ONE);
            outlined.createRun().setText("Scene");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            file = out.toByteArray();
        }

        TextDocument document = DocumentFormat.DOCX.read(file);

        assertThat(document.title()).isEqualTo("The Title");
        assertThat(document.blocks()).containsExactly(
                new TextBlock(1, "Chapter One"),
                new TextBlock(0, "He said *never \\* again* **loudly**"),
                new TextBlock(0, "\\- not a list"),
                new TextBlock(2, "Scene"));
    }

    @Test
    void notADocxIsRefused() {
        assertThatThrownBy(() -> DocumentFormat.DOCX.read("plain text".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DocumentFormat.DOCX.read(new byte[0])).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void odtHeadingsTitleStylesAndLists() throws Exception {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><office:document-content " + ODT_NS + ">"
                + "<office:automatic-styles>"
                + "<style:style style:name=\"T1\" style:family=\"text\"><style:text-properties fo:font-style=\"italic\"/></style:style>"
                + "<style:style style:name=\"T2\" style:family=\"text\"><style:text-properties fo:font-weight=\"bold\"/></style:style>"
                + "<style:style style:name=\"P1\" style:family=\"paragraph\"><style:text-properties fo:font-style=\"italic\"/></style:style>"
                + "</office:automatic-styles><office:body><office:text>"
                + "<text:p text:style-name=\"Title\">My Novel</text:p>"
                + "<text:h text:outline-level=\"1\">One</text:h>"
                + "<text:p>He said <text:span text:style-name=\"T1\">never</text:span> and <text:span text:style-name=\"T2\">loudly </text:span>"
                + "now<text:s text:c=\"2\"/>and<text:line-break/>then<text:note><text:note-body><text:p>footnote</text:p></text:note-body></text:note></text:p>"
                + "<text:p text:style-name=\"P1\">all italic</text:p>"
                + "<text:list><text:list-item><text:p>in a list</text:p></text:list-item></text:list>"
                + "<text:tracked-changes><text:changed-region><text:deletion><text:p>deleted</text:p></text:deletion></text:changed-region></text:tracked-changes>"
                + "<text:h text:outline-level=\"2\">Scene</text:h>"
                + "</office:text></office:body></office:document-content>";

        TextDocument document = DocumentFormat.ODT.read(odt(content));

        assertThat(document.title()).isEqualTo("My Novel");
        assertThat(document.blocks()).containsExactly(
                new TextBlock(1, "One"),
                new TextBlock(0, "He said *never* and **loudly** now and  \nthen"),
                new TextBlock(0, "*all italic*"),
                new TextBlock(0, "in a list"),
                new TextBlock(2, "Scene"));
        assertThat(document.headingLevels()).isEqualTo(List.of(1, 2));
    }

    @Test
    void odtWithADoctypeIsRefused() throws Exception {
        String content = "<?xml version=\"1.0\"?><!DOCTYPE x [<!ENTITY e SYSTEM \"file:///etc/passwd\">]><office:document-content " + ODT_NS
                + "><office:body><office:text><text:p>&e;</text:p></office:text></office:body></office:document-content>";

        assertThatThrownBy(() -> DocumentFormat.ODT.read(odt(content))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void notAnOdtIsRefused() {
        assertThatThrownBy(() -> DocumentFormat.ODT.read("plain text".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static byte[] odt(String content) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry("content.xml"));
            zip.write(content.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return out.toByteArray();
    }
}
