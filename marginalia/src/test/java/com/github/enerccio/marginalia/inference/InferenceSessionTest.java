package com.github.enerccio.marginalia.inference;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.service.InferenceSession;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.InferenceCollector;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The header {@code X-Session-ID}: what the requests of one kind of work for one user have in common, and that a request
 * outside of any kind has none.
 */
class InferenceSessionTest extends GenerationTestBase {

    private static final List<LLMChatMessage> PAYLOAD = List.of(LLMChatMessage.of(LLMRole.USER, "Hello."));

    @Autowired
    private InferenceServices inferenceServices;

    private String ask(String kind, String scope) throws Exception {
        InferenceService service = inferenceServices.forAIOrThrow(ai);
        InferenceCollector collector = new InferenceCollector();
        if (kind == null) {
            service.stream(PAYLOAD, null, collector);
        } else {
            try (InferenceSession _ = InferenceSession.open(kind, scope)) {
                service.stream(PAYLOAD, null, collector);
            }
        }
        collector.await(Duration.ofSeconds(10));
        return llm.lastCompletionRequest().getHeader(InferenceSession.HEADER);
    }

    @Test
    void sessionIdIsAnOpaqueHashOfTheUserAndTheKind() throws Exception {
        String annotate = ask(InferenceSession.ANNOTATE, null);

        assertThat(annotate).matches("[0-9a-f]{32}");
        assertThat(annotate).doesNotContain("annotate");
        assertThat(ask(InferenceSession.ANNOTATE, null)).isEqualTo(annotate);
        assertThat(ask(InferenceSession.REDRAFT, null)).isNotEqualTo(annotate);
        // a plugin names its own kind
        assertThat(ask("reviewer", null)).matches("[0-9a-f]{32}").isNotEqualTo(annotate);
    }

    @Test
    void scopeTellsApartParallelRunsOfOneKind() throws Exception {
        String first = ask(InferenceSession.AGENT, "1");

        assertThat(ask(InferenceSession.AGENT, "1")).isEqualTo(first);
        assertThat(ask(InferenceSession.AGENT, "2")).isNotEqualTo(first);
        assertThat(ask(InferenceSession.AGENT, null)).isNotEqualTo(first);
    }

    @Test
    void otherUserHasOtherSessionId() throws Exception {
        String mine = ask(InferenceSession.ANNOTATE, null);
        login();

        assertThat(ask(InferenceSession.ANNOTATE, null)).isNotEqualTo(mine);
    }

    @Test
    void noKindNoHeader() throws Exception {
        assertThat(ask(null, null)).isNull();
    }

    @Test
    void scopeEndsWithTheTryBlock() throws Exception {
        ask(InferenceSession.ANNOTATE, null);

        assertThat(InferenceSession.current()).isNull();
        try (InferenceSession _ = InferenceSession.open("outer")) {
            try (InferenceSession _ = InferenceSession.open("inner", "run")) {
                assertThat(InferenceSession.current().key()).isEqualTo("inner/run");
            }
            assertThat(InferenceSession.current().key()).isEqualTo("outer");
        }
        assertThat(InferenceSession.current()).isNull();
    }

    @Test
    void newPartRegenerationAndSwipeShareTheSessionIdOfTheGeneration() throws Exception {
        Manuscript manuscript = manuscriptService.save(newManuscript());
        var first = generate(manuscript, "Write the first part");
        String id = llm.lastCompletionRequest().getHeader(InferenceSession.HEADER);
        assertThat(id).matches("[0-9a-f]{32}");

        GenerationRun again = new GenerationRun();
        storyGenerationService.generateNextTurn(manuscriptService.find(manuscript.getId()),
                new TurnInput(null, null, null, null), GenerationRequest.regenerate(first.getMessage()), again);
        again.await(TIMEOUT);
        assertThat(llm.lastCompletionRequest().getHeader(InferenceSession.HEADER)).isEqualTo(id);

        // another book of the same user: the same cache is meant
        Manuscript other = manuscriptService.save(newManuscript());
        generate(other, "Write another story");
        assertThat(llm.lastCompletionRequest().getHeader(InferenceSession.HEADER)).isEqualTo(id);
        assertThat(llm.getCompletionRequests()).extracting(r -> r.getHeader(InferenceSession.HEADER)).doesNotContainNull();
    }
}
