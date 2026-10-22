package com.github.enerccio.marginalia.lorebook;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookReplaceService;
import com.github.enerccio.marginalia.domain.service.LorebookReplaceService.Options;
import com.github.enerccio.marginalia.domain.service.LorebookReplaceService.Plan;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.search.replace.TaskProgress;
import com.github.enerccio.marginalia.domain.service.search.replace.TextSearch;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CancellationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Search and replace in the entries of a lorebook: name, text and comment of the entries of that lorebook only.
 */
class LorebookReplaceTest extends MarginaliaTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService entryService;

    @Autowired
    private LorebookReplaceService replaceService;

    private Lorebook lorebook;
    private LorebookEntry harry, hogwarts, other;

    private LorebookEntry entry(Lorebook book, String name, String payload, String comment, int order) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(book);
        entry.setName(name);
        entry.setPayload(payload);
        entry.setComment(comment);
        entry.setOrder(order);
        return entryService.save(entry);
    }

    @BeforeEach
    void createLorebooks() throws Exception {
        login();
        Lorebook l = new Lorebook();
        l.setName("lore");
        lorebook = lorebookService.save(l);
        Lorebook o = new Lorebook();
        o.setName("another");
        Lorebook another = lorebookService.save(o);

        hogwarts = entry(lorebook, "Hogwarts", "A school where Harry studies. Harry likes it.", "about Harry", 20);
        harry = entry(lorebook, "Harry", "The boy.", null, 10);
        entry(lorebook, "Dragons", "Fire.", null, 30);
        other = entry(another, "Harry", "In another book", null, 10);
    }

    private LorebookEntry reload(LorebookEntry entry) throws Exception {
        return entryService.find(entry.getId());
    }

    private Plan plan(String search, String replacement, boolean regex) throws Exception {
        return replaceService.plan(lorebook, new Options(search, replacement, regex, true), TaskProgress.NONE);
    }

    @Test
    void planListsTheEntriesWithMatchesInTheirOrder() throws Exception {
        Plan plan = plan("Harry", "Ron", false);

        assertThat(plan.items()).extracting(i -> i.name()).containsExactly("Harry", "Hogwarts");
        assertThat(plan.items().get(0).hits()).hasSize(1);
        assertThat(plan.items().get(1).hits()).extracting(h -> h.field()).containsExactly("payload", "payload", "comment");
        assertThat(plan.hitCount()).isEqualTo(4);
        // planning changes nothing
        assertThat(reload(harry).getName()).isEqualTo("Harry");
    }

    @Test
    void replaceChangesNameTextAndCommentOfTheEntriesOfTheLorebookOnly() throws Exception {
        Plan plan = plan("Harry", "Ron", false);

        LorebookReplaceService.Result result = replaceService.apply(lorebook, plan, TaskProgress.NONE);

        assertThat(result.changed()).isEqualTo(2);
        assertThat(result.hits()).isEqualTo(4);
        assertThat(result.skipped()).isZero();
        assertThat(reload(harry).getName()).isEqualTo("Ron");
        assertThat(reload(hogwarts).getPayload()).isEqualTo("A school where Ron studies. Ron likes it.");
        assertThat(reload(hogwarts).getComment()).isEqualTo("about Ron");
        assertThat(reload(hogwarts).get_fulltext()).contains("Ron").doesNotContain("Harry");
        assertThat(reload(other).getName()).isEqualTo("Harry");
    }

    @Test
    void caseInsensitiveSearchFindsAllSpellings() throws Exception {
        assertThat(plan("harry", "Ron", false).items()).isEmpty();

        Plan plan = replaceService.plan(lorebook, new Options("harry", "Ron", false, false), TaskProgress.NONE);

        assertThat(plan.items()).extracting(i -> i.name()).containsExactly("Harry", "Hogwarts");
        replaceService.apply(lorebook, plan, TaskProgress.NONE);
        assertThat(reload(harry).getName()).isEqualTo("Ron");
    }

    @Test
    void regexReplacementUsesGroups() throws Exception {
        Plan plan = plan("(\\w+) likes", "$1 loves", true);

        replaceService.apply(lorebook, plan, TaskProgress.NONE);

        assertThat(reload(hogwarts).getPayload()).isEqualTo("A school where Harry studies. Harry loves it.");
    }

    @Test
    void entryEditedAfterThePlanIsNotReplaced() throws Exception {
        Plan plan = plan("Harry", "Ron", false);
        LorebookEntry edited = reload(harry);
        edited.setName("Edited meanwhile");
        entryService.save(edited);

        LorebookReplaceService.Result result = replaceService.apply(lorebook, plan, TaskProgress.NONE);

        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.changed()).isEqualTo(1);
        assertThat(reload(harry).getName()).isEqualTo("Edited meanwhile");
    }

    @Test
    void deletedEntriesAreNotSearched() throws Exception {
        entryService.delete(reload(harry), false);

        assertThat(plan("Harry", "Ron", false).items()).extracting(i -> i.name()).containsExactly("Hogwarts");
    }

    @Test
    void invalidRegexAndStopAreReported() {
        assertThatThrownBy(() -> plan("(", "x", true)).isInstanceOf(TextSearch.InvalidSearchException.class);
        TaskProgress stopped = new TaskProgress() {
            @Override
            public void update(long done, long total) {
            }

            @Override
            public boolean isCancelled() {
                return true;
            }
        };
        assertThatThrownBy(() -> replaceService.plan(lorebook, new Options("Harry", "Ron", false, true), stopped))
                .isInstanceOf(CancellationException.class);
    }

    @Test
    void otherUserCannotSearchTheLorebook() throws Exception {
        loginAs(createUser());

        assertThatThrownBy(() -> plan("Harry", "Ron", false)).isInstanceOf(IllegalArgumentException.class);
    }

}
