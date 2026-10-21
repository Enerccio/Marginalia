package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentMessage;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.AgentApprovalService.PendingApproval;
import com.github.enerccio.marginalia.domain.service.agent.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.components.CommandBar;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.shared.Registration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Talks to an agent and shows what it does: the transcript of its latest run (what it was told, what it said, the
 * commands it called with their results, folded), the state and the tokens of the run, the question it waits an answer
 * to or the report it ended with, a box to write to it, and its workspace (the second tab). The run is read again
 * every few seconds while the dialog is open, a round of the agent takes longer than that.
 * <p>
 * On a phone it is made for a thumb: it fills the screen, the tabs are icons (the title names the open one), the controls
 * of the run are icon buttons that show only while they can be used, refresh and close sit in the header, and the message box
 * is one line with the attach, command and send buttons around it (the command line opens from its button).
 */
@Configurable
public class AgentConsoleDialog extends Dialog implements LLMInteract {

    /**
     * How often the open console looks at the run.
     */
    static final int POLL_MILLIS = 2000;
    // the position of the checkpoints among the tabs, which are read again only while they are shown
    static final int CHECKPOINTS_TAB = 3;
    // the context is made when its tab is picked, not while the run works
    static final int CONTEXT_TAB = 4;
    static final int COPY_TAB = 5;
    // the conversations of the sub-agents are read again while their tab is shown, the run may be working
    static final int SUBAGENTS_TAB = 6;
    static final int DIAGNOSTICS_TAB = 7;
    // after a command: the answer of a run that waits comes in a moment, looked for faster for a while
    static final int COMMAND_POLL_MILLIS = 500;
    static final long COMMAND_POLL_FOR_MILLIS = 15_000;

    /**
     * Where the images attached to a message are kept in the workspace, for the agent to look at.
     */
    static final String UPLOAD_FOLDER = "uploads";

    /**
     * An image the author attached to the message that is not sent yet.
     */
    private record PendingImage(String name, byte[] content) {
    }

    private static final Set<AgentRunStatus> PAUSABLE = Set.of(AgentRunStatus.RUNNING, AgentRunStatus.IDLE,
            AgentRunStatus.WAITING_APPROVAL);

    // a pause at once cuts off the request and the call that go on; the others pause at once anyway
    private static final Set<AgentRunStatus> PAUSABLE_NOW = Set.of(AgentRunStatus.RUNNING, AgentRunStatus.PAUSING);

    private static final Set<AgentRunStatus> STOPPABLE = Set.of(AgentRunStatus.RUNNING, AgentRunStatus.IDLE,
            AgentRunStatus.WAITING_APPROVAL, AgentRunStatus.PAUSING, AgentRunStatus.PAUSED, AgentRunStatus.STOPPED_KEPT,
            AgentRunStatus.DONE);

    // the conversation goes back to before a message only while nothing works on the run: a run that waits for the author is
    // paused for it
    private static final Set<AgentRunStatus> ROLLBACK = Set.of(AgentRunStatus.IDLE, AgentRunStatus.PAUSED,
            AgentRunStatus.STOPPED_KEPT);

    // a conversation in these statuses takes a message that waits for the agent to be ready to read it
    private static final Set<AgentRunStatus> QUEUEING = Set.of(AgentRunStatus.RUNNING, AgentRunStatus.WAITING_APPROVAL,
            AgentRunStatus.PAUSING);

    @Autowired
    private Localization loc;

    @Autowired
    private AgentRunService agentRunService;

    @Autowired
    private AgentRunner agentRunner;

    @Autowired
    private CommandService commandService;

    @Autowired
    private AgentVirtualFileSystemService fileSystemService;

    @Autowired
    private AgentMessageService agentMessageService;

    @Autowired
    private AgentApprovalService agentApprovalService;

    @Autowired
    private CheckpointService checkpointService;

    private final Agent agent;
    // whether the console is the one of a phone: see the description of the class
    private final boolean phone;
    private Runnable onChange;
    // the names of the tabs, for the title on a phone where the tabs show icons only
    private final List<String> tabNames = new ArrayList<>();
    // the command line with its hint, hidden on a phone until its button opens it
    private VerticalLayout commandRow;
    // the buttons that are only on the phone console, or elsewhere there
    private Button commandButton;
    private Button refreshButton;
    private Button closeButton;

    // the latest run of the agent, null when it never ran
    private AgentRun run;
    // how much of the transcript of the run is shown
    private Long shownRunId;
    private int shown;
    // puts the messages into the transcript box
    private AgentTranscriptRenderer renderer;
    // the messages that wait for the agent: their bubbles are the last ones of the transcript, and the ids they show
    private final List<Div> queuedBubbles = new ArrayList<>();
    private List<String> shownQueued = List.of();
    // the buttons that take the conversation back to before a message, enabled while the run waits or is paused
    private final List<Button> rollbackButtons = new ArrayList<>();

    private Span statusLabel;
    private Span usageLabel;
    // the plan of the agent: a button in the header that opens it, shown while the agent has one
    private Button planButton;
    private Popover planPopover;
    private VerticalLayout planBox;
    // what the plan is made from now, to build it again only when it changed
    private String shownPlanKey = "";
    // what the plan was read for, it is read again when one of them changes (not at every look at the run)
    private String planReadFor = "";
    private VerticalLayout transcriptBox;
    private TextArea messageField;
    private Button sendButton;
    // the command line of the author for the agent
    private CommandBar commandBar;
    private Upload imageUpload;
    private HorizontalLayout pendingRow;
    private final List<PendingImage> pendingImages = new ArrayList<>();
    private Button pauseButton;
    private Button pauseNowButton;
    private Button resumeButton;
    private Button stopButton;
    private Button finishButton;
    private Button reviewButton;
    private AgentWorkspaceBrowser workspaceBrowser;
    private AgentCheckpointBrowser checkpointBrowser;
    private AgentRunHistory history;
    private AgentContextView contextView;
    private AgentDiagnosticsView diagnosticsView;
    private AgentCopyView copyView;
    private AgentSubAgentsView subAgentsView;
    private TabSheet tabs;
    // the run the user picked in the history, null for the latest one that was not thrown away
    private String selectedRunUuid;

    private Dialog lastDialog;
    // the popup that asks for the approval of a command, null when none is open
    private AgentApprovalDialog approvalDialog;
    private Registration pollRegistration;
    private int previousPollInterval = -1;
    private long fastPollUntil;

    public AgentConsoleDialog(Agent agent) {
        this(agent, UIUtils.isPhone());
    }

    /**
     * @param phone whether to make the console of a phone
     */
    public AgentConsoleDialog(Agent agent, boolean phone) {
        this.agent = agent;
        this.phone = phone;
    }

    /**
     * Called when the user changed the run from the console (paused, resumed, stopped it), so the table of agents
     * can show it.
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    public void create() {
        setHeaderTitle(phone ? agent.getName() : loc.getValue(L.LABEL_AGENT_CONSOLE) + " - " + agent.getName());
        setWidth("1100px");
        setMaxWidth("96%");
        setHeight("90%");
        setModality(ModalityMode.STRICT);
        setCloseOnOutsideClick(false);
        setCloseOnEsc(false);
        UIUtils.fitPhone(this, true);
        if (phone) {
            addClassName(CssStyles.AGENT_CONSOLE_PHONE);
        }

        statusLabel = new Span();
        statusLabel.addClassName(CssStyles.TEXT_SEMIBOLD);
        usageLabel = new Span();
        usageLabel.addClassNames(CssStyles.HINT_TEXT);
        planBox = new VerticalLayout();
        planBox.setPadding(false);
        planBox.setSpacing(false);
        planBox.addClassName(CssStyles.AGENT_PLAN);
        planButton = new Button(loc.getValue(L.LABEL_AGENT_PLAN), Solid.LIST_CHECK.create());
        planButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        planButton.setVisible(false);
        planPopover = new Popover(planBox);
        planPopover.setTarget(planButton);
        planPopover.setPosition(PopoverPosition.BOTTOM_START);
        planPopover.setOpenOnClick(true);
        planPopover.setAriaLabel(loc.getValue(L.LABEL_AGENT_PLAN));
        HorizontalLayout statusRow = new HorizontalLayout(statusLabel, planButton, usageLabel);
        statusRow.setWidthFull();
        statusRow.setPadding(false);
        statusRow.setWrap(true);
        statusRow.setAlignItems(FlexComponent.Alignment.CENTER);
        statusRow.setJustifyContentMode(phone ? FlexComponent.JustifyContentMode.START : FlexComponent.JustifyContentMode.BETWEEN);

        transcriptBox = new VerticalLayout();
        transcriptBox.setPadding(false);
        transcriptBox.setSpacing(false);
        transcriptBox.addClassNames(CssStyles.HELP_MESSAGES, CssStyles.FULL_WIDTH, CssStyles.MIN_HEIGHT_0);
        renderer = new AgentTranscriptRenderer(loc, transcriptBox);
        renderer.setOnSubAgent(this::openSubAgent);

        messageField = new TextArea();
        messageField.setPlaceholder(loc.getValue(L.ENUM_DOMAIN_MESSAGE));
        messageField.setAriaLabel(loc.getValue(L.ENUM_DOMAIN_MESSAGE));
        messageField.setWidthFull();
        messageField.setMinRows(phone ? 1 : 2);
        messageField.setMaxHeight("120px");
        sendButton = new Button(loc.getValue(L.LABEL_SEND), Solid.PAPER_PLANE.create(), event -> send());
        sendButton.setThemeName("primary");
        // Ctrl+Enter (Cmd+Enter on a Mac) sends from the box; the value goes to the server with every key, so it is there
        // when the shortcut is
        messageField.setValueChangeMode(ValueChangeMode.EAGER);
        sendButton.addClickShortcut(Key.ENTER, KeyModifier.CONTROL).listenOn(messageField);
        sendButton.addClickShortcut(Key.ENTER, KeyModifier.META).listenOn(messageField);
        sendButton.setTooltipText("Ctrl+Enter");
        if (phone) {
            iconOnly(sendButton, L.LABEL_SEND);
        }
        // pictures can go with a message when the model of the agent sees them
        Button attachButton = new Button(loc.getValue(L.LABEL_ATTACH_IMAGE), Solid.IMAGE.create());
        imageUpload = new Upload(new InMemoryUploadHandler((metadata, data) -> attachImage(metadata.fileName(), data)));
        imageUpload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        imageUpload.setMaxFileSize((int) Math.min(Integer.MAX_VALUE, imageLimit()));
        if (phone) {
            iconOnly(attachButton, L.LABEL_ATTACH_IMAGE);
        }
        imageUpload.setUploadButton(attachButton);
        imageUpload.setDropLabel(new Span());
        imageUpload.setVisible(seesImages());
        if (seesImages()) {
            JavascriptLibrary.forwardPastedImages(messageField.getElement(), imageUpload.getElement());
        }
        pendingRow = new HorizontalLayout();
        pendingRow.setWidthFull();
        pendingRow.setWrap(true);
        pendingRow.setAlignItems(FlexComponent.Alignment.CENTER);
        pendingRow.setVisible(false);
        // the command line of the application, offering the commands of the agent and running them on it
        commandBar = new CommandBar();
        commandBar.addClassName(CssStyles.FULL_WIDTH);
        commandBar.setCommands(() -> AgentConsoleCommands.of(commandService, agentRunner.consoleCommands(agent)));
        commandBar.setLineRunner(this::runCommand);
        Span commandHint = new Span(loc.getValue(L.HELP_AGENT_COMMAND));
        commandHint.addClassName(CssStyles.HINT_TEXT);
        commandRow = new VerticalLayout(commandBar, commandHint);
        commandRow.setPadding(false);
        commandRow.setSpacing(false);
        commandRow.setWidthFull();

        HorizontalLayout inputRow = new HorizontalLayout(messageField, imageUpload, sendButton);
        if (phone) {
            // the keyboard takes half of the screen: the command line is out of the way until its button opens it
            commandRow.setVisible(false);
            commandButton = new Button(Solid.TERMINAL.create(), event -> commandRow.setVisible(!commandRow.isVisible()));
            iconOnly(commandButton, L.LABEL_COMMAND);
            inputRow = new HorizontalLayout(commandButton, imageUpload, messageField, sendButton);
        }
        inputRow.setWidthFull();
        inputRow.setPadding(false);
        inputRow.setAlignItems(FlexComponent.Alignment.END);
        inputRow.expand(messageField);

        // the state and the box that answers stay where they are, the transcript between them scrolls; what the agent
        // waits for (its question, its report) stays in the transcript where it was asked, see specialCall
        VerticalLayout runTab = new VerticalLayout(statusRow, transcriptBox, commandRow, pendingRow, inputRow);
        runTab.setSizeFull();
        runTab.setPadding(false);
        runTab.setSpacing(false);
        runTab.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_HIDDEN, CssStyles.GAP_S);
        runTab.expand(transcriptBox);

        workspaceBrowser = new AgentWorkspaceBrowser(agent, this::isWorking);
        workspaceBrowser.setPhone(phone);
        workspaceBrowser.create();
        checkpointBrowser = new AgentCheckpointBrowser(() -> run, this::isWorking, this::refreshRun, this::rewound);
        checkpointBrowser.setPhone(phone);
        checkpointBrowser.create();

        history = new AgentRunHistory(agent, picked -> {
            selectedRunUuid = picked.getUuid();
            tabs.setSelectedIndex(0);
            refreshRun();
        }, deleted -> {
            if (deleted.getUuid().equals(selectedRunUuid)) {
                selectedRunUuid = null;
            }
            refreshRun();
            if (onChange != null) {
                onChange.run();
            }
        });
        history.setPhone(phone);
        history.create();

        tabs = new TabSheet();
        tabs.setSizeFull();
        // the workspace wants every pixel, the other tabs keep a small padding of their own
        tabs.addThemeVariants(TabSheetVariant.LUMO_NO_PADDING);
        runTab.addClassName(CssStyles.PADDING_COMPACT);
        history.addClassName(CssStyles.PADDING_COMPACT);
        addTab(L.LABEL_AGENT_RUN, Solid.COMMENT_ALT, runTab);
        addTab(L.LABEL_AGENT_RUNS, Solid.LIST_UL, history);
        addTab(L.LABEL_AGENT_WORKSPACE, Solid.FOLDER, workspaceBrowser);
        checkpointBrowser.addClassName(CssStyles.PADDING_COMPACT);
        addTab(L.LABEL_AGENT_CHECKPOINTS, Solid.FLOPPY_DISK, checkpointBrowser);
        contextView = new AgentContextView(() -> run);
        contextView.setPhone(phone);
        contextView.create();
        contextView.addClassName(CssStyles.PADDING_COMPACT);
        addTab(L.LABEL_AGENT_CONTEXT, Solid.CHART_BAR, contextView);
        copyView = new AgentCopyView(() -> run);
        copyView.create();
        copyView.addClassName(CssStyles.PADDING_COMPACT);
        addTab(L.LABEL_AGENT_BOOK_COPY, Solid.CLONE, copyView);
        subAgentsView = new AgentSubAgentsView(() -> run);
        subAgentsView.create();
        subAgentsView.addClassName(CssStyles.PADDING_COMPACT);
        addTab(L.LABEL_AGENT_SUBAGENTS, Solid.ROBOT, subAgentsView);
        diagnosticsView = new AgentDiagnosticsView(() -> run);
        diagnosticsView.create();
        diagnosticsView.addClassName(CssStyles.PADDING_COMPACT);
        addTab(L.LABEL_AGENT_DIAGNOSTICS, Solid.COGS, diagnosticsView);
        tabs.addSelectedChangeListener(event -> {
            if (phone && tabs.getSelectedIndex() >= 0 && tabs.getSelectedIndex() < tabNames.size()) {
                setHeaderTitle(agent.getName() + " - " + tabNames.get(tabs.getSelectedIndex()));
            }
            checkpointBrowser.refresh();
            if (tabs.getSelectedIndex() == COPY_TAB) {
                copyView.refresh();
            }
            if (tabs.getSelectedIndex() == CONTEXT_TAB) {
                contextView.refresh();
            }
            if (tabs.getSelectedIndex() == SUBAGENTS_TAB) {
                subAgentsView.refresh();
            }
            if (tabs.getSelectedIndex() == DIAGNOSTICS_TAB) {
                diagnosticsView.refresh();
            }
        });
        add(tabs);
        if (phone) {
            setHeaderTitle(agent.getName() + " - " + tabNames.getFirst());
        }

        pauseButton = new Button(loc.getValue(L.LABEL_PAUSE), Solid.PAUSE.create(),
                event -> perform(() -> agentRunService.pause(run)));
        pauseNowButton = new Button(loc.getValue(L.LABEL_PAUSE_NOW), Solid.BOLT.create(),
                event -> perform(() -> agentRunner.pauseNow(run)));
        pauseNowButton.setTooltipText(loc.getValue(L.HELP_AGENT_PAUSE_NOW));
        resumeButton = new Button(loc.getValue(L.LABEL_RESUME), Solid.PLAY.create(), event -> {
            AgentResumeDialog resumeDialog = new AgentResumeDialog(note -> perform(() -> {
                agentRunner.run(agentRunService.resume(run, note));
                return null;
            }));
            resumeDialog.create();
            resumeDialog.open();
            lastDialog = resumeDialog;
        });
        finishButton = new Button(loc.getValue(L.LABEL_FINISH), Solid.CHECK.create(),
                event -> perform(() -> agentRunService.finish(run)));
        reviewButton = new Button(loc.getValue(L.LABEL_REVIEW_CHANGES), Solid.CODE_COMPARE.create(), event -> {
            AgentMergeDialog dialog = new AgentMergeDialog(agent.getName(), run, () -> {
                refreshRun();
                if (onChange != null) {
                    onChange.run();
                }
            });
            dialog.create();
            dialog.open();
            lastDialog = dialog;
        });
        reviewButton.setThemeName("primary");
        stopButton = new Button(loc.getValue(L.LABEL_STOP), Solid.STOP.create(), event -> {
            AgentStopDialog dialog = new AgentStopDialog(agent.getName(), run.getStatus() != AgentRunStatus.DONE,
                    keep -> perform(() -> agentRunService.stop(run, keep)));
            dialog.create();
            dialog.open();
            lastDialog = dialog;
        });
        refreshButton = new Button(loc.getValue(L.LABEL_REFRESH), Solid.ARROWS_ROTATE.create(), event -> refreshRun());
        closeButton = new Button(loc.getValue(L.LABEL_CLOSE), event -> close());
        HorizontalLayout footer;
        if (phone) {
            // the controls of the run are the footer, as icons that show only while they can be used; the review keeps its
            // words, it is what a finished run waits for. Refresh and close are in the header, where a thumb finds them
            iconOnly(pauseButton, L.LABEL_PAUSE);
            iconOnly(pauseNowButton, L.LABEL_PAUSE_NOW);
            iconOnly(resumeButton, L.LABEL_RESUME);
            iconOnly(finishButton, L.LABEL_FINISH);
            iconOnly(stopButton, L.LABEL_STOP);
            iconOnly(refreshButton, L.LABEL_REFRESH);
            closeButton = new Button(Solid.XMARK.create(), event -> close());
            iconOnly(closeButton, L.LABEL_CLOSE);
            getHeader().add(refreshButton, closeButton);
            footer = new HorizontalLayout(pauseButton, pauseNowButton, resumeButton, finishButton, reviewButton, stopButton);
        } else {
            footer = new HorizontalLayout(pauseButton, pauseNowButton, resumeButton, finishButton, reviewButton, stopButton, refreshButton, closeButton);
        }
        footer.setWidthFull();
        footer.setWrap(true);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footer);

        refreshRun();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();
        previousPollInterval = ui.getPollInterval();
        ui.setPollInterval(POLL_MILLIS);
        pollRegistration = ui.addPollListener(event -> refreshRun());
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (approvalDialog != null) {
            approvalDialog.close();
            approvalDialog = null;
        }
        if (pollRegistration != null) {
            pollRegistration.remove();
            pollRegistration = null;
            detachEvent.getUI().setPollInterval(previousPollInterval);
        }
        super.onDetach(detachEvent);
    }

    /**
     * A tab of the console: its name, on a phone an icon with the name for the title and for a screen reader.
     */
    private void addTab(L label, Solid icon, Component content) {
        String name = loc.getValue(label);
        tabNames.add(name);
        if (!phone) {
            tabs.add(name, content);
            return;
        }
        Tab tab = new Tab(icon.create());
        tab.getElement().setAttribute("aria-label", name);
        tabs.add(tab, content);
    }

    /**
     * A button of the phone without its words: the icon, the words are its tooltip and its label for a screen reader.
     */
    private void iconOnly(Button button, L label) {
        button.setText("");
        button.addThemeVariants(ButtonVariant.LUMO_ICON);
        button.setAriaLabel(loc.getValue(label));
        button.setTooltipText(loc.getValue(label));
    }

    /**
     * A control of the run that can or can't be used now; a phone has no room for the ones that can't, they are hidden.
     */
    private void usable(Button button, boolean usable) {
        button.setEnabled(usable);
        if (phone) {
            button.setVisible(usable);
        }
    }

    /**
     * Whether this console is the console of the agent.
     */
    public boolean isFor(String agentUuid) {
        return agent.getUuid().equals(agentUuid);
    }

    /**
     * The run went back in its conversation: the rows that were shown are marked now, so the transcript is made again.
     */
    void rewound() {
        shownRunId = null;
        refreshRun();
    }

    /**
     * The button of a {@code spawn_agent} call in the transcript: the conversation of the sub-agent it started.
     */
    private void openSubAgent(String callId) {
        if (subAgentsView.showCall(callId)) {
            tabs.setSelectedIndex(SUBAGENTS_TAB);
        } else {
            Notification.warning(loc.getValue(L.MSG_AGENT_SUBAGENT_NOT_FOUND));
        }
    }

    private boolean isWorking() {
        return run != null && run.getStatus().isActive();
    }

    /**
     * Reads the latest run again: its state, and the part of the transcript that is new.
     */
    void refreshRun() {
        if (statusLabel == null) {
            return;
        }
        if (fastPollUntil != 0 && System.currentTimeMillis() > fastPollUntil) {
            fastPollUntil = 0;
            getUI().ifPresent(ui -> ui.setPollInterval(POLL_MILLIS));
        }
        try {
            List<AgentRun> runs = agentRunService.findForAgent(agent);
            // the run the user picked in the history, else the latest run that was not thrown away: a failed or finished
            // one is read here, a discarded one only when it is picked
            run = runs.stream().filter(candidate -> candidate.getUuid().equals(selectedRunUuid)).findFirst()
                    .orElseGet(() -> runs.stream().filter(candidate -> candidate.getStatus() != AgentRunStatus.STOPPED_DISCARDED)
                            .findFirst().orElse(null));
            history.setRuns(runs);
            showState();
            showApproval();
            showTranscript();
            workspaceBrowser.refresh();
            if (tabs.getSelectedIndex() == CHECKPOINTS_TAB) {
                checkpointBrowser.refresh();
            }
            if (tabs.getSelectedIndex() == SUBAGENTS_TAB) {
                subAgentsView.refresh();
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void showState() throws Exception {
        if (run != null && run.isAttention()) {
            // the user is looking at it now; the other screens of the user are told
            run = agentRunService.acknowledge(run);
            if (onChange != null) {
                onChange.run();
            }
        }
        if (run == null) {
            UIUtils.setTextIfChanged(statusLabel, loc.getValue(L.MSG_AGENT_NO_RUN_YET));
            UIUtils.setTextIfChanged(usageLabel, "");
        } else {
            AgentRunStatus status = run.getStatus();
            String reason = StringUtils.trimToEmpty(run.getStatusReason());
            // the question and the report are in the transcript, where the agent put them
            boolean inTranscript = status == AgentRunStatus.IDLE || status == AgentRunStatus.DONE;
            UIUtils.setTextIfChanged(statusLabel, loc.getValue(loc.getAgentRunStatus(status))
                    + (!reason.isEmpty() && !inTranscript ? ": " + reason : ""));
            long budget = agentRunService.getTokenBudget(run);
            long context = agentMessageService.getContextTokens(run);
            long contextCap = agentRunService.getContextCap(run);
            long replyCap = agentRunService.getResponseCap(run);
            // which run this is, there can be many in the history
            UIUtils.setTextIfChanged(usageLabel, (run.getStartedAt() == null ? "" : java.text.DateFormat.getDateTimeInstance(
                    java.text.DateFormat.SHORT, java.text.DateFormat.SHORT).format(run.getStartedAt()) + "   ")
                    + loc.getValue(L.LABEL_ROUNDS) + ": " + run.getRounds() + "   "
                    + loc.getValue(L.LABEL_TOKEN_CONTEXT) + ": " + context + (contextCap > 0 ? " / " + contextCap : "") + "   "
                    + (replyCap > 0 ? loc.getValue(L.LABEL_AGENT_REPLY_LIMIT) + ": " + replyCap + "   " : "")
                    + loc.getValue(L.LABEL_TOKENS_TOTAL) + ": " + run.getTotalTokens()
                    + (budget > 0 ? " / " + budget : ""));
        }
        showPlan();
        AgentRunStatus status = run == null ? null : run.getStatus();
        boolean queueing = run != null && run.isInteractive() && QUEUEING.contains(status);
        boolean writable = status == AgentRunStatus.IDLE || queueing;
        messageField.setEnabled(writable);
        messageField.setHelperText(queueing ? loc.getValue(L.HELP_AGENT_MESSAGE_QUEUED) : null);
        sendButton.setEnabled(writable);
        // a command is run for a run that waits or is paused as well; where it works it is queued like a message
        commandBar.setEnabled(writable || status == AgentRunStatus.PAUSED);
        imageUpload.setEnabled(writable && pendingImages.size() < AgentRunner.MAX_ATTACHED_IMAGES);
        usable(pauseButton, status != null && PAUSABLE.contains(status));
        usable(pauseNowButton, status != null && PAUSABLE_NOW.contains(status));
        usable(resumeButton, status != null && status.isResumable());
        usable(finishButton, status == AgentRunStatus.IDLE);
        reviewButton.setVisible(status != null && (status == AgentRunStatus.DONE || status == AgentRunStatus.STOPPED_KEPT) && run.getWorkingCopy() != null);
        usable(stopButton, status != null && STOPPABLE.contains(status));
        rollbackButtons.forEach(button -> showRollbackButton(button, status));
    }

    /**
     * The plan of the agent in the popover of the header. It is read again when the run, its round or its status changes (the agent
     * writes it between rounds), and the popover is built again only when the plan itself changed.
     */
    private void showPlan() throws Exception {
        String readFor = run == null ? "" : run.getUuid() + ":" + run.getRounds() + ":" + run.getStatus();
        if (readFor.equals(planReadFor)) {
            return;
        }
        planReadFor = readFor;
        List<RunTools.PlanItem> plan = run == null ? List.of() : agentRunService.getPlan(run);
        planButton.setVisible(!plan.isEmpty());
        String key = RunToolsImpl.render(plan);
        if (key.equals(shownPlanKey)) {
            return;
        }
        shownPlanKey = key;
        planBox.removeAll();
        if (plan.isEmpty()) {
            planPopover.close();
            planBox.add(new Span(loc.getValue(L.MSG_AGENT_NO_PLAN)));
            return;
        }
        long done = plan.stream().filter(item -> "done".equals(item.status())).count();
        planButton.setText(loc.getValue(L.LABEL_AGENT_PLAN) + " " + done + "/" + plan.size());
        for (RunTools.PlanItem item : plan) {
            planBox.add(planItem(item));
        }
    }

    private Div planItem(RunTools.PlanItem item) {
        boolean done = "done".equals(item.status());
        boolean active = "in_progress".equals(item.status());
        Icon icon = (done ? Solid.CHECK : active ? Solid.ARROW_RIGHT : Solid.MINUS).create();
        icon.getElement().setAttribute("aria-label", loc.getValue(done ? L.ENUM_AGENT_PLAN_DONE
                : active ? L.ENUM_AGENT_PLAN_IN_PROGRESS : L.ENUM_AGENT_PLAN_PENDING));
        icon.getElement().setAttribute("title", icon.getElement().getAttribute("aria-label"));
        Span text = new Span(item.text());
        if (done) {
            text.addClassName(CssStyles.AGENT_PLAN_DONE);
        } else if (active) {
            text.addClassName(CssStyles.AGENT_PLAN_ACTIVE);
        }
        Div row = new Div(icon, text);
        row.addClassName(CssStyles.AGENT_PLAN_ITEM);
        return row;
    }

    /**
     * A command that waits for the user pops up as a dialog over the console, the oldest first; when it is decided the
     * next one does.
     */
    private void showApproval() throws Exception {
        List<PendingApproval> waiting = run != null && run.getStatus() == AgentRunStatus.WAITING_APPROVAL
                ? agentApprovalService.pending(run) : List.of();
        if (approvalDialog != null && waiting.stream().noneMatch(a -> a.id().equals(approvalDialog.getApproval().id()))) {
            // decided meanwhile (or the run was paused): the question is gone
            approvalDialog.close();
            approvalDialog = null;
        }
        if (approvalDialog == null && !waiting.isEmpty()) {
            PendingApproval next = waiting.getFirst();
            AgentRun asked = run;
            approvalDialog = new AgentApprovalDialog(agent.getName(), next, approved -> {
                approvalDialog = null;
                perform(() -> agentApprovalService.decide(asked, next.id(), approved));
            });
            approvalDialog.create();
            approvalDialog.open();
        }
    }

    private void showTranscript() throws Exception {
        Long runId = run == null ? null : run.getId();
        if (!java.util.Objects.equals(runId, shownRunId)) {
            // another run: start the transcript again
            renderer.clear();
            shown = 0;
            shownRunId = runId;
            rollbackButtons.clear();
            queuedBubbles.clear();
            shownQueued = List.of();
        }
        // the messages that wait stay the last bubbles: they come off while the transcript grows and go back after it
        List<AgentRunner.QueuedMessage> queued = run == null ? List.of() : agentRunner.queued(run);
        boolean queuedChanged = !queued.stream().map(AgentRunner.QueuedMessage::id).toList().equals(shownQueued)
                || run != null && agentMessageService.count(run) != shown;
        if (queuedChanged) {
            queuedBubbles.forEach(transcriptBox::remove);
            queuedBubbles.clear();
        }
        showMessages();
        if (queuedChanged) {
            for (AgentRunner.QueuedMessage message : queued) {
                Div bubble = queuedBubble(message);
                queuedBubbles.add(bubble);
                transcriptBox.add(bubble);
            }
            shownQueued = queued.stream().map(AgentRunner.QueuedMessage::id).toList();
            if (!queued.isEmpty()) {
                JavascriptLibrary.scrollToBottomIfAtBottom(transcriptBox.getElement());
            }
        }
    }

    private void showMessages() throws Exception {
        if (run == null || agentMessageService.count(run) == shown) {
            return;
        }
        List<AgentMessage> messages = agentMessageService.messages(run);
        boolean first = shown == 0;
        for (AgentMessage row : messages.subList(Math.min(shown, messages.size()), messages.size())) {
            int before = transcriptBox.getComponentCount();
            renderer.render(row.toChatMessage(), row.getReasoning(), row.isSummary(), row.getCreation());
            if (row.isRewound()) {
                // what the agent forgot is still there to read
                for (int i = before; i < transcriptBox.getComponentCount(); i++) {
                    transcriptBox.getComponentAt(i).getElement().getClassList().add(CssStyles.AGENT_REWOUND);
                }
            } else if (row.toChatMessage().getRole() == LLMRole.USER && !row.isSummary()
                    && transcriptBox.getComponentCount() > before && transcriptBox.getComponentAt(before) instanceof Div bubble) {
                bubble.add(rollbackButton(row));
            }
        }
        shown = messages.size();
        if (first) {
            JavascriptLibrary.scrollToBottomAfterLayout(transcriptBox.getElement());
        } else {
            JavascriptLibrary.scrollToBottomIfAtBottom(transcriptBox.getElement());
        }
    }

    /**
     * The button under a message of the author that takes the conversation back to before it, see
     * {@link CheckpointService#rewindToMessage}.
     */
    private Button rollbackButton(AgentMessage row) {
        Button button = new Button(loc.getValue(L.LABEL_REWIND), Solid.BACKWARD_STEP.create(), event -> rollBack(row.getUuid()));
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        rollbackButtons.add(button);
        showRollbackButton(button, run == null ? null : run.getStatus());
        return button;
    }

    private void showRollbackButton(Button button, AgentRunStatus status) {
        boolean possible = status != null && ROLLBACK.contains(status);
        button.setEnabled(possible);
        button.setTooltipText(loc.getValue(possible ? L.HELP_AGENT_ROLLBACK_MESSAGE
                : status != null && status.isActive() ? L.MSG_AGENT_CHECKPOINT_BUSY : L.MSG_AGENT_REWIND_PAUSE_FIRST));
    }

    /**
     * Asks, then goes back to before the message: the checkpoint it goes to and what the agent forgets are shown first. A run
     * that waits for the author is paused for it.
     */
    private void rollBack(String messageUuid) {
        AgentRun current = run;
        if (current == null) {
            return;
        }
        try {
            CheckpointService.RewindPlan plan = checkpointService.planRewindToMessage(current, messageUuid);
            ConfirmDialog.open(String.format(loc.getValue(L.MSG_AGENT_CONFIRM_ROLLBACK_MESSAGE), plan.checkpoint().id(),
                    plan.checkpoint().round(), plan.forgotten()), () -> rollBackNow(messageUuid), () -> {});
        } catch (CheckpointService.CheckpointException e) {
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    void rollBackNow(String messageUuid) {
        perform(() -> {
            try {
                AgentRun target = run.getStatus() == AgentRunStatus.IDLE ? agentRunService.pause(run) : run;
                CheckpointService.Rewound result = checkpointService.rewindToMessage(target, messageUuid);
                Notification.success(String.format(loc.getValue(L.MSG_AGENT_CHECKPOINT_REWOUND), result.restored().checkpoint().id(),
                        result.forgotten(), result.restored().removedFiles().size()));
            } catch (CheckpointService.CheckpointException e) {
                Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
            }
            // the rows that were shown are marked now: the transcript is made again
            shownRunId = null;
            return null;
        });
    }

    /**
     * The author ran a command from the bar: given to the agent as the author's, see {@link AgentRunner#command}.
     */
    private void runCommand(String line) {
        if (run == null) {
            return;
        }
        // deletes the notes of the agent, as the button of the workspace does
        if (line.strip().equals("/" + AgentRunner.CLEAR_WORKSPACE) || line.strip().startsWith("/" + AgentRunner.CLEAR_WORKSPACE + " ")) {
            ConfirmDialog.open(loc.getValue(L.MSG_AGENT_CONFIRM_CLEAR_WORKSPACE), () -> runCommandNow(line), () -> {});
            return;
        }
        runCommandNow(line);
    }

    private void runCommandNow(String line) {
        perform(() -> agentRunner.command(run, line));
        fastPollUntil = System.currentTimeMillis() + COMMAND_POLL_FOR_MILLIS;
        getUI().ifPresent(ui -> ui.setPollInterval(COMMAND_POLL_MILLIS));
    }

    /**
     * A message or a command of the author that waits for the agent: a bubble marked as queued, with the small button that
     * gives it to the agent now.
     */
    private Div queuedBubble(AgentRunner.QueuedMessage message) {
        Button force = new Button(Solid.BOLT.create(), event -> perform(() -> {
            agentRunner.force(run, message.id());
            return null;
        }));
        force.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        L now = message.command() ? L.LABEL_AGENT_RUN_NOW : L.LABEL_AGENT_SEND_NOW;
        force.setTooltipText(loc.getValue(now) + ": " + loc.getValue(message.command() ? L.HELP_AGENT_RUN_NOW : L.HELP_AGENT_SEND_NOW));
        force.setAriaLabel(loc.getValue(now));
        Div heading = renderer.text(loc.getValue(message.command() ? L.LABEL_COMMAND : L.LABEL_YOU) + " ("
                + loc.getValue(L.LABEL_AGENT_QUEUED) + ")", true);
        Button cancel = new Button(Solid.TRASH.create(), event -> perform(() -> {
            agentRunner.cancelQueued(run, message.id());
            return null;
        }));
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        cancel.setTooltipText(loc.getValue(L.LABEL_CANCEL) + ": " + loc.getValue(L.HELP_AGENT_CANCEL_QUEUED));
        cancel.setAriaLabel(loc.getValue(L.LABEL_CANCEL));
        HorizontalLayout header = new HorizontalLayout(heading, new HorizontalLayout(force, cancel));
        header.setWidthFull();
        header.setPadding(false);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        Div bubble = new Div(header, message.command() ? renderer.text(StringUtils.defaultString(message.text()), false)
                : new Markdown(StringUtils.defaultString(message.text())));
        bubble.addClassNames(CssStyles.HELP_MESSAGE, CssStyles.HELP_MESSAGE_USER, CssStyles.HELP_MESSAGE_QUEUED);
        return bubble;
    }

    private void send() {
        String text = messageField.getValue();
        if ((StringUtils.isBlank(text) && pendingImages.isEmpty()) || run == null) {
            return;
        }
        perform(() -> {
            agentRunner.send(run, text, storePendingImages());
            messageField.clear();
            pendingImages.clear();
            showPendingImages();
            return null;
        });
    }

    /**
     * The largest image of the workspace of the agent, the default limit when it can't be read.
     */
    private long imageLimit() {
        try {
            return fileSystemService.getLimits(agent).maxImageBytes();
        } catch (Exception e) {
            return fileSystemService.getLimits().maxImageBytes();
        }
    }

    private boolean seesImages() {
        return agent.getMainProfile() != null && agent.getMainProfile().isImages();
    }

    /**
     * An image the author picked for the next message: kept until it is sent, and shown beside the message.
     */
    void attachImage(String fileName, byte[] content) {
        imageUpload.clearFileList();
        if (pendingImages.size() >= AgentRunner.MAX_ATTACHED_IMAGES) {
            return;
        }
        if (!ImageFiles.isImage(content)) {
            Notification.warning(String.format(loc.getValue(L.MSG_AGENT_UPLOAD_NOT_IMAGE), fileName));
            return;
        }
        pendingImages.add(new PendingImage(fileName, content));
        showPendingImages();
    }

    private void showPendingImages() {
        pendingRow.removeAll();
        for (PendingImage image : pendingImages) {
            Button remove = new Button(Solid.XMARK.create(), event -> {
                pendingImages.remove(image);
                showPendingImages();
            });
            remove.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            remove.setTooltipText(loc.getValue(L.LABEL_DELETE));
            Span name = new Span(image.name());
            name.addClassNames(CssStyles.HINT_TEXT, CssStyles.WRAP_ANYWHERE);
            HorizontalLayout chip = new HorizontalLayout(Solid.IMAGE.create(), name, remove);
            chip.setAlignItems(FlexComponent.Alignment.CENTER);
            chip.setSpacing(false);
            pendingRow.add(chip);
        }
        pendingRow.setVisible(!pendingImages.isEmpty());
        imageUpload.setEnabled(sendButton.isEnabled() && pendingImages.size() < AgentRunner.MAX_ATTACHED_IMAGES);
    }

    /**
     * Puts the images of the message in the workspace of the agent, in {@value #UPLOAD_FOLDER}, where the agent can
     * look at them (the same limits and rules of names as for the agent's own files); a name that is taken gets a number.
     *
     * @return their paths
     */
    private List<String> storePendingImages() throws Exception {
        List<String> paths = new ArrayList<>();
        if (pendingImages.isEmpty()) {
            return paths;
        }
        VirtualFileSystem files = fileSystemService.openWorkspace(agent);
        try {
            for (PendingImage image : pendingImages) {
                VirtualPath path = uploadPath(files, image.name());
                files.writeImage(path, image.content());
                paths.add(path.toString());
            }
        } catch (VirtualFileException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return paths;
    }

    /**
     * The path for an uploaded file in the uploads folder: its own name, or with {@code -2}, {@code -3}... before the
     * extension when that name is taken.
     */
    static VirtualPath uploadPath(VirtualFileSystem files, String fileName) {
        VirtualPath folder = VirtualPath.ROOT.resolve(UPLOAD_FOLDER);
        int dot = fileName.lastIndexOf('.');
        String base = dot > 0 ? fileName.substring(0, dot) : fileName;
        String extension = dot > 0 ? fileName.substring(dot) : "";
        VirtualPath path = folder.resolve(fileName);
        for (int i = 2; files.stat(path) != null; i++) {
            path = folder.resolve(base + "-" + i + extension);
        }
        return path;
    }

    private void perform(Callable<?> action) {
        try {
            action.call();
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            // the rules of the service, its text says what
            Notification.warning(String.format(loc.getValue(L.ERROR_AGENT_ACTION_FAILED), e.getMessage()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        refreshRun();
        if (onChange != null) {
            onChange.run();
        }
    }

    // for the tests of the console

    VerticalLayout getTranscriptBox() {
        return transcriptBox;
    }

    TextArea getMessageField() {
        return messageField;
    }

    CommandBar getCommandBar() {
        return commandBar;
    }

    Button getSendButton() {
        return sendButton;
    }

    Upload getImageUpload() {
        return imageUpload;
    }

    int getPendingImages() {
        return pendingImages.size();
    }

    Button getPauseButton() {
        return pauseButton;
    }

    Button getPauseNowButton() {
        return pauseNowButton;
    }

    Button getResumeButton() {
        return resumeButton;
    }

    Button getReviewButton() {
        return reviewButton;
    }

    Button getFinishButton() {
        return finishButton;
    }

    Button getStopButton() {
        return stopButton;
    }

    List<Button> getRollbackButtons() {
        return rollbackButtons;
    }

    List<Div> getQueuedBubbles() {
        return queuedBubbles;
    }

    Span getStatusLabel() {
        return statusLabel;
    }

    Span getUsageLabel() {
        return usageLabel;
    }

    Button getPlanButton() {
        return planButton;
    }

    Popover getPlanPopover() {
        return planPopover;
    }

    VerticalLayout getPlanBox() {
        return planBox;
    }

    AgentApprovalDialog getApprovalDialog() {
        return approvalDialog;
    }

    Dialog getLastDialog() {
        return lastDialog;
    }

    AgentRunHistory getHistory() {
        return history;
    }

    AgentContextView getContextView() {
        return contextView;
    }

    AgentCopyView getCopyView() {
        return copyView;
    }

    TabSheet getTabs() {
        return tabs;
    }

    VerticalLayout getCommandRow() {
        return commandRow;
    }

    Button getCommandButton() {
        return commandButton;
    }

    Button getRefreshButton() {
        return refreshButton;
    }

    Button getCloseButton() {
        return closeButton;
    }

    AgentSubAgentsView getSubAgentsView() {
        return subAgentsView;
    }

    AgentDiagnosticsView getDiagnosticsView() {
        return diagnosticsView;
    }

    AgentWorkspaceBrowser getWorkspaceBrowser() {
        return workspaceBrowser;
    }
}
