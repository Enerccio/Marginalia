package com.github.enerccio.marginalia.ui.components;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;

public class ThreadCopyRequestAttributes extends ServletRequestAttributes {

	public ThreadCopyRequestAttributes(HttpServletRequest request, HttpServletResponse response) {
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
	
}