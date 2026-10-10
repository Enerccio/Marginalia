package com.github.enerccio.marginalia.cleanup;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.CleanupService.EntityKey;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.TrashService.RestoreResult;
import com.github.enerccio.marginalia.domain.service.TrashService.TrashFilter;
import com.github.enerccio.marginalia.domain.service.TrashService.TrashItem;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Listing and restoring soft deleted entities ({@link TrashService}): users see only their own, administrators all,
 * and an object is not restored while an object it depends on is deleted (unless restored with it).
 */
class TrashServiceTest extends MarginaliaTestBase {

    @Autowired
    private TrashService trashService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ProtocolService protocolService;

    private Lorebook lorebook(String name) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(name);
        lorebook.setSubbooks(new ArrayList<>());
        return lorebookService.save(lorebook);
    }

    private LorebookEntry entry(Lorebook lorebook, String name) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        return lorebookEntryService.save(entry);
    }

    private static EntityKey key(Class<?> rootType, BaseEntity entity) {
        return new EntityKey(rootType, entity.getId());
    }

    private void delete(Lorebook lorebook) throws Exception {
        lorebookService.delete(lorebookService.find(lorebook.getId()), false);
    }

    private void delete(LorebookEntry entry) throws Exception {
        lorebookEntryService.delete(lorebookEntryService.find(entry.getId()), false);
    }

    private List<EntityKey> trash(TrashFilter filter) throws Exception {
        return trashService.find(filter, 0, 1000).stream().map(TrashItem::key).toList();
    }

    @Test
    void userSeesAndRestoresOnlyOwnObjects() throws Exception {
        User other = login();
        Lorebook foreign = lorebook(uniqueName("foreign"));
        delete(foreign);

        User user = login();
        Lorebook mine = lorebook(uniqueName("mine"));
        delete(mine);
        assertThat(lorebookService.findAllForUser()).isEmpty();

        // the filter by owner is for administrators, a user can't look at somebody else's trash with it
        assertThat(trash(TrashFilter.all())).containsExactly(key(Lorebook.class, mine));
        assertThat(trash(new TrashFilter(other.getId(), null))).containsExactly(key(Lorebook.class, mine));
        assertThat(trashService.count(TrashFilter.all())).isEqualTo(1);
        assertThat(trashService.find(TrashFilter.all(), 0, 10).getFirst().label()).isEqualTo(mine.getName());

        assertThatThrownBy(() -> trashService.restore(List.of(key(Lorebook.class, foreign))))
                .isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> trashService.getExtendedContent(key(Lorebook.class, foreign)))
                .isInstanceOf(SecurityException.class);

        RestoreResult result = trashService.restore(List.of(key(Lorebook.class, mine)));
        assertThat(result.isBlocked()).isFalse();
        assertThat(result.getRestored()).containsExactly(key(Lorebook.class, mine));
        assertThat(lorebookService.findAllForUser()).extracting(Lorebook::getUuid).containsExactly(mine.getUuid());
        assertThat(trash(TrashFilter.all())).isEmpty();

        // the foreign lorebook is still deleted
        loginAs(other);
        assertThat(trash(TrashFilter.all())).containsExactly(key(Lorebook.class, foreign));
        assertThat(user.getId()).isNotEqualTo(other.getId());
    }

    @Test
    void administratorSeesAllUsersAndFiltersThem() throws Exception {
        User first = login();
        Lorebook firstBook = lorebook(uniqueName("first"));
        delete(firstBook);
        LorebookEntry firstEntry = entry(lorebook(uniqueName("kept")), uniqueName("entry"));
        delete(firstEntry);

        User second = login();
        Lorebook secondBook = lorebook(uniqueName("second"));
        delete(secondBook);

        loginAdmin();
        assertThat(trash(TrashFilter.all())).contains(key(Lorebook.class, firstBook), key(Lorebook.class, secondBook),
                key(LorebookEntry.class, firstEntry));
        assertThat(trash(new TrashFilter(first.getId(), null)))
                .containsExactlyInAnyOrder(key(Lorebook.class, firstBook), key(LorebookEntry.class, firstEntry));
        assertThat(trash(new TrashFilter(first.getId(), Lorebook.class))).containsExactly(key(Lorebook.class, firstBook));
        assertThat(trash(new TrashFilter(second.getId(), LorebookEntry.class))).isEmpty();
        assertThat(trashService.count(new TrashFilter(first.getId(), null))).isEqualTo(2);
        assertThat(trashService.getTypes()).contains(Lorebook.class, LorebookEntry.class, Manuscript.class, Protocol.class);

        // administrator restores for the user
        assertThat(trashService.restore(List.of(key(Lorebook.class, secondBook))).getRestored()).hasSize(1);
        loginAs(second);
        assertThat(lorebookService.findAllForUser()).extracting(Lorebook::getUuid).containsExactly(secondBook.getUuid());
    }

    @Test
    void objectIsNotRestoredWithoutItsDeletedParent() throws Exception {
        login();
        Lorebook lorebook = lorebook(uniqueName("book"));
        LorebookEntry entry = entry(lorebook, "the entry");
        delete(entry);
        delete(lorebook);
        EntityKey lorebookKey = key(Lorebook.class, lorebook);
        EntityKey entryKey = key(LorebookEntry.class, entry);

        RestoreResult result = trashService.restore(List.of(entryKey));
        assertThat(result.isBlocked()).isTrue();
        assertThat(result.getRestored()).isEmpty();
        assertThat(result.getBlocked()).containsOnlyKeys(entryKey);
        assertThat(result.getBlocked().get(entryKey)).singleElement().satisfies(blocker -> {
            assertThat(blocker.parent()).isEqualTo(lorebookKey);
            assertThat(blocker.label()).isEqualTo(lorebook.getName());
        });
        assertThat(result.getBlockedLabels().get(entryKey)).isEqualTo("the entry");
        assertThat(trash(TrashFilter.all())).containsExactlyInAnyOrder(lorebookKey, entryKey);

        // with its parent in the selection, in any order
        result = trashService.restore(List.of(entryKey, lorebookKey));
        assertThat(result.isBlocked()).isFalse();
        assertThat(result.getRestored()).containsExactlyInAnyOrder(lorebookKey, entryKey);
        assertThat(trash(TrashFilter.all())).isEmpty();
        assertThat(lorebookService.findAllForUser()).hasSize(1);
        assertThat(lorebookEntryService.find(entry.getId()).isDeleted()).isFalse();
    }

    @Test
    void restoringBlockedObjectRestoresNothing() throws Exception {
        login();
        Lorebook free = lorebook(uniqueName("free"));
        delete(free);
        Lorebook parent = lorebook(uniqueName("parent"));
        LorebookEntry entry = entry(parent, "orphan");
        delete(entry);
        delete(parent);

        RestoreResult result = trashService.restore(List.of(key(Lorebook.class, free), key(LorebookEntry.class, entry)));
        assertThat(result.isBlocked()).isTrue();
        assertThat(lorebookService.find(free.getId()).isDeleted()).isTrue();
    }

    @Test
    void strongReferenceToDeletedObjectBlocksRestore() throws Exception {
        login();
        OpenAICompatible ai = createAI();
        ChatCompletionProtocol protocol = new ChatCompletionProtocol();
        protocol.setName(uniqueName("protocol"));
        protocol.setProtocolType(com.github.enerccio.marginalia.domain.collections.ProtocolType.CHAT_COMPLETION);
        protocol.setMaxTokens(1000);
        protocol.setReplyTokens(100);
        Protocol savedProtocol = protocolService.save(protocol);

        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript.setAi(ai);
        manuscript.setProtocol(savedProtocol);
        manuscript = manuscriptService.save(manuscript);

        manuscriptService.delete(manuscriptService.find(manuscript.getId()), false);
        protocolService.delete(protocolService.find(savedProtocol.getId()), false);

        EntityKey manuscriptKey = key(Manuscript.class, manuscript);
        EntityKey protocolKey = key(Protocol.class, savedProtocol);
        RestoreResult blocked = trashService.restore(List.of(manuscriptKey));
        assertThat(blocked.getBlocked().get(manuscriptKey)).extracting(TrashService.Blocker::parent).containsExactly(protocolKey);

        // the provider (AI) was not deleted, so only the protocol is needed
        assertThat(trashService.restore(List.of(manuscriptKey, protocolKey)).getRestored()).hasSize(2);
        assertThat(manuscriptService.findAllForUser()).extracting(Manuscript::getUuid).containsExactly(manuscript.getUuid());
    }

    @Test
    void extendedContentIsShownAsJson() throws Exception {
        login();
        Lorebook lorebook = lorebook(uniqueName("book"));
        LorebookEntry entry = entry(lorebook, "with content");
        LorebookEntry loaded = lorebookEntryService.find(entry.getId());
        JsonObject data = new JsonObject();
        data.addProperty("answer", 42);
        loaded.getAttributes().add("test", data);
        lorebookEntryService.save(loaded);
        delete(entry);
        delete(lorebook);

        TrashItem item = trashService.find(new TrashFilter(null, LorebookEntry.class), 0, 10).getFirst();
        assertThat(item.extendable()).isTrue();
        String json = trashService.getExtendedContent(item.key());
        assertThat(json).contains("\"answer\": 42").contains("\n");

        TrashItem book = trashService.find(new TrashFilter(null, Lorebook.class), 0, 10).getFirst();
        assertThat(book.extendable()).isTrue();
    }

    @Test
    void restoredObjectsAreNotListedAgain() throws Exception {
        login();
        Lorebook lorebook = lorebook(uniqueName("book"));
        delete(lorebook);
        EntityKey key = key(Lorebook.class, lorebook);
        assertThat(trashService.restore(List.of(key)).getRestored()).hasSize(1);

        RestoreResult again = trashService.restore(List.of(key));
        assertThat(again.isBlocked()).isFalse();
        assertThat(again.getRestored()).isEmpty();
    }

    @Test
    void notLoggedInSeesNothing() {
        currentUser.setId(null);
        assertThatThrownBy(() -> trashService.find(TrashFilter.all(), 0, 10)).isInstanceOf(SecurityException.class);
    }
}
