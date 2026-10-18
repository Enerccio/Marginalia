package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The commands an agent may call: a table with a check box per command and a selection that sets them all at once
 * (all, none, all that don't change data). A command the agent is allowed that is not registered now (the extension
 * that brought it is not loaded) is not shown but stays allowed when the selection is saved.
 */
@Configurable(preConstruction = true)
public class AgentCommandsEditor extends VerticalLayout {

    /**
     * A command as the table shows it.
     */
    record Row(String name, String humanName, String effect, String description, boolean destructive) {
    }

    @Autowired
    private Localization loc;

    @Autowired
    private CommandService commandService;

    private final ComboBox<AgentCommandPreset> presetCombo;
    private final Grid<Row> grid;
    private final List<Row> rows = new ArrayList<>();
    private final Set<String> selected = new LinkedHashSet<>();
    // allowed names that no command has now
    private final Set<String> unknown = new LinkedHashSet<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private boolean loading;

    public AgentCommandsEditor() {
        presetCombo = new ComboBox<>(loc.getValue(L.LABEL_COMMAND_SELECTION));
        presetCombo.setItems(AgentCommandPreset.values());
        presetCombo.setItemLabelGenerator(preset -> loc.getValue(preset.getLabel()));
        presetCombo.setPlaceholder(loc.getValue(L.LABEL_COMMAND_SELECTION_CUSTOM));
        presetCombo.setClearButtonVisible(false);
        presetCombo.addValueChangeListener(event -> {
            if (!loading && event.isFromClient() && event.getValue() != null) {
                select(event.getValue());
            }
        });

        grid = new Grid<>(Row.class, false);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_ROW_STRIPES);
        grid.setHeight("280px");
        grid.addComponentColumn(row -> {
            Checkbox checkbox = new Checkbox(selected.contains(row.name()));
            checkbox.addValueChangeListener(event -> {
                if (event.getValue()) {
                    selected.add(row.name());
                } else {
                    selected.remove(row.name());
                }
                presetCombo.setValue(currentPreset());
                changed();
            });
            return checkbox;
        }).setHeader(loc.getValue(L.LABEL_ENABLED)).setFlexGrow(0).setWidth("100px");
        grid.addColumn(Row::humanName).setHeader(loc.getValue(L.LABEL_COMMAND)).setFlexGrow(2).setResizable(true);
        grid.addColumn(Row::effect).setHeader(loc.getValue(L.LABEL_COMMAND_EFFECT)).setFlexGrow(1);
        grid.addColumn(Row::description).setHeader(loc.getValue(L.LABEL_DESCRIPTION)).setFlexGrow(4).setResizable(true);

        setPadding(false);
        setSpacing(false);
        add(presetCombo, grid);
        loadCommands();
    }

    private void loadCommands() {
        Map<String, CommandDefinition> byName = new LinkedHashMap<>();
        for (CommandDefinition definition : commandService.getCommands()) {
            byName.putIfAbsent(definition.commandName(), definition);
        }
        rows.clear();
        for (CommandDefinition definition : byName.values()) {
            boolean destructive = definition.dataSeverity().isDestructive();
            String description = definition.llmDescription().isEmpty()
                    ? definition.description().get(loc) : definition.llmDescription().get(loc);
            rows.add(new Row(definition.commandName(), definition.humanName().get(loc),
                    loc.getValue(definition.dataSeverity().getLabel()), description == null ? "" : description, destructive));
        }
        rows.sort(Comparator.comparing(Row::humanName, loc.createLocaleComparator()));
        grid.setItems(rows);
    }

    /**
     * Allows exactly these commands (the names), the table shows the ones that exist.
     */
    public void setAllowed(Collection<String> names) {
        loading = true;
        try {
            Set<String> known = rows.stream().map(Row::name).collect(Collectors.toSet());
            selected.clear();
            unknown.clear();
            for (String name : names) {
                (known.contains(name) ? selected : unknown).add(name);
            }
            presetCombo.setValue(currentPreset());
            grid.getDataProvider().refreshAll();
        } finally {
            loading = false;
        }
    }

    /**
     * The allowed commands: the ticked ones in the order of the table, then the allowed ones that don't exist now.
     */
    public List<String> getAllowed() {
        List<String> allowed = new ArrayList<>();
        rows.stream().map(Row::name).filter(selected::contains).forEach(allowed::add);
        allowed.addAll(unknown);
        return allowed;
    }

    /**
     * Ticks the commands of the preset and clears the others.
     */
    public void select(AgentCommandPreset preset) {
        selected.clear();
        for (Row row : rows) {
            if (preset == AgentCommandPreset.ALL || (preset == AgentCommandPreset.NON_DESTRUCTIVE && !row.destructive())) {
                selected.add(row.name());
            }
        }
        loading = true;
        try {
            presetCombo.setValue(currentPreset());
        } finally {
            loading = false;
        }
        grid.getDataProvider().refreshAll();
        changed();
    }

    /**
     * The preset that the ticked commands are exactly, null for any other selection.
     */
    public AgentCommandPreset getPreset() {
        return currentPreset();
    }

    public boolean isSelected(String commandName) {
        return selected.contains(commandName);
    }

    /**
     * Ticks or clears one command, as the check box in its row does.
     */
    public void setSelected(String commandName, boolean value) {
        if (rows.stream().noneMatch(row -> row.name().equals(commandName))) {
            throw new IllegalArgumentException("No such command: " + commandName);
        }
        if (value) {
            selected.add(commandName);
        } else {
            selected.remove(commandName);
        }
        presetCombo.setValue(currentPreset());
        grid.getDataProvider().refreshAll();
        changed();
    }

    /**
     * Names of the commands in the table.
     */
    public List<String> getCommandNames() {
        return rows.stream().map(Row::name).toList();
    }

    public void onChange(Runnable listener) {
        listeners.add(listener);
    }

    private AgentCommandPreset currentPreset() {
        if (rows.isEmpty()) {
            return null;
        }
        Set<String> all = rows.stream().map(Row::name).collect(Collectors.toSet());
        Set<String> readOnly = rows.stream().filter(row -> !row.destructive()).map(Row::name).collect(Collectors.toSet());
        if (selected.equals(all)) {
            return AgentCommandPreset.ALL;
        }
        if (selected.isEmpty()) {
            return AgentCommandPreset.NONE;
        }
        return selected.equals(readOnly) ? AgentCommandPreset.NON_DESTRUCTIVE : null;
    }

    private void changed() {
        if (!loading) {
            listeners.forEach(Runnable::run);
        }
    }

    ComboBox<AgentCommandPreset> getPresetCombo() {
        return presetCombo;
    }

    Grid<Row> getGrid() {
        return grid;
    }
}
