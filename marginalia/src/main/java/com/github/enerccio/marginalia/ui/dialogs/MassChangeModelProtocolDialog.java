package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.UIConstants;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.widgets.ModelProtocolPicker;
import com.github.enerccio.marginalia.ui.widgets.Notification;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;

/**
 * Puts one model and/or one protocol on many books without opening each. The user picks the model and the protocol (an
 * empty box means "do not replace": that part of the book stays as it is), and the list shows every book by name with its
 * model and protocol; the name filter narrows the list, the checkboxes (and the one in the header, which ticks the books
 * that are shown) choose the books. <b>Replace and exit</b> changes the checked books and closes the window. A book an
 * agent works on is locked: it is shown grayed out, and nothing can be done with it.
 */
@Configurable
@Extendable
public class MassChangeModelProtocolDialog extends Dialog implements LLMInteract {

    /**
     * One book of the list: how it is shown, whether it can be changed, its model and protocol now and whether it is
     * checked.
     */
    static final class Row {
        private final Manuscript book;
        private final String name;
        private final String model;
        private final String protocol;
        private final boolean locked;
        private boolean checked;

        private Row(Manuscript book, boolean locked) {
            this.book = book;
            this.name = StringUtils.defaultString(book.getName());
            this.model = book.getAi() == null ? "" : StringUtils.defaultString(book.getAi().getName());
            this.protocol = book.getProtocol() == null ? "" : StringUtils.defaultString(book.getProtocol().getName());
            this.locked = locked;
        }

        Manuscript book() {
            return book;
        }

        String name() {
            return name;
        }

        String model() {
            return model;
        }

        String protocol() {
            return protocol;
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
    private AgentRunService agentRunService;

    private final Runnable onChange;

    private ModelProtocolPicker picker;
    private TextField nameFilter;
    private Grid<Row> grid;
    private Checkbox allBox;
    private final List<Row> rows = new ArrayList<>();
    // what the name filter lets through, in the order of the rows (by name)
    private final List<Row> shown = new ArrayList<>();

    /**
     * @param onChange told after books were changed, may be null
     */
    public MassChangeModelProtocolDialog(Runnable onChange) {
        this.onChange = onChange;
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
        setHeaderTitle(loc.getValue(L.LABEL_MASS_CHANGE_MODEL_PROTOCOL));
        setWidth("900px");
        setMaxWidth("96%");
        setHeight("80%");
        setModality(ModalityMode.STRICT);
        setCloseOnOutsideClick(false);
        UIUtils.fitPhone(this, true);

        Span hint = new Span(loc.getValue(L.HELP_MASS_CHANGE_MODEL_PROTOCOL));
        hint.addClassName(CssStyles.HINT_TEXT);

        picker = new ModelProtocolPicker();
        picker.setPlaceholder(loc.getValue(L.LABEL_DO_NOT_REPLACE));

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
        grid.addComponentColumn(row -> text(row, row.model))
                .setHeader(loc.getValue(L.LABEL_MODEL)).setFlexGrow(2);
        grid.addComponentColumn(row -> text(row, row.protocol))
                .setHeader(loc.getValue(L.LABEL_PROTOCOLS)).setFlexGrow(2);
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

        VerticalLayout content = new VerticalLayout(hint, picker, grid);
        content.setSizeFull();
        content.setPadding(false);
        content.addClassName(CssStyles.MIN_HEIGHT_0);
        content.expand(grid);
        add(content);

        load();
    }

    /**
     * Reads the books and their models and protocols again. The checks and the filter stay.
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "loaded", type = List.class),
                    @APILocals(name = "book", type = Manuscript.class),
                    @APILocals(name = "checked", type = Set.class),
                    @APILocals(name = "row", type = MassChangeModelProtocolDialog.Row.class),
                    @APILocals(name = "e", type = Exception.class)})
    public void load() {
        try {
            List<Row> loaded = new ArrayList<>();
            for (Manuscript book : manuscriptService.findAllRegularForUser()) {
                loaded.add(new Row(book, agentRunService.findLockingRun(book) != null));
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
                    @APILocals(name = "row", type = MassChangeModelProtocolDialog.Row.class)})
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
            parameters = {@APIParameter(name = "row", type = MassChangeModelProtocolDialog.Row.class)},
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
            parameters = {@APIParameter(name = "row", type = MassChangeModelProtocolDialog.Row.class),
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
     * Puts the picked model and protocol (the one that is not picked is left as it is in each book) on the checked books.
     * A book that was locked meanwhile is left alone.
     *
     * @return whether the books were changed, false when there was nothing to do (the window stays open then)
     */
    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = boolean.class),
            locals = {@APILocals(name = "ai", type = AI.class),
                    @APILocals(name = "protocol", type = Protocol.class),
                    @APILocals(name = "targets", type = List.class),
                    @APILocals(name = "row", type = MassChangeModelProtocolDialog.Row.class),
                    @APILocals(name = "book", type = Manuscript.class),
                    @APILocals(name = "e", type = Exception.class)})
    boolean replace() {
        AI ai = picker.getAi();
        Protocol protocol = picker.getProtocol();
        if (ai == null && protocol == null) {
            Notification.warning(loc.getValue(L.MSG_MASS_CHANGE_PICK_FIRST));
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
                if (ai != null) {
                    book.setAi(ai);
                }
                if (protocol != null) {
                    book.setProtocol(protocol);
                }
                manuscriptService.save(book);
                changed++;
            }
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
        Notification.success(String.format(loc.getValue(L.MSG_MASS_CHANGE_DONE), changed));
        if (changed > 0 && onChange != null) {
            onChange.run();
        }
        return true;
    }

    // for the tests of the dialog

    @PublicAPI(from = "1.6.0",
            returns = @APIReturn(type = ModelProtocolPicker.class))
    ModelProtocolPicker getPicker() {
        return picker;
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
            parameters = {@APIParameter(name = "row", type = MassChangeModelProtocolDialog.Row.class),
                    @APIParameter(name = "checked", type = boolean.class)})
    void check(Row row, boolean checked) {
        row.checked = checked;
        updateAllBox();
    }
}
