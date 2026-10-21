package com.github.enerccio.marginalia.ui.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * A {@link CommandResultTarget} that shows the last result in a popover above its target (the {@link CommandBar} whose
 * result target it is), so that it covers what is above the bar and moves nothing in the layout. It is closed by its
 * cross, by Esc, by a click outside of it and when the user starts a new command.
 */
@Configurable
@Extendable
public class CommandResultView implements CommandResultTarget {

    @Autowired
    private Localization loc;

    private Popover popover;
    private Span title;
    private Div body;

    /**
     * Builds the popover.
     *
     * @param target the component the popover opens above, normally the command bar
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "target", type = Component.class)},
            locals = {@APILocals(name = "close", type = Button.class),
                    @APILocals(name = "header", type = HorizontalLayout.class),
                    @APILocals(name = "content", type = Div.class)})
    public void create(Component target) {
        popover = new Popover();
        popover.setTarget(target);
        popover.setPosition(PopoverPosition.TOP_START);
        popover.setOpenOnClick(false);
        popover.setOpenOnFocus(false);
        popover.setOpenOnHover(false);
        popover.setModal(false);

        title = new Span();
        title.addClassName(CssStyles.TEXT_SECONDARY);
        Button close = new Button(Solid.TIMES.create(), _ -> popover.close());
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        close.setTooltipText(loc.getValue(L.LABEL_CLOSE));
        HorizontalLayout header = new HorizontalLayout(title, close);
        header.setWidthFull();
        header.setPadding(false);
        header.setSpacing(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        body = new Div();
        body.addClassName(CssStyles.COMMAND_RESULT_TEXT);

        Div content = new Div(header, body);
        content.addClassName(CssStyles.COMMAND_RESULT);
        popover.add(content);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "result", type = CommandResult.class)})
    @Override
    public void onCommandResult(CommandResult result) {
        title.setText(String.format(loc.getValue(L.LABEL_COMMAND_RESULT), result.humanName()));
        body.setText(result.value() == null ? loc.getValue(L.LABEL_COMMAND_NO_RESULT) : result.text());
        popover.open();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    @Override
    public void clearCommandResult() {
        popover.close();
    }

}
