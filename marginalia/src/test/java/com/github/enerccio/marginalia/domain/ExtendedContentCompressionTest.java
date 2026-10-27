package com.github.enerccio.marginalia.domain;

import com.github.enerccio.marginalia.bound.Migration.MigrationType;
import com.github.enerccio.marginalia.bound.migration.ExtendedContentMigration;
import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The extended content of an entity is stored gzipped: what it reads as, what an older row (stored plain) reads as, and
 * the migration that compresses the old rows.
 */
class ExtendedContentCompressionTest extends MarginaliaTestBase {

    private static final String BIG = "{\"text\":\"" + "Příliš žluťoučký kůň úpěl ďábelské ódy. ".repeat(500) + "\"}";

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private AutowireCapableBeanFactory beanFactory;

    @BeforeEach
    void logIn() throws Exception {
        login();
    }

    private static byte[] stored(ExtendableEntity entity) throws Exception {
        Field field = ExtendableEntity.class.getDeclaredField("extendedContent");
        field.setAccessible(true);
        return (byte[]) field.get(entity);
    }

    private static void storePlain(ExtendableEntity entity, String content) throws Exception {
        Field field = ExtendableEntity.class.getDeclaredField("extendedContent");
        field.setAccessible(true);
        field.set(entity, content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void bigContentIsStoredSmallerAndReadAsItWas() throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.setExtendedContent(BIG.getBytes(StandardCharsets.UTF_8));

        assertThat(stored(manuscript).length).isLessThan(BIG.getBytes(StandardCharsets.UTF_8).length / 5);
        assertThat(new String(manuscript.getExtendedContent(), StandardCharsets.UTF_8)).isEqualTo(BIG);
        assertThat(manuscript.needsCompression()).isFalse();
    }

    @Test
    void smallContentAndNothingAreLeftAlone() {
        Manuscript manuscript = new Manuscript();
        manuscript.setExtendedContent("{\"a\":1}".getBytes(StandardCharsets.UTF_8));
        assertThat(manuscript.getExtendedContent()).isEqualTo("{\"a\":1}".getBytes(StandardCharsets.UTF_8));
        assertThat(manuscript.needsCompression()).isFalse();

        manuscript.setExtendedContent(null);
        assertThat(manuscript.getExtendedContent()).isNull();
    }

    @Test
    void anEntityKeepsItsAttributesThroughTheDatabase() throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript.getAttributes().addProperty("plugin", "x".repeat(5000));
        Manuscript saved = manuscriptService.save(manuscript);

        Manuscript loaded = manuscriptService.find(saved.getId());
        assertThat(loaded.getAttributes().get("plugin").getAsString()).isEqualTo("x".repeat(5000));
        assertThat(loaded.needsCompression()).isFalse();
        assertThat(stored(loaded).length).isLessThan(1000);
    }

    @Test
    void theMigrationCompressesTheRowsStoredPlain() throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        Manuscript saved = manuscriptService.save(manuscript);
        // a row as the version before wrote it
        Manuscript legacy = manuscriptService.find(saved.getId());
        storePlain(legacy, "{\"attributes_plugin\":\"" + "y".repeat(5000) + "\"}");
        manuscriptService.saveWithoutEvent(legacy);
        assertThat(manuscriptService.find(saved.getId()).needsCompression()).isTrue();

        ExtendedContentMigration migration = new ExtendedContentMigration();
        beanFactory.autowireBean(migration);
        assertThat(migration.migrate(5, MigrationType.APP)).isEqualTo(6);

        Manuscript migrated = manuscriptService.find(saved.getId());
        assertThat(migrated.needsCompression()).isFalse();
        assertThat(migrated.getAttributes().get("plugin").getAsString()).isEqualTo("y".repeat(5000));
        assertThat(stored(migrated).length).isLessThan(1000);
    }
}
