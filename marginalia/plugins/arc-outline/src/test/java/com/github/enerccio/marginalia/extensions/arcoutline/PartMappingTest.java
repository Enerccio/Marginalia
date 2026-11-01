package com.github.enerccio.marginalia.extensions.arcoutline;

import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService.StoryMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The numbers a model reports for the messages of a range are mapped back to the places in the branch.
 */
class PartMappingTest {

    /** Messages 3 to 5 among those with text; the branch has a part without text before the first and one between. */
    private static final List<StoryMessage> SENT = List.of(
            new StoryMessage(3, 4, "three", 1),
            new StoryMessage(4, 6, "four", 1),
            new StoryMessage(5, 7, "five", 1));

    @Test
    void reportedNumbersCountFromOneInTheRangeSent() {
        assertEquals(4, ArcOutlineService.mapPart(SENT, 1));
        assertEquals(6, ArcOutlineService.mapPart(SENT, 2));
        assertEquals(7, ArcOutlineService.mapPart(SENT, 3));
    }

    @Test
    void aNumberOutsideTheRangeIsNotMapped() {
        assertNull(ArcOutlineService.mapPart(SENT, 0));
        assertNull(ArcOutlineService.mapPart(SENT, 4));
        assertNull(ArcOutlineService.mapPart(SENT, 42));
    }

    @Test
    void theStoryIsNumberedFromOneOnlyWhenAsked() {
        assertTrue(ArcOutlineService.storyText(SENT, true).contains("--- Message 1 ---\nthree"));
        assertTrue(ArcOutlineService.storyText(SENT, true).contains("--- Message 3 ---\nfive"));
        assertTrue(ArcOutlineService.storyText(SENT).contains("--- Message 3 ---\nthree"));
        assertTrue(ArcOutlineService.storyText(SENT, false).contains("--- Message 5 ---\nfive"));
    }
}
