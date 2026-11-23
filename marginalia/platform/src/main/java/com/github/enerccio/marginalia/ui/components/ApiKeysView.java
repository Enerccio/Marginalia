package com.github.enerccio.marginalia.ui.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.security.model.ApiScope;
import com.github.enerccio.marginalia.domain.security.model.ApiKey;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.ApiKeyService;
import com.github.enerccio.marginalia.domain.security.service.ApiKeyService.CreatedKey;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.ui.widgets.DialogShortcuts;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The keys of the HTTP API of the current user, in the user settings: the list, a new key (shown once, to copy) and revoking a key.
 * The changes are stored at once, not by the Save of the settings. A user who may not use the API sees why.
 */
@Configurable
public class ApiKeysView extends VerticalLayout {
    private static final Logger log = LoggerFactory.getLogger(ApiKeysView.class);

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private AdminGuard adminGuard;

    @Autowired
    private Localization loc;

    @Autowired
    private User currentUser;

    private final Grid<ApiKey> grid = new Grid<>(ApiKey.class, false);
    private boolean built;

    public ApiKeysView() {
        setSizeFull();
        setPadding(false);
        // texts need the injected localization, which is there only after the constructor
        addAttachListener(e -> {
            if (!built) {
                built = true;
                build();
            }
            refresh();
        });
    }

    private void build() {
        add(new Span(loc.getValue(L.MSG_API_KEYS_INFO)));
        if (!mayUseApi()) {
            add(new Span(loc.getValue(L.MSG_API_NOT_ALLOWED)));
            return;
        }
        Button create = new Button(loc.getValue(L.LABEL_NEW_API_KEY), Solid.PLUS.create(), e -> openCreateDialog());
        add(new HorizontalLayout(create));

        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        grid.setSizeFull();
        grid.addColumn(ApiKey::getName)
                .setHeader(loc.getValue(L.LABEL_NAME))
                .setSortable(true)
                .setFlexGrow(1);
        grid.addColumn(key -> ApiKeyService.TOKEN_START + key.getKeyPrefix() + "_…")
                .setHeader(loc.getValue(L.LABEL_KEY))
                .setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE_GIANT);
        grid.addColumn(key -> scopesText(key.getScopes()))
                .setHeader(loc.getValue(L.LABEL_ACCESS))
                .setFlexGrow(1);
        grid.addColumn(key -> date(format, key.getCreation(), ""))
                .setHeader(loc.getValue(L.LABEL_CREATED))
                .setFlexGrow(0).setWidth(UIConstants.DATE_COLUMN_SIZE);
        grid.addColumn(key -> date(format, key.getExpires(), loc.getValue(L.LABEL_NEVER)))
                .setHeader(loc.getValue(L.LABEL_EXPIRES))
                .setFlexGrow(0).setWidth(UIConstants.DATE_COLUMN_SIZE);
        grid.addColumn(key -> date(format, key.getLastUsed(), ""))
                .setHeader(loc.getValue(L.LABEL_LAST_USED))
                .setFlexGrow(0).setWidth(UIConstants.DATE_COLUMN_SIZE);
        grid.addComponentColumn(key -> {
                    Button revoke = new Button(Solid.TRASH.create(), e -> confirmRevoke(key));
                    revoke.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                    revoke.setTooltipText(loc.getValue(L.LABEL_REVOKE));
                    revoke.setAriaLabel(loc.getValue(L.LABEL_REVOKE));
                    return revoke;
                })
                .setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE);
        add(grid);
        setFlexGrow(1, grid);
    }

    public void refresh() {
        if (!built || grid.getParent().isEmpty()) {
            return;
        }
        try {
            List<ApiKey> keys = new ArrayList<>(apiKeyService.findAllForUser());
            keys.sort(Comparator.comparing(ApiKey::getCreation, Comparator.nullsLast(Comparator.reverseOrder())));
            grid.setItems(keys);
        } catch (Exception e) {
            log.error("Cannot load the API keys", e);
            Notification.error(e.getMessage());
        }
    }

    private void openCreateDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(loc.getValue(L.LABEL_NEW_API_KEY));

        TextField name = new TextField(loc.getValue(L.LABEL_NAME));
        name.setWidthFull();
        name.setRequired(true);
        name.setMaxLength(100);

        CheckboxGroup<ApiScope> access = new CheckboxGroup<>(loc.getValue(L.LABEL_ACCESS));
        access.setHelperText(loc.getValue(L.HELP_API_ACCESS));
        access.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        access.setWidthFull();
        boolean admin = isAdmin();
        access.setItems(Arrays.stream(ApiScope.values()).filter(scope -> admin || !scope.isAdministration()).toList());
        access.setItemLabelGenerator(this::scopeText);
        access.setValue(Set.of(ApiScope.READ));

        IntegerField days = new IntegerField(loc.getValue(L.LABEL_EXPIRES_AFTER_DAYS));
        days.setHelperText(loc.getValue(L.HELP_EXPIRES_AFTER_DAYS));
        days.setMin(1);
        days.setWidthFull();

        Button createButton = new Button(loc.getValue(L.LABEL_CREATE_KEY), e -> {
            try {
                Date expires = days.getValue() == null || days.getValue() < 1 ? null
                        : new Date(System.currentTimeMillis() + days.getValue() * 24L * 60 * 60 * 1000);
                CreatedKey created = apiKeyService.create(name.getValue(), access.getValue(), expires);
                dialog.close();
                refresh();
                showCreated(created);
            } catch (Exception ex) {
                Notification.warning(String.format(loc.getValue(L.ERROR_API_KEY_CREATE_FAILED), ex.getMessage()));
            }
        });
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button(loc.getValue(L.LABEL_CANCEL), e -> dialog.close());

        VerticalLayout content = new VerticalLayout(name, access, days);
        content.setPadding(false);
        dialog.add(content);
        dialog.getFooter().add(cancel, createButton);
        DialogFocus.onOpen(dialog, name);
        DialogShortcuts.submitWith(createButton, name, days);
        dialog.open();
    }

    private void showCreated(CreatedKey created) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(created.key().getName());
        dialog.setCloseOnOutsideClick(false);

        TextField token = new TextField(loc.getValue(L.LABEL_KEY));
        token.setValue(created.token());
        token.setReadOnly(true);
        token.setWidthFull();

        Button copy = new Button(loc.getValue(L.LABEL_COPY), Solid.COPY.create(),
                e -> JavascriptLibrary.copyToClipboard(e.getSource().getUI().orElseThrow().getPage(), created.token()));
        Button close = new Button(loc.getValue(L.LABEL_CLOSE), e -> dialog.close());
        close.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        VerticalLayout content = new VerticalLayout(new Span(loc.getValue(L.MSG_API_KEY_CREATED)), token);
        content.setPadding(false);
        dialog.add(content);
        dialog.getFooter().add(copy, close);
        dialog.open();
    }

    private void confirmRevoke(ApiKey key) {
        ConfirmDialog.show(String.format(loc.getValue(L.MSG_REVOKE_API_KEY), key.getName()), () -> {
            try {
                apiKeyService.revoke(key.getUuid());
            } catch (Exception e) {
                log.error("Cannot revoke the API key", e);
                Notification.error(e.getMessage());
            }
            refresh();
        });
    }

    private String scopeText(ApiScope scope) {
        return loc.getValue(switch (scope) {
            case READ -> L.ENUM_API_SCOPE_READ;
            case WRITE -> L.ENUM_API_SCOPE_WRITE;
            case UPDATE -> L.ENUM_API_SCOPE_UPDATE;
            case EXTENSIONS -> L.ENUM_API_SCOPE_EXTENSIONS;
        });
    }

    private String scopesText(Set<ApiScope> scopes) {
        // write includes read, it need not be said
        return scopes.stream().filter(scope -> scope != ApiScope.READ || !scopes.contains(ApiScope.WRITE)).sorted()
                .map(this::scopeText).collect(Collectors.joining(", "));
    }

    private static String date(DateFormat format, Date date, String none) {
        return date == null ? none : format.format(date);
    }

    private boolean mayUseApi() {
        try {
            return apiKeyService.mayUseApi(currentUser);
        } catch (Exception e) {
            log.error("Cannot check the API permission", e);
            return false;
        }
    }

    private boolean isAdmin() {
        try {
            return adminGuard.isAdmin();
        } catch (Exception e) {
            return false;
        }
    }

}
