package com.github.enerccio.marginalia.extensions.translatorcommon.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import org.slf4j.Logger;
import org.springframework.web.context.request.RequestAttributes;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Runs the work on a part on a worker thread: a request to a model takes minutes, so the page goes on while it works. One run for a part at a
 * time; a failure is reported to the page that asked and never reaches the story.
 *
 * @param <R> what the work answers (the outcome of a translation)
 */
public final class PartRuns<R> {

    private final String threadName;
    private final Logger log;
    private final Set<String> running = ConcurrentHashMap.newKeySet();

    /**
     * @param threadName the name of the worker threads (the name of the plugin)
     */
    public PartRuns(String threadName, Logger log) {
        this.threadName = threadName;
        this.log = log;
    }

    /**
     * Whether a part is being worked on.
     */
    public boolean isRunning(ChatMessage part) {
        return running.contains(part.getUuid());
    }

    /**
     * Runs the job on a worker thread; call it on the thread of the user's request.
     *
     * @param done called on the thread of the page (in the session of the user) when the run is over, with the result, or with the failure (the
     *             result is null then); may be null
     * @return false when the part is being worked on already (nothing is started)
     */
    public boolean start(ChatMessage part, Callable<R> job, BiConsumer<R, Throwable> done) {
        return start(part, job, done, null);
    }

    /**
     * As {@link #start(ChatMessage, Callable, BiConsumer)}, with a progress that the requests of the job report to and that stops them (see {@link RunProgress}).
     *
     * @param progress bound to the worker thread for the job; may be null
     */
    public boolean start(ChatMessage part, Callable<R> job, BiConsumer<R, Throwable> done, RunProgress progress) {
        if (!running.add(part.getUuid())) {
            return false;
        }
        RequestAttributes attributes = ThreadCopyRequestAttributes.create();
        UI ui = UI.getCurrent();
        Thread.ofVirtual().name(threadName).start(() -> {
            R result = null;
            Throwable failure = null;
            try (InRequestScope _ = new InRequestScope(attributes); RunProgress.Binding _ = progress == null ? null : progress.bind()) {
                result = job.call();
            } catch (CancellationException e) {
                failure = e;
                log.info("The run on a part was stopped");
            } catch (Throwable e) {
                failure = e;
                log.warn("Failed to translate a part: {}", e.getMessage(), e);
            } finally {
                running.remove(part.getUuid());
            }
            if (done != null && ui != null) {
                R finalResult = result;
                Throwable finalFailure = failure;
                try {
                    ui.access(() -> {
                        // the page's own code (the services that load the story again) needs the session of the user, which this thread has not
                        try (InRequestScope _ = new InRequestScope(attributes)) {
                            done.accept(finalResult, finalFailure);
                        } catch (RuntimeException e) {
                            log.warn("The page could not show the result of a translation: {}", e.getMessage(), e);
                        }
                        UIPushGuard.push(ui);
                    });
                } catch (UIDetachedException e) {
                    log.debug("The page is gone, the result of the translation is not shown");
                }
            }
        });
        return true;
    }
}
