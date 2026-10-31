package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;

/**
 * Turns what is going on into the stretches of time of the writing statistics ({@link ActivityIntervalService}): the one
 * writer of the stretches. Code that has something going on takes a registration and closes it when it is over:
 * <pre>
 * try (ActivityTracker.Registration _ = tracker.generate(bookId)) {
 *     // the request is on the wire
 * }
 * </pre>
 * The registrations of one kind, book and side that are open together are one stretch (two requests at once, two browser
 * tabs: one row); different books and different kinds are different rows that overlap as they happened, and
 * {@code TimeUnion} combines them when they are shown. Nothing here touches the database in the calling thread: the
 * registration is a note in memory and the tracker's own thread writes it, so a caller that holds a transaction is never
 * kept waiting for it.
 * <p>
 * The book of an agent's hidden working copy is resolved to the original, on the agents' side, the way
 * {@link ManuscriptStatisticService#count} does it. Never throws.
 */
public interface ActivityTracker {

    /**
     * The author is typing when the last key was no longer ago than this: after it the editing time stops, and the browser says
     * so (a lost message is covered by the tracker, which treats a registration that stayed typing for longer than this and a
     * tick as idle).
     */
    long IDLE_CUTOFF_MILLIS = 5 * 60 * 1000L;

    /**
     * The author's book window: the story of the book is on the screen. The stretch of {@code OPEN} runs while the page is
     * visible ({@link BookPresence#visible}), the one of {@code EDIT} while the author is typing ({@link BookPresence#editing}).
     * The same book in two windows is one stretch of each.
     *
     * @return a registration that does nothing for a book that is not saved or an agent's copy that belongs to no run
     */
    BookPresence open(Manuscript book);

    /**
     * A request to a model is on the wire for the book (the author's, or the original of an agent's working copy): the time
     * from the moment the request is sent to the moment the answer ends, is stopped or fails. The next round of an agent
     * follows within a moment, so a stretch is kept open for a short linger.
     *
     * @param bookId the id of the book as {@link UsageContext} has it, null registers nothing
     */
    Registration generate(Long bookId);

    /**
     * Moves everything the registrations noted into the database now (the tracker's thread does it a moment after each
     * change and on every tick). For the end of a work that needs the rows, the shutdown and tests.
     */
    void flush();

    /**
     * A registration of nothing, for what has no book or is not counted.
     */
    Registration NONE = () -> {
    };

    /**
     * What something has that is going on. Closing it again does nothing.
     */
    interface Registration extends AutoCloseable {

        @Override
        void close();
    }

    /**
     * The registration of an open book window.
     */
    interface BookPresence extends Registration {

        /**
         * Whether the page of the browser is visible; a book left open in a hidden tab does not count.
         */
        void visible(boolean visible);

        /**
         * Whether the author is typing in a part of the book. Only the changes are told, not every key.
         */
        void editing(boolean typing);
    }

}
