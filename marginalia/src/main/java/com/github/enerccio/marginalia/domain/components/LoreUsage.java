package com.github.enerccio.marginalia.domain.components;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * The lorebook entries a generation put into its prompt, kept with the prompt of the part
 * ({@code MessagePrompt#getLoreUsage()}). Only the entries that were used are listed: the ones that were left out are the
 * entries of the lorebooks minus these, and why an entry is not used now is answered by the activation preview. An empty
 * list means the lorebook was scanned and nothing was used; {@code null} on the prompt means it was not recorded (the part
 * was made before this, or the book has no lorebook).
 */
public class LoreUsage {

    private static final Gson GSON = new Gson();

    private List<Used> entries = new ArrayList<>();

    /**
     * One entry that was used.
     *
     * @param entry  the uuid of the entry (the id of a restored book differs, the uuid is kept)
     * @param tokens the tokens of the text it added to the prompt, as the model of the book counts them
     * @param depth  0 when the prompt activated it, n when the content of the entries of the pass n - 1 did (scan depth)
     */
    public record Used(String entry, long tokens, int depth) {
    }

    public List<Used> getEntries() {
        return entries;
    }

    public void setEntries(List<Used> entries) {
        this.entries = entries == null ? new ArrayList<>() : entries;
    }

    public void add(String entry, long tokens, int depth) {
        entries.add(new Used(entry, tokens, depth));
    }

    /**
     * @return the tokens of all the entries
     */
    public long totalTokens() {
        long sum = 0;
        for (Used used : entries) {
            sum += used.tokens();
        }
        return sum;
    }

    /**
     * @return json for storing, never null
     */
    public String toJson() {
        return GSON.toJson(this);
    }

    /**
     * @return parsed usage, null for null, blank or malformed input (a part whose lore was not recorded)
     */
    public static LoreUsage fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return GSON.fromJson(json, LoreUsage.class);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
