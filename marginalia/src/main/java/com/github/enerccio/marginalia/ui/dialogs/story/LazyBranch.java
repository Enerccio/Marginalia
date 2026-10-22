package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Which parts of a branch a view has loaded: a window over the branch that slides while the user scrolls. A view starts
 * with one end of the branch - the story with the last parts ({@link #tail}), the reader with the first ones
 * ({@link #head}) - loads one chunk (half of the window) when the user scrolls to an edge and drops the parts at the
 * opposite edge ({@link #trimLater}, {@link #trimEarlier}), so no more than the window is loaded at a time. The size of
 * the window is the setting of the user, {@code null} or less than one loads the whole branch.
 */
public final class LazyBranch {

    private final List<ChatMessage> branch;
    private final int size;
    private final int chunk;
    private int from;
    private int to;

    private LazyBranch(List<ChatMessage> branch, Integer limit, boolean fromEnd) {
        this.branch = new ArrayList<>(branch);
        this.size = limit == null || limit < 1 ? Integer.MAX_VALUE : limit;
        this.chunk = size == Integer.MAX_VALUE ? size : Math.max(1, size / 2);
        int total = branch.size();
        if (fromEnd) {
            this.from = Math.max(0, total - size);
            this.to = total;
        } else {
            this.from = 0;
            this.to = (int) Math.min(total, (long) size);
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
     * Whether the window is at the end of the branch, where a new part is added.
     */
    public boolean atTail() {
        return !hasLater();
    }

    /**
     * Extends the window by one chunk towards the start of the branch. The window can be larger than its size until
     * {@link #trimLater} is called.
     *
     * @return the parts that were added, in the order of the branch
     */
    public List<ChatMessage> loadEarlier() {
        int newFrom = (int) Math.max(0, (long) from - chunk);
        List<ChatMessage> added = List.copyOf(branch.subList(newFrom, from));
        from = newFrom;
        return added;
    }

    /**
     * Extends the window by one chunk towards the end of the branch. The window can be larger than its size until
     * {@link #trimEarlier} is called.
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
     * Drops the parts at the end of the window that do not fit its size, after parts were loaded at its start.
     *
     * @return the parts that were dropped, in the order of the branch
     */
    public List<ChatMessage> trimLater() {
        int newTo = (int) Math.min(to, (long) from + size);
        List<ChatMessage> removed = List.copyOf(branch.subList(newTo, to));
        to = newTo;
        return removed;
    }

    /**
     * Drops the parts at the start of the window that do not fit its size, after parts were loaded at its end.
     *
     * @return the parts that were dropped, in the order of the branch
     */
    public List<ChatMessage> trimEarlier() {
        int newFrom = (int) Math.max(from, (long) to - size);
        List<ChatMessage> removed = List.copyOf(branch.subList(from, newFrom));
        from = newFrom;
        return removed;
    }

    /**
     * Moves the window to the end of the branch, where a new part is added.
     */
    public void moveToTail() {
        from = Math.max(0, branch.size() - size);
        to = branch.size();
    }

    /**
     * Moves the window so the part is in the middle of it, for a part that is far from what is loaded. Nothing happens
     * when the part is loaded already or is not on the branch.
     *
     * @return whether the window moved, i.e. the parts have to be loaded again
     */
    public boolean centerOn(Long messageId) {
        int index = indexOf(messageId);
        if (index < 0 || isLoaded(index)) {
            return false;
        }
        int length = (int) Math.min(size, (long) branch.size());
        from = Math.max(0, Math.min(index - length / 2, branch.size() - length));
        to = from + length;
        return true;
    }

    public boolean isLoaded(Long messageId) {
        return isLoaded(indexOf(messageId));
    }

    private boolean isLoaded(int index) {
        return index >= from && index < to;
    }

    /**
     * Replaces the part by the instance a card holds now, the edit of a part saves a new instance and a card made of
     * the old one later would show the text from before the edit.
     */
    public void refresh(ChatMessage message) {
        int index = message == null ? -1 : indexOf(message.getId());
        if (index >= 0) {
            branch.set(index, message);
        }
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
}
