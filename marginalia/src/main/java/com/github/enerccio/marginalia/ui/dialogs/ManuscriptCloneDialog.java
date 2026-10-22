package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.BackupService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.function.Consumer;

/**
 * Makes a new book of a book: asks for its name and whether to copy only the branch of the story the book is set to or
 * all of it. The lorebook is linked, not copied (see {@link BackupService#cloneManuscript}).
 */
@Configurable
@Extendable
public class ManuscriptCloneDialog extends Dialog implements LLMInteract {

    @Autowired
    private Localization loc;

    @Autowired
    private BackupService backupService;

    private final Manuscript source;
    private Consumer<Manuscript> onClone;
    private TextField nameField;
    private Checkbox onlyCurrentBranch;
    private Button cloneButton;

    public ManuscriptCloneDialog(Manuscript source) {
        this.source = source;
    }

    /**
     * @param onClone called with the new book after the dialog closed
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "onClone", type = Consumer.class)})
    public void setOnClone(Consumer<Manuscript> onClone) {
        this.onClone = onClone;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "content", type = VerticalLayout.class),
                    @APILocals(name = "cancelButton", type = Button.class),
                    @APILocals(name = "footer", type = HorizontalLayout.class)})
    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_CLONE_BOOK));
        setWidth("460px");
        UIUtils.fitPhone(this, false);
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        nameField = new TextField(loc.getValue(L.LABEL_NAME), source.getName() + " (" + loc.getValue(L.LABEL_COPY) + ")", "");
        nameField.setRequiredIndicatorVisible(true);
        nameField.setWidthFull();

        onlyCurrentBranch = new Checkbox(loc.getValue(L.LABEL_ONLY_CURRENT_BRANCH), true);

        VerticalLayout content = new VerticalLayout(nameField, onlyCurrentBranch);
        content.setPadding(false);
        add(content);

        Button cancelButton = new Button(loc.getValue(L.LABEL_CANCEL), event -> close());
        cloneButton = new Button(loc.getValue(L.LABEL_CLONE_BOOK), event -> cloneBook());
        cloneButton.setThemeName("primary");
        HorizontalLayout footer = new HorizontalLayout(cancelButton, cloneButton);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footer);
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "clone", type = Manuscript.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void cloneBook() {
        if (StringUtils.isBlank(nameField.getValue())) {
            Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
            return;
        }
        Manuscript clone;
        try {
            clone = backupService.cloneManuscript(source, nameField.getValue(), onlyCurrentBranch.getValue());
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return;
        }
        close();
        Notification.success(loc.getValue(L.LABEL_CLONE_BOOK) + ": " + clone.getName());
        if (onClone != null) {
            onClone.accept(clone);
        }
    }

    // for the tests of the dialog

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = TextField.class))
    TextField getNameField() {
        return nameField;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Checkbox.class))
    Checkbox getOnlyCurrentBranch() {
        return onlyCurrentBranch;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Button.class))
    Button getCloneButton() {
        return cloneButton;
    }
}
