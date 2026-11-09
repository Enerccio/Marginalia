package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.ExporterService;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import com.github.enerccio.marginalia.domain.service.ExporterService.Exporter;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.ProgressBarDialog;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The PDF: footnotes at the bottom of the page of their mark, in a smaller type under a rule, and Japanese text in a font that has it.
 */
class PdfExporterNotesTest extends MarginaliaTestBase {

    @Autowired
    private ExporterService exporterService;

    private PDDocument export(String... texts) throws Exception {
        Exporter exporter = exporterService.getExporters().stream().filter(e -> e.getFileExtension().equals("pdf")).findFirst().orElseThrow();
        List<ChatMessage> parts = new ArrayList<>();
        for (String text : texts) {
            ChatMessage message = new ChatMessage();
            message.setResponse(text);
            parts.add(message);
        }
        return Loader.loadPDF(exporter.export(new ExportOptions("Book", "Author", false, "en"), parts, new ProgressBarDialog(false)));
    }

    /** One piece of text on a page: what it says, where (from the top of the page) and how large. */
    private record Piece(String text, float top, float size) {
    }

    private static List<Piece> pieces(PDDocument document, int page) throws IOException {
        List<Piece> result = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void writeString(String text, List<TextPosition> positions) throws IOException {
                if (!positions.isEmpty()) {
                    result.add(new Piece(text, positions.getFirst().getYDirAdj(), positions.getFirst().getFontSizeInPt()));
                }
                super.writeString(text, positions);
            }
        };
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        stripper.setSortByPosition(true);
        stripper.getText(document);
        return result;
    }

    /** Paragraphs of a few lines each, every one with a note: they run over several pages. */
    private static String longText(int paragraphs) {
        StringBuilder text = new StringBuilder();
        for (int i = 1; i <= paragraphs; i++) {
            text.append("Paragraph ").append(i).append(" tells how the water rose against the weir and how the clerk counted the sacks twice in the dark, ")
                    .append("until the count agreed with nothing he had written down earlier that day.[^").append(i).append("]\n\n");
        }
        for (int i = 1; i <= paragraphs; i++) {
            text.append("[^").append(i).append("]: NOTETEXT").append(i).append(" is what the speaker said, in English, before it was translated.\n\n");
        }
        return text.toString();
    }

    @Test
    void aNoteIsOnThePageOfItsMarkBelowTheTextInASmallerType() throws Exception {
        try (PDDocument document = export(longText(24))) {
            assertThat(document.getNumberOfPages()).isGreaterThan(2);
            int notesSeen = 0;
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                List<Piece> pieces = pieces(document, page);
                List<Piece> body = pieces.stream().filter(p -> p.text().contains("Paragraph") || p.text().contains("count agreed")).toList();
                List<Piece> notes = pieces.stream().filter(p -> p.text().contains("NOTETEXT")).toList();
                if (notes.isEmpty()) {
                    continue;
                }
                notesSeen += notes.size();
                // every note on the page belongs to a paragraph on the page: the paragraph number is the number of the note
                for (Piece note : notes) {
                    String number = note.text().replaceAll(".*NOTETEXT(\\d+).*", "$1");
                    assertThat(body.stream().anyMatch(p -> p.text().contains("Paragraph " + number + " "))).as("the paragraph %s is on page %d with its note", number, page).isTrue();
                }
                float lowestBody = (float) body.stream().mapToDouble(Piece::top).max().orElseThrow();
                float highestNote = (float) notes.stream().mapToDouble(Piece::top).min().orElseThrow();
                assertThat(highestNote).as("notes are below the text of page %d", page).isGreaterThan(lowestBody);
                assertThat(notes.getFirst().size()).as("notes are smaller than the text").isLessThan(body.getFirst().size());
            }
            // all the notes are somewhere, none was lost and none was left for the end of the book
            assertThat(notesSeen).isEqualTo(24);
        }
    }

    @Test
    void aBookWithoutNotesIsLaidOutAsBefore() throws Exception {
        try (PDDocument document = export("A plain text.\n\nAnd another one.")) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);
            assertThat(new PDFTextStripper().getText(document)).contains("A plain text.").contains("And another one.");
        }
    }

    @Test
    void japaneseTextIsDrawn() throws Exception {
        try (PDDocument document = export("Mizuki said, 「家に帰りたい」, and then ミズキ added 漢字 in Czech: příliš žluťoučký kůň.")) {
            // the line breaks of the page are not the text
            String text = new PDFTextStripper().getText(document).replaceAll("\\s+", "");
            assertThat(text).contains("家に帰りたい").contains("ミズキ").contains("漢字").contains("příliš žluťoučký kůň".replace(" ", ""));
        }
    }

    @Test
    void aNoteInJapaneseIsDrawnToo() throws Exception {
        try (PDDocument document = export("She spoke softly.[^1]\n\n[^1]: ミズキ: 家に帰りたい。")) {
            assertThat(new PDFTextStripper().getText(document)).contains("ミズキ: 家に帰りたい。");
        }
    }
}
