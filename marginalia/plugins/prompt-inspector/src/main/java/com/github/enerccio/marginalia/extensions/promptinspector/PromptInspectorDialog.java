package com.github.enerccio.marginalia.extensions.promptinspector;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextArea;

import static com.github.enerccio.marginalia.extensions.promptinspector.PromptInspectorLocalization.*;

/**
 * The prompt of a generation, read only and scrollable, with the choice to send it or to stop the generation. It does not close on
 * Esc or a click outside: the generation waits for the answer.
 */
class PromptInspectorDialog extends Dialog {

    PromptInspectorDialog(String prompt, Runnable send, Runnable cancel) {
        setHeaderTitle(T.localized(SEND_THIS_PROMPT_TO_THE_MODEL));
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModal(true);
        setDraggable(true);
        setResizable(true);

        TextArea text = new TextArea();
        text.setValue(prompt);
        text.setReadOnly(true);
        text.setWidth("min(60rem, 85vw)");
        text.setHeight("60vh");
        add(text);

        Button sendButton = new Button(T.localized(SEND), event -> {
            close();
            send.run();
        });
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button(T.localized(CANCEL), event -> {
            close();
            cancel.run();
        });
        getFooter().add(cancelButton, sendButton);
    }
}
