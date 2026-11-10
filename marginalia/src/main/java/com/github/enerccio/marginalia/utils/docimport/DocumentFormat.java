package com.github.enerccio.marginalia.utils.docimport;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A kind of file that holds a finished manuscript and can be read into the blocks of text of a {@link TextDocument}, which
 * {@link PartSplitter} cuts into the parts of a book. The import dialog takes the format from the name of the uploaded file,
 * so a new format is one more constant here with its reader. A folder (an Obsidian vault, a Scrivener
 * project) is not a file with an extension of its own: those are read from a zip by {@link ObsidianReader} and {@link ScrivenerReader}
 * ({@link #vault(byte[])}, {@link #scrivener(byte[])}).
 */
public enum DocumentFormat {

    DOCX(DocxReader::read, List.of(".docx"), "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    ODT(OdtReader::read, List.of(".odt"), "application/vnd.oasis.opendocument.text"),
    RTF(RtfReader::read, List.of(".rtf"), "application/rtf", "text/rtf"),
    MARKDOWN(MarkdownReader::read, List.of(".md", ".markdown"), "text/markdown"),
    TEXT(PlainTextReader::read, List.of(".txt", ".text"), "text/plain"),
    EPUB(EpubReader::read, List.of(".epub"), "application/epub+zip");

    private final Function<byte[], TextDocument> reader;
    private final List<String> extensions;
    private final List<String> mimeTypes;

    DocumentFormat(Function<byte[], TextDocument> reader, List<String> extensions, String... mimeTypes) {
        this.reader = reader;
        this.extensions = extensions;
        this.mimeTypes = List.of(mimeTypes);
    }

    /**
     * @return the format of the file name (by its extension), null when no format reads it
     */
    public static DocumentFormat of(String fileName) {
        if (fileName == null) {
            return null;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(format -> format.extensionOf(lower) != null).findFirst().orElse(null);
    }

    /**
     * @return the extensions and media types for the accepted types of an upload
     */
    public static String[] fileTypes() {
        return Arrays.stream(values()).flatMap(format -> Stream.concat(format.extensions.stream(), format.mimeTypes.stream())).toArray(String[]::new);
    }

    /**
     * @return the file name without the extension of the format
     */
    public String baseName(String fileName) {
        String extension = extensionOf(fileName.toLowerCase(Locale.ROOT));
        return extension == null ? fileName : fileName.substring(0, fileName.length() - extension.length());
    }

    private String extensionOf(String lowerCaseName) {
        return extensions.stream().filter(lowerCaseName::endsWith).findFirst().orElse(null);
    }

    /**
     * @param zip a zip of an Obsidian vault
     * @throws IllegalArgumentException when it is not one or has no notes with text
     */
    public static TextDocument vault(byte[] zip) {
        return checked(zip, ObsidianReader::read);
    }

    /**
     * @param zip a zip of a Scrivener project ({@code .scriv})
     * @throws IllegalArgumentException when it is not one or its manuscript has no text
     */
    public static TextDocument scrivener(byte[] zip) {
        return checked(zip, ScrivenerReader::read);
    }

    private static TextDocument checked(byte[] data, Function<byte[], TextDocument> reader) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Document cannot be empty");
        }
        TextDocument document = reader.apply(data);
        if (document.blocks().isEmpty()) {
            throw new IllegalArgumentException("Document has no text");
        }
        return document;
    }

    /**
     * @throws IllegalArgumentException when the data is not a document of the format or has no text
     */
    public TextDocument read(byte[] data) {
        return checked(data, reader);
    }
}
