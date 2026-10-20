package com.github.enerccio.marginalia.domain.collections;

/**
 * How many calls of one answer of an agent are made, the setting of the agent ({@code Agent.batchMode}). An error ends the answer in
 * every mode: the calls after the failed one are not made.
 */
public enum AgentBatchMode {

    /**
     * All the calls of an answer are made, whatever the policies of the commands say ({@code BatchPolicy}).
     */
    BATCHED_IGNORE_POLICIES,

    /**
     * The calls of an answer are made together, except that a command whose policy is {@code SINGLE} is made alone. The default.
     */
    BATCHED,

    /**
     * Only the first call of an answer is made: the agent writes the next one after it has read the result.
     */
    NOT_BATCHED

}
