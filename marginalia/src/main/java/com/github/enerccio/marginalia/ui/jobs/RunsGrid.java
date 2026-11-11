package com.github.enerccio.marginalia.ui.jobs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.RunsOnPage;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;

/**
 * The runs that work on the books now (the registry of runs, {@link Runs}): whose they are, which book, what they do and what they
 * hold, with <b>Stop</b> and, for a run whose work never reached its end (it holds its book until a restart), <b>End</b>, which gives
 * back what it holds without stopping anything. For administrators, on {@code /jobs}.
 */
@Configurable
public class RunsGrid extends VerticalLayout {

    @Autowired
    private Localization loc;

    private final Grid<Runs.Run> grid = new Grid<>();
    private final Span summary = new Span();
    private final List<Registration> registrations = new ArrayList<>();

    public RunsGrid() {
        setSizeFull();
        setPadding(false);
    }

    /**
     * Builds the components; call once after construction.
     */
    public RunsGrid create() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        grid.setSizeFull();
        grid.addColumn(run -> run.getOwnerLogin() == null ? "" : run.getOwnerLogin()).setHeader(loc.getValue(L.LABEL_USER))
                .setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(run -> run.getBookName() == null ? bookId(run) : run.getBookName()).setHeader(loc.getValue(L.LABEL_BOOK))
                .setFlexGrow(1);
        grid.addColumn(Runs.Run::getLabel).setHeader(loc.getValue(L.LABEL_VERSION_LABEL)).setFlexGrow(1);
        grid.addColumn(Runs.Run::getSource).setHeader(loc.getValue(L.LABEL_SOURCE)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(this::state).setHeader(loc.getValue(L.LABEL_STATE)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(run -> JobFormat.time(run.getStarted())).setHeader(loc.getValue(L.LABEL_JOB_STARTED)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(run -> JobFormat.duration(run.getStarted(), null)).setHeader(loc.getValue(L.LABEL_JOB_DURATION))
                .setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(run -> run.getHolds().size()).setHeader(loc.getValue(L.LABEL_RUN_HOLDS)).setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(this::actions).setAutoWidth(true).setFlexGrow(0);
        summary.addClassName(CssStyles.TEXT_SECONDARY);
        add(summary, grid);
        refresh();
        return this;
    }

    private static String bookId(Runs.Run run) {
        return run.getBookId() == null ? "" : "#" + run.getBookId();
    }

    private String state(Runs.Run run) {
        if (run.isStopping()) {
            return loc.getValue(L.MSG_RUN_STOPPING);
        }
        if (run.isWaitingForUser()) {
            return loc.getValue(L.MSG_RUN_WAITS_FOR_YOU);
        }
        if (run.getText().getWaiting() > 0) {
            return String.format(loc.getValue(L.MSG_WAITING_FOR_MODEL_AHEAD), run.getText().getWaiting());
        }
        return run.getStage() == null ? "" : run.getStage().name();
    }

    private Div actions(Runs.Run run) {
        Div buttons = new Div();
        buttons.addClassName(CssStyles.RUN_CORNER_BUTTONS);
        if (run.canStop()) {
            Button stop = new Button(loc.getValue(L.LABEL_STOP), _ -> {
                run.stop();
                refresh();
            });
            stop.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            stop.setEnabled(!run.isStopping());
            buttons.add(stop);
        }
        Button end = new Button(loc.getValue(L.LABEL_END_RUN), _ -> ConfirmDialog.show(
                String.format(loc.getValue(L.MSG_RUN_END_CONFIRM), run.getLabel()), () -> {
                    run.end();
                    refresh();
                }));
        end.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        buttons.add(end);
        return buttons;
    }

    public void refresh() {
        List<Runs.Run> runs = Runs.all().stream().filter(run -> !run.isEnded()).toList();
        grid.setItems(runs);
        summary.setText(runs.isEmpty() ? loc.getValue(L.MSG_RUNS_NONE) : "");
        summary.setVisible(runs.isEmpty());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        registrations.add(RunsOnPage.listen(attachEvent.getUI(), this::refresh));
        // the durations move on with the poll of the jobs above
        registrations.add(attachEvent.getUI().addPollListener(_ -> refresh()));
        refresh();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        registrations.forEach(Registration::remove);
        registrations.clear();
        super.onDetach(detachEvent);
    }

    // for the tests

    Grid<Runs.Run> gridForTest() {
        return grid;
    }
}
