package com.github.enerccio.marginalia.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Follows a text through its versions and remembers for every word whether a model or a person wrote it. A version is applied on top of
 * the one before it the way a merge does: the words it has in common with the older text (see {@link WordAlignment}) keep who wrote
 * them, the other words are the work of whoever made the version. So a sentence a person typed into a generated text stays the
 * person's, and a passage a model redrafted is the model's, however many versions come later.
 */
public final class AuthorshipTracker {

    private List<String> tokens = List.of();
    // per token of the text, true when a model wrote it; only the tokens that are not white space are tracked
    private boolean[] generated = new boolean[0];

    /**
     * What the tracker knows at a time, to {@link #load(Snapshot) put back}.
     */
    public record Snapshot(List<String> tokens, boolean[] generated) {
    }

    /**
     * Starts from a text a model wrote, all of it.
     */
    public AuthorshipTracker startWith(String modelText) {
        replaceAll(modelText, true);
        return this;
    }

    /**
     * The text is a new one that has nothing of the old text: a generation, or a generation again in place.
     *
     * @param model whether a model wrote it
     */
    public void replaceAll(String text, boolean model) {
        tokens = TextTokens.tokenize(text == null ? "" : text);
        generated = new boolean[tokens.size()];
        Arrays.fill(generated, model);
    }

    /**
     * The text is the old one changed: what both have in common keeps its writer, the rest is written by the one who made this version.
     *
     * @param model whether a model made the change
     */
    public void change(String text, boolean model) {
        List<String> next = TextTokens.tokenize(text == null ? "" : text);
        int[] matched = WordAlignment.align(next, tokens);
        boolean[] flags = new boolean[next.size()];
        for (int i = 0; i < next.size(); i++) {
            flags[i] = matched[i] >= 0 ? generated[matched[i]] : model;
        }
        tokens = next;
        generated = flags;
    }

    /**
     * The text says what the old one said in other words (a translation), so the old text cannot be compared to it. {@code modelText}
     * is the text a model wrote, in the same words as this one: what the two have in common is a model's, the rest is a person's.
     */
    public void changeAgainst(String text, String modelText) {
        List<String> next = TextTokens.tokenize(text == null ? "" : text);
        int[] matched = WordAlignment.align(next, TextTokens.tokenize(modelText == null ? "" : modelText));
        boolean[] flags = new boolean[next.size()];
        for (int i = 0; i < next.size(); i++) {
            flags[i] = matched[i] >= 0;
        }
        tokens = next;
        generated = flags;
    }

    public Snapshot snapshot() {
        return new Snapshot(tokens, generated.clone());
    }

    public void load(Snapshot snapshot) {
        tokens = snapshot.tokens();
        generated = snapshot.generated().clone();
    }

    public String text() {
        return String.join("", tokens);
    }

    /**
     * @return the runs of the text a model wrote, as [start, end) offsets in the text, in order; a white space between two words of a model is
     * the model's too
     */
    public List<int[]> generatedRanges() {
        List<int[]> ranges = new ArrayList<>();
        boolean[] marked = marked();
        int position = 0;
        int runStart = -1;
        for (int i = 0; i < tokens.size(); i++) {
            if (marked[i]) {
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

    /**
     * @return how many words (letters and digits) a model wrote
     */
    public int generatedWords() {
        return count(true);
    }

    /**
     * @return how many words (letters and digits) a person wrote
     */
    public int writtenWords() {
        return count(false);
    }

    private int count(boolean byModel) {
        int words = 0;
        for (int i = 0; i < tokens.size(); i++) {
            if (isWord(tokens.get(i)) && generated[i] == byModel) {
                words++;
            }
        }
        return words;
    }

    private static boolean isWord(String token) {
        return Character.isLetterOrDigit(token.codePointAt(0)) || token.charAt(0) == '_';
    }

    private boolean[] marked() {
        boolean[] marked = new boolean[tokens.size()];
        int[] words = WordAlignment.wordIndexes(tokens);
        for (int word : words) {
            marked[word] = generated[word];
        }
        for (int w = 0; w + 1 < words.length; w++) {
            if (marked[words[w]] && marked[words[w + 1]]) {
                for (int t = words[w] + 1; t < words[w + 1]; t++) {
                    marked[t] = true;
                }
            }
        }
        return marked;
    }
}
