package com.github.enerccio.marginalia.extensions.audiobook.script;

/**
 * One piece of the script of a part: everything the speech server is told about it and the pause after it. The audio of a piece is cached by
 * {@link #key()}, the hash of what changes the sound, so the same entry is never made twice and a change of any value makes it again (the seed
 * too: another seed is another take). The speaker and the pause are not in the key: they do not change the audio of the piece.
 *
 * @param speaker      who says it (the narrator or a character), for the studio and the cast; not sent to the server
 * @param text         the spoken form (what the voice reads, not what the book says)
 * @param voice        the voice
 * @param model        the model that makes it ({@code BackendConfig.MODELS}); the pieces of one model are made together
 * @param language     the language code for the multilingual model, null for none
 * @param seed         0 lets the server choose (then the audio is not reproducible, which the key can't know: give a seed)
 * @param pauseAfter   seconds of silence after the piece
 */
public record ScriptEntry(String speaker, String text, VoiceRef voice, String model, String language, double exaggeration, double cfgWeight,
                          double temperature, double speed, int seed, int chunkSize, String format, double pauseAfter) {

    /** Changes when what the key covers changes meaning, so that old audio is not taken for the audio of a new meaning. */
    static final String KEY_VERSION = "1";

    /** The cache key of the audio of this piece. */
    public String key() {
        return Hashes.sha256(String.join("\u0001", KEY_VERSION, text, voice.toString(), model, language == null ? "" : language,
                Double.toString(exaggeration), Double.toString(cfgWeight), Double.toString(temperature), Double.toString(speed),
                Integer.toString(seed), Integer.toString(chunkSize), format));
    }

    public ScriptEntry withSeed(int seed) {
        return new ScriptEntry(speaker, text, voice, model, language, exaggeration, cfgWeight, temperature, speed, seed, chunkSize, format, pauseAfter);
    }

    public ScriptEntry withPause(double pauseAfter) {
        return new ScriptEntry(speaker, text, voice, model, language, exaggeration, cfgWeight, temperature, speed, seed, chunkSize, format, pauseAfter);
    }
}
