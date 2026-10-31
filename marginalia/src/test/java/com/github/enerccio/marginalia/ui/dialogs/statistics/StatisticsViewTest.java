package com.github.enerccio.marginalia.ui.dialogs.statistics;

import com.github.enerccio.marginalia.domain.collections.StatisticRange;
import com.github.enerccio.marginalia.domain.collections.StatisticSide;
import com.github.enerccio.marginalia.domain.components.StatisticCounts;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.ManuscriptSizeService;
import com.github.enerccio.marginalia.domain.service.ManuscriptStatisticService;
import com.github.enerccio.marginalia.domain.service.impl.ManuscriptSizeServiceImpl;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.GlobalStatisticsDialog;
import com.github.enerccio.marginalia.ui.dialogs.StatisticsDialog;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneGlobalStatisticsDialog;
import com.github.enerccio.marginalia.ui.dialogs.phone.PhoneStatisticsDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.AopTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The statistics windows: the book's and everybody's, desktop and phone. They are built from the report, the controls reload it.
 */
class StatisticsViewTest extends MarginaliaTestBase {

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ManuscriptStatisticService statisticService;

    @Autowired
    private ManuscriptSizeService sizeService;

    // UI.setCurrent keeps a weak reference only
    private UI ui;
    private Manuscript book;
    private Manuscript other;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        ((ManuscriptSizeServiceImpl) AopTestUtils.getUltimateTargetObject(sizeService)).setManual(true);
        book = manuscriptService.save(named("The Saga"));
        other = manuscriptService.save(named("The Other"));
        statisticService.count(book, StatisticCounts.wordsWritten(120).plus(StatisticCounts.partsCreated(3)));
        statisticService.count(book, true, StatisticCounts.wordsGenerated(500));
        statisticService.count(other, StatisticCounts.wordsWritten(30));
    }

    @AfterEach
    void tearDown() {
        ((ManuscriptSizeServiceImpl) AopTestUtils.getUltimateTargetObject(sizeService)).setManual(false);
        UI.setCurrent(null);
    }

    private Manuscript named(String name) {
        Manuscript manuscript = new Manuscript();
        manuscript.setName(name);
        return manuscript;
    }

    private static <T extends Component> List<T> find(Component root, Class<T> type) {
        List<T> found = new ArrayList<>();
        if (type.isInstance(root)) {
            found.add(type.cast(root));
        }
        root.getChildren().forEach(child -> found.addAll(find(child, type)));
        return found;
    }

    private static String textOf(Component root) {
        StringBuilder text = new StringBuilder(root.getElement().getText());
        root.getChildren().forEach(child -> text.append(' ').append(textOf(child)));
        return text.toString();
    }

    private static int rows(Grid<?> grid) {
        return grid.getListDataView().getItemCount();
    }

    private static StatisticsView view(Component dialog) {
        return find(dialog, StatisticsView.class).getFirst();
    }

    @Test
    void theWindowOfABookShowsItsTodayInTheGridAndTheTotals() {
        StatisticsDialog dialog = new StatisticsDialog(book);
        dialog.create();

        assertThat(view(dialog).booksGrid()).as("only the days").isNull();
        assertThat(rows(view(dialog).daysGrid())).isEqualTo(1);
        String totals = textOf(find(dialog, Div.class).stream().filter(d -> d.getClassNames().contains("statistic-totals")).findFirst().orElseThrow());
        assertThat(totals).contains("120").contains("500");
        assertThat(dialog.getHeaderTitle()).contains("The Saga");
        assertThat(find(dialog, StatisticsView.class)).hasSize(1);
    }

    @Test
    void theOtherBooksAreNotInTheWindowOfABook() {
        StatisticsDialog dialog = new StatisticsDialog(other);
        dialog.create();

        String totals = textOf(find(dialog, Div.class).stream().filter(d -> d.getClassNames().contains("statistic-totals")).findFirst().orElseThrow());
        assertThat(totals).contains("30").doesNotContain("500");
    }

    @Test
    void theSideSwitchReloadsTheNumbers() {
        StatisticsDialog dialog = new StatisticsDialog(book);
        dialog.create();
        @SuppressWarnings("unchecked")
        RadioButtonGroup<StatisticSide> side = find(dialog, RadioButtonGroup.class).stream()
                .filter(group -> group.getValue() == StatisticSide.ALL).findFirst().orElseThrow();

        side.setValue(StatisticSide.AGENTS);

        String totals = textOf(find(dialog, Div.class).stream().filter(d -> d.getClassNames().contains("statistic-totals")).findFirst().orElseThrow());
        assertThat(totals).contains("500").doesNotContain("120");
        side.setValue(StatisticSide.AUTHOR);
        totals = textOf(find(dialog, Div.class).stream().filter(d -> d.getClassNames().contains("statistic-totals")).findFirst().orElseThrow());
        assertThat(totals).contains("120").doesNotContain("500");
    }

    @Test
    void theRangeCanBeSetToTwoDatesAndAnotherPeriodHasNothing() {
        StatisticsDialog dialog = new StatisticsDialog(book);
        dialog.create();
        @SuppressWarnings("unchecked")
        ComboBox<StatisticRange> range = find(dialog, ComboBox.class).stream()
                .filter(box -> box.getValue() == StatisticRange.LAST_30_DAYS).findFirst().orElseThrow();
        List<DatePicker> pickers = find(dialog, DatePicker.class);
        assertThat(pickers).hasSize(2).allMatch(picker -> !picker.isVisible());

        range.setValue(StatisticRange.CUSTOM);

        assertThat(pickers).allMatch(DatePicker::isVisible);
        assertThat(pickers.get(0).getValue()).isNotNull();
        pickers.get(0).setValue(java.time.LocalDate.of(2020, 1, 1));
        pickers.get(1).setValue(java.time.LocalDate.of(2020, 1, 31));
        assertThat(rows(view(dialog).daysGrid())).isZero();
        range.setValue(StatisticRange.TODAY);
        assertThat(pickers).allMatch(picker -> !picker.isVisible());
        assertThat(rows(view(dialog).daysGrid())).isEqualTo(1);
    }

    @Test
    void theWindowOfAllTheBooksHasTheListOfTheBooks() {
        GlobalStatisticsDialog dialog = new GlobalStatisticsDialog(_ -> {
        });
        dialog.create();

        assertThat(rows(view(dialog).daysGrid())).isEqualTo(1);
        assertThat(rows(view(dialog).booksGrid())).isEqualTo(2);
        assertThat(textOf(find(dialog, Div.class).stream().filter(d -> d.getClassNames().contains("statistic-totals")).findFirst().orElseThrow()))
                .contains("150");
    }

    @Test
    void theDeletedBooksCanBeLeftOutOfTheList() throws Exception {
        manuscriptService.delete(manuscriptService.find(other.getId()), false);
        GlobalStatisticsDialog dialog = new GlobalStatisticsDialog(_ -> {
        });
        dialog.create();
        Grid<?> books = view(dialog).booksGrid();
        assertThat(rows(books)).as("the deleted one is listed by default").isEqualTo(2);

        view(dialog).showDeleted().setValue(false);

        assertThat(rows(books)).isEqualTo(1);
        assertThat(textOf(find(dialog, Div.class).stream().filter(d -> d.getClassNames().contains("statistic-totals")).findFirst().orElseThrow()))
                .as("the totals still are what was done").contains("150");
    }

    @Test
    void thePhoneWindowsHaveFewerColumns() {
        StatisticsDialog desktop = new StatisticsDialog(book);
        desktop.create();
        PhoneStatisticsDialog phone = new PhoneStatisticsDialog(book);
        phone.create();
        GlobalStatisticsDialog desktopAll = new GlobalStatisticsDialog(_ -> {
        });
        desktopAll.create();
        PhoneGlobalStatisticsDialog phoneAll = new PhoneGlobalStatisticsDialog(_ -> {
        });
        phoneAll.create();

        assertThat(view(phone).daysGrid().getColumns().size()).isLessThan(view(desktop).daysGrid().getColumns().size());
        assertThat(view(phoneAll).booksGrid().getColumns().size()).isLessThan(view(desktopAll).booksGrid().getColumns().size());
        assertThat(rows(view(phone).daysGrid())).isEqualTo(1);
        assertThat(rows(view(phoneAll).booksGrid())).isEqualTo(2);
    }

    @Test
    void everyTabDrawsItsChartForAnyRangeAndSide() {
        GlobalStatisticsDialog dialog = new GlobalStatisticsDialog(_ -> {
        });
        dialog.create();
        StatisticsView view = view(dialog);
        @SuppressWarnings("unchecked")
        ComboBox<StatisticRange> range = find(dialog, ComboBox.class).stream()
                .filter(box -> box.getValue() == StatisticRange.LAST_30_DAYS).findFirst().orElseThrow();
        @SuppressWarnings("unchecked")
        RadioButtonGroup<StatisticSide> side = find(dialog, RadioButtonGroup.class).stream()
                .filter(group -> group.getValue() == StatisticSide.ALL).findFirst().orElseThrow();

        for (StatisticRange next : List.of(StatisticRange.LAST_30_DAYS, StatisticRange.ALL_TIME, StatisticRange.THIS_MONTH)) {
            range.setValue(next);
            for (StatisticSide who : StatisticSide.values()) {
                side.setValue(who);
                for (int i = 0; i < 7; i++) {
                    view.tabs().setSelectedIndex(i);
                    assertThat(view.tabs().getSelectedIndex()).isEqualTo(i);
                }
            }
        }
    }

    @Test
    void timeIsShownAsHoursAndMinutesAndASpentTimeIsNeverZero() {
        assertThat(StatisticsView.formatTime(0)).isEqualTo("0:00");
        assertThat(StatisticsView.formatTime(30_000)).as("less than a minute is a minute").isEqualTo("0:01");
        assertThat(StatisticsView.formatTime(61 * 60_000L)).isEqualTo("1:01");
        assertThat(StatisticsView.formatTime(24 * 3_600_000L)).isEqualTo("24:00");
    }

}
