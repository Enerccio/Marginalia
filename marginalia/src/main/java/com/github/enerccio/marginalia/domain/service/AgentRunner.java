package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.AgentRun;

import java.util.List;

/**
 * Works the runs of the agents: owns the loop of a run, one round at a time. A round is one request to the model and
 * the calls it asked for; every round is written to the transcript ({@link AgentMessageService}), so a run can be
 * paused, stopped and continued, days later or after a restart of the server.
 * <p>
 * The run goes {@code RUNNING} while a worker is busy with it and ends the turn in {@code IDLE} (the model answered
 * without a call, or asked the author), {@code DONE} (the model called {@code finish}), {@code PAUSED} (a pause was
 * asked, a budget or the context is used up, the model could not be asked), {@code STOPPED_*} (a stop was asked) or
 * {@code FAILED} (the runner itself broke). The worker leaves the pause and the stop to the next stable point, between
 * two rounds.
 * <p>
 * The tools of the agent are the commands it is allowed, its file tools and {@code finish}, {@code ask_user},
 * {@code update_plan} and {@code get_run_status}.
 */
public interface AgentRunner {

    /**
     * The most images one message of the author may carry.
     */
    int MAX_ATTACHED_IMAGES = 8;

    /**
     * Takes the run up: a worker works on it in the background until the run leaves {@code RUNNING}. Call it after
     * {@link AgentRunService#start} and {@link AgentRunService#resume}, and after {@link #send}.
     *
     * @throws IllegalStateException when the run is not {@code RUNNING}
     */
    void run(AgentRun run);

    /**
     * Takes up a run that has not worked yet, with this as its first message instead of the usual one: a run the
     * author starts from the console, where the agent is to ask the author what they want.
     *
     * @throws IllegalStateException when the run is not {@code RUNNING} or already has a transcript
     */
    void run(AgentRun run, String firstMessage) throws Exception;

    /**
     * Writes to the agent: a message of the author to a run that waits for it ({@code IDLE}, the model ended its turn
     * or asked a question). The run goes {@code RUNNING} again and a worker takes it up.
     *
     * @return the run in its new status
     * @throws IllegalStateException when the run does not wait for the author
     */
    default AgentRun send(AgentRun run, String text) throws Exception {
        return send(run, text, List.of());
    }

    /**
     * As {@link #send(AgentRun, String)}, with images of the agent's workspace attached to the message (the author put
     * them there, see {@code AgentVirtualFileSystemService}). The model is sent the images with the message; the message
     * of the transcript keeps the paths only. The text may be empty when there are images.
     *
     * @param images paths of image files of the workspace, in order
     * @return the run in its new status
     * @throws IllegalStateException    when the run does not wait for the author
     * @throws IllegalArgumentException when there is neither text nor an image, a path is not an image of the workspace,
     *                                  there are more than {@link #MAX_ATTACHED_IMAGES}, or the model of the agent does not
     *                                  see images ({@code AgentModelProfile#isImages})
     */
    AgentRun send(AgentRun run, String text, List<String> images) throws Exception;

    /**
     * Whether a worker is busy with the run in this server.
     */
    boolean isWorking(AgentRun run);

}
