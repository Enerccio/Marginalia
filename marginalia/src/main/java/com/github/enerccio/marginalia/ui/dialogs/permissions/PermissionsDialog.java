package com.github.enerccio.marginalia.ui.dialogs.permissions;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.bound.SessionManager;
import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.security.service.PermissionService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

/**
 * What an administrator allowed a user to do: the permissions of the user in a grid, where each can be granted or not,
 * deleted, and settings that only some permission types have are edited in its detail row ({@link
 * PermissionUIFactory}), folded until the administrator unfolds them with the arrow in front of the name. A permission type the user has none of can be added. Nothing is stored until <b>Save</b>
 * (deleting included, a deleted permission is deleted for good); after the save the page of the user is reloaded, so
 * what depends on the permissions is built again, unless they are the user who edits.
 */
@Configurable
public class PermissionsDialog extends Dialog implements LLMInteract {

    @Autowired
    private Localization loc;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private User currentUser;

    private final User user;
    private final PermissionUIFactory uiFactory = new PermissionUIFactoryImpl();

    /** A permission as edited in the dialog. */
    private static final class Row {
        private final Permission permission;
        private final PermissionUIComponent component;
        private final boolean originalGranted;
        private boolean touched;

        private Row(Permission permission, PermissionUIComponent component) {
            this.permission = permission;
            this.component = component;
            this.originalGranted = permission.isGranted();
        }

        private boolean isNew() {
            return permission.getId() == null;
        }

        private boolean isModified() {
            return isNew() || touched || permission.isGranted() != originalGranted;
        }
    }

    private final List<Row> rows = new ArrayList<>();
    // permissions that exist and are deleted when saved
    private final List<Permission> deleted = new ArrayList<>();
    // types whose settings are unfolded; kept when the rows are read again
    private final Set<PermissionType> expanded = new HashSet<>();
    private final ListDataProvider<Row> dataProvider = new ListDataProvider<>(rows);
    private final Grid<Row> grid = new Grid<>();
    private ComboBox<PermissionType> typeCombo;
    private Button addButton;
    private Button saveButton;

    public PermissionsDialog(User user) {
        this.user = user;
    }

    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_PERMISSIONS) + ": " + user.getLogin());
        setWidth("720px");
        setModality(ModalityMode.STRICT);
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);

        typeCombo = new ComboBox<>(loc.getValue(L.LABEL_PERMISSION));
        typeCombo.setItemLabelGenerator(type -> loc.getValue(loc.getPermissionType(type)));
        typeCombo.setWidth("16em");
        typeCombo.addValueChangeListener(e -> addButton.setEnabled(e.getValue() != null));
        addButton = new Button(loc.getValue(L.LABEL_ADD_PERMISSION), Solid.PLUS.create(), e -> addPermission());
        addButton.setThemeName("primary");
        addButton.setEnabled(false);
        HorizontalLayout toolbar = new HorizontalLayout(typeCombo, addButton);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.END);

        grid.setDataProvider(dataProvider);
        grid.setHeight("420px");
        grid.setDetailsVisibleOnClick(false);
        grid.addComponentColumn(row -> {
                    // folds the settings of the permission, rows without settings keep the space so the names line up
                    Component fold;
                    if (row.component == null) {
                        fold = new Span();
                        fold.addClassName(CssStyles.FOLD_SPACER);
                    } else {
                        boolean open = expanded.contains(row.permission.getPermissionType());
                        Button toggle = new Button((open ? Solid.ANGLE_DOWN : Solid.ANGLE_RIGHT).create(),
                                e -> setExpanded(row, !expanded.contains(row.permission.getPermissionType())));
                        toggle.setThemeName("tertiary-inline");
                        fold = toggle;
                    }
                    HorizontalLayout name = new HorizontalLayout(fold,
                            new Span(loc.getValue(loc.getPermissionType(row.permission.getPermissionType()))));
                    name.setAlignItems(FlexComponent.Alignment.CENTER);
                    return name;
                })
                .setHeader(loc.getValue(L.LABEL_NAME))
                .setAutoWidth(false)
                .setFlexGrow(1);
        grid.addComponentColumn(row -> {
                    Checkbox granted = new Checkbox(row.permission.isGranted());
                    granted.addValueChangeListener(e -> setGranted(row, e.getValue()));
                    return granted;
                })
                .setHeader(loc.getValue(L.LABEL_GRANTED))
                .setAutoWidth(false)
                .setFlexGrow(0)
                .setWidth(UIConstants.TOOL_COLUMN_SIZE_LARGE);
        grid.addComponentColumn(row -> {
                    Button delete = new Button(Solid.TRASH.create(), e -> remove(row));
                    delete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
                    return delete;
                })
                .setHeader("")
                .setAutoWidth(false)
                .setFlexGrow(0)
                .setWidth(UIConstants.TOOL_COLUMN_SIZE_LARGE);
        grid.setItemDetailsRenderer(new ComponentRenderer<>(row -> row.component == null ? new Span() : row.component.getComponent()));

        VerticalLayout content = new VerticalLayout(toolbar, grid);
        content.setPadding(false);
        add(content);

        Button exitButton = new Button(loc.getValue(L.LABEL_EXIT), e -> exit());
        saveButton = new Button(loc.getValue(L.LABEL_SAVE), e -> save());
        saveButton.setThemeName("primary");
        getFooter().add(exitButton, saveButton);

        load();
    }

    /**
     * Whether something was changed since the permissions were read.
     */
    public boolean isDirty() {
        return !deleted.isEmpty() || rows.stream().anyMatch(Row::isModified);
    }

    private void load() {
        try {
            rows.clear();
            deleted.clear();
            for (Permission permission : permissionService.getPermissions(user)) {
                rows.add(createRow(permission));
            }
            refresh();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private Row createRow(Permission permission) {
        PermissionUIComponent component = uiFactory.createComponent(permission.getPermissionType());
        Row row = new Row(permission, component);
        if (component != null) {
            component.model2component(permission);
            component.onChange(() -> {
                row.touched = true;
                updateState();
            });
        }
        return row;
    }

    /**
     * Shows the rows again (details of the rows that have settings are open) and offers the permission types the user
     * has none of.
     */
    private void refresh() {
        rows.sort((a, b) -> a.permission.getPermissionType().compareTo(b.permission.getPermissionType()));
        dataProvider.refreshAll();
        rows.forEach(row -> grid.setDetailsVisible(row, isExpanded(row)));
        List<PermissionType> available = Arrays.stream(PermissionType.values())
                .filter(type -> rows.stream().noneMatch(row -> row.permission.getPermissionType() == type))
                .toList();
        PermissionType selected = typeCombo.getValue();
        typeCombo.setItems(available);
        typeCombo.setEnabled(!available.isEmpty());
        typeCombo.setValue(available.contains(selected) ? selected : null);
        updateState();
    }

    private void updateState() {
        saveButton.setEnabled(isDirty());
    }

    private void addPermission() {
        PermissionType type = typeCombo.getValue();
        if (type == null) {
            return;
        }
        Permission permission = new Permission();
        permission.setPermissionHolder(user);
        permission.setPermissionType(type);
        permission.setGranted(true);
        Row row = createRow(permission);
        rows.add(row);
        // what was just added is what the administrator wants to set up
        if (row.component != null) {
            expanded.add(type);
        }
        refresh();
    }

    private boolean isExpanded(Row row) {
        return row.component != null && expanded.contains(row.permission.getPermissionType());
    }

    private void setExpanded(Row row, boolean open) {
        if (open) {
            expanded.add(row.permission.getPermissionType());
        } else {
            expanded.remove(row.permission.getPermissionType());
        }
        // the icon of the button follows
        dataProvider.refreshItem(row);
        grid.setDetailsVisible(row, isExpanded(row));
    }

    private void setGranted(Row row, boolean granted) {
        row.permission.setGranted(granted);
        updateState();
    }

    // what the grid does with a permission, by its type, for the tests: the components of the grid cells are only
    // created when the browser asks for them

    List<PermissionType> getTypes() {
        return rows.stream().map(row -> row.permission.getPermissionType()).toList();
    }

    List<PermissionType> getAvailableTypes() {
        return typeCombo.getListDataView().getItems().toList();
    }

    void setGranted(PermissionType type, boolean granted) {
        setGranted(row(type), granted);
    }

    void remove(PermissionType type) {
        remove(row(type));
    }

    boolean isExpanded(PermissionType type) {
        return isExpanded(row(type)) && grid.isDetailsVisible(row(type));
    }

    void setExpanded(PermissionType type, boolean open) {
        setExpanded(row(type), open);
    }

    PermissionUIComponent getSettings(PermissionType type) {
        return row(type).component;
    }

    private Row row(PermissionType type) {
        return rows.stream().filter(row -> row.permission.getPermissionType() == type).findFirst().orElseThrow();
    }

    private void remove(Row row) {
        rows.remove(row);
        if (!row.isNew()) {
            deleted.add(row.permission);
        }
        refresh();
    }

    private void save() {
        try {
            for (Permission permission : deleted) {
                permissionService.delete(permission, true);
            }
            for (Row row : rows) {
                if (row.isModified()) {
                    if (row.component != null) {
                        row.component.component2model(row.permission);
                    }
                    permissionService.save(row.permission);
                }
            }
            load();
            Notification.success(loc.getValue(L.MSG_SETTINGS_SAVED));
            if (!user.getId().equals(currentUser.getId())) {
                sessionManager.reloadUser(user);
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void exit() {
        if (isDirty()) {
            ConfirmDialog.show(loc.getValue(L.MSG_UNSAVED_CHANGES_EXIT), this::close);
        } else {
            close();
        }
    }
}
