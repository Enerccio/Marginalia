package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.service.agent.AgentTranscriptExport;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.InputStreamDownloadCallback;
import com.vaadin.flow.server.streams.InputStreamDownloadHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.Consumer;

/**
 * The runs of an agent, the latest first, to look at again, keep or throw away: <b>View</b> opens the run in the
 * console (its steps and its report), <b>Download</b> gives its transcript as a Markdown file, <b>Delete</b> moves it
 * to the trash. A run that is going on can't be deleted, it is stopped first.
 */
@Configurable
public class AgentRunHistory extends VerticalLayout implements LLMInteract {

    private static final int RESULT_LIMIT = 100;

    @Autowired
    private Localization loc;

    @Autowired
    private AgentRunService agentRunService;

    @Autowired
    private AgentMessageService agentMessageService;

    private final Agent agent;
    private final Consumer<AgentRun> onView;
    private final Consumer<AgentRun> onDeleted;

    private Grid<AgentRun> grid;
    private Span emptyLabel;
    // what the grid shows, so a poll that finds nothing new does not rebuild it
    private String signature = "";

    /**
     * @param agent     the agent whose runs these are
     * @param onView    called when the user wants to look at the run in the console
     * @param onDeleted called after a run was deleted, the console may be looking at it
     */
    public AgentRunHistory(Agent agent, Consumer<AgentRun> onView, Consumer<AgentRun> onDeleted) {
        this.agent = agent;
        this.onView = onView;
        this.onDeleted = onDeleted;
    }

    public void create() {
        setSizeFull();
        setPadding(false);

        grid = new Grid<>();
        grid.setSizeFull();
        grid.addColumn(run -> run.getStartedAt() == null ? ""
                        : DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(run.getStartedAt()))
                .setHeader(loc.getValue(L.LABEL_DATE)).setFlexGrow(2);
        grid.addColumn(run -> loc.getValue(loc.getAgentRunStatus(run.getStatus())))
                .setHeader(loc.getValue(L.LABEL_STATUS)).setFlexGrow(2);
        grid.addColumn(AgentRun::getRounds).setHeader(loc.getValue(L.LABEL_ROUNDS)).setFlexGrow(1);
        grid.addColumn(AgentRun::getTotalTokens).setHeader(loc.getValue(L.LABEL_TOKENS)).setFlexGrow(1);
        grid.addColumn(run -> StringUtils.abbreviate(StringUtils.defaultString(run.getStatusReason()).replaceAll("\\s+", " "), RESULT_LIMIT))
                .setHeader(loc.getValue(L.LABEL_RESULT)).setFlexGrow(4);
        grid.addComponentColumn(this::actions).setHeader("").setFlexGrow(0).setWidth("160px");

        emptyLabel = new Span(loc.getValue(L.MSG_AGENT_NO_RUN_YET));
        emptyLabel.addClassName(CssStyles.HINT_TEXT);
        add(grid, emptyLabel);
        setFlexGrow(1, grid);
    }

    /**
     * Shows these runs, the latest first.
     */
    public void setRuns(List<AgentRun> runs) {
        if (grid == null) {
            return;
        }
        StringBuilder now = new StringBuilder();
        for (AgentRun run : runs) {
            now.append(run.getUuid()).append(run.getStatus()).append(run.getRounds()).append(run.getTotalTokens())
                    .append(run.getStatusReason() == null ? 0 : run.getStatusReason().length()).append(';');
        }
        emptyLabel.setVisible(runs.isEmpty());
        if (!signature.contentEquals(now)) {
            signature = now.toString();
            grid.setItems(runs);
        }
    }

    // package visible: the cells of a grid are made only when it is on a page, the tests ask for them
    Component actions(AgentRun run) {
        Button viewButton = new Button(Solid.EYE.create(), event -> view(run));
        viewButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        viewButton.setTooltipText(loc.getValue(L.LABEL_OPEN));

        Button downloadButton = new Button(Solid.DOWNLOAD.create());
        downloadButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        downloadButton.setTooltipText(loc.getValue(L.LABEL_DOWNLOAD));

        boolean busy = run.getStatus().isActive();
        Button deleteButton = new Button(Solid.TRASH.create(), event ->
                ConfirmDialog.open(loc.getValue(L.MSG_AGENT_CONFIRM_DELETE_RUN), () -> delete(run), () -> {}));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteButton.setEnabled(!busy);
        deleteButton.setTooltipText(loc.getValue(busy ? L.MSG_AGENT_RUN_BUSY : L.LABEL_DELETE));

        HorizontalLayout actions = new HorizontalLayout(viewButton, downloadAnchor(run, downloadButton), deleteButton);
        actions.setSpacing(false);
        return actions;
    }

    private Anchor downloadAnchor(AgentRun run, Button button) {
        Anchor anchor = new Anchor(new InputStreamDownloadHandler((InputStreamDownloadCallback) downloadEvent -> {
            try {
                byte[] content = export(run).getBytes(StandardCharsets.UTF_8);
                return new DownloadResponse(new ByteArrayInputStream(content), fileName(run), "text/markdown", content.length);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }), "");
        anchor.getElement().setAttribute("download", true);
        anchor.add(button);
        return anchor;
    }

    void view(AgentRun run) {
        onView.accept(run);
    }

    String export(AgentRun run) throws Exception {
        return AgentTranscriptExport.export(agent.getName(), run, agentMessageService.messages(run), agentMessageService.threads(run));
    }

    static String fileName(AgentRun run) {
        return "agent-run-" + (run.getStartedAt() == null ? run.getUuid()
                : new SimpleDateFormat("yyyyMMdd-HHmm").format(run.getStartedAt())) + ".md";
    }

    void delete(AgentRun run) {
        try {
            agentRunService.delete(run, false);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        onDeleted.accept(run);
    }

    // for the tests of the history

    Grid<AgentRun> getGrid() {
        return grid;
    }
}
