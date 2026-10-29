package com.github.enerccio.marginalia.backup;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupExport;
import com.github.enerccio.marginalia.domain.service.BackupService.ManuscriptBackup;
import com.github.enerccio.marginalia.domain.service.EffectiveSetting;
import com.github.enerccio.marginalia.domain.service.EffectiveSetting.Origin;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.PromptField;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The file of an exported backup says nothing of the folder of the book, and (when asked) carries what the book inherits as
 * its own values, so a book read where there are no such folders and settings reads the same.
 */
class BackupExportInheritanceTest extends BackupTestBase {

    @Autowired
    private FolderService folderService;

    @Autowired
    private SettingService settingService;

    private Manuscript original;

    @BeforeEach
    void fileTheBook() throws Exception {
        login();
        Folder far = folderService.create("Sci-fi", null);
        far = folderService.update(far, new FolderService.Details("Sci-fi", null, Language.CZECH,
                Map.of(PromptField.REDRAFT_PROMPT, "far redraft", PromptField.ANNOTATE_PROMPT, "far annotate", PromptField.STYLE, "far style")));
        Folder near = folderService.create("Expanse", far);
        near = folderService.update(near, new FolderService.Details("Expanse", null, null, Map.of(PromptField.REDRAFT_PROMPT, "near redraft")));
        UserSetting user = settingService.getOrCreate(UserSetting.class);
        user.setDefaultImpersonatePrompt("user impersonate");
        settingService.save(user);
        // the fixture book sets its own pov, tense, style, template, user prompt and summary prompt
        original = story(manuscript("Leviathan", null, null, null));
        manuscriptService.moveToFolder(List.of(original), near);
    }

    private byte[] exported(boolean includeInherited) throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(original);
        BackupExport export = backupService.exportBackup(backup, false, includeInherited, null);
        try {
            return FileUtils.readFileToByteArray(export.file());
        } finally {
            FileUtils.deleteQuietly(export.file());
        }
    }

    private Manuscript imported(byte[] data) throws Exception {
        return reload(backupService.restoreAsNewManuscript(data, "Imported", Map.of()));
    }

    private EffectiveSetting<String> resolve(Manuscript book, PromptField field) throws Exception {
        return manuscriptService.resolve(manuscriptService.find(book.getId()), field);
    }

    @Test
    void theStoredBackupKeepsTheFolderButTheExportDoesNot() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(original);
        assertThat(backup.getBackup().has("folder")).isTrue();

        for (boolean inherited : new boolean[]{true, false}) {
            JsonObject file = JsonParser.parseString(new String(exported(inherited), StandardCharsets.UTF_8)).getAsJsonObject();
            assertThat(file.getAsJsonObject("backup").has("folder")).isFalse();
        }
        assertThat(imported(exported(true)).getFolder()).isNull();
    }

    @Test
    void whatTheBookInheritsIsWrittenAsItsOwn() throws Exception {
        Manuscript book = imported(exported(true));

        // the nearest folder wins, then the one above, then the user settings
        assertThat(resolve(book, PromptField.REDRAFT_PROMPT).value()).isEqualTo("near redraft");
        assertThat(resolve(book, PromptField.REDRAFT_PROMPT).origin()).isEqualTo(Origin.BOOK);
        assertThat(resolve(book, PromptField.ANNOTATE_PROMPT).value()).isEqualTo("far annotate");
        assertThat(resolve(book, PromptField.ANNOTATE_PROMPT).origin()).isEqualTo(Origin.BOOK);
        assertThat(resolve(book, PromptField.IMPERSONATE_PROMPT).value()).isEqualTo("user impersonate");
        assertThat(resolve(book, PromptField.IMPERSONATE_PROMPT).origin()).isEqualTo(Origin.BOOK);
        Manuscript stored = manuscriptService.find(book.getId());
        assertThat(stored.getOwnLanguage()).isEqualTo(Language.CZECH);
        assertThat(manuscriptService.getLanguage(stored)).isEqualTo(Language.CZECH);
    }

    @Test
    void theValuesTheBookSetsItselfStay() throws Exception {
        Manuscript book = imported(exported(true));

        assertThat(resolve(book, PromptField.STYLE).value()).isEqualTo("terse");
        assertThat(resolve(book, PromptField.POV).value()).isEqualTo("first person");
    }

    @Test
    void aBuiltInDefaultIsNotWritten() throws Exception {
        Manuscript book = imported(exported(true));

        assertThat(resolve(book, PromptField.META_SUMMARY_PROMPT).origin()).isEqualTo(Origin.DEFAULT);
    }

    @Test
    void withoutTheOptionTheBookKeepsOnlyItsOwn() throws Exception {
        Manuscript book = imported(exported(false));

        assertThat(resolve(book, PromptField.REDRAFT_PROMPT).origin()).isEqualTo(Origin.DEFAULT);
        // the user settings are this account's, so the book still reads them here; it is only not written into the file
        assertThat(resolve(book, PromptField.IMPERSONATE_PROMPT).origin()).isEqualTo(Origin.USER);
        assertThat(manuscriptService.find(book.getId()).getOwnLanguage()).isNull();
        assertThat(resolve(book, PromptField.STYLE).value()).isEqualTo("terse");
    }

    @Test
    void theArchiveWithImagesIsWrittenTheSameWay() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(original);
        BackupExport export = backupService.exportBackup(backup, true, true, null);
        try {
            String json = null;
            try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(FileUtils.readFileToByteArray(export.file())))) {
                for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                    if (entry.getName().equals("backup.json")) {
                        json = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                    }
                }
            }
            assertThat(json).isNotNull().doesNotContain("\"folder\"");
            Manuscript book = reload(backupService.restoreAsNewManuscript(FileUtils.readFileToByteArray(export.file()), "Zipped", Map.of()));
            assertThat(resolve(book, PromptField.REDRAFT_PROMPT).origin()).isEqualTo(Origin.BOOK);
        } finally {
            FileUtils.deleteQuietly(export.file());
        }
    }

    @Test
    void aBookAtTheTopLevelGetsTheUserSettingsOnly() throws Exception {
        Manuscript loose = story(manuscript("Loose", null, null, null));
        ManuscriptBackup backup = backupService.takeBackup(loose);
        BackupExport export = backupService.exportBackup(backup, false, true, null);
        byte[] data = FileUtils.readFileToByteArray(export.file());
        FileUtils.deleteQuietly(export.file());

        Manuscript book = imported(data);

        assertThat(resolve(book, PromptField.IMPERSONATE_PROMPT).origin()).isEqualTo(Origin.BOOK);
        assertThat(resolve(book, PromptField.REDRAFT_PROMPT).origin()).isEqualTo(Origin.DEFAULT);
        assertThat(manuscriptService.find(book.getId()).getOwnLanguage()).isNull();
    }
}
