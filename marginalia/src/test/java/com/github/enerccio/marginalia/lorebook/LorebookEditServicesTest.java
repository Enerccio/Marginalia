package com.github.enerccio.marginalia.lorebook;

import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookCycleException;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService.EntryProblemKind;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService.EntrySaved;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The editing of lorebooks the screens and the commands share: subbooks with the cycle check, entries with the problems
 * of their templates and filters as data.
 */
class LorebookEditServicesTest extends MarginaliaTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService entryService;

    private Lorebook root, middle, leaf;

    private Lorebook lorebook(String name) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(uniqueName(name));
        return lorebookService.save(lorebook);
    }

    @BeforeEach
    void createLorebooks() throws Exception {
        login();
        root = lorebook("root");
        middle = lorebook("middle");
        leaf = lorebook("leaf");
    }

    private List<Long> subbookIds(Lorebook lorebook) throws Exception {
        return lorebookService.getSubbooks(lorebook).stream().map(Lorebook::getId).toList();
    }

    // ----------------------------------------------------------------------------------------------- subbooks

    @Test
    void addAndRemoveSubbook() throws Exception {
        lorebookService.addSubbook(root, middle);
        lorebookService.addSubbook(root, leaf);
        assertThat(subbookIds(root)).containsExactlyInAnyOrder(middle.getId(), leaf.getId());

        lorebookService.addSubbook(root, middle);
        assertThat(subbookIds(root)).as("adding twice changes nothing").containsExactlyInAnyOrder(middle.getId(), leaf.getId());

        lorebookService.removeSubbook(root, middle);
        assertThat(subbookIds(root)).containsExactly(leaf.getId());
        lorebookService.removeSubbook(root, middle);
        assertThat(subbookIds(root)).as("removing what is not there changes nothing").containsExactly(leaf.getId());
    }

    @Test
    void aLorebookCanNotContainItselfHoweverDeep() throws Exception {
        lorebookService.addSubbook(root, middle);
        lorebookService.addSubbook(middle, leaf);

        assertThat(lorebookService.wouldCycle(root, root)).isTrue();
        assertThat(lorebookService.wouldCycle(leaf, root)).isTrue();
        assertThat(lorebookService.wouldCycle(middle, root)).isTrue();
        assertThat(lorebookService.wouldCycle(root, leaf)).as("a second path is not a cycle").isFalse();

        assertThatThrownBy(() -> lorebookService.addSubbook(leaf, root)).isInstanceOf(LorebookCycleException.class);
        assertThatThrownBy(() -> lorebookService.addSubbook(root, root)).isInstanceOf(LorebookCycleException.class);
        assertThat(subbookIds(leaf)).isEmpty();
    }

    @Test
    void setSubbooksReplacesAllOrNothing() throws Exception {
        lorebookService.addSubbook(root, middle);
        lorebookService.addSubbook(middle, leaf);

        lorebookService.setSubbooks(root, List.of(leaf, middle));
        assertThat(subbookIds(root)).containsExactlyInAnyOrder(leaf.getId(), middle.getId());

        assertThatThrownBy(() -> lorebookService.setSubbooks(leaf, List.of(root))).isInstanceOf(LorebookCycleException.class);
        assertThat(subbookIds(leaf)).isEmpty();

        lorebookService.setSubbooks(root, null);
        assertThat(subbookIds(root)).isEmpty();
    }

    @Test
    void createSubbookMakesAnEnabledChild() throws Exception {
        Lorebook created = lorebookService.createSubbook(root, "  places  ");

        assertThat(created.getName()).isEqualTo("places");
        assertThat(created.isEnabled()).isTrue();
        assertThat(created.isAgentic()).isFalse();
        assertThat(subbookIds(root)).containsExactly(created.getId());
        assertThatThrownBy(() -> lorebookService.createSubbook(root, " ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void lorebooksOfOtherUsersAreNotReachable() throws Exception {
        Lorebook mine = root;
        login();
        Lorebook theirs = lorebook("theirs");
        loginAs(mine.getOwner());

        assertThatThrownBy(() -> lorebookService.addSubbook(mine, theirs)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> lorebookService.createSubbook(theirs, "x")).isInstanceOf(IllegalArgumentException.class);
    }

    // ------------------------------------------------------------------------------------------------ entries

    private LorebookEntry fields(String name, String payload) {
        LorebookEntry fields = new LorebookEntry();
        fields.setName(name);
        fields.setPayload(payload);
        fields.setOrder(7);
        fields.setComment("a note");
        return fields;
    }

    @Test
    void createMakesTheEntryOfTheLorebook() throws Exception {
        EntrySaved saved = entryService.create(root, fields("Harry", "A wizard."));

        assertThat(saved.problems()).isEmpty();
        LorebookEntry entry = entryService.find(saved.entry().getId());
        assertThat(entry.getLorebook().getId()).isEqualTo(root.getId());
        assertThat(entry.getName()).isEqualTo("Harry");
        assertThat(entry.getPayload()).isEqualTo("A wizard.");
        assertThat(entry.getOrder()).isEqualTo(7);
        assertThat(entry.getComment()).isEqualTo("a note");
        assertThat(entryService.getEntriesForLorebook(root)).hasSize(1);
    }

    @Test
    void aBrokenTemplateIsSavedAndReported() throws Exception {
        EntrySaved saved = entryService.create(root, fields("Broken", "{{#if}} never closed"));

        assertThat(saved.problems()).extracting(p -> p.kind()).containsExactly(EntryProblemKind.INVALID_TEMPLATE);
        assertThat(saved.problems().getFirst().field()).isEqualTo("payload");
        assertThat(entryService.find(saved.entry().getId()).getPayload()).isEqualTo("{{#if}} never closed");
    }

    @Test
    void anInvalidRegexIsReportedOnlyForARegexFilter() throws Exception {
        LorebookEntry regex = fields("Regex", "x");
        regex.setFilteringMode(FilteringMode.REGEX);
        regex.setFiltering("(unclosed");
        LorebookEntry text = fields("Text", "x");
        text.setFilteringMode(FilteringMode.TEXT);
        text.setFiltering("(unclosed");

        assertThat(entryService.create(root, regex).problems()).extracting(p -> p.kind()).containsExactly(EntryProblemKind.INVALID_REGEX);
        assertThat(entryService.create(root, text).problems()).isEmpty();
    }

    @Test
    void updateChangesTheFieldsAndChecksThemAgain() throws Exception {
        LorebookEntry entry = entryService.create(root, fields("Harry", "A wizard.")).entry();

        LorebookEntry changes = entryService.find(entry.getId());
        changes.setPayload("{{#if}} broken");
        changes.setName("Harry Potter");
        EntrySaved saved = entryService.update(changes);

        assertThat(saved.problems()).extracting(p -> p.kind()).containsExactly(EntryProblemKind.INVALID_TEMPLATE);
        assertThat(entryService.find(entry.getId()).getName()).isEqualTo("Harry Potter");

        changes.setPayload("A wizard.");
        assertThat(entryService.update(changes).problems()).isEmpty();
    }

    @Test
    void updateRefusesAnEntryOfAnotherUser() throws Exception {
        LorebookEntry entry = entryService.create(root, fields("Mine", "x")).entry();
        login();

        assertThatThrownBy(() -> entryService.update(entry)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> entryService.create(root, fields("x", "x"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> entryService.deleteEntry(root, entry)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteEntryChecksTheEntryIsOfTheLorebook() throws Exception {
        LorebookEntry inRoot = entryService.create(root, fields("a", "x")).entry();
        LorebookEntry inLeaf = entryService.create(leaf, fields("b", "x")).entry();

        assertThatThrownBy(() -> entryService.deleteEntry(root, inLeaf)).isInstanceOf(IllegalArgumentException.class);
        assertThat(entryService.getEntriesForLorebook(leaf)).hasSize(1);

        entryService.deleteEntry(root, inRoot);
        assertThat(entryService.getEntriesForLorebook(root)).isEmpty();
        assertThat(entryService.getEntriesForLorebook(leaf)).hasSize(1);
    }
}
