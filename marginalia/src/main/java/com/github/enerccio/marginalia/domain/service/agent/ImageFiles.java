package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * The images of a workspace: which bytes are an image (by what they start with, never by the name), and what a model
 * is shown of one. A workspace file is an image when it starts like a PNG, a JPEG, a GIF or a WebP; SVG is not one,
 * it is text and a model can't look at it.
 * <p>
 * What the model is sent is {@link #prepare prepared}: an image that is small enough goes as it is, a larger one is
 * scaled down (providers cut the size themselves and charge for the pixels) in the format it has, and a GIF is sent as
 * its first frame in PNG, since the providers take no animation.
 */
public final class ImageFiles {

    /** The longest side of an image sent to a model, in pixels. */
    public static final int MAX_SIDE = 1568;
    /** The most pixels an image of the workspace may have; a bigger one (a decompression bomb) is not stored. */
    public static final long MAX_PIXELS = 50_000_000L;
    /** A file that is not scaled down when its sides are within {@link #MAX_SIDE}, in bytes. */
    private static final int KEEP_BYTES = 1_500_000;

    /**
     * What {@link #prepare} made.
     *
     * @param data     the bytes to send
     * @param mimeType their type
     * @param width    the size of the image sent, 0 when not known (a WebP is not decoded)
     * @param height   see width
     * @param changed  whether the bytes are not the ones of the file (scaled down or converted)
     */
    public record Prepared(byte[] data, String mimeType, int width, int height, boolean changed) {

        /** The image as an attachment of a message, named by the path of the file it is from. */
        public LLMAttachment toAttachment(String name) {
            return LLMAttachment.of(LLMAttachment.Kind.IMAGE, mimeType, name, data);
        }
    }

    private ImageFiles() {
    }

    /**
     * The type of the image the bytes are, null when they are not one the workspace takes.
     */
    public static String mimeType(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return null;
        }
        if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G' && bytes[4] == 0x0D
                && bytes[5] == 0x0A && bytes[6] == 0x1A && bytes[7] == 0x0A) {
            return "image/png";
        }
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == '8' && (bytes[4] == '7' || bytes[4] == '9')
                && bytes[5] == 'a') {
            return "image/gif";
        }
        if (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F' && bytes[8] == 'W' && bytes[9] == 'E'
                && bytes[10] == 'B' && bytes[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    public static boolean isImage(byte[] bytes) {
        return mimeType(bytes) != null;
    }

    /**
     * The attachment that stands for an image file of the workspace in a stored message: the path and no bytes, so the
     * transcript stays small and the image is read, scaled down, when a request is made ({@link #isReference}).
     */
    public static LLMAttachment reference(VirtualPath file) {
        return LLMAttachment.of(LLMAttachment.Kind.IMAGE, null, file.toString(), null);
    }

    /** Whether the attachment is a {@link #reference}: an image that has a path and neither bytes nor a link. */
    public static boolean isReference(LLMAttachment attachment) {
        return attachment.getKind() == LLMAttachment.Kind.IMAGE && attachment.getData() == null && attachment.getUrl() == null
                && attachment.getName() != null && attachment.getName().startsWith("/");
    }

    /**
     * Checks that the bytes can be kept as an image of the workspace: a known type, and for the types that can be
     * decoded here a size in pixels that is not absurd. Nothing is decoded.
     *
     * @return the type
     * @throws VirtualFileException with the reason for the model and the user
     */
    public static String check(byte[] bytes, VirtualPath file) {
        String mime = mimeType(bytes);
        if (mime == null) {
            throw new VirtualFileException(file + " is not an image the workspace takes (PNG, JPEG, GIF or WebP)");
        }
        int[] size = size(bytes);
        if (size == null && !"image/webp".equals(mime)) {
            throw new VirtualFileException(file + " starts like an image but can't be read as one");
        }
        if (size != null && (size[0] < 1 || size[1] < 1 || (long) size[0] * size[1] > MAX_PIXELS)) {
            throw new VirtualFileException("The image " + file + " has " + size[0] + " x " + size[1]
                    + " pixels, the most it may have is " + MAX_PIXELS + " pixels in all");
        }
        return mime;
    }

    /**
     * What a model is sent of the image, see the class description.
     *
     * @param file the name for the message
     * @throws VirtualFileException when the bytes are not an image or can't be decoded
     */
    public static Prepared prepare(byte[] bytes, VirtualPath file) {
        String mime = check(bytes, file);
        int[] size = size(bytes);
        if (size == null) {
            // not decoded here (WebP): as it is
            return new Prepared(bytes, mime, 0, 0, false);
        }
        int longest = Math.max(size[0], size[1]);
        boolean gif = "image/gif".equals(mime);
        if (longest <= MAX_SIDE && bytes.length <= KEEP_BYTES && !gif) {
            return new Prepared(bytes, mime, size[0], size[1], false);
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new VirtualFileException("The image " + file + " can't be read");
            }
            double scale = longest > MAX_SIDE ? (double) MAX_SIDE / longest : 1;
            int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
            boolean alpha = image.getColorModel().hasAlpha();
            BufferedImage scaled = new BufferedImage(width, height, alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = scaled.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.drawImage(image, 0, 0, width, height, null);
            } finally {
                graphics.dispose();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // line art and text (maps, screenshots) stay lossless, only a photograph is made smaller as a JPEG
            String format = alpha || gif || "image/png".equals(mime) ? "png" : "jpg";
            if (!ImageIO.write(scaled, format, out)) {
                throw new VirtualFileException("The image " + file + " can't be converted");
            }
            byte[] result = out.toByteArray();
            // an image that got no smaller and needed no change is kept as it was
            if (scale == 1 && !gif && result.length >= bytes.length) {
                return new Prepared(bytes, mime, size[0], size[1], false);
            }
            return new Prepared(result, "png".equals(format) ? "image/png" : "image/jpeg", width, height, true);
        } catch (IOException e) {
            throw new VirtualFileException("The image " + file + " can't be read", e);
        }
    }

    /**
     * The size of an image in pixels from its header, without decoding it; null when this runtime can't tell (WebP, which
     * it does not read, or a file that starts like an image and is not one).
     */
    private static int[] size(byte[] bytes) {
        if ("image/webp".equals(mimeType(bytes))) {
            return null;
        }
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (in == null) {
                return null;
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (!readers.hasNext()) {
                return null;
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(in, true, true);
                return new int[]{reader.getWidth(0), reader.getHeight(0)};
            } finally {
                reader.dispose();
            }
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

}
