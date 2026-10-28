package com.github.enerccio.marginalia.annotation;

import com.github.enerccio.marginalia.domain.collections.AnnotationSource;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A part that is generated again has a new text, the annotations of the part find their passages in it by their quotes.
 */
class AnnotationGenerationTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private AnnotationService annotationService;

    private Manuscript manuscript;
    private ChatMessage part;
    private Annotation kept;
    private Annotation gone;

    @BeforeEach
    void createPart() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
        llm.enqueue(MockLLMResponse.text("Anna met John at the bar. They talked for hours. Then she left."));
        GenerationRun run = run(GenerationRequest.newMessage());
        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        part = chatMessageService.find(run.getMessage().getId());
        kept = annotate("They talked for hours.");
        gone = annotate("Then she left.");
    }

    private GenerationRun run(GenerationRequest request) throws Exception {
        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(manuscriptService.find(manuscript.getId()), new TurnInput(null, null, null, "go"), request, run);
        run.await(TIMEOUT);
        return run;
    }

    private Annotation annotate(String passage) throws Exception {
        String text = chatMessageService.find(part.getId()).getResponse();
        int start = text.indexOf(passage);
        return annotationService.create(manuscript, part, start, start + passage.length(), "note on " + passage, AnnotationSource.AUTHOR, null, null);
    }

    @Test
    void regeneratedPartKeepsTheAnnotationsWhosePassageIsStillThere() throws Exception {
        llm.enqueue(MockLLMResponse.text("A different opening. They talked for hours. And nothing else happened."));

        GenerationRun run = run(GenerationRequest.regenerate(chatMessageService.find(part.getId())));

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        String text = chatMessageService.find(part.getId()).getResponse();
        Annotation moved = annotationService.find(kept.getId());
        assertThat(moved.getStatus()).isEqualTo(AnnotationStatus.OPEN);
        assertThat(text.substring(moved.getStart(), moved.getEnd())).isEqualTo("They talked for hours.");
        Annotation lost = annotationService.find(gone.getId());
        assertThat(lost.getStatus()).isEqualTo(AnnotationStatus.ORPHANED);
        assertThat(lost.hasRange()).isFalse();
    }

    @Test
    void failedRegenerationLeavesTheAnnotationsAlone() throws Exception {
        llm.enqueue(MockLLMResponse.error(400, "context too long"));

        GenerationRun run = run(GenerationRequest.regenerate(chatMessageService.find(part.getId())));

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.CANCELLED);
        String text = chatMessageService.find(part.getId()).getResponse();
        Annotation unchanged = annotationService.find(kept.getId());
        assertThat(unchanged.getStatus()).isEqualTo(AnnotationStatus.OPEN);
        assertThat(text.substring(unchanged.getStart(), unchanged.getEnd())).isEqualTo("They talked for hours.");
        assertThat(annotationService.find(gone.getId()).getStatus()).isEqualTo(AnnotationStatus.OPEN);
    }

    @Test
    void theNotesMarkedForThePromptAreGivenToTheModelWithTheirParts() throws Exception {
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setTemplate("STORY NOTES:\n{{annotations}}");
        manuscript = manuscriptService.save(loaded);
        annotationService.update(manuscript, kept, null, null, true);
        // not marked, and closed: not given
        annotationService.setStatus(manuscript, gone, AnnotationStatus.RESOLVED);
        annotationService.update(manuscript, gone, null, null, true);
        annotationService.create(manuscript, part, null, null, "Keep it short", AnnotationSource.AUTHOR, null, null);
        Annotation whole = annotationService.create(manuscript, part, null, null, "Whole part, to the model", AnnotationSource.AUTHOR, null, null);
        annotationService.update(manuscript, whole, null, null, true);
        llm.enqueue(MockLLMResponse.text("NEXT"));

        GenerationRun run = run(GenerationRequest.newMessage());

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        String prompt = llm.lastCompletionRequest().getPromptText();
        assertThat(prompt).contains("- \"They talked for hours.\": note on They talked for hours.")
                .contains("(the part beginning \"Anna met John at the bar.")
                .contains("Whole part, to the model")
                .doesNotContain("Keep it short").doesNotContain("note on Then she left.");
    }

    @Test
    void withoutMarkedNotesTheVariableIsEmpty() throws Exception {
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setTemplate("STORY NOTES:[{{annotations}}]");
        manuscript = manuscriptService.save(loaded);
        llm.enqueue(MockLLMResponse.text("NEXT"));

        GenerationRun run = run(GenerationRequest.newMessage());

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(llm.lastCompletionRequest().getPromptText()).contains("STORY NOTES:[]");
    }
}
