package com.github.enerccio.marginalia.utils;

import com.github.enerccio.marginalia.bound.ProblemLog;
import com.github.enerccio.marginalia.bound.SessionPoint;
import com.github.enerccio.marginalia.bound.UiProblem;
import com.github.enerccio.marginalia.bound.UiProblem.Kind;
import com.github.enerccio.marginalia.ui.widgets.Notification;
import com.vaadin.flow.server.VaadinSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the application writes down of the problems the user was shown, for the help assistant.
 */
class UiProblemsTest {

    private SessionPoint sessionPoint;

    /** A session whose lock is "held": the test thread acts as the thread of the request. */
    private static final class HeldSession extends VaadinSession {
        HeldSession() {
            super(null);
        }

        @Override
        public boolean hasLock() {
            return true;
        }
    }

    @BeforeEach
    void setUp() {
        sessionPoint = new SessionPoint();
        VaadinSession.setCurrent(new HeldSession());
    }

    @AfterEach
    void tearDown() {
        VaadinSession.setCurrent(null);
    }

    @Test
    void nothingIsWrittenBeforeTheLogIsAttached() {
        UiProblems.error("lost");

        assertThat(sessionPoint.getProblems().last(10)).isEmpty();
    }

    @Test
    void writesWhatWasShownToTheLogOfTheSession() {
        UiProblems.attach(sessionPoint);

        UiProblems.exception("Internal error", new IllegalStateException("boom", new RuntimeException("root cause")));
        UiProblems.validation("Validation failed, can't save: Name is empty");
        UiProblems.warning("a warning");
        UiProblems.error("an error");

        List<UiProblem> problems = sessionPoint.getProblems().last(10);
        assertThat(problems).extracting(UiProblem::kind).containsExactly(Kind.ERROR, Kind.WARNING, Kind.VALIDATION, Kind.EXCEPTION);
        assertThat(problems.getLast().detail()).isEqualTo("IllegalStateException: boom <- caused by RuntimeException: root cause")
                .doesNotContain("\tat ");
    }

    @Test
    void anInferenceFailureIsNotRecordedTwiceWhenItsNotificationFollows() {
        UiProblems.attach(sessionPoint);

        UiProblems.inference("The provider did not accept the API key.", new RuntimeException("401 Unauthorized"));
        UiProblems.error("The provider did not accept the API key.");

        List<UiProblem> problems = sessionPoint.getProblems().last(10);
        assertThat(problems).hasSize(1);
        assertThat(problems.getFirst().kind()).isEqualTo(Kind.INFERENCE);
        assertThat(problems.getFirst().detail()).contains("401 Unauthorized");
    }

    @Test
    void theNotificationsTheUserSeesAreRecorded() {
        // setting the UI replaces the session of the thread with the UI's own (none), so the session comes second
        com.vaadin.flow.component.UI.setCurrent(new com.vaadin.flow.component.UI());
        VaadinSession.setCurrent(new HeldSession());
        UiProblems.attach(sessionPoint);
        try {
            Notification.warning("Validation failed, can't save");
            Notification.error("Could not reach the server");
            Notification.success("Saved");
        } finally {
            com.vaadin.flow.component.UI.setCurrent(null);
        }

        assertThat(sessionPoint.getProblems().last(10)).extracting(UiProblem::kind, UiProblem::message)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(Kind.ERROR, "Could not reach the server"),
                        org.assertj.core.groups.Tuple.tuple(Kind.WARNING, "Validation failed, can't save"));
    }

    @Test
    void aLogThatIsNotAttachedToThisSessionIsNotWrittenTo() {
        SessionPoint other = new SessionPoint();
        UiProblems.attach(other);
        VaadinSession.setCurrent(new HeldSession());

        UiProblems.error("a different session");

        assertThat(other.getProblems().last(10)).isEmpty();
    }

    @Test
    void readsWhatTheBrowserKeptAndSkipsWhatIsNotAProblem() {
        String stored = """
                [{"id":"a","time":10,"kind":"EXCEPTION","message":"Internal error","detail":"X: y"},
                 {"id":"b","time":20,"kind":"NOT_A_KIND","message":"x","detail":""},
                 "not an object",
                 {"id":"c","time":30,"kind":"INFERENCE","message":"Key refused"}]""";

        assertThat(UiProblems.fromBrowser(stored)).extracting(UiProblem::id, UiProblem::kind, UiProblem::detail)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("a", Kind.EXCEPTION, "X: y"),
                        org.assertj.core.groups.Tuple.tuple("c", Kind.INFERENCE, ""));
        assertThat(UiProblems.fromBrowser("not json")).isEmpty();
        assertThat(UiProblems.fromBrowser("{}")).isEmpty();
        assertThat(UiProblems.fromBrowser(null)).isEmpty();
    }

    @Test
    void theBrowsersCopyAddsWhatTheSessionNoLongerHasWithoutRepeatingTheRest() {
        ProblemLog log = new ProblemLog();
        UiProblem inSession = log.add(Kind.EXCEPTION, "new", "");
        UiProblem old = new UiProblem("old-id", inSession.time() - 60_000, Kind.EXCEPTION, "before the restart", "");

        List<UiProblem> merged = UiProblems.merge(log.last(10), List.of(old, inSession), 10);

        assertThat(merged).extracting(UiProblem::message).containsExactly("new", "before the restart");
    }

    @Test
    void mergeKeepsTheNewestAndClampsTheCount() {
        ProblemLog log = new ProblemLog();
        for (int i = 0; i < 15; i++) {
            log.add(Kind.ERROR, "problem " + i, "");
        }

        assertThat(UiProblems.merge(log.last(15), List.of(), 2)).hasSize(2);
        assertThat(UiProblems.merge(log.last(15), List.of(), 0)).as("at least one").hasSize(1);
        assertThat(UiProblems.merge(log.last(15), List.of(), 99)).as("at most ten").hasSize(10);
    }

    @Test
    void theModelGetsTheProblemsNewestFirstWithHowLongAgo() {
        long now = 1_000_000;
        List<UiProblem> problems = List.of(
                new UiProblem("1", now - 5_000, Kind.INFERENCE, "The provider did not accept the key.", "InferenceException: 401"),
                new UiProblem("2", now - 5 * 60_000, Kind.VALIDATION, "Validation failed, can't save", ""));

        String text = UiProblems.toMarkdown(problems, now);

        assertThat(text).startsWith("1. **INFERENCE**, 5 s ago: The provider did not accept the key.")
                .contains("   Detail: InferenceException: 401")
                .contains("2. **VALIDATION**, 5 min ago: Validation failed, can't save");
        assertThat(UiProblems.toMarkdown(List.of(), now)).contains("Nothing went wrong");
    }
}
