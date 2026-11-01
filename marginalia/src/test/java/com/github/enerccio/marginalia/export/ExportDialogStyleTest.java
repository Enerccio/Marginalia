package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ExporterService;
import com.github.enerccio.marginalia.domain.service.PartStyleService;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.styles.PartStyleOption;
import com.github.enerccio.marginalia.test.ComponentTree;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.ExportDialog;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.select.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The choice of the print style of the export dialog: the look of the format first, the style the book shows now, then the styles of the
 * user; it is there only for the formats that use a style.
 */
class ExportDialogStyleTest extends MarginaliaTestBase {

    @Autowired
    private PartStyleService styleService;

    // UI.setCurrent keeps only a weak reference
    private UI ui;

    @BeforeEach
    void user() throws Exception {
        login();
        ui = new UI();
        UI.setCurrent(ui);
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
    }

    @SuppressWarnings("unchecked")
    private static List<Select<Object>> selects(ExportDialog dialog) {
        return ComponentTree.all(dialog, Select.class).map(s -> (Select<Object>) s).toList();
    }

    private static Select<Object> styleSelect(ExportDialog dialog) {
        return selects(dialog).stream().filter(s -> "Print style".equals(s.getLabel())).findFirst().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private static Select<Object> formatSelect(ExportDialog dialog) {
        return selects(dialog).stream().filter(s -> "Format".equals(s.getLabel())).findFirst().orElseThrow();
    }

    private static List<String> labels(Select<Object> select) {
        return select.getListDataView().getItems().map(item -> select.getItemLabelGenerator().apply(item)).toList();
    }

    @Test
    void theChoiceListsTheStylesForExportsByNameAndNoStyleOfBooks() throws Exception {
        styleService.save(new PartStyle(null, "Zebra print").set(PartStyleOption.FONT_SIZE, "1rem").setForExport(true));
        styleService.save(new PartStyle(null, "Reading").set(PartStyleOption.FONT_SIZE, "1rem"));
        Manuscript book = new Manuscript();
        book.setName("A book");

        ExportDialog dialog = new ExportDialog(book);

        assertThat(labels(styleSelect(dialog))).containsExactly("E-book (EPUB)", "HTML page", "PDF book", "Word document", "Zebra print");
    }

    @Test
    void theStyleOfTheFormatIsSelectedAndFollowsTheFormat() throws Exception {
        Manuscript book = new Manuscript();
        book.setName("A book");
        ExportDialog dialog = new ExportDialog(book);
        Select<Object> format = formatSelect(dialog);
        Select<Object> style = styleSelect(dialog);

        for (Object item : format.getListDataView().getItems().toList()) {
            format.setValue(item);
            ExporterService.Exporter exporter = (ExporterService.Exporter) item;
            if (exporter.getDefaultStyleId() != null) {
                String expected = styleService.find(exporter.getDefaultStyleId()).getName();
                assertThat(style.getItemLabelGenerator().apply(style.getValue())).as(format.getItemLabelGenerator().apply(item)).isEqualTo(expected);
            }
        }
    }

    @Test
    void theChoiceIsShownOnlyForTheFormatsThatUseAStyle() {
        Manuscript book = new Manuscript();
        book.setName("A book");
        ExportDialog dialog = new ExportDialog(book);
        Select<Object> format = formatSelect(dialog);
        Select<Object> style = styleSelect(dialog);

        for (Object item : format.getListDataView().getItems().toList()) {
            format.setValue(item);
            boolean supports = ((ExporterService.Exporter) item).supportsStyles();
            assertThat(style.isVisible()).as(format.getItemLabelGenerator().apply(item)).isEqualTo(supports);
        }
    }
}
