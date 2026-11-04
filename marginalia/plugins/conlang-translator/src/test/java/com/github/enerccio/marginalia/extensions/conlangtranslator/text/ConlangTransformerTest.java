package com.github.enerccio.marginalia.extensions.conlangtranslator.text;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Span;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.Reverse.Located;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The transformer through the real registry of the core: what a model reads of parts that have translated passages.
 */
class ConlangTransformerTest {

    private static final String TEXT = "Mara looked up. \"Da ja-se vek,\" she said.";

    private static ChatMessage part(long id, String text) {
        ChatMessage part = new ChatMessage();
        part.setId(id);
        part.setResponse(text);
        return part;
    }

    private static Located passage(String text, String conlang, String english) {
        int start = text.indexOf(conlang);
        return new Located(start, start + conlang.length(), new Span("lang", english, conlang));
    }

    @Test
    void theRegistryGivesTheModelTheEnglishOfAPartWithPassages() {
        PartTextRegistry registry = new PartTextRegistry();
        registry.register("test", new ConlangTransformer(parts -> Map.of(1L, List.of(passage(TEXT, "Da ja-se vek,", "I see the enemy,")))));

        assertEquals("Mara looked up. \"I see the enemy,\" she said.", registry.forModel(part(1, TEXT)));
        assertEquals(TEXT, registry.forModel(part(2, TEXT)), "a part with no passages is read as it is");
    }

    @Test
    void thePassagesOfAWholeBranchAreAskedForOnceAndEachPartGetsItsOwn() {
        List<Integer> asked = new ArrayList<>();
        PartTextRegistry registry = new PartTextRegistry();
        registry.register("test", new ConlangTransformer(parts -> {
            asked.add(parts.size());
            return Map.of(1L, List.of(passage(TEXT, "Da ja-se vek,", "I see the enemy,")), 3L, List.of(passage(TEXT, "Da ja-se vek,", "I see you,")));
        }));

        List<String> read = registry.forModel(List.of(part(1, TEXT), part(2, TEXT), part(3, TEXT)));

        assertEquals(List.of(3), asked);
        assertEquals("Mara looked up. \"I see the enemy,\" she said.", read.get(0));
        assertEquals(TEXT, read.get(1));
        assertEquals("Mara looked up. \"I see you,\" she said.", read.get(2));
    }

    @Test
    void anEditedPassageIsReadAsItIsAndAMapBackToTheStoredTextWorks() {
        PartTextRegistry registry = new PartTextRegistry();
        Located made = passage(TEXT, "Da ja-se vek,", "I see the enemy,");
        String edited = TEXT.replace("vek,", "vek-is,");
        registry.register("test", new ConlangTransformer(parts -> Map.of(1L, List.of(new Located(made.start(), made.end() + 3, made.span())))));

        assertEquals(edited, registry.forModel(part(1, edited)));

        registry.unregisterAll("test");
        registry.register("test", new ConlangTransformer(parts -> Map.of(1L, List.of(made))));
        var model = registry.modelText(part(1, TEXT));
        int start = model.text().indexOf("I see the enemy,");
        int[] stored = model.storedRange(start, start + 6);
        assertEquals("Da ja-se vek,", TEXT.substring(stored[0], stored[1]));
    }

    @Test
    void aFailureOfTheSourceOfPassagesLeavesThePartAsStored() {
        PartTextRegistry registry = new PartTextRegistry();
        registry.register("test", new ConlangTransformer(new ConlangTransformer.Passages() {
            @Override
            public Map<Long, List<Located>> of(Collection<ChatMessage> parts) throws Exception {
                throw new IllegalStateException("the database is gone");
            }
        }));

        assertEquals(TEXT, registry.forModel(part(1, TEXT)));
    }
}
