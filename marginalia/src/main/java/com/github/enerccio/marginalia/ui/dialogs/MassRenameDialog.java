package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptLockedException;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.widgets.BookLabels;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.MassRename;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

/**
 * Renames many books without opening each. The user picks how (a text before the name, a text after it, a text replaced in it,
 * or a numbering), and the list shows every book by name with the name it would get; the name filter narrows the list, the
 * checkboxes (and the one in the header, which ticks the books that are shown) choose the books. <b>Rename</b> renames the
 * checked books and closes the window. A numbering counts the checked books in the order of the list. A book an agent works
 * on is locked: it is shown grayed out, and nothing can be done with it.
 */
@Configurable
@Extendable
public class MassRenameDialog extends Dialog implements LLMInteract {

    /**
     * How the names are changed.
     */
    public enum Mode {
        PREFIX(L.LABEL_RENAME_ADD_PREFIX),
        POSTFIX(L.LABEL_RENAME_ADD_POSTFIX),
        REPLACE(L.LABEL_RENAME_FIND_REPLACE),
        NUMBER(L.LABEL_RENAME_NUMBER);

        private final L label;

        Mode(L label) {
            this.label = label;
        }

        L label() {
            return label;
        }
    }

    /**
     * One book of the list: how it is shown, whether it can be renamed, whether it is checked and the name it would get.
     */
    static final class Row {
        private final Manuscript book;
        private final String label;
        private final String name;
        private final boolean locked;
        private boolean checked;
        private String preview;

        private Row(Manuscript book, String label, boolean locked) {
            this.book = book;
            this.label = label;
            this.name = StringUtils.defaultString(book.getName());
            this.locked = locked;
        }

        Manuscript book() {
            return book;
        }

        /**
         * How the list shows the book (with its folder when the folders below are listed too), by which it is sorted.
         */
        String label() {
            return label;
        }

        /**
         * The name of the book.
         */
        String name() {
            return name;
        }

        boolean locked() {
            return locked;
        }

        boolean checked() {
            return checked;
        }

        /**
         * The name the book would get, null when it is not checked or the settings are not complete.
         */
        String preview() {
            return preview;
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

    private RadioButtonGroup<Mode> modeBox;
    private TextField textBox;
    private TextField findBox;
    private TextField withBox;
    private Checkbox caseBox;
    private TextField patternBox;
    private IntegerField startBox;
    private IntegerField stepBox;
    private IntegerField digitsBox;
    private TextField nameFilter;
    private Grid<Row> grid;
    private Checkbox allBox;
    private final List<Row> rows = new ArrayList<>();
    // what the name filter lets through, in the order of the rows (by name)
    private final List<Row> shown = new ArrayList<>();

    /**
     * @param onChange told after books were renamed, may be null
     */
    public MassRenameDialog(Runnable onChange) {
        this(onChange, (Folder) null);
    }

    /**
     * @param onChange told after books were renamed, may be null
     * @param folder   the folder whose books are listed (with a checkbox for the ones in the folders below it), null is the top level
     */
    public MassRenameDialog(Runnable onChange, Folder folder) {
        this.onChange = onChange;
        this.folder = folder;
        this.chosen = null;
    }

    /**
     * @param onChange told after books were renamed, may be null
     * @param books    the books listed, and only them
     */
    public MassRenameDialog(Runnable onChange, List<Manuscript> books) {
        this.onChange = onChange;
        this.folder = null;
        this.chosen = List.copyOf(books);
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "hint", type = Span.class),
                    @APILocals(name = "fields", type = HorizontalLayout.class),
                    @APILocals(name = "nameColumn", type = Grid.Column.class),
                    @APILocals(name = "filterRow", type = HeaderRow.class),
                    @APILocals(name = "rename", type = Button.class),
                    @APILocals(name = "cancel", type = Button.class),
                    @APILocals(name = "footer", type = HorizontalLayout.class),
                    @APILocals(name = "content", type = VerticalLayout.class)})
    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_MASS_RENAME) + ": " + scopeName());
        setWidth("900px");
        setMaxWidth("96%");
        setHeight("80%");
        setModality(ModalityMode.STRICT);
        setCloseOnOutsideClick(false);
        UIUtils.fitPhone(this, true);

        subfolders = new Checkbox(loc.getValue(L.LABEL_INCLUDE_SUBFOLDERS));
        subfolders.setVisible(chosen == null);
        subfolders.addValueChangeListener(event -> load());

        Span hint = new Span(loc.getValue(L.HELP_MASS_RENAME));
        hint.addClassName(CssStyles.HINT_TEXT);

        modeBox = new RadioButtonGroup<>();
        modeBox.setItems(Mode.values());
        modeBox.setItemLabelGenerator(mode -> loc.getValue(mode.label()));
        modeBox.setValue(Mode.PREFIX);
        modeBox.addValueChangeListener(event -> modeChanged());

        textBox = field(loc.getValue(L.LABEL_RENAME_TEXT_TO_ADD), "260px");
        findBox = field(loc.getValue(L.LABEL_RENAME_FIND), "220px");
        withBox = field(loc.getValue(L.LABEL_REPLACE_WITH), "220px");
        caseBox = new Checkbox(loc.getValue(L.LABEL_CASE_SENSITIVE));
        caseBox.addValueChangeListener(event -> preview());
        patternBox = field(loc.getValue(L.LABEL_RENAME_PATTERN), "260px");
        patternBox.setValue(MassRename.NAME + " " + MassRename.NUMBER);
        patternBox.setHelperText(MassRename.NAME + ", " + MassRename.NUMBER);
        startBox = numberField(loc.getValue(L.LABEL_RENAME_START_AT), 1, null);
        stepBox = numberField(loc.getValue(L.LABEL_RENAME_STEP), 1, null);
        digitsBox = numberField(loc.getValue(L.LABEL_RENAME_DIGITS), 1, 12);
        digitsBox.setMin(1);

        HorizontalLayout fields = new HorizontalLayout(textBox, findBox, withBox, caseBox, patternBox, startBox, stepBox, digitsBox);
        fields.setWrap(true);
        fields.setAlignItems(FlexComponent.Alignment.BASELINE);
        fields.setWidthFull();

        allBox = new Checkbox();
        allBox.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                shown.stream().filter(row -> !row.locked).forEach(row -> row.checked = event.getValue());
                preview();
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
        Grid.Column<Row> nameColumn = grid.addComponentColumn(row -> text(row, row.label))
                .setHeader(loc.getValue(L.LABEL_NAME)).setFlexGrow(3);
        grid.addComponentColumn(row -> text(row, StringUtils.defaultString(row.preview)))
                .setHeader(loc.getValue(L.LABEL_NEW_NAME)).setFlexGrow(3);
        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(nameColumn).setComponent(nameFilter);

        Button rename = new Button(loc.getValue(L.LABEL_RENAME), event -> renameAndExit());
        rename.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancel = new Button(loc.getValue(L.LABEL_CANCEL), event -> close());
        HorizontalLayout footer = new HorizontalLayout(rename, cancel);
        footer.setWidthFull();
        footer.setWrap(true);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        getFooter().add(footer);

        VerticalLayout content = new VerticalLayout(hint, subfolders, modeBox, fields, grid);
        content.setSizeFull();
        content.setPadding(false);
        content.addClassName(CssStyles.MIN_HEIGHT_0);
        content.expand(grid);
        add(content);

        modeChanged();
        load();
    }

    /**
     * A text field that makes the new names again 200 ms after the last key.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = TextField.class),
            parameters = {@APIParameter(name = "label", type = String.class),
                    @APIParameter(name = "width", type = String.class)},
            locals = {@APILocals(name = "field", type = TextField.class)})
    private TextField field(String label, String width) {
        TextField field = new TextField(label);
        field.setWidth(width);
        field.setMaxWidth("100%");
        field.setValueChangeMode(ValueChangeMode.TIMEOUT);
        field.setValueChangeTimeout(200);
        field.addValueChangeListener(event -> preview());
        return field;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = IntegerField.class),
            parameters = {@APIParameter(name = "label", type = String.class),
                    @APIParameter(name = "value", type = int.class),
                    @APIParameter(name = "max", type = Integer.class)},
            locals = {@APILocals(name = "field", type = IntegerField.class)})
    private IntegerField numberField(String label, int value, Integer max) {
        IntegerField field = new IntegerField(label);
        field.setWidth("110px");
        field.setStepButtonsVisible(true);
        field.setValue(value);
        if (max != null) {
            field.setMax(max);
        }
        field.setValueChangeMode(ValueChangeMode.TIMEOUT);
        field.setValueChangeTimeout(200);
        field.addValueChangeListener(event -> preview());
        return field;
    }

    /**
     * Shows the fields of the picked way to rename.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "mode", type = Mode.class)})
    private void modeChanged() {
        Mode mode = modeBox.getValue();
        textBox.setVisible(mode == Mode.PREFIX || mode == Mode.POSTFIX);
        findBox.setVisible(mode == Mode.REPLACE);
        withBox.setVisible(mode == Mode.REPLACE);
        caseBox.setVisible(mode == Mode.REPLACE);
        patternBox.setVisible(mode == Mode.NUMBER);
        startBox.setVisible(mode == Mode.NUMBER);
        stepBox.setVisible(mode == Mode.NUMBER);
        digitsBox.setVisible(mode == Mode.NUMBER);
        preview();
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
     * Reads the books again. The checks and the filter stay.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "loaded", type = List.class),
                    @APILocals(name = "paths", type = Map.class),
                    @APILocals(name = "book", type = Manuscript.class),
                    @APILocals(name = "label", type = String.class),
                    @APILocals(name = "checked", type = Set.class),
                    @APILocals(name = "row", type = MassRenameDialog.Row.class),
                    @APILocals(name = "e", type = Exception.class)})
    public void load() {
        try {
            List<Row> loaded = new ArrayList<>();
            boolean nested = subfolders != null && subfolders.getValue();
            Map<Long, String> paths = nested ? folderService.getPaths() : Map.of();
            for (Manuscript book : booksInScope(nested)) {
                // with the folders below listed too, a book is told apart by its folder
                String label = nested ? BookLabels.label(book, paths) : StringUtils.defaultString(book.getName());
                loaded.add(new Row(book, label, agentRunService.findLockingRun(book) != null));
            }
            Set<Long> checked = new HashSet<>();
            rows.stream().filter(row -> row.checked).forEach(row -> checked.add(row.book.getId()));
            for (Row row : loaded) {
                row.checked = !row.locked && checked.contains(row.book.getId());
            }
            loaded.sort(Comparator.comparing((Row row) -> row.label, String.CASE_INSENSITIVE_ORDER));
            rows.clear();
            rows.addAll(loaded);
            applyFilter();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Shows the books whose name has the filter text in it (in any case). A book that the filter hides is unchecked, so
     * that nothing is renamed that the user can not see.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "filter", type = String.class),
                    @APILocals(name = "row", type = MassRenameDialog.Row.class)})
    private void applyFilter() {
        if (grid == null) {
            return;
        }
        String filter = StringUtils.defaultString(nameFilter.getValue()).trim().toLowerCase(Locale.ROOT);
        shown.clear();
        for (Row row : rows) {
            if (row.label.toLowerCase(Locale.ROOT).contains(filter)) {
                shown.add(row);
            } else {
                row.checked = false;
                row.preview = null;
            }
        }
        preview();
        grid.setItems(shown);
        updateAllBox();
    }

    /**
     * The header box is ticked when every book that can be renamed and is shown is checked.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "changeable", type = List.class)})
    private void updateAllBox() {
        List<Row> changeable = shown.stream().filter(row -> !row.locked).toList();
        allBox.setValue(!changeable.isEmpty() && changeable.stream().allMatch(row -> row.checked));
    }

    /**
     * What is missing in the settings of the way to rename, as the message to show; null when they are complete.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = L.class),
            locals = {@APILocals(name = "mode", type = Mode.class)})
    private L problem() {
        Mode mode = modeBox.getValue();
        boolean empty = switch (mode) {
            case PREFIX, POSTFIX -> StringUtils.isEmpty(textBox.getValue());
            case REPLACE -> StringUtils.isEmpty(findBox.getValue());
            case NUMBER -> StringUtils.isEmpty(patternBox.getValue());
        };
        if (empty) {
            return L.MSG_MASS_RENAME_FILL_FIRST;
        }
        if (mode == Mode.NUMBER && !MassRename.hasNumber(patternBox.getValue())) {
            return L.MSG_MASS_RENAME_PATTERN_NEEDS_NUMBER;
        }
        return null;
    }

    /**
     * The way to rename that the fields describe, call it only when {@link #problem()} is null.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = MassRename.class),
            locals = {@APILocals(name = "mode", type = Mode.class)})
    private MassRename rule() {
        Mode mode = modeBox.getValue();
        return switch (mode) {
            case PREFIX -> MassRename.prefix(textBox.getValue());
            case POSTFIX -> MassRename.postfix(textBox.getValue());
            case REPLACE -> MassRename.replace(findBox.getValue(), withBox.getValue(), caseBox.getValue());
            case NUMBER -> MassRename.number(patternBox.getValue(), valueOf(startBox, 1), valueOf(stepBox, 1), valueOf(digitsBox, 1));
        };
    }

    private static int valueOf(IntegerField field, int empty) {
        return field.getValue() == null ? empty : field.getValue();
    }

    /**
     * Works out the new names of the checked books, in the order of the list, and shows the ones that changed.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "rule", type = MassRename.class),
                    @APILocals(name = "row", type = MassRenameDialog.Row.class),
                    @APILocals(name = "before", type = String.class)})
    private void preview() {
        if (grid == null) {
            return;
        }
        MassRename rule = problem() == null ? rule() : null;
        int index = 0;
        for (Row row : shown) {
            String before = row.preview;
            row.preview = rule != null && row.checked && !row.locked ? rule.rename(row.name, index++) : null;
            if (!Objects.equals(before, row.preview)) {
                grid.getDataProvider().refreshItem(row);
            }
        }
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = Checkbox.class),
            parameters = {@APIParameter(name = "row", type = MassRenameDialog.Row.class)},
            locals = {@APILocals(name = "box", type = Checkbox.class)})
    private Checkbox checkbox(Row row) {
        Checkbox box = new Checkbox(row.checked);
        box.setEnabled(!row.locked);
        dimmed(box, row);
        box.addValueChangeListener(event -> {
            row.checked = event.getValue();
            preview();
            updateAllBox();
        });
        return box;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = Span.class),
            parameters = {@APIParameter(name = "row", type = MassRenameDialog.Row.class),
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
    private void renameAndExit() {
        if (rename()) {
            close();
        }
    }

    /**
     * Renames the checked books. A book that was locked meanwhile is left alone. Nothing is done when a name would be empty.
     *
     * @return whether books were renamed, false when there was nothing to do (the window stays open then)
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = boolean.class),
            locals = {@APILocals(name = "problem", type = L.class),
                    @APILocals(name = "targets", type = List.class),
                    @APILocals(name = "rule", type = MassRename.class),
                    @APILocals(name = "names", type = Map.class),
                    @APILocals(name = "row", type = MassRenameDialog.Row.class),
                    @APILocals(name = "name", type = String.class),
                    @APILocals(name = "entry", type = Map.Entry.class),
                    @APILocals(name = "e", type = Exception.class)})
    boolean rename() {
        L problem = problem();
        if (problem != null) {
            Notification.warning(loc.getValue(problem));
            return false;
        }
        List<Row> targets = shown.stream().filter(row -> row.checked && !row.locked).toList();
        if (targets.isEmpty()) {
            Notification.warning(loc.getValue(L.MSG_TAG_CHECK_FIRST));
            return false;
        }
        MassRename rule = rule();
        Map<Row, String> names = new LinkedHashMap<>();
        int index = 0;
        for (Row row : targets) {
            String name = rule.rename(row.name, index++);
            if (name.isEmpty()) {
                Notification.warning(String.format(loc.getValue(L.MSG_MASS_RENAME_EMPTY_NAME), row.name));
                return false;
            }
            if (!name.equals(row.name)) {
                names.put(row, name);
            }
        }
        if (names.isEmpty()) {
            Notification.warning(loc.getValue(L.MSG_MASS_RENAME_NOTHING_CHANGES));
            return false;
        }
        int renamed = 0;
        try {
            for (Map.Entry<Row, String> entry : names.entrySet()) {
                try {
                    manuscriptService.update(entry.getKey().book, stored -> stored.setName(entry.getValue()));
                    renamed++;
                } catch (ManuscriptLockedException ignored) {
                    // an agent started on the book meanwhile
                }
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        Notification.success(String.format(loc.getValue(L.MSG_MASS_RENAME_DONE), renamed));
        if (renamed > 0 && onChange != null) {
            onChange.run();
        }
        return true;
    }

    // for the tests of the dialog

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = Checkbox.class))
    Checkbox getSubfolders() {
        return subfolders;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = RadioButtonGroup.class))
    RadioButtonGroup<Mode> getModeBox() {
        return modeBox;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = TextField.class))
    TextField getTextBox() {
        return textBox;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = TextField.class))
    TextField getFindBox() {
        return findBox;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = TextField.class))
    TextField getWithBox() {
        return withBox;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = Checkbox.class))
    Checkbox getCaseBox() {
        return caseBox;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = TextField.class))
    TextField getPatternBox() {
        return patternBox;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = IntegerField.class))
    IntegerField getStartBox() {
        return startBox;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = IntegerField.class))
    IntegerField getStepBox() {
        return stepBox;
    }

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = IntegerField.class))
    IntegerField getDigitsBox() {
        return digitsBox;
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
            parameters = {@APIParameter(name = "row", type = MassRenameDialog.Row.class),
                    @APIParameter(name = "checked", type = boolean.class)})
    void check(Row row, boolean checked) {
        row.checked = checked;
        preview();
        updateAllBox();
    }
}
