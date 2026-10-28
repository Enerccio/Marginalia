package com.github.enerccio.marginalia.extensions.anthropic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.enerccio.marginalia.domain.collections.ReasoningEffort;
import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.InferenceException.Type;
import com.github.enerccio.marginalia.domain.service.InferenceService.*;
import com.github.enerccio.marginalia.domain.service.function.FunctionDefinition;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.github.enerccio.marginalia.extensions.anthropic.FakeAnthropicServer.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The service against the Anthropic SDK and a fake server: what goes out on the wire, what comes back as chunks, rounds, results
 * and usage, how the failures of the API read, and that a request can be stopped while it waits.
 */
class AnthropicServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int WAIT_SECONDS = 20;

    private FakeAnthropicServer server;
    private ExtensionAI ai;
    private AnthropicInferenceService service;
    private final List<long[]> usage = Collections.synchronizedList(new ArrayList<>());

    @BeforeEach
    void setUp() throws Exception {
        server = new FakeAnthropicServer();
        ai = new ExtensionAI();
        ai.setExtensionId(AnthropicFactory.EXTENSION_ID);
        ai.getSettings().addProperty("apiKey", "sk-ant-test");
        ai.getSettings().addProperty("model", "claude-opus-5-5");
        ai.getSettings().addProperty("baseUrl", server.url());
        ai.getSettings().addProperty("maxRetries", 0);
        ai.setMaxCompletionTokens(2000);
        service = newService(ai);
    }

    @AfterEach
    void tearDown() {
        service.close();
        server.close();
    }

    private AnthropicInferenceService newService(ExtensionAI ai) {
        return newService(ai, () -> null);
    }

    private AnthropicInferenceService newService(ExtensionAI ai, Supplier<String> sessionIds) {
        AnthropicInferenceService created = new AnthropicInferenceService(ai, AnthropicSettings.of(ai, key -> key),
                LoggerFactory.getLogger("test"), sessionIds);
        TokenizerService tokenizer = (TokenizerService) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{TokenizerService.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "countTokensApprox" -> (long) ((String) args[0]).length() / 4;
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        UsageService usages = (UsageService) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{UsageService.class}, (proxy, method, args) -> {
                    if (method.getName().equals("record")) {
                        usage.add(new long[]{(Long) args[1], (Long) args[2]});
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        AILimitService limits = (AILimitService) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{AILimitService.class}, (proxy, method, args) -> null);
        created.wire(tokenizer, null, usages, limits);
        return created;
    }

    private static List<LLMChatMessage> story() {
        return List.of(LLMChatMessage.of(LLMRole.SYSTEM, "You write fiction."), LLMChatMessage.of(LLMRole.USER, "Begin."));
    }

    /** Collects what a stream hands out. */
    private static final class Collector implements InferenceAsyncCallback {
        final List<String> response = Collections.synchronizedList(new ArrayList<>());
        final List<String> reasoning = Collections.synchronizedList(new ArrayList<>());
        final List<String> calls = Collections.synchronizedList(new ArrayList<>());
        final AtomicReference<Throwable> error = new AtomicReference<>();
        final CountDownLatch done = new CountDownLatch(1);
        volatile boolean cancelled;
        volatile boolean completed;

        @Override
        public void onChunk(InferenceAsyncController controller, ChunkType type, String text) {
            (type == ChunkType.REASONING ? reasoning : response).add(text);
            controller.continueInference();
        }

        @Override
        public void onCompletion() {
            completed = true;
            done.countDown();
        }

        @Override
        public void onCancel() {
            cancelled = true;
            done.countDown();
        }

        @Override
        public void onError(Throwable exception) {
            error.set(exception);
            done.countDown();
        }

        @Override
        public boolean isDead() {
            return false;
        }

        @Override
        public void onFunctionCall(LLMToolCall call, String result, boolean failed) {
            calls.add(call.getName() + "=" + result + (failed ? " (failed)" : ""));
        }

        void await() throws InterruptedException {
            assertTrue(done.await(WAIT_SECONDS, TimeUnit.SECONDS), "the request did not end");
        }

        String text() {
            return String.join("", response);
        }
    }

    private static FunctionSet lookup(List<String> asked) {
        FunctionDefinition definition = FunctionDefinition.dynamic("lookup", "Looks a name up",
                (com.fasterxml.jackson.databind.node.ObjectNode) MAPPER.createObjectNode().put("type", "object")
                        .set("properties", MAPPER.createObjectNode().set("name", MAPPER.createObjectNode().put("type", "string"))),
                (continuation, arguments) -> {
                    asked.add(String.valueOf(arguments.get("name")));
                    continuation.complete("Mira is a ship's cook.");
                });
        return FunctionSet.bind(List.of(definition), null);
    }

    // ---- the stream

    @Test
    void textAndReasoningArriveAsChunksAndUsageIsWhatAnthropicReported() throws Exception {
        server.stream(messageStart(100, 40), thinking(0, "Plan it.", "SIG1"), text(1, "Hello, ", "world."), end("end_turn", 25));
        ai.setEnabledReasoning(true);
        ai.setReasoningEffort(ReasoningEffort.HIGH);
        Collector collector = new Collector();

        service.stream(story(), null, collector);
        collector.await();

        assertNull(collector.error.get());
        assertTrue(collector.completed);
        assertEquals("Hello, world.", collector.text());
        assertEquals(List.of("Plan it."), collector.reasoning);
        // the prompt is what was read as well as what was cached
        assertEquals(1, usage.size());
        assertArrayEquals(new long[]{140, 25}, usage.get(0));
    }

    @Test
    void requestCarriesTheSystemPromptTheMessagesAndTheLimits() throws Exception {
        server.stream(messageStart(10, 0), text(0, "ok"), end("end_turn", 1));
        Collector collector = new Collector();

        service.stream(story(), null, collector);
        collector.await();

        assertEquals("/v1/messages", server.paths.get(0));
        JsonNode request = server.requests.get(0);
        assertEquals("claude-opus-5-5", request.path("model").asText());
        assertEquals(2000, request.path("max_tokens").asInt());
        assertTrue(request.path("stream").asBoolean());
        assertEquals("You write fiction.", request.path("system").asText());
        assertEquals("user", request.path("messages").get(0).path("role").asText());
        assertEquals("Begin.", request.path("messages").get(0).path("content").get(0).path("text").asText());
        assertFalse(request.has("temperature"), "the sampling settings are refused by the API");
        assertFalse(request.has("thinking"), "reasoning is off");
    }

    @Test
    void sessionIdOfTheRequestIsSentAsAHeader() throws Exception {
        server.stream(messageStart(10, 0), text(0, "ok"), end("end_turn", 1));
        server.stream(messageStart(10, 0), text(0, "ok"), end("end_turn", 1));
        AnthropicInferenceService withSession = newService(ai, () -> "0123456789abcdef0123456789abcdef");
        try {
            // a request of the plain service has none, the one of the service that has a session sends it
            Collector without = new Collector();
            service.stream(story(), null, without);
            without.await();
            Collector with = new Collector();
            withSession.stream(story(), null, with);
            with.await();
        } finally {
            withSession.close();
        }

        assertNull(server.sessionIds.get(0));
        assertEquals("0123456789abcdef0123456789abcdef", server.sessionIds.get(1));
    }

    @Test
    void reasoningOnAsksForAnAdaptiveSummaryAndTheEffort() throws Exception {
        server.stream(messageStart(10, 0), text(0, "ok"), end("end_turn", 1));
        ai.setEnabledReasoning(true);
        ai.setReasoningEffort(ReasoningEffort.MEDIUM);
        Collector collector = new Collector();

        service.stream(story(), null, collector);
        collector.await();

        JsonNode request = server.requests.get(0);
        assertEquals("adaptive", request.path("thinking").path("type").asText());
        assertEquals("summarized", request.path("thinking").path("display").asText());
        assertEquals("medium", request.path("output_config").path("effort").asText());
    }

    @Test
    void olderModelsThinkWithABudget() throws Exception {
        ai.getSettings().addProperty("model", "claude-haiku-4-5");
        service.close();
        service = newService(ai);
        server.stream(messageStart(10, 0), text(0, "ok"), end("end_turn", 1));
        ai.setEnabledReasoning(true);
        ai.setReasoningEffort(ReasoningEffort.LOW);
        Collector collector = new Collector();

        service.stream(story(), null, collector);
        collector.await();

        JsonNode request = server.requests.get(0);
        assertEquals("enabled", request.path("thinking").path("type").asText());
        assertEquals(1024, request.path("thinking").path("budget_tokens").asInt());
        assertFalse(request.path("output_config").has("effort"));
    }

    @Test
    void refusalWithoutAnyTextIsAnErrorWithTheReason() throws Exception {
        server.stream(messageStart(10, 0), end("refusal", 0));
        Collector collector = new Collector();

        service.stream(story(), null, collector);
        collector.await();

        InferenceException error = assertInstanceOf(InferenceException.class, collector.error.get());
        assertEquals(Type.BAD_REQUEST, error.getType());
        assertTrue(error.getProviderMessage().contains("refusal"));
    }

    @Test
    void streamThatEndsBeforeTheEndOfTheMessageIsAConnectionError() throws Exception {
        server.stream(messageStart(10, 0), text(0, "half of"));
        Collector collector = new Collector();

        service.stream(story(), null, collector);
        collector.await();

        assertEquals("half of", collector.text());
        assertEquals(Type.CONNECTION, assertInstanceOf(InferenceException.class, collector.error.get()).getType());
    }

    @Test
    void stoppingWhileTheServerStallsCancelsAtOnce() throws Exception {
        server.streamThenHang(messageStart(10, 0), text(0, "first"));
        Collector collector = new Collector();

        CancellationToken token = service.stream(story(), null, collector);
        assertTrue(server.hanging.await(WAIT_SECONDS, TimeUnit.SECONDS));
        token.cancel();
        collector.await();

        assertTrue(collector.cancelled);
        assertNull(collector.error.get());
    }

    // ---- functions

    @Test
    void functionsAreCalledAndTheModelIsAskedAgainWithTheResultAndItsSignedThinking() throws Exception {
        server.stream(messageStart(50, 0), thinking(0, "Need the cook.", "SIG1"), text(1, "Let me check."),
                toolUse(2, "toolu_1", "lookup", "{\"name\":", "\"Mira\"}"), end("tool_use", 20));
        server.stream(messageStart(80, 0), text(0, "Mira cooks."), end("end_turn", 5));
        ai.setEnabledReasoning(true);
        ai.setReasoningEffort(ReasoningEffort.HIGH);
        List<String> asked = Collections.synchronizedList(new ArrayList<>());
        Collector collector = new Collector();

        service.stream(story(), null, lookup(asked), collector);
        collector.await();

        assertNull(collector.error.get());
        assertEquals(List.of("Mira"), asked);
        assertEquals(List.of("lookup=Mira is a ship's cook."), collector.calls);
        assertEquals("Let me check.Mira cooks.", collector.text());
        assertEquals("lookup", server.requests.get(0).path("tools").get(0).path("name").asText());

        JsonNode second = server.requests.get(1).path("messages");
        assertEquals(3, second.size());
        JsonNode assistant = second.get(1);
        assertEquals("assistant", assistant.path("role").asText());
        assertEquals("thinking", assistant.path("content").get(0).path("type").asText());
        assertEquals("SIG1", assistant.path("content").get(0).path("signature").asText());
        assertEquals("tool_use", assistant.path("content").get(2).path("type").asText());
        assertEquals("Mira", assistant.path("content").get(2).path("input").path("name").asText());
        JsonNode result = second.get(2).path("content").get(0);
        assertEquals("tool_result", result.path("type").asText());
        assertEquals("toolu_1", result.path("tool_use_id").asText());
        assertEquals("Mira is a ship's cook.", result.path("content").asText());
        // both requests are counted
        assertEquals(2, usage.size());
    }

    // ---- the whole answer

    @Test
    void inferHandsOverTheWholeAnswerCheckedAgainstTheSchema() throws Exception {
        server.stream(messageStart(10, 0), text(0, "{\"name\":", "\"Mira\"}"), end("end_turn", 6));
        JsonNode schema = MAPPER.readTree("{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}},"
                + "\"required\":[\"name\"],\"additionalProperties\":false}");
        AtomicReference<String> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        service.infer(story(), null, schema, null, new InferenceResultCallback() {
            @Override
            public void onResult(String text, com.github.enerccio.marginalia.domain.service.structured.SchemaCheck check) {
                assertTrue(check.valid());
                result.set(text);
                done.countDown();
            }

            @Override
            public void onCancel() {
                done.countDown();
            }

            @Override
            public void onError(Throwable exception) {
                failure.set(exception);
                done.countDown();
            }
        });

        assertTrue(done.await(WAIT_SECONDS, TimeUnit.SECONDS));
        assertNull(failure.get());
        assertEquals("{\"name\":\"Mira\"}", result.get());
        JsonNode format = server.requests.get(0).path("output_config").path("format");
        assertEquals("json_schema", format.path("type").asText());
        assertEquals("object", format.path("schema").path("type").asText());
    }

    @Test
    void roundHandsBackTheCallsWithoutMakingThemAndTheThinkingTravelsWithTheFirst() throws Exception {
        server.stream(messageStart(60, 20), thinking(0, "Hm.", "SIG9"), text(1, "Looking."),
                toolUse(2, "toolu_7", "lookup", "{\"name\":\"Mira\"}"), end("tool_use", 12));
        ai.setEnabledReasoning(true);
        ai.setReasoningEffort(ReasoningEffort.HIGH);
        List<String> asked = Collections.synchronizedList(new ArrayList<>());
        AtomicReference<List<LLMToolCall>> calls = new AtomicReference<>();
        AtomicReference<RoundUsage> roundUsage = new AtomicReference<>();
        AtomicReference<String> text = new AtomicReference<>();
        AtomicReference<String> reasoning = new AtomicReference<>();
        List<String> shown = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch done = new CountDownLatch(1);

        service.round(story(), null, lookup(asked), new InferenceRoundCallback() {
            @Override
            public void onRound(String t, List<LLMToolCall> c, RoundUsage u) {
                fail("the reasoning is reported with the round");
            }

            @Override
            public void onChunk(ChunkType type, String piece) {
                shown.add(type + ":" + piece);
            }

            @Override
            public void onRound(String t, String r, List<LLMToolCall> c, RoundUsage u) {
                text.set(t);
                reasoning.set(r);
                calls.set(c);
                roundUsage.set(u);
                done.countDown();
            }

            @Override
            public void onCancel() {
                done.countDown();
            }

            @Override
            public void onError(Throwable exception) {
                done.countDown();
            }
        });

        assertTrue(done.await(WAIT_SECONDS, TimeUnit.SECONDS));
        assertEquals("Looking.", text.get());
        assertEquals("Hm.", reasoning.get());
        assertTrue(asked.isEmpty(), "the runner of the agent makes the calls");
        assertEquals(1, calls.get().size());
        assertEquals("toolu_7", calls.get().get(0).getId());
        assertEquals("{\"name\":\"Mira\"}", calls.get().get(0).getArguments());
        List<?> thinking = AnthropicMessages.decodeThinking(calls.get().get(0).getSignature());
        assertEquals(1, thinking.size());
        assertEquals(80, roundUsage.get().promptTokens());
        assertEquals(12, roundUsage.get().completionTokens());
        assertEquals(20, roundUsage.get().stats().cachedTokens());
        assertEquals("tool_calls", roundUsage.get().stats().finishReason());
        assertEquals(List.of("REASONING:Hm.", "RESPONSE:Looking."), shown, "the answer is shown while it is written");
    }

    // ---- failures

    private Type failureOf(int status, String type, String message) {
        server.error(status, type, message);
        InferenceException e = assertThrows(InferenceException.class, () -> service.getModels());
        assertEquals(status, e.getStatusCode());
        assertTrue(e.getProviderMessage().contains(message), e.getProviderMessage());
        return e.getType();
    }

    @Test
    void failuresOfTheApiReadAsTheirTypes() {
        assertEquals(Type.AUTHENTICATION, failureOf(401, "authentication_error", "invalid x-api-key"));
        assertEquals(Type.PERMISSION, failureOf(403, "permission_error", "not allowed"));
        assertEquals(Type.NOT_FOUND, failureOf(404, "not_found_error", "no such thing"));
        assertEquals(Type.MODEL_NOT_FOUND, failureOf(404, "not_found_error", "model: claude-nothing"));
        assertEquals(Type.RATE_LIMIT, failureOf(429, "rate_limit_error", "slow down"));
        assertEquals(Type.SERVER, failureOf(529, "overloaded_error", "Overloaded"));
        assertEquals(Type.CONTEXT_OVERFLOW, failureOf(400, "invalid_request_error", "prompt is too long: 210000 tokens > 200000 maximum"));
        assertEquals(Type.BAD_REQUEST, failureOf(400, "invalid_request_error", "max_tokens must be positive"));
    }

    @Test
    void failureOfAStreamedRequestReachesTheCallback() throws Exception {
        server.error(401, "authentication_error", "invalid x-api-key");
        Collector collector = new Collector();

        service.stream(story(), null, collector);
        collector.await();

        assertEquals(Type.AUTHENTICATION, assertInstanceOf(InferenceException.class, collector.error.get()).getType());
        assertTrue(usage.isEmpty(), "a request that failed before any text is not counted");
    }

    @Test
    void noKeyAndNoModelAreSaidBeforeAnythingIsSent() {
        ExtensionAI bare = new ExtensionAI();
        bare.setExtensionId(AnthropicFactory.EXTENSION_ID);
        AnthropicInferenceService empty = newService(bare);

        assertEquals(Type.AUTHENTICATION, assertThrows(InferenceException.class, empty::getModels).getType());
        bare.getSettings().addProperty("apiKey", "k");
        AnthropicInferenceService noModel = newService(bare);
        assertEquals(Type.MODEL_NOT_FOUND, assertThrows(InferenceException.class,
                () -> noModel.stream(story(), null, new Collector())).getType());
        assertTrue(server.paths.isEmpty());
    }

    @Test
    void closedServiceSaysTheExtensionIsGone() {
        service.close();

        assertEquals(Type.EXTENSION_UNAVAILABLE, assertThrows(InferenceException.class, () -> service.getModels()).getType());
    }

    // ---- models and tokens

    @Test
    void modelsAreListedAndTheConnectionIsTestedWithAShortRequest() {
        String models = "{\"data\":[{\"type\":\"model\",\"id\":\"claude-opus-5-5\",\"display_name\":\"Claude Opus 5.5\","
                + "\"created_at\":\"2026-08-01T00:00:00Z\"},{\"type\":\"model\",\"id\":\"claude-haiku-5-5\",\"display_name\":\"Claude Haiku 5.5\","
                + "\"created_at\":\"2026-08-01T00:00:00Z\"}],\"has_more\":false,\"first_id\":\"claude-opus-5-5\",\"last_id\":\"claude-haiku-5-5\"}";
        server.reply(200, models);
        assertEquals(List.of("claude-opus-5-5", "claude-haiku-5-5"), service.getModels());

        server.reply(200, models);
        server.reply(200, "{\"id\":\"msg_1\",\"type\":\"message\",\"role\":\"assistant\",\"model\":\"claude-opus-5-5\","
                + "\"content\":[{\"type\":\"text\",\"text\":\"OK\"}],\"stop_reason\":\"end_turn\",\"stop_sequence\":null,"
                + "\"usage\":{\"input_tokens\":9,\"output_tokens\":2}}");
        assertEquals(2, service.testConnection().size());
        assertEquals("/v1/messages", server.paths.get(2));
        assertEquals(16, server.requests.get(2).path("max_tokens").asInt());
    }

    @Test
    void tokensAreCountedByAnthropicOnceForEachText() {
        server.reply(200, "{\"input_tokens\":42}");

        assertEquals(42, service.countTokensOnline("The ship sailed."));
        assertEquals(42, service.countTokensOnline("The ship sailed."), "the answer is remembered");
        assertEquals(1, server.paths.stream().filter(path -> path.equals("/v1/messages/count_tokens")).count());
        assertEquals(0, service.countTokensOnline("  "), "nothing is asked for a blank text");
    }
}
