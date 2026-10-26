package com.github.enerccio.marginalia.ui.menu;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryCard;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryEditor;
import com.vaadin.flow.component.icon.Icon;

import java.util.Objects;
import java.util.function.*;

/**
 * The definition of an item of one of the menus of the story editor (see {@link MenuKind}), registered in the
 * {@link StoryMenuRegistry}: its key, its text, what it does, when it is shown, and the key combination it has until the user
 * changes it. Every item that has an action gets a row in the key bindings of the user settings, whether it has a default key or not.
 * <p>
 * The callbacks take the story editor ({@link StoryEditor}: the desktop or the phone story tab) and, for the menus of a part, the
 * card of the part ({@link StoryCard}). An item with {@link #parent} is an item of the sub menu of the item with that key (which has no
 * action of its own); the text, the visibility and the state of an item are worked out each time its menu opens, so a sub menu can
 * have items that come and go.
 */
public final class StoryMenuItem {

    /**
     * The text of an item for the story editor and the card it is shown for (the card is null in the tools menu).
     */
    @FunctionalInterface
    public interface ContextLabel {
        String get(Localization loc, StoryEditor part, StoryCard card);
    }

    private final String key;
    private final MenuKind kind;
    private final Function<Localization, String> label;
    private final Consumer<StoryEditor> toolsAction;
    private final BiConsumer<StoryEditor, StoryCard> cardAction;

    private ContextLabel contextLabel;
    private Supplier<Icon> icon;
    private String actionId;
    private String defaultKey;
    private boolean phone = true;
    private String parent;
    private BiPredicate<StoryEditor, StoryCard> visible = (part, card) -> true;
    private BiPredicate<StoryEditor, StoryCard> enabled = (part, card) -> true;
    private Predicate<StoryEditor> checked;
    private ContextAction hintAction;
    private volatile boolean retired;

    /**
     * The id of the action whose key an item shows, given what the item is shown for.
     */
    @FunctionalInterface
    public interface ContextAction {
        String get(StoryEditor part, StoryCard card);
    }

    private StoryMenuItem(String key, MenuKind kind, Function<Localization, String> label,
                          Consumer<StoryEditor> toolsAction, BiConsumer<StoryEditor, StoryCard> cardAction) {
        this.key = Objects.requireNonNull(key, "key");
        this.kind = kind;
        this.label = Objects.requireNonNull(label, "label");
        this.toolsAction = toolsAction;
        this.cardAction = cardAction;
        this.actionId = toolsAction == null && cardAction == null ? null : "menu." + key;
    }

    /**
     * An item of the settings menu of the story.
     *
     * @param key    identifies the item, a plugin prefixes it with its name
     * @param action does what the item says, null for an item that only holds a sub menu
     */
    public static StoryMenuItem tools(String key, Function<Localization, String> label, Consumer<StoryEditor> action) {
        return new StoryMenuItem(key, MenuKind.TOOLS, label, action, null);
    }

    public static StoryMenuItem tools(String key, L label, Consumer<StoryEditor> action) {
        return tools(key, loc -> loc.getValue(label), action);
    }

    /**
     * An item of the menu of a part.
     *
     * @param action does what the item says, null for an item that only holds a sub menu
     */
    public static StoryMenuItem card(String key, Function<Localization, String> label, BiConsumer<StoryEditor, StoryCard> action) {
        return new StoryMenuItem(key, MenuKind.CARD, label, null, action);
    }

    public static StoryMenuItem card(String key, L label, BiConsumer<StoryEditor, StoryCard> action) {
        return card(key, loc -> loc.getValue(label), action);
    }

    /**
     * An item of the menu of the text of a part (right click).
     */
    public static StoryMenuItem text(String key, Function<Localization, String> label, BiConsumer<StoryEditor, StoryCard> action) {
        return new StoryMenuItem(key, MenuKind.TEXT, label, null, action);
    }

    public static StoryMenuItem text(String key, L label, BiConsumer<StoryEditor, StoryCard> action) {
        return text(key, loc -> loc.getValue(label), action);
    }

    /**
     * The text of the item when it depends on the part ("Edit" or "Save"); the plain label stays the name of the action in the key
     * bindings.
     */
    public StoryMenuItem contextLabel(ContextLabel contextLabel) {
        this.contextLabel = contextLabel;
        return this;
    }

    /**
     * The icon in front of the text, a new one for every menu the item is shown in.
     */
    public StoryMenuItem icon(Supplier<Icon> icon) {
        this.icon = icon;
        return this;
    }

    /**
     * The id of the action in the key bindings (default {@code "menu." + key}), to keep the id a user already has a key stored for, or to
     * make two items (a part menu and a text menu item) one action.
     */
    public StoryMenuItem actionId(String actionId) {
        this.actionId = actionId;
        return this;
    }

    /**
     * The key combination the item has until the user changes it (see {@code KeyCombo}), null for none (the default).
     */
    public StoryMenuItem defaultKey(String defaultKey) {
        this.defaultKey = defaultKey;
        return this;
    }

    /**
     * Whether the phone shows the item too (default yes).
     */
    public StoryMenuItem phone(boolean phone) {
        this.phone = phone;
        return this;
    }

    /**
     * Makes the item an item of the sub menu of the item with this key (of the same kind).
     */
    public StoryMenuItem parent(String parentKey) {
        this.parent = parentKey;
        return this;
    }

    /**
     * When the item is shown; asked every time the menu opens (and after an item of the tools menu ran). The card is null in the tools menu.
     */
    public StoryMenuItem visibleWhen(BiPredicate<StoryEditor, StoryCard> visible) {
        this.visible = visible;
        return this;
    }

    /**
     * When the item can be chosen; asked as {@link #visibleWhen}.
     */
    public StoryMenuItem enabledWhen(BiPredicate<StoryEditor, StoryCard> enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Makes the item a check box item, ticked when the predicate says so (the action switches the thing the predicate asks).
     */
    public StoryMenuItem checkedWhen(Predicate<StoryEditor> checked) {
        this.checked = checked;
        return this;
    }

    /**
     * The action whose key the item shows, when that is not its own (the edit item shows the key of saving while the text is edited).
     */
    public StoryMenuItem hintAction(ContextAction hintAction) {
        this.hintAction = hintAction;
        return this;
    }

    public String getKey() {
        return key;
    }

    public MenuKind getKind() {
        return kind;
    }

    /**
     * The name of the action in the key bindings.
     */
    public String getLabel(Localization loc) {
        return label.apply(loc);
    }

    /**
     * The text of the item for the part it is shown for.
     */
    public String getText(Localization loc, StoryEditor part, StoryCard card) {
        return contextLabel == null ? label.apply(loc) : contextLabel.get(loc, part, card);
    }

    public Icon createIcon() {
        return icon == null ? null : icon.get();
    }

    /**
     * The id of the action in the key bindings, null for an item that only holds a sub menu.
     */
    public String getActionId() {
        return actionId;
    }

    public String getDefaultKey() {
        return defaultKey;
    }

    public boolean isPhone() {
        return phone;
    }

    public String getParent() {
        return parent;
    }

    public boolean hasAction() {
        return toolsAction != null || cardAction != null;
    }

    public boolean isCheckable() {
        return checked != null;
    }

    public boolean isChecked(StoryEditor part) {
        return checked != null && checked.test(part);
    }

    /**
     * Whether the item was unregistered: the menus that were built with it do not show it any more and it does nothing.
     */
    public boolean isRetired() {
        return retired;
    }

    void retire() {
        retired = true;
    }

    public boolean isVisible(StoryEditor part, StoryCard card) {
        return !retired && visible.test(part, card);
    }

    public boolean isEnabled(StoryEditor part, StoryCard card) {
        return enabled.test(part, card);
    }

    /**
     * The id of the action whose key the item shows for the part, null for none.
     */
    public String getHintAction(StoryEditor part, StoryCard card) {
        return hintAction == null ? actionId : hintAction.get(part, card);
    }

    /**
     * Does what the item says.
     */
    public void run(StoryEditor part, StoryCard card) {
        if (retired) {
            return;
        }
        if (toolsAction != null) {
            toolsAction.accept(part);
        } else if (cardAction != null) {
            cardAction.accept(part, card);
        }
    }
}
