package com.github.enerccio.marginalia.ui.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Read only preview of the lore of a story, for a quick look without leaving the story: the entries of its lorebook,
 * each opens to its content and the text that activates it. Nothing is saved from here. With the sub lorebooks included,
 * the lorebook and every lorebook below it (however deep) is a top level row with its entries under it, otherwise
 * the entries of the lorebook of the story are the top level rows. The content of an entry is rendered as Markdown.
 * <p>
 * The owner calls {@link #refresh} whenever the lore may have changed.
 */
@Configurable
@Extendable
public class LorebookPreview extends VerticalLayout implements LLMInteract {

    /**
     * A row of the grid: a lorebook (without entry) or one of its entries.
     */
    record Row(Lorebook book, LorebookEntry entry) {
    }

    @Autowired
    private Localization loc;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    private Checkbox includeSubbooks;
    private TreeGrid<Row> grid;
    private Span noLorebook;

    // uuids kept over refreshing: entries that are opened, lorebooks that the user closed
    private final Set<String> openEntries = new HashSet<>();
    private final Set<String> closedBooks = new HashSet<>();
    private Lorebook lastLorebook;

    /**
     * Builds the components (the injected services are there only after the constructor), call it once before
     * {@link #refresh}.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = LorebookPreview.class))
    public LorebookPreview create() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        includeSubbooks = new Checkbox(loc.getValue(L.LABEL_INCLUDE_SUB_LOREBOOKS));
        includeSubbooks.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                refresh(lastLorebook);
            }
        });

        noLorebook = new Span(loc.getValue(L.MSG_NO_STORY_LOREBOOK));
        noLorebook.addClassNames(CssStyles.HINT_TEXT, CssStyles.WHITE_SPACE_NORMAL);

        grid = new TreeGrid<>();
        grid.setSizeFull();
        // rows grow with their content, so a long name breaks into lines
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_NO_BORDER);
        grid.addComponentHierarchyColumn(this::createRow).setFlexGrow(1);
        grid.setItemDetailsRenderer(new ComponentRenderer<>(this::createDetails));
        grid.addExpandListener(e -> e.getItems().forEach(row -> closedBooks.remove(row.book().getUuid())));
        grid.addCollapseListener(e -> e.getItems().forEach(row -> closedBooks.add(row.book().getUuid())));

        add(includeSubbooks, noLorebook, grid);
        setFlexGrow(1, grid);
        return this;
    }

    /**
     * Loads the lore again.
     *
     * @param lorebook the lorebook of the story, {@code null} when it has none
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "lorebook", type = Lorebook.class)},
            locals = {@APILocals(name = "root", type = Lorebook.class),
                    @APILocals(name = "data", type = TreeData.class),
                    @APILocals(name = "uuids", type = Set.class),
                    @APILocals(name = "book", type = Lorebook.class),
                    @APILocals(name = "bookRow", type = LorebookPreview.Row.class),
                    @APILocals(name = "e", type = Exception.class)})
    public void refresh(Lorebook lorebook) {
        lastLorebook = lorebook;
        try {
            Lorebook root = lorebook == null ? null : lorebookService.find(lorebook);
            noLorebook.setVisible(root == null);
            grid.setVisible(root != null);
            includeSubbooks.setVisible(root != null);
            if (root == null) {
                grid.setDataProvider(new TreeDataProvider<>(new TreeData<>()));
                return;
            }

            TreeData<Row> data = new TreeData<>();
            Set<String> uuids = new HashSet<>();
            if (includeSubbooks.getValue()) {
                for (Lorebook book : collectBooks(root)) {
                    Row bookRow = new Row(book, null);
                    data.addItem(null, bookRow);
                    data.addItems(bookRow, entriesOf(book));
                }
            } else {
                data.addItems(null, entriesOf(root));
            }
            data.getRootItems().forEach(row -> collect(data, row, uuids));

            grid.setDataProvider(new TreeDataProvider<>(data));
            openEntries.retainAll(uuids);
            data.getRootItems().stream().filter(row -> row.entry() == null).forEach(row -> {
                if (!closedBooks.contains(row.book().getUuid())) {
                    grid.expand(row);
                }
            });
            forEachEntry(data, row -> {
                if (openEntries.contains(row.entry().getUuid())) {
                    grid.setDetailsVisible(row, true);
                }
            });
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "data", type = TreeData.class),
                    @APIParameter(name = "row", type = LorebookPreview.Row.class),
                    @APIParameter(name = "uuids", type = Set.class)})
    private void collect(TreeData<Row> data, Row row, Set<String> uuids) {
        if (row.entry() != null) {
            uuids.add(row.entry().getUuid());
        }
        data.getChildren(row).forEach(child -> collect(data, child, uuids));
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "data", type = TreeData.class),
                    @APIParameter(name = "action", type = Consumer.class)})
    private void forEachEntry(TreeData<Row> data, Consumer<Row> action) {
        data.getRootItems().forEach(row -> forEachEntry(data, row, action));
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "data", type = TreeData.class),
                    @APIParameter(name = "row", type = LorebookPreview.Row.class),
                    @APIParameter(name = "action", type = Consumer.class)})
    private void forEachEntry(TreeData<Row> data, Row row, Consumer<Row> action) {
        if (row.entry() != null) {
            action.accept(row);
        }
        data.getChildren(row).forEach(child -> forEachEntry(data, child, action));
    }

    /**
     * The lorebook and all the lorebooks below it, each once, the nearest first (the order the generation finds them).
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = List.class),
            parameters = {@APIParameter(name = "root", type = Lorebook.class)},
            locals = {@APILocals(name = "books", type = List.class),
                    @APILocals(name = "seen", type = Set.class),
                    @APILocals(name = "queue", type = Queue.class),
                    @APILocals(name = "book", type = Lorebook.class)})
    private List<Lorebook> collectBooks(Lorebook root) throws Exception {
        List<Lorebook> books = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        Queue<Lorebook> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Lorebook book = queue.remove();
            if (book.isDeleted() || !seen.add(book.getId())) {
                continue;
            }
            books.add(book);
            queue.addAll(lorebookService.getSubbooks(book));
        }
        return books;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = List.class),
            parameters = {@APIParameter(name = "book", type = Lorebook.class)},
            locals = {@APILocals(name = "rows", type = List.class),
                    @APILocals(name = "entry", type = LorebookEntry.class)})
    private List<Row> entriesOf(Lorebook book) throws Exception {
        List<Row> rows = new ArrayList<>();
        for (LorebookEntry entry : lorebookEntryService.getEntriesForLorebook(book)) {
            rows.add(new Row(book, entry));
        }
        return rows;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = HorizontalLayout.class),
            parameters = {@APIParameter(name = "row", type = LorebookPreview.Row.class)},
            locals = {@APILocals(name = "name", type = Span.class),
                    @APILocals(name = "layout", type = HorizontalLayout.class),
                    @APILocals(name = "toggle", type = Button.class)})
    private HorizontalLayout createRow(Row row) {
        boolean isBook = row.entry() == null;
        boolean enabled = isBook ? row.book().isEnabled() : row.entry().isEnabled();
        Span name = new Span(StringUtils.defaultString(isBook ? row.book().getName() : row.entry().getName()));
        name.addClassNames(CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE, CssStyles.MIN_WIDTH_0);
        // what is switched off is not used by the generation, it is muted
        name.addClassName(enabled ? CssStyles.TEXT_BOLD : CssStyles.HINT_TEXT);

        HorizontalLayout layout = new HorizontalLayout(name);
        layout.setWidthFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setFlexGrow(1, name);

        if (!isBook) {
            Button toggle = new Button((grid.isDetailsVisible(row) ? Solid.ANGLE_UP : Solid.ANGLE_DOWN).create(), event ->
                    event.getSource().setIcon((toggleDetails(row) ? Solid.ANGLE_UP : Solid.ANGLE_DOWN).create()));
            toggle.setThemeName("tertiary small");
            toggle.addClassName(CssStyles.FLEX_SHRINK_0);
            layout.add(toggle);
        }
        return layout;
    }

    /**
     * Opens the details of the entry, or closes them when they are open.
     *
     * @return whether they are open now
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = boolean.class),
            parameters = {@APIParameter(name = "row", type = LorebookPreview.Row.class)})
    boolean toggleDetails(Row row) {
        boolean open = !grid.isDetailsVisible(row);
        grid.setDetailsVisible(row, open);
        if (open) {
            openEntries.add(row.entry().getUuid());
        } else {
            openEntries.remove(row.entry().getUuid());
        }
        return open;
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = VerticalLayout.class),
            parameters = {@APIParameter(name = "row", type = LorebookPreview.Row.class)},
            locals = {@APILocals(name = "details", type = VerticalLayout.class),
                    @APILocals(name = "entry", type = LorebookEntry.class),
                    @APILocals(name = "activation", type = Span.class),
                    @APILocals(name = "contentLabel", type = Span.class),
                    @APILocals(name = "content", type = Markdown.class)})
    private VerticalLayout createDetails(Row row) {
        VerticalLayout details = new VerticalLayout();
        details.setWidthFull();
        details.setPadding(false);
        details.setSpacing(false);
        details.getThemeList().add("spacing-xs");
        if (row.entry() == null) {
            return details;
        }
        // a flex item does not get narrower than its content unless it is allowed to: the text wraps inside the cell
        details.addClassNames(CssStyles.SUBTLE_BOX, CssStyles.MIN_WIDTH_0, CssStyles.MAX_WIDTH_FULL, CssStyles.BORDER_BOX);

        LorebookEntry entry = row.entry();
        if (StringUtils.isNotBlank(entry.getFiltering())) {
            Span activation = infoSpan(L.LABEL_FILTERING, entry.getFiltering());
            if (entry.getFilteringMode() == FilteringMode.REGEX) {
                activation.add(new Span(" (" + loc.getValue(loc.getFilteringMode(FilteringMode.REGEX)) + ")"));
            }
            details.add(activation);
        }
        if (StringUtils.isNotBlank(entry.getPayload())) {
            Span contentLabel = new Span(loc.getValue(L.LABEL_CONTENT) + ":");
            contentLabel.addClassName(CssStyles.TEXT_SEMIBOLD);
            // entries are often written in Markdown
            Markdown content = new Markdown(entry.getPayload());
            content.addClassNames(CssStyles.MIN_WIDTH_0, CssStyles.MAX_WIDTH_FULL);
            details.add(contentLabel, content);
        }
        return details;
    }

    /**
     * A "label: value" line, empty when there is no value.
     */
    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = Span.class),
            parameters = {@APIParameter(name = "label", type = L.class),
                    @APIParameter(name = "value", type = String.class)},
            locals = {@APILocals(name = "span", type = Span.class),
                    @APILocals(name = "labelSpan", type = Span.class),
                    @APILocals(name = "valueSpan", type = Span.class)})
    private Span infoSpan(L label, String value) {
        Span span = new Span();
        span.addClassNames(CssStyles.WHITE_SPACE_NORMAL, CssStyles.WRAP_ANYWHERE);
        if (StringUtils.isBlank(value)) {
            span.setVisible(false);
            return span;
        }
        Span labelSpan = new Span(loc.getValue(label) + ": ");
        labelSpan.addClassName(CssStyles.TEXT_SEMIBOLD);
        Span valueSpan = new Span(value);
        valueSpan.addClassName(CssStyles.PRE_WRAP);
        span.add(labelSpan, valueSpan);
        return span;
    }

}
