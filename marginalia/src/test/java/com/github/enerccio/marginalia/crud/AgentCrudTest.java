package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode;
import com.github.enerccio.marginalia.domain.model.AgentModelProfile;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.AgentService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.OwnedService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AgentCrudTest extends ExtendableCrudContract<Agent> {

    @Autowired
    private AgentService agentService;

    @Autowired
    private ManuscriptService manuscriptService;

    @BeforeEach
    void allowAgents() {
        setPermission(owner, PermissionType.AGENTS, true);
    }

    @Override
    protected OwnedService<Agent, ?> service() {
        return agentService;
    }

    @Override
    protected Agent newEntity() throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.setName("Agent's book");

        AgentModelProfile main = new AgentModelProfile("overseer", "ai-uuid", "protocol-uuid");
        main.setDescription("decides what to do");
        main.setContextCap(8000);
        AgentModelProfile writer = new AgentModelProfile("writer", "ai-uuid-2", "protocol-uuid-2");
        writer.setTools(false);
        writer.setConcurrency(2);

        Agent agent = new Agent();
        agent.setName("Continuity");
        agent.setManuscript(manuscriptService.save(manuscript));
        agent.setModelProfiles(new ArrayList<>(List.of(main, writer)));
        agent.setSystemPrompt("You check continuity.");
        agent.setInstructions("# Rules\nNever invent names.");
        agent.setAllowedCommands(new ArrayList<>(List.of("list-parts", "get-part")));
        agent.setDestructiveMode(DestructiveCommandMode.DENY);
        agent.setMaxRounds(50);
        agent.setTokenBudget(100_000L);
        agent.setContextCap(16_000);
        return agent;
    }

    @Override
    protected void assertCreated(Agent loaded) {
        assertThat(loaded.getName()).isEqualTo("Continuity");
        assertThat(loaded.isEnabled()).isTrue();
        assertThat(loaded.getManuscript().getName()).isEqualTo("Agent's book");
        assertThat(loaded.getModelProfiles()).hasSize(2);
        AgentModelProfile main = loaded.getMainProfile();
        assertThat(main.getRole()).isEqualTo("overseer");
        assertThat(main.getAiUuid()).isEqualTo("ai-uuid");
        assertThat(main.getProtocolUuid()).isEqualTo("protocol-uuid");
        assertThat(main.getDescription()).isEqualTo("decides what to do");
        assertThat(main.isTools()).isTrue();
        assertThat(main.getContextCap()).isEqualTo(8000);
        AgentModelProfile writer = loaded.getModelProfiles().get(1);
        assertThat(writer.isTools()).isFalse();
        assertThat(writer.getConcurrency()).isEqualTo(2);
        assertThat(loaded.getSystemPrompt()).isEqualTo("You check continuity.");
        assertThat(loaded.getInstructions()).isEqualTo("# Rules\nNever invent names.");
        assertThat(loaded.getAllowedCommands()).containsExactly("list-parts", "get-part");
        assertThat(loaded.getDestructiveMode()).isEqualTo(DestructiveCommandMode.DENY);
        assertThat(loaded.getMaxRounds()).isEqualTo(50);
        assertThat(loaded.getTokenBudget()).isEqualTo(100_000L);
        assertThat(loaded.getContextCap()).isEqualTo(16_000);
    }

    @Override
    protected void modify(Agent entity) {
        entity.setName("Lorebook upkeep");
        entity.setEnabled(false);
        entity.setInstructions(null);
        entity.getModelProfiles().removeLast();
        entity.getAllowedCommands().add("search-parts");
        entity.setDestructiveMode(null);
    }

    @Override
    protected void assertModified(Agent loaded) {
        assertThat(loaded.getName()).isEqualTo("Lorebook upkeep");
        assertThat(loaded.isEnabled()).isFalse();
        assertThat(loaded.getInstructions()).isNull();
        assertThat(loaded.getModelProfiles()).extracting(AgentModelProfile::getRole).containsExactly("overseer");
        assertThat(loaded.getAllowedCommands()).containsExactly("list-parts", "get-part", "search-parts");
        assertThat(loaded.getDestructiveMode()).as("not set means asking").isEqualTo(DestructiveCommandMode.ASK);
        assertThat(loaded.getSystemPrompt()).isEqualTo("You check continuity.");
        assertThat(loaded.getMaxRounds()).isEqualTo(50);
    }
}
