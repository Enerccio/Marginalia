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
     * A message of the author that waits for the agent to be ready to read it, see {@link #send(AgentRun, String, List)}.
     *
     * @param id   names the message for {@link #force}
     * @param text what the message says, with the note about its images
     */
    record QueuedMessage(String id, String text) {
    }

    /**
     * Writes to the agent: a message of the author to a run that waits for it ({@code IDLE}, the model ended its turn
     * or asked a question). The run goes {@code RUNNING} again and a worker takes it up.
     * <p>
     * A run that is a conversation can be written to while it works ({@code RUNNING}, {@code WAITING_APPROVAL},
     * {@code PAUSING}) too: the message is queued, and the worker gives it to the model at the end of the turn, where
     * it would have waited for the author, so the run does not go {@code IDLE} (see {@link #queued}, {@link #force}).
     *
     * @return the run in its new status, as it was when the message was queued
     * @throws IllegalStateException when the run does not wait for the author and is not a conversation that works
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
     * @throws IllegalStateException    when the run does not wait for the author and is not a conversation that works
     * @throws IllegalArgumentException when there is neither text nor an image, a path is not an image of the workspace,
     *                                  there are more than {@link #MAX_ATTACHED_IMAGES}, or the model of the agent does not
     *                                  see images ({@code AgentModelProfile#isImages})
     */
    AgentRun send(AgentRun run, String text, List<String> images) throws Exception;

    /**
     * The messages the author wrote while the run worked, that wait for the end of its turn, the oldest first. They are
     * kept in memory: a restart of the server forgets them, as it forgets the approvals; a run that is stopped or
     * finished drops them.
     */
    List<QueuedMessage> queued(AgentRun run);

    /**
     * Gives a queued message to the agent now: the round the worker is in is thrown away (the request to the model is
     * canceled, the calls of the round that did not run are answered as skipped, a command that waited for an approval is
     * refused) and the message, with the ones queued before it, is the next thing the model reads. The thrown away part
     * of the round that was already written to the transcript, the calls and their results, stays in it so that the
     * model knows. Where the run is {@code IDLE} meanwhile, the messages are sent as usual.
     * <p>
     * Does nothing when the message is not queued any more (it was delivered meanwhile).
     *
     * @param id the {@link QueuedMessage#id()}
     * @throws IllegalStateException when the run is not working ({@code RUNNING}, {@code WAITING_APPROVAL}) or waiting for
     *                               the author, a pause comes first
     */
    void force(AgentRun run, String id) throws Exception;

    /**
     * Whether a worker is busy with the run in this server.
     */
    boolean isWorking(AgentRun run);

}
