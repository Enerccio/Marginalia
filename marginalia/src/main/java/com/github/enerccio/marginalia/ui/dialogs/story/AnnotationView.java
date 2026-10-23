package com.github.enerccio.marginalia.ui.dialogs.story;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * One annotation as the author sees and handles it (in the popover of a mark, the list of a part and the list of the book): who
 * wrote it and what state it is in, the passage it is about, the note, and what can be done with it: resolve or dismiss it, open
 * it again, change the note, delete it, say that the model gets it with the part and, for a note that lost its passage, attach it
 * to the selection. Every change goes through {@link StoryAnnotations}, which redraws the marks; the view draws itself again
 * with the annotation as it is saved.
 */
@Configurable(preConstruction = true)
public class AnnotationView extends VerticalLayout {

    /**
     * What the view asks of the screen it is on.
     */
    public interface Host {

        /**
         * Attaches the annotation to the text the author has selected in its part.
         */
        default void attachToSelection(Annotation annotation) {
        }

        /**
         * Brings the part of the annotation to the screen.
         */
        default void goToPart(Annotation annotation) {
        }

        /**
         * Whether the annotation can be attached to a selection from here (the part is shown by a card).
         */
        default boolean canAttach(Annotation annotation) {
            return false;
        }

        /**
         * Whether {@link #goToPart} does something from here.
         */
        default boolean canGoToPart(Annotation annotation) {
            return false;
        }

        Host NONE = new Host() {
        };
    }

    @Autowired
    private Localization loc;

    private final StoryAnnotations annotations;
    private final Host host;
    private Annotation annotation;
    private boolean editing;

    public AnnotationView(Annotation annotation, StoryAnnotations annotations, Host host) {
        this.annotation = annotation;
        this.annotations = annotations;
        this.host = host == null ? Host.NONE : host;
        setPadding(false);
        setSpacing(false);
        addClassNames(CssStyles.ANNOTATION_VIEW, CssStyles.GAP_S);
        build();
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    private void build() {
        removeAll();
        add(buildHeader());
        Component quote = buildQuote();
        if (quote != null) {
            add(quote);
        }
        add(editing ? buildEditor() : buildNote());
        if (!editing) {
            add(buildFooter());
        }
    }

    private Component buildHeader() {
        Span status = new Span(loc.getValue(statusLabel(annotation.getStatus())));
        status.addClassNames(CssStyles.ANNOTATION_STATUS, switch (annotation.getStatus()) {
            case RESOLVED -> CssStyles.ANNOTATION_STATUS_RESOLVED;
            case DISMISSED -> CssStyles.ANNOTATION_STATUS_DISMISSED;
            case ORPHANED -> CssStyles.ANNOTATION_STATUS_ORPHANED;
            default -> CssStyles.ANNOTATION_STATUS_OPEN;
        });

        Span source = new Span(sourceText());
        source.addClassName(CssStyles.TEXT_SECONDARY);

        HorizontalLayout header = new HorizontalLayout(status, source);
        if (StringUtils.isNotBlank(annotation.getType())) {
            Span type = new Span(annotation.getType());
            type.addClassName(CssStyles.ANNOTATION_TYPE);
            header.add(type);
        }
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);
        header.setPadding(false);
        header.setWidthFull();

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(false);
        buttons.setPadding(false);
        buttons.addClassName(CssStyles.MARGIN_LEFT_AUTO);
        if (annotation.getStatus() == AnnotationStatus.OPEN || annotation.getStatus() == AnnotationStatus.ORPHANED) {
            buttons.add(iconButton(Solid.CHECK, L.LABEL_ANNOTATION_RESOLVE, () -> annotations.setStatus(annotation, AnnotationStatus.RESOLVED)));
            buttons.add(iconButton(Solid.TIMES, L.LABEL_ANNOTATION_DISMISS, () -> annotations.setStatus(annotation, AnnotationStatus.DISMISSED)));
        } else {
            buttons.add(iconButton(Solid.UNDO, L.LABEL_ANNOTATION_REOPEN, () -> annotations.setStatus(annotation, AnnotationStatus.OPEN)));
        }
        buttons.add(iconButton(Solid.PENCIL, L.LABEL_EDIT, this::edit));
        buttons.add(iconButton(Solid.TRASH, L.LABEL_DELETE, () ->
                ConfirmDialog.show(loc.getValue(L.MSG_COMMAND_DELETE_ANNOTATION_CONFIRM), () -> annotations.remove(annotation))));
        header.add(buttons);
        return header;
    }

    private Button iconButton(Solid icon, L tooltip, Runnable action) {
        Button button = new Button(icon.create());
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        button.setTooltipText(loc.getValue(tooltip));
        button.addClickListener(event -> {
            action.run();
            refreshFromStore();
        });
        return button;
    }

    private String sourceText() {
        String source = loc.getValue(switch (annotation.getSource()) {
            case AI -> L.ENUM_ANNOTATION_SOURCE_AI;
            case AGENT -> L.ENUM_ANNOTATION_SOURCE_AGENT;
            case PLUGIN -> L.ENUM_ANNOTATION_SOURCE_PLUGIN;
            default -> L.ENUM_ANNOTATION_SOURCE_AUTHOR;
        });
        return StringUtils.isBlank(annotation.getLabel()) ? source : source + " · " + annotation.getLabel();
    }

    /**
     * The words the annotation is about, or why it has none.
     */
    private Component buildQuote() {
        if (annotation.getStatus() == AnnotationStatus.ORPHANED) {
            return hint(L.MSG_ANNOTATION_ORPHANED_HINT, annotation.getQuote());
        }
        if (annotation.isNotLocated()) {
            return hint(L.MSG_ANNOTATION_NOT_LOCATED_HINT, annotation.getQuote());
        }
        if (annotation.hasRange() && StringUtils.isNotBlank(annotation.getQuote())) {
            Div quote = new Div(new Span(StringUtils.abbreviate(annotation.getQuote(), 240)));
            quote.addClassName(CssStyles.ANNOTATION_QUOTE);
            return quote;
        }
        return null;
    }

    private Component hint(L message, String quote) {
        Div hint = new Div(new Span(loc.getValue(message)));
        hint.addClassName(CssStyles.HINT_TEXT);
        if (StringUtils.isNotBlank(quote)) {
            Div old = new Div(new Span(StringUtils.abbreviate(quote, 240)));
            old.addClassName(CssStyles.ANNOTATION_QUOTE);
            Div both = new Div(hint, old);
            return both;
        }
        return hint;
    }

    private Component buildNote() {
        Div note = new Div();
        note.setText(StringUtils.defaultString(annotation.getText()));
        note.addClassName(CssStyles.ANNOTATION_NOTE);
        return note;
    }

    private Component buildEditor() {
        TextArea text = new TextArea();
        text.setWidthFull();
        text.setMinHeight("80px");
        text.setLabel(loc.getValue(L.LABEL_NOTE));
        text.setValue(StringUtils.defaultString(annotation.getText()));

        TextField type = new TextField(loc.getValue(L.LABEL_TYPE));
        type.setWidthFull();
        type.setValue(StringUtils.defaultString(annotation.getType()));

        Button save = new Button(loc.getValue(L.LABEL_SAVE), event -> {
            if (StringUtils.isBlank(text.getValue())) {
                text.setInvalid(true);
                return;
            }
            editing = false;
            // an empty type clears it
            annotations.update(annotation, text.getValue(), type.getValue(), null);
            refreshFromStore();
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        Button cancel = new Button(loc.getValue(L.LABEL_CANCEL), event -> {
            editing = false;
            build();
        });
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        HorizontalLayout buttons = new HorizontalLayout(save, cancel);

        VerticalLayout editor = new VerticalLayout(text, type, buttons);
        editor.setPadding(false);
        editor.setSpacing(false);
        editor.addClassName(CssStyles.GAP_S);
        return editor;
    }

    private Component buildFooter() {
        HorizontalLayout footer = new HorizontalLayout();
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setPadding(false);
        footer.setWidthFull();

        Checkbox send = new Checkbox(loc.getValue(L.LABEL_ANNOTATION_SEND_TO_PROMPT));
        send.setValue(annotation.isSendToPrompt());
        send.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                annotations.update(annotation, null, null, event.getValue());
            }
        });
        footer.add(send);

        if (!annotation.hasRange() && host.canAttach(annotation)) {
            Button attach = new Button(loc.getValue(L.LABEL_ANNOTATION_ATTACH_SELECTION), event -> host.attachToSelection(annotation));
            attach.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            attach.addClassName(CssStyles.MARGIN_LEFT_AUTO);
            footer.add(attach);
        }
        if (host.canGoToPart(annotation)) {
            Button go = new Button(loc.getValue(L.LABEL_SHOW_IN_STORY), event -> host.goToPart(annotation));
            go.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            go.addClassName(CssStyles.MARGIN_LEFT_AUTO);
            footer.add(go);
        }
        return footer;
    }

    private void edit() {
        editing = true;
        build();
    }

    /**
     * Draws the view again with the annotation as it is saved now (it is not given back by the change).
     */
    private void refreshFromStore() {
        Annotation fresh = annotations.byUuid(annotation.getUuid());
        if (fresh != null) {
            annotation = fresh;
        }
        build();
    }

    static L statusLabel(AnnotationStatus status) {
        return switch (status) {
            case RESOLVED -> L.ENUM_ANNOTATION_STATUS_RESOLVED;
            case DISMISSED -> L.ENUM_ANNOTATION_STATUS_DISMISSED;
            case ORPHANED -> L.ENUM_ANNOTATION_STATUS_ORPHANED;
            default -> L.ENUM_ANNOTATION_STATUS_OPEN;
        };
    }

}
