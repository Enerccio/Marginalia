package com.github.enerccio.marginalia.bound;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptWindow;

/**
 * The {@link SessionPoint} of a session that has no browser, the run of an agent: there is no workspace and no book
 * window, the book the commands work on is held directly (the working copy of the run). The commands ask
 * {@link com.github.enerccio.marginalia.utils.CommandUtils} for the book and get it from here, so the same command
 * works for the user at the command bar and for an agent.
 * <p>
 * The request attributes of such a session keep this object under the name of the target of the scoped
 * {@code sessionPoint} bean ({@link #SCOPED_TARGET_NAME}), in the session scope.
 */
public class AgenticSessionPoint extends SessionPoint {

    /**
     * Name of the session scoped attribute that holds the {@link SessionPoint}: the name of the target bean behind the
     * scoped proxy of {@code sessionPoint}.
     */
    public static final String SCOPED_TARGET_NAME = "scopedTarget.sessionPoint";

    private volatile Manuscript manuscript;

    public AgenticSessionPoint() {
    }

    public AgenticSessionPoint(Manuscript manuscript) {
        this.manuscript = manuscript;
    }

    /**
     * Points the session at another book, for a run that continues on a new working copy.
     */
    public void setManuscript(Manuscript manuscript) {
        this.manuscript = manuscript;
    }

    @Override
    public Manuscript getCurrentManuscript() {
        return manuscript;
    }

    @Override
    public AI getCurrentAI() {
        Manuscript current = manuscript;
        return current == null ? null : current.getAi();
    }

    /**
     * An agent has no window to show the book in.
     */
    @Override
    public ManuscriptWindow getManuscriptWindow() {
        return null;
    }

}
