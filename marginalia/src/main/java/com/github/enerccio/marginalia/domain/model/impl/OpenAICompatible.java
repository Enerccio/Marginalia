package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Transient;

@Entity
public class OpenAICompatible extends AI {

    @Lob
    private String uri;

    @Lob
    private String model;

    @Lob
    private String apiKey;

    @Lob
    private String modelName;

    @Transient
    @ExtendedAttribute(inject = true, injectPrefix = "additionalParameters")
    private String additionalParameters;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getAdditionalParameters() {
        return additionalParameters;
    }

    public void setAdditionalParameters(String additionalParameters) {
        this.additionalParameters = additionalParameters;
    }
}
