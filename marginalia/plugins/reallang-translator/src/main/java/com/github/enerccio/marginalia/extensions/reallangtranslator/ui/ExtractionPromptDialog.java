package com.github.enerccio.marginalia.extensions.reallangtranslator.ui;

import com.github.enerccio.marginalia.extensions.reallangtranslator.model.ExtractionPrompt;
import com.github.enerccio.marginalia.extensions.translatorcommon.ui.EditDialog;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import static com.github.enerccio.marginalia.extensions.reallangtranslator.ReallangTranslatorLocalization.*;

/**
 * The window that edits a prompt of the extraction library: its name and the instructions for the model. The prompt is changed only when <i>OK</i> is
 * pressed and the fields are right.
 */
public class ExtractionPromptDialog extends EditDialog {

    private final TextField name = new TextField(T.localized(NAME));
    private final TextArea text = new TextArea(T.localized(INSTRUCTIONS_FOR_THE_MODEL));

    /**
     * @param prompt the prompt to edit; written from the fields on OK
     * @param isNew  whether the prompt is new
     * @param done   run after the prompt was written from the fields
     */
    public ExtractionPromptDialog(ExtractionPrompt prompt, boolean isNew, Runnable done) {
        super(isNew ? T.localized(NEW_PROMPT) : T.localized(EDIT_THE_PROMPT), done);
        setWidth("760px");
        name.setWidthFull();
        name.setValue(prompt.getName());
        text.setWidthFull();
        text.setMinHeight("18rem");
        text.setMaxHeight("60vh");
        text.setHelperText(T.localized(WHAT_TO_LOOK_FOR_AND_HOW_TO_WRITE_IT));
        text.setValue(prompt.getText());
        body().add(name, text);
        setValidator(() -> {
            if (StringUtils.isBlank(name.getValue()) || StringUtils.isBlank(text.getValue())) {
                return T.localized(THE_PROMPT_NEEDS_A_NAME_AND_A_TEXT);
            }
            prompt.setName(name.getValue().strip());
            prompt.setText(text.getValue().strip());
            return null;
        });
    }
}
