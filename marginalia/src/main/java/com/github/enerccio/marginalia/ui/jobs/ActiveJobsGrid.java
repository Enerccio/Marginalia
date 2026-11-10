package com.github.enerccio.marginalia.ui.jobs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.JobService;
import com.github.enerccio.marginalia.domain.service.job.ActiveJob;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The jobs that run now, refreshed every few seconds while it is on the page: who started them, how long they run and what
 * they wait for, so an administrator sees at a glance whether the server is busy. For administrators only (the service checks).
 */
@Configurable
public class ActiveJobsGrid extends VerticalLayout {

    private static final int POLL_MILLIS = 2000;

    @Autowired
    private Localization loc;

    @Autowired
    private JobService jobService;

    private final Grid<ActiveJob> grid = new Grid<>();
    private final Span summary = new Span();
    private int previousPollInterval = -1;
    private Registration pollRegistration;

    public ActiveJobsGrid() {
        setSizeFull();
        setPadding(false);
    }

    /**
     * Builds the components; call once after construction.
     */
    public ActiveJobsGrid create() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        grid.setSizeFull();
        grid.addColumn(job -> job.login() == null ? "" : job.login()).setHeader(loc.getValue(L.LABEL_USER)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(ActiveJob::kind).setHeader(loc.getValue(L.LABEL_JOB_KIND)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(job -> job.label() == null ? "" : job.label()).setHeader(loc.getValue(L.LABEL_VERSION_LABEL)).setFlexGrow(1);
        grid.addColumn(job -> JobFormat.time(job.startedAt())).setHeader(loc.getValue(L.LABEL_JOB_STARTED)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(job -> JobFormat.duration(job.startedAt(), null)).setHeader(loc.getValue(L.LABEL_JOB_DURATION)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(job -> (job.callCount() - job.runningCalls()) + " / " + job.callCount()).setHeader(loc.getValue(L.LABEL_JOB_CALLS))
                .setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(this::inFlight).setHeader(loc.getValue(L.LABEL_JOB_IN_FLIGHT)).setFlexGrow(2);
        summary.addClassName(CssStyles.TEXT_SECONDARY);
        add(summary, grid);
        refresh();
        return this;
    }

    private String inFlight(ActiveJob job) {
        return job.calls().stream().filter(call -> call.endedAt() == null)
                .map(call -> JobFormat.type(loc, call.type()) + " " + (call.backend() == null ? "" : call.backend())
                        + (call.model() == null ? "" : " (" + call.model() + ")") + " " + JobFormat.duration(call.startedAt(), Instant.now()))
                .collect(Collectors.joining(", "));
    }

    public void refresh() {
        try {
            List<ActiveJob> jobs = jobService.active();
            grid.setItems(jobs);
            int calls = jobs.stream().mapToInt(ActiveJob::runningCalls).sum();
            summary.setText(jobs.isEmpty() ? loc.getValue(L.MSG_JOBS_NONE_ACTIVE)
                    : String.format(loc.getValue(L.MSG_JOBS_ACTIVE_SUMMARY), jobs.size(), calls));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();
        previousPollInterval = ui.getPollInterval();
        ui.setPollInterval(previousPollInterval > 0 ? Math.min(previousPollInterval, POLL_MILLIS) : POLL_MILLIS);
        pollRegistration = ui.addPollListener(_ -> refresh());
        refresh();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (pollRegistration != null) {
            pollRegistration.remove();
            pollRegistration = null;
            detachEvent.getUI().setPollInterval(previousPollInterval);
        }
        super.onDetach(detachEvent);
    }
}
