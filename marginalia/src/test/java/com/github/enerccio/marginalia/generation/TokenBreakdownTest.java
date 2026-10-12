package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.components.PromptTokenBreakdown;
import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The raw token counts of the prompt are stored with the generated part; parts without them read as null.
 */
class TokenBreakdownTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    private ChatMessage generatePart(Manuscript manuscript, String text) throws Exception {
        llm.enqueue(MockLLMResponse.text(text));
        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(manuscriptService.find(manuscript.getId()),
                new TurnInput(null, null, null, "go"), GenerationRequest.newMessage(), run);
        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        return chatMessageService.find(run.getMessage().getId());
    }

    @Test
    void generatedPartHasSectionsAndPayloadMessages() throws Exception {
        Manuscript manuscript = manuscriptService.save(newManuscript());
        generatePart(manuscript, "FIRST");
        ChatMessage second = generatePart(manuscript, "SECOND");

        PromptTokenBreakdown breakdown = second.getTokenBreakdown();

        assertThat(breakdown).isNotNull();
        assertThat(breakdown.getHistoryMessages()).isEqualTo(1);
        assertThat(breakdown.getHistoryTokens()).isPositive();
        assertThat(breakdown.getUserPromptTokens()).isPositive();
        // system + (user, assistant) of the first part + user prompt
        assertThat(breakdown.getMessages()).hasSize(4);
        assertThat(breakdown.getMessages().getFirst().role()).isEqualTo("SYSTEM");
        assertThat(breakdown.getMessages().getLast().role()).isEqualTo("USER");
        assertThat(breakdown.getPayloadTokens()).isPositive();
        assertThat(breakdown.getCutMessages()).isZero();
    }

    @Test
    void reservedTokensAndCutStoryAreRecorded() throws Exception {
        Manuscript manuscript = manuscriptService.save(newManuscript());
        generatePart(manuscript, "FIRST");
        generatePart(manuscript, "SECOND");
        int limit = TokenLimits.promptTokens(ai, protocol);
        // the rest of the prompt fits, no story part does
        onEvent(Events.BEFORE_PREPARE_CONTENT, manuscript, e -> e.getPrePromptData().setReservedTokens(limit - 300));

        ChatMessage third = generatePart(manuscript, "THIRD");

        PromptTokenBreakdown breakdown = third.getTokenBreakdown();
        assertThat(breakdown.getReservedTokens()).isEqualTo(limit - 300);
        assertThat(breakdown.getPromptLimitTokens()).isEqualTo(limit);
        assertThat(breakdown.getContextTokens()).isEqualTo(TokenLimits.contextTokens(ai, protocol));
        assertThat(breakdown.getHistoryMessages()).isZero();
        assertThat(breakdown.getCutMessages()).isEqualTo(2);
        assertThat(breakdown.getCutTokens()).isPositive();
    }

    @Test
    void partWithoutBreakdownReadsAsNull() throws Exception {
        Manuscript manuscript = manuscriptService.save(newManuscript());
        ChatMessage message = generatePart(manuscript, "FIRST");

        message.setTokenBreakdown(null);

        assertThat(message.getTokenBreakdown()).isNull();
        assertThat(PromptTokenBreakdown.fromJson(null)).isNull();
        assertThat(PromptTokenBreakdown.fromJson("")).isNull();
        assertThat(PromptTokenBreakdown.fromJson("{not json")).isNull();
        assertThat(PromptTokenBreakdown.fromJson("{}").getMessages()).isEmpty();
    }
}
