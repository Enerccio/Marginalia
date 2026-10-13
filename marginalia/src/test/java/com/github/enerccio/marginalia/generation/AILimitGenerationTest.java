package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.collections.LimitStrategy;
import com.github.enerccio.marginalia.domain.collections.ResetStrategy;
import com.github.enerccio.marginalia.domain.components.AILimit;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.AILimitService;
import com.github.enerccio.marginalia.domain.service.InferenceErrors;
import com.github.enerccio.marginalia.domain.service.InferenceException;
import com.github.enerccio.marginalia.domain.service.InferenceException.Type;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A generation with a shared AI is refused when the administrator's limit for the user does not allow it, before
 * anything is sent to the provider.
 */
class AILimitGenerationTest extends GenerationTestBase {

    @Autowired
    private AILimitService limitService;

    private User admin;
    private User reader;

    private Manuscript readerManuscript(AILimit limit) throws Exception {
        admin = loginAdmin();
        OpenAICompatible shared = createAI();
        shared.setShared(true);
        ai = (OpenAICompatible) aiService.save(shared);
        reader = createUser();
        limitService.setLimit(ai, reader, limit);
        loginAs(reader);
        return manuscriptService.save(newManuscript());
    }

    @Test
    void unavailableAiIsRefused() throws Exception {
        AILimit limit = new AILimit();
        limit.setStrategy(LimitStrategy.NOT_AVAILABLE);
        Manuscript manuscript = readerManuscript(limit);
        llm.enqueue(MockLLMResponse.text("never sent"));

        GenerationRun run = generate(manuscript, "write");

        assertThat(run.getErrors()).extracting(InferenceErrors::find).extracting(InferenceException::getType)
                .contains(Type.NOT_AVAILABLE);
        assertThat(llm.getCompletionRequests()).isEmpty();
    }

    @Test
    void generationsStopWhenTheLimitIsUsedUp() throws Exception {
        AILimit limit = new AILimit();
        limit.setStrategy(LimitStrategy.TOKEN_LIMIT);
        limit.setResetStrategy(ResetStrategy.DAILY);
        limit.setTotalTokens(100L);
        Manuscript manuscript = readerManuscript(limit);
        llm.enqueue(MockLLMResponse.text("one").usage(80, 30));
        assertThat(generate(manuscript, "one").await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);

        llm.enqueue(MockLLMResponse.text("two").usage(1, 1));
        GenerationRun refused = generate(manuscript, "two");

        assertThat(refused.getErrors()).extracting(InferenceErrors::find).extracting(InferenceException::getType)
                .contains(Type.LIMIT_REACHED);
        assertThat(llm.getCompletionRequests()).hasSize(1);
    }
}
