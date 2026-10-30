package com.github.enerccio.marginalia.revision;

import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.components.RevisionRow;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.BackupService;
import com.github.enerccio.marginalia.domain.service.BackupService.ManuscriptBackup;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.MessageRevisionService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a backup does with the history of the parts: it has it when asked to, a restore (as the book or as a copy) brings it back,
 * a backup without it gives each part one first version, and a duplicate of the book starts a history of its own.
 */
class PartHistoryBackupTest extends MarginaliaTestBase {

    @Autowired
    private BackupService backupService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private MessageRevisionService messageRevisionService;

    @Autowired
    private SettingService settingService;

    private Manuscript book;
    private ChatMessage first, second;

    @BeforeEach
    void createBook() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
        first = chatMessageService.createPart(book, null, "One.", null);
        Thread.sleep(3);
        second = chatMessageService.createPart(book, first, "Two.", null, new RevisionOrigin(RevisionSource.GENERATE, "model · protocol", false, "thinking"));
        chatMessageService.updateResponse(book, first, "One, edited.");
        messageRevisionService.label(versionOf(first, "One, edited."), "keep this");
        chatMessageService.updateResponse(book, first, "One, edited twice.");
        // the first is put back on top, the third version goes to the trash
        messageRevisionService.restoreOnTop(book, chatMessageService.find(first.getId()), versionOf(first, "One."));
        messageRevisionService.trash(versionOf(first, "One, edited twice."));
    }

    private MessageRevision versionOf(ChatMessage part, String text) throws Exception {
        for (RevisionRow row : messageRevisionService.list(part, true)) {
            MessageRevision revision = messageRevisionService.find(row.id());
            if (text.equals(revision.getResponse())) {
                return revision;
            }
        }
        throw new AssertionError("no version " + text);
    }

    private List<MessageRevision> history(ChatMessage part) throws Exception {
        List<MessageRevision> versions = new ArrayList<>();
        for (RevisionRow row : messageRevisionService.list(part, true).reversed()) {
            versions.add(messageRevisionService.find(row.id()));
        }
        return versions;
    }

    private ChatMessage partOf(Manuscript copy, String text) throws Exception {
        return chatMessageService.getAllMessages(copy).stream().filter(m -> m.getResponse().equals(text)).findFirst().orElseThrow();
    }

    private List<String> texts(ChatMessage part) throws Exception {
        return history(part).stream().map(MessageRevision::getResponse).toList();
    }

    private Manuscript restoreAsCopy(ManuscriptBackup backup) throws Exception {
        return backupService.cloneBackup(backup, "Copy", Map.of());
    }

    @Test
    void aBackupHasNoHistoryUnlessAskedTo() throws Exception {
        ManuscriptBackup plain = backupService.takeBackup(book);
        ManuscriptBackup full = backupService.takeBackup(book, true);

        assertThat(plain.getBackup().toString()).doesNotContain("\"revisions\"");
        assertThat(full.getBackup().toString()).contains("\"revisions\"");
    }

    @Test
    void aCopyOfABackupWithHistoryHasTheHistory() throws Exception {
        Manuscript copy = restoreAsCopy(backupService.takeBackup(book, true));

        ChatMessage one = partOf(copy, "One.");
        assertThat(texts(one)).containsExactlyElementsOf(texts(chatMessageService.find(first.getId())));
        assertThat(texts(one)).containsExactly("One.", "One, edited.", "One, edited twice.", "One.");
        List<MessageRevision> versions = history(one);
        List<MessageRevision> originals = history(chatMessageService.find(first.getId()));
        for (int i = 0; i < versions.size(); i++) {
            assertThat(versions.get(i).getUuid()).as("a version of its own").isNotEqualTo(originals.get(i).getUuid());
            assertThat(versions.get(i).getSource()).isEqualTo(originals.get(i).getSource());
            assertThat(versions.get(i).getLabel()).isEqualTo(originals.get(i).getLabel());
            assertThat(versions.get(i).isDeleted()).isEqualTo(originals.get(i).isDeleted());
            assertThat(versions.get(i).getCreation()).isEqualTo(originals.get(i).getCreation());
            assertThat(versions.get(i).getWordCount()).isEqualTo(originals.get(i).getWordCount());
        }
        assertThat(versions.get(2).isDeleted()).as("the trash comes back").isTrue();
        assertThat(versions.get(1).getLabel()).isEqualTo("keep this");
        assertThat(versions.get(3).getSource()).isEqualTo(RevisionSource.RESTORE);
        assertThat(versions.get(3).getRestoredFrom()).as("points to the copy of the version it was made of").isEqualTo(versions.get(0).getUuid());
    }

    @Test
    void theReasoningOfAGeneratedVersionComesBack() throws Exception {
        Manuscript copy = restoreAsCopy(backupService.takeBackup(book, true));

        MessageRevision generated = history(partOf(copy, "Two.")).getFirst();

        assertThat(generated.getSource()).isEqualTo(RevisionSource.GENERATE);
        assertThat(generated.getSourceDetail()).isEqualTo("model · protocol");
        assertThat(generated.getResponseReasoning()).isEqualTo("thinking");
    }

    @Test
    void aBackupWithoutHistoryGivesEachPartAFirstVersion() throws Exception {
        Manuscript copy = restoreAsCopy(backupService.takeBackup(book));

        ChatMessage one = partOf(copy, "One.");
        List<MessageRevision> versions = history(one);

        assertThat(versions).hasSize(1);
        assertThat(versions.getFirst().getResponse()).isEqualTo("One.");
        assertThat(versions.getFirst().getSource()).isEqualTo(RevisionSource.HUMAN);
        assertThat(history(partOf(copy, "Two."))).hasSize(1);
    }

    @Test
    void aBackupWithoutHistoryOfAGeneratedPartStartsWithTheGeneratedVersion() throws Exception {
        ChatMessage generated = chatMessageService.find(second.getId());
        generated.setModelUsed("gpt");
        generated.setProtocolUsed("chat");
        chatMessageService.save(generated);

        Manuscript copy = restoreAsCopy(backupService.takeBackup(book));

        MessageRevision version = history(partOf(copy, "Two.")).getFirst();
        assertThat(version.getSource()).isEqualTo(RevisionSource.GENERATE);
        assertThat(version.getSourceDetail()).isEqualTo("gpt · chat");
    }

    @Test
    void restoringTheBookReplacesItsHistoryWithTheOneOfTheBackup() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(book, true);
        chatMessageService.updateResponse(book, chatMessageService.find(first.getId()), "A later edit");

        Manuscript restored = backupService.applyBackup(manuscriptService.find(book.getId()), backup, false, Map.of());

        ChatMessage one = partOf(restored, "One.");
        assertThat(texts(one)).containsExactly("One.", "One, edited.", "One, edited twice.", "One.");
        assertThat(chatMessageService.getAllMessages(restored)).hasSize(2);
    }

    @Test
    void theHistoryIsPrunedToTheLimitOfTheBookItComesTo() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(book, true);
        UserSetting settings = settingService.getOrCreate(UserSetting.class);
        settings.setHistoryMaxVersions(2);
        settingService.save(settings);

        Manuscript copy = restoreAsCopy(backup);

        ChatMessage one = partOf(copy, "One.");
        // the labeled version stays, it does not count; the newest is the text of the part
        assertThat(texts(one)).hasSizeLessThanOrEqualTo(3);
        assertThat(history(one).getLast().getResponse()).as("the newest version is the text of the part").isEqualTo("One.");
        assertThat(history(one).stream().filter(v -> v.getLabel() != null)).hasSize(1);
    }

    @Test
    void aBookThatKeepsNoHistoryGetsNone() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(book, true);
        UserSetting settings = settingService.getOrCreate(UserSetting.class);
        settings.setHistoryMaxVersions(0);
        settingService.save(settings);

        Manuscript copy = restoreAsCopy(backup);

        assertThat(messageRevisionService.list(partOf(copy, "One."), true)).isEmpty();
    }

    @Test
    void aDuplicateOfTheBookStartsAHistoryOfItsOwn() throws Exception {
        Manuscript duplicate = backupService.cloneManuscript(manuscriptService.find(book.getId()), uniqueName("duplicate"), false);

        for (String text : List.of("One.", "Two.")) {
            ChatMessage part = partOf(duplicate, text);
            List<MessageRevision> versions = history(part);
            assertThat(versions).hasSize(1);
            assertThat(versions.getFirst().getSource()).isEqualTo(RevisionSource.DUPLICATE);
            assertThat(versions.getFirst().getResponse()).isEqualTo(text);
        }
        assertThat(texts(chatMessageService.find(first.getId()))).as("the original keeps its own").hasSize(4);
    }

    @Test
    void theWorkingCopyOfAnAgentGetsNoVersionsUntilItIsChanged() throws Exception {
        JsonObject base = manuscriptService.createBackup(manuscriptService.find(book.getId()));

        Manuscript copy = manuscriptService.cloneFromBackup(base);

        assertThat(messageRevisionService.list(partOf(copy, "One."), true)).isEmpty();
    }
}
