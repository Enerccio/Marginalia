package com.github.enerccio.marginalia.extensions.betareaders.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
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

import static com.github.enerccio.marginalia.extensions.betareaders.BetaReadersLocalization.*;

/**
 * The personas of the library of the user (or, with another title and list, of a book): the list with add, edit, copy, delete and
 * reorder, and a button that adds the starter personas that are not in it.
 */
public class PersonaLibraryDialog extends LibraryDialog<Persona> {

    private final PersonaGenerator generator;

    /**
     * @param title   what the dialog says, like "Persona library"
     * @param source  the personas, which are not changed
     * @param onApply told with the new list after <i>Save</i>
     */
    public PersonaLibraryDialog(String title, List<Persona> source, Consumer<List<Persona>> onApply) {
        this(title, source, null, onApply);
    }

    /**
     * @param generator lets the model write a persona in the window of a persona; null for no such button
     */
    public PersonaLibraryDialog(String title, List<Persona> source, PersonaGenerator generator, Consumer<List<Persona>> onApply) {
        super(title, source, Persona::copy, Persona::duplicate, onApply);
        this.generator = generator;
        Button starters = new Button(T.localized(ADD_THE_STARTER_PERSONAS), Solid.USERS.create(), _ -> addStarters());
        starters.setTooltipText(T.localized(ADDS_THE_PERSONAS_THE_PLUGIN));
        addToolbarButton(starters);
        build();
    }

    @Override
    protected void addColumns(Grid<Persona> grid) {
        grid.addComponentColumn(persona -> {
            Image icon = new Image(PersonaIcons.source(persona), persona.getName());
            icon.addClassName(BetaReadersStyles.ICON);
            return icon;
        }).setHeader(T.localized(ICON)).setFlexGrow(0).setWidth("5rem");
        grid.addColumn(Persona::getName).setHeader(T.localized(NAME)).setFlexGrow(1).setAutoWidth(true);
        grid.addColumn(persona -> summary(persona.getDescription())).setHeader(T.localized(DESCRIPTION)).setFlexGrow(4);
    }

    @Override
    protected Persona create() {
        return new Persona();
    }

    @Override
    protected void edit(Persona persona, boolean isNew, Runnable done) {
        String header = isNew ? T.localized(NEW_PERSONA) : T.localized(EDIT_PERSONA);
        createEditor(header, persona, name -> nameTaken(persona, name), done).open();
    }

    /**
     * The window that edits one persona.
     */
    protected PersonaDialog createEditor(String header, Persona persona, Predicate<String> nameTaken, Runnable done) {
        return new PersonaDialog(header, persona, nameTaken, done, generator,
                () -> items().stream().map(Persona::getName).filter(name -> name != null && !name.isBlank()).toList());
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
        for (Persona starter : T.defaults().starterPersonas()) {
            if (!nameTaken(starter, starter.getName())) {
                items().add(starter);
                added++;
            }
        }
        refresh();
        if (added == 0) {
            Notification.warning(T.localized(ALL_THE_STARTER_PERSONAS));
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
