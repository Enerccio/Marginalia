package com.github.enerccio.marginalia.db;

import com.github.enerccio.marginalia.bound.Migration.MigrationType;
import com.github.enerccio.marginalia.bound.migration.FulltextMigration;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The app migration 2 → 3 fills {@code _fulltext} of rows saved before the column existed.
 */
class FulltextMigrationTest extends MarginaliaTestBase {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    private FulltextMigration migration() {
        return context.getAutowireCapableBeanFactory().createBean(FulltextMigration.class);
    }

    private String fulltext(String table, Long id) {
        return jdbc.queryForObject("SELECT CAST(_fulltext AS TEXT) FROM " + table + " WHERE id = ?", String.class, id);
    }

    @Test
    void fillsRowsWithoutFulltext() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript.setDescription("Smugglers of the north coast");
        manuscript = manuscriptService.save(manuscript);

        Lorebook lorebook = new Lorebook();
        lorebook.setName(uniqueName("lore"));
        lorebook = lorebookService.save(lorebook);
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName("Harbour");
        entry.setPayload("A foggy place");
        entry = lorebookEntryService.save(entry);

        assertThat(fulltext("manuscripts", manuscript.getId())).isEqualTo(manuscript.getName() + "\nSmugglers of the north coast");
        assertThat(fulltext("entries", entry.getId())).isEqualTo("Harbour\nA foggy place");

        // rows from before the column, one of them soft deleted
        jdbc.update("UPDATE manuscripts SET _fulltext = NULL, is_deleted = true WHERE id = ?", manuscript.getId());
        jdbc.update("UPDATE entries SET _fulltext = NULL WHERE id = ?", entry.getId());
        jdbc.update("UPDATE lorebooks SET _fulltext = NULL WHERE id = ?", lorebook.getId());
        String extendedBefore = jdbc.queryForObject("SELECT CAST(extendedContent AS TEXT) FROM entries WHERE id = ?", String.class, entry.getId());

        assertThat(migration().migrate(2, MigrationType.APP)).isEqualTo(3);

        assertThat(fulltext("manuscripts", manuscript.getId())).isEqualTo(manuscript.getName() + "\nSmugglers of the north coast");
        assertThat(fulltext("entries", entry.getId())).isEqualTo("Harbour\nA foggy place");
        assertThat(fulltext("lorebooks", lorebook.getId())).isEqualTo(lorebook.getName());
        assertThat(jdbc.queryForObject("SELECT CAST(extendedContent AS TEXT) FROM entries WHERE id = ?", String.class, entry.getId()))
                .isEqualTo(extendedBefore);
    }

    @Test
    void onlyRunsForAppVersionTwo() throws Exception {
        assertThat(migration().migrate(1, MigrationType.APP)).isEqualTo(1);
        assertThat(migration().migrate(3, MigrationType.APP)).isEqualTo(3);
        assertThat(migration().migrate(2, MigrationType.DB)).isEqualTo(2);
    }
}
