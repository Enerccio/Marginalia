package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

import java.util.List;

/** Something that turns a prompt into an image. Blocks the calling (worker) thread until the image is there. */
public interface ImageBackend {

    /**
     * @return the bytes of the image (PNG, JPEG or WebP)
     * @throws BackendException when the server is not reachable, refuses, or gives nothing usable
     */
    byte[] generate(ImageRequest request, Cancel cancel) throws BackendException;

    /** Checks the address and the key without making an image. */
    void test() throws BackendException;

    /** The LoRA files the server offers, for {@code {{lora}}}; empty when the backend has none to choose from. */
    default List<String> loras() throws BackendException {
        return List.of();
    }

    /** Asked while waiting; the backend stops (and tells ComfyUI to interrupt) when it is set. */
    interface Cancel {
        boolean isCancelled();
    }
}
