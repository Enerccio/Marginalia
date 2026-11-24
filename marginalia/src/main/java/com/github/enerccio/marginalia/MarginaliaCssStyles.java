package com.github.enerccio.marginalia;

/**
 * Names of Marginalia's own CSS classes, defined in {@code shared-styles.css} (its frontend folder), which imports the
 * platform's stylesheet first: the shared classes (typography, layout helpers, panels, the platform widgets) are in
 * {@link CssStyles}. Style components with these instead of {@code getStyle().set(...)}, so the look lives in the
 * stylesheet.
 */
public final class MarginaliaCssStyles {

    // --- Classes with their own rules in shared-styles.css (the look of the text of the parts comes from the PartStyle classes, see PartStyleApplier) ---
    public static final String CHAT_MESSAGE = "chat-message";
    public static final String MARKED_FOR_DELETE = "marked-for-delete";
    public static final String CHAT_MESSAGE_MARKDOWN = "chat-message-markdown";

    // --- Layout helpers ---
    public static final String ORGANISE_PANE = "organise-pane";
    public static final String ORGANISE_PANE_ACTIVE = "organise-pane-active";
    public static final String ORGANISE_CURSOR_ROW = "organise-cursor-row";

    // --- Lists, scroll panels and reader ---
    public static final String SIDEBAR_BUTTON = "sidebar-button";
    public static final String SIDEBAR_HEADER = "sidebar-header";
    /** The icon in front of the caption of a part (tab) of the desktop workspace, see {@code PartIcon}. */
    public static final String WORKSPACE_PART_ICON = "workspace-part-icon";
    public static final String STORY_SCROLL_PANEL = "story-scroll-panel";
    public static final String READER_SCROLL_PANEL = "reader-scroll-panel";
    public static final String READER_CONTENT = "reader-content";
    public static final String FLOATING_BACK_BUTTON = "floating-back-button";
    public static final String FLOATING_STYLE_BUTTON = "floating-style-button";

    // --- Message images ---
    public static final String MESSAGE_IMAGES = "message-images";
    public static final String MESSAGE_IMAGE = "message-image";
    public static final String PART_VIEW = "part-view";
    public static final String PART_PICTURES = "part-pictures";
    public static final String PART_PICTURES_LEFT = "part-pictures-left";
    public static final String PART_PICTURES_CENTER = "part-pictures-center";
    public static final String PART_PICTURES_RIGHT = "part-pictures-right";
    public static final String PART_PICTURES_FLOAT = "part-pictures-float";
    public static final String PART_PICTURE = "part-picture";
    public static final String PART_PICTURE_SIZED = "part-picture-sized";
    public static final String PART_PICTURE_FLOAT_LEFT = "part-picture-float-left";
    public static final String PART_PICTURE_FLOAT_RIGHT = "part-picture-float-right";
    public static final String PART_BAND = "part-band";
    public static final String PART_BAND_MIDDLE = "part-band-middle";
    public static final String PART_BAND_TEXT = "part-band-text";
    public static final String IMAGE_ANCHOR_PICKER = "image-anchor-picker";
    public static final String IMAGE_ANCHOR_DOT = "image-anchor-dot";
    public static final String IMAGE_ANCHOR_DOT_SELECTED = "image-anchor-dot-selected";
    public static final String PART_PREVIEW_FRAME = "part-preview-frame";
    public static final String PART_PREVIEW_PHONE = "part-preview-phone";

    // --- Story messages and summaries ---
    public static final String CHAT_MESSAGE_CONTENT = "chat-message-content";
    public static final String CHAT_MESSAGE_META = "chat-message-meta";
    public static final String GENERATED_TEXT = "generated-text";
    public static final String SEARCH_MATCH = "search-match";
    public static final String SEARCH_MATCH_CURRENT = "search-match-current";
    public static final String ANNOTATION_MARK = "annotation-mark";
    public static final String ANNOTATION_MARK_AUTHOR = "annotation-mark-author";
    public static final String ANNOTATION_MARK_AI = "annotation-mark-ai";
    public static final String ANNOTATION_MARK_AGENT = "annotation-mark-agent";
    public static final String ANNOTATION_MARK_PLUGIN = "annotation-mark-plugin";
    public static final String ANNOTATION_ANCHOR = "annotation-anchor";
    public static final String ANNOTATION_BADGE = "annotation-badge";
    public static final String ANNOTATION_VIEW = "annotation-view";
    public static final String ANNOTATION_STATUS = "annotation-status";
    public static final String ANNOTATION_STATUS_OPEN = "annotation-status-open";
    public static final String ANNOTATION_STATUS_ORPHANED = "annotation-status-orphaned";
    public static final String ANNOTATION_STATUS_RESOLVED = "annotation-status-resolved";
    public static final String ANNOTATION_STATUS_DISMISSED = "annotation-status-dismissed";
    public static final String ANNOTATION_TYPE = "annotation-type";
    public static final String ANNOTATION_QUOTE = "annotation-quote";
    public static final String ANNOTATION_NOTE = "annotation-note";
    public static final String ANNOTATION_PANEL = "annotation-panel";
    public static final String ANNOTATION_PART = "annotation-part";
    public static final String ANNOTATION_POPOVER = "annotation-popover";
    public static final String ANNOTATION_MARGIN = "annotation-margin";
    public static final String ANNOTATION_MARGIN_LAYOUT = "annotations-in-margin";
    public static final String ANNOTATION_LIT = "annotation-lit";
    public static final String SUMMARY_CONTENT = "summary-content";
    public static final String SUMMARY_ACTIONS = "summary-actions";
    /** Row of the summaries overview of a summary that no longer describes the story. */
    public static final String SUMMARY_OUTDATED = "summary-outdated";

    // --- Chart.js chart ---
    public static final String TOKEN_USAGE_SUMMARY = "token-usage-summary";

    // --- statistics ---
    public static final String STATISTIC_TOTALS = "statistic-totals";
    public static final String STATISTIC_TOTAL_ITEM = "statistic-total-item";
    public static final String STATISTIC_GROWTH_UP = "statistic-growth-up";
    public static final String STATISTIC_GROWTH_DOWN = "statistic-growth-down";
    public static final String ACTIVITY_CALENDAR = "activity-calendar";
    public static final String ACTIVITY_CALENDAR_WEEK = "activity-calendar-week";
    public static final String ACTIVITY_CALENDAR_MONTH = "activity-calendar-month";
    public static final String ACTIVITY_CALENDAR_DAY = "activity-calendar-day";
    /** The prefix of the shade classes, the level (0 to 4) follows it. */
    public static final String ACTIVITY_CALENDAR_LEVEL = "activity-calendar-level-";
    public static final String ACTIVITY_CALENDAR_TODAY = "activity-calendar-today";
    public static final String ACTIVITY_CALENDAR_LEGEND = "activity-calendar-legend";
    public static final String ACTIVITY_GOAL = "activity-goal";
    public static final String ACTIVITY_GOAL_LINE = "activity-goal-line";
    public static final String ACTIVITY_GOAL_BAR = "activity-goal-bar";
    public static final String PROMPT_COMPARE_FIRST_DIFFERENCE = "prompt-compare-first-difference";
    public static final String PROMPT_COMPARE_LOST = "prompt-compare-lost";
    public static final String HISTORY_ROW_DELETED = "history-row-deleted";

    // --- Manuscript dialog ---
    /** The dialog of a manuscript: its footer (the Exit button) takes little room. */
    public static final String MANUSCRIPT_DIALOG = "manuscript-dialog";

    // --- Phone workspace ---
    /** A page of the phone workspace made of layouts, grids and fields: nothing in it is wider than the screen. */
    public static final String PHONE_PAGE = "phone-page";

    // --- Phone manuscript dialog ---
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
    public static final String JSON_MERGE_CELL = "json-merge-cell";
    public static final String JSON_MERGE_CELL_AGENT = "json-merge-cell-agent";
    public static final String JSON_MERGE_CELL_BOOK = "json-merge-cell-book";
    public static final String JSON_MERGE_CELL_CONFLICT = "json-merge-cell-conflict";
    public static final String JSON_MERGE_ABSENT = "json-merge-absent";
    public static final String JSON_MERGE_TEXT = "json-merge-text";
    public static final String AGENT_COPY_PART = "agent-copy-part";
    public static final String AGENT_COPY_PART_HEAD = "agent-copy-part-head";
    public static final String AGENT_PLAN = "agent-plan";
    public static final String AGENT_PLAN_ITEM = "agent-plan-item";
    public static final String AGENT_PLAN_DONE = "agent-plan-done";
    public static final String AGENT_PLAN_ACTIVE = "agent-plan-active";
    public static final String AGENT_FINDINGS = "agent-findings";
    public static final String AGENT_FINDINGS_COMPACT = "agent-findings-compact";
    public static final String AGENT_FINDING = "agent-finding";
    public static final String AGENT_FINDING_WARNING = "agent-finding-warning";
    public static final String AGENT_FINDING_PROBLEM = "agent-finding-problem";
    public static final String AGENT_ATTENTION_CUE = "agent-attention-cue";
    public static final String HELP_INPUT = "help-input";

    // --- Commands (ui.components.CommandBar, CommandResultView) ---
    public static final String COMMAND_BOX = "command-box";

    // --- Search and replace (ui.dialogs.search) ---
    public static final String SEARCH_BUTTON_ROW = "search-button-row";
    public static final String SEARCH_INPUT_GRID = "search-input-grid";
    public static final String SEARCH_HIT_CONTEXT = "search-hit-context";
    public static final String SEARCH_HIT_MATCH = "search-hit-match";
    public static final String SEARCH_HIT_REPLACEMENT = "search-hit-replacement";
    public static final String SEARCH_HIT_PLACEHOLDER = "search-hit-placeholder";

    // --- Tags of many items at once (ui.dialogs.MultiTagDialog) ---
    public static final String MULTI_TAG_LOCKED = "multi-tag-locked";
    public static final String MULTI_TAG_CHIP = "multi-tag-chip";
    public static final String MULTI_TAG_CHIP_MARKED = "multi-tag-chip-marked";

    // --- Mass edit of the entries of a lorebook (ui.dialogs.LorebookMassEditDialog) ---
    public static final String MASS_EDIT_CONTENT = "mass-edit-content";

    // --- Editor of the text styles in the user settings (ui.styles.PartStyleEditor) ---
    public static final String STYLE_EDITOR_LIST = "style-editor-list";
    public static final String STYLE_EDITOR_PREVIEW = "style-editor-preview";
    public static final String STYLE_EDITOR_CSS = "style-editor-css";

    // --- Planner tab of the story editor (ui.dialogs.story.PlannerPanel) ---
    public static final String PLANNER_SCENE = "planner-scene";
    public static final String PLANNER_SCENE_DONE = "planner-scene-done";
    public static final String PLANNER_SCENE_CURRENT = "planner-scene-current";
    public static final String PLANNER_SCENE_MAIN = "planner-scene-main";
    public static final String PLANNER_SCENE_TEXT = "planner-scene-text";

    // the corner of the page where the windows of model runs are folded (RunCorner), and the mark of a part a window works on
    public static final String PART_BUSY = "part-busy";

    private MarginaliaCssStyles() {
    }

}
