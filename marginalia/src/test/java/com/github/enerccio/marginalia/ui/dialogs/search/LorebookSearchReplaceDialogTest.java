package com.github.enerccio.marginalia.ui.dialogs.search;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.search.SearchReplaceDialog.State;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The search and replace dialog of a lorebook on the desktop and on the phone, driven as in
 * {@link ManuscriptSearchReplaceDialogTest}.
 */
class LorebookSearchReplaceDialogTest extends MarginaliaTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService entryService;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;
    private Lorebook lorebook;
    private LorebookEntry harry;
    private final AtomicInteger replaced = new AtomicInteger();

    @BeforeEach
    void createLorebook() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        Lorebook l = new Lorebook();
        l.setName("lore");
        lorebook = lorebookService.save(l);
        harry = entry("Harry", "The boy who lived.");
        entry("Hogwarts", "Harry studies here.");
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private LorebookEntry entry(String name, String payload) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        entry.setPayload(payload);
        return entryService.save(entry);
    }

    private LorebookSearchReplaceDialog dialog(boolean phone) {
        LorebookSearchReplaceDialog dialog = new LorebookSearchReplaceDialog(lorebook, phone) {
            // no session to lock: the worker thread changes the components itself
            @Override
            public void vaadinLocked(Runnable r) {
                vaadinLockedSync(r);
            }

            @Override
            public void vaadinLockedSync(Runnable r) {
                UI.setCurrent(ui);
                try {
                    r.run();
                } finally {
                    UI.setCurrent(null);
                }
            }
        };
        dialog.onReplaced(replaced::incrementAndGet);
        return dialog;
    }

    private static Stream<Component> all(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(LorebookSearchReplaceDialogTest::all));
    }

    private static Button button(Component root, String text) {
        return all(root).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> text.equals(b.getText())).findFirst().orElseThrow();
    }

    private static TextField field(Component root, String label) {
        return all(root).filter(TextField.class::isInstance).map(TextField.class::cast)
                .filter(b -> label.equals(b.getLabel())).findFirst().orElseThrow();
    }

    private static void await(BooleanSupplier condition) throws InterruptedException {
        for (int i = 0; i < 500 && !condition.getAsBoolean(); i++) {
            Thread.sleep(20);
        }
        assertThat(condition.getAsBoolean()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void searchesAndReplacesTheEntries(boolean phone) throws Exception {
        LorebookSearchReplaceDialog dialog = dialog(phone);
        // only the options of the search, the lorebook has no summaries or branches
        assertThat(all(dialog).filter(com.vaadin.flow.component.checkbox.Checkbox.class::isInstance).count()).isEqualTo(2);
        field(dialog, "Search for").setValue("Harry");
        field(dialog, "Replace with").setValue("Ron");

        button(dialog, "Search").click();
        await(() -> dialog.getState() == State.PREPARED);

        assertThat(button(dialog, "Replace").isEnabled()).isTrue();
        assertThat(entryService.find(harry.getId()).getName()).isEqualTo("Harry");

        button(dialog, "Replace").click();
        await(() -> replaced.get() == 1);

        assertThat(dialog.getState()).isEqualTo(State.INPUT);
        assertThat(entryService.find(harry.getId()).getName()).isEqualTo("Ron");
        assertThat(entryService.getEntriesForLorebook(lorebook)).extracting(LorebookEntry::getPayload)
                .containsExactlyInAnyOrder("The boy who lived.", "Ron studies here.");
    }

}
