package com.github.enerccio.marginalia.extensions.audiobook.studio;

import com.github.enerccio.marginalia.extensions.audiobook.script.PartScript;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The script of a part as the studio keeps it, with the part (its attributes): the segments, the hash of the text they were made from and the
 * status of the part. It is the input of the renderer: {@link #toPartScript()}.
 *
 * @param sourceHash the hash of the text of the part the segments were made from
 */
public record StudioScript(List<StudioSegment> segments, String sourceHash, Status status) {

    /** Where the part is: the batch run leaves a {@link #FINAL} part alone. */
    public enum Status {
        DRAFT, REVIEWED, FINAL
    }

    public StudioScript {
        segments = List.copyOf(segments);
        status = status == null ? Status.DRAFT : status;
    }

    /** A script made without the studio's information (the plain script, or any {@link PartScript}). */
    public static StudioScript of(PartScript script, java.util.function.Function<com.github.enerccio.marginalia.extensions.audiobook.script.ScriptEntry, String> source) {
        List<StudioSegment> segments = new ArrayList<>();
        for (var entry : script.entries()) {
            segments.add(StudioSegment.made(entry, source.apply(entry), "NEUTRAL", 0.5));
        }
        return new StudioScript(segments, script.sourceHash(), Status.DRAFT);
    }

    /** The script the renderer takes: the current take of each segment; the audio of the other takes is kept with the part. */
    public PartScript toPartScript() {
        Set<String> keep = new HashSet<>();
        segments.forEach(s -> keep.addAll(s.takeKeys()));
        return new PartScript(segments.stream().map(StudioSegment::entry).toList(), sourceHash, keep);
    }

    public StudioScript withSegment(int index, StudioSegment segment) {
        List<StudioSegment> all = new ArrayList<>(segments);
        all.set(index, segment);
        return new StudioScript(all, sourceHash, status);
    }

    public StudioScript withStatus(Status status) {
        return new StudioScript(segments, sourceHash, status);
    }

    /**
     * A new annotation merged with what is stored: every segment of {@code fresh} is taken, except where a segment that was <b>locked</b> (changed by
     * hand) has the same words of the book: that one is kept as it is, with its takes. Each locked segment is used once, in order, so a line said
     * twice in a part is matched twice. The status stays when the text is the same, else the part is a draft again.
     *
     * @param stored what is stored, null for nothing
     */
    public static StudioScript merge(StudioScript stored, StudioScript fresh) {
        if (stored == null) {
            return fresh;
        }
        List<StudioSegment> locked = new ArrayList<>(stored.segments.stream().filter(StudioSegment::locked).toList());
        List<StudioSegment> merged = new ArrayList<>();
        for (StudioSegment segment : fresh.segments) {
            StudioSegment kept = null;
            for (int i = 0; i < locked.size(); i++) {
                if (locked.get(i).source().equals(segment.source())) {
                    kept = locked.remove(i);
                    break;
                }
            }
            merged.add(kept != null ? kept : segment);
        }
        Status status = stored.sourceHash != null && stored.sourceHash.equals(fresh.sourceHash) ? stored.status : Status.DRAFT;
        return new StudioScript(merged, fresh.sourceHash, status);
    }
}
