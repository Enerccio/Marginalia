package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * The turn details of a part (scene, POV, present characters, instructions): editable on the last part of the branch
 * only, the changes are saved with the part.
 */
@Extendable
public class TurnDetailsForm {

    private final TextArea sceneField;
    private final TextField povField;
    private final TextArea presentField;
    private final TextArea instructionsField;

    /**
     * @param message the part the fields are filled from
     * @param current the part now, the card replaces its message with the saved one
     * @param isLast  whether the part is the last one of the branch now
     * @param save    saves the changed message
     */
    public TurnDetailsForm(Localization loc, ChatMessage message, Supplier<ChatMessage> current, BooleanSupplier isLast, Runnable save) {
        boolean last = isLast.getAsBoolean();

        sceneField = new TextArea(loc.getValue(L.LABEL_SCENE_SETTING), StringUtils.defaultString(message.getSceneSetting()), "");
        sceneField.setReadOnly(!last);
        sceneField.setWidthFull();
        ResizableTextArea.install(loc, sceneField, "100px");
        sceneField.addValueChangeListener(e -> {
            if (e.isFromClient() && isLast.getAsBoolean()) {
                current.get().setSceneSetting(e.getValue());
                save.run();
            }
        });

        povField = new TextField(loc.getValue(L.LABEL_POV_CHARACTER), StringUtils.defaultString(message.getPovCharacter()), "");
        povField.setReadOnly(!last);
        povField.setWidthFull();
        povField.addValueChangeListener(e -> {
            if (e.isFromClient() && isLast.getAsBoolean()) {
                current.get().setPovCharacter(e.getValue());
                save.run();
            }
        });

        presentField = new TextArea(loc.getValue(L.LABEL_PRESENT_CHARACTERS), StringUtils.defaultString(message.getPresentCharacters()), "");
        presentField.setReadOnly(!last);
        presentField.setWidthFull();
        ResizableTextArea.install(loc, presentField, "80px");
        presentField.addValueChangeListener(e -> {
            if (e.isFromClient() && isLast.getAsBoolean()) {
                current.get().setPresentCharacters(e.getValue());
                save.run();
            }
        });

        instructionsField = new TextArea(loc.getValue(L.LABEL_INSTRUCTIONS), StringUtils.defaultString(message.getInstructions()), "");
        instructionsField.setReadOnly(!last);
        instructionsField.setWidthFull();
        ResizableTextArea.install(loc, instructionsField, "120px");
        instructionsField.addValueChangeListener(e -> {
            if (e.isFromClient() && isLast.getAsBoolean()) {
                current.get().setInstructions(e.getValue());
                save.run();
            }
        });
    }

    public void addTo(HasComponents container) {
        container.add(sceneField, povField, presentField, instructionsField);
    }

    public void setLastMessage(boolean isLast) {
        sceneField.setReadOnly(!isLast);
        povField.setReadOnly(!isLast);
        presentField.setReadOnly(!isLast);
        instructionsField.setReadOnly(!isLast);
    }

    public TurnInput toTurnInput() {
        return new TurnInput(sceneField.getValue(), povField.getValue(), presentField.getValue(), instructionsField.getValue());
    }

}
