package com.github.enerccio.marginalia.backup;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.BackupService.ManuscriptBackup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * How a backup's AI and protocol references are resolved on copy and on full restore: same uuid, missing but with
 * the same name, missing entirely, deleted, and owned by another user. Messages-only restore never touches them.
 */
class BackupAiProtocolLinkTest extends BackupTestBase {

    private String aiName;
    private String protocolName;
    private AI ai;
    private Protocol protocol;
    private Manuscript original;
    private ManuscriptBackup backup;

    @BeforeEach
    void createOriginal() throws Exception {
        login();
        aiName = uniqueName("Local Model");
        protocolName = uniqueName("Creative");
        ai = ai(aiName);
        protocol = protocol(protocolName);
        original = story(manuscript("Book", ai, protocol, null));
        backup = backupService.takeBackup(original);
    }

    private Manuscript copy() throws Exception {
        return reload(backupService.cloneBackup(backup, null, Map.of()));
    }

    /**
     * Full restore into a manuscript currently using {@code currentAi}/{@code currentProtocol}.
     */
    private Manuscript restoreInto(AI currentAi, Protocol currentProtocol) throws Exception {
        Manuscript target = manuscript("Target", currentAi, currentProtocol, null);
        return reload(backupService.applyBackup(target, backup, false, Map.of()));
    }

    private void delete(AI entity) throws Exception {
        aiService.delete(aiService.find(entity.getId()), false);
    }

    private void delete(Protocol entity) throws Exception {
        protocolService.delete(protocolService.find(entity.getId()), false);
    }

    // ---- same uuid -----------------------------------------------------------------------------------------------

    @Test
    void sameUuidIsLinkedOnCopy() throws Exception {
        Manuscript copy = copy();

        assertThat(idOf(copy.getAi())).isEqualTo(ai.getId());
        assertThat(idOf(copy.getProtocol())).isEqualTo(protocol.getId());
    }

    @Test
    void sameUuidIsLinkedOnRestore() throws Exception {
        Manuscript restored = restoreInto(ai(uniqueName("other")), protocol(uniqueName("other")));

        assertThat(idOf(restored.getAi())).isEqualTo(ai.getId());
        assertThat(idOf(restored.getProtocol())).isEqualTo(protocol.getId());
    }

    @Test
    void sameUuidWinsOverSameName() throws Exception {
        AI sameName = ai(aiName);
        Protocol sameNameProtocol = protocol(protocolName);

        Manuscript copy = copy();

        assertThat(idOf(copy.getAi())).isEqualTo(ai.getId()).isNotEqualTo(sameName.getId());
        assertThat(idOf(copy.getProtocol())).isEqualTo(protocol.getId()).isNotEqualTo(sameNameProtocol.getId());
    }

    @Test
    void renamedOriginalIsStillLinkedByUuid() throws Exception {
        AI renamed = aiService.find(ai.getId());
        renamed.setName("Renamed model");
        aiService.save(renamed);

        assertThat(idOf(copy().getAi())).isEqualTo(ai.getId());
    }

    // ---- missing, same name exists -------------------------------------------------------------------------------

    @Test
    void missingAiAndProtocolAreLinkedByNameOnCopy() throws Exception {
        delete(ai);
        delete(protocol);
        AI replacement = ai(aiName);
        Protocol replacementProtocol = protocol(protocolName);

        Manuscript copy = copy();

        assertThat(idOf(copy.getAi())).isEqualTo(replacement.getId());
        assertThat(idOf(copy.getProtocol())).isEqualTo(replacementProtocol.getId());
    }

    @Test
    void missingAiAndProtocolAreLinkedByNameOnRestore() throws Exception {
        delete(ai);
        delete(protocol);
        AI replacement = ai(aiName);
        Protocol replacementProtocol = protocol(protocolName);

        Manuscript restored = restoreInto(ai(uniqueName("current")), protocol(uniqueName("current")));

        assertThat(idOf(restored.getAi())).isEqualTo(replacement.getId());
        assertThat(idOf(restored.getProtocol())).isEqualTo(replacementProtocol.getId());
    }

    @Test
    void nameMatchIgnoresCaseAndSurroundingWhitespace() throws Exception {
        delete(ai);
        delete(protocol);
        AI replacement = ai("  " + aiName.toUpperCase() + " ");
        Protocol replacementProtocol = protocol(protocolName.toLowerCase());

        Manuscript copy = copy();

        assertThat(idOf(copy.getAi())).isEqualTo(replacement.getId());
        assertThat(idOf(copy.getProtocol())).isEqualTo(replacementProtocol.getId());
    }

    @Test
    void severalSameNamedPickTheOldest() throws Exception {
        delete(ai);
        AI older = ai(aiName);
        ai(aiName);

        assertThat(idOf(copy().getAi())).isEqualTo(older.getId());
    }

    @Test
    void deletedEntityWithSameNameIsNotLinked() throws Exception {
        delete(ai);
        AI replacement = ai(aiName);
        delete(replacement);

        assertThat(copy().getAi()).isNull();
    }

    // ---- missing, nothing matches --------------------------------------------------------------------------------

    @Test
    void missingWithoutNameMatchLeavesCopyUnlinked() throws Exception {
        delete(ai);
        delete(protocol);
        ai(uniqueName("unrelated"));

        Manuscript copy = copy();

        assertThat(copy.getAi()).isNull();
        assertThat(copy.getProtocol()).isNull();
        assertThat(tree(copy)).isEqualTo(storyTree());
    }

    @Test
    void missingWithoutNameMatchKeepsCurrentOnRestore() throws Exception {
        delete(ai);
        delete(protocol);
        AI current = ai(uniqueName("current"));
        Protocol currentProtocol = protocol(uniqueName("current"));

        Manuscript restored = restoreInto(current, currentProtocol);

        assertThat(idOf(restored.getAi())).isEqualTo(current.getId());
        assertThat(idOf(restored.getProtocol())).isEqualTo(currentProtocol.getId());
    }

    @Test
    void backupWithoutAiOrProtocol() throws Exception {
        backup = backupService.takeBackup(manuscript("Bare", null, null, null));
        AI current = ai(uniqueName("current"));

        assertThat(copy().getAi()).isNull();
        assertThat(idOf(restoreInto(current, null).getAi())).isEqualTo(current.getId());
    }

    // ---- other users ---------------------------------------------------------------------------------------------

    @Test
    void otherUsersEntitiesAreNeverLinked() throws Exception {
        login();
        // the other user owns nothing named like the backup's AI - the original owner's AI must not be used
        Manuscript copy = copy();

        assertThat(copy.getAi()).isNull();
        assertThat(copy.getProtocol()).isNull();
        assertThat(copy.getOwner().getId()).isEqualTo(currentUser.getId());
    }

    @Test
    void otherUserLinksOwnSameNamedEntities() throws Exception {
        User other = login();
        AI ownAi = ai(aiName);
        Protocol ownProtocol = protocol(protocolName);

        Manuscript copy = copy();

        assertThat(copy.getOwner().getId()).isEqualTo(other.getId());
        assertThat(idOf(copy.getAi())).isEqualTo(ownAi.getId());
        assertThat(idOf(copy.getProtocol())).isEqualTo(ownProtocol.getId());
    }

    // ---- messages only -------------------------------------------------------------------------------------------

    @Test
    void messagesOnlyRestoreKeepsCurrentAiAndProtocol() throws Exception {
        AI current = ai(uniqueName("current"));
        Protocol currentProtocol = protocol(uniqueName("current"));
        Manuscript target = manuscript("Target", current, currentProtocol, null);

        Manuscript restored = reload(backupService.applyBackup(target, backup, true, Map.of()));

        assertThat(idOf(restored.getAi())).isEqualTo(current.getId());
        assertThat(idOf(restored.getProtocol())).isEqualTo(currentProtocol.getId());
        assertThat(tree(restored)).isEqualTo(storyTree());
    }

    @Test
    void messagesOnlyRestoreKeepsNoAi() throws Exception {
        Manuscript target = manuscript("Target", null, null, null);

        Manuscript restored = reload(backupService.applyBackup(target, backup, true, Map.of()));

        assertThat(restored.getAi()).isNull();
        assertThat(restored.getProtocol()).isNull();
    }
}
