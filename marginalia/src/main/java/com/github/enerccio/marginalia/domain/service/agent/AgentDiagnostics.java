package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.AgentMessage;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * What a run did, counted from the transcript as it is stored: how the calls were spread over the tools, which of them failed, where
 * the context went, where the time went, and the patterns that cost rounds (the same tool called round after round, a search followed
 * by one edit per hit, the same call made again). For finding out what an agent needs from its tools and its prompt, with a long run
 * of a real model as the data. Made by {@code AgentRunner#diagnostics}; works for any run, also an old one, because it needs nothing
 * but the rows.
 * <p>
 * The time of a step is the gap between the time its row was written and the time of the row before it: for an answer of the model
 * that is the request (the model and the wait for a free place), for the result of a call the call itself (the results are stored one by
 * one as the calls are made). A gap of {@link #IDLE_MILLIS} or more is a pause of the run or a wait for the author and counts as idle,
 * not as work. The tokens of a result are estimated from its characters.
 */
public final class AgentDiagnostics {

    /** A gap this long between two rows is not work: the run was paused, or waited for the author. */
    public static final long IDLE_MILLIS = 30 * 60 * 1000L;

    /** Rounds of the same tool in a row from which it is reported as a series. */
    public static final int SERIES_FROM = 3;

    /** A text argument this long is a text the model wrote out, not a name or a number. */
    public static final int LONG_TEXT = 300;

    /** Rounds in a block of the timeline. */
    public static final int BLOCK = 25;

    // the results before a call that its long texts are looked for in: what the model has just read is what it copies
    private static final int RECENT_RESULTS = 60;
    private static final int SAMPLE = 80;
    private static final int TOP = 10;
    private static final int PREVIEW = 120;
    private static final int CHARS_PER_TOKEN = 4;
    private static final String ERROR = "Error:";
    private static final String ERROR_INTERRUPTED = "Error: this call ";
    private static final Set<String> SEARCHES = Set.of("grep_files", "search-parts", "find-occurrences", "search-lore");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private AgentDiagnostics() {
    }

    /**
     * The totals of the run.
     *
     * @param rounds           the answers of the model
     * @param textAnswers      the answers without a call
     * @param calls            the calls it made
     * @param errors           the calls that ended with an error
     * @param interrupted      the calls that did not finish because of a pause, a stop or a message of the author
     * @param authorMessages   the messages of the author (and of the runner in their role) that are not a notice from the run
     * @param notices          the notices the runner gave the agent, by kind
     * @param rewound          the rows the run went back over
     * @param compactions      how many times the work was compacted
     * @param wallMillis       from the first row to the last
     * @param modelMillis      the requests, idle gaps left out
     * @param toolMillis       the calls, idle gaps left out
     * @param idleMillis       the pauses and waits
     * @param peakContext      the biggest context a request reported, in tokens
     * @param lastContext      the context at the last answer
     * @param growthPerRound   how much the context grew at an answer on average (the drops of a compaction left out)
     * @param promptTokens     what the requests of the run used for their prompts, as reported
     * @param generatedTokens  what the model generated, as reported
     * @param avgCallsPerRound calls divided by the answers
     * @param argChars         the characters of the arguments of all the calls the model wrote
     * @param textChars        the characters of the text the model wrote besides its calls
     * @param reasoningChars   the characters of the reasoning the provider sent
     */
    public record Overview(long rounds, long textAnswers, long calls, long errors, long interrupted, long authorMessages,
                           Map<String, Long> notices, long rewound, long compactions, long wallMillis, long modelMillis,
                           long toolMillis, long idleMillis, long peakContext, long lastContext, long growthPerRound,
                           long promptTokens, long generatedTokens, double avgCallsPerRound, long argChars, long textChars,
                           long reasoningChars) {
    }

    /**
     * One tool.
     *
     * @param name         the name of the function
     * @param calls        how many times it was called
     * @param errors       the calls that ended with an error
     * @param interrupted  the calls a pause, a stop or a message of the author cut off
     * @param cut          the results that were cut (the model had to ask for the rest)
     * @param soloRounds   the rounds in which it was the only call
     * @param argChars     the characters of the arguments the model wrote in all
     * @param resultChars  the characters of the results in all
     * @param maxResult    the longest result
     * @param millis       the time of its calls
     * @param maxMillis    the longest call
     * @param referenced   the calls with a text argument given as {@code @file:} or {@code @result}
     * @param longTexts    the calls with a text argument written out in full ({@value #LONG_TEXT} characters or more)
     * @param copied       the calls whose long text is in one of the results the model had just been given (it copied it)
     * @param copiedChars  the characters of those texts
     */
    public record ToolStat(String name, long calls, long errors, long interrupted, long cut, long soloRounds, long argChars,
                           long resultChars, long maxResult, long millis, long maxMillis, long referenced, long longTexts,
                           long copied, long copiedChars) {

        /** Estimated tokens of all its results. */
        public long resultTokens() {
            return resultChars / CHARS_PER_TOKEN;
        }

        public double errorRate() {
            return calls == 0 ? 0 : errors * 1.0 / calls;
        }
    }

    /**
     * Rounds in a row that called only one tool, three or more.
     *
     * @param tool        the tool
     * @param firstRound  the round it started in
     * @param rounds      how many rounds
     * @param calls       how many calls
     * @param singleCalls how many of the rounds had one call only (the ones that more calls in one answer would have saved)
     * @param afterSearch whether a search round came right before it (a search followed by one edit per hit)
     * @param millis      the time of the series
     */
    public record Series(String tool, int firstRound, int rounds, int calls, int singleCalls, boolean afterSearch, long millis) {
    }

    /**
     * A call made again with the same arguments.
     *
     * @param tool      the tool
     * @param arguments the arguments, cut
     * @param times     how many times it was made in all
     */
    public record Repeat(String tool, String arguments, int times) {
    }

    /**
     * An error message of a tool and how often it came.
     *
     * @param example the start of the arguments of the first call that got it
     */
    public record ErrorStat(String tool, String message, int times, String example) {
    }

    /**
     * One of the biggest results.
     *
     * @param round     the round of the call
     * @param tool      the tool
     * @param arguments the arguments, cut
     * @param chars     the characters of the result
     * @param cut       whether the result was cut
     */
    public record Biggest(int round, String tool, String arguments, long chars, boolean cut) {

        public long tokens() {
            return chars / CHARS_PER_TOKEN;
        }
    }

    /**
     * One of the calls with the longest arguments.
     *
     * @param round     the round of the call
     * @param tool      the tool
     * @param arguments the start of the arguments (the path of a file tool is in it)
     * @param chars     the characters of the arguments
     * @param copied    whether it is a text the model had just been given in a result
     */
    public record BigArgument(int round, String tool, String arguments, long chars, boolean copied) {
    }

    /**
     * What the model wrote in {@value #BLOCK} rounds.
     *
     * @param firstRound     the first round of the block
     * @param lastRound      the last
     * @param calls          its calls
     * @param argChars       the characters of their arguments
     * @param textChars      the characters of the text besides the calls
     * @param reasoningChars the characters of the reasoning
     * @param referenced     the calls with a text argument given as a reference
     * @param longTexts      the calls with a long text written out
     * @param copied         the calls whose long text was in a result just given
     * @param errors         the calls that failed
     * @param modelMillis    the requests
     */
    public record Block(int firstRound, int lastRound, long calls, long argChars, long textChars, long reasoningChars,
                        long referenced, long longTexts, long copied, long errors, long modelMillis) {
    }

    /**
     * One of the slowest rounds.
     *
     * @param round         the number of the round
     * @param modelMillis   the request
     * @param toolMillis    the calls
     * @param contextTokens the context at the answer, 0 when unknown
     * @param calls         the tools it called
     */
    public record Slow(int round, long modelMillis, long toolMillis, long contextTokens, List<String> calls) {
    }

    /**
     * One compaction.
     *
     * @param afterRound    the round it came after
     * @param contextBefore the context at the answer before it, 0 when unknown
     * @param rows          how many rows it replaced
     */
    public record Compaction(int afterRound, long contextBefore, int rows) {
    }

    /**
     * One sub-agent.
     *
     * @param handle the handle
     * @param task   what it was asked, cut
     * @param state  how it ended, {@code PAUSED} when it only waits to be continued
     * @param rounds how many times the model answered
     * @param tokens what the requests cost, as reported
     * @param calls  how many calls it made
     * @param errors how many of them ended with an error
     * @param tools  the tools it called, with the numbers
     */
    public record SubAgent(String handle, String task, String state, int rounds, long tokens, int calls, int errors,
                           Map<String, Integer> tools) {
    }

    /**
     * The whole report.
     *
     * @param agentName     the name of the agent
     * @param status        the status of the run
     * @param started       when the run started, null when not known
     * @param overview      the totals
     * @param tools         the tools, the most called first
     * @param series        the series of rounds of one tool, the longest first
     * @param repeats       the calls made again, the most often first
     * @param errorMessages the errors, the most frequent first
     * @param biggest       the biggest results
     * @param bigArguments  the calls with the longest arguments
     * @param blocks        what the model wrote, {@value #BLOCK} rounds at a time
     * @param slowest       the slowest rounds
     * @param compactions   the compactions
     * @param subAgents     the sub-agents
     */
    public record Report(String agentName, AgentRunStatus status, String started, Overview overview, List<ToolStat> tools,
                         List<Series> series, List<Repeat> repeats, List<ErrorStat> errorMessages, List<Biggest> biggest,
                         List<BigArgument> bigArguments, List<Block> blocks, List<Slow> slowest, List<Compaction> compactions, List<SubAgent> subAgents) {

        /** The report as JSON, for a file to keep or to hand to somebody. */
        public String toJson() {
            return GSON.toJson(this);
        }

        /** The report as a Markdown document. */
        public String toMarkdown() {
            return AgentDiagnostics.toMarkdown(this);
        }
    }

    // a call with its result
    private static final class Call {
        final int round;
        final LLMToolCall call;
        String result;
        long millis;
        // what the arguments carry
        int references;
        int longTexts;
        // the characters of the long texts
        long longChars;
        boolean copied;

        Call(int round, LLMToolCall call) {
            this.round = round;
            this.call = call;
        }

        int argChars() {
            return call.getArguments().length();
        }

        boolean answered() {
            return result != null;
        }

        boolean failed() {
            return result != null && result.startsWith(ERROR) && !interrupted();
        }

        boolean interrupted() {
            return result != null && result.startsWith(ERROR_INTERRUPTED);
        }

        boolean cut() {
            return result != null && (result.contains("\n[cut: ") || result.contains("go on with start_line"));
        }

        int resultChars() {
            return result == null ? 0 : result.length();
        }
    }

    // an answer of the model with its calls
    private static final class Round {
        final int number;
        final List<Call> calls = new ArrayList<>();
        long modelMillis;
        long contextTokens;
        long textChars;
        long reasoningChars;

        Round(int number) {
            this.number = number;
        }

        long toolMillis() {
            return calls.stream().mapToLong(call -> call.millis).sum();
        }

        List<String> names() {
            return calls.stream().map(call -> call.call.getName()).toList();
        }
    }

    // what is read from the rows of one conversation
    private static final class Scan {
        final List<Round> rounds = new ArrayList<>();
        final List<Compaction> compactions = new ArrayList<>();
        final Map<String, Long> notices = new TreeMap<>();
        long textAnswers;
        long authorMessages;
        long rewound;
        long idleMillis;
        long modelMillis;
        long toolMillis;
        long wallMillis;

        List<Call> calls() {
            return rounds.stream().flatMap(round -> round.calls.stream()).toList();
        }
    }

    /**
     * Counts a run.
     *
     * @param agentName the name of the agent
     * @param run       the run
     * @param rows      the transcript of the agent
     * @param threads   the conversations of its sub-agents
     */
    public static Report analyze(String agentName, AgentRun run, List<AgentMessage> rows, List<AgentMessageService.SubThread> threads) {
        Scan scan = scan(rows);
        List<Call> calls = scan.calls();

        Map<String, List<Call>> byTool = new LinkedHashMap<>();
        calls.forEach(call -> byTool.computeIfAbsent(call.call.getName(), name -> new ArrayList<>()).add(call));
        List<ToolStat> tools = byTool.entrySet().stream().map(entry -> toolStat(entry.getKey(), entry.getValue(), scan))
                .sorted(Comparator.comparingLong(ToolStat::calls).reversed().thenComparing(ToolStat::name)).toList();

        List<Long> contexts = scan.rounds.stream().map(round -> round.contextTokens).filter(value -> value > 0).toList();
        long grown = 0;
        int steps = 0;
        for (int i = 1; i < contexts.size(); i++) {
            long delta = contexts.get(i) - contexts.get(i - 1);
            if (delta > 0) {
                grown += delta;
                steps++;
            }
        }
        Overview overview = new Overview(scan.rounds.size(), scan.textAnswers, calls.size(),
                calls.stream().filter(Call::failed).count(), calls.stream().filter(Call::interrupted).count(), scan.authorMessages,
                scan.notices, scan.rewound, scan.compactions.size(), scan.wallMillis, scan.modelMillis, scan.toolMillis,
                scan.idleMillis, contexts.stream().mapToLong(Long::longValue).max().orElse(0),
                contexts.isEmpty() ? 0 : contexts.getLast(), steps == 0 ? 0 : grown / steps,
                run == null ? 0 : run.getPrefillTokens(), run == null ? 0 : run.getGeneratedTokens(),
                scan.rounds.isEmpty() ? 0 : calls.size() * 1.0 / scan.rounds.size(), calls.stream().mapToLong(Call::argChars).sum(),
                scan.rounds.stream().mapToLong(round -> round.textChars).sum(),
                scan.rounds.stream().mapToLong(round -> round.reasoningChars).sum());

        List<Biggest> biggest = calls.stream().filter(Call::answered)
                .sorted(Comparator.comparingInt(Call::resultChars).reversed()).limit(TOP)
                .map(call -> new Biggest(call.round, call.call.getName(), preview(call.call.getArguments()), call.resultChars(), call.cut()))
                .toList();
        List<BigArgument> bigArguments = calls.stream().sorted(Comparator.comparingInt(Call::argChars).reversed()).limit(TOP)
                .map(call -> new BigArgument(call.round, call.call.getName(), preview(call.call.getArguments()), call.argChars(), call.copied))
                .toList();
        List<Slow> slowest = scan.rounds.stream()
                .sorted(Comparator.comparingLong((Round round) -> round.modelMillis + round.toolMillis()).reversed()).limit(5)
                .map(round -> new Slow(round.number, round.modelMillis, round.toolMillis(), round.contextTokens, round.names())).toList();

        List<SubAgent> subAgents = new ArrayList<>();
        for (AgentMessageService.SubThread thread : threads == null ? List.<AgentMessageService.SubThread>of() : threads) {
            if (thread.rewound()) {
                continue;
            }
            Scan sub = scan(thread.rows().stream().filter(row -> row.getThreadEnd() == null).toList());
            Map<String, Integer> used = new TreeMap<>();
            sub.calls().forEach(call -> used.merge(call.call.getName(), 1, Integer::sum));
            subAgents.add(new SubAgent(thread.handle(), preview(thread.task()), thread.isOver() ? thread.end() : "PAUSED", thread.rounds(),
                    thread.tokens(), sub.calls().size(), (int) sub.calls().stream().filter(Call::failed).count(), used));
        }

        return new Report(agentName, run == null ? null : run.getStatus(),
                run == null || run.getStartedAt() == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(run.getStartedAt()),
                overview, tools, series(scan.rounds), repeats(calls), errors(calls), biggest, bigArguments, blocks(scan.rounds), slowest,
                scan.compactions, subAgents);
    }

    private static Scan scan(List<AgentMessage> rows) {
        Scan scan = new Scan();
        Map<String, Call> open = new HashMap<>();
        Deque<String> recent = new ArrayDeque<>();
        Round round = null;
        Date previous = null;
        Date first = null;
        Date last = null;
        long lastContext = 0;
        for (AgentMessage row : rows) {
            Date now = row.getCreation();
            long gap = previous == null || now == null ? 0 : Math.max(0, now.getTime() - previous.getTime());
            boolean idle = gap >= IDLE_MILLIS;
            if (idle) {
                scan.idleMillis += gap;
            }
            if (now != null) {
                previous = now;
                first = first == null ? now : first;
                last = now;
            }
            if (row.isRewound()) {
                scan.rewound++;
            }
            LLMChatMessage message = row.toChatMessage();
            if (message == null || message.getRole() == null) {
                continue;
            }
            if (row.isSummary()) {
                int replaced = (int) rows.stream().filter(other -> other.isCompacted() && other.getOrdinal() < row.getOrdinal()).count();
                scan.compactions.add(new Compaction(scan.rounds.size(), lastContext, replaced));
                round = null;
                continue;
            }
            switch (message.getRole()) {
                case ASSISTANT -> {
                    round = new Round(scan.rounds.size() + 1);
                    round.modelMillis = idle ? 0 : gap;
                    scan.modelMillis += round.modelMillis;
                    round.contextTokens = row.getContextTokens() == null ? 0 : row.getContextTokens();
                    round.textChars = StringUtils.length(message.getContent());
                    round.reasoningChars = StringUtils.length(row.getReasoning());
                    lastContext = round.contextTokens > 0 ? round.contextTokens : lastContext;
                    scan.rounds.add(round);
                    if (message.getToolCalls().isEmpty()) {
                        scan.textAnswers++;
                    }
                    for (LLMToolCall toolCall : message.getToolCalls()) {
                        Call call = new Call(round.number, toolCall);
                        arguments(call, recent);
                        round.calls.add(call);
                        open.put(toolCall.getId(), call);
                    }
                }
                case TOOL -> {
                    Call call = open.remove(message.getToolCallId());
                    if (call != null) {
                        call.result = StringUtils.defaultString(message.getContent());
                        recent.addLast(call.result);
                        if (recent.size() > RECENT_RESULTS) {
                            recent.removeFirst();
                        }
                        call.millis = idle ? 0 : gap;
                        scan.toolMillis += call.millis;
                    }
                }
                case USER -> {
                    round = null;
                    String kind = notice(message.getContent());
                    if (kind == null) {
                        scan.authorMessages++;
                    } else {
                        scan.notices.merge(kind, 1L, Long::sum);
                    }
                }
                default -> {
                }
            }
        }
        scan.wallMillis = first == null ? 0 : last.getTime() - first.getTime();
        return scan;
    }

    // what the text arguments of a call are: references to a file or a result, short values, or long texts written out (and whether
    // such a text is in one of the results the model was just given)
    private static void arguments(Call call, Collection<String> recent) {
        JsonElement parsed;
        try {
            parsed = JsonParser.parseString(call.call.getArguments());
        } catch (RuntimeException e) {
            // cut off in the middle of the text: what is there counts as written
            call.longTexts = call.argChars() >= LONG_TEXT ? 1 : 0;
            call.longChars = call.longTexts > 0 ? call.argChars() : 0;
            return;
        }
        if (!parsed.isJsonObject()) {
            return;
        }
        JsonObject object = parsed.getAsJsonObject();
        for (Map.Entry<String, JsonElement> argument : object.entrySet()) {
            if (!argument.getValue().isJsonPrimitive() || !argument.getValue().getAsJsonPrimitive().isString()) {
                continue;
            }
            String text = argument.getValue().getAsString();
            if (text.startsWith(CommandFunctions.FILE_PREFIX) || CommandResults.isReference(text)) {
                call.references++;
                continue;
            }
            if (text.length() >= LONG_TEXT) {
                call.longTexts++;
                call.longChars += text.length();
                int from = text.length() / 2 - SAMPLE / 2;
                String sample = text.substring(from, from + SAMPLE);
                if (recent.stream().anyMatch(result -> result.contains(sample))) {
                    call.copied = true;
                }
            }
        }
    }

    // the kinds of the notices of the runner (the text of each with its values left open), null for a message of the author
    private static final Map<Pattern, String> NOTICES = new LinkedHashMap<>();

    static {
        notice(Defaults.AGENT_UNREADABLE_CALL, "unreadable call");
        notice(Defaults.AGENT_TOO_MANY_CALLS, "too many calls");
        notice(Defaults.AGENT_ONE_CALL_NOTICE, "one call per round");
        notice(Defaults.AGENT_SINGLE_CALL_NOTICE, "command made alone");
        notice(Defaults.AGENT_CONTEXT_WARNING, "context warning");
        notice(Defaults.AGENT_CONTEXT_CRITICAL, "context critical");
        notice(Defaults.AGENT_SUBAGENTS_STILL_WORKING, "sub-agents still work");
        notice(Defaults.AGENT_RESTORED, "restored");
        notice(Defaults.AGENT_REWOUND, "rewound");
    }

    private static void notice(String format, String kind) {
        // the parts of the text between the values, found one after the other from the start
        String[] parts = format.replace("%%", "\0").split("%[-#+ 0,(\\d.$]*[a-zA-Z]", -1);
        String pattern = Arrays.stream(parts).map(part -> Pattern.quote(part.replace("\0", "%")))
                .collect(Collectors.joining(".*?"));
        NOTICES.put(Pattern.compile(pattern, Pattern.DOTALL), kind);
    }

    private static String notice(String content) {
        if (content == null) {
            return null;
        }
        for (Map.Entry<Pattern, String> notice : NOTICES.entrySet()) {
            if (notice.getKey().matcher(content).lookingAt()) {
                return notice.getValue();
            }
        }
        return content.startsWith("Notice from the run:") ? "other" : null;
    }

    private static ToolStat toolStat(String name, List<Call> calls, Scan scan) {
        long solo = scan.rounds.stream().filter(round -> round.calls.size() == 1 && round.calls.getFirst().call.getName().equals(name)).count();
        return new ToolStat(name, calls.size(), calls.stream().filter(Call::failed).count(), calls.stream().filter(Call::interrupted).count(),
                calls.stream().filter(Call::cut).count(), solo, calls.stream().mapToLong(call -> call.call.getArguments().length()).sum(),
                calls.stream().mapToLong(Call::resultChars).sum(), calls.stream().mapToLong(Call::resultChars).max().orElse(0),
                calls.stream().mapToLong(call -> call.millis).sum(), calls.stream().mapToLong(call -> call.millis).max().orElse(0),
                calls.stream().filter(call -> call.references > 0).count(), calls.stream().filter(call -> call.longTexts > 0).count(),
                calls.stream().filter(call -> call.copied).count(),
                calls.stream().filter(call -> call.copied).mapToLong(call -> call.longChars).sum());
    }

    private static List<Block> blocks(List<Round> rounds) {
        List<Block> blocks = new ArrayList<>();
        for (int from = 0; from < rounds.size(); from += BLOCK) {
            List<Round> part = rounds.subList(from, Math.min(rounds.size(), from + BLOCK));
            List<Call> calls = part.stream().flatMap(round -> round.calls.stream()).toList();
            blocks.add(new Block(part.getFirst().number, part.getLast().number, calls.size(),
                    calls.stream().mapToLong(Call::argChars).sum(), part.stream().mapToLong(round -> round.textChars).sum(),
                    part.stream().mapToLong(round -> round.reasoningChars).sum(), calls.stream().filter(call -> call.references > 0).count(),
                    calls.stream().filter(call -> call.longTexts > 0).count(), calls.stream().filter(call -> call.copied).count(),
                    calls.stream().filter(Call::failed).count(), part.stream().mapToLong(round -> round.modelMillis).sum()));
        }
        return blocks;
    }

    // rounds in a row whose calls are all of one tool
    private static List<Series> series(List<Round> rounds) {
        List<Series> found = new ArrayList<>();
        int i = 0;
        while (i < rounds.size()) {
            String tool = only(rounds.get(i));
            if (tool == null) {
                i++;
                continue;
            }
            int end = i;
            while (end + 1 < rounds.size() && tool.equals(only(rounds.get(end + 1)))) {
                end++;
            }
            int length = end - i + 1;
            if (length >= SERIES_FROM) {
                List<Round> part = rounds.subList(i, end + 1);
                boolean afterSearch = i > 0 && rounds.get(i - 1).names().stream().anyMatch(SEARCHES::contains);
                found.add(new Series(tool, rounds.get(i).number, length, part.stream().mapToInt(round -> round.calls.size()).sum(),
                        (int) part.stream().filter(round -> round.calls.size() == 1).count(), afterSearch,
                        part.stream().mapToLong(round -> round.modelMillis + round.toolMillis()).sum()));
            }
            i = end + 1;
        }
        found.sort(Comparator.comparingInt(Series::rounds).reversed());
        return found;
    }

    // the one tool all the calls of the round are of, null when there are none or more
    private static String only(Round round) {
        Set<String> names = new HashSet<>(round.names());
        return names.size() == 1 ? names.iterator().next() : null;
    }

    private static List<Repeat> repeats(List<Call> calls) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, Call> first = new HashMap<>();
        for (Call call : calls) {
            String key = call.call.getName() + "\n" + call.call.getArguments().replaceAll("\\s+", "");
            counts.merge(key, 1, Integer::sum);
            first.putIfAbsent(key, call);
        }
        return counts.entrySet().stream().filter(entry -> entry.getValue() > 1)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(TOP)
                .map(entry -> new Repeat(first.get(entry.getKey()).call.getName(), preview(first.get(entry.getKey()).call.getArguments()), entry.getValue()))
                .toList();
    }

    private static List<ErrorStat> errors(List<Call> calls) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, Call> first = new HashMap<>();
        for (Call call : calls) {
            if (call.failed()) {
                String message = StringUtils.left(call.result.substring(ERROR.length()).strip().lines().findFirst().orElse("")
                        .replaceAll("\\d+", "N"), PREVIEW);
                String key = call.call.getName() + "\n" + message;
                counts.merge(key, 1, Integer::sum);
                first.putIfAbsent(key, call);
            }
        }
        return counts.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(TOP * 2)
                .map(entry -> {
                    int at = entry.getKey().indexOf('\n');
                    return new ErrorStat(entry.getKey().substring(0, at), entry.getKey().substring(at + 1), entry.getValue(),
                            preview(first.get(entry.getKey()).call.getArguments()));
                }).toList();
    }

    private static String preview(String text) {
        return StringUtils.abbreviate(StringUtils.defaultString(text).replaceAll("\\s+", " ").strip(), PREVIEW);
    }

    private static String duration(long millis) {
        long seconds = millis / 1000;
        if (seconds < 60) {
            return String.format("%.1f s", millis / 1000.0);
        }
        long minutes = seconds / 60;
        return minutes < 60 ? minutes + " min " + seconds % 60 + " s" : minutes / 60 + " h " + minutes % 60 + " min";
    }

    private static String percent(long part, long whole) {
        return whole == 0 ? "-" : String.format("%.0f %%", part * 100.0 / whole);
    }

    private static String cell(String text) {
        return StringUtils.defaultString(text).replace("|", "\\|").replace("\n", " ");
    }

    private static String toMarkdown(Report report) {
        Overview o = report.overview();
        StringBuilder md = new StringBuilder();
        md.append("# Diagnostics of the agent \"").append(StringUtils.defaultString(report.agentName())).append("\"\n\n");
        md.append("Counted from the transcript as it is stored. Times are the gaps between the rows (a gap of ").append(IDLE_MILLIS / 60000)
                .append(" minutes or more is idle), tokens of results are estimated from the characters.\n\n");
        md.append("## Overview\n\n");
        md.append("- Run: ").append(report.status());
        if (report.started() != null) {
            md.append(", started ").append(report.started());
        }
        md.append("\n- Rounds: ").append(o.rounds()).append(" (").append(o.textAnswers()).append(" answered in text), calls: ").append(o.calls())
                .append(String.format(", %.2f calls per round%n", o.avgCallsPerRound()));
        md.append("- Calls that failed: ").append(o.errors()).append(" (").append(percent(o.errors(), o.calls())).append("), cut off by a pause or a message: ")
                .append(o.interrupted()).append('\n');
        md.append("- Time: ").append(duration(o.wallMillis())).append(" from the first row to the last; the model ").append(duration(o.modelMillis()))
                .append(", the calls ").append(duration(o.toolMillis())).append(", idle ").append(duration(o.idleMillis())).append('\n');
        md.append("- Context: peak ").append(o.peakContext()).append(" tokens, ").append(o.lastContext()).append(" at the last answer, grows by about ")
                .append(o.growthPerRound()).append(" per round; compactions: ").append(o.compactions()).append('\n');
        md.append("- Tokens as reported: ").append(o.promptTokens()).append(" prompt, ").append(o.generatedTokens()).append(" generated\n");
        md.append("- Written by the model: ").append(o.argChars()).append(" characters in the arguments of calls, ").append(o.textChars())
                .append(" of text beside them, ").append(o.reasoningChars()).append(" of reasoning (about ")
                .append((o.argChars() + o.textChars() + o.reasoningChars()) / 4).append(" tokens together, at 4 characters a token)\n");
        md.append("- Messages of the author: ").append(o.authorMessages()).append(", rows rewound: ").append(o.rewound()).append('\n');
        if (!o.notices().isEmpty()) {
            md.append("- Notices from the run to the agent: ")
                    .append(o.notices().entrySet().stream().map(entry -> entry.getKey() + " " + entry.getValue()).collect(Collectors.joining(", "))).append('\n');
        }
        md.append('\n');

        md.append("## Tools\n\n");
        if (report.tools().isEmpty()) {
            md.append("No calls.\n\n");
        } else {
            long total = report.tools().stream().mapToLong(ToolStat::resultChars).sum();
            md.append("| Tool | Calls | Errors | Cut | Alone in a round | Written (chars) | With @file / @result | Long text written | Copied from a result | Results (~tokens) | Share of results | Longest result | Time | Longest |\n");
            md.append("|---|---|---|---|---|---|---|---|---|---|---|---|---|---|\n");
            for (ToolStat tool : report.tools()) {
                md.append("| ").append(cell(tool.name())).append(" | ").append(tool.calls()).append(" | ").append(tool.errors())
                        .append(tool.calls() > 0 && tool.errors() > 0 ? " (" + percent(tool.errors(), tool.calls()) + ")" : "")
                        .append(" | ").append(tool.cut()).append(" | ").append(tool.soloRounds()).append(" | ").append(tool.argChars())
                        .append(" | ").append(tool.referenced()).append(" | ").append(tool.longTexts()).append(" | ").append(tool.copied())
                        .append(" | ").append(tool.resultTokens()).append(" | ").append(percent(tool.resultChars(), total))
                        .append(" | ").append(tool.maxResult()).append(" | ").append(duration(tool.millis()))
                        .append(" | ").append(duration(tool.maxMillis())).append(" |\n");
            }
            md.append('\n');
        }

        md.append("## What the model wrote, by block of ").append(BLOCK).append(" rounds\n\n");
        if (report.blocks().isEmpty()) {
            md.append("None.\n\n");
        } else {
            md.append("A call counts as \"long text\" when an argument is a text of ").append(LONG_TEXT).append(" characters or more written out, as \"copied\" when ")
                    .append("that text is in one of the 60 results before it (the model had just read it), as \"with reference\" when a text argument is @file: or @result. ")
                    .append("Characters are of arguments, of the text beside the calls and of the reasoning.\n\n");
            md.append("| Rounds | Calls | Arguments | Text | Reasoning | With reference | Long text | Copied | Errors | Model time |\n|---|---|---|---|---|---|---|---|---|---|\n");
            report.blocks().forEach(block -> md.append("| ").append(block.firstRound()).append('-').append(block.lastRound()).append(" | ")
                    .append(block.calls()).append(" | ").append(block.argChars()).append(" | ").append(block.textChars()).append(" | ")
                    .append(block.reasoningChars()).append(" | ").append(block.referenced()).append(" | ").append(block.longTexts()).append(" | ")
                    .append(block.copied()).append(" | ").append(block.errors()).append(" | ").append(duration(block.modelMillis())).append(" |\n"));
            md.append('\n');
        }

        md.append("## Longest arguments\n\n");
        if (report.bigArguments().isEmpty()) {
            md.append("None.\n\n");
        } else {
            md.append("| Round | Tool | Start of the arguments | Characters | Copied |\n|---|---|---|---|---|\n");
            report.bigArguments().forEach(big -> md.append("| ").append(big.round()).append(" | ").append(cell(big.tool())).append(" | `")
                    .append(cell(big.arguments()).replace("`", "'")).append("` | ").append(big.chars()).append(" | ")
                    .append(big.copied() ? "yes" : "").append(" |\n"));
            md.append('\n');
        }

        md.append("## Rounds lost to a pattern\n\n");
        if (report.series().isEmpty()) {
            md.append("No tool was called in ").append(SERIES_FROM).append(" or more rounds in a row.\n\n");
        } else {
            md.append("Rounds in a row that called only one tool. \"Single\" is how many of them had one call: with more calls in one answer ")
                    .append("(or a tool that takes many items) they would be fewer.\n\n");
            md.append("| Tool | From round | Rounds | Calls | Single | After a search | Time |\n|---|---|---|---|---|---|---|\n");
            for (Series series : report.series()) {
                md.append("| ").append(cell(series.tool())).append(" | ").append(series.firstRound()).append(" | ").append(series.rounds())
                        .append(" | ").append(series.calls()).append(" | ").append(series.singleCalls()).append(" | ")
                        .append(series.afterSearch() ? "yes" : "").append(" | ").append(duration(series.millis())).append(" |\n");
            }
            md.append('\n');
        }
        if (!report.repeats().isEmpty()) {
            md.append("### The same call made again\n\n| Tool | Arguments | Times |\n|---|---|---|\n");
            report.repeats().forEach(repeat -> md.append("| ").append(cell(repeat.tool())).append(" | `").append(cell(repeat.arguments()).replace("`", "'"))
                    .append("` | ").append(repeat.times()).append(" |\n"));
            md.append('\n');
        }

        md.append("## Errors\n\n");
        if (report.errorMessages().isEmpty()) {
            md.append("No call failed.\n\n");
        } else {
            md.append("| Tool | Message (numbers as N) | Times | Arguments of the first call |\n|---|---|---|---|\n");
            report.errorMessages().forEach(error -> md.append("| ").append(cell(error.tool())).append(" | ").append(cell(error.message()))
                    .append(" | ").append(error.times()).append(" | `").append(cell(error.example()).replace("`", "'")).append("` |\n"));
            md.append('\n');
        }

        md.append("## Biggest results\n\n");
        if (report.biggest().isEmpty()) {
            md.append("None.\n\n");
        } else {
            md.append("| Round | Tool | Arguments | Characters | ~Tokens | Cut |\n|---|---|---|---|---|---|\n");
            report.biggest().forEach(big -> md.append("| ").append(big.round()).append(" | ").append(cell(big.tool())).append(" | `")
                    .append(cell(big.arguments()).replace("`", "'")).append("` | ").append(big.chars()).append(" | ").append(big.tokens())
                    .append(" | ").append(big.cut() ? "yes" : "").append(" |\n"));
            md.append('\n');
        }

        md.append("## Slowest rounds\n\n");
        if (report.slowest().isEmpty()) {
            md.append("None.\n\n");
        } else {
            md.append("| Round | Model | Calls | Context | Tools |\n|---|---|---|---|---|\n");
            report.slowest().forEach(slow -> md.append("| ").append(slow.round()).append(" | ").append(duration(slow.modelMillis())).append(" | ")
                    .append(duration(slow.toolMillis())).append(" | ").append(slow.contextTokens()).append(" | ")
                    .append(cell(String.join(", ", slow.calls()))).append(" |\n"));
            md.append('\n');
        }

        if (!report.compactions().isEmpty()) {
            md.append("## Compactions\n\n| After round | Context before | Rows replaced |\n|---|---|---|\n");
            report.compactions().forEach(compaction -> md.append("| ").append(compaction.afterRound()).append(" | ").append(compaction.contextBefore())
                    .append(" | ").append(compaction.rows()).append(" |\n"));
            md.append('\n');
        }

        if (!report.subAgents().isEmpty()) {
            md.append("## Sub-agents\n\n| Handle | State | Rounds | Tokens | Calls | Errors | Tools | Task |\n|---|---|---|---|---|---|---|---|\n");
            report.subAgents().forEach(sub -> md.append("| ").append(cell(sub.handle())).append(" | ").append(sub.state()).append(" | ")
                    .append(sub.rounds()).append(" | ").append(sub.tokens()).append(" | ").append(sub.calls()).append(" | ").append(sub.errors())
                    .append(" | ").append(cell(sub.tools().entrySet().stream().map(entry -> entry.getKey() + " " + entry.getValue())
                            .collect(Collectors.joining(", ")))).append(" | ").append(cell(sub.task())).append(" |\n"));
            md.append('\n');
        }
        return md.toString();
    }
}
