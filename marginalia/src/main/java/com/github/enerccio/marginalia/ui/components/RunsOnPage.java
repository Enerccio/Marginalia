package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import org.springframework.web.context.request.RequestAttributes;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Tells a component of a page when the {@link Runs} change: a run changes on any thread (a translation ends on its worker), so the
 * refresh runs in the page, through {@code ui.access} and a push, or at once when the page's session is held by this thread.
 * <p>
 * The refresh runs with the request scope of the page as it was when the listener was added (call it on the page's thread, on attach):
 * the thread that changed the run may work for another user, whose session beans (the user, the language) the refresh must not see.
 */
public final class RunsOnPage {

    /** How often at most the text the model writes is shown again. */
    static final long TEXT_MILLIS = 500;

    private static final ScheduledExecutorService LATER = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "runs-on-page");
        thread.setDaemon(true);
        return thread;
    });

    private RunsOnPage() {
    }

    /**
     * @param refresh run in the page after every change of a run
     * @return removes the listener (on detach)
     */
    public static Registration listen(UI ui, Runnable refresh) {
        RequestAttributes scope = ThreadCopyRequestAttributes.create();
        return Runs.addListener(() -> inPage(ui, scope, refresh));
    }

    /**
     * Tells the page when the end of a run (once per run, in the page).
     *
     * @return removes the listener (on detach)
     */
    public static Registration listenEnd(UI ui, Consumer<Runs.Run> ended) {
        RequestAttributes scope = ThreadCopyRequestAttributes.create();
        return Runs.addEndListener(run -> inPage(ui, scope, () -> ended.accept(run)));
    }

    /**
     * Tells the page that the model wrote for a run, at most every {@value #TEXT_MILLIS} ms and once more after the last piece, so a
     * fast model does not flood the page.
     *
     * @param refresh run in the page; it reads the text of the runs it shows
     * @return removes the listener (on detach)
     */
    public static Registration listenText(UI ui, Runnable refresh) {
        RequestAttributes scope = ThreadCopyRequestAttributes.create();
        AtomicBoolean scheduled = new AtomicBoolean();
        AtomicBoolean removed = new AtomicBoolean();
        Registration registration = Runs.addTextListener(_ -> {
            if (scheduled.compareAndSet(false, true)) {
                LATER.schedule(() -> {
                    scheduled.set(false);
                    if (!removed.get()) {
                        inPage(ui, scope, refresh);
                    }
                }, TEXT_MILLIS, TimeUnit.MILLISECONDS);
            }
        });
        return () -> {
            removed.set(true);
            registration.remove();
        };
    }

    private static void inPage(UI ui, RequestAttributes scope, Runnable refresh) {
        VaadinSession session = ui.getSession();
        if (session == null) {
            return;
        }
        Runnable scoped = () -> {
            if (scope == null) {
                refresh.run();
                return;
            }
            try (ThreadCopyRequestAttributes.InRequestScope _ = new ThreadCopyRequestAttributes.InRequestScope(scope)) {
                refresh.run();
            }
        };
        if (session.hasLock()) {
            scoped.run();
            return;
        }
        try {
            ui.access(() -> {
                scoped.run();
                UIPushGuard.push(ui);
            });
        } catch (UIDetachedException e) {
            // the page is gone
        }
    }
}
