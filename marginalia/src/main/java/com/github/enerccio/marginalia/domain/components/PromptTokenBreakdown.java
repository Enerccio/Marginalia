package com.github.enerccio.marginalia.domain.components;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Raw token counts of one built prompt: the budget (context, room for the response), the tokens of each section of
 * the prompt, what was cut from the story and the tokens of every message of the final payload.
 * <p>
 * Filled by the generation steps and stored with the generated part (see {@code ChatMessage#getTokenBreakdown()}).
 * Parts generated before the breakdown existed have none, readers must be ready for {@code null}.
 * <p>
 * The section counts are measured separately and the model's tokenizer is not additive, so they do not have to add up
 * exactly to {@link #getPayloadTokens()}.
 */
public class PromptTokenBreakdown {

    private static final Gson GSON = new Gson();

    /**
     * Tokens of a single message of the final payload.
     *
     * @param role   {@code SYSTEM}, {@code USER} or {@code ASSISTANT}
     * @param tokens tokens of the message content
     */
    public record MessageTokens(String role, long tokens) {
    }

    /**
     * Total context size of the model, 0 when not configured.
     */
    private long contextTokens;
    /**
     * Room kept for the response, 0 when not limited.
     */
    private long responseTokens;
    /**
     * Context minus the room for the response, what the prompt may use.
     */
    private long promptLimitTokens;

    /**
     * Jailbreak, the first thing of the system message.
     */
    private long jailbreakTokens;
    /**
     * Master template with the narrative settings, without lore and summaries.
     */
    private long templateTokens;
    /**
     * Lore entries rendered into the system message.
     */
    private long loreTokens;
    /**
     * Summaries rendered into the system message.
     */
    private long summaryTokens;
    /**
     * Story messages (the response text) put into the payload.
     */
    private long historyTokens;
    /**
     * Lore entries put in front of the user prompt.
     */
    private long userLoreTokens;
    /**
     * User prompt (turn instructions, scene, characters), without the lore in front of it.
     */
    private long userPromptTokens;
    /**
     * Room reserved by extensions for text they add to the prompt later.
     */
    private long reservedTokens;

    /**
     * Story messages put into the payload.
     */
    private int historyMessages;
    /**
     * Story messages that did not fit into the room and were left out (not counting those replaced by a summary).
     */
    private int cutMessages;
    /**
     * Tokens of the story messages that were left out.
     */
    private long cutTokens;

    /**
     * Messages of the final payload in order, as sent to the model.
     */
    private List<MessageTokens> messages = new ArrayList<>();

    /**
     * @return sum of the tokens of all payload messages
     */
    public long getPayloadTokens() {
        long sum = 0;
        for (MessageTokens message : getMessages()) {
            sum += message.tokens();
        }
        return sum;
    }

    /**
     * @return json for storing, never null
     */
    public String toJson() {
        return GSON.toJson(this);
    }

    /**
     * @return parsed breakdown, null for null, blank or malformed input (a part generated before this was recorded)
     */
    public static PromptTokenBreakdown fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return GSON.fromJson(json, PromptTokenBreakdown.class);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public long getContextTokens() {
        return contextTokens;
    }

    public void setContextTokens(long contextTokens) {
        this.contextTokens = contextTokens;
    }

    public long getResponseTokens() {
        return responseTokens;
    }

    public void setResponseTokens(long responseTokens) {
        this.responseTokens = responseTokens;
    }

    public long getPromptLimitTokens() {
        return promptLimitTokens;
    }

    public void setPromptLimitTokens(long promptLimitTokens) {
        this.promptLimitTokens = promptLimitTokens;
    }

    public long getJailbreakTokens() {
        return jailbreakTokens;
    }

    public void setJailbreakTokens(long jailbreakTokens) {
        this.jailbreakTokens = jailbreakTokens;
    }

    public long getTemplateTokens() {
        return templateTokens;
    }

    public void setTemplateTokens(long templateTokens) {
        this.templateTokens = templateTokens;
    }

    public long getLoreTokens() {
        return loreTokens;
    }

    public void setLoreTokens(long loreTokens) {
        this.loreTokens = loreTokens;
    }

    public long getSummaryTokens() {
        return summaryTokens;
    }

    public void setSummaryTokens(long summaryTokens) {
        this.summaryTokens = summaryTokens;
    }

    public long getHistoryTokens() {
        return historyTokens;
    }

    public void setHistoryTokens(long historyTokens) {
        this.historyTokens = historyTokens;
    }

    public long getUserLoreTokens() {
        return userLoreTokens;
    }

    public void setUserLoreTokens(long userLoreTokens) {
        this.userLoreTokens = userLoreTokens;
    }

    public long getUserPromptTokens() {
        return userPromptTokens;
    }

    public void setUserPromptTokens(long userPromptTokens) {
        this.userPromptTokens = userPromptTokens;
    }

    public long getReservedTokens() {
        return reservedTokens;
    }

    public void setReservedTokens(long reservedTokens) {
        this.reservedTokens = reservedTokens;
    }

    public int getHistoryMessages() {
        return historyMessages;
    }

    public void setHistoryMessages(int historyMessages) {
        this.historyMessages = historyMessages;
    }

    public int getCutMessages() {
        return cutMessages;
    }

    public void setCutMessages(int cutMessages) {
        this.cutMessages = cutMessages;
    }

    public long getCutTokens() {
        return cutTokens;
    }

    public void setCutTokens(long cutTokens) {
        this.cutTokens = cutTokens;
    }

    /**
     * @return payload messages, never null
     */
    public List<MessageTokens> getMessages() {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        return messages;
    }

    public void setMessages(List<MessageTokens> messages) {
        this.messages = messages == null ? new ArrayList<>() : new ArrayList<>(messages);
    }
}
