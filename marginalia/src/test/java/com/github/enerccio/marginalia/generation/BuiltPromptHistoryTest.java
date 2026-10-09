package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.TurnInput;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The built prompt carries the story so far: every previous part for a new part, everything but the replaced part for
 * a swipe or regenerate.
 */
class BuiltPromptHistoryTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    private Manuscript manuscript;

    private ChatMessage generate(GenerationRequest request, String text) throws Exception {
        llm.enqueue(MockLLMResponse.text(text));
        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(manuscriptService.find(manuscript.getId()),
                new TurnInput(null, null, null, "go"), request, run);
        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        return chatMessageService.find(run.getMessage().getId());
    }

    private static List<String> assistantMessages(ChatMessage message) {
        List<String> result = new ArrayList<>();
        for (JsonElement element : new Gson().fromJson(message.getBuiltPrompt(), JsonArray.class)) {
            String role = element.getAsJsonObject().get("role").getAsString();
            if ("assistant".equalsIgnoreCase(role)) {
                result.add(element.getAsJsonObject().get("content").getAsString());
            }
        }
        return result;
    }

    private void setUpStory() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
    }

    @Test
    void newPartHasAllPreviousParts() throws Exception {
        setUpStory();
        generate(GenerationRequest.newMessage(), "FIRST");
        generate(GenerationRequest.newMessage(), "SECOND");
        ChatMessage third = generate(GenerationRequest.newMessage(), "THIRD");

        assertThat(assistantMessages(third)).containsExactly("FIRST", "SECOND");
    }

    @Test
    void swipeHasPreviousPartsWithoutTheSwipedOne() throws Exception {
        setUpStory();
        generate(GenerationRequest.newMessage(), "FIRST");
        generate(GenerationRequest.newMessage(), "SECOND");
        ChatMessage third = generate(GenerationRequest.newMessage(), "THIRD");

        ChatMessage swiped = generate(GenerationRequest.newSwipe(third), "THIRD-B");

        assertThat(assistantMessages(swiped)).containsExactly("FIRST", "SECOND");
    }

    @Test
    void regenerateHasPreviousPartsWithoutTheRegeneratedOne() throws Exception {
        setUpStory();
        generate(GenerationRequest.newMessage(), "FIRST");
        generate(GenerationRequest.newMessage(), "SECOND");
        ChatMessage third = generate(GenerationRequest.newMessage(), "THIRD");

        ChatMessage regenerated = generate(GenerationRequest.regenerate(third), "THIRD-B");

        assertThat(assistantMessages(regenerated)).containsExactly("FIRST", "SECOND");
    }
}
