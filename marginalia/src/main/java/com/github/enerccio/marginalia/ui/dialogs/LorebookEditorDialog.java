package com.github.enerccio.marginalia.ui.dialogs;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.*;
import com.github.enerccio.marginalia.ui.dialogs.search.LorebookSearchReplaceDialog;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.MarkdownEditor;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.TabDock;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * The lorebook editor: a full screen window with the entries of one lorebook in a tab dock. One tab, which cannot be
 * closed, lists the entries (text only: open, open in a new tab, delete); every entry opens in a tab of its own
 * ({@link LorebookEntryEditor}), at most once: opening an entry that has a tab shows that tab. <i>Open</i> shows the entry
 * in the entry tab the user had last, <i>Open in new tab</i> adds a tab. The tabs can be moved and split like the tabs of
 * the story editor, and closed. The header has the token estimate of the lorebook and the search and replace of its entries.
 * Every change is saved at once, so closing loses nothing.
 * <p>
 * The arrangement is kept in the lorebook (its attribute {@link #CONFIG_ATTRIBUTE}, so it goes along with backups): the
 * layout of the dock, the tab each sheet shows and the open entries, and the editor opens as it was left. It is saved when
 * the layout changes, when a tab is opened or closed and when the editor is closed; entries that are gone are left out.
 */
@Configurable
@Extendable
public class LorebookEditorDialog extends Dialog {

    static final String LIST_TAB = "entries";
    private static final String ENTRY_TAB = "entry-";
    /**
     * The attribute of the lorebook that keeps how the editor was left: {@code {"layout": TabDock layout, "state": TabDock state,
     * "tabs": [{"tab": "entry-2", "entry": "<uuid>", "mode": "LIVE"}, ...]}}, the tabs in the order they were used, the last one
     * first, each with the mode of its Markdown editor.
     */
    public static final String CONFIG_ATTRIBUTE = "$lorebookEditor";
    private static final String KEY_LAYOUT = "layout";
    private static final String KEY_STATE = "state";
    private static final String KEY_TABS = "tabs";
    private static final String KEY_TAB = "tab";
    private static final String KEY_ENTRY = "entry";
    private static final String KEY_MODE = "mode";
    private static final String KEY_SUBBOOKS = "subbooks";

    @Autowired
    private Localization loc;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private AgentRunService agentRunService;

    private Lorebook lorebook;
    private final LorebookView origin;
    // the book the editor was opened for (the story's lorebook tab), null when it is not opened for one
    private Callable<Manuscript> book;
    private final Runnable onClose;
    private final LorebookTokens tokens = new LorebookTokens();

    private TabDock dock;
    @PublicAPI(from = "1.8.0", type = Grid.class)
    private Grid<LorebookEntry> grid;
    private ListDataProvider<LorebookEntry> entryProvider;
    private final LorebookEntryFilter entryFilter = new LorebookEntryFilter();
    private TextField searchField;
    private ComboBox<String> tagFilter;
    private ComboBox<LorebookEntryFilter.Status> statusFilter;
    private Span totalSpan;
    private Span withSubbooksSpan;
    private Button newEntryButton;
    private Button searchReplaceButton;
    private Button massEditButton;
    private Button tagEntriesButton;
    // shown while an agent works on the lorebook: the editor is read only until it lets go
    private HorizontalLayout lockBar;
    private Span lockText;
    private String lockedBy;

    private List<LorebookEntry> entries = new ArrayList<>();
    // the entries of the sub lorebooks are listed too: the lorebooks listed (this one first) and the lorebook of every entry
    private Checkbox includeSubbooks;
    private ComboBox<Lorebook> targetBook;
    private List<Lorebook> books = new ArrayList<>();
    private final Map<Long, Lorebook> entryBooks = new HashMap<>();
    private Map<Long, Set<String>> entryTags = new HashMap<>();
    // the entry tabs by their id; the id of a tab stays when another entry is shown in it
    private final Map<String, LorebookEntryEditor> tabs = new LinkedHashMap<>();
    // the mode of the Markdown editor of every entry tab, by the id of the tab: it stays with the tab, whatever entry it shows
    private final Map<String, MarkdownEditor.Mode> modes = new HashMap<>();
    // the ids of the entry tabs, the one the user had last first
    private final Deque<String> recent = new ArrayDeque<>();
    private int nextTab = 1;
    // the stored arrangement is being applied: nothing to save
    private boolean restoring;
    // told of the entries written by other windows and by agents while the editor is open
    private Registration loreChanges;

    /**
     * @param lorebook the lorebook whose entries are edited
     * @param onClose  told when the editor is closed (the view it was opened from shows the entries again), may be null
     */
    public LorebookEditorDialog(Lorebook lorebook, Runnable onClose) {
        this(lorebook, null, onClose);
    }

    /**
     * @param lorebook the lorebook whose entries are edited
     * @param origin   the lorebook view it is opened from (the one of the workspace or of a book), may be null; extensions
     *                 read what they put on it (the Lore Generator: the book)
     * @param onClose  told when the editor is closed (the view it was opened from shows the entries again), may be null
     */
    public LorebookEditorDialog(Lorebook lorebook, LorebookView origin, Runnable onClose) {
        this.lorebook = lorebook;
        this.origin = origin;
        this.onClose = onClose;
    }

    /**
     * The lorebook whose entries are edited.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Lorebook.class))
    public Lorebook getLorebook() {
        return lorebook;
    }

    /**
     * The lorebook view the editor was opened from, null when none.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = LorebookView.class))
    public LorebookView getOrigin() {
        return origin;
    }

    /**
     * The book the editor is opened for, as the database has it now (the story's *Lorebook* tab gives it), for extensions
     * that work for a book (the Lore Generator bills its model); {@code null} when it was not opened for a book.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Callable.class))
    public Callable<Manuscript> getBook() {
        return book;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "book", type = Callable.class)})
    public void setBook(Callable<Manuscript> book) {
        this.book = book;
    }

    /**
     * Loads the entries from the database again and shows them in the list and in the open tabs (after something changed
     * many entries: a replace, an import).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class))
    public void refresh() {
        loadEntries(true);
        reloadTabs();
    }

    /**
     * Shows one entry as the database has it now, in its tab when it has one and in the list (something else changed it:
     * a generator, a revision).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "id", type = String.class)})
    public void reloadEntry(LorebookEntry entry) {
        String id = entry == null ? null : tabOf(entry);
        if (id != null) {
            tabs.get(id).reloadFromDatabase();
        }
        // the open entries stay the objects of their tabs, the others come from the database
        loadEntries(false);
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = LorebookEditorDialog.class),
            locals = {@APILocals(name = "fresh", type = Lorebook.class),
                    @APILocals(name = "e", type = Exception.class),
                    @APILocals(name = "countAll", type = Button.class),
                    @APILocals(name = "searchReplace", type = Button.class),
                    @APILocals(name = "header", type = HorizontalLayout.class),
                    @APILocals(name = "checkAgain", type = Button.class),
                    @APILocals(name = "content", type = VerticalLayout.class),
                    @APILocals(name = "close", type = Button.class)})
    public LorebookEditorDialog create() {
        try {
            // the arrangement stored last is read from the lorebook as the database has it
            Lorebook fresh = lorebook.getUuid() == null ? null : lorebookService.findForUser(lorebook.getUuid());
            if (fresh != null) {
                lorebook = fresh;
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        setHeaderTitle(StringUtils.defaultString(lorebook.getName()));
        setSizeFull();
        // every pixel goes to the entries: the window has no padding of its own, the content is dense
        getElement().getThemeList().add("no-padding");
        setModality(ModalityMode.STRICT);
        setCloseOnOutsideClick(false);
        setCloseOnEsc(false);

        totalSpan = new Span();
        totalSpan.addClassName(CssStyles.HINT_TEXT);
        withSubbooksSpan = new Span();
        withSubbooksSpan.addClassName(CssStyles.HINT_TEXT);
        Button countAll = new Button(loc.getValue(L.LABEL_COUNT_TOKENS_WITH_SUBBOOKS), event -> countWithSubbooks());
        countAll.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        Button searchReplace = new Button(loc.getValue(L.LABEL_SEARCH_AND_REPLACE), Solid.EXCHANGE_ALT.create(),
                event -> openSearchReplace());
        searchReplaceButton = searchReplace;
        HorizontalLayout header = new HorizontalLayout(totalSpan, countAll, withSubbooksSpan, searchReplace);
        header.setAlignItems(FlexComponent.Alignment.BASELINE);
        header.addClassName(CssStyles.COMPACT_SIZES);
        getHeader().add(header);

        dock = new TabDock();
        dock.setSizeFull();
        dock.add(LIST_TAB, loc.getValue(L.LABEL_ENTRIES), createList());
        dock.setMinimizable(LIST_TAB, false);
        dock.setOnClose(this::tabClosed);
        dock.setOnStateChange(this::tabShown);

        lockText = new Span();
        lockText.addClassName(CssStyles.TEXT_ERROR);
        Button checkAgain = new Button(loc.getValue(L.LABEL_CHECK_AGAIN), Solid.REFRESH.create(), event -> checkLock());
        checkAgain.addThemeVariants(ButtonVariant.LUMO_SMALL);
        lockBar = new HorizontalLayout(lockText, checkAgain);
        lockBar.setWidthFull();
        lockBar.setAlignItems(FlexComponent.Alignment.BASELINE);
        lockBar.addClassNames(CssStyles.BORDERED_PANEL, CssStyles.PADDING_COMPACT_NARROW);
        lockBar.setVisible(false);

        VerticalLayout content = new VerticalLayout(lockBar, dock);
        content.setFlexGrow(1, dock);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.addClassNames(CssStyles.OVERFLOW_HIDDEN, CssStyles.COMPACT_SIZES);
        add(content);

        Button close = new Button(loc.getValue(L.LABEL_CLOSE), event -> close());
        close.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(close);

        addOpenedChangeListener(event -> {
            if (!event.isOpened()) {
                // the tab each sheet shows is saved only now, not with every click
                saveConfig();
                if (onClose != null) {
                    onClose.run();
                }
            }
        });
        DialogFocus.onOpen(this, searchField);
        addAttachListener(event -> {
            if (loreChanges != null) {
                loreChanges.remove();
            }
            loreChanges = LoreChangesOnPage.listen(event.getUI(), change -> loreChanged(change.lorebookIds(),
                    change.entryUuids(), change.source() == null));
        });
        addDetachListener(event -> {
            if (loreChanges != null) {
                loreChanges.remove();
                loreChanges = null;
            }
        });

        // the list as it was left: with the sub lorebooks or not
        includeSubbooks.setValue(storedSubbooks());
        targetBook.setVisible(includeSubbooks.getValue());
        loadEntries(false);
        restoreConfig();
        dock.setOnLayoutChange(this::saveConfig);
        checkLock();
        return this;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "newEntry", type = Button.class),
                    @APILocals(name = "refresh", type = Button.class),
                    @APILocals(name = "massEdit", type = Button.class),
                    @APILocals(name = "tagEntries", type = Button.class),
                    @APILocals(name = "preview", type = Button.class),
                    @APILocals(name = "toolbar", type = HorizontalLayout.class),
                    @APILocals(name = "list", type = VerticalLayout.class)})
    private Component createList() {
        Button newEntry = new Button(loc.getValue(L.LABEL_ADD_ENTRY), Solid.PLUS_CIRCLE.create(), event -> createEntry());
        newEntry.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newEntryButton = newEntry;

        searchField = new TextField();
        searchField.setPlaceholder(loc.getValue(L.LABEL_SEARCH));
        searchField.setPrefixComponent(Solid.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(event -> applyFilter());

        tagFilter = LorebookEntryFilter.createTagBox(loc);
        tagFilter.setWidth("140px");
        tagFilter.addValueChangeListener(event -> applyFilter());

        statusFilter = LorebookEntryFilter.createStatusBox(loc);
        statusFilter.setWidth("120px");
        statusFilter.addValueChangeListener(event -> applyFilter());

        // the list is narrow beside the open entries: the tools are icons with a tooltip
        Button refresh = new Button(Solid.REFRESH.create(), event -> refresh());
        iconOnly(refresh, loc.getValue(L.LABEL_REFRESH));

        Button massEdit = new Button(Solid.TABLE.create(), event -> openMassEdit());
        iconOnly(massEdit, loc.getValue(L.LABEL_MASS_EDIT));
        massEditButton = massEdit;
        Button tagEntries = new Button(Solid.TAGS.create(), event -> openTagEntries());
        iconOnly(tagEntries, loc.getValue(L.LABEL_TAG_ENTRIES));
        tagEntriesButton = tagEntries;
        Button preview = new Button(Solid.EYE.create(), event -> openActivationPreview());
        iconOnly(preview, loc.getValue(L.LABEL_PREVIEW_ACTIVATION));

        includeSubbooks = new Checkbox(loc.getValue(L.LABEL_INCLUDE_SUB_LOREBOOKS));
        includeSubbooks.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                subbooksChanged();
            }
        });
        // where Add Entry puts a new entry while the sub lorebooks are listed
        targetBook = new ComboBox<>();
        targetBook.setItemLabelGenerator(book -> StringUtils.defaultString(book.getName()));
        targetBook.setAllowCustomValue(false);
        targetBook.setWidth("10em");
        targetBook.setAriaLabel(loc.getValue(L.LABEL_LOREBOOK));
        targetBook.setTooltipText(loc.getValue(L.LABEL_LOREBOOK));
        targetBook.setVisible(false);

        HorizontalLayout toolbar = new HorizontalLayout(newEntry, targetBook, massEdit, tagEntries, preview, refresh, searchField,
                tagFilter, statusFilter, includeSubbooks);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setWrap(true);
        toolbar.getThemeList().set("spacing-xs", true);
        toolbar.setSpacing(false);

        grid = new Grid<>(LorebookEntry.class, false);
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        // the column of the check mark is as narrow as the mark, it needs no title (the tooltip of the cell says it)
        grid.addComponentColumn(this::enabledMark).setFlexGrow(0).setWidth("2.5em");
        grid.addComponentColumn(this::nameCell).setHeader(loc.getValue(L.LABEL_ENTRY_NAME))
                .setComparator(entry -> StringUtils.defaultString(entry.getName()).toLowerCase()).setFlexGrow(1);
        // a narrow list beside the open entries: the tags and the filter are in the entry tab
        grid.addColumn(LorebookEntry::getOrder).setHeader(loc.getValue(L.LABEL_ORDER)).setSortable(true)
                .setFlexGrow(0).setWidth("5.5em");
        grid.addComponentColumn(this::rowActions).setHeader("").setFlexGrow(0).setWidth("5.5em");
        grid.addItemDoubleClickListener(event -> openEntry(event.getItem(), false));

        VerticalLayout list = new VerticalLayout(toolbar, grid);
        list.setSizeFull();
        list.setPadding(false);
        list.setSpacing(false);
        list.getThemeList().set("spacing-xs", true);
        list.setFlexGrow(1, grid);
        return list;
    }

    private static void iconOnly(Button button, String text) {
        button.setTooltipText(text);
        button.setAriaLabel(text);
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "open", type = Button.class),
                    @APILocals(name = "openNew", type = Button.class),
                    @APILocals(name = "delete", type = Button.class),
                    @APILocals(name = "actions", type = HorizontalLayout.class)})
    private Component rowActions(LorebookEntry entry) {
        Button open = new Button(Solid.PEN.create(), event -> openEntry(entry, false));
        open.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        open.setTooltipText(loc.getValue(L.LABEL_OPEN));
        open.setAriaLabel(loc.getValue(L.LABEL_OPEN));

        Button openNew = new Button(Solid.ARROW_UP_RIGHT_FROM_SQUARE.create(), event -> openEntry(entry, true));
        openNew.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        openNew.setTooltipText(loc.getValue(L.LABEL_OPEN_IN_NEW_TAB));
        openNew.setAriaLabel(loc.getValue(L.LABEL_OPEN_IN_NEW_TAB));

        Button delete = new Button(Solid.TRASH.create(), event -> deleteEntry(entry));
        delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ERROR);
        delete.setEnabled(lockedBy == null);
        delete.setTooltipText(loc.getValue(L.LABEL_DELETE));
        delete.setAriaLabel(loc.getValue(L.LABEL_DELETE));

        // the icons without padding, a few pixels apart
        HorizontalLayout actions = new HorizontalLayout(open, openNew, delete);
        actions.setSpacing(false);
        actions.addClassName(CssStyles.GAP_S);
        return actions;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "mark", type = Component.class)})
    private Component enabledMark(LorebookEntry entry) {
        Component mark = entry.isEnabled() ? Solid.CHECK.create() : new Span();
        if (entry.isEnabled()) {
            UIUtils.addTooltip(mark, loc.getValue(L.LABEL_ENABLED));
        }
        return mark;
    }

    /**
     * Shows the entry in a tab: the tab it already has, else the entry tab the user had last (or a new one when there is none
     * or {@code newTab}).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class),
                    @APIParameter(name = "newTab", type = boolean.class)},
            locals = {@APILocals(name = "existing", type = String.class),
                    @APILocals(name = "id", type = String.class)})
    void openEntry(LorebookEntry entry, boolean newTab) {
        String existing = tabOf(entry);
        if (existing != null) {
            dock.select(existing);
            touch(existing);
            return;
        }
        String id;
        if (!newTab && !recent.isEmpty()) {
            id = recent.peekFirst();
        } else {
            id = ENTRY_TAB + nextTab++;
        }
        showInTab(id, entry);
        saveConfig();
    }

    /**
     * Shows the entry in the tab with this id, a new closable one or the one that showed another entry (it is replaced where it
     * is).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "id", type = String.class),
                    @APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "with", type = String.class),
                    @APILocals(name = "editor", type = LorebookEntryEditor[].class)})
    private void showInTab(String id, LorebookEntry entry) {
        dock.setClosable(id, true);
        // a new tab goes beside the entry tab used last, the first one into a column of its own right of the list
        String with = recent.stream().filter(other -> !other.equals(id) && tabs.containsKey(other)).findFirst().orElse(null);
        dock.place(id, with, LIST_TAB);
        LorebookEntryEditor[] editor = new LorebookEntryEditor[1];
        editor[0] = new LorebookEntryEditor(entry, lorebookOf(entry), tokens, () -> entryChanged(editor[0])).create();
        editor[0].setOnLocked(agent -> locked(agent, true));
        editor[0].setOnGone(() -> entryGone(editor[0]));
        // a link opens the entry as Open does (the reused tab) or as Open in new tab
        editor[0].setLinks(() -> entries, this::openEntry);
        editor[0].setReadOnly(lockedBy != null);
        editor[0].setEditorMode(modes.get(id));
        editor[0].setOnModeChange(mode -> {
            modes.put(id, mode);
            saveConfig();
        });
        tabs.put(id, editor[0]);
        dock.add(id, caption(entry), editor[0]);
        dock.select(id);
        touch(id);
    }

    /**
     * Opens the tabs that were open when the editor was closed last (those whose entries are still there), arranged as they
     * were.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "stored", type = JsonElement.class),
                    @APILocals(name = "config", type = JsonObject.class),
                    @APILocals(name = "byUuid", type = Map.class),
                    @APILocals(name = "open", type = List.class),
                    @APILocals(name = "element", type = JsonElement.class),
                    @APILocals(name = "tab", type = JsonObject.class),
                    @APILocals(name = "id", type = String.class),
                    @APILocals(name = "uuid", type = String.class),
                    @APILocals(name = "mode", type = MarkdownEditor.Mode.class),
                    @APILocals(name = "entry", type = LorebookEntry.class)})
    private void restoreConfig() {
        JsonElement stored = lorebook.getAttributes() == null ? null : lorebook.getAttributes().get(CONFIG_ATTRIBUTE);
        if (stored == null || !stored.isJsonObject()) {
            return;
        }
        JsonObject config = stored.getAsJsonObject();
        restoring = true;
        try {
            if (config.has(KEY_LAYOUT) && config.get(KEY_LAYOUT).isJsonObject()) {
                dock.setLayout(config.getAsJsonObject(KEY_LAYOUT));
            }
            Map<String, LorebookEntry> byUuid = new HashMap<>();
            entries.forEach(entry -> byUuid.put(entry.getUuid(), entry));
            List<String[]> open = new ArrayList<>();
            if (config.has(KEY_TABS) && config.get(KEY_TABS).isJsonArray()) {
                for (JsonElement element : config.getAsJsonArray(KEY_TABS)) {
                    if (!element.isJsonObject()) {
                        continue;
                    }
                    JsonObject tab = element.getAsJsonObject();
                    String id = tab.has(KEY_TAB) && tab.get(KEY_TAB).isJsonPrimitive() ? tab.get(KEY_TAB).getAsString() : null;
                    String uuid = tab.has(KEY_ENTRY) && tab.get(KEY_ENTRY).isJsonPrimitive() ? tab.get(KEY_ENTRY).getAsString() : null;
                    if (id != null && uuid != null && id.startsWith(ENTRY_TAB)) {
                        open.add(new String[]{id, uuid});
                        MarkdownEditor.Mode mode = tab.has(KEY_MODE) && tab.get(KEY_MODE).isJsonPrimitive()
                                ? EnumUtils.getEnum(MarkdownEditor.Mode.class, tab.get(KEY_MODE).getAsString()) : null;
                        if (mode != null) {
                            modes.put(id, mode);
                        }
                    }
                }
            }
            // the last used one is first: opened last, so it ends up first in recent
            for (int i = open.size() - 1; i >= 0; i--) {
                String id = open.get(i)[0];
                LorebookEntry entry = byUuid.get(open.get(i)[1]);
                if (entry == null || tabs.containsKey(id) || tabOf(entry) != null) {
                    continue;
                }
                showInTab(id, entry);
                nextTab = Math.max(nextTab, slotNumber(id) + 1);
            }
            if (config.has(KEY_STATE) && config.get(KEY_STATE).isJsonObject()) {
                dock.restoreState(config.getAsJsonObject(KEY_STATE));
            }
        } finally {
            restoring = false;
        }
    }

    private static int slotNumber(String id) {
        try {
            return Integer.parseInt(id.substring(ENTRY_TAB.length()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Keeps the arrangement in the lorebook, written into the lorebook as the database has it now (the copy of the editor may be
     * older than a change made elsewhere).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "config", type = JsonObject.class),
                    @APILocals(name = "open", type = JsonArray.class),
                    @APILocals(name = "id", type = String.class),
                    @APILocals(name = "editor", type = LorebookEntryEditor.class),
                    @APILocals(name = "tab", type = JsonObject.class),
                    @APILocals(name = "fresh", type = Lorebook.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void saveConfig() {
        if (restoring || lorebook.getUuid() == null) {
            return;
        }
        JsonObject config = new JsonObject();
        config.add(KEY_LAYOUT, dock.getLayout());
        config.add(KEY_STATE, dock.storeState());
        JsonArray open = new JsonArray();
        for (String id : recent) {
            LorebookEntryEditor editor = tabs.get(id);
            if (editor != null && editor.getEntry().getUuid() != null) {
                JsonObject tab = new JsonObject();
                tab.addProperty(KEY_TAB, id);
                tab.addProperty(KEY_ENTRY, editor.getEntry().getUuid());
                tab.addProperty(KEY_MODE, editor.getEditorMode().name());
                open.add(tab);
            }
        }
        config.add(KEY_TABS, open);
        config.addProperty(KEY_SUBBOOKS, includeSubbooks.getValue());
        try {
            Lorebook fresh = lorebookService.findForUser(lorebook.getUuid());
            if (fresh == null) {
                return;
            }
            fresh.getAttributes().add(CONFIG_ATTRIBUTE, config);
            lorebookService.save(fresh);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "tab", type = Map.Entry.class)})
    private String tabOf(LorebookEntry entry) {
        for (Map.Entry<String, LorebookEntryEditor> tab : tabs.entrySet()) {
            if (Objects.equals(tab.getValue().getEntry().getUuid(), entry.getUuid())) {
                return tab.getKey();
            }
        }
        return null;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "name", type = String.class),
                    @APILocals(name = "book", type = Lorebook.class)})
    private String caption(LorebookEntry entry) {
        String name = StringUtils.defaultIfBlank(entry.getName(), loc.getValue(L.LABEL_NEW_LOREBOOK_ENTRY));
        Lorebook book = lorebookOf(entry);
        // an entry of a sub lorebook says which one
        return Objects.equals(book.getId(), lorebook.getId()) ? name : name + " (" + StringUtils.defaultString(book.getName()) + ")";
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "id", type = String.class)})
    private void touch(String id) {
        recent.remove(id);
        recent.addFirst(id);
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "current", type = String.class)})
    private void tabShown() {
        if (!restoring) {
            // a cheap query: a run that started or ended since shows when the user moves on
            checkLock();
        }
        String current = dock.getCurrentId();
        if (current != null && tabs.containsKey(current)) {
            touch(current);
        }
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "id", type = String.class)})
    private void tabClosed(String id) {
        tabs.remove(id);
        recent.remove(id);
        saveConfig();
    }

    /**
     * An entry tab saved a change that the list or the totals show.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "editor", type = LorebookEntryEditor.class)},
            locals = {@APILocals(name = "shown", type = LorebookEntry.class)})
    private void entryChanged(LorebookEntryEditor editor) {
        // the tab may have loaded its entry again: the list shows the object of the tab
        LorebookEntry shown = editor.getEntry();
        entries.replaceAll(entry -> Objects.equals(entry.getUuid(), shown.getUuid()) ? shown : entry);
        tabs.forEach((id, open) -> {
            if (open == editor) {
                dock.setCaption(id, caption(editor.getEntry()));
            }
        });
        if (entryProvider != null) {
            entryProvider.refreshAll();
        }
        updateTotals();
        // a changed content or name may name other entries now
        tabs.values().forEach(LorebookEntryEditor::showMentions);
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "entry", type = LorebookEntry.class),
                    @APILocals(name = "target", type = Lorebook.class),
                    @APILocals(name = "uuid", type = String.class),
                    @APILocals(name = "listed", type = LorebookEntry.class),
                    @APILocals(name = "e", type = Exception.class)})
    void createEntry() {
        if (checkLock()) {
            return;
        }
        try {
            LorebookEntry entry = new LorebookEntry();
            Lorebook target = includeSubbooks.getValue() && targetBook.getValue() != null ? targetBook.getValue() : lorebook;
            entry.setLorebook(target);
            entry.setName(loc.getValue(L.LABEL_NEW_LOREBOOK_ENTRY));
            entry.setEnabled(true);
            entry.setOrder(100);
            entry = lorebookEntryService.save(entry);
            loadEntries(false);
            String uuid = entry.getUuid();
            LorebookEntry listed = entries.stream().filter(e -> Objects.equals(e.getUuid(), uuid)).findFirst().orElse(entry);
            openEntry(listed, false);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Whether an agent works on the lorebook now: the editor goes read only, or editable again (and loads the entries,
     * which the run may have changed) when the run let go.
     *
     * @return whether it is locked
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = boolean.class),
            locals = {@APILocals(name = "run", type = AgentRun.class),
                    @APILocals(name = "e", type = Exception.class)})
    public boolean checkLock() {
        try {
            AgentRun run = lorebook.getId() == null ? null : agentRunService.findLockingRun(lorebook);
            if (run != null) {
                locked(run.getAgent().getName(), false);
                return true;
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return lockedBy != null;
        }
        if (lockedBy != null) {
            lockedBy = null;
            lockBar.setVisible(false);
            tabs.values().forEach(editor -> editor.setReadOnly(false));
            newEntryButton.setEnabled(true);
            searchReplaceButton.setEnabled(true);
            massEditButton.setEnabled(true);
            tagEntriesButton.setEnabled(true);
            refresh();
        }
        return false;
    }

    /**
     * An agent works on the lorebook: read only until {@link #checkLock} finds it free. {@code refused} when a save of a
     * tab was refused, the change that was typed last is not saved.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "agent", type = String.class),
                    @APIParameter(name = "refused", type = boolean.class)},
            locals = {@APILocals(name = "text", type = String.class)})
    private void locked(String agent, boolean refused) {
        boolean wasLocked = lockedBy != null;
        lockedBy = agent;
        String text = String.format(loc.getValue(L.ERROR_LOREBOOK_LOCKED), agent);
        if (refused || (wasLocked && lockText.getText().endsWith(loc.getValue(L.MSG_LAST_CHANGE_NOT_SAVED)))) {
            text += " " + loc.getValue(L.MSG_LAST_CHANGE_NOT_SAVED);
        }
        lockText.setText(text);
        lockBar.setVisible(true);
        tabs.values().forEach(editor -> editor.setReadOnly(true));
        newEntryButton.setEnabled(false);
        searchReplaceButton.setEnabled(false);
        massEditButton.setEnabled(false);
        tagEntriesButton.setEnabled(false);
        if (!wasLocked && entryProvider != null) {
            // the delete buttons of the rows
            entryProvider.refreshAll();
        }
    }

    /**
     * Entries were written elsewhere (another window, an agent): their tabs show them as they are now, a tab of a deleted one
     * closes, the list is loaded again. A write of a worker (an agent) may also mean that an agent took the lorebook.
     *
     * @param lorebookIds the lorebooks of the entries
     * @param entryUuids  the entries
     * @param byWorker    whether a worker wrote them, not a page
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "lorebookIds", type = Set.class),
                    @APIParameter(name = "entryUuids", type = Set.class),
                    @APIParameter(name = "byWorker", type = boolean.class)},
            locals = {@APILocals(name = "touched", type = List.class)})
    public void loreChanged(Set<Long> lorebookIds, Set<String> entryUuids, boolean byWorker) {
        // an entry moved away is known by its tab
        List<LorebookEntryEditor> touched = tabs.values().stream()
                .filter(editor -> entryUuids.contains(editor.getEntry().getUuid())).toList();
        if (books.stream().noneMatch(book -> lorebookIds.contains(book.getId())) && touched.isEmpty()) {
            return;
        }
        if (byWorker) {
            checkLock();
        }
        touched.forEach(LorebookEntryEditor::changedElsewhere);
        loadEntries(false);
    }

    /**
     * A tab found its entry deleted elsewhere: the tab closes, the list loses it.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "editor", type = LorebookEntryEditor.class)})
    private void entryGone(LorebookEntryEditor editor) {
        tabs.entrySet().stream().filter(tab -> tab.getValue() == editor).map(Map.Entry::getKey).findFirst()
                .ifPresent(dock::close);
        Notification.warning(loc.getValue(L.MSG_ENTRY_DELETED_ELSEWHERE));
        loadEntries(false);
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)})
    private void deleteEntry(LorebookEntry entry) {
        if (checkLock()) {
            return;
        }
        ConfirmDialog.show(loc.getValue(L.MSG_CONFIRM_DELETE), () -> {
            try {
                lorebookEntryService.delete(entry, false);
                String id = tabOf(entry);
                if (id != null) {
                    dock.close(id);
                }
                loadEntries(false);
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
    }

    /**
     * Shows the entry in a tab as {@code Open} would (the editor was opened on it, from a row of the lorebook view).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "listed", type = LorebookEntry.class)})
    public void showEntry(LorebookEntry entry) {
        if (entry == null) {
            return;
        }
        // the object of the list, so the tab and the list share it
        LorebookEntry listed = entries.stream().filter(e -> Objects.equals(e.getUuid(), entry.getUuid())).findFirst().orElse(null);
        if (listed != null) {
            openEntry(listed, false);
        }
    }

    /**
     * The mass edit of the entries of the lorebook and its subbooks; the list and the open tabs show the changes.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = LorebookMassEditDialog.class)})
    private void openMassEdit() {
        if (checkLock()) {
            return;
        }
        LorebookMassEditDialog dialog = new LorebookMassEditDialog(lorebook, this::refresh);
        dialog.create();
        dialog.open();
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = MultiTagDialog.class)})
    private void openTagEntries() {
        if (checkLock()) {
            return;
        }
        MultiTagDialog dialog = new MultiTagDialog(lorebook, this::refresh);
        dialog.create();
        dialog.open();
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = LorebookActivationPreviewDialog.class)})
    private void openActivationPreview() {
        LorebookActivationPreviewDialog dialog = new LorebookActivationPreviewDialog(lorebook);
        dialog.create();
        dialog.open();
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = LorebookSearchReplaceDialog.class)})
    private void openSearchReplace() {
        LorebookSearchReplaceDialog dialog = new LorebookSearchReplaceDialog(lorebook, false);
        dialog.onReplaced(this::refresh);
        dialog.open();
    }

    /**
     * Loads the entries of the lorebook. An entry that is open in a tab stays the object of its tab (the tab and the list show
     * the same entry), unless {@code fromDatabase}: then the tabs are {@linkplain #reloadTabs reloaded} after it.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "fromDatabase", type = boolean.class)},
            locals = {@APILocals(name = "loaded", type = List.class),
                    @APILocals(name = "book", type = Lorebook.class),
                    @APILocals(name = "entry", type = LorebookEntry.class),
                    @APILocals(name = "target", type = Lorebook.class),
                    @APILocals(name = "open", type = Map.class),
                    @APILocals(name = "tagNames", type = List.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void loadEntries(boolean fromDatabase) {
        try {
            books = includeSubbooks != null && includeSubbooks.getValue() ? collectBooks() : new ArrayList<>(List.of(lorebook));
            List<LorebookEntry> loaded = new ArrayList<>();
            entryBooks.clear();
            for (Lorebook book : books) {
                for (LorebookEntry entry : lorebookEntryService.getEntriesForLorebook(book.getId())) {
                    loaded.add(entry);
                    entryBooks.put(entry.getId(), book);
                }
            }
            Lorebook target = targetBook.getValue();
            targetBook.setItems(books);
            targetBook.setValue(target != null && books.stream().anyMatch(book -> Objects.equals(book.getId(), target.getId()))
                    ? books.stream().filter(book -> Objects.equals(book.getId(), target.getId())).findFirst().orElse(lorebook)
                    : books.getFirst());
            if (!fromDatabase) {
                Map<String, LorebookEntry> open = new HashMap<>();
                tabs.values().forEach(editor -> open.put(editor.getEntry().getUuid(), editor.getEntry()));
                loaded.replaceAll(entry -> open.getOrDefault(entry.getUuid(), entry));
            }
            entries = new ArrayList<>(loaded);
            entryTags = LorebookEntryFilter.loadTags(tagRelationService, entries);
            entryFilter.setTags(entryTags);
            List<String> tagNames = LorebookEntryFilter.tagNames(tagRelationService, entries);
            tagFilter.setItems(tagNames);
            if (tagFilter.getValue() != null && !tagNames.contains(tagFilter.getValue())) {
                tagFilter.clear();
            }
            entryProvider = new ListDataProvider<>(entries);
            entryProvider.setFilter(entryFilter::matches);
            grid.setItems(entryProvider);
            tokens.clear();
            updateTotals();
            tabs.values().forEach(LorebookEntryEditor::showMentions);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Shows the entries of the tabs as the list has them now (loaded from the database), closes the tabs of the entries that
     * are gone.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "byUuid", type = Map.class),
                    @APILocals(name = "tab", type = Map.Entry.class),
                    @APILocals(name = "fresh", type = LorebookEntry.class),
                    @APILocals(name = "open", type = Map.class)})
    private void reloadTabs() {
        Map<String, LorebookEntry> byUuid = new HashMap<>();
        entries.forEach(entry -> byUuid.put(entry.getUuid(), entry));
        for (Map.Entry<String, LorebookEntryEditor> tab : new ArrayList<>(tabs.entrySet())) {
            LorebookEntry fresh = byUuid.get(tab.getValue().getEntry().getUuid());
            if (fresh == null) {
                dock.close(tab.getKey());
            } else {
                // the tab keeps its object, the list takes it
                tab.getValue().reload(fresh);
                dock.setCaption(tab.getKey(), caption(fresh));
            }
        }
        Map<String, LorebookEntry> open = new HashMap<>();
        tabs.values().forEach(editor -> open.put(editor.getEntry().getUuid(), editor.getEntry()));
        entries.replaceAll(entry -> open.getOrDefault(entry.getUuid(), entry));
        entryProvider.refreshAll();
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class))
    private void applyFilter() {
        entryFilter.setText(searchField.getValue());
        entryFilter.setTag(tagFilter.getValue());
        entryFilter.setStatus(statusFilter.getValue());
        if (entryProvider != null) {
            entryProvider.setFilter(entryFilter::matches);
        }
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void updateTotals() {
        withSubbooksSpan.setText("");
        try {
            // the total of this lorebook; Count with subbooks has the rest
            totalSpan.setText(tokens.format(tokens.ofEnabled(entries.stream()
                    .filter(entry -> Objects.equals(lorebookOf(entry).getId(), lorebook.getId())).toList())));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * The lorebook of a listed entry: this one or one of its sub lorebooks (the entry's own reference is a lazy one).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Lorebook.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "book", type = Lorebook.class)})
    public Lorebook lorebookOf(LorebookEntry entry) {
        Lorebook book = entry == null || entry.getId() == null ? null : entryBooks.get(entry.getId());
        return book == null ? lorebook : book;
    }

    /**
     * The lorebook and every lorebook below it, each once, the nearest first (as the generation finds them).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = List.class),
            locals = {@APILocals(name = "found", type = List.class),
                    @APILocals(name = "seen", type = Set.class),
                    @APILocals(name = "queue", type = Deque.class),
                    @APILocals(name = "book", type = Lorebook.class)})
    private List<Lorebook> collectBooks() throws Exception {
        List<Lorebook> found = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        Deque<Lorebook> queue = new ArrayDeque<>();
        queue.add(lorebook);
        while (!queue.isEmpty()) {
            Lorebook book = queue.remove();
            if (book.isDeleted() || !seen.add(book.getId())) {
                continue;
            }
            found.add(book);
            queue.addAll(lorebookService.getSubbooks(book));
        }
        return found;
    }

    /**
     * The user listed the sub lorebooks or stopped: the list is loaded again (open tabs stay), the choice is kept.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class))
    private void subbooksChanged() {
        targetBook.setVisible(includeSubbooks.getValue());
        loadEntries(false);
        saveConfig();
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = boolean.class),
            locals = {@APILocals(name = "stored", type = JsonElement.class),
                    @APILocals(name = "flag", type = JsonElement.class)})
    private boolean storedSubbooks() {
        JsonElement stored = lorebook.getAttributes() == null ? null : lorebook.getAttributes().get(CONFIG_ATTRIBUTE);
        if (stored == null || !stored.isJsonObject()) {
            return false;
        }
        JsonElement flag = stored.getAsJsonObject().get(KEY_SUBBOOKS);
        return flag != null && flag.isJsonPrimitive() && flag.getAsBoolean();
    }

    /**
     * The name of the entry, and the lorebook of an entry of a sub lorebook (muted).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "cell", type = Span.class),
                    @APILocals(name = "book", type = Lorebook.class),
                    @APILocals(name = "bookName", type = Span.class)})
    private Component nameCell(LorebookEntry entry) {
        Span cell = new Span(StringUtils.defaultString(entry.getName()));
        Lorebook book = lorebookOf(entry);
        if (!Objects.equals(book.getId(), lorebook.getId())) {
            Span bookName = new Span(" · " + StringUtils.defaultString(book.getName()));
            bookName.addClassName(CssStyles.HINT_TEXT);
            cell.add(bookName);
        }
        return cell;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void countWithSubbooks() {
        try {
            withSubbooksSpan.setText(tokens.formatWithSubbooks(tokens.withSubbooks(lorebook)));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    // for the tests

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Checkbox.class))
    Checkbox getIncludeSubbooks() {
        return includeSubbooks;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = ComboBox.class))
    ComboBox<Lorebook> getTargetBook() {
        return targetBook;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = TabDock.class))
    TabDock getDock() {
        return dock;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Grid.class))
    Grid<LorebookEntry> getGrid() {
        return grid;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Map.class))
    Map<String, LorebookEntryEditor> getTabs() {
        return tabs;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Span.class))
    Span getTotalSpan() {
        return totalSpan;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = HorizontalLayout.class))
    HorizontalLayout getLockBar() {
        return lockBar;
    }
}
