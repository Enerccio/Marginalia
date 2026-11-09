package com.github.enerccio.marginalia.extensions.audiobook.script;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** The hashes that name audio: the cache keys of a piece and of a part, and the hash of the text a script was made from. */
public final class Hashes {

    private Hashes() {
    }

    /** SHA-256 of the text (UTF-8) as hex. */
    public static String sha256(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
