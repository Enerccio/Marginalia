package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Which parts of a branch the story (tail) and the reader (head) have loaded while the window slides, no UI involved.
 */
class LazyBranchTest {

    private static List<ChatMessage> branch(int size) {
        List<ChatMessage> branch = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ChatMessage message = new ChatMessage();
            message.setId((long) i + 1);
            branch.add(message);
        }
        return branch;
    }

    private static List<Long> ids(List<ChatMessage> messages) {
        return messages.stream().map(ChatMessage::getId).toList();
    }

    @Test
    void tailStartsWithTheLastParts() {
        LazyBranch window = LazyBranch.tail(branch(10), 4);

        assertThat(ids(window.loaded())).containsExactly(7L, 8L, 9L, 10L);
        assertThat(window.offset()).isEqualTo(6);
        assertThat(window.total()).isEqualTo(10);
        assertThat(window.hasEarlier()).isTrue();
        assertThat(window.hasLater()).isFalse();
        assertThat(window.atTail()).isTrue();
    }

    @Test
    void scrollingUpLoadsHalfOfTheWindowAndDropsTheSameFromTheEnd() {
        LazyBranch window = LazyBranch.tail(branch(10), 4);

        assertThat(ids(window.loadEarlier())).containsExactly(5L, 6L);
        assertThat(ids(window.trimLater())).containsExactly(9L, 10L);
        assertThat(ids(window.loaded())).containsExactly(5L, 6L, 7L, 8L);
        assertThat(window.offset()).isEqualTo(4);
        assertThat(window.hasLater()).isTrue();
        assertThat(window.atTail()).isFalse();

        assertThat(ids(window.loadEarlier())).containsExactly(3L, 4L);
        assertThat(ids(window.trimLater())).containsExactly(7L, 8L);
        assertThat(ids(window.loadEarlier())).containsExactly(1L, 2L);
        assertThat(ids(window.trimLater())).containsExactly(5L, 6L);
        assertThat(ids(window.loaded())).containsExactly(1L, 2L, 3L, 4L);
        assertThat(window.hasEarlier()).isFalse();
        assertThat(window.loadEarlier()).isEmpty();
        assertThat(window.trimLater()).isEmpty();
    }

    @Test
    void scrollingBackDownLoadsTheDroppedPartsAgain() {
        LazyBranch window = LazyBranch.tail(branch(10), 4);
        window.loadEarlier();
        window.trimLater();

        assertThat(ids(window.loadLater())).containsExactly(9L, 10L);
        assertThat(ids(window.trimEarlier())).containsExactly(5L, 6L);
        assertThat(ids(window.loaded())).containsExactly(7L, 8L, 9L, 10L);
        assertThat(window.atTail()).isTrue();
        assertThat(window.loadLater()).isEmpty();
    }

    @Test
    void headStartsWithTheFirstPartsAndSlidesLater() {
        LazyBranch window = LazyBranch.head(branch(10), 4);

        assertThat(ids(window.loaded())).containsExactly(1L, 2L, 3L, 4L);
        assertThat(window.hasEarlier()).isFalse();
        assertThat(window.hasLater()).isTrue();
        assertThat(ids(window.loadLater())).containsExactly(5L, 6L);
        assertThat(ids(window.trimEarlier())).containsExactly(1L, 2L);
        assertThat(ids(window.loadLater())).containsExactly(7L, 8L);
        assertThat(ids(window.trimEarlier())).containsExactly(3L, 4L);
        assertThat(ids(window.loadLater())).containsExactly(9L, 10L);
        assertThat(ids(window.trimEarlier())).containsExactly(5L, 6L);
        assertThat(window.hasLater()).isFalse();
        assertThat(ids(window.loaded())).containsExactly(7L, 8L, 9L, 10L);
    }

    @Test
    void theWindowKeepsGrowingUntilItIsTrimmed() {
        LazyBranch window = LazyBranch.tail(branch(10), 4);

        window.loadEarlier();
        window.loadEarlier();
        assertThat(ids(window.loaded())).hasSize(8).startsWith(3L).endsWith(10L);

        assertThat(ids(window.trimLater())).containsExactly(7L, 8L, 9L, 10L);
        assertThat(ids(window.loaded())).containsExactly(3L, 4L, 5L, 6L);
    }

    @Test
    void aWindowOfOnePartSlidesByOne() {
        LazyBranch window = LazyBranch.tail(branch(3), 1);

        assertThat(ids(window.loaded())).containsExactly(3L);
        assertThat(ids(window.loadEarlier())).containsExactly(2L);
        assertThat(ids(window.trimLater())).containsExactly(3L);
        assertThat(ids(window.loaded())).containsExactly(2L);
    }

    @Test
    void noLimitLoadsEverything() {
        for (Integer limit : new Integer[]{null, 0, -3}) {
            LazyBranch tail = LazyBranch.tail(branch(10), limit);
            assertThat(tail.loaded()).hasSize(10);
            assertThat(tail.hasEarlier()).isFalse();

            LazyBranch head = LazyBranch.head(branch(10), limit);
            assertThat(head.loaded()).hasSize(10);
            assertThat(head.hasLater()).isFalse();
        }
    }

    @Test
    void limitLargerThanTheBranchLoadsEverything() {
        LazyBranch window = LazyBranch.tail(branch(3), 50);

        assertThat(window.loaded()).hasSize(3);
        assertThat(window.hasEarlier()).isFalse();
    }

    @Test
    void emptyBranch() {
        assertThat(LazyBranch.tail(List.of(), 5).loaded()).isEmpty();
        assertThat(LazyBranch.head(List.of(), 5).loaded()).isEmpty();
    }

    @Test
    void centersOnAPartThatIsNotLoaded() {
        LazyBranch window = LazyBranch.tail(branch(20), 6);

        assertThat(window.centerOn(4L)).isTrue();
        assertThat(ids(window.loaded())).containsExactly(1L, 2L, 3L, 4L, 5L, 6L);

        assertThat(window.centerOn(12L)).isTrue();
        assertThat(ids(window.loaded())).containsExactly(9L, 10L, 11L, 12L, 13L, 14L);
        assertThat(window.offset()).isEqualTo(8);
        assertThat(window.isLoaded(12L)).isTrue();
        assertThat(window.isLoaded(1L)).isFalse();
    }

    @Test
    void centeringNearTheEndStopsAtTheEnd() {
        LazyBranch window = LazyBranch.head(branch(20), 6);

        assertThat(window.centerOn(19L)).isTrue();
        assertThat(ids(window.loaded())).containsExactly(15L, 16L, 17L, 18L, 19L, 20L);
        assertThat(window.atTail()).isTrue();
    }

    @Test
    void centeringOnALoadedOrUnknownPartChangesNothing() {
        LazyBranch window = LazyBranch.tail(branch(20), 5);

        assertThat(window.centerOn(20L)).isFalse();
        assertThat(window.centerOn(999L)).isFalse();
        assertThat(window.centerOn(null)).isFalse();
        assertThat(window.offset()).isEqualTo(15);
    }

    @Test
    void movesBackToTheTail() {
        LazyBranch window = LazyBranch.tail(branch(20), 5);
        window.centerOn(2L);

        window.moveToTail();

        assertThat(ids(window.loaded())).containsExactly(16L, 17L, 18L, 19L, 20L);
        assertThat(window.atTail()).isTrue();
    }

    @Test
    void refreshReplacesThePartByTheNewInstance() {
        LazyBranch window = LazyBranch.tail(branch(5), 5);
        ChatMessage edited = new ChatMessage();
        edited.setId(3L);
        edited.setResponse("edited");

        window.refresh(edited);
        window.refresh(null);
        ChatMessage unknown = new ChatMessage();
        unknown.setId(99L);
        window.refresh(unknown);

        assertThat(window.loaded().get(2)).isSameAs(edited);
        assertThat(window.loaded()).hasSize(5);
    }
}
