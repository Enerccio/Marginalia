package com.github.enerccio.marginalia.extensions.namegenerator;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The packed tables of a category ({@link NameTables.Chain}) and the reading of {@code name-tables.json}.
 */
class NameChainTest {

    private static final String JSON = """
            {"categories": [
              {"tables": {"^^": {"b": 0.5, "a": 0.5}, "^b": {"o": 1.0}, "^a": {"n": 1.0}, "bo": {"b": 1.0}, "an": {"n": 1.0}, "ob": {"$": 1.0}, "nn": {"a": 1.0}, "na": {"$": 1.0}},
               "title": "Test - Male", "size": 2, "order": 2, "unknown": [1, {"x": 2}], "minLength": 3, "maxLength": 5}
            ]}
            """;

    @Test
    void theTablesAreReadWhateverTheOrderOfTheKeys() {
        NameTables tables = NameTables.parse(new StringReader(JSON));

        assertEquals(List.of("Test - Male"), tables.categories());
        List<String> names = tables.generate("Test - Male", 10, new Random(3));
        assertEquals(List.of("Bob", "Anna").stream().sorted().toList(), names.stream().sorted().toList());
    }

    @Test
    void theCharactersOfAContextKeepTheOrderOfTheFile() {
        Map<Character, Double> next = new LinkedHashMap<>();
        next.put('z', 0.9);
        next.put('a', 0.1);
        NameTables.Chain chain = NameTables.Chain.of(1, Map.of("^", next));

        int index = chain.find(chain.start());
        assertTrue(index >= 0);
        // the roll falls into the first character by its probability, as it did when the tables were maps
        assertEquals('z', chain.pick(index, new Random(1) {
            @Override
            public double nextDouble() {
                return 0.5;
            }
        }));
        assertEquals('a', chain.pick(index, new Random(1) {
            @Override
            public double nextDouble() {
                return 0.95;
            }
        }));
        assertTrue(chain.find(NameTables.Chain.pack("x")) < 0);
    }

    @Test
    void aContextIsTheLastCharactersAndAdvancingKeepsOnlyThose() {
        NameTables.Chain chain = NameTables.Chain.of(2, Map.of("^^", Map.of('a', 1.0)));

        long context = chain.start();
        assertEquals(NameTables.Chain.pack("^^"), context);
        context = chain.advance(chain.advance(chain.advance(context, 'a'), 'b'), 'c');
        assertEquals(NameTables.Chain.pack("bc"), context);
    }

    @Test
    void anOrderAboveTheLimitIsRefused() {
        assertThrows(IllegalStateException.class, () -> NameTables.Chain.pack("abcde"));
        NameTables.Chain four = NameTables.Chain.of(4, Map.of("^^^^", Map.of('a', 1.0)));
        assertEquals(NameTables.Chain.pack("bcde"), four.advance(four.advance(four.advance(four.advance(four.advance(four.start(), 'a'), 'b'), 'c'), 'd'), 'e'));
    }
}
