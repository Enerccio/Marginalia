package com.github.enerccio.marginalia.extensions;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.extensions.ExportTextRegistry.Edit;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What the export asks of the extensions: edits are ranges of the stored text, they are put together once, an edit that overlaps an earlier one
 * is dropped, and the footnotes of the whole export are numbered in one sequence.
 */
class ExportTextRegistryTest {

    private static final ExportTextRegistry.Session SESSION = new ExportTextRegistry.Session() {
        @Override
        public Object source() {
            return null;
        }

        @Override
        public com.github.enerccio.marginalia.domain.model.impl.Manuscript manuscript() {
            return null;
        }
    };

    private static ChatMessage part(String text) {
        ChatMessage part = new ChatMessage();
        part.setResponse(text);
        return part;
    }

    private static ExportTextRegistry.Contributor edits(Edit... edits) {
        return (part, session) -> List.of(edits);
    }

    @Test
    void aReplacementKeepsTheTextAroundIt() {
        ExportTextRegistry.Rendered rendered = ExportTextRegistry.render("Mara said \"Da vek\" and left.", List.of(Edit.replace(11, 17, "I see")), 1);

        assertEquals("Mara said \"I see\" and left.", rendered.text());
        assertEquals(1, rendered.next());
    }

    @Test
    void aNoteGetsAMarkBehindTheClosingQuoteAndADefinitionAfterThePart() {
        String stored = "She said \"Da vek\" and left.";
        int start = stored.indexOf("Da vek");
        ExportTextRegistry.Rendered rendered = ExportTextRegistry.render(stored,
                List.of(Edit.withNote(start, start + 6, "Da vek", "Mara: \"I see.\"\nSecond line", start + 7)), 4);

        assertEquals("She said \"Da vek\"[^4] and left.\n\n[^4]: Mara: \"I see.\" Second line", rendered.text(),
                "the mark is behind the closing quote and the note is on one line");
        assertEquals(5, rendered.next());
    }

    @Test
    void anEditThatOverlapsAnEarlierOneIsDroppedAndOneOutsideTheTextToo() {
        ExportTextRegistry.Rendered rendered = ExportTextRegistry.render("0123456789",
                List.of(Edit.replace(2, 6, "A"), Edit.replace(4, 8, "B"), Edit.replace(8, 99, "C"), Edit.replace(6, 8, "D")), 1);

        assertEquals("01A" + "D" + "89", rendered.text());
    }

    @Test
    void theFirstOfTwoEditsThatStartTogetherWins() {
        ExportTextRegistry.Rendered rendered = ExportTextRegistry.render("abcdef", List.of(Edit.replace(1, 3, "X"), Edit.replace(1, 4, "Y")), 1);

        assertEquals("aXdef", rendered.text());
    }

    @Test
    void noEditMeansNothingToRender() {
        assertNull(ExportTextRegistry.render("text", List.of(), 1));
        assertNull(ExportTextRegistry.render("text", List.of(Edit.replace(0, 9, "x")), 1), "an edit outside the text is ignored");
    }

    @Test
    void theContributorsAreMergedOnTheStoredTextAndTheFootnotesAreNumberedAcrossTheParts() {
        ExportTextRegistry registry = new ExportTextRegistry();
        // two extensions, each knows only the stored text: one translates the first word, the other the last
        registry.register("a", (part, session) -> part.getResponse().startsWith("one") ? List.of(Edit.withNote(0, 3, "1", "first")) : List.of());
        registry.register("b", (part, session) -> {
            String text = part.getResponse();
            return text.endsWith("two") ? List.of(Edit.withNote(text.length() - 3, text.length(), "2", "second")) : List.of();
        });

        ChatMessage first = part("one and two");
        ChatMessage second = part("only two");
        ChatMessage plain = part("nothing");
        List<ChatMessage> result = registry.apply(List.of(first, second, plain), SESSION);

        assertEquals("1[^1] and 2[^2]\n\n[^1]: first\n\n[^2]: second", result.get(0).getResponse());
        assertEquals("only 2[^3]\n\n[^3]: second", result.get(1).getResponse());
        assertSame(plain, result.get(2), "a part with no edit is the part itself");
        assertEquals("one and two", first.getResponse(), "the stored part is not changed");
    }

    @Test
    void aContributorThatFailsIsLeftOutAndTheOthersWork() {
        ExportTextRegistry registry = new ExportTextRegistry();
        registry.register("bad", (part, session) -> {
            throw new IllegalStateException("broken");
        });
        registry.register("good", edits(Edit.replace(0, 1, "X")));

        assertEquals("Xbc", registry.apply(List.of(part("abc")), SESSION).getFirst().getResponse());
    }

    @Test
    void preparedIsCalledOnceWithAllThePartsAndAnOwnerCanBeRemoved() {
        ExportTextRegistry registry = new ExportTextRegistry();
        List<Integer> prepared = new ArrayList<>();
        registry.register("owner", new ExportTextRegistry.Contributor() {
            @Override
            public void prepare(java.util.Collection<ChatMessage> parts, ExportTextRegistry.Session session) {
                prepared.add(parts.size());
            }

            @Override
            public List<Edit> edits(ChatMessage part, ExportTextRegistry.Session session) {
                return List.of();
            }
        });
        assertTrue(registry.isActive());

        registry.apply(List.of(part("a"), part("b"), part("c")), SESSION);
        assertEquals(List.of(3), prepared);

        registry.unregisterAll("owner");
        assertFalse(registry.isActive());
        List<ChatMessage> parts = List.of(part("a"));
        assertSame(parts, registry.apply(parts, SESSION), "with none registered the parts are returned as they are");
    }
}
