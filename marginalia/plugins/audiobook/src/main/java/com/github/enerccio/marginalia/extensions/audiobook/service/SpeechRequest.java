package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;

/**
 * Everything the speech server is told about one piece of speech: the entry of the script of a part as the renderer sends it. The
 * audio of a piece is cached by the hash of these values, so a value that changes the sound is a field here (the seed too).
 *
 * @param voiceMode    {@link VoiceMode#PREDEFINED} (a voice that comes with the server) or {@link VoiceMode#CLONE} (a reference clip)
 * @param voice        the file name of the voice on the server (a predefined one, or the name {@link SpeechBackend#ensureReference} gave a clip)
 * @param language     the language code for the multilingual model, null for none (the other models ignore it)
 * @param exaggeration 0.25 to 2.0, the original model and the multilingual one; Turbo ignores it
 * @param seed         a seed changes the take; 0 lets the server choose
 */
public record SpeechRequest(String text, VoiceMode voiceMode, String voice, String language, double exaggeration, double cfgWeight,
                            double temperature, double speed, int seed, String format, int chunkSize) {

    public enum VoiceMode {
        PREDEFINED("predefined"), CLONE("clone");

        private final String wire;

        VoiceMode(String wire) {
            this.wire = wire;
        }

        /** The value of {@code voice_mode} in the request. */
        public String wire() {
            return wire;
        }
    }

    /** A request with the defaults of the backend; the caller changes what the entry changes. */
    public static SpeechRequest of(BackendConfig config, String text, VoiceMode mode, String voice) {
        return new SpeechRequest(text, mode, voice, null, 0.5, config.getCfgWeight(), config.getTemperature(), config.getSpeed(), 0,
                config.getFormat(), config.getChunkSize());
    }

    public SpeechRequest withLanguage(String language) {
        return new SpeechRequest(text, voiceMode, voice, language, exaggeration, cfgWeight, temperature, speed, seed, format, chunkSize);
    }

    public SpeechRequest withExaggeration(double exaggeration) {
        return new SpeechRequest(text, voiceMode, voice, language, exaggeration, cfgWeight, temperature, speed, seed, format, chunkSize);
    }

    public SpeechRequest withSeed(int seed) {
        return new SpeechRequest(text, voiceMode, voice, language, exaggeration, cfgWeight, temperature, speed, seed, format, chunkSize);
    }
}
