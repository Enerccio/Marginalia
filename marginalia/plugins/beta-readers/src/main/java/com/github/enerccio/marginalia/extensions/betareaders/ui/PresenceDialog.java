package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.github.enerccio.marginalia.extensions.betareaders.model.Persona;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.List;
import java.util.function.BiConsumer;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.T;

/**
 * Brings a persona into a group chat or sends one away. It asks for what the user says to the group about it, which goes into the
 * history as the line the model reads ("Nox joined the conversation." and the note). To bring one in it also asks which.
 */
public class PresenceDialog extends Dialog {

    final ComboBox<Persona> persona = new ComboBox<>(T.localized("Persona"));
    final TextArea note = new TextArea(T.localized("What the group is told (optional)"));
    final Button confirm;

    private final boolean choosing;

    /**
     * @param choices  the personas that can be brought in; null when the persona is already chosen (sending one away)
     * @param onChosen told with the persona (null when none was to be chosen) and the note after the button is pressed
     */
    public PresenceDialog(String title, String button, List<Persona> choices, BiConsumer<Persona, String> onChosen) {
        this.choosing = choices != null;
        setHeaderTitle(title);
        setWidth("440px");
        setMaxWidth("96vw");
        setCloseOnOutsideClick(false);

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        if (choosing) {
            persona.setItems(choices);
            persona.setItemLabelGenerator(Persona::getName);
            persona.setWidthFull();
            content.add(persona);
        }
        note.setWidthFull();
        note.setMaxLength(500);
        content.add(note);
        add(content);

        confirm = new Button(button);
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirm.addClickListener(_ -> {
            if (choosing && persona.getValue() == null) {
                Notification.warning(T.localized("Choose a persona"));
                return;
            }
            close();
            onChosen.accept(persona.getValue(), note.getValue() == null ? "" : note.getValue().strip());
        });
        getFooter().add(new Button(T.localized("Cancel"), _ -> close()), confirm);
    }
}
