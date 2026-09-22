package com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LlamaCppTokenizerStrategy implements TokenizerStrategy {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public long countTokens(AI aiEntity, String text) throws Exception {
        OpenAICompatible ai = (OpenAICompatible) aiEntity;
        String baseUri = ai.getUri().replaceAll("/v1/?$", "").replaceAll("/+$", "");
        URI tokenizeUri = URI.create(baseUri + "/tokenize");

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("content", text);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(tokenizeUri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()));

        if (ai.getApiKey() != null && !ai.getApiKey().isBlank()) {
            builder.header("Authorization", "Bearer " + ai.getApiKey());
        }

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("HTTP " + response.statusCode());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return json.getAsJsonArray("tokens").size();
    }

    @Override
    public String getName() {
        return "LLAMA_CPP_NATIVE";
    }
}