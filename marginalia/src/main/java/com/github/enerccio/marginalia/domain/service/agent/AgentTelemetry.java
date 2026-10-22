package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.InferenceService.RoundUsage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * What the runner knows about one request to the model, kept with the answer it got ({@code AgentMessage.telemetry}, JSON) for
 * {@link AgentDiagnostics}: the numbers the model server reports, the time the request waited for the first word and for the rest
 * of the answer, why the answer ended, and how many of its calls were made. -1 (null for the text) is not known: a provider tells
 * what it tells, and a row of an older run has none of this.
 *
 * @param promptTokens        tokens sent
 * @param completionTokens    tokens generated, the reasoning too
 * @param cachedTokens        the tokens of the prompt the server did not read again
 * @param reasoningTokens     the tokens of the answer that were reasoning
 * @param firstTokenMillis    the request until the first thing came back: the server reading the prompt
 * @param generationMillis    the first thing until the end of the answer
 * @param finishReason        why the answer ended ({@code stop}, {@code tool_calls}, {@code length}...)
 * @param serverPromptMillis  the time the server says it needed for the prompt
 * @param serverPredictedMillis the time the server says it needed for the answer
 * @param asked               the calls the answer had
 * @param kept                the calls that were made (repeats, the limits and the batch policies drop the rest)
 * @param waitMillis          the time the request waited for a place (the cap of the run, the cap of the model) before it was sent
 * @param profile             the role of the model profile of the agent that answered, null when it was not noted (a row of an older run)
 */
public record AgentTelemetry(long promptTokens, long completionTokens, long cachedTokens, long reasoningTokens, long firstTokenMillis,
                             long generationMillis, String finishReason, long serverPromptMillis, long serverPredictedMillis,
                             int asked, int kept, long waitMillis, String profile) {

    public static AgentTelemetry of(RoundUsage usage, int asked, int kept, long waitMillis) {
        return of(usage, asked, kept, waitMillis, null);
    }

    public static AgentTelemetry of(RoundUsage usage, int asked, int kept, long waitMillis, String profile) {
        var stats = usage.stats();
        return new AgentTelemetry(usage.promptTokens(), usage.completionTokens(), stats.cachedTokens(), stats.reasoningTokens(),
                stats.firstTokenMillis(), stats.generationMillis(), stats.finishReason(), stats.serverPromptMillis(),
                stats.serverPredictedMillis(), asked, kept, waitMillis, profile);
    }

    public String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("promptTokens", promptTokens);
        json.addProperty("completionTokens", completionTokens);
        json.addProperty("cachedTokens", cachedTokens);
        json.addProperty("reasoningTokens", reasoningTokens);
        json.addProperty("firstTokenMillis", firstTokenMillis);
        json.addProperty("generationMillis", generationMillis);
        json.addProperty("finishReason", finishReason);
        json.addProperty("serverPromptMillis", serverPromptMillis);
        json.addProperty("serverPredictedMillis", serverPredictedMillis);
        json.addProperty("asked", asked);
        json.addProperty("kept", kept);
        json.addProperty("waitMillis", waitMillis);
        if (profile != null) {
            json.addProperty("profile", profile);
        }
        return json.toString();
    }

    /**
     * The telemetry as it was stored; null for none, or for text that is not one.
     */
    public static AgentTelemetry fromJson(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            JsonObject json = JsonParser.parseString(text).getAsJsonObject();
            return new AgentTelemetry(number(json, "promptTokens"), number(json, "completionTokens"), number(json, "cachedTokens"),
                    number(json, "reasoningTokens"), number(json, "firstTokenMillis"), number(json, "generationMillis"),
                    json.has("finishReason") && !json.get("finishReason").isJsonNull() ? json.get("finishReason").getAsString() : null,
                    number(json, "serverPromptMillis"), number(json, "serverPredictedMillis"), (int) number(json, "asked"),
                    (int) number(json, "kept"), number(json, "waitMillis"),
                    json.has("profile") && !json.get("profile").isJsonNull() ? json.get("profile").getAsString() : null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static long number(JsonObject json, String name) {
        return json.has(name) && !json.get(name).isJsonNull() ? json.get(name).getAsLong() : -1;
    }
}
