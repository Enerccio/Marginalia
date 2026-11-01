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
 *       what was read). While the reply streams the message being written is returned too, with the text it has so far, once it is
 *       known who speaks and the part that is written has its type, so nothing changes under the eyes of the reader.</li>
 *   <li>Other text is read as free text: a message starts with the name of a speaker in bold ({@code **Athena:**}), what follows is
 *       its parts, an action between asterisks and the rest speech. Only a name of a speaker that is present (or the narrator) starts a
 *       message, so a bold word in the middle of a speech does not. The last message is shown as it grows; an action that is not closed
 *       yet is left out until it is.</li>
 *   <li>A reply that is neither is returned as it was written.</li>
 * </ul>
 * The format is judged from the text, not from what the chat asked for: a model that ignores the instructions is still read.
 */
public final class ReplyReader {

    /** {@code **Name:** rest} or {@code **Name**: rest}, on one line. */
    private static final Pattern HEADER = Pattern.compile("^\\s*\\*\\*\\s*([^*\\n]{1,60}?)\\s*(?::\\s*\\*\\*|\\*\\*\\s*:)\\s*(.*)$");

    /** A reasoning at the start of the text: {@code <think>...</think>} (or {@code <thinking>}). */
    private static final Pattern THINK = Pattern.compile("^\\s*<(think|thinking)>(.*?)</\\1>", Pattern.DOTALL);

    /** A reasoning at the start of the text that is not closed. */
    private static final Pattern THINK_OPEN = Pattern.compile("^\\s*<(?:think|thinking)>(.*)$", Pattern.DOTALL);

    /** The start of the JSON of a reply after a sentence of introduction: an object that begins with one of its two keys. */
    private static final Pattern JSON_KEYS = Pattern.compile("\\{\\s*\"(?:reasoning|messages)\"");

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
        // a model that is not held to a schema may think in the text first, between think tags
        Matcher think = THINK.matcher(reply);
        if (think.find()) {
            Reading body = readBody(reply.substring(think.end()), done, speakers, maxMessages);
            return body.reasoning().isBlank()
                    ? new Reading(think.group(2).strip(), body.messages(), body.rawText(), body.dropped()) : body;
        }
        Matcher open = THINK_OPEN.matcher(reply);
        if (open.find()) {
            // still thinking (or cut in the middle of it): what is thought so far is the reasoning
            return new Reading(open.group(1).strip(), List.of(), "", 0);
        }
        return readBody(reply, done, speakers, maxMessages);
    }

    private static Reading readBody(String reply, boolean done, List<String> speakers, int maxMessages) {
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
        if (head.startsWith("{") || head.startsWith("[")) {
            return offset;
        }
        // a sentence of introduction before the JSON ("Here is the reply:")
        Matcher keys = JSON_KEYS.matcher(text);
        return keys.find() ? keys.start() : -1;
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

        int finished = done ? array.size() : Math.min(array.size(), ends.size());
        List<BetaSpoken> messages = new ArrayList<>();
        for (int i = 0; i < finished; i++) {
            BetaSpoken spoken = spoken(array.get(i), speakers, false);
            if (spoken != null) {
                messages.add(spoken);
            }
        }
        if (finished < array.size()) {
            // the message that is being written
            BetaSpoken writing = spoken(array.get(finished), speakers, true);
            if (writing != null) {
                messages.add(writing);
            }
        }
        return limited(reasoning, messages, maxMessages);
    }

    /**
     * @param writing the message is being written: it is only read when it is known who speaks, and a part only when its type is
     *                written in full (a part that begins as speech and turns out to be an action would change under the reader)
     */
    private static BetaSpoken spoken(JsonNode node, List<String> speakers, boolean writing) {
        if (!node.isObject() || (writing && node.path("speaker").asText("").isBlank())) {
            return null;
        }
        List<BetaPart> parts = new ArrayList<>();
        for (JsonNode partNode : node.path("parts")) {
            String text = partNode.path("text").asText("");
            if (text.isBlank()) {
                continue;
            }
            String typeName = partNode.path("type").asText("");
            if (writing && !"action".equalsIgnoreCase(typeName) && !"speech".equalsIgnoreCase(typeName)) {
                continue;
            }
            PartType type = "action".equalsIgnoreCase(typeName) ? PartType.ACTION : PartType.SPEECH;
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
        for (int i = 0; i < contents.size(); i++) {
            // the message that is being written: an action that is not closed yet waits
            String content = !done && i == contents.size() - 1 ? withoutOpenAction(contents.get(i).toString()) : contents.get(i).toString();
            List<BetaPart> parts = parts(content);
            if (!parts.isEmpty()) {
                messages.add(new BetaSpoken(names.get(i), parts));
            }
        }
        return limited("", messages, maxMessages);
    }

    /**
     * The text up to an action that is opened with an asterisk and not closed yet; the text as it is when there is none.
     */
    private static String withoutOpenAction(String content) {
        Matcher action = ACTION.matcher(content);
        int last = 0;
        while (action.find()) {
            last = action.end();
        }
        int open = content.indexOf('*', last);
        return open < 0 ? content : content.substring(0, open);
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
