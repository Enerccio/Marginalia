package com.github.enerccio.marginalia.domain.service.agent.jsonmerge;

import com.google.gson.JsonElement;

/**
 * The three-way merge of JSON data given as text, what the review does with the data an agent and the book both keep (the
 * attributes): the base is the data when the run started, the other two the versions to put together. A blank text is data that is not
 * there. The merge itself is {@link JsonMergeNode}; the user can decide each place of it in the dialog.
 */
public final class JsonMerge {

    private JsonMerge() {
    }

    /**
     * The data with the changes of both versions, in the compact form of {@link JsonText#compact}; what both changed to different
     * data is the agent's. Text that is not JSON in any of the three is not merged, the agent's text is returned.
     */
    public static String merge(String base, String agent, String book) {
        JsonMergeNode root = root(base, agent, book);
        return root == null ? agent : JsonText.compact(root.result());
    }

    /**
     * Whether both versions changed the same place to different data. Text that is not JSON is one place, changed by both when the
     * book's differs from the base.
     */
    public static boolean conflicts(String base, String agent, String book) {
        JsonMergeNode root = root(base, agent, book);
        if (root == null) {
            return !java.util.Objects.equals(book, base);
        }
        return root.conflict();
    }

    private static JsonMergeNode root(String base, String agent, String book) {
        try {
            JsonElement b = JsonText.parse(base);
            JsonElement a = JsonText.parse(agent);
            JsonElement k = JsonText.parse(book);
            return JsonMergeNode.root(b, a, k);
        } catch (JsonText.InvalidJsonException e) {
            return null;
        }
    }
}
