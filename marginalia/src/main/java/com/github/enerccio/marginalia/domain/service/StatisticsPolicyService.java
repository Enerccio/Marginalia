package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.security.model.User;

import java.time.LocalDate;

/**
 * The privacy side of the writing statistics ({@link ManuscriptStatisticService}, {@link ActivityIntervalService},
 * {@link ManuscriptSizeService}): whether anything is recorded for a user and how long it is kept, and the administrator's means
 * to clear the history of a user or of everybody.
 * <p>
 * Recording and retention are each decided twice, by the instance (the administrator, {@code AppSettings}) and by the user
 * ({@code UserSetting}); the stricter wins: nothing is recorded when either is off, and the shorter of the two times is kept. The
 * recording side works on a thread without a session. What the user may do with their own history (clear a book, clear all) is in
 * {@link ManuscriptStatisticService}.
 */
public interface StatisticsPolicyService {

    /**
     * Whether the counts, times and sizes of the books of the owner are recorded now. Never throws (a failure to read the settings
     * records). The answer may be up to a few seconds old, see {@link #invalidate()}.
     */
    boolean isRecording(User owner);

    /**
     * The number of months of the statistics that are kept for the user (the shorter of the instance's and the user's limit), zero for all
     * of it.
     */
    int keepMonths(User owner);

    /**
     * The first day that is kept for the user: the days before it are deleted by {@link #prune}; null when everything is kept.
     */
    LocalDate cutoff(User owner);

    /**
     * Deletes for good the statistics of the user that are older than they may be kept ({@link #cutoff}); of the sizes the newest row of
     * each book before the cutoff stays, the growth of the days after it is measured from it.
     *
     * @return number of deleted rows
     */
    int prune(User owner);

    /**
     * {@link #prune} for every user. A scheduled job calls it once a day and after the start.
     */
    int pruneAll();

    /**
     * Forgets what was read of the settings, after a setting was saved. The tests need it too, the application calls it from the places
     * that save the settings.
     */
    void invalidate();

    // ---- the administrator ----

    /**
     * Whether the instance records the statistics (the switch of the administrator, on by default).
     */
    boolean isInstanceRecording() throws Exception;

    /**
     * The months the instance keeps the statistics of every user, zero for all of it.
     */
    int getInstanceKeepMonths() throws Exception;

    /**
     * Sets the switch and the retention of the instance and prunes at once. For administrators.
     *
     * @param keepMonths zero keeps everything
     */
    void setInstance(boolean recording, int keepMonths) throws Exception;

    /**
     * Deletes for good all the statistics of the user. For administrators.
     */
    void clearUser(User user) throws Exception;

    /**
     * Deletes for good all the statistics of all the users. For administrators.
     */
    void clearEveryone() throws Exception;

}
