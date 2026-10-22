package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.google.gson.JsonPrimitive;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The review of merging a part into the one before it: the combined prompt fields, the data of both parts and the merge.
 */
class PartMergeDialogTest extends MarginaliaTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;

    private Manuscript book;
    private ChatMessage first;
    private ChatMessage second;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        Manuscript m = new Manuscript();
        m.setName(uniqueName("book"));
        book = manuscriptService.save(m);
        first = chatMessageService.find(chatMessageService.createPart(book, null, "He drew", new TurnInput(null, null, null, "Write the duel. Short")).getId());
        first.getAttributes().add("mood", new JsonPrimitive("calm"));
        chatMessageService.save(first);
        second = chatMessageService.find(chatMessageService.createPart(book, first, "his sword.", new TurnInput(null, null, null, "Write the duel. Add rain")).getId());
        second.getAttributes().add("mood", new JsonPrimitive("angry"));
        chatMessageService.save(second);
    }

    private static Stream<Component> all(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(PartMergeDialogTest::all));
    }

    @Test
    void showsTheCombinedPromptAndTheDifferingData() {
        PartMergeDialog dialog = open(new AtomicReference<>());

        List<Component> components = new ArrayList<>(all(dialog).toList());
        dialog.getFooter().getElement().getChildren().forEach(e -> e.getComponent().ifPresent(components::add));
        List<String> instructions = components.stream().filter(TextArea.class::isInstance).map(c -> ((TextArea) c).getValue()).toList();
        assertThat(instructions).contains("Write the duel. Short\nAdd rain");

        Grid<?> grid = (Grid<?>) components.stream().filter(Grid.class::isInstance).findFirst().orElseThrow();
        assertThat(grid.getListDataView().getItemCount()).isEqualTo(1);
    }

    @Test
    void mergeButtonMergesAndReportsThePreviousPart() throws Exception {
        AtomicReference<ChatMessage> merged = new AtomicReference<>();
        PartMergeDialog dialog = open(merged);

        List<Component> components = new ArrayList<>(all(dialog).toList());
        dialog.getFooter().getElement().getChildren().forEach(e -> e.getComponent().ifPresent(components::add));
        Button merge = (Button) components.stream().filter(c -> c instanceof Button b && "primary".equals(b.getThemeName())).findFirst().orElseThrow();
        merge.click();

        assertThat(merged.get().getId()).isEqualTo(first.getId());
        ChatMessage stored = chatMessageService.find(first.getId());
        assertThat(stored.getResponse()).isEqualTo("He drew his sword.");
        assertThat(stored.getAttributes().get("mood").getAsString()).isEqualTo("angry");
        assertThat(chatMessageService.find(second.getId()).isDeleted()).isTrue();
    }

    private PartMergeDialog open(AtomicReference<ChatMessage> merged) {
        PartMergeDialog dialog = PartMergeDialog.create(book, second, merged::set);
        assertThat(dialog).isNotNull();
        return dialog;
    }
}
