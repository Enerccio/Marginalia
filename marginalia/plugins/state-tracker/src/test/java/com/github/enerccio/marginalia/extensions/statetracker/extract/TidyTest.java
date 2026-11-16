package com.github.enerccio.marginalia.extensions.statetracker.extract;

import com.github.enerccio.marginalia.extensions.statetracker.extract.StateChange.Action;
import com.github.enerccio.marginalia.extensions.statetracker.extract.TidyAnswer.CharacterTidy;
import com.github.enerccio.marginalia.extensions.statetracker.extract.TidyAnswer.Drop;
import com.github.enerccio.marginalia.extensions.statetracker.extract.TidyAnswer.Merge;
import com.github.enerccio.marginalia.extensions.statetracker.extract.TidyAnswer.Update;
import com.github.enerccio.marginalia.extensions.statetracker.model.*;
import com.github.enerccio.marginalia.extensions.statetracker.service.StateExtractionService;
import com.github.enerccio.marginalia.extensions.storytracker.process.Part;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The tidy: the request, the checks of the answer, applying it (to the history as tidied, locked values left alone), taking it
 * back with a reprocess, and when the automatic tidy is due.
 */
class TidyTest {

    private final StateBook book = new StateBook();
    private final Category mood = new Category("mood", "Mood", CategoryKind.SINGLE, "How the character feels.");
    private final Category carrying = new Category("carrying", "Carrying", CategoryKind.LIST, "Things carried.");
    private final Category injuries = new Category("injuries", "Injuries", CategoryKind.LIST, "");
    private final TrackedCharacter fiora = new TrackedCharacter("Fiora");
    private final TrackedCharacter mara = new TrackedCharacter("Mara");
    private final StateItem angry;
    private final StateItem rope;
    private final StateItem coil;
    private final StateItem cup;
    private final StateItem scar;
    private final StateItem tired;
    private final List<Part> recent = List.of(
            new Part("p1", "Fiora", "Fiora", "Fiora laughed at the joke and coiled her rope."),
            new Part("p2", "Fiora", "Fiora", "Days passed on the road."));

    TidyTest() {
        book.getCategories().addAll(List.of(mood, carrying, injuries));
        book.getCharacters().addAll(List.of(fiora, mara));
        fiora.setCategories(List.of("mood", "carrying", "injuries"));
        mara.setCategories(List.of("mood"));
        angry = fiora.set(mood, "angry", 1);
        rope = fiora.set(carrying, "rope", 1);
        coil = fiora.set(carrying, "coil of rope", 1);
        cup = fiora.set(carrying, "a cup of tea", 1);
        scar = fiora.set(injuries, "scar on the cheek", 1);
        scar.setLocked(true);
        tired = mara.set(mood, "tired", 1);
    }

    private StatePrompt.Request request() {
        return TidyPrompt.build(book, recent, null, null);
    }

    private static TidyAnswer answer(String name, List<Drop> drop, List<Update> update, List<Merge> merge) {
        return new TidyAnswer(List.of(new CharacterTidy(name, drop, update, merge)));
    }

    @Test
    void theRequestHasThePresentCharactersAndOnlyTheUnlockedValuesAreNumbered() {
        StatePrompt.Request request = request();
        assertEquals(List.of(fiora), request.included());
        assertEquals(List.of(angry, rope, coil, cup), request.numbering().items());
        String user = request.user();
        assertTrue(user.contains("Fiora (present in the messages: #1, #2)"));
        assertTrue(user.contains("  Mood (single): How the character feels.\n    1. angry"));
        assertTrue(user.contains("    4. a cup of tea"));
        assertTrue(user.contains("  Injuries (list)\n    - (stays) scar on the cheek"));
        assertFalse(user.contains("Mara"));
        assertTrue(user.contains("#2 (POV Fiora; present: Fiora)\nDays passed on the road."));
        assertEquals(StateDefaults.tidySystem(), request.system());
    }

    @Test
    void aCharacterWithOnlyLockedValuesOrAbsentIsNotAsked() {
        angry.setLocked(true);
        rope.setLocked(true);
        coil.setLocked(true);
        cup.setLocked(true);
        assertTrue(request().isEmpty());
        assertTrue(TidyPrompt.build(book, List.of(), null, null).isEmpty());
    }

    @Test
    void dropUpdateAndMergeBecomeChanges() {
        StateChangeBuilder.Result result = TidyChangeBuilder.build(book, request(), answer("Fiora",
                List.of(new Drop(4, "the meal is long over")),
                List.of(new Update(1, "cheerful", "she laughed")),
                List.of(new Merge(List.of(2, 3), "coil of rope", "the same rope"))), "p2");
        assertEquals(List.of(), result.dropped());
        List<StateChange> changes = result.changes();
        assertEquals(4, changes.size());
        assertTrue(changes.stream().allMatch(StateChange::isTidy));
        assertTrue(changes.stream().allMatch(c -> "p2".equals(c.getPart())));

        assertEquals(Action.REMOVE, changes.get(0).getAction());
        assertEquals(cup.getId(), changes.get(0).getItemId());
        assertEquals("the meal is long over", changes.get(0).getEvidence());
        assertEquals(0, changes.get(0).getPartNumber());

        assertEquals(Action.SET, changes.get(1).getAction());
        assertEquals(angry.getId(), changes.get(1).getItemId());
        assertEquals("cheerful", changes.get(1).getValue());

        assertEquals(Action.REPLACE, changes.get(2).getAction());
        assertEquals(rope.getId(), changes.get(2).getItemId());
        assertEquals("coil of rope", changes.get(2).getValue());
        assertEquals(Action.REMOVE, changes.get(3).getAction());
        assertEquals(coil.getId(), changes.get(3).getItemId());
        assertEquals("merged: the same rope", changes.get(3).getEvidence());
    }

    @Test
    void unusableChangesAreDropped() {
        StateChangeBuilder.Result result = TidyChangeBuilder.build(book, request(), answer("Fiora",
                List.of(new Drop(9, "no such value"), new Drop(4, " "), new Drop(1, "faded"), new Drop(1, "twice")),
                List.of(new Update(2, "Rope.", "the same text"), new Update(3, " ", "no value")),
                List.of(new Merge(List.of(2), "rope", "one entry"), new Merge(List.of(1, 2), "angry rope", "two categories"))), "p2");
        assertEquals(1, result.changes().size(), "only the first drop of value 1");
        assertEquals(angry.getId(), result.changes().getFirst().getItemId());
        assertEquals(7, result.dropped().size());
    }

    @Test
    void aCharacterThatWasNotAskedAboutIsDropped() {
        StateChangeBuilder.Result result = TidyChangeBuilder.build(book, request(), answer("Mara",
                List.of(new Drop(1, "rested")), null, null), "p2");
        assertTrue(result.changes().isEmpty());
        assertEquals(List.of("Not asked about: Mara"), result.dropped());
    }

    @Test
    void appliedTheValuesGoToTheHistoryAsTidiedAndAReprocessBringsThemBack() {
        List<StateChange> changes = TidyChangeBuilder.build(book, request(), answer("Fiora",
                List.of(new Drop(4, "the meal is long over")),
                List.of(new Update(1, "cheerful", "she laughed")),
                List.of(new Merge(List.of(2, 3), "coiled rope", "the same rope"))), "p2").changes();
        StateApplier.Result result = StateApplier.apply(book, changes, 5);
        assertEquals(4, result.applied());

        assertEquals(List.of("cheerful"), fiora.active("mood").stream().map(StateItem::getText).toList());
        assertEquals(List.of("coiled rope"), fiora.active("carrying").stream().map(StateItem::getText).toList());
        for (StateItem ended : List.of(angry, rope, coil, cup)) {
            assertFalse(ended.isActive());
            assertEquals(StateItem.EndReason.TIDIED, ended.getEndReason());
            assertEquals("p2", ended.getEndedBy());
        }
        assertTrue(scar.isActive());
        // what the tidy wrote is marked as the tidy's, not as read from the story; an edit makes it the user's
        StateItem cheerful = fiora.active("mood").getFirst();
        assertTrue(cheerful.isFromTidy());
        assertTrue(fiora.active("carrying").getFirst().isFromTidy());
        assertFalse(scar.isFromTidy());
        cheerful.setSourceMessage(null);
        assertFalse(cheerful.isFromTidy());
        cheerful.setSourceMessage("p2");

        // a reprocess from the last part takes the tidy back
        book.rewind("p2"::equals);
        assertEquals(List.of("angry"), fiora.active("mood").stream().map(StateItem::getText).toList());
        assertEquals(List.of("rope", "coil of rope", "a cup of tea"), fiora.active("carrying").stream().map(StateItem::getText).toList());
    }

    @Test
    void aValueLockedOrRemovedMeanwhileIsSkipped() {
        List<StateChange> changes = TidyChangeBuilder.build(book, request(), answer("Fiora",
                List.of(new Drop(4, "the meal is long over")),
                List.of(new Update(1, "cheerful", "she laughed")), null), "p2").changes();
        cup.setLocked(true);
        fiora.remove(angry, 3);
        StateApplier.Result result = StateApplier.apply(book, changes, 5);
        assertEquals(0, result.applied());
        assertEquals(2, result.skipped());
        assertTrue(cup.isActive());
        assertTrue(fiora.active("mood").isEmpty(), "the update does not bring back a value the user removed");
    }

    @Test
    void theAutomaticTidyIsDueAfterKPartsOfARunThatIsNotGuided() {
        book.getSettings().setTidyEveryParts(3);
        book.partsRead(2);
        assertFalse(StateExtractionService.tidyDue(book, false));
        book.partsRead(1);
        assertTrue(StateExtractionService.tidyDue(book, false));
        assertFalse(StateExtractionService.tidyDue(book, true), "a guided run never starts a tidy");
        book.getSettings().setAutoTidy(false);
        assertFalse(StateExtractionService.tidyDue(book, false));
        book.getSettings().setAutoTidy(true);
        book.tidied();
        assertFalse(StateExtractionService.tidyDue(book, false));
    }
}
