package com.github.enerccio.marginalia.extensions.prompthistory;

import com.github.enerccio.marginalia.domain.service.InferenceListener.FunctionCall;
import com.github.enerccio.marginalia.domain.service.InferenceListener.Message;
import com.github.enerccio.marginalia.domain.service.InferenceListener.Outcome;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

import static com.github.enerccio.marginalia.extensions.prompthistory.PromptHistoryLocalization.*;

/**
 * The requests of a session, newest first: a list with the date, the model and the type of each, and under it what was
 * sent, what the model thought and what it answered for the one selected. A request that still runs is in the list and
 * its answer grows as the model writes it. The requests are unpacked here, when they are shown.
 */
class PromptHistoryDialog extends Dialog {

    private final HistoryStore store;
    private final Grid<HistoryEntry> grid = new Grid<>();
    private final TextArea request = readOnlyText();
    private final TextArea reasoning = readOnlyText();
    private final TextArea response = readOnlyText();
    private final Runnable changeListener = () -> UIUtils.accessComponent(this, this::reload);

    // the request text of the entry shown: it does not change and unpacking it for every piece of an answer would be waste
    private HistoryEntry requestShownFor;

    PromptHistoryDialog(HistoryStore store) {
        this.store = store;
        setHeaderTitle(T.localized(PROMPT_HISTORY));
        setModal(true);
        setDraggable(true);
        setResizable(true);
        setWidth("min(70rem, 92vw)");

        DateTimeFormatter dates = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withZone(ZoneId.systemDefault());
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        grid.addColumn(entry -> dates.format(entry.startedAt())).setHeader(T.localized(DATE)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(PromptHistoryDialog::modelName).setHeader(T.localized(MODEL)).setFlexGrow(1);
        grid.addColumn(entry -> kindName(entry.kind())).setHeader(T.localized(REQUEST_TYPE)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(entry -> statusName(entry.outcome())).setHeader(T.localized(STATUS)).setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(entry -> entry.running() ? "" : duration(entry.millis())).setHeader(T.localized(DURATION)).setAutoWidth(true).setFlexGrow(0);
        grid.setHeight("14rem");
        grid.setEmptyStateText(T.localized(NO_REQUESTS_YET));
        grid.addSelectionListener(event -> show(event.getFirstSelectedItem().orElse(null)));

        TabSheet tabs = new TabSheet();
        tabs.setWidthFull();
        tabs.add(T.localized(REQUEST), request);
        tabs.add(T.localized(REASONING), reasoning);
        tabs.add(T.localized(RESPONSE), response);
        // the answer is what one comes for after picking a request
        tabs.setSelectedIndex(2);

        VerticalLayout content = new VerticalLayout(grid, tabs);
        content.setPadding(false);
        add(content);

        getFooter().add(new Button(T.localized(REFRESH), event -> reload()),
                new Button(T.localized(CLEAR), event -> store.clear()),
                new Button(T.localized(CLOSE), event -> close()));

        // told when a request starts, writes or ends, from the thread of the request
        addAttachListener(event -> {
            store.addListener(changeListener);
            reload();
        });
        addDetachListener(event -> store.removeListener(changeListener));
    }

    private static TextArea readOnlyText() {
        TextArea text = new TextArea();
        text.setReadOnly(true);
        text.setWidthFull();
        text.setHeight("40vh");
        return text;
    }

    private void reload() {
        HistoryEntry selected = grid.getSelectedItems().stream().findFirst().orElse(null);
        List<HistoryEntry> entries = store.entries();
        grid.setItems(entries);
        if (selected == null || !entries.contains(selected)) {
            selected = entries.isEmpty() ? null : entries.get(0);
        }
        if (selected != null) {
            grid.select(selected);
        }
        // the selection may be the same as before and report no change, the answer may still have grown
        show(selected);
    }

    private void show(HistoryEntry entry) {
        if (entry == null) {
            requestShownFor = null;
            request.clear();
            reasoning.clear();
            response.clear();
            return;
        }
        if (requestShownFor != entry) {
            request.setValue(formatRequest(entry.messages()));
            requestShownFor = entry;
        }
        HistoryEntry.Answer answer = entry.answer();
        setIfChanged(reasoning, answer.reasoning() == null ? "" : answer.reasoning());
        setIfChanged(response, formatAnswer(answer));
    }

    private static void setIfChanged(TextArea area, String value) {
        if (!value.equals(area.getValue())) {
            area.setValue(value);
        }
    }

    static String formatRequest(List<Message> messages) {
        StringBuilder text = new StringBuilder();
        for (Message message : messages) {
            if (!text.isEmpty()) {
                text.append("\n\n");
            }
            text.append('[').append(message.role()).append("]\n");
            if (message.content() != null) {
                text.append(message.content().strip());
            }
            if (message.attachments() > 0) {
                text.append('\n').append(T.localized(ATTACHMENTS).formatted(message.attachments()));
            }
            if (message.calls() != null) {
                for (FunctionCall call : message.calls()) {
                    text.append("\n-> ").append(call.name()).append(' ').append(call.arguments());
                }
            }
        }
        return text.toString();
    }

    static String formatAnswer(HistoryEntry.Answer answer) {
        StringBuilder text = new StringBuilder(answer.text() == null ? "" : answer.text());
        if (!answer.calls().isEmpty()) {
            text.append("\n\n[").append(T.localized(FUNCTION_CALLS)).append("]");
            for (FunctionCall call : answer.calls()) {
                text.append("\n-> ").append(call.name()).append(' ').append(call.arguments());
                if (call.result() == null) {
                    text.append("\n   (").append(T.localized(NOT_RUN)).append(')');
                } else {
                    text.append("\n   ").append(call.failed() ? "(" + T.localized(FAILED_CALL) + ") " : "").append(call.result());
                }
            }
        }
        if (answer.error() != null) {
            text.append("\n\n").append(T.localized(ERROR).formatted(answer.error()));
        }
        return text.toString();
    }

    static String modelName(HistoryEntry entry) {
        String ai = entry.aiName();
        String model = entry.model();
        if (model == null || model.isBlank() || model.equals(ai)) {
            return ai == null ? "" : ai;
        }
        return ai == null || ai.isBlank() ? model : ai + " (" + model + ")";
    }

    static String kindName(String kind) {
        return kind == null || kind.isBlank() ? T.localized(OTHER) : kind;
    }

    static String statusName(Outcome outcome) {
        if (outcome == null) {
            return T.localized(RUNNING);
        }
        return switch (outcome) {
            case COMPLETED -> T.localized(COMPLETED);
            case CANCELLED -> T.localized(CANCELLED);
            case FAILED -> T.localized(FAILED);
        };
    }

    static String duration(long millis) {
        return millis < 1000 ? millis + " ms" : "%.1f s".formatted(millis / 1000.0);
    }
}
