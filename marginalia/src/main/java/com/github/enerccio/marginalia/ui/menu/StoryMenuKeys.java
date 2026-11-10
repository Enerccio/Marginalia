package com.github.enerccio.marginalia.ui.menu;

/**
 * Keys of the menu items the application registers in the {@link StoryMenuRegistry}: the code that has to find the item of a menu
 * (to change it, to hide it, to add one next to it) asks the {@link BuiltMenu} for its key.
 */
public final class StoryMenuKeys {

    // --- GROUPS: the paths of the groups of the menus, an extension puts its item into one with StoryMenuItem.path ---
    public static final String GROUP_APPEARANCE = "appearance";
    public static final String GROUP_ANALYSIS = "analysis";
    public static final String GROUP_IMPORT_EXPORT = "importExport";
    public static final String GROUP_CLEAN_UP = "cleanUp";
    public static final String GROUP_LAYOUT = "layout";
    public static final String GROUP_ANNOTATE = "annotate";
    public static final String GROUP_SUMMARY = "summary";
    public static final String GROUP_INSPECT = "inspect";
    public static final String GROUP_BRANCH_MERGE = "branchMerge";

    // --- TOOLS ---
    public static final String CHANGE_STYLES = "tools.changeStyles";
    public static final String HIGHLIGHT_GENERATED = "tools.highlightGenerated";
    public static final String ANNOTATIONS_IN_MARGIN = "tools.annotationsInMargin";
    public static final String SEARCH_REPLACE = "tools.searchReplace";
    // the import into the book; the key is from the time it only took archives, it is kept so that menus the users arranged stay
    public static final String IMPORT_ARCHIVE = "tools.importArchive";
    public static final String ANNOTATE_RANGE = "tools.annotateRange";
    public static final String CREATE_SUMMARIES = "tools.createSummaries";
    public static final String LORE_NOTES = "tools.loreNotes";
    public static final String AUTO_ANNOTATE = "tools.autoAnnotate";
    public static final String BRANCH_TOKENS = "tools.branchTokens";
    public static final String LORE_USAGE = "tools.loreUsage";
    public static final String STATISTICS = "tools.statistics";
    public static final String EXPORT = "tools.export";
    public static final String CLEAR_BOOK_HISTORY = "tools.clearBookHistory";
    public static final String EMPTY_HISTORY_TRASH = "tools.emptyHistoryTrash";
    public static final String MASS_DELETE = "tools.massDelete";
    public static final String RESTORE_LAYOUT = "tools.restoreLayout";
    public static final String SAVE_LAYOUT_AS_DEFAULT = "tools.saveLayoutAsDefault";

    // --- CARD ---
    public static final String EDIT = "card.edit";
    public static final String DISCARD = "card.discard";
    public static final String REGENERATE = "card.regenerate";
    public static final String SWIPE = "card.swipe";
    public static final String REDRAFT = "card.redraft";
    public static final String HISTORY = "card.history";
    public static final String ANNOTATE_PART = "card.annotatePart";
    public static final String ANNOTATE_WITH_AI = "card.annotateWithAi";
    public static final String BRANCH = "card.branch";
    public static final String MERGE = "card.merge";
    public static final String SHOW_PROMPT = "card.showPrompt";
    public static final String COMPARE_PROMPT = "card.comparePrompt";
    public static final String TOKEN_USAGE = "card.tokenUsage";
    public static final String CARD_LORE_USAGE = "card.loreUsage";
    public static final String IMAGES = "card.images";
    public static final String IMAGE_PREVIEW = "card.imagePreview";
    public static final String SUMMARY = "card.summary";
    public static final String DELETE_SUMMARY = "card.deleteSummary";
    public static final String DELETE = "card.delete";

    // --- TEXT ---
    public static final String TEXT_ANNOTATE_SELECTION = "text.annotateSelection";
    public static final String TEXT_REDRAFT_SELECTION = "text.redraftSelection";
    public static final String TEXT_COPY = "text.copy";

    private StoryMenuKeys() {
    }
}
