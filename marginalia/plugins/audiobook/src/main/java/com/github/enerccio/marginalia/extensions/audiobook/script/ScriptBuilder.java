package com.github.enerccio.marginalia.extensions.audiobook.script;

import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;

import com.github.enerccio.marginalia.extensions.audiobook.studio.StudioScript;
import com.github.enerccio.marginalia.extensions.audiobook.studio.StudioSegment;

import java.util.ArrayList;
import java.util.List;

/**
 * Makes a script from the text of a part. This is the plain one: the narrator reads every paragraph, with a pause after a paragraph and a longer
 * one after a heading. The labelling (a voice for each speaker, delivery, sound tags) and the spoken form (numerals, a pronunciation dictionary) come
 * later and replace it; the shape of the result is the same.
 */
public final class ScriptBuilder {

    public static final double PAUSE_AFTER_HEADING = 1.0;
    public static final double PAUSE_AFTER_PARAGRAPH = 0.6;

    private ScriptBuilder() {
    }

    /** The pause that stands for a scene break (a line of asterisks), which is not spoken. */
    public static final double PAUSE_SCENE_BREAK = 1.2;

    /**
     * @param narrator the voice of the narrator
     * @param defaults the backend: its model, format and the defaults of a piece
     * @param seed     the seed of every piece (a fixed one makes the audio reproducible and cacheable)
     */
    public static PartScript narrated(String text, VoiceRef narrator, BackendConfig defaults, int seed) {
        return studio(text, narrator, defaults, seed, SpokenForm.DEFAULT).toPartScript();
    }

    /**
     * The plain script as the studio keeps it: the narrator reads every paragraph, spelled for the ear by {@code spoken}; the words of the book are the
     * {@code source} of each segment. A scene break is not read, it makes the pause before it longer.
     */
    public static StudioScript studio(String text, VoiceRef narrator, BackendConfig defaults, int seed, SpokenForm spoken) {
        List<StudioSegment> segments = new ArrayList<>();
        String source = text == null ? "" : text;
        boolean breakBefore = false;
        for (String paragraph : source.strip().split("\\R\\s*\\R")) {
            boolean heading = paragraph.stripLeading().startsWith("#");
            String words = paragraph.strip().replaceFirst("^#+\\s*", "").replaceAll("\\s*\\R\\s*", " ").strip();
            if (words.isEmpty()) {
                continue;
            }
            String said = spoken.apply(words, heading);
            if (said.isEmpty()) {
                breakBefore = true;
                if (!segments.isEmpty()) {
                    StudioSegment last = segments.getLast();
                    segments.set(segments.size() - 1, withPause(last, Math.max(last.entry().pauseAfter(), PAUSE_SCENE_BREAK)));
                }
                continue;
            }
            segments.add(StudioSegment.made(new ScriptEntry("Narrator", said, narrator, defaults.getModel(), null, 0.5, defaults.getCfgWeight(),
                    defaults.getTemperature(), defaults.getSpeed(), seed, defaults.getChunkSize(), defaults.getFormat(),
                    heading ? PAUSE_AFTER_HEADING : PAUSE_AFTER_PARAGRAPH), words, "NEUTRAL", 0.5));
        }
        if (!segments.isEmpty()) {
            // nothing after the last piece
            segments.set(segments.size() - 1, withPause(segments.getLast(), 0));
        }
        return new StudioScript(segments, Hashes.sha256(source), StudioScript.Status.DRAFT);
    }

    private static StudioSegment withPause(StudioSegment segment, double pause) {
        return new StudioSegment(segment.entry().withPause(pause), segment.source(), segment.delivery(), segment.intensity(), segment.locked(), segment.takes());
    }
}
