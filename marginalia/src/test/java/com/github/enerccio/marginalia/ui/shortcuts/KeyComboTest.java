package com.github.enerccio.marginalia.ui.shortcuts;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeyComboTest {

    @Test
    void normalizesModifierOrderAndCase() {
        assertThat(KeyCombo.normalize("shift+ctrl+K")).isEqualTo("Ctrl+Shift+k");
        assertThat(KeyCombo.normalize("Meta+Alt+Ctrl+enter")).isEqualTo("Ctrl+Alt+Meta+Enter");
        assertThat(KeyCombo.normalize("  Ctrl+Enter ")).isEqualTo("Ctrl+Enter");
    }

    @Test
    void plusAndSpaceAreKeys() {
        assertThat(KeyCombo.normalize("+")).isEqualTo("+");
        assertThat(KeyCombo.normalize("Ctrl++")).isEqualTo("Ctrl++");
        assertThat(KeyCombo.normalize(" ")).isEqualTo("Space");
        assertThat(KeyCombo.normalize("ctrl+space")).isEqualTo("Ctrl+Space");
    }

    @Test
    void shiftIsDroppedForCharactersThatAreNoLettersOrDigits() {
        assertThat(KeyCombo.normalize("Shift++")).isEqualTo("+");
        assertThat(KeyCombo.normalize("Ctrl+Shift+.")).isEqualTo("Ctrl+.");
        assertThat(KeyCombo.normalize("Shift+a")).isEqualTo("Shift+a");
        assertThat(KeyCombo.normalize("Shift+1")).isEqualTo("Shift+1");
    }

    @Test
    void namedAndFunctionKeys() {
        assertThat(KeyCombo.normalize("escape")).isEqualTo("Escape");
        assertThat(KeyCombo.normalize("arrowup")).isEqualTo("ArrowUp");
        assertThat(KeyCombo.normalize("f5")).isEqualTo("F5");
        assertThat(KeyCombo.normalize("Alt+f12")).isEqualTo("Alt+F12");
    }

    @Test
    void invalidTextIsNull() {
        assertThat(KeyCombo.normalize(null)).isNull();
        assertThat(KeyCombo.normalize("")).isNull();
        assertThat(KeyCombo.normalize("  ")).isNull();
        assertThat(KeyCombo.normalize("Ctrl+")).isNull();
        assertThat(KeyCombo.normalize("Ctrl+Shift+")).isNull();
        assertThat(KeyCombo.normalize("Ctrl+Banana")).isNull();
        assertThat(KeyCombo.normalize("F25")).isNull();
        assertThat(KeyCombo.isValid("x")).isTrue();
        assertThat(KeyCombo.isValid("nonsense")).isFalse();
    }

    @Test
    void normalizeIsIdempotent() {
        for (String combo : new String[]{"+", "Ctrl++", "Ctrl+Shift+k", "Ctrl+Alt+Meta+Enter", "Space", "F5", "Ctrl+."}) {
            assertThat(KeyCombo.normalize(combo)).isEqualTo(combo);
        }
    }

    @Test
    void singleKeysHaveNoModifier() {
        assertThat(KeyCombo.hasModifier("s")).isFalse();
        assertThat(KeyCombo.hasModifier("+")).isFalse();
        assertThat(KeyCombo.hasModifier("Shift+a")).isFalse();
        assertThat(KeyCombo.hasModifier("Ctrl+s")).isTrue();
        assertThat(KeyCombo.hasModifier("Alt+x")).isTrue();
        assertThat(KeyCombo.hasModifier("Meta+x")).isTrue();
        assertThat(KeyCombo.hasModifier(null)).isFalse();
    }

    @Test
    void browserKeysAndAltCharacters() {
        assertThat(KeyCombo.isBrowserReserved("Ctrl+t")).isTrue();
        assertThat(KeyCombo.isBrowserReserved("Meta+w")).isTrue();
        assertThat(KeyCombo.isBrowserReserved("Ctrl+Enter")).isFalse();
        assertThat(KeyCombo.isAltCharacter("Alt+s")).isTrue();
        assertThat(KeyCombo.isAltCharacter("Ctrl+Alt+7")).isTrue();
        assertThat(KeyCombo.isAltCharacter("Alt+Enter")).isFalse();
        assertThat(KeyCombo.isAltCharacter("Alt++")).isFalse();
        assertThat(KeyCombo.isAltCharacter("Ctrl+s")).isFalse();
    }
}
