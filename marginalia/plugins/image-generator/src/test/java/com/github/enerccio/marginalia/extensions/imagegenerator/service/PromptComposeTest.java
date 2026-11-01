package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromptComposeTest {

    private static ChatMessage part() {
        ChatMessage message = new ChatMessage();
        message.setResponse("Mira stepped into the rain.");
        message.setInstructions("Show the storm");
        message.setSceneSetting("A harbor at night");
        message.setPovCharacter("Mira");
        message.setPresentCharacters("Mira, Tobias");
        return message;
    }

    @Test
    void theInstructionIsTheSystemMessageAndTheSceneTheUserMessage() {
        List<LLMChatMessage> payload = ImageGeneratorService.compose("Write a prompt.", "", ImageGeneratorService.sceneText(part()), "");

        assertEquals(2, payload.size());
        assertEquals(LLMRole.SYSTEM, payload.get(0).getRole());
        assertEquals("Write a prompt.", payload.get(0).getContent());
        assertEquals(LLMRole.USER, payload.get(1).getRole());
        assertTrue(payload.get(1).getContent().contains("Scene setting:\nA harbor at night"));
        assertTrue(payload.get(1).getContent().contains("Text:\nMira stepped into the rain."));
        assertFalse(payload.get(1).getContent().contains("Details"));
    }

    @Test
    void theLoreIsAddedToTheSystemMessageInABlock() {
        List<LLMChatMessage> payload = ImageGeneratorService.compose("Write a prompt.", "[Mira]:\nRed coat, short black hair", "scene", "");

        String system = payload.get(0).getContent();
        assertTrue(system.startsWith("Write a prompt."));
        assertTrue(system.contains("LOREBOOK START"));
        assertTrue(system.contains("[Mira]:\nRed coat, short black hair"));
        assertTrue(system.indexOf("LOREBOOK START") < system.indexOf("Red coat"));
        assertTrue(system.indexOf("Red coat") < system.indexOf("LOREBOOK END"));
    }

    @Test
    void noLoreMeansNoBlock() {
        assertFalse(ImageGeneratorService.compose("x", "   ", "scene", "").get(0).getContent().contains("LOREBOOK"));
    }

    @Test
    void theDetailsOfTheUserEndTheUserMessage() {
        List<LLMChatMessage> payload = ImageGeneratorService.compose("x", "", "scene", "  at night, close up  ");

        String user = payload.get(1).getContent();
        assertTrue(user.startsWith("scene"));
        assertTrue(user.endsWith("Details the picture must follow:\nat night, close up"));
    }

    @Test
    void loreFiltersSeeTheTextInstructionsSceneCharactersAndDetails() {
        String matched = ImageGeneratorService.matchedText(part(), "wearing the amulet");

        for (String expected : List.of("Mira stepped into the rain.", "Show the storm", "A harbor at night", "Mira, Tobias", "wearing the amulet")) {
            assertTrue(matched.contains(expected), expected);
        }
    }

    @Test
    void blankFieldsAreLeftOut() {
        ChatMessage message = new ChatMessage();
        message.setResponse("Only text");

        assertEquals("Only text\n", ImageGeneratorService.matchedText(message, null));
        assertEquals("Text:\nOnly text", ImageGeneratorService.sceneText(message));
    }
}
