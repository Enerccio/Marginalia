package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.model.impl.ChatCompletionProtocol;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a generation sends to the model: protocol sampling parameters, response limit (protocol, falling back to the
 * AI), context limit with room reserved for the response, and the jailbreak prompt.
 */
class GenerationRequestTest extends GenerationTestBase {

    @Autowired
    private Localization loc;

    private Protocol protocol(Consumer<Protocol> setup) throws Exception {
        ChatCompletionProtocol p = new ChatCompletionProtocol();
        p.setName(uniqueName("protocol"));
        p.setProtocolType(ProtocolType.CHAT_COMPLETION);
        setup.accept(p);
        return protocolService.save(p);
    }

    private Manuscript manuscript(Protocol protocol) throws Exception {
        Manuscript manuscript = newManuscript();
        manuscript.setProtocol(protocol);
        return manuscriptService.save(manuscript);
    }

    private JsonObject completedRequestBody(Manuscript manuscript) throws Exception {
        GenerationRun run = generate(manuscript, "write");
        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(run.getErrors()).isEmpty();
        assertThat(run.getSimpleErrors()).isEmpty();
        return llm.lastCompletionRequest().getBody();
    }

    @Test
    void enabledSamplingParametersAreSent() throws Exception {
        Protocol p = protocol(x -> {
            x.setTemperatureEnabled(true);
            x.setTemperature(0.7);
            x.setTopPEnabled(true);
            x.setTopP(0.9);
            x.setFrequencyPenaltyEnabled(true);
            x.setFrequencyPenalty(0.3);
            x.setPresencePenaltyEnabled(false);
            x.setPresencePenalty(0.5);
        });

        JsonObject body = completedRequestBody(manuscript(p));

        assertThat(body.get("temperature").getAsDouble()).isEqualTo(0.7);
        assertThat(body.get("top_p").getAsDouble()).isEqualTo(0.9);
        assertThat(body.get("frequency_penalty").getAsDouble()).isEqualTo(0.3);
        assertThat(body.has("presence_penalty")).as("disabled parameter is not sent").isFalse();
    }

    @Test
    void protocolReplyTokensOverrideAi() throws Exception {
        JsonObject body = completedRequestBody(manuscript(protocol(x -> x.setReplyTokens(777))));

        assertThat(body.get("max_completion_tokens").getAsInt()).isEqualTo(777);
    }

    @Test
    void protocolWithoutLimitsFallsBackToAi() throws Exception {
        JsonObject body = completedRequestBody(manuscript(protocol(x -> {
            x.setMaxTokens(null);
            x.setReplyTokens(null);
        })));

        assertThat(body.get("max_completion_tokens").getAsInt()).isEqualTo(ai.getMaxCompletionTokens());
        assertThat(body.has("temperature")).isFalse();
    }

    @Test
    void responseTokensAreReservedFromContext() throws Exception {
        ai.setMaxContext(1000);
        ai.setMaxCompletionTokens(950);
        ai = (OpenAICompatible) aiService.save(ai);

        // 1000 - 950 leaves no room for the prompt
        GenerationRun run = generate(manuscript(protocol(x -> x.setMaxTokens(null))), "write");
        run.await(TIMEOUT);

        assertThat(run.getSimpleErrors()).containsExactly(loc.getValue(L.ERROR_CONTEXT_INSUFFICIENT));
        assertThat(llm.getCompletionRequests()).isEmpty();
    }

    @Test
    void protocolContextOverridesAi() throws Exception {
        ai.setMaxContext(1000);
        ai.setMaxCompletionTokens(950);
        ai = (OpenAICompatible) aiService.save(ai);

        JsonObject body = completedRequestBody(manuscript(protocol(x -> x.setMaxTokens(8000))));

        assertThat(body.get("max_completion_tokens").getAsInt()).isEqualTo(950);
    }

    @Test
    void jailbreakIsFirstInPrompt() throws Exception {
        ai.setNeedsJailbreak(true);
        ai.setJailbreak("JAILBREAK");
        ai = (OpenAICompatible) aiService.save(ai);

        completedRequestBody(manuscript(protocol));

        MockLLMRequest request = llm.lastCompletionRequest();
        MockLLMRequest.Message first = request.getMessages().getFirst();
        assertThat(first.role()).isEqualTo("system");
        assertThat(first.content()).startsWith("JAILBREAK\n\n");
    }

    @Test
    void jailbreakIsLeftOutWhenNotNeeded() throws Exception {
        ai.setNeedsJailbreak(false);
        ai.setJailbreak("JAILBREAK");
        ai = (OpenAICompatible) aiService.save(ai);

        completedRequestBody(manuscript(protocol));

        assertThat(llm.lastCompletionRequest().getPromptText()).doesNotContain("JAILBREAK");
    }

    @Test
    void additionalParametersAreSent() throws Exception {
        ai.setAdditionalParameters(JsonParser.parseString(
                "{\"top_k\": 40, \"min_p\": 0.05, \"stop\": [\"THE END\"], \"provider\": {\"order\": [\"a\"]}}").getAsJsonObject());
        ai = (OpenAICompatible) aiService.save(ai);

        JsonObject body = completedRequestBody(manuscript(protocol));

        assertThat(body.get("top_k").getAsJsonPrimitive().getAsString()).as("integer stays integer").isEqualTo("40");
        assertThat(body.get("min_p").getAsDouble()).isEqualTo(0.05);
        assertThat(body.getAsJsonArray("stop").get(0).getAsString()).isEqualTo("THE END");
        assertThat(body.getAsJsonObject("provider").getAsJsonArray("order").get(0).getAsString()).isEqualTo("a");
    }

    @Test
    void additionalParametersOverrideProtocol() throws Exception {
        ai.setAdditionalParameters(JsonParser.parseString("{\"temperature\": 0.2}").getAsJsonObject());
        ai = (OpenAICompatible) aiService.save(ai);

        JsonObject body = completedRequestBody(manuscript(protocol(x -> {
            x.setTemperatureEnabled(true);
            x.setTemperature(0.7);
        })));

        assertThat(body.get("temperature").getAsDouble()).isEqualTo(0.2);
        assertThat(llm.lastCompletionRequest().getRawBody().split("\"temperature\"", -1)).as("sent once").hasSize(2);
    }
}
