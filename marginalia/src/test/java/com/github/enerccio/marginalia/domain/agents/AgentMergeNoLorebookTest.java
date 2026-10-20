package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.AgentMergeService;
import com.github.enerccio.marginalia.domain.service.AgentMergeService.MergeSelection;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Change;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Item;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan.Kind;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A book that has no lorebook when the agent starts: the agent makes the root lorebook in its copy, and taking that over
 * gives the book its first lorebook.
 */
class AgentMergeNoLorebookTest extends AgentTestBase {

    @Autowired
    private AgentMergeService mergeService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Test
    void theRootLorebookTheAgentMadeBecomesTheLorebookOfTheBook() throws Exception {
        loginAgentUser();
        Manuscript book = manuscriptService.save(manuscript());
        assertThat(book.getLorebook()).isNull();
        Agent agent = agentService.save(newAgent(book));
        AgentRun run = agentRunService.start(agent);

        Manuscript copy = agentRunService.find(run.getId()).getWorkingCopy();
        Lorebook root = new Lorebook();
        root.setName("lore");
        root.setAgentic(copy.isAgentic());
        root.setSubbooks(new ArrayList<>());
        copy.setLorebook(lorebookService.save(root));
        copy = manuscriptService.save(copy);
        LorebookEntry entry = new LorebookEntry();
        entry.setName("Lily");
        entry.setPayload("a witch");
        lorebookEntryService.create(copy.getLorebook(), entry);

        run = agentRunService.transition(agentRunService.find(run.getId()), AgentRunStatus.DONE, "report");
        MergePlan plan = mergeService.plan(run);
        assertThat(plan.items().stream().filter(i -> i.kind() == Kind.LOREBOOK && i.change() == Change.ADDED)).hasSize(1);
        assertThat(plan.items().stream().filter(i -> i.kind() == Kind.ENTRY && i.change() == Change.ADDED)).hasSize(1);

        mergeService.apply(run, new MergeSelection(plan.items().stream().map(Item::id).collect(Collectors.toSet()), Map.of(), false));

        Manuscript merged = manuscriptService.find(book.getId());
        assertThat(merged.getLorebook()).as("the book has a lorebook now").isNotNull();
        assertThat(lorebookEntryService.getEntriesForLorebook(lorebookService.find(merged.getLorebook())))
                .extracting(LorebookEntry::getName).containsExactly("Lily");
    }
}
