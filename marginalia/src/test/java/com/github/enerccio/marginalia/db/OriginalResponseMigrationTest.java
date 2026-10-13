package com.github.enerccio.marginalia.db;

import com.github.enerccio.marginalia.bound.Migration.MigrationType;
import com.github.enerccio.marginalia.bound.migration.OriginalResponseMigration;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The app migration 3 → 4 copies {@code response} into {@code originalResponse} of the parts saved before it existed.
 */
class OriginalResponseMigrationTest extends MarginaliaTestBase {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    private OriginalResponseMigration migration() {
        return context.getAutowireCapableBeanFactory().createBean(OriginalResponseMigration.class);
    }

    private ChatMessage save(Manuscript manuscript, String response, String original) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setParentScript(manuscript);
        message.setResponse(response);
        message.setOriginalResponse(original);
        return chatMessageService.save(message);
    }

    @Test
    void copiesResponsesIncludingDeletedOnes() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript = manuscriptService.save(manuscript);

        ChatMessage plain = save(manuscript, "The harbour was quiet.", null);
        ChatMessage deleted = save(manuscript, "A gull screamed.", null);
        ChatMessage keeps = save(manuscript, "Edited by the author.", "As the LLM wrote it.");
        ChatMessage empty = save(manuscript, null, null);
        jdbc.update("UPDATE messages SET is_deleted = true WHERE id = ?", deleted.getId());
        Object modification = jdbc.queryForObject("SELECT modification FROM messages WHERE id = ?", Object.class, plain.getId());

        assertThat(migration().migrate(3, MigrationType.APP)).isEqualTo(4);

        assertThat(chatMessageService.find(plain.getId()).getOriginalResponse()).isEqualTo("The harbour was quiet.");
        assertThat(chatMessageService.find(deleted.getId()).getOriginalResponse()).isEqualTo("A gull screamed.");
        assertThat(chatMessageService.find(keeps.getId()).getOriginalResponse()).isEqualTo("As the LLM wrote it.");
        assertThat(chatMessageService.find(keeps.getId()).getResponse()).isEqualTo("Edited by the author.");
        assertThat(chatMessageService.find(empty.getId()).getOriginalResponse()).isNull();
        assertThat(jdbc.queryForObject("SELECT modification FROM messages WHERE id = ?", Object.class, plain.getId()))
                .isEqualTo(modification);
    }

    @Test
    void onlyRunsForAppVersionThree() throws Exception {
        assertThat(migration().migrate(2, MigrationType.APP)).isEqualTo(2);
        assertThat(migration().migrate(4, MigrationType.APP)).isEqualTo(4);
        assertThat(migration().migrate(3, MigrationType.DB)).isEqualTo(3);
    }
}
