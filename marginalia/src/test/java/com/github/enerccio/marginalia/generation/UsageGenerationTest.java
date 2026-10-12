package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.model.impl.Usage;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.UsageService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.InferenceCollector;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Generations count what they send to the AI and what it generates.
 */
class UsageGenerationTest extends GenerationTestBase {

    @Autowired
    private UsageService usageService;

    @Test
    void reportedCountsAreCounted() throws Exception {
        Manuscript manuscript = manuscriptService.save(newManuscript());
        llm.enqueue(MockLLMResponse.text("a story").usage(1234, 56));

        assertThat(generate(manuscript, "write").await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);

        Usage usage = usageService.findForUser(ai);
        assertThat(usage.getPrefillTokens()).isEqualTo(1234);
        assertThat(usage.getGeneratedTokens()).isEqualTo(56);
        assertThat(usage.getRequests()).isEqualTo(1);
    }

    @Test
    void theRequestAsksForTheCounts() throws Exception {
        Manuscript manuscript = manuscriptService.save(newManuscript());

        assertThat(generate(manuscript, "write").await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);

        assertThat(llm.lastCompletionRequest().includesUsage()).isTrue();
    }

    @Test
    void ownCountIsUsedWhenTheProviderReportsNone() throws Exception {
        Manuscript manuscript = manuscriptService.save(newManuscript());
        llm.enqueue(MockLLMResponse.text("a story that was told"));

        assertThat(generate(manuscript, "write").await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);

        Usage usage = usageService.findForUser(ai);
        assertThat(usage.getPrefillTokens()).isPositive();
        assertThat(usage.getGeneratedTokens()).isPositive();
        assertThat(usage.getRequests()).isEqualTo(1);
    }

    @Test
    void generationsAddUp() throws Exception {
        Manuscript manuscript = manuscriptService.save(newManuscript());
        llm.enqueue(MockLLMResponse.text("one").usage(100, 10));
        assertThat(generate(manuscript, "one").await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        llm.enqueue(MockLLMResponse.text("two").usage(200, 20));
        assertThat(generate(manuscript, "two").await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);

        Usage usage = usageService.findForUser(ai);
        assertThat(usage.getPrefillTokens()).isEqualTo(300);
        assertThat(usage.getGeneratedTokens()).isEqualTo(30);
        assertThat(usage.getRequests()).isEqualTo(2);
    }

    @Test
    void directInferenceIsCountedToo() throws Exception {
        // what an extension does: it streams from the AI itself, without the generation pipeline
        llm.enqueue(MockLLMResponse.text("hello").usage(11, 2));
        InferenceCollector collector = new InferenceCollector();
        inferenceServices.forAI(ai).stream(List.of(LLMChatMessage.of(LLMRole.USER, "hi")), collector);

        assertThat(collector.await(TIMEOUT)).isEqualTo(InferenceCollector.Outcome.COMPLETED);

        Usage usage = usageService.findForUser(ai);
        assertThat(usage.getPrefillTokens()).isEqualTo(11);
        assertThat(usage.getGeneratedTokens()).isEqualTo(2);
    }

    @Test
    void failedRequestIsNotCounted() throws Exception {
        Manuscript manuscript = manuscriptService.save(newManuscript());
        llm.enqueue(MockLLMResponse.error(400, "bad request"));

        generate(manuscript, "write").await(TIMEOUT);

        assertThat(usageService.findForUser(ai)).isNull();
    }

    @Test
    void usersOfASharedAiAreCountedSeparately() throws Exception {
        User admin = loginAdmin();
        OpenAICompatible shared = createAI();
        shared.setShared(true);
        shared = (OpenAICompatible) aiService.save(shared);

        User reader = loginAs(createUser());
        ai = shared;
        Manuscript manuscript = manuscriptService.save(newManuscript());
        llm.enqueue(MockLLMResponse.text("a story").usage(70, 7));
        assertThat(generate(manuscript, "write").await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);

        assertThat(usageService.findForUser(shared).getPrefillTokens()).isEqualTo(70);

        loginAs(admin);
        assertThat(usageService.findForUser(shared)).as("the owner did not use it").isNull();
        assertThat(usageService.findAllForAI(shared)).extracting(u -> u.getOwner().getId()).containsExactly(reader.getId());
    }
}
