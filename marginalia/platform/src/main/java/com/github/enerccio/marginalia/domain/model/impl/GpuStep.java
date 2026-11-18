package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.converter.EncryptedStringConverter;
import jakarta.persistence.*;

/**
 * An HTTP request made when a {@link GpuLock} takes a GPU or gives it to another lock: load a model, unload it, start or stop a
 * container. Several locks can use one step (the "unload the language model" request is usually the same for all of them).
 * Set up by the administrator, so private addresses are allowed.
 */
@Entity
@Table(name = "gpu_steps")
public class GpuStep extends BaseEntity {

    public static final int DEFAULT_TIMEOUT_SECONDS = 60;
    public static final int MAX_TIMEOUT_SECONDS = 600;
    public static final int MAX_WAIT_SECONDS = 300;

    @Column(nullable = false)
    private String name = "";

    @Column(nullable = false, length = 2000)
    private String url = "";

    @Column(nullable = false, length = 16)
    private String method = "POST";

    /** One header per line, {@code Name: value}; encrypted (usually holds a bearer key). */
    @Lob
    @Convert(converter = EncryptedStringConverter.class)
    private String headers;

    @Lob
    private String body;

    /** How long the answer may take, in seconds; 0 for {@link #DEFAULT_TIMEOUT_SECONDS}. */
    private int timeoutSeconds;

    /** How long to wait after the answer, in seconds (a server that is still busy after it said yes). */
    private int waitSeconds;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method == null || method.isBlank() ? "POST" : method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    /** The time the answer may take: the default when none is set, never more than {@link #MAX_TIMEOUT_SECONDS}. */
    public int effectiveTimeoutSeconds() {
        return timeoutSeconds <= 0 ? DEFAULT_TIMEOUT_SECONDS : Math.min(timeoutSeconds, MAX_TIMEOUT_SECONDS);
    }

    public int getWaitSeconds() {
        return waitSeconds;
    }

    public void setWaitSeconds(int waitSeconds) {
        this.waitSeconds = waitSeconds;
    }

    /** The time to wait after the answer, from 0 to {@link #MAX_WAIT_SECONDS}. */
    public int effectiveWaitSeconds() {
        return Math.max(0, Math.min(waitSeconds, MAX_WAIT_SECONDS));
    }
}
