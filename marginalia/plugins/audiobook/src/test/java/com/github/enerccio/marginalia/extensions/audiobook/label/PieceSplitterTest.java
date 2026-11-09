package com.github.enerccio.marginalia.extensions.audiobook.label;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PieceSplitterTest {

    private static String joined(List<Piece> pieces) {
        return String.join(" ", pieces.stream().map(Piece::text).toList());
    }

    @Test
    void narrationAndSpeechAreSeparatedWithoutTheQuotationMarks() {
        List<Piece> pieces = PieceSplitter.split("Marta pushed the door. \"Nobody has been here,\" she said. \"Years.\"");

        assertEquals(List.of("Marta pushed the door.", "Nobody has been here,", "she said.", "Years."), pieces.stream().map(Piece::text).toList());
        assertEquals(List.of(Piece.Kind.NARRATION, Piece.Kind.SPEECH, Piece.Kind.NARRATION, Piece.Kind.SPEECH), pieces.stream().map(Piece::kind).toList());
        assertEquals(List.of(0, 1, 2, 3), pieces.stream().map(Piece::id).toList());
    }

    @Test
    void japaneseCornerBracketsAreSpeechToo() {
        List<Piece> pieces = PieceSplitter.split("マルタは扉を押した。「誰もいないわ」と彼女は言った。『古い歌よ』");

        assertEquals(List.of("マルタは扉を押した。", "誰もいないわ", "と彼女は言った。", "古い歌よ"), pieces.stream().map(Piece::text).toList());
        assertEquals(List.of(Piece.Kind.NARRATION, Piece.Kind.SPEECH, Piece.Kind.NARRATION, Piece.Kind.SPEECH), pieces.stream().map(Piece::kind).toList());
    }

    @Test
    void aQuotationInsideACornerBracketStaysInTheSpeech() {
        List<Piece> pieces = PieceSplitter.split("「彼は『行く』と言った」と彼女は言った。");

        assertEquals(List.of("彼は『行く』と言った", "と彼女は言った。"), pieces.stream().map(Piece::text).toList());
        // Japanese has no spaces between words: the pieces are compared without any
        assertEquals(PieceSplitter.normalize("「彼は『行く』と言った」と彼女は言った。"), PieceSplitter.normalize(joined(pieces)).replace(" ", ""));
    }

    @Test
    void curlyQuotesWorkToo() {
        List<Piece> pieces = PieceSplitter.split("He nodded. “Cold wall,” he said.");

        assertEquals(List.of("He nodded.", "Cold wall,", "he said."), pieces.stream().map(Piece::text).toList());
        assertEquals(Piece.Kind.SPEECH, pieces.get(1).kind());
    }

    @Test
    void paragraphsAreNumberedAndAHeadingIsNarrationWithoutItsMarks() {
        List<Piece> pieces = PieceSplitter.split("# Chapter One\n\nFirst paragraph\nover two lines.\n\n\nSecond paragraph.");

        assertEquals(3, pieces.size());
        assertEquals("Chapter One", pieces.get(0).text());
        assertTrue(pieces.get(0).heading());
        assertEquals(Piece.Kind.NARRATION, pieces.get(0).kind());
        assertEquals("First paragraph over two lines.", pieces.get(1).text());
        assertFalse(pieces.get(1).heading());
        assertEquals(List.of(0, 1, 2), pieces.stream().map(Piece::paragraph).toList());
    }

    @Test
    void anUnclosedQuotationMarkStaysInTheNarration() {
        List<Piece> pieces = PieceSplitter.split("She said \"and then it was gone.");

        assertEquals(1, pieces.size());
        assertEquals(Piece.Kind.NARRATION, pieces.getFirst().kind());
        assertTrue(pieces.getFirst().text().contains("\"and then"));
    }

    @Test
    void speechAcrossAParagraphBreakIsNotJoined() {
        List<Piece> pieces = PieceSplitter.split("\"One.\n\nTwo.\"");

        // each paragraph is read alone: the quotation mark of the first has no partner in it
        assertTrue(pieces.stream().allMatch(p -> p.kind() == Piece.Kind.NARRATION));
        assertEquals(PieceSplitter.normalize("\"One.\n\nTwo.\""), PieceSplitter.normalize(joined(pieces)));
    }

    @Test
    void theWordsAreNeverChanged() {
        String text = "# The Arch\n\nMarta coughed at the dust. \"Nobody,\" she said, \"has been down here in years.\" Jonas grinned.\n\n"
                + "\"Then you won't mind,\" he whispered. \"If I jump out at you!\"\n\n\"Not funny!\" Marta shouted.";

        List<Piece> pieces = PieceSplitter.split(text);

        assertEquals(PieceSplitter.normalize(text), PieceSplitter.normalize(joined(pieces)));
        assertEquals(5, pieces.stream().filter(p -> p.kind() == Piece.Kind.SPEECH).count(), "Nobody, / has been down here in years. / Then you won't mind, / If I jump out at you! / Not funny!");
    }

    @Test
    void emptyTextHasNoPieces() {
        assertTrue(PieceSplitter.split(null).isEmpty());
        assertTrue(PieceSplitter.split("  \n\n ").isEmpty());
        assertTrue(PieceSplitter.split("#\n\n#  ").isEmpty());
    }

    @Test
    void normalizeIgnoresQuotesHeadingMarksTagsAndWhiteSpace() {
        assertEquals("Hello there friend", PieceSplitter.normalize("# \"Hello [laugh] there\"   friend"));
        assertEquals("", PieceSplitter.normalize(null));
        // a bracket that is not an allowed tag is a word of the book
        assertEquals("a [secret] word", PieceSplitter.normalize("a [secret] word"));
    }
}
