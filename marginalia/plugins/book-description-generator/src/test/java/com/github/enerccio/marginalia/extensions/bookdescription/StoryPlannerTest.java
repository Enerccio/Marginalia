package com.github.enerccio.marginalia.extensions.bookdescription;

import com.github.enerccio.marginalia.extensions.bookdescription.service.StoryPlanner;
import com.github.enerccio.marginalia.extensions.bookdescription.service.StoryPlanner.BranchEntry;
import com.github.enerccio.marginalia.extensions.bookdescription.service.StoryPlanner.Piece;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Which pieces a range of the story is sent as, and how they are divided when they are more than the model takes.
 */
class StoryPlannerTest {

    /** Six messages; the summary of the third covers 1-3, the summary of the sixth covers 4-6. */
    private static List<BranchEntry> branch() {
        List<BranchEntry> branch = new ArrayList<>();
        for (int order = 1; order <= 6; order++) {
            BranchEntry entry = BranchEntry.message(order, order, "text " + order, 10);
            if (order == 3) {
                entry = entry.withSummary("summary of 1-3", 4, 1);
            } else if (order == 6) {
                entry = entry.withSummary("summary of 4-6", 4, 4);
            }
            branch.add(entry);
        }
        return branch;
    }

    @Test
    void withoutSummariesEveryMessageIsAPiece() {
        List<Piece> pieces = StoryPlanner.pieces(branch(), 1, 6, false);
        assertEquals(6, pieces.size());
        assertEquals("Message 1", pieces.getFirst().label());
        assertEquals("text 6", pieces.getLast().text());
    }

    @Test
    void aSummaryStandsInForTheMessagesItCoversWhenAllOfThemAreInTheRange() {
        List<Piece> pieces = StoryPlanner.pieces(branch(), 1, 6, true);
        assertEquals(2, pieces.size());
        assertEquals("Summary of messages 1-3", pieces.get(0).label());
        assertEquals("summary of 1-3", pieces.get(0).text());
        assertEquals("Summary of messages 4-6", pieces.get(1).label());
    }

    @Test
    void aSummaryThatReachesOutsideOfTheRangeIsNotUsed() {
        // 2-6: the first summary covers message 1 as well, the second one is whole
        List<Piece> pieces = StoryPlanner.pieces(branch(), 2, 6, true);
        assertEquals(List.of("Message 2", "Message 3", "Summary of messages 4-6"), pieces.stream().map(Piece::label).toList());
        // 1-5: the second summary ends after the range
        pieces = StoryPlanner.pieces(branch(), 1, 5, true);
        assertEquals(List.of("Summary of messages 1-3", "Message 4", "Message 5"), pieces.stream().map(Piece::label).toList());
    }

    @Test
    void messagesAfterTheNewestSummaryAreSentAsTheyAre() {
        List<BranchEntry> branch = branch();
        branch.add(BranchEntry.message(7, 7, "text 7", 10));
        List<Piece> pieces = StoryPlanner.pieces(branch, 1, 7, true);
        assertEquals(List.of("Summary of messages 1-3", "Summary of messages 4-6", "Message 7"), pieces.stream().map(Piece::label).toList());
    }

    @Test
    void aPartWithoutTextIsSkippedAndNotNumbered() {
        List<BranchEntry> branch = List.of(
                BranchEntry.message(1, 1, "one", 5),
                BranchEntry.message(2, 0, "  ", 0),
                BranchEntry.message(3, 2, "two", 5));
        List<Piece> pieces = StoryPlanner.pieces(branch, 1, 3, false);
        assertEquals(List.of("Message 1", "Message 2"), pieces.stream().map(Piece::label).toList());
    }

    @Test
    void theRenderedStoryHasAHeadingOverEveryPiece() {
        String text = StoryPlanner.render(StoryPlanner.pieces(branch(), 1, 2, false));
        assertEquals("--- Message 1 ---\ntext 1\n\n--- Message 2 ---\ntext 2", text);
    }

    @Test
    void piecesAreDividedInOrderIntoPartsThatFit() {
        List<Piece> pieces = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            pieces.add(new Piece("Message " + i, "text " + i, 100));
        }
        // a piece costs 112 with its heading: three fit in 400
        List<List<Piece>> parts = StoryPlanner.chunk(pieces, 400);
        assertEquals(4, parts.size());
        assertEquals(3, parts.getFirst().size());
        assertEquals("Message 10", parts.getLast().getLast().label());
        for (List<Piece> part : parts) {
            assertTrue(StoryPlanner.cost(part) <= 400);
        }
    }

    @Test
    void aPieceLargerThanTheBudgetIsCutIntoPiecesThatFit() {
        String paragraph = "A sentence that goes on a little. Another sentence follows it.\n\n";
        String text = paragraph.repeat(60);
        Piece big = new Piece("Message 1", text, 3000);
        List<List<Piece>> parts = StoryPlanner.chunk(List.of(big), 500);

        assertTrue(parts.size() > 1);
        StringBuilder joined = new StringBuilder();
        for (List<Piece> part : parts) {
            assertTrue(StoryPlanner.cost(part) <= 500, "a part costs " + StoryPlanner.cost(part));
            for (Piece piece : part) {
                assertTrue(piece.label().startsWith("Message 1 (part "));
                joined.append(piece.text()).append(' ');
            }
        }
        // nothing is lost: all the sentences are there
        assertEquals(60, org.apache.commons.lang3.StringUtils.countMatches(joined, "A sentence that goes on a little."));
        assertEquals(60, org.apache.commons.lang3.StringUtils.countMatches(joined, "Another sentence follows it."));
    }

    @Test
    void aTextWithoutAnySeparatorIsCutHard() {
        Piece big = new Piece("Message 1", "x".repeat(10_000), 5000);
        List<List<Piece>> parts = StoryPlanner.chunk(List.of(big), 600);
        int length = parts.stream().flatMap(List::stream).mapToInt(p -> p.text().length()).sum();
        assertEquals(10_000, length);
        parts.forEach(part -> assertTrue(StoryPlanner.cost(part) <= 600));
    }

    @Test
    void aBudgetThatIsTooSmallIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> StoryPlanner.chunk(List.of(new Piece("Message 1", "text", 1)), 50));
    }
}
