package com.github.enerccio.marginalia.extensions.knowledgetracker.model;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The life of an item (active, old, restored, edited), the rewind by source message and the stored form.
 */
class KnowledgeStateTest {

    private static TrackedCharacter fiora() {
        TrackedCharacter fiora = new TrackedCharacter("Fiora");
        fiora.setAliases(List.of("Fi"));
        return fiora;
    }

    @Test
    void aCharacterIsFoundByNameOrAliasIgnoringCase() {
        KnowledgeState state = new KnowledgeState();
        state.getCharacters().add(fiora());
        assertSame(state.getCharacters().getFirst(), state.findByName(" FIORA "));
        assertSame(state.getCharacters().getFirst(), state.findByName("fi"));
        assertNull(state.findByName("Fiora's sister"));
        assertNull(state.findByName(""));
    }

    @Test
    void retractedItemsAreOldAndRestoreMakesThemActive() {
        KnowledgeItem item = KnowledgeItem.initial("has not met Thomas", ItemKind.DOES_NOT_KNOW);
        assertTrue(item.isActive());
        item.retractByHand();
        assertFalse(item.isActive());
        assertEquals(OldReason.RETRACTED, item.getOldReason());
        item.restore();
        assertTrue(item.isActive());
        assertEquals(ItemOrigin.INITIAL, item.getOrigin());
    }

    @Test
    void restoringARejectedAdditionMakesItTheUsers() {
        KnowledgeItem item = KnowledgeItem.learned("Thomas is a smuggler", ItemKind.KNOWS, "m5", "he winked");
        item.reject();
        assertEquals(OldReason.REJECTED, item.getOldReason());
        item.restore();
        assertTrue(item.isActive());
        assertEquals(ItemOrigin.MANUAL, item.getOrigin());
        assertNull(item.getSourceMessage());
        assertNull(item.getEvidence());
    }

    @Test
    void editingALearnedItemMakesItTheUsers() {
        KnowledgeItem item = KnowledgeItem.learned("Thomas is a smuggler", ItemKind.KNOWS, "m5", "he winked");
        item.edit("Thomas is a thief", ItemKind.KNOWS);
        assertEquals("Thomas is a thief", item.getText());
        assertEquals(ItemOrigin.MANUAL, item.getOrigin());
        assertNull(item.getSourceMessage());
    }

    @Test
    void rewindDropsWhatWasLearnedInTheDroppedMessages() {
        TrackedCharacter fiora = fiora();
        fiora.getItems().add(KnowledgeItem.initial("knows the city", ItemKind.KNOWS));
        fiora.getItems().add(KnowledgeItem.manual("owes the Guild", ItemKind.KNOWS));
        fiora.getItems().add(KnowledgeItem.learned("Mara is a courier", ItemKind.KNOWS, "m1", "q"));
        fiora.getItems().add(KnowledgeItem.learned("Thomas is a smuggler", ItemKind.KNOWS, "m3", "q"));
        KnowledgeItem rejected = KnowledgeItem.learned("Thomas is her father", ItemKind.KNOWS, "m4", "q");
        rejected.reject();
        fiora.getItems().add(rejected);
        fiora.addMet("Mara", "m1");
        fiora.addMet("Thomas", "m3");

        int changed = fiora.rewind(Set.of("m3", "m4")::contains);

        assertEquals(3, changed); // the learned item, the rejected addition, the meeting
        assertEquals(List.of("knows the city", "owes the Guild", "Mara is a courier"),
                fiora.getItems().stream().map(KnowledgeItem::getText).toList());
        assertEquals(List.of("Mara"), fiora.getMet().stream().map(MetPerson::getName).toList());
    }

    @Test
    void rewindBringsBackWhatTheDroppedMessagesRetracted() {
        TrackedCharacter fiora = fiora();
        KnowledgeItem notMet = KnowledgeItem.initial("has not met Thomas", ItemKind.DOES_NOT_KNOW);
        notMet.retract("m3");
        fiora.getItems().add(notMet);

        fiora.rewind(Set.of("m3")::contains);

        assertTrue(notMet.isActive());
        assertEquals(1, fiora.activeItems().size());
    }

    @Test
    void rewindKeepsARetractionByHandAndTheRetractionOfEarlierMessages() {
        TrackedCharacter fiora = fiora();
        KnowledgeItem byHand = KnowledgeItem.initial("a", ItemKind.KNOWS);
        byHand.retractByHand();
        KnowledgeItem early = KnowledgeItem.initial("b", ItemKind.KNOWS);
        early.retract("m1");
        fiora.getItems().addAll(List.of(byHand, early));

        assertEquals(0, fiora.rewind(Set.of("m3")::contains));

        assertFalse(byHand.isActive());
        assertFalse(early.isActive());
        assertEquals(2, fiora.oldItems().size());
    }

    @Test
    void rewindDropsAnItemTogetherWithItsRetractionWhenBothAreDropped() {
        TrackedCharacter fiora = fiora();
        KnowledgeItem item = KnowledgeItem.learned("x", ItemKind.KNOWS, "m2", "q");
        item.retract("m3");
        fiora.getItems().add(item);

        fiora.rewind(Set.of("m2", "m3")::contains);

        assertTrue(fiora.getItems().isEmpty());
    }

    @Test
    void metPeopleAreRecordedOnceAndNeverIncludeTheCharacter() {
        TrackedCharacter fiora = fiora();
        fiora.addMet("Mara", "m1");
        fiora.addMet(" mara ", "m2");
        fiora.addMet("Fiora", "m1");
        fiora.addMet("fi", "m1");
        fiora.addMet("", "m1");

        assertEquals(1, fiora.getMet().size());
        assertEquals("m1", fiora.getMet().getFirst().getSourceMessage());
    }

    @Test
    void metPeopleCanBeEditedAndKeepTheirRecordWhenNamedAgain() {
        TrackedCharacter fiora = fiora();
        fiora.addMet("Mara", "m1");
        fiora.addMet("Thomas", "m2");
        fiora.addMet("female clerk", "m3");

        fiora.setMetNames(List.of("thomas", "Queen", "Fiora", "Queen", " "));

        assertEquals(List.of("Thomas", "Queen"), fiora.getMet().stream().map(MetPerson::getName).toList());
        assertEquals("m2", fiora.getMet().getFirst().getSourceMessage());
        // written by the user: a reprocessing does not take it back
        assertNull(fiora.getMet().get(1).getSourceMessage());
        fiora.rewind(Set.of("m1", "m2", "m3")::contains);
        assertEquals(List.of("Queen"), fiora.getMet().stream().map(MetPerson::getName).toList());

        fiora.setMetNames(List.of());
        assertTrue(fiora.getMet().isEmpty());
    }

    @Test
    void stateSurvivesItsStoredForm() {
        KnowledgeState state = new KnowledgeState();
        state.setCursor("m7");
        state.getSettings().setInPrompt(true);
        state.getSettings().setSeparators(List.of(",", "and"));
        TrackedCharacter fiora = fiora();
        KnowledgeItem item = KnowledgeItem.learned("Mara is a courier", ItemKind.KNOWS, "m1", "she carried a satchel");
        fiora.getItems().add(item);
        KnowledgeItem old = KnowledgeItem.initial("has not met Thomas", ItemKind.DOES_NOT_KNOW);
        old.retract("m3");
        fiora.getItems().add(old);
        fiora.addMet("Mara", "m1");
        state.getCharacters().add(fiora);

        Gson gson = new Gson();
        KnowledgeState back = gson.fromJson(gson.toJson(state), KnowledgeState.class);

        assertEquals("m7", back.getCursor());
        assertTrue(back.getSettings().isInPrompt());
        assertEquals(List.of(",", "and"), back.getSettings().getSeparators());
        TrackedCharacter backFiora = back.findByName("Fiora");
        assertEquals(2, backFiora.getItems().size());
        assertEquals(item.getId(), backFiora.getItems().getFirst().getId());
        assertEquals("she carried a satchel", backFiora.getItems().getFirst().getEvidence());
        assertEquals(OldReason.RETRACTED, backFiora.getItems().get(1).getOldReason());
        assertEquals("m3", backFiora.getItems().get(1).getRetractedAt());
        assertEquals("Mara", backFiora.getMet().getFirst().getName());
    }

    @Test
    void anOldOrPartialStoredFormGetsDefaults() {
        KnowledgeState back = new Gson().fromJson("{\"characters\":[{\"name\":\"Fiora\"}]}", KnowledgeState.class);
        assertTrue(back.getSettings().isClosedWorld());
        TrackedCharacter fiora = back.findByName("Fiora");
        assertTrue(fiora.getItems().isEmpty());
        assertTrue(fiora.getMet().isEmpty());
        assertTrue(fiora.getAliases().isEmpty());
        assertNotNull(new Gson().fromJson("{}", KnowledgeState.class).getCharacters());
    }
}
