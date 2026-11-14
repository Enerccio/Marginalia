package com.github.enerccio.marginalia.extensions.audiobook.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** A {@code multipart/form-data} body with file parts, for the servers that take an upload (the JDK client has none). */
final class Multipart {

    private final String boundary = "----marginalia" + UUID.randomUUID().toString().replace("-", "");
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    /** Adds a file part. The names are made by the caller from a fixed alphabet, so they need no escaping. */
    Multipart file(String field, String filename, String contentType, byte[] content) {
        write("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + field + "\"; filename=\"" + filename + "\"\r\nContent-Type: "
                + contentType + "\r\n\r\n");
        out.writeBytes(content);
        write("\r\n");
        return this;
    }

    /** Adds a text field. The values are made by the caller from a fixed alphabet, so they need no escaping. */
    Multipart field(String name, String value) {
        write("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" + value + "\r\n");
        return this;
    }

    String contentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    byte[] body() {
        ByteArrayOutputStream all = new ByteArrayOutputStream();
        all.writeBytes(out.toByteArray());
        all.writeBytes(("--" + boundary + "--\r\n").getBytes(StandardCharsets.US_ASCII));
        return all.toByteArray();
    }

    private void write(String text) {
        out.writeBytes(text.getBytes(StandardCharsets.UTF_8));
    }
}
