package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.repository.LorebookEntryRepository;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LorebookEntryServiceImpl extends ExtendableServiceImpl<LorebookEntry, LorebookEntryRepository> implements LorebookEntryService {

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    @Lazy
    private LorebookService lorebookService;

    /**
     * An entry of a lorebook of a book an agent works on can't be changed, see {@link AgentRunService#checkNotLocked(Lorebook)}.
     */
    @Override
    @CommonTx
    public LorebookEntry save(LorebookEntry entry) throws Exception {
        if (entry != null && entry.getLorebook() != null) {
            agentRunService.checkNotLocked(entry.getLorebook());
        }
        return super.save(entry);
    }

    /**
     * A soft deleted entry takes its tags with it.
     */
    @Override
    @CommonTx
    public LorebookEntry delete(LorebookEntry entry, boolean hard) throws Exception {
        if (entry != null && entry.getLorebook() != null) {
            agentRunService.checkNotLocked(entry.getLorebook());
        }
        if (!hard && entry != null && entry.getId() != null) {
            tagRelationService.deleteForObject(entry, false);
        }
        return super.delete(entry, hard);
    }

    @Override
    @CommonTx
    public EntrySaved create(Lorebook lorebook, LorebookEntry fields) throws Exception {
        Lorebook book = ownLorebook(lorebook);
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(book);
        copyFields(fields == null ? new LorebookEntry() : fields, entry);
        List<EntryProblem> problems = validate(entry);
        return new EntrySaved(save(entry), problems);
    }

    @Override
    @CommonTx
    public EntrySaved update(LorebookEntry entry) throws Exception {
        LorebookEntry stored = entry == null || entry.getUuid() == null ? null : (LorebookEntry) Hibernate.unproxy(findForUser(entry.getUuid()));
        if (stored == null || stored.isDeleted()) {
            throw new IllegalArgumentException("The entry does not exist");
        }
        if (stored != entry) {
            copyFields(entry, stored);
        }
        List<EntryProblem> problems = validate(stored);
        return new EntrySaved(save(stored), problems);
    }

    @Override
    @CommonTxReadOnly
    public List<EntryProblem> validate(LorebookEntry entry) throws Exception {
        List<EntryProblem> problems = new ArrayList<>();
        if (StringUtils.isNotBlank(entry.getPayload())) {
            TemplateService.ValidationResult result = templateService.isValidTemplate(entry.getPayload(), "lorebookEntry", LorebookTemplateData.class);
            if (!result.isValid()) {
                problems.add(new EntryProblem(EntryProblemKind.INVALID_TEMPLATE, "payload", result.errorMessage(), List.of()));
            } else if (result.hasWarnings()) {
                problems.add(new EntryProblem(EntryProblemKind.UNKNOWN_NAME, "payload",
                        "The template uses names that do not exist: " + String.join(", ", result.unknownNames()), result.unknownNames()));
            }
        }
        if (entry.getFilteringMode() == FilteringMode.REGEX && StringUtils.isNotBlank(entry.getFiltering())) {
            try {
                Pattern.compile(entry.getFiltering(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL);
            } catch (PatternSyntaxException e) {
                problems.add(new EntryProblem(EntryProblemKind.INVALID_REGEX, "filtering", e.getDescription(), List.of()));
            }
        }
        return problems;
    }

    @Override
    @CommonTx
    public void deleteEntry(Lorebook lorebook, LorebookEntry entry) throws Exception {
        Lorebook book = ownLorebook(lorebook);
        LorebookEntry stored = entry == null ? null : (LorebookEntry) Hibernate.unproxy(find(entry));
        if (stored == null || stored.isDeleted() || stored.getLorebook() == null || !stored.getLorebook().getId().equals(book.getId())) {
            throw new IllegalArgumentException("The entry does not exist");
        }
        delete(stored, false);
    }

    private Lorebook ownLorebook(Lorebook lorebook) throws Exception {
        Lorebook stored = lorebook == null || lorebook.getUuid() == null ? null : (Lorebook) Hibernate.unproxy(lorebookService.findForUser(lorebook.getUuid()));
        if (stored == null || stored.isDeleted()) {
            throw new IllegalArgumentException("The lorebook does not exist");
        }
        return stored;
    }

    private static void copyFields(LorebookEntry from, LorebookEntry to) {
        to.setName(StringUtils.defaultIfBlank(from.getName(), "Entry"));
        to.setPayload(from.getPayload());
        to.setComment(from.getComment());
        to.setEnabled(from.isEnabled());
        to.setOrder(from.getOrder());
        to.setFiltering(from.getFiltering());
        to.setFilteringMode(from.getFilteringMode());
        to.setInsertionMode(from.getInsertionMode());
    }

    @Override
    @CommonTxReadOnly
    public List<LorebookEntry> getEntriesForLorebook(Long lorebookId) throws Exception {
        if (lorebookId == null) {
            return Collections.emptyList();
        }
        return getRepository().findByLorebookOrdered(lorebookId);
    }

    @Override
    @CommonTxReadOnly
    public List<LorebookEntry> getEntriesForLorebook(Lorebook lorebook) throws Exception {
        if (lorebook == null || lorebook.getId() == null) {
            return Collections.emptyList();
        }
        return getEntriesForLorebook(lorebook.getId());
    }

    @Override
    @CommonTxReadOnly
    public List<EntryFulltext> findFulltextsContaining(Lorebook lorebook, String literal) throws Exception {
        if (lorebook == null || lorebook.getId() == null) {
            return Collections.emptyList();
        }
        return getRepository().findFulltextsContaining(lorebook.getId(), literal).stream()
                .map(row -> new EntryFulltext((Long) row[0], (String) row[1]))
                .toList();
    }
}
