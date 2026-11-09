package com.github.enerccio.marginalia.ui.notifications;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.NotificationService;
import com.github.enerccio.marginalia.domain.service.NotificationService.Entry;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.*;

/**
 * The notifications of the user ({@link NotificationService}): a switch for all of them, which turns them on in general and enables the
 * button that registers this browser (asks it for the permission, which belongs to the browser and not to the user; the check box itself
 * never asks), and the list of what notifies (a generation, an annotation, the automatic annotation... and what extensions add), each
 * chosen on its own. The list has a filter by name and buttons that check or uncheck what the filter shows.
 * <p>
 * Like {@code PartStyleEditor} it is a part of the settings page, which saves and discards for it: edits only fill the fields and tell the
 * page ({@link #setOnChange}); {@link #save} is called by the save of the page, {@link #reload} by its refresh (the discard).
 */
public class NotificationSettingsEditor extends VerticalLayout {

    private static final String PERMISSION_EVENT = "marginalia-notification-permission";

    private final Localization loc;
    private final NotificationService service;

    private final Checkbox enabled = new Checkbox();
    private final Span permission = new Span();
    private final Button register = new Button();
    private final Button test = new Button();
    private final TextField filter = new TextField();
    private final Grid<Entry> grid = new Grid<>(Entry.class, false);
    private final ListDataProvider<Entry> entries = new ListDataProvider<>(new ArrayList<>());
    private final Span count = new Span();
    private final VerticalLayout list = new VerticalLayout();

    // the state of the permission of this browser, as it said last: granted, denied, default or unsupported; null before it said
    private String state;
    private Runnable onChange = () -> {
    };
    // true while the fields are filled: their listeners must not count that as a change
    private boolean loading;

    public NotificationSettingsEditor(Localization loc, NotificationService service) {
        this.loc = loc;
        this.service = service;
        setPadding(false);
        setWidthFull();

        Span info = new Span(loc.getValue(L.MSG_NOTIFICATIONS_INFO));
        info.addClassName(CssStyles.HINT_TEXT);

        enabled.setLabel(loc.getValue(L.LABEL_NOTIFICATIONS_ENABLE));
        // the check box is the switch of the user: it turns notifications on in general and lets this browser be registered
        enabled.addValueChangeListener(event -> {
            list.setEnabled(event.getValue());
            register.setEnabled(event.getValue());
            test.setEnabled(event.getValue());
            changed();
        });
        // only the button asks the browser for the permission (the permission belongs to the browser, not to the user), from the click
        // itself: the script tells what the browser answered
        register.setText(loc.getValue(L.LABEL_NOTIFICATIONS_REGISTER));
        register.setEnabled(false);
        register.getElement().addEventListener(PERMISSION_EVENT, event -> permissionChanged(
                event.getEventData().get("event.detail.state").asString(),
                event.getEventData().get("event.detail.requested").asBoolean()))
                .addEventData("event.detail.state").addEventData("event.detail.requested");
        addAttachListener(event -> JavascriptLibrary.watchNotificationPermission(register.getElement()));
        addDetachListener(event -> JavascriptLibrary.unwatchNotificationPermission(register.getElement()));

        permission.addClassName(CssStyles.HINT_TEXT);
        test.setText(loc.getValue(L.LABEL_NOTIFICATIONS_TEST));
        test.setEnabled(false);
        test.addClickListener(event -> sendTest());
        HorizontalLayout switchRow = new HorizontalLayout(enabled, register, test);
        switchRow.setAlignItems(Alignment.CENTER);

        filter.setPlaceholder(loc.getValue(L.LABEL_TOOLS_FILTER));
        filter.setPrefixComponent(VaadinIcon.SEARCH.create());
        filter.setClearButtonVisible(true);
        filter.setValueChangeMode(ValueChangeMode.LAZY);
        filter.addClassName(CssStyles.FULL_WIDTH);
        filter.addValueChangeListener(event -> applyFilter());

        grid.setDataProvider(entries);
        GridMultiSelectionModel<Entry> selection = (GridMultiSelectionModel<Entry>) grid.setSelectionMode(Grid.SelectionMode.MULTI);
        // "select all" is the buttons below: they act on what the filter shows, and say so
        selection.setSelectAllCheckboxVisibility(GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);
        grid.addColumn(Entry::name).setHeader(loc.getValue(L.LABEL_NAME));
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("min(480px, 60vh)");
        grid.addClassName(CssStyles.MIN_HEIGHT_0);
        grid.setWidthFull();
        grid.addSelectionListener(event -> {
            updateCount();
            changed();
        });

        Button checkShown = new Button(loc.getValue(L.LABEL_TOOLS_SELECT_SHOWN), event -> checkShown());
        Button clearShown = new Button(loc.getValue(L.LABEL_TOOLS_CLEAR_SHOWN), event -> uncheckShown());
        count.addClassName(CssStyles.HINT_TEXT);
        HorizontalLayout buttons = new HorizontalLayout(checkShown, clearShown, count);
        buttons.setAlignItems(Alignment.CENTER);

        list.setPadding(false);
        list.setWidthFull();
        list.addClassName(CssStyles.GAP_S);
        list.add(filter, grid, buttons);

        add(info, switchRow, permission, list);
        showPermission();
    }

    /**
     * @param onChange called when the user changes something (not when the editor fills its fields)
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange == null ? () -> {
        } : onChange;
    }

    /**
     * Shows the settings as they are stored now, dropping what was edited. The list shows the sources that are registered now.
     */
    public void reload() throws Exception {
        loading = true;
        try {
            List<Entry> all = service.entries();
            entries.getItems().clear();
            entries.getItems().addAll(all);
            entries.refreshAll();
            Set<String> chosen = service.getChosen();
            Set<Entry> selected = new LinkedHashSet<>();
            all.stream().filter(entry -> chosen.contains(entry.key())).forEach(selected::add);
            grid.asMultiSelect().setValue(selected);
            filter.clear();
            entries.clearFilters();
            enabled.setValue(service.isEnabled());
            list.setEnabled(enabled.getValue());
            register.setEnabled(enabled.getValue());
            test.setEnabled(enabled.getValue());
            updateCount();
        } finally {
            loading = false;
        }
    }

    /**
     * Stores the settings.
     */
    public void save() throws Exception {
        service.save(enabled.getValue(), getCheckedKeys());
    }

    /**
     * Checks the entries the filter shows now; the ones it hides stay as they are.
     */
    public void checkShown() {
        grid.asMultiSelect().updateSelection(new LinkedHashSet<>(shown()), Set.of());
    }

    /**
     * Unchecks the entries the filter shows now; the ones it hides stay as they are.
     */
    public void uncheckShown() {
        grid.asMultiSelect().updateSelection(Set.of(), new LinkedHashSet<>(shown()));
    }

    /**
     * The entries the filter shows now, in the order of the list.
     */
    public List<Entry> shown() {
        return entries.fetch(new Query<>()).toList();
    }

    public Checkbox getEnabledField() {
        return enabled;
    }

    public Button getRegisterButton() {
        return register;
    }

    public Button getTestButton() {
        return test;
    }

    public TextField getFilterField() {
        return filter;
    }

    /**
     * The keys of the entries that are checked, the ones the filter hides included.
     */
    public Set<String> getCheckedKeys() {
        Set<String> keys = new LinkedHashSet<>();
        grid.getSelectedItems().forEach(entry -> keys.add(entry.key()));
        return keys;
    }

    private void applyFilter() {
        String needle = filter.getValue() == null ? "" : filter.getValue().strip().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) {
            entries.clearFilters();
        } else {
            entries.setFilter(entry -> entry.name().toLowerCase(Locale.ROOT).contains(needle));
        }
    }

    private void updateCount() {
        count.setText(String.format(loc.getValue(L.LABEL_TOOLS_CHECKED), grid.getSelectedItems().size(), entries.getItems().size()));
    }

    private void changed() {
        if (!loading) {
            onChange.run();
        }
    }

    /**
     * The browser told what it lets the page do. After a click on the register button that did not get the permission the reason is said;
     * the check box stays as the user left it, it is their switch and not the state of this browser.
     */
    private void permissionChanged(String state, boolean requested) {
        this.state = state;
        showPermission();
        if (requested && !"granted".equals(state)) {
            String reason = stateText(state);
            if (reason != null) {
                Notification.warning(reason);
            }
        }
    }

    private void showPermission() {
        String text = stateText(state);
        permission.setText(text == null ? "" : text);
        permission.setVisible(text != null);
    }

    private String stateText(String state) {
        if (state == null) {
            return null;
        }
        return switch (state) {
            case "granted" -> loc.getValue(L.MSG_NOTIFICATIONS_PERMISSION_GRANTED);
            case "denied" -> loc.getValue(L.MSG_NOTIFICATIONS_PERMISSION_DENIED);
            case "default" -> loc.getValue(L.MSG_NOTIFICATIONS_PERMISSION_DEFAULT);
            case "unsupported" -> loc.getValue(L.MSG_NOTIFICATIONS_UNSUPPORTED);
            default -> null;
        };
    }

    /**
     * Shows a notification in this browser, also while the page is in front, to see that it works.
     */
    private void sendTest() {
        NotificationService.Notice notice = service.test();
        getUI().ifPresent(ui -> JavascriptLibrary.showDesktopNotification(ui.getPage(), notice.title(), notice.body(), notice.tag(), true)
                .then(String.class, result -> {
                    if (!"shown".equals(result)) {
                        String reason = stateText(result);
                        Notification.warning(reason == null ? loc.getValue(L.MSG_NOTIFICATIONS_TEST_FAILED) : reason);
                    }
                }));
    }
}
