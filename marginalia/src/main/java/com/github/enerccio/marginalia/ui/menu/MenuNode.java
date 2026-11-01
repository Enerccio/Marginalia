package com.github.enerccio.marginalia.ui.menu;

import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryCard;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryEditor;
import com.vaadin.flow.component.icon.Icon;

import java.util.ArrayList;
import java.util.List;

/**
 * An entry of a {@link MenuDefinition}: an item, or a group that holds the entries of its sub menu, in the order they are shown.
 * An item that is the {@link StoryMenuItem#parent parent} of others holds them too.
 */
public final class MenuNode {

    private final String id;
    private final StoryMenuItem item;
    private final MenuGroup group;
    private final String name;
    private final List<MenuNode> children = new ArrayList<>();
    private long order;

    MenuNode(StoryMenuItem item, long order) {
        this.id = item.getKey();
        this.item = item;
        this.group = null;
        this.name = null;
        this.order = order;
    }

    MenuNode(String path, String name, MenuGroup group) {
        this.id = path;
        this.item = null;
        this.group = group;
        this.name = name;
        this.order = Long.MAX_VALUE;
    }

    /**
     * The key of the item or the path of the group, what a {@link MenuPosition} refers to.
     */
    public String getId() {
        return id;
    }

    public boolean isGroup() {
        return item == null;
    }

    /**
     * The item, null for a group.
     */
    public StoryMenuItem getItem() {
        return item;
    }

    public List<MenuNode> getChildren() {
        return children;
    }

    public String getText(Localization loc, StoryEditor part, StoryCard card) {
        if (item != null) {
            return item.getText(loc, part, card);
        }
        String text = group == null ? null : group.getText(loc);
        return text == null ? name : text;
    }

    Icon createIcon() {
        return item != null ? item.createIcon() : group == null ? null : group.createIcon();
    }

    boolean hasSeparatorAfter() {
        return item != null ? item.hasSeparatorAfter() : group != null && group.hasSeparatorAfter();
    }

    MenuPosition position() {
        return item != null ? item.getPosition() : group == null ? MenuPosition.DEFAULT : group.getPosition();
    }

    MenuGroup group() {
        return group;
    }

    long order() {
        return order;
    }

    void order(long order) {
        this.order = order;
    }
}
