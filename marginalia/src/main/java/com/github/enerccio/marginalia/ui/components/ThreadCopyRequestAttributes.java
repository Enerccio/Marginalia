package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.bound.AgentRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;

public class ThreadCopyRequestAttributes extends ServletRequestAttributes {

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
	}

	@Override
	protected void updateAccessedSessionAttributes() {
		// fake copy, do nothing
	}

	/**
	 * The request context of the current thread in a form another thread can run in (see {@link InRequestScope}), null
	 * when this thread has none. Whoever starts a worker thread takes it before, on the thread that has the context.
	 * <p>
	 * A browser session is copied (the copy outlives the request); the attributes of an agent run are not bound to a
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