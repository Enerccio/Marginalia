package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.bound.AgentRequestAttributes;
import com.github.enerccio.marginalia.bound.AgentSession;
import com.github.enerccio.marginalia.bound.AgenticSessionPoint;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.github.enerccio.marginalia.utils.CommandUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The request context of a run: made from the run alone, it makes the session scoped beans resolve on any thread of
 * the run, and {@code ThreadCopyRequestAttributes.create()} hands it to the threads the run starts.
 */
class AgentSessionTest extends AgentTestBase {

    @Autowired
    private Localization localization;

    private User user;
    private Agent agent;
    private AgentRun run;

    @BeforeEach
    void setUp() throws Exception {
        user = loginAgentUser();
        agent = agent();
        run = agentRunService.start(agent);
    }

    @Test
    void theSessionOfARunIsItsOwnerAndItsLanguage() throws Exception {
        AgentSession session = AgentSession.of(run);

        assertThat(session.userUuid()).isEqualTo(user.getUuid());
        assertThat(session.language().code()).isEqualTo("en");

        run.setLanguage("ja");
        assertThat(AgentSession.of(run).language().code()).isEqualTo("ja");
    }

    @Test
    void aSessionNeedsAUser() {
        assertThatThrownBy(() -> new AgentSession(null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AgentSession(" ", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AgentSession.of(new AgentRun())).isInstanceOf(IllegalArgumentException.class);
        assertThat(new AgentSession("u", null).language().code()).as("English by default").isEqualTo("en");
    }

    @Test
    void theSessionBeansResolveAsTheOwnerOfTheRun() throws Exception {
        Manuscript copy = agentRunService.find(run.getId()).getWorkingCopy();
        // the thread of the request is somebody else
        loginAs(createUser());
        assertThat(currentUser.getId()).isNotEqualTo(user.getId());

        try (AgentRequestAttributes attributes = agentRunService.openSession(run);
             InRequestScope _ = new InRequestScope(attributes)) {
            assertThat(currentUser.getId()).isEqualTo(user.getId());
            assertThat(currentUser.getUuid()).isEqualTo(user.getUuid());
            assertThat(currentUser.getLogin()).isEqualTo(user.getLogin());
            assertThat(currentUser.isAdmin()).isFalse();
            assertThat(CommandUtils.getSessionPoint()).isInstanceOf(AgenticSessionPoint.class);
            assertThat(CommandUtils.getCurrentManuscript().getUuid()).as("the copy of the run, not the book of the user")
                    .isEqualTo(copy.getUuid()).isNotEqualTo(agent.getManuscript().getUuid());
            assertThat(CommandUtils.getWorkspace()).isNull();
            assertThat(CommandUtils.getManuscriptWindow()).isNull();
        }

        assertThat(currentUser.getId()).as("the user of the request again").isNotEqualTo(user.getId());
    }

    @Test
    void aRunWithoutACopyHasNoSession() throws Exception {
        AgentRun noCopy = new AgentRun();
        noCopy.setAgent(agent());
        noCopy.setStatus(AgentRunStatus.PAUSED);
        AgentRun saved = agentRunService.save(noCopy);

        assertThatThrownBy(() -> agentRunService.openSession(saved)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void servicesRunAsTheOwnerOfTheRun() throws Exception {
        loginAs(createUser());

        try (AgentRequestAttributes attributes = agentRunService.openSession(run);
             InRequestScope _ = new InRequestScope(attributes)) {
            assertThat(agentService.findForUser(agent.getUuid())).isNotNull();
        }
        assertThat(agentService.findForUser(agent.getUuid())).as("not the agent of this user").isNull();
    }

    @Test
    void textsAreInTheLanguageOfTheRun() throws Exception {
        run.setLanguage("ja");
        run = agentRunService.save(run);

        try (AgentRequestAttributes attributes = agentRunService.openSession(run);
             InRequestScope _ = new InRequestScope(attributes)) {
            assertThat(localization.getCurrentLanguage().code()).isEqualTo("ja");
            assertThat(localization.getValue(L.LABEL_CLOSE)).isEqualTo("閉じる");
        }
        assertThat(localization.getCurrentLanguage().code()).isEqualTo("en");
    }

    @Test
    void aLanguageWithoutABundleFallsBackToTheDefault() throws Exception {
        run.setLanguage("cs");
        run = agentRunService.save(run);

        try (AgentRequestAttributes attributes = agentRunService.openSession(run);
             InRequestScope _ = new InRequestScope(attributes)) {
            assertThat(localization.getCurrentLanguage().code()).isEqualTo("en");
        }
    }

    @Test
    void theThreadsOfARunShareTheContext() throws Exception {
        loginAs(createUser());
        AtomicReference<Long> userOnThread = new AtomicReference<>();
        AtomicReference<RequestAttributes> copied = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();

        try (AgentRequestAttributes attributes = agentRunService.openSession(run);
             InRequestScope _ = new InRequestScope(attributes)) {
            // what a command that starts a thread does: takes the context here, runs the thread in it
            RequestAttributes handed = ThreadCopyRequestAttributes.create();
            copied.set(handed);
            Thread thread = new Thread(() -> {
                assertThat(RequestContextHolder.getRequestAttributes()).as("a new thread has none").isNull();
                try (InRequestScope _ = new InRequestScope(handed)) {
                    userOnThread.set(currentUser.getId());
                } catch (Throwable t) {
                    failure.set(t);
                }
            });
            thread.start();
            thread.join();

            assertThat(handed).isSameAs(attributes);
        }

        assertThat(failure.get()).isNull();
        assertThat(userOnThread.get()).isEqualTo(user.getId());
    }

    @Test
    void createGivesTheCopyOfABrowserSessionAndNothingWithoutOne() throws Exception {
        RequestAttributes browser = RequestContextHolder.getRequestAttributes();
        assertThat(ThreadCopyRequestAttributes.create()).isInstanceOf(ThreadCopyRequestAttributes.class);
        try {
            RequestContextHolder.resetRequestAttributes();
            assertThat(ThreadCopyRequestAttributes.create()).isNull();
        } finally {
            RequestContextHolder.setRequestAttributes(browser);
        }
    }

    @Test
    void theUserOfTheSessionMustBeTheUserGiven() throws Exception {
        User other = createUser();
        AgenticSessionPoint point = new AgenticSessionPoint(agent.getManuscript());

        assertThatThrownBy(() -> new AgentRequestAttributes(AgentSession.of(run), other, point))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AgentRequestAttributes(AgentSession.of(run), null, point))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void closingTheSessionRunsTheDestructionCallbacksAndClearsItExceptTheUser() throws Exception {
        AtomicReference<String> destroyed = new AtomicReference<>();
        AgentRequestAttributes attributes = agentRunService.openSession(run);
        attributes.setAttribute("x", "value", RequestAttributes.SCOPE_SESSION);
        attributes.registerDestructionCallback("x", () -> destroyed.set("x"), RequestAttributes.SCOPE_SESSION);
        assertThat(attributes.getAttribute("x", RequestAttributes.SCOPE_SESSION)).isEqualTo("value");
        assertThat(attributes.getAttribute("x", RequestAttributes.SCOPE_REQUEST)).as("separate scopes").isNull();
        assertThat(attributes.getSessionId()).startsWith("agent-");

        attributes.close();

        assertThat(destroyed.get()).isEqualTo("x");
        // the user and the session point stay, work that is still in the air (the counting of the usage) resolves them
        assertThat(attributes.getAttributeNames(RequestAttributes.SCOPE_SESSION))
                .containsExactlyInAnyOrder(AgentRequestAttributes.USER_TARGET_NAME, AgenticSessionPoint.SCOPED_TARGET_NAME);
        assertThat(attributes.getAttribute("x", RequestAttributes.SCOPE_SESSION)).isNull();
    }

    @Test
    void aRunThatDoesNotExistHasNoSession() {
        assertThatThrownBy(() -> agentRunService.openSession(new AgentRun())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> agentRunService.openSession(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
