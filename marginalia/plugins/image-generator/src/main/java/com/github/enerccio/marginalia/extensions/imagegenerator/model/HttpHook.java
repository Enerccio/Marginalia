package com.github.enerccio.marginalia.extensions.imagegenerator.model;

/**
 * A request the plugin makes to a URL of the user around a session of image generation: to free or take the VRAM of the
 * GPU (stop the language model, load the image model, ask ComfyUI to unload its models), to wake a server up.
 */
public class HttpHook {

    private String url;
    private String method = "POST";
    /** One header per line, {@code Name: value}. */
    private String headers;
    private String body;

    public boolean isEmpty() {
        return url == null || url.isBlank();
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
}
