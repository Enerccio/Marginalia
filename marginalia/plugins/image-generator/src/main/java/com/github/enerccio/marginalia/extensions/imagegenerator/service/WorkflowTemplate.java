package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.google.gson.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fills the placeholders of a ComfyUI workflow: {@code {{prompt}}}, {{negative}}, {{seed}}, {{width}}, {{height}}, {{lora}} and
 * {{lora_strength}}. Text is put into the parsed workflow, so quotes and line breaks in a prompt can't break the JSON; a number placeholder can
 * stand alone ({@code "seed": {{seed}}}) or be a whole string ({@code "seed": "{{seed}}"}), both give a number.
 * <p>
 * Without a LoRA, every node with an input that is just {@code {{lora}}} (a LoRA loader) is taken out of the graph: what used its outputs
 * gets what went into it (output 0 the {@code model} input, output 1 the {@code clip} input).
 */
public final class WorkflowTemplate {

    /** The placeholder that names the LoRA; workflows with it get the LoRA choice in the window. */
    public static final String LORA = "{{lora}}";

    private static final Pattern BARE_NUMBER = Pattern.compile("(?<!\")\\{\\{\\s*(seed|width|height|lora_strength)\\s*}}(?!\")");
    private static final Pattern WHOLE_NUMBER = Pattern.compile("^\\s*\\{\\{\\s*(seed|width|height|lora_strength)\\s*}}\\s*$");
    private static final Pattern WHOLE_LORA = Pattern.compile("^\\s*\\{\\{\\s*lora\\s*}}\\s*$");
    private static final Pattern ANY_LORA = Pattern.compile("\\{\\{\\s*lora\\s*}}");
    private static final Pattern TEXT = Pattern.compile("\\{\\{\\s*(prompt|negative|seed|width|height|lora|lora_strength)\\s*}}");
    /** The input that takes the place of an output of a bypassed LoRA loader, by the index of the output. */
    private static final List<String> PASS_THROUGH = List.of("model", "clip");

    private static final Gson GSON = new Gson();

    private WorkflowTemplate() {
    }

    /** Whether the workflow has the LoRA placeholder. */
    public static boolean usesLora(String workflow) {
        return workflow != null && ANY_LORA.matcher(workflow).find();
    }

    /**
     * @return the workflow as a JSON object ready to be posted as {@code prompt}
     * @throws BackendException when the workflow is not a JSON object, or a LoRA loader without a LoRA can't be taken out
     */
    public static JsonObject fill(String workflow, ImageRequest request) throws BackendException {
        if (workflow == null || workflow.isBlank()) {
            throw new BackendException("The backend has no workflow");
        }
        Map<String, Number> numbers = Map.of("seed", request.seed(), "width", request.width(), "height", request.height(),
                "lora_strength", request.loraStrength());
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
        if (!request.hasLora()) {
            root = bypassLoras(root);
        }
        return (JsonObject) walk(root, request, numbers);
    }

    // --- no LoRA: the loaders leave the graph ---

    private static JsonObject bypassLoras(JsonObject root) throws BackendException {
        Set<String> loaders = new HashSet<>();
        for (Map.Entry<String, JsonElement> node : root.entrySet()) {
            JsonObject inputs = inputs(node.getValue());
            if (inputs != null && inputs.entrySet().stream().anyMatch(i -> isWholeLora(i.getValue()))) {
                loaders.add(node.getKey());
            }
        }
        if (loaders.isEmpty()) {
            return root;
        }
        JsonObject result = new JsonObject();
        for (Map.Entry<String, JsonElement> node : root.entrySet()) {
            if (loaders.contains(node.getKey())) {
                continue;
            }
            JsonObject copy = node.getValue().deepCopy().getAsJsonObject();
            JsonObject inputs = inputs(copy);
            if (inputs != null) {
                for (Map.Entry<String, JsonElement> input : inputs.entrySet()) {
                    if (isLink(input.getValue()) && loaders.contains(linkNode(input.getValue()))) {
                        input.setValue(resolve(root, loaders, input.getValue().getAsJsonArray(), node.getKey()));
                    }
                }
            }
            result.add(node.getKey(), copy);
        }
        return result;
    }

    /** What a link to a bypassed loader stands for: the input of the loader, followed through chained loaders. */
    private static JsonArray resolve(JsonObject root, Set<String> loaders, JsonArray link, String user) throws BackendException {
        JsonArray current = link;
        Set<String> seen = new HashSet<>();
        while (isLink(current) && loaders.contains(linkNode(current))) {
            String loader = linkNode(current);
            if (!seen.add(loader)) {
                throw new BackendException("The LoRA nodes of the workflow form a loop");
            }
            int output = current.get(1).getAsInt();
            JsonObject inputs = inputs(root.get(loader));
            JsonElement source = output >= 0 && output < PASS_THROUGH.size() && inputs != null ? inputs.get(PASS_THROUGH.get(output)) : null;
            if (!isLink(source)) {
                throw new BackendException("Without a LoRA, node " + loader + " can't be left out: node " + user
                        + " uses its output " + output + ", which has no '" + (output >= 0 && output < PASS_THROUGH.size()
                        ? PASS_THROUGH.get(output) : "?") + "' input to take its place");
            }
            current = source.getAsJsonArray();
        }
        return current.deepCopy();
    }

    private static JsonObject inputs(JsonElement node) {
        return node != null && node.isJsonObject() && node.getAsJsonObject().has("inputs") && node.getAsJsonObject().get("inputs").isJsonObject()
                ? node.getAsJsonObject().getAsJsonObject("inputs") : null;
    }

    private static boolean isWholeLora(JsonElement value) {
        return value.isJsonPrimitive() && value.getAsJsonPrimitive().isString() && WHOLE_LORA.matcher(value.getAsString()).matches();
    }

    /** {@code ["node id", output index]} */
    private static boolean isLink(JsonElement value) {
        if (value == null || !value.isJsonArray() || value.getAsJsonArray().size() != 2) {
            return false;
        }
        JsonElement id = value.getAsJsonArray().get(0);
        JsonElement index = value.getAsJsonArray().get(1);
        return id.isJsonPrimitive() && index.isJsonPrimitive() && index.getAsJsonPrimitive().isNumber();
    }

    private static String linkNode(JsonElement link) {
        return link.getAsJsonArray().get(0).getAsString();
    }

    // --- the placeholders ---

    private static JsonElement walk(JsonElement element, ImageRequest request, Map<String, Number> numbers) {
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
                case "lora" -> request.hasLora() ? request.lora().strip() : "";
                default -> String.valueOf(numbers.get(m.group(1)));
            })));
        }
        return element;
    }
}
