package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.domain.traits.LLMParam;

/**
 * Starting a sub-agent: a helper that works on one task with its own conversation, in the same working copy and workspace as
 * the agent, and answers with a text. Offered only to an agent that may have sub-agents, and never to a sub-agent. The runner
 * binds it (see {@link SubAgentToolsImpl}).
 */
public interface SubAgentTools {

    @LLMFunction(name = "spawn_agent", description = "Starts a sub-agent that does one task on its own and answers with a text: "
            + "it has the same commands and files as you (not run control, checkpoints or spawning), works in the same book and "
            + "workspace, and does not know your conversation, so the task must say everything it needs. Use it for a job that "
            + "can be done separately (check a chapter, audit the names, draft a part) so that its reading and trying stay out of "
            + "your context. The limit of sub-agents working at once is in your instructions; when this call reaches it, the call "
            + "waits until one ends and then returns what ended. Do not start two sub-agents that change the same part.")
    void spawn(FunctionContinuation<String> done,
               @LLMParam(description = "What the sub-agent has to do and what its answer should contain, complete without any other context") String task,
               @LLMParam(description = "Material it needs: handles, paths of files in your workspace, facts you found",
                       required = false) String context);
}
