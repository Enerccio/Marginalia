package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.Restorable;
import com.github.enerccio.marginalia.ui.RestorableState;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dnd.DragSource;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.dnd.EffectAllowed;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Tab sheets that the user can take apart: a tab dragged out of its sheet either joins another sheet (dropped on a tab or
 * on the sheet) or becomes a sheet of its own (dropped on one of the strips that show up while dragging). The sheets are
 * arranged in columns next to each other, the sheets of a column are stacked under each other: a strip between the sheets of
 * a column makes a new sheet in that column, a strip between the columns (and at the sides) makes a new column. Splitters
 * between the sheets and between the columns let the user resize them, the sizes are a part of the layout. A sheet that
 * loses its last tab is gone, and so is a column without sheets. Used by the story editor, whose tabs are the story itself, the
 * outline and the other sidebar tabs (extensions add theirs with {@link #add}), a dock that is not {@code detachable} is just
 * one tab sheet. A tab sheet hides the content of the tabs it does not show and disables it, so {@link #select} is how code
 * shows a tab.
 * <p>
 * The tabs are identified by an id that is the same in every session: the {@linkplain #getLayout() layout} (which tabs are
 * together, in which order) names tabs by it and can be {@linkplain #setLayout stored and applied} later, even when
 * only some of its tabs are there at the moment: an extension adds its tab after the application did, so the layout keeps
 * the place of tabs that are not (yet) in the dock and a tab it does not know goes to the first sheet. What the layout
 * does not hold - the selected tab of each sheet - is the {@link Restorable} state.
 * <p>
 * A general widget: it knows nothing of the story, whoever needs detachable tabs can use it (and keep the layout where it
 * likes).
 */
@Configurable
@Extendable
public class TabDock extends Div implements Restorable {

    private static final String KEY_COLUMNS = "columns";
    // a layout of one column, as the dock stored it before it had columns
    private static final String KEY_GROUPS = "groups";
    private static final String KEY_TABS = "tabs";
    private static final String KEY_SIZE = "size";
    private static final String KEY_SELECTED = "selected";

    @Autowired
    private Localization loc;

    /**
     * A tab that is in the dock.
     */
    private static final class Entry {
        private final String id;
        private final Component content;
        private final Tab tab;
        private DropTarget<Tab> drop;

        private Entry(String id, String caption, Component content) {
            this.id = id;
            this.content = content;
            this.tab = new Tab(caption);
        }
    }

    /**
     * A sheet of the layout: the ids of its tabs in order, the ones that are in the dock have a tab in the sheet.
     */
    private static final class Group {
        private final List<String> ids = new ArrayList<>();
        private TabSheet sheet;
        private DropTarget<TabSheet> drop;
        // the tab the sheet shows, may be a tab that is not there yet
        private String selectedId;
        // percent of the column the user gave the sheet, null while the user has not resized
        private Double size;
    }

    /**
     * A column of the layout: the sheets stacked under each other.
     */
    private static final class Column {
        private final List<Group> groups = new ArrayList<>();
        // percent of the dock the user gave the column, null while the user has not resized
        private Double size;
    }

    private final boolean detachable;
    private final Map<String, Entry> entries = new LinkedHashMap<>();
    private final List<Column> columns = new ArrayList<>();
    // groups that were dropped from the layout, their sheets still have to give the tabs back
    private final List<Group> retired = new ArrayList<>();
    // the sheets that are shown, by column
    private List<List<Group>> shown = new ArrayList<>();
    private final Set<String> pendingSelection = new HashSet<>();
    private boolean tabsEnabled = true;
    // the tab that stays enabled while the others are not
    private String enabledId;
    private Entry dragged;
    // the tab the user (or select) showed last, where selectRelative goes on from
    private String currentId;
    // the sheets change by themselves while the dock arranges them, that is not a selection of the user
    private boolean arranging;
    private Runnable onLayoutChange;
    private Runnable onStateChange;

    public TabDock() {
        this(true);
    }

    /**
     * @param detachable false for a dock that is one plain tab sheet: the tabs can not be dragged
     */
    public TabDock(boolean detachable) {
        this.detachable = detachable;
        addClassName(CssStyles.TAB_DOCK);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = boolean.class))
    public boolean isDetachable() {
        return detachable;
    }

    /**
     * Adds a tab whose id is its caption, see {@link #add(String, String, Component)}.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Tab.class),
            parameters = {@APIParameter(name = "caption", type = String.class),
                    @APIParameter(name = "content", type = Component.class)})
    public Tab add(String caption, Component content) {
        return add(caption, caption, content);
    }

    /**
     * Adds the tab to the sheet the layout puts it into, the first sheet when the layout does not know it. A tab with the
     * same id is replaced.
     *
     * @param id      identifies the tab in the layout, it must not change between sessions (not the localized caption)
     * @param caption text of the tab
     * @param content what the tab shows
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Tab.class),
            parameters = {@APIParameter(name = "id", type = String.class),
                    @APIParameter(name = "caption", type = String.class),
                    @APIParameter(name = "content", type = Component.class)},
            locals = {@APILocals(name = "old", type = TabDock.Entry.class),
                    @APILocals(name = "entry", type = TabDock.Entry.class),
                    @APILocals(name = "group", type = TabDock.Group.class)})
    public Tab add(String id, String caption, Component content) {
        Entry old = entries.get(id);
        if (old != null) {
            remove(old.content);
        }
        Entry entry = new Entry(id, caption, content);
        if (detachable) {
            makeDraggable(entry);
        }
        entries.put(id, entry);
        applyEnabled(entry);

        Group group = groupOf(id);
        if (group == null) {
            group = firstGroup();
            group.ids.add(id);
        }
        if (pendingSelection.remove(id)) {
            group.selectedId = id;
        }
        arrange();
        return entry.tab;
    }

    /**
     * Shows the tab: it becomes the selected one of its sheet. For the code that is about to show something that lives in
     * the tab, whichever sheet the user has put it into.
     *
     * @return false when there is no tab with this id in the dock
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = boolean.class),
            parameters = {@APIParameter(name = "id", type = String.class)},
            locals = {@APILocals(name = "entry", type = TabDock.Entry.class),
                    @APILocals(name = "group", type = TabDock.Group.class)})
    public boolean select(String id) {
        Entry entry = entries.get(id);
        Group group = entry == null ? null : groupOf(id);
        if (group == null) {
            return false;
        }
        currentId = id;
        if (id.equals(group.selectedId) && group.sheet != null && group.sheet.getSelectedTab() == entry.tab) {
            return true;
        }
        group.selectedId = id;
        arrange();
        stateChanged();
        return true;
    }

    /**
     * Shows the tab after (or before) the one shown last, going through the tabs of all sheets in the order of the layout
     * (the columns from the left, the sheets of a column from the top) and round from the last to the first. Tabs that are
     * disabled are skipped. Goes on from the tab on the screen (the first sheet's selected one) when none was shown yet.
     *
     * @param step 1 for the next tab, -1 for the previous one
     * @return false when there is no tab to show
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = boolean.class),
            parameters = {@APIParameter(name = "step", type = int.class)},
            locals = {@APILocals(name = "order", type = List.class),
                    @APILocals(name = "group", type = TabDock.Group.class),
                    @APILocals(name = "id", type = String.class),
                    @APILocals(name = "entry", type = TabDock.Entry.class)})
    public boolean selectRelative(int step) {
        List<String> order = new ArrayList<>();
        for (Group group : allGroups()) {
            for (String id : group.ids) {
                Entry entry = entries.get(id);
                if (entry != null && entry.tab.isEnabled()) {
                    order.add(id);
                }
            }
        }
        if (order.isEmpty()) {
            return false;
        }
        int at = order.indexOf(currentId);
        for (Group group : allGroups()) {
            if (at >= 0) {
                break;
            }
            // none shown by a call or a click yet: go on from the tab on the screen, the selected one of the first sheet
            Entry entry = group.sheet == null ? null : entryOf(group.sheet.getSelectedTab());
            at = entry == null ? -1 : order.indexOf(entry.id);
        }
        return select(order.get(at < 0 ? 0 : Math.floorMod(at + step, order.size())));
    }

    /**
     * Enables or disables the contents and the tabs of the dock, except for one tab that is not touched (the content of the
     * dock that has to go on working while the rest does not). Tabs added later get the state too.
     *
     * @param exceptId the tab that is always enabled, may be null
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "enabled", type = boolean.class),
                    @APIParameter(name = "exceptId", type = String.class)})
    public void setTabsEnabled(boolean enabled, String exceptId) {
        tabsEnabled = enabled;
        enabledId = exceptId;
        entries.values().forEach(this::applyEnabled);
    }

    /**
     * The tab sheet enables the content of the tab it selects and disables the content of the others by itself, so the content
     * is only touched where that matters: disabled for a tab that is to be disabled (it can be the selected one), enabled again
     * when the tab is the selected one of its sheet.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "entry", type = TabDock.Entry.class)},
            locals = {@APILocals(name = "group", type = TabDock.Group.class)})
    private void applyEnabled(Entry entry) {
        boolean enabled = tabsEnabled || entry.id.equals(enabledId);
        entry.tab.setEnabled(enabled);
        if (!enabled) {
            entry.content.getElement().setEnabled(false);
            return;
        }
        Group group = groupOf(entry.id);
        if (group != null && group.sheet != null && group.sheet.getSelectedTab() == entry.tab) {
            entry.content.getElement().setEnabled(true);
        }
    }

    /**
     * Removes the tab with this content, wherever the user has moved it to. Its place in the layout is kept for when it
     * is added again.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "content", type = Component.class)},
            locals = {@APILocals(name = "entry", type = TabDock.Entry.class),
                    @APILocals(name = "group", type = TabDock.Group.class)})
    public void remove(Component content) {
        Entry entry = entries.values().stream().filter(candidate -> candidate.content == content).findFirst().orElse(null);
        if (entry == null) {
            return;
        }
        entries.remove(entry.id);
        Group group = groupOf(entry.id);
        if (group != null && group.sheet != null && group.sheet.getIndexOf(entry.tab) >= 0) {
            arranging = true;
            try {
                group.sheet.remove(entry.tab);
            } finally {
                arranging = false;
            }
        }
        arrange();
    }

    /**
     * @return which tabs are together in which order: {@code {"columns":[{"groups":[{"tabs":["id", ...]}, ...]}, ...]}},
     * including the tabs that are not in the dock now
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = JsonObject.class))
    public JsonObject getLayout() {
        return layoutOf(columns);
    }

    /**
     * Arranges the tabs as the layout says, the tabs it does not name are put to the first sheet. {@code null} or a layout
     * without sheets puts all tabs into one sheet. A layout with {@code groups} at the top is one column. Does not call the
     * {@linkplain #setOnLayoutChange layout listener}.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "layout", type = JsonObject.class)},
            locals = {@APILocals(name = "parsed", type = List.class),
                    @APILocals(name = "seen", type = Set.class),
                    @APILocals(name = "columnArray", type = JsonElement.class),
                    @APILocals(name = "element", type = JsonElement.class),
                    @APILocals(name = "column", type = TabDock.Column.class),
                    @APILocals(name = "id", type = String.class),
                    @APILocals(name = "selected", type = Set.class),
                    @APILocals(name = "previous", type = List.class)})
    public void setLayout(JsonObject layout) {
        if (!detachable) {
            return;
        }
        List<Column> parsed = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        JsonElement columnArray = layout == null ? null : layout.get(KEY_COLUMNS);
        if (columnArray != null && columnArray.isJsonArray()) {
            for (JsonElement element : columnArray.getAsJsonArray()) {
                if (element.isJsonObject()) {
                    Column column = parseColumn(element.getAsJsonObject(), seen);
                    if (!column.groups.isEmpty()) {
                        parsed.add(column);
                    }
                }
            }
        } else if (layout != null) {
            Column column = parseColumn(layout, seen);
            if (!column.groups.isEmpty()) {
                parsed.add(column);
            }
        }
        for (String id : entries.keySet()) {
            if (seen.add(id)) {
                if (parsed.isEmpty()) {
                    parsed.add(new Column());
                }
                if (parsed.get(0).groups.isEmpty()) {
                    parsed.get(0).groups.add(new Group());
                }
                parsed.get(0).groups.get(0).ids.add(id);
            }
        }

        Set<String> selected = new HashSet<>();
        allGroups().forEach(group -> {
            if (group.selectedId != null) {
                selected.add(group.selectedId);
            }
        });
        List<Column> previous = new ArrayList<>(columns);
        columns.clear();
        columns.addAll(parsed);
        allGroups().forEach(group -> group.ids.stream().filter(selected::contains).findFirst()
                .ifPresent(id -> group.selectedId = id));
        if (getLayout().equals(layoutOf(previous))) {
            // the same arrangement: keep the sheets as they are
            columns.clear();
            columns.addAll(previous);
            return;
        }
        previous.forEach(column -> retired.addAll(column.groups));
        arrange();
    }

    private static Column parseColumn(JsonObject object, Set<String> seen) {
        Column column = new Column();
        column.size = size(object);
        JsonElement array = object.get(KEY_GROUPS);
        if (array != null && array.isJsonArray()) {
            for (JsonElement element : array.getAsJsonArray()) {
                if (!element.isJsonObject()) {
                    continue;
                }
                Group group = new Group();
                group.size = size(element.getAsJsonObject());
                for (String id : RestorableState.strings(element.getAsJsonObject(), KEY_TABS)) {
                    if (!id.isBlank() && seen.add(id)) {
                        group.ids.add(id);
                    }
                }
                if (!group.ids.isEmpty()) {
                    column.groups.add(group);
                }
            }
        }
        return column;
    }

    private static Double size(JsonObject object) {
        JsonElement element = object.get(KEY_SIZE);
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()
                && element.getAsDouble() > 0) {
            return element.getAsDouble();
        }
        return null;
    }

    /**
     * Called after the user changed the layout by dragging a tab; {@link #getLayout()} is the new one.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "onLayoutChange", type = Runnable.class)})
    public void setOnLayoutChange(Runnable onLayoutChange) {
        this.onLayoutChange = onLayoutChange;
    }

    /**
     * Called after the user selected a tab or changed the layout, what {@link #storeState()} stores changed.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "onStateChange", type = Runnable.class)})
    public void setOnStateChange(Runnable onStateChange) {
        this.onStateChange = onStateChange;
    }

    /**
     * The selected tab of every sheet.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = JsonObject.class),
            locals = {@APILocals(name = "state", type = JsonObject.class),
                    @APILocals(name = "selected", type = List.class)})
    @Override
    public JsonObject storeState() {
        JsonObject state = new JsonObject();
        List<String> selected = new ArrayList<>();
        allGroups().forEach(group -> {
            if (group.selectedId != null && entries.containsKey(group.selectedId)) {
                selected.add(group.selectedId);
            }
        });
        if (selected.size() > 0) {
            state.add(KEY_SELECTED, RestorableState.array(selected));
        }
        return state;
    }

    /**
     * Selects the tabs; a tab that is not in the dock yet (an extension adds it later) is selected when it comes.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "self", type = JsonObject.class)},
            locals = {@APILocals(name = "id", type = String.class),
                    @APILocals(name = "group", type = TabDock.Group.class)})
    @Override
    public void restoreState(JsonObject self) {
        for (String id : RestorableState.strings(self, KEY_SELECTED)) {
            Group group = groupOf(id);
            if (group != null) {
                group.selectedId = id;
            } else {
                pendingSelection.add(id);
            }
        }
        arrange();
    }

    // ---- layout

    private static JsonObject layoutOf(List<Column> list) {
        JsonArray columnArray = new JsonArray();
        for (Column column : list) {
            JsonArray groupArray = new JsonArray();
            for (Group group : column.groups) {
                JsonObject object = new JsonObject();
                if (group.size != null) {
                    object.addProperty(KEY_SIZE, group.size);
                }
                object.add(KEY_TABS, RestorableState.array(group.ids));
                groupArray.add(object);
            }
            JsonObject columnObject = new JsonObject();
            if (column.size != null) {
                columnObject.addProperty(KEY_SIZE, column.size);
            }
            columnObject.add(KEY_GROUPS, groupArray);
            columnArray.add(columnObject);
        }
        JsonObject layout = new JsonObject();
        layout.add(KEY_COLUMNS, columnArray);
        return layout;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = List.class),
            locals = {@APILocals(name = "all", type = List.class)})
    private List<Group> allGroups() {
        List<Group> all = new ArrayList<>();
        columns.forEach(column -> all.addAll(column.groups));
        return all;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = TabDock.Group.class),
            locals = {@APILocals(name = "column", type = TabDock.Column.class)})
    private Group firstGroup() {
        if (columns.isEmpty()) {
            columns.add(new Column());
        }
        Column column = columns.get(0);
        if (column.groups.isEmpty()) {
            column.groups.add(new Group());
        }
        return column.groups.get(0);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = TabDock.Group.class),
            parameters = {@APIParameter(name = "id", type = String.class)},
            locals = {@APILocals(name = "group", type = TabDock.Group.class)})
    private Group groupOf(String id) {
        for (Group group : allGroups()) {
            if (group.ids.contains(id)) {
                return group;
            }
        }
        return null;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = TabDock.Column.class),
            parameters = {@APIParameter(name = "group", type = TabDock.Group.class)},
            locals = {@APILocals(name = "column", type = TabDock.Column.class)})
    private Column columnOf(Group group) {
        for (Column column : columns) {
            if (column.groups.contains(group)) {
                return column;
            }
        }
        return null;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = TabDock.Entry.class),
            parameters = {@APIParameter(name = "tab", type = Tab.class)},
            locals = {@APILocals(name = "entry", type = TabDock.Entry.class)})
    private Entry entryOf(Tab tab) {
        for (Entry entry : entries.values()) {
            if (entry.tab == tab) {
                return entry;
            }
        }
        return null;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = List.class),
            parameters = {@APIParameter(name = "group", type = TabDock.Group.class)},
            locals = {@APILocals(name = "present", type = List.class),
                    @APILocals(name = "id", type = String.class),
                    @APILocals(name = "entry", type = TabDock.Entry.class)})
    private List<Entry> presentEntries(Group group) {
        List<Entry> present = new ArrayList<>();
        for (String id : group.ids) {
            Entry entry = entries.get(id);
            if (entry != null) {
                present.add(entry);
            }
        }
        return present;
    }

    private static List<Tab> tabsOf(TabSheet sheet) {
        List<Tab> tabs = new ArrayList<>();
        for (int i = 0; i < sheet.getTabCount(); i++) {
            tabs.add(sheet.getTabAt(i));
        }
        return tabs;
    }

    /**
     * Makes the sheets show what the model says: takes the tabs that moved out of their sheets, puts them into the new
     * ones, creates the sheets that are needed and drops those that have no tab, and selects the remembered tabs.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "live", type = List.class),
                    @APILocals(name = "all", type = List.class),
                    @APILocals(name = "group", type = TabDock.Group.class),
                    @APILocals(name = "current", type = List.class),
                    @APILocals(name = "wanted", type = List.class),
                    @APILocals(name = "visible", type = List.class),
                    @APILocals(name = "visibleColumns", type = List.class),
                    @APILocals(name = "column", type = TabDock.Column.class),
                    @APILocals(name = "visibleGroups", type = List.class),
                    @APILocals(name = "present", type = List.class),
                    @APILocals(name = "selected", type = TabDock.Entry.class)})
    private void arrange() {
        arranging = true;
        try {
            List<Group> live = allGroups();
            List<Group> all = new ArrayList<>(live);
            all.addAll(retired);
            // first take out everything that is not where it belongs, so that a tab is in one sheet at a time
            for (Group group : all) {
                if (group.sheet == null) {
                    continue;
                }
                List<Tab> current = tabsOf(group.sheet);
                List<Tab> wanted = new ArrayList<>();
                if (live.contains(group)) {
                    presentEntries(group).forEach(entry -> wanted.add(entry.tab));
                }
                if (!(wanted.size() >= current.size() && wanted.subList(0, current.size()).equals(current))) {
                    current.forEach(group.sheet::remove);
                }
            }
            retired.forEach(group -> group.sheet = null);
            retired.clear();

            List<List<Group>> visible = new ArrayList<>();
            List<Column> visibleColumns = new ArrayList<>();
            for (Column column : columns) {
                List<Group> visibleGroups = new ArrayList<>();
                for (Group group : column.groups) {
                    List<Entry> present = presentEntries(group);
                    if (present.isEmpty()) {
                        group.sheet = null;
                        continue;
                    }
                    if (group.sheet == null) {
                        createSheet(group);
                    }
                    for (int i = group.sheet.getTabCount(); i < present.size(); i++) {
                        group.sheet.add(present.get(i).tab, present.get(i).content);
                    }
                    visibleGroups.add(group);
                }
                if (!visibleGroups.isEmpty()) {
                    visible.add(visibleGroups);
                    visibleColumns.add(column);
                }
            }
            if (!visible.equals(shown)) {
                shown = visible;
                fill(visibleColumns);
            }

            for (Group group : live) {
                Entry selected = group.selectedId == null ? null : entries.get(group.selectedId);
                if (group.sheet != null && selected != null && group.ids.contains(selected.id)
                        && group.sheet.getSelectedTab() != selected.tab) {
                    group.sheet.setSelectedTab(selected.tab);
                }
            }
            // the sheets enable and disable the contents as they select them, the state of the dock goes on top
            entries.values().forEach(this::applyEnabled);
            setDropsActive(dragged != null);
        } finally {
            arranging = false;
        }
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "group", type = TabDock.Group.class)},
            locals = {@APILocals(name = "sheet", type = TabSheet.class)})
    private void createSheet(Group group) {
        TabSheet sheet = new TabSheet();
        sheet.setWidthFull();
        sheet.addClassName(CssStyles.TAB_DOCK_PANEL);
        sheet.addSelectedChangeListener(event -> {
            Entry entry = arranging || event.getSelectedTab() == null ? null : entryOf(event.getSelectedTab());
            if (entry != null) {
                group.selectedId = entry.id;
                currentId = entry.id;
                stateChanged();
            }
        });
        group.sheet = sheet;
        if (detachable) {
            group.drop = DropTarget.create(sheet);
            group.drop.setDropEffect(DropEffect.MOVE);
            group.drop.setActive(false);
            group.drop.addDropListener(event -> dropOnGroup(group, null));
        }
    }

    /**
     * The columns of the dock, next to each other with a splitter between them, and in every column the sheets under each
     * other with splitters between them. The strips for a new column (at the sides of the columns) and for a new sheet (above
     * and under the sheets) are visible while dragging.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "visibleColumns", type = List.class)},
            locals = {@APILocals(name = "columnCells", type = List.class),
                    @APILocals(name = "column", type = TabDock.Column.class),
                    @APILocals(name = "visibleGroups", type = List.class),
                    @APILocals(name = "groupCells", type = List.class),
                    @APILocals(name = "group", type = TabDock.Group.class),
                    @APILocals(name = "cell", type = Div.class),
                    @APILocals(name = "body", type = Div.class)})
    private void fill(List<Column> visibleColumns) {
        removeAll();
        List<Component> columnCells = new ArrayList<>();
        for (int c = 0; c < visibleColumns.size(); c++) {
            Column column = visibleColumns.get(c);
            List<Group> visibleGroups = shown.get(c);

            List<Component> groupCells = new ArrayList<>();
            for (int g = 0; g < visibleGroups.size(); g++) {
                Group group = visibleGroups.get(g);
                Div cell = new Div();
                cell.addClassName(CssStyles.TAB_DOCK_CELL);
                if (detachable) {
                    cell.add(createZone(column, group));
                }
                cell.add(group.sheet);
                if (detachable && g == visibleGroups.size() - 1) {
                    cell.add(createZone(column, null));
                }
                groupCells.add(cell);
            }
            Div body = new Div(chain(groupCells, shares(visibleGroups.stream().map(group -> group.size).toList()),
                    SplitLayout.Orientation.VERTICAL, sizes -> {
                        for (int i = 0; i < sizes.length; i++) {
                            visibleGroups.get(i).size = sizes[i];
                        }
                    }));
            body.addClassName(CssStyles.TAB_DOCK_COLUMN_BODY);

            Div cell = new Div();
            cell.addClassNames(CssStyles.TAB_DOCK_CELL, CssStyles.TAB_DOCK_COLUMN);
            if (detachable) {
                cell.add(createSideZone(column));
            }
            cell.add(body);
            if (detachable && c == visibleColumns.size() - 1) {
                cell.add(createSideZone(null));
            }
            columnCells.add(cell);
        }
        if (!columnCells.isEmpty()) {
            add(chain(columnCells, shares(visibleColumns.stream().map(column -> column.size).toList()),
                    SplitLayout.Orientation.HORIZONTAL, sizes -> {
                        for (int i = 0; i < sizes.length; i++) {
                            visibleColumns.get(i).size = sizes[i];
                        }
                    }));
        }
    }

    /**
     * Puts the cells next to (or under) each other with splitters between them. A split layout has two sides, so it is a
     * chain: the first cell and a split layout of the rest, and so on; the position of each splitter is the share of its cell in
     * what is left. When the user moves a splitter, the shares of all the cells are read again and handed to {@code onResize}.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "cells", type = List.class),
                    @APIParameter(name = "shares", type = double[].class),
                    @APIParameter(name = "orientation", type = SplitLayout.Orientation.class),
                    @APIParameter(name = "onResize", type = Consumer.class)},
            locals = {@APILocals(name = "splits", type = List.class),
                    @APILocals(name = "tail", type = Component.class),
                    @APILocals(name = "split", type = SplitLayout.class)})
    private Component chain(List<Component> cells, double[] shares, SplitLayout.Orientation orientation,
                            java.util.function.Consumer<double[]> onResize) {
        int count = cells.size();
        if (count == 1) {
            return cells.get(0);
        }
        List<SplitLayout> splits = new ArrayList<>();
        Component tail = cells.get(count - 1);
        double rest = shares[count - 1];
        for (int k = count - 2; k >= 0; k--) {
            rest += shares[k];
            SplitLayout split = new SplitLayout(orientation);
            split.setSizeFull();
            split.addToPrimary(cells.get(k));
            split.addToSecondary(tail);
            split.setSplitterPosition(shares[k] / rest * 100);
            splits.add(0, split);
            tail = split;
        }
        for (SplitLayout split : splits) {
            split.addSplitterDragEndListener(event -> {
                double[] sizes = new double[count];
                double remaining = 100;
                for (int k = 0; k < count - 1; k++) {
                    Double position = splits.get(k).getSplitterPosition();
                    sizes[k] = Math.round(remaining * (position == null ? 50 : position)) / 100.0;
                    remaining -= sizes[k];
                }
                sizes[count - 1] = Math.round(remaining * 100) / 100.0;
                onResize.accept(sizes);
                if (onLayoutChange != null) {
                    onLayoutChange.run();
                }
            });
        }
        return splits.get(0);
    }

    /**
     * The shares (percent) the cells get: the sizes the user gave them, a cell without one gets the average, none too small.
     */
    private static double[] shares(List<Double> sizes) {
        double known = 0;
        int count = 0;
        for (Double size : sizes) {
            if (size != null) {
                known += size;
                count++;
            }
        }
        double average = count == 0 ? 1 : known / count;
        double[] shares = new double[sizes.size()];
        double total = 0;
        for (int i = 0; i < shares.length; i++) {
            shares[i] = Math.max(sizes.get(i) == null ? average : sizes.get(i), average * 0.1);
            total += shares[i];
        }
        for (int i = 0; i < shares.length; i++) {
            shares[i] = shares[i] / total * 100;
        }
        return shares;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Div.class),
            parameters = {@APIParameter(name = "column", type = TabDock.Column.class),
                    @APIParameter(name = "before", type = TabDock.Group.class)},
            locals = {@APILocals(name = "zone", type = Div.class),
                    @APILocals(name = "target", type = DropTarget.class)})
    private Div createZone(Column column, Group before) {
        Div zone = new Div(loc.getValue(L.LABEL_DROP_TAB_TO_DETACH));
        zone.addClassName(CssStyles.TAB_DOCK_ZONE);
        DropTarget<Div> target = DropTarget.create(zone);
        target.setDropEffect(DropEffect.MOVE);
        target.addDropListener(event -> dropAsNewGroup(column, before));
        return zone;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Div.class),
            parameters = {@APIParameter(name = "before", type = TabDock.Column.class)},
            locals = {@APILocals(name = "zone", type = Div.class),
                    @APILocals(name = "target", type = DropTarget.class)})
    private Div createSideZone(Column before) {
        Div zone = new Div(loc.getValue(L.LABEL_DROP_TAB_TO_DETACH_SIDE));
        zone.addClassNames(CssStyles.TAB_DOCK_ZONE, CssStyles.TAB_DOCK_SIDE_ZONE);
        DropTarget<Div> target = DropTarget.create(zone);
        target.setDropEffect(DropEffect.MOVE);
        target.addDropListener(event -> dropAsNewColumn(before));
        return zone;
    }

    // ---- dragging

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "entry", type = TabDock.Entry.class)},
            locals = {@APILocals(name = "source", type = DragSource.class)})
    private void makeDraggable(Entry entry) {
        entry.tab.addClassName(CssStyles.TAB_DOCK_TAB);

        DragSource<Tab> source = DragSource.create(entry.tab);
        source.setEffectAllowed(EffectAllowed.MOVE);
        source.addDragStartListener(event -> startDrag(entry));
        source.addDragEndListener(event -> finishDrag());

        entry.drop = DropTarget.create(entry.tab);
        entry.drop.setDropEffect(DropEffect.MOVE);
        entry.drop.setActive(false);
        entry.drop.addDropListener(event -> {
            Group group = groupOf(entry.id);
            if (group != null) {
                dropOnGroup(group, entry);
            }
        });
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "entry", type = TabDock.Entry.class)})
    private void startDrag(Entry entry) {
        dragged = entry;
        addClassName(CssStyles.TAB_DOCK_DRAGGING);
        setDropsActive(true);
    }

    /**
     * The end of a drag; also run by the drop, because the tab that was dropped has moved and its end event may never come.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    private void finishDrag() {
        dragged = null;
        removeClassName(CssStyles.TAB_DOCK_DRAGGING);
        setDropsActive(false);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "active", type = boolean.class)},
            locals = {@APILocals(name = "group", type = TabDock.Group.class),
                    @APILocals(name = "entry", type = TabDock.Entry.class)})
    private void setDropsActive(boolean active) {
        for (Group group : allGroups()) {
            if (group.drop != null) {
                group.drop.setActive(active);
            }
        }
        for (Entry entry : entries.values()) {
            if (entry.drop != null) {
                entry.drop.setActive(active);
            }
        }
    }

    /**
     * Takes the tab out of its sheet; a sheet left without tabs and a column left without sheets are dropped from the layout.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "moved", type = TabDock.Entry.class)},
            locals = {@APILocals(name = "from", type = TabDock.Group.class),
                    @APILocals(name = "column", type = TabDock.Column.class)})
    private void takeOut(Entry moved) {
        Group from = groupOf(moved.id);
        if (from == null) {
            return;
        }
        from.ids.remove(moved.id);
        if (moved.id.equals(from.selectedId)) {
            from.selectedId = null;
        }
        if (from.ids.isEmpty()) {
            Column column = columnOf(from);
            column.groups.remove(from);
            retired.add(from);
            if (column.groups.isEmpty()) {
                columns.remove(column);
            }
        }
    }

    /**
     * The drop of a tab on a tab or a sheet joins the sheet, before the tab it was dropped on. A drop reaches the tab and
     * then the sheet, the second one finds nothing dragged any more.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "target", type = TabDock.Group.class),
                    @APIParameter(name = "before", type = TabDock.Entry.class)},
            locals = {@APILocals(name = "moved", type = TabDock.Entry.class),
                    @APILocals(name = "layoutBefore", type = JsonObject.class)})
    private void dropOnGroup(Group target, Entry before) {
        Entry moved = dragged;
        finishDrag();
        if (moved == null || moved == before) {
            return;
        }
        JsonObject layoutBefore = getLayout();

        // within its own sheet the tab is only reordered, the sheet must not go away on the way
        if (groupOf(moved.id) == target) {
            target.ids.remove(moved.id);
        } else {
            takeOut(moved);
        }
        int index = before == null ? -1 : target.ids.indexOf(before.id);
        if (index < 0) {
            target.ids.add(moved.id);
        } else {
            target.ids.add(index, moved.id);
        }
        target.selectedId = moved.id;
        layoutMoved(layoutBefore);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "column", type = TabDock.Column.class),
                    @APIParameter(name = "before", type = TabDock.Group.class)},
            locals = {@APILocals(name = "moved", type = TabDock.Entry.class),
                    @APILocals(name = "from", type = TabDock.Group.class),
                    @APILocals(name = "layoutBefore", type = JsonObject.class),
                    @APILocals(name = "group", type = TabDock.Group.class)})
    private void dropAsNewGroup(Column column, Group before) {
        Entry moved = dragged;
        finishDrag();
        if (moved == null) {
            return;
        }
        Group from = groupOf(moved.id);
        if (from != null && columnOf(from) == column && column.groups.size() == 1 && from.ids.size() == 1) {
            // the only tab of the only sheet of the column, it can't go anywhere
            return;
        }
        JsonObject layoutBefore = getLayout();

        int index = before == null ? column.groups.size() : column.groups.indexOf(before);
        if (from != null && columnOf(from) == column && from.ids.size() == 1 && column.groups.indexOf(from) < index) {
            index--;
        }
        takeOut(moved);
        Group group = new Group();
        group.ids.add(moved.id);
        group.selectedId = moved.id;
        if (!columns.contains(column)) {
            columns.add(column);
        }
        column.groups.add(Math.max(0, Math.min(index, column.groups.size())), group);
        layoutMoved(layoutBefore);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "before", type = TabDock.Column.class)},
            locals = {@APILocals(name = "moved", type = TabDock.Entry.class),
                    @APILocals(name = "layoutBefore", type = JsonObject.class),
                    @APILocals(name = "from", type = TabDock.Group.class),
                    @APILocals(name = "fromColumn", type = TabDock.Column.class),
                    @APILocals(name = "group", type = TabDock.Group.class),
                    @APILocals(name = "column", type = TabDock.Column.class)})
    private void dropAsNewColumn(Column before) {
        Entry moved = dragged;
        finishDrag();
        if (moved == null) {
            return;
        }
        JsonObject layoutBefore = getLayout();

        int index = before == null ? columns.size() : columns.indexOf(before);
        Group from = groupOf(moved.id);
        Column fromColumn = from == null ? null : columnOf(from);
        if (fromColumn != null && fromColumn.groups.size() == 1 && from.ids.size() == 1 && columns.indexOf(fromColumn) < index) {
            // the column of the tab is gone when it leaves
            index--;
        }
        takeOut(moved);
        Group group = new Group();
        group.ids.add(moved.id);
        group.selectedId = moved.id;
        Column column = new Column();
        column.groups.add(group);
        columns.add(Math.max(0, Math.min(index, columns.size())), column);
        layoutMoved(layoutBefore);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "layoutBefore", type = JsonObject.class)})
    private void layoutMoved(JsonObject layoutBefore) {
        arrange();
        if (!getLayout().equals(layoutBefore)) {
            if (onLayoutChange != null) {
                onLayoutChange.run();
            }
        }
        stateChanged();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    private void stateChanged() {
        if (onStateChange != null) {
            onStateChange.run();
        }
    }

}
