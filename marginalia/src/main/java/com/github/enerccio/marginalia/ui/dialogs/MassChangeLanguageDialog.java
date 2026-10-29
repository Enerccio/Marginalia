package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.widgets.BookLabels;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

/**
 * Puts one language on many books without opening each. The user picks the language, and the list shows every book by name
 * with its language; the name filter narrows the list, the checkboxes (and the one in the header, which ticks the books that
 * are shown) choose the books. <b>Replace and exit</b> changes the checked books and closes the window. A book an agent
 * works on is locked: it is shown grayed out, and nothing can be done with it.
 */
@Configurable
@Extendable
public class MassChangeLanguageDialog extends Dialog implements LLMInteract {

    /**
     * One book of the list: how it is shown, whether it can be changed, its language now (the one it has with its folders) and whether it is checked.
     */
    static final class Row {
        private final Manuscript book;
        private final String name;
        private final Language language;
        private final boolean locked;
        private boolean checked;

        private Row(Manuscript book, String name, boolean locked, Language language) {
            this.book = book;
            this.name = name;
            this.language = language;
            this.locked = locked;
        }

        Manuscript book() {
            return book;
        }

        String name() {
            return name;
        }

        Language language() {
            return language;
        }

        boolean locked() {
            return locked;
        }

        boolean checked() {
            return checked;
        }
    }

    @Autowired
    private Localization loc;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private FolderService folderService;

    @Autowired
    private AgentRunService agentRunService;

    private final Runnable onChange;
    // the folder whose books are listed, null is the top level
    private final Folder folder;
    // the books listed instead of the folder, null: the folder
    private final List<Manuscript> chosen;
    private Checkbox subfolders;

    private ComboBox<Language> languageBox;
    private TextField nameFilter;
    private Grid<Row> grid;
    private Checkbox allBox;
    private final List<Row> rows = new ArrayList<>();
    // what the name filter lets through, in the order of the rows (by name)
    private final List<Row> shown = new ArrayList<>();

    /**
     * @param onChange told after books were changed, may be null
     */
    public MassChangeLanguageDialog(Runnable onChange) {
        this(onChange, (Folder) null);
    }

    /**
     * @param onChange told after books were changed, may be null
     * @param folder   the folder whose books are listed (with a checkbox for the ones in the folders below it), null is the top level
     */
    public MassChangeLanguageDialog(Runnable onChange, Folder folder) {
        this.onChange = onChange;
        this.folder = folder;
        this.chosen = null;
    }

    /**
     * @param onChange told after books were changed, may be null
     * @param books    the books listed, and only them
     */
    public MassChangeLanguageDialog(Runnable onChange, List<Manuscript> books) {
        this.onChange = onChange;
        this.folder = null;
        this.chosen = List.copyOf(books);
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "hint", type = Span.class),
                    @APILocals(name = "nameColumn", type = Grid.Column.class),
                    @APILocals(name = "filterRow", type = HeaderRow.class),
                    @APILocals(name = "replace", type = Button.class),
                    @APILocals(name = "cancel", type = Button.class),
                    @APILocals(name = "footer", type = HorizontalLayout.class),
                    @APILocals(name = "content", type = VerticalLayout.class)})
    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_MASS_CHANGE_LANGUAGE) + ": " + scopeName());
        setWidth("900px");
        setMaxWidth("96%");
        setHeight("80%");
        setModality(ModalityMode.STRICT);
        setCloseOnOutsideClick(false);
        UIUtils.fitPhone(this, true);

        subfolders = new Checkbox(loc.getValue(L.LABEL_INCLUDE_SUBFOLDERS));
        subfolders.setVisible(chosen == null);
        subfolders.addValueChangeListener(event -> load());

        Span hint = new Span(loc.getValue(L.HELP_MASS_CHANGE_LANGUAGE));
        hint.addClassName(CssStyles.HINT_TEXT);

        languageBox = new ComboBox<>(loc.getValue(L.LABEL_LANGUAGE));
        languageBox.setWidth("260px");
        languageBox.setMaxWidth("100%");
        languageBox.setItems(Language.values());
        languageBox.setItemLabelGenerator(Language::getDisplayName);
        languageBox.setAllowCustomValue(false);
        languageBox.setClearButtonVisible(true);

        allBox = new Checkbox();
        allBox.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                shown.stream().filter(row -> !row.locked).forEach(row -> row.checked = event.getValue());
                grid.getDataProvider().refreshAll();
            }
        });
        UIUtils.addTooltip(allBox, loc.getValue(L.LABEL_SELECT_ALL));

        nameFilter = new TextField();
        nameFilter.setWidthFull();
        nameFilter.setClearButtonVisible(true);
        nameFilter.setValueChangeMode(ValueChangeMode.TIMEOUT);
        nameFilter.setValueChangeTimeout(200);
        nameFilter.addValueChangeListener(event -> applyFilter());

        grid = new Grid<>();
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.addComponentColumn(this::checkbox).setHeader(allBox).setFlexGrow(0).setWidth(UIConstants.TOOL_COLUMN_SIZE);
        Grid.Column<Row> nameColumn = grid.addComponentColumn(row -> text(row, row.name))
                .setHeader(loc.getValue(L.LABEL_NAME)).setFlexGrow(3);
        grid.addComponentColumn(row -> text(row, row.language.getDisplayName()))
                .setHeader(loc.getValue(L.LABEL_LANGUAGE)).setFlexGrow(2);
        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(nameColumn).setComponent(nameFilter);

        Button replace = new Button(loc.getValue(L.LABEL_REPLACE_AND_EXIT), event -> replaceAndExit());
        replace.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button(loc.getValue(L.LABEL_CANCEL), event -> close());
        HorizontalLayout footer = new HorizontalLayout(replace, cancel);
        footer.setWidthFull();
        footer.setWrap(true);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        getFooter().add(footer);

        VerticalLayout content = new VerticalLayout(hint, subfolders, languageBox, grid);
        content.setSizeFull();
        content.setPadding(false);
        content.addClassName(CssStyles.MIN_HEIGHT_0);
        content.expand(grid);
        add(content);

        load();
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = String.class),
            locals = {@APILocals(name = "e", type = Exception.class)})
    private String scopeName() {
        try {
            if (chosen != null) {
                return loc.getValue(L.LABEL_BOOKS) + " (" + chosen.size() + ")";
            }
            return folder == null ? loc.getValue(L.LABEL_TOP_LEVEL) : folderService.getPath(folder);
        } catch (Exception e) {
            return folder.getName();
        }
    }

    /**
     * The books the dialog lists: the ones it was given (read again, without those deleted meanwhile) or those in the folder.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = List.class),
            parameters = {@APIParameter(name = "nested", type = boolean.class)},
            locals = {@APILocals(name = "books", type = List.class),
                    @APILocals(name = "book", type = Manuscript.class),
                    @APILocals(name = "stored", type = Manuscript.class)})
    private List<Manuscript> booksInScope(boolean nested) throws Exception {
        if (chosen == null) {
            return manuscriptService.findRegularInFolder(folder, nested);
        }
        List<Manuscript> books = new ArrayList<>();
        for (Manuscript book : chosen) {
            Manuscript stored = manuscriptService.findForUser(book.getUuid());
            if (stored != null && !stored.isDeleted()) {
                books.add(stored);
            }
        }
        return books;
    }

    /**
     * Reads the books and their languages again. The checks and the filter stay.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "loaded", type = List.class),
                    @APILocals(name = "paths", type = Map.class),
                    @APILocals(name = "book", type = Manuscript.class),
                    @APILocals(name = "bookName", type = String.class),
                    @APILocals(name = "checked", type = Set.class),
                    @APILocals(name = "row", type = MassChangeLanguageDialog.Row.class),
                    @APILocals(name = "e", type = Exception.class)})
    public void load() {
        try {
            List<Row> loaded = new ArrayList<>();
            boolean nested = subfolders != null && subfolders.getValue();
            Map<Long, String> paths = nested ? folderService.getPaths() : Map.of();
            for (Manuscript book : booksInScope(nested)) {
                // with the folders below listed too, a book is told apart by its folder
                String bookName = nested ? BookLabels.label(book, paths) : StringUtils.defaultString(book.getName());
                loaded.add(new Row(book, bookName, agentRunService.findLockingRun(book) != null, manuscriptService.getLanguage(book)));
            }
            Set<Long> checked = new HashSet<>();
            rows.stream().filter(row -> row.checked).forEach(row -> checked.add(row.book.getId()));
            for (Row row : loaded) {
                row.checked = !row.locked && checked.contains(row.book.getId());
            }
            loaded.sort(Comparator.comparing((Row row) -> row.name, String.CASE_INSENSITIVE_ORDER));
            rows.clear();
            rows.addAll(loaded);
            applyFilter();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Shows the books whose name has the filter text in it (in any case). A book that the filter hides is unchecked, so
     * that nothing is changed that the user can not see.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "filter", type = String.class),
                    @APILocals(name = "row", type = MassChangeLanguageDialog.Row.class)})
    private void applyFilter() {
        if (grid == null) {
            return;
        }
        String filter = StringUtils.defaultString(nameFilter.getValue()).trim().toLowerCase(Locale.ROOT);
        shown.clear();
        for (Row row : rows) {
            if (row.name.toLowerCase(Locale.ROOT).contains(filter)) {
                shown.add(row);
            } else {
                row.checked = false;
            }
        }
        grid.setItems(shown);
        updateAllBox();
    }

    /**
     * The header box is ticked when every book that can be changed and is shown is checked.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "changeable", type = List.class)})
    private void updateAllBox() {
        List<Row> changeable = shown.stream().filter(row -> !row.locked).toList();
        allBox.setValue(!changeable.isEmpty() && changeable.stream().allMatch(row -> row.checked));
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = Checkbox.class),
            parameters = {@APIParameter(name = "row", type = MassChangeLanguageDialog.Row.class)},
            locals = {@APILocals(name = "box", type = Checkbox.class)})
    private Checkbox checkbox(Row row) {
        Checkbox box = new Checkbox(row.checked);
        box.setEnabled(!row.locked);
        dimmed(box, row);
        box.addValueChangeListener(event -> {
            row.checked = event.getValue();
            updateAllBox();
        });
        return box;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = Span.class),
            parameters = {@APIParameter(name = "row", type = MassChangeLanguageDialog.Row.class),
                    @APIParameter(name = "value", type = String.class)},
            locals = {@APILocals(name = "text", type = Span.class)})
    private Span text(Row row, String value) {
        Span text = new Span(value);
        if (row.locked) {
            UIUtils.addTooltip(text, loc.getValue(L.LABEL_LOCKED_BY_AGENT));
        }
        return dimmed(text, row);
    }

    /**
     * A locked book is shown grayed out.
     */
    private static <T extends Component> T dimmed(T component, Row row) {
        if (row.locked) {
            component.addClassName(CssStyles.MULTI_TAG_LOCKED);
        }
        return component;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class))
    private void replaceAndExit() {
        if (replace()) {
            close();
        }
    }

    /**
     * Puts the picked language on the checked books. A book that was locked meanwhile is left alone.
     *
     * @return whether the books were changed, false when there was nothing to do (the window stays open then)
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = boolean.class),
            locals = {@APILocals(name = "language", type = Language.class),
                    @APILocals(name = "targets", type = List.class),
                    @APILocals(name = "row", type = MassChangeLanguageDialog.Row.class),
                    @APILocals(name = "book", type = Manuscript.class),
                    @APILocals(name = "e", type = Exception.class)})
    boolean replace() {
        Language language = languageBox.getValue();
        if (language == null) {
            Notification.warning(loc.getValue(L.MSG_MASS_CHANGE_LANGUAGE_PICK_FIRST));
            return false;
        }
        List<Row> targets = shown.stream().filter(row -> row.checked && !row.locked).toList();
        if (targets.isEmpty()) {
            Notification.warning(loc.getValue(L.MSG_TAG_CHECK_FIRST));
            return false;
        }
        int changed = 0;
        try {
            for (Row row : targets) {
                if (agentRunService.findLockingRun(row.book) != null) {
                    continue;
                }
                Manuscript book = manuscriptService.findForUser(row.book.getUuid());
                if (book == null) {
                    continue;
                }
                book.setLanguage(language);
                manuscriptService.save(book);
                changed++;
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        Notification.success(String.format(loc.getValue(L.MSG_MASS_CHANGE_LANGUAGE_DONE), changed));
        if (changed > 0 && onChange != null) {
            onChange.run();
        }
        return true;
    }

    // for the tests of the dialog

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = ComboBox.class))
    ComboBox<Language> getLanguageBox() {
        return languageBox;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = TextField.class))
    TextField getNameFilter() {
        return nameFilter;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = Grid.class))
    Grid<Row> getGrid() {
        return grid;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = List.class))
    List<Row> getRows() {
        return rows;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = List.class))
    List<Row> getShown() {
        return shown;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = Checkbox.class))
    Checkbox getAllBox() {
        return allBox;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "row", type = MassChangeLanguageDialog.Row.class),
                    @APIParameter(name = "checked", type = boolean.class)})
    void check(Row row, boolean checked) {
        row.checked = checked;
        updateAllBox();
    }
}
