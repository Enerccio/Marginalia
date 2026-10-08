package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.OwnedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class LorebookEntryCrudTest extends ExtendableCrudContract<LorebookEntry> {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    private Lorebook lorebook;

    @BeforeEach
    void createLorebook() throws Exception {
        Lorebook l = new Lorebook();
        l.setName("lore");
        lorebook = lorebookService.save(l);
    }

    @Override
    protected OwnedService<LorebookEntry, ?> service() {
        return lorebookEntryService;
    }

    @Override
    protected LorebookEntry newEntity() {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName("Dragons");
        entry.setPayload("Dragons breathe fire.");
        entry.setComment("internal note");
        entry.setEnabled(false);
        entry.setOrder(5);
        entry.setFiltering("drag(on|ons)");
        entry.setFilteringMode(FilteringMode.REGEX);
        entry.setInsertionMode(InsertionMode.BEFORE_USER_PROMPT);
        return entry;
    }

    @Override
    protected void assertCreated(LorebookEntry loaded) {
        assertThat(idOf(loaded.getLorebook())).isEqualTo(lorebook.getId());
        assertThat(loaded.getName()).isEqualTo("Dragons");
        assertThat(loaded.getPayload()).isEqualTo("Dragons breathe fire.");
        assertThat(loaded.getComment()).isEqualTo("internal note");
        assertThat(loaded.isEnabled()).isFalse();
        assertThat(loaded.getOrder()).isEqualTo(5);
        assertThat(loaded.getFiltering()).isEqualTo("drag(on|ons)");
        assertThat(loaded.getFilteringMode()).isEqualTo(FilteringMode.REGEX);
        assertThat(loaded.getInsertionMode()).isEqualTo(InsertionMode.BEFORE_USER_PROMPT);
    }

    @Override
    protected void modify(LorebookEntry entity) {
        entity.setPayload("Dragons sleep.");
        entity.setComment(null);
        entity.setFilteringMode(FilteringMode.TEXT);
        entity.setEnabled(true);
        entity.setOrder(1);
    }

    @Override
    protected void assertModified(LorebookEntry loaded) {
        assertThat(loaded.getPayload()).isEqualTo("Dragons sleep.");
        assertThat(loaded.getComment()).isNull();
        assertThat(loaded.getFilteringMode()).isEqualTo(FilteringMode.TEXT);
        assertThat(loaded.getInsertionMode()).isEqualTo(InsertionMode.BEFORE_USER_PROMPT);
        assertThat(loaded.isEnabled()).isTrue();
        assertThat(loaded.getOrder()).isEqualTo(1);
    }

    @Test
    void defaultsSurviveRoundTrip() throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName("plain");

        LorebookEntry loaded = reload(lorebookEntryService.save(entry));

        assertThat(loaded.isEnabled()).isTrue();
        assertThat(loaded.getOrder()).isEqualTo(100);
        assertThat(loaded.getFilteringMode()).isEqualTo(FilteringMode.TEXT);
        assertThat(loaded.getInsertionMode()).isEqualTo(InsertionMode.IN_LORE_BLOCK);
        assertThat(loaded.getPayload()).isNull();
    }

    @Test
    void entriesOfLorebookAreOrderedAndExcludeDeleted() throws Exception {
        LorebookEntry third = save("third", 30);
        LorebookEntry first = save("first", 10);
        LorebookEntry second = save("second", 20);
        LorebookEntry deleted = save("deleted", 15);
        lorebookEntryService.delete(deleted, false);

        Lorebook otherBook = new Lorebook();
        otherBook.setName("other");
        otherBook = lorebookService.save(otherBook);
        LorebookEntry foreign = newEntity();
        foreign.setLorebook(otherBook);
        lorebookEntryService.save(foreign);

        assertThat(lorebookEntryService.getEntriesForLorebook(lorebook)).extracting(BaseEntity::getId)
                .containsExactly(first.getId(), second.getId(), third.getId());
        assertThat(lorebookEntryService.getEntriesForLorebook(lorebook.getId())).hasSize(3);
        assertThat(lorebookEntryService.getEntriesForLorebook((Lorebook) null)).isEmpty();
        assertThat(lorebookEntryService.getEntriesForLorebook((Long) null)).isEmpty();
    }

    private LorebookEntry save(String name, int order) throws Exception {
        LorebookEntry entry = newEntity();
        entry.setName(name);
        entry.setOrder(order);
        return lorebookEntryService.save(entry);
    }
}
