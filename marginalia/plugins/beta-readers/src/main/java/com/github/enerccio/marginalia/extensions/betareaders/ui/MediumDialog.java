package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.github.enerccio.marginalia.extensions.betareaders.model.Medium;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.T;

/**
 * Creates or edits a medium: its name and the fragment of the prompt that tells the personas what the text stands for. The fields are
 * written to the medium when it is saved.
 */
public class MediumDialog extends Dialog {

    private final Medium medium;
    private final Predicate<String> nameTaken;
    private final Runnable onSaved;

    private final TextField name = new TextField(T.localized("Name"));
    private final TextArea prompt = new TextArea(T.localized("Prompt"));

    /**
     * @param header    what the dialog says, like "New medium"
     * @param medium    the medium to edit, or a new one that the owner adds to its list in {@code onSaved}
     * @param nameTaken whether another medium of the list has this name
     * @param onSaved   called after the medium was changed
     */
    public MediumDialog(String header, Medium medium, Predicate<String> nameTaken, Runnable onSaved) {
        this.medium = medium;
        this.nameTaken = nameTaken;
        this.onSaved = onSaved;

        setHeaderTitle(header);
        setWidth("620px");
        setMaxWidth("96vw");
        setMaxHeight("90vh");
        setCloseOnOutsideClick(false);

        name.setRequiredIndicatorVisible(true);
        name.setWidthFull();
        name.setValue(StringUtils.defaultString(medium.getName()));
        DialogFocus.onOpen(this, name);

        prompt.setWidthFull();
        prompt.setMinHeight("200px");
        prompt.setHelperText(T.localized("Goes into the prompt of the chats: what the text stands for and how the personas react to it"));
        prompt.setValue(StringUtils.defaultString(medium.getPrompt()));

        VerticalLayout content = new VerticalLayout(name, prompt);
        content.setPadding(false);
        add(content);

        Button cancel = new Button(T.localized("Cancel"), _ -> close());
        Button save = new Button(T.localized("Save"), _ -> save());
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(cancel, save);
    }

    private void save() {
        String trimmed = StringUtils.strip(name.getValue());
        if (StringUtils.isEmpty(trimmed)) {
            name.setInvalid(true);
            name.setErrorMessage(T.localized("The name is required"));
            return;
        }
        if (nameTaken.test(trimmed)) {
            name.setInvalid(true);
            name.setErrorMessage(T.localized("Another medium has this name"));
            return;
        }
        medium.setName(trimmed);
        medium.setPrompt(StringUtils.defaultString(prompt.getValue()));
        onSaved.run();
        close();
    }
}
