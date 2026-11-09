package com.github.enerccio.marginalia.extensions.translatorcommon.ui;

import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.function.Supplier;

import static com.github.enerccio.marginalia.extensions.translatorcommon.ui.TranslatorCommonLocalization.*;

/**
 * A small window that edits one thing: the fields, <i>Cancel</i> and <i>OK</i>. The check runs when <i>OK</i> is pressed; it writes the
 * fields into the thing and answers the problem to show, or null when all is well, and then the window closes.
 */
public class EditDialog extends Dialog {

    private final VerticalLayout body = new VerticalLayout();
    private Supplier<String> validator = () -> null;

    /**
     * @param validate checks the fields and, when they are right, writes them into the thing; the problem or null
     * @param done     run after the window closed with <i>OK</i>
     */
    public EditDialog(String title, Component content, Supplier<String> validate, Runnable done) {
        this(title, done);
        body.add(content);
        validator = validate;
    }

    /**
     * A window whose content and check the subclass sets after its fields exist ({@link #body()}, {@link #setValidator}).
     */
    protected EditDialog(String title, Runnable done) {
        setHeaderTitle(title);
        setWidth("640px");
        setMaxWidth("96vw");
        setMaxHeight("92vh");
        setCloseOnOutsideClick(false);
        setResizable(true);
        setDraggable(true);

        body.setPadding(false);
        body.setWidthFull();
        add(body);

        Button cancel = new Button(TC.localized(CANCEL), _ -> close());
        Button ok = new Button(TC.localized(OK), _ -> {
            String problem = validator.get();
            if (problem != null) {
                Notification.error(problem);
                return;
            }
            close();
            if (done != null) {
                done.run();
            }
        });
        ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(cancel, ok);
    }

    /** Where the fields go. */
    protected VerticalLayout body() {
        return body;
    }

    protected void setValidator(Supplier<String> validator) {
        this.validator = validator;
    }
}
