package com.github.enerccio.marginalia.extensions.arcoutline.model;

import java.util.UUID;

/**
 * What an arc and a chapter have in common.
 */
public abstract class OutlineNode {

    private String uuid = UUID.randomUUID().toString();
    private String title = "";
    /**
     * What happens in it, meant for the model.
     */
    private String description = "";
    /**
     * The author's private note.
     */
    private String note = "";
    private OutlineState state = OutlineState.PLANNED;
    /**
     * The reasoning of the model that wrote the node, as it streamed; {@code null} for a node that was not generated.
     * Chapters written in one go all keep the reasoning of that request.
     */
    private String generationReasoning;
    /**
     * The answer of the model that wrote the node (JSON), as it streamed; {@code null} for a node that was not
     * generated. Chapters written in one go all keep the whole answer.
     */
    private String generationResult;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public OutlineState getState() {
        return state;
    }

    public void setState(OutlineState state) {
        this.state = state;
    }

    public String getGenerationReasoning() {
        return generationReasoning;
    }

    public void setGenerationReasoning(String generationReasoning) {
        this.generationReasoning = generationReasoning;
    }

    public String getGenerationResult() {
        return generationResult;
    }

    public void setGenerationResult(String generationResult) {
        this.generationResult = generationResult;
    }
}
