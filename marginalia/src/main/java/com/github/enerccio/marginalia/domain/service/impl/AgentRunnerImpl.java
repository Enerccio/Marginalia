package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.bound.AgentRequestAttributes;
import com.github.enerccio.marginalia.domain.collections.AgentBatchMode;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.AgentModelProfile;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentMessage;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.AgentService.ResolvedProfile;
import com.github.enerccio.marginalia.domain.service.InferenceService.RoundUsage;
import com.github.enerccio.marginalia.domain.service.agent.*;
import com.github.enerccio.marginalia.domain.service.agent.RunTools.PlanItem;
import com.github.enerccio.marginalia.domain.service.agent.SubAgents.State;
import com.github.enerccio.marginalia.domain.service.agent.SubAgents.Sub;
import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.domain.service.command.CommandRun;
import com.github.enerccio.marginalia.domain.service.command.CommandScope;
import com.github.enerccio.marginalia.domain.service.function.FunctionCallException;
import com.github.enerccio.marginalia.domain.service.function.FunctionDefinition;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.domain.service.manual.ManualTools;
import com.github.enerccio.marginalia.domain.service.manual.ManualToolsImpl;
import com.github.enerccio.marginalia.domain.traits.BatchPolicy;
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
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class AgentRunnerImpl implements AgentRunner, SmartInitializingSingleton, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(AgentRunnerImpl.class);
    private static final Gson GSON = new Gson();

    // how often a worker that waits for a request or a call looks whether the run was thrown away
    private static final long POLL_MILLIS = 1_000;
    // how many commands of a model of the agent are named in the note about the models for sub-agents, the rest is counted
    private static final int MAX_NAMED_COMMANDS = 12;
    // a conversation in these statuses is written to by queueing the message, the worker reads it at the end of the turn
    private static final Set<AgentRunStatus> QUEUEING = EnumSet.of(AgentRunStatus.RUNNING, AgentRunStatus.WAITING_APPROVAL,
            AgentRunStatus.PAUSING);
    // the statuses in which a run keeps what was written to it: it works, waits, or is paused and can be resumed
    private static final Set<AgentRunStatus> LIVE = EnumSet.of(AgentRunStatus.RUNNING, AgentRunStatus.IDLE,
            AgentRunStatus.WAITING_APPROVAL, AgentRunStatus.PAUSING, AgentRunStatus.PAUSED);
    // the status reason holds this much of the report of a finished run
    private static final int MAX_REASON = 2_000;

    // the tools that change neither the working copy nor the workspace; every other call may, and a round of them needs a checkpoint first
    private static final Set<String> NO_CHANGE_TOOLS = Set.of("finish", "ask_user", "update_plan", "get_run_status", "request_compaction",
            "checkpoint", "checkpoint_list", "checkpoint_restore", "checkpoint_delete", "get_sync_report", "view_image", "list_files", "read_file",
            "grep_files", "list_command_results");
    // the tools that start, wait for and stop sub-agents: they change nothing themselves (and are not dropped as repeats: two spawns of one task are two)
    private static final Set<String> SUBAGENT_TOOLS = Set.of("spawn_agent", "await_agent", "agent_status", "cancel_agent");
    private static final com.fasterxml.jackson.databind.ObjectMapper JSON = new com.fasterxml.jackson.databind.ObjectMapper();
    // how many times in a row a model that wrote an unreadable call is asked again (with a notice) before the run pauses
    private static final int MAX_UNREADABLE_CALLS = 3;
    private static final java.util.regex.Pattern UNREADABLE_CALL = java.util.regex.Pattern.compile(
            "(?is)failed to parse tool call|tool call arguments|arguments as json|invalid tool call|malformed tool call");
    // the calls that wait for a sub-agent: a pause leaves them unanswered, a resume makes them again
    private static final Set<String> WAITING_TOOLS = Set.of("spawn_agent", "await_agent");
    // images the model is shown at once: the newest ones; an older one is a note that view_image shows it again
    static final int MAX_SHOWN_IMAGES = 4;

    @Autowired
    @Lazy
    private AgentRunService runService;

    @Autowired
    private AIGate aiGate;

    // the runs the author paused "at once": the worker gives up the request or the call that is going on (in memory, like the inboxes)
    private final Set<Long> interrupts = ConcurrentHashMap.newKeySet();

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
    private CommandScopes commandScopes;

    @Autowired
    @Lazy
    private AgentRunPairService pairService;

    @Autowired
    @Lazy
    private CheckpointService checkpointService;

    @Autowired
    @Lazy
    private InferenceServices inferenceServices;

    @Autowired
    private Localization localization;

    @Autowired
    @Lazy
    private ManualService manualService;

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
    // the messages the author wrote to a conversation while it worked, by run; in memory only, like the approvals
    private final Map<Long, Inbox> inboxes = new ConcurrentHashMap<>();

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
    public AgentRun pauseNow(AgentRun run) throws Exception {
        if (run == null || run.getId() == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        // set first: the worker looks at it when it waits, the pause alone only changes the status
        interrupts.add(run.getId());
        try {
            AgentRun current = runService.pause(run);
            if (current.getStatus() != AgentRunStatus.PAUSING || !isWorking(current)) {
                interrupts.remove(run.getId());
            }
            return current;
        } catch (Exception | Error e) {
            interrupts.remove(run.getId());
            throw e;
        }
    }

    @Override
    public AgentContext context(AgentRun run) throws Exception {
        AgentRun stored = run == null || run.getUuid() == null ? null : runService.findForUser(run.getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        // nothing starts: the worker only reads, in the context of the run
        return new Worker(stored, null).describe();
    }

    @Override
    public AgentDiagnostics.Report diagnostics(AgentRun run) throws Exception {
        AgentRun stored = run == null || run.getUuid() == null ? null : runService.findForUser(run.getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        return AgentDiagnostics.analyze(stored.getAgent() == null ? null : stored.getAgent().getName(), stored,
                messageService.messages(stored), messageService.threads(stored));
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
        start(run, null);
    }

    /**
     * Gives the run a worker unless it has one.
     *
     * @param initial what the worker delivers before it goes on: queued messages and commands of the author, null for none
     * @return whether the run got a worker, false when one is busy with it already
     */
    private boolean start(AgentRun run, List<Queued> initial) {
        if (!working.add(run.getId())) {
            return false;
        }
        // before the thread starts: a pause asked right now has to wait for the worker
        runService.workerStarted(run);
        try {
            log.info("Agent run {} is taken up by a worker", run.getUuid());
            executor.execute(new Worker(run, initial));
        } catch (RuntimeException e) {
            working.remove(run.getId());
            runService.workerFinished(run);
            throw e;
        }
        return true;
    }

    @Override
    public AgentRun send(AgentRun run, String text, List<String> images) throws Exception {
        if (run == null || run.getId() == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        List<String> attached = images == null ? List.of() : images;
        if (StringUtils.isBlank(text) && attached.isEmpty()) {
            throw new IllegalArgumentException("The message is empty");
        }
        boolean queue = run.isInteractive() && QUEUEING.contains(run.getStatus());
        if (run.getStatus() != AgentRunStatus.IDLE && !queue) {
            throw new IllegalStateException("A run that is " + run.getStatus() + " does not wait for a message");
        }
        if (!queue && working.contains(run.getId())) {
            // a command of the author is being run for the run that waits: it writes the transcript
            throw new IllegalStateException("A command is running, the message can be sent when it is done");
        }
        LLMChatMessage message = LLMChatMessage.of(LLMRole.USER, StringUtils.defaultString(text).strip());
        if (!attached.isEmpty()) {
            attach(run, message, attached);
        }
        if (queue) {
            return enqueue(run, message);
        }
        // the change first: when the run is no longer idle, the message is not written
        AgentRun running = runService.transition(run, AgentRunStatus.RUNNING, null);
        // only that there is a message, what the user writes is the story's business
        log.info("Agent run {}: a message of the user ({} characters, {} image(s)) goes to the agent", run.getUuid(),
                StringUtils.defaultString(text).strip().length(), attached.size());
        messageService.append(running, List.of(message));
        run(running);
        return running;
    }

    /**
     * Queues the message of a conversation that works, to be read at the end of its turn. The worker decides to end the
     * turn under the lock of the inbox, so the message is either seen by it, or the run is {@code IDLE} here and the
     * message is sent as usual.
     */
    private AgentRun enqueue(AgentRun run, LLMChatMessage message) throws Exception {
        Inbox inbox = inboxes.computeIfAbsent(run.getId(), id -> new Inbox());
        synchronized (inbox) {
            AgentRun current = runService.find(run.getId());
            if (QUEUEING.contains(current.getStatus())) {
                inbox.add(new Queued(UUID.randomUUID().toString(), message, null));
                log.info("Agent run {}: a message of the user ({} characters) waits for the end of the turn", run.getUuid(),
                        message.getContent().length());
                return current;
            }
            if (current.getStatus() != AgentRunStatus.IDLE) {
                throw new IllegalStateException("A run that is " + current.getStatus() + " does not wait for a message");
            }
            // the turn ended while the message was made
            AgentRun running = runService.transition(current, AgentRunStatus.RUNNING, null);
            messageService.append(running, List.of(message));
            run(running);
            return running;
        }
    }

    @Override
    public List<String> consoleCommands(Agent agent) {
        List<String> names = new ArrayList<>();
        names.add(COMPACT);
        names.add(CLEAR_WORKSPACE);
        Set<String> allowed = agent == null ? Set.of() : new HashSet<>(agent.getAllowedCommands());
        commandService.getCommands().stream()
                .filter(definition -> allowed.contains(definition.commandName()) && definition.availableForAgent()
                        && !definition.dataSeverity().isDestructive())
                .map(CommandDefinition::commandName).distinct().sorted().forEach(names::add);
        return names;
    }

    @Override
    public AgentRun command(AgentRun run, String line) throws Exception {
        if (run == null || run.getId() == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        String text = StringUtils.strip(line);
        if (text == null || text.length() < 2 || text.charAt(0) != '/') {
            throw new IllegalArgumentException("A command starts with a slash");
        }
        Agent agent = agentService.findForUser(run.getAgent().getUuid());
        String name = commandName(text);
        if (agent == null || !consoleCommands(agent).contains(name)) {
            throw new IllegalArgumentException("The command /" + name + " is not one the author can run on this agent");
        }
        Queued item = new Queued(UUID.randomUUID().toString(), null, text);
        AgentRun current = runService.find(run.getId());
        if (current == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        if (QUEUEING.contains(current.getStatus())) {
            if (!current.isInteractive()) {
                throw new IllegalStateException("A command can be queued only for a conversation, the agent is working on its own");
            }
            Inbox inbox = inboxes.computeIfAbsent(current.getId(), id -> new Inbox());
            synchronized (inbox) {
                AgentRun again = runService.find(current.getId());
                if (QUEUEING.contains(again.getStatus())) {
                    inbox.add(item);
                    log.info("Agent run {}: the command /{} of the user waits for the end of the turn", current.getUuid(), name);
                    return again;
                }
                current = again;
            }
        }
        if (current.getStatus() != AgentRunStatus.IDLE && current.getStatus() != AgentRunStatus.PAUSED) {
            throw new IllegalStateException("A run that is " + current.getStatus() + " can't be given a command");
        }
        // the run waits: a worker that only does the command, the run stays as it is
        log.info("Agent run {}: the command /{} of the user is run", current.getUuid(), name);
        if (!start(current, List.of(item))) {
            throw new IllegalStateException("A command is running, the next one can be given when it is done");
        }
        return current;
    }

    /**
     * The name of the command in the line, without the slash.
     */
    private static String commandName(String line) {
        int end = 1;
        while (end < line.length() && !Character.isWhitespace(line.charAt(end))) {
            end++;
        }
        return line.substring(1, end);
    }

    /**
     * What follows the name of the command in the line, trimmed.
     */
    private static String commandArguments(String line) {
        return line.substring(commandName(line).length() + 1).strip();
    }

    /**
     * The words of the arguments, a word with spaces in double quotes.
     */
    private static List<String> words(String arguments) {
        List<String> words = new ArrayList<>();
        StringBuilder word = new StringBuilder();
        boolean quoted = false;
        boolean started = false;
        for (char c : arguments.toCharArray()) {
            if (c == '"') {
                quoted = !quoted;
                started = true;
            } else if (Character.isWhitespace(c) && !quoted) {
                if (started) {
                    words.add(word.toString());
                    word.setLength(0);
                    started = false;
                }
            } else {
                word.append(c);
                started = true;
            }
        }
        if (started) {
            words.add(word.toString());
        }
        return words;
    }

    @Override
    public List<QueuedMessage> queued(AgentRun run) {
        if (run == null || run.getId() == null) {
            return List.of();
        }
        Inbox inbox = inboxes.get(run.getId());
        if (inbox == null) {
            return List.of();
        }
        if (!LIVE.contains(run.getStatus())) {
            // the run is over (or was stopped), what was written to it is dropped
            inboxes.remove(run.getId());
            return List.of();
        }
        return inbox.snapshot();
    }

    @Override
    public void force(AgentRun run, String id) throws Exception {
        if (run == null || run.getId() == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        Inbox inbox = inboxes.get(run.getId());
        if (inbox == null) {
            return;
        }
        synchronized (inbox) {
            if (!inbox.contains(id)) {
                return;
            }
            AgentRun current = runService.find(run.getId());
            switch (current.getStatus()) {
                case RUNNING, WAITING_APPROVAL -> inbox.force(id);
                case IDLE -> {
                    // the turn ended meanwhile, nothing to throw away; a message makes the agent answer, commands alone do not
                    List<Queued> due = inbox.takeThrough(id);
                    boolean message = due.stream().anyMatch(item -> item.command() == null);
                    AgentRun running = message ? runService.transition(current, AgentRunStatus.RUNNING, null) : current;
                    if (!start(running, due)) {
                        throw new IllegalStateException("The agent is busy, try again in a moment");
                    }
                }
                default -> throw new IllegalStateException("A run that is " + current.getStatus() + " can't be given a message now");
            }
        }
    }

    @Override
    public boolean cancelQueued(AgentRun run, String id) throws Exception {
        if (run == null || run.getId() == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        Inbox inbox = inboxes.get(run.getId());
        if (inbox == null) {
            return false;
        }
        boolean removed = inbox.remove(id);
        if (removed) {
            log.info("Agent run {}: a queued message of the user was taken back", run.getUuid());
        }
        return removed;
    }

    /**
     * The messages as one message of the user: some chat templates refuse two of the same role in a row.
     */
    private static LLMChatMessage merged(List<Queued> due) {
        if (due.size() == 1) {
            return due.getFirst().message();
        }
        LLMChatMessage merged = LLMChatMessage.of(LLMRole.USER, due.stream().map(queued -> queued.message().getContent())
                .collect(java.util.stream.Collectors.joining("\n\n")));
        due.forEach(queued -> queued.message().getAttachments().forEach(merged::addAttachment));
        return merged;
    }

    /**
     * A message the author wrote while the run worked.
     */
    private record Queued(String id, LLMChatMessage message, String command) {
    }

    /**
     * The queued messages of one run, and the wish of the author to have one of them now. All of it is read and changed
     * under the lock of the object, which is also what the worker holds when it decides that the turn is over.
     */
    private static final class Inbox {
        private final List<Queued> queued = new ArrayList<>();
        // the message the author wants delivered now, null when none
        private String forced;

        synchronized void add(Queued message) {
            queued.add(message);
        }

        synchronized boolean contains(String id) {
            return queued.stream().anyMatch(message -> message.id().equals(id));
        }

        synchronized List<QueuedMessage> snapshot() {
            return queued.stream().map(item -> item.command() != null ? new QueuedMessage(item.id(), item.command(), true)
                    : new QueuedMessage(item.id(), item.message().getContent())).toList();
        }

        /**
         * Removes the queued message with the id (and the wish to have it now).
         *
         * @return whether it was queued
         */
        synchronized boolean remove(String id) {
            boolean removed = queued.removeIf(item -> item.id().equals(id));
            if (removed && id.equals(forced)) {
                forced = null;
            }
            return removed;
        }

        synchronized void force(String id) {
            forced = id;
        }

        synchronized boolean isForced() {
            return forced != null;
        }

        synchronized String forcedId() {
            return forced;
        }

        /**
         * Removes the messages of the author at the head of the queue, up to the first command (a command waits for the end
         * of the turn, and what is behind it keeps its place after it).
         */
        synchronized List<Queued> takeMessages() {
            int end = 0;
            while (end < queued.size() && queued.get(end).command() == null) {
                end++;
            }
            List<Queued> head = queued.subList(0, end);
            List<Queued> taken = new ArrayList<>(head);
            head.clear();
            if (forced != null && !contains(forced)) {
                forced = null;
            }
            return taken;
        }

        /**
         * Removes the queued messages up to the one with the id, all of them for null (and the wish to force one is met).
         */
        synchronized List<Queued> takeThrough(String id) {
            int end = queued.size();
            for (int i = 0; id != null && i < queued.size(); i++) {
                if (queued.get(i).id().equals(id)) {
                    end = i + 1;
                    break;
                }
            }
            List<Queued> head = queued.subList(0, end);
            List<Queued> taken = new ArrayList<>(head);
            head.clear();
            if (forced != null && !contains(forced)) {
                forced = null;
            }
            return taken;
        }
    }

    /**
     * Puts the images in the message as references to the files of the workspace, and says so in its text so that the
     * transcript, and a model that does not see the pictures, know what was sent.
     */
    private void attach(AgentRun run, LLMChatMessage message, List<String> images) throws Exception {
        if (images.size() > MAX_ATTACHED_IMAGES) {
            throw new IllegalArgumentException("A message takes at most " + MAX_ATTACHED_IMAGES + " images");
        }
        AgentModelProfile main = agentService.findForUser(run.getAgent().getUuid()).getMainProfile();
        if (main == null || !main.isImages()) {
            throw new IllegalArgumentException("The model of this agent does not see images");
        }
        VirtualFileSystem files = fileSystemService.open(run);
        List<VirtualPath> paths = new ArrayList<>();
        try {
            for (String image : images) {
                VirtualPath path = VirtualPath.parse(image);
                // checks that it is an image the model can be shown, the bytes are read again when a request is made
                files.readImage(path);
                paths.add(path);
            }
        } catch (VirtualFileException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        String names = paths.stream().map(VirtualPath::toString).collect(java.util.stream.Collectors.joining(", "));
        message.setContent((message.getContent().isEmpty() ? "" : message.getContent() + "\n\n")
                + (paths.size() == 1 ? "Attached image: " : "Attached images, in this order: ") + names);
        paths.forEach(path -> message.addAttachment(ImageFiles.reference(path)));
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
        // what is said about the sub-agents, null for an agent that has none
        volatile java.util.function.Supplier<String> subAgents;

        String render() {
            StringBuilder text = new StringBuilder();
            text.append("Rounds: ").append(rounds).append(maxRounds > 0 ? " of " + maxRounds : " (no limit)").append('\n');
            text.append("Tokens spent by the run: ").append(tokens).append(tokenBudget > 0 ? " of " + tokenBudget : " (no limit)").append('\n');
            text.append("Context of the model: about ").append(contextTokens).append(contextCap > 0 ? " of " + contextCap : "").append(" tokens\n");
            text.append("Longest answer you can write: ").append(responseCap > 0 ? responseCap + " tokens (a call that needs more is cut off and fails, "
                    + "write long texts in parts)" : "no limit is known").append('\n');
            text.append("Working for ").append((System.currentTimeMillis() - startedAt) / 1000).append(" s in this session of the run");
            java.util.function.Supplier<String> sub = subAgents;
            if (sub != null) {
                text.append('\n').append(sub.get());
            }
            return text.toString();
        }
    }

    /**
     * Who asked for a compaction.
     */
    private enum Compaction {AUTHOR, AGENT, AUTOMATIC}

    // rounds a run waits before it tries an automatic compaction again after one failed
    private static final int AUTO_COMPACT_RETRY_ROUNDS = 3;

    /**
     * What one request to the model answered.
     */
    private record Round(String text, String reasoning, List<LLMToolCall> calls, RoundUsage usage, long waitMillis) {

        Round(String text, String reasoning, List<LLMToolCall> calls, RoundUsage usage) {
            this(text, reasoning, calls, usage, 0);
        }

        Round withCalls(List<LLMToolCall> kept) {
            return new Round(text, reasoning, kept, usage, waitMillis);
        }

        Round waited(long millis) {
            return new Round(text, reasoning, calls, usage, millis);
        }
    }

    /**
     * The calls without the repeats of a call that changes nothing: the same name with the same arguments, made again in the
     * same answer.
     */
    private static List<LLMToolCall> withoutRepeats(List<LLMToolCall> calls) {
        Set<String> seen = new HashSet<>();
        List<LLMToolCall> kept = new ArrayList<>();
        for (LLMToolCall call : calls) {
            if (!NO_CHANGE_TOOLS.contains(call.getName()) || seen.add(call.getName() + '\0' + call.getArguments())) {
                kept.add(call);
            }
        }
        return kept;
    }

    /**
     * What of an answer is made: the repeats of calls that read are dropped, and no more than
     * {@link Defaults#AGENT_MAX_CALLS_PER_ROUND} calls are kept, one when the agent is not batched, and the answer ends at a call of
     * a command that is made alone ({@code BatchPolicy.SINGLE}) unless the agent ignores the policies. The dropped calls are not in the transcript either, so no
     * result is owed for them.
     */
    private static Round limitCalls(Round round, AgentBatchMode mode, Set<String> singles) {
        List<LLMToolCall> kept = withoutRepeats(round.calls());
        if (mode == AgentBatchMode.NOT_BATCHED && kept.size() > 1) {
            kept = new ArrayList<>(kept.subList(0, 1));
        } else if (mode == AgentBatchMode.BATCHED) {
            int single = singleCall(kept, singles);
            if (single >= 0 && kept.size() > 1) {
                // a call that is made alone: it is the answer when it is the first, else the answer ends before it
                kept = new ArrayList<>(kept.subList(0, Math.max(single, 1)));
            }
        }
        if (kept.size() > Defaults.AGENT_MAX_CALLS_PER_ROUND) {
            kept = new ArrayList<>(kept.subList(0, Defaults.AGENT_MAX_CALLS_PER_ROUND));
        }
        return kept.size() == round.calls().size() ? round : round.withCalls(kept);
    }

    /**
     * The index of the first call of a command that is made alone in an answer, -1 when there is none.
     */
    private static int singleCall(List<LLMToolCall> calls, Set<String> singles) {
        for (int i = 0; i < calls.size(); i++) {
            if (singles.contains(calls.get(i).getName())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * The end of a worker that is not an exception: the run was thrown away under it.
     */
    private static class Discarded extends RuntimeException {
        Discarded() {
            super(null, null, false, false);
        }
    }

    /**
     * The end of a round that is not an exception: the author paused the run at once, or paused it while it waited for a place at
     * the model; what was going on is thrown away and the worker pauses at the next stable point.
     */
    private static final class Interrupted extends Discarded {
    }

    /**
     * The end of a round that is not an exception: the author forced a message in, what the worker waited for is thrown away.
     */
    private static final class Forced extends RuntimeException {
        Forced() {
            super(null, null, false, false);
        }
    }

    /**
     * The end of a sub-agent that is not an exception of the run: it was canceled, the run was paused or thrown away, or it ran into a limit.
     */
    private static final class SubStopped extends RuntimeException {
        private final State state;

        SubStopped(State state, String reason) {
            super(reason, null, false, false);
            this.state = state;
        }
    }

    /**
     * How a thread of the run waits for a future: the agent reacts to the author (a pause at once, a forced message), a
     * sub-agent to its cancel and to the run being paused or thrown away.
     */
    private interface Waiter {
        <T> T await(CompletableFuture<T> future, Runnable cancel, boolean queued) throws Exception;
    }

    private final class Worker implements Runnable {

        private final Long runId;
        private AgentRun run;
        private AgentRequestAttributes attributes;
        private Agent agent;
        // the prompt of the agent or the default it follows, resolved when the work starts (like the instructions)
        private String systemPrompt;
        // the instructions of the agent or the default of its owner, resolved with the prompt
        private String instructions;
        private final Progress progress = new Progress();
        // made again when the run is loaded, it depends on whether the run is a conversation
        private RunToolsImpl runTools = new RunToolsImpl(progress::render);
        // the checkpoint tools of the run, and the commands that change nothing (a round of those needs no checkpoint)
        private CheckpointToolsImpl checkpointTools;
        private Set<String> readOnlyCommands = Set.of();
        // the commands and the tools that are called alone in an answer (BatchPolicy.SINGLE)
        private Set<String> singleCalls = Set.of();
        // whether the working copy or the workspace may have changed since the last checkpoint
        private volatile boolean dirty;
        // whether the agent was told that no automatic checkpoint fits, until one is made again
        private boolean fullNoticeGiven;
        // the files of the run, opened with the tools; the images the model asked to see in this round
        private VirtualFileSystem files;
        private final ShownImages shown = new ShownImages();
        // whether the model of the run looks at images
        private boolean seesImages;
        // the messages of the author, only for a conversation
        private Inbox inbox;
        // the model of the run and its tools, set when the work starts (the compaction asks the model too)
        // (volatile: refreshed between the rounds, the sub-agents read them from their threads)
        private volatile ResolvedProfile profile;
        private volatile InferenceService inference;
        private FunctionSet tools;
        // set while a command of the author runs: a message the author forces in then waits for it
        private boolean ignoreForce;
        // the round before which a failed automatic compaction is not tried again
        private long autoCompactRetryAt;
        // the requests to the model the run has in the air: the agent and its sub-agents take turns when the cap is low
        private RequestSlots requestSlots = new RequestSlots(1);
        // how many sub-agents may work at once (zero: none, the agent gets no tools for them), the ones started, the tools of a sub-agent
        private int maxSubagents;
        private SubAgents subAgents;
        // what a sub-agent needs to be built, and the models built, by role in lower case ("" is the main one)
        private VirtualFileSystem subFiles;
        private CommandCage subCage;
        private final Map<String, SubModel> subModels = new ConcurrentHashMap<>();
        // the calls that change the book or the workspace run one at a time, whoever makes them (the agent or a sub-agent)
        private final ReentrantLock changeLock = new ReentrantLock(true);
        // the id of the call of the agent that is made now (the calls of the agent are made one after the other)
        private volatile String currentCallId;
        // the id of the call that is made again because the run was paused while it waited for a sub-agent
        private volatile String resumedCallId;
        // how often the agent ended its turn with sub-agents working, since it last made a call
        private int stillWorkingNotices;
        // the queued messages and commands of the author to deliver before the work goes on, null for none; a worker
        // for a run that waits only does these and leaves the run as it is
        private final List<Queued> initial;

        Worker(AgentRun run, List<Queued> initial) {
            this.runId = run.getId();
            this.run = run;
            this.initial = initial;
        }

        @Override
        public void run() {
            try {
                attributes = runService.openSession(run);
                try (InRequestScope _ = new InRequestScope(attributes)) {
                    work();
                } catch (Interrupted e) {
                    log.info("Agent run {}: the author paused it at once", run.getUuid());
                    run = runService.find(runId);
                    settleRequested();
                } catch (Discarded e) {
                    log.info("Agent run {} was thrown away while it worked", run.getUuid());
                } catch (Throwable e) {
                    log.error("Agent run {} failed", run.getUuid(), e);
                    failed(e);
                }
            } catch (Throwable e) {
                log.error("Agent run {} could not start working", run.getUuid(), e);
            } finally {
                stopSubAgents();
                dropInbox();
                interrupts.remove(runId);
                working.remove(runId);
                runService.workerFinished(run);
                if (attributes != null) {
                    attributes.close();
                }
            }
        }

        /**
         * A run that ended for good has no one to read what was written to it.
         */
        private void dropInbox() {
            try {
                AgentRun last = runService.find(runId);
                if (last == null || !LIVE.contains(last.getStatus())) {
                    inboxes.remove(runId);
                }
            } catch (Exception e) {
                log.debug("Agent run {}: the queued messages were not looked at: {}", runId, e.getMessage());
            }
        }

        // ------------------------------------------------------------------ the loop

        /**
         * Reads the run and the definition of its agent: the prompt, the instructions and the model.
         */
        private void loadAgent() throws Exception {
            run = runService.find(runId);
            runTools = new RunToolsImpl(progress::render, run.isInteractive());
            agent = agentService.findForUser(run.getAgent().getUuid());
            systemPrompt = agentService.getSystemPrompt(agent);
            instructions = agentService.getInstructions(agent);
            profile = agentService.resolveProfile(agent.getMainProfile());
        }

        /**
         * What the model of the run is asked with: its service, whether it sees images, the requests it may have in the air and
         * the sub-agents it may have. A command of the author that does not ask the model needs no profile.
         */
        private void loadModel() throws Exception {
            inference = profile == null ? null : inferenceServices.forAIOrThrow(profile.ai());
            seesImages = profile != null && profile.profile().isImages();
            requestSlots = new RequestSlots(runService.getMaxConcurrency(run));
            maxSubagents = runService.getMaxSubagents(run);
        }

        private void work() throws Exception {
            loadAgent();
            inbox = run.isInteractive() ? inboxes.computeIfAbsent(runId, id -> new Inbox()) : null;
            if (profile == null && initial == null) {
                pause("The model of the agent is not available");
                return;
            }
            // the copy writes with the model of the agent or with the one of the book, as the definition of the agent says
            runService.useAgentModel(run);
            loadModel();

            List<LLMChatMessage> transcript = new ArrayList<>(messageService.transcript(run));
            runTools.restorePlan(planOf(transcript));
            checkpointTools = new CheckpointToolsImpl(() -> run, checkpointService);
            readOnlyCommands = commandService.getCommands().stream().filter(CommandDefinition::readOnly)
                    .map(CommandDefinition::commandName).collect(Collectors.toSet());
            tools = tools();
            if (initial == null) {
                // the sub-agents that were paused go on, and the call of the agent that waited for one waits again
                restoreSubAgents(true);
                boolean parked = resumeParked(transcript);
                run = runService.find(runId);
                if (!parked || run.getStatus() == AgentRunStatus.RUNNING) {
                    prepare(transcript);
                }
            } else {
                // the run waits: no "go on", the calls of an interrupted round only get their answer
                repairCalls(transcript);
            }

            progress.maxRounds = agent.getMaxRounds() == null ? 0 : agent.getMaxRounds();
            progress.contextCap = runService.getContextCap(run);
            progress.responseCap = profile == null ? 0 : TokenLimits.responseTokens(profile.ai(), profile.protocol());
            progress.rounds = run.getRounds();
            progress.tokens = run.getTotalTokens();
            progress.tokenBudget = runService.getTokenBudget(run);
            log.info("Agent run {} of agent \"{}\" works: AI \"{}\", protocol \"{}\", {} message(s) so far, round {}, limits: {} rounds, {} tokens, {} context",
                    run.getUuid(), agent.getName(), profile == null ? "none" : profile.ai().getName(),
                    profile == null ? "none" : profile.protocol().getName(),
                    transcript.size(), run.getRounds(), progress.maxRounds > 0 ? progress.maxRounds : "no",
                    progress.tokenBudget > 0 ? progress.tokenBudget : "no", progress.contextCap > 0 ? progress.contextCap : "no");

            if (initial != null) {
                deliver(initial, transcript);
                run = runService.find(runId);
                if (run.getStatus() != AgentRunStatus.RUNNING) {
                    // the run waits still, or a pause or a stop was asked meanwhile
                    settleRequested();
                    return;
                }
                // somebody wrote to the run or resumed it meanwhile: the work goes on from here
                prepare(transcript);
            }

            startCheckpoint(transcript);

            int unreadable = 0;
            while (true) {
                // the stable point: a pause or a stop asked meanwhile ends here
                run = runService.find(runId);
                if (run.getStatus() != AgentRunStatus.RUNNING) {
                    settleRequested();
                    return;
                }
                if (inbox != null && inbox.isForced()) {
                    // asked between two rounds: nothing is in the air
                    deliverForced(transcript, List.of());
                    continue;
                }
                // between two rounds: what the author wrote meanwhile is read by the next request, not at the end of the turn
                deliverQueuedMessages(transcript);
                String limit = limit(run, transcript, inference, progress);
                if (limit != null) {
                    pause(limit);
                    return;
                }

                refreshModel();
                Round round;
                try {
                    round = ask(inference, profile, payload(transcript), tools);
                } catch (Interrupted e) {
                    // the answer, if it came, is not written; the stable point pauses
                    log.info("Agent run {}: the request was canceled, the author paused the run at once", run.getUuid());
                    interrupts.remove(runId);
                    continue;
                } catch (Discarded e) {
                    throw e;
                } catch (Forced e) {
                    log.info("Agent run {}: the request was canceled, the author sent a message", run.getUuid());
                    deliverForced(transcript, List.of());
                    continue;
                } catch (Exception e) {
                    log.warn("The model of agent run {} could not be asked: {}", run.getUuid(), e.getMessage(), e);
                    if (isUnreadableCall(e)) {
                        // the agent is told, in the transcript: a resume would ask the same question and get the same answer
                        append(transcript, LLMChatMessage.of(LLMRole.USER, Defaults.AGENT_UNREADABLE_CALL));
                        if (++unreadable <= MAX_UNREADABLE_CALLS) {
                            log.info("Agent run {}: the agent wrote an unreadable call, it is told and asked again ({} of {})", run.getUuid(),
                                    unreadable, MAX_UNREADABLE_CALLS);
                            continue;
                        }
                    }
                    pause("The model could not be asked: " + messageOf(e));
                    return;
                }
                unreadable = 0;
                changeLock.lock();
                try {
                    run = runService.addUsage(run, round.usage().promptTokens(), round.usage().completionTokens());
                } finally {
                    changeLock.unlock();
                }
                progress.rounds = run.getRounds();
                progress.tokens = run.getTotalTokens();
                progress.contextTokens = round.usage().promptTokens() + round.usage().completionTokens();
                log.info("Agent run {} round {}: {} + {} tokens, context {}{}{}, {}", run.getUuid(), run.getRounds(),
                        round.usage().promptTokens(), round.usage().completionTokens(), progress.contextTokens,
                        progress.contextCap > 0 ? " of " + progress.contextCap : "",
                        round.reasoning() == null ? "" : ", reasoning " + round.reasoning().length() + " characters",
                        round.calls().isEmpty() ? "answers in text" : "calls " + round.calls().stream().map(LLMToolCall::getName)
                                .collect(java.util.stream.Collectors.joining(", ")));

                int asked = round.calls().size();
                int repeats = asked - withoutRepeats(round.calls()).size();
                AgentBatchMode mode = agent.batchModeOrDefault();
                int single = mode == AgentBatchMode.BATCHED ? singleCall(withoutRepeats(round.calls()), singleCalls) : -1;
                String singleName = single < 0 ? null : withoutRepeats(round.calls()).get(single).getName();
                round = limitCalls(round, mode, singleCalls);
                String callNotice = round.calls().size() == asked ? null
                        : mode == AgentBatchMode.NOT_BATCHED && asked - repeats > 1 ? String.format(Defaults.AGENT_ONE_CALL_NOTICE, asked - repeats, round.calls().getFirst().getName(), asked - repeats - 1)
                        : singleName != null && round.calls().size() < asked - repeats
                        ? String.format(Defaults.AGENT_SINGLE_CALL_NOTICE, singleName, asked - repeats, round.calls().size())
                        : String.format(Defaults.AGENT_TOO_MANY_CALLS, asked, repeats, round.calls().size());

                LLMChatMessage answer = round.calls().isEmpty()
                        ? LLMChatMessage.of(LLMRole.ASSISTANT, round.text())
                        : LLMChatMessage.ofToolCalls(StringUtils.isEmpty(round.text()) ? null : round.text(), round.calls());
                changeLock.lock();
                try {
                    messageService.appendAnswer(run, answer, progress.contextTokens, round.reasoning(),
                            AgentTelemetry.of(round.usage(), asked, round.calls().size(), round.waitMillis(), mainRole()).toJson());
                } finally {
                    changeLock.unlock();
                }
                transcript.add(answer);
                if (round.calls().isEmpty()) {
                    if (subAgents != null && subAgents.live() > 0) {
                        if (stillWorkingNotices++ < 1) {
                            append(transcript, LLMChatMessage.of(LLMRole.USER, String.format(Defaults.AGENT_SUBAGENTS_STILL_WORKING,
                                    subAgents.live(), subAgents.all().stream().filter(sub -> !sub.state().isFinished()).map(Sub::handle)
                                            .collect(Collectors.joining(", ")))));
                            continue;
                        }
                        subAgents.cancelAll();
                    }
                    if (endTurn(transcript, AgentRunStatus.IDLE, null)) {
                        continue;
                    }
                    return;
                }

                stillWorkingNotices = 0;
                runTools.clearRequests();
                // the state the changes of this round start from is saved first; the notice for the agent, if any, comes after the results
                boolean changes = round.calls().stream().anyMatch(this::changes);
                String notice = changes ? checkpointBeforeChanges() : null;
                // the answers to the calls the author's message cut off, stored with the message (the others are stored as they are made)
                List<LLMChatMessage> results = new ArrayList<>();
                boolean forced = false;
                boolean paused = false;
                boolean suspended = false;
                boolean failed = false;
                for (int index = 0; index < round.calls().size() && !suspended; index++) {
                    LLMToolCall call = round.calls().get(index);
                    if (paused || interrupts.contains(runId) || stopping()) {
                        paused = true;
                        results.add(LLMChatMessage.ofToolResult(call.getId(), stopping() ? Defaults.AGENT_STOPPED_SKIPPED_CALL
                                : Defaults.AGENT_PAUSED_SKIPPED_CALL));
                        continue;
                    }
                    if (forced || inbox != null && inbox.isForced()) {
                        forced = true;
                        results.add(LLMChatMessage.ofToolResult(call.getId(), Defaults.AGENT_SKIPPED_CALL));
                        continue;
                    }
                    if (failed) {
                        // an error ends the answer: what follows may need what the failed call did not do, the model reads the error first
                        results.add(LLMChatMessage.ofToolResult(call.getId(), Defaults.AGENT_AFTER_ERROR_SKIPPED_CALL));
                        continue;
                    }
                    long began = System.currentTimeMillis();
                    String result;
                    try {
                        result = call(tools, call);
                    } catch (Interrupted e) {
                        if (WAITING_TOOLS.contains(call.getName()) && subAgents != null) {
                            // it waited for a sub-agent: no answer, the call is made again when the run goes on
                            suspended = true;
                            continue;
                        }
                        paused = true;
                        results.add(LLMChatMessage.ofToolResult(call.getId(), stopping() ? Defaults.AGENT_STOPPED_CALL : Defaults.AGENT_PAUSED_CALL));
                        continue;
                    } catch (Forced e) {
                        forced = true;
                        results.add(LLMChatMessage.ofToolResult(call.getId(), Defaults.AGENT_FORCED_CALL));
                        continue;
                    }
                    if (SubAgentToolsImpl.SUSPENDED.equals(result)) {
                        // the run is paused while the call waits for a sub-agent; it is answered when the run goes on
                        suspended = true;
                        continue;
                    }
                    // the arguments and the result are the story's business and not logged
                    log.info("Agent run {}: {} {} in {} ms ({} characters)", run.getUuid(), call.getName(),
                            result.startsWith("Error:") ? "failed" : "done", System.currentTimeMillis() - began, result.length());
                    // stored at once: the console shows the result of a call when it is made, not when the round is over, and a
                    // pause in the middle of a round keeps what the calls before it did
                    append(transcript, LLMChatMessage.ofToolResult(call.getId(), result));
                    failed = result.startsWith("Error:");
                }
                if (suspended) {
                    log.info("Agent run {}: the run was paused while the agent waited for a sub-agent, the rest of the round waits", run.getUuid());
                    interrupts.remove(runId);
                    // the calls that were not answered are answered when the run goes on: the stable point pauses
                    run = runService.find(runId);
                    if (run.getStatus() == AgentRunStatus.RUNNING) {
                        // nobody asked for it, the sub-agents were stopped by something else: the calls get their answer now
                        append(transcript, openCalls(round.calls(), transcript).stream()
                                .map(open -> LLMChatMessage.ofToolResult(open.getId(), Defaults.AGENT_PAUSED_CALL)).toArray(LLMChatMessage[]::new));
                    }
                    continue;
                }
                if (paused) {
                    log.info("Agent run {}: the rest of the round was thrown away, the author paused the run at once", run.getUuid());
                    // what was done is in the transcript, the cut off calls are answered; the stable point pauses
                    interrupts.remove(runId);
                    append(transcript, results.toArray(new LLMChatMessage[0]));
                    continue;
                }
                if (forced) {
                    log.info("Agent run {}: the rest of the round was thrown away, the author sent a message", run.getUuid());
                    deliverForced(transcript, results);
                    continue;
                }
                if (!results.isEmpty()) {
                    // the calls after a failed one
                    append(transcript, results.toArray(new LLMChatMessage[0]));
                }
                List<VirtualPath> looked = shown.take();
                if (!looked.isEmpty()) {
                    append(transcript, imagesMessage(looked));
                }
                if (notice != null) {
                    append(transcript, LLMChatMessage.of(LLMRole.USER, notice));
                }
                if (callNotice != null) {
                    append(transcript, LLMChatMessage.of(LLMRole.USER, callNotice));
                }
                dirty |= changes;

                if ((runTools.getSummary() != null || runTools.getQuestion() != null) && subAgents != null) {
                    subAgents.cancelAll();
                }
                if (runTools.getSummary() != null) {
                    // a conversation goes on after the report, only the user ends it
                    if (endTurn(transcript, run.isInteractive() ? AgentRunStatus.IDLE : AgentRunStatus.DONE,
                            StringUtils.abbreviate(runTools.getSummary(), MAX_REASON))) {
                        continue;
                    }
                    return;
                }
                if (runTools.getQuestion() != null) {
                    if (endTurn(transcript, AgentRunStatus.IDLE, StringUtils.abbreviate(runTools.getQuestion(), MAX_REASON))) {
                        continue;
                    }
                    return;
                }
                String restore = checkpointTools.takeRestore();
                if (restore != null) {
                    restoreForAgent(restore, transcript);
                }
                if (runTools.isCompactionRequested()) {
                    compactForAgent(runTools.getCompactionFocus(), transcript);
                } else if (!compactAutomatically(transcript)) {
                    warnAboutContext(transcript);
                }
            }
        }

        /**
         * The checkpoint of the start of the run, if it has none; a run with others (resumed) may have changed since the last.
         */
        private void startCheckpoint(List<LLMChatMessage> transcript) throws Exception {
            try {
                boolean first = checkpointService.list(run).isEmpty();
                checkpointService.ensureStart(run);
                dirty = !first;
            } catch (Discarded e) {
                throw e;
            } catch (Exception e) {
                log.warn("Agent run {}: no checkpoint of the start: {}", run.getUuid(), e.getMessage());
                dirty = true;
                append(transcript, LLMChatMessage.of(LLMRole.USER, Defaults.AGENT_NO_START_CHECKPOINT + " " + e.getMessage()));
            }
        }

        /**
         * Whether a call of the model may change the working copy or the workspace: everything but the tools that only look
         * (the run tools, the checkpoint tools, the reading file tools) and the commands that are declared read only.
         */
        private boolean changes(LLMToolCall call) {
            String name = call.getName();
            return !NO_CHANGE_TOOLS.contains(name) && !SUBAGENT_TOOLS.contains(name) && !readOnlyCommands.contains(name);
        }

        /**
         * Before the calls of a round that changes something: an automatic checkpoint, when something changed since the last
         * one. The run goes on without it when there is no room, and the agent is told once.
         *
         * @return what the agent should be told after the results of the round, null for nothing
         */
        private String checkpointBeforeChanges() {
            if (!dirty) {
                return null;
            }
            // a sub-agent may be writing: the state is saved between its calls
            changeLock.lock();
            try {
                // the answer that asks for the changes is already in the transcript: a rewind to this checkpoint forgets it
                checkpointService.makeAutomatic(run, "before round " + (run.getRounds()), 1);
                dirty = false;
                fullNoticeGiven = false;
                return null;
            } catch (Exception e) {
                log.info("Agent run {}: no automatic checkpoint: {}", run.getUuid(), e.getMessage());
                if (fullNoticeGiven) {
                    return null;
                }
                fullNoticeGiven = true;
                return Defaults.AGENT_NO_AUTOMATIC_CHECKPOINT + " " + e.getMessage();
            } finally {
                changeLock.unlock();
            }
        }

        /**
         * The agent asked to go back to a checkpoint with {@code checkpoint_restore}: done between two rounds, with every call
         * of the round answered. The working copy is another book afterwards, so the tools are made again. A failure changes
         * nothing and the agent is told.
         */
        private void restoreForAgent(String id, List<LLMChatMessage> transcript) throws Exception {
            boolean before = ignoreForce;
            ignoreForce = true;
            try {
                // they work in the book that is replaced
                boolean stopped = stopSubAgents();
                CheckpointService.Restored restored = checkpointService.restore(run, id);
                run = restored.run();
                tools = tools();
                // what they said is kept, they are all over: the agent can still ask what came of them
                restoreSubAgents(false);
                dirty = false;
                StringBuilder text = new StringBuilder(String.format(Defaults.AGENT_RESTORED, restored.checkpoint().id(),
                        restored.checkpoint().round()));
                if (stopped) {
                    text.append(" Your sub-agents that were working were stopped.");
                }
                if (!restored.removedFiles().isEmpty()) {
                    text.append(" The files you wrote after it are gone: ")
                            .append(restored.removedFiles().stream().limit(20).collect(Collectors.joining(", ")))
                            .append(restored.removedFiles().size() > 20 ? " and " + (restored.removedFiles().size() - 20) + " more" : "")
                            .append('.');
                }
                text.append(restored.safety() == null ? " There was no room to save the state before the restore."
                        : " The state before the restore is checkpoint " + restored.safety().id() + ".");
                log.info("Agent run {}: the agent restored checkpoint {}", run.getUuid(), restored.checkpoint().id());
                append(transcript, LLMChatMessage.of(LLMRole.USER, text.toString()));
            } catch (Discarded e) {
                throw e;
            } catch (Exception e) {
                log.warn("Agent run {}: restore of checkpoint {} failed: {}", run.getUuid(), id, e.getMessage());
                append(transcript, LLMChatMessage.of(LLMRole.USER, "You asked to go back to checkpoint " + id + ", but it failed: "
                        + CommandFunctions.messageOf(e) + "\nNothing was changed."));
            } finally {
                ignoreForce = before;
            }
        }

        /**
         * The agent asked for a compaction with {@code request_compaction}: done between two rounds, with every call of the
         * round answered. A failure changes nothing and the agent is told, it goes on with the context it has.
         */
        private void compactForAgent(String focus, List<LLMChatMessage> transcript) throws Exception {
            boolean before = ignoreForce;
            ignoreForce = true;
            try {
                log.info("Agent run {}: the agent asks for a compaction", run.getUuid());
                compact(focus, transcript, Compaction.AGENT);
            } catch (Discarded e) {
                throw e;
            } catch (Exception e) {
                log.warn("Agent run {}: compaction failed: {}", run.getUuid(), e.getMessage());
                append(transcript, LLMChatMessage.of(LLMRole.USER, "You asked to compact your work, but it failed: "
                        + CommandFunctions.messageOf(e) + "\nNothing was changed."));
            } finally {
                ignoreForce = before;
            }
        }

        /**
         * After a round, once the context passes the share of its cap that the agent says ({@code Agent.autoCompactPercent}, else
         * {@link Defaults#AGENT_AUTO_COMPACT_PERCENT}), the runner compacts the run itself: the agent had the warnings to do it
         * and did not, and the run would stop when the context is full. The number is the one of the request just made, like
         * for the warnings. A failure changes nothing, the agent is told, and the next try is some rounds later.
         *
         * @return whether the run was compacted or the attempt failed (the agent was told either way, no warning is added)
         */
        private boolean compactAutomatically(List<LLMChatMessage> transcript) throws Exception {
            long cap = progress.contextCap;
            int percent = agent.getAutoCompactPercent() == null ? Defaults.AGENT_AUTO_COMPACT_PERCENT : agent.getAutoCompactPercent();
            if (percent <= 0 || cap <= 0 || progress.contextTokens * 100 < cap * percent || run.getRounds() < autoCompactRetryAt) {
                return false;
            }
            boolean before = ignoreForce;
            ignoreForce = true;
            try {
                log.info("Agent run {}: the context is {} of {} tokens, the runner compacts the run", run.getUuid(), progress.contextTokens, cap);
                compact("", transcript, Compaction.AUTOMATIC);
                return true;
            } catch (Discarded e) {
                throw e;
            } catch (Exception e) {
                log.warn("Agent run {}: automatic compaction failed: {}", run.getUuid(), e.getMessage());
                autoCompactRetryAt = run.getRounds() + AUTO_COMPACT_RETRY_ROUNDS;
                append(transcript, LLMChatMessage.of(LLMRole.USER, "The run tried to compact your work because your context is nearly full, "
                        + "but it failed: " + CommandFunctions.messageOf(e) + "\nNothing was changed. Save your notes to a file and call "
                        + "request_compaction yourself."));
                return true;
            } finally {
                ignoreForce = before;
            }
        }

        /**
         * After a round, once the context passes {@link Defaults#AGENT_CONTEXT_WARNING_PERCENT} of its cap, the agent is told
         * so, and where to go from there: its notes to a file, then {@code request_compaction}. The number is the one of the
         * request just made, so the results of the round are not in it yet.
         */
        private void warnAboutContext(List<LLMChatMessage> transcript) throws Exception {
            long cap = progress.contextCap;
            long used = progress.contextTokens;
            if (cap <= 0 || used * 100 < cap * Defaults.AGENT_CONTEXT_WARNING_PERCENT) {
                return;
            }
            int percent = (int) Math.min(100, used * 100 / cap);
            append(transcript, LLMChatMessage.of(LLMRole.USER, String.format(
                    percent >= Defaults.AGENT_CONTEXT_CRITICAL_PERCENT ? Defaults.AGENT_CONTEXT_CRITICAL : Defaults.AGENT_CONTEXT_WARNING,
                    percent, used, cap)));
        }

        /**
         * The turn is over: the run waits for the author ({@code IDLE}) or is done. A conversation with queued messages
         * does not wait, they are what the author would have written: they are read by the model next. The decision is
         * made under the lock of the inbox, see {@link #enqueue}.
         *
         * @return whether the messages were delivered and the loop goes on
         */
        private boolean endTurn(List<LLMChatMessage> transcript, AgentRunStatus target, String reason) throws Exception {
            if (inbox == null) {
                reach(target, reason);
                return false;
            }
            while (true) {
                List<Queued> due;
                synchronized (inbox) {
                    due = inbox.takeThrough(null);
                    if (due.isEmpty()) {
                        reach(target, reason);
                        return false;
                    }
                }
                // outside of the lock: a command can take long, and the author must be able to write meanwhile
                if (deliver(due, transcript)) {
                    return true;
                }
                // only commands, they do not make the agent answer: look again before the turn ends
            }
        }

        /**
         * Delivers the queued messages and commands, all of them, in the order they came.
         *
         * @return true when a message of the author was among them, so that the agent reads it and goes on; false when
         * nothing was queued or only commands
         */
        private boolean deliverQueued(List<LLMChatMessage> transcript) throws Exception {
            if (inbox == null) {
                return false;
            }
            List<Queued> due;
            synchronized (inbox) {
                due = inbox.takeThrough(null);
            }
            return !due.isEmpty() && deliver(due, transcript);
        }

        /**
         * Delivers the messages of the author at the head of the queue (not the commands, they wait for the end of the turn),
         * so that the next request of the model reads them.
         */
        private void deliverQueuedMessages(List<LLMChatMessage> transcript) throws Exception {
            if (inbox == null) {
                return;
            }
            List<Queued> due;
            synchronized (inbox) {
                due = inbox.takeMessages();
            }
            if (!due.isEmpty()) {
                deliver(due, transcript);
            }
        }

        /**
         * Puts what the author queued into the transcript, in order: the messages as one message of the author, a command
         * is run (as the author, the agent did not call it) and what it said is a message of the author too.
         *
         * @return whether a message of the author (not a command) was among them
         */
        private boolean deliver(List<Queued> due, List<LLMChatMessage> transcript) throws Exception {
            log.info("Agent run {}: {} queued message(s) and command(s) of the user go to the agent", run.getUuid(), due.size());
            boolean message = false;
            List<Queued> messages = new ArrayList<>();
            for (Queued item : due) {
                if (item.command() == null) {
                    messages.add(item);
                    message = true;
                    continue;
                }
                if (!messages.isEmpty()) {
                    append(transcript, merged(messages));
                    messages.clear();
                }
                runCommand(item.command(), transcript);
            }
            if (!messages.isEmpty()) {
                append(transcript, merged(messages));
            }
            return message;
        }

        /**
         * Runs a command of the author. {@code /compact} summarizes the transcript, the others are run in the working copy
         * as any command; the answer, or the error, is written to the transcript as a message of the author that says
         * they ran it.
         */
        private void runCommand(String line, List<LLMChatMessage> transcript) throws Exception {
            String name = commandName(line);
            boolean before = ignoreForce;
            ignoreForce = true;
            try {
                log.info("Agent run {}: the user runs /{}", run.getUuid(), name);
                if (COMPACT.equals(name)) {
                    try {
                        compact(commandArguments(line), transcript, Compaction.AUTHOR);
                    } catch (Discarded e) {
                        throw e;
                    } catch (Exception e) {
                        log.warn("Agent run {}: compaction failed: {}", run.getUuid(), e.getMessage());
                        append(transcript, LLMChatMessage.of(LLMRole.USER, "The author asked to compact your work, but it failed: "
                                + CommandFunctions.messageOf(e) + "\nNothing was changed."));
                    }
                    return;
                }
                if (CLEAR_WORKSPACE.equals(name)) {
                    append(transcript, LLMChatMessage.of(LLMRole.USER, Defaults.AGENT_AUTHOR_COMMAND_NOTE + "\n\nCommand: " + line
                            + "\n\nResult:\n" + clearWorkspace(commandArguments(line))));
                    return;
                }
                String result;
                try {
                    CommandRun commandRun = commandService.executeLine(line);
                    result = CommandFunctions.bound(FunctionDefinition.render(await(commandRun, () -> commandRun.cancel(true))));
                } catch (Discarded e) {
                    throw e;
                } catch (Exception e) {
                    result = "Error: " + CommandFunctions.messageOf(e);
                }
                log.info("Agent run {}: /{} {} ({} characters)", run.getUuid(), name,
                        result.startsWith("Error:") ? "failed" : "done", result.length());
                append(transcript, LLMChatMessage.of(LLMRole.USER, Defaults.AGENT_AUTHOR_COMMAND_NOTE + "\n\nCommand: " + line
                        + "\n\nResult:\n" + result));
            } finally {
                ignoreForce = before;
            }
        }

        /**
         * {@code /clear-workspace}: the files and folders of the workspace are deleted but for the paths and patterns in the
         * arguments. The answer is what the model reads, a failure leaves the workspace as it was.
         */
        private String clearWorkspace(String arguments) {
            try {
                VirtualFileSystem.Cleared cleared = files.clearWorkspace(words(arguments));
                log.info("Agent run {}: workspace cleared, {} file(s) and {} folder(s) deleted, {} kept", run.getUuid(),
                        cleared.files(), cleared.folders(), cleared.kept().size());
                StringBuilder result = new StringBuilder("Deleted ").append(cleared.files()).append(" file(s) and ")
                        .append(cleared.folders()).append(" folder(s) from the workspace.");
                if (!cleared.kept().isEmpty()) {
                    result.append(" Kept: ").append(cleared.kept().stream().map(VirtualPath::toString).collect(Collectors.joining(", ")))
                            .append('.');
                }
                return result.toString();
            } catch (Exception e) {
                log.warn("Agent run {}: the workspace could not be cleared: {}", run.getUuid(), e.getMessage());
                return "Error: " + CommandFunctions.messageOf(e) + "\nNothing was deleted.";
            }
        }

        /**
         * The compaction: the model reads the whole transcript and writes the summary, which becomes the start of what it
         * reads from now on. The rounds before stay in the transcript of the user, marked as compacted.
         *
         * @param focus   what the author wants the summary to stress, may be empty
         * @param who     who asked for it: the author, the agent with {@code request_compaction}, or the runner
         */
        private void compact(String focus, List<LLMChatMessage> transcript, Compaction who) throws Exception {
            if (profile == null || inference == null) {
                throw new IllegalStateException("The model of the agent is not available");
            }
            if (transcript.isEmpty()) {
                throw new IllegalStateException("There is nothing to compact");
            }
            List<LLMChatMessage> history = new ArrayList<>(transcript);
            int dropped = fit(history);
            String prompt = agentService.getCompactionPrompt(agent).strip();
            history.add(LLMChatMessage.of(LLMRole.USER, focus.isEmpty() ? prompt
                    : prompt + (who == Compaction.AGENT ? "\n\nYou asked the summary to stress: " : "\n\nThe author wants the summary to stress: ") + focus));

            Round round = ask(inference, profile, payload(history), tools);
            changeLock.lock();
            try {
                run = runService.addUsage(run, round.usage().promptTokens(), round.usage().completionTokens());
            } finally {
                changeLock.unlock();
            }
            progress.rounds = run.getRounds();
            progress.tokens = run.getTotalTokens();
            String text = StringUtils.defaultString(round.text()).strip();
            if (text.isEmpty()) {
                throw new IllegalStateException(round.calls().isEmpty() ? "The model wrote no summary"
                        : "The model called a tool instead of writing the summary");
            }
            StringBuilder message = new StringBuilder(switch (who) {
                case AGENT -> Defaults.AGENT_SELF_COMPACTED_NOTE;
                case AUTOMATIC -> Defaults.AGENT_AUTO_COMPACTED_NOTE;
                case AUTHOR -> Defaults.AGENT_COMPACTED_NOTE;
            })
                    .append("\n\n").append(text);
            if (dropped > 0) {
                message.append("\n\n").append(String.format(Defaults.AGENT_COMPACTED_TRIMMED, dropped));
            }
            if (!runTools.getPlan().isEmpty()) {
                message.append("\n\nYour plan when this summary was written:\n").append(RunToolsImpl.render(runTools.getPlan()));
            }
            LLMChatMessage summary = LLMChatMessage.of(LLMRole.USER, message.toString());
            long summaryTokens = inference.countTokensApprox(summary.getContent());
            // what the next request will be: the system prompt and the tools as well, the model reports the real number then
            long tokens = estimate(payload(List.of(summary))) + toolTokens();
            changeLock.lock();
            try {
                messageService.compact(run, summary, tokens, AgentTelemetry.of(round.usage(), 0, 0, round.waitMillis(), mainRole()).toJson());
            } finally {
                changeLock.unlock();
            }
            log.info("Agent run {}: {} message(s) compacted to a summary of {} tokens, about {} with the prompt and the tools ({} + {} tokens spent)",
                    run.getUuid(), transcript.size(), summaryTokens, tokens, round.usage().promptTokens(), round.usage().completionTokens());
            transcript.clear();
            transcript.add(summary);
            progress.contextTokens = tokens;
        }

        /**
         * Leaves out of the messages the oldest ones, whole rounds, until the rest and the prompt fit the context of the
         * model with room for the summary it writes (80% of the cap of the context); a compaction that is asked for
         * after the context is full can still be done this way.
         *
         * @return how many messages were left out
         */
        private int fit(List<LLMChatMessage> history) throws Exception {
            long cap = progress.contextCap;
            if (cap <= 0) {
                return 0;
            }
            long room = cap * 8 / 10;
            long used = estimate(payload(history)) + toolTokens();
            int dropped = 0;
            while (used > room && history.size() > 1) {
                used -= estimate(List.of(history.removeFirst()));
                dropped++;
                // results of calls that are gone are gone too
                while (history.size() > 1 && history.getFirst().getRole() == LLMRole.TOOL) {
                    used -= estimate(List.of(history.removeFirst()));
                    dropped++;
                }
            }
            return dropped;
        }

        /**
         * What the definitions of the tools add to every request.
         */
        private long toolTokens() throws Exception {
            long tokens = 0;
            for (FunctionDefinition definition : tools.getDefinitions()) {
                tokens += inference.countTokensApprox(definition.getName() + " " + StringUtils.defaultString(definition.getDescription())
                        + " " + definition.getParametersSchema());
            }
            return tokens;
        }

        private long estimate(List<LLMChatMessage> messages) throws Exception {
            long tokens = 0;
            for (LLMChatMessage message : messages) {
                tokens += inference.countTokensApprox(StringUtils.defaultString(message.getContent()));
                for (LLMToolCall call : message.getToolCalls()) {
                    tokens += inference.countTokensApprox(StringUtils.defaultString(call.getArguments()));
                }
            }
            return tokens;
        }

        /**
         * The author wants a message now: whatever of the round was in the air is thrown away. The calls that were asked
         * of the model and not done get their answer (the transcript keeps the calls and their results together), a
         * command that waited for an approval is refused, and the messages up to the forced one are the next thing in
         * the transcript.
         *
         * @param results the answers of the calls of the round that was written down, empty when the model had not answered
         */
        private void deliverForced(List<LLMChatMessage> transcript, List<LLMChatMessage> results) throws Exception {
            // the run works again first: the refused approval would bring it back on its own thread, and two writers of
            // the database at once fail
            run = runService.find(runId);
            if (run.getStatus() == AgentRunStatus.WAITING_APPROVAL) {
                try {
                    run = runService.transition(run, AgentRunStatus.RUNNING, null);
                } catch (IllegalStateException e) {
                    // a pause or a stop came first
                    run = runService.find(runId);
                }
            }
            // the calls end in the air, the approval that waited for the author is refused
            approvalService.rejectAll(runId);
            if (!results.isEmpty()) {
                append(transcript, results.toArray(new LLMChatMessage[0]));
            }
            List<Queued> due;
            synchronized (inbox) {
                // the author may have taken the message back after the round was thrown away: then nothing is due
                String forcedId = inbox.forcedId();
                due = forcedId == null ? List.of() : inbox.takeThrough(forcedId);
            }
            if (!due.isEmpty()) {
                deliver(due, transcript);
            }
        }

        /**
         * The transcript as the loop can go on with it: the start of a new run, the end of an interrupted round.
         */
        private void prepare(List<LLMChatMessage> transcript) throws Exception {
            String note = fileSystemService.takeResumeNote(run);
            if (transcript.isEmpty()) {
                append(transcript, LLMChatMessage.of(LLMRole.USER, note == null ? Defaults.DEFAULT_AGENT_START_MESSAGE
                        : Defaults.DEFAULT_AGENT_START_MESSAGE + "\n\n" + note.strip()));
                return;
            }
            repairCalls(transcript);
            // the user changed the book while the run was paused: the model hears what came into its copy and what did not
            String sync = fileSystemService.takePendingSync(run);
            if (sync != null || note != null) {
                // the report first, then what the author wrote with the resume, then the way on: also for a run that is not a conversation
                List<String> told = new ArrayList<>();
                if (sync != null) {
                    told.add(sync.strip());
                }
                if (note != null) {
                    told.add(Defaults.AGENT_RESUME_NOTE + "\n" + note.strip());
                }
                told.add(Defaults.DEFAULT_AGENT_RESUME_MESSAGE);
                append(transcript, LLMChatMessage.of(LLMRole.USER, String.join("\n\n", told)));
                return;
            }
            LLMChatMessage last = transcript.getLast();
            if (last.getRole() == LLMRole.ASSISTANT && !last.hasToolCalls() && !deliverQueued(transcript)) {
                append(transcript, LLMChatMessage.of(LLMRole.USER, Defaults.DEFAULT_AGENT_RESUME_MESSAGE));
            }
        }

        /**
         * A round interrupted between the calls and their results: the calls get an answer, the model is not left with
         * calls nobody answered (providers refuse that).
         */
        private void repairCalls(List<LLMChatMessage> transcript) throws Exception {
            if (transcript.isEmpty()) {
                return;
            }
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
        }

        /**
         * The calls of the last message of the conversation that made calls and have no result after it, in order (the id of a call
         * is only the one of its round: a model may use the same ids again).
         */
        private List<LLMToolCall> openCalls(List<LLMToolCall> calls, List<LLMChatMessage> transcript) {
            int from = 0;
            for (int i = transcript.size() - 1; i >= 0; i--) {
                if (transcript.get(i).hasToolCalls()) {
                    from = i + 1;
                    break;
                }
            }
            Set<String> answered = new HashSet<>();
            for (int i = from; i < transcript.size(); i++) {
                if (transcript.get(i).getToolCallId() != null) {
                    answered.add(transcript.get(i).getToolCallId());
                }
            }
            return calls.stream().filter(call -> !answered.contains(call.getId())).toList();
        }

        /**
         * The run is resumed and the last round of the agent stopped at a call that waited for a sub-agent: the call is made
         * again (the sub-agent is the one that was started, it goes on), and the calls after it, which were never made, are
         * answered as skipped.
         *
         * @return whether the run was paused again while the call waited
         */
        private boolean resumeParked(List<LLMChatMessage> transcript) throws Exception {
            if (subAgents == null || transcript.isEmpty()) {
                return false;
            }
            LLMChatMessage last = null;
            for (int i = transcript.size() - 1; i >= 0; i--) {
                if (transcript.get(i).getRole() == LLMRole.ASSISTANT) {
                    last = transcript.get(i);
                    break;
                }
            }
            if (last == null || !last.hasToolCalls()) {
                return false;
            }
            List<LLMToolCall> open = openCalls(last.getToolCalls(), transcript);
            if (open.isEmpty() || !WAITING_TOOLS.contains(open.getFirst().getName())) {
                return false;
            }
            LLMToolCall parked = open.getFirst();
            log.info("Agent run {}: the agent waits again for the sub-agent of its call {}", run.getUuid(), parked.getName());
            String result;
            resumedCallId = parked.getId();
            try {
                result = call(tools, parked);
            } catch (Interrupted e) {
                interrupts.remove(runId);
                return true;
            } catch (Forced e) {
                // the author wrote: the call is answered as cut off by the repair that follows
                return false;
            } finally {
                resumedCallId = null;
            }
            if (SubAgentToolsImpl.SUSPENDED.equals(result)) {
                return true;
            }
            List<LLMChatMessage> results = new ArrayList<>();
            results.add(LLMChatMessage.ofToolResult(parked.getId(), result));
            for (LLMToolCall skipped : open.subList(1, open.size())) {
                results.add(LLMChatMessage.ofToolResult(skipped.getId(), Defaults.AGENT_PAUSED_SKIPPED_CALL));
            }
            append(transcript, results.toArray(new LLMChatMessage[0]));
            return false;
        }

        private void append(List<LLMChatMessage> transcript, LLMChatMessage... messages) throws Exception {
            // one writer of the database at a time: a sub-agent may be writing (SQLite fails the second writer)
            changeLock.lock();
            try {
                messageService.append(run, List.of(messages));
            } finally {
                changeLock.unlock();
            }
            transcript.addAll(List.of(messages));
        }

        private FunctionSet tools() throws Exception {
            VirtualFileSystem files = fileSystemService.open(run);
            this.files = files;
            // the commands run on the working copy of the run, and the agent knows its items by handles, never by uuids
            CommandScope scope = commandScopes.forRun(run);
            CommandCage cage = new CommandCage(scope, pairService.translator(run, scope));
            FunctionSet tools = coreTools(files, cage)
                    .and(inferenceServices.getFunctionRegistry().bind(RunTools.class, runTools))
                    .and(inferenceServices.getFunctionRegistry().bind(CheckpointTools.class, checkpointTools));
            // a run the user did not change the book of has nothing to ask about: a tool that always answers "nothing" only
            // invites the model to call it again and again
            if (StringUtils.isNotBlank(fileSystemService.readSyncReport(run))) {
                tools = tools.and(inferenceServices.getFunctionRegistry().bind(SyncTools.class, new SyncToolsImpl(() -> {
                    try {
                        return fileSystemService.readSyncReport(run);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                })));
            }
            if (maxSubagents > 0 && (agent.isSelfSubagentAllowed() || agent.getModelProfiles().size() > 1)) {
                // a sub-agent has the commands and the files of the agent, and nothing else: it can't start sub-agents of its own
                subFiles = files;
                subCage = cage;
                SubAgents pool = new SubAgents(maxSubagents);
                subAgents = pool;
                progress.subAgents = () -> "Sub-agents: " + pool.live() + " working of at most " + maxSubagents;
                SubAgentToolsImpl subAgentTools = new SubAgentToolsImpl(pool, (sub, message) -> startSub(pool, sub, message), () -> currentCallId,
                        () -> resumedCallId, this::subProfile);
                tools = tools.and(inferenceServices.getFunctionRegistry().bind(SubAgentTools.class, subAgentTools));
                if (maxSubagents > 1) {
                    // with one, spawning waits for the sub-agent, there is nothing else to do with it
                    tools = tools.and(inferenceServices.getFunctionRegistry().bind(SubAgentControlTools.class, subAgentTools));
                }
            }
            FunctionSet all = seesImages ? tools.and(inferenceServices.getFunctionRegistry().bind(ImageTools.class, new ImageToolsImpl(files, shown)))
                    : tools;
            singleCalls = singleCallsOf(all);
            return all;
        }

        /**
         * The names of the commands and the tools that are made alone in an answer ({@code BatchPolicy.SINGLE}): a command when any of
         * its overloads says so.
         */
        private Set<String> singleCallsOf(FunctionSet functions) {
            Set<String> alone = commandService.getCommands().stream().filter(definition -> definition.batch() == BatchPolicy.SINGLE)
                    .map(CommandDefinition::commandName).collect(Collectors.toCollection(HashSet::new));
            functions.getDefinitions().stream().filter(definition -> definition.getBatch() == BatchPolicy.SINGLE)
                    .forEach(definition -> alone.add(definition.getName()));
            return Set.copyOf(alone);
        }

        /**
         * The commands, the file tools and the read-only manual, with the results of the commands kept for the tool that
         * saves one to a file.
         */
        private FunctionSet coreTools(VirtualFileSystem files, CommandCage cage) {
            return coreTools(files, cage, agent.getAllowedCommands());
        }

        /**
         * As above, with the commands of the list and not all the ones the agent is allowed.
         */
        private FunctionSet coreTools(VirtualFileSystem files, CommandCage cage, Collection<String> allowed) {
            // what the commands return, whole, for the tool that saves it to a file
            CommandResults commandResults = new CommandResults();
            // the commands that send images to their own model take them from the files, whether this model sees them or not
            return CommandFunctions.create(commandService, allowed, agent.getDestructiveMode(), l -> localization.getValue(l), this::approve,
                            path -> files.readText(VirtualPath.parse(path)), commandResults,
                            path -> files.readImage(VirtualPath.parse(path)).toAttachment(path), cage)
                    .and(fileSystemService.getFileTools(files, commandResults))
                    .and(inferenceServices.getFunctionRegistry().bind(ManualTools.class, new ManualToolsImpl(manualService)));
        }

        /**
         * The message that shows the model the images it asked for: a tool result is text, so they come as a message of
         * the user. It keeps the paths only, the images are read when a request is made ({@link #withImages}).
         */
        private LLMChatMessage imagesMessage(List<VirtualPath> looked) {
            LLMChatMessage message = LLMChatMessage.of(LLMRole.USER, (looked.size() == 1 ? "The image you asked to look at: "
                    : "The images you asked to look at, in this order: ")
                    + looked.stream().map(VirtualPath::toString).collect(java.util.stream.Collectors.joining(", ")));
            looked.forEach(path -> message.addAttachment(ImageFiles.reference(path)));
            return message;
        }

        /**
         * The transcript as the model is sent it: the images of the messages that name files of the workspace are read
         * and scaled down now, the newest {@link #MAX_SHOWN_IMAGES} of them; an older one, one that is gone, and every one
         * when the model does not see images, becomes a note in the text. The transcript itself stays as it is.
         */
        private List<LLMChatMessage> withImages(List<LLMChatMessage> transcript) {
            int room = seesImages ? MAX_SHOWN_IMAGES : 0;
            LLMChatMessage[] sent = new LLMChatMessage[transcript.size()];
            for (int i = transcript.size() - 1; i >= 0; i--) {
                LLMChatMessage message = transcript.get(i);
                if (message.getAttachments().stream().noneMatch(ImageFiles::isReference)) {
                    sent[i] = message;
                    continue;
                }
                LLMChatMessage copy = LLMChatMessage.of(message.getRole(), message.getContent());
                StringBuilder notes = new StringBuilder();
                for (LLMAttachment attachment : message.getAttachments()) {
                    if (!ImageFiles.isReference(attachment)) {
                        copy.addAttachment(attachment);
                    } else if (room > 0) {
                        try {
                            copy.addAttachment(files.readImage(VirtualPath.parse(attachment.getName())).toAttachment(attachment.getName()));
                            room--;
                        } catch (RuntimeException e) {
                            notes.append("\n[").append(attachment.getName()).append(" can't be shown: ").append(messageOf(e)).append(']');
                        }
                    } else {
                        notes.append("\n[").append(attachment.getName()).append(seesImages
                                ? " is not shown any more, view_image shows it again]"
                                : " is not shown, the model of this agent does not see images]");
                    }
                }
                if (!notes.isEmpty()) {
                    copy.setContent(StringUtils.defaultString(copy.getContent()) + notes);
                }
                sent[i] = copy;
            }
            return Arrays.asList(sent);
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
            return limitsNote(progress.contextCap, progress.responseCap);
        }

        private String limitsNote(long contextCap, long responseCap) {
            StringBuilder note = new StringBuilder("Limits of the model you run on:");
            if (contextCap > 0) {
                note.append("\n- Context: ").append(contextCap).append(" tokens, everything you read at once: this prompt, the conversation and "
                        + "the results of your calls. When it is full the run pauses.");
            }
            if (responseCap > 0) {
                note.append("\n- One answer of yours: at most ").append(responseCap).append(" tokens, the text you write and the arguments of "
                        + "your calls together. A call that needs more (a long file, a long text) is cut off and fails: write long texts in parts, "
                        + "with write_file for the first and append_file for the rest, a part per call.");
            }
            return note.append("\nget_run_status tells how much of this is used.").toString();
        }

        /**
         * The transcript with messages of the author that follow each other made one: a command the author ran is a message
         * of the author, so is the summary of a compaction, and some chat templates refuse two of one role in a row. The
         * transcript itself is not changed.
         */
        private List<LLMChatMessage> coalesced(List<LLMChatMessage> transcript) {
            List<LLMChatMessage> result = new ArrayList<>();
            for (LLMChatMessage original : transcript) {
                LLMChatMessage message = withReadableCalls(original);
                LLMChatMessage previous = result.isEmpty() ? null : result.getLast();
                if (previous != null && previous.getRole() == LLMRole.USER && message.getRole() == LLMRole.USER) {
                    LLMChatMessage joined = LLMChatMessage.of(LLMRole.USER, StringUtils.defaultString(previous.getContent())
                            + "\n\n" + StringUtils.defaultString(message.getContent()));
                    previous.getAttachments().forEach(joined::addAttachment);
                    message.getAttachments().forEach(joined::addAttachment);
                    result.set(result.size() - 1, joined);
                } else {
                    result.add(message);
                }
            }
            return result;
        }

        /**
         * The message as the model is sent it back: a call whose arguments are not valid JSON (the answer was cut off in the middle
         * of a text) has {@code {}} instead. The model server reads the arguments of the calls in the history again, and refuses
         * the whole request for the broken one, at every request from then on; the result of the call tells the model what
         * went wrong, and the stored row keeps what it wrote.
         */
        private LLMChatMessage withReadableCalls(LLMChatMessage message) {
            if (message.getRole() != LLMRole.ASSISTANT || !message.hasToolCalls()) {
                return message;
            }
            boolean broken = false;
            List<LLMToolCall> calls = new ArrayList<>();
            for (LLMToolCall call : message.getToolCalls()) {
                if (isJson(call.getArguments())) {
                    calls.add(call);
                } else {
                    broken = true;
                    calls.add(LLMToolCall.of(call.getId(), call.getName(), "{}", call.getSignature()));
                }
            }
            return broken ? LLMChatMessage.ofToolCalls(message.getContent(), calls) : message;
        }

        private static boolean isJson(String text) {
            if (StringUtils.isBlank(text)) {
                return true;
            }
            try {
                return JSON.readTree(text) != null;
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                return false;
            }
        }

        /**
         * The model a sub-agent runs on: its profile, service and the commands it may use (the tools are made for each sub-agent, they
         * write in its folder), and what the model may read and write.
         *
         * @param role the role of the profile, null for the main one
         */
        private record SubModel(String role, ResolvedProfile profile, InferenceService inference, Collection<String> commands, boolean textOnly,
                                long contextCap, long responseCap) {
        }

        /**
         * A section of the system message.
         */
        private record SystemPart(AgentContext.Kind kind, String title, String text) {
        }

        /**
         * The sections of the system message of the agent, in order; the message is these joined by a blank line.
         */
        private List<SystemPart> systemParts() {
            List<SystemPart> parts = new ArrayList<>();
            parts.add(new SystemPart(AgentContext.Kind.SYSTEM_PROMPT, "Prompt of the agent", StringUtils.defaultString(systemPrompt).strip()));
            if (StringUtils.isNotBlank(instructions)) {
                parts.add(new SystemPart(AgentContext.Kind.INSTRUCTIONS, "Instructions of the author",
                        "# Instructions of the author\n\n" + instructions.strip()));
            }
            parts.add(new SystemPart(AgentContext.Kind.NOTE, "Note of the runner", Defaults.AGENT_RUNNER_NOTE.strip()));
            if (run.isInteractive()) {
                parts.add(new SystemPart(AgentContext.Kind.NOTE, "Note for a conversation", Defaults.AGENT_INTERACTIVE_NOTE.strip()));
            }
            if (maxSubagents > 0) {
                parts.add(new SystemPart(AgentContext.Kind.NOTE, "Note about sub-agents", String.format(Defaults.AGENT_SUBAGENTS_NOTE.strip(),
                        maxSubagents, maxSubagents == 1 ? Defaults.AGENT_SUBAGENTS_NOTE_ONE : Defaults.AGENT_SUBAGENTS_NOTE_MANY, requestSlots.cap())));
            }
            String models = modelsNote();
            if (models != null) {
                parts.add(new SystemPart(AgentContext.Kind.NOTE, "Models for sub-agents", models));
            }
            if (progress.contextCap > 0 || progress.responseCap > 0) {
                parts.add(new SystemPart(AgentContext.Kind.NOTE, "Limits of the model", limitsNote()));
            }
            String calls = callsNote();
            if (calls != null) {
                parts.add(new SystemPart(AgentContext.Kind.NOTE, "Note about calls", calls));
            }
            return parts;
        }

        /**
         * What the agent is told up front about how many calls an answer may have, so it does not write calls that are thrown away;
         * null for nothing. The same text every round, the start of the request stays what the cache of the server has.
         */
        private String callsNote() {
            AgentBatchMode mode = agent == null ? AgentBatchMode.BATCHED : agent.batchModeOrDefault();
            if (mode == AgentBatchMode.NOT_BATCHED) {
                return Defaults.AGENT_ONE_CALL_RULE;
            }
            if (mode == AgentBatchMode.BATCHED && tools != null) {
                String alone = tools.getDefinitions().stream().map(FunctionDefinition::getName).filter(singleCalls::contains).sorted()
                        .collect(Collectors.joining(", "));
                return alone.isEmpty() ? null : String.format(Defaults.AGENT_SINGLE_CALLS_RULE, alone);
            }
            return null;
        }

        private List<LLMChatMessage> payload(List<LLMChatMessage> transcript) {
            List<LLMChatMessage> payload = new ArrayList<>();
            payload.add(LLMChatMessage.of(LLMRole.SYSTEM, systemParts().stream().map(SystemPart::text).collect(Collectors.joining("\n\n"))));
            payload.addAll(withImages(coalesced(transcript)));
            return payload;
        }

        // ------------------------------------------------------------------ the context, taken apart

        /**
         * What the model of the run reads with its next request, part by part. Made for a worker that was never started: it loads
         * the run in the context of the run, builds the tools as the loop does, and writes nothing.
         */
        AgentContext describe() throws Exception {
            attributes = runService.openSession(run);
            try (InRequestScope _ = new InRequestScope(attributes)) {
                loadAgent();
                loadModel();
                progress.contextCap = runService.getContextCap(run);
                progress.responseCap = profile == null ? 0 : TokenLimits.responseTokens(profile.ai(), profile.protocol());
                checkpointTools = new CheckpointToolsImpl(() -> run, checkpointService);
                List<AgentContext.Part> parts = new ArrayList<>();
                // the tools first: the system message tells about the calls that are made alone
                Exception toolsFailed = null;
                try {
                    tools = tools();
                } catch (Exception e) {
                    toolsFailed = e;
                }
                for (SystemPart part : systemParts()) {
                    parts.add(new AgentContext.Part(part.kind(), part.title(), part.text(), count(part.text())));
                }
                try {
                    if (toolsFailed != null) {
                        throw toolsFailed;
                    }
                    for (FunctionDefinition definition : tools.getDefinitions()) {
                        String text = StringUtils.defaultString(definition.getDescription()) + "\n\nParameters:\n"
                                + definition.getParametersSchema().toPrettyString();
                        parts.add(new AgentContext.Part(AgentContext.Kind.TOOL, "Tool: " + definition.getName(), text,
                                count(definition.getName() + " " + StringUtils.defaultString(definition.getDescription()) + " "
                                        + definition.getParametersSchema())));
                    }
                } catch (Exception e) {
                    log.warn("The tools of agent run {} could not be made for its context: {}", run.getUuid(), e.getMessage(), e);
                    String note = "The definitions of the tools could not be made: " + messageOf(e);
                    parts.add(new AgentContext.Part(AgentContext.Kind.TOOL, "Tools", note, 0));
                }
                List<AgentMessage> rows = messageService.messages(run);
                boolean startsWithSummary = rows.stream().filter(row -> !row.isCompacted() && !row.isRewound()).findFirst()
                        .map(AgentMessage::isSummary).orElse(false);
                parts.addAll(messageParts(coalesced(messageService.transcript(run)), startsWithSummary));
                List<AgentContext.Point> history = new ArrayList<>();
                long reported = 0;
                // what the prompt and the tools weigh is the same at every answer, as far as it is known
                Map<AgentContext.Kind, Long> fixed = new EnumMap<>(AgentContext.Kind.class);
                for (AgentContext.Part part : parts) {
                    if (part.kind().isSystem() || part.kind() == AgentContext.Kind.TOOL) {
                        fixed.merge(part.kind(), part.tokens(), Long::sum);
                    }
                }
                List<LiveRow> live = new ArrayList<>();
                for (AgentMessage row : rows) {
                    live = replay(live, row);
                    if (row.getContextTokens() != null && row.getContextTokens() > 0) {
                        Map<AgentContext.Kind, Long> estimated = new EnumMap<>(fixed);
                        for (LiveRow read : live) {
                            estimated.merge(read.kind(), read.tokens(), Long::sum);
                        }
                        history.add(new AgentContext.Point(history.size() + 1, row.getContextTokens(), estimated));
                        reported = row.getContextTokens();
                    }
                }
                return new AgentContext(agent.getName(), run.getStatus(), run.getRounds(), parts, history, progress.contextCap, reported);
            } finally {
                attributes.close();
            }
        }

        private long count(String text) throws Exception {
            return inference == null ? text.length() / 4 : inference.countTokensApprox(text);
        }

        /**
         * A message of the conversation the model read at some answer: its kind and its estimated tokens.
         */
        private record LiveRow(AgentMessage row, AgentContext.Kind kind, long tokens) {
        }

        /**
         * What the model read once the row was in the transcript. A summary of a compaction replaced everything before it; the rows a
         * rewind went back over were read until the next row that was kept was written (a rewound compaction is not brought back,
         * the sizes after it are a little too small).
         */
        private List<LiveRow> replay(List<LiveRow> live, AgentMessage row) throws Exception {
            List<LiveRow> next = new ArrayList<>(live);
            if (!row.isRewound()) {
                next.removeIf(read -> read.row().isRewound());
            }
            if (row.isSummary()) {
                next.clear();
            }
            LLMChatMessage message = row.toChatMessage();
            long tokens = count(StringUtils.defaultString(message.getContent()));
            for (LLMToolCall call : message.getToolCalls()) {
                tokens += count(call.getName() + " " + StringUtils.defaultString(call.getArguments()));
            }
            AgentContext.Kind kind = switch (message.getRole()) {
                case ASSISTANT -> AgentContext.Kind.ASSISTANT;
                case TOOL -> AgentContext.Kind.RESULT;
                default -> row.isSummary() ? AgentContext.Kind.SUMMARY : AgentContext.Kind.USER;
            };
            next.add(new LiveRow(row, kind, tokens));
            return next;
        }

        /**
         * The messages of the conversation as the model reads them. An image shows as its name: what the model sees of it is
         * the picture, which the context does not hold as text.
         */
        private List<AgentContext.Part> messageParts(List<LLMChatMessage> conversation, boolean startsWithSummary) throws Exception {
            List<AgentContext.Part> parts = new ArrayList<>();
            Map<String, String> callNames = new HashMap<>();
            boolean first = true;
            for (int i = 0; i < conversation.size(); i++) {
                LLMChatMessage message = conversation.get(i);
                StringBuilder text = new StringBuilder(StringUtils.defaultString(message.getContent()));
                long tokens = count(text.toString());
                for (LLMAttachment attachment : message.getAttachments()) {
                    text.append("\n\n[image: ").append(attachment.getName()).append(']');
                }
                String title = "#" + (i + 1) + " " + message.getRole().name().toLowerCase();
                AgentContext.Kind kind;
                switch (message.getRole()) {
                    case ASSISTANT -> {
                        kind = AgentContext.Kind.ASSISTANT;
                        List<String> names = new ArrayList<>();
                        for (LLMToolCall call : message.getToolCalls()) {
                            callNames.put(call.getId(), call.getName());
                            names.add(call.getName());
                            text.append(text.isEmpty() ? "" : "\n\n").append("[call ").append(call.getId()).append("] ").append(call.getName())
                                    .append('\n').append(StringUtils.defaultString(call.getArguments()));
                            tokens += count(call.getName() + " " + StringUtils.defaultString(call.getArguments()));
                        }
                        title += names.isEmpty() ? "" : " -> " + String.join(", ", names);
                    }
                    case TOOL -> {
                        kind = AgentContext.Kind.RESULT;
                        title += " result of " + callNames.getOrDefault(message.getToolCallId(), "?");
                    }
                    default -> {
                        // the summary of a compaction is the first message of the conversation, a message of the author
                        kind = first && startsWithSummary ? AgentContext.Kind.SUMMARY : AgentContext.Kind.USER;
                        title += kind == AgentContext.Kind.SUMMARY ? " (summary of the compacted rounds)" : "";
                    }
                }
                first = false;
                parts.add(new AgentContext.Part(kind, title, text.toString(), tokens));
            }
            return parts;
        }

        /**
         * What a sub-agent is sent: the prompt and the instructions of the agent, the note of the sub-agent, and its own conversation.
         */
        private List<LLMChatMessage> subPayload(List<LLMChatMessage> conversation, SubModel model, Sub sub) {
            StringBuilder system = new StringBuilder(StringUtils.defaultString(systemPrompt).strip());
            if (StringUtils.isNotBlank(instructions)) {
                system.append("\n\n# Instructions of the author\n\n").append(instructions.strip());
            }
            system.append("\n\n").append(Defaults.AGENT_SUBAGENT_NOTE.strip());
            system.append("\n\nYour folder: ").append(sub.folder());
            if (model.contextCap() > 0 || model.responseCap() > 0) {
                system.append("\n\n").append(limitsNote(model.contextCap(), model.responseCap()));
            }
            List<LLMChatMessage> payload = new ArrayList<>();
            payload.add(LLMChatMessage.of(LLMRole.SYSTEM, system.toString()));
            payload.addAll(coalesced(conversation));
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
                    long estimate = toolTokens();
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

        /**
         * Reads the model of the agent again, so what the author changed in its settings (timeout, URL, key, sampling) is
         * used by the next request of a run that is already going. A model that can't be found now is left as it was.
         */
        private void refreshModel() throws Exception {
            if (profile == null) {
                return;
            }
            ResolvedProfile current = agentService.resolveProfile(profile.profile());
            if (current != null) {
                profile = current;
                inference = inferenceServices.forAIOrThrow(current.ai());
            }
        }

        private Round ask(InferenceService inference, ResolvedProfile profile, List<LLMChatMessage> payload,
                          FunctionSet tools) throws Exception {
            return ask(inference, profile, payload, tools, this::await);
        }

        private Round ask(InferenceService inference, ResolvedProfile profile, List<LLMChatMessage> payload,
                          FunctionSet tools, Waiter waiter) throws Exception {
            // the run has a cap of requests in the air, its agent and sub-agents take turns (a wait in a queue is part of the round: a
            // pause, a stop and a forced message find it)
            long asking = System.nanoTime();
            CompletableFuture<AIGate.Permit> turn = requestSlots.acquire();
            if (!turn.isDone()) {
                log.info("Agent run {} waits for a turn to ask the model ({} waiting, {} in the air)", runId, requestSlots.waiting(), requestSlots.active());
            }
            try (AIGate.Permit _ = waiter.await(turn, () -> turn.cancel(true), true)) {
                // a model with a cap of requests lets the runs take turns
                CompletableFuture<AIGate.Permit> place = aiGate.acquire(profile.ai());
                if (!place.isDone()) {
                    log.info("Agent run {} waits for a place at the AI {} ({} waiting)", runId, profile.ai().getUuid(), aiGate.waiting(profile.ai()));
                }
                try (AIGate.Permit _ = waiter.await(place, () -> place.cancel(true), true)) {
                    long waited = (System.nanoTime() - asking) / 1_000_000;
                    return request(inference, profile, payload, tools, waiter).waited(waited);
                }
            } catch (ExecutionException e) {
                throw e.getCause() instanceof Exception cause ? cause : e;
            }
        }

        private Round request(InferenceService inference, ResolvedProfile profile, List<LLMChatMessage> payload,
                              FunctionSet tools, Waiter waiter) throws Exception {
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
                return waiter.await(answer, token::cancel, false);
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
            boolean locked = changes(call);
            if (locked) {
                lockChanges();
            }
            currentCallId = call.getId();
            try {
                try {
                    CompletableFuture<String> answer = tools.call(call.getName(), call.getArguments(), onThread, canceled::get);
                    // a command that runs on its own is stopped too, when it can be
                    return await(answer, () -> {
                        canceled.set(true);
                        answer.cancel(true);
                    });
                } finally {
                    if (locked) {
                        changeLock.unlock();
                    }
                }
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
            return await(future, cancel, false);
        }

        /**
         * As above. {@code queued}: the wait is for a place in a queue and not for work that is going on, so an ordinary pause or
         * stop ends it too (nothing is lost by it). A pause "at once" ends every wait.
         */
        private <T> T await(CompletableFuture<T> future, Runnable cancel, boolean queued) throws Exception {
            while (true) {
                if (interrupts.contains(runId)) {
                    cancel.run();
                    throw new Interrupted();
                }
                if (!ignoreForce && inbox != null && inbox.isForced()) {
                    cancel.run();
                    throw new Forced();
                }
                try {
                    return future.get(POLL_MILLIS, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    AgentRun current = runService.find(runId);
                    if (current == null || current.isDeleted() || current.getStatus() == AgentRunStatus.STOPPED_DISCARDED) {
                        cancel.run();
                        throw new Discarded();
                    }
                    if ((queued && current.getStatus() == AgentRunStatus.PAUSING) || current.getStatus() == AgentRunStatus.STOPPING) {
                        // a stop does not wait for the request or the call that go on: they are canceled, a command that can be
                        // stopped ends with them (a pause waits for them, it ends at a stable point)
                        cancel.run();
                        throw new Interrupted();
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

        /**
         * Whether the user asked to stop the run and it did not end yet.
         */
        private boolean stopping() {
            try {
                AgentRun current = runService.find(runId);
                return current != null && current.getStatus() == AgentRunStatus.STOPPING;
            } catch (Exception e) {
                log.warn("Agent run {}: its status could not be read: {}", runId, e.getMessage());
                return false;
            }
        }

        // ------------------------------------------------------------------ sub-agents

        /**
         * Waits for the lock of the calls that change the book or the workspace, as the agent does: a pause at once, a forced
         * message and a run thrown away end the wait.
         */
        private void lockChanges() throws Exception {
            while (!changeLock.tryLock(POLL_MILLIS, TimeUnit.MILLISECONDS)) {
                if (interrupts.contains(runId)) {
                    throw new Interrupted();
                }
                if (!ignoreForce && inbox != null && inbox.isForced()) {
                    throw new Forced();
                }
                AgentRun current = runService.find(runId);
                if (current == null || current.isDeleted() || current.getStatus() == AgentRunStatus.STOPPED_DISCARDED) {
                    throw new Discarded();
                }
            }
        }

        /**
         * Starts the loop of a sub-agent on a thread of its own.
         */
        private void startSub(SubAgents pool, Sub sub, String message) {
            LLMChatMessage first = LLMChatMessage.of(LLMRole.USER, message);
            changeLock.lock();
            try {
                messageService.startThread(run, sub.handle(), sub.task(), sub.callId(), sub.profile(), first);
            } catch (Exception e) {
                throw new IllegalStateException("its conversation can't be stored: " + messageOf(e), e);
            } finally {
                changeLock.unlock();
            }
            prepareFolder(sub, true);
            log.info("Agent run {}: sub-agent {} is started ({} of {} working)", run.getUuid(), sub.handle(), pool.live(), maxSubagents);
            List<LLMChatMessage> conversation = new ArrayList<>();
            conversation.add(first);
            executor.execute(() -> runSub(pool, sub, conversation));
        }

        /**
         * Brings back the sub-agents the run stored: the ones that ended are there to be asked about, the ones that only waited
         * (the run was paused, or the server stopped) go on with their conversation.
         *
         * @param start whether the unfinished ones go on; false for a worker that must not start any
         */
        private void restoreSubAgents(boolean start) throws Exception {
            SubAgents pool = subAgents;
            if (pool == null) {
                return;
            }
            for (AgentMessageService.SubThread thread : messageService.threads(run)) {
                if (thread.rewound()) {
                    continue;
                }
                State end = thread.isOver() ? State.valueOf(thread.end()) : null;
                if (end == null && !start) {
                    continue;
                }
                Sub sub = pool.restore(thread.handle(), StringUtils.defaultString(thread.task()), thread.callId(), thread.profile(), end, thread.result(),
                        thread.rounds(), thread.tokens());
                if (end == State.DONE && subFiles.stat(VirtualPath.parse(sub.folder()).resolve("result.md")) != null) {
                    sub.resultFile(sub.folder() + "/result.md");
                }
                if (end == null) {
                    prepareFolder(sub, false);
                    List<LLMChatMessage> conversation = new ArrayList<>(thread.conversation());
                    log.info("Agent run {}: sub-agent {} goes on after {} round(s)", run.getUuid(), sub.handle(), sub.rounds());
                    executor.execute(() -> runSub(pool, sub, conversation));
                }
            }
        }

        private void runSub(SubAgents pool, Sub sub, List<LLMChatMessage> conversation) {
            // the end is stored with the session of the run, so it is made inside of the scope
            try (InRequestScope _ = new InRequestScope(attributes)) {
                try {
                    String answer = subLoop(sub, conversation);
                    endSub(pool, sub, State.DONE, answer);
                } catch (SubStopped e) {
                    endSub(pool, sub, e.state, e.getMessage());
                } catch (Throwable e) {
                    log.warn("Agent run {}: sub-agent {} failed", runId, sub.handle(), e);
                    endSub(pool, sub, State.FAILED, "It failed: " + messageOf(e));
                } finally {
                    // a sub-agent never ends without a state, the agent waits for it
                    endSub(pool, sub, State.FAILED, "It ended without an answer.");
                    log.info("Agent run {}: sub-agent {} is {} after {} round(s), {} tokens", runId, sub.handle(), sub.state().name().toLowerCase(),
                            sub.rounds(), sub.tokens());
                }
            }
        }

        /**
         * Ends the sub-agent for the agent that waits for it and in the transcript, where a conversation that did not end is one
         * that goes on with the run. The first end wins.
         */
        private void endSub(SubAgents pool, Sub sub, State state, String text) {
            if (sub.state().isFinished()) {
                return;
            }
            if (state != State.SUSPENDED) {
                changeLock.lock();
                try {
                    messageService.endThread(run, sub.handle(), state.name(), text);
                } catch (Exception e) {
                    // the run may be thrown away, and with it the rows
                    log.debug("Agent run {}: the end of sub-agent {} is not stored: {}", runId, sub.handle(), e.getMessage());
                } finally {
                    changeLock.unlock();
                }
            }
            if (state == State.DONE) {
                saveResult(sub, text);
            }
            pool.finish(sub, state, text);
        }

        /**
         * The conversation of a sub-agent: it asks the model and makes the calls until the model answers with a text, which is its
         * answer. Nothing of it is stored: it lives while the worker does. Every request counts in the budgets of the run.
         *
         * @return the answer
         * @throws SubStopped when it was canceled, the run was paused or thrown away, or a limit of the run was reached
         */
        private String subLoop(Sub sub, List<LLMChatMessage> conversation) throws Exception {
            // a conversation that was cut off in the middle of a round: the calls nobody answered get their answer
            List<LLMToolCall> cutOff = conversation.isEmpty() || !conversation.stream().anyMatch(LLMChatMessage::hasToolCalls) ? List.of()
                    : openCalls(lastCalls(conversation), conversation);
            if (!cutOff.isEmpty()) {
                List<LLMChatMessage> answers = cutOff.stream().map(open -> LLMChatMessage.ofToolResult(open.getId(), Defaults.AGENT_INTERRUPTED_CALL)).toList();
                storeSub(sub, answers);
                conversation.addAll(answers);
            }
            Waiter waiter = new Waiter() {
                @Override
                public <T> T await(CompletableFuture<T> future, Runnable cancel, boolean queued) throws Exception {
                    return awaitSub(future, cancel, sub);
                }
            };
            SubModel model = subModelOf(sub.profile());
            FunctionSet tools = subTools(sub, model);
            int unreadable = 0;
            int rounds = sub.rounds();
            long tokens = sub.tokens();
            long contextTokens = 0;
            while (true) {
                checkSub(sub);
                if (rounds >= Defaults.AGENT_SUBAGENT_MAX_ROUNDS) {
                    throw new SubStopped(State.FAILED, "It used all its " + Defaults.AGENT_SUBAGENT_MAX_ROUNDS + " rounds without an answer.");
                }
                AgentRun current = runService.find(runId);
                if (progress.maxRounds > 0 && current.getRounds() >= progress.maxRounds) {
                    throw new SubStopped(State.FAILED, "The run used all its " + progress.maxRounds + " rounds.");
                }
                if (runService.isBudgetExceeded(current)) {
                    throw new SubStopped(State.FAILED, "The run spent its budget of " + progress.tokenBudget + " tokens.");
                }
                if (model.contextCap() > 0 && contextTokens >= model.contextCap()) {
                    throw new SubStopped(State.FAILED, "Its context is full (" + contextTokens + " of " + model.contextCap()
                            + " tokens), the task is too big for one sub-agent.");
                }
                Round round;
                int asked;
                try {
                    // the main model is the one of the run, read again every round; the others are the ones built when the sub-agent began
                    ResolvedProfile modelProfile = model.role() == null ? profile : model.profile();
                    InferenceService modelService = model.role() == null ? inference : model.inference();
                    Round answered = ask(modelService, modelProfile, subPayload(conversation, model, sub), tools, waiter);
                    asked = answered.calls().size();
                    round = limitCalls(answered, agent.batchModeOrDefault(), singleCalls);
                    unreadable = 0;
                } catch (SubStopped e) {
                    throw e;
                } catch (Discarded e) {
                    throw new SubStopped(State.CANCELLED, "The run was thrown away.");
                } catch (Exception e) {
                    if (isUnreadableCall(e) && ++unreadable <= MAX_UNREADABLE_CALLS) {
                        LLMChatMessage notice = LLMChatMessage.of(LLMRole.USER, Defaults.AGENT_UNREADABLE_CALL);
                        storeSub(sub, List.of(notice));
                        conversation.add(notice);
                        continue;
                    }
                    log.warn("The model of agent run {} could not be asked for sub-agent {}: {}", runId, sub.handle(), e.getMessage());
                    throw new SubStopped(State.FAILED, "The model could not be asked: " + messageOf(e));
                }
                AgentRun counted;
                while (!changeLock.tryLock(POLL_MILLIS, TimeUnit.MILLISECONDS)) {
                    checkSub(sub);
                }
                try {
                    counted = runService.addUsage(current, round.usage().promptTokens(), round.usage().completionTokens());
                } finally {
                    changeLock.unlock();
                }
                progress.rounds = counted.getRounds();
                progress.tokens = counted.getTotalTokens();
                rounds++;
                tokens += round.usage().promptTokens() + round.usage().completionTokens();
                contextTokens = round.usage().promptTokens() + round.usage().completionTokens();
                sub.progress(rounds, tokens, null);
                if (round.calls().isEmpty()) {
                    String text = StringUtils.defaultString(round.text()).strip();
                    return text.isEmpty() ? "(The sub-agent gave no answer.)" : text;
                }
                LLMChatMessage said = LLMChatMessage.ofToolCalls(StringUtils.isEmpty(round.text()) ? null : round.text(), round.calls());
                conversation.add(said);
                changeLock.lock();
                try {
                    messageService.appendAnswerToThread(run, sub.handle(), said, round.usage().promptTokens() + round.usage().completionTokens(),
                            round.reasoning(), AgentTelemetry.of(round.usage(), asked, round.calls().size(), round.waitMillis(), model.profile().profile().getRole()).toJson());
                } finally {
                    changeLock.unlock();
                }
                boolean failed = false;
                for (LLMToolCall call : round.calls()) {
                    if (failed) {
                        // an error ends the answer, as for the agent
                        LLMChatMessage skipped = LLMChatMessage.ofToolResult(call.getId(), Defaults.AGENT_AFTER_ERROR_SKIPPED_CALL);
                        storeSub(sub, List.of(skipped));
                        conversation.add(skipped);
                        continue;
                    }
                    try {
                        checkSub(sub);
                        long began = System.currentTimeMillis();
                        String result = callSub(call, sub, tools);
                        failed = result.startsWith("Error:");
                        log.info("Agent run {}: sub-agent {} {} {} in {} ms ({} characters)", runId, sub.handle(), call.getName(),
                                result.startsWith("Error:") ? "failed" : "done", System.currentTimeMillis() - began, result.length());
                        LLMChatMessage answer = LLMChatMessage.ofToolResult(call.getId(), result);
                        storeSub(sub, List.of(answer));
                        conversation.add(answer);
                        sub.progress(rounds, tokens, call.getName());
                    } catch (SubStopped e) {
                        if (e.state == State.SUSPENDED) {
                            // the run goes on with this conversation: what is left of the round is answered so that it can
                            List<LLMChatMessage> answers = new ArrayList<>();
                            for (LLMToolCall open : openCalls(round.calls(), conversation)) {
                                answers.add(LLMChatMessage.ofToolResult(open.getId(), open.getId().equals(call.getId())
                                        ? Defaults.AGENT_PAUSED_CALL : Defaults.AGENT_PAUSED_SKIPPED_CALL));
                            }
                            storeSub(sub, answers);
                        }
                        throw e;
                    }
                }
            }
        }

        /**
         * The role of the main model profile, null when the run has none.
         */
        private String mainRole() {
            return profile == null ? null : profile.profile().getRole();
        }

        /**
         * The tools of a sub-agent: the commands of its model and the file tools, which read the whole workspace and write only in the
         * folder of the sub-agent. None for a model that only answers with text.
         */
        private FunctionSet subTools(Sub sub, SubModel model) {
            if (model.textOnly()) {
                return FunctionSet.empty();
            }
            return coreTools(subFiles.scopedTo(VirtualPath.parse(sub.folder())), subCage, model.commands());
        }

        /**
         * Makes the folder of a sub-agent that begins (what an earlier run left in it is gone), or sees that the folder of one that
         * goes on is there.
         */
        private void prepareFolder(Sub sub, boolean fresh) {
            try {
                VirtualPath folder = VirtualPath.parse(sub.folder());
                if (fresh && subFiles.stat(folder) != null) {
                    subFiles.delete(folder, true);
                }
                if (subFiles.stat(folder) == null) {
                    subFiles.createFolder(folder);
                }
            } catch (VirtualFileException e) {
                // the workspace is full or in the way: the sub-agent works without a folder, its writes say why they fail
                log.warn("Agent run {}: the folder of sub-agent {} could not be made: {}", runId, sub.handle(), e.getMessage());
            }
        }

        /**
         * Saves the answer of a sub-agent that is done in its folder, for the agent to read in parts when the call shows it cut.
         */
        private void saveResult(Sub sub, String answer) {
            try {
                VirtualPath file = VirtualPath.parse(sub.folder()).resolve("result.md");
                subFiles.writeText(file, answer, false);
                sub.resultFile(file.toString());
            } catch (VirtualFileException e) {
                log.warn("Agent run {}: the answer of sub-agent {} is not saved: {}", runId, sub.handle(), e.getMessage());
            }
        }

        /**
         * The model a sub-agent runs on, built once for each role. A role that is not there (the agent was edited meanwhile) or whose
         * AI the user can not use now ends the sub-agent.
         *
         * @param role the role of the profile, null for the main one
         */
        private SubModel subModelOf(String role) throws Exception {
            String key = role == null ? "" : role.toLowerCase();
            SubModel known = subModels.get(key);
            if (known != null) {
                return known;
            }
            AgentModelProfile definition = role == null ? agent.getMainProfile() : agent.getModelProfiles().stream()
                    .filter(candidate -> candidate.getRole().equalsIgnoreCase(role)).findFirst().orElse(null);
            ResolvedProfile resolved = definition == null ? null : role == null ? profile : agentService.resolveProfile(definition);
            if (resolved == null) {
                throw new SubStopped(State.FAILED, "The model " + (role == null ? "of the agent" : role) + " is not available.");
            }
            Collection<String> commands = agent.getAllowedCommands();
            if (definition.getCommands() != null) {
                Set<String> narrowed = new HashSet<>(commands);
                narrowed.retainAll(definition.getCommands());
                commands = narrowed;
            }
            SubModel model = new SubModel(role, resolved, inferenceServices.forAIOrThrow(resolved.ai()), commands, role != null && !definition.isTools(),
                    AgentService.contextCap(agent, resolved), TokenLimits.responseTokens(resolved.ai(), resolved.protocol()));
            subModels.put(key, model);
            return model;
        }

        /**
         * The role a sub-agent that the agent asked for runs on: null for the main model, which is also what leaving it out means
         * when the agent may use it.
         *
         * @param requested what the agent wrote, null when it left it out
         * @throws IllegalArgumentException with the text for the agent when it may not
         */
        private String subProfile(String requested) {
            List<AgentModelProfile> all = agent.getModelProfiles();
            AgentModelProfile main = agent.getMainProfile();
            String others = all.stream().skip(1).map(AgentModelProfile::getRole).collect(Collectors.joining(", "));
            if (requested == null || main != null && main.getRole().equalsIgnoreCase(requested)) {
                if (!agent.isSelfSubagentAllowed()) {
                    throw new IllegalArgumentException("You may not start a sub-agent on your own model, name another one with profile: " + others);
                }
                return null;
            }
            return all.stream().skip(1).map(AgentModelProfile::getRole).filter(role -> role.equalsIgnoreCase(requested)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("There is no model " + StringUtils.abbreviate(requested, 40) + " for a sub-agent. "
                            + (others.isEmpty() ? "Leave profile out." : "The models: " + others
                            + (agent.isSelfSubagentAllowed() ? ", or leave it out for your own." : "."))));
        }

        /**
         * The models a sub-agent may run on and what each can do, for the system message; null when the agent has just its own
         * model for them (nothing to choose).
         */
        private String modelsNote() {
            List<AgentModelProfile> all = agent.getModelProfiles();
            if (maxSubagents <= 0 || all.isEmpty() || all.size() == 1 && agent.isSelfSubagentAllowed()
                    && all.getFirst().getCommands() == null) {
                return null;
            }
            StringBuilder note = new StringBuilder("Models for sub-agents (the profile of spawn_agent):");
            for (int i = 0; i < all.size(); i++) {
                AgentModelProfile model = all.get(i);
                boolean main = i == 0;
                if (main && !agent.isSelfSubagentAllowed()) {
                    note.append("\n- ").append(model.getRole()).append(" (your own model): you may not start a sub-agent on it.");
                    continue;
                }
                note.append("\n- ").append(model.getRole());
                if (main) {
                    note.append(" (your own model, the default when profile is left out)");
                }
                if (StringUtils.isNotBlank(model.getDescription())) {
                    note.append(": ").append(model.getDescription().strip());
                }
                note.append(" [").append(!main && !model.isTools() ? "no commands or tools, it only answers with text"
                        : model.getCommands() == null ? "commands: the same as yours" : model.getCommands().isEmpty() ? "no commands, files only"
                        : "commands: " + describeCommands(model.getCommands())).append("]");
            }
            return note.toString();
        }

        private String describeCommands(List<String> commands) {
            List<String> allowed = commands.stream().filter(agent.getAllowedCommands()::contains).sorted().toList();
            if (allowed.isEmpty()) {
                return "none of yours";
            }
            return allowed.size() > MAX_NAMED_COMMANDS ? String.join(", ", allowed.subList(0, MAX_NAMED_COMMANDS)) + " and " + (allowed.size() - MAX_NAMED_COMMANDS) + " more"
                    : String.join(", ", allowed);
        }

        /**
         * The calls of the last message of the conversation that made any.
         */
        private List<LLMToolCall> lastCalls(List<LLMChatMessage> conversation) {
            for (int i = conversation.size() - 1; i >= 0; i--) {
                if (conversation.get(i).hasToolCalls()) {
                    return conversation.get(i).getToolCalls();
                }
            }
            return List.of();
        }

        /**
         * Stores messages of the conversation of a sub-agent; one writer of the database at a time.
         */
        private void storeSub(Sub sub, List<LLMChatMessage> messages) throws Exception {
            if (messages.isEmpty()) {
                return;
            }
            changeLock.lock();
            try {
                messageService.appendToThread(run, sub.handle(), messages);
            } finally {
                changeLock.unlock();
            }
        }

        /**
         * One call of a sub-agent, as {@link #call} for the agent: the calls that change something run one at a time.
         */
        private String callSub(LLMToolCall call, Sub sub, FunctionSet subTools) throws Exception {
            Executor onThread = task -> CompletableFuture.runAsync(() -> {
                try (InRequestScope _ = new InRequestScope(attributes)) {
                    task.run();
                }
            }, executor);
            AtomicReference<Boolean> canceled = new AtomicReference<>(false);
            boolean locked = changes(call);
            if (locked) {
                while (!changeLock.tryLock(POLL_MILLIS, TimeUnit.MILLISECONDS)) {
                    checkSub(sub);
                }
                dirty = true;
            }
            try {
                CompletableFuture<String> answer = subTools.call(call.getName(), call.getArguments(), onThread, canceled::get);
                return awaitSub(answer, () -> {
                    canceled.set(true);
                    answer.cancel(true);
                }, sub);
            } catch (ExecutionException e) {
                Throwable error = e.getCause();
                if (error instanceof FunctionCallException failure && failure.getKind() != FunctionCallException.Kind.FAILED) {
                    log.info("Call of function {} by sub-agent {} refused: {}", call.getName(), sub.handle(), failure.getMessage());
                } else {
                    log.warn("Function {} called by sub-agent {} failed", call.getName(), sub.handle(), error);
                }
                return "Error: " + messageOf(error);
            } finally {
                if (locked) {
                    changeLock.unlock();
                }
            }
        }

        /**
         * Waits for the future for a sub-agent: its cancel, the run being paused, stopped or thrown away end the wait.
         */
        private <T> T awaitSub(CompletableFuture<T> future, Runnable cancel, Sub sub) throws Exception {
            while (true) {
                try {
                    checkSub(sub);
                    return future.get(POLL_MILLIS, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    // looks again
                } catch (SubStopped e) {
                    cancel.run();
                    throw e;
                } catch (CancellationException e) {
                    throw new ExecutionException(e);
                }
            }
        }

        /**
         * Ends the sub-agent, at a stable point, when it was canceled or the run is not working any more.
         */
        private void checkSub(Sub sub) throws Exception {
            if (sub.isCancelRequested()) {
                throw new SubStopped(State.CANCELLED, "It was canceled.");
            }
            if (sub.isSuspendRequested() || interrupts.contains(runId)) {
                throw new SubStopped(State.SUSPENDED, "The run was paused.");
            }
            AgentRun current = runService.find(runId);
            if (current == null || current.isDeleted() || current.getStatus() == AgentRunStatus.STOPPED_DISCARDED) {
                throw new SubStopped(State.CANCELLED, "The run was thrown away.");
            }
            if (current.getStatus() != AgentRunStatus.RUNNING && current.getStatus() != AgentRunStatus.WAITING_APPROVAL) {
                if (current.getStatus() == AgentRunStatus.PAUSING || current.getStatus() == AgentRunStatus.PAUSED
                        || current.getStatus() == AgentRunStatus.STOPPING && runService.getStopTarget(current) == AgentRunStatus.STOPPED_KEPT) {
                    // it goes on when the run is resumed
                    throw new SubStopped(State.SUSPENDED, "The run was paused.");
                }
                throw new SubStopped(State.CANCELLED, "The run was stopped or ended before it was done.");
            }
        }

        /**
         * Whether the run is paused (or stopped to be kept): what its sub-agents said is stored and goes on with it.
         */
        private boolean waitsForResume() {
            try {
                AgentRun current = runService.find(runId);
                return current != null && (current.getStatus() == AgentRunStatus.PAUSING || current.getStatus() == AgentRunStatus.PAUSED
                        || current.getStatus() == AgentRunStatus.STOPPING && runService.getStopTarget(current) == AgentRunStatus.STOPPED_KEPT
                        || current.getStatus() == AgentRunStatus.STOPPED_KEPT);
            } catch (Exception e) {
                log.warn("Agent run {}: its status could not be read: {}", runId, e.getMessage());
                return false;
            }
        }

        /**
         * Stops the sub-agents that work and waits (a little) for their threads: they use the session of the run. When the run
         * is paused they stop where they are and go on with it, otherwise they are canceled.
         *
         * @return whether any was working
         */
        private boolean stopSubAgents() {
            SubAgents agents = subAgents;
            if (agents == null) {
                return false;
            }
            subAgents = null;
            progress.subAgents = null;
            boolean working = agents.live() > 0;
            if (waitsForResume()) {
                // they wait for the resume where they are
                agents.suspend();
            } else {
                agents.cancelAll();
            }
            try {
                agents.allFinished().get(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("Agent run {}: the sub-agents did not stop in time: {}", runId, e.getMessage());
            }
            return working;
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

    /**
     * Whether the model server refused the answer because it could not read the arguments of a call in it, which says nothing is
     * wrong with the server: the same context makes the same mistake again, so the agent has to be told.
     */
    static boolean isUnreadableCall(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause() == cause ? null : cause.getCause()) {
            if (cause.getMessage() != null && UNREADABLE_CALL.matcher(cause.getMessage()).find()) {
                return true;
            }
        }
        return false;
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
