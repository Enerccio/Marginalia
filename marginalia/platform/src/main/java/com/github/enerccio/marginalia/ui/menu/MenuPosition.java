package com.github.enerccio.marginalia.ui.menu;

/**
 * Where an item or a group stands among the entries of its level of a menu. Positions are worked out when a menu is built
 * ({@link StoryMenuRegistry#definition}), not when the item is registered, so they do not depend on the order the extensions load in:
 * an entry that has none is placed after the others in the order of registration, an anchor that is not there (an extension that
 * is not loaded) leaves the entry at the end of its level.
 * <p>
 * An anchor is the key of an item or the path of a group in the same level.
 */
public final class MenuPosition {

    enum Type {DEFAULT, FIRST, LAST, INDEX, BEFORE, AFTER}

    static final MenuPosition DEFAULT = new MenuPosition(Type.DEFAULT, 0, null);

    private final Type type;
    private final int index;
    private final String anchor;

    private MenuPosition(Type type, int index, String anchor) {
        this.type = type;
        this.index = index;
        this.anchor = anchor;
    }

    /**
     * At the start of the level (several entries that ask for it keep the order of their registration).
     */
    public static MenuPosition first() {
        return new MenuPosition(Type.FIRST, 0, null);
    }

    /**
     * At the end of the level, even after the entries that are registered later (what an entry without a position gets only until
     * another one is registered).
     */
    public static MenuPosition last() {
        return new MenuPosition(Type.LAST, 0, null);
    }

    /**
     * At this place (counted from 0) in the level as it is when the position is applied: the entries without a position, in the order
     * of their registration, with the ones that are {@link #first()} in front; an index past the end means the end.
     */
    public static MenuPosition index(int index) {
        return new MenuPosition(Type.INDEX, Math.max(index, 0), null);
    }

    /**
     * Right before the entry.
     */
    public static MenuPosition before(String anchor) {
        return new MenuPosition(Type.BEFORE, 0, anchor);
    }

    /**
     * Right after the entry (and after its separator, if it has one, see {@link StoryMenuItem#separatorAfter}); entries placed after
     * the same one keep the order of their registration.
     */
    public static MenuPosition after(String anchor) {
        return new MenuPosition(Type.AFTER, 0, anchor);
    }

    Type type() {
        return type;
    }

    int index() {
        return index;
    }

    String anchor() {
        return anchor;
    }
}
