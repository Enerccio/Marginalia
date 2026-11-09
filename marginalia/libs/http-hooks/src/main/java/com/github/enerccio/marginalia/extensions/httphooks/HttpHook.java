package com.github.enerccio.marginalia.extensions.httphooks;

import java.util.function.UnaryOperator;

/**
 * A request a plugin makes to a URL of the user around work on a shared GPU: to free or take the VRAM (stop the language model, load
 * the image or speech model, ask ComfyUI to unload its models), to wake a server up. Kept in the settings of the user as JSON.
 * <p>
 * The headers usually hold a secret ({@code Authorization: Bearer ...}), so they are stored protected ({@link #protectedHeaders}, made
 * with {@code InferenceServices.protectSecret}); {@link #headers} is the plain text of settings saved before that, read as long as there is
 * nothing protected and cleared when the settings are saved again.
 */
public class HttpHook {

    private String url;
    private String method = "POST";
    /** One header per line, {@code Name: value}; plain text, only read (an old setting). */
    private String headers;
    /** The headers, protected with the key of the installation. */
    private String protectedHeaders;
    private String body;
    /** How long the answer may take, in seconds; 0 for the default of {@link #DEFAULT_TIMEOUT_SECONDS}. */
    private int timeoutSeconds;
    /** How long to wait after the request has been answered, in seconds (a server that is still busy after it said yes). */
    private int waitSeconds;

    public static final int DEFAULT_TIMEOUT_SECONDS = 60;
    public static final int MAX_TIMEOUT_SECONDS = 600;
    public static final int MAX_WAIT_SECONDS = 300;

    public boolean isEmpty() {
        return url == null || url.isBlank();
    }

    /**
     * @param reveal opens a protected text (null when the caller can't)
     * @return the headers, one per line; null when there are none or they were protected by another installation
     */
    public String headersText(UnaryOperator<String> reveal) {
        if (protectedHeaders != null && !protectedHeaders.isBlank()) {
            return reveal == null ? null : reveal.apply(protectedHeaders);
        }
        return headers;
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

    /** The plain headers of an old setting; use {@link #headersText}. */
    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getProtectedHeaders() {
        return protectedHeaders;
    }

    public void setProtectedHeaders(String protectedHeaders) {
        this.protectedHeaders = protectedHeaders;
    }

    public String getBody() {
        return body;
    }

    /** The time the answer may take: the default when none is set, never more than {@link #MAX_TIMEOUT_SECONDS}. */
    public int getTimeoutSeconds() {
        return timeoutSeconds <= 0 ? DEFAULT_TIMEOUT_SECONDS : Math.min(timeoutSeconds, MAX_TIMEOUT_SECONDS);
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    /** The time to wait after the answer, from 0 to {@link #MAX_WAIT_SECONDS}. */
    public int getWaitSeconds() {
        return Math.max(0, Math.min(waitSeconds, MAX_WAIT_SECONDS));
    }

    public void setWaitSeconds(int waitSeconds) {
        this.waitSeconds = waitSeconds;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
