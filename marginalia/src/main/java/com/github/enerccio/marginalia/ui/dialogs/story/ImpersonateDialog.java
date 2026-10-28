package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.ImpersonateService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.function.Consumer;

/**
 * Lets the model draft the instructions of the next turn in the place of the author (the new turn form of the desktop and the phone
 * story tab, and extensions that know what the next turn should be about): the author says what the turn should be about, the
 * instructions stream in, and only <b>Use as Instructions</b> hands them over. The model gets the story so far, the turn details
 * and, when the caller has one, the plan of the story, see {@link ImpersonateService}.
 */
@Configurable(preConstruction = true)
public class ImpersonateDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private ImpersonateService impersonateService;

    private final Manuscript manuscript;
    private final TurnInput turn;
    private final String outline;
    private final Consumer<String> onUse;

    private final TextArea guidance = new TextArea();
    private final Button start = new Button();
    private final Details reasoningDetails;
    private final Markdown reasoningMarkdown = new Markdown();
    private final TextArea result = new TextArea();
    private final Button use = new Button();

    private CancellationToken cancellationToken;
    private boolean generating;
    private boolean hasDraft;

    /**
     * @param turn    the turn details the author typed, the instructions in it are the first guidance; {@code null} for the details of
     *                the last part of the story and no guidance
     * @param outline the plan of the story the instructions should follow (the data of the current chapter, for example), shown to the
     *                author and given to the model; blank for none
     * @param onUse   gets the drafted instructions (as the author left them) when they are used
     */
    public static void open(Manuscript manuscript, TurnInput turn, String outline, Consumer<String> onUse) {
        create(manuscript, turn, outline, onUse).open();
    }

    /**
     * @return the dialog, not opened
     */
    static ImpersonateDialog create(Manuscript manuscript, TurnInput turn, String outline, Consumer<String> onUse) {
        return new ImpersonateDialog(manuscript, turn, outline, onUse);
    }

    private ImpersonateDialog(Manuscript manuscript, TurnInput turn, String outline, Consumer<String> onUse) {
        this.manuscript = manuscript;
        this.turn = turn;
        this.outline = StringUtils.trimToEmpty(outline);
        this.onUse = onUse;

        setHeaderTitle(loc.getValue(L.LABEL_IMPERSONATE));
        setWidth("min(900px, 95vw)");
        setHeight("85vh");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.STRICT);

        Span info = new Span(loc.getValue(L.MSG_IMPERSONATE_INFO));
        info.addClassName(CssStyles.HINT_TEXT);

        TextArea plan = new TextArea(loc.getValue(L.LABEL_IMPERSONATE_OUTLINE));
        plan.setValue(this.outline);
        plan.setReadOnly(true);
        plan.setWidthFull();
        plan.setMaxRows(8);
        plan.setVisible(!this.outline.isEmpty());

        guidance.setLabel(loc.getValue(L.LABEL_IMPERSONATE_GUIDANCE));
        guidance.setPlaceholder(loc.getValue(L.MSG_IMPERSONATE_PLACEHOLDER));
        guidance.setValue(turn == null ? "" : StringUtils.defaultString(turn.instructions()));
        guidance.setWidthFull();
        guidance.setMinRows(3);
        guidance.setMaxRows(8);

        start.addClickListener(_ -> startOrStop());
        showGenerating(false);

        reasoningMarkdown.addClassName(CssStyles.CHAT_MESSAGE_MARKDOWN);
        reasoningDetails = new Details(loc.getValue(L.LABEL_VIEW_REASONING), reasoningMarkdown);
        reasoningDetails.setWidthFull();
        reasoningDetails.setVisible(false);

        result.setLabel(loc.getValue(L.LABEL_IMPERSONATED_INSTRUCTIONS));
        result.setWidthFull();
        result.setMinRows(6);
        result.setMaxRows(24);
        result.setVisible(false);
        // the author may change the text, or empty it
        result.addValueChangeListener(_ -> use.setEnabled(!generating && StringUtils.isNotBlank(result.getValue())));

        VerticalLayout content = new VerticalLayout(info, plan, guidance, start, reasoningDetails, result);
        content.setPadding(false);
        content.setWidthFull();
        content.addClassName(CssStyles.GAP_S);
        add(content);

        use.setText(loc.getValue(L.LABEL_IMPERSONATE_USE));
        use.setThemeName("primary");
        use.setEnabled(false);
        use.addClickListener(_ -> use());
        Button close = new Button(loc.getValue(L.LABEL_CLOSE), _ -> close());
        getFooter().add(close, use);

        addOpenedChangeListener(e -> {
            if (!e.isOpened() && generating && cancellationToken != null) {
                cancellationToken.cancel();
            }
        });
    }

    /**
     * The start button starts the drafting, and stops it while it runs.
     */
    private void showGenerating(boolean running) {
        generating = running;
        setHeaderTitle(loc.getValue(running ? L.LABEL_IMPERSONATING : L.LABEL_IMPERSONATE));
        if (running) {
            start.setText(loc.getValue(L.LABEL_STOP));
            start.setThemeName("error primary");
        } else {
            start.setText(loc.getValue(hasDraft ? L.LABEL_IMPERSONATE_AGAIN : L.LABEL_IMPERSONATE));
            start.setThemeName("primary");
        }
        guidance.setReadOnly(running);
        result.setReadOnly(running);
        use.setEnabled(!running && StringUtils.isNotBlank(result.getValue()));
    }

    private void startOrStop() {
        if (generating) {
            if (cancellationToken != null && !cancellationToken.isCancelled()) {
                cancellationToken.cancel();
            }
            return;
        }

        UI ui = UI.getCurrent();
        ImpersonateService.Callback callback = new ImpersonateService.Callback() {
            @Override
            public void onProgress(String reasoning, String text) {
                ui.access(() -> {
                    show(reasoning, text);
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onFinished(String reasoning, String text) {
                ui.access(() -> {
                    finish(reasoning, text);
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onTerminated(String reasoning, String text) {
                ui.access(() -> {
                    finish(reasoning, text);
                    UIPushGuard.push(ui);
                });
            }

            @Override
            public void onError(Throwable throwable) {
                ui.access(() -> {
                    showGenerating(false);
                    UIUtils.inferenceError(loc, throwable);
                    UIPushGuard.push(ui);
                });
            }
        };

        result.setValue("");
        reasoningMarkdown.setContent("");
        reasoningDetails.setVisible(false);
        result.setVisible(true);
        showGenerating(true);
        try {
            ImpersonateService.Request request = ImpersonateService.Request.of(guidance.getValue()).withOutline(outline);
            if (turn != null) {
                request = request.withTurn(turn.sceneSetting(), turn.povCharacter(), turn.presentCharacters());
            }
            cancellationToken = impersonateService.impersonate(manuscript, request, callback);
            if (cancellationToken == null) {
                showGenerating(false);
                Notification.error(loc.getValue(L.ERROR_IMPERSONATE_FAILED));
            }
        } catch (ImpersonateService.ContextInsufficient e) {
            showGenerating(false);
            Notification.error(loc.getValue(L.ERROR_CONTEXT_INSUFFICIENT));
        } catch (Exception e) {
            showGenerating(false);
            UIUtils.internalServerError(loc, e);
        }
    }

    private void show(String reasoning, String text) {
        if (StringUtils.isNotBlank(reasoning)) {
            reasoningMarkdown.setContent(reasoning);
            reasoningDetails.setVisible(true);
        }
        result.setValue(StringUtils.defaultString(text));
    }

    /**
     * The model is done or was stopped: what arrived stays in the text field, where it can be changed before it is used.
     */
    private void finish(String reasoning, String text) {
        show(reasoning, text);
        hasDraft = StringUtils.isNotBlank(text);
        showGenerating(false);
    }

    private void use() {
        String text = result.getValue();
        if (StringUtils.isBlank(text)) {
            return;
        }
        close();
        onUse.accept(text.strip());
    }
}
