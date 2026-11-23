package com.github.enerccio.marginalia;

import java.util.List;

/**
 * Marginalia's database: its Flyway migrations ({@code src/main/resources/migration}) and the tables every Marginalia
 * database has (created by V1), set on the {@code configuration} bean, which checks a database against it on start,
 * on a restore and before migrating it.
 */
public final class MarginaliaDatabase {

    public static final DatabaseCheck.Schema SCHEMA = new DatabaseCheck.Schema("classpath:migration",
            List.of("users", "settings", "manuscripts", "messages", "lorebooks", "entries", "ais", "protocols", "tags",
                    "t2e"));

    private MarginaliaDatabase() {
    }
}
