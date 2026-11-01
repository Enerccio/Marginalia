package com.github.enerccio.marginalia.ui.menu;

import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryCard;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryEditor;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A menu of the story editor made from a {@link MenuDefinition}: the Vaadin items by the key of their definition (to reach
 * the item of a key, as the code used to keep a field for each), refreshed by {@link #refresh} with the text, the visibility, the
 * state and the key hint each item has for the story editor and the card of the moment.
 */
public final class BuiltMenu {

    private final Localization loc;
    private final StoryEditor part;
    private final Supplier<StoryCard> card;
    private final Map<String, StoryMenuItem> definitions = new LinkedHashMap<>();
    private final Map<String, MenuItem> items = new LinkedHashMap<>();
    private final Map<String, MenuItemContent> contents = new LinkedHashMap<>();
    private final Map<String, List<String>> shownChoices = new LinkedHashMap<>();
    private MenuDefinition definition;

    BuiltMenu(MenuDefinition definition, HasMenuItems menu, Localization loc, StoryEditor part, Supplier<StoryCard> card) {
        this.definition = definition;
        this.loc = loc;
        this.part = part;
        this.card = card;
        for (StoryMenuItem item : definition.getItems()) {
            definitions.put(item.getKey(), item);
        }
        for (StoryMenuItem item : definition.getItems()) {
            if (item.getParent() == null) {
                add(menu, item);
            }
        }
        refresh(definition);
    }

    private void add(HasMenuItems menu, StoryMenuItem item) {
        MenuItemContent content = new MenuItemContent(item.createIcon());
        MenuItem menuItem = menu.addItem(content, event -> run(item));
        if (item.isCheckable()) {
            menuItem.setCheckable(true);
        }
        items.put(item.getKey(), menuItem);
        contents.put(item.getKey(), content);
        definitions.values().stream().filter(child -> item.getKey().equals(child.getParent()))
                .forEach(child -> add(menuItem.getSubMenu(), child));
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
     * Works out the text, the visibility, the enabled and ticked state and the key of every item again, for the keys of the definition
     * (a new one after the user changed them).
     */
    public void refresh(MenuDefinition definition) {
        this.definition = definition;
        StoryCard current = card == null ? null : card.get();
        for (Map.Entry<String, StoryMenuItem> entry : definitions.entrySet()) {
            StoryMenuItem item = entry.getValue();
            MenuItem menuItem = items.get(entry.getKey());
            if (menuItem == null) {
                continue;
            }
            contents.get(entry.getKey()).set(item.getText(loc, part, current), definition.keyOf(item.getHintAction(part, current)));
            menuItem.setVisible(item.isVisible(part, current));
            menuItem.setEnabled(item.isEnabled(part, current));
            if (item.isCheckable()) {
                menuItem.setChecked(item.isChecked(part));
            }
            if (item.hasChoices()) {
                refreshChoices(item, menuItem);
            }
        }
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
