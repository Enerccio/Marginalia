package com.github.enerccio.marginalia.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * What {@link ApiDispatcher} answers: the status, the type and the body, and the extra headers.
 */
public record ApiResponse(int status, String contentType, byte[] body, Map<String, String> headers) {

    static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();

    public ApiResponse {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    public static ApiResponse json(int status, JsonElement json) {
        return new ApiResponse(status, "application/json; charset=utf-8", GSON.toJson(json).getBytes(StandardCharsets.UTF_8), null);
    }

    public static ApiResponse text(int status, String text) {
        return new ApiResponse(status, "text/plain; charset=utf-8", text.getBytes(StandardCharsets.UTF_8), null);
    }

    public static ApiResponse error(int status, String message) {
        JsonObject json = new JsonObject();
        json.addProperty("error", message);
        return json(status, json);
    }

    public ApiResponse withHeader(String name, String value) {
        HashMap<String, String> copy = new HashMap<>(headers);
        copy.put(name, value);
        return new ApiResponse(status, contentType, body, copy);
    }

    /**
     * The body as text, for tests and logs of the caller.
     */
    public String bodyText() {
        return new String(body, StandardCharsets.UTF_8);
    }
}
