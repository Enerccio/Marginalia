package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.agent.AgentContext;

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
     * The command of the console the runner does itself: {@code /compact [focus]} replaces the transcript the model reads
     * by a summary the model writes, see {@link #command}.
     */
    String COMPACT = "compact";

    /**
     * The other command of the console the runner does itself: {@code /clear-workspace [what to keep]} deletes the files
     * and folders of the workspace of the run except the paths and patterns named, see {@link #command}.
     */
    String CLEAR_WORKSPACE = "clear-workspace";

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
     * A message or a command of the author that waits for the agent to be ready, see {@link #send(AgentRun, String, List)}
     * and {@link #command}.
     *
     * @param id      names the message for {@link #force}
     * @param text    what the message says, with the note about its images; for a command the line that will be run
     * @param command whether this is a command (its line is the text), not a message
     */
    record QueuedMessage(String id, String text, boolean command) {

        public QueuedMessage(String id, String text) {
            this(id, text, false);
        }
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
     * The commands the author can run on the agent from the console: {@link #COMPACT}, {@link #CLEAR_WORKSPACE} and the commands the agent is
     * allowed that only read or add data. A command that changes or removes data is not offered: it asks the user to
     * confirm in a window of the book, which a run does not have.
     *
     * @return the command names, {@code compact} and {@code clear-workspace} first and then the others by name
     */
    List<String> consoleCommands(Agent agent);

    /**
     * Runs a command as the author, not as the agent: {@code /compact [focus]} or {@code /name arguments} of one of the
     * {@link #consoleCommands}, in the working copy of the run. The agent does not make the call: it reads, as a message of
     * the author, that the author ran the command and what it answered (its tool calls are its own), and a command that
     * changes the copy tells it that what it read may be out of date. {@code /compact} asks the model of the agent, with
     * its compaction prompt ({@code AgentService#getCompactionPrompt}) and the optional focus, to summarize everything so
     * far; the summary becomes the start of what the model reads, the rest stays in the transcript of the user marked as
     * compacted.
     * <p>
     * Where the run waits ({@code IDLE}) or is paused the command runs at once in the background, and the run stays as it
     * is. Where a conversation works, the command is queued like a message of the author and runs at the end of the
     * turn of the agent, where the agent would have waited (the agent then goes on to wait, it is not asked to answer);
     * {@link #force} runs it now and the agent goes on from there. The queue is in memory.
     *
     * @param line the command as typed, with the slash
     * @return the run in its status when the command was queued or started
     * @throws IllegalArgumentException when the line is not a command of {@link #consoleCommands}
     * @throws IllegalStateException    when the run is working and is not a conversation, a command is running already, or
     *                                  the run is in a status in which nothing can be done to it (stopped, done)
     */
    AgentRun command(AgentRun run, String line) throws Exception;

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
     * Takes a queued message or command back: it is not given to the agent. Does nothing when it is not queued any more
     * (it was delivered meanwhile).
     *
     * @param id the {@link QueuedMessage#id()}
     * @return whether it was queued and is gone now
     */
    boolean cancelQueued(AgentRun run, String id) throws Exception;

    /**
     * Pauses the run at once: as {@link AgentRunService#pause}, but the request to the model that is going on is canceled (its
     * answer is thrown away and not paid for beyond what the provider counted) and so is the call that is going on (it gets the
     * signal to stop; a command cannot always be killed, so the call is answered as cut off and the agent checks the state
     * when it goes on), the calls of the round that did not run are answered as not made, and the worker pauses. A normal
     * pause waits for all of that to finish. For a run that does not work it is the same as a normal pause.
     *
     * @return the run as it is after the request ({@code PAUSING} while the worker still ends, else {@code PAUSED})
     * @throws IllegalStateException when the run is in a status that can't be paused
     */
    AgentRun pauseNow(AgentRun run) throws Exception;

    /**
     * What the model of the run reads with its next request, taken apart for the user to look at and to debug a run: the
     * sections of the system message, the definition of every tool and every message of the conversation, each with its
     * text and its tokens (estimated). The system message and the tools are made as the next request makes them (a changed
     * prompt or allowlist shows at once), the messages are the transcript as it is stored, without what a resume adds
     * ("go on", the answers to calls an interruption left open). The definitions of the tools are left out, with a note, when
     * they can't be made (the book of the run is gone). Nothing is written and no worker is started, it works while the run does.
     *
     * @throws IllegalArgumentException when the run is not a run of the current user
     */
    AgentContext context(AgentRun run) throws Exception;

    /**
     * Whether a worker is busy with the run in this server.
     */
    boolean isWorking(AgentRun run);

}
