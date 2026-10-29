package com.github.enerccio.marginalia.ui;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptDialog;
import com.github.enerccio.marginalia.ui.dialogs.manuscript.ManuscriptStoryPart;
import com.github.enerccio.marginalia.ui.dialogs.story.StoryDockLayout;
import com.github.enerccio.marginalia.ui.widgets.TabDock;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dnd.DragStartEvent;
import com.vaadin.flow.component.dnd.DropEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The tab dock of the story editor (the sidebar tabs and the story itself): the layout, the drag and drop of the tabs, the
 * stored selection and the layout kept in the attributes of the manuscript.
 */
class TabDockTest extends MarginaliaTestBase {

    @Autowired
    private ManuscriptService manuscriptService;

    // UI.setCurrent keeps a weak reference only
    private UI ui;

    @BeforeEach
    void currentUI() {
        ui = new UI();
        UI.setCurrent(ui);
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }

    /**
     * A layout of one column, every argument is the tabs of a sheet: {@code "\"a\",\"b\""}.
     */
    private static JsonObject layout(String... groups) {
        return columns(groups);
    }

    /**
     * A layout of columns, every column is an array of sheets: {@code {"a","b"}, {"c"}} is a column with the sheets a, b and a
     * column with the sheet c. Written as one string per sheet, a {@code null} starts the next column.
     */
    private static JsonObject columns(String... groupsAndSeparators) {
        StringBuilder text = new StringBuilder("{\"columns\":[{\"groups\":[");
        boolean first = true;
        for (String group : groupsAndSeparators) {
            if (group == null) {
                text.append("]},{\"groups\":[");
                first = true;
                continue;
            }
            text.append(first ? "" : ",").append("{\"tabs\":[").append(group).append("]}");
            first = false;
        }
        return json(text.append("]}]}").toString());
    }

    /**
     * Everything under the component, in the order it is shown.
     */
    private static List<Component> descendants(Component root) {
        List<Component> all = new java.util.ArrayList<>();
        root.getChildren().forEach(child -> {
            all.add(child);
            all.addAll(descendants(child));
        });
        return all;
    }

    private static <T extends Component> List<T> descendants(Component root, Class<T> type) {
        return descendants(root).stream().filter(type::isInstance).map(type::cast).collect(Collectors.toList());
    }

    private static List<Div> divs(TabDock dock, String className) {
        return descendants(dock, Div.class).stream().filter(div -> div.getClassNames().contains(className))
                .collect(Collectors.toList());
    }

    private static List<Div> columnBoxes(TabDock dock) {
        return divs(dock, CssStyles.TAB_DOCK_COLUMN);
    }

    private static List<TabSheet> sheets(TabDock dock) {
        return descendants(dock, TabSheet.class);
    }

    /**
     * The strips for a new sheet of all columns, in order.
     */
    private static List<Div> zones(TabDock dock) {
        return divs(dock, CssStyles.TAB_DOCK_ZONE).stream()
                .filter(div -> !div.getClassNames().contains(CssStyles.TAB_DOCK_SIDE_ZONE)).collect(Collectors.toList());
    }

    /**
     * The strips for a new column.
     */
    private static List<Div> sideZones(TabDock dock) {
        return divs(dock, CssStyles.TAB_DOCK_SIDE_ZONE);
    }

    private static void dragEnd(SplitLayout split, double position) {
        // the browser reports the sizes of the two sides, the split layout works out its position from them
        ComponentUtil.fireEvent(split, new SplitLayout.SplitterDragEndEvent(split, true, position + "%", (100 - position) + "%"));
    }

    private static List<String> captions(TabSheet sheet) {
        List<String> captions = new java.util.ArrayList<>();
        for (int i = 0; i < sheet.getTabCount(); i++) {
            captions.add(sheet.getTabAt(i).getLabel());
        }
        return captions;
    }

    private TabDock dock(String... ids) {
        TabDock dock = new TabDock();
        for (String id : ids) {
            dock.add(id, id.toUpperCase(), new Div());
        }
        // the drag and drop events need the UI
        ui.add(dock);
        return dock;
    }

    private static void drag(Tab tab) {
        ComponentUtil.fireEvent(tab, new DragStartEvent<>(tab, true));
    }

    private static <T extends Component> void drop(T target) {
        ComponentUtil.fireEvent(target, new DropEvent<>(target, true, "move"));
    }

    private static Tab tab(TabDock dock, String caption) {
        return sheets(dock).stream().flatMap(sheet -> {
            List<Tab> tabs = new java.util.ArrayList<>();
            for (int i = 0; i < sheet.getTabCount(); i++) {
                tabs.add(sheet.getTabAt(i));
            }
            return tabs.stream();
        }).filter(tab -> caption.equals(tab.getLabel())).findFirst().orElseThrow();
    }

    @Test
    void tabsStartInOneSheet() throws Exception {
        login();
        TabDock dock = dock("a", "b", "c");

        assertThat(sheets(dock)).hasSize(1);
        assertThat(captions(sheets(dock).get(0))).containsExactly("A", "B", "C");
        assertThat(dock.getLayout()).isEqualTo(layout("\"a\",\"b\",\"c\""));
    }

    @Test
    void selectRelativeGoesThroughTheSheetsInTheOrderOfTheLayoutAndRoundAgain() throws Exception {
        login();
        TabDock dock = dock("a", "b", "c", "d");
        dock.setLayout(columns("\"a\",\"b\"", null, "\"c\"", "\"d\""));
        List<TabSheet> shown = sheets(dock);

        // nothing shown yet by a call: on from the tab on the screen (A)
        assertThat(dock.selectRelative(1)).isTrue();
        assertThat(shown.get(0).getSelectedTab().getLabel()).isEqualTo("B");
        dock.selectRelative(1);
        assertThat(shown.get(1).getSelectedTab().getLabel()).isEqualTo("C");
        dock.selectRelative(1);
        assertThat(shown.get(2).getSelectedTab().getLabel()).isEqualTo("D");
        dock.selectRelative(1);
        assertThat(shown.get(0).getSelectedTab().getLabel()).isEqualTo("A");
        dock.selectRelative(-1);
        assertThat(shown.get(2).getSelectedTab().getLabel()).isEqualTo("D");
        dock.selectRelative(-1);
        assertThat(shown.get(1).getSelectedTab().getLabel()).isEqualTo("C");
    }

    @Test
    void selectRelativeContinuesFromTheTabTheUserClickedAndSkipsDisabledOnes() throws Exception {
        login();
        TabDock dock = dock("a", "b", "c");
        TabSheet sheet = sheets(dock).get(0);

        sheet.setSelectedTab(sheet.getTabAt(1));
        dock.setTabsEnabled(false, "c");
        dock.selectRelative(1);
        assertThat(sheet.getSelectedTab().getLabel()).isEqualTo("C");
        dock.selectRelative(1);
        assertThat(sheet.getSelectedTab().getLabel()).isEqualTo("C");
    }

    @Test
    void layoutSplitsTheTabsAndKeepsTheOnesItDoesNotKnow() throws Exception {
        login();
        TabDock dock = dock("a", "b", "c");

        dock.setLayout(layout("\"c\"", "\"b\",\"a\""));

        assertThat(sheets(dock)).hasSize(2);
        assertThat(captions(sheets(dock).get(0))).containsExactly("C");
        assertThat(captions(sheets(dock).get(1))).containsExactly("B", "A");

        dock.add("d", "D", new Div());
        assertThat(captions(sheets(dock).get(0))).containsExactly("C", "D");
        assertThat(dock.getLayout()).isEqualTo(layout("\"c\",\"d\"", "\"b\",\"a\""));
    }

    @Test
    void tabsAddedLaterFindTheirPlaceInTheLayout() throws Exception {
        login();
        TabDock dock = new TabDock();
        dock.setLayout(layout("\"a\"", "\"plugin\"", "\"b\""));
        dock.add("a", "A", new Div());
        dock.add("b", "B", new Div());

        assertThat(sheets(dock)).hasSize(2);

        Div content = new Div();
        dock.add("plugin", "PLUGIN", content);
        assertThat(sheets(dock)).hasSize(3);
        assertThat(captions(sheets(dock).get(1))).containsExactly("PLUGIN");

        dock.remove(content);
        assertThat(sheets(dock)).hasSize(2);
        // the place is kept for when the extension is back
        assertThat(dock.getLayout()).isEqualTo(layout("\"a\"", "\"plugin\"", "\"b\""));
    }

    @Test
    void droppingOnAStripDetachesTheTab() throws Exception {
        login();
        TabDock dock = dock("a", "b");
        int[] changes = {0};
        dock.setOnLayoutChange(() -> changes[0]++);

        drag(tab(dock, "B"));
        assertThat(dock.getClassNames()).contains(CssStyles.TAB_DOCK_DRAGGING);
        List<Div> strips = zones(dock);
        assertThat(strips).hasSize(2);
        drop(strips.get(1));

        assertThat(dock.getClassNames()).doesNotContain(CssStyles.TAB_DOCK_DRAGGING);
        assertThat(dock.getLayout()).isEqualTo(layout("\"a\"", "\"b\""));
        assertThat(sheets(dock)).hasSize(2);
        assertThat(captions(sheets(dock).get(0))).containsExactly("A");
        assertThat(captions(sheets(dock).get(1))).containsExactly("B");
        assertThat(changes[0]).isEqualTo(1);
    }

    @Test
    void droppingOnTheTopStripPutsTheSheetFirst() throws Exception {
        login();
        TabDock dock = dock("a", "b");

        drag(tab(dock, "B"));
        drop(zones(dock).get(0));

        assertThat(dock.getLayout()).isEqualTo(layout("\"b\"", "\"a\""));
    }

    @Test
    void droppingOnASheetJoinsItAndTheEmptySheetIsGone() throws Exception {
        login();
        TabDock dock = dock("a", "b");
        dock.setLayout(layout("\"a\"", "\"b\""));
        int[] changes = {0};
        dock.setOnLayoutChange(() -> changes[0]++);

        drag(tab(dock, "B"));
        drop(sheets(dock).get(0));

        assertThat(sheets(dock)).hasSize(1);
        assertThat(captions(sheets(dock).get(0))).containsExactly("A", "B");
        assertThat(sheets(dock).get(0).getSelectedTab().getLabel()).isEqualTo("B");
        assertThat(dock.getLayout()).isEqualTo(layout("\"a\",\"b\""));
        assertThat(changes[0]).isEqualTo(1);
    }

    @Test
    void droppingOnATabInsertsBeforeIt() throws Exception {
        login();
        TabDock dock = dock("a", "b", "c");

        drag(tab(dock, "C"));
        drop(tab(dock, "A"));

        assertThat(captions(sheets(dock).get(0))).containsExactly("C", "A", "B");
        assertThat(dock.getLayout()).isEqualTo(layout("\"c\",\"a\",\"b\""));
    }

    @Test
    void aDropOnATabIsNotHandledAgainBySheet() throws Exception {
        login();
        TabDock dock = dock("a", "b", "c");
        dock.setLayout(layout("\"a\",\"b\"", "\"c\""));

        drag(tab(dock, "C"));
        // the tab and then the sheet get the same drop
        drop(tab(dock, "A"));
        drop(sheets(dock).get(0));

        assertThat(dock.getLayout()).isEqualTo(layout("\"c\",\"a\",\"b\""));
    }

    @Test
    void droppingBackWhereItWasChangesNothing() throws Exception {
        login();
        TabDock dock = dock("a");
        int[] changes = {0};
        dock.setOnLayoutChange(() -> changes[0]++);

        drag(tab(dock, "A"));
        drop(zones(dock).get(0));

        assertThat(sheets(dock)).hasSize(1);
        assertThat(changes[0]).isZero();
    }

    @Test
    void aDropWithoutADragIsIgnored() throws Exception {
        login();
        TabDock dock = dock("a", "b");

        drop(zones(dock).get(0));

        assertThat(dock.getLayout()).isEqualTo(layout("\"a\",\"b\""));
    }

    @Test
    void aDockThatIsNotDetachableKeepsOneSheet() throws Exception {
        login();
        TabDock dock = new TabDock(false);
        dock.add("a", "A", new Div());
        dock.add("b", "B", new Div());
        dock.setLayout(layout("\"a\"", "\"b\""));

        assertThat(sheets(dock)).hasSize(1);
        assertThat(zones(dock)).isEmpty();
    }

    @Test
    void selectedTabsAreRestoredEvenForTabsThatComeLater() throws Exception {
        login();
        TabDock dock = dock("a", "b");
        dock.setLayout(layout("\"a\"", "\"b\",\"c\""));
        sheets(dock).get(1).setSelectedTab(tab(dock, "B"));

        dock.restoreState(json("{\"selected\":[\"a\",\"c\"]}"));
        assertThat(sheets(dock).get(1).getSelectedTab().getLabel()).isEqualTo("B");

        dock.add("c", "C", new Div());
        assertThat(sheets(dock).get(1).getSelectedTab().getLabel()).isEqualTo("C");
        assertThat(dock.storeState()).isEqualTo(json("{\"selected\":[\"a\",\"c\"]}"));
    }

    @Test
    void userSelectionIsReportedAsStateChange() throws Exception {
        login();
        TabDock dock = dock("a", "b");
        int[] changes = {0};
        dock.setOnStateChange(() -> changes[0]++);
        int before = changes[0];

        sheets(dock).get(0).setSelectedTab(tab(dock, "B"));

        assertThat(changes[0]).isGreaterThan(before);
        assertThat(dock.storeState()).isEqualTo(json("{\"selected\":[\"b\"]}"));
    }

    @Test
    void droppingOnASideStripMakesAColumn() throws Exception {
        login();
        TabDock dock = dock("a", "b");
        int[] changes = {0};
        dock.setOnLayoutChange(() -> changes[0]++);

        drag(tab(dock, "B"));
        assertThat(sideZones(dock)).hasSize(2);
        drop(sideZones(dock).get(1));

        assertThat(dock.getLayout()).isEqualTo(columns("\"a\"", null, "\"b\""));
        assertThat(columnBoxes(dock)).hasSize(2);
        assertThat(sheets(dock)).hasSize(2);
        assertThat(changes[0]).isEqualTo(1);
    }

    @Test
    void droppingOnTheFirstSideStripPutsTheColumnFirst() throws Exception {
        login();
        TabDock dock = dock("a", "b");

        drag(tab(dock, "B"));
        drop(sideZones(dock).get(0));

        assertThat(dock.getLayout()).isEqualTo(columns("\"b\"", null, "\"a\""));
    }

    @Test
    void droppingOnAStripBetweenColumnsMovesTheColumn() throws Exception {
        login();
        TabDock dock = dock("a", "b", "c");
        dock.setLayout(columns("\"a\"", null, "\"b\"", null, "\"c\""));
        assertThat(sideZones(dock)).hasSize(4);

        drag(tab(dock, "A"));
        drop(sideZones(dock).get(3));
        assertThat(dock.getLayout()).isEqualTo(columns("\"b\"", null, "\"c\"", null, "\"a\""));

        drag(tab(dock, "A"));
        drop(sideZones(dock).get(1));
        assertThat(dock.getLayout()).isEqualTo(columns("\"b\"", null, "\"a\"", null, "\"c\""));
    }

    @Test
    void aSheetGoesBetweenTheSheetsOfAnotherColumn() throws Exception {
        login();
        TabDock dock = dock("a", "b", "c");
        dock.setLayout(columns("\"a\"", "\"b\"", null, "\"c\""));
        // strips: a, b, end of the first column; c, end of the second
        assertThat(zones(dock)).hasSize(5);

        drag(tab(dock, "C"));
        drop(zones(dock).get(1));

        assertThat(dock.getLayout()).isEqualTo(columns("\"a\"", "\"c\"", "\"b\""));
        assertThat(columnBoxes(dock)).hasSize(1);
    }

    @Test
    void aLoneTabCanNotGoToItsOwnColumn() throws Exception {
        login();
        TabDock dock = dock("a", "b");
        dock.setLayout(columns("\"a\"", null, "\"b\""));
        int[] changes = {0};
        dock.setOnLayoutChange(() -> changes[0]++);

        drag(tab(dock, "A"));
        drop(zones(dock).get(0));
        // next to itself
        drag(tab(dock, "A"));
        drop(sideZones(dock).get(0));
        drag(tab(dock, "A"));
        drop(sideZones(dock).get(1));

        assertThat(dock.getLayout()).isEqualTo(columns("\"a\"", null, "\"b\""));
        assertThat(changes[0]).isZero();
    }

    @Test
    void aLayoutOfSheetsOnlyIsOneColumn() throws Exception {
        login();
        TabDock dock = dock("a", "b");

        dock.setLayout(json("{\"groups\":[{\"tabs\":[\"b\"]},{\"tabs\":[\"a\"]}]}"));

        assertThat(dock.getLayout()).isEqualTo(columns("\"b\"", "\"a\""));
    }

    @Test
    void droppingATabOnItsOwnSheetKeepsIt() throws Exception {
        login();
        TabDock dock = dock("a");

        drag(tab(dock, "A"));
        drop(sheets(dock).get(0));

        assertThat(dock.getLayout()).isEqualTo(layout("\"a\""));
        assertThat(sheets(dock)).hasSize(1);
    }

    @Test
    void sheetsAreSplitLayouts() throws Exception {
        login();
        TabDock dock = dock("a", "b", "c");
        dock.setLayout(columns("\"a\"", "\"b\"", null, "\"c\""));

        // one split between the columns, one between the sheets of the first column
        List<SplitLayout> splits = descendants(dock, SplitLayout.class);
        assertThat(splits).hasSize(2);
        assertThat(splits.get(0).getOrientation()).isEqualTo(SplitLayout.Orientation.HORIZONTAL);
        assertThat(splits.get(1).getOrientation()).isEqualTo(SplitLayout.Orientation.VERTICAL);
    }

    @Test
    void sizesAreSplitterPositionsOfTheChain() throws Exception {
        login();
        TabDock dock = dock("a", "b", "c");
        JsonObject sized = json("{\"columns\":[{\"groups\":[{\"size\":50,\"tabs\":[\"a\"]},{\"size\":30,\"tabs\":[\"b\"]},"
                + "{\"size\":20,\"tabs\":[\"c\"]}]}]}");

        dock.setLayout(sized);

        List<SplitLayout> splits = descendants(dock, SplitLayout.class);
        assertThat(splits).hasSize(2);
        assertThat(splits.get(0).getSplitterPosition()).isEqualTo(50.0);
        assertThat(splits.get(1).getSplitterPosition()).isEqualTo(60.0);
        assertThat(dock.getLayout()).isEqualTo(sized);
    }

    @Test
    void resizingIsSavedInTheLayout() throws Exception {
        login();
        TabDock dock = dock("a", "b", "c");
        dock.setLayout(layout("\"a\"", "\"b\"", "\"c\""));
        int[] changes = {0};
        dock.setOnLayoutChange(() -> changes[0]++);
        List<SplitLayout> splits = descendants(dock, SplitLayout.class);

        dragEnd(splits.get(0), 25);
        dragEnd(splits.get(1), 40);

        assertThat(changes[0]).isEqualTo(2);
        // 25 %, 40 % of the rest (75 %) = 30 %, 45 %
        assertThat(dock.getLayout()).isEqualTo(json("{\"columns\":[{\"groups\":[{\"size\":25.0,\"tabs\":[\"a\"]},"
                + "{\"size\":30.0,\"tabs\":[\"b\"]},{\"size\":45.0,\"tabs\":[\"c\"]}]}]}"));
    }

    @Test
    void columnsAreResizableToo() throws Exception {
        login();
        TabDock dock = dock("a", "b");
        dock.setLayout(columns("\"a\"", null, "\"b\""));

        dragEnd(descendants(dock, SplitLayout.class).get(0), 70);

        assertThat(dock.getLayout()).isEqualTo(json("{\"columns\":[{\"size\":70.0,\"groups\":[{\"tabs\":[\"a\"]}]},"
                + "{\"size\":30.0,\"groups\":[{\"tabs\":[\"b\"]}]}]}"));
    }

    @Test
    void aNewSheetGetsTheAverageSize() throws Exception {
        login();
        TabDock dock = dock("a", "b", "c");
        dock.setLayout(json("{\"columns\":[{\"groups\":[{\"size\":70,\"tabs\":[\"a\"]},{\"size\":30,\"tabs\":[\"b\"]}]}]}"));

        drag(tab(dock, "C"));
        // the end strip of the column
        drop(zones(dock).get(zones(dock).size() - 1));

        // 70, 30 and 50 (the average) of 150
        List<SplitLayout> splits = descendants(dock, SplitLayout.class);
        assertThat(splits).hasSize(2);
        assertThat(splits.get(0).getSplitterPosition()).isEqualTo(70.0 / 150 * 100);
    }

    // ---- the story part

    private static final String STORY = ManuscriptStoryPart.STORY_TAB;
    private static final String CHAPTERS = ManuscriptStoryPart.CHAPTERS_TAB;
    private static final String LOREBOOK = ManuscriptStoryPart.LOREBOOK_TAB;
    private static final String ANNOTATIONS = ManuscriptStoryPart.ANNOTATIONS_TAB;

    private Manuscript book() throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        return manuscriptService.save(manuscript);
    }

    private ManuscriptDialog opened(Manuscript manuscript) throws Exception {
        ManuscriptDialog dialog = new ManuscriptDialog(manuscript);
        dialog.create();
        dialog.open();
        return dialog;
    }

    @Test
    void storyIsATabOfTheDockAtTheRightOfTheSidebar() throws Exception {
        login();
        ManuscriptDialog dialog = opened(book());

        TabDock dock = leftBar(dialog);
        assertThat(dock.getLayout()).isEqualTo(json("{\"columns\":["
                + "{\"size\":20,\"groups\":[{\"tabs\":[\"" + CHAPTERS + "\",\"" + LOREBOOK + "\",\"" + ANNOTATIONS + "\"]}]},"
                + "{\"size\":80,\"groups\":[{\"tabs\":[\"" + STORY + "\"]}]}]}"));
        assertThat(sheets(dock)).hasSize(2);
        assertThat(captions(sheets(dock).get(1))).hasSize(1);
        // one splitter between the two columns, the story is not in a split layout of its own
        assertThat(descendants(dock, SplitLayout.class)).hasSize(1);
    }

    @Test
    void storyPartKeepsTheLayoutInTheManuscript() throws Exception {
        login();
        Manuscript manuscript = book();
        ManuscriptDialog dialog = opened(manuscript);

        TabDock dock = leftBar(dialog);
        dock.add("extra", "Extra", new Div());
        attached(dock);

        drag(tab(dock, "Extra"));
        // the strip under the sheet of the sidebar, the first column
        drop(zones(dock).get(1));

        Manuscript saved = manuscriptService.find(manuscript);
        assertThat(saved.getAttributes().getAsJsonObject(ManuscriptStoryPart.TABS_CONFIG_ATTRIBUTE))
                .isEqualTo(json("{\"columns\":["
                        + "{\"size\":20,\"groups\":[{\"tabs\":[\"" + CHAPTERS + "\",\"" + LOREBOOK + "\",\"" + ANNOTATIONS + "\"]},{\"tabs\":[\"extra\"]}]},"
                        + "{\"size\":80,\"groups\":[{\"tabs\":[\"" + STORY + "\"]}]}]}"));

        // a dialog opened later has the sheets as they were left
        TabDock restored = leftBar(opened(saved));
        restored.add("extra", "Extra", new Div());
        assertThat(sheets(restored)).hasSize(3);
        assertThat(captions(sheets(restored).get(1))).containsExactly("Extra");
    }

    @Test
    void theUserCanPutTheStoryAnywhere() throws Exception {
        login();
        Manuscript manuscript = book();
        TabDock dock = leftBar(opened(manuscript));
        attached(dock);

        // the story takes a column of its own at the left
        drag(tab(dock, "Story"));
        drop(sideZones(dock).get(0));

        Manuscript saved = manuscriptService.find(manuscript);
        JsonObject stored = saved.getAttributes().getAsJsonObject(ManuscriptStoryPart.TABS_CONFIG_ATTRIBUTE);
        assertThat(stored.getAsJsonArray("columns").get(0).getAsJsonObject().getAsJsonArray("groups").get(0)
                .getAsJsonObject().getAsJsonArray("tabs")).hasSize(1).first().isEqualTo(new com.google.gson.JsonPrimitive(STORY));

        // and it is there when the book is opened again
        TabDock restored = leftBar(opened(saved));
        assertThat(restored.getLayout().getAsJsonArray("columns").get(0).getAsJsonObject().getAsJsonArray("groups").get(0)
                .getAsJsonObject().getAsJsonArray("tabs")).hasSize(1).first().isEqualTo(new com.google.gson.JsonPrimitive(STORY));
    }

    @Test
    void storyPartKeepsTheWidthOfTheStoryInTheManuscript() throws Exception {
        login();
        Manuscript manuscript = book();
        TabDock dock = leftBar(opened(manuscript));

        dragEnd(descendants(dock, SplitLayout.class).get(0), 35);

        Manuscript saved = manuscriptService.find(manuscript);
        JsonObject stored = saved.getAttributes().getAsJsonObject(ManuscriptStoryPart.TABS_CONFIG_ATTRIBUTE);
        assertThat(stored.getAsJsonArray("columns").get(0).getAsJsonObject().get("size").getAsDouble()).isEqualTo(35.0);
        assertThat(stored.getAsJsonArray("columns").get(1).getAsJsonObject().get("size").getAsDouble()).isEqualTo(65.0);

        // a dialog opened later has the divider where it was left
        TabDock restored = leftBar(opened(saved));
        assertThat(descendants(restored, SplitLayout.class).get(0).getSplitterPosition()).isEqualTo(35.0);
    }

    @Test
    void aLayoutSavedBeforeTheStoryWasATabGetsTheStoryAtTheRight() throws Exception {
        login();
        Manuscript manuscript = book();
        // the sidebar in two sheets, the splitter at 30 %: what the old versions stored
        manuscript.getAttributes().add(ManuscriptStoryPart.TABS_CONFIG_ATTRIBUTE,
                layout("\"" + CHAPTERS + "\"", "\"" + LOREBOOK + "\""));
        manuscript.getAttributes().addProperty(ManuscriptStoryPart.SPLIT_POSITION_ATTRIBUTE, 30.0);
        manuscript = manuscriptService.save(manuscript);

        TabDock dock = leftBar(opened(manuscript));

        assertThat(dock.getLayout()).isEqualTo(json("{\"columns\":["
                // the tab of the annotations is new to the layout: it goes to the first sheet, as the tabs of extensions do
                + "{\"size\":30,\"groups\":[{\"tabs\":[\"" + CHAPTERS + "\",\"" + ANNOTATIONS + "\"]},{\"tabs\":[\"" + LOREBOOK + "\"]}]},"
                + "{\"size\":70,\"groups\":[{\"tabs\":[\"" + STORY + "\"]}]}]}"));
        assertThat(sheets(dock)).hasSize(3);

        // the next save of the layout drops the old attribute
        dragEnd(descendants(dock, SplitLayout.class).get(0), 40);
        Manuscript saved = manuscriptService.find(manuscript);
        assertThat(saved.getAttributes().has(ManuscriptStoryPart.SPLIT_POSITION_ATTRIBUTE)).isFalse();
        assertThat(saved.getAttributes().getAsJsonObject(ManuscriptStoryPart.TABS_CONFIG_ATTRIBUTE)
                .getAsJsonArray("columns").get(0).getAsJsonObject().get("size").getAsDouble()).isEqualTo(40.0);
    }

    @Test
    void aSplitterSavedBeforeTheStoryWasATabIsTheWidthOfTheSidebar() throws Exception {
        login();
        Manuscript manuscript = book();
        manuscript.getAttributes().addProperty(ManuscriptStoryPart.SPLIT_POSITION_ATTRIBUTE, 35.0);
        manuscript = manuscriptService.save(manuscript);

        TabDock dock = leftBar(opened(manuscript));

        assertThat(descendants(dock, SplitLayout.class).get(0).getSplitterPosition()).isEqualTo(35.0);
        assertThat(dock.getLayout().getAsJsonArray("columns")).hasSize(2);
    }

    @Test
    void restoreDefaultLayoutPutsTheTabsBack() throws Exception {
        login();
        Manuscript manuscript = book();
        ManuscriptDialog dialog = opened(manuscript);
        TabDock dock = leftBar(dialog);
        attached(dock);
        drag(tab(dock, "Story"));
        drop(sideZones(dock).get(0));
        dragEnd(descendants(dock, SplitLayout.class).get(0), 35);
        assertThat(manuscriptService.find(manuscript).getAttributes().has(ManuscriptStoryPart.TABS_CONFIG_ATTRIBUTE)).isTrue();

        java.lang.reflect.Method restore = ManuscriptStoryPart.class.getDeclaredMethod("restoreDefaultLayout");
        restore.setAccessible(true);
        restore.invoke(storyPart(dialog));

        assertThat(dock.getLayout()).isEqualTo(json("{\"columns\":["
                + "{\"size\":20,\"groups\":[{\"tabs\":[\"" + CHAPTERS + "\",\"" + LOREBOOK + "\",\"" + ANNOTATIONS + "\"]}]},"
                + "{\"size\":80,\"groups\":[{\"tabs\":[\"" + STORY + "\"]}]}]}"));
        Manuscript saved = manuscriptService.find(manuscript);
        assertThat(saved.getAttributes().has(ManuscriptStoryPart.TABS_CONFIG_ATTRIBUTE)).isFalse();
        assertThat(saved.getAttributes().has(ManuscriptStoryPart.SPLIT_POSITION_ATTRIBUTE)).isFalse();
    }

    @Test
    void restoreDefaultLayoutKeepsTheTabsOfExtensions() throws Exception {
        login();
        ManuscriptDialog dialog = opened(book());
        TabDock dock = leftBar(dialog);
        dock.add("extra", "Extra", new Div());
        attached(dock);
        drag(tab(dock, "Extra"));
        drop(sideZones(dock).get(0));
        assertThat(sheets(dock)).hasSize(3);

        java.lang.reflect.Method restore = ManuscriptStoryPart.class.getDeclaredMethod("restoreDefaultLayout");
        restore.setAccessible(true);
        restore.invoke(storyPart(dialog));

        // in the first sheet, with the tabs of the sidebar
        assertThat(captions(sheets(dock).get(0))).hasSize(4).contains("Extra");
    }

    @Test
    void showStorySelectsTheStoryTabWhenItIsBehindAnother() throws Exception {
        login();
        Manuscript manuscript = book();
        // the story and the outline in one sheet
        manuscript.getAttributes().add(ManuscriptStoryPart.TABS_CONFIG_ATTRIBUTE,
                layout("\"" + CHAPTERS + "\",\"" + STORY + "\",\"" + LOREBOOK + "\""));
        manuscript = manuscriptService.save(manuscript);
        ManuscriptDialog dialog = opened(manuscript);
        TabDock dock = leftBar(dialog);
        TabSheet sheet = sheets(dock).get(0);
        sheet.setSelectedTab(tab(dock, "Story"));
        sheet.setSelectedTab(sheet.getTabAt(0));
        assertThat(sheet.getSelectedTab()).isNotEqualTo(tab(dock, "Story"));

        storyPart(dialog).showStory();

        assertThat(sheet.getSelectedTab()).isEqualTo(tab(dock, "Story"));
    }

    @Test
    void choosingABranchInTheTreeShowsTheStory() throws Exception {
        login();
        Manuscript manuscript = book();
        manuscript.getAttributes().add(ManuscriptStoryPart.TABS_CONFIG_ATTRIBUTE,
                layout("\"" + CHAPTERS + "\",\"" + STORY + "\",\"" + LOREBOOK + "\""));
        manuscript = manuscriptService.save(manuscript);
        ManuscriptDialog dialog = opened(manuscript);
        TabDock dock = leftBar(dialog);
        TabSheet sheet = sheets(dock).get(0);
        sheet.setSelectedTab(sheet.getTabAt(0));

        // what the tree does after it made another part the end of the branch
        dialog.selectStoryPart();

        assertThat(sheet.getSelectedTab()).isEqualTo(tab(dock, "Story"));
    }

    @Test
    void aGenerationLeavesTheStoryEnabledAndTheOtherTabsNot() throws Exception {
        login();
        ManuscriptDialog dialog = opened(book());
        TabDock dock = leftBar(dialog);
        // a book without parts opens on the info tab, the tab sheet of the dialog disables the content of the others
        dialog.selectStoryPart();

        dialog.freeze();
        assertThat(tab(dock, "Story").isEnabled()).isTrue();
        assertThat(tab(dock, "Lorebook").isEnabled()).isFalse();

        dialog.unfreeze();
        assertThat(tab(dock, "Lorebook").isEnabled()).isTrue();
    }

    // ---- select and enabled

    @Test
    void selectShowsTheTabInItsSheet() throws Exception {
        login();
        TabDock dock = dock("a", "b");
        TabSheet sheet = sheets(dock).get(0);
        sheet.setSelectedTab(tab(dock, "B"));

        assertThat(dock.select("a")).isTrue();

        assertThat(sheet.getSelectedTab().getLabel()).isEqualTo("A");
        assertThat(dock.storeState()).isEqualTo(json("{\"selected\":[\"a\"]}"));
        assertThat(dock.select("nothing")).isFalse();
    }

    @Test
    void selectFindsTheTabWhereTheUserMovedIt() throws Exception {
        login();
        TabDock dock = dock("a", "b", "c");
        drag(tab(dock, "A"));
        drop(zones(dock).get(zones(dock).size() - 1));
        TabSheet sheetOfB = sheets(dock).get(0);
        sheetOfB.setSelectedTab(tab(dock, "C"));

        assertThat(dock.select("b")).isTrue();

        assertThat(sheetOfB.getSelectedTab().getLabel()).isEqualTo("B");
    }

    @Test
    void selectTellsTheStateListener() throws Exception {
        login();
        TabDock dock = dock("a", "b");
        sheets(dock).get(0).setSelectedTab(tab(dock, "B"));
        int[] changes = {0};
        dock.setOnStateChange(() -> changes[0]++);

        dock.select("a");
        dock.select("a");

        // the second one changes nothing
        assertThat(changes[0]).isEqualTo(1);
    }

    @Test
    void tabsExceptOneCanBeDisabled() throws Exception {
        login();
        TabDock dock = new TabDock();
        Div a = new Div();
        Div b = new Div();
        dock.add("a", "A", a);
        dock.add("b", "B", b);
        ui.add(dock);
        dock.select("b");
        // the sheet itself disables the content of the tab it does not show
        assertThat(a.getElement().isEnabled()).isFalse();

        dock.setTabsEnabled(false, "b");

        assertThat(tab(dock, "A").isEnabled()).isFalse();
        assertThat(tab(dock, "B").isEnabled()).isTrue();
        assertThat(b.getElement().isEnabled()).isTrue();

        // a tab that comes later is disabled too
        Div c = new Div();
        dock.add("c", "C", c);
        assertThat(tab(dock, "C").isEnabled()).isFalse();

        dock.setTabsEnabled(true, null);
        assertThat(tab(dock, "A").isEnabled()).isTrue();
        assertThat(tab(dock, "C").isEnabled()).isTrue();
        assertThat(b.getElement().isEnabled()).isTrue();
    }

    @Test
    void theShownContentOfADisabledTabIsDisabledAndEnabledAgain() throws Exception {
        login();
        TabDock dock = new TabDock();
        Div a = new Div();
        Div b = new Div();
        dock.add("a", "A", a);
        // the second sheet shows b
        dock.add("b", "B", b);
        ui.add(dock);
        drag(tab(dock, "B"));
        drop(zones(dock).get(zones(dock).size() - 1));
        assertThat(a.getElement().isEnabled()).isTrue();
        assertThat(b.getElement().isEnabled()).isTrue();

        dock.setTabsEnabled(false, "a");

        assertThat(a.getElement().isEnabled()).isTrue();
        assertThat(b.getElement().isEnabled()).isFalse();

        dock.setTabsEnabled(true, null);
        assertThat(b.getElement().isEnabled()).isTrue();
    }

    @Test
    void storyPartStoresTheSelectedSidebarTab() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript = manuscriptService.save(manuscript);
        ManuscriptDialog dialog = new ManuscriptDialog(manuscript);
        dialog.create();
        dialog.open();
        TabDock dock = leftBar(dialog);
        dock.add("extra", "Extra", new Div());
        sheets(dock).get(0).setSelectedTab(tab(dock, "Extra"));

        assertThat(dialog.storeState().getAsJsonObject("parts").getAsJsonObject("story").getAsJsonObject("tabs"))
                .isEqualTo(json("{\"selected\":[\"extra\"]}"));
    }

    // ---- minimizing

    private static List<Button> barIcons(TabDock dock) {
        return descendants(dock, Button.class).stream()
                .filter(button -> button.getClassNames().contains(CssStyles.TAB_DOCK_BAR_ICON)).collect(Collectors.toList());
    }

    private static Div bar(TabDock dock) {
        return divs(dock, CssStyles.TAB_DOCK_BAR).get(0);
    }

    private static void click(Button button) {
        ComponentUtil.fireEvent(button, new ClickEvent<>(button));
    }

    @Test
    void aMinimizedTabLeavesItsSheetAndShowsInTheBar() throws Exception {
        login();
        TabDock dock = dock("a", "b", "c");
        Div content = new Div();
        dock.add("b", "B", content);

        assertThat(bar(dock).isVisible()).isFalse();
        assertThat(dock.minimize("b")).isTrue();

        assertThat(captions(sheets(dock).get(0))).containsExactly("A", "C");
        assertThat(dock.isMinimized("b")).isTrue();
        assertThat(bar(dock).isVisible()).isTrue();
        assertThat(barIcons(dock)).hasSize(1);
        // the layout keeps its place, and says what is minimized
        assertThat(dock.getLayout()).isEqualTo(json("{\"columns\":[{\"groups\":[{\"tabs\":[\"a\",\"b\",\"c\"]}]}],\"minimized\":[\"b\"]}"));
        // the content is alive, not in a sheet
        assertThat(content.getParent()).isEmpty();
    }

    @Test
    void restoringATabPutsItBackWhereItWas() throws Exception {
        login();
        TabDock dock = dock("a", "b", "c");
        List<JsonObject> changes = new java.util.ArrayList<>();
        dock.minimize("b");
        dock.setOnLayoutChange(() -> changes.add(dock.getLayout()));

        click(barIcons(dock).get(0));

        assertThat(captions(sheets(dock).get(0))).containsExactly("A", "B", "C");
        assertThat(sheets(dock).get(0).getSelectedTab().getLabel()).isEqualTo("B");
        assertThat(dock.isMinimized("b")).isFalse();
        assertThat(bar(dock).isVisible()).isFalse();
        assertThat(dock.getLayout()).isEqualTo(layout("\"a\",\"b\",\"c\""));
        assertThat(changes).hasSize(1);
    }

    @Test
    void theMinimizeButtonOfATabMinimizesIt() throws Exception {
        login();
        TabDock dock = dock("a", "b");
        Button minimize = descendants(tab(dock, "B"), Button.class).get(0);
        assertThat(minimize.getClassNames()).contains(CssStyles.TAB_DOCK_MINIMIZE);

        click(minimize);

        assertThat(captions(sheets(dock).get(0))).containsExactly("A");
        assertThat(barIcons(dock)).hasSize(1);
    }

    @Test
    void aTabThatIsNotMinimizableStays() throws Exception {
        login();
        TabDock dock = dock("a", "b");
        dock.setMinimizable("a", false);

        assertThat(dock.minimize("a")).isFalse();
        assertThat(descendants(tab(dock, "A"), Button.class).get(0).isVisible()).isFalse();
        assertThat(descendants(tab(dock, "B"), Button.class).get(0).isVisible()).isTrue();
        assertThat(captions(sheets(dock).get(0))).containsExactly("A", "B");

        // also for a tab that comes later
        TabDock later = new TabDock();
        later.setMinimizable("x", false);
        later.add("x", "X", new Div());
        assertThat(later.minimize("x")).isFalse();
    }

    @Test
    void aDockThatIsNotDetachableHasNoMinimizing() throws Exception {
        login();
        TabDock dock = new TabDock(false);
        dock.add("a", "A", new Div());

        assertThat(dock.minimize("a")).isFalse();
        assertThat(descendants(dock, Button.class)).isEmpty();
    }

    @Test
    void aSheetWithOnlyMinimizedTabsComesBackWhereItWas() throws Exception {
        login();
        TabDock dock = dock("a", "b", "c");
        dock.setLayout(columns("\"a\"", null, "\"b\"", "\"c\""));
        assertThat(columnBoxes(dock)).hasSize(2);

        dock.minimize("a");
        assertThat(columnBoxes(dock)).hasSize(1);
        assertThat(dock.getLayout().getAsJsonArray("columns")).hasSize(2);

        dock.restore("a");
        assertThat(columnBoxes(dock)).hasSize(2);
        assertThat(captions(sheets(dock).get(0))).containsExactly("A");
        assertThat(captions(sheets(dock).get(1))).containsExactly("B");
    }

    @Test
    void theSheetShowsAnotherTabWhenTheSelectedOneIsMinimized() throws Exception {
        login();
        TabDock dock = dock("a", "b", "c");
        dock.select("b");

        dock.minimize("b");

        assertThat(sheets(dock).get(0).getSelectedTab().getLabel()).isIn("A", "C");
        assertThat(dock.storeState().has("selected")).isTrue();
        assertThat(dock.storeState().getAsJsonArray("selected").toString()).doesNotContain("\"b\"");

        dock.restore("b");
        assertThat(sheets(dock).get(0).getSelectedTab().getLabel()).isEqualTo("B");
    }

    @Test
    void selectingAMinimizedTabRestoresIt() throws Exception {
        login();
        TabDock dock = dock("a", "b");
        dock.minimize("b");

        assertThat(dock.select("b")).isTrue();

        assertThat(dock.isMinimized("b")).isFalse();
        assertThat(captions(sheets(dock).get(0))).containsExactly("A", "B");
        assertThat(sheets(dock).get(0).getSelectedTab().getLabel()).isEqualTo("B");
    }

    @Test
    void theNextTabSkipsTheMinimizedOnes() throws Exception {
        login();
        TabDock dock = dock("a", "b", "c");
        dock.select("a");
        dock.minimize("b");

        dock.selectRelative(1);

        assertThat(sheets(dock).get(0).getSelectedTab().getLabel()).isEqualTo("C");
    }

    @Test
    void theBarCanBeMovedToEverySide() throws Exception {
        login();
        TabDock dock = dock("a", "b");
        List<JsonObject> changes = new java.util.ArrayList<>();
        dock.setOnLayoutChange(() -> changes.add(dock.getLayout()));
        dock.minimize("b");
        changes.clear();

        assertThat(dock.getDockSide()).isEqualTo(TabDock.DockSide.LEFT);
        assertThat(dock.getClassNames()).contains(CssStyles.TAB_DOCK_AT_LEFT);

        dock.setDockSide(TabDock.DockSide.BOTTOM);

        assertThat(dock.getClassNames()).contains(CssStyles.TAB_DOCK_AT_BOTTOM).doesNotContain(CssStyles.TAB_DOCK_AT_LEFT);
        assertThat(dock.getLayout().get("dock").getAsString()).isEqualTo("bottom");
        assertThat(changes).hasSize(1);
        // no change, no event
        dock.setDockSide(TabDock.DockSide.BOTTOM);
        assertThat(changes).hasSize(1);
        for (TabDock.DockSide side : TabDock.DockSide.values()) {
            dock.setDockSide(side);
            assertThat(dock.getDockSide()).isEqualTo(side);
            assertThat(dock.getClassNames().stream().filter(name -> name.startsWith("tab-dock-at-"))).hasSize(1);
        }
    }

    @Test
    void theMoveButtonSendsTheBarToTheNextSideClockwise() throws Exception {
        login();
        TabDock dock = dock("a", "b");
        dock.minimize("b");
        Button move = descendants(bar(dock), Button.class).stream()
                .filter(button -> button.getClassNames().contains(CssStyles.TAB_DOCK_BAR_MOVE)).findFirst().orElseThrow();

        List<TabDock.DockSide> sides = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            click(descendants(bar(dock), Button.class).stream()
                    .filter(button -> button.getClassNames().contains(CssStyles.TAB_DOCK_BAR_MOVE)).findFirst().orElseThrow());
            sides.add(dock.getDockSide());
        }

        assertThat(move).isNotNull();
        assertThat(sides).containsExactly(TabDock.DockSide.TOP, TabDock.DockSide.RIGHT, TabDock.DockSide.BOTTOM, TabDock.DockSide.LEFT);
    }

    @Test
    void theMinimizedTabsAndTheSideAreAppliedWithTheLayout() throws Exception {
        login();
        TabDock source = dock("a", "b", "c");
        source.minimize("c");
        source.minimize("a");
        source.setDockSide(TabDock.DockSide.RIGHT);
        JsonObject saved = source.getLayout();
        assertThat(saved.getAsJsonArray("minimized").toString()).isEqualTo("[\"c\",\"a\"]");

        TabDock dock = dock("a", "b", "c");
        dock.setLayout(saved);

        assertThat(captions(sheets(dock).get(0))).containsExactly("B");
        assertThat(barIcons(dock)).hasSize(2);
        assertThat(dock.getDockSide()).isEqualTo(TabDock.DockSide.RIGHT);
        assertThat(dock.getLayout()).isEqualTo(saved);

        // a layout without them restores everything
        dock.setLayout(layout("\"a\",\"b\",\"c\""));
        assertThat(captions(sheets(dock).get(0))).containsExactly("A", "B", "C");
        assertThat(bar(dock).isVisible()).isFalse();
        assertThat(dock.getDockSide()).isEqualTo(TabDock.DockSide.LEFT);
    }

    @Test
    void aTabThatComesLaterIsMinimizedWhenTheLayoutSaysSo() throws Exception {
        login();
        TabDock dock = dock("a");
        dock.setLayout(json("{\"columns\":[{\"groups\":[{\"tabs\":[\"a\",\"extra\"]}]}],\"minimized\":[\"extra\"]}"));
        assertThat(barIcons(dock)).isEmpty();

        dock.add("extra", "Extra", new Div());

        assertThat(captions(sheets(dock).get(0))).containsExactly("A");
        assertThat(barIcons(dock)).hasSize(1);
    }

    @Test
    void theIconOfATabHasItsDescription() throws Exception {
        login();
        TabDock dock = new TabDock();
        dock.add("a", "A", new Div());
        dock.add("b", "Side Query", "SQ", new Div());
        dock.minimize("b");

        Button icon = barIcons(dock).get(0);
        assertThat(icon.getAriaLabel()).hasValueSatisfying(label -> assertThat(label).endsWith("Side Query"));
        assertThat(descendants(icon, com.vaadin.flow.component.html.Image.class).get(0).getAlt()).hasValue("Side Query");
        assertThat(descendants(icon, com.vaadin.flow.component.html.Image.class).get(0).getSrc()).startsWith("data:image/svg+xml;base64,");
    }

    @Test
    void storyPartKeepsTheMinimizedTabsInTheManuscript() throws Exception {
        login();
        Manuscript manuscript = book();
        ManuscriptDialog dialog = opened(manuscript);
        TabDock dock = leftBar(dialog);
        attached(dock);

        // the story is the one tab that has no button
        assertThat(descendants(tab(dock, "Story"), Button.class).get(0).isVisible()).isFalse();
        assertThat(dock.minimize(STORY)).isFalse();
        dock.minimize(LOREBOOK);
        dock.setDockSide(TabDock.DockSide.TOP);

        JsonObject stored = manuscriptService.find(manuscript).getAttributes()
                .getAsJsonObject(ManuscriptStoryPart.TABS_CONFIG_ATTRIBUTE);
        assertThat(stored.getAsJsonArray("minimized").toString()).isEqualTo("[\"" + LOREBOOK + "\"]");
        assertThat(stored.get("dock").getAsString()).isEqualTo("top");

        // a dialog opened later has them as they were left
        TabDock reopened = leftBar(opened(manuscriptService.find(manuscript)));
        assertThat(reopened.isMinimized(LOREBOOK)).isTrue();
        assertThat(reopened.getDockSide()).isEqualTo(TabDock.DockSide.TOP);
    }

    @Test
    void theDefaultLayoutOfTheUserIsUsedByBooksWithoutOne() throws Exception {
        login();
        ManuscriptDialog dialog = opened(book());
        TabDock dock = leftBar(dialog);
        attached(dock);
        dock.minimize(ANNOTATIONS);
        dock.setDockSide(TabDock.DockSide.RIGHT);
        JsonObject arranged = dock.getLayout();

        storyPart(dialog).saveLayoutAsDefault();

        // a book that has no arrangement opens with it, and does not store it
        Manuscript other = book();
        TabDock otherDock = leftBar(opened(other));
        assertThat(otherDock.getLayout()).isEqualTo(arranged);
        assertThat(manuscriptService.find(other).getAttributes().has(ManuscriptStoryPart.TABS_CONFIG_ATTRIBUTE)).isFalse();

        // a book with its own arrangement keeps it
        Manuscript own = book();
        own.getAttributes().add(ManuscriptStoryPart.TABS_CONFIG_ATTRIBUTE, StoryDockLayout.defaultLayout(STORY, null, CHAPTERS, LOREBOOK, ANNOTATIONS));
        own = manuscriptService.save(own);
        TabDock ownDock = leftBar(opened(own));
        assertThat(ownDock.isMinimized(ANNOTATIONS)).isFalse();
        assertThat(ownDock.getDockSide()).isEqualTo(TabDock.DockSide.LEFT);
    }

    @Test
    void restoreDefaultLayoutGoesToTheDefaultOfTheUser() throws Exception {
        login();
        ManuscriptDialog dialog = opened(book());
        TabDock dock = leftBar(dialog);
        attached(dock);
        dock.minimize(ANNOTATIONS);
        storyPart(dialog).saveLayoutAsDefault();
        dock.restore(ANNOTATIONS);
        dock.minimize(LOREBOOK);

        java.lang.reflect.Method restore = ManuscriptStoryPart.class.getDeclaredMethod("restoreDefaultLayout");
        restore.setAccessible(true);
        restore.invoke(storyPart(dialog));

        assertThat(dock.isMinimized(ANNOTATIONS)).isTrue();
        assertThat(dock.isMinimized(LOREBOOK)).isFalse();
    }

    /**
     * The content of a dialog is not in the UI in the test, the drag and drop events need it: the dock is moved there.
     */
    private void attached(TabDock dock) {
        dock.removeFromParent();
        ui.add(dock);
    }

    private static ManuscriptStoryPart storyPart(ManuscriptDialog dialog) throws Exception {
        java.lang.reflect.Field story = ManuscriptDialog.class.getDeclaredField("storyPart");
        story.setAccessible(true);
        return (ManuscriptStoryPart) story.get(dialog);
    }

    private static TabDock leftBar(ManuscriptDialog dialog) throws Exception {
        java.lang.reflect.Field bar = ManuscriptStoryPart.class.getDeclaredField("leftBar");
        bar.setAccessible(true);
        return (TabDock) bar.get(storyPart(dialog));
    }

}
