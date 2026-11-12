package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.TokenizerService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

/**
 * The token estimates of a lorebook as the lorebook view and the lorebook editor show them, so that both give the same
 * numbers: of an entry (its content, counted approximately and kept until it changes), of the enabled entries of a lorebook,
 * and of the enabled entries of the lorebook with all its subbooks.
 */
@Configurable
public class LorebookTokens {

    @Autowired
    private Localization loc;

    @Autowired
    private TokenizerService tokenizerService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private LorebookService lorebookService;

    private final Map<Long, Long> counted = new HashMap<>();

    /**
     * The tokens of the content of the entry, counted when it was not counted yet.
     */
    public long of(LorebookEntry entry) throws Exception {
        Long cached = entry.getId() != null ? counted.get(entry.getId()) : null;
        return cached != null ? cached : recount(entry);
    }

    /**
     * Counts the content of the entry again (it changed).
     */
    public long recount(LorebookEntry entry) throws Exception {
        long tokens = tokenizerService.countTokensApprox(StringUtils.defaultString(entry.getPayload()));
        if (entry.getId() != null) {
            counted.put(entry.getId(), tokens);
        }
        return tokens;
    }

    /**
     * Forgets the counts, the entries are loaded again.
     */
    public void clear() {
        counted.clear();
    }

    /**
     * The tokens of the enabled ones of the entries.
     */
    public long ofEnabled(Collection<LorebookEntry> entries) throws Exception {
        long total = 0;
        for (LorebookEntry entry : entries) {
            if (entry.isEnabled()) {
                total += of(entry);
            }
        }
        return total;
    }

    /**
     * The tokens of the enabled entries of the lorebook and of its subbooks, however deep. As the generation does, a disabled
     * subbook is left out with its own subbooks, and a lorebook that is a subbook twice counts once; the lorebook itself counts
     * whether it is enabled or not, as its own count does.
     */
    public long withSubbooks(Lorebook root) throws Exception {
        long total = 0;
        Set<Long> seen = new HashSet<>();
        Deque<Lorebook> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Lorebook book = queue.remove();
            if (book.getId() == null || !seen.add(book.getId())) {
                continue;
            }
            if (book != root && !book.isEnabled()) {
                continue;
            }
            for (LorebookEntry entry : lorebookEntryService.getEntriesForLorebook(book.getId())) {
                if (entry.isEnabled()) {
                    total += book == root ? of(entry)
                            : tokenizerService.countTokensApprox(StringUtils.defaultString(entry.getPayload()));
                }
            }
            queue.addAll(lorebookService.getSubbooks(book));
        }
        return total;
    }

    /**
     * {@code ~123 tokens}
     */
    public String format(long tokens) {
        return "~" + tokens + " " + loc.getValue(L.LABEL_TOKENS);
    }

    /**
     * The total with the subbooks, in the words of the lorebook view.
     */
    public String formatWithSubbooks(long tokens) {
        return String.format(loc.getValue(L.LABEL_TOKENS_WITH_SUBBOOKS), tokens);
    }
}
