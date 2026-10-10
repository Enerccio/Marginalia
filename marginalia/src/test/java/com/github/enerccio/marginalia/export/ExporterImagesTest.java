package com.github.enerccio.marginalia.export;

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
import org.apache.poi.xwpf.usermodel.XWPFDocument;
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
    void textMarksTheImages() throws Exception {
        String text = new String(export("txt"), StandardCharsets.UTF_8);

        assertThat(text).contains("[Image: " + CAPTION + "]").contains("[Image]").contains("Text of the last message.");
    }
}
