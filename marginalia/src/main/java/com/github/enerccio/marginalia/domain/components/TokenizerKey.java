package com.github.enerccio.marginalia.domain.components;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Objects;

/**
 * What a tokenizer is chosen and cached by: AIs with an equal key count the tokens the same way and share what was found
 * out about it. The {@link #id()} says what is counted against (e.g. the endpoint and the model) and is safe to log and to
 * hand to code that is not the owner of the AI. The credentials are only a fingerprint (HMAC with a secret of this process, so
 * it can not be read back or looked up), which takes part in {@link #equals} and {@link #hashCode} but is never shown.
 */
public final class TokenizerKey {

    private static final byte[] PROCESS_SECRET = new byte[32];

    static {
        new SecureRandom().nextBytes(PROCESS_SECRET);
    }

    private final String id;
    private final String fingerprint;

    private TokenizerKey(String id, String fingerprint) {
        this.id = id;
        this.fingerprint = fingerprint;
    }

    /**
     * A key without credentials.
     */
    public static TokenizerKey of(String id) {
        return new TokenizerKey(Objects.requireNonNull(id, "id"), "");
    }

    /**
     * A key of the id and the credentials (an API key, a token), which may be null or blank for none. The credentials are
     * not kept, only their fingerprint.
     */
    public static TokenizerKey of(String id, String secret) {
        if (secret == null || secret.isBlank()) {
            return of(id);
        }
        return new TokenizerKey(Objects.requireNonNull(id, "id"), fingerprint(secret));
    }

    private static String fingerprint(String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(PROCESS_SECRET, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(secret.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 is not available", e);
        }
    }

    /**
     * What is counted against, without credentials.
     */
    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TokenizerKey other && id.equals(other.id) && fingerprint.equals(other.fingerprint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fingerprint);
    }

    @Override
    public String toString() {
        return id;
    }
}
