package com.github.enerccio.marginalia.test;

import com.github.enerccio.marginalia.bound.AgenticSessionPoint;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * The request attributes of a test, as an agent run would have them: everything is the one of the logged in test
 * session (the user, the other session beans), except the {@code sessionPoint}, which is the given
 * {@link AgenticSessionPoint}: no workspace, no window, the book is held directly.
 */
public final class AgenticRequestAttributes implements RequestAttributes, AutoCloseable {

    private final RequestAttributes delegate;
    private final AgenticSessionPoint sessionPoint;

    private AgenticRequestAttributes(RequestAttributes delegate, AgenticSessionPoint sessionPoint) {
        this.delegate = delegate;
        this.sessionPoint = sessionPoint;
    }

    /**
     * Makes the current thread an agent session until it is closed.
     */
    public static AgenticRequestAttributes install(AgenticSessionPoint sessionPoint) {
        AgenticRequestAttributes attributes = new AgenticRequestAttributes(RequestContextHolder.getRequestAttributes(), sessionPoint);
        RequestContextHolder.setRequestAttributes(attributes);
        return attributes;
    }

    @Override
    public void close() {
        RequestContextHolder.setRequestAttributes(delegate);
    }

    @Override
    public Object getAttribute(String name, int scope) {
        if (scope == SCOPE_SESSION && AgenticSessionPoint.SCOPED_TARGET_NAME.equals(name)) {
            return sessionPoint;
        }
        return delegate.getAttribute(name, scope);
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {
        delegate.setAttribute(name, value, scope);
    }

    @Override
    public void removeAttribute(String name, int scope) {
        delegate.removeAttribute(name, scope);
    }

    @Override
    public String[] getAttributeNames(int scope) {
        return delegate.getAttributeNames(scope);
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback, int scope) {
        delegate.registerDestructionCallback(name, callback, scope);
    }

    @Override
    public Object resolveReference(String key) {
        return delegate.resolveReference(key);
    }

    @Override
    public String getSessionId() {
        return delegate.getSessionId();
    }

    @Override
    public Object getSessionMutex() {
        return delegate.getSessionMutex();
    }
}
