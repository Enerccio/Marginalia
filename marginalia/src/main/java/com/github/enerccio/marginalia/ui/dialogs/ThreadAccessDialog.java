package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;

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
		attributes = ThreadCopyRequestAttributes.create();
		Thread t = new Thread(() -> {
			try (InRequestScope _ = new InRequestScope(attributes)) {
				runInThread();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		});
		t.setName(threadName);
		t.start();
	}

	protected abstract void runInThread() throws Exception;
	
	public void vaadinLocked(Runnable r) {
		ui.access(() -> {
			try (InRequestScope _ = new InRequestScope(attributes)) {
				RequestContextHolder.setRequestAttributes(attributes);
				r.run();
			}
			if (push) {
				UIPushGuard.push(ui);
			}
		});
	}

	public void vaadinLockedSync(Runnable r) {
		ui.accessSynchronously(() -> {
			try (InRequestScope _ = new InRequestScope(attributes)) {;
				r.run();
			}
			if (push) {
				UIPushGuard.push(ui);
			}
		});
	}
}
