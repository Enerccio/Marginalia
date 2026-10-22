package com.github.enerccio.marginalia.domain.service.agent;

/**
 * A text for a regular expression that gives up when its time is over: the pattern of a model can backtrack for
 * hours, and a thread cannot be stopped from outside. The matcher reads the characters through {@link #charAt}, which
 * looks at the clock now and then.
 */
final class DeadlineCharSequence implements CharSequence {

    private final String text;
    private final long deadline;
    private int reads;

    /**
     * @param deadline the {@link System#nanoTime} to give up at, see {@link #deadlineIn}
     */
    DeadlineCharSequence(String text, long deadline) {
        this.text = text;
        this.deadline = deadline;
    }

    /**
     * The nano time to give up at, to share by the texts of one tool call.
     */
    static long deadlineIn(long millis) {
        return System.nanoTime() + millis * 1_000_000L;
    }

    @Override
    public int length() {
        return text.length();
    }

    @Override
    public char charAt(int index) {
        if ((++reads & 0x3FF) == 0 && System.nanoTime() - deadline > 0) {
            throw new VirtualFileException("The regular expression took too long to match, use a simpler one");
        }
        return text.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return text.substring(start, end);
    }

    @Override
    public String toString() {
        return text;
    }

}
