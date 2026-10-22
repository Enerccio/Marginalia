package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;

/**
 * What an agent's model can ask about the changes the user made to the book while its run was paused. The runner binds it (see
 * {@link SyncToolsImpl}) next to the run tools.
 */
public interface SyncTools {

    @LLMFunction(name = "get_sync_report", description = "Tells again what the user changed in the book while your run was paused: "
            + "which of their changes are in your working copy now, and which were not taken over because you changed the same thing "
            + "(your version is kept then, the user decides when they review your result). The run tells you this itself when it "
            + "goes on; ask when you need the list again, for example after your context was compacted.")
    void getSyncReport(FunctionContinuation<String> done);
}
