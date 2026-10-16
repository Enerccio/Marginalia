package com.github.enerccio.marginalia.bound;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The last problems that were shown to the user in a session (see {@link UiProblem}), newest last, for the help
 * assistant. Kept in {@link SessionPoint#getProblems()}; it is a plain object so that code running on threads that
 * have no session can hold it. Synchronized: problems are added from the thread of the page and from the threads that
 * generate.
 */
public class ProblemLog {

    /** How many problems are kept. */
    public static final int CAPACITY = 20;

    /** The same message shown again within this time is the same problem (the code that says it and the notification it uses). */
    static final long SAME_PROBLEM_MILLIS = 2000;

    private final List<UiProblem> problems = new ArrayList<>();

    /**
     * Adds a problem. A problem with the same message as the newest one, within {@link #SAME_PROBLEM_MILLIS}, is the
     * same problem told twice (the code that failed, then the notification that showed it): only the more specific of
     * the two stays (a kind other than a plain error or warning, or the one that has a detail).
     */
    public synchronized UiProblem add(UiProblem.Kind kind, String message, String detail) {
        UiProblem problem = new UiProblem(UUID.randomUUID().toString(), System.currentTimeMillis(), kind, message, detail);
        if (!problems.isEmpty()) {
            UiProblem last = problems.getLast();
            if (last.message().equals(problem.message()) && problem.time() - last.time() <= SAME_PROBLEM_MILLIS) {
                if (specificity(problem) > specificity(last)) {
                    UiProblem merged = new UiProblem(last.id(), last.time(), problem.kind(), problem.message(), problem.detail());
                    problems.set(problems.size() - 1, merged);
                    return merged;
                }
                return last;
            }
        }
        problems.add(problem);
        if (problems.size() > CAPACITY) {
            problems.removeFirst();
        }
        return problem;
    }

    private static int specificity(UiProblem problem) {
        int kind = problem.kind() == UiProblem.Kind.ERROR || problem.kind() == UiProblem.Kind.WARNING ? 0 : 2;
        return kind + (problem.detail().isEmpty() ? 0 : 1);
    }

    /** The last {@code count} problems, newest first. */
    public synchronized List<UiProblem> last(int count) {
        List<UiProblem> last = new ArrayList<>();
        for (int i = problems.size() - 1; i >= 0 && last.size() < count; i--) {
            last.add(problems.get(i));
        }
        return last;
    }

    public synchronized void clear() {
        problems.clear();
    }
}
