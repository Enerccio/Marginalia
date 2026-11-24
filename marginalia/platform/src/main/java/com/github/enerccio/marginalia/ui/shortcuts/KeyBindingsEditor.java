package com.github.enerccio.marginalia.ui.shortcuts;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The key bindings table of the user settings: a row for every registered action with its key, a button that records the
 * next key combination pressed, a reset to the default and a clear. Rows whose key collides with another action or with a
 * key the browser keeps carry a warning. The value is the key of every action (action id to canonical key combination, null
 * for none), so the settings page tracks the changes like those of any other field.
 */
public class KeyBindingsEditor extends CustomField<Map<String, String>> {

    private final Localization loc;
    private final Grid<ShortcutAction> grid = new Grid<>();
    private Map<String, String> keys = new LinkedHashMap<>();

    public KeyBindingsEditor(Localization loc) {
        super(new LinkedHashMap<>());
        this.loc = loc;

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
        grid.setAllRowsVisible(true);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addColumn(action -> action.getLabel(loc)).setHeader(loc.getValue(L.LABEL_ACTION)).setAutoWidth(true).setFlexGrow(1);
        grid.addComponentColumn(this::keyCell).setHeader(loc.getValue(L.LABEL_KEY)).setAutoWidth(true);
        grid.addComponentColumn(this::buttonsCell).setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(this::warningCell).setAutoWidth(true).setFlexGrow(1);
        add(grid);
    }

    @Override
    protected Map<String, String> generateModelValue() {
        return new LinkedHashMap<>(keys);
    }

    @Override
    protected void setPresentationValue(Map<String, String> value) {
        keys = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
        grid.setItems(ShortcutRegistry.getActions());
    }

    private Component keyCell(ShortcutAction action) {
        String key = keys.get(action.getId());
        Span span = new Span(key == null ? loc.getValue(L.LABEL_NO_KEY) : key);
        if (key == null) {
            span.addClassName(CssStyles.TEXT_SECONDARY);
        } else {
            span.addClassName(CssStyles.TEXT_MONOSPACE);
        }
        return span;
    }

    private Component buttonsCell(ShortcutAction action) {
        Button record = new Button(loc.getValue(L.LABEL_RECORD_KEY), event -> startRecording(event.getSource()));
        record.setThemeName("small");
        record.getElement().addEventListener("key-recorded", event -> {
            String combo = KeyCombo.normalize(event.getEventData().get("event.detail").asString());
            if (combo != null) {
                set(action, combo);
            } else {
                grid.getDataProvider().refreshItem(action);
            }
        }).addEventData("event.detail");
        record.getElement().addEventListener("key-record-cancelled", event -> grid.getDataProvider().refreshAll());

        Button reset = new Button(loc.getValue(L.LABEL_RESET_TO_DEFAULT), event -> set(action, action.getDefaultKey()));
        reset.setThemeName("tertiary small");
        reset.setEnabled(!Objects.equals(keys.get(action.getId()), action.getDefaultKey()));

        Button clear = new Button(loc.getValue(L.LABEL_CLEAR), event -> set(action, null));
        clear.setThemeName("tertiary small");
        clear.setEnabled(keys.get(action.getId()) != null);

        return new HorizontalLayout(record, reset, clear);
    }

    private Component warningCell(ShortcutAction action) {
        Div warnings = new Div();
        for (String warning : warningsOf(action)) {
            Span span = new Span(warning);
            span.addClassNames(CssStyles.TEXT_ERROR, CssStyles.TEXT_S);
            warnings.add(span);
        }
        return warnings;
    }

    private List<String> warningsOf(ShortcutAction action) {
        String key = keys.get(action.getId());
        List<String> warnings = new ArrayList<>();
        if (key == null) {
            return warnings;
        }
        if (KeyCombo.isBrowserReserved(key)) {
            warnings.add(loc.getValue(L.MSG_SHORTCUT_BROWSER_KEY));
        }
        if (KeyCombo.isAltCharacter(key)) {
            warnings.add(loc.getValue(L.MSG_SHORTCUT_ALT_CHARACTER));
        }
        warnings.addAll(ShortcutRegistry.getActions().stream()
                .filter(other -> !other.getId().equals(action.getId()) && key.equals(keys.get(other.getId())))
                .map(other -> String.format(loc.getValue(L.MSG_SHORTCUT_USED_BY), other.getLabel(loc)))
                .collect(Collectors.toList()));
        return warnings;
    }

    private void startRecording(Button button) {
        // the button stays: its element receives the recorded key
        button.setText(loc.getValue(L.LABEL_PRESS_KEY));
        JavascriptLibrary.recordKeyCombo(button.getElement());
    }

    private void set(ShortcutAction action, String key) {
        keys.put(action.getId(), key);
        // the warnings of the rows that share the key change too
        grid.getDataProvider().refreshAll();
        updateValue();
    }
}
