package com.github.enerccio.marginalia.ui.jobs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Job;
import com.github.enerccio.marginalia.domain.model.impl.JobCall;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.service.JobService;
import com.github.enerccio.marginalia.domain.service.job.JobFilter;
import com.github.enerccio.marginalia.domain.service.job.JobStatus;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * The log of the jobs that ended, for the administrator: a filtered, paged list of jobs that opens into the calls of each, and the
 * deletion of the jobs the filter matches (the log is never cleaned by itself).
 */
@Configurable
public class JobHistoryGrid extends VerticalLayout {

    @Autowired
    private Localization loc;

    @Autowired
    private JobService jobService;

    @Autowired
    private UserService userService;

    private final Grid<Job> grid = new Grid<>();
    private ComboBox<User> users;
    private ComboBox<String> kinds;
    private ComboBox<JobStatus> statuses;
    private IntegerField olderThanDays;

    public JobHistoryGrid() {
        setSizeFull();
        setPadding(false);
    }

    public JobHistoryGrid create() {
        Span description = new Span(loc.getValue(L.HELP_JOBS_ADMIN));
        description.addClassName(CssStyles.TEXT_SECONDARY);

        users = new ComboBox<>(loc.getValue(L.LABEL_USER));
        users.setItemLabelGenerator(User::getLogin);
        users.setClearButtonVisible(true);
        kinds = new ComboBox<>(loc.getValue(L.LABEL_JOB_KIND));
        kinds.setClearButtonVisible(true);
        statuses = new ComboBox<>(loc.getValue(L.LABEL_STATUS));
        statuses.setItems(JobStatus.values());
        statuses.setItemLabelGenerator(status -> JobFormat.status(loc, status));
        statuses.setClearButtonVisible(true);
        olderThanDays = new IntegerField(loc.getValue(L.LABEL_JOB_OLDER_THAN_DAYS));
        olderThanDays.setMin(0);
        olderThanDays.setStepButtonsVisible(true);
        olderThanDays.setClearButtonVisible(true);

        Button refresh = new Button(loc.getValue(L.LABEL_REFRESH), _ -> reload());
        Button delete = new Button(loc.getValue(L.LABEL_JOBS_DELETE_MATCHING), _ -> confirmDelete());
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Anchor live = new Anchor("jobs", loc.getValue(L.LABEL_JOBS_OPEN_LIVE));
        live.setTarget("_blank");

        FlexLayout filters = new FlexLayout(users, kinds, statuses, olderThanDays, refresh, delete, live);
        filters.addClassNames(CssStyles.FLEX_WRAP, CssStyles.GAP_S);
        filters.setAlignItems(FlexComponent.Alignment.END);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        grid.setSizeFull();
        grid.addColumn(job -> JobFormat.time(job.getStartedAt())).setHeader(loc.getValue(L.LABEL_JOB_STARTED)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(job -> JobFormat.time(job.getEndedAt())).setHeader(loc.getValue(L.LABEL_JOB_ENDED)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(job -> JobFormat.duration(job.getStartedAt(), job.getEndedAt())).setHeader(loc.getValue(L.LABEL_JOB_DURATION))
                .setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(job -> job.getOwnerLogin() == null ? "" : job.getOwnerLogin()).setHeader(loc.getValue(L.LABEL_USER)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Job::getKind).setHeader(loc.getValue(L.LABEL_JOB_KIND)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(job -> job.getLabel() == null ? "" : job.getLabel()).setHeader(loc.getValue(L.LABEL_VERSION_LABEL)).setFlexGrow(1);
        grid.addComponentColumn(job -> JobFormat.badge(loc, job.getStatus())).setHeader(loc.getValue(L.LABEL_STATUS)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(job -> job.getFailedCalls() == 0 ? String.valueOf(job.getCallCount()) : job.getCallCount() + " (" + job.getFailedCalls() + " !)")
                .setHeader(loc.getValue(L.LABEL_JOB_CALLS)).setAutoWidth(true).setFlexGrow(0);
        grid.setItemDetailsRenderer(new ComponentRenderer<>(this::calls));
        grid.setDetailsVisibleOnClick(true);

        grid.setItems(query -> {
            try {
                return jobService.search(filter(), query.getOffset(), query.getLimit()).stream();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }, query -> {
            try {
                return (int) Math.min(Integer.MAX_VALUE, jobService.count(filter()));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        add(description, filters, grid);
        loadChoices();
        return this;
    }

    private void loadChoices() {
        try {
            List<User> all = userService.findAll().stream().filter(user -> !user.isDeleted())
                    .sorted(Comparator.comparing(User::getLogin, String.CASE_INSENSITIVE_ORDER)).toList();
            users.setItems(all);
            kinds.setItems(jobService.kinds());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * The filter the fields show now.
     */
    JobFilter filter() {
        User user = users.getValue();
        Integer days = olderThanDays.getValue();
        Date before = days == null ? null : Date.from(Instant.now().minus(Duration.ofDays(days)));
        return new JobFilter(user == null ? null : user.getId(), kinds.getValue(), statuses.getValue(), null, before);
    }

    public void reload() {
        loadChoices();
        grid.getDataProvider().refreshAll();
    }

    private void confirmDelete() {
        try {
            JobFilter filter = filter();
            long count = jobService.count(filter);
            if (count == 0) {
                Notification.success(String.format(loc.getValue(L.MSG_JOBS_PURGED), 0));
                return;
            }
            ConfirmDialog.show(String.format(loc.getValue(L.MSG_CONFIRM_JOBS_PURGE), count), () -> {
                try {
                    int deleted = jobService.purge(filter);
                    Notification.success(String.format(loc.getValue(L.MSG_JOBS_PURGED), deleted));
                    reload();
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            });
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private Grid<JobCall> calls(Job job) {
        Grid<JobCall> calls = new Grid<>();
        calls.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);
        calls.addColumn(call -> JobFormat.type(loc, call.getCallType())).setHeader(loc.getValue(L.LABEL_TYPE)).setAutoWidth(true).setFlexGrow(0);
        calls.addColumn(call -> call.getBackend() == null ? "" : call.getBackend()).setHeader(loc.getValue(L.LABEL_JOB_BACKEND)).setAutoWidth(true).setFlexGrow(0);
        calls.addColumn(call -> call.getModel() == null ? "" : call.getModel()).setHeader(loc.getValue(L.LABEL_MODEL)).setAutoWidth(true).setFlexGrow(0);
        calls.addColumn(call -> JobFormat.time(call.getStartedAt())).setHeader(loc.getValue(L.LABEL_JOB_STARTED)).setAutoWidth(true).setFlexGrow(0);
        calls.addColumn(call -> JobFormat.time(call.getEndedAt())).setHeader(loc.getValue(L.LABEL_JOB_ENDED)).setAutoWidth(true).setFlexGrow(0);
        calls.addColumn(call -> JobFormat.duration(call.getStartedAt(), call.getEndedAt())).setHeader(loc.getValue(L.LABEL_JOB_DURATION))
                .setAutoWidth(true).setFlexGrow(0);
        calls.addComponentColumn(call -> JobFormat.badge(loc, call.getStatus())).setHeader(loc.getValue(L.LABEL_STATUS)).setAutoWidth(true).setFlexGrow(0);
        calls.addColumn(call -> call.getError() == null ? "" : call.getError()).setHeader(loc.getValue(L.LABEL_ERROR)).setFlexGrow(1);
        calls.setAllRowsVisible(true);
        try {
            calls.setItems(jobService.calls(job));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        return calls;
    }
}
