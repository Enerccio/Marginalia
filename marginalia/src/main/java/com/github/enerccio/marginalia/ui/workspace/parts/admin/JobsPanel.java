package com.github.enerccio.marginalia.ui.workspace.parts.admin;

import com.github.enerccio.marginalia.ui.LLMInteract;
import com.github.enerccio.marginalia.ui.jobs.JobHistoryGrid;
import com.vaadin.flow.component.Component;

/**
 * The administrator's log of the jobs (see {@code Jobs}): what ran, for whom, when it started and ended, and each call it made to a
 * model, a speech or an image backend; filtered, paged and deleted by hand. What runs now is on the page {@code /jobs}.
 */
public class JobsPanel implements LLMInteract {

    public Component create() {
        return new JobHistoryGrid().create();
    }
}
