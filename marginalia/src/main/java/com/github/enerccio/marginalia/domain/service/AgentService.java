package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.AgentModelProfile;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.repository.AgentRepository;
import com.github.enerccio.marginalia.domain.security.model.User;

/**
 * Definitions of agents: LLMs that work on one manuscript on their own (see {@link Agent}). A manuscript has at most
 * one agent, and an agent has at most one unfinished run ({@link AgentRunService}).
 * <p>
 * Only users with the permission {@code AGENTS} (and administrators) can create agents; the permission also decides
 * whether they can run them and with what limits, see {@link #getGrant}. {@code save} checks that the manuscript is the
 * user's own, is not changed on an existing agent, has no other agent, and that the configuration is within the
 * limits.
 */
public interface AgentService extends ExtendableService<Agent, AgentRepository> {

    /**
     * Longest instructions ("agents.md") of an agent, in characters.
     */
    int MAX_INSTRUCTIONS_LENGTH = 20_000;

    /**
     * Most model profiles of an agent.
     */
    int MAX_MODEL_PROFILES = 8;

    /**
     * How many agents (definitions, clones included) a user may have when the administrator did not say.
     */
    int DEFAULT_MAX_AGENTS = 5;

    /**
     * What a user may do with agents.
     *
     * @param allowed           whether the user can create agents and run them
     * @param maxConcurrentRuns how many runs of the user can work at once
     * @param runTokenBudget    most tokens a run can spend, zero is no limit
     * @param maxAgents         how many agents the user can have, clones included
     * @param maxCopies         how many working copies the runs of the user keep at once, zero is no limit
     * @param maxFileBytes      the largest file in a workspace of the user, zero is the default of the application
     * @param maxWorkspaceBytes the most all the files of one workspace of the user take, zero is the default
     * @param maxWorkspaceFiles the most files in one workspace of the user, zero is the default
     */
    record AgentGrant(boolean allowed, int maxConcurrentRuns, long runTokenBudget, int maxAgents, int maxCopies,
                      long maxFileBytes, long maxWorkspaceBytes, int maxWorkspaceFiles) {

        /**
         * A grant with no quotas of copies and files: the default ones of the application.
         */
        public AgentGrant(boolean allowed, int maxConcurrentRuns, long runTokenBudget, int maxAgents) {
            this(allowed, maxConcurrentRuns, runTokenBudget, maxAgents, 0, 0, 0, 0);
        }
    }

    /**
     * An AI and a protocol a profile points to, both usable by the user.
     */
    record ResolvedProfile(AgentModelProfile profile, AI ai, Protocol protocol) {
    }

    /**
     * Whether the current user can use agents.
     */
    boolean isAllowed() throws Exception;

    /**
     * What the user may do with agents: an administrator everything, others what the permission {@code AGENTS} says,
     * nothing without it.
     *
     * @param user may be the session's proxy of the user, it is loaded again
     */
    AgentGrant getGrant(User user) throws Exception;

    /**
     * Whether the current user may have one more agent: they can use agents and have fewer than
     * {@link AgentGrant#maxAgents()}. The same rule makes {@code save} of a new agent (and so {@link #cloneAgent})
     * fail.
     */
    boolean canCreate() throws Exception;

    /**
     * The system prompt an agent runs with: its own, and when that is blank the default prompt of its owner in the
     * settings, then the one of the application settings, then {@link com.github.enerccio.marginalia.Defaults#DEFAULT_AGENT_PROMPT}.
     * Resolved at every request, so a changed default applies to the agents that follow it.
     */
    String getSystemPrompt(Agent agent) throws Exception;

    /**
     * What a blank system prompt of an agent of the current user means: their default prompt in the settings, else
     * {@link #getAppDefaultSystemPrompt()}. The placeholder of the agent's prompt, and what it is filled with to
     * edit a copy of it.
     */
    String getDefaultSystemPrompt() throws Exception;

    /**
     * What a user who has no default prompt of their own gets: the prompt of the application settings, else
     * {@link com.github.enerccio.marginalia.Defaults#DEFAULT_AGENT_PROMPT}. The placeholder of the user's default prompt.
     */
    String getAppDefaultSystemPrompt() throws Exception;

    /**
     * The default prompt as the administrator stored it, blank when there is none. Administrators only.
     */
    String getAppSystemPrompt() throws Exception;

    /**
     * Stores the default prompt of the application settings, blank removes it. Administrators only.
     */
    void setAppSystemPrompt(String prompt) throws Exception;

    /**
     * What the model is asked to write the summary of a compaction with: the prompt of the agent, and when that is blank the
     * default of its owner in the settings, then the one of the application settings, then
     * {@link com.github.enerccio.marginalia.Defaults#DEFAULT_AGENT_COMPACTION_PROMPT}; the same chain as
     * {@link #getSystemPrompt}.
     */
    String getCompactionPrompt(Agent agent) throws Exception;

    /**
     * What a blank compaction prompt of an agent of the current user means: their default in the settings, else
     * {@link #getAppDefaultCompactionPrompt()}.
     */
    String getDefaultCompactionPrompt() throws Exception;

    /**
     * What a user who has no default compaction prompt of their own gets: the one of the application settings, else the
     * built-in one.
     */
    String getAppDefaultCompactionPrompt() throws Exception;

    /**
     * The default compaction prompt as the administrator stored it, blank when there is none. Administrators only.
     */
    String getAppCompactionPrompt() throws Exception;

    /**
     * Stores the default compaction prompt of the application settings, blank removes it. Administrators only.
     */
    void setAppCompactionPrompt(String prompt) throws Exception;

    /**
     * The agent of the manuscript of the current user, null when there is none.
     */
    Agent findForManuscript(Manuscript manuscript) throws Exception;

    /**
     * The AI and the protocol of the profile when the current user can use them (the AI is their own or shared, the
     * protocol their own), null otherwise.
     */
    ResolvedProfile resolveProfile(AgentModelProfile profile) throws Exception;

    /**
     * Copies the definition of the agent (not its runs) to a new agent of another manuscript of the current user.
     *
     * @param name the name of the copy, the name of the source when blank
     */
    Agent cloneAgent(Agent source, String name, Manuscript target) throws Exception;

    /**
     * Deletes the agents of the manuscript with their runs, what deleting the manuscript does.
     */
    void deleteForManuscript(Manuscript manuscript, boolean hard) throws Exception;

}
