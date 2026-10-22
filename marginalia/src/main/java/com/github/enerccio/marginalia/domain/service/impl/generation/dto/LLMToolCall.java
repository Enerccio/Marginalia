package com.github.enerccio.marginalia.domain.service.impl.generation.dto;

/**
 * A call of a function the model asked for in an {@link LLMChatMessage} of the {@link LLMRole#ASSISTANT}. The result
 * goes back as a {@link LLMRole#TOOL} message with the same {@link #getId() id}.
 */
public class LLMToolCall {

    private String id;
    private String name;
    private String arguments;
    private String signature;

    /** Identifies the call, the result message refers to it. */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** The arguments as the JSON object the model wrote; never null. */
    public String getArguments() {
        return arguments != null ? arguments : "{}";
    }

    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    /**
     * What the provider that made the call wants back with it when the conversation is sent again, opaque text (Google's thought
     * signature, base64); null when it gave none. It is kept with the call in the transcript of an agent.
     */
    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public static LLMToolCall of(String id, String name, String arguments) {
        LLMToolCall call = new LLMToolCall();
        call.id = id;
        call.name = name;
        call.arguments = arguments;
        return call;
    }

    public static LLMToolCall of(String id, String name, String arguments, String signature) {
        LLMToolCall call = of(id, name, arguments);
        call.signature = signature;
        return call;
    }

}
