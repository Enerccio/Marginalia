package com.github.enerccio.marginalia.ui.dialogs.bookimport;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.docimport.DocumentFormat;
import com.github.enerccio.marginalia.utils.docimport.PartSplitter;
import com.github.enerccio.marginalia.utils.docimport.TextDocument;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

/**
 * A finished manuscript in a file ({@link DocumentFormat}: Word, LibreOffice, RTF, Markdown, plain text, EPUB), or in the zip of
 * a folder that holds one (an Obsidian vault, a Scrivener project): the same options for all, the number of words
 * of a part and the level of the headings that start a chapter, the cutting is {@link PartSplitter}.
 */
final class DocumentSource extends BookImportSource {

    private static final String[] ZIP_TYPES = {"application/zip", "application/x-zip-compressed", ".zip"};

    /**
     * What differs between the sources of this kind: the texts, the uploads they take and how an upload is read.
     *
     * @param baseName the name of the book from the name of the file
     */
    private record Kind(L label, L description, L invalid, String[] fileTypes, Reader reader, UnaryOperator<String> baseName) {
    }

    @FunctionalInterface
    private interface Reader {
        TextDocument read(byte[] data, String fileName);
    }

    private final Kind kind;

    private final VerticalLayout options;
    private final IntegerField wordsPerPart;
    private final ComboBox<Integer> chapterLevel;

    private TextDocument document;
    private String name = "";

    /**
     * A file of a {@link DocumentFormat}.
     */
    DocumentSource(Localization loc) {
        this(loc, new Kind(L.LABEL_IMPORT_FROM_DOCUMENT, L.MSG_IMPORT_DOCUMENT, L.MSG_INVALID_DOCUMENT, DocumentFormat.fileTypes(), (data, fileName) -> {
            DocumentFormat format = DocumentFormat.of(fileName);
            if (format == null) {
                throw new IllegalArgumentException("Unknown document type: " + fileName);
            }
            return format.read(data);
        }, fileName -> {
            DocumentFormat format = DocumentFormat.of(fileName);
            return format == null ? fileName : format.baseName(fileName);
        }));
    }

    /**
     * @return the source for the zip of an Obsidian vault
     */
    static DocumentSource obsidian(Localization loc) {
        return new DocumentSource(loc, new Kind(L.LABEL_IMPORT_FROM_OBSIDIAN, L.MSG_IMPORT_OBSIDIAN, L.MSG_INVALID_OBSIDIAN, ZIP_TYPES,
                (data, fileName) -> DocumentFormat.vault(data), DocumentSource::withoutZip));
    }

    /**
     * @return the source for the zip of a Scrivener project
     */
    static DocumentSource scrivener(Localization loc) {
        return new DocumentSource(loc, new Kind(L.LABEL_IMPORT_FROM_SCRIVENER, L.MSG_IMPORT_SCRIVENER, L.MSG_INVALID_SCRIVENER, ZIP_TYPES,
                (data, fileName) -> DocumentFormat.scrivener(data), fileName -> {
            String name = withoutZip(fileName);
            return name.toLowerCase(Locale.ROOT).endsWith(".scriv") ? name.substring(0, name.length() - ".scriv".length()) : name;
        }));
    }

    private static String withoutZip(String fileName) {
        return fileName.toLowerCase(Locale.ROOT).endsWith(".zip") ? fileName.substring(0, fileName.length() - ".zip".length()) : fileName;
    }

    private DocumentSource(Localization loc, Kind kind) {
        super(loc);
        this.kind = kind;
        wordsPerPart = new IntegerField(loc.getValue(L.LABEL_WORDS_PER_PART));
        wordsPerPart.setMin(PartSplitter.MIN_WORDS_PER_PART);
        wordsPerPart.setMax(PartSplitter.MAX_WORDS_PER_PART);
        wordsPerPart.setStep(100);
        wordsPerPart.setStepButtonsVisible(true);
        wordsPerPart.setValue(PartSplitter.DEFAULT_WORDS_PER_PART);
        wordsPerPart.setWidthFull();
        wordsPerPart.setHelperText(loc.getValue(L.HELP_WORDS_PER_PART));
        wordsPerPart.addValueChangeListener(_ -> changed());

        chapterLevel = new ComboBox<>(loc.getValue(L.LABEL_CHAPTER_HEADING_LEVEL));
        chapterLevel.setAllowCustomValue(false);
        chapterLevel.setWidthFull();
        chapterLevel.addValueChangeListener(_ -> changed());
        chapterLevel.setHelperText(loc.getValue(L.HELP_CHAPTER_HEADING_LEVEL));
        showChapterLevel(List.of());

        options = new VerticalLayout(wordsPerPart, chapterLevel);
        options.setPadding(false);
    }

    @Override
    L label() {
        return kind.label;
    }

    @Override
    L description() {
        return kind.description;
    }

    @Override
    String[] fileTypes() {
        return kind.fileTypes;
    }

    @Override
    Component options() {
        return options;
    }

    @Override
    void read(byte[] data, String fileName) {
        TextDocument read = kind.reader.read(data, fileName);
        document = read;
        name = StringUtils.defaultIfBlank(read.title(), kind.baseName.apply(fileName).trim());
        showChapterLevel(read.headingLevels());
    }

    // the combo box offers the levels the document has, the shallowest one is chosen; without headings it is hidden
    private void showChapterLevel(List<Integer> levels) {
        chapterLevel.setItems(levels);
        chapterLevel.setValue(levels.isEmpty() ? null : levels.getFirst());
        chapterLevel.setVisible(!levels.isEmpty());
    }

    @Override
    String invalid(String fileName) {
        return String.format(loc.getValue(kind.invalid), fileName);
    }

    @Override
    void reset() {
        document = null;
        name = "";
        showChapterLevel(List.of());
    }

    private PartSplitter.Result split() {
        Integer words = wordsPerPart.getValue();
        if (document == null || words == null || words < PartSplitter.MIN_WORDS_PER_PART || words > PartSplitter.MAX_WORDS_PER_PART) {
            return null;
        }
        // without headings there is no chapter, the level above all of them starts none
        Integer level = chapterLevel.getValue();
        return PartSplitter.split(document, level == null ? 0 : level, words);
    }

    @Override
    String status() {
        PartSplitter.Result result = split();
        return result == null ? "" : String.format(loc.getValue(L.MSG_DOCUMENT_READY), result.chapters(), result.words(), result.parts().size());
    }

    @Override
    boolean ready() {
        return split() != null;
    }

    @Override
    String suggestedName() {
        return name;
    }

    @Override
    BookImportDialog.Run prepare() {
        PartSplitter.Result result = split();
        if (result == null) {
            throw new IllegalArgumentException(loc.getValue(L.LABEL_WORDS_PER_PART));
        }
        List<String> parts = result.parts();
        return (manuscript, afterwards) -> new DocumentImportRunner().run(manuscript, parts, RevisionDetail.DOCUMENT_IMPORT, afterwards);
    }
}
