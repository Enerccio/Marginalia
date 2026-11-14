package com.github.enerccio.marginalia.domain;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LoreMentions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Where a text names lorebook entries: by name or filter word, whole words only, ignoring case.
 */
class LoreMentionsTest {

    private static LorebookEntry entry(String name, FilteringMode mode, String filter, String payload) {
        LorebookEntry entry = new LorebookEntry();
        entry.setUuid(UUID.randomUUID().toString());
        entry.setName(name);
        entry.setFilteringMode(mode);
        entry.setFiltering(filter);
        entry.setPayload(payload);
        return entry;
    }

    @Test
    void theNameAndTheKeywordsAreFoundAsWholeWordsIgnoringCase() {
        LorebookEntry varro = entry("Varro", FilteringMode.KEYWORDS_OR, "captain, the watch", null);

        assertThat(LoreMentions.find(varro, "VARRO and the Watch")).extracting(LoreMentions.Mention::start)
                .containsExactly(0, 10);
        assertThat(LoreMentions.names("Varrons are a people", varro)).isFalse();
        assertThat(LoreMentions.names("their captain", varro)).isTrue();
        assertThat(LoreMentions.names("captainship", varro)).isFalse();
    }

    @Test
    void aTextFilterIsOneWordAndARegexMatches() {
        LorebookEntry city = entry("Port", FilteringMode.TEXT, "Low City", null);
        LorebookEntry odo = entry("Priest", FilteringMode.REGEX, "od[oa]", null);

        assertThat(LoreMentions.names("in the low city", city)).isTrue();
        assertThat(LoreMentions.names("in the low", city)).isFalse();
        assertThat(LoreMentions.names("Oda prays", odo)).isTrue();
        assertThat(LoreMentions.names("nothing", entry("Bad", FilteringMode.REGEX, "([", null))).isFalse();
    }

    @Test
    void scriptsWithoutSpacesAreFoundInsideTheText() {
        LorebookEntry tokyo = entry("東京", FilteringMode.TEXT, "", null);

        assertThat(LoreMentions.names("彼は東京へ行った", tokyo)).isTrue();
    }

    @Test
    void theEntryAtAPositionIsTheLongestNameThereAndNeverItself() {
        LorebookEntry city = entry("City", FilteringMode.TEXT, "", null);
        LorebookEntry lowCity = entry("Low City", FilteringMode.TEXT, "", null);
        LorebookEntry self = entry("Varro", FilteringMode.TEXT, "", null);
        List<LorebookEntry> all = List.of(city, lowCity, self);
        String text = "Varro lives in the Low City.";

        assertThat(LoreMentions.entryAt(text, text.indexOf("City") + 2, all, self)).isSameAs(lowCity);
        assertThat(LoreMentions.entryAt(text, text.indexOf("Low"), all, self)).isSameAs(lowCity);
        // the caret right after the word
        assertThat(LoreMentions.entryAt(text, text.indexOf("City") + 4, all, self)).isSameAs(lowCity);
        assertThat(LoreMentions.entryAt(text, 2, all, self)).isNull();
        assertThat(LoreMentions.entryAt(text, text.indexOf("lives") + 1, all, self)).isNull();
        assertThat(LoreMentions.entryAt(text, 2, all, null)).isSameAs(self);
    }

    @Test
    void mentionedInListsTheEntriesWhoseContentNamesIt() {
        LorebookEntry varro = entry("Varro", FilteringMode.KEYWORDS_OR, "captain", "Varro is a captain.");
        LorebookEntry lia = entry("Lia", FilteringMode.TEXT, "", "Lia robs the captain.");
        LorebookEntry odo = entry("Odo", FilteringMode.TEXT, "", "Odo knows Varro and Lia.");
        LorebookEntry empty = entry("Empty", FilteringMode.TEXT, "", null);
        List<LorebookEntry> all = List.of(varro, lia, odo, empty);

        assertThat(LoreMentions.mentionedIn(varro, all)).containsExactly(lia, odo);
        assertThat(LoreMentions.mentionedIn(lia, all)).containsExactly(odo);
        assertThat(LoreMentions.mentionedIn(odo, all)).isEmpty();
    }
}
