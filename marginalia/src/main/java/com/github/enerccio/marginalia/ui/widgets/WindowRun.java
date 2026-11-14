package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.dialog.Dialog;

import java.util.function.Supplier;

/**
 * The request of a window the author waits in (impersonate, a quick translation, a chat, the help assistant) as a run of the
 * {@link Runs}: while the model answers it is in the panel of the runs of every page of the user (its place in the queue of the
 * model, what the model writes, a Stop), and in {@code /jobs}. It holds nothing: the window only reads, and its answer stays in the
 * window as before. Closing the window, or the page, ends the run.
 * <pre>{@code
 * private final WindowRun request = WindowRun.of(this, "my-plugin", () -> T.localized(ASKING)).book(book);
 *
 * try (Runs.Binding _ = request.start(token::cancel)) {    // what the window's Stop does, run in the window's page
 *     service.ask(book, question, callbacks);              // the requests made here, and from their callbacks, report to the run
 * }
 * // in the end callbacks (any thread): request.finished();
 * }</pre>
 */
public final class WindowRun {

    private final String source;
    private final Supplier<String> label;
    private Manuscript book;
    private volatile Runs.Run run;

    private WindowRun(Component window, String source, Supplier<String> label) {
        this.source = source;
        this.label = label;
        if (window instanceof Dialog dialog) {
            dialog.addOpenedChangeListener(event -> {
                if (!event.isOpened()) {
                    finished();
                }
            });
        }
        window.addDetachListener(_ -> finished());
    }

    /**
     * @param window the window (a dialog, a panel) whose closing or detach ends the run
     * @param source what the run is (the id of the panel and of {@code /jobs}, the id of its notifications when it has them)
     * @param label  what it does, read each time the panel shows it
     */
    public static WindowRun of(Component window, String source, Supplier<String> label) {
        return new WindowRun(window, source, label);
    }

    /**
     * The book the window works on, shown in the panel; none for a run that is about no book (the help assistant).
     */
    public WindowRun book(Manuscript book) {
        this.book = book;
        return this;
    }

    /**
     * A request starts: on the thread of the page, before it is sent. A run that still goes on ends first.
     *
     * @param stop what the window's own Stop does, the panel of another page calls it in the window's page; null when the request
     *             can not be stopped (the panel shows no Stop)
     * @return the binding to close once the request was sent (try-with-resources); a worker thread the window starts binds the run
     * itself ({@link #getRun()}, {@link Runs.Run#bind()})
     */
    public Runs.Binding start(Runnable stop) {
        finished();
        UI ui = UI.getCurrent();
        Runs.Builder builder = Runs.run(source, label).book(book).page(ui).quiet();
        if (stop != null) {
            builder.onStop(() -> inPage(ui, stop));
        }
        Runs.Run started = builder.start();
        run = started;
        return started.bind();
    }

    /**
     * Something that sends a request and returns what stops it (a {@code CancellationToken}).
     */
    @FunctionalInterface
    public interface Request<T> {
        T send() throws Exception;
    }

    /**
     * As {@link #start(Runnable)}, with the request sent while the run is bound; a request that fails to start ends the run.
     *
     * @return what the request returned
     */
    public <T> T start(Runnable stop, Request<T> request) throws Exception {
        boolean sent = false;
        try (Runs.Binding _ = start(stop)) {
            T result = request.send();
            sent = true;
            return result;
        } finally {
            if (!sent) {
                finished();
            }
        }
    }

    /**
     * The request is over (answered, stopped or failed); on any thread, again is harmless.
     */
    public void finished() {
        Runs.Run ended = run;
        run = null;
        if (ended != null) {
            ended.end();
        }
    }

    /** Whether a request goes on. */
    public boolean isRunning() {
        Runs.Run current = run;
        return current != null && !current.isEnded();
    }

    /** The run of the request that goes on, null when none. */
    public Runs.Run getRun() {
        Runs.Run current = run;
        return current == null || current.isEnded() ? null : current;
    }

    private static void inPage(UI ui, Runnable work) {
        if (ui == null || UI.getCurrent() == ui || ui.getSession() == null || ui.getSession().hasLock()) {
            work.run();
            return;
        }
        try {
            ui.access(() -> {
                work.run();
                UIPushGuard.push(ui);
            });
        } catch (UIDetachedException e) {
            // the page is gone, and its window with it
        }
    }
}
