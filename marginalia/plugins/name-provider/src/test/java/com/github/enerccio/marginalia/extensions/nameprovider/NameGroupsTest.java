package com.github.enerccio.marginalia.extensions.nameprovider;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class NameGroupsTest {

    private static List<NameEntry> names(String... romanized) {
        return Arrays.stream(romanized).map(n -> new NameEntry(n, n)).toList();
    }

    private static NameLists lists() {
        Map<String, List<NameEntry>> categories = new LinkedHashMap<>();
        categories.put("North - Surnames", names("Aa", "Ab", "Ac"));
        categories.put("South - Surnames", names("Ba", "Bb"));
        categories.put("South - Forenames", names("Bc"));
        return new NameLists(categories, Map.of("Compass - Surnames", List.of("*- Surnames"), "Empty", List.of("Nothing"),
                "North - Surnames", List.of("South - Surnames")));
    }

    @Test
    void patternsMatchWholeTitlesWithWildcards() {
        List<String> titles = List.of("A - X", "A - Y", "B - X");
        assertEquals(List.of("A - X", "A - Y"), NameLists.resolve(titles, List.of("a - *")));
        assertEquals(List.of("A - X", "B - X"), NameLists.resolve(titles, List.of("*- X", "B - X")));
        assertEquals(List.of(), NameLists.resolve(titles, List.of("A")));
        assertEquals(List.of(), NameLists.resolve(titles, List.of("A.*")));
    }

    @Test
    void groupIsListedAndKnown() {
        NameLists lists = lists();
        assertTrue(lists.has("compass - surnames"));
        assertTrue(lists.isGroup("Compass - Surnames"));
        assertEquals(List.of("North - Surnames", "South - Surnames"), lists.members("Compass - Surnames"));
        assertEquals(List.of("Compass - Surnames"), lists.categories("compass"));
        assertEquals(5, lists.size("Compass - Surnames"));
        assertEquals(3, lists.size("North - Surnames"));
    }

    @Test
    void groupWithoutMembersOrWithTheTitleOfAListIsLeftOut() {
        NameLists lists = lists();
        assertFalse(lists.has("Empty"));
        assertFalse(lists.isGroup("North - Surnames"));
        assertEquals(3, lists.pick("North - Surnames", 10, new Random(1)).size());
    }

    @Test
    void groupPicksFromEveryMemberWithoutRepeats() {
        NameLists lists = lists();
        Random random = new Random(42);
        Set<String> seenFirst = new TreeSet<>();
        for (int i = 0; i < 40; i++) {
            seenFirst.add(lists.pick("Compass - Surnames", 1, random).get(0).romanized().substring(0, 1));
        }
        assertEquals(Set.of("A", "B"), seenFirst);
        List<NameEntry> all = lists.pick("Compass - Surnames", 100, random);
        assertEquals(5, all.size());
        assertEquals(5, new HashSet<>(all).size());
        assertEquals(Set.of("Aa", "Ab", "Ac", "Ba", "Bb"), all.stream().map(NameEntry::romanized).collect(Collectors.toSet()));
    }

    @Test
    void smallMemberDoesNotEmptyTheGroupEarly() {
        List<NameEntry> picked = lists().pick("Compass - Surnames", 4, new Random(9));
        assertEquals(4, picked.size());
        assertEquals(4, new HashSet<>(picked).size());
    }

    @Test
    void unknownListThrowsAndPlainListStillShuffles() {
        assertThrows(IllegalArgumentException.class, () -> lists().pick("Nope", 1, new Random(1)));
        assertEquals(Set.of("Bc"), lists().pick("South - Forenames", 5, new Random(1)).stream().map(NameEntry::romanized).collect(Collectors.toSet()));
        assertEquals(List.of(), lists().pick("South - Forenames", 0, new Random(1)));
    }

    @Test
    void groupsFileIsRead() {
        assertEquals(Map.of("G", List.of("A - *", "B")), NameLists.parseGroups(new StringReader(
                "{\"groups\": [{\"title\": \"G\", \"members\": [\"A - *\", \"B\"]}]}")));
    }

    /** The groups shipped with the extension have to name lists that exist, or they silently get no names. */
    @Test
    void shippedGroupsResolveToLists() throws Exception {
        NameLists all = NameLists.load();
        try (var in = NameLists.class.getClassLoader().getResourceAsStream(NameLists.GROUPS_RESOURCE)) {
            assertNotNull(in);
            Map<String, List<String>> groups = NameLists.parseGroups(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
            assertFalse(groups.isEmpty());
            for (var group : groups.entrySet()) {
                assertTrue(all.isGroup(group.getKey()), "Group without members or with the title of a list: " + group.getKey());
                for (String pattern : group.getValue()) {
                    assertFalse(NameLists.resolve(all.categories(), List.of(pattern)).isEmpty(), group.getKey() + " has no list for " + pattern);
                }
                assertFalse(all.pick(group.getKey(), 20, new Random(5)).isEmpty(), group.getKey());
            }
        }
    }
}
