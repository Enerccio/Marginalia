package com.github.enerccio.marginalia.extensions.betareaders.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.betareaders.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Builds the messages sent to the model for a chat. It touches no service: the system prompt arrives rendered, so the result is
 * the same text for the same chat, which keeps the cache of the prefix.
 * <ul>
 *   <li>The system message is the prompt (with the medium) and the description of how to answer.</li>
 *   <li>The history is the lines of the chat; the replies go back in the form they were asked for (the JSON or the text). The lines
 *       on the side of the user that follow each other (the note of a persona joining and the next message) are one message.</li>
 *   <li>The roster, the personas present with their descriptions, is added to the last message of the user. It is not stored, so the
 *       system prompt and the history never change when a persona joins or leaves, and the model still knows who is there.</li>
 * </ul>
 */
public final class PromptBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PromptBuilder() {
    }

    /**
     * How the request is made.
     *
     * @param json            the replies are JSON (else free text)
     * @param withReasoning   the reply starts with a short reasoning (JSON only; a model that reasons on its own does not)
     * @param maxMessages     the most messages the model may write in a turn; 0 or less for no limit
     * @param maxPromptTokens the most tokens the prompt may have; 0 or less for no limit
     * @param tokens          counts the tokens of a text
     */
    public record Options(boolean json, boolean withReasoning, int maxMessages, int maxPromptTokens, ToIntFunction<String> tokens) {

        public Options {
            tokens = tokens != null ? tokens : Options::estimate;
        }

        /** About four characters to a token. */
        public static int estimate(String text) {
            return text == null ? 0 : (text.length() + 3) / 4;
        }
    }

    /**
     * The messages to send.
     *
     * @param tokens  the tokens of the messages
     * @param dropped how many messages of the history were left out (the oldest) to fit the limit
     * @param fits    false when the prompt is over the limit even without the history
     */
    public record Prompt(List<LLMChatMessage> messages, int tokens, int dropped, boolean fits) {
    }

    /**
     * @param system the system prompt of the chat, rendered (the prompt of its kind with the medium in it)
     */
    public static Prompt build(BetaChat chat, String system, Options options) {
        String systemText = (system == null ? "" : system.strip()) + "\n\n" + answerFormat(chat, options);

        List<Entry> entries = new ArrayList<>();
        for (BetaMessage message : chat.getMessages()) {
            String text = textOf(message, options);
            if (text.isBlank()) {
                continue;
            }
            Entry last = entries.isEmpty() ? null : entries.getLast();
            if (message.isUserSide() && last != null && last.user()) {
                entries.set(entries.size() - 1, new Entry(true, last.text() + "\n\n" + text));
            } else {
                entries.add(new Entry(message.isUserSide(), text));
            }
        }

        // the roster goes to the last message of the user; when the history ends with a reply, it is a message of its own
        String roster = roster(chat);
        if (entries.isEmpty() || !entries.getLast().user()) {
            entries.add(new Entry(true, roster));
        } else if (!roster.isEmpty()) {
            Entry last = entries.removeLast();
            entries.add(new Entry(true, last.text() + "\n\n" + roster));
        }

        int dropped = 0;
        int limit = options.maxPromptTokens();
        if (limit > 0) {
            int total = count(systemText, options) + entries.stream().mapToInt(entry -> count(entry.text(), options)).sum();
            while (total > limit && entries.size() > 1) {
                total -= count(entries.removeFirst().text(), options);
                dropped++;
            }
            // the history has to begin with the user
            while (entries.size() > 1 && !entries.getFirst().user()) {
                total -= count(entries.removeFirst().text(), options);
                dropped++;
            }
        }

        List<LLMChatMessage> messages = new ArrayList<>();
        messages.add(LLMChatMessage.of(LLMRole.SYSTEM, systemText));
        entries.forEach(entry -> messages.add(LLMChatMessage.of(entry.user() ? LLMRole.USER : LLMRole.ASSISTANT, entry.text())));
        int tokens = messages.stream().mapToInt(message -> count(message.getContent(), options)).sum();
        return new Prompt(messages, tokens, dropped, limit <= 0 || tokens <= limit);
    }

    private record Entry(boolean user, String text) {
    }

    private static int count(String text, Options options) {
        // a few tokens go on the role and the framing of every message
        return options.tokens().applyAsInt(text) + 4;
    }

    /**
     * Tells the model how to write its reply; it is part of the system message, so it is the same for the whole chat.
     */
    static String answerFormat(BetaChat chat, Options options) {
        StringBuilder text = new StringBuilder("# How to answer\n");
        if (options.json()) {
            text.append("Answer with one JSON object and nothing else: ");
            if (options.withReasoning()) {
                text.append("{\"reasoning\": \"...\", ");
            } else {
                text.append("{");
            }
            text.append("\"messages\": [{\"speaker\": \"...\", \"parts\": [{\"type\": \"action\" or \"speech\", \"text\": \"...\"}]}]}.\n");
            if (options.withReasoning()) {
                text.append("The reasoning is short, a few sentences: who has something to say now and why. It comes first.\n");
            }
            text.append("The speaker is the name of a persona present or \"Narrator\" for a description of the scene. A message is made of "
                    + "parts that may alternate: an \"action\" is a gesture, an expression or a tone, \"speech\" is what is said. Do not "
                    + "write quotation marks or asterisks in the text.");
        } else {
            text.append("Write every message as a paragraph that starts with the speaker in bold, **Name:**, and then the message. "
                    + "Put gestures, expressions and tones between asterisks, like *nods slowly*; what is said is plain text. The "
                    + "speaker is the name of a persona present or \"Narrator\" for a description of the scene. Separate the messages "
                    + "with a blank line.");
        }
        if (options.maxMessages() > 0) {
            text.append("\nWrite at most ").append(options.maxMessages()).append(" messages in a turn.");
        }
        if (chat.getKind() == ChatKind.GROUP) {
            text.append("\nThe personas who join or leave the group are announced in the author's messages.");
        }
        return text.toString();
    }

    /**
     * The personas present with their descriptions, for the end of the last message of the user; empty when nobody is present.
     */
    static String roster(BetaChat chat) {
        List<Persona> present = chat.present();
        if (present.isEmpty()) {
            return "";
        }
        StringBuilder text = new StringBuilder(chat.getKind() == ChatKind.SINGLE
                ? "[The persona you play]" : "[The personas present]");
        for (Persona persona : present) {
            text.append("\n**").append(persona.getName()).append("**");
            if (persona.getDescription() != null && !persona.getDescription().isBlank()) {
                text.append(": ").append(persona.getDescription().strip());
            }
        }
        return text.toString();
    }

    private static String textOf(BetaMessage message, Options options) {
        return switch (message.getKind()) {
            case USER -> (message.getContext().isBlank() ? "" : message.getContext().strip() + "\n\n") + message.getText().strip();
            case JOIN -> presence(message, "joins the conversation");
            case LEAVE -> presence(message, "leaves the conversation");
            case ASSISTANT -> assistant(message, options);
        };
    }

    private static String presence(BetaMessage message, String what) {
        String name = message.getPersonaName() == null ? "" : message.getPersonaName();
        return "[" + name + " " + what + ".]" + (message.getText().isBlank() ? "" : " " + message.getText().strip());
    }

    /**
     * A reply of the model as it is sent back: the JSON of the schema or the text, whichever the chat asks for now.
     */
    private static String assistant(BetaMessage message, Options options) {
        if (message.getSpoken().isEmpty()) {
            return message.getRawText().strip();
        }
        if (!options.json()) {
            return TextFormat.render(message.getSpoken());
        }
        ObjectNode reply = MAPPER.createObjectNode();
        if (options.withReasoning()) {
            reply.put("reasoning", message.getReasoning());
        }
        ArrayNode messages = reply.putArray("messages");
        for (BetaSpoken spoken : message.getSpoken()) {
            ObjectNode node = messages.addObject();
            node.put("speaker", spoken.getSpeaker());
            ArrayNode parts = node.putArray("parts");
            for (BetaPart part : spoken.getParts()) {
                ObjectNode partNode = parts.addObject();
                partNode.put("type", part.getType() == PartType.ACTION ? "action" : "speech");
                partNode.put("text", part.getText());
            }
        }
        return reply.toString();
    }
}
