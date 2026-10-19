package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.bound.AgentRequestAttributes;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.AgentService.ResolvedProfile;
import com.github.enerccio.marginalia.domain.service.InferenceService.RoundUsage;
import com.github.enerccio.marginalia.domain.service.agent.*;
import com.github.enerccio.marginalia.domain.service.agent.RunTools.PlanItem;
import com.github.enerccio.marginalia.domain.service.function.FunctionCallException;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AgentRunnerImpl implements AgentRunner, SmartInitializingSingleton, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(AgentRunnerImpl.class);
    private static final Gson GSON = new Gson();

    // how often a worker that waits for a request or a call looks whether the run was thrown away
    private static final long POLL_MILLIS = 1_000;
    // the status reason holds this much of the report of a finished run
    private static final int MAX_REASON = 2_000;

    @Autowired
    @Lazy
    private AgentRunService runService;

    @Autowired
    @Lazy
    private AgentMessageService messageService;

    @Autowired
    @Lazy
    private AgentService agentService;

    @Autowired
    @Lazy
    private AgentVirtualFileSystemService fileSystemService;

    @Autowired
    @Lazy
    private CommandService commandService;

    @Autowired
    @Lazy
    private InferenceServices inferenceServices;

    @Autowired
    private Localization localization;

    @Autowired
    private AgentApprovalService approvalService;

    private final AtomicInteger threads = new AtomicInteger();
    private final ExecutorService executor = Executors.newCachedThreadPool(task -> {
        Thread thread = new Thread(task, "agent-run-" + threads.incrementAndGet());
        thread.setDaemon(true);
        return thread;
    });
    // runs a worker is busy with: one worker per run
    private final Set<Long> working = ConcurrentHashMap.newKeySet();

    @Override
    public void afterSingletonsInstantiated() {
        try {
            int recovered = runService.recoverInterrupted();
            if (recovered > 0) {
                log.info("{} agent run(s) were interrupted by the restart and are paused", recovered);
            }
        } catch (Exception e) {
            log.error("Failed to recover the interrupted agent runs", e);
        }
    }

    @Override
    public void destroy() {
        executor.shutdownNow();
    }

    @Override
    public boolean isWorking(AgentRun run) {
        return run != null && run.getId() != null && working.contains(run.getId());
    }

    @Override
    public void run(AgentRun run, String firstMessage) throws Exception {
        if (run == null || run.getId() == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        if (StringUtils.isBlank(firstMessage)) {
            throw new IllegalArgumentException("The message is empty");
        }
        if (run.getStatus() != AgentRunStatus.RUNNING) {
            throw new IllegalStateException("A run that is " + run.getStatus() + " is not taken up");
        }
        if (messageService.count(run) > 0) {
            throw new IllegalStateException("The run has worked already");
        }
        messageService.append(run, List.of(LLMChatMessage.of(LLMRole.USER, firstMessage.strip())));
        run(run);
    }

    @Override
    public void run(AgentRun run) {
        if (run == null || run.getId() == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        if (run.getStatus() != AgentRunStatus.RUNNING) {
            throw new IllegalStateException("A run that is " + run.getStatus() + " is not taken up");
        }
        if (!working.add(run.getId())) {
            return;
        }
        // before the thread starts: a pause asked right now has to wait for the worker
        runService.workerStarted(run);
        try {
            log.info("Agent run {} is taken up by a worker", run.getUuid());
            executor.execute(new Worker(run));
        } catch (RuntimeException e) {
            working.remove(run.getId());
            runService.workerFinished(run);
            throw e;
        }
    }

    @Override
    public AgentRun send(AgentRun run, String text) throws Exception {
        if (run == null || run.getId() == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("The message is empty");
        }
        if (run.getStatus() != AgentRunStatus.IDLE) {
            throw new IllegalStateException("A run that is " + run.getStatus() + " does not wait for a message");
        }
        // the change first: when the run is no longer idle, the message is not written
        AgentRun running = runService.transition(run, AgentRunStatus.RUNNING, null);
        // only that there is a message, what the user writes is the story's business
        log.info("Agent run {}: a message of the user ({} characters) goes to the agent", run.getUuid(), text.strip().length());
        messageService.append(running, List.of(LLMChatMessage.of(LLMRole.USER, text.strip())));
        run(running);
        return running;
    }

    /**
     * What the budgets of the run are now, kept by the worker and read by {@code get_run_status}.
     */
    private static final class Progress {
        volatile long rounds;
        volatile long maxRounds;
        volatile long tokens;
        volatile long tokenBudget;
        volatile long contextTokens;
        volatile long contextCap;
        // the longest answer of the model in tokens, 0 when not known
        volatile long responseCap;
        final long startedAt = System.currentTimeMillis();

        String render() {
            StringBuilder text = new StringBuilder();
            text.append("Rounds: ").append(rounds).append(maxRounds > 0 ? " of " + maxRounds : " (no limit)").append('\n');
            text.append("Tokens spent by the run: ").append(tokens).append(tokenBudget > 0 ? " of " + tokenBudget : " (no limit)").append('\n');
            text.append("Context of the model: about ").append(contextTokens).append(contextCap > 0 ? " of " + contextCap : "").append(" tokens\n");
            text.append("Longest answer you can write: ").append(responseCap > 0 ? responseCap + " tokens (a call that needs more is cut off and fails, "
                    + "write long texts in parts)" : "no limit is known").append('\n');
            text.append("Working for ").append((System.currentTimeMillis() - startedAt) / 1000).append(" s in this session of the run");
            return text.toString();
        }
    }

    /**
     * What one request to the model answered.
     */
    private record Round(String text, String reasoning, List<LLMToolCall> calls, RoundUsage usage) {
    }

    /**
     * The end of a worker that is not an exception: the run was thrown away under it.
     */
    private static final class Discarded extends RuntimeException {
        Discarded() {
            super(null, null, false, false);
        }
    }

    private final class Worker implements Runnable {

        private final Long runId;
        private AgentRun run;
        private AgentRequestAttributes attributes;
        private Agent agent;
        private final Progress progress = new Progress();
        // made again when the run is loaded, it depends on whether the run is a conversation
        private RunToolsImpl runTools = new RunToolsImpl(progress::render);

        Worker(AgentRun run) {
            this.runId = run.getId();
            this.run = run;
        }

        @Override
        public void run() {
            try {
                attributes = runService.openSession(run);
                try (InRequestScope _ = new InRequestScope(attributes)) {
                    work();
                } catch (Discarded e) {
                    log.info("Agent run {} was thrown away while it worked", run.getUuid());
                } catch (Throwable e) {
                    log.error("Agent run {} failed", run.getUuid(), e);
                    failed(e);
                }
            } catch (Throwable e) {
                log.error("Agent run {} could not start working", run.getUuid(), e);
            } finally {
                working.remove(runId);
                runService.workerFinished(run);
                if (attributes != null) {
                    attributes.close();
                }
            }
        }

        // ------------------------------------------------------------------ the loop

        private void work() throws Exception {
            run = runService.find(runId);
            runTools = new RunToolsImpl(progress::render, run.isInteractive());
            agent = agentService.findForUser(run.getAgent().getUuid());
            ResolvedProfile profile = agentService.resolveProfile(agent.getMainProfile());
            if (profile == null) {
                pause("The model of the agent is not available");
                return;
            }
            InferenceService inference = inferenceServices.forAIOrThrow(profile.ai());

            List<LLMChatMessage> transcript = new ArrayList<>(messageService.transcript(run));
            prepare(transcript);
            runTools.restorePlan(planOf(transcript));
            FunctionSet tools = tools();

            progress.maxRounds = agent.getMaxRounds() == null ? 0 : agent.getMaxRounds();
            progress.contextCap = runService.getContextCap(run);
            progress.responseCap = TokenLimits.responseTokens(profile.ai(), profile.protocol());
            progress.rounds = run.getRounds();
            progress.tokens = run.getTotalTokens();
            progress.tokenBudget = runService.getTokenBudget(run);
            log.info("Agent run {} of agent \"{}\" works: AI \"{}\", protocol \"{}\", {} message(s) so far, round {}, limits: {} rounds, {} tokens, {} context",
                    run.getUuid(), agent.getName(), profile.ai().getName(), profile.protocol().getName(),
                    transcript.size(), run.getRounds(), progress.maxRounds > 0 ? progress.maxRounds : "no",
                    progress.tokenBudget > 0 ? progress.tokenBudget : "no", progress.contextCap > 0 ? progress.contextCap : "no");

            while (true) {
                // the stable point: a pause or a stop asked meanwhile ends here
                run = runService.find(runId);
                if (run.getStatus() != AgentRunStatus.RUNNING) {
                    settleRequested();
                    return;
                }
                String limit = limit(run, transcript, inference, progress);
                if (limit != null) {
                    pause(limit);
                    return;
                }

                Round round;
                try {
                    round = ask(inference, profile, payload(transcript), tools);
                } catch (Discarded e) {
                    throw e;
                } catch (Exception e) {
                    log.warn("The model of agent run {} could not be asked: {}", run.getUuid(), e.getMessage(), e);
                    pause("The model could not be asked: " + messageOf(e));
                    return;
                }
                run = runService.addUsage(run, round.usage().promptTokens(), round.usage().completionTokens());
                progress.rounds = run.getRounds();
                progress.tokens = run.getTotalTokens();
                progress.contextTokens = round.usage().promptTokens() + round.usage().completionTokens();
                log.info("Agent run {} round {}: {} + {} tokens, context {}{}{}, {}", run.getUuid(), run.getRounds(),
                        round.usage().promptTokens(), round.usage().completionTokens(), progress.contextTokens,
                        progress.contextCap > 0 ? " of " + progress.contextCap : "",
                        round.reasoning() == null ? "" : ", reasoning " + round.reasoning().length() + " characters",
                        round.calls().isEmpty() ? "answers in text" : "calls " + round.calls().stream().map(LLMToolCall::getName)
                                .collect(java.util.stream.Collectors.joining(", ")));

                LLMChatMessage answer = round.calls().isEmpty()
                        ? LLMChatMessage.of(LLMRole.ASSISTANT, round.text())
                        : LLMChatMessage.ofToolCalls(StringUtils.isEmpty(round.text()) ? null : round.text(), round.calls());
                messageService.appendAnswer(run, answer, progress.contextTokens, round.reasoning());
                transcript.add(answer);
                if (round.calls().isEmpty()) {
                    reach(AgentRunStatus.IDLE, null);
                    return;
                }

                runTools.clearRequests();
                List<LLMChatMessage> results = new ArrayList<>();
                for (LLMToolCall call : round.calls()) {
                    long began = System.currentTimeMillis();
                    String result = call(tools, call);
                    // the arguments and the result are the story's business and not logged
                    log.info("Agent run {}: {} {} in {} ms ({} characters)", run.getUuid(), call.getName(),
                            result.startsWith("Error:") ? "failed" : "done", System.currentTimeMillis() - began, result.length());
                    results.add(LLMChatMessage.ofToolResult(call.getId(), result));
                }
                append(transcript, results.toArray(new LLMChatMessage[0]));

                if (runTools.getSummary() != null) {
                    // a conversation goes on after the report, only the user ends it
                    reach(run.isInteractive() ? AgentRunStatus.IDLE : AgentRunStatus.DONE,
                            StringUtils.abbreviate(runTools.getSummary(), MAX_REASON));
                    return;
                }
                if (runTools.getQuestion() != null) {
                    reach(AgentRunStatus.IDLE, StringUtils.abbreviate(runTools.getQuestion(), MAX_REASON));
                    return;
                }
            }
        }

        /**
         * The transcript as the loop can go on with it: the start of a new run, the end of an interrupted round.
         */
        private void prepare(List<LLMChatMessage> transcript) throws Exception {
            if (transcript.isEmpty()) {
                append(transcript, LLMChatMessage.of(LLMRole.USER, Defaults.DEFAULT_AGENT_START_MESSAGE));
                return;
            }
            // a round interrupted between the calls and their results: the calls get an answer, the model is not left
            // with calls nobody answered (providers refuse that)
            int lastCalls = -1;
            for (int i = transcript.size() - 1; i >= 0; i--) {
                if (transcript.get(i).getRole() == LLMRole.ASSISTANT) {
                    lastCalls = transcript.get(i).hasToolCalls() ? i : -1;
                    break;
                }
            }
            if (lastCalls >= 0) {
                Set<String> answered = new HashSet<>();
                for (int i = lastCalls + 1; i < transcript.size(); i++) {
                    answered.add(transcript.get(i).getToolCallId());
                }
                List<LLMChatMessage> missing = new ArrayList<>();
                for (LLMToolCall call : transcript.get(lastCalls).getToolCalls()) {
                    if (!answered.contains(call.getId())) {
                        missing.add(LLMChatMessage.ofToolResult(call.getId(), Defaults.AGENT_INTERRUPTED_CALL));
                    }
                }
                if (!missing.isEmpty()) {
                    append(transcript, missing.toArray(new LLMChatMessage[0]));
                }
            }
            LLMChatMessage last = transcript.getLast();
            if (last.getRole() == LLMRole.ASSISTANT && !last.hasToolCalls()) {
                append(transcript, LLMChatMessage.of(LLMRole.USER, Defaults.DEFAULT_AGENT_RESUME_MESSAGE));
            }
        }

        private void append(List<LLMChatMessage> transcript, LLMChatMessage... messages) throws Exception {
            messageService.append(run, List.of(messages));
            transcript.addAll(List.of(messages));
        }

        private FunctionSet tools() throws Exception {
            VirtualFileSystem files = fileSystemService.open(run);
            // what the commands return, whole, for the tool that saves it to a file
            CommandResults commandResults = new CommandResults();
            return CommandFunctions.forAgent(commandService, agent, l -> localization.getValue(l), this::approve,
                            path -> files.readText(VirtualPath.parse(path)), commandResults)
                    .and(fileSystemService.getFileTools(files, commandResults))
                    .and(inferenceServices.getFunctionRegistry().bind(RunTools.class, runTools));
        }

        /**
         * A destructive command in the {@code ASK} mode: the run waits in {@code WAITING_APPROVAL} until the user decides in
         * the console (or the run is paused or stopped, which refuses it). Whoever decides, what follows runs in the
         * context of the run, never in the session of the user's browser.
         */
        private CompletableFuture<Boolean> approve(String command, Map<String, Object> arguments) {
            CompletableFuture<Boolean> decision = approvalService.request(run, command, arguments);
            try {
                AgentRun current = runService.find(runId);
                if (current.getStatus() == AgentRunStatus.RUNNING) {
                    runService.transition(current, AgentRunStatus.WAITING_APPROVAL, command);
                } else if (current.getStatus() != AgentRunStatus.WAITING_APPROVAL) {
                    // a pause or a stop was asked meanwhile
                    decision.complete(false);
                }
            } catch (Exception e) {
                // the status changed under us, the same
                decision.complete(false);
            }
            Executor inRun = task -> executor.execute(() -> {
                try (InRequestScope _ = new InRequestScope(attributes)) {
                    task.run();
                }
            });
            return decision.whenCompleteAsync((approved, error) -> backToWork(), inRun);
        }

        /**
         * The user decided: the run works again, unless more commands wait or the run was paused or stopped meanwhile.
         */
        private void backToWork() {
            try {
                if (!approvalService.hasPending(runId)) {
                    AgentRun current = runService.find(runId);
                    if (current != null && current.getStatus() == AgentRunStatus.WAITING_APPROVAL) {
                        runService.transition(current, AgentRunStatus.RUNNING, null);
                    }
                }
            } catch (Exception e) {
                log.debug("Agent run {} did not go back to work after an approval: {}", runId, e.getMessage());
            }
        }

        /**
         * What the model can't know: how much it can read and how much it can write in one answer. Without it a model
         * tries to write a file of 100 000 tokens in one call and the call is cut off.
         */
        private String limitsNote() {
            StringBuilder note = new StringBuilder("Limits of the model you run on:");
            if (progress.contextCap > 0) {
                note.append("\n- Context: ").append(progress.contextCap).append(" tokens, everything you read at once: this prompt, the conversation and "
                        + "the results of your calls. When it is full the run pauses.");
            }
            if (progress.responseCap > 0) {
                note.append("\n- One answer of yours: at most ").append(progress.responseCap).append(" tokens, the text you write and the arguments of "
                        + "your calls together. A call that needs more (a long file, a long text) is cut off and fails: write long texts in parts, "
                        + "with write_file for the first and append_file for the rest, a part per call.");
            }
            return note.append("\nget_run_status tells how much of this is used.").toString();
        }

        private List<LLMChatMessage> payload(List<LLMChatMessage> transcript) {
            StringBuilder system = new StringBuilder(StringUtils.defaultString(agent.getSystemPrompt()).strip());
            if (StringUtils.isNotBlank(agent.getInstructions())) {
                system.append("\n\n# Instructions of the author\n\n").append(agent.getInstructions().strip());
            }
            system.append("\n\n").append(Defaults.AGENT_RUNNER_NOTE.strip());
            if (run.isInteractive()) {
                system.append("\n\n").append(Defaults.AGENT_INTERACTIVE_NOTE.strip());
            }
            if (progress.contextCap > 0 || progress.responseCap > 0) {
                system.append("\n\n").append(limitsNote());
            }
            List<LLMChatMessage> payload = new ArrayList<>();
            payload.add(LLMChatMessage.of(LLMRole.SYSTEM, system.toString()));
            payload.addAll(transcript);
            return payload;
        }

        // ------------------------------------------------------------------ budgets

        /**
         * Why the run may not make another request, null when it may.
         */
        private String limit(AgentRun current, List<LLMChatMessage> transcript, InferenceService inference,
                             Progress progress) throws Exception {
            if (progress.maxRounds > 0 && current.getRounds() >= progress.maxRounds) {
                return "The run used all its " + progress.maxRounds + " rounds";
            }
            if (runService.isBudgetExceeded(current)) {
                return "The run spent its budget of " + progress.tokenBudget + " tokens";
            }
            if (progress.contextCap > 0) {
                if (progress.contextTokens == 0) {
                    long estimate = 0;
                    for (LLMChatMessage message : payload(transcript)) {
                        estimate += inference.countTokensApprox(StringUtils.defaultString(message.getContent()));
                    }
                    progress.contextTokens = estimate;
                }
                if (progress.contextTokens >= progress.contextCap) {
                    return "The context of the model is full (" + progress.contextTokens + " of " + progress.contextCap
                            + " tokens), older rounds can't be summarized yet";
                }
            }
            return null;
        }

        // ------------------------------------------------------------------ requests and calls

        private Round ask(InferenceService inference, ResolvedProfile profile, List<LLMChatMessage> payload,
                          FunctionSet tools) throws Exception {
            CompletableFuture<Round> answer = new CompletableFuture<>();
            CancellationToken token = inference.round(payload, profile.protocol(), tools, new InferenceService.InferenceRoundCallback() {
                @Override
                public void onRound(String text, List<LLMToolCall> calls, RoundUsage usage) {
                    onRound(text, null, calls, usage);
                }

                @Override
                public void onRound(String text, String reasoning, List<LLMToolCall> calls, RoundUsage usage) {
                    answer.complete(new Round(text, reasoning, calls, usage));
                }

                @Override
                public void onCancel() {
                    answer.completeExceptionally(new CancellationException("The request was canceled"));
                }

                @Override
                public void onError(Throwable exception) {
                    answer.completeExceptionally(exception);
                }
            });
            try {
                return await(answer, token::cancel);
            } catch (ExecutionException e) {
                throw e.getCause() instanceof Exception cause ? cause : e;
            }
        }

        /**
         * Runs one call of the model; whatever goes wrong is the text it gets (as the inference service writes it).
         */
        private String call(FunctionSet tools, LLMToolCall call) throws Exception {
            Executor onThread = task -> CompletableFuture.runAsync(() -> {
                try (InRequestScope _ = new InRequestScope(attributes)) {
                    task.run();
                }
            }, executor);
            AtomicReference<Boolean> canceled = new AtomicReference<>(false);
            CompletableFuture<String> answer = tools.call(call.getName(), call.getArguments(), onThread, canceled::get);
            try {
                return await(answer, () -> canceled.set(true));
            } catch (ExecutionException e) {
                Throwable error = e.getCause();
                if (error instanceof FunctionCallException failure && failure.getKind() != FunctionCallException.Kind.FAILED) {
                    log.info("Call of function {} refused: {}", call.getName(), failure.getMessage());
                } else {
                    log.warn("Function {} called by the model failed", call.getName(), error);
                }
                return "Error: " + messageOf(error);
            }
        }

        /**
         * Waits for the future, and gives up when the run is thrown away meanwhile (its status is checked every
         * {@link #POLL_MILLIS}); a pause or a stop waits for the answer.
         */
        private <T> T await(CompletableFuture<T> future, Runnable cancel) throws Exception {
            while (true) {
                try {
                    return future.get(POLL_MILLIS, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    AgentRun current = runService.find(runId);
                    if (current == null || current.isDeleted() || current.getStatus() == AgentRunStatus.STOPPED_DISCARDED) {
                        cancel.run();
                        throw new Discarded();
                    }
                } catch (CancellationException e) {
                    AgentRun current = runService.find(runId);
                    if (current == null || current.isDeleted() || current.getStatus() == AgentRunStatus.STOPPED_DISCARDED) {
                        throw new Discarded();
                    }
                    throw new ExecutionException(e);
                }
            }
        }

        // ------------------------------------------------------------------ the status

        /**
         * The run goes to the status from RUNNING. When a pause or a stop was asked meanwhile, it wins: the run
         * pauses or stops here, the reason of the original change is the one in the transcript.
         */
        private void reach(AgentRunStatus target, String reason) throws Exception {
            run = runService.find(runId);
            if (run.getStatus() == AgentRunStatus.RUNNING) {
                try {
                    run = runService.transition(run, target, reason);
                    return;
                } catch (IllegalStateException e) {
                    run = runService.find(runId);
                }
            }
            settleRequested();
        }

        /**
         * Pauses the run with the reason, the way a request of the user would have.
         */
        private void pause(String reason) throws Exception {
            run = runService.find(runId);
            if (run.getStatus() == AgentRunStatus.RUNNING) {
                try {
                    run = runService.transition(run, AgentRunStatus.PAUSING, reason);
                } catch (IllegalStateException e) {
                    run = runService.find(runId);
                }
            }
            if (run.getStatus() == AgentRunStatus.PAUSING) {
                runService.transition(run, AgentRunStatus.PAUSED, reason);
            } else {
                settleRequested();
            }
        }

        /**
         * Ends the pause or the stop somebody asked for; any other status is somebody else's doing.
         */
        private void settleRequested() throws Exception {
            switch (run.getStatus()) {
                case PAUSING -> run = runService.transition(run, AgentRunStatus.PAUSED, run.getStatusReason());
                case STOPPING -> run = runService.endStop(run);
                default -> log.info("Agent run {} is {} now, the worker leaves it", run.getUuid(), run.getStatus());
            }
        }

        private void failed(Throwable error) {
            try {
                AgentRun current = runService.find(runId);
                String reason = StringUtils.abbreviate("The run failed: " + messageOf(error), MAX_REASON);
                if (current != null && current.getStatus().canChangeTo(AgentRunStatus.FAILED)) {
                    runService.transition(current, AgentRunStatus.FAILED, reason);
                }
            } catch (Exception e) {
                log.error("Failed to mark agent run {} as failed", run.getUuid(), e);
            }
        }
    }

    private static String messageOf(Throwable error) {
        return StringUtils.defaultIfEmpty(error.getMessage(), error.getClass().getSimpleName());
    }

    /**
     * The plan the model wrote last: the arguments of the last {@code update_plan} call of the transcript.
     */
    public static List<PlanItem> planOf(List<LLMChatMessage> transcript) {
        for (int i = transcript.size() - 1; i >= 0; i--) {
            for (LLMToolCall call : transcript.get(i).getToolCalls()) {
                if (!"update_plan".equals(call.getName())) {
                    continue;
                }
                try {
                    JsonArray items = GSON.fromJson(call.getArguments(), JsonObject.class).getAsJsonArray("items");
                    List<PlanItem> plan = new ArrayList<>();
                    for (JsonElement item : items) {
                        JsonObject object = item.getAsJsonObject();
                        plan.add(new PlanItem(object.get("text").getAsString(),
                                object.has("status") ? object.get("status").getAsString() : "pending"));
                    }
                    return plan;
                } catch (RuntimeException e) {
                    // a plan the tool refused, the one before it stands
                    continue;
                }
            }
        }
        return List.of();
    }
}
