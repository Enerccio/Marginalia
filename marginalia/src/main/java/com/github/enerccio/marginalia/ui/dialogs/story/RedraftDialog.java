package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.github.enerccio.marginalia.ui.widgets.WordDiffView;
import com.github.enerccio.marginalia.utils.TextMarkup;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;
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
 * <p>
 * Opened by {@link #openAuto} (the automatic annotation of a generated part) it starts at once, with the open annotations of the part
 * for the author's instructions, and with {@code apply} it also applies the redraft when the model is done, without asking, and closes.
 * Stopping it or closing it before that applies nothing. The part was only just generated, so no summary describes it and the box to
 * revalidate summaries is not offered. The redrafted text (with its reasoning and differences) is then one tab and the original text with
 * the annotations marked in it another, so the author can compare what the model did with what it was asked to deal with, and stop it
 * or close the window if the redraft is not what is wanted.
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

    @Autowired
    private SummaryService summaryService;

    private final Manuscript manuscript;
    private final ChatMessage part;
    // the passage of the part that is redrafted, null for the whole part
    private final RedraftService.Passage passage;
    // the text of the part when the dialog was opened, and of the passage in it
    private final String partText;
    private final String selected;
    private final Consumer<ChatMessage> onApplied;
    // opened by the automatic annotation: the part is new, no summary is touched
    private final boolean automatic;
    // the redraft is applied as soon as the model is done
    private final boolean applyAutomatically;

    private final TextArea instructions = new TextArea();
    private final Checkbox withAnnotations = new Checkbox();
    // shown when a summary describes the part: the summaries the applied redraft makes outdated get their hash again
    private final Checkbox revalidateSummaries = new Checkbox();
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
    // the reasoning the model wrote the text of the field with, it goes to the history of the part
    private String redraftReasoning;

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
     * Opens the dialog on the part and starts the redraft at once, to address the open annotations of the part.
     *
     * @param apply     whether the redraft is applied without asking when the model is done, and the dialog closes
     * @param onApplied gets the part with the redrafted text, after it was stored (not called when the dialog was closed before)
     */
    static void openAuto(Manuscript manuscript, ChatMessage part, boolean apply, Consumer<ChatMessage> onApplied) {
        RedraftDialog dialog = createAuto(manuscript, part, apply, onApplied);
        dialog.open();
        dialog.startOrStop();
    }

    /**
     * @return the dialog of the automatic redraft, not opened and not started
     */
    static RedraftDialog createAuto(Manuscript manuscript, ChatMessage part, boolean apply, Consumer<ChatMessage> onApplied) {
        return new RedraftDialog(manuscript, part, null, onApplied, true, apply);
    }

    /**
     * @return the dialog, not opened
     */
    static RedraftDialog create(Manuscript manuscript, ChatMessage part, Consumer<ChatMessage> onApplied) {
        return new RedraftDialog(manuscript, part, null, onApplied, false, false);
    }

    /**
     * @return the dialog for the passage of the part, not opened
     */
    static RedraftDialog create(Manuscript manuscript, ChatMessage part, RedraftService.Passage passage, Consumer<ChatMessage> onApplied) {
        return new RedraftDialog(manuscript, part, passage, onApplied, false, false);
    }

    private RedraftDialog(Manuscript manuscript, ChatMessage part, RedraftService.Passage passage, Consumer<ChatMessage> onApplied,
                          boolean automatic, boolean applyAutomatically) {
        this.manuscript = manuscript;
        this.part = part;
        this.passage = passage;
        this.partText = StringUtils.defaultString(part.getResponse());
        this.selected = passage == null ? partText : partText.substring(passage.start(), passage.end());
        this.onApplied = onApplied;
        this.automatic = automatic;
        this.applyAutomatically = applyAutomatically;

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

        revalidateSummaries.setLabel(loc.getValue(L.LABEL_REDRAFT_REVALIDATE_SUMMARIES));
        revalidateSummaries.setVisible(false);
        if (!automatic) {
            try {
                revalidateSummaries.setVisible(chatMessageService.isSummarized(manuscript, part));
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }

        start.addClickListener(_ -> startOrStop());
        showGenerating(false);

        reasoningMarkdown.addClassName(CssStyles.CHAT_MESSAGE_MARKDOWN);
        reasoningDetails = new Details(loc.getValue(L.LABEL_VIEW_REASONING), reasoningMarkdown);
        reasoningDetails.setWidthFull();
        reasoningDetails.setVisible(false);

        result.setLabel(loc.getValue(L.LABEL_REDRAFTED_TEXT));
        result.setWidthFull();
        ResizableTextArea.install(loc, result, "300px");
        result.setValueChangeMode(ValueChangeMode.LAZY);
        result.setVisible(false);
        // the author may change the text, or empty it; the differences follow the text
        result.addValueChangeListener(_ -> {
            if (generating) {
                return;
            }
            boolean text = StringUtils.isNotBlank(result.getValue());
            apply.setEnabled(text);
            showChanges(text ? result.getValue() : null);
        });

        changes = new WordDiffView(loc.getValue(L.MSG_MERGE_NO_DIFFERENCE));
        changesDetails = new Details(loc.getValue(L.LABEL_MERGE_DIFFERENCES), changes);
        changesDetails.setWidthFull();
        changesDetails.setVisible(false);

        VerticalLayout content;
        if (automatic) {
            TabSheet tabs = new TabSheet();
            tabs.setWidthFull();
            VerticalLayout redrafted = new VerticalLayout(reasoningDetails, result, changesDetails);
            redrafted.setPadding(false);
            redrafted.addClassName(CssStyles.GAP_S);
            tabs.add(loc.getValue(L.LABEL_REDRAFTED_TEXT), redrafted);
            tabs.add(loc.getValue(L.LABEL_REDRAFT_ORIGINAL_TEXT), originalWithAnnotations());
            content = new VerticalLayout(info, instructions, withAnnotations, revalidateSummaries, start, tabs);
        } else {
            content = new VerticalLayout(info, instructions, withAnnotations, revalidateSummaries, start, reasoningDetails, result, changesDetails);
        }
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

    /**
     * The text of the part as it was when the dialog was opened with the passages of its open annotations marked as in the story, and
     * the notes under it.
     */
    private VerticalLayout originalWithAnnotations() {
        VerticalLayout original = new VerticalLayout();
        original.setPadding(false);
        original.addClassName(CssStyles.GAP_S);
        try {
            StoryAnnotations annotations = new StoryAnnotations(() -> manuscript, _ -> {
            });
            Markdown text = new Markdown(TextMarkup.apply(partText, annotations.marks(part, partText)));
            text.addClassName(CssStyles.CHAT_MESSAGE_MARKDOWN);
            original.add(text);
            List<Annotation> open = openAnnotations();
            if (!open.isEmpty()) {
                // a popover would open behind the modal window: the notes are here, a click on a mark brings its note into view and
                // the pointer over a mark or a note lights both
                VerticalLayout notes = new VerticalLayout();
                notes.setPadding(false);
                notes.setSpacing(false);
                notes.addClassNames(CssStyles.ANNOTATION_MARGIN, CssStyles.GAP_S);
                for (Annotation annotation : open) {
                    notes.add(note(annotation));
                }
                Div anchor = new Div();
                anchor.addClassName(CssStyles.ANNOTATION_ANCHOR);
                original.add(new Span(loc.getValue(L.LABEL_ANNOTATIONS)), notes, anchor);
                JavascriptLibrary.linkMarginNotes(text.getElement(), notes.getElement());
                JavascriptLibrary.fireAnnotationClickEvent(text.getElement(), anchor.getElement());
                text.getElement().addEventListener("annotation-click",
                        event -> JavascriptLibrary.flashMarginNote(notes.getElement(),
                                event.getEventData().get("event.detail.ids").asString().split(",")[0]))
                        .addEventData("event.detail.ids");
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        return original;
    }

    /**
     * The note of an annotation, read only: the words it is about and what it says.
     */
    private static Div note(Annotation annotation) {
        Div note = new Div();
        note.addClassName(CssStyles.ANNOTATION_VIEW);
        note.getElement().setAttribute(StoryAnnotations.NOTE_ATTRIBUTE, annotation.getUuid());
        if (StringUtils.isNotBlank(annotation.getQuote())) {
            Div quote = new Div("\u201c" + annotation.getQuote() + "\u201d");
            quote.addClassName(CssStyles.ANNOTATION_QUOTE);
            note.add(quote);
        }
        Div text = new Div(StringUtils.defaultString(annotation.getText()));
        text.addClassName(CssStyles.ANNOTATION_NOTE);
        note.add(text);
        return note;
    }

    // for the tests

    void completedForTest(String text) {
        completed(null, text);
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
        revalidateSummaries.setReadOnly(running);
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
                    completed(reasoning, text);
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
        redraftReasoning = null;
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
        redraftReasoning = StringUtils.defaultIfEmpty(reasoning, null);
        if (StringUtils.isNotBlank(reasoning)) {
            reasoningMarkdown.setContent(reasoning);
            reasoningDetails.setVisible(true);
        }
        result.setValue(StringUtils.defaultString(text));
    }

    /**
     * The model is done (not stopped): the redraft is applied at once when the dialog was opened to do that, unless the author closed it.
     */
    private void completed(String reasoning, String text) {
        finish(reasoning, text);
        if (applyAutomatically && isOpened() && hasRedraft) {
            apply();
        }
    }

    /**
     * The model is done or was stopped: what arrived stays in the text field, where it can be changed before it is applied.
     */
    private void finish(String reasoning, String text) {
        show(reasoning, text);
        hasRedraft = StringUtils.isNotBlank(text);
        showGenerating(false);
        showChanges(hasRedraft ? text : null);
    }

    /**
     * The differences of the text against the part (of the passage), hidden when there is no text.
     */
    private void showChanges(String text) {
        if (text != null) {
            changes.show(selected, text);
        }
        changesDetails.setVisible(text != null);
    }

    /**
     * The model and the protocol of the book, which the redraft is made with: "model · protocol" for the history of the part.
     */
    private String modelAndProtocol() {
        try {
            return (manuscript.getAi() == null ? "" : manuscript.getAi().getName()) + " · "
                    + (manuscript.getProtocol() == null ? "" : manuscript.getProtocol().getName());
        } catch (RuntimeException e) {
            return null;
        }
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
            ChatMessageService.PartEdit edit = chatMessageService.updateResponse(manuscript, part, text,
                    RevisionOrigin.model(RevisionSource.REDRAFT, modelAndProtocol(), false, redraftReasoning));
            // the notes the redraft was asked to deal with are dealt with
            for (Annotation annotation : addressed) {
                annotationService.setStatus(manuscript, annotation, AnnotationStatus.RESOLVED);
            }
            if (revalidateSummaries.isVisible() && revalidateSummaries.getValue() && !edit.outdated().isEmpty()) {
                // the author says the summaries still describe the story
                summaryService.revalidate(manuscript, edit.outdated());
                Notification.success(loc.getValue(L.MSG_REDRAFT_APPLIED));
            } else if (edit.outdated().isEmpty()) {
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
