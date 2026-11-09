package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ExportSettingsService;
import com.github.enerccio.marginalia.domain.service.ExporterService;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportTemplate;
import com.github.enerccio.marginalia.domain.service.ExporterService.Exporter;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.impl.ExportInheritedSettings;
import com.github.enerccio.marginalia.extensions.InheritedSetting;
import com.github.enerccio.marginalia.extensions.InheritedSettingRegistry;
import com.github.enerccio.marginalia.extensions.InheritedSettingValues;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The templates of the exporters are settings like the prompts: the book, else the nearest folder above it, else the user's own version (<i>Settings → Exports</i>),
 * else the template of the exporter; a template that none of them changed is not passed to the exporter.
 */
class ExportTemplateInheritanceTest extends MarginaliaTestBase {

    @Autowired
    private ExporterService exporterService;

    @Autowired
    private ExportSettingsService exportSettingsService;

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private InheritedSettingRegistry registry;

    private Exporter exporter;
    private ExportTemplate template;
    private String key;

    @BeforeEach
    void pickAnExporterWithATemplate() throws Exception {
        login();
        exporter = exporterService.getExporters().stream().filter(e -> !e.getTemplates().isEmpty()).findFirst().orElseThrow();
        template = exporter.getTemplates().getFirst();
        key = ExportInheritedSettings.key(exporter.getId(), template.key());
    }

    private Manuscript book(Folder folder) throws Exception {
        Manuscript book = new Manuscript();
        book.setName("book");
        book.setFolder(folder);
        return manuscriptService.save(book);
    }

    private Map<String, String> templatesOf(Manuscript book) throws Exception {
        return exportSettingsService.getTemplates(exporter, manuscriptService.find(book.getId()));
    }

    @Test
    void everyTemplateOfAnExporterIsASettingBooksAndFoldersInherit() {
        InheritedSetting setting = registry.find(key);

        assertThat(setting).as("registered with the exporter").isNotNull();
        assertThat(setting.getKind()).isEqualTo(InheritedSetting.Kind.TEXT_AREA);
        assertThat(setting.label()).isEqualTo(template.label());
        assertThat(setting.group()).contains(exporter.getName());
        assertThat(setting.fallback()).isEqualTo(template.defaultText());
        assertThat(setting.variables()).isEqualTo(template.dataClass());
    }

    @Test
    void aBookThatNobodyChangedPassesNothing() throws Exception {
        assertThat(templatesOf(book(null))).isEmpty();
    }

    @Test
    void theUserLevelComesBeforeTheDefault() throws Exception {
        exportSettingsService.setTemplate(exporter.getId(), template, "user {{title}}");

        assertThat(templatesOf(book(null))).containsEntry(template.key(), "user {{title}}");
    }

    @Test
    void aFolderBeatsTheUserAndTheBookBeatsTheFolder() throws Exception {
        exportSettingsService.setTemplate(exporter.getId(), template, "user {{title}}");
        Folder folder = folderService.create("Series", null);
        folder = folderService.update(folder, new FolderService.Details(folder.getName(), null, null, Map.of(), null, Map.of(key, "folder {{title}}")));
        Manuscript book = book(folder);

        assertThat(templatesOf(book)).containsEntry(template.key(), "folder {{title}}");

        InheritedSettingValues.set(book, key, "book {{title}}");
        book = manuscriptService.save(book);

        assertThat(templatesOf(book)).containsEntry(template.key(), "book {{title}}");
        assertThat(templatesOf(book(folder))).as("another book of the folder").containsEntry(template.key(), "folder {{title}}");
        assertThat(exportSettingsService.getTemplate(exporter.getId(), template.key())).as("the user's own is untouched").isEqualTo("user {{title}}");
    }
}
