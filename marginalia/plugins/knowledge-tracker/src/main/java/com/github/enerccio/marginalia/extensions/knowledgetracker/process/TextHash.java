package com.github.enerccio.marginalia.extensions.knowledgetracker.process;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * The hash of the text of a part, to tell that it changed since it was processed.
 */
public final class TextHash {

    private TextHash() {
    }

    public static String of(String text) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
