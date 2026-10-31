package com.github.enerccio.marginalia.extensions.googlegenai;

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
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.*;
import okhttp3.Call;
import okhttp3.OkHttpClient;
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
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.enerccio.marginalia.extensions.googlegenai.GoogleGenAiLocalization.T;

/**
 * The inference service of a Google Gemini AI, over the Google Gen AI Java SDK. It does what the service of an OpenAI
 * compatible AI does: streams with the reasoning (the thought summaries of Gemini) apart from the answer, lets the model
 * call the functions, answers in a JSON schema, counts the usage of the user and honors the limits an administrator set.
 * <p>
 * The tokens of the texts are counted by Google ({@code models.countTokens}, through the {@link TokenizerResolver} of the
 * {@link GoogleGenAiFactory} so that the application caches the way of counting and counts approximately inside a transaction
 * that holds the database write lock). Each text is asked once (the answers are remembered), and after a failure the service
 * counts approximately for a minute instead of waiting for Google on every text. The usage is what Google reports.
 */
@Configurable
public class GoogleGenAiInferenceService implements InferenceService, AutoCloseable {

    @Autowired
    private TokenizerService tokenizerService;

    @Autowired
    private Configuration configuration;

    @Autowired
    private UsageService usageService;

    @Autowired
    private AILimitService limitService;

    private final ExtensionAI ai;
    private final GoogleGenAiSettings settings;
    private final Logger log;
    private final Supplier<String> sessionIds;

    /**
     * The header that names the request on its way through the client, so that the HTTP call of a request can be
     * found (by the interceptor) and cancelled. Removed before the request is sent.
     */
    private static final String REQUEST_HEADER = "X-Marginalia-Request";

    // the HTTP calls that are running, by the id of the request they belong to
    private final Map<String, Call> activeCalls = new ConcurrentHashMap<>();

    private static final int COUNT_CACHE_SIZE = 512;
    private static final long COUNT_PAUSE_MILLIS = 60_000;

    // the tokens of the texts Google counted, by the hash of the text (the least recently used go first)
    private final Map<String, Long> countCache = Collections.synchronizedMap(new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
            return size() > COUNT_CACHE_SIZE;
        }
    });

    // after a failed count Google is not asked until then, the estimate is used
    private volatile long countPausedUntil;

    private Client client;
    private boolean closed;

    GoogleGenAiInferenceService(ExtensionAI ai, GoogleGenAiSettings settings, Logger log) {
        this(ai, settings, log, () -> null);
    }

    /**
     * @param sessionIds the session id of the request that starts on the calling thread, see
     *                   {@code InferenceServices.currentSessionId()}
     */
    GoogleGenAiInferenceService(ExtensionAI ai, GoogleGenAiSettings settings, Logger log, Supplier<String> sessionIds) {
        this.ai = ai;
        this.settings = settings;
        this.log = log;
        this.sessionIds = sessionIds;
    }

    // ---- models and connection

    @Override
    public List<String> getModels() {
        try {
            List<String> names = new ArrayList<>();
            for (Model model : client().models.list(ListModelsConfig.builder().pageSize(100).queryBase(true).build())) {
                List<String> actions = model.supportedActions().orElse(List.of());
                // not everything Google lists writes text (embeddings, images...)
                if (!actions.isEmpty() && !actions.contains("generateContent")) {
                    continue;
                }
                model.name().map(GoogleGenAiSettings::normalizeModel).ifPresent(names::add);
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
            client().models.generateContent(model(), "Say OK.", GenerateContentConfig.builder().maxOutputTokens(16).build());
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
     * The count of Google's own tokenizer, for {@link GoogleGenAiFactory}'s resolver: the application calls it through
     * {@link TokenizerService#countTokens}, which falls back to the estimate when this throws.
     */
    long countTokensOnline(String text) {
        if (System.currentTimeMillis() < countPausedUntil) {
            throw new IllegalStateException("Counting with Google is paused after a failure");
        }
        String hash = sha256(text);
        Long known = countCache.get(hash);
        if (known != null) {
            return known;
        }
        try {
            long tokens = client().models.countTokens(model(), text, CountTokensConfig.builder().build()).totalTokens()
                    .orElseThrow(() -> new IllegalStateException("Google did not tell the number of tokens"));
            countCache.put(hash, tokens);
            return tokens;
        } catch (RuntimeException e) {
            countPausedUntil = System.currentTimeMillis() + COUNT_PAUSE_MILLIS;
            logFailure("Counting the tokens with Google failed, counting approximately for a minute", e);
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
     * One request for an agent: the model is asked once, not streamed, and what it answered is handed back, the functions it asked
     * for are <b>not</b> called (the runner of the agent calls them and asks again).
     */
    @Override
    public CancellationToken round(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions,
                                   InferenceRoundCallback callback) throws Exception {
        if (!UnmeteredScope.isActive()) {
            limitService.check(ai, approximatePromptTokens(payload), TokenLimits.responseTokens(ai, protocol));
        }
        // the history of an agent is rebuilt from its transcript: a call that has no thought signature of its own (made before it was
        // kept, or by another model) gets the value Google documents for it, which Gemini 3 insists on
        GoogleGenAiMessages.Prepared prepared = GoogleGenAiMessages.prepare(payload,
                settings.model().toLowerCase(Locale.ROOT).contains("gemini-3"));
        boolean functionsOffered = functions != null && !functions.isEmpty();
        Run run = new Run(protocol, functionsOffered ? functions : null, null, prepared.systemInstruction(),
                new Outcome(callback::isDead, (call, result, failed) -> {}, callback::onCancel, callback::onError));
        Client sdk = client();
        String model = model();
        GenerateContentConfig config = config(run, false);

        AsyncRunnableWrapper wrapper = new AsyncRunnableWrapper(configuration);
        CompletableFuture.runAsync(() -> {
            try {
                wrapper.run(() -> {
                    if (run.outcome.dead().getAsBoolean() || run.token.isCancelled()) {
                        callback.onCancel();
                        return;
                    }
                    long began = System.nanoTime();
                    GenerateContentResponse response = sdk.models.generateContent(model, prepared.contents(), config);
                    long millis = (System.nanoTime() - began) / 1_000_000;
                    Answer answer = new Answer();
                    List<ChunkPiece> pieces = answer.absorb(response, functionsOffered);
                    TokenUsage counted = run.metered
                            ? recordUsage(run.attributes, run.book, prepared.contents(), answer.reported, answer.received.toString()) : null;
                    if (run.outcome.dead().getAsBoolean() || run.token.isCancelled()) {
                        callback.onCancel();
                    } else if (answer.calls.isEmpty() && answer.isEmptyAndStopped()) {
                        throw answer.stopped();
                    } else {
                        String reasoning = pieces.stream().filter(ChunkPiece::reasoning).map(ChunkPiece::text)
                                .collect(java.util.stream.Collectors.joining());
                        callback.onRound(answer.text.toString(), reasoning.isEmpty() ? null : reasoning, answer.calls,
                                roundUsage(counted != null ? counted : answer.reported, answer, millis));
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
     * What a round used and how it went. The answer is not streamed, so the time of the whole request is the one thing the
     * provider's side of the clock can tell: it is given as the time of writing, the wait for the first word is not known.
     */
    static RoundUsage roundUsage(TokenUsage reported, Answer answer, long millis) {
        long prompt = reported == null ? 0 : reported.promptTokens();
        long generated = reported == null ? 0 : reported.generatedTokens();
        String finish = answer.finishReason == null ? null
                : "MAX_TOKENS".equals(answer.finishReason) ? "length"
                : "STOP".equals(answer.finishReason) ? (answer.calls.isEmpty() ? "stop" : "tool_calls")
                : answer.finishReason.toLowerCase(Locale.ROOT);
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
            limitService.check(ai, approximatePromptTokens(payload), TokenLimits.responseTokens(ai, protocol));
        }
        GoogleGenAiMessages.Prepared prepared = GoogleGenAiMessages.prepare(payload);
        Run run = new Run(protocol, functions != null && !functions.isEmpty() ? functions : null, schema,
                prepared.systemInstruction(), new Outcome(callback::isDead, callback::onFunctionCall, callback::onCancel, callback::onError));
        startStreamRound(run, callback, prepared.contents(), 0);
        return run.token;
    }

    /**
     * Sends the conversation and streams the answer. Building the request fails here, in the caller's thread, for the
     * first round; the rest of the round runs asynchronously.
     */
    private void startStreamRound(Run run, InferenceAsyncCallback callback, List<Content> contents, int round) {
        boolean functionsOffered = run.functions != null;
        boolean finalRound = round >= MAX_FUNCTION_ROUNDS;
        Client sdk = client();
        String model = model();
        GenerateContentConfig config = config(run, functionsOffered && finalRound);

        AsyncRunnableWrapper wrapper = new AsyncRunnableWrapper(configuration);
        CompletableFuture.runAsync(() -> {
            try {
                wrapper.run(() -> {
                    if (run.token.isCancelled()) {
                        callback.onCancel();
                        return;
                    }
                    ResponseStream<GenerateContentResponse> stream = sdk.models.generateContentStream(model, contents, config);
                    GoogleStreamController controller = new GoogleStreamController(stream, callback, run,
                            (reported, generated) -> {
                                if (run.metered) {
                                    recordUsage(run.attributes, run.book, contents, reported, generated);
                                }
                            },
                            functionsOffered && !finalRound
                                    ? (answer, modelContent, calls) -> callFunctions(run, run.outcome, contents, modelContent, calls,
                                    next -> startStreamRound(run, callback, next, round + 1)) : null);
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
            limitService.check(ai, approximatePromptTokens(payload), TokenLimits.responseTokens(ai, protocol));
        }
        GoogleGenAiMessages.Prepared prepared = GoogleGenAiMessages.prepare(payload);
        Run run = new Run(protocol, functions != null && !functions.isEmpty() ? functions : null, schema,
                prepared.systemInstruction(), new Outcome(callback::isDead, callback::onFunctionCall, callback::onCancel, callback::onError));
        inferRound(run, callback, prepared.contents(), 0);
        return run.token;
    }

    /**
     * One request of {@link #infer}. Building it fails here, in the caller's thread, for the first round; the rest runs
     * asynchronously.
     */
    private void inferRound(Run run, InferenceResultCallback callback, List<Content> contents, int round) {
        boolean functionsOffered = run.functions != null;
        boolean finalRound = round >= MAX_FUNCTION_ROUNDS;
        Client sdk = client();
        String model = model();
        GenerateContentConfig config = config(run, functionsOffered && finalRound);

        AsyncRunnableWrapper wrapper = new AsyncRunnableWrapper(configuration);
        CompletableFuture.runAsync(() -> {
            try {
                wrapper.run(() -> {
                    if (run.outcome.dead().getAsBoolean() || run.token.isCancelled()) {
                        callback.onCancel();
                        return;
                    }
                    GenerateContentResponse response = sdk.models.generateContent(model, contents, config);
                    Answer answer = new Answer();
                    answer.absorb(response, functionsOffered && !finalRound);
                    if (run.metered) {
                        recordUsage(run.attributes, run.book, contents, answer.reported, answer.received.toString());
                    }

                    // the request can not be taken back once sent, but a canceled one gets no result
                    if (run.outcome.dead().getAsBoolean() || run.token.isCancelled()) {
                        callback.onCancel();
                    } else if (!answer.calls.isEmpty()) {
                        callFunctions(run, run.outcome, contents, answer.modelContent(), answer.calls,
                                next -> inferRound(run, callback, next, round + 1));
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
        final Content systemInstruction;
        final Outcome outcome;
        final CancellationToken token = new CancellationToken();
        // the request ends on other threads, the user it counts for is the one who started it
        final RequestAttributes attributes = ThreadCopyRequestAttributes.create();
        // read in the thread that starts the request: it is not metered inside an UnmeteredScope (the help assistant)
        final boolean metered = !UnmeteredScope.isActive();
        // the book the request is made for, read in the same thread (see UsageContext)
        final Long book = UsageContext.current();
        final String id = UUID.randomUUID().toString();
        // read in the same thread: the header that tells a server with a prompt cache what kind of work this is
        final String sessionId = sessionIds.get();

        Run(Protocol protocol, FunctionSet functions, JsonNode schema, Content systemInstruction, Outcome outcome) {
            this.protocol = protocol;
            this.functions = functions;
            this.schema = schema;
            this.systemInstruction = systemInstruction;
            this.outcome = outcome;
            // a read that waits for the next chunk (or for the whole answer) is cut short by cancelling the HTTP call
            token.onCancel(() -> {
                Call call = activeCalls.get(id);
                if (call != null) {
                    call.cancel();
                }
            });
        }

        /** The HTTP call of the round is over. */
        void forget() {
            activeCalls.remove(id);
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

    private GenerateContentConfig config(Run run, boolean noFunctionCalls) {
        Protocol protocol = run.protocol;
        Map<String, String> headers = run.sessionId == null ? Map.of(REQUEST_HEADER, run.id)
                : Map.of(REQUEST_HEADER, run.id, InferenceSession.HEADER, run.sessionId);
        GenerateContentConfig.Builder config = GenerateContentConfig.builder()
                .httpOptions(HttpOptions.builder().headers(headers).build());
        if (run.systemInstruction != null) {
            config.systemInstruction(run.systemInstruction);
        }

        int responseTokens = TokenLimits.responseTokens(ai, protocol);
        if (responseTokens > 0) {
            config.maxOutputTokens(responseTokens);
        }
        if (protocol != null) {
            if (protocol.getTemperatureEnabled() && protocol.getTemperature() != null) {
                config.temperature(protocol.getTemperature().floatValue());
            }
            if (protocol.getTopPEnabled() && protocol.getTopP() != null) {
                config.topP(protocol.getTopP().floatValue());
            }
            if (protocol.getFrequencyPenaltyEnabled() && protocol.getFrequencyPenalty() != null) {
                config.frequencyPenalty(protocol.getFrequencyPenalty().floatValue());
            }
            if (protocol.getPresencePenaltyEnabled() && protocol.getPresencePenalty() != null) {
                config.presencePenalty(protocol.getPresencePenalty().floatValue());
            }
        }

        ThinkingConfig thinking = thinkingConfig();
        if (thinking != null) {
            config.thinkingConfig(thinking);
        }
        if (settings.safetyOff()) {
            config.safetySettings(safetyOff());
        }

        if (run.schema != null) {
            config.responseMimeType("application/json");
            config.responseJsonSchema(GoogleGenAiMessages.toMap(run.schema));
        }

        if (run.functions != null) {
            List<FunctionDeclaration> declarations = new ArrayList<>();
            // the tools stay in the final round too: the conversation contains their calls
            for (FunctionDefinition function : run.functions.getDefinitions()) {
                FunctionDeclaration.Builder declaration = FunctionDeclaration.builder().name(function.getName())
                        .parametersJsonSchema(GoogleGenAiMessages.toMap(function.getParametersSchema()));
                if (StringUtils.isNotEmpty(function.getDescription())) {
                    declaration.description(function.getDescription());
                }
                declarations.add(declaration.build());
            }
            config.tools(List.of(Tool.builder().functionDeclarations(declarations).build()));
            if (noFunctionCalls) {
                config.toolConfig(ToolConfig.builder().functionCallingConfig(FunctionCallingConfig.builder()
                        .mode(new FunctionCallingConfigMode(FunctionCallingConfigMode.Known.NONE)).build()).build());
            }
        }
        return config.build();
    }

    /**
     * Thought summaries and the effort of the model, when the AI has reasoning on. Gemini 3 takes a level, the earlier
     * models a budget of tokens; without it the model thinks as it does by default, and shows nothing of it.
     */
    private ThinkingConfig thinkingConfig() {
        ReasoningEffort effort = ai.getReasoningEffort();
        if (!Boolean.TRUE.equals(ai.getEnabledReasoning()) || effort == null || effort == ReasoningEffort.NONE) {
            return null;
        }
        ThinkingConfig.Builder thinking = ThinkingConfig.builder().includeThoughts(true);
        if (settings.model().toLowerCase(Locale.ROOT).contains("gemini-3")) {
            thinking.thinkingLevel(new ThinkingLevel(switch (effort) {
                case LOW -> ThinkingLevel.Known.LOW;
                case MEDIUM -> ThinkingLevel.Known.MEDIUM;
                default -> ThinkingLevel.Known.HIGH;
            }));
        } else {
            thinking.thinkingBudget(switch (effort) {
                case LOW -> 1024;
                case MEDIUM -> 8192;
                default -> 24576;
            });
        }
        return thinking.build();
    }

    private static List<SafetySetting> safetyOff() {
        List<SafetySetting> filters = new ArrayList<>();
        for (HarmCategory.Known category : List.of(HarmCategory.Known.HARM_CATEGORY_HARASSMENT,
                HarmCategory.Known.HARM_CATEGORY_HATE_SPEECH, HarmCategory.Known.HARM_CATEGORY_SEXUALLY_EXPLICIT,
                HarmCategory.Known.HARM_CATEGORY_DANGEROUS_CONTENT)) {
            filters.add(SafetySetting.builder().category(new HarmCategory(category))
                    .threshold(new HarmBlockThreshold(HarmBlockThreshold.Known.BLOCK_NONE)).build());
        }
        return filters;
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
    private synchronized Client client() {
        if (closed) {
            throw new InferenceException(Type.EXTENSION_UNAVAILABLE, null, null,
                    new IllegalStateException("The extension " + GoogleGenAiFactory.EXTENSION_ID + " was unloaded"));
        }
        if (client == null) {
            if (settings.apiKey().isBlank()) {
                throw new InferenceException(Type.AUTHENTICATION, null, T.localized("No API key is set"), null);
            }
            Duration timeout = Duration.ofSeconds(settings.timeoutSeconds());
            // read is the wait for the first text and between the chunks; the whole request limit must not cut a long
            // story in the middle, so it is much longer
            OkHttpClient http = new OkHttpClient.Builder()
                    .connectTimeout(timeout.compareTo(Duration.ofSeconds(30)) < 0 ? timeout : Duration.ofSeconds(30))
                    .readTimeout(timeout)
                    .writeTimeout(timeout)
                    .callTimeout(timeout.compareTo(Duration.ofHours(1)) > 0 ? timeout : Duration.ofHours(1))
                    .addInterceptor(chain -> {
                        String id = chain.request().header(REQUEST_HEADER);
                        if (id == null) {
                            return chain.proceed(chain.request());
                        }
                        activeCalls.put(id, chain.call());
                        return chain.proceed(chain.request().newBuilder().removeHeader(REQUEST_HEADER).build());
                    })
                    .build();

            // attempts include the first request
            HttpOptions.Builder options = HttpOptions.builder()
                    .retryOptions(HttpRetryOptions.builder().attempts(settings.maxRetries() + 1).build());
            if (!settings.baseUrl().isBlank()) {
                options.baseUrl(settings.baseUrl());
            }
            client = Client.builder()
                    .apiKey(settings.apiKey())
                    .httpOptions(options.build())
                    .clientOptions(ClientOptions.builder().customHttpClient(http).build())
                    .build();
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
                log.debug("Error closing the Google Gen AI client", e);
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
     *
     * @param modelContent what the model said in this round, it goes back in front of the results (with the thought
     *                     signatures Gemini 3 needs to see)
     */
    private void callFunctions(Run run, Outcome outcome, List<Content> contents, Content modelContent, List<LLMToolCall> calls,
                               Consumer<List<Content>> nextRound) {
        List<Content> next = new ArrayList<>(contents);
        next.add(modelContent);
        List<Part> results = new ArrayList<>();

        BooleanSupplier stopped = () -> outcome.dead().getAsBoolean() || run.token.isCancelled();
        Executor executor = task -> CompletableFuture.runAsync(() -> {
            try (InRequestScope _ = new InRequestScope(run.attributes)) {
                task.run();
            }
        });

        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);
        for (LLMToolCall call : calls) {
            chain = chain.thenCompose(_ -> {
                if (stopped.getAsBoolean()) {
                    throw new CancellationException();
                }
                CompletableFuture<String> answer = run.functions.call(call.getName(), call.getArguments(), executor, stopped);
                // a call that never answers must not outlive the request
                Runnable unregister = run.token.onCancel(() -> answer.completeExceptionally(new CancellationException()));
                return answer.handle((result, error) -> {
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
                    results.add(GoogleGenAiMessages.responsePart(call.getId(), call.getName(), content));
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
                    // the answers to the calls of one message are one content
                    GoogleGenAiMessages.append(next, "user", results);
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
     * Google reported, otherwise our count of the contents and of the text that arrived (a stopped request has no
     * report).
     */
    private TokenUsage recordUsage(RequestAttributes attributes, Long book, List<Content> contents, TokenUsage reported, String generated) {
        // the book of the request, not of the thread that runs it now
        try (InRequestScope _ = new InRequestScope(attributes); UsageContext _ = UsageContext.open(book)) {
            long prefill;
            long generatedTokens;
            if (reported != null) {
                prefill = reported.promptTokens();
                generatedTokens = reported.generatedTokens();
            } else {
                prefill = approximateTokens(contents);
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

    /** Our count of the tokens of the payload, Google's own differs a little. */
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

    private long approximateTokens(List<Content> contents) throws Exception {
        long tokens = 0;
        for (Content content : contents) {
            for (Part part : content.parts().orElse(List.of())) {
                tokens += tokenizerService.countTokensApprox(part.text().orElse(""));
                if (part.functionCall().isPresent()) {
                    FunctionCall call = part.functionCall().get();
                    tokens += tokenizerService.countTokensApprox(call.name().orElse("") + GoogleGenAiMessages.argumentsJson(call));
                }
                if (part.functionResponse().isPresent()) {
                    tokens += tokenizerService.countTokensApprox(String.valueOf(part.functionResponse().get().response().orElse(null)));
                }
            }
        }
        return tokens;
    }

    /**
     * Token counts Google reported for a request. Thinking tokens are generated tokens: they are billed as such.
     */
    record TokenUsage(long promptTokens, long generatedTokens, long cachedTokens, long thoughtTokens) {

        TokenUsage(long promptTokens, long generatedTokens) {
            this(promptTokens, generatedTokens, -1, -1);
        }

        static TokenUsage of(GenerateContentResponseUsageMetadata usage) {
            long prompt = usage.promptTokenCount().orElse(0) + usage.toolUsePromptTokenCount().orElse(0);
            long generated = usage.candidatesTokenCount().orElse(0) + usage.thoughtsTokenCount().orElse(0);
            return new TokenUsage(prompt, generated, usage.cachedContentTokenCount().orElse(-1), usage.thoughtsTokenCount().orElse(-1));
        }
    }

    // ---- answers

    private static RuntimeException runtime(RuntimeException e) {
        Throwable translated = GoogleGenAiErrors.translate(e);
        return translated instanceof RuntimeException r ? r : e;
    }

    /** What the provider answered is no bug, so it is a line; anything else gets its stack trace. */
    private void logFailure(String what, Throwable e) {
        if (GoogleGenAiErrors.translate(e) instanceof InferenceException) {
            log.warn("{}: {}", what, e.getMessage());
            log.debug("Stack trace of the failure", e);
        } else {
            log.error("{}: {}", what, e.getMessage(), e);
        }
    }

    private void fail(Outcome outcome, Throwable e) {
        try {
            outcome.error().accept(GoogleGenAiErrors.translate(e));
        } catch (Exception ex) {
            log.error("Error during callback.onError", ex);
        }
    }

    /**
     * What one response (or the chunks of a streamed one) holds: the text, the reasoning, the calls and how it ended.
     */
    static class Answer {
        /** The text of the answer, without the reasoning. */
        final StringBuilder text = new StringBuilder();
        /** Everything that arrived, reasoning included. */
        final StringBuilder received = new StringBuilder();
        final List<LLMToolCall> calls = new ArrayList<>();
        // what goes back to the model in front of the results of the calls
        final List<Part> modelParts = new ArrayList<>();
        TokenUsage reported;
        String finishReason;
        String finishMessage;
        String blockReason;
        String blockMessage;

        /** Reads the response; the calls are only taken when the caller offered functions. */
        List<ChunkPiece> absorb(GenerateContentResponse response, boolean takeCalls) {
            List<ChunkPiece> pieces = new ArrayList<>();
            response.usageMetadata().ifPresent(usage -> reported = TokenUsage.of(usage));
            response.promptFeedback().ifPresent(feedback -> feedback.blockReason().ifPresent(reason -> {
                blockReason = reason.toString();
                blockMessage = feedback.blockReasonMessage().orElse(null);
            }));

            Candidate candidate = response.candidates().filter(list -> !list.isEmpty()).map(list -> list.get(0)).orElse(null);
            if (candidate == null) {
                return pieces;
            }
            candidate.finishReason().ifPresent(reason -> finishReason = reason.toString());
            candidate.finishMessage().ifPresent(message -> finishMessage = message);
            for (Part part : candidate.content().flatMap(Content::parts).orElse(List.of())) {
                if (part.functionCall().isPresent()) {
                    if (takeCalls) {
                        FunctionCall call = part.functionCall().get();
                        String arguments = GoogleGenAiMessages.argumentsJson(call);
                        received.append(arguments);
                        calls.add(LLMToolCall.of(GoogleGenAiMessages.callId(call), call.name().orElse(""), arguments,
                                part.thoughtSignature().map(bytes -> Base64.getEncoder().encodeToString(bytes)).orElse(null)));
                        // as it came: with the id and the thought signature the model gave it
                        addModelPart(part);
                    }
                    continue;
                }
                boolean thought = part.thought().orElse(false);
                String content = part.text().orElse("");
                if (!content.isEmpty()) {
                    received.append(content);
                    if (!thought) {
                        text.append(content);
                    }
                    pieces.add(new ChunkPiece(thought, content));
                }
                // the summary of the thinking is not sent back, its signature is
                if (!thought || part.thoughtSignature().isPresent()) {
                    addModelPart(part);
                }
            }
            return pieces;
        }

        /** Text that follows text of the same kind is one part. */
        private void addModelPart(Part part) {
            if (!modelParts.isEmpty() && isPlainText(part) && isPlainText(modelParts.get(modelParts.size() - 1))) {
                Part last = modelParts.remove(modelParts.size() - 1);
                modelParts.add(Part.fromText(last.text().orElse("") + part.text().orElse("")));
                return;
            }
            modelParts.add(part);
        }

        private static boolean isPlainText(Part part) {
            return part.text().isPresent() && !part.thought().orElse(false) && part.thoughtSignature().isEmpty()
                    && part.functionCall().isEmpty();
        }

        Content modelContent() {
            return Content.builder().role("model").parts(new ArrayList<>(modelParts)).build();
        }

        /** The model answered nothing, and not because it had nothing to say: it was stopped. */
        boolean isEmptyAndStopped() {
            if (!received.isEmpty()) {
                return false;
            }
            return blockReason != null || (finishReason != null && !"STOP".equals(finishReason) && !"FINISH_REASON_UNSPECIFIED".equals(finishReason));
        }

        InferenceException stopped() {
            return GoogleGenAiErrors.noAnswer(blockReason != null ? blockReason : finishReason,
                    blockReason != null ? blockMessage : finishMessage);
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

        /**
         * @param text         what the model wrote before the calls
         * @param modelContent the whole answer, for the conversation
         * @param calls        the calls, in the order the model made them
         */
        void call(String text, Content modelContent, List<LLMToolCall> calls);
    }

    private interface UsageRecorder {

        /**
         * @param reported  what Google reported, null if it didn't
         * @param generated all the text that arrived, reasoning included
         */
        void record(TokenUsage reported, String generated);
    }

    /**
     * Hands the chunks of the stream to the callback one at a time: the callback asks for the next with
     * {@link #continueInference()}, so a slow consumer slows the stream down instead of piling the text up.
     */
    private final class GoogleStreamController implements InferenceAsyncController {

        private final ResponseStream<GenerateContentResponse> stream;
        private final Iterator<GenerateContentResponse> iterator;
        private final InferenceAsyncCallback callback;
        private final Run run;
        private final Queue<ChunkPiece> pending = new LinkedList<>();
        private final UsageRecorder usageRecorder;
        private final FunctionCaller functionCaller;
        private final Answer answer = new Answer();
        private boolean completed = false;
        private boolean usageRecorded;

        GoogleStreamController(ResponseStream<GenerateContentResponse> stream, InferenceAsyncCallback callback, Run run,
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

                        pending.addAll(answer.absorb(iterator.next(), functionCaller != null));
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
                    if (answer.isEmptyAndStopped()) {
                        // nothing came and the model was stopped: that is not an empty answer, the user has to know why
                        fail(run.outcome, answer.stopped());
                    } else if (!answer.calls.isEmpty() && functionCaller != null) {
                        try {
                            functionCaller.call(answer.text.toString(), answer.modelContent(), answer.calls);
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
            usageRecorder.record(answer.reported, answer.received.toString());
        }

        private void closeStream() {
            run.forget();
            try {
                stream.close();
            } catch (Exception e) {
                log.trace("Error closing the Google Gen AI stream", e);
            }
        }
    }
}
