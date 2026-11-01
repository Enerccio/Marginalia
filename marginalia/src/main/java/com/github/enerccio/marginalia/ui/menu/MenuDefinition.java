package com.github.enerccio.marginalia.ui.menu;

import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryCard;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryEditor;
import com.github.enerccio.marginalia.ui.shortcuts.KeyBindings;
import com.vaadin.flow.component.contextmenu.HasMenuItems;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * One of the menus of the story editor as the {@link StoryMenuRegistry} defines it for the moment it is asked: the tree of its items
 * and groups in the order they are shown, and the keys of the user, the changed ones or the defaults. The story tab builds its menu from it ({@link #build}) and gives it again to {@link BuiltMenu#refresh} when the keys changed.
 */
public final class MenuDefinition {

    private final MenuKind kind;
    private final List<MenuNode> roots;
    private final List<StoryMenuItem> items;
    private final KeyBindings bindings;

    MenuDefinition(MenuKind kind, List<MenuNode> roots, KeyBindings bindings) {
        this.kind = kind;
        this.roots = List.copyOf(roots);
        List<StoryMenuItem> all = new ArrayList<>();
        collect(roots, all);
        this.items = List.copyOf(all);
        this.bindings = bindings;
    }

    private static void collect(List<MenuNode> nodes, List<StoryMenuItem> into) {
        for (MenuNode node : nodes) {
            if (node.getItem() != null) {
                into.add(node.getItem());
            }
            collect(node.getChildren(), into);
        }
    }

    /**
     * The entries of the top level of the menu in the order they are shown; the groups and the items that hold a sub menu have their
     * entries in {@link MenuNode#getChildren}.
     */
    public List<MenuNode> getRoots() {
        return roots;
    }

    public MenuKind getKind() {
        return kind;
    }

    /**
     * The items of the menu, the ones of sub menus too, in the order they are shown (the order of the tree, depth first).
     */
    public List<StoryMenuItem> getItems() {
        return items;
    }

    /**
     * The key combination of the action, null for none or when the menu shows no keys (the phone).
     */
    public String keyOf(String actionId) {
        return bindings == null || actionId == null ? null : bindings.get(actionId);
    }

    /**
     * Adds the items to the menu (a {@code ContextMenu} or the sub menu of a menu bar item) and wires them: a click runs the item for
     * the story editor and the card of the moment.
     *
     * @param card the card the menu is of, null for the tools menu
     */
    public BuiltMenu build(HasMenuItems menu, Localization loc, StoryEditor part, Supplier<StoryCard> card) {
        return new BuiltMenu(this, menu, loc, part, card);
    }
}
