package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The request context a worker thread runs in outlives the browser session: a page that is closed while a model works closes the session, and the work
 * must still be written, so the session-scoped beans (the user) are found in the copy.
 */
class ThreadCopyRequestAttributesTest {

    private static final String USER = "scopedTarget.user";

    private MockHttpSession session;

    private MockHttpServletRequest request;

    @BeforeEach
    void pageRequest() {
        session = new MockHttpSession();
        request = new MockHttpServletRequest();
        request.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
    }

    @AfterEach
    void clear() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void aSessionScopedBeanIsFoundAfterTheSessionIsInvalid() {
        Object user = new Object();
        session.setAttribute(USER, user);
        RequestAttributes copy = ThreadCopyRequestAttributes.create();

        session.invalidate();

        assertSame(user, copy.getAttribute(USER, RequestAttributes.SCOPE_SESSION));
        assertArrayEquals(new String[]{USER}, copy.getAttributeNames(RequestAttributes.SCOPE_SESSION));
    }

    @Test
    void theCopyNeedsNoRequestOnceTheRequestIsOver() {
        request.setAttribute("seen", "yes");
        ThreadCopyRequestAttributes copy = (ThreadCopyRequestAttributes) ThreadCopyRequestAttributes.create();

        // the container recycles the request: every call on it fails from now on (the mock request does the same once closed)
        request.close();
        assertThrows(IllegalStateException.class, () -> request.getAttribute("seen"));

        assertEquals("yes", copy.getAttribute("seen", RequestAttributes.SCOPE_REQUEST));
        copy.setAttribute("later", 1, RequestAttributes.SCOPE_REQUEST);
        assertEquals(1, copy.getAttribute("later", RequestAttributes.SCOPE_REQUEST));
        copy.removeAttribute("seen", RequestAttributes.SCOPE_REQUEST);
        assertArrayEquals(new String[]{"later"}, copy.getAttributeNames(RequestAttributes.SCOPE_REQUEST));
        assertSame(request.getServletContext(), copy.getServletContext());
    }

    @Test
    void aWorkerThreadResolvesTheBeanAfterThePageIsGone() throws Exception {
        Object user = new Object();
        session.setAttribute(USER, user);
        RequestAttributes copy = ThreadCopyRequestAttributes.create();
        session.invalidate();

        AtomicReference<Object> found = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread worker = Thread.ofVirtual().start(() -> {
            try (InRequestScope _ = new InRequestScope(copy)) {
                found.set(RequestContextHolder.currentRequestAttributes().getAttribute(USER, RequestAttributes.SCOPE_SESSION));
            } catch (Throwable e) {
                failure.set(e);
            }
        });
        worker.join();

        assertNull(failure.get());
        assertSame(user, found.get());
    }

    @Test
    void theCopyLocksTheSameMutexAsThePageAndNeverFailsOnAnInvalidSession() {
        RequestAttributes copy = ThreadCopyRequestAttributes.create();
        Object mutex = ((ServletRequestAttributes) copy).getSessionMutex();

        assertSame(WebUtils.getSessionMutex(session), mutex);
        session.invalidate();
        assertSame(mutex, ((ServletRequestAttributes) copy).getSessionMutex());
    }

    @Test
    void aBeanTheWorkerAddsIsSeenByThePageWhileTheSessionLivesAndKeptWhenItDoesNot() {
        RequestAttributes copy = ThreadCopyRequestAttributes.create();
        Object early = new Object();
        Object late = new Object();

        copy.setAttribute("early", early, RequestAttributes.SCOPE_SESSION);
        assertSame(early, session.getAttribute("early"));

        session.invalidate();
        copy.setAttribute("late", late, RequestAttributes.SCOPE_SESSION);
        assertSame(late, copy.getAttribute("late", RequestAttributes.SCOPE_SESSION));
        copy.removeAttribute("early", RequestAttributes.SCOPE_SESSION);
        assertNull(copy.getAttribute("early", RequestAttributes.SCOPE_SESSION));
    }

    @Test
    void aCopyOfACopyIsTheSameCopyAndThereIsNothingToCopyWithoutARequest() {
        RequestAttributes copy = ThreadCopyRequestAttributes.create();
        RequestContextHolder.setRequestAttributes(copy);
        assertSame(copy, ThreadCopyRequestAttributes.create());

        RequestContextHolder.resetRequestAttributes();
        assertNull(ThreadCopyRequestAttributes.create());
    }
}
