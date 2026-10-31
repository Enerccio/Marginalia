package com.github.enerccio.marginalia.extensions.betareaders.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.enerccio.marginalia.domain.service.structured.PartialJson;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaMessage;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaPart;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaSpoken;
import com.github.enerccio.marginalia.extensions.betareaders.model.PartType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads a reply of the model into messages, also while it is still being written.
 * <ul>
 *   <li>A reply that starts with JSON is read as the schema says (through {@link PartialJson}, so a reply that is cut or broken keeps
 *       what was read). While the reply streams only the messages that are finished are returned: the one being written is hidden.</li>
 *   <li>Other text is read as free text: a message starts with the name of a speaker in bold ({@code **Athena:**}), what follows is
 *       its parts, an action between asterisks and the rest speech. Only a name of a speaker that is present (or the narrator) starts a
 *       message, so a bold word in the middle of a speech does not. The last message is unfinished until the reply is done.</li>
 *   <li>A reply that is neither is returned as it was written.</li>
 * </ul>
 * The format is judged from the text, not from what the chat asked for: a model that ignores the instructions is still read.
 */
public final class ReplyReader {

    /** {@code **Name:** rest} or {@code **Name**: rest}, on one line. */
    private static final Pattern HEADER = Pattern.compile("^\\s*\\*\\*\\s*([^*\\n]{1,60}?)\\s*(?::\\s*\\*\\*|\\*\\*\\s*:)\\s*(.*)$");

    /** An action: text between single asterisks (not the bold of {@code **}, not inside a word). */
    private static final Pattern ACTION = Pattern.compile("(?<![*\\w])\\*(?!\\*)(.+?)(?<!\\*)\\*(?![*\\w])", Pattern.DOTALL);

    private ReplyReader() {
    }

    /**
     * What a reading found.
     *
     * @param reasoning the short reasoning (JSON only), as far as it has been written
     * @param messages  the messages to show, in order; only finished ones while the reply is not done
     * @param rawText   the reply as it was written when it could not be read as messages and is done; empty otherwise
     * @param dropped   how many messages were over the limit and left out
     */
    public record Reading(String reasoning, List<BetaSpoken> messages, String rawText, int dropped) {

        public Reading {
            messages = List.copyOf(messages);
        }

        /** Whether there is anything to show. */
        public boolean isEmpty() {
            return reasoning.isBlank() && messages.isEmpty() && rawText.isBlank();
        }

        /** The reply as the line of the history. */
        public BetaMessage toMessage() {
            BetaMessage message = BetaMessage.assistant(reasoning, messages);
            message.setRawText(rawText);
            return message;
        }
    }

    /**
     * @param text        what the model wrote so far
     * @param done        the reply is complete (or cut for good): the last message is a message too
     * @param speakers    the names of the personas that may speak; the narrator always may
     * @param maxMessages the most messages of a reply, the rest is dropped; 0 or less for no limit
     */
    public static Reading read(String text, boolean done, List<String> speakers, int maxMessages) {
        String reply = text == null ? "" : text;
        int jsonStart = jsonStart(reply);
        Reading reading = jsonStart >= 0 ? readJson(reply, jsonStart, done, speakers, maxMessages)
                : readText(reply, done, speakers, maxMessages);
        if (jsonStart >= 0 && done && reading.messages().isEmpty() && reading.reasoning().isBlank()) {
            // text that starts with a bracket is not necessarily JSON
            Reading asText = readText(reply, true, speakers, maxMessages);
            if (!asText.messages().isEmpty()) {
                return asText;
            }
        }
        if (reading.messages().isEmpty() && reading.reasoning().isBlank() && done && !reply.isBlank()) {
            return new Reading("", List.of(), reply.strip(), 0);
        }
        return reading;
    }

    // ------------------------------------------------------------------------------------------------------------
    // JSON
    // ------------------------------------------------------------------------------------------------------------

    /** Where the JSON starts: the first brace or bracket, when only a fence of a markdown block is before it. */
    private static int jsonStart(String text) {
        String head = text.stripLeading();
        int offset = text.length() - head.length();
        if (head.startsWith("```")) {
            int newline = head.indexOf('\n');
            if (newline < 0) {
                return -1;
            }
            offset += newline + 1;
            head = text.substring(offset).stripLeading();
            offset = text.length() - head.length();
        }
        return head.startsWith("{") || head.startsWith("[") ? offset : -1;
    }

    private static Reading readJson(String text, int start, boolean done, List<String> speakers, int maxMessages) {
        String json = text.substring(start);
        List<Integer> ends = messageEnds(json);
        JsonNode root = PartialJson.parse(text);
        if (root == null && !ends.isEmpty()) {
            // the reply is broken after some messages: what came before is kept
            root = PartialJson.parse(json.substring(0, ends.getLast() + 1));
        }
        if (root == null) {
            return empty();
        }
        String reasoning = root.isObject() ? root.path("reasoning").asText("") : "";
        JsonNode array = root.isArray() ? root : root.path("messages");
        if (!array.isArray()) {
            return new Reading(reasoning, List.of(), "", 0);
        }

        int available = done ? array.size() : Math.min(array.size(), ends.size());
        List<BetaSpoken> messages = new ArrayList<>();
        for (int i = 0; i < available; i++) {
            BetaSpoken spoken = spoken(array.get(i), speakers);
            if (spoken != null) {
                messages.add(spoken);
            }
        }
        return limited(reasoning, messages, maxMessages);
    }

    private static BetaSpoken spoken(JsonNode node, List<String> speakers) {
        if (!node.isObject()) {
            return null;
        }
        List<BetaPart> parts = new ArrayList<>();
        for (JsonNode partNode : node.path("parts")) {
            String text = partNode.path("text").asText("");
            if (text.isBlank()) {
                continue;
            }
            PartType type = "action".equalsIgnoreCase(partNode.path("type").asText("")) ? PartType.ACTION : PartType.SPEECH;
            parts.add(new BetaPart(type, text.strip()));
        }
        if (parts.isEmpty()) {
            return null;
        }
        return new BetaSpoken(canonical(node.path("speaker").asText(""), speakers), parts);
    }

    /**
     * Where the messages that have been closed in the JSON so far end (the index of their closing brace). The messages are the objects
     * directly in the array of the root object ({@code {"messages": [ {...}, ...]}}), or of the root array.
     */
    public static List<Integer> messageEnds(String json) {
        List<Integer> ends = new ArrayList<>();
        if (json.isEmpty()) {
            return ends;
        }
        int target = json.charAt(0) == '[' ? 1 : 2;
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            switch (c) {
                case '"' -> inString = true;
                case '{', '[' -> depth++;
                case ']' -> depth--;
                case '}' -> {
                    depth--;
                    if (depth == target) {
                        ends.add(i);
                    }
                }
                default -> {
                }
            }
        }
        return ends;
    }

    // ------------------------------------------------------------------------------------------------------------
    // Free text
    // ------------------------------------------------------------------------------------------------------------

    private static Reading readText(String text, boolean done, List<String> speakers, int maxMessages) {
        List<String> names = new ArrayList<>();
        List<StringBuilder> contents = new ArrayList<>();
        StringBuilder before = new StringBuilder();

        for (String line : text.split("\n", -1)) {
            Matcher header = HEADER.matcher(line);
            String name = header.matches() ? canonicalKnown(header.group(1), speakers) : null;
            if (name != null) {
                names.add(name);
                contents.add(new StringBuilder(header.group(2)));
            } else if (contents.isEmpty()) {
                before.append(line).append('\n');
            } else {
                contents.getLast().append('\n').append(line);
            }
        }

        List<BetaSpoken> messages = new ArrayList<>();
        // what comes before the first speaker (an introduction) is not lost: it is the narrator's
        if (!contents.isEmpty() && !before.toString().isBlank()) {
            BetaSpoken introduction = new BetaSpoken(BetaSpoken.NARRATOR, parts(before.toString()));
            if (!introduction.getParts().isEmpty()) {
                messages.add(introduction);
            }
        }
        int available = done ? contents.size() : contents.size() - 1;
        for (int i = 0; i < available; i++) {
            List<BetaPart> parts = parts(contents.get(i).toString());
            if (!parts.isEmpty()) {
                messages.add(new BetaSpoken(names.get(i), parts));
            }
        }
        return limited("", messages, maxMessages);
    }

    /**
     * The parts of the text of a message: an action between asterisks, the rest speech.
     */
    static List<BetaPart> parts(String content) {
        List<BetaPart> parts = new ArrayList<>();
        Matcher action = ACTION.matcher(content);
        int last = 0;
        while (action.find()) {
            addSpeech(parts, content.substring(last, action.start()));
            String text = action.group(1).strip();
            if (!text.isEmpty()) {
                parts.add(new BetaPart(PartType.ACTION, text));
            }
            last = action.end();
        }
        addSpeech(parts, content.substring(last));
        return parts;
    }

    private static void addSpeech(List<BetaPart> parts, String text) {
        if (!text.isBlank()) {
            parts.add(new BetaPart(PartType.SPEECH, text.strip()));
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // Both
    // ------------------------------------------------------------------------------------------------------------

    private static Reading limited(String reasoning, List<BetaSpoken> messages, int maxMessages) {
        if (maxMessages > 0 && messages.size() > maxMessages) {
            return new Reading(reasoning, messages.subList(0, maxMessages), "", messages.size() - maxMessages);
        }
        return new Reading(reasoning, messages, "", 0);
    }

    private static Reading empty() {
        return new Reading("", List.of(), "", 0);
    }

    /**
     * The speaker as the chat names it (the case and the spacing of the model are not trusted); a name that is not known is kept as
     * it was written, so the message is not lost.
     */
    private static String canonical(String name, List<String> speakers) {
        String known = canonicalKnown(name, speakers);
        return known != null ? known : name.strip();
    }

    private static String canonicalKnown(String name, List<String> speakers) {
        String key = BetaReadersStore.nameKey(name);
        for (String speaker : BetaSchemas.allowed(speakers)) {
            if (BetaReadersStore.nameKey(speaker).equals(key)) {
                return speaker;
            }
        }
        return null;
    }
}
