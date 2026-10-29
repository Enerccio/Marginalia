package com.github.enerccio.marginalia.backup;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.BackupService.ManuscriptBackup;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.PromptField;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The folder of a book travels in its backup: a book restored from it goes into the same folder, else into the folders of
 * the same names (made with the settings they had), else to the top level.
 */
class BackupFoldersTest extends BackupTestBase {

    @Autowired
    private FolderService folderService;

    private Folder scifi;
    private Folder expanse;
    private Manuscript original;
    private ManuscriptBackup backup;

    @BeforeEach
    void fileTheBook() throws Exception {
        login();
        scifi = folderService.create("Sci-fi", null);
        scifi = folderService.update(scifi, new FolderService.Details("Sci-fi", "Space stories", Language.CZECH,
                Map.of(PromptField.STYLE, "sparse")));
        expanse = folderService.create("Expanse", scifi);
        original = story(manuscript("Leviathan", null, null, null));
        manuscriptService.moveToFolder(java.util.List.of(original), expanse);
        backup = backupService.takeBackup(original);
    }

    private Manuscript restored() throws Exception {
        return restoredFrom(FileUtils.readFileToString(new File(backup.getFile()), StandardCharsets.UTF_8));
    }

    private Manuscript restoredFrom(String json) throws Exception {
        return reload(backupService.restoreAsNewManuscript(json.getBytes(StandardCharsets.UTF_8), "Restored", Map.of()));
    }

    private void forgetTheFolders() throws Exception {
        folderService.deleteWithContent(folderService.find(scifi.getId()));
    }

    private String path(Manuscript book) throws Exception {
        return folderService.getPath(manuscriptService.find(book.getId()).getFolder());
    }

    @Test
    void theBackupNamesTheFoldersFromTheTopOne() throws Exception {
        JsonObject folder = backup.getBackup().getAsJsonObject("folder");

        assertThat(folder.get("uuid").getAsString()).isEqualTo(expanse.getUuid());
        assertThat(folder.getAsJsonArray("path")).hasSize(2);
        JsonObject top = folder.getAsJsonArray("path").get(0).getAsJsonObject();
        assertThat(top.get("name").getAsString()).isEqualTo("Sci-fi");
        assertThat(top.get("language").getAsString()).isEqualTo("CZECH");
        assertThat(top.getAsJsonObject("settings").get("STYLE").getAsString()).isEqualTo("sparse");
    }

    @Test
    void aBookAtTheTopLevelHasNoFolderInItsBackup() throws Exception {
        Manuscript loose = manuscript("Loose", null, null, null);

        ManuscriptBackup looseBackup = backupService.takeBackup(loose);

        assertThat(looseBackup.getBackup().has("folder")).isFalse();
        assertThat(reload(backupService.cloneBackup(looseBackup, null, null)).getFolder()).isNull();
    }

    @Test
    void aRestoredBookGoesIntoTheFolderItWasIn() throws Exception {
        assertThat(path(restored())).isEqualTo("Sci-fi/Expanse");
        assertThat(folderService.findChildren(expanse)).isEmpty();
    }

    @Test
    void aClonedBackupGoesIntoTheFolderItWasIn() throws Exception {
        Manuscript clone = reload(backupService.cloneBackup(backup, "Clone", null));

        assertThat(path(clone)).isEqualTo("Sci-fi/Expanse");
    }

    @Test
    void theFolderOfTheUuidWinsOverTheNames() throws Exception {
        // the folder was renamed and moved since the backup was taken
        Folder elsewhere = folderService.create("Elsewhere", null);
        folderService.rename(expanse, "Renamed");
        folderService.move(folderService.find(expanse.getId()), elsewhere);

        assertThat(path(restored())).isEqualTo("Elsewhere/Renamed");
    }

    @Test
    void missingFoldersAreMadeWithTheSettingsTheyHad() throws Exception {
        forgetTheFolders();

        Manuscript book = restored();

        assertThat(path(book)).isEqualTo("Sci-fi/Expanse");
        Folder made = folderService.findChild(null, "Sci-fi");
        assertThat(made.getId()).isNotEqualTo(scifi.getId());
        assertThat(made.getDescription()).isEqualTo("Space stories");
        assertThat(made.getLanguage()).isEqualTo(Language.CZECH);
        assertThat(PromptField.STYLE.ofFolder(made)).isEqualTo("sparse");
        assertThat(manuscriptService.getLanguage(book)).isEqualTo(Language.CZECH);
    }

    @Test
    void aFolderThatIsThereByNameIsUsedAndLeftAsItIs() throws Exception {
        forgetTheFolders();
        Folder mine = folderService.create("sci-fi", null);

        Manuscript book = restored();

        assertThat(path(book)).isEqualTo("sci-fi/Expanse");
        assertThat(folderService.findChildren(mine)).hasSize(1);
        Folder same = folderService.find(mine.getId());
        assertThat(same.getLanguage()).isNull();
        assertThat(PromptField.STYLE.ofFolder(same)).isNull();
        assertThat(folderService.findChildren(null)).hasSize(1);
    }

    @Test
    void aFolderThatCantBeMadeLeavesTheBookAtTheTopLevel() throws Exception {
        forgetTheFolders();
        String json = FileUtils.readFileToString(new File(backup.getFile()), StandardCharsets.UTF_8)
                .replace("\"Expanse\"", "\"Ex/panse\"");

        Manuscript book = restoredFrom(json);

        assertThat(manuscriptService.find(book.getId()).getName()).isEqualTo("Restored");
        assertThat(manuscriptService.find(book.getId()).getFolder()).isNull();
    }

    @Test
    void aBackupWithoutAFolderRestoresAtTheTopLevel() throws Exception {
        String json = FileUtils.readFileToString(new File(backup.getFile()), StandardCharsets.UTF_8);
        JsonObject root = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
        root.getAsJsonObject("backup").remove("folder");

        Manuscript book = restoredFrom(root.toString());

        assertThat(manuscriptService.find(book.getId()).getFolder()).isNull();
    }

    @Test
    void applyingABackupKeepsTheBookWhereItIs() throws Exception {
        Folder other = folderService.create("Other", null);
        manuscriptService.moveToFolder(java.util.List.of(manuscriptService.find(original.getId())), other);

        backupService.applyBackup(manuscriptService.find(original.getId()), backup, false, Map.of());

        assertThat(path(original)).isEqualTo("Other");
    }

    @Test
    void aClonedBookStaysInTheFolderOfTheOriginal() throws Exception {
        Manuscript clone = reload(backupService.cloneManuscript(manuscriptService.find(original.getId()), "Copy", false));

        assertThat(path(clone)).isEqualTo("Sci-fi/Expanse");
    }

    @Test
    void aClonedBookCanBePutIntoAnotherFolderOrTheTopLevel() throws Exception {
        Folder other = folderService.create("Other", null);
        Manuscript source = manuscriptService.find(original.getId());

        assertThat(path(backupService.cloneManuscript(source, "In other", false, other))).isEqualTo("Other");
        assertThat(manuscriptService.find(backupService.cloneManuscript(source, "Loose", false, null).getId()).getFolder()).isNull();
    }
}
