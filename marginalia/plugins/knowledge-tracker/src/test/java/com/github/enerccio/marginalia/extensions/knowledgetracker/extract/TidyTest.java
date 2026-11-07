package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ExtractionAnswer.CharacterChanges;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ExtractionAnswer.Retraction;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ExtractionPrompt.Request;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.TidyAnswer.CharacterTidy;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.TidyAnswer.Drop;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.TidyAnswer.Merge;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.*;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.Part;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The tidy pass: what it may touch (learned, active, not locked), what the request shows, the checking of the answer, applying it and
 * taking it back, and the lock (which only the tidy pass respects: the story can still contradict a locked item).
 */
class TidyTest {

    /** Peter with: a manual item, an initial item, a locked learned one, an old learned one and four learned ones he can lose. */
    private static KnowledgeState state() {
        KnowledgeState state = new KnowledgeState();
        state.getProcessed().add(new ProcessedPart("m1", "h", 1));
        state.getProcessed().add(new ProcessedPart("m2", "h", 2));
        state.getProcessed().add(new ProcessedPart("m3", "h", 3));
        TrackedCharacter peter = new TrackedCharacter("Peter");
        peter.getItems().add(KnowledgeItem.manual("written by hand", ItemKind.KNOWS));
        peter.getItems().add(KnowledgeItem.initial("the start", ItemKind.KNOWS));
        KnowledgeItem locked = KnowledgeItem.learned("Miyu betrayed him", ItemKind.KNOWS, "m1", "q");
        locked.setLocked(true);
        peter.getItems().add(locked);
        KnowledgeItem old = KnowledgeItem.learned("an old thing", ItemKind.KNOWS, "m1", "q");
        old.retract("m2");
        peter.getItems().add(old);
        peter.getItems().add(KnowledgeItem.learned("the elevator stalled", ItemKind.KNOWS, "m3", "q"));
        peter.getItems().add(KnowledgeItem.learned("Miyu uses nya comments", ItemKind.KNOWS, "m2", "q"));
        peter.getItems().add(KnowledgeItem.learned("Miyu writes nya in her docs", ItemKind.KNOWS, "m3", "q"));
        peter.getItems().add(KnowledgeItem.learned("Miyu is leaving the company", ItemKind.KNOWS, "m1", "q"));
        state.getCharacters().add(peter);
        TrackedCharacter few = new TrackedCharacter("Ilse");
        few.getItems().add(KnowledgeItem.learned("one thing", ItemKind.KNOWS, "m1", "q"));
        state.getCharacters().add(few);
        return state;
    }

    private static final Part RECENT = new Part("m3", "Peter", "Peter, Miyu", "The elevator shuddered.");

    private static KnowledgeItem item(KnowledgeState state, String text) {
        return state.findByName("Peter").getItems().stream().filter(i -> i.getText().equals(text)).findFirst().orElseThrow();
    }

    // --- what may be touched, what is asked ---

    @Test
    void onlyActiveLearnedUnlockedItemsAreCandidates() {
        KnowledgeState state = state();
        List<String> candidates = state.findByName("Peter").getItems().stream().filter(KnowledgeItem::isTidyCandidate).map(KnowledgeItem::getText).toList();
        assertEquals(List.of("the elevator stalled", "Miyu uses nya comments", "Miyu writes nya in her docs", "Miyu is leaving the company"), candidates);
    }

    @Test
    void theRequestNumbersOnlyTheCandidatesAndShowsTheRestForComparison() {
        KnowledgeState state = state();
        Request request = TidyPrompt.build(state, List.of(RECENT), 3);
        assertEquals(1, request.included().size(), "Ilse has too few items");
        assertTrue(request.user().contains("  1. knows: the elevator stalled"));
        assertTrue(request.user().contains("  4. knows: Miyu is leaving the company"));
        assertTrue(request.user().contains("Items that stay as they are (comparison only):"));
        assertTrue(request.user().contains("- knows: Miyu betrayed him"), "a locked item is shown but not numbered");
        assertTrue(request.user().contains("- knows: written by hand"));
        assertFalse(request.user().contains("an old thing"), "an old item is not shown");
        assertFalse(request.user().contains("Ilse"));
        assertTrue(request.user().contains("[Last parts of the story]"));
        assertEquals(4, request.numbering().items().get(state.findByName("Peter").getId()).size());
    }

    @Test
    void anInheritedSystemMessageReplacesTheBuiltInOne() {
        assertEquals("Tidy my way.", TidyPrompt.build(state(), List.of(RECENT), 3, " Tidy my way. ").system());
        assertEquals(TidyPrompt.SYSTEM, TidyPrompt.build(state(), List.of(RECENT), 3, "").system());
        assertEquals(TidyPrompt.SYSTEM, TidyPrompt.build(state(), List.of(RECENT), 3, null).system());
    }

    @Test
    void nothingToTidyGivesAnEmptyRequest() {
        assertTrue(TidyPrompt.build(state(), List.of(), 9).isEmpty());
        assertTrue(TidyPrompt.build(new KnowledgeState(), List.of(), 2).isEmpty());
    }

    // --- the answer ---

    private static ChangeSet build(KnowledgeState state, CharacterTidy... entries) {
        Request request = TidyPrompt.build(state, List.of(RECENT), 3);
        return TidyChangeSetBuilder.build(state, request.numbering(), new TidyAnswer(List.of(entries)));
    }

    @Test
    void aDropBecomesATidyRetraction() {
        KnowledgeState state = state();
        ChangeSet set = build(state, new CharacterTidy("Peter", List.of(new Drop(1, "shared by the whole scene")), null));
        assertEquals(1, set.changes().size());
        Change change = set.changes().getFirst();
        assertEquals(Change.Type.RETRACT, change.getType());
        assertTrue(change.isTidy());
        assertEquals("the elevator stalled", change.getItemText());
        assertEquals("shared by the whole scene", change.getEvidence());
    }

    @Test
    void aMergeRetractsItsItemsAndAddsOneFromTheEarliestSource() {
        KnowledgeState state = state();
        ChangeSet set = build(state, new CharacterTidy("Peter", null,
                List.of(new Merge(List.of(3, 2), "knows", "Miyu puts nya in her code and docs", "same thing"))));
        assertEquals(3, set.changes().size());
        Change add = set.changes().getLast();
        assertEquals(Change.Type.ADD, add.getType());
        assertEquals("Miyu puts nya in her code and docs", add.getText());
        assertEquals("m2", add.getMessageUuid(), "the earliest of the sources (m2 before m3)");
        assertTrue(set.changes().stream().limit(2).allMatch(c -> c.getType() == Change.Type.RETRACT && c.isTidy()));
    }

    @Test
    void anAnswerThatNamesWhatItMayNotIsDropped() {
        KnowledgeState state = state();
        ChangeSet set = build(state,
                new CharacterTidy("Peter", List.of(new Drop(9, "x"), new Drop(0, "x"), new Drop(1, " "), new Drop(2, "ok"), new Drop(2, "again")),
                        List.of(new Merge(List.of(3), "knows", "t", "r"), new Merge(List.of(3, 3), "knows", "t", "r"),
                                new Merge(List.of(2, 4), "knows", "x", "r"), new Merge(List.of(3, 4), "knows", " ", "r"))),
                new CharacterTidy("Ilse", List.of(new Drop(1, "x")), null),
                new CharacterTidy("Nobody", List.of(new Drop(1, "x")), null));
        // only the drop of item 2 got through; items 3 and 4 were never used (merge with item 2 is refused, the others are broken)
        assertEquals(1, set.changes().size());
        assertEquals("Miyu uses nya comments", set.changes().getFirst().getItemText());
        assertTrue(set.dropped().size() >= 8);
    }

    @Test
    void aMergedItemThatExistsAlreadyIsRefused() {
        KnowledgeState state = state();
        ChangeSet set = build(state, new CharacterTidy("Peter", null,
                List.of(new Merge(List.of(2, 3), "knows", "Miyu is leaving the company", "same"))));
        assertTrue(set.changes().isEmpty());
        assertEquals(1, set.dropped().size());
    }

    // --- applying ---

    @Test
    void applyingATidyMakesTheDroppedOldAndAddsTheMerged() {
        KnowledgeState state = state();
        ChangeSet set = build(state,
                new CharacterTidy("Peter", List.of(new Drop(1, "shared")), List.of(new Merge(List.of(2, 3), "knows", "Miyu puts nya in her work", "same"))));
        ChangeApplier.Result result = ChangeApplier.apply(state, set, true, 9L);

        assertEquals(3, result.retracted());
        assertEquals(1, result.added());
        assertEquals(OldReason.TIDIED, item(state, "the elevator stalled").getOldReason());
        assertNull(item(state, "the elevator stalled").getRetractedAt(), "a reprocess does not restore it");
        KnowledgeItem merged = item(state, "Miyu puts nya in her work");
        assertTrue(merged.isActive());
        assertEquals("m2", merged.getSourceMessage());
        assertTrue(merged.isTidyCandidate(), "it can be tidied again later");
        // what must stay, stays
        assertTrue(item(state, "Miyu betrayed him").isActive());
        assertTrue(item(state, "Miyu is leaving the company").isActive());
        assertTrue(item(state, "written by hand").isActive());
        assertEquals(4, state.getChangeLog().size());
    }

    @Test
    void anItemLockedOrEditedAfterTheRequestIsNotTouched() {
        KnowledgeState state = state();
        ChangeSet set = build(state, new CharacterTidy("Peter", List.of(new Drop(1, "shared"), new Drop(2, "trivia")), null));
        item(state, "the elevator stalled").setLocked(true);
        item(state, "Miyu uses nya comments").edit("Miyu uses nya comments, as she says", ItemKind.KNOWS);

        ChangeApplier.Result result = ChangeApplier.apply(state, set, false, 1L);

        assertEquals(0, result.retracted());
        assertTrue(item(state, "the elevator stalled").isActive());
        assertTrue(item(state, "Miyu uses nya comments, as she says").isActive());
    }

    @Test
    void anUncheckedMergeKeepsNothing() {
        KnowledgeState state = state();
        ChangeSet set = build(state, new CharacterTidy("Peter", null, List.of(new Merge(List.of(2, 3), "knows", "merged", "same"))));
        set.changes().getLast().setApply(false);
        ChangeApplier.apply(state, set, false, 1L);
        assertTrue(state.findByName("Peter").getItems().stream().noneMatch(i -> i.getText().equals("merged")));
    }

    @Test
    void aTidyCanBeTakenBackFromTheLog() {
        KnowledgeState state = state();
        ChangeSet set = build(state, new CharacterTidy("Peter", List.of(new Drop(1, "shared")), List.of(new Merge(List.of(2, 3), "knows", "merged text", "same"))));
        ChangeApplier.apply(state, set, true, 1L);
        for (ChangeLogEntry entry : List.copyOf(state.getChangeLog())) {
            assertEquals(ChangeLog.Outcome.REVERTED, ChangeLog.revert(state, entry.getId()), entry.getText());
        }
        assertTrue(item(state, "the elevator stalled").isActive());
        assertTrue(item(state, "Miyu uses nya comments").isActive());
        assertFalse(item(state, "merged text").isActive());
    }

    // --- the lock ---

    @Test
    void theStoryCanStillContradictALockedItem() {
        KnowledgeState state = state();
        // only the numbering of an extraction request is needed: the locked item is number 3 of the active ones
        Request request = ExtractionPrompt.build(state, "", List.of(RECENT), null);
        int number = state.findByName("Peter").activeItems().indexOf(item(state, "Miyu betrayed him")) + 1;
        ExtractionAnswer answer = new ExtractionAnswer(List.of(new CharacterChanges("Peter", List.of(new Retraction(number, "The elevator shuddered.", 1)), List.of())));
        ChangeSet set = ChangeSetBuilder.build(state, request, answer);
        assertEquals(1, set.changes().size(), "memory loss: a locked item is retracted by the story");
        ChangeApplier.apply(state, set);
        assertFalse(item(state, "Miyu betrayed him").isActive());
        assertEquals(OldReason.RETRACTED, item(state, "Miyu betrayed him").getOldReason());
    }

    @Test
    void theLockAndTheSettingsSurviveStorageAndOldDataReadsAsUnlocked() {
        Gson gson = new Gson();
        KnowledgeState state = gson.fromJson(gson.toJson(state()), KnowledgeState.class);
        assertTrue(item(state, "Miyu betrayed him").isLocked());
        assertFalse(item(state, "the elevator stalled").isLocked());
        assertEquals(10, state.getSettings().getTidyContextParts());
        assertEquals(8, state.getSettings().getTidyMinItems());

        KnowledgeState old = gson.fromJson("{\"settings\":{\"partsPerRun\":2},\"characters\":[{\"id\":\"c\",\"name\":\"A\",\"items\":[{\"id\":\"i\",\"text\":\"t\",\"origin\":\"LEARNED\"}]}]}",
                KnowledgeState.class);
        assertFalse(old.getCharacters().getFirst().getItems().getFirst().isLocked());
        assertEquals(10, old.getSettings().getTidyContextParts());
        assertEquals(8, old.getSettings().getTidyMinItems());
    }
}
