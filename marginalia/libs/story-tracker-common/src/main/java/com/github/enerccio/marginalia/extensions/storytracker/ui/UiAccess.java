package com.github.enerccio.marginalia.extensions.storytracker.ui;

import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Changes a component from a thread that is not the one of its session (the worker of a run): through
 * {@code ui.access} and a push, skipped when the page is gone.
 */
public final class UiAccess {

    private static final Logger log = LoggerFactory.getLogger(UiAccess.class);

    private UiAccess() {
    }

    public static void run(Component component, Runnable change) {
        component.getUI().ifPresent(ui -> run(ui, change));
    }

    /**
     * A trigger for {@code change} that never queues more than one run: while one is waiting for the session (or running), the
     * calls are dropped, the waiting run reads the latest state itself. A worker that reports faster than the page can show would
     * otherwise fill the queue of the session and the page would never answer again.
     */
    public static Runnable coalesced(Component component, Runnable change) {
        AtomicBoolean queued = new AtomicBoolean();
        // taken where the trigger is made (the page's request): the queued change may be run by any thread that unlocks the session
        RequestAttributes scope = ThreadCopyRequestAttributes.create();
        return () -> {
            if (!queued.compareAndSet(false, true)) {
                return;
            }
            UI ui = component.getUI().orElse(null);
            if (ui == null) {
                queued.set(false);
                return;
            }
            try {
                ui.access(() -> {
                    queued.set(false);
                    inScope(scope, change);
                    UIPushGuard.push(ui);
                });
            } catch (UIDetachedException e) {
                queued.set(false);
                log.debug("The page is gone, the change is skipped");
            }
        };
    }

    public static void run(UI ui, Runnable change) {
        RequestAttributes scope = ThreadCopyRequestAttributes.create();
        try {
            ui.access(() -> {
                inScope(scope, change);
                UIPushGuard.push(ui);
            });
        } catch (UIDetachedException e) {
            log.debug("The page is gone, the change is skipped");
        }
    }

    /** Runs the change with the request scope of the page (the session-scoped user), whichever thread runs the queued task. */
    private static void inScope(RequestAttributes scope, Runnable change) {
        RequestAttributes bound = scope != null ? scope : RequestContextHolder.getRequestAttributes();
        try (ThreadCopyRequestAttributes.InRequestScope _ = new ThreadCopyRequestAttributes.InRequestScope(bound)) {
            change.run();
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
        }
    }
}
