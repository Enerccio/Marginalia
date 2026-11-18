package com.github.enerccio.marginalia.domain.service;

/**
 * Told when a request to a model is on the wire for a context (the id {@link UsageContext} has): the time from the
 * moment the request is sent to the moment the answer ends, is stopped or fails. The inference service takes a
 * registration and closes it when the request is over. Marginalia's {@link ActivityTracker} turns it into the
 * generating time of a book; without a bean of this type nothing is told.
 */
public interface RequestActivity {

    /**
     * A registration of nothing, for what has no context or is not counted.
     */
    Registration NONE = () -> {
    };

    /**
     * @param contextId the id of the context as {@link UsageContext} has it, null registers nothing
     */
    Registration generate(Long contextId);

    /**
     * What something has that is going on. Closing it again does nothing.
     */
    interface Registration extends AutoCloseable {

        @Override
        void close();
    }
}
