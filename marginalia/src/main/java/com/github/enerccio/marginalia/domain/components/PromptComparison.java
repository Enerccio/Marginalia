package com.github.enerccio.marginalia.domain.components;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Compares two built prompts (the payloads stored with the parts, see {@code MessagePromptService}) the way a server
 * with a prompt cache does: from the first character on, the first difference ends the part of the cache that can be
 * reused. The result says which message differs first, where, how much of the new prompt is the same as the earlier
 * one from the start, and why that probably is (see {@link Verdict}).
 * <p>
 * Only the messages are compared. What a server also takes into account (the model, the tools sent with the request,
 * the slot a request lands in, the sampler) is not in the stored prompt.
 */
public class PromptComparison {

    private static final Gson GSON = new Gson();

    /**
     * Status of a message position, comparing the earlier (cached) prompt with the new one.
     */
    public enum Status {
        /** The same role and content. */
        SAME,
        /** Both prompts have a message here and it differs. */
        CHANGED,
        /** Only the new prompt has a message here. */
        ADDED,
        /** Only the earlier prompt has a message here. */
        REMOVED
    }

    /**
     * Why the cache ends where it does.
     */
    public enum Verdict {
        /** The prompts are the same. */
        IDENTICAL,
        /** The new prompt starts with the whole earlier one and adds to it: the whole cache is reused. */
        EXTENDED,
        /** The new prompt is the start of the earlier one. */
        SHORTENED,
        /**
         * The first difference is in the last message of the earlier prompt, which is the user message of that turn and
         * is not repeated in the story: the best a next part or a swipe can get.
         */
        LAST_MESSAGE,
        /** The first message (the system message: jailbreak, template, lore, summaries, notes) differs. */
        SYSTEM_CHANGED,
        /** A message of the story is found again at another place: the start of the story moved or parts were summarised. */
        HISTORY_SHIFTED,
        /** A message in the middle of the story differs: a part was edited, regenerated or replaced by a summary. */
        HISTORY_CHANGED,
        /** The messages at the first difference have different roles. */
        ROLE_CHANGED
    }

    /**
     * One message of a prompt.
     *
     * @param role    {@code SYSTEM}, {@code USER}, {@code ASSISTANT} (as stored), may be null
     * @param content the text, attachments and tool calls written out; never null
     */
    public record Message(String role, String content) {
    }

    /**
     * A position of the two prompts.
     *
     * @param index       0 based position
     * @param status      what the two prompts have here
     * @param cached      the message of the earlier prompt, null when there is none
     * @param current     the message of the new prompt, null when there is none
     * @param commonChars characters at the start of the content the two messages share (all of it when {@code SAME},
     *                    0 for {@code ADDED}, {@code REMOVED} and different roles)
     * @param cacheKept   the position is entirely before the first difference
     */
    public record Row(int index, Status status, Message cached, Message current, int commonChars, boolean cacheKept) {
    }

    private final List<Message> cached;
    private final List<Message> current;
    private final List<Row> rows = new ArrayList<>();
    private final Verdict verdict;
    private final int firstDifference;
    private final int firstDifferenceChar;
    private final long sharedChars;
    private final long cachedChars;
    private final long currentChars;

    private PromptComparison(List<Message> cached, List<Message> current) {
        this.cached = cached;
        this.current = current;

        int common = Math.min(cached.size(), current.size());
        int first = -1;
        for (int i = 0; i < common; i++) {
            if (!cached.get(i).equals(current.get(i))) {
                first = i;
                break;
            }
        }
        if (first < 0 && cached.size() != current.size()) {
            first = common;
        }
        this.firstDifference = first;

        long shared = 0;
        int firstChar = 0;
        for (int i = 0; i < Math.max(cached.size(), current.size()); i++) {
            Message a = i < cached.size() ? cached.get(i) : null;
            Message b = i < current.size() ? current.get(i) : null;
            Status status;
            int commonChars = 0;
            if (a == null) {
                status = Status.ADDED;
            } else if (b == null) {
                status = Status.REMOVED;
            } else if (a.equals(b)) {
                status = Status.SAME;
                commonChars = a.content().length();
            } else {
                status = Status.CHANGED;
                if (Objects.equals(a.role(), b.role())) {
                    commonChars = commonPrefix(a.content(), b.content());
                }
            }
            boolean kept = first < 0 || i < first;
            if (kept) {
                shared += b == null ? 0 : b.content().length();
            } else if (i == first) {
                shared += commonChars;
                firstChar = commonChars;
            }
            rows.add(new Row(i, status, a, b, commonChars, kept));
        }
        this.firstDifferenceChar = firstChar;
        this.sharedChars = shared;
        this.cachedChars = cached.stream().mapToLong(m -> m.content().length()).sum();
        this.currentChars = current.stream().mapToLong(m -> m.content().length()).sum();
        this.verdict = decide();
    }

    private Verdict decide() {
        if (firstDifference < 0) {
            return Verdict.IDENTICAL;
        }
        if (firstDifference >= cached.size()) {
            return Verdict.EXTENDED;
        }
        if (firstDifference >= current.size()) {
            return Verdict.SHORTENED;
        }
        Message a = cached.get(firstDifference);
        Message b = current.get(firstDifference);
        if (!Objects.equals(a.role(), b.role())) {
            return Verdict.ROLE_CHANGED;
        }
        if (firstDifference == 0) {
            return Verdict.SYSTEM_CHANGED;
        }
        if (firstDifference == cached.size() - 1) {
            return Verdict.LAST_MESSAGE;
        }
        // the message of the new prompt is there in the earlier one, moved: the story window slid
        for (int j = 0; j < cached.size(); j++) {
            if (j != firstDifference && cached.get(j).equals(b) && !isBoilerplate(b)) {
                return Verdict.HISTORY_SHIFTED;
            }
        }
        return Verdict.HISTORY_CHANGED;
    }

    /**
     * The user messages that separate the parts of the story are the same everywhere and say nothing about a shift.
     */
    private static boolean isBoilerplate(Message message) {
        return message.content().length() < 40;
    }

    private static int commonPrefix(String a, String b) {
        int max = Math.min(a.length(), b.length());
        int i = 0;
        while (i < max && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        // do not cut a surrogate pair in half
        if (i > 0 && i < max && Character.isHighSurrogate(a.charAt(i - 1))) {
            i--;
        }
        return i;
    }

    /**
     * @param cachedPrompt  the built prompt of the earlier request (JSON of the payload), the one a server has cached
     * @param currentPrompt the built prompt of the new request
     * @return the comparison, null when one of them is missing or is not a list of messages
     */
    public static PromptComparison compare(String cachedPrompt, String currentPrompt) {
        List<Message> a = parse(cachedPrompt);
        List<Message> b = parse(currentPrompt);
        if (a == null || b == null) {
            return null;
        }
        return new PromptComparison(a, b);
    }

    /**
     * @return the messages of a stored prompt, null for null, blank or anything that is not a JSON list of messages
     */
    public static List<Message> parse(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return null;
        }
        try {
            JsonElement root = GSON.fromJson(prompt, JsonElement.class);
            // some prompts were stored as a JSON string holding the JSON
            if (root != null && root.isJsonPrimitive() && root.getAsJsonPrimitive().isString()) {
                root = GSON.fromJson(root.getAsString(), JsonElement.class);
            }
            if (root == null || !root.isJsonArray()) {
                return null;
            }
            List<Message> messages = new ArrayList<>();
            for (JsonElement element : (JsonArray) root) {
                if (!element.isJsonObject()) {
                    return null;
                }
                messages.add(toMessage(element.getAsJsonObject()));
            }
            return messages;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Message toMessage(JsonObject object) {
        String role = text(object, "role");
        StringBuilder content = new StringBuilder(text(object, "content") == null ? "" : text(object, "content"));
        for (String extra : new String[]{"attachments", "toolCalls", "toolCallId"}) {
            JsonElement value = object.get(extra);
            boolean empty = value == null || value.isJsonNull()
                    || (value.isJsonArray() && value.getAsJsonArray().isEmpty())
                    || (value.isJsonPrimitive() && value.getAsString().isEmpty());
            if (!empty) {
                content.append("\n[").append(extra).append("] ").append(GSON.toJson(value));
            }
        }
        return new Message(role, content.toString());
    }

    private static String text(JsonObject object, String name) {
        JsonElement value = object.get(name);
        return value == null || value.isJsonNull() ? null : value.getAsString();
    }

    public List<Row> getRows() {
        return rows;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    /**
     * @return the first position that differs (a position only one prompt has counts), -1 when they are the same
     */
    public int getFirstDifference() {
        return firstDifference;
    }

    /**
     * @return characters of the content that are shared at the start of the first differing message, 0 when there is none
     */
    public int getFirstDifferenceChar() {
        return firstDifferenceChar;
    }

    /**
     * @return characters of content of the new prompt that are the same as the earlier one from the start of the prompt
     */
    public long getSharedChars() {
        return sharedChars;
    }

    public long getCachedChars() {
        return cachedChars;
    }

    public long getCurrentChars() {
        return currentChars;
    }

    /**
     * @return the share (0 to 1) of the new prompt that a prefix cache of the earlier one covers; 1 for an empty prompt
     */
    public double getSharedShare() {
        return currentChars == 0 ? 1 : (double) sharedChars / currentChars;
    }

    public int getCachedMessages() {
        return cached.size();
    }

    public int getCurrentMessages() {
        return current.size();
    }
}
