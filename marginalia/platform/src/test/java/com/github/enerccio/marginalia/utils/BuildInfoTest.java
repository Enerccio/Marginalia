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

    @Test
    void nameAndKeysOfAnotherApplication() {
        BuildInfo info = new BuildInfo("Joi", props("joi-version", "0.3.0", "joi-build-timestamp", "2026-11-01T10:00:00Z"),
                "joi", props("git.commit.id.abbrev", "abc1234"));

        assertThat(info.getName()).isEqualTo("Joi");
        assertThat(info.getNamedShortText()).isEqualTo("Joi 0.3.0 (abc1234)");
        assertThat(info.getDetailText()).startsWith("Joi 0.3.0").contains("2026-11-01T10:00:00Z");
    }

    @Test
    void marginaliaIsTheDefaultName() {
        BuildInfo info = new BuildInfo(props("marginalia-version", "1.4.0"), new Properties());

        assertThat(info.getNamedShortText()).isEqualTo("Marginalia 1.4.0");
    }

    @Test
    void installedBuildIsTheCurrentOne() {
        BuildInfo previous = BuildInfo.current();
        try {
            BuildInfo joi = BuildInfo.load("Joi", "no/such/resource.properties", "joi", "no/such/git.properties",
                    getClass().getClassLoader());
            BuildInfo.install(joi);
            assertThat(BuildInfo.current()).isSameAs(joi);
            assertThat(BuildInfo.current().getVersion()).isEqualTo(BuildInfo.UNKNOWN_VERSION);
        } finally {
            BuildInfo.install(previous);
        }
    }

}
