package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import com.github.enerccio.marginalia.test.llm.MockLLMScenario;
import com.github.enerccio.marginalia.test.llm.MockLLMServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A generation can run with another AI and protocol than the book's (agents do that) without the book being touched.
 */
class GenerationOptionsTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    private MockLLMScenario otherLlm;

    @AfterEach
    void closeOther() {
        if (otherLlm != null) {
            otherLlm.close();
        }
    }

    private Protocol otherProtocol() throws Exception {
        ChatCompletionProtocol p = new ChatCompletionProtocol();
        p.setName("other protocol");
        p.setProtocolType(ProtocolType.CHAT_COMPLETION);
        p.setMaxTokens(16000);
        p.setReplyTokens(500);
        return protocolService.save(p);
    }

    @Test
    void optionsChooseModelWithoutChangingTheBook() throws Exception {
        Manuscript manuscript = manuscriptService.save(newManuscript());
        otherLlm = MockLLMServer.shared().scenario();
        otherLlm.fallback(MockLLMResponse.text("from the other"));
        OpenAICompatible otherAi = createAI(otherLlm);
        Protocol other = otherProtocol();

        AtomicReference<Object> eventAi = new AtomicReference<>();
        AtomicReference<Object> eventProtocol = new AtomicReference<>();
        onEvent(Events.AFTER_PREPARE_PAYLOAD, manuscript, e -> {
            eventAi.set(e.getAi().getId());
            eventProtocol.set(e.getProtocol().getId());
        });

        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(manuscriptService.find(manuscript.getId()),
                new TurnInput(null, null, null, "go"), GenerationRequest.newMessage(), run,
                GenerationOptions.defaults().withModel(otherAi, other));
        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);

        ChatMessage part = chatMessageService.find(run.getMessage().getId());
        assertThat(part.getResponse()).isEqualTo("from the other");
        assertThat(part.getModelUsed()).isEqualTo(otherAi.getName());
        assertThat(part.getProtocolUsed()).isEqualTo(other.getName());
        assertThat(otherLlm.getCompletionRequests()).hasSize(1);
        assertThat(llm.getCompletionRequests()).isEmpty();
        assertThat(eventAi.get()).isEqualTo(otherAi.getId());
        assertThat(eventProtocol.get()).isEqualTo(other.getId());

        Manuscript after = manuscriptService.find(manuscript.getId());
        assertThat(after.getAi().getId()).isEqualTo(ai.getId());
        assertThat(after.getProtocol().getId()).isEqualTo(protocol.getId());
    }

    @Test
    void defaultsUseTheBooksModel() throws Exception {
        Manuscript manuscript = manuscriptService.save(newManuscript());

        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(manuscriptService.find(manuscript.getId()),
                new TurnInput(null, null, null, "go"), GenerationRequest.newMessage(), run, GenerationOptions.defaults());

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(chatMessageService.find(run.getMessage().getId()).getModelUsed()).isEqualTo(ai.getName());
        assertThat(llm.getCompletionRequests()).hasSize(1);
    }
}
