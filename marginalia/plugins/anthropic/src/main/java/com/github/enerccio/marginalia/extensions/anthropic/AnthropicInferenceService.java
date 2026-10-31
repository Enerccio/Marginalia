package com.github.enerccio.marginalia.extensions.anthropic;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.Timeout;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.models.messages.*;
import com.anthropic.models.models.ModelInfo;
import com.anthropic.models.models.ModelListParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.concurrent.AsyncRunnableWrapper;
import com.github.enerccio.marginalia.domain.collections.ReasoningEffort;
import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.InferenceException.Type;
import com.github.enerccio.marginalia.domain.service.function.FunctionCallException;
import com.github.enerccio.marginalia.domain.service.function.FunctionDefinition;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.domain.service.structured.SchemaCheck;
import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputs;
import com.github.enerccio.marginalia.extensions.anthropic.AnthropicMessages.Prepared;
import com.github.enerccio.marginalia.extensions.anthropic.AnthropicMessages.Turn;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.context.request.RequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.github.enerccio.marginalia.extensions.anthropic.AnthropicLocalization.T;

/**
 * The inference service of an Anthropic Claude AI, over the Anthropic Java SDK. It does what the service of an OpenAI
 * compatible AI does: streams with the reasoning (the thinking summaries of Claude) apart from the answer, lets the model
 * call the functions, answers in a JSON schema, counts the usage of the user and honors the limits an administrator set.
 * <p>
 * Every request is streamed (Anthropic recommends it for long answers, the SDK refuses a long one that is not), also the ones
 * whose answer is handed over whole ({@link #infer}, {@link #round}). The sampling settings of the protocol (temperature, top p,
 * the penalties) are not sent: the current API refuses them, only the response size is used.
 * <p>
 * The tokens of the texts are counted by Anthropic ({@code messages.countTokens}, through the {@link TokenizerResolver} of the
 * {@link AnthropicFactory} so that the application caches the way of counting and counts approximately inside a transaction
 * that holds the database write lock). Each text is asked once (the answers are remembered), and after a failure the service
 * counts approximately for a minute instead of waiting for Anthropic on every text. The usage is what Anthropic reports.
 */
@Configurable
public class AnthropicInferenceService implements InferenceService, AutoCloseable {

    @Autowired
    private TokenizerService tokenizerService;

    @Autowired
    private Configuration configuration;

    @Autowired
    private UsageService usageService;

    @Autowired
    private AILimitService limitService;

    @Autowired
    private ActivityTracker activityTracker;

    private final ExtensionAI ai;
    private final AnthropicSettings settings;
    private final Logger log;
    private final Supplier<String> sessionIds;

    private static final int COUNT_CACHE_SIZE = 512;
    private static final long COUNT_PAUSE_MILLIS = 60_000;

    /** The response size when the AI and the protocol set none: Claude needs a limit on every request. */
    static final int DEFAULT_RESPONSE_TOKENS = 16_000;

    /**
     * The models that think with a budget of tokens (before Claude 4.6); the later ones think adaptively and refuse a budget.
     * {@code claude-3-7-sonnet-...}, {@code claude-sonnet-4-...}, {@code claude-opus-4-1-...}, {@code claude-haiku-4-5}.
     */
    private static final Pattern BUDGET_THINKING = Pattern.compile(
            "^claude-(3[-.].*|(opus|sonnet|haiku)-3.*|(opus|sonnet|haiku)-4(-[0-5])?(-\\d{8})?)$");

    // the tokens of the texts Anthropic counted, by the hash of the text (the least recently used go first)
    private final Map<String, Long> countCache = Collections.synchronizedMap(new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
            return size() > COUNT_CACHE_SIZE;
        }
    });

    // after a failed count Anthropic is not asked until then, the estimate is used
    private volatile long countPausedUntil;

    private AnthropicClient client;
    private boolean closed;

    AnthropicInferenceService(ExtensionAI ai, AnthropicSettings settings, Logger log) {
        this(ai, settings, log, () -> null);
    }

    /**
     * @param sessionIds the session id of the request that starts on the calling thread, see
     *                   {@code InferenceServices.currentSessionId()}
     */
    AnthropicInferenceService(ExtensionAI ai, AnthropicSettings settings, Logger log, Supplier<String> sessionIds) {
        this.ai = ai;
        this.settings = settings;
        this.log = log;
        this.sessionIds = sessionIds;
    }

    /** What the application injects, for a test that has no Spring context. */
    void wire(TokenizerService tokenizerService, Configuration configuration, UsageService usageService, AILimitService limitService) {
        this.tokenizerService = tokenizerService;
        this.configuration = configuration;
        this.usageService = usageService;
        this.limitService = limitService;
    }

    /**
     * The time the request of the book is on the wire, for the generation time of the writing statistics; nothing for a request that
     * is not metered or has no book (and in a test without the application).
     */
    private ActivityTracker.Registration generating(Run run) {
        return run.metered && activityTracker != null ? activityTracker.generate(run.book) : ActivityTracker.NONE;
    }

    // ---- models and connection

    @Override
    public List<String> getModels() {
        try {
            List<String> names = new ArrayList<>();
            for (ModelInfo model : client().models().list(ModelListParams.builder().limit(1000L).build()).autoPager()) {
                names.add(model.id());
            }
            return names;
        } catch (RuntimeException e) {
            throw runtime(e);
        }
    }

    @Override
    public List<String> testConnection() {
        List<String> models = getModels();
        try {
            client().messages().create(MessageCreateParams.builder().model(model()).maxTokens(16L)
                    .addUserMessage("Say OK.").build());
        } catch (RuntimeException e) {
            throw runtime(e);
        }
        return models;
    }

    @Override
    public long countTokens(String text) throws Exception {
        return tokenizerService.countTokens(ai, text);
    }

    /**
     * The count of Anthropic's own tokenizer, for {@link AnthropicFactory}'s resolver: the application calls it through
     * {@link TokenizerService#countTokens}, which falls back to the estimate when this throws.
     */
    long countTokensOnline(String text) {
        if (System.currentTimeMillis() < countPausedUntil) {
            throw new IllegalStateException("Counting with Anthropic is paused after a failure");
        }
        if (StringUtils.isBlank(text)) {
            // a message of nothing is refused by the API
            return 0;
        }
        String hash = sha256(text);
        Long known = countCache.get(hash);
        if (known != null) {
            return known;
        }
        try {
            long tokens = client().messages().countTokens(MessageCountTokensParams.builder().model(model())
                    .addUserMessage(text).build()).inputTokens();
            countCache.put(hash, tokens);
            return tokens;
        } catch (RuntimeException e) {
            countPausedUntil = System.currentTimeMillis() + COUNT_PAUSE_MILLIS;
            logFailure("Counting the tokens with Anthropic failed, counting approximately for a minute", e);
            throw runtime(e);
        }
    }

    private static String sha256(String text) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    @Override
    public long countTokensApprox(String text) throws Exception {
        return tokenizerService.countTokensApprox(text);
    }

    // ---- one round

    /**
     * One request for an agent: the model is asked once and what it answered is handed back whole, the functions it asked for are
     * <b>not</b> called (the runner of the agent calls them and asks again).
     */
    @Override
    public CancellationToken round(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions,
                                   InferenceRoundCallback callback) throws Exception {
        if (!UnmeteredScope.isActive()) {
            limitService.check(ai, approximatePromptTokens(payload), responseTokens(protocol));
        }
        Prepared prepared = AnthropicMessages.prepare(payload);
        boolean functionsOffered = functions != null && !functions.isEmpty();
        Run run = new Run(protocol, functionsOffered ? functions : null, null,
                new Outcome(callback::isDead, (call, result, failed) -> {}, callback::onCancel, callback::onError));
        AnthropicClient sdk = client();
        MessageCreateParams params = request(run, prepared.system(), prepared.turns(), false);

        AsyncRunnableWrapper wrapper = new AsyncRunnableWrapper(configuration);
        CompletableFuture.runAsync(() -> {
            try {
                wrapper.run(() -> {
                    if (run.outcome.dead().getAsBoolean() || run.token.isCancelled()) {
                        callback.onCancel();
                        return;
                    }
                    long began = System.nanoTime();
                    Answer answer;
                    try (ActivityTracker.Registration generating = generating(run)) {
                        answer = collect(sdk, run, params, piece -> {
                            try {
                                callback.onChunk(piece.reasoning() ? ChunkType.REASONING : ChunkType.RESPONSE, piece.text());
                            } catch (RuntimeException e) {
                                // the view must not fail the round
                                log.trace("A chunk of a round was not shown", e);
                            }
                        });
                    }
                    long millis = (System.nanoTime() - began) / 1_000_000;
                    TokenUsage counted = run.metered
                            ? recordUsage(run.attributes, run.book, prepared.turns(), answer.reported(), answer.received.toString()) : null;
                    if (run.outcome.dead().getAsBoolean() || run.token.isCancelled()) {
                        callback.onCancel();
                    } else if (answer.calls().isEmpty() && answer.isEmptyAndStopped()) {
                        throw answer.stopped();
                    } else {
                        String reasoning = answer.reasoning.toString();
                        callback.onRound(answer.text.toString(), reasoning.isEmpty() ? null : reasoning, withThinking(answer),
                                roundUsage(counted != null ? counted : answer.reported(), answer, millis));
                    }
                });
            } catch (Throwable e) {
                logFailure("Failed to ask for a round", e);
                run.failOrCancel(e);
            } finally {
                run.forget();
            }
        });
        return run.token;
    }

    /**
     * The calls of an answer; the first carries the thinking blocks of the answer, which the next request has to send back with it
     * (the transcript of an agent keeps only the calls).
     */
    private static List<LLMToolCall> withThinking(Answer answer) {
        List<LLMToolCall> calls = answer.calls();
        String thinking = answer.message == null ? null : AnthropicMessages.encodeThinking(answer.message.content());
        if (thinking == null || calls.isEmpty()) {
            return calls;
        }
        LLMToolCall first = calls.get(0);
        List<LLMToolCall> carrying = new ArrayList<>(calls);
        carrying.set(0, LLMToolCall.of(first.getId(), first.getName(), first.getArguments(), thinking));
        return carrying;
    }

    /**
     * What a round used and how it went. The answer is not delivered while it is written, so the time of the whole request is
     * the one thing the provider's side of the clock can tell: it is given as the time of writing, the wait for the first word is
     * not known.
     */
    static RoundUsage roundUsage(TokenUsage reported, Answer answer, long millis) {
        long prompt = reported == null ? 0 : reported.promptTokens();
        long generated = reported == null ? 0 : reported.generatedTokens();
        String stop = answer.stopReason();
        String finish = stop == null ? null
                : "max_tokens".equals(stop) ? "length"
                : "end_turn".equals(stop) || "stop_sequence".equals(stop) ? "stop"
                : "tool_use".equals(stop) ? "tool_calls"
                : stop;
        return new RoundUsage(prompt, generated, new RoundStats(reported == null ? -1 : reported.cachedTokens(),
                reported == null ? -1 : reported.thoughtTokens(), -1, millis, finish, -1, -1));
    }

    // ---- streaming

    @Override
    public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, InferenceAsyncCallback callback) throws Exception {
        return stream(payload, protocol, null, null, callback);
    }

    @Override
    public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions,
                                    InferenceAsyncCallback callback) throws Exception {
        return stream(payload, protocol, null, functions, callback);
    }

    @Override
    public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, JsonNode schema,
                                    FunctionSet functions, InferenceAsyncCallback callback) throws Exception {
        // the limits an administrator set for the user, before anything is sent: the prompt is known, so it must fit
        if (!UnmeteredScope.isActive()) {
            limitService.check(ai, approximatePromptTokens(payload), responseTokens(protocol));
        }
        Prepared prepared = AnthropicMessages.prepare(payload);
        Run run = new Run(protocol, functions != null && !functions.isEmpty() ? functions : null, schema,
                new Outcome(callback::isDead, callback::onFunctionCall, callback::onCancel, callback::onError));
        startStreamRound(run, callback, prepared.system(), prepared.turns(), 0);
        return run.token;
    }

    /**
     * Sends the conversation and streams the answer. Building the request fails here, in the caller's thread, for the
     * first round; the rest of the round runs asynchronously.
     */
    private void startStreamRound(Run run, InferenceAsyncCallback callback, String system, List<Turn> turns, int round) {
        boolean functionsOffered = run.functions != null;
        boolean finalRound = round >= MAX_FUNCTION_ROUNDS;
        AnthropicClient sdk = client();
        MessageCreateParams params = request(run, system, turns, functionsOffered && finalRound);

        AsyncRunnableWrapper wrapper = new AsyncRunnableWrapper(configuration);
        CompletableFuture.runAsync(() -> {
            try {
                wrapper.run(() -> {
                    if (run.token.isCancelled()) {
                        callback.onCancel();
                        return;
                    }
                    // from the moment the request is sent to the end of the answer (the recorder below is called whichever way it ends)
                    ActivityTracker.Registration generating = generating(run);
                    StreamResponse<RawMessageStreamEvent> stream;
                    try {
                        stream = open(sdk, run, params);
                    } catch (Throwable e) {
                        generating.close();
                        throw e;
                    }
                    AnthropicStreamController controller = new AnthropicStreamController(stream, callback, run,
                            (reported, generated) -> {
                                generating.close();
                                if (run.metered) {
                                    recordUsage(run.attributes, run.book, turns, reported, generated);
                                }
                            },
                            functionsOffered && !finalRound
                                    ? (answer) -> callFunctions(run, run.outcome, turns, answer,
                                    next -> startStreamRound(run, callback, system, next, round + 1)) : null);
                    controller.continueInference();
                });
            } catch (Throwable e) {
                run.forget();
                logFailure("Failed to start streaming inference", e);
                run.failOrCancel(e);
            }
        });
    }

    // ---- the whole answer at once

    @Override
    public CancellationToken infer(List<LLMChatMessage> payload, Protocol protocol, JsonNode schema,
                                   FunctionSet functions, InferenceResultCallback callback) throws Exception {
        if (!UnmeteredScope.isActive()) {
            limitService.check(ai, approximatePromptTokens(payload), responseTokens(protocol));
        }
        Prepared prepared = AnthropicMessages.prepare(payload);
        Run run = new Run(protocol, functions != null && !functions.isEmpty() ? functions : null, schema,
                new Outcome(callback::isDead, callback::onFunctionCall, callback::onCancel, callback::onError));
        inferRound(run, callback, prepared.system(), prepared.turns(), 0);
        return run.token;
    }

    /**
     * One request of {@link #infer}. Building it fails here, in the caller's thread, for the first round; the rest runs
     * asynchronously.
     */
    private void inferRound(Run run, InferenceResultCallback callback, String system, List<Turn> turns, int round) {
        boolean functionsOffered = run.functions != null;
        boolean finalRound = round >= MAX_FUNCTION_ROUNDS;
        AnthropicClient sdk = client();
        MessageCreateParams params = request(run, system, turns, functionsOffered && finalRound);

        AsyncRunnableWrapper wrapper = new AsyncRunnableWrapper(configuration);
        CompletableFuture.runAsync(() -> {
            try {
                wrapper.run(() -> {
                    if (run.outcome.dead().getAsBoolean() || run.token.isCancelled()) {
                        callback.onCancel();
                        return;
                    }
                    Answer answer;
                    try (ActivityTracker.Registration generating = generating(run)) {
                        answer = collect(sdk, run, params, null);
                    }
                    if (run.metered) {
                        recordUsage(run.attributes, run.book, turns, answer.reported(), answer.received.toString());
                    }

                    // the request can not be taken back once sent, but a canceled one gets no result
                    if (run.outcome.dead().getAsBoolean() || run.token.isCancelled()) {
                        callback.onCancel();
                    } else if (functionsOffered && !finalRound && !answer.calls().isEmpty()) {
                        callFunctions(run, run.outcome, turns, answer, next -> inferRound(run, callback, system, next, round + 1));
                    } else if (answer.isEmptyAndStopped()) {
                        throw answer.stopped();
                    } else {
                        String text = answer.text.toString();
                        callback.onResult(text, run.schema == null ? SchemaCheck.notChecked() : StructuredOutputs.validate(run.schema, text));
                    }
                });
            } catch (Throwable e) {
                logFailure("Failed to infer", e);
                run.failOrCancel(e);
            } finally {
                run.forget();
            }
        });
    }

    // ---- the request

    /**
     * What stays the same through the rounds of a request: with functions the model is asked again after each call.
     */
    private final class Run {
        final Protocol protocol;
        final FunctionSet functions;
        final JsonNode schema;
        final Outcome outcome;
        final CancellationToken token = new CancellationToken();
        // the request ends on other threads, the user it counts for is the one who started it
        final RequestAttributes attributes = ThreadCopyRequestAttributes.create();
        // read in the thread that starts the request: it is not metered inside an UnmeteredScope (the help assistant)
        final boolean metered = !UnmeteredScope.isActive();
        // the book the request is made for, read in the same thread (see UsageContext)
        final Long book = UsageContext.current();
        // read in the same thread: the header that tells a server with a prompt cache what kind of work this is
        final String sessionId = sessionIds.get();

        // the response that is being read: closing it cancels the HTTP call, also while a read waits for the next chunk
        private volatile StreamResponse<?> current;

        Run(Protocol protocol, FunctionSet functions, JsonNode schema, Outcome outcome) {
            this.protocol = protocol;
            this.functions = functions;
            this.schema = schema;
            this.outcome = outcome;
            token.onCancel(this::closeCurrent);
        }

        void track(StreamResponse<?> stream) {
            current = stream;
            if (token.isCancelled()) {
                closeCurrent();
            }
        }

        private void closeCurrent() {
            StreamResponse<?> stream = current;
            if (stream != null) {
                try {
                    stream.close();
                } catch (RuntimeException e) {
                    log.trace("Error closing the Anthropic stream", e);
                }
            }
        }

        /** The HTTP call of the round is over. */
        void forget() {
            current = null;
        }

        /** The request failed: because the user cancelled it (the call was cut short), or for real. */
        void failOrCancel(Throwable e) {
            if (token.isCancelled()) {
                try {
                    outcome.cancel().run();
                } catch (Exception ex) {
                    log.error("Error during callback.onCancel", ex);
                }
            } else {
                fail(outcome, e);
            }
        }
    }

    /** Opens the stream of the request and has the run keep it, so that a cancellation can close it. */
    private StreamResponse<RawMessageStreamEvent> open(AnthropicClient sdk, Run run, MessageCreateParams params) {
        StreamResponse<RawMessageStreamEvent> stream = sdk.messages().createStreaming(params);
        run.track(stream);
        return stream;
    }

    /**
     * Streams the request to its end and returns the whole answer.
     *
     * @param pieces gets the text and the reasoning as they arrive, null for none
     */
    private Answer collect(AnthropicClient sdk, Run run, MessageCreateParams params, Consumer<ChunkPiece> pieces) {
        Answer answer = new Answer();
        try (StreamResponse<RawMessageStreamEvent> stream = open(sdk, run, params)) {
            for (RawMessageStreamEvent event : (Iterable<RawMessageStreamEvent>) stream::iterator) {
                if (run.token.isCancelled()) {
                    break;
                }
                for (ChunkPiece piece : answer.absorb(event)) {
                    if (pieces != null) {
                        pieces.accept(piece);
                    }
                }
            }
        }
        if (answer.message == null && !run.token.isCancelled()) {
            throw new InferenceException(Type.CONNECTION, null, T.localized("The connection to Anthropic ended before the answer was complete"), null);
        }
        return answer;
    }

    private MessageCreateParams request(Run run, String system, List<Turn> turns, boolean noFunctionCalls) {
        MessageCreateParams.Builder request = MessageCreateParams.builder().model(model())
                .maxTokens((long) responseTokens(run.protocol));
        new Prepared(system, turns).into(request);
        if (run.sessionId != null) {
            request.putAdditionalHeader(InferenceSession.HEADER, run.sessionId);
        }

        ThinkingConfigParam thinking = thinking(run.protocol);
        OutputConfig.Builder output = OutputConfig.builder();
        boolean outputSet = false;
        if (thinking != null) {
            request.thinking(thinking);
            OutputConfig.Effort effort = effort();
            if (effort != null && !budgetThinking()) {
                output.effort(effort);
                outputSet = true;
            }
        }
        if (run.schema != null) {
            output.format(AnthropicMessages.outputFormat(run.schema));
            outputSet = true;
        }
        if (outputSet) {
            request.outputConfig(output.build());
        }

        if (run.functions != null) {
            // the tools stay in the final round too: the conversation contains their calls
            for (FunctionDefinition function : run.functions.getDefinitions()) {
                request.addTool(AnthropicMessages.tool(function));
            }
            if (noFunctionCalls) {
                request.toolChoice(ToolChoiceNone.builder().build());
            }
        }
        return request.build();
    }

    private int responseTokens(Protocol protocol) {
        int tokens = TokenLimits.responseTokens(ai, protocol);
        return tokens > 0 ? tokens : DEFAULT_RESPONSE_TOKENS;
    }

    private boolean budgetThinking() {
        return BUDGET_THINKING.matcher(settings.model()).matches();
    }

    /**
     * Thinking, when the AI has reasoning on. Claude 4.6 and later think adaptively and show a summary of it, the earlier models
     * think with a budget of tokens that depends on the effort. With reasoning off nothing is sent: the models that can not
     * think without it (Opus 5.5) think as they do by default and the others do not think; their thinking is not shown.
     */
    private ThinkingConfigParam thinking(Protocol protocol) {
        ReasoningEffort effort = ai.getReasoningEffort();
        if (!Boolean.TRUE.equals(ai.getEnabledReasoning()) || effort == null || effort == ReasoningEffort.NONE) {
            return null;
        }
        if (budgetThinking()) {
            long budget = switch (effort) {
                case LOW -> 1024;
                case MEDIUM -> 8192;
                default -> 24576;
            };
            // the budget has to be below the response size
            budget = Math.min(budget, responseTokens(protocol) - 1L);
            return budget < 1024 ? null : ThinkingConfigParam.ofEnabled(ThinkingConfigEnabled.builder().budgetTokens(budget).build());
        }
        return ThinkingConfigParam.ofAdaptive(ThinkingConfigAdaptive.builder().display(ThinkingConfigAdaptive.Display.SUMMARIZED).build());
    }

    private OutputConfig.Effort effort() {
        ReasoningEffort effort = ai.getReasoningEffort();
        if (effort == null) {
            return null;
        }
        return switch (effort) {
            case LOW -> OutputConfig.Effort.LOW;
            case MEDIUM -> OutputConfig.Effort.MEDIUM;
            case HIGH -> OutputConfig.Effort.HIGH;
            default -> null;
        };
    }

    private String model() {
        if (settings.model().isBlank()) {
            throw new InferenceException(Type.MODEL_NOT_FOUND, null, T.localized("No model is set"), null);
        }
        return settings.model();
    }

    /**
     * The SDK client of the AI, created on first use. It keeps HTTP connections and threads, so it is closed with the
     * service ({@link #close()}) when the extension is unloaded.
     */
    private synchronized AnthropicClient client() {
        if (closed) {
            throw new InferenceException(Type.EXTENSION_UNAVAILABLE, null, null,
                    new IllegalStateException("The extension " + AnthropicFactory.EXTENSION_ID + " was unloaded"));
        }
        if (client == null) {
            if (settings.apiKey().isBlank()) {
                throw new InferenceException(Type.AUTHENTICATION, null, T.localized("No API key is set"), null);
            }
            Duration timeout = Duration.ofSeconds(settings.timeoutSeconds());
            // read is the wait for the first text and between the chunks; the whole request limit must not cut a long
            // story in the middle, so it is much longer
            AnthropicOkHttpClient.Builder builder = AnthropicOkHttpClient.builder()
                    .apiKey(settings.apiKey())
                    .maxRetries(settings.maxRetries())
                    .timeout(Timeout.builder()
                            .connect(timeout.compareTo(Duration.ofSeconds(30)) < 0 ? timeout : Duration.ofSeconds(30))
                            .read(timeout)
                            .write(timeout)
                            .request(timeout.compareTo(Duration.ofHours(1)) > 0 ? timeout : Duration.ofHours(1))
                            .build());
            if (!settings.baseUrl().isBlank()) {
                builder.baseUrl(settings.baseUrl());
            }
            client = builder.build();
        }
        return client;
    }

    /**
     * Ends the service: the client is closed, the requests that are running fail. The extension was unloaded.
     */
    @Override
    public synchronized void close() {
        closed = true;
        if (client != null) {
            try {
                client.close();
            } catch (RuntimeException e) {
                log.debug("Error closing the Anthropic client", e);
            }
            client = null;
        }
    }

    // ---- functions

    /**
     * The model ended its answer with calls: runs them as a chain, one after the other, each on a thread of its own,
     * and asks the model again with the results when the last has answered. A call finishes whenever and from
     * whichever thread its function says, nothing here waits for it. A call that fails is not an error of the request,
     * the model gets the message and can correct itself.
     */
    private void callFunctions(Run run, Outcome outcome, List<Turn> turns, Answer answer, Consumer<List<Turn>> nextRound) {
        List<Turn> next = new ArrayList<>(turns);
        // what the model said in this round goes back in front of the results, with its signed thinking
        Turn said = new Turn(true);
        for (ContentBlock block : answer.message.content()) {
            said.blocks.add(block.toParam());
        }
        next.add(said);
        Turn results = new Turn(false);
        next.add(results);

        BooleanSupplier stopped = () -> outcome.dead().getAsBoolean() || run.token.isCancelled();
        Executor executor = task -> CompletableFuture.runAsync(() -> {
            try (InRequestScope _ = new InRequestScope(run.attributes)) {
                task.run();
            }
        });

        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (LLMToolCall call : answer.calls()) {
            chain = chain.thenCompose(_ -> {
                if (stopped.getAsBoolean()) {
                    throw new CancellationException();
                }
                CompletableFuture<String> reply = run.functions.call(call.getName(), call.getArguments(), executor, stopped);
                // a call that never answers must not outlive the request
                Runnable unregister = run.token.onCancel(() -> reply.completeExceptionally(new CancellationException()));
                return reply.handle((result, error) -> {
                    unregister.run();
                    if (error instanceof CompletionException wrapped && wrapped.getCause() != null) {
                        error = wrapped.getCause();
                    }
                    if (error instanceof CancellationException) {
                        throw (CancellationException) error;
                    }
                    boolean failed = error != null;
                    String content = result;
                    if (failed) {
                        content = "Error: " + error.getMessage();
                        if (error instanceof FunctionCallException e && e.getKind() != FunctionCallException.Kind.FAILED) {
                            log.info("Call of function {} refused: {}", call.getName(), e.getMessage());
                        } else {
                            log.warn("Function {} called by the model failed", call.getName(), error);
                        }
                    }
                    results.results.add(AnthropicMessages.resultBlock(call.getId(), content));
                    try {
                        outcome.observer().onFunctionCall(call, content, failed);
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                    return null;
                });
            });
        }
        chain.whenComplete((_, error) -> {
            try {
                if (error instanceof CompletionException wrapped && wrapped.getCause() != null) {
                    error = wrapped.getCause();
                }
                if (error instanceof CancellationException || (error == null && stopped.getAsBoolean())) {
                    outcome.cancel().run();
                } else if (error != null) {
                    throw error;
                } else {
                    nextRound.accept(next);
                }
            } catch (Throwable e) {
                log.error("Error calling the functions of the model", e);
                fail(outcome, e);
            }
        });
    }

    // ---- usage

    /**
     * Counts a request for the user whose thread runs it, whoever started it (generation, summary, an extension): what
     * Anthropic reported, otherwise our count of the messages and of the text that arrived (a stopped request has no
     * report).
     */
    private TokenUsage recordUsage(RequestAttributes attributes, Long book, List<Turn> turns, TokenUsage reported, String generated) {
        // the book of the request, not of the thread that runs it now
        try (InRequestScope _ = new InRequestScope(attributes); UsageContext _ = UsageContext.open(book)) {
            long prefill;
            long generatedTokens;
            if (reported != null) {
                prefill = reported.promptTokens();
                generatedTokens = reported.generatedTokens();
            } else {
                prefill = approximateTokens(turns);
                generatedTokens = tokenizerService.countTokensApprox(generated);
            }
            usageService.record(ai, prefill, generatedTokens);
            return new TokenUsage(prefill, generatedTokens, reported == null ? -1 : reported.cachedTokens(),
                    reported == null ? -1 : reported.thoughtTokens());
        } catch (Exception e) {
            log.warn("Failed to count the usage of AI {}", ai.getId(), e);
            return null;
        }
    }

    /** Our count of the tokens of the payload, Anthropic's own differs a little. */
    private long approximatePromptTokens(List<LLMChatMessage> payload) throws Exception {
        long tokens = 0;
        for (LLMChatMessage message : payload) {
            tokens += tokenizerService.countTokensApprox(StringUtils.defaultString(message.getContent()));
            for (LLMToolCall call : message.getToolCalls()) {
                tokens += tokenizerService.countTokensApprox(call.getName() + call.getArguments());
            }
        }
        return tokens;
    }

    private long approximateTokens(List<Turn> turns) throws Exception {
        long tokens = 0;
        for (Turn turn : turns) {
            for (ContentBlockParam block : turn.content()) {
                if (block.isText()) {
                    tokens += tokenizerService.countTokensApprox(block.asText().text());
                } else if (block.isToolUse()) {
                    ToolUseBlockParam call = block.asToolUse();
                    tokens += tokenizerService.countTokensApprox(call.name() + call.input());
                } else if (block.isToolResult()) {
                    ToolResultBlockParam result = block.asToolResult();
                    tokens += tokenizerService.countTokensApprox(result.content().filter(ToolResultBlockParam.Content::isString)
                            .map(ToolResultBlockParam.Content::asString).orElse(""));
                }
            }
        }
        return tokens;
    }

    /**
     * Token counts Anthropic reported for a request. The tokens of the input that was read from the cache or written to it are
     * part of the prompt, and the thinking is generated: both are billed as such.
     */
    record TokenUsage(long promptTokens, long generatedTokens, long cachedTokens, long thoughtTokens) {

        static TokenUsage of(Usage usage) {
            long cacheRead = usage.cacheReadInputTokens().orElse(0L);
            long prompt = usage.inputTokens() + cacheRead + usage.cacheCreationInputTokens().orElse(0L);
            return new TokenUsage(prompt, usage.outputTokens(), usage.cacheReadInputTokens().orElse(-1L), -1);
        }
    }

    // ---- answers

    private static RuntimeException runtime(RuntimeException e) {
        Throwable translated = AnthropicErrors.translate(e);
        return translated instanceof RuntimeException r ? r : e;
    }

    /** What the provider answered is no bug, so it is a line; anything else gets its stack trace. */
    private void logFailure(String what, Throwable e) {
        if (AnthropicErrors.translate(e) instanceof InferenceException) {
            log.warn("{}: {}", what, e.getMessage());
            log.debug("Stack trace of the failure", e);
        } else {
            log.error("{}: {}", what, e.getMessage(), e);
        }
    }

    private void fail(Outcome outcome, Throwable e) {
        try {
            outcome.error().accept(AnthropicErrors.translate(e));
        } catch (Exception ex) {
            log.error("Error during callback.onError", ex);
        }
    }

    /**
     * What the events of one response hold: the text, the reasoning, the calls and how it ended. The message with its content
     * blocks (signed thinking, text, calls) is put together by the SDK's accumulator and is there when the stream ended.
     */
    static class Answer {
        private final com.anthropic.helpers.MessageAccumulator accumulator = com.anthropic.helpers.MessageAccumulator.create();
        /** The text of the answer, without the reasoning. */
        final StringBuilder text = new StringBuilder();
        /** The summary of the thinking. */
        final StringBuilder reasoning = new StringBuilder();
        /** Everything that arrived, reasoning included. */
        final StringBuilder received = new StringBuilder();
        /** The whole message, once the stream ended; null before and when it was cut short. */
        Message message;

        /** Reads the event; returns the pieces of text it brought. */
        List<ChunkPiece> absorb(RawMessageStreamEvent event) {
            accumulator.accumulate(event);
            if (event.isMessageStop()) {
                message = accumulator.message();
            } else if (event.isContentBlockDelta()) {
                RawContentBlockDelta delta = event.asContentBlockDelta().delta();
                if (delta.isText()) {
                    String content = delta.asText().text();
                    if (!content.isEmpty()) {
                        text.append(content);
                        received.append(content);
                        return List.of(new ChunkPiece(false, content));
                    }
                } else if (delta.isThinking()) {
                    String content = delta.asThinking().thinking();
                    if (!content.isEmpty()) {
                        reasoning.append(content);
                        received.append(content);
                        return List.of(new ChunkPiece(true, content));
                    }
                } else if (delta.isInputJson()) {
                    received.append(delta.asInputJson().partialJson());
                }
            }
            return List.of();
        }

        /** What Anthropic reported for the request; null for an answer that did not end, nothing was billed that we know of. */
        TokenUsage reported() {
            return message == null ? null : TokenUsage.of(message.usage());
        }

        /** The calls the model made, in order; empty for an answer that did not end. */
        List<LLMToolCall> calls() {
            List<LLMToolCall> calls = new ArrayList<>();
            if (message != null) {
                for (ContentBlock block : message.content()) {
                    block.toolUse().ifPresent(call -> calls.add(LLMToolCall.of(AnthropicMessages.callId(call), call.name(),
                            AnthropicMessages.argumentsJson(call))));
                }
            }
            return calls;
        }

        /** {@code end_turn}, {@code tool_use}, {@code max_tokens}, {@code refusal}...; null when the stream did not end. */
        String stopReason() {
            return message == null ? null : message.stopReason().map(StopReason::asString).orElse(null);
        }

        /** The model answered nothing, and not because it had nothing to say: it was stopped. */
        boolean isEmptyAndStopped() {
            if (!received.isEmpty()) {
                return false;
            }
            String reason = stopReason();
            return reason != null && !"end_turn".equals(reason) && !"stop_sequence".equals(reason) && !"tool_use".equals(reason);
        }

        InferenceException stopped() {
            String explanation = message == null ? null
                    : message.stopDetails().flatMap(details -> details.explanation()).orElse(null);
            return AnthropicErrors.noAnswer(stopReason(), explanation);
        }
    }

    record ChunkPiece(boolean reasoning, String text) {}

    // ---- streamed answers

    private interface FunctionObserver {
        void onFunctionCall(LLMToolCall call, String result, boolean failed) throws Exception;
    }

    private interface Failure<T> {
        void accept(T value) throws Exception;
    }

    private interface Signal {
        void run() throws Exception;
    }

    private record Outcome(BooleanSupplier dead, FunctionObserver observer, Signal cancel, Failure<Throwable> error) {}

    /**
     * Runs the functions the model called at the end of a stream and starts the next request.
     */
    private interface FunctionCaller {

        void call(Answer answer);
    }

    private interface UsageRecorder {

        /**
         * @param reported  what Anthropic reported, null if it didn't
         * @param generated all the text that arrived, reasoning included
         */
        void record(TokenUsage reported, String generated);
    }

    /**
     * Hands the chunks of the stream to the callback one at a time: the callback asks for the next with
     * {@link #continueInference()}, so a slow consumer slows the stream down instead of piling the text up.
     */
    private final class AnthropicStreamController implements InferenceAsyncController {

        private final StreamResponse<RawMessageStreamEvent> stream;
        private final Iterator<RawMessageStreamEvent> iterator;
        private final InferenceAsyncCallback callback;
        private final Run run;
        private final Queue<ChunkPiece> pending = new LinkedList<>();
        private final UsageRecorder usageRecorder;
        private final FunctionCaller functionCaller;
        private final Answer answer = new Answer();
        private boolean completed = false;
        private boolean usageRecorded;

        AnthropicStreamController(StreamResponse<RawMessageStreamEvent> stream, InferenceAsyncCallback callback, Run run,
                                  UsageRecorder usageRecorder, FunctionCaller functionCaller) {
            this.stream = stream;
            this.iterator = stream.iterator();
            this.callback = callback;
            this.run = run;
            this.usageRecorder = usageRecorder;
            this.functionCaller = functionCaller;
        }

        @Override
        public void continueInference() {
            AsyncRunnableWrapper wrapper = new AsyncRunnableWrapper(configuration);
            CompletableFuture.runAsync(() -> this.processNext(wrapper));
        }

        @Override
        public synchronized void terminateInference() {
            if (completed) {
                return;
            }
            completed = true;
            pending.clear();
            closeStream();
            recordUsage();
            log.debug("Inference execution terminated by controller request.");
        }

        private synchronized void processNext(AsyncRunnableWrapper wrapper) {
            if (completed) {
                return;
            }

            try {
                wrapper.run(() -> {
                    if (!pending.isEmpty()) {
                        deliver(pending.poll());
                        return;
                    }

                    while (iterator.hasNext()) {
                        if (completed) {
                            return;
                        }
                        if (callback.isDead() || run.token.isCancelled()) {
                            cancelled();
                            return;
                        }

                        pending.addAll(answer.absorb(iterator.next()));
                        if (!pending.isEmpty()) {
                            deliver(pending.poll());
                            return;
                        }
                    }

                    if (callback.isDead() || run.token.isCancelled()) {
                        // the stream ended, or was cut short, after the user stopped it
                        cancelled();
                        return;
                    }
                    completed = true;
                    closeStream();
                    recordUsage();
                    if (answer.message == null) {
                        // the connection ended without the end of the message: what arrived is not a whole answer
                        fail(run.outcome, new InferenceException(Type.CONNECTION, null,
                                T.localized("The connection to Anthropic ended before the answer was complete"), null));
                    } else if (answer.isEmptyAndStopped()) {
                        // nothing came and the model was stopped: that is not an empty answer, the user has to know why
                        fail(run.outcome, answer.stopped());
                    } else if (!answer.calls().isEmpty() && functionCaller != null) {
                        try {
                            functionCaller.call(answer);
                        } catch (Throwable e) {
                            log.error("Error calling the functions of the model", e);
                            fail(run.outcome, e);
                        }
                    } else {
                        try {
                            callback.onCompletion();
                        } catch (Throwable e) {
                            // completed is already set, so the outer catch would swallow this and the caller would wait forever
                            log.error("Error invoking callback.onCompletion", e);
                            fail(run.outcome, e);
                        }
                    }
                });
            } catch (Throwable e) {
                if (!completed) {
                    completed = true;
                    closeStream();
                    if (run.token.isCancelled()) {
                        // the read failed because the stream was closed to cancel it
                        recordUsage();
                        try {
                            callback.onCancel();
                        } catch (Exception ex) {
                            log.error("Error invoking callback.onCancel", ex);
                        }
                        return;
                    }
                    if (!answer.received.isEmpty()) {
                        // it answered before it failed, so that was billed; a failure before any text is not counted
                        recordUsage();
                    }
                    fail(run.outcome, e);
                }
            }
        }

        private void deliver(ChunkPiece piece) throws Exception {
            callback.onChunk(this, piece.reasoning() ? ChunkType.REASONING : ChunkType.RESPONSE, piece.text());
        }

        private void cancelled() throws Exception {
            completed = true;
            closeStream();
            recordUsage();
            callback.onCancel();
        }

        /** Counts the request, once, whichever way it ends. */
        private void recordUsage() {
            if (usageRecorded) {
                return;
            }
            usageRecorded = true;
            TokenUsage reported = answer.reported();
            usageRecorder.record(reported, answer.received.toString());
        }

        private void closeStream() {
            run.forget();
            try {
                stream.close();
            } catch (Exception e) {
                log.trace("Error closing the Anthropic stream", e);
            }
        }
    }
}
