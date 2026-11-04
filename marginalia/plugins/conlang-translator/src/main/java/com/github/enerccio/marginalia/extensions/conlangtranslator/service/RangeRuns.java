package com.github.enerccio.marginalia.extensions.conlangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.RangeLoop.Mode;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.RangeLoop.PartResult;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.RangeLoop.RangePart;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.RangeLoop.Summary;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.TranslationService.Outcome;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.TranslationService.Reverted;
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

/**
 * Runs an action over a range of parts on a worker thread of the user's session, one part after the other (a part is minutes of a model's
 * time), telling the page how far it is and what it came to. One range run at a time for a book.
 */
public final class RangeRuns {

    /** What the page can do with a run that is on its way. */
    public interface Handle {
        /** Stops after the part that is being done: the request to the model that is on its way can not be taken back. */
        void stop();
    }

    private final TranslationService service;
    private final Logger log;
    private final java.util.Set<String> running = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public RangeRuns(TranslationService service, Logger log) {
        this.service = service;
        this.log = log;
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
     * @param progress told on the thread of the page with the number of the parts done before each part
     * @param done     told on the thread of the page when the run is over, with the summary, or the failure (the summary is null then)
     * @return the handle of the run, null when a run of the book is on its way already (nothing is started)
     */
    public Handle start(Manuscript book, Mode mode, List<RangePart> parts, IntConsumer progress, BiConsumer<Summary, Throwable> done) {
        if (!running.add(book.getUuid())) {
            return null;
        }
        RequestAttributes attributes = ThreadCopyRequestAttributes.create();
        UI ui = UI.getCurrent();
        AtomicBoolean stop = new AtomicBoolean();
        Thread.ofVirtual().name("conlang-translator-range").start(() -> {
            Summary summary = null;
            Throwable failure = null;
            try (InRequestScope _ = new InRequestScope(attributes)) {
                summary = RangeLoop.run(parts, part -> apply(book, mode, part), stop, count -> tell(ui, attributes, () -> progress.accept(count)));
            } catch (Throwable e) {
                failure = e;
                log.warn("The range run failed: {}", e.getMessage(), e);
            } finally {
                running.remove(book.getUuid());
            }
            Summary finalSummary = summary;
            Throwable finalFailure = failure;
            tell(ui, attributes, () -> done.accept(finalSummary, finalFailure));
        });
        return () -> stop.set(true);
    }

    private PartResult apply(Manuscript book, Mode mode, ChatMessage part) throws Exception {
        if (mode == Mode.REVERT) {
            Reverted reverted = service.revert(book, part);
            return reverted.reverted() == 0 ? PartResult.nothing() : PartResult.done(reverted.reverted(), 0, reverted.kept());
        }
        Outcome outcome = service.translate(book, part);
        return switch (outcome.status()) {
            case TRANSLATED -> PartResult.done(outcome.translated(), outcome.newWords(), 0);
            case NOTHING_TO_TRANSLATE, EMPTY -> PartResult.nothing();
            case NO_LANGUAGE, NO_MODEL -> PartResult.abort(outcome.note());
            case BAD_ANSWER -> PartResult.failed(outcome.note());
        };
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
