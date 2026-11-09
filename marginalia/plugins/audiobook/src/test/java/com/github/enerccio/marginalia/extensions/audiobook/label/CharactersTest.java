package com.github.enerccio.marginalia.extensions.audiobook.label;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharactersTest {

    @Test
    void namesAreCollectedOnceInTheOrderOfTheirFirstMention() {
        assertEquals(List.of("Tom\u00e1\u0161", "Mizuki", "Jonas"),
                Characters.of(Arrays.asList("Tom\u00e1\u0161", "Mizuki, Tom\u00e1\u0161", null, "  ", "Jonas; Mizuki\nTom\u00e1\u0161")));
    }

    @Test
    void nothingIsNothing() {
        assertEquals(List.of(), Characters.of(List.of()));
        assertEquals(List.of(), Characters.of(Arrays.asList(null, "", " , ; ")));
    }

    @Test
    void aPassageThatIsNotANameIsLeftOutAndTheListIsCapped() {
        List<String> fields = new ArrayList<>();
        fields.add("x".repeat(80));
        for (int i = 0; i < 30; i++) {
            fields.add("Name " + i);
        }
        List<String> names = Characters.of(fields);
        assertEquals(Characters.MAX, names.size());
        assertEquals("Name 0", names.getFirst());
    }
}
