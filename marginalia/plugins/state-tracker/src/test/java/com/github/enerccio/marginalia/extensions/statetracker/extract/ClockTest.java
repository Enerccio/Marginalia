package com.github.enerccio.marginalia.extensions.statetracker.extract;

import com.github.enerccio.marginalia.extensions.statetracker.block.StateBlockBuilder;
import com.github.enerccio.marginalia.extensions.statetracker.extract.StateAnswer.ClockReport;
import com.github.enerccio.marginalia.extensions.statetracker.extract.StateChange.Action;
import com.github.enerccio.marginalia.extensions.statetracker.model.*;
import com.github.enerccio.marginalia.extensions.storytracker.process.Part;
import com.github.enerccio.marginalia.extensions.storytracker.process.ProcessedPart;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The story clock in the extraction: the request, the checks of what the model reported, storing it with a run, the story day of the
 * values, the ages the tidy sees and the time in the block.
 */
class ClockTest {

    private final StateBook book = new StateBook();
    private final Category carrying = new Category("carrying", "Carrying", CategoryKind.LIST, "Things carried.");
    private final TrackedCharacter fiora = new TrackedCharacter("Fiora");
    private final List<Part> parts = List.of(
            new Part("p1", "Fiora", "Fiora", "The next morning the fog lay thick on the river."),
            new Part("p2", "Mara", "Mara", "Three days later they reached the walls of Kessa."));

    ClockTest() {
        book.getCategories().add(carrying);
        book.getCharacters().add(fiora);
        fiora.setCategories(List.of("carrying"));
        fiora.set(carrying, "rope", 1);
        book.getClock().setEnabled(true);
        book.getClock().setAnchor("the awakening");
        book.getClock().setStartDay(3);
        book.getClock().setStartTimeOfDay(TimeOfDay.EVENING);
    }

    private StatePrompt.Request request() {
        return StatePrompt.build(book, "", parts, null, null);
    }

    private static ClockReport report(int message, String kind, Integer days, Integer day, String date, String timeOfDay, String evidence) {
        return new ClockReport(message, kind, days, day, date, timeOfDay, null, evidence);
    }

    @Test
    void theRequestHasTheClockAndIsAskedAlsoWithoutATrackedCharacterPresent() {
        StatePrompt.Request request = request();
        assertTrue(request.clock());
        assertTrue(request.user().startsWith("[Clock]\nDay 3 after the awakening, evening. No calendar date.\n\n[Tracked characters]"));
        assertTrue(request.system().contains("The clock. For each message"));
        assertTrue(request.system().contains("\"clock\":[{"));

        StatePrompt.Request nobody = StatePrompt.build(book, "", List.of(new Part("p9", "Mara", "Mara", "Two days passed.")), null, null);
        assertTrue(nobody.included().isEmpty());
        assertFalse(nobody.isEmpty(), "every part can move the clock");
        assertTrue(nobody.user().contains("(none of them is in these messages)"));

        book.getClock().setEnabled(false);
        assertFalse(request().user().contains("[Clock]"));
        assertFalse(request().system().contains("The clock."));
        assertTrue(StatePrompt.build(book, "", List.of(new Part("p9", "Mara", "Mara", "x")), null, null).isEmpty());
    }

    @Test
    void theReportsAreChecked() {
        ClockBuilder.Result result = ClockBuilder.build(request(), List.of(
                report(1, "advance", 1, null, null, "Morning", "The next morning"),
                report(2, "advance", 3, null, null, null, "Three days later they reached"),
                report(2, "advance", 1, null, null, null, "twice"),                  // named twice
                report(3, "advance", 1, null, null, null, "x"),                      // no message 3
                report(1, "none", null, null, null, null, null)));                  // none is no change
        assertEquals(2, result.changes().size());
        ClockMark first = result.changes().getFirst().getMark();
        assertEquals("p1", first.getPart());
        assertEquals(TimeOfDay.MORNING, first.getTimeOfDay());
        assertTrue(result.changes().getFirst().isEvidenceFound());
        assertEquals(2, result.dropped().size());

        StatePrompt.Request request = request();
        // each alone: negative days, too many days, day 0, a date that is not one, no evidence, an unknown kind
        for (ClockReport bad : List.of(
                report(1, "advance", -2, null, null, null, "The next morning"),
                report(1, "advance", 5000, null, null, null, "The next morning"),
                report(1, "day", null, 0, null, null, "The next morning"),
                report(1, "date", null, null, "the 20th", null, "The next morning"),
                report(1, "advance", 1, null, null, null, " "),
                report(1, "yesterday", null, null, null, null, "The next morning"))) {
            ClockBuilder.Result one = ClockBuilder.build(request, List.of(bad));
            assertTrue(one.changes().isEmpty(), bad.toString());
            assertEquals(1, one.dropped().size(), bad.toString());
        }
        assertTrue(ClockBuilder.build(request, List.of(report(1, "advance", null, null, null, null, "The next morning"))).changes().isEmpty(),
                "an advance that says nothing is no change");
    }

    @Test
    void aRunStoresTheClockAndTheValuesRememberTheirDay() {
        StatePrompt.Request request = request();
        List<ClockChange> clock = ClockBuilder.build(request, List.of(
                report(1, "advance", 1, null, null, "morning", "The next morning"),
                report(2, "advance", 3, null, null, null, "Three days later"))).changes();
        book.getClock().fix("p2", new ClockMark("p2", ClockMark.Kind.SET).day(30));
        book.getClock().read(new ClockMark("p1", ClockMark.Kind.DAY).day(99));      // an older reading of p1

        parts.forEach(part -> book.getProcessed().add(new ProcessedPart(part.uuid(), "h", 1)));
        ClockBuilder.commit(book, parts, clock);
        assertEquals(ClockMark.Kind.ADVANCE, book.getClock().getMarks().get("p1").getKind(), "the new reading replaces the old");
        assertTrue(book.getClock().getMarks().get("p2").isManual(), "the fixed point stays");
        assertEquals(30, book.getClock().now(book).day());

        StateChange add = new StateChange(fiora.getId(), "Fiora", "carrying", "Carrying", Action.ADD, "lantern", null, null, "fog", 1, "p1", true);
        StateApplier.apply(book, List.of(add), 5);
        StateItem lantern = fiora.active("carrying").getLast();
        assertEquals(4, lantern.getStoryDay(), "the day after its part");
        assertNull(fiora.active("carrying").getFirst().getStoryDay(), "set before the clock was on");
    }

    @Test
    void theTidySeesHowLongAgoAValueWasSet() {
        parts.forEach(part -> book.getProcessed().add(new ProcessedPart(part.uuid(), "h", 1)));
        book.getClock().read(new ClockMark("p2", ClockMark.Kind.DAY).day(12));
        fiora.active("carrying").getFirst().setStoryDay(3);
        StatePrompt.Request request = TidyPrompt.build(book, List.of(parts.get(0)), null, null);
        assertTrue(request.user().startsWith("[Clock]\nDay 12 after the awakening. No calendar date."));
        assertTrue(request.user().contains("1. rope (set on day 3, 9 days ago)"), request.user());
    }

    @Test
    void theBlockStartsWithTheStoryTime() {
        book.getProcessed().add(new ProcessedPart("p1", "h", 1));
        book.getClock().read(new ClockMark("p1", ClockMark.Kind.ADVANCE).days(1).timeOfDay(TimeOfDay.MORNING));
        assertEquals("Story time: day 4 after the awakening, morning.", StateBlockBuilder.build(book, "Mara"));
        assertTrue(StateBlockBuilder.build(book, "Fiora").startsWith("Story time: day 4 after the awakening, morning.\n\nThe current state"));
        book.getClock().setEnabled(false);
        assertEquals("", StateBlockBuilder.build(book, "Mara"));
    }
}
