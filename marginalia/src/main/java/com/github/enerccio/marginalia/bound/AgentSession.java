package com.github.enerccio.marginalia.bound;

import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.loc.Language;

/**
 * What a run keeps of the session it works in, the rest of a session (the workspace, the open book window, the help
 * chat) is made up again at every start and resume, see {@link AgentRequestAttributes}.
 *
 * @param userUuid uuid of the user the run works as, the owner of the run
 * @param language language of the texts the agent sees (results and errors of commands), the language of the book
 *                 and not the one of the interface of the user
 */
public record AgentSession(String userUuid, Language language) {

    public AgentSession {
        if (userUuid == null || userUuid.isBlank()) {
            throw new IllegalArgumentException("The session needs a user");
        }
        if (language == null) {
            language = Language.EN;
        }
    }

    /**
     * The session of a run: its owner, and its language (a book language without a bundle shows the texts in the
     * default language).
     */
    public static AgentSession of(AgentRun run) {
        if (run == null || run.getOwner() == null) {
            throw new IllegalArgumentException("The run has no owner");
        }
        return new AgentSession(run.getOwner().getUuid(), Language.of(run.getLanguage().getCode()));
    }
}
