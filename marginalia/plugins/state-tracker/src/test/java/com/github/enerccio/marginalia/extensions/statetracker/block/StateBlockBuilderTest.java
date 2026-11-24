package com.github.enerccio.marginalia.extensions.statetracker.block;

import com.github.enerccio.marginalia.extensions.statetracker.model.Category;
import com.github.enerccio.marginalia.extensions.statetracker.model.CategoryKind;
import com.github.enerccio.marginalia.extensions.statetracker.model.StateBook;
import com.github.enerccio.marginalia.extensions.statetracker.model.TrackedCharacter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StateBlockBuilderTest {

    private final StateBook book = new StateBook();
    private final Category wearing = new Category("wearing", "Wearing", CategoryKind.DESCRIPTION, "");
    private final Category weapon = new Category("weapon", "Weapon", CategoryKind.SINGLE, "");
    private final Category carrying = new Category("carrying", "Carrying", CategoryKind.LIST, "");
    private final TrackedCharacter fiora = new TrackedCharacter("Fiora");
    private final TrackedCharacter mara = new TrackedCharacter("Mara");

    StateBlockBuilderTest() {
        weapon.setShowWhenEmpty(true);
        book.getCategories().addAll(List.of(wearing, weapon, carrying));
        book.getCharacters().addAll(List.of(fiora, mara));
        fiora.setCategories(List.of("wearing", "weapon", "carrying"));
        mara.setCategories(List.of("carrying"));
    }

    @Test
    void onlyThePresentCharactersWithAStateAreDescribed() {
        fiora.set(wearing, "torn green cloak.", 1);
        fiora.set(carrying, "rope", 2);
        fiora.set(carrying, "lantern", 3);
        String block = StateBlockBuilder.build(book, "Fiora (tired), Thomas, Mara");
        assertEquals(StateBlockBuilder.HEADER + "\n\nFiora:\n- Wearing: torn green cloak\n- Weapon: none\n- Carrying: rope; lantern", block);
    }

    @Test
    void anEmptyCategoryIsLeftOutUnlessItSaysNone() {
        mara.set(carrying, "letter", 1);
        mara.remove(mara.active("carrying").getFirst(), 2);
        assertEquals("", StateBlockBuilder.build(book, "Mara"));
    }

    @Test
    void nobodyTrackedIsPresent() {
        fiora.set(wearing, "cloak", 1);
        assertEquals("", StateBlockBuilder.build(book, "Thomas"));
        assertEquals("", StateBlockBuilder.build(book, null));
    }

    @Test
    void aValueOfSeveralLinesIsOneLine() {
        fiora.set(wearing, "a cloak\nand boots", 1);
        assertTrue(StateBlockBuilder.build(book, "Fiora").contains("- Wearing: a cloak and boots\n"));
    }

    @Test
    void theSeparatorsOfTheBookSplitTheField() {
        fiora.set(wearing, "cloak", 1);
        book.getSettings().setSeparators(List.of("+"));
        assertTrue(StateBlockBuilder.build(book, "Thomas + Fiora").contains("Fiora:"));
        assertEquals("", StateBlockBuilder.build(book, "Thomas, Fiora"));
    }
}
