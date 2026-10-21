package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.traits.BatchPolicy;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.domain.traits.LLMParam;

import java.util.List;

/**
 * What an agent's model can do about its own run: report that it is done, ask the author, keep a to-do list and look
 * at how much of its budget is left. They only make sense inside a run, so the runner binds them (see
 * {@link RunToolsImpl}) next to the tools made from the commands and the file tools.
 */
public interface RunTools {

    /**
     * One line of the to-do list of {@link #updatePlan}.
     *
     * @param text   what is to be done
     * @param status {@code pending}, {@code in_progress} or {@code done}
     */
    record PlanItem(String text, String status) {
    }

    /**
     * One finding of {@link #report}.
     *
     * @param number   the place in the list of the run, from 1
     * @param severity {@code note}, {@code warning} or {@code problem}
     * @param text     what the agent found
     * @param time     when it was reported (ISO, UTC), empty when not known
     */
    record Finding(int number, String severity, String text, String time) {
    }

    @LLMFunction(name = "finish", description = "Ends your work when the task is done. Call it once, with your report "
            + "for the author: what you did, what you found, what you changed and what the author should check. "
            + "Nothing is done after this call, so finish your notes and checks first.")
    void finish(FunctionContinuation<String> done,
                @LLMParam(description = "Your report for the author") String summary);

    @LLMFunction(name = "report", description = "Records a finding for the author and goes on: something you found that "
            + "the author should know about (a contradiction, an error, a doubt, a thing you could not decide). The author reads "
            + "the findings in the console and in the review of the run, apart from your final report. It does not stop you and "
            + "asks nothing. One finding per call, complete enough to be understood without the rest of your work: say where it is "
            + "(the number of the part, the name of the entry) and quote the words it is about. Report each finding once; "
            + "`get_run_status` lists the ones you made.")
    void report(FunctionContinuation<String> done,
                @LLMParam(description = "The finding: what, where and why it matters") String text,
                @LLMParam(description = "note (default), warning or problem", required = false) String severity);

    @LLMFunction(name = "ask_user", description = "Asks the author a question and waits for the answer. Use it only "
            + "when the answer is the author's decision and you can not find it out yourself with your tools. The run "
            + "stops here until the author answers; the answer comes as the next message.")
    void askUser(FunctionContinuation<String> done,
                 @LLMParam(description = "The question, complete enough to be answered without any other context") String question,
                 @LLMParam(description = "Answers you suggest, if the question has a few obvious ones",
                         required = false) List<String> options);

    @LLMFunction(name = "update_plan", description = "Writes your to-do list, the whole list each time: the steps of "
            + "the task, each with its status (pending, in_progress or done). The author sees it, and it stays when "
            + "the run is paused and continued. Keep it short, update it when a step starts and when it is done.")
    void updatePlan(FunctionContinuation<String> done,
                    @LLMParam(description = "All the steps, in order; each has text and status") List<PlanItem> items);

    @LLMFunction(name = "request_compaction", batch = BatchPolicy.SINGLE, description = "Frees your context: when this round ends, "
            + "everything you did so far is replaced by a summary that you write then. Use it when the run tells you the context is "
            + "filling up, or before you start a new stage of the task. Save your notes to a file of your workspace and update your "
            + "plan first, in the answers before this one; what is in neither is only in the summary if you write it there. Call it in "
            + "an answer of its own.")
    void requestCompaction(FunctionContinuation<String> done,
                           @LLMParam(description = "What the summary should stress, e.g. the stage you start next",
                                   required = false) String focus);

    @LLMFunction(name = "get_run_status", description = "Tells how much of your budget is spent: rounds, tokens, the "
            + "context of the model, and your current plan. Look at it before a long step to pace yourself.")
    void getRunStatus(FunctionContinuation<String> done);

}
