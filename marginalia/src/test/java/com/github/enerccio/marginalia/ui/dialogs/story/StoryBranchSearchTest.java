package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The search of the story: matches are numbered through the whole branch, the buttons walk them (wrapping around) and the
 * parts mark the text of their cards.
 */
class StoryBranchSearchTest extends MarginaliaTestBase {

    private final List<Collection<Long>> redrawn = new ArrayList<>();
    private final List<Long> shown = new ArrayList<>();
    private List<ChatMessage> branch;
    private StoryBranchSearch search;

    private static ChatMessage part(long id, String response) {
        ChatMessage message = new ChatMessage();
        message.setId(id);
        message.setResponse(response);
        return message;
    }

    @BeforeEach
    void setUp() {
        branch = List.of(part(1, "A key on the table."), part(2, "Nothing here."), part(3, "The key, the other key."));
        search = new StoryBranchSearch(() -> branch, redrawn::add, shown::add);
    }

    @Test
    void searchGoesToTheFirstMatchAndCountsEveryOccurrence() {
        search.search("key");

        assertThat(shown).containsExactly(1L);
        assertThat(search.statusText()).isEqualTo("Match 1 of 3, in part #1");
        assertThat(redrawn.getLast()).containsExactly(1L, 3L);
    }

    @Test
    void nextAndPreviousWalkTheMatchesAndWrapAround() {
        search.search("key");
        search.move(1);
        search.move(1);
        assertThat(search.statusText()).isEqualTo("Match 3 of 3, in part #3");
        search.move(1);
        assertThat(search.statusText()).isEqualTo("Match 1 of 3, in part #1");
        search.move(-1);
        assertThat(search.statusText()).isEqualTo("Match 3 of 3, in part #3");
        assertThat(shown).containsExactly(1L, 3L, 3L, 1L, 3L);
    }

    @Test
    void theCurrentMatchIsMarkedStronger() {
        search.search("key");
        search.move(1);

        assertThat(search.mark(1L, "A key")).isEqualTo("A <mark class=\"search-match\">key</mark>");
        assertThat(search.mark(3L, "key key"))
                .isEqualTo("<mark class=\"search-match search-match-current\">key</mark> <mark class=\"search-match\">key</mark>");
        assertThat(search.mark(2L, "Nothing")).isEqualTo("Nothing");
    }

    @Test
    void nothingFoundAndClearedSearchMarkNothing() {
        search.search("dragon");
        assertThat(search.statusText()).isEqualTo("Nothing found.");
        assertThat(shown).isEmpty();

        search.search("key");
        search.search("");
        assertThat(search.statusText()).isEmpty();
        assertThat(search.mark(1L, "A key")).isEqualTo("A key");
    }

    @Test
    void refreshKeepsTheMatchTheUserIsAt() {
        search.search("key");
        search.move(2);

        search.refresh(List.of(part(3, "The key, the other key."), part(4, "One more key.")));

        // the second key of the part that is now the first one
        assertThat(search.statusText()).isEqualTo("Match 2 of 3, in part #1");
    }
}
