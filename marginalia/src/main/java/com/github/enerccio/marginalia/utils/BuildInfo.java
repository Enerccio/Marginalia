package com.github.enerccio.marginalia.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Version and Git commit of the running build. The version comes from {@code META-INF/build-info/build-info.properties}
 * and the commit from {@code git.properties} (written by {@code git-commit-id-plugin}). The WAR filters the first one,
 * so while the app runs from {@code target/classes} (IDE, {@code mvn jetty:run}) the version is still a
 * {@code ${...}} placeholder and is reported as {@value #UNKNOWN_VERSION}.
 */
public final class BuildInfo {
    private static final Logger log = LoggerFactory.getLogger(BuildInfo.class);

    public static final String UNKNOWN_VERSION = "dev";

    private static final String BUILD_INFO = "META-INF/build-info/build-info.properties";
    private static final String GIT_INFO = "git.properties";

    private static final BuildInfo CURRENT = new BuildInfo(load(BUILD_INFO), load(GIT_INFO));

    private final String version;
    private final String commit;
    private final String commitFull;
    private final String commitTime;
    private final String buildTime;

    BuildInfo(Properties build, Properties git) {
        version = value(build, "marginalia-version", UNKNOWN_VERSION);
        buildTime = value(build, "marginalia-build-timestamp", null);
        commit = value(git, "git.commit.id.abbrev", null);
        commitFull = value(git, "git.commit.id.full", null);
        commitTime = value(git, "git.commit.time", null);
    }

    public static BuildInfo current() {
        return CURRENT;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Abbreviated commit id, {@code null} when the build had no Git information.
     */
    public String getCommit() {
        return commit;
    }

    public String getCommitFull() {
        return commitFull;
    }

    public String getCommitTime() {
        return commitTime;
    }

    public String getBuildTime() {
        return buildTime;
    }

    /**
     * {@code 1.4.0 (93ec118)}, only the version when the commit is not known.
     */
    public String getShortText() {
        return commit == null ? version : version + " (" + commit + ")";
    }

    /**
     * Everything known, one item per line, for a tooltip.
     */
    public String getDetailText() {
        StringBuilder sb = new StringBuilder("Marginalia ").append(version);
        if (commitFull != null) {
            sb.append("\nCommit ").append(commitFull);
        }
        if (commitTime != null) {
            sb.append("\nCommitted ").append(commitTime);
        }
        if (buildTime != null) {
            sb.append("\nBuilt ").append(buildTime);
        }
        return sb.toString();
    }

    private static String value(Properties properties, String key, String fallback) {
        String value = properties.getProperty(key);
        // an unfiltered placeholder (${project.version}) is no value
        if (value == null || value.isBlank() || value.contains("${")) {
            return fallback;
        }
        return value.trim();
    }

    private static Properties load(String resource) {
        Properties properties = new Properties();
        try (InputStream in = BuildInfo.class.getClassLoader().getResourceAsStream(resource)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            log.warn("Cannot read {}", resource, e);
        }
        return properties;
    }

}
