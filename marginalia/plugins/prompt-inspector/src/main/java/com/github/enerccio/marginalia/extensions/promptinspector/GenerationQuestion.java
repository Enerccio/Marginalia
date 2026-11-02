package com.github.enerccio.marginalia.extensions.promptinspector;

import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.EventChain;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Asks the user a question in the middle of a generation and lets the generation go on by the answer. The listener that
 * calls {@link #ask} holds the {@link EventChain}: nothing is blocked while the question is open, the generation is just not
 * going on until the chain is released, so whatever can end the question must release it, exactly once: the answer, a stop
 * from another window, the page of the user going away.
 */
final class GenerationQuestion {

    /**
     * What the user sees, a dialog; a fake in the tests.
     */
    interface View {
        void open();

        void close();

        /**
         * Runs the action when the view is removed from the page, whatever the reason (an answer, or the page was closed or
         * expired with the question still open).
         */
        void whenDetached(Runnable action);
    }

    /**
     * Makes the view on the thread of the user's page.
     *
     * @param send   the user agreed: the generation goes on
     * @param cancel the user refused: the generation is stopped before the request
     */
    @FunctionalInterface
    interface ViewFactory {
        View create(Runnable send, Runnable cancel);
    }

    private GenerationQuestion() {
    }

    /**
     * Shows the view on the page that started the generation.
     *
     * @return whether the question is out, so that the chain goes on by the answer; false when nobody can be asked (an agent or
     * a command started the generation, or the page is gone), then the chain is not touched and the caller goes on
     */
    static boolean ask(GenerationControllerEvent event, EventChain chain, Logger log, ViewFactory factory) {
        AtomicBoolean answered = new AtomicBoolean();
        AtomicReference<View> view = new AtomicReference<>();
        AtomicReference<Runnable> unsubscribe = new AtomicReference<>(() -> { });

        // whatever answers first lets the generation go on, once
        Runnable proceed = () -> {
            if (answered.compareAndSet(false, true)) {
                unsubscribe.get().run();
                chain.next();
            }
        };
        // the user refused, or is not there any more: the generation ends in its cleanup, no request is sent
        Runnable stop = () -> {
            event.getCancellationToken().cancel();
            proceed.run();
        };

        // stopped from elsewhere (another window): go on first, then the view has no use any more. Nothing may keep the chain from
        // going on, and a listener that throws would keep the rest of the token's listeners from running
        unsubscribe.set(event.getCancellationToken().onCancel(() -> {
            try {
                proceed.run();
            } finally {
                closeQuietly(event, view, log);
            }
        }));
        // cancelled already: the listener has run and the chain goes on, do not ask
        if (answered.get()) {
            return true;
        }

        boolean scheduled;
        try {
            scheduled = event.accessUI(ui -> {
                try {
                    View shown = factory.create(proceed, stop);
                    view.set(shown);
                    // the page was closed or expired with the question open: nobody will answer, so it is a refusal
                    shown.whenDetached(() -> {
                        if (!answered.get()) {
                            stop.run();
                        }
                    });
                    shown.open();
                } catch (RuntimeException e) {
                    // generate without asking rather than keep the generation waiting for a question nobody sees
                    log.warn("Could not show the question: {}", e.getMessage(), e);
                    proceed.run();
                }
            });
        } catch (RuntimeException e) {
            log.warn("Could not show the question: {}", e.getMessage(), e);
            scheduled = false;
        }
        if (!scheduled) {
            unsubscribe.get().run();
            // an answer cannot have come, but the cancellation may have: then the chain has gone on already
            return answered.get();
        }
        return true;
    }

    private static void closeQuietly(GenerationControllerEvent event, AtomicReference<View> view, Logger log) {
        try {
            event.accessUI(ui -> {
                View shown = view.get();
                if (shown != null) {
                    shown.close();
                }
            });
        } catch (RuntimeException e) {
            log.debug("The question could not be closed: {}", e.getMessage());
        }
    }
}
