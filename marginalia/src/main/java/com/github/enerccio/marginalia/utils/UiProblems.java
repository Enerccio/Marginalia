package com.github.enerccio.marginalia.utils;

import com.github.enerccio.marginalia.bound.ProblemLog;
import com.github.enerccio.marginalia.bound.SessionPoint;
import com.github.enerccio.marginalia.bound.UiProblem;
import com.github.enerccio.marginalia.bound.UiProblem.Kind;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Where the application writes down what went wrong for the user, so that the help assistant can explain it: the
 * session's {@link ProblemLog} (everything that was shown: errors, inference failures, failed validations) and, for the
 * unexpected failures, the sessionStorage of the browser tab (the last {@link #BROWSER_COPIES}), which outlives the
 * session. {@link UIUtils} and the notifications call it, the rest of the code does not have to.
 * <p>
 * The log is reached through the Vaadin session, not through the {@link SessionPoint} bean: a notification is shown
 * from {@code ui.access(...)} on threads that have no HTTP request, where the bean can not be resolved. The widget of
 * the help assistant and the main view {@link #attach attach} the log of the session once.
 */
public final class UiProblems {
    private static final Logger log = LoggerFactory.getLogger(UiProblems.class);

    /** How many unexpected failures the browser keeps. */
    public static final int BROWSER_COPIES = 3;

    private static final int DEFAULT_COUNT = 3;
    private static final int MAX_COUNT = 10;

    private UiProblems() {
    }

    /** Makes the log of the session the one the problems of this Vaadin session are written to. */
    public static void attach(SessionPoint sessionPoint) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null && session.hasLock()) {
            session.setAttribute(ProblemLog.class, sessionPoint.getProblems());
        }
    }

    /** An unexpected failure that the user got an error dialog for. */
    public static void exception(String message, Throwable cause) {
        record(Kind.EXCEPTION, message, describe(cause));
    }

    /**
     * A failed request to an inference provider, as the user was told.
     *
     * @param cause the failure, may be null when only the text is known
     */
    public static void inference(String message, Throwable cause) {
        record(Kind.INFERENCE, message, cause == null ? "" : describe(cause));
    }

    /** A form that could not be saved, with what the validation said. */
    public static void validation(String message) {
        record(Kind.VALIDATION, message, "");
    }

    /** An error shown in a notification. */
    public static void error(String message) {
        record(Kind.ERROR, message, "");
    }

    /** A warning shown in a notification. */
    public static void warning(String message) {
        record(Kind.WARNING, message, "");
    }

    private static void record(Kind kind, String message, String detail) {
        try {
            VaadinSession session = VaadinSession.getCurrent();
            ProblemLog problems = session != null && session.hasLock() ? session.getAttribute(ProblemLog.class) : null;
            if (problems == null) {
                return;
            }
            UiProblem problem = problems.add(kind, message, detail);
            UI ui = UI.getCurrent();
            if (ui != null && (kind == Kind.EXCEPTION || kind == Kind.INFERENCE)) {
                JavascriptLibrary.rememberProblem(ui.getPage(), toJson(problem).toString(), BROWSER_COPIES);
            }
        } catch (RuntimeException e) {
            // writing down a problem must never be one
            log.debug("A problem could not be written down", e);
        }
    }

    /** The exceptions of the chain, the first class names and messages, without stack traces. */
    static String describe(Throwable cause) {
        List<String> chain = new ArrayList<>();
        for (int depth = 0; cause != null && depth < 4; depth++, cause = cause.getCause()) {
            chain.add(cause.getClass().getSimpleName() + (cause.getMessage() == null ? "" : ": " + cause.getMessage()));
        }
        return String.join(" <- caused by ", chain);
    }

    static JsonObject toJson(UiProblem problem) {
        JsonObject json = new JsonObject();
        json.addProperty("id", problem.id());
        json.addProperty("time", problem.time());
        json.addProperty("kind", problem.kind().name());
        json.addProperty("message", problem.message());
        json.addProperty("detail", problem.detail());
        return json;
    }

    /**
     * The problems the browser kept, from the text of the JSON array it returned. Anything that does not read is
     * skipped: the storage is the user's and can hold anything.
     */
    public static List<UiProblem> fromBrowser(String json) {
        List<UiProblem> problems = new ArrayList<>();
        try {
            JsonElement parsed = JsonParser.parseString(json == null || json.isBlank() ? "[]" : json);
            if (!parsed.isJsonArray()) {
                return problems;
            }
            JsonArray array = parsed.getAsJsonArray();
            for (JsonElement element : array) {
                try {
                    JsonObject object = element.getAsJsonObject();
                    problems.add(new UiProblem(object.get("id").getAsString(), object.get("time").getAsLong(),
                            Kind.valueOf(object.get("kind").getAsString()), text(object, "message"), text(object, "detail")));
                } catch (RuntimeException e) {
                    log.debug("A problem the browser kept can not be read", e);
                }
            }
        } catch (RuntimeException e) {
            log.debug("The problems the browser kept can not be read", e);
        }
        return problems;
    }

    private static String text(JsonObject object, String name) {
        return object.has(name) && !object.get(name).isJsonNull() ? object.get(name).getAsString() : "";
    }

    /**
     * The newest {@code count} problems of both copies, once each (the same id), newest first. The browser's copy
     * adds what a new session no longer has (the session expired, the application restarted).
     */
    public static List<UiProblem> merge(List<UiProblem> session, List<UiProblem> browser, int count) {
        Map<String, UiProblem> all = new LinkedHashMap<>();
        browser.forEach(problem -> all.put(problem.id(), problem));
        session.forEach(problem -> all.put(problem.id(), problem));
        return all.values().stream()
                .sorted(Comparator.comparingLong(UiProblem::time).reversed())
                .limit(Math.clamp(count, 1, MAX_COUNT))
                .toList();
    }

    /** How many the model gets when it does not say. */
    public static int defaultCount() {
        return DEFAULT_COUNT;
    }

    /**
     * The problems as Markdown for the model: newest first, with how long ago each happened. Says so when there are
     * none.
     */
    public static String toMarkdown(List<UiProblem> problems, long now) {
        if (problems.isEmpty()) {
            return "Nothing went wrong that the user was shown in this session or in this browser tab.";
        }
        StringBuilder sb = new StringBuilder();
        int number = 1;
        for (UiProblem problem : problems) {
            sb.append(number++).append(". **").append(problem.kind()).append("**, ").append(ago(now - problem.time()))
                    .append(": ").append(problem.message()).append('\n');
            if (!problem.detail().isEmpty()) {
                sb.append("   Detail: ").append(problem.detail()).append('\n');
            }
        }
        return sb.toString().strip();
    }

    private static String ago(long millis) {
        long seconds = Math.max(0, millis / 1000);
        if (seconds < 90) {
            return seconds + " s ago";
        }
        long minutes = seconds / 60;
        return minutes < 90 ? minutes + " min ago" : minutes / 60 + " h ago";
    }
}
