package com.github.enerccio.marginalia;

/**
 * Names of the CSS classes defined in {@code shared-styles.css}. Style components with these instead of
 * {@code getStyle().set(...)}, so the look lives in the stylesheet.
 */
public final class CssStyles {

    // --- Classes with their own rules in shared-styles.css (manuscript-styles and chat-message-markdown: reading-mode-styles.css) ---
    public static final String MARKDOWN_MANUSCRIPT_STYLES = "manuscript-styles";
    public static final String CHAT_MESSAGE = "chat-message";
    public static final String CHAT_MESSAGE_MARKDOWN = "chat-message-markdown";

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
    public static final String SIDEBAR_BUTTON = "sidebar-button";
    public static final String SIDEBAR_HEADER = "sidebar-header";
    public static final String TAB_DOCK = "tab-dock";
    public static final String TAB_DOCK_CELL = "tab-dock-cell";
    public static final String TAB_DOCK_COLUMN = "tab-dock-column";
    public static final String TAB_DOCK_COLUMN_BODY = "tab-dock-column-body";
    public static final String TAB_DOCK_PANEL = "tab-dock-panel";
    public static final String TAB_DOCK_TAB = "tab-dock-tab";
    public static final String TAB_DOCK_ZONE = "tab-dock-zone";
    public static final String TAB_DOCK_SIDE_ZONE = "tab-dock-side-zone";
    public static final String TAB_DOCK_DRAGGING = "tab-dock-dragging";
    public static final String STORY_SCROLL_PANEL = "story-scroll-panel";
    public static final String READER_SCROLL_PANEL = "reader-scroll-panel";
    public static final String LAZY_LOAD_INDICATOR = "lazy-load-indicator";
    public static final String READER_CONTENT = "reader-content";
    public static final String FLOATING_BACK_BUTTON = "floating-back-button";

    // --- Message images ---
    public static final String MESSAGE_IMAGES = "message-images";
    public static final String MESSAGE_IMAGE = "message-image";

    // --- Story messages and summaries ---
    public static final String CHAT_MESSAGE_CONTENT = "chat-message-content";
    public static final String CHAT_MESSAGE_META = "chat-message-meta";
    public static final String GENERATED_TEXT = "generated-text";
    public static final String SUMMARY_CONTENT = "summary-content";
    public static final String SUMMARY_ACTIONS = "summary-actions";

    // --- graph ---
    public static final String GRAPH_VIEW = "graph-view";

    // --- Chart.js chart ---
    public static final String CHART_JS = "chart-js";
    public static final String TOKEN_USAGE_SUMMARY = "token-usage-summary";

    // --- Resizable text area toggle icon ---
    public static final String RESIZABLE_TEXT_AREA_ICON = "resizable-text-area-icon";

    // --- Phone manuscript dialog ---
    public static final String PHONE_DIALOG = "phone-dialog";
    public static final String PHONE_STORY_LAYOUT = "phone-story-layout";
    public static final String PHONE_MESSAGE = "phone-message";
    public static final String PHONE_MESSAGE_HEADER = "phone-message-header";
    public static final String PHONE_POPOVER_CONTENT = "phone-popover-content";

    // --- Help assistant (ui.components.HelpAssistantWidget) ---
    public static final String HELP_ROOT = "help-root";
    public static final String HELP_FAB = "help-fab";
    public static final String HELP_PANEL = "help-panel";
    public static final String HELP_HEADER = "help-header";
    public static final String HELP_WARNING = "help-warning";
    public static final String HELP_MESSAGES = "help-messages";
    public static final String HELP_MESSAGE = "help-message";
    public static final String HELP_MESSAGE_USER = "help-message-user";
    public static final String HELP_MESSAGE_QUEUED = "help-message-queued";
    public static final String HELP_MESSAGE_FOLDED = "help-message-folded";
    public static final String HELP_MESSAGE_ERROR = "help-message-error";
    public static final String AGENT_REWOUND = "agent-rewound";
    public static final String AGENT_CONSOLE_PHONE = "agent-console-phone";
    public static final String AGENT_CARD = "agent-card";
    public static final String AGENT_REPORT = "agent-report";
    public static final String AGENT_TIME = "agent-time";
    public static final String MERGE_DIFF = "merge-diff";
    public static final String MERGE_DIFF_ADDED = "merge-diff-added";
    public static final String MERGE_DIFF_REMOVED = "merge-diff-removed";
    public static final String AGENT_COPY_PART = "agent-copy-part";
    public static final String AGENT_COPY_PART_HEAD = "agent-copy-part-head";
    public static final String HELP_INPUT = "help-input";

    // --- Commands (ui.components.CommandBar, CommandResultView) ---
    public static final String COMMAND_BOX = "command-box";
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

    // --- Search and replace (ui.dialogs.search) ---
    public static final String SEARCH_BUTTON_ROW = "search-button-row";
    public static final String SEARCH_INPUT_GRID = "search-input-grid";
    public static final String SEARCH_HIT_CONTEXT = "search-hit-context";
    public static final String SEARCH_HIT_MATCH = "search-hit-match";
    public static final String SEARCH_HIT_REPLACEMENT = "search-hit-replacement";
    public static final String SEARCH_HIT_PLACEHOLDER = "search-hit-placeholder";

    // --- Agent console (ui.dialogs.agent) ---
    public static final String WORKSPACE_BROWSER = "workspace-browser";

    // --- Tags of many items at once (ui.dialogs.MultiTagDialog) ---
    public static final String MULTI_TAG_LOCKED = "multi-tag-locked";
    public static final String MULTI_TAG_CHIP = "multi-tag-chip";
    public static final String MULTI_TAG_CHIP_MARKED = "multi-tag-chip-marked";

    // --- Mass edit of the entries of a lorebook (ui.dialogs.LorebookMassEditDialog) ---
    public static final String MASS_EDIT_CONTENT = "mass-edit-content";

    // --- Version footer (ui.components.BuildInfoFooter) ---
    public static final String BUILD_INFO_FOOTER = "build-info-footer";

    private CssStyles() {
    }

}
