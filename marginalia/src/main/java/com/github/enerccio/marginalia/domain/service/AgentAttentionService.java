package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;

import java.util.function.Consumer;

/**
 * Tells the open screens of a user that a run of an agent of theirs changed, so the table of agents is up to date on
 * every screen (a stop or a pause ends some time after the button was pressed, when the worker gets to a stable
 * point), and that an agent of theirs needs them: it asks a question or for the approval of a
 * command, waits for the next message, finished, failed, or paused for a reason of its own. The runs work while the
 * user is away, so the state is stored with the run ({@link AgentRun#isAttention()}, read again by every screen that
 * opens); this is only the push to the screens that are open now. Every UI of the user (several browsers, several tabs)
 * subscribes on its own.
 */
public interface AgentAttentionService {

    /**
     * What changed for a run.
     *
     * @param userId    the owner of the run
     * @param attention whether the run waits to be looked at now (the screens tell the user); false for a change that
     *                  needs nobody, and when the user did look at it (so the other screens of the user count again)
     */
    record Notice(Long userId, String runUuid, String agentUuid, String agentName, AgentRunStatus status, String reason,
                  boolean attention) {
    }

    /**
     * Calls the listener, on whatever thread changes the run, for every change of a run of the user.
     *
     * @return cancels the subscription
     */
    Runnable subscribe(Long userId, Consumer<Notice> listener);

    /**
     * Passes the changed run on to the listeners of its owner.
     */
    void publish(AgentRun run);

}
