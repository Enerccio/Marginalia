package com.github.enerccio.marginalia.extensions.audiobook.queue;

/**
 * Where the audio is kept: the audio of each piece of a part by its cache key, and the joined audio of the part. The application stores them as
 * resources of the user; the queue only knows this interface (and tests use a map).
 */
public interface AudioStore {

    /** @return the audio of the piece with that key in the part, null when there is none */
    byte[] segment(String partId, String key);

    void putSegment(String partId, String key, byte[] audio);

    /**
     * What is known of the joined audio of a part.
     *
     * @param joinKey    the key of the script it was joined from ({@code PartScript.joinKey})
     * @param sourceHash the hash of the text of the part it was made from
     * @param loudness   the loudness in LUFS it was levelled to, null when it was not (audio made before the levelling existed, or without ffmpeg)
     */
    record PartAudio(String joinKey, String sourceHash, Double loudness) {

        public PartAudio(String joinKey, String sourceHash) {
            this(joinKey, sourceHash, null);
        }
    }

    /** @return the joined audio of the part (without its bytes), null when there is none */
    PartAudio partAudio(String partId);

    /**
     * Stores the joined audio; the one stored before is replaced, and the pieces of the part that are not in {@code liveKeys} (pieces of an older
     * text) are forgotten.
     *
     * @param liveKeys the cache keys of the pieces of the script that was joined
     */
    void putPart(String partId, String joinKey, String sourceHash, byte[] audio, java.util.Collection<String> liveKeys);

    /**
     * Like {@link #putPart(String, String, String, byte[], java.util.Collection)} for audio that was levelled to {@code loudness} LUFS (null: it was not); a store
     * that does not keep it forgets it, and the part is then asked to be levelled again.
     */
    default void putPart(String partId, String joinKey, String sourceHash, byte[] audio, java.util.Collection<String> liveKeys, Double loudness) {
        putPart(partId, joinKey, sourceHash, audio, liveKeys);
    }

    /** Called when a job of the part is over, whatever became of it: what was put since is made permanent (the pieces of a failed job are kept for a retry). */
    default void flush(String partId) {
    }
}
