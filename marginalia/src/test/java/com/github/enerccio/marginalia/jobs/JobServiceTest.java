package com.github.enerccio.marginalia.jobs;

import com.github.enerccio.marginalia.domain.model.impl.Job;
import com.github.enerccio.marginalia.domain.model.impl.JobCall;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.InferenceSession;
import com.github.enerccio.marginalia.domain.service.JobService;
import com.github.enerccio.marginalia.domain.service.job.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.test.InferenceCollector;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The log of the jobs: ended jobs are written with their calls, the administrator reads and purges them, nobody else does, and
 * the requests to a model are recorded as calls.
 */
class JobServiceTest extends MarginaliaTestBase {

    @Autowired
    private JobService jobService;

    private FinishedJob finished(User user, String kind, JobStatus status, Instant start, FinishedCall... calls) {
        return new FinishedJob(java.util.UUID.randomUUID().toString(), user == null ? null : user.getId(),
                user == null ? null : user.getLogin(), kind, kind + " label", status, start, start.plusSeconds(5),
                List.of(calls), calls.length, (int) java.util.Arrays.stream(calls).filter(c -> c.status() == JobStatus.FAILED).count());
    }

    private FinishedCall call(JobCallType type, JobStatus status, Instant start) {
        return new FinishedCall(type, "backend", "model", status, start, start.plusSeconds(2), status == JobStatus.FAILED ? "boom" : null);
    }

    @Test
    void anEndedJobIsWrittenWithItsCalls() throws Exception {
        User admin = loginAdmin();
        Instant start = Instant.now().minusSeconds(60);

        jobService.persist(finished(admin, "audiobook", JobStatus.FAILED, start,
                call(JobCallType.AUDIO, JobStatus.COMPLETED, start), call(JobCallType.AUDIO, JobStatus.FAILED, start.plusSeconds(2))));

        List<Job> jobs = jobService.search(new JobFilter(admin.getId(), "audiobook", null, null, null), 0, 10);
        assertThat(jobs).singleElement().satisfies(job -> {
            assertThat(job.getStatus()).isEqualTo(JobStatus.FAILED);
            assertThat(job.getOwnerLogin()).isEqualTo(admin.getLogin());
            assertThat(job.getOwner().getId()).isEqualTo(admin.getId());
            assertThat(job.getCallCount()).isEqualTo(2);
            assertThat(job.getFailedCalls()).isEqualTo(1);
            assertThat(job.getEndedAt()).isAfter(job.getStartedAt());
        });
        List<JobCall> calls = jobService.calls(jobs.getFirst());
        assertThat(calls).extracting(JobCall::getStatus).containsExactly(JobStatus.COMPLETED, JobStatus.FAILED);
        assertThat(calls.get(1).getError()).isEqualTo("boom");
        assertThat(calls.get(0).getCallType()).isEqualTo(JobCallType.AUDIO);
        assertThat(jobService.kinds()).contains("audiobook");
    }

    @Test
    void searchFiltersByKindStatusAndTimeAndPages() throws Exception {
        User admin = loginAdmin();
        String kind = "kind-" + System.nanoTime();
        Instant now = Instant.now();
        jobService.persist(finished(admin, kind, JobStatus.COMPLETED, now.minusSeconds(3 * 86400)));
        jobService.persist(finished(admin, kind, JobStatus.COMPLETED, now.minusSeconds(2 * 86400)));
        jobService.persist(finished(admin, kind, JobStatus.FAILED, now.minusSeconds(60)));

        JobFilter mine = new JobFilter(admin.getId(), kind, null, null, null);
        assertThat(jobService.count(mine)).isEqualTo(3);
        assertThat(jobService.search(mine, 0, 10)).extracting(Job::getStatus)
                .containsExactly(JobStatus.FAILED, JobStatus.COMPLETED, JobStatus.COMPLETED);
        assertThat(jobService.search(mine, 1, 1)).hasSize(1);
        assertThat(jobService.count(new JobFilter(admin.getId(), kind, JobStatus.FAILED, null, null))).isEqualTo(1);
        assertThat(jobService.count(new JobFilter(admin.getId(), kind, null, Date.from(now.minusSeconds(86400)), null))).isEqualTo(1);
        assertThat(jobService.count(new JobFilter(admin.getId(), kind, null, null, Date.from(now.minusSeconds(86400))))).isEqualTo(2);
    }

    @Test
    void purgeDeletesTheMatchingJobsAndTheirCallsAndNothingElse() throws Exception {
        User admin = loginAdmin();
        String kind = "purge-" + System.nanoTime();
        Instant now = Instant.now();
        jobService.persist(finished(admin, kind, JobStatus.COMPLETED, now.minusSeconds(40 * 86400),
                call(JobCallType.LLM, JobStatus.COMPLETED, now.minusSeconds(40 * 86400))));
        jobService.persist(finished(admin, kind, JobStatus.COMPLETED, now.minusSeconds(60),
                call(JobCallType.LLM, JobStatus.COMPLETED, now.minusSeconds(60))));
        JobFilter old = new JobFilter(admin.getId(), kind, null, null, Date.from(now.minusSeconds(30 * 86400)));

        assertThat(jobService.purge(old)).isEqualTo(1);

        List<Job> left = jobService.search(new JobFilter(admin.getId(), kind, null, null, null), 0, 10);
        assertThat(left).hasSize(1);
        assertThat(jobService.calls(left.getFirst())).hasSize(1);
        assertThat(jobService.purge(new JobFilter(admin.getId(), kind, null, null, null))).isEqualTo(1);
        assertThat(jobService.count(new JobFilter(admin.getId(), kind, null, null, null))).isZero();
    }

    @Test
    void onlyAnAdministratorReadsOrPurgesTheLog() throws Exception {
        loginAdmin();
        User other = createUser();
        loginAs(other);

        assertThatThrownBy(() -> jobService.search(JobFilter.ALL, 0, 10)).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> jobService.count(JobFilter.ALL)).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> jobService.kinds()).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> jobService.active()).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> jobService.purge(JobFilter.ALL)).isInstanceOf(SecurityException.class);
    }

    @Test
    void aRequestToAModelIsACallOfTheJobOfTheThreadWithTheKindOfItsSession() throws Exception {
        loginAdmin();
        OpenAICompatible ai = createAI();
        llm.enqueue(MockLLMResponse.chunks("Hi ", "there"));
        List<FinishedJob> ended = Collections.synchronizedList(new ArrayList<>());
        Consumer<FinishedJob> sink = ended::add;
        Jobs.addSink(sink);
        try {
            InferenceCollector collector = new InferenceCollector();
            try (InferenceSession _ = InferenceSession.open(InferenceSession.ANNOTATE)) {
                inferenceServices.forAI(ai).stream(List.of(LLMChatMessage.of(LLMRole.USER, "Hello.")), null, collector);
            }
            collector.await(Duration.ofSeconds(10));
            long deadline = System.currentTimeMillis() + 5000;
            while (ended.isEmpty() && System.currentTimeMillis() < deadline) {
                Thread.sleep(20);
            }
        } finally {
            Jobs.removeSink(sink);
        }

        assertThat(ended).singleElement().satisfies(job -> {
            assertThat(job.kind()).isEqualTo(InferenceSession.ANNOTATE);
            assertThat(job.status()).isEqualTo(JobStatus.COMPLETED);
            assertThat(job.calls()).singleElement().satisfies(call -> {
                assertThat(call.type()).isEqualTo(JobCallType.LLM);
                assertThat(call.backend()).isEqualTo(ai.getName());
                assertThat(call.model()).isEqualTo(ai.getModel());
                assertThat(call.status()).isEqualTo(JobStatus.COMPLETED);
            });
        });
    }

    @Test
    void theRequestsInsideAnOpenJobAreItsCalls() throws Exception {
        loginAdmin();
        OpenAICompatible ai = createAI();
        llm.enqueue(MockLLMResponse.chunks("one"));
        llm.enqueue(MockLLMResponse.chunks("two"));
        List<FinishedJob> ended = Collections.synchronizedList(new ArrayList<>());
        Consumer<FinishedJob> sink = ended::add;
        Jobs.addSink(sink);
        try {
            try (Jobs.Handle _ = Jobs.open("book-summary", "Chapters")) {
                for (int i = 0; i < 2; i++) {
                    InferenceCollector collector = new InferenceCollector();
                    inferenceServices.forAI(ai).stream(List.of(LLMChatMessage.of(LLMRole.USER, "Hello.")), null, collector);
                    collector.await(Duration.ofSeconds(10));
                }
            }
        } finally {
            Jobs.removeSink(sink);
        }

        assertThat(ended).singleElement().satisfies(job -> {
            assertThat(job.kind()).isEqualTo("book-summary");
            assertThat(job.label()).isEqualTo("Chapters");
            assertThat(job.calls()).hasSize(2);
        });
    }
}
