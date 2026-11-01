package com.github.enerccio.marginalia.statistics;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.components.SizeGrowth;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.AgentMergeService.MergeSelection;
import com.github.enerccio.marginalia.domain.service.CleanupService.EntityKey;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan;
import com.github.enerccio.marginalia.domain.service.impl.ManuscriptSizeServiceImpl;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.AopTestUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The size of a book is measured, a moment after something may have changed it, from what its parts have saved (the word and token
 * counts of each part: no tokenizer is called), and kept as one row per book and day. The tests measure by hand ({@code flush}).
 */
class ManuscriptSizeTest extends AgentTestBase {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 28);
    private static final RevisionOrigin COMMAND = RevisionOrigin.automatic(RevisionDetail.COMMAND, true);

    @Autowired
    private ManuscriptSizeService sizeService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private TrashService trashService;

    @Autowired
    private AgentMergeService mergeService;

    @Autowired
    private ActivityTracker tracker;

    private ManuscriptSizeServiceImpl impl;
    private Manuscript book;

    @BeforeEach
    void setUp() throws Exception {
        loginAgentUser();
        impl = (ManuscriptSizeServiceImpl) AopTestUtils.getUltimateTargetObject(sizeService);
        impl.setManual(true);
        day(DAY);
        // whatever other tests left to measure is measured on the day of the test and on its own books
        impl.flush();
        book = manuscript();
    }

    @AfterEach
    void restore() {
        impl.flush();
        impl.setClock(Clock.systemDefaultZone());
        impl.setDelayMillis(ManuscriptSizeServiceImpl.DEFAULT_DELAY_MILLIS);
        impl.setManual(false);
    }

    private void day(LocalDate day) {
        impl.setClock(Clock.fixed(day.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));
    }

    private Manuscript fresh(Manuscript of) throws Exception {
        return manuscriptService.find(of.getId());
    }

    private ManuscriptSize size(Manuscript of, LocalDate day) throws Exception {
        impl.flush();
        return sizeService.sizeAt(of.getUuid(), day);
    }

    private ManuscriptSize size() throws Exception {
        return size(book, DAY);
    }

    private ChatMessage part(ChatMessage parent, String text) throws Exception {
        return chatMessageService.createPart(fresh(book), parent, text, null);
    }

    @Test
    void aNewBookHasItsFirstRowAndItsGrowthIsFromNothing() throws Exception {
        ChatMessage first = part(null, "one two three");
        part(first, "four five");

        ManuscriptSize size = size();

        assertThat(size.getBookWords()).isEqualTo(5);
        assertThat(size.getBranchWords()).isEqualTo(5);
        assertThat(size.getBookTokens()).isPositive();
        assertThat(size.isBaseline()).isFalse();
        assertThat(sizeService.growth(book.getUuid(), DAY, DAY)).isEqualTo(
                new SizeGrowth(5, size.getBookTokens(), 5, size.getBranchTokens(), true));
    }

    @Test
    void aBurstOfChangesIsOneMeasurement() throws Exception {
        ChatMessage last = null;
        for (int i = 0; i < 6; i++) {
            last = part(last, "word word");
        }

        assertThat(sizeService.sizeAt(book.getUuid(), DAY)).as("nothing is measured until the burst is over").isNull();
        impl.flush();

        assertThat(sizeService.findRange(book.getUuid(), DAY, DAY)).hasSize(1);
        assertThat(sizeService.sizeAt(book.getUuid(), DAY).getBookWords()).isEqualTo(12);
    }

    @Test
    void anEditAndADeleteAndARestoreFromTheTrashChangeIt() throws Exception {
        ChatMessage first = part(null, "one two three");
        ChatMessage second = part(first, "four five");
        assertThat(size().getBookWords()).isEqualTo(5);

        chatMessageService.updateResponse(fresh(book), chatMessageService.find(second.getId()), "four five six seven", COMMAND);
        assertThat(size().getBookWords()).isEqualTo(7);

        chatMessageService.delete(chatMessageService.find(second.getId()), false);
        assertThat(size().getBookWords()).isEqualTo(3);

        trashService.restore(List.of(new EntityKey(ChatMessage.class, second.getId())));
        assertThat(size().getBookWords()).isEqualTo(7);
    }

    @Test
    void aNewPartInTheMiddleOfTheStoryCounts() throws Exception {
        ChatMessage first = part(null, "one two three");
        part(first, "four five");
        size();

        chatMessageService.insertPart(fresh(book), chatMessageService.find(first.getId()), "inserted here", null);

        assertThat(size().getBookWords()).isEqualTo(7);
    }

    @Test
    void switchingTheBranchChangesTheBranchAndNotTheWholeBook() throws Exception {
        ChatMessage root = part(null, "one two three");
        ChatMessage a = part(root, "four five");
        ChatMessage b = part(root, "six");
        Manuscript m = fresh(book);
        m.setActiveLeaf(chatMessageService.find(a.getId()));
        manuscriptService.save(m);
        ManuscriptSize onA = size();
        assertThat(onA.getBranchWords()).isEqualTo(5);
        assertThat(onA.getLeafUuid()).isEqualTo(a.getUuid());

        m = fresh(book);
        m.setActiveLeaf(chatMessageService.find(b.getId()));
        manuscriptService.save(m);
        ManuscriptSize onB = size();

        assertThat(onB.getBookWords()).isEqualTo(onA.getBookWords()).isEqualTo(6);
        assertThat(onB.getBranchWords()).isEqualTo(4);
        assertThat(onB.getLeafUuid()).isEqualTo(b.getUuid());
    }

    @Test
    void theMergeOfAnAgentGrowsTheOriginalAndTheCopyIsNotMeasured() throws Exception {
        ChatMessage first = part(null, "one two three");
        Agent agent = agentService.save(newAgent(fresh(book)));
        size();
        AgentRun run = agentRunService.start(agent);
        Manuscript copy = agentRunService.find(run.getId()).getWorkingCopy();
        ChatMessage copied = chatMessageService.getAllMessages(copy).getFirst();
        chatMessageService.createPart(copy, copied, "and a castle", null, COMMAND);
        assertThat(size().getBookWords()).as("the book does not grow while the agent works").isEqualTo(3);
        assertThat(size(copy, DAY)).as("the copy is no book").isNull();

        run = agentRunService.transition(agentRunService.find(run.getId()), AgentRunStatus.DONE, "report");
        MergePlan plan = mergeService.plan(run);
        mergeService.apply(run, new MergeSelection(plan.items().stream().map(MergePlan.Item::id).collect(Collectors.toSet()), Map.of(), false));

        assertThat(size().getBookWords()).isEqualTo(6);
        assertThat(size(copy, DAY)).isNull();
        assertThat(first).isNotNull();
    }

    @Test
    void aChangeOfTheModelLeavesTheStoredTokensAndTheEarlierRowsAlone() throws Exception {
        part(part(null, "one two three"), "four five");
        ManuscriptSize before = size();
        long tokens = before.getBookTokens();
        assertThat(tokens).isPositive();

        day(DAY.plusDays(1));
        Manuscript m = fresh(book);
        m.setAi(createAI());
        manuscriptService.save(m);
        ManuscriptSize after = size(book, DAY.plusDays(1));

        assertThat(after.getBookTokens()).isEqualTo(tokens);
        assertThat(size(book, DAY).getBookTokens()).isEqualTo(tokens);
        assertThat(sizeService.growth(book.getUuid(), DAY.plusDays(1), DAY.plusDays(1)).bookTokens()).isZero();
    }

    @Test
    void aBookThatWasThereBeforeGetsABaselineAndNoGrowthIsShownForTheDayItWasSeen() throws Exception {
        ChatMessage first = part(null, "one two three");
        size();
        // as if the statistics did not exist when the book was written
        sizeService.clearBook(book.getUuid());
        impl.touch(fresh(book));

        ManuscriptSize seen = size();
        assertThat(seen.isBaseline()).isTrue();
        assertThat(seen.getBookWords()).isEqualTo(3);
        assertThat(sizeService.growth(book.getUuid(), DAY, DAY)).isEqualTo(SizeGrowth.UNKNOWN);

        part(first, "four five");
        assertThat(size().isBaseline()).as("the rest of the day stays unknown").isTrue();

        day(DAY.plusDays(1));
        part(chatMessageService.find(first.getId()), "six");
        ManuscriptSize next = size(book, DAY.plusDays(1));
        assertThat(next.isBaseline()).isFalse();
        assertThat(sizeService.growth(book.getUuid(), DAY.plusDays(1), DAY.plusDays(1)).bookWords()).isEqualTo(1);
        assertThat(sizeService.growth(book.getUuid(), DAY, DAY.plusDays(1)).bookWords())
                .as("measured from the row the book was first seen with (what it was at the end of that day)").isEqualTo(1);
    }

    @Test
    void aDayWithoutARowCarriesTheSizeBeforeIt() throws Exception {
        ChatMessage first = part(null, "one two three");
        size();
        day(DAY.plusDays(2));
        part(first, "four five six");
        size(book, DAY.plusDays(2));

        assertThat(sizeService.sizeAt(book.getUuid(), DAY.plusDays(1)).getBookWords()).isEqualTo(3);
        assertThat(sizeService.growth(book.getUuid(), DAY.plusDays(1), DAY.plusDays(1)).bookWords()).isZero();
        assertThat(sizeService.growth(book.getUuid(), DAY.plusDays(2), DAY.plusDays(2)).bookWords()).isEqualTo(3);
        assertThat(sizeService.growth(book.getUuid(), DAY, DAY.plusDays(2)).bookWords()).as("from nothing, the book was born in the range").isEqualTo(6);
        assertThat(sizeService.growth(book.getUuid(), DAY.minusDays(5), DAY.minusDays(1))).isEqualTo(SizeGrowth.ZERO);
    }

    @Test
    void shrinkingIsNegativeGrowth() throws Exception {
        ChatMessage first = part(null, "one two three");
        ChatMessage second = part(first, "four five six seven");
        size();
        day(DAY.plusDays(1));
        chatMessageService.delete(chatMessageService.find(second.getId()), false);
        size(book, DAY.plusDays(1));

        assertThat(sizeService.growth(book.getUuid(), DAY.plusDays(1), DAY.plusDays(1)).bookWords()).isEqualTo(-4);
    }

    @Test
    void aDeletedBookKeepsItsSize() throws Exception {
        part(null, "one two three");
        size();

        manuscriptService.delete(fresh(book), false);
        impl.touch(book);

        assertThat(size().getBookWords()).isEqualTo(3);
    }

    @Test
    void theWorkerNeedsNoSession() throws Exception {
        part(part(null, "one two"), "three");
        Thread worker = new Thread(() -> {
            impl.touch(book);
            impl.flush();
        });
        worker.start();
        worker.join();

        assertThat(sizeService.sizeAt(book.getUuid(), DAY).getBookWords()).isEqualTo(3);
    }

    @Test
    void openingAndClosingTheBookMeasuresIt() throws Exception {
        part(null, "one two three");
        size();
        sizeService.clearBook(book.getUuid());
        assertThat(sizeService.sizeAt(book.getUuid(), DAY)).isNull();

        ActivityTracker.BookPresence presence = tracker.open(fresh(book));
        presence.close();

        assertThat(size().getBookWords()).isEqualTo(3);
    }

    @Test
    void nothingToChangeWritesNothing() throws Exception {
        part(null, "one two three");
        ManuscriptSize first = size();

        impl.touch(book);
        ManuscriptSize again = size();

        assertThat(again.getId()).isEqualTo(first.getId());
        assertThat(again.getLastChange()).isEqualTo(first.getLastChange());
    }

    @Test
    void theWorkerMeasuresByItselfAfterTheBurst() throws Exception {
        impl.setManual(false);
        impl.setDelayMillis(150);
        ChatMessage last = null;
        for (int i = 0; i < 4; i++) {
            last = part(last, "word word");
        }

        ManuscriptSize size = null;
        for (int i = 0; i < 100 && (size == null || size.getBookWords() != 8); i++) {
            Thread.sleep(50);
            size = sizeService.sizeAt(book.getUuid(), DAY);
        }

        assertThat(size).isNotNull();
        assertThat(size.getBookWords()).isEqualTo(8);
        assertThat(sizeService.findRange(book.getUuid(), DAY, DAY)).hasSize(1);
    }
}
