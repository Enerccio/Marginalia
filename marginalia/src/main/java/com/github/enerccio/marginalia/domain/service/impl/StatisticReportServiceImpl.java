package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.ActivityKind;
import com.github.enerccio.marginalia.domain.collections.StatisticRange;
import com.github.enerccio.marginalia.domain.collections.StatisticSide;
import com.github.enerccio.marginalia.domain.components.*;
import com.github.enerccio.marginalia.domain.components.BookStatistic.BookState;
import com.github.enerccio.marginalia.domain.components.StatisticReport.BookLine;
import com.github.enerccio.marginalia.domain.components.StatisticReport.DayLine;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptSize;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptStatistic;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDate;
import java.util.*;

public class StatisticReportServiceImpl implements StatisticReportService {

    private static final Logger log = LoggerFactory.getLogger(StatisticReportServiceImpl.class);

    @Autowired
    private ManuscriptStatisticService statisticService;

    @Autowired
    private ActivityIntervalService intervalService;

    @Autowired
    private ManuscriptSizeService sizeService;

    @Autowired
    @Lazy
    private ManuscriptService manuscriptService;

    @Autowired
    @Lazy
    private SettingService settingService;

    @Override
    public StatisticReport report(StatisticFilter requested) throws Exception {
        StatisticFilter filter = requested;
        List<DayStatistic> counted = statisticService.sumByDay(filter);
        SortedMap<LocalDate, ActivityTimes> timed = intervalService.timesByDay(filter);
        String book = filter.bookUuid();
        LocalDate dayBefore = filter.from().minusDays(1);
        List<ManuscriptSize> sizeRows = sizeService.findRange(book, filter.from(), filter.to());

        if (filter.from().equals(StatisticRange.BEGINNING)) {
            filter = narrow(filter, counted, timed, sizeRows);
            dayBefore = filter.from().minusDays(1);
        }
        LocalDate from = filter.from();
        List<ManuscriptSize> before = new ArrayList<>();
        if (book == null) {
            before.addAll(sizeService.sizesAt(dayBefore));
        } else {
            ManuscriptSize earlier = sizeService.sizeBefore(book, from);
            if (earlier != null) {
                before.add(earlier);
            }
        }

        Map<LocalDate, StatisticCounts> countsByDay = new HashMap<>();
        counted.forEach(day -> countsByDay.put(day.day(), day.counts()));
        TreeSet<LocalDate> dayList = new TreeSet<>(countsByDay.keySet());
        dayList.addAll(timed.keySet());
        sizeRows.forEach(row -> dayList.add(row.getDay()));
        Set<LocalDate> rebuilt = new HashSet<>();
        statisticService.findRange(filter).stream().filter(ManuscriptStatistic::isReconstructed).forEach(row -> rebuilt.add(row.getDay()));
        sizeRows.stream().filter(ManuscriptSize::isReconstructed).forEach(row -> rebuilt.add(row.getDay()));
        List<DayLine> days = new ArrayList<>();
        for (LocalDate day : dayList) {
            if (!day.isBefore(from) && !day.isAfter(filter.to())) {
                days.add(new DayLine(day, countsByDay.getOrDefault(day, StatisticCounts.ZERO),
                        timed.getOrDefault(day, ActivityTimes.ZERO), rebuilt.contains(day)));
            }
        }

        Map<String, SizeGrowth> growths = SizeSeries.perBook(before, sizeRows);
        List<BookLine> books = book == null ? booksOf(filter, growths, before, sizeRows) : List.of();
        return new StatisticReport(filter, statisticService.total(filter), intervalService.times(filter), days, books,
                SizeSeries.build(before, sizeRows, from, filter.to()), SizeSeries.sum(growths.values()), timeSince());
    }

    @Override
    public WritingActivity activity(String bookUuid) throws Exception {
        LocalDate today = statisticService.today();
        StatisticFilter filter = new StatisticFilter(StatisticRange.BEGINNING, today, StatisticSide.AUTHOR, bookUuid);
        Map<LocalDate, StatisticCounts> counts = new HashMap<>();
        statisticService.sumByDay(filter).forEach(day -> counts.put(day.day(), day.counts()));
        Map<LocalDate, Date> last = new HashMap<>();
        Set<LocalDate> rebuilt = new HashSet<>();
        for (ManuscriptStatistic row : statisticService.findRange(filter)) {
            if (row.getLastChange() != null) {
                last.merge(row.getDay(), row.getLastChange(), (a, b) -> a.after(b) ? a : b);
            }
            if (row.isReconstructed()) {
                rebuilt.add(row.getDay());
            }
        }
        List<ActivityRow> rows = intervalService.findRows(filter, List.of(ActivityKind.EDIT));
        Map<LocalDate, Long> edit = new HashMap<>();
        TimeUnion.byDay(rows).forEach((day, times) -> edit.put(day, times.edit()));
        for (ActivityRow row : rows) {
            last.merge(row.day(), row.endTime(), (a, b) -> a.after(b) ? a : b);
        }
        WritingGoal goal = WritingGoal.NONE;
        if (bookUuid == null) {
            UserSetting setting = settingService.getOrCreate(UserSetting.class);
            goal = new WritingGoal(orZero(setting.getDailyGoalWords()), orZero(setting.getDailyGoalMinutes()));
        }
        return WritingActivity.of(today, counts, edit, last, rebuilt, goal, timeSince());
    }

    private static int orZero(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * The day the time of the current user is recorded from, null when it is not known (a user that was made after the backfill).
     */
    private LocalDate timeSince() {
        try {
            String since = settingService.getOrCreate(UserSetting.class).getStatisticsSince();
            return since == null || since.isBlank() ? null : LocalDate.parse(since);
        } catch (Exception e) {
            log.debug("Cannot read the day the time is recorded from", e);
            return null;
        }
    }

    /**
     * The days from the first one that has anything.
     */
    private StatisticFilter narrow(StatisticFilter filter, List<DayStatistic> counted, SortedMap<LocalDate, ActivityTimes> timed,
                                   List<ManuscriptSize> sizeRows) {
        LocalDate first = filter.to();
        if (!counted.isEmpty()) {
            first = min(first, counted.getFirst().day());
        }
        if (!timed.isEmpty()) {
            first = min(first, timed.firstKey());
        }
        for (ManuscriptSize row : sizeRows) {
            first = min(first, row.getDay());
        }
        return new StatisticFilter(first, filter.to(), filter.side(), filter.bookUuid());
    }

    private static LocalDate min(LocalDate a, LocalDate b) {
        return b.isBefore(a) ? b : a;
    }

    private List<BookLine> booksOf(StatisticFilter filter, Map<String, SizeGrowth> growths, List<ManuscriptSize> before,
                                   List<ManuscriptSize> range) throws Exception {
        Map<String, BookStatistic> counted = new LinkedHashMap<>();
        for (BookStatistic book : statisticService.sumByBook(filter)) {
            counted.put(book.bookUuid(), book);
        }
        Map<String, ActivityTimes> timed = intervalService.timesByBook(filter);

        Set<String> uuids = new LinkedHashSet<>(counted.keySet());
        uuids.addAll(timed.keySet());
        growths.forEach((uuid, growth) -> {
            if (!growth.known() || growth.bookWords() != 0 || growth.bookTokens() != 0) {
                uuids.add(uuid);
            }
        });
        // the names of the books that only have time or a size
        Map<String, ManuscriptSize> named = new HashMap<>();
        before.forEach(row -> named.put(row.getBookUuid(), row));
        range.forEach(row -> named.put(row.getBookUuid(), row));

        List<BookLine> lines = new ArrayList<>();
        for (String uuid : uuids) {
            BookStatistic book = counted.get(uuid);
            if (book == null) {
                book = unnamed(uuid, named.get(uuid));
            }
            lines.add(new BookLine(book, timed.getOrDefault(uuid, ActivityTimes.ZERO), growths.getOrDefault(uuid, SizeGrowth.ZERO)));
        }
        return lines;
    }

    /**
     * A book that has no count in the range: the name its size rows keep, the state from the book itself.
     */
    private BookStatistic unnamed(String uuid, ManuscriptSize size) {
        BookState state = BookState.PURGED;
        String name = size == null ? "" : size.getBookName();
        String path = size == null ? "" : size.getFolderPath();
        try {
            Manuscript book = manuscriptService.findForUser(uuid);
            if (book != null) {
                state = book.isDeleted() ? BookState.TRASHED : BookState.PRESENT;
                if (name == null || name.isEmpty()) {
                    name = book.getName();
                }
            }
        } catch (Exception e) {
            log.debug("Cannot look up book {} for the statistics", uuid, e);
        }
        return new BookStatistic(uuid, name == null ? "" : name, path == null ? "" : path, state, StatisticCounts.ZERO, null);
    }

}
