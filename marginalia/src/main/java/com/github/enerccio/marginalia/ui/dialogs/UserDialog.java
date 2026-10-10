package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
@Extendable
public class UserDialog extends Dialog {

    @Autowired
    private Localization loc;

    @Autowired
    private UserService userService;

    @Autowired
    private Configuration configuration;

    private boolean firstTime; // forces admin creation, admin should be checked, cancel should be disabled
    private boolean selfEdit; // user edits own account, admin flag can't be changed
    private Runnable onSave;

    private User user;

    private TextField loginField;
    private TextField fullNameField;
    private PasswordField currentPassword;
    private PasswordField passwordField;
    private PasswordField passwordRepeatField;
    private Checkbox clearPasswordCheckbox;
    private Checkbox unlockCheckbox;
    private Checkbox isAdminCheckbox;
    private final boolean openedFromUser;

    public UserDialog(boolean openedFromUser) {
        this(new User(), openedFromUser);
    }

    public UserDialog(User user, boolean openedFromUser) {
        this.user = user != null ? user : new User();
        this.openedFromUser = openedFromUser;
    }

    public void create() {
        setCloseOnEsc(!firstTime);
        setCloseOnOutsideClick(!firstTime);
        setWidth("480px");
        setModality(ModalityMode.STRICT);

        if (firstTime) {
            setHeaderTitle(loc.getValue(L.LABEL_LOGIN));
        } else if (selfEdit) {
            setHeaderTitle(loc.getValue(L.LABEL_CHANGE_PASSWORD));
        } else {
            setHeaderTitle(user.getId() == null ? loc.getValue(L.LABEL_NEW_USER) : loc.getValue(L.LABEL_EDIT_USER));
        }

        FormLayout formLayout = new FormLayout();

        loginField = new TextField(loc.getValue(L.LABEL_USERNAME));
        loginField.setRequired(true);
        loginField.setWidthFull();

        fullNameField = new TextField(loc.getValue(L.LABEL_USER_FULLNAME));
        fullNameField.setWidthFull();

        if (openedFromUser) {
            currentPassword = new PasswordField(loc.getValue(L.LABEL_CURRENT_PASSWORD));
            currentPassword.setWidthFull();
        }

        passwordField = new PasswordField(loc.getValue(L.LABEL_PASSWORD));
        passwordField.setRequired(true);
        passwordField.setWidthFull();

        passwordRepeatField = new PasswordField(loc.getValue(L.LABEL_PASSWORD_AGAIN));
        passwordRepeatField.setRequired(true);
        passwordRepeatField.setWidthFull();

        // only an administrator editing an existing account can remove its password (forgotten password reset)
        clearPasswordCheckbox = new Checkbox(loc.getValue(L.LABEL_CLEAR_PASSWORD));
        clearPasswordCheckbox.setVisible(!openedFromUser && !firstTime && user.getId() != null);
        clearPasswordCheckbox.addValueChangeListener(e -> {
            passwordField.clear();
            passwordRepeatField.clear();
            passwordField.setEnabled(!e.getValue());
            passwordRepeatField.setEnabled(!e.getValue());
        });

        // offered only while the account has failed logins on record
        unlockCheckbox = new Checkbox(loc.getValue(L.LABEL_UNLOCK_ACCOUNT));
        unlockCheckbox.setVisible(!openedFromUser && !firstTime && user.getId() != null
                && (user.getFailedLogins() > 0 || user.getLockedUntil() > 0));

        isAdminCheckbox = new Checkbox(loc.getValue(L.LABEL_ADMINISTRATOR));

        if (firstTime) {
            isAdminCheckbox.setValue(true);
            isAdminCheckbox.setEnabled(false);
        } else {
            isAdminCheckbox.setValue(user.isAdmin());
            isAdminCheckbox.setVisible(!selfEdit);
        }

        if (user.getId() != null) {
            loginField.setValue(StringUtils.defaultString(user.getLogin()));
            fullNameField.setValue(StringUtils.defaultString(user.getFullName()));
        }

        if (openedFromUser)
            formLayout.add(loginField, fullNameField, currentPassword, passwordField, passwordRepeatField, isAdminCheckbox);
        else
            formLayout.add(loginField, fullNameField, passwordField, passwordRepeatField, clearPasswordCheckbox, unlockCheckbox, isAdminCheckbox);
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
        boolean clearPassword = clearPasswordCheckbox.getValue();

        if (StringUtils.isBlank(login)) {
            Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
            return;
        }

        if (openedFromUser) {
            String currentPasswordValue = currentPassword.getValue();
            try {
                if (!userService.authenticate(user.getLogin(), currentPasswordValue, UIUtils.clientAddress(configuration))) {
                    Notification.warning(loc.getValue(L.MSG_VALIDATION_FAILED_CANT_SAVE));
                    return;
                }
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
                return;
            }
        }

        if (user.getId() == null || StringUtils.isNotBlank(password) || StringUtils.isNotBlank(passwordRepeat)) {
            if (!Strings.CS.equals(password, passwordRepeat)) {
                Notification.warning(loc.getValue(L.MSG_PASSWORDS_DO_NOT_MATCH));
                return;
            }
        }

        try {
            if (!userService.isLoginAvailable(user, login.trim())) {
                Notification.warning(loc.getValue(L.MSG_LOGIN_ALREADY_EXISTS));
                return;
            }
            if (user.getId() != null && !isAdminCheckbox.getValue() && userService.isLastAdmin(user)) {
                Notification.warning(loc.getValue(L.MSG_CANNOT_DELETE_LAST_ADMIN));
                return;
            }

            Runnable continueWithSave = () -> {
                try {
                    user.setLogin(login.trim());
                    user.setFullName(fullName != null ? fullName.trim() : null);
                    user.setAdmin(isAdminCheckbox.getValue());

                    if (user.getId() == null) {
                        user = userService.save(user);
                    }

                    if (clearPassword) {
                        userService.clearPassword(user);
                    } else if (StringUtils.isNotBlank(password)) {
                        userService.changePassword(user, password);
                    } else {
                        userService.save(user);
                    }

                    if (unlockCheckbox.getValue()) {
                        userService.unlock(user);
                    }

                    close();

                    if (onSave != null) {
                        onSave.run();
                    }
                } catch (Exception e) {
                    UIUtils.internalServerError(loc, e);
                }
            };

            // existing user with empty password fields keeps the current password
            if (clearPassword) {
                ConfirmDialog.show(loc.getValue(L.MSG_CLEAR_PASSWORD_WARNING), continueWithSave);
            } else if (user.getId() == null && StringUtils.isBlank(password)) {
                ConfirmDialog.show(loc.getValue(L.MSG_NO_PASSWORD_WARNING), continueWithSave);
            } else {
                continueWithSave.run();
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

    public boolean isSelfEdit() {
        return selfEdit;
    }

    public void setSelfEdit(boolean selfEdit) {
        this.selfEdit = selfEdit;
    }

    public Runnable getOnSave() {
        return onSave;
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }
}