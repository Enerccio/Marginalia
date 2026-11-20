package com.github.enerccio.marginalia.extensions.grok;

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

import static com.github.enerccio.marginalia.extensions.grok.FakeGrokServer.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The service against a fake xAI server: what goes out on the wire, what comes back as chunks, rounds, results and usage, how the
 * failures of the API read, and that a request can be stopped while it waits.
 */
class GrokServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int WAIT_SECONDS = 20;

    private FakeGrokServer server;
    private ExtensionAI ai;
    private GrokInferenceService service;
    private final List<long[]> usage = Collections.synchronizedList(new ArrayList<>());

    @BeforeEach
    void setUp() throws Exception {
        server = new FakeGrokServer();
        ai = new ExtensionAI();
        ai.setExtensionId(GrokFactory.EXTENSION_ID);
        ai.getSettings().addProperty("apiKey", "xai-test");
        ai.getSettings().addProperty("model", "grok-4");
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

    private GrokInferenceService newService(ExtensionAI ai) {
        GrokInferenceService created = new GrokInferenceService(ai, GrokSettings.of(ai, key -> key),
                LoggerFactory.getLogger("test"), () -> "session-1");
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
    void textAndReasoningArriveAsChunksAndTheReasoningTokensAreGenerated() throws Exception {
        server.stream(thought("Plan it."), text("Hello, "), text("world."), finish("stop"), usage(100, 25, 15, 40));
        Collector collector = new Collector();

        service.stream(story(), null, collector);
        collector.await();

        assertNull(collector.error.get());
        assertTrue(collector.completed);
        assertEquals("Hello, world.", collector.text());
        assertEquals(List.of("Plan it."), collector.reasoning);
        assertEquals(1, usage.size());
        // xAI reports the reasoning tokens apart from the completion tokens and bills both
        assertArrayEquals(new long[]{100, 40}, usage.get(0));
    }

    @Test
    void requestCarriesTheMessagesTheLimitsTheKeyAndTheSession() throws Exception {
        server.stream(text("ok"), finish("stop"));
        Collector collector = new Collector();

        service.stream(story(), null, collector);
        collector.await();

        assertEquals("/v1/chat/completions", server.paths.get(0));
        assertEquals("Bearer xai-test", server.authorizations.get(0));
        assertEquals("session-1", server.sessionIds.get(0));
        JsonNode request = server.requests.get(0);
        assertEquals("grok-4", request.path("model").asText());
        assertEquals(2000, request.path("max_completion_tokens").asInt());
        assertTrue(request.path("stream").asBoolean());
        assertTrue(request.path("stream_options").path("include_usage").asBoolean());
        assertEquals("system", request.path("messages").get(0).path("role").asText());
        assertEquals("Begin.", request.path("messages").get(1).path("content").asText());
        assertFalse(request.has("reasoning_effort"), "grok-4 refuses the parameter");
        assertFalse(request.has("temperature"));
    }

    @Test
    void reasoningEffortIsSentToGrok3MiniOnly() throws Exception {
        ai.getSettings().addProperty("model", "grok-3-mini");
        service.close();
        service = newService(ai);
        ai.setEnabledReasoning(true);
        ai.setReasoningEffort(ReasoningEffort.MEDIUM);
        server.stream(text("ok"), finish("stop"));
        Collector collector = new Collector();

        service.stream(story(), null, collector);
        collector.await();

        assertEquals("high", server.requests.get(0).path("reasoning_effort").asText());
    }

    @Test
    void aStreamThatIsCutShortIsAConnectionError() throws Exception {
        server.streamCut(text("half"));
        Collector collector = new Collector();

        service.stream(story(), null, collector);
        collector.await();

        assertInstanceOf(InferenceException.class, collector.error.get());
        assertEquals(Type.CONNECTION, ((InferenceException) collector.error.get()).getType());
    }

    @Test
    void anAnswerThatWasCutByTheLimitAndSaidNothingIsAnError() throws Exception {
        server.stream(finish("length"));
        Collector collector = new Collector();

        service.stream(story(), null, collector);
        collector.await();

        assertEquals(Type.BAD_REQUEST, ((InferenceException) collector.error.get()).getType());
    }

    @Test
    void theRequestCanBeStoppedWhileItWaits() throws Exception {
        server.streamThenHang(text("first"));
        Collector collector = new Collector();

        CancellationToken token = service.stream(story(), null, collector);
        assertTrue(server.hanging.await(WAIT_SECONDS, TimeUnit.SECONDS));
        // the read for the next chunk is blocked on the stalled server; cancelling cuts the call
        Thread.sleep(300);
        token.cancel();
        collector.await();

        assertTrue(collector.cancelled);
        assertNull(collector.error.get());
        assertEquals("first", collector.text());
    }

    // ---- failures

    @Test
    void failuresOfTheApiAreClassified() throws Exception {
        assertFailure(401, "{\"code\":\"x\",\"error\":\"Incorrect API key provided\"}", Type.AUTHENTICATION);
        assertFailure(403, "{\"error\":\"no access\"}", Type.PERMISSION);
        assertFailure(404, "{\"error\":\"The model grok-9 does not exist\"}", Type.MODEL_NOT_FOUND);
        assertFailure(429, "{\"error\":{\"message\":\"slow down\"}}", Type.RATE_LIMIT);
        assertFailure(503, "{\"error\":\"down\"}", Type.SERVER);
        assertFailure(400, "{\"error\":\"This model's maximum prompt length is 131072 but the request contains 200000 tokens\"}", Type.CONTEXT_OVERFLOW);
        assertFailure(400, "{\"error\":\"bad field\"}", Type.BAD_REQUEST);
    }

    private void assertFailure(int status, String body, Type type) throws Exception {
        server.json(status, body);
        Collector collector = new Collector();

        service.stream(story(), null, collector);
        collector.await();

        assertInstanceOf(InferenceException.class, collector.error.get(), body);
        assertEquals(type, ((InferenceException) collector.error.get()).getType(), body);
        assertEquals(status, ((InferenceException) collector.error.get()).getStatusCode());
    }

    @Test
    void aMissingKeyIsRefusedBeforeAnythingIsSent() {
        ai.getSettings().remove("apiKey");
        service.close();
        service = newService(ai);

        InferenceException e = assertThrows(InferenceException.class, () -> service.stream(story(), null, new Collector()));

        assertEquals(Type.AUTHENTICATION, e.getType());
        assertTrue(server.paths.isEmpty());
    }

    @Test
    void retriesGoOnAfterAServerFailure() throws Exception {
        ai.getSettings().addProperty("maxRetries", 1);
        service.close();
        service = newService(ai);
        server.json(503, "{\"error\":\"down\"}").stream(text("ok"), finish("stop"));
        Collector collector = new Collector();

        service.stream(story(), null, collector);
        collector.await();

        assertNull(collector.error.get());
        assertEquals("ok", collector.text());
        assertEquals(2, server.paths.size());
    }

    // ---- models, tokens

    @Test
    void modelsAreListedAndTheConnectionIsTested() {
        server.json(200, "{\"data\":[{\"id\":\"grok-4\"},{\"id\":\"grok-3-mini\"}]}")
                .json(200, "{\"data\":[{\"id\":\"grok-4\"},{\"id\":\"grok-3-mini\"}]}")
                .json(200, "{\"choices\":[{\"message\":{\"content\":\"OK\"}}]}");

        assertEquals(List.of("grok-4", "grok-3-mini"), service.getModels());
        assertEquals(List.of("grok-4", "grok-3-mini"), service.testConnection());
        assertEquals("/v1/models", server.paths.get(0));
        assertEquals("/v1/chat/completions", server.paths.get(2));
        assertFalse(server.requests.get(2).path("stream").asBoolean());
    }

    @Test
    void tokensAreCountedByXaiOnceForEachText() {
        server.json(200, "{\"token_ids\":[{\"token_id\":1},{\"token_id\":2},{\"token_id\":3}]}");

        assertEquals(3, service.countTokensOnline("Hello there"));
        assertEquals(3, service.countTokensOnline("Hello there"));

        assertEquals(1, server.paths.size());
        assertEquals("/v1/tokenize-text", server.paths.get(0));
        assertEquals("Hello there", server.requests.get(0).path("text").asText());
    }

    // ---- functions

    @Test
    void functionsAreCalledAndTheModelIsAskedAgainWithTheResults() throws Exception {
        List<String> asked = new ArrayList<>();
        server.stream(text("Looking."), call(0, "call_7", "lookup", "{\"name\":\"Mira\"}"), finish("tool_calls"), usage(50, 5, 0, 0))
                .stream(text("She cooks."), finish("stop"), usage(70, 6, 0, 0));
        Collector collector = new Collector();

        service.stream(story(), null, lookup(asked), collector);
        collector.await();

        assertNull(collector.error.get());
        assertEquals(List.of("Mira"), asked);
        assertEquals(List.of("lookup=Mira is a ship's cook."), collector.calls);
        assertEquals("Looking.She cooks.", collector.text());
        assertEquals(2, server.requests.size());
        assertEquals("lookup", server.requests.get(0).path("tools").get(0).path("function").path("name").asText());
        JsonNode second = server.requests.get(1).path("messages");
        JsonNode said = second.get(second.size() - 2);
        assertEquals("call_7", said.path("tool_calls").get(0).path("id").asText());
        JsonNode result = second.get(second.size() - 1);
        assertEquals("tool", result.path("role").asText());
        assertEquals("call_7", result.path("tool_call_id").asText());
        assertEquals(2, usage.size());
    }

    // ---- the whole answer

    @Test
    void inferHandsOverTheWholeAnswerCheckedAgainstTheSchema() throws Exception {
        server.stream(text("{\"name\":"), text("\"Mira\"}"), finish("stop"));
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
        assertEquals("json_schema", server.requests.get(0).path("response_format").path("type").asText());
    }

    @Test
    void aRoundReportsTheCallsWithoutMakingThem() throws Exception {
        List<String> asked = new ArrayList<>();
        server.stream(thought("Hm."), text("Looking."), call(0, "call_7", "lookup", "{\"name\":\"Mira\"}"),
                finish("tool_calls"), usage(80, 9, 3, 0));
        AtomicReference<String> text = new AtomicReference<>();
        AtomicReference<String> reasoning = new AtomicReference<>();
        AtomicReference<List<LLMToolCall>> calls = new AtomicReference<>();
        AtomicReference<RoundUsage> roundUsage = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);

        service.round(story(), null, lookup(asked), new InferenceRoundCallback() {
            @Override
            public void onRound(String t, List<LLMToolCall> c, RoundUsage u) {
                fail("the reasoning is reported with the round");
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
        assertEquals("call_7", calls.get().get(0).getId());
        assertEquals("{\"name\":\"Mira\"}", calls.get().get(0).getArguments());
        assertEquals(80, roundUsage.get().promptTokens());
        assertEquals(12, roundUsage.get().completionTokens());
        assertEquals("tool_calls", roundUsage.get().stats().finishReason());
    }
}
