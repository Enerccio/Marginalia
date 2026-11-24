package com.github.enerccio.marginalia.extensions.audiobook.recognize;

import com.github.enerccio.marginalia.extensions.audiobook.model.RecognizerConfig;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

/**
 * Writes down what a piece of audio says: the check of the pieces of a batch and the text of a voice clip. Blocks the calling (worker) thread.
 */
public interface SpeechRecognizer {

    /** Asked while waiting; the recognition stops when it is set. */
    @FunctionalInterface
    interface Cancel {
        boolean isCancelled();
    }

    /**
     * @param audio     wav or mp3
     * @param extension {@code wav} or {@code mp3}
     * @param language  the code of the language ({@code en}, {@code ja}), null to let the recogniser find it
     * @return the text, never null
     */
    String transcribe(byte[] audio, String extension, String language, Cancel cancel) throws BackendException;

    /** Checks that the server is there and can recognise speech, without recognising anything. */
    void test() throws BackendException;

    /** The recogniser of the settings. */
    static SpeechRecognizer of(RecognizerConfig config, String apiKey) {
        return config.getType() == RecognizerConfig.Type.OPENAI ? new OpenAiRecognizer(config, apiKey) : new ComfyGraniteRecognizer(config, apiKey);
    }
}
