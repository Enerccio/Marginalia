package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.ExtendableEntity;
import com.github.enerccio.marginalia.domain.model.ImageAttachment;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import com.github.enerccio.marginalia.domain.traits.Fulltextable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.persistence.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
@Table(name = "messages")
public class ChatMessage extends ExtendableEntity {

    private static final Gson GSON = new Gson();
    private static final Type IMAGES_TYPE = new TypeToken<List<ImageAttachment>>() {
    }.getType();

    @ManyToOne(fetch = FetchType.LAZY)
    @CleanupReference(Policy.OWNED_BY)
    private Manuscript parentScript;

    @ManyToOne(fetch = FetchType.LAZY)
    @CleanupReference(Policy.OWNED_BY)
    private ChatMessage parent;

    @OneToOne
    @CleanupReference(Policy.OWNS)
    private Summary summary;

    @ExtendedAttribute
    @Transient
    private String instructions;

    @ExtendedAttribute
    @Transient
    private String sceneSetting;

    @ExtendedAttribute
    @Transient
    private String povCharacter;

    @ExtendedAttribute
    @Transient
    private String presentCharacters;

    @ExtendedAttribute
    @Transient
    private String responseReasoning;

    @ExtendedAttribute
    @Fulltextable
    @Transient
    private String response;

    // approx since model is used and can be changed
    private long tokenCount;

    @ExtendedAttribute
    @Transient
    private Long promptTokens;

    @ExtendedAttribute
    @Transient
    private Long tokenReasoningCount;

    private int wordCount;

    private boolean edited = false;

    @ExtendedAttribute
    @Transient
    private Date request;

    @ExtendedAttribute
    @Transient
    private Date ttft;

    @ExtendedAttribute
    @Transient
    private Date reasoningEnd;

    @ExtendedAttribute
    @Transient
    private String modelUsed;

    @ExtendedAttribute
    @Transient
    private String protocolUsed;

    @ExtendedAttribute
    @Transient
    private String builtPrompt;

    @ExtendedAttribute
    @Transient
    private Long builtPromptTokens;

    @ExtendedAttribute
    @Transient
    private Integer scrollPosition;

    @ExtendedAttribute
    @Transient
    private String backgroundLore;

    // JSON array of ImageAttachment, extended attributes are plain strings
    @ExtendedAttribute
    @Transient
    private String imageAttachments;
    /**
     * @return detached copy of all values of given message (extended attributes deep copied, summary not included),
     * meant to be restored later with {@link #loadFrom(ChatMessage)}, not to be saved itself
     */
    public static ChatMessage copyOf(ChatMessage source) {
        ChatMessage copy = new ChatMessage();
        copy.loadFrom(source);
        return copy;
    }

    /**
     * Replaces all values of this message (except identity and summary) with values of the source, including null
     * ones, so nothing of the previous content is kept. Extended attributes are deep copied.
     */
    public void loadFrom(ChatMessage source) {
        this.backgroundLore = source.backgroundLore;
        this.imageAttachments = source.imageAttachments;
        this.builtPrompt = source.builtPrompt;
        this.builtPromptTokens = source.builtPromptTokens;
        this.instructions = source.instructions;
        this.modelUsed = source.modelUsed;
        this.parent = source.parent;
        this.parentScript = source.parentScript;
        this.povCharacter = source.povCharacter;
        this.presentCharacters = source.presentCharacters;
        this.promptTokens = source.promptTokens;
        this.protocolUsed = source.protocolUsed;
        this.reasoningEnd = source.reasoningEnd;
        this.request = source.request;
        this.response = source.response;
        this.responseReasoning = source.responseReasoning;
        this.sceneSetting = source.sceneSetting;
        this.scrollPosition = source.scrollPosition;
        this.tokenCount = source.tokenCount;
        this.tokenReasoningCount = source.tokenReasoningCount;
        this.ttft = source.ttft;
        this.wordCount = source.wordCount;
        this.edited = source.edited;
        this.setAttributes(source.getAttributes().deepCopy());
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public Manuscript getParentScript() {
        return parentScript;
    }

    public void setParentScript(Manuscript parentScript) {
        this.parentScript = parentScript;
    }

    public ChatMessage getParent() {
        return parent;
    }

    public void setParent(ChatMessage parent) {
        this.parent = parent;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getSceneSetting() {
        return sceneSetting;
    }

    public void setSceneSetting(String sceneSetting) {
        this.sceneSetting = sceneSetting;
    }

    public String getPovCharacter() {
        return povCharacter;
    }

    public void setPovCharacter(String povCharacter) {
        this.povCharacter = povCharacter;
    }

    public String getPresentCharacters() {
        return presentCharacters;
    }

    public void setPresentCharacters(String presentCharacters) {
        this.presentCharacters = presentCharacters;
    }

    public String getResponseReasoning() {
        return responseReasoning;
    }

    public void setResponseReasoning(String responseReasoning) {
        this.responseReasoning = responseReasoning;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public long getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(long tokenCount) {
        this.tokenCount = tokenCount;
    }

    public Long getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(Long promptTokens) {
        this.promptTokens = promptTokens;
    }

    public Long getTokenReasoningCount() {
        return tokenReasoningCount;
    }

    public void setTokenReasoningCount(Long tokenReasoningCount) {
        this.tokenReasoningCount = tokenReasoningCount;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public Date getRequest() {
        return request;
    }

    public void setRequest(Date request) {
        this.request = request;
    }

    public Date getTtft() {
        return ttft;
    }

    public void setTtft(Date ttft) {
        this.ttft = ttft;
    }

    public Date getReasoningEnd() {
        return reasoningEnd;
    }

    public void setReasoningEnd(Date reasoningEnd) {
        this.reasoningEnd = reasoningEnd;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public void setModelUsed(String modelUsed) {
        this.modelUsed = modelUsed;
    }

    public String getProtocolUsed() {
        return protocolUsed;
    }

    public void setProtocolUsed(String protocolUsed) {
        this.protocolUsed = protocolUsed;
    }

    public String getBuiltPrompt() {
        return builtPrompt;
    }

    public void setBuiltPrompt(String builtPrompt) {
        this.builtPrompt = builtPrompt;
    }

    public Long getBuiltPromptTokens() {
        return builtPromptTokens;
    }

    public void setBuiltPromptTokens(Long builtPromptTokens) {
        this.builtPromptTokens = builtPromptTokens;
    }

    public Integer getScrollPosition() {
        return scrollPosition;
    }

    public void setScrollPosition(Integer scrollPosition) {
        this.scrollPosition = scrollPosition;
    }

    public String getBackgroundLore() {
        return backgroundLore;
    }

    public void setBackgroundLore(String backgroundLore) {
        this.backgroundLore = backgroundLore;
    }

    /**
     * @return images attached to the message in the order they are shown, never null
     */
    public List<ImageAttachment> getImages() {
        if (imageAttachments == null || imageAttachments.isBlank()) {
            return new ArrayList<>();
        }
        List<ImageAttachment> images = GSON.fromJson(imageAttachments, IMAGES_TYPE);
        return images == null ? new ArrayList<>() : new ArrayList<>(images);
    }

    public void setImages(List<ImageAttachment> images) {
        this.imageAttachments = images == null || images.isEmpty() ? null : GSON.toJson(images, IMAGES_TYPE);
    }

}
