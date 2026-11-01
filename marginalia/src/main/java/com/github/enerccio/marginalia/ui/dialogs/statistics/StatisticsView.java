package com.github.enerccio.marginalia.ui.dialogs.statistics;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.StatisticRange;
import com.github.enerccio.marginalia.domain.collections.StatisticSide;
import com.github.enerccio.marginalia.domain.components.*;
import com.github.enerccio.marginalia.domain.components.StatisticReport.BookLine;
import com.github.enerccio.marginalia.domain.components.StatisticReport.DayLine;
import com.github.enerccio.marginalia.domain.service.ManuscriptStatisticService;
import com.github.enerccio.marginalia.domain.service.StatisticReportService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.ChartJs;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.function.ValueProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The statistics of one book, or of all the books, for a range of days: the totals, a grid by day, the charts and (for all the
 * books) the list of the books. The dialogs of the desktop and of the phone put it in a window; the phone has fewer columns and
 * smaller charts ({@code compact}). The charts are drawn when their tab is shown, a chart in a hidden tab has no size.
 */
@Configurable
public class StatisticsView extends VerticalLayout {

    private static final Gson gson = new Gson();
    private static final String CHART_HEIGHT = "340px";
    private static final String CHART_HEIGHT_COMPACT = "240px";
    private static final String GRID_HEIGHT = "420px";
    private static final String GRID_HEIGHT_COMPACT = "340px";

    @Autowired
    private Localization loc;

    @Autowired
    private StatisticReportService reportService;

    @Autowired
    private ManuscriptStatisticService statisticService;

    private final String bookUuid;
    private final boolean compact;
    private Consumer<String> onOpenBook;

    private StatisticReport report;
    private final List<Runnable> renderers = new ArrayList<>();
    private Map<String, String> bookTags = Map.of();

    private ComboBox<StatisticRange> range;
    private DatePicker from;
    private DatePicker to;
    private RadioButtonGroup<StatisticSide> side;
    private Div totals;
    private Div notes;
    private TabSheet tabs;
    private Grid<DayRow> daysGrid;
    private Grid<BookLine> booksGrid;
    private Checkbox showDeleted;
    private RadioButtonGroup<Boolean> sizeUnit;
    private ComboBox<StatisticCharts.BookMetric> metric;
    private ActivityCalendarView calendar;
    private ChartJs wordsChart, timeChart, tokensChart, sizeChart, growthChart, booksChart;

    /**
     * @param bookUuid the book, null for all the books of the user
     * @param compact  for a phone
     */
    public StatisticsView(String bookUuid, boolean compact) {
        this.bookUuid = bookUuid;
        this.compact = compact;
    }

    /**
     * A line of the grid by day: the counts and times, and the size of the book(s) at the end of the day.
     */
    record DayRow(DayLine line, SizeSeries.Point size) {
    }

    /**
     * What is done when a line of the list of books is clicked, with the uuid of the book; not for a book that is gone.
     */
    public void setOnOpenBook(Consumer<String> onOpenBook) {
        this.onOpenBook = onOpenBook;
    }

    public void create() {
        setPadding(false);
        setSpacing(true);
        setWidthFull();

        add(createControls());
        totals = new Div();
        totals.addClassName(CssStyles.STATISTIC_TOTALS);
        notes = new Div();
        notes.addClassName(CssStyles.TOKEN_USAGE_SUMMARY);
        tabs = new TabSheet();
        tabs.setWidthFull();
        addTab(loc.getValue(L.LABEL_STATISTICS_DAYS), createDays(), null);
        wordsChart = chart();
        addTab(loc.getValue(L.LABEL_WORDS), wordsChart, () -> wordsChart.setChartConfig(gson.toJson(StatisticCharts.words(report, loc::getValue))));
        timeChart = chart();
        addTab(loc.getValue(L.LABEL_STATISTICS_TIME), timeChart, () -> timeChart.setChartConfig(gson.toJson(StatisticCharts.time(report, loc::getValue))));
        tokensChart = chart();
        addTab(loc.getValue(L.LABEL_TOKENS), tokensChart, () -> tokensChart.setChartConfig(gson.toJson(StatisticCharts.tokens(report, loc::getValue))));
        addTab(loc.getValue(L.LABEL_SIZE), createSize(), this::renderSize);
        if (bookUuid == null) {
            addTab(loc.getValue(L.LABEL_BOOKS), createBooks(), null);
            addTab(loc.getValue(L.LABEL_STATISTICS_BY_BOOK), createByBook(), this::renderByBook);
        }
        calendar = new ActivityCalendarView(bookUuid, compact);
        calendar.create();
        addTab(loc.getValue(L.LABEL_STATISTICS_CALENDAR), calendar, calendar::load);
        tabs.addSelectedChangeListener(_ -> renderSelected());
        add(totals, notes, tabs);
        reload();
    }

    /**
     * The grid by day, for tests: the content of the tabs is not among the children of the tab sheet.
     */
    Grid<DayRow> daysGrid() {
        return daysGrid;
    }

    /**
     * The grid of the books (null for the view of one book) and the check box of the deleted ones, for tests.
     */
    Grid<BookLine> booksGrid() {
        return booksGrid;
    }

    Checkbox showDeleted() {
        return showDeleted;
    }

    ActivityCalendarView calendar() {
        return calendar;
    }

    TabSheet tabs() {
        return tabs;
    }

    private void addTab(String label, Component content, Runnable renderer) {
        tabs.add(label, content);
        renderers.add(renderer);
    }

    private ChartJs chart() {
        ChartJs chart = new ChartJs();
        chart.setHeight(compact ? CHART_HEIGHT_COMPACT : CHART_HEIGHT);
        return chart;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // controls

    private Component createControls() {
        range = new ComboBox<>(loc.getValue(L.LABEL_STATISTICS_RANGE));
        range.setItems(StatisticRange.values());
        range.setItemLabelGenerator(item -> loc.getValue(item.getLabel()));
        range.setAllowCustomValue(false);
        range.setValue(StatisticRange.LAST_30_DAYS);
        range.addValueChangeListener(event -> {
            boolean custom = event.getValue() == StatisticRange.CUSTOM;
            from.setVisible(custom);
            to.setVisible(custom);
            if (custom && from.isEmpty()) {
                StatisticRange.Span span = StatisticRange.LAST_30_DAYS.span(statisticService.today());
                from.setValue(span.from());
                to.setValue(span.to());
            }
            reload();
        });
        from = new DatePicker(loc.getValue(L.LABEL_STATISTICS_FROM));
        to = new DatePicker(loc.getValue(L.LABEL_STATISTICS_TO));
        from.setVisible(false);
        to.setVisible(false);
        from.addValueChangeListener(_ -> reload());
        to.addValueChangeListener(_ -> reload());

        side = new RadioButtonGroup<>(loc.getValue(L.LABEL_STATISTICS_SIDE));
        side.setItems(StatisticSide.ALL, StatisticSide.AUTHOR, StatisticSide.AGENTS);
        side.setItemLabelGenerator(item -> loc.getValue(switch (item) {
            case ALL -> L.LABEL_ALL;
            case AUTHOR -> L.ENUM_STATISTIC_SIDE_AUTHOR;
            case AGENTS -> L.LABEL_AGENTS;
        }));
        side.setValue(StatisticSide.ALL);
        side.addValueChangeListener(_ -> reload());

        FlexLayout controls = new FlexLayout(range, from, to, side);
        controls.addClassNames(CssStyles.FLEX_WRAP, CssStyles.GAP_S);
        return controls;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // the tabs

    private Component createDays() {
        daysGrid = new Grid<>(DayRow.class, false);
        daysGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);
        daysGrid.setWidthFull();
        daysGrid.setHeight(compact ? GRID_HEIGHT_COMPACT : GRID_HEIGHT);
        Grid.Column<DayRow> date = daysGrid.addComponentColumn(this::dayCell).setHeader(loc.getValue(L.LABEL_DATE))
                .setComparator(Comparator.comparing(row -> row.line().day())).setAutoWidth(true).setFlexGrow(0).setFrozen(true);
        if (!compact) {
            estimate(daysGrid, row -> row.line().counts().prefillTokens(), L.LABEL_TOKENS_SENT);
            estimate(daysGrid, row -> row.line().counts().generatedTokens(), L.LABEL_TOKENS_GENERATED);
            number(daysGrid, row -> row.line().counts().requests(), L.LABEL_REQUESTS);
        }
        number(daysGrid, row -> row.line().counts().wordsWritten(), L.LABEL_WORDS_WRITTEN);
        number(daysGrid, row -> row.line().counts().wordsGenerated(), L.LABEL_WORDS_GENERATED);
        number(daysGrid, row -> row.line().counts().wordsRemoved(), L.LABEL_WORDS_REMOVED);
        if (!compact) {
            number(daysGrid, row -> row.line().counts().partsCreated(), L.LABEL_PARTS_CREATED);
            number(daysGrid, row -> row.line().counts().edits(), L.LABEL_EDITS);
            number(daysGrid, row -> row.line().counts().partsDeleted(), L.LABEL_PARTS_DELETED);
            dayTime(row -> row.line().times().open(), L.LABEL_OPEN);
        }
        dayTime(row -> row.line().times().edit(), L.LABEL_TIME_EDITING);
        if (!compact) {
            dayTime(row -> row.line().times().generate(), L.LABEL_TIME_GENERATING);
            dayTime(row -> row.line().times().run(), L.LABEL_TIME_AGENTS_RUNNING);
            daysGrid.addColumn(row -> row.size() == null || !row.size().present() ? "–" : format(row.size().bookWords()))
                    .setHeader(loc.getValue(L.LABEL_SIZE_BOOK_WORDS)).setTextAlign(ColumnTextAlign.END).setAutoWidth(true).setFlexGrow(0)
                    .setComparator(Comparator.comparingLong(row -> row.size() == null ? 0 : row.size().bookWords()));
        }
        daysGrid.addColumn(row -> growth(row.size() == null || !row.size().growthKnown(), row.size() == null ? 0 : row.size().growthWords()))
                .setHeader(loc.getValue(L.LABEL_GROWTH_WORDS)).setTextAlign(ColumnTextAlign.END).setAutoWidth(true).setFlexGrow(0)
                .setPartNameGenerator(row -> signPart(row.size() == null || !row.size().growthKnown() ? 0 : row.size().growthWords()))
                .setComparator(Comparator.comparingLong(row -> row.size() == null ? 0 : row.size().growthWords()));
        daysGrid.sort(List.of(new GridSortOrder<>(date, SortDirection.DESCENDING)));
        return daysGrid;
    }

    private Component createSize() {
        sizeUnit = new RadioButtonGroup<>();
        sizeUnit.setItems(false, true);
        sizeUnit.setItemLabelGenerator(tokens -> loc.getValue(tokens ? L.LABEL_TOKENS : L.LABEL_WORDS));
        sizeUnit.setValue(false);
        sizeUnit.addValueChangeListener(_ -> renderSize());
        sizeChart = chart();
        growthChart = chart();
        VerticalLayout layout = new VerticalLayout(sizeUnit, sizeChart, growthChart);
        layout.setPadding(false);
        layout.setSpacing(false);
        return layout;
    }

    private Component createBooks() {
        showDeleted = new Checkbox(loc.getValue(L.LABEL_STATISTICS_SHOW_DELETED), true);
        showDeleted.addValueChangeListener(_ -> refreshBooks());
        booksGrid = new Grid<>(BookLine.class, false);
        booksGrid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);
        booksGrid.setWidthFull();
        booksGrid.setHeight(compact ? GRID_HEIGHT_COMPACT : GRID_HEIGHT);
        booksGrid.addComponentColumn(this::bookName).setHeader(loc.getValue(L.LABEL_NAME)).setAutoWidth(true).setFlexGrow(0).setFrozen(true)
                .setComparator(Comparator.comparing(line -> line.book().bookName(), String.CASE_INSENSITIVE_ORDER));
        if (!compact) {
            booksGrid.addColumn(line -> line.book().folderPath()).setHeader(loc.getValue(L.LABEL_FOLDER)).setAutoWidth(true).setFlexGrow(0)
                    .setComparator(Comparator.comparing(line -> line.book().folderPath(), String.CASE_INSENSITIVE_ORDER));
            number(booksGrid, line -> line.book().counts().prefillTokens(), L.LABEL_TOKENS_SENT);
            number(booksGrid, line -> line.book().counts().generatedTokens(), L.LABEL_TOKENS_GENERATED);
        }
        number(booksGrid, line -> line.book().counts().wordsWritten(), L.LABEL_WORDS_WRITTEN);
        number(booksGrid, line -> line.book().counts().wordsGenerated(), L.LABEL_WORDS_GENERATED);
        number(booksGrid, line -> line.book().counts().wordsRemoved(), L.LABEL_WORDS_REMOVED);
        if (!compact) {
            number(booksGrid, line -> line.book().counts().partsCreated(), L.LABEL_PARTS_CREATED);
            number(booksGrid, line -> line.book().counts().edits(), L.LABEL_EDITS);
            number(booksGrid, line -> line.book().counts().partsDeleted(), L.LABEL_PARTS_DELETED);
            time(booksGrid, line -> line.times().open(), L.LABEL_OPEN);
        }
        time(booksGrid, line -> line.times().edit(), L.LABEL_TIME_EDITING);
        if (!compact) {
            time(booksGrid, line -> line.times().generate(), L.LABEL_TIME_GENERATING);
            time(booksGrid, line -> line.times().run(), L.LABEL_TIME_AGENTS_RUNNING);
        }
        booksGrid.addColumn(line -> growth(!line.growth().known(), line.growth().bookWords()))
                .setHeader(loc.getValue(L.LABEL_GROWTH_WORDS)).setTextAlign(ColumnTextAlign.END).setAutoWidth(true).setFlexGrow(0)
                .setPartNameGenerator(line -> signPart(line.growth().known() ? line.growth().bookWords() : 0))
                .setComparator(Comparator.comparingLong(line -> line.growth().known() ? line.growth().bookWords() : 0));
        Grid.Column<BookLine> share = booksGrid.addColumn(this::share).setHeader(loc.getValue(L.LABEL_STATISTICS_SHARE))
                .setTextAlign(ColumnTextAlign.END).setAutoWidth(true).setFlexGrow(0)
                .setComparator(Comparator.comparingLong(line -> line.book().counts().wordsAdded()));
        booksGrid.sort(List.of(new GridSortOrder<>(share, SortDirection.DESCENDING)));
        booksGrid.addItemClickListener(event -> {
            BookLine line = event.getItem();
            if (onOpenBook != null && !line.book().deleted()) {
                onOpenBook.accept(line.book().bookUuid());
            }
        });
        VerticalLayout layout = new VerticalLayout(showDeleted, booksGrid);
        layout.setPadding(false);
        layout.setSpacing(false);
        return layout;
    }

    private Component createByBook() {
        metric = new ComboBox<>(loc.getValue(L.LABEL_STATISTICS_METRIC));
        metric.setItems(StatisticCharts.BookMetric.values());
        metric.setItemLabelGenerator(item -> loc.getValue(item.getLabel()));
        metric.setAllowCustomValue(false);
        metric.setValue(StatisticCharts.BookMetric.WORDS);
        metric.addValueChangeListener(_ -> renderByBook());
        booksChart = chart();
        VerticalLayout layout = new VerticalLayout(metric, booksChart);
        layout.setPadding(false);
        layout.setSpacing(false);
        return layout;
    }

    // ---------------------------------------------------------------------------------------------------------------
    // columns

    private <T> void number(Grid<T> grid, ValueProvider<T, Long> value, L header) {
        grid.addColumn(row -> format(value.apply(row))).setHeader(loc.getValue(header)).setTextAlign(ColumnTextAlign.END)
                .setAutoWidth(true).setFlexGrow(0).setComparator(Comparator.comparingLong(value::apply));
    }

    private <T> void time(Grid<T> grid, ValueProvider<T, Long> value, L header) {
        grid.addColumn(row -> formatTime(value.apply(row))).setHeader(loc.getValue(header)).setTextAlign(ColumnTextAlign.END)
                .setAutoWidth(true).setFlexGrow(0).setComparator(Comparator.comparingLong(value::apply));
    }

    /**
     * The date of the day, with a mark on a day that was rebuilt from the history of the parts.
     */
    private Component dayCell(DayRow row) {
        Div cell = new Div(new Span(row.line().day().toString()));
        if (row.line().reconstructed()) {
            Span mark = new Span(" ~");
            mark.getElement().setAttribute("title", loc.getValue(L.MSG_STATISTICS_RECONSTRUCTED_DAY));
            cell.add(mark);
        }
        return cell;
    }

    /**
     * A count that is an estimate on a rebuilt day (the tokens of the calls were not kept, what is shown is a floor).
     */
    private void estimate(Grid<DayRow> grid, ValueProvider<DayRow, Long> value, L header) {
        grid.addColumn(row -> (row.line().reconstructed() && value.apply(row) > 0 ? "~" : "") + format(value.apply(row)))
                .setHeader(loc.getValue(header)).setTextAlign(ColumnTextAlign.END).setAutoWidth(true).setFlexGrow(0)
                .setComparator(Comparator.comparingLong(value::apply));
    }

    /**
     * A time of a day; a day before the time was recorded has none, which is not a zero.
     */
    private void dayTime(ValueProvider<DayRow, Long> value, L header) {
        daysGrid.addColumn(row -> report != null && !report.timeKnown(row.line().day()) ? "–" : formatTime(value.apply(row)))
                .setHeader(loc.getValue(header)).setTextAlign(ColumnTextAlign.END).setAutoWidth(true).setFlexGrow(0)
                .setComparator(Comparator.comparingLong(value::apply));
    }

    private Component bookName(BookLine line) {
        Div cell = new Div();
        cell.add(new Span(line.book().bookName().isEmpty() ? line.book().bookUuid() : line.book().bookName()));
        String tag = bookTags.get(line.book().bookUuid());
        if (tag != null) {
            Span badge = new Span("#" + tag);
            badge.getElement().getThemeList().add("badge contrast");
            badge.getElement().setAttribute("title", line.book().bookUuid());
            cell.add(badge);
        }
        if (line.book().deleted()) {
            Span badge = new Span(loc.getValue(L.LABEL_SOFT_DELETED));
            badge.getElement().getThemeList().add("badge error");
            cell.add(badge);
        }
        return cell;
    }

    private String share(BookLine line) {
        long all = report == null ? 0 : report.total().wordsAdded();
        return all <= 0 ? "" : Math.round(100.0 * line.book().counts().wordsAdded() / all) + " %";
    }

    // ---------------------------------------------------------------------------------------------------------------
    // data

    private void reload() {
        StatisticRange.Span span;
        if (range.getValue() == StatisticRange.CUSTOM) {
            LocalDate first = from.getValue();
            LocalDate last = to.getValue();
            if (first == null || last == null || last.isBefore(first)) {
                return;
            }
            span = new StatisticRange.Span(first, last);
        } else {
            span = range.getValue().span(statisticService.today());
        }
        try {
            report = reportService.report(new StatisticFilter(span.from(), span.to(), side.getValue(), bookUuid));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        refresh();
    }

    private void refresh() {
        totals.removeAll();
        StatisticCounts counts = report.total();
        ActivityTimes times = report.times();
        addTotal(L.LABEL_WORDS_WRITTEN, format(counts.wordsWritten()));
        addTotal(L.LABEL_WORDS_GENERATED, format(counts.wordsGenerated()));
        addTotal(L.LABEL_WORDS_REMOVED, format(counts.wordsRemoved()));
        addTotal(L.LABEL_PARTS_CREATED, format(counts.partsCreated()));
        addTotal(L.LABEL_EDITS, format(counts.edits()));
        addTotal(L.LABEL_PARTS_DELETED, format(counts.partsDeleted()));
        addTotal(L.LABEL_TOKENS_SENT, format(counts.prefillTokens()));
        addTotal(L.LABEL_TOKENS_GENERATED, format(counts.generatedTokens()));
        addTotal(L.LABEL_REQUESTS, format(counts.requests()));
        addTotal(L.LABEL_OPEN, formatTime(times.open()));
        addTotal(L.LABEL_TIME_EDITING, formatTime(times.edit()));
        addTotal(L.LABEL_TIME_GENERATING, formatTime(times.generate()));
        addTotal(L.LABEL_TIME_AGENTS_RUNNING, formatTime(times.run()));
        addTotal(L.LABEL_GROWTH_WORDS, growth(!report.growth().known(), report.growth().bookWords()));

        notes.removeAll();
        List<String> lines = new ArrayList<>();
        if (report.isEmpty()) {
            lines.add(loc.getValue(L.MSG_STATISTICS_EMPTY));
        }
        if (report.filter().side() != StatisticSide.AUTHOR) {
            lines.add(loc.getValue(L.MSG_STATISTICS_AGENTS));
        }
        if (bookUuid == null) {
            lines.add(loc.getValue(L.MSG_STATISTICS_TIMES_OVERLAP));
        }
        if (report.timeSince() != null && report.filter().from().isBefore(report.timeSince())) {
            lines.add(String.format(loc.getValue(L.MSG_STATISTICS_TIME_SINCE), report.timeSince()));
        }
        if (report.reconstructedDays() > 0) {
            lines.add(String.format(loc.getValue(L.MSG_STATISTICS_RECONSTRUCTED), report.reconstructedDays()));
        }
        lines.forEach(line -> notes.add(new Div(line)));

        Map<LocalDate, SizeSeries.Point> sizes = new HashMap<>();
        report.sizes().forEach(point -> sizes.put(point.day(), point));
        List<DayRow> rows = new ArrayList<>();
        for (DayLine line : report.days()) {
            rows.add(new DayRow(line, sizes.get(line.day())));
        }
        daysGrid.setItems(rows);
        if (booksGrid != null) {
            refreshBooks();
        }
        renderSelected();
    }

    private List<BookLine> visibleBooks() {
        if (report == null) {
            return List.of();
        }
        return report.books().stream().filter(line -> showDeleted == null || showDeleted.getValue() || !line.book().deleted()).toList();
    }

    private void refreshBooks() {
        List<BookLine> books = visibleBooks();
        bookTags = StatisticCharts.duplicateTags(books);
        booksGrid.setItems(books);
    }

    private void addTotal(L label, String value) {
        Span item = new Span(loc.getValue(label) + ": " + value);
        item.addClassName(CssStyles.STATISTIC_TOTAL_ITEM);
        totals.add(item);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // charts

    private void renderSelected() {
        int index = tabs.getSelectedIndex();
        if (report != null && index >= 0 && index < renderers.size() && renderers.get(index) != null) {
            renderers.get(index).run();
        }
    }

    private void renderSize() {
        if (report == null) {
            return;
        }
        boolean tokens = sizeUnit.getValue();
        Function<L, String> text = loc::getValue;
        sizeChart.setChartConfig(gson.toJson(StatisticCharts.size(report, tokens, text)));
        growthChart.setChartConfig(gson.toJson(StatisticCharts.growth(report, tokens, text)));
    }

    private void renderByBook() {
        if (report != null) {
            booksChart.setChartConfig(gson.toJson(StatisticCharts.byBook(report, metric.getValue(), loc::getValue)));
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // texts

    private static String format(long number) {
        return NumberFormat.getIntegerInstance().format(number);
    }

    /**
     * The time as {@code h:mm}; a stretch that is shorter than a minute is one minute, so a time that was spent is never 0:00.
     */
    static String formatTime(long millis) {
        if (millis <= 0) {
            return "0:00";
        }
        long minutes = Math.max(1, Math.round(millis / 60_000.0));
        return String.format("%d:%02d", minutes / 60, minutes % 60);
    }

    private static String growth(boolean unknown, long words) {
        if (unknown) {
            return "–";
        }
        return words > 0 ? "+" + format(words) : format(words);
    }

    private static String signPart(long words) {
        if (words > 0) {
            return CssStyles.STATISTIC_GROWTH_UP;
        }
        return words < 0 ? CssStyles.STATISTIC_GROWTH_DOWN : null;
    }

}
