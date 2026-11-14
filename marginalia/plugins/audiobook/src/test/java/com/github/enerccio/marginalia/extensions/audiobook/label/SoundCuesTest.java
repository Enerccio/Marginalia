package com.github.enerccio.marginalia.extensions.audiobook.label;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SoundCuesTest {

    /** The sound each speech of the text gets, by its words; every speech is Mira's unless a label says otherwise. */
    private static Map<String, String> cues(String text, Map<String, String> speakerOf) {
        List<Piece> pieces = PieceSplitter.split(text);
        Map<Integer, PieceLabel> labels = new HashMap<>();
        for (Piece piece : pieces) {
            String speaker = piece.kind() == Piece.Kind.SPEECH ? speakerOf.getOrDefault(piece.text(), "Mira") : "Narrator";
            labels.put(piece.id(), new PieceLabel(piece.id(), speaker, Delivery.NEUTRAL, 0.5, ""));
        }
        labels.put(-1, new PieceLabel(-1, "Tomas", Delivery.NEUTRAL, 0.5, ""));
        Map<Integer, String> found = SoundCues.find(pieces, labels, Tags.HIGGS);
        Map<String, String> byText = new HashMap<>();
        for (Piece piece : pieces) {
            if (found.containsKey(piece.id())) {
                byText.put(piece.text(), found.get(piece.id()));
            }
        }
        return byText;
    }

    private static Map<String, String> cues(String text) {
        return cues(text, Map.of());
    }

    @Test
    void anAttributionAfterTheSpeechGivesItsSound() {
        assertEquals(Map.of("You look funny,", "giggle"), cues("\"You look funny,\" she giggled."));
        assertEquals(Map.of("Finally,", "moan"), cues("\"Finally,\" she moaned, sinking into the water."));
        assertEquals(Map.of("Don't stop,", "pant"), cues("\"Don't stop,\" she panted against his neck."));
        assertEquals(Map.of("Please,", "moan"), cues("\"Please,\" she whimpered."));
    }

    @Test
    void narrationBeforeTheSpeechGivesItsSoundToTheSpeechAfterIt() {
        assertEquals(Map.of("Careful!", "laugh"), cues("She burst out laughing. \"Careful!\""));
        assertEquals(Map.of("Finally.", "moan"), cues("That night Mira sank into the hot bath with a long, contented moan. \"Finally.\""));
        assertEquals(Map.of("He never even noticed,", "cry"), cues("She sat alone at the table and cried quietly. \"He never even noticed,\" she whispered."));
        assertEquals(Map.of("My back.", "groan"), cues("Tomas sank to the floor, groaning. \"My back.\"", Map.of("My back.", "Tomas")),
                "the floor is no subject here: a comma stands between");
        assertEquals(Map.of("Give me... a second.", "pant"), cues("He was still out of breath. \"Give me... a second.\""));
    }

    @Test
    void anIntimateSceneHasItsMoansAndPanting() {
        String text = "Her breath hitched and a soft moan escaped her lips. \"Yes...\" "
                + "\n\n\"Like that?\" he asked."
                + "\n\nShe arched against him, panting. \"Don't you dare stop.\""
                + "\n\n\"God,\" she moaned his name into the pillow.";
        assertEquals(Map.of("Yes...", "moan", "Don't you dare stop.", "pant", "God,", "moan"), cues(text, Map.of("Like that?", "Tomas")));
    }

    @Test
    void whatOnlyLooksLikeASoundIsNone() {
        assertEquals(Map.of(), cues("\"Stop!\" she cried."), "a bare cried is an exclamation");
        assertEquals(Map.of(), cues("\"It rains again,\" she moaned about the weather."), "a complaint");
        assertEquals(Map.of(), cues("The wind moaned outside. \"We should go.\""));
        assertEquals(Map.of(), cues("The floorboards groaned under him. \"Who's there?\""));
        assertEquals(Map.of(), cues("She stifled a giggle. \"Sorry.\""));
        assertEquals(Map.of(), cues("She didn't laugh. \"That's not funny.\""));
        assertEquals(Map.of(), cues("She fought back a sob. \"I'm fine.\""));
        assertEquals(Map.of(), cues("She tried not to laugh. \"Really?\""));
        assertEquals(Map.of(), cues("He pulled on his pants. \"Ready.\""), "trousers");
    }

    @Test
    void theSoundOfAnotherNamedSpeakerIsNotGivenToThisOne() {
        assertEquals(Map.of(), cues("Tomas groaned. \"Get up,\" Mira said.", Map.of("Get up,", "Mira")));
        assertEquals(Map.of("Get up,", "groan"), cues("Mira groaned. \"Get up,\" she said.", Map.of("Get up,", "Mira")));
    }

    @Test
    void theFirstSoundCountsAndPantingBeatsAGasp() {
        assertEquals(Map.of("Wait...", "pant"), cues("She was gasping for air, then laughed. \"Wait...\""));
        assertEquals(Map.of("Wait...", "sigh"), cues("She sighed and coughed. \"Wait...\""));
    }

    @Test
    void aSoundTheModelCanNotVoiceIsNotGiven() {
        List<Piece> pieces = PieceSplitter.split("\"Mmh,\" she moaned.");
        Map<Integer, PieceLabel> labels = Map.of(0, new PieceLabel(0, "Mira", Delivery.NEUTRAL, 0.5, ""));

        assertEquals(Map.of(), SoundCues.find(pieces, labels, Tags.TURBO));
    }
}
