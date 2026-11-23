package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.FloatingWindow;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The window of an asynchronous command, with a button that stops it. It is a {@link FloatingWindow}: not modal (the page stays usable),
 * folded into the panel of the runs by its button, and a run of the registry while the command works ({@link #bind}: the command's
 * requests report to it, so the panel shows what the model writes and its Stop). Under the progress
 * bar it shows the two lines of status the command reports ({@link #setStatus}, {@link #setSubStatus}); a line that is
 * empty is hidden. The owner closes it when the command finishes, whatever the reason.
 */
@Configurable
@Extendable
public class CommandProgressDialog extends Dialog {

    @Autowired
    private Localization loc;

    private final String commandName;
    private final Runnable onStop;

    private final Div status = new Div();
    private final Div subStatus = new Div();
    private Button stop;
    private boolean stopRequested;
    private boolean done;
    private FloatingWindow floating;

    /**
     * @param commandName the name of the command for the user
     * @param onStop      called once, when the user presses Stop
     */
    public CommandProgressDialog(String commandName, Runnable onStop) {
        this.commandName = commandName;
        this.onStop = onStop;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "progress", type = ProgressBar.class),
                    @APILocals(name = "content", type = VerticalLayout.class)})
    public void create() {
        setHeaderTitle(String.format(loc.getValue(L.LABEL_COMMAND_RUNNING), commandName));
        setWidth("380px");
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        floating = new FloatingWindow(this, "command", this::getHeaderTitle, this::requestStop);
        floating.setRunning(true);

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setWidthFull();

        stop = new Button(loc.getValue(L.LABEL_STOP));
        stop.addClickListener(_ -> requestStop());

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
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "text", type = String.class)})
    public void setStatus(String text) {
        show(status, text);
    }

    /**
     * Shows the second line of status, hides it when the text is null or blank. Call it with the UI lock.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "text", type = String.class)})
    public void setSubStatus(String text) {
        show(subStatus, text);
    }

    /**
     * Enables or disables the Stop button: the command can't be stopped while it does something it has to finish.
     * Once the user has pressed it stays disabled. Call it with the UI lock.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "enabled", type = boolean.class)})
    public void setStopEnabled(boolean enabled) {
        stop.setEnabled(enabled && !stopRequested);
    }

    /**
     * Binds the current thread to the window's run until the binding is closed: what is started on it (the command) reports to the run.
     * Call it after {@link #create}; null when there is no run.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Runs.Binding.class))
    public Runs.Binding bind() {
        return floating == null ? null : floating.bind();
    }

    /**
     * The command is over: the window closes, also when it is folded in the panel of the runs. Call it with the UI lock.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class))
    public void finished() {
        done = true;
        if (floating == null) {
            close();
            return;
        }
        floating.setRunning(false);
        floating.closeForGood();
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class))
    private void requestStop() {
        if (done || stopRequested) {
            return;
        }
        stopRequested = true;
        if (stop != null) {
            stop.setEnabled(false);
        }
        onStop.run();
    }

    private static void show(Div line, String text) {
        boolean present = text != null && !text.isBlank();
        line.setText(present ? text : "");
        line.setVisible(present);
    }

}
