package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;

import java.util.List;

/**
 * Which parts of a branch a view has loaded so far. A view starts with one end of the branch - the story with the last
 * parts ({@link #tail}), the reader with the first ones ({@link #head}) - and extends the window by one chunk when the
 * user scrolls to its edge. The limit is the setting of the user, {@code null} or less than one loads the whole branch.
 */
public final class LazyBranch {

    private final List<ChatMessage> branch;
    private final int chunk;
    private int from;
    private int to;

    private LazyBranch(List<ChatMessage> branch, Integer limit, boolean fromEnd) {
        this.branch = branch;
        this.chunk = limit == null || limit < 1 ? Integer.MAX_VALUE : limit;
        int size = branch.size();
        if (fromEnd) {
            this.from = Math.max(0, size - chunk);
            this.to = size;
        } else {
            this.from = 0;
            this.to = (int) Math.min(size, (long) chunk);
        }
    }

    /**
     * Starts with the last {@code limit} parts.
     */
    public static LazyBranch tail(List<ChatMessage> branch, Integer limit) {
        return new LazyBranch(branch, limit, true);
    }

    /**
     * Starts with the first {@code limit} parts.
     */
    public static LazyBranch head(List<ChatMessage> branch, Integer limit) {
        return new LazyBranch(branch, limit, false);
    }

    /**
     * The parts that are loaded, in the order of the branch.
     */
    public List<ChatMessage> loaded() {
        return branch.subList(from, to);
    }

    /**
     * Index in the branch of the first loaded part, i.e. how many parts are before the window.
     */
    public int offset() {
        return from;
    }

    public int total() {
        return branch.size();
    }

    public boolean hasEarlier() {
        return from > 0;
    }

    public boolean hasLater() {
        return to < branch.size();
    }

    /**
     * Extends the window by one chunk towards the start of the branch.
     *
     * @return the parts that were added, in the order of the branch
     */
    public List<ChatMessage> loadEarlier() {
        return extendTo((int) Math.max(0, (long) from - chunk));
    }

    /**
     * Extends the window by one chunk towards the end of the branch.
     *
     * @return the parts that were added, in the order of the branch
     */
    public List<ChatMessage> loadLater() {
        int end = (int) Math.min(branch.size(), (long) to + chunk);
        List<ChatMessage> added = List.copyOf(branch.subList(to, end));
        to = end;
        return added;
    }

    /**
     * Extends the window towards the start of the branch by as many chunks as it takes to contain the part; nothing
     * happens when the part is loaded already or is not on the branch.
     *
     * @return the parts that were added, in the order of the branch
     */
    public List<ChatMessage> loadEarlierThrough(Long messageId) {
        int index = indexOf(messageId);
        if (index < 0 || index >= from) {
            return List.of();
        }
        int missing = from - index;
        int chunks = (int) ((missing + (long) chunk - 1) / chunk);
        return extendTo((int) Math.max(0, (long) from - (long) chunks * chunk));
    }

    /**
     * Index of the part in the branch, -1 when it is not on it.
     */
    public int indexOf(Long messageId) {
        if (messageId == null) {
            return -1;
        }
        for (int i = 0; i < branch.size(); i++) {
            if (messageId.equals(branch.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    private List<ChatMessage> extendTo(int newFrom) {
        List<ChatMessage> added = List.copyOf(branch.subList(newFrom, from));
        from = newFrom;
        return added;
    }
}
