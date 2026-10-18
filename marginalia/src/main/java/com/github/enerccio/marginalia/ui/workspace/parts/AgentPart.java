package com.github.enerccio.marginalia.ui.workspace.parts;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.AgentAttentionService;
import com.github.enerccio.marginalia.domain.service.AgentAttentionService.Notice;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.service.AgentRunner;
import com.github.enerccio.marginalia.domain.service.AgentService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.LLMInteraction;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.agent.AgentCloneDialog;
import com.github.enerccio.marginalia.ui.dialogs.agent.AgentConsoleDialog;
import com.github.enerccio.marginalia.ui.dialogs.agent.AgentDialog;
import com.github.enerccio.marginalia.ui.dialogs.agent.AgentStopDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceLLMSupport;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.context.request.RequestAttributes;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * The tab with the agents of the user: a table of them with the state of their run and the buttons to start, pause or
 * stop the run, to edit, clone and delete the agent. The workspace shows it only to users who may use agents.
 */
@Configurable
public class AgentPart implements WorkspaceComponent {

    /**
     * The statuses of a run in which the user can ask for a pause: it works or waits for the user.
     */
    private static final Set<AgentRunStatus> PAUSABLE = Set.of(AgentRunStatus.RUNNING, AgentRunStatus.IDLE,
            AgentRunStatus.WAITING_APPROVAL);

    /**
     * The statuses of a run the user can end: not one that is stopping already, and not one that is over.
     */
    private static final Set<AgentRunStatus> STOPPABLE = Set.of(AgentRunStatus.RUNNING, AgentRunStatus.IDLE,
            AgentRunStatus.WAITING_APPROVAL, AgentRunStatus.PAUSING, AgentRunStatus.PAUSED, AgentRunStatus.STOPPED_KEPT,
            AgentRunStatus.DONE);

    @Autowired
    private Localization loc;

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentRunService agentRunService;

    @Autowired
    private AgentRunner agentRunner;

    @Autowired
    private AgentAttentionService attentionService;

    @Autowired
    private User currentUser;

    private final Workspace workspace;
    private Grid<Agent> grid;
    private Button addAgentButton;
    // the unfinished run of an agent by the agent's uuid
    private final Map<String, AgentRun> runs = new HashMap<>();
    private boolean canCreate;
    // the dialog the buttons opened last, what the tests press the buttons of
    private Dialog lastDialog;
    // the menu of the Start button built last, what the tests look at
    private MenuItem lastStartItem;
    // the console opened last
    private AgentConsoleDialog console;
    // how many agents wait to be looked at, shown on the tab even when another tab is open
    private final Span attentionBadge = new Span();
    private Runnable unsubscribe;

    public AgentPart(Workspace workspace) {
        this.workspace = workspace;
        attentionBadge.getElement().getThemeList().add("badge error pill small");
        attentionBadge.setVisible(false);
    }

    /**
     * The count of the agents that ask something, wait for a message, finished or failed, for the header of the tab:
     * hidden when none does. Updated when an agent changes, in every open UI of the user.
     */
    public Span getAttentionBadge() {
        return attentionBadge;
    }

    public String getName() {
        return loc.getValue(L.LABEL_AGENTS);
    }

    @Override
    public Component create() throws Exception {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);

        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        Span titleSpan = new Span(loc.getValue(L.LABEL_AGENTS));
        titleSpan.addClassName(CssStyles.SECTION_TITLE);

        addAgentButton = new Button(loc.getValue(L.LABEL_NEW_AGENT), event -> {
            AgentDialog dialog = new AgentDialog();
            dialog.setOnSave(this::refreshGrid);
            dialog.create();
            dialog.open();
            lastDialog = dialog;
        });
        addAgentButton.setThemeName("primary");

        headerLayout.add(titleSpan, addAgentButton);
        headerLayout.setFlexGrow(1, titleSpan);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        grid = new Grid<>(Agent.class, false);
        grid.setSizeFull();
        grid.addColumn(Agent::getUuid).setHeader(loc.getValue(L.LABEL_AGENT_ID)).setFlexGrow(2).setResizable(true);
        grid.addColumn(Agent::getName).setHeader(loc.getValue(L.LABEL_NAME)).setFlexGrow(2);
        grid.addColumn(this::statusText).setHeader(loc.getValue(L.LABEL_STATUS)).setFlexGrow(1);
        grid.addComponentColumn(this::createActions).setHeader("").setFlexGrow(0).setWidth("410px");

        mainLayout.add(headerLayout, grid);
        mainLayout.setFlexGrow(1, grid);

        refreshGrid();
        subscribeToAttention();
        return mainLayout;
    }

    /**
     * Hears of the agents that need the user from now on, in this UI. The runs work while the user is away; what
     * happened then is stored with the run and counted by {@link #refreshGrid}.
     */
    private void subscribeToAttention() {
        UI ui = UI.getCurrent();
        if (ui == null || unsubscribe != null) {
            return;
        }
        // the push comes from another thread, which needs the session of this UI to resolve the user
        RequestAttributes attributes = ThreadCopyRequestAttributes.create();
        unsubscribe = attentionService.subscribe(currentUser.getId(), notice -> {
            try {
                ui.access(() -> {
                    try (InRequestScope _ = new InRequestScope(attributes)) {
                        onAttention(notice);
                    }
                });
            } catch (UIDetachedException e) {
                // the screen is gone
            }
        });
        Runnable cancel = unsubscribe;
        ui.addDetachListener(event -> cancel.run());
    }

    /**
     * An agent of the user changed: the table and the count are read again, and when it needs the user, a notice
     * says so with a button that opens its console.
     */
    void onAttention(Notice notice) {
        refreshGrid();
        // the user is looking at that agent already, the console shows it
        if (!notice.attention() || (console != null && console.isOpened() && console.isFor(notice.agentUuid()))) {
            return;
        }
        com.vaadin.flow.component.notification.Notification toast = new com.vaadin.flow.component.notification.Notification();
        toast.setPosition(com.vaadin.flow.component.notification.Notification.Position.TOP_END);
        toast.setDuration(Notification.DURATION_LONGER);
        Button open = new Button(loc.getValue(L.LABEL_OPEN), event -> {
            toast.close();
            openConsole(notice.agentUuid());
        });
        open.setThemeName("primary small");
        Button close = new Button(Solid.CLOSE.create(), event -> toast.close());
        HorizontalLayout content = new HorizontalLayout(new Span(noticeText(notice)), open, close);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        toast.add(content);
        toast.open();
    }

    String noticeText(Notice notice) {
        String reason = notice.reason() == null ? "" : notice.reason();
        return switch (notice.status()) {
            case WAITING_APPROVAL -> String.format(loc.getValue(L.MSG_AGENT_NOTICE_APPROVAL), notice.agentName(), reason);
            case DONE -> String.format(loc.getValue(L.MSG_AGENT_NOTICE_DONE), notice.agentName());
            case FAILED -> String.format(loc.getValue(L.MSG_AGENT_NOTICE_FAILED), notice.agentName());
            case PAUSED -> String.format(loc.getValue(L.MSG_AGENT_NOTICE_PAUSED), notice.agentName(), reason);
            default -> String.format(loc.getValue(L.MSG_AGENT_NOTICE_IDLE), notice.agentName());
        };
    }

    private void openConsole(String agentUuid) {
        try {
            Agent agent = agentService.findForUser(agentUuid);
            if (agent != null) {
                openConsole(agent);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void openConsole(Agent agent) {
        AgentConsoleDialog dialog = new AgentConsoleDialog(agent);
        console = dialog;
        dialog.setOnChange(this::refreshGrid);
        dialog.create();
        dialog.open();
        lastDialog = dialog;
    }

    String statusText(Agent agent) {
        AgentRun run = runs.get(agent.getUuid());
        if (run != null) {
            return loc.getValue(loc.getAgentRunStatus(run.getStatus()));
        }
        return loc.getValue(agent.isEnabled() ? L.LABEL_AGENT_NO_RUN : L.LABEL_AGENT_DISABLED);
    }

    private Component createActions(Agent agent) {
        AgentRun run = runs.get(agent.getUuid());
        AgentRunStatus status = run == null ? null : run.getStatus();

        // an agent without a run starts from a menu (with the agent at work at once, or in the console), a paused or stopped
        // run resumes. A menu bar, not a context menu on a button: a context menu is attached next to its target and the
        // target of a row of the table has no parent yet when it is made, so it never opened
        Component startControl;
        if (status == null) {
            MenuBar startBar = new MenuBar();
            startBar.addThemeVariants(MenuBarVariant.LUMO_ICON);
            MenuItem startItem = startBar.addItem(Solid.PLAY.create());
            startBar.setTooltipText(startItem, loc.getValue(L.LABEL_START));
            startItem.getSubMenu().addItem(loc.getValue(L.LABEL_START_IMMEDIATELY), event -> startImmediately(agent));
            startItem.getSubMenu().addItem(loc.getValue(L.LABEL_START_IN_CONSOLE), event -> startInConsole(agent));
            lastStartItem = startItem;
            startControl = startBar;
        } else {
            Button resumeButton = button(Solid.PLAY, L.LABEL_RESUME, () -> {
                agentRunner.run(agentRunService.resume(run));
                return null;
            });
            resumeButton.setVisible(status.isResumable());
            startControl = resumeButton;
        }

        Button pauseButton = button(Solid.PAUSE, L.LABEL_PAUSE, () -> agentRunService.pause(run));
        pauseButton.setVisible(status != null && PAUSABLE.contains(status));

        Button stopButton = new Button(Solid.STOP.create(), event -> new StopAction(agent, run).run());
        stopButton.setTooltipText(loc.getValue(L.LABEL_STOP));
        stopButton.setVisible(status != null && STOPPABLE.contains(status));

        Button consoleButton = new Button(Solid.TERMINAL.create(), event -> openConsole(agent));
        consoleButton.setTooltipText(loc.getValue(L.LABEL_AGENT_CONSOLE));
        // the agent needs the user: the button stands out
        if (run != null && run.isAttention()) {
            consoleButton.setThemeName("primary");
        }

        Button editButton = new Button(Solid.PEN.create(), event -> {
            AgentDialog dialog = new AgentDialog(agent);
            dialog.setOnSave(this::refreshGrid);
            dialog.create();
            dialog.open();
            lastDialog = dialog;
        });
        editButton.setTooltipText(loc.getValue(L.LABEL_EDIT_AGENT));

        Button cloneButton = new Button(Solid.CLONE.create(), event -> {
            AgentCloneDialog dialog = new AgentCloneDialog(agent);
            dialog.setOnSave(this::refreshGrid);
            dialog.create();
            dialog.open();
            lastDialog = dialog;
        });
        cloneButton.setTooltipText(loc.getValue(canCreate ? L.LABEL_CLONE_AGENT : L.MSG_AGENT_LIMIT_REACHED));
        cloneButton.setEnabled(canCreate);

        Button deleteButton = new Button(Solid.TRASH.create(), event ->
                lastDialog = ConfirmDialog.open(loc.getValue(L.MSG_CONFIRM_DELETE), () -> {
                    try {
                        agentService.delete(agent, false);
                        refreshGrid();
                    } catch (Exception e) {
                        UIUtils.internalServerError(loc, e);
                    }
                }, () -> {}));
        deleteButton.setThemeName("error tertiary");
        deleteButton.setTooltipText(loc.getValue(L.LABEL_DELETE));

        HorizontalLayout actions = new HorizontalLayout(startControl, pauseButton, stopButton, consoleButton, editButton, cloneButton, deleteButton);
        actions.setSpacing(true);
        return actions;
    }

    /**
     * Starts a run and lets it work: it does its task and the author hears of it when it asks or finishes.
     */
    void startImmediately(Agent agent) {
        perform(() -> {
            agentRunner.run(agentRunService.start(agent));
            return null;
        });
    }

    /**
     * Starts a run, opens its console, and tells the agent to ask the author what they want before it does anything.
     */
    void startInConsole(Agent agent) {
        if (perform(() -> {
            agentRunner.run(agentRunService.start(agent, true), Defaults.AGENT_CONSOLE_START_MESSAGE);
            return null;
        })) {
            openConsole(agent);
        }
    }

    /**
     * A button that does something to a run; the answer of the service decides what the table shows next.
     */
    private Button button(Solid icon, L tooltip, Callable<?> action) {
        Button button = new Button(icon.create(), event -> perform(action));
        button.setTooltipText(loc.getValue(tooltip));
        return button;
    }

    /**
     * @return whether the action was done; the table shows what the run is like now either way
     */
    private boolean perform(Callable<?> action) {
        boolean done = false;
        try {
            action.call();
            done = true;
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            // the rules of the service (a disabled agent, no model, all the concurrent runs...), its text says what
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        refreshGrid();
        return done;
    }

    /**
     * The end of a run: asks whether the result is kept.
     */
    private class StopAction {
        private final Agent agent;
        private final AgentRun run;

        StopAction(Agent agent, AgentRun run) {
            this.agent = agent;
            this.run = run;
        }

        void run() {
            AgentStopDialog dialog = new AgentStopDialog(agent.getName(), run.getStatus() != AgentRunStatus.DONE,
                    keep -> perform(() -> agentRunService.stop(run, keep)));
            dialog.create();
            dialog.open();
            lastDialog = dialog;
        }
    }

    @Override
    public void refresh() throws Exception {
        refreshGrid();
    }

    private void refreshGrid() {
        if (grid == null) {
            return;
        }
        try {
            List<Agent> agents = agentService.findAllForUser();
            agents.sort(Comparator.comparing(Agent::getName, loc.createLocaleComparator()));
            runs.clear();
            for (Agent agent : agents) {
                AgentRun run = agentRunService.findUnfinished(agent);
                if (run != null) {
                    runs.put(agent.getUuid(), run);
                }
            }
            int waiting = agentRunService.findNeedingAttention().size();
            attentionBadge.setText(String.valueOf(waiting));
            attentionBadge.setVisible(waiting > 0);
            canCreate = agentService.canCreate();
            addAgentButton.setEnabled(canCreate);
            addAgentButton.setTooltipText(canCreate ? null : loc.getValue(L.MSG_AGENT_LIMIT_REACHED));
            grid.setItems(agents);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @Override
    public String getLLMState() {
        return WorkspaceLLMSupport.describeTable("agents", grid, addAgentButton);
    }

    @Override
    public Map<String, LLMInteraction> getInteraction() {
        return LLMInteract.dialogButtons(addAgentButton);
    }

    @Override
    public void onTabSwitched() throws Exception {
        refresh();
    }

    @Override
    public void onTabClosed() throws Exception {
    }

    // for the tests of the tab

    Grid<Agent> getGrid() {
        return grid;
    }

    MenuItem getLastStartItem() {
        return lastStartItem;
    }

    Dialog getLastDialog() {
        return lastDialog;
    }

    Button getAddAgentButton() {
        return addAgentButton;
    }
}
