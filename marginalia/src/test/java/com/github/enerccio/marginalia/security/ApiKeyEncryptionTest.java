package com.github.enerccio.marginalia.security;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.bound.Migration.MigrationType;
import com.github.enerccio.marginalia.bound.migration.EncryptApiKeysMigration;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API keys are stored encrypted with the installation key (BUG-29), plain keys are encrypted by the app migration.
 */
class ApiKeyEncryptionTest extends MarginaliaTestBase {

    @Autowired
    private Configuration configuration;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationContext context;

    private String storedApiKey(Long id) {
        return jdbcTemplate.queryForObject("select apiKey from ais_openaicompat where id = ?", String.class, id);
    }

    @Test
    void secretKeyFileIsCreatedInApplicationFolder() {
        assertThat(new File(configuration.getFolder(), Configuration.SECRET_KEY_FILE)).isFile();
    }

    @Test
    void apiKeyIsEncryptedInDatabaseAndDecryptedOnLoad() throws Exception {
        login();
        OpenAICompatible ai = createAI();

        String stored = storedApiKey(ai.getId());
        assertThat(stored).startsWith(Configuration.ENCRYPTED_PREFIX).doesNotContain("test-key");
        assertThat(((OpenAICompatible) aiService.find(ai.getId())).getApiKey()).isEqualTo("test-key");
    }

    @Test
    void encryptionUsesRandomIv() {
        String first = configuration.encrypt("secret");
        String second = configuration.encrypt("secret");
        assertThat(first).isNotEqualTo(second);
        assertThat(configuration.decrypt(first)).isEqualTo("secret");
        assertThat(configuration.decrypt(second)).isEqualTo("secret");
    }

    @Test
    void valueEncryptedWithAnotherKeyDecryptsToNull() {
        assertThat(configuration.decrypt(Configuration.ENCRYPTED_PREFIX + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")).isNull();
    }

    @Test
    void migrationEncryptsPlainApiKeys() throws Exception {
        login();
        OpenAICompatible ai = createAI();
        jdbcTemplate.update("update ais_openaicompat set apiKey = ? where id = ?", "plain-key", ai.getId());

        EncryptApiKeysMigration migration = context.getAutowireCapableBeanFactory().createBean(EncryptApiKeysMigration.class);
        assertThat(migration.migrate(1, MigrationType.DB)).isEqualTo(1);
        assertThat(storedApiKey(ai.getId())).isEqualTo("plain-key");

        assertThat(migration.migrate(1, MigrationType.APP)).isEqualTo(2);
        String stored = storedApiKey(ai.getId());
        assertThat(stored).startsWith(Configuration.ENCRYPTED_PREFIX);
        assertThat(configuration.decrypt(stored)).isEqualTo("plain-key");
        assertThat(((OpenAICompatible) aiService.find(ai.getId())).getApiKey()).isEqualTo("plain-key");

        assertThat(migration.migrate(2, MigrationType.APP)).isEqualTo(2);
        assertThat(storedApiKey(ai.getId())).isEqualTo(stored);
    }
}
