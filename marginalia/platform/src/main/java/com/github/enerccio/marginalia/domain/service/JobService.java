package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Job;
import com.github.enerccio.marginalia.domain.model.impl.JobCall;
import com.github.enerccio.marginalia.domain.repository.JobRepository;
import com.github.enerccio.marginalia.domain.service.job.ActiveJob;
import com.github.enerccio.marginalia.domain.service.job.FinishedJob;
import com.github.enerccio.marginalia.domain.service.job.JobFilter;
import com.github.enerccio.marginalia.domain.service.job.Jobs;

import java.util.List;

/**
 * The log of the jobs (see {@link Jobs}) and the administrator's view of it. The log is written when a job ends, in the
 * background, and nothing is ever deleted by itself: the administrator purges it ({@link #purge}). Everything that reads it
 * is for administrators only.
 */
public interface JobService extends BaseService<Job, JobRepository> {

    /**
     * Queues an ended job for the log; never throws and never waits for the database. Called by {@link Jobs}, not by the code that
     * does the work.
     */
    void log(FinishedJob job);

    /**
     * Writes the job and its calls. Only for {@link #log}, which calls it from its own thread.
     */
    void persist(FinishedJob job) throws Exception;

    /**
     * @throws SecurityException for anybody but an administrator (as the rest of the methods)
     */
    List<Job> search(JobFilter filter, int offset, int limit) throws Exception;

    long count(JobFilter filter) throws Exception;

    List<JobCall> calls(Job job) throws Exception;

    /**
     * The kinds of the jobs in the log.
     */
    List<String> kinds() throws Exception;

    /**
     * The jobs that run now.
     */
    List<ActiveJob> active() throws Exception;

    /**
     * Deletes the jobs of the filter and their calls for good.
     *
     * @return how many jobs were deleted
     */
    int purge(JobFilter filter) throws Exception;

}
