package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.components.PromptComparison;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.MessagePrompt;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.MessagePromptService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Compares the prompt of a part with the prompt of an earlier one (by default the nearest one before it on the branch)
 * to find out why a server's prompt cache was not reused: a cache covers the new prompt only up to the first
 * difference, whatever follows is read again. Shows the messages of both prompts with the first one that differs
 * marked, how much of the new prompt is the same from the start, and the texts at the difference.
 */
@Configurable
public class PromptComparisonDialog extends Dialog {

    /** Characters of context shown in front of the first difference. */
    private static final int CONTEXT_CHARS = 200;
    /** Longest text shown for one message. */
    private static final int MAX_SHOWN_CHARS = 20000;

    @Autowired
    private Localization loc;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private MessagePromptService messagePromptService;

    private final ChatMessage part;

    private PromptComparison comparison;
    private Div verdict;
    private Div shared;
    private Grid<PromptComparison.Row> grid;
    private TextArea cachedText;
    private TextArea currentText;

    /**
     * An earlier prompt to compare with.
     *
     * @param message     the part it belongs to
     * @param prompt      its stored built prompt
     * @param alternative the part is an alternative (swipe) of the compared one, not a part before it
     */
    private record Candidate(ChatMessage message, String prompt, boolean alternative) {
    }

    /**
     * @param part the part whose prompt is the new one
     */
    public PromptComparisonDialog(ChatMessage part) {
        this.part = part;
    }

    public void create() throws Exception {
        setHeaderTitle(loc.getValue(L.LABEL_COMPARE_PROMPT));
        setWidth("1100px");
        setHeight("800px");
        setResizable(true);
        setDraggable(true);

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(true);

        String currentPrompt = messagePromptService.getBuiltPrompt(part);
        List<Candidate> candidates = currentPrompt == null ? List.of() : candidates();
        if (currentPrompt == null) {
            content.add(new Div(loc.getValue(L.MSG_PROMPT_COMPARE_NO_PROMPT)));
        } else if (candidates.isEmpty()) {
            content.add(new Div(loc.getValue(L.MSG_PROMPT_COMPARE_NO_CANDIDATE)));
        } else {
            buildContent(content, currentPrompt, candidates);
        }
        add(content);

        Button closeButton = new Button(loc.getValue(L.LABEL_CLOSE), event -> close());
        closeButton.setThemeName("primary");
        getFooter().add(closeButton);
    }

    /**
     * The parts before this one on its branch (the nearest first), then its alternatives, that have a stored prompt.
     */
    private List<Candidate> candidates() throws Exception {
        List<ChatMessage> before = new ArrayList<>();
        List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(part);
        for (int i = branch.size() - 1; i >= 0; i--) {
            ChatMessage message = branch.get(i);
            if (!message.getId().equals(part.getId())) {
                before.add(message);
            }
        }
        List<ChatMessage> alternatives = new ArrayList<>();
        for (ChatMessage swipe : chatMessageService.getSwipesForMessage(part)) {
            if (!swipe.getId().equals(part.getId())) {
                alternatives.add(swipe);
            }
        }
        List<ChatMessage> all = new ArrayList<>(before);
        all.addAll(alternatives);
        Map<Long, MessagePrompt> prompts = messagePromptService.findAllFor(all);

        List<Candidate> candidates = new ArrayList<>();
        for (ChatMessage message : before) {
            addCandidate(candidates, prompts, message, false);
        }
        for (ChatMessage message : alternatives) {
            addCandidate(candidates, prompts, message, true);
        }
        return candidates;
    }

    private static void addCandidate(List<Candidate> candidates, Map<Long, MessagePrompt> prompts, ChatMessage message,
                                     boolean alternative) {
        MessagePrompt prompt = prompts.get(message.getId());
        if (prompt != null && StringUtils.isNotBlank(prompt.getBuiltPrompt())) {
            candidates.add(new Candidate(message, prompt.getBuiltPrompt(), alternative));
        }
    }

    private String candidateLabel(Candidate candidate) {
        String text = StringUtils.abbreviate(StringUtils.normalizeSpace(StringUtils.defaultString(candidate.message().getResponse())), 70);
        return (candidate.alternative() ? loc.getValue(L.LABEL_COMPARE_ALTERNATIVE) + ": " : "") + text;
    }

    private void buildContent(VerticalLayout content, String currentPrompt, List<Candidate> candidates) {
        ComboBox<Candidate> with = new ComboBox<>(loc.getValue(L.LABEL_COMPARE_WITH));
        with.setItems(candidates);
        with.setItemLabelGenerator(this::candidateLabel);
        with.setAllowCustomValue(false);
        with.setWidthFull();

        verdict = new Div();
        shared = new Div();
        shared.addClassName(CssStyles.TOKEN_USAGE_SUMMARY);

        grid = new Grid<>(PromptComparison.Row.class, false);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("230px");
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addColumn(row -> row.index() + 1).setHeader("#").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(row -> roleName(row)).setHeader(loc.getValue(L.LABEL_ROLE)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(row -> statusName(row.status())).setHeader(loc.getValue(L.LABEL_STATUS)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(row -> row.cached() == null ? "" : row.cached().content().length())
                .setHeader(loc.getValue(L.LABEL_COMPARE_EARLIER_PROMPT) + " (" + loc.getValue(L.LABEL_COMPARE_CHARACTERS) + ")");
        grid.addColumn(row -> row.current() == null ? "" : row.current().content().length())
                .setHeader(loc.getValue(L.LABEL_COMPARE_THIS_PROMPT) + " (" + loc.getValue(L.LABEL_COMPARE_CHARACTERS) + ")");
        grid.addColumn(row -> row.cacheKept() ? "" : loc.getValue(L.LABEL_COMPARE_READ_AGAIN))
                .setHeader(loc.getValue(L.LABEL_COMPARE_CACHE));
        grid.setPartNameGenerator(row -> {
            if (comparison == null || row.cacheKept()) {
                return null;
            }
            return row.index() == comparison.getFirstDifference() ? CssStyles.PROMPT_COMPARE_FIRST_DIFFERENCE : CssStyles.PROMPT_COMPARE_LOST;
        });
        grid.addSelectionListener(event -> showTexts(event.getFirstSelectedItem().orElse(null)));

        cachedText = textArea(loc.getValue(L.LABEL_COMPARE_EARLIER_PROMPT));
        currentText = textArea(loc.getValue(L.LABEL_COMPARE_THIS_PROMPT));
        HorizontalLayout texts = new HorizontalLayout(cachedText, currentText);
        texts.setWidthFull();
        texts.setPadding(false);
        texts.setSpacing(true);

        content.add(with, verdict, shared, grid, texts);
        content.setFlexGrow(1, texts);

        with.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                compare(event.getValue(), currentPrompt);
            }
        });
        with.setValue(candidates.get(0));
    }

    private TextArea textArea(String label) {
        TextArea area = new TextArea(label);
        area.setReadOnly(true);
        area.setSizeFull();
        return area;
    }

    private void compare(Candidate candidate, String currentPrompt) {
        comparison = PromptComparison.compare(candidate.prompt(), currentPrompt);
        if (comparison == null) {
            verdict.setText(loc.getValue(L.MSG_PROMPT_COMPARE_UNREADABLE));
            shared.setText("");
            grid.setItems(List.of());
            showTexts(null);
            return;
        }
        verdict.setText(loc.getValue(verdictKey(comparison.getVerdict())));

        String position = comparison.getFirstDifference() < 0
                ? loc.getValue(L.MSG_PROMPT_COMPARE_NO_DIFFERENCE)
                : String.format(loc.getValue(L.MSG_PROMPT_COMPARE_FIRST_DIFFERENCE), comparison.getFirstDifference() + 1,
                comparison.getFirstDifferenceChar());
        long tokens = part.getBuiltPromptTokens() == null ? 0 : Math.round(comparison.getSharedShare() * part.getBuiltPromptTokens());
        shared.setText(position + " " + String.format(loc.getValue(L.MSG_PROMPT_COMPARE_SHARED),
                Math.round(comparison.getSharedShare() * 100), comparison.getSharedChars(), comparison.getCurrentChars(),
                tokens, part.getBuiltPromptTokens() == null ? 0 : part.getBuiltPromptTokens()));

        grid.setItems(comparison.getRows());
        int first = Math.max(0, comparison.getFirstDifference());
        if (first < comparison.getRows().size()) {
            grid.select(comparison.getRows().get(first));
        } else {
            showTexts(null);
        }
    }

    private void showTexts(PromptComparison.Row row) {
        if (row == null) {
            cachedText.setValue("");
            currentText.setValue("");
            return;
        }
        // a changed message is shown from just before the difference, the start of it is the same on both sides
        int from = row.status() == PromptComparison.Status.CHANGED ? Math.max(0, row.commonChars() - CONTEXT_CHARS) : 0;
        cachedText.setValue(excerpt(row.cached(), from));
        currentText.setValue(excerpt(row.current(), from));
    }

    private static String excerpt(PromptComparison.Message message, int from) {
        if (message == null) {
            return "";
        }
        String content = message.content();
        int to = Math.min(content.length(), from + MAX_SHOWN_CHARS);
        return (from > 0 ? "…" : "") + content.substring(from, to) + (to < content.length() ? "…" : "");
    }

    private String roleName(PromptComparison.Row row) {
        PromptComparison.Message message = row.current() != null ? row.current() : row.cached();
        String role = message == null ? null : message.role();
        if (role == null) {
            return "";
        }
        return switch (role) {
            case "SYSTEM" -> loc.getValue(L.LABEL_SYSTEM);
            case "USER" -> loc.getValue(L.LABEL_USER);
            case "ASSISTANT" -> loc.getValue(L.LABEL_ASSISTANT);
            default -> role;
        };
    }

    private String statusName(PromptComparison.Status status) {
        return loc.getValue(switch (status) {
            case SAME -> L.ENUM_PART_CHANGE_SAME;
            case CHANGED -> L.ENUM_MERGE_CHANGE_CHANGED;
            case ADDED -> L.ENUM_MERGE_CHANGE_ADDED;
            case REMOVED -> L.ENUM_MERGE_CHANGE_DELETED;
        });
    }

    private static L verdictKey(PromptComparison.Verdict verdict) {
        return switch (verdict) {
            case IDENTICAL -> L.MSG_PROMPT_COMPARE_IDENTICAL;
            case EXTENDED -> L.MSG_PROMPT_COMPARE_EXTENDED;
            case SHORTENED -> L.MSG_PROMPT_COMPARE_SHORTENED;
            case LAST_MESSAGE -> L.MSG_PROMPT_COMPARE_LAST_MESSAGE;
            case SYSTEM_CHANGED -> L.MSG_PROMPT_COMPARE_SYSTEM_CHANGED;
            case HISTORY_SHIFTED -> L.MSG_PROMPT_COMPARE_HISTORY_SHIFTED;
            case HISTORY_CHANGED -> L.MSG_PROMPT_COMPARE_HISTORY_CHANGED;
            case ROLE_CHANGED -> L.MSG_PROMPT_COMPARE_ROLE_CHANGED;
        };
    }
}
