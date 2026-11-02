package com.github.enerccio.marginalia.db;

import com.github.enerccio.marginalia.DatabaseCheck;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

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
     * The configuration of the {@code flywayConfiguration} bean in datasources-config.xml.
     */
    private Flyway flyway(String target) {
        ClassicConfiguration configuration = DatabaseCheck.flywayConfiguration(dataSource);
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

    @Test
    void upgradesV12UsagesThroughTheV13TableRebuild() {
        flyway("12").migrate();
        // V11 made usages.ai_id NOT NULL, V12 added the period columns
        jdbc.update("INSERT INTO usages (id, creation, is_deleted, modification, uuid, userId, ai_id, prefillTokens, "
                + "generatedTokens, requests, lastUsed, periodPrefillTokens, periodGeneratedTokens) "
                + "VALUES (1, '2026-01-01 10:00:00', false, '2026-01-02 10:00:00', 'u-1', 7, 3, 1200, 340, 9, "
                + "'2026-01-02 09:00:00', 200, 40)");
        jdbc.update("INSERT INTO usages (id, is_deleted, uuid, userId, ai_id) VALUES (2, true, 'u-2', 8, 3)");
        assertThatThrownBy(() -> jdbc.update("INSERT INTO usages (id, is_deleted, uuid, userId, ai_id) "
                + "VALUES (3, false, 'u-3', 9, NULL)")).isInstanceOf(DataAccessException.class);

        flyway(null).migrate();

        assertThat(flyway(null).info().pending()).isEmpty();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM usages", Integer.class)).isEqualTo(2);
        assertThat(jdbc.queryForMap("SELECT * FROM usages WHERE id = 1"))
                .containsEntry("uuid", "u-1").containsEntry("userId", 7).containsEntry("ai_id", 3)
                .containsEntry("prefillTokens", 1200).containsEntry("generatedTokens", 340)
                .containsEntry("requests", 9).containsEntry("periodPrefillTokens", 200)
                .containsEntry("periodGeneratedTokens", 40)
                .containsEntry("creation", "2026-01-01 10:00:00").containsEntry("lastUsed", "2026-01-02 09:00:00");
        // the defaults of the rebuilt table apply to the row that gave only the required columns
        assertThat(jdbc.queryForMap("SELECT * FROM usages WHERE id = 2"))
                .containsEntry("prefillTokens", 0).containsEntry("generatedTokens", 0).containsEntry("requests", 0)
                .containsEntry("periodPrefillTokens", 0).containsEntry("periodGeneratedTokens", 0);
        assertThat(jdbc.queryForObject("SELECT is_deleted FROM usages WHERE id = 2", Boolean.class)).isTrue();

        // both indexes were recreated and are used
        assertThat(jdbc.queryForList("SELECT name FROM sqlite_master WHERE type = 'index' AND tbl_name = 'usages'",
                String.class)).contains("ux_usages_ai_user", "ix_usages_user");
        assertThat(queryPlan("SELECT id FROM usages WHERE userId = 7 AND is_deleted = false"))
                .contains("ix_usages_user");
        // ai_id may be NULL now (purging an AI nulls it), and the pair is still unique
        jdbc.update("INSERT INTO usages (id, is_deleted, uuid, userId, ai_id) VALUES (3, false, 'u-3', 9, NULL)");
        assertThatThrownBy(() -> jdbc.update("INSERT INTO usages (id, is_deleted, uuid, userId, ai_id) "
                + "VALUES (4, false, 'u-4', 7, 3)")).isInstanceOf(DataAccessException.class);
        assertThatCode(() -> validateSchema(dataSource)).doesNotThrowAnyException();
    }

    @Test
    void tagRelationLookupsUseIndexes() {
        flyway(null).migrate();

        List<String> indexes = jdbc.queryForList("SELECT name FROM sqlite_master WHERE type = 'index' AND tbl_name = 't2e'", String.class);
        assertThat(indexes).contains("ix_t2e_object", "ix_t2e_tag")
                .doesNotContain("ix__tag__id_clazz", "ix__tag__id_clazz_neg");

        // V6: lookups by object and by tag no longer scan the table
        assertThat(queryPlan("SELECT tag_id FROM t2e WHERE objectId = 1 AND clazz = 'x' AND negative = false AND is_deleted = false"))
                .contains("ix_t2e_object");
        assertThat(queryPlan("SELECT objectId FROM t2e WHERE tag_id = 1 AND clazz = 'x' AND negative = false AND is_deleted = false"))
                .contains("ix_t2e_tag");
        assertThat(queryPlan("SELECT id FROM entries WHERE lorebook_id = 1 AND is_deleted = false ORDER BY ordinal"))
                .contains("ix_entries_lorebook").doesNotContain("TEMP B-TREE");
    }

    private String queryPlan(String sql) {
        return String.join("\n", jdbc.queryForList("EXPLAIN QUERY PLAN " + sql).stream()
                .map(row -> String.valueOf(row.get("detail"))).toList());
    }

    private void insertV1Data() {
        jdbc.update("INSERT INTO manuscripts (id, is_deleted, uuid, name) VALUES (1, false, 'm-1', 'Old book')");
        // manuscripts.description was a column and an extended attribute: V9 keeps what only the column has
        jdbc.update("INSERT INTO manuscripts (id, is_deleted, uuid, name, description) VALUES (2, false, 'm-2', 'Column only', 'From column')");
        jdbc.update("INSERT INTO manuscripts (id, is_deleted, uuid, name, description, extendedContent) VALUES (3, false, 'm-3', 'Other keys', 'Merged', ?)",
                (Object) "{\"style\":\"terse\"}".getBytes(StandardCharsets.UTF_8));
        jdbc.update("INSERT INTO manuscripts (id, is_deleted, uuid, name, description, extendedContent) VALUES (4, false, 'm-4', 'Both', 'Stale column', ?)",
                (Object) "{\"description\":\"Extended wins\"}".getBytes(StandardCharsets.UTF_8));
        jdbc.update("INSERT INTO lorebooks (id, is_deleted, uuid, name, enabled) VALUES (1, false, 'l-1', 'A', true)");
        jdbc.update("INSERT INTO lorebooks (id, is_deleted, uuid, name, enabled) VALUES (2, false, 'l-2', 'B', true)");
        jdbc.update("INSERT INTO lorebooks (id, is_deleted, uuid, name, enabled) VALUES (3, false, 'l-3', 'Shared', true)");
        jdbc.update("INSERT INTO lorebooks_lorebooks (Lorebook_id, subbooks_id) VALUES (1, 3)");
        jdbc.update("INSERT INTO protocols (id, is_deleted, uuid, name, protocolType, maxTokens, replyTokens) VALUES (1, false, 'p-1', 'Set', 0, 8000, 500)");
        jdbc.update("INSERT INTO protocols (id, is_deleted, uuid, name, protocolType, maxTokens, replyTokens) VALUES (2, false, 'p-2', 'Unset', 0, -1, 0)");
    }

    private String extendedContent(int manuscriptId) {
        return jdbc.queryForObject("SELECT CAST(extendedContent AS TEXT) FROM manuscripts WHERE id = " + manuscriptId, String.class);
    }

    private void assertUpgradedData() {
        // V9: description lives only in extendedContent, every extendable table has _fulltext
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM pragma_table_info('manuscripts') WHERE name = 'description'", Integer.class))
                .isZero();
        for (String table : List.of("ais", "entries", "lorebooks", "manuscripts", "messages", "protocols", "settings",
                "summaries", "t2e", "tags")) {
            assertThat(jdbc.queryForObject(
                    "SELECT COUNT(*) FROM pragma_table_info('" + table + "') WHERE name = '_fulltext'", Integer.class))
                    .as(table).isEqualTo(1);
        }
        assertThat(extendedContent(1)).isNull();
        assertThat(JsonParser.parseString(extendedContent(2)).getAsJsonObject().get("description").getAsString())
                .isEqualTo("From column");
        JsonObject merged = JsonParser.parseString(extendedContent(3)).getAsJsonObject();
        assertThat(merged.get("description").getAsString()).isEqualTo("Merged");
        assertThat(merged.get("style").getAsString()).isEqualTo("terse");
        assertThat(JsonParser.parseString(extendedContent(4)).getAsJsonObject().get("description").getAsString())
                .isEqualTo("Extended wins");

        // V5: protocol limits are optional, old "not set" values (0, negative) became NULL
        assertThat(jdbc.queryForMap("SELECT maxTokens, replyTokens, name FROM protocols WHERE id = 1"))
                .containsEntry("maxTokens", 8000).containsEntry("replyTokens", 500).containsEntry("name", "Set");
        assertThat(jdbc.queryForMap("SELECT maxTokens, replyTokens FROM protocols WHERE id = 2"))
                .containsEntry("maxTokens", null).containsEntry("replyTokens", null);
        jdbc.update("INSERT INTO protocols (id, is_deleted, uuid, name) VALUES (3, false, 'p-3', 'No limits')");

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
