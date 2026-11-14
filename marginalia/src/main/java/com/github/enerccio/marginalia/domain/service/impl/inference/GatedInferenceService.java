package com.github.enerccio.marginalia.domain.service.impl.inference;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.AIGate;
import com.github.enerccio.marginalia.domain.service.AIGate.Permit;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceSession;
import com.github.enerccio.marginalia.domain.service.UnmeteredScope;
import com.github.enerccio.marginalia.domain.service.UsageContext;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.domain.service.job.Jobs;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.domain.service.structured.SchemaCheck;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CancellationException;

/**
 * The connections of an AI for every request (see {@link AIGate}): wraps the service of any AI, the core's and an extension's,
 * made by {@code InferenceServicesImpl.forAI}. A request ({@code stream}, {@code infer}, {@code round}) takes a place at the
 * gate before it is passed on and gives it back before its caller hears how it ended (completion, result, cancel, error), so a
 * request the caller starts from that callback queues behind the others instead of waiting for itself. While a function the
 * model called runs, the place is given back and taken again for the answer (a function may ask a model itself; the next round
 * goes to the back of the queue like any request).
 * <p>
 * With a free place the request is passed on at once, on the thread of the caller, exactly as without the gate. Otherwise the
 * caller gets a token at once (the methods never block: the story window calls them on the thread of the page), the callback
 * is told {@link InferenceAsyncCallback#onWaiting how many are before it}, and the request is passed on from a thread of its
 * own when its turn comes, with what the caller's thread had for it: the request scope (the user), the book of the usage, the
 * unmetered scope, the session of the prompt cache and the job. The time in the queue does not run against the timeout of the
 * request. Cancelling the token while it waits takes it out of the queue and calls {@code onCancel}.
 * <p>
 * Counting tokens, listing the models and the connection test take no place.
 * <p>
 * A request made on a thread bound to a run of the {@link Runs} ({@link Runs.Run#bind}) reports to that run: its place in the
 * queue, its start and what the model writes ({@link Runs.Run#getText()}), which the panel shows. Its callbacks are called with
 * the run bound, so a request the caller starts from them reports to the same run.
 */
public class GatedInferenceService implements InferenceService {

    private static final Logger log = LoggerFactory.getLogger(GatedInferenceService.class);

    private final InferenceService delegate;
    private final AI ai;
    private final AIGate gate;

    public GatedInferenceService(InferenceService delegate, AI ai, AIGate gate) {
        this.delegate = delegate;
        this.ai = ai;
        this.gate = gate;
    }

    /** The wrapped service. */
    public InferenceService getDelegate() {
        return delegate;
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        return type.isInstance(this) ? type.cast(this) : delegate.unwrap(type);
    }

    @Override
    public List<String> getModels() {
        return delegate.getModels();
    }

    @Override
    public List<String> testConnection() {
        return delegate.testConnection();
    }

    @Override
    public long countTokens(String text) throws Exception {
        return delegate.countTokens(text);
    }

    @Override
    public long countTokensApprox(String text) throws Exception {
        return delegate.countTokensApprox(text);
    }

    @Override
    public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, InferenceAsyncCallback callback) throws Exception {
        Flight flight = new Flight();
        return flight.start(asOutcome(callback), () -> delegate.stream(payload, protocol, flight.stream(callback)));
    }

    @Override
    public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions,
                                    InferenceAsyncCallback callback) throws Exception {
        Flight flight = new Flight();
        return flight.start(asOutcome(callback), () -> delegate.stream(payload, protocol, flight.functions(functions), flight.stream(callback)));
    }

    @Override
    public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, JsonNode schema, FunctionSet functions,
                                    InferenceAsyncCallback callback) throws Exception {
        Flight flight = new Flight();
        return flight.start(asOutcome(callback),
                () -> delegate.stream(payload, protocol, schema, flight.functions(functions), flight.stream(callback)));
    }

    @Override
    public CancellationToken infer(List<LLMChatMessage> payload, Protocol protocol, JsonNode schema, FunctionSet functions,
                                   InferenceResultCallback callback) throws Exception {
        Flight flight = new Flight();
        return flight.start(callback, () -> delegate.infer(payload, protocol, schema, flight.functions(functions), flight.result(callback)));
    }

    @Override
    public CancellationToken round(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions,
                                   InferenceRoundCallback callback) throws Exception {
        Flight flight = new Flight();
        return flight.start(callback, () -> delegate.round(payload, protocol, flight.functions(functions), flight.round(callback)));
    }

    @Override
    public CancellationToken round(List<LLMChatMessage> payload, Protocol protocol, FunctionSet functions, boolean noFunctionCalls,
                                   InferenceRoundCallback callback) throws Exception {
        Flight flight = new Flight();
        return flight.start(callback,
                () -> delegate.round(payload, protocol, flight.functions(functions), noFunctionCalls, flight.round(callback)));
    }

    /** The streaming callback seen as the outcome of a request: for the wait, the cancel and the error before it is sent. */
    private static InferenceOutcomeCallback asOutcome(InferenceAsyncCallback callback) {
        return new InferenceOutcomeCallback() {
            @Override
            public void onCancel() throws Exception {
                callback.onCancel();
            }

            @Override
            public void onError(Throwable exception) throws Exception {
                callback.onError(exception);
            }

            @Override
            public boolean isDead() {
                return callback.isDead();
            }

            @Override
            public void onWaiting(int ahead) {
                callback.onWaiting(ahead);
            }
        };
    }

    @FunctionalInterface
    private interface Send {
        CancellationToken send() throws Exception;
    }

    /**
     * What the caller's thread has for the request, to be put on the thread that sends it later.
     */
    private record Context(RequestAttributes attributes, Long book, boolean unmetered, InferenceSession.Scope session, Jobs.Handle job,
                           Runs.Run run) {

        static Context capture(Runs.Run run) {
            return new Context(ThreadCopyRequestAttributes.create(), UsageContext.current(), UnmeteredScope.isActive(),
                    InferenceSession.current(), Jobs.current(), run);
        }

        /** Enters everything on this thread; the result closes it again, in reverse order. */
        AutoCloseable enter() {
            Deque<AutoCloseable> opened = new ArrayDeque<>();
            opened.push(new InRequestScope(attributes));
            opened.push(UsageContext.open(book));
            if (unmetered) {
                opened.push(UnmeteredScope.open());
            }
            if (session != null) {
                opened.push(InferenceSession.open(session.kind(), session.scope()));
            }
            if (job != null) {
                opened.push(job.enter());
            }
            if (run != null) {
                opened.push(run.bind());
            }
            return () -> {
                while (!opened.isEmpty()) {
                    opened.pop().close();
                }
            };
        }
    }

    /**
     * One request and its place at the gate. The place is held from the start until the end, except while functions run.
     */
    private final class Flight {

        // the run the caller's thread works for: told about the wait, the start and the text
        private final Runs.Run run = Runs.current();
        private Permit permit;
        // functions that run now; the place is given back while there are any
        private int calls;
        // completes when the place is taken again after the functions, for the ones that answered
        private CompletableFuture<Void> resumed;
        private CompletableFuture<Permit> pending;
        private boolean ended;

        /**
         * Takes the place and sends the request, now or when its turn comes.
         */
        CancellationToken start(InferenceOutcomeCallback outcome, Send send) throws Exception {
            CompletableFuture<Permit> place = gate.acquire(ai, ahead -> {
                if (run != null) {
                    run.waiting(ahead);
                }
                outcome.onWaiting(ahead);
            });
            if (place.isDone() && !place.isCompletedExceptionally()) {
                // a free place: as if there were no gate
                held(place.join());
                try {
                    sent();
                    return send.send();
                } catch (Exception | Error e) {
                    end();
                    throw e;
                }
            }
            CancellationToken token = new CancellationToken();
            Context context = Context.capture(run);
            synchronized (this) {
                pending = place;
            }
            token.onCancel(() -> {
                if (place.cancel(true)) {
                    // out of the queue before its turn: nothing was sent
                    end();
                    notify(outcome, null);
                }
            });
            place.whenComplete((granted, error) -> {
                if (error != null) {
                    return;
                }
                Thread.ofVirtual().name("inference-gated").start(() -> sendLater(granted, token, context, outcome, send));
            });
            return token;
        }

        private void sendLater(Permit granted, CancellationToken token, Context context, InferenceOutcomeCallback outcome, Send send) {
            held(granted);
            if (token.isCancelled() || outcome.isDead()) {
                end();
                notify(outcome, null);
                return;
            }
            try {
                // its turn came: the wait is over
                if (run != null) {
                    run.waiting(0);
                }
                outcome.onWaiting(0);
            } catch (RuntimeException e) {
                log.debug("A callback failed on the end of the wait: {}", e.getMessage(), e);
            }
            CancellationToken sent;
            try (AutoCloseable _ = context.enter()) {
                sent();
                sent = send.send();
            } catch (Exception | Error e) {
                end();
                notify(outcome, e);
                return;
            }
            if (sent != null) {
                token.onCancel(sent::cancel);
            }
        }

        /** The request goes out: its run starts a new text. */
        private void sent() {
            if (run != null) {
                run.requestStarted();
            }
        }

        /** The run bound while a callback of the caller runs, null without one. */
        private Runs.Binding bound() {
            return run == null ? null : run.bind();
        }

        private void chunk(ChunkType type, String text) {
            if (run != null) {
                run.chunk(type == ChunkType.REASONING, text);
            }
        }

        private void notify(InferenceOutcomeCallback outcome, Throwable error) {
            try (Runs.Binding _ = bound()) {
                if (error == null) {
                    outcome.onCancel();
                } else {
                    outcome.onError(error);
                }
            } catch (Exception e) {
                log.debug("The callback of a request that never went out failed: {}", e.getMessage(), e);
            }
        }

        private synchronized void held(Permit granted) {
            pending = null;
            if (ended) {
                granted.close();
            } else {
                permit = granted;
            }
        }

        /** The request is over: the place is given back, a place that is still to come is not taken. */
        synchronized void end() {
            if (ended) {
                return;
            }
            ended = true;
            if (permit != null) {
                permit.close();
                permit = null;
            }
            if (pending != null) {
                pending.cancel(true);
                pending = null;
            }
            if (resumed != null && !resumed.isDone()) {
                resumed.completeExceptionally(new CancellationException("The request is over"));
            }
        }

        FunctionSet functions(FunctionSet functions) {
            if (functions == null || functions.isEmpty()) {
                return functions;
            }
            return functions.around(new FunctionSet.CallAround() {
                @Override
                public void before() {
                    synchronized (Flight.this) {
                        if (calls++ == 0) {
                            resumed = new CompletableFuture<>();
                            if (permit != null) {
                                permit.close();
                                permit = null;
                            }
                        }
                    }
                }

                @Override
                public CompletableFuture<Void> after() {
                    CompletableFuture<Void> mine;
                    CompletableFuture<Permit> place = null;
                    synchronized (Flight.this) {
                        mine = resumed;
                        if (--calls == 0 && !ended) {
                            place = gate.acquire(ai);
                            pending = place;
                        }
                    }
                    if (place != null) {
                        CompletableFuture<Permit> taken = place;
                        taken.whenComplete((granted, error) -> {
                            if (error != null) {
                                mine.completeExceptionally(error);
                                return;
                            }
                            synchronized (Flight.this) {
                                if (pending == taken) {
                                    pending = null;
                                }
                            }
                            held(granted);
                            mine.complete(null);
                        });
                    }
                    return mine == null ? CompletableFuture.completedFuture(null) : mine;
                }
            });
        }

        InferenceAsyncCallback stream(InferenceAsyncCallback callback) {
            return new InferenceAsyncCallback() {
                @Override
                public void onChunk(InferenceAsyncController controller, ChunkType chunkType, String text) throws Exception {
                    chunk(chunkType, text);
                    callback.onChunk(controller, chunkType, text);
                }

                @Override
                public void onCompletion() throws Exception {
                    end();
                    try (Runs.Binding _ = bound()) {
                        callback.onCompletion();
                    }
                }

                @Override
                public void onCancel() throws Exception {
                    end();
                    try (Runs.Binding _ = bound()) {
                        callback.onCancel();
                    }
                }

                @Override
                public void onError(Throwable exception) throws Exception {
                    end();
                    try (Runs.Binding _ = bound()) {
                        callback.onError(exception);
                    }
                }

                @Override
                public boolean isDead() {
                    return callback.isDead();
                }

                @Override
                public void onFunctionCall(LLMToolCall call, String result, boolean failed) throws Exception {
                    callback.onFunctionCall(call, result, failed);
                }

                @Override
                public void onWaiting(int ahead) {
                    callback.onWaiting(ahead);
                }
            };
        }

        InferenceResultCallback result(InferenceResultCallback callback) {
            return new InferenceResultCallback() {
                @Override
                public void onResult(String text, SchemaCheck check) throws Exception {
                    // delivered whole: the panel shows it at the end
                    chunk(ChunkType.RESPONSE, text);
                    end();
                    try (Runs.Binding _ = bound()) {
                        callback.onResult(text, check);
                    }
                }

                @Override
                public void onCancel() throws Exception {
                    end();
                    try (Runs.Binding _ = bound()) {
                        callback.onCancel();
                    }
                }

                @Override
                public void onError(Throwable exception) throws Exception {
                    end();
                    try (Runs.Binding _ = bound()) {
                        callback.onError(exception);
                    }
                }

                @Override
                public boolean isDead() {
                    return callback.isDead();
                }

                @Override
                public void onFunctionCall(LLMToolCall call, String result, boolean failed) throws Exception {
                    callback.onFunctionCall(call, result, failed);
                }

                @Override
                public void onWaiting(int ahead) {
                    callback.onWaiting(ahead);
                }
            };
        }

        InferenceRoundCallback round(InferenceRoundCallback callback) {
            return new InferenceRoundCallback() {
                @Override
                public void onRound(String text, List<LLMToolCall> calls, RoundUsage usage) throws Exception {
                    end();
                    try (Runs.Binding _ = bound()) {
                        callback.onRound(text, calls, usage);
                    }
                }

                @Override
                public void onRound(String text, String reasoning, List<LLMToolCall> calls, RoundUsage usage) throws Exception {
                    end();
                    try (Runs.Binding _ = bound()) {
                        callback.onRound(text, reasoning, calls, usage);
                    }
                }

                @Override
                public void onChunk(ChunkType chunkType, String text) {
                    chunk(chunkType, text);
                    callback.onChunk(chunkType, text);
                }

                @Override
                public void onCancel() throws Exception {
                    end();
                    try (Runs.Binding _ = bound()) {
                        callback.onCancel();
                    }
                }

                @Override
                public void onError(Throwable exception) throws Exception {
                    end();
                    try (Runs.Binding _ = bound()) {
                        callback.onError(exception);
                    }
                }

                @Override
                public boolean isDead() {
                    return callback.isDead();
                }

                @Override
                public void onFunctionCall(LLMToolCall call, String result, boolean failed) throws Exception {
                    callback.onFunctionCall(call, result, failed);
                }

                @Override
                public void onWaiting(int ahead) {
                    callback.onWaiting(ahead);
                }
            };
        }
    }
}
