package com.github.enerccio.marginalia.ui.dialogs.story;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.ResizableTextArea;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

/**
 * The fields of the "new turn" popover of a story tab (scene, POV, present characters, instructions) and what the user
 * typed in them. The tab puts {@link #createContent} into a popover of its own size and position.
 */
@Extendable
public class NewTurnForm {

    private final Localization loc;

    private TextArea sceneSettingField;
    private TextField povCharacterField;
    private TextArea presentCharactersField;
    private TextArea instructionsField;

    private String pendingSceneSetting = "";
    private String pendingPovCharacter = "";
    private String pendingPresentCharacters = "";
    private String pendingInstructions = "";
    private boolean editedByUser = false;

    public NewTurnForm(Localization loc) {
        this.loc = loc;
    }

    /**
     * @param onGenerate runs when the generate button is clicked
     */
    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = VerticalLayout.class),
            parameters = {@APIParameter(name = "onGenerate", type = Runnable.class)},
            locals = {@APILocals(name = "content", type = VerticalLayout.class),
                    @APILocals(name = "title", type = Span.class),
                    @APILocals(name = "generateBtn", type = Button.class)})
    public VerticalLayout createContent(Runnable onGenerate) {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(false);

        Span title = new Span(loc.getValue(L.LABEL_NEW_TURN_INSTRUCTIONS));
        title.addClassName(CssStyles.TEXT_BOLD);

        sceneSettingField = new TextArea(loc.getValue(L.LABEL_SCENE_SETTING));
        sceneSettingField.setWidthFull();
        ResizableTextArea.install(loc, sceneSettingField, "100px");
        sceneSettingField.addValueChangeListener(e -> {
            pendingSceneSetting = e.getValue();
            editedByUser = true;
        });

        povCharacterField = new TextField(loc.getValue(L.LABEL_POV_CHARACTER));
        povCharacterField.setWidthFull();
        povCharacterField.addValueChangeListener(e -> {
            pendingPovCharacter = e.getValue();
            editedByUser = true;
        });

        presentCharactersField = new TextArea(loc.getValue(L.LABEL_PRESENT_CHARACTERS));
        presentCharactersField.setWidthFull();
        ResizableTextArea.install(loc, presentCharactersField, "80px");
        presentCharactersField.addValueChangeListener(e -> {
            pendingPresentCharacters = e.getValue();
            editedByUser = true;
        });

        instructionsField = new TextArea(loc.getValue(L.LABEL_INSTRUCTIONS));
        instructionsField.setWidthFull();
        ResizableTextArea.install(loc, instructionsField, "120px");
        instructionsField.addValueChangeListener(e -> {
            pendingInstructions = e.getValue();
            editedByUser = true;
        });

        Button generateBtn = new Button(loc.getValue(L.LABEL_GENERATE), Solid.PLAY.create(), event -> onGenerate.run());
        generateBtn.setThemeName("primary small");
        generateBtn.setWidthFull();

        content.add(title, sceneSettingField, povCharacterField, presentCharactersField, instructionsField, generateBtn);
        return content;
    }

    /**
     * Fills the fields from the last part of the branch, unless the user typed something that is not generated yet.
     */
    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "lastMsg", type = ChatMessage.class)})
    public void syncIfUntouched(ChatMessage lastMsg) {
        if (!editedByUser) {
            syncFieldsFromState(lastMsg);
        }
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "lastMsg", type = ChatMessage.class)})
    private void syncFieldsFromState(ChatMessage lastMsg) {
        if (lastMsg != null) {
            pendingSceneSetting = StringUtils.defaultString(lastMsg.getSceneSetting());
            pendingPovCharacter = StringUtils.defaultString(lastMsg.getPovCharacter());
            pendingPresentCharacters = StringUtils.defaultString(lastMsg.getPresentCharacters());
        }
        pendingInstructions = "";

        sceneSettingField.setValue(pendingSceneSetting);
        povCharacterField.setValue(pendingPovCharacter);
        presentCharactersField.setValue(pendingPresentCharacters);
        instructionsField.setValue(pendingInstructions);
    }

    /**
     * The typed values go into a turn, the next opening of the popover shows the last part again.
     */
    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = TurnInput.class))
    public TurnInput consume() {
        editedByUser = false;
        return toTurnInput();
    }

    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = TurnInput.class))
    public TurnInput toTurnInput() {
        return new TurnInput(pendingSceneSetting, pendingPovCharacter, pendingPresentCharacters, pendingInstructions);
    }

    /**
     * Keeps what the user typed on the next opening of the popover, e.g. after the turn was discarded.
     */
    @PublicAPI(from = "1.2.0",
            returns = @APIReturn(type = void.class))
    public void keepTypedValues() {
        editedByUser = true;
    }

}
