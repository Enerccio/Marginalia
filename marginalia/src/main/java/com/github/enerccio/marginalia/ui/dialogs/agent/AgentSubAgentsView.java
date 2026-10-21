package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.AgentMessage;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.AgentMessageService.SubThread;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * The conversations of the sub-agents of the run, to read: what the agent told each one, what it said, the commands it called
 * with their results and how it ended, in the blocks of the transcript of the run. They are stored with the run, so the ones of
 * a run that ended can be read as well as those of a run that works (they are looked at again while the tab is shown and the
 * console polls).
 */
@Configurable
public class AgentSubAgentsView extends VerticalLayout implements LLMInteract {

    private static final int TASK_LIMIT = 60;

    @Autowired
    private Localization loc;

    @Autowired
    private AgentMessageService agentMessageService;

    private final Supplier<AgentRun> runSupplier;

    private Span emptyLabel;
    private VerticalLayout content;
    private ComboBox<SubThread> picker;
    private Div info;
    private VerticalLayout transcriptBox;
    private AgentTranscriptRenderer renderer;

    // what is shown: the run, the sub-agent and how many of its rows
    private String shownRun;
    private String shownHandle;
    private int shownRows;
    // the handles with their states, to set the items of the picker only when they changed
    private String pickerSignature = "";
    // true while the view sets the picker itself
    private boolean adjusting;

    /**
     * @param runSupplier the run the console looks at now, null when there is none
     */
    public AgentSubAgentsView(Supplier<AgentRun> runSupplier) {
        this.runSupplier = runSupplier;
    }

    public void create() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.GAP_S);

        Span help = new Span(loc.getValue(L.HELP_AGENT_SUBAGENTS));
        help.addClassName(CssStyles.HINT_TEXT);
        Button refreshButton = new Button(loc.getValue(L.LABEL_REFRESH), Solid.ARROWS_ROTATE.create(), event -> refresh());
        refreshButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        HorizontalLayout header = new HorizontalLayout(help, refreshButton);
        header.setWidthFull();
        header.setPadding(false);
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        emptyLabel = new Span(loc.getValue(L.MSG_AGENT_NO_SUBAGENTS));
        emptyLabel.addClassName(CssStyles.HINT_TEXT);

        picker = new ComboBox<>(loc.getValue(L.LABEL_AGENT_SUBAGENT));
        picker.setWidthFull();
        picker.setAllowCustomValue(false);
        picker.setItemLabelGenerator(this::label);
        picker.addValueChangeListener(event -> {
            if (!adjusting && event.getValue() != null) {
                show(event.getValue());
            }
        });
        info = new Div();
        info.addClassNames(CssStyles.PRE_WRAP, CssStyles.WRAP_ANYWHERE, CssStyles.HINT_TEXT);

        transcriptBox = new VerticalLayout();
        transcriptBox.setPadding(false);
        transcriptBox.setSpacing(false);
        transcriptBox.addClassNames(CssStyles.HELP_MESSAGES, CssStyles.FULL_WIDTH, CssStyles.MIN_HEIGHT_0);
        renderer = new AgentTranscriptRenderer(loc, transcriptBox);

        content = new VerticalLayout(picker, info, transcriptBox);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(false);
        content.addClassNames(CssStyles.MIN_HEIGHT_0, CssStyles.GAP_S);
        content.expand(transcriptBox);

        add(header, emptyLabel, content);
        expand(content);
        showEmpty();
    }

    /**
     * Reads the conversations of the run again: the list of the sub-agents, and the part of the shown one that is new.
     */
    public void refresh() {
        if (picker == null) {
            return;
        }
        AgentRun run = runSupplier.get();
        List<SubThread> threads;
        try {
            threads = run == null ? List.of() : agentMessageService.threads(run);
        } catch (IllegalArgumentException e) {
            threads = List.of();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        if (!Objects.equals(run == null ? null : run.getUuid(), shownRun)) {
            // another run: start again
            shownRun = run == null ? null : run.getUuid();
            shownHandle = null;
            shownRows = 0;
            pickerSignature = "";
            renderer.clear();
        }
        if (threads.isEmpty()) {
            showEmpty();
            return;
        }
        emptyLabel.setVisible(false);
        content.setVisible(true);
        String signature = signature(run, threads);
        SubThread selected = threads.stream().filter(thread -> thread.handle().equals(shownHandle)).findFirst()
                .orElse(threads.getFirst());
        if (!signature.equals(pickerSignature)) {
            pickerSignature = signature;
            adjusting = true;
            try {
                picker.setItems(threads);
                picker.setValue(selected);
            } finally {
                adjusting = false;
            }
        }
        show(selected);
    }

    /**
     * Shows the conversation of the sub-agent the {@code spawn_agent} call started.
     *
     * @return false when none is stored for the call
     */
    public boolean showCall(String callId) {
        refresh();
        AgentRun run = runSupplier.get();
        if (run == null || callId == null) {
            return false;
        }
        try {
            for (SubThread thread : agentMessageService.threads(run)) {
                if (callId.equals(thread.callId())) {
                    shownHandle = null;
                    adjusting = true;
                    try {
                        picker.setValue(thread);
                    } finally {
                        adjusting = false;
                    }
                    show(thread);
                    return true;
                }
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        return false;
    }

    private void showEmpty() {
        emptyLabel.setVisible(true);
        content.setVisible(false);
    }

    private String signature(AgentRun run, List<SubThread> threads) {
        StringBuilder signature = new StringBuilder(run.getStatus().name()).append(';');
        for (SubThread thread : threads) {
            signature.append(thread.handle()).append(thread.end()).append(thread.rewound()).append(';');
        }
        return signature.toString();
    }

    private String label(SubThread thread) {
        return thread.handle() + (thread.profile() == null ? "" : " [" + thread.profile() + "]") + " - " + state(thread, runSupplier.get()) + " - "
                + StringUtils.abbreviate(StringUtils.defaultString(thread.task()).replaceAll("\\s+", " "), TASK_LIMIT)
                + (thread.rewound() ? " (" + loc.getValue(L.LABEL_AGENT_SUBAGENT_REWOUND) + ")" : "");
    }

    private String state(SubThread thread, AgentRun run) {
        if (thread.isOver()) {
            return endLabel(thread.end());
        }
        // not ended: it works with the run, or waits for the run to be resumed
        return loc.getValue(run != null && run.getStatus().isActive() ? L.ENUM_AGENT_RUN_RUNNING : L.ENUM_AGENT_RUN_PAUSED);
    }

    /**
     * Shows the sub-agent: its line, and the rows of its conversation that are not shown yet.
     */
    private void show(SubThread thread) {
        boolean another = !thread.handle().equals(shownHandle);
        if (another) {
            renderer.clear();
            shownHandle = thread.handle();
            shownRows = 0;
        }
        info.setText(String.format("%s: %s   %s: %d   %s: %d\n%s: %s", loc.getValue(L.LABEL_STATE), state(thread, runSupplier.get()),
                loc.getValue(L.LABEL_ROUNDS), thread.rounds(), loc.getValue(L.LABEL_TOKENS), thread.tokens(),
                loc.getValue(L.LABEL_AGENT_TASK), StringUtils.defaultString(thread.task()).strip()));
        List<AgentMessage> rows = thread.rows();
        if (rows.size() == shownRows) {
            return;
        }
        renderer.setLabels(loc.getValue(L.ENUM_DOMAIN_AGENT), thread.handle());
        for (AgentMessage row : rows.subList(Math.min(shownRows, rows.size()), rows.size())) {
            int before = transcriptBox.getComponentCount();
            if (row.getThreadEnd() != null) {
                transcriptBox.add(AgentTranscriptRenderer.stamped(renderer.bubble(loc.getValue(L.LABEL_RESULT) + " - " + endLabel(row.getThreadEnd()),
                        row.toChatMessage().getContent(), false), row.getCreation()));
            } else {
                renderer.render(row.toChatMessage(), row.getReasoning(), row.isSummary(), row.getCreation());
            }
            if (row.isRewound()) {
                // what the agent went back over is still there to read
                for (int i = before; i < transcriptBox.getComponentCount(); i++) {
                    transcriptBox.getComponentAt(i).getElement().getClassList().add(CssStyles.AGENT_REWOUND);
                }
            }
        }
        boolean first = shownRows == 0;
        shownRows = rows.size();
        if (first) {
            JavascriptLibrary.scrollToBottomAfterLayout(transcriptBox.getElement());
        } else {
            JavascriptLibrary.scrollToBottomIfAtBottom(transcriptBox.getElement());
        }
    }

    private String endLabel(String end) {
        return switch (end) {
            case "DONE" -> loc.getValue(L.ENUM_AGENT_RUN_DONE);
            case "FAILED" -> loc.getValue(L.ENUM_AGENT_RUN_FAILED);
            case "CANCELLED" -> loc.getValue(L.LABEL_AGENT_SUBAGENT_CANCELLED);
            default -> end;
        };
    }

    // for the tests of the view

    Span getEmptyLabel() {
        return emptyLabel;
    }

    ComboBox<SubThread> getPicker() {
        return picker;
    }

    Div getInfo() {
        return info;
    }

    VerticalLayout getTranscriptBox() {
        return transcriptBox;
    }
}
