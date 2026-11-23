package com.github.enerccio.marginalia.bound;

import com.github.enerccio.marginalia.domain.security.model.User;

/**
 * The request context of an agent run: what the session scoped beans ({@code user}, {@code sessionPoint}) resolve
 * against on the threads of a run, where there is no browser, no servlet request and no session of the container. It is
 * made from the database at every start and resume (the run stores only its {@link AgentSession}), keeps its session
 * scope in memory and is shared by all the threads of the run: {@code ThreadCopyRequestAttributes.create()} hands it on
 * as it is, so the commands, the function calls and the generation worker of the run all see the same user and
 * session. See {@link DetachedRequestAttributes}.
 * <p>
 * The session scope starts with the {@code user} (a copy of the user of the run, as the login makes it) and the
 * {@code sessionPoint} (an {@link AgenticSessionPoint} with the language of the session). Any other session bean is
 * created on first use, empty, as in a new browser session.
 */
public final class AgentRequestAttributes extends DetachedRequestAttributes {

    private final AgentSession session;

    /**
     * @param session      what the run keeps of its session
     * @param user         the user of the run, loaded from the database; the {@code user} bean is a copy of it (the
     *                     login makes the same copy, nothing else of the entity is wanted in a session)
     * @param sessionPoint where the commands find the book, see {@link AgenticSessionPoint}; gets the language of the
     *                     session
     */
    public AgentRequestAttributes(AgentSession session, User user, AgenticSessionPoint sessionPoint) {
        super("agent", checked(session, user, sessionPoint), sessionPoint);
        this.session = session;
        sessionPoint.setLanguage(session.language());
        // what the agent reads, the built-in prompts and the notices of the run included, is in the language of the agent
        sessionPoint.setPromptLanguage(session.language());
    }

    private static User checked(AgentSession session, User user, AgenticSessionPoint sessionPoint) {
        if (session == null || user == null || sessionPoint == null) {
            throw new IllegalArgumentException("The session, the user and the session point are needed");
        }
        if (!session.userUuid().equals(user.getUuid())) {
            throw new IllegalArgumentException("The user is not the user of the session");
        }
        return user;
    }

    public AgentSession getSession() {
        return session;
    }
}
