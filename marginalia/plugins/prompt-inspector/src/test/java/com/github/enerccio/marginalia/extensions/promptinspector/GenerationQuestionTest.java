package com.github.enerccio.marginalia.extensions.promptinspector;

import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.EventChain;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The question a generation waits for: whatever ends it lets the generation go on exactly once, a refusal and a page that goes
 * away cancel it, and nobody to ask means the caller goes on.
 */
class GenerationQuestionTest {

    private static final Logger LOG = NOPLogger.NOP_LOGGER;

    /** What the page does with the actions it is given. */
    private enum Page {
        /** Runs the action at once. */
        UP,
        /** Is not there: the generation has no UI. */
        NONE,
        /** Is gone: the call throws. */
        DETACHED
    }

    private static class FakeView implements GenerationQuestion.View {
        final Runnable send;
        final Runnable cancel;
        Runnable detached;
        boolean opened;
        boolean closed;

        FakeView(Runnable send, Runnable cancel) {
            this.send = send;
            this.cancel = cancel;
        }

        @Override
        public void open() {
            opened = true;
        }

        @Override
        public void close() {
            closed = true;
        }

        @Override
        public void whenDetached(Runnable action) {
            detached = action;
        }
    }

    private CancellationToken token;
    private Page page;
    private int nexts;
    private final List<FakeView> views = new ArrayList<>();
    private GenerationControllerEvent event;
    private EventChain chain;

    @BeforeEach
    void setUp() {
        token = new CancellationToken();
        page = Page.UP;
        nexts = 0;
        views.clear();
        event = (GenerationControllerEvent) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{GenerationControllerEvent.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getCancellationToken" -> token;
                    case "accessUI" -> {
                        switch (page) {
                            case NONE -> {
                                yield false;
                            }
                            case DETACHED -> throw new IllegalStateException("UI detached");
                            default -> {
                                @SuppressWarnings("unchecked")
                                Consumer<UI> action = (Consumer<UI>) args[0];
                                action.accept(null);
                                yield true;
                            }
                        }
                    }
                    case "toString" -> "event";
                    case "hashCode" -> 1;
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        chain = new EventChain() {
            @Override
            public void next() {
                nexts++;
            }

            @Override
            public void terminate() {
                fail("the question never terminates the chain");
            }
        };
    }

    private boolean ask() {
        return GenerationQuestion.ask(event, chain, LOG, (send, cancel) -> {
            FakeView view = new FakeView(send, cancel);
            views.add(view);
            return view;
        });
    }

    private FakeView view() {
        assertEquals(1, views.size());
        return views.getFirst();
    }

    @Test
    void theQuestionIsOpenAndTheGenerationWaitsForTheAnswer() {
        assertTrue(ask());

        assertTrue(view().opened);
        assertEquals(0, nexts);
        assertFalse(token.isCancelled());
    }

    @Test
    void sendGoesOnWithoutCancelling() {
        ask();

        view().send.run();

        assertEquals(1, nexts);
        assertFalse(token.isCancelled());
    }

    @Test
    void cancelStopsTheGenerationAndGoesOnOnce() {
        ask();

        view().cancel.run();

        assertTrue(token.isCancelled());
        assertEquals(1, nexts);
    }

    @Test
    void thePageGoingAwayWithTheQuestionOpenIsARefusal() {
        ask();

        view().detached.run();

        assertTrue(token.isCancelled());
        assertEquals(1, nexts);
    }

    @Test
    void closingAfterSendIsNotARefusal() {
        ask();

        view().send.run();
        view().detached.run();

        assertFalse(token.isCancelled());
        assertEquals(1, nexts);
    }

    @Test
    void closingAfterCancelGoesOnOnlyOnce() {
        ask();

        view().cancel.run();
        view().detached.run();

        assertEquals(1, nexts);
    }

    @Test
    void aSecondAnswerChangesNothing() {
        ask();

        view().send.run();
        view().cancel.run();
        view().send.run();

        assertEquals(1, nexts);
    }

    @Test
    void stoppedFromAnotherWindowGoesOnAndClosesTheQuestion() {
        ask();

        token.cancel();

        assertEquals(1, nexts);
        assertTrue(view().closed);
    }

    @Test
    void stoppedFromElsewhereAndThenClosedGoesOnOnce() {
        ask();

        token.cancel();
        view().detached.run();

        assertEquals(1, nexts);
    }

    @Test
    void aGenerationWithoutAPageIsNotAsked() {
        page = Page.NONE;

        assertFalse(ask());

        assertEquals(0, nexts, "the caller goes on, the question does not touch the chain");
        assertTrue(views.isEmpty());
        token.cancel();
        assertEquals(0, nexts, "and nothing is left listening to the token");
    }

    @Test
    void aPageThatIsGoneIsNotAsked() {
        page = Page.DETACHED;

        assertFalse(ask());

        assertEquals(0, nexts);
        token.cancel();
        assertEquals(0, nexts);
    }

    @Test
    void aGenerationThatWasStoppedAlreadyGoesOnAndIsNotAsked() {
        token.cancel();

        assertTrue(ask());

        assertEquals(1, nexts);
        assertTrue(views.isEmpty());
    }

    @Test
    void aQuestionThatCanNotBeBuiltLetsTheGenerationGoOnUnasked() {
        assertTrue(GenerationQuestion.ask(event, chain, LOG, (send, cancel) -> {
            throw new IllegalStateException("no dialog");
        }));

        assertEquals(1, nexts);
        assertFalse(token.isCancelled());
    }

    @Test
    void aPageThatIsGoneWhenTheQuestionIsClosedDoesNotKeepTheGenerationWaiting() {
        ask();
        page = Page.DETACHED;

        token.cancel();

        assertEquals(1, nexts);
    }

    @Test
    void otherListenersOfTheTokenStillRunWhenTheQuestionCanNotBeClosed() {
        ask();
        List<String> ran = new ArrayList<>();
        token.onCancel(() -> ran.add("other"));
        page = Page.DETACHED;

        token.cancel();

        assertEquals(List.of("other"), ran);
    }
}
