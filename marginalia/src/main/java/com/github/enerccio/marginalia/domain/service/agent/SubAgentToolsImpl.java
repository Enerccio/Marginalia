package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.service.agent.SubAgents.State;
import com.github.enerccio.marginalia.domain.service.agent.SubAgents.Sub;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * {@link SubAgentTools} and {@link SubAgentControlTools} of one run over its {@link SubAgents}. Starting a sub-agent hands it
 * to the runner, which runs its loop; the call returns at once while fewer sub-agents than the limit work, and when it reaches
 * the limit it waits until one ends (with a limit of one that is the sub-agent just started, so the call returns its answer).
 * <p>
 * When the run is paused the sub-agents are suspended, and a call that waits for one gets {@link #SUSPENDED} instead of an
 * answer: the runner does not store it, the call is made again when the run is resumed ({@code spawn_agent} then finds the
 * sub-agent it started by the id of the call and waits for it again).
 */
public final class SubAgentToolsImpl implements SubAgentTools, SubAgentControlTools {

    /**
     * What a call that waits for a sub-agent returns when the run was paused meanwhile, not an answer to give to the model.
     */
    public static final String SUSPENDED = "\u0000suspended";

    private final SubAgents agents;
    private final BiConsumer<Sub, String> starter;
    private final Supplier<String> callId;
    private final Supplier<String> resumedCall;
    private final Function<String, String> profiles;

    /**
     * @param starter     starts the loop of the sub-agent with its first message; a failure of it ends the sub-agent
     * @param callId      the id of the call of the agent that is being made, null when it is not known
     * @param resumedCall the id of the call that is made again because the run was paused while it waited, null for any other
     *                    (a model may use an id again in another round, so only the call the runner says is a resumed one is)
     * @param profiles    the role of the profile a sub-agent may run on for the role the model asked for (null when it left it out):
     *                    null for the main profile, the role as the agent spells it for another; throws an
     *                    {@code IllegalArgumentException} with the text for the model when the agent may not use it
     */
    public SubAgentToolsImpl(SubAgents agents, BiConsumer<Sub, String> starter, Supplier<String> callId, Supplier<String> resumedCall,
                             Function<String, String> profiles) {
        this.profiles = profiles;
        this.agents = agents;
        this.starter = starter;
        this.callId = callId;
        this.resumedCall = resumedCall;
    }

    @Override
    public void spawn(FunctionContinuation<String> done, String task, String context, String profile) {
        if (StringUtils.isBlank(task)) {
            done.fail("The task is empty: say what the sub-agent has to do");
            return;
        }
        String role;
        try {
            role = profiles.apply(StringUtils.trimToNull(profile));
        } catch (IllegalArgumentException e) {
            done.fail(e.getMessage());
            return;
        }
        Sub started = agents.findByCall(resumedCall.get());
        if (started != null) {
            // the call was made before, the run was paused while it waited: the sub-agent is not started again
            goOn(done, started);
            return;
        }
        // a call made while the limit is reached (the one before it was cut off) waits for a place first
        if (!agents.whenSlotFree(() -> spawn(done, task, context, profile))) {
            return;
        }
        Sub sub;
        try {
            sub = agents.create(task.strip(), callId.get(), role);
        } catch (IllegalStateException e) {
            done.fail(e.getMessage());
            return;
        }
        Set<String> endedBefore = ended();
        try {
            starter.accept(sub, StringUtils.isBlank(context) ? task.strip() : task.strip() + "\n\nMaterial:\n" + context.strip());
        } catch (RuntimeException e) {
            agents.finish(sub, State.FAILED, "It could not be started: " + StringUtils.defaultIfEmpty(e.getMessage(), e.getClass().getSimpleName()));
            done.fail("The sub-agent could not be started: " + sub.result());
            return;
        }
        waitFor(done, sub, endedBefore);
    }

    /**
     * What the call of {@code spawn_agent} that is made again answers: as it did the first time, for the sub-agent that call started.
     */
    private void goOn(FunctionContinuation<String> done, Sub sub) {
        if (sub.state() == State.SUSPENDED) {
            done.complete(SUSPENDED);
            return;
        }
        waitFor(done, sub, ended());
    }

    private void waitFor(FunctionContinuation<String> done, Sub sub, Set<String> endedBefore) {
        if (agents.whenSlotFree(() -> {
            if (!done.isCancelled()) {
                done.complete(waited(sub, endedBefore));
            }
        })) {
            done.complete(started(sub));
        }
    }

    /**
     * What a spawn that had to wait returns: what ended meanwhile. A spawn that did not have to wait returns {@link #started}.
     */
    private String waited(Sub sub, Set<String> endedBefore) {
        List<Sub> ended = agents.all().stream().filter(candidate -> candidate.state().isFinished() && !endedBefore.contains(candidate.handle())).toList();
        if (sub.state() == State.SUSPENDED || ended.stream().anyMatch(candidate -> candidate.state() == State.SUSPENDED)) {
            return SUSPENDED;
        }
        if (ended.isEmpty()) {
            return started(sub);
        }
        return "Sub-agent " + sub.handle() + " was started, and the limit of " + agents.max() + " sub-agent"
                + (agents.max() == 1 ? "" : "s") + " working at once was reached, so this call waited until " + (ended.size() == 1 ? "one" : "some")
                + " ended.\n\n" + ended.stream().map(SubAgentToolsImpl::report).collect(Collectors.joining("\n\n"));
    }

    private String started(Sub sub) {
        return "Sub-agent " + sub.handle() + " was started and works on its own now (" + agents.live() + " of " + agents.max()
                + " working). Ask for its answer with await_agent when you need it.";
    }

    private Set<String> ended() {
        Set<String> ended = new HashSet<>();
        agents.all().stream().filter(sub -> sub.state().isFinished()).forEach(sub -> ended.add(sub.handle()));
        return ended;
    }

    @Override
    public void await(FunctionContinuation<String> done, String handle, Integer timeoutSeconds) {
        Sub sub = agents.find(handle);
        if (sub == null) {
            done.fail(unknown(handle));
            return;
        }
        if (sub.state().isFinished()) {
            done.complete(report(sub));
            return;
        }
        var wait = sub.finished().copy();
        if (timeoutSeconds != null && timeoutSeconds > 0) {
            wait = wait.orTimeout(timeoutSeconds, TimeUnit.SECONDS);
        }
        wait.whenComplete((ended, error) -> {
            if (!done.isCancelled()) {
                done.complete(error == null ? report(sub) : sub.handle() + " is still working after " + timeoutSeconds + " seconds: "
                        + line(sub));
            }
        });
    }

    @Override
    public void status(FunctionContinuation<String> done, String handle) {
        if (StringUtils.isNotBlank(handle)) {
            Sub sub = agents.find(handle);
            done.complete(sub == null ? unknown(handle) : line(sub));
            return;
        }
        List<Sub> all = agents.all();
        done.complete(all.isEmpty() ? "You have started no sub-agents." : all.stream().map(SubAgentToolsImpl::line)
                .collect(Collectors.joining("\n")));
    }

    @Override
    public void cancel(FunctionContinuation<String> done, String handle) {
        Sub sub = agents.find(handle);
        if (sub == null) {
            done.fail(unknown(handle));
            return;
        }
        if (sub.state().isFinished()) {
            done.complete(sub.handle() + " is over already (" + sub.state().name().toLowerCase() + ").");
            return;
        }
        sub.cancel();
        done.complete("Cancel asked for " + sub.handle() + "; it stops at its next step. What it changed already stays.");
    }

    private String unknown(String handle) {
        List<Sub> all = agents.all();
        return "There is no sub-agent " + StringUtils.abbreviate(StringUtils.defaultString(handle), 40) + (all.isEmpty() ? ". You have started none."
                : ". Yours: " + all.stream().map(Sub::handle).collect(Collectors.joining(", ")));
    }

    /**
     * One line about a sub-agent: its state and what it did.
     */
    static String line(Sub sub) {
        return sub.handle() + (sub.profile() == null ? "" : " on " + sub.profile()) + ": " + (sub.state() == State.SUSPENDED ? "paused" : sub.state().name().toLowerCase()) + ", " + sub.rounds() + " round(s), " + sub.tokens() + " tokens"
                + (sub.lastAction() == null || sub.state().isFinished() ? "" : ", last: " + sub.lastAction())
                + ", task: " + StringUtils.abbreviate(sub.task().replaceAll("\\s+", " "), 120);
    }

    /**
     * The answer of a sub-agent that is over, or the reason it did not give one.
     */
    static String report(Sub sub) {
        String head = sub.handle() + (sub.profile() == null ? "" : " on " + sub.profile()) + " (" + sub.rounds() + " round(s), " + sub.tokens() + " tokens) ";
        String text = StringUtils.defaultString(sub.result());
        return switch (sub.state()) {
            case SUSPENDED -> SUSPENDED;
            case DONE -> head + "is done. Its answer:\n" + (text.length() > Defaults.AGENT_SUBAGENT_RESULT_CHARS
                    ? text.substring(0, Defaults.AGENT_SUBAGENT_RESULT_CHARS) + "\n[The answer is cut off here, it had " + text.length()
                    + " characters. " + (sub.resultFile() != null ? "All of it is in the file " + sub.resultFile() + ", read it with read_file."
                    : "Ask the next sub-agent to write long results to a file of its folder " + sub.folder() + ".") + "]" : text);
            case FAILED -> head + "failed: " + text + " What it changed before stays in the book.";
            case CANCELLED -> head + "was stopped: " + text + " What it changed before stays in the book.";
            case RUNNING -> head + "is still working.";
        };
    }
}
