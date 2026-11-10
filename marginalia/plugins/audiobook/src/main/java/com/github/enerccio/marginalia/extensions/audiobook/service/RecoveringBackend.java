package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.GpuGate;
import com.github.enerccio.marginalia.extensions.httphooks.ServerGoneException;

import java.util.List;

/**
 * A backend that keeps a batch alive when its server does not: the server is started again (the requests after the batch, then the requests
 * before it, then the model check) and
 * <ul>
 *   <li>every {@code restartEvery} pieces, when that is set: a speech server that leaks memory is stopped before it takes the machine down;</li>
 *   <li>when a piece finds the server gone (refused, reset, timed out, "no model loaded"): the piece is made again once on the fresh server.</li>
 * </ul>
 * When the server cannot be brought back, or is gone again at once, the backend is <i>broken</i>: every later piece of the batch fails at once
 * with the same message, instead of each one waiting for a connection that does not come.
 */
final class RecoveringBackend implements SpeechBackend {

    private final SpeechBackend inner;
    private final GpuGate.Step restart;
    private final int restartEvery;
    private int made;
    private BackendException broken;

    /**
     * @param restart      stops the server and starts it again, the requests of the backend (may fail)
     * @param restartEvery restart before the piece that follows this many pieces; 0: never
     */
    RecoveringBackend(SpeechBackend inner, GpuGate.Step restart, int restartEvery) {
        this.inner = inner;
        this.restart = restart;
        this.restartEvery = Math.max(0, restartEvery);
    }

    @Override
    public void prepare() throws BackendException {
        inner.prepare();
    }

    @Override
    public byte[] synthesize(SpeechRequest request, Cancel cancel) throws BackendException {
        if (broken != null) {
            throw broken;
        }
        if (restartEvery > 0 && made >= restartEvery) {
            AudiobookLog.get().info("The speech server made {} pieces: it is restarted before the next one", made);
            restartServer();
        }
        byte[] audio;
        try {
            audio = inner.synthesize(request, cancel);
        } catch (ServerGoneException e) {
            if (cancel != null && cancel.isCancelled()) {
                throw e;
            }
            AudiobookLog.get().warn("The speech server is gone ({}): it is restarted and the piece is made again", e.getMessage());
            restartServer();
            try {
                audio = inner.synthesize(request, cancel);
            } catch (ServerGoneException again) {
                broken = new BackendException("The speech server is gone again after a restart: " + again.getMessage(), again);
                AudiobookLog.get().warn("{}", broken.getMessage());
                throw broken;
            }
        }
        made++;
        return audio;
    }

    /** The requests after the batch (all of them, a failure does not matter: the server may well be dead), the ones before it, the check. */
    private void restartServer() throws BackendException {
        try {
            try {
                restart.run();
            } catch (BackendException e) {
                throw new BackendException("The speech server could not be restarted: " + e.getMessage(), e);
            }
            inner.prepare();
            made = 0;
            AudiobookLog.get().info("The speech server is up again");
        } catch (BackendException e) {
            broken = e;
            AudiobookLog.get().warn("{}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<String> referenceFiles() throws BackendException {
        return inner.referenceFiles();
    }

    @Override
    public List<String> predefinedVoices() throws BackendException {
        return inner.predefinedVoices();
    }

    @Override
    public String ensureReference(byte[] audio, String extension) throws BackendException {
        return inner.ensureReference(audio, extension);
    }
}
