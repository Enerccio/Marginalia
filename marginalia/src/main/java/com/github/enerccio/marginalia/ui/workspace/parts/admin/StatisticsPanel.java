package com.github.enerccio.marginalia.ui.workspace.parts.admin;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.service.StatisticsPolicyService;
import com.github.enerccio.marginalia.domain.traits.APILocals;
import com.github.enerccio.marginalia.domain.traits.APIReturn;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.domain.traits.PublicAPI;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Comparator;
import java.util.List;

/**
 * The administrator's side of the writing statistics: the switch that stops recording for everybody, how many months of them are
 * kept (the users may choose less for themselves), and the clearing of the history of one user or of all of them. What a user may do
 * with their own history is in the statistics windows.
 */
@Configurable
@Extendable
public class StatisticsPanel implements LLMInteract {

    @Autowired
    private Localization loc;

    @Autowired
    private StatisticsPolicyService policyService;

    @Autowired
    private UserService userService;

    private Checkbox recording;
    private IntegerField keepMonths;
    private ComboBox<User> users;

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = Component.class),
            locals = {@APILocals(name = "layout", type = VerticalLayout.class),
                    @APILocals(name = "description", type = Span.class),
                    @APILocals(name = "save", type = Button.class),
                    @APILocals(name = "clearUser", type = Button.class),
                    @APILocals(name = "clearEveryone", type = Button.class),
                    @APILocals(name = "clearRow", type = FlexLayout.class)})
    public Component create() throws Exception {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        Span description = new Span(loc.getValue(L.HELP_STATISTICS_ADMIN));
        description.addClassName(CssStyles.TEXT_SECONDARY);

        recording = new Checkbox(loc.getValue(L.LABEL_STATISTICS_RECORD));
        keepMonths = new IntegerField(loc.getValue(L.LABEL_STATISTICS_KEEP_MONTHS));
        keepMonths.setMin(0);
        keepMonths.setStepButtonsVisible(true);
        keepMonths.setWidth("20em");
        keepMonths.setHelperText(loc.getValue(L.HELP_STATISTICS_KEEP_MONTHS_INSTANCE));

        Button save = new Button(loc.getValue(L.LABEL_SAVE), _ -> save());
        save.setThemeName("primary");

        users = new ComboBox<>(loc.getValue(L.LABEL_USERS));
        users.setItemLabelGenerator(User::getLogin);
        users.setClearButtonVisible(true);
        Button clearUser = new Button(loc.getValue(L.LABEL_STATISTICS_CLEAR_USER), _ -> clearUser());
        clearUser.addThemeVariants(ButtonVariant.LUMO_ERROR);
        Button clearEveryone = new Button(loc.getValue(L.LABEL_STATISTICS_CLEAR_EVERYONE), _ -> ConfirmDialog.show(
                loc.getValue(L.MSG_CONFIRM_STATISTICS_CLEAR_EVERYONE), this::clearEveryone));
        clearEveryone.addThemeVariants(ButtonVariant.LUMO_ERROR);
        FlexLayout clearRow = new FlexLayout(users, clearUser, clearEveryone);
        clearRow.addClassNames(CssStyles.FLEX_WRAP, CssStyles.GAP_S);
        clearRow.setAlignItems(FlexComponent.Alignment.END);

        layout.add(description, recording, keepMonths, save, clearRow);
        load();
        return layout;
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "all", type = List.class)})
    private void load() throws Exception {
        recording.setValue(policyService.isInstanceRecording());
        int months = policyService.getInstanceKeepMonths();
        keepMonths.setValue(months > 0 ? months : null);
        List<User> all = userService.findAll().stream().filter(user -> !user.isDeleted())
                .sorted(Comparator.comparing(User::getLogin, String.CASE_INSENSITIVE_ORDER)).toList();
        users.setItems(all);
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "months", type = Integer.class),
                    @APILocals(name = "e", type = Exception.class)})
    private void save() {
        try {
            Integer months = keepMonths.getValue();
            policyService.setInstance(recording.getValue(), months == null ? 0 : months);
            Notification.success(loc.getValue(L.MSG_SETTINGS_SAVED));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "user", type = User.class)})
    private void clearUser() {
        User user = users.getValue();
        if (user == null) {
            return;
        }
        ConfirmDialog.show(String.format(loc.getValue(L.MSG_CONFIRM_STATISTICS_CLEAR_USER), user.getLogin()), () -> {
            try {
                policyService.clearUser(user);
                Notification.success(loc.getValue(L.MSG_STATISTICS_CLEARED));
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "e", type = Exception.class)})
    private void clearEveryone() {
        try {
            policyService.clearEveryone();
            Notification.success(loc.getValue(L.MSG_STATISTICS_CLEARED));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }
}
