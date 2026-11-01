package com.github.enerccio.marginalia.extensions.imagegenerator.service;

/** Something that turns a prompt into an image. Blocks the calling (worker) thread until the image is there. */
public interface ImageBackend {

    /**
     * @return the bytes of the image (PNG, JPEG or WebP)
     * @throws ImageException when the server is not reachable, refuses, or gives nothing usable
     */
    byte[] generate(ImageRequest request, Cancel cancel) throws ImageException;

    /** Checks the address and the key without making an image. */
    void test() throws ImageException;

    /** Asked while waiting; the backend stops (and tells ComfyUI to interrupt) when it is set. */
    interface Cancel {
        boolean isCancelled();
    }

    class ImageException extends Exception {
        public ImageException(String message) {
            super(message);
        }

        public ImageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
