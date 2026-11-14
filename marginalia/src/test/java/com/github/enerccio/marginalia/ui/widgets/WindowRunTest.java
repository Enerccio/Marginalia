package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The request of a window the author waits in, as a run of the registry: there while the model answers, bound while the request is
 * made, stopped by the panel through the window's own Stop, and gone with the window.
 */
class WindowRunTest {

    // UI.setCurrent keeps only a weak reference
    private UI ui;

    @BeforeEach
    void setUp() {
        Runs.reset();
        ui = new UI();
        UI.setCurrent(ui);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
        Runs.reset();
    }

    private Dialog openDialog() {
        Dialog dialog = new Dialog();
        ui.add(dialog);
        dialog.open();
        return dialog;
    }

    @Test
    void theRequestIsARunWhileTheModelAnswers() {
        Dialog dialog = openDialog();
        WindowRun request = WindowRun.of(dialog, "test-window", () -> "Asking");

        assertThat(request.isRunning()).isFalse();
        Runs.Run run;
        try (Runs.Binding _ = request.start(() -> { })) {
            run = request.getRun();
            // the requests made here report to the run
            assertThat(Runs.current()).isSameAs(run);
        }
        assertThat(Runs.current()).isNull();
        assertThat(Runs.all()).containsExactly(run);
        assertThat(run.getLabel()).isEqualTo("Asking");
        assertThat(run.getHolds()).isEmpty();
        assertThat(run.marksParts()).isFalse();

        request.finished();
        assertThat(run.isEnded()).isTrue();
        assertThat(Runs.all()).isEmpty();
        assertThat(request.isRunning()).isFalse();
    }

    @Test
    void theStopOfThePanelIsTheStopOfTheWindow() {
        WindowRun request = WindowRun.of(openDialog(), "test-window", () -> "Asking");
        AtomicInteger stopped = new AtomicInteger();
        try (Runs.Binding _ = request.start(stopped::incrementAndGet)) {
            assertThat(request.getRun().canStop()).isTrue();
        }

        request.getRun().stop();

        assertThat(stopped.get()).isEqualTo(1);
    }

    @Test
    void aRequestThatCanNotBeStoppedHasNoStop() {
        WindowRun request = WindowRun.of(openDialog(), "test-window", () -> "Translating");
        try (Runs.Binding _ = request.start(null)) {
            assertThat(request.getRun().canStop()).isFalse();
        }
    }

    @Test
    void closingTheWindowEndsTheRun() {
        Dialog dialog = openDialog();
        WindowRun request = WindowRun.of(dialog, "test-window", () -> "Asking");
        try (Runs.Binding _ = request.start(() -> { })) {
            assertThat(Runs.all()).hasSize(1);
        }

        dialog.close();

        assertThat(Runs.all()).isEmpty();
    }

    @Test
    void aRequestThatFailsToStartEndsItsRun() throws Exception {
        WindowRun request = WindowRun.of(openDialog(), "test-window", () -> "Asking");

        String sent = request.start(() -> { }, () -> {
            assertThat(Runs.current()).isSameAs(request.getRun());
            return "token";
        });
        assertThat(sent).isEqualTo("token");
        assertThat(request.isRunning()).isTrue();

        try {
            request.start(() -> { }, () -> {
                throw new IllegalStateException("no model");
            });
        } catch (IllegalStateException expected) {
            // the window shows it
        }
        assertThat(request.isRunning()).isFalse();
        assertThat(Runs.all()).isEmpty();
    }

    @Test
    void aNewRequestEndsTheOneBefore() {
        WindowRun request = WindowRun.of(openDialog(), "test-window", () -> "Asking");
        Runs.Run first;
        try (Runs.Binding _ = request.start(() -> { })) {
            first = request.getRun();
        }
        try (Runs.Binding _ = request.start(() -> { })) {
            assertThat(first.isEnded()).isTrue();
            assertThat(Runs.all()).containsExactly(request.getRun());
        }
    }
}
