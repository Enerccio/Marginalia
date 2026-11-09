package com.github.enerccio.marginalia.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Marks the text of a part that is still the text the LLM wrote: diffs the current response against the original one
 * word by word and wraps what both have in common in {@code <mark>} elements of the given CSS class, so the author's
 * additions and rewrites stay unmarked. The result is markdown with inline HTML for the {@code Markdown} component.
 */
public final class GeneratedTextHighlighter {

    private GeneratedTextHighlighter() {
    }

    /**
     * @return response with the parts shared with the original wrapped in {@code <mark class="cssClass">}; the
     * response itself when there is no original (the part was not generated) or nothing is shared
     */
    public static String highlight(String response, String original, String cssClass) {
        List<TextMarkup.Mark> marks = new ArrayList<>();
        for (int[] range : ranges(response, original)) {
            marks.add(new TextMarkup.Mark(range[0], range[1], cssClass));
        }
        return TextMarkup.apply(response, marks);
    }

    /**
     * @return the runs of the response that are still the text of the original, as [start, end) offsets in the response, in order;
     * empty when there is no original (the part was not generated) or nothing is shared
     */
    public static List<int[]> ranges(String response, String original) {
        List<int[]> ranges = new ArrayList<>();
        if (response == null || response.isBlank() || original == null || original.isBlank()) {
            return ranges;
        }

        List<String> tokens = tokenize(response);
        boolean[] generated = matchGenerated(tokens, tokenize(original));
        int position = 0;
        int runStart = -1;
        for (int i = 0; i < tokens.size(); i++) {
            if (generated[i]) {
                if (runStart < 0) {
                    runStart = position;
                }
            } else if (runStart >= 0) {
                ranges.add(new int[]{runStart, position});
                runStart = -1;
            }
            position += tokens.get(i).length();
        }
        if (runStart >= 0) {
            ranges.add(new int[]{runStart, position});
        }
        return ranges;
    }

    private static List<String> tokenize(String text) {
        return TextTokens.tokenize(text);
    }

    /**
     * The words both texts share (see {@link WordAlignment}); a white space token between two shared words is shared too.
     */
    private static boolean[] matchGenerated(List<String> current, List<String> original) {
        int[] matched = WordAlignment.align(current, original);
        int[] words = WordAlignment.wordIndexes(current);
        boolean[] shared = new boolean[current.size()];
        for (int word : words) {
            shared[word] = matched[word] >= 0;
        }
        for (int w = 0; w + 1 < words.length; w++) {
            if (shared[words[w]] && shared[words[w + 1]]) {
                for (int t = words[w] + 1; t < words[w + 1]; t++) {
                    shared[t] = true;
                }
            }
        }
        return shared;
    }
}
