package com.github.enerccio.marginalia.utils;

import com.github.enerccio.marginalia.utils.SillyTavernChats.Alternative;
import com.github.enerccio.marginalia.utils.SillyTavernChats.Chat;
import com.github.enerccio.marginalia.utils.SillyTavernChats.Message;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Conversations saved by LM Studio ({@code ~/.lmstudio/conversations/<time>.conversation.json}), read into the {@link Chat} of
 * {@link SillyTavernChats}, so that they are joined into a tree ({@link SillyTavernChats#merge}) and stored like the chats of
 * SillyTavern are.
 * <p>
 * A conversation is a JSON object with a {@code messages} array. A message has {@code versions} (what LM Studio offers as the
 * regenerated answers or the edited text of a message: the alternatives of the part) and {@code currentlySelected}, the shown
 * one. A version of the user has {@code content} (blocks of {@code type: text}); a version of the assistant is made of
 * {@code steps}: the blocks with the thinking ({@code style.type == "thinking"}) are the reasoning, the other blocks with text are
 * the answer. The older {@code content} directly in an assistant version is read too. The system prompt of the conversation is not a
 * message and is not imported.
 */
public final class LmStudioChats {

    private static final String SUFFIX = ".conversation.json";

    private LmStudioChats() {
    }

    /**
     * Tells a conversation of LM Studio from a chat of SillyTavern (a line of JSON per message) in a file whose kind is not known: a
     * conversation is one JSON object with a {@code messages} array.
     */
    public static boolean isConversation(String text) {
        String start = text == null ? "" : text.stripLeading();
        if (start.startsWith("\uFEFF")) {
            start = start.substring(1).stripLeading();
        }
        if (!start.startsWith("{")) {
            return false;
        }
        try {
            JsonElement element = JsonParser.parseString(start);
            return element.isJsonObject() && element.getAsJsonObject().has("messages") && element.getAsJsonObject().get("messages").isJsonArray();
        } catch (RuntimeException e) {
            // several lines of JSON are not one document
            return false;
        }
    }

    /**
     * Reads a conversation.
     *
     * @param fileName the name of the uploaded file, used as the name of the chat when the conversation has none
     * @throws IllegalArgumentException when the text is not a conversation of LM Studio
     */
    public static Chat parse(String text, String fileName) {
        JsonObject json;
        try {
            JsonElement element = JsonParser.parseString(text);
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException("Not an LM Studio conversation: not a JSON object");
            }
            json = element.getAsJsonObject();
        } catch (RuntimeException e) {
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new IllegalArgumentException("Not an LM Studio conversation: not JSON", e);
        }
        if (!json.has("messages") || !json.get("messages").isJsonArray()) {
            throw new IllegalArgumentException("Not an LM Studio conversation: no messages");
        }

        // LM Studio dates a conversation, not its messages: the chat written last decides what is shown
        Instant sent = instant(json, "userLastMessagedAt");
        if (sent == null) {
            sent = instant(json, "createdAt");
        }
        List<Message> messages = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray("messages")) {
            if (element.isJsonObject()) {
                Message message = readMessage(element.getAsJsonObject(), sent);
                if (message != null) {
                    messages.add(message);
                }
            }
        }
        String name = string(json, "name");
        if (StringUtils.isBlank(name)) {
            name = fileName == null ? "" : fileName.trim();
            if (name.toLowerCase().endsWith(SUFFIX)) {
                name = name.substring(0, name.length() - SUFFIX.length());
            } else if (name.toLowerCase().endsWith(".json")) {
                name = name.substring(0, name.length() - ".json".length());
            }
        }
        return new Chat(name.strip(), null, messages);
    }

    private static Message readMessage(JsonObject json, Instant sent) {
        JsonArray versions = json.has("versions") && json.get("versions").isJsonArray() ? json.getAsJsonArray("versions") : new JsonArray();
        if (versions.isEmpty()) {
            return null;
        }
        int selected = json.has("currentlySelected") && json.get("currentlySelected").isJsonPrimitive() ? json.get("currentlySelected").getAsInt() : 0;
        if (selected < 0 || selected >= versions.size()) {
            selected = versions.size() - 1;
        }
        JsonObject shown = versions.get(selected).isJsonObject() ? versions.get(selected).getAsJsonObject() : null;
        if (shown == null) {
            return null;
        }
        String role = string(shown, "role");
        // the system prompt and the results of tools are not a part of the story
        if ("system".equals(role) || "tool".equals(role)) {
            return null;
        }
        boolean user = "user".equals(role);

        List<Alternative> alternatives = new ArrayList<>();
        int active = 0;
        for (int i = 0; i < versions.size(); i++) {
            JsonObject version = versions.get(i).isJsonObject() ? versions.get(i).getAsJsonObject() : null;
            if (version == null) {
                continue;
            }
            if (i == selected) {
                active = alternatives.size();
            }
            alternatives.add(alternative(version));
        }
        String name = null;
        JsonObject sender = object(shown, "senderInfo");
        if (sender != null && !user) {
            name = string(sender, "senderName");
        }
        return new Message(user, StringUtils.isBlank(name) ? null : name.strip(), alternatives, active, sent);
    }

    private static Alternative alternative(JsonObject version) {
        StringBuilder answer = new StringBuilder();
        StringBuilder reasoning = new StringBuilder();
        String model = null;

        JsonArray steps = version.has("steps") && version.get("steps").isJsonArray() ? version.getAsJsonArray("steps") : null;
        if (steps == null) {
            // the older and the simple form: the text is the content of the version
            append(answer, text(version.get("content")));
        } else {
            for (JsonElement element : steps) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject step = element.getAsJsonObject();
                if (step.has("type") && !"contentBlock".equals(string(step, "type"))) {
                    // the calls of tools and the debug information
                    continue;
                }
                JsonObject style = object(step, "style");
                boolean thinking = style != null && "thinking".equals(string(style, "type"));
                append(thinking ? reasoning : answer, text(step.get("content")));
                if (model == null && !thinking) {
                    model = model(step);
                }
            }
        }
        if (model == null) {
            JsonObject sender = object(version, "senderInfo");
            model = sender == null ? null : string(sender, "senderName");
        }
        String reasoningText = reasoning.toString().strip();
        return new Alternative(answer.toString().strip(), reasoningText.isEmpty() ? null : reasoningText,
                StringUtils.isBlank(model) ? null : model.strip());
    }

    /**
     * @return the identifier of the model that wrote the block, null when the block does not tell
     */
    private static String model(JsonObject step) {
        JsonObject info = object(step, "genInfo");
        if (info == null) {
            return null;
        }
        String model = string(info, "identifier");
        return StringUtils.isBlank(model) ? string(info, "indexedModelIdentifier") : model;
    }

    private static void append(StringBuilder target, String text) {
        if (text.isBlank()) {
            return;
        }
        if (!target.isEmpty()) {
            target.append("\n\n");
        }
        target.append(text.strip());
    }

    /**
     * The text of the content of a version or of a block: a text, or an array of blocks of which the ones of type {@code text} count
     * (the files and the images that were attached are not text).
     */
    private static String text(JsonElement content) {
        if (content == null || content.isJsonNull()) {
            return "";
        }
        if (content.isJsonPrimitive()) {
            return content.getAsString();
        }
        if (!content.isJsonArray()) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        for (JsonElement element : content.getAsJsonArray()) {
            if (element.isJsonPrimitive()) {
                text.append(element.getAsString());
            } else if (element.isJsonObject()) {
                JsonObject block = element.getAsJsonObject();
                String type = string(block, "type");
                if ((type == null || "text".equals(type)) && block.has("text")) {
                    text.append(StringUtils.defaultString(string(block, "text")));
                }
            }
        }
        return text.toString();
    }

    private static Instant instant(JsonObject json, String key) {
        JsonElement element = json.get(key);
        if (element == null || !element.isJsonPrimitive()) {
            return null;
        }
        try {
            return Instant.ofEpochMilli(element.getAsLong());
        } catch (RuntimeException e) {
            return null;
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
}
