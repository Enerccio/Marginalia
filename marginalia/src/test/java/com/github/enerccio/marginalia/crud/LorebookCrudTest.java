package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LorebookCrudTest extends ExtendableCrudContract<Lorebook> {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    @Override
    protected OwnedService<Lorebook, ?> service() {
        return lorebookService;
    }

    @Override
    protected Lorebook newEntity() {
        Lorebook lorebook = new Lorebook();
        lorebook.setName("World lore");
        lorebook.setEnabled(false);
        return lorebook;
    }

    @Override
    protected void assertCreated(Lorebook loaded) {
        assertThat(loaded.getName()).isEqualTo("World lore");
        assertThat(loaded.isEnabled()).isFalse();
        assertThat(loaded.getSubbooks()).isNullOrEmpty();
    }

    @Override
    protected void modify(Lorebook entity) {
        entity.setName("Renamed lore");
        entity.setEnabled(true);
    }

    @Override
    protected void assertModified(Lorebook loaded) {
        assertThat(loaded.getName()).isEqualTo("Renamed lore");
        assertThat(loaded.isEnabled()).isTrue();
    }

    private Lorebook lorebook(String name, Lorebook... subbooks) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(name);
        lorebook.setSubbooks(new ArrayList<>(List.of(subbooks)));
        return lorebookService.save(lorebook);
    }

    private LorebookEntry entry(Lorebook lorebook, String name, int order) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        entry.setOrder(order);
        return lorebookEntryService.save(entry);
    }

    private Tag tag(String value) throws Exception {
        Tag tag = new Tag();
        tag.setValue(value);
        return tagService.save(tag);
    }

    @Test
    void subbooksArePersistedAndCanBeShared() throws Exception {
        Lorebook shared = lorebook("shared");
        Lorebook other = lorebook("other");
        Lorebook first = lorebook("first", shared, other);
        Lorebook second = lorebook("second", shared);

        assertThat(lorebookService.getSubbooks(first)).extracting(BaseEntity::getId)
                .containsExactlyInAnyOrder(shared.getId(), other.getId());
        assertThat(lorebookService.getSubbooks(second)).extracting(BaseEntity::getId).containsExactly(shared.getId());

        Lorebook loaded = reload(first);
        loaded.getSubbooks().removeIf(l -> l.getId().equals(other.getId()));
        lorebookService.save(loaded);
        assertThat(lorebookService.getSubbooks(first)).extracting(BaseEntity::getId).containsExactly(shared.getId());
    }

    @Test
    void deletedSubbooksAreSkipped() throws Exception {
        Lorebook sub = lorebook("sub");
        Lorebook root = lorebook("root", sub);

        lorebookService.delete(reload(sub), false);

        assertThat(lorebookService.getSubbooks(root)).isEmpty();
    }

    @Test
    void fillEntriesMergesBookAndEntryTags() throws Exception {
        Lorebook book = lorebook("tagged");
        LorebookEntry late = entry(book, "late", 200);
        LorebookEntry early = entry(book, "early", 10);
        LorebookEntry deleted = entry(book, "deleted", 50);
        lorebookEntryService.delete(deleted, false);

        tagRelationService.createRelation(tag("world"), book);
        tagRelationService.createRelation(tag("dragon"), early);
        tagRelationService.createRelation(tag("night"), early, true);

        Lorebook filled = lorebookService.fillEntries(reload(book));

        assertThat(filled.getCachedEntries()).extracting(BaseEntity::getId).containsExactly(early.getId(), late.getId());
        LorebookEntry filledEarly = filled.getCachedEntries().getFirst();
        assertThat(filledEarly.getCachedTags()).containsExactlyInAnyOrder("world", "dragon");
        assertThat(filledEarly.getCachedNegativeTags()).containsExactly("night");
        LorebookEntry filledLate = filled.getCachedEntries().getLast();
        assertThat(filledLate.getCachedTags()).containsExactly("world");
        assertThat(filledLate.getCachedNegativeTags()).isEmpty();
    }
}
