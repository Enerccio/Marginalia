package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.SharedStyles;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ModalityMode;
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

/**
 * Shows the summary of a part and generates it. Generates a meta summary when {@code metaTo} is given: it merges the
 * summaries from the part to {@code metaTo} (see {@link SummaryService#createMetaSummary}).
 */
@Configurable(preConstruction = true)
@Extendable
public class SummaryDialog extends Dialog {

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
        setWidth("700px");
        setHeight("500px");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.STRICT);

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(true);

        reasoningMarkdown = new Markdown();
        reasoningMarkdown.addClassName(SharedStyles.CHAT_MESSAGE_MARKDOWN);
        UIUtils.applyMarkdownStyles(reasoningMarkdown);

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
        updateActionButtonState(isGenerating);

        actionButton.addClickListener(_ -> {
            if (isGeneratingState) {
                if (cancellationToken != null && !cancellationToken.isCancelled()) {
                    cancellationToken.cancel();
                }
                updateActionButtonState(false);
            } else {
                close();
            }
        });

        footer.add(actionButton);
        getFooter().add(footer);

        if (!isGenerating) {
            loadExistingSummary();
        }
    }

    /**
     * Runs when the dialog is closed, whatever the summary ended as.
     */
    public void onClosed(Runnable action) {
        addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                action.run();
            }
        });
    }

    private void updateActionButtonState(boolean generating) {
        this.isGeneratingState = generating;
        if (generating) {
            actionButton.setText(loc.getValue(L.LABEL_STOP));
            actionButton.setThemeName("error primary");
        } else {
            actionButton.setText(loc.getValue(L.LABEL_EXIT));
            actionButton.setThemeName("primary");
        }
    }

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
                            dialogMessage.setSummary(summary);
                            chatMessageService.save(dialogMessage);
                        }
                        setHeaderTitle(loc.getValue(L.LABEL_SUMMARY));
                        updateActionButtonState(false);
                    } catch (Exception e) {
                        UIUtils.internalServerError(loc, e);
                    }
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
                    UIUtils.internalServerError(loc, throwable);
                    UIPushGuard.push(ui);
                });
            }
        };

        try {
            cancellationToken = metaTo == null
                    ? summaryService.createSummary(manuscript, dialogMessage, callback)
                    : summaryService.createMetaSummary(manuscript, dialogMessage, metaTo, callback);

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
