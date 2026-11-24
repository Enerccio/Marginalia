package com.github.enerccio.marginalia.extensions.statetracker.model;

import com.github.enerccio.marginalia.extensions.storytracker.process.ProcessedPart;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The counting of the story clock: the marks of the processed parts folded in their order, the fixed points of the user, and taking
 * back what the model read in dropped parts.
 */
class StoryClockTest {

    private final StateBook book = new StateBook();
    private final StoryClock clock = book.getClock();

    StoryClockTest() {
        clock.setEnabled(true);
        clock.setAnchor("the awakening");
        clock.setStartDay(3);
        clock.setStartTimeOfDay(TimeOfDay.EVENING);
    }

    private void part(String uuid, ClockMark mark) {
        book.getProcessed().add(new ProcessedPart(uuid, "h", 1));
        if (mark != null) {
            clock.read(mark);
        }
    }

    private static ClockMark advance(String part, Integer days, TimeOfDay to) {
        return new ClockMark(part, ClockMark.Kind.ADVANCE).days(days).timeOfDay(to);
    }

    @Test
    void theTestedSceneCountsAsExpected() {
        part("p1", null);                                                       // camp, nothing said
        part("p2", advance("p2", 1, TimeOfDay.MORNING));                        // the next morning
        part("p3", new ClockMark("p3", ClockMark.Kind.FLASHBACK));              // a childhood memory
        part("p4", advance("p4", 3, null));                                     // three days later
        part("p5", advance("p5", 0, TimeOfDay.NIGHT));                          // by nightfall
        assertEquals(new StoryTime(3, TimeOfDay.EVENING, null, false), clock.upTo(book, "p1"));
        assertEquals(new StoryTime(4, TimeOfDay.MORNING, null, false), clock.upTo(book, "p3"));
        assertEquals(new StoryTime(7, null, null, false), clock.upTo(book, "p4"));
        assertEquals(new StoryTime(7, TimeOfDay.NIGHT, null, false), clock.now(book));
        assertEquals(7, book.today());
        assertEquals(5, clock.timeline(book).size());
    }

    @Test
    void aStatedDayOverridesAnEstimate() {
        part("p1", advance("p1", 14, null).approximate(true));
        assertTrue(clock.now(book).approximate());
        assertEquals(17, clock.now(book).day());
        part("p2", new ClockMark("p2", ClockMark.Kind.DAY).day(40));
        assertEquals(new StoryTime(40, null, null, false), clock.now(book));
    }

    @Test
    void onlyTheTimeOfDayMovesTheDayWhenThatTimeHasPassed() {
        part("p1", advance("p1", null, TimeOfDay.MORNING));                     // evening → morning: the next day
        assertEquals(new StoryTime(4, TimeOfDay.MORNING, null, false), clock.now(book));
        part("p2", advance("p2", null, TimeOfDay.EVENING));                     // morning → evening: the same day
        assertEquals(new StoryTime(4, TimeOfDay.EVENING, null, false), clock.now(book));
    }

    @Test
    void zeroDaysToAnEarlierTimeOfDayIsTheNextDay() {
        part("p1", advance("p1", 0, TimeOfDay.DAWN));                           // evening, "she woke at first light": the next day
        assertEquals(new StoryTime(4, TimeOfDay.DAWN, null, false), clock.now(book));
        part("p2", advance("p2", 0, TimeOfDay.AFTERNOON));                      // dawn → afternoon: the same day
        assertEquals(new StoryTime(4, TimeOfDay.AFTERNOON, null, false), clock.now(book));
        part("p3", advance("p3", 0, null));                                     // an hour later: nothing to roll over on
        assertEquals(new StoryTime(4, TimeOfDay.AFTERNOON, null, false), clock.now(book));
        part("p4", advance("p4", 1, TimeOfDay.MORNING));                        // "the next morning": one day, not two
        assertEquals(new StoryTime(5, TimeOfDay.MORNING, null, false), clock.now(book));
    }

    @Test
    void datesAndDaysMoveTogether() {
        clock.setStartDay(1);
        clock.setStartTimeOfDay(TimeOfDay.NIGHT);
        clock.setStartDate("1453-04-06");
        part("p1", new ClockMark("p1", ClockMark.Kind.DATE).date("1453-04-20"));
        assertEquals(new StoryTime(15, null, LocalDate.of(1453, 4, 20), false), clock.now(book));
        part("p2", advance("p2", 2, TimeOfDay.EVENING));
        assertEquals(new StoryTime(17, TimeOfDay.EVENING, LocalDate.of(1453, 4, 22), false), clock.now(book));
        part("p3", new ClockMark("p3", ClockMark.Kind.DAY).day(20));
        assertEquals(LocalDate.of(1453, 4, 25), clock.now(book).date());
    }

    @Test
    void aFixedPointStaysWhenThePartIsReadAgainAndTheClockCountsOnFromIt() {
        part("p1", advance("p1", 2, null));
        part("p2", advance("p2", 1, null));
        clock.fix("p1", new ClockMark("p1", ClockMark.Kind.SET).day(10).timeOfDay(TimeOfDay.NOON));
        assertEquals(new StoryTime(11, null, null, false), clock.now(book));
        clock.read(advance("p1", 5, null));
        assertTrue(clock.getMarks().get("p1").isManual(), "a reading does not replace the fixed point");

        // a reprocess from p1: what the model read there and later goes, the fixed point stays
        assertEquals(1, book.rewind(part -> part.equals("p1") || part.equals("p2")));
        assertTrue(clock.getMarks().containsKey("p1"));
        assertFalse(clock.getMarks().containsKey("p2"));
        clock.fix("p1", null);
        assertTrue(clock.getMarks().isEmpty());
    }

    @Test
    void theWordsOfThePromptAndTheBlock() {
        StoryTime time = new StoryTime(14, TimeOfDay.EVENING, LocalDate.of(1453, 4, 20), false);
        assertEquals("Day 14 after the awakening, evening. Calendar date: 1453-04-20.", time.forRequest("the awakening"));
        assertEquals("Story time: day 14 after the awakening, evening (20 April 1453).", time.forBlock("the awakening"));
        assertEquals("About day 3. No calendar date.", new StoryTime(3, null, null, true).forRequest(""));
    }

    @Test
    void timesOfDayAreReadLeniently() {
        assertEquals(TimeOfDay.EVENING, TimeOfDay.parse(" Dusk "));
        assertEquals(TimeOfDay.NIGHT, TimeOfDay.parse("midnight"));
        assertEquals(TimeOfDay.DAWN, TimeOfDay.parse("early-morning"));
        assertNull(TimeOfDay.parse("teatime"));
        assertNull(TimeOfDay.parse(null));
    }
}
