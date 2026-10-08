package com.github.enerccio.marginalia.test;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the test infrastructure itself: context, isolated database, session user and the mock LLM wiring.
 */
class TestBaseSmokeTest extends MarginaliaTestBase {

    @Autowired
    private Configuration configuration;

    @Test
    void usesIsolatedApplicationFolder() {
        File home = new File(System.getProperty("user.home"), ".marginalia");
        assertThat(configuration.getFolder().getAbsoluteFile()).isNotEqualTo(home.getAbsoluteFile());
        assertThat(configuration.getDatabaseFile()).exists();
        assertThat(configuration.getDatabaseFile().toPath()).startsWith(configuration.getFolder().toPath());
    }

    @Test
    void createsAndLogsInUser() throws Exception {
        User user = login();

        assertThat(currentUser.getId()).isEqualTo(user.getId());
        assertThat(userService.authenticate(user.getLogin(), DEFAULT_PASSWORD)).isTrue();
        assertThat(userService.authenticate(user.getLogin(), "wrong")).isFalse();
    }

    @Test
    void createdAIIsOwnedByCurrentUser() throws Exception {
        User user = login();
        OpenAICompatible ai = createAI();

        assertThat(ai.getOwner().getId()).isEqualTo(user.getId());
        assertThat(aiService.findAll()).extracting(AI::getId).contains(ai.getId());
    }

    @Test
    void streamsFromMockLLMThroughInferenceService() throws Exception {
        login();
        llm.enqueue(MockLLMResponse.chunks("Once ", "upon ", "a time").withReasoning("plotting"));
        InferenceService inference = inferenceServices.forAI(createAI());

        InferenceCollector collector = new InferenceCollector();
        inference.stream(List.of(
                LLMChatMessage.of(LLMRole.SYSTEM, "You are a storyteller."),
                LLMChatMessage.of(LLMRole.USER, "Tell me a story.")
        ), collector);

        assertThat(collector.await(Duration.ofSeconds(10))).isEqualTo(InferenceCollector.Outcome.COMPLETED);
        assertThat(collector.getResponse()).isEqualTo("Once upon a time");
        assertThat(collector.getReasoning()).isEqualTo("plotting");
        assertThat(llm.lastCompletionRequest().getMessages(LLMRole.USER.name().toLowerCase()))
                .containsExactly("Tell me a story.");
    }

    @Test
    void listsModelsOfMockLLM() throws Exception {
        login();
        llm.models("alpha", "beta");

        assertThat(inferenceServices.forAI(createAI()).getModels()).containsExactly("alpha", "beta");
    }
}
