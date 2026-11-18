package com.github.enerccio.marginalia.domain.security.service.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The production hash of a password, which the other tests do not use (the test context hashes with
 * {@code TestUserService}). The expected values are PBKDF2-HMAC-SHA256 from another implementation: a different one
 * would make every stored password wrong.
 */
class PasswordHashTest {

    private static final String SALT = "AAECAwQFBgcICQoLDA0ODw==";

    @Test
    void passwordIsHashedWithPbkdf2() throws Exception {
        String hash = new UserServiceImpl().hashPassword("password", SALT, UserServiceImpl.LEGACY_ITERATIONS);

        assertThat(hash).isEqualTo("J0qaj0gXVMcyzQ4LMo1HjDXdNPHcHlDDgkH+ryhRt4c=");
        assertThat(new UserServiceImpl().hashPassword("Password", SALT, UserServiceImpl.LEGACY_ITERATIONS))
                .isNotEqualTo(hash);
    }

    @Test
    void currentHashHas600000Rounds() throws Exception {
        assertThat(UserServiceImpl.ITERATIONS).isEqualTo(600_000);
        assertThat(new UserServiceImpl().hashPassword("password", SALT, UserServiceImpl.ITERATIONS))
                .isEqualTo("O8NxGOYlCT6bee0Ikw6nr3OJWRIz/dkt3fNpNx5g28A=");
    }

    @Test
    void storedValueIsParsed() {
        assertThat(UserServiceImpl.StoredHash.parse("pbkdf2-sha256$700000$salt$hash"))
                .isEqualTo(new UserServiceImpl.StoredHash(700_000, "salt", "hash"));
        assertThat(UserServiceImpl.StoredHash.parse("salt:hash"))
                .isEqualTo(new UserServiceImpl.StoredHash(UserServiceImpl.LEGACY_ITERATIONS, "salt", "hash"));
        assertThat(new UserServiceImpl.StoredHash(600_000, "s", "h").format()).isEqualTo("pbkdf2-sha256$600000$s$h");

        for (String bad : new String[]{null, "", "x", "a:b:c", "pbkdf2-sha256$x$s$h", "pbkdf2-sha256$-1$s$h",
                "pbkdf2-sha256$600000$s", "pbkdf2-sha256$600000$s$h$extra"}) {
            assertThat(UserServiceImpl.StoredHash.parse(bad)).as(String.valueOf(bad)).isNull();
        }
    }
}
