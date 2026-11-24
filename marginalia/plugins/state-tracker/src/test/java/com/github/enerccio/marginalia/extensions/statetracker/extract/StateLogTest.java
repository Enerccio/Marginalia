package com.github.enerccio.marginalia.extensions.statetracker.extract;

import com.github.enerccio.marginalia.extensions.statetracker.extract.StateChange.Action;
import com.github.enerccio.marginalia.extensions.statetracker.model.*;
import com.github.enerccio.marginalia.extensions.storytracker.log.TrackerChangeLog.Outcome;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The log of changes applied without asking: what goes into it, and taking an entry back while the state is as the run left it.
 */
class StateLogTest {

    private final StateBook book = new StateBook();
    private final Category weapon = new Category("weapon", "Weapon at hand", CategoryKind.SINGLE, "");
    private final Category carrying = new Category("carrying", "Carrying", CategoryKind.LIST, "");
    private final TrackedCharacter fiora = new TrackedCharacter("Fiora");
    private final StateItem sword;
    private final StateItem lantern;

    StateLogTest() {
        book.getCategories().addAll(List.of(weapon, carrying));
        book.getCharacters().add(fiora);
        fiora.setCategories(List.of("weapon", "carrying"));
        sword = fiora.set(weapon, "short sword", 1);
        lantern = fiora.set(carrying, "lantern", 1);
    }

    private StateChange change(Category category, Action action, String value, StateItem item) {
        return new StateChange(fiora.getId(), "Fiora", category.getId(), category.getName(), action, value,
                item == null ? null : item.getId(), item == null ? null : item.getText(), "quote", 1, "p1", true);
    }

    /** A set of the weapon, the lantern removed and a rope added, applied without asking. */
    private List<StateLogEntry> run(boolean logged) {
        StateApplier.apply(book, List.of(
                change(weapon, Action.SET, "axe", sword),
                change(carrying, Action.REMOVE, null, lantern),
                change(carrying, Action.ADD, "rope", null)), 5, logged);
        return book.getChangeLog();
    }

    private List<String> current(Category category) {
        return fiora.active(category.getId()).stream().map(StateItem::getText).toList();
    }

    @Test
    void onlyWhatIsAppliedWithoutAskingIsLogged() {
        assertTrue(run(false).isEmpty());
    }

    @Test
    void theEntriesSayWhatEndedAndWhatCame() {
        List<StateLogEntry> log = run(true);
        assertEquals(3, log.size());
        assertEquals(StateLogEntry.Action.SET, log.get(0).getAction());
        assertEquals("short sword", log.get(0).getEndedText());
        assertEquals("axe", log.get(0).getText());
        assertEquals("p1", log.get(0).getMessageUuid());
        assertEquals("lantern", log.get(1).getEndedText());
        assertNull(log.get(1).getItemId());
        assertNull(log.get(2).getEndedItemId());
        assertTrue(log.stream().allMatch(e -> StateLog.canRevert(book, e)));
    }

    @Test
    void aRevertIsAnUncheckedChange() {
        List<StateLogEntry> log = run(true);
        for (StateLogEntry entry : List.copyOf(log)) {
            assertEquals(Outcome.REVERTED, StateLog.revert(book, entry.getId()));
        }
        assertEquals(List.of("short sword"), current(weapon));
        assertEquals(List.of("lantern"), current(carrying));
        assertEquals(2, fiora.getItems().size(), "the values the run made are gone, not history");
        assertEquals(Outcome.ALREADY_HANDLED, StateLog.revert(book, log.getFirst().getId()));
    }

    @Test
    void whatTheUserChangedSinceIsNotTouched() {
        List<StateLogEntry> log = run(true);
        StateItem axe = fiora.findItem(log.get(0).getItemId());
        axe.setText("battle axe");                      // edited
        fiora.restore(lantern, carrying, 6);            // brought back by hand
        StateItem rope = fiora.findItem(log.get(2).getItemId());
        fiora.delete(rope);                             // deleted
        assertEquals(Outcome.ALREADY_HANDLED, StateLog.revert(book, log.get(0).getId()));
        assertEquals(Outcome.ALREADY_HANDLED, StateLog.revert(book, log.get(1).getId()));
        assertEquals(Outcome.GONE, StateLog.revert(book, log.get(2).getId()));
        assertEquals(List.of("battle axe"), current(weapon));
    }

    @Test
    void aSingleValueDoesNotComeBackOverAnotherOne() {
        StateApplier.apply(book, List.of(change(weapon, Action.CLEAR, null, sword)), 5, true);
        StateLogEntry clear = book.getChangeLog().getFirst();
        fiora.set(weapon, "dagger", 6);                 // the user gave her another weapon meanwhile
        assertFalse(StateLog.canRevert(book, clear));
        assertEquals(Outcome.ALREADY_HANDLED, StateLog.revert(book, clear.getId()));
        assertEquals(List.of("dagger"), current(weapon));
    }

    @Test
    void theTidyIsLoggedToo() {
        StateApplier.apply(book, List.of(change(carrying, Action.REMOVE, null, lantern).tidy()), 5, true);
        StateLogEntry entry = book.getChangeLog().getFirst();
        assertTrue(entry.isTidy());
        assertEquals(StateItem.EndReason.TIDIED, lantern.getEndReason());
        assertEquals(Outcome.REVERTED, StateLog.revert(book, entry.getId()));
        assertTrue(lantern.isActive());
    }
}
