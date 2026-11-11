package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The core's steps after a generated part: the automatic annotation and the redraft of the book, without their windows.
 */
class AutoAnnotateStepsTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private AnnotationService annotationService;

    private Manuscript book(boolean accept, boolean redraft) throws Exception {
        Manuscript m = newManuscript();
        m.setAutoAnnotate(true);
        m.setAutoAnnotateAccept(accept);
        m.setAutoAnnotateRedraft(redraft);
        m.setAutoAnnotateAcceptRedraft(true);
        return manuscriptService.save(m);
    }

    private CompletableFuture<Runs.Run> phaseEnd(Manuscript book) {
        CompletableFuture<Runs.Run> ended = new CompletableFuture<>();
        Runs.addEndListener(run -> {
            if (Runs.AFTER_PART.equals(run.getSource()) && Objects.equals(run.getBookId(), book.getId())) {
                ended.complete(run);
            }
        });
        return ended;
    }

    @Test
    void theNotesAreAddedAndThePartRedraftedToAddressThemWithoutAWindow() throws Exception {
        Manuscript book = book(true, true);
        llm.enqueue(MockLLMResponse.text("Anna met John. It rained all day."));
        llm.enqueue(MockLLMResponse.text("{\"findings\": [{\"quote\": \"It rained all day\", \"note\": \"The weather was sunny before\"}]}"));
        llm.enqueue(MockLLMResponse.text("Anna met John. The sun shone all day."));
        CompletableFuture<Runs.Run> ended = phaseEnd(book);

        GenerationRun generation = generate(book, "go");
        Runs.Run phase = ended.get(30, TimeUnit.SECONDS);

        assertThat(phase.getOutcome()).isEqualTo(Runs.Outcome.FINISHED);
        ChatMessage part = chatMessageService.find(generation.getMessage());
        assertThat(part.getResponse()).isEqualTo("Anna met John. The sun shone all day.");
        List<Annotation> notes = annotationService.listForPart(book, part);
        assertThat(notes).hasSize(1);
        assertThat(notes.getFirst().getStatus()).as("the redraft dealt with it").isEqualTo(AnnotationStatus.RESOLVED);
    }

    @Test
    void notesTheBookDoesNotTakeWaitForTheAuthorAndClosingIsAStop() throws Exception {
        Manuscript book = book(false, false);
        llm.enqueue(MockLLMResponse.text("Anna met John. It rained all day."));
        llm.enqueue(MockLLMResponse.text("{\"findings\": [{\"quote\": \"It rained all day\", \"note\": \"The weather was sunny before\"}]}"));
        CompletableFuture<Runs.Run> ended = phaseEnd(book);

        GenerationRun generation = generate(book, "go");
        Runs.Run phase = waitingPhase(book);
        assertThat(phase.getOnOpen()).as("the panel's Review opens the notes").isNotNull();
        phase.stop();
        ended.get(30, TimeUnit.SECONDS);

        assertThat(phase.getOutcome()).isEqualTo(Runs.Outcome.STOPPED);
        assertThat(annotationService.listForPart(book, chatMessageService.find(generation.getMessage()))).isEmpty();
    }

    private static Runs.Run waitingPhase(Manuscript book) throws InterruptedException {
        Instant until = Instant.now().plus(Duration.ofSeconds(30));
        while (Instant.now().isBefore(until)) {
            for (Runs.Run run : Runs.ofBook(book.getId())) {
                if (Runs.AFTER_PART.equals(run.getSource()) && run.isWaitingForUser()) {
                    return run;
                }
            }
            Thread.sleep(50);
        }
        throw new AssertionError("the phase does not wait for the author");
    }
}
