package com.github.enerccio.marginalia.domain.service;

/**
 * Marks the requests an inference service starts on this thread as not metered: the limits an administrator set for
 * the user ({@link AILimitService#check}) are not applied and the tokens are not added to the user's {@link
 * com.github.enerccio.marginalia.domain.model.impl.Usage}. For work the application does on its own account with an
 * AI an administrator chose, like the help assistant ({@link HelpAssistantService}); everything else is metered.
 * <pre>
 * try (UnmeteredScope _ = UnmeteredScope.open()) {
 *     service.stream(payload, protocol, callback);
 * }
 * </pre>
 * The scope only covers the call that starts the request: an inference service reads {@link #isActive()} in the
 * calling thread (before it hands the request to another one) and remembers the answer for the request. An extension
 * AI ({@code ExtensionAIFactory}) has to do the same around its limit check and its usage counting, see
 * {@link #isActive()}.
 */
public final class UnmeteredScope implements AutoCloseable {

    private static final ThreadLocal<Boolean> ACTIVE = new ThreadLocal<>();

    private final Boolean previous;

    private UnmeteredScope(Boolean previous) {
        this.previous = previous;
    }

    /**
     * Starts the scope on this thread; close it (try-with-resources) in the same thread.
     */
    public static UnmeteredScope open() {
        UnmeteredScope scope = new UnmeteredScope(ACTIVE.get());
        ACTIVE.set(Boolean.TRUE);
        return scope;
    }

    /**
     * Whether the request that is started on this thread now must not be metered. An inference service calls it once,
     * when the request starts: when true it skips the limit check and does not count the usage of the request, however
     * many rounds or threads the request takes.
     */
    public static boolean isActive() {
        return Boolean.TRUE.equals(ACTIVE.get());
    }

    @Override
    public void close() {
        if (previous == null) {
            ACTIVE.remove();
        } else {
            ACTIVE.set(previous);
        }
    }
}
