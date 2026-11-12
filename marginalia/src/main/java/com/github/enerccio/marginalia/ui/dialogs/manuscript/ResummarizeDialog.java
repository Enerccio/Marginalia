package com.github.enerccio.marginalia.ui.dialogs.manuscript;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.components.SummaryNode;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.NotificationService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.DesktopNotifier;
import com.github.enerccio.marginalia.ui.widgets.FloatingWindow;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

/**
 * Writes all the summaries of the active branch again with the model ({@link SummaryService#resummarize}), for when the
 * summary prompts changed and the summaries should follow them. Shows the summary being written; the texts are stored
 * after the last one, so a stop or an error changes nothing. {@link #addMenuItem} puts it into the menu bar of the
 * summaries overview (the desktop and the phone one).
 */
@Configurable
public class ResummarizeDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private SummaryService summaryService;

    private final Manuscript manuscript;
    private final Runnable onClosed;
    private final UI ui = UI.getCurrent();

    private final Span progress = new Span();
    private final Markdown reasoningMarkdown = new Markdown();
    private final Details reasoningDetails = new Details();
    private final TextArea summaryArea = new TextArea();
    private final Button actionButton = new Button();
    private final Button closeButton = new Button();
    private FloatingWindow floating;

    private CancellationToken cancellationToken;
    private boolean running;

    private ResummarizeDialog(Manuscript manuscript, Runnable onClosed) {
        this.manuscript = manuscript;
        this.onClosed = onClosed;
    }

    /**
     * Adds <i>Resummarize all summaries</i>: it asks first, the dialog with the progress opens when the user agrees.
     *
     * @param onDone runs when the dialog is closed, the summaries may have changed
     */
    public static void addMenuItem(MenuBar bar, Manuscript manuscript, Localization loc, SummaryService summaryService,
                                   Runnable onDone) {
        bar.addItem(loc.getValue(L.LABEL_RESUMMARIZE_ALL_SUMMARIES), _ -> {
            try {
                List<SummaryNode> roots = summaryService.collectTree(manuscript);
                int count = roots.stream().mapToInt(SummaryNode::countAll).sum();
                if (count == 0) {
                    Notification.warning(loc.getValue(L.MSG_RESUMMARIZE_NO_SUMMARIES));
                    return;
                }
                ConfirmDialog.show(String.format(loc.getValue(L.MSG_RESUMMARIZE_CONFIRM), count), () -> {
                    ResummarizeDialog dialog = new ResummarizeDialog(manuscript, onDone);
                    dialog.create();
                    dialog.open();
                    dialog.start();
                });
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
    }

    private void create() {
        setHeaderTitle(loc.getValue(L.LABEL_RESUMMARIZE_ALL_SUMMARIES));
        setWidth("700px");
        setHeight("500px");
        setMaxWidth("100vw");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        floating = new FloatingWindow(this, "summaries", this::getHeaderTitle, this::stopRunning).book(manuscript);

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
        // not when it is only folded into the corner
        floating.addEndListener(onClosed);
        setRunning(true);
        UIUtils.fitPhone(this, true);
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

    private void start() {
        SummaryService.ResummaryCallback callback = new SummaryService.ResummaryCallback() {
            @Override
            public void onStep(int number, int total, ChatMessage part, boolean meta) {
                ui.access(() -> {
                    String text = String.format(loc.getValue(L.MSG_RESUMMARIZE_PROGRESS), number, total);
                    progress.setText(meta ? text + " - " + loc.getValue(L.LABEL_META_SUMMARY) : text);
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
                    Notification.success(String.format(loc.getValue(L.MSG_RESUMMARIZE_FINISHED), count));
                    new DesktopNotifier(ui).finished(NotificationService.RESUMMARIZE, false, manuscript.getName());
                    floating.closeForGood();
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onTerminated() {
                ui.access(() -> {
                    setRunning(false);
                    progress.setText(loc.getValue(L.MSG_RESUMMARIZE_STOPPED));
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onError(Throwable throwable) {
                ui.access(() -> {
                    setRunning(false);
                    progress.setText(loc.getValue(L.LABEL_SUMMARY_ERROR));
                    UIUtils.inferenceError(loc, throwable);
                    new DesktopNotifier(ui).failed(NotificationService.RESUMMARIZE, false, manuscript.getName());
                    UIPushGuard.push(ui);
                });
            }
        };

        try {
            cancellationToken = summaryService.resummarize(manuscript, callback);
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
}
