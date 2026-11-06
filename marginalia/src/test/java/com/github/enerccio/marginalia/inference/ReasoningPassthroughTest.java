package com.github.enerccio.marginalia.inference;

import com.github.enerccio.marginalia.domain.collections.ReasoningPassthrough;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.test.InferenceCollector;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An AI can be set to get the thinking of the model back: never, between the tool calls of one turn, or with every answer.
 * The thinking goes as {@code reasoning_content} of the assistant message it belongs to.
 */
class ReasoningPassthroughTest extends MarginaliaTestBase {

    interface Cast {

        @LLMFunction(description = "Looks up a character")
        void lookup(FunctionContinuation<String> done);
    }

    /** The assistant messages of the request the model got after the call, as the server saw them. */
    private List<JsonObject> assistantMessagesOfSecondRound(ReasoningPassthrough mode) throws Exception {
        login();
        OpenAICompatible ai = createAI();
        ai.setReasoningPassthrough(mode);
        aiService.save(ai);

        FunctionSet functions = inferenceServices.getFunctionRegistry().bind(Cast.class,
                (Cast) done -> done.complete("Anna is 30"));
        llm.enqueue(MockLLMResponse.toolCall("lookup", "{}").withReasoning("I should look Anna up."));
        llm.enqueue(MockLLMResponse.text("Anna is 30."));

        // an earlier turn: the answer has thinking too, it is not of this turn
        LLMChatMessage earlier = LLMChatMessage.of(LLMRole.ASSISTANT, "Earlier answer.").withReasoning("Earlier thought.", false);
        List<LLMChatMessage> payload = List.of(
                LLMChatMessage.of(LLMRole.USER, "Hello."), earlier, LLMChatMessage.of(LLMRole.USER, "Who is Anna?"));

        InferenceCollector collector = new InferenceCollector();
        inferenceServices.forAI(ai).stream(payload, null, null, functions, collector);
        assertThat(collector.await(Duration.ofSeconds(10))).isEqualTo(InferenceCollector.Outcome.COMPLETED);

        JsonArray messages = llm.lastCompletionRequest().getBody().getAsJsonArray("messages");
        List<JsonObject> assistants = new ArrayList<>();
        for (JsonElement message : messages) {
            if ("assistant".equals(message.getAsJsonObject().get("role").getAsString())) {
                assistants.add(message.getAsJsonObject());
            }
        }
        return assistants;
    }

    private static String reasoningOf(JsonObject message) {
        return message.has("reasoning_content") ? message.get("reasoning_content").getAsString() : null;
    }

    @Test
    void noneSendsNoThinking() throws Exception {
        List<JsonObject> assistants = assistantMessagesOfSecondRound(ReasoningPassthrough.NONE);

        assertThat(assistants).hasSize(2);
        assertThat(assistants).allSatisfy(message -> assertThat(reasoningOf(message)).isNull());
    }

    @Test
    void theDefaultIsNone() throws Exception {
        login();
        assertThat(createAI().getReasoningPassthrough()).isEqualTo(ReasoningPassthrough.NONE);
    }

    @Test
    void toolCallsSendsTheThinkingOfTheCallsOfThisTurnOnly() throws Exception {
        List<JsonObject> assistants = assistantMessagesOfSecondRound(ReasoningPassthrough.TOOL_CALLS);

        assertThat(assistants).hasSize(2);
        // the earlier answer first: its thinking stays at home, the answer that called the tool takes its own along
        assertThat(reasoningOf(assistants.get(0))).isNull();
        assertThat(assistants.get(1).has("tool_calls")).isTrue();
        assertThat(reasoningOf(assistants.get(1))).isEqualTo("I should look Anna up.");
    }

    @Test
    void alwaysSendsTheThinkingOfEveryAnswerThatHasIt() throws Exception {
        List<JsonObject> assistants = assistantMessagesOfSecondRound(ReasoningPassthrough.ALWAYS);

        assertThat(assistants).hasSize(2);
        assertThat(reasoningOf(assistants.get(0))).isEqualTo("Earlier thought.");
        assertThat(reasoningOf(assistants.get(1))).isEqualTo("I should look Anna up.");
    }
}
