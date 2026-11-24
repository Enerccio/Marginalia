package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.service.agent.ImageFiles;
import com.github.enerccio.marginalia.domain.service.agent.VirtualFileException;
import com.github.enerccio.marginalia.domain.service.agent.VirtualPath;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.github.enerccio.marginalia.test.TestImages;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Which bytes are an image of the workspace, and what a model is sent of one.
 */
class ImageFilesTest {

    private static final VirtualPath FILE = VirtualPath.parse("/refs/pic.png");

    private static byte[] image(int width, int height, String format) throws IOException {
        return TestImages.image(width, height, format);
    }

    private static byte[] webp() {
        byte[] bytes = new byte[40];
        System.arraycopy("RIFF".getBytes(StandardCharsets.US_ASCII), 0, bytes, 0, 4);
        System.arraycopy("WEBP".getBytes(StandardCharsets.US_ASCII), 0, bytes, 8, 4);
        return bytes;
    }

    @Test
    void theTypeComesFromTheBytesNotTheName() throws Exception {
        assertThat(ImageFiles.mimeType(image(4, 4, "png"))).isEqualTo("image/png");
        assertThat(ImageFiles.mimeType(image(4, 4, "jpg"))).isEqualTo("image/jpeg");
        assertThat(ImageFiles.mimeType(image(4, 4, "gif"))).isEqualTo("image/gif");
        assertThat(ImageFiles.mimeType(webp())).isEqualTo("image/webp");

        assertThat(ImageFiles.mimeType("just a text file, long enough".getBytes(StandardCharsets.UTF_8))).isNull();
        assertThat(ImageFiles.mimeType("<svg xmlns='http://www.w3.org/2000/svg'/>".getBytes(StandardCharsets.UTF_8))).isNull();
        assertThat(ImageFiles.mimeType(new byte[]{1, 2, 3})).isNull();
        assertThat(ImageFiles.mimeType(null)).isNull();
    }

    @Test
    void somethingThatOnlyStartsLikeAnImageIsRefused() {
        byte[] fake = Arrays.copyOf(new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}, 64);

        assertThatThrownBy(() -> ImageFiles.check(fake, FILE)).isInstanceOf(VirtualFileException.class)
                .hasMessageContaining("can't be read as one");
        assertThatThrownBy(() -> ImageFiles.check("plain text, nothing else".getBytes(StandardCharsets.UTF_8), FILE))
                .isInstanceOf(VirtualFileException.class).hasMessageContaining("not an image");
    }

    @Test
    void aSmallImageGoesAsItIs() throws Exception {
        byte[] bytes = image(200, 100, "png");

        ImageFiles.Prepared prepared = ImageFiles.prepare(bytes, FILE);

        assertThat(prepared.data()).isEqualTo(bytes);
        assertThat(prepared.mimeType()).isEqualTo("image/png");
        assertThat(prepared.changed()).isFalse();
        assertThat(prepared.width()).isEqualTo(200);
        assertThat(prepared.height()).isEqualTo(100);
    }

    @Test
    void aLargeImageIsScaledDownKeepingItsProportions() throws Exception {
        ImageFiles.Prepared prepared = ImageFiles.prepare(image(3136, 1568, "png"), FILE);

        assertThat(prepared.changed()).isTrue();
        assertThat(prepared.width()).isEqualTo(ImageFiles.MAX_SIDE);
        assertThat(prepared.height()).isEqualTo(ImageFiles.MAX_SIDE / 2);
        BufferedImage sent = ImageIO.read(new ByteArrayInputStream(prepared.data()));
        assertThat(sent.getWidth()).isEqualTo(ImageFiles.MAX_SIDE);
        assertThat(sent.getHeight()).isEqualTo(ImageFiles.MAX_SIDE / 2);
    }

    @Test
    void aLargeJpegStaysAJpeg() throws Exception {
        ImageFiles.Prepared prepared = ImageFiles.prepare(image(3200, 1600, "jpg"), FILE);

        assertThat(prepared.changed()).isTrue();
        assertThat(prepared.mimeType()).isEqualTo("image/jpeg");
        assertThat(prepared.width()).isEqualTo(ImageFiles.MAX_SIDE);
    }

    @Test
    void aGifIsSentAsPng() throws Exception {
        ImageFiles.Prepared prepared = ImageFiles.prepare(image(50, 40, "gif"), FILE);

        assertThat(prepared.mimeType()).isEqualTo("image/png");
        assertThat(prepared.changed()).isTrue();
        assertThat(ImageFiles.mimeType(prepared.data())).isEqualTo("image/png");
    }

    @Test
    void aWebpIsSentAsItIsBecauseItIsNotDecoded() {
        byte[] bytes = webp();

        ImageFiles.Prepared prepared = ImageFiles.prepare(bytes, FILE);

        assertThat(prepared.data()).isEqualTo(bytes);
        assertThat(prepared.mimeType()).isEqualTo("image/webp");
        assertThat(prepared.width()).isZero();
    }

    @Test
    void aReferenceIsAnImageWithAPathAndNoBytes() throws Exception {
        LLMAttachment reference = ImageFiles.reference(FILE);

        assertThat(ImageFiles.isReference(reference)).isTrue();
        assertThat(reference.getName()).isEqualTo("/refs/pic.png");
        assertThat(ImageFiles.isReference(ImageFiles.prepare(image(10, 10, "png"), FILE).toAttachment("/refs/pic.png"))).isFalse();
        assertThat(ImageFiles.isReference(LLMAttachment.ofUrl(LLMAttachment.Kind.IMAGE, "image/png", "https://example.com/a.png"))).isFalse();
    }
}
