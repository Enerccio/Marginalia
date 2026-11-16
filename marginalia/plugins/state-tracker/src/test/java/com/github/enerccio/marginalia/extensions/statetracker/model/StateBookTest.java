package com.github.enerccio.marginalia.extensions.statetracker.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StateBookTest {

    private static Category single() {
        return new Category("weapon", "Weapon", CategoryKind.SINGLE, "");
    }

    private static Category list() {
        return new Category("carrying", "Carrying", CategoryKind.LIST, "");
    }

    @Test
    void aNewValueOfASingleCategoryReplacesTheOld() {
        TrackedCharacter fiora = new TrackedCharacter("Fiora");
        Category weapon = single();
        StateItem sword = fiora.set(weapon, "sword", 1);
        StateItem axe = fiora.set(weapon, "axe", 2);
        assertEquals(List.of(axe), fiora.active("weapon"));
        assertEquals(List.of(sword), fiora.history("weapon"));
        assertEquals(StateItem.EndReason.REPLACED, sword.getEndReason());
        assertEquals(List.of("weapon"), fiora.getCategories());
    }

    @Test
    void aListCollectsEntriesAndRemovesThemOneByOne() {
        TrackedCharacter fiora = new TrackedCharacter("Fiora");
        Category carrying = list();
        StateItem rope = fiora.set(carrying, "rope", 1);
        StateItem lantern = fiora.set(carrying, "lantern", 2);
        assertEquals(List.of(rope, lantern), fiora.active("carrying"));
        fiora.remove(rope, 3);
        assertEquals(List.of(lantern), fiora.active("carrying"));
        assertEquals(StateItem.EndReason.REMOVED, rope.getEndReason());
    }

    @Test
    void restoringAPastValueOfASingleCategoryPutsTheCurrentOneAway() {
        TrackedCharacter fiora = new TrackedCharacter("Fiora");
        Category weapon = single();
        StateItem sword = fiora.set(weapon, "sword", 1);
        StateItem axe = fiora.set(weapon, "axe", 2);
        fiora.restore(sword, weapon, 3);
        assertEquals(List.of(sword), fiora.active("weapon"));
        assertFalse(axe.isActive());
    }

    @Test
    void untrackingKeepsTheValuesForgettingDeletesThem() {
        TrackedCharacter fiora = new TrackedCharacter("Fiora");
        fiora.set(single(), "sword", 1);
        fiora.untrack("weapon");
        assertTrue(fiora.getCategories().isEmpty());
        assertTrue(fiora.hasItemsOf("weapon"));
        fiora.forget("weapon");
        assertFalse(fiora.hasItemsOf("weapon"));
    }

    @Test
    void aBookCopiesAUserCategoryOnceAndStaysLinked() {
        StateBook book = new StateBook();
        Category mine = new Category("default-weapon", "Weapon", CategoryKind.SINGLE, "the weapon");
        Category copy = book.adopt(mine);
        assertNotEquals(mine.getId(), copy.getId());
        assertEquals("default-weapon", copy.getSourceId());
        assertSame(copy, book.adopt(mine));
        assertTrue(mine.sameDefinition(copy));
        mine.setInstruction("only the one in hand");
        assertFalse(mine.sameDefinition(copy));
        copy.updateFrom(mine);
        assertTrue(mine.sameDefinition(copy));
        assertEquals("default-weapon", copy.getSourceId());
    }

    @Test
    void deletingACategoryOfTheBookDeletesItsValues() {
        StateBook book = new StateBook();
        Category weapon = single();
        book.getCategories().add(weapon);
        TrackedCharacter fiora = new TrackedCharacter("Fiora");
        book.getCharacters().add(fiora);
        fiora.set(weapon, "sword", 1);
        assertTrue(book.isUsed("weapon"));
        assertEquals(1, book.trackedBy("weapon"));
        book.deleteCategory("weapon");
        assertTrue(book.getCategories().isEmpty());
        assertTrue(fiora.getItems().isEmpty());
        assertTrue(fiora.getCategories().isEmpty());
    }

    @Test
    void namesAndAliasesClash() {
        StateBook book = new StateBook();
        TrackedCharacter fiora = new TrackedCharacter("Fiora");
        fiora.setAliases(List.of("Fio"));
        book.getCharacters().add(fiora);
        assertSame(fiora, book.findByName("fio"));
        assertSame(fiora, book.findClash(null, "Mara", List.of("FIO")));
        assertNull(book.findClash(fiora, "Fiora", List.of()));
    }

    @Test
    void theStateSurvivesJson() {
        StateBook book = new StateBook();
        Category weapon = single();
        book.getCategories().add(weapon);
        TrackedCharacter fiora = new TrackedCharacter("Fiora");
        book.getCharacters().add(fiora);
        fiora.set(weapon, "sword", 1).setLocked(true);
        fiora.set(weapon, "axe", 2);
        Gson gson = new Gson();
        StateBook read = gson.fromJson(gson.toJson(book), StateBook.class);
        TrackedCharacter again = read.getCharacters().getFirst();
        assertEquals("axe", again.active("weapon").getFirst().getText());
        assertTrue(again.history("weapon").getFirst().isLocked());
        assertEquals(StateItem.EndReason.REPLACED, again.history("weapon").getFirst().getEndReason());
    }

    @Test
    void theCatalogIsTheDefaultsUntilItIsStored() {
        JsonObject attributes = new JsonObject();
        List<Category> defaults = List.of(single());
        assertEquals(1, CategoryCatalog.read(attributes, () -> defaults).size());
        assertTrue(CategoryCatalog.isDefault(attributes));
        CategoryCatalog.write(attributes, List.of());
        assertTrue(CategoryCatalog.read(attributes, () -> defaults).isEmpty());
        CategoryCatalog.write(attributes, List.of(list(), single()));
        List<Category> read = CategoryCatalog.read(attributes, () -> defaults);
        assertEquals(List.of("carrying", "weapon"), read.stream().map(Category::getId).toList());
        CategoryCatalog.reset(attributes);
        assertTrue(CategoryCatalog.isDefault(attributes));
    }
}
