package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.Job;
import com.github.enerccio.marginalia.domain.model.impl.JobCall;
import com.github.enerccio.marginalia.domain.repository.JobRepository;
import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.service.InferenceSession;
import com.github.enerccio.marginalia.domain.service.JobService;
import com.github.enerccio.marginalia.domain.service.impl.job.JobRecorder;
import com.github.enerccio.marginalia.domain.service.job.*;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobServiceImpl extends BaseServiceImpl<Job, JobRepository> implements JobService, InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);

    // one thread writes, in the order the jobs ended: SQLite has one writer anyway and the jobs must not wait for it
    private final java.util.function.Consumer<FinishedJob> sink = this::log;

    private final ExecutorService writer = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "job-log");
        thread.setDaemon(true);
        return thread;
    });

    @Autowired
    @Lazy
    private JobService self;

    @Autowired
    private AdminGuard adminGuard;

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private User currentUser;

    @Override
    public void afterPropertiesSet() {
        Jobs.setOwnerResolver(this::owner);
        Jobs.addSink(sink);
        // one recorder however many contexts are alive (tests keep several), or a request would be recorded twice
        if (!InferenceSession.listeners().contains(JobRecorder.INSTANCE)) {
            InferenceSession.addListener(JobRecorder.INSTANCE);
        }
    }

    @Override
    public void destroy() {
        InferenceSession.removeListener(JobRecorder.INSTANCE);
        Jobs.removeSink(sink);
        writer.shutdown();
    }

    /**
     * The user of the thread that opens a job: the session scoped user, null on a thread without a session.
     */
    private Jobs.Owner owner() {
        Long id = currentUser.getId();
        return id == null ? null : new Jobs.Owner(id, currentUser.getLogin());
    }

    @Override
    @NoTx
    public void log(FinishedJob job) {
        try {
            writer.execute(() -> {
                try {
                    self.persist(job);
                } catch (Exception e) {
                    log.warn("The job {} ({}) was not logged", job.uuid(), job.kind(), e);
                }
            });
        } catch (RuntimeException e) {
            // the service is stopping
            log.debug("The job {} was not logged: {}", job.uuid(), e.getMessage());
        }
    }

    @Override
    @CommonTx
    public void persist(FinishedJob finished) throws Exception {
        Job job = new Job();
        if (finished.userId() != null) {
            job.setOwner(userService.find(finished.userId()));
        }
        job.setOwnerLogin(finished.login());
        job.setKind(finished.kind());
        job.setLabel(finished.label());
        job.setStatus(finished.status());
        job.setStartedAt(Date.from(finished.startedAt()));
        job.setEndedAt(Date.from(finished.endedAt()));
        job.setCallCount(finished.callCount());
        job.setFailedCalls(finished.failedCalls());
        job = getRepository().saveWithoutEvent(job);
        for (FinishedCall call : finished.calls()) {
            JobCall entity = new JobCall();
            entity.setJob(job);
            entity.setCallType(call.type());
            entity.setBackend(truncate(call.backend(), 255));
            entity.setModel(truncate(call.model(), 255));
            entity.setStatus(call.status());
            entity.setStartedAt(Date.from(call.startedAt()));
            entity.setEndedAt(Date.from(call.endedAt()));
            entity.setError(truncate(call.error(), 1000));
            getRepository().saveCall(entity);
        }
    }

    private static String truncate(String text, int max) {
        return text == null || text.length() <= max ? text : text.substring(0, max);
    }

    @Override
    @CommonTxReadOnly
    public List<Job> search(JobFilter filter, int offset, int limit) throws Exception {
        adminGuard.requireAdmin();
        return getRepository().search(filter == null ? JobFilter.ALL : filter, Math.max(0, offset), Math.max(1, limit));
    }

    @Override
    @CommonTxReadOnly
    public long count(JobFilter filter) throws Exception {
        adminGuard.requireAdmin();
        return getRepository().count(filter == null ? JobFilter.ALL : filter);
    }

    @Override
    @CommonTxReadOnly
    public List<JobCall> calls(Job job) throws Exception {
        adminGuard.requireAdmin();
        return getRepository().findCalls(job);
    }

    @Override
    @CommonTxReadOnly
    public List<String> kinds() throws Exception {
        adminGuard.requireAdmin();
        return getRepository().findKinds();
    }

    @Override
    @NoTx
    public List<ActiveJob> active() throws Exception {
        adminGuard.requireAdmin();
        return Jobs.active();
    }

    @Override
    @CommonTx
    public int purge(JobFilter filter) throws Exception {
        adminGuard.requireAdmin();
        return getRepository().purge(filter == null ? JobFilter.ALL : filter);
    }
}
