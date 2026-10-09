package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.TurnInput;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A request that fails before any text arrived must not change the story: a new part or swipe is removed, a
 * regenerated part gets its previous content back. Text that already arrived is kept.
 */
class FailedGenerationTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    private Manuscript manuscript;

    private GenerationRun run(GenerationRequest request, String instructions) throws Exception {
        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(manuscriptService.find(manuscript.getId()),
                new TurnInput(null, null, null, instructions), request, run);
        run.await(TIMEOUT);
        return run;
    }

    private ChatMessage part(String text, String instructions) throws Exception {
        llm.enqueue(MockLLMResponse.text(text));
        GenerationRun run = run(GenerationRequest.newMessage(), instructions);
        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        return chatMessageService.find(run.getMessage().getId());
    }

    private ChatMessage activeLeaf() throws Exception {
        return chatMessageService.find(manuscriptService.find(manuscript.getId()).getActiveLeaf());
    }

    private List<String> story() throws Exception {
        ChatMessage leaf = activeLeaf();
        if (leaf == null) {
            return List.of();
        }
        return chatMessageService.getBranchFromLeaf(leaf).stream().map(ChatMessage::getResponse).toList();
    }

    private void assertFailed(GenerationRun run) throws Exception {
        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.CANCELLED);
        assertThat(run.getErrors()).isNotEmpty();
    }

    private void setUpManuscript() throws Exception {
        Manuscript m = newManuscript();
        // local variable, stored in the part's attributes
        m.setUserPrompt("{{.turn++}}{{instructions}}");
        manuscript = manuscriptService.save(m);
    }

    @Test
    void failedFirstPartLeavesNoPart() throws Exception {
        setUpManuscript();
        llm.enqueue(MockLLMResponse.error(401, "wrong key"));

        assertFailed(run(GenerationRequest.newMessage(), "write"));

        assertThat(activeLeaf()).isNull();
        assertThat(chatMessageService.hasAnyMessages(manuscriptService.find(manuscript.getId()))).isFalse();
    }

    @Test
    void failedNewPartIsRemoved() throws Exception {
        setUpManuscript();
        ChatMessage first = part("FIRST", "one");
        llm.enqueue(MockLLMResponse.error(401, "wrong key"));

        GenerationRun run = run(GenerationRequest.newMessage(), "two");

        assertFailed(run);
        assertThat(run.getMessage()).isNull();
        assertThat(story()).containsExactly("FIRST");
        assertThat(activeLeaf().getId()).isEqualTo(first.getId());
        assertThat(chatMessageService.getAllMessages(manuscriptService.find(manuscript.getId()))).hasSize(1);
    }

    @Test
    void failedSwipeIsRemovedAndPreviousVersionIsActive() throws Exception {
        setUpManuscript();
        part("FIRST", "one");
        ChatMessage second = part("SECOND", "two");
        llm.enqueue(MockLLMResponse.error(400, "context too long"));

        GenerationRun run = run(GenerationRequest.newSwipe(second), "two");

        assertFailed(run);
        assertThat(run.getMessage().getId()).isEqualTo(second.getId());
        assertThat(story()).containsExactly("FIRST", "SECOND");
        assertThat(chatMessageService.getSwipesForMessage(second)).extracting(ChatMessage::getId).containsExactly(second.getId());
    }

    @Test
    void failedRegenerateRestoresThePart() throws Exception {
        setUpManuscript();
        part("FIRST", "one");
        llm.enqueue(MockLLMResponse.text("SECOND").withReasoning("thinking"));
        GenerationRun secondRun = run(GenerationRequest.newMessage(), "two");
        assertThat(secondRun.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);

        ChatMessage second = chatMessageService.find(secondRun.getMessage().getId());
        second.setEdited(true);
        second.getAttributes().addProperty("extension", "keep me");
        second = chatMessageService.save(second);
        ChatMessage before = chatMessageService.find(second.getId());
        JsonObject attributesBefore = before.getAttributes().deepCopy();

        // a different turn input must not stay in the part (non-retried errors, the client retries 429 and 5xx)
        llm.enqueue(MockLLMResponse.error(400, "context too long"));
        GenerationRun run = run(GenerationRequest.regenerate(second), "changed instructions");

        assertFailed(run);
        ChatMessage after = chatMessageService.find(second.getId());
        assertThat(after.getResponse()).isEqualTo("SECOND");
        assertThat(after.getResponseReasoning()).isEqualTo(before.getResponseReasoning());
        assertThat(after.getInstructions()).isEqualTo("two");
        assertThat(after.getTokenCount()).isEqualTo(before.getTokenCount());
        assertThat(after.getTokenReasoningCount()).isEqualTo(before.getTokenReasoningCount());
        assertThat(after.getWordCount()).isEqualTo(before.getWordCount());
        assertThat(after.getPromptTokens()).isEqualTo(before.getPromptTokens());
        assertThat(after.getBuiltPrompt()).isEqualTo(before.getBuiltPrompt());
        assertThat(after.getRequest()).isEqualTo(before.getRequest());
        assertThat(after.getTtft()).isEqualTo(before.getTtft());
        assertThat(after.getReasoningEnd()).isEqualTo(before.getReasoningEnd());
        assertThat(after.isEdited()).isTrue();
        assertThat(after.getAttributes()).isEqualTo(attributesBefore);
        assertThat(story()).containsExactly("FIRST", "SECOND");

        // the restored part works as before - a successful regenerate replaces the text instead of appending to it
        llm.enqueue(MockLLMResponse.text("THIRD"));
        GenerationRun again = run(GenerationRequest.regenerate(after), "two");
        assertThat(again.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(chatMessageService.find(second.getId()).getResponse()).isEqualTo("THIRD");
        assertThat(story()).containsExactly("FIRST", "THIRD");
    }

    @Test
    void textThatArrivedIsKept() throws Exception {
        setUpManuscript();
        part("FIRST", "one");
        llm.enqueue(MockLLMResponse.chunks("partial ", "text ", "never sent").disconnectAfter(2));

        GenerationRun run = run(GenerationRequest.newMessage(), "two");

        assertThat(run.await(TIMEOUT)).isNotNull();
        assertThat(story()).hasSize(2);
        assertThat(story().getLast()).startsWith("partial");
    }
}
