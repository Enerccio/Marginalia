package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Modal window that blocks the UI while an asynchronous command runs, with a button that stops it. Under the progress
 * bar it shows the two lines of status the command reports ({@link #setStatus}, {@link #setSubStatus}); a line that is
 * empty is hidden. The owner closes it when the command finishes, whatever the reason.
 */
@Configurable
public class CommandProgressDialog extends Dialog {

    @Autowired
    private Localization loc;

    private final String commandName;
    private final Runnable onStop;

    private final Div status = new Div();
    private final Div subStatus = new Div();

    /**
     * @param commandName the name of the command for the user
     * @param onStop      called once, when the user presses Stop
     */
    public CommandProgressDialog(String commandName, Runnable onStop) {
        this.commandName = commandName;
        this.onStop = onStop;
    }

    public void create() {
        setHeaderTitle(String.format(loc.getValue(L.LABEL_COMMAND_RUNNING), commandName));
        setWidth("380px");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.STRICT);

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setWidthFull();

        Button stop = new Button(loc.getValue(L.LABEL_STOP));
        stop.addClickListener(_ -> {
            stop.setEnabled(false);
            onStop.run();
        });

        status.addClassNames(CssStyles.TEXT_SEMIBOLD);
        status.setVisible(false);
        subStatus.addClassNames(CssStyles.TEXT_SECONDARY, CssStyles.TEXT_S);
        subStatus.setVisible(false);

        VerticalLayout content = new VerticalLayout(progress, status, subStatus);
        content.setPadding(false);
        content.setAlignItems(FlexComponent.Alignment.STRETCH);
        add(content);
        getFooter().add(stop);
    }

    /**
     * Shows the main line of status, hides it when the text is null or blank. Call it with the UI lock.
     */
    public void setStatus(String text) {
        show(status, text);
    }

    /**
     * Shows the second line of status, hides it when the text is null or blank. Call it with the UI lock.
     */
    public void setSubStatus(String text) {
        show(subStatus, text);
    }

    private static void show(Div line, String text) {
        boolean present = text != null && !text.isBlank();
        line.setText(present ? text : "");
        line.setVisible(present);
    }

}
