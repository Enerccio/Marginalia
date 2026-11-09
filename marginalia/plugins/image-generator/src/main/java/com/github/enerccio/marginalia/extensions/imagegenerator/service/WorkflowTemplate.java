package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.google.gson.*;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fills the placeholders of a ComfyUI workflow: {@code {{prompt}}}, {{negative}}, {{seed}}, {{width}} and {{height}}.
 * Text is put into the parsed workflow, so quotes and line breaks in a prompt can't break the JSON; a number placeholder can
 * stand alone ({@code "seed": {{seed}}}) or be a whole string ({@code "seed": "{{seed}}"}), both give a number.
 */
public final class WorkflowTemplate {

    private static final Pattern BARE_NUMBER = Pattern.compile("(?<!\")\\{\\{\\s*(seed|width|height)\\s*}}(?!\")");
    private static final Pattern WHOLE_NUMBER = Pattern.compile("^\\s*\\{\\{\\s*(seed|width|height)\\s*}}\\s*$");
    private static final Pattern TEXT = Pattern.compile("\\{\\{\\s*(prompt|negative|seed|width|height)\\s*}}");

    private static final Gson GSON = new Gson();

    private WorkflowTemplate() {
    }

    /**
     * @return the workflow as a JSON object ready to be posted as {@code prompt}
     * @throws BackendException when the workflow is not a JSON object
     */
    public static JsonObject fill(String workflow, ImageRequest request) throws BackendException {
        if (workflow == null || workflow.isBlank()) {
            throw new BackendException("The backend has no workflow");
        }
        Map<String, Long> numbers = Map.of("seed", request.seed(), "width", (long) request.width(), "height", (long) request.height());
        String prepared = BARE_NUMBER.matcher(workflow).replaceAll(m -> Matcher.quoteReplacement(String.valueOf(numbers.get(m.group(1)))));
        JsonElement parsed;
        try {
            parsed = GSON.fromJson(prepared, JsonElement.class);
        } catch (JsonParseException e) {
            throw new BackendException("The workflow is not valid JSON: " + e.getMessage(), e);
        }
        if (parsed == null || !parsed.isJsonObject()) {
            throw new BackendException("The workflow must be a JSON object (Save (API Format) in ComfyUI)");
        }
        if (parsed.getAsJsonObject().has("nodes") && !parsed.getAsJsonObject().has("prompt")) {
            throw new BackendException("This is the editor format of the workflow; export it with Save (API Format)");
        }
        // some tools export {"prompt": {...}}
        JsonObject root = parsed.getAsJsonObject();
        if (root.has("prompt") && root.get("prompt").isJsonObject()) {
            root = root.getAsJsonObject("prompt");
        }
        return (JsonObject) walk(root, request, numbers);
    }

    private static JsonElement walk(JsonElement element, ImageRequest request, Map<String, Long> numbers) {
        if (element.isJsonObject()) {
            JsonObject result = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                result.add(entry.getKey(), walk(entry.getValue(), request, numbers));
            }
            return result;
        }
        if (element.isJsonArray()) {
            JsonArray result = new JsonArray();
            for (JsonElement item : element.getAsJsonArray()) {
                result.add(walk(item, request, numbers));
            }
            return result;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            String text = element.getAsString();
            Matcher whole = WHOLE_NUMBER.matcher(text);
            if (whole.matches()) {
                return new JsonPrimitive(numbers.get(whole.group(1)));
            }
            Matcher matcher = TEXT.matcher(text);
            return new JsonPrimitive(matcher.replaceAll(m -> Matcher.quoteReplacement(switch (m.group(1)) {
                case "prompt" -> request.prompt();
                case "negative" -> request.negative() == null ? "" : request.negative();
                default -> String.valueOf(numbers.get(m.group(1)));
            })));
        }
        return element;
    }
}
