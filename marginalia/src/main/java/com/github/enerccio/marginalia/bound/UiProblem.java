package com.github.enerccio.marginalia.bound;

import java.util.regex.Pattern;

/**
 * Something that went wrong for the user and was shown to them (an error dialog, a notification), as the help
 * assistant's model can read it, see {@link ProblemLog}.
 *
 * @param id      identifies the problem, the same in the log of the session and in the copy the browser keeps
 * @param time    when it happened, epoch milliseconds
 * @param kind    what kind of problem it is
 * @param message what the user was told, one line of text without secrets
 * @param detail  what is known on top of it (the exceptions with their causes, the type of an inference failure), may
 *                be empty
 */
public record UiProblem(String id, long time, Kind kind, String message, String detail) {

    public enum Kind {
        /** An unexpected failure of the application: an error dialog with a stack trace. */
        EXCEPTION,
        /** A request to an inference provider that failed (wrong key, no such model, rate limit, a limit of the user). */
        INFERENCE,
        /** A form that could not be saved, with what the validation said. */
        VALIDATION,
        /** Another error the user was shown in a notification. */
        ERROR,
        /** A warning the user was shown in a notification. */
        WARNING
    }

    /** The longest text kept of a message or detail; the model needs the gist, not a dump. */
    public static final int MAX_TEXT = 400;

    private static final Pattern SECRET = Pattern.compile(
            "(?i)(bearer\\s+|sk-|(?:api[_-]?key|password|token)[\\s\"']*[=:][\\s\"']*)[A-Za-z0-9._\\-/+=]{6,}");

    public UiProblem {
        message = clean(message);
        detail = clean(detail);
    }

    /**
     * The text for the model: white space collapsed to single spaces, what looks like a key, a token or a password
     * replaced, cut at {@link #MAX_TEXT}. Never null.
     */
    public static String clean(String text) {
        if (text == null) {
            return "";
        }
        String clean = SECRET.matcher(text.replaceAll("\\s+", " ").strip()).replaceAll("$1[removed]");
        return clean.length() <= MAX_TEXT ? clean : clean.substring(0, MAX_TEXT - 3).stripTrailing() + "...";
    }
}
