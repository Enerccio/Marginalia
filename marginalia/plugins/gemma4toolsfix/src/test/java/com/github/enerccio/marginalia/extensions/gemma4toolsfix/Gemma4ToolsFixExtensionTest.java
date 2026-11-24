package com.github.enerccio.marginalia.extensions.gemma4toolsfix;

import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Only Gemma 4 models get the tool role their chat template expects; every other model keeps the standard one.
 */
class Gemma4ToolsFixExtensionTest {

    private final Gemma4ToolsFixExtension extension = new Gemma4ToolsFixExtension();

    private static OpenAICompatible ai(String model) {
        OpenAICompatible ai = new OpenAICompatible();
        ai.setModel(model);
        return ai;
    }

    @Test
    void gemma4IsRecognizedInTheUsualSpellingsOfTheModelId() {
        assertTrue(Gemma4ToolsFixExtension.isGemma4("gemma4:27b"));
        assertTrue(Gemma4ToolsFixExtension.isGemma4("google/Gemma-4-27B-it"));
        assertTrue(Gemma4ToolsFixExtension.isGemma4("unsloth/GEMMA-4-12b-GGUF"));
        assertFalse(Gemma4ToolsFixExtension.isGemma4("gemma3:27b"));
        assertFalse(Gemma4ToolsFixExtension.isGemma4("google/gemma-3-4b-it"));
        assertFalse(Gemma4ToolsFixExtension.isGemma4("qwen3"));
        assertFalse(Gemma4ToolsFixExtension.isGemma4(null));
    }

    @Test
    void theToolRoleIsChangedForGemma4Only() {
        assertEquals(Gemma4ToolsFixExtension.TOOL_ROLE, extension.toolRole(ai("gemma-4-31b")));
        assertNull(extension.toolRole(ai("gemma-3-27b")), "the standard role");
        assertNull(extension.toolRole(ai(null)));
        assertNull(extension.toolRole(null));
    }
}
