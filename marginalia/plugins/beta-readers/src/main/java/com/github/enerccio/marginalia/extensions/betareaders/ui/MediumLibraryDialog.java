package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersDefaults;
import com.github.enerccio.marginalia.extensions.betareaders.model.Medium;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersStore;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;

import java.util.List;
import java.util.function.Consumer;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.*;

/**
 * The mediums of the user: the list with add, edit, copy, delete and reorder, and a button that brings back the built-in ones that
 * were deleted.
 */
public class MediumLibraryDialog extends LibraryDialog<Medium> {

    /**
     * @param source  the mediums, which are not changed
     * @param onApply told with the new list after <i>Save</i>
     */
    public MediumLibraryDialog(List<Medium> source, Consumer<List<Medium>> onApply) {
        super(T.localized(MEDIUMS), source, Medium::copy, Medium::duplicate, onApply);
        Button builtIn = new Button(T.localized(RESTORE_THE_BUILT_IN_MEDIUMS), Solid.UNDO.create(), _ -> restoreBuiltIn());
        builtIn.setTooltipText(T.localized(ADDS_THE_MEDIUMS_THE_PLUGIN));
        addToolbarButton(builtIn);
        build();
    }

    @Override
    protected void addColumns(Grid<Medium> grid) {
        grid.addColumn(Medium::getName).setHeader(T.localized(NAME)).setFlexGrow(1).setAutoWidth(true);
        grid.addColumn(medium -> summary(medium.getPrompt())).setHeader(T.localized(PROMPT)).setFlexGrow(4);
    }

    @Override
    protected Medium create() {
        return new Medium();
    }

    @Override
    protected void edit(Medium medium, boolean isNew, Runnable done) {
        String header = isNew ? T.localized(NEW_MEDIUM) : T.localized(EDIT_MEDIUM);
        new MediumDialog(header, medium, name -> nameTaken(medium, name), done).open();
    }

    @Override
    protected String labelOf(Medium medium) {
        return medium.getName();
    }

    private boolean nameTaken(Medium edited, String name) {
        String key = BetaReadersStore.nameKey(name);
        return items().stream().anyMatch(other -> other != edited && BetaReadersStore.nameKey(other.getName()).equals(key));
    }

    private void restoreBuiltIn() {
        int added = 0;
        for (Medium builtIn : BetaReadersDefaults.builtInMediums()) {
            // by id: a built-in medium that was renamed is still there
            if (items().stream().noneMatch(medium -> builtIn.getId().equals(medium.getId()))) {
                if (nameTaken(builtIn, builtIn.getName())) {
                    builtIn.setName(builtIn.getName() + " (" + T.localized(BUILT) + ")");
                }
                items().add(builtIn);
                added++;
            }
        }
        refresh();
        if (added == 0) {
            Notification.warning(T.localized(ALL_THE_BUILT_IN_MEDIUMS));
        }
    }

    private static String summary(String text) {
        if (text == null) {
            return "";
        }
        String line = text.replaceAll("\\s+", " ").strip();
        return line.length() > 160 ? line.substring(0, 157) + "..." : line;
    }
}
