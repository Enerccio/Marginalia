package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.bound.AgentRequestAttributes;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.AgentModelProfile;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.AgentService.ResolvedProfile;
import com.github.enerccio.marginalia.domain.service.InferenceService.RoundUsage;
import com.github.enerccio.marginalia.domain.service.agent.*;
import com.github.enerccio.marginalia.domain.service.agent.RunTools.PlanItem;
import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.domain.service.command.CommandRun;
import com.github.enerccio.marginalia.domain.service.function.FunctionCallException;
import com.github.enerccio.marginalia.domain.service.function.FunctionDefinition;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
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
import java.util.stream.Collectors;

public class AgentRunnerImpl implements AgentRunner, SmartInitializingSingleton, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(AgentRunnerImpl.class);
    private static final Gson GSON = new Gson();

    // how often a worker that waits for a request or a call looks whether the run was thrown away
    private static final long POLL_MILLIS = 1_000;
    // a conversation in these statuses is written to by queueing the message, the worker reads it at the end of the turn
    private static final Set<AgentRunStatus> QUEUEING = EnumSet.of(AgentRunStatus.RUNNING, AgentRunStatus.WAITING_APPROVAL,
            AgentRunStatus.PAUSING);
    // the statuses in which a run keeps what was written to it: it works, waits, or is paused and can be resumed
    private static final Set<AgentRunStatus> LIVE = EnumSet.of(AgentRunStatus.RUNNING, AgentRunStatus.IDLE,
            AgentRunStatus.WAITING_APPROVAL, AgentRunStatus.PAUSING, AgentRunStatus.PAUSED);
    // the status reason holds this much of the report of a finished run
    private static final int MAX_REASON = 2_000;
    // images the model is shown at once: the newest ones; an older one is a note that view_image shows it again
    static final int MAX_SHOWN_IMAGES = 4;

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

    /**
     * The end of a round that is not an exception: the author forced a message in, what the worker waited for is thrown away.
     */
    private static final class Forced extends RuntimeException {
        Forced() {
            super(null, null, false, false);
        }
    }

    private final class Worker implements Runnable {

        private final Long runId;
        private AgentRun run;
        private AgentRequestAttributes attributes;
        private Agent agent;
        // the prompt of the agent or the default it follows, resolved when the work starts (like the instructions)
        private String systemPrompt;
        private final Progress progress = new Progress();
        // made again when the run is loaded, it depends on whether the run is a conversation
        private RunToolsImpl runTools = new RunToolsImpl(progress::render);
        // the files of the run, opened with the tools; the images the model asked to see in this round
        private VirtualFileSystem files;
        private final ShownImages shown = new ShownImages();
        // whether the model of the run looks at images
        private boolean seesImages;
        // the messages of the author, only for a conversation
        private Inbox inbox;
        // the model of the run and its tools, set when the work starts (the compaction asks the model too)
        private ResolvedProfile profile;
        private InferenceService inference;
        private FunctionSet tools;
        // set while a command of the author runs: a message the author forces in then waits for it
        private boolean ignoreForce;
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
                } catch (Discarded e) {
                    log.info("Agent run {} was thrown away while it worked", run.getUuid());
                } catch (Throwable e) {
                    log.error("Agent run {} failed", run.getUuid(), e);
                    failed(e);
                }
            } catch (Throwable e) {
                log.error("Agent run {} could not start working", run.getUuid(), e);
            } finally {
                dropInbox();
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

        private void work() throws Exception {
            run = runService.find(runId);
            runTools = new RunToolsImpl(progress::render, run.isInteractive());
            inbox = run.isInteractive() ? inboxes.computeIfAbsent(runId, id -> new Inbox()) : null;
            agent = agentService.findForUser(run.getAgent().getUuid());
            systemPrompt = agentService.getSystemPrompt(agent);
            profile = agentService.resolveProfile(agent.getMainProfile());
            if (profile == null && initial == null) {
                pause("The model of the agent is not available");
                return;
            }
            // a command of the author that does not ask the model needs none
            inference = profile == null ? null : inferenceServices.forAIOrThrow(profile.ai());
            seesImages = profile != null && profile.profile().isImages();

            List<LLMChatMessage> transcript = new ArrayList<>(messageService.transcript(run));
            if (initial == null) {
                prepare(transcript);
            } else {
                // the run waits: no "go on", the calls of an interrupted round only get their answer
                repairCalls(transcript);
            }
            runTools.restorePlan(planOf(transcript));
            tools = tools();

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
                } catch (Forced e) {
                    log.info("Agent run {}: the request was canceled, the author sent a message", run.getUuid());
                    deliverForced(transcript, List.of());
                    continue;
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
                    if (endTurn(transcript, AgentRunStatus.IDLE, null)) {
                        continue;
                    }
                    return;
                }

                runTools.clearRequests();
                List<LLMChatMessage> results = new ArrayList<>();
                boolean forced = false;
                for (LLMToolCall call : round.calls()) {
                    if (forced || inbox != null && inbox.isForced()) {
                        forced = true;
                        results.add(LLMChatMessage.ofToolResult(call.getId(), Defaults.AGENT_SKIPPED_CALL));
                        continue;
                    }
                    long began = System.currentTimeMillis();
                    String result;
                    try {
                        result = call(tools, call);
                    } catch (Forced e) {
                        forced = true;
                        results.add(LLMChatMessage.ofToolResult(call.getId(), Defaults.AGENT_FORCED_CALL));
                        continue;
                    }
                    // the arguments and the result are the story's business and not logged
                    log.info("Agent run {}: {} {} in {} ms ({} characters)", run.getUuid(), call.getName(),
                            result.startsWith("Error:") ? "failed" : "done", System.currentTimeMillis() - began, result.length());
                    results.add(LLMChatMessage.ofToolResult(call.getId(), result));
                }
                if (forced) {
                    log.info("Agent run {}: the rest of the round was thrown away, the author sent a message", run.getUuid());
                    deliverForced(transcript, results);
                    continue;
                }
                append(transcript, results.toArray(new LLMChatMessage[0]));
                List<VirtualPath> looked = shown.take();
                if (!looked.isEmpty()) {
                    append(transcript, imagesMessage(looked));
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
            }
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
                        compact(commandArguments(line), transcript);
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
         * @param focus what the author wants the summary to stress, may be empty
         */
        private void compact(String focus, List<LLMChatMessage> transcript) throws Exception {
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
                    : prompt + "\n\nThe author wants the summary to stress: " + focus));

            Round round = ask(inference, profile, payload(history), tools);
            run = runService.addUsage(run, round.usage().promptTokens(), round.usage().completionTokens());
            progress.rounds = run.getRounds();
            progress.tokens = run.getTotalTokens();
            String text = StringUtils.defaultString(round.text()).strip();
            if (text.isEmpty()) {
                throw new IllegalStateException(round.calls().isEmpty() ? "The model wrote no summary"
                        : "The model called a tool instead of writing the summary");
            }
            StringBuilder message = new StringBuilder(Defaults.AGENT_COMPACTED_NOTE).append("\n\n").append(text);
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
            messageService.compact(run, summary, tokens);
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
                due = inbox.takeThrough(inbox.forcedId());
            }
            if (!due.isEmpty()) {
                deliver(due, transcript);
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
            repairCalls(transcript);
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

        private void append(List<LLMChatMessage> transcript, LLMChatMessage... messages) throws Exception {
            messageService.append(run, List.of(messages));
            transcript.addAll(List.of(messages));
        }

        private FunctionSet tools() throws Exception {
            VirtualFileSystem files = fileSystemService.open(run);
            this.files = files;
            // what the commands return, whole, for the tool that saves it to a file
            CommandResults commandResults = new CommandResults();
            // the commands that send images to their own model take them from the files, whether this model sees them or not
            FunctionSet tools = CommandFunctions.forAgent(commandService, agent, l -> localization.getValue(l), this::approve,
                            path -> files.readText(VirtualPath.parse(path)), commandResults,
                            path -> files.readImage(VirtualPath.parse(path)).toAttachment(path))
                    .and(fileSystemService.getFileTools(files, commandResults))
                    .and(inferenceServices.getFunctionRegistry().bind(RunTools.class, runTools));
            return seesImages ? tools.and(inferenceServices.getFunctionRegistry().bind(ImageTools.class, new ImageToolsImpl(files, shown)))
                    : tools;
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

        /**
         * The transcript with messages of the author that follow each other made one: a command the author ran is a message
         * of the author, so is the summary of a compaction, and some chat templates refuse two of one role in a row. The
         * transcript itself is not changed.
         */
        private List<LLMChatMessage> coalesced(List<LLMChatMessage> transcript) {
            List<LLMChatMessage> result = new ArrayList<>();
            for (LLMChatMessage message : transcript) {
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

        private List<LLMChatMessage> payload(List<LLMChatMessage> transcript) {
            StringBuilder system = new StringBuilder(StringUtils.defaultString(systemPrompt).strip());
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
            payload.addAll(withImages(coalesced(transcript)));
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
