package com.github.enerccio.marginalia.extensions.namegenerator;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class NameGroupsTest {

    /** A category whose chain can make only the one name: the order is its length, so every context is unique. */
    private static NameTables.Category single(String title, String name) {
        return single(title, name, null);
    }

    private static NameTables.Category single(String title, String name, Integer size) {
        int order = name.length();
        Map<String, Map<Character, Double>> tables = new java.util.HashMap<>();
        String padded = "^".repeat(order) + name + "$";
        for (int i = order; i < padded.length(); i++) {
            tables.put(padded.substring(i - order, i), Map.of(padded.charAt(i), 1.0));
        }
        return new NameTables.Category(title, order, null, null, size, tables);
    }

    private static NameTables tables() {
        return new NameTables(List.of(single("Tribe - Alpha - Male", "alaric"), single("Tribe - Beta - Male", "bertram"),
                single("Tribe - Beta - Female", "berta")),
                Map.of("Tribal - Male", new NameTables.Group(List.of("Tribe - * - Male"), false),
                        "Empty", new NameTables.Group(List.of("Nothing - *"), false),
                        "Tribe - Alpha - Male", new NameTables.Group(List.of("Tribe - Beta - Male"), false)));
    }

    @Test
    void patternsMatchWholeTitlesWithWildcards() {
        List<String> titles = List.of("A - X - Male", "A - Y - Male", "A - Y - Female", "B - X - Male");
        assertEquals(List.of("A - X - Male", "A - Y - Male"), NameTables.resolve(titles, List.of("a - * - male", "A - X - Male")).subList(0, 2));
        assertEquals(List.of("A - Y - Female"), NameTables.resolve(titles, List.of("A - Y - Female")));
        assertEquals(List.of(), NameTables.resolve(titles, List.of("A - Y")));
        assertEquals(List.of("A - X - Male"), NameTables.resolve(titles, List.of("A - X - Male", "A - * - X - Male", "*X - Male")).subList(0, 1));
        assertEquals(List.of(), NameTables.resolve(titles, List.of("A.*")));
    }

    @Test
    void groupIsListedAndKnown() {
        NameTables tables = tables();
        assertTrue(tables.has("tribal - male"));
        assertTrue(tables.isGroup("Tribal - Male"));
        assertFalse(tables.isGroup("Tribe - Beta - Male"));
        assertTrue(tables.categories().contains("Tribal - Male"));
        assertEquals(List.of("Tribe - Alpha - Male", "Tribe - Beta - Male"), tables.members("Tribal - Male"));
        assertEquals(List.of("Tribal - Male"), tables.categories("tribal"));
    }

    @Test
    void groupWithoutMembersOrWithTheTitleOfACategoryIsLeftOut() {
        NameTables tables = tables();
        assertFalse(tables.has("Empty"));
        assertFalse(tables.isGroup("Tribe - Alpha - Male"));
        assertEquals(List.of("alaric"), tables.generate("Tribe - Alpha - Male", 5, new Random(1)).stream().map(String::toLowerCase).toList());
    }

    @Test
    void everyNameComesFromOneMemberAndBothAreUsed() {
        NameTables tables = tables();
        Set<String> seen = new TreeSet<>();
        Random random = new Random(42);
        for (int i = 0; i < 20; i++) {
            seen.addAll(tables.generate("Tribal - Male", 1, null, null, random));
        }
        assertEquals(Set.of("Alaric", "Bertram"), seen);
        assertEquals(2, tables.generate("Tribal - Male", 10, new Random(3)).size());
    }

    @Test
    void groupDoesNotTakeMembersOutsideThePattern() {
        assertEquals(List.of("Berta"), tables().generate("Tribe - Beta - Female", 3, new Random(7)));
        assertThrows(IllegalArgumentException.class, () -> tables().generate("Nope", 1, new Random(7)));
    }

    @Test
    void groupsFileIsRead() {
        Map<String, NameTables.Group> groups = NameTables.parseGroups(new StringReader(
                "{\"groups\": [{\"title\": \"G\", \"members\": [\"A - *\", \"B\"]},"
                        + "{\"title\": \"H\", \"members\": [\"C\"], \"weight\": \"size\"},"
                        + "{\"title\": \"I\", \"members\": [\"C\"], \"weight\": \"equal\"}]}"));
        assertEquals(new NameTables.Group(List.of("A - *", "B"), false), groups.get("G"));
        assertEquals(new NameTables.Group(List.of("C"), true), groups.get("H"));
        assertEquals(new NameTables.Group(List.of("C"), false), groups.get("I"));
        assertThrows(IllegalStateException.class, () -> NameTables.parseGroups(new StringReader(
                "{\"groups\": [{\"title\": \"G\", \"members\": [\"A\"], \"weight\": \"lots\"}]}")));
    }

    private static NameTables weighted(boolean bySize) {
        return new NameTables(List.of(single("W - Big", "bigname", 90), single("W - Small", "smallname", 10), single("W - Unknown", "unknownname")),
                Map.of("W", new NameTables.Group(List.of("W - *"), bySize)));
    }

    private static Map<String, Integer> share(NameTables tables, int draws) {
        Random random = new Random(7);
        Map<String, Integer> seen = new java.util.TreeMap<>();
        for (int i = 0; i < draws; i++) {
            tables.generate("W", 1, 3, 20, random).forEach(name -> seen.merge(name, 1, Integer::sum));
        }
        return seen;
    }

    @Test
    void bySizeGroupChoosesMembersInProportionToTheirSize() {
        Map<String, Integer> seen = share(weighted(true), 10000);
        // sizes 90, 10 and unknown (counts as 1): about 89 %, 10 % and 1 %
        assertTrue(seen.get("Bigname") > 8500 && seen.get("Bigname") < 9300, seen.toString());
        assertTrue(seen.get("Smallname") > 800 && seen.get("Smallname") < 1250, seen.toString());
        assertTrue(seen.getOrDefault("Unknownname", 0) < 250, seen.toString());
    }

    @Test
    void equalGroupChoosesMembersWithTheSameChance() {
        Map<String, Integer> seen = share(weighted(false), 9000);
        seen.values().forEach(n -> assertTrue(n > 2700 && n < 3300, seen.toString()));
        assertEquals(3, seen.size());
    }

    /**
     * A "- Male" group may not pick up a female or surname category and the other way round (the lists of both genders are
     * separate); a "- Forenames" group is both genders, and no surnames.
     */
    @Test
    void shippedGroupsKeepTheirGender() {
        NameTables all = NameTables.load();
        for (String group : all.categories().stream().filter(all::isGroup).toList()) {
            for (String member : all.members(group)) {
                if (group.endsWith(" - Male")) {
                    assertFalse(member.matches("(?i).*(female|surnames).*"), group + " has " + member);
                } else if (group.endsWith(" - Female")) {
                    assertFalse(member.matches("(?i).*((?<!fe)male|surnames).*"), group + " has " + member);
                } else if (group.endsWith(" - Forenames")) {
                    assertFalse(member.matches("(?i).*surnames.*"), group + " has " + member);
                }
            }
        }
    }

    /** The groups shipped with the extension have to name categories that exist, or they silently get no names. */
    @Test
    void shippedGroupsResolveToCategories() throws Exception {
        NameTables all = NameTables.load();
        try (var in = NameTables.class.getClassLoader().getResourceAsStream(NameTables.GROUPS_RESOURCE)) {
            assertNotNull(in);
            Map<String, NameTables.Group> groups = NameTables.parseGroups(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
            assertFalse(groups.isEmpty());
            for (var group : groups.entrySet()) {
                assertTrue(all.isGroup(group.getKey()), "Group without members or with the title of a category: " + group.getKey());
                for (String pattern : group.getValue().members()) {
                    assertFalse(NameTables.resolve(all.categories(), List.of(pattern)).isEmpty(), group.getKey() + " has no category for " + pattern);
                }
                assertFalse(all.generate(group.getKey(), 20, new Random(5)).isEmpty(), group.getKey());
            }
        }
    }

    /** The groups of both genders have members of the male and of the female lists of the language. */
    @Test
    void shippedForenamesGroupsHaveBothGenders() {
        NameTables all = NameTables.load();
        assertTrue(all.isGroup("German - Forenames"));
        List<String> members = all.members("German - Forenames");
        assertTrue(members.containsAll(all.members("German - Male")));
        assertTrue(members.containsAll(all.members("German - Female")));
        assertTrue(members.contains("German Forenames"), "the unisex list is a member too");
        assertFalse(all.isGroup("Turkic - Forenames"), "one male member more than female would skew it two to one");
    }
}
