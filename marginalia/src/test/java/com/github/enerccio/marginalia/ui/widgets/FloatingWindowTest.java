package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.components.RunCorner;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dialog.Dialog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A window of a model run: Close and Stop are two things. Closing it while the model works folds it into the panel of the runs and
 * the model goes on; a click on its entry opens it again.
 */
class FloatingWindowTest extends MarginaliaTestBase {

    // UI.setCurrent keeps a weak reference only
    private UI ui;
    private final AtomicInteger stopped = new AtomicInteger();

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
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(FloatingWindowTest::all));
    }

    private FloatingWindow window(Dialog dialog) {
        return new FloatingWindow(dialog, "test-window", () -> "Writing a summary", stopped::incrementAndGet);
    }

    @Test
    void closingWhileTheModelWorksFoldsTheWindowAndTheModelGoesOn() {
        RunCorner corner = new RunCorner();
        ui.add(corner);
        Dialog dialog = new Dialog();
        FloatingWindow floating = window(dialog);
        dialog.open();
        assertThat(floating.holdForTest()).as("an idle window that holds nothing is not a run").isNull();

        floating.setRunning(true);
        Runs.Run run = floating.holdForTest();
        assertThat(run).as("the model works: a run in the panel of every page").isNotNull();

        dialog.close();
        assertThat(stopped.get()).as("Close is not Stop").isZero();
        assertThat(floating.isMinimised()).isTrue();
        assertThat(floating.isLive()).isTrue();
        assertThat(run.isEnded()).isFalse();

        // a click on the entry (its text and progress, not a button) opens the window again
        Component entry = all(corner.panelForTest()).filter(c -> c.hasClassName(CssStyles.CURSOR_POINTER)).findFirst().orElseThrow();
        ComponentUtil.fireEvent(entry, new ClickEvent<>(entry));
        assertThat(dialog.isOpened()).isTrue();
        assertThat(floating.isMinimised()).isFalse();

        // the model is done: the run ends, and closing now ends the window
        floating.setRunning(false);
        assertThat(run.isEnded()).isTrue();
        dialog.close();
        assertThat(floating.isLive()).isFalse();
        assertThat(floating.runForTest()).isNull();
        assertThat(stopped.get()).isEqualTo(1);
        ui.remove(corner);
    }

    @Test
    void closingAFoldedWindowForGoodStopsTheModel() {
        RunCorner corner = new RunCorner();
        ui.add(corner);
        Dialog dialog = new Dialog();
        FloatingWindow floating = window(dialog);
        AtomicInteger ended = new AtomicInteger();
        floating.addEndListener(ended::incrementAndGet);
        dialog.open();
        floating.setRunning(true);
        Runs.Run run = floating.holdForTest();
        dialog.close();
        assertThat(ended.get()).as("folding does not end the window").isZero();

        floating.closeForGood();
        assertThat(stopped.get()).isEqualTo(1);
        assertThat(ended.get()).isEqualTo(1);
        assertThat(run.isEnded()).isTrue();
        assertThat(floating.isLive()).isFalse();
        ui.remove(corner);
    }

    @Test
    void aFoldedWindowEndsWithItsPage() {
        RunCorner corner = new RunCorner();
        ui.add(corner);
        Dialog dialog = new Dialog();
        FloatingWindow floating = window(dialog);
        dialog.open();
        floating.setRunning(true);
        Runs.Run run = floating.holdForTest();
        dialog.close();
        assertThat(floating.isMinimised()).isTrue();

        // the tab is closed: no book window owns the window, the page itself ends it
        ComponentUtil.fireEvent(ui, new DetachEvent(ui));
        assertThat(stopped.get()).isEqualTo(1);
        assertThat(run.isEnded()).isTrue();
        assertThat(floating.isLive()).isFalse();
        ui.remove(corner);
    }

    @Test
    void aModalThatIsNotABookWindowDoesNotEndTheWindowWithItself() {
        RunCorner corner = new RunCorner();
        ui.add(corner);
        // the summaries overview: a strict modal the window is opened from, closed while the summaries are written
        Dialog overview = new Dialog();
        overview.setModality(ModalityMode.STRICT);
        overview.open();
        Dialog dialog = new Dialog();
        FloatingWindow floating = window(dialog);
        dialog.open();
        floating.setRunning(true);
        Runs.Run run = floating.holdForTest();

        overview.close();
        ui.remove(overview);

        assertThat(stopped.get()).isZero();
        assertThat(run.isEnded()).isFalse();
        assertThat(floating.isLive()).isTrue();
        floating.closeForGood();
        ui.remove(corner);
    }

    @Test
    void aWindowTakenOutOfThePageWithTheModalItWasOpenedOverIsFolded() {
        RunCorner corner = new RunCorner();
        ui.add(corner);
        Dialog overview = new Dialog();
        overview.setModality(ModalityMode.STRICT);
        ui.add(overview);
        overview.open();
        // Vaadin attaches a dialog opened over a modal to that modal
        Dialog dialog = new Dialog();
        FloatingWindow floating = window(dialog);
        overview.add(dialog);
        dialog.open();
        floating.setRunning(true);
        Runs.Run run = floating.holdForTest();

        overview.close();
        ui.remove(overview);

        assertThat(dialog.isOpened()).isFalse();
        assertThat(floating.isMinimised()).as("folded: its entry in the panel opens it again").isTrue();
        assertThat(stopped.get()).isZero();
        assertThat(run.isEnded()).isFalse();
        floating.closeForGood();
        ui.remove(corner);
    }

    @Test
    void withoutACornerClosingStopsTheModel() {
        Dialog dialog = new Dialog();
        FloatingWindow floating = window(dialog);
        dialog.open();
        floating.setRunning(true);
        dialog.close();
        assertThat(stopped.get()).as("nowhere to fold to").isEqualTo(1);
        assertThat(floating.isLive()).isFalse();
    }
}
