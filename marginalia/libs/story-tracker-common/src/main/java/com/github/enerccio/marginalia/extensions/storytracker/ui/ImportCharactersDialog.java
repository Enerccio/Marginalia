package com.github.enerccio.marginalia.extensions.storytracker.ui;

import com.github.enerccio.marginalia.extensions.storytracker.characters.CharacterTrackerCharacters.Candidate;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.storytracker.ui.StoryTrackerLocalization.*;

/**
 * Lets the user choose characters of the Character Tracker to track: a grid of the characters the book does not track yet (all of
 * them checked at first) and below it the tracker's own options of the import (what the imported characters get).
 */
public class ImportCharactersDialog extends Dialog {

    private final Grid<Candidate> grid = new Grid<>();
    private final Button importButton;
    private final BooleanSupplier optionsValid;

    /**
     * @param all          every character of the Character Tracker in the book
     * @param untracked    those of them that are not tracked yet, the ones to choose from
     * @param options      the tracker's options below the grid, {@code null} for none
     * @param optionsValid whether the options allow an import now; the tracker calls {@link #updateImportButton()} when they change
     * @param onImport     called with the checked characters (in the order shown) when the user imports
     */
    public ImportCharactersDialog(List<Candidate> all, List<Candidate> untracked, Component options, BooleanSupplier optionsValid,
                                  Consumer<List<Candidate>> onImport) {
        this.optionsValid = optionsValid == null ? () -> true : optionsValid;
        setHeaderTitle(T.localized(IMPORT_FROM_THE_CHARACTER_TRACKER));
        setWidth("520px");
        setMaxWidth("95vw");
        setMaxHeight("90vh");

        if (untracked.isEmpty()) {
            add(new Span(all.isEmpty() ? T.localized(THE_CHARACTER_TRACKER_HAS_NO_CHARACTERS)
                    : T.localized(EVERY_CHARACTER_OF_THE_CHARACTER_TRACKER_IS_TRACKED)));
        } else {
            grid.setSelectionMode(Grid.SelectionMode.MULTI);
            grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_COMPACT);
            grid.addColumn(Candidate::name).setHeader(T.localized(NAME)).setFlexGrow(1);
            grid.addColumn(Candidate::tagline).setHeader(T.localized(TAGLINE)).setFlexGrow(2);
            grid.setItems(untracked);
            grid.setWidthFull();
            grid.setHeight("40vh");
            grid.addSelectionListener(_ -> updateImportButton());
            untracked.forEach(grid::select);

            VerticalLayout content = new VerticalLayout(grid);
            if (options != null) {
                content.add(options);
            }
            content.setPadding(false);
            add(content);
        }

        importButton = new Button(T.localized(IMPORT), _ -> {
            // in the order shown, not in the order the selection was made
            List<Candidate> chosen = untracked.stream().filter(grid.getSelectedItems()::contains).toList();
            close();
            onImport.accept(chosen);
        });
        importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button(T.localized(CANCEL), _ -> close()), importButton);
        updateImportButton();
    }

    /** Import needs at least one character and options that allow it; its label shows how many are checked. */
    public void updateImportButton() {
        if (importButton == null) {
            return;
        }
        int selected = grid.getSelectedItems().size();
        importButton.setEnabled(selected > 0 && optionsValid.getAsBoolean());
        importButton.setText(selected > 0 ? T.localized(IMPORT_D).formatted(selected) : T.localized(IMPORT));
    }
}
