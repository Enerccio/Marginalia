package com.github.enerccio.marginalia.statistics;

import com.github.enerccio.marginalia.domain.collections.ActivityKind;
import com.github.enerccio.marginalia.domain.collections.StatisticSide;
import com.github.enerccio.marginalia.domain.components.StatisticCounts;
import com.github.enerccio.marginalia.domain.components.StatisticFilter;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.AppSettings;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.ManuscriptStatisticServiceImpl;
import com.github.enerccio.marginalia.domain.service.impl.StatisticsPolicyServiceImpl;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.AopTestUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The privacy side of the writing statistics: the switches that stop recording (the user's and the instance's), how long the rows
 * are kept, and the clearing of a user's history or of everybody's by the administrator.
 */
class StatisticsPolicyTest extends MarginaliaTestBase {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 30);

    @Autowired
    private StatisticsPolicyService policy;

    @Autowired
    private ManuscriptStatisticService statisticService;

    @Autowired
    private ActivityIntervalService intervalService;

    @Autowired
    private ManuscriptSizeService sizeService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SettingService settingService;

    private User user;

    @BeforeEach
    void setUp() throws Exception {
        user = loginAdmin();
        setDay(TODAY);
        policy.invalidate();
    }

    @AfterEach
    void restore() throws Exception {
        loginAs(user);
        policy.setInstance(true, 0);
        setClock(Clock.systemDefaultZone());
        policy.invalidate();
    }

    private void setDay(LocalDate day) {
        setClock(Clock.fixed(day.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));
    }

    private void setClock(Clock clock) {
        ((ManuscriptStatisticServiceImpl) AopTestUtils.getUltimateTargetObject(statisticService)).setClock(clock);
        ((StatisticsPolicyServiceImpl) AopTestUtils.getUltimateTargetObject(policy)).setClock(clock);
    }

    private Manuscript book() throws Exception {
        Manuscript book = new Manuscript();
        book.setName(uniqueName("book"));
        return manuscriptService.save(book);
    }

    private void userSettings(Boolean recording, Integer months) throws Exception {
        UserSetting setting = settingService.getOrCreate(UserSetting.class, user);
        setting.setStatisticsRecording(recording);
        setting.setStatisticsKeepMonths(months);
        settingService.save(setting);
        policy.invalidate();
    }

    private static StatisticFilter widest() {
        return StatisticFilter.of(LocalDate.of(2000, 1, 1), LocalDate.of(2100, 1, 1), StatisticSide.ALL);
    }

    // ------------------------------------------------------------------------------------------------------------
    // recording
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void recordsByDefault() throws Exception {
        assertThat(policy.isRecording(user)).isTrue();
        assertThat(policy.keepMonths(user)).isZero();
        assertThat(policy.cutoff(user)).isNull();
        assertThat(policy.isInstanceRecording()).isTrue();
    }

    @Test
    void aUserWhoSwitchedItOffRecordsNothingNewAndKeepsWhatWasThere() throws Exception {
        Manuscript book = book();
        statisticService.count(book, StatisticCounts.wordsWritten(5));

        userSettings(Boolean.FALSE, null);
        statisticService.count(book, StatisticCounts.wordsWritten(7));
        sizeService.set(book, TODAY, 10, 15, 10, 15, null, false);

        assertThat(policy.isRecording(user)).isFalse();
        assertThat(statisticService.find(book.getUuid(), TODAY, false).getWordsWritten()).isEqualTo(5);
        assertThat(sizeService.sizeAt(book.getUuid(), TODAY)).isNull();

        userSettings(null, null);
        statisticService.count(book, StatisticCounts.wordsWritten(2));
        assertThat(statisticService.find(book.getUuid(), TODAY, false).getWordsWritten()).isEqualTo(7);
    }

    @Test
    void theInstanceSwitchStopsEveryoneWhateverTheyChose() throws Exception {
        Manuscript book = book();

        policy.setInstance(false, 0);
        statisticService.count(book, StatisticCounts.wordsWritten(5));

        assertThat(policy.isInstanceRecording()).isFalse();
        assertThat(policy.isRecording(user)).isFalse();
        assertThat(statisticService.find(book.getUuid(), TODAY, false)).isNull();

        policy.setInstance(true, 0);
        statisticService.count(book, StatisticCounts.wordsWritten(5));
        assertThat(statisticService.find(book.getUuid(), TODAY, false).getWordsWritten()).isEqualTo(5);
    }

    @Test
    void theInstanceSettingsAreForAdministratorsOnly() throws Exception {
        loginAs(createUser());

        assertThatThrownBy(() -> policy.setInstance(false, 3)).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> policy.clearEveryone()).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> policy.clearUser(user)).isInstanceOf(SecurityException.class);
        assertThat(policy.isInstanceRecording()).isTrue();
    }

    // ------------------------------------------------------------------------------------------------------------
    // retention
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void theShorterLimitWins() throws Exception {
        assertThat(StatisticsPolicyServiceImpl.shorter(null, null)).isZero();
        assertThat(StatisticsPolicyServiceImpl.shorter(0, 6)).isEqualTo(6);
        assertThat(StatisticsPolicyServiceImpl.shorter(12, null)).isEqualTo(12);
        assertThat(StatisticsPolicyServiceImpl.shorter(12, 6)).isEqualTo(6);
        assertThat(StatisticsPolicyServiceImpl.shorter(3, 6)).isEqualTo(3);
        assertThat(StatisticsPolicyServiceImpl.shorter(-4, 6)).isEqualTo(6);

        userSettings(null, 6);
        assertThat(policy.keepMonths(user)).isEqualTo(6);
        policy.setInstance(true, 3);
        assertThat(policy.keepMonths(user)).isEqualTo(3);
        assertThat(policy.cutoff(user)).isEqualTo(TODAY.minusMonths(3));
        policy.setInstance(true, 12);
        assertThat(policy.keepMonths(user)).isEqualTo(6);
    }

    @Test
    void pruningDeletesTheOldDaysOfAllThreeKindsAndKeepsTheSizeTheGrowthStartsFrom() throws Exception {
        Manuscript book = book();
        LocalDate old = TODAY.minusMonths(8);
        LocalDate older = TODAY.minusMonths(9);
        LocalDate recent = TODAY.minusDays(10);

        setDay(older);
        statisticService.count(book, StatisticCounts.wordsWritten(1));
        sizeService.set(book, older, 100, 150, 100, 150, null, false);
        setDay(old);
        statisticService.count(book, StatisticCounts.wordsWritten(2));
        sizeService.set(book, old, 200, 250, 200, 250, null, false);
        intervalService.extend(intervalService.start(book, ActivityKind.EDIT, false, dateOf(old)).getId(), dateOf(old));
        setDay(recent);
        statisticService.count(book, StatisticCounts.wordsWritten(4));
        sizeService.set(book, recent, 300, 350, 300, 350, null, false);
        intervalService.extend(intervalService.start(book, ActivityKind.EDIT, false, dateOf(recent)).getId(), dateOf(recent));
        setDay(TODAY);

        userSettings(null, 6);
        int deleted = policy.prune(user);

        assertThat(deleted).isEqualTo(1 + 1 + 1 + 1);
        assertThat(statisticService.find(book.getUuid(), older, false)).isNull();
        assertThat(statisticService.find(book.getUuid(), old, false)).isNull();
        assertThat(statisticService.find(book.getUuid(), recent, false).getWordsWritten()).isEqualTo(4);
        assertThat(intervalService.findRows(widest().forBook(book.getUuid()), null)).hasSize(1);
        // the size of the last day before the cutoff stays: the growth of the first kept day is measured from it
        assertThat(sizeService.sizeAt(book.getUuid(), older)).isNull();
        assertThat(sizeService.sizeAt(book.getUuid(), old).getBookWords()).isEqualTo(200);
        assertThat(sizeService.sizeAt(book.getUuid(), recent).getBookWords()).isEqualTo(300);
        assertThat(sizeService.growth(book.getUuid(), recent, TODAY).bookWords()).isEqualTo(100);
    }

    @Test
    void pruningWithoutALimitDeletesNothing() throws Exception {
        Manuscript book = book();
        setDay(TODAY.minusYears(3));
        statisticService.count(book, StatisticCounts.wordsWritten(1));
        setDay(TODAY);

        assertThat(policy.prune(user)).isZero();
        assertThat(policy.pruneAll()).isZero();
        assertThat(statisticService.findRange(widest())).hasSize(1);
    }

    @Test
    void theInstanceLimitPrunesEveryUserAtOnce() throws Exception {
        Manuscript mine = book();
        setDay(TODAY.minusMonths(5));
        statisticService.count(mine, StatisticCounts.wordsWritten(1));
        User other = loginAs(createUser());
        Manuscript theirs = book();
        statisticService.count(theirs, StatisticCounts.wordsWritten(1));
        setDay(TODAY);
        loginAs(user);

        policy.setInstance(true, 2);

        assertThat(statisticService.findRange(widest())).isEmpty();
        loginAs(other);
        assertThat(statisticService.findRange(widest())).isEmpty();
    }

    private static Date dateOf(LocalDate day) {
        return Date.from(day.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant());
    }

    // ------------------------------------------------------------------------------------------------------------
    // clearing by the administrator
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void anAdministratorClearsOneUsersHistoryAndLeavesTheOthers() throws Exception {
        Manuscript mine = book();
        statisticService.count(mine, StatisticCounts.wordsWritten(3));
        sizeService.set(mine, TODAY, 10, 15, 10, 15, null, false);
        intervalService.extend(intervalService.start(mine, ActivityKind.OPEN, false, new Date()).getId(), new Date());
        User other = loginAs(createUser());
        Manuscript theirs = book();
        statisticService.count(theirs, StatisticCounts.wordsWritten(9));
        loginAs(user);

        policy.clearUser(user);

        assertThat(statisticService.findRange(widest())).isEmpty();
        assertThat(sizeService.findRange(null, LocalDate.of(2000, 1, 1), LocalDate.of(2100, 1, 1))).isEmpty();
        assertThat(intervalService.findRows(widest(), null)).isEmpty();
        loginAs(other);
        assertThat(statisticService.find(theirs.getUuid(), TODAY, false).getWordsWritten()).isEqualTo(9);
    }

    @Test
    void anAdministratorClearsEverybodysHistory() throws Exception {
        Manuscript mine = book();
        statisticService.count(mine, StatisticCounts.wordsWritten(3));
        User other = loginAs(createUser());
        statisticService.count(book(), StatisticCounts.wordsWritten(9));
        loginAs(user);

        policy.clearEveryone();

        assertThat(statisticService.findRange(widest())).isEmpty();
        loginAs(other);
        assertThat(statisticService.findRange(widest())).isEmpty();
    }

    @Test
    void theAppSettingsKeepTheSwitchAsAnExtendedAttribute() throws Exception {
        policy.setInstance(false, 4);

        AppSettings app = settingService.find(AppSettings.class, null);

        assertThat(app.isStatisticsRecording()).isFalse();
        assertThat(app.getStatisticsKeepMonths()).isEqualTo(4);
        assertThat(policy.getInstanceKeepMonths()).isEqualTo(4);
    }
}
