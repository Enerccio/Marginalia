package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookLockedException;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The lorebooks of the book of an agent (the one it links and, however deep, its subbooks) are locked with the book:
 * reading is allowed, changing is not, the working copy of the run is free, and a paused or finished run lets go.
 */
class LorebookLockTest extends AgentTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService entryService;

    private Lorebook root, sub, deep, unrelated;
    private LorebookEntry entry;
    private Agent agent;

    private Lorebook lorebook(String name) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(uniqueName(name));
        lorebook.setSubbooks(new ArrayList<>());
        return lorebookService.save(lorebook);
    }

    @BeforeEach
    void setUp() throws Exception {
        loginAgentUser();
        deep = lorebook("deep");
        sub = lorebook("sub");
        sub.setSubbooks(new ArrayList<>(List.of(deep)));
        sub = lorebookService.save(sub);
        root = lorebook("root");
        root.setSubbooks(new ArrayList<>(List.of(sub)));
        root = lorebookService.save(root);
        unrelated = lorebook("unrelated");

        entry = new LorebookEntry();
        entry.setLorebook(deep);
        entry.setName("Harry");
        entry = entryService.save(entry);

        Manuscript book = manuscript();
        book.setLorebook(root);
        book = manuscriptService.save(book);
        agent = agentService.save(newAgent(book));
    }

    @Test
    void aLorebookWithoutARunIsFree() throws Exception {
        assertThat(agentRunService.findLockingRun(root)).isNull();

        root.setName("renamed");
        assertThat(lorebookService.save(root).getName()).isEqualTo("renamed");
    }

    @Test
    void theWholeTreeIsLockedWhileTheRunWorks() throws Exception {
        AgentRun run = agentRunService.start(agent);

        for (Lorebook locked : List.of(root, sub, deep)) {
            assertThat(agentRunService.findLockingRun(locked).getId()).as(locked.getName()).isEqualTo(run.getId());
        }
        assertThat(agentRunService.findLockingRun(unrelated)).isNull();

        Lorebook stored = lorebookService.find(deep);
        stored.setName("renamed");
        assertThatThrownBy(() -> lorebookService.save(stored)).isInstanceOf(LorebookLockedException.class)
                .hasMessageContaining(agent.getName());
        assertThatThrownBy(() -> lorebookService.delete(root, false)).isInstanceOf(LorebookLockedException.class);
        assertThatThrownBy(() -> lorebookService.addSubbook(sub, unrelated)).isInstanceOf(LorebookLockedException.class);

        LorebookEntry changed = entryService.find(entry);
        changed.setPayload("changed");
        assertThatThrownBy(() -> entryService.save(changed)).isInstanceOf(LorebookLockedException.class);
        assertThatThrownBy(() -> entryService.delete(changed, false)).isInstanceOf(LorebookLockedException.class);
        LorebookEntry fresh = new LorebookEntry();
        fresh.setName("new");
        assertThatThrownBy(() -> entryService.create(deep, fresh)).isInstanceOf(LorebookLockedException.class);

        // reading goes on, and so do lorebooks of nothing the agent works on
        assertThat(lorebookService.find(deep).getName()).isNotEqualTo("renamed");
        assertThat(entryService.getEntriesForLorebook(deep)).hasSize(1);
        unrelated.setName("renamed");
        lorebookService.save(unrelated);
    }

    @Test
    void theWorkingCopyOfTheRunIsNotLocked() throws Exception {
        AgentRun run = agentRunService.start(agent);
        Lorebook copy = lorebookService.find(agentRunService.find(run.getId()).getWorkingCopy().getLorebook());
        assertThat(copy.isAgentic()).isTrue();

        assertThat(agentRunService.findLockingRun(copy)).isNull();
        copy.setName("changed by the agent");
        assertThat(lorebookService.save(copy).getName()).isEqualTo("changed by the agent");
        LorebookEntry copied = entryService.getEntriesForLorebook(lorebookService.getSubbooks(lorebookService.getSubbooks(copy).getFirst()).getFirst()).getFirst();
        copied.setPayload("changed by the agent");
        assertThat(entryService.save(copied).getPayload()).isEqualTo("changed by the agent");
    }

    @Test
    void pausingAndFinishingFreeTheLorebooks() throws Exception {
        AgentRun run = agentRunService.start(agent);
        assertThat(agentRunService.findLockingRun(deep)).isNotNull();

        run = agentRunService.pause(run);
        assertThat(agentRunService.findLockingRun(deep)).as("paused").isNull();
        deep.setName("while paused");
        lorebookService.save(deep);

        run = agentRunService.resume(run);
        assertThat(agentRunService.findLockingRun(deep)).as("resumed").isNotNull();
        agentRunService.stop(run, true);
        assertThat(agentRunService.findLockingRun(deep)).as("stopped").isNull();
    }

    @Test
    void aDeletedAgentDoesNotLock() throws Exception {
        agentRunService.start(agent);
        agentService.delete(agent, false);

        assertThat(agentRunService.findLockingRun(root)).isNull();
    }

    @Test
    void aLorebookSharedByAnotherBookIsLockedForThatBookToo() throws Exception {
        Manuscript other = manuscript();
        other.setLorebook(sub);
        other = manuscriptService.save(other);
        agentRunService.start(agent);

        assertThat(agentRunService.findLockingRun(sub)).as("the other book is not locked, the lorebook is").isNotNull();
        assertThat(agentRunService.findLockingRun(other)).isNull();
    }
}
