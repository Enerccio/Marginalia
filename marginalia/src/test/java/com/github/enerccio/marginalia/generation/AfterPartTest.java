package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.impl.generation.AfterPart;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.Registration;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * What follows a generated part: the steps bound to the stages, run after a completed generation as one run of the registry.
 */
class AfterPartTest extends GenerationTestBase {

    private final List<Registration> bindings = new ArrayList<>();
    private final List<String> done = new CopyOnWriteArrayList<>();

    @AfterEach
    void unbind() {
        bindings.forEach(Registration::unregister);
        bindings.clear();
    }

    /** A step of the test that runs only for the book of the test. */
    private void bind(Manuscript book, String id, AfterPart.Stage stage, AfterPart.Mode mode, AfterPart.Handler work) {
        bindings.add(storyGenerationService.addAfterPartBinding(new AfterPart.Binding(id, () -> id, stage, mode, new AfterPart.Handler() {
            @Override
            public boolean appliesTo(Manuscript candidate) {
                return Objects.equals(candidate.getId(), book.getId());
            }

            @Override
            public void run(AfterPart.Context context) throws Exception {
                work.run(context);
                done.add(id);
            }
        })));
    }

    /** Completes with the run of the phase of the book when it ends. */
    private CompletableFuture<Runs.Run> phaseEnd(Manuscript book) {
        CompletableFuture<Runs.Run> ended = new CompletableFuture<>();
        Runs.addEndListener(run -> {
            if (Runs.AFTER_PART.equals(run.getSource()) && Objects.equals(run.getBookId(), book.getId())) {
                ended.complete(run);
            }
        });
        return ended;
    }

    private static final class Watched extends GenerationRun {
        private final CompletableFuture<ChatMessage> finalPart = new CompletableFuture<>();
        private final List<Long> changed = new CopyOnWriteArrayList<>();

        @Override
        public void onPartFinal(ChatMessage part) {
            finalPart.complete(part);
        }

        @Override
        public void onPartChanged(ChatMessage part) {
            changed.add(part.getId());
        }
    }

    private Watched start(Manuscript book, GenerationOptions options) throws Exception {
        Watched run = new Watched();
        storyGenerationService.generateNextTurn(manuscriptService.find(book.getId()), new TurnInput(null, null, null, "go"),
                GenerationRequest.newMessage(), run, options);
        run.await(TIMEOUT);
        return run;
    }

    @Test
    void theStepsRunByStageTheParallelOnesTogetherWithTheFinalPartAfterTheTextChangingOnes() throws Exception {
        Manuscript book = manuscriptService.save(newManuscript());
        CountDownLatch both = new CountDownLatch(2);
        AtomicReference<Runs.Run> child = new AtomicReference<>();
        // bound in the wrong order on purpose: the stage decides
        bind(book, "extract-a", AfterPart.Stage.EXTRACT, AfterPart.Mode.PARALLEL, context -> {
            both.countDown();
            assertThat(both.await(10, TimeUnit.SECONDS)).as("the parallel steps run side by side").isTrue();
        });
        bind(book, "extract-b", AfterPart.Stage.EXTRACT, AfterPart.Mode.PARALLEL, context -> {
            both.countDown();
            assertThat(both.await(10, TimeUnit.SECONDS)).isTrue();
        });
        bind(book, "transform", AfterPart.Stage.TRANSFORM, AfterPart.Mode.SEQUENTIAL, context -> {
            assertThat(done).containsExactly("review");
            // a run the step starts is a part of the phase's entry
            Runs.Run started = Runs.run("test", () -> "translating").start();
            child.set(started);
            started.end();
            context.changed();
        });
        bind(book, "review", AfterPart.Stage.REVIEW, AfterPart.Mode.SEQUENTIAL, context -> {
            assertThat(context.getPart().getResponse()).isEqualTo("generated");
            assertThat(context.getRun().blocksGeneration()).as("notes do not hold the next part back").isFalse();
        });
        CompletableFuture<Runs.Run> ended = phaseEnd(book);

        Watched generation = start(book, GenerationOptions.defaults());
        Runs.Run phase = ended.get(30, TimeUnit.SECONDS);

        assertThat(done).hasSize(4);
        assertThat(done.subList(0, 2)).containsExactly("review", "transform");
        assertThat(phase.getOutcome()).isEqualTo(Runs.Outcome.FINISHED);
        assertThat(child.get().getParent()).isSameAs(phase);
        assertThat(generation.finalPart.get(5, TimeUnit.SECONDS).getId()).isEqualTo(generation.getMessage().getId());
        assertThat(generation.changed).containsExactly(generation.getMessage().getId());
        assertThat(phase.getLabel()).contains("review ✓").contains("extract-b ✓");
    }

    @Test
    void aRewriteRefusesTheNextGenerationOfTheBookWhileItRuns() throws Exception {
        Manuscript book = manuscriptService.save(newManuscript());
        CompletableFuture<Void> release = new CompletableFuture<>();
        CountDownLatch rewriting = new CountDownLatch(1);
        bind(book, "redraft", AfterPart.Stage.REWRITE, AfterPart.Mode.SEQUENTIAL, context -> {
            rewriting.countDown();
            context.await(release);
        });
        CompletableFuture<Runs.Run> ended = phaseEnd(book);
        start(book, GenerationOptions.defaults());

        assertThat(rewriting.await(10, TimeUnit.SECONDS)).isTrue();
        Manuscript stored = manuscriptService.find(book.getId());
        assertThatThrownBy(() -> Runs.check(stored, Runs.Operation.GENERATE)).isInstanceOf(Runs.HeldException.class);
        release.complete(null);
        ended.get(30, TimeUnit.SECONDS);
        Runs.check(stored, Runs.Operation.GENERATE);
    }

    @Test
    void stopEndsThePhaseAndTheLaterStepsAreNotRunAndNamed() throws Exception {
        Manuscript book = manuscriptService.save(newManuscript());
        CountDownLatch waiting = new CountDownLatch(1);
        bind(book, "review", AfterPart.Stage.REVIEW, AfterPart.Mode.SEQUENTIAL, context -> {
            context.waitForAuthor(() -> {
            });
            waiting.countDown();
            context.await(new CompletableFuture<>());
        });
        bind(book, "media", AfterPart.Stage.MEDIA, AfterPart.Mode.PARALLEL, context -> {
        });
        CompletableFuture<Runs.Run> ended = phaseEnd(book);
        Watched generation = start(book, GenerationOptions.defaults());

        assertThat(waiting.await(10, TimeUnit.SECONDS)).isTrue();
        Runs.Run phase = Runs.ofBook(book.getId()).stream().filter(run -> Runs.AFTER_PART.equals(run.getSource())).findFirst().orElseThrow();
        assertThat(phase.isWaitingForUser()).isTrue();
        assertThat(phase.getOnOpen()).isNotNull();
        phase.stop();
        ended.get(30, TimeUnit.SECONDS);

        assertThat(phase.getOutcome()).isEqualTo(Runs.Outcome.STOPPED);
        assertThat(done).isEmpty();
        assertThat(generation.finalPart).as("the part is in the author's hands").isNotDone();
        assertThat(generation.getWarnings()).anyMatch(warning -> warning.contains("review") && warning.contains("media"));
    }

    @Test
    void aFailureIsAStop() throws Exception {
        Manuscript book = manuscriptService.save(newManuscript());
        bind(book, "review", AfterPart.Stage.REVIEW, AfterPart.Mode.SEQUENTIAL, context -> {
            throw new IllegalStateException("the model is gone");
        });
        bind(book, "extract", AfterPart.Stage.EXTRACT, AfterPart.Mode.SEQUENTIAL, context -> {
        });
        CompletableFuture<Runs.Run> ended = phaseEnd(book);
        Watched generation = start(book, GenerationOptions.defaults());
        Runs.Run phase = ended.get(30, TimeUnit.SECONDS);

        assertThat(phase.getOutcome()).isEqualTo(Runs.Outcome.FAILED);
        assertThat(done).isEmpty();
        assertThat(generation.getWarnings()).anyMatch(warning -> warning.contains("the model is gone") && warning.contains("extract"));
    }

    @Test
    void nothingFollowsTheGenerationOfAnAgentAndAStepThatDoesNotApplyIsLeftOut() throws Exception {
        Manuscript book = manuscriptService.save(newManuscript());
        bind(book, "review", AfterPart.Stage.REVIEW, AfterPart.Mode.SEQUENTIAL, context -> {
        });
        Watched agent = start(book, GenerationOptions.defaults().withoutAfterPart());
        Thread.sleep(300);
        assertThat(done).isEmpty();
        assertThat(agent.finalPart).isNotDone();

        // another book: the step does not apply, nothing runs and the part is final at once
        Manuscript other = manuscriptService.save(newManuscript());
        Watched generation = start(other, GenerationOptions.defaults());
        assertThat(generation.finalPart.get(10, TimeUnit.SECONDS)).isNotNull();
        assertThat(done).isEmpty();
    }
}
