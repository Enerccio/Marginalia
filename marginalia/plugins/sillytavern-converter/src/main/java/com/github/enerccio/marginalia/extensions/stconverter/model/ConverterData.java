package com.github.enerccio.marginalia.extensions.stconverter.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything the converter keeps with a book, as the attribute {@link #KEY} of the book: which chat was imported (the file
 * itself is a resource of the user, a chat can be megabytes, and is not in this attribute) and the messages the author has
 * ticked, the turn details made of the chat, the model chosen for the wand and the prompt of the user.
 */
public class ConverterData {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.stconverter";

    public static final int DEFAULT_BATCH_SIZE = 20;

    /** The name of the uploaded file, null while no chat is loaded. */
    private String fileName;
    /** The uuid of the resource with the uploaded file, null while no chat is loaded. */
    private String resourceUuid;
    /** The numbers (from 1) of the messages the author has ticked; {@link ChatLine#isReviewed} is made of it when the chat is read. */
    private List<Integer> reviewed = new ArrayList<>();
    /** The messages of the chat, read from the resource when the data is loaded; not stored here. */
    private transient List<ChatLine> chat = new ArrayList<>();
    /** The turn details, in the order of the chat. */
    private List<TurnItem> turns = new ArrayList<>();
    /** The model the wand runs with, null for the model of the book. */
    private Long aiId;
    private Long protocolId;
    /** The prompt template of the user, null for the default one. */
    private String prompt;
    /** Whether the range snaps to the boundaries of the turns, and how many messages the auto mode sends at once. */
    private boolean align;
    private int batchSize = DEFAULT_BATCH_SIZE;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getResourceUuid() {
        return resourceUuid;
    }

    public void setResourceUuid(String resourceUuid) {
        this.resourceUuid = resourceUuid;
    }

    public List<Integer> getReviewed() {
        if (reviewed == null) {
            reviewed = new ArrayList<>();
        }
        return reviewed;
    }

    public void setReviewed(List<Integer> reviewed) {
        this.reviewed = reviewed;
    }

    public List<ChatLine> getChat() {
        if (chat == null) {
            chat = new ArrayList<>();
        }
        return chat;
    }

    public void setChat(List<ChatLine> chat) {
        this.chat = chat;
    }

    public List<TurnItem> getTurns() {
        if (turns == null) {
            turns = new ArrayList<>();
        }
        return turns;
    }

    public void setTurns(List<TurnItem> turns) {
        this.turns = turns;
    }

    public Long getAiId() {
        return aiId;
    }

    public void setAiId(Long aiId) {
        this.aiId = aiId;
    }

    public Long getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(Long protocolId) {
        this.protocolId = protocolId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public boolean isAlign() {
        return align;
    }

    public void setAlign(boolean align) {
        this.align = align;
    }

    public int getBatchSize() {
        return batchSize > 0 ? batchSize : DEFAULT_BATCH_SIZE;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
