package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.bound.AgentRequestAttributes;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The request context of a page in a form a worker thread can run in. The request is over by the time the worker is done, and so may be the browser
 * session: the last page of a browser that is closed while a model works closes the session (the work of minutes would be lost at the write). So the
 * session scope of this copy is a <b>copy of the attributes of the session</b>, taken when the copy is made: the session-scoped beans the thread needs
 * (the user above all) are in it and go on being found after the session is invalid. A bean the worker puts into the session scope is kept in the copy
 * and, as long as the session is alive, also in the session, so the page sees it.
 */
public class ThreadCopyRequestAttributes extends ServletRequestAttributes {

	/** The session attributes as they were when the copy was made, and what the thread added to them. */
	private final Map<String, Object> sessionCopy = new ConcurrentHashMap<>();
	/** What the page locks for the session (the mutex of the session, or the session itself); the copy locks the same object. */
	private final Object sessionMutex;
	/**
	 * The request attributes, copied too: the servlet container recycles the request when it is over (Tomcat then throws "The request object has
	 * been recycled" from every call on it), and the worker runs long after that.
	 */
	private final Map<String, Object> requestCopy = new ConcurrentHashMap<>();
	/** The context of the application, taken from the request while it is alive ({@link #getServletContext()}). */
	private final ServletContext servletContext;

	private ThreadCopyRequestAttributes(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
		HttpSession s = getSession(true); // store session
		requestDestructionCallbacks.clear();
		requestCompleted();
		try {
			Field f = ServletRequestAttributes.class.getDeclaredField("session");
			f.setAccessible(true);
			f.set(this, s);
		} catch (Exception e) {
			// ignore
		}
		sessionMutex = WebUtils.getSessionMutex(s);
		servletContext = request.getServletContext();
		for (Enumeration<String> names = request.getAttributeNames(); names.hasMoreElements(); ) {
			String name = names.nextElement();
			Object value = request.getAttribute(name);
			if (value != null) {
				requestCopy.put(name, value);
			}
		}
		for (Enumeration<String> names = s.getAttributeNames(); names.hasMoreElements(); ) {
			String name = names.nextElement();
			Object value = s.getAttribute(name);
			if (value != null) {
				sessionCopy.put(name, value);
			}
		}
	}

	@Override
	protected void updateAccessedSessionAttributes() {
		// fake copy, do nothing
	}

	/** The context of the application, for a thread whose request is over (asking the request for it would fail then). */
	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public Object getAttribute(String name, int scope) {
		return (scope == SCOPE_SESSION ? sessionCopy : requestCopy).get(name);
	}

	@Override
	public void setAttribute(String name, Object value, int scope) {
		if (scope != SCOPE_SESSION) {
			if (value == null) {
				requestCopy.remove(name);
			} else {
				requestCopy.put(name, value);
			}
			return;
		}
		sessionCopy.put(name, value);
		try {
			super.setAttribute(name, value, scope);
		} catch (IllegalStateException e) {
			// the session is gone, the copy has it
		}
	}

	@Override
	public void removeAttribute(String name, int scope) {
		if (scope != SCOPE_SESSION) {
			requestCopy.remove(name);
			return;
		}
		sessionCopy.remove(name);
		try {
			super.removeAttribute(name, scope);
		} catch (IllegalStateException e) {
			// the session is gone, the copy is changed
		}
	}

	@Override
	public String[] getAttributeNames(int scope) {
		return (scope == SCOPE_SESSION ? sessionCopy : requestCopy).keySet().toArray(String[]::new);
	}

	@Override
	public Object getSessionMutex() {
		return sessionMutex;
	}

	/**
	 * The request context of the current thread in a form another thread can run in (see {@link InRequestScope}), null
	 * when this thread has none. Whoever starts a worker thread takes it before, on the thread that has the context.
	 * <p>
	 * A browser session is copied (the copy outlives the request and the session); the attributes of an agent run are not bound to a
	 * request and are used by all the threads of the run as they are.
	 */
	public static RequestAttributes create() {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if (attrs instanceof ThreadCopyRequestAttributes || attrs instanceof AgentRequestAttributes) {
			return attrs;
		}
		if (attrs instanceof ServletRequestAttributes servletAttrs) {
			return new ThreadCopyRequestAttributes(servletAttrs.getRequest(), servletAttrs.getResponse());
		}
		return null;
	}

	public static class InRequestScope implements AutoCloseable {

		private final RequestAttributes attributes;
		private final RequestAttributes oldAttributes;

		public InRequestScope(RequestAttributes attributes) {
			this.attributes = attributes;
			oldAttributes = RequestContextHolder.getRequestAttributes();
			if (attributes == null)
				return;
			RequestContextHolder.setRequestAttributes(attributes);
		}

		@Override
		public void close() {
			if (attributes == null)
				return;
			RequestContextHolder.setRequestAttributes(oldAttributes);
		}
	}

}
