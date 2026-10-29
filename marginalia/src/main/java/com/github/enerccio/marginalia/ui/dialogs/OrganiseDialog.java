package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.github.enerccio.marginalia.utils.UIUtils;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Two panes side by side, each showing a folder with its folders and books, like a two-pane file manager. This is the
 * structure only: loading the panes and moving about in them (the keyboard included). What the dialog is for, copying and
 * moving between the panes, comes with the actions that are added to it.
 * <p>
 * Keys (in a pane): Left / Right change the pane, Up / Down move the cursor (the grid does that), Enter opens the folder under
 * the cursor, Backspace goes up one level, Space / Insert mark the entry under the cursor.
 */
@Configurable
public class OrganiseDialog extends Dialog {

    private enum Kind {
        /** The first entry of a pane that is in a folder: the folder above. */
        UP,
        FOLDER,
        BOOK
    }

    /**
     * One line of a pane: the folder above (UP, its folder is where it leads, null is the top level), a folder or a book.
     */
    private record Entry(Kind kind, Folder folder, Manuscript book) {
        String name() {
            return switch (kind) {
                case UP -> "..";
                case FOLDER -> folder.getName();
                case BOOK -> book.getName();
            };
        }
    }

    @Autowired
    private Localization loc;

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    private final Folder leftStart;
    private final Folder rightStart;
    private Pane left;
    private Pane right;
    private Pane active;

    /**
     * @param left  the folder the left pane starts in, null is the top level
     * @param right the folder the right pane starts in
     */
    public OrganiseDialog(Folder left, Folder right) {
        this.leftStart = left;
        this.rightStart = right;
    }

    public void create() {
        setHeaderTitle(loc.getValue(L.LABEL_ORGANISE));
        setWidth("1100px");
        setMaxWidth("96%");
        setHeight("80%");
        setModality(ModalityMode.STRICT);
        setCloseOnOutsideClick(false);
        UIUtils.fitPhone(this, true);

        left = new Pane(leftStart);
        right = new Pane(rightStart);

        Span hint = new Span(loc.getValue(L.HELP_ORGANISE));
        hint.addClassName(CssStyles.HINT_TEXT);

        HorizontalLayout panes = new HorizontalLayout(left.layout, right.layout);
        panes.setSizeFull();
        panes.setPadding(false);
        panes.addClassName(CssStyles.MIN_HEIGHT_0);
        panes.expand(left.layout, right.layout);

        VerticalLayout content = new VerticalLayout(hint, panes);
        content.setSizeFull();
        content.setPadding(false);
        content.addClassName(CssStyles.MIN_HEIGHT_0);
        content.expand(panes);
        add(content);

        Button close = new Button(loc.getValue(L.LABEL_CLOSE), _ -> close());
        HorizontalLayout footer = new HorizontalLayout(close);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footer);

        left.load();
        right.load();
        activate(left);
        DialogFocus.onOpen(this, left.grid);
    }

    // for the tests of the dialog

    List<String> names(boolean leftPane) {
        return (leftPane ? left : right).grid.getListDataView().getItems().map(Entry::name).toList();
    }

    String path(boolean leftPane) {
        return (leftPane ? left : right).path.getText();
    }

    void open(boolean leftPane, String name) {
        Pane pane = leftPane ? left : right;
        pane.open(pane.grid.getListDataView().getItems().filter(entry -> entry.name().equals(name)).findFirst().orElseThrow());
    }

    void up(boolean leftPane) {
        (leftPane ? left : right).up();
    }

    private Pane other(Pane pane) {
        return pane == left ? right : left;
    }

    private void activate(Pane pane) {
        active = pane;
        left.layout.removeClassName(CssStyles.ORGANISE_PANE_ACTIVE);
        right.layout.removeClassName(CssStyles.ORGANISE_PANE_ACTIVE);
        pane.layout.addClassName(CssStyles.ORGANISE_PANE_ACTIVE);
    }

    /**
     * One side of the dialog: the folder it shows, the entries of it and the cursor.
     */
    private final class Pane {
        private Folder folder;
        private final Span path = new Span();
        private final Grid<Entry> grid = new Grid<>();
        private final VerticalLayout layout;
        // the entry under the cursor, the last one the grid reported focus on
        private Entry cursor;

        Pane(Folder folder) {
            this.folder = folder;

            grid.setSizeFull();
            grid.addThemeVariants(GridVariant.LUMO_COMPACT);
            grid.setSelectionMode(Grid.SelectionMode.MULTI);
            grid.addComponentColumn(entry -> name(entry)).setHeader(loc.getValue(L.LABEL_NAME)).setFlexGrow(3);
            grid.addColumn(entry -> entry.kind() == Kind.FOLDER ? count(entry.folder()) : "")
                    .setHeader(loc.getValue(L.LABEL_BOOKS)).setFlexGrow(0).setWidth("100px");
            grid.addCellFocusListener(event -> {
                cursor = event.getItem().orElse(null);
                activate(this);
            });
            grid.addItemDoubleClickListener(event -> open(event.getItem()));

            // movement: the keys listen on the grid, so they work in the pane that has the focus
            Shortcuts.addShortcutListener(grid, () -> open(cursor), Key.ENTER).listenOn(grid);
            Shortcuts.addShortcutListener(grid, this::up, Key.BACKSPACE).listenOn(grid);
            Shortcuts.addShortcutListener(grid, this::mark, Key.INSERT).listenOn(grid);
            Shortcuts.addShortcutListener(grid, () -> goTo(other(this)), Key.ARROW_LEFT).listenOn(grid);
            Shortcuts.addShortcutListener(grid, () -> goTo(other(this)), Key.ARROW_RIGHT).listenOn(grid);

            path.addClassName(CssStyles.TEXT_BOLD);
            layout = new VerticalLayout(path, grid);
            layout.setPadding(false);
            layout.setSpacing(false);
            layout.addClassNames(CssStyles.ORGANISE_PANE, CssStyles.MIN_HEIGHT_0);
            layout.expand(grid);
        }

        private com.vaadin.flow.component.Component name(Entry entry) {
            VaadinIcon icon = switch (entry.kind()) {
                case UP -> VaadinIcon.LEVEL_UP;
                case FOLDER -> VaadinIcon.FOLDER;
                case BOOK -> VaadinIcon.BOOK;
            };
            HorizontalLayout name = new HorizontalLayout(icon.create(), new Span(entry.name()));
            name.setPadding(false);
            name.setAlignItems(FlexComponent.Alignment.CENTER);
            return name;
        }

        private String count(Folder inside) {
            Long books = counts.get(inside.getId());
            return NumberFormat.getIntegerInstance().format(books == null ? 0 : books);
        }

        private Map<Long, Long> counts = Map.of();

        /**
         * Reads the folder again. A folder that is gone is the top level.
         */
        void load() {
            try {
                if (folder != null) {
                    Folder stored = folderService.find(folder.getId());
                    folder = stored == null || stored.isDeleted() ? null : stored;
                }
                counts = folderService.countBooksByFolder();
                List<Entry> entries = new ArrayList<>();
                if (folder != null) {
                    entries.add(new Entry(Kind.UP, folder.getParent(), null));
                }
                for (Folder inside : folderService.findChildren(folder)) {
                    entries.add(new Entry(Kind.FOLDER, inside, null));
                }
                List<Manuscript> books = new ArrayList<>(manuscriptService.findRegularInFolder(folder, false));
                books.sort(Comparator.comparing(book -> StringUtils.defaultString(book.getName()), String.CASE_INSENSITIVE_ORDER));
                for (Manuscript book : books) {
                    entries.add(new Entry(Kind.BOOK, null, book));
                }
                grid.setItems(entries);
                grid.deselectAll();
                cursor = null;
                path.setText(folder == null ? loc.getValue(L.LABEL_TOP_LEVEL) : folderService.getPath(folder));
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }

        /**
         * Goes into the folder of the entry, or up for the entry above; a book is not opened.
         */
        void open(Entry entry) {
            if (entry == null || entry.kind() == Kind.BOOK) {
                return;
            }
            folder = entry.folder();
            load();
            grid.focus();
        }

        void up() {
            try {
                Folder stored = folder == null ? null : folderService.find(folder.getId());
                if (stored != null) {
                    folder = stored.getParent();
                    load();
                    grid.focus();
                }
            } catch (Exception e) {
                UIUtils.internalServerError(loc, e);
            }
        }

        /**
         * Marks or unmarks the entry under the cursor, the folder above can't be marked.
         */
        void mark() {
            if (cursor == null || cursor.kind() == Kind.UP) {
                return;
            }
            if (grid.getSelectedItems().contains(cursor)) {
                grid.deselect(cursor);
            } else {
                grid.select(cursor);
            }
        }

        private void goTo(Pane pane) {
            activate(pane);
            pane.grid.focus();
        }
    }
}
