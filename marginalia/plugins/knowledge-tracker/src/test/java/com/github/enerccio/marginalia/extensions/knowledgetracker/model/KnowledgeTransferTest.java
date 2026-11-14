package com.github.enerccio.marginalia.extensions.knowledgetracker.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The mass changes of items (retract, restore) and the export of the tracked characters to JSON and its import.
 */
class KnowledgeTransferTest {

    private static KnowledgeState book() {
        KnowledgeState state = new KnowledgeState();
        TrackedCharacter fiora = new TrackedCharacter("Fiora");
        fiora.setAliases(List.of("Fi"));
        fiora.getItems().add(KnowledgeItem.initial("has not met Thomas", ItemKind.DOES_NOT_KNOW));
        KnowledgeItem learned = KnowledgeItem.learned("Mara is a courier", ItemKind.KNOWS, "msg-1", "Mara said so");
        learned.setLocked(true);
        fiora.getItems().add(learned);
        KnowledgeItem old = KnowledgeItem.manual("the Guild owes her money", ItemKind.KNOWS);
        old.retract("msg-2");
        fiora.getItems().add(old);
        fiora.addMet("Mara", "msg-1");
        state.getCharacters().add(fiora);
        state.getCharacters().add(new TrackedCharacter("Mara"));
        return state;
    }

    @Test
    void retractAndRestoreChangeOnlyTheItemsTheyCanChange() {
        TrackedCharacter fiora = book().getCharacters().getFirst();
        List<String> all = fiora.getItems().stream().map(KnowledgeItem::getId).toList();
        assertEquals(2, fiora.retractItems(all));
        assertTrue(fiora.activeItems().isEmpty());
        assertEquals(3, fiora.restoreItems(all));
        assertEquals(3, fiora.activeItems().size());
        assertEquals(0, fiora.restoreItems(List.of(all.getFirst())));
    }

    @Test
    void anExportImportedIntoAnEmptyBookGivesTheSameCharacters() {
        String json = KnowledgeTransfer.export(book());
        assertTrue(json.contains(KnowledgeTransfer.FORMAT));
        assertFalse(json.contains("msg-1"), "no message of the book is exported");

        KnowledgeState other = new KnowledgeState();
        KnowledgeTransfer.ImportResult result = KnowledgeTransfer.importInto(other, json);
        assertEquals(new KnowledgeTransfer.ImportResult(2, 0, 3, 0), result);

        TrackedCharacter fiora = other.findByName("fi");
        assertNotNull(fiora);
        assertEquals("Fiora", fiora.getName());
        assertEquals(List.of("Fi"), fiora.getAliases());
        assertEquals(List.of("Mara"), fiora.getMet().stream().map(MetPerson::getName).toList());
        assertNull(fiora.getMet().getFirst().getSourceMessage());

        KnowledgeItem initial = fiora.getItems().get(0);
        assertEquals(ItemOrigin.INITIAL, initial.getOrigin());
        assertEquals(ItemKind.DOES_NOT_KNOW, initial.getKind());
        KnowledgeItem learned = fiora.getItems().get(1);
        assertEquals(ItemOrigin.MANUAL, learned.getOrigin(), "it has no part in this book: it is the user's");
        assertNull(learned.getSourceMessage());
        assertTrue(learned.isLocked());
        KnowledgeItem old = fiora.getItems().get(2);
        assertFalse(old.isActive());
        assertEquals(OldReason.RETRACTED, old.getOldReason());
        assertNull(old.getRetractedAt(), "a retraction by hand, which a reprocess leaves alone");
    }

    @Test
    void anImportIntoTheSameBookAddsOnlyWhatIsMissing() {
        KnowledgeState state = book();
        String json = KnowledgeTransfer.export(state);
        state.getCharacters().getFirst().getItems().removeLast();

        KnowledgeTransfer.ImportResult result = KnowledgeTransfer.importInto(state, json);
        assertEquals(new KnowledgeTransfer.ImportResult(0, 1, 1, 2), result);
        assertEquals(2, state.getCharacters().size());
        assertEquals(3, state.getCharacters().getFirst().getItems().size());
        assertEquals(1, state.getCharacters().getFirst().getMet().size());
    }

    @Test
    void anAliasOfAnotherCharacterIsNotTaken() {
        KnowledgeState state = new KnowledgeState();
        state.getCharacters().add(new TrackedCharacter("Thomas"));
        String json = """
                {"format": "marginalia-knowledge-tracker", "version": 1, "characters": [
                  {"name": "Tom", "aliases": ["Tommy"], "items": [{"text": "knows the city", "kind": "KNOWS"}, {"text": " "}]},
                  {"name": "Fiora", "aliases": ["Thomas", "Fi"]}
                ]}""";
        KnowledgeTransfer.ImportResult result = KnowledgeTransfer.importInto(state, json);
        assertEquals(2, result.newCharacters());
        assertEquals(1, result.itemsAdded());
        assertEquals(List.of("Tommy"), state.findByName("Tom").getAliases());
        // "Thomas" is the name of another character: Fiora is a new character without that alias
        assertEquals(3, state.getCharacters().size());
        assertEquals(List.of("Fi"), state.findByName("Fiora").getAliases());
        assertSame(state.getCharacters().getFirst(), state.findByName("Thomas"));
    }

    @Test
    void aFileThatIsNotAnExportChangesNothing() {
        KnowledgeState state = book();
        assertThrows(IllegalArgumentException.class, () -> KnowledgeTransfer.importInto(state, "not json {"));
        assertThrows(IllegalArgumentException.class, () -> KnowledgeTransfer.importInto(state, "[]"));
        assertThrows(IllegalArgumentException.class, () -> KnowledgeTransfer.importInto(state, "{\"format\": \"other\", \"characters\": []}"));
        assertThrows(IllegalArgumentException.class, () -> KnowledgeTransfer.importInto(state, "{\"version\": 1}"));
        assertEquals(2, state.getCharacters().size());
    }
}
