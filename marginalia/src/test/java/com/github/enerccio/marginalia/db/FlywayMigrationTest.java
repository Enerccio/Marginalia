package com.github.enerccio.marginalia.db;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Flyway migrations on an empty database, on a database at V1 and on a pre-Flyway database (no history table),
 * each followed by Hibernate schema validation ({@code hbm2ddl.auto=validate} from persistence.xml).
 */
class FlywayMigrationTest {

    @TempDir
    Path tempDir;

    private SingleConnectionDataSource dataSource;
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        dataSource = new SingleConnectionDataSource("jdbc:sqlite:" + tempDir.resolve("test.sqlite"), true);
        dataSource.setDriverClassName("org.sqlite.JDBC");
        jdbc = new JdbcTemplate(dataSource);
    }

    /**
     * Same configuration as the {@code flywayConfiguration} bean in datasources-config.xml.
     */
    private Flyway flyway(String target) {
        ClassicConfiguration configuration = new ClassicConfiguration();
        configuration.setDataSource(dataSource);
        configuration.setBaselineOnMigrate(true);
        configuration.setBaselineVersion(MigrationVersion.fromVersion("1"));
        configuration.setLocationsAsStrings("classpath:migration");
        if (target != null) {
            configuration.setTarget(MigrationVersion.fromVersion(target));
        }
        return new Flyway(configuration);
    }

    private static void validateSchema(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setPersistenceXmlLocation("file:src/main/webapp/config/persistence.xml");
        emf.setPersistenceUnitName("PU");
        emf.setDataSource(dataSource);
        emf.afterPropertiesSet();
        emf.destroy();
    }

    private List<String> availableVersions() {
        return Arrays.stream(flyway(null).info().all()).map(i -> i.getVersion().getVersion()).toList();
    }

    @Test
    void migratesEmptyDatabaseAndSchemaValidates() {
        flyway(null).migrate();

        MigrationInfo[] applied = flyway(null).info().applied();
        assertThat(applied).allMatch(i -> i.getState() == MigrationState.SUCCESS);
        assertThat(Arrays.stream(applied).map(i -> i.getVersion().getVersion()).toList())
                .containsExactlyElementsOf(availableVersions());
        assertThat(flyway(null).info().pending()).isEmpty();
        assertThatCode(() -> validateSchema(dataSource)).doesNotThrowAnyException();
    }

    @Test
    void migrationIsIdempotent() {
        flyway(null).migrate();
        int applied = flyway(null).info().applied().length;

        flyway(null).migrate();

        assertThat(flyway(null).info().applied()).hasSize(applied);
    }

    @Test
    void upgradesV1DatabaseKeepingData() {
        flyway("1").migrate();
        insertV1Data();

        flyway(null).migrate();

        assertThat(flyway(null).info().pending()).isEmpty();
        assertUpgradedData();
        assertThatCode(() -> validateSchema(dataSource)).doesNotThrowAnyException();
    }

    @Test
    void upgradesPreFlywayDatabaseThroughBaseline() {
        // installations from before Flyway have the V1 schema but no history table
        flyway("1").migrate();
        jdbc.execute("DROP TABLE flyway_schema_history");
        insertV1Data();

        flyway(null).migrate();

        MigrationInfo[] applied = flyway(null).info().applied();
        assertThat(applied[0].getVersion().getVersion()).isEqualTo("1");
        assertThat(applied[0].getType().isBaseline()).isTrue();
        assertThat(flyway(null).info().pending()).isEmpty();
        assertUpgradedData();
        assertThatCode(() -> validateSchema(dataSource)).doesNotThrowAnyException();
    }

    private void insertV1Data() {
        jdbc.update("INSERT INTO manuscripts (id, is_deleted, uuid, name) VALUES (1, false, 'm-1', 'Old book')");
        jdbc.update("INSERT INTO lorebooks (id, is_deleted, uuid, name, enabled) VALUES (1, false, 'l-1', 'A', true)");
        jdbc.update("INSERT INTO lorebooks (id, is_deleted, uuid, name, enabled) VALUES (2, false, 'l-2', 'B', true)");
        jdbc.update("INSERT INTO lorebooks (id, is_deleted, uuid, name, enabled) VALUES (3, false, 'l-3', 'Shared', true)");
        jdbc.update("INSERT INTO lorebooks_lorebooks (Lorebook_id, subbooks_id) VALUES (1, 3)");
    }

    private void assertUpgradedData() {
        // V4: new columns with defaults
        assertThat(jdbc.queryForObject("SELECT name FROM manuscripts WHERE id = 1", String.class)).isEqualTo("Old book");
        assertThat(jdbc.queryForObject("SELECT published FROM manuscripts WHERE id = 1", Boolean.class)).isFalse();
        assertThat(jdbc.queryForObject("SELECT lastOpened FROM manuscripts WHERE id = 1", Object.class)).isNull();

        // V3: links copied, and a lorebook can now be subbook of several lorebooks
        assertThat(jdbc.queryForList("SELECT subbooks_id FROM lorebooks_lorebooks WHERE Lorebook_id = 1", Long.class))
                .containsExactly(3L);
        jdbc.update("INSERT INTO lorebooks_lorebooks (Lorebook_id, subbooks_id) VALUES (2, 3)");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM lorebooks_lorebooks WHERE subbooks_id = 3", Integer.class))
                .isEqualTo(2);

        // V2: messages reference summaries
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM pragma_table_info('messages') WHERE name = 'summary_id'", Integer.class))
                .isEqualTo(1);
    }
}
