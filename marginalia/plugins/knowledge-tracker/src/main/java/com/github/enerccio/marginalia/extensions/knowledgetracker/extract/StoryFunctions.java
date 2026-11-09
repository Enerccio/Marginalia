package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.domain.traits.LLMParam;

/**
 * What the model may call while it reads the story for the Knowledge Tracker.
 */
public interface StoryFunctions {

    @LLMFunction(name = "read_part", description = "Reads an earlier part of the story, the text as it was written. Use it when an item says where "
            + "it was learned and you can not tell from the item whether the new messages confirm it, repeat it or contradict it (hearsay, a rumour, "
            + "a plan, a name that may or may not have been given): read the part before you decide. Only parts before the messages can be read.")
    void readPart(FunctionContinuation<String> done,
                  @LLMParam(name = "part", description = "The number of the part, as in 'learned in part N'") int part);
}
