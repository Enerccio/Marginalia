package com.github.enerccio.marginalia.utils;

import com.github.enerccio.marginalia.bound.AgenticSessionPoint;
import com.github.enerccio.marginalia.bound.SessionPoint;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.dialogs.ConfirmDialog;
import com.github.enerccio.marginalia.ui.dialogs.ManuscriptWindow;
import com.github.enerccio.marginalia.ui.dialogs.UIPushGuard;
import com.github.enerccio.marginalia.ui.workspace.ManuscriptSection;
import com.github.enerccio.marginalia.ui.workspace.Workspace;
import com.vaadin.flow.component.UI;
import jakarta.servlet.ServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * What a {@link com.github.enerccio.marginalia.domain.traits.Command} needs to know about what the user is looking at,
 * read from the {@link SessionPoint} of the session the command runs in (the session of the caller, also on the worker
 * thread of an {@code async} command); the methods only delegate to it, the session point knows where the book comes
 * from (the open window for the user, the working copy for an {@link AgenticSessionPoint}). A command suite is stateless, so it asks here every time it runs.
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
     * The session scoped {@link SessionPoint}, null when the thread has no session. For a browser session it is the
     * bean of the servlet session; for any other request attributes (the run of an agent) the
     * {@link AgenticSessionPoint} they hold.
     */
    public static SessionPoint getSessionPoint() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servlet) {
            ServletContext servletContext = servlet.getRequest().getServletContext();
            WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
            return context == null ? null : context.getBean(SessionPoint.class);
        }
        if (attributes != null && attributes.getAttribute(AgenticSessionPoint.SCOPED_TARGET_NAME,
                RequestAttributes.SCOPE_SESSION) instanceof SessionPoint sessionPoint) {
            return sessionPoint;
        }
        return null;
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
        SessionPoint sessionPoint = getSessionPoint();
        return sessionPoint == null ? null : sessionPoint.getManuscriptSection();
    }

    /**
     * The window of the book that is open, the {@code ManuscriptDialog} or the {@code PhoneManuscriptDialog}; null in a
     * session without windows.
     */
    public static ManuscriptWindow getManuscriptWindow() {
        SessionPoint sessionPoint = getSessionPoint();
        return sessionPoint == null ? null : sessionPoint.getManuscriptWindow();
    }

    /**
     * The book the command works on: the book that is open (the window's own copy of it, see
     * {@link ManuscriptWindow#getManuscript()}), or the book of the agent's session.
     */
    public static Manuscript getCurrentManuscript() {
        SessionPoint sessionPoint = getSessionPoint();
        return sessionPoint == null ? null : sessionPoint.getCurrentManuscript();
    }

    /**
     * The model of the book; null when there is no book or the book has no model.
     */
    public static AI getCurrentAI() {
        SessionPoint sessionPoint = getSessionPoint();
        return sessionPoint == null ? null : sessionPoint.getCurrentAI();
    }

    /**
     * Asks the user a yes/no question from a command that runs on a worker thread, and waits for the answer. The dialog
     * opens in the UI of the book window, on top of the progress window of the command (so it covers its Stop button).
     * Gives {@code false} when the user answers no, when the run is terminated meanwhile (the dialog is closed then) and
     * when there is no window to ask in.
     *
     * @param context the context of the command that asks, to see whether it was stopped
     */
    public static boolean confirm(CommandContext context, String message) throws InterruptedException {
        ManuscriptWindow window = getManuscriptWindow();
        UI ui = window == null ? null : window.asDialog().getUI().orElse(null);
        if (ui == null) {
            return false;
        }
        CompletableFuture<Boolean> answer = new CompletableFuture<>();
        AtomicReference<ConfirmDialog> shown = new AtomicReference<>();
        ThreadCopyRequestAttributes attributes = ThreadCopyRequestAttributes.create();
        ui.access(() -> {
            try (ThreadCopyRequestAttributes.InRequestScope _ = new ThreadCopyRequestAttributes.InRequestScope(attributes)) {
                shown.set(ConfirmDialog.open(message, () -> answer.complete(true), () -> answer.complete(false)));
            }
            UIPushGuard.push(ui);
        });
        while (true) {
            try {
                return answer.get(100, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                if (context.isTerminated()) {
                    ui.access(() -> {
                        ConfirmDialog dialog = shown.get();
                        if (dialog != null) {
                            dialog.close();
                        }
                        UIPushGuard.push(ui);
                    });
                    return false;
                }
            } catch (java.util.concurrent.ExecutionException e) {
                return false;
            }
        }
    }

    /**
     * Shows the book window again from the database, after a command changed the book from a worker thread. Does
     * nothing when no book is open.
     */
    public static void reloadManuscriptWindow() {
        ManuscriptWindow window = getManuscriptWindow();
        UI ui = window == null ? null : window.asDialog().getUI().orElse(null);
        if (ui == null) {
            return;
        }
        ThreadCopyRequestAttributes attributes = ThreadCopyRequestAttributes.create();
        ui.access(() -> {
            try (ThreadCopyRequestAttributes.InRequestScope _ = new ThreadCopyRequestAttributes.InRequestScope(attributes)) {
                window.reload();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            UIPushGuard.push(ui);
        });
    }

}
