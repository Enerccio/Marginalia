package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.components.LoreUsageReport;
import com.github.enerccio.marginalia.domain.components.LoreUsageReport.Scope;
import com.github.enerccio.marginalia.domain.components.LoreUsageReport.Status;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.LoreUsageService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Comparator;
import java.util.List;

/**
 * Which lorebook entries the generations of the book used and which never did, over the active branch or every branch and
 * over a range of parts (numbered as in the outline). Counted from the record each generation kept with its prompt, so the
 * parts typed by hand and the ones made before the record existed are not in the shares.
 */
@Configurable
public class BookLoreUsageDialog extends Dialog {

    /**
     * What the table shows.
     */
    private enum Show {
        ALL, NEVER, SOMETIMES, ALWAYS;

        boolean matches(Status status) {
            return this == ALL || name().equals(status.name());
        }
    }

    @Autowired
    private Localization loc;

    @Autowired
    private LoreUsageService loreUsageService;

    private final Manuscript manuscript;

    private Select<Scope> scopeSelect;
    private IntegerField fromField;
    private IntegerField toField;
    private Select<Show> showSelect;
    private Div summary;
    private Grid<LoreUsageReport.Row> grid;
    private Div help;
    private LoreUsageReport report;

    public BookLoreUsageDialog(Manuscript manuscript) {
        this.manuscript = manuscript;
    }

    public void create() throws Exception {
        setHeaderTitle(loc.getValue(L.LABEL_LORE_USAGE));
        setWidth("1000px");
        setMaxWidth("100vw");
        setResizable(true);
        setDraggable(true);

        scopeSelect = new Select<>();
        scopeSelect.setLabel(loc.getValue(L.LABEL_PARTS));
        scopeSelect.setItems(Scope.values());
        scopeSelect.setItemLabelGenerator(this::label);
        scopeSelect.setValue(Scope.ACTIVE_BRANCH);
        scopeSelect.addValueChangeListener(event -> reload());

        fromField = partNumberField(L.LABEL_ANNOTATE_FROM_PART);
        toField = partNumberField(L.LABEL_ANNOTATE_TO_PART);

        showSelect = new Select<>();
        showSelect.setLabel(loc.getValue(L.LABEL_JSON_SHOW));
        showSelect.setItems(Show.values());
        showSelect.setItemLabelGenerator(this::label);
        showSelect.setValue(Show.ALL);
        showSelect.addValueChangeListener(event -> refill());

        HorizontalLayout controls = new HorizontalLayout(scopeSelect, fromField, toField, showSelect);
        controls.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        controls.setWrap(true);

        summary = new Div();
        summary.addClassName(CssStyles.TOKEN_USAGE_SUMMARY);
        grid = table();
        help = new Div(loc.getValue(L.MSG_LORE_USAGE_HELP));
        help.addClassName(CssStyles.TOKEN_USAGE_SUMMARY);

        VerticalLayout content = new VerticalLayout(controls, summary, grid, help);
        content.setPadding(false);
        content.setSpacing(true);
        add(content);

        Button closeButton = new Button(loc.getValue(L.LABEL_CLOSE), event -> close());
        closeButton.setThemeName("primary");
        getFooter().add(closeButton);

        reload();
    }

    private IntegerField partNumberField(L label) {
        IntegerField field = new IntegerField(loc.getValue(label));
        field.setMin(1);
        field.setStepButtonsVisible(true);
        field.setWidth("9em");
        field.addValueChangeListener(event -> reload());
        return field;
    }

    private Grid<LoreUsageReport.Row> table() {
        Grid<LoreUsageReport.Row> table = new Grid<>(LoreUsageReport.Row.class, false);
        table.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);
        table.setHeight("420px");
        table.addColumn(row -> row.name() == null ? loc.getValue(L.LABEL_LORE_USAGE_UNKNOWN_ENTRY) : row.name())
                .setHeader(loc.getValue(L.ENUM_MERGE_KIND_ENTRY)).setFlexGrow(3).setResizable(true)
                .setComparator(Comparator.comparing(row -> row.name() == null ? "" : row.name(), String.CASE_INSENSITIVE_ORDER));
        table.addColumn(row -> row.lorebook() == null ? "" : row.lorebook())
                .setHeader(loc.getValue(L.LABEL_LOREBOOK)).setFlexGrow(2).setResizable(true)
                .setComparator(Comparator.comparing(row -> row.lorebook() == null ? "" : row.lorebook(), String.CASE_INSENSITIVE_ORDER));
        table.addColumn(this::statusText).setHeader(loc.getValue(L.LABEL_STATUS)).setFlexGrow(3).setResizable(true)
                .setComparator(Comparator.comparing(LoreUsageReport.Row::status));
        table.addColumn(this::usedText).setHeader(loc.getValue(L.LABEL_LORE_USAGE_USED_IN)).setFlexGrow(2)
                .setComparator(Comparator.comparingInt(LoreUsageReport.Row::used)).setTextAlign(ColumnTextAlign.END);
        table.addColumn(row -> String.format("%,d", row.tokens())).setHeader(loc.getValue(L.LABEL_TOKENS))
                .setComparator(Comparator.comparingLong(LoreUsageReport.Row::tokens)).setTextAlign(ColumnTextAlign.END);
        table.addColumn(row -> row.used() == 0 ? "" : String.format("%,d", row.averageTokens()))
                .setHeader(loc.getValue(L.LABEL_LORE_USAGE_AVERAGE_TOKENS))
                .setComparator(Comparator.comparingLong(LoreUsageReport.Row::averageTokens)).setTextAlign(ColumnTextAlign.END);
        table.addColumn(row -> row.used() == 0 ? "" : String.valueOf(row.firstPart())).setHeader(loc.getValue(L.LABEL_LORE_USAGE_FIRST_PART))
                .setComparator(Comparator.comparingInt(LoreUsageReport.Row::firstPart)).setTextAlign(ColumnTextAlign.END);
        table.addColumn(row -> row.used() == 0 ? "" : String.valueOf(row.lastPart())).setHeader(loc.getValue(L.LABEL_LORE_USAGE_LAST_PART))
                .setComparator(Comparator.comparingInt(LoreUsageReport.Row::lastPart)).setTextAlign(ColumnTextAlign.END);
        return table;
    }

    private String statusText(LoreUsageReport.Row row) {
        StringBuilder text = new StringBuilder(label(row.status()));
        if (!row.inLorebook()) {
            text.append(" · ").append(loc.getValue(L.LABEL_LORE_USAGE_NOT_IN_LOREBOOK));
        } else if (!row.enabled()) {
            text.append(" · ").append(loc.getValue(L.LABEL_AGENT_DISABLED));
        }
        return text.toString();
    }

    private String usedText(LoreUsageReport.Row row) {
        int recorded = report == null ? 0 : report.recordedParts();
        return recorded == 0 ? "0" : String.format("%d / %d (%d %%)", row.used(), recorded, Math.round(100.0 * row.used() / recorded));
    }

    private String label(Scope scope) {
        return loc.getValue(scope == Scope.ACTIVE_BRANCH ? L.ENUM_LORE_USAGE_SCOPE_ACTIVE_BRANCH : L.ENUM_LORE_USAGE_SCOPE_ALL_BRANCHES);
    }

    private String label(Status status) {
        return loc.getValue(switch (status) {
            case NEVER -> L.ENUM_LORE_USAGE_STATUS_NEVER;
            case SOMETIMES -> L.ENUM_LORE_USAGE_STATUS_SOMETIMES;
            case ALWAYS -> L.ENUM_LORE_USAGE_STATUS_ALWAYS;
        });
    }

    private String label(Show show) {
        return show == Show.ALL ? loc.getValue(L.LABEL_ALL) : label(Status.valueOf(show.name()));
    }

    /**
     * Counts again for the scope and range now chosen.
     */
    private void reload() {
        if (grid == null || scopeSelect.getValue() == null) {
            return;
        }
        try {
            report = loreUsageService.forManuscript(manuscript, scopeSelect.getValue(), fromField.getValue(), toField.getValue());
            refill();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    /**
     * Shows the rows of the last count that the filter lets through.
     */
    private void refill() {
        if (report == null) {
            return;
        }
        if (report.recordedParts() == 0) {
            summary.setText(loc.getValue(report.hasLorebook() ? L.MSG_LORE_USAGE_NO_RECORDS : L.MSG_LORE_USAGE_NO_LOREBOOK));
            grid.setItems(List.of());
            return;
        }
        String text = String.format(loc.getValue(L.MSG_LORE_USAGE_SUMMARY), report.parts(), report.recordedParts());
        int unrecorded = report.parts() - report.recordedParts();
        if (unrecorded > 0) {
            text += " " + String.format(loc.getValue(L.MSG_LORE_USAGE_UNRECORDED), unrecorded);
        }
        summary.setText(text);
        Show show = showSelect.getValue() == null ? Show.ALL : showSelect.getValue();
        grid.setItems(report.rows().stream().filter(row -> show.matches(row.status())).toList());
    }
}
