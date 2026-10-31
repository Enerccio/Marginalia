package com.github.enerccio.marginalia.statistics;

import com.github.enerccio.marginalia.domain.collections.ActivityKind;
import com.github.enerccio.marginalia.domain.collections.StatisticSide;
import com.github.enerccio.marginalia.domain.components.ActivityRow;
import com.github.enerccio.marginalia.domain.components.ActivityTimes;
import com.github.enerccio.marginalia.domain.components.StatisticFilter;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ActivityIntervalService;
import com.github.enerccio.marginalia.domain.service.ActivityTracker;
import com.github.enerccio.marginalia.domain.service.ActivityTracker.BookPresence;
import com.github.enerccio.marginalia.domain.service.ActivityTracker.Registration;
import com.github.enerccio.marginalia.domain.service.impl.ActivityTrackerImpl;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.AopTestUtils;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The tracker turns registrations into the stretches of the writing statistics. The tests drive its clock and its tick, and
 * write with {@code flush}, so nothing waits for a minute.
 */
class ActivityTrackerTest extends AgentTestBase {

    private static final long SECOND = 1000;
    private static final long MINUTE = 60 * SECOND;
    private static final LocalDate DAY = LocalDate.of(2026, 10, 30);

    /** A clock that stands where the test puts it. */
    private static final class TestClock extends Clock {
        long millis;

        TestClock(LocalDateTime at) {
            millis = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }

        void advance(long millis) {
            this.millis += millis;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.systemDefault();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis);
        }

        @Override
        public long millis() {
            return millis;
        }
    }

    @Autowired
    private ActivityTracker tracker;

    @Autowired
    private ActivityIntervalService intervalService;

    private ActivityTrackerImpl impl;
    private TestClock clock;
    private Manuscript book;
    private long t0;

    @BeforeEach
    void setUp() throws Exception {
        loginAgentUser();
        impl = (ActivityTrackerImpl) AopTestUtils.getUltimateTargetObject(tracker);
        impl.setManual(true);
        impl.forget();
        impl.setTickMillis(MINUTE);
        impl.setGenerateLingerMillis(2 * SECOND);
        clock = new TestClock(DAY.atTime(12, 0));
        t0 = clock.millis;
        impl.setClock(clock);
        book = manuscript();
    }

    @AfterEach
    void restore() {
        impl.forget();
        impl.setClock(Clock.systemDefaultZone());
        impl.setManual(false);
    }

    private void tick(long millis) {
        clock.advance(millis);
        impl.tick();
    }

    private List<ActivityRow> rows(Manuscript of, ActivityKind kind) throws Exception {
        impl.flush();
        StatisticFilter filter = StatisticFilter.of(DAY.minusDays(1), DAY.plusDays(2), StatisticSide.ALL).forBook(of.getUuid());
        return intervalService.findRows(filter, List.of(kind));
    }

    private long length(ActivityRow row) {
        return row.endTime().getTime() - row.startTime().getTime();
    }

    @Test
    void aRequestThatEndsIsOneRowThatEndsWhereItEnded() throws Exception {
        Registration registration = tracker.generate(book.getId());
        clock.advance(3500);
        registration.close();
        clock.advance(10 * MINUTE);

        List<ActivityRow> rows = rows(book, ActivityKind.GENERATE);

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().startTime().getTime()).isEqualTo(t0);
        assertThat(length(rows.getFirst())).as("the close, not the next tick").isEqualTo(3500);
        assertThat(rows.getFirst().agent()).isFalse();
    }

    @Test
    void requestsAtOnceAreOneStretch() throws Exception {
        Registration first = tracker.generate(book.getId());
        clock.advance(2 * SECOND);
        Registration second = tracker.generate(book.getId());
        clock.advance(4 * SECOND);
        first.close();
        clock.advance(3 * SECOND);
        second.close();

        List<ActivityRow> rows = rows(book, ActivityKind.GENERATE);

        assertThat(rows).hasSize(1);
        assertThat(length(rows.getFirst())).isEqualTo(9 * SECOND);
    }

    @Test
    void roundsBackToBackInsideTheLingerAreOneRowAndTheGapIsCounted() throws Exception {
        Registration first = tracker.generate(book.getId());
        clock.advance(5 * SECOND);
        first.close();
        clock.advance(SECOND);
        Registration second = tracker.generate(book.getId());
        clock.advance(5 * SECOND);
        second.close();

        List<ActivityRow> rows = rows(book, ActivityKind.GENERATE);

        assertThat(rows).hasSize(1);
        assertThat(length(rows.getFirst())).isEqualTo(11 * SECOND);
    }

    @Test
    void roundsApartAreTwoRows() throws Exception {
        Registration first = tracker.generate(book.getId());
        clock.advance(5 * SECOND);
        first.close();
        clock.advance(10 * SECOND);
        impl.flush();
        Registration second = tracker.generate(book.getId());
        clock.advance(5 * SECOND);
        second.close();

        List<ActivityRow> rows = rows(book, ActivityKind.GENERATE);

        assertThat(rows).hasSize(2);
        assertThat(rows).allSatisfy(r -> assertThat(length(r)).isEqualTo(5 * SECOND));
        assertThat(rows.get(1).startTime().getTime()).isEqualTo(t0 + 15 * SECOND);
    }

    @Test
    void closingTwiceChangesNothing() throws Exception {
        Registration registration = tracker.generate(book.getId());
        clock.advance(SECOND);
        registration.close();
        clock.advance(SECOND);
        registration.close();

        assertThat(rows(book, ActivityKind.GENERATE)).hasSize(1);
        assertThat(length(rows(book, ActivityKind.GENERATE).getFirst())).isEqualTo(SECOND);
    }

    @Test
    void twoBooksAtTheSameTimeAreTwoRowsThatOverlap() throws Exception {
        Manuscript other = manuscript();
        Registration a = tracker.generate(book.getId());
        Registration b = tracker.generate(other.getId());
        clock.advance(10 * SECOND);
        a.close();
        b.close();

        List<ActivityRow> first = rows(book, ActivityKind.GENERATE);
        List<ActivityRow> second = rows(other, ActivityKind.GENERATE);

        assertThat(first).hasSize(1);
        assertThat(second).hasSize(1);
        assertThat(first.getFirst().startTime()).isEqualTo(second.getFirst().startTime());
        assertThat(length(first.getFirst())).isEqualTo(10 * SECOND);
    }

    @Test
    void aRequestWithoutABookRegistersNothing() throws Exception {
        Registration none = tracker.generate(null);
        clock.advance(SECOND);
        none.close();

        assertThat(rows(book, ActivityKind.GENERATE)).isEmpty();
    }

    @Test
    void theOpenBookIsOpenWhileItsWindowIsAndTheTickMovesTheEnd() throws Exception {
        BookPresence presence = tracker.open(book);

        tick(MINUTE);
        tick(MINUTE);
        List<ActivityRow> during = rows(book, ActivityKind.OPEN);
        assertThat(during).hasSize(1);
        assertThat(length(during.getFirst())).as("the end follows the tick while it is open").isEqualTo(2 * MINUTE);

        clock.advance(30 * SECOND);
        presence.close();
        List<ActivityRow> after = rows(book, ActivityKind.OPEN);
        assertThat(after).hasSize(1);
        assertThat(length(after.getFirst())).isEqualTo(2 * MINUTE + 30 * SECOND);
    }

    @Test
    void theSameBookInTwoWindowsIsOneStretch() throws Exception {
        BookPresence first = tracker.open(book);
        tick(MINUTE);
        BookPresence second = tracker.open(book);
        tick(MINUTE);
        first.close();
        tick(MINUTE);
        second.close();

        List<ActivityRow> rows = rows(book, ActivityKind.OPEN);

        assertThat(rows).hasSize(1);
        assertThat(length(rows.getFirst())).isEqualTo(3 * MINUTE);
    }

    @Test
    void aHiddenPageDoesNotCountAndComingBackStartsANewStretch() throws Exception {
        BookPresence presence = tracker.open(book);
        tick(MINUTE);
        presence.visible(false);
        tick(8 * MINUTE);
        presence.visible(true);
        tick(MINUTE);
        presence.close();

        List<ActivityRow> rows = rows(book, ActivityKind.OPEN);

        assertThat(rows).hasSize(2);
        assertThat(length(rows.get(0))).isEqualTo(MINUTE);
        assertThat(length(rows.get(1))).isEqualTo(MINUTE);
        assertThat(rows.get(1).startTime().getTime()).isEqualTo(t0 + 9 * MINUTE);
    }

    @Test
    void editingCountsOnlyWhileTheAuthorTypesAndIsInsideOpen() throws Exception {
        BookPresence presence = tracker.open(book);
        tick(MINUTE);
        presence.editing(true);
        tick(MINUTE);
        presence.editing(false);
        tick(MINUTE);
        presence.close();

        List<ActivityRow> edit = rows(book, ActivityKind.EDIT);
        List<ActivityRow> open = rows(book, ActivityKind.OPEN);

        assertThat(edit).hasSize(1);
        assertThat(length(edit.getFirst())).isEqualTo(MINUTE);
        assertThat(edit.getFirst().startTime().getTime()).isEqualTo(t0 + MINUTE);
        assertThat(length(open.getFirst())).isEqualTo(3 * MINUTE);
    }

    @Test
    void aHiddenPageStopsTheEditingToo() throws Exception {
        BookPresence presence = tracker.open(book);
        presence.editing(true);
        tick(MINUTE);
        presence.visible(false);
        tick(MINUTE);
        presence.close();

        assertThat(length(rows(book, ActivityKind.EDIT).getFirst())).isEqualTo(MINUTE);
    }

    @Test
    void editingStopsFiveMinutesAfterTheLastKeyWhenTheBrowserSaidNothing() throws Exception {
        BookPresence presence = tracker.open(book);
        presence.editing(true);
        for (int i = 0; i < 9; i++) {
            tick(MINUTE);
        }

        List<ActivityRow> rows = rows(book, ActivityKind.EDIT);

        assertThat(rows).hasSize(1);
        assertThat(length(rows.getFirst())).as("the message about being idle was lost: the cut-off ends it").isEqualTo(5 * MINUTE);
        assertThat(length(rows(book, ActivityKind.OPEN).getFirst())).as("reading is still open time").isEqualTo(9 * MINUTE);
        presence.close();
    }

    @Test
    void aKeyAfterTheIdlenessStartsEditingAgain() throws Exception {
        BookPresence presence = tracker.open(book);
        presence.editing(true);
        tick(MINUTE);
        presence.editing(false);
        tick(10 * MINUTE);
        presence.editing(true);
        tick(MINUTE);
        presence.close();

        List<ActivityRow> rows = rows(book, ActivityKind.EDIT);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(1).startTime().getTime()).isEqualTo(t0 + 11 * MINUTE);
    }

    @Test
    void aTypistWhoTheBrowserKeepsReportingIsNotCutOff() throws Exception {
        BookPresence presence = tracker.open(book);
        for (int i = 0; i < 12; i++) {
            presence.editing(true);
            tick(MINUTE);
        }
        presence.editing(false);

        assertThat(length(rows(book, ActivityKind.EDIT).getFirst())).isEqualTo(12 * MINUTE);
        presence.close();
    }

    @Test
    void aStretchAcrossMidnightIsTwoRowsThatMeetAtMidnight() throws Exception {
        clock.millis = DAY.atTime(23, 59, 30).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        BookPresence presence = tracker.open(book);
        tick(MINUTE);
        presence.close();

        List<ActivityRow> rows = rows(book, ActivityKind.OPEN);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).day()).isEqualTo(DAY);
        assertThat(rows.get(1).day()).isEqualTo(DAY.plusDays(1));
        long midnight = DAY.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        assertThat(rows.get(0).endTime().getTime()).isEqualTo(midnight);
        assertThat(rows.get(1).startTime().getTime()).isEqualTo(midnight);
        assertThat(length(rows.get(0)) + length(rows.get(1))).isEqualTo(MINUTE);
    }

    @Test
    void aRegistrationThatNeverClosesIsDroppedAtItsCap() throws Exception {
        impl.setTickMillis(10 * MINUTE);
        Registration leaked = tracker.generate(book.getId());
        for (int i = 0; i < 8; i++) {
            tick(10 * MINUTE);
        }

        List<ActivityRow> rows = rows(book, ActivityKind.GENERATE);

        assertThat(rows).hasSize(1);
        assertThat(length(rows.getFirst())).as("an hour is the cap, the tick after it drops it").isEqualTo(60 * MINUTE);
        leaked.close();
    }

    @Test
    void aComputerThatSleptIsNotCountedAsWorking() throws Exception {
        BookPresence presence = tracker.open(book);
        tick(MINUTE);
        // no tick for hours
        clock.advance(5 * 60 * MINUTE);
        impl.tick();
        tick(MINUTE);
        presence.close();

        List<ActivityRow> rows = rows(book, ActivityKind.OPEN);

        assertThat(rows).hasSize(2);
        assertThat(length(rows.get(0))).isEqualTo(MINUTE);
        assertThat(length(rows.get(1))).isEqualTo(MINUTE);
    }

    @Test
    void anAgentRunCountsAsRunWhileItsStatusIsRunning() throws Exception {
        Agent agent = agentService.save(newAgent(book));
        AgentRun run = agentRunService.start(agent);

        // the tick that finds the run starts the stretch, the next one moves its end
        tick(MINUTE);
        tick(MINUTE);
        List<ActivityRow> during = rows(book, ActivityKind.RUN);
        assertThat(during).hasSize(1);
        assertThat(during.getFirst().agent()).isTrue();
        assertThat(length(during.getFirst())).isEqualTo(MINUTE);

        agentRunService.stop(run, true);
        tick(MINUTE);
        tick(MINUTE);

        List<ActivityRow> after = rows(book, ActivityKind.RUN);
        assertThat(after).hasSize(1);
        assertThat(length(after.getFirst())).as("the stop closed it at the tick that noticed").isEqualTo(2 * MINUTE);
    }

    @Test
    void theWorkOfAnAgentOnItsCopyIsGenerationOfTheOriginalOnTheAgentsSide() throws Exception {
        Agent agent = agentService.save(newAgent(book));
        AgentRun run = agentRunService.start(agent);
        Manuscript copy = agentRunService.find(run.getId()).getWorkingCopy();

        Registration registration = tracker.generate(copy.getId());
        clock.advance(4 * SECOND);
        registration.close();

        List<ActivityRow> rows = rows(book, ActivityKind.GENERATE);
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().agent()).isTrue();
        assertThat(length(rows.getFirst())).isEqualTo(4 * SECOND);
        assertThat(rows(copy, ActivityKind.GENERATE)).isEmpty();
        assertThat(tracker.open(copy)).as("an agent's copy is no window of the author").isNotNull();
        assertThat(rows(copy, ActivityKind.OPEN)).isEmpty();
    }

    @Test
    void thePlanExamplesOfTheShownTimeComeOutOfTheDatabase() throws Exception {
        Manuscript other = manuscript();
        // the author and an agent generate at the same time on two books
        Registration author = tracker.generate(book.getId());
        clock.advance(10 * SECOND);
        Agent agent = agentService.save(newAgent(other));
        agentRunService.start(agent);
        Manuscript copy = agentRunService.find(agentRunService.findUnfinished(agent).getId()).getWorkingCopy();
        Registration agents = tracker.generate(copy.getId());
        clock.advance(10 * SECOND);
        author.close();
        clock.advance(10 * SECOND);
        agents.close();
        impl.flush();

        StatisticFilter all = StatisticFilter.of(DAY.minusDays(1), DAY.plusDays(2), StatisticSide.ALL);
        ActivityTimes both = intervalService.times(all.forBook(book.getUuid()));
        ActivityTimes mine = intervalService.times(StatisticFilter.of(DAY, DAY, StatisticSide.AUTHOR).forBook(book.getUuid()));
        ActivityTimes theirs = intervalService.times(StatisticFilter.of(DAY, DAY, StatisticSide.AGENTS).forBook(other.getUuid()));

        assertThat(both.generate()).isEqualTo(20 * SECOND);
        assertThat(mine.generate()).isEqualTo(20 * SECOND);
        assertThat(theirs.generate()).isEqualTo(20 * SECOND);
        assertThat(intervalService.timesByBook(all)).containsKeys(book.getUuid(), other.getUuid());
        assertThat(intervalService.timesByDay(all).get(DAY).generate()).isGreaterThanOrEqualTo(30 * SECOND);
    }
}
