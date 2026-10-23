package com.github.enerccio.marginalia.ui.workspace.parts;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
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
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.LLMInteraction;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.agent.*;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceComponent;
import com.github.enerccio.marginalia.ui.workspace.WorkspaceLLMSupport;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.context.request.RequestAttributes;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.IntConsumer;

/**
 * The tab with the agents of the user: a table of them with the state of their run and the buttons to start, pause or
 * stop the run, to edit, clone and delete the agent. The workspace shows it only to users who may use agents.
 */
@Configurable
@Extendable
public class AgentPart implements WorkspaceComponent {

    /**
     * The statuses of a run in which the user can ask for a pause: it works or waits for the user.
     */
    private static final Set<AgentRunStatus> PAUSABLE = Set.of(AgentRunStatus.RUNNING, AgentRunStatus.IDLE,
            AgentRunStatus.WAITING_APPROVAL);

    /**
     * The statuses of a run in which the user can ask for a pause at once: the request to the model and the call that go on are
     * cut off. The others pause at once anyway.
     */
    private static final Set<AgentRunStatus> PAUSABLE_NOW = Set.of(AgentRunStatus.RUNNING, AgentRunStatus.PAUSING);

    /**
     * The statuses of a run the user can end: not one that is stopping already, and not one that is over.
     */
    private static final Set<AgentRunStatus> STOPPABLE = Set.of(AgentRunStatus.RUNNING, AgentRunStatus.IDLE,
            AgentRunStatus.WAITING_APPROVAL, AgentRunStatus.PAUSING, AgentRunStatus.PAUSED, AgentRunStatus.STOPPED_KEPT,
            AgentRunStatus.DONE);

    /**
     * The statuses of a run whose result the user can take over.
     */
    private static final Set<AgentRunStatus> REVIEWABLE = Set.of(AgentRunStatus.DONE, AgentRunStatus.STOPPED_KEPT);

    /**
     * What the agents are ordered by on the phone, where there are no column headers to press.
     */
    enum Sort {
        NAME(L.LABEL_NAME), STATUS(L.LABEL_STATUS), LAST_RUN(L.LABEL_LAST_RUN);

        private final L label;

        Sort(L label) {
            this.label = label;
        }
    }

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
    // the agents of the user, filtered by what the fields above the table say
    private final List<Agent> agents = new ArrayList<>();
    private final ListDataProvider<Agent> dataProvider = new ListDataProvider<>(agents);
    private TextField nameFilter;
    private TextField bookFilter;
    private ComboBox<Sort> sortBox;
    // when the newest run of an agent started, by the agent's uuid; an agent that never ran is not there
    private final Map<String, Date> lastRuns = new HashMap<>();
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
    // told the same count, for the cue in the top bar of the phone
    private final List<IntConsumer> attentionListeners = new ArrayList<>();
    private int waiting;
    private Runnable unsubscribe;

    // the phone shows each agent as a card (name, state, buttons) in a single column
    private final boolean phone;

    public AgentPart(Workspace workspace) {
        this(workspace, false);
    }

    /**
     * @param phone whether the tab is the one of the phone workspace: a card for each agent in place of a table
     */
    public AgentPart(Workspace workspace, boolean phone) {
        this.workspace = workspace;
        this.phone = phone;
        attentionBadge.getElement().getThemeList().add("badge error pill small");
        attentionBadge.setVisible(false);
    }

    /**
     * The count of the agents that ask something, wait for a message, finished or failed, for the header of the tab:
     * hidden when none does. Updated when an agent changes, in every open UI of the user.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Span.class))
    public Span getAttentionBadge() {
        return attentionBadge;
    }

    /**
     * Asks to be told the count of the agents that wait for the user, now and whenever it is read again.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "listener", type = IntConsumer.class)})
    public void addAttentionListener(IntConsumer listener) {
        attentionListeners.add(listener);
        listener.accept(waiting);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class))
    public String getName() {
        return loc.getValue(L.LABEL_AGENTS);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "mainLayout", type = VerticalLayout.class),
                    @APILocals(name = "headerLayout", type = HorizontalLayout.class),
                    @APILocals(name = "titleSpan", type = Span.class),
                    @APILocals(name = "filterLayout", type = HorizontalLayout.class),
                    @APILocals(name = "collator", type = Comparator.class)})
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

        nameFilter = filterField(L.LABEL_NAME);
        bookFilter = filterField(L.LABEL_BOOK);
        HorizontalLayout filterLayout = new HorizontalLayout(nameFilter, bookFilter);
        filterLayout.setWidthFull();
        filterLayout.setWrap(true);
        filterLayout.setFlexGrow(1, nameFilter, bookFilter);
        if (phone) {
            sortBox = new ComboBox<>(loc.getValue(L.LABEL_SORT_BY));
            sortBox.setItems(Sort.values());
            sortBox.setItemLabelGenerator(sort -> loc.getValue(sort.label));
            sortBox.setAllowCustomValue(false);
            sortBox.setValue(Sort.NAME);
            sortBox.addValueChangeListener(event -> sortBy(event.getValue()));
            filterLayout.add(sortBox);
            filterLayout.setFlexGrow(1, sortBox);
        }

        grid = new Grid<>(Agent.class, false);
        grid.setSizeFull();
        grid.setItems(dataProvider);
        if (phone) {
            grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
            grid.addComponentColumn(this::createCard).setHeader(loc.getValue(L.LABEL_AGENTS));
        } else {
            Comparator<String> collator = loc.createLocaleComparator();
            grid.addColumn(Agent::getUuid).setHeader(loc.getValue(L.LABEL_AGENT_ID)).setFlexGrow(2).setResizable(true);
            grid.addColumn(Agent::getName).setHeader(loc.getValue(L.LABEL_NAME)).setFlexGrow(2)
                    .setComparator(Comparator.comparing(Agent::getName, collator));
            grid.addColumn(this::bookName).setHeader(loc.getValue(L.LABEL_BOOK)).setFlexGrow(2)
                    .setComparator(Comparator.comparing(this::bookName, collator));
            grid.addColumn(this::statusText).setHeader(loc.getValue(L.LABEL_STATUS)).setFlexGrow(1)
                    .setComparator(Comparator.comparing(this::statusText, collator));
            grid.addColumn(this::lastRunText).setHeader(loc.getValue(L.LABEL_LAST_RUN)).setFlexGrow(1)
                    .setComparator(lastRunOrder());
            grid.addComponentColumn(this::createActions).setHeader("").setFlexGrow(0).setWidth("410px");
        }

        mainLayout.add(headerLayout, filterLayout, grid);
        mainLayout.setFlexGrow(1, grid);

        refreshGrid();
        subscribeToAttention();
        return mainLayout;
    }

    /**
     * Hears of the agents that need the user from now on, in this UI. The runs work while the user is away; what
     * happened then is stored with the run and counted by {@link #refreshGrid}.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "ui", type = UI.class),
                    @APILocals(name = "attributes", type = RequestAttributes.class),
                    @APILocals(name = "cancel", type = Runnable.class)})
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
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "notice", type = AgentAttentionService.Notice.class)},
            locals = {@APILocals(name = "toast", type = com.vaadin.flow.component.notification.Notification.class),
                    @APILocals(name = "open", type = Button.class),
                    @APILocals(name = "close", type = Button.class),
                    @APILocals(name = "content", type = HorizontalLayout.class)})
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

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "notice", type = AgentAttentionService.Notice.class)},
            locals = {@APILocals(name = "reason", type = String.class)})
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

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "agentUuid", type = String.class)},
            locals = {@APILocals(name = "agent", type = Agent.class),
                    @APILocals(name = "e", type = Exception.class)})
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

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "agent", type = Agent.class)},
            locals = {@APILocals(name = "dialog", type = AgentConsoleDialog.class)})
    private void openConsole(Agent agent) {
        AgentConsoleDialog dialog = new AgentConsoleDialog(agent);
        console = dialog;
        dialog.setOnChange(this::refreshGrid);
        dialog.create();
        dialog.open();
        lastDialog = dialog;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "agent", type = Agent.class)},
            locals = {@APILocals(name = "run", type = AgentRun.class)})
    String statusText(Agent agent) {
        AgentRun run = runs.get(agent.getUuid());
        if (run != null) {
            return loc.getValue(loc.getAgentRunStatus(run.getStatus()));
        }
        return loc.getValue(agent.isEnabled() ? L.LABEL_AGENT_NO_RUN : L.LABEL_AGENT_DISABLED);
    }

    /**
     * A field that narrows the table to the agents that contain all of the words typed into it.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = TextField.class),
            parameters = {@APIParameter(name = "label", type = L.class)},
            locals = {@APILocals(name = "field", type = TextField.class)})
    private TextField filterField(L label) {
        TextField field = new TextField(loc.getValue(label));
        field.setClearButtonVisible(true);
        field.setPrefixComponent(Solid.MAGNIFYING_GLASS.create());
        field.setValueChangeMode(ValueChangeMode.LAZY);
        field.addValueChangeListener(event -> applyFilter());
        return field;
    }

    /**
     * Shows the agents whose name and book have all the words of the two fields.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "name", type = String.class),
                    @APILocals(name = "book", type = String.class)})
    private void applyFilter() {
        String name = nameFilter.getValue();
        String book = bookFilter.getValue();
        dataProvider.setFilter(agent -> containsWords(agent.getName(), name) && containsWords(bookName(agent), book));
    }

    /**
     * Whether the text has every word of the filter, in any order, whatever the case; a blank filter has them all.
     */
    static boolean containsWords(String text, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        for (String word : filter.strip().toLowerCase(Locale.ROOT).split("\\s+")) {
            if (!lower.contains(word)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Orders the table of the phone; the table of the desktop is ordered by its headers.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "sort", type = Sort.class)},
            locals = {@APILocals(name = "collator", type = Comparator.class),
                    @APILocals(name = "byName", type = Comparator.class)})
    void sortBy(Sort sort) {
        Comparator<String> collator = loc.createLocaleComparator();
        Comparator<Agent> byName = Comparator.comparing(Agent::getName, collator);
        if (sort == Sort.STATUS) {
            dataProvider.setSortComparator(Comparator.comparing(this::statusText, collator).thenComparing(byName)::compare);
        } else if (sort == Sort.LAST_RUN) {
            // the newest first, the agents that never ran at the end
            dataProvider.setSortComparator(lastRunOrder().reversed().thenComparing(byName)::compare);
        } else {
            dataProvider.setSortComparator(byName::compare);
        }
    }

    /**
     * The older run first, an agent that never ran before all of them.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = Comparator.class))
    private Comparator<Agent> lastRunOrder() {
        return Comparator.comparing(agent -> lastRuns.get(agent.getUuid()), Comparator.nullsFirst(Comparator.<Date>naturalOrder()));
    }

    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "agent", type = Agent.class)})
    private String bookName(Agent agent) {
        return agent.getManuscript() == null || agent.getManuscript().getName() == null ? "" : agent.getManuscript().getName();
    }

    /**
     * When the newest run of the agent started, "never" for an agent without one.
     */
    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = String.class),
            parameters = {@APIParameter(name = "agent", type = Agent.class)},
            locals = {@APILocals(name = "started", type = Date.class)})
    String lastRunText(Agent agent) {
        Date started = lastRuns.get(agent.getUuid());
        return started == null ? loc.getValue(L.LABEL_NEVER)
                : DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(started);
    }

    /**
     * An agent on the phone: its name and state above, the buttons below them, wrapping onto more lines when they do not fit.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "agent", type = Agent.class)},
            locals = {@APILocals(name = "name", type = Span.class),
                    @APILocals(name = "state", type = Span.class),
                    @APILocals(name = "history", type = Span.class),
                    @APILocals(name = "title", type = HorizontalLayout.class),
                    @APILocals(name = "actions", type = Component.class),
                    @APILocals(name = "row", type = HorizontalLayout.class),
                    @APILocals(name = "card", type = VerticalLayout.class)})
    private Component createCard(Agent agent) {
        Span name = new Span(agent.getName());
        name.addClassNames(CssStyles.TEXT_SEMIBOLD, CssStyles.WRAP_ANYWHERE);
        Span state = new Span(statusText(agent));
        state.addClassName(CssStyles.HINT_TEXT);
        Span history = new Span(bookName(agent) + " - " + lastRunText(agent));
        history.addClassNames(CssStyles.HINT_TEXT, CssStyles.WRAP_ANYWHERE);
        HorizontalLayout title = new HorizontalLayout(name, state);
        title.setWidthFull();
        title.setWrap(true);
        title.setAlignItems(FlexComponent.Alignment.BASELINE);
        title.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        Component actions = createActions(agent);
        if (actions instanceof HorizontalLayout row) {
            row.setWrap(true);
            row.setWidthFull();
        }
        VerticalLayout card = new VerticalLayout(title, history, actions);
        card.setPadding(false);
        card.setSpacing(false);
        card.addClassName(CssStyles.GAP_S);
        return card;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Component.class),
            parameters = {@APIParameter(name = "agent", type = Agent.class)},
            locals = {@APILocals(name = "run", type = AgentRun.class),
                    @APILocals(name = "status", type = AgentRunStatus.class),
                    @APILocals(name = "startControl", type = Component.class),
                    @APILocals(name = "startBar", type = MenuBar.class),
                    @APILocals(name = "startItem", type = MenuItem.class),
                    @APILocals(name = "resumeButton", type = Button.class),
                    @APILocals(name = "pauseButton", type = Button.class),
                    @APILocals(name = "pauseNowButton", type = Button.class),
                    @APILocals(name = "stopButton", type = Button.class),
                    @APILocals(name = "reviewButton", type = Button.class),
                    @APILocals(name = "consoleButton", type = Button.class),
                    @APILocals(name = "editButton", type = Button.class),
                    @APILocals(name = "cloneButton", type = Button.class),
                    @APILocals(name = "deleteButton", type = Button.class),
                    @APILocals(name = "actions", type = HorizontalLayout.class)})
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
                AgentResumeDialog dialog = new AgentResumeDialog(note -> perform(() -> {
                    agentRunner.run(agentRunService.resume(run, note));
                    return null;
                }));
                dialog.create();
                dialog.open();
                lastDialog = dialog;
                return null;
            });
            resumeButton.setVisible(status.isResumable());
            startControl = resumeButton;
        }

        Button pauseButton = button(Solid.PAUSE, L.LABEL_PAUSE, () -> agentRunService.pause(run));
        pauseButton.setVisible(status != null && PAUSABLE.contains(status));

        Button pauseNowButton = button(Solid.BOLT, L.LABEL_PAUSE_NOW, () -> agentRunner.pauseNow(run));
        pauseNowButton.setVisible(status != null && PAUSABLE_NOW.contains(status));

        Button stopButton = new Button(Solid.STOP.create(), event -> new StopAction(agent, run).run());
        stopButton.setTooltipText(loc.getValue(L.LABEL_STOP));
        stopButton.setVisible(status != null && STOPPABLE.contains(status));

        // the result of a finished or kept run waits for the user: they take over what they want of it
        Button reviewButton = new Button(Solid.CODE_COMPARE.create(), event -> openReview(agent, run));
        reviewButton.setTooltipText(loc.getValue(L.LABEL_REVIEW_CHANGES));
        reviewButton.setVisible(status != null && REVIEWABLE.contains(status));
        reviewButton.setThemeName("primary");

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

        HorizontalLayout actions = new HorizontalLayout(startControl, pauseButton, pauseNowButton, stopButton, reviewButton, consoleButton, editButton, cloneButton, deleteButton);
        actions.setSpacing(true);
        return actions;
    }

    /**
     * Opens the review of the result of the run.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "agent", type = Agent.class),
                    @APIParameter(name = "run", type = AgentRun.class)},
            locals = {@APILocals(name = "dialog", type = AgentMergeDialog.class)})
    void openReview(Agent agent, AgentRun run) {
        AgentMergeDialog dialog = new AgentMergeDialog(agent.getName(), run, this::refreshGrid);
        dialog.create();
        dialog.open();
        lastDialog = dialog;
    }

    /**
     * Starts a run and lets it work: it does its task and the author hears of it when it asks or finishes.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "agent", type = Agent.class)})
    void startImmediately(Agent agent) {
        perform(() -> {
            agentRunner.run(agentRunService.start(agent));
            return null;
        });
    }

    /**
     * Starts a run, opens its console, and tells the agent to ask the author what they want before it does anything.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "agent", type = Agent.class)})
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
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Button.class),
            parameters = {@APIParameter(name = "icon", type = FontAwesome.Solid.class),
                    @APIParameter(name = "tooltip", type = L.class),
                    @APIParameter(name = "action", type = Callable.class)},
            locals = {@APILocals(name = "button", type = Button.class)})
    private Button button(Solid icon, L tooltip, Callable<?> action) {
        Button button = new Button(icon.create(), event -> perform(action));
        button.setTooltipText(loc.getValue(tooltip));
        return button;
    }

    /**
     * @return whether the action was done; the table shows what the run is like now either way
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = boolean.class),
            parameters = {@APIParameter(name = "action", type = Callable.class)},
            locals = {@APILocals(name = "e", type = RuntimeException.class),
                    @APILocals(name = "e", type = Exception.class)})
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

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void refresh() throws Exception {
        refreshGrid();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "found", type = List.class),
                    @APILocals(name = "agent", type = Agent.class),
                    @APILocals(name = "run", type = AgentRun.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void refreshGrid() {
        if (grid == null) {
            return;
        }
        try {
            List<Agent> found = agentService.findAllForUser();
            found.sort(Comparator.comparing(Agent::getName, loc.createLocaleComparator()));
            runs.clear();
            lastRuns.clear();
            for (Agent agent : found) {
                AgentRun run = agentRunService.findUnfinished(agent);
                if (run != null) {
                    runs.put(agent.getUuid(), run);
                }
                // the newest first
                agentRunService.findForAgent(agent).stream().map(AgentRun::getStartedAt).filter(Objects::nonNull).findFirst()
                        .ifPresent(started -> lastRuns.put(agent.getUuid(), started));
            }
            waiting = agentRunService.findNeedingAttention().size();
            attentionBadge.setText(String.valueOf(waiting));
            attentionBadge.setVisible(waiting > 0);
            attentionListeners.forEach(listener -> listener.accept(waiting));
            canCreate = agentService.canCreate();
            addAgentButton.setEnabled(canCreate);
            addAgentButton.setTooltipText(canCreate ? null : loc.getValue(L.MSG_AGENT_LIMIT_REACHED));
            agents.clear();
            agents.addAll(found);
            dataProvider.refreshAll();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = String.class))
    @Override
    public String getLLMState() {
        return WorkspaceLLMSupport.describeTable("agents", grid, addAgentButton);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Map.class))
    @Override
    public Map<String, LLMInteraction> getInteraction() {
        return LLMInteract.dialogButtons(addAgentButton);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void onTabSwitched() throws Exception {
        refresh();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void onTabClosed() throws Exception {
    }

    // for the tests of the tab

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Grid.class))
    Grid<Agent> getGrid() {
        return grid;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = MenuItem.class))
    MenuItem getLastStartItem() {
        return lastStartItem;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Dialog.class))
    Dialog getLastDialog() {
        return lastDialog;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Button.class))
    Button getAddAgentButton() {
        return addAgentButton;
    }

    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = TextField.class))
    TextField getNameFilter() {
        return nameFilter;
    }

    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = TextField.class))
    TextField getBookFilter() {
        return bookFilter;
    }

    @PublicAPI(from = "1.5.0",
            returns = @APIReturn(type = ComboBox.class))
    ComboBox<Sort> getSortBox() {
        return sortBox;
    }
}
