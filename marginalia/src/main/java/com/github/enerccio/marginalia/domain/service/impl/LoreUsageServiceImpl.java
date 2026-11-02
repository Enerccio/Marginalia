package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.components.LoreUsage;
import com.github.enerccio.marginalia.domain.components.LoreUsageReport;
import com.github.enerccio.marginalia.domain.components.LoreUsageReport.Scope;
import com.github.enerccio.marginalia.domain.components.LoreUsageReport.Status;
import com.github.enerccio.marginalia.domain.components.PartLoreUsage;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class LoreUsageServiceImpl implements LoreUsageService {

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private MessagePromptService messagePromptService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private LorebookActivationService activationService;

    /**
     * What is known of an entry.
     */
    private record Info(String name, String lorebook, boolean enabled, boolean inLorebook) {
    }

    /**
     * The entries of the lorebooks a book uses now, by uuid, and the way to find the ones that are not among them.
     */
    private final class Catalog {
        final Map<String, Info> entries = new LinkedHashMap<>();
        final boolean hasLorebook;

        Catalog(Manuscript manuscript) throws Exception {
            Lorebook root = manuscript.getLorebook() == null ? null : lorebookService.find(manuscript.getLorebook());
            hasLorebook = root != null && !root.isDeleted();
            if (!hasLorebook) {
                return;
            }
            for (Lorebook lorebook : activationService.collectLorebooks(root)) {
                for (LorebookEntry entry : lorebookEntryService.getEntriesForLorebook(lorebook)) {
                    entries.put(entry.getUuid(), new Info(entry.getName(), lorebook.getName(), entry.isEnabled(), true));
                }
            }
        }

        Info lookup(String uuid) throws Exception {
            Info known = entries.get(uuid);
            if (known != null) {
                return known;
            }
            LorebookEntry entry = lorebookEntryService.findForUser(uuid);
            if (entry == null) {
                return new Info(null, null, false, false);
            }
            Lorebook lorebook = entry.getLorebook();
            return new Info(entry.getName(), lorebook == null ? null : lorebook.getName(), entry.isEnabled(), false);
        }
    }

    @Override
    @CommonTxReadOnly
    public PartLoreUsage forPart(ChatMessage part) throws Exception {
        ChatMessage own = part == null || part.getUuid() == null ? null : chatMessageService.findForUser(part.getUuid());
        LoreUsage usage = own == null ? null : messagePromptService.getLoreUsage(own);
        if (usage == null) {
            return new PartLoreUsage(false, List.of());
        }
        Catalog catalog = new Catalog(own.getParentScript());
        List<PartLoreUsage.Used> used = new ArrayList<>();
        for (LoreUsage.Used entry : usage.getEntries()) {
            Info info = catalog.lookup(entry.entry());
            used.add(new PartLoreUsage.Used(entry.entry(), info.name(), info.lorebook(), entry.tokens(), entry.depth()));
        }
        return new PartLoreUsage(true, used);
    }

    @Override
    @CommonTxReadOnly
    public LoreUsageReport forManuscript(Manuscript manuscript, Scope scope, Integer from, Integer to) throws Exception {
        Manuscript own = manuscript == null || manuscript.getUuid() == null ? null : manuscriptService.findForUser(manuscript.getUuid());
        if (own == null) {
            throw new IllegalArgumentException("The book is not yours");
        }
        int first = from == null || from < 1 ? 1 : from;
        int last = to == null || to < 1 ? 0 : to;

        // part number by id: the first part of a branch is 1, the same for every branch of the tree
        Map<Long, Integer> numbers = scope == Scope.ALL_BRANCHES ? depthOfAll(own) : numbersOfBranch(own);
        List<Long> ids = new ArrayList<>();
        for (Map.Entry<Long, Integer> part : numbers.entrySet()) {
            if (part.getValue() >= first && (last == 0 || part.getValue() <= last)) {
                ids.add(part.getKey());
            }
        }
        Map<Long, LoreUsage> usages = messagePromptService.findLoreUsage(ids);

        Map<String, int[]> counts = new HashMap<>();
        Map<String, Long> tokens = new HashMap<>();
        for (Map.Entry<Long, LoreUsage> recorded : usages.entrySet()) {
            int number = numbers.get(recorded.getKey());
            Set<String> seen = new HashSet<>();
            for (LoreUsage.Used used : recorded.getValue().getEntries()) {
                if (!seen.add(used.entry())) {
                    continue;
                }
                // used, first part, last part, deepest pass
                int[] count = counts.computeIfAbsent(used.entry(), k -> new int[]{0, Integer.MAX_VALUE, 0, 0});
                count[0]++;
                count[1] = Math.min(count[1], number);
                count[2] = Math.max(count[2], number);
                count[3] = Math.max(count[3], used.depth());
                tokens.merge(used.entry(), used.tokens(), Long::sum);
            }
        }

        Catalog catalog = new Catalog(own);
        int recordedParts = usages.size();
        List<LoreUsageReport.Row> rows = new ArrayList<>();
        Set<String> listed = new LinkedHashSet<>(catalog.entries.keySet());
        listed.addAll(counts.keySet());
        for (String uuid : listed) {
            Info info = catalog.lookup(uuid);
            int[] count = counts.get(uuid);
            int used = count == null ? 0 : count[0];
            Status status = used == 0 ? Status.NEVER : used == recordedParts ? Status.ALWAYS : Status.SOMETIMES;
            rows.add(new LoreUsageReport.Row(uuid, info.name(), info.lorebook(), info.inLorebook(), info.enabled(), used,
                    count == null ? 0 : count[1], count == null ? 0 : count[2], tokens.getOrDefault(uuid, 0L),
                    count == null ? 0 : count[3], status));
        }
        rows.sort(Comparator.comparingInt(LoreUsageReport.Row::used).reversed()
                .thenComparing(Comparator.comparingLong(LoreUsageReport.Row::tokens).reversed())
                .thenComparing(row -> row.name() == null ? "" : row.name(), String.CASE_INSENSITIVE_ORDER));
        return new LoreUsageReport(scope, first, last, ids.size(), recordedParts, catalog.hasLorebook, rows);
    }

    private Map<Long, Integer> numbersOfBranch(Manuscript manuscript) throws Exception {
        Map<Long, Integer> numbers = new LinkedHashMap<>();
        int number = 1;
        for (ChatMessage part : chatMessageService.getBranchFromLeaf(manuscript.getActiveLeaf())) {
            numbers.put(part.getId(), number++);
        }
        return numbers;
    }

    private Map<Long, Integer> depthOfAll(Manuscript manuscript) throws Exception {
        List<ChatMessage> all = chatMessageService.getAllMessages(manuscript);
        Map<Long, Long> parentOf = new HashMap<>();
        for (ChatMessage part : all) {
            parentOf.put(part.getId(), part.getParent() == null ? null : part.getParent().getId());
        }
        Map<Long, Integer> depths = new LinkedHashMap<>();
        for (ChatMessage part : all) {
            // a part whose parent is not in the list (deleted) counts as a root
            int depth = 1;
            Set<Long> guard = new HashSet<>();
            for (Long parent = parentOf.get(part.getId()); parent != null && parentOf.containsKey(parent) && guard.add(parent);
                 parent = parentOf.get(parent)) {
                depth++;
            }
            depths.put(part.getId(), depth);
        }
        return depths;
    }
}
