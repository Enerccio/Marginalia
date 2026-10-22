package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.command.CommandScope;

/**
 * What the commands an agent calls are kept in: the {@link CommandScope} they run in (the working copy of the run, the
 * questions answered for the agent) and the {@link HandleTranslator} the model's arguments and the results of the commands
 * pass through.
 */
public record CommandCage(CommandScope scope, HandleTranslator handles) {

    public CommandCage {
        handles = handles == null ? HandleTranslator.NONE : handles;
    }
}
