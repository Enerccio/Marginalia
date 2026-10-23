package com.github.enerccio.marginalia.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.*;

/**
 * Chats exported by SillyTavern ({@code .jsonl}: a header line with {@code chat_metadata}, then a message per line) and the
 * tree made of several of them. A branch or checkpoint of SillyTavern is a copy of the chat up to a message, so chats of one
 * story share their beginning: {@link #merge} puts the chats into one trie, the shared messages are stored once and the chats
 * part where their messages differ. The swipes (alternative answers) of a message are siblings in the tree.
 * <p>
 * A part of the story is a turn: the instructions the author gave and the text that followed. With {@code mergeTurns} the
 * message of the user (the messages, when several follow each other) is the instructions of the part made of the answer after
 * it; without, every message is a part of its own with its text as the text of the part.
 */
public final class SillyTavernChats {

    /**
     * One text of a message: its only text or one of its swipes.
     *
     * @param reasoning the reasoning of the model, null for none
     * @param model     the model that wrote it, null when not known
     */
    public record Alternative(String text, String reasoning, String model) {
    }

    /**
     * A message of a chat; {@code alternatives} are the swipes ({@code active} is the shown one), a message without swipes has
     * only one.
     *
     * @param name the name of the speaker (the persona of the user or the character), null when the message has none
     */
    public record Message(boolean user, String name, List<Alternative> alternatives, int active, Instant sent) {

        /**
         * The text that is shown, the active swipe.
         */
        public Alternative shown() {
            return alternatives.get(Math.min(Math.max(active, 0), alternatives.size() - 1));
        }
    }

    /**
     * @param name          the name of the file without the extension
     * @param characterName the character of the chat, null when the header does not tell
     */
    public record Chat(String name, String characterName, List<Message> messages) {

        /**
         * The time of the last message, null when it has none or no usable one.
         */
        public Instant lastSent() {
            return messages.isEmpty() ? null : messages.getLast().sent();
        }
    }

    /**
     * A part of the tree, the same turn in every chat that has it. {@code instructions} is null for a part without them.
     */
    public static final class Node {
        private final Node parent;
        private final String instructions;
        private final Alternative alternative;
        private final Map<Key, Node> children = new LinkedHashMap<>();

        private Node(Node parent, String instructions, Alternative alternative) {
            this.parent = parent;
            this.instructions = instructions;
            this.alternative = alternative;
        }

        public Node parent() {
            return parent;
        }

        public String instructions() {
            return instructions;
        }

        public Alternative alternative() {
            return alternative;
        }

        /**
         * @return the continuations, the swipes of the next message in the order they were met
         */
        public List<Node> children() {
            return new ArrayList<>(children.values());
        }
    }

    /**
     * @param roots      the first parts, several when the chats don't begin with the same message
     * @param activeLeaf the end of the branch to show: the last part of the chat that was written last; null for no chats
     * @param size       how many parts there are
     */
    public record Tree(List<Node> roots, Node activeLeaf, int size) {

        /**
         * The parts in the order they are stored in: every part after its parent, the continuations of a part (the swipes of
         * a message) in the order of the messages, so they keep their order in the book.
         */
        public List<Node> inStoringOrder() {
            List<Node> order = new ArrayList<>(size);
            Deque<Node> stack = new ArrayDeque<>();
            for (int i = roots.size() - 1; i >= 0; i--) {
                stack.push(roots.get(i));
            }
            while (!stack.isEmpty()) {
                Node node = stack.pop();
                order.add(node);
                List<Node> children = node.children();
                for (int i = children.size() - 1; i >= 0; i--) {
                    stack.push(children.get(i));
                }
            }
            return order;
        }
    }

    private record Key(String instructions, String text) {
    }

    /**
     * A message in the unit the tree is made of: the instructions it has (the messages of the user before it, merged) and
     * the texts it can be.
     */
    private record Turn(String instructions, List<Alternative> alternatives, int active) {
    }

    private SillyTavernChats() {
    }

    /**
     * Reads a chat file.
     *
     * @param fileName the name of the uploaded file, a {@code .jsonl} ending is cut off to name the chat
     * @throws IllegalArgumentException when the text is not a chat of SillyTavern
     */
    public static Chat parse(String text, String fileName) {
        List<JsonObject> lines = new ArrayList<>();
        for (String line : text.split("\\R")) {
            if (line.isBlank()) {
                continue;
            }
            try {
                JsonElement element = JsonParser.parseString(line);
                if (element.isJsonObject()) {
                    lines.add(element.getAsJsonObject());
                    continue;
                }
            } catch (RuntimeException e) {
                // handled below
            }
            throw new IllegalArgumentException("Not a SillyTavern chat: a line is not a JSON object");
        }
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Not a SillyTavern chat: empty file");
        }
        JsonObject header = lines.getFirst();
        boolean hasHeader = !header.has("mes") && (header.has("user_name") || header.has("name") || header.has("chat_metadata"));
        if (!hasHeader && !header.has("mes")) {
            throw new IllegalArgumentException("Not a SillyTavern chat: no header and no message");
        }

        List<Message> messages = new ArrayList<>();
        for (int i = hasHeader ? 1 : 0; i < lines.size(); i++) {
            Message message = readMessage(lines.get(i));
            if (message != null) {
                messages.add(message);
            }
        }
        String characterName = hasHeader ? string(header, "character_name") : null;
        if ("unused".equals(characterName)) {
            // group chats and some exports have no character
            characterName = null;
        }
        String name = fileName == null ? "" : fileName.trim();
        if (name.toLowerCase().endsWith(".jsonl")) {
            name = name.substring(0, name.length() - ".jsonl".length());
        }
        return new Chat(name, characterName, messages);
    }

    private static Message readMessage(JsonObject json) {
        // the hidden messages (comments, the narrator's notes) are not a part of the story
        if (json.has("is_system") && json.get("is_system").isJsonPrimitive() && json.get("is_system").getAsBoolean()) {
            return null;
        }
        if (!json.has("mes") || !json.get("mes").isJsonPrimitive()) {
            return null;
        }
        boolean user = json.has("is_user") && json.get("is_user").isJsonPrimitive() && json.get("is_user").getAsBoolean();
        String shown = json.get("mes").getAsString();
        JsonArray swipes = json.has("swipes") && json.get("swipes").isJsonArray() ? json.getAsJsonArray("swipes") : new JsonArray();
        JsonArray swipeInfo = json.has("swipe_info") && json.get("swipe_info").isJsonArray() ? json.getAsJsonArray("swipe_info") : new JsonArray();

        List<Alternative> alternatives = new ArrayList<>();
        int active = 0;
        if (swipes.size() > 1) {
            int swipeId = json.has("swipe_id") && json.get("swipe_id").isJsonPrimitive() ? json.get("swipe_id").getAsInt() : swipes.size() - 1;
            active = swipeId >= 0 && swipeId < swipes.size() ? swipeId : swipes.size() - 1;
            for (int i = 0; i < swipes.size(); i++) {
                String swipe = swipes.get(i).isJsonPrimitive() ? swipes.get(i).getAsString() : "";
                // the shown text is the edited text of the active swipe
                String swipeText = i == active && !shown.isBlank() ? shown : swipe;
                JsonObject extra = i < swipeInfo.size() && swipeInfo.get(i).isJsonObject() ? object(swipeInfo.get(i).getAsJsonObject(), "extra") : null;
                alternatives.add(alternative(swipeText, extra));
            }
        } else {
            alternatives.add(alternative(shown, object(json, "extra")));
        }
        String name = string(json, "name");
        return new Message(user, StringUtils.isBlank(name) ? null : name.strip(), alternatives, active, sent(json));
    }

    private static Alternative alternative(String text, JsonObject extra) {
        String reasoning = extra == null ? null : string(extra, "reasoning");
        String model = extra == null ? null : string(extra, "model");
        return new Alternative(text, StringUtils.isBlank(reasoning) ? null : reasoning, StringUtils.isBlank(model) ? null : model);
    }

    private static Instant sent(JsonObject json) {
        String date = string(json, "send_date");
        if (date == null) {
            return null;
        }
        try {
            return Instant.parse(date);
        } catch (RuntimeException e) {
            try {
                return Instant.ofEpochMilli(Long.parseLong(date.trim()));
            } catch (NumberFormatException ignored) {
                // an old human readable date, no use for the order
                return null;
            }
        }
    }

    private static String string(JsonObject json, String key) {
        JsonElement element = json.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }

    private static JsonObject object(JsonObject json, String key) {
        JsonElement element = json.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    /**
     * Puts the chats into one tree. Two chats go through the same part while their turns are the same (the same instructions and
     * text), then they branch; the swipes of a message are the children of the part before it, the chats continue under the
     * swipe they had selected.
     *
     * @param mergeTurns see the class
     */
    public static Tree merge(List<Chat> chats, boolean mergeTurns) {
        Node root = new Node(null, null, null);
        int size = 0;
        Node activeLeaf = null;
        Instant activeSent = null;
        for (Chat chat : chats) {
            Node current = root;
            for (Turn turn : turns(chat, mergeTurns)) {
                Node selected = null;
                for (int i = 0; i < turn.alternatives().size(); i++) {
                    Alternative alternative = turn.alternatives().get(i);
                    Key key = new Key(turn.instructions(), alternative.text());
                    Node child = current.children.get(key);
                    if (child == null) {
                        child = new Node(current == root ? null : current, turn.instructions(), alternative);
                        current.children.put(key, child);
                        size++;
                    }
                    if (i == turn.active()) {
                        selected = child;
                    }
                }
                current = selected;
            }
            if (current != root) {
                // the chat written last decides what is shown, a chat without dates is taken as written after the others
                Instant sent = chat.lastSent();
                if (activeLeaf == null || sent == null || activeSent == null || !sent.isBefore(activeSent)) {
                    activeLeaf = current;
                    activeSent = sent;
                }
            }
        }
        return new Tree(root.children(), activeLeaf, size);
    }

    private static List<Turn> turns(Chat chat, boolean mergeTurns) {
        List<Turn> turns = new ArrayList<>();
        StringBuilder instructions = new StringBuilder();
        for (Message message : chat.messages()) {
            List<Alternative> alternatives = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            for (Alternative alternative : message.alternatives()) {
                // the swipes that were never written and the same text twice are no use
                if (!alternative.text().isBlank() && seen.add(alternative.text())) {
                    alternatives.add(alternative);
                }
            }
            if (alternatives.isEmpty()) {
                continue;
            }
            String shown = message.shown().text();
            int active = alternatives.size() - 1;
            for (int i = 0; i < alternatives.size(); i++) {
                if (alternatives.get(i).text().equals(shown)) {
                    active = i;
                    break;
                }
            }

            if (mergeTurns && message.user()) {
                if (!instructions.isEmpty()) {
                    instructions.append("\n\n");
                }
                instructions.append(shown.strip());
                continue;
            }
            turns.add(new Turn(mergeTurns && !instructions.isEmpty() ? instructions.toString() : null, alternatives, active));
            instructions.setLength(0);
        }
        if (!instructions.isEmpty()) {
            // the last message of the user has no answer yet
            turns.add(new Turn(instructions.toString(), List.of(new Alternative("", null, null)), 0));
        }
        return turns;
    }
}
