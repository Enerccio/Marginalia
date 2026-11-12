package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.components.SummaryPlanner;
import com.github.enerccio.marginalia.domain.components.SummaryPlanner.Mode;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.*;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Writes summaries for the story after the newest summary, a block after the other ({@link SummaryService#createSummaries}): the
 * author says where a block ends (after a number of tokens, before a chapter, after a number of parts) and how far to go, sees how
 * many summaries that makes, and then follows them being written. The summaries that exist are not touched; each one is stored when
 * it is written, so a stop leaves those before it. Opened from the summaries overview, the settings menu of the story and after an
 * import.
 */
@Configurable
public class CreateSummariesDialog extends Dialog {

    private static final int DEFAULT_TOKENS = 8000;
    private static final int DEFAULT_PARTS = 5;
    private static final int DEFAULT_MIN_TOKENS = 2000;

    @Autowired
    private Localization loc;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private ChatMessageService chatMessageService;

    private final Manuscript manuscript;
    private final Runnable onClosed;
    private final UI ui = UI.getCurrent();
    // the parts without a summary, the oldest first, and the number of the first one in the story
    private List<ChatMessage> parts = List.of();
    private int firstNumber = 1;

    private final Select<Mode> modeSelect = new Select<>();
    private final IntegerField amountField = new IntegerField();
    private final IntegerField minTokensField = new IntegerField();
    private final Span chaptersHelp = new Span();
    private final ComboBox<Integer> upTo = new ComboBox<>();
    private final Checkbox rest = new Checkbox();
    private final Span preview = new Span();
    private final Button startButton = new Button();

    private final Span progress = new Span();
    private final Markdown reasoningMarkdown = new Markdown();
    private final Details reasoningDetails = new Details();
    private final TextArea summaryArea = new TextArea();
    private final Button actionButton = new Button();
    private final Button closeButton = new Button();
    private FloatingWindow floating;

    private List<ChatMessage> planned = List.of();
    private CancellationToken cancellationToken;
    private boolean running;

    /**
     * @param onClosed runs when the dialog is closed, the story may have summaries it did not have
     */
    public CreateSummariesDialog(Manuscript manuscript, Runnable onClosed) {
        this.manuscript = manuscript;
        this.onClosed = onClosed;
    }

    /**
     * Opens the dialog; says so instead when there is nothing to summarize.
     */
    public void show() {
        try {
            parts = summaryService.unsummarizedParts(manuscript);
            if (parts.isEmpty()) {
                Notification.warning(loc.getValue(L.MSG_NO_PARTS_WITHOUT_SUMMARY));
                return;
            }
            int total = chatMessageService.getBranchFromLeaf(manuscript.getActiveLeaf()).size();
            firstNumber = total - parts.size() + 1;
            build();
            open();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void build() {
        setHeaderTitle(loc.getValue(L.LABEL_CREATE_SUMMARIES));
        setWidth("560px");
        setMaxWidth("100vw");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        floating = new FloatingWindow(this, "summaries", this::getHeaderTitle, this::stopRunning).book(manuscript);

        Span help = new Span(loc.getValue(L.HELP_CREATE_SUMMARIES));
        help.addClassName(CssStyles.HINT_TEXT);

        modeSelect.setLabel(loc.getValue(L.LABEL_SUMMARIES_CUT_BY));
        modeSelect.setItems(Mode.values());
        modeSelect.setItemLabelGenerator(mode -> loc.getValue(switch (mode) {
            case TOKENS -> L.ENUM_SUMMARY_SPLIT_TOKENS;
            case CHAPTERS -> L.ENUM_SUMMARY_SPLIT_CHAPTERS;
            case PARTS -> L.ENUM_SUMMARY_SPLIT_PARTS;
        }));
        modeSelect.setWidthFull();

        amountField.setMin(1);
        amountField.setStepButtonsVisible(true);
        amountField.setWidthFull();

        minTokensField.setLabel(loc.getValue(L.LABEL_MIN_TOKENS_PER_SUMMARY));
        minTokensField.setMin(0);
        minTokensField.setStep(500);
        minTokensField.setStepButtonsVisible(true);
        minTokensField.setValue(DEFAULT_MIN_TOKENS);
        minTokensField.setWidthFull();
        chaptersHelp.setText(loc.getValue(L.HELP_SUMMARY_CHAPTERS));
        chaptersHelp.addClassName(CssStyles.HINT_TEXT);

        upTo.setLabel(loc.getValue(L.LABEL_UP_TO_PART));
        upTo.setPlaceholder(loc.getValue(L.LABEL_AT_THE_END_OF_THE_STORY));
        upTo.setItems(IntStream.range(0, parts.size()).boxed().toList());
        upTo.setItemLabelGenerator(this::partLabel);
        upTo.setClearButtonVisible(true);
        upTo.setAllowCustomValue(false);
        upTo.setWidthFull();

        rest.setLabel(loc.getValue(L.LABEL_ALSO_SUMMARIZE_THE_REST));
        rest.setValue(true);

        preview.addClassName(CssStyles.TEXT_BOLD);

        modeSelect.addValueChangeListener(_ -> modeChanged());
        amountField.addValueChangeListener(_ -> refreshPreview());
        minTokensField.addValueChangeListener(_ -> refreshPreview());
        upTo.addValueChangeListener(_ -> refreshPreview());
        rest.addValueChangeListener(_ -> refreshPreview());

        VerticalLayout form = new VerticalLayout(help, modeSelect, amountField, minTokensField, chaptersHelp, upTo, rest, preview);
        form.setPadding(false);
        form.setSpacing(true);
        add(form);

        Button cancel = new Button(loc.getValue(L.LABEL_CANCEL), _ -> close());
        startButton.setText(loc.getValue(L.LABEL_START));
        startButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        startButton.addClickListener(_ -> start());
        getFooter().add(cancel, startButton);
        DialogShortcuts.submitWith(startButton, amountField, minTokensField);
        DialogFocus.onOpen(this, amountField);

        // not when it is only folded into the corner
        floating.addEndListener(onClosed);
        modeSelect.setValue(Mode.TOKENS);
        UIUtils.fitPhone(this, true);
    }

    private String partLabel(Integer index) {
        String number = String.format(loc.getValue(L.LABEL_ANNOTATION_PART_NUMBER), firstNumber + index);
        String text = StringUtils.abbreviate(StringUtils.normalizeSpace(parts.get(index).getResponse()), 60);
        return text.isEmpty() ? number : number + ": " + text;
    }

    private void modeChanged() {
        Mode mode = modeSelect.getValue();
        boolean chapters = mode == Mode.CHAPTERS;
        amountField.setVisible(!chapters);
        minTokensField.setVisible(chapters);
        chaptersHelp.setVisible(chapters);
        if (mode == Mode.TOKENS) {
            amountField.setLabel(loc.getValue(L.LABEL_TOKENS_PER_SUMMARY));
            amountField.setStep(500);
            amountField.setValue(DEFAULT_TOKENS);
        } else if (mode == Mode.PARTS) {
            amountField.setLabel(loc.getValue(L.LABEL_PARTS_PER_SUMMARY));
            amountField.setStep(1);
            amountField.setValue(DEFAULT_PARTS);
        }
        refreshPreview();
    }

    /**
     * The options as set in the form, null while they are not valid.
     */
    private SummaryPlanner.Options options() {
        Mode mode = modeSelect.getValue();
        if (mode == null) {
            return null;
        }
        if (mode == Mode.CHAPTERS) {
            Integer min = minTokensField.getValue();
            return min == null || min < 0 ? null : new SummaryPlanner.Options(mode, 0, min, rest.getValue());
        }
        Integer amount = amountField.getValue();
        return amount == null || amount < 1 ? null : new SummaryPlanner.Options(mode, amount, 0, rest.getValue());
    }

    private void refreshPreview() {
        planned = List.of();
        SummaryPlanner.Options options = options();
        if (options != null) {
            try {
                Integer end = upTo.getValue();
                planned = summaryService.planSummaries(manuscript, options, end == null ? null : parts.get(end));
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }
        if (planned.isEmpty()) {
            preview.setText(loc.getValue(L.MSG_NO_SUMMARIES_PLANNED));
        } else {
            int covered = parts.indexOf(planned.getLast()) + 1;
            long tokens = parts.subList(0, covered).stream().mapToLong(ChatMessage::getTokenCount).sum();
            preview.setText(String.format(loc.getValue(L.MSG_SUMMARIES_PLANNED), planned.size(), covered, tokens));
        }
        startButton.setEnabled(!planned.isEmpty());
    }

    private void start() {
        if (planned.isEmpty()) {
            return;
        }
        showProgress();
        SummaryService.BatchCallback callback = new SummaryService.BatchCallback() {
            @Override
            public void onStep(int number, int total, ChatMessage part) {
                ui.access(() -> {
                    progress.setText(String.format(loc.getValue(L.MSG_CREATE_SUMMARIES_PROGRESS), number, total));
                    reasoningMarkdown.setContent("");
                    reasoningDetails.setVisible(false);
                    summaryArea.setValue("");
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onSummaryProgress(String reasoning, String summary) {
                ui.access(() -> {
                    if (StringUtils.isNotBlank(reasoning)) {
                        reasoningMarkdown.setContent(reasoning);
                        reasoningDetails.setVisible(true);
                    }
                    if (StringUtils.isNotBlank(summary)) {
                        summaryArea.setValue(summary);
                    }
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onFinished(int count) {
                ui.access(() -> {
                    setRunning(false);
                    Notification.success(String.format(loc.getValue(L.MSG_CREATE_SUMMARIES_FINISHED), count));
                    floating.closeForGood();
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onTerminated(int done) {
                ui.access(() -> {
                    setRunning(false);
                    progress.setText(String.format(loc.getValue(L.MSG_CREATE_SUMMARIES_STOPPED), done));
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onError(Throwable throwable, int done) {
                ui.access(() -> {
                    setRunning(false);
                    progress.setText(String.format(loc.getValue(L.MSG_CREATE_SUMMARIES_FAILED), done));
                    UIUtils.inferenceError(loc, throwable);
                    UIPushGuard.push(ui);
                });
            }
        };

        try {
            cancellationToken = summaryService.createSummaries(manuscript, planned, callback);
            floating.follow("summaries");
            if (cancellationToken == null) {
                setRunning(false);
                progress.setText(loc.getValue(L.LABEL_SUMMARY_ERROR));
                Notification.error(loc.getValue(L.ERROR_SUMMARY_FAILED));
            }
        } catch (Exception e) {
            setRunning(false);
            progress.setText(loc.getValue(L.LABEL_SUMMARY_ERROR));
            UIUtils.inferenceError(loc, e);
        }
    }

    /**
     * Replaces the form with the summary being written, as the dialog of the rewrite of all summaries shows it.
     */
    private void showProgress() {
        removeAll();
        getFooter().removeAll();
        setWidth("700px");
        setHeight("500px");

        progress.addClassName(CssStyles.TEXT_BOLD);
        reasoningMarkdown.addClassName(CssStyles.CHAT_MESSAGE_MARKDOWN);
        reasoningDetails.setSummaryText(loc.getValue(L.LABEL_VIEW_REASONING));
        reasoningDetails.add(reasoningMarkdown);
        reasoningDetails.setWidthFull();
        reasoningDetails.setVisible(false);
        summaryArea.setSizeFull();
        summaryArea.setReadOnly(true);

        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.setSizeFull();
        VerticalLayout scrollContent = new VerticalLayout(reasoningDetails, summaryArea);
        scrollContent.setPadding(false);
        scrollContent.setSpacing(true);
        scrollContent.setWidthFull();
        scrollPanel.add(scrollContent);

        VerticalLayout layout = new VerticalLayout(progress, scrollPanel);
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setFlexGrow(1, scrollPanel);
        add(layout);

        actionButton.addClickListener(_ -> {
            if (running) {
                stopRunning();
                actionButton.setEnabled(false);
            } else {
                close();
            }
        });
        // while the model writes: folds the window into the corner, the summaries go on
        closeButton.setText(loc.getValue(L.LABEL_CLOSE));
        closeButton.addClickListener(_ -> close());
        getFooter().add(closeButton, actionButton);
        setRunning(true);
    }

    private void stopRunning() {
        if (running && cancellationToken != null && !cancellationToken.isCancelled()) {
            cancellationToken.cancel();
        }
    }

    /**
     * Closes the window, also when it is folded into the corner; closing it while the model writes folds it.
     */
    @Override
    public void close() {
        if (floating == null) {
            super.close();
            return;
        }
        super.close();
        floating.closed();
    }

    private void setRunning(boolean running) {
        this.running = running;
        if (floating != null) {
            floating.setRunning(running);
        }
        closeButton.setVisible(running);
        actionButton.setEnabled(true);
        if (running) {
            actionButton.setText(loc.getValue(L.LABEL_STOP));
            actionButton.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            actionButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        } else {
            actionButton.setText(loc.getValue(L.LABEL_EXIT));
            actionButton.removeThemeVariants(ButtonVariant.LUMO_ERROR);
            actionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        }
    }
}
