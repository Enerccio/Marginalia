package com.github.enerccio.marginalia.backup;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.BackupService.ManuscriptBackup;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookDecision;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookImportCandidate;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookMatch;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lorebooks carried by a manuscript backup: analysis (existing / same name / not found) and the effect of each
 * decision (link, create, skip, missing) on copy and restore, for the root lorebook and its sub-lorebooks.
 * <p>
 * Backup lorebook tree: World (tag "fantasy") -> Magic, Shared; Magic -> Shared.
 */
class BackupLorebookTest extends BackupTestBase {

    private Lorebook world;
    private Lorebook magic;
    private Lorebook shared;
    private Manuscript original;
    private ManuscriptBackup backup;

    @BeforeEach
    void createOriginal() throws Exception {
        login();
        shared = lorebook("Shared");
        entry(shared, "harbour", e -> e.setOrder(3));
        magic = lorebook("Magic", shared);
        LorebookEntry spells = entry(magic, "spells", BackupTestBase::fillEntry);
        tagRelationService.createRelation(tag("wizard"), spells);
        tagRelationService.createRelation(tag("grimdark"), spells, true);
        world = lorebook("World", magic, shared);
        tagRelationService.createRelation(tag("fantasy"), world);
        entry(world, "geography", e -> e.setOrder(1));
        entry(world, "history", e -> e.setOrder(2));
        original = story(manuscript("Book", null, null, world));
        backup = backupService.takeBackup(original);
    }

    // ------------------------------------------------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------------------------------------------------

    private Map<String, LorebookImportCandidate> analyze() throws Exception {
        return backupService.analyzeLorebooks(backup).stream()
                .collect(Collectors.toMap(LorebookImportCandidate::getName, c -> c));
    }

    /**
     * Decisions as the import dialog proposes them, with overrides by lorebook name.
     */
    private Map<String, LorebookDecision> decisions(Map<String, LorebookDecision> overrides) throws Exception {
        Map<String, LorebookDecision> decisions = new HashMap<>();
        for (LorebookImportCandidate candidate : backupService.analyzeLorebooks(backup)) {
            LorebookDecision decision = overrides.getOrDefault(candidate.getName(), candidate.getDecision());
            if (decision != null) {
                decisions.put(candidate.getUuid(), decision);
            }
        }
        return decisions;
    }

    private Map<String, LorebookDecision> proposed() throws Exception {
        return decisions(Map.of());
    }

    private Manuscript copy(Map<String, LorebookDecision> decisions) throws Exception {
        return reload(backupService.cloneBackup(backup, null, decisions));
    }

    private void softDeleteAll() throws Exception {
        for (Lorebook lorebook : List.of(world, magic, shared)) {
            lorebookService.delete(lorebookService.find(lorebook.getId()), false);
        }
    }

    /**
     * Lorebook graph reachable from {@code root} as "name -> sorted subbook names".
     */
    private Map<String, List<String>> structure(Lorebook root) throws Exception {
        Map<String, List<String>> result = new TreeMap<>();
        List<Lorebook> queue = new ArrayList<>(List.of(lorebookService.find(root.getId())));
        Set<Long> seen = new HashSet<>();
        while (!queue.isEmpty()) {
            Lorebook book = queue.removeFirst();
            if (!seen.add(book.getId())) {
                continue;
            }
            List<Lorebook> subbooks = lorebookService.getSubbooks(book);
            result.put(book.getName(), subbooks.stream().map(Lorebook::getName).sorted().toList());
            queue.addAll(subbooks);
        }
        return result;
    }

    private static Map<String, List<String>> originalStructure() {
        return new TreeMap<>(Map.of(
                "World", List.of("Magic", "Shared"),
                "Magic", List.of("Shared"),
                "Shared", List.of()));
    }

    private Lorebook subbook(Lorebook parent, String name) throws Exception {
        return lorebookService.getSubbooks(lorebookService.find(parent.getId())).stream()
                .filter(l -> l.getName().equals(name)).findFirst().orElseThrow();
    }

    private void assertContentCopied(Lorebook copiedWorld) throws Exception {
        assertThat(structure(copiedWorld)).isEqualTo(originalStructure());
        assertThat(tagValues(lorebookService.find(copiedWorld.getId()))).containsExactly("fantasy");
        assertThat(lorebookEntryService.getEntriesForLorebook(copiedWorld)).extracting(LorebookEntry::getName)
                .containsExactly("geography", "history");

        Lorebook copiedMagic = subbook(copiedWorld, "Magic");
        LorebookEntry spells = lorebookEntryService.getEntriesForLorebook(copiedMagic).getFirst();
        assertThat(spells.getName()).isEqualTo("spells");
        assertThat(spells.getPayload()).isEqualTo("payload of spells");
        assertThat(spells.getComment()).isEqualTo("comment");
        assertThat(spells.getOrder()).isEqualTo(7);
        assertThat(spells.getFiltering()).isEqualTo("dragon");
        assertThat(spells.getFilteringMode()).isEqualTo(FilteringMode.REGEX);
        assertThat(spells.getInsertionMode()).isEqualTo(InsertionMode.BEFORE_USER_PROMPT);
        assertThat(spells.isEnabled()).isFalse();
        assertThat(tagValues(spells)).containsExactly("wizard");
        assertThat(tagRelationService.getTagsForObject(spells, true)).extracting(t -> t.getValue()).containsExactly("grimdark");

        // the shared subbook is one lorebook in the copy too
        assertThat(subbook(copiedWorld, "Shared").getId()).isEqualTo(subbook(copiedMagic, "Shared").getId());
    }

    // ------------------------------------------------------------------------------------------------------------
    // analysis
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void analysisFindsExistingLorebooks() throws Exception {
        Map<String, LorebookImportCandidate> candidates = analyze();

        assertThat(candidates.keySet()).containsExactlyInAnyOrder("World", "Magic", "Shared");
        assertThat(candidates.values()).allMatch(c -> c.getMatch() == LorebookMatch.EXISTING && c.getDecision() == LorebookDecision.LINK);
        assertThat(candidates.get("World").isRoot()).isTrue();
        assertThat(candidates.get("Magic").isRoot()).isFalse();
        assertThat(candidates.get("World").getEntryCount()).isEqualTo(2);
        assertThat(candidates.get("Magic").getMatchedLorebook().getId()).isEqualTo(magic.getId());
        assertThat(candidates.get("World").getUuid()).isEqualTo(world.getUuid());
    }

    @Test
    void analysisFindsSameNamedLorebooks() throws Exception {
        softDeleteAll();
        Lorebook newWorld = lorebook(" world ");
        Lorebook newMagic = lorebook("MAGIC");

        Map<String, LorebookImportCandidate> candidates = analyze();

        assertThat(candidates.get("World").getMatch()).isEqualTo(LorebookMatch.SAME_NAME);
        assertThat(candidates.get("World").getDecision()).isEqualTo(LorebookDecision.LINK);
        assertThat(candidates.get("World").getMatchedLorebook().getId()).isEqualTo(newWorld.getId());
        assertThat(candidates.get("Magic").getMatchedLorebook().getId()).isEqualTo(newMagic.getId());
        assertThat(candidates.get("Shared").getMatch()).isEqualTo(LorebookMatch.NOT_FOUND);
        assertThat(candidates.get("Shared").getDecision()).isEqualTo(LorebookDecision.CREATE);
    }

    @Test
    void analysisReportsMissingLorebooks() throws Exception {
        softDeleteAll();

        assertThat(analyze().values()).allMatch(c -> c.getMatch() == LorebookMatch.NOT_FOUND
                && c.getDecision() == LorebookDecision.CREATE && c.getMatchedLorebook() == null);
    }

    @Test
    void analysisIgnoresOtherUsersLorebooks() throws Exception {
        login();

        assertThat(analyze().values()).allMatch(c -> c.getMatch() == LorebookMatch.NOT_FOUND);

        Lorebook own = lorebook("World");
        assertThat(analyze().get("World").getMatch()).isEqualTo(LorebookMatch.SAME_NAME);
        assertThat(analyze().get("World").getMatchedLorebook().getId()).isEqualTo(own.getId());
    }

    // ------------------------------------------------------------------------------------------------------------
    // existing lorebooks
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void existingLorebooksAreLinked() throws Exception {
        List<String> before = lorebookNames();

        Manuscript copy = copy(proposed());

        assertThat(idOf(copy.getLorebook())).isEqualTo(world.getId());
        assertThat(lorebookNames()).isEqualTo(before);
        // relinking doesn't duplicate subbook links
        assertThat(lorebookService.find(world.getId()).getSubbooks()).hasSize(2);
        assertThat(structure(world)).isEqualTo(originalStructure());
    }

    @Test
    void linkedLorebookGetsMissingSubbookLinkBack() throws Exception {
        Lorebook loaded = lorebookService.find(world.getId());
        loaded.getSubbooks().removeIf(l -> l.getId().equals(shared.getId()));
        lorebookService.save(loaded);
        assertThat(structure(world).get("World")).containsExactly("Magic");

        copy(proposed());

        assertThat(structure(world)).isEqualTo(originalStructure());
    }

    @Test
    void linkedLorebookKeepsSubbooksAddedAfterBackup() throws Exception {
        Lorebook extra = lorebook("Extra");
        Lorebook loaded = lorebookService.find(world.getId());
        loaded.getSubbooks().add(extra);
        lorebookService.save(loaded);

        copy(proposed());

        assertThat(structure(world).get("World")).containsExactly("Extra", "Magic", "Shared");
    }

    @Test
    void existingLorebookCanBeCopied() throws Exception {
        Manuscript copy = copy(decisions(Map.of(
                "World", LorebookDecision.CREATE, "Magic", LorebookDecision.CREATE, "Shared", LorebookDecision.CREATE)));

        Lorebook copiedWorld = lorebookService.find(copy.getLorebook().getId());
        assertThat(copiedWorld.getId()).isNotEqualTo(world.getId());
        // the original uuid is taken, the copy gets its own
        assertThat(copiedWorld.getUuid()).isNotEqualTo(world.getUuid());
        assertContentCopied(copiedWorld);
        assertThat(structure(world)).isEqualTo(originalStructure());
        assertThat(lorebookNames()).hasSize(6);
    }

    @Test
    void existingLorebookIsLinkedEvenWhenSkipped() throws Exception {
        Manuscript copy = copy(decisions(Map.of("World", LorebookDecision.SKIP)));

        assertThat(idOf(copy.getLorebook())).isEqualTo(world.getId());
    }

    @Test
    void existingLorebookIsLinkedWithoutDecision() throws Exception {
        Manuscript copy = copy(Map.of());

        assertThat(idOf(copy.getLorebook())).isEqualTo(world.getId());
    }

    @Test
    void copiedSubbookIsLinkedUnderLinkedRoot() throws Exception {
        Manuscript copy = copy(decisions(Map.of("Magic", LorebookDecision.CREATE)));

        assertThat(idOf(copy.getLorebook())).isEqualTo(world.getId());
        Lorebook copiedMagic = lorebookService.findAllForUser().stream()
                .filter(l -> l.getName().equals("Magic") && !l.getId().equals(magic.getId())).findFirst().orElseThrow();
        // the copy is attached next to the original Magic, which stays linked
        assertThat(lorebookService.getSubbooks(lorebookService.find(world.getId()))).extracting(Lorebook::getId)
                .containsExactlyInAnyOrder(magic.getId(), copiedMagic.getId(), shared.getId());
        assertThat(subbook(copiedMagic, "Shared").getId()).isEqualTo(shared.getId());
        assertThat(subbook(magic, "Shared").getId()).isEqualTo(shared.getId());
    }

    // ------------------------------------------------------------------------------------------------------------
    // same name
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void sameNamedLorebookIsLinkedInsteadOfMissingOne() throws Exception {
        softDeleteAll();
        Lorebook newWorld = lorebook("World");

        Manuscript copy = copy(proposed());

        assertThat(idOf(copy.getLorebook())).isEqualTo(newWorld.getId());
        // missing subbooks are created and linked under the same-named lorebook
        assertThat(structure(newWorld)).isEqualTo(originalStructure());
        assertThat(lorebookNames()).containsExactly("Magic", "Shared", "World");
    }

    @Test
    void sameNamedLorebookCanBeIgnoredInFavourOfCopy() throws Exception {
        softDeleteAll();
        Lorebook newWorld = lorebook("World");

        Manuscript copy = copy(decisions(Map.of("World", LorebookDecision.CREATE)));

        assertThat(idOf(copy.getLorebook())).isNotEqualTo(newWorld.getId());
        assertContentCopied(lorebookService.find(copy.getLorebook().getId()));
    }

    @Test
    void sameNamedLorebookOnRestore() throws Exception {
        softDeleteAll();
        Lorebook newWorld = lorebook("World");
        Manuscript target = manuscript("Target", null, null, null);

        Manuscript restored = reload(backupService.applyBackup(target, backup, false, proposed()));

        assertThat(idOf(restored.getLorebook())).isEqualTo(newWorld.getId());
        assertThat(structure(newWorld)).isEqualTo(originalStructure());
    }

    @Test
    void sameNamedSubbooksAreLinkedTogether() throws Exception {
        softDeleteAll();
        Lorebook newWorld = lorebook("World");
        Lorebook newMagic = lorebook("Magic");
        Lorebook newShared = lorebook("Shared");

        copy(proposed());

        assertThat(lorebookNames()).containsExactly("Magic", "Shared", "World");
        assertThat(structure(newWorld)).isEqualTo(originalStructure());
        assertThat(subbook(newWorld, "Magic").getId()).isEqualTo(newMagic.getId());
        assertThat(subbook(newMagic, "Shared").getId()).isEqualTo(newShared.getId());
    }

    // ------------------------------------------------------------------------------------------------------------
    // not found
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void missingLorebooksAreCreated() throws Exception {
        softDeleteAll();

        Manuscript copy = copy(proposed());

        Lorebook created = lorebookService.find(copy.getLorebook().getId());
        assertThat(created.getId()).isNotIn(world.getId(), magic.getId(), shared.getId());
        assertContentCopied(created);
    }

    @Test
    void createdLorebookKeepsOriginalUuidWhenFree() throws Exception {
        // a backup from another installation: its lorebook uuids don't exist here at all
        JsonObject json = manuscriptService.createBackup(original);
        Map<String, String> renamed = new HashMap<>();
        for (JsonElement element : json.getAsJsonArray("lorebooks")) {
            JsonObject book = element.getAsJsonObject();
            String fresh = UUID.randomUUID().toString();
            renamed.put(book.get("uuid").getAsString(), fresh);
        }
        String text = json.toString();
        for (Map.Entry<String, String> e : renamed.entrySet()) {
            text = text.replace(e.getKey(), e.getValue());
        }
        softDeleteAll();
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        Map<String, LorebookDecision> decisions = new HashMap<>();
        backupService.analyzeLorebooks(data).forEach(c -> decisions.put(c.getUuid(), c.getDecision()));

        Manuscript restored = reload(backupService.restoreAsNewManuscript(data, null, decisions));

        Lorebook created = lorebookService.find(restored.getLorebook().getId());
        assertThat(created.getUuid()).isEqualTo(renamed.get(world.getUuid()));
        assertContentCopied(created);
    }

    @Test
    void skippedMissingRootLeavesCopyWithoutLorebook() throws Exception {
        softDeleteAll();

        Manuscript copy = copy(decisions(Map.of(
                "World", LorebookDecision.SKIP, "Magic", LorebookDecision.SKIP, "Shared", LorebookDecision.SKIP)));

        assertThat(copy.getLorebook()).isNull();
        assertThat(lorebookNames()).isEmpty();
        assertThat(tree(copy)).isEqualTo(storyTree());
    }

    @Test
    void missingDecisionForMissingLorebookImportsNothing() throws Exception {
        softDeleteAll();

        Manuscript copy = copy(Map.of());

        assertThat(copy.getLorebook()).isNull();
        assertThat(lorebookNames()).isEmpty();
    }

    @Test
    void skippedMissingRootKeepsCurrentLorebookOnRestore() throws Exception {
        softDeleteAll();
        Lorebook current = lorebook("Current");
        Manuscript target = manuscript("Target", null, null, current);

        Manuscript restored = reload(backupService.applyBackup(target, backup, false,
                decisions(Map.of("World", LorebookDecision.SKIP))));

        assertThat(idOf(restored.getLorebook())).isEqualTo(current.getId());
    }

    @Test
    void createdSubbookIsWiredUnderCreatedRoot() throws Exception {
        softDeleteAll();
        Lorebook newShared = lorebook("Shared");

        Manuscript copy = copy(proposed());

        Lorebook createdWorld = lorebookService.find(copy.getLorebook().getId());
        // World and Magic are created, the same-named Shared is linked into both
        assertThat(structure(createdWorld)).isEqualTo(originalStructure());
        assertThat(subbook(createdWorld, "Shared").getId()).isEqualTo(newShared.getId());
        assertThat(subbook(subbook(createdWorld, "Magic"), "Shared").getId()).isEqualTo(newShared.getId());
    }

    @Test
    void skippedSubbookIsLeftOut() throws Exception {
        softDeleteAll();

        Manuscript copy = copy(decisions(Map.of("Magic", LorebookDecision.SKIP)));

        assertThat(structure(lorebookService.find(copy.getLorebook().getId())))
                .isEqualTo(Map.of("World", List.of("Shared"), "Shared", List.of()));
    }

    @Test
    void anotherUserGetsCopiesWithNewUuids() throws Exception {
        login();

        Manuscript copy = copy(proposed());

        Lorebook created = lorebookService.find(copy.getLorebook().getId());
        assertThat(created.getOwner().getId()).isEqualTo(currentUser.getId());
        assertThat(created.getUuid()).isNotEqualTo(world.getUuid());
        assertContentCopied(created);
        // the original owner's lorebook is untouched
        assertThat(lorebookService.find(world.getId()).getOwner().getId()).isNotEqualTo(currentUser.getId());
    }

    @Test
    void cyclicLorebooksAreRecreated() throws Exception {
        Lorebook a = lorebook("A");
        Lorebook b = lorebook("B", a);
        Lorebook loadedA = lorebookService.find(a.getId());
        loadedA.getSubbooks().add(b);
        lorebookService.save(loadedA);
        backup = backupService.takeBackup(manuscript("Cyclic", null, null, a));
        lorebookService.delete(lorebookService.find(a.getId()), false);
        lorebookService.delete(lorebookService.find(b.getId()), false);

        Manuscript copy = copy(proposed());

        assertThat(structure(lorebookService.find(copy.getLorebook().getId())))
                .isEqualTo(Map.of("A", List.of("B"), "B", List.of("A")));
    }

    // ------------------------------------------------------------------------------------------------------------
    // without decisions / old backups / messages only
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void withoutDecisionsOnlyUuidLinkIsUsed() throws Exception {
        byte[] data = backupService.serializeBackup(backup).getBytes(StandardCharsets.UTF_8);

        assertThat(idOf(reload(backupService.restoreAsNewManuscript(data, null, null)).getLorebook())).isEqualTo(world.getId());

        softDeleteAll();
        lorebook("World");
        List<String> before = lorebookNames();
        assertThat(reload(backupService.restoreAsNewManuscript(data, null, null)).getLorebook()).isNull();
        assertThat(lorebookNames()).isEqualTo(before);
    }

    @Test
    void oldBackupWithoutLorebookListLinksByUuidOnly() throws Exception {
        JsonObject json = manuscriptService.createBackup(original);
        json.remove("lorebooks");
        byte[] data = json.toString().getBytes(StandardCharsets.UTF_8);

        assertThat(backupService.analyzeLorebooks(data)).isEmpty();
        assertThat(idOf(reload(backupService.restoreAsNewManuscript(data, null, Map.of())).getLorebook())).isEqualTo(world.getId());

        softDeleteAll();
        assertThat(reload(backupService.restoreAsNewManuscript(data, null, Map.of())).getLorebook()).isNull();
    }

    @Test
    void messagesOnlyRestoreIgnoresLorebooks() throws Exception {
        softDeleteAll();
        Lorebook current = lorebook("Current");
        Manuscript target = manuscript("Target", null, null, current);

        Manuscript restored = reload(backupService.applyBackup(target, backup, true, proposed()));

        assertThat(idOf(restored.getLorebook())).isEqualTo(current.getId());
        assertThat(lorebookNames()).containsExactly("Current");
        assertThat(tree(restored)).isEqualTo(storyTree());
    }

    @Test
    void lorebookChangesAfterBackupAreNotRolledBackByLinking() throws Exception {
        LorebookEntry added = entry(world, "added later", e -> e.setOrder(9));

        Manuscript copy = copy(proposed());

        assertThat(lorebookEntryService.getEntriesForLorebook(copy.getLorebook().getId()))
                .extracting(LorebookEntry::getId).contains(added.getId());
    }
}
