package com.github.enerccio.marginalia.ui.dialogs.statistics;

import com.github.enerccio.marginalia.domain.components.ActivityTimes;
import com.github.enerccio.marginalia.domain.components.SizeSeries;
import com.github.enerccio.marginalia.domain.components.StatisticCounts;
import com.github.enerccio.marginalia.domain.components.StatisticReport;
import com.github.enerccio.marginalia.domain.components.StatisticReport.BookLine;
import com.github.enerccio.marginalia.domain.components.StatisticReport.DayLine;
import com.github.enerccio.marginalia.loc.L;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToLongFunction;

/**
 * Builds the Chart.js configurations of the statistics from a {@link StatisticReport}: the same ones for the window of a book, the
 * window of all the books and their phone twins, so they cannot drift. A configuration is a {@code Map} that the widget turns into
 * JSON; nothing in it is a function, so the values are already converted (hours, negative removed words). The days of a range that
 * have no row are filled in with zero so that the axis is honest, and a long range is grouped by week (over 90 days) or by month
 * (over a year).
 */
public final class StatisticCharts {

    public static final int GROUP_BY_WEEK_OVER_DAYS = 90;
    public static final int GROUP_BY_MONTH_OVER_DAYS = 366;
    /** How many books the chart of the books names before the rest is {@code Other}. */
    public static final int TOP_BOOKS = 10;

    static final String COLOR_WRITTEN = "#4e79a7";
    static final String COLOR_GENERATED = "#f28e2b";
    static final String COLOR_REMOVED = "#e15759";
    static final String COLOR_OPEN = "#9aa0a6";
    static final String COLOR_EDITING = "#4e79a7";
    static final String COLOR_GENERATING = "#f28e2b";
    static final String COLOR_AGENTS = "#59a14f";
    static final String COLOR_BRANCH = "#b07aa1";
    static final String COLOR_OTHER = "#9aa0a6";

    private static final long HOUR = 3_600_000L;

    private StatisticCharts() {
    }

    public enum Grain {
        DAY, WEEK, MONTH
    }

    /**
     * A bar of a chart: the days from-to (inside the range) under one label, the first of them.
     */
    public record Bucket(LocalDate from, LocalDate to, String label) {

        boolean contains(LocalDate day) {
            return !day.isBefore(from) && !day.isAfter(to);
        }
    }

    /**
     * The things a book can be measured by in the chart of the books: the values of the lines of the list.
     */
    public enum BookMetric {
        WORDS(L.LABEL_WORDS, line -> line.book().counts().wordsAdded()),
        TOKENS(L.LABEL_TOKENS, line -> line.book().counts().prefillTokens() + line.book().counts().generatedTokens()),
        GROWTH_WORDS(L.LABEL_GROWTH_WORDS, line -> line.growth().known() ? line.growth().bookWords() : 0),
        GROWTH_TOKENS(L.LABEL_GROWTH_TOKENS, line -> line.growth().known() ? line.growth().bookTokens() : 0),
        TIME_OPEN(L.LABEL_OPEN, line -> line.times().open()),
        TIME_EDITING(L.LABEL_TIME_EDITING, line -> line.times().edit()),
        TIME_GENERATING(L.LABEL_TIME_GENERATING, line -> line.times().generate()),
        TIME_AGENTS(L.LABEL_TIME_AGENTS_RUNNING, line -> line.times().run());

        private final L label;
        private final ToLongFunction<BookLine> value;

        BookMetric(L label, ToLongFunction<BookLine> value) {
            this.label = label;
            this.value = value;
        }

        public L getLabel() {
            return label;
        }

        public boolean isTime() {
            return ordinal() >= TIME_OPEN.ordinal();
        }

        public long value(BookLine line) {
            return value.applyAsLong(line);
        }
    }

    public static Grain grainOf(LocalDate from, LocalDate to) {
        long days = to.toEpochDay() - from.toEpochDay() + 1;
        if (days > GROUP_BY_MONTH_OVER_DAYS) {
            return Grain.MONTH;
        }
        return days > GROUP_BY_WEEK_OVER_DAYS ? Grain.WEEK : Grain.DAY;
    }

    /**
     * The bars of the range, the oldest first. A week starts on Monday and a month on the first, the first and the last bar are
     * cut by the range.
     */
    public static List<Bucket> buckets(LocalDate from, LocalDate to) {
        Grain grain = grainOf(from, to);
        List<Bucket> buckets = new ArrayList<>();
        LocalDate start = from;
        while (!start.isAfter(to)) {
            LocalDate end = switch (grain) {
                case DAY -> start;
                case WEEK -> start.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                case MONTH -> start.with(TemporalAdjusters.lastDayOfMonth());
            };
            if (end.isAfter(to)) {
                end = to;
            }
            buckets.add(new Bucket(start, end, start.toString()));
            start = end.plusDays(1);
        }
        return buckets;
    }

    private static List<String> labels(List<Bucket> buckets) {
        return buckets.stream().map(Bucket::label).toList();
    }

    /**
     * The counts of the days of each bar added up.
     */
    private static List<StatisticCounts> countsOf(StatisticReport report, List<Bucket> buckets) {
        return buckets.stream().map(bucket -> report.days().stream().filter(line -> bucket.contains(line.day()))
                .map(DayLine::counts).reduce(StatisticCounts.ZERO, StatisticCounts::plus)).toList();
    }

    /**
     * The time of the days of each bar added up: the days do not overlap (a stretch is cut at midnight), so the sum of the unions
     * of the days is the union of the bar.
     */
    private static List<ActivityTimes> timesOf(StatisticReport report, List<Bucket> buckets) {
        return buckets.stream().map(bucket -> {
            long open = 0, edit = 0, generate = 0, run = 0;
            for (DayLine line : report.days()) {
                if (bucket.contains(line.day())) {
                    open += line.times().open();
                    edit += line.times().edit();
                    generate += line.times().generate();
                    run += line.times().run();
                }
            }
            return new ActivityTimes(open, edit, generate, run);
        }).toList();
    }

    private static Map<String, Object> dataset(String label, String color, List<?> data) {
        Map<String, Object> dataset = new LinkedHashMap<>();
        dataset.put("label", label);
        dataset.put("data", data);
        dataset.put("backgroundColor", color);
        return dataset;
    }

    private static Map<String, Object> bars(List<String> labels, List<Map<String, Object>> datasets, boolean stacked,
                                            String title, String yTitle) {
        Map<String, Object> scales = new LinkedHashMap<>();
        Map<String, Object> y = new LinkedHashMap<>();
        y.put("stacked", stacked);
        y.put("beginAtZero", true);
        if (yTitle != null) {
            y.put("title", Map.of("display", true, "text", yTitle));
        }
        scales.put("x", Map.of("stacked", stacked));
        scales.put("y", y);
        return chart("bar", labels, datasets, scales, title);
    }

    private static Map<String, Object> chart(String type, List<String> labels, List<Map<String, Object>> datasets,
                                             Map<String, Object> scales, String title) {
        Map<String, Object> plugins = new LinkedHashMap<>();
        plugins.put("legend", Map.of("position", "bottom"));
        if (title != null) {
            plugins.put("title", Map.of("display", true, "text", title));
        }
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("scales", scales);
        options.put("plugins", plugins);
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("type", type);
        config.put("data", Map.of("labels", labels, "datasets", datasets));
        config.put("options", options);
        return config;
    }

    /**
     * What a chart says about the bars: the grouping of a long range, null for days.
     */
    private static String grouping(StatisticReport report, Function<L, String> text) {
        return switch (grainOf(report.filter().from(), report.filter().to())) {
            case DAY -> null;
            case WEEK -> text.apply(L.MSG_STATISTICS_GROUPED_WEEK);
            case MONTH -> text.apply(L.MSG_STATISTICS_GROUPED_MONTH);
        };
    }

    /**
     * Words per day: stacked bars of the written and the generated ones, the removed ones below the axis.
     */
    public static Map<String, Object> words(StatisticReport report, Function<L, String> text) {
        List<Bucket> buckets = buckets(report.filter().from(), report.filter().to());
        List<StatisticCounts> counts = countsOf(report, buckets);
        return bars(labels(buckets), List.of(
                dataset(text.apply(L.LABEL_WORDS_WRITTEN), COLOR_WRITTEN, counts.stream().map(StatisticCounts::wordsWritten).toList()),
                dataset(text.apply(L.LABEL_WORDS_GENERATED), COLOR_GENERATED, counts.stream().map(StatisticCounts::wordsGenerated).toList()),
                dataset(text.apply(L.LABEL_WORDS_REMOVED), COLOR_REMOVED, counts.stream().map(c -> -c.wordsRemoved()).toList())),
                true, grouping(report, text), null);
    }

    /**
     * Tokens per day: stacked bars of the sent and the generated ones.
     */
    public static Map<String, Object> tokens(StatisticReport report, Function<L, String> text) {
        List<Bucket> buckets = buckets(report.filter().from(), report.filter().to());
        List<StatisticCounts> counts = countsOf(report, buckets);
        return bars(labels(buckets), List.of(
                dataset(text.apply(L.LABEL_TOKENS_SENT), COLOR_WRITTEN, counts.stream().map(StatisticCounts::prefillTokens).toList()),
                dataset(text.apply(L.LABEL_TOKENS_GENERATED), COLOR_GENERATED, counts.stream().map(StatisticCounts::generatedTokens).toList())),
                true, grouping(report, text), null);
    }

    /**
     * Time per day in hours: grouped, not stacked, bars of the four kinds, which overlap in time and are not parts of a whole.
     */
    public static Map<String, Object> time(StatisticReport report, Function<L, String> text) {
        List<Bucket> buckets = buckets(report.filter().from(), report.filter().to());
        List<ActivityTimes> times = timesOf(report, buckets);
        // a bar of days on which no time was recorded is left out, not drawn as zero
        List<Boolean> known = buckets.stream().map(bucket -> report.timeKnown(bucket.to())).toList();
        return bars(labels(buckets), List.of(
                dataset(text.apply(L.LABEL_OPEN), COLOR_OPEN, hours(times, known, ActivityTimes::open)),
                dataset(text.apply(L.LABEL_TIME_EDITING), COLOR_EDITING, hours(times, known, ActivityTimes::edit)),
                dataset(text.apply(L.LABEL_TIME_GENERATING), COLOR_GENERATING, hours(times, known, ActivityTimes::generate)),
                dataset(text.apply(L.LABEL_TIME_AGENTS_RUNNING), COLOR_AGENTS, hours(times, known, ActivityTimes::run))),
                false, grouping(report, text), text.apply(L.LABEL_STATISTICS_HOURS));
    }

    private static List<Double> hours(List<ActivityTimes> times, List<Boolean> known, ToLongFunction<ActivityTimes> kind) {
        List<Double> hours = new ArrayList<>();
        for (int i = 0; i < times.size(); i++) {
            hours.add(known.get(i) ? hours(kind.applyAsLong(times.get(i))) : null);
        }
        return hours;
    }

    private static double hours(long millis) {
        return Math.round(millis * 100.0 / HOUR) / 100.0;
    }

    /**
     * The size of the book(s) at the end of each bar: a step line of the whole book and of the current branch, in words or tokens.
     * A bar on which the branch changed is marked on the branch line, a bar before the first known size has no point.
     */
    public static Map<String, Object> size(StatisticReport report, boolean tokens, Function<L, String> text) {
        List<Bucket> buckets = buckets(report.filter().from(), report.filter().to());
        List<Object> book = new ArrayList<>();
        List<Object> branch = new ArrayList<>();
        List<Integer> radius = new ArrayList<>();
        for (Bucket bucket : buckets) {
            List<SizeSeries.Point> points = report.sizes().stream().filter(point -> bucket.contains(point.day())).toList();
            SizeSeries.Point last = points.isEmpty() ? null : points.getLast();
            if (last == null || !last.present()) {
                book.add(null);
                branch.add(null);
                radius.add(0);
                continue;
            }
            book.add(tokens ? last.bookTokens() : last.bookWords());
            branch.add(tokens ? last.branchTokens() : last.branchWords());
            radius.add(points.stream().anyMatch(SizeSeries.Point::branchChanged) ? 5 : 0);
        }
        Map<String, Object> bookLine = line(text.apply(tokens ? L.LABEL_SIZE_BOOK_TOKENS : L.LABEL_SIZE_BOOK_WORDS), COLOR_WRITTEN, book);
        Map<String, Object> branchLine = line(text.apply(tokens ? L.LABEL_SIZE_BRANCH_TOKENS : L.LABEL_SIZE_BRANCH_WORDS), COLOR_BRANCH, branch);
        branchLine.put("pointRadius", radius);
        branchLine.put("pointStyle", "triangle");
        return chart("line", labels(buckets), List.of(bookLine, branchLine), Map.of("y", Map.of("beginAtZero", true)),
                grouping(report, text));
    }

    private static Map<String, Object> line(String label, String color, List<Object> data) {
        Map<String, Object> line = dataset(label, color, data);
        line.put("borderColor", color);
        line.put("stepped", true);
        line.put("pointRadius", 0);
        line.put("spanGaps", false);
        return line;
    }

    /**
     * The growth of the size in each bar (negative when the book shrank), of the whole book and of the current branch.
     */
    public static Map<String, Object> growth(StatisticReport report, boolean tokens, Function<L, String> text) {
        List<Bucket> buckets = buckets(report.filter().from(), report.filter().to());
        List<Long> book = new ArrayList<>();
        List<Long> branch = new ArrayList<>();
        for (Bucket bucket : buckets) {
            long bookGrowth = 0, branchGrowth = 0;
            for (SizeSeries.Point point : report.sizes()) {
                if (bucket.contains(point.day()) && point.growthKnown()) {
                    bookGrowth += tokens ? point.growthTokens() : point.growthWords();
                    branchGrowth += tokens ? point.branchGrowthTokens() : point.branchGrowthWords();
                }
            }
            book.add(bookGrowth);
            branch.add(branchGrowth);
        }
        return bars(labels(buckets), List.of(
                dataset(text.apply(tokens ? L.LABEL_SIZE_BOOK_TOKENS : L.LABEL_SIZE_BOOK_WORDS), COLOR_WRITTEN, book),
                dataset(text.apply(tokens ? L.LABEL_SIZE_BRANCH_TOKENS : L.LABEL_SIZE_BRANCH_WORDS), COLOR_BRANCH, branch)),
                false, text.apply(tokens ? L.LABEL_GROWTH_TOKENS : L.LABEL_GROWTH_WORDS), null);
    }

    /**
     * The first characters of the uuid of the books that look like another line of the list (same name, same folder): the tag that
     * tells them apart. A name that is unique in the list has none.
     */
    public static Map<String, String> duplicateTags(Collection<BookLine> lines) {
        Map<String, List<BookLine>> byName = new HashMap<>();
        for (BookLine line : lines) {
            byName.computeIfAbsent(line.book().bookName() + "\u0000" + line.book().folderPath(), _ -> new ArrayList<>()).add(line);
        }
        Map<String, String> tags = new HashMap<>();
        for (List<BookLine> same : byName.values()) {
            if (same.size() > 1) {
                same.forEach(line -> tags.put(line.book().bookUuid(), line.book().bookUuid().substring(0, Math.min(8, line.book().bookUuid().length()))));
            }
        }
        return tags;
    }

    /**
     * The name of a book as the list and the chart show it: its name, the tag of a book that has a twin and a mark of one that is gone.
     */
    public static String bookLabel(BookLine line, Map<String, String> tags, Function<L, String> text) {
        StringBuilder label = new StringBuilder(line.book().bookName().isEmpty() ? line.book().bookUuid() : line.book().bookName());
        String tag = tags.get(line.book().bookUuid());
        if (tag != null) {
            label.append(" #").append(tag);
        }
        if (line.book().deleted()) {
            label.append(" (").append(text.apply(L.LABEL_SOFT_DELETED)).append(")");
        }
        return label.toString();
    }

    /**
     * The books of the report by one measure: a horizontal bar each for the ones that come first, and the rest as {@code Other}
     * (not for a time, whose lines overlap and do not make a total).
     */
    public static Map<String, Object> byBook(StatisticReport report, BookMetric metric, Function<L, String> text) {
        List<BookLine> sorted = new ArrayList<>(report.books());
        sorted.removeIf(line -> metric.value(line) == 0);
        sorted.sort(Comparator.comparingLong((BookLine line) -> Math.abs(metric.value(line))).reversed());
        Map<String, String> tags = duplicateTags(report.books());

        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        long rest = 0;
        for (int i = 0; i < sorted.size(); i++) {
            BookLine line = sorted.get(i);
            if (i < TOP_BOOKS) {
                labels.add(bookLabel(line, tags, text));
                values.add(value(metric, metric.value(line)));
            } else {
                rest += metric.value(line);
            }
        }
        if (rest != 0 && !metric.isTime()) {
            labels.add(text.apply(L.LABEL_STATISTICS_OTHER));
            values.add(value(metric, rest));
        }
        String title = text.apply(metric.getLabel()) + (metric.isTime() ? " (" + text.apply(L.LABEL_STATISTICS_HOURS) + ") — "
                + text.apply(L.MSG_STATISTICS_TIMES_OVERLAP) : "");
        Map<String, Object> config = chart("bar", labels, List.of(dataset(text.apply(metric.getLabel()), COLOR_WRITTEN, values)),
                Map.of("x", Map.of("beginAtZero", true)), title);
        @SuppressWarnings("unchecked")
        Map<String, Object> options = (Map<String, Object>) config.get("options");
        options.put("indexAxis", "y");
        @SuppressWarnings("unchecked")
        Map<String, Object> plugins = (Map<String, Object>) options.get("plugins");
        plugins.put("legend", Map.of("display", false));
        return config;
    }

    private static double value(BookMetric metric, long raw) {
        return metric.isTime() ? hours(raw) : raw;
    }

}
