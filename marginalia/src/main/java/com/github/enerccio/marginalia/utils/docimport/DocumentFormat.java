package com.github.enerccio.marginalia.utils.docimport;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A kind of file that holds a finished manuscript and can be read into the blocks of text of a {@link TextDocument}, which
 * {@link PartSplitter} cuts into the parts of a book. The import dialog takes the format from the name of the uploaded file,
 * so a new format (Markdown, plain text, EPUB) is one more constant here with its reader.
 */
public enum DocumentFormat {

    DOCX(DocxReader::read, ".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    ODT(OdtReader::read, ".odt", "application/vnd.oasis.opendocument.text");

    private final Function<byte[], TextDocument> reader;
    private final String extension;
    private final String mimeType;

    DocumentFormat(Function<byte[], TextDocument> reader, String extension, String mimeType) {
        this.reader = reader;
        this.extension = extension;
        this.mimeType = mimeType;
    }

    /**
     * @return the format of the file name (by its extension), null when no format reads it
     */
    public static DocumentFormat of(String fileName) {
        if (fileName == null) {
            return null;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(format -> lower.endsWith(format.extension)).findFirst().orElse(null);
    }

    /**
     * @return the extensions and media types for the accepted types of an upload
     */
    public static String[] fileTypes() {
        return Arrays.stream(values()).flatMap(format -> Stream.of(format.extension, format.mimeType)).toArray(String[]::new);
    }

    /**
     * @return the file name without the extension of the format
     */
    public String baseName(String fileName) {
        return fileName.toLowerCase(Locale.ROOT).endsWith(extension) ? fileName.substring(0, fileName.length() - extension.length()) : fileName;
    }

    /**
     * @throws IllegalArgumentException when the data is not a document of the format or has no text
     */
    public TextDocument read(byte[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Document cannot be empty");
        }
        TextDocument document = reader.apply(data);
        if (document.blocks().isEmpty()) {
            throw new IllegalArgumentException("Document has no text");
        }
        return document;
    }
}
