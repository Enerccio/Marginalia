package com.github.enerccio.marginalia.jobs;

import com.github.enerccio.marginalia.domain.service.job.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The jobs and their calls ({@link Jobs}), without the application: what is a job, when does it end, what a call outside of
 * a job does.
 */
class JobsTest {

    private final List<FinishedJob> ended = Collections.synchronizedList(new ArrayList<>());

    @BeforeEach
    void install() {
        Jobs.reset();
        Jobs.setOwnerResolver(() -> new Jobs.Owner(7L, "alice"));
        Jobs.addSink(ended::add);
    }

    @AfterEach
    void clean() {
        Jobs.reset();
    }

    @Test
    void closingTheHandleEndsTheJobWithItsCalls() {
        try (Jobs.Handle job = Jobs.open("audiobook", "Chapter 3", JobCallType.AUDIO)) {
            assertThat(Jobs.current()).isSameAs(job);
            assertThat(Jobs.active()).singleElement().satisfies(active -> {
                assertThat(active.kind()).isEqualTo("audiobook");
                assertThat(active.label()).isEqualTo("Chapter 3");
                assertThat(active.login()).isEqualTo("alice");
            });
            Jobs.beginCall("tts.example.org").finish(JobStatus.COMPLETED, null);
            Jobs.beginCall(JobCallType.IMAGE, "comfy.local", "flux").finish(JobStatus.FAILED, "boom");
        }

        assertThat(Jobs.current()).isNull();
        assertThat(Jobs.active()).isEmpty();
        assertThat(ended).singleElement().satisfies(job -> {
            assertThat(job.status()).isEqualTo(JobStatus.COMPLETED);
            assertThat(job.userId()).isEqualTo(7L);
            assertThat(job.endedAt()).isAfterOrEqualTo(job.startedAt());
            assertThat(job.callCount()).isEqualTo(2);
            assertThat(job.failedCalls()).isEqualTo(1);
            assertThat(job.calls()).extracting(FinishedCall::type).containsExactly(JobCallType.AUDIO, JobCallType.IMAGE);
            assertThat(job.calls().get(1).error()).isEqualTo("boom");
            assertThat(job.calls().get(1).model()).isEqualTo("flux");
        });
    }

    @Test
    void aCallOutsideOfAJobIsNotRecorded() {
        assertThat(Jobs.beginCall("health.example.org")).isNull();
        assertThat(Jobs.beginCall(JobCallType.LLM, "x", null)).isNull();
        assertThat(Jobs.active()).isEmpty();
        assertThat(ended).isEmpty();
    }

    @Test
    void aRequestOutsideOfAJobIsAJobOfItsOwnThatEndsWithTheCall() {
        Jobs.Call call = Jobs.beginCallOrJob(JobCallType.LLM, "my ai", "model-1", "annotate");

        assertThat(Jobs.active()).singleElement().satisfies(active -> {
            assertThat(active.kind()).isEqualTo("annotate");
            assertThat(active.runningCalls()).isEqualTo(1);
        });
        call.finish(JobStatus.COMPLETED, null);
        call.finish(JobStatus.FAILED, "ignored: the first end counts");

        assertThat(Jobs.active()).isEmpty();
        assertThat(ended).singleElement().satisfies(job -> {
            assertThat(job.kind()).isEqualTo("annotate");
            assertThat(job.status()).isEqualTo(JobStatus.COMPLETED);
            assertThat(job.failedCalls()).isZero();
        });
    }

    @Test
    void anUntaggedRequestGetsTheUntaggedKind() {
        Jobs.beginCallOrJob(JobCallType.LLM, "ai", null, null).finish(JobStatus.CANCELLED, null);

        assertThat(ended).singleElement().satisfies(job -> {
            assertThat(job.kind()).isEqualTo(Jobs.UNTAGGED);
            assertThat(job.status()).isEqualTo(JobStatus.CANCELLED);
        });
    }

    @Test
    void aRequestInsideAJobIsACallOfIt() {
        try (Jobs.Handle _ = Jobs.open("summary", null)) {
            Jobs.beginCallOrJob(JobCallType.LLM, "ai", "m", "summary").finish(JobStatus.COMPLETED, null);
            Jobs.beginCallOrJob(JobCallType.LLM, "ai", "m", "summary").finish(JobStatus.COMPLETED, null);
            // the job goes on after a call ended
            assertThat(Jobs.active()).hasSize(1);
        }

        assertThat(ended).singleElement().satisfies(job -> assertThat(job.callCount()).isEqualTo(2));
    }

    @Test
    void anAsynchronousJobEndsWhenFinishedNotWhenClosed() throws Exception {
        Jobs.Handle job = Jobs.openAsync("generation", "Next part", JobCallType.LLM);
        Jobs.Call call = Jobs.beginCall(JobCallType.LLM, "ai", null);
        job.close();

        assertThat(Jobs.current()).isNull();
        assertThat(Jobs.active()).singleElement().satisfies(active -> assertThat(active.runningCalls()).isEqualTo(1));

        // another thread takes the job over and ends the call, then the job
        CompletableFuture.runAsync(() -> {
            try (Jobs.Scope _ = job.enter()) {
                assertThat(Jobs.current()).isSameAs(job);
                call.finish(JobStatus.COMPLETED, null);
            }
            assertThat(Jobs.current()).isNull();
        }).get();
        assertThat(ended).isEmpty();
        job.finish(JobStatus.COMPLETED);
        job.finish(JobStatus.FAILED);

        assertThat(ended).singleElement().satisfies(done -> assertThat(done.status()).isEqualTo(JobStatus.COMPLETED));
    }

    @Test
    void aCallStillRunningWhenItsJobEndsIsMarkedCancelled() {
        Jobs.Handle job = Jobs.openAsync("agent", null, JobCallType.LLM);
        Jobs.beginCall(JobCallType.LLM, "ai", null);
        job.close();
        job.finish(JobStatus.CANCELLED);

        assertThat(ended).singleElement().satisfies(done -> {
            assertThat(done.status()).isEqualTo(JobStatus.CANCELLED);
            assertThat(done.calls()).singleElement().satisfies(call -> assertThat(call.status()).isEqualTo(JobStatus.CANCELLED));
        });
    }

    @Test
    void nestedJobsRestoreTheOuterOne() {
        try (Jobs.Handle outer = Jobs.open("outer", null)) {
            try (Jobs.Handle inner = Jobs.open("inner", null)) {
                assertThat(Jobs.current()).isSameAs(inner);
            }
            assertThat(Jobs.current()).isSameAs(outer);
        }
        assertThat(ended).extracting(FinishedJob::kind).containsExactly("inner", "outer");
    }

    @Test
    void failMarksTheJobFailed() {
        try (Jobs.Handle job = Jobs.open("x", null)) {
            job.fail();
        }

        assertThat(ended).singleElement().satisfies(done -> assertThat(done.status()).isEqualTo(JobStatus.FAILED));
    }

    @Test
    void sweepEndsAJobNobodyEndedOnceItIsIdleButNotOneWithACallInFlight() {
        Jobs.Handle forgotten = Jobs.openAsync("forgotten", null, JobCallType.OTHER);
        forgotten.close();
        Jobs.Handle busy = Jobs.openAsync("busy", null, JobCallType.OTHER);
        Jobs.beginCall(JobCallType.OTHER, "slow.example.org", null);
        busy.close();

        Jobs.sweep(Jobs.IDLE_LIMIT);
        assertThat(ended).isEmpty();

        Jobs.sweep(java.time.Duration.ofSeconds(-1));
        assertThat(ended).extracting(FinishedJob::kind).containsExactly("forgotten");
        assertThat(ended.get(0).status()).isEqualTo(JobStatus.COMPLETED);
    }

    @Test
    void theKindIsRequired() {
        assertThatThrownBy(() -> Jobs.open(" ", null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aSinkThatThrowsDoesNotBreakTheJob() {
        Jobs.addSink(_ -> {
            throw new IllegalStateException("log is down");
        });

        try (Jobs.Handle _ = Jobs.open("x", null)) {
            Jobs.beginCall("host");
        }

        assertThat(ended).hasSize(1);
    }
}
