package com.github.enerccio.marginalia.revision;

import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.RevisionClearing;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.components.RevisionRow;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.EffectiveHistoryLimit.Origin;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * How many versions of a part the history keeps: the strictest of the book, its folders, the user and the permission, and what
 * the cap does to a part (the trash goes first, the first generated version and the labeled ones stay), and <i>Clear history</i>.
 */
class PartHistoryLimitTest extends MarginaliaTestBase {

    @Autowired
    private MessageRevisionService messageRevisionService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private FolderService folderService;

    @Autowired
    private SettingService settingService;

    private User user;
    private Manuscript book;
    private ChatMessage part;

    @BeforeEach
    void createBook() throws Exception {
        user = login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
        part = chatMessageService.createPart(book, null, "v1", null);
    }

    private void limitBook(Integer max) throws Exception {
        book = manuscriptService.find(book.getId());
        book.setHistoryMaxVersions(max);
        book = manuscriptService.save(book);
    }

    private Folder folder(String name, Folder parent, Integer max) throws Exception {
        Folder folder = folderService.create(name, parent);
        folder.setHistoryMaxVersions(max);
        return folderService.save(folder);
    }

    private void fileInto(Folder folder) throws Exception {
        manuscriptService.moveToFolder(List.of(book), folder);
        book = manuscriptService.find(book.getId());
    }

    private void limitUser(Integer max) throws Exception {
        UserSetting settings = settingService.getOrCreate(UserSetting.class);
        settings.setHistoryMaxVersions(max);
        settingService.save(settings);
    }

    /** What the administrator sets: the permission of the user. */
    private void permission(boolean granted, Integer max) throws Exception {
        loginAdmin();
        permissionService.setGranted(user, PermissionType.VERSION_HISTORY, granted);
        Permission permission = permissionService.getPermission(user, PermissionType.VERSION_HISTORY);
        permission.setHistoryMaxVersions(max);
        permissionService.save(permission);
        loginAs(user);
    }

    private EffectiveHistoryLimit limit() throws Exception {
        return manuscriptService.resolveHistoryLimit(manuscriptService.find(book.getId()));
    }

    /** Writes the part as many times as the number of texts. */
    private void edit(String... texts) throws Exception {
        for (String text : texts) {
            chatMessageService.updateResponse(book, part, text);
        }
    }

    private List<String> texts() throws Exception {
        List<String> texts = new ArrayList<>();
        for (RevisionRow row : messageRevisionService.list(part, true).reversed()) {
            texts.add(messageRevisionService.find(row.id()).getResponse());
        }
        return texts;
    }

    private MessageRevision versionOf(String text) throws Exception {
        for (RevisionRow row : messageRevisionService.list(part, true)) {
            MessageRevision revision = messageRevisionService.find(row.id());
            if (revision.getResponse().equals(text)) {
                return revision;
            }
        }
        throw new AssertionError("no version " + text);
    }

    // --- the limit ---

    @Test
    void aUserWithoutAPermissionHasTheDefault() throws Exception {
        EffectiveHistoryLimit limit = limit();

        assertThat(limit.value()).isEqualTo(50).isEqualTo(EffectiveHistoryLimit.DEFAULT_MAX_VERSIONS);
        assertThat(limit.origin()).isEqualTo(Origin.PERMISSION);
        assertThat(limit.isUnlimited()).isFalse();
        assertThat(limit.isOff()).isFalse();
    }

    @Test
    void theNearestLevelThatIsStrictestIsTheOrigin() throws Exception {
        limitBook(20);
        assertThat(limit()).extracting(EffectiveHistoryLimit::value, EffectiveHistoryLimit::origin).containsExactly(20, Origin.BOOK);

        limitUser(10);
        assertThat(limit()).extracting(EffectiveHistoryLimit::value, EffectiveHistoryLimit::origin).containsExactly(10, Origin.USER);

        Folder top = folder("Sci-fi", null, 8);
        Folder child = folder("Expanse", top, 12);
        fileInto(child);
        EffectiveHistoryLimit limit = limit();
        assertThat(limit.value()).as("a folder far up limits the book too").isEqualTo(8);
        assertThat(limit.origin()).isEqualTo(Origin.FOLDER);
        assertThat(limit.folder().getId()).isEqualTo(top.getId());

        permission(true, 5);
        assertThat(limit()).extracting(EffectiveHistoryLimit::value, EffectiveHistoryLimit::origin).containsExactly(5, Origin.PERMISSION);

        limitBook(2);
        assertThat(limit()).extracting(EffectiveHistoryLimit::value, EffectiveHistoryLimit::origin).containsExactly(2, Origin.BOOK);
    }

    @Test
    void aLooserLevelCannotRaiseWhatAStricterOneLimited() throws Exception {
        permission(true, 4);
        limitUser(30);
        limitBook(100);

        assertThat(limit()).extracting(EffectiveHistoryLimit::value, EffectiveHistoryLimit::origin).containsExactly(4, Origin.PERMISSION);
    }

    @Test
    void whenLevelsSetTheSameTheNearestIsTheOrigin() throws Exception {
        permission(true, 6);
        limitUser(6);
        Folder folder = folder("Same", null, 6);
        fileInto(folder);
        assertThat(limit().origin()).isEqualTo(Origin.FOLDER);

        limitBook(6);
        assertThat(limit().origin()).isEqualTo(Origin.BOOK);
    }

    @Test
    void aPermissionWithoutANumberIsNoLimitFromThatLevel() throws Exception {
        permission(true, null);
        EffectiveHistoryLimit none = limit();
        assertThat(none.isUnlimited()).isTrue();
        assertThat(none.value()).isNull();
        assertThat(none.origin()).isEqualTo(Origin.NONE);

        limitUser(7);
        assertThat(limit()).extracting(EffectiveHistoryLimit::value, EffectiveHistoryLimit::origin).containsExactly(7, Origin.USER);
    }

    @Test
    void aPermissionThatIsNotGrantedKeepsNoHistory() throws Exception {
        permission(false, 50);
        limitBook(10);

        EffectiveHistoryLimit limit = limit();
        assertThat(limit.value()).isZero();
        assertThat(limit.origin()).isEqualTo(Origin.PERMISSION);
        assertThat(limit.isOff()).isTrue();
    }

    @Test
    void aNegativeNumberDoesNotLimit() throws Exception {
        limitBook(-1);
        limitUser(-5);

        assertThat(limit().value()).isEqualTo(50);
    }

    @Test
    void theInheritedLimitIsWhatABookInTheFolderGets() throws Exception {
        assertThat(manuscriptService.resolveInheritedHistoryLimit(null).origin()).isEqualTo(Origin.PERMISSION);

        Folder top = folder("Top", null, 9);
        Folder child = folder("Child", top, null);

        EffectiveHistoryLimit limit = manuscriptService.resolveInheritedHistoryLimit(child);
        assertThat(limit.value()).isEqualTo(9);
        assertThat(limit.origin()).isEqualTo(Origin.FOLDER);
        assertThat(limit.folder().getId()).isEqualTo(top.getId());
    }

    @Test
    void theFolderOfABookTheWindowHoldsIsALazyProxyWithoutASession() throws Exception {
        Folder top = folder("Window top", null, 9);
        fileInto(top);

        // what the About tab does: the folder comes with the book that was loaded, the session is gone
        Folder held = manuscriptService.find(book.getId()).getFolder();
        EffectiveHistoryLimit limit = manuscriptService.resolveInheritedHistoryLimit(held);

        assertThat(limit.value()).isEqualTo(9);
        assertThat(limit.origin()).isEqualTo(Origin.FOLDER);
    }

    @Test
    void aCopiedFolderKeepsTheLimit() throws Exception {
        Folder source = folder("Original", null, 11);

        Folder target = folder("Target", null, null);

        folderService.transfer(new FolderService.Transfer(List.of(), List.of(source), target, true, false, Map.of()));

        Folder copy = folderService.findChild(target, "Original");
        assertThat(copy.getHistoryMaxVersions()).isEqualTo(11);
    }

    // --- the cap ---

    @Test
    void theOldestVersionsGoWhenThePartIsOverTheLimit() throws Exception {
        limitBook(3);

        edit("v2", "v3", "v4", "v5");

        assertThat(texts()).containsExactly("v3", "v4", "v5");
        assertThat(messageRevisionService.findLatest(part).getResponse()).isEqualTo("v5");
    }

    @Test
    void theTrashIsPrunedBeforeTheVersionsThatAreNot() throws Exception {
        limitBook(3);
        edit("v2", "v3");
        messageRevisionService.trash(versionOf("v2"));

        edit("v4");

        assertThat(texts()).as("the version in the trash went, though it was not the oldest").containsExactly("v1", "v3", "v4");
    }

    @Test
    void theTrashCountsTowardTheLimit() throws Exception {
        limitBook(3);
        edit("v2", "v3");
        messageRevisionService.trash(versionOf("v1"));
        messageRevisionService.trash(versionOf("v2"));

        edit("v4", "v5");

        assertThat(texts()).containsExactly("v3", "v4", "v5");
    }

    @Test
    void theNewestVersionIsNeverPruned() throws Exception {
        limitBook(1);

        edit("v2", "v3");

        assertThat(texts()).containsExactly("v3");
        assertThat(chatMessageService.find(part.getId()).getResponse()).isEqualTo("v3");
    }

    @Test
    void theFirstGeneratedVersionAndTheLabeledOnesStayAndAreNotCounted() throws Exception {
        limitBook(2);
        ChatMessage generated = chatMessageService.createPart(book, part, "g1", null,
                RevisionOrigin.model(RevisionSource.GENERATE, "model · protocol", false, "thinking"));
        part = generated;
        chatMessageService.updateResponse(book, part, "g2", RevisionOrigin.model(RevisionSource.REGENERATE, "model · protocol", false, null));
        MessageRevision snapshot = messageRevisionService.snapshot(book, part, "keep");

        edit("e1", "e2", "e3", "e4");

        assertThat(texts()).as("the generated one, the snapshot, and two others").containsExactly("g1", "g2", "e3", "e4");
        assertThat(messageRevisionService.find(snapshot.getId())).isNotNull();
        assertThat(versionOf("g1").getSource()).isEqualTo(RevisionSource.GENERATE);
    }

    @Test
    void aSecondGenerationIsPrunedLikeAnyOther() throws Exception {
        limitBook(1);
        part = chatMessageService.createPart(book, part, "g1", null, RevisionOrigin.model(RevisionSource.GENERATE, "m", false, null));
        chatMessageService.updateResponse(book, part, "g2", RevisionOrigin.model(RevisionSource.REGENERATE, "m", false, null));
        chatMessageService.updateResponse(book, part, "g3", RevisionOrigin.model(RevisionSource.REGENERATE, "m", false, null));

        assertThat(texts()).containsExactly("g1", "g3");
    }

    @Test
    void loweringTheLimitPrunesWhenTheNextVersionComes() throws Exception {
        edit("v2", "v3", "v4", "v5");
        limitBook(2);
        assertThat(texts()).as("nothing is pruned by changing the limit").hasSize(5);

        edit("v6");

        assertThat(texts()).containsExactly("v5", "v6");
    }

    @Test
    void raisingTheLimitChangesNothingUntilMoreVersionsCome() throws Exception {
        limitBook(2);
        edit("v2", "v3");
        assertThat(texts()).containsExactly("v2", "v3");

        limitBook(4);
        assertThat(texts()).containsExactly("v2", "v3");
        edit("v4", "v5", "v6");
        assertThat(texts()).containsExactly("v3", "v4", "v5", "v6");
    }

    @Test
    void theLimitOfAnotherLevelAppliesToo() throws Exception {
        limitUser(2);

        edit("v2", "v3");

        assertThat(texts()).containsExactly("v2", "v3");
    }

    @Test
    void noHistoryIsKeptWhenTheLimitIsZero() throws Exception {
        long before = texts().size();
        limitBook(0);

        edit("v2", "v3");

        assertThat(texts()).hasSize((int) before);
        assertThat(chatMessageService.find(part.getId()).getResponse()).as("the part is saved all the same").isEqualTo("v3");
        assertThatThrownBy(() -> messageRevisionService.snapshot(book, part, "x")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aPermissionThatIsNotGrantedKeepsNothingForTheUser() throws Exception {
        permission(false, null);

        ChatMessage other = chatMessageService.createPart(book, part, "typed", null);

        assertThat(messageRevisionService.list(other, true)).isEmpty();
    }

    @Test
    void aPartOfAnotherBookIsLimitedByItsOwnBook() throws Exception {
        limitBook(2);
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("other"));
        Manuscript other = manuscriptService.save(manuscript);
        ChatMessage otherPart = chatMessageService.createPart(other, null, "a", null);

        chatMessageService.updateResponse(other, otherPart, "b");
        chatMessageService.updateResponse(other, otherPart, "c");

        assertThat(messageRevisionService.list(otherPart, true)).hasSize(3);
    }

    // --- clear history ---

    @Test
    void clearingAPartKeepsTheCurrentVersionOnly() throws Exception {
        edit("v2", "v3", "v4");
        messageRevisionService.trash(versionOf("v2"));

        RevisionClearing preview = messageRevisionService.previewClear(part, false);
        RevisionClearing cleared = messageRevisionService.clear(part, false);

        assertThat(preview).isEqualTo(cleared);
        assertThat(cleared.versions()).isEqualTo(3);
        assertThat(cleared.bytes()).isPositive();
        assertThat(texts()).containsExactly("v4");
        assertThat(chatMessageService.find(part.getId()).getResponse()).isEqualTo("v4");
    }

    @Test
    void clearingCanKeepTheSnapshotsAndTheFirstGeneratedVersion() throws Exception {
        part = chatMessageService.createPart(book, part, "g1", null, RevisionOrigin.model(RevisionSource.GENERATE, "m", false, null));
        edit("e1");
        messageRevisionService.snapshot(book, part, "keep");
        edit("e2", "e3");

        RevisionClearing preview = messageRevisionService.previewClear(part, true);
        RevisionClearing cleared = messageRevisionService.clear(part, true);

        assertThat(cleared).isEqualTo(preview);
        assertThat(cleared.versions()).isEqualTo(2);
        assertThat(texts()).containsExactly("g1", "e1", "e3");
    }

    @Test
    void clearingWithoutKeepingTakesTheSnapshotsToo() throws Exception {
        part = chatMessageService.createPart(book, part, "g1", null, RevisionOrigin.model(RevisionSource.GENERATE, "m", false, null));
        messageRevisionService.snapshot(book, part, "keep");
        edit("e1");

        messageRevisionService.clear(part, false);

        assertThat(texts()).containsExactly("e1");
    }

    @Test
    void clearingAPartThatHasOnlyTheCurrentVersionDoesNothing() throws Exception {
        assertThat(messageRevisionService.clear(part, false)).isEqualTo(RevisionClearing.NOTHING);
        assertThat(texts()).containsExactly("v1");
    }

    @Test
    void clearingABookClearsAllItsParts() throws Exception {
        edit("v2", "v3");
        ChatMessage second = chatMessageService.createPart(book, part, "w1", null);
        chatMessageService.updateResponse(book, second, "w2");
        ChatMessage deleted = chatMessageService.createPart(book, second, "d1", null);
        chatMessageService.updateResponse(book, deleted, "d2");
        chatMessageService.delete(chatMessageService.find(deleted.getId()), false);

        RevisionClearing preview = messageRevisionService.previewClear(book, false);
        RevisionClearing cleared = messageRevisionService.clear(book, false);

        assertThat(cleared).isEqualTo(preview);
        assertThat(cleared.versions()).isEqualTo(3);
        assertThat(texts()).containsExactly("v3");
        assertThat(messageRevisionService.list(second, true)).hasSize(1);
        assertThat(messageRevisionService.list(deleted, true)).as("a deleted part is not touched").hasSize(2);
    }

    @Test
    void nobodyClearsTheHistoryOfAnotherUser() throws Exception {
        edit("v2");
        loginAs(createUser());

        assertThat(messageRevisionService.clear(part, false)).isEqualTo(RevisionClearing.NOTHING);
        assertThat(messageRevisionService.previewClear(part, false)).isEqualTo(RevisionClearing.NOTHING);
        assertThatThrownBy(() -> messageRevisionService.clear(book, false)).isInstanceOf(IllegalArgumentException.class);

        loginAs(user);
        assertThat(texts()).containsExactly("v1", "v2");
    }
}
