package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowTemplateTest {

    private static final String WORKFLOW = """
            {
              "3": {"class_type": "KSampler", "inputs": {"seed": {{seed}}, "steps": 20}},
              "5": {"class_type": "EmptyLatentImage", "inputs": {"width": "{{width}}", "height": {{height}}, "batch_size": 1}},
              "6": {"class_type": "CLIPTextEncode", "inputs": {"text": "{{prompt}}", "clip": ["4", 1]}},
              "7": {"class_type": "CLIPTextEncode", "inputs": {"text": "bad, {{negative}}", "clip": ["4", 1]}}
            }
            """;

    private static final ImageRequest REQUEST = new ImageRequest("a \"quoted\"\nprompt", "blurry", 42L, 640, 480);

    @Test
    void numbersBecomeNumbersWhetherQuotedOrNot() throws Exception {
        JsonObject filled = WorkflowTemplate.fill(WORKFLOW, REQUEST);

        assertEquals(42L, filled.getAsJsonObject("3").getAsJsonObject("inputs").get("seed").getAsLong());
        assertTrue(filled.getAsJsonObject("3").getAsJsonObject("inputs").get("seed").getAsJsonPrimitive().isNumber());
        assertTrue(filled.getAsJsonObject("5").getAsJsonObject("inputs").get("width").getAsJsonPrimitive().isNumber());
        assertEquals(640, filled.getAsJsonObject("5").getAsJsonObject("inputs").get("width").getAsInt());
        assertEquals(480, filled.getAsJsonObject("5").getAsJsonObject("inputs").get("height").getAsInt());
    }

    @Test
    void textIsPutInWithoutBreakingTheJson() throws Exception {
        JsonObject filled = WorkflowTemplate.fill(WORKFLOW, REQUEST);

        assertEquals("a \"quoted\"\nprompt", filled.getAsJsonObject("6").getAsJsonObject("inputs").get("text").getAsString());
        assertEquals("bad, blurry", filled.getAsJsonObject("7").getAsJsonObject("inputs").get("text").getAsString());
        // the links between the nodes are left alone
        assertEquals("4", filled.getAsJsonObject("6").getAsJsonObject("inputs").getAsJsonArray("clip").get(0).getAsString());
    }

    @Test
    void aPromptWithABackslashOrDollarIsKeptAsItIs() throws Exception {
        ImageRequest request = new ImageRequest("C:\\path $1 {{seed}}", null, 1L, 64, 64);

        JsonObject filled = WorkflowTemplate.fill(WORKFLOW, request);

        assertEquals("C:\\path $1 {{seed}}", filled.getAsJsonObject("6").getAsJsonObject("inputs").get("text").getAsString());
        assertEquals("bad, ", filled.getAsJsonObject("7").getAsJsonObject("inputs").get("text").getAsString());
    }

    @Test
    void aWrappedPromptObjectIsUnwrapped() throws Exception {
        JsonObject filled = WorkflowTemplate.fill("{\"prompt\": {\"1\": {\"inputs\": {\"text\": \"{{prompt}}\"}}}}", REQUEST);

        assertTrue(filled.has("1"));
        assertFalse(filled.has("prompt"));
    }

    @Test
    void theEditorFormatIsRefused() {
        var e = assertThrows(BackendException.class,
                () -> WorkflowTemplate.fill("{\"nodes\": [], \"links\": []}", REQUEST));
        assertTrue(e.getMessage().contains("API Format"));
    }

    @Test
    void notJsonAndEmptyAreRefused() {
        assertThrows(BackendException.class, () -> WorkflowTemplate.fill("{not json", REQUEST));
        assertThrows(BackendException.class, () -> WorkflowTemplate.fill("[1,2]", REQUEST));
        assertThrows(BackendException.class, () -> WorkflowTemplate.fill("  ", REQUEST));
        assertThrows(BackendException.class, () -> WorkflowTemplate.fill(null, REQUEST));
    }

    // --- LoRA ---

    private static final String LORA_WORKFLOW = """
            {
              "1": {"class_type": "UNETLoader", "inputs": {"unet_name": "model.safetensors"}},
              "2": {"class_type": "LoraLoaderModelOnly", "inputs": {"model": ["1", 0], "lora_name": "{{lora}}", "strength_model": {{lora_strength}}}},
              "3": {"class_type": "KSampler", "inputs": {"model": ["2", 0], "seed": {{seed}}}}
            }
            """;

    @Test
    void theChosenLoraAndItsStrengthAreFilledIn() throws Exception {
        ImageRequest request = new ImageRequest("p", "", 1L, 64, 64, "style.safetensors", 0.75);

        JsonObject filled = WorkflowTemplate.fill(LORA_WORKFLOW, request);

        JsonObject lora = filled.getAsJsonObject("2").getAsJsonObject("inputs");
        assertEquals("style.safetensors", lora.get("lora_name").getAsString());
        assertTrue(lora.get("strength_model").getAsJsonPrimitive().isNumber());
        assertEquals(0.75, lora.get("strength_model").getAsDouble());
        assertEquals("2", filled.getAsJsonObject("3").getAsJsonObject("inputs").getAsJsonArray("model").get(0).getAsString());
    }

    @Test
    void withoutALoraTheLoaderIsLeftOut() throws Exception {
        ImageRequest request = new ImageRequest("p", "", 1L, 64, 64, "  ", 1.0);

        JsonObject filled = WorkflowTemplate.fill(LORA_WORKFLOW, request);

        assertFalse(filled.has("2"));
        var model = filled.getAsJsonObject("3").getAsJsonObject("inputs").getAsJsonArray("model");
        assertEquals("1", model.get(0).getAsString());
        assertEquals(0, model.get(1).getAsInt());
    }

    @Test
    void aLoraLoaderWithClipAndChainedLoadersAreLeftOutTogether() throws Exception {
        String workflow = """
                {
                  "4": {"class_type": "CheckpointLoaderSimple", "inputs": {"ckpt_name": "x"}},
                  "10": {"class_type": "LoraLoader", "inputs": {"model": ["4", 0], "clip": ["4", 1], "lora_name": "{{lora}}"}},
                  "11": {"class_type": "LoraLoaderModelOnly", "inputs": {"model": ["10", 0], "lora_name": " {{ lora }} "}},
                  "6": {"class_type": "CLIPTextEncode", "inputs": {"text": "{{prompt}}", "clip": ["10", 1]}},
                  "3": {"class_type": "KSampler", "inputs": {"model": ["11", 0], "positive": ["6", 0]}}
                }
                """;

        JsonObject filled = WorkflowTemplate.fill(workflow, REQUEST);

        assertFalse(filled.has("10"));
        assertFalse(filled.has("11"));
        assertEquals("4", filled.getAsJsonObject("3").getAsJsonObject("inputs").getAsJsonArray("model").get(0).getAsString());
        var clip = filled.getAsJsonObject("6").getAsJsonObject("inputs").getAsJsonArray("clip");
        assertEquals("4", clip.get(0).getAsString());
        assertEquals(1, clip.get(1).getAsInt());
        assertEquals("6", filled.getAsJsonObject("3").getAsJsonObject("inputs").getAsJsonArray("positive").get(0).getAsString());
    }

    @Test
    void aLoaderWhoseOutputCannotBeReplacedIsAnError() {
        String workflow = """
                {"2": {"class_type": "Odd", "inputs": {"lora_name": "{{lora}}"}},
                 "3": {"class_type": "KSampler", "inputs": {"model": ["2", 0]}}}
                """;

        var e = assertThrows(BackendException.class, () -> WorkflowTemplate.fill(workflow, REQUEST));
        assertTrue(e.getMessage().contains("node 2"));
    }

    @Test
    void theLoraPlaceholderIsFound() {
        assertTrue(WorkflowTemplate.usesLora(LORA_WORKFLOW));
        assertTrue(WorkflowTemplate.usesLora("{\"a\": \"{{ lora }}\"}"));
        assertFalse(WorkflowTemplate.usesLora(WORKFLOW));
        assertFalse(WorkflowTemplate.usesLora("{\"a\": {{lora_strength}}}"));
        assertFalse(WorkflowTemplate.usesLora(null));
    }
}
