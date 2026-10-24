package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.RedraftService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.WordDiffView;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Lets the model write a part of the story again (the desktop and the phone story tab), or only a passage of it that the author
 * selected: the author says what should change, the redraft streams in, and only <b>Apply</b> replaces the text of the part (of the
 * passage). The model gets the part, the story around it and the data the part was written with, see {@link RedraftService}.
 */
@Configurable(preConstruction = true)
public class RedraftDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private RedraftService redraftService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private AnnotationService annotationService;

    private final Manuscript manuscript;
    private final ChatMessage part;
    // the passage of the part that is redrafted, null for the whole part
    private final RedraftService.Passage passage;
    // the text of the part when the dialog was opened, and of the passage in it
    private final String partText;
    private final String selected;
    private final Consumer<ChatMessage> onApplied;

    private final TextArea instructions = new TextArea();
    private final Checkbox withAnnotations = new Checkbox();
    // the open annotations the redraft was asked to deal with
    private final List<Annotation> addressed = new ArrayList<>();
    private final Button start = new Button();
    private final Details reasoningDetails;
    private final Markdown reasoningMarkdown = new Markdown();
    private final TextArea result = new TextArea();
    private final WordDiffView changes;
    private final Details changesDetails;
    private final Button apply = new Button();

    private CancellationToken cancellationToken;
    private boolean generating;
    private boolean hasRedraft;

    /**
     * @param onApplied gets the part with the redrafted text, after it was stored
     */
    public static void open(Manuscript manuscript, ChatMessage part, Consumer<ChatMessage> onApplied) {
        create(manuscript, part, null, onApplied).open();
    }

    /**
     * Redrafts the passage of the part only.
     *
     * @param passage the selected text as offsets in the text of the part
     */
    public static void open(Manuscript manuscript, ChatMessage part, RedraftService.Passage passage, Consumer<ChatMessage> onApplied) {
        create(manuscript, part, passage, onApplied).open();
    }

    /**
     * @return the dialog, not opened
     */
    static RedraftDialog create(Manuscript manuscript, ChatMessage part, Consumer<ChatMessage> onApplied) {
        return new RedraftDialog(manuscript, part, null, onApplied);
    }

    /**
     * @return the dialog for the passage of the part, not opened
     */
    static RedraftDialog create(Manuscript manuscript, ChatMessage part, RedraftService.Passage passage, Consumer<ChatMessage> onApplied) {
        return new RedraftDialog(manuscript, part, passage, onApplied);
    }

    private RedraftDialog(Manuscript manuscript, ChatMessage part, RedraftService.Passage passage, Consumer<ChatMessage> onApplied) {
        this.manuscript = manuscript;
        this.part = part;
        this.passage = passage;
        this.partText = StringUtils.defaultString(part.getResponse());
        this.selected = passage == null ? partText : partText.substring(passage.start(), passage.end());
        this.onApplied = onApplied;

        setHeaderTitle(loc.getValue(title()));
        setWidth("min(900px, 95vw)");
        setHeight("85vh");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.STRICT);

        Span info = new Span(loc.getValue(passage == null ? L.MSG_REDRAFT_INFO : L.MSG_REDRAFT_SELECTION_INFO));
        info.addClassName(CssStyles.HINT_TEXT);

        TextArea selection = new TextArea(loc.getValue(L.LABEL_REDRAFT_SELECTED_TEXT));
        selection.setValue(selected);
        selection.setReadOnly(true);
        selection.setWidthFull();
        selection.setMaxRows(6);

        instructions.setLabel(loc.getValue(L.LABEL_REDRAFT_INSTRUCTIONS));
        instructions.setPlaceholder(loc.getValue(L.MSG_REDRAFT_PLACEHOLDER));
        instructions.setWidthFull();
        instructions.setMinRows(3);
        instructions.setMaxRows(8);

        withAnnotations.setLabel(loc.getValue(passage == null ? L.LABEL_REDRAFT_WITH_ANNOTATIONS : L.LABEL_REDRAFT_WITH_ANNOTATIONS_SELECTION));
        withAnnotations.setVisible(false);
        try {
            boolean any = !openAnnotations().isEmpty();
            withAnnotations.setVisible(any);
            withAnnotations.setValue(any);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }

        start.addClickListener(_ -> startOrStop());
        showGenerating(false);

        reasoningMarkdown.addClassName(CssStyles.CHAT_MESSAGE_MARKDOWN);
        reasoningDetails = new Details(loc.getValue(L.LABEL_VIEW_REASONING), reasoningMarkdown);
        reasoningDetails.setWidthFull();
        reasoningDetails.setVisible(false);

        result.setLabel(loc.getValue(L.LABEL_REDRAFTED_TEXT));
        result.setWidthFull();
        result.setMinRows(8);
        result.setMaxRows(30);
        result.setVisible(false);
        // the author may change the text, or empty it
        result.addValueChangeListener(_ -> apply.setEnabled(!generating && StringUtils.isNotBlank(result.getValue())));

        changes = new WordDiffView(loc.getValue(L.MSG_MERGE_NO_DIFFERENCE));
        changesDetails = new Details(loc.getValue(L.LABEL_MERGE_DIFFERENCES), changes);
        changesDetails.setWidthFull();
        changesDetails.setVisible(false);

        VerticalLayout content = new VerticalLayout(info, instructions, withAnnotations, start, reasoningDetails, result, changesDetails);
        if (passage != null) {
            content.addComponentAtIndex(1, selection);
        }
        content.setPadding(false);
        content.setWidthFull();
        content.addClassName(CssStyles.GAP_S);
        add(content);

        apply.setText(loc.getValue(L.LABEL_APPLY));
        apply.setThemeName("primary");
        apply.setEnabled(false);
        apply.addClickListener(_ -> apply());
        Button close = new Button(loc.getValue(L.LABEL_CLOSE), _ -> close());
        getFooter().add(close, apply);

        addOpenedChangeListener(e -> {
            if (!e.isOpened() && generating && cancellationToken != null) {
                cancellationToken.cancel();
            }
        });
    }

    private L title() {
        return passage == null ? L.LABEL_REDRAFT : L.LABEL_REDRAFT_SELECTION;
    }

    /**
     * The open annotations the redraft can deal with: the ones of the part, of a passage only the ones that overlap it.
     */
    private List<Annotation> openAnnotations() throws Exception {
        return annotationService.listForPart(manuscript, part).stream()
                .filter(a -> a.getStatus() == AnnotationStatus.OPEN)
                .filter(a -> passage == null || passage.overlaps(a)).toList();
    }

    /**
     * The start button starts the redraft, and stops it while it runs.
     */
    private void showGenerating(boolean running) {
        generating = running;
        setHeaderTitle(loc.getValue(running ? L.LABEL_REDRAFTING : title()));
        if (running) {
            start.setText(loc.getValue(L.LABEL_STOP));
            start.setThemeName("error primary");
        } else {
            start.setText(loc.getValue(hasRedraft ? L.LABEL_REDRAFT_AGAIN : title()));
            start.setThemeName("primary");
        }
        instructions.setReadOnly(running);
        withAnnotations.setReadOnly(running);
        result.setReadOnly(running);
        apply.setEnabled(!running && StringUtils.isNotBlank(result.getValue()));
    }

    private void startOrStop() {
        if (generating) {
            if (cancellationToken != null && !cancellationToken.isCancelled()) {
                cancellationToken.cancel();
            }
            return;
        }

        UI ui = UI.getCurrent();
        RedraftService.Callback callback = new RedraftService.Callback() {
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
        changesDetails.setVisible(false);
        result.setVisible(true);
        showGenerating(true);
        try {
            addressed.clear();
            boolean give = withAnnotations.isVisible() && withAnnotations.getValue();
            if (give) {
                addressed.addAll(openAnnotations());
            }
            cancellationToken = redraftService.redraft(manuscript, part, instructions.getValue(), null,
                    com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions.defaults(), give, passage, callback);
            if (cancellationToken == null) {
                showGenerating(false);
                Notification.error(loc.getValue(L.ERROR_REDRAFT_FAILED));
            }
        } catch (RedraftService.ContextInsufficient e) {
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
     * The model is done or was stopped: what arrived stays in the text field, where it can be changed before it is applied.
     */
    private void finish(String reasoning, String text) {
        show(reasoning, text);
        hasRedraft = StringUtils.isNotBlank(text);
        if (hasRedraft) {
            changes.show(selected, text);
            changesDetails.setVisible(true);
        }
        showGenerating(false);
    }

    private void apply() {
        String text = result.getValue();
        if (StringUtils.isBlank(text)) {
            return;
        }
        try {
            if (passage != null) {
                // the text of the part is the one the passage was selected in
                String stored = StringUtils.defaultString(chatMessageService.find(part).getResponse());
                if (!stored.equals(partText)) {
                    Notification.error(loc.getValue(L.MSG_REDRAFT_SELECTION_CHANGED));
                    return;
                }
                text = replacePassage(partText, passage, text);
            }
            ChatMessageService.PartEdit edit = chatMessageService.updateResponse(manuscript, part, text);
            // the notes the redraft was asked to deal with are dealt with
            for (Annotation annotation : addressed) {
                annotationService.setStatus(manuscript, annotation, AnnotationStatus.RESOLVED);
            }
            if (edit.outdated().isEmpty()) {
                Notification.success(loc.getValue(L.MSG_REDRAFT_APPLIED));
            } else {
                Notification.warning(loc.getValue(L.MSG_REDRAFT_APPLIED) + " "
                        + String.format(loc.getValue(L.MSG_REDRAFT_OUTDATED_SUMMARIES), edit.outdated().size()));
            }
            close();
            onApplied.accept(edit.message());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * The text of the part with the passage replaced. The whitespace around the passage (a selection often takes the space or the line
     * break after a sentence) is kept, however the model started and ended its text.
     */
    static String replacePassage(String text, RedraftService.Passage passage, String replacement) {
        String old = text.substring(passage.start(), passage.end());
        String leading = old.substring(0, old.length() - old.stripLeading().length());
        String trailing = old.substring(old.stripTrailing().length());
        return text.substring(0, passage.start()) + leading + replacement.strip() + trailing + text.substring(passage.end());
    }
}
