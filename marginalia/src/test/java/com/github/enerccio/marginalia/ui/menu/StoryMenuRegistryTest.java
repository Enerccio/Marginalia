package com.github.enerccio.marginalia.ui.menu;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryCard;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryEditor;
import com.github.enerccio.marginalia.ui.shortcuts.KeyBindings;
import com.github.enerccio.marginalia.ui.shortcuts.ShortcutAction;
import com.github.enerccio.marginalia.ui.shortcuts.ShortcutActions;
import com.github.enerccio.marginalia.ui.shortcuts.ShortcutRegistry;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The registry of the menu items of the story editor: what the application registers, the keys of the user in a definition, the
 * shortcut actions of the items, and a menu built from a definition (text, visibility, state, key hint, sub menus, what a click runs).
 */
class StoryMenuRegistryTest extends MarginaliaTestBase {

    @Autowired
    private StoryMenuRegistry registry;

    @Autowired
    private Localization loc;

    @AfterEach
    void unregisterTestItems() {
        registry.unregisterAll("test");
    }

    private static List<String> keys(MenuDefinition definition) {
        return definition.getItems().stream().map(StoryMenuItem::getKey).collect(Collectors.toList());
    }

    /**
     * A story card or editor that answers what the test sets, everything else does nothing.
     */
    private static StoryCard card(ChatMessage message, boolean editing, boolean last, List<String> calls) {
        return (StoryCard) Proxy.newProxyInstance(StoryCard.class.getClassLoader(), new Class<?>[]{StoryCard.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getMessage" -> message;
                    case "isEditing" -> editing;
                    case "isLast" -> last;
                    case "getHistoryCount" -> 0L;
                    default -> {
                        calls.add(method.getName());
                        yield method.getReturnType() == boolean.class ? Boolean.FALSE : null;
                    }
                });
    }

    private static StoryEditor editor(AtomicReference<Boolean> highlight, List<String> calls) {
        return (StoryEditor) Proxy.newProxyInstance(StoryEditor.class.getClassLoader(), new Class<?>[]{StoryEditor.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "isShowGeneratedDiff" -> highlight.get();
                    case "setShowGeneratedDiff" -> {
                        highlight.set((Boolean) args[0]);
                        calls.add("setShowGeneratedDiff");
                        yield null;
                    }
                    case "isAnnotationsInMargin" -> Boolean.FALSE;
                    case "isAutoAnnotate" -> Boolean.FALSE;
                    case "isMassDelete" -> Boolean.FALSE;
                    default -> {
                        calls.add(method.getName());
                        yield null;
                    }
                });
    }

    private static ChatMessage message(String response, boolean withParent) {
        ChatMessage message = new ChatMessage();
        message.setId(5L);
        message.setResponse(response);
        if (withParent) {
            ChatMessage parent = new ChatMessage();
            parent.setId(4L);
            message.setParent(parent);
        }
        return message;
    }

    private static String textOf(MenuItem item) {
        return descendants(item).stream().filter(c -> c instanceof Span && c.getElement().getClassList().contains("menu-item-text"))
                .map(c -> ((Span) c).getText()).findFirst().orElse(null);
    }

    private static String keyOf(MenuItem item) {
        return descendants(item).stream().filter(c -> c instanceof Span && c.getElement().getClassList().contains("menu-item-key"))
                .filter(Component::isVisible).map(c -> ((Span) c).getText()).findFirst().orElse(null);
    }

    private static List<Component> descendants(Component root) {
        List<Component> all = new ArrayList<>();
        root.getChildren().forEach(child -> {
            all.add(child);
            all.addAll(descendants(child));
        });
        return all;
    }

    @Test
    void theApplicationRegistersItsMenusAndThePhoneShowsOnlyTheItemsMarkedForIt() {
        // depth first: the items of a group follow it
        assertThat(keys(registry.definition(MenuKind.TOOLS, false, null))).containsExactly(
                StoryMenuKeys.SEARCH_REPLACE,
                StoryMenuKeys.CHANGE_STYLES, StoryMenuKeys.HIGHLIGHT_GENERATED, StoryMenuKeys.ANNOTATIONS_IN_MARGIN,
                StoryMenuKeys.ANNOTATE_RANGE, StoryMenuKeys.AUTO_ANNOTATE, StoryMenuKeys.BRANCH_TOKENS, StoryMenuKeys.LORE_USAGE, StoryMenuKeys.STATISTICS,
                StoryMenuKeys.IMPORT_ARCHIVE, StoryMenuKeys.EXPORT,
                StoryMenuKeys.CLEAR_BOOK_HISTORY, StoryMenuKeys.EMPTY_HISTORY_TRASH, StoryMenuKeys.MASS_DELETE,
                StoryMenuKeys.SAVE_LAYOUT_AS_DEFAULT, StoryMenuKeys.RESTORE_LAYOUT);
        assertThat(keys(registry.definition(MenuKind.TOOLS, true, null))).containsExactly(
                StoryMenuKeys.SEARCH_REPLACE, StoryMenuKeys.CHANGE_STYLES, StoryMenuKeys.HIGHLIGHT_GENERATED,
                StoryMenuKeys.ANNOTATE_RANGE, StoryMenuKeys.AUTO_ANNOTATE, StoryMenuKeys.BRANCH_TOKENS, StoryMenuKeys.LORE_USAGE, StoryMenuKeys.STATISTICS,
                StoryMenuKeys.IMPORT_ARCHIVE, StoryMenuKeys.EXPORT,
                StoryMenuKeys.CLEAR_BOOK_HISTORY, StoryMenuKeys.EMPTY_HISTORY_TRASH, StoryMenuKeys.MASS_DELETE);
        assertThat(ids(registry.definition(MenuKind.TOOLS, false, null).getRoots())).containsExactly(
                StoryMenuKeys.SEARCH_REPLACE, StoryMenuKeys.GROUP_APPEARANCE, StoryMenuKeys.GROUP_ANALYSIS,
                StoryMenuKeys.GROUP_IMPORT_EXPORT, StoryMenuKeys.GROUP_CLEAN_UP, StoryMenuKeys.GROUP_LAYOUT);
        assertThat(ids(registry.definition(MenuKind.TOOLS, true, null).getRoots())).as("the layout is desktop only, its group is not shown")
                .containsExactly(StoryMenuKeys.SEARCH_REPLACE, StoryMenuKeys.GROUP_APPEARANCE, StoryMenuKeys.GROUP_ANALYSIS,
                        StoryMenuKeys.GROUP_IMPORT_EXPORT, StoryMenuKeys.GROUP_CLEAN_UP);

        assertThat(registry.definition(MenuKind.CARD, false, null).getItems()).hasSize(19);
        assertThat(keys(registry.definition(MenuKind.CARD, false, null))).startsWith(StoryMenuKeys.EDIT, StoryMenuKeys.DISCARD);
        assertThat(ids(registry.definition(MenuKind.CARD, false, null).getRoots())).containsExactly(
                StoryMenuKeys.EDIT, StoryMenuKeys.DISCARD, StoryMenuKeys.REGENERATE, StoryMenuKeys.SWIPE, StoryMenuKeys.REDRAFT,
                StoryMenuKeys.HISTORY, StoryMenuKeys.IMAGES, StoryMenuKeys.IMAGE_PREVIEW, StoryMenuKeys.GROUP_ANNOTATE, StoryMenuKeys.GROUP_SUMMARY,
                StoryMenuKeys.GROUP_INSPECT, StoryMenuKeys.GROUP_BRANCH_MERGE, StoryMenuKeys.DELETE);
        assertThat(keys(registry.definition(MenuKind.TEXT, true, null))).containsExactly(
                StoryMenuKeys.TEXT_ANNOTATE_SELECTION, StoryMenuKeys.TEXT_REDRAFT_SELECTION, StoryMenuKeys.TEXT_COPY);
    }

    private static List<String> ids(List<MenuNode> nodes) {
        return nodes.stream().map(MenuNode::getId).collect(Collectors.toList());
    }

    private static MenuNode node(List<MenuNode> nodes, String id) {
        for (MenuNode node : nodes) {
            if (node.getId().equals(id)) {
                return node;
            }
            MenuNode inside = node(node.getChildren(), id);
            if (inside != null) {
                return inside;
            }
        }
        return null;
    }

    @Test
    void everyItemWithAnActionIsAShortcutActionEvenWithoutADefaultKey() {
        ShortcutAction delete = ShortcutRegistry.get("menu." + StoryMenuKeys.DELETE);
        assertThat(delete).isNotNull();
        assertThat(delete.getDefaultKey()).isNull();
        assertThat(delete.getLabel(loc)).isEqualTo(loc.getValue(L.LABEL_DELETE));
        assertThat(KeyBindings.of(null).asMap()).containsKey("menu." + StoryMenuKeys.DELETE);

        // the actions the keys of the user were stored for keep their ids; the part menu and the text menu share one for annotating
        assertThat(ShortcutRegistry.get(ShortcutActions.STORY_SWIPE).getDefaultKey()).isEqualTo("s");
        assertThat(ShortcutRegistry.get(ShortcutActions.STORY_SEARCH_REPLACE).getDefaultKey()).isEqualTo("Ctrl+Shift+h");
        // annotating the selection is in the right click menu only: the menu of the part takes the selection away
        assertThat(registry.find(StoryMenuKeys.TEXT_ANNOTATE_SELECTION).getActionId()).isEqualTo(ShortcutActions.STORY_ANNOTATE_SELECTION);
        assertThat(registry.findByAction(ShortcutActions.STORY_ANNOTATE_SELECTION).getKey()).isEqualTo(StoryMenuKeys.TEXT_ANNOTATE_SELECTION);
        assertThat(keys(registry.definition(MenuKind.CARD, false, null))).doesNotContain("card.annotateSelection");
        assertThat(registry.findByAction(ShortcutActions.STORY_SWIPE).getKey()).isEqualTo(StoryMenuKeys.SWIPE);
    }

    @Test
    void aDefinitionHasTheKeysOfTheUserOrTheDefaults() {
        UserSetting setting = new UserSetting();
        JsonObject stored = new JsonObject();
        stored.addProperty(ShortcutActions.STORY_SWIPE, "Ctrl+Shift+s");
        stored.addProperty("menu." + StoryMenuKeys.DELETE, "Ctrl+Delete");
        setting.setKeyBindings(stored);

        MenuDefinition changed = registry.definition(MenuKind.CARD, false, KeyBindings.of(setting));
        assertThat(changed.keyOf(ShortcutActions.STORY_SWIPE)).isEqualTo("Ctrl+Shift+s");
        assertThat(changed.keyOf("menu." + StoryMenuKeys.DELETE)).isEqualTo("Ctrl+Delete");
        assertThat(changed.keyOf(ShortcutActions.STORY_REGENERATE)).isEqualTo("r");
        assertThat(changed.keyOf("menu." + StoryMenuKeys.BRANCH)).isNull();

        assertThat(registry.definition(MenuKind.CARD, true, null).keyOf(ShortcutActions.STORY_SWIPE)).isNull();
    }

    @Test
    void aBuiltMenuShowsTextVisibilityStateAndKeysForThePartItIsShownFor() {
        ContextMenu menu = new ContextMenu(new Button());
        List<String> calls = new ArrayList<>();
        ChatMessage message = message("Some text.", true);
        boolean[] editing = {false};
        boolean[] last = {false};
        long[] history = {0};
        StoryCard card = (StoryCard) Proxy.newProxyInstance(StoryCard.class.getClassLoader(), new Class<?>[]{StoryCard.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getMessage" -> message;
                    case "isEditing" -> editing[0];
                    case "isLast" -> last[0];
                    case "getHistoryCount" -> history[0];
                    default -> {
                        calls.add(method.getName());
                        yield null;
                    }
                });
        MenuDefinition definition = registry.definition(MenuKind.CARD, false, KeyBindings.of(null));
        BuiltMenu built = definition.build(menu, loc, editor(new AtomicReference<>(false), calls), () -> card);

        assertThat(textOf(built.get(StoryMenuKeys.EDIT))).isEqualTo(loc.getValue(L.LABEL_EDIT));
        assertThat(textOf(built.get(StoryMenuKeys.HISTORY))).as("no count while the part has no versions").isEqualTo(loc.getValue(L.LABEL_HISTORY));
        history[0] = 3;
        built.refresh();
        assertThat(textOf(built.get(StoryMenuKeys.HISTORY))).isEqualTo(String.format(loc.getValue(L.LABEL_HISTORY_WITH_COUNT), 3));
        history[0] = 0;
        built.refresh();
        assertThat(keyOf(built.get(StoryMenuKeys.EDIT))).isEqualTo("e");
        assertThat(built.get(StoryMenuKeys.REGENERATE).isVisible()).isFalse();
        assertThat(built.get(StoryMenuKeys.DISCARD).isVisible()).isFalse();
        assertThat(built.get(StoryMenuKeys.MERGE).isVisible()).isTrue();
        assertThat(built.get(StoryMenuKeys.DELETE_SUMMARY).isEnabled()).isFalse();
        assertThat(keyOf(built.get(StoryMenuKeys.DELETE))).isNull();

        editing[0] = true;
        last[0] = true;
        built.refresh();
        // while the text is edited the item saves, and shows the key of saving
        assertThat(textOf(built.get(StoryMenuKeys.EDIT))).isEqualTo(loc.getValue(L.LABEL_SAVE));
        assertThat(keyOf(built.get(StoryMenuKeys.EDIT))).isEqualTo("Ctrl+s");
        assertThat(built.get(StoryMenuKeys.REGENERATE).isVisible()).isTrue();
        assertThat(built.get(StoryMenuKeys.DISCARD).isVisible()).isTrue();
        assertThat(built.get(StoryMenuKeys.REDRAFT).isVisible()).isFalse();
    }

    @Test
    void aClickRunsTheItemForTheStoryEditorAndTheCardAndTickedItemsFollowTheirState() {
        ContextMenu menu = new ContextMenu(new Button());
        List<String> calls = new ArrayList<>();
        AtomicReference<Boolean> highlight = new AtomicReference<>(false);
        BuiltMenu built = registry.definition(MenuKind.TOOLS, false, null).build(menu, loc, editor(highlight, calls), () -> null);

        MenuItem item = built.get(StoryMenuKeys.HIGHLIGHT_GENERATED);
        assertThat(item.isCheckable()).isTrue();
        assertThat(item.isChecked()).isFalse();
        ComponentUtil.fireEvent(item, new ClickEvent<>(item));
        assertThat(highlight.get()).isTrue();
        assertThat(item.isChecked()).isTrue();

        ComponentUtil.fireEvent(built.get(StoryMenuKeys.EXPORT), new ClickEvent<>(built.get(StoryMenuKeys.EXPORT)));
        assertThat(calls).contains("setShowGeneratedDiff", "openExportDialog");

        ContextMenu cardMenu = new ContextMenu(new Button());
        List<String> cardCalls = new ArrayList<>();
        StoryCard card = card(message("x", false), false, true, cardCalls);
        BuiltMenu builtCard = registry.definition(MenuKind.CARD, false, null).build(cardMenu, loc, editor(highlight, calls), () -> card);
        ComponentUtil.fireEvent(builtCard.get(StoryMenuKeys.SWIPE), new ClickEvent<>(builtCard.get(StoryMenuKeys.SWIPE)));
        assertThat(cardCalls).containsExactly("swipe");
    }

    @Test
    void aSubMenuOfChoicesIsMadeAgainOnlyWhenWhatItShowsChanges() {
        List<String> chosen = new ArrayList<>();
        List<StoryMenuItem.Choice> shown = new ArrayList<>();
        shown.add(new StoryMenuItem.Choice("Beta", false, () -> chosen.add("beta")));
        shown.add(new StoryMenuItem.Choice("Alpha", true, () -> chosen.add("alpha")));
        registry.register("test", StoryMenuItem.tools("test.styles", l -> "Styles", null).choices(story -> new ArrayList<>(shown)));
        ContextMenu menu = new ContextMenu(new Button());
        BuiltMenu built = registry.definition(MenuKind.TOOLS, false, null).build(menu, loc, editor(new AtomicReference<>(false), new ArrayList<>()), () -> null);

        MenuItem parent = built.get("test.styles");
        List<MenuItem> entries = parent.getSubMenu().getItems();
        assertThat(entries).extracting(MenuItem::getText).containsExactly("Beta", "Alpha");
        assertThat(entries).extracting(MenuItem::isChecked).containsExactly(false, true);

        // the same entries (the actions are new lambdas each time) keep the Vaadin items
        built.refresh();
        assertThat(parent.getSubMenu().getItems()).containsExactlyElementsOf(entries);

        ComponentUtil.fireEvent(entries.get(0), new ClickEvent<>(entries.get(0)));
        assertThat(chosen).containsExactly("beta");

        // a style that was added shows up the next time the menu is refreshed
        shown.add(new StoryMenuItem.Choice("Gamma", false, () -> chosen.add("gamma")));
        built.refresh();
        assertThat(parent.getSubMenu().getItems()).extracting(MenuItem::getText).containsExactly("Beta", "Alpha", "Gamma");
    }

    @Test
    void anItemThatIsUnregisteredIsHiddenAndRunsNothingInTheMenusThatWereBuiltWithIt() {
        List<String> ran = new ArrayList<>();
        registry.register("test", StoryMenuItem.tools("test.tool", l -> "Tool", story -> ran.add("tool")));
        ContextMenu menu = new ContextMenu(new Button());
        BuiltMenu built = registry.definition(MenuKind.TOOLS, false, null).build(menu, loc, editor(new AtomicReference<>(false), new ArrayList<>()), () -> null);
        MenuItem item = built.get("test.tool");
        assertThat(item.isVisible()).isTrue();

        registry.unregisterAll("test");
        built.refresh();
        assertThat(item.isVisible()).isFalse();
        ComponentUtil.fireEvent(item, new ClickEvent<>(item));
        assertThat(ran).isEmpty();
    }

    @Test
    void anExtensionRegistersItemsWithSubMenusThatComeAndGoAndLeavesNothingBehind() {
        List<String> ran = new ArrayList<>();
        boolean[] reviewed = {false};
        registry.register("test", StoryMenuItem.card("test.review", l -> "Review", null));
        registry.register("test", StoryMenuItem.card("test.review.view", l -> "View", (part, card) -> ran.add("view"))
                .parent("test.review").defaultKey("Ctrl+Alt+r"));
        registry.register("test", StoryMenuItem.card("test.review.delete", l -> "Delete Review", (part, card) -> ran.add("delete"))
                .parent("test.review").enabledWhen((part, card) -> reviewed[0]));
        registry.register("test", StoryMenuItem.card("test.orphan", l -> "Orphan", (part, card) -> ran.add("orphan")).parent("test.missing"));

        MenuDefinition definition = registry.definition(MenuKind.CARD, false, KeyBindings.of(null));
        assertThat(keys(definition)).contains("test.review", "test.review.view", "test.review.delete").doesNotContain("test.orphan");
        assertThat(definition.keyOf("menu.test.review.view")).isEqualTo("Ctrl+Alt+r");
        assertThat(ShortcutRegistry.get("menu.test.review.view")).isNotNull();
        // the item that only holds a sub menu is no action
        assertThat(ShortcutRegistry.get("menu.test.review")).isNull();

        ContextMenu menu = new ContextMenu(new Button());
        BuiltMenu built = definition.build(menu, loc, editor(new AtomicReference<>(false), new ArrayList<>()),
                () -> card(message("x", false), false, false, new ArrayList<>()));
        MenuItem review = built.get("test.review");
        assertThat(review.getSubMenu().getItems()).hasSize(2);
        assertThat(built.get("test.review.delete").isEnabled()).isFalse();
        reviewed[0] = true;
        built.refresh();
        assertThat(built.get("test.review.delete").isEnabled()).isTrue();
        ComponentUtil.fireEvent(built.get("test.review.view"), new ClickEvent<>(built.get("test.review.view")));
        assertThat(ran).containsExactly("view");

        registry.unregisterAll("test");
        assertThat(keys(registry.definition(MenuKind.CARD, false, null))).doesNotContain("test.review", "test.review.view");
        assertThat(ShortcutRegistry.get("menu.test.review.view")).isNull();
        assertThat(registry.find(StoryMenuKeys.EDIT)).isNotNull();
    }

    @Test
    void aPathMakesTheGroupsAndTheLastNameOfALiteralNameIsTheText() {
        registry.register("test", StoryMenuItem.tools("test.a", "Whatever/Is/Plugin", story -> {
        }));
        registry.register("test", StoryMenuItem.tools("test.b", l -> "Other", null).path("Whatever/Is"));
        registry.register("test", StoryMenuItem.tools("test.c", "Slash\\/Name/Leaf", story -> {
        }));
        registry.register("test", StoryMenuItem.tools("test.d", "Alone", story -> {
        }));

        MenuDefinition definition = registry.definition(MenuKind.TOOLS, false, null);
        MenuNode whatever = node(definition.getRoots(), "Whatever");
        assertThat(whatever.isGroup()).isTrue();
        assertThat(whatever.getText(loc, null, null)).as("a group that is not registered shows its name").isEqualTo("Whatever");
        MenuNode is = whatever.getChildren().getFirst();
        assertThat(is.getId()).isEqualTo("Whatever/Is");
        assertThat(ids(is.getChildren())).containsExactly("test.a", "test.b");
        assertThat(is.getChildren().getFirst().getText(loc, null, null)).isEqualTo("Plugin");
        // a slash that is written as \/ is a part of the name
        assertThat(node(definition.getRoots(), "Slash\\/Name").getText(loc, null, null)).isEqualTo("Slash/Name");
        assertThat(ids(definition.getRoots())).endsWith("Whatever", "Slash\\/Name", "test.d");
        assertThat(MenuPath.parse("a/ b \\/c/ /d")).containsExactly("a", "b /c", "d");
        assertThat(MenuPath.parse(null)).isEmpty();
    }

    @Test
    void aRegisteredGroupHasItsTextAndIconAndGoesWhereItWasRegistered() {
        registry.register("test", StoryMenuItem.tools("test.item", l -> "Item", story -> {
        }).path("test-group"));
        registry.group("test", MenuKind.TOOLS, "test-group", l -> "Translated").icon(com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid.STAR::create)
                .position(MenuPosition.first());

        MenuDefinition definition = registry.definition(MenuKind.TOOLS, false, null);
        assertThat(ids(definition.getRoots()).getFirst()).isEqualTo("test-group");
        ContextMenu menu = new ContextMenu(new Button());
        BuiltMenu built = definition.build(menu, loc, editor(new AtomicReference<>(false), new ArrayList<>()), () -> null);
        assertThat(textOf(built.getGroup("test-group"))).isEqualTo("Translated");
        assertThat(built.getGroup("test-group").getSubMenu().getItems()).hasSize(1);
        assertThat(built.get("test.item").isVisible()).isTrue();

        registry.unregisterAll("test");
        assertThat(ids(registry.definition(MenuKind.TOOLS, false, null).getRoots())).doesNotContain("test-group");
    }

    @Test
    void positionsOrderTheEntriesOfALevelWhateverTheOrderOfRegistration() {
        java.util.function.Function<String, StoryMenuItem> item = key -> StoryMenuItem.tools("test." + key, l -> key, story -> {
        }).path("test-pos");
        registry.register("test", item.apply("a"));
        // registered before the anchors they name
        registry.register("test", item.apply("d").position(MenuPosition.after("test.a")));
        registry.register("test", item.apply("e").position(MenuPosition.before("test.a")));
        registry.register("test", item.apply("f").position(MenuPosition.first()));
        registry.register("test", item.apply("g").position(MenuPosition.last()));
        registry.register("test", item.apply("b"));
        registry.register("test", item.apply("c"));
        registry.register("test", item.apply("h"));
        registry.register("test", item.apply("i").position(MenuPosition.after("test.nothing")));
        registry.register("test", item.apply("j").position(MenuPosition.after("test.a")));
        registry.register("test", item.apply("k").position(MenuPosition.index(3)));

        List<String> order = ids(node(registry.definition(MenuKind.TOOLS, false, null).getRoots(), "test-pos").getChildren());
        // first, then the ones before and after a (after it in the order of their registration), an index counts the entries without a
        // position (and the ones that are first), the one whose anchor is not there goes to the end, the last one after it
        assertThat(order).containsExactly("test.f", "test.e", "test.a", "test.d", "test.j", "test.b", "test.k", "test.c", "test.h",
                "test.i", "test.g");
    }

    @Test
    void anEntryCanBePlacedAfterAnEntryThatIsPlacedToo() {
        registry.register("test", StoryMenuItem.tools("test.x", l -> "x", story -> {
        }).path("test-chain").position(MenuPosition.after("test.y")));
        registry.register("test", StoryMenuItem.tools("test.y", l -> "y", story -> {
        }).path("test-chain").position(MenuPosition.after("test.z")));
        registry.register("test", StoryMenuItem.tools("test.z", l -> "z", story -> {
        }).path("test-chain"));
        // two that ask for each other have no place of their own: they go to the end
        registry.register("test", StoryMenuItem.tools("test.p", l -> "p", story -> {
        }).path("test-chain").position(MenuPosition.after("test.q")));
        registry.register("test", StoryMenuItem.tools("test.q", l -> "q", story -> {
        }).path("test-chain").position(MenuPosition.after("test.p")));

        assertThat(ids(node(registry.definition(MenuKind.TOOLS, false, null).getRoots(), "test-chain").getChildren()))
                .containsExactly("test.z", "test.y", "test.x", "test.p", "test.q");
    }

    @Test
    void anExtensionFindsAPlaceWithTheEntriesOfALevelAndThePositionOfOne() {
        assertThat(registry.children(MenuKind.TOOLS, "")).extracting(StoryMenuRegistry.MenuEntry::id).containsExactly(
                StoryMenuKeys.SEARCH_REPLACE, StoryMenuKeys.GROUP_APPEARANCE, StoryMenuKeys.GROUP_ANALYSIS,
                StoryMenuKeys.GROUP_IMPORT_EXPORT, StoryMenuKeys.GROUP_CLEAN_UP, StoryMenuKeys.GROUP_LAYOUT);
        assertThat(registry.children(MenuKind.TOOLS, "")).extracting(StoryMenuRegistry.MenuEntry::group)
                .containsExactly(false, true, true, true, true, true);
        assertThat(registry.children(MenuKind.TOOLS, StoryMenuKeys.GROUP_IMPORT_EXPORT)).extracting(StoryMenuRegistry.MenuEntry::id)
                .containsExactly(StoryMenuKeys.IMPORT_ARCHIVE, StoryMenuKeys.EXPORT);
        assertThat(registry.children(MenuKind.TOOLS, "nothing")).isEmpty();
        assertThat(registry.positionOf(MenuKind.TOOLS, StoryMenuKeys.SEARCH_REPLACE)).isZero();
        assertThat(registry.positionOf(MenuKind.TOOLS, StoryMenuKeys.GROUP_ANALYSIS)).isEqualTo(2);
        assertThat(registry.positionOf(MenuKind.TOOLS, StoryMenuKeys.EXPORT)).isEqualTo(1);
        assertThat(registry.positionOf(MenuKind.TOOLS, StoryMenuKeys.DELETE)).as("the delete item is in the other menu").isEqualTo(-1);
        assertThat(registry.positionOf(MenuKind.CARD, StoryMenuKeys.DELETE)).isEqualTo(12);
        assertThat(registry.positionOf(MenuKind.TOOLS, "test.unknown")).isEqualTo(-1);
    }

    @Test
    void aGroupIsShownWhileOneOfItsEntriesIsAndALineOnlyBetweenShownEntries() {
        boolean[] shown = {true, true, true};
        registry.register("test", StoryMenuItem.tools("test.x", l -> "x", story -> {
        }).path("test-sep").separatorAfter().visibleWhen((part, card) -> shown[0]));
        registry.register("test", StoryMenuItem.tools("test.y", l -> "y", story -> {
        }).path("test-sep").separatorAfter().visibleWhen((part, card) -> shown[1]));
        registry.register("test", StoryMenuItem.tools("test.z", l -> "z", story -> {
        }).path("test-sep").separatorAfter().visibleWhen((part, card) -> shown[2]));

        ContextMenu menu = new ContextMenu(new Button());
        BuiltMenu built = registry.definition(MenuKind.TOOLS, false, null)
                .build(menu, loc, editor(new AtomicReference<>(false), new ArrayList<>()), () -> null);
        MenuItem group = built.getGroup("test-sep");
        java.util.function.Supplier<List<Boolean>> lines = () -> group.getSubMenu().getChildren().filter(c -> c instanceof com.vaadin.flow.component.html.Hr)
                .map(Component::isVisible).collect(Collectors.toList());

        assertThat(group.getSubMenu().getChildren().map(c -> c instanceof MenuItem ? "item" : "line").collect(Collectors.toList()))
                .containsExactly("item", "line", "item", "line", "item", "line");
        assertThat(group.isVisible()).isTrue();
        assertThat(lines.get()).as("none after the last shown entry").containsExactly(true, true, false);

        shown[1] = false;
        built.refresh();
        assertThat(lines.get()).as("none after an entry that is not shown").containsExactly(true, false, false);

        shown[2] = false;
        built.refresh();
        assertThat(lines.get()).as("none at the end of the menu").containsExactly(false, false, false);
        assertThat(group.isVisible()).isTrue();

        shown[0] = false;
        built.refresh();
        assertThat(group.isVisible()).as("no entry shown, no group").isFalse();
    }

    @Test
    void aMenuIsRefreshedWithADefinitionThatWasAskedLaterAndKeepsItsShape() {
        ContextMenu menu = new ContextMenu(new Button());
        BuiltMenu built = registry.definition(MenuKind.TOOLS, false, KeyBindings.of(null))
                .build(menu, loc, editor(new AtomicReference<>(false), new ArrayList<>()), () -> null);
        List<String> before = menu.getItems().stream().map(MenuItem::getText).collect(Collectors.toList());

        // the story tab asks again when it is entered (the user may have changed the keys); an extension registered since is not in the menu
        UserSetting setting = new UserSetting();
        JsonObject stored = new JsonObject();
        stored.addProperty(ShortcutActions.STORY_SEARCH_REPLACE, "Ctrl+Alt+h");
        setting.setKeyBindings(stored);
        registry.register("test", StoryMenuItem.tools("test.late", l -> "Late", story -> {
        }).path(StoryMenuKeys.GROUP_ANALYSIS));
        built.refresh(registry.definition(MenuKind.TOOLS, false, KeyBindings.of(setting)));

        assertThat(keyOf(built.get(StoryMenuKeys.SEARCH_REPLACE))).isEqualTo("Ctrl+Alt+h");
        assertThat(built.get("test.late")).isNull();
        assertThat(menu.getItems().stream().map(MenuItem::getText).collect(Collectors.toList())).isEqualTo(before);
    }

    @Test
    void theKeyBindingsNameAnItemWithTheGroupsItIsIn() {
        assertThat(ShortcutRegistry.get("menu." + StoryMenuKeys.STATISTICS).getLabel(loc))
                .isEqualTo(loc.getValue(L.LABEL_ANALYSIS) + " \u203A " + loc.getValue(L.LABEL_STATISTICS));
        assertThat(ShortcutRegistry.get("menu." + StoryMenuKeys.CHANGE_STYLES)).as("it holds a sub menu, it is no action").isNull();
        assertThat(ShortcutRegistry.get("menu." + StoryMenuKeys.EXPORT).getLabel(loc))
                .isEqualTo(loc.getValue(L.LABEL_IMPORT_AND_EXPORT) + " \u203A " + loc.getValue(L.LABEL_EXPORT_STORY));
    }
}
