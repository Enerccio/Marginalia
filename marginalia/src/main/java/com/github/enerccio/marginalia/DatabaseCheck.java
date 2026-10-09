package com.github.enerccio.marginalia;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.sqlite.SQLiteConfig;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks that a SQLite file is a Marginalia database the running version can use, before it is restored. Also holds
 * the Flyway configuration, so the restore on start migrates the database exactly like the application does.
 */
public final class DatabaseCheck {

    public static final String MIGRATION_LOCATION = "classpath:migration";
    private static final String MIGRATION_PATTERN = "classpath*:migration/V*__*.sql";
    private static final Pattern MIGRATION_NAME = Pattern.compile("^V([0-9._]+)__.*\\.sql$");
    private static final byte[] SQLITE_HEADER = "SQLite format 3\0".getBytes(StandardCharsets.US_ASCII);
    private static final String HISTORY_TABLE = "flyway_schema_history";

    /**
     * Tables every Marginalia database has (created by V1).
     */
    static final List<String> CORE_TABLES = List.of("users", "settings", "manuscripts", "messages", "lorebooks",
            "entries", "ais", "protocols", "tags", "t2e");

    private DatabaseCheck() {
    }

    /**
     * Flyway configuration of the application database, used by the {@code flyway} bean and by the restore on start.
     */
    public static ClassicConfiguration flywayConfiguration(DataSource dataSource) {
        ClassicConfiguration configuration = new ClassicConfiguration();
        configuration.setDataSource(dataSource);
        configuration.setBaselineOnMigrate(true);
        configuration.setBaselineVersion(MigrationVersion.fromVersion("1"));
        configuration.setLocationsAsStrings(MIGRATION_LOCATION);
        return configuration;
    }

    /**
     * Checks the file and throws {@link InvalidDatabaseException} with the reason when it can't be restored: not a
     * SQLite file, damaged ({@code PRAGMA integrity_check}), not a Marginalia database (core tables missing), a failed
     * migration in its history, or a database of a newer Marginalia version than the running one.
     */
    public static void check(File file) throws IOException {
        byte[] header = new byte[SQLITE_HEADER.length];
        int read;
        try (InputStream inputStream = new FileInputStream(file)) {
            read = inputStream.readNBytes(header, 0, header.length);
        }
        if (read != header.length || !Arrays.equals(header, SQLITE_HEADER)) {
            throw new InvalidDatabaseException("not a SQLite database");
        }

        SQLiteConfig config = new SQLiteConfig();
        config.setReadOnly(true);
        try (Connection connection = config.createConnection("jdbc:sqlite:" + file.getAbsolutePath());
             Statement statement = connection.createStatement()) {
            List<String> problems = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery("PRAGMA integrity_check")) {
                while (rs.next() && problems.size() < 5) {
                    String result = rs.getString(1);
                    if (!"ok".equalsIgnoreCase(result)) {
                        problems.add(result);
                    }
                }
            }
            if (!problems.isEmpty()) {
                throw new InvalidDatabaseException("the database is damaged: " + String.join("; ", problems));
            }

            Set<String> tables = new HashSet<>();
            try (ResultSet rs = statement.executeQuery("SELECT lower(name) FROM sqlite_master WHERE type = 'table'")) {
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
            }
            List<String> missing = CORE_TABLES.stream().filter(t -> !tables.contains(t)).toList();
            if (!missing.isEmpty()) {
                throw new InvalidDatabaseException("not a Marginalia database, missing tables: " + String.join(", ", missing));
            }

            // a database without history predates Flyway, it is baselined at V1 on start
            if (tables.contains(HISTORY_TABLE)) {
                checkHistory(statement);
            }
        } catch (SQLException e) {
            throw new InvalidDatabaseException("the database can't be read: " + e.getMessage(), e);
        }
    }

    private static void checkHistory(Statement statement) throws SQLException, IOException {
        MigrationVersion latestKnown = latestMigration();
        MigrationVersion latestApplied = null;
        try (ResultSet rs = statement.executeQuery("SELECT version, success FROM " + HISTORY_TABLE)) {
            while (rs.next()) {
                String version = rs.getString(1);
                if (!rs.getBoolean(2)) {
                    throw new InvalidDatabaseException("the database has a failed migration (version " + version + ")");
                }
                if (version != null) {
                    MigrationVersion applied = MigrationVersion.fromVersion(version);
                    if (latestApplied == null || applied.isNewerThan(latestApplied.getVersion())) {
                        latestApplied = applied;
                    }
                }
            }
        }
        if (latestApplied != null && latestKnown != null && latestApplied.isNewerThan(latestKnown.getVersion())) {
            throw new InvalidDatabaseException("the database is from a newer version of Marginalia (schema version "
                    + latestApplied + ", this version knows up to " + latestKnown + ")");
        }
    }

    /**
     * Highest migration version bundled with the application.
     */
    static MigrationVersion latestMigration() throws IOException {
        Resource[] resources = new PathMatchingResourcePatternResolver(DatabaseCheck.class.getClassLoader())
                .getResources(MIGRATION_PATTERN);
        MigrationVersion latest = null;
        for (Resource resource : resources) {
            Matcher matcher = MIGRATION_NAME.matcher(Objects.toString(resource.getFilename(), ""));
            if (matcher.matches()) {
                MigrationVersion version = MigrationVersion.fromVersion(matcher.group(1).replace('_', '.'));
                if (latest == null || version.isNewerThan(latest.getVersion())) {
                    latest = version;
                }
            }
        }
        return latest;
    }

    /**
     * The file can't be restored, the message says why.
     */
    public static class InvalidDatabaseException extends IllegalArgumentException {

        public InvalidDatabaseException(String message) {
            super(message);
        }

        public InvalidDatabaseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
