package com.github.enerccio.marginalia.extensions.lorebookvcs.service;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.extensions.lorebookvcs.model.LoreEntryRevision;
import com.github.enerccio.marginalia.extensions.lorebookvcs.model.LoreEntryVCSData;
import com.github.enerccio.marginalia.extensions.lorebookvcs.model.LorebookVCSData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Importing a history: matching the entry histories of another copy of the lorebook to this one's entries, and reading the
 * revision history SillyTavern exports.
 */
class LorebookVCSServiceTest {

    private LorebookVCSService service;
    private Lorebook lorebook;
    private List<LorebookEntry> entries;

    private static LorebookEntry entry(String uuid, String name, int order) {
        LorebookEntry entry = new LorebookEntry();
        entry.setUuid(uuid);
        entry.setName(name);
        entry.setOrder(order);
        entry.setPayload("payload of " + name);
        entry.setComment("comment of " + name);
        entry.setFiltering(name.toLowerCase());
        return entry;
    }

    private static LoreEntryVCSData history(String uuid, String name, int order) {
        LoreEntryRevision rev = new LoreEntryRevision();
        rev.setName(name);
        rev.setOrder(order);
        LoreEntryVCSData history = new LoreEntryVCSData();
        history.setEntryUuid(uuid);
        history.getRevisions().add(rev);
        history.setCurrentRevision(0);
        return history;
    }

    @BeforeEach
    void setUp() throws Exception {
        lorebook = new Lorebook();
        lorebook.setUuid("book");
        entries = List.of(entry("u1", "Alice", 100), entry("u2", "Bob", 100), entry("u3", "Bob", 200),
                entry("u4", "Carol", 50), entry("u5", "Eve", 10));
        LorebookEntryService entryService = (LorebookEntryService) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{LorebookEntryService.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getEntriesForLorebook")) {
                        assertSame(lorebook, args[0]);
                        return entries;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        service = new LorebookVCSService();
        Field field = LorebookVCSService.class.getDeclaredField("lorebookEntryService");
        field.setAccessible(true);
        field.set(service, entryService);
    }

    @Test
    void importedHistoriesAreMatchedByUuidThenNameAndOrderThenAUniqueName() throws Exception {
        Map<String, LoreEntryVCSData> imported = new LinkedHashMap<>();
        // the uuid wins even when the name changed
        imported.put("u1", history("whatever", "Renamed Alice", 1));
        imported.put("old-bob-200", history("old-bob-200", "Bob", 200));
        // two entries are named Bob, but the other one is taken already
        imported.put("old-bob-300", history("old-bob-300", "Bob", 300));
        imported.put("old-carol", history("old-carol", "Carol", 999));
        imported.put("old-dave", history("old-dave", "Dave", 100));
        LoreEntryVCSData noCurrent = history("old-eve", "Nobody", 1);
        LoreEntryRevision last = new LoreEntryRevision();
        last.setName("Eve");
        last.setOrder(10);
        noCurrent.getRevisions().add(last);
        noCurrent.setCurrentRevision(-1);
        imported.put("old-eve", noCurrent);
        imported.put("old-empty", new LoreEntryVCSData());
        imported.put("old-null", null);
        LorebookVCSData data = new LorebookVCSData();
        data.setLorebookUuid("another book");
        data.setEntries(imported);

        int dropped = service.remapEntries(data, lorebook);

        assertEquals(2, dropped, "Dave and the empty history");
        assertEquals("book", data.getLorebookUuid());
        Map<String, String> byEntry = new LinkedHashMap<>();
        data.getEntries().forEach((uuid, history) -> {
            assertEquals(uuid, history.getEntryUuid());
            byEntry.put(uuid, history.getRevisions().getLast().getName());
        });
        assertEquals(Map.of("u1", "Renamed Alice", "u2", "Bob", "u3", "Bob", "u4", "Carol", "u5", "Eve"), byEntry);
        assertEquals(300, data.getEntries().get("u2").getCurrent().getOrder());
        assertEquals(200, data.getEntries().get("u3").getCurrent().getOrder());
    }

    @Test
    void anAmbiguousNameIsDropped() throws Exception {
        Map<String, LoreEntryVCSData> imported = new LinkedHashMap<>();
        imported.put("old-bob", history("old-bob", "Bob", 1));
        LorebookVCSData data = new LorebookVCSData();
        data.setEntries(imported);
        assertEquals(1, service.remapEntries(data, lorebook));
        assertTrue(data.getEntries().isEmpty());
    }

    @Test
    void aStoredHistoryIsReadBack() {
        LorebookVCSData data = service.parseVCSJson("""
                {"lorebookUuid": "book", "entries": {"u1": {"entryUuid": "u1", "currentRevision": 0,
                  "revisions": [{"revisionId": "r1", "lastModified": 7, "name": "Alice", "order": 5, "positiveTags": ["Hero"]}]}}}
                """);
        assertEquals("book", data.getLorebookUuid());
        LoreEntryRevision current = data.getEntries().get("u1").getCurrent();
        assertEquals("r1", current.getRevisionId());
        assertEquals(5, current.getOrder());
        assertEquals(List.of("Hero"), current.getPositiveTags());
        assertTrue(current.isEnabled(), "a value that is not stored keeps its default");
    }

    @Test
    void aSillyTavernHistoryBecomesRevisionsOfTheMatchingEntries() throws Exception {
        String json = """
                {"entries": [
                  {"uid": "0", "currentRevision": 1, "revisions": [
                    {"revisionId": "st-1", "lastModified": 123, "data": {
                      "content": {"value": "Alice is a knight."}, "comment": {"value": "first"},
                      "disable": {"value": true}, "order": {"value": "7"},
                      "key": {"value": ["knight", "sword"]}, "keysecondary": {"value": []},
                      "position": {"value": 4},
                      "characterFilter": {"value": {"isExclude": true, "names": ["Bob"], "tags": ["villain"]}}}},
                    {"revisionId": "st-2", "data": {"key": "sword", "characterFilter": "Bob, Carol "}},
                    {"revisionId": "st-3", "data": {"constant": {"value": true}, "key": {"value": ["x"]},
                      "characterFilter": ["Dan"], "character_exclusion": true}}]},
                  {"uid": "not a number", "revisions": [{"revisionId": "st-4", "data": {}}]},
                  {"uid": "17", "revisions": [{"revisionId": "st-5", "data": {}}]},
                  {"uid": "3", "revisions": []}
                ]}
                """;
        entries = entries.subList(0, 2);
        LorebookVCSData data = service.parseSillyTavernVCSJson(json, lorebook);

        assertEquals("book", data.getLorebookUuid());
        assertEquals(List.of("u1", "u2"), data.getEntries().keySet().stream().sorted().toList(),
                "a uid that is no number goes by the position, past the entries nothing matches, no revisions no history");

        LoreEntryVCSData alice = data.getEntries().get("u1");
        assertEquals(1, alice.getCurrentRevision());
        assertEquals(3, alice.getRevisions().size());

        LoreEntryRevision first = alice.getRevisions().get(0);
        assertEquals("st-1", first.getRevisionId());
        assertEquals(123, first.getLastModified());
        assertEquals("Alice", first.getName(), "SillyTavern revisions have no name, the entry's is kept");
        assertEquals("Alice is a knight.", first.getPayload());
        assertEquals("first", first.getComment());
        assertFalse(first.isEnabled());
        assertEquals(7, first.getOrder());
        assertEquals(FilteringMode.REGEX, first.getFilteringMode());
        assertTrue(first.getFiltering().contains("knight") && first.getFiltering().contains("sword"), first.getFiltering());
        assertEquals(InsertionMode.BEFORE_USER_PROMPT, first.getInsertionMode());
        assertEquals(List.of(), first.getPositiveTags());
        assertEquals(List.of("Bob", "villain"), first.getNegativeTags());

        LoreEntryRevision second = alice.getRevisions().get(1);
        assertEquals(FilteringMode.TEXT, second.getFilteringMode());
        assertEquals("sword", second.getFiltering());
        assertEquals("payload of Alice", second.getPayload(), "what the revision lacks comes from the entry");
        assertEquals(100, second.getOrder());
        assertTrue(second.isEnabled());
        assertEquals(List.of("Bob", "Carol"), second.getPositiveTags());

        LoreEntryRevision third = alice.getRevisions().get(2);
        assertEquals(FilteringMode.TEXT, third.getFilteringMode());
        assertEquals("", third.getFiltering(), "a constant entry is always active");
        assertEquals(List.of("Dan"), third.getNegativeTags());

        LoreEntryRevision bob = data.getEntries().get("u2").getRevisions().getFirst();
        assertEquals("st-4", bob.getRevisionId());
        assertEquals("Bob", bob.getName());
        assertEquals("payload of Bob", bob.getPayload());
        assertEquals("bob", bob.getFiltering());
        assertEquals(InsertionMode.IN_LORE_BLOCK, bob.getInsertionMode());
    }
}
