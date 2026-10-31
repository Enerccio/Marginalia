package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;

/**
 * Says which book the requests an inference service starts on this thread are made for, so that the tokens they cost are
 * counted for the book as well as for the user and the AI ({@link UsageService#record}, the writing statistics of the book,
 * {@link ManuscriptStatisticService}). Without it a request counts for the AI only.
 * <pre>
 * try (UsageContext _ = UsageContext.open(book)) {
 *     service.stream(payload, protocol, callback);
 * }
 * </pre>
 * The scope only covers the call that starts the request: an inference service reads {@link #current()} in the calling thread
 * (before it hands the request to another one) and remembers the answer for the request, then counts the usage of the request
 * in its own threads with {@link #open(Long)} of that answer. An extension AI ({@code ExtensionAIFactory}) has to do the same
 * around its call of {@link UsageService#record}, the book is the one of the request, not of the thread that happens to run it.
 */
public final class UsageContext implements AutoCloseable {

    private static final ThreadLocal<Long> ACTIVE = new ThreadLocal<>();

    private final Long previous;

    private UsageContext(Long previous) {
        this.previous = previous;
    }

    /**
     * Starts the scope on this thread for the book; close it (try-with-resources) in the same thread. A book that is not
     * saved is no book.
     */
    public static UsageContext open(Manuscript book) {
        return open(book == null ? null : book.getId());
    }

    /**
     * Starts the scope on this thread for the book with the id; {@code null} starts a scope of no book, inside another one.
     */
    public static UsageContext open(Long manuscriptId) {
        UsageContext scope = new UsageContext(ACTIVE.get());
        if (manuscriptId == null) {
            ACTIVE.remove();
        } else {
            ACTIVE.set(manuscriptId);
        }
        return scope;
    }

    /**
     * @return the id of the book the request that is started on this thread now is made for, null outside of a scope
     */
    public static Long current() {
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
