package com.github.enerccio.marginalia.backup;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.BackupService.ManuscriptBackup;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Restoring a backup into an existing manuscript, in full and messages only. After taking the backup the manuscript
 * is changed in every restorable way, so each test can tell restored state from current state.
 */
class ManuscriptRestoreTest extends BackupTestBase {

    private AI backupAi;
    private Protocol backupProtocol;
    private Lorebook backupLorebook;
    private AI currentAi;
    private Protocol currentProtocol;
    private Lorebook currentLorebook;
    private Manuscript manuscript;
    private ManuscriptBackup backup;
    private List<Long> liveMessages;
    private List<Long> liveSummaries;

    @BeforeEach
    void backupThenChangeEverything() throws Exception {
        login();
        backupAi = ai(uniqueName("backup ai"));
        backupProtocol = protocol(uniqueName("backup protocol"));
        backupLorebook = lorebook("Backup lore");
        manuscript = manuscript("Book", backupAi, backupProtocol, backupLorebook);
        tagRelationService.createRelation(tag("fantasy"), manuscript);
        manuscript = story(manuscript);
        backup = backupService.takeBackup(manuscript);

        // change everything after the backup
        currentAi = ai(uniqueName("current ai"));
        currentProtocol = protocol(uniqueName("current protocol"));
        currentLorebook = lorebook("Current lore");
        Manuscript changed = reload(manuscript);
        changed.setName("Renamed");
        changed.setDescription("changed description");
        changed.setPov("second person");
        changed.setBackupStrategy(BackupStrategy.DISABLED);
        changed.getAttributes().addProperty("plugin.flag", "off");
        changed.getAttributes().addProperty("plugin.added", "later");
        changed.setAi(currentAi);
        changed.setProtocol(currentProtocol);
        changed.setLorebook(currentLorebook);
        changed.setPublished(true);
        manuscriptService.save(changed);
        tagRelationService.removeRelation(tag("fantasy"), manuscript);
        tagRelationService.createRelation(tag("scifi"), manuscript);

        ChatMessage chapter1 = messageByResponse(manuscript, "chapter 1");
        ChatMessage newLeaf = message(reload(manuscript), chapter1, "chapter 3 written later");
        chatMessageService.deleteNodeAndMigrateChildren(messageByResponse(manuscript, "chapter 2b"), reload(manuscript), false);
        ChatMessage edited = chatMessageService.find(messageByResponse(manuscript, "prologue").getId());
        edited.setResponse("prologue rewritten");
        chatMessageService.save(edited);
        setActiveLeaf(manuscript, newLeaf);

        liveMessages = chatMessageService.getAllMessages(manuscript).stream().map(ChatMessage::getId).toList();
        liveSummaries = chatMessageService.getAllMessages(manuscript).stream()
                .filter(m -> m.getSummary() != null).map(m -> m.getSummary().getId()).toList();
        assertThat(liveSummaries).hasSize(1);
    }

    private Map<String, String> changedTree() {
        return new java.util.TreeMap<>(Map.of(
                "prologue rewritten", "",
                "chapter 1", "prologue rewritten",
                "chapter 2a", "chapter 1",
                "chapter 3 written later", "chapter 1"));
    }

    private void assertMessagesRestored(Manuscript restored) throws Exception {
        assertThat(tree(restored)).isEqualTo(storyTree());
        assertThat(activeLeafResponse(restored)).isEqualTo("chapter 2a");
        assertThat(messageByResponse(restored, "chapter 1").isEdited()).isTrue();
        assertThat(messageByResponse(restored, "chapter 1").getInstructions()).isEqualTo("instructions for chapter 1");
        ChatMessage twoA = messageByResponse(restored, "chapter 2a");
        assertThat(summaryService.find(twoA.getSummary().getId()).getSummary()).isEqualTo("summary up to 2a");
        assertThat(chatMessageService.getTotalWordCount(restored))
                .isEqualTo("prologue".length() + "chapter 1".length() + "chapter 2a".length() + "chapter 2b".length());
    }

    private void assertCurrentStoryReplaced() throws Exception {
        // messages live before the restore and their summaries are hard deleted, restored ones are new rows
        for (Long id : liveMessages) {
            assertThat(chatMessageService.find(id)).as("message " + id).isNull();
        }
        for (Long id : liveSummaries) {
            assertThat(summaryService.find(id)).as("summary " + id).isNull();
        }
        assertThat(chatMessageService.getAllMessages(manuscript)).hasSize(4);
    }

    // ---- full restore ----------------------------------------------------------------------------------------------

    @Test
    void fullRestoreBringsBackManuscriptSettings() throws Exception {
        Manuscript restored = reload(backupService.applyBackup(manuscript, backup, false, Map.of()));

        assertThat(restored.getId()).isEqualTo(manuscript.getId());
        assertThat(restored.getUuid()).isEqualTo(manuscript.getUuid());
        assertThat(restored.getName()).isEqualTo("Book");
        assertThat(restored.getDescription()).isEqualTo("description of Book");
        assertThat(restored.getPov()).isEqualTo("first person");
        assertThat(restored.getBackupStrategy()).isEqualTo(BackupStrategy.AFTER_N_MESSAGES);
        assertThat(restored.getAttributes().get("plugin.flag").getAsString()).isEqualTo("on");
        assertThat(restored.getAttributes().has("plugin.added")).isFalse();
        assertThat(idOf(restored.getAi())).isEqualTo(backupAi.getId());
        assertThat(idOf(restored.getProtocol())).isEqualTo(backupProtocol.getId());
        assertThat(idOf(restored.getLorebook())).isEqualTo(backupLorebook.getId());
        assertThat(tagValues(restored)).containsExactly("fantasy");
        assertThat(restored.isPublished()).isFalse();
    }

    @Test
    void fullRestoreRecreatesDeletedTags() throws Exception {
        tagService.delete(tag("fantasy"), true);

        Manuscript restored = reload(backupService.applyBackup(manuscript, backup, false, Map.of()));

        assertThat(tagValues(restored)).containsExactly("fantasy");
    }

    @Test
    void fullRestoreOfOldBackupKeepsPublishedFlag() throws Exception {
        JsonObject json = manuscriptService.createBackup(manuscript);
        assertThat(json.get("published").getAsBoolean()).isTrue();
        json.remove("published");
        ManuscriptBackup old = backupService.importBackup(manuscript, json.toString().getBytes(StandardCharsets.UTF_8));

        Manuscript restored = reload(backupService.applyBackup(manuscript, old, false, Map.of()));

        assertThat(restored.isPublished()).isTrue();
    }

    @Test
    void fullRestoreBringsBackMessages() throws Exception {
        Manuscript restored = backupService.applyBackup(manuscript, backup, false, Map.of());

        assertMessagesRestored(restored);
        assertCurrentStoryReplaced();
    }

    @Test
    void fullRestoreWithoutLorebookDecisionsLinksByUuid() throws Exception {
        Manuscript restored = reload(backupService.applyBackup(manuscript, backup, false, null));

        assertThat(idOf(restored.getLorebook())).isEqualTo(backupLorebook.getId());
    }

    @Test
    void restoringTwiceGivesSameResult() throws Exception {
        backupService.applyBackup(manuscript, backup, false, Map.of());
        Manuscript again = backupService.applyBackup(manuscript, backup, false, Map.of());

        assertMessagesRestored(again);
        assertThat(chatMessageService.getAllMessages(manuscript)).hasSize(4);
    }

    @Test
    void restoreFromListedBackupFile() throws Exception {
        ManuscriptBackup listed = backupService.getBackups(manuscript).getFirst();
        assertThat(listed.isLoaded()).isFalse();

        assertMessagesRestored(backupService.applyBackup(manuscript, listed, false, Map.of()));
    }

    @Test
    void restoreOfBackupOfAnotherManuscript() throws Exception {
        Manuscript other = manuscript("Other", null, null, null);
        message(other, null, "other story");

        Manuscript restored = reload(backupService.applyBackup(other, backup, false, Map.of()));

        assertThat(restored.getId()).isEqualTo(other.getId());
        assertThat(restored.getName()).isEqualTo("Book");
        assertThat(tree(restored)).isEqualTo(storyTree());
        // the backed up manuscript itself is not touched
        assertThat(tree(manuscript)).isEqualTo(changedTree());
    }

    @Test
    void restoreAfterUserChangedNothingKeepsStory() throws Exception {
        ManuscriptBackup current = backupService.takeBackup(reload(manuscript));

        Manuscript restored = backupService.applyBackup(manuscript, current, false, Map.of());

        assertThat(tree(restored)).isEqualTo(changedTree());
        assertThat(activeLeafResponse(restored)).isEqualTo("chapter 3 written later");
        assertThat(reload(restored).getName()).isEqualTo("Renamed");
    }

    @Test
    void restoreRequiresBackup() {
        assertThatThrownBy(() -> backupService.applyBackup(manuscript, null, false, Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ---- messages only ---------------------------------------------------------------------------------------------

    @Test
    void messagesOnlyRestoreBringsBackMessages() throws Exception {
        Manuscript restored = backupService.applyBackup(manuscript, backup, true, Map.of());

        assertMessagesRestored(restored);
        assertCurrentStoryReplaced();
    }

    @Test
    void messagesOnlyRestoreKeepsEverythingElse() throws Exception {
        Manuscript restored = reload(backupService.applyBackup(manuscript, backup, true, Map.of()));

        assertThat(restored.getName()).isEqualTo("Renamed");
        assertThat(restored.getDescription()).isEqualTo("changed description");
        assertThat(restored.getPov()).isEqualTo("second person");
        assertThat(restored.getBackupStrategy()).isEqualTo(BackupStrategy.DISABLED);
        assertThat(restored.getAttributes().get("plugin.flag").getAsString()).isEqualTo("off");
        assertThat(restored.getAttributes().get("plugin.added").getAsString()).isEqualTo("later");
        assertThat(idOf(restored.getAi())).isEqualTo(currentAi.getId());
        assertThat(idOf(restored.getProtocol())).isEqualTo(currentProtocol.getId());
        assertThat(idOf(restored.getLorebook())).isEqualTo(currentLorebook.getId());
        assertThat(tagValues(restored)).containsExactly("scifi");
        assertThat(restored.isPublished()).isTrue();
    }

    @Test
    void messagesOnlyRestoreWithoutLorebookDecisions() throws Exception {
        Manuscript restored = reload(backupService.applyBackup(manuscript, backup, true, null));

        assertThat(idOf(restored.getLorebook())).isEqualTo(currentLorebook.getId());
        assertThat(tree(restored)).isEqualTo(storyTree());
    }

    @Test
    void messagesOnlyRestoreOfEmptyBackupClearsStory() throws Exception {
        Manuscript empty = new Manuscript();
        empty.setName("Empty");
        ManuscriptBackup emptyBackup = backupService.takeBackup(manuscriptService.save(empty));

        Manuscript restored = backupService.applyBackup(manuscript, emptyBackup, true, Map.of());

        assertThat(chatMessageService.hasAnyMessages(restored)).isFalse();
        assertThat(activeLeafResponse(restored)).isNull();
        assertThat(reload(restored).getName()).isEqualTo("Renamed");
    }

    @Test
    void messagesOnlyRestoreIntoEmptyManuscript() throws Exception {
        Manuscript empty = manuscript("Fresh", currentAi, null, null);

        Manuscript restored = reload(backupService.applyBackup(empty, backup, true, Map.of()));

        assertThat(tree(restored)).isEqualTo(storyTree());
        assertThat(activeLeafResponse(restored)).isEqualTo("chapter 2a");
        assertThat(restored.getName()).isEqualTo("Fresh");
    }

    @Test
    void backupWithDanglingReferencesStillRestores() throws Exception {
        JsonObject json = manuscriptService.createBackup(manuscript);
        JsonArray messages = json.getAsJsonArray("messages");
        // a message whose parent is missing becomes a root, an unknown active leaf is ignored
        messages.get(messages.size() - 1).getAsJsonObject().addProperty("parentUuid", "missing-parent");
        json.addProperty("activeLeafUuid", "missing-leaf");
        Manuscript target = manuscript("Target", null, null, null);
        ManuscriptBackup imported = backupService.importBackup(target, json.toString().getBytes(StandardCharsets.UTF_8));

        Manuscript restored = backupService.applyBackup(target, imported, true, Map.of());

        assertThat(chatMessageService.getAllMessages(restored)).hasSize(4);
        assertThat(tree(restored).values()).filteredOn(String::isEmpty).hasSize(2);
        assertThat(activeLeafResponse(restored)).isNull();
    }
}
