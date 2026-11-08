package com.github.enerccio.marginalia.extensions.audiobook.script;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The script of a part: its pieces in order. The audio of the part is the audio of the pieces joined with their pauses; it is cached by
 * {@link #joinKey()}.
 *
 * @param sourceHash the hash of the text of the part the script was made from ({@link Hashes#sha256}): when the text changes the audio is stale
 * @param keepKeys   cache keys of audio that is not in the script but is to be kept with the part (the other takes of a piece in the studio)
 */
public record PartScript(List<ScriptEntry> entries, String sourceHash, Set<String> keepKeys) {

    /** Changes when the way pieces are joined changes (the pauses, a loudness step), so that old audio is made again. */
    static final String JOIN_VERSION = "1";

    public PartScript {
        entries = List.copyOf(entries);
        keepKeys = keepKeys == null ? Set.of() : Set.copyOf(keepKeys);
    }

    public PartScript(List<ScriptEntry> entries, String sourceHash) {
        this(entries, sourceHash, Set.of());
    }

    /** The keys of the audio that belongs to the part: the pieces of the script and what is kept besides. */
    public Set<String> liveKeys() {
        Set<String> keys = new HashSet<>(keepKeys);
        entries.forEach(e -> keys.add(e.key()));
        return keys;
    }

    /** The cache key of the joined audio: the pieces and the pauses between them, in order. */
    public String joinKey() {
        StringBuilder all = new StringBuilder(JOIN_VERSION);
        for (ScriptEntry entry : entries) {
            all.append('\u0001').append(entry.key()).append(':').append(entry.pauseAfter());
        }
        return Hashes.sha256(all.toString());
    }

    /** Whether the audio made from a script of {@code storedSourceHash} no longer matches the text of the part. */
    public static boolean stale(String storedSourceHash, String currentText) {
        return storedSourceHash == null || !storedSourceHash.equals(Hashes.sha256(currentText == null ? "" : currentText));
    }
}
