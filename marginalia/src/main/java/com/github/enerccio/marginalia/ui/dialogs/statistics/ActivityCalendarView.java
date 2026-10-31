package com.github.enerccio.marginalia.ui.dialogs.statistics;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.ActivityMetric;
import com.github.enerccio.marginalia.domain.components.WritingActivity;
import com.github.enerccio.marginalia.domain.components.WritingGoal;
import com.github.enerccio.marginalia.domain.service.StatisticReportService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.text.NumberFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * The calendar of the days the author worked on, like a contribution calendar: a square for every day of the last weeks, shaded by
 * the words written or the time spent editing, with the current and the longest streak of days in a row and (for all the books)
 * the progress towards the daily goal and the streak of the days that reached it. It is independent of the range of the
 * statistics, it always shows the latest weeks. Read from the same rows as the rest of the statistics ({@link WritingActivity}).
 */
@Configurable
public class ActivityCalendarView extends VerticalLayout {

    /** The weeks a calendar reaches back: a year, and fewer on a phone. */
    static final int WEEKS = 53;
    static final int WEEKS_COMPACT = 16;

    @Autowired
    private Localization loc;

    @Autowired
    private StatisticReportService reportService;

    private final String bookUuid;
    private final boolean compact;

    private WritingActivity activity;
    private RadioButtonGroup<ActivityMetric> metric;
    private Div summary;
    private Div goal;
    private Div calendar;
    private Div note;

    /**
     * @param bookUuid the book, null for all the books of the user
     * @param compact  for a phone
     */
    public ActivityCalendarView(String bookUuid, boolean compact) {
        this.bookUuid = bookUuid;
        this.compact = compact;
    }

    public void create() {
        setPadding(false);
        setSpacing(true);
        setWidthFull();

        metric = new RadioButtonGroup<>();
        metric.setItems(ActivityMetric.values());
        metric.setItemLabelGenerator(item -> loc.getValue(item.getLabel()));
        metric.setValue(ActivityMetric.WORDS);
        metric.addValueChangeListener(_ -> drawCalendar());

        summary = new Div();
        summary.addClassName(CssStyles.STATISTIC_TOTALS);
        goal = new Div();
        goal.addClassName(CssStyles.ACTIVITY_GOAL);
        calendar = new Div();
        calendar.addClassName(CssStyles.ACTIVITY_CALENDAR);
        note = new Div(loc.getValue(L.MSG_STATISTICS_CALENDAR_NOTE));
        note.addClassName(CssStyles.TOKEN_USAGE_SUMMARY);
        add(metric, summary, goal, calendar, legend(), note);
    }

    /**
     * Reads the days again and draws; the tab calls it when it is shown.
     */
    public void load() {
        try {
            activity = reportService.activity(bookUuid);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        drawSummary();
        drawGoal();
        drawCalendar();
    }

    /**
     * The squares of the calendar, for tests.
     */
    Div calendar() {
        return calendar;
    }

    Div summary() {
        return summary;
    }

    Div goal() {
        return goal;
    }

    RadioButtonGroup<ActivityMetric> metric() {
        return metric;
    }

    // ---------------------------------------------------------------------------------------------------------------

    private int weeks() {
        return compact ? WEEKS_COMPACT : WEEKS;
    }

    private void drawSummary() {
        summary.removeAll();
        LocalDate from = ActivityCalendarGrid.start(activity, weeks(), firstDayOfWeek());
        item(summary, L.LABEL_STREAK_CURRENT, activity.currentStreak());
        item(summary, L.LABEL_STREAK_LONGEST, activity.longestStreak());
        item(summary, L.LABEL_DAYS_WORKED, activity.daysWorked(from, activity.today()));
        if (activity.goal().isSet()) {
            item(summary, L.LABEL_STREAK_GOAL_CURRENT, activity.currentGoalStreak());
            item(summary, L.LABEL_STREAK_GOAL_LONGEST, activity.longestGoalStreak());
        }
    }

    private void item(Div into, L label, long value) {
        Span item = new Span(loc.getValue(label) + ": " + format(value));
        item.addClassName(CssStyles.STATISTIC_TOTAL_ITEM);
        into.add(item);
    }

    /**
     * Today against the goals that are set, a bar each.
     */
    private void drawGoal() {
        goal.removeAll();
        WritingGoal target = activity.goal();
        WritingActivity.Day today = activity.day(activity.today());
        if (target.words() > 0) {
            goal.add(bar(today.wordsWritten(), target.words(),
                    String.format(loc.getValue(L.MSG_GOAL_PROGRESS_WORDS), format(today.wordsWritten()), format(target.words()))));
        }
        if (target.minutes() > 0) {
            goal.add(bar(today.editMillis(), target.minutesInMillis(), String.format(loc.getValue(L.MSG_GOAL_PROGRESS_MINUTES),
                    format(Math.round(today.editMillis() / 60_000.0)), format(target.minutes()))));
        }
        goal.setVisible(target.isSet());
    }

    private Component bar(long value, long target, String text) {
        ProgressBar bar = new ProgressBar(0, 1, Math.min(1.0, (double) value / target));
        bar.addClassName(CssStyles.ACTIVITY_GOAL_BAR);
        if (value >= target) {
            bar.addThemeVariants(ProgressBarVariant.LUMO_SUCCESS);
        }
        Div line = new Div(new Span(text), bar);
        line.addClassName(CssStyles.ACTIVITY_GOAL_LINE);
        return line;
    }

    private void drawCalendar() {
        calendar.removeAll();
        if (activity == null) {
            return;
        }
        Locale locale = locale();
        List<ActivityCalendarGrid.Week> weeks = ActivityCalendarGrid.weeks(activity, metric.getValue(), weeks(), firstDayOfWeek());
        for (ActivityCalendarGrid.Week week : weeks) {
            Div column = new Div();
            column.addClassName(CssStyles.ACTIVITY_CALENDAR_WEEK);
            Span month = new Span(week.month() == null ? "" : week.month().getMonth().getDisplayName(TextStyle.SHORT, locale));
            month.addClassName(CssStyles.ACTIVITY_CALENDAR_MONTH);
            column.add(month);
            for (ActivityCalendarGrid.Cell cell : week.days()) {
                Div day = new Div();
                day.addClassNames(CssStyles.ACTIVITY_CALENDAR_DAY, CssStyles.ACTIVITY_CALENDAR_LEVEL + cell.level());
                if (cell.today()) {
                    day.addClassName(CssStyles.ACTIVITY_CALENDAR_TODAY);
                }
                day.getElement().setAttribute("title", dayText(cell.day()));
                column.add(day);
            }
            calendar.add(column);
        }
    }

    private Component legend() {
        FlexLayout legend = new FlexLayout();
        legend.addClassNames(CssStyles.ACTIVITY_CALENDAR_LEGEND, CssStyles.GAP_S);
        legend.add(new Span(loc.getValue(L.LABEL_LESS)));
        for (int level = 0; level < WritingActivity.LEVELS; level++) {
            Div square = new Div();
            square.addClassNames(CssStyles.ACTIVITY_CALENDAR_DAY, CssStyles.ACTIVITY_CALENDAR_LEVEL + level);
            legend.add(square);
        }
        legend.add(new Span(loc.getValue(L.LABEL_MORE)));
        return legend;
    }

    /**
     * What a day says when it is pointed at: the date, the words and the time, the last time something was done.
     */
    String dayText(LocalDate date) {
        WritingActivity.Day day = activity.day(date);
        if (!day.worked()) {
            return String.format(loc.getValue(L.MSG_STATISTICS_CALENDAR_NONE), date);
        }
        String time = activity.timeKnown(date) ? StatisticsView.formatTime(day.editMillis()) : "–";
        String text = String.format(loc.getValue(L.MSG_STATISTICS_CALENDAR_DAY), date, format(day.wordsWritten()), time);
        if (day.lastActivity() != null) {
            text += String.format(loc.getValue(L.MSG_STATISTICS_CALENDAR_LAST), clock(day.lastActivity()));
        }
        return day.reconstructed() ? text + " (~)" : text;
    }

    private static String clock(Date time) {
        return DateTimeFormatter.ofPattern("HH:mm").format(time.toInstant().atZone(ZoneId.systemDefault()));
    }

    private static Locale locale() {
        UI ui = UI.getCurrent();
        return ui == null || ui.getLocale() == null ? Locale.getDefault() : ui.getLocale();
    }

    private static DayOfWeek firstDayOfWeek() {
        return WeekFields.of(locale()).getFirstDayOfWeek();
    }

    private static String format(long number) {
        return NumberFormat.getIntegerInstance().format(number);
    }

}
