package com.github.enerccio.marginalia.test;

import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.model.AgentModelProfile;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import com.github.enerccio.marginalia.domain.service.AgentService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.ProtocolService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Agents need a user who is allowed to have them, a manuscript and a model; the helpers make them.
 */
public abstract class AgentTestBase extends MarginaliaTestBase {

    @Autowired
    protected AgentService agentService;

    @Autowired
    protected AgentRunService agentRunService;

    @Autowired
    protected ManuscriptService manuscriptService;

    @Autowired
    protected ProtocolService protocolService;

    /**
     * Creates a user who may use agents and logs them in.
     */
    protected User loginAgentUser() throws Exception {
        User user = login();
        setPermission(user, PermissionType.AGENTS, true);
        return user;
    }

    protected Manuscript manuscript() throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        return manuscriptService.save(manuscript);
    }

    protected Protocol protocol() throws Exception {
        ChatCompletionProtocol protocol = new ChatCompletionProtocol();
        protocol.setName(uniqueName("protocol"));
        protocol.setProtocolType(ProtocolType.CHAT_COMPLETION);
        protocol.setMaxTokens(4096);
        protocol.setReplyTokens(300);
        return protocolService.save(protocol);
    }

    /**
     * Profile of the main model, an AI and a protocol of the current user.
     */
    protected AgentModelProfile profile(String role) throws Exception {
        OpenAICompatible ai = createAI();
        return new AgentModelProfile(role, ai.getUuid(), protocol().getUuid());
    }

    /**
     * Unsaved agent of the manuscript, with a model the current user can use.
     */
    protected Agent newAgent(Manuscript manuscript) throws Exception {
        Agent agent = new Agent();
        agent.setName(uniqueName("agent"));
        agent.setManuscript(manuscript);
        agent.getModelProfiles().add(profile("overseer"));
        return agent;
    }

    protected Agent agent() throws Exception {
        return agentService.save(newAgent(manuscript()));
    }
}
