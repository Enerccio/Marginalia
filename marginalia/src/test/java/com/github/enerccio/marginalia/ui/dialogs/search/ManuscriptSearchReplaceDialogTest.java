package com.github.enerccio.marginalia.ui.dialogs.search;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.SummaryService.BlockTexts;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.search.SearchReplaceDialog.State;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The search and replace dialog on the desktop and on the phone: the states it goes through, what is locked in each
 * of them and the replace in the book. The calls of the worker thread to the UI run on that thread (there is no
 * session to lock), which is enough to see the dialog.
 * <pre>
 * m1 ─ m2 ─ m3[S3]            (active)
 *       └─ m3b
 * </pre>
 */
class ManuscriptSearchReplaceDialogTest extends MarginaliaTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SummaryService summaryService;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;
    private Manuscript manuscript;
    private ChatMessage m1, m2, m3, m3b;
    private final AtomicInteger replaced = new AtomicInteger();
    // the question the dialog asked before the replace and what the user answers
    private final List<String> questions = new ArrayList<>();
    private boolean answer;

    @BeforeEach
    void createBook() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        OpenAICompatible ai = createAI();
        Manuscript m = new Manuscript();
        m.setName(uniqueName("book"));
        m.setAi(ai);
        manuscript = manuscriptService.save(m);

        m1 = child(null, "Anna met John");
        m2 = child(m1, "John went home");
        m3 = child(m2, "Nothing here");
        m3b = child(m2, "John again");

        summarize(m3, "John and Anna");

        manuscript = manuscriptService.find(manuscript.getId());
        manuscript.setActiveLeaf(m3);
        manuscript = manuscriptService.save(manuscript);
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private ChatMessage child(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        Thread.sleep(3);  // messages are ordered by creation time
        return parent == null ? chatMessageService.createRoot(manuscript, message) : chatMessageService.addChild(parent, message);
    }

    /**
     * Puts a valid summary on the part.
     */
    private void summarize(ChatMessage message, String text) throws Exception {
        Summary summary = new Summary();
        summary.setSummary(text);
        summary = summaryService.save(summary);
        message = chatMessageService.find(message.getId());
        message.setSummary(summary);
        chatMessageService.save(message);
        summary = summaryService.find(summary.getId());
        summary.setSummaryMessageHash(summaryService.collectFirstBlock(
                chatMessageService.getBranchFromLeaf(chatMessageService.find(message.getId())).reversed().iterator(),
                BlockTexts.CURRENT).hash());
        summaryService.save(summary);
    }

    private ManuscriptSearchReplaceDialog dialog(boolean phone) {
        ManuscriptSearchReplaceDialog dialog = new ManuscriptSearchReplaceDialog(manuscript, phone) {
            // no session to lock: the worker thread changes the components itself
            @Override
            protected void confirm(String warning, Runnable yes) {
                questions.add(warning);
                if (answer) {
                    yes.run();
                }
            }

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
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(ManuscriptSearchReplaceDialogTest::all));
    }

    private static Button button(Component root, String text) {
        return all(root).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> text.equals(b.getText())).findFirst().orElseThrow();
    }

    private static Checkbox checkbox(Component root, String label) {
        return all(root).filter(Checkbox.class::isInstance).map(Checkbox.class::cast)
                .filter(b -> label.equals(b.getLabel())).findFirst().orElseThrow();
    }

    private static TextField field(Component root, String label) {
        return all(root).filter(TextField.class::isInstance).map(TextField.class::cast)
                .filter(b -> label.equals(b.getLabel())).findFirst().orElseThrow();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static int roots(Component root) {
        TreeGrid grid = all(root).filter(TreeGrid.class::isInstance).map(TreeGrid.class::cast).findFirst().orElseThrow();
        return ((HierarchicalDataProvider) grid.getDataProvider()).getChildCount(new HierarchicalQuery(null, null));
    }

    private static void await(BooleanSupplier condition) throws InterruptedException {
        for (int i = 0; i < 500 && !condition.getAsBoolean(); i++) {
            Thread.sleep(20);
        }
        assertThat(condition.getAsBoolean()).isTrue();
    }

    private String response(ChatMessage message) throws Exception {
        return chatMessageService.find(message.getId()).getResponse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void startsWaitingForTheSearch(boolean phone) {
        ManuscriptSearchReplaceDialog dialog = dialog(phone);

        assertThat(dialog.getState()).isEqualTo(State.INPUT);
        assertThat(button(dialog, "Search").isEnabled()).isTrue();
        assertThat(button(dialog, "Replace").isEnabled()).isFalse();
        assertThat(checkbox(dialog, "Search by regex").getValue()).isFalse();
        assertThat(checkbox(dialog, "Case sensitive").getValue()).isTrue();
        assertThat(checkbox(dialog, "Case sensitive").isEnabled()).isTrue();
        assertThat(checkbox(dialog, "Search in summaries").getValue()).isTrue();
        assertThat(checkbox(dialog, "Search in all branches").getValue()).isFalse();
        assertThat(checkbox(dialog, "Revalidate all summaries").getValue()).isTrue();
        // the phone has the Stop button without a text
        Button stop = all(dialog).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> phone ? b.getText().isEmpty() : "Stop".equals(b.getText())).findFirst().orElseThrow();
        assertThat(stop.isEnabled()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void caseSensitiveIsLockedByRegex(boolean phone) throws Exception {
        ManuscriptSearchReplaceDialog dialog = dialog(phone);

        checkbox(dialog, "Search by regex").setValue(true);
        assertThat(checkbox(dialog, "Case sensitive").isEnabled()).isFalse();

        // the search locks and unlocks the inputs, the box stays locked while the regex is on
        field(dialog, "Search for").setValue("John");
        button(dialog, "Search").click();
        await(() -> dialog.getState() == State.PREPARED);
        button(dialog, "Search again").click();
        assertThat(checkbox(dialog, "Search by regex").isEnabled()).isTrue();
        assertThat(checkbox(dialog, "Case sensitive").isEnabled()).isFalse();

        checkbox(dialog, "Search by regex").setValue(false);
        assertThat(checkbox(dialog, "Case sensitive").isEnabled()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void caseInsensitiveSearch(boolean phone) throws Exception {
        ManuscriptSearchReplaceDialog dialog = dialog(phone);
        checkbox(dialog, "Case sensitive").setValue(false);
        checkbox(dialog, "Search in all branches").setValue(true);
        field(dialog, "Search for").setValue("john");
        field(dialog, "Replace with").setValue("Jon");

        button(dialog, "Search").click();
        await(() -> dialog.getState() == State.PREPARED);

        assertThat(roots(dialog)).isEqualTo(4);
        assertThat(checkbox(dialog, "Case sensitive").isEnabled()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void emptySearchIsRefused(boolean phone) {
        ManuscriptSearchReplaceDialog dialog = dialog(phone);

        button(dialog, "Search").click();

        assertThat(field(dialog, "Search for").isInvalid()).isTrue();
        assertThat(dialog.getState()).isEqualTo(State.INPUT);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void foundIsShownAndTheInputsAreLocked(boolean phone) throws Exception {
        ManuscriptSearchReplaceDialog dialog = dialog(phone);
        field(dialog, "Search for").setValue("John");
        field(dialog, "Replace with").setValue("Jon");

        button(dialog, "Search").click();
        await(() -> dialog.getState() == State.PREPARED);

        // two parts and the summary of the active branch
        assertThat(roots(dialog)).isEqualTo(3);
        assertThat(button(dialog, "Search again").isEnabled()).isTrue();
        assertThat(button(dialog, "Replace").isEnabled()).isTrue();
        assertThat(field(dialog, "Search for").isEnabled()).isFalse();
        assertThat(field(dialog, "Replace with").isEnabled()).isFalse();
        assertThat(checkbox(dialog, "Search by regex").isEnabled()).isFalse();
        assertThat(checkbox(dialog, "Search in summaries").isEnabled()).isFalse();
        assertThat(checkbox(dialog, "Search in all branches").isEnabled()).isFalse();
        assertThat(checkbox(dialog, "Revalidate all summaries").isEnabled()).isFalse();
        // searching changes nothing
        assertThat(response(m1)).isEqualTo("Anna met John");

        button(dialog, "Search again").click();

        assertThat(dialog.getState()).isEqualTo(State.INPUT);
        assertThat(roots(dialog)).isZero();
        assertThat(field(dialog, "Search for").isEnabled()).isTrue();
        assertThat(field(dialog, "Search for").getValue()).isEqualTo("John");
        assertThat(button(dialog, "Replace").isEnabled()).isFalse();
        assertThat(button(dialog, "Search").isEnabled()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void nothingFoundGoesBackToTheFirstState(boolean phone) throws Exception {
        ManuscriptSearchReplaceDialog dialog = dialog(phone);
        field(dialog, "Search for").setValue("Zebra");

        button(dialog, "Search").click();
        // the search runs on its own thread, wait until it is over: the search button is enabled again
        await(() -> dialog.getState() == State.INPUT && button(dialog, "Search").isEnabled()
                && field(dialog, "Search for").isEnabled());
        Thread.sleep(200);

        assertThat(dialog.getState()).isEqualTo(State.INPUT);
        assertThat(roots(dialog)).isZero();
        assertThat(button(dialog, "Replace").isEnabled()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void replaceChangesTheBookAndResetsTheDialog(boolean phone) throws Exception {
        ManuscriptSearchReplaceDialog dialog = dialog(phone);
        // the other branch is searched too, so no summary is left outdated and nothing needs to be confirmed
        checkbox(dialog, "Search in all branches").setValue(true);
        field(dialog, "Search for").setValue("John");
        field(dialog, "Replace with").setValue("Jon");
        button(dialog, "Search").click();
        await(() -> dialog.getState() == State.PREPARED);
        assertThat(roots(dialog)).isEqualTo(4);

        button(dialog, "Replace").click();
        await(() -> replaced.get() == 1);

        assertThat(dialog.getState()).isEqualTo(State.INPUT);
        assertThat(response(m1)).isEqualTo("Anna met Jon");
        assertThat(response(m2)).isEqualTo("Jon went home");
        assertThat(response(m3b)).isEqualTo("Jon again");
        assertThat(roots(dialog)).isZero();
        assertThat(button(dialog, "Replace").isEnabled()).isFalse();
        assertThat(field(dialog, "Search for").isEnabled()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void revalidatedSummariesNeedNoConfirmation(boolean phone) throws Exception {
        summarize(m3b, "again");
        ManuscriptSearchReplaceDialog dialog = dialog(phone);
        field(dialog, "Search for").setValue("Anna");
        field(dialog, "Replace with").setValue("Ann");
        button(dialog, "Search").click();
        await(() -> dialog.getState() == State.PREPARED);

        button(dialog, "Replace").click();
        await(() -> replaced.get() == 1);

        assertThat(questions).isEmpty();
        assertThat(response(m1)).isEqualTo("Ann met John");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void summariesThatGoOutdatedNeedConfirmation(boolean phone) throws Exception {
        // the summary of the other branch is based on the text that changes, and the search covers the active branch only
        summarize(m3b, "again");
        ManuscriptSearchReplaceDialog dialog = dialog(phone);
        checkbox(dialog, "Revalidate all summaries").setValue(false);
        field(dialog, "Search for").setValue("Anna");
        field(dialog, "Replace with").setValue("Ann");
        button(dialog, "Search").click();
        await(() -> dialog.getState() == State.PREPARED);

        button(dialog, "Replace").click();

        // the question is asked, nothing happened yet
        assertThat(questions).hasSize(1);
        assertThat(questions.getFirst()).contains("1 summaries");
        assertThat(dialog.getState()).isEqualTo(State.PREPARED);
        assertThat(response(m1)).isEqualTo("Anna met John");

        answer = true;
        button(dialog, "Replace").click();
        await(() -> replaced.get() == 1);

        assertThat(questions).hasSize(2);
        assertThat(dialog.getState()).isEqualTo(State.INPUT);
        assertThat(response(m1)).isEqualTo("Ann met John");
    }

}
