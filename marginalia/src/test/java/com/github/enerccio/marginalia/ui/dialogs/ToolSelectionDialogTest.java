package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.service.ToolService;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.tool.*;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.ui.widgets.ToolSelectionButton;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Choosing tools: the sections of the providers (items with a filter of their own, or one check box), the hint of a
 * provider, the button with the number that is on, and what the dialog gives back.
 */
class ToolSelectionDialogTest extends AgentTestBase {

    @Autowired
    private ToolService toolService;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;

    private static final String BUNDLE = "tool-dialog-test";

    private static final ToolProvider NAMES = new ToolProvider() {
        @Override
        public List<ToolItem> items() {
            return IntStream.rangeClosed(1, 30).mapToObj(i -> new ToolItem("n" + i, i % 10 == 0 ? "Elvish " + i : "Dwarvish " + i)).toList();
        }

        @Override
        public FunctionSet functions(Collection<String> items, ToolRequest request) {
            return FunctionSet.empty();
        }

        @Override
        public String defaultHint() {
            return "Default names hint";
        }
    };

    private static final ToolProvider PLACES = (items, request) -> FunctionSet.empty();

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        loginAgentUser();
        toolService.registerProvider(BUNDLE, "names", "Name generator", NAMES);
        toolService.registerProvider(BUNDLE, "places", "Place provider", PLACES);
    }

    @AfterEach
    void tearDown() {
        toolService.unregisterAll(BUNDLE);
        UI.setCurrent(null);
        ui = null;
    }

    private static Stream<Component> descendants(Component component) {
        return Stream.concat(Stream.of(component), component.getChildren().flatMap(ToolSelectionDialogTest::descendants));
    }

    private static <T extends Component> List<T> all(Component root, Class<T> type) {
        return descendants(root).filter(type::isInstance).map(type::cast).toList();
    }

    private ToolSelectionDialog dialog(ToolSelection selection, AtomicReference<ToolSelection> saved) {
        List<ToolProviderInfo> providers = toolService.getProviders().stream().filter(p -> p.ref().startsWith(BUNDLE)).toList();
        return new ToolSelectionDialog(providers, selection, saved::set);
    }

    /** Clicks the button with the text, in the footer (Save) or in a section (Check shown). */
    private static void click(ToolSelectionDialog dialog, String text) {
        Stream<Component> footer = dialog.getFooter().getElement().getChildren().map(e -> e.getComponent().orElseThrow());
        Stream.concat(footer, descendants(dialog)).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> text.equals(b.getText())).findFirst().orElseThrow().click();
    }

    @Test
    void thereIsASectionForEachProviderUnderItsName() {
        ToolSelectionDialog dialog = dialog(null, new AtomicReference<>());

        List<Details> sections = all(dialog, Details.class).stream().filter(d -> d.getSummary() != null).toList();

        assertThat(all(dialog, Grid.class)).hasSize(1);
        assertThat(all(dialog, Checkbox.class)).hasSize(1);
        assertThat(sections.stream().map(d -> d.getSummary().getElement().getText())).contains("Name generator", "Place provider");
    }

    @Test
    void savingWithoutChangesGivesBackTheSameChoice() {
        ToolSelection selection = new ToolSelection().withItems(ToolSelection.ref(BUNDLE, "names"), List.of("n3", "n1"));
        selection.entry(ToolSelection.ref(BUNDLE, "names")).setHint("My hint");
        selection.entry(ToolSelection.ref(BUNDLE, "places")).setEnabled(true);
        AtomicReference<ToolSelection> saved = new AtomicReference<>();

        ToolSelectionDialog dialog = dialog(selection, saved);
        click(dialog, "Save");

        // the items come back in the order of the provider
        assertThat(saved.get().get(ToolSelection.ref(BUNDLE, "names")).getItems()).containsExactly("n1", "n3");
        assertThat(saved.get().get(ToolSelection.ref(BUNDLE, "names")).getHint()).isEqualTo("My hint");
        assertThat(saved.get().get(ToolSelection.ref(BUNDLE, "places")).isEnabled()).isTrue();
        // the choice that was passed in is not changed
        assertThat(selection.get(ToolSelection.ref(BUNDLE, "names")).getItems()).containsExactly("n3", "n1");
    }

    @Test
    void theFilterOfASectionKeepsCheckedItemsAndCheckShownChecksOnlyWhatIsShown() {
        ToolSelection selection = new ToolSelection().withItems(ToolSelection.ref(BUNDLE, "names"), List.of("n1"));
        AtomicReference<ToolSelection> saved = new AtomicReference<>();
        ToolSelectionDialog dialog = dialog(selection, saved);
        @SuppressWarnings("unchecked")
        Grid<ToolItem> grid = all(dialog, Grid.class).getFirst();
        TextField filter = all(dialog, TextField.class).getFirst();

        filter.setValue("elvish");

        // 3 Elvish items and the checked one
        assertThat(grid.getLazyDataView().getItems().count()).isEqualTo(4);
        click(dialog, "Check shown");
        click(dialog, "Save");
        assertThat(saved.get().get(ToolSelection.ref(BUNDLE, "names")).getItems()).containsExactly("n1", "n10", "n20", "n30");
    }

    @Test
    void aProviderWithoutItemsIsOnOrOffAndTheHintIsKeptWhenWritten() {
        AtomicReference<ToolSelection> saved = new AtomicReference<>();
        ToolSelectionDialog dialog = dialog(null, saved);
        all(dialog, Checkbox.class).getFirst().setValue(true);
        // the first text area belongs to the first provider: the placeholder is its default hint
        List<TextArea> hints = all(dialog, TextArea.class);
        assertThat(hints.getFirst().getPlaceholder()).isEqualTo("Default names hint");
        hints.getLast().setValue("  Look places up.  ");

        click(dialog, "Save");

        assertThat(saved.get().get(ToolSelection.ref(BUNDLE, "places")).isEnabled()).isTrue();
        assertThat(saved.get().get(ToolSelection.ref(BUNDLE, "places")).getHint()).isEqualTo("  Look places up.  ");
        // nothing chosen and no hint: not kept
        assertThat(saved.get().get(ToolSelection.ref(BUNDLE, "names"))).isNull();
    }

    @Test
    void theButtonShowsTheNumberThatIsOnAndHidesWhenNothingIsInstalled() {
        ToolSelectionButton button = new ToolSelectionButton();
        assertThat(button.isVisible()).isTrue();
        assertThat(button.getText()).isEqualTo("Tools");

        button.setValue(new ToolSelection().withItems(ToolSelection.ref(BUNDLE, "names"), List.of("n1", "n2", "gone")));
        assertThat(button.getText()).isEqualTo("Tools (2)");

        List<ToolSelection> told = new ArrayList<>();
        button.addValueChangeListener(told::add);
        button.click();
        assertThat(told).isEmpty();

        toolService.getProviders().forEach(p -> toolService.unregisterProvider(p.ref().split("/")[0], p.ref().split("/")[1]));
        button.refresh();
        assertThat(button.isVisible()).isFalse();
    }
}
