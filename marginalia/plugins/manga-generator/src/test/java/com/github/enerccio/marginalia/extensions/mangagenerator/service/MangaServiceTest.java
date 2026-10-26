package com.github.enerccio.marginalia.extensions.mangagenerator.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.Manga;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.MangaData;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.MangaSection;
import com.github.enerccio.marginalia.extensions.mangagenerator.model.MangaSettings;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The text a request is made of: the placeholders of the prompts, the numbered parts of the story, the boundary rules of a section.
 */
class MangaServiceTest {

    private static ChatMessage part(String response) {
        ChatMessage message = new ChatMessage();
        message.setResponse(response);
        return message;
    }

    @Test
    void compileReplacesTheKnownPlaceholders() {
        Manga manga = new Manga();
        manga.setTokenBudget(2000);

        String text = MangaService.compile("Use {{tokenBudget}} tokens.\n{{ additionalRules }}\n{{other}}", manga, "No SFX");

        assertEquals("Use 2000 tokens.\nNo SFX\n{{other}}", text);
    }

    @Test
    void compileKeepsDollarsAndBackslashesOfTheRules() {
        String text = MangaService.compile("{{additionalRules}}", new Manga(), "costs $5 \\ more");

        assertEquals("costs $5 \\ more", text);
    }

    @Test
    void builtInPromptsHaveNoPlaceholderLeftAfterCompile() {
        Manga manga = new Manga();

        assertFalse(MangaService.compile(Defaults.DEFAULT_PROMPT, manga, "").contains("{{"));
        assertFalse(MangaService.compile(Defaults.DEFAULT_TAGS_PROMPT, manga, "").contains("{{"));
        assertFalse(Defaults.DEFAULT_PROMPT.contains("====="), "the blocks are added by the service, not the prompt");
    }

    @Test
    void partsAreNumberedFromOneAndCutToTheStory() {
        List<ChatMessage> branch = List.of(part("First."), part("Second."), part("Third."));

        assertEquals("[Part 2]\nSecond.\n\n[Part 3]\nThird.", MangaService.partsText(branch, 2, 10));
        assertEquals("[Part 1]\nFirst.", MangaService.partsText(branch, -4, 1));
        assertEquals("", MangaService.partsText(branch, 5, 6));
    }

    @Test
    void partsWithoutTextAreLeftOutButKeepTheirNumbers() {
        List<ChatMessage> branch = List.of(part("First."), part("  "), part("Third."));

        assertEquals("[Part 1]\nFirst.\n\n[Part 3]\nThird.", MangaService.partsText(branch, 1, 3));
    }

    @Test
    void noBoundaryRulesWhenTheContextIsTheAdaptedParts() {
        MangaSection section = new MangaSection();
        section.setStartPart(3);
        section.setEndPart(5);
        section.setContextStart(3);
        section.setContextEnd(5);

        assertEquals("", MangaService.boundaryRules(section));
    }

    @Test
    void boundaryRulesNameTheAdaptedPartsAndTheContextAroundThem() {
        MangaSection section = new MangaSection();
        section.setStartPart(3);
        section.setEndPart(5);
        section.setContextStart(2);
        section.setContextEnd(6);

        String rules = MangaService.boundaryRules(section);

        assertTrue(rules.contains("parts 3 to 5"));
        assertTrue(rules.contains("before them"));
        assertTrue(rules.contains("after them"));
        assertTrue(rules.contains("part 5"));
    }

    @Test
    void aNewMangaStartsWithTheDefaultsOfTheUser() {
        MangaSettings settings = new MangaSettings();
        settings.setDefaultTokenBudget(1234);
        settings.setDefaultSplitCount(7);
        settings.setDefaultContextLeft(2);
        settings.setDefaultContextRight(0);
        settings.setDefaultAdditionalRules("rules");

        Manga manga = new Manga("Title", settings);

        assertEquals("Title", manga.getTitle());
        assertEquals(1234, manga.getTokenBudget());
        assertEquals(7, manga.getSplitCount());
        assertEquals(2, manga.getContextLeft());
        assertEquals(0, manga.getContextRight());
        assertEquals("rules", manga.getAdditionalRules());
        assertNull(manga.getPromptOverride());
    }

    @Test
    void dataSurvivesTheJsonItIsStoredAs() {
        MangaData data = new MangaData();
        Manga manga = new Manga("One", new MangaSettings());
        MangaSection section = new MangaSection();
        section.setStartPart(2);
        section.setScript("# PAGE 1");
        section.setTags("1girl");
        section.setReasoningMillis(1500);
        manga.getSections().add(section);
        data.getMangas().add(manga);
        data.setActiveManga(0);

        Gson gson = new Gson();
        MangaData read = gson.fromJson(gson.toJson(data), MangaData.class);

        assertEquals("One", read.getCurrent().getTitle());
        MangaSection readSection = read.getCurrent().getSections().getFirst();
        assertEquals(2, readSection.getStartPart());
        assertEquals("# PAGE 1", readSection.getScript());
        assertEquals("1girl", readSection.getTags());
        assertEquals(1500, readSection.getReasoningMillis());
    }

    @Test
    void dataWithoutMangasHasNoCurrent() {
        MangaData read = new Gson().fromJson("{}", MangaData.class);

        assertNull(read.getCurrent());
        assertEquals(new ArrayList<>(), read.getMangas());
    }
}
