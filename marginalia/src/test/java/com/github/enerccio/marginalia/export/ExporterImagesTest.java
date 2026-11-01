package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.model.ImageAnchor;
import com.github.enerccio.marginalia.domain.model.ImageAttachment;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.ExporterService;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import com.github.enerccio.marginalia.domain.service.ExporterService.Exporter;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.ProgressBarDialog;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Images attached to messages are embedded in the exports, they are not part of the message text.
 */
class ExporterImagesTest extends MarginaliaTestBase {
    private static final String CAPTION = "A map & a <legend>";

    @Autowired
    private ExporterService exporterService;

    @Autowired
    private ResourceService resourceService;

    private List<ChatMessage> story;

    @BeforeEach
    void createStory() throws Exception {
        login();
        BufferedImage image = new BufferedImage(40, 20, BufferedImage.TYPE_INT_RGB);
        image.setRGB(1, 1, uniqueName("image").hashCode());
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(image, "png", png);
        Resource map = resourceService.uploadImage("map.png", png.toByteArray());

        ChatMessage withImage = new ChatMessage();
        withImage.setResponse("# Chapter\n\nThe story goes on.");
        withImage.setImages(List.of(new ImageAttachment(map.getUuid(), CAPTION)));

        // no text at all, still exported
        ChatMessage onlyImage = new ChatMessage();
        onlyImage.setResponse("  ");
        onlyImage.setImages(List.of(new ImageAttachment(map.getUuid(), "")));

        // image that does not exist is skipped, the text is still exported
        ChatMessage missing = new ChatMessage();
        missing.setResponse("Text of the last message.");
        missing.setImages(List.of(new ImageAttachment("no-such-uuid", "gone")));

        story = List.of(withImage, onlyImage, missing);
    }

    private byte[] export(String extension) throws Exception {
        Exporter exporter = exporterService.getExporters().stream()
                .filter(e -> e.getFileExtension().equals(extension)).findFirst().orElseThrow();
        return exporter.export(new ExportOptions("Book", "Author", true, "en"), story, new ProgressBarDialog(false));
    }

    private static Map<String, byte[]> unzip(byte[] zip) throws Exception {
        Map<String, byte[]> entries = new HashMap<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            for (ZipEntry entry = in.getNextEntry(); entry != null; entry = in.getNextEntry()) {
                entries.put(entry.getName(), in.readAllBytes());
            }
        }
        return entries;
    }

    @Test
    void imagesAreNotInTheMessageText() {
        assertThat(story.getFirst().getResponse()).doesNotContain("![").doesNotContain("data:");
        assertThat(story.getFirst().getImages()).hasSize(1);
    }

    @Test
    void htmlEmbedsImagesAsDataUris() throws Exception {
        String html = new String(export("html"), StandardCharsets.UTF_8);

        // the image of the first message and the image-only message
        assertThat(html.split("<img src=\"data:image/png;base64,", -1)).hasSize(3);
        assertThat(html).contains("<figcaption>A map &amp; a &lt;legend&gt;</figcaption>");
        assertThat(html).contains("Text of the last message.").doesNotContain("gone");
    }

    @Test
    void epubContainsTheImageFilesOnce() throws Exception {
        Map<String, byte[]> epub = unzip(export("epub"));

        assertThat(epub.keySet().stream().filter(name -> name.startsWith("OEBPS/images/"))).hasSize(2);
        String manifest = new String(epub.get("OEBPS/content.opf"), StandardCharsets.UTF_8);
        assertThat(manifest).contains("href=\"images/image1.png\" media-type=\"image/png\"")
                .contains("href=\"images/image2.png\" media-type=\"image/png\"");
        String part = new String(epub.get("OEBPS/part1.xhtml"), StandardCharsets.UTF_8);
        assertThat(part).contains("src=\"images/image1.png\"").contains("A map &amp; a &lt;legend&gt;");
        assertThat(ImageIO.read(new ByteArrayInputStream(epub.get("OEBPS/images/image1.png")))).isNotNull();
    }

    @Test
    void docxContainsThePictures() throws Exception {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(export("docx")))) {
            assertThat(document.getAllPictures()).isNotEmpty();
            assertThat(document.getParagraphs().stream().map(p -> p.getText()).toList()).contains(CAPTION);
        }
    }

    @Test
    void pdfContainsTheImages() throws Exception {
        try (PDDocument document = Loader.loadPDF(export("pdf"))) {
            List<COSName> images = new ArrayList<>();
            for (PDPage page : document.getPages()) {
                for (COSName name : page.getResources().getXObjectNames()) {
                    if (page.getResources().isImageXObject(name)) {
                        images.add(name);
                    }
                }
            }
            assertThat(images).isNotEmpty();
        }
    }

    @Test
    void markdownEmbedsImagesAsDataUris() throws Exception {
        String markdown = new String(export("md"), StandardCharsets.UTF_8);

        // the image of the first message and the image-only message
        assertThat(markdown.split("\\]\\(data:image/png;base64,", -1)).hasSize(3);
        assertThat(markdown).contains("![A map & a \\<legend\\>](data:image/png;base64,").contains("![](data:image/png;base64,")
                .contains("*A map & a \\<legend\\>*").contains("Text of the last message.").doesNotContain("gone");
    }

    @Test
    void textMarksTheImages() throws Exception {
        String text = new String(export("txt"), StandardCharsets.UTF_8);

        assertThat(text).contains("[Image: " + CAPTION + "]").contains("[Image]").contains("Text of the last message.");
    }

    // ------------------------------------------------------------------------------------------------------------
    // where the images are
    // ------------------------------------------------------------------------------------------------------------

    private static final String TEXT = "Alpha one.\n\nBravo two.\n\nCharlie three.\n\nDelta four.";

    /**
     * A part with four paragraphs: a picture the text flows around at the top left, one in the middle, and one after the text at the
     * right; and the same ones in the order of the exports.
     */
    private List<ChatMessage> placedStory() throws Exception {
        BufferedImage image = new BufferedImage(40, 20, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(image, "png", png);
        String uuid = resourceService.uploadImage("map.png", png.toByteArray()).getUuid();

        ChatMessage message = new ChatMessage();
        message.setResponse(TEXT);
        message.setImages(List.of(
                new ImageAttachment(uuid, "Caption-end", ImageAnchor.BOTTOM_RIGHT, false, null),
                new ImageAttachment(uuid, "Caption-top", ImageAnchor.TOP_LEFT, true, 30),
                new ImageAttachment(uuid, "Caption-mid", ImageAnchor.CENTER_CENTER, false, 50)));
        return List.of(message);
    }

    private byte[] exportPlaced(String extension) throws Exception {
        Exporter exporter = exporterService.getExporters().stream()
                .filter(e -> e.getFileExtension().equals(extension)).findFirst().orElseThrow();
        return exporter.export(new ExportOptions("Book", "Author", false, "en"), placedStory(), new ProgressBarDialog(false));
    }

    private static void assertInOrder(String text, String... parts) {
        int at = -1;
        for (String part : parts) {
            int found = text.indexOf(part, at + 1);
            assertThat(found).describedAs("%s after position %d in %s", part, at, text).isGreaterThan(at);
            at = found;
        }
    }

    @Test
    void htmlPutsTheImagesAmongTheText() throws Exception {
        String html = new String(exportPlaced("html"), StandardCharsets.UTF_8);

        assertInOrder(html, "Caption-top", "Alpha one.", "Bravo two.", "Caption-mid", "Charlie three.", "Delta four.", "Caption-end");
        // the width asked for, and the side the text flows around
        assertThat(html).contains("<figure class=\"float-left sized\" style=\"width: 30%\">")
                .contains("<figure class=\"sized\" style=\"width: 50%\">")
                .contains("<div class=\"pictures pictures-center\">").contains("<div class=\"pictures pictures-right\">")
                .contains("figure.float-left {");
        // the part keeps the picture it flows around
        assertThat(html).contains("<div class=\"clear\"></div>");
    }

    @Test
    void epubPutsTheImagesAmongTheText() throws Exception {
        Map<String, byte[]> epub = unzip(exportPlaced("epub"));
        String part = new String(epub.get("OEBPS/part1.xhtml"), StandardCharsets.UTF_8);

        assertInOrder(part, "Caption-top", "Alpha one.", "Bravo two.", "Caption-mid", "Charlie three.", "Delta four.", "Caption-end");
        assertThat(part).contains("class=\"float-left sized\"");
        assertThat(new String(epub.get("OEBPS/style.css"), StandardCharsets.UTF_8)).contains("figure.float-left");
    }

    @Test
    void formatsThatDoNotPlaceImagesWriteThemWhereTheyComeInTheText() throws Exception {
        String text = new String(exportPlaced("txt"), StandardCharsets.UTF_8);
        assertInOrder(text, "[Image: Caption-top]", "Alpha one.", "Bravo two.", "[Image: Caption-mid]", "Charlie three.", "Delta four.",
                "[Image: Caption-end]");

        String markdown = new String(exportPlaced("md"), StandardCharsets.UTF_8);
        assertInOrder(markdown, "*Caption-top*", "Alpha one.", "Bravo two.", "*Caption-mid*", "Charlie three.", "Delta four.", "*Caption-end*");
    }

    @Test
    void docxFloatsThePictureTheTextFlowsAroundAndPutsTheOthersAmongTheParagraphs() throws Exception {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(exportPlaced("docx")))) {
            List<String> order = new ArrayList<>();
            for (IBodyElement element : document.getBodyElements()) {
                if (element instanceof XWPFTable table) {
                    // the table of the picture the text flows around is positioned
                    assertThat(table.getCTTbl().getTblPr().isSetTblpPr()).isTrue();
                    order.add("table:" + table.getText().strip());
                } else if (element instanceof org.apache.poi.xwpf.usermodel.XWPFParagraph paragraph) {
                    order.add(paragraph.getText());
                }
            }
            String all = String.join("|", order);
            assertInOrder(all, "table:Caption-top", "Alpha one.", "Bravo two.", "Caption-mid", "Charlie three.", "Delta four.", "Caption-end");
            assertThat(document.getAllPictures()).isNotEmpty();
        }
    }

    @Test
    void docxPutsImagesWithAWidthNextToEachOtherInARow() throws Exception {
        BufferedImage image = new BufferedImage(40, 20, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(image, "png", png);
        String uuid = resourceService.uploadImage("row.png", png.toByteArray()).getUuid();
        ChatMessage message = new ChatMessage();
        message.setResponse("Text.");
        message.setImages(List.of(new ImageAttachment(uuid, "Left", ImageAnchor.BOTTOM_LEFT, false, 40),
                new ImageAttachment(uuid, "Right", ImageAnchor.BOTTOM_LEFT, false, 40)));
        Exporter exporter = exporterService.getExporters().stream().filter(e -> e.getFileExtension().equals("docx")).findFirst().orElseThrow();
        byte[] docx = exporter.export(new ExportOptions("Book", "Author", false, "en"), List.of(message), new ProgressBarDialog(false));

        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(docx))) {
            assertThat(document.getTables()).hasSize(1);
            assertThat(document.getTables().getFirst().getRow(0).getTableCells()).hasSize(2);
            assertThat(document.getTables().getFirst().getText()).contains("Left").contains("Right");
        }
    }

    @Test
    void pdfHasAllTheTextAndTheCaptionsOfTheImagesAmongIt() throws Exception {
        try (PDDocument document = Loader.loadPDF(exportPlaced("pdf"))) {
            String text = new PDFTextStripper().getText(document);

            assertThat(text).contains("Alpha one.").contains("Bravo two.").contains("Charlie three.").contains("Delta four.")
                    .contains("Caption-top").contains("Caption-mid").contains("Caption-end");
            // the picture in the middle is between the paragraphs
            assertInOrder(text, "Bravo two.", "Caption-mid", "Charlie three.");
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // text on both sides of an image in the middle
    // ------------------------------------------------------------------------------------------------------------

    private byte[] exportBand(String extension) throws Exception {
        BufferedImage image = new BufferedImage(40, 20, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(image, "png", png);
        String uuid = resourceService.uploadImage("band.png", png.toByteArray()).getUuid();
        ChatMessage message = new ChatMessage();
        message.setResponse(String.join("\n\n", List.of("Firstpar", "Secondpar", "Thirdpar", "Fourthpar").stream()
                .map(name -> name + " " + "lorem ipsum ".repeat(20)).toList()));
        message.setImages(List.of(new ImageAttachment(uuid, "Caption-band", ImageAnchor.TOP_CENTER, true, 30)));
        Exporter exporter = exporterService.getExporters().stream().filter(e -> e.getFileExtension().equals(extension)).findFirst().orElseThrow();
        return exporter.export(new ExportOptions("Book", "Author", false, "en"), List.of(message), new ProgressBarDialog(false));
    }

    @Test
    void htmlHasTheTextInTwoColumnsWithTheImageBetweenThem() throws Exception {
        String html = new String(exportBand("html"), StandardCharsets.UTF_8);

        assertThat(html).contains("<div class=\"band\" style=\"grid-template-columns: 1fr 30% 1fr\">")
                .contains("<div class=\"band-middle\">");
        assertInOrder(html, "Firstpar", "Caption-band", "Secondpar", "Thirdpar", "Fourthpar");
        // two blocks are enough for a band of 400 characters, the rest of the text follows it
        assertThat(html.indexOf("class=\"band-middle\"")).isLessThan(html.indexOf("Thirdpar"));
    }

    @Test
    void formatsWithoutColumnsWriteTheLeftTextTheImageAndTheRightText() throws Exception {
        String text = new String(exportBand("txt"), StandardCharsets.UTF_8);

        assertInOrder(text, "Firstpar", "[Image: Caption-band]", "Secondpar", "Thirdpar", "Fourthpar");
        String epub = new String(unzip(exportBand("epub")).get("OEBPS/part1.xhtml"), StandardCharsets.UTF_8);
        assertInOrder(epub, "Firstpar", "Caption-band", "Secondpar", "Thirdpar");
    }

    @Test
    void docxFloatsTheImageInTheMiddleSoTheTextFlowsOnBothSidesOfIt() throws Exception {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(exportBand("docx")))) {
            assertThat(document.getTables()).hasSize(1);
            assertThat(document.getTables().getFirst().getCTTbl().getTblPr().getTblpPr().getTblpXSpec().toString()).isEqualTo("center");
            assertThat(document.getTables().getFirst().getText()).contains("Caption-band");
            assertThat(document.getParagraphs().stream().map(p -> p.getText()).reduce("", String::concat))
                    .contains("Firstpar").contains("Fourthpar");
        }
    }

    @Test
    void pdfHasTheBandTextAndTheCaption() throws Exception {
        try (PDDocument document = Loader.loadPDF(exportBand("pdf"))) {
            String text = new PDFTextStripper().getText(document);

            assertThat(text).contains("Firstpar").contains("Secondpar").contains("Thirdpar").contains("Fourthpar").contains("Caption-band");
        }
    }
}
