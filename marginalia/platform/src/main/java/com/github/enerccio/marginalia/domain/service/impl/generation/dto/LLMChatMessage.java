package com.github.enerccio.marginalia.domain.service.impl.generation.dto;

import java.util.ArrayList;
import java.util.List;

public class LLMChatMessage {

    private LLMRole role;
    private String content;
    private List<LLMAttachment> attachments = new ArrayList<>();
    // null when there are none, so that the serialized payload of a plain message stays small
    private List<LLMToolCall> toolCalls;
    private String toolCallId;
    // what the model thought before it wrote this message, null for none; sent back only as the AI is set to (ReasoningPassthrough)
    // transient: it is never stored with the message (an agent keeps it in its own column) or logged with the payload
    private transient String reasoning;
    // the thinking belongs to the turn that is going on: the calls of the loop that runs now, not an answer of an earlier turn
    private transient boolean reasoningInTurn;
    // the images go before the text (the AI's image placement, set on the copy that is sent, see PromptPlacements)
    private transient boolean imagesFirst;

    public LLMRole getRole() {
        return role;
    }

    public void setRole(LLMRole role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /** Media sent along with the text; only providers/models that support them use them. Never null. */
    public List<LLMAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<LLMAttachment> attachments) {
        this.attachments = attachments != null ? attachments : new ArrayList<>();
    }

    public LLMChatMessage addAttachment(LLMAttachment attachment) {
        attachments.add(attachment);
        return this;
    }

    public boolean hasAttachments() {
        return !attachments.isEmpty();
    }

    /** Functions the assistant called with this message, in the order they are answered. Never null. */
    public List<LLMToolCall> getToolCalls() {
        return toolCalls != null ? toolCalls : List.of();
    }

    public void setToolCalls(List<LLMToolCall> toolCalls) {
        this.toolCalls = toolCalls != null && !toolCalls.isEmpty() ? new ArrayList<>(toolCalls) : null;
    }

    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }

    /** For a {@link LLMRole#TOOL} message: the {@link LLMToolCall#getId() id} of the call this is the result of. */
    public String getToolCallId() {
        return toolCallId;
    }

    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }

    /** What the model thought before it wrote this message (an assistant message), null for none. */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning == null || reasoning.isBlank() ? null : reasoning;
    }

    /**
     * Whether the reasoning is of the turn that is going on (the tool calls the model made in the request that runs now): an AI set to
     * send the thinking back only between tool calls sends it for these and not for the answers of earlier turns.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isReasoningInTurn() {
        return reasoningInTurn;
    }

    public void setReasoningInTurn(boolean reasoningInTurn) {
        this.reasoningInTurn = reasoningInTurn;
    }

    /** This message with the reasoning of the model; {@code inTurn} see {@link #isReasoningInTurn()}. */
    public LLMChatMessage withReasoning(String reasoning, boolean inTurn) {
        setReasoning(reasoning);
        this.reasoningInTurn = inTurn;
        return this;
    }

    /** Whether the images of this message go before its text, see {@link #getAttachmentsBeforeText()}. */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isImagesFirst() {
        return imagesFirst;
    }

    public void setImagesFirst(boolean imagesFirst) {
        this.imagesFirst = imagesFirst;
    }

    /**
     * The attachments a provider puts before the text of the message: the images when {@link #isImagesFirst()}, otherwise none.
     * Together with {@link #getAttachmentsAfterText()} they are all the attachments, each in its order.
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public List<LLMAttachment> getAttachmentsBeforeText() {
        return imagesFirst ? attachments.stream().filter(LLMChatMessage::isImage).toList() : List.of();
    }

    /** The attachments a provider puts after the text: all of them, or all but the images when {@link #isImagesFirst()}. */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public List<LLMAttachment> getAttachmentsAfterText() {
        return imagesFirst ? attachments.stream().filter(attachment -> !isImage(attachment)).toList() : attachments;
    }

    private static boolean isImage(LLMAttachment attachment) {
        LLMAttachment.Kind kind = attachment.getKind() != null ? attachment.getKind() : LLMAttachment.Kind.fromMimeType(attachment.getMimeType());
        return kind == LLMAttachment.Kind.IMAGE;
    }

    /**
     * A shallow copy to change before a request: the same role, text, tool calls, reasoning and the same attachment objects in a
     * list of its own.
     */
    public LLMChatMessage copy() {
        LLMChatMessage copy = new LLMChatMessage();
        copy.role = role;
        copy.content = content;
        copy.attachments = new ArrayList<>(attachments);
        copy.toolCalls = toolCalls != null ? new ArrayList<>(toolCalls) : null;
        copy.toolCallId = toolCallId;
        copy.reasoning = reasoning;
        copy.reasoningInTurn = reasoningInTurn;
        copy.imagesFirst = imagesFirst;
        return copy;
    }

    public static LLMChatMessage of(LLMRole role, String content) {
        LLMChatMessage message = new LLMChatMessage();
        message.role = role;
        message.content = content;
        return message;
    }

    /** Assistant message that calls functions; the content is the text it wrote before the calls, may be null. */
    public static LLMChatMessage ofToolCalls(String content, List<LLMToolCall> toolCalls) {
        LLMChatMessage message = of(LLMRole.ASSISTANT, content);
        message.setToolCalls(toolCalls);
        return message;
    }

    /** What a called function returned (or the error it ended with). */
    public static LLMChatMessage ofToolResult(String toolCallId, String result) {
        LLMChatMessage message = of(LLMRole.TOOL, result);
        message.toolCallId = toolCallId;
        return message;
    }

}
