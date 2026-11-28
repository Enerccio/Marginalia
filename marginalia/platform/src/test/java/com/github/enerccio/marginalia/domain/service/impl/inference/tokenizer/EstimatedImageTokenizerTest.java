package com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer;

import com.github.enerccio.marginalia.domain.service.agent.ImageFiles;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The estimate of an image by its pixels, of the size it is sent at.
 */
class EstimatedImageTokenizerTest {

    private static byte[] image(int width, int height, String format) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), format, out);
        return out.toByteArray();
    }

    /** A RIFF/WEBP header with the first chunk and its bytes, padded to 40 bytes. */
    private static byte[] webp(String chunk, int... payload) {
        byte[] bytes = new byte[40];
        System.arraycopy("RIFF".getBytes(StandardCharsets.US_ASCII), 0, bytes, 0, 4);
        System.arraycopy("WEBP".getBytes(StandardCharsets.US_ASCII), 0, bytes, 8, 4);
        System.arraycopy(chunk.getBytes(StandardCharsets.US_ASCII), 0, bytes, 12, 4);
        for (int i = 0; i < payload.length; i++) {
            bytes[20 + i] = (byte) payload[i];
        }
        return bytes;
    }

    @Test
    void onePerSevenHundredFiftyPixels() {
        assertThat(EstimatedImageTokenizer.estimate(1000, 750)).isEqualTo(1000);
        assertThat(EstimatedImageTokenizer.estimate(1, 1)).isEqualTo(1);
    }

    @Test
    void aLargeImageIsCountedAtTheSizeItIsSent() {
        // scaled to 1568 x 784, as ImageFiles.prepare sends it
        assertThat(EstimatedImageTokenizer.estimate(4000, 2000)).isEqualTo(EstimatedImageTokenizer.estimate(ImageFiles.MAX_SIDE, ImageFiles.MAX_SIDE / 2));
    }

    @Test
    void thePixelsAreReadFromTheHeader() throws Exception {
        LLMAttachment png = LLMAttachment.of("image/png", image(750, 100, "png"));
        LLMAttachment jpeg = LLMAttachment.of("image/jpeg", image(750, 200, "jpg"));

        assertThat(EstimatedImageTokenizer.estimate(png)).isEqualTo(100);
        assertThat(EstimatedImageTokenizer.estimate(jpeg)).isEqualTo(200);
    }

    @Test
    void theSizeOfAWebpIsReadFromItsFirstChunk() {
        // lossy: frame tag (3 bytes), start code 9d 01 2a, 14 bits of width and height
        assertThat(ImageFiles.dimensions(webp("VP8 ", 0, 0, 0, 0x9D, 0x01, 0x2A, 0x20, 0x03, 0x58, 0x02)))
                .containsExactly(800, 600);
        // lossless: signature 2f, then width - 1 and height - 1 in 14 bits each
        int w = 1023, h = 511;
        long bits = w | ((long) h << 14);
        assertThat(ImageFiles.dimensions(webp("VP8L", 0x2F, (int) (bits & 0xFF), (int) (bits >> 8 & 0xFF),
                (int) (bits >> 16 & 0xFF), (int) (bits >> 24 & 0xFF)))).containsExactly(1024, 512);
        // extended: flags (4 bytes), then the canvas width - 1 and height - 1 in 24 bits each
        assertThat(ImageFiles.dimensions(webp("VP8X", 0, 0, 0, 0, 0xFF, 0x0F, 0x00, 0xFF, 0x07, 0x00)))
                .containsExactly(4096, 2048);
    }

    @Test
    void anImageOfUnknownSizeCountsTheDefault() {
        assertThat(EstimatedImageTokenizer.estimate(LLMAttachment.ofUrl(LLMAttachment.Kind.IMAGE, "image/png", "https://example.invalid/a.png")))
                .isEqualTo(EstimatedImageTokenizer.UNKNOWN_TOKENS);
        assertThat(EstimatedImageTokenizer.estimate(LLMAttachment.of("image/webp", webp("ABCD"))))
                .isEqualTo(EstimatedImageTokenizer.UNKNOWN_TOKENS);
    }

    @Test
    void whatIsNotAnImageCountsNothing() {
        assertThat(EstimatedImageTokenizer.estimate((LLMAttachment) null)).isZero();
        assertThat(EstimatedImageTokenizer.estimate(LLMAttachment.of(LLMAttachment.Kind.AUDIO, "audio/wav", "a.wav", new byte[16]))).isZero();
    }
}
