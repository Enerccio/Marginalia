package com.github.enerccio.marginalia.utils;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class BuildInfoTest {

    private static Properties props(String... keyValues) {
        Properties p = new Properties();
        for (int i = 0; i < keyValues.length; i += 2) {
            p.setProperty(keyValues[i], keyValues[i + 1]);
        }
        return p;
    }

    @Test
    void versionAndCommit() {
        BuildInfo info = new BuildInfo(
                props("marginalia-version", "1.4.0", "marginalia-build-timestamp", "2026-10-17T10:00:00Z"),
                props("git.commit.id.abbrev", "93ec118", "git.commit.id.full", "93ec118ac6fed4eed3ec9d565aef8a961a9724a1"));

        assertThat(info.getShortText()).isEqualTo("1.4.0 (93ec118)");
        assertThat(info.getDetailText()).contains("1.4.0", "93ec118ac6fed4eed3ec9d565aef8a961a9724a1", "2026-10-17T10:00:00Z");
    }

    @Test
    void unfilteredPlaceholderIsNoVersion() {
        BuildInfo info = new BuildInfo(
                props("marginalia-version", "${project.version}"),
                props("git.commit.id.abbrev", "93ec118"));

        assertThat(info.getVersion()).isEqualTo(BuildInfo.UNKNOWN_VERSION);
        assertThat(info.getShortText()).isEqualTo("dev (93ec118)");
    }

    @Test
    void missingGitInformationShowsOnlyTheVersion() {
        BuildInfo info = new BuildInfo(props("marginalia-version", "1.4.0"), new Properties());

        assertThat(info.getShortText()).isEqualTo("1.4.0");
        assertThat(info.getCommit()).isNull();
    }

}
