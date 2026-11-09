package com.github.enerccio.marginalia.extensions.audiobook.studio;

import com.github.enerccio.marginalia.extensions.audiobook.script.PartScript;
import com.github.enerccio.marginalia.extensions.audiobook.script.ScriptEntry;
import com.github.enerccio.marginalia.extensions.audiobook.script.VoiceRef;
import com.github.enerccio.marginalia.extensions.audiobook.studio.StudioScript.Status;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudioScriptTest {

    private static ScriptEntry entry(String text, int seed) {
        return new ScriptEntry("Narrator", text, VoiceRef.predefined("Henry.wav"), "chatterbox-turbo", null, 0.5, 0.5, 0.8, 1.0, seed, 240, "mp3", 0.25);
    }

    private static StudioSegment segment(String source, int seed) {
        return StudioSegment.made(entry(source, seed), source, "NEUTRAL", 0.5);
    }

    private static StudioScript script(String hash, StudioSegment... segments) {
        return new StudioScript(List.of(segments), hash, Status.DRAFT);
    }

    @Test
    void aSegmentAlwaysHasItsCurrentSeedAmongItsTakes() {
        assertEquals(List.of(42), segment("a", 42).takes());
        assertEquals(List.of(1, 42), new StudioSegment(entry("a", 42), "a", "NEUTRAL", 0.5, false, List.of(1)).takes());
    }

    @Test
    void anotherTakeKeepsTheEarlierOnesAndLocksTheSegment() {
        StudioSegment taken = segment("a", 42).anotherTake(7);

        assertEquals(7, taken.entry().seed());
        assertEquals(List.of(42, 7), taken.takes());
        assertTrue(taken.locked());
        assertEquals(2, taken.takeKeys().size());
        assertNotEquals(taken.takeKeys().get(0), taken.takeKeys().get(1), "another seed is another piece of audio");
        assertEquals(segment("a", 42).entry().key(), taken.takeKeys().get(0));
    }

    @Test
    void anEditLocksTheSegmentAndKeepsTheWordsOfTheBook() {
        StudioSegment edited = segment("a", 42).edited(entry("a, spelled out", 42));

        assertTrue(edited.locked());
        assertEquals("a", edited.source());
        assertEquals("a, spelled out", edited.entry().text());
    }

    @Test
    void theRendererGetsTheCurrentTakesAndTheOtherTakesAreKept() {
        StudioScript studio = script("h", segment("a", 42), segment("b", 42).anotherTake(7));

        PartScript script = studio.toPartScript();

        assertEquals(2, script.entries().size());
        assertEquals(7, script.entries().get(1).seed());
        // the audio of the first take of "b" is kept though it is not in the script
        assertTrue(script.liveKeys().contains(entry("b", 42).key()));
        assertTrue(script.liveKeys().contains(entry("b", 7).key()));
        assertEquals(3, script.liveKeys().size());
        assertEquals("h", script.sourceHash());
    }

    @Test
    void aNewAnnotationKeepsTheLockedSegmentsAndTakesTheRestFresh() {
        StudioSegment hand = segment("The door.", 42).edited(entry("The door!", 42)).anotherTake(9);
        StudioScript stored = new StudioScript(List.of(segment("Intro.", 42), hand, segment("End.", 42)), "old", Status.REVIEWED);
        // the text changed: a line is new, one is gone, and the locked one is still there
        StudioScript fresh = script("new", segment("Intro.", 1), segment("A new line.", 1), segment("The door.", 1));

        StudioScript merged = StudioScript.merge(stored, fresh);

        assertEquals(List.of("Intro.", "A new line.", "The door."), merged.segments().stream().map(StudioSegment::source).toList());
        assertEquals(1, merged.segments().get(0).entry().seed(), "an unlocked segment is the fresh one");
        assertSame(hand, merged.segments().get(2), "a locked one is kept with its text and takes");
        assertEquals("new", merged.sourceHash());
        assertEquals(Status.DRAFT, merged.status(), "a changed text is a draft again");
    }

    @Test
    void theStatusStaysWhenTheTextIsTheSame() {
        StudioScript stored = new StudioScript(List.of(segment("a", 42)), "same", Status.REVIEWED);

        assertEquals(Status.REVIEWED, StudioScript.merge(stored, script("same", segment("a", 1))).status());
        assertEquals(Status.DRAFT, StudioScript.merge(null, script("same", segment("a", 1))).status());
    }

    @Test
    void aLineSaidTwiceIsMatchedTwiceInOrder() {
        StudioSegment first = segment("Yes.", 42).edited(entry("Yes!", 42));
        StudioSegment second = segment("Yes.", 42).edited(entry("Yes?", 42));
        StudioScript stored = new StudioScript(List.of(first, second), "h", Status.DRAFT);

        StudioScript merged = StudioScript.merge(stored, script("h", segment("Yes.", 1), segment("Yes.", 1), segment("Yes.", 1)));

        assertSame(first, merged.segments().get(0));
        assertSame(second, merged.segments().get(1));
        assertFalse(merged.segments().get(2).locked(), "no third locked one");
    }

    @Test
    void aScriptSurvivesJson() {
        Gson gson = new Gson();
        StudioScript studio = new StudioScript(List.of(segment("a", 42).anotherTake(7), segment("b", 42)), "h", Status.FINAL);

        StudioScript back = gson.fromJson(gson.toJson(studio), StudioScript.class);

        assertEquals(studio, back);
        assertEquals(Status.FINAL, back.status());
        assertEquals("predefined:Henry.wav", back.segments().get(0).entry().voice().toString());
    }
}
