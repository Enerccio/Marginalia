package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private volatile List<PlanItem> plan = List.of();

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
     * The runner takes the notes of the round.
     */
    public void clearRequests() {
        summary = null;
        question = null;
    }

    public List<PlanItem> getPlan() {
        return plan;
    }

    /**
     * The plan the run had before it was paused (read back from the transcript).
     */
    public void restorePlan(List<PlanItem> items) {
        this.plan = items == null ? List.of() : List.copyOf(items);
    }

    @Override
    public void finish(FunctionContinuation<String> done, String summary) {
        if (StringUtils.isBlank(summary)) {
            done.fail("Write your report in summary: what you did, found and changed");
            return;
        }
        this.summary = summary.strip();
        done.complete(interactive
                ? "Your report is passed to the author. The run waits for their next message; do not call anything else."
                : "The run ends after this round. Your report is passed to the author.");
    }

    @Override
    public void askUser(FunctionContinuation<String> done, String question, List<String> options) {
        if (StringUtils.isBlank(question)) {
            done.fail("Write the question");
            return;
        }
        StringBuilder text = new StringBuilder(StringUtils.abbreviate(question.strip(), MAX_QUESTION_LENGTH));
        if (options != null && !options.isEmpty()) {
            text.append("\n\nSuggested answers:");
            options.stream().filter(StringUtils::isNotBlank).limit(MAX_OPTIONS)
                    .forEach(option -> text.append("\n- ").append(StringUtils.abbreviate(option.strip(), MAX_ITEM_LENGTH)));
        }
        this.question = text.toString();
        done.complete("The question is passed to the author. The run waits here; the answer comes as the next message.");
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
        done.complete("Plan saved.\n" + render(cleaned));
    }

    @Override
    public void getRunStatus(FunctionContinuation<String> done) {
        String text = status.get();
        done.complete(plan.isEmpty() ? text : text + "\n\nPlan:\n" + render(plan));
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
