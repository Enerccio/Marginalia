package com.github.enerccio.marginalia.statistics;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.collections.StatisticSide;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.components.StatisticCounts;
import com.github.enerccio.marginalia.domain.components.StatisticFilter;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.AgentMergeService.MergeSelection;
import com.github.enerccio.marginalia.domain.service.agent.CheckpointStore;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * What an agent does is counted against the book it works on, on the agents' side, although it works on a hidden copy; the
 * merge into the book, making the copy and dropping it count nothing, and the author's side does not move while a run holds the
 * book.
 */
class AgentStatisticsTest extends AgentTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptStatisticService statisticService;

    @Autowired
    private AgentMergeService mergeService;

    @Autowired
    private CheckpointService checkpointService;

    @Autowired
    private UsageService usageService;

    @Autowired
    private AgentRunner runner;

    private Manuscript book;
    private Agent agent;
    private ChatMessage first;
    private ChatMessage second;

    @BeforeEach
    void setUp() throws Exception {
        loginAgentUser();
        book = manuscript();
        first = chatMessageService.createPart(book, null, "Once upon a time", null);
        second = chatMessageService.createPart(book, first, "there was a dragon", null);
        agent = agentService.save(newAgent(book));
    }

    private StatisticCounts counts(StatisticSide side) throws Exception {
        return counts(book, side);
    }

    private StatisticCounts counts(Manuscript of, StatisticSide side) throws Exception {
        return statisticService.total(StatisticFilter.of(statisticService.today(), statisticService.today(), side).forBook(of.getUuid()));
    }

    private Manuscript copyOf(AgentRun run) throws Exception {
        return agentRunService.find(run.getId()).getWorkingCopy();
    }

    private ChatMessage partOfCopy(AgentRun run, String text) throws Exception {
        return chatMessageService.getAllMessages(copyOf(run)).stream().filter(m -> text.equals(m.getResponse())).findFirst().orElseThrow();
    }

    private static final RevisionOrigin COMMAND = RevisionOrigin.automatic(RevisionDetail.COMMAND, true);

    @Test
    void makingTheCopyCountsNothingForTheCopyOrTheBook() throws Exception {
        StatisticCounts author = counts(StatisticSide.AUTHOR);

        AgentRun run = agentRunService.start(agent);

        Manuscript copy = copyOf(run);
        assertThat(copy.isAgentic()).isTrue();
        assertThat(counts(StatisticSide.AUTHOR)).isEqualTo(author);
        assertThat(counts(StatisticSide.AGENTS)).isEqualTo(StatisticCounts.ZERO);
        assertThat(counts(copy, StatisticSide.ALL)).isEqualTo(StatisticCounts.ZERO);
    }

    @Test
    void whatTheAgentDoesOnTheCopyIsTheAgentsSideOfTheBook() throws Exception {
        StatisticCounts author = counts(StatisticSide.AUTHOR);
        AgentRun run = agentRunService.start(agent);
        Manuscript copy = copyOf(run);

        ChatMessage added = chatMessageService.createPart(copy, partOfCopy(run, "there was a dragon"), "and a castle", null, COMMAND);
        chatMessageService.updateResponse(copy, partOfCopy(run, "Once upon a time"), "Once upon a long time ago", COMMAND);
        chatMessageService.delete(chatMessageService.find(added.getId()), false);

        assertThat(counts(StatisticSide.AGENTS)).isEqualTo(new StatisticCounts(0, 0, 0, 3 + 2, 0, 3, 1, 1, 1));
        assertThat(counts(StatisticSide.AUTHOR)).isEqualTo(author);
        assertThat(counts(copy, StatisticSide.ALL)).as("nothing is booked for the copy").isEqualTo(StatisticCounts.ZERO);
        assertThat(counts(StatisticSide.ALL).wordsWritten()).isEqualTo(author.wordsWritten() + 5);
    }

    @Test
    void theTokensOfTheAgentAreAddedToTheAgentsSide() throws Exception {
        AgentRun run = agentRunService.start(agent);
        StatisticCounts author = counts(StatisticSide.AUTHOR);

        try (UsageContext _ = UsageContext.open(copyOf(run))) {
            usageService.record(createAI(), 500, 50);
        }

        assertThat(counts(StatisticSide.AGENTS)).isEqualTo(StatisticCounts.tokens(500, 50));
        assertThat(counts(StatisticSide.AUTHOR)).isEqualTo(author);
    }

    @Test
    void aCopyThatNoRunHasIsNotCounted() throws Exception {
        Manuscript orphan = manuscript();
        orphan.setAgentic(true);
        orphan = manuscriptService.save(orphan);

        chatMessageService.createPart(orphan, null, "Nobody owns this", null, COMMAND);

        assertThat(counts(orphan, StatisticSide.ALL)).isEqualTo(StatisticCounts.ZERO);
        assertThat(counts(StatisticSide.AGENTS)).isEqualTo(StatisticCounts.ZERO);
    }

    @Test
    void theMergeIntoTheBookAddsNothing() throws Exception {
        AgentRun run = agentRunService.start(agent);
        Manuscript copy = copyOf(run);
        chatMessageService.createPart(copy, partOfCopy(run, "there was a dragon"), "and a castle", null, COMMAND);
        chatMessageService.updateResponse(copy, partOfCopy(run, "Once upon a time"), "Once upon a long time ago", COMMAND);
        chatMessageService.delete(partOfCopy(run, "there was a dragon"), false);
        StatisticCounts agents = counts(StatisticSide.AGENTS);
        StatisticCounts author = counts(StatisticSide.AUTHOR);

        run = agentRunService.transition(agentRunService.find(run.getId()), AgentRunStatus.DONE, "report");
        MergePlan plan = mergeService.plan(run);
        assertThat(plan.items()).isNotEmpty();
        mergeService.apply(run, new MergeSelection(plan.items().stream().map(MergePlan.Item::id).collect(Collectors.toSet()), Map.of(), false));

        assertThat(chatMessageService.getAllMessages(manuscriptService.find(book.getId())))
                .extracting(ChatMessage::getResponse).containsExactly("Once upon a long time ago");
        assertThat(counts(StatisticSide.AGENTS)).isEqualTo(agents);
        assertThat(counts(StatisticSide.AUTHOR)).isEqualTo(author);
    }

    @Test
    void aDiscardedRunKeepsWhatItSpent() throws Exception {
        AgentRun run = agentRunService.start(agent);
        Manuscript copy = copyOf(run);
        chatMessageService.createPart(copy, partOfCopy(run, "there was a dragon"), "and a castle", null, COMMAND);
        try (UsageContext _ = UsageContext.open(copy)) {
            usageService.record(createAI(), 500, 50);
        }
        StatisticCounts agents = counts(StatisticSide.AGENTS);
        assertThat(agents.requests()).isEqualTo(1);

        agentRunService.delete(agentRunService.find(run.getId()), false);

        assertThat(manuscriptService.find(copy.getId())).as("the copy is gone").isNull();
        assertThat(counts(StatisticSide.AGENTS)).isEqualTo(agents);
    }

    @Test
    void restoringACheckpointAndDroppingTheOldCopyCountNothing() throws Exception {
        AgentRun run = agentRunService.start(agent);
        CheckpointStore.Info checkpoint = checkpointService.ensureStart(run);
        chatMessageService.createPart(copyOf(run), partOfCopy(run, "there was a dragon"), "and a castle", null, COMMAND);
        StatisticCounts agents = counts(StatisticSide.AGENTS);
        Long oldCopy = copyOf(run).getId();

        checkpointService.restore(run, checkpoint.id());

        assertThat(copyOf(run).getId()).isNotEqualTo(oldCopy);
        assertThat(manuscriptService.find(oldCopy)).isNull();
        assertThat(counts(StatisticSide.AGENTS)).isEqualTo(agents);
        assertThat(counts(StatisticSide.AUTHOR).partsCreated()).isEqualTo(2);
        assertThat(counts(copyOf(run), StatisticSide.ALL)).isEqualTo(StatisticCounts.ZERO);
    }

    @Test
    void theBookIsLockedSoTheAuthorsSideDoesNotMoveWhileTheAgentWorks() throws Exception {
        StatisticCounts author = counts(StatisticSide.AUTHOR);
        agentRunService.start(agent);

        assertThatThrownBy(() -> chatMessageService.createPart(book, second, "The author interrupts", null))
                .isInstanceOf(ManuscriptLockedException.class);
        assertThatThrownBy(() -> chatMessageService.delete(chatMessageService.find(second.getId()), false))
                .isInstanceOf(ManuscriptLockedException.class);

        assertThat(counts(StatisticSide.AUTHOR)).isEqualTo(author);
    }

    @Test
    void aBookHasOneUnfinishedRunSoOneAgentSideAtATime() throws Exception {
        agentRunService.start(agent);

        assertThatThrownBy(() -> agentRunService.start(agent)).isInstanceOf(IllegalStateException.class);
        assertThat(agentRunService.findOriginalOfCopy(copyOf(agentRunService.findUnfinished(agent))).getId()).isEqualTo(book.getId());
        assertThat(List.of(counts(StatisticSide.AGENTS))).hasSize(1);
    }

    @Test
    void theModelCallsOfARealRunAreTheAgentsTokensOfTheBook() throws Exception {
        agent.setSystemPrompt("You are a test agent.");
        agent.setInstructions("Count the parts.");
        agent.setAllowedCommands(List.of("get-book-info"));
        agent = agentService.save(agent);
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}").usage(80, 40),
                MockLLMResponse.toolCall("finish", "{\"summary\":\"done\"}").usage(300, 20));

        AgentRun run = agentRunService.start(agent);
        runner.run(run);
        long end = System.currentTimeMillis() + 20_000;
        while (System.currentTimeMillis() < end && (agentRunService.find(run.getId()).getStatus() == AgentRunStatus.RUNNING
                || runner.isWorking(agentRunService.find(run.getId())))) {
            Thread.sleep(25);
        }

        StatisticCounts agents = counts(StatisticSide.AGENTS);
        assertThat(agents.requests()).isEqualTo(2);
        assertThat(agents.prefillTokens()).isEqualTo(380);
        assertThat(agents.generatedTokens()).isEqualTo(60);
        assertThat(counts(StatisticSide.AUTHOR).requests()).isZero();
    }
}
