package com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LiteLlmAnthropicTokenizerStrategy implements TokenizerStrategy {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public long countTokens(AI aiEntity, String text) throws Exception {
        OpenAICompatible ai = (OpenAICompatible) aiEntity;
        
        // Ensures path is http://<host>:<port>/v1/messages/count_tokens
        String baseUri = ai.getUri().replaceAll("/+$", "");
        if (!baseUri.endsWith("/v1")) {
            baseUri += "/v1";
        }
        URI uri = URI.create(baseUri + "/messages/count_tokens");

        // Anthropic messages format expected by LiteLLM
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", text);

        JsonArray messages = new JsonArray();
        messages.add(message);

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("model", ai.getModel());
        jsonBody.add("messages", messages);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()));

        if (ai.getApiKey() != null && !ai.getApiKey().isBlank()) {
            builder.header("Authorization", "Bearer " + ai.getApiKey());
        }

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        if (json.has("input_tokens")) {
            return json.get("input_tokens").getAsLong();
        }

        throw new IllegalStateException("Missing 'input_tokens' field in response");
    }

    @Override
    public String getName() {
        return "LITELLM_ANTHROPIC";
    }
}