package com.github.enerccio.marginalia.extensions.prompthistory;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Keeps a finished request small: JSON, gzipped. A prompt is the whole story context, repeated almost word for word by
 * the next request, and compresses well; it is unpacked only when the dialog shows it.
 */
final class Packing {

    private static final Gson GSON = new Gson();

    private Packing() {
    }

    static byte[] pack(Object value) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bytes)) {
            gzip.write(GSON.toJson(value).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return bytes.toByteArray();
    }

    static <T> T unpack(byte[] packed, Type type) {
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(packed))) {
            return GSON.fromJson(new String(gzip.readAllBytes(), StandardCharsets.UTF_8), type);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
