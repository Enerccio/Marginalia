package com.github.enerccio.marginalia.domain.service.run;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A generation is a run of its book: one at a time, its new part is not deleted under it, and it gives everything back.
 */
class GenerationRunsTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Test
    void aSecondGenerationOfTheBookIsRefusedWhileOneRunsAndTheNewPartIsHeld() throws Exception {
        login();
        Manuscript book = manuscriptService.save(newManuscript());
        CountDownLatch inInference = new CountDownLatch(1);
        CountDownLatch goOn = new CountDownLatch(1);
        AtomicReference<ChatMessage> written = new AtomicReference<>();
        onEvent(Events.BEFORE_INFERENCE, book, event -> {
            written.set(event.getMessage());
            inInference.countDown();
            try {
                goOn.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        GenerationRun first = new GenerationRun();
        storyGenerationService.generateNextTurn(manuscriptService.find(book.getId()), new TurnInput(null, null, null, "Go on"),
                GenerationRequest.newMessage(), first);
        assertThat(inInference.await(30, TimeUnit.SECONDS)).isTrue();

        assertThat(Runs.ofBook(book.getId())).singleElement().satisfies(run -> {
            assertThat(run.getSource()).isEqualTo(Runs.GENERATION);
            assertThat(run.marksParts()).as("the page shows its own generation").isFalse();
        });
        assertThatThrownBy(() -> storyGenerationService.generateNextTurn(manuscriptService.find(book.getId()),
                new TurnInput(null, null, null, "Again"), GenerationRequest.newMessage(), new GenerationRun()))
                .isInstanceOf(Runs.HeldException.class);
        assertThatThrownBy(() -> chatMessageService.delete(chatMessageService.find(written.get().getId()), false))
                .isInstanceOf(Runs.HeldException.class);
        assertThatThrownBy(() -> manuscriptService.delete(manuscriptService.find(book.getId()), false))
                .isInstanceOf(Runs.HeldException.class);

        goOn.countDown();
        assertThat(first.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(Runs.ofBook(book.getId())).isEmpty();

        // the next one right after the end, as a batch of commands does
        assertThat(generate(book, "Once more").await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(Runs.ofBook(book.getId())).isEmpty();
    }

    @Test
    void fromItsInferenceOnTheGenerationHoldsOnlyItsPartAndTheBranchAboveIt() throws Exception {
        login();
        Manuscript book = manuscriptService.save(newManuscript());
        ChatMessage earlier = chatMessageService.createPart(book, null, "It was night.", new TurnInput(null, null, null, "Begin"));
        CountDownLatch inInference = new CountDownLatch(1);
        CountDownLatch goOn = new CountDownLatch(1);
        AtomicReference<ChatMessage> written = new AtomicReference<>();
        AtomicReference<Runs.Stage> stage = new AtomicReference<>();
        onEvent(Events.BEFORE_INFERENCE, book, event -> {
            written.set(event.getMessage());
            Runs.Run run = Runs.generationOf(book.getId());
            stage.set(run == null ? null : run.getStage());
            inInference.countDown();
            try {
                goOn.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(manuscriptService.find(book.getId()), new TurnInput(null, null, null, "Go on"),
                GenerationRequest.newMessage(), run);
        assertThat(inInference.await(30, TimeUnit.SECONDS)).isTrue();
        try {
            assertThat(stage.get()).isEqualTo(Runs.Stage.INFERENCE);
            Runs.Run generation = Runs.generationOf(book.getId());
            assertThat(generation.getWrittenPart()).isEqualTo(written.get().getId());
            assertThat(generation.getParts(Runs.Mode.STRUCTURE)).containsExactly(earlier.getId());

            assertThatThrownBy(() -> chatMessageService.updateResponse(manuscriptService.find(book.getId()),
                    chatMessageService.find(written.get().getId()), "Mine")).isInstanceOf(Runs.HeldException.class);
            assertThatThrownBy(() -> chatMessageService.delete(chatMessageService.find(earlier.getId()), false))
                    .isInstanceOf(Runs.HeldException.class);
            // the text of the earlier part and the settings of the book are free
            chatMessageService.updateResponse(manuscriptService.find(book.getId()), chatMessageService.find(earlier.getId()), "It was a dark night.");
            Manuscript renamed = manuscriptService.find(book.getId());
            renamed.setName("Renamed while generating");
            manuscriptService.save(renamed);
        } finally {
            goOn.countDown();
        }

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(Runs.ofBook(book.getId())).isEmpty();
        assertThat(chatMessageService.find(earlier.getId()).getResponse()).isEqualTo("It was a dark night.");
        assertThat(manuscriptService.find(book.getId()).getName()).isEqualTo("Renamed while generating");
        assertThat(manuscriptService.find(book.getId()).getActiveLeaf().getId()).isEqualTo(written.get().getId());
    }

    @Test
    void aStoppedGenerationGivesTheBookBackAndTakesItsEmptyPartAway() throws Exception {
        login();
        Manuscript book = manuscriptService.save(newManuscript());
        onEvent(Events.BEFORE_INFERENCE, book, event -> event.getCancellationToken().cancel());

        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(manuscriptService.find(book.getId()), new TurnInput(null, null, null, "Go on"),
                GenerationRequest.newMessage(), run);

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.CANCELLED);
        assertThat(Runs.ofBook(book.getId())).isEmpty();
        assertThat(chatMessageService.hasAnyMessages(manuscriptService.find(book.getId()))).isFalse();
    }
}
