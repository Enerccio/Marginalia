package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookReplaceService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.search.replace.EntityTextSearch;
import com.github.enerccio.marginalia.domain.service.search.replace.EntityTextSearch.FieldChange;
import com.github.enerccio.marginalia.domain.service.search.replace.TaskProgress;
import com.github.enerccio.marginalia.domain.service.search.replace.TextSearch;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LorebookReplaceServiceImpl implements LorebookReplaceService {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    /**
     * An entry to change.
     */
    private record EntryChange(Long id, List<FieldChange> fields) {

        int hits() {
            return fields.stream().mapToInt(f -> f.hits().size()).sum();
        }
    }

    private record PlanImpl(Long lorebookId, List<Item> items, List<EntryChange> changes) implements Plan {

        @Override
        public int hitCount() {
            return items.stream().mapToInt(item -> item.hits().size()).sum();
        }
    }

    @Override
    @CommonTxReadOnly
    public Plan plan(Lorebook lorebook, Options options, TaskProgress progress) throws Exception {
        Lorebook owned = owned(lorebook);
        TextSearch search = TextSearch.of(options.search(), options.replacement(), options.regex(), options.caseSensitive());

        // the database narrows the entries down by _fulltext, the fields of those are then searched
        List<LorebookEntryService.EntryFulltext> rows = lorebookEntryService.findFulltextsContaining(owned, search.literal()).stream()
                .filter(row -> search.matches(row.fulltext()))
                .toList();
        progress.checkCancelled();

        List<Item> items = new ArrayList<>();
        List<EntryChange> changes = new ArrayList<>();
        long done = 0;
        for (LorebookEntryService.EntryFulltext row : rows) {
            progress.checkCancelled();
            LorebookEntry entry = lorebookEntryService.find(row.entryId());
            List<FieldChange> fields = EntityTextSearch.search(entry, search);
            if (!fields.isEmpty()) {
                EntryChange change = new EntryChange(entry.getId(), fields);
                changes.add(change);
                items.add(new Item(entry.getId(), entry.getName(),
                        fields.stream().flatMap(f -> f.hits().stream()).toList()));
            }
            progress.update(++done, rows.size());
        }
        return new PlanImpl(owned.getId(), items, changes);
    }

    @Override
    @CommonTx
    public Result apply(Lorebook lorebook, Plan plan, TaskProgress progress) throws Exception {
        Lorebook owned = owned(lorebook);
        PlanImpl planned = (PlanImpl) plan;
        if (!Objects.equals(planned.lorebookId(), owned.getId())) {
            throw new IllegalArgumentException("The plan is of another lorebook");
        }

        int changed = 0;
        int hits = 0;
        int skipped = 0;
        long done = 0;
        for (EntryChange change : planned.changes()) {
            progress.update(done++, planned.changes().size());
            // saving serializes the fields of the object it gets, a lazy proxy has none
            LorebookEntry entry = Hibernate.unproxy(lorebookEntryService.find(change.id()), LorebookEntry.class);
            if (entry == null || entry.isDeleted() || !EntityTextSearch.hasTexts(entry, change.fields())) {
                skipped++;
                continue;
            }
            EntityTextSearch.apply(entry, change.fields());
            lorebookEntryService.save(entry);
            changed++;
            hits += change.hits();
        }
        progress.update(planned.changes().size(), planned.changes().size());
        return new Result(changed, hits, skipped);
    }

    private Lorebook owned(Lorebook lorebook) throws Exception {
        Lorebook owned = lorebook == null ? null : lorebookService.findForUser(lorebook.getUuid());
        if (owned == null) {
            throw new IllegalArgumentException("The lorebook is not available");
        }
        return owned;
    }

}
