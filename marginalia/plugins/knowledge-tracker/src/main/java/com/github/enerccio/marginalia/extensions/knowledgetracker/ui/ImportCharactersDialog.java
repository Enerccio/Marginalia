package com.github.enerccio.marginalia.extensions.knowledgetracker.ui;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.CharacterTrackerImport.Candidate;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.function.BiConsumer;

import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.*;

/**
 * Lets the user choose characters of the Character Tracker to track: a grid of the characters the book does not track yet
 * (all of them checked at first), and whether their relationships count as meetings.
 */
public class ImportCharactersDialog extends Dialog {

    private final Grid<Candidate> grid = new Grid<>();
    private final Button importButton;

    /**
     * @param all       every character of the Character Tracker in the book
     * @param untracked those of them that are not tracked yet, the ones to choose from
     * @param onImport  called with the checked characters (in the order shown) and whether relationships are meetings
     */
    public ImportCharactersDialog(List<Candidate> all, List<Candidate> untracked, BiConsumer<List<Candidate>, Boolean> onImport) {
        setHeaderTitle(T.localized(IMPORT_FROM_THE_CHARACTER_TRACKER));
        setWidth("520px");
        setMaxWidth("95vw");
        setMaxHeight("90vh");

        Checkbox relationsAsMet = new Checkbox(T.localized(RELATIONSHIPS_ARE_MEETINGS));
        relationsAsMet.setHelperText(T.localized(AN_IMPORTED_CHARACTER_HAS_MET_THE_PEOPLE));

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
            grid.setHeight("45vh");
            grid.addSelectionListener(_ -> updateImportButton());
            untracked.forEach(grid::select);

            VerticalLayout content = new VerticalLayout(grid, relationsAsMet);
            content.setPadding(false);
            add(content);
        }

        importButton = new Button(T.localized(IMPORT), _ -> {
            // in the order shown, not in the order the selection was made
            List<Candidate> chosen = untracked.stream().filter(grid.getSelectedItems()::contains).toList();
            close();
            onImport.accept(chosen, relationsAsMet.getValue());
        });
        importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(new Button(T.localized(CANCEL), _ -> close()), importButton);
        updateImportButton();
    }

    /** Import needs at least one character; its label shows how many are checked. */
    private void updateImportButton() {
        if (importButton == null) {
            return;
        }
        int selected = grid.getSelectedItems().size();
        importButton.setEnabled(selected > 0);
        importButton.setText(selected > 0 ? T.localized(IMPORT_D).formatted(selected) : T.localized(IMPORT));
    }
}
