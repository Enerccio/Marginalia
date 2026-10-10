package com.github.enerccio.marginalia.cleanup;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.Setting;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.CleanupService.CleanupContributor;
import com.github.enerccio.marginalia.domain.service.CleanupService.CleanupPlan;
import com.github.enerccio.marginalia.domain.service.CleanupService.EntityKey;
import com.github.enerccio.marginalia.domain.service.CleanupService.ReferenceDescriptor;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Garbage collection of soft deleted entities ({@link CleanupService}): what is purged, what is purged together with
 * it (owned data), what blocks a purge (strong references from live data) and what is only unlinked (weak
 * references).
 * <p>
 * The test database is shared and purge works on all of it, so tests only assert on the entities they created.
 */
class CleanupServiceTest extends MarginaliaTestBase {

    @Autowired
    private CleanupService cleanupService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private SettingService settingService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private com.github.enerccio.marginalia.Configuration configuration;

    private final List<CleanupContributor> contributors = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        loginAdmin();
    }

    @Test
    void onlyAdministratorsUseCleanup() throws Exception {
        login();

        assertThatThrownBy(() -> cleanupService.getReferenceModel()).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> cleanupService.analyze()).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> cleanupService.purge()).isInstanceOf(SecurityException.class);
    }

    @AfterEach
    void unregisterContributors() {
        contributors.forEach(cleanupService::unregisterContributor);
    }

    // ------------------------------------------------------------------------------------------------------------
    // builders and helpers
    // ------------------------------------------------------------------------------------------------------------

    private AI ai(String name) throws Exception {
        OpenAICompatible ai = new OpenAICompatible();
        ai.setAiType(AIType.OPEN_AI_COMPATIBLE);
        ai.setName(name);
        ai.setEnabledReasoning(false);
        return aiService.save(ai);
    }

    private Protocol protocol(String name) throws Exception {
        ChatCompletionProtocol protocol = new ChatCompletionProtocol();
        protocol.setName(name);
        protocol.setProtocolType(ProtocolType.CHAT_COMPLETION);
        protocol.setMaxTokens(1000);
        protocol.setReplyTokens(100);
        return protocolService.save(protocol);
    }

    private Lorebook lorebook(String name, Lorebook... subbooks) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(name);
        lorebook.setSubbooks(new ArrayList<>(List.of(subbooks)));
        return lorebookService.save(lorebook);
    }

    private LorebookEntry entry(Lorebook lorebook, String name) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        return lorebookEntryService.save(entry);
    }

    private Manuscript manuscript(AI ai, Protocol protocol, Lorebook lorebook) throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        manuscript.setAi(ai);
        manuscript.setProtocol(protocol);
        manuscript.setLorebook(lorebook);
        return manuscriptService.save(manuscript);
    }

    private ChatMessage message(Manuscript manuscript, ChatMessage parent, String response) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(response);
        return parent == null ? chatMessageService.createRoot(manuscript, message) : chatMessageService.addChild(parent, message);
    }

    private Summary summary(ChatMessage message) throws Exception {
        Summary summary = new Summary();
        summary.setSummary("summary of " + message.getResponse());
        summary = summaryService.save(summary);
        ChatMessage loaded = chatMessageService.find(message.getId());
        loaded.setSummary(summary);
        chatMessageService.save(loaded);
        return summary;
    }

    private Tag tag(String value) throws Exception {
        return tagService.getOrCreateForUser(uniqueName(value));
    }

    private Manuscript setActiveLeaf(Manuscript manuscript, ChatMessage leaf) throws Exception {
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(leaf);
        return manuscriptService.save(loaded);
    }

    private static EntityKey key(Class<?> rootType, BaseEntity entity) {
        return new EntityKey(rootType, entity.getId());
    }

    private static EntityKey key(AI ai) {
        return key(AI.class, ai);
    }

    private static EntityKey key(Protocol protocol) {
        return key(Protocol.class, protocol);
    }

    private static EntityKey key(BaseEntity entity) {
        return key(entity.getClass(), entity);
    }

    private void softDelete(AI ai) throws Exception {
        aiService.delete(aiService.find(ai.getId()), false);
    }

    private void softDelete(Protocol protocol) throws Exception {
        protocolService.delete(protocolService.find(protocol.getId()), false);
    }

    private void softDelete(Lorebook lorebook) throws Exception {
        lorebookService.delete(lorebookService.find(lorebook.getId()), false);
    }

    private void softDelete(Manuscript manuscript) throws Exception {
        manuscriptService.delete(manuscriptService.find(manuscript.getId()), false);
    }

    private void softDelete(ChatMessage message) throws Exception {
        chatMessageService.delete(chatMessageService.find(message.getId()), false);
    }

    private CleanupContributor contribute(CleanupContributor contributor) {
        cleanupService.registerContributor(contributor);
        contributors.add(contributor);
        return contributor;
    }

    // ------------------------------------------------------------------------------------------------------------
    // reference model
    // ------------------------------------------------------------------------------------------------------------

    private ReferenceDescriptor reference(String entity, String field) throws Exception {
        return cleanupService.getReferenceModel().stream()
                .filter(r -> r.getReferrerEntity().equals(entity) && r.getField().equals(field))
                .findFirst().orElseThrow(() -> new AssertionError("No reference " + entity + "." + field));
    }

    @Test
    void referenceModelReflectsAnnotations() throws Exception {
        assertThat(reference("Manuscript", "ai").getPolicy()).isEqualTo(Policy.STRONG);
        assertThat(reference("Manuscript", "ai").getTargetType()).isEqualTo(AI.class);
        assertThat(reference("Manuscript", "activeLeaf").getPolicy()).isEqualTo(Policy.STRONG);
        assertThat(reference("Manuscript", "owner").getPolicy()).isEqualTo(Policy.OWNED_BY);
        assertThat(reference("ChatMessage", "parentScript").getPolicy()).isEqualTo(Policy.OWNED_BY);
        assertThat(reference("ChatMessage", "parent").getPolicy()).isEqualTo(Policy.OWNED_BY);
        assertThat(reference("ChatMessage", "summary").getPolicy()).isEqualTo(Policy.OWNS);
        assertThat(reference("LorebookEntry", "lorebook").getPolicy()).isEqualTo(Policy.OWNED_BY);
        assertThat(reference("Lorebook", "subbooks").getPolicy()).isEqualTo(Policy.WEAK);
        assertThat(reference("Lorebook", "subbooks").isCollection()).isTrue();
        assertThat(reference("Setting", "owner").getPolicy()).isEqualTo(Policy.OWNED_BY);

        ReferenceDescriptor objectId = reference("TagRelation", "objectId");
        assertThat(objectId.getPolicy()).isEqualTo(Policy.OWNED_BY);
        assertThat(objectId.isAssociation()).isFalse();
        assertThat(objectId.getTargetType()).isNull();
        assertThat(objectId.getTargetClassField()).isEqualTo("clazz");

        ReferenceDescriptor defaultModel = reference("UserSetting", "defaultModel");
        assertThat(defaultModel.getPolicy()).isEqualTo(Policy.WEAK);
        assertThat(defaultModel.getTargetType()).isEqualTo(AI.class);
        assertThat(defaultModel.isPersistent()).as("stored in extended attributes").isFalse();
    }

    // ------------------------------------------------------------------------------------------------------------
    // basics
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void unreferencedSoftDeletedEntityIsPurged() throws Exception {
        AI ai = ai("unused");
        AI live = ai("live");
        softDelete(ai);

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.isExecuted()).isTrue();
        assertThat(plan.getPurge()).contains(key(ai)).doesNotContain(key(live));
        assertThat(aiService.find(ai.getId())).isNull();
        assertThat(aiService.find(live.getId())).isNotNull();
    }

    @Test
    void analyzeChangesNothing() throws Exception {
        AI ai = ai("unused");
        softDelete(ai);

        CleanupPlan plan = cleanupService.analyze();

        assertThat(plan.isExecuted()).isFalse();
        assertThat(plan.getPurge()).contains(key(ai));
        assertThat(plan.getStats().get(AI.class).getSoftDeleted()).isPositive();
        assertThat(aiService.find(ai.getId())).isNotNull();
    }

    @Test
    void deletedResourceIsPurgedButItsFileStays() throws Exception {
        Resource deleted = resourceService.upload("gone.txt", uniqueName("gone").getBytes(java.nio.charset.StandardCharsets.UTF_8), "text/plain");
        Resource live = resourceService.upload("live.txt", uniqueName("live").getBytes(java.nio.charset.StandardCharsets.UTF_8), "text/plain");
        // the object it points to is gone or never existed, nothing depends on that
        resourceService.link(deleted, ChatMessage.class, 123456789L);
        resourceService.softDelete(List.of(deleted.getUuid()));

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getPurge()).contains(key(deleted)).doesNotContain(key(live));
        assertThat(resourceService.find(deleted.getId())).isNull();
        assertThat(resourceService.find(live.getId())).isNotNull();
        assertThat(new java.io.File(configuration.getResourcesFolder(currentUser), deleted.getPath())).exists();
    }

    @Test
    void liveEntitiesAreNeverPurged() throws Exception {
        Lorebook lorebook = lorebook("lore");
        LorebookEntry entry = entry(lorebook, "entry");
        Manuscript manuscript = manuscript(ai("ai"), protocol("protocol"), lorebook);
        ChatMessage root = message(manuscript, null, "root");
        Summary summary = summary(root);

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getPurge()).doesNotContain(key(manuscript), key(lorebook), key(entry), key(root), key(summary));
        assertThat(manuscriptService.find(manuscript.getId())).isNotNull();
        assertThat(chatMessageService.find(root.getId())).isNotNull();
        assertThat(summaryService.find(summary.getId())).isNotNull();
    }

    @Test
    void secondPurgeHasNothingLeftOfOurs() throws Exception {
        AI ai = ai("unused");
        softDelete(ai);
        cleanupService.purge();

        CleanupPlan again = cleanupService.purge();

        assertThat(again.getPurge()).doesNotContain(key(ai));
        assertThat(again.getBlocked()).doesNotContainKey(key(ai));
    }

    // ------------------------------------------------------------------------------------------------------------
    // strong references block
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void deletedAiAndProtocolUsedByLiveManuscriptAreBlocked() throws Exception {
        AI ai = ai("in use");
        Protocol protocol = protocol("in use");
        Manuscript manuscript = manuscript(ai, protocol, null);
        softDelete(ai);
        softDelete(protocol);

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getPurge()).doesNotContain(key(ai), key(protocol));
        assertThat(plan.getBlocked()).containsKeys(key(ai), key(protocol));
        CleanupService.BlockedEntity blocked = plan.getBlocked().get(key(ai));
        assertThat(blocked.getReasons()).singleElement().asString()
                .contains("Manuscript #" + manuscript.getId()).contains("(ai)");
        assertThat(aiService.find(ai.getId())).isNotNull();
    }

    @Test
    void blockedEntityHasReadableLabel() throws Exception {
        AI ai = ai("Readable Name");
        manuscript(ai, null, null);
        softDelete(ai);

        assertThat(cleanupService.analyze().getBlocked().get(key(ai)).getLabel()).isEqualTo("Readable Name");
    }

    @Test
    void deletingTheReferrerUnblocks() throws Exception {
        AI ai = ai("in use");
        Manuscript manuscript = manuscript(ai, null, null);
        softDelete(ai);
        assertThat(cleanupService.analyze().getBlocked()).containsKey(key(ai));

        softDelete(manuscript);
        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getPurge()).contains(key(ai), key(manuscript));
        assertThat(aiService.find(ai.getId())).isNull();
        assertThat(manuscriptService.find(manuscript.getId())).isNull();
    }

    @Test
    void unlinkingTheReferrerUnblocks() throws Exception {
        AI ai = ai("in use");
        Manuscript manuscript = manuscript(ai, null, null);
        softDelete(ai);

        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setAi(ai("replacement"));
        manuscriptService.save(loaded);

        assertThat(cleanupService.purge().getPurge()).contains(key(ai));
    }

    @Test
    void blockedEntityKeepsWhatItReferencesAlive() throws Exception {
        // a deleted manuscript that can't be purged still references its deleted AI
        AI ai = ai("referenced by blocked");
        Manuscript manuscript = manuscript(ai, null, null);
        softDelete(ai);
        softDelete(manuscript);
        EntityKey manuscriptKey = key(manuscript);
        contribute(new CleanupContributor() {
            @Override
            public void collectReferences(Set<EntityKey> candidates, CleanupService.BlockSink sink) {
                sink.block(manuscriptKey, "kept by extension");
            }
        });

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getBlocked()).containsKeys(manuscriptKey, key(ai));
        assertThat(plan.getBlocked().get(manuscriptKey).getReasons()).containsExactly("kept by extension");
        assertThat(plan.getPurge()).doesNotContain(manuscriptKey, key(ai));
        assertThat(aiService.find(ai.getId())).isNotNull();
    }

    @Test
    void deletedLorebookUsedByLiveManuscriptIsBlockedWithItsEntries() throws Exception {
        Lorebook lorebook = lorebook("in use");
        LorebookEntry entry = entry(lorebook, "entry");
        manuscript(null, null, lorebook);
        softDelete(lorebook);

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getBlocked()).containsKey(key(lorebook));
        assertThat(plan.getPurge()).doesNotContain(key(lorebook), key(entry));
        assertThat(lorebookEntryService.find(entry.getId())).isNotNull();
    }

    // ------------------------------------------------------------------------------------------------------------
    // owned data goes with its owner
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void deletedManuscriptTakesItsStoryAndTagLinks() throws Exception {
        Manuscript manuscript = manuscript(null, null, null);
        ChatMessage root = message(manuscript, null, "root");
        ChatMessage child = message(manuscript, root, "child");
        ChatMessage sibling = message(manuscript, root, "sibling");
        Summary summary = summary(child);
        manuscript = setActiveLeaf(manuscript, child);
        Tag tag = tag("genre");
        TagRelation relation = tagRelationService.createRelation(tag, manuscript);
        softDelete(manuscript);

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getPurge()).contains(key(manuscript), key(root), key(child), key(sibling), key(summary), key(relation));
        assertThat(manuscriptService.find(manuscript.getId())).isNull();
        assertThat(chatMessageService.find(child.getId())).isNull();
        assertThat(summaryService.find(summary.getId())).isNull();
        assertThat(tagRelationService.find(relation.getId())).isNull();
        // the tag itself is shared vocabulary and stays
        assertThat(tagService.find(tag.getId())).isNotNull();
        assertThat(plan.getStats().get(ChatMessage.class).getPurgeable()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void deletedLorebookTakesItsEntriesAndTagLinks() throws Exception {
        Lorebook lorebook = lorebook("old");
        LorebookEntry entry = entry(lorebook, "entry");
        Tag tag = tag("magic");
        TagRelation bookTag = tagRelationService.createRelation(tag, lorebook);
        TagRelation entryTag = tagRelationService.createRelation(tag, entry, true);
        softDelete(lorebook);

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getPurge()).contains(key(lorebook), key(entry), key(bookTag), key(entryTag));
        assertThat(lorebookEntryService.find(entry.getId())).isNull();
        assertThat(tagRelationService.find(entryTag.getId())).isNull();
        assertThat(tagService.find(tag.getId())).isNotNull();
    }

    @Test
    void deletedEntryAloneIsPurged() throws Exception {
        Lorebook lorebook = lorebook("live");
        LorebookEntry kept = entry(lorebook, "kept");
        LorebookEntry deleted = entry(lorebook, "deleted");
        TagRelation relation = tagRelationService.createRelation(tag("t"), deleted);
        lorebookEntryService.delete(lorebookEntryService.find(deleted.getId()), false);

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getPurge()).contains(key(deleted), key(relation)).doesNotContain(key(kept), key(lorebook));
        assertThat(lorebookEntryService.getEntriesForLorebook(lorebook)).extracting(LorebookEntry::getId).containsExactly(kept.getId());
    }

    @Test
    void softDeletedTagGoesWithItsRelations() throws Exception {
        Lorebook lorebook = lorebook("live");
        Tag tag = tag("obsolete");
        TagRelation relation = tagRelationService.createRelation(tag, lorebook);
        tagService.delete(tagService.find(tag.getId()), false);

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getPurge()).contains(key(tag), key(relation)).doesNotContain(key(lorebook));
        assertThat(tagService.find(tag.getId())).isNull();
        assertThat(lorebookService.find(lorebook.getId())).isNotNull();
    }

    @Test
    void liveTagLinkedToDeletedObjectLosesOnlyTheLink() throws Exception {
        Tag tag = tag("shared");
        Lorebook deleted = lorebook("deleted");
        Lorebook live = lorebook("live");
        TagRelation deletedLink = tagRelationService.createRelation(tag, deleted);
        TagRelation liveLink = tagRelationService.createRelation(tag, live);
        softDelete(deleted);

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getPurge()).contains(key(deletedLink)).doesNotContain(key(liveLink), key(tag));
        assertThat(tagRelationService.getTagsForObject(live)).extracting(Tag::getId).containsExactly(tag.getId());
    }

    // ------------------------------------------------------------------------------------------------------------
    // story tree
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void deletedMessageWithItsSummaryIsPurged() throws Exception {
        Manuscript manuscript = manuscript(null, null, null);
        ChatMessage root = message(manuscript, null, "root");
        ChatMessage leaf = message(manuscript, root, "leaf");
        Summary summary = summary(leaf);
        setActiveLeaf(manuscript, root);
        softDelete(leaf);

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getPurge()).contains(key(leaf), key(summary)).doesNotContain(key(root), key(manuscript));
        assertThat(summaryService.find(summary.getId())).isNull();
    }

    @Test
    void activeLeafCannotBePurged() throws Exception {
        Manuscript manuscript = manuscript(null, null, null);
        ChatMessage root = message(manuscript, null, "root");
        ChatMessage leaf = message(manuscript, root, "leaf");
        setActiveLeaf(manuscript, leaf);
        softDelete(leaf);

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getBlocked()).containsKey(key(leaf));
        assertThat(plan.getBlocked().get(key(leaf)).getReasons()).anyMatch(r -> r.contains("activeLeaf"));
        assertThat(chatMessageService.find(leaf.getId())).isNotNull();
    }

    @Test
    void deletedMessageTakesItsDescendants() throws Exception {
        // deleting without migrating children: the subtree belongs to the deleted message
        Manuscript manuscript = manuscript(null, null, null);
        ChatMessage root = message(manuscript, null, "root");
        ChatMessage branch = message(manuscript, root, "branch");
        ChatMessage child = message(manuscript, branch, "child");
        ChatMessage grandchild = message(manuscript, child, "grandchild");
        setActiveLeaf(manuscript, root);
        softDelete(branch);

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getPurge()).contains(key(branch), key(child), key(grandchild)).doesNotContain(key(root));
        assertThat(chatMessageService.find(grandchild.getId())).isNull();
        assertThat(chatMessageService.find(root.getId())).isNotNull();
    }

    @Test
    void descendantThatIsActiveLeafBlocksTheDeletedBranch() throws Exception {
        Manuscript manuscript = manuscript(null, null, null);
        ChatMessage root = message(manuscript, null, "root");
        ChatMessage branch = message(manuscript, root, "branch");
        ChatMessage leaf = message(manuscript, branch, "leaf");
        setActiveLeaf(manuscript, leaf);
        softDelete(branch);

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getBlocked()).containsKey(key(branch));
        assertThat(plan.getBlocked().get(key(branch)).getReasons())
                .anyMatch(r -> r.startsWith("ChatMessage #" + leaf.getId()) && r.contains("activeLeaf"));
        assertThat(plan.getPurge()).doesNotContain(key(branch), key(leaf));
        assertThat(chatMessageService.find(leaf.getId())).isNotNull();
    }

    @Test
    void deletedNodeWithMigratedChildrenIsPurgedAlone() throws Exception {
        Manuscript manuscript = manuscript(null, null, null);
        ChatMessage root = message(manuscript, null, "root");
        ChatMessage middle = message(manuscript, root, "middle");
        ChatMessage leaf = message(manuscript, middle, "leaf");
        Manuscript loaded = setActiveLeaf(manuscript, leaf);
        chatMessageService.deleteNodeAndMigrateChildren(chatMessageService.find(middle.getId()), loaded, false);

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getPurge()).contains(key(middle)).doesNotContain(key(leaf), key(root));
        assertThat(chatMessageService.getBranchFromLeaf(chatMessageService.find(leaf.getId())))
                .extracting(ChatMessage::getId).containsExactly(root.getId(), leaf.getId());
    }

    @Test
    void summaryStillUsedByLiveMessageSurvives() throws Exception {
        Manuscript manuscript = manuscript(null, null, null);
        ChatMessage root = message(manuscript, null, "root");
        ChatMessage deleted = message(manuscript, root, "deleted");
        ChatMessage live = message(manuscript, root, "live");
        Summary shared = summary(deleted);
        ChatMessage loaded = chatMessageService.find(live.getId());
        loaded.setSummary(shared);
        chatMessageService.save(loaded);
        setActiveLeaf(manuscript, live);
        softDelete(deleted);

        CleanupPlan plan = cleanupService.purge();

        // the summary is only optional to the deleted message, so it's excluded instead of blocking the message
        assertThat(plan.getPurge()).contains(key(deleted)).doesNotContain(key(shared));
        assertThat(plan.getBlocked()).doesNotContainKey(key(deleted));
        assertThat(summaryService.find(shared.getId())).isNotNull();
        assertThat(chatMessageService.find(live.getId()).getSummary().getId()).isEqualTo(shared.getId());
    }

    // ------------------------------------------------------------------------------------------------------------
    // weak references are unlinked
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void deletedSubbookIsPurgedAndUnlinked() throws Exception {
        Lorebook sub = lorebook("sub");
        Lorebook other = lorebook("other");
        Lorebook parent = lorebook("parent", sub, other);
        softDelete(sub);

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getPurge()).contains(key(sub)).doesNotContain(key(parent));
        assertThat(lorebookService.find(sub.getId())).isNull();
        assertThat(lorebookService.find(parent.getId()).getSubbooks()).extracting(Lorebook::getId).containsExactly(other.getId());
    }

    @Test
    void cyclicDeletedLorebooksArePurged() throws Exception {
        Lorebook a = lorebook("a");
        Lorebook b = lorebook("b", a);
        Lorebook loadedA = lorebookService.find(a.getId());
        loadedA.getSubbooks().add(b);
        lorebookService.save(loadedA);
        softDelete(a);
        softDelete(b);

        assertThat(cleanupService.purge().getPurge()).contains(key(a), key(b));
        assertThat(lorebookService.find(a.getId())).isNull();
        assertThat(lorebookService.find(b.getId())).isNull();
    }

    @Test
    void deletedDefaultModelIsPurgedAndClearedFromSettings() throws Exception {
        AI ai = ai("default");
        Protocol protocol = protocol("default");
        UserSetting settings = settingService.getOrCreate(UserSetting.class);
        settings.setDefaultModel(ai.getId());
        settings.setDefaultProtocol(protocol.getId());
        settings.setDefaultPov("kept");
        settingService.save(settings);
        softDelete(ai);

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getPurge()).contains(key(ai)).doesNotContain(key(Setting.class, settings));
        UserSetting reloaded = settingService.getOrCreate(UserSetting.class);
        assertThat(reloaded.getDefaultModel()).isNull();
        assertThat(reloaded.getDefaultProtocol()).isEqualTo(protocol.getId());
        assertThat(reloaded.getDefaultPov()).isEqualTo("kept");
    }

    // ------------------------------------------------------------------------------------------------------------
    // users
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void deletedUserIsPurgedWithAllTheirData() throws Exception {
        User owner = loginAs(createUser());
        AI ai = ai("owned");
        Protocol protocol = protocol("owned");
        Lorebook lorebook = lorebook("owned");
        LorebookEntry entry = entry(lorebook, "entry");
        Manuscript manuscript = manuscript(ai, protocol, lorebook);
        ChatMessage root = message(manuscript, null, "root");
        Summary summary = summary(root);
        setActiveLeaf(manuscript, root);
        Tag tag = tag("owned");
        UserSetting settings = settingService.getOrCreate(UserSetting.class);
        loginAdmin();
        userService.deleteUser(userService.find(owner.getId()));

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getBlocked()).doesNotContainKey(new EntityKey(User.class, owner.getId()));
        assertThat(plan.getPurge()).contains(new EntityKey(User.class, owner.getId()), key(ai), key(protocol),
                key(lorebook), key(entry), key(manuscript), key(root), key(summary), key(tag),
                key(Setting.class, settings));
        assertThat(userService.find(owner.getId())).isNull();
        assertThat(aiService.find(ai.getId())).isNull();
        assertThat(protocolService.find(protocol.getId())).isNull();
        assertThat(lorebookService.find(lorebook.getId())).isNull();
        assertThat(lorebookEntryService.find(entry.getId())).isNull();
        assertThat(manuscriptService.find(manuscript.getId())).isNull();
        assertThat(chatMessageService.find(root.getId())).isNull();
        assertThat(summaryService.find(summary.getId())).isNull();
        assertThat(tagService.find(tag.getId())).isNull();
    }

    @Test
    void deletedUserIsBlockedByLiveDataOfOthers() throws Exception {
        User owner = loginAs(createUser());
        AI ai = ai("owned");
        loginAs(createUser());
        Manuscript foreign = manuscript(ai, null, null);
        loginAdmin();
        userService.deleteUser(userService.find(owner.getId()));

        CleanupPlan plan = cleanupService.purge();

        EntityKey userKey = new EntityKey(User.class, owner.getId());
        assertThat(plan.getBlocked()).containsKey(userKey);
        assertThat(plan.getBlocked().get(userKey).getReasons()).anyMatch(r -> r.contains("Manuscript #" + foreign.getId()));
        assertThat(userService.find(owner.getId())).isNotNull();
        assertThat(aiService.find(ai.getId())).isNotNull();
    }

    @Test
    void deletedUserWithOnlySettingsIsPurgedWithThem() throws Exception {
        User owner = loginAs(createUser());
        UserSetting settings = settingService.getOrCreate(UserSetting.class);
        loginAdmin();
        userService.deleteUser(userService.find(owner.getId()));

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getPurge()).contains(new EntityKey(User.class, owner.getId()), key(Setting.class, settings));
        assertThat(userService.find(owner.getId())).isNull();
        assertThat(settingService.find(settings.getId())).isNull();
    }

    @Test
    void deletedUserIsPurgedOnceTheirDataIsDeleted() throws Exception {
        User owner = loginAs(createUser());
        AI ai = ai("owned");
        softDelete(ai);
        loginAdmin();
        userService.deleteUser(userService.find(owner.getId()));

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getPurge()).contains(new EntityKey(User.class, owner.getId()), key(ai));
        assertThat(userService.find(owner.getId())).isNull();
    }

    // ------------------------------------------------------------------------------------------------------------
    // contributors
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void contributorCanBlockAndSeesPurge() throws Exception {
        AI kept = ai("kept by extension");
        AI purged = ai("purged");
        softDelete(kept);
        softDelete(purged);
        List<Set<EntityKey>> seenCandidates = new CopyOnWriteArrayList<>();
        List<Set<EntityKey>> seenPurge = new CopyOnWriteArrayList<>();
        contribute(new CleanupContributor() {
            @Override
            public void collectReferences(Set<EntityKey> candidates, CleanupService.BlockSink sink) {
                seenCandidates.add(Set.copyOf(candidates));
                sink.block(key(kept), "extension data");
            }

            @Override
            public void beforePurge(Set<EntityKey> purge) {
                seenPurge.add(Set.copyOf(purge));
            }
        });

        CleanupPlan plan = cleanupService.purge();

        assertThat(seenCandidates.getFirst()).contains(key(kept), key(purged));
        assertThat(plan.getBlocked().get(key(kept)).getReasons()).containsExactly("extension data");
        assertThat(seenPurge).singleElement().satisfies(p -> assertThat(p).contains(key(purged)).doesNotContain(key(kept)));
        assertThat(aiService.find(kept.getId())).isNotNull();
        assertThat(aiService.find(purged.getId())).isNull();
    }

    @Test
    void contributorBlockingLiveEntityIsIgnored() throws Exception {
        AI live = ai("live");
        AI deleted = ai("deleted");
        softDelete(deleted);
        contribute(new CleanupContributor() {
            @Override
            public void collectReferences(Set<EntityKey> candidates, CleanupService.BlockSink sink) {
                sink.block(key(live), "not a candidate");
            }
        });

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getBlocked()).doesNotContainKey(key(live));
        assertThat(plan.getPurge()).contains(key(deleted));
    }

    @Test
    void unregisteredContributorIsNotAsked() throws Exception {
        AI ai = ai("deleted");
        softDelete(ai);
        CleanupContributor contributor = contribute(new CleanupContributor() {
            @Override
            public void collectReferences(Set<EntityKey> candidates, CleanupService.BlockSink sink) {
                sink.block(key(ai), "blocked");
            }
        });
        assertThat(cleanupService.analyze().getBlocked()).containsKey(key(ai));

        cleanupService.unregisterContributor(contributor);

        assertThat(cleanupService.purge().getPurge()).contains(key(ai));
    }

    @Test
    void contributorSeesOwnedDataAsCandidates() throws Exception {
        Lorebook lorebook = lorebook("deleted");
        LorebookEntry entry = entry(lorebook, "entry");
        softDelete(lorebook);
        // an extension keeping a lorebook entry alive keeps the whole lorebook
        contribute(new CleanupContributor() {
            @Override
            public void collectReferences(Set<EntityKey> candidates, CleanupService.BlockSink sink) {
                sink.block(key(entry), "entry history");
            }
        });

        CleanupPlan plan = cleanupService.purge();

        assertThat(plan.getBlocked()).containsKey(key(lorebook));
        assertThat(plan.getBlocked().get(key(lorebook)).getReasons()).anyMatch(r -> r.contains("entry history"));
        assertThat(plan.getPurge()).doesNotContain(key(lorebook), key(entry));
    }
}
