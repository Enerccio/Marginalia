package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.Job;
import com.github.enerccio.marginalia.domain.model.impl.JobCall;
import com.github.enerccio.marginalia.domain.service.job.JobFilter;

import java.util.List;

public interface JobRepository extends OwnedRepository<Job> {

    /**
     * The jobs of the filter, the newest first.
     */
    List<Job> search(JobFilter filter, int offset, int limit) throws Exception;

    long count(JobFilter filter) throws Exception;

    /**
     * The calls of the job, the first one first.
     */
    List<JobCall> findCalls(Job job) throws Exception;

    /**
     * Saves a call of a job that was just saved; sets what the repository sets of an entity (uuid, dates).
     */
    JobCall saveCall(JobCall call) throws Exception;

    /**
     * The kinds the log has jobs of, sorted.
     */
    List<String> findKinds() throws Exception;

    /**
     * Deletes the jobs of the filter and their calls for good.
     *
     * @return the number of deleted jobs
     */
    int purge(JobFilter filter) throws Exception;

}
