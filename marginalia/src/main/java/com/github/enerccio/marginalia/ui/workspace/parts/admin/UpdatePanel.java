package com.github.enerccio.marginalia.ui.workspace.parts.admin;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.JobService;
import com.github.enerccio.marginalia.domain.service.UpdateService;
import com.github.enerccio.marginalia.domain.service.UpdateService.UpdateStatus;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Updating a Docker installation from the Admin page: what runs, the branch of the checkout on the server and its newest commit,
 * the button that asks the watcher on the server to pull, build and restart, and the output of the last update. Without the watcher
 * (the desktop application, a server where it was not set up) the tab only says so. See {@link UpdateService}.
 */
@Configurable
@Extendable
public class UpdatePanel implements LLMInteract {

    private static final int LOG_BYTES = 256 * 1024;

    @Autowired
    private Localization loc;

    @Autowired
    private UpdateService updateService;

    @Autowired
    private JobService jobService;

    private final Span version = new Span();
    private final Span branch = new Span();
    private final Span state = new Span();
    private final Span lastRun = new Span();
    private Button update;
    private Button showLog;

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "layout", type = VerticalLayout.class),
                    @APILocals(name = "description", type = Span.class),
                    @APILocals(name = "refresh", type = Button.class),
                    @APILocals(name = "buttons", type = FlexLayout.class)})
    public Component create() throws Exception {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        Span description = new Span(loc.getValue(L.HELP_UPDATE));
        description.addClassName(CssStyles.TEXT_SECONDARY);

        update = new Button(loc.getValue(L.LABEL_UPDATE_AND_RESTART), _ -> confirmUpdate());
        update.setThemeName("primary");
        Button refresh = new Button(loc.getValue(L.LABEL_REFRESH), _ -> refreshOrReport());
        showLog = new Button(loc.getValue(L.LABEL_LOG), _ -> showLog());
        FlexLayout buttons = new FlexLayout(update, refresh, showLog);
        buttons.addClassNames(CssStyles.FLEX_WRAP, CssStyles.GAP_S);

        layout.add(description, version, branch, state, lastRun, buttons);
        refresh();
        return layout;
    }

    /**
     * Reads the status again: the watcher writes it on the server, nothing tells the page.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "status", type = UpdateStatus.class),
                    @APILocals(name = "when", type = String.class)})
    public void refresh() throws Exception {
        if (update == null) {
            return;
        }
        UpdateStatus status = updateService.getStatus();
        version.setText(String.format(loc.getValue(L.MSG_UPDATE_RUNNING_VERSION), status.runningVersion()));

        branch.setVisible(status.watching());
        lastRun.setVisible(status.watching() && status.lastRun() != null);
        showLog.setVisible(status.watching());
        update.setEnabled(status.canRequest() && status.upstream() != null);
        state.removeClassNames(CssStyles.TEXT_SUCCESS, CssStyles.TEXT_ERROR);

        if (!status.watching()) {
            state.setText(loc.getValue(L.MSG_UPDATE_NOT_SET_UP));
            return;
        }
        if (status.upstream() == null) {
            branch.setText(String.format(loc.getValue(L.MSG_UPDATE_NO_UPSTREAM), status.branch()));
        } else {
            branch.setText(String.format(loc.getValue(L.MSG_UPDATE_BRANCH), status.branch(), status.upstream(),
                    shortCommit(status.remoteCommit()), loc.getDateHourFormat().format(status.checked())));
        }
        if (status.running()) {
            state.setText(loc.getValue(L.MSG_UPDATE_IN_PROGRESS));
        } else if (status.requested()) {
            state.setText(loc.getValue(L.MSG_UPDATE_REQUESTED));
        } else if (status.fetchFailed()) {
            state.setText(loc.getValue(L.MSG_UPDATE_FETCH_FAILED));
            state.addClassName(CssStyles.TEXT_ERROR);
        } else if (status.updateAvailable()) {
            state.setText(loc.getValue(L.MSG_UPDATE_AVAILABLE));
            state.addClassName(CssStyles.TEXT_SUCCESS);
        } else {
            state.setText(status.upstream() == null ? "" : loc.getValue(L.MSG_UPDATE_UP_TO_DATE));
        }
        if (status.lastRun() != null) {
            String when = loc.getDateHourFormat().format(status.lastRun());
            lastRun.setText(String.format(loc.getValue(status.lastRunOk() ? L.MSG_UPDATE_LAST_OK : L.MSG_UPDATE_LAST_FAILED), when));
            lastRun.setClassName(CssStyles.TEXT_ERROR, !status.lastRunOk());
        }
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void refreshOrReport() {
        try {
            refresh();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "message", type = String.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void confirmUpdate() {
        try {
            String message = loc.getValue(L.MSG_CONFIRM_UPDATE);
            int jobs = jobService.active().size();
            if (jobs > 0) {
                message = String.format(loc.getValue(L.MSG_CONFIRM_UPDATE_JOBS), jobs) + " " + message;
            }
            ConfirmDialog.show(message, this::requestUpdate);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void requestUpdate() {
        try {
            updateService.requestUpdate();
            Notification.success(loc.getValue(L.MSG_UPDATE_REQUESTED));
        } catch (IllegalStateException e) {
            // the watcher stopped, or another administrator asked a moment ago: the status below says which
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        refreshOrReport();
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "dialog", type = Dialog.class),
                    @APILocals(name = "text", type = TextArea.class),
                    @APILocals(name = "reload", type = Button.class),
                    @APILocals(name = "close", type = Button.class)})
    private void showLog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(loc.getValue(L.LABEL_LOG));
        dialog.setWidth("1000px");
        UIUtils.fitPhone(dialog, true);

        TextArea text = new TextArea();
        text.setReadOnly(true);
        text.setWidthFull();
        text.setHeight("500px");
        text.addClassName(CssStyles.TEXT_MONOSPACE);
        text.setPlaceholder(loc.getValue(L.MSG_UPDATE_LOG_EMPTY));
        dialog.add(text);

        Button reload = new Button(loc.getValue(L.LABEL_REFRESH), _ -> loadLog(text));
        Button close = new Button(loc.getValue(L.LABEL_CLOSE), _ -> dialog.close());
        dialog.getFooter().add(reload, close);
        loadLog(text);
        dialog.open();
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "text", type = TextArea.class)},
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void loadLog(TextArea text) {
        try {
            text.setValue(updateService.readLog(LOG_BYTES));
            // the newest lines are at the end
            JavascriptLibrary.scrollTextAreaToBottom(text.getElement());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private static String shortCommit(String commit) {
        return commit == null ? "?" : commit.length() > 7 ? commit.substring(0, 7) : commit;
    }
}
