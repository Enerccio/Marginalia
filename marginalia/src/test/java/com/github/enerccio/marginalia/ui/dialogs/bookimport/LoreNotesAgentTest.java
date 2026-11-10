package com.github.enerccio.marginalia.ui.dialogs.bookimport;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.service.AgentRunner;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The agent the import offers to take the lore notes of a book: when it can be started, what it is given and that its run is handed
 * to the runner.
 */
class LoreNotesAgentTest extends AgentTestBase {

    // the runs the agent handed to the runner; a real runner would work on them
    private final List<AgentRun> takenUp = new ArrayList<>();
    private LoreNotesAgent loreNotes;

    @BeforeEach
    void setUp() {
        loreNotes = new LoreNotesAgent();
        ReflectionTestUtils.setField(loreNotes, "agentRunner", Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{AgentRunner.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("run")) {
                        takenUp.add((AgentRun) args[0]);
                    }
                    return null;
                }));
    }

    private Manuscript bookWithModel() throws Exception {
        loginAgentUser();
        OpenAICompatible ai = createAI();
        Manuscript book = manuscript();
        book.setAi(ai);
        book.setProtocol(protocol());
        return manuscriptService.save(book);
    }

    @Test
    void aBookWithoutAModelCantHaveIt() throws Exception {
        loginAgentUser();
        assertThat(loreNotes.check(manuscript())).isEqualTo(LoreNotesAgent.Problem.NO_MODEL);
    }

    @Test
    void aUserWithoutTheAgentsPermissionCant() throws Exception {
        login();
        assertThat(loreNotes.check(manuscript())).isEqualTo(LoreNotesAgent.Problem.NOT_ALLOWED);
    }

    @Test
    void startsAnAgentThatReadsAndWritesLoreAndHandsTheRunToTheRunner() throws Exception {
        Manuscript book = bookWithModel();
        assertThat(book.getLorebook()).isNull();
        assertThat(loreNotes.check(book)).isNull();

        Agent agent = loreNotes.start(book);

        assertThat(agent.getName()).isEqualTo(Defaults.AGENT_LORE_NOTES_NAME);
        assertThat(agent.getInstructions()).isEqualTo(Defaults.AGENT_LORE_NOTES_INSTRUCTIONS);
        assertThat(agent.getAllowedCommands()).contains("get-part", "create-lore-entry").doesNotContain("delete-lore-entry", "delete-lorebook");
        // the model of the book is the model of the agent
        assertThat(agent.getModelProfiles()).hasSize(1);
        assertThat(takenUp).hasSize(1);
        assertThat(takenUp.getFirst().getStatus()).isEqualTo(AgentRunStatus.RUNNING);
        // the agent writes into the lorebook of the book, so the book has one
        assertThat(manuscriptService.find(book).getLorebook()).isNotNull();
    }

    @Test
    void aBookHasOnlyOneAgent() throws Exception {
        Manuscript book = bookWithModel();
        loreNotes.start(book);
        assertThat(loreNotes.check(book)).isEqualTo(LoreNotesAgent.Problem.HAS_AGENT);
        assertThat(loreNotes.existingAgentName(book)).isEqualTo(Defaults.AGENT_LORE_NOTES_NAME);
        assertThatThrownBy(() -> loreNotes.start(book)).isInstanceOf(IllegalStateException.class);
    }

    @Autowired
    private CommandService commandService;

    @Test
    void everyCommandItMayCallExists() {
        // a misspelled name would only show when the agent tries it
        List<String> registered = commandService.getCommands().stream().map(CommandDefinition::commandName).toList();
        assertThat(registered).containsAll(LoreNotesAgent.COMMANDS);
    }
}
