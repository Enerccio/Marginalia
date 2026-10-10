package com.github.enerccio.marginalia.ui.widgets;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.service.UserService;
import com.github.enerccio.marginalia.domain.service.CleanupService.EntityKey;
import com.github.enerccio.marginalia.domain.service.TrashService;
import com.github.enerccio.marginalia.domain.service.TrashService.Blocker;
import com.github.enerccio.marginalia.domain.service.TrashService.RestoreResult;
import com.github.enerccio.marginalia.domain.service.TrashService.TrashFilter;
import com.github.enerccio.marginalia.domain.service.TrashService.TrashItem;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Deleted objects that can be restored, in a lazy table with a check box per object. Filters by type of the object
 * and, for administrators (<code>allUsers</code>), by its owner. Restoring the selection is refused as a whole when
 * a selected object depends on a deleted object that is not selected, see {@link TrashService}.
 * <p>
 * The widget only shows what {@link TrashService} allows the current user to see, <code>allUsers</code> just adds the
 * owner filter and column.
 */
@Configurable
@Extendable
public class TrashGrid extends VerticalLayout {

    @Autowired
    private Localization loc;

    @Autowired
    private TrashService trashService;

    @Autowired
    private UserService userService;

    private final boolean allUsers;
    private final List<Runnable> restoreListeners = new ArrayList<>();

    private Grid<TrashItem> grid;
    private ComboBox<User> userFilter;
    private ComboBox<Class<?>> typeFilter;
    private Button restoreButton;

    /**
     * @param allUsers show the owner filter and column, for administrators
     */
    public TrashGrid(boolean allUsers) {
        this.allUsers = allUsers;
    }

    public TrashGrid create() throws Exception {
        setSizeFull();
        setPadding(false);
        setSpacing(true);

        Span description = new Span(loc.getValue(L.MSG_TRASH_DESCRIPTION));
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.END);

        if (allUsers) {
            userFilter = new ComboBox<>(loc.getValue(L.LABEL_USER));
            userFilter.setPlaceholder(loc.getValue(L.LABEL_ALL_USERS));
            userFilter.setClearButtonVisible(true);
            userFilter.setItemLabelGenerator(User::getLogin);
            userFilter.addValueChangeListener(e -> refreshGrid());
            toolbar.add(userFilter);
        }

        typeFilter = new ComboBox<>(loc.getValue(L.LABEL_TYPE));
        typeFilter.setPlaceholder(loc.getValue(L.LABEL_ALL_TYPES));
        typeFilter.setClearButtonVisible(true);
        typeFilter.setItemLabelGenerator(loc::localizeDomainObject);
        typeFilter.addValueChangeListener(e -> refreshGrid());
        toolbar.add(typeFilter);

        Button refreshButton = new Button(loc.getValue(L.LABEL_REFRESH), Solid.SYNC.create(), e -> refresh());
        restoreButton = new Button(loc.getValue(L.LABEL_RESTORE_SELECTED), Solid.UNDO.create(), e -> restoreSelected());
        restoreButton.setThemeName("primary");
        restoreButton.setEnabled(false);

        Div spacer = new Div();
        toolbar.add(spacer, refreshButton, restoreButton);
        toolbar.setFlexGrow(1, spacer);

        grid = new Grid<>(TrashItem.class, false);
        grid.setSizeFull();
        GridMultiSelectionModel<TrashItem> selection = (GridMultiSelectionModel<TrashItem>) grid.setSelectionMode(Grid.SelectionMode.MULTI);
        selection.setSelectAllCheckboxVisibility(GridMultiSelectionModel.SelectAllCheckboxVisibility.VISIBLE);
        grid.setEmptyStateText(loc.getValue(L.MSG_TRASH_EMPTY));
        // selection survives loading other pages of the lazy table, items are identified by what they are
        grid.setItems(new CallbackDataProvider<TrashItem, Void>(this::fetch, query -> count(), TrashItem::key));
        grid.addSelectionListener(e -> restoreButton.setEnabled(!e.getAllSelectedItems().isEmpty()));

        grid.addColumn(item -> loc.localizeDomainObject(item.key().type()))
                .setHeader(loc.getValue(L.LABEL_TYPE)).setFlexGrow(1);
        grid.addColumn(TrashItem::label)
                .setHeader(loc.getValue(L.LABEL_NAME)).setFlexGrow(3);
        if (allUsers) {
            grid.addColumn(TrashItem::ownerLogin)
                    .setHeader(loc.getValue(L.LABEL_USER)).setFlexGrow(1);
        }
        grid.addColumn(item -> item.deletedAt() != null ? loc.getDateHourFormat().format(item.deletedAt()) : "")
                .setHeader(loc.getValue(L.LABEL_SOFT_DELETED)).setFlexGrow(1);
        grid.addComponentColumn(this::contentLink)
                .setHeader("").setFlexGrow(0).setWidth("200px");

        add(description, toolbar, grid);
        setFlexGrow(1, grid);

        if (allUsers) {
            List<User> users = new ArrayList<>(userService.findAll());
            users.sort(Comparator.comparing(User::getLogin, String.CASE_INSENSITIVE_ORDER));
            userFilter.setItems(users);
        }
        typeFilter.setItems(trashService.getTypes().stream()
                .sorted(Comparator.comparing(loc::localizeDomainObject, loc.createLocaleComparator())).toList());
        return this;
    }

    /**
     * Called after objects were restored, so the rest of the UI can show them again.
     */
    public void addRestoreListener(Runnable listener) {
        restoreListeners.add(listener);
    }

    public void refresh() {
        refreshGrid();
    }

    private void refreshGrid() {
        if (grid != null) {
            grid.deselectAll();
            grid.getDataProvider().refreshAll();
        }
    }

    private TrashFilter filter() {
        User user = userFilter != null ? userFilter.getValue() : null;
        return new TrashFilter(user != null ? user.getId() : null, typeFilter.getValue());
    }

    private Stream<TrashItem> fetch(com.vaadin.flow.data.provider.Query<TrashItem, Void> query) {
        try {
            return trashService.find(filter(), query.getOffset(), query.getLimit()).stream();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return Stream.empty();
        }
    }

    private int count() {
        try {
            return (int) Math.min(Integer.MAX_VALUE, trashService.count(filter()));
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return 0;
        }
    }

    private Button contentLink(TrashItem item) {
        if (!item.extendable()) {
            return null;
        }
        Button link = new Button(loc.getValue(L.LABEL_EXTENDED_CONTENT), e -> showExtendedContent(item));
        link.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        return link;
    }

    private void showExtendedContent(TrashItem item) {
        try {
            String json = trashService.getExtendedContent(item.key());
            if (json == null) {
                Notification.warning(loc.getValue(L.MSG_NO_EXTENDED_CONTENT));
                return;
            }
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle(loc.getValue(L.LABEL_EXTENDED_CONTENT) + ": " + item.label());
            dialog.setWidth("800px");
            dialog.setResizable(true);

            TextArea text = new TextArea();
            text.setValue(json);
            text.setReadOnly(true);
            text.setWidthFull();
            text.setHeight("500px");
            text.getStyle().set("font-family", "var(--lumo-font-family-monospace, monospace)");
            dialog.add(text);
            dialog.getFooter().add(new Button(loc.getValue(L.LABEL_CLOSE), e -> dialog.close()));
            dialog.open();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void restoreSelected() {
        List<EntityKey> keys = grid.getSelectedItems().stream().map(TrashItem::key).toList();
        if (keys.isEmpty()) {
            return;
        }
        ConfirmDialog.show(String.format(loc.getValue(L.MSG_CONFIRM_RESTORE), keys.size()), () -> restore(keys));
    }

    private void restore(List<EntityKey> keys) {
        try {
            RestoreResult result = trashService.restore(keys);
            if (result.isBlocked()) {
                showBlocked(result);
                return;
            }
            Notification.success(String.format(loc.getValue(L.MSG_RESTORED), result.getRestored().size()));
            refreshGrid();
            restoreListeners.forEach(Runnable::run);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    private void showBlocked(RestoreResult result) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(loc.getValue(L.LABEL_RESTORE));
        dialog.setWidth("600px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.add(new Span(loc.getValue(L.MSG_RESTORE_BLOCKED)));
        for (Map.Entry<EntityKey, List<Blocker>> blocked : result.getBlocked().entrySet()) {
            EntityKey key = blocked.getKey();
            for (Blocker blocker : blocked.getValue()) {
                content.add(new Span(String.format(loc.getValue(L.MSG_RESTORE_BLOCKED_ITEM),
                        loc.localizeDomainObject(key.type()), result.getBlockedLabels().get(key),
                        loc.localizeDomainObject(blocker.parent().type()), blocker.label(), blocker.field())));
            }
        }
        dialog.add(content);
        dialog.getFooter().add(new Button(loc.getValue(L.LABEL_CLOSE), e -> dialog.close()));
        dialog.open();
    }
}
