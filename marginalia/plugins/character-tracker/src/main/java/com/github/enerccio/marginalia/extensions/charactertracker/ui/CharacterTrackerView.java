package com.github.enerccio.marginalia.extensions.charactertracker.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.charactertracker.model.CharacterBook;
import com.github.enerccio.marginalia.extensions.charactertracker.model.StoryCharacter;
import com.github.enerccio.marginalia.extensions.charactertracker.service.CharacterTrackerService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Characters tab of the story sidebar: the characters of the book ordered by name, each on its own color, with the
 * dialogs to edit them and the relationship map. The characters are kept in memory while the tab is open and stored
 * in the book after every change.
 */
@Configurable(preConstruction = true)
public class CharacterTrackerView extends VerticalLayout {

    @Autowired
    private Localization loc;

    private final CharacterTrackerService service;
    private final Manuscript manuscript;
    private CharacterBook book;
    /** Removes the listener that reads the characters again when a command changes them, set while the view is attached. */
    private Runnable changeRegistration;
    /**
     * Uuids of the characters whose icon and note are shown; kept here because the grid forgets it when it is reloaded.
     */
    private final Set<String> expanded = new HashSet<>();

    private final Grid<StoryCharacter> grid = new Grid<>();

    public CharacterTrackerView(Manuscript manuscript, CharacterTrackerService service) {
        this.manuscript = manuscript;
        this.service = service;
        this.book = loadBook();

        setSizeFull();
        // the grid goes to the edges of the tab, the buttons above it keep their padding
        setPadding(false);
        buildUI();
        refreshGrid();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // a command (the command bar, an agent) stores the characters behind the back of the view, which keeps them in memory
        changeRegistration = service.onChange(manuscript, () -> UIUtils.accessComponent(this, () -> {
            book = loadBook();
            refreshGrid();
        }));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (changeRegistration != null) {
            changeRegistration.run();
            changeRegistration = null;
        }
        super.onDetach(detachEvent);
    }

    private CharacterBook loadBook() {
        try {
            // the story view keeps the book it was opened with, the characters may have been saved since
            return service.load(manuscript);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return service.read(manuscript);
        }
    }

    private void buildUI() {
        Button add = new Button("Add new character", Solid.PLUS.create(), _ -> openDialog(null));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button map = new Button("Relationship map", Solid.SITEMAP.create(), _ -> new RelationshipMapDialog(book).open());

        MenuBar tools = new MenuBar();
        tools.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
        MenuItem toolsItem = tools.addItem(Solid.WAND_MAGIC_SPARKLES.create());
        toolsItem.getElement().setAttribute("aria-label", "More actions");
        toolsItem.getSubMenu().addItem("Import lore entries as characters", _ -> openImportDialog());

        HorizontalLayout buttons = new HorizontalLayout(add, tools, map);
        buttons.setAlignItems(Alignment.CENTER);
        buttons.setWrap(true);
        buttons.setPadding(true);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        grid.setDetailsVisibleOnClick(false);
        grid.addComponentColumn(this::createCard);
        grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createDetails));
        grid.setSizeFull();

        add(buttons, grid);
        setFlexGrow(1, grid);
    }

    private void refreshGrid() {
        grid.setItems(book.sortedCharacters());
        for (StoryCharacter character : book.sortedCharacters()) {
            if (expanded.contains(character.getUuid())) {
                grid.setDetailsVisible(character, true);
            }
        }
    }

    private void openDialog(StoryCharacter character) {
        new CharacterDialog(book, character, this::persist).open();
    }

    private void openImportDialog() {
        try {
            new ImportLoreDialog(service.collectLoreEntries(manuscript), this::importCharacters).open();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Creates a character with the name of each lore entry.
     */
    private void importCharacters(List<String> names) {
        for (String name : names) {
            StoryCharacter character = new StoryCharacter();
            character.setName(name.trim());
            book.put(character);
        }
        persist();
        Notification.success("Imported " + names.size() + " characters");
    }

    /**
     * Stores the book after a change and shows it.
     */
    private void persist() {
        try {
            service.save(manuscript, book);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        refreshGrid();
    }

    private Component createCard(StoryCharacter character) {
        Div name = new Div(new Span(character.getName()));
        name.addClassName(CharacterTrackerStyles.NAME);
        Div body = new Div(name);
        body.addClassName(CharacterTrackerStyles.CARD_BODY);
        if (StringUtils.isNotBlank(character.getTagline())) {
            Div tagline = new Div(new Span(character.getTagline()));
            tagline.addClassName(CharacterTrackerStyles.TAGLINE);
            body.add(tagline);
        }

        Button toggle = new Button();
        toggle.setIcon(toggleIcon(character));
        toggle.addClickListener(_ -> {
            boolean show = !grid.isDetailsVisible(character);
            grid.setDetailsVisible(character, show);
            if (show) {
                expanded.add(character.getUuid());
            } else {
                expanded.remove(character.getUuid());
            }
            toggle.setIcon(toggleIcon(character));
        });
        toggle.setAriaLabel("Show icon and note");
        Button edit = new Button(Solid.PEN.create(), _ -> openDialog(character));
        edit.setAriaLabel("Edit character");
        Button delete = new Button(Solid.TRASH.create(), _ -> confirmDelete(character));
        delete.setAriaLabel("Delete character");
        for (Button button : new Button[]{toggle, edit, delete}) {
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        }
        Div actions = new Div(toggle, edit, delete);
        actions.addClassName(CharacterTrackerStyles.CARD_ACTIONS);

        Div card = new Div(body, actions);
        card.addClassName(CharacterTrackerStyles.CARD);
        applyColors(card, character);
        return card;
    }

    private Component toggleIcon(StoryCharacter character) {
        return (expanded.contains(character.getUuid()) ? Solid.CHEVRON_UP : Solid.CHEVRON_DOWN).create();
    }

    /**
     * Icon and note of the expanded character, on its color.
     */
    private Component createDetails(StoryCharacter character) {
        Div details = new Div();
        details.addClassNames(CharacterTrackerStyles.CARD, CharacterTrackerStyles.DETAILS);
        applyColors(details, character);

        String source = CharacterTrackerService.iconSource(character.getIcon());
        if (source != null) {
            Image icon = new Image(source, character.getName());
            icon.addClassName(CharacterTrackerStyles.ICON);
            details.add(icon);
        }
        if (StringUtils.isNotBlank(character.getNote())) {
            Div note = new Div(new Span(character.getNote()));
            note.addClassName(CharacterTrackerStyles.NOTE);
            details.add(note);
        }
        if (source == null && StringUtils.isBlank(character.getNote())) {
            details.add(new Span("No icon or note"));
        }
        return details;
    }

    /**
     * The color is different for every character, so it can't be a class: the rules of the plugin's CSS use the
     * custom properties set here.
     */
    private static void applyColors(Component component, StoryCharacter character) {
        String color = CharacterTrackerService.safeColor(character.getColor());
        component.getElement().getStyle().set(CharacterTrackerStyles.VAR_COLOR, color);
        component.getElement().getStyle().set(CharacterTrackerStyles.VAR_TEXT, CharacterTrackerService.textColor(color));
    }

    private void confirmDelete(StoryCharacter character) {
        ConfirmDialog.show("Delete the character " + character.getName() + " and all its relationships?", () -> {
            book.delete(character);
            expanded.remove(character.getUuid());
            persist();
        });
    }
}
