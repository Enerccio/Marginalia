package com.github.enerccio.marginalia.extensions.httphooks;

import com.github.enerccio.marginalia.domain.service.job.FinishedJob;
import com.github.enerccio.marginalia.domain.service.job.JobCallType;
import com.github.enerccio.marginalia.domain.service.job.JobStatus;
import com.github.enerccio.marginalia.domain.service.job.Jobs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobCallsTest {

    private final List<FinishedJob> ended = Collections.synchronizedList(new ArrayList<>());

    @BeforeEach
    void install() {
        Jobs.reset();
        Jobs.addSink(ended::add);
    }

    @AfterEach
    void clean() {
        Jobs.reset();
    }

    @Test
    void aCallInsideAJobIsRecordedWithTheHostOnly() throws Exception {
        try (Jobs.Handle _ = Jobs.open("audiobook", "Chapter 1")) {
            String result = JobCalls.track(JobCallType.AUDIO, "audiobook", "http://tts.example.org:8004/v1?key=secret", "model-x", () -> "ok");
            assertEquals("ok", result);
        }

        assertEquals(1, ended.size());
        FinishedJob job = ended.get(0);
        assertEquals("audiobook", job.kind());
        assertEquals(1, job.calls().size());
        assertEquals(JobCallType.AUDIO, job.calls().get(0).type());
        assertEquals("tts.example.org", job.calls().get(0).backend());
        assertEquals("model-x", job.calls().get(0).model());
        assertEquals(JobStatus.COMPLETED, job.calls().get(0).status());
    }

    @Test
    void aCallOutsideOfAJobIsAJobOfTheKindOfThePlugin() throws Exception {
        JobCalls.track(JobCallType.IMAGE, "image-generator", "http://comfy.local:8188", null, () -> 1);

        assertEquals(1, ended.size());
        assertEquals("image-generator", ended.get(0).kind());
    }

    @Test
    void aFailureIsRecordedAndRethrownAndCancellationIsNotAFailure() {
        assertThrows(BackendException.class, () -> JobCalls.track(JobCallType.AUDIO, "audiobook", "http://a", null, () -> {
            throw new BackendException("server said no");
        }));
        assertThrows(BackendException.class, () -> JobCalls.track(JobCallType.AUDIO, "audiobook", "http://a", null, () -> {
            throw new BackendException("Cancelled");
        }));

        assertEquals(JobStatus.FAILED, ended.get(0).status());
        assertEquals(JobStatus.CANCELLED, ended.get(1).status());
        assertEquals("server said no", ended.get(0).calls().get(0).error());
    }

    @Test
    void theHostOfAnAddress() {
        assertEquals("example.org", JobCalls.hostOf("https://example.org/v1"));
        assertEquals(null, JobCalls.hostOf(" "));
    }
}
