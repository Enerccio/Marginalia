package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read.StoryTree;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class AgentCopyServiceImpl implements AgentCopyService {

    private static final int PREVIEW = 90;

    @Autowired
    private AgentRunService agentRunService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TagRelationService tagRelationService;

    private Manuscript copyOf(AgentRun run) throws Exception {
        AgentRun stored = run == null ? null : agentRunService.findForUser(run.getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        if (stored.getWorkingCopy() == null) {
            throw new IllegalStateException("The run has no copy of the book");
        }
        return (Manuscript) Hibernate.unproxy(stored.getWorkingCopy());
    }

    private StoryTree tree(Manuscript copy) throws Exception {
        return new StoryTree(chatMessageService.getAllMessages(copy), copy.getActiveLeaf());
    }

    @Override
    @CommonTxReadOnly
    public List<CopyBranch> branches(AgentRun run) throws Exception {
        StoryTree tree = tree(copyOf(run));
        List<CopyBranch> branches = new ArrayList<>();
        for (ChatMessage leaf : tree.leaves) {
            branches.add(new CopyBranch(tree.leafNumbers.get(leaf.getId()), leaf.getUuid(), preview(leaf.getResponse()),
                    tree.pathTo(leaf).size(), leaf.getId().equals(tree.activeLeafId)));
        }
        return branches;
    }

    @Override
    @CommonTxReadOnly
    public List<CopyPart> branch(AgentRun run, String partUuid) throws Exception {
        StoryTree tree = tree(copyOf(run));
        ChatMessage leaf = null;
        if (partUuid != null) {
            ChatMessage part = tree.byId.values().stream().filter(m -> partUuid.equals(m.getUuid())).findFirst().orElse(null);
            leaf = part == null ? null : tree.leafOf(part);
        }
        if (leaf == null && tree.activeLeafId != null) {
            leaf = tree.byId.get(tree.activeLeafId);
        }
        if (leaf == null && !tree.leaves.isEmpty()) {
            leaf = tree.leaves.getLast();
        }
        List<CopyPart> parts = new ArrayList<>();
        if (leaf == null) {
            return parts;
        }
        int position = 0;
        for (ChatMessage message : tree.pathTo(leaf)) {
            ChatMessage parent = tree.parentOf(message);
            List<ChatMessage> alternatives = parent == null ? tree.roots() : tree.children.get(parent.getId());
            Summary summary = message.getSummary() == null ? null : summaryService.find(message.getSummary());
            parts.add(new CopyPart(message.getUuid(), ++position, alternatives.indexOf(message) + 1, alternatives.size(),
                    message.getResponse(), message.getInstructions(), message.getSceneSetting(), message.getPovCharacter(),
                    message.getPresentCharacters(), summary == null ? null : summary.getSummary(), message.isEdited(),
                    message.getWordCount()));
        }
        return parts;
    }

    @Override
    @CommonTxReadOnly
    public List<CopyLorebook> lorebooks(AgentRun run) throws Exception {
        Manuscript copy = copyOf(run);
        Lorebook root = copy.getLorebook() == null ? null : lorebookService.find(copy.getLorebook());
        List<CopyLorebook> books = new ArrayList<>();
        if (root == null || root.isDeleted()) {
            return books;
        }
        Set<Long> seen = new HashSet<>();
        Queue<Lorebook> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Lorebook next = queue.remove();
            if (!seen.add(next.getId())) {
                continue;
            }
            List<Lorebook> subbooks = lorebookService.getSubbooks(next);
            queue.addAll(subbooks);
            List<CopyEntry> entries = new ArrayList<>();
            for (LorebookEntry entry : lorebookEntryService.getEntriesForLorebook(next)) {
                entries.add(new CopyEntry(entry.getUuid(), entry.getName(), entry.isEnabled(), entry.getOrder(), entry.getPayload(),
                        entry.getComment(), tags(entry, false), tags(entry, true), entry.getFiltering(),
                        entry.getFilteringMode() == null ? null : entry.getFilteringMode().name(),
                        entry.getInsertionMode() == null ? null : entry.getInsertionMode().name()));
            }
            books.add(new CopyLorebook(next.getUuid(), next.getName(), next.isEnabled(),
                    subbooks.stream().map(Lorebook::getName).toList(), entries));
        }
        return books;
    }

    private List<String> tags(LorebookEntry entry, boolean negative) throws Exception {
        return tagRelationService.getTagsForObject(entry, negative).stream().map(Tag::getValue).toList();
    }

    private static String preview(String text) {
        String line = text == null ? "" : text.strip().replaceAll("\\s+", " ");
        return line.length() > PREVIEW ? line.substring(0, PREVIEW).stripTrailing() + "…" : line;
    }
}
