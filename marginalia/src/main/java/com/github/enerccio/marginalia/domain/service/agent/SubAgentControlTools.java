package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.domain.traits.LLMParam;

/**
 * Working with several sub-agents at once: waiting for one, looking at them, stopping one. Offered only to an agent that may
 * have more than one sub-agent working at a time (with one, {@code spawn_agent} waits by itself). The runner binds it (see
 * {@link SubAgentToolsImpl}).
 */
public interface SubAgentControlTools {

    @LLMFunction(name = "await_agent", description = "Waits until a sub-agent is over and returns its answer; at once when it is over "
            + "already. Do your own work first and wait for the answer when you need it.")
    void await(FunctionContinuation<String> done,
               @LLMParam(description = "The handle of the sub-agent, e.g. sub1") String handle,
               @LLMParam(name = "timeout_seconds", description = "Gives up waiting after this many seconds and tells how far the "
                       + "sub-agent is; default waits until it is over", required = false) Integer timeoutSeconds);

    @LLMFunction(name = "agent_status", description = "Tells how your sub-agents are doing: state, rounds, tokens and the last "
            + "thing each did.")
    void status(FunctionContinuation<String> done,
                @LLMParam(description = "One sub-agent, e.g. sub1; default all of them", required = false) String handle);

    @LLMFunction(name = "cancel_agent", description = "Stops a sub-agent at its next step. What it already changed in the book stays.")
    void cancel(FunctionContinuation<String> done,
                @LLMParam(description = "The handle of the sub-agent, e.g. sub1") String handle);
}
