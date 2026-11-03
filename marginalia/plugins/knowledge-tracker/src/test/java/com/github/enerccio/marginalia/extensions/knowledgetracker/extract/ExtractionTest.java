package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ExtractionAnswer.Addition;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ExtractionAnswer.CharacterChanges;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ExtractionAnswer.Retraction;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ItemKind;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeItem;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.OldReason;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.TrackedCharacter;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.Part;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The request, the checking of the answer and the applying of the reviewed changes.
 */
class ExtractionTest {

    private static final Part P1 = new Part("m1", "Fiora", "Fiora, Thomas",
            "The bell rang. \"Thomas Wren, assessor of the Crown,\" he said. Fiora's hands went still on the dough.");
    private static final Part P2 = new Part("m2", null, "Fiora, Mara", "Mara   brought\na letter from Nell.");

    private static KnowledgeState state() {
        KnowledgeState state = new KnowledgeState();
        TrackedCharacter fiora = new TrackedCharacter("Fiora");
        fiora.setAliases(List.of("Fi"));
        fiora.getItems().add(KnowledgeItem.initial("Mara is a courier", ItemKind.KNOWS));
        fiora.getItems().add(KnowledgeItem.initial("Thomas Wren: she has never met him", ItemKind.DOES_NOT_KNOW));
        KnowledgeItem old = KnowledgeItem.initial("an old thing", ItemKind.KNOWS);
        old.retractByHand();
        fiora.getItems().add(old);
        state.getCharacters().add(fiora);
        TrackedCharacter peter = new TrackedCharacter("Peter");
        peter.getItems().add(KnowledgeItem.initial("Lord Harlan is the chamberlain", ItemKind.KNOWS));
        state.getCharacters().add(peter);
        return state;
    }

    private static ExtractionPrompt.Request request(KnowledgeState state) {
        return ExtractionPrompt.build(state, "Thomas Wren is an assessor.", List.of(P1, P2), null);
    }

    private static ExtractionAnswer answer(CharacterChanges... changes) {
        return new ExtractionAnswer(List.of(changes));
    }

    // --- the request ---

    @Test
    void theRequestNumbersTheActiveItemsAndListsOnlyThePresentCharacters() {
        KnowledgeState state = state();
        ExtractionPrompt.Request request = request(state);

        assertEquals(1, request.included().size());
        assertTrue(request.user().contains("[World]\nThomas Wren is an assessor."));
        assertTrue(request.user().contains("Fiora (present in the messages: #1, #2)"));
        assertTrue(request.user().contains("  1. knows: Mara is a courier"));
        assertTrue(request.user().contains("  2. does not know: Thomas Wren: she has never met him"));
        assertFalse(request.user().contains("an old thing"), "old items are not sent");
        assertFalse(request.user().contains("Peter"), "a character that is not present is not asked about");
        assertTrue(request.user().contains("#1 (POV Fiora; present: Fiora, Thomas)\nThe bell rang."));
        assertTrue(request.user().contains("#2 (POV ; present: Fiora, Mara)"));
        assertEquals(2, request.numbering().items().get(state.findByName("Fiora").getId()).size());
    }

    @Test
    void withoutLoreOrPresentCharactersTheRequestSaysSo() {
        assertFalse(ExtractionPrompt.build(state(), "", List.of(P1), null).user().contains("[World]"));
        assertTrue(ExtractionPrompt.build(state(), "", List.of(new Part("x", null, "Ilse", "t")), null).isEmpty());
    }

    @Test
    void aCharacterWithNothingWrittenYetIsStillAsked() {
        KnowledgeState state = new KnowledgeState();
        state.getCharacters().add(new TrackedCharacter("Ilse"));
        ExtractionPrompt.Request request = ExtractionPrompt.build(state, "", List.of(new Part("x", null, "Ilse", "t")), null);
        assertTrue(request.user().contains("(nothing is written yet)"));
    }

    // --- checking the answer ---

    @Test
    void aGoodAnswerBecomesChanges() {
        KnowledgeState state = state();
        ChangeSet set = ChangeSetBuilder.build(state, request(state), answer(new CharacterChanges("Fiora",
                List.of(new Retraction(2, "Thomas Wren, assessor of the Crown", 1)),
                List.of(new Addition("knows", "Thomas Wren is an assessor of the Crown", "\"Thomas Wren, assessor of the Crown,\" he said", 1)))));

        assertEquals(2, set.changes().size());
        Change retract = set.changes().get(0);
        assertEquals(Change.Type.RETRACT, retract.getType());
        assertEquals("Thomas Wren: she has never met him", retract.getItemText());
        assertEquals("m1", retract.getMessageUuid());
        assertTrue(retract.isEvidenceFound());
        Change add = set.changes().get(1);
        assertEquals(ItemKind.KNOWS, add.getKind());
        assertTrue(add.isApply());
        assertTrue(add.isEvidenceFound());
        assertTrue(set.dropped().isEmpty());
    }

    @Test
    void anAliasNamesTheSameCharacter() {
        KnowledgeState state = state();
        ChangeSet set = ChangeSetBuilder.build(state, request(state), answer(new CharacterChanges("fi", List.of(),
                List.of(new Addition("does-not-know", "Mara knows the assessor", "Mara brought a letter", 2)))));
        assertEquals(1, set.changes().size());
        assertEquals(ItemKind.DOES_NOT_KNOW, set.changes().get(0).getKind());
    }

    @Test
    void whatIsNotUsableIsDroppedWithAReason() {
        KnowledgeState state = state();
        ChangeSet set = ChangeSetBuilder.build(state, request(state), answer(
                new CharacterChanges("Nobody", List.of(), List.of(new Addition("knows", "x", "q", 1))),
                new CharacterChanges("Peter", List.of(), List.of(new Addition("knows", "x", "q", 1))),
                new CharacterChanges("Fiora",
                        List.of(new Retraction(9, "q", 1), new Retraction(1, "", 1), new Retraction(1, "q", 7), new Retraction(2, "q", 1), new Retraction(2, "q", 1)),
                        List.of(new Addition("knows", "Mara is a courier", "q", 1),
                                new Addition("knows", "  ", "q", 1),
                                new Addition("knows", "a thing", "", 1),
                                new Addition("knows", "another thing", "q", 0),
                                new Addition("knows", "a new thing", "q", 1),
                                new Addition("knows", "A new thing.", "q", 2)))));

        // kept: the first retraction of item 2 and "a new thing"
        assertEquals(2, set.changes().size());
        assertEquals(Change.Type.RETRACT, set.changes().get(0).getType());
        assertEquals("a new thing", set.changes().get(1).getText());
        assertEquals(11, set.dropped().size(), set.dropped().toString());
    }

    @Test
    void anOldItemCanNotBeRetractedAgain() {
        KnowledgeState state = state();
        ExtractionPrompt.Request request = request(state);
        // the answer names item 3, which is not in the numbering at all (old items are not numbered)
        ChangeSet set = ChangeSetBuilder.build(state, request, answer(new CharacterChanges("Fiora", List.of(new Retraction(3, "q", 1)), null)));
        assertTrue(set.isEmpty());
    }

    @Test
    void anItemRetractedByHandAfterTheRequestIsNotRetractedTwice() {
        KnowledgeState state = state();
        ExtractionPrompt.Request request = request(state);
        state.findByName("Fiora").getItems().get(1).retractByHand();
        ChangeSet set = ChangeSetBuilder.build(state, request, answer(new CharacterChanges("Fiora", List.of(new Retraction(2, "q", 1)), null)));
        assertTrue(set.isEmpty());
    }

    @Test
    void nullListsAndANullAnswerGiveNothing() {
        KnowledgeState state = state();
        assertTrue(ChangeSetBuilder.build(state, request(state), null).isEmpty());
        assertTrue(ChangeSetBuilder.build(state, request(state), new ExtractionAnswer(null)).isEmpty());
        assertTrue(ChangeSetBuilder.build(state, request(state), answer(new CharacterChanges("Fiora", null, null))).isEmpty());
    }

    @Test
    void aQuoteThatIsNotInTheMessageIsFlagged() {
        KnowledgeState state = state();
        ChangeSet set = ChangeSetBuilder.build(state, request(state), answer(new CharacterChanges("Fiora", List.of(), List.of(
                new Addition("knows", "something", "she smiled and waved", 1),
                new Addition("knows", "something else", "Mara brought a letter", 2),
                new Addition("knows", "a third", "The bell rang ... went still on the dough", 1),
                new Addition("knows", "a fourth", "The bell rang ... never happened", 1)))));
        assertEquals(List.of(false, true, true, false), set.changes().stream().map(Change::isEvidenceFound).toList());
    }

    // --- applying ---

    @Test
    void appliedChangesCarryTheirSourceAndCanBeTakenBack() {
        KnowledgeState state = state();
        ChangeSet set = ChangeSetBuilder.build(state, request(state), answer(new CharacterChanges("Fiora",
                List.of(new Retraction(2, "Thomas Wren", 1)),
                List.of(new Addition("knows", "Thomas Wren is an assessor", "assessor of the Crown", 1)))));

        ChangeApplier.Result result = ChangeApplier.apply(state, set);

        assertEquals(1, result.retracted());
        assertEquals(1, result.added());
        TrackedCharacter fiora = state.findByName("Fiora");
        KnowledgeItem retracted = fiora.getItems().get(1);
        assertFalse(retracted.isActive());
        assertEquals("m1", retracted.getRetractedAt());
        KnowledgeItem learned = fiora.getItems().getLast();
        assertEquals("m1", learned.getSourceMessage());
        assertEquals("assessor of the Crown", learned.getEvidence());
        assertTrue(learned.isActive());

        // a reprocess from m1 undoes both
        fiora.rewind(Set.of("m1")::contains);
        assertTrue(retracted.isActive());
        assertFalse(fiora.getItems().contains(learned));
    }

    @Test
    void uncheckedChangesAreNotApplied() {
        KnowledgeState state = state();
        ChangeSet set = ChangeSetBuilder.build(state, request(state), answer(new CharacterChanges("Fiora",
                List.of(new Retraction(2, "Thomas Wren", 1)),
                List.of(new Addition("knows", "Thomas Wren is an assessor", "assessor of the Crown", 1)))));
        set.changes().forEach(c -> c.setApply(false));

        ChangeApplier.Result result = ChangeApplier.apply(state, set);

        assertEquals(0, result.retracted());
        assertEquals(0, result.added());
        assertEquals(1, result.rejected());
        TrackedCharacter fiora = state.findByName("Fiora");
        assertTrue(fiora.getItems().get(1).isActive(), "a rejected retraction leaves the item as it is");
        KnowledgeItem kept = fiora.getItems().getLast();
        assertEquals(OldReason.REJECTED, kept.getOldReason());
        assertEquals("assessor of the Crown", kept.getEvidence());
        assertEquals(2, fiora.activeItems().size(), "the rejected addition is not sent");
    }

    @Test
    void anEditedAdditionIsAppliedWithTheEditedText() {
        KnowledgeState state = state();
        ChangeSet set = ChangeSetBuilder.build(state, request(state), answer(new CharacterChanges("Fiora", List.of(),
                List.of(new Addition("knows", "Thomas is an assessor", "assessor of the Crown", 1)))));
        set.changes().getFirst().setText("  Thomas Wren is the Crown's assessor ");
        set.changes().getFirst().setKind(ItemKind.DOES_NOT_KNOW);

        ChangeApplier.apply(state, set);

        KnowledgeItem item = state.findByName("Fiora").getItems().getLast();
        assertEquals("Thomas Wren is the Crown's assessor", item.getText());
        assertEquals(ItemKind.DOES_NOT_KNOW, item.getKind());
    }

    @Test
    void aCharacterThatWasDeletedMeanwhileIsSkipped() {
        KnowledgeState state = state();
        ChangeSet set = ChangeSetBuilder.build(state, request(state), answer(new CharacterChanges("Fiora", List.of(),
                List.of(new Addition("knows", "x", "assessor", 1)))));
        state.deleteCharacter(state.findByName("Fiora").getId());
        ChangeApplier.Result result = ChangeApplier.apply(state, set);
        assertEquals(0, result.added());
    }

    // --- committing a run ---

    private static final List<Part> BRANCH = List.of(P1, P2, new Part("m3", null, "Fiora", "Third part."));

    @Test
    void whatIsAppliedSurvivesTheMovingOfTheCursor() {
        // this was a bug: the cursor was moved after the changes were applied and its first step took them back
        KnowledgeState state = state();
        ExtractionPrompt.Request request = ExtractionPrompt.build(state, "", List.of(P1, P2), null);
        ChangeSet set = ChangeSetBuilder.build(state, request, answer(new CharacterChanges("Fiora",
                List.of(new Retraction(2, "Thomas Wren", 1)),
                List.of(new Addition("knows", "Thomas Wren is an assessor", "assessor of the Crown", 1)))));

        ChangeApplier.Result result = RunCommit.commit(state, BRANCH, List.of(P1, P2), set, null, 5L);

        assertNotNull(result);
        assertEquals(1, result.retracted());
        assertEquals(1, result.added());
        assertEquals("m2", state.getCursor());
        TrackedCharacter fiora = state.findByName("Fiora");
        assertFalse(fiora.getItems().get(1).isActive());
        assertEquals("Thomas Wren is an assessor", fiora.getItems().getLast().getText());
        assertEquals(List.of("Thomas", "Mara"), fiora.getMet().stream().map(m -> m.getName()).toList());

        // and a later pass over the same branch keeps them
        assertEquals(0, com.github.enerccio.marginalia.extensions.knowledgetracker.process.BranchProcessor.align(state, BRANCH));
        assertEquals(2, fiora.activeItems().size());
        assertEquals(2, fiora.oldItems().size());
    }

    @Test
    void aRunThatIsNotNextInLineIsNotCommitted() {
        KnowledgeState state = state();
        ChangeSet nothing = new ChangeSet(List.of(), List.of());
        assertNull(RunCommit.commit(state, BRANCH, List.of(P2), nothing, null, 1L));
        assertNull(state.getCursor());
        assertNull(RunCommit.commit(state, BRANCH, List.of(P1, P2, new Part("other", null, "x", "y")), nothing, null, 1L));
    }

    @Test
    void aSecondRunContinuesWhereTheFirstStopped() {
        KnowledgeState state = state();
        ChangeSet nothing = new ChangeSet(List.of(), List.of());
        assertNotNull(RunCommit.commit(state, BRANCH, List.of(P1), nothing, null, 1L));
        assertNull(RunCommit.commit(state, BRANCH, List.of(P1), nothing, null, 2L), "P1 is read already");
        assertNotNull(RunCommit.commit(state, BRANCH, List.of(P2, BRANCH.get(2)), nothing, null, 3L));
        assertEquals("m3", state.getCursor());
    }
}
