package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.traits.APILocals;
import com.github.enerccio.marginalia.domain.traits.APIReturn;
import com.github.enerccio.marginalia.domain.traits.Extendable;
import com.github.enerccio.marginalia.domain.traits.PublicAPI;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.utils.MarkdownArchive;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Shows the files of an archive with the name each would give its lorebook entry (from the {@code names.txt} of the
 * archive, the first heading of the file or the file name), to change before the import. The callback gets the names in the
 * order of the files; the dialog is not closed by Cancel's callback.
 */
@Configurable
@Extendable
public class ArchiveEntryNamesDialog extends Dialog {

    private static final class Row {
        private final int index;
        private final String path;
        private String name;

        private Row(int index, String path, String name) {
            this.index = index;
            this.path = path;
            this.name = name;
        }
    }

    @Autowired
    private Localization loc;

    private final List<Row> rows = new ArrayList<>();
    private final Consumer<List<String>> onConfirm;

    private ArchiveEntryNamesDialog(MarkdownArchive.Content content, Consumer<List<String>> onConfirm) {
        List<String> names = content.itemNames();
        for (int i = 0; i < names.size(); i++) {
            rows.add(new Row(i + 1, content.items().get(i).path(), names.get(i)));
        }
        this.onConfirm = onConfirm;
    }

    /**
     * Opens the dialog for the content of an archive; the callback runs when the user confirms.
     */
    public static void show(MarkdownArchive.Content content, Consumer<List<String>> onConfirm) {
        ArchiveEntryNamesDialog dialog = new ArchiveEntryNamesDialog(content, onConfirm);
        dialog.create();
        dialog.open();
    }

    @PublicAPI(from = "1.4.0",
            returns = @APIReturn(type = void.class),
            locals = {@APILocals(name = "grid", type = Grid.class),
                    @APILocals(name = "layout", type = VerticalLayout.class),
                    @APILocals(name = "importButton", type = Button.class)})
    private void create() {
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setModality(ModalityMode.STRICT);
        setWidth("900px");
        setHeaderTitle(loc.getValue(L.LABEL_ENTRY_NAMES));

        Grid<Row> grid = new Grid<>();
        grid.setHeight("450px");
        grid.addColumn(row -> row.index)
                .setHeader("#")
                .setFlexGrow(0)
                .setWidth("80px");
        grid.addColumn(row -> row.path)
                .setHeader(loc.getValue(L.LABEL_ARCHIVE_FILE))
                .setFlexGrow(1);
        grid.addComponentColumn(row -> {
                    TextField field = new TextField();
                    field.setWidthFull();
                    field.setValue(row.name);
                    field.addValueChangeListener(e -> row.name = e.getValue());
                    return field;
                })
                .setHeader(loc.getValue(L.LABEL_NAME))
                .setFlexGrow(2);
        grid.setItems(rows);

        VerticalLayout layout = new VerticalLayout(new Span(loc.getValue(L.MSG_ARCHIVE_ENTRY_NAMES)), grid);
        layout.setPadding(false);
        add(layout);

        Button importButton = new Button(loc.getValue(L.LABEL_IMPORT), e -> {
            close();
            onConfirm.accept(rows.stream().map(row -> row.name).toList());
        });
        importButton.setThemeName("primary");
        getFooter().add(new Button(loc.getValue(L.LABEL_CANCEL), e -> close()), importButton);
    }
}
