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
 * <p>
 * The name and the version are Marginalia's by default. An application on the platform installs its own at its start
 * ({@link #install}, built by {@link #load(String, String, String, String, ClassLoader)}), so the version footer, the
 * login page and the API name it, not Marginalia.
 */
public final class BuildInfo {
    private static final Logger log = LoggerFactory.getLogger(BuildInfo.class);

    public static final String UNKNOWN_VERSION = "dev";
    public static final String DEFAULT_NAME = "Marginalia";

    public static final String BUILD_INFO = "META-INF/build-info/build-info.properties";
    public static final String GIT_INFO = "git.properties";
    private static final String DEFAULT_KEY_PREFIX = "marginalia";

    private static volatile BuildInfo current = load(DEFAULT_NAME, BUILD_INFO, DEFAULT_KEY_PREFIX, GIT_INFO,
            BuildInfo.class.getClassLoader());

    private final String name;
    private final String version;
    private final String commit;
    private final String commitFull;
    private final String commitTime;
    private final String buildTime;

    BuildInfo(Properties build, Properties git) {
        this(DEFAULT_NAME, build, DEFAULT_KEY_PREFIX, git);
    }

    /**
     * The build of the application {@code name}: the version is {@code <keyPrefix>-version} and the build time
     * {@code <keyPrefix>-build-timestamp} of {@code build}, the commit is the {@code git.commit.*} of {@code git}.
     */
    public BuildInfo(String name, Properties build, String keyPrefix, Properties git) {
        this.name = name == null || name.isBlank() ? DEFAULT_NAME : name;
        version = value(build, keyPrefix + "-version", UNKNOWN_VERSION);
        buildTime = value(build, keyPrefix + "-build-timestamp", null);
        commit = value(git, "git.commit.id.abbrev", null);
        commitFull = value(git, "git.commit.id.full", null);
        commitTime = value(git, "git.commit.time", null);
    }

    public static BuildInfo current() {
        return current;
    }

    /**
     * Makes {@code info} the build every caller of {@link #current()} sees; an application on the platform calls it
     * once at its start (its {@code WebApplicationInitializer}), before the first page.
     */
    public static void install(BuildInfo info) {
        if (info == null) {
            throw new IllegalArgumentException("No build info");
        }
        current = info;
    }

    /**
     * Reads the build of the application {@code name} from the resources {@code buildResource} (the keys
     * {@code <keyPrefix>-version} and {@code <keyPrefix>-build-timestamp}) and {@code gitResource} (written by
     * {@code git-commit-id-plugin}) of the class loader; a missing resource gives no value.
     */
    public static BuildInfo load(String name, String buildResource, String keyPrefix, String gitResource,
                                 ClassLoader classLoader) {
        return new BuildInfo(name, load(buildResource, classLoader), keyPrefix, load(gitResource, classLoader));
    }

    /**
     * The name of the application ({@value #DEFAULT_NAME} unless another one was {@link #install installed}).
     */
    public String getName() {
        return name;
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
     * {@code Marginalia 1.4.0 (93ec118)}: the name and {@link #getShortText()}.
     */
    public String getNamedShortText() {
        return name + " " + getShortText();
    }

    /**
     * Everything known, one item per line, for a tooltip.
     */
    public String getDetailText() {
        StringBuilder sb = new StringBuilder(name).append(' ').append(version);
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

    private static Properties load(String resource, ClassLoader classLoader) {
        Properties properties = new Properties();
        if (resource == null || classLoader == null) {
            return properties;
        }
        try (InputStream in = classLoader.getResourceAsStream(resource)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            log.warn("Cannot read {}", resource, e);
        }
        return properties;
    }

}
