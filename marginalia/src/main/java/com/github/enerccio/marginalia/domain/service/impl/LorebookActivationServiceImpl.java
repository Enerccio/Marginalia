package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookActivationService;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LorebookEntryVerdict;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LorebookActivationServiceImpl implements LorebookActivationService {

    private static final Logger log = LoggerFactory.getLogger(LorebookActivationServiceImpl.class);

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TemplateService templateService;

    @Override
    public List<Lorebook> collectLorebooks(Lorebook root) throws Exception {
        Set<Long> processedLorebooks = new HashSet<>();
        List<Lorebook> found = new ArrayList<>();
        Queue<Lorebook> toProcess = new LinkedList<>();
        toProcess.add(root);
        while (!toProcess.isEmpty()) {
            Lorebook l = toProcess.remove();
            if (l.isDeleted() || !l.isEnabled()) {
                log.debug("Lorebook {} skipped because it's deleted or not enabled", l.getName());
                continue;
            }
            if (!processedLorebooks.contains(l.getId())) {
                processedLorebooks.add(l.getId());
                found.add(l);
                toProcess.addAll(lorebookService.getSubbooks(l));
            }
        }

        List<Lorebook> filled = new ArrayList<>();
        for (Lorebook book : found) {
            book = lorebookService.fillEntries(book);
            lorebookService.evict(book);
            for (LorebookEntry entry : book.getCachedEntries()) {
                lorebookEntryService.evict(entry);
            }
            filled.add(book);
        }
        return filled;
    }

    @Override
    public List<LorebookEntry> orderedEntries(List<Lorebook> lorebooks) {
        Map<Integer, List<LorebookEntry>> byOrder = new TreeMap<>();
        for (Lorebook l : lorebooks) {
            for (LorebookEntry lorebookEntry : l.getCachedEntries()) {
                byOrder.computeIfAbsent(lorebookEntry.getOrder(), _ -> new ArrayList<>()).add(lorebookEntry);
            }
        }
        List<LorebookEntry> entries = new ArrayList<>();
        for (List<LorebookEntry> list : byOrder.values()) {
            list.sort(Comparator.comparing(LorebookEntry::getCreation, Comparator.nullsLast(Comparator.naturalOrder())));
            entries.addAll(list);
        }
        return entries;
    }

    @Override
    public Verdict evaluate(LorebookEntry entry, List<String> manuscriptTags, String text) {
        if (!entry.isEnabled()) {
            return new Verdict(Reason.DISABLED, null);
        }
        String negative = firstShared(entry.getCachedNegativeTags(), manuscriptTags);
        if (negative != null) {
            return new Verdict(Reason.NEGATIVE_TAG, negative);
        }
        if (!entry.getCachedTags().isEmpty() && firstShared(entry.getCachedTags(), manuscriptTags) == null) {
            return new Verdict(Reason.MISSING_TAG, String.join(", ", entry.getCachedTags()));
        }
        if (StringUtils.isBlank(entry.getFiltering())) {
            return new Verdict(Reason.CONSTANT, null);
        }
        return filterMatches(entry, text) ? new Verdict(Reason.FILTER_MATCHED, entry.getFiltering())
                : new Verdict(Reason.FILTER_NOT_MATCHED, entry.getFiltering());
    }

    @Override
    public int scanDepth(Lorebook root) {
        return root == null ? 0 : Math.max(0, Math.min(MAX_SCAN_DEPTH, root.getScanDepth()));
    }

    @Override
    public List<LorebookEntry> scannable(List<LorebookEntry> used) {
        return used.stream().filter(entry -> !entry.isPreventRecursion() && StringUtils.isNotBlank(entry.getPayload())).toList();
    }

    @Override
    public Verdict evaluateInContent(LorebookEntry entry, List<String> manuscriptTags, List<LorebookEntry> sources) {
        Verdict all = evaluate(entry, manuscriptTags,
                sources.stream().map(LorebookEntry::getPayload).collect(java.util.stream.Collectors.joining("\n\n")));
        if (all.reason() != Reason.FILTER_MATCHED) {
            return all;
        }
        for (LorebookEntry source : sources) {
            if (evaluate(entry, manuscriptTags, source.getPayload()).activated()) {
                return new Verdict(Reason.LORE_MATCHED, entry.getFiltering() + " <- " + source.getName());
            }
        }
        return new Verdict(Reason.LORE_MATCHED, entry.getFiltering());
    }

    @Override
    public List<LorebookEntryVerdict> evaluateAll(Lorebook root, List<Lorebook> lorebooks, List<String> manuscriptTags, String text) {
        List<LorebookEntry> entries = orderedEntries(lorebooks);
        LorebookEntryVerdict[] verdicts = new LorebookEntryVerdict[entries.size()];
        List<LorebookEntry> pass = new ArrayList<>();
        for (int i = 0; i < verdicts.length; i++) {
            verdicts[i] = new LorebookEntryVerdict(entries.get(i), evaluate(entries.get(i), manuscriptTags, text), 0);
            if (verdicts[i].verdict().activated()) {
                pass.add(entries.get(i));
            }
        }
        int depth = scanDepth(root);
        for (int d = 1; d <= depth; d++) {
            List<LorebookEntry> sources = scannable(pass);
            if (sources.isEmpty()) {
                break;
            }
            pass = new ArrayList<>();
            for (int i = 0; i < verdicts.length; i++) {
                if (verdicts[i].verdict().reason() != Reason.FILTER_NOT_MATCHED) {
                    continue;
                }
                Verdict verdict = evaluateInContent(entries.get(i), manuscriptTags, sources);
                if (verdict.activated()) {
                    verdicts[i] = new LorebookEntryVerdict(entries.get(i), verdict, d);
                    pass.add(entries.get(i));
                }
            }
        }
        return Arrays.asList(verdicts);
    }

    @Override
    public String renderPayload(LorebookEntry entry, LorebookTemplateData data) {
        String payload = entry.getPayload();
        if (StringUtils.isBlank(payload)) {
            return payload;
        }
        try {
            return templateService.processTemplate(payload, "lorebookEntry", data);
        } catch (Exception e) {
            // a broken entry must not break the generation, use it as it is
            log.warn("Failed to process template of lorebook entry '{}': {}", entry.getName(), e.getMessage());
            log.debug(e.getMessage(), e);
            return payload;
        }
    }

    private boolean filterMatches(LorebookEntry entry, String textToSearch) {
        String content = Objects.toString(textToSearch, "");
        if (entry.getFilteringMode() == FilteringMode.TEXT) {
            return Strings.CI.contains(content, entry.getFiltering());
        }
        if (entry.getFilteringMode() == FilteringMode.KEYWORDS_OR || entry.getFilteringMode() == FilteringMode.KEYWORDS_AND) {
            List<String> keywords = Arrays.stream(Objects.toString(entry.getFiltering(), "").split(","))
                    .map(String::strip).filter(StringUtils::isNotEmpty).toList();
            if (keywords.isEmpty()) {
                return false;
            }
            return entry.getFilteringMode() == FilteringMode.KEYWORDS_OR
                    ? keywords.stream().anyMatch(keyword -> Strings.CI.contains(content, keyword))
                    : keywords.stream().allMatch(keyword -> Strings.CI.contains(content, keyword));
        }
        try {
            Pattern pattern = Pattern.compile(
                    entry.getFiltering(),
                    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.DOTALL
            );
            return pattern.matcher(content).find();
        } catch (PatternSyntaxException e) {
            log.warn("Invalid regex pattern '{}' in lorebook entry '{}': {}",
                    entry.getFiltering(), entry.getName(), e.getMessage());
            return false;
        }
    }

    /**
     * The first of the entry's tags that the book has too, null when there is none.
     */
    private static String firstShared(List<String> entryTags, List<String> manuscriptTags) {
        Set<String> book = new HashSet<>(manuscriptTags);
        for (String tag : entryTags) {
            if (book.contains(tag)) {
                return tag;
            }
        }
        return null;
    }
}
