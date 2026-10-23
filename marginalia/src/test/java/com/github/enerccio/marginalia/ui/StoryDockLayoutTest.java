package com.github.enerccio.marginalia.ui;

import com.github.enerccio.marginalia.ui.dialogs.story.StoryDockLayout;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The default layout of the story editor and the migration of the layouts stored before the story was a tab of the dock.
 */
class StoryDockLayoutTest {

    private static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }

    private static final String DEFAULT = "{\"columns\":["
            + "{\"size\":20,\"groups\":[{\"tabs\":[\"chapters\",\"lorebook\"]}]},"
            + "{\"size\":80,\"groups\":[{\"tabs\":[\"story\"]}]}]}";

    @Test
    void defaultLayoutHasTheSidebarAtTheLeftAndTheStoryAtTheRight() {
        assertThat(StoryDockLayout.defaultLayout("story", null, "chapters", "lorebook")).isEqualTo(json(DEFAULT));
    }

    @Test
    void defaultLayoutTakesTheShareOfTheSidebar() {
        assertThat(StoryDockLayout.defaultLayout("story", 35.0, "chapters", "lorebook")).isEqualTo(json("{\"columns\":["
                + "{\"size\":35,\"groups\":[{\"tabs\":[\"chapters\",\"lorebook\"]}]},"
                + "{\"size\":65,\"groups\":[{\"tabs\":[\"story\"]}]}]}"));
    }

    @Test
    void anUnusableShareIsTheDefaultOne() {
        for (Double share : new Double[]{0.0, -5.0, 100.0, 250.0}) {
            assertThat(StoryDockLayout.defaultLayout("story", share, "chapters", "lorebook")).isEqualTo(json(DEFAULT));
        }
    }

    @Test
    void nothingStoredIsTheDefaultLayout() {
        assertThat(StoryDockLayout.migrate(null, null, "story", "chapters", "lorebook")).isEqualTo(json(DEFAULT));
        assertThat(StoryDockLayout.migrate(json("{\"columns\":[]}"), null, "story", "chapters", "lorebook")).isEqualTo(json(DEFAULT));
        assertThat(StoryDockLayout.migrate(json("{}"), 30.0, "story", "chapters", "lorebook")).isEqualTo(
                StoryDockLayout.defaultLayout("story", 30.0, "chapters", "lorebook"));
    }

    @Test
    void aLayoutWithTheStoryIsKept() {
        JsonObject stored = json("{\"columns\":[{\"size\":50,\"groups\":[{\"tabs\":[\"story\",\"chapters\"]}]}]}");

        assertThat(StoryDockLayout.migrate(stored, 30.0, "story", "chapters", "lorebook")).isSameAs(stored);
    }

    @Test
    void anOldLayoutKeepsItsColumnsAndGetsTheStoryAtTheRight() {
        JsonObject stored = json("{\"columns\":[{\"groups\":[{\"tabs\":[\"chapters\"]},{\"tabs\":[\"lorebook\"]}]}]}");

        assertThat(StoryDockLayout.migrate(stored, 30.0, "story", "chapters", "lorebook")).isEqualTo(json("{\"columns\":["
                + "{\"size\":30,\"groups\":[{\"tabs\":[\"chapters\"]},{\"tabs\":[\"lorebook\"]}]},"
                + "{\"size\":70,\"groups\":[{\"tabs\":[\"story\"]}]}]}"));
        // the stored layout is not changed
        assertThat(stored).isEqualTo(json("{\"columns\":[{\"groups\":[{\"tabs\":[\"chapters\"]},{\"tabs\":[\"lorebook\"]}]}]}"));
    }

    @Test
    void theColumnsOfAnOldLayoutShareTheWidthOfTheSidebar() {
        JsonObject stored = json("{\"columns\":["
                + "{\"size\":75,\"groups\":[{\"size\":40,\"tabs\":[\"chapters\"]}]},"
                + "{\"size\":25,\"groups\":[{\"tabs\":[\"lorebook\"]}]}]}");

        assertThat(StoryDockLayout.migrate(stored, 40.0, "story", "chapters", "lorebook")).isEqualTo(json("{\"columns\":["
                + "{\"size\":30,\"groups\":[{\"size\":40,\"tabs\":[\"chapters\"]}]},"
                + "{\"size\":10,\"groups\":[{\"tabs\":[\"lorebook\"]}]},"
                + "{\"size\":60,\"groups\":[{\"tabs\":[\"story\"]}]}]}"));
    }

    @Test
    void aColumnWithoutASizeGetsTheAverage() {
        JsonObject stored = json("{\"columns\":["
                + "{\"size\":60,\"groups\":[{\"tabs\":[\"chapters\"]}]},"
                + "{\"groups\":[{\"tabs\":[\"lorebook\"]}]}]}");

        JsonObject migrated = StoryDockLayout.migrate(stored, 20.0, "story", "chapters", "lorebook");

        assertThat(migrated.getAsJsonArray("columns").get(0).getAsJsonObject().get("size").getAsDouble()).isEqualTo(10.0);
        assertThat(migrated.getAsJsonArray("columns").get(1).getAsJsonObject().get("size").getAsDouble()).isEqualTo(10.0);
        assertThat(migrated.getAsJsonArray("columns").get(2).getAsJsonObject().get("size").getAsDouble()).isEqualTo(80.0);
    }

    @Test
    void theOldestLayoutWithGroupsAtTheTopIsOneColumn() {
        JsonObject stored = json("{\"groups\":[{\"tabs\":[\"chapters\",\"lorebook\"]}]}");

        assertThat(StoryDockLayout.migrate(stored, null, "story", "chapters", "lorebook")).isEqualTo(json(DEFAULT));
    }

    @Test
    void containsFindsATabInAnyColumn() {
        JsonObject layout = json("{\"columns\":[{\"groups\":[{\"tabs\":[\"a\"]}]},{\"groups\":[{\"tabs\":[\"b\"]},{\"tabs\":[\"c\",\"d\"]}]}]}");

        assertThat(StoryDockLayout.contains(layout, "a")).isTrue();
        assertThat(StoryDockLayout.contains(layout, "d")).isTrue();
        assertThat(StoryDockLayout.contains(layout, "e")).isFalse();
        assertThat(StoryDockLayout.contains(json("{\"groups\":[{\"tabs\":[\"x\"]}]}"), "x")).isTrue();
        assertThat(StoryDockLayout.contains(null, "x")).isFalse();
    }

}
