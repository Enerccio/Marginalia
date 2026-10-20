package com.github.enerccio.marginalia.domain.traits;

import com.github.enerccio.marginalia.loc.L;

/**
 * How a {@link Command} may be used among the calls of one answer of an agent (LLM), declared by {@link Command#batch()}. It is
 * advice about use and not a claim about what the command does (that is {@link Command#readOnly()}): the runner of an agent follows
 * it unless the agent is set to ignore the policies, see {@code AgentBatchMode}.
 */
public enum BatchPolicy {

    /** The command may be called together with others in one answer. */
    BATCHABLE(L.ENUM_BATCH_POLICY_BATCHABLE),

    /**
     * The command is called alone in its answer: the calls that come after it are not made, and one that comes after other calls
     * waits for an answer of its own. For a command that is long or costly, as one that makes a call to a model, or one that the
     * agent should see the result of before it goes on.
     */
    SINGLE(L.ENUM_BATCH_POLICY_SINGLE);

    private final L label;

    BatchPolicy(L label) {
        this.label = label;
    }

    /** The short name shown to the user. */
    public L getLabel() {
        return label;
    }

}
