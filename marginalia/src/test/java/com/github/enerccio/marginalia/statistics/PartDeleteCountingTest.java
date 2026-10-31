package com.github.enerccio.marginalia.statistics;

import com.github.enerccio.marginalia.domain.collections.StatisticSide;
import com.github.enerccio.marginalia.domain.components.PartMerge;
import com.github.enerccio.marginalia.domain.components.StatisticCounts;
import com.github.enerccio.marginalia.domain.components.StatisticFilter;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.CleanupService.EntityKey;
import com.github.enerccio.marginalia.domain.service.ManuscriptStatisticService;
import com.github.enerccio.marginalia.domain.service.TrashService;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The parts and words a delete takes away are counted where the parts are deleted ({@code ChatMessageService}): a delete by hand
 * is work, deleting for good, restoring from the trash and the delete of a merge are not.
 */
class PartDeleteCountingTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptStatisticService statisticService;

    @Autowired
    private TrashService trashService;

    private Manuscript manuscript;
    private ChatMessage first;
    private ChatMessage second;
    private ChatMessage third;
    private ChatMessage branch;

    @BeforeEach
    void createBook() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
        first = chatMessageService.createPart(book(), null, "One two", null);
        second = chatMessageService.createPart(book(), first, "Three four five", null);
        third = chatMessageService.createPart(book(), second, "Six", null);
        branch = chatMessageService.createPart(book(), second, "Seven eight nine ten", null);
    }

    private Manuscript book() throws Exception {
        return manuscriptService.find(manuscript.getId());
    }

    private StatisticCounts counts() throws Exception {
        return statisticService.total(StatisticFilter.of(statisticService.today(), statisticService.today(), StatisticSide.ALL)
                .forBook(manuscript.getUuid()));
    }

    private ChatMessage stored(ChatMessage part) throws Exception {
        return chatMessageService.find(part.getId());
    }

    @Test
    void aDeletedPartTakesItsDescendantsAndTheirWordsIntoTheCount() throws Exception {
        StatisticCounts before = counts();

        chatMessageService.delete(stored(second), false);

        StatisticCounts after = counts();
        assertThat(after.partsDeleted() - before.partsDeleted()).isEqualTo(3);
        assertThat(after.wordsRemoved() - before.wordsRemoved()).isEqualTo(3 + 1 + 4);
        assertThat(after.partsCreated()).isEqualTo(before.partsCreated());
        assertThat(after.wordsWritten()).isEqualTo(before.wordsWritten());
    }

    @Test
    void deletingAPartThatIsDeletedAlreadyCountsNothing() throws Exception {
        chatMessageService.delete(stored(second), false);
        StatisticCounts before = counts();

        chatMessageService.delete(stored(second), false);
        chatMessageService.delete(stored(third), false);

        assertThat(counts()).isEqualTo(before);
    }

    @Test
    void deletingOnePartAndMovingItsChildrenCountsThatPartOnly() throws Exception {
        StatisticCounts before = counts();

        chatMessageService.deleteNodeAndMigrateChildren(stored(second), book(), false);

        StatisticCounts after = counts();
        assertThat(after.partsDeleted() - before.partsDeleted()).isEqualTo(1);
        assertThat(after.wordsRemoved() - before.wordsRemoved()).isEqualTo(3);
        assertThat(chatMessageService.find(third.getId()).isDeleted()).isFalse();
    }

    @Test
    void deletingForGoodCountsNothing() throws Exception {
        StatisticCounts before = counts();

        chatMessageService.deleteNodeAndMigrateChildren(stored(third), book(), true);

        assertThat(counts()).isEqualTo(before);
    }

    @Test
    void theTrashIsNotCountedAgainWhenRestoredOrEmptied() throws Exception {
        chatMessageService.delete(stored(third), false);
        StatisticCounts deleted = counts();

        trashService.restore(List.of(new EntityKey(ChatMessage.class, third.getId())));
        assertThat(counts()).isEqualTo(deleted);

        chatMessageService.delete(stored(third), false);
        StatisticCounts deletedAgain = counts();
        assertThat(deletedAgain.partsDeleted()).isEqualTo(deleted.partsDeleted() + 1);
        trashService.deletePermanently(List.of(new EntityKey(ChatMessage.class, third.getId())));
        assertThat(counts()).isEqualTo(deletedAgain);
    }

    @Test
    void aMergeDeletesThePartButItsWordsAreNotTakenAway() throws Exception {
        StatisticCounts before = counts();

        chatMessageService.mergeIntoPrevious(book(), stored(second), null, PartMerge.Join.SPACE, Set.of(), Set.of());

        StatisticCounts after = counts();
        assertThat(after.partsDeleted() - before.partsDeleted()).isEqualTo(1);
        assertThat(after.wordsRemoved()).isEqualTo(before.wordsRemoved());
        assertThat(after.wordsWritten()).isEqualTo(before.wordsWritten());
        assertThat(after.edits() - before.edits()).isEqualTo(1);
    }

    @Test
    void theDeleteOfAnAgentMergeIsNotCounted() throws Exception {
        StatisticCounts before = counts();

        chatMessageService.deleteNodeAndMigrateChildren(stored(third), book(), false, false);

        assertThat(counts()).isEqualTo(before);
        assertThat(chatMessageService.find(third.getId()).isDeleted()).isTrue();
    }
}
