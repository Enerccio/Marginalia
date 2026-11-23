package com.github.enerccio.marginalia.bound;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptWindow;
import com.github.enerccio.marginalia.ui.workspace.ManuscriptSection;
import com.github.enerccio.marginalia.ui.workspace.Workspace;

/**
 * The session of the application (the bean {@code sessionPoint}): the {@link SessionPoint} with the book the user has open, its
 * window and the book list. Code that needs those injects this type. {@link AgenticSessionPoint} holds the book of an agent or an
 * API call instead.
 */
public class MarginaliaSessionPoint extends SessionPoint {

    /**
     * The workspace of the page the user logged in on, see {@link SessionPoint#getWorkspace()}.
     */
    @Override
    public Workspace getWorkspace() {
        return (Workspace) super.getWorkspace();
    }

    /**
     * The book list of the workspace, null before the workspace exists.
     */
    public ManuscriptSection getManuscriptSection() {
        Workspace current = getWorkspace();
        return current == null ? null : current.getManuscriptSection();
    }

    /**
     * The window of the book that is open (the {@code ManuscriptDialog} or the {@code PhoneManuscriptDialog}), null when
     * there is no workspace or no book is open. {@link AgenticSessionPoint} has no window.
     */
    public ManuscriptWindow getManuscriptWindow() {
        Workspace current = getWorkspace();
        return current == null ? null : current.getOpenManuscriptWindow();
    }

    /**
     * The book the commands of this session work on: here the one the user has open, the window's own copy of it (see
     * {@link ManuscriptWindow#getManuscript()}). Null when there is none. {@link AgenticSessionPoint} holds the book
     * directly.
     */
    public Manuscript getCurrentManuscript() {
        ManuscriptWindow window = getManuscriptWindow();
        return window == null ? null : window.getManuscript();
    }

    /**
     * The model of {@link #getCurrentManuscript()}; null when there is no book or the book has no model.
     */
    public AI getCurrentAI() {
        ManuscriptWindow window = getManuscriptWindow();
        return window == null ? null : window.getAi();
    }
}
