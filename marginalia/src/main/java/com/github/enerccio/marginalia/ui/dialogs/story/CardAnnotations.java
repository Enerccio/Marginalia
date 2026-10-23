package com.github.enerccio.marginalia.ui.dialogs.story;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.AnnotationService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * What a card of the story (the desktop and the phone one) does with the annotations of its part: the badge that counts what needs
 * a look, the popover that shows the notes (at the mark that was clicked, or at the badge), and making a note about the whole part,
 * the text the author selected, or attaching a note that lost its passage to the selection.
 * <p>
 * A click on a mark of the text is reported by the browser (see {@link JavascriptLibrary#fireAnnotationClickEvent}) after it moved the
 * anchor, a small element in the card, onto the mark; the popover has the anchor as its target. The anchor has to be added to the
 * card (it is positioned against the card), the badge to its header.
 */
public class CardAnnotations {

    private final Localization loc;
    private final StoryAnnotations annotations;
    private final Supplier<ChatMessage> message;
    private final Markdown response;
    private final Supplier<TextArea> editor;
    private final Runnable saveEdit;

    private final Button badge = new Button(Solid.COMMENT_ALT.create());
    private final Div anchor = new Div();
    private final Popover popover = new Popover();
    private final VerticalLayout margin = new VerticalLayout();
    private boolean marginMode;
    private final AnnotationView.Host host = new AnnotationView.Host() {
        @Override
        public boolean canAttach(Annotation annotation) {
            return true;
        }

        @Override
        public void attachToSelection(Annotation annotation) {
            CardAnnotations.this.attachToSelection(annotation);
        }
    };

    /**
     * @param message  the part of the card as it is now
     * @param response the markdown the text of the part is drawn in
     * @param editor   the text area the part is edited in; null while it is not edited
     * @param saveEdit saves the edit of the part and leaves the editor: the offsets of a selection in the editor are the ones of the saved text
     */
    public CardAnnotations(Localization loc, StoryAnnotations annotations, Supplier<ChatMessage> message, Markdown response,
                           Supplier<TextArea> editor, Runnable saveEdit) {
        this.loc = loc;
        this.annotations = annotations;
        this.message = message;
        this.response = response;
        this.editor = editor;
        this.saveEdit = saveEdit;

        badge.setThemeName("tertiary small");
        badge.addClassName(CssStyles.ANNOTATION_BADGE);
        badge.setVisible(false);
        badge.addClickListener(event -> show(null));

        anchor.addClassName(CssStyles.ANNOTATION_ANCHOR);
        popover.setTarget(anchor);
        popover.setOpenOnClick(false);
        margin.setPadding(false);
        margin.setSpacing(false);
        margin.addClassNames(CssStyles.ANNOTATION_MARGIN, CssStyles.GAP_S);
        margin.setVisible(false);
        JavascriptLibrary.linkMarginNotes(response.getElement(), margin.getElement());
        JavascriptLibrary.fireAnnotationClickEvent(response.getElement(), anchor.getElement());
        // the menu of the buttons of the card takes the selection away, so the text has a menu of its own (right click, long press) that
        // finds it, and keeps the copy of the browser's menu, which it replaces
        JavascriptLibrary.rememberSelectionOnContextMenu(response.getElement(), AnnotationService.CONTEXT_LENGTH / 2);
        ContextMenu textMenu = new ContextMenu(response);
        textMenu.addItem(loc.getValue(L.LABEL_ANNOTATE_SELECTION), event -> annotateSelection());
        textMenu.addItem(loc.getValue(L.LABEL_COPY), event -> JavascriptLibrary.copySelection(response.getElement()));
        response.getElement().addEventListener("annotation-click",
                event -> show(event.getEventData().get("event.detail.ids").asString())).addEventData("event.detail.ids");
    }

    /**
     * The badge for the header of the card.
     */
    public Button getBadge() {
        return badge;
    }

    /**
     * The anchor of the popover, to be added to the card.
     */
    public Div getAnchor() {
        return anchor;
    }

    /**
     * The column of the notes of the part, to be added to the card before the text. It is hidden until the margin is switched on.
     */
    public VerticalLayout getMargin() {
        return margin;
    }

    public boolean isMarginMode() {
        return marginMode;
    }

    /**
     * Shows the notes of the part in the margin (a click on a mark then finds the note there) or in the popover at the mark.
     */
    public void setMarginMode(boolean on) {
        marginMode = on;
        margin.setVisible(on);
        if (on) {
            popover.close();
        }
        fillMargin(false);
    }

    /**
     * Shows how many annotations of the part need a look, and the notes in the margin when it is on.
     *
     * @param generating the text is being generated again: its annotations are not in it yet
     */
    public void refresh(boolean generating) {
        int count = generating ? 0 : annotations.openCount(message.get().getId());
        badge.setVisible(count > 0);
        badge.setText(String.valueOf(count));
        badge.setTooltipText(String.format(loc.getValue(L.MSG_ANNOTATIONS_OPEN_COUNT), count));
        fillMargin(generating);
    }

    /**
     * The notes in the margin are the ones the badge counts, in the order of their passages.
     */
    private void fillMargin(boolean generating) {
        margin.removeAll();
        if (!marginMode || generating) {
            return;
        }
        for (Annotation annotation : annotations.of(message.get().getId())) {
            if (annotation.getStatus() != AnnotationStatus.RESOLVED && annotation.getStatus() != AnnotationStatus.DISMISSED) {
                margin.add(new AnnotationView(annotation, annotations, host));
            }
        }
    }

    /**
     * Opens the annotations in the popover: the ones with the uuids (the marks that were clicked, at the mark), or all the
     * annotations of the part (at the badge) when the ids are null.
     */
    public void show(String ids) {
        Set<String> wanted = ids == null ? null : Set.of(ids.split(","));
        if (wanted != null && marginMode) {
            // the note is in the margin already
            JavascriptLibrary.flashMarginNote(margin.getElement(), ids.split(",")[0]);
            return;
        }
        List<Annotation> shown = new ArrayList<>();
        for (Annotation annotation : annotations.of(message.get().getId())) {
            if (wanted == null || wanted.contains(annotation.getUuid())) {
                shown.add(annotation);
            }
        }
        if (shown.isEmpty()) {
            return;
        }
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.addClassNames(CssStyles.GAP_S, CssStyles.ANNOTATION_POPOVER);
        for (Annotation annotation : shown) {
            content.add(new AnnotationView(annotation, annotations, host));
        }
        popover.removeAll();
        popover.add(content);
        popover.setTarget(wanted == null ? badge : anchor);
        popover.setOpened(true);
    }

    /**
     * Scrolls to the passage of the annotation and flashes it.
     */
    public void flash(Annotation annotation) {
        JavascriptLibrary.flashAnnotation(response.getElement(), annotation.getUuid());
    }

    /**
     * Makes a note about the whole part.
     */
    public void annotatePart() {
        AnnotationDialog.open(null, (text, type, send) -> annotations.create(message.get(), null, null, text, type, send));
    }

    /**
     * Makes a note about the text the author selected.
     */
    public void annotateSelection() {
        withSelection(range -> AnnotationDialog.open(message.get().getResponse().substring(range[0], range[1]),
                (text, type, send) -> annotations.create(message.get(), range[0], range[1], text, type, send)));
    }

    /**
     * Attaches the note to the text the author selected, instead of the passage it lost.
     */
    public void attachToSelection(Annotation annotation) {
        withSelection(range -> {
            annotations.reattach(annotation, range[0], range[1]);
            popover.close();
        });
    }

    /**
     * Finds out what the author selected in the part, as offsets in its text, and gives them to the action: the offsets of the
     * selection in the editor when the part is edited (the edit is saved first), else the words the browser says are selected,
     * which are found in the markdown source of the part. Tells the author when nothing is selected, or the selection is not in the
     * text.
     */
    void withSelection(Consumer<int[]> action) {
        TextArea editing = editor.get();
        if (editing != null) {
            JavascriptLibrary.getTextAreaSelection(editing.getElement()).then(String.class, value -> {
                String[] range = StringUtils.defaultString(value).split(";");
                int start = range.length == 2 ? Integer.parseInt(range[0]) : 0;
                int end = range.length == 2 ? Integer.parseInt(range[1]) : 0;
                if (end <= start) {
                    Notification.warning(loc.getValue(L.MSG_ANNOTATION_SELECT_TEXT));
                    return;
                }
                saveEdit.run();
                action.accept(new int[]{start, Math.min(end, StringUtils.length(message.get().getResponse()))});
            });
            return;
        }
        JavascriptLibrary.getSelectionContext(response.getElement(), AnnotationService.CONTEXT_LENGTH / 2).then(String.class, json -> {
            if (StringUtils.isBlank(json)) {
                Notification.warning(loc.getValue(L.MSG_ANNOTATION_SELECT_TEXT));
                return;
            }
            JsonObject selection = JsonParser.parseString(json).getAsJsonObject();
            int[] range = StoryAnnotations.locate(StringUtils.defaultString(message.get().getResponse()),
                    selection.get("text").getAsString(), selection.get("before").getAsString(), selection.get("after").getAsString());
            if (range == null) {
                Notification.warning(loc.getValue(L.MSG_ANNOTATION_NOT_IN_SOURCE));
                return;
            }
            action.accept(range);
        });
    }

}
