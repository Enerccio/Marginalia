package com.github.enerccio.marginalia.ui.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.command.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.github.enerccio.marginalia.ui.dialogs.CommandProgressDialog;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Input line for the commands of the {@link CommandService}, to place anywhere: a combo box where the user types
 * {@code /command-name argument argument} (arguments are separated by spaces, a space or a backslash that belongs to an
 * argument is written {@code \ } and {@code \\}), and a button that runs it. While the user types, the list offers the
 * commands that match and, once the name is typed, a help popover above the bar shows what the command does, the
 * forms (overloads) that fit and which argument comes next. The help and the result are popovers: they cover what is
 * above the bar and move nothing in the layout. Picking a command from the list writes it into the field for the arguments, unless it takes
 * none, in which case it is run.
 * <p>
 * The bar does not show what a command returns: it sends it to its {@link #setResultTarget result target}, which each
 * place that uses the bar decides about (the story tab shows it in a {@link CommandResultView} above the bar). The
 * bar reports failed and stopped commands itself, and an asynchronous command (one declared with
 * {@code async = true}) blocks the UI with a modal window that has a Stop button, which cancels the run.
 */
@Configurable
public class CommandBar extends Composite<HorizontalLayout> implements HasEnabled {
    private static final Logger log = LoggerFactory.getLogger(CommandBar.class);

    @Autowired
    private Localization loc;

    @Autowired
    private CommandService commandService;

    private ComboBox<String> input;
    private Popover helpPopover;
    private Div help;
    private boolean focused;
    private CommandResultTarget resultTarget;

    // what the user has typed, as far as the server knows
    private String text = "";

    // the last change of the value was the pick of an item of the list, so the Enter that picked it does not run
    private boolean pickedByEnter;

    /**
     * Where the results of the commands go; null (the default) drops them.
     */
    public void setResultTarget(CommandResultTarget resultTarget) {
        this.resultTarget = resultTarget;
    }

    public CommandResultTarget getResultTarget() {
        return resultTarget;
    }

    @Override
    protected HorizontalLayout initContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.addClassName(CssStyles.COMMAND_BAR);
        layout.setPadding(false);
        layout.setSpacing(true);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        input = new ComboBox<>();
        input.setPlaceholder(loc.getValue(L.LABEL_COMMAND_PLACEHOLDER));
        input.setAllowCustomValue(true);
        input.setClearButtonVisible(true);
        input.setFilterTimeout(150);
        input.setWidthFull();
        input.setRenderer(new ComponentRenderer<>(this::renderItem));
        input.setItems(query -> {
            String filter = query.getFilter().orElse("");
            typed(filter);
            CommandLine line = CommandLine.parse(filter);
            // once the name is complete the user types arguments: no list to cover the help and the run button
            List<String> names = line.command() && !line.typingName() ? List.of()
                    : CommandHelp.matchingNames(commandService.getCommands(), filter);
            // the data provider contract: the offset and the limit of the query are always applied
            return names.stream().map(name -> "/" + name).skip(query.getOffset()).limit(query.getLimit());
        });
        // The help is a popover above the bar, not a helper under the field: it covers what is above and moves nothing.
        help = new Div();
        help.addClassName(CssStyles.COMMAND_HELP);
        helpPopover = new Popover();
        helpPopover.setTarget(layout);
        helpPopover.setPosition(PopoverPosition.TOP_START);
        helpPopover.setOpenOnClick(false);
        helpPopover.setOpenOnFocus(false);
        helpPopover.setOpenOnHover(false);
        helpPopover.setModal(false);
        helpPopover.add(help);
        input.addFocusListener(_ -> {
            focused = true;
            showHelp();
        });
        input.addBlurListener(_ -> {
            focused = false;
            helpPopover.close();
        });
        // The combo box commits the text it holds as a custom value on Enter, but also when it loses the focus, on an
        // outside click and on Esc, so this only records the text. Running is Enter (below) and the run button.
        input.addCustomValueSetListener(event -> {
            changed(event.getDetail());
        });
        input.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                picked(event.getValue());
            }
        });
        // Enter also selects the focused item of the list, which is not a command to run: the selection is processed
        // in the same round trip, and the run waits for it (beforeClientResponse) to see it.
        ShortcutRegistration enter = Shortcuts.addShortcutListener(input, () -> getUI().ifPresent(ui ->
                ui.beforeClientResponse(input, _ -> runTyped())), Key.ENTER);
        enter.setBrowserDefaultAllowed(true);
        enter.setEventPropagationAllowed(true);
        enter.listenOn(input);

        Button run = new Button(Solid.PLAY.create(), _ -> run(text));
        run.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        run.setTooltipText(loc.getValue(L.LABEL_RUN_COMMAND));

        layout.add(input, run);
        layout.setFlexGrow(1, input);
        return layout;
    }

    /**
     * Puts the cursor in the field.
     */
    public void focus() {
        input.focus();
    }

    private Div renderItem(String item) {
        String name = item.startsWith("/") ? item.substring(1) : item;
        List<CommandDefinition> overloads = commandService.getCommands(name);
        Div row = new Div();
        Span nameSpan = new Span(item);
        nameSpan.addClassName(CssStyles.COMMAND_ITEM_NAME);
        row.add(nameSpan);
        if (!overloads.isEmpty()) {
            Span humanName = new Span(" " + overloads.getFirst().humanName());
            row.add(humanName);
            String description = overloads.stream().map(CommandDefinition::description).filter(d -> !d.isBlank())
                    .findFirst().orElse("");
            if (!description.isBlank()) {
                Div descriptionDiv = new Div(description);
                descriptionDiv.addClassName(CssStyles.COMMAND_ITEM_DESCRIPTION);
                row.add(descriptionDiv);
            }
        }
        return row;
    }

    /**
     * The user typed in the field (the combo box asks for the matching items on every change).
     */
    private void typed(String filter) {
        if (!filter.equals(text)) {
            changed(filter);
        }
    }

    /**
     * The text of the field is another one: a result that is shown is out of date, the help is for the new text.
     */
    private void changed(String newText) {
        if (!newText.equals(text) && resultTarget != null) {
            resultTarget.clearCommandResult();
        }
        text = newText;
        pickedByEnter = false;
        showHelp();
    }

    /**
     * Enter was pressed in the field.
     */
    private void runTyped() {
        if (pickedByEnter) {
            pickedByEnter = false;
            return;
        }
        if (!text.isBlank()) {
            run(text);
        }
    }

    /**
     * The user picked an item of the list, or cleared the field.
     */
    private void picked(String value) {
        if (value == null || value.isEmpty()) {
            changed("");
            return;
        }
        pickedByEnter = true;
        List<CommandDefinition> overloads = commandService.getCommands(CommandLine.parse(value).name());
        boolean takesNothingOnly = !overloads.isEmpty()
                && overloads.stream().allMatch(d -> !d.anyArguments() && d.parameters().isEmpty());
        if (takesNothingOnly) {
            text = value;
            run(value);
            return;
        }
        changed(value + " ");
        pickedByEnter = true;
        input.setValue(text);
    }

    /**
     * Fills the help popover for the text and opens it, or closes it when there is nothing to tell. While the name is
     * typed the list of commands (with their descriptions) is the help, so the popover is for the arguments, for text
     * that is not a command and for a name nothing matches.
     */
    private void showHelp() {
        help.removeAll();
        CommandLine line = CommandLine.parse(text);
        if (text.isBlank()) {
            helpPopover.close();
            return;
        }
        if (!line.command()) {
            help.add(new Div(loc.getValue(L.HELP_COMMAND_TYPE_SLASH)));
            helpPopover.setOpened(focused);
            return;
        }
        CommandHelp info = CommandHelp.of(commandService.getCommands(), text);
        if (info.commandName() == null) {
            help.add(new Div(loc.getValue(L.HELP_COMMAND_NO_MATCH)));
            helpPopover.setOpened(focused);
            return;
        }
        if (line.typingName()) {
            helpPopover.close();
            return;
        }

        String description = info.overloads().stream().map(CommandDefinition::description).filter(d -> !d.isBlank())
                .findFirst().orElse(info.overloads().getFirst().humanName());
        help.add(new Div(description));

        for (CommandDefinition overload : info.overloads()) {
            Div usage = new Div();
            usage.addClassName(CssStyles.COMMAND_HELP_USAGE);
            usage.add(new Span("/" + overload.commandName()));
            if (overload.anyArguments()) {
                usage.add(new Span(" [" + loc.getValue(L.LABEL_COMMAND_ANY_ARGUMENTS) + "]"));
            } else if (overload.parameters().isEmpty()) {
                usage.add(new Span(" (" + loc.getValue(L.LABEL_COMMAND_NO_ARGUMENTS) + ")"));
            }
            for (int i = 0; i < overload.parameters().size(); i++) {
                CommandParameterDefinition parameter = overload.parameters().get(i);
                Span span = new Span(" <" + parameter.name() + ": " + parameter.typeName() + ">");
                if (i == info.argumentIndex()) {
                    span.addClassName(CssStyles.COMMAND_HELP_CURRENT);
                }
                usage.add(span);
            }
            help.add(usage);
            if (info.argumentIndex() >= 0 && info.argumentIndex() < overload.parameters().size()) {
                CommandParameterDefinition next = overload.parameters().get(info.argumentIndex());
                if (!next.description().isBlank()) {
                    help.add(new Div(next.name() + ": " + next.description()));
                }
            }
        }

        info.overloads().stream().map(CommandDefinition::returns).filter(r -> !r.isVoid()).findFirst().ifPresent(returns -> {
            String text = loc.getValue(L.LABEL_COMMAND_RETURNS) + ": " + returns.name() + " (" + returns.typeName() + ")";
            help.add(new Div(returns.description().isBlank() ? text : text + " - " + returns.description()));
        });

        if (info.tooManyArguments()) {
            Div tooMany = new Div(loc.getValue(L.HELP_COMMAND_TOO_MANY_ARGUMENTS));
            tooMany.addClassName(CssStyles.COMMAND_HELP_ERROR);
            help.add(tooMany);
        }
        helpPopover.setOpened(focused);
    }

    private void run(String line) {
        CommandLine parsed = CommandLine.parse(line);
        if (!parsed.command()) {
            Notification.error(loc.getValue(L.ERROR_COMMAND_NEEDS_SLASH));
            return;
        }
        List<CommandDefinition> overloads = commandService.getCommands(parsed.name());
        if (overloads.isEmpty()) {
            Notification.error(String.format(loc.getValue(L.ERROR_COMMAND_UNKNOWN), parsed.name()));
            return;
        }
        String humanName = overloads.getFirst().humanName();

        helpPopover.close();
        if (resultTarget != null) {
            resultTarget.clearCommandResult();
        }
        CompletableFuture<Object> future = commandService.executeLine(line);
        if (future.isDone()) {
            finished(parsed.name(), humanName, future);
            return;
        }

        // an asynchronous command: block the UI with a window that can stop it
        UI ui = UI.getCurrent();
        ThreadCopyRequestAttributes attributes = ThreadCopyRequestAttributes.create();
        CommandProgressDialog dialog = new CommandProgressDialog(humanName, () -> future.cancel(true));
        dialog.create();
        dialog.open();
        future.whenComplete((_, _) -> {
            try {
                ui.access(() -> {
                    try (InRequestScope _ = new InRequestScope(attributes)) {
                        dialog.close();
                        finished(parsed.name(), humanName, future);
                    }
                    UIPushGuard.push(ui);
                });
            } catch (RuntimeException e) {
                // the UI is gone
                log.debug("The UI of command {} is gone", parsed.name(), e);
            }
        });
    }

    /**
     * The command is over: hands the result to the target, or tells the user what went wrong.
     */
    private void finished(String commandName, String humanName, CompletableFuture<Object> future) {
        try {
            Object value = future.join();
            if (resultTarget != null) {
                resultTarget.onCommandResult(new CommandResult(commandName, humanName, value));
            }
        } catch (CancellationException e) {
            Notification.warning(String.format(loc.getValue(L.MSG_COMMAND_STOPPED), humanName));
        } catch (CompletionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            if (cause instanceof CommandException failure) {
                Throwable reason = failure.getCause() == null ? failure : failure.getCause();
                log.warn("Command {} failed", commandName, reason);
                Notification.error(String.format(loc.getValue(L.ERROR_COMMAND_FAILED), humanName, reason.getMessage()));
            } else if (cause instanceof IllegalArgumentException) {
                Notification.error(String.format(loc.getValue(L.ERROR_COMMAND_ARGUMENTS), commandName));
            } else {
                log.warn("Command {} failed", commandName, cause);
                Notification.error(String.format(loc.getValue(L.ERROR_COMMAND_FAILED), humanName, cause.getMessage()));
            }
        }
    }

}
