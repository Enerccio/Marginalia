package com.github.enerccio.marginalia.extensions.knowledgetracker.facts;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.*;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.Part;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What the annotation is told: the state of the characters when the reviewed part begins, rebuilt from the messages the items remember,
 * and what the part itself changes.
 */
class KnowledgeFactsBuilderTest {

    private static Part part(String uuid, String present, String text) {
        return new Part(uuid, present, text);
    }

    /** m1, m2, m3 are read; the reviewed part is the one the branch ends with. */
    private static List<Part> branch(String... uuids) {
        List<Part> parts = new ArrayList<>();
        for (String uuid : uuids) {
            parts.add(part(uuid, "Fiora", "Fiora walked."));
        }
        return parts;
    }

    private static KnowledgeState read(String... uuids) {
        KnowledgeState state = new KnowledgeState();
        for (String uuid : uuids) {
            state.getProcessed().add(new ProcessedPart(uuid, "h", 0));
        }
        return state;
    }

    private static TrackedCharacter fiora(KnowledgeState state) {
        TrackedCharacter fiora = new TrackedCharacter("Fiora");
        fiora.setAliases(List.of("Fi"));
        state.getCharacters().add(fiora);
        return fiora;
    }

    @Test
    void theInitialStateAndWhatWasLearnedBeforeThePartAreFacts() {
        KnowledgeState state = read("m1", "m2");
        TrackedCharacter fiora = fiora(state);
        fiora.getItems().add(KnowledgeItem.initial("Thomas is her father", ItemKind.DOES_NOT_KNOW));
        fiora.getItems().add(KnowledgeItem.learned("Mara is a courier", ItemKind.KNOWS, "m1", "q"));
        fiora.addMet("Mara", "m1");

        String facts = KnowledgeFactsBuilder.build(state, branch("m1", "m2", "m3"), null);

        assertEquals("Fiora knows: Mara is a courier.\n"
                + "Fiora does not know: Thomas is her father.\n"
                + "Fiora has met: Mara (nobody else).", facts);
    }

    @Test
    void whatIsLearnedInALaterPartIsNotYetAFact() {
        KnowledgeState state = read("m1", "m2", "m3", "m4");
        TrackedCharacter fiora = fiora(state);
        fiora.getItems().add(KnowledgeItem.learned("Mara is a courier", ItemKind.KNOWS, "m4", "q"));
        fiora.addMet("Mara", "m4");

        String facts = KnowledgeFactsBuilder.build(state, branch("m1", "m2"), null);

        assertFalse(facts.contains("Mara"), facts);
        assertTrue(facts.contains("Fiora has met: nobody (nobody else)."), facts);
    }

    @Test
    void anItemRetractedByALaterPartWasStillTrueAndOneRetractedBeforeIsNot() {
        KnowledgeState state = read("m1", "m2", "m3", "m4");
        TrackedCharacter fiora = fiora(state);
        KnowledgeItem early = KnowledgeItem.initial("Thomas exists", ItemKind.DOES_NOT_KNOW);
        early.retract("m2");
        KnowledgeItem late = KnowledgeItem.initial("the map is a fake", ItemKind.DOES_NOT_KNOW);
        late.retract("m4");
        fiora.getItems().add(early);
        fiora.getItems().add(late);

        String facts = KnowledgeFactsBuilder.build(state, branch("m1", "m2", "m3"), null);

        assertTrue(facts.contains("Fiora does not know: the map is a fake."), facts);
        assertFalse(facts.contains("Thomas exists"), facts);
    }

    @Test
    void whatThePartItselfChangesIsListedApartNotAsAFact() {
        KnowledgeState state = read("m1", "m2");
        TrackedCharacter fiora = fiora(state);
        KnowledgeItem unmet = KnowledgeItem.initial("Thomas exists", ItemKind.DOES_NOT_KNOW);
        unmet.retract("m3");
        fiora.getItems().add(unmet);
        fiora.getItems().add(KnowledgeItem.learned("Thomas is a smith", ItemKind.KNOWS, "m3", "q"));
        fiora.addMet("Thomas", "m3");

        String facts = KnowledgeFactsBuilder.build(state, branch("m1", "m2", "m3"), null);

        assertFalse(facts.contains("Fiora does not know: Thomas exists"), facts);
        assertFalse(facts.contains("Fiora knows: Thomas is a smith"), facts);
        assertTrue(facts.contains("Fiora learns in this part: Thomas is a smith."), facts);
        assertTrue(facts.contains("Fiora meets in this part: Thomas."), facts);
        assertTrue(facts.contains("Fiora until this part, which changes it: does not know Thomas exists."), facts);
    }

    @Test
    void itemsThatAreNotFactsAtAnyPointAreLeftOut() {
        KnowledgeState state = read("m1", "m2");
        TrackedCharacter fiora = fiora(state);
        KnowledgeItem byHand = KnowledgeItem.initial("the key is hidden", ItemKind.KNOWS);
        byHand.retractByHand();
        KnowledgeItem tidied = KnowledgeItem.learned("it rained", ItemKind.KNOWS, "m1", "q");
        tidied.tidy();
        KnowledgeItem rejected = KnowledgeItem.learned("the inn is closed", ItemKind.KNOWS, "m1", "q");
        rejected.reject();
        fiora.getItems().add(byHand);
        fiora.getItems().add(tidied);
        fiora.getItems().add(rejected);
        fiora.getItems().add(KnowledgeItem.initial("the road is long", ItemKind.KNOWS));

        String facts = KnowledgeFactsBuilder.build(state, branch("m1", "m2", "m3"), null);

        assertTrue(facts.contains("the road is long"), facts);
        assertFalse(facts.contains("the key is hidden") || facts.contains("it rained") || facts.contains("the inn is closed"), facts);
    }

    @Test
    void anItemLearnedOnAnotherBranchIsNotAFact() {
        KnowledgeState state = read("m1", "x2");
        TrackedCharacter fiora = fiora(state);
        fiora.getItems().add(KnowledgeItem.learned("the duke is dead", ItemKind.KNOWS, "x2", "q"));

        String facts = KnowledgeFactsBuilder.build(state, branch("m1", "m2"), null);

        assertFalse(facts.contains("the duke is dead"), facts);
    }

    @Test
    void onlyCharactersOfThePartAreDescribed() {
        KnowledgeState state = read("m1");
        fiora(state).getItems().add(KnowledgeItem.initial("the road is long", ItemKind.KNOWS));
        TrackedCharacter thomas = new TrackedCharacter("Thomas");
        thomas.getItems().add(KnowledgeItem.initial("he has a daughter", ItemKind.KNOWS));
        state.getCharacters().add(thomas);

        String facts = KnowledgeFactsBuilder.build(state, List.of(part("m1", "Fiora", "She walked alone.")), null);

        assertTrue(facts.contains("Fiora knows"), facts);
        assertFalse(facts.contains("Thomas"), facts);
    }

    @Test
    void aCharacterNamedInTheTextTakesPartWithoutBeingInThePresentField() {
        KnowledgeState state = read();
        fiora(state).getItems().add(KnowledgeItem.initial("the road is long", ItemKind.KNOWS));

        // by the alias, as a word, whatever the case; Fiora's is not a part of another word
        assertTrue(KnowledgeFactsBuilder.build(state, List.of(part("m1", "", "\"Hello, FI!\" said the smith.")), null).contains("Fiora knows"));
        assertEquals("", KnowledgeFactsBuilder.build(state, List.of(part("m1", "", "The Fiorans cheered.")), null));
        assertEquals("", KnowledgeFactsBuilder.build(state, List.of(part("m1", "", "A Fidget spinner.")), null));
    }

    @Test
    void theNameOfACharacterInChineseIsFoundInsideTheText() {
        assertTrue(KnowledgeFactsBuilder.isNamedIn("菲奥拉", "他看见菲奥拉走来。"));
        assertFalse(KnowledgeFactsBuilder.isNamedIn("菲奥拉", "他看见玛拉走来。"));
        assertFalse(KnowledgeFactsBuilder.isNamedIn(" ", "anything"));
    }

    @Test
    void theClosedWorldLineIsNotWrittenWhenTheBookTurnedItOff() {
        KnowledgeState state = read("m1", "m2");
        TrackedCharacter fiora = fiora(state);
        fiora.addMet("Mara", "m1");
        state.getSettings().setClosedWorld(false);

        String facts = KnowledgeFactsBuilder.build(state, branch("m1", "m2"), null);

        assertEquals("Fiora has met: Mara.", facts);
    }

    @Test
    void thePartsTheTrackerHasNotReadAreSaid() {
        KnowledgeState state = read("m1");
        fiora(state).getItems().add(KnowledgeItem.initial("the road is long", ItemKind.KNOWS));

        String facts = KnowledgeFactsBuilder.build(state, branch("m1", "m2", "m3"), null);

        assertTrue(facts.endsWith(KnowledgeFactsBuilder.UNREAD), facts);
        assertFalse(KnowledgeFactsBuilder.build(read("m1", "m2"), branch("m1", "m2", "m3"), null).contains("not read"));
    }

    @Test
    void thePartsBeforeTheStartPointDoNotCountAsUnread() {
        KnowledgeState state = read("m2");
        state.setStartMessage("m2");
        fiora(state).getItems().add(KnowledgeItem.initial("the road is long", ItemKind.KNOWS));

        assertFalse(KnowledgeFactsBuilder.build(state, branch("m1", "m2", "m3"), null).contains("not read"));
    }

    @Test
    void nothingIsGivenWhenNobodyIsTracked() {
        assertEquals("", KnowledgeFactsBuilder.build(read("m1"), branch("m1"), null));
        assertEquals("", KnowledgeFactsBuilder.build(read("m1"), List.of(), null));
    }
}
