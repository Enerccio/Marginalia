package com.github.enerccio.marginalia.extensions.audiobook.script;

import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScriptTest {

    private static final VoiceRef NARRATOR = VoiceRef.predefined("Henry.wav");

    private static ScriptEntry entry(String text) {
        return new ScriptEntry("Narrator", text, NARRATOR, BackendConfig.MODEL_TURBO, null, 0.5, 0.5, 0.8, 1.0, 42, 240, "mp3", 0.6);
    }

    @Test
    void voiceRefsRoundTripAsText() {
        assertEquals("predefined:Henry.wav", VoiceRef.predefined("Henry.wav").toString());
        assertEquals(VoiceRef.clip("abc-123"), VoiceRef.parse("clip:abc-123"));
        assertEquals(VoiceRef.predefined("A:b.wav"), VoiceRef.parse("predefined:A:b.wav"));
        assertThrows(IllegalArgumentException.class, () -> VoiceRef.parse("nonsense"));
        assertThrows(IllegalArgumentException.class, () -> VoiceRef.parse("clip:"));
    }

    @Test
    void theKeyCoversWhatChangesTheSoundAndNothingElse() {
        ScriptEntry base = entry("The door was heavy.");
        String key = base.key();
        assertEquals(key, entry("The door was heavy.").key());

        assertNotEquals(key, entry("The door was light.").key());
        assertNotEquals(key, base.withSeed(43).key());
        assertNotEquals(key, new ScriptEntry("Narrator", base.text(), VoiceRef.clip("x"), base.model(), null, 0.5, 0.5, 0.8, 1.0, 42, 240, "mp3", 0.6).key());
        assertNotEquals(key, new ScriptEntry("Narrator", base.text(), NARRATOR, BackendConfig.MODEL_MULTILINGUAL, null, 0.5, 0.5, 0.8, 1.0, 42, 240, "mp3", 0.6).key());
        assertNotEquals(key, new ScriptEntry("Narrator", base.text(), NARRATOR, base.model(), "ja", 0.5, 0.5, 0.8, 1.0, 42, 240, "mp3", 0.6).key());
        assertNotEquals(key, new ScriptEntry("Narrator", base.text(), NARRATOR, base.model(), null, 0.9, 0.5, 0.8, 1.0, 42, 240, "mp3", 0.6).key());
        assertNotEquals(key, new ScriptEntry("Narrator", base.text(), NARRATOR, base.model(), null, 0.5, 0.5, 0.8, 1.0, 42, 240, "wav", 0.6).key());

        // who it is for and the pause after it are not audio
        assertEquals(key, base.withPause(2.5).key());
        assertEquals(key, new ScriptEntry("Someone else", base.text(), NARRATOR, base.model(), null, 0.5, 0.5, 0.8, 1.0, 42, 240, "mp3", 0.6).key());
    }

    @Test
    void theJoinKeyCoversThePiecesTheirOrderAndThePauses() {
        PartScript ab = new PartScript(java.util.List.of(entry("A"), entry("B")), "h");
        assertEquals(ab.joinKey(), new PartScript(java.util.List.of(entry("A"), entry("B")), "other source").joinKey());
        assertNotEquals(ab.joinKey(), new PartScript(java.util.List.of(entry("B"), entry("A")), "h").joinKey());
        assertNotEquals(ab.joinKey(), new PartScript(java.util.List.of(entry("A"), entry("B").withPause(0.1)), "h").joinKey());
        assertNotEquals(ab.joinKey(), new PartScript(java.util.List.of(entry("A")), "h").joinKey());
    }

    @Test
    void aScriptSurvivesJson() {
        PartScript script = new PartScript(java.util.List.of(entry("A"), entry("B").withPause(0)), "h");
        Gson gson = new Gson();
        PartScript again = gson.fromJson(gson.toJson(script), PartScript.class);
        assertEquals(script.joinKey(), again.joinKey());
        assertEquals(2, again.entries().size());
        assertEquals(NARRATOR, again.entries().get(0).voice());
    }

    @Test
    void thePlainScriptHasAPieceForEveryParagraph() {
        BackendConfig config = new BackendConfig();
        String text = "# The Bricked Arch\n\nMarta pushed open\nthe cellar door.\n\n\nNobody had been down there in years.\n";

        PartScript script = ScriptBuilder.narrated(text, NARRATOR, config, 7);

        assertEquals(3, script.entries().size());
        assertEquals("The Bricked Arch", script.entries().get(0).text());
        assertEquals(ScriptBuilder.PAUSE_AFTER_HEADING, script.entries().get(0).pauseAfter());
        assertEquals("Marta pushed open the cellar door.", script.entries().get(1).text());
        assertEquals(ScriptBuilder.PAUSE_AFTER_PARAGRAPH, script.entries().get(1).pauseAfter());
        assertEquals(0, script.entries().get(2).pauseAfter(), "nothing after the last piece");
        assertTrue(script.entries().stream().allMatch(e -> e.seed() == 7 && e.model().equals(config.getModel()) && e.voice().equals(NARRATOR)));
        assertFalse(PartScript.stale(script.sourceHash(), text));
        assertTrue(PartScript.stale(script.sourceHash(), text + "More."));
        assertTrue(PartScript.stale(null, text));
    }

    @Test
    void thePlainScriptIsSpelledForTheEarAndKeepsTheWordsOfTheBookAsSource() {
        String text = "# IV. The Arch\n\nHe counted *11* heads.\n\n* * *\n\nNext.";

        var studio = ScriptBuilder.studio(text, VoiceRef.predefined("Henry.wav"), new BackendConfig(), 42, SpokenForm.DEFAULT);

        assertEquals(java.util.List.of("Chapter four. The Arch", "He counted eleven heads.", "Next."), studio.segments().stream().map(s -> s.entry().text()).toList());
        assertEquals(java.util.List.of("IV. The Arch", "He counted *11* heads.", "Next."), studio.segments().stream().map(StudioSegmentSource::of).toList());
        assertEquals(ScriptBuilder.PAUSE_SCENE_BREAK, studio.segments().get(1).entry().pauseAfter(), "the scene break is a long pause, not words");
        assertEquals(0, studio.segments().get(2).entry().pauseAfter());
    }

    private static final class StudioSegmentSource {
        static String of(com.github.enerccio.marginalia.extensions.audiobook.studio.StudioSegment segment) {
            return segment.source();
        }
    }

    @Test
    void aPartWithoutTextHasNoPieces() {
        assertTrue(ScriptBuilder.narrated("  \n\n ", NARRATOR, new BackendConfig(), 1).entries().isEmpty());
        assertTrue(ScriptBuilder.narrated(null, NARRATOR, new BackendConfig(), 1).entries().isEmpty());
    }
}
