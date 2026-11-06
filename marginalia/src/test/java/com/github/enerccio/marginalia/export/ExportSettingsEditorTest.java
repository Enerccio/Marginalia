package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.service.ExportSettingsService;
import com.github.enerccio.marginalia.domain.service.ExporterService;
import com.github.enerccio.marginalia.domain.service.PartStyleService;
import com.github.enerccio.marginalia.domain.service.TemplateService;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.styles.PartStyleOption;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.exports.ExportSettingsEditor;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The export part of the user settings: the style and the title page template of each format, kept in memory until the page saves.
 */
class ExportSettingsEditorTest extends MarginaliaTestBase {

    @Autowired
    private ExporterService exporterService;

    @Autowired
    private ExportSettingsService settingsService;

    @Autowired
    private PartStyleService styleService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private Localization loc;

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

    private ExportSettingsEditor editor() throws Exception {
        ExportSettingsEditor editor = new ExportSettingsEditor(loc, exporterService, settingsService, styleService, templateService);
        editor.reload();
        return editor;
    }

    @Test
    void everyFormatWithAStyleOrATemplateIsShown() throws Exception {
        ExportSettingsEditor editor = editor();

        assertThat(editor.getShownExporters()).extracting(ExporterService.Exporter::getId)
                .containsExactlyInAnyOrder("txt", "md", "html", "docx", "pdf", "epub", "markdown-front-matter", "obsidian", "latex");
        assertThat(editor.getStyleField("txt")).as("no fonts, no style").isNull();
        assertThat(editor.getStyleField("pdf").getValue()).isEqualTo("export-pdf");
        assertThat(editor.getStyleField("html").getValue()).isEqualTo("export-html");
        assertThat(editor.getTemplateField("txt", "titlePage").getValue()).isEmpty();
        assertThat(editor.getTemplateField("txt", "titlePage").getPlaceholder()).contains("{{title}}");
    }

    @Test
    void theStylesToChooseFromAreTheOnesForExports() throws Exception {
        styleService.save(new PartStyle(null, "My print").set(PartStyleOption.FONT_SIZE, "11pt").setForExport(true));
        styleService.save(new PartStyle(null, "Reading").set(PartStyleOption.FONT_SIZE, "1rem"));

        var field = editor().getStyleField("pdf");

        assertThat(field.getListDataView().getItems().map(id -> field.getItemLabelGenerator().apply(id)).toList())
                .containsExactly("E-book (EPUB)", "HTML page", "My print", "PDF book", "Word document");
    }

    @Test
    void editsAreStoredOnlyOnSaveAndTellThePage() throws Exception {
        PartStyle mine = styleService.save(new PartStyle(null, "My print").set(PartStyleOption.FONT_SIZE, "11pt").setForExport(true));
        ExportSettingsEditor editor = editor();
        AtomicInteger changes = new AtomicInteger();
        editor.setOnChange(changes::incrementAndGet);

        editor.getStyleField("pdf").setValue(mine.getId());
        editor.getTemplateField("txt", "titlePage").setValue("== {{title}} ==");

        assertThat(changes.get()).isEqualTo(2);
        assertThat(settingsService.getChosenStyleId("pdf")).isNull();
        assertThat(settingsService.getTemplate("txt", "titlePage")).isNull();

        assertThat(editor.validate()).isTrue();
        editor.save();

        assertThat(settingsService.getChosenStyleId("pdf")).isEqualTo(mine.getId());
        assertThat(settingsService.getTemplate("txt", "titlePage")).isEqualTo("== {{title}} ==");
        assertThat(changes.get()).as("saving is not a change").isEqualTo(2);
    }

    @Test
    void theStoredSettingsAreShownAndReloadDropsTheEdits() throws Exception {
        settingsService.setStyleId("pdf", "export-html");
        settingsService.setTemplate("md", ExporterBase.TITLE_PAGE_TEMPLATE, "# {{title}}!");
        ExportSettingsEditor editor = editor();

        assertThat(editor.getStyleField("pdf").getValue()).isEqualTo("export-html");
        assertThat(editor.getTemplateField("md", "titlePage").getValue()).isEqualTo("# {{title}}!");

        editor.getStyleField("pdf").setValue("export-epub");
        editor.getTemplateField("md", "titlePage").setValue("changed");
        AtomicInteger changes = new AtomicInteger();
        editor.setOnChange(changes::incrementAndGet);
        editor.reload();

        assertThat(editor.getStyleField("pdf").getValue()).isEqualTo("export-html");
        assertThat(editor.getTemplateField("md", "titlePage").getValue()).isEqualTo("# {{title}}!");
        assertThat(changes.get()).as("filling the fields is not a change").isZero();
    }

    @Test
    void theDefaultsThemselvesAreNotStored() throws Exception {
        settingsService.setStyleId("pdf", "export-html");
        settingsService.setTemplate("md", ExporterBase.TITLE_PAGE_TEMPLATE, "mine");
        ExportSettingsEditor editor = editor();

        editor.getStyleField("pdf").setValue("export-pdf");
        editor.getTemplateField("md", "titlePage").setValue(ExporterBase.TITLE_PAGE_TEMPLATE.defaultText());
        editor.save();

        assertThat(settingsService.getChosenStyleId("pdf")).isNull();
        assertThat(settingsService.getTemplate("md", "titlePage")).isNull();
    }

    @Test
    void aStyleDeletedBeforeTheSaveCountsAsNone() throws Exception {
        PartStyle mine = styleService.save(new PartStyle(null, "My print").set(PartStyleOption.FONT_SIZE, "11pt").setForExport(true));
        ExportSettingsEditor editor = editor();
        editor.getStyleField("pdf").setValue(mine.getId());

        styleService.delete(mine.getId());
        editor.save();

        assertThat(settingsService.getChosenStyleId("pdf")).isNull();
    }

    @Test
    void anInvalidTemplateStopsTheSave() throws Exception {
        ExportSettingsEditor editor = editor();

        editor.getTemplateField("txt", "titlePage").setValue("{{#title}} never closed");

        assertThat(editor.validate()).isFalse();
    }
}
