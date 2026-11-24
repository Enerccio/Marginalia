package com.github.enerccio.marginalia.inference;

import com.github.enerccio.marginalia.domain.collections.PromptPlacement;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.domain.service.impl.inference.PromptPlacements;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.test.InferenceCollector;
import com.github.enerccio.marginalia.test.PlatformTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The AI settings that place parts of a request: the images before or after the text, the function definitions and the rule
 * about calls in the system message or at the start or end of the latest user message, consecutive messages of one role merged.
 */
class PromptPlacementTest extends PlatformTestBase {

    private static final String RULE = "Act only through function calls.";

    interface Cast {

        @LLMFunction(description = "Looks up a character")
        void lookup(FunctionContinuation<String> done);
    }

    private static List<LLMChatMessage> conversation() {
        return List.of(LLMChatMessage.of(LLMRole.SYSTEM, "You are a writer."), LLMChatMessage.of(LLMRole.USER, "Hello."),
                LLMChatMessage.of(LLMRole.ASSISTANT, "Hi."), LLMChatMessage.of(LLMRole.USER, "Who is Anna?"));
    }

    /** The messages the server got for one answer of the AI, with the functions offered or not. */
    private JsonArray sent(OpenAICompatible ai, List<LLMChatMessage> payload, boolean withFunctions) throws Exception {
        aiService.save(ai);
        FunctionSet functions = withFunctions
                ? inferenceServices.getFunctionRegistry().bind(Cast.class, (Cast) done -> done.complete("Anna is 30")) : null;
        llm.enqueue(MockLLMResponse.text("Anna is 30."));
        InferenceCollector collector = new InferenceCollector();
        inferenceServices.forAI(ai).stream(payload, null, null, functions, collector);
        assertThat(collector.await(Duration.ofSeconds(10))).isEqualTo(InferenceCollector.Outcome.COMPLETED);
        return llm.lastCompletionRequest().getBody().getAsJsonArray("messages");
    }

    private static String content(JsonArray messages, int index) {
        return messages.get(index).getAsJsonObject().get("content").getAsString();
    }

    @Test
    void theRuleGoesWhereItIsSet() throws Exception {
        login();
        OpenAICompatible ai = createAI();
        ai.setFunctionRule(RULE);

        ai.setFunctionRulePlacement(PromptPlacement.AFTER);
        JsonArray messages = sent(ai, conversation(), true);
        assertThat(content(messages, 3)).isEqualTo("Who is Anna?\n\n" + RULE);
        assertThat(content(messages, 1)).isEqualTo("Hello.");
        assertThat(content(messages, 0)).isEqualTo("You are a writer.");

        ai.setFunctionRulePlacement(PromptPlacement.BEFORE);
        messages = sent(ai, conversation(), true);
        assertThat(content(messages, 3)).isEqualTo(RULE + "\n\nWho is Anna?");

        ai.setFunctionRulePlacement(PromptPlacement.SYSTEM);
        messages = sent(ai, conversation(), true);
        assertThat(content(messages, 0)).isEqualTo("You are a writer.\n\n" + RULE);
        assertThat(content(messages, 3)).isEqualTo("Who is Anna?");
    }

    @Test
    void noRuleWithoutFunctionsAndTheCallerKeepsItsMessages() throws Exception {
        login();
        OpenAICompatible ai = createAI();
        ai.setFunctionRule(RULE);
        List<LLMChatMessage> payload = conversation();

        JsonArray messages = sent(ai, payload, false);
        assertThat(content(messages, 3)).isEqualTo("Who is Anna?");

        sent(ai, payload, true);
        assertThat(payload.get(3).getContent()).isEqualTo("Who is Anna?");
    }

    @Test
    void theDefinitionsCanBeWrittenIntoTheLatestUserMessage() throws Exception {
        login();
        OpenAICompatible ai = createAI();
        ai.setFunctionsPlacement(PromptPlacement.BEFORE);
        ai.setFunctionRule(RULE);
        ai.setFunctionRulePlacement(PromptPlacement.BEFORE);

        JsonArray messages = sent(ai, conversation(), true);
        String latest = content(messages, 3);
        assertThat(latest).startsWith(RULE + "\n\nFunctions you can call:").contains("\"lookup\"").endsWith("Who is Anna?");
        // the server still gets the tools, it needs them to read the calls
        assertThat(llm.lastCompletionRequest().getBody().getAsJsonArray("tools")).hasSize(1);
    }

    @Test
    void imagesGoBeforeTheTextWhenTheAiWantsIt() throws Exception {
        login();
        OpenAICompatible ai = createAI();
        ai.setMultimodal(true);
        LLMChatMessage withImage = LLMChatMessage.of(LLMRole.USER, "What is this?")
                .addAttachment(LLMAttachment.of("image/png", new byte[]{1, 2, 3}));

        JsonArray parts = sent(ai, List.of(withImage), false).get(0).getAsJsonObject().getAsJsonArray("content");
        assertThat(type(parts.get(0).getAsJsonObject())).isEqualTo("text");
        assertThat(type(parts.get(1).getAsJsonObject())).isEqualTo("image_url");

        ai.setImagePlacement(PromptPlacement.BEFORE);
        parts = sent(ai, List.of(withImage), false).get(0).getAsJsonObject().getAsJsonArray("content");
        assertThat(type(parts.get(0).getAsJsonObject())).isEqualTo("image_url");
        assertThat(type(parts.get(1).getAsJsonObject())).isEqualTo("text");
        assertThat(withImage.isImagesFirst()).isFalse();
    }

    @Test
    void placeWithoutAUserMessageGoesIntoTheSystemMessage() {
        List<LLMChatMessage> placed = PromptPlacements.place(List.of(LLMChatMessage.of(LLMRole.ASSISTANT, "Hi.")), RULE,
                PromptPlacement.AFTER);

        assertThat(placed).hasSize(2);
        assertThat(placed.getFirst().getRole()).isEqualTo(LLMRole.SYSTEM);
        assertThat(placed.getFirst().getContent()).isEqualTo(RULE);
    }

    @Test
    void consecutiveMessagesOfOneRoleAreSentAsOneWhenTheAiWantsIt() throws Exception {
        login();
        OpenAICompatible ai = createAI();
        List<LLMChatMessage> payload = List.of(LLMChatMessage.of(LLMRole.SYSTEM, "You are a writer."),
                LLMChatMessage.of(LLMRole.SYSTEM, "Style: terse."), LLMChatMessage.of(LLMRole.USER, "Hello."),
                LLMChatMessage.of(LLMRole.ASSISTANT, "Hi."), LLMChatMessage.of(LLMRole.USER, "What Anna knows."),
                LLMChatMessage.of(LLMRole.USER, "Go on."));

        assertThat(sent(ai, payload, false)).hasSize(6);

        ai.setMergeConsecutiveRoles(true);
        JsonArray messages = sent(ai, payload, false);
        assertThat(messages).hasSize(4);
        assertThat(content(messages, 0)).isEqualTo("You are a writer.\n\nStyle: terse.");
        assertThat(content(messages, 1)).isEqualTo("Hello.");
        assertThat(content(messages, 3)).isEqualTo("What Anna knows.\n\nGo on.");
        // the caller's messages are not changed
        assertThat(payload.get(4).getContent()).isEqualTo("What Anna knows.");
        assertThat(aiService.findForUser(ai.getUuid()).isMergeConsecutiveRoles()).isTrue();
    }

    @Test
    void functionResultsCallsAndTwoAttachmentsStayApart() {
        LLMChatMessage call = LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("c1", "lookup", "{}")));
        List<LLMChatMessage> payload = List.of(LLMChatMessage.of(LLMRole.ASSISTANT, "Let me look."), call,
                LLMChatMessage.ofToolResult("c1", "a"), LLMChatMessage.ofToolResult("c2", "b"),
                LLMChatMessage.of(LLMRole.USER, "One").addAttachment(LLMAttachment.of("image/png", new byte[]{1})),
                LLMChatMessage.of(LLMRole.USER, "Two").addAttachment(LLMAttachment.of("image/png", new byte[]{2})),
                LLMChatMessage.of(LLMRole.USER, "Three"));

        List<LLMChatMessage> merged = PromptPlacements.mergeConsecutive(payload);

        assertThat(merged).hasSize(6);
        assertThat(merged.get(5).getContent()).isEqualTo("Two\n\nThree");
        assertThat(merged.get(5).getAttachments()).hasSize(1);
        // nothing to merge: the same list
        List<LLMChatMessage> plain = conversation();
        assertThat(PromptPlacements.mergeConsecutive(plain)).isSameAs(plain);
    }

    private static String type(JsonObject part) {
        return part.get("type").getAsString();
    }
}
