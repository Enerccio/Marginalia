package com.github.enerccio.marginalia.domain.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.components.LoreConditions;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LorebookEntryVerdict;
import com.github.enerccio.marginalia.domain.templates.*;
import com.github.enerccio.marginalia.domain.templates.macros.MacroHelpers;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
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
    private ChatMessageService chatMessageService;

    @Autowired
    private PartTextRegistry partTextRegistry;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private TagRelationService tagRelationService;

    /**
     * Whether a condition is a valid template without names that do not exist, by its text: the same conditions are
     * checked for every turn.
     */
    private final Cache<String, TemplateService.ValidationResult> conditionChecks = Caffeine.newBuilder()
            .maximumSize(1000)
            .build();

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
        return evaluate(entry, manuscriptTags, text, List.of());
    }

    @Override
    public Verdict evaluate(LorebookEntry entry, List<String> manuscriptTags, String instructions, List<String> parts) {
        return evaluate(entry, manuscriptTags, instructions, parts, null);
    }

    @Override
    public Verdict evaluate(LorebookEntry entry, List<String> manuscriptTags, String instructions, List<String> parts, Integer position) {
        return evaluate(entry, manuscriptTags, instructions, parts, position, null);
    }

    @Override
    public Verdict evaluate(LorebookEntry entry, List<String> manuscriptTags, String instructions, List<String> parts, Integer position, LoreConditions conditions) {
        return evaluate(entry, manuscriptTags, instructions, StoryText.ofParts(parts), position, conditions);
    }

    @Override
    public Verdict evaluate(LorebookEntry entry, List<String> manuscriptTags, String instructions, StoryText story, Integer position, LoreConditions conditions) {
        return evaluate(entry, manuscriptTags, instructions, story, position, conditions, false);
    }

    /**
     * @param viaContent the text is the content of an entry that was used, not the turn: an entry that is used only that way
     *                   (recursion only) may match it
     */
    private Verdict evaluate(LorebookEntry entry, List<String> manuscriptTags, String instructions, StoryText story, Integer position,
                             LoreConditions conditions, boolean viaContent) {
        if (!entry.isEnabled()) {
            return new Verdict(Reason.DISABLED, null);
        }
        if (position != null && !entry.inMessageRange(position)) {
            return new Verdict(Reason.OUT_OF_RANGE, rangeOf(entry));
        }
        String negative = firstShared(entry.getCachedNegativeTags(), manuscriptTags);
        if (negative != null) {
            return new Verdict(Reason.NEGATIVE_TAG, negative);
        }
        if (!entry.getCachedTags().isEmpty() && firstShared(entry.getCachedTags(), manuscriptTags) == null) {
            return new Verdict(Reason.MISSING_TAG, String.join(", ", entry.getCachedTags()));
        }
        Verdict condition = checkCondition(entry, conditions);
        if (condition != null) {
            return condition;
        }
        if (entry.isRecursionOnly() && (!viaContent || StringUtils.isBlank(entry.getFiltering()))) {
            // only the content of other entries can use it (an entry without a filter has nothing to be found by)
            return new Verdict(Reason.RECURSION_ONLY, entry.getFiltering());
        }
        if (StringUtils.isBlank(entry.getFiltering())) {
            return new Verdict(Reason.CONSTANT, null);
        }
        return filterMatches(entry, contextOf(entry, instructions, story)) ? new Verdict(Reason.FILTER_MATCHED, entry.getFiltering())
                : new Verdict(Reason.FILTER_NOT_MATCHED, entry.getFiltering());
    }

    /**
     * The verdict that stops the entry because of its condition, null when it has none or it is true. Decided once per
     * entry for the {@code conditions}.
     */
    private Verdict checkCondition(LorebookEntry entry, LoreConditions conditions) {
        if (!entry.hasActivationCondition()) {
            return null;
        }
        LoreConditions scope = conditions == null ? new LoreConditions(null) : conditions;
        Verdict kept = scope.decision(entry);
        if (kept == null) {
            kept = decideCondition(entry, scope);
            scope.keep(entry, kept);
        }
        return kept == CONDITION_TRUE ? null : kept;
    }

    private static final Verdict CONDITION_TRUE = new Verdict(Reason.CONSTANT, null);

    private Verdict decideCondition(LorebookEntry entry, LoreConditions scope) {
        String condition = entry.getActivationCondition();
        if (scope.exhausted()) {
            log.warn("The time for the conditions of the lorebook entries is spent, the condition of '{}' counts as false", entry.getName());
            return new Verdict(Reason.CONDITION_ERROR, "timeout");
        }
        long started = System.nanoTime();
        try {
            TemplateService.ValidationResult check = conditionChecks.get(condition, this::validateCondition);
            if (!check.isValid()) {
                return new Verdict(Reason.CONDITION_ERROR, check.errorMessage());
            }
            if (check.hasWarnings()) {
                return new Verdict(Reason.CONDITION_ERROR, "unknown: " + String.join(", ", check.unknownNames()));
            }
            LorebookTemplateData data = scope.forCondition();
            String result = StringUtils.trimToEmpty(templateService.processTemplate(condition, "lorebookCondition", data));
            if (MacroHelpers.MISSING_VALUE.equals(result)) {
                // a name or a command that does not exist renders as this
                return new Verdict(Reason.CONDITION_ERROR, result);
            }
            return TemplateData.isTruthy(result) ? CONDITION_TRUE : new Verdict(Reason.CONDITION_FALSE, condition);
        } catch (Exception e) {
            log.warn("The condition of the lorebook entry '{}' failed, it counts as false: {}", entry.getName(), e.getMessage());
            log.debug(e.getMessage(), e);
            return new Verdict(Reason.CONDITION_ERROR, StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName()));
        } finally {
            scope.spend(System.nanoTime() - started);
        }
    }

    private TemplateService.ValidationResult validateCondition(String condition) {
        try {
            return templateService.isValidTemplate(condition, "lorebookCondition", LorebookTemplateData.class);
        } catch (Exception e) {
            return new TemplateService.ValidationResult(false, StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName()));
        }
    }

    @Override
    public boolean conditionsNeeded(List<LorebookEntry> entries) {
        return entries.stream().anyMatch(entry -> entry.isEnabled() && entry.hasActivationCondition());
    }

    private static String rangeOf(LorebookEntry entry) {
        return Objects.toString(entry.getFromMessage(), "") + ".." + Objects.toString(entry.getToMessage(), "");
    }

    @Override
    public int turnPosition(ChatMessage leaf, boolean excludeLeaf) throws Exception {
        if (leaf == null) {
            return 1;
        }
        int size = chatMessageService.getBranchFromLeaf(leaf).size();
        return excludeLeaf ? Math.max(1, size) : size + 1;
    }

    @Override
    public boolean positionNeeded(List<LorebookEntry> entries) {
        return entries.stream().anyMatch(entry -> entry.isEnabled() && entry.hasMessageRange());
    }

    @Override
    public int partsNeeded(List<LorebookEntry> entries) {
        return entries.stream().filter(LorebookEntry::isEnabled).mapToInt(LorebookEntry::partsInContext).max().orElse(0);
    }

    /**
     * What the filter of the entry is looked for in: the instructions, with the summaries of the story (when the entry asks for
     * them) and the newest parts of the story (as many as the entry asks for) in front of them.
     */
    private static String contextOf(LorebookEntry entry, String instructions, StoryText story) {
        List<String> parts = story.parts();
        int wanted = Math.min(entry.partsInContext(), parts.size());
        boolean summaries = entry.isActivationSummaries() && !story.summaries().isBlank();
        if (wanted == 0 && !summaries) {
            return instructions;
        }
        StringBuilder text = new StringBuilder();
        if (summaries) {
            text.append(story.summaries()).append("\n\n");
        }
        for (String part : parts.subList(parts.size() - wanted, parts.size())) {
            text.append(part).append("\n\n");
        }
        return text.append(Objects.toString(instructions, "")).toString();
    }

    @Override
    public StoryText storyText(List<LorebookEntry> entries, ChatMessage leaf, boolean excludeLeaf) throws Exception {
        List<String> parts = recentParts(leaf, partsNeeded(entries), excludeLeaf);
        if (!summariesNeeded(entries) || leaf == null) {
            return StoryText.ofParts(parts);
        }
        List<ChatMessage> branch = new ArrayList<>(chatMessageService.getBranchFromLeaf(leaf));
        if (excludeLeaf && !branch.isEmpty()) {
            branch.removeLast();
        }
        List<String> texts = new ArrayList<>();
        for (SummaryService.SummaryBlock block : summaryService.collectBlocks(new ArrayList<>(branch.reversed()))) {
            // a summary that no longer matches its story is not read by the prompt either
            if (block.isValid() && block.summary() != null && StringUtils.isNotBlank(block.summary().getSummary())) {
                texts.add(block.summary().getSummary());
            }
        }
        return new StoryText(parts, String.join("\n\n", texts.reversed()));
    }

    @Override
    public boolean summariesNeeded(List<LorebookEntry> entries) {
        return entries.stream().anyMatch(entry -> entry.isEnabled() && entry.isActivationSummaries());
    }

    @Override
    public List<String> recentParts(ChatMessage leaf, int count, boolean excludeLeaf) throws Exception {
        if (count <= 0 || leaf == null) {
            return List.of();
        }
        List<ChatMessage> branch = new ArrayList<>(chatMessageService.getBranchFromLeaf(leaf));
        if (excludeLeaf && !branch.isEmpty()) {
            branch.removeLast();
        }
        List<ChatMessage> picked = new ArrayList<>();
        for (ChatMessage message : branch.reversed()) {
            if (StringUtils.isNotBlank(message.getResponse())) {
                picked.add(message);
                if (picked.size() == count) {
                    break;
                }
            }
        }
        // the scan reads what the model reads (an extension may keep the story in another form)
        return partTextRegistry.forModel(picked).reversed();
    }

    @Override
    public int scanDepth(Lorebook root) {
        return root == null ? 0 : Math.max(0, Math.min(MAX_SCAN_DEPTH, root.getScanDepth()));
    }

    @Override
    public int scanDepth(Lorebook root, Lorebook book) {
        if (book == null || root == null || Objects.equals(book.getId(), root.getId()) || book.getOwnScanDepth() == null) {
            return scanDepth(root);
        }
        return Math.max(0, Math.min(MAX_SCAN_DEPTH, book.getOwnScanDepth()));
    }

    @Override
    public Map<Long, Integer> scanDepths(Lorebook root, List<Lorebook> lorebooks) {
        Map<Long, Integer> depths = new HashMap<>();
        for (Lorebook book : lorebooks) {
            depths.put(book.getId(), scanDepth(root, book));
        }
        return depths;
    }

    @Override
    public List<LorebookEntry> scannable(List<LorebookEntry> used) {
        return used.stream().filter(entry -> !entry.isPreventRecursion() && StringUtils.isNotBlank(entry.getPayload())).toList();
    }

    @Override
    public Verdict evaluateInContent(LorebookEntry entry, List<String> manuscriptTags, List<LorebookEntry> sources) {
        return evaluateInContent(entry, manuscriptTags, sources, null);
    }

    @Override
    public Verdict evaluateInContent(LorebookEntry entry, List<String> manuscriptTags, List<LorebookEntry> sources, LoreConditions conditions) {
        Verdict all = evaluate(entry, manuscriptTags,
                sources.stream().map(LorebookEntry::getPayload).collect(java.util.stream.Collectors.joining("\n\n")),
                StoryText.NONE, null, conditions, true);
        if (all.reason() != Reason.FILTER_MATCHED) {
            return all;
        }
        for (LorebookEntry source : sources) {
            if (evaluate(entry, manuscriptTags, source.getPayload(), StoryText.NONE, null, conditions, true).activated()) {
                return new Verdict(Reason.LORE_MATCHED, entry.getFiltering() + " <- " + source.getName());
            }
        }
        return new Verdict(Reason.LORE_MATCHED, entry.getFiltering());
    }

    @Override
    public List<LorebookEntryVerdict> evaluateAll(Lorebook root, List<Lorebook> lorebooks, List<String> manuscriptTags, String text) {
        return evaluateAll(root, lorebooks, manuscriptTags, text, List.of());
    }

    @Override
    public List<LorebookEntryVerdict> evaluateAll(Lorebook root, List<Lorebook> lorebooks, List<String> manuscriptTags, String text, List<String> parts) {
        return evaluateAll(root, lorebooks, manuscriptTags, text, parts, null);
    }

    @Override
    public List<LorebookEntryVerdict> evaluateAll(Lorebook root, List<Lorebook> lorebooks, List<String> manuscriptTags, String text, List<String> parts, Integer position) {
        return evaluateAll(root, lorebooks, manuscriptTags, text, parts, position, null);
    }

    @Override
    public List<LorebookEntryVerdict> evaluateAll(Lorebook root, List<Lorebook> lorebooks, List<String> manuscriptTags, String text, List<String> parts, Integer position, LoreConditions conditions) {
        return evaluateAll(root, lorebooks, manuscriptTags, text, StoryText.ofParts(parts), position, conditions);
    }

    @Override
    public List<LorebookEntryVerdict> evaluateAll(Lorebook root, List<Lorebook> lorebooks, List<String> manuscriptTags, String text, StoryText story, Integer position, LoreConditions conditions) {
        List<LorebookEntry> entries = orderedEntries(lorebooks);
        // one scope for all the entries: the condition of an entry is decided once, whatever the scan depth does
        LoreConditions scope = conditions != null ? conditions : conditionsNeeded(entries) ? new LoreConditions(null) : null;
        LorebookEntryVerdict[] verdicts = new LorebookEntryVerdict[entries.size()];
        List<LorebookEntry> pass = new ArrayList<>();
        for (int i = 0; i < verdicts.length; i++) {
            verdicts[i] = new LorebookEntryVerdict(entries.get(i), evaluate(entries.get(i), manuscriptTags, text, story, position, scope), 0);
            if (verdicts[i].verdict().activated()) {
                pass.add(entries.get(i));
            }
        }
        // every lorebook of the tree has its own depth: its entries are found by content only in the passes up to it
        Map<Long, Integer> depths = scanDepths(root, lorebooks);
        int depth = deepestScan(depths);
        for (int d = 1; d <= depth; d++) {
            List<LorebookEntry> sources = scannable(pass);
            if (sources.isEmpty()) {
                break;
            }
            pass = new ArrayList<>();
            for (int i = 0; i < verdicts.length; i++) {
                if (!verdicts[i].verdict().reason().isScannable() || d > scanDepthOf(depths, entries.get(i))) {
                    continue;
                }
                Verdict verdict = evaluateInContent(entries.get(i), manuscriptTags, sources, scope);
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

    @Override
    public String currentBackgroundLore(Manuscript manuscript, ChatMessage part) throws Exception {
        Lorebook root = manuscript.getLorebook() == null ? null : lorebookService.find(manuscript.getLorebook());
        if (root == null) {
            return null;
        }
        TurnInput input = new TurnInput(part.getSceneSetting(), part.getPovCharacter(), part.getPresentCharacters(), part.getInstructions());
        TemplateContext context = new TemplateContext();
        context.setPovCharacter(input.povCharacter());
        context.setPresentCharacters(input.presentCharacters());
        context.setSceneSetting(input.sceneSetting());
        context.setInstructions(input.instructions());
        context.setNarrativePov(manuscriptService.getPov(manuscript));
        context.setNarrativeTense(manuscriptService.getTense(manuscript));
        context.setStyle(manuscriptService.getStyle(manuscript));
        context.setManuscriptName(manuscript.getName());
        context.setManuscriptDescription(manuscript.getDescription());
        context.setPickSeed(String.valueOf(manuscript.getId()));
        context.setGenerationType("normal");
        context.getVariables().loadFrom(TemplateVariables.Scope.GLOBAL, manuscript.getAttributes());
        context.getVariables().loadFrom(TemplateVariables.Scope.LOCAL, part.getAttributes());

        // what the filters are looked for in: the user prompt of the book for the turn
        UserPromptData prompt = new UserPromptData();
        prompt.setInstructions(input.instructions());
        prompt.setPovCharacter(input.povCharacter());
        prompt.setPresentCharacters(input.presentCharacters());
        prompt.setSceneSetting(input.sceneSetting());
        prompt.setTemplateContext(context.fork());
        String matched;
        try {
            matched = StringUtils.defaultString(templateService.processTemplate(manuscriptService.getUserPrompt(manuscript), "userPrompt", prompt));
        } catch (Exception e) {
            log.debug("The user prompt of the book did not render: {}", e.getMessage());
            matched = java.util.stream.Stream.of(input.instructions(), input.sceneSetting(), input.povCharacter(), input.presentCharacters())
                    .filter(StringUtils::isNotBlank).collect(java.util.stream.Collectors.joining("\n"));
        }

        LorebookTemplateData data = new LorebookTemplateData();
        data.setPovCharacter(input.povCharacter());
        data.setSceneSetting(input.sceneSetting());
        data.setPresentCharacters(input.presentCharacters());
        data.setInstructions(input.instructions());
        data.setNarrativePov(context.getNarrativePov());
        data.setNarrativeTense(context.getNarrativeTense());
        data.setStyle(context.getStyle());
        data.setTemplateContext(context);

        List<String> tags = tagRelationService.getTagsForObject(manuscript).stream().map(Tag::getValue).toList();
        List<Lorebook> lorebooks = collectLorebooks(root);
        List<LorebookEntry> entries = orderedEntries(lorebooks);
        // the story in front of the part, the part itself is the turn
        StoryText story = storyText(entries, part, true);
        Integer position = positionNeeded(entries) ? turnPosition(part, true) : null;
        StringBuilder block = new StringBuilder();
        for (LorebookEntryVerdict decided : evaluateAll(root, lorebooks, tags, matched, story, position, new LoreConditions(data))) {
            if (!decided.verdict().activated() || decided.entry().getInsertionMode() != InsertionMode.IN_LORE_BLOCK) {
                continue;
            }
            String payload = renderPayload(decided.entry(), data);
            if (StringUtils.isNotBlank(payload)) {
                block.append(payload).append("\n\n");
            }
        }
        return block.toString().trim();
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
