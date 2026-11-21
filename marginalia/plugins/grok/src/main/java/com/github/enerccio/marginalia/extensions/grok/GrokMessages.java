package com.github.enerccio.marginalia.extensions.grok;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.enerccio.marginalia.domain.service.function.FunctionDefinition;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.github.enerccio.marginalia.extensions.grok.GrokClient.MAPPER;

/**
 * The payload of the application as the {@code messages} of a chat completion, and the other JSON parts of the request (tools,
 * the response format). The system messages stay where they are (xAI takes them anywhere), a message of nothing is left out.
 */
final class GrokMessages {

    /** The ids of calls that the model did not name, made up here (xAI always names them, a proxy may not). */
    static final String GENERATED_ID_PREFIX = "call_gen_";

    private GrokMessages() {
    }

    static ArrayNode messages(List<LLMChatMessage> payload) {
        ArrayNode messages = MAPPER.createArrayNode();
        for (LLMChatMessage message : payload) {
            if (message.getContent() == null && !message.hasAttachments() && !message.hasToolCalls()) {
                continue;
            }
            switch (message.getRole()) {
                case SYSTEM -> {
                    if (StringUtils.isNotBlank(message.getContent())) {
                        messages.addObject().put("role", "system").put("content", message.getContent());
                    }
                }
                case USER -> messages.add(user(message));
                case ASSISTANT -> messages.add(assistant(message));
                case TOOL -> {
                    if (message.getToolCallId() == null) {
                        throw new IllegalArgumentException("Tool message has no toolCallId");
                    }
                    messages.addObject().put("role", "tool").put("tool_call_id", message.getToolCallId())
                            .put("content", StringUtils.defaultString(message.getContent()));
                }
            }
        }
        return messages;
    }

    private static ObjectNode user(LLMChatMessage message) {
        ObjectNode node = MAPPER.createObjectNode().put("role", "user");
        if (!message.hasAttachments()) {
            node.put("content", StringUtils.defaultString(message.getContent()));
            return node;
        }
        ArrayNode parts = node.putArray("content");
        // the images of an AI that wants them before the text (its image placement) come first
        for (LLMAttachment attachment : message.getAttachmentsBeforeText()) {
            parts.add(attachmentPart(attachment));
        }
        if (StringUtils.isNotEmpty(message.getContent())) {
            parts.addObject().put("type", "text").put("text", message.getContent());
        }
        for (LLMAttachment attachment : message.getAttachmentsAfterText()) {
            parts.add(attachmentPart(attachment));
        }
        return node;
    }

    private static ObjectNode attachmentPart(LLMAttachment attachment) {
        String mime = StringUtils.defaultIfBlank(attachment.getMimeType(), "application/octet-stream").toLowerCase(Locale.ROOT);
        if (attachment.getData() == null && attachment.getUrl() == null) {
            throw new IllegalArgumentException("Attachment has neither data nor url");
        }
        if (mime.equals("image/jpeg") || mime.equals("image/png")) {
            ObjectNode part = MAPPER.createObjectNode().put("type", "image_url");
            part.putObject("image_url").put("url", attachment.getData() != null ? attachment.toDataUrl() : attachment.getUrl());
            return part;
        }
        if (mime.startsWith("text/") && attachment.getData() != null) {
            return MAPPER.createObjectNode().put("type", "text")
                    .put("text", new String(attachment.getData(), java.nio.charset.StandardCharsets.UTF_8));
        }
        throw new IllegalArgumentException("Grok can read pictures (JPEG, PNG) and text files, not " + mime);
    }

    /** The assistant message that called functions (or answered): what it wrote and the calls. */
    static ObjectNode assistant(LLMChatMessage message) {
        ObjectNode node = MAPPER.createObjectNode().put("role", "assistant");
        if (message.getContent() != null) {
            node.put("content", message.getContent());
        } else {
            node.put("content", "");
        }
        if (message.hasToolCalls()) {
            ArrayNode calls = node.putArray("tool_calls");
            for (LLMToolCall call : message.getToolCalls()) {
                calls.add(toolCall(call));
            }
        }
        return node;
    }

    static ObjectNode toolCall(LLMToolCall call) {
        ObjectNode node = MAPPER.createObjectNode().put("id", call.getId()).put("type", "function");
        node.putObject("function").put("name", call.getName()).put("arguments", StringUtils.defaultIfBlank(call.getArguments(), "{}"));
        return node;
    }

    static ObjectNode toolResult(String callId, String result) {
        return MAPPER.createObjectNode().put("role", "tool").put("tool_call_id", callId).put("content", result);
    }

    /** A function the model may call; its JSON schema is the schema of the parameters. */
    static ObjectNode tool(FunctionDefinition function) {
        ObjectNode tool = MAPPER.createObjectNode().put("type", "function");
        ObjectNode definition = tool.putObject("function").put("name", function.getName());
        if (StringUtils.isNotEmpty(function.getDescription())) {
            definition.put("description", function.getDescription());
        }
        JsonNode parameters = function.getParametersSchema();
        definition.set("parameters", parameters != null && parameters.isObject() ? parameters
                : MAPPER.createObjectNode().put("type", "object"));
        return tool;
    }

    /** The JSON schema the answer has to follow. Not strict: the answer is checked by the application either way. */
    static ObjectNode responseFormat(JsonNode schema) {
        ObjectNode format = MAPPER.createObjectNode().put("type", "json_schema");
        ObjectNode body = format.putObject("json_schema").put("name", "response").put("strict", false);
        body.set("schema", schema);
        return format;
    }

    /** The id of a call, a made up one when the model did not name it. */
    static String callId(String id) {
        return StringUtils.isNotEmpty(id) ? id : GENERATED_ID_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }
}
