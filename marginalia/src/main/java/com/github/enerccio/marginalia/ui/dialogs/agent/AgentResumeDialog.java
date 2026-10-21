package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.function.Consumer;

/**
 * Resumes a paused or stopped run, with a note for the agent if the author has something to add. The note is optional and
 * is kept with the run, so it works for every run, also for one that is not a conversation.
 */
@Configurable
public class AgentResumeDialog extends Dialog implements LLMInteract {

    @Autowired
    private Localization loc;

    private final Consumer<String> onResume;
    private TextArea noteField;
    private Button resumeButton;

    /**
     * @param onResume called with the note, empty when the author wrote none
     */
    public AgentResumeDialog(Consumer<String> onResume) {
        this.onResume = onResume;
    }

    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_RESUME));
        setWidth("520px");
        UIUtils.fitPhone(this, false);

        noteField = new TextArea(loc.getValue(L.LABEL_AGENT_RESUME_NOTE));
        noteField.setHelperText(loc.getValue(L.HELP_AGENT_RESUME_NOTE));
        noteField.setMinRows(4);
        noteField.setWidthFull();
        add(noteField);

        Button cancelButton = new Button(loc.getValue(L.LABEL_CANCEL), event -> close());
        resumeButton = new Button(loc.getValue(L.LABEL_RESUME), event -> {
            close();
            onResume.accept(noteField.getValue().strip());
        });
        resumeButton.setThemeName("primary");
        HorizontalLayout footer = new HorizontalLayout(cancelButton, resumeButton);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footer);
    }

    // for the tests of the dialog

    TextArea getNoteField() {
        return noteField;
    }

    Button getResumeButton() {
        return resumeButton;
    }
}
