package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.converter.EncryptedStringConverter;
import com.github.enerccio.marginalia.domain.traits.ExtendedAttribute;
import com.google.gson.JsonObject;
import jakarta.persistence.*;

@Entity
@Table(name = "ais_openaicompat")
public class OpenAICompatible extends AI {

    @Lob
    private String uri;

    @Lob
    private String model;

    @Lob
    @Convert(converter = EncryptedStringConverter.class)
    private String apiKey;

    @Lob
    private String modelName;

    /** Seconds to wait for the provider; null uses the default of the inference service. */
    @Transient
    @ExtendedAttribute
    private Integer requestTimeoutSeconds;

    /** Retries after 429 / 5xx / connection errors; null uses the default of the inference service. */
    @Transient
    @ExtendedAttribute
    private Integer maxRetries;

    /**
     * How the tokens are counted: null is automatic (the tokenizer endpoints of the server are tried, then a local estimate),
     * otherwise the name of one tokenizer strategy or {@code ext:} and the id of a tokenizer of a plugin, see
     * {@code TokenizerService.getChoices}.
     */
    @Transient
    @ExtendedAttribute
    private String tokenizer;

    @Transient
    @ExtendedAttribute(inject = true, injectPrefix = "additionalParameters")
    private JsonObject additionalParameters = new JsonObject();

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

    public JsonObject getAdditionalParameters() {
        return additionalParameters;
    }

    public void setAdditionalParameters(JsonObject additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    public Integer getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    public void setRequestTimeoutSeconds(Integer requestTimeoutSeconds) {
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public String getTokenizer() {
        return tokenizer;
    }

    public void setTokenizer(String tokenizer) {
        this.tokenizer = tokenizer;
    }
}
