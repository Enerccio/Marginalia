package com.github.enerccio.marginalia.ui.dialogs.story;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class StoryMassDeleteTest {

    private final AtomicInteger modeChanges = new AtomicInteger();
    private StoryMassDelete massDelete;

    private static ChatMessage part(long id) {
        ChatMessage message = new ChatMessage();
        message.setId(id);
        return message;
    }

    private List<Long> selected() {
        List<Long> ids = new ArrayList<>();
        for (long id = 1; id <= 6; id++) {
            if (massDelete.isSelected(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    @BeforeEach
    void branch() {
        massDelete = new StoryMassDelete(null, modeChanges::incrementAndGet, () -> {
        });
        massDelete.setBranch(List.of(part(1), part(2), part(3), part(4), part(5), part(6)));
    }

    @Test
    void aClickWithoutShiftTicksOnlyThePart() {
        massDelete.clicked(2L, true, false);
        massDelete.clicked(5L, true, false);

        assertThat(selected()).containsExactly(2L, 5L);
    }

    @Test
    void aClickWithShiftTicksEverythingBetweenItAndTheLastClick() {
        massDelete.clicked(2L, true, false);
        massDelete.clicked(5L, true, true);

        assertThat(selected()).containsExactly(2L, 3L, 4L, 5L);
        assertThat(modeChanges.get()).isEqualTo(1);
    }

    @Test
    void theRangeGoesBackwardsToo() {
        massDelete.clicked(5L, true, false);
        massDelete.clicked(3L, true, true);

        assertThat(selected()).containsExactly(3L, 4L, 5L);
    }

    @Test
    void aShiftClickThatClearsClearsTheRange() {
        massDelete.clicked(1L, true, false);
        massDelete.clicked(6L, true, true);
        massDelete.clicked(4L, false, true);

        // the anchor is the part clicked last (6), so the range 4..6 is cleared
        assertThat(selected()).containsExactly(1L, 2L, 3L);
    }

    @Test
    void aShiftClickWithoutAnEarlierClickTicksOnlyThePart() {
        massDelete.clicked(4L, true, true);

        assertThat(selected()).containsExactly(4L);
    }

    @Test
    void partsThatLeftTheBranchAreForgotten() {
        massDelete.clicked(2L, true, false);
        massDelete.clicked(5L, true, false);

        massDelete.setBranch(List.of(part(1), part(5), part(6)));

        assertThat(selected()).containsExactly(5L);
    }
}
