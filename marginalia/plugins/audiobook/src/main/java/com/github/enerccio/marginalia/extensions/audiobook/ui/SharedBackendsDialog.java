package com.github.enerccio.marginalia.extensions.audiobook.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.audiobook.model.SharedBackend;
import com.github.enerccio.marginalia.extensions.audiobook.model.SharedSettings;
import com.github.enerccio.marginalia.extensions.audiobook.service.AudiobookService;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.google.gson.Gson;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * For an administrator: the speech servers of the installation that users may use. Each one has its settings (the same fields as a server of a
 * user) and who may use it (everybody, or chosen users); a switch says whether users may bring servers of their own. A user never sees the
 * address, the keys or the requests of a shared server, only its name.
 */
public class SharedBackendsDialog extends Dialog {

    private final AudiobookService service;
    private final Runnable saved;

    private final SharedSettings settings;
    private SharedBackend current;
    private boolean binding;

    private final Select<SharedBackend> picker = new Select<>();
    private final Button deleteButton = new Button(Solid.TRASH.create(), e -> deleteBackend());
    private final Button duplicateButton = new Button(Solid.COPY.create(), e -> duplicateBackend());
    private final BackendEditor editor;
    private final Checkbox allUsers = new Checkbox(T.localized(ALL_USERS_MAY_USE_IT));
    private final MultiSelectComboBox<User> users = new MultiSelectComboBox<>(T.localized(THESE_USERS_MAY_USE_IT));
    private final VerticalLayout accessSection = new VerticalLayout();
    private final Checkbox usersMayAddOwn = new Checkbox(T.localized(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS));

    /** @param saved called after the settings were stored */
    public SharedBackendsDialog(AudiobookService service, Runnable saved) {
        this.service = service;
        this.saved = saved;
        // a copy is edited; Save stores it
        Gson gson = new Gson();
        this.settings = gson.fromJson(gson.toJson(service.sharedSettings()), SharedSettings.class);
        this.editor = new BackendEditor(service);

        setHeaderTitle(T.localized(SHARED_SPEECH_SERVERS));
        setWidth("min(60em, 95vw)");
        setMaxHeight("92vh");
        setCloseOnOutsideClick(false);

        Span help = new Span(T.localized(SHARED_SPEECH_SERVERS_DIALOG_HELP));
        help.addClassName(CssStyles.HINT_TEXT);

        picker.setLabel(T.localized(SHARED_SPEECH_SERVERS));
        picker.setItemLabelGenerator(b -> StringUtils.defaultIfBlank(b.getConfig().getName(), T.localized(NEW_BACKEND)));
        picker.setWidth("20em");
        picker.addValueChangeListener(e -> {
            if (!binding) {
                commit();
                show(e.getValue());
            }
        });
        editor.onRename(() -> {
            if (current != null) {
                picker.getListDataView().refreshItem(current);
            }
        });
        Button add = new Button(Solid.PLUS.create(), e -> addBackend());
        add.setThemeName("tertiary icon");
        add.setTooltipText(T.localized(ADD_A_BACKEND));
        deleteButton.setThemeName("tertiary icon");
        deleteButton.setTooltipText(T.localized(DELETE_THE_BACKEND));
        duplicateButton.setThemeName("tertiary icon");
        duplicateButton.setTooltipText(T.localized(DUPLICATE_THE_BACKEND));
        HorizontalLayout bar = new HorizontalLayout(picker, add, duplicateButton, deleteButton);
        bar.setAlignItems(FlexComponent.Alignment.END);

        users.setItems(service.usersToShareWith());
        users.setItemLabelGenerator(User::getLogin);
        users.setWidthFull();
        allUsers.addValueChangeListener(e -> users.setVisible(!e.getValue()));
        accessSection.setPadding(false);
        accessSection.setSpacing(true);
        accessSection.add(allUsers, users);
        accessSection.addClassName(AudiobookStyles.SECTION);

        usersMayAddOwn.setValue(settings.isUsersMayAddOwn());
        Span ownHelp = new Span(T.localized(USERS_MAY_ADD_THEIR_OWN_SPEECH_SERVERS_HELP));
        ownHelp.addClassName(CssStyles.HINT_TEXT);

        VerticalLayout content = new VerticalLayout(help, bar, accessSection, editor, usersMayAddOwn, ownHelp);
        content.setPadding(false);
        add(content);

        Button save = new Button(T.localized(SAVE), e -> save());
        save.addThemeName("primary");
        getFooter().add(new Button(T.localized(CANCEL), e -> close()), save);
        DialogFocus.onOpen(this, editor.nameField());
        DialogShortcuts.submitWith(save, editor.nameField(), editor.addressField());

        picker.setItems(settings.getBackends());
        SharedBackend first = settings.getBackends().stream().findFirst().orElse(null);
        binding = true;
        try {
            picker.setValue(first);
        } finally {
            binding = false;
        }
        show(first);
    }

    private void show(SharedBackend shared) {
        current = shared;
        deleteButton.setEnabled(shared != null);
        duplicateButton.setEnabled(shared != null);
        accessSection.setVisible(shared != null);
        editor.show(shared == null ? null : shared.getConfig());
        if (shared != null) {
            allUsers.setValue(shared.isAllUsers());
            users.setVisible(!shared.isAllUsers());
            Set<String> uuids = shared.getUserUuids();
            users.setValue(service.usersToShareWith().stream().filter(u -> uuids.contains(u.getUuid())).collect(Collectors.toSet()));
        }
    }

    /** Puts what the fields show into the shared backend that is open. */
    private void commit() {
        if (current == null) {
            return;
        }
        editor.commit();
        current.setAllUsers(allUsers.getValue());
        current.setUserUuids(users.getValue().stream().map(User::getUuid).collect(Collectors.toCollection(HashSet::new)));
    }

    private void addBackend() {
        commit();
        SharedBackend created = new SharedBackend();
        created.getConfig().setName(T.localized(NEW_BACKEND));
        settings.getBackends().add(created);
        refreshPicker(created);
    }

    /** A copy of the server with the same users allowed. */
    private void duplicateBackend() {
        if (current == null) {
            return;
        }
        commit();
        SharedBackend created = new SharedBackend();
        created.setConfig(current.getConfig().duplicate(T.localized(COPY_OF).formatted(current.getConfig().getName())));
        created.setAllUsers(current.isAllUsers());
        created.setUserUuids(current.getUserUuids());
        settings.getBackends().add(created);
        refreshPicker(created);
    }

    private void deleteBackend() {
        if (current == null) {
            return;
        }
        settings.getBackends().remove(current);
        List<SharedBackend> rest = settings.getBackends();
        refreshPicker(rest.isEmpty() ? null : rest.getFirst());
    }

    private void refreshPicker(SharedBackend selected) {
        binding = true;
        try {
            picker.setItems(settings.getBackends());
            picker.setValue(selected);
        } finally {
            binding = false;
        }
        show(selected);
    }

    private void save() {
        commit();
        for (SharedBackend shared : settings.getBackends()) {
            BackendConfig config = shared.getConfig();
            if (config.getBaseUrl().isBlank()) {
                Notification.error(T.localized(A_SHARED_SPEECH_SERVER_NEEDS_AN_ADDRESS).formatted(StringUtils.defaultIfBlank(config.getName(), T.localized(NEW_BACKEND))));
                return;
            }
        }
        settings.setUsersMayAddOwn(usersMayAddOwn.getValue());
        try {
            service.saveSharedSettings(settings);
        } catch (RuntimeException e) {
            Notification.error(e.getMessage());
            return;
        }
        Notification.success(T.localized(THE_SHARED_SPEECH_SERVERS_ARE_SAVED));
        close();
        saved.run();
    }
}
