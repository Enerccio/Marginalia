package com.github.enerccio.marginalia.extensions.httphooks;

import com.github.enerccio.marginalia.domain.service.job.JobCallType;
import com.github.enerccio.marginalia.domain.service.job.JobStatus;
import com.github.enerccio.marginalia.domain.service.job.Jobs;

import java.net.URI;

/**
 * Records the work a plugin asks of an external AI backend (a speech made, an image generated) as a call of the job of the thread,
 * so that it shows in the administrator's overview of jobs ({@link Jobs}). Wrap the one operation that is the work, not the
 * checks of a connection or the polling of a server: a call outside of a job makes a job of its own.
 */
public final class JobCalls {

    private JobCalls() {
    }

    @FunctionalInterface
    public interface Work<T> {
        T run() throws BackendException;
    }

    /**
     * Runs the work as a call and records how it ended.
     *
     * @param type    what the backend does
     * @param autoKind the kind of the job it makes when the thread has none (the name of the plugin)
     * @param baseUrl the address of the server; only its host is recorded, never the path or the query
     * @param model   the model, null when there is none
     */
    public static <T> T track(JobCallType type, String autoKind, String baseUrl, String model, Work<T> work) throws BackendException {
        Jobs.Call call = Jobs.beginCallOrJob(type, hostOf(baseUrl), model, autoKind);
        try {
            T result = work.run();
            call.finish(JobStatus.COMPLETED, null);
            return result;
        } catch (BackendException e) {
            call.finish("Cancelled".equals(e.getMessage()) ? JobStatus.CANCELLED : JobStatus.FAILED, e.getMessage());
            throw e;
        } catch (RuntimeException | Error e) {
            call.finish(JobStatus.FAILED, e.getClass().getSimpleName());
            throw e;
        }
    }

    static String hostOf(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return null;
        }
        try {
            String host = URI.create(baseUrl.trim()).getHost();
            return host == null ? baseUrl : host;
        } catch (IllegalArgumentException e) {
            return baseUrl;
        }
    }
}
