package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.model.ImageAttachment;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.TurnInput;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The images of a part belong to the part, not to its text: they stay when the text is regenerated, whether the
 * request succeeds or fails, and the model never sees them.
 */
class ImageAttachmentGenerationTest extends GenerationTestBase {
    private static final List<ImageAttachment> IMAGES = List.of(new ImageAttachment("uuid-1", "map"), new ImageAttachment("uuid-2", ""));

    @Autowired
    private ChatMessageService chatMessageService;

    private Manuscript manuscript;

    private GenerationRun run(GenerationRequest request) throws Exception {
        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(manuscriptService.find(manuscript.getId()),
                new TurnInput(null, null, null, "write"), request, run);
        run.await(TIMEOUT);
        return run;
    }

    private ChatMessage partWithImages() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
        llm.enqueue(MockLLMResponse.text("ORIGINAL"));
        GenerationRun run = run(GenerationRequest.newMessage());
        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        ChatMessage part = chatMessageService.find(run.getMessage().getId());
        part.setImages(IMAGES);
        return chatMessageService.save(part);
    }

    @Test
    void regenerateKeepsTheImages() throws Exception {
        ChatMessage part = partWithImages();
        llm.enqueue(MockLLMResponse.text("REGENERATED"));

        GenerationRun run = run(GenerationRequest.regenerate(part));

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        ChatMessage after = chatMessageService.find(part.getId());
        assertThat(after.getId()).isEqualTo(part.getId());
        assertThat(after.getResponse()).isEqualTo("REGENERATED");
        assertThat(after.getImages()).containsExactlyElementsOf(IMAGES);
    }

    @Test
    void failedRegenerateKeepsTheImages() throws Exception {
        ChatMessage part = partWithImages();
        llm.enqueue(MockLLMResponse.error(400, "context too long"));

        GenerationRun run = run(GenerationRequest.regenerate(part));

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.CANCELLED);
        ChatMessage after = chatMessageService.find(part.getId());
        assertThat(after.getResponse()).isEqualTo("ORIGINAL");
        assertThat(after.getImages()).containsExactlyElementsOf(IMAGES);
    }

    @Test
    void imagesAreNotSentToTheModel() throws Exception {
        ChatMessage part = partWithImages();
        llm.enqueue(MockLLMResponse.text("NEXT"));

        GenerationRun run = run(GenerationRequest.newMessage());

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        ChatMessage next = chatMessageService.find(run.getMessage().getId());
        assertThat(next.getBuiltPrompt()).contains("ORIGINAL").doesNotContain("uuid-1").doesNotContain("map");
        assertThat(next.getImages()).isEmpty();
        assertThat(chatMessageService.find(part.getId()).getImages()).hasSize(2);
    }
}
