package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.model.AgentModelProfile;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.security.model.Permission;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.AgentService;
import com.github.enerccio.marginalia.domain.service.AgentService.AgentGrant;
import com.github.enerccio.marginalia.domain.service.AgentService.ResolvedProfile;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
        assertThat(agentService.getGrant(currentUser)).as("the limits of a user, only the permission is not needed")
                .isEqualTo(new AgentGrant(true, 1, 0, AgentService.DEFAULT_MAX_AGENTS));
        assertThat(agent().getId()).isNotNull();
    }

    @Test
    void administratorFollowsTheSettingsOfTheirOwnPermission() throws Exception {
        User admin = loginAdmin();
        updatePermission(admin, PermissionType.AGENTS, permission -> {
            permission.setGranted(false);
            permission.setAgentsMaxWorkspaceFiles(5000);
            permission.setAgentsMaxWorkspaceMb(100);
        });

        AgentGrant grant = agentService.getGrant(currentUser);
        assertThat(grant.allowed()).as("the granted flag does not matter for an administrator").isTrue();
        assertThat(grant.maxWorkspaceFiles()).isEqualTo(5000);
        assertThat(grant.maxWorkspaceBytes()).isEqualTo(100L * 1024 * 1024);
        assertThat(grant.maxAgents()).as("what is not filled in is the default").isEqualTo(AgentService.DEFAULT_MAX_AGENTS);
        assertThat(grant.maxConcurrentRuns()).isEqualTo(1);
        assertThat(grant.maxFileBytes()).isZero();
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

    // --- the system prompt ---

    @Autowired
    private SettingService settingService;

    /** The application settings outlive a test in the shared database. */
    private void clearApplicationPrompt() throws Exception {
        loginAdmin();
        agentService.setAppSystemPrompt(null);
    }

    @Test
    void aBlankPromptFollowsTheUsersDefaultThenTheApplicationsThenTheBuiltInOne() throws Exception {
        try {
            loginAgentUser();
            Agent agent = agent();
            assertThat(agent.getSystemPrompt()).isBlank();
            assertThat(agentService.getSystemPrompt(agent)).as("nothing set").isEqualTo(Defaults.DEFAULT_AGENT_PROMPT);
            assertThat(agentService.getDefaultSystemPrompt()).isEqualTo(Defaults.DEFAULT_AGENT_PROMPT);

            loginAdmin();
            agentService.setAppSystemPrompt("The application's prompt");
            loginAs(agent.getOwner());
            assertThat(agentService.getSystemPrompt(agent)).isEqualTo("The application's prompt");
            assertThat(agentService.getDefaultSystemPrompt()).isEqualTo("The application's prompt");
            assertThat(agentService.getAppDefaultSystemPrompt()).isEqualTo("The application's prompt");

            UserSetting setting = settingService.getOrCreate(UserSetting.class);
            setting.setDefaultAgentPrompt("The user's prompt");
            settingService.save(setting);
            assertThat(agentService.getSystemPrompt(agent)).as("the user's beats the application's").isEqualTo("The user's prompt");
            assertThat(agentService.getDefaultSystemPrompt()).isEqualTo("The user's prompt");
            assertThat(agentService.getAppDefaultSystemPrompt()).as("what a user without one gets").isEqualTo("The application's prompt");

            agent.setSystemPrompt("Its own");
            agent = agentService.save(agent);
            assertThat(agentService.getSystemPrompt(agent)).isEqualTo("Its own");
        } finally {
            clearApplicationPrompt();
        }
    }

    @Test
    void theShareAtWhichTheRunIsCompactedIsNeverOrWithinTheLimits() throws Exception {
        loginAgentUser();
        Agent agent = newAgent(manuscript());

        for (int allowed : new int[]{0, 50, 90, 99}) {
            agent.setAutoCompactPercent(allowed);
            agent = agentService.save(agent);
            assertThat(agentService.findForUser(agent.getUuid()).getAutoCompactPercent()).isEqualTo(allowed);
        }
        for (int refused : new int[]{-1, 10, 49, 100}) {
            Agent another = newAgent(manuscript());
            another.setAutoCompactPercent(refused);
            assertThatThrownBy(() -> agentService.save(another)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void blankInstructionsFollowTheDefaultOfTheUserAndThenAreBlank() throws Exception {
        loginAgentUser();
        Agent agent = agent();
        assertThat(agent.getInstructions()).isBlank();
        assertThat(agentService.getInstructions(agent)).as("nothing set").isEmpty();
        assertThat(agentService.getDefaultInstructions()).isEmpty();

        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        setting.setDefaultAgentInstructions("Keep the names as they are.");
        settingService.save(setting);
        assertThat(agentService.getInstructions(agent)).isEqualTo("Keep the names as they are.");
        assertThat(agentService.getDefaultInstructions()).isEqualTo("Keep the names as they are.");

        agent.setInstructions("Its own.");
        agent = agentService.save(agent);
        assertThat(agentService.getInstructions(agent)).isEqualTo("Its own.");
        assertThat(agentService.getDefaultInstructions()).as("the placeholder stays the default").isEqualTo("Keep the names as they are.");
    }

    @Test
    void theDefaultInstructionsOfAnotherUserDoNotReachTheAgent() throws Exception {
        loginAgentUser();
        Agent agent = agent();
        login();
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        setting.setDefaultAgentInstructions("Somebody else's instructions");
        settingService.save(setting);

        assertThat(agentService.getInstructions(agent)).isEmpty();
    }

    @Test
    void theDefaultOfAnotherUserDoesNotReachTheAgent() throws Exception {
        try {
            loginAgentUser();
            Agent agent = agent();
            User other = login();
            UserSetting setting = settingService.getOrCreate(UserSetting.class);
            setting.setDefaultAgentPrompt("Somebody else's prompt");
            settingService.save(setting);
            assertThat(other.getId()).isNotEqualTo(agent.getOwner().getId());

            assertThat(agentService.getSystemPrompt(agent)).isEqualTo(Defaults.DEFAULT_AGENT_PROMPT);
        } finally {
            clearApplicationPrompt();
        }
    }

    @Test
    void onlyAnAdministratorReadsAndSetsTheApplicationPrompt() throws Exception {
        try {
            loginAgentUser();

            assertThatThrownBy(() -> agentService.setAppSystemPrompt("mine")).isInstanceOf(SecurityException.class);
            assertThatThrownBy(() -> agentService.getAppSystemPrompt()).isInstanceOf(SecurityException.class);
            assertThat(agentService.getAppDefaultSystemPrompt()).as("every user gets what applies").isNotBlank();

            loginAdmin();
            agentService.setAppSystemPrompt("The application's prompt");
            assertThat(agentService.getAppSystemPrompt()).isEqualTo("The application's prompt");
            agentService.setAppSystemPrompt("  ");
            assertThat(agentService.getAppSystemPrompt()).as("blank removes it").isEmpty();
            assertThat(agentService.getAppDefaultSystemPrompt()).isEqualTo(Defaults.DEFAULT_AGENT_PROMPT);
        } finally {
            clearApplicationPrompt();
        }
    }

    // --- quotas ---

    @Test
    void grantReadsTheQuotasOfThePermission() throws Exception {
        User user = loginAgentUser();
        updatePermission(user, PermissionType.AGENTS, permission -> {
            permission.setAgentsMaxCopies(2);
            permission.setAgentsMaxFileKb(100);
            permission.setAgentsMaxWorkspaceMb(5);
            permission.setAgentsMaxWorkspaceFiles(50);
        });

        AgentGrant grant = agentService.getGrant(user);

        assertThat(grant).isEqualTo(new AgentGrant(true, 1, 0, AgentService.DEFAULT_MAX_AGENTS, 2, 100 * 1024L, 5L * 1024 * 1024, 50));
        Permission stored = permissionService.getPermission(user, PermissionType.AGENTS);
        assertThat(stored.getAgentsMaxCopies()).isEqualTo(2);
        assertThat(stored.getAgentsMaxFileKb()).isEqualTo(100);
        assertThat(stored.getAgentsMaxWorkspaceMb()).isEqualTo(5);
        assertThat(stored.getAgentsMaxWorkspaceFiles()).isEqualTo(50);
    }

    @Test
    void aQuotaThatIsNotSetOrNotPositiveIsTheDefault() throws Exception {
        User user = loginAgentUser();
        updatePermission(user, PermissionType.AGENTS, permission -> {
            permission.setAgentsMaxCopies(-1);
            permission.setAgentsMaxFileKb(0);
        });

        AgentGrant grant = agentService.getGrant(user);

        assertThat(grant.maxCopies()).isZero();
        assertThat(grant.maxFileBytes()).isZero();
        assertThat(grant.maxWorkspaceBytes()).isZero();
        assertThat(grant.maxWorkspaceFiles()).isZero();
    }

    @Test
    void aBlankCompactionPromptFollowsTheSameChainAsTheSystemPrompt() throws Exception {
        try {
            loginAgentUser();
            Agent agent = agent();
            assertThat(agentService.getCompactionPrompt(agent)).isEqualTo(Defaults.DEFAULT_AGENT_COMPACTION_PROMPT);
            assertThat(agentService.getDefaultCompactionPrompt()).isEqualTo(Defaults.DEFAULT_AGENT_COMPACTION_PROMPT);

            loginAdmin();
            agentService.setAppCompactionPrompt("The application's compaction");
            assertThat(agentService.getAppCompactionPrompt()).isEqualTo("The application's compaction");
            loginAs(agent.getOwner());
            assertThat(agentService.getCompactionPrompt(agent)).isEqualTo("The application's compaction");
            assertThat(agentService.getAppDefaultCompactionPrompt()).isEqualTo("The application's compaction");
            assertThatThrownBy(() -> agentService.setAppCompactionPrompt("mine")).isInstanceOf(SecurityException.class);
            assertThatThrownBy(() -> agentService.getAppCompactionPrompt()).isInstanceOf(SecurityException.class);

            UserSetting setting = settingService.getOrCreate(UserSetting.class);
            setting.setDefaultAgentCompactionPrompt("The user's compaction");
            settingService.save(setting);
            assertThat(agentService.getCompactionPrompt(agent)).isEqualTo("The user's compaction");
            assertThat(agentService.getDefaultCompactionPrompt()).isEqualTo("The user's compaction");
            assertThat(agentService.getAppDefaultCompactionPrompt()).isEqualTo("The application's compaction");

            agent.setCompactionPrompt("Its own compaction");
            agent = agentService.save(agent);
            assertThat(agentService.getCompactionPrompt(agent)).isEqualTo("Its own compaction");
            assertThat(agentService.cloneAgent(agent, "copy", manuscript()).getCompactionPrompt()).isEqualTo("Its own compaction");
        } finally {
            loginAdmin();
            agentService.setAppCompactionPrompt(null);
        }
    }
}
