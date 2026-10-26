package com.github.enerccio.marginalia.extensions.anthropic;

import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.enerccio.marginalia.domain.service.InferenceException;
import com.github.enerccio.marginalia.domain.service.InferenceException.Type;
import com.github.enerccio.marginalia.domain.service.function.FunctionDefinition;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The payload of the application as the messages of a Claude request.
 * <p>
 * Claude has no system role in the conversation: the system messages in front of it become the {@code system}
 * prompt, the ones in the middle (an author's note inserted at a depth, for one) are sent as user messages. Messages of
 * the same role that follow each other are one message, and the results of the calls of one assistant message are one
 * user message with all the results in front of anything else, as the API wants them. The conversation has to start and end
 * with a user message (the models after Claude 4.5 refuse a prefilled answer); the payload of the story always does, any
 * other is refused with an {@link InferenceException} instead of being changed.
 * <p>
 * The thinking blocks of an answer that called functions must go back with it, signed, or the API refuses the next
 * round. The service keeps the whole answer within a request; across requests (the transcript of an agent, which keeps
 * only the calls) the blocks travel in the {@link LLMToolCall#getSignature() signature} of the first call, see
 * {@link #encodeThinking}.
 */
final class AnthropicMessages {

    /** The ids of calls that the model did not name, made up here (the API always names them, but a proxy may not). */
    static final String GENERATED_ID_PREFIX = "tu_gen_";

    private static final String THINKING_PREFIX = "anthropic-thinking:1:";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AnthropicMessages() {
    }

    /** A message of the conversation: who said it and what is in it. */
    static final class Turn {
        final boolean assistant;
        // the results of function calls go in front of anything else the user says
        final List<ContentBlockParam> results = new ArrayList<>();
        final List<ContentBlockParam> blocks = new ArrayList<>();

        Turn(boolean assistant) {
            this.assistant = assistant;
        }

        List<ContentBlockParam> content() {
            List<ContentBlockParam> all = new ArrayList<>(results);
            all.addAll(blocks);
            return all;
        }

        boolean isEmpty() {
            return results.isEmpty() && blocks.isEmpty();
        }
    }

    /**
     * @param system null when there are no system messages (or there was nothing else to send)
     */
    record Prepared(String system, List<Turn> turns) {

        /** Puts the messages into the builder of a request. */
        void into(MessageCreateParams.Builder builder) {
            if (system != null) {
                builder.system(system);
            }
            for (Turn turn : turns) {
                if (turn.assistant) {
                    builder.addAssistantMessageOfContentBlockParams(turn.content());
                } else {
                    builder.addUserMessageOfContentBlockParams(turn.content());
                }
            }
        }

        void into(MessageCountTokensParams.Builder builder) {
            if (system != null) {
                builder.system(system);
            }
            for (Turn turn : turns) {
                if (turn.assistant) {
                    builder.addAssistantMessageOfContentBlockParams(turn.content());
                } else {
                    builder.addUserMessageOfContentBlockParams(turn.content());
                }
            }
        }
    }

    static Prepared prepare(List<LLMChatMessage> payload) {
        StringBuilder system = new StringBuilder();
        List<Turn> turns = new ArrayList<>();
        boolean leading = true;

        for (LLMChatMessage message : payload) {
            switch (message.getRole()) {
                case SYSTEM -> {
                    if (StringUtils.isBlank(message.getContent())) {
                        continue;
                    }
                    if (leading) {
                        if (system.length() > 0) {
                            system.append("\n\n");
                        }
                        system.append(message.getContent());
                    } else {
                        turn(turns, false).blocks.add(ContentBlockParam.ofText(message.getContent()));
                    }
                }
                case USER -> {
                    leading = false;
                    List<ContentBlockParam> blocks = new ArrayList<>();
                    if (StringUtils.isNotBlank(message.getContent())) {
                        blocks.add(ContentBlockParam.ofText(message.getContent()));
                    }
                    for (LLMAttachment attachment : message.getAttachments()) {
                        blocks.add(attachmentBlock(attachment));
                    }
                    if (!blocks.isEmpty()) {
                        turn(turns, false).blocks.addAll(blocks);
                    }
                }
                case ASSISTANT -> {
                    leading = false;
                    List<ContentBlockParam> blocks = new ArrayList<>();
                    // the thinking comes first, with the signature the API checks
                    for (LLMToolCall call : message.getToolCalls()) {
                        blocks.addAll(decodeThinking(call.getSignature()));
                    }
                    if (StringUtils.isNotBlank(message.getContent())) {
                        blocks.add(ContentBlockParam.ofText(message.getContent()));
                    }
                    for (LLMToolCall call : message.getToolCalls()) {
                        blocks.add(ContentBlockParam.ofToolUse(ToolUseBlockParam.builder()
                                .id(call.getId()).name(call.getName()).input(input(call.getArguments())).build()));
                    }
                    if (!blocks.isEmpty()) {
                        turn(turns, true).blocks.addAll(blocks);
                    }
                }
                case TOOL -> {
                    leading = false;
                    turn(turns, false).results.add(resultBlock(message.getToolCallId(),
                            StringUtils.defaultString(message.getContent())));
                }
            }
        }

        if (turns.isEmpty()) {
            // a request needs a message, the instructions were all there is
            if (system.length() > 0) {
                Turn only = new Turn(false);
                only.blocks.add(ContentBlockParam.ofText(system.toString()));
                turns.add(only);
            }
            return new Prepared(null, turns);
        }
        if (turns.get(0).assistant || turns.get(turns.size() - 1).assistant) {
            throw new InferenceException(Type.BAD_REQUEST, null, "The payload has to start and end with a message of the user, "
                    + "Claude does not continue an answer", null);
        }
        return new Prepared(system.length() == 0 ? null : system.toString(), turns);
    }

    /** The last turn when it has the role, a new one otherwise. */
    static Turn turn(List<Turn> turns, boolean assistant) {
        if (!turns.isEmpty() && turns.get(turns.size() - 1).assistant == assistant) {
            return turns.get(turns.size() - 1);
        }
        Turn turn = new Turn(assistant);
        turns.add(turn);
        return turn;
    }

    /** The answer to a call: what the function returned, an error when it failed. */
    static ContentBlockParam resultBlock(String callId, String result) {
        return ContentBlockParam.ofToolResult(ToolResultBlockParam.builder()
                .toolUseId(callId).content(result).isError(result.startsWith("Error: ")).build());
    }

    private static ContentBlockParam attachmentBlock(LLMAttachment attachment) {
        String mime = StringUtils.defaultIfBlank(attachment.getMimeType(), "application/octet-stream").toLowerCase(Locale.ROOT);
        if (attachment.getData() == null && attachment.getUrl() == null) {
            throw new IllegalArgumentException("Attachment has neither data nor url");
        }
        if (mime.equals("image/jpeg") || mime.equals("image/png") || mime.equals("image/gif") || mime.equals("image/webp")) {
            if (attachment.getData() != null) {
                return ContentBlockParam.ofImage(Base64ImageSource.builder().data(attachment.toBase64())
                        .mediaType(Base64ImageSource.MediaType.of(mime)).build());
            }
            return ContentBlockParam.ofImage(UrlImageSource.builder().url(attachment.getUrl()).build());
        }
        if (mime.equals("application/pdf")) {
            if (attachment.getData() != null) {
                return ContentBlockParam.ofDocument(Base64PdfSource.builder().data(attachment.toBase64()).build());
            }
            return ContentBlockParam.ofDocument(UrlPdfSource.builder().url(attachment.getUrl()).build());
        }
        if (mime.startsWith("text/") && attachment.getData() != null) {
            return ContentBlockParam.ofDocument(PlainTextSource.builder()
                    .data(new String(attachment.getData(), StandardCharsets.UTF_8)).build());
        }
        throw new IllegalArgumentException("Claude can read pictures (JPEG, PNG, GIF, WebP), PDF files and text files, not " + mime);
    }

    // ---- thinking that travels with the calls

    /**
     * The thinking blocks of an answer as the text kept in the signature of its first call, or null when it has none.
     */
    static String encodeThinking(List<ContentBlock> content) {
        ArrayNode blocks = MAPPER.createArrayNode();
        for (ContentBlock block : content) {
            if (block.isThinking()) {
                ThinkingBlock thinking = block.asThinking();
                blocks.addObject().put("thinking", thinking.thinking()).put("signature", thinking.signature());
            } else if (block.isRedactedThinking()) {
                blocks.addObject().put("redacted", block.asRedactedThinking().data());
            }
        }
        if (blocks.isEmpty()) {
            return null;
        }
        return THINKING_PREFIX + Base64.getEncoder().encodeToString(blocks.toString().getBytes(StandardCharsets.UTF_8));
    }

    /** The thinking blocks kept by {@link #encodeThinking}; nothing for a signature of another provider. */
    static List<ContentBlockParam> decodeThinking(String signature) {
        if (signature == null || !signature.startsWith(THINKING_PREFIX)) {
            return List.of();
        }
        try {
            JsonNode blocks = MAPPER.readTree(Base64.getDecoder().decode(signature.substring(THINKING_PREFIX.length())));
            List<ContentBlockParam> result = new ArrayList<>();
            for (JsonNode block : blocks) {
                if (block.has("redacted")) {
                    result.add(ContentBlockParam.ofRedactedThinking(RedactedThinkingBlockParam.builder()
                            .data(block.get("redacted").asText()).build()));
                } else {
                    result.add(ContentBlockParam.ofThinking(ThinkingBlockParam.builder()
                            .thinking(block.path("thinking").asText("")).signature(block.path("signature").asText()).build()));
                }
            }
            return result;
        } catch (Exception e) {
            return List.of();
        }
    }

    // ---- JSON

    /** The arguments of a call as the object Claude wrote. */
    static ToolUseBlockParam.Input input(String arguments) {
        ToolUseBlockParam.Input.Builder input = ToolUseBlockParam.Input.builder();
        try {
            JsonNode node = MAPPER.readTree(StringUtils.defaultIfBlank(arguments, "{}"));
            if (node != null && node.isObject()) {
                node.fields().forEachRemaining(field -> input.putAdditionalProperty(field.getKey(), JsonValue.fromJsonNode(field.getValue())));
            }
        } catch (Exception e) {
            // arguments that are not an object are sent as none
        }
        return input.build();
    }

    /** The arguments of a call the model made, as the JSON object text the application passes to the function. */
    static String argumentsJson(ToolUseBlock call) {
        try {
            JsonNode node = call._input().convert(JsonNode.class);
            return node == null || node.isNull() || node.isMissingNode() ? "{}" : MAPPER.writeValueAsString(node);
        } catch (Exception e) {
            return "{}";
        }
    }

    /** The id of a call, a made up one when the model did not name it. */
    static String callId(ToolUseBlock call) {
        return StringUtils.isNotEmpty(call.id()) ? call.id()
                : GENERATED_ID_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }

    /** A function the model may call; its JSON schema is the schema of the input. */
    static Tool tool(FunctionDefinition function) {
        Tool.InputSchema.Builder schema = Tool.InputSchema.builder();
        JsonNode parameters = function.getParametersSchema();
        if (parameters != null && parameters.isObject()) {
            parameters.fields().forEachRemaining(field -> {
                String key = field.getKey();
                JsonNode value = field.getValue();
                switch (key) {
                    case "type" -> {
                        // always an object
                    }
                    case "properties" -> {
                        Tool.InputSchema.Properties.Builder properties = Tool.InputSchema.Properties.builder();
                        value.fields().forEachRemaining(property ->
                                properties.putAdditionalProperty(property.getKey(), JsonValue.fromJsonNode(property.getValue())));
                        schema.properties(properties.build());
                    }
                    case "required" -> value.forEach(name -> schema.addRequired(name.asText()));
                    default -> schema.putAdditionalProperty(key, JsonValue.fromJsonNode(value));
                }
            });
        }
        Tool.Builder tool = Tool.builder().name(function.getName()).inputSchema(schema.build());
        if (StringUtils.isNotEmpty(function.getDescription())) {
            tool.description(function.getDescription());
        }
        return tool.build();
    }

    /** The JSON schema the answer has to follow. */
    static JsonOutputFormat outputFormat(JsonNode schema) {
        JsonOutputFormat.Schema.Builder builder = JsonOutputFormat.Schema.builder();
        if (schema != null && schema.isObject()) {
            schema.fields().forEachRemaining(field -> builder.putAdditionalProperty(field.getKey(), JsonValue.fromJsonNode(field.getValue())));
        }
        return JsonOutputFormat.builder().schema(builder.build()).build();
    }
}
