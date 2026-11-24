package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.service.tool.ToolItem;
import com.github.enerccio.marginalia.domain.service.tool.ToolProviderInfo;
import com.github.enerccio.marginalia.domain.service.tool.ToolSelection;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Chooses which tool suites the model may use and, of a suite that has items (categories of names...), which items: one
 * section for each {@link ToolProviderInfo} under its human name, with a filter of its own. The checked items stay in the
 * list whatever the filter says. A suite that is only on or off has a check box. Each section can carry a hint for the
 * model that replaces the default one of the suite.
 * <p>
 * Works on a copy of the selection and gives it to {@code onSave} when the user saves.
 */
@Configurable(preConstruction = true)
public class ToolSelectionDialog extends Dialog {

    @Autowired
    private Localization loc;

    private final List<Section> sections = new ArrayList<>();

    /**
     * @param providers the suites that are installed now
     * @param selection the choice made so far; not changed
     * @param onSave    gets the new choice when the user saves
     */
    public ToolSelectionDialog(List<ToolProviderInfo> providers, ToolSelection selection, Consumer<ToolSelection> onSave) {
        setHeaderTitle(loc.getValue(L.LABEL_TOOLS));
        // the names of the items are long: wide enough for them, and the sections take the height
        setWidth("min(720px, 95vw)");
        setHeight("80vh");

        Span info = new Span(loc.getValue(L.MSG_TOOLS_INFO));
        info.addClassName(CssStyles.HINT_TEXT);
        VerticalLayout content = new VerticalLayout(info);
        content.setPadding(false);
        content.setWidthFull();
        content.addClassName(CssStyles.GAP_S);
        ToolSelection current = selection == null ? new ToolSelection() : selection;
        for (ToolProviderInfo provider : providers) {
            Section section = new Section(provider, current.get(provider.ref()), providers.size() == 1);
            sections.add(section);
            section.details.setWidthFull();
            content.add(section.details);
        }
        add(content);
        if (!sections.isEmpty()) {
            sections.getFirst().focusFirst(this);
        }

        Button save = new Button(loc.getValue(L.LABEL_SAVE), e -> {
            ToolSelection result = new ToolSelection();
            sections.forEach(s -> s.writeTo(result.entry(s.provider.ref())));
            onSave.accept(result.compact());
            close();
        });
        save.setThemeName("primary");
        getFooter().add(new Button(loc.getValue(L.LABEL_CANCEL), e -> close()), save);
    }

    /** One provider: a panel that is open when something of it is checked. */
    private final class Section {

        private final ToolProviderInfo provider;
        private final Details details = new Details();
        private final Span title = new Span();
        private final TextArea hint = new TextArea();
        private final Checkbox enabled;
        private final TextField filter;
        private final Grid<ToolItem> items;
        private final Span count = new Span();

        Section(ToolProviderInfo provider, ToolSelection.Entry saved, boolean alone) {
            this.provider = provider;
            VerticalLayout body = new VerticalLayout();
            body.setPadding(false);
            body.setWidthFull();
            body.addClassName(CssStyles.GAP_S);
            if (provider.description() != null) {
                Span description = new Span(provider.description());
                description.addClassName(CssStyles.HINT_TEXT);
                body.add(description);
            }
            if (provider.commands()) {
                Span commands = new Span(loc.getValue(L.MSG_TOOLS_ALSO_COMMANDS));
                commands.addClassName(CssStyles.HINT_TEXT);
                body.add(commands);
            }

            boolean any;
            if (provider.selectable()) {
                enabled = null;
                filter = new TextField();
                filter.setPlaceholder(loc.getValue(L.LABEL_TOOLS_FILTER));
                filter.setPrefixComponent(VaadinIcon.SEARCH.create());
                filter.setClearButtonVisible(true);
                filter.setValueChangeMode(ValueChangeMode.LAZY);
                filter.addClassName(CssStyles.FULL_WIDTH);
                // only the filter changes the rows: an item unchecked while it does not match stays until the next change of the filter
                filter.addValueChangeListener(e -> refreshItems());

                items = new Grid<>(ToolItem.class, false);
                GridMultiSelectionModel<ToolItem> selection = (GridMultiSelectionModel<ToolItem>) items.setSelectionMode(Grid.SelectionMode.MULTI);
                // "select all" would check the items the filter hides too
                selection.setSelectAllCheckboxVisibility(GridMultiSelectionModel.SelectAllCheckboxVisibility.HIDDEN);
                items.addColumn(ToolItem::label);
                items.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
                items.setHeight("260px");
                items.addClassName(CssStyles.MIN_HEIGHT_0);
                items.setItems(
                        query -> shown().skip(query.getOffset()).limit(query.getLimit()),
                        query -> (int) shown().count());
                Set<String> checked = new HashSet<>(saved == null ? List.of() : saved.getItems());
                provider.items().stream().filter(i -> checked.contains(i.id())).forEach(items::select);
                items.addSelectionListener(e -> update());

                Button checkShown = new Button(loc.getValue(L.LABEL_TOOLS_SELECT_SHOWN),
                        e -> items.asMultiSelect().updateSelection(new LinkedHashSet<>(shown().toList()), Set.of()));
                Button clearShown = new Button(loc.getValue(L.LABEL_TOOLS_CLEAR_SHOWN),
                        e -> items.asMultiSelect().updateSelection(Set.of(), new LinkedHashSet<>(shown().toList())));
                count.addClassName(CssStyles.HINT_TEXT);
                items.setWidthFull();
                body.add(filter, items, new HorizontalLayout(checkShown, clearShown, count));
                any = !items.getSelectedItems().isEmpty();
            } else {
                filter = null;
                items = null;
                enabled = new Checkbox(loc.getValue(L.LABEL_TOOLS_ENABLED), saved != null && saved.isEnabled());
                enabled.addValueChangeListener(e -> update());
                body.add(enabled);
                any = enabled.getValue();
            }

            hint.setLabel(loc.getValue(L.LABEL_TOOLS_HINT));
            hint.setHelperText(loc.getValue(L.MSG_TOOLS_HINT_INFO));
            hint.setPlaceholder(provider.defaultHint() == null ? "" : provider.defaultHint().strip());
            hint.setWidthFull();
            hint.setValue(saved == null || saved.getHint() == null ? "" : saved.getHint());
            Details hintDetails = new Details(loc.getValue(L.LABEL_TOOLS_HINT), hint);
            hintDetails.setWidthFull();
            body.add(hintDetails);

            details.setSummary(title);
            details.add(body);
            details.setOpened(any || alone);
            update();
        }

        /** The items to list: the ones containing the filter text (any case) and the checked ones, in the order they come. */
        private Stream<ToolItem> shown() {
            String needle = filter.getValue() == null ? "" : filter.getValue().strip().toLowerCase(Locale.ROOT);
            Set<ToolItem> checked = items.getSelectedItems();
            return provider.items().stream()
                    .filter(i -> needle.isEmpty() || checked.contains(i) || i.label().toLowerCase(Locale.ROOT).contains(needle));
        }

        private void refreshItems() {
            items.getDataProvider().refreshAll();
        }

        private void update() {
            int checked = items != null ? items.getSelectedItems().size() : enabled.getValue() ? 1 : 0;
            title.setText(checked > 0 ? provider.name() + " (" + checked + ")" : provider.name());
            if (items != null) {
                count.setText(String.format(loc.getValue(L.LABEL_TOOLS_CHECKED), checked, provider.items().size()));
            }
        }

        void focusFirst(Dialog dialog) {
            if (filter != null) {
                DialogFocus.onOpen(dialog, filter);
            }
        }

        void writeTo(ToolSelection.Entry entry) {
            if (items != null) {
                // in the order of the provider
                Set<ToolItem> checked = items.getSelectedItems();
                entry.setItems(provider.items().stream().filter(checked::contains).map(ToolItem::id).toList());
            } else {
                entry.setEnabled(enabled.getValue());
            }
            entry.setHint(hint.getValue());
        }
    }
}
