package com.github.enerccio.marginalia.domain.service.agent.jsonmerge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

/**
 * Finds out which element of a list is which element of the same list in another version of it. A list has no keys, so the
 * versions are matched by what the elements are: the same data first, then the same identity (a {@code uuid}, an {@code id}, a
 * {@code name}...), then the elements that look most alike, and what is left over and of the same kind in order. The elements that
 * stay without a partner were added or removed.
 * <p>
 * The three versions of a list (the base, the agent's, the book's) become rows, a row is one element in each version that has it.
 * The user can correct the guess; the rows are theirs to split and join.
 */
public final class ListMatcher {

    /**
     * The fields that say which element an object is, in the order they are trusted.
     */
    static final List<String> IDENTITY = List.of("uuid", "id", "key", "name", "title", "label", "handle", "slug");

    private static final double SIMILAR = 0.5;
    // a list this long is matched by the data and the identity only, comparing everything with everything would take long
    private static final int SIMILARITY_LIMIT = 250_000;
    private static final int LEAVES = 400;

    private ListMatcher() {
    }

    /**
     * One element in each version of the list, the index of the element or -1 when the version has none.
     */
    public record Row(int base, int agent, int book) {
        public boolean hasAll() {
            return base >= 0 && agent >= 0 && book >= 0;
        }
    }

    /**
     * Lines up the versions of a list. A row follows the agent's order; the elements the agent does not have are put after the row
     * that precedes them in the book (then in the base).
     */
    public static List<Row> align(List<JsonElement> base, List<JsonElement> agent, List<JsonElement> book) {
        int[] baseToAgent = match(base, agent, true);
        int[] baseToBook = match(base, book, true);
        List<Integer> agentLeft = new ArrayList<>();
        List<Integer> bookLeft = new ArrayList<>();
        boolean[] agentUsed = new boolean[agent.size()];
        boolean[] bookUsed = new boolean[book.size()];
        for (int i = 0; i < base.size(); i++) {
            if (baseToAgent[i] >= 0) {
                agentUsed[baseToAgent[i]] = true;
            }
            if (baseToBook[i] >= 0) {
                bookUsed[baseToBook[i]] = true;
            }
        }
        for (int i = 0; i < agent.size(); i++) {
            if (!agentUsed[i]) {
                agentLeft.add(i);
            }
        }
        for (int i = 0; i < book.size(); i++) {
            if (!bookUsed[i]) {
                bookLeft.add(i);
            }
        }
        // what both sides added, matched to each other
        int[] agentToBook = match(pick(agent, agentLeft), pick(book, bookLeft), false);
        Map<Integer, Integer> bookOfNewAgent = new HashMap<>();
        Set<Integer> bookPaired = new HashSet<>();
        for (int i = 0; i < agentLeft.size(); i++) {
            if (agentToBook[i] >= 0) {
                bookOfNewAgent.put(agentLeft.get(i), bookLeft.get(agentToBook[i]));
                bookPaired.add(bookLeft.get(agentToBook[i]));
            }
        }

        Map<Integer, Integer> baseOfAgent = new HashMap<>();
        Map<Integer, Integer> baseOfBook = new HashMap<>();
        for (int i = 0; i < base.size(); i++) {
            if (baseToAgent[i] >= 0) {
                baseOfAgent.put(baseToAgent[i], i);
            }
            if (baseToBook[i] >= 0) {
                baseOfBook.put(baseToBook[i], i);
            }
        }

        // the agent's elements in its order
        List<Row> rows = new ArrayList<>();
        for (int a = 0; a < agent.size(); a++) {
            Integer b = baseOfAgent.get(a);
            int k = b != null ? baseToBook[b] : bookOfNewAgent.getOrDefault(a, -1);
            rows.add(new Row(b == null ? -1 : b, a, k));
        }
        // the book's elements the agent has not, each after the row that has the element before it
        int last = -1;
        for (int k = 0; k < book.size(); k++) {
            int placed = rowOfBook(rows, k);
            if (placed >= 0) {
                last = placed;
                continue;
            }
            Integer b = baseOfBook.get(k);
            rows.add(last + 1, new Row(b == null ? -1 : b, -1, k));
            last++;
        }
        // the base's elements neither of the others has
        last = -1;
        for (int b = 0; b < base.size(); b++) {
            int placed = rowOfBase(rows, b);
            if (placed >= 0) {
                last = placed;
                continue;
            }
            rows.add(last + 1, new Row(b, -1, -1));
            last++;
        }
        return rows;
    }

    private static int rowOfBook(List<Row> rows, int book) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).book == book) {
                return i;
            }
        }
        return -1;
    }

    private static int rowOfBase(List<Row> rows, int base) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).base == base) {
                return i;
            }
        }
        return -1;
    }

    private static List<JsonElement> pick(List<JsonElement> list, List<Integer> indexes) {
        List<JsonElement> picked = new ArrayList<>();
        for (int index : indexes) {
            picked.add(list.get(index));
        }
        return picked;
    }

    /**
     * The element of {@code to} for each element of {@code from}, -1 for none; what is left over is taken as replaced.
     */
    public static int[] match(List<JsonElement> from, List<JsonElement> to) {
        return match(from, to, true);
    }

    /**
     * The element of {@code to} for each element of {@code from}, -1 for none.
     *
     * @param replacement whether the elements that are left over, as many on each side and of the same kind, are taken as ones that
     *                    replaced the others (true for a version against the one it was made from), or as ones added alone (two versions
     *                    that both added elements)
     */
    public static int[] match(List<JsonElement> from, List<JsonElement> to, boolean replacement) {
        int[] result = new int[from.size()];
        Arrays.fill(result, -1);
        boolean[] taken = new boolean[to.size()];
        if (from.isEmpty() || to.isEmpty()) {
            return result;
        }
        // 1. the same data
        Map<JsonElement, Deque<Integer>> byData = new HashMap<>();
        for (int j = 0; j < to.size(); j++) {
            byData.computeIfAbsent(to.get(j), element -> new ArrayDeque<>()).add(j);
        }
        for (int i = 0; i < from.size(); i++) {
            Deque<Integer> same = byData.get(from.get(i));
            if (same != null && !same.isEmpty()) {
                int j = same.poll();
                result[i] = j;
                taken[j] = true;
            }
        }
        // 2. the same identity
        for (String field : IDENTITY) {
            Map<String, Deque<Integer>> byIdentity = new HashMap<>();
            for (int j = 0; j < to.size(); j++) {
                String identity = taken[j] ? null : identity(to.get(j), field);
                if (identity != null) {
                    byIdentity.computeIfAbsent(identity, key -> new ArrayDeque<>()).add(j);
                }
            }
            for (int i = 0; i < from.size(); i++) {
                String identity = result[i] >= 0 ? null : identity(from.get(i), field);
                Deque<Integer> same = identity == null ? null : byIdentity.get(identity);
                if (same != null && !same.isEmpty()) {
                    int j = same.poll();
                    result[i] = j;
                    taken[j] = true;
                }
            }
        }
        // 3. the ones that look alike
        List<Integer> freeFrom = free(result);
        List<Integer> freeTo = new ArrayList<>();
        for (int j = 0; j < to.size(); j++) {
            if (!taken[j]) {
                freeTo.add(j);
            }
        }
        if ((long) freeFrom.size() * freeTo.size() <= SIMILARITY_LIMIT && !freeFrom.isEmpty() && !freeTo.isEmpty()) {
            Map<Integer, Set<String>> leavesFrom = new HashMap<>();
            Map<Integer, Set<String>> leavesTo = new HashMap<>();
            record Candidate(int from, int to, double score) {
            }
            List<Candidate> candidates = new ArrayList<>();
            for (int i : freeFrom) {
                for (int j : freeTo) {
                    double score = similarity(from.get(i), to.get(j), i, j, leavesFrom, leavesTo);
                    if (score >= SIMILAR) {
                        candidates.add(new Candidate(i, j, score));
                    }
                }
            }
            candidates.sort(Comparator.comparingDouble((Candidate c) -> -c.score).thenComparingInt(c -> Math.abs(c.from - c.to)));
            for (Candidate candidate : candidates) {
                if (result[candidate.from] < 0 && !taken[candidate.to]) {
                    result[candidate.from] = candidate.to;
                    taken[candidate.to] = true;
                }
            }
        }
        // 4. what is left, as many on each side and of the same kind: one replaced the other
        freeFrom = free(result);
        freeTo = new ArrayList<>();
        for (int j = 0; j < to.size(); j++) {
            if (!taken[j]) {
                freeTo.add(j);
            }
        }
        if (replacement && !freeFrom.isEmpty() && freeFrom.size() == freeTo.size()) {
            boolean alike = true;
            for (int n = 0; n < freeFrom.size(); n++) {
                alike &= kind(from.get(freeFrom.get(n))).equals(kind(to.get(freeTo.get(n))));
            }
            if (alike) {
                for (int n = 0; n < freeFrom.size(); n++) {
                    result[freeFrom.get(n)] = freeTo.get(n);
                }
            }
        }
        return result;
    }

    private static List<Integer> free(int[] result) {
        List<Integer> free = new ArrayList<>();
        for (int i = 0; i < result.length; i++) {
            if (result[i] < 0) {
                free.add(i);
            }
        }
        return free;
    }

    private static String kind(JsonElement element) {
        if (element.isJsonObject()) {
            return "object";
        }
        if (element.isJsonArray()) {
            return "array";
        }
        if (element.isJsonNull()) {
            return "null";
        }
        return element.getAsJsonPrimitive().isString() ? "string" : element.getAsJsonPrimitive().isBoolean() ? "boolean" : "number";
    }

    /**
     * What the object says about itself in the field, null when it says nothing.
     */
    static String identity(JsonElement element, String field) {
        if (element == null || !element.isJsonObject()) {
            return null;
        }
        JsonElement value = element.getAsJsonObject().get(field);
        if (value == null || !value.isJsonPrimitive()) {
            return null;
        }
        String text = value.getAsString();
        return text.isBlank() ? null : text;
    }

    /**
     * How much of what two elements say is the same, 0 to 1.
     */
    private static double similarity(JsonElement a, JsonElement b, int i, int j, Map<Integer, Set<String>> leavesA,
                                     Map<Integer, Set<String>> leavesB) {
        if (!kind(a).equals(kind(b))) {
            return 0;
        }
        if (a.isJsonPrimitive() && a.getAsJsonPrimitive().isString()) {
            return jaccard(words(a.getAsString()), words(b.getAsString()));
        }
        if (!a.isJsonObject() && !a.isJsonArray()) {
            return 0;
        }
        Set<String> left = leavesA.computeIfAbsent(i, key -> leaves(a));
        Set<String> right = leavesB.computeIfAbsent(j, key -> leaves(b));
        return jaccard(left, right);
    }

    private static double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() && b.isEmpty()) {
            return 1;
        }
        int common = 0;
        for (String item : a) {
            if (b.contains(item)) {
                common++;
            }
        }
        return (double) common / (a.size() + b.size() - common);
    }

    private static Set<String> words(String text) {
        Set<String> words = new HashSet<>();
        for (String word : text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+")) {
            if (!word.isEmpty()) {
                words.add(word);
            }
        }
        return words;
    }

    /**
     * The values of an element with the path to each, the place in a list left out; a text is cut to its words.
     */
    private static Set<String> leaves(JsonElement element) {
        Set<String> leaves = new HashSet<>();
        collect(element, "", leaves);
        return leaves;
    }

    private static void collect(JsonElement element, String path, Set<String> leaves) {
        if (leaves.size() >= LEAVES) {
            return;
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            // the names of the keys count as well: the objects of one shape are alike even when every value changed
            for (Map.Entry<String, JsonElement> member : object.entrySet()) {
                leaves.add(path + "/" + member.getKey());
                collect(member.getValue(), path + "/" + member.getKey(), leaves);
            }
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement item : array) {
                collect(item, path + "[]", leaves);
            }
        } else if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            for (String word : words(element.getAsString())) {
                leaves.add(path + "=" + word);
            }
        } else {
            leaves.add(path + "=" + element);
        }
    }
}
