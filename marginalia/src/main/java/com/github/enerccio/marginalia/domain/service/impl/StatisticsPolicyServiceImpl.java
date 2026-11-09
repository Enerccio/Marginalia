package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.settings.AppSettings;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.repository.ActivityIntervalRepository;
import com.github.enerccio.marginalia.domain.repository.ManuscriptSizeRepository;
import com.github.enerccio.marginalia.domain.repository.ManuscriptStatisticRepository;
import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.domain.service.StatisticsPolicyService;
import com.github.enerccio.marginalia.utils.AppClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class StatisticsPolicyServiceImpl implements StatisticsPolicyService, ApplicationListener<ContextRefreshedEvent>, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(StatisticsPolicyServiceImpl.class);

    /** How long a read setting is believed: a count is made for every change of a book, the database is not asked each time. */
    static final long CACHE_MILLIS = 10_000;

    /** The daily prune, a quiet hour; and the first one a little after the start. */
    private static final String PRUNE_CRON = "0 20 4 * * *";
    private static final Duration FIRST_PRUNE_DELAY = Duration.ofMinutes(2);

    @Autowired
    private SettingService settingService;

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private AdminGuard adminGuard;

    @Autowired
    private ManuscriptStatisticRepository statisticRepository;

    @Autowired
    private ActivityIntervalRepository intervalRepository;

    @Autowired
    private ManuscriptSizeRepository sizeRepository;

    @Autowired
    @Qualifier("common")
    private PlatformTransactionManager transactionManager;

    /** What the settings said for a user: recording, months to keep, when it was read. */
    private record Policy(boolean recording, int keepMonths, long readAt) {
        boolean fresh() {
            return System.currentTimeMillis() - readAt < CACHE_MILLIS;
        }
    }

    private final Map<Long, Policy> policies = new ConcurrentHashMap<>();
    private final AtomicBoolean started = new AtomicBoolean();
    private ThreadPoolTaskScheduler scheduler;

    private java.time.Clock clock = AppClock.INSTANCE;

    @Override
    public boolean isRecording(User owner) {
        return policy(owner).recording();
    }

    @Override
    public int keepMonths(User owner) {
        return policy(owner).keepMonths();
    }

    @Override
    public LocalDate cutoff(User owner) {
        int months = keepMonths(owner);
        return months <= 0 ? null : LocalDate.now(clock).minusMonths(months);
    }

    private Policy policy(User owner) {
        if (owner == null || owner.getId() == null) {
            return new Policy(true, 0, System.currentTimeMillis());
        }
        Policy known = policies.get(owner.getId());
        if (known != null && known.fresh()) {
            return known;
        }
        try {
            AppSettings app = settingService.find(AppSettings.class, null);
            UserSetting mine = settingService.find(UserSetting.class, owner);
            boolean recording = (app == null || app.isStatisticsRecording()) && (mine == null || mine.isStatisticsRecording());
            int months = shorter(app == null ? null : app.getStatisticsKeepMonths(), mine == null ? null : mine.getStatisticsKeepMonths());
            known = new Policy(recording, months, System.currentTimeMillis());
            policies.put(owner.getId(), known);
            return known;
        } catch (Exception e) {
            // the settings can't be read: the work is recorded, a missing count is the worse mistake
            log.warn("Failed to read the statistics settings of user {}", owner.getId(), e);
            return new Policy(true, 0, System.currentTimeMillis());
        }
    }

    /** The shorter of two limits, zero or null is no limit. */
    public static int shorter(Integer a, Integer b) {
        int first = a == null ? 0 : Math.max(0, a);
        int second = b == null ? 0 : Math.max(0, b);
        if (first == 0) {
            return second;
        }
        return second == 0 ? first : Math.min(first, second);
    }

    @Override
    public void invalidate() {
        policies.clear();
    }

    @Override
    public int prune(User owner) {
        LocalDate cutoff = cutoff(owner);
        if (cutoff == null) {
            return 0;
        }
        try {
            Integer deleted = new TransactionTemplate(transactionManager).execute(status -> {
                try {
                    return statisticRepository.deleteBefore(owner, cutoff) + intervalRepository.deleteBefore(owner, cutoff)
                            + sizeRepository.deleteBefore(owner, cutoff);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
            return deleted == null ? 0 : deleted;
        } catch (Exception e) {
            log.warn("Failed to prune the statistics of user {}", owner.getId(), e);
            return 0;
        }
    }

    @Override
    public int pruneAll() {
        int deleted = 0;
        try {
            for (User user : userService.findAll()) {
                if (!user.isDeleted()) {
                    deleted += prune(user);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to prune the statistics", e);
        }
        return deleted;
    }

    // ---- the administrator ----

    @Override
    public boolean isInstanceRecording() throws Exception {
        AppSettings app = settingService.find(AppSettings.class, null);
        return app == null || app.isStatisticsRecording();
    }

    @Override
    public int getInstanceKeepMonths() throws Exception {
        AppSettings app = settingService.find(AppSettings.class, null);
        Integer months = app == null ? null : app.getStatisticsKeepMonths();
        return months == null ? 0 : Math.max(0, months);
    }

    @Override
    public void setInstance(boolean recording, int keepMonths) throws Exception {
        adminGuard.requireAdmin();
        AppSettings app = settingService.getOrCreateApp(AppSettings.class);
        app.setStatisticsRecording(recording ? null : Boolean.FALSE);
        app.setStatisticsKeepMonths(keepMonths <= 0 ? null : keepMonths);
        settingService.save(app);
        invalidate();
        pruneAll();
    }

    @Override
    public void clearUser(User user) throws Exception {
        adminGuard.requireAdmin();
        if (user == null) {
            return;
        }
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            try {
                statisticRepository.deleteAll(user);
                intervalRepository.deleteAll(user);
                sizeRepository.deleteAll(user);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @Override
    public void clearEveryone() throws Exception {
        adminGuard.requireAdmin();
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            try {
                statisticRepository.deleteEverything();
                intervalRepository.deleteEverything();
                sizeRepository.deleteEverything();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }

    // ---- the schedule ----

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        try {
            ThreadPoolTaskScheduler pool = new ThreadPoolTaskScheduler();
            pool.setPoolSize(1);
            pool.setThreadNamePrefix("statistics-prune-");
            pool.setDaemon(true);
            pool.setRemoveOnCancelPolicy(true);
            pool.initialize();
            scheduler = pool;
            pool.schedule(this::pruneAll, new CronTrigger(PRUNE_CRON));
            pool.schedule(this::pruneAll, Instant.now().plus(FIRST_PRUNE_DELAY));
        } catch (Exception e) {
            log.error("Failed to start the statistics prune schedule", e);
        }
    }

    @Override
    public void destroy() {
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }
    }

    public void setClock(java.time.Clock clock) {
        this.clock = clock;
    }
}
