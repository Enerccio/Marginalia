package com.github.enerccio.marginalia.ui.dialogs.agent;

import com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.TokenizerService;
import com.github.enerccio.marginalia.domain.service.agent.CommandFunctions;
import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.domain.service.function.FunctionDefinition;
import com.github.enerccio.marginalia.domain.traits.BatchPolicy;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The commands an agent may call: a table with a check box per command and a selection that sets them all at once
 * (all, none, all that don't remove data, and the roles auditor, lore keeper, editor and drafter). A command the agent is allowed that is not registered now (the extension
 * that brought it is not loaded) is not shown but stays allowed when the selection is saved.
 */
@Configurable(preConstruction = true)
public class AgentCommandsEditor extends VerticalLayout {

    /**
     * A command as the table shows it.
     */
    record Row(String name, String humanName, String effect, String batch, String description, boolean destructive, boolean readOnly, long tokens) {
    }

    @Autowired
    private Localization loc;

    @Autowired
    private CommandService commandService;

    @Autowired
    private TokenizerService tokenizerService;

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
        presetCombo.setHelperText(loc.getValue(L.HELP_COMMAND_PRESETS));
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
        }).setHeader(loc.getValue(L.LABEL_ENABLED)).setFlexGrow(0).setWidth("100px").setResizable(true);
        grid.addColumn(Row::humanName).setHeader(loc.getValue(L.LABEL_COMMAND)).setFlexGrow(2).setResizable(true);
        grid.addColumn(Row::effect).setHeader(loc.getValue(L.LABEL_COMMAND_EFFECT)).setFlexGrow(1).setResizable(true);
        grid.addColumn(Row::batch).setHeader(loc.getValue(L.LABEL_COMMAND_BATCH_POLICY)).setFlexGrow(0).setWidth("110px")
                .setResizable(true).setTooltipGenerator(row -> loc.getValue(L.HELP_COMMAND_BATCH_POLICY));
        grid.addColumn(Row::tokens).setHeader(loc.getValue(L.LABEL_TOKENS)).setTextAlign(ColumnTextAlign.END).setFlexGrow(0)
                .setWidth("100px").setResizable(true).setTooltipGenerator(row -> loc.getValue(L.HELP_AGENT_COMMAND_TOKENS));
        grid.addColumn(Row::description).setHeader(loc.getValue(L.LABEL_DESCRIPTION)).setFlexGrow(4).setResizable(true);

        setPadding(false);
        setSpacing(false);
        add(presetCombo, grid);
        loadCommands();
    }

    private void loadCommands() {
        Map<String, CommandDefinition> byName = new LinkedHashMap<>();
        // a command is alone in its answer when any of its overloads says so, as the runner takes it
        Set<String> single = new LinkedHashSet<>();
        for (CommandDefinition definition : commandService.getCommands()) {
            if (definition.availableForAgent()) {
                byName.putIfAbsent(definition.commandName(), definition);
                if (definition.batch() == BatchPolicy.SINGLE) {
                    single.add(definition.commandName());
                }
            }
        }
        rows.clear();
        for (CommandDefinition definition : byName.values()) {
            boolean destructive = definition.dataSeverity().isDestructive();
            String description = definition.llmDescription().isEmpty()
                    ? definition.description().get(loc) : definition.llmDescription().get(loc);
            rows.add(new Row(definition.commandName(), definition.humanName().get(loc),
                    loc.getValue(definition.dataSeverity().getLabel()),
                    loc.getValue((single.contains(definition.commandName()) ? BatchPolicy.SINGLE : BatchPolicy.BATCHABLE).getLabel()),
                    description == null ? "" : description, destructive,
                    definition.readOnly(), tokensOf(definition.commandName())));
        }
        rows.sort(Comparator.comparing(Row::humanName, loc.createLocaleComparator()));
        grid.setItems(rows);
    }

    /**
     * What the definition of the command adds to every request of the agent, in tokens: its name, description and parameters as the
     * model reads them (the same estimate the runner makes).
     */
    private long tokensOf(String commandName) {
        try {
            long tokens = 0;
            for (FunctionDefinition function : CommandFunctions.create(commandService, List.of(commandName), DestructiveCommandMode.AUTO,
                    l -> loc.getValue(l), null).getDefinitions()) {
                tokens += tokenizerService.countTokensApprox(function.getName() + " " + StringUtils.defaultString(function.getDescription())
                        + " " + function.getParametersSchema());
            }
            return tokens;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * What the definitions of the ticked commands add to every request of the agent, in tokens (estimated).
     */
    public long getSelectedTokens() {
        return rows.stream().filter(row -> selected.contains(row.name())).mapToLong(Row::tokens).sum();
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
            if (preset.includes(row)) {
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
        for (AgentCommandPreset preset : AgentCommandPreset.values()) {
            Set<String> names = rows.stream().filter(preset::includes).map(Row::name).collect(Collectors.toSet());
            if (selected.equals(names)) {
                return preset;
            }
        }
        return null;
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
