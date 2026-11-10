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

/**
 * A manuscript written in a word processor ({@link DocumentFormat}: Word, LibreOffice). The options are the number of words
 * of a part and the level of the headings that start a chapter, the cutting is {@link PartSplitter}.
 */
final class DocumentSource extends BookImportSource {

    private final VerticalLayout options;
    private final IntegerField wordsPerPart;
    private final ComboBox<Integer> chapterLevel;

    private TextDocument document;
    private String name = "";

    DocumentSource(Localization loc) {
        super(loc);
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
        return L.LABEL_IMPORT_FROM_DOCUMENT;
    }

    @Override
    L description() {
        return L.MSG_IMPORT_DOCUMENT;
    }

    @Override
    String[] fileTypes() {
        return DocumentFormat.fileTypes();
    }

    @Override
    Component options() {
        return options;
    }

    @Override
    void read(byte[] data, String fileName) {
        DocumentFormat format = DocumentFormat.of(fileName);
        if (format == null) {
            throw new IllegalArgumentException("Unknown document type: " + fileName);
        }
        TextDocument read = format.read(data);
        document = read;
        name = StringUtils.defaultIfBlank(read.title(), format.baseName(fileName).trim());
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
        return String.format(loc.getValue(L.MSG_INVALID_DOCUMENT), fileName);
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
