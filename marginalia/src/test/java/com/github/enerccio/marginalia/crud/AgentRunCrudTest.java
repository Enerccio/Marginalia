package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.AgentModelProfile;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.service.AgentService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.OwnedService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AgentRunCrudTest extends ExtendableCrudContract<AgentRun> {

    @Autowired
    private AgentRunService agentRunService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private ManuscriptService manuscriptService;

    private Agent agent;

    @BeforeEach
    void createAgent() throws Exception {
        setPermission(owner, PermissionType.AGENTS, true);
        Manuscript manuscript = new Manuscript();
        manuscript.setName("Run's book");
        agent = new Agent();
        agent.setName("Agent");
        agent.setManuscript(manuscriptService.save(manuscript));
        agent.getModelProfiles().add(new AgentModelProfile("overseer", "ai", "protocol"));
        agent = agentService.save(agent);
    }

    @Override
    protected OwnedService<AgentRun, ?> service() {
        return agentRunService;
    }

    @Override
    protected AgentRun newEntity() {
        AgentRun run = new AgentRun();
        run.setAgent(agent);
        run.setStatus(AgentRunStatus.PAUSED);
        run.setStatusReason("waiting for the user");
        run.setStartedAt(new Date());
        run.setPrefillTokens(120);
        run.setGeneratedTokens(30);
        run.setRounds(2);
        run.setLanguage(Language.JAPANESE);
        Map<String, String> pairs = new HashMap<>();
        pairs.put("copy-1", "original-1");
        run.setUuidPairs(pairs);
        return run;
    }

    @Override
    protected void assertCreated(AgentRun loaded) {
        assertThat(loaded.getAgent().getId()).isEqualTo(agent.getId());
        assertThat(loaded.getStatus()).isEqualTo(AgentRunStatus.PAUSED);
        assertThat(loaded.getStatusReason()).isEqualTo("waiting for the user");
        assertThat(loaded.getStartedAt()).isNotNull();
        assertThat(loaded.getFinishedAt()).isNull();
        assertThat(loaded.getWorkingCopy()).isNull();
        assertThat(loaded.getPrefillTokens()).isEqualTo(120);
        assertThat(loaded.getGeneratedTokens()).isEqualTo(30);
        assertThat(loaded.getTotalTokens()).isEqualTo(150);
        assertThat(loaded.getRounds()).isEqualTo(2);
        assertThat(loaded.getLanguage()).isEqualTo(Language.JAPANESE);
        assertThat(loaded.getUuidPairs()).containsExactly(Map.entry("copy-1", "original-1"));
    }

    @Override
    protected void modify(AgentRun entity) {
        entity.setLanguage(Language.ENGLISH);
        entity.getUuidPairs().put("copy-2", "original-2");
        // the status and the counters are not written by a save, only the service changes them
        entity.setStatus(AgentRunStatus.APPLIED);
        entity.setPrefillTokens(1);
    }

    @Override
    protected void assertModified(AgentRun loaded) {
        assertThat(loaded.getLanguage()).isEqualTo(Language.ENGLISH);
        assertThat(loaded.getUuidPairs()).containsOnlyKeys("copy-1", "copy-2");
        assertThat(loaded.getStatus()).isEqualTo(AgentRunStatus.PAUSED);
        assertThat(loaded.getPrefillTokens()).isEqualTo(120);
    }
}
