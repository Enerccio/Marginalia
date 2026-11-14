package com.github.enerccio.marginalia.extensions.translatorcommon.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.Mode;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.PartResult;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.RangePart;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeLoop.Summary;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import org.slf4j.Logger;
import org.springframework.web.context.request.RequestAttributes;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * Runs an action over a range of parts on a worker thread of the user's session, one part after the other (a part is minutes of a model's
 * time), telling the page how far it is and what it came to. One range run at a time for a book. The run is a run of the core's {@link Runs}
 * that holds the parts of the range (writing them, by default): a translation of one of them by any translator is refused while it works,
 * and a delete of them waits for it.
 */
public final class RangeRuns {

    /** What the page can do with a run that is on its way. */
    public interface Handle {
        /** Stops after the part that is being done: the request to the model that is on its way can not be taken back. */
        void stop();
    }

    /** What the plugin does to one part: translates it or puts the English back; the answer is how it went. */
    public interface Work {
        /**
         * @param languageId the language of the book to work on, null for all of them
         */
        PartResult apply(Manuscript book, Mode mode, ChatMessage part, String languageId) throws Exception;
    }

    private final Work work;
    private final String threadName;
    private final Logger log;
    private final Supplier<String> label;
    private final Runs.Mode partMode;
    private final java.util.Set<String> running = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /**
     * @param work       what is done to one part
     * @param threadName the name of the worker threads (the name of the plugin)
     */
    public RangeRuns(Work work, String threadName, Logger log) {
        this(work, threadName, log, null, Runs.Mode.WRITE);
    }

    /**
     * @param label    what the run does, shown on the cards of its parts (read each time, localized); null shows the thread name
     * @param partMode how the run holds the parts of the range: {@link Runs.Mode#WRITE} when it changes their text,
     *                 {@link Runs.Mode#READ} when it only reads them (nothing is refused then)
     */
    public RangeRuns(Work work, String threadName, Logger log, Supplier<String> label, Runs.Mode partMode) {
        this.work = work;
        this.threadName = threadName;
        this.log = log;
        this.label = label == null ? () -> threadName : label;
        this.partMode = partMode;
    }

    /**
     * Whether a range run of the book is on its way.
     */
    public boolean isRunning(Manuscript book) {
        return running.contains(book.getUuid());
    }

    /**
     * Starts the run; call it on the thread of the user's request.
     *
     * @param languageId the language of the book to work on (its id), null for all of them
     * @param progress told on the thread of the page with the number of the parts done before each part
     * @param done     told on the thread of the page when the run is over, with the summary, or the failure (the summary is null then)
     * @return the handle of the run, null when a run of the book is on its way already (nothing is started)
     * @throws Runs.HeldException when the parts are written by another run (a translation of one of them), naming it
     */
    public Handle start(Manuscript book, Mode mode, String languageId, List<RangePart> parts, IntConsumer progress, BiConsumer<Summary, Throwable> done) {
        if (!running.add(book.getUuid())) {
            return null;
        }
        AtomicBoolean stop = new AtomicBoolean();
        Runs.Run run;
        try {
            Runs.Builder builder = Runs.run(threadName, label).book(book).currentPage().onStop(() -> stop.set(true));
            List<ChatMessage> held = parts.stream().map(RangePart::part).toList();
            held.forEach(part -> builder.hold(part, partMode));
            if (partMode == Runs.Mode.READ) {
                builder.quiet();
            }
            run = partMode == Runs.Mode.WRITE ? builder.startUnless(held, Runs.Operation.WRITE) : builder.start();
        } catch (RuntimeException e) {
            running.remove(book.getUuid());
            throw e;
        }
        RequestAttributes attributes = ThreadCopyRequestAttributes.create();
        UI ui = UI.getCurrent();
        Thread.ofVirtual().name(threadName).start(() -> {
            Summary summary = null;
            Throwable failure = null;
            try (InRequestScope _ = new InRequestScope(attributes); Runs.Binding _ = run.bind()) {
                summary = RangeLoop.run(parts, part -> work.apply(book, mode, part, languageId), stop, count -> tell(ui, attributes, () -> progress.accept(count)));
            } catch (Throwable e) {
                failure = e;
                log.warn("The range run failed: {}", e.getMessage(), e);
            } finally {
                running.remove(book.getUuid());
                run.end(failure == null ? Runs.Outcome.FINISHED : Runs.Outcome.FAILED);
            }
            Summary finalSummary = summary;
            Throwable finalFailure = failure;
            tell(ui, attributes, () -> done.accept(finalSummary, finalFailure));
        });
        return () -> stop.set(true);
    }

    /** Runs the action on the page, in the session of the user; the page runs them in the order they were queued. */
    private void tell(UI ui, RequestAttributes attributes, Runnable action) {
        if (ui == null) {
            return;
        }
        try {
            ui.access(() -> {
                try (InRequestScope _ = new InRequestScope(attributes)) {
                    action.run();
                } catch (RuntimeException e) {
                    log.warn("The page could not show the progress of a range run: {}", e.getMessage(), e);
                }
                UIPushGuard.push(ui);
            });
        } catch (UIDetachedException e) {
            log.debug("The page is gone, the progress of the range run is not shown");
        }
    }
}
