package com.github.enerccio.marginalia.domain.service.function;

import com.github.enerccio.marginalia.domain.service.function.FunctionCallException.Kind;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

/**
 * Functions the model may call in one request: {@link FunctionDefinition}s bound to the objects that implement them.
 * Cheap to create and immutable; the expensive part is in the definitions, which the registry caches. A function whose
 * {@linkplain FunctionDefinition#getLinkedTools() linked tools} are not all in the set is not offered (it is, once the set
 * with them is added with {@link #and}).
 * <pre>
 * FunctionSet functions = inferenceServices.getFunctionRegistry().bind(Weather.class, new WeatherImpl());
 * inferenceService.stream(payload, protocol, functions, callback);
 * </pre>
 */
public final class FunctionSet {

    private static final FunctionSet EMPTY = new FunctionSet(Map.of());

    private record Binding(FunctionDefinition definition, Object target) {}

    // everything bound, so that a function that waits for its linked tool is there when the set it needs is added
    private final Map<String, Binding> bound;
    // what is offered: the functions whose linked tools are all bound too
    private final Map<String, Binding> functions;

    private FunctionSet(Map<String, Binding> bound) {
        this.bound = bound;
        this.functions = complete(bound);
    }

    // a function is out when a function linked to it is missing, and so is everything linked to that one in turn
    private static Map<String, Binding> complete(Map<String, Binding> bound) {
        Map<String, Binding> offered = new LinkedHashMap<>(bound);
        boolean changed = true;
        while (changed) {
            changed = offered.values().removeIf(binding -> !offered.keySet().containsAll(binding.definition().getLinkedTools()));
        }
        return offered.size() == bound.size() ? bound : offered;
    }

    public static FunctionSet empty() {
        return EMPTY;
    }

    /** Binds the definitions of an interface to its implementation. */
    public static FunctionSet bind(Collection<FunctionDefinition> definitions, Object target) {
        Map<String, Binding> bindings = new LinkedHashMap<>();
        for (FunctionDefinition definition : definitions) {
            bindings.put(definition.getName(), new Binding(definition, target));
        }
        return new FunctionSet(bindings);
    }

    /**
     * @throws IllegalArgumentException when both have a function of the same name
     */
    public FunctionSet and(FunctionSet other) {
        if (other.bound.isEmpty()) {
            return this;
        }
        if (bound.isEmpty()) {
            return other;
        }
        Map<String, Binding> bindings = new LinkedHashMap<>(bound);
        for (Map.Entry<String, Binding> binding : other.bound.entrySet()) {
            if (bindings.putIfAbsent(binding.getKey(), binding.getValue()) != null) {
                throw new IllegalArgumentException("Function " + binding.getKey() + " is bound twice");
            }
        }
        return new FunctionSet(bindings);
    }

    public boolean isEmpty() {
        return functions.isEmpty();
    }

    public boolean contains(String name) {
        return functions.containsKey(name);
    }

    /** The functions in the order they were bound. */
    public List<FunctionDefinition> getDefinitions() {
        List<FunctionDefinition> definitions = new ArrayList<>();
        for (Binding binding : functions.values()) {
            definitions.add(binding.definition());
        }
        return definitions;
    }

    /**
     * Calls a function with the arguments the model wrote, asynchronously: the method runs on the executor (argument
     * parsing included) and gets a {@link FunctionContinuation} to answer with, from whichever thread it likes.
     * Nothing is thrown; every failure is the exception the returned future completes with.
     *
     * @param argumentsJson JSON object with the arguments by parameter name
     * @param executor      runs the call; wrap it to carry the request scope of the caller
     * @param cancelled     asked by {@link FunctionContinuation#isCancelled()}
     * @return the text for the model: what the method completed with (a string as it is, anything else as JSON, null
     * as "OK"). Completes exceptionally with a {@link FunctionCallException} when the function is unknown, the
     * arguments do not fit, the method threw or failed the continuation.
     */
    public CompletableFuture<String> call(String name, String argumentsJson, Executor executor, BooleanSupplier cancelled) {
        CompletableFuture<String> result = new CompletableFuture<>();
        Binding binding = functions.get(name);
        if (binding == null) {
            result.completeExceptionally(new FunctionCallException(Kind.UNKNOWN_FUNCTION, "Unknown function " + name
                    + ", available functions: " + String.join(", ", functions.keySet())));
            return result;
        }
        PendingCall continuation = new PendingCall(binding.definition().getName(), result, cancelled, new ConcurrentLinkedQueue<>());
        // the caller cancels the future it got
        result.whenComplete((_, _) -> {
            if (result.isCancelled()) {
                continuation.runHooks();
            }
        });
        try {
            executor.execute(() -> {
                try {
                    binding.definition().invoke(binding.target(), continuation, argumentsJson);
                } catch (FunctionCallException e) {
                    result.completeExceptionally(e);
                } catch (Throwable e) {
                    result.completeExceptionally(new FunctionCallException(Kind.FAILED,
                            "Function " + name + " failed: " + e, e));
                }
            });
        } catch (RuntimeException e) {
            result.completeExceptionally(new FunctionCallException(Kind.FAILED, "Function " + name + " could not be started: " + e, e));
        }
        return result;
    }

    /** The continuation of one call: finishes the future the caller waits on, once. */
    private record PendingCall(String name, CompletableFuture<String> result, BooleanSupplier cancelled, Queue<Runnable> hooks)
            implements FunctionContinuation<Object> {

        @Override
        public void onCancel(Runnable hook) {
            hooks.add(hook);
            if (result.isCancelled()) {
                runHooks();
            }
        }

        // each hook runs once, whoever gets to it first
        void runHooks() {
            Runnable hook;
            while ((hook = hooks.poll()) != null) {
                try {
                    hook.run();
                } catch (RuntimeException e) {
                    // the cancel of the others goes on
                }
            }
        }

        @Override
        public void complete(Object value) {
            try {
                result.complete(FunctionDefinition.render(value));
            } catch (FunctionCallException e) {
                result.completeExceptionally(e);
            }
        }

        @Override
        public void fail(String message) {
            result.completeExceptionally(new FunctionCallException(Kind.FAILED, "Function " + name + " failed: " + message));
        }

        @Override
        public void fail(Throwable error) {
            if (error instanceof FunctionCallException e) {
                result.completeExceptionally(e);
                return;
            }
            String message = error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName();
            result.completeExceptionally(new FunctionCallException(Kind.FAILED, "Function " + name + " failed: " + message, error));
        }

        @Override
        public boolean isCancelled() {
            return cancelled.getAsBoolean();
        }
    }

}
