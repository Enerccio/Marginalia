package com.github.enerccio.marginalia.extensions.knowledgetracker.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.extensions.knowledgetracker.block.KnowledgeBlockBuilder;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * The block as the generation needs it: what to insert for the turn, and what it costs in tokens.
 */
@Configurable
public class KnowledgePromptService {

    /** Buffer for the headers of the inserted message, like the Author's Note adds. */
    private static final long MESSAGE_BUFFER_TOKENS = 100;

    @Autowired
    private InferenceServices inferenceServices;

    private final KnowledgeStateService stateService;

    public KnowledgePromptService(KnowledgeStateService stateService) {
        this.stateService = stateService;
    }

    /**
     * What a generation inserts for the turn: the block for its present characters, with the settings it was built with.
     * Empty text when the plugin does not insert (switched off, no tracked character is present).
     */
    public Prepared prepare(Manuscript manuscript, String presentCharacters) throws Exception {
        KnowledgeState state = stateService.load(manuscript);
        if (!state.getSettings().isInPrompt()) {
            return new Prepared("", state);
        }
        String block = KnowledgeBlockBuilder.build(state, presentCharacters,
                KnowledgeBlockBuilder.effectiveSeparators(state, stateService.getUserSeparators()));
        return new Prepared(block, state);
    }

    /**
     * Tokens the block takes in the prompt of the model {@code ai} that generates, 0 when there is none.
     */
    public long countTokens(AI ai, String block) throws Exception {
        if (block == null || block.isBlank()) {
            return 0;
        }
        return inferenceServices.forAI(ai).countTokens(block.strip()) + MESSAGE_BUFFER_TOKENS;
    }

    /**
     * @param block the text to insert, may be empty
     * @param state the state the block was made from (its depth and role are used for the insertion)
     */
    public record Prepared(String block, KnowledgeState state) {
    }
}
