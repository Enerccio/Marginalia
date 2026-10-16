package com.github.enerccio.marginalia.extensions.googlegenai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.google.genai.types.Content;
import com.google.genai.types.FunctionCall;
import com.google.genai.types.FunctionResponse;
import com.google.genai.types.Part;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * The payload of the application as the contents of a Gemini request.
 * <p>
 * Gemini has no system role in the conversation: the system messages in front of it become the system instruction,
 * the ones in the middle (an author's note inserted at a depth, for one) are sent as user messages. Messages of the
 * same role that follow each other are one content, and the results of the calls of one assistant message are one
 * content with all the responses, as the API wants them.
 */
final class GoogleGenAiMessages {

    /** The ids of calls that the model did not name, made up here; they must not be sent back as the model's. */
    static final String GENERATED_ID_PREFIX = "ggc_";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> OBJECT = new TypeReference<>() {
    };

    private GoogleGenAiMessages() {
    }

    /**
     * @param systemInstruction null when there are no system messages (or there was nothing else to send)
     */
    record Prepared(Content systemInstruction, List<Content> contents) {
    }

    static Prepared prepare(List<LLMChatMessage> payload) {
        StringBuilder system = new StringBuilder();
        List<Content> contents = new ArrayList<>();
        Map<String, String> callNames = new HashMap<>();
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
                        append(contents, "user", List.of(Part.fromText(message.getContent())));
                    }
                }
                case USER -> {
                    leading = false;
                    List<Part> parts = new ArrayList<>();
                    if (StringUtils.isNotEmpty(message.getContent())) {
                        parts.add(Part.fromText(message.getContent()));
                    }
                    for (LLMAttachment attachment : message.getAttachments()) {
                        parts.add(attachmentPart(attachment));
                    }
                    if (!parts.isEmpty()) {
                        append(contents, "user", parts);
                    }
                }
                case ASSISTANT -> {
                    leading = false;
                    List<Part> parts = new ArrayList<>();
                    if (StringUtils.isNotEmpty(message.getContent())) {
                        parts.add(Part.fromText(message.getContent()));
                    }
                    for (LLMToolCall call : message.getToolCalls()) {
                        callNames.put(call.getId(), call.getName());
                        parts.add(callPart(call));
                    }
                    if (!parts.isEmpty()) {
                        append(contents, "model", parts);
                    }
                }
                case TOOL -> {
                    leading = false;
                    String name = callNames.getOrDefault(message.getToolCallId(), "unknown");
                    append(contents, "user", List.of(responsePart(message.getToolCallId(), name,
                            StringUtils.defaultString(message.getContent()))));
                }
            }
        }

        if (contents.isEmpty()) {
            // a request needs a content, the instructions were all there is
            if (system.length() > 0) {
                contents.add(Content.builder().role("user").parts(List.of(Part.fromText(system.toString()))).build());
            }
            return new Prepared(null, contents);
        }
        Content instruction = system.length() == 0 ? null
                : Content.builder().parts(List.of(Part.fromText(system.toString()))).build();
        return new Prepared(instruction, contents);
    }

    /** The part of a conversation that is the answer to calls: what the functions returned, for the next round. */
    static Part responsePart(String id, String name, String result) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put(result.startsWith("Error: ") ? "error" : "output", result);
        FunctionResponse.Builder builder = FunctionResponse.builder().name(name).response(response);
        if (id != null && !id.startsWith(GENERATED_ID_PREFIX)) {
            builder.id(id);
        }
        return Part.builder().functionResponse(builder.build()).build();
    }

    /** Adds the parts to the last content when it has the same role, a new content otherwise. */
    static void append(List<Content> contents, String role, List<Part> parts) {
        if (!contents.isEmpty()) {
            Content last = contents.get(contents.size() - 1);
            if (role.equals(last.role().orElse("user"))) {
                List<Part> merged = new ArrayList<>(last.parts().orElse(List.of()));
                merged.addAll(parts);
                contents.set(contents.size() - 1, Content.builder().role(role).parts(merged).build());
                return;
            }
        }
        contents.add(Content.builder().role(role).parts(new ArrayList<>(parts)).build());
    }

    private static Part callPart(LLMToolCall call) {
        Map<String, Object> arguments;
        try {
            arguments = MAPPER.readValue(call.getArguments(), OBJECT);
        } catch (Exception e) {
            arguments = new LinkedHashMap<>();
        }
        FunctionCall.Builder builder = FunctionCall.builder().name(call.getName()).args(arguments);
        if (call.getId() != null && !call.getId().startsWith(GENERATED_ID_PREFIX)) {
            builder.id(call.getId());
        }
        return Part.builder().functionCall(builder.build()).build();
    }

    private static Part attachmentPart(LLMAttachment attachment) {
        String mime = StringUtils.defaultIfBlank(attachment.getMimeType(), "application/octet-stream");
        if (attachment.getData() != null) {
            return Part.fromBytes(attachment.getData(), mime);
        }
        if (attachment.getUrl() != null) {
            return Part.fromUri(attachment.getUrl(), mime);
        }
        throw new IllegalArgumentException("Attachment has neither data nor url");
    }

    /** The arguments of a call the model made, as the JSON object text the application passes to the function. */
    static String argumentsJson(FunctionCall call) {
        try {
            return MAPPER.writeValueAsString(call.args().orElse(Map.of()));
        } catch (Exception e) {
            return "{}";
        }
    }

    /** The id of a call, a made up one when the model did not name it. */
    static String callId(FunctionCall call) {
        Optional<String> id = call.id();
        return id.filter(StringUtils::isNotEmpty).orElseGet(() -> GENERATED_ID_PREFIX + java.util.UUID.randomUUID().toString().replace("-", ""));
    }

    /** The JSON schema of a function's parameters as the plain maps the SDK serializes. */
    static Map<String, Object> toMap(com.fasterxml.jackson.databind.JsonNode schema) {
        return MAPPER.convertValue(schema, OBJECT);
    }
}
