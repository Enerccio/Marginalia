package com.github.enerccio.marginalia.extensions.loregenerator.names;

import com.github.enerccio.marginalia.domain.service.tool.ToolSelection;
import com.github.enerccio.marginalia.extensions.loregenerator.model.LoreEntryGeneration;
import com.github.enerccio.marginalia.extensions.loregenerator.model.LoreGeneratorData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The name categories and hints saved before the common tool dialog become a choice of the name tools.
 */
class LegacyNameToolsTest {

    private static final String GENERATOR = ToolSelection.ref("name-generator", "names");
    private static final String PROVIDER = ToolSelection.ref("name-provider", "names");

    @Test
    void categoriesGoToTheExtensionTheirPrefixNames() {
        ToolSelection selection = LegacyNameTools.migrate(
                List.of("Generator: Elvish", "Provider: Japan - Surnames", "Dwarvish", "Provider: Czechia - Forenames"), null, null);
        assertEquals(List.of("Elvish", "Dwarvish"), selection.get(GENERATOR).getItems(), "no prefix was the generator, the only one there was");
        assertEquals(List.of("Japan - Surnames", "Czechia - Forenames"), selection.get(PROVIDER).getItems());
        assertNull(selection.get(GENERATOR).getHint());
    }

    @Test
    void anExtensionWithoutCategoriesOrHintIsLeftOut() {
        ToolSelection selection = LegacyNameTools.migrate(List.of("Generator: Elvish"), " ", null);
        assertEquals(List.of(GENERATOR), List.copyOf(selection.getProviders().keySet()));
    }

    @Test
    void aHintAloneIsKept() {
        ToolSelection selection = LegacyNameTools.migrate(null, null, "Prefer short names");
        assertEquals(List.of(PROVIDER), List.copyOf(selection.getProviders().keySet()));
        assertEquals("Prefer short names", selection.get(PROVIDER).getHint());
        assertTrue(selection.get(PROVIDER).getItems().isEmpty());
        assertTrue(selection.isEmpty(), "a hint chooses nothing");
    }

    @Test
    void nothingSavedIsNoSelection() {
        assertNull(LegacyNameTools.migrate(null, null, null));
        assertNull(LegacyNameTools.migrate(List.of(), "", "  "));
    }

    @Test
    void storedDataIsMigratedOnceAndForgetsTheOldForm() {
        LoreGeneratorData data = new Gson().fromJson("""
                {"compactionPrompt": "c", "nameCategories": ["Provider: Japan - Surnames"], "nameToolsPrompt": "Elvish sounding"}
                """, LoreGeneratorData.class);
        data.migrateLegacyNameTools();
        assertEquals(List.of("Japan - Surnames"), data.getTools().get(PROVIDER).getItems());
        assertEquals("Elvish sounding", data.getTools().get(GENERATOR).getHint());

        JsonObject stored = new Gson().toJsonTree(data).getAsJsonObject();
        assertFalse(stored.has("nameCategories"));
        assertFalse(stored.has("nameToolsPrompt"));
        assertTrue(stored.has("tools"));
    }

    @Test
    void aChoiceMadeInTheToolDialogWinsOverTheOldForm() {
        LoreGeneratorData data = new Gson().fromJson("""
                {"tools": {"providers": {"name-provider/names": {"enabled": false, "items": ["Korea - Surnames"]}}},
                 "nameCategories": ["Provider: Japan - Surnames"]}
                """, LoreGeneratorData.class);
        data.migrateLegacyNameTools();
        assertEquals(List.of("Korea - Surnames"), data.getTools().get(PROVIDER).getItems());
        assertFalse(new Gson().toJsonTree(data).getAsJsonObject().has("nameCategories"));
    }

    @Test
    void anEntrySavedBeforeTheToolDialogHasItsCategoriesAsTools() {
        LoreEntryGeneration old = new Gson().fromJson("{\"prompt\": \"p\", \"nameCategories\": [\"Elvish\"]}", LoreEntryGeneration.class);
        assertEquals(List.of("Elvish"), old.tools().get(GENERATOR).getItems());

        LoreEntryGeneration none = new Gson().fromJson("{\"prompt\": \"p\"}", LoreEntryGeneration.class);
        assertNull(none.tools());
    }
}
