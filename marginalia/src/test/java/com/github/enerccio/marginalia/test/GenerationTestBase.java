package com.github.enerccio.marginalia.test;

import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.model.impl.ChatCompletionProtocol;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import com.github.enerccio.marginalia.domain.service.StoryGenerationService;
import com.github.enerccio.marginalia.domain.service.TurnInput;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.Registration;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Runs real generations (all pipeline steps) against the mock LLM.
 * <p>
 * A logged in user, an AI pointing to {@link #llm} and a protocol are ready before each test; the mock answers every
 * completion with {@code "generated"} unless the test programs something else. Use {@link #onEvent} to look at the
 * generation state at a given event.
 */
public abstract class GenerationTestBase extends MarginaliaTestBase {

    public static final Duration TIMEOUT = Duration.ofSeconds(30);

    @Autowired
    protected StoryGenerationService storyGenerationService;

    @Autowired
    protected ManuscriptService manuscriptService;

    @Autowired
    protected ProtocolService protocolService;

    protected OpenAICompatible ai;
    protected Protocol protocol;

    private final List<Registration> registrations = new ArrayList<>();

    @BeforeEach
    void setUpGeneration() throws Exception {
        login();
        ai = createAI();
        ChatCompletionProtocol p = new ChatCompletionProtocol();
        p.setName("test protocol");
        p.setProtocolType(ProtocolType.CHAT_COMPLETION);
        p.setMaxTokens(32000);
        p.setReplyTokens(1000);
        protocol = protocolService.save(p);
        llm.fallback(MockLLMResponse.text("generated"));
    }

    @AfterEach
    void unregisterListeners() {
        registrations.forEach(Registration::unregister);
        registrations.clear();
    }

    /**
     * Manuscript with the test AI and protocol and minimal prompts: the user prompt is just the instructions, the
     * master (system) template just the activated lore, so tests see exactly what the generation produced.
     */
    protected Manuscript newManuscript() {
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript.setAi(ai);
        manuscript.setProtocol(protocol);
        manuscript.setUserPrompt("{{instructions}}");
        manuscript.setTemplate("{{backgroundLore}}");
        return manuscript;
    }

    /**
     * Calls {@code action} whenever a generation of {@code manuscript} reaches {@code event} (listeners are global,
     * other generations are ignored). Registered for the current test only.
     */
    protected void onEvent(Events event, Manuscript manuscript, Consumer<GenerationControllerEvent> action) {
        registrations.add(storyGenerationService.addEventListener(event, (e, chain) -> {
            try {
                if (e.getManuscript() != null && Objects.equals(e.getManuscript().getId(), manuscript.getId())) {
                    action.accept(e);
                }
            } finally {
                chain.next();
            }
        }));
    }

    protected GenerationRun generate(Manuscript manuscript, TurnInput input) throws Exception {
        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(manuscriptService.find(manuscript.getId()), input,
                GenerationRequest.newMessage(), run);
        run.await(TIMEOUT);
        return run;
    }

    protected GenerationRun generate(Manuscript manuscript, String instructions) throws Exception {
        return generate(manuscript, new TurnInput(null, null, null, instructions));
    }
}
