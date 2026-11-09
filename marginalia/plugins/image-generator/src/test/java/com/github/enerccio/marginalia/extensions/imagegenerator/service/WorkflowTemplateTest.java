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
}
