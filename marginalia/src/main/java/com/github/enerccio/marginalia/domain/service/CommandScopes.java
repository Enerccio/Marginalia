package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.command.CommandScope;

/**
 * Makes the {@link CommandScope}s commands run in.
 */
public interface CommandScopes {

    /**
     * The scope of the user at the command bar: the book that is open in their session (read again every time it is asked,
     * the user may open another one), asking by a dialog.
     */
    CommandScope session();

    /**
     * The scope of the run of an agent: the working copy of the run, whichever it is at the moment (a resumed run may have
     * a new one), never the book of the user.
     *
     * @throws IllegalArgumentException when the run does not exist
     */
    CommandScope forRun(AgentRun run);

}
