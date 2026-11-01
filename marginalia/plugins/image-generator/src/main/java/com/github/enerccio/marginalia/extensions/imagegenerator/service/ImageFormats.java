package com.github.enerccio.marginalia.extensions.imagegenerator.service;

/** Tells the format of an image from its first bytes (the servers don't always say). */
public final class ImageFormats {

    private ImageFormats() {
    }

    /** {@code png}, {@code jpg}, {@code webp} or {@code gif}; null when the bytes are none of them. */
    public static String extension(byte[] data) {
        if (data == null || data.length < 12) {
            return null;
        }
        if ((data[0] & 0xFF) == 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
            return "png";
        }
        if ((data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8) {
            return "jpg";
        }
        if (data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P') {
            return "webp";
        }
        if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F') {
            return "gif";
        }
        return null;
    }

    public static String mimeType(String extension) {
        return switch (extension) {
            case "png" -> "image/png";
            case "jpg" -> "image/jpeg";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }
}
