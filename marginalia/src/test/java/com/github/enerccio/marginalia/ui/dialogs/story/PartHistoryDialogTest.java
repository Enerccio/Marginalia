package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.RevisionClearing;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.components.RevisionRow;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.MessageRevisionService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The version history of a part as the author sees it: the list, the text of a version as a difference, putting a version back or
 * going back to it, labels, the trash.
 */
class PartHistoryDialogTest extends MarginaliaTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private MessageRevisionService messageRevisionService;

    @Autowired
    private Localization loc;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;

    private Manuscript book;
    private ChatMessage part;
    private AtomicReference<ChatMessage> changed;
    private PartHistoryDialog dialog;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
        part = chatMessageService.createPart(book, null, "One.", null);
        chatMessageService.updateResponse(book, part, "Two.");
        chatMessageService.updateResponse(book, part, "Three. And more.");
        open();
    }

    private void open() throws Exception {
        changed = new AtomicReference<>();
        dialog = new PartHistoryDialog(book, chatMessageService.find(part.getId()), changed::set);
    }

    private List<RevisionRow> rows() {
        return dialog.grid.getListDataView().getItems().toList();
    }

    private RevisionRow rowOf(String text) throws Exception {
        for (RevisionRow row : messageRevisionService.list(part, true)) {
            if (messageRevisionService.find(row.id()).getResponse().equals(text)) {
                return row;
            }
        }
        throw new AssertionError("no version " + text);
    }

    private RevisionRow selected() {
        return dialog.grid.getSelectedItems().iterator().next();
    }

    private static Stream<Component> all(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(PartHistoryDialogTest::all));
    }

    @Test
    void opensOnTheVersionAHitWasFoundIn() throws Exception {
        RevisionRow one = rowOf("One.");

        dialog = PartHistoryDialog.create(book, chatMessageService.find(part.getId()), changed::set, one.uuid());

        assertThat(selected().id()).isEqualTo(one.id());
        assertThat(dialog.plain.getText()).contains("One.");
    }

    @Test
    void anUnknownVersionLeavesTheNewestSelected() throws Exception {
        dialog = PartHistoryDialog.create(book, chatMessageService.find(part.getId()), changed::set, "no-such-version");

        assertThat(selected().id()).isEqualTo(rows().getFirst().id());
    }

    @Test
    void listsTheVersionsNewestFirstAndSelectsTheNewest() {
        assertThat(rows()).hasSize(3);
        assertThat(rows().getFirst().id()).isEqualTo(selected().id());
        assertThat(dialog.empty.isVisible()).isFalse();
        assertThat(dialog.meta.getText()).contains(loc.getValue(loc.getRevisionSource(RevisionSource.HUMAN)));
        assertThat(dialog.plain.getText()).isEmpty();
        // the newest version is the text of the part: nothing to put back, nothing to go back from, nothing to delete
        assertThat(dialog.restore.isEnabled()).isFalse();
        assertThat(dialog.rollBack.isEnabled()).isFalse();
        assertThat(dialog.delete.isEnabled()).isFalse();
        assertThat(dialog.label.isEnabled()).isTrue();
        assertThat(dialog.purge.isVisible()).isFalse();
    }

    @Test
    void anOlderVersionIsShownAsTheDifferenceToThePreviousOneOrTheCurrentTextOrAsItIs() throws Exception {
        dialog.select(rowOf("Two.").id());

        assertThat(dialog.restore.isEnabled()).isTrue();
        assertThat(dialog.rollBack.isEnabled()).isTrue();
        assertThat(dialog.delete.isEnabled()).isTrue();
        assertThat(dialog.compare.getValue().name()).isEqualTo("PREVIOUS");
        assertThat(all(dialog).filter(c -> c.getClass().getSimpleName().equals("WordDiffView")).anyMatch(Component::isVisible)).isTrue();
        assertThat(dialog.plain.isVisible()).isFalse();

        dialog.compare.setValue(PartHistoryDialog.Compare.NONE);
        assertThat(dialog.plain.isVisible()).isTrue();
        assertThat(dialog.plain.getText()).isEqualTo("Two.");
    }

    @Test
    void theFirstVersionHasNoPreviousOneAndIsShownAsItIs() throws Exception {
        dialog.select(rowOf("One.").id());

        assertThat(dialog.plain.isVisible()).isTrue();
        assertThat(dialog.plain.getText()).isEqualTo("One.");
    }

    @Test
    void puttingAVersionBackAddsANewestOneAndHandsOnThePart() throws Exception {
        dialog.select(rowOf("One.").id());

        dialog.restore();

        assertThat(changed.get().getResponse()).isEqualTo("One.");
        assertThat(chatMessageService.find(part.getId()).getResponse()).isEqualTo("One.");
        assertThat(rows()).hasSize(4);
        assertThat(rows().getFirst().source()).isEqualTo(RevisionSource.RESTORE);
        assertThat(selected().id()).isEqualTo(rows().getFirst().id());
        assertThat(dialog.origin.getText()).isNotEmpty();
        assertThat(dialog.restore.isEnabled()).isFalse();
    }

    @Test
    void goingBackSendsTheNewerVersionsToTheTrash() throws Exception {
        dialog.select(rowOf("Two.").id());

        dialog.rollBack();

        assertThat(changed.get().getResponse()).isEqualTo("Two.");
        assertThat(rows()).hasSize(2);
        dialog.showDeleted.setValue(true);
        assertThat(rows()).hasSize(3);
        assertThat(rows().stream().filter(RevisionRow::deleted)).hasSize(1);
        assertThat(dialog.emptyTrash.isEnabled()).isTrue();
    }

    @Test
    void aVersionInTheTrashIsTakenOutOrDeletedForGood() throws Exception {
        dialog.select(rowOf("Two.").id());
        dialog.deleteOrUndelete();
        assertThat(rows()).hasSize(2);

        dialog.showDeleted.setValue(true);
        dialog.select(rowOf("Two.").id());
        assertThat(dialog.delete.getText()).isEqualTo(loc.getValue(L.LABEL_UNDELETE_VERSION));
        assertThat(dialog.purge.isVisible()).isTrue();
        dialog.deleteOrUndelete();
        assertThat(rows().stream().filter(RevisionRow::deleted)).isEmpty();
        assertThat(dialog.delete.getText()).isEqualTo(loc.getValue(L.LABEL_DELETE));

        dialog.deleteOrUndelete();
        dialog.purge();
        assertThat(rows()).hasSize(2);
        assertThat(messageRevisionService.list(part, true)).hasSize(2);
    }

    @Test
    void theNewestVersionCannotBeDeleted() {
        dialog.deleteOrUndelete();

        assertThat(rows()).hasSize(3);
    }

    @Test
    void aVersionCanBeLabeledAndTheTextSavedAsALabeledVersion() throws Exception {
        dialog.select(rowOf("Two.").id());
        dialog.setLabel("  the quiet one ");
        assertThat(rows().stream().filter(row -> "the quiet one".equals(row.label()))).hasSize(1);
        assertThat(selected().label()).isEqualTo("the quiet one");

        dialog.setLabel("");
        assertThat(rows().stream().filter(row -> row.label() != null)).isEmpty();

        dialog.saveVersion("before the duel");
        assertThat(rows()).hasSize(4);
        assertThat(rows().getFirst().source()).isEqualTo(RevisionSource.SNAPSHOT);
        assertThat(rows().getFirst().label()).isEqualTo("before the duel");
    }

    @Test
    void emptyingTheTrashDeletesWhatIsInIt() throws Exception {
        dialog.select(rowOf("One.").id());
        dialog.rollBack();
        dialog.showDeleted.setValue(true);
        assertThat(rows()).hasSize(3);

        dialog.emptyTrash();

        assertThat(rows()).hasSize(1);
        assertThat(dialog.emptyTrash.isEnabled()).isFalse();
    }

    @Test
    void theReasoningOfAGeneratedVersionIsShown() throws Exception {
        ChatMessage generated = chatMessageService.createPart(book, part, "Generated.", null,
                RevisionOrigin.model(RevisionSource.GENERATE, "model · protocol", false, "The reasoning."));
        part = generated;
        open();

        assertThat(dialog.reasoningDetails.isVisible()).isTrue();
        assertThat(dialog.reasoning.getText()).isEqualTo("The reasoning.");
        assertThat(dialog.meta.getText()).contains("model · protocol");
        dialog.select(rowOf("Generated.").id());
        assertThat(dialog.reasoningDetails.isVisible()).isTrue();
    }

    @Test
    void aPartWithoutVersionsSaysSo() throws Exception {
        ChatMessage blank = chatMessageService.createRoot(book, new ChatMessage());
        part = blank;
        open();

        assertThat(rows()).isEmpty();
        assertThat(dialog.empty.isVisible()).isTrue();
        assertThat(dialog.grid.isVisible()).isFalse();
        assertThat(dialog.clearHistory.isEnabled()).isFalse();
    }

    @Test
    void clearingAsksFirstAndTellsWhatGoes() throws Exception {
        List<String> calls = new ArrayList<>();
        ClearHistoryDialog clearing = new ClearHistoryDialog("Clear", new ClearHistoryDialog.Clearing() {
            @Override
            public RevisionClearing preview(boolean keepSnapshots) {
                calls.add("preview " + keepSnapshots);
                return new RevisionClearing(4, 2048);
            }

            @Override
            public RevisionClearing clear(boolean keepSnapshots) {
                calls.add("clear " + keepSnapshots);
                return new RevisionClearing(4, 2048);
            }
        }, () -> calls.add("done"));

        List<Component> components = new ArrayList<>(all(clearing).toList());
        clearing.getFooter().getElement().getChildren().forEach(e -> e.getComponent().ifPresent(components::add));
        assertThat(components.stream().filter(c -> c instanceof com.vaadin.flow.component.html.Span s && s.getText().contains("4"))).hasSize(1);
        Button clear = (Button) components.stream().filter(c -> c instanceof Button b && loc.getValue(L.LABEL_CLEAR_HISTORY).equals(b.getText())).findFirst().orElseThrow();
        assertThat(clear.isEnabled()).isTrue();
        clear.click();

        assertThat(calls).containsExactly("preview true", "clear true", "done");
    }

    @Test
    void clearingWithNothingToClearCannotBeConfirmed() {
        ClearHistoryDialog clearing = new ClearHistoryDialog("Clear", new ClearHistoryDialog.Clearing() {
            @Override
            public RevisionClearing preview(boolean keepSnapshots) {
                return RevisionClearing.NOTHING;
            }

            @Override
            public RevisionClearing clear(boolean keepSnapshots) {
                throw new AssertionError();
            }
        }, () -> {
        });

        List<Component> components = new ArrayList<>();
        clearing.getFooter().getElement().getChildren().forEach(e -> e.getComponent().ifPresent(components::add));
        assertThat(components.stream().filter(c -> c instanceof Button b && loc.getValue(L.LABEL_CLEAR_HISTORY).equals(b.getText()))
                .allMatch(c -> !((Button) c).isEnabled())).isTrue();
    }
}
