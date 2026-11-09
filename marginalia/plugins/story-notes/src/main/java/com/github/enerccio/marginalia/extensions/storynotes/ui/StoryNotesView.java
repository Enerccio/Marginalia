package com.github.enerccio.marginalia.extensions.storynotes.ui;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.storynotes.model.NoteBook;
import com.github.enerccio.marginalia.extensions.storynotes.model.NotePage;
import com.github.enerccio.marginalia.extensions.storynotes.service.StoryNotesService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;

import static com.github.enerccio.marginalia.extensions.storynotes.StoryNotesLocalization.*;

/**
 * The Notes tab of the story sidebar: the pages of the book ordered by title, with a search and a filter by tag. A page
 * opens in {@link NotePageDialog}. The pages are read again whenever they are stored, by the dialog, another tab or a
 * command (an agent, the plot wizard), so the tab shows what the assistants wrote at once.
 */
@Configurable(preConstruction = true)
public class StoryNotesView extends VerticalLayout implements NotePageDialog.Navigator {

    @Autowired
    private Localization loc;

    private final StoryNotesService service;
    private final Manuscript manuscript;
    private NoteBook notes;
    /** Removes the listener that reads the pages again when they are stored, set while the view is attached. */
    private Runnable changeRegistration;

    private final Grid<NotePage> grid = new Grid<>();
    private final Div empty = new Div();
    private final TextField search = new TextField();
    private final Select<String> tagFilter = new Select<>();

    public StoryNotesView(Manuscript manuscript, StoryNotesService service) {
        this.manuscript = manuscript;
        this.service = service;
        this.notes = loadNotes();

        setSizeFull();
        // the grid goes to the edges of the tab, the controls above it keep their padding
        setPadding(false);
        buildUI();
        refresh();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        changeRegistration = service.onChange(manuscript, () -> UIUtils.accessComponent(this, () -> {
            notes = loadNotes();
            refresh();
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

    private NoteBook loadNotes() {
        try {
            // the story view keeps the book it was opened with, the pages may have been saved since
            return service.load(manuscript);
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
            return service.read(manuscript);
        }
    }

    private void buildUI() {
        Button add = new Button(T.localized(ADD), Solid.PLUS.create(), _ -> openNew(null));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        add.setTooltipText(T.localized(ADD_NEW_NOTE));

        tagFilter.setEmptySelectionAllowed(true);
        tagFilter.setEmptySelectionCaption(T.localized(ALL_TAGS));
        tagFilter.setAriaLabel(T.localized(TAGS));
        tagFilter.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                refresh();
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(add, tagFilter);
        buttons.setAlignItems(Alignment.CENTER);
        buttons.setPadding(false);

        search.setPlaceholder(T.localized(SEARCH_NOTES));
        search.setPrefixComponent(Solid.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.setValueChangeTimeout(300);
        search.addValueChangeListener(_ -> refresh());
        search.setWidthFull();
        VerticalLayout controls = new VerticalLayout(buttons, search);
        controls.setPadding(true);
        controls.setSpacing(false);

        empty.addClassName(StoryNotesStyles.EMPTY);

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS);
        grid.addComponentColumn(this::createCard);
        grid.setSizeFull();

        add(controls, empty, grid);
        setFlexGrow(1, grid);
    }

    /** Shows the pages that match the search and the tag. */
    private void refresh() {
        String selectedTag = tagFilter.getValue();
        List<String> tags = notes.allTags();
        tagFilter.setItems(tags);
        if (selectedTag != null && tags.stream().anyMatch(selectedTag::equalsIgnoreCase)) {
            tagFilter.setValue(selectedTag);
        } else {
            selectedTag = null;
        }
        tagFilter.setVisible(!tags.isEmpty());

        String query = search.getValue();
        List<NotePage> pages = StringUtils.isBlank(query) ? notes.sorted() : notes.search(query, false);
        if (selectedTag != null) {
            String tag = selectedTag;
            pages = pages.stream().filter(p -> NoteBook.hasTag(p, tag)).toList();
        }
        grid.setItems(pages);

        boolean none = pages.isEmpty();
        empty.setVisible(none);
        grid.setVisible(!none);
        if (none) {
            empty.setText(notes.size() == 0 ? T.localized(NO_NOTES_YET) : T.localized(NO_NOTE_MATCHES));
        }
    }

    private Component createCard(NotePage page) {
        Span name = new Span(page.getTitle());
        Div titleLine = new Div(name);
        titleLine.addClassName(StoryNotesStyles.TITLE);
        if (!page.isAiVisible()) {
            titleLine.add(flag(Solid.EYE_SLASH.create(), T.localized(HIDDEN_FROM_ASSISTANTS)));
        }
        if (page.isChangedByCommand()) {
            titleLine.add(flag(Solid.WAND_MAGIC_SPARKLES.create(), T.localized(CHANGED_BY_A_COMMAND)));
        }
        Div body = new Div(titleLine);
        body.addClassName(StoryNotesStyles.CARD_BODY);

        String first = NoteBook.firstLine(page.getContent(), 120);
        if (!first.isEmpty()) {
            Div preview = new Div(new Span(first));
            preview.addClassName(StoryNotesStyles.PREVIEW);
            body.add(preview);
        }
        if (!page.getTags().isEmpty()) {
            Div tags = new Div();
            tags.addClassName(StoryNotesStyles.TAGS);
            page.getTags().forEach(t -> {
                Span tag = new Span(t);
                tag.addClassName(StoryNotesStyles.TAG);
                tags.add(tag);
            });
            body.add(tags);
        }
        // only the text opens the note: a click on a button of the card must not
        body.addClickListener(_ -> open(page));

        Button edit = new Button(Solid.PEN.create(), _ -> open(page));
        edit.setAriaLabel(T.localized(EDIT_NOTE));
        Button delete = new Button(Solid.TRASH.create(), _ -> confirmDelete(page));
        delete.setAriaLabel(T.localized(DELETE_NOTE));
        for (Button button : new Button[]{edit, delete}) {
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        }
        Div actions = new Div(edit, delete);
        actions.addClassName(StoryNotesStyles.CARD_ACTIONS);

        Div card = new Div(body, actions);
        card.addClassName(StoryNotesStyles.CARD);
        return card;
    }

    private static Span flag(Component icon, String tooltip) {
        Span flag = new Span(icon);
        flag.addClassName(StoryNotesStyles.FLAG);
        flag.getElement().setAttribute("title", tooltip);
        return flag;
    }

    private void open(NotePage page) {
        new NotePageDialog(service, manuscript, page, null, this).open();
    }

    private void openNew(String title) {
        new NotePageDialog(service, manuscript, null, title, this).open();
    }

    @Override
    public void open(String title) {
        NoteBook current = loadNotes();
        NotePage page = current.byTitle(title);
        if (page != null) {
            open(page);
        } else {
            openNew(title);
        }
    }

    @Override
    public void create(String title) {
        openNew(title);
    }

    private void confirmDelete(NotePage page) {
        ConfirmDialog.show(T.localized(DELETE_THE_NOTE_PAGE).formatted(page.getTitle()), () -> {
            try {
                String uuid = page.getUuid();
                service.update(manuscript, current -> {
                    NotePage stored = current.get(uuid);
                    if (stored != null) {
                        current.remove(stored);
                    }
                    return null;
                });
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        });
    }
}
