package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersDefaults;
import com.github.enerccio.marginalia.extensions.betareaders.model.Persona;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersStore;
import com.github.enerccio.marginalia.extensions.betareaders.service.PersonaIcons;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.T;

/**
 * The personas of the library of the user (or, with another title and list, of a book): the list with add, edit, copy, delete and
 * reorder, and a button that adds the starter personas that are not in it.
 */
public class PersonaLibraryDialog extends LibraryDialog<Persona> {

    /**
     * @param title   what the dialog says, like "Persona library"
     * @param source  the personas, which are not changed
     * @param onApply told with the new list after <i>Save</i>
     */
    public PersonaLibraryDialog(String title, List<Persona> source, Consumer<List<Persona>> onApply) {
        super(title, source, Persona::copy, Persona::duplicate, onApply);
        Button starters = new Button(T.localized("Add the starter personas"), Solid.USERS.create(), _ -> addStarters());
        starters.setTooltipText(T.localized("Adds the personas the plugin comes with that are not in the list"));
        addToolbarButton(starters);
        build();
    }

    @Override
    protected void addColumns(Grid<Persona> grid) {
        grid.addComponentColumn(persona -> {
            Image icon = new Image(PersonaIcons.source(persona), persona.getName());
            icon.addClassName(BetaReadersStyles.ICON);
            return icon;
        }).setHeader(T.localized("Icon")).setFlexGrow(0).setWidth("5rem");
        grid.addColumn(Persona::getName).setHeader(T.localized("Name")).setFlexGrow(1).setAutoWidth(true);
        grid.addColumn(persona -> summary(persona.getDescription())).setHeader(T.localized("Description")).setFlexGrow(4);
    }

    @Override
    protected Persona create() {
        return new Persona();
    }

    @Override
    protected void edit(Persona persona, boolean isNew, Runnable done) {
        String header = isNew ? T.localized("New persona") : T.localized("Edit persona");
        createEditor(header, persona, name -> nameTaken(persona, name), done).open();
    }

    /**
     * The window that edits one persona.
     */
    protected PersonaDialog createEditor(String header, Persona persona, Predicate<String> nameTaken, Runnable done) {
        return new PersonaDialog(header, persona, nameTaken, done);
    }

    @Override
    protected String labelOf(Persona persona) {
        return persona.getName();
    }

    private boolean nameTaken(Persona edited, String name) {
        String key = BetaReadersStore.nameKey(name);
        return items().stream().anyMatch(other -> other != edited && BetaReadersStore.nameKey(other.getName()).equals(key));
    }

    private void addStarters() {
        int added = 0;
        for (Persona starter : BetaReadersDefaults.starterPersonas()) {
            if (!nameTaken(starter, starter.getName())) {
                items().add(starter);
                added++;
            }
        }
        refresh();
        if (added == 0) {
            Notification.warning(T.localized("All the starter personas are in the list"));
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
