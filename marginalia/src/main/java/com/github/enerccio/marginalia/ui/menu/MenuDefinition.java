package com.github.enerccio.marginalia.ui.menu;

import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryCard;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryEditor;
import com.github.enerccio.marginalia.ui.shortcuts.KeyBindings;
import com.vaadin.flow.component.contextmenu.HasMenuItems;

import java.util.List;
import java.util.function.Supplier;

/**
 * One of the menus of the story editor as the {@link StoryMenuRegistry} defines it for the moment it is asked: the items in the order
 * of their registration (the items of a sub menu after their parent in the tree) and the keys of the user, the changed ones or the
 * defaults. The story tab builds its menu from it ({@link #build}) and gives it again to {@link BuiltMenu#refresh} when the keys changed.
 */
public final class MenuDefinition {

    private final MenuKind kind;
    private final List<StoryMenuItem> items;
    private final KeyBindings bindings;

    MenuDefinition(MenuKind kind, List<StoryMenuItem> items, KeyBindings bindings) {
        this.kind = kind;
        this.items = List.copyOf(items);
        this.bindings = bindings;
    }

    public MenuKind getKind() {
        return kind;
    }

    /**
     * The items of the menu, the ones of sub menus too.
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
