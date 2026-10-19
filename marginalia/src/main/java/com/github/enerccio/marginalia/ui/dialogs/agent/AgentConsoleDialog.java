package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentMessage;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentApprovalService;
import com.github.enerccio.marginalia.domain.service.AgentApprovalService.PendingApproval;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.service.AgentRunner;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Talks to an agent and shows what it does: the transcript of its latest run (what it was told, what it said, the
 * commands it called with their results, folded), the state and the tokens of the run, the question it waits an answer
 * to or the report it ended with, a box to write to it, and its workspace (the second tab). The run is read again
 * every few seconds while the dialog is open, a round of the agent takes longer than that.
 */
@Configurable
public class AgentConsoleDialog extends Dialog implements LLMInteract {

    /**
     * How often the open console looks at the run.
     */
    static final int POLL_MILLIS = 2000;

    /**
     * How much of a result of a command is shown; the model got it whole, up to its own cut.
     */
    static final int RESULT_LIMIT = 20_000;

    private static final int SUMMARY_LIMIT = 80;

    private static final Set<AgentRunStatus> PAUSABLE = Set.of(AgentRunStatus.RUNNING, AgentRunStatus.IDLE,
            AgentRunStatus.WAITING_APPROVAL);

    private static final Set<AgentRunStatus> STOPPABLE = Set.of(AgentRunStatus.RUNNING, AgentRunStatus.IDLE,
            AgentRunStatus.WAITING_APPROVAL, AgentRunStatus.PAUSING, AgentRunStatus.PAUSED, AgentRunStatus.STOPPED_KEPT,
            AgentRunStatus.DONE);

    @Autowired
    private Localization loc;

    @Autowired
    private AgentRunService agentRunService;

    @Autowired
    private AgentRunner agentRunner;

    @Autowired
    private AgentMessageService agentMessageService;

    @Autowired
    private AgentApprovalService agentApprovalService;

    private final Agent agent;
    private Runnable onChange;

    // the latest run of the agent, null when it never ran
    private AgentRun run;
    // how much of the transcript of the run is shown
    private Long shownRunId;
    private int shown;
    // the bodies of the folded commands by the id of the call, where their results go when they come
    private final Map<String, VerticalLayout> callBodies = new HashMap<>();

    private Span statusLabel;
    private Span usageLabel;
    private Div banner;
    private Div bannerHeading;
    // whether the banner is to be shown: the question or the report of the agent
    private boolean bannerWanted;
    private Markdown bannerText;
    private VerticalLayout transcriptBox;
    private TextArea messageField;
    private Button sendButton;
    private Button pauseButton;
    private Button resumeButton;
    private Button stopButton;
    private Button finishButton;
    private AgentWorkspaceBrowser workspaceBrowser;
    private AgentRunHistory history;
    private TabSheet tabs;
    // the run the user picked in the history, null for the latest one that was not thrown away
    private String selectedRunUuid;

    private Dialog lastDialog;
    // the popup that asks for the approval of a command, null when none is open
    private AgentApprovalDialog approvalDialog;
    private Registration pollRegistration;
    private int previousPollInterval = -1;

    public AgentConsoleDialog(Agent agent) {
        this.agent = agent;
    }

    /**
     * Called when the user changed the run from the console (paused, resumed, stopped it), so the table of agents
     * can show it.
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_AGENT_CONSOLE) + " - " + agent.getName());
        setWidth("900px");
        setHeight("90%");

        statusLabel = new Span();
        statusLabel.addClassName(CssStyles.TEXT_SEMIBOLD);
        usageLabel = new Span();
        usageLabel.addClassNames(CssStyles.HINT_TEXT);
        HorizontalLayout statusRow = new HorizontalLayout(statusLabel, usageLabel);
        statusRow.setWidthFull();
        statusRow.setAlignItems(FlexComponent.Alignment.CENTER);
        statusRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        bannerHeading = text("", true);
        bannerText = new Markdown();
        banner = new Div(bannerHeading, bannerText);
        banner.addClassName(CssStyles.HELP_MESSAGE);

        transcriptBox = new VerticalLayout();
        transcriptBox.setPadding(false);
        transcriptBox.setSpacing(false);
        transcriptBox.addClassNames(CssStyles.HELP_MESSAGES, CssStyles.FULL_WIDTH, CssStyles.MIN_HEIGHT_0);

        messageField = new TextArea();
        messageField.setLabel(loc.getValue(L.ENUM_DOMAIN_MESSAGE));
        messageField.setWidthFull();
        messageField.setMaxHeight("120px");
        sendButton = new Button(loc.getValue(L.LABEL_SEND), Solid.PAPER_PLANE.create(), event -> send());
        sendButton.setThemeName("primary");
        // Ctrl+Enter (Cmd+Enter on a Mac) sends from the box; the value goes to the server with every key, so it is there
        // when the shortcut is
        messageField.setValueChangeMode(ValueChangeMode.EAGER);
        sendButton.addClickShortcut(Key.ENTER, KeyModifier.CONTROL).listenOn(messageField);
        sendButton.addClickShortcut(Key.ENTER, KeyModifier.META).listenOn(messageField);
        sendButton.setTooltipText("Ctrl+Enter");
        HorizontalLayout inputRow = new HorizontalLayout(messageField, sendButton);
        inputRow.setWidthFull();
        inputRow.setAlignItems(FlexComponent.Alignment.END);
        inputRow.expand(messageField);

        // the state and the box that answers stay where they are, the transcript between them scrolls; what the agent
        // waits for (its question, its report) is the last bubble of the transcript, see placeBanner
        VerticalLayout runTab = new VerticalLayout(statusRow, transcriptBox, inputRow);
        runTab.setSizeFull();
        runTab.setPadding(false);
        runTab.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.OVERFLOW_HIDDEN);
        runTab.expand(transcriptBox);

        workspaceBrowser = new AgentWorkspaceBrowser(agent, this::isWorking);
        workspaceBrowser.create();

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
        history.create();

        tabs = new TabSheet();
        tabs.setSizeFull();
        // the workspace wants every pixel, the other tabs keep a small padding of their own
        tabs.addThemeVariants(TabSheetVariant.LUMO_NO_PADDING);
        runTab.addClassName(CssStyles.PADDING_COMPACT);
        history.addClassName(CssStyles.PADDING_COMPACT);
        tabs.add(loc.getValue(L.LABEL_AGENT_RUN), runTab);
        tabs.add(loc.getValue(L.LABEL_AGENT_RUNS), history);
        tabs.add(loc.getValue(L.LABEL_AGENT_WORKSPACE), workspaceBrowser);
        add(tabs);

        pauseButton = new Button(loc.getValue(L.LABEL_PAUSE), Solid.PAUSE.create(),
                event -> perform(() -> agentRunService.pause(run)));
        resumeButton = new Button(loc.getValue(L.LABEL_RESUME), Solid.PLAY.create(), event -> perform(() -> {
            agentRunner.run(agentRunService.resume(run));
            return null;
        }));
        finishButton = new Button(loc.getValue(L.LABEL_FINISH), Solid.CHECK.create(),
                event -> perform(() -> agentRunService.finish(run)));
        stopButton = new Button(loc.getValue(L.LABEL_STOP), Solid.STOP.create(), event -> {
            AgentStopDialog dialog = new AgentStopDialog(agent.getName(), run.getStatus() != AgentRunStatus.DONE,
                    keep -> perform(() -> agentRunService.stop(run, keep)));
            dialog.create();
            dialog.open();
            lastDialog = dialog;
        });
        Button refreshButton = new Button(loc.getValue(L.LABEL_REFRESH), Solid.ARROWS_ROTATE.create(), event -> refreshRun());
        Button closeButton = new Button(loc.getValue(L.LABEL_CLOSE), event -> close());
        HorizontalLayout footer = new HorizontalLayout(pauseButton, resumeButton, finishButton, stopButton, refreshButton, closeButton);
        footer.setWidthFull();
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
     * Whether this console is the console of the agent.
     */
    public boolean isFor(String agentUuid) {
        return agent.getUuid().equals(agentUuid);
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
            statusLabel.setText(loc.getValue(L.MSG_AGENT_NO_RUN_YET));
            usageLabel.setText("");
            bannerWanted = false;
        } else {
            AgentRunStatus status = run.getStatus();
            String reason = StringUtils.trimToEmpty(run.getStatusReason());
            boolean bannerStatus = status == AgentRunStatus.IDLE || status == AgentRunStatus.DONE;
            statusLabel.setText(loc.getValue(loc.getAgentRunStatus(status))
                    + (!reason.isEmpty() && !bannerStatus ? ": " + reason : ""));
            long budget = agentRunService.getTokenBudget(run);
            long context = agentMessageService.getContextTokens(run);
            long contextCap = agentRunService.getContextCap(run);
            // which run this is, there can be many in the history
            usageLabel.setText((run.getStartedAt() == null ? "" : java.text.DateFormat.getDateTimeInstance(
                    java.text.DateFormat.SHORT, java.text.DateFormat.SHORT).format(run.getStartedAt()) + "   ")
                    + loc.getValue(L.LABEL_ROUNDS) + ": " + run.getRounds() + "   "
                    + loc.getValue(L.LABEL_TOKEN_CONTEXT) + ": " + context + (contextCap > 0 ? " / " + contextCap : "") + "   "
                    + loc.getValue(L.LABEL_TOKENS_TOTAL) + ": " + run.getTotalTokens()
                    + (budget > 0 ? " / " + budget : ""));
            // the question the agent waits an answer to, or the report it ended with
            bannerWanted = bannerStatus && !reason.isEmpty();
            bannerHeading.setText(loc.getValue(status == AgentRunStatus.DONE ? L.LABEL_AGENT_REPORT : L.LABEL_AGENT_WAITS));
            bannerText.setContent(reason);
        }
        AgentRunStatus status = run == null ? null : run.getStatus();
        messageField.setEnabled(status == AgentRunStatus.IDLE);
        sendButton.setEnabled(status == AgentRunStatus.IDLE);
        pauseButton.setEnabled(status != null && PAUSABLE.contains(status));
        resumeButton.setEnabled(status != null && status.isResumable());
        finishButton.setEnabled(status == AgentRunStatus.IDLE);
        stopButton.setEnabled(status != null && STOPPABLE.contains(status));
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
            transcriptBox.removeAll();
            callBodies.clear();
            shown = 0;
            shownRunId = runId;
        }
        if (run == null || agentMessageService.count(run) == shown) {
            if (placeBanner()) {
                JavascriptLibrary.scrollToBottomIfAtBottom(transcriptBox.getElement());
            }
            return;
        }
        List<AgentMessage> messages = agentMessageService.messages(run);
        boolean first = shown == 0;
        for (AgentMessage row : messages.subList(Math.min(shown, messages.size()), messages.size())) {
            render(row.toChatMessage(), row.getReasoning());
        }
        shown = messages.size();
        placeBanner();
        if (first) {
            JavascriptLibrary.scrollToBottomAfterLayout(transcriptBox.getElement());
        } else {
            JavascriptLibrary.scrollToBottomIfAtBottom(transcriptBox.getElement());
        }
    }

    /**
     * What the agent waits for (its question, its report) is the last bubble of the transcript, under the steps that led
     * to it: after the messages that were added, and gone again when the run goes on.
     *
     * @return whether the banner is there as a new last bubble
     */
    private boolean placeBanner() {
        boolean last = bannerWanted && transcriptBox.getComponentCount() > 0
                && transcriptBox.getComponentAt(transcriptBox.getComponentCount() - 1) == banner;
        if (last) {
            return false;
        }
        if (banner.getParent().isPresent()) {
            transcriptBox.remove(banner);
        }
        if (bannerWanted) {
            transcriptBox.add(banner);
        }
        return bannerWanted;
    }

    private void render(LLMChatMessage message, String reasoning) {
        switch (message.getRole()) {
            case USER -> transcriptBox.add(bubble(loc.getValue(L.LABEL_YOU), message.getContent(), true));
            case ASSISTANT -> {
                if (StringUtils.isNotBlank(reasoning)) {
                    transcriptBox.add(reasoningBlock(reasoning));
                }
                if (StringUtils.isNotBlank(message.getContent())) {
                    transcriptBox.add(bubble(loc.getValue(L.ENUM_DOMAIN_AGENT), message.getContent(), false));
                }
                for (LLMToolCall call : message.getToolCalls()) {
                    transcriptBox.add(callBlock(call));
                }
            }
            case TOOL -> {
                VerticalLayout body = callBodies.get(message.getToolCallId());
                if (body != null) {
                    body.add(text(loc.getValue(L.LABEL_RESULT), true), text(cut(message.getContent()), false));
                } else {
                    transcriptBox.add(bubble(loc.getValue(L.LABEL_RESULT), cut(message.getContent()), false));
                }
            }
            default -> {
                // the system message is made for every request and not part of the transcript
            }
        }
    }

    private Div bubble(String who, String content, boolean user) {
        Div bubble = new Div(text(who, true), new Markdown(StringUtils.defaultString(content)));
        bubble.addClassName(CssStyles.HELP_MESSAGE);
        if (user) {
            bubble.addClassName(CssStyles.HELP_MESSAGE_USER);
        }
        return bubble;
    }

    /**
     * What the model thought before its answer, folded to one line: the reasoning is long and for looking up.
     */
    private Div reasoningBlock(String reasoning) {
        Details details = new Details(loc.getValue(L.LABEL_REASONING), text(reasoning, false));
        details.addClassName(CssStyles.HINT_TEXT);
        Div block = new Div(details);
        block.addClassName(CssStyles.HELP_MESSAGE);
        return block;
    }

    private Div callBlock(LLMToolCall call) {
        VerticalLayout body = new VerticalLayout(text(StringUtils.defaultString(call.getArguments()), false));
        body.setPadding(false);
        body.setSpacing(false);
        callBodies.put(call.getId(), body);
        Details details = new Details(call.getName() + " " + StringUtils.abbreviate(
                StringUtils.defaultString(call.getArguments()).replaceAll("\\s+", " "), SUMMARY_LIMIT), body);
        details.addClassName(CssStyles.TEXT_MONOSPACE);
        Div block = new Div(details);
        block.addClassName(CssStyles.HELP_MESSAGE);
        return block;
    }

    /**
     * A block of text of a message; the heading (who said it) and the text are blocks, so they are on lines of their own.
     */
    private Div text(String content, boolean heading) {
        Div block = new Div(content);
        block.addClassNames(CssStyles.PRE_WRAP, CssStyles.WRAP_ANYWHERE);
        if (heading) {
            block.addClassNames(CssStyles.TEXT_SEMIBOLD, CssStyles.TEXT_SECONDARY, CssStyles.TEXT_XS);
        }
        return block;
    }

    private static String cut(String content) {
        String text = StringUtils.defaultString(content);
        return text.length() > RESULT_LIMIT ? text.substring(0, RESULT_LIMIT) + "\n..." : text;
    }

    private void send() {
        String text = messageField.getValue();
        if (StringUtils.isBlank(text) || run == null) {
            return;
        }
        perform(() -> {
            agentRunner.send(run, text);
            messageField.clear();
            return null;
        });
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

    Button getSendButton() {
        return sendButton;
    }

    Button getPauseButton() {
        return pauseButton;
    }

    Button getResumeButton() {
        return resumeButton;
    }

    Button getFinishButton() {
        return finishButton;
    }

    Button getStopButton() {
        return stopButton;
    }

    Div getBanner() {
        return banner;
    }

    Div getBannerHeading() {
        return bannerHeading;
    }

    Markdown getBannerText() {
        return bannerText;
    }

    Span getStatusLabel() {
        return statusLabel;
    }

    Span getUsageLabel() {
        return usageLabel;
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

    TabSheet getTabs() {
        return tabs;
    }

    AgentWorkspaceBrowser getWorkspaceBrowser() {
        return workspaceBrowser;
    }
}
