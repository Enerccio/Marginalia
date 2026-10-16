package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.extensions.ExtensionAIFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * An AI whose provider is implemented by an extension: the {@link ExtensionAIFactory} registered in
 * {@code InferenceServices} under {@link #getExtensionId()} creates its inference service and the form of its settings.
 * The only column is the id of the extension, the settings of the provider are kept by the extension in
 * {@link #getSettings()} (a part of the dynamic {@link #getAttributes() attributes}, so no migration is needed for them
 * and they travel with backups). When the extension is not loaded the AI stays in the database and in the lists, it just
 * has no inference service until the extension is loaded again.
 */
@Entity
@Table(name = "ais_extension")
public class ExtensionAI extends AI {

    @Column(nullable = false)
    private String extensionId;

    /**
     * The factory that created the cached {@link #getInferenceService() inference service}, so a service of an
     * extension that was unloaded (or reloaded) is not used any more.
     */
    @Transient
    private ExtensionAIFactory inferenceServiceFactory;

    public String getExtensionId() {
        return extensionId;
    }

    public void setExtensionId(String extensionId) {
        this.extensionId = extensionId;
    }

    /**
     * The settings of the provider, the object stored in the attributes under the id of the extension (created when
     * missing). Anything can be in it, but a secret has to be stored with {@code InferenceServices.protectSecret}: the
     * attributes are not encrypted. Saved with the AI by {@code AIService.save}.
     */
    public JsonObject getSettings() {
        if (extensionId == null) {
            throw new IllegalStateException("The extension id is not set");
        }
        if (getAttributes() == null) {
            setAttributes(new JsonObject());
        }
        JsonElement settings = getAttributes().get(extensionId);
        if (settings != null && settings.isJsonObject()) {
            return settings.getAsJsonObject();
        }
        JsonObject created = new JsonObject();
        getAttributes().add(extensionId, created);
        return created;
    }

    public ExtensionAIFactory getInferenceServiceFactory() {
        return inferenceServiceFactory;
    }

    public void setInferenceServiceFactory(ExtensionAIFactory inferenceServiceFactory) {
        this.inferenceServiceFactory = inferenceServiceFactory;
    }

    @Override
    public void setAiType(AIType aiType) {
        super.setAiType(aiType);
        this.inferenceServiceFactory = null;
    }
}
