package com.github.enerccio.marginalia.extensions.audiobook.queue;

import com.github.enerccio.marginalia.extensions.audiobook.script.VoiceRef;
import com.github.enerccio.marginalia.extensions.audiobook.service.SpeechBackend;
import com.github.enerccio.marginalia.extensions.audiobook.service.SpeechRequest.VoiceMode;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

/** Turns the voice of a piece into what the server is asked for: a clip of the user is put on the server (once) and gets the name of its bytes. */
@FunctionalInterface
public interface Voices {

    /**
     * What a request takes.
     *
     * @param name the file name of the voice on the server
     * @param text the words spoken in the clip, null when they are not known (or the voice is not a clip)
     */
    record Resolved(VoiceMode mode, String name, String text) {

        public Resolved(VoiceMode mode, String name) {
            this(mode, name, null);
        }
    }

    Resolved resolve(SpeechBackend backend, VoiceRef voice) throws BackendException;
}
