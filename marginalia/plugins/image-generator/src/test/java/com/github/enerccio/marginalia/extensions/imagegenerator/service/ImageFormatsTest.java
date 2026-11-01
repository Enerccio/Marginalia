package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ImageFormatsTest {

    @Test
    void recognizesTheFormats() {
        assertEquals("png", ImageFormats.extension(FakeServer.PNG));
        assertEquals("jpg", ImageFormats.extension(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        assertEquals("webp", ImageFormats.extension("RIFF....WEBPVP8 ".getBytes()));
        assertEquals("gif", ImageFormats.extension("GIF89a......".getBytes()));
        assertEquals("image/webp", ImageFormats.mimeType("webp"));
    }

    @Test
    void textAndShortDataAreNotImages() {
        assertNull(ImageFormats.extension("<html>this is not an image</html>".getBytes()));
        assertNull(ImageFormats.extension(new byte[]{1, 2}));
        assertNull(ImageFormats.extension(null));
    }
}
