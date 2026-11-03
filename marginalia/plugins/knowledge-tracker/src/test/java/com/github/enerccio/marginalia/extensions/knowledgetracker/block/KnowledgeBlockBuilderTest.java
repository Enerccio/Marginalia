package com.github.enerccio.marginalia.extensions.knowledgetracker.block;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ItemKind;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeItem;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.TrackedCharacter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What goes to the model: only tracked characters that are present, only active items.
 */
class KnowledgeBlockBuilderTest {

    private static KnowledgeState state() {
        KnowledgeState state = new KnowledgeState();
        TrackedCharacter fiora = new TrackedCharacter("Fiora");
        fiora.setAliases(List.of("Fi"));
        fiora.getItems().add(KnowledgeItem.initial("Mara (courier, her friend's sister)", ItemKind.KNOWS));
        fiora.getItems().add(KnowledgeItem.initial("the Guild owes her money.", ItemKind.KNOWS));
        fiora.getItems().add(KnowledgeItem.initial("Thomas is her father", ItemKind.DOES_NOT_KNOW));
        fiora.addMet("Mara", "m1");
        state.getCharacters().add(fiora);
        TrackedCharacter thomas = new TrackedCharacter("Thomas");
        thomas.getItems().add(KnowledgeItem.initial("he has a daughter", ItemKind.KNOWS));
        state.getCharacters().add(thomas);
        return state;
    }

    @Test
    void describesAPresentCharacter() {
        String block = KnowledgeBlockBuilder.build(state(), "Fiora (tired), Mara", null);

        assertEquals(KnowledgeBlockBuilder.HEADER + "\n\n"
                + "Fiora knows: Mara (courier, her friend's sister); the Guild owes her money.\n"
                + "Fiora does not know: Thomas is her father.\n"
                + "Fiora has met: Mara.\n"
                + "Fiora is currently with: Mara.\n"
                + "Fiora has never met or heard of anyone else in this story and knows nothing else of its secrets, "
                + "plans or events.", block);
    }

    @Test
    void itemsThatEndWithAFullStopAreNotJoinedAsDotSemicolon() {
        KnowledgeState state = new KnowledgeState();
        TrackedCharacter ilse = new TrackedCharacter("Ilse");
        ilse.getItems().add(KnowledgeItem.initial("Harry is a rival.", ItemKind.KNOWS));
        ilse.getItems().add(KnowledgeItem.initial("the Guild owes her money...", ItemKind.KNOWS));
        ilse.getItems().add(KnowledgeItem.initial("彼を知っている。", ItemKind.KNOWS));
        state.getCharacters().add(ilse);

        String block = KnowledgeBlockBuilder.build(state, "Ilse", null);

        assertTrue(block.contains("Ilse knows: Harry is a rival; the Guild owes her money; 彼を知っている."), block);
        assertFalse(block.contains(".;"));
    }

    @Test
    void aCharacterThatIsNotPresentIsNotMentionedAtAll() {
        String block = KnowledgeBlockBuilder.build(state(), "Fiora, Mara", null);
        assertFalse(block.contains("Thomas knows"));
        assertFalse(block.contains("Thomas is currently"));
        assertFalse(block.contains("he has a daughter"));
    }

    @Test
    void nothingTrackedPresentGivesAnEmptyBlock() {
        assertEquals("", KnowledgeBlockBuilder.build(state(), "Mara, Ilse", null));
        assertEquals("", KnowledgeBlockBuilder.build(state(), "", null));
        assertEquals("", KnowledgeBlockBuilder.build(state(), null, null));
        assertEquals("", KnowledgeBlockBuilder.build(new KnowledgeState(), "Fiora", null));
    }

    @Test
    void severalCharactersAreInTheOrderOfTheState() {
        String block = KnowledgeBlockBuilder.build(state(), "Thomas; Fi", null);
        assertTrue(block.indexOf("Fiora knows") < block.indexOf("Thomas knows"));
        assertTrue(block.contains("Thomas is currently with: Fi."));
    }

    @Test
    void oldItemsAreNeverSent() {
        KnowledgeState state = state();
        state.findByName("Fiora").getItems().get(2).retract("m3");
        String block = KnowledgeBlockBuilder.build(state, "Fiora", null);
        assertFalse(block.contains("does not know"));
        assertFalse(block.contains("her father"));
    }

    @Test
    void theClosedWorldLineCanBeSwitchedOff() {
        KnowledgeState state = state();
        state.getSettings().setClosedWorld(false);
        assertFalse(KnowledgeBlockBuilder.build(state, "Fiora", null).contains("never met"));
    }

    @Test
    void aCharacterWithNothingKnownStillGetsTheClosedWorldLine() {
        KnowledgeState state = new KnowledgeState();
        state.getCharacters().add(new TrackedCharacter("Ilse"));
        assertEquals(KnowledgeBlockBuilder.HEADER + "\n\nIlse has never met or heard of anyone else in this story and "
                + "knows nothing else of its secrets, plans or events.", KnowledgeBlockBuilder.build(state, "Ilse", null));
    }

    @Test
    void separatorsComeFromTheBookThenTheUserThenTheDefaults() {
        KnowledgeState state = new KnowledgeState();
        assertNull(KnowledgeBlockBuilder.effectiveSeparators(state, null));
        assertNull(KnowledgeBlockBuilder.effectiveSeparators(state, List.of()));
        assertEquals(List.of("&"), KnowledgeBlockBuilder.effectiveSeparators(state, List.of("&")));
        state.getSettings().setSeparators(List.of("and"));
        assertEquals(List.of("and"), KnowledgeBlockBuilder.effectiveSeparators(state, List.of("&")));
    }

    @Test
    void theSeparatorsDecideWhoIsPresent() {
        KnowledgeState state = state();
        assertEquals("", KnowledgeBlockBuilder.build(state, "Fiora and Thomas", List.of(",")));
        String block = KnowledgeBlockBuilder.build(state, "Fiora and Thomas", List.of("and"));
        assertTrue(block.contains("Fiora knows"));
        assertTrue(block.contains("Thomas knows"));
    }
}
