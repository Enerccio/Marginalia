package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.JsonMergeNode;
import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.JsonMergeNode.Choice;
import com.github.enerccio.marginalia.domain.service.agent.jsonmerge.JsonText;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.google.gson.JsonParser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The merge of JSON data as the user sees it: the tree of the versions, what they can do with a place, and the text of the agent's
 * data that is checked when they go back to the tree.
 */
class JsonMergeDialogTest extends AgentTestBase {

    // UI.setCurrent keeps only a weak reference
    private UI ui;
    private final AtomicReference<String> taken = new AtomicReference<>();

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        loginAgentUser();
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private JsonMergeDialog dialog(String base, String agent, String book) {
        JsonMergeNode root = JsonMergeNode.root(JsonText.parse(base), JsonText.parse(agent), JsonText.parse(book));
        JsonMergeDialog dialog = new JsonMergeDialog("data", root, taken::set);
        dialog.create();
        return dialog;
    }

    private static List<JsonMergeNode> children(JsonMergeDialog dialog, JsonMergeNode parent) {
        return dialog.getGrid().getDataProvider().fetchChildren(new HierarchicalQuery<>(null, parent)).toList();
    }

    private static JsonMergeNode place(JsonMergeDialog dialog, JsonMergeNode parent, String title) {
        return children(dialog, parent).stream().filter(n -> n.title().startsWith(title)).findFirst().orElseThrow();
    }

    private static List<Component> descendants(Component component) {
        List<Component> found = new ArrayList<>();
        component.getChildren().forEach(child -> {
            found.add(child);
            found.addAll(descendants(child));
        });
        return found;
    }

    private static Button button(JsonMergeDialog dialog, String text) {
        return descendants(dialog).stream().filter(c -> c instanceof Button b && text.equals(b.getText())).map(c -> (Button) c)
                .findFirst().orElseThrow(() -> new AssertionError("no button " + text));
    }

    private void select(JsonMergeDialog dialog, JsonMergeNode node) {
        dialog.getGrid().select(node);
    }

    // --- the tree

    @Test
    void theWholeDocumentIsTheFirstPlaceAndItsChangesAreCounted() {
        JsonMergeDialog dialog = dialog("{\"a\":1,\"b\":1,\"c\":1}", "{\"a\":2,\"b\":1,\"c\":1}", "{\"a\":3,\"b\":1,\"c\":9}");

        assertThat(children(dialog, null)).containsExactly(dialog.getLevel());
        assertThat(dialog.getStatusLabel().getText()).isEqualTo("1 conflicts, 1 not decided");
        assertThat(dialog.getBreadcrumb().getComponentCount()).isEqualTo(1);
    }

    @Test
    void onlyThePlacesThatChangedAreShownUntilTheUserAsksForAll() {
        JsonMergeDialog dialog = dialog("{\"a\":1,\"b\":1,\"c\":1}", "{\"a\":2,\"b\":1,\"c\":1}", "{\"a\":1,\"b\":1,\"c\":9}");

        assertThat(children(dialog, dialog.getLevel())).extracting(JsonMergeNode::title).containsExactly("a", "c");
        dialog.getOnlyChanges().setValue(false);
        assertThat(children(dialog, dialog.getLevel())).extracting(JsonMergeNode::title).containsExactly("a", "b", "c");
    }

    @Test
    void theChildrenOfAPlaceAreMadeWhenItIsOpened() {
        JsonMergeDialog dialog = dialog("{\"o\":{\"p\":1}}", "{\"o\":{\"p\":2}}", "{\"o\":{\"p\":1}}");
        JsonMergeNode o = place(dialog, dialog.getLevel(), "o");

        assertThat(o.isLoaded()).isFalse();
        assertThat(dialog.getGrid().getDataProvider().hasChildren(o)).isTrue();
        assertThat(o.isLoaded()).isFalse();
        assertThat(children(dialog, o)).extracting(JsonMergeNode::title).containsExactly("p");
        assertThat(o.isLoaded()).isTrue();
    }

    @Test
    void aPlaceTakesTheDataOfTheVersionTheUserPicks() {
        JsonMergeDialog dialog = dialog("{\"a\":1}", "{\"a\":2}", "{\"a\":3}");
        JsonMergeNode a = place(dialog, dialog.getLevel(), "a");
        assertThat(a.conflict()).isTrue();

        select(dialog, a);
        button(dialog, "Use the book's").click();

        assertThat(a.choice()).isEqualTo(Choice.BOOK);
        assertThat(dialog.getStatusLabel().getText()).isEqualTo("1 conflicts, 0 not decided");
        dialog.getTakeButton().click();
        assertThat(taken.get()).isEqualTo("{\"a\":3}");
    }

    @Test
    void aPlaceCanBeLeftOutAndPutTogetherAgain() {
        JsonMergeDialog dialog = dialog("{\"o\":{\"p\":1}}", "{\"o\":{\"p\":2}}", "{\"o\":{\"p\":1}}");
        JsonMergeNode o = place(dialog, dialog.getLevel(), "o");

        select(dialog, o);
        button(dialog, "Leave out").click();
        assertThat(JsonText.compact(dialog.getLevel().effectiveResult())).isEqualTo("{}");
        assertThat(button(dialog, "Put together from inside").isEnabled()).isTrue();
        button(dialog, "Put together from inside").click();
        assertThat(JsonText.compact(dialog.getLevel().effectiveResult())).isEqualTo("{\"o\":{\"p\":2}}");
    }

    @Test
    void takingTheResultGivesTheDataOfTheWholeDocument() {
        JsonMergeDialog dialog = dialog("{\"a\":1,\"b\":1}", "{\"a\":2,\"b\":1}", "{\"a\":1,\"b\":5}");

        dialog.getTakeButton().click();

        assertThat(taken.get()).isEqualTo("{\"a\":2,\"b\":5}");
    }

    @Test
    void theCellsShowWhatEachVersionHolds() {
        assertThat(JsonMergeDialog.preview(null)).isEqualTo("—");
        assertThat(JsonMergeDialog.preview(JsonParser.parseString("{\"a\":1,\"b\":2}"))).isEqualTo("{2}");
        assertThat(JsonMergeDialog.preview(JsonParser.parseString("[1,2,3]"))).isEqualTo("[3]");
        assertThat(JsonMergeDialog.preview(JsonParser.parseString("\"text\""))).isEqualTo("\"text\"");
        assertThat(JsonMergeDialog.preview(JsonParser.parseString("\"" + "x".repeat(200) + "\""))).hasSize(81).endsWith("…");
    }

    // --- the elements of a list

    @Test
    void anElementThatWasMatchedByGuessCanBeSplit() {
        JsonMergeDialog dialog = dialog("[{\"name\":\"a\",\"v\":1}]", "[{\"name\":\"a\",\"v\":2}]", "[{\"name\":\"a\",\"v\":1}]");
        dialog.getOnlyChanges().setValue(false);
        assertThat(children(dialog, dialog.getLevel())).hasSize(1);
        JsonMergeNode element = children(dialog, dialog.getLevel()).getFirst();
        assertThat(button(dialog, "Split").isEnabled()).isFalse();

        select(dialog, element);
        assertThat(button(dialog, "Split").isEnabled()).isTrue();
        button(dialog, "Split").click();

        assertThat(children(dialog, dialog.getLevel())).hasSize(3);
    }

    // --- opening a place

    @Test
    void aPlaceCanBeOpenedToWorkWithItAlone() {
        JsonMergeDialog dialog = dialog("{\"o\":{\"p\":1}}", "{\"o\":{\"p\":2}}", "{\"o\":{\"p\":1}}");
        JsonMergeNode o = place(dialog, dialog.getLevel(), "o");
        select(dialog, o);

        button(dialog, "Open").click();

        assertThat(dialog.getLevel()).isSameAs(o);
        assertThat(children(dialog, null)).containsExactly(o);
        // the way back
        assertThat(dialog.getBreadcrumb().getChildren().filter(c -> c instanceof Button).count()).isEqualTo(2);
        Button back = (Button) dialog.getBreadcrumb().getChildren().filter(c -> c instanceof Button).findFirst().orElseThrow();
        assertThat(back.getText()).isEqualTo("$");
        back.click();
        assertThat(dialog.getLevel().isRoot()).isTrue();
    }

    // --- the agent's data as text

    private void showText(JsonMergeDialog dialog) {
        dialog.getTabs().setSelectedTab(dialog.getTextTab());
    }

    @Test
    void theAgentsDataIsShownPrettyPrintedInTheTextView() {
        JsonMergeDialog dialog = dialog("{\"a\":1}", "{\"a\":2,\"l\":[1,2]}", "{\"a\":1}");

        showText(dialog);

        assertThat(dialog.getTextArea().getValue()).isEqualTo("{\n  \"a\": 2,\n  \"l\": [\n    1,\n    2\n  ]\n}");
        assertThat(dialog.getTextArea().isReadOnly()).isFalse();
    }

    @Test
    void theOtherVersionsAreShownReadOnly() {
        JsonMergeDialog dialog = dialog("{\"a\":1}", "{\"a\":2}", "{\"a\":3}");
        showText(dialog);

        dialog.getViewSelect().setValue(JsonMergeDialog.View.BOOK);

        assertThat(dialog.getTextArea().getValue()).isEqualTo("{\n  \"a\": 3\n}");
        assertThat(dialog.getTextArea().isReadOnly()).isTrue();
        dialog.getViewSelect().setValue(JsonMergeDialog.View.RESULT);
        assertThat(dialog.getTextArea().getValue()).isEqualTo("{\n  \"a\": 2\n}");
        dialog.getViewSelect().setValue(JsonMergeDialog.View.BASE);
        assertThat(dialog.getTextArea().getValue()).isEqualTo("{\n  \"a\": 1\n}");
    }

    @Test
    void textThatIsJsonIsTakenWhenTheUserGoesBackToTheTree() {
        JsonMergeDialog dialog = dialog("{\"a\":1}", "{\"a\":2}", "{\"a\":3}");
        assertThat(dialog.getLevel().conflict()).isTrue();
        showText(dialog);

        dialog.getTextArea().setValue("{\"a\": 3, \"b\": true}");
        dialog.getTabs().setSelectedIndex(0);

        assertThat(dialog.getTabs().getSelectedTab()).isNotSameAs(dialog.getTextTab());
        assertThat(dialog.getLevel().agentText()).isEqualTo("{\n  \"a\": 3,\n  \"b\": true\n}");
        assertThat(dialog.getLevel().conflict()).isFalse();
        assertThat(dialog.getStatusLabel().getText()).isEqualTo("0 conflicts, 0 not decided");
    }

    @Test
    void textThatIsNotJsonKeepsTheUserInTheTextAndChangesNothing() {
        JsonMergeDialog dialog = dialog("{\"a\":1}", "{\"a\":2}", "{\"a\":1}");
        showText(dialog);

        dialog.getTextArea().setValue("{\"a\": 2,}");
        assertThat(dialog.getTextArea().isInvalid()).isTrue();
        dialog.getTabs().setSelectedIndex(0);

        assertThat(dialog.getTabs().getSelectedTab()).isSameAs(dialog.getTextTab());
        assertThat(dialog.getLevel().agentText()).isEqualTo("{\n  \"a\": 2\n}");
        // nor can the result be taken, or another version shown
        dialog.getTakeButton().click();
        assertThat(taken.get()).isNull();
        dialog.getViewSelect().setValue(JsonMergeDialog.View.BOOK);
        assertThat(dialog.getViewSelect().getValue()).isEqualTo(JsonMergeDialog.View.AGENT);

        dialog.getTextArea().setValue("{\"a\": 5}");
        assertThat(dialog.getTextArea().isInvalid()).isFalse();
        dialog.getTakeButton().click();
        assertThat(taken.get()).isEqualTo("{\"a\":5}");
    }

    @Test
    void theTextOfTheOpenPlaceIsTheDataOfThatPlace() {
        JsonMergeDialog dialog = dialog("{\"o\":{\"p\":1},\"q\":1}", "{\"o\":{\"p\":2},\"q\":1}", "{\"o\":{\"p\":1},\"q\":7}");
        select(dialog, place(dialog, dialog.getLevel(), "o"));
        button(dialog, "Open").click();
        showText(dialog);

        assertThat(dialog.getTextArea().getValue()).isEqualTo("{\n  \"p\": 2\n}");
        dialog.getTextArea().setValue("{\"p\": 9}");
        dialog.getTabs().setSelectedIndex(0);

        dialog.getTakeButton().click();
        assertThat(taken.get()).isEqualTo("{\"o\":{\"p\":9},\"q\":7}");
    }

    @Test
    void startingOverGivesUpWhatWasDecidedAndWritten() {
        JsonMergeDialog dialog = dialog("{\"a\":1}", "{\"a\":2}", "{\"a\":3}");
        select(dialog, place(dialog, dialog.getLevel(), "a"));
        button(dialog, "Use the book's").click();
        showText(dialog);
        dialog.getTextArea().setValue("{\"a\": 3}");
        dialog.getTabs().setSelectedIndex(0);

        dialog.getStartOverButton().click();

        assertThat(dialog.getLevel().agentText()).isEqualTo("{\n  \"a\": 2\n}");
        assertThat(dialog.getStatusLabel().getText()).isEqualTo("1 conflicts, 1 not decided");
    }

    @Test
    void theStatusOfAPlaceIsSaidInWords() {
        JsonMergeDialog dialog = dialog("{\"a\":1,\"b\":1,\"c\":1,\"d\":1}", "{\"a\":2,\"b\":1,\"c\":2,\"d\":1}",
                "{\"a\":1,\"b\":2,\"c\":3,\"d\":1}");
        dialog.getOnlyChanges().setValue(false);

        List<String> states = children(dialog, dialog.getLevel()).stream().map(n -> dialog.statusBadge(n).getText()).toList();

        assertThat(states).containsExactly("Agent changed", "Book changed", "Conflict", "Unchanged");
    }
}
