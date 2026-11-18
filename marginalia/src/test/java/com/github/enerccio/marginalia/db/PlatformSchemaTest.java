package com.github.enerccio.marginalia.db;

import com.github.enerccio.marginalia.DatabaseCheck;
import com.github.enerccio.marginalia.MarginaliaDatabase;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The platform ships no migrations: its own tests run on test-only migrations of the platform tables
 * ({@code platform-test-migration} in the platform's test-jar), written from Marginalia's schema. This keeps them in
 * step: every table they create exists in a database migrated by Marginalia's migrations, with the same columns (name,
 * type, not null, primary key) and the same indexes. A change of a platform table in a Marginalia migration fails here
 * until the platform's test migrations get the same change (a new migration there).
 */
class PlatformSchemaTest {

    static final DatabaseCheck.Schema PLATFORM_TESTS = new DatabaseCheck.Schema("classpath:platform-test-migration",
            DatabaseCheck.Schema.PLATFORM.coreTables());

    @TempDir
    Path tempDir;

    @Test
    void platformTestTablesMatchMarginaliasSchema() {
        Map<String, List<String>> platform = schema(migrate("platform.sqlite", PLATFORM_TESTS));
        Map<String, List<String>> marginalia = schema(migrate("marginalia.sqlite", MarginaliaDatabase.SCHEMA));

        assertThat(platform.keySet()).as("tables of the platform's test migrations")
                .containsAll(DatabaseCheck.Schema.PLATFORM.coreTables());
        Map<String, List<String>> expected = new TreeMap<>();
        platform.keySet().forEach(table -> expected.put(table, marginalia.get(table)));
        assertThat(platform).as("the platform tables (platform-test-migration) against Marginalia's migrations")
                .isEqualTo(expected);
    }

    private JdbcTemplate migrate(String file, DatabaseCheck.Schema schema) {
        SingleConnectionDataSource dataSource = new SingleConnectionDataSource("jdbc:sqlite:" + tempDir.resolve(file), true);
        dataSource.setDriverClassName("org.sqlite.JDBC");
        new Flyway(DatabaseCheck.flywayConfiguration(dataSource, schema)).migrate();
        return new JdbcTemplate(dataSource);
    }

    /**
     * Table to its columns and indexes, as comparable lines; Flyway's history table and Hibernate's temporary tables
     * ({@code HTE_*}, {@code HT_*}, Hibernate creates them when it needs them) left out.
     */
    private static Map<String, List<String>> schema(JdbcTemplate jdbc) {
        Map<String, List<String>> tables = new TreeMap<>();
        List<String> names = jdbc.queryForList("SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'"
                + " AND name <> 'flyway_schema_history' AND name NOT LIKE 'HTE\\_%' ESCAPE '\\' AND name NOT LIKE 'HT\\_%' ESCAPE '\\'"
                + " ORDER BY name", String.class);
        for (String table : names) {
            List<String> lines = new ArrayList<>(jdbc.query("SELECT name, upper(type) AS type, \"notnull\", pk FROM pragma_table_info(?)"
                            + " ORDER BY name",
                    (rs, i) -> "column " + rs.getString("name") + " " + rs.getString("type")
                            + (rs.getInt("notnull") != 0 ? " not null" : "") + (rs.getInt("pk") != 0 ? " pk" + rs.getInt("pk") : ""),
                    table));
            lines.addAll(jdbc.query("SELECT name, \"unique\" FROM pragma_index_list(?) WHERE origin = 'c' ORDER BY name",
                    (rs, i) -> "index " + rs.getString("name") + (rs.getInt("unique") != 0 ? " unique" : ""), table));
            tables.put(table, lines);
        }
        return tables;
    }
}
