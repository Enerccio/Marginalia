package com.github.enerccio.marginalia.backup;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.domain.service.BackupService.ManuscriptBackup;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables.Scope;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Copying a manuscript through its backup ({@code cloneBackup} / {@code restoreAsNewManuscript}) and the backup file
 * lifecycle.
 */
class ManuscriptBackupCopyTest extends BackupTestBase {

    private AI ai;
    private Protocol protocol;
    private Lorebook lorebook;
    private Manuscript original;

    @BeforeEach
    void createOriginal() throws Exception {
        login();
        ai = ai(uniqueName("ai"));
        protocol = protocol(uniqueName("protocol"));
        lorebook = lorebook(uniqueName("lore"));
        original = manuscript("Original", ai, protocol, lorebook);
        tagRelationService.createRelation(tag("fantasy"), original);
        tagRelationService.createRelation(tag("dark"), original);
        original = story(original);
    }

    private Manuscript copy(String newName) throws Exception {
        return backupService.cloneBackup(backupService.takeBackup(original), newName, Map.of());
    }

    @Test
    void copyHasManuscriptContent() throws Exception {
        Manuscript copy = reload(copy(null));

        assertThat(copy.getName()).isEqualTo("Original");
        assertThat(copy.getDescription()).isEqualTo("description of Original");
        assertThat(copy.getPov()).isEqualTo("first person");
        assertThat(copy.getTense()).isEqualTo("present");
        assertThat(copy.getStyle()).isEqualTo("terse");
        assertThat(copy.getTemplate()).isEqualTo("{{backgroundLore}}");
        assertThat(copy.getUserPrompt()).isEqualTo("{{instructions}}");
        assertThat(copy.getSummaryPrompt()).isEqualTo("summarize");
        assertThat(copy.getShowBookStyles()).isTrue();
        assertThat(copy.getBackupStrategy()).isEqualTo(BackupStrategy.AFTER_N_MESSAGES);
        assertThat(copy.getBackupStrategyValue()).isEqualTo("5");
        assertThat(copy.getAttributes().get("plugin.flag").getAsString()).isEqualTo("on");
        assertThat(idOf(copy.getAi())).isEqualTo(ai.getId());
        assertThat(idOf(copy.getProtocol())).isEqualTo(protocol.getId());
        assertThat(idOf(copy.getLorebook())).isEqualTo(lorebook.getId());
        assertThat(tagValues(copy)).containsExactly("dark", "fantasy");
        assertThat(copy.getOwner().getId()).isEqualTo(currentUser.getId());
        assertThat(copy.isPublished()).isFalse();
    }

    @Test
    void copyCanBeRenamed() throws Exception {
        assertThat(reload(copy("  Renamed copy  ")).getName()).isEqualTo("Renamed copy");
        assertThat(reload(copy("   ")).getName()).isEqualTo("Original");
    }

    @Test
    void copyHasTheStoryTree() throws Exception {
        Manuscript copy = copy(null);

        assertThat(tree(copy)).isEqualTo(storyTree());
        assertThat(activeLeafResponse(copy)).isEqualTo("chapter 2a");
        assertThat(chatMessageService.getTotalWordCount(copy)).isEqualTo(chatMessageService.getTotalWordCount(original));
    }

    @Test
    void copiedMessagesKeepContent() throws Exception {
        Manuscript copy = copy(null);

        ChatMessage one = messageByResponse(copy, "chapter 1");
        assertThat(one.getInstructions()).isEqualTo("instructions for chapter 1");
        assertThat(one.getRequest()).isEqualTo(REQUEST);
        assertThat(one.getWordCount()).isEqualTo("chapter 1".length());
        assertThat(one.getTokenCount()).isEqualTo("chapter 1".length() * 2L);
        assertThat(one.isEdited()).isTrue();
        assertThat(one.getAttributes().get("chapter").getAsString()).isEqualTo("chapter 1");
        assertThat(messageByResponse(copy, "chapter 2b").isEdited()).isFalse();

        ChatMessage twoA = messageByResponse(copy, "chapter 2a");
        assertThat(twoA.getSummary()).isNotNull();
        assertThat(summaryService.find(twoA.getSummary().getId()).getSummary()).isEqualTo("summary up to 2a");
    }

    @Test
    void copyIsIndependentOfOriginal() throws Exception {
        Manuscript copy = copy(null);

        assertThat(copy.getId()).isNotEqualTo(original.getId());
        assertThat(copy.getUuid()).isNotEqualTo(original.getUuid());
        List<ChatMessage> originalMessages = chatMessageService.getAllMessages(original);
        List<ChatMessage> copiedMessages = chatMessageService.getAllMessages(copy);
        assertThat(copiedMessages).extracting(ChatMessage::getId).doesNotContainAnyElementsOf(
                originalMessages.stream().map(ChatMessage::getId).toList());
        assertThat(copiedMessages).extracting(ChatMessage::getUuid).doesNotContainAnyElementsOf(
                originalMessages.stream().map(ChatMessage::getUuid).toList());
        assertThat(idOf(messageByResponse(copy, "chapter 2a").getSummary()))
                .isNotEqualTo(idOf(messageByResponse(original, "chapter 2a").getSummary()));

        // changing the copy leaves the original alone
        ChatMessage copiedLeaf = messageByResponse(copy, "chapter 2a");
        chatMessageService.deleteNodeAndMigrateChildren(copiedLeaf, reload(copy), true);
        Manuscript renamed = reload(copy);
        renamed.setName("changed");
        manuscriptService.save(renamed);

        assertThat(tree(original)).isEqualTo(storyTree());
        assertThat(reload(original).getName()).isEqualTo("Original");
        assertThat(summaryService.find(messageByResponse(original, "chapter 2a").getSummary().getId()).getSummary())
                .isEqualTo("summary up to 2a");
    }

    @Test
    void templateVariablesTravelWithMessagesAndManuscript() throws Exception {
        ChatMessage leaf = chatMessageService.find(messageByResponse(original, "chapter 2a").getId());
        TemplateVariables variables = new TemplateVariables();
        variables.set(Scope.LOCAL, "hp", "17");
        variables.storeTo(Scope.LOCAL, leaf.getAttributes());
        chatMessageService.save(leaf);
        Manuscript m = reload(original);
        TemplateVariables globals = new TemplateVariables();
        globals.set(Scope.GLOBAL, "world", "Testland");
        globals.storeTo(Scope.GLOBAL, m.getAttributes());
        manuscriptService.save(m);

        Manuscript copy = copy(null);

        TemplateVariables loaded = new TemplateVariables();
        loaded.loadFrom(Scope.LOCAL, messageByResponse(copy, "chapter 2a").getAttributes());
        loaded.loadFrom(Scope.GLOBAL, reload(copy).getAttributes());
        assertThat(loaded.get(Scope.LOCAL, "hp")).isEqualTo("17");
        assertThat(loaded.get(Scope.GLOBAL, "world")).isEqualTo("Testland");
    }

    @Test
    void copyOfEmptyManuscript() throws Exception {
        Manuscript empty = new Manuscript();
        empty.setName("Empty");
        empty = manuscriptService.save(empty);

        Manuscript copy = backupService.cloneBackup(backupService.takeBackup(empty), null, Map.of());

        assertThat(reload(copy).getName()).isEqualTo("Empty");
        assertThat(reload(copy).getAi()).isNull();
        assertThat(reload(copy).getLorebook()).isNull();
        assertThat(chatMessageService.hasAnyMessages(copy)).isFalse();
        assertThat(activeLeafResponse(copy)).isNull();
    }

    @Test
    void tagsThatNoLongerExistAreRecreated() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(original);
        Tag dark = tag("dark");
        tagService.delete(dark, true);

        Manuscript copy = backupService.cloneBackup(backup, null, Map.of());

        assertThat(tagValues(copy)).containsExactly("dark", "fantasy");
        Tag recreated = tagRelationService.getTagsForObject(copy).stream().filter(t -> t.getValue().equals("dark")).findFirst().orElseThrow();
        assertThat(recreated.getId()).isNotEqualTo(dark.getId());
        assertThat(recreated.getOwner().getId()).isEqualTo(currentUser.getId());
        // the recreated tag is reused by the next copy
        Manuscript second = backupService.cloneBackup(backup, null, Map.of());
        assertThat(tagRelationService.getTagsForObject(second)).extracting(Tag::getId).contains(recreated.getId());
        assertThat(tagService.searchTagsForUser("dark", 0, 10)).hasSize(1);
    }

    @Test
    void existingTagsAreReusedIgnoringCase() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(original);
        Tag fantasy = tag("fantasy");
        tagService.delete(tag("dark"), true);
        Tag upperDark = tag("DARK");

        Manuscript copy = backupService.cloneBackup(backup, null, Map.of());

        assertThat(tagRelationService.getTagsForObject(copy)).extracting(Tag::getId)
                .containsExactlyInAnyOrder(fantasy.getId(), upperDark.getId());
    }

    @Test
    void otherUserGetsOwnCopiesOfTags() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(original);
        Tag ownersFantasy = tag("fantasy");
        login();

        Manuscript copy = backupService.cloneBackup(backup, null, Map.of());

        List<Tag> tags = tagRelationService.getTagsForObject(copy);
        assertThat(tags).extracting(Tag::getValue).containsExactlyInAnyOrder("dark", "fantasy");
        assertThat(tags).allMatch(t -> t.getOwner().getId().equals(currentUser.getId()));
        assertThat(tags).extracting(Tag::getId).doesNotContain(ownersFantasy.getId());
    }

    @Test
    void publishedFlagIsCopied() throws Exception {
        Manuscript published = reload(original);
        published.setPublished(true);
        manuscriptService.save(published);

        assertThat(manuscriptService.createBackup(original).get("published").getAsBoolean()).isTrue();
        assertThat(reload(copy(null)).isPublished()).isTrue();
    }

    @Test
    void oldBackupWithoutPublishedFlagCopiesUnpublished() throws Exception {
        Manuscript published = reload(original);
        published.setPublished(true);
        manuscriptService.save(published);
        JsonObject json = manuscriptService.createBackup(original);
        json.remove("published");

        Manuscript copy = backupService.restoreAsNewManuscript(json.toString().getBytes(StandardCharsets.UTF_8), null, Map.of());

        assertThat(reload(copy).isPublished()).isFalse();
    }

    // ------------------------------------------------------------------------------------------------------------
    // backup files
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void backupIsStoredAndListedNewestFirst() throws Exception {
        ManuscriptBackup first = backupService.takeBackup(original);
        Thread.sleep(5);
        ManuscriptBackup second = backupService.takeBackup(original);

        assertThat(new File(first.getFile())).exists();
        assertThat(first.getManuscriptName()).isEqualTo("Original");
        assertThat(first.getOwnerName()).isEqualTo(currentUser.getLogin());
        assertThat(first.getTotalMessagesCount()).isEqualTo(4);

        List<ManuscriptBackup> listed = backupService.getBackups(original);
        assertThat(listed).extracting(ManuscriptBackup::getFile).containsExactly(second.getFile(), first.getFile());
        // listing reads metadata only
        assertThat(listed.getFirst().isLoaded()).isFalse();
        assertThat(listed.getFirst().getBackup()).isNull();
        assertThat(listed.getFirst().getTotalMessagesCount()).isEqualTo(4);

        ManuscriptBackup loaded = backupService.loadBackup(listed.getLast());
        assertThat(loaded.getBackup().get("name").getAsString()).isEqualTo("Original");
        assertThat(loaded.getBackup().getAsJsonArray("messages")).hasSize(4);
    }

    @Test
    void listedBackupCanBeCopied() throws Exception {
        backupService.takeBackup(original);
        ManuscriptBackup listed = backupService.getBackups(original).getFirst();

        Manuscript copy = backupService.cloneBackup(listed, "From disk", Map.of());

        assertThat(tree(copy)).isEqualTo(storyTree());
    }

    @Test
    void deleteBackupRemovesFile() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(original);

        backupService.deleteBackup(backup);

        assertThat(new File(backup.getFile())).doesNotExist();
        assertThat(backupService.getBackups(original)).extracting(ManuscriptBackup::getFile).doesNotContain(backup.getFile());
    }

    @Test
    void exportedBackupCanBeImportedAndRestoredAsNewManuscript() throws Exception {
        String exported = backupService.serializeBackup(backupService.takeBackup(original));
        Manuscript target = manuscript("Target", null, null, null);

        ManuscriptBackup imported = backupService.importBackup(target, exported.getBytes(StandardCharsets.UTF_8));
        Manuscript restored = backupService.restoreAsNewManuscript(exported.getBytes(StandardCharsets.UTF_8), "Imported", Map.of());

        assertThat(imported.getManuscriptName()).isEqualTo("Original");
        assertThat(backupService.getBackups(target)).hasSize(1);
        assertThat(reload(restored).getName()).isEqualTo("Imported");
        assertThat(tree(restored)).isEqualTo(storyTree());
    }

    @Test
    void rawManuscriptJsonCanBeImported() throws Exception {
        JsonObject raw = manuscriptService.createBackup(original);
        Manuscript target = manuscript("Target", null, null, null);

        ManuscriptBackup imported = backupService.importBackup(target, raw.toString().getBytes(StandardCharsets.UTF_8));

        assertThat(imported.getManuscriptName()).isEqualTo("Original");
        assertThat(imported.getTotalMessagesCount()).isEqualTo(4);
        assertThat(tree(backupService.cloneBackup(backupService.getBackups(target).getFirst(), null, Map.of())))
                .isEqualTo(storyTree());
    }

    @Test
    void invalidImportsAreRejected() throws Exception {
        assertThatThrownBy(() -> backupService.importBackup(original, new byte[0])).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> backupService.importBackup(original, "not json {".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> backupService.importBackup(original, "[1, 2]".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> backupService.importBackup(original, "{\"foo\": 1}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(backupService.getBackups(original)).isEmpty();
    }

    @Test
    void backupsAreKeptPerManuscript() throws Exception {
        Manuscript other = manuscript("Other", null, null, null);
        backupService.takeBackup(original);

        assertThat(backupService.getBackups(other)).isEmpty();
        assertThat(backupService.getBackups(original)).hasSize(1);
    }
}
