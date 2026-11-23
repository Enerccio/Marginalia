package com.github.enerccio.marginalia;

/**
 * Names of the CSS classes of the platform, defined in {@code platform-styles.css} (the platform JAR's
 * {@code META-INF/frontend/styles}), the look every application on the platform shares; an application's own classes
 * are in a class of its own (Marginalia's {@code MarginaliaCssStyles}). Style components with these instead of
 * {@code getStyle().set(...)}, so the look lives in the stylesheet.
 */
public final class CssStyles {

    // --- Classes with their own rules in shared-styles.css (the look of the text of the parts comes from the PartStyle classes, see PartStyleApplier) ---
    public static final String MASS_DELETE_BOX = "mass-delete-box";

    // --- Typography ---
    public static final String SECTION_TITLE = "section-title";
    public static final String TEXT_CENTER = "text-center";
    public static final String TEXT_BOLD = "text-bold";
    public static final String TEXT_SEMIBOLD = "text-semibold";
    public static final String TEXT_XS = "text-xs";
    public static final String TEXT_S = "text-s";
    public static final String TEXT_M = "text-m";
    public static final String TEXT_MONOSPACE = "text-monospace";
    public static final String TEXT_MONOSPACE_LUMO = "text-monospace-lumo";
    public static final String TEXT_SECONDARY = "text-secondary";
    public static final String TEXT_ERROR = "text-error";
    /** Takes the place of the fold button in a row that has nothing to fold. */
    public static final String FOLD_SPACER = "fold-spacer";
    public static final String TEXT_DANGER = "text-danger";
    public static final String TEXT_SUCCESS = "text-success";
    public static final String TEXT_PRIMARY = "text-primary";
    public static final String HINT_TEXT = "hint-text";
    public static final String WHITE_SPACE_NORMAL = "white-space-normal";
    public static final String PRE_WRAP = "pre-wrap";
    public static final String WRAP_ANYWHERE = "wrap-anywhere";
    /** One line, cut with an ellipsis; the whole text belongs in the tooltip. */
    public static final String CLIPPED_LINE = "clipped-line";

    // --- Layout helpers ---
    public static final String FULL_WIDTH = "full-width";
    public static final String FULL_SIZE = "full-size";
    public static final String MIN_WIDTH_0 = "min-width-0";
    public static final String MIN_HEIGHT_0 = "min-height-0";
    public static final String MAX_WIDTH_FULL = "max-width-full";
    public static final String BORDER_BOX = "border-box";
    public static final String OVERFLOW_HIDDEN = "overflow-hidden";
    public static final String OVERFLOW_AUTO = "overflow-auto";
    public static final String DISPLAY_BLOCK = "display-block";
    public static final String POSITION_RELATIVE = "position-relative";
    public static final String FLEX_WRAP = "flex-wrap";
    public static final String FLEX_SHRINK_0 = "flex-shrink-0";
    /** In a wrapping flex layout: what follows starts a new line. */
    public static final String FLEX_BREAK = "flex-break";
    public static final String GAP_S = "gap-s";
    public static final String MARGIN_LEFT_AUTO = "margin-left-auto";
    public static final String CURSOR_POINTER = "cursor-pointer";
    public static final String CLICKABLE_CHIPS = "clickable-chips";
    public static final String FORM_PADDING_BOTTOM = "form-padding-bottom";
    public static final String PADDING_COMPACT = "padding-compact";
    public static final String PADDING_COMPACT_NARROW = "padding-compact-narrow";
    public static final String BORDER_TOP_SUBTLE = "border-top-subtle";
    public static final String BORDER_RIGHT_SUBTLE = "border-right-subtle";

    // --- Panels and boxes ---
    public static final String NOTICE_WARNING = "notice-warning";
    public static final String NOTICE_ERROR = "notice-error";
    public static final String SUBTLE_BOX = "subtle-box";
    public static final String BORDERED_PANEL = "bordered-panel";
    public static final String INFO_ICON = "info-icon";
    public static final String POPOVER_FORM = "popover-form";
    public static final String TREE_TOOLBAR = "tree-toolbar";

    // --- Lists, scroll panels and reader ---
    public static final String LIST_ITEM_SUBROW = "list-item-subrow";
    public static final String TAB_DOCK = "tab-dock";
    public static final String TAB_DOCK_CELL = "tab-dock-cell";
    public static final String TAB_DOCK_COLUMN = "tab-dock-column";
    public static final String TAB_DOCK_COLUMN_BODY = "tab-dock-column-body";
    public static final String TAB_DOCK_PANEL = "tab-dock-panel";
    public static final String TAB_DOCK_TAB = "tab-dock-tab";
    public static final String TAB_DOCK_ZONE = "tab-dock-zone";
    public static final String TAB_DOCK_SIDE_ZONE = "tab-dock-side-zone";
    public static final String TAB_DOCK_DRAGGING = "tab-dock-dragging";
    public static final String TAB_DOCK_BODY = "tab-dock-body";
    public static final String TAB_DOCK_BAR = "tab-dock-bar";
    public static final String TAB_DOCK_BAR_ICON = "tab-dock-bar-icon";
    public static final String TAB_DOCK_BAR_MOVE = "tab-dock-bar-move";
    public static final String TAB_DOCK_MINIMIZE = "tab-dock-minimize";
    public static final String TAB_DOCK_AT_LEFT = "tab-dock-at-left";
    public static final String TAB_DOCK_AT_RIGHT = "tab-dock-at-right";
    public static final String TAB_DOCK_AT_TOP = "tab-dock-at-top";
    public static final String TAB_DOCK_AT_BOTTOM = "tab-dock-at-bottom";
    public static final String LAZY_LOAD_INDICATOR = "lazy-load-indicator";
    /** The Markdown editor ({@code ui.widgets.MarkdownEditor}): fills its height, the colors and the font of Lumo. */
    public static final String MARKDOWN_EDITOR = "markdown-editor";

    // --- graph ---
    public static final String GRAPH_VIEW = "graph-view";

    // --- Chart.js chart ---
    public static final String CHART_JS = "chart-js";

    // --- Resizable text area toggle icon ---
    public static final String RESIZABLE_TEXT_AREA_ICON = "resizable-text-area-icon";
    public static final String RESIZABLE_TEXT_AREA_FIELD = "resizable-text-area-field";

    // --- Phone manuscript dialog ---
    public static final String PHONE_DIALOG = "phone-dialog";

    // --- Help assistant (ui.components.HelpAssistantWidget) ---
    public static final String MERGE_DIFF = "merge-diff";
    public static final String MERGE_DIFF_ADDED = "merge-diff-added";
    public static final String MERGE_DIFF_REMOVED = "merge-diff-removed";

    // --- Commands (ui.components.CommandBar, CommandResultView) ---
    public static final String MENU_ITEM_CONTENT = "menu-item-content";
    public static final String MENU_ITEM_TEXT = "menu-item-text";
    public static final String MENU_ITEM_KEY = "menu-item-key";
    public static final String COMMAND_BAR = "command-bar";
    public static final String COMMAND_HELP = "command-help";
    public static final String COMMAND_HELP_USAGE = "command-help-usage";
    public static final String COMMAND_HELP_CURRENT = "command-help-current";
    public static final String COMMAND_HELP_ERROR = "command-help-error";
    public static final String COMMAND_ITEM_NAME = "command-item-name";
    public static final String COMMAND_ITEM_DESCRIPTION = "command-item-description";
    public static final String COMMAND_SEVERITY = "command-severity";
    public static final String COMMAND_SEVERITY_DESTRUCTIVE = "command-severity-destructive";
    public static final String COMMAND_SEVERITY_NON_DESTRUCTIVE = "command-severity-non-destructive";
    public static final String COMMAND_RESULT = "command-result";
    public static final String COMMAND_RESULT_TEXT = "command-result-text";

    // --- Version footer (ui.components.BuildInfoFooter) ---
    public static final String BUILD_INFO_FOOTER = "build-info-footer";

    // the corner of the page where the windows of model runs are folded (RunCorner), and the mark of a part a window works on
    public static final String RUN_CORNER_ROOT = "run-corner-root";
    public static final String RUN_CORNER = "run-corner";
    public static final String RUN_CORNER_ENTRY = "run-corner-entry";
    public static final String RUN_CORNER_BODY = "run-corner-body";
    public static final String RUN_CORNER_LABEL = "run-corner-label";
    public static final String RUN_CORNER_BUTTONS = "run-corner-buttons";
    public static final String RUN_CORNER_CHIP = "run-corner-chip";
    public static final String RUN_CORNER_GRIP = "run-corner-grip";
    public static final String RUN_CORNER_BAR = "run-corner-bar";
    public static final String RUN_CORNER_LIST = "run-corner-list";
    public static final String RUN_CORNER_STATUS = "run-corner-status";
    public static final String RUN_CORNER_TEXT = "run-corner-text";
    /** A dense window: smaller fields, buttons, icons, text and gaps inside it (the full-screen lorebook editor). */
    public static final String COMPACT_SIZES = "compact-sizes";
    /** The browser of a workspace (WorkspaceBrowser): small text, the list and the viewer share the height. */
    public static final String WORKSPACE_BROWSER = "workspace-browser";

    private CssStyles() {
    }

}
