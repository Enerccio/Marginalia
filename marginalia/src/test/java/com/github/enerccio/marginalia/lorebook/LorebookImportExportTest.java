package com.github.enerccio.marginalia.lorebook;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.domain.service.impl.LorebookServiceImpl;
import com.github.enerccio.marginalia.test.ExpectedLog;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Lorebook file export and import: Marginalia format version 2 (lorebook with all sub-lorebooks), version 1 (single
 * lorebook) and SillyTavern world info.
 * <p>
 * Exported tree: World (tag "fantasy") -> Magic, Shared; Magic -> Shared, Magic -> World (cycle).
 */
class LorebookImportExportTest extends MarginaliaTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    private User owner;
    private Lorebook world;
    private Lorebook magic;
    private Lorebook shared;

    @BeforeEach
    void createTree() throws Exception {
        owner = login();
        shared = lorebook("Shared");
        entry(shared, "harbour", e -> e.setOrder(3));
        magic = lorebook("Magic", shared);
        LorebookEntry spells = entry(magic, "spells", e -> {
            e.setComment("comment with \"quotes\" and ünïcödé");
            e.setPayload("{{user}} casts a spell.\nSecond line.");
            e.setOrder(7);
            e.setFiltering("\\bspell(s)?\\b");
            e.setFilteringMode(FilteringMode.REGEX);
            e.setInsertionMode(InsertionMode.BEFORE_USER_PROMPT);
            e.setEnabled(false);
        });
        tagRelationService.createRelation(tagService.getOrCreateForUser("wizard"), spells);
        tagRelationService.createRelation(tagService.getOrCreateForUser("arcane"), spells);
        tagRelationService.createRelation(tagService.getOrCreateForUser("grimdark"), spells, true);
        lorebookEntryService.delete(entry(magic, "deleted entry", e -> e.setOrder(1)), false);
        world = lorebook("World", magic, shared);
        tagRelationService.createRelation(tagService.getOrCreateForUser("fantasy"), world);
        entry(world, "geography", e -> e.setOrder(1));
        entry(world, "history", e -> {
            e.setOrder(2);
            e.setFiltering("king");
        });
        Lorebook loadedMagic = lorebookService.find(magic.getId());
        loadedMagic.getSubbooks().add(world);
        lorebookService.save(loadedMagic);
    }

    // ------------------------------------------------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------------------------------------------------

    private Lorebook lorebook(String name, Lorebook... subbooks) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(name);
        lorebook.setSubbooks(new ArrayList<>(List.of(subbooks)));
        return lorebookService.save(lorebook);
    }

    private LorebookEntry entry(Lorebook lorebook, String name, Consumer<LorebookEntry> setup) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        entry.setPayload("payload of " + name);
        setup.accept(entry);
        return lorebookEntryService.save(entry);
    }

    private List<String> tags(Object entity, boolean negative) throws Exception {
        List<Tag> tags = entity instanceof LorebookEntry e ? tagRelationService.getTagsForObject(e, negative)
                : tagRelationService.getTagsForObject((Lorebook) entity, negative);
        return tags.stream().map(Tag::getValue).sorted().toList();
    }

    private Lorebook subbook(Lorebook parent, String name) throws Exception {
        return lorebookService.getSubbooks(lorebookService.find(parent.getId())).stream()
                .filter(l -> l.getName().equals(name)).findFirst().orElseThrow();
    }

    private LorebookEntry entryNamed(Lorebook lorebook, String name) throws Exception {
        return lorebookEntryService.getEntriesForLorebook(lorebook).stream()
                .filter(e -> e.getName().equals(name)).findFirst().orElseThrow();
    }

    private static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }

    /**
     * Export without identities: lorebooks keyed by name, subbook references by name, sorted where order is not
     * meaningful - two exports of equal content compare equal.
     */
    private static Map<String, JsonObject> normalized(String export) {
        JsonObject root = json(export);
        Map<String, String> names = new HashMap<>();
        for (JsonElement element : root.getAsJsonArray("lorebooks")) {
            JsonObject book = element.getAsJsonObject();
            names.put(book.get("uuid").getAsString(), book.get("name").getAsString());
        }
        Map<String, JsonObject> result = new TreeMap<>();
        for (JsonElement element : root.getAsJsonArray("lorebooks")) {
            JsonObject book = element.getAsJsonObject().deepCopy();
            book.remove("uuid");
            JsonArray subbooks = new JsonArray();
            book.getAsJsonArray("subbooks").asList().stream().map(s -> names.get(s.getAsString())).sorted().forEach(subbooks::add);
            book.add("subbooks", subbooks);
            book.add("tags", sorted(book.getAsJsonArray("tags")));
            for (JsonElement entry : book.getAsJsonArray("entries")) {
                JsonObject entryObj = entry.getAsJsonObject();
                entryObj.add("tags", sorted(entryObj.getAsJsonArray("tags")));
                entryObj.add("negativeTags", sorted(entryObj.getAsJsonArray("negativeTags")));
            }
            result.put(book.get("name").getAsString(), book);
        }
        return result;
    }

    private static JsonArray sorted(JsonArray array) {
        JsonArray result = new JsonArray();
        array.asList().stream().map(JsonElement::getAsString).sorted().forEach(result::add);
        return result;
    }

    private Set<Long> allLorebookIds() throws Exception {
        Set<Long> ids = new HashSet<>();
        lorebookService.findAllForUser().forEach(l -> ids.add(l.getId()));
        return ids;
    }

    // ------------------------------------------------------------------------------------------------------------
    // export
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void exportContainsWholeTree() throws Exception {
        JsonObject export = json(lorebookService.exportLorebook(world));

        assertThat(export.get("format").getAsString()).isEqualTo(LorebookServiceImpl.EXPORT_FORMAT);
        assertThat(export.get("version").getAsInt()).isEqualTo(2);
        assertThat(export.get("root").getAsString()).isEqualTo(world.getUuid());
        JsonArray lorebooks = export.getAsJsonArray("lorebooks");
        assertThat(lorebooks).hasSize(3);
        assertThat(lorebooks.get(0).getAsJsonObject().get("uuid").getAsString()).as("root first").isEqualTo(world.getUuid());

        Map<String, JsonObject> books = new HashMap<>();
        lorebooks.forEach(b -> books.put(b.getAsJsonObject().get("name").getAsString(), b.getAsJsonObject()));
        assertThat(books.get("World").getAsJsonArray("subbooks").asList()).extracting(JsonElement::getAsString)
                .containsExactlyInAnyOrder(magic.getUuid(), shared.getUuid());
        assertThat(books.get("Magic").getAsJsonArray("subbooks").asList()).extracting(JsonElement::getAsString)
                .containsExactlyInAnyOrder(shared.getUuid(), world.getUuid());
        assertThat(books.get("World").getAsJsonArray("tags").asList()).extracting(JsonElement::getAsString).containsExactly("fantasy");
        assertThat(books.get("World").get("enabled").getAsBoolean()).isTrue();
    }

    @Test
    void exportContainsEntryFields() throws Exception {
        Map<String, JsonObject> books = normalized(lorebookService.exportLorebook(world));

        JsonArray magicEntries = books.get("Magic").getAsJsonArray("entries");
        assertThat(magicEntries).as("deleted entries are not exported").hasSize(1);
        JsonObject spells = magicEntries.get(0).getAsJsonObject();
        assertThat(spells.get("name").getAsString()).isEqualTo("spells");
        assertThat(spells.get("payload").getAsString()).isEqualTo("{{user}} casts a spell.\nSecond line.");
        assertThat(spells.get("comment").getAsString()).isEqualTo("comment with \"quotes\" and ünïcödé");
        assertThat(spells.get("enabled").getAsBoolean()).isFalse();
        assertThat(spells.get("order").getAsInt()).isEqualTo(7);
        assertThat(spells.get("filtering").getAsString()).isEqualTo("\\bspell(s)?\\b");
        assertThat(spells.get("filteringMode").getAsString()).isEqualTo("REGEX");
        assertThat(spells.get("insertionMode").getAsString()).isEqualTo("BEFORE_USER_PROMPT");
        assertThat(spells.getAsJsonArray("tags").asList()).extracting(JsonElement::getAsString).containsExactly("arcane", "wizard");
        assertThat(spells.getAsJsonArray("negativeTags").asList()).extracting(JsonElement::getAsString).containsExactly("grimdark");

        assertThat(books.get("World").getAsJsonArray("entries").asList()).extracting(e -> e.getAsJsonObject().get("name").getAsString())
                .containsExactly("geography", "history");
    }

    @Test
    void exportOfSubbookStartsThere() throws Exception {
        JsonObject export = json(lorebookService.exportLorebook(shared));

        assertThat(export.get("root").getAsString()).isEqualTo(shared.getUuid());
        assertThat(export.getAsJsonArray("lorebooks")).hasSize(1);
    }

    @Test
    void exportSkipsDeletedSubbooks() throws Exception {
        lorebookService.delete(lorebookService.find(shared.getId()), false);

        assertThat(normalized(lorebookService.exportLorebook(world)).keySet()).containsExactly("Magic", "World");
    }

    @Test
    void exportOfUnknownLorebookFails() {
        assertThatThrownBy(() -> lorebookService.exportLorebook(new Lorebook())).isInstanceOf(IllegalArgumentException.class);
    }

    // ------------------------------------------------------------------------------------------------------------
    // import (version 2)
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void roundTripRecreatesIdenticalContent() throws Exception {
        String export = lorebookService.exportLorebook(world);

        Lorebook imported = lorebookService.importLorebook(export, "ignored.json");

        assertThat(imported.getName()).isEqualTo("World");
        assertThat(normalized(lorebookService.exportLorebook(imported))).isEqualTo(normalized(export));
    }

    @Test
    void importAlwaysCreatesNewLorebooks() throws Exception {
        Set<Long> before = allLorebookIds();
        String export = lorebookService.exportLorebook(world);

        Lorebook imported = lorebookService.importLorebook(export, null);

        Set<Long> created = allLorebookIds();
        created.removeAll(before);
        assertThat(created).hasSize(3).contains(imported.getId());
        // the uuids are taken by the originals, so the copies get new ones
        assertThat(imported.getUuid()).isNotEqualTo(world.getUuid());
        assertThat(subbook(imported, "Magic").getId()).isNotEqualTo(magic.getId());
        assertThat(subbook(subbook(imported, "Magic"), "World").getId()).as("cycle points to the copy").isEqualTo(imported.getId());
        assertThat(subbook(imported, "Shared").getId()).isEqualTo(subbook(subbook(imported, "Magic"), "Shared").getId());
        // the originals are untouched
        assertThat(normalized(lorebookService.exportLorebook(world))).isEqualTo(normalized(export));
    }

    @Test
    void importedEntriesHaveAllFieldsAndTags() throws Exception {
        Lorebook imported = lorebookService.importLorebook(lorebookService.exportLorebook(world), null);

        LorebookEntry spells = entryNamed(subbook(imported, "Magic"), "spells");
        assertThat(spells.getPayload()).isEqualTo("{{user}} casts a spell.\nSecond line.");
        assertThat(spells.getComment()).isEqualTo("comment with \"quotes\" and ünïcödé");
        assertThat(spells.isEnabled()).isFalse();
        assertThat(spells.getOrder()).isEqualTo(7);
        assertThat(spells.getFiltering()).isEqualTo("\\bspell(s)?\\b");
        assertThat(spells.getFilteringMode()).isEqualTo(FilteringMode.REGEX);
        assertThat(spells.getInsertionMode()).isEqualTo(InsertionMode.BEFORE_USER_PROMPT);
        assertThat(tags(spells, false)).containsExactly("arcane", "wizard");
        assertThat(tags(spells, true)).containsExactly("grimdark");
        assertThat(tags(lorebookService.find(imported.getId()), false)).containsExactly("fantasy");
        assertThat(lorebookEntryService.getEntriesForLorebook(subbook(imported, "Magic"))).hasSize(1);
        // existing tags are reused, not duplicated
        assertThat(tagService.searchTagsForUser("wizard", 0, 10)).hasSize(1);
    }

    @Test
    void disabledLorebookStaysDisabled() throws Exception {
        Lorebook loaded = lorebookService.find(shared.getId());
        loaded.setEnabled(false);
        lorebookService.save(loaded);

        Lorebook imported = lorebookService.importLorebook(lorebookService.exportLorebook(world), null);

        assertThat(subbook(imported, "Shared").isEnabled()).isFalse();
        assertThat(lorebookService.find(imported.getId()).isEnabled()).isTrue();
    }

    @Test
    void importingTwiceGivesTwoIndependentCopies() throws Exception {
        String export = lorebookService.exportLorebook(world);

        Lorebook first = lorebookService.importLorebook(export, null);
        Lorebook second = lorebookService.importLorebook(export, null);

        assertThat(second.getId()).isNotEqualTo(first.getId());
        assertThat(subbook(second, "Shared").getId()).isNotEqualTo(subbook(first, "Shared").getId());
        assertThat(normalized(lorebookService.exportLorebook(second))).isEqualTo(normalized(lorebookService.exportLorebook(first)));
    }

    @Test
    void importKeepsUuidsFromAnotherInstallation() throws Exception {
        String export = lorebookService.exportLorebook(world);
        Map<String, String> fresh = new HashMap<>();
        for (Lorebook book : List.of(world, magic, shared)) {
            fresh.put(book.getUuid(), UUID.randomUUID().toString());
        }
        for (Map.Entry<String, String> e : fresh.entrySet()) {
            export = export.replace(e.getKey(), e.getValue());
        }

        Lorebook imported = lorebookService.importLorebook(export, null);

        assertThat(imported.getUuid()).isEqualTo(fresh.get(world.getUuid()));
        assertThat(subbook(imported, "Magic").getUuid()).isEqualTo(fresh.get(magic.getUuid()));
        assertThat(normalized(lorebookService.exportLorebook(imported))).isEqualTo(normalized(export));
    }

    @Test
    void anotherUserImportsIntoOwnAccount() throws Exception {
        String export = lorebookService.exportLorebook(world);
        User other = login();

        Lorebook imported = lorebookService.importLorebook(export, null);

        assertThat(lorebookService.find(imported.getId()).getOwner().getId()).isEqualTo(other.getId());
        assertThat(lorebookService.findAllForUser()).hasSize(3);
        assertThat(normalized(lorebookService.exportLorebook(imported))).isEqualTo(normalized(export));
        // tags are the importing user's own
        LorebookEntry spells = entryNamed(subbook(imported, "Magic"), "spells");
        assertThat(tagRelationService.getTagsForObject(spells)).allMatch(t -> t.getOwner().getId().equals(other.getId()));
        assertThat(lorebookService.find(world.getId()).getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    void versionTwoUsesNamesFromFile() throws Exception {
        Lorebook imported = lorebookService.importLorebook(lorebookService.exportLorebook(world), "My upload.json");

        assertThat(imported.getName()).isEqualTo("World");
    }

    @Test
    void missingRootIsRejected() throws Exception {
        JsonObject export = json(lorebookService.exportLorebook(world));
        export.addProperty("root", "not-in-file");

        assertThatThrownBy(() -> lorebookService.importLorebook(export.toString(), null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Root");
    }

    @Test
    void danglingSubbookReferenceIsIgnored() throws Exception {
        JsonObject export = json(lorebookService.exportLorebook(shared));
        export.getAsJsonArray("lorebooks").get(0).getAsJsonObject().getAsJsonArray("subbooks").add("missing-uuid");

        Lorebook imported = lorebookService.importLorebook(export.toString(), null);

        assertThat(lorebookService.getSubbooks(imported)).isEmpty();
    }

    @Test
    void unknownModesFallBackToDefaults() throws Exception {
        JsonObject export = json(lorebookService.exportLorebook(magic));
        JsonObject spells = null;
        for (JsonElement book : export.getAsJsonArray("lorebooks")) {
            if (book.getAsJsonObject().get("name").getAsString().equals("Magic")) {
                spells = book.getAsJsonObject().getAsJsonArray("entries").get(0).getAsJsonObject();
            }
        }
        spells.addProperty("filteringMode", "SEMANTIC");
        spells.addProperty("insertionMode", "AT_DEPTH_4");

        Lorebook imported;
        try (ExpectedLog log = ExpectedLog.capture(LorebookServiceImpl.class)) {
            imported = lorebookService.importLorebook(export.toString(), null);
            assertThat(log.warnings()).hasSize(2);
        }

        LorebookEntry entry = entryNamed(imported, "spells");
        assertThat(entry.getFilteringMode()).isEqualTo(FilteringMode.TEXT);
        assertThat(entry.getInsertionMode()).isEqualTo(InsertionMode.IN_LORE_BLOCK);
        assertThat(entry.getFiltering()).isEqualTo("\\bspell(s)?\\b");
    }

    @Test
    void minimalEntriesGetDefaults() throws Exception {
        String file = """
                {"format": "marginalia-lorebook", "version": 2, "root": "r",
                 "lorebooks": [{"uuid": "r", "name": "", "entries": [{}, {"name": "named", "payload": "text"}]}]}
                """;

        Lorebook imported = lorebookService.importLorebook(file, null);

        assertThat(imported.getName()).isEqualTo("Imported Lorebook");
        assertThat(imported.isEnabled()).isTrue();
        List<LorebookEntry> entries = lorebookEntryService.getEntriesForLorebook(imported);
        assertThat(entries).extracting(LorebookEntry::getName).containsExactlyInAnyOrder("Entry", "named");
        assertThat(entries).allMatch(e -> e.isEnabled() && e.getOrder() == 100
                && e.getFilteringMode() == FilteringMode.TEXT && e.getInsertionMode() == InsertionMode.IN_LORE_BLOCK);
    }

    // ------------------------------------------------------------------------------------------------------------
    // import (version 1)
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void versionOneSingleLorebook() throws Exception {
        String file = """
                {"format": "marginalia-lorebook", "version": 1, "name": "Old book", "enabled": false,
                 "tags": ["legacy"],
                 "entries": [
                   {"name": "first", "payload": "one", "order": 5, "filtering": "x", "filteringMode": "REGEX",
                    "insertionMode": "BEFORE_USER_PROMPT", "tags": ["a"], "negativeTags": ["b"], "enabled": false},
                   {"name": "second", "payload": "two"}
                 ]}
                """;

        Lorebook imported = lorebookService.importLorebook(file, "file name.json");

        assertThat(imported.getName()).isEqualTo("Old book");
        assertThat(imported.isEnabled()).isFalse();
        assertThat(tags(lorebookService.find(imported.getId()), false)).containsExactly("legacy");
        assertThat(lorebookService.getSubbooks(imported)).isEmpty();
        LorebookEntry first = entryNamed(imported, "first");
        assertThat(first.getPayload()).isEqualTo("one");
        assertThat(first.getOrder()).isEqualTo(5);
        assertThat(first.getFilteringMode()).isEqualTo(FilteringMode.REGEX);
        assertThat(first.getInsertionMode()).isEqualTo(InsertionMode.BEFORE_USER_PROMPT);
        assertThat(first.isEnabled()).isFalse();
        assertThat(tags(first, false)).containsExactly("a");
        assertThat(tags(first, true)).containsExactly("b");
        assertThat(entryNamed(imported, "second").isEnabled()).isTrue();
    }

    @Test
    void versionOneWithoutNameUsesFileNameOrDefault() throws Exception {
        String file = "{\"format\": \"marginalia-lorebook\", \"version\": 1, \"entries\": []}";

        assertThat(lorebookService.importLorebook(file, "uploaded").getName()).isEqualTo("uploaded");
        assertThat(lorebookService.importLorebook(file, " ").getName()).isEqualTo("Imported Lorebook");
    }

    @Test
    void invalidFilesAreRejected() throws Exception {
        assertThatThrownBy(() -> lorebookService.importLorebook("  ", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> lorebookService.importLorebook("{\"entries\": {}}", null))
                .as("SillyTavern file in the Marginalia importer").isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> lorebookService.importLorebook("{\"format\": \"other\"}", null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> lorebookService.importLorebook("not json", null)).isInstanceOf(RuntimeException.class);
        assertThat(lorebookService.findAllForUser()).hasSize(3);
    }

    // ------------------------------------------------------------------------------------------------------------
    // SillyTavern
    // ------------------------------------------------------------------------------------------------------------

    private String resource(String name) throws Exception {
        try (InputStream in = getClass().getResourceAsStream(name)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void sillyTavernWorldInfo() throws Exception {
        String file = resource("/macro-test.json");

        Lorebook imported = lorebookService.importFromSillytavern(file, "macro-test.json");

        // the world info name stored in the file wins over the file name
        assertThat(imported.getName()).isEqualTo("Macro Test");
        assertThat(imported.isEnabled()).isTrue();
        List<LorebookEntry> entries = lorebookEntryService.getEntriesForLorebook(imported);
        assertThat(entries).hasSize(14);
        assertThat(entries).extracting(LorebookEntry::getName).first().isEqualTo("MacroTest 01 - setup");
        LorebookEntry disabled = entries.stream().filter(e -> e.getName().contains("DISABLED")).findFirst().orElseThrow();
        assertThat(disabled.isEnabled()).isFalse();
        assertThat(entries).filteredOn(LorebookEntry::isEnabled).hasSize(13);

        JsonObject source = json(file).getAsJsonObject("entries").getAsJsonObject("1");
        LorebookEntry names = entryNamed(imported, "MacroTest 02 - names");
        assertThat(names.getPayload()).isEqualTo(source.get("content").getAsString());
        assertThat(names.getComment()).isNull();
        assertThat(names.getOrder()).isEqualTo(20);
        // no keys: always active, like the lorebook was written for
        assertThat(entries).allMatch(e -> e.getFiltering() == null);
    }

    @Test
    void sillyTavernKeysBecomeFilters() throws Exception {
        String file = """
                {"entries": {
                  "0": {"uid": 0, "comment": "dragons", "content": "Dragons fly.", "key": ["dragon"], "order": 50},
                  "1": {"uid": 1, "comment": "any key", "content": "x", "key": "king, queen ,"},
                  "2": {"uid": 2, "comment": "with secondary", "content": "y", "key": ["king"],
                        "keysecondary": ["crown"], "selective": true, "selectiveLogic": 3},
                  "3": {"uid": 3, "comment": "constant", "content": "z", "key": ["never"], "constant": true},
                  "4": {"uid": 4, "comment": "", "content": "w", "key": ["/drag(on|ons)/i"]}
                }}
                """;

        Lorebook imported = lorebookService.importFromSillytavern(file, "world");

        LorebookEntry dragons = entryNamed(imported, "dragons");
        assertThat(dragons.getFiltering()).isEqualTo("dragon");
        assertThat(dragons.getFilteringMode()).isEqualTo(FilteringMode.TEXT);
        assertThat(dragons.getOrder()).isEqualTo(50);
        assertThat(entryNamed(imported, "any key").getFilteringMode()).isEqualTo(FilteringMode.REGEX);
        assertThat(entryNamed(imported, "with secondary").getFilteringMode()).isEqualTo(FilteringMode.REGEX);
        assertThat(entryNamed(imported, "constant").getFiltering()).isNull();
        // no comment: named after the first key
        assertThat(entryNamed(imported, "/drag(on|ons)/i").getFilteringMode()).isEqualTo(FilteringMode.REGEX);
        // keys are no longer turned into tags
        for (LorebookEntry entry : lorebookEntryService.getEntriesForLorebook(imported)) {
            assertThat(tags(entry, false)).as(entry.getName()).isEmpty();
        }
        assertThat(tagService.searchTagsForUser("dragon", 0, 10)).isEmpty();
    }

    @Test
    void sillyTavernCharacterFilterBecomesTags() throws Exception {
        String file = """
                {"entries": {
                  "0": {"uid": 0, "comment": "excluded chars", "content": "y",
                        "characterFilter": {"isExclude": true, "names": ["Bob"], "tags": ["villain"]}},
                  "1": {"uid": 1, "comment": "included chars", "content": "z",
                        "characterFilter": {"isExclude": false, "names": ["Alice"]}},
                  "2": {"uid": 2, "comment": "again", "content": "z", "characterFilter": {"names": ["Alice"]}}
                }}
                """;

        Lorebook imported = lorebookService.importFromSillytavern(file, "world");

        assertThat(tags(entryNamed(imported, "excluded chars"), true)).containsExactly("Bob", "villain");
        assertThat(tags(entryNamed(imported, "excluded chars"), false)).isEmpty();
        assertThat(tags(entryNamed(imported, "included chars"), false)).containsExactly("Alice");
        assertThat(tagService.searchTagsForUser("Alice", 0, 10)).hasSize(1);
    }

    @Test
    void sillyTavernPositionAndUnsupportedSettings() throws Exception {
        String file = """
                {"entries": {
                  "0": {"uid": 0, "comment": "near prompt", "content": "a", "position": 4, "depth": 2, "probability": 50},
                  "1": {"uid": 1, "comment": "background", "content": "b", "position": 0}
                }}
                """;

        Lorebook imported = lorebookService.importFromSillytavern(file, "world");

        LorebookEntry near = entryNamed(imported, "near prompt");
        assertThat(near.getInsertionMode()).isEqualTo(InsertionMode.BEFORE_USER_PROMPT);
        assertThat(near.getComment()).contains("depth 2").contains("probability 50%");
        LorebookEntry background = entryNamed(imported, "background");
        assertThat(background.getInsertionMode()).isEqualTo(InsertionMode.IN_LORE_BLOCK);
        assertThat(background.getComment()).isNull();
    }

    @Test
    void sillyTavernEntriesAsArraySortedByUid() throws Exception {
        String file = """
                {"entries": [
                  {"uid": 2, "comment": "third", "content": "c", "order": 1},
                  {"uid": 0, "comment": "first", "content": "a", "order": 1},
                  {"uid": 1, "comment": "", "content": "b", "order": 1}
                ]}
                """;

        Lorebook imported = lorebookService.importFromSillytavern(file, null);

        assertThat(imported.getName()).isEqualTo("Imported Lorebook");
        // same order, so creation order (= uid order) decides
        List<LorebookEntry> entries = lorebookEntryService.getEntriesForLorebook(imported);
        assertThat(entries).extracting(LorebookEntry::getPayload).containsExactlyInAnyOrder("a", "b", "c");
        assertThat(entryNamed(imported, "Entry").getPayload()).isEqualTo("b");
        assertThat(entries.stream().sorted((x, y) -> Long.compare(x.getId(), y.getId())).map(LorebookEntry::getPayload))
                .containsExactly("a", "b", "c");
    }

    @Test
    void sillyTavernDefaults() throws Exception {
        Lorebook imported = lorebookService.importFromSillytavern("{\"entries\": {\"0\": {}}}", "bare.JSON");

        assertThat(imported.getName()).isEqualTo("bare");
        LorebookEntry entry = lorebookEntryService.getEntriesForLorebook(imported).getFirst();
        assertThat(entry.getName()).isEqualTo("Entry");
        assertThat(entry.getPayload()).isEmpty();
        assertThat(entry.getOrder()).isEqualTo(100);
        assertThat(entry.isEnabled()).isTrue();
        assertThat(entry.getFiltering()).isNull();
    }

    @Test
    void sillyTavernWithoutEntriesIsRejected() throws Exception {
        String name = uniqueName("no-entries");

        assertThatThrownBy(() -> lorebookService.importFromSillytavern("{\"name\": \"" + name + "\"}", "empty"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(lorebookService.findAllForUser()).extracting(Lorebook::getName).doesNotContain(name);
    }

    @Test
    void sillyTavernImportCanBeExportedAndReimported() throws Exception {
        Lorebook imported = lorebookService.importFromSillytavern(resource("/macro-test.json"), "macro-test.json");
        String export = lorebookService.exportLorebook(imported);

        Lorebook again = lorebookService.importLorebook(export, null);

        assertThat(normalized(lorebookService.exportLorebook(again))).isEqualTo(normalized(export));
    }

    @Test
    void sillyTavernInvalidFiles() throws Exception {
        assertThatThrownBy(() -> lorebookService.importFromSillytavern("", "x")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> lorebookService.importFromSillytavern("not json", "x")).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> lorebookService.importFromSillytavern("[1, 2]", "x")).isInstanceOf(RuntimeException.class);
        // a failed import leaves no half-created lorebook behind
        assertThat(lorebookService.findAllForUser()).extracting(Lorebook::getName).containsExactlyInAnyOrder("World", "Magic", "Shared");
    }
}
