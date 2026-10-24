package com.github.enerccio.marginalia.extensions.stconverter.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything the converter keeps with a book, as the attribute {@link #KEY} of the book (so it is in the backups and copies
 * of the book): the imported chat, the turn details made of it, the model chosen for the wand and the prompt of the user.
 */
public class ConverterData {

    public static final String KEY = "com.github.enerccio.marginalia.extensions.stconverter";

    public static final int DEFAULT_BATCH_SIZE = 20;

    /** The name of the uploaded file, null while no chat is loaded. */
    private String fileName;
    private List<ChatLine> chat = new ArrayList<>();
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
