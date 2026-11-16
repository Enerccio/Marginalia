package com.github.enerccio.marginalia.extensions.lorebookvcs.model;

import com.github.enerccio.marginalia.domain.collections.ActivationContext;
import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A revision is a snapshot of every setting of a lorebook entry: taken from an entry, written back to one, and compared with
 * one to tell whether the entry changed since.
 */
class LoreEntryRevisionTest {

    private static LorebookEntry entry() {
        LorebookEntry entry = new LorebookEntry();
        entry.setUuid("entry-1");
        entry.setName("Dragon");
        entry.setEnabled(false);
        entry.setOrder(42);
        entry.setPayload("Dragons breathe fire.");
        entry.setComment("from chapter 3");
        entry.setFilteringMode(FilteringMode.KEYWORDS_OR);
        entry.setFiltering("dragon, wyrm");
        entry.setInsertionMode(InsertionMode.BEFORE_USER_PROMPT);
        entry.setPreventRecursion(true);
        entry.setRecursionOnly(true);
        entry.setActivationSummaries(true);
        entry.setActivationContext(ActivationContext.INCLUDE_PARTS);
        entry.setActivationParts(3);
        entry.setFromMessage(2);
        entry.setToMessage(9);
        entry.setActivationCondition("{{eq mood 'dark'}}");
        return entry;
    }

    @Test
    void aSnapshotTakesEverySettingAndCopiesTheTags() {
        List<String> positive = new ArrayList<>(List.of("Fantasy"));
        LoreEntryRevision rev = LoreEntryRevision.snapshotOf(entry(), positive, List.of("Modern"));
        positive.add("added later");

        assertEquals("Dragon", rev.getName());
        assertFalse(rev.isEnabled());
        assertEquals(42, rev.getOrder());
        assertEquals("Dragons breathe fire.", rev.getPayload());
        assertEquals("from chapter 3", rev.getComment());
        assertEquals(FilteringMode.KEYWORDS_OR, rev.getFilteringMode());
        assertEquals("dragon, wyrm", rev.getFiltering());
        assertEquals(InsertionMode.BEFORE_USER_PROMPT, rev.getInsertionMode());
        assertTrue(rev.isPreventRecursion());
        assertTrue(rev.isRecursionOnly());
        assertTrue(rev.isActivationSummaries());
        assertEquals(ActivationContext.INCLUDE_PARTS, rev.getActivationContext());
        assertEquals(3, rev.getActivationParts());
        assertEquals(2, rev.getFromMessage());
        assertEquals(9, rev.getToMessage());
        assertEquals("{{eq mood 'dark'}}", rev.getActivationCondition());
        assertEquals(List.of("Fantasy"), rev.getPositiveTags());
        assertEquals(List.of("Modern"), rev.getNegativeTags());
        assertNotNull(rev.getRevisionId());
        assertTrue(rev.getLastModified() > 0);
    }

    @Test
    void aSnapshotOfAnEmptyEntryHasTheDefaults() {
        LorebookEntry blank = new LorebookEntry();
        blank.setName(null);
        blank.setPayload(null);
        blank.setFilteringMode(null);
        blank.setInsertionMode(null);
        LoreEntryRevision rev = LoreEntryRevision.snapshotOf(blank, null, null);
        assertEquals("", rev.getName());
        assertEquals("", rev.getPayload());
        assertEquals("", rev.getComment());
        assertEquals("", rev.getFiltering());
        assertEquals(FilteringMode.TEXT, rev.getFilteringMode());
        assertEquals(InsertionMode.IN_LORE_BLOCK, rev.getInsertionMode());
        assertEquals(List.of(), rev.getPositiveTags());
        assertEquals(List.of(), rev.getNegativeTags());
        assertTrue(rev.matches(blank, null, List.of()));
    }

    @Test
    void appliedRevisionMakesTheEntryMatchIt() {
        LoreEntryRevision rev = LoreEntryRevision.snapshotOf(entry(), List.of("Fantasy"), List.of());
        LorebookEntry other = new LorebookEntry();
        assertFalse(rev.matches(other, List.of("Fantasy"), List.of()));

        rev.applyTo(other);
        assertTrue(rev.matches(other, List.of("Fantasy"), List.of()));
        assertEquals("Dragon", other.getName());
        assertEquals(42, other.getOrder());
        assertEquals("{{eq mood 'dark'}}", other.getActivationCondition());
        assertEquals(9, other.getToMessage());
    }

    @Test
    void tagsAreComparedWithoutCaseAndOrder() {
        LoreEntryRevision rev = LoreEntryRevision.snapshotOf(entry(), List.of("Fantasy", "dragons"), List.of("Modern"));
        assertTrue(rev.matches(entry(), List.of("DRAGONS", "fantasy"), List.of("modern")));
        assertFalse(rev.matches(entry(), List.of("Fantasy"), List.of("Modern")));
        assertFalse(rev.matches(entry(), List.of("Fantasy", "dragons", "extra"), List.of("Modern")));
        assertFalse(rev.matches(entry(), List.of("Fantasy", "dragons"), List.of()));
        assertFalse(rev.matches(entry(), List.of("Fantasy", "wyrms"), List.of("Modern")));
    }

    @Test
    void anyChangedSettingIsAChange() {
        List<Consumer<LorebookEntry>> changes = List.of(
                e -> e.setName("Wyrm"),
                e -> e.setEnabled(true),
                e -> e.setOrder(43),
                e -> e.setPayload("Dragons sleep."),
                e -> e.setComment(""),
                e -> e.setFilteringMode(FilteringMode.TEXT),
                e -> e.setFiltering("dragon"),
                e -> e.setInsertionMode(InsertionMode.IN_LORE_BLOCK),
                e -> e.setPreventRecursion(false),
                e -> e.setRecursionOnly(false),
                e -> e.setActivationSummaries(false),
                e -> e.setActivationContext(ActivationContext.INSTRUCTIONS),
                e -> e.setActivationParts(4),
                e -> e.setFromMessage(null),
                e -> e.setToMessage(10),
                e -> e.setActivationCondition(null));
        LoreEntryRevision rev = LoreEntryRevision.snapshotOf(entry(), List.of(), List.of());
        assertTrue(rev.matches(entry(), List.of(), List.of()));
        for (int i = 0; i < changes.size(); i++) {
            LorebookEntry changed = entry();
            changes.get(i).accept(changed);
            assertFalse(rev.matches(changed, List.of(), List.of()), "change " + i + " is not seen");
        }
        assertFalse(rev.matches(null, List.of(), List.of()));
    }

    @Test
    void outOfRangeValuesReadAsTheirDefaults() {
        LoreEntryRevision rev = new LoreEntryRevision();
        rev.setActivationParts(0);
        rev.setFromMessage(0);
        rev.setToMessage(-3);
        rev.setActivationCondition("   ");
        rev.setPositiveTags(null);
        rev.setName(null);
        rev.setActivationContext(null);
        assertEquals(1, rev.getActivationParts());
        assertNull(rev.getFromMessage());
        assertNull(rev.getToMessage());
        assertNull(rev.getActivationCondition());
        assertEquals(List.of(), rev.getPositiveTags());
        assertEquals("", rev.getName());
        assertEquals(ActivationContext.INSTRUCTIONS, rev.getActivationContext());
    }

    @Test
    void aRevisionStoredBeforeTheNewerSettingsReadsWithTheirDefaults() {
        LoreEntryRevision old = new Gson().fromJson("""
                {"revisionId": "rev-1", "lastModified": 5, "name": "Dragon", "enabled": true, "order": 100,
                 "payload": "x", "filteringMode": "TEXT", "filtering": "dragon", "insertionMode": "IN_LORE_BLOCK"}
                """, LoreEntryRevision.class);
        assertEquals("rev-1", old.getRevisionId());
        assertEquals(5, old.getLastModified());
        assertFalse(old.isPreventRecursion());
        assertFalse(old.isRecursionOnly());
        assertEquals(ActivationContext.INSTRUCTIONS, old.getActivationContext());
        assertEquals(1, old.getActivationParts());
        assertNull(old.getFromMessage());
        assertNull(old.getActivationCondition());
        assertEquals("", old.getComment());
    }

    @Test
    void theCurrentRevisionIsOnlyOneThatExists() {
        LoreEntryVCSData history = new LoreEntryVCSData();
        assertNull(history.getCurrent());
        LoreEntryRevision first = new LoreEntryRevision();
        history.getRevisions().add(first);
        history.setCurrentRevision(0);
        assertSame(first, history.getCurrent());
        history.setCurrentRevision(1);
        assertNull(history.getCurrent());
    }

    @Test
    void anEntryHistoryIsCreatedOnceWithItsUuid() {
        LorebookVCSData data = new LorebookVCSData();
        LoreEntryVCSData created = data.getOrCreateEntry("u1");
        assertEquals("u1", created.getEntryUuid());
        assertSame(created, data.getOrCreateEntry("u1"));
        assertEquals(1, data.getEntries().size());
    }
}
