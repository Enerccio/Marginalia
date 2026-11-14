package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Job;
import com.github.enerccio.marginalia.domain.model.impl.JobCall;
import com.github.enerccio.marginalia.domain.repository.JobRepository;
import com.github.enerccio.marginalia.domain.service.job.JobFilter;
import jakarta.persistence.Query;

import com.github.enerccio.marginalia.utils.AppClock;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.List;

public class JpaJobRepository extends JpaOwnedRepository<Job> implements JobRepository {

    @Override
    protected Class<Job> getEntityClass() {
        return Job.class;
    }

    /**
     * The conditions of the filter on {@code j}, and the parameters of them in the order they were added.
     */
    private static String where(JobFilter filter, List<Object[]> parameters) {
        List<String> conditions = new ArrayList<>();
        conditions.add("j.deleted = false");
        if (filter.userId() != null) {
            conditions.add("j.owner.id = :user");
            parameters.add(new Object[]{"user", filter.userId()});
        }
        if (filter.kind() != null) {
            conditions.add("j.kind = :kind");
            parameters.add(new Object[]{"kind", filter.kind()});
        }
        if (filter.status() != null) {
            conditions.add("j.status = :status");
            parameters.add(new Object[]{"status", filter.status()});
        }
        if (filter.from() != null) {
            conditions.add("j.startedAt >= :from");
            parameters.add(new Object[]{"from", filter.from()});
        }
        if (filter.before() != null) {
            conditions.add("j.startedAt < :before");
            parameters.add(new Object[]{"before", filter.before()});
        }
        return String.join(" AND ", conditions);
    }

    private static void bind(Query query, List<Object[]> parameters) {
        for (Object[] parameter : parameters) {
            query.setParameter((String) parameter[0], parameter[1]);
        }
    }

    @Override
    public List<Job> search(JobFilter filter, int offset, int limit) {
        List<Object[]> parameters = new ArrayList<>();
        var query = getEntityManager().createQuery("SELECT j FROM Job j WHERE " + where(filter, parameters)
                + " ORDER BY j.startedAt DESC, j.id DESC", Job.class);
        bind(query, parameters);
        return query.setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    @Override
    public long count(JobFilter filter) {
        List<Object[]> parameters = new ArrayList<>();
        var query = getEntityManager().createQuery("SELECT COUNT(j) FROM Job j WHERE " + where(filter, parameters), Long.class);
        bind(query, parameters);
        return query.getSingleResult();
    }

    @Override
    public List<JobCall> findCalls(Job job) {
        return getEntityManager()
                .createQuery("SELECT c FROM JobCall c WHERE c.job.id = :job AND c.deleted = false ORDER BY c.startedAt, c.id", JobCall.class)
                .setParameter("job", job.getId())
                .getResultList();
    }

    @Override
    public JobCall saveCall(JobCall call) {
        Date now = AppClock.now();
        call.setUuid(UUID.randomUUID().toString());
        call.setCreation(now);
        call.setModification(now);
        getEntityManager().persist(call);
        return call;
    }

    @Override
    public List<String> findKinds() {
        return getEntityManager()
                .createQuery("SELECT DISTINCT j.kind FROM Job j WHERE j.deleted = false ORDER BY j.kind", String.class)
                .getResultList();
    }

    @Override
    public int purge(JobFilter filter) {
        List<Object[]> parameters = new ArrayList<>();
        String where = where(filter, parameters);
        Query calls = getEntityManager().createQuery("DELETE FROM JobCall c WHERE c.job.id IN (SELECT j.id FROM Job j WHERE " + where + ")");
        bind(calls, parameters);
        calls.executeUpdate();
        Query jobs = getEntityManager().createQuery("DELETE FROM Job j WHERE " + where);
        bind(jobs, parameters);
        return jobs.executeUpdate();
    }
}
