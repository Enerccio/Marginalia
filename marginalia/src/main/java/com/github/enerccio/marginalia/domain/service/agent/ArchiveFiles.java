package com.github.enerccio.marginalia.domain.service.agent;

/**
 * Zip files, the third kind of file the workspace holds next to text and images. They are not read by the model, it
 * unpacks them with {@code unzip_file} ({@link ZipExtractor}); the workspace only keeps the bytes, so all it checks is
 * that they start like a zip file.
 */
public final class ArchiveFiles {

    private ArchiveFiles() {
    }

    /**
     * Whether the bytes start like a zip file: a local file header, or the end record of an empty archive.
     */
    public static boolean isZip(byte[] bytes) {
        return bytes != null && bytes.length >= 4 && bytes[0] == 'P' && bytes[1] == 'K'
                && ((bytes[2] == 3 && bytes[3] == 4) || (bytes[2] == 5 && bytes[3] == 6));
    }

}
