package com.github.enerccio.marginalia.bound.migration;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.bound.Migration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * App version 1 → 2: encrypts API keys stored in plain text. Works on the table directly, the entity
 * already expects encrypted values.
 */
public class EncryptApiKeysMigration implements Migration {
    private static final Logger log = LoggerFactory.getLogger(EncryptApiKeysMigration.class);

    @Autowired
    private Configuration configuration;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public int migrate(int cversion, MigrationType migrationType) throws Exception {
        if (migrationType != MigrationType.APP || cversion != 1) {
            return cversion;
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select id, apiKey from ais_openaicompat");
        int encrypted = 0;
        for (Map<String, Object> row : rows) {
            Object apiKey = row.get("apiKey");
            if (apiKey == null || configuration.isEncrypted(apiKey.toString()) || apiKey.toString().isEmpty()) {
                continue;
            }
            jdbcTemplate.update("update ais_openaicompat set apiKey = ? where id = ?",
                    configuration.encrypt(apiKey.toString()), row.get("id"));
            encrypted++;
        }
        log.info("Encrypted {} API keys", encrypted);
        return 2;
    }
}
