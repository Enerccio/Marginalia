package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Asks for the note of a new annotation: shows the passage it is about (the whole part when there is none), takes the note, a
 * free word for its type and whether the model gets it with the part. Making the annotation is up to the caller.
 */
@Configurable(preConstruction = true)
public class AnnotationDialog extends Dialog {

    /**
     * What the author entered.
     */
    @FunctionalInterface
    public interface Saved {
        void save(String text, String type, boolean sendToPrompt);
    }

    @Autowired
    private Localization loc;

    private final TextArea note = new TextArea();
    private final TextField type = new TextField();
    private final Checkbox sendToPrompt = new Checkbox();
    private final Saved saved;

    /**
     * @param passage the words the note is about; null for a note about the whole part
     */
    public static AnnotationDialog open(String passage, Saved saved) {
        AnnotationDialog dialog = create(passage, saved);
        dialog.open();
        dialog.note.focus();
        return dialog;
    }

    /**
     * The dialog, not opened.
     */
    static AnnotationDialog create(String passage, Saved saved) {
        return new AnnotationDialog(passage, saved);
    }

    private AnnotationDialog(String passage, Saved saved) {
        this.saved = saved;
        setHeaderTitle(loc.getValue(L.LABEL_ANNOTATION_NEW));
        setWidth("min(560px, 95vw)");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(false);
        content.addClassName(CssStyles.GAP_S);

        if (StringUtils.isNotBlank(passage)) {
            Div quote = new Div(new Span(StringUtils.abbreviate(passage.strip(), 400)));
            quote.addClassName(CssStyles.ANNOTATION_QUOTE);
            content.add(quote);
        } else {
            Span whole = new Span(loc.getValue(L.MSG_ANNOTATION_WHOLE_PART));
            whole.addClassName(CssStyles.HINT_TEXT);
            content.add(whole);
        }

        note.setLabel(loc.getValue(L.LABEL_NOTE));
        note.setWidthFull();
        note.setMinHeight("120px");
        type.setLabel(loc.getValue(L.LABEL_TYPE));
        type.setWidthFull();
        type.setPlaceholder(loc.getValue(L.MSG_ANNOTATION_TYPE_PLACEHOLDER));
        sendToPrompt.setLabel(loc.getValue(L.LABEL_ANNOTATION_SEND_TO_PROMPT));
        content.add(note, type, sendToPrompt);
        add(content);

        Button save = new Button(loc.getValue(L.LABEL_SAVE), event -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button(loc.getValue(L.LABEL_CANCEL), event -> close());
        getFooter().add(cancel, save);
    }

    private void save() {
        if (StringUtils.isBlank(note.getValue())) {
            note.setInvalid(true);
            note.setErrorMessage(loc.getValue(L.MSG_ANNOTATION_NOTE_REQUIRED));
            return;
        }
        close();
        saved.save(note.getValue(), StringUtils.trimToNull(type.getValue()), sendToPrompt.getValue());
    }

    TextArea noteField() {
        return note;
    }

    TextField typeField() {
        return type;
    }

    Checkbox sendField() {
        return sendToPrompt;
    }

    void saveForTest() {
        save();
    }

}
