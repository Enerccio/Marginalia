package com.github.enerccio.marginalia.extensions.conlangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.RangeLoop.Mode;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.RangeLoop.PartResult;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.RangeLoop.RangePart;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.RangeLoop.Summary;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Which parts a range holds for each action, and what the loop over them adds up to.
 */
class RangeLoopTest {

    /** Parts 1 to 5, the second and the fourth are translated. */
    private static List<RangePart> story() {
        List<RangePart> parts = new ArrayList<>();
        for (int number = 1; number <= 5; number++) {
            ChatMessage message = new ChatMessage();
            message.setResponse("part " + number);
            parts.add(new RangePart(number, message, number == 2 || number == 4));
        }
        return parts;
    }

    private static List<Integer> numbers(List<RangePart> parts) {
        return parts.stream().map(RangePart::number).toList();
    }

    @Test
    void translatingTheNewOnesTakesTheParts_thatHaveNoTranslation() {
        assertEquals(List.of(1, 3, 5), numbers(RangeLoop.select(Mode.TRANSLATE_NEW, story(), 1, 5)));
    }

    @Test
    void retranslatingAndRevertingTakeTheTranslatedParts() {
        assertEquals(List.of(2, 4), numbers(RangeLoop.select(Mode.RETRANSLATE, story(), 1, 5)));
        assertEquals(List.of(2, 4), numbers(RangeLoop.select(Mode.REVERT, story(), 1, 5)));
    }

    @Test
    void theRangeCutsTheParts() {
        assertEquals(List.of(3), numbers(RangeLoop.select(Mode.TRANSLATE_NEW, story(), 3, 4)));
        assertEquals(List.of(4), numbers(RangeLoop.select(Mode.REVERT, story(), 3, 9)));
        assertTrue(RangeLoop.select(Mode.REVERT, story(), 5, 5).isEmpty());
    }

    @Test
    void theSummaryAddsUpTheParts() {
        List<Integer> told = new ArrayList<>();
        Summary summary = RangeLoop.run(story(), part -> {
            String text = part.getResponse();
            return switch (text) {
                case "part 1" -> PartResult.done(3, 2, 0);
                case "part 2" -> PartResult.nothing();
                case "part 3" -> PartResult.done(1, 0, 1);
                default -> PartResult.failed("the model said no");
            };
        }, new AtomicBoolean(), told::add);

        assertEquals(new Summary(5, 5, 2, 4, 2, 1, 2, "the model said no", false, false), summary);
        assertEquals(List.of(0, 1, 2, 3, 4), told);
    }

    @Test
    void aPartThatThrowsIsAFailureAndTheRunGoesOn() {
        Summary summary = RangeLoop.run(story(), part -> {
            if (part.getResponse().equals("part 2")) {
                throw new IllegalStateException("The part was changed while it was being translated");
            }
            return PartResult.done(1, 0, 0);
        }, new AtomicBoolean(), null);

        assertEquals(4, summary.changed());
        assertEquals(1, summary.failed());
        assertEquals("The part was changed while it was being translated", summary.firstFailure());
        assertFalse(summary.aborted());
    }

    @Test
    void anAbortEndsTheRunAtOnce() {
        Summary summary = RangeLoop.run(story(), part -> PartResult.abort("The book has no language."), new AtomicBoolean(), null);

        assertTrue(summary.aborted());
        assertEquals(1, summary.processed());
        assertEquals("The book has no language.", summary.firstFailure());
    }

    @Test
    void theStopIsLookedAtBeforeEachPart() {
        AtomicBoolean stop = new AtomicBoolean();
        Summary summary = RangeLoop.run(story(), part -> {
            stop.set(true);
            return PartResult.done(1, 0, 0);
        }, stop, null);

        assertTrue(summary.stopped());
        assertEquals(1, summary.processed());
        assertEquals(5, summary.planned());
    }
}
