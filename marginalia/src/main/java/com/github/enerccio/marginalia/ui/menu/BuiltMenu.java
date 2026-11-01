package com.github.enerccio.marginalia.ui.menu;

import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryCard;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryEditor;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.ContextMenuBase;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenuBase;
import com.vaadin.flow.component.html.Hr;

import java.util.*;
import java.util.function.Supplier;

/**
 * A menu of the story editor made from a {@link MenuDefinition}: the Vaadin items by the key of their definition (to reach
 * the item of a key, as the code used to keep a field for each) and by the path of their group, refreshed by {@link #refresh} with the
 * text, the visibility, the state and the key hint each item has for the story editor and the card of the moment. A group is shown
 * while one of its entries is, and the line after an entry only while the entry is and another entry follows it.
 */
public final class BuiltMenu {

    private final Localization loc;
    private final StoryEditor part;
    private final Supplier<StoryCard> card;
    private final Map<MenuNode, MenuItem> menuItems = new IdentityHashMap<>();
    private final Map<MenuNode, MenuItemContent> contents = new IdentityHashMap<>();
    private final Map<MenuNode, Hr> separators = new IdentityHashMap<>();
    private final Map<String, MenuItem> items = new LinkedHashMap<>();
    private final Map<String, MenuItem> groups = new HashMap<>();
    private final Map<String, List<String>> shownChoices = new LinkedHashMap<>();
    // the entries the Vaadin items were made for; a definition that is asked later (the keys of the user changed) is a new tree
    private final List<MenuNode> roots;
    private MenuDefinition definition;

    BuiltMenu(MenuDefinition definition, HasMenuItems menu, Localization loc, StoryEditor part, Supplier<StoryCard> card) {
        this.definition = definition;
        this.loc = loc;
        this.part = part;
        this.card = card;
        this.roots = definition.getRoots();
        for (MenuNode node : roots) {
            add(menu, node);
        }
        refresh(definition);
    }

    private void add(HasMenuItems menu, MenuNode node) {
        MenuItemContent content = new MenuItemContent(node.createIcon());
        StoryMenuItem item = node.getItem();
        MenuItem menuItem = menu.addItem(content, event -> {
            if (item != null) {
                run(item);
            }
        });
        if (item != null) {
            if (item.isCheckable()) {
                menuItem.setCheckable(true);
            }
            items.put(item.getKey(), menuItem);
        } else {
            groups.put(node.getId(), menuItem);
        }
        menuItems.put(node, menuItem);
        contents.put(node, content);
        for (MenuNode child : node.getChildren()) {
            add(menuItem.getSubMenu(), child);
        }
        if (node.hasSeparatorAfter()) {
            Hr separator = new Hr();
            addComponent(menu, separator);
            separators.put(node, separator);
        }
    }

    // what ContextMenu.addSeparator does, with the line kept to show and hide it
    private static void addComponent(HasMenuItems menu, Component component) {
        if (menu instanceof ContextMenuBase<?, ?, ?> contextMenu) {
            contextMenu.addComponent(component);
        } else if (menu instanceof SubMenuBase<?, ?, ?> subMenu) {
            subMenu.addComponent(component);
        }
    }

    private void run(StoryMenuItem item) {
        if (!item.hasAction()) {
            return;
        }
        item.run(part, card == null ? null : card.get());
        // what an item changed (a ticked item, a part that is no longer the last) shows the next time the menu opens, the tools menu is not
        // reopened by a lazy listener
        refresh(definition);
    }

    /**
     * The item of the key, null when the menu has none.
     */
    public MenuItem get(String key) {
        return items.get(key);
    }

    /**
     * The item of the group with this path, null when the menu has none.
     */
    public MenuItem getGroup(String path) {
        return groups.get(path);
    }

    /**
     * Works out the text, the visibility, the enabled and ticked state and the key of every item again, for the keys of the definition
     * (a new one after the user changed them). The menu keeps the shape it was built with, the definition only gives the keys.
     */
    public void refresh(MenuDefinition definition) {
        this.definition = definition;
        refreshLevel(roots, card == null ? null : card.get());
    }

    // returns whether an entry of the level is shown
    private boolean refreshLevel(List<MenuNode> level, StoryCard current) {
        boolean[] shown = new boolean[level.size()];
        boolean any = false;
        for (int i = 0; i < shown.length; i++) {
            shown[i] = refreshNode(level.get(i), current);
            any |= shown[i];
        }
        boolean later = false;
        for (int i = shown.length - 1; i >= 0; i--) {
            Hr separator = separators.get(level.get(i));
            if (separator != null) {
                separator.setVisible(shown[i] && later);
            }
            later |= shown[i];
        }
        return any;
    }

    private boolean refreshNode(MenuNode node, StoryCard current) {
        MenuItem menuItem = menuItems.get(node);
        MenuItemContent content = contents.get(node);
        boolean entries = refreshLevel(node.getChildren(), current);
        if (node.isGroup()) {
            content.set(node.getText(loc, part, current), null);
            menuItem.setVisible(entries);
            return entries;
        }
        StoryMenuItem item = node.getItem();
        content.set(item.getText(loc, part, current), definition.keyOf(item.getHintAction(part, current)));
        boolean visible = item.isVisible(part, current);
        // an item that only holds a sub menu is no use without an entry in it
        if (visible && !item.hasAction() && !item.hasChoices() && !node.getChildren().isEmpty()) {
            visible = entries;
        }
        menuItem.setVisible(visible);
        menuItem.setEnabled(item.isEnabled(part, current));
        if (item.isCheckable()) {
            menuItem.setChecked(item.isChecked(part));
        }
        if (item.hasChoices()) {
            refreshChoices(item, menuItem);
        }
        return visible;
    }

    // the sub menu is made again only when what it shows changed
    private void refreshChoices(StoryMenuItem item, MenuItem menuItem) {
        List<StoryMenuItem.Choice> choices = item.getChoices(part);
        // the actions are new lambdas every time: what the user sees is the text and the tick
        List<String> shown = choices.stream().map(choice -> choice.text() + "\u0000" + choice.checked()).toList();
        if (shown.equals(shownChoices.get(item.getKey()))) {
            return;
        }
        shownChoices.put(item.getKey(), shown);
        menuItem.getSubMenu().removeAll();
        for (StoryMenuItem.Choice choice : choices) {
            MenuItem entry = menuItem.getSubMenu().addItem(choice.text(), event -> {
                if (!item.isRetired()) {
                    choice.action().run();
                    refresh(definition);
                }
            });
            entry.setCheckable(true);
            entry.setChecked(choice.checked());
        }
    }

    /**
     * Refreshes with the definition the menu has.
     */
    public void refresh() {
        refresh(definition);
    }
}
