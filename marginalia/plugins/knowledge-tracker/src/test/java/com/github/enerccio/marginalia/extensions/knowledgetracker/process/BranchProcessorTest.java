package com.github.enerccio.marginalia.extensions.knowledgetracker.process;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ItemKind;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeItem;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.MetPerson;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.TrackedCharacter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The processor follows the active branch: the cursor, the settled parts, the "has met" pass, the rewind.
 */
class BranchProcessorTest {

    private static Part part(String uuid, String present) {
        return new Part(uuid, present, "text of " + uuid);
    }

    private static KnowledgeState state() {
        KnowledgeState state = new KnowledgeState();
        state.getCharacters().add(new TrackedCharacter("Fiora"));
        return state;
    }

    private static List<String> met(KnowledgeState state) {
        return state.findByName("Fiora").getMet().stream().map(MetPerson::getName).toList();
    }

    private static final List<Part> BRANCH = List.of(part("a", "Fiora, Mara"), part("b", "Mara"), part("c", "Fiora (tired), Thomas"));

    @Test
    void readsTheSettledPartsAndNotesWhoMetWhom() {
        KnowledgeState state = state();

        BranchProcessor.Report report = BranchProcessor.process(state, BRANCH, 3, null, 1000L, 100);

        assertEquals(3, report.processed());
        assertEquals(0, report.pending());
        assertEquals("c", state.getCursor());
        assertEquals(List.of("Mara", "Thomas"), met(state));
        assertEquals("a", state.findByName("Fiora").getMet().getFirst().getSourceMessage());
        assertEquals(List.of("a", "b", "c"), state.getProcessed().stream().map(p -> p.getUuid()).toList());
        assertEquals(3, BranchProcessor.cursorPosition(state, BRANCH));
    }

    @Test
    void aCharacterThatIsNotPresentMeetsNobody() {
        KnowledgeState state = state();
        BranchProcessor.process(state, List.of(part("a", "Mara, Thomas")), 1, null, 1L, 100);
        assertEquals(List.of(), met(state));
    }

    @Test
    void theLastPartIsNotReadUntilItIsSettled() {
        KnowledgeState state = state();
        int settled = BranchProcessor.settledCount(BRANCH, false, null);

        BranchProcessor.Report report = BranchProcessor.process(state, BRANCH, settled, null, 1L, 100);

        assertEquals(2, report.processed());
        assertEquals("b", state.getCursor());
        assertEquals(List.of("Mara"), met(state));

        // when something follows it, or the user asks, the rest is read
        assertEquals(1, BranchProcessor.process(state, BRANCH, BranchProcessor.settledCount(BRANCH, true, null), null, 2L, 100).processed());
        assertEquals("c", state.getCursor());
    }

    @Test
    void aPartThatIsBeingReplacedAndTheOnesAfterItAreNotSettled() {
        assertEquals(1, BranchProcessor.settledCount(BRANCH, true, "b"));
        assertEquals(0, BranchProcessor.settledCount(BRANCH, true, "a"));
        assertEquals(3, BranchProcessor.settledCount(BRANCH, true, "elsewhere"));
        assertEquals(2, BranchProcessor.settledCount(BRANCH, false, "c"));
        assertEquals(0, BranchProcessor.settledCount(List.of(), true, null));
    }

    @Test
    void aRunIsLimitedAndSaysHowManyAreLeft() {
        KnowledgeState state = state();
        BranchProcessor.Report first = BranchProcessor.process(state, BRANCH, 3, null, 1L, 2);
        assertEquals(2, first.processed());
        assertEquals(1, first.pending());
        assertEquals("b", state.getCursor());

        BranchProcessor.Report second = BranchProcessor.process(state, BRANCH, 3, null, 2L, 2);
        assertEquals(1, second.processed());
        assertEquals(0, second.pending());
    }

    @Test
    void processingAgainReadsNothingNew() {
        KnowledgeState state = state();
        BranchProcessor.process(state, BRANCH, 3, null, 1L, 100);
        BranchProcessor.Report again = BranchProcessor.process(state, BRANCH, 3, null, 2L, 100);
        assertEquals(0, again.processed());
        assertEquals(0, again.rewound());
        assertEquals(3, state.getProcessed().size());
    }

    @Test
    void theStartPointSkipsTheEarlierParts() {
        KnowledgeState state = state();
        state.setStartMessage("b");

        BranchProcessor.process(state, BRANCH, 3, null, 1L, 100);

        assertEquals(List.of("b", "c"), state.getProcessed().stream().map(p -> p.getUuid()).toList());
        // meeting Mara in part a is before the start point, so it is not known
        assertEquals(List.of("Thomas"), met(state));
    }

    @Test
    void aStartPointThatIsNotOnTheBranchMeansTheStartOfTheBook() {
        KnowledgeState state = state();
        state.setStartMessage("not-here");
        BranchProcessor.process(state, BRANCH, 3, null, 1L, 100);
        assertEquals(3, state.getProcessed().size());
    }

    @Test
    void aSwipeRewindsToTheLastPartBothBranchesShare() {
        KnowledgeState state = state();
        BranchProcessor.process(state, BRANCH, 3, null, 1L, 100);
        List<Part> other = List.of(part("a", "Fiora, Mara"), part("b", "Mara"), part("c2", "Fiora, Ilse"));

        BranchProcessor.Report report = BranchProcessor.process(state, other, 3, null, 2L, 100);

        assertTrue(report.rewound() > 0);
        assertEquals(1, report.processed());
        assertEquals("c2", state.getCursor());
        assertEquals(List.of("Mara", "Ilse"), met(state));
        assertEquals(List.of("a", "b", "c2"), state.getProcessed().stream().map(p -> p.getUuid()).toList());
    }

    @Test
    void aDeletedPartTakesItsStateWithIt() {
        KnowledgeState state = state();
        BranchProcessor.process(state, BRANCH, 3, null, 1L, 100);

        // c is gone from the branch
        int rewound = BranchProcessor.align(state, List.of(part("a", "Fiora, Mara"), part("b", "Mara")));

        assertTrue(rewound > 0);
        assertEquals("b", state.getCursor());
        assertEquals(List.of("Mara"), met(state));
    }

    @Test
    void reprocessFromAPartTakesBackWhatCameFromItAndLater() {
        KnowledgeState state = state();
        BranchProcessor.process(state, BRANCH, 3, null, 1L, 100);
        TrackedCharacter fiora = state.findByName("Fiora");
        fiora.getItems().add(KnowledgeItem.initial("written by hand", ItemKind.KNOWS));
        fiora.getItems().add(KnowledgeItem.learned("learned in c", ItemKind.KNOWS, "c", "q"));

        int changed = BranchProcessor.reprocessFrom(state, BRANCH, "c");

        assertTrue(changed >= 3);
        assertEquals("b", state.getCursor());
        assertEquals(List.of("Mara"), met(state));
        assertEquals(List.of("written by hand"), fiora.getItems().stream().map(KnowledgeItem::getText).toList());

        // and it is read again
        assertEquals(1, BranchProcessor.process(state, BRANCH, 3, null, 2L, 100).processed());
        assertEquals(List.of("Mara", "Thomas"), met(state));
    }

    @Test
    void reprocessFromTheStartPointLeavesNothingProcessed() {
        KnowledgeState state = state();
        state.setStartMessage("b");
        BranchProcessor.process(state, BRANCH, 3, null, 1L, 100);

        BranchProcessor.reprocessFrom(state, BRANCH, "b");

        assertNull(state.getCursor());
        assertEquals(0, BranchProcessor.cursorPosition(state, BRANCH));
        assertTrue(state.getProcessed().isEmpty());
    }

    @Test
    void reprocessFromAPartThatIsNotOnTheBranchDoesNothing() {
        KnowledgeState state = state();
        BranchProcessor.process(state, BRANCH, 3, null, 1L, 100);
        assertEquals(-1, BranchProcessor.reprocessFrom(state, BRANCH, "nope"));
        assertEquals("c", state.getCursor());
    }

    @Test
    void movingTheStartPointBackReadsFromTheNewOneAgain() {
        KnowledgeState state = state();
        state.setStartMessage("b");
        BranchProcessor.process(state, BRANCH, 3, null, 1L, 100);
        state.setStartMessage(null);

        BranchProcessor.Report report = BranchProcessor.process(state, BRANCH, 3, null, 2L, 100);

        assertEquals(3, report.processed());
        assertEquals(List.of("a", "b", "c"), state.getProcessed().stream().map(p -> p.getUuid()).sorted().toList());
        assertEquals(List.of("Mara", "Thomas"), met(state));
    }

    @Test
    void aPartWhoseTextChangedIsReportedAndNotReprocessed() {
        KnowledgeState state = state();
        BranchProcessor.process(state, BRANCH, 3, null, 1L, 100);
        List<Part> edited = List.of(BRANCH.get(0), new Part("b", "Mara", "the author edited this"), BRANCH.get(2));

        assertEquals(List.of("b"), BranchProcessor.changedSinceProcessed(state, edited));
        assertEquals(List.of(), BranchProcessor.changedSinceProcessed(state, BRANCH));
        // nothing is done by itself
        assertEquals(0, BranchProcessor.process(state, edited, 3, null, 2L, 100).processed());
        assertEquals("c", state.getCursor());
    }

    @Test
    void theSeparatorsDecideWhoIsPresent() {
        KnowledgeState state = state();
        BranchProcessor.process(state, List.of(part("a", "Fiora and Thomas")), 1, List.of("and"), 1L, 100);
        assertEquals(List.of("Thomas"), met(state));

        KnowledgeState other = state();
        BranchProcessor.process(other, List.of(part("a", "Fiora and Thomas")), 1, null, 1L, 100);
        assertEquals(List.of(), met(other));
    }

    @Test
    void theHashIsStableAndDependsOnTheText() {
        assertEquals(TextHash.of("x"), TextHash.of("x"));
        assertNotEquals(TextHash.of("x"), TextHash.of("y"));
        assertEquals(TextHash.of(""), TextHash.of(null));
    }
}
