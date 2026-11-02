package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.Registration;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A listener shows the user something through {@code GenerationControllerEvent.accessUI} (the Prompt Inspector shows the prompt in
 * a dialog before the request): the generation waits for the answer, sending goes on, stopping ends the generation before the
 * request, and a generation without a UI is not held at all.
 */
class GenerationAccessUiTest extends GenerationTestBase {

    /**
     * A generation whose UI answers: {@code answer} gets the action that the listener scheduled and plays the user.
     */
    private static class AnsweringRun extends GenerationRun {
        private final boolean hasUi;
        final List<String> shown = new CopyOnWriteArrayList<>();

        AnsweringRun(boolean hasUi) {
            this.hasUi = hasUi;
        }

        @Override
        public boolean accessUI(Consumer<UI> action) {
            if (!hasUi) {
                return false;
            }
            new Thread(() -> action.accept(null), "fake ui").start();
            return true;
        }
    }

    /**
     * Holds the generation before the inference, the way an extension does: shows the prompt, {@code send} decides what the user
     * pressed.
     */
    private Registration inspect(Manuscript manuscript, List<String> log, boolean send) {
        return storyGenerationService.addEventListener(Events.BEFORE_INFERENCE, (event, chain) -> {
            if (event.getManuscript() == null || !event.getManuscript().getId().equals(manuscript.getId())) {
                chain.next();
                return;
            }
            AtomicBoolean answered = new AtomicBoolean();
            Runnable proceed = () -> {
                if (answered.compareAndSet(false, true)) {
                    chain.next();
                }
            };
            event.getCancellationToken().onCancel(proceed);
            boolean scheduled = event.accessUI(ui -> {
                log.add("shown " + event.getPayload().getLast().getContent());
                if (!send) {
                    event.getCancellationToken().cancel();
                }
                proceed.run();
            });
            if (!scheduled) {
                log.add("no ui");
                proceed.run();
            }
        });
    }

    private GenerationRun generate(Manuscript manuscript, GenerationRun run) throws Exception {
        storyGenerationService.generateNextTurn(manuscriptService.find(manuscript.getId()),
                new TurnInput(null, null, null, "write it"), GenerationRequest.newMessage(), run);
        return run;
    }

    @Test
    void sendingGoesOnWithTheRequest() throws Exception {
        Manuscript manuscript = manuscriptService.save(newManuscript());
        List<String> log = new CopyOnWriteArrayList<>();
        Registration registration = inspect(manuscript, log, true);
        try {
            llm.enqueue(MockLLMResponse.text("The end."));

            GenerationRun run = generate(manuscript, new AnsweringRun(true));

            assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
            assertThat(log).containsExactly("shown write it");
            assertThat(run.getResponse()).isEqualTo("The end.");
            assertThat(llm.getCompletionRequests()).hasSize(1);
        } finally {
            registration.unregister();
        }
    }

    @Test
    void stoppingEndsTheGenerationBeforeTheRequest() throws Exception {
        Manuscript manuscript = manuscriptService.save(newManuscript());
        List<String> log = new CopyOnWriteArrayList<>();
        Registration registration = inspect(manuscript, log, false);
        try {
            GenerationRun run = generate(manuscript, new AnsweringRun(true));

            assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.CANCELLED);
            assertThat(log).containsExactly("shown write it");
            // nothing was sent, and no empty part is left behind
            assertThat(llm.getCompletionRequests()).isEmpty();
            assertThat(run.getErrors()).isEmpty();
            assertThat(manuscriptService.find(manuscript.getId()).getActiveLeaf()).isNull();
        } finally {
            registration.unregister();
        }
    }

    @Test
    void aGenerationWithoutUiIsNotHeld() throws Exception {
        Manuscript manuscript = manuscriptService.save(newManuscript());
        List<String> log = new CopyOnWriteArrayList<>();
        Registration registration = inspect(manuscript, log, false);
        try {
            llm.enqueue(MockLLMResponse.text("The end."));

            // an agent or a command: the listener has nobody to show it to
            GenerationRun run = generate(manuscript, new AnsweringRun(false));

            assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
            assertThat(log).containsExactly("no ui");
            assertThat(llm.getCompletionRequests()).hasSize(1);
        } finally {
            registration.unregister();
        }
    }
}
