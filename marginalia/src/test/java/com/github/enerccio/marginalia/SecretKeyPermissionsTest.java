package com.github.enerccio.marginalia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The secret key is created readable by its owner only, and an open one of an existing installation is reported.
 */
class SecretKeyPermissionsTest {

    @TempDir
    Path dir;

    private static void assumePosix() {
        assumeTrue(java.nio.file.FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
    }

    @Test
    void newKeyIsOwnerOnly() throws Exception {
        assumePosix();
        Path key = dir.resolve("secret.key");
        Configuration.createOwnerOnly(key, "abc");
        assertThat(PosixFilePermissions.toString(Files.getPosixFilePermissions(key))).isEqualTo("rw-------");
        assertThat(Files.readString(key)).isEqualTo("abc");
    }

    @Test
    void openKeyIsReported() throws Exception {
        assumePosix();
        Path key = dir.resolve("secret.key");
        Files.writeString(key, "abc");
        Files.setPosixFilePermissions(key, PosixFilePermissions.fromString("rw-r--r--"));
        assertThat(Configuration.warnWhenReadableByOthers(key)).isTrue();
        Files.setPosixFilePermissions(key, PosixFilePermissions.fromString("rw-r-----"));
        assertThat(Configuration.warnWhenReadableByOthers(key)).isTrue();
    }

    @Test
    void ownerOnlyKeyIsNotReportedAndNotChanged() throws Exception {
        assumePosix();
        Path key = dir.resolve("secret.key");
        Configuration.createOwnerOnly(key, "abc");
        assertThat(Configuration.warnWhenReadableByOthers(key)).isFalse();
    }

    @Test
    void openKeyIsLeftAsItIs() throws Exception {
        assumePosix();
        Path key = dir.resolve("secret.key");
        Files.writeString(key, "abc");
        Files.setPosixFilePermissions(key, PosixFilePermissions.fromString("rw-r--r--"));
        Configuration.warnWhenReadableByOthers(key);
        assertThat(PosixFilePermissions.toString(Files.getPosixFilePermissions(key))).isEqualTo("rw-r--r--");
    }
}
