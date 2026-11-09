package com.github.enerccio.marginalia.extensions.translatorcommon.translate;

import com.github.enerccio.marginalia.extensions.translatorcommon.model.Span;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Reverse.Located;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The reverse: the conlang passages of the stored text turned back into their English source, nothing else touched.
 */
class ReverseTest {

    private static final String STORED = "Mara looked up. \"Da ja-se vek,\" she said, and \"Frag drak?\" came from the dark.";

    private static Located passage(String stored, String conlang, String source) {
        int start = stored.indexOf(conlang);
        return new Located(start, start + conlang.length(), new Span("lang", source, conlang));
    }

    @Test
    void aTextWithoutPassagesIsAsItIs() {
        Reverse.Result result = Reverse.toModel(STORED, List.of());

        assertEquals(STORED, result.text());
        assertFalse(result.model().changed());
        assertTrue(result.edited().isEmpty());
        assertTrue(result.ignored().isEmpty());
    }

    @Test
    void everyPassageIsTurnedBackAndTheQuotationMarksAndTheNarrationStay() {
        List<Located> passages = List.of(passage(STORED, "Da ja-se vek,", "I see the enemy,"), passage(STORED, "Frag drak?", "War?"));

        Reverse.Result result = Reverse.toModel(STORED, passages);

        assertEquals("Mara looked up. \"I see the enemy,\" she said, and \"War?\" came from the dark.", result.text());
        assertTrue(result.model().changed());
        assertEquals(STORED, result.model().stored());
    }

    @Test
    void thePassagesMayComeInAnyOrder() {
        List<Located> passages = List.of(passage(STORED, "Frag drak?", "War?"), passage(STORED, "Da ja-se vek,", "I see the enemy,"));

        assertEquals("Mara looked up. \"I see the enemy,\" she said, and \"War?\" came from the dark.", Reverse.sourceText(STORED, passages));
    }

    @Test
    void anEditedPassageIsLeftAsItIsAndFlagged() {
        Located made = passage(STORED, "Da ja-se vek,", "I see the enemy,");
        String edited = STORED.replace("Da ja-se vek,", "Da ja-se vek-is,");
        Located now = new Located(made.start(), made.end() + 3, made.span());
        Located other = passage(edited, "Frag drak?", "War?");

        Reverse.Result result = Reverse.toModel(edited, List.of(now, other));

        assertEquals("Mara looked up. \"Da ja-se vek-is,\" she said, and \"War?\" came from the dark.", result.text());
        assertEquals(List.of(now), result.edited());
    }

    @Test
    void aPassageThatOverlapsAnEarlierOneOrIsNotInTheTextOrHasNoSourceIsIgnored() {
        Located first = passage(STORED, "Da ja-se vek,", "I see the enemy,");
        Located overlap = new Located(first.start() + 3, first.end() + 4, new Span("lang", "x", "x"));
        Located outside = new Located(STORED.length() - 2, STORED.length() + 10, new Span("lang", "y", "y"));
        Located backwards = new Located(10, 5, new Span("lang", "z", "z"));
        Located noSource = passage(STORED, "Frag drak?", "");

        Reverse.Result result = Reverse.toModel(STORED, List.of(first, overlap, outside, backwards, noSource));

        assertEquals("Mara looked up. \"I see the enemy,\" she said, and \"Frag drak?\" came from the dark.", result.text());
        assertEquals(List.of(overlap, outside, backwards, noSource).size(), result.ignored().size());
        assertTrue(result.ignored().containsAll(List.of(overlap, outside, backwards, noSource)));
    }

    @Test
    void adjoiningPassagesAreBothTurnedBack() {
        // one quotation made two entries: the second begins where the first ends
        String stored = "\"Bef drak.Da ja-se.\"";
        Located a = passage(stored, "Bef drak.", "Fight.");
        Located b = passage(stored, "Da ja-se.", " I see.");

        assertEquals(a.end(), b.start());
        assertEquals("\"Fight. I see.\"", Reverse.sourceText(stored, List.of(a, b)));
    }

    @Test
    void theMapTakesAQuoteOfTheModelBackToTheStoredText() {
        List<Located> passages = List.of(passage(STORED, "Da ja-se vek,", "I see the enemy,"));
        Reverse.Result result = Reverse.toModel(STORED, passages);
        String model = result.text();

        int start = model.indexOf("I see the enemy,");
        int[] range = result.model().storedRange(start, start + "I see the enemy,".length());

        assertEquals("Da ja-se vek,", STORED.substring(range[0], range[1]));
        // a quote that touches only a part of the passage grows to all of it
        int[] grown = result.model().storedRange(start + 2, start + 5);
        assertEquals("Da ja-se vek,", STORED.substring(grown[0], grown[1]));
    }

    @Test
    void nullsAreEmpty() {
        assertEquals("", Reverse.sourceText(null, null));
        assertEquals("", Reverse.sourceText(null, List.of()));
    }

    @Test
    void anEditedSpeechWhoseSourceWasSetIsTurnedBackAgain() {
        // a native speaker fixed the translation; "Edit the source" gave the new English and took the text as it is now as the one it belongs to
        String edited = STORED.replace("Da ja-se vek,", "Da ja-se vek-is,");
        Located made = passage(edited, "Da ja-se vek-is,", "I see the enemy,");
        assertTrue(new Span("lang", "I see the enemy,", "Da ja-se vek,").isEdited("Da ja-se vek-is,"));
        Span span = new Span("lang", "I see the enemy,", "Da ja-se vek,");
        assertEquals("Mara looked up. \"Da ja-se vek-is,\" she said, and \"Frag drak?\" came from the dark.",
                Reverse.sourceText(edited, List.of(new Located(made.start(), made.end(), span))));

        span.setSource("I can see the enemy,");
        span.textIs("Da ja-se vek-is,");

        assertEquals("Mara looked up. \"I can see the enemy,\" she said, and \"Frag drak?\" came from the dark.",
                Reverse.sourceText(edited, List.of(new Located(made.start(), made.end(), span))));
    }
}
