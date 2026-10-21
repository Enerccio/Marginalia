package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Which parts of a branch the story (tail) and the reader (head) have loaded, no UI involved.
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
    }

    @Test
    void tailLoadsEarlierChunksUntilTheStart() {
        LazyBranch window = LazyBranch.tail(branch(10), 4);

        assertThat(ids(window.loadEarlier())).containsExactly(3L, 4L, 5L, 6L);
        assertThat(window.offset()).isEqualTo(2);
        assertThat(ids(window.loadEarlier())).containsExactly(1L, 2L);
        assertThat(window.offset()).isZero();
        assertThat(window.hasEarlier()).isFalse();
        assertThat(window.loadEarlier()).isEmpty();
        assertThat(ids(window.loaded())).hasSize(10);
    }

    @Test
    void headStartsWithTheFirstPartsAndLoadsLater() {
        LazyBranch window = LazyBranch.head(branch(10), 4);

        assertThat(ids(window.loaded())).containsExactly(1L, 2L, 3L, 4L);
        assertThat(window.hasEarlier()).isFalse();
        assertThat(window.hasLater()).isTrue();
        assertThat(ids(window.loadLater())).containsExactly(5L, 6L, 7L, 8L);
        assertThat(ids(window.loadLater())).containsExactly(9L, 10L);
        assertThat(window.hasLater()).isFalse();
        assertThat(window.loadLater()).isEmpty();
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
    void loadsEarlierThroughAPart() {
        LazyBranch window = LazyBranch.tail(branch(20), 5);

        // part 4 is at index 3, the window starts at index 15: three chunks of five are needed
        assertThat(ids(window.loadEarlierThrough(4L))).hasSize(15).startsWith(1L).endsWith(15L);
        assertThat(window.offset()).isZero();
        assertThat(window.loadEarlierThrough(4L)).isEmpty();
    }

    @Test
    void loadsEarlierThroughAPartInOneChunk() {
        LazyBranch window = LazyBranch.tail(branch(20), 5);

        assertThat(ids(window.loadEarlierThrough(12L))).containsExactly(11L, 12L, 13L, 14L, 15L);
        assertThat(window.offset()).isEqualTo(10);
    }

    @Test
    void loadingThroughALoadedOrUnknownPartChangesNothing() {
        LazyBranch window = LazyBranch.tail(branch(20), 5);

        assertThat(window.loadEarlierThrough(20L)).isEmpty();
        assertThat(window.loadEarlierThrough(999L)).isEmpty();
        assertThat(window.loadEarlierThrough(null)).isEmpty();
        assertThat(window.offset()).isEqualTo(15);
    }
}
