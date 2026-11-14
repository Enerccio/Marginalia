package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.NotificationService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.DesktopNotifier;
import com.github.enerccio.marginalia.ui.widgets.FloatingWindow;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * Shows the summary of a part and generates it. Generates a meta summary when {@code metaTo} is given: it merges the
 * summaries from the part to {@code metaTo} (see {@link SummaryService#createMetaSummary}).
 * <p>
 * Not modal ({@link FloatingWindow}): the author works on while the model writes. <i>Stop</i> stops the model, <i>Close</i> while
 * it writes folds the window into the corner of the page and the summary goes on; the summarized part is held
 * {@link Runs.Mode#READ} (not deleted or merged away) meanwhile.
 */
@Configurable(preConstruction = true)
@Extendable
public class SummaryDialog extends Dialog implements LLMInteract {

    @Autowired
    private Localization loc;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private ChatMessageService chatMessageService;

    private final Manuscript manuscript;
    private final ChatMessage dialogMessage;
    private final ChatMessage metaTo;

    private final Button actionButton;
    private final Button closeButton;
    private final FloatingWindow floating;
    private final Details reasoningDetails;
    private final Markdown reasoningMarkdown;
    private final TextArea summaryArea;

    private CancellationToken cancellationToken;
    private boolean isGeneratingState;

    public SummaryDialog(Manuscript manuscript, ChatMessage message, boolean isGenerating) {
        this(manuscript, message, null, isGenerating);
    }

    /**
     * @param metaTo the oldest part whose summary the meta summary merges, null for the summary of the part
     */
    public SummaryDialog(Manuscript manuscript, ChatMessage message, ChatMessage metaTo, boolean isGenerating) {
        this.manuscript = manuscript;
        this.dialogMessage = message;
        this.metaTo = metaTo;

        setHeaderTitle(isGenerating
                ? loc.getValue(metaTo == null ? L.LABEL_GENERATING_SUMMARY : L.LABEL_GENERATING_META_SUMMARY)
                : loc.getValue(L.LABEL_SUMMARY));
        setWidth("min(700px, 95vw)");
        setHeight("500px");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        floating = new FloatingWindow(this, NotificationService.SUMMARY,
                () -> getHeaderTitle() + " \u00b7 " + manuscript.getName(), this::stopGeneration).book(manuscript);
        if (isGenerating) {
            floating.holdParts(manuscript, metaTo == null ? List.of(message) : List.of(message, metaTo), Runs.Mode.READ);
        }

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(true);

        reasoningMarkdown = new Markdown();
        reasoningMarkdown.addClassName(CssStyles.CHAT_MESSAGE_MARKDOWN);

        reasoningDetails = new Details(loc.getValue(L.LABEL_VIEW_REASONING), reasoningMarkdown);
        reasoningDetails.setWidthFull();
        reasoningDetails.setVisible(false);

        summaryArea = new TextArea();
        summaryArea.setSizeFull();
        summaryArea.setReadOnly(true);

        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.setSizeFull();
        VerticalLayout scrollContent = new VerticalLayout(reasoningDetails, summaryArea);
        scrollContent.setPadding(false);
        scrollContent.setSpacing(true);
        scrollContent.setWidthFull();
        scrollPanel.add(scrollContent);

        layout.add(scrollPanel);
        layout.setFlexGrow(1, scrollPanel);
        add(layout);

        HorizontalLayout footer = new HorizontalLayout();
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        actionButton = new Button();
        // while the model writes: folds the window, the summary goes on
        closeButton = new Button(loc.getValue(L.LABEL_CLOSE), _ -> close());
        updateActionButtonState(isGenerating);

        actionButton.addClickListener(_ -> {
            if (isGeneratingState) {
                stopGeneration();
                updateActionButtonState(false);
            } else {
                close();
            }
        });

        footer.add(closeButton, actionButton);
        getFooter().add(footer);

        if (!isGenerating) {
            loadExistingSummary();
        }
    }

    /**
     * Runs when the dialog is closed, whatever the summary ended as.
     */
    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "action", type = Runnable.class)})
    public void onClosed(Runnable action) {
        // not when it is only folded into the corner while the model writes
        floating.addEndListener(action);
    }

    /**
     * Closes the window, also when it is folded into the corner; closing it while the model writes folds it.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void close() {
        super.close();
        floating.closed();
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class))
    private void stopGeneration() {
        if (cancellationToken != null && !cancellationToken.isCancelled()) {
            cancellationToken.cancel();
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "generating", type = boolean.class)})
    private void updateActionButtonState(boolean generating) {
        this.isGeneratingState = generating;
        floating.setRunning(generating);
        closeButton.setVisible(generating);
        if (generating) {
            actionButton.setText(loc.getValue(L.LABEL_STOP));
            actionButton.setThemeName("error primary");
        } else {
            actionButton.setText(loc.getValue(L.LABEL_EXIT));
            actionButton.setThemeName("primary");
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "summary", type = Summary.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void loadExistingSummary() {
        try {
            if (dialogMessage.getSummary() != null) {
                Summary summary = summaryService.find(dialogMessage.getSummary());
                if (summary != null) {
                    if (StringUtils.isNotBlank(summary.getReasoning())) {
                        reasoningMarkdown.setContent(summary.getReasoning());
                        reasoningDetails.setVisible(true);
                    } else {
                        reasoningDetails.setVisible(false);
                    }
                    summaryArea.setValue(StringUtils.defaultString(summary.getSummary()));
                }
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.1.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "ui", type = UI.class),
                    @APILocals(name = "callback", type = SummaryService.AsyncCallback.class),
                    @APILocals(name = "e", type = Exception.class)})
    public void startGeneration() {
        UI ui = UI.getCurrent();
        SummaryService.AsyncCallback callback = new SummaryService.AsyncCallback() {
            @Override
            public void onSummaryProgress(String reasoning, String summaryText) {
                ui.access(() -> {
                    if (StringUtils.isNotBlank(reasoning)) {
                        reasoningMarkdown.setContent(reasoning);
                        reasoningDetails.setVisible(true);
                    }
                    if (StringUtils.isNotBlank(summaryText)) {
                        summaryArea.setValue(summaryText);
                    }
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onSummaryFinished(Summary summary) {
                ui.access(() -> {
                    try {
                        if (metaTo == null) {
                            // the author may have changed the part meanwhile: only the summary is written
                            dialogMessage.setSummary(summary);
                            chatMessageService.update(dialogMessage, part -> part.setSummary(summary));
                        }
                        setHeaderTitle(loc.getValue(L.LABEL_SUMMARY));
                        updateActionButtonState(false);
                    } catch (Exception e) {
                        UIUtils.internalServerError(loc, e);
                    }
                    new DesktopNotifier(ui).finished(NotificationService.SUMMARY, false, manuscript.getName());
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onSummaryTerminated() {
                ui.access(() -> {
                    setHeaderTitle(loc.getValue(L.LABEL_SUMMARY_STOPPED));
                    updateActionButtonState(false);
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onError(Throwable throwable) {
                ui.access(() -> {
                    setHeaderTitle(loc.getValue(L.LABEL_SUMMARY_ERROR));
                    updateActionButtonState(false);
                    UIUtils.inferenceError(loc, throwable);
                    new DesktopNotifier(ui).failed(NotificationService.SUMMARY, false, manuscript.getName());
                    UIPushGuard.push(ui);
                });
            }
        };

        try {
            // the panel of the runs shows what the model writes and its place in the queue
            cancellationToken = floating.send(() -> metaTo == null
                    ? summaryService.createSummary(manuscript, dialogMessage, callback)
                    : summaryService.createMetaSummary(manuscript, dialogMessage, metaTo, callback));

            if (cancellationToken == null) {
                setHeaderTitle(loc.getValue(L.LABEL_SUMMARY_ERROR));
                updateActionButtonState(false);
                Notification.error(loc.getValue(L.ERROR_SUMMARY_FAILED));
            }
        } catch (Exception e) {
            setHeaderTitle(loc.getValue(L.LABEL_SUMMARY_ERROR));
            updateActionButtonState(false);
            UIUtils.internalServerError(loc, e);
        }
    }
}
