package com.github.enerccio.marginalia.extensions.loregenerator.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.loregenerator.LoreGeneratorLocalization.*;

/** Asks for the name of a prompt to save. */
public class SavePromptAsDialog extends Dialog {

    /** @param onName gets the name, not empty, after the dialog closed */
    public SavePromptAsDialog(Consumer<String> onName) {
        setHeaderTitle(T.localized(SAVE_PROMPT_AS));
        TextField name = new TextField(T.localized(NAME));
        name.setWidthFull();
        add(name);
        Button save = new Button(T.localized(SAVE), e -> {
            String newName = StringUtils.trimToNull(name.getValue());
            if (newName == null) {
                name.setInvalid(true);
                name.setErrorMessage(T.localized(NAME_CANT_BE_EMPTY));
                return;
            }
            close();
            onName.accept(newName);
        });
        save.setThemeName("primary");
        getFooter().add(new Button(T.localized(CANCEL), e -> close()), save);
        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                name.focus();
            }
        });
    }
}
