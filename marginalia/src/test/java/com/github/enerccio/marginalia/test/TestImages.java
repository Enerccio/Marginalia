package com.github.enerccio.marginalia.test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Images for the tests, made on the spot.
 */
public final class TestImages {

    private TestImages() {
    }

    /**
     * An image of the size with some pattern in it, so that it does not compress to nothing.
     *
     * @param format what ImageIO writes: "png", "jpg" or "gif"
     */
    public static byte[] image(int width, int height, String format) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x += 7) {
            for (int y = 0; y < height; y += 5) {
                image.setRGB(x, y, (x * 31 + y * 17) & 0xFFFFFF);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (!ImageIO.write(image, format, out)) {
            throw new IOException("No writer for " + format);
        }
        return out.toByteArray();
    }
}
