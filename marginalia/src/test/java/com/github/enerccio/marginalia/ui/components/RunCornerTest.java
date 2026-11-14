package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The panel of the runs: the runs of the user from the registry, and the windows of the page folded into it.
 */
class RunCornerTest extends MarginaliaTestBase {

    @Autowired
    private ManuscriptService manuscriptService;

    // UI.setCurrent keeps a weak reference only
    private UI ui;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    private static Stream<Component> all(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(RunCornerTest::all));
    }

    private static List<Button> buttons(RunCorner corner) {
        return all(corner.panelForTest()).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(button -> !button.getText().isEmpty()).toList();
    }

    private static String texts(RunCorner corner) {
        return corner.panelForTest().getElement().getTextRecursively();
    }

    @Test
    void aFoldedWindowOpensThePanelWithOpenAndStopAndCloseOnceTheModelIsDone() {
        RunCorner corner = new RunCorner();
        ui.add(corner);
        PageRuns runs = PageRuns.of(ui);
        assertThat(runs.hasCorner()).isTrue();
        AtomicInteger opened = new AtomicInteger();
        AtomicInteger stopped = new AtomicInteger();
        AtomicInteger closed = new AtomicInteger();
        PageRuns.Run run = runs.add(() -> "Annotating · Book", opened::incrementAndGet, stopped::incrementAndGet, closed::incrementAndGet);
        run.setRunning(true);

        assertThat(corner.panelForTest().isVisible()).as("an open window without a run is not in the panel").isFalse();
        run.setMinimised(true);
        assertThat(corner.panelForTest().isVisible()).isTrue();
        assertThat(corner.isExpandedForTest()).as("folding a window shows it").isTrue();

        List<Button> buttons = buttons(corner);
        assertThat(buttons).extracting(Button::getText).containsExactly("Open", "Stop");
        buttons.get(0).click();
        buttons.get(1).click();
        assertThat(opened.get()).isEqualTo(1);
        assertThat(stopped.get()).isEqualTo(1);

        // the model is done: the window can be closed from the panel
        run.setRunning(false);
        assertThat(buttons(corner)).extracting(Button::getText).containsExactly("Open", "Close");
        buttons(corner).get(1).click();
        assertThat(closed.get()).isEqualTo(1);

        run.remove();
        assertThat(corner.panelForTest().isVisible()).isFalse();
        ui.remove(corner);
        assertThat(runs.hasCorner()).isFalse();
    }

    @Test
    void aRunOfTheUserFromAnotherPageIsListedWithItsBookTheModelsTextAndStop() throws Exception {
        Manuscript m = new Manuscript();
        m.setName(uniqueName("book"));
        Manuscript book = manuscriptService.save(m);
        RunCorner corner = new RunCorner();
        ui.add(corner);
        AtomicInteger stopped = new AtomicInteger();

        UI other = new UI();
        Runs.Run run = Runs.run("reallang-translator", () -> "Translating the speech").book(book).page(other)
                .onStop(stopped::incrementAndGet).start();
        try {
            // a test page has no session: the listeners of the registry do not reach it, the test does what they do
            corner.refreshForTest();
            assertThat(corner.panelForTest().isVisible()).isTrue();
            assertThat(corner.isExpandedForTest()).as("a chip until it is opened").isFalse();
            assertThat(texts(corner)).contains("Translating the speech");

            corner.setExpandedForTest(true);
            run.waiting(2);
            corner.refreshForTest();
            assertThat(texts(corner)).contains(book.getName()).contains("2 requests before this one");

            run.requestStarted();
            run.chunk(false, "Er sagte: ");
            run.chunk(false, "Guten Tag.");
            // the text is shown a moment later, not for every piece (here: when the list is drawn again)
            corner.setExpandedForTest(true);
            assertThat(texts(corner)).contains("Er sagte: Guten Tag.").contains("The model is writing");

            buttons(corner).stream().filter(b -> b.getText().equals("Stop")).findFirst().orElseThrow().click();
            assertThat(stopped.get()).isEqualTo(1);
            corner.refreshForTest();
            assertThat(texts(corner)).contains("Stopping");
        } finally {
            run.end();
        }
        corner.refreshForTest();
        assertThat(corner.panelForTest().isVisible()).as("nothing runs").isFalse();
        assertThat(run.getOutcome()).isEqualTo(Runs.Outcome.STOPPED);
    }

    @Test
    void theRunsOfAnotherUserAndTheWindowOfABookAreNotListed() throws Exception {
        RunCorner corner = new RunCorner();
        ui.add(corner);
        Manuscript m = new Manuscript();
        m.setName(uniqueName("book"));
        Manuscript book = manuscriptService.save(m);
        Runs.Run window = Runs.run(Runs.BOOK_WINDOW, () -> "Open").book(book).page(ui).quiet().start();
        Runs.Run someoneElses = Runs.run("test", () -> "Elsewhere").owner(-1L).start();
        try {
            corner.refreshForTest();
            assertThat(corner.panelForTest().isVisible()).isFalse();
        } finally {
            window.end();
            someoneElses.end();
        }
    }

    @Test
    void theEndOfARunWhosePageIsGoneIsNotifiedByAnotherPageOfTheUserOnceAndAStoppedOneNot() throws Exception {
        RunCorner corner = new RunCorner();
        ui.add(corner);
        Manuscript m = new Manuscript();
        m.setName(uniqueName("book"));
        Manuscript book = manuscriptService.save(m);
        Runs.Run finished = Runs.run("summaries", () -> "Rewriting the summaries").book(book).page(new UI())
                .notifies("resummarize", false, book.getName()).start();
        finished.end(Runs.Outcome.FINISHED);
        corner.endedForTest(finished);
        assertThat(finished.claimNotice()).as("this page sent it").isFalse();

        Runs.Run stopped = Runs.run("summaries", () -> "Rewriting the summaries").book(book).page(new UI())
                .notifies("resummarize", false, book.getName()).onStop(() -> {
                }).start();
        stopped.stop();
        stopped.end();
        corner.endedForTest(stopped);
        assertThat(stopped.claimNotice()).as("the author stopped it, nothing to say").isTrue();
    }
}
