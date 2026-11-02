package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.model.impl.ChatCompletionProtocol;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Sharing a lorebook or a protocol with the other users ("Import from other user"): nothing is shared until the owner says
 * so, the others see it and import a copy, the original stays the owner's and nobody else can change it.
 */
class SharedLibraryTest extends MarginaliaTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    private User author;
    private User reader;

    @BeforeEach
    void logInAsAuthor() throws Exception {
        author = createUser();
        reader = createUser();
        loginAs(author);
    }

    // lorebook

    private Lorebook lorebook(String name, boolean shared) throws Exception {
        Lorebook book = new Lorebook();
        book.setName(name);
        book.setDescription("Places of the north");
        book.setEnabled(false);
        book.setShared(shared);
        book.getAttributes().addProperty("myplugin", "data");
        book = lorebookService.save(book);
        tagRelationService.createRelation(tagService.getOrCreateForUser("setting"), book);

        LorebookEntry harbour = new LorebookEntry();
        harbour.setLorebook(book);
        harbour.setName("Harbour");
        harbour.setPayload("A grey harbour");
        harbour.setOrder(7);
        harbour.setFiltering("harbour|port");
        harbour.setFilteringMode(FilteringMode.REGEX);
        harbour = lorebookEntryService.save(harbour);
        tagRelationService.createRelation(tagService.getOrCreateForUser("water"), harbour, false);
        tagRelationService.createRelation(tagService.getOrCreateForUser("inland"), harbour, true);
        return lorebookService.find(book);
    }

    private Lorebook addEntry(Lorebook book, String name, String payload) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(book);
        entry.setName(name);
        entry.setPayload(payload);
        lorebookEntryService.save(entry);
        return book;
    }

    @Test
    void aLorebookIsNotSharedUntilItsOwnerSaysSo() throws Exception {
        Lorebook book = lorebook(uniqueName("Private"), false);
        assertThat(book.isShared()).isFalse();

        loginAs(reader);

        assertThat(lorebookService.findAllShared()).extracting(Lorebook::getUuid).doesNotContain(book.getUuid());
        assertThatThrownBy(() -> lorebookService.importShared(book, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void otherUsersSeeASharedLorebookWithItsAuthorButTheOwnerDoesNot() throws Exception {
        Lorebook book = lorebook(uniqueName("Shared"), true);
        assertThat(lorebookService.findAllShared()).extracting(Lorebook::getUuid)
                .as("what the owner shares is not offered to the owner").doesNotContain(book.getUuid());

        loginAs(reader);

        List<Lorebook> shared = lorebookService.findAllShared();
        Lorebook found = shared.stream().filter(l -> l.getUuid().equals(book.getUuid())).findFirst().orElseThrow();
        assertThat(found.getOwner().getId()).isEqualTo(author.getId());
        assertThat(found.getDescription()).isEqualTo("Places of the north");
        assertThat(lorebookService.findAllRegularForUser()).extracting(Lorebook::getUuid)
                .as("it does not become a lorebook of the reader").doesNotContain(book.getUuid());
    }

    @Test
    void aDeletedOrUnsharedLorebookIsNotOffered() throws Exception {
        Lorebook deleted = lorebook(uniqueName("Deleted"), true);
        Lorebook unshared = lorebook(uniqueName("Unshared"), true);
        lorebookService.delete(deleted, false);
        unshared.setShared(false);
        lorebookService.save(unshared);

        loginAs(reader);

        assertThat(lorebookService.findAllShared()).extracting(Lorebook::getUuid).doesNotContain(deleted.getUuid(), unshared.getUuid());
        assertThatThrownBy(() -> lorebookService.importShared(deleted, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> lorebookService.importShared(unshared, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void importedLorebookIsACopyOfTheReader() throws Exception {
        Lorebook original = lorebook(uniqueName("Places"), true);
        loginAs(reader);

        Lorebook copy = lorebookService.importShared(original, null);

        assertThat(copy.getUuid()).isNotEqualTo(original.getUuid());
        assertThat(copy.getOwner().getId()).isEqualTo(reader.getId());
        assertThat(copy.getName()).isEqualTo(original.getName());
        assertThat(copy.getDescription()).isEqualTo("Places of the north");
        assertThat(copy.isEnabled()).isFalse();
        assertThat(copy.isShared()).as("a copy is private").isFalse();
        assertThat(copy.isAgentic()).isFalse();
        assertThat(copy.getAttributes().get("myplugin").getAsString()).isEqualTo("data");
        assertThat(tagRelationService.getTagsForObject(copy)).extracting(Tag::getValue).containsExactly("setting");

        List<LorebookEntry> entries = lorebookEntryService.getEntriesForLorebook(copy);
        assertThat(entries).extracting(LorebookEntry::getName).containsExactly("Harbour");
        LorebookEntry harbour = entries.getFirst();
        assertThat(harbour.getPayload()).isEqualTo("A grey harbour");
        assertThat(harbour.getOrder()).isEqualTo(7);
        assertThat(harbour.getFiltering()).isEqualTo("harbour|port");
        assertThat(harbour.getFilteringMode()).isEqualTo(FilteringMode.REGEX);
        assertThat(tagRelationService.getTagsForObject(harbour)).extracting(Tag::getValue).containsExactly("water");
        assertThat(tagRelationService.getTagsForObject(harbour, true)).extracting(Tag::getValue).containsExactly("inland");

        // it is the reader's: listed with their lorebooks, found by them, and still on offer from the author
        assertThat(lorebookService.findAllRegularForUser()).extracting(Lorebook::getUuid).containsExactly(copy.getUuid());
        assertThat(lorebookService.findForUser(copy.getUuid())).isNotNull();
        assertThat(lorebookService.findForUser(original.getUuid())).as("the original is not the reader's").isNull();
        assertThat(lorebookService.findAllShared()).extracting(Lorebook::getUuid).contains(original.getUuid());
    }

    @Test
    void importedLorebookCanBeNamedAndImportedAgain() throws Exception {
        Lorebook original = lorebook(uniqueName("Places"), true);
        loginAs(reader);

        Lorebook first = lorebookService.importShared(original, "My places");
        Lorebook second = lorebookService.importShared(original, null);

        assertThat(first.getName()).isEqualTo("My places");
        assertThat(lorebookService.find(first).getName()).isEqualTo("My places");
        assertThat(second.getName()).isEqualTo(original.getName());
        assertThat(second.getId()).isNotEqualTo(first.getId());
        assertThat(lorebookService.findAllRegularForUser()).hasSize(2);
    }

    @Test
    void subLorebooksAreCopiedWithTheLorebook() throws Exception {
        Lorebook original = lorebook(uniqueName("World"), true);
        Lorebook sub = lorebookService.createSubbook(original, "Cities");
        Lorebook deeper = lorebookService.createSubbook(sub, "Streets");
        addEntry(sub, "Capital", "The capital");
        addEntry(deeper, "Main street", "The main street");
        lorebookService.share(original, true);

        loginAs(reader);
        Lorebook copy = lorebookService.importShared(original, null);

        List<Lorebook> tree = lorebookService.getLorebookTree(copy);
        assertThat(tree).extracting(Lorebook::getName).containsExactlyInAnyOrder(original.getName(), "Cities", "Streets");
        assertThat(tree).extracting(Lorebook::getUuid).doesNotContain(original.getUuid(), sub.getUuid(), deeper.getUuid());
        assertThat(tree).allSatisfy(l -> assertThat(l.getOwner().getId()).isEqualTo(reader.getId()));
        Lorebook copiedSub = lorebookService.getSubbooks(copy).getFirst();
        assertThat(copiedSub.getName()).isEqualTo("Cities");
        assertThat(lorebookEntryService.getEntriesForLorebook(copiedSub)).extracting(LorebookEntry::getName).containsExactly("Capital");
        Lorebook copiedDeeper = lorebookService.getSubbooks(copiedSub).getFirst();
        assertThat(lorebookEntryService.getEntriesForLorebook(copiedDeeper)).extracting(LorebookEntry::getName).containsExactly("Main street");
        assertThat(lorebookService.findAllRegularForUser()).as("the reader has three lorebooks of their own").hasSize(3);
    }

    @Test
    void subLorebooksThatAreNotSharedAreNotCopiedWithEverythingBelowThem() throws Exception {
        Lorebook original = lorebook(uniqueName("World"), true);
        Lorebook shared = lorebookService.createSubbook(original, "Shared sub");
        Lorebook sharedDeep = lorebookService.createSubbook(shared, "Deep unshared");
        Lorebook hidden = lorebookService.createSubbook(original, "Hidden sub");
        Lorebook hiddenDeep = lorebookService.createSubbook(hidden, "Shared below hidden");
        addEntry(shared, "Visible", "visible");
        addEntry(sharedDeep, "Deep secret", "deep");
        addEntry(hidden, "Secret", "secret");
        addEntry(hiddenDeep, "Deeper secret", "deeper");
        shared.setShared(true);
        lorebookService.save(shared);
        hiddenDeep.setShared(true);
        lorebookService.save(hiddenDeep);

        loginAs(reader);
        Lorebook copy = lorebookService.importShared(original, null);

        List<Lorebook> tree = lorebookService.getLorebookTree(copy);
        assertThat(tree).extracting(Lorebook::getName).containsExactlyInAnyOrder(original.getName(), "Shared sub");
        assertThat(lorebookService.findAllRegularForUser()).extracting(Lorebook::getName)
                .containsExactlyInAnyOrder(original.getName(), "Shared sub");
        Lorebook copiedSub = lorebookService.getSubbooks(copy).getFirst();
        assertThat(lorebookEntryService.getEntriesForLorebook(copiedSub)).extracting(LorebookEntry::getName).containsExactly("Visible");
        assertThat(lorebookService.getSubbooks(copiedSub)).isEmpty();
    }

    @Test
    void unsharedSubLorebooksAreFoundBelowTheLorebookAtAnyDepth() throws Exception {
        Lorebook original = lorebook(uniqueName("World"), false);
        Lorebook shared = lorebookService.createSubbook(original, "Shared sub");
        Lorebook deep = lorebookService.createSubbook(shared, "Deep");
        Lorebook other = lorebookService.createSubbook(original, "Other");
        shared.setShared(true);
        lorebookService.save(shared);

        assertThat(lorebookService.findUnsharedSubbooks(original)).extracting(Lorebook::getUuid)
                .containsExactlyInAnyOrder(deep.getUuid(), other.getUuid());
        assertThat(lorebookService.findUnsharedSubbooks(deep)).isEmpty();

        loginAs(reader);
        assertThatThrownBy(() -> lorebookService.findUnsharedSubbooks(original)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sharingCanLeaveTheSubLorebooksOrShareThemToo() throws Exception {
        Lorebook original = lorebook(uniqueName("World"), false);
        Lorebook sub = lorebookService.createSubbook(original, "Sub");
        Lorebook deep = lorebookService.createSubbook(sub, "Deep");

        Lorebook shared = lorebookService.share(original, false);
        assertThat(shared.isShared()).isTrue();
        assertThat(lorebookService.find(sub).isShared()).isFalse();
        assertThat(lorebookService.find(deep).isShared()).isFalse();

        lorebookService.share(original, true);
        assertThat(lorebookService.find(original).isShared()).isTrue();
        assertThat(lorebookService.find(sub).isShared()).isTrue();
        assertThat(lorebookService.find(deep).isShared()).isTrue();
        assertThat(lorebookService.findUnsharedSubbooks(original)).isEmpty();
    }

    @Test
    void stoppingToShareLeavesTheSubLorebooksAlone() throws Exception {
        Lorebook original = lorebook(uniqueName("World"), false);
        Lorebook sub = lorebookService.createSubbook(original, "Sub");
        lorebookService.share(original, true);

        Lorebook stored = lorebookService.find(original);
        stored.setShared(false);
        lorebookService.save(stored);

        assertThat(lorebookService.find(original).isShared()).isFalse();
        assertThat(lorebookService.find(sub).isShared()).isTrue();
    }

    @Test
    void lorebookOfSomebodyElseCanNotBeSharedByTheReader() throws Exception {
        Lorebook original = lorebook(uniqueName("World"), false);

        loginAs(reader);

        assertThatThrownBy(() -> lorebookService.share(original, true)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> lorebookService.share(null, false)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void changingTheCopyLeavesTheOriginalAlone() throws Exception {
        Lorebook original = lorebook(uniqueName("Places"), true);
        loginAs(reader);
        Lorebook copy = lorebookService.importShared(original, null);

        copy.setDescription("changed");
        lorebookService.save(copy);
        addEntry(copy, "Inn", "A warm inn");

        assertThat(lorebookService.find(original).getDescription()).isEqualTo("Places of the north");
        assertThat(lorebookEntryService.getEntriesForLorebook(original)).extracting(LorebookEntry::getName).containsExactly("Harbour");
    }

    @Test
    void ownLorebookCanNotBeImportedAsShared() throws Exception {
        Lorebook own = lorebook(uniqueName("Own"), true);

        assertThatThrownBy(() -> lorebookService.importShared(own, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> lorebookService.importShared(null, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void workingCopyOfAnAgentIsNeverOffered() throws Exception {
        Lorebook agentic = lorebook(uniqueName("Agentic"), true);
        agentic.setAgentic(true);
        lorebookService.save(agentic);

        loginAs(reader);

        assertThat(lorebookService.findAllShared()).extracting(Lorebook::getUuid).doesNotContain(agentic.getUuid());
        assertThatThrownBy(() -> lorebookService.importShared(agentic, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void descriptionTravelsWithTheLorebookButTheSharingDoesNot() throws Exception {
        Lorebook original = lorebook(uniqueName("Places"), true);

        Lorebook clone = lorebookService.cloneLorebook(original, "Clone");
        assertThat(clone.getDescription()).isEqualTo("Places of the north");
        assertThat(clone.isShared()).isFalse();
        assertThat(lorebookService.findForUser(clone.getUuid()).getDescription()).isEqualTo("Places of the north");

        Lorebook imported = lorebookService.importLorebook(lorebookService.exportLorebook(original), null);
        assertThat(imported.getDescription()).isEqualTo("Places of the north");
        assertThat(imported.isShared()).isFalse();
    }

    // protocol

    private Protocol protocol(String name, boolean shared) throws Exception {
        ChatCompletionProtocol protocol = new ChatCompletionProtocol();
        protocol.setProtocolType(ProtocolType.CHAT_COMPLETION);
        protocol.setName(name);
        protocol.setMaxTokens(8000);
        protocol.setReplyTokens(700);
        protocol.setTemperatureEnabled(true);
        protocol.setTemperature(0.4);
        protocol.setTopPEnabled(true);
        protocol.setTopP(0.9);
        protocol.getAttributes().addProperty("myplugin", "p");
        protocol.setShared(shared);
        return protocolService.save(protocol);
    }

    @Test
    void aProtocolIsNotSharedUntilItsOwnerSaysSo() throws Exception {
        Protocol protocol = protocol(uniqueName("Private"), false);

        loginAs(reader);

        assertThat(protocolService.findAllShared()).extracting(Protocol::getUuid).doesNotContain(protocol.getUuid());
        assertThatThrownBy(() -> protocolService.importShared(protocol, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void otherUsersSeeASharedProtocolWithItsAuthor() throws Exception {
        Protocol protocol = protocol(uniqueName("Shared"), true);
        assertThat(protocolService.findAllShared()).extracting(Protocol::getUuid).doesNotContain(protocol.getUuid());

        loginAs(reader);

        Protocol found = protocolService.findAllShared().stream().filter(p -> p.getUuid().equals(protocol.getUuid())).findFirst().orElseThrow();
        assertThat(found.getOwner().getId()).isEqualTo(author.getId());
        assertThat(protocolService.findAllForUser()).extracting(Protocol::getUuid).doesNotContain(protocol.getUuid());
    }

    @Test
    void importedProtocolIsACopyOfTheReader() throws Exception {
        Protocol original = protocol(uniqueName("Precise"), true);
        loginAs(reader);

        Protocol copy = protocolService.importShared(original, null);

        assertThat(copy.getUuid()).isNotEqualTo(original.getUuid());
        assertThat(copy.getOwner().getId()).isEqualTo(reader.getId());
        assertThat(copy).isInstanceOf(ChatCompletionProtocol.class);
        assertThat(copy.getName()).isEqualTo(original.getName());
        assertThat(copy.isShared()).as("a copy is private").isFalse();

        Protocol stored = protocolService.findForUser(copy.getUuid());
        assertThat(stored.getMaxTokens()).isEqualTo(8000);
        assertThat(stored.getReplyTokens()).isEqualTo(700);
        assertThat(stored.getTemperatureEnabled()).isTrue();
        assertThat(stored.getTemperature()).isEqualTo(0.4);
        assertThat(stored.getTopP()).isEqualTo(0.9);
        assertThat(stored.getAttributes().get("myplugin").getAsString()).isEqualTo("p");
        assertThat(protocolService.findForUser(original.getUuid())).as("the original is not the reader's").isNull();
        assertThat(protocolService.importShared(original, "Mine").getName()).isEqualTo("Mine");
    }

    @Test
    void unsharedOrDeletedOrOwnProtocolCanNotBeImported() throws Exception {
        Protocol own = protocol(uniqueName("Own"), true);
        Protocol deleted = protocol(uniqueName("Deleted"), true);
        protocolService.delete(deleted, false);
        Protocol unshared = protocol(uniqueName("Unshared"), true);
        unshared.setShared(false);
        protocolService.save(unshared);

        assertThatThrownBy(() -> protocolService.importShared(own, null)).isInstanceOf(IllegalArgumentException.class);

        loginAs(reader);

        assertThat(protocolService.findAllShared()).extracting(Protocol::getUuid).doesNotContain(deleted.getUuid(), unshared.getUuid());
        assertThatThrownBy(() -> protocolService.importShared(deleted, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> protocolService.importShared(unshared, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> protocolService.importShared(null, null)).isInstanceOf(IllegalArgumentException.class);
    }
}
