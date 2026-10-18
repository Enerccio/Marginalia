package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.model.AgentModelProfile;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.AgentService;
import com.github.enerccio.marginalia.domain.service.AgentService.AgentGrant;
import com.github.enerccio.marginalia.domain.service.AgentService.ResolvedProfile;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Who can have agents, what the manuscript of an agent can be, and what the configuration may be.
 */
class AgentServiceTest extends AgentTestBase {

    @Test
    void userWithoutThePermissionCannotCreateAnAgent() throws Exception {
        login();
        Manuscript manuscript = manuscript();
        Agent agent = newAgent(manuscript);

        assertThat(agentService.isAllowed()).isFalse();
        assertThatThrownBy(() -> agentService.save(agent)).isInstanceOf(SecurityException.class);
    }

    @Test
    void revokedPermissionDoesNotAllow() throws Exception {
        User user = loginAgentUser();
        setPermission(user, PermissionType.AGENTS, false);

        assertThat(agentService.isAllowed()).isFalse();
        assertThat(agentService.getGrant(user)).isEqualTo(new AgentGrant(false, 0, 0, 0));
    }

    @Test
    void administratorNeedsNoPermission() throws Exception {
        loginAdmin();

        assertThat(agentService.isAllowed()).isTrue();
        assertThat(agentService.getGrant(currentUser)).isEqualTo(new AgentGrant(true, Integer.MAX_VALUE, 0, Integer.MAX_VALUE));
        assertThat(agent().getId()).isNotNull();
    }

    @Test
    void grantReadsTheSettingsOfThePermission() throws Exception {
        User user = loginAgentUser();
        assertThat(agentService.getGrant(user)).as("defaults").isEqualTo(new AgentGrant(true, 1, 0, AgentService.DEFAULT_MAX_AGENTS));

        updatePermission(user, PermissionType.AGENTS, permission -> {
            permission.setAgentsMaxConcurrentRuns(3);
            permission.setAgentsRunTokenBudget(500_000L);
            permission.setAgentsMaxAgents(7);
        });

        assertThat(agentService.getGrant(user)).isEqualTo(new AgentGrant(true, 3, 500_000, 7));
        Permission stored = permissionService.getPermission(user, PermissionType.AGENTS);
        assertThat(stored.getAgentsMaxConcurrentRuns()).isEqualTo(3);
        assertThat(stored.getAgentsRunTokenBudget()).isEqualTo(500_000L);
        assertThat(stored.getAgentsMaxAgents()).isEqualTo(7);
    }

    @Test
    void theNumberOfAgentsIsLimitedByThePermissionAndClonesCount() throws Exception {
        User user = loginAgentUser();
        updatePermission(user, PermissionType.AGENTS, permission -> permission.setAgentsMaxAgents(2));
        Agent first = agent();
        assertThat(agentService.canCreate()).isTrue();

        Agent clone = agentService.cloneAgent(first, "copy", manuscript());

        assertThat(clone.getId()).isNotNull();
        assertThat(agentService.canCreate()).isFalse();
        assertThatThrownBy(() -> agentService.cloneAgent(first, "another", manuscript()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("all the agents");
        assertThatThrownBy(() -> agentService.save(newAgent(manuscript()))).isInstanceOf(IllegalStateException.class);

        agentService.delete(clone, false);

        assertThat(agentService.canCreate()).as("a deleted agent frees its place").isTrue();
    }

    @Test
    void editingAnAgentIsNotLimitedByTheNumberOfAgents() throws Exception {
        User user = loginAgentUser();
        Agent agent = agent();
        updatePermission(user, PermissionType.AGENTS, permission -> permission.setAgentsMaxAgents(0));

        agent.setName("renamed");

        assertThat(agentService.save(agent).getName()).isEqualTo("renamed");
        assertThat(agentService.canCreate()).isFalse();
    }

    @Test
    void manuscriptHasOneAgent() throws Exception {
        loginAgentUser();
        Manuscript manuscript = manuscript();
        Agent first = agentService.save(newAgent(manuscript));

        assertThat(agentService.findForManuscript(manuscript).getId()).isEqualTo(first.getId());
        assertThatThrownBy(() -> agentService.save(newAgent(manuscript)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("already has an agent");

        agentService.delete(agentService.find(first.getId()), false);
        assertThat(agentService.findForManuscript(manuscript)).as("deleted").isNull();
        assertThat(agentService.save(newAgent(manuscript)).getId()).as("a deleted agent does not count").isNotNull();
    }

    @Test
    void agentCannotUseAnotherUsersManuscript() throws Exception {
        loginAgentUser();
        Manuscript foreign = manuscript();
        loginAgentUser();
        Agent agent = newAgent(foreign);

        assertThatThrownBy(() -> agentService.save(agent)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not the user's own");
    }

    @Test
    void agentNeedsAnExistingManuscript() throws Exception {
        loginAgentUser();
        Agent noManuscript = newAgent(null);
        assertThatThrownBy(() -> agentService.save(noManuscript)).isInstanceOf(IllegalArgumentException.class);

        Manuscript deleted = manuscript();
        manuscriptService.delete(deleted, false);
        assertThatThrownBy(() -> agentService.save(newAgent(deleted))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void manuscriptOfAnAgentCannotBeChanged() throws Exception {
        loginAgentUser();
        Agent agent = agentService.find(agent().getId());
        agent.setManuscript(manuscript());

        assertThatThrownBy(() -> agentService.save(agent)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be changed");
    }

    @Test
    void existingAgentCanStillBeEditedWhenThePermissionWasTaken() throws Exception {
        User user = loginAgentUser();
        Agent agent = agent();
        setPermission(user, PermissionType.AGENTS, false);

        Agent loaded = agentService.find(agent.getId());
        loaded.setName("renamed");
        agentService.save(loaded);

        assertThat(agentService.find(agent.getId()).getName()).isEqualTo("renamed");
    }

    @Test
    void configurationIsValidated() throws Exception {
        loginAgentUser();
        Manuscript manuscript = manuscript();

        Agent blank = newAgent(manuscript);
        blank.setName("  ");
        assertThatThrownBy(() -> agentService.save(blank)).isInstanceOf(IllegalArgumentException.class);

        Agent longInstructions = newAgent(manuscript);
        longInstructions.setInstructions("x".repeat(AgentService.MAX_INSTRUCTIONS_LENGTH + 1));
        assertThatThrownBy(() -> agentService.save(longInstructions)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("instructions");

        Agent twoRoles = newAgent(manuscript);
        twoRoles.getModelProfiles().add(profile(" OVERSEER "));
        assertThatThrownBy(() -> agentService.save(twoRoles)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("two models");

        Agent noRole = newAgent(manuscript);
        noRole.getModelProfiles().getFirst().setRole(" ");
        assertThatThrownBy(() -> agentService.save(noRole)).isInstanceOf(IllegalArgumentException.class);

        Agent tooMany = newAgent(manuscript);
        for (int i = 0; i < AgentService.MAX_MODEL_PROFILES; i++) {
            tooMany.getModelProfiles().add(new AgentModelProfile("role" + i, "ai", "protocol"));
        }
        assertThatThrownBy(() -> agentService.save(tooMany)).isInstanceOf(IllegalArgumentException.class);

        Agent badCap = newAgent(manuscript);
        badCap.getModelProfiles().getFirst().setContextCap(0);
        assertThatThrownBy(() -> agentService.save(badCap)).isInstanceOf(IllegalArgumentException.class);

        Agent badConcurrency = newAgent(manuscript);
        badConcurrency.getModelProfiles().getFirst().setConcurrency(-1);
        assertThatThrownBy(() -> agentService.save(badConcurrency)).isInstanceOf(IllegalArgumentException.class);

        Agent badRounds = newAgent(manuscript);
        badRounds.setMaxRounds(0);
        assertThatThrownBy(() -> agentService.save(badRounds)).isInstanceOf(IllegalArgumentException.class);

        Agent badBudget = newAgent(manuscript);
        badBudget.setTokenBudget(-1L);
        assertThatThrownBy(() -> agentService.save(badBudget)).isInstanceOf(IllegalArgumentException.class);

        Agent badContext = newAgent(manuscript);
        badContext.setContextCap(0);
        assertThatThrownBy(() -> agentService.save(badContext)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nameRoleAndCommandsAreTidied() throws Exception {
        loginAgentUser();
        Agent agent = newAgent(manuscript());
        agent.setName("  Checker ");
        agent.getModelProfiles().getFirst().setRole(" overseer ");
        agent.setAllowedCommands(new ArrayList<>(List.of("list-parts", " list-parts", "", "get-part ")));

        Agent saved = agentService.find(agentService.save(agent).getId());

        assertThat(saved.getName()).isEqualTo("Checker");
        assertThat(saved.getMainProfile().getRole()).isEqualTo("overseer");
        assertThat(saved.getAllowedCommands()).containsExactly("list-parts", "get-part");
    }

    @Test
    void agentWithoutModelCanBeSaved() throws Exception {
        loginAgentUser();
        Agent agent = newAgent(manuscript());
        agent.getModelProfiles().clear();

        Agent saved = agentService.find(agentService.save(agent).getId());

        assertThat(saved.getMainProfile()).isNull();
    }

    @Test
    void profileIsResolvedToUsableModels() throws Exception {
        loginAgentUser();
        AgentModelProfile profile = profile("overseer");

        ResolvedProfile resolved = agentService.resolveProfile(profile);

        assertThat(resolved.ai().getUuid()).isEqualTo(profile.getAiUuid());
        assertThat(resolved.protocol().getUuid()).isEqualTo(profile.getProtocolUuid());
        assertThat(resolved.profile()).isSameAs(profile);
    }

    @Test
    void profileOfAnotherUsersModelIsNotResolved() throws Exception {
        loginAgentUser();
        AgentModelProfile profile = profile("overseer");
        loginAgentUser();

        assertThat(agentService.resolveProfile(profile)).isNull();
        assertThat(agentService.resolveProfile(null)).isNull();
        assertThat(agentService.resolveProfile(new AgentModelProfile("x", null, null))).isNull();
        assertThat(agentService.resolveProfile(new AgentModelProfile("x", "missing", "missing"))).isNull();
    }

    @Test
    void sharedAiIsResolved() throws Exception {
        User admin = loginAdmin();
        var ai = createAI();
        ai.setShared(true);
        aiService.save(ai);
        AgentModelProfile profile = new AgentModelProfile("overseer", ai.getUuid(), null);
        User user = loginAgentUser();
        profile.setProtocolUuid(protocol().getUuid());

        assertThat(agentService.resolveProfile(profile)).isNotNull();
        assertThat(user.getId()).isNotEqualTo(admin.getId());
    }

    @Test
    void cloneCopiesTheDefinitionToAnotherManuscript() throws Exception {
        loginAgentUser();
        Agent source = newAgent(manuscript());
        source.setInstructions("Be careful.");
        source.setAllowedCommands(new ArrayList<>(List.of("list-parts")));
        source.setMaxRounds(7);
        source.setTokenBudget(1000L);
        source = agentService.save(source);
        Manuscript target = manuscript();

        Agent copy = agentService.cloneAgent(source, "", target);

        assertThat(copy.getId()).isNotEqualTo(source.getId());
        assertThat(copy.getUuid()).isNotEqualTo(source.getUuid());
        Agent loaded = agentService.find(copy.getId());
        assertThat(loaded.getName()).isEqualTo(source.getName());
        assertThat(loaded.getManuscript().getId()).isEqualTo(target.getId());
        assertThat(loaded.getInstructions()).isEqualTo("Be careful.");
        assertThat(loaded.getAllowedCommands()).containsExactly("list-parts");
        assertThat(loaded.getMaxRounds()).isEqualTo(7);
        assertThat(loaded.getTokenBudget()).isEqualTo(1000L);
        assertThat(loaded.getModelProfiles()).isEqualTo(source.getModelProfiles());

        loaded.getModelProfiles().getFirst().setRole("changed");
        agentService.save(loaded);
        assertThat(agentService.find(source.getId()).getMainProfile().getRole()).isEqualTo("overseer");
        assertThat(agentService.cloneAgent(source, "Second", manuscript()).getName()).isEqualTo("Second");
    }

    @Test
    void cloneNeedsOwnAgentAndManuscript() throws Exception {
        loginAgentUser();
        Agent source = agent();
        Manuscript occupied = agent().getManuscript();

        assertThatThrownBy(() -> agentService.cloneAgent(source, null, occupied)).isInstanceOf(IllegalStateException.class);

        loginAgentUser();
        Manuscript own = manuscript();
        assertThatThrownBy(() -> agentService.cloneAgent(source, null, own)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> agentService.cloneAgent(null, null, own)).isInstanceOf(IllegalArgumentException.class);
    }
}
