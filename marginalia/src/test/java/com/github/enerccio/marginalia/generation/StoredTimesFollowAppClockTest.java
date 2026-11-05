package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.utils.AppClock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A generation run "on" another day (the dataset build does this): the times it stores come from {@link AppClock}, not from the system clock.
 */
class StoredTimesFollowAppClockTest extends GenerationTestBase {

    private static final Instant DAY = Instant.parse("2026-01-14T19:30:00Z");

    @AfterEach
    void resetClock() {
        AppClock.reset();
    }

    @Test
    void aGeneratedPartCarriesTheTimesOfTheSetClock() throws Exception {
        Manuscript manuscript = manuscriptService.save(newManuscript());
        AppClock.set(Clock.fixed(DAY, ZoneOffset.UTC));

        GenerationRun run = generate(manuscript, "write");
        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(run.getErrors()).isEmpty();

        ChatMessage message = run.getMessage();
        Date day = Date.from(DAY);
        assertThat(message.getCreation()).isEqualTo(day);
        assertThat(message.getModification()).isEqualTo(day);
        assertThat(message.getRequest()).isEqualTo(day);
        assertThat(message.getTtft()).isEqualTo(day);
    }
}
