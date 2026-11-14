package com.github.enerccio.marginalia.domain.service.impl.generation;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.vaadin.flow.component.UI;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * What follows a generated part: the second phase of a generation. When a generation of the author (from the page or the command bar,
 * not of an agent) completed, the engine goes on, on the server and with no page needed, through the {@link Stage}s in their order;
 * the core and the plugins bind a {@link Handler} to a stage ({@code StoryGenerationService.addAfterPartBinding}). The phase is one run
 * of the registry ({@link Runs#AFTER_PART}): one entry in the panel of the runs, with the text of the step that has the model and one
 * Stop.
 * <p>
 * Rules: the steps of a stage that are {@link Mode#SEQUENTIAL} run one after another, in the order they were bound, each seeing what
 * the one before left; then its {@link Mode#PARALLEL} ones start together, and the stage ends when all are done. Stop ends the phase:
 * the step that runs applies nothing, the ones after it are not run, and there is no resume (the author does the rest by hand, each
 * binding has its manual action). A failure is a Stop. While a {@link Stage#REWRITE} step runs, a new generation of the book is
 * refused; the part is held (no delete, no move) until the end of {@link Stage#TRANSFORM}.
 */
public final class AfterPart {

    private AfterPart() {
    }

    /**
     * The stages, in their order: what changes the text comes before what reads it.
     */
    public enum Stage {
        /** Notes on the new text (the automatic annotation). */
        REVIEW,
        /** The text is written again (the automatic redraft); the next generation waits for it. */
        REWRITE,
        /** Passages of the final text are replaced (the translators). */
        TRANSFORM,
        /** Data is read out of the final text (the Knowledge Tracker). */
        EXTRACT,
        /** What is made of the final text (the audiobook). */
        MEDIA
    }

    /**
     * How a step runs among the others of its stage.
     */
    public enum Mode {
        /** Alone, in the order of the bindings; for a step that changes the text of the part or needs the one before it. */
        SEQUENTIAL,
        /** Beside the other parallel steps of the stage, after its sequential ones; it must not change the text of the part. */
        PARALLEL
    }

    /**
     * The work of a step: plain blocking code on a worker thread, in the request scope of the user who generated, with the run of the
     * phase bound (the requests it makes to a model show in the panel, the runs it starts are a part of the phase's entry).
     */
    public interface Handler {

        /**
         * Whether the book uses the step (its automatic switch is on). A step that does not apply is not shown in the panel.
         */
        default boolean appliesTo(Manuscript book) throws Exception {
            return true;
        }

        /**
         * Does the step; returns when it is done. Throws to fail (a failure ends the phase); {@link Stopped} (from
         * {@link Context#await}) when the phase was stopped.
         */
        void run(Context context) throws Exception;
    }

    /**
     * A step bound to a stage.
     *
     * @param id    stable and unique: the key of the plugin plus the step, the same id as its notification source when it has one
     * @param name  what the step is called in the panel ("translate"), asked each time it is shown
     */
    public record Binding(String id, Supplier<String> name, Stage stage, Mode mode, Handler handler) {
        public Binding {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(stage, "stage");
            Objects.requireNonNull(mode, "mode");
            Objects.requireNonNull(handler, "handler");
        }
    }

    /**
     * The phase was stopped (by the author, a failure of another step, the part deleted or generated again) while a step waited.
     */
    public static final class Stopped extends Exception {
        public Stopped() {
            super("Stopped");
        }
    }

    /**
     * What a step has in its hands.
     */
    public interface Context {

        /** The book, as it is stored when the step starts. */
        Manuscript getManuscript();

        /** The generated part, as it is stored when the step starts (with what the steps before it did). */
        ChatMessage getPart();

        /** What the generation was (a new part, a swipe, a regeneration). */
        GenerationRequest getRequest();

        /** The AI the part was generated with. */
        AI getAi();

        Protocol getProtocol();

        /** The turn the author gave. */
        TurnInput getInput();

        /**
         * The properties of the generation ({@link GenerationProperties}), and what the steps before put there: shared by the steps of
         * the phase (prefix your keys with the package of the extension).
         */
        Map<String, Object> getProperties();

        /** The run of the phase. */
        Runs.Run getRun();

        boolean isStopped();

        /** Run when the phase is stopped (at once when it is stopped already): stop the work of the step. */
        void onStop(Runnable action);

        /** Stops the phase, as the Stop of the panel (the author closed the window of the step without taking its result). */
        void stop();

        /**
         * Waits for the work of the step; when the phase is stopped meanwhile the future is cancelled and {@link Stopped} thrown.
         *
         * @throws Exception what the future failed with
         */
        <T> T await(CompletableFuture<T> future) throws Exception;

        /** The step changed the part (notes, a new text): the page that generated shows it again, when it is still there. */
        void changed();

        /** The step had nothing to do (the model found no notes, no speech to translate): the panel does not tick it. */
        void skip();

        /**
         * The step waits for the author (a result to review): the panel says so, and its <b>Review</b> runs {@code open} in the page of
         * the click. {@code null} when it does not wait any more.
         */
        void waitForAuthor(Runnable open);

        /**
         * Runs the action in the page that generated ({@code ui.access} and a push), to tell the author something there.
         *
         * @return false when the page is gone or the generation had none (the command bar of another page does)
         */
        boolean accessUI(Consumer<UI> action);
    }
}
