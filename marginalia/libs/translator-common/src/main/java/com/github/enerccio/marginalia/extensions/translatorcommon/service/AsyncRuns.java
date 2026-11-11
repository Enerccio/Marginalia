package com.github.enerccio.marginalia.extensions.translatorcommon.service;

import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import org.slf4j.Logger;
import org.springframework.web.context.request.RequestAttributes;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

/**
 * Runs something that takes minutes (a request to a model) on a worker thread of the user's session and hands the result to the page. The page
 * goes on while it works; a failure is told to the page that asked, never thrown. The run of the registry bound to the calling thread
 * ({@code WindowRun.start}) is bound to the worker too: its requests show in the panel of the runs.
 */
public final class AsyncRuns {

    private AsyncRuns() {
    }

    /**
     * Call it on the thread of the user's request.
     *
     * @param work the job, run on a worker thread with the session of the user
     * @param done called on the thread of the page with the result, or with the failure (the result is null then); may be null
     */
    public static <T> void start(String name, Logger log, Callable<T> work, BiConsumer<T, Throwable> done) {
        RequestAttributes attributes = ThreadCopyRequestAttributes.create();
        UI ui = UI.getCurrent();
        Runs.Run run = Runs.current();
        Thread.ofVirtual().name(name).start(() -> {
            T result = null;
            Throwable failure = null;
            try (InRequestScope _ = new InRequestScope(attributes); Runs.Binding _ = run == null ? null : run.bind()) {
                result = work.call();
            } catch (Throwable e) {
                failure = e;
                log.warn("The job {} failed: {}", name, e.getMessage(), e);
            }
            if (done != null && ui != null) {
                T finalResult = result;
                Throwable finalFailure = failure;
                try {
                    ui.access(() -> {
                        try (InRequestScope _ = new InRequestScope(attributes)) {
                            done.accept(finalResult, finalFailure);
                        } catch (RuntimeException e) {
                            log.warn("The page could not show the result of {}: {}", name, e.getMessage(), e);
                        }
                        UIPushGuard.push(ui);
                    });
                } catch (UIDetachedException e) {
                    log.debug("The page is gone, the result of {} is not shown", name);
                }
            }
        });
    }
}
