package com.github.enerccio.marginalia.db;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.DatabaseCheck;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An upgrade that is going to migrate an existing database first saves a copy of it in the backup folder.
 */
class PreMigrationBackupTest {

    private static final String DB = "marginalia.sqlite";

    @TempDir
    Path tempDir;

    private Configuration configuration;
    private File database;

    @BeforeEach
    void setUp() throws Exception {
        configuration = new Configuration();
        configuration.setFolder(tempDir.resolve("app").toFile());
        configuration.afterPropertiesSet();
        database = new File(configuration.getFolder(), DB);
    }

    /**
     * The application database as an older version of Marginalia left it: migrated up to {@code version} only.
     */
    private void databaseAt(String version) {
        withDatabase(database, jdbc -> {
            ClassicConfiguration flyway = DatabaseCheck.flywayConfiguration(jdbc.getDataSource());
            flyway.setTarget(MigrationVersion.fromVersion(version));
            new Flyway(flyway).migrate();
            jdbc.execute("CREATE TABLE marker (id INTEGER)");
            jdbc.update("INSERT INTO marker (id) VALUES (42)");
        });
    }

    private static void withDatabase(File file, Consumer<JdbcTemplate> action) {
        SingleConnectionDataSource dataSource = new SingleConnectionDataSource("jdbc:sqlite:" + file.getAbsolutePath(), true);
        dataSource.setDriverClassName("org.sqlite.JDBC");
        try {
            action.accept(new JdbcTemplate(dataSource));
        } finally {
            dataSource.destroy();
        }
    }

    private static String latestVersion(File file) {
        String[] version = new String[1];
        withDatabase(file, jdbc -> version[0] = jdbc.queryForObject(
                "SELECT version FROM flyway_schema_history WHERE success = 1 ORDER BY installed_rank DESC LIMIT 1", String.class));
        return version[0];
    }

    private List<String> copies() {
        String[] names = configuration.getDatabaseBackupFolder().list();
        return Arrays.stream(names == null ? new String[0] : names)
                .filter(n -> n.startsWith(Configuration.PRE_MIGRATION_PREFIX)).sorted().toList();
    }

    @Test
    void savesCopyOfOlderDatabase() throws Exception {
        databaseAt("5");

        configuration.resolveDb(DB);

        assertThat(copies()).hasSize(1).first().asString().contains("-V5-").endsWith(DB);
        File copy = new File(configuration.getDatabaseBackupFolder(), copies().get(0));
        assertThat(latestVersion(copy)).isEqualTo("5");
        withDatabase(copy, jdbc -> assertThat(jdbc.queryForObject("SELECT id FROM marker", Integer.class)).isEqualTo(42));
        assertThatCopyPassesRestoreCheck(copy);
        // the database itself is migrated later, by the application
        assertThat(latestVersion(database)).isEqualTo("5");
    }

    private static void assertThatCopyPassesRestoreCheck(File copy) throws Exception {
        DatabaseCheck.check(copy);
    }

    @Test
    void savesCopyOfDatabaseWithoutMigrationHistory() throws Exception {
        databaseAt("1");
        withDatabase(database, jdbc -> jdbc.execute("DROP TABLE flyway_schema_history"));

        configuration.resolveDb(DB);

        assertThat(copies()).hasSize(1).first().asString().contains("-V1-");
    }

    @Test
    void savesNothingForCurrentDatabase() throws Exception {
        withDatabase(database, jdbc -> new Flyway(DatabaseCheck.flywayConfiguration(jdbc.getDataSource())).migrate());

        configuration.resolveDb(DB);

        assertThat(copies()).isEmpty();
    }

    @Test
    void savesNothingForNewInstallation() throws Exception {
        configuration.resolveDb(DB);
        assertThat(copies()).isEmpty();

        Files.createFile(database.toPath());
        configuration.resolveDb(DB);
        assertThat(copies()).isEmpty();
    }

    @Test
    void keepsOnlyNewestCopies() throws Exception {
        for (String stamp : List.of("20200101-000000", "20200102-000000", "20200103-000000")) {
            Files.writeString(new File(configuration.getDatabaseBackupFolder(),
                    Configuration.PRE_MIGRATION_PREFIX + stamp + "-V3-" + DB).toPath(), "old");
        }
        databaseAt("5");

        configuration.resolveDb(DB);

        List<String> kept = copies();
        assertThat(kept).hasSize(3);
        assertThat(kept).noneMatch(n -> n.contains("20200101"));
        assertThat(kept).anyMatch(n -> n.contains("-V5-"));
    }
}
