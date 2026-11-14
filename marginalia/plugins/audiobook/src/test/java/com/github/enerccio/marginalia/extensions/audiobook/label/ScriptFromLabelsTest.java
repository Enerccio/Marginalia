package com.github.enerccio.marginalia.extensions.audiobook.label;

import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.audiobook.script.*;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ScriptFromLabelsTest {

    private static final String TEXT = "# The Arch\n\nMarta coughed at the dust. \"Nobody has been here,\" she said. \"Years.\"\n\nJonas grinned. \"Then you won't mind.\" He sighed.";

    private static PieceLabel label(int id, String speaker, Delivery delivery, double intensity, String tagged) {
        return new PieceLabel(id, speaker, delivery, intensity, tagged);
    }

    private static Map<Integer, PieceLabel> labels(PieceLabel... all) {
        Map<Integer, PieceLabel> map = new HashMap<>();
        for (PieceLabel l : all) {
            map.put(l.id(), l);
        }
        return map;
    }

    private static final ScriptFromLabels.VoiceChooser VOICE = (speaker, delivery, language) -> VoiceRef.predefined(speaker + ".wav");

    private PartScript build(Map<Integer, PieceLabel> labels, BackendConfig backend, Set<String> models) {
        return ScriptFromLabels.build(TEXT, PieceSplitter.split(TEXT), labels, backend, VOICE, models, 42);
    }

    @Test
    void narrationIsTheNarratorAndSpeechIsTheSpeakerOfItsLabel() {
        List<Piece> pieces = PieceSplitter.split(TEXT);
        // pieces: 0 heading, 1 narration, 2 speech, 3 "she said.", 4 speech, 5 narration, 6 speech, 7 narration
        assertEquals(8, pieces.size());

        PartScript script = build(labels(label(2, "Marta", Delivery.CALM, 0.4, ""), label(4, "Marta", Delivery.SAD, 0.6, ""),
                label(6, "Jonas", Delivery.AMUSED, 0.5, "")), new BackendConfig(), Set.of());

        assertEquals(List.of("Narrator", "Narrator", "Marta", "Narrator", "Marta", "Narrator", "Jonas", "Narrator"),
                script.entries().stream().map(ScriptEntry::speaker).toList());
        assertEquals("Jonas.wav", script.entries().get(6).voice().id());
        assertEquals("Narrator.wav", script.entries().get(0).voice().id());
        assertEquals("The Arch", script.entries().getFirst().text());
        assertEquals(com.github.enerccio.marginalia.extensions.audiobook.script.Hashes.sha256(TEXT), script.sourceHash());
    }

    @Test
    void plainNarrationIsTheNarratorWhateverTheModelSaysAndOnlyItalicsCanBeACharacter() {
        // the model takes the character the narration is about ("She watched as Percy...") for the speaker; a thought in italics is the character's
        String text = "She watched as Percy crossed the room.\n\n*Not again,* she thought.\n\nShe breathed *deeply*.\n\n_Dear Luna, come home._";
        List<Piece> pieces = PieceSplitter.split(text);
        assertEquals(4, pieces.size());

        PartScript script = ScriptFromLabels.build(text, pieces, labels(label(0, "Luna", Delivery.CALM, 0.4, ""), label(1, "Luna", Delivery.CALM, 0.4, ""),
                label(2, "Luna", Delivery.CALM, 0.4, ""), label(3, "Percy", Delivery.CALM, 0.4, "")), new BackendConfig(), VOICE, Set.of(), 1);

        assertEquals(List.of("Narrator", "Luna", "Narrator", "Percy"), script.entries().stream().map(ScriptEntry::speaker).toList());
    }

    @Test
    void narrationThatTheModelGivesToACharacterIsReadByTheCharacter() {
        // a letter: no quotation marks, so the code calls it narration; the model knows it is Snape's
        String text = "The handwriting was unmistakable.\n\n*Albus, you are a liability.*\n\n*S. Snape*\n\nPeter lowered the letter.";
        List<Piece> pieces = PieceSplitter.split(text);
        assertTrue(pieces.stream().allMatch(p -> p.kind() == Piece.Kind.NARRATION));
        assertEquals(4, pieces.size());

        PartScript script = ScriptFromLabels.build(text, pieces, labels(label(1, "Snape", Delivery.CALM, 0.4, ""), label(2, " snape ", Delivery.CALM, 0.3, ""),
                label(0, "narrator", Delivery.NEUTRAL, 0.2, "")), new BackendConfig(), VOICE, Set.of(), 1);

        assertEquals(List.of("Narrator", "Snape", "snape", "Narrator"), script.entries().stream().map(ScriptEntry::speaker).toList(),
                "the model's name is kept as it gave it, any case of 'narrator' is the narrator, a missing label is the narrator");
        assertEquals("Snape.wav", script.entries().get(1).voice().id());
        // the words are the book's, the asterisks stay for the spoken form to remove
        assertEquals("*Albus, you are a liability.*", pieces.get(1).text());
    }

    @Test
    void theChooserIsToldWhoSpeaksHowAndInWhichLanguage() {
        java.util.List<String> asked = new java.util.ArrayList<>();
        ScriptFromLabels.VoiceChooser spy = (speaker, delivery, language) -> {
            asked.add(speaker + "/" + delivery + "/" + language);
            return VoiceRef.predefined("x.wav");
        };
        String text = "She nodded. \"Quiet.\" 「静かに」";

        ScriptFromLabels.build(text, PieceSplitter.split(text), labels(label(1, "Marta", Delivery.WHISPER, 0.5, ""), label(2, "Marta", Delivery.SHOUT, 0.9, "")),
                new BackendConfig(), spy, Set.of(BackendConfig.MODEL_MULTILINGUAL), 1);

        assertEquals(List.of("Narrator/NEUTRAL/null", "Marta/WHISPER/null", "Marta/SHOUT/ja"), asked);
    }

    @Test
    void theStudioKeepsTheWordsOfThePieceTheDeliveryAndTheIntensityOfEverySegment() {
        List<Piece> pieces = PieceSplitter.split(TEXT);
        var studio = ScriptFromLabels.studio(TEXT, pieces, labels(label(2, "Marta", Delivery.WHISPER, 0.8, "")), new BackendConfig(), VOICE, Set.of(), 42);

        assertEquals(pieces.size(), studio.segments().size());
        var whispered = studio.segments().get(2);
        assertEquals(pieces.get(2).text(), whispered.source());
        assertEquals("WHISPER", whispered.delivery());
        assertEquals(0.8, whispered.intensity());
        assertFalse(whispered.locked(), "an annotation never locks");
        assertEquals("NEUTRAL", studio.segments().get(0).delivery(), "narration without a label");
        assertEquals(ScriptFromLabels.exaggeration(0.5), 0.65);
        assertEquals(studio.toPartScript().entries(), build(labels(label(2, "Marta", Delivery.WHISPER, 0.8, "")), new BackendConfig(), Set.of()).entries());
    }

    @Test
    void theSpokenFormIsWhatIsReadAndTheBookKeepsItsWords() {
        String text = "# IV. The Arch\n\nHe counted 11 heads. \"Dr. Kos said 3 times.\"\n\n***\n\nNext morning.";
        List<Piece> pieces = PieceSplitter.split(text);

        var studio = ScriptFromLabels.studio(text, pieces, Map.of(), new BackendConfig(), VOICE, Set.of(), 42, SpokenForm.DEFAULT);

        assertEquals(List.of("Chapter four. The Arch", "He counted eleven heads.", "Doctor Kos said three times.", "Next morning."),
                studio.segments().stream().map(seg -> seg.entry().text()).toList());
        assertEquals(List.of("IV. The Arch", "He counted 11 heads.", "Dr. Kos said 3 times.", "Next morning."),
                studio.segments().stream().map(seg -> seg.source()).toList(), "the words of the book are the key of a segment");
        // the scene break is not read: the pause before it is long, and nothing follows the last piece
        assertEquals(ScriptBuilder.PAUSE_SCENE_BREAK, studio.segments().get(2).entry().pauseAfter());
        assertEquals(0, studio.segments().getLast().entry().pauseAfter());
    }

    @Test
    void aSceneBreakAtTheEndMakesNoPause() {
        String text = "The end.\n\n***";
        var studio = ScriptFromLabels.studio(text, PieceSplitter.split(text), Map.of(), new BackendConfig(), VOICE, Set.of(), 42, SpokenForm.DEFAULT);

        assertEquals(1, studio.segments().size());
        assertEquals(0, studio.segments().getFirst().entry().pauseAfter());
    }

    @Test
    void aMissingOrOddLabelIsTheNarratorReadingNeutrally() {
        PartScript script = build(labels(label(2, "  ", Delivery.SHOUT, 3.0, "")), new BackendConfig(), Set.of());

        assertEquals("Narrator", script.entries().get(2).speaker(), "a blank speaker");
        assertEquals("Narrator", script.entries().get(4).speaker(), "no label at all");
        assertEquals(8, script.entries().size(), "no piece is lost");
    }

    @Test
    void aTaggedTextIsTakenOnlyWhenItIsThePieceWithAllowedTags() {
        BackendConfig turbo = new BackendConfig();
        // 6: "Then you won't mind." - valid tag; 4: "Years." - a changed word; 2: a tag that is not allowed
        PartScript script = build(labels(
                label(6, "Jonas", Delivery.AMUSED, 0.5, "[chuckle] Then you won't mind."),
                label(4, "Marta", Delivery.SAD, 0.5, "Years and years."),
                label(2, "Marta", Delivery.CALM, 0.5, "[whistle] Nobody has been here,")), turbo, Set.of());

        assertEquals("[chuckle] Then you won't mind.", script.entries().get(6).text());
        assertEquals("Years.", script.entries().get(4).text(), "a changed word: the original is read");
        assertEquals("Nobody has been here,", script.entries().get(2).text(), "a tag the engine does not know");
    }

    @Test
    void tagsAreForTurboOnlyAndNarrationNeverHasThem() {
        BackendConfig original = new BackendConfig();
        original.setModel(BackendConfig.MODEL_ORIGINAL);
        PartScript script = build(labels(label(6, "Jonas", Delivery.AMUSED, 0.5, "[chuckle] Then you won't mind."),
                label(1, "Narrator", Delivery.NEUTRAL, 0.5, "[sigh] Marta coughed at the dust.")), original, Set.of());

        assertEquals("Then you won't mind.", script.entries().get(6).text());
        assertEquals("Marta coughed at the dust.", script.entries().get(1).text());
    }

    @Test
    void intensityBecomesExaggerationOnlyWhereTheModelHasOne() {
        PartScript turbo = build(labels(label(2, "Marta", Delivery.SHOUT, 1.0, "")), new BackendConfig(), Set.of());
        assertEquals(0.5, turbo.entries().get(2).exaggeration(), "a constant for Turbo: the cache must not miss for nothing");

        BackendConfig original = new BackendConfig();
        original.setModel(BackendConfig.MODEL_ORIGINAL);
        PartScript script = build(labels(label(2, "Marta", Delivery.SHOUT, 1.0, ""), label(4, "Marta", Delivery.WHISPER, 0.2, "")), original, Set.of());
        assertEquals(0.9, script.entries().get(2).exaggeration());
        assertEquals(0.5, script.entries().get(4).exaggeration());
    }

    @Test
    void pausesFollowThePositionOfThePiece() {
        PartScript script = build(Map.of(), new BackendConfig(), Set.of());
        List<Double> pauses = script.entries().stream().map(ScriptEntry::pauseAfter).toList();

        assertEquals(ScriptFromLabels.PAUSE_AFTER_HEADING, pauses.get(0));
        assertEquals(ScriptFromLabels.PAUSE_DEFAULT, pauses.get(1), "narration into speech in a paragraph");
        assertEquals(ScriptFromLabels.PAUSE_BEFORE_ATTRIBUTION, pauses.get(2), "speech into \"she said.\"");
        assertEquals(ScriptFromLabels.PAUSE_DEFAULT, pauses.get(3), "narration into speech");
        assertEquals(ScriptFromLabels.PAUSE_BETWEEN_PARAGRAPHS, pauses.get(4), "the last piece of a paragraph");
        assertEquals(0.0, pauses.getLast(), "nothing after the last piece");
    }

    @Test
    void speechIntoAnAttributionIsOneBreath() {
        String text = "\"Cold wall,\" he said. \"Damp.\"";
        List<Piece> pieces = PieceSplitter.split(text);

        PartScript script = ScriptFromLabels.build(text, pieces, Map.of(), new BackendConfig(), VOICE, Set.of(), 1);

        assertEquals(ScriptFromLabels.PAUSE_BEFORE_ATTRIBUTION, script.entries().get(0).pauseAfter());
        assertEquals(ScriptFromLabels.PAUSE_DEFAULT, script.entries().get(1).pauseAfter());
    }

    @Test
    void aPieceInAnotherScriptGoesToTheMultilingualModelWhenThereIsOne() {
        String text = "She whispered. \"息をしてる。\"";
        List<Piece> pieces = PieceSplitter.split(text);
        BackendConfig turbo = new BackendConfig();

        PartScript with = ScriptFromLabels.build(text, pieces, Map.of(), turbo, VOICE, Set.of(BackendConfig.MODEL_TURBO, BackendConfig.MODEL_MULTILINGUAL), 1);
        PartScript without = ScriptFromLabels.build(text, pieces, Map.of(), turbo, VOICE, Set.of(BackendConfig.MODEL_TURBO), 1);

        assertEquals(BackendConfig.MODEL_MULTILINGUAL, with.entries().get(1).model());
        assertEquals("ja", with.entries().get(1).language());
        assertEquals(BackendConfig.MODEL_TURBO, with.entries().get(0).model(), "the English piece stays");
        assertNull(with.entries().get(0).language());
        assertEquals(BackendConfig.MODEL_TURBO, without.entries().get(1).model(), "no backend for it: the model of the book reads it");
        assertNull(without.entries().get(1).language());
    }

    @Test
    void theScriptIsTheSameEveryTimeSoItCanBeCached() {
        Map<Integer, PieceLabel> labels = labels(label(2, "Marta", Delivery.CALM, 0.4, ""));
        assertEquals(build(labels, new BackendConfig(), Set.of()).joinKey(), build(labels, new BackendConfig(), Set.of()).joinKey());
        assertNotEquals(build(labels, new BackendConfig(), Set.of()).joinKey(),
                build(labels(label(2, "Jonas", Delivery.CALM, 0.4, "")), new BackendConfig(), Set.of()).joinKey(), "another speaker, another voice");
    }
}
