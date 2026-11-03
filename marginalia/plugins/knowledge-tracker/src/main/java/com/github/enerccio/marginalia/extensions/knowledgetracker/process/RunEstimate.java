package com.github.enerccio.marginalia.extensions.knowledgetracker.process;

/**
 * What a series of runs with the model means before it starts: how many parts, how many requests and roughly how much
 * story text goes through the model. The instructions, the lorebook and the knowledge of the characters are sent with
 * every request on top of it, so the real count is higher; this is the lower bound.
 *
 * @param parts        the parts the series would read
 * @param runs         the requests (one run reads {@code partsPerRun} parts)
 * @param storyTokens  a rough count of the tokens of the text of those parts (a quarter of the characters)
 */
public record RunEstimate(int parts, int runs, long storyTokens) {

    /** A series of more requests than this is shown to the user before it starts. */
    public static final int CONFIRM_FROM_RUNS = 3;

    public static RunEstimate of(int parts, int partsPerRun, long characters) {
        int perRun = Math.max(1, partsPerRun);
        int runs = parts <= 0 ? 0 : (parts + perRun - 1) / perRun;
        return new RunEstimate(Math.max(0, parts), runs, Math.max(0, characters) / 4);
    }

    /** Whether the user should be told before the series starts. */
    public boolean isLong() {
        return runs > CONFIRM_FROM_RUNS;
    }
}
