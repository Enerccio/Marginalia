package com.github.enerccio.marginalia.extensions.audiobook.studio;

import com.github.enerccio.marginalia.extensions.audiobook.script.ScriptEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * One segment of the script of a part in the studio: the entry the renderer sends, and what the studio knows besides.
 *
 * @param entry     everything the speech server is told (and the pause after)
 * @param source    the words of the piece in the book, what an annotation of a changed text finds this segment by
 * @param delivery  how the labelling said to read it (a {@code Delivery} name), for people
 * @param intensity how strongly, 0 to 1
 * @param locked    changed by hand: a new annotation keeps it as it is
 * @param takes     the seeds tried for this piece, the current one among them (the audio of each is kept)
 */
public record StudioSegment(ScriptEntry entry, String source, String delivery, double intensity, boolean locked, List<Integer> takes) {

    public StudioSegment {
        List<Integer> all = new ArrayList<>(takes == null ? List.of() : takes);
        if (!all.contains(entry.seed())) {
            all.add(entry.seed());
        }
        takes = List.copyOf(all);
    }

    /** A segment as the annotation makes it: not locked, with the one take it has. */
    public static StudioSegment made(ScriptEntry entry, String source, String delivery, double intensity) {
        return new StudioSegment(entry, source, delivery, intensity, false, List.of());
    }

    /** What a person changed: the segment is locked from now on. */
    public StudioSegment edited(ScriptEntry changed) {
        return new StudioSegment(changed, source, delivery, intensity, true, takes);
    }

    public StudioSegment withDelivery(String delivery, double intensity) {
        return new StudioSegment(entry, source, delivery, intensity, true, takes);
    }

    public StudioSegment withLocked(boolean locked) {
        return new StudioSegment(entry, source, delivery, intensity, locked, takes);
    }

    /** The piece taken again with another seed: the new seed is the current take, the earlier takes stay. Locked, since it is a choice. */
    public StudioSegment anotherTake(int seed) {
        return new StudioSegment(entry.withSeed(seed), source, delivery, intensity, true, takes);
    }

    /** The entry of the take with this seed. */
    public ScriptEntry entryOfTake(int seed) {
        return entry.withSeed(seed);
    }

    /** The cache keys of the audio of all the takes. */
    public List<String> takeKeys() {
        return takes.stream().map(seed -> entry.withSeed(seed).key()).toList();
    }
}
