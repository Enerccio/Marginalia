package com.github.enerccio.marginalia.extensions.loregenerator.service;

import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.tool.ToolSelection;
import com.github.enerccio.marginalia.extensions.loregenerator.model.LoreEntryGeneration;
import com.github.enerccio.marginalia.extensions.loregenerator.model.LoreGeneratorData;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The answer of the compaction prompt without its code fence, and the options of a generation kept on the lore entry.
 */
class LoreGeneratorServiceTest {

    private final LoreGeneratorService service = new LoreGeneratorService();

    @Test
    void theCodeFenceIsTakenOff() {
        assertEquals("**Name:** Ash\n- tall", LoreGeneratorService.unfence("```markdown\n**Name:** Ash\n- tall\n```"));
        assertEquals("**Name:** Ash", LoreGeneratorService.unfence("\n  ```\n**Name:** Ash```  \n"));
        assertEquals("a\n```\nb", LoreGeneratorService.unfence("```\na\n```\nb\n```"), "the fence around the whole answer only");
    }

    @Test
    void anAnswerWithoutAFenceIsOnlyStripped() {
        assertEquals("**Name:** Ash", LoreGeneratorService.unfence("  **Name:** Ash \n"));
        assertEquals("Intro\n```\ncode\n```", LoreGeneratorService.unfence("Intro\n```\ncode\n```"), "text before the fence keeps it");
    }

    @Test
    void theOptionsOfAGenerationAreKeptOnTheEntry() {
        LorebookEntry entry = new LorebookEntry();
        assertNull(service.getEntryGeneration(entry));

        LoreEntryGeneration generation = new LoreEntryGeneration();
        generation.setPromptName("Default");
        generation.setPrompt("Describe {{name}}.");
        generation.setScope("BOOK");
        generation.setLimit("ALL");
        generation.setTools(new ToolSelection().withItems(ToolSelection.ref("name-provider", "names"), List.of("Japan - Surnames")));
        service.putEntryGeneration(entry, generation);

        LoreEntryGeneration read = service.getEntryGeneration(entry);
        assertEquals("Default", read.getPromptName());
        assertEquals("Describe {{name}}.", read.getPrompt());
        assertEquals("BOOK", read.getScope());
        assertEquals("ALL", read.getLimit());
        assertEquals(List.of("Japan - Surnames"), read.tools().get(ToolSelection.ref("name-provider", "names")).getItems());
    }

    @Test
    void unreadableOptionsAreNone() {
        LorebookEntry entry = new LorebookEntry();
        JsonObject attributes = new JsonObject();
        attributes.add(LoreGeneratorData.KEY, new JsonPrimitive("not an object"));
        entry.setAttributes(attributes);
        assertNull(service.getEntryGeneration(entry));
    }
}
