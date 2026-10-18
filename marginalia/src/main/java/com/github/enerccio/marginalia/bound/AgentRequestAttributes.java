package com.github.enerccio.marginalia.bound;

import com.github.enerccio.marginalia.domain.security.model.User;
import org.springframework.web.context.request.RequestAttributes;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The request context of an agent run: what the session scoped beans ({@code user}, {@code sessionPoint}) resolve
 * against on the threads of a run, where there is no browser, no servlet request and no session of the container. It is
 * made from the database at every start and resume (the run stores only its {@link AgentSession}), keeps its session
 * scope in memory and is shared by all the threads of the run: {@code ThreadCopyRequestAttributes.create()} hands it on
 * as it is, so the commands, the function calls and the generation worker of the run all see the same user and
 * session.
 * <p>
 * The session scope starts with the {@code user} (a copy of the user of the run, as the login makes it) and the
 * {@code sessionPoint} (an {@link AgenticSessionPoint} with the language of the session). Any other session bean is
 * created on first use, empty, as in a new browser session.
 */
public final class AgentRequestAttributes implements RequestAttributes, AutoCloseable {

    /**
     * Name of the session scoped attribute that holds the {@link User}: the name of the target bean behind the scoped
     * proxy of {@code user}.
     */
    public static final String USER_TARGET_NAME = "scopedTarget.user";

    private final AgentSession session;
    private final String sessionId = "agent-" + UUID.randomUUID();
    private final Object mutex = new Object();
    private final Map<String, Object> sessionAttributes = new ConcurrentHashMap<>();
    private final Map<String, Object> requestAttributes = new ConcurrentHashMap<>();
    private final Map<String, Runnable> sessionCallbacks = new ConcurrentHashMap<>();
    private final Map<String, Runnable> requestCallbacks = new ConcurrentHashMap<>();

    /**
     * @param session      what the run keeps of its session
     * @param user         the user of the run, loaded from the database; the {@code user} bean is a copy of it (the
     *                     login makes the same copy, nothing else of the entity is wanted in a session)
     * @param sessionPoint where the commands find the book, see {@link AgenticSessionPoint}; gets the language of the
     *                     session
     */
    public AgentRequestAttributes(AgentSession session, User user, AgenticSessionPoint sessionPoint) {
        if (session == null || user == null || sessionPoint == null) {
            throw new IllegalArgumentException("The session, the user and the session point are needed");
        }
        if (!session.userUuid().equals(user.getUuid())) {
            throw new IllegalArgumentException("The user is not the user of the session");
        }
        this.session = session;
        User snapshot = new User();
        snapshot.setId(user.getId());
        snapshot.setUuid(user.getUuid());
        snapshot.setLogin(user.getLogin());
        snapshot.setFullName(user.getFullName());
        snapshot.setAdmin(user.isAdmin());
        sessionPoint.setLanguage(session.language());
        sessionAttributes.put(USER_TARGET_NAME, snapshot);
        sessionAttributes.put(AgenticSessionPoint.SCOPED_TARGET_NAME, sessionPoint);
    }

    public AgentSession getSession() {
        return session;
    }

    /**
     * Runs the destruction callbacks of the beans of the session (those that have any), when the run ends. The user
     * and the session point stay: work of the run that is still in the air (the counting of the tokens of its last
     * request is asynchronous) resolves them after the run is over, and an empty user would own what it saves.
     */
    @Override
    public void close() {
        run(requestCallbacks);
        run(sessionCallbacks);
        requestAttributes.clear();
        sessionAttributes.keySet().removeIf(name -> !name.equals(USER_TARGET_NAME) && !name.equals(AgenticSessionPoint.SCOPED_TARGET_NAME));
    }

    private static void run(Map<String, Runnable> callbacks) {
        callbacks.values().forEach(Runnable::run);
        callbacks.clear();
    }

    private Map<String, Object> attributes(int scope) {
        return scope == SCOPE_SESSION ? sessionAttributes : requestAttributes;
    }

    @Override
    public Object getAttribute(String name, int scope) {
        return attributes(scope).get(name);
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {
        attributes(scope).put(name, value);
    }

    @Override
    public void removeAttribute(String name, int scope) {
        attributes(scope).remove(name);
        (scope == SCOPE_SESSION ? sessionCallbacks : requestCallbacks).remove(name);
    }

    @Override
    public String[] getAttributeNames(int scope) {
        return attributes(scope).keySet().toArray(new String[0]);
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback, int scope) {
        (scope == SCOPE_SESSION ? sessionCallbacks : requestCallbacks).put(name, callback);
    }

    @Override
    public Object resolveReference(String key) {
        return null;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public Object getSessionMutex() {
        return mutex;
    }
}
