package com.github.enerccio.marginalia.extensions.conlangtranslator.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.TranslationService.Outcome;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import org.slf4j.Logger;
import org.springframework.web.context.request.RequestAttributes;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Runs the translations of parts on worker threads: a request to a model takes minutes, so the page goes on while it works. One run for a
 * part at a time; a failure is reported to the page that asked and never reaches the story.
 */
public final class TranslationRuns {

    private final TranslationService service;
    private final Logger log;
    private final Set<String> running = ConcurrentHashMap.newKeySet();

    public TranslationRuns(TranslationService service, Logger log) {
        this.service = service;
        this.log = log;
    }

    /**
     * Whether a part is being translated.
     */
    public boolean isRunning(ChatMessage part) {
        return running.contains(part.getUuid());
    }

    /**
     * Translates the part on a worker thread; call it on the thread of the user's request.
     *
     * @param done called on the thread of the page (in the session of the user) when the run is over, with the outcome, or with the failure (the outcome is null then);
     *             may be null
     * @return false when the part is being translated already (nothing is started)
     */
    public boolean start(Manuscript manuscript, ChatMessage part, BiConsumer<Outcome, Throwable> done) {
        return run(part, () -> service.translate(manuscript, part), done);
    }

    /**
     * Translates the sentence selected in the part, see {@link TranslationService#translateSelection}; the rules of {@link #start} apply.
     */
    public boolean startSelection(Manuscript manuscript, ChatMessage part, String expected, int from, int to, BiConsumer<Outcome, Throwable> done) {
        return run(part, () -> service.translateSelection(manuscript, part, expected, from, to), done);
    }

    private boolean run(ChatMessage part, Callable<Outcome> job, BiConsumer<Outcome, Throwable> done) {
        if (!running.add(part.getUuid())) {
            return false;
        }
        RequestAttributes attributes = ThreadCopyRequestAttributes.create();
        UI ui = UI.getCurrent();
        Thread.ofVirtual().name("conlang-translator").start(() -> {
            Outcome outcome = null;
            Throwable failure = null;
            try (InRequestScope _ = new InRequestScope(attributes)) {
                outcome = job.call();
            } catch (Throwable e) {
                failure = e;
                log.warn("Failed to translate a part: {}", e.getMessage(), e);
            } finally {
                running.remove(part.getUuid());
            }
            if (done != null && ui != null) {
                Outcome finalOutcome = outcome;
                Throwable finalFailure = failure;
                try {
                    ui.access(() -> {
                        // the page's own code (the services that load the story again) needs the session of the user, which this thread has not
                        try (InRequestScope _ = new InRequestScope(attributes)) {
                            done.accept(finalOutcome, finalFailure);
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
