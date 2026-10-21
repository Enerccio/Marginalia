package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * {@link RunTools} of one run. The tools only note what the model asked for; the runner reads it after the round and
 * changes the status of the run, so that every call of the round has its result in the transcript first.
 */
public final class RunToolsImpl implements RunTools {

    public static final int MAX_PLAN_ITEMS = 50;
    public static final int MAX_ITEM_LENGTH = 300;
    public static final int MAX_QUESTION_LENGTH = 4_000;
    public static final int MAX_OPTIONS = 10;

    private final Supplier<String> status;
    private final boolean interactive;

    private volatile String summary;
    private volatile String question;
    private volatile String compaction;
    private volatile List<PlanItem> plan = List.of();
    private volatile Consumer<List<PlanItem>> planStore = items -> {
    };
    private volatile List<Finding> findings = List.of();
    private volatile Consumer<List<Finding>> findingsStore = items -> {
    };

    /**
     * @param status the text of {@code get_run_status} without the plan, made by the runner from the budgets it keeps
     */
    public RunToolsImpl(Supplier<String> status) {
        this(status, false);
    }

    /**
     * @param status      as above
     * @param interactive whether the run is a conversation: the report does not end it, the author may answer
     */
    public RunToolsImpl(Supplier<String> status, boolean interactive) {
        this.status = status;
        this.interactive = interactive;
    }

    public static final String REPORT_ACK_INTERACTIVE = "Your report is passed to the author. The run waits for their next message; do not call anything else.";
    public static final String REPORT_ACK = "The run ends after this round. Your report is passed to the author.";
    public static final String COMPACTION_ACK = "The earlier rounds are summarized when this round ends. You will write the summary then; what you need afterwards must be in your workspace or your plan.";
    public static final String QUESTION_ACK = "The question is passed to the author. The run waits here; the answer comes as the next message.";

    /**
     * Whether a result of a call is the plain acknowledgement of {@code finish} or {@code ask_user}, which the author
     * reads as the report or the question itself and not as a result.
     */
    public static boolean isAcknowledgement(String result) {
        return REPORT_ACK_INTERACTIVE.equals(result) || REPORT_ACK.equals(result) || QUESTION_ACK.equals(result);
    }

    /**
     * The question of {@code ask_user} as the author reads it: the text and the answers suggested.
     */
    public static String questionText(String question, List<String> options) {
        StringBuilder text = new StringBuilder(StringUtils.abbreviate(question.strip(), MAX_QUESTION_LENGTH));
        if (options != null && !options.isEmpty()) {
            text.append("\n\nSuggested answers:");
            options.stream().filter(StringUtils::isNotBlank).limit(MAX_OPTIONS)
                    .forEach(option -> text.append("\n- ").append(StringUtils.abbreviate(option.strip(), MAX_ITEM_LENGTH)));
        }
        return text.toString();
    }

    /**
     * The report of the model when it called {@code finish}, null when it did not.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * The question of the model when it called {@code ask_user}, null when it did not.
     */
    public String getQuestion() {
        return question;
    }

    /**
     * Whether the model called {@code request_compaction} in this round.
     */
    public boolean isCompactionRequested() {
        return compaction != null;
    }

    /**
     * What the summary should stress, as the model wrote it; empty when it did not say or no compaction was requested.
     */
    public String getCompactionFocus() {
        return StringUtils.defaultString(compaction);
    }

    /**
     * The runner takes the notes of the round.
     */
    public void clearRequests() {
        summary = null;
        question = null;
        compaction = null;
    }

    public List<PlanItem> getPlan() {
        return plan;
    }

    /**
     * Called with the whole plan after every {@code update_plan} that was accepted, for the runner to keep it where a restart
     * finds it. A restored plan is not stored again.
     */
    public void onPlanChange(Consumer<List<PlanItem>> store) {
        this.planStore = store == null ? items -> {
        } : store;
    }

    /**
     * The plan the run had before it was paused (kept by {@link #onPlanChange}, or read back from the transcript).
     */
    public void restorePlan(List<PlanItem> items) {
        this.plan = items == null ? List.of() : List.copyOf(items);
    }

    public List<Finding> getFindings() {
        return findings;
    }

    /**
     * Called with all the findings after every {@code report} that was accepted, for the runner to keep them in the folder of the run.
     * Restored findings are not stored again.
     */
    public void onFindingsChange(Consumer<List<Finding>> store) {
        this.findingsStore = store == null ? items -> {
        } : store;
    }

    /**
     * The findings the run had before it was paused (kept by {@link #onFindingsChange}).
     */
    public void restoreFindings(List<Finding> items) {
        this.findings = items == null ? List.of() : List.copyOf(items);
    }

    @Override
    public synchronized void report(FunctionContinuation<String> done, String text, String severity) {
        if (StringUtils.isBlank(text)) {
            done.fail("Write the finding in text: what you found, where, and why it matters");
            return;
        }
        if (findings.size() >= AgentFindings.MAX_FINDINGS) {
            done.fail("You reported " + findings.size() + " findings, the most there can be. Put the rest in your final report or in a file");
            return;
        }
        List<Finding> all = new ArrayList<>(findings);
        all.add(new Finding(all.size() + 1, AgentFindings.severityOf(severity),
                StringUtils.abbreviate(text.strip(), AgentFindings.MAX_TEXT_LENGTH), Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()));
        this.findings = List.copyOf(all);
        findingsStore.accept(findings);
        done.complete("Finding " + all.size() + " recorded for the author. Go on with your task.");
    }

    @Override
    public void finish(FunctionContinuation<String> done, String summary) {
        if (StringUtils.isBlank(summary)) {
            done.fail("Write your report in summary: what you did, found and changed");
            return;
        }
        this.summary = summary.strip();
        done.complete(interactive ? REPORT_ACK_INTERACTIVE : REPORT_ACK);
    }

    @Override
    public void askUser(FunctionContinuation<String> done, String question, List<String> options) {
        if (StringUtils.isBlank(question)) {
            done.fail("Write the question");
            return;
        }
        this.question = questionText(question, options);
        done.complete(QUESTION_ACK);
    }

    @Override
    public void updatePlan(FunctionContinuation<String> done, List<PlanItem> items) {
        if (items == null || items.isEmpty()) {
            done.fail("The plan needs at least one step");
            return;
        }
        if (items.size() > MAX_PLAN_ITEMS) {
            done.fail("The plan has " + items.size() + " steps, keep it to " + MAX_PLAN_ITEMS + " or fewer");
            return;
        }
        List<PlanItem> cleaned = new ArrayList<>();
        for (PlanItem item : items) {
            if (item == null || StringUtils.isBlank(item.text())) {
                done.fail("Every step needs a text");
                return;
            }
            cleaned.add(new PlanItem(StringUtils.abbreviate(item.text().strip(), MAX_ITEM_LENGTH), statusOf(item.status())));
        }
        this.plan = List.copyOf(cleaned);
        planStore.accept(plan);
        done.complete("Plan saved.\n" + render(cleaned));
    }

    @Override
    public void requestCompaction(FunctionContinuation<String> done, String focus) {
        this.compaction = StringUtils.abbreviate(StringUtils.defaultString(focus).strip(), MAX_QUESTION_LENGTH);
        done.complete(COMPACTION_ACK);
    }

    @Override
    public void getRunStatus(FunctionContinuation<String> done) {
        String text = status.get();
        if (!plan.isEmpty()) {
            text += "\n\nPlan:\n" + render(plan);
        }
        if (!findings.isEmpty()) {
            text += "\n\nFindings you reported:\n" + AgentFindings.render(findings, 120);
        }
        done.complete(text);
    }

    /**
     * Statuses written the way the models write them ({@code in progress}, {@code completed}) map to the three that
     * exist; an unknown one is a step still to do.
     */
    static String statusOf(String status) {
        String normal = status == null ? "" : status.strip().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        return switch (normal) {
            case "done", "completed", "complete", "finished" -> "done";
            case "in_progress", "doing", "started", "active", "working" -> "in_progress";
            default -> "pending";
        };
    }

    public static String render(List<PlanItem> items) {
        StringBuilder text = new StringBuilder();
        int number = 1;
        for (PlanItem item : items) {
            text.append(number++).append(". [").append(switch (item.status()) {
                case "done" -> "x";
                case "in_progress" -> "~";
                default -> " ";
            }).append("] ").append(item.text()).append('\n');
        }
        return text.toString().stripTrailing();
    }
}
