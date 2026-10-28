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
        assertThat(keys(registry.definition(MenuKind.TOOLS, false, null))).containsExactly(
                StoryMenuKeys.CHANGE_STYLES, StoryMenuKeys.HIGHLIGHT_GENERATED, StoryMenuKeys.ANNOTATIONS_IN_MARGIN,
                StoryMenuKeys.SEARCH_REPLACE, StoryMenuKeys.IMPORT_ARCHIVE, StoryMenuKeys.ANNOTATE_RANGE, StoryMenuKeys.BRANCH_TOKENS, StoryMenuKeys.EXPORT,
                StoryMenuKeys.RESTORE_LAYOUT);
        assertThat(keys(registry.definition(MenuKind.TOOLS, true, null))).containsExactly(
                StoryMenuKeys.CHANGE_STYLES, StoryMenuKeys.SEARCH_REPLACE, StoryMenuKeys.IMPORT_ARCHIVE, StoryMenuKeys.ANNOTATE_RANGE, StoryMenuKeys.BRANCH_TOKENS,
                StoryMenuKeys.EXPORT);
        assertThat(registry.definition(MenuKind.CARD, false, null).getItems()).hasSize(15);
        assertThat(keys(registry.definition(MenuKind.CARD, false, null))).startsWith(StoryMenuKeys.EDIT, StoryMenuKeys.DISCARD);
        assertThat(keys(registry.definition(MenuKind.TEXT, true, null))).containsExactly(
                StoryMenuKeys.TEXT_ANNOTATE_SELECTION, StoryMenuKeys.TEXT_REDRAFT_SELECTION, StoryMenuKeys.TEXT_COPY);
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
        StoryCard card = (StoryCard) Proxy.newProxyInstance(StoryCard.class.getClassLoader(), new Class<?>[]{StoryCard.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getMessage" -> message;
                    case "isEditing" -> editing[0];
                    case "isLast" -> last[0];
                    default -> {
                        calls.add(method.getName());
                        yield null;
                    }
                });
        MenuDefinition definition = registry.definition(MenuKind.CARD, false, KeyBindings.of(null));
        BuiltMenu built = definition.build(menu, loc, editor(new AtomicReference<>(false), calls), () -> card);

        assertThat(textOf(built.get(StoryMenuKeys.EDIT))).isEqualTo(loc.getValue(L.LABEL_EDIT));
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
}
