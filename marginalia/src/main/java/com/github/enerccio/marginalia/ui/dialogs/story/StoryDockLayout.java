package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.ui.widgets.TabDock;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The layouts of the desktop story editor ({@link TabDock#getLayout()}): the default one, and the migration of the layouts
 * stored before the story was a tab of the dock. Those hold the tabs of the sidebar only, the story was next to the dock
 * and the share of the width the dock had was a number of its own.
 */
public final class StoryDockLayout {

    /**
     * Percent of the width the sidebar has in the default layout (and the old layouts that did not store it).
     */
    public static final double DEFAULT_SIDEBAR_SHARE = 20;

    private static final String KEY_COLUMNS = "columns";
    // a layout of one column, as the dock stored it before it had columns
    private static final String KEY_GROUPS = "groups";
    private static final String KEY_TABS = "tabs";
    private static final String KEY_SIZE = "size";

    private StoryDockLayout() {
    }

    /**
     * The sidebar tabs in one sheet on the left and the story on the right. A tab the layout does not name (an extension's)
     * goes to the first sheet, so it lands in the sidebar.
     *
     * @param sidebarShare percent of the width of the sidebar, {@link #DEFAULT_SIDEBAR_SHARE} when null or not between 0 and 100
     */
    public static JsonObject defaultLayout(String storyId, Double sidebarShare, String... sidebarIds) {
        double share = share(sidebarShare);
        JsonArray sidebarTabs = new JsonArray();
        for (String id : sidebarIds) {
            sidebarTabs.add(id);
        }
        JsonArray columns = new JsonArray();
        columns.add(column(share, sidebarTabs));
        columns.add(column(round(100 - share), array(storyId)));
        JsonObject layout = new JsonObject();
        layout.add(KEY_COLUMNS, columns);
        return layout;
    }

    /**
     * The layout to apply for what the manuscript has stored: the stored layout when it has the story already, the old
     * arrangement when it has not (its columns take the share of the sidebar, the story gets a column of its own at the right),
     * the default one when nothing usable is stored.
     *
     * @param stored       the stored layout, null when there is none
     * @param sidebarShare the stored share of the sidebar of the old layouts, null when there is none
     */
    public static JsonObject migrate(JsonObject stored, Double sidebarShare, String storyId, String... sidebarIds) {
        List<JsonObject> columns = columns(stored);
        if (columns.isEmpty()) {
            return defaultLayout(storyId, sidebarShare, sidebarIds);
        }
        if (contains(stored, storyId)) {
            return stored;
        }
        double share = share(sidebarShare);

        // a column without a size gets the average of the others, like the dock does
        double known = 0;
        int count = 0;
        for (JsonObject column : columns) {
            Double size = size(column);
            if (size != null) {
                known += size;
                count++;
            }
        }
        double average = count == 0 ? 1 : known / count;
        double total = 0;
        List<Double> sizes = new ArrayList<>();
        for (JsonObject column : columns) {
            Double size = size(column);
            sizes.add(size == null ? average : size);
            total += sizes.get(sizes.size() - 1);
        }

        JsonArray migrated = new JsonArray();
        for (int i = 0; i < columns.size(); i++) {
            JsonObject column = columns.get(i).deepCopy();
            column.addProperty(KEY_SIZE, round(sizes.get(i) / total * share));
            migrated.add(column);
        }
        migrated.add(column(round(100 - share), array(storyId)));
        JsonObject layout = new JsonObject();
        layout.add(KEY_COLUMNS, migrated);
        return layout;
    }

    /**
     * @return true when the layout names the tab
     */
    public static boolean contains(JsonObject layout, String id) {
        for (JsonObject column : columns(layout)) {
            JsonElement groups = column.get(KEY_GROUPS);
            if (groups == null || !groups.isJsonArray()) {
                continue;
            }
            for (JsonElement group : groups.getAsJsonArray()) {
                JsonElement tabs = group.isJsonObject() ? group.getAsJsonObject().get(KEY_TABS) : null;
                if (tabs != null && tabs.isJsonArray()) {
                    for (JsonElement tab : tabs.getAsJsonArray()) {
                        if (tab.isJsonPrimitive() && id.equals(tab.getAsString())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * The columns of a layout, a layout with {@code groups} at the top is one column.
     */
    private static List<JsonObject> columns(JsonObject layout) {
        List<JsonObject> columns = new ArrayList<>();
        if (layout == null) {
            return columns;
        }
        JsonElement array = layout.get(KEY_COLUMNS);
        if (array != null && array.isJsonArray()) {
            for (JsonElement element : array.getAsJsonArray()) {
                if (element.isJsonObject()) {
                    columns.add(element.getAsJsonObject());
                }
            }
        } else if (layout.has(KEY_GROUPS)) {
            columns.add(layout);
        }
        return columns;
    }

    private static JsonObject column(double size, JsonArray tabs) {
        JsonObject group = new JsonObject();
        group.add(KEY_TABS, tabs);
        JsonArray groups = new JsonArray();
        groups.add(group);
        JsonObject column = new JsonObject();
        column.addProperty(KEY_SIZE, size);
        column.add(KEY_GROUPS, groups);
        return column;
    }

    private static JsonArray array(String id) {
        JsonArray array = new JsonArray();
        array.add(id);
        return array;
    }

    private static Double size(JsonObject object) {
        JsonElement element = object.get(KEY_SIZE);
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()
                && element.getAsDouble() > 0) {
            return element.getAsDouble();
        }
        return null;
    }

    private static double share(Double sidebarShare) {
        return sidebarShare != null && sidebarShare > 0 && sidebarShare < 100 ? sidebarShare : DEFAULT_SIDEBAR_SHARE;
    }

    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }

}
