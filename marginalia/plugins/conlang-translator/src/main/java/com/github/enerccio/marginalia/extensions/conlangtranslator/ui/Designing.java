package com.github.enerccio.marginalia.extensions.conlangtranslator.ui;

import com.github.enerccio.marginalia.extensions.conlangtranslator.service.AsyncRuns;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.PluginLog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangTranslatorLocalization.*;

/**
 * Runs a request of the designer while a small window says that it works (a request to a model takes a minute or more). <i>Stop waiting</i>
 * closes the window and drops the answer when it comes; the page is never blocked.
 */
final class Designing {

    private Designing() {
    }

    /**
     * @param text what the window says
     * @param job  the request, run on a worker thread of the session
     * @param done called on the page with the result; a failure is shown to the user and {@code done} is not called
     */
    static <T> void run(String text, Callable<T> job, Consumer<T> done) {
        AtomicBoolean stopped = new AtomicBoolean();
        Dialog busy = new Dialog();
        busy.setCloseOnOutsideClick(false);
        busy.setCloseOnEsc(false);
        busy.setWidth("26rem");
        busy.setMaxWidth("96vw");
        ProgressBar bar = new ProgressBar();
        bar.setIndeterminate(true);
        Span label = new Span(text);
        Button stop = new Button(T.localized(STOP_WAITING), _ -> {
            stopped.set(true);
            busy.close();
        });
        VerticalLayout content = new VerticalLayout(label, bar, stop);
        content.setPadding(false);
        busy.add(content);
        busy.open();
        AsyncRuns.start("conlang-designer", PluginLog.get(), job, (result, failure) -> {
            busy.close();
            if (stopped.get()) {
                return;
            }
            if (failure != null) {
                Notification.error(String.format(T.localized(THE_DESIGNER_FAILED), failure.getMessage()));
                return;
            }
            done.accept(result);
        });
    }
}
