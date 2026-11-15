package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

/**
 * One image: the GPU lock of the backend is taken for it (the language model or another backend on the same GPU is unloaded by the
 * lock's steps when it has to be), the requests before it run, the backend makes it, the requests after it run, and the lock is given
 * back. Nothing is held between the images: a window that is closed, folded or forgotten keeps no GPU, and the next image of the same
 * lock finds the GPU still loaded.
 */
public final class ImageJob {

    /** The hold of the GPU lock; closing it twice does nothing. */
    @FunctionalInterface
    public interface Lease extends AutoCloseable {
        @Override
        void close();
    }

    /** Takes the GPU lock of the backend, blocking; the backend without a lock needs nothing. */
    @FunctionalInterface
    public interface Gpu {
        Lease take(ImageBackend.Cancel cancel) throws BackendException;
    }

    /** The requests before or after the image. */
    @FunctionalInterface
    public interface Hooks {
        void run() throws BackendException;
    }

    private ImageJob() {
    }

    /**
     * Makes the image. Called from a worker thread.
     *
     * @throws BackendException the GPU could not be taken, a request before the image failed or the backend failed; a failure of the
     *                          requests after the image is only logged (the image is there)
     */
    public static byte[] run(Gpu gpu, ImageBackend backend, Hooks before, Hooks after, ImageRequest request, ImageBackend.Cancel cancel)
            throws BackendException {
        try (Lease _ = gpu.take(cancel)) {
            before.run();
            try {
                return backend.generate(request, cancel);
            } finally {
                try {
                    after.run();
                } catch (BackendException e) {
                    ImageGeneratorLogs.warn("The request after the image failed: {}", e.getMessage());
                }
            }
        }
    }
}
