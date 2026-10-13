package com.github.enerccio.marginalia.extensions.charactertracker.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.popover.Popover;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Consumer;

/**
 * Lets the user choose lorebook entries to import as characters: a grid of the names of the entries with a checkbox
 * each (the checkbox in the header checks all of them) and an (i) icon that shows the content of the entry.
 */
public class ImportLoreDialog extends Dialog {

    private final List<LorebookEntry> entries;
    private final Grid<LorebookEntry> grid = new Grid<>();
    private final Button importButton;

    /**
     * @param entries  the entries to choose from, in the order they are shown
     * @param onImport called with the names of the checked entries (in the order shown) when the user imports
     */
    public ImportLoreDialog(List<LorebookEntry> entries, Consumer<List<String>> onImport) {
        this.entries = entries.stream().filter(entry -> StringUtils.isNotBlank(entry.getName())).toList();

        setHeaderTitle("Import lore entries as characters");
        setWidth("450px");
        setMaxWidth("95vw");
        setMaxHeight("90vh");

        if (this.entries.isEmpty()) {
            add(new Span("The lorebook of the book has no entries."));
        } else {
            grid.setSelectionMode(Grid.SelectionMode.MULTI);
            grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_COMPACT);
            grid.addColumn(LorebookEntry::getName).setHeader("Name").setFlexGrow(1);
            grid.addComponentColumn(this::createInfoIcon).setFlexGrow(0).setWidth("3.5rem");
            grid.setItems(this.entries);
            grid.setWidthFull();
            grid.setHeight("50vh");
            grid.addSelectionListener(_ -> updateImportButton());
            add(grid);
        }

        importButton = new Button("Import", _ -> {
            // in the order shown, not in the order the selection was made
            onImport.accept(this.entries.stream().filter(grid.getSelectedItems()::contains)
                    .map(LorebookEntry::getName).toList());
            close();
        });
        importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        importButton.setEnabled(false);
        getFooter().add(new Button("Close", _ -> close()), importButton);
    }

    /**
     * The (i) icon of an entry: a click shows the content of the entry in a popover.
     */
    private Icon createInfoIcon(LorebookEntry entry) {
        Icon icon = Solid.INFO_CIRCLE.create();
        icon.addClassName(CssStyles.INFO_ICON);

        Div preview = new Div(new Span(StringUtils.isBlank(entry.getPayload()) ? "(empty)" : entry.getPayload()));
        preview.addClassName(CharacterTrackerStyles.LORE_PREVIEW);
        Popover popover = new Popover(preview);
        popover.setTarget(icon);
        return icon;
    }

    /**
     * Import needs at least one entry; its label shows how many are checked.
     */
    private void updateImportButton() {
        int selected = grid.getSelectedItems().size();
        importButton.setEnabled(selected > 0);
        importButton.setText(selected > 0 ? "Import (" + selected + ")" : "Import");
    }
}
