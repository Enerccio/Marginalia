package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.model.WatchedPhrase;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.domain.service.PhraseWatchService;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The step of the phrase watch list after a generated part: a book that scans its parts gets the hits annotated, one that does not
 * gets nothing.
 */
class PhraseWatchStepTest extends GenerationTestBase {

    @Autowired
    private PhraseWatchService phraseWatchService;

    @Autowired
    private AnnotationService annotationService;

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

    @Test
    void theHitsOfAGeneratedPartAreAnnotatedWhenTheBookScansItsParts() throws Exception {
        llm.fallback(MockLLMResponse.text("The tapestry of fate unfurled. A shiver ran down her spine."));
        Manuscript book = manuscriptService.save(newManuscript());
        book = phraseWatchService.setForBook(book, List.of(new WatchedPhrase("tapestry of fate", false, null),
                new WatchedPhrase("shiver\\s+ran", true, null)), true);
        CompletableFuture<Runs.Run> ended = phaseEnd(book);

        GenerationRun run = generate(book, "go");
        Runs.Run phase = ended.get(30, TimeUnit.SECONDS);

        assertThat(phase.getOutcome()).isEqualTo(Runs.Outcome.FINISHED);
        List<Annotation> annotations = annotationService.listForPart(book, run.getMessage());
        assertThat(annotations).extracting(Annotation::getQuote).containsExactly("tapestry of fate", "shiver ran");
        assertThat(annotations).allMatch(annotation -> annotation.getSource() == AnnotationSource.PHRASE);
    }

    @Test
    void aBookThatDoesNotScanItsPartsGetsNoAnnotations() throws Exception {
        llm.fallback(MockLLMResponse.text("The tapestry of fate unfurled."));
        Manuscript book = manuscriptService.save(newManuscript());
        book = phraseWatchService.setForBook(book, List.of(new WatchedPhrase("tapestry", false, null)), false);

        GenerationRun run = generate(book, "go");
        // nothing follows the part: wait a little for a phase that would run
        Thread.sleep(500);

        assertThat(annotationService.listForPart(book, run.getMessage())).isEmpty();
    }
}
