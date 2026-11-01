package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.service.ExportSettingsService;
import com.github.enerccio.marginalia.domain.service.ExporterService;
import com.github.enerccio.marginalia.domain.service.ExporterService.Exporter;
import com.github.enerccio.marginalia.domain.service.PartStyleService;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.styles.PartStyleOption;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * What a user keeps of their exports: the style each format is exported in, and their versions of the templates.
 */
class ExportSettingsServiceTest extends MarginaliaTestBase {

    @Autowired
    private ExportSettingsService service;

    @Autowired
    private PartStyleService styleService;

    @Autowired
    private ExporterService exporterService;

    @BeforeEach
    void user() throws Exception {
        login();
    }

    private Exporter exporter(String id) {
        return exporterService.getExporters().stream().filter(e -> e.getId().equals(id)).findFirst().orElseThrow();
    }

    private PartStyle exportStyle(String name) throws Exception {
        return styleService.save(new PartStyle(null, name).set(PartStyleOption.FONT_SIZE, "11pt").setForExport(true));
    }

    @Test
    void aUserWhoChangedNothingGetsTheOwnStyleOfTheFormat() throws Exception {
        assertThat(service.getChosenStyleId("pdf")).isNull();
        assertThat(service.getStyleId(exporter("pdf"))).isEqualTo("export-pdf");
        assertThat(service.getStyleId(exporter("txt"))).isNull();
        assertThat(service.getTemplates("pdf")).isEmpty();
    }

    @Test
    void theChosenStyleIsTheOneOfThatFormatOnly() throws Exception {
        PartStyle mine = exportStyle("My print");

        service.setStyleId("pdf", mine.getId());

        assertThat(service.getStyleId(exporter("pdf"))).isEqualTo(mine.getId());
        assertThat(service.getChosenStyleId("pdf")).isEqualTo(mine.getId());
        assertThat(service.getStyleId(exporter("docx"))).isEqualTo("export-docx");
    }

    @Test
    void aBuiltInStyleOfAnotherFormatCanBeChosen() throws Exception {
        service.setStyleId("html", "export-pdf");

        assertThat(service.getStyleId(exporter("html"))).isEqualTo("export-pdf");
    }

    @Test
    void nullReturnsTheFormatToItsOwnStyle() throws Exception {
        service.setStyleId("pdf", exportStyle("My print").getId());

        service.setStyleId("pdf", null);

        assertThat(service.getChosenStyleId("pdf")).isNull();
        assertThat(service.getStyleId(exporter("pdf"))).isEqualTo("export-pdf");
    }

    @Test
    void aStyleOfBooksOrAStyleThatDoesNotExistCanNotBeChosen() throws Exception {
        PartStyle reading = styleService.save(new PartStyle(null, "Reading").set(PartStyleOption.FONT_SIZE, "1rem"));

        assertThatThrownBy(() -> service.setStyleId("pdf", reading.getId())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.setStyleId("pdf", "book")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.setStyleId("pdf", "nothing")).isInstanceOf(IllegalArgumentException.class);
        assertThat(service.getChosenStyleId("pdf")).isNull();
    }

    @Test
    void aDeletedOrChangedStyleFallsBackToTheFormat() throws Exception {
        PartStyle mine = exportStyle("My print");
        service.setStyleId("pdf", mine.getId());

        mine.setForExport(false);
        styleService.save(mine);
        assertThat(service.getStyleId(exporter("pdf"))).isEqualTo("export-pdf");

        mine.setForExport(true);
        styleService.save(mine);
        assertThat(service.getStyleId(exporter("pdf"))).isEqualTo(mine.getId());

        styleService.delete(mine.getId());
        assertThat(service.getStyleId(exporter("pdf"))).isEqualTo("export-pdf");
    }

    @Test
    void aTemplateIsStoredPerFormatAndKey() throws Exception {
        service.setTemplate("txt", ExporterBase.TITLE_PAGE_TEMPLATE, "== {{title}} ==");

        assertThat(service.getTemplate("txt", "titlePage")).isEqualTo("== {{title}} ==");
        assertThat(service.getTemplates("txt")).containsExactly(java.util.Map.entry("titlePage", "== {{title}} =="));
        assertThat(service.getTemplate("md", "titlePage")).isNull();
        assertThat(service.getTemplates("md")).isEmpty();
    }

    @Test
    void aBlankTemplateReturnsToTheDefault() throws Exception {
        service.setTemplate("txt", ExporterBase.TITLE_PAGE_TEMPLATE, "{{title}}");
        service.setTemplate("md", ExporterBase.TITLE_PAGE_TEMPLATE, "{{title}} again");

        service.setTemplate("txt", ExporterBase.TITLE_PAGE_TEMPLATE, "  ");

        assertThat(service.getTemplate("txt", "titlePage")).isNull();
        assertThat(service.getTemplates("txt")).isEmpty();
        assertThat(service.getTemplate("md", "titlePage")).isEqualTo("{{title}} again");
    }

    @Test
    void anInvalidTemplateIsRefused() throws Exception {
        assertThatThrownBy(() -> service.setTemplate("txt", ExporterBase.TITLE_PAGE_TEMPLATE, "{{#title}} never closed"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(service.getTemplate("txt", "titlePage")).isNull();
    }

    @Test
    void theSettingsBelongToTheUser() throws Exception {
        service.setStyleId("pdf", "export-html");
        service.setTemplate("txt", ExporterBase.TITLE_PAGE_TEMPLATE, "{{title}}");

        login();

        assertThat(service.getChosenStyleId("pdf")).isNull();
        assertThat(service.getTemplate("txt", "titlePage")).isNull();
    }
}
