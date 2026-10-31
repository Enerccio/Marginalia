package com.github.enerccio.marginalia.extensions.betareaders.service;

import com.github.enerccio.marginalia.extensions.betareaders.model.Persona;
import com.github.enerccio.marginalia.ui.widgets.TabDockIcons;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * The icons of the personas: an uploaded image scaled down to a PNG that is stored (base64) with the persona, and for a persona
 * without one a circle with the first letters of its name, drawn as the tabs of a {@code TabDock} are.
 */
public final class PersonaIcons {

    /**
     * Icons are scaled down to fit into this many pixels (width and height).
     */
    public static final int MAX_ICON_SIZE = 200;

    /**
     * Refuse images with more pixels than this, a small file can decompress to a huge image.
     */
    private static final long MAX_SOURCE_PIXELS = 50_000_000L;

    private static final Pattern BASE64 = Pattern.compile("[A-Za-z0-9+/]*={0,2}");

    private PersonaIcons() {
    }

    /**
     * Scales the image down to fit into {@link #MAX_ICON_SIZE} x {@link #MAX_ICON_SIZE} (smaller images are kept as they are) and
     * returns it as base64 of a PNG.
     *
     * @throws IllegalArgumentException when the data is not an image that can be read
     */
    public static String createIcon(byte[] content) {
        BufferedImage source = decode(content);
        int width = source.getWidth();
        int height = source.getHeight();
        double scale = Math.min(1.0, Math.min((double) MAX_ICON_SIZE / width, (double) MAX_ICON_SIZE / height));
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));

        // halving first keeps the result sharp when a large photo is shrunk to an icon
        BufferedImage image = toArgb(source);
        while (image.getWidth() / 2 >= targetWidth && image.getHeight() / 2 >= targetHeight) {
            image = resize(image, image.getWidth() / 2, image.getHeight() / 2);
        }
        if (image.getWidth() != targetWidth || image.getHeight() != targetHeight) {
            image = resize(image, targetWidth, targetHeight);
        }

        try {
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            if (!ImageIO.write(image, "png", png)) {
                throw new IllegalArgumentException("Can't write the icon");
            }
            return Base64.getEncoder().encodeToString(png.toByteArray());
        } catch (java.io.IOException e) {
            throw new IllegalArgumentException("Can't write the icon", e);
        }
    }

    /**
     * The {@code src} of an image element for the icon of a persona: its image, or a circle with the first letters of its name when
     * it has none (or a stored one that is not base64). The color of the circle follows the uuid, so a persona keeps it.
     */
    public static String source(Persona persona) {
        String icon = persona.getIcon();
        if (icon != null && !icon.isBlank() && BASE64.matcher(icon).matches()) {
            return "data:image/png;base64," + icon;
        }
        return TabDockIcons.dataUri(letters(persona), persona.getUuid());
    }

    /**
     * The letters of the circle of a persona without an icon: the initials of the first two words of its name, or the first two
     * letters of a single word ({@code "Athena"} is {@code At}).
     */
    public static String letters(Persona persona) {
        return TabDockIcons.letters(persona.getName());
    }

    private static BufferedImage decode(byte[] content) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Not an image");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                if ((long) reader.getWidth(0) * reader.getHeight(0) > MAX_SOURCE_PIXELS) {
                    throw new IllegalArgumentException("The image has too many pixels");
                }
                return reader.read(0);
            } finally {
                reader.dispose();
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("The image can't be read", e);
        }
    }

    private static BufferedImage toArgb(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_INT_ARGB) {
            return image;
        }
        return resize(image, image.getWidth(), image.getHeight());
    }

    private static BufferedImage resize(BufferedImage image, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(image, 0, 0, width, height, null);
        } finally {
            g.dispose();
        }
        return result;
    }
}
