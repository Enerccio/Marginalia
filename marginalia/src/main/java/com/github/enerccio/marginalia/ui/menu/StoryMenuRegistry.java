package com.github.enerccio.marginalia.ui.menu;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.loc.L;
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

/**
 * The items of the menus of the story editor ({@link MenuKind}: the settings menu, the menu of a part and the right click menu of its
 * text). The application registers its own when it starts ({@link #afterPropertiesSet}), an extension registers its items when it is
 * loaded and unregisters them in {@code onExtensionUnload} (a bundle that is unloaded loses the rest by its name). The story tabs ask
 * for a {@link MenuDefinition} of a kind and build their menu from it, so a menu has the items that are registered when it is built.
 * <p>
 * Every item that has an action is also an action of the key bindings of the user (see {@link ShortcutRegistry}): it is listed in the
 * settings with the key it was registered with, none for a new item, and the menus show the key the user has.
 */
public class StoryMenuRegistry implements InitializingBean {

    private static final String APPLICATION = "marginalia";

    private final Map<String, StoryMenuItem> items = new LinkedHashMap<>();
    private final Map<String, String> owners = new HashMap<>();

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
        String actionId = item.getActionId();
        if (item.hasAction() && actionId != null && ShortcutRegistry.get(actionId) == null) {
            ShortcutRegistry.register(new ShortcutAction(actionId, item::getLabel, item.getDefaultKey(), null));
        }
    }

    public synchronized void unregister(String key) {
        StoryMenuItem removed = items.remove(key);
        owners.remove(key);
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
        List<StoryMenuItem> chosen = new ArrayList<>();
        Set<String> shown = new HashSet<>();
        for (StoryMenuItem item : items.values()) {
            if (item.getKind() != kind || (phone && !item.isPhone())) {
                continue;
            }
            // the sub menu of a parent that is not there has no place
            if (item.getParent() != null && !shown.contains(item.getParent())) {
                continue;
            }
            shown.add(item.getKey());
            chosen.add(item);
        }
        return new MenuDefinition(kind, chosen, bindings);
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
        application(StoryMenuItem.tools(StoryMenuKeys.CHANGE_STYLES, L.LABEL_CHANGE_STYLES, StoryEditor::toggleBookStyles)
                .icon(Solid.PEN_FANCY::create));
        application(StoryMenuItem.tools(StoryMenuKeys.HIGHLIGHT_GENERATED, L.LABEL_HIGHLIGHT_GENERATED_TEXT,
                        part -> part.setShowGeneratedDiff(!part.isShowGeneratedDiff()))
                .icon(Solid.HIGHLIGHTER::create).checkedWhen(StoryEditor::isShowGeneratedDiff).phone(false));
        application(StoryMenuItem.tools(StoryMenuKeys.ANNOTATIONS_IN_MARGIN, L.LABEL_ANNOTATIONS_IN_MARGIN,
                        part -> part.setAnnotationsInMargin(!part.isAnnotationsInMargin()))
                .icon(Solid.COMMENT_ALT::create).checkedWhen(StoryEditor::isAnnotationsInMargin).phone(false));
        application(StoryMenuItem.tools(StoryMenuKeys.SEARCH_REPLACE, L.LABEL_SEARCH_AND_REPLACE, StoryEditor::openSearchReplaceDialog)
                .icon(Solid.EXCHANGE_ALT::create).actionId(ShortcutActions.STORY_SEARCH_REPLACE).defaultKey("Ctrl+Shift+h"));
        application(StoryMenuItem.tools(StoryMenuKeys.IMPORT_ARCHIVE, L.LABEL_IMPORT_FROM_ARCHIVE, StoryEditor::openArchiveImportDialog)
                .icon(Solid.FILE_IMPORT::create));
        application(StoryMenuItem.tools(StoryMenuKeys.ANNOTATE_RANGE, L.LABEL_ANNOTATE_BATCH, StoryEditor::openAnnotateRangeDialog)
                .icon(Solid.COMMENT_DOTS::create));
        application(StoryMenuItem.tools(StoryMenuKeys.AUTO_ANNOTATE, L.LABEL_AUTO_ANNOTATE, StoryEditor::openAutoAnnotateDialog)
                .icon(Solid.ROBOT::create).checkedWhen(StoryEditor::isAutoAnnotate));
        application(StoryMenuItem.tools(StoryMenuKeys.BRANCH_TOKENS, L.LABEL_BRANCH_TOKENS, StoryEditor::openBranchTokensDialog)
                .icon(Solid.CHART_BAR::create));
        application(StoryMenuItem.tools(StoryMenuKeys.EXPORT, L.LABEL_EXPORT_STORY, StoryEditor::openExportDialog)
                .icon(Solid.FILE_EXPORT::create));
        application(StoryMenuItem.tools(StoryMenuKeys.CLEAR_BOOK_HISTORY, L.LABEL_CLEAR_BOOK_HISTORY,
                        part -> new BookHistoryActions(part.getCurrentManuscript()).clear())
                .icon(Solid.HISTORY::create));
        application(StoryMenuItem.tools(StoryMenuKeys.EMPTY_HISTORY_TRASH, L.LABEL_EMPTY_BOOK_HISTORY_TRASH,
                        part -> new BookHistoryActions(part.getCurrentManuscript()).emptyTrash())
                .icon(Solid.TRASH_RESTORE::create));
        application(StoryMenuItem.tools(StoryMenuKeys.MASS_DELETE, L.LABEL_MASS_DELETE, StoryEditor::toggleMassDelete)
                .icon(Solid.TRASH::create).checkedWhen(StoryEditor::isMassDelete));
        application(StoryMenuItem.tools(StoryMenuKeys.SAVE_LAYOUT_AS_DEFAULT, L.LABEL_SAVE_LAYOUT_AS_DEFAULT, StoryEditor::saveLayoutAsDefault)
                .icon(Solid.TABLE_COLUMNS::create).phone(false));
        application(StoryMenuItem.tools(StoryMenuKeys.RESTORE_LAYOUT, L.LABEL_RESTORE_DEFAULT_LAYOUT, StoryEditor::confirmRestoreDefaultLayout)
                .icon(Solid.TABLE_COLUMNS::create).phone(false));
    }

    private void registerCardItems() {
        application(StoryMenuItem.card(StoryMenuKeys.EDIT, L.LABEL_EDIT, (part, card) -> card.toggleEdit())
                .contextLabel((loc, part, card) -> loc.getValue(card != null && card.isEditing() ? L.LABEL_SAVE : L.LABEL_EDIT))
                .hintAction((part, card) -> card != null && card.isEditing() ? ShortcutActions.STORY_SAVE_EDIT : ShortcutActions.STORY_EDIT)
                .actionId(ShortcutActions.STORY_EDIT).defaultKey("e"));
        application(StoryMenuItem.card(StoryMenuKeys.DISCARD, L.LABEL_DISCARD_CHANGES, (part, card) -> card.discardEdit())
                .visibleWhen((part, card) -> card != null && card.isEditing()));
        application(StoryMenuItem.card(StoryMenuKeys.REGENERATE, L.LABEL_REGENERATE, (part, card) -> card.regenerate())
                .visibleWhen((part, card) -> card != null && card.isLast())
                .actionId(ShortcutActions.STORY_REGENERATE).defaultKey("r"));
        application(StoryMenuItem.card(StoryMenuKeys.SWIPE, L.LABEL_SWIPE, (part, card) -> card.swipe())
                .visibleWhen((part, card) -> card != null && card.isLast())
                .actionId(ShortcutActions.STORY_SWIPE).defaultKey("s"));
        // the text that is redrafted is the saved one
        application(StoryMenuItem.card(StoryMenuKeys.REDRAFT, L.LABEL_REDRAFT, (part, card) -> card.redraft())
                .visibleWhen((part, card) -> card != null && !card.isEditing() && StringUtils.isNotBlank(card.getMessage().getResponse())));
        application(StoryMenuItem.card(StoryMenuKeys.HISTORY, L.LABEL_HISTORY, (part, card) -> card.openHistory())
                .contextLabel((loc, part, card) -> card == null || card.getHistoryCount() == 0 ? loc.getValue(L.LABEL_HISTORY)
                        : String.format(loc.getValue(L.LABEL_HISTORY_WITH_COUNT), card.getHistoryCount()))
                .visibleWhen((part, card) -> card != null && !card.isEditing() && card.getMessage().getId() != null));
        application(StoryMenuItem.card(StoryMenuKeys.ANNOTATE_PART, L.LABEL_ANNOTATE_PART, (part, card) -> card.annotatePart())
                .visibleWhen((part, card) -> card != null && card.getMessage().getId() != null));
        application(StoryMenuItem.card(StoryMenuKeys.ANNOTATE_WITH_AI, L.LABEL_ANNOTATE_WITH_AI, (part, card) -> card.annotateWithAi())
                .visibleWhen((part, card) -> hasText(card) && !card.isEditing()));
        application(StoryMenuItem.card(StoryMenuKeys.BRANCH, L.LABEL_BRANCH_STORY, (part, card) -> card.branch()));
        application(StoryMenuItem.card(StoryMenuKeys.MERGE, L.LABEL_MERGE_INTO_PREVIOUS, (part, card) -> card.mergeIntoPrevious())
                .visibleWhen((part, card) -> card != null && card.getMessage().getParent() != null));
        application(StoryMenuItem.card(StoryMenuKeys.SHOW_PROMPT, L.LABEL_SHOW_PROMPT, (part, card) -> card.showPrompt()));
        application(StoryMenuItem.card(StoryMenuKeys.COMPARE_PROMPT, L.LABEL_COMPARE_PROMPT, (part, card) -> card.comparePrompt()));
        application(StoryMenuItem.card(StoryMenuKeys.TOKEN_USAGE, L.LABEL_TOKEN_USAGE, (part, card) -> card.showTokenUsage()));
        application(StoryMenuItem.card(StoryMenuKeys.IMAGES, L.LABEL_IMAGES, (part, card) -> card.openImagesDialog()));
        application(StoryMenuItem.card(StoryMenuKeys.SUMMARY, L.LABEL_GENERATE_SUMMARY, (part, card) -> card.openSummaryDialog())
                .contextLabel((loc, part, card) -> loc.getValue(hasSummary(card) ? L.LABEL_SHOW_SUMMARY : L.LABEL_GENERATE_SUMMARY)));
        application(StoryMenuItem.card(StoryMenuKeys.DELETE_SUMMARY, L.LABEL_DELETE_SUMMARY, (part, card) -> card.confirmDeleteSummary())
                .enabledWhen((part, card) -> hasSummary(card)));
        application(StoryMenuItem.card(StoryMenuKeys.DELETE, L.LABEL_DELETE, (part, card) -> card.confirmDelete()));
    }

    private void registerTextItems() {
        // only here, not in the menu of the part: opening that menu takes the selection away
        application(StoryMenuItem.text(StoryMenuKeys.TEXT_ANNOTATE_SELECTION, L.LABEL_ANNOTATE_SELECTION, (part, card) -> card.annotateSelection())
                .actionId(ShortcutActions.STORY_ANNOTATE_SELECTION).defaultKey("a"));
        application(StoryMenuItem.text(StoryMenuKeys.TEXT_REDRAFT_SELECTION, L.LABEL_REDRAFT_SELECTION, (part, card) -> card.redraftSelection()));
        application(StoryMenuItem.text(StoryMenuKeys.TEXT_COPY, L.LABEL_COPY, (part, card) -> card.copySelection()));
    }
}
