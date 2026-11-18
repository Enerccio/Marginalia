package com.github.enerccio.marginalia.utils;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A text for a regular expression that gives up when its time is over. A pattern with nested repetitions can backtrack
 * for hours on a text that is not long, and a thread can't be stopped from outside, so the matcher reads the text
 * through this and {@link #charAt} looks at the clock now and then and throws {@link RegexTimeoutException}.
 * <pre>
 * long deadline = TimeSafeText.deadlineIn(TimeSafeText.timeoutMillis());
 * Matcher matcher = TimeSafeText.matcher(pattern, text, deadline);
 * </pre>
 * (The technique is the one described in "Preventing unbounded regular expression operations in Java" on exratione.com.)
 * A matcher is made for one use and then dropped, because the deadline is fixed when the text is wrapped. The patterns of
 * a user or a model go through this, the patterns the application writes itself do not need it. The
 * time is {@value #DEFAULT_TIMEOUT_MILLIS} ms unless the system property {@value #TIMEOUT_PROPERTY} says otherwise.
 */
public final class TimeSafeText implements CharSequence {

    /** How long a regular expression may run, 30 seconds. */
    public static final long DEFAULT_TIMEOUT_MILLIS = 30_000;

    /** The system property that changes {@link #DEFAULT_TIMEOUT_MILLIS}, in milliseconds. */
    public static final String TIMEOUT_PROPERTY = "marginalia.regexTimeoutMillis";

    private final String text;
    private final long deadline;
    private int reads;

    /**
     * @param deadline the {@link System#nanoTime} to give up at, see {@link #deadlineIn}
     */
    public TimeSafeText(String text, long deadline) {
        this.text = text;
        this.deadline = deadline;
    }

    /** The time a regular expression may run for, see the class description. */
    public static long timeoutMillis() {
        return Long.getLong(TIMEOUT_PROPERTY, DEFAULT_TIMEOUT_MILLIS);
    }

    /** The nano time to give up at, to share by the texts of one call. */
    public static long deadlineIn(long millis) {
        return System.nanoTime() + millis * 1_000_000L;
    }

    /** The matcher of the pattern over the text, which throws {@link RegexTimeoutException} after the deadline. */
    public static Matcher matcher(Pattern pattern, String text, long deadline) {
        return pattern.matcher(new TimeSafeText(text, deadline));
    }

    /**
     * The view of a text for the matching that gives each text the whole {@link #timeoutMillis()} from the moment it
     * is looked at; for the places that take a {@code Function<String, CharSequence>}.
     */
    public static Function<String, CharSequence> view() {
        return text -> new TimeSafeText(text, deadlineIn(timeoutMillis()));
    }

    @Override
    public int length() {
        return text.length();
    }

    @Override
    public char charAt(int index) {
        if ((++reads & 0x3FF) == 0 && System.nanoTime() - deadline > 0) {
            throw new RegexTimeoutException("The regular expression took too long to match and was stopped, use a simpler one");
        }
        return text.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        // the part is limited by the same deadline, whatever reads it
        return new TimeSafeText(text.substring(start, end), deadline);
    }

    @Override
    public String toString() {
        return text;
    }

}
