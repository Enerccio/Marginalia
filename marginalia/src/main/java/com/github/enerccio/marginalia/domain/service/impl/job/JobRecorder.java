package com.github.enerccio.marginalia.domain.service.impl.job;

import com.github.enerccio.marginalia.domain.service.InferenceListener;
import com.github.enerccio.marginalia.domain.service.job.JobCallType;
import com.github.enerccio.marginalia.domain.service.job.JobStatus;
import com.github.enerccio.marginalia.domain.service.job.Jobs;

/**
 * Records every request to a model as a call of the job of the thread; a request outside of any job is a job of its own, of the
 * kind of its {@code InferenceSession}. Reads nothing of the messages.
 */
public class JobRecorder implements InferenceListener {

    public static final JobRecorder INSTANCE = new JobRecorder();

    @Override
    public boolean wantsMessages() {
        return false;
    }

    @Override
    public Observation onRequest(Request request) {
        Jobs.Call call = Jobs.beginCallOrJob(JobCallType.LLM, request.aiName(), request.model(), request.kind());
        return response -> call.finish(status(response.outcome()), response.error());
    }

    private static JobStatus status(Outcome outcome) {
        return switch (outcome) {
            case COMPLETED -> JobStatus.COMPLETED;
            case CANCELLED -> JobStatus.CANCELLED;
            case FAILED -> JobStatus.FAILED;
        };
    }
}
