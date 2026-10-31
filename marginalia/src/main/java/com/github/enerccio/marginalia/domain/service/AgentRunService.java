package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.bound.AgentRequestAttributes;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.AgentRunRepository;
import com.github.enerccio.marginalia.domain.service.agent.UuidPair;

import java.util.List;

/**
 * Runs of agents (see {@link AgentRun}) and what happens to them: they start, change {@link AgentRunStatus status}
 * along the allowed transitions, count the tokens they spend and are trashed.
 * <p>
 * The status and the counters are changed only here, by SQL updates: {@code save} does not write them for an existing
 * run. Callers hold a copy of the run that goes stale, so they take the result of {@link #transition} and
 * {@link #addUsage} or load the run again ({@code find}).
 */
public interface AgentRunService extends ExtendableService<AgentRun, AgentRunRepository> {

    /**
     * Starts a run of the agent, in the status {@link AgentRunStatus#RUNNING}. Nothing runs by this: the runner takes
     * the run up.
     *
     * @throws SecurityException     when the user can't use agents
     * @throws IllegalArgumentException when the agent is not an agent of the user
     * @throws IllegalStateException when the agent is disabled, its main model is missing or not usable, it already
     *                               has an unfinished run, or the user has all the concurrent runs they are given
     */
    AgentRun start(Agent agent) throws Exception;

    /**
     * Starts a run as {@link #start(Agent)}, either autonomous or as a conversation with the user.
     *
     * @param interactive true for a conversation (started in the console): the {@code finish} of the agent does not end
     *                    the run, it waits for the next message of the user until they {@link #finish} or stop it;
     *                    false for an autonomous run, which ends with {@code finish}
     */
    AgentRun start(Agent agent, boolean interactive) throws Exception;

    /**
     * Ends a run that waits for the user: the user is satisfied, the run is {@link AgentRunStatus#DONE}.
     *
     * @throws IllegalStateException when the run is not {@link AgentRunStatus#IDLE}
     */
    AgentRun finish(AgentRun run) throws Exception;

    /**
     * The context the run may use: the lowest limit of the agent, of its model profile and of the model itself, 0 when
     * there is none (or the model of the agent can't be resolved).
     */
    long getContextCap(AgentRun run) throws Exception;

    /**
     * The longest answer the model of the run can write, in tokens, 0 when it is not known (or the model of the agent can't be
     * resolved). A call whose arguments need more is cut off.
     */
    long getResponseCap(AgentRun run) throws Exception;

    /**
     * The plan the agent keeps with {@code update_plan}: the one it kept for a restart, else the one of its last call that it still
     * reads; empty for none.
     */
    List<com.github.enerccio.marginalia.domain.service.agent.RunTools.PlanItem> getPlan(AgentRun run) throws Exception;

    /**
     * The findings the agent reported with {@code report}, in the order it made them; empty for none. They are kept in the folder of the
     * run, so they are there after the working copy is gone.
     */
    List<com.github.enerccio.marginalia.domain.service.agent.RunTools.Finding> getFindings(AgentRun run) throws Exception;

    /**
     * Sets the model the working copy of the run writes with: the AI and the protocol of the main model profile of the agent when
     * the agent uses its own model ({@code Agent.useAgentModel}, the default), else the AI and the protocol the book has now. Done
     * every time a worker starts, so a profile or a flag the author changed in the meantime is used. The two are not merged back
     * into the book. Nothing happens for a run without a copy or a model that can't be resolved.
     */
    void useAgentModel(AgentRun run) throws Exception;

    /**
     * Resumes a paused or stopped run (any time later): the run goes to {@link AgentRunStatus#RUNNING} again and the
     * runner takes it up. The same conditions as for {@link #start} apply (the agent is enabled and has a usable
     * model, the user has a free concurrent run), the run keeps its own place so the one-unfinished-run rule is not
     * touched.
     *
     * @param run the run as the caller knows it, the status of the copy is the expected one
     * @throws IllegalStateException when the run can't be resumed from its status or the conditions are not met
     */
    AgentRun resume(AgentRun run) throws Exception;

    /**
     * {@link #resume(AgentRun)} with a note of the author: the agent reads it first when it goes on (after the report of the changes the
     * author made to the book, if there are any). It works for every run, a conversation or an autonomous one, as the note is kept with
     * the run and not in the queue of messages.
     *
     * @param note what the author wants the agent to know, blank for none
     */
    AgentRun resume(AgentRun run, String note) throws Exception;

    /**
     * The runs of the current user that wait to be looked at ({@link AgentRun#isAttention()}): they ask something, wait
     * for a message, finished, failed or were paused for a reason of their own. They stay on the list while the user
     * is away, until {@link #acknowledge} (the console does it when it shows the run).
     */
    List<AgentRun> findNeedingAttention() throws Exception;

    /**
     * The user looked at the run: it no longer needs attention. The open screens of the user are told
     * ({@link AgentAttentionService}).
     *
     * @return the run as it is now
     * @throws IllegalArgumentException when the run is not a run of the current user
     */
    AgentRun acknowledge(AgentRun run) throws Exception;

    /**
     * Asks the run to pause. A run that waits for the user (or for an approval) is paused at once; a working one goes
     * to {@link AgentRunStatus#PAUSING} and the runner pauses it at the next stable point. When no runner is busy with
     * the run ({@link #workerStarted}), nobody can finish the pause, so it is finished here.
     *
     * @return the run in its new status
     * @throws IllegalStateException when the run can't be paused from its status
     */
    AgentRun pause(AgentRun run) throws Exception;

    /**
     * Stops the run, the working copy is kept for the review or dropped. A paused or stopped run is stopped at once; a
     * going on one goes to {@link AgentRunStatus#STOPPING} and the runner ends it, unless no runner is busy with it,
     * then it is ended here.
     *
     * @param keep whether the working copy stays ({@link AgentRunStatus#STOPPED_KEPT}) or is dropped
     * @throws IllegalStateException when the run can't be stopped from its status
     */
    AgentRun stop(AgentRun run, boolean keep) throws Exception;

    /**
     * What a run that is {@link AgentRunStatus#STOPPING} ends in: the status the {@link #stop} asked for,
     * {@link AgentRunStatus#STOPPED_KEPT} where it can't be known (the server was restarted since). Kept in memory
     * while a worker is busy with the run.
     */
    AgentRunStatus getStopTarget(AgentRun run);

    /**
     * The runner ends a stop it saw: the run goes from {@link AgentRunStatus#STOPPING} to the status {@link #stop}
     * asked for, and the working copy is dropped when the run is discarded.
     *
     * @param run the run as the runner knows it, in the status {@code STOPPING}
     * @return the run in its new status
     * @throws IllegalStateException when the run is not stopping
     */
    AgentRun endStop(AgentRun run) throws Exception;

    /**
     * The runner tells that a worker is busy with the run, so that {@link #pause} and {@link #stop} leave the end of
     * the request to it. Not persistent: a restart of the server forgets all workers, see {@link #recoverInterrupted}.
     */
    void workerStarted(AgentRun run);

    /**
     * The runner tells that no worker is busy with the run any more.
     */
    void workerFinished(AgentRun run);

    /**
     * Gives the run another working copy, the one a checkpoint was restored into: the pairs (with the handles) are stored in
     * place of the old ones and the old copy is deleted for good, with its messages and its lorebooks.
     *
     * @param copy  the new copy, as made by {@code ManuscriptService.createAgenticCopyFromSnapshot}
     * @param pairs the pairs of the new copy
     * @return the run with the new copy
     * @throws IllegalArgumentException when the run is not a run of the user or has no copy
     */
    AgentRun replaceWorkingCopy(AgentRun run, Manuscript copy, java.util.Collection<UuidPair> pairs) throws Exception;

    /**
     * The book the agent of the run that has the manuscript as its working copy works on, whoever owns the run; null when the
     * manuscript is not the working copy of a run. Needs no current user, the writing statistics ask from any thread.
     */
    Manuscript findOriginalOfCopy(Manuscript copy) throws Exception;

    /**
     * The run that locks the manuscript, whoever it belongs to; null when the book is free. A book is locked while
     * the run of its agent works or waits for the user ({@link AgentRunStatus#holdsLock()}); paused, stopped and
     * finished runs release it. Reading the book is always allowed, the services that change it ask
     * {@link #checkNotLocked}.
     */
    AgentRun findLockingRun(Manuscript manuscript) throws Exception;

    /**
     * @throws ManuscriptLockedException when a run of the agent of the manuscript holds the lock
     */
    void checkNotLocked(Manuscript manuscript) throws Exception;

    /**
     * The run that locks the lorebook, whoever it belongs to; null when it is free. The lorebooks of a book an agent
     * works on (the one the book links and, transitively, its subbooks) are locked while the book is
     * ({@link #findLockingRun(Manuscript)}), as the run works on a copy of them and the copy is compared with the
     * lorebooks as they were. The working copies of agents ({@code agentic}) are never locked.
     */
    AgentRun findLockingRun(Lorebook lorebook) throws Exception;

    /**
     * @throws LorebookLockedException when a run of an agent whose book uses the lorebook holds the lock
     */
    void checkNotLocked(Lorebook lorebook) throws Exception;

    /**
     * Runs of the agent of the current user, the newest first.
     */
    List<AgentRun> findForAgent(Agent agent) throws Exception;

    /**
     * The unfinished run of the agent of the current user ({@link AgentRunStatus#isUnfinished()}), null when there is
     * none.
     */
    AgentRun findUnfinished(Agent agent) throws Exception;

    /**
     * Changes the status of the run, only along {@link AgentRunStatus#next()}. The caller names the status it knows
     * the run has (the status of {@code run}), the change is refused when the run has another one by now - the user
     * paused it while the runner wanted to go on, say - and the caller has to look again.
     *
     * @param reason why, shown to the user (the error of a failed run); may be null
     * @return the run in the new status
     * @throws IllegalStateException when the change is not allowed or the run is not in the status of {@code run}
     * @throws SecurityException     when the run is not the current user's
     */
    AgentRun transition(AgentRun run, AgentRunStatus to, String reason) throws Exception;

    /**
     * Adds the tokens of one request to the run (and counts the round), safe for parallel threads of the run.
     *
     * @return the run with the new totals
     * @throws IllegalArgumentException when the run does not exist
     */
    AgentRun addUsage(AgentRun run, long prefillTokens, long generatedTokens) throws Exception;

    /**
     * Most tokens the run can spend: the smaller of the limit of its agent and the limit the administrator gave to the
     * user, zero is no limit.
     */
    long getTokenBudget(AgentRun run) throws Exception;

    /**
     * How many sub-agents the run can have working at once: what its agent says, zero (none, no tools to spawn them) when it says nothing.
     */
    int getMaxSubagents(AgentRun run) throws Exception;

    /**
     * How many requests to the model the run can have in the air at once, the agent and its sub-agents together: what the
     * administrator gave to the user, at least one.
     */
    int getMaxConcurrency(AgentRun run) throws Exception;

    /**
     * Whether the run spent the whole {@link #getTokenBudget budget}.
     */
    boolean isBudgetExceeded(AgentRun run) throws Exception;

    /**
     * Deletes the runs of the agent, see {@code delete}.
     */
    void deleteForAgent(Agent agent, boolean hard) throws Exception;

    /**
     * Deleting a run that is not over discards it first ({@link AgentRunStatus#STOPPED_DISCARDED}, the runner sees it
     * at its next change of the status) and takes its working copy with it. The run is no longer unfinished, so it
     * does not block its agent and does not count to the limits.
     */
    @Override
    AgentRun delete(AgentRun run, boolean hard) throws Exception;

    /**
     * The request context to run the threads of the run in (the commands, the function calls, the generation), made
     * from the database: the user is the owner of the run, the language the one of the run, the book the commands
     * work on the working copy of the run or, where it has none, the manuscript of its agent. Every start and every
     * resume makes a new one; the runner closes it when the run stops working. Needs no logged in user and checks
     * nothing: it is for the runner, which got the run from {@link #start} or {@link #resume}.
     *
     * @throws IllegalArgumentException when the run or its owner does not exist
     */
    AgentRequestAttributes openSession(AgentRun run) throws Exception;

    /**
     * What a start of the server does with the runs a runner was busy with, as no runner is alive then: a stop in
     * progress ends with the copy kept, every other run goes to {@link AgentRunStatus#PAUSED}, resumable from the last
     * complete round. Runs of all users, needs no logged in user.
     *
     * @return number of the runs
     */
    int recoverInterrupted() throws Exception;

}
