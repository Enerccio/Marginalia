package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.extensions.InheritedSetting;
import com.github.enerccio.marginalia.extensions.InheritedSetting.Choice;
import com.github.enerccio.marginalia.extensions.InheritedSettingRegistry;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersDefaults;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersSettings;
import com.github.enerccio.marginalia.extensions.betareaders.service.BetaReadersInherited;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The settings books and folders inherit are registered with the user level in the settings of the user and the built-in text or the
 * book's own model as the last level.
 */
class BetaReadersInheritedTest {

    private static final String OWNER = "betareaders";

    private final InheritedSettingRegistry registry = new InheritedSettingRegistry();
    private BetaReadersSettings settings;

    @BeforeEach
    void register() {
        settings = new BetaReadersDefaults().newSettings();
        BetaReadersInherited.register(registry, OWNER, () -> settings,
                () -> List.of(new Choice("ai-1", "Model one")), () -> List.of(new Choice("protocol-1", "Protocol one")));
    }

    private InheritedSetting setting(String key) {
        InheritedSetting setting = registry.find(key);
        assertNotNull(setting, key);
        return setting;
    }

    @Test
    void sixSettingsAreRegisteredAsOneGroup() {
        List<InheritedSettingRegistry.Group> groups = registry.groups();

        assertEquals(1, groups.size());
        assertEquals("Beta Readers", groups.get(0).title());
        assertEquals(List.of(BetaReadersInherited.SINGLE_PROMPT, BetaReadersInherited.GROUP_PROMPT, BetaReadersInherited.SCENE,
                        BetaReadersInherited.MEDIUM, BetaReadersInherited.AI, BetaReadersInherited.PROTOCOL),
                groups.get(0).settings().stream().map(InheritedSetting::getKey).toList());
    }

    @Test
    void theKindsAreTheEditorsOfTheFields() {
        assertEquals(InheritedSetting.Kind.TEXT_AREA, setting(BetaReadersInherited.SINGLE_PROMPT).getKind());
        assertEquals(InheritedSetting.Kind.TEXT_AREA, setting(BetaReadersInherited.GROUP_PROMPT).getKind());
        assertEquals(InheritedSetting.Kind.TEXT_AREA, setting(BetaReadersInherited.SCENE).getKind());
        assertEquals(InheritedSetting.Kind.CHOICE, setting(BetaReadersInherited.MEDIUM).getKind());
        assertEquals(InheritedSetting.Kind.CHOICE, setting(BetaReadersInherited.AI).getKind());
        assertEquals(InheritedSetting.Kind.CHOICE, setting(BetaReadersInherited.PROTOCOL).getKind());
    }

    @Test
    void thePromptsFallBackToTheBuiltInOnesAndTakeTheUserLevelFirst() {
        assertTrue(setting(BetaReadersInherited.SINGLE_PROMPT).userValue().isBlank());
        assertTrue(setting(BetaReadersInherited.GROUP_PROMPT).userValue().isBlank());
        assertEquals(new BetaReadersDefaults().singlePrompt(), setting(BetaReadersInherited.SINGLE_PROMPT).fallback());
        assertEquals(new BetaReadersDefaults().groupPrompt(), setting(BetaReadersInherited.GROUP_PROMPT).fallback());

        settings.setSinglePrompt("my one on one");
        settings.setGroupPrompt("my group");

        assertEquals("my one on one", setting(BetaReadersInherited.SINGLE_PROMPT).userValue());
        assertEquals("my group", setting(BetaReadersInherited.GROUP_PROMPT).userValue());
    }

    @Test
    void theSceneFallsBackToAnOnlineChatAndTakesTheUserLevelFirst() {
        assertTrue(setting(BetaReadersInherited.SCENE).userValue().isBlank());
        assertEquals(new BetaReadersDefaults().scene(), setting(BetaReadersInherited.SCENE).fallback());
        assertTrue(new BetaReadersDefaults().scene().contains("online chat"));

        settings.setScene("A rainy bar.");

        assertEquals("A rainy bar.", setting(BetaReadersInherited.SCENE).userValue());
    }

    @Test
    void theMediumsToChooseFromAreThoseOfTheUserAndTheDefaultIsTheBook() {
        InheritedSetting medium = setting(BetaReadersInherited.MEDIUM);

        assertEquals(7, medium.choices().size());
        assertEquals(new Choice("movie", "Movie"), medium.choices().get(1));
        assertEquals("book", medium.fallback());
        assertTrue(medium.choices().stream().anyMatch(choice -> choice.id().equals(medium.fallback())));

        settings.getMediums().remove(1);
        settings.setMedium("manga");

        assertEquals(6, medium.choices().size());
        assertEquals("manga", medium.userValue());
    }

    @Test
    void theModelAndTheProtocolAreChosenFromTheSuppliedOnesAndHaveNoDefault() {
        InheritedSetting ai = setting(BetaReadersInherited.AI);
        InheritedSetting protocol = setting(BetaReadersInherited.PROTOCOL);

        assertEquals(List.of(new Choice("ai-1", "Model one")), ai.choices());
        assertEquals(List.of(new Choice("protocol-1", "Protocol one")), protocol.choices());
        // nothing set: the book's model and protocol apply, which the plugin works out
        assertEquals("", ai.fallback());
        assertEquals("", protocol.fallback());

        settings.setAi("ai-1");
        settings.setProtocol("protocol-1");

        assertEquals("ai-1", ai.userValue());
        assertEquals("protocol-1", protocol.userValue());
    }

    @Test
    void theSettingsAreRemovedWithTheBundle() {
        registry.unregisterAll(OWNER);

        assertTrue(registry.groups().isEmpty());
    }
}
