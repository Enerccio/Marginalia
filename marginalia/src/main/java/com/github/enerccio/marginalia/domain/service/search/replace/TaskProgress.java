package com.github.enerccio.marginalia.domain.service.search.replace;

/**
 * Progress of a long search or replace that runs on a worker thread: the task reports how far it is and asks whether
 * the user wants it to stop.
 */
public interface TaskProgress {

    /**
     * @param done  items done so far
     * @param total items to do in this step, 0 when not known
     */
    void update(long done, long total);

    /**
     * @return true when the user asked to stop, the task is then abandoned with a {@link java.util.concurrent.CancellationException}
     */
    boolean isCancelled();

    /**
     * @throws java.util.concurrent.CancellationException when {@link #isCancelled()}
     */
    default void checkCancelled() {
        if (isCancelled()) {
            throw new java.util.concurrent.CancellationException();
        }
    }

    TaskProgress NONE = new TaskProgress() {
        @Override
        public void update(long done, long total) {
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    };

}
