package com.github.enerccio.marginalia.ui.menu;

/**
 * Keys of the menu items the application registers in the {@link StoryMenuRegistry}: the code that has to find the item of a menu
 * (to change it, to hide it, to add one next to it) asks the {@link BuiltMenu} for its key.
 */
public final class StoryMenuKeys {

    // --- TOOLS ---
    public static final String CHANGE_STYLES = "tools.changeStyles";
    public static final String HIGHLIGHT_GENERATED = "tools.highlightGenerated";
    public static final String ANNOTATIONS_IN_MARGIN = "tools.annotationsInMargin";
    public static final String SEARCH_REPLACE = "tools.searchReplace";
    public static final String IMPORT_ARCHIVE = "tools.importArchive";
    public static final String BRANCH_TOKENS = "tools.branchTokens";
    public static final String EXPORT = "tools.export";
    public static final String RESTORE_LAYOUT = "tools.restoreLayout";

    // --- CARD ---
    public static final String EDIT = "card.edit";
    public static final String DISCARD = "card.discard";
    public static final String REGENERATE = "card.regenerate";
    public static final String SWIPE = "card.swipe";
    public static final String REDRAFT = "card.redraft";
    public static final String ANNOTATE_PART = "card.annotatePart";
    public static final String ANNOTATE_WITH_AI = "card.annotateWithAi";
    public static final String BRANCH = "card.branch";
    public static final String MERGE = "card.merge";
    public static final String SHOW_PROMPT = "card.showPrompt";
    public static final String TOKEN_USAGE = "card.tokenUsage";
    public static final String IMAGES = "card.images";
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
