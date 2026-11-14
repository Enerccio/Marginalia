package com.github.enerccio.marginalia.ui.menu;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.story.BookHistoryActions;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryCard;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryEditor;
import com.github.enerccio.marginalia.ui.shortcuts.KeyBindings;
import com.github.enerccio.marginalia.ui.shortcuts.ShortcutAction;
import com.github.enerccio.marginalia.ui.shortcuts.ShortcutActions;
import com.github.enerccio.marginalia.ui.shortcuts.ShortcutRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The items of the menus of the story editor ({@link MenuKind}: the settings menu, the menu of a part and the right click menu of its
 * text). The application registers its own when it starts ({@link #afterPropertiesSet}), an extension registers its items when it is
 * loaded and unregisters them in {@code onExtensionUnload} (a bundle that is unloaded loses the rest by its name). The story tabs ask
 * for a {@link MenuDefinition} of a kind and build their menu from it, so a menu has the items that are registered when it is built.
 * <p>
 * Every item that has an action is also an action of the key bindings of the user (see {@link ShortcutRegistry}): it is listed in the
 * settings with the key it was registered with, none for a new item, and the menus show the key the user has.
 * <p>
 * The shape of a menu is made of the {@link StoryMenuItem#path paths} of the items: the groups on the path are sub menus (made when a
 * menu is built, shared by everyone who names them, {@link #group} gives one a translated text and an icon), and the
 * {@link StoryMenuItem#position positions} order the entries of a level. {@link #children} and {@link #positionOf} tell where things
 * are now, for an extension that wants its item somewhere.
 */
public class StoryMenuRegistry implements InitializingBean {

    private static final String APPLICATION = "marginalia";

    private static final String PATH_SEPARATOR = " \u203A ";

    private final Map<String, StoryMenuItem> items = new LinkedHashMap<>();
    private final Map<String, String> owners = new HashMap<>();
    private final Map<String, Long> itemOrder = new HashMap<>();
    private final Map<String, MenuGroup> groups = new HashMap<>();
    private final Map<String, String> groupOwners = new HashMap<>();
    private final Map<String, Long> groupOrder = new HashMap<>();
    private long sequence;

    @Override
    public void afterPropertiesSet() {
        registerToolsItems();
        registerCardItems();
        registerTextItems();
    }

    /**
     * Adds the item (one with the same key is replaced); a sub menu item comes after its parent. An item that has an action becomes an
     * action of the key bindings if its action id is not one yet.
     *
     * @param owner who registers it, the symbolic name of the bundle of an extension; see {@link #unregisterAll}
     */
    public synchronized void register(String owner, StoryMenuItem item) {
        StoryMenuItem replaced = items.put(item.getKey(), item);
        if (replaced != null && replaced != item) {
            replaced.retire();
        }
        owners.put(item.getKey(), owner);
        // a new item goes after the ones registered before, one that is replaced keeps its place
        itemOrder.computeIfAbsent(item.getKey(), key -> sequence++);
        String actionId = item.getActionId();
        if (item.hasAction() && actionId != null && ShortcutRegistry.get(actionId) == null) {
            ShortcutRegistry.register(new ShortcutAction(actionId, loc -> qualifiedLabel(item, loc), item.getDefaultKey(), null));
        }
    }

    /**
     * Registers the text, the icon and the place of a group, the sub menu that the paths of the items make (one with the same path is
     * replaced). It needs no registration: a group without one shows the last name of its path and goes where its first item does.
     * Everyone who gives the same path shares the group.
     *
     * @param owner who registers it, see {@link #unregisterAll}
     * @param path  the names of the group and of the groups it is in, outermost first, separated by {@code /}
     */
    public synchronized MenuGroup group(String owner, MenuKind kind, String path, Function<Localization, String> label) {
        String normalized = MenuPath.join(MenuPath.parse(path));
        MenuGroup group = new MenuGroup(kind, normalized, label);
        String id = groupId(kind, normalized);
        groups.put(id, group);
        groupOwners.put(id, owner);
        groupOrder.computeIfAbsent(id, key -> sequence++);
        return group;
    }

    public synchronized MenuGroup group(String owner, MenuKind kind, String path, L label) {
        return group(owner, kind, path, loc -> loc.getValue(label));
    }

    private static String groupId(MenuKind kind, String path) {
        return kind.name() + ":" + path;
    }

    // "Analysis > Statistics": the name of the action in the key bindings of the user, with the groups it is in
    private synchronized String qualifiedLabel(StoryMenuItem item, Localization loc) {
        List<String> names = item.getParent() == null ? item.getPath() : List.of();
        StringBuilder label = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            MenuGroup group = groups.get(groupId(item.getKind(), MenuPath.join(names, i + 1)));
            String text = group == null ? null : group.getText(loc);
            label.append(text == null ? names.get(i) : text).append(PATH_SEPARATOR);
        }
        return label.append(item.getLabel(loc)).toString();
    }

    public synchronized void unregister(String key) {
        StoryMenuItem removed = items.remove(key);
        owners.remove(key);
        itemOrder.remove(key);
        if (removed != null) {
            // the menus built with it hide it and it runs nothing
            removed.retire();
        }
        if (removed != null && removed.getActionId() != null
                && items.values().stream().noneMatch(other -> removed.getActionId().equals(other.getActionId()))) {
            ShortcutRegistry.unregister(removed.getActionId());
        }
    }

    /**
     * Removes the items an owner registered.
     */
    public synchronized void unregisterAll(String owner) {
        new ArrayList<>(owners.entrySet()).stream().filter(entry -> Objects.equals(owner, entry.getValue()))
                .map(Map.Entry::getKey).forEach(this::unregister);
        new ArrayList<>(groupOwners.entrySet()).stream().filter(entry -> Objects.equals(owner, entry.getValue()))
                .map(Map.Entry::getKey).forEach(id -> {
                    groups.remove(id);
                    groupOwners.remove(id);
                    groupOrder.remove(id);
                });
    }

    public synchronized StoryMenuItem find(String key) {
        return items.get(key);
    }

    /**
     * The first item whose action has this id, null when none or when an action of the application has no item.
     */
    public synchronized StoryMenuItem findByAction(String actionId) {
        return items.values().stream().filter(item -> item.hasAction() && Objects.equals(actionId, item.getActionId()))
                .findFirst().orElse(null);
    }

    /**
     * The menu of the kind as it is now.
     *
     * @param phone    whether it is for the phone, which leaves out the items that are desktop only
     * @param bindings the keys of the user, shown with the items; null when the menu shows no keys (the phone has no keyboard)
     */
    public synchronized MenuDefinition definition(MenuKind kind, boolean phone, KeyBindings bindings) {
        return new MenuDefinition(kind, tree(kind, phone), bindings);
    }

    /**
     * The entries of a level of the menu of the kind in the order they are shown, whatever their state: the items and the groups
     * directly in the group with this path (blank for the top level), each with its id (the key of the item or the path of the group)
     * and its index. For an extension that looks for a place for its own item, see {@link MenuPosition}.
     *
     * @param path the path of the group, empty for the top level; an item that holds a sub menu is named by its key
     */
    public synchronized List<MenuEntry> children(MenuKind kind, String path) {
        List<MenuNode> level = tree(kind, false);
        String wanted = MenuPath.join(MenuPath.parse(path));
        if (!wanted.isEmpty()) {
            MenuNode holder = find(level, wanted);
            if (holder == null) {
                return List.of();
            }
            level = holder.getChildren();
        }
        List<MenuEntry> entries = new ArrayList<>();
        for (int i = 0; i < level.size(); i++) {
            entries.add(new MenuEntry(level.get(i).getId(), level.get(i).isGroup(), i));
        }
        return entries;
    }

    /**
     * The index of the item or the group among the entries of its level, -1 when the menu has none.
     *
     * @param id the key of the item or the path of the group
     */
    public synchronized int positionOf(MenuKind kind, String id) {
        List<MenuNode> level = levelOf(tree(kind, false), id);
        if (level == null) {
            return -1;
        }
        for (int i = 0; i < level.size(); i++) {
            if (level.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * An entry of a level of a menu.
     *
     * @param id    the key of the item or the path of the group
     * @param group whether it is a group
     * @param index its place among the entries of the level
     */
    public record MenuEntry(String id, boolean group, int index) {
    }

    private static MenuNode find(List<MenuNode> nodes, String id) {
        for (MenuNode node : nodes) {
            if (node.getId().equals(id)) {
                return node;
            }
            MenuNode inside = find(node.getChildren(), id);
            if (inside != null) {
                return inside;
            }
        }
        return null;
    }

    private static List<MenuNode> levelOf(List<MenuNode> nodes, String id) {
        for (MenuNode node : nodes) {
            if (node.getId().equals(id)) {
                return nodes;
            }
            List<MenuNode> inside = levelOf(node.getChildren(), id);
            if (inside != null) {
                return inside;
            }
        }
        return null;
    }

    /**
     * Makes the tree of the menu: a node for every item of the kind, the groups of their paths, the sub menu of the items that are
     * parents, and the order of every level.
     */
    private List<MenuNode> tree(MenuKind kind, boolean phone) {
        Map<String, MenuNode> nodes = new LinkedHashMap<>();
        for (StoryMenuItem item : items.values()) {
            if (item.getKind() == kind && (!phone || item.isPhone())) {
                nodes.put(item.getKey(), new MenuNode(item, itemOrder.getOrDefault(item.getKey(), 0L)));
            }
        }
        List<MenuNode> roots = new ArrayList<>();
        Map<String, MenuNode> made = new HashMap<>();
        for (MenuNode node : nodes.values()) {
            StoryMenuItem item = node.getItem();
            if (item.getParent() != null) {
                // the sub menu of a parent that is not there has no place
                MenuNode parent = nodes.get(item.getParent());
                if (parent != null && parent != node) {
                    parent.getChildren().add(node);
                }
                continue;
            }
            List<MenuNode> level = groupLevel(kind, phone, item.getPath(), roots, made);
            if (level != null) {
                level.add(node);
            }
        }
        // what hangs under a parent that is itself lost (a cycle) is not reachable from the roots and is gone with it
        prune(roots);
        order(kind, roots);
        return roots;
    }

    // the level of the group of the path, making the groups on the way; null for a group that the phone does not show
    private List<MenuNode> groupLevel(MenuKind kind, boolean phone, List<String> names, List<MenuNode> roots, Map<String, MenuNode> made) {
        List<MenuNode> level = roots;
        for (int i = 0; i < names.size(); i++) {
            String path = MenuPath.join(names, i + 1);
            MenuNode group = made.get(path);
            if (group == null) {
                MenuGroup registered = groups.get(groupId(kind, path));
                if (phone && registered != null && !registered.isPhone()) {
                    return null;
                }
                group = new MenuNode(path, names.get(i), registered);
                made.put(path, group);
                level.add(group);
            }
            level = group.getChildren();
        }
        return level;
    }

    // the groups with nothing in them are no menu
    private static void prune(List<MenuNode> nodes) {
        for (MenuNode node : new ArrayList<>(nodes)) {
            prune(node.getChildren());
            if (node.isGroup() && node.getChildren().isEmpty()) {
                nodes.remove(node);
            }
        }
    }

    // a group goes where it was registered or where its first entry goes
    private long orderOf(MenuKind kind, MenuNode node) {
        if (!node.isGroup()) {
            return node.order();
        }
        long order = groupOrder.getOrDefault(groupId(kind, node.getId()), Long.MAX_VALUE);
        for (MenuNode child : node.getChildren()) {
            order = Math.min(order, orderOf(kind, child));
        }
        return order;
    }

    private void order(MenuKind kind, List<MenuNode> level) {
        for (MenuNode node : level) {
            node.order(orderOf(kind, node));
            order(kind, node.getChildren());
        }
        List<MenuNode> unplaced = level.stream().filter(node -> node.position().type() == MenuPosition.Type.DEFAULT)
                .sorted(Comparator.comparingLong(MenuNode::order)).collect(Collectors.toList());
        List<MenuNode> placed = level.stream().filter(node -> node.position().type() != MenuPosition.Type.DEFAULT)
                .sorted(Comparator.comparingLong(MenuNode::order)).toList();
        List<MenuNode> result = new ArrayList<>(unplaced);
        List<MenuNode> last = new ArrayList<>();
        List<MenuNode> relative = new ArrayList<>();
        int first = 0;
        for (MenuNode node : placed) {
            MenuPosition position = node.position();
            switch (position.type()) {
                case FIRST -> result.add(first++, node);
                case LAST -> last.add(node);
                case INDEX -> result.add(Math.min(position.index(), result.size()), node);
                default -> relative.add(node);
            }
        }
        // before / after an entry: the anchor may itself be one that is placed (an entry after an entry after another), so go on while
        // something finds its anchor
        Map<String, Integer> after = new HashMap<>();
        boolean progress = true;
        while (!relative.isEmpty() && progress) {
            progress = false;
            for (MenuNode node : new ArrayList<>(relative)) {
                String anchor = node.position().anchor();
                int at = indexOf(result, anchor);
                if (at < 0) {
                    continue;
                }
                if (node.position().type() == MenuPosition.Type.BEFORE) {
                    result.add(at, node);
                } else {
                    int offset = after.merge(anchor, 1, Integer::sum);
                    result.add(at + offset, node);
                }
                relative.remove(node);
                progress = true;
            }
        }
        // an anchor that is not there
        result.addAll(relative);
        result.addAll(last);
        level.clear();
        level.addAll(result);
    }

    private static int indexOf(List<MenuNode> nodes, String id) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private void application(StoryMenuItem item) {
        register(APPLICATION, item);
    }

    private static boolean hasText(StoryCard card) {
        return card != null && card.getMessage() != null && card.getMessage().getId() != null
                && StringUtils.isNotBlank(card.getMessage().getResponse());
    }

    private static boolean hasSummary(StoryCard card) {
        return card != null && card.getMessage() != null && card.getMessage().getSummary() != null;
    }

    private void registerToolsItems() {
        application(StoryMenuItem.tools(StoryMenuKeys.SEARCH_REPLACE, L.LABEL_SEARCH_AND_REPLACE, StoryEditor::openSearchReplaceDialog)
                .enabledWhen((part, card) -> notGenerating(part)).icon(Solid.EXCHANGE_ALT::create).actionId(ShortcutActions.STORY_SEARCH_REPLACE).defaultKey("Ctrl+Shift+h")
                .separatorAfter());

        group(APPLICATION, MenuKind.TOOLS, StoryMenuKeys.GROUP_APPEARANCE, L.LABEL_APPEARANCE).icon(Solid.PALETTE::create);
        group(APPLICATION, MenuKind.TOOLS, StoryMenuKeys.GROUP_ANALYSIS, L.LABEL_ANALYSIS).icon(Solid.MAGNIFYING_GLASS_CHART::create);
        group(APPLICATION, MenuKind.TOOLS, StoryMenuKeys.GROUP_IMPORT_EXPORT, L.LABEL_IMPORT_AND_EXPORT)
                .icon(Solid.ARROW_RIGHT_ARROW_LEFT::create).separatorAfter();
        group(APPLICATION, MenuKind.TOOLS, StoryMenuKeys.GROUP_CLEAN_UP, L.LABEL_CLEANUP).icon(Solid.BROOM::create);
        group(APPLICATION, MenuKind.TOOLS, StoryMenuKeys.GROUP_LAYOUT, L.LABEL_LAYOUT).icon(Solid.TABLE_COLUMNS::create).phone(false);

        application(StoryMenuItem.tools(StoryMenuKeys.CHANGE_STYLES, L.LABEL_CHANGE_STYLES, null)
                .icon(Solid.PEN_FANCY::create).choices(StoryEditor::getPartStyleChoices).path(StoryMenuKeys.GROUP_APPEARANCE));
        application(StoryMenuItem.tools(StoryMenuKeys.HIGHLIGHT_GENERATED, L.LABEL_HIGHLIGHT_GENERATED_TEXT,
                        part -> part.setShowGeneratedDiff(!part.isShowGeneratedDiff()))
                .icon(Solid.HIGHLIGHTER::create).checkedWhen(StoryEditor::isShowGeneratedDiff)
                .path(StoryMenuKeys.GROUP_APPEARANCE));
        application(StoryMenuItem.tools(StoryMenuKeys.ANNOTATIONS_IN_MARGIN, L.LABEL_ANNOTATIONS_IN_MARGIN,
                        part -> part.setAnnotationsInMargin(!part.isAnnotationsInMargin()))
                .icon(Solid.COMMENT_ALT::create).checkedWhen(StoryEditor::isAnnotationsInMargin).phone(false)
                .path(StoryMenuKeys.GROUP_APPEARANCE));

        application(StoryMenuItem.tools(StoryMenuKeys.ANNOTATE_RANGE, L.LABEL_ANNOTATE_BATCH, StoryEditor::openAnnotateRangeDialog)
                .icon(Solid.COMMENT_DOTS::create).path(StoryMenuKeys.GROUP_ANALYSIS));
        application(StoryMenuItem.tools(StoryMenuKeys.CREATE_SUMMARIES, L.LABEL_CREATE_SUMMARIES, StoryEditor::openCreateSummariesDialog)
                .icon(Solid.ALIGN_LEFT::create).path(StoryMenuKeys.GROUP_ANALYSIS));
        application(StoryMenuItem.tools(StoryMenuKeys.LORE_NOTES, L.LABEL_TAKE_LORE_NOTES, StoryEditor::openLoreNotesDialog)
                .icon(Solid.FILE_ALT::create).path(StoryMenuKeys.GROUP_ANALYSIS));
        application(StoryMenuItem.tools(StoryMenuKeys.AUTO_ANNOTATE, L.LABEL_AUTO_ANNOTATE, StoryEditor::openAutoAnnotateDialog)
                .icon(Solid.ROBOT::create).checkedWhen(StoryEditor::isAutoAnnotate).path(StoryMenuKeys.GROUP_ANALYSIS));
        application(StoryMenuItem.tools(StoryMenuKeys.BRANCH_TOKENS, L.LABEL_BRANCH_TOKENS, StoryEditor::openBranchTokensDialog)
                .icon(Solid.CHART_BAR::create).path(StoryMenuKeys.GROUP_ANALYSIS));
        application(StoryMenuItem.tools(StoryMenuKeys.LORE_USAGE, L.LABEL_LORE_USAGE, StoryEditor::openLoreUsageDialog)
                .icon(Solid.BOOK_OPEN::create).path(StoryMenuKeys.GROUP_ANALYSIS));
        application(StoryMenuItem.tools(StoryMenuKeys.STATISTICS, L.LABEL_STATISTICS, StoryEditor::openStatisticsDialog)
                .icon(Solid.CHART_LINE::create).path(StoryMenuKeys.GROUP_ANALYSIS));

        application(StoryMenuItem.tools(StoryMenuKeys.IMPORT_ARCHIVE, L.LABEL_IMPORT, StoryEditor::openArchiveImportDialog)
                .enabledWhen((part, card) -> notGenerating(part)).icon(Solid.FILE_IMPORT::create).path(StoryMenuKeys.GROUP_IMPORT_EXPORT));
        application(StoryMenuItem.tools(StoryMenuKeys.EXPORT, L.LABEL_EXPORT_STORY, StoryEditor::openExportDialog)
                .icon(Solid.FILE_EXPORT::create).path(StoryMenuKeys.GROUP_IMPORT_EXPORT));

        application(StoryMenuItem.tools(StoryMenuKeys.CLEAR_BOOK_HISTORY, L.LABEL_CLEAR_BOOK_HISTORY,
                        part -> new BookHistoryActions(part.getCurrentManuscript()).clear())
                .icon(Solid.HISTORY::create).path(StoryMenuKeys.GROUP_CLEAN_UP));
        application(StoryMenuItem.tools(StoryMenuKeys.EMPTY_HISTORY_TRASH, L.LABEL_EMPTY_BOOK_HISTORY_TRASH,
                        part -> new BookHistoryActions(part.getCurrentManuscript()).emptyTrash())
                .icon(Solid.TRASH_RESTORE::create).path(StoryMenuKeys.GROUP_CLEAN_UP));
        application(StoryMenuItem.tools(StoryMenuKeys.MASS_DELETE, L.LABEL_MASS_DELETE, StoryEditor::toggleMassDelete)
                .enabledWhen((part, card) -> notGenerating(part)).icon(Solid.TRASH::create).checkedWhen(StoryEditor::isMassDelete).path(StoryMenuKeys.GROUP_CLEAN_UP));

        application(StoryMenuItem.tools(StoryMenuKeys.SAVE_LAYOUT_AS_DEFAULT, L.LABEL_SAVE_LAYOUT_AS_DEFAULT, StoryEditor::saveLayoutAsDefault)
                .icon(Solid.TABLE_COLUMNS::create).phone(false).path(StoryMenuKeys.GROUP_LAYOUT));
        application(StoryMenuItem.tools(StoryMenuKeys.RESTORE_LAYOUT, L.LABEL_RESTORE_DEFAULT_LAYOUT, StoryEditor::confirmRestoreDefaultLayout)
                .icon(Solid.TABLE_COLUMNS::create).phone(false).path(StoryMenuKeys.GROUP_LAYOUT));
    }

    private void registerCardItems() {
        // a group goes where it is registered: after the items above it
        application(StoryMenuItem.card(StoryMenuKeys.EDIT, L.LABEL_EDIT, (part, card) -> card.toggleEdit())
                .contextLabel((loc, part, card) -> loc.getValue(card != null && card.isEditing() ? L.LABEL_SAVE : L.LABEL_EDIT))
                .hintAction((part, card) -> card != null && card.isEditing() ? ShortcutActions.STORY_SAVE_EDIT : ShortcutActions.STORY_EDIT)
                .actionId(ShortcutActions.STORY_EDIT).defaultKey("e"));
        application(StoryMenuItem.card(StoryMenuKeys.DISCARD, L.LABEL_DISCARD_CHANGES, (part, card) -> card.discardEdit())
                .visibleWhen((part, card) -> card != null && card.isEditing()));
        application(StoryMenuItem.card(StoryMenuKeys.REGENERATE, L.LABEL_REGENERATE, (part, card) -> card.regenerate())
                .visibleWhen((part, card) -> card != null && card.isLast()).enabledWhen((part, card) -> notBusy(card))
                .actionId(ShortcutActions.STORY_REGENERATE).defaultKey("r"));
        application(StoryMenuItem.card(StoryMenuKeys.SWIPE, L.LABEL_SWIPE, (part, card) -> card.swipe())
                .visibleWhen((part, card) -> card != null && card.isLast()).enabledWhen((part, card) -> notBusy(card))
                .actionId(ShortcutActions.STORY_SWIPE).defaultKey("s"));
        // the text that is redrafted is the saved one
        application(StoryMenuItem.card(StoryMenuKeys.REDRAFT, L.LABEL_REDRAFT, (part, card) -> card.redraft())
                .visibleWhen((part, card) -> card != null && !card.isEditing() && StringUtils.isNotBlank(card.getMessage().getResponse()))
                .enabledWhen((part, card) -> notBusy(card)));
        application(StoryMenuItem.card(StoryMenuKeys.HISTORY, L.LABEL_HISTORY, (part, card) -> card.openHistory())
                .contextLabel((loc, part, card) -> card == null || card.getHistoryCount() == 0 ? loc.getValue(L.LABEL_HISTORY)
                        : String.format(loc.getValue(L.LABEL_HISTORY_WITH_COUNT), card.getHistoryCount()))
                .visibleWhen((part, card) -> card != null && !card.isEditing() && card.getMessage().getId() != null));
        application(StoryMenuItem.card(StoryMenuKeys.IMAGES, L.LABEL_IMAGES, (part, card) -> card.openImagesDialog()));
        application(StoryMenuItem.card(StoryMenuKeys.IMAGE_PREVIEW, L.LABEL_PREVIEW_AS_VIEWER, (part, card) -> card.openImagePreview())
                .visibleWhen((part, card) -> card != null && !card.getMessage().getImages().isEmpty()).separatorAfter());

        group(APPLICATION, MenuKind.CARD, StoryMenuKeys.GROUP_ANNOTATE, L.LABEL_ANNOTATIONS);
        group(APPLICATION, MenuKind.CARD, StoryMenuKeys.GROUP_TRANSLATE, L.LABEL_TRANSLATE).icon(Solid.LANGUAGE::create);
        application(StoryMenuItem.card(StoryMenuKeys.ANNOTATE_PART, L.LABEL_ANNOTATE_PART, (part, card) -> card.annotatePart())
                .visibleWhen((part, card) -> card != null && card.getMessage().getId() != null).path(StoryMenuKeys.GROUP_ANNOTATE));
        application(StoryMenuItem.card(StoryMenuKeys.ANNOTATE_WITH_AI, L.LABEL_ANNOTATE_WITH_AI, (part, card) -> card.annotateWithAi())
                .visibleWhen((part, card) -> hasText(card) && !card.isEditing()).enabledWhen((part, card) -> notBusy(card))
                .path(StoryMenuKeys.GROUP_ANNOTATE));

        group(APPLICATION, MenuKind.CARD, StoryMenuKeys.GROUP_SUMMARY, L.LABEL_SUMMARY);
        application(StoryMenuItem.card(StoryMenuKeys.SUMMARY, L.LABEL_GENERATE_SUMMARY, (part, card) -> card.openSummaryDialog())
                .contextLabel((loc, part, card) -> loc.getValue(hasSummary(card) ? L.LABEL_SHOW_SUMMARY : L.LABEL_GENERATE_SUMMARY))
                .path(StoryMenuKeys.GROUP_SUMMARY));
        application(StoryMenuItem.card(StoryMenuKeys.DELETE_SUMMARY, L.LABEL_DELETE_SUMMARY, (part, card) -> card.confirmDeleteSummary())
                .enabledWhen((part, card) -> hasSummary(card)).path(StoryMenuKeys.GROUP_SUMMARY));

        group(APPLICATION, MenuKind.CARD, StoryMenuKeys.GROUP_INSPECT, L.LABEL_INSPECT);
        application(StoryMenuItem.card(StoryMenuKeys.SHOW_PROMPT, L.LABEL_SHOW_PROMPT, (part, card) -> card.showPrompt())
                .path(StoryMenuKeys.GROUP_INSPECT));
        application(StoryMenuItem.card(StoryMenuKeys.COMPARE_PROMPT, L.LABEL_COMPARE_PROMPT, (part, card) -> card.comparePrompt())
                .path(StoryMenuKeys.GROUP_INSPECT));
        application(StoryMenuItem.card(StoryMenuKeys.TOKEN_USAGE, L.LABEL_TOKEN_USAGE, (part, card) -> card.showTokenUsage())
                .path(StoryMenuKeys.GROUP_INSPECT));
        application(StoryMenuItem.card(StoryMenuKeys.CARD_LORE_USAGE, L.LABEL_LORE_USAGE, (part, card) -> card.showLoreUsage())
                .path(StoryMenuKeys.GROUP_INSPECT));

        group(APPLICATION, MenuKind.CARD, StoryMenuKeys.GROUP_BRANCH_MERGE, L.LABEL_BRANCH_AND_MERGE).separatorAfter();
        application(StoryMenuItem.card(StoryMenuKeys.BRANCH, L.LABEL_BRANCH_STORY, (part, card) -> card.branch())
                .enabledWhen((part, card) -> notKeptInPlace(card)).path(StoryMenuKeys.GROUP_BRANCH_MERGE));
        application(StoryMenuItem.card(StoryMenuKeys.MERGE, L.LABEL_MERGE_INTO_PREVIOUS, (part, card) -> card.mergeIntoPrevious())
                .visibleWhen((part, card) -> card != null && card.getMessage().getParent() != null)
                .enabledWhen((part, card) -> notBusy(card) && notKeptInPlace(card)).path(StoryMenuKeys.GROUP_BRANCH_MERGE));

        // the last, whatever extensions add
        application(StoryMenuItem.card(StoryMenuKeys.DELETE, L.LABEL_DELETE, (part, card) -> card.confirmDelete())
                .enabledWhen((part, card) -> notBusy(card) && notKeptInPlace(card)).position(MenuPosition.last()));
    }

    /**
     * The actions that would take the part away from a window of the model that works on it (or start a second one) are off while
     * the window is there, see {@link StoryCard#getBusyWith()}.
     */
    private static boolean notBusy(StoryCard card) {
        return card == null || card.getBusyWith() == null;
    }

    /**
     * What works over the whole book (search and replace, an import into it, the mass delete) waits while a part of it is generated.
     */
    private static boolean notGenerating(StoryEditor part) {
        return part == null || part.getCurrentManuscript() == null || Runs.generationOf(part.getCurrentManuscript().getId()) == null;
    }

    /**
     * A generation continues the branch of the part: it stays where it is, see {@link StoryCard#isKeptInPlace()}.
     */
    private static boolean notKeptInPlace(StoryCard card) {
        return card == null || !card.isKeptInPlace();
    }

    private void registerTextItems() {
        // the translations of the extensions; the group is hidden while nothing is in it
        group(APPLICATION, MenuKind.TEXT, StoryMenuKeys.GROUP_TRANSLATE, L.LABEL_TRANSLATE).icon(Solid.LANGUAGE::create);
        // only here, not in the menu of the part: opening that menu takes the selection away
        application(StoryMenuItem.text(StoryMenuKeys.TEXT_ANNOTATE_SELECTION, L.LABEL_ANNOTATE_SELECTION, (part, card) -> card.annotateSelection())
                .actionId(ShortcutActions.STORY_ANNOTATE_SELECTION).defaultKey("a"));
        application(StoryMenuItem.text(StoryMenuKeys.TEXT_REDRAFT_SELECTION, L.LABEL_REDRAFT_SELECTION, (part, card) -> card.redraftSelection())
                .enabledWhen((part, card) -> notBusy(card)));
        application(StoryMenuItem.text(StoryMenuKeys.TEXT_COPY, L.LABEL_COPY, (part, card) -> card.copySelection()));
    }
}
