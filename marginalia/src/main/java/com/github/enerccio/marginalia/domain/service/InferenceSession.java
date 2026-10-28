package com.github.enerccio.marginalia.domain.service;

/**
 * Names the kind of work the requests an inference service starts on this thread belong to, so that the service can
 * send it to the provider as the header {@value #HEADER}. The value is an opaque hash of the user and the kind (see
 * {@link InferenceServices#currentSessionId()}): the same user doing the same kind of work always sends the same value,
 * whatever book it is about. A server that keeps a prompt cache can use it to route the request to the cache of the
 * previous one (or save and restore a slot by it, as a proxy in front of llama.cpp can), so the cache of a
 * kind of work is not thrown away by another one and the number of caches is bounded: one per user and kind.
 * <p>
 * Generating, swiping and regenerating a part are one kind ({@link #GENERATION}), as their prompts share the story. A
 * plugin chooses a kind of its own, named like the plugin.
 * <pre>
 * try (InferenceSession _ = InferenceSession.open(InferenceSession.ANNOTATE)) {
 *     service.stream(payload, protocol, callback);
 * }
 * </pre>
 * The scope only covers the call that starts the request: an inference service asks for {@link
 * InferenceServices#currentSessionId()} in the calling thread (before it hands the request to another one) and
 * remembers the answer for the request. A request started outside of any scope sends no header. An extension AI
 * ({@code ExtensionAIFactory}) has to do the same and send the value in the way its provider accepts.
 */
public final class InferenceSession implements AutoCloseable {

    /** The header the session id is sent in. Harmless to a server that does not use it. */
    public static final String HEADER = "X-Session-ID";

    /** A new part, a regenerated part and a swipe. */
    public static final String GENERATION = "generation";
    public static final String IMPERSONATE = "impersonate";
    public static final String ANNOTATE = "annotate";
    public static final String REDRAFT = "redraft";
    public static final String SUMMARY = "summary";
    public static final String AGENT = "agent";
    public static final String HELP = "help";

    private static final ThreadLocal<Scope> ACTIVE = new ThreadLocal<>();

    /**
     * @param kind  what the work is, see the constants; a plugin uses its own name
     * @param scope what tells apart the parallel works of one kind that do not share a prompt (the run of an agent), null
     *              when the kind is enough
     */
    public record Scope(String kind, String scope) {

        /** The text the session id is made of, kinds and scopes can't be confused with each other. */
        public String key() {
            return scope == null ? kind : kind + "/" + scope;
        }
    }

    private final Scope previous;

    private InferenceSession(Scope previous) {
        this.previous = previous;
    }

    /**
     * Starts the scope on this thread; close it (try-with-resources) in the same thread.
     */
    public static InferenceSession open(String kind) {
        return open(kind, null);
    }

    /**
     * Like {@link #open(String)}, for a kind of work that has parallel runs with prompts of their own (an agent run
     * grows its conversation, another run must not take its cache).
     */
    public static InferenceSession open(String kind, String scope) {
        if (kind == null || kind.isBlank()) {
            throw new IllegalArgumentException("The kind of the session is required");
        }
        InferenceSession session = new InferenceSession(ACTIVE.get());
        ACTIVE.set(new Scope(kind, scope));
        return session;
    }

    /**
     * @return what the request that is started on this thread now belongs to, null outside of a scope
     */
    public static Scope current() {
        return ACTIVE.get();
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
