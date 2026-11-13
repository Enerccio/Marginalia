package com.github.enerccio.marginalia.ui.components;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.model.ImageAttachment;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.service.LoreImages;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.traits.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.dialogs.LorebookEditorDialog;
import com.github.enerccio.marginalia.ui.dialogs.LorebookEntryEditDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Preview of the lore of a story, for a quick look without leaving the story: the entries of its lorebook,
 * each opens to its content and the text that activates it. A search box above the list limits it to the entries with the
 * text in their name, content, note or filter. It changes nothing unless the owner makes it {@link #setEditable editable}:
 * an entry then has an edit button that opens a small dialog for its text (the lorebook editor has the rest), and, where the
 * owner {@link #setFullScreenEditor allows it} (a desktop), a button that opens the full-screen lorebook editor on it. With the sub lorebooks included,
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

    @Autowired
    private AgentRunService agentRunService;

    private Checkbox includeSubbooks;
    private TextField searchField;
    private final LorebookEntryFilter filter = new LorebookEntryFilter();
    private boolean editable = false;
    private boolean fullScreenEditor = false;
    private Callable<Manuscript> book;
    private Button editorButton;
    private TreeGrid<Row> grid;
    private Span noLorebook;

    // uuids kept over refreshing: entries that are opened, lorebooks that the user closed
    private final Set<String> openEntries = new HashSet<>();
    private final Set<String> closedBooks = new HashSet<>();
    private Lorebook lastLorebook;
    // which sub lorebooks are shown, all by default
    private Predicate<Lorebook> subbookFilter = book -> true;

    /**
     * Builds the components (the injected services are there only after the constructor), call it once before
     * {@link #refresh}.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = LorebookPreview.class),
            locals = {@APILocals(name = "searchRow", type = HorizontalLayout.class)})
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

        searchField = new TextField();
        searchField.setPlaceholder(loc.getValue(L.LABEL_SEARCH));
        searchField.setPrefixComponent(Solid.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setWidthFull();
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                filter.setText(e.getValue());
                refresh(lastLorebook);
            }
        });

        editorButton = new Button(Solid.EXPAND_ALT.create(), event -> openEditor(lastLorebook, null));
        editorButton.setThemeName("tertiary");
        editorButton.addClassName(CssStyles.FLEX_SHRINK_0);
        editorButton.setTooltipText(loc.getValue(L.LABEL_FULL_SCREEN_EDITOR));
        editorButton.setAriaLabel(loc.getValue(L.LABEL_FULL_SCREEN_EDITOR));
        editorButton.setVisible(false);
        HorizontalLayout searchRow = new HorizontalLayout(searchField, editorButton);
        searchRow.setWidthFull();
        searchRow.setPadding(false);
        searchRow.setSpacing(false);
        searchRow.setAlignItems(FlexComponent.Alignment.CENTER);
        searchRow.setFlexGrow(1, searchField);

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

        add(searchRow, includeSubbooks, noLorebook, grid);
        setFlexGrow(1, grid);
        return this;
    }

    /**
     * Limits the sub lorebooks that are shown: one that is not accepted is left out with everything below it. For a lorebook
     * that is not the user's, whose owner shares only some of its sub lorebooks.
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "subbookFilter", type = Predicate.class)})
    public void setSubbookFilter(Predicate<Lorebook> subbookFilter) {
        this.subbookFilter = subbookFilter == null ? book -> true : subbookFilter;
    }

    /**
     * Lets the user edit the text of the entries from here (an edit button on every entry). Off by default: the lore of
     * another user is only looked at.
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "editable", type = boolean.class)})
    public void setEditable(boolean editable) {
        this.editable = editable;
        if (grid != null) {
            grid.getDataProvider().refreshAll();
        }
    }

    /**
     * Lets an {@link #setEditable editable} preview open the full-screen lorebook editor: on the lorebook (next to the search box),
     * on a lorebook and on an entry (a button in its row). Off by default: the editor is for a desktop.
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "fullScreenEditor", type = boolean.class)})
    public void setFullScreenEditor(boolean fullScreenEditor) {
        this.fullScreenEditor = fullScreenEditor;
        if (grid != null) {
            grid.getDataProvider().refreshAll();
        }
    }

    /**
     * The book whose lore this is, handed to the full-screen editor (extensions there work for it).
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "book", type = Callable.class)})
    public void setBook(Callable<Manuscript> book) {
        this.book = book;
    }

    /**
     * Opens the full-screen lorebook editor on the lorebook, and on the entry when there is one; the lore is loaded again when
     * it is closed. While an agent works on the lorebook the editor is read only.
     *
     * @return the editor, {@code null} without a lorebook
     */
    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = LorebookEditorDialog.class),
            parameters = {@APIParameter(name = "book", type = Lorebook.class),
                    @APIParameter(name = "entry", type = LorebookEntry.class)},
            locals = {@APILocals(name = "dialog", type = LorebookEditorDialog.class)})
    LorebookEditorDialog openEditor(Lorebook book, LorebookEntry entry) {
        if (book == null) {
            return null;
        }
        LorebookEditorDialog dialog = new LorebookEditorDialog(book, () -> refresh(lastLorebook)).create();
        dialog.setBook(this.book);
        dialog.open();
        if (entry != null) {
            dialog.showEntry(entry);
        }
        return dialog;
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
            searchField.setVisible(root != null);
            editorButton.setVisible(root != null && editable && fullScreenEditor);
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
            lorebookService.getSubbooks(book).stream().filter(subbookFilter).forEach(queue::add);
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
            if (filter.matches(entry)) {
                rows.add(new Row(book, entry));
            }
        }
        return rows;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = HorizontalLayout.class),
            parameters = {@APIParameter(name = "row", type = LorebookPreview.Row.class)},
            locals = {@APILocals(name = "name", type = Span.class),
                    @APILocals(name = "layout", type = HorizontalLayout.class),
                    @APILocals(name = "tooltip", type = L.class),
                    @APILocals(name = "open", type = Button.class),
                    @APILocals(name = "toggle", type = Button.class),
                    @APILocals(name = "edit", type = Button.class)})
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

        if (editable && fullScreenEditor) {
            L tooltip = isBook ? L.LABEL_FULL_SCREEN_EDITOR : L.LABEL_OPEN_IN_FULL_SCREEN_EDITOR;
            Button open = new Button(Solid.EXPAND_ALT.create(), event -> openEditor(row.book(), row.entry()));
            open.setThemeName("tertiary small");
            open.addClassName(CssStyles.FLEX_SHRINK_0);
            UIUtils.addTooltip(open, loc.getValue(tooltip));
            layout.add(open);
        }
        if (!isBook) {
            Button toggle = new Button((grid.isDetailsVisible(row) ? Solid.ANGLE_UP : Solid.ANGLE_DOWN).create(), event ->
                    event.getSource().setIcon((toggleDetails(row) ? Solid.ANGLE_UP : Solid.ANGLE_DOWN).create()));
            toggle.setThemeName("tertiary small");
            toggle.addClassName(CssStyles.FLEX_SHRINK_0);
            if (editable) {
                Button edit = new Button(Solid.PEN.create(), event -> edit(row));
                edit.setThemeName("tertiary small");
                edit.addClassName(CssStyles.FLEX_SHRINK_0);
                UIUtils.addTooltip(edit, loc.getValue(L.LABEL_EDIT_ENTRY));
                layout.add(edit);
            }
            layout.add(toggle);
        }
        return layout;
    }

    /**
     * Opens the dialog that edits the entry, then loads the lore again. Nothing happens while an agent works on the lorebook
     * of the entry.
     */
    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = void.class),
            parameters = {@APIParameter(name = "row", type = LorebookPreview.Row.class)},
            locals = {@APILocals(name = "e", type = Exception.class)})
    void edit(Row row) {
        try {
            if (agentRunService.findLockingRun(row.book()) != null) {
                Notification.warning(loc.getValue(L.LABEL_LOCKED_BY_AGENT));
                return;
            }
            new LorebookEntryEditDialog(row.entry(), () -> refresh(lastLorebook)).create().open();
        } catch (Exception e) {
            UIUtils.internalServerError(loc, e);
        }
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

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = VerticalLayout.class),
            parameters = {@APIParameter(name = "row", type = LorebookPreview.Row.class)},
            locals = {@APILocals(name = "details", type = VerticalLayout.class),
                    @APILocals(name = "entry", type = LorebookEntry.class),
                    @APILocals(name = "activation", type = Span.class),
                    @APILocals(name = "contentLabel", type = Span.class),
                    @APILocals(name = "content", type = Markdown.class),
                    @APILocals(name = "images", type = List.class)})
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
            // entries are often written in Markdown; the images are shown under the text (the Markdown can't load them)
            Markdown content = new Markdown(StringUtils.defaultString(LoreImages.strip(entry.getPayload())));
            content.addClassNames(CssStyles.MIN_WIDTH_0, CssStyles.MAX_WIDTH_FULL);
            details.add(contentLabel, content);
            List<LoreImages.Reference> images = LoreImages.references(entry.getPayload());
            if (!images.isEmpty()) {
                details.add(new MessageImages(images.stream()
                        .map(image -> new ImageAttachment(image.uuid(), image.caption())).toList()));
            }
        }
        return details;
    }

    @PublicAPI(from = "1.7.0",
            returns = @APIReturn(type = TextField.class))
    TextField getSearchField() {
        return searchField;
    }

    @PublicAPI(from = "1.8.0",
            returns = @APIReturn(type = Button.class))
    Button getEditorButton() {
        return editorButton;
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
