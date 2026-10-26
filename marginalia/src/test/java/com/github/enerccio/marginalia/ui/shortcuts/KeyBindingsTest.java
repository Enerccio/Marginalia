package com.github.enerccio.marginalia.ui.shortcuts;

import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The key bindings of the user: defaults, what is stored (only the differences), extensions' actions.
 */
class KeyBindingsTest extends MarginaliaTestBase {

    @Autowired
    private SettingService settingService;

    @AfterEach
    void unregisterTestActions() {
        ShortcutRegistry.unregister("test.action");
    }

    private static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }

    @Test
    void userWithoutSettingsHasTheDefaults() {
        KeyBindings bindings = KeyBindings.of(null);
        assertThat(bindings.get(ShortcutActions.STORY_NEW_TURN)).isEqualTo("+");
        assertThat(bindings.get(ShortcutActions.STORY_GENERATE)).isEqualTo("Ctrl+Enter");
        assertThat(bindings.get(ShortcutActions.STORY_SWIPE)).isEqualTo("s");
        assertThat(bindings.get(ShortcutActions.STORY_SAVE_EDIT)).isEqualTo("Ctrl+s");
    }

    @Test
    void navigationActionsHaveDefaultsThatTheBrowserDoesNotKeep() {
        KeyBindings bindings = KeyBindings.of(null);
        assertThat(bindings.get(ShortcutActions.STORY_COMMAND_BAR)).isEqualTo("/");
        assertThat(bindings.get(ShortcutActions.STORY_SEARCH)).isEqualTo("Ctrl+Shift+f");
        assertThat(bindings.get(ShortcutActions.STORY_SEARCH_REPLACE)).isEqualTo("Ctrl+Shift+h");
        assertThat(bindings.get(ShortcutActions.STORY_ANNOTATE_SELECTION)).isEqualTo("a");
        assertThat(bindings.get(ShortcutActions.DOCK_NEXT_TAB)).isEqualTo("Alt+PageDown");
        assertThat(bindings.get(ShortcutActions.DOCK_PREVIOUS_TAB)).isEqualTo("Alt+PageUp");
        assertThat(bindings.asMap().values().stream().filter(java.util.Objects::nonNull).toList())
                .doesNotHaveDuplicates()
                .noneMatch(KeyCombo::isBrowserReserved)
                .noneMatch(KeyCombo::isAltCharacter);
    }

    @Test
    void storedKeyOverridesTheDefaultAndEmptyIsNoKey() {
        UserSetting setting = new UserSetting();
        setting.setKeyBindings(json("{\"story.swipe\":\"Ctrl+Shift+S\",\"story.regenerate\":\"\",\"story.edit\":\"nonsense\"}"));
        KeyBindings bindings = KeyBindings.of(setting);
        assertThat(bindings.get(ShortcutActions.STORY_SWIPE)).isEqualTo("Ctrl+Shift+s");
        assertThat(bindings.get(ShortcutActions.STORY_REGENERATE)).isNull();
        assertThat(bindings.get(ShortcutActions.STORY_EDIT)).isNull();
        assertThat(bindings.get(ShortcutActions.STORY_NEW_TURN)).isEqualTo("+");
    }

    @Test
    void storesOnlyWhatDiffersFromTheDefaults() {
        Map<String, String> keys = KeyBindings.of(null).asMap();
        assertThat(KeyBindings.toStored(null, keys).size()).isZero();

        keys.put(ShortcutActions.STORY_SWIPE, "Ctrl+Shift+S");
        keys.put(ShortcutActions.STORY_REGENERATE, null);
        keys.put(ShortcutActions.STORY_EDIT, "e");
        assertThat(KeyBindings.toStored(null, keys))
                .isEqualTo(json("{\"story.swipe\":\"Ctrl+Shift+s\",\"story.regenerate\":\"\"}"));
    }

    @Test
    void resetToTheDefaultRemovesTheStoredKey() {
        JsonObject existing = json("{\"story.swipe\":\"x\"}");
        Map<String, String> keys = new HashMap<>();
        keys.put(ShortcutActions.STORY_SWIPE, "s");
        assertThat(KeyBindings.toStored(existing, keys).size()).isZero();
        // the argument is not modified
        assertThat(existing.size()).isEqualTo(1);
    }

    @Test
    void bindingsOfActionsThatAreNotRegisteredStay() {
        JsonObject existing = json("{\"gone.action\":\"Ctrl+g\"}");
        assertThat(KeyBindings.toStored(existing, KeyBindings.of(null).asMap())).isEqualTo(existing);
    }

    @Test
    void extensionActionsAreListedWithTheirDefaultKey() {
        ShortcutRegistry.register(new ShortcutAction("test.action", loc -> "Test", "Ctrl+Alt+t", component -> {
        }));
        assertThat(ShortcutRegistry.getIds()).contains("test.action");
        assertThat(KeyBindings.of(null).get("test.action")).isEqualTo("Ctrl+Alt+t");
        assertThat(KeyBindings.of(null).toJson()).contains("\"test.action\":\"Ctrl+Alt+t\"");

        ShortcutRegistry.unregister("test.action");
        assertThat(KeyBindings.of(null).get("test.action")).isNull();
    }

    @Test
    void actionRejectsAKeyThatIsNoCombination() {
        assertThatThrownBy(() -> new ShortcutAction("test.action", loc -> "Test", "Ctrl+Banana", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void keyBindingsTravelWithTheSetting() throws Exception {
        login();
        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        assertThat(setting.getKeyBindings().size()).isZero();

        setting.setKeyBindings(json("{\"story.swipe\":\"Ctrl+Shift+s\",\"story.regenerate\":\"\"}"));
        settingService.save(setting);

        UserSetting loaded = settingService.getOrCreate(UserSetting.class);
        assertThat(loaded.getKeyBindings()).isEqualTo(json("{\"story.swipe\":\"Ctrl+Shift+s\",\"story.regenerate\":\"\"}"));
        assertThat(KeyBindings.of(loaded).get(ShortcutActions.STORY_SWIPE)).isEqualTo("Ctrl+Shift+s");
    }
}
