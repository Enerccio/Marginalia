package com.github.enerccio.marginalia.ui.dialogs;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.ScrollPanel;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
@Extendable
public class ErrorDialog {

    @Autowired
    private Localization loc;

    private final String message;
    private final Throwable cause;

    public ErrorDialog(String message, Throwable cause) {
        if (message == null)
            message = "";

        this.message = message;
        this.cause = cause;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class))
    public void open() {
        open(true);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "showDetails", type = boolean.class)},
            locals = {@APILocals(name = "dialog", type = Dialog.class),
                    @APILocals(name = "errorIcon", type = Icon.class),
                    @APILocals(name = "stackTrace", type = String.class),
                    @APILocals(name = "scrollPanel", type = ScrollPanel.class),
                    @APILocals(name = "messageText", type = Div.class),
                    @APILocals(name = "close", type = Button.class),
                    @APILocals(name = "buttons", type = HorizontalLayout.class)})
    public void open(boolean showDetails) {
        Dialog dialog = new Dialog();
        dialog.setModality(ModalityMode.STRICT);
        dialog.setWidth(showDetails ? "1024px" : "550px");
        dialog.setHeight(showDetails ? "510px" : "190px");
        dialog.setCloseOnOutsideClick(true);
        dialog.setHeaderTitle(loc.getValue(L.LABEL_APPLICATION_ERROR));

        Icon errorIcon = Solid.CIRCLE_INFO.create();
        errorIcon.setColor("var(--lumo-error-color)");
        errorIcon.setSize("30px");
        errorIcon.addClickListener(event -> dialog.close());
        dialog.getHeader().add(errorIcon);

        String stackTrace = ExceptionUtils.getStackTrace(cause);

        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.setHeight("250px");
        scrollPanel.add(new Div(stackTrace));

        dialog.add(new Hr());
        // plain text node: the message can carry text from outside (provider replies, user input, exception messages)
        Div messageText = new Div(message);
        messageText.addClassNames(CssStyles.TEXT_CENTER, CssStyles.TEXT_BOLD, CssStyles.PRE_WRAP, CssStyles.PADDING_COMPACT);
        dialog.add(messageText);

        if (showDetails) {
            dialog.add(new Hr());
            dialog.add(scrollPanel);
            dialog.add(new Hr());
        }

        Button close = new Button(loc.getValue(L.LABEL_EXIT), event -> dialog.close());
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setHeight("50px");
        buttons.add(close);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        dialog.add(buttons);

        dialog.open();
    }
}
