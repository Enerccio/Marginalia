package com.github.enerccio.marginalia.extensions.storytracker.llm;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LorebookEntryVerdict;
import com.github.enerccio.marginalia.domain.templates.LorebookTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateContext;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.storytracker.process.Part;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

/**
 * The world text of a request that reads the story: the lorebook entries of the book, as a generation after the last part of the run
 * would see them.
 */
@Configurable
public class StoryLore {

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private PartTextRegistry partTexts;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private LorebookActivationService activationService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private SummaryService summaryService;

    /**
     * The lore for the request. {@link LoreMode#WHOLE} that does not fit becomes {@link LoreMode#RESOLVE}, and entries that do not fit
     * the budget are left out (the note says so).
     *
     * @param run    the parts of the run, in order
     * @param budget the tokens the lore may take
     * @param note   what the user should know is appended here
     */
    public String lore(Manuscript manuscript, LoreMode mode, List<Part> run, long budget, InferenceService service, StringBuilder note)
            throws Exception {
        if (mode == LoreMode.NONE || budget <= 0 || run.isEmpty()) {
            return "";
        }
        Manuscript current = manuscriptService.find(manuscript);
        Manuscript book = current != null ? current : manuscript;
        Lorebook root = book.getLorebook() != null ? lorebookService.find(book.getLorebook()) : null;
        if (root == null) {
            return "";
        }
        List<ChatMessage> branch = new ArrayList<>(chatMessageService.getBranchFromLeaf(book.getActiveLeaf()));
        int last = -1;
        for (int i = 0; i < branch.size(); i++) {
            if (branch.get(i).getUuid().equals(run.getLast().uuid())) {
                last = i;
            }
        }
        List<ChatMessage> upToRun = last >= 0 ? branch.subList(0, last + 1) : branch;
        LorebookTemplateData data = templateData(book, upToRun);

        if (mode == LoreMode.WHOLE) {
            String whole = wholeLore(root, data);
            if (service.countTokensApprox(whole) <= budget) {
                return whole;
            }
            note.append("The whole lorebook does not fit the context, only the entries of these parts are sent. ");
        }
        return resolvedLore(book, root, upToRun, data, budget, service, note);
    }

    private String wholeLore(Lorebook root, LorebookTemplateData data) throws Exception {
        List<LorebookEntry> entries = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        Queue<Lorebook> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Lorebook lorebook = queue.remove();
            if (lorebook.isDeleted() || !seen.add(lorebook.getId())) {
                continue;
            }
            lorebookEntryService.getEntriesForLorebook(lorebook).stream().filter(e -> !e.isDeleted()).forEach(entries::add);
            queue.addAll(lorebookService.getSubbooks(lorebook));
        }
        entries.sort(Comparator.comparingInt(LorebookEntry::getOrder));
        StringBuilder lore = new StringBuilder();
        for (LorebookEntry entry : entries) {
            String payload = activationService.renderPayload(entry, data);
            if (StringUtils.isNotBlank(payload)) {
                lore.append('[').append(entry.getName()).append("]:\n").append(payload.strip()).append("\n\n");
            }
        }
        return lore.toString().strip();
    }

    private String resolvedLore(Manuscript book, Lorebook root, List<ChatMessage> upToRun, LorebookTemplateData data, long budget,
                                InferenceService service, StringBuilder note) throws Exception {
        List<Lorebook> books = activationService.collectLorebooks(root);
        List<String> tags = tagRelationService.getTagsForObject(book).stream().map(Tag::getValue).toList();
        StringBuilder matched = new StringBuilder();
        // the parts of the run are the ones after the cursor: the last ones of upToRun; matching on all of the run is enough
        for (ChatMessage message : upToRun.subList(Math.max(0, upToRun.size() - 10), upToRun.size())) {
            for (String piece : new String[]{partTexts.forModel(message), message.getInstructions(), message.getSceneSetting(),
                    message.getPovCharacter(), message.getPresentCharacters()}) {
                if (StringUtils.isNotBlank(piece)) {
                    matched.append(piece.strip()).append('\n');
                }
            }
        }
        StringBuilder lore = new StringBuilder();
        long used = 0;
        int skipped = 0;
        for (LorebookEntryVerdict verdict : activationService.evaluateAll(root, books, tags, matched.toString())) {
            if (!verdict.verdict().activated()) {
                continue;
            }
            String payload = activationService.renderPayload(verdict.entry(), data);
            if (StringUtils.isBlank(payload)) {
                continue;
            }
            String entry = "[" + verdict.entry().getName() + "]:\n" + payload.strip();
            long tokens = service.countTokensApprox(entry);
            if (skipped > 0 || used + tokens > budget) {
                skipped++;
                continue;
            }
            lore.append(entry).append("\n\n");
            used += tokens;
        }
        if (skipped > 0) {
            note.append(skipped).append(" lorebook entries did not fit and were left out. ");
        }
        return lore.toString().strip();
    }

    /** Template data for the entries, set up like a quiet generation after the last part of the run. */
    private LorebookTemplateData templateData(Manuscript manuscript, List<ChatMessage> branch) throws Exception {
        TemplateContext context = new TemplateContext();
        ChatMessage last = branch.isEmpty() ? null : branch.getLast();
        if (last != null) {
            context.setPovCharacter(last.getPovCharacter());
            context.setPresentCharacters(last.getPresentCharacters());
            context.setSceneSetting(last.getSceneSetting());
            context.setInstructions(last.getInstructions());
            context.setLastInstructions(last.getInstructions());
            context.setLastMessageTime(last.getRequest());
        }
        String pov = manuscriptService.getPov(manuscript);
        String tense = manuscriptService.getTense(manuscript);
        String style = manuscriptService.getStyle(manuscript);
        context.setNarrativePov(pov);
        context.setNarrativeTense(tense);
        context.setStyle(style);
        context.setManuscriptName(manuscript.getName());
        context.setManuscriptDescription(manuscript.getDescription());
        context.setPickSeed(String.valueOf(manuscript.getId()));
        context.setGenerationType("quiet");
        List<String> story = new ArrayList<>();
        for (ChatMessage message : branch) {
            String text = partTexts.forModel(message);
            if (StringUtils.isNotBlank(text)) {
                story.add(text);
            }
        }
        context.setStoryMessages(story);
        for (ChatMessage message : branch.reversed()) {
            if (message.getSummary() != null) {
                Summary summary = summaryService.find(message.getSummary());
                if (summary != null) {
                    context.setLatestSummary(summary.getSummary());
                }
                break;
            }
        }
        if (last != null) {
            context.getVariables().loadFrom(TemplateVariables.Scope.LOCAL, last.getAttributes());
        }
        context.getVariables().loadFrom(TemplateVariables.Scope.GLOBAL, manuscript.getAttributes());

        LorebookTemplateData data = new LorebookTemplateData();
        if (last != null) {
            data.setPovCharacter(last.getPovCharacter());
            data.setSceneSetting(last.getSceneSetting());
            data.setPresentCharacters(last.getPresentCharacters());
            data.setInstructions(last.getInstructions());
        }
        data.setNarrativePov(pov);
        data.setNarrativeTense(tense);
        data.setStyle(style);
        data.setTemplateContext(context);
        return data;
    }
}
