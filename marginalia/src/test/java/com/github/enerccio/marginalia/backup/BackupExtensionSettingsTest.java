package com.github.enerccio.marginalia.backup;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupExport;
import com.github.enerccio.marginalia.domain.service.BackupService.ManuscriptBackup;
import com.github.enerccio.marginalia.domain.service.EffectiveSetting;
import com.github.enerccio.marginalia.domain.service.EffectiveSetting.Origin;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.extensions.InheritedSetting;
import com.github.enerccio.marginalia.extensions.InheritedSettingRegistry;
import com.github.enerccio.marginalia.extensions.InheritedSettingValues;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The settings extensions added to the books ({@link InheritedSetting}) travel with the backup of a book: the values of the folders
 * above it are in its folder path (and come back when the folders are made again), and an export that writes what the book
 * inherits writes them, and the user level of the extension, as the book's own.
 */
class BackupExtensionSettingsTest extends BackupTestBase {

    private static final String OWNER = "test-extension";
    private static final String MEDIUM = "test.medium";
    private static final String TONE = "test.tone";
    private static final String NOTE = "test.note";

    @Autowired
    private FolderService folderService;

    @Autowired
    private InheritedSettingRegistry registry;

    private Folder scifi;
    private Folder expanse;
    private Manuscript original;

    @BeforeEach
    void fileTheBook() throws Exception {
        login();
        registry.register(OWNER, InheritedSetting.text(MEDIUM, () -> "Medium"));
        registry.register(OWNER, InheritedSetting.text(TONE, () -> "Tone").userValue(() -> "dry"));
        registry.register(OWNER, InheritedSetting.text(NOTE, () -> "Note"));
        scifi = folderService.create("Sci-fi", null);
        scifi = folderService.update(scifi, new FolderService.Details("Sci-fi", null, null, Map.of(), null,
                Map.of(MEDIUM, "movie", NOTE, "far note")));
        expanse = folderService.create("Expanse", scifi);
        expanse = folderService.update(expanse, new FolderService.Details("Expanse", null, null, Map.of(), null, Map.of(NOTE, "near note")));
        original = story(manuscript("Leviathan", null, null, null));
        InheritedSettingValues.set(original, NOTE, "own note");
        original = manuscriptService.save(original);
        manuscriptService.moveToFolder(List.of(original), expanse);
    }

    @AfterEach
    void unregister() {
        registry.unregisterAll(OWNER);
    }

    private EffectiveSetting<String> resolve(Manuscript book, String key) throws Exception {
        return manuscriptService.resolveExtension(manuscriptService.find(book.getId()), key);
    }

    private Manuscript exportedAndImported(boolean includeInherited) throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(manuscriptService.find(original.getId()));
        BackupExport export = backupService.exportBackup(backup, false, includeInherited, null);
        try {
            return reload(backupService.restoreAsNewManuscript(FileUtils.readFileToByteArray(export.file()), "Imported", Map.of()));
        } finally {
            FileUtils.deleteQuietly(export.file());
        }
    }

    @Test
    void theFoldersAreMadeAgainWithTheirExtensionSettings() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(manuscriptService.find(original.getId()));
        String json = FileUtils.readFileToString(new File(backup.getFile()), StandardCharsets.UTF_8);
        folderService.deleteWithContent(folderService.find(scifi.getId()));

        Manuscript restored = reload(backupService.restoreAsNewManuscript(json.getBytes(StandardCharsets.UTF_8), "Restored", Map.of()));

        EffectiveSetting<String> medium = resolve(restored, MEDIUM);
        assertThat(medium.value()).isEqualTo("movie");
        assertThat(medium.origin()).isEqualTo(Origin.FOLDER);
        assertThat(medium.folder().getName()).isEqualTo("Sci-fi");
        // the book keeps its own value, the folders their notes
        assertThat(resolve(restored, NOTE).value()).isEqualTo("own note");
        Long nearId = manuscriptService.find(restored.getId()).getFolder().getId();
        assertThat(InheritedSettingValues.get(folderService.find(nearId), NOTE)).isEqualTo("near note");
    }

    @Test
    void anExportWritesWhatTheBookInheritsAsItsOwn() throws Exception {
        Manuscript book = exportedAndImported(true);

        // the folder of the book is not in the export, so only its own values can say where this one comes from
        EffectiveSetting<String> medium = resolve(book, MEDIUM);
        assertThat(medium.value()).isEqualTo("movie");
        assertThat(medium.origin()).isEqualTo(Origin.BOOK);
        // the user level of the extension comes after the folders
        assertThat(resolve(book, TONE).value()).isEqualTo("dry");
        assertThat(resolve(book, TONE).origin()).isEqualTo(Origin.BOOK);
        // what the book sets itself is not replaced by the folders' value
        assertThat(resolve(book, NOTE).value()).isEqualTo("own note");
    }

    @Test
    void withoutTheOptionOnlyTheValuesOfTheBookAreExported() throws Exception {
        Manuscript book = exportedAndImported(false);

        assertThat(resolve(book, MEDIUM).origin()).isNotEqualTo(Origin.BOOK);
        assertThat(resolve(book, NOTE).value()).isEqualTo("own note");
        assertThat(InheritedSettingValues.all(manuscriptService.find(book.getId()))).containsExactly(Map.entry(NOTE, "own note"));
    }
}
