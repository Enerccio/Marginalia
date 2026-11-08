package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

import java.util.List;

/** Something that turns a piece of text into speech. Blocks the calling (worker) thread until the audio is there. */
public interface SpeechBackend {

    /**
     * Checks that the server is up and has a model loaded, which the requests before a batch have to see to. Called by the batch before
     * its first piece, so that a missing load request is told as that and not as the 503 of the first piece.
     *
     * @throws BackendException when the server is not reachable, or answers that it has no model loaded
     */
    void prepare() throws BackendException;

    /**
     * @return the bytes of the audio in the format of the request
     * @throws BackendException when the server is not reachable, refuses, or gives nothing usable
     */
    byte[] synthesize(SpeechRequest request, Cancel cancel) throws BackendException;

    /** The reference clips on the server, by file name. */
    List<String> referenceFiles() throws BackendException;

    /** The voices that come with the server, by file name (what {@link SpeechRequest#voice} takes for a predefined voice). */
    List<String> predefinedVoices() throws BackendException;

    /**
     * Makes sure a reference clip is on the server and gives the name to put in a request: the name is made from the bytes (the same clip is
     * the same name, uploaded once per server, and users can't overwrite each other's clips by choosing a name).
     *
     * @param extension {@code wav} or {@code mp3}
     */
    String ensureReference(byte[] audio, String extension) throws BackendException;

    /** Asked while waiting; the backend stops when it is set. */
    interface Cancel {
        boolean isCancelled();
    }
}
