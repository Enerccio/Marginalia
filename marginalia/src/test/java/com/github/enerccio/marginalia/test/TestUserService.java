package com.github.enerccio.marginalia.test;

import com.github.enerccio.marginalia.domain.security.service.impl.UserServiceImpl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * {@code UserServiceImpl} for the tests (see {@link TestContextPostProcessor}): one round of SHA-256 in place of
 * PBKDF2. Every test creates a user and with 65536 rounds hashing its password was close to half of the time the tests
 * spent, while only the tests of the login check the password.
 */
public class TestUserService extends UserServiceImpl {

    @Override
    protected String hashPassword(String password, String salt) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(Base64.getDecoder().decode(salt));
        digest.update(password.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(digest.digest());
    }
}
