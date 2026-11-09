package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.Http;
import com.github.enerccio.marginalia.extensions.httphooks.UrlGuard;
import com.github.enerccio.marginalia.extensions.imagegenerator.model.BackendConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * {@code POST <base>/v1/images/generations} of the OpenAI API. The answer is either {@code b64_json} or an {@code url} of the
 * image; both are accepted, as the servers that speak this API differ. No {@code response_format} is sent (some models refuse it).
 */
public class OpenAiImagesBackend implements ImageBackend {

    private final BackendConfig config;
    private final String apiKey;

    public OpenAiImagesBackend(BackendConfig config, String apiKey) {
        this.config = config;
        this.apiKey = apiKey;
    }

    @Override
    public byte[] generate(ImageRequest request, Cancel cancel) throws BackendException {
        JsonObject body = new JsonObject();
        if (StringUtils.isNotBlank(config.getModel())) {
            body.addProperty("model", config.getModel());
        }
        body.addProperty("prompt", request.prompt());
        body.addProperty("n", 1);
        body.addProperty("size", request.width() + "x" + request.height());
        if (StringUtils.isNotBlank(request.negative())) {
            // not part of the OpenAI API; the servers that know it (sd.cpp, many proxies) use it, the others ignore it
            body.addProperty("negative_prompt", request.negative());
        }
        if (cancel.isCancelled()) {
            throw new BackendException("Cancelled");
        }
        HttpResponse<byte[]> response = Http.send(builder(endpoint("/images/generations"), config.getTimeoutSeconds())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build());
        Http.requireOk(response, "Generating the image");

        JsonObject answer;
        try {
            answer = JsonParser.parseString(new String(response.body(), StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (RuntimeException e) {
            throw new BackendException("The server answered with something that is not JSON: " + Http.excerpt(response), e);
        }
        JsonArray data = answer.has("data") && answer.get("data").isJsonArray() ? answer.getAsJsonArray("data") : null;
        if (data == null || data.isEmpty() || !data.get(0).isJsonObject()) {
            throw new BackendException("The answer has no image: " + Http.excerpt(response));
        }
        JsonObject first = data.get(0).getAsJsonObject();
        if (isText(first, "b64_json")) {
            try {
                return Base64.getMimeDecoder().decode(first.get("b64_json").getAsString());
            } catch (IllegalArgumentException e) {
                throw new BackendException("The image is not valid base64", e);
            }
        }
        if (isText(first, "url")) {
            URI uri = UrlGuard.check(first.get("url").getAsString(), config.isLocal());
            HttpResponse<byte[]> image = Http.send(Http.request(uri, config.getTimeoutSeconds()).GET().build());
            Http.requireOk(image, "Downloading the image");
            return image.body();
        }
        throw new BackendException("The answer has neither b64_json nor url: " + Http.excerpt(response));
    }

    @Override
    public void test() throws BackendException {
        Http.requireOk(Http.send(builder(endpoint("/models"), 30).GET().build()), "Connecting to the server");
    }

    private static boolean isText(JsonObject o, String name) {
        JsonElement e = o.get(name);
        return e != null && e.isJsonPrimitive() && !e.getAsString().isBlank();
    }

    /** {@code <base>/v1<path>}, with the {@code /v1} of the base address taken as it is. */
    private URI endpoint(String path) throws BackendException {
        String base = config.getBaseUrl().replaceAll("/+$", "");
        if (!base.matches(".*/v\\d+$")) {
            base += "/v1";
        }
        return UrlGuard.check(base + path, config.isLocal());
    }

    private HttpRequest.Builder builder(URI uri, int timeoutSeconds) {
        HttpRequest.Builder builder = Http.request(uri, timeoutSeconds);
        if (apiKey != null && !apiKey.isBlank()) {
            builder.header("Authorization", "Bearer " + apiKey);
        }
        return builder;
    }
}
