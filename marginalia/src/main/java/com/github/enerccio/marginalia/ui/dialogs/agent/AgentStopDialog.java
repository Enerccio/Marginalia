package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.function.Consumer;

/**
 * Asks how the run of an agent ends: the result (the working copy) is kept for the review, or discarded.
 */
@Configurable
public class AgentStopDialog extends Dialog implements LLMInteract {

    @Autowired
    private Localization loc;

    private final String agentName;
    private final boolean canKeep;
    private final Consumer<Boolean> onStop;
    private Button keepButton;
    private Button discardButton;

    /**
     * @param canKeep whether the result can be kept (a finished run can only be discarded)
     * @param onStop  called with true to keep the result, false to discard it
     */
    public AgentStopDialog(String agentName, boolean canKeep, Consumer<Boolean> onStop) {
        this.agentName = agentName;
        this.canKeep = canKeep;
        this.onStop = onStop;
    }

    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_STOP));
        add(new Span(String.format(loc.getValue(L.MSG_CONFIRM_STOP_RUN), agentName)));

        Button cancelButton = new Button(loc.getValue(L.LABEL_CANCEL), event -> close());
        keepButton = new Button(loc.getValue(L.LABEL_STOP_AND_KEEP), event -> stop(true));
        keepButton.setVisible(canKeep);
        discardButton = new Button(loc.getValue(L.LABEL_STOP_AND_DISCARD), event -> stop(false));
        discardButton.setThemeName("error");
        HorizontalLayout footer = new HorizontalLayout(cancelButton, discardButton, keepButton);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footer);
    }

    private void stop(boolean keep) {
        close();
        onStop.accept(keep);
    }

    // for the tests of the dialog

    Button getKeepButton() {
        return keepButton;
    }

    Button getDiscardButton() {
        return discardButton;
    }
}
