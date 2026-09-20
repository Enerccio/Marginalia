package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public abstract class ThreadAccessDialog extends Dialog {
	private static final Logger log = LoggerFactory.getLogger(ThreadAccessDialog.class);

	private ThreadCopyRequestAttributes attributes;
	protected UI ui;
	private final String threadName;
	protected final boolean push;
	
	public ThreadAccessDialog(String threadName, boolean push) {
		this.threadName = threadName;
		this.push = push;
	}
	
	public void run() {
		ui = UI.getCurrent();
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes(); 
		attributes = new ThreadCopyRequestAttributes(attrs.getRequest(), attrs.getResponse());
		Thread t = new Thread(() -> {
			RequestContextHolder.resetRequestAttributes();
			RequestContextHolder.setRequestAttributes(attributes);

			try {
				runInThread();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}

			RequestContextHolder.resetRequestAttributes();
		});
		t.setName(threadName);
		t.start();
	}

	protected abstract void runInThread() throws Exception;
	
	public void vaadinLocked(Runnable r) {
		ui.access(() -> {
			RequestAttributes old = RequestContextHolder.getRequestAttributes();
			try {
				RequestContextHolder.setRequestAttributes(attributes);
				r.run();
			} finally {
				RequestContextHolder.setRequestAttributes(old);
			}
			if (push) {
				UIPushGuard.push(ui);
			}
		});
	}

	public void vaadinLockedSync(Runnable r) {
		ui.accessSynchronously(() -> {
			RequestAttributes old = RequestContextHolder.getRequestAttributes();
			try {
				RequestContextHolder.setRequestAttributes(attributes);
				r.run();
			} finally {
				RequestContextHolder.setRequestAttributes(old);
			}
			if (push) {
				UIPushGuard.push(ui);
			}
		});
	}
}
