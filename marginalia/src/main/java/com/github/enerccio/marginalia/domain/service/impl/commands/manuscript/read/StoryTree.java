package com.github.enerccio.marginalia.domain.service.impl.commands.manuscript.read;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;

import java.util.*;

/**
 * The parts of the book as a tree, with the numbers of the branches.
 */
public final class StoryTree {

    public static final Comparator<ChatMessage> CREATED = Comparator
            .comparing(ChatMessage::getCreation, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(ChatMessage::getId);

    public final Map<Long, ChatMessage> byId = new HashMap<>();
    public final Map<Long, List<ChatMessage>> children = new HashMap<>();
    // the ends of the branches, the oldest first
    public final List<ChatMessage> leaves = new ArrayList<>();
    public final Map<Long, Integer> leafNumbers = new HashMap<>();
    public final Set<Long> activeIds = new HashSet<>();
    public final Long activeLeafId;

    public StoryTree(List<ChatMessage> all, ChatMessage activeLeaf) {
        for (ChatMessage message : all) {
            if (!message.isDeleted()) {
                byId.put(message.getId(), message);
            }
        }
        for (ChatMessage message : byId.values()) {
            if (message.getParent() != null && byId.containsKey(message.getParent().getId())) {
                children.computeIfAbsent(message.getParent().getId(), _ -> new ArrayList<>()).add(message);
            }
        }
        children.values().forEach(list -> list.sort(CREATED));
        for (ChatMessage message : byId.values()) {
            if (!children.containsKey(message.getId())) {
                leaves.add(message);
            }
        }
        leaves.sort(CREATED);
        for (int i = 0; i < leaves.size(); i++) {
            leafNumbers.put(leaves.get(i).getId(), i + 1);
        }
        activeLeafId = activeLeaf == null || !byId.containsKey(activeLeaf.getId()) ? null : activeLeaf.getId();
        if (activeLeafId != null) {
            pathTo(byId.get(activeLeafId)).forEach(message -> activeIds.add(message.getId()));
        }
    }

    public ChatMessage parentOf(ChatMessage message) {
        return message.getParent() == null ? null : byId.get(message.getParent().getId());
    }

    /** The root first. */
    public List<ChatMessage> pathTo(ChatMessage message) {
        List<ChatMessage> path = new ArrayList<>();
        for (ChatMessage current = message; current != null; current = parentOf(current)) {
            path.add(current);
        }
        Collections.reverse(path);
        return path;
    }

    /** The end of the branch that shows the part: the active leaf if the part is on the active branch, else the newest below it. */
    public ChatMessage leafOf(ChatMessage message) {
        if (activeIds.contains(message.getId())) {
            return byId.get(activeLeafId);
        }
        ChatMessage current = message;
        while (children.containsKey(current.getId())) {
            current = children.get(current.getId()).getLast();
        }
        return current;
    }

    /** The number of a branch that shows the part: the active one if the part is on it, else the newest below it. */
    public int branchOf(ChatMessage message) {
        return leafNumbers.getOrDefault(leafOf(message).getId(), 0);
    }

    public int siblings(ChatMessage message) {
        ChatMessage parent = parentOf(message);
        return parent == null ? roots().size() : children.get(parent.getId()).size();
    }

    public List<ChatMessage> roots() {
        return byId.values().stream().filter(m -> parentOf(m) == null).sorted(CREATED).toList();
    }

    int depth(ChatMessage message) {
        int depth = 0;
        for (ChatMessage current = message; current != null; current = parentOf(current)) {
            depth++;
        }
        return depth;
    }

}
