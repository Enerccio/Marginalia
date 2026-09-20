package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class UserDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private UserService userService;

    private boolean firstTime; // forces admin creation, admin should be checked, cancel should be disabled
    private Runnable onSave;

    private User user;

    private TextField loginField;
    private TextField fullNameField;
    private PasswordField passwordField;
    private PasswordField passwordRepeatField;
    private Checkbox isAdminCheckbox;

    public UserDialog() {
        this(new User());
    }

    public UserDialog(User user) {
        this.user = user != null ? user : new User();
    }

    public void create() {
        setCloseOnEsc(!firstTime);
        setCloseOnOutsideClick(!firstTime);
        setWidth("450px");

        setHeaderTitle(user.getId() == null ? loc.getValue(L.LABEL_LOGIN) : loc.getValue(L.LABEL_USERNAME));

        FormLayout formLayout = new FormLayout();

        loginField = new TextField(loc.getValue(L.LABEL_USERNAME));
        loginField.setRequired(true);
        loginField.setWidthFull();

        fullNameField = new TextField(loc.getValue(L.LABEL_USER_FULLNAME));
        fullNameField.setWidthFull();

        passwordField = new PasswordField(loc.getValue(L.LABEL_PASSWORD));
        passwordField.setRequired(true);
        passwordField.setWidthFull();

        passwordRepeatField = new PasswordField(loc.getValue(L.LABEL_PASSWORD_AGAIN));
        passwordRepeatField.setRequired(true);
        passwordRepeatField.setWidthFull();

        isAdminCheckbox = new Checkbox(loc.getValue(L.LABEL_ADMINISTRATOR));

        if (firstTime) {
            isAdminCheckbox.setValue(true);
            isAdminCheckbox.setEnabled(false);
        } else {
            isAdminCheckbox.setValue(user.isAdmin());
        }

        if (user.getId() != null) {
            loginField.setValue(StringUtils.defaultString(user.getLogin()));
            fullNameField.setValue(StringUtils.defaultString(user.getFullName()));
        }

        formLayout.add(loginField, fullNameField, passwordField, passwordRepeatField, isAdminCheckbox);
        add(formLayout);

        Button saveButton = new Button(loc.getValue(L.LABEL_OK), event -> save());
        saveButton.setThemeName("primary");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        if (!firstTime) {
            Button cancelButton = new Button(loc.getValue(L.LABEL_CANCEL), event -> close());
            buttons.add(cancelButton);
        }

        buttons.add(saveButton);
        getFooter().add(buttons);
    }

    private void save() {
        String login = loginField.getValue();
        String fullName = fullNameField.getValue();
        String password = passwordField.getValue();
        String passwordRepeat = passwordRepeatField.getValue();

        if (StringUtils.isBlank(login)) {
            Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
            return;
        }

        if (user.getId() == null || StringUtils.isNotBlank(password) || StringUtils.isNotBlank(passwordRepeat)) {
            if (!StringUtils.equals(password, passwordRepeat)) {
                Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
                return;
            }
        }

        try {
            user.setLogin(login.trim());
            user.setFullName(fullName != null ? fullName.trim() : null);
            user.setAdmin(isAdminCheckbox.getValue());

            if (user.getId() == null) {
                user = userService.save(user);
            }

            if (StringUtils.isNotBlank(password)) {
                userService.changePassword(user, password);
            } else {
                userService.save(user);
            }

            close();

            if (onSave != null) {
                onSave.run();
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    public boolean isFirstTime() {
        return firstTime;
    }

    public void setFirstTime(boolean firstTime) {
        this.firstTime = firstTime;
    }

    public Runnable getOnSave() {
        return onSave;
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }
}