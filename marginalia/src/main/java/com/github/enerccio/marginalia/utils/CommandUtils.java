package com.github.enerccio.marginalia.utils;

import com.github.enerccio.marginalia.bound.SessionPoint;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptWindow;
import com.github.enerccio.marginalia.ui.workspace.ManuscriptSection;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import jakarta.servlet.ServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * What a {@link com.github.enerccio.marginalia.domain.traits.Command} needs to know about what the user is looking at,
 * read from the {@link SessionPoint} of the session the command runs in (the session of the caller, also on the worker
 * thread of an {@code async} command). A command suite is stateless, so it asks here every time it runs.
 * <p>
 * The methods work on the desktop and the phone workspace alike: the book window is the {@link ManuscriptWindow}
 * interface, not the dialog of one of them. All of them return {@code null} when there is nothing to return - no
 * session (a thread without the request attributes), no workspace yet (before the login completes), no book open - so
 * a command checks the result.
 * <p>
 * A session shared by several browser tabs holds the workspace of the tab that logged in last, see
 * {@link SessionPoint#getWorkspace()}. The components belong to their UI: change them from a thread without the UI lock
 * only inside {@code ui.access(...)} ({@link ManuscriptWindow#asDialog()} reaches the UI).
 */
public final class CommandUtils {

    private CommandUtils() {
    }

    /**
     * The session scoped {@link SessionPoint}, null when the thread has no session.
     */
    public static SessionPoint getSessionPoint() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servlet)) {
            return null;
        }
        ServletContext servletContext = servlet.getRequest().getServletContext();
        WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        return context == null ? null : context.getBean(SessionPoint.class);
    }

    /**
     * The workspace the user is working in.
     */
    public static Workspace getWorkspace() {
        SessionPoint sessionPoint = getSessionPoint();
        return sessionPoint == null ? null : sessionPoint.getWorkspace();
    }

    /**
     * The book list of the workspace.
     */
    public static ManuscriptSection getManuscriptSection() {
        Workspace workspace = getWorkspace();
        return workspace == null ? null : workspace.getManuscriptSection();
    }

    /**
     * The window of the book that is open, the {@code ManuscriptDialog} or the {@code PhoneManuscriptDialog}.
     */
    public static ManuscriptWindow getManuscriptWindow() {
        Workspace workspace = getWorkspace();
        return workspace == null ? null : workspace.getOpenManuscriptWindow();
    }

    /**
     * The book that is open. It is the window's own copy of the book, see {@link ManuscriptWindow#getManuscript()}.
     */
    public static Manuscript getCurrentManuscript() {
        ManuscriptWindow window = getManuscriptWindow();
        return window == null ? null : window.getManuscript();
    }

    /**
     * The model of the book that is open; null when no book is open or the book has no model.
     */
    public static AI getCurrentAI() {
        ManuscriptWindow window = getManuscriptWindow();
        return window == null ? null : window.getAi();
    }

}
