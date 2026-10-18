package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.AgentModelProfile;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.repository.AgentRepository;
import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.security.service.PermissionService;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.*;

public class AgentServiceImpl extends ExtendableServiceImpl<Agent, AgentRepository> implements AgentService {

    @Autowired
    @Lazy
    private ManuscriptService manuscriptService;

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    @Autowired
    @Lazy
    private AgentVirtualFileSystemService fileSystemService;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private PermissionService permissionService;

    @Override
    @CommonTxReadOnly
    public boolean isAllowed() throws Exception {
        return getGrant(currentUser).allowed();
    }

    @Override
    @CommonTxReadOnly
    public AgentGrant getGrant(User user) throws Exception {
        User holder = user == null || user.getId() == null ? null : userService.find(user.getId());
        if (holder == null || holder.isDeleted()) {
            return new AgentGrant(false, 0, 0, 0);
        }
        // the loaded user has the flag, the session's one does not
        if (holder.isAdmin()) {
            return new AgentGrant(true, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        }
        Permission permission = permissionService.getPermission(holder, PermissionType.AGENTS);
        if (permission == null || !permission.isGranted()) {
            return new AgentGrant(false, 0, 0, 0);
        }
        Integer runs = permission.getAgentsMaxConcurrentRuns();
        Long budget = permission.getAgentsRunTokenBudget();
        Integer agents = permission.getAgentsMaxAgents();
        return new AgentGrant(true, runs == null ? 1 : Math.max(0, runs), budget == null ? 0 : Math.max(0, budget),
                agents == null ? DEFAULT_MAX_AGENTS : Math.max(0, agents));
    }

    @Override
    @CommonTxReadOnly
    public boolean canCreate() throws Exception {
        AgentGrant grant = getGrant(currentUser);
        return grant.allowed() && findAllForUser().size() < grant.maxAgents();
    }

    @Override
    @CommonTx
    public Agent save(Agent agent) throws Exception {
        Manuscript manuscript = validate(agent);
        agent.setManuscript(manuscript);
        return super.save(agent);
    }

    /**
     * @return the stored manuscript of the agent
     */
    private Manuscript validate(Agent agent) throws Exception {
        if (StringUtils.isBlank(agent.getName())) {
            throw new IllegalArgumentException("The agent needs a name");
        }
        agent.setName(agent.getName().trim());
        boolean created = agent.getId() == null;
        if (created) {
            AgentGrant grant = getGrant(currentUser);
            if (!grant.allowed()) {
                throw new SecurityException("The user is not allowed to use agents");
            }
            if (findAllForUser().size() >= grant.maxAgents()) {
                throw new IllegalStateException("The user has all the agents they are allowed (" + grant.maxAgents() + ")");
            }
        }

        Manuscript manuscript = agent.getManuscript() == null || agent.getManuscript().getId() == null
                ? null : manuscriptService.find(agent.getManuscript().getId());
        if (manuscript == null || manuscript.isDeleted()) {
            throw new IllegalArgumentException("The agent needs a manuscript");
        }
        Long ownerId = agent.getOwner() != null ? agent.getOwner().getId() : currentUser.getId();
        if (manuscript.getOwner() == null || !Objects.equals(manuscript.getOwner().getId(), ownerId)) {
            throw new IllegalArgumentException("The manuscript is not the user's own");
        }
        if (!created) {
            Agent stored = getRepository().find(agent.getId());
            if (stored != null && !Objects.equals(stored.getManuscript().getId(), manuscript.getId())) {
                throw new IllegalStateException("The manuscript of an agent can't be changed");
            }
        }
        Agent other = getRepository().findByManuscript(manuscript);
        if (other != null && !Objects.equals(other.getId(), agent.getId())) {
            throw new IllegalStateException("The manuscript already has an agent");
        }

        if (agent.getInstructions() != null && agent.getInstructions().length() > MAX_INSTRUCTIONS_LENGTH) {
            throw new IllegalArgumentException("The instructions are longer than " + MAX_INSTRUCTIONS_LENGTH + " characters");
        }
        validateProfiles(agent.getModelProfiles());
        agent.setAllowedCommands(agent.getAllowedCommands().stream()
                .filter(StringUtils::isNotBlank).map(String::trim).distinct().toList());
        if (agent.getMaxRounds() != null && agent.getMaxRounds() <= 0) {
            throw new IllegalArgumentException("The most rounds must be positive");
        }
        if (agent.getContextCap() != null && agent.getContextCap() <= 0) {
            throw new IllegalArgumentException("The context limit must be positive");
        }
        if (agent.getTokenBudget() != null && agent.getTokenBudget() < 0) {
            throw new IllegalArgumentException("The token budget can't be negative");
        }
        return manuscript;
    }

    private void validateProfiles(List<AgentModelProfile> profiles) {
        if (profiles.size() > MAX_MODEL_PROFILES) {
            throw new IllegalArgumentException("An agent can have at most " + MAX_MODEL_PROFILES + " models");
        }
        Set<String> roles = new HashSet<>();
        for (AgentModelProfile profile : profiles) {
            if (StringUtils.isBlank(profile.getRole())) {
                throw new IllegalArgumentException("A model of the agent needs a role");
            }
            profile.setRole(profile.getRole().trim());
            if (!roles.add(profile.getRole().toLowerCase())) {
                throw new IllegalArgumentException("The agent has two models with the role " + profile.getRole());
            }
            if (profile.getContextCap() != null && profile.getContextCap() <= 0) {
                throw new IllegalArgumentException("The context limit of the model " + profile.getRole() + " must be positive");
            }
            if (profile.getConcurrency() != null && profile.getConcurrency() <= 0) {
                throw new IllegalArgumentException("The concurrency of the model " + profile.getRole() + " must be positive");
            }
        }
    }

    @Override
    @CommonTx
    public Agent delete(Agent agent, boolean hard) throws Exception {
        if (agent != null && agent.getId() != null) {
            agentRunService.deleteForAgent(agent, hard);
        }
        Agent deleted = super.delete(agent, hard);
        if (hard && agent != null && agent.getId() != null) {
            fileSystemService.deleteWorkspace(agent);
        }
        return deleted;
    }

    @Override
    @CommonTx
    public void deleteForManuscript(Manuscript manuscript, boolean hard) throws Exception {
        for (Agent agent : getRepository().findAllByManuscript(manuscript)) {
            delete(agent, hard);
        }
    }

    @Override
    @CommonTxReadOnly
    public Agent findForManuscript(Manuscript manuscript) throws Exception {
        Agent agent = getRepository().findByManuscript(manuscript);
        return agent != null && agent.getOwner() != null && Objects.equals(agent.getOwner().getId(), currentUser.getId())
                ? agent : null;
    }

    @Override
    @CommonTxReadOnly
    public ResolvedProfile resolveProfile(AgentModelProfile profile) throws Exception {
        if (profile == null || StringUtils.isAnyBlank(profile.getAiUuid(), profile.getProtocolUuid())) {
            return null;
        }
        AI ai = aiService.findAllForUser().stream()
                .filter(candidate -> profile.getAiUuid().equals(candidate.getUuid())).findFirst().orElse(null);
        Protocol protocol = protocolService.findForUser(profile.getProtocolUuid());
        return ai == null || protocol == null ? null : new ResolvedProfile(profile, ai, protocol);
    }

    @Override
    @CommonTx
    public Agent cloneAgent(Agent source, String name, Manuscript target) throws Exception {
        Agent original = source == null ? null : findForUser(source.getUuid());
        if (original == null) {
            throw new IllegalArgumentException("The agent does not exist");
        }
        Manuscript manuscript = target == null ? null : manuscriptService.findForUser(target.getUuid());
        if (manuscript == null) {
            throw new IllegalArgumentException("The manuscript does not exist");
        }

        Agent copy = new Agent();
        copy.setName(StringUtils.isBlank(name) ? original.getName() : name);
        copy.setEnabled(original.isEnabled());
        copy.setManuscript(manuscript);
        copy.setModelProfiles(original.getModelProfiles().stream().map(AgentServiceImpl::copyProfile).toList());
        copy.setSystemPrompt(original.getSystemPrompt());
        copy.setInstructions(original.getInstructions());
        copy.setAllowedCommands(new ArrayList<>(original.getAllowedCommands()));
        copy.setDestructiveMode(original.getDestructiveMode());
        copy.setMaxRounds(original.getMaxRounds());
        copy.setTokenBudget(original.getTokenBudget());
        copy.setContextCap(original.getContextCap());
        Agent saved = save(copy);
        fileSystemService.copyWorkspace(original, saved);
        return saved;
    }

    private static AgentModelProfile copyProfile(AgentModelProfile profile) {
        AgentModelProfile copy = new AgentModelProfile(profile.getRole(), profile.getAiUuid(), profile.getProtocolUuid());
        copy.setDescription(profile.getDescription());
        copy.setTools(profile.isTools());
        copy.setContextCap(profile.getContextCap());
        copy.setConcurrency(profile.getConcurrency());
        return copy;
    }
}
