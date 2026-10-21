package com.github.enerccio.marginalia.domain.security.service.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The production hash of a password, which the other tests do not use (the test context hashes with
 * {@code TestUserService}). The expected value is PBKDF2-HMAC-SHA256 with 65536 rounds from another implementation:
 * a different one would make every stored password wrong.
 */
class PasswordHashTest {

    @Test
    void passwordIsHashedWithPbkdf2() throws Exception {
        String salt = "AAECAwQFBgcICQoLDA0ODw==";

        String hash = new UserServiceImpl().hashPassword("password", salt);

        assertThat(hash).isEqualTo("J0qaj0gXVMcyzQ4LMo1HjDXdNPHcHlDDgkH+ryhRt4c=");
        assertThat(new UserServiceImpl().hashPassword("Password", salt)).isNotEqualTo(hash);
    }
}
