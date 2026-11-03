package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ChangeLog.Outcome;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ExtractionAnswer.Addition;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ExtractionAnswer.CharacterChanges;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ExtractionAnswer.Retraction;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ChangeLogEntry;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ItemKind;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeItem;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.OldReason;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.TrackedCharacter;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.Part;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What an automatic run logs, and taking it back.
 */
class ChangeLogTest {

    private static final Part P1 = new Part("m1", "Fiora", "Fiora, Thomas",
            "The bell rang. \"Thomas Wren, assessor of the Crown,\" he said. Fiora's hands went still on the dough.");
    private static final List<Part> BRANCH = List.of(P1, new Part("m2", null, "Fiora", "Second."));

    private static KnowledgeState state() {
        KnowledgeState state = new KnowledgeState();
        TrackedCharacter fiora = new TrackedCharacter("Fiora");
        fiora.getItems().add(KnowledgeItem.initial("Thomas Wren: she has never met him", ItemKind.DOES_NOT_KNOW));
        state.getCharacters().add(fiora);
        return state;
    }

    /** A run over m1 that retracts the first item and adds one, applied with logging. */
    private static KnowledgeState afterARun(boolean logged) {
        KnowledgeState state = state();
        ExtractionPrompt.Request request = ExtractionPrompt.build(state, "", List.of(P1), null);
        ChangeSet set = ChangeSetBuilder.build(state, request, new ExtractionAnswer(List.of(new CharacterChanges("Fiora",
                List.of(new Retraction(1, "Thomas Wren", 1)),
                List.of(new Addition("knows", "Thomas Wren is an assessor", "assessor of the Crown", 1))))));
        assertNotNull(RunCommit.commit(state, BRANCH, List.of(P1), set, null, 7L, logged));
        return state;
    }

    @Test
    void anAutomaticRunLogsWhatItApplied() {
        KnowledgeState state = afterARun(true);
        List<ChangeLogEntry> log = state.getChangeLog();
        assertEquals(2, log.size());
        ChangeLogEntry retracted = log.stream().filter(e -> e.getType() == ChangeLogEntry.Type.RETRACTED).findFirst().orElseThrow();
        ChangeLogEntry added = log.stream().filter(e -> e.getType() == ChangeLogEntry.Type.ADDED).findFirst().orElseThrow();
        assertEquals("Fiora", added.getCharacterName());
        assertEquals("Thomas Wren is an assessor", added.getText());
        assertEquals("m1", added.getMessageUuid());
        assertEquals("assessor of the Crown", added.getEvidence());
        assertEquals(7L, added.getTime());
        assertEquals("Thomas Wren: she has never met him", retracted.getText());
        assertTrue(ChangeLog.canRevert(state, added));
        assertTrue(ChangeLog.canRevert(state, retracted));
    }

    @Test
    void aReviewedRunLogsNothing() {
        assertTrue(afterARun(false).getChangeLog().isEmpty());
    }

    @Test
    void revertingAnAdditionKeepsTheItemAsOldKnowledge() {
        KnowledgeState state = afterARun(true);
        ChangeLogEntry added = state.getChangeLog().stream().filter(e -> e.getType() == ChangeLogEntry.Type.ADDED).findFirst().orElseThrow();

        assertEquals(Outcome.REVERTED, ChangeLog.revert(state, added.getId()));

        KnowledgeItem item = state.findByName("Fiora").findItem(added.getItemId());
        assertFalse(item.isActive());
        assertEquals(OldReason.REJECTED, item.getOldReason());
        assertTrue(added.isReverted());
        assertFalse(ChangeLog.canRevert(state, added));
        assertEquals(Outcome.ALREADY_HANDLED, ChangeLog.revert(state, added.getId()), "twice does nothing");
    }

    @Test
    void revertingARetractionMakesTheItemActiveAgain() {
        KnowledgeState state = afterARun(true);
        ChangeLogEntry retracted = state.getChangeLog().stream().filter(e -> e.getType() == ChangeLogEntry.Type.RETRACTED).findFirst().orElseThrow();

        assertEquals(Outcome.REVERTED, ChangeLog.revert(state, retracted.getId()));

        KnowledgeItem item = state.findByName("Fiora").findItem(retracted.getItemId());
        assertTrue(item.isActive());
        assertNull(item.getRetractedAt());
    }

    @Test
    void anItemTheUserDealtWithMeanwhileIsNotTouched() {
        KnowledgeState state = afterARun(true);
        ChangeLogEntry added = state.getChangeLog().stream().filter(e -> e.getType() == ChangeLogEntry.Type.ADDED).findFirst().orElseThrow();
        ChangeLogEntry retracted = state.getChangeLog().stream().filter(e -> e.getType() == ChangeLogEntry.Type.RETRACTED).findFirst().orElseThrow();
        TrackedCharacter fiora = state.findByName("Fiora");

        // the user retracted the added one by hand, and restored the retracted one then retracted it again by hand
        fiora.findItem(added.getItemId()).retractByHand();
        KnowledgeItem other = fiora.findItem(retracted.getItemId());
        other.restore();
        other.retractByHand();

        assertEquals(Outcome.ALREADY_HANDLED, ChangeLog.revert(state, added.getId()));
        assertEquals(Outcome.ALREADY_HANDLED, ChangeLog.revert(state, retracted.getId()));
        assertFalse(other.isActive(), "left as the user left it");
    }

    @Test
    void anItemThatIsGoneCannotBeReverted() {
        KnowledgeState state = afterARun(true);
        ChangeLogEntry added = state.getChangeLog().stream().filter(e -> e.getType() == ChangeLogEntry.Type.ADDED).findFirst().orElseThrow();
        state.findByName("Fiora").deleteItem(added.getItemId());
        assertEquals(Outcome.GONE, ChangeLog.revert(state, added.getId()));

        ChangeLogEntry other = state.getChangeLog().stream().filter(e -> e.getType() == ChangeLogEntry.Type.RETRACTED).findFirst().orElseThrow();
        state.deleteCharacter(state.findByName("Fiora").getId());
        assertEquals(Outcome.GONE, ChangeLog.revert(state, other.getId()));
        assertEquals(Outcome.UNKNOWN, ChangeLog.revert(state, "nope"));
    }

    @Test
    void theLogKeepsTheNewestEntries() {
        KnowledgeState state = new KnowledgeState();
        for (int i = 0; i < ChangeLog.KEEP + 5; i++) {
            ChangeLog.add(state, new ArrayList<>(List.of(new ChangeLogEntry(i, ChangeLogEntry.Type.ADDED, "c", "C", "i" + i, "t" + i,
                    ItemKind.KNOWS, "m", null))));
        }
        assertEquals(ChangeLog.KEEP, state.getChangeLog().size());
        assertEquals("t5", state.getChangeLog().getFirst().getText());
        ChangeLog.clear(state);
        assertTrue(state.getChangeLog().isEmpty());
    }
}
