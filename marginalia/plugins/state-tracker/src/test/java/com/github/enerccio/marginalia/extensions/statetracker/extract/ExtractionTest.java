package com.github.enerccio.marginalia.extensions.statetracker.extract;

import com.github.enerccio.marginalia.extensions.statetracker.extract.StateChange.Action;
import com.github.enerccio.marginalia.extensions.statetracker.model.*;
import com.github.enerccio.marginalia.extensions.storytracker.process.BranchProcessor;
import com.github.enerccio.marginalia.extensions.storytracker.process.Part;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The request, the checks of the answer, applying the changes and taking them back with a reprocess.
 */
class ExtractionTest {

    private final StateBook book = new StateBook();
    private final Category weapon = new Category("weapon", "Weapon at hand", CategoryKind.SINGLE, "The weapon in hand.");
    private final Category carrying = new Category("carrying", "Carrying", CategoryKind.LIST, "Things carried.");
    private final Category wearing = new Category("wearing", "Wearing", CategoryKind.DESCRIPTION, "");
    private final TrackedCharacter fiora = new TrackedCharacter("Fiora");
    private final TrackedCharacter mara = new TrackedCharacter("Mara");
    private final TrackedCharacter thomas = new TrackedCharacter("Thomas");
    private final StateItem sword;
    private final StateItem rope;
    private final StateItem lantern;
    private final StateItem letter;
    private final List<Part> parts = List.of(
            new Part("p1", "Fiora", "Fiora, Mara", "She pushed the lantern into Mara's hands. Fiora drew an axe."),
            new Part("p2", "Mara", "Mara", "The current took the letter from her belt."));

    ExtractionTest() {
        wearing.setStrict(false);
        book.getCategories().addAll(List.of(weapon, carrying, wearing));
        book.getCharacters().addAll(List.of(fiora, mara, thomas));
        fiora.setCategories(List.of("weapon", "carrying"));
        mara.setCategories(List.of("carrying", "wearing"));
        thomas.setCategories(List.of("carrying"));
        sword = fiora.set(weapon, "short sword", 1);
        rope = fiora.set(carrying, "rope", 1);
        lantern = fiora.set(carrying, "lantern", 1);
        letter = mara.set(carrying, "sealed letter", 1);
    }

    private StatePrompt.Request request() {
        return StatePrompt.build(book, "", parts, null, null);
    }

    private static StateAnswer answer(String name, StateAnswer.Change... changes) {
        return new StateAnswer(null, List.of(new StateAnswer.CharacterChanges(name, List.of(changes))));
    }

    private static StateAnswer.Change change(String category, String action, String value, Integer item, String evidence, int message) {
        return new StateAnswer.Change(category, action, value, item, evidence, message);
    }

    @Test
    void theRequestHasThePresentCharactersWithTheirCategoriesNumbered() {
        StatePrompt.Request request = request();
        assertEquals(List.of(fiora, mara), request.included());
        assertEquals(List.of(sword, rope, lantern, letter), request.numbering().items());
        String user = request.user();
        assertTrue(user.contains("Fiora (present in the messages: #1)"));
        assertTrue(user.contains("Mara (present in the messages: #1, #2)"));
        assertTrue(user.contains("  Weapon at hand (single, strict): The weapon in hand.\n    1. short sword"));
        assertTrue(user.contains("    3. lantern"));
        assertTrue(user.contains("  Wearing (description, not strict)\n    (nothing)"));
        assertFalse(user.contains("Thomas"));
        assertTrue(user.contains("#2 (POV Mara; present: Mara)\nThe current took the letter from her belt."));
    }

    @Test
    void nobodyTrackedIsPresent() {
        assertTrue(StatePrompt.build(book, "", List.of(new Part("p9", "", "Thomas", "text")), null, null).included().stream()
                .noneMatch(c -> c == fiora));
        assertTrue(StatePrompt.build(book, "", List.of(new Part("p9", "", "Guard", "text")), null, null).isEmpty());
    }

    @Test
    void theChangesOfAnAnswerAreChecked() {
        StatePrompt.Request request = request();
        StateChangeBuilder.Result result = StateChangeBuilder.build(book, request, new StateAnswer(null, List.of(
                new StateAnswer.CharacterChanges("Fiora", List.of(
                        change("Weapon at hand", "set", "axe", null, "Fiora drew an axe", 1),
                        change("Carrying", "remove", null, 3, "pushed the lantern into Mara's hands", 1))),
                new StateAnswer.CharacterChanges("mara", List.of(
                        change("carrying", "add", "lantern", null, "pushed the lantern into Mara's hands", 1),
                        change("Carrying", "remove", null, 4, "The current took the letter", 2))))));
        assertEquals(List.of(), result.dropped());
        List<StateChange> changes = result.changes();
        assertEquals(4, changes.size());
        assertEquals(Action.SET, changes.get(0).getAction());
        assertEquals(sword.getId(), changes.get(0).getItemId());
        assertEquals("short sword", changes.get(0).getItemText());
        assertEquals("p1", changes.get(0).getPart());
        assertTrue(changes.get(0).isEvidenceFound());
        assertEquals(lantern.getId(), changes.get(1).getItemId());
        assertEquals(Action.ADD, changes.get(2).getAction());
        assertEquals(letter.getId(), changes.get(3).getItemId());
        assertEquals("p2", changes.get(3).getPart());
    }

    @Test
    void unusableChangesAreDropped() {
        StateChangeBuilder.Result result = StateChangeBuilder.build(book, request(), answer("Fiora",
                change("Weapon at hand", "add", "axe", null, "Fiora drew an axe", 1),           // a single category has no add
                change("Mood", "set", "angry", null, "Fiora drew an axe", 1),                   // not a tracked category
                change("Weapon at hand", "set", "Short sword.", null, "Fiora drew an axe", 1),  // the value it has already
                change("Carrying", "remove", null, 4, "the letter", 2),                         // Mara's value
                change("Carrying", "remove", null, 1, "the lantern", 1),                        // a value of another category
                change("Carrying", "add", "lamp", null, "lantern", 7),                          // no message 7
                change("Carrying", "add", "lamp", null, " ", 1)));                              // no evidence
        assertEquals(List.of(), result.changes());
        assertEquals(7, result.dropped().size());
    }

    @Test
    void aCharacterThatWasNotAskedAboutIsDropped() {
        StateChangeBuilder.Result result = StateChangeBuilder.build(book, request(), answer("Thomas",
                change("Carrying", "add", "bread", null, "Fiora drew an axe", 1)));
        assertTrue(result.changes().isEmpty());
        assertEquals(List.of("Not asked about: Thomas"), result.dropped());
    }

    @Test
    void aQuoteThatIsNotInThePartIsFlagged() {
        StateChangeBuilder.Result result = StateChangeBuilder.build(book, request(), answer("Fiora",
                change("Weapon at hand", "set", "axe", null, "she raised the axe", 1)));
        assertFalse(result.changes().getFirst().isEvidenceFound());
    }

    @Test
    void appliedChangesRememberThePartAndAReprocessTakesThemBack() {
        StatePrompt.Request request = request();
        List<StateChange> changes = StateChangeBuilder.build(book, request, answer("Fiora",
                change("Weapon at hand", "set", "axe", null, "Fiora drew an axe", 1),
                change("Carrying", "remove", null, 3, "pushed the lantern", 1),
                change("Carrying", "add", "map", null, "Fiora drew", 1))).changes();
        changes.get(2).setApply(false);
        StateApplier.Result result = StateApplier.apply(book, changes, 2);
        assertEquals(new StateApplier.Result(2, 0, 1), result);
        assertEquals("axe", fiora.active("weapon").getFirst().getText());
        assertEquals("p1", fiora.active("weapon").getFirst().getSourceMessage());
        assertFalse(sword.isActive());
        assertEquals("p1", sword.getEndedBy());
        assertFalse(lantern.isActive());
        assertEquals(List.of(rope), fiora.active("carrying"));

        // a value the user writes by hand is not taken back
        StateItem bread = fiora.set(carrying, "bread", 3);

        int taken = book.rewind(uuid -> uuid.equals("p1"));
        assertEquals(3, taken);
        assertEquals(List.of(sword), fiora.active("weapon"));
        assertTrue(fiora.active("carrying").containsAll(List.of(rope, lantern, bread)));
        assertTrue(fiora.getItems().stream().noneMatch(item -> item.getText().equals("axe")));
    }

    @Test
    void aChangeWhoseValueIsGoneMeanwhileIsSkipped() {
        List<StateChange> changes = StateChangeBuilder.build(book, request(), answer("Fiora",
                change("Carrying", "remove", null, 3, "pushed the lantern", 1))).changes();
        fiora.remove(lantern, 5);
        assertEquals(new StateApplier.Result(0, 1, 0), StateApplier.apply(book, changes, 6));
    }

    @Test
    void theCursorMovesOverTheRunAndARegenerateTakesBackItsPart() {
        BranchProcessor.process(book, parts, 2, null, 1, 2);
        assertEquals("p2", book.getCursor());
        List<StateChange> changes = StateChangeBuilder.build(book, StatePrompt.build(book, "", parts, null, null), answer("Mara",
                change("Carrying", "remove", null, 4, "The current took the letter", 2))).changes();
        StateApplier.apply(book, changes, 2);
        assertFalse(letter.isActive());
        assertTrue(BranchProcessor.reprocessFrom(book, parts, "p2") > 0);
        assertEquals("p1", book.getCursor());
        assertTrue(letter.isActive());
    }
}
